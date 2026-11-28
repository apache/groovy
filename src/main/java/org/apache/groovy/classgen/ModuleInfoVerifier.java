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
package org.apache.groovy.classgen;

import org.apache.groovy.ast.ModuleInfoNode;
import org.apache.groovy.ast.ModuleInfoNode.Exports;
import org.apache.groovy.ast.ModuleInfoNode.Opens;
import org.apache.groovy.ast.ModuleInfoNode.PackageDirective;
import org.apache.groovy.ast.ModuleInfoNode.Provides;
import org.apache.groovy.ast.ModuleInfoNode.Requires;
import org.apache.groovy.ast.ModuleInfoNode.Uses;
import org.apache.groovy.control.ModuleInfoResolveVisitor;
import org.apache.groovy.control.ModuleOptions;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompiledClassIndex;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.codehaus.groovy.ast.tools.WideningCategories.implementsInterfaceOrSubclassOf;

/**
 * Compile-time checks for a module declaration, following JLS §7.7.
 * Directive types are resolved by {@link ModuleInfoResolveVisitor}. This
 * verifier checks the module graph and whether those types are accessible.
 */
public final class ModuleInfoVerifier {

    private static final String JAVA_BASE = "java.base";
    private static final String MODULE_NOT_FOUND = "module not found: ";
    private static final String NOT_ACCESSIBLE_IN_MODULE = " is not accessible in module ";
    private static final String PROVIDER_PREFIX = "provider ";

    private final SourceUnit source;
    private final CompilationUnit compilationUnit;
    private final CompileUnit compileUnit;
    private final CompiledClassIndex compiledClasses;
    private final ModuleInfoNode declaration;
    private final ModuleFinder classpathFinder;
    private final ModuleFinder accessibilityFinder;
    private final boolean reportGraphErrors;
    private final Set<String> modulePackages = new LinkedHashSet<>();
    private final Set<String> allModulePackages = new LinkedHashSet<>();
    private ModuleFinder finder = ModuleFinder.ofSystem();
    private Set<String> reads = Set.of(JAVA_BASE);
    private Map<String, Set<String>> extraExports = Map.of();
    private Set<String> accessiblePackages;
    private boolean resolutionFailed;

    private ModuleInfoVerifier(final SourceUnit source, final ModuleInfoNode declaration,
            final CompilationUnit compilationUnit, final ModuleFinder classpathFinder,
            final ModuleFinder accessibilityFinder, final boolean reportGraphErrors) {
        this.source = source;
        this.declaration = declaration;
        this.compilationUnit = compilationUnit;
        this.classpathFinder = classpathFinder;
        this.accessibilityFinder = accessibilityFinder;
        this.reportGraphErrors = reportGraphErrors;
        this.compiledClasses = compilationUnit != null ? compilationUnit.getCompiledClasses() : null;
        ModuleNode moduleNode = declaration.getSourceModule();
        CompileUnit unit = moduleNode != null ? moduleNode.getUnit() : null;
        if (unit == null && source.getAST() != null) {
            unit = source.getAST().getUnit();
        }
        if (unit == null && compilationUnit != null) {
            unit = compilationUnit.getAST();
        }
        this.compileUnit = unit;
    }

    /**
     * Version string recorded on a {@code requires} edge, or {@code null} when the
     * module has none or the finder cannot be read.
     */
    public static String readModuleVersion(final ModuleFinder finder, final String moduleName) {
        try {
            return finder.find(moduleName)
                    .filter(ref -> !ref.descriptor().isAutomatic())
                    .flatMap(ref -> ref.descriptor().rawVersion())
                    .orElse(null);
        } catch (FindException ignored) {
            return null;
        }
    }

    /**
     * Resolves the readability set without reporting errors.
     * Source classes resolve after this, and use the set when it is ready.
     * The full check, including errors, still runs at class generation.
     *
     * @param source the modular compilation unit, or {@code null}
     * @param declaration the module declaration, or {@code null}
     */
    public static void prepareReads(final SourceUnit source, final ModuleInfoNode declaration) {
        if (source == null || declaration == null) {
            return;
        }
        new ModuleInfoVerifier(source, declaration, null, null, null, false).prepareGraph();
    }

    /**
     * Checks {@code declaration}. No-op when either argument is {@code null}.
     *
     * @param source the source under compilation
     * @param declaration the module declaration
     */
    public static void check(final SourceUnit source, final ModuleInfoNode declaration) {
        check(source, declaration, null, null, null);
    }

