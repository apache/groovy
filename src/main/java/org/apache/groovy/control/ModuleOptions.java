/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.control;

import groovy.transform.Internal;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ModuleImportHelper;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Compile-time JPMS module-path and option utilities implementing JEP 261
 * search order ({@code --upgrade-module-path}, system modules, {@code --module-path},
 * classpath fallback), compile-time JMOD reading, {@code --limit-modules},
 * {@code --add-modules}, {@code --add-reads}, {@code --add-exports}, and
 * {@code --module-version} validation.
 *
 * @since 7.0.0
 */
@Internal
public final class ModuleOptions {

    public static final String ALL_UNNAMED = "ALL-UNNAMED";
    public static final String ALL_MODULE_PATH = "ALL-MODULE-PATH";
    public static final String ALL_SYSTEM = "ALL-SYSTEM";
    public static final String ALL_DEFAULT = "ALL-DEFAULT";

    private static final String JAVA_BASE = "java.base";
    private static final String INCUBATOR_PREFIX = "jdk.incubator.";
    private static final String JMOD_SUFFIX = ".jmod";
    private static final String JMOD_CLASSES_PREFIX = "classes/";
    private static final String MODULE_INFO_CLASS = "module-info.class";
    private static final String INVALID_ADD_READS = "invalid --add-reads option: ";
    private static final String INVALID_ADD_EXPORTS = "invalid --add-exports option: ";

    private ModuleOptions() {
    }

