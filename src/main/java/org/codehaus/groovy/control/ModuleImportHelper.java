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
package org.codehaus.groovy.control;

import groovy.transform.Internal;
import org.apache.groovy.control.ModuleOptions;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shared utilities for expanding JPMS module imports into package-level
 * star imports (JEP 511 / JLS §7.5.5). Used by the parser ({@code AstBuilder}),
 * {@link ResolveVisitor}, and {@link ImportCustomizer}.
 *
 * @since 6.0.0
 */
public final class ModuleImportHelper {

    private ModuleImportHelper() { }

    /**
     * Builds a {@link ModuleFinder} following JEP 261 search order:
     * {@code --upgrade-module-path}, system modules, {@code --module-path},
     * and the compilation classpath, honoring {@code --limit-modules} and
     * {@code --add-modules} when configured.
     *
     * @param source the source unit providing configuration and classloader
     * @return a composite module finder
     */
    public static ModuleFinder moduleFinder(final SourceUnit source) {
        return ModuleOptions.observableModuleFinder(source, null, null, null);
    }

    /**
     * Collects the exported package names from the given module and,
     * recursively, from any modules it {@code requires transitive}.
     * This implements the transitive readability semantics specified
     * by JEP 511 (JLS §7.5.5).
     *
     * @param moduleRef the module to inspect
     * @param finder    the finder used to resolve transitive dependencies
     * @param packageNames accumulator for discovered package names
     * @param visited   set of already-visited module names (to avoid cycles)
     */
    public static void collectModuleExports(final ModuleReference moduleRef, final ModuleFinder finder,
                                            final List<String> packageNames, final Set<String> visited) {
        collectModuleExports(moduleRef, finder, null, Map.of(), packageNames, visited);
    }

    /**
     * Collects exported package names from {@code moduleRef} (and its
     * {@code requires transitive} closure), including qualified exports targeting
     * {@code targetModule} when non-null (JLS §7.5.5 / JEP 511).
     *
     * @param moduleRef    the module to inspect
     * @param finder       the finder used to resolve transitive dependencies
     * @param targetModule the importing module name, or {@code null} for unnamed modules
     * @param packageNames accumulator for discovered package names
     * @param visited      set of already-visited module names (to avoid cycles)
     * @since 7.0.0
     */
    public static void collectModuleExports(final ModuleReference moduleRef, final ModuleFinder finder,
                                            final String targetModule, final List<String> packageNames,
                                            final Set<String> visited) {
        collectModuleExports(moduleRef, finder, targetModule, Map.of(), packageNames, visited);
    }

    /**
     * Like {@link #collectModuleExports(ModuleReference, ModuleFinder, String, List, Set)},
     * additionally including packages granted to the current module via {@code --add-exports}
     * on {@code moduleRef} or any module read transitively through it.
     *
     * @since 7.0.0
     */
    @Internal
    public static void collectModuleExports(final ModuleReference moduleRef, final ModuleFinder finder,
                                            final String targetModule, final Map<String, Set<String>> extraExports,
                                            final List<String> packageNames, final Set<String> visited) {
        ModuleDescriptor descriptor = moduleRef.descriptor();
        String name = descriptor.name();
        if (!visited.add(name)) return;
        // Sort exports/packages/requires so package discovery order is deterministic
        // across JVM invocations (ModuleDescriptor returns randomized immutable Sets).
        if (descriptor.isAutomatic()) {
            descriptor.packages().stream()
                    .sorted()
                    .forEach(packageNames::add);
        } else {
            descriptor.exports().stream()
                    .filter(e -> !e.isQualified() || (targetModule != null && e.targets().contains(targetModule)))
                    .map(ModuleDescriptor.Exports::source)
                    .sorted()
                    .forEach(packageNames::add);
        }
        if (extraExports != null) {
            Set<String> added = extraExports.get(name);
            if (added != null) {
                added.stream()
                        .filter(pkg -> !packageNames.contains(pkg))
                        .sorted()
                        .forEach(packageNames::add);
            }
        }
        // Recursively process transitive dependencies (JEP 511 / JLS §7.5.5)
        descriptor.requires().stream()
                .filter(req -> req.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE))
                .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
                .forEach(req -> findQuietly(finder, req.name()).ifPresent(ref ->
                        collectModuleExports(ref, finder, targetModule, extraExports, packageNames, visited)));
    }

    /**
     * Resolves a module by name and collects its exported packages
     * (including transitive dependencies).
     *
     * @param moduleName the JPMS module name
     * @param finder     the module finder to use
     * @return the list of exported package names
     * @throws IllegalArgumentException if the module is not found
     */
    public static List<String> resolveModulePackages(final String moduleName, final ModuleFinder finder) {
        return resolveModulePackages(moduleName, finder, null, Map.of());
    }

    /**
     * Resolves a module by name and collects packages exported to {@code targetModule}
     * (unqualified exports plus qualified exports targeting {@code targetModule},
     * along with {@code requires transitive} dependencies).
     *
     * @param moduleName   the JPMS module name
     * @param finder       the module finder to use
     * @param targetModule the importing module name, or {@code null} for unnamed modules
     * @return the list of exported package names
     * @throws IllegalArgumentException if the module is not found
     * @since 7.0.0
     */
    public static List<String> resolveModulePackages(final String moduleName, final ModuleFinder finder,
                                                     final String targetModule) {
        return resolveModulePackages(moduleName, finder, targetModule, Map.of());
    }

    /**
     * Like {@link #resolveModulePackages(String, ModuleFinder, String)}, additionally
     * applying {@code --add-exports} grants across {@code moduleName} and its
     * {@code requires transitive} closure.
     *
     * @since 7.0.0
     */
    @Internal
    public static List<String> resolveModulePackages(final String moduleName, final ModuleFinder finder,
                                                     final String targetModule, final Map<String, Set<String>> extraExports) {
        ModuleReference moduleRef = finder.find(moduleName).orElse(null);
        if (moduleRef == null) {
            throw new IllegalArgumentException("Unknown module: " + moduleName);
        }
        List<String> packageNames = new ArrayList<>();
        collectModuleExports(moduleRef, finder, targetModule, extraExports, packageNames, new HashSet<>());
        return packageNames;
    }

    private static Optional<ModuleReference> findQuietly(final ModuleFinder finder, final String name) {
        try {
            return finder.find(name);
        } catch (FindException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Classpath and class-loader entries only, without the system module finder.
     * Compose this after {@link ModuleFinder#ofSystem()} so platform modules
     * remain observable and are not shadowed by a classpath JAR.
     */
    public static ModuleFinder classpathModuleFinder(final SourceUnit source) {
        Set<Path> paths = new LinkedHashSet<>();
        for (String entry : source.getConfiguration().getClasspath()) {
            Path p = Path.of(entry);
            if (Files.exists(p)) {
                paths.add(p);
            }
        }
        ClassLoader cl = source.getClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    // Module-path entries are files or directories. In-memory
                    // class output and other non-file URLs have no NIO provider.
                    if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
                        continue;
                    }
                    try {
                        Path p = Path.of(url.toURI());
                        if (Files.exists(p)) {
                            paths.add(p);
                        }
                    } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException ignore) {
                        // Ignore malformed or non-filesystem URLs on the class loader.
                    }
                }
            }
            cl = cl.getParent();
        }
        try {
            return ModuleFinder.of(paths.toArray(Path[]::new));
        } catch (FindException e) {
            return ModuleFinder.of();
        }
    }
}