    /**
     * Like {@link #check(SourceUnit, ModuleInfoNode)} for a known compilation unit.
     * Java class names recorded by joint compilation are read from its
     * {@link CompiledClassIndex}.
     *
     * @param source the source under compilation
     * @param declaration the module declaration
     * @param compilationUnit the compilation, or {@code null}
     * @since 7.0.0
     */
    public static void check(final SourceUnit source, final ModuleInfoNode declaration, final CompilationUnit compilationUnit) {
        check(source, declaration, compilationUnit, null, null);
    }

    /**
     * Like {@link #check(SourceUnit, ModuleInfoNode)} but with test-supplied finders
     * in place of the classpath finder and the post-resolution accessibility finder.
     */
    static void check(final SourceUnit source, final ModuleInfoNode declaration,
            final ModuleFinder classpathFinder, final ModuleFinder accessibilityFinder) {
        check(source, declaration, null, classpathFinder, accessibilityFinder);
    }

    static void check(final SourceUnit source, final ModuleInfoNode declaration, final CompilationUnit compilationUnit,
            final ModuleFinder classpathFinder, final ModuleFinder accessibilityFinder) {
        if (declaration == null || source == null) {
            return;
        }
        new ModuleInfoVerifier(source, declaration, compilationUnit, classpathFinder, accessibilityFinder, true).verify();
    }

    private void prepareGraph() {
        if (!checkRequires()) {
            declaration.adoptResolution(Set.of(JAVA_BASE), ModuleFinder.ofSystem(), true);
            return;
        }
        resolveModuleGraph();
    }

    private void verify() {
        collectPackages();
        boolean requiresValid = checkRequires();
        checkPackageDirectives(declaration.getExports(), false);
        checkPackageDirectives(declaration.getOpens(), true);
        if (requiresValid) {
            resolveModuleGraph();
        }
        if (!declaration.hasResolvedTypes()) {
            ModuleInfoResolveVisitor.resolve(source, declaration, compilationUnit);
        }
        if (requiresValid && !resolutionFailed) {
            checkImports();
        }
        checkAnnotations();
        checkUses();
        checkProvides();
    }

    private void collectPackages() {
        boolean unnamedReported = collectCompileUnitPackages();
        if (compiledClasses != null) {
            allModulePackages.addAll(compiledClasses.getPackages());
            modulePackages.addAll(compiledClasses.getTypePackages());
            if (compiledClasses.hasUnnamedClass() && !unnamedReported) {
                error("unnamed package is not allowed in named modules", declaration);
            }
        }
    }

    private boolean collectCompileUnitPackages() {
        boolean unnamedReported = false;
        if (compileUnit == null) {
            return false;
        }
        for (ClassNode cn : compileUnit.getClasses()) {
            String pkg = cn.getPackageName();
            if (pkg == null || pkg.isEmpty()) {
                error("unnamed package is not allowed in named modules", cn);
                unnamedReported = true;
            } else {
                allModulePackages.add(pkg);
                if (!"package-info".equals(cn.getNameWithoutPackage())) {
                    modulePackages.add(pkg);
                }
            }
        }
        return unnamedReported;
    }

    /**
     * @return {@code false} when the requires clauses cannot be resolved as a module graph
     */
    private boolean checkRequires() {
        String moduleName = declaration.getName();
        boolean valid = true;
        Set<String> seen = new HashSet<>();
        if (JAVA_BASE.equals(moduleName)) {
            for (Requires requires : declaration.getRequires()) {
                error("requires directive is not allowed in module java.base", requires);
                valid = false;
            }
            return valid;
        }
        for (Requires requires : declaration.getRequires()) {
            String dependency = requires.getModuleName();
            if (!seen.add(dependency)) {
                error("duplicate requires on module " + dependency, requires);
                valid = false;
            }
            if (moduleName.equals(dependency)) {
                error("module " + moduleName + " requires itself", requires);
                valid = false;
            }
            if (JAVA_BASE.equals(dependency) && (requires.isStaticPhase() || (requires.isTransitive() && !allowsTransitiveJavaBase()))) {
                error("requires java.base must not have modifiers", requires);
                valid = false;
            }
        }
        return valid;
    }

    private void checkPackageDirectives(final List<? extends PackageDirective> directives, final boolean opens) {
        if (opens && declaration.isOpen()) {
            for (PackageDirective directive : directives) {
                error("opens directive is not allowed in an open module", directive);
            }
        }
        Set<String> seen = new HashSet<>();
        for (PackageDirective directive : directives) {
            checkPackageDirective(directive, opens, seen);
        }
    }

    private void checkPackageDirective(final PackageDirective directive, final boolean opens, final Set<String> seen) {
        String packageName = directive.getPackageName();
        if (!seen.add(packageName)) {
            error("duplicate " + (opens ? "opens" : "exports") + " of package " + packageName, directive);
        }
        if (!modulePackages.contains(packageName)) {
            // javac: exports of an empty or missing package is an error; opens is a warning.
            String message = "package is empty or does not exist: " + packageName;
            if (opens) {
                warn(WarningMessage.LIKELY_ERRORS, message, directive);
            } else {
                error(message, directive);
            }
        }
        Set<String> friends = new HashSet<>();
        for (String target : directive.getTargets()) {
            if (!friends.add(target)) {
                error("duplicate module " + target + " in to clause", directive);
            }
        }
    }

