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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompiledClassIndex;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ModuleImportHelper;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private final SourceUnit source;
    private final CompilationUnit compilationUnit;
    private final CompileUnit compileUnit;
    private final CompiledClassIndex compiledClasses;
    private final ModuleInfoNode declaration;
    private final ModuleFinder classpathFinder;
    private final ModuleFinder accessibilityFinder;
    private final boolean reportGraphErrors;
    private final Set<String> modulePackages = new LinkedHashSet<>();
    private ModuleFinder finder = ModuleFinder.ofSystem();
    private Set<String> reads = Set.of(JAVA_BASE);
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
        if (!declaration.hasResolvedTypes()) {
            ModuleInfoResolveVisitor.resolve(source, declaration, compilationUnit);
        }
        collectPackages();
        boolean requiresValid = checkRequires();
        checkPackageDirectives(declaration.getExports(), false);
        checkPackageDirectives(declaration.getOpens(), true);
        if (requiresValid) {
            resolveModuleGraph();
        }
        checkAnnotations();
        checkUses();
        checkProvides();
    }

    private void collectPackages() {
        boolean unnamedReported = false;
        if (compileUnit != null) {
            for (ClassNode cn : compileUnit.getClasses()) {
                String pkg = cn.getPackageName();
                if (pkg == null || pkg.isEmpty()) {
                    error("unnamed package is not allowed in named modules", cn);
                    unnamedReported = true;
                } else {
                    modulePackages.add(pkg);
                }
            }
        }
        if (compiledClasses != null) {
            modulePackages.addAll(compiledClasses.getPackages());
            if (compiledClasses.hasUnnamedClass() && !unnamedReported) {
                error("unnamed package is not allowed in named modules", declaration);
            }
        }
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
            String packageName = directive.getPackageName();
            if (!seen.add(packageName)) {
                error("duplicate " + (opens ? "opens" : "exports") + " of package " + packageName, directive);
            }
            if (!modulePackages.contains(packageName)) {
                // javac: exports of an empty or missing package is an error; opens is a warning.
                String message = "package is empty or does not exist: " + packageName;
                if (opens) {
                    source.addWarning(WarningMessage.LIKELY_ERRORS, message, directive);
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
    }

    private void resolveModuleGraph() {
        String moduleName = declaration.getName();
        ModuleDescriptor.Builder builder = declaration.isOpen()
                ? ModuleDescriptor.newOpenModule(moduleName)
                : ModuleDescriptor.newModule(moduleName);
        try {
            if (!JAVA_BASE.equals(moduleName)) {
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
            Set<String> builtExports = new HashSet<>();
            for (Exports exports : declaration.getExports()) {
                if (!modulePackages.contains(exports.getPackageName()) || !builtExports.add(exports.getPackageName())) {
                    continue;
                }
                addPackage(builder, exports, true);
            }
            if (!declaration.isOpen()) {
                Set<String> builtOpens = new HashSet<>();
                for (Opens opens : declaration.getOpens()) {
                    if (!builtOpens.add(opens.getPackageName())) {
                        continue;
                    }
                    addPackage(builder, opens, false);
                }
            }
            ModuleDescriptor descriptor = builder.build();
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
            // Compile-time resolution is independent of the host boot layer (JEP 511):
            // unbooted platform modules such as java.se must still be observable.
            // Configuration.resolve (not resolveAndBind) is the compile-time graph;
            // binding would scan every classpath JAR for service providers.
            // Platform modules come first so a classpath JAR cannot shadow them.
            ModuleFinder classpath = classpathFinder != null
                    ? classpathFinder
                    : ModuleImportHelper.classpathModuleFinder(source);
            ModuleFinder resolutionFinder = dedupe(ModuleFinder.compose(compiling, ModuleFinder.ofSystem(), classpath));
            finder = accessibilityFinder != null ? accessibilityFinder : resolutionFinder;
            Configuration configuration = Configuration.resolve(
                    resolutionFinder, List.of(Configuration.empty()), ModuleFinder.of(), Set.of(moduleName));
            Set<String> readable = new LinkedHashSet<>();
            configuration.findModule(moduleName).ifPresent(resolved ->
                    resolved.reads().forEach(other -> readable.add(other.name())));
            // requires static is not added to the runtime graph, but it is readable
            // at compile time when the module is observable (javac / JLS §7.7.1).
            for (Requires requires : declaration.getRequires()) {
                addCompileTimeReads(requires.getModuleName(), readable);
            }
            if (!JAVA_BASE.equals(moduleName)) {
                readable.add(JAVA_BASE);
            }
            reads = readable;
            // requires static is optional at resolution time, but javac still requires the
            // module to be observable at compile time. Check every requires edge here.
            checkRequiresFound(resolutionFinder);
            checkSplitPackages();
        } catch (FindException | ResolutionException | IllegalArgumentException e) {
            String message = e.getMessage();
            error(message == null || message.isBlank() ? "module resolution failed for " + moduleName : message, declaration);
            // Keep java.base readable so later checks do not emit cascading
            // "is not accessible" errors for JDK types.
            reads = Set.of(JAVA_BASE);
            finder = ModuleFinder.ofSystem();
            resolutionFailed = true;
        }
        declaration.adoptResolution(reads, finder, resolutionFailed);
    }

    /**
     * Adds {@code name} and its {@code requires transitive} edges to the compile-time
     * readability set, walking only modules the finder can still observe.
     */
    private void addCompileTimeReads(final String name, final Set<String> readable) {
        if (!readable.add(name)) {
            return;
        }
        Optional<ModuleReference> reference = finder.find(name);
        if (reference.isEmpty()) {
            return;
        }
        for (ModuleDescriptor.Requires required : reference.get().descriptor().requires()) {
            if (required.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE)) {
                addCompileTimeReads(required.name(), readable);
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
                    error("module not found: " + dependency, requires);
                }
            } catch (FindException e) {
                String message = e.getMessage();
                error(message == null || message.isBlank() ? "module not found: " + dependency : message, requires);
            }
        }
    }

    /**
     * A package of this module must not also be exported to this module by a
     * module it reads (JLS §7.4.3). Concealed packages and exports qualified to
     * some other module do not collide at compile time. Automatic modules export
     * every package, so their complete package set is checked.
     */
    private void checkSplitPackages() {
        if (reads.isEmpty() || modulePackages.isEmpty()) {
            return;
        }
        String moduleName = declaration.getName();
        for (String dependency : reads) {
            Optional<ModuleReference> reference = finder.find(dependency);
            if (reference.isEmpty()) {
                continue;
            }
            ModuleDescriptor descriptor = reference.get().descriptor();
            Set<String> visible = visiblePackages(descriptor, moduleName);
            for (String pkg : modulePackages) {
                if (visible.contains(pkg)) {
                    error("package " + pkg + " exists in another module: " + dependency, classInPackage(pkg));
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
        return config != null && config.getBytecodeVersion() >= Opcodes.V25;
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

    private void checkAnnotations() {
        for (AnnotationNode annotation : declaration.getAnnotations()) {
            ClassNode type = annotation.getClassNode();
            ClassNode resolved = type.redirect();
            if (!resolved.isResolved() && !resolved.isPrimaryClassNode() && resolved.getCompileUnit() == null) {
                continue;
            }
            if ("groovy.lang.Groovydoc".equals(resolved.getName())) {
                // Compiler-attached documentation; groovy.lang is typically on the
                // classpath (unnamed module), not a required named module.
                continue;
            }
            if (!isAccessible(resolved)) {
                error("annotation @" + resolved.getName() + " is not accessible in module " + declaration.getName(), annotation);
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
                error("service " + binary + " is not accessible in module " + declaration.getName(), uses);
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
                error("service " + serviceName + " is not accessible in module " + declaration.getName(), provides);
            }
            Set<String> providersSeen = new HashSet<>();
            List<ClassNode> resolvedProviders = provides.getResolvedProviders();
            for (int i = 0; i < resolvedProviders.size(); i++) {
                ClassNode provider = resolvedProviders.get(i);
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
        if (!isPublicIncludingEnclosing(type)) {
            error("provider " + name + " must be public", provides);
        }
        if (!isTopLevelOrStatic(type)) {
            error("provider " + name + " must be a top-level or static nested type", provides);
        }
        if (!inThisModule(type)) {
            error("provider " + name + " must be declared in the current module", provides);
        }
        MethodNode providerMethod = findProviderMethod(type);
        if (providerMethod != null) {
            ClassNode returnType = providerMethod.getReturnType();
            if (ClassHelper.isPrimitiveType(returnType) || !implementsInterfaceOrSubclassOf(returnType, service)) {
                error("provider method of " + name + " must return a subtype of " + service.getName(), provides);
            } else if (!isAccessible(returnType)) {
                error("return type " + returnType.getName() + " of provider method is not accessible in module " + declaration.getName(), provides);
            }
            return;
        }
        // Interfaces, enums, and abstract classes have no provider constructor ServiceLoader can call.
        if (type.isInterface() || type.isAbstract() || type.isEnum() || !hasPublicNoArgConstructor(type)) {
            error("provider " + name + " must have a public no-arg constructor or a public static provider() method", provides);
            return;
        }
        if (!implementsInterfaceOrSubclassOf(type, service)) {
            error("provider " + name + " must be a subtype of " + service.getName(), provides);
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
            if (inner.isAnonymous() || !Modifier.isStatic(inner.getModifiers())) {
                return false;
            }
            for (ClassNode outer = inner.getOuterClass(); outer instanceof InnerClassNode; outer = outer.getOuterClass()) {
                if (!Modifier.isStatic(outer.getModifiers())) {
                    return false;
                }
            }
            return true;
        }
        Class<?> loaded = resolvedClass(type);
        if (loaded != null) {
            if (loaded.isAnonymousClass() || loaded.isLocalClass()) {
                return false;
            }
            Class<?> enclosing = loaded.getDeclaringClass();
            if (enclosing == null) {
                return true;
            }
            if (!Modifier.isStatic(loaded.getModifiers())) {
                return false;
            }
            for (Class<?> outer = enclosing; outer != null && outer.getDeclaringClass() != null; outer = outer.getDeclaringClass()) {
                if (!Modifier.isStatic(outer.getModifiers())) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    private static boolean isPublicIncludingEnclosing(final ClassNode type) {
        if (!Modifier.isPublic(type.getModifiers())) {
            return false;
        }
        if (type instanceof InnerClassNode inner) {
            for (ClassNode outer = inner.getOuterClass(); outer != null; outer = outer.getOuterClass()) {
                if (!Modifier.isPublic(outer.getModifiers())) {
                    return false;
                }
            }
            return true;
        }
        Class<?> loaded = resolvedClass(type);
        if (loaded != null) {
            if (!Modifier.isPublic(loaded.getModifiers())) {
                return false;
            }
            for (Class<?> outer = loaded.getDeclaringClass(); outer != null; outer = outer.getDeclaringClass()) {
                if (!Modifier.isPublic(outer.getModifiers())) {
                    return false;
                }
            }
            return true;
        }
        for (ClassNode outer = type.getOuterClass(); outer != null; outer = outer.getOuterClass()) {
            if (!Modifier.isPublic(outer.getModifiers())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> resolvedClass(final ClassNode type) {
        if (!type.isResolved()) {
            return null;
        }
        try {
            return type.getTypeClass();
        } catch (RuntimeException ignored) {
            return null;
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
        if (!isPublicIncludingEnclosing(type)) {
            return false;
        }
        if (inThisModule(type)) {
            return true;
        }
        String pkg = type.getPackageName();
        if (pkg == null) {
            return false;
        }
        String moduleName = declaration.getName();
        for (String dependency : reads) {
            Optional<ModuleReference> reference = finder.find(dependency);
            if (reference.isEmpty()) {
                continue;
            }
            ModuleDescriptor descriptor = reference.get().descriptor();
            if (descriptor.isAutomatic()) {
                if (descriptor.packages().contains(pkg)) {
                    return true;
                }
                continue;
            }
            for (ModuleDescriptor.Exports exports : descriptor.exports()) {
                if (exports.source().equals(pkg)
                        && (exports.targets().isEmpty() || exports.targets().contains(moduleName))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void error(final String message, final ASTNode node) {
        if (!reportGraphErrors) {
            return;
        }
        source.addErrorAndContinue(new SyntaxException(message, node));
    }

    /**
     * {@code findAll} on a composed finder can surface the same module twice
     * (platform module and a copy reachable from the compiler class loader).
     * Resolution rejects that, including for automatic modules that read everything.
     * The first occurrence wins, matching {@code ModuleFinder.find}.
     */
    private static ModuleFinder dedupe(final ModuleFinder finder) {
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
                }
            };
        }
    }
}