    /**
     * Splits a {@link File#pathSeparator}-delimited path string into non-empty entries.
     *
     * @param path the path string, or {@code null}
     * @return non-empty path segments in declaration order
     */
    public static List<String> splitPath(final String path) {
        if (path == null || path.isEmpty()) {
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        int start = 0;
        int sepLen = File.pathSeparator.length();
        while (start <= path.length()) {
            int idx = path.indexOf(File.pathSeparator, start);
            String token = idx >= 0 ? path.substring(start, idx) : path.substring(start);
            if (!token.isEmpty()) {
                entries.add(token);
            }
            if (idx < 0) {
                break;
            }
            start = idx + sepLen;
        }
        return entries;
    }

    /**
     * Validates a {@code --module-version} value using {@link ModuleDescriptor.Version#parse(String)}.
     *
     * @param version the version string
     * @return the parsed module version
     * @throws IllegalArgumentException if {@code version} is not a valid JPMS module version
     */
    public static ModuleDescriptor.Version validateModuleVersion(final String version) {
        return ModuleDescriptor.Version.parse(version);
    }

    /**
     * Builds a compile-time {@link ModuleFinder} over {@code entries}, supporting
     * modular JARs, exploded module directories, directories of module definitions,
     * and compile-time {@code .jmod} archives (JEP 261).
     *
     * @param entries module-path entries in search order
     * @return a finder covering every existing entry
     */
    public static ModuleFinder modulePathFinder(final List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return ModuleFinder.of();
        }
        List<ModuleFinder> finders = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            Path path = Path.of(entry);
            if (Files.exists(path)) {
                finders.add(finderForEntry(path));
            }
        }
        if (finders.isEmpty()) {
            return ModuleFinder.of();
        }
        return finders.size() == 1
                ? finders.get(0)
                : ModuleFinder.compose(finders.toArray(ModuleFinder[]::new));
    }

    /**
     * Class-loading URLs for modules located on {@code --upgrade-module-path} and
     * {@code --module-path}. Returns an empty list without filesystem I/O when
     * neither path is configured.
     *
     * @param config the compiler configuration, or {@code null}
     * @return URLs usable by a {@link URLClassLoader} in JEP 261 search order
     */
    public static List<URL> moduleClassLoaderUrls(final CompilerConfiguration config) {
        if (config == null || (config.getUpgradeModulePath().isEmpty() && config.getModulePath().isEmpty())) {
            return List.of();
        }
        List<URL> urls = new ArrayList<>();
        Set<URI> seen = new LinkedHashSet<>();
        collectModuleUrls(config.getUpgradeModulePath(), seen, urls);
        collectModuleUrls(config.getModulePath(), seen, urls);
        return urls;
    }

    /**
     * Builds the observable {@link ModuleFinder} and module-path package boundary
     * in a single pass following JEP 261 search order: compiling module (optional),
     * {@code --upgrade-module-path}, system modules, {@code --module-path}, and
     * classpath fallback, optionally restricted by {@code --limit-modules} and
     * {@code --add-modules}.
     *
     * @param source the source unit being compiled
     * @param compilingFinder optional finder for the module currently being compiled
     * @param compilingModuleName optional name of the module currently being compiled
     * @param classpathFinderOverride optional override for the classpath finder (for tests)
     * @return the observable finder and module-path bounded packages
     */
    public static Observation observeModules(final SourceUnit source, final ModuleFinder compilingFinder,
            final String compilingModuleName, final ModuleFinder classpathFinderOverride) {
        CompilerConfiguration config = source != null ? source.getConfiguration() : null;
        List<ModuleFinder> chain = new ArrayList<>(5);
        if (compilingFinder != null) {
            chain.add(compilingFinder);
        }
        boolean hasUpgrade = config != null && !config.getUpgradeModulePath().isEmpty();
        boolean hasAppPath = config != null && !config.getModulePath().isEmpty();
        ModuleFinder upgradeFinder = hasUpgrade ? modulePathFinder(config.getUpgradeModulePath()) : ModuleFinder.of();
        if (hasUpgrade) {
            chain.add(upgradeFinder);
        }
        chain.add(ModuleFinder.ofSystem());
        ModuleFinder appModuleFinder = hasAppPath ? modulePathFinder(config.getModulePath()) : ModuleFinder.of();
        if (hasAppPath) {
            chain.add(appModuleFinder);
        }
        if (classpathFinderOverride != null) {
            chain.add(classpathFinderOverride);
        } else if (source != null) {
            chain.add(ModuleImportHelper.classpathModuleFinder(source));
        }
        Set<String> boundedPackages = collectBoundedPackages(hasUpgrade, upgradeFinder, hasAppPath, appModuleFinder);
        ModuleFinder composed = dedupe(ModuleFinder.compose(chain.toArray(ModuleFinder[]::new)));
        if (config != null && !config.getLimitModules().isEmpty()) {
            Set<String> observable = computeLimitedObservable(
                    composed, upgradeFinder, appModuleFinder, compilingModuleName, config);
            composed = limitFinder(composed, observable);
        }
        return new Observation(composed, boundedPackages);
    }

    /**
     * Convenience wrapper returning only the observable {@link ModuleFinder}.
     */
    public static ModuleFinder observableModuleFinder(final SourceUnit source, final ModuleFinder compilingFinder,
            final String compilingModuleName, final ModuleFinder classpathFinderOverride) {
        return observeModules(source, compilingFinder, compilingModuleName, classpathFinderOverride).finder();
    }

    /**
     * Parses {@code --add-reads <source>=<target>(,<target>)*} specifications and extracts
     * extra readability edges granted to {@code compilingModuleName}.
     *
     * @param specs raw option values
     * @param compilingModuleName name of the module being compiled
     * @param errorSink callback receiving malformed-option diagnostics
     * @return parsed readability additions for {@code compilingModuleName}
     */
    public static AddReads parseAddReads(final List<String> specs, final String compilingModuleName,
            final Consumer<String> errorSink) {
        if (specs == null || specs.isEmpty()) {
            return AddReads.EMPTY;
        }
        Set<String> modules = new LinkedHashSet<>();
        boolean readsUnnamed = false;
        for (String spec : specs) {
            int eq = spec != null ? spec.indexOf('=') : -1;
            if (eq <= 0 || eq == spec.length() - 1) {
                errorSink.accept(INVALID_ADD_READS + spec);
                continue;
            }
            String source = spec.substring(0, eq).trim();
            List<String> targets = splitCommaList(spec.substring(eq + 1));
            if (source.isEmpty() || targets.isEmpty()) {
                errorSink.accept(INVALID_ADD_READS + spec);
                continue;
            }
            if (!source.equals(compilingModuleName)) {
                continue;
            }
            for (String target : targets) {
                if (ALL_UNNAMED.equals(target)) {
                    readsUnnamed = true;
                } else {
                    modules.add(target);
                }
            }
        }
        return modules.isEmpty() && !readsUnnamed
                ? AddReads.EMPTY
                : new AddReads(Set.copyOf(modules), readsUnnamed);
    }

    /**
     * Parses {@code --add-exports <source>/<package>=<target>(,<target>)*} specifications
     * and extracts extra package exports granted to {@code targetModuleName}.
     *
     * @param specs raw option values
     * @param targetModuleName name of the module being compiled
     * @param errorSink callback receiving malformed-option diagnostics
     * @return map of {@code sourceModule -> exported packages} granted to {@code targetModuleName}
     */
    public static Map<String, Set<String>> parseAddExports(final List<String> specs, final String targetModuleName,
            final Consumer<String> errorSink) {
        if (specs == null || specs.isEmpty()) {
            return Map.of();
        }
        Map<String, Set<String>> exportsBySource = new LinkedHashMap<>();
        for (String spec : specs) {
            int eq = spec != null ? spec.indexOf('=') : -1;
            int slash = eq > 0 ? spec.indexOf('/') : -1;
            if (slash <= 0 || slash >= eq - 1 || eq == spec.length() - 1) {
                errorSink.accept(INVALID_ADD_EXPORTS + spec);
                continue;
            }
            String source = spec.substring(0, slash).trim();
            String pkg = spec.substring(slash + 1, eq).trim();
            List<String> targets = splitCommaList(spec.substring(eq + 1));
            if (source.isEmpty() || pkg.isEmpty() || targets.isEmpty()) {
                errorSink.accept(INVALID_ADD_EXPORTS + spec);
                continue;
            }
            if (targets.contains(targetModuleName)) {
                exportsBySource.computeIfAbsent(source, k -> new LinkedHashSet<>()).add(pkg);
            }
        }
        if (exportsBySource.isEmpty()) {
            return Map.of();
        }
        Map<String, Set<String>> result = new LinkedHashMap<>();
        exportsBySource.forEach((mod, pkgs) -> result.put(mod, Set.copyOf(pkgs)));
        return Map.copyOf(result);
    }

    /**
     * Splits a comma-separated list of module names, rejecting empty elements.
     */
    public static List<String> splitCommaList(final String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        for (String part : raw.split(",", -1)) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                return List.of();
            }
            items.add(trimmed);
        }
        return items;
    }

    /**
     * Deduplicates a composed {@link ModuleFinder} so {@code findAll()} preserves
     * first-occurrence-wins semantics matching {@link ModuleFinder#find(String)}.
     */
    public static ModuleFinder dedupe(final ModuleFinder finder) {
        return new ModuleFinder() {
            @Override
            public Optional<ModuleReference> find(final String name) {
                return finder.find(name);
            }

            @Override
            public Set<ModuleReference> findAll() {
                Map<String, ModuleReference> unique = new LinkedHashMap<>();
                for (ModuleReference reference : finder.findAll()) {
                    unique.putIfAbsent(reference.descriptor().name(), reference);
                }
                return new HashSet<>(unique.values());
            }
        };
    }

    private static Set<String> collectBoundedPackages(final boolean hasUpgrade, final ModuleFinder upgradeFinder,
            final boolean hasAppPath, final ModuleFinder appModuleFinder) {
        if (!hasUpgrade && !hasAppPath) {
            return Set.of();
        }
        Set<String> packages = new LinkedHashSet<>();
        if (hasUpgrade) {
            collectPackagesFromFinder(upgradeFinder, packages);
        }
        if (hasAppPath) {
            collectPackagesFromFinder(appModuleFinder, packages);
        }
        return Set.copyOf(packages);
    }

    private static void collectPackagesFromFinder(final ModuleFinder finder, final Set<String> packages) {
        try {
            for (ModuleReference ref : finder.findAll()) {
                packages.addAll(ref.descriptor().packages());
            }
        } catch (FindException ignored) {
            // Broken entries are reported during module-graph resolution.
        }
    }

    /**
     * Whether {@code moduleName} is readable by the unnamed module under JEP 261
     * and JEP 511 default-root rules. Non-system modules (from {@code --upgrade-module-path},
     * {@code --module-path}, or the classpath) are readable when observable. System modules
     * are readable when they belong to the default root closure (unqualified-exporting,
     * non-incubator system modules and their dependencies) or are explicitly added via
     * {@code --add-modules} (including {@code ALL-SYSTEM}).
     *
     * @param moduleName the module name to test
     * @param observableFinder the observable module finder
     * @param config the compiler configuration, or {@code null}
     * @return {@code true} when an unnamed module may read {@code moduleName}
     */
    public static boolean isReadableByUnnamedModule(final String moduleName, final ModuleFinder observableFinder,
            final CompilerConfiguration config) {
        if (moduleName == null || moduleName.isEmpty()) {
            return false;
        }
        if (findQuietly(ModuleFinder.ofSystem(), moduleName).isEmpty()) {
            return true;
        }
        if (DefaultSystemModules.READS.contains(moduleName)) {
            return true;
        }
        if (config == null || config.getAddModules().isEmpty()) {
            return false;
        }
        List<String> added = config.getAddModules();
        if (added.contains(ALL_SYSTEM)) {
            return true;
        }
        ModuleFinder effectiveFinder = observableFinder != null ? observableFinder : ModuleFinder.ofSystem();
        Set<String> seeds = new LinkedHashSet<>();
        for (String seed : added) {
            if (ALL_MODULE_PATH.equals(seed)) {
                addNonSystemModuleNames(effectiveFinder, seeds);
            } else if (seed != null && !seed.isEmpty() && !ALL_DEFAULT.equals(seed)) {
                seeds.add(seed);
            }
        }
        if (seeds.isEmpty()) {
            return false;
        }
        Set<String> reachable = new LinkedHashSet<>();
        collectRequiresClosure(effectiveFinder, seeds, reachable);
        return reachable.contains(moduleName);
    }

    /**
     * Default system root modules for the unnamed module per JEP 261 / JEP 511:
     * system modules exporting at least one package without qualification, excluding
     * incubator modules ({@code jdk.incubator.*}), always including {@code java.base}.
     */
    static Set<String> defaultSystemRootModules() {
        return DefaultSystemModules.ROOTS;
    }

    private static Set<String> computeLimitedObservable(final ModuleFinder fullFinder, final ModuleFinder upgradeFinder,
            final ModuleFinder appModuleFinder, final String compilingModuleName, final CompilerConfiguration config) {
        Set<String> seeds = new LinkedHashSet<>(config.getLimitModules());
        seeds.add(JAVA_BASE);
        for (String added : config.getAddModules()) {
            if (ALL_MODULE_PATH.equals(added)) {
                addAllModuleNames(upgradeFinder, seeds);
                addAllModuleNames(appModuleFinder, seeds);
            } else if (ALL_SYSTEM.equals(added)) {
                addAllModuleNames(ModuleFinder.ofSystem(), seeds);
            } else if (ALL_DEFAULT.equals(added)) {
                seeds.addAll(DefaultSystemModules.ROOTS);
            } else if (added != null && !added.isEmpty()) {
                seeds.add(added);
            }
        }
        Set<String> observable = new LinkedHashSet<>();
        if (compilingModuleName != null && !compilingModuleName.isEmpty()) {
            // The compiling module itself must be observable as the root, but its
            // own requires clauses must not widen the --limit-modules universe.
            observable.add(compilingModuleName);
        }
        collectRequiresClosure(fullFinder, seeds, observable);
        return observable;
    }

    private static void addNonSystemModuleNames(final ModuleFinder finder, final Set<String> target) {
        ModuleFinder system = ModuleFinder.ofSystem();
        try {
            for (ModuleReference ref : finder.findAll()) {
                String name = ref.descriptor().name();
                if (findQuietly(system, name).isEmpty()) {
                    target.add(name);
                }
            }
        } catch (FindException ignored) {
            // Malformed module-path entries surface during graph resolution.
        }
    }

    private static void collectRequiresClosure(final ModuleFinder finder, final Set<String> seeds, final Set<String> out) {
        Deque<String> queue = new ArrayDeque<>(seeds);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!out.add(current)) {
                continue;
            }
            Optional<ModuleReference> ref = findQuietly(finder, current);
            if (ref.isEmpty()) {
                continue;
            }
            for (ModuleDescriptor.Requires req : ref.get().descriptor().requires()) {
                if (!out.contains(req.name())) {
                    queue.addLast(req.name());
                }
            }
        }
    }

    private static final class DefaultSystemModules {
        static final Set<String> ROOTS;
        static final Set<String> READS;

        static {
            ModuleFinder system = ModuleFinder.ofSystem();
            Set<String> roots = new LinkedHashSet<>();
            roots.add(JAVA_BASE);
            for (ModuleReference ref : system.findAll()) {
                ModuleDescriptor md = ref.descriptor();
                String name = md.name();
                if (!name.startsWith(INCUBATOR_PREFIX) && md.exports().stream().anyMatch(e -> !e.isQualified())) {
                    roots.add(name);
                }
            }
            ROOTS = Set.copyOf(roots);
            Set<String> reads = new LinkedHashSet<>();
            collectRequiresClosure(system, ROOTS, reads);
            READS = Set.copyOf(reads);
        }
    }

    private static void addAllModuleNames(final ModuleFinder finder, final Set<String> target) {
        try {
            for (ModuleReference ref : finder.findAll()) {
                target.add(ref.descriptor().name());
            }
        } catch (FindException ignored) {
            // Malformed module-path entries surface during graph resolution.
        }
    }

    private static ModuleFinder limitFinder(final ModuleFinder delegate, final Set<String> allowed) {
        return new ModuleFinder() {
            @Override
            public Optional<ModuleReference> find(final String name) {
                return allowed.contains(name) ? delegate.find(name) : Optional.empty();
            }

            @Override
            public Set<ModuleReference> findAll() {
                Set<ModuleReference> filtered = new LinkedHashSet<>();
                for (ModuleReference ref : delegate.findAll()) {
                    if (allowed.contains(ref.descriptor().name())) {
                        filtered.add(ref);
                    }
                }
                return filtered;
            }
        };
    }

    private static Optional<ModuleReference> findQuietly(final ModuleFinder finder, final String name) {
        try {
            return finder.find(name);
        } catch (FindException ignored) {
            return Optional.empty();
        }
    }

    private static void collectModuleUrls(final List<String> entries, final Set<URI> seen, final List<URL> urls) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        ModuleFinder finder = modulePathFinder(entries);
        Set<ModuleReference> refs;
        try {
            refs = finder.findAll();
        } catch (FindException ignored) {
            return;
        }
        for (ModuleReference ref : refs) {
            URI loc = ref.location().orElse(null);
            if (loc == null || !seen.add(loc)) {
                continue;
            }
            URL url = toClassLoaderUrl(loc);
            if (url != null) {
                urls.add(url);
            }
        }
    }

    static URL toClassLoaderUrl(final URI location) {
        try {
            String path = location.getPath();
            if (path != null && path.endsWith(JMOD_SUFFIX)) {
                return URI.create("jar:" + location + "!/" + JMOD_CLASSES_PREFIX).toURL();
            }
            return location.toURL();
        } catch (MalformedURLException | IllegalArgumentException ignored) {
            return null;
        }
    }

    private static ModuleFinder finderForEntry(final Path path) {
        if (isJmodFile(path)) {
            return new SingleEntryModuleFinder(List.of(path), List.of());
        }
        if (Files.isDirectory(path) && !Files.isRegularFile(path.resolve(MODULE_INFO_CLASS))) {
            return directoryModuleFinder(path);
        }
        return ModuleFinder.of(path);
    }

    private static ModuleFinder directoryModuleFinder(final Path dir) {
        List<Path> jmods = new ArrayList<>();
        List<Path> others = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            children.forEach(child -> {
                if (isJmodFile(child)) {
                    jmods.add(child);
                } else if (Files.isDirectory(child) || child.getFileName().toString().endsWith(".jar")) {
                    others.add(child);
                }
            });
        } catch (IOException ignored) {
            return ModuleFinder.of(dir);
        }
        if (jmods.isEmpty()) {
            return ModuleFinder.of(dir);
        }
        return new SingleEntryModuleFinder(jmods, others);
    }

    private static boolean isJmodFile(final Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(JMOD_SUFFIX);
    }

    private static ModuleReference readJmodReference(final Path jmodPath) {
        try (ZipFile zip = new ZipFile(jmodPath.toFile())) {
            ZipEntry entry = zip.getEntry(JMOD_CLASSES_PREFIX + MODULE_INFO_CLASS);
            if (entry == null) {
                throw new FindException("Missing " + JMOD_CLASSES_PREFIX + MODULE_INFO_CLASS + " in " + jmodPath);
            }
            try (InputStream in = zip.getInputStream(entry)) {
                ModuleDescriptor descriptor = ModuleDescriptor.read(in, () -> scanJmodPackages(zip));
                return new JmodModuleReference(descriptor, jmodPath.toUri());
            }
        } catch (IOException e) {
            throw new FindException("Unable to read JMOD file: " + jmodPath, e);
        }
    }

    private static Set<String> scanJmodPackages(final ZipFile zip) {
        Set<String> packages = new LinkedHashSet<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            if (ze.isDirectory()) {
                continue;
            }
            String name = ze.getName();
            if (!name.startsWith(JMOD_CLASSES_PREFIX) || !name.endsWith(".class")) {
                continue;
            }
            String rel = name.substring(JMOD_CLASSES_PREFIX.length());
            int slash = rel.lastIndexOf('/');
            if (slash > 0 && !rel.startsWith("META-INF/")) {
                packages.add(rel.substring(0, slash).replace('/', '.'));
            }
        }
        return packages;
    }

    /**
     * Result of scanning the observable module universe and module-path package boundary.
     */
    public record Observation(ModuleFinder finder, Set<String> boundedPackages) {
    }

    /**
     * Parsed {@code --add-reads} additions targeting the module being compiled.
     */
    public static final class AddReads {
        public static final AddReads EMPTY = new AddReads(Set.of(), false);

        private final Set<String> modules;
        private final boolean readsUnnamed;

        private AddReads(final Set<String> modules, final boolean readsUnnamed) {
            this.modules = modules;
            this.readsUnnamed = readsUnnamed;
        }

        public Set<String> getModules() {
            return modules;
        }

        public boolean isReadsUnnamed() {
            return readsUnnamed;
        }
    }

    /**
     * {@link ModuleFinder} for a single module-path entry containing at least one
     * {@code .jmod} archive. Preserves JEP 261's rule that duplicate module names
     * within the same directory fail with {@link FindException}.
     */
    private static final class SingleEntryModuleFinder implements ModuleFinder {
        private final List<Path> jmods;
        private final List<Path> others;
        private Map<String, ModuleReference> resolved;

        private SingleEntryModuleFinder(final List<Path> jmods, final List<Path> others) {
            this.jmods = jmods;
            this.others = others;
        }

        @Override
        public Optional<ModuleReference> find(final String name) {
            return Optional.ofNullable(load().get(name));
        }

        @Override
        public Set<ModuleReference> findAll() {
            return Set.copyOf(load().values());
        }

        private Map<String, ModuleReference> load() {
            if (resolved != null) {
                return resolved;
            }
            Map<String, ModuleReference> map = new HashMap<>();
            for (Path other : others) {
                for (ModuleReference ref : ModuleFinder.of(other).findAll()) {
                    putUnique(map, ref);
                }
            }
            for (Path jmod : jmods) {
                putUnique(map, readJmodReference(jmod));
            }
            resolved = map;
            return map;
        }

        private static void putUnique(final Map<String, ModuleReference> map, final ModuleReference ref) {
            String name = ref.descriptor().name();
            if (map.putIfAbsent(name, ref) != null) {
                throw new FindException("Two versions of module " + name + " found in module path directory");
            }
        }
    }

    private static final class JmodModuleReference extends ModuleReference {
        private JmodModuleReference(final ModuleDescriptor descriptor, final URI location) {
            super(descriptor, location);
        }

        @Override
        public ModuleReader open() {
            return new ModuleReader() {
                @Override
                public Optional<URI> find(final String name) {
                    return Optional.empty();
                }

                @Override
                public Stream<String> list() {
                    return Stream.empty();
                }

                @Override
                public void close() {
                    // Nothing to release for a descriptor-only JMOD reference.
                }
            };
        }
    }
}