    private void resolveModuleGraph() {
        String moduleName = declaration.getName();
        CompilerConfiguration config = source.getConfiguration();
        ModuleOptions.AddReads extraReads = config != null
                ? ModuleOptions.parseAddReads(config.getAddReads(), moduleName, msg -> error(msg, declaration))
                : ModuleOptions.AddReads.EMPTY;
        extraExports = config != null
                ? ModuleOptions.parseAddExports(config.getAddExports(), moduleName, msg -> error(msg, declaration))
                : Map.of();
        Set<String> boundedPackages = Set.of();
        try {
            ModuleOptions.Observation observation = observeModules(moduleName, buildModuleDescriptor(moduleName));
            ModuleFinder resolutionFinder = observation.finder();
            boundedPackages = observation.boundedPackages();
            finder = accessibilityFinder != null ? accessibilityFinder : resolutionFinder;
            accessiblePackages = null;
            Configuration configuration = Configuration.resolve(
                    resolutionFinder, List.of(Configuration.empty()), ModuleFinder.of(), Set.of(moduleName));
            reads = collectReadableModules(moduleName, configuration, resolutionFinder, extraReads);
            // requires static is optional at resolution time, but javac still requires the
            // module to be observable and acyclic at compile time.
            checkRequiresFound(resolutionFinder);
            checkStaticRequiresCycles(resolutionFinder);
            if (reportGraphErrors) {
                checkExtraExportsSources(resolutionFinder);
                checkModuleWarnings(resolutionFinder);
                checkSplitPackages();
            }
        } catch (FindException | ResolutionException | IllegalArgumentException e) {
            handleResolutionFailure(moduleName, e);
        }
        declaration.adoptResolution(reads, finder, resolutionFailed, extraExports, boundedPackages, extraReads.isReadsUnnamed());
    }

    private ModuleDescriptor buildModuleDescriptor(final String moduleName) {
        ModuleDescriptor.Builder builder = declaration.isOpen()
                ? ModuleDescriptor.newOpenModule(moduleName)
                : ModuleDescriptor.newModule(moduleName);
        applyModuleVersion(builder);
        if (!JAVA_BASE.equals(moduleName)) {
            addRequiresToDescriptor(builder);
        }
        Set<String> builtExports = new HashSet<>();
        for (Exports exports : declaration.getExports()) {
            if ((!reportGraphErrors || modulePackages.contains(exports.getPackageName()))
                    && builtExports.add(exports.getPackageName())) {
                addPackage(builder, exports, true);
            }
        }
        if (!declaration.isOpen()) {
            Set<String> builtOpens = new HashSet<>();
            for (Opens opens : declaration.getOpens()) {
                if (builtOpens.add(opens.getPackageName())) {
                    addPackage(builder, opens, false);
                }
            }
        }
        return builder.build();
    }

    private void applyModuleVersion(final ModuleDescriptor.Builder builder) {
        CompilerConfiguration config = source.getConfiguration();
        String version = config != null ? config.getModuleVersion() : null;
        if (version == null || version.isEmpty()) {
            return;
        }
        try {
            builder.version(ModuleOptions.validateModuleVersion(version));
        } catch (IllegalArgumentException e) {
            error("bad value for --module-version option: '" + version + "'", declaration);
        }
    }

    private void addRequiresToDescriptor(final ModuleDescriptor.Builder builder) {
        for (Requires requires : declaration.getRequires()) {
            Set<ModuleDescriptor.Requires.Modifier> modifiers = new LinkedHashSet<>();
            if (requires.isTransitive()) {
                modifiers.add(ModuleDescriptor.Requires.Modifier.TRANSITIVE);
            }
            if (requires.isStaticPhase()) {
                modifiers.add(ModuleDescriptor.Requires.Modifier.STATIC);
            }
            if (modifiers.isEmpty()) {
                builder.requires(requires.getModuleName());
            } else {
                builder.requires(modifiers, requires.getModuleName());
            }
        }
    }

    private ModuleOptions.Observation observeModules(final String moduleName, final ModuleDescriptor descriptor) {
        ModuleReference reference = new CompilingModuleReference(descriptor);
        ModuleFinder compiling = new ModuleFinder() {
            @Override
            public Optional<ModuleReference> find(final String name) {
                return moduleName.equals(name) ? Optional.of(reference) : Optional.empty();
            }

            @Override
            public Set<ModuleReference> findAll() {
                return Set.of(reference);
            }
        };
        // Compile-time resolution follows JEP 261 search order independent of the host boot layer:
        // compiling module -> --upgrade-module-path -> system modules -> --module-path -> classpath,
        // restricted by --limit-modules / --add-modules when configured.
        return ModuleOptions.observeModules(source, compiling, moduleName, classpathFinder);
    }

    private Set<String> collectReadableModules(final String moduleName, final Configuration configuration,
            final ModuleFinder resolutionFinder, final ModuleOptions.AddReads extraReads) {
        Set<String> readable = new LinkedHashSet<>();
        configuration.findModule(moduleName).ifPresent(resolved ->
                resolved.reads().forEach(other -> readable.add(other.name())));
        if (!JAVA_BASE.equals(moduleName)) {
            readable.add(JAVA_BASE);
        }
        Set<String> seeds = new LinkedHashSet<>(readable);
        for (Requires requires : declaration.getRequires()) {
            seeds.add(requires.getModuleName());
        }
        for (String target : extraReads.getModules()) {
            if (findModule(resolutionFinder, target).isPresent()) {
                seeds.add(target);
            } else {
                warn(WarningMessage.LIKELY_ERRORS, "module name in --add-reads option not found: " + target, declaration);
            }
        }
        expandCompileTimeReads(seeds, readable);
        return readable;
    }

    private void handleResolutionFailure(final String moduleName, final RuntimeException e) {
        String message = e.getMessage();
        error(message == null || message.isBlank() ? "module resolution failed for " + moduleName : message, declaration);
        // Keep java.base readable so later checks do not emit cascading
        // "is not accessible" errors for JDK types.
        reads = Set.of(JAVA_BASE);
        finder = ModuleFinder.ofSystem();
        extraExports = Map.of();
        accessiblePackages = null;
        resolutionFailed = true;
    }

    /**
     * Expands compile-time readability from {@code seeds} across every
     * {@code requires transitive} edge (including {@code requires static transitive},
     * which {@link Configuration#resolve} omits) and across automatic modules
     * (which grant implied readability to all other observable automatic modules per JEP 261).
     */
    private void expandCompileTimeReads(final Set<String> seeds, final Set<String> readable) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(seeds);
        boolean automaticAdded = false;
        while (!queue.isEmpty()) {
            String name = queue.removeFirst();
            if (!visited.add(name)) {
                continue;
            }
            readable.add(name);
            Optional<ModuleReference> reference = findModule(finder, name);
            if (reference.isEmpty()) {
                continue;
            }
            ModuleDescriptor descriptor = reference.get().descriptor();
            if (descriptor.isAutomatic()) {
                if (!automaticAdded) {
                    automaticAdded = true;
                    addAllObservableAutomaticModules(readable, visited);
                }
                continue;
            }
            for (ModuleDescriptor.Requires required : descriptor.requires()) {
                if (required.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE)) {
                    queue.addLast(required.name());
                }
            }
        }
    }

    private void addAllObservableAutomaticModules(final Set<String> readable, final Set<String> visited) {
        try {
            for (ModuleReference ref : finder.findAll()) {
                ModuleDescriptor descriptor = ref.descriptor();
                if (descriptor.isAutomatic()) {
                    readable.add(descriptor.name());
                    visited.add(descriptor.name());
                }
            }
        } catch (FindException ignored) {
            // Resolution has already validated observable modules.
        }
    }

    private static Optional<ModuleReference> findModule(final ModuleFinder moduleFinder, final String name) {
        try {
            return moduleFinder.find(name);
        } catch (FindException ignored) {
            return Optional.empty();
        }
    }

    private void checkExtraExportsSources(final ModuleFinder resolutionFinder) {
        for (String sourceModule : extraExports.keySet()) {
            if (findModule(resolutionFinder, sourceModule).isEmpty()) {
                warn(WarningMessage.LIKELY_ERRORS, "module name in --add-exports option not found: " + sourceModule, declaration);
            }
        }
    }

    /**
     * Emits JEP 261 compile-time warnings matching {@code javac}:
     * {@code requires transitive} on an automatic module (default warning),
     * non-transitive {@code requires} on an automatic module (level 2),
     * and qualified {@code exports}/{@code opens} targeting an unobservable module.
     */
    private void checkModuleWarnings(final ModuleFinder resolutionFinder) {
        for (Requires requires : declaration.getRequires()) {
            Optional<ModuleReference> ref = findModule(resolutionFinder, requires.getModuleName());
            if (ref.isPresent() && ref.get().descriptor().isAutomatic()) {
                if (requires.isTransitive()) {
                    warn(WarningMessage.LIKELY_ERRORS,
                            "requires transitive directive for an automatic module: " + requires.getModuleName(), requires);
                } else {
                    warn(WarningMessage.POSSIBLE_ERRORS,
                            "requires directive for an automatic module: " + requires.getModuleName(), requires);
                }
            }
        }
        checkQualifiedTargetsExist(declaration.getExports(), resolutionFinder);
        checkQualifiedTargetsExist(declaration.getOpens(), resolutionFinder);
    }

    private void checkQualifiedTargetsExist(final List<? extends PackageDirective> directives, final ModuleFinder resolutionFinder) {
        String self = declaration.getName();
        for (PackageDirective directive : directives) {
            for (String target : directive.getTargets()) {
                if (!self.equals(target) && findModule(resolutionFinder, target).isEmpty()) {
                    warn(WarningMessage.LIKELY_ERRORS, MODULE_NOT_FOUND + target, directive);
                }
            }
        }
    }

    /**
     * Every {@code requires} target, including {@code requires static}, must be observable.
     * Resolution does not fail when a static dependence is missing.
     */
    private void checkRequiresFound(final ModuleFinder resolutionFinder) {
        for (Requires requires : declaration.getRequires()) {
            String dependency = requires.getModuleName();
            try {
                if (resolutionFinder.find(dependency).isEmpty()) {
                    error(MODULE_NOT_FOUND + dependency, requires);
                }
            } catch (FindException e) {
                String message = e.getMessage();
                error(message == null || message.isBlank() ? MODULE_NOT_FOUND + dependency : message, requires);
            }
        }
    }

    /**
     * {@code Configuration.resolve} ignores {@code requires static}, so cycles
     * through a static dependence are checked directly (JLS §7.7.1).
     */
    private void checkStaticRequiresCycles(final ModuleFinder resolutionFinder) {
        String self = declaration.getName();
        for (Requires requires : declaration.getRequires()) {
            if (!requires.isStaticPhase()) {
                continue;
            }
            if (reachesModule(resolutionFinder, requires.getModuleName(), self)) {
                error("cyclic dependence involving " + requires.getModuleName(), requires);
                resolutionFailed = true;
            }
        }
    }

    private static boolean reachesModule(final ModuleFinder resolutionFinder, final String start, final String target) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            Optional<ModuleReference> reference;
            try {
                reference = resolutionFinder.find(current);
            } catch (FindException ignored) {
                continue;
            }
            if (reference.isEmpty()) {
                continue;
            }
            for (ModuleDescriptor.Requires edge : reference.get().descriptor().requires()) {
                String next = edge.name();
                if (target.equals(next)) {
                    return true;
                }
                if (visited.add(next)) {
                    queue.addLast(next);
                }
            }
        }
        return false;
    }

    /**
     * Checks JLS §7.4.3 split-package constraints:
     * <ul>
     *   <li>a package of this module must not also be exported to this module by a module it reads;</li>
     *   <li>two distinct modules read by this module (including via {@code requires static}) must not
     *       both export the same package to this module.</li>
     * </ul>
     * Concealed packages and exports qualified to some other module do not collide at compile time.
     * Automatic modules export every package, so their complete package set is checked.
     */
    private void checkSplitPackages() {
        if (reads.isEmpty()) {
            return;
        }
        String moduleName = declaration.getName();
        Map<String, String> packageOwners = new HashMap<>();
        for (String dependency : reads) {
            Optional<ModuleReference> reference = findModule(finder, dependency);
            if (reference.isEmpty()) {
                continue;
            }
            ModuleDescriptor descriptor = reference.get().descriptor();
            Set<String> visible = visiblePackages(descriptor, moduleName);
            for (String pkg : allModulePackages) {
                if (visible.contains(pkg)) {
                    error("package " + pkg + " exists in another module: " + dependency, classInPackage(pkg));
                }
            }
            for (String pkg : visible) {
                String first = packageOwners.putIfAbsent(pkg, dependency);
                if (first != null && !first.equals(dependency)) {
                    error("module " + moduleName + " reads package " + pkg + " from both " + first + " and " + dependency, declaration);
                }
            }
        }
    }

    private static Set<String> visiblePackages(final ModuleDescriptor descriptor, final String moduleName) {
        if (descriptor.isAutomatic()) {
            return descriptor.packages();
        }
        Set<String> visible = new HashSet<>();
        for (ModuleDescriptor.Exports exports : descriptor.exports()) {
            if (exports.targets().isEmpty() || exports.targets().contains(moduleName)) {
                visible.add(exports.source());
            }
        }
        return visible;
    }

    /**
     * JEP 511 / JLS SE 25 §7.7.1 / JVMS §4.7.25: {@code requires transitive java.base}
     * is legal when the class file version is 25 or higher. {@code requires static java.base}
     * remains illegal in every version.
     */
    private boolean allowsTransitiveJavaBase() {
        CompilerConfiguration config = source.getConfiguration();
        return config != null && (config.getBytecodeVersion() & ~Opcodes.V_PREVIEW) >= Opcodes.V25;
    }

    private ASTNode classInPackage(final String pkg) {
        if (compileUnit != null) {
            for (ClassNode cn : compileUnit.getClasses()) {
                if (pkg.equals(cn.getPackageName())) {
                    return cn;
                }
            }
        }
        return declaration;
    }

    private static void addPackage(final ModuleDescriptor.Builder builder, final PackageDirective directive, final boolean export) {
        if (directive.isQualified()) {
            Set<String> targets = new LinkedHashSet<>(directive.getTargets());
            if (export) {
                builder.exports(directive.getPackageName(), targets);
            } else {
                builder.opens(directive.getPackageName(), targets);
            }
        } else if (export) {
            builder.exports(directive.getPackageName());
        } else {
            builder.opens(directive.getPackageName());
        }
    }

    private void checkImports() {
        ModuleNode module = declaration.getSourceModule();
        if (module == null) {
            return;
        }
        for (ImportNode importNode : module.getImports()) {
            checkImportedType(importNode.getType(), importNode);
        }
        for (ImportNode importNode : module.getStaticImports().values()) {
            checkImportedType(importNode.getType(), importNode);
        }
        for (ImportNode importNode : module.getStaticStarImports().values()) {
            checkImportedType(importNode.getType(), importNode);
        }
        checkStarImports(module);
        checkModuleImports(module);
    }

    private void checkModuleImports(final ModuleNode module) {
        String self = declaration.getName();
        for (ImportNode importNode : module.getModuleImports()) {
            String imported = importNode.getPackageName();
            if (imported == null || imported.isEmpty() || self.equals(imported)) {
                continue;
            }
            if (findModule(finder, imported).isEmpty()) {
                error("Unknown module: " + imported, importNode);
            } else if (!reads.contains(imported)) {
                error("module " + imported + " is not readable in module " + self, importNode);
            }
        }
    }

    private void checkStarImports(final ModuleNode module) {
        for (ImportNode importNode : module.getStarImports()) {
            if (importNode.getLineNumber() <= 0) {
                continue;
            }
            if (importNode.getType() != null) {
                checkImportedType(importNode.getType(), importNode);
                continue;
            }
            String pkg = importNode.getPackageName();
            if (pkg != null && pkg.endsWith(".")) {
                pkg = pkg.substring(0, pkg.length() - 1);
            }
            if (pkg != null && !pkg.isEmpty() && !allModulePackages.contains(pkg) && !isPackageAccessible(pkg)) {
                error("package " + pkg + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), importNode);
            }
        }
    }

    private void checkImportedType(final ClassNode type, final ASTNode where) {
        if (type == null) {
            return;
        }
        ClassNode resolved = type.redirect();
        if (!resolved.isResolved() && !resolved.isPrimaryClassNode() && resolved.getCompileUnit() == null) {
            return;
        }
        if (!isAccessible(resolved)) {
            error("class " + resolved.getName() + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), where);
        }
    }

    private void checkAnnotations() {
        Set<String> reportedRefs = new HashSet<>();
        for (AnnotationNode collector : declaration.getExpandedCollectors()) {
            checkAnnotation(collector, reportedRefs);
        }
        for (AnnotationNode annotation : declaration.getAnnotations()) {
            checkAnnotation(annotation, reportedRefs);
        }
        for (Map.Entry<ClassNode, ASTNode> ref : declaration.getAnnotationTypeRefs()) {
            checkAnnotationReferencedType(ref.getKey(), ref.getValue(), reportedRefs);
        }
    }

    private void checkAnnotation(final AnnotationNode annotation, final Set<String> reportedRefs) {
        checkAnnotationType(annotation);
        if (annotation.getMembers().isEmpty()) {
            return;
        }
        CodeVisitorSupport valueVisitor = new CodeVisitorSupport() {
            @Override
            public void visitClassExpression(final ClassExpression expression) {
                checkAnnotationReferencedType(expression.getType(), expression, reportedRefs);
            }

            @Override
            public void visitConstantExpression(final ConstantExpression expression) {
                if (expression instanceof AnnotationConstantExpression && expression.getValue() instanceof AnnotationNode nested) {
                    checkAnnotationType(nested);
                }
            }
        };
        for (Expression value : annotation.getMembers().values()) {
            value.visit(valueVisitor);
        }
    }

    private void checkAnnotationType(final AnnotationNode annotation) {
        ClassNode resolved = annotation.getClassNode().redirect();
        if ((resolved.isResolved() || resolved.isPrimaryClassNode() || resolved.getCompileUnit() != null)
                && !"groovy.lang.Groovydoc".equals(resolved.getName())
                && !isAccessible(resolved)) {
            error("annotation @" + resolved.getName() + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), annotation);
        }
    }

    private void checkAnnotationReferencedType(final ClassNode type, final ASTNode where, final Set<String> reportedRefs) {
        if (type == null) {
            return;
        }
        ClassNode resolved = type.redirect();
        while (resolved.isArray()) {
            resolved = resolved.getComponentType();
        }
        if (ClassHelper.isPrimitiveType(resolved)) {
            return;
        }
        if (!resolved.isResolved() && !resolved.isPrimaryClassNode() && resolved.getCompileUnit() == null) {
            return;
        }
        if (!isAccessible(resolved)) {
            String key = resolved.getName() + "@" + where.getLineNumber() + ":" + where.getColumnNumber();
            if (reportedRefs.add(key)) {
                error("class " + resolved.getName() + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), where);
            }
        }
    }

    private void checkUses() {
        Set<String> seen = new HashSet<>();
        for (Uses uses : declaration.getUses()) {
            ClassNode type = uses.getResolvedType();
            if (type == null) {
                continue;
            }
            String binary = type.getName();
            if (!seen.add(binary)) {
                error("duplicate uses of service " + binary, uses);
            }
            if (type.isEnum()) {
                error("uses directive specifies an enum class " + binary, uses);
            } else if (!isAccessible(type)) {
                error("service " + binary + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), uses);
            }
        }
    }

    private void checkProvides() {
        Set<String> seen = new HashSet<>();
        for (Provides provides : declaration.getProvides()) {
            ClassNode service = provides.getResolvedService();
            if (service == null) {
                continue;
            }
            String serviceName = service.getName();
            if (!seen.add(serviceName)) {
                error("duplicate provides of service " + serviceName, provides);
            }
            // An enum service is legal when a provider() method returns it. uses rejects enums.
            if (!isAccessible(service)) {
                error("service " + serviceName + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), provides);
            }
            Set<String> providersSeen = new HashSet<>();
            for (ClassNode provider : provides.getResolvedProviders()) {
                if (provider == null) {
                    continue;
                }
                String binary = provider.getName();
                if (!providersSeen.add(binary)) {
                    error("duplicate provider " + binary, provides);
                    continue;
                }
                checkProvider(provides, service, provider);
            }
        }
    }

    private void checkProvider(final Provides provides, final ClassNode service, final ClassNode type) {
        String name = type.getName();
        if (!ModuleInfoNode.isPublicIncludingEnclosing(type)) {
            error(PROVIDER_PREFIX + name + " must be public", provides);
        }
        if (!isTopLevelOrStatic(type)) {
            error(PROVIDER_PREFIX + name + " must be a top-level or static nested type", provides);
        }
        if (!inThisModule(type)) {
            error(PROVIDER_PREFIX + name + " must be declared in the current module", provides);
        }
        MethodNode providerMethod = findProviderMethod(type);
        if (providerMethod != null) {
            ClassNode returnType = providerMethod.getReturnType();
            if (ClassHelper.isPrimitiveType(returnType) || !implementsInterfaceOrSubclassOf(returnType, service)) {
                error("provider method of " + name + " must return a subtype of " + service.getName(), provides);
            } else if (!isAccessible(returnType)) {
                error("return type " + returnType.getName() + " of provider method" + NOT_ACCESSIBLE_IN_MODULE + declaration.getName(), provides);
            }
            return;
        }
        // Interfaces, enums, and abstract classes have no provider constructor ServiceLoader can call.
        if (type.isInterface() || type.isAbstract() || type.isEnum() || !hasPublicNoArgConstructor(type)) {
            error(PROVIDER_PREFIX + name + " must have a public no-arg constructor or a public static provider() method", provides);
            return;
        }
        if (!implementsInterfaceOrSubclassOf(type, service)) {
            error(PROVIDER_PREFIX + name + " must be a subtype of " + service.getName(), provides);
        }
    }

    private static MethodNode findProviderMethod(final ClassNode type) {
        for (MethodNode method : type.getDeclaredMethods("provider")) {
            if (method.isStatic() && Modifier.isPublic(method.getModifiers()) && method.getParameters().length == 0) {
                return method;
            }
        }
        return null;
    }

    /**
     * A concrete class that declares no constructor receives a public default constructor (JLS 8.8.9).
     * Called only after interfaces, enums, and abstract classes have been rejected.
     */
    private static boolean hasPublicNoArgConstructor(final ClassNode type) {
        List<ConstructorNode> constructors = type.getDeclaredConstructors();
        if (constructors.isEmpty()) {
            return true;
        }
        for (ConstructorNode constructor : constructors) {
            if (constructor.getParameters().length == 0 && Modifier.isPublic(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTopLevelOrStatic(final ClassNode type) {
        if (type instanceof InnerClassNode inner) {
            return isStaticInnerClass(inner);
        }
        if (type instanceof DecompiledClassNode decompiled && decompiled.getMemberDeclaringClassName() != null) {
            return isStaticDecompiledMember(decompiled);
        }
        Class<?> loaded = ModuleInfoNode.resolvedClass(type);
        if (loaded != null) {
            return isTopLevelOrStaticLoaded(type, loaded);
        }
        return isTopLevelOrStaticAst(type);
    }

    private static boolean isTopLevelOrStaticAst(final ClassNode type) {
        return type.getName().indexOf('$') < 0 || isImplicitlyOrExplicitlyStatic(type);
    }

    private static boolean isStaticInnerClass(final InnerClassNode inner) {
        if (inner.isAnonymous() || !isImplicitlyOrExplicitlyStatic(inner)) {
            return false;
        }
        for (ClassNode outer = inner.getOuterClass(); outer instanceof InnerClassNode; outer = outer.getOuterClass()) {
            if (!isImplicitlyOrExplicitlyStatic(outer)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStaticDecompiledMember(final DecompiledClassNode member) {
        DecompiledClassNode current = member;
        while (current != null && current.getMemberDeclaringClassName() != null) {
            if (!isImplicitlyOrExplicitlyStatic(current)) {
                return false;
            }
            ClassNode outer = current.resolveClassNullable(current.getMemberDeclaringClassName());
            current = outer instanceof DecompiledClassNode decompiled ? decompiled : null;
        }
        return true;
    }

    /**
     * Member interfaces, enums, and records are implicitly {@code static}
     * (JLS §8.5.1, §8.9, §8.10, §9.1.1).
     */
    private static boolean isImplicitlyOrExplicitlyStatic(final ClassNode type) {
        return Modifier.isStatic(type.getModifiers()) || type.isInterface() || type.isEnum() || type.isRecord();
    }

    private static boolean isTopLevelOrStaticLoaded(final ClassNode type, final Class<?> loaded) {
        try {
            if (loaded.isAnonymousClass() || loaded.isLocalClass()) {
                return false;
            }
            for (Class<?> current = loaded; current.getDeclaringClass() != null; current = current.getDeclaringClass()) {
                if (!Modifier.isStatic(current.getModifiers())) {
                    return false;
                }
            }
            return true;
        } catch (LinkageError | RuntimeException ignored) {
            // Fall back to AST modifiers if InnerClasses metadata is inconsistent (e.g., stale inner .class).
            return isTopLevelOrStaticAst(type);
        }
    }

    private boolean inThisModule(final ClassNode type) {
        if (compileUnit != null && compileUnit.getClass(type.getName()) != null) {
            return true;
        }
        return compiledClasses != null && compiledClasses.contains(type.getName());
    }

    /**
     * A public type declared in this module is accessible. A public type from
     * another module is accessible when that module is read and exports the
     * type's package to the current module (JLS §6.6 / §7.7).
     */
    private boolean isAccessible(final ClassNode type) {
        if (!ModuleInfoNode.isPublicIncludingEnclosing(type)) {
            return false;
        }
        if (inThisModule(type)) {
            return true;
        }
        String pkg = type.getPackageName();
        return pkg != null && !pkg.isEmpty() && isPackageAccessible(pkg);
    }

    private boolean isPackageAccessible(final String pkg) {
        if (declaration.isReadsReady() && declaration.getAccessibilityFinder() == finder) {
            return declaration.isReadsUnnamed()
                    ? declaration.canAccessPackage(pkg)
                    : declaration.isPackageExportedToSelf(pkg);
        }
        Set<String> packages = accessiblePackages;
        if (packages == null) {
            packages = ModuleInfoNode.computeAccessiblePackages(reads, finder, declaration.getName(), extraExports);
            accessiblePackages = packages;
        }
        return packages.contains(pkg);
    }

    private void error(final String message, final ASTNode node) {
        if (!reportGraphErrors) {
            return;
        }
        source.addErrorAndContinue(new SyntaxException(message, node));
    }

    private void warn(final int importance, final String message, final ASTNode node) {
        if (!reportGraphErrors) {
            return;
        }
        source.addWarning(importance, message, node);
    }

    /**
     * Descriptor-only reference for the module being compiled, so resolution
     * can use it as a root before {@code module-info.class} exists.
     */
    static final class CompilingModuleReference extends ModuleReference {
        CompilingModuleReference(final ModuleDescriptor descriptor) {
            super(descriptor, null);
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
                    // Nothing to release for a descriptor-only reference.
                }
            };
        }
    }
}
