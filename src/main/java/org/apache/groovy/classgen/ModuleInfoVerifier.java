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
import org.apache.groovy.ast.ModuleInfoNode.Directive;
import org.apache.groovy.ast.ModuleInfoNode.Exports;
import org.apache.groovy.ast.ModuleInfoNode.Opens;
import org.apache.groovy.ast.ModuleInfoNode.PackageDirective;
import org.apache.groovy.ast.ModuleInfoNode.Provides;
import org.apache.groovy.ast.ModuleInfoNode.Requires;
import org.apache.groovy.ast.ModuleInfoNode.Uses;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ModuleImportHelper;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
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
 * Name resolution for {@code uses} and {@code provides} uses the modular
 * compilation unit's imports plus {@code java.lang}. Annotation types are
 * resolved earlier as they would be on an ordinary compilation unit; this
 * verifier then checks they are accessible to the module.
 */
public final class ModuleInfoVerifier {

    private static final String JAVA_BASE = "java.base";

    private final SourceUnit source;
    private final CompileUnit compileUnit;
    private final ModuleNode moduleNode;
    private final ModuleInfoNode declaration;
    private final Set<String> modulePackages = new LinkedHashSet<>();
    private ModuleFinder finder = ModuleFinder.of();
    private Set<String> reads = Set.of();

    /**
     * Replaces the classpath half of module resolution. Tests use this to raise
     * {@link FindException} with a chosen message. Always cleared by the test.
     */
    static volatile ModuleFinder classpathFinderOverride;

    /**
     * Replaces the finder consulted when checking whether a read module still
     * exports a package. Tests use this to drop a module that resolution already accepted.
     */
    static volatile ModuleFinder accessibilityFinderOverride;

    private ModuleInfoVerifier(final SourceUnit source, final ClassNode classNode, final ModuleInfoNode declaration) {
        this.source = source;
        this.declaration = declaration;
        this.moduleNode = classNode.getModule();
        CompileUnit unit = moduleNode != null ? moduleNode.getUnit() : null;
        if (unit == null && source.getAST() != null) {
            unit = source.getAST().getUnit();
        }
        this.compileUnit = unit;
    }

    /**
     * Version string recorded on a {@code requires} edge, or {@code null} when the
     * module has none or the finder cannot be read.
     */
    public static String readModuleVersion(final ModuleFinder finder, final String moduleName) {
        try {
            return finder.find(moduleName).flatMap(ref -> ref.descriptor().rawVersion()).orElse(null);
        } catch (FindException ignored) {
            return null;
        }
    }

    /**
     * Checks {@code classNode} when it carries a module declaration.
     * No-op for every other class.
     */
    public static void check(final SourceUnit source, final ClassNode classNode) {
        ModuleInfoNode declaration = ModuleInfoNode.of(classNode);
        if (declaration == null || source == null) {
            return;
        }
        new ModuleInfoVerifier(source, classNode, declaration).verify();
    }

    private void verify() {
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
        if (compileUnit == null) {
            return;
        }
        for (ClassNode cn : compileUnit.getClasses()) {
            if (ModuleInfoNode.of(cn) != null) {
                continue;
            }
            String pkg = cn.getPackageName();
            if (pkg != null) {
                modulePackages.add(pkg);
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
            if (JAVA_BASE.equals(dependency) && (requires.isTransitive() || requires.isStaticPhase())) {
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
            // Platform modules come from the boot configuration. Putting the system
            // finder in front of that parent makes every automatic module read them twice.
            ModuleFinder classpath = classpathFinderOverride != null
                    ? classpathFinderOverride
                    : ModuleImportHelper.classpathModuleFinder(source);
            ModuleFinder before = dedupe(ModuleFinder.compose(compiling, classpath));
            // Descriptor lookup still needs the platform modules (exports of java.base, …).
            ModuleFinder resolutionFinder = ModuleFinder.compose(before, ModuleFinder.ofSystem());
            finder = accessibilityFinderOverride != null
                    ? accessibilityFinderOverride
                    : resolutionFinder;
            Configuration configuration = Configuration.resolveAndBind(
                    before, List.of(ModuleLayer.boot().configuration()), ModuleFinder.of(), Set.of(moduleName));
            Set<String> readable = new LinkedHashSet<>();
            configuration.findModule(moduleName).ifPresent(resolved ->
                    resolved.reads().forEach(other -> readable.add(other.name())));
            reads = readable;
            // requires static is optional at resolution time, but javac still requires the
            // module to be observable at compile time. Check every requires edge here.
            checkRequiresFound(resolutionFinder);
            checkSplitPackages();
        } catch (FindException | ResolutionException | IllegalArgumentException e) {
            String message = e.getMessage();
            error(message == null || message.isBlank() ? "module resolution failed for " + moduleName : message, declaration);
            reads = Set.of();
            finder = ModuleImportHelper.moduleFinder(source);
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
     * A package of this module must not also belong to a module it reads.
     */
    private void checkSplitPackages() {
        if (reads.isEmpty() || modulePackages.isEmpty()) {
            return;
        }
        for (String dependency : reads) {
            Optional<ModuleReference> reference = finder.find(dependency);
            if (reference.isEmpty()) {
                continue;
            }
            Set<String> packages = reference.get().descriptor().packages();
            for (String pkg : modulePackages) {
                if (packages.contains(pkg)) {
                    error("package " + pkg + " exists in another module: " + dependency, classInPackage(pkg));
                }
            }
        }
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
            if (!isAccessible(resolved)) {
                error("annotation @" + resolved.getName() + " is not accessible in module " + declaration.getName(), annotation);
            }
        }
    }

    private void checkUses() {
        Set<String> seen = new HashSet<>();
        for (Uses uses : declaration.getUses()) {
            ResolvedType resolved = resolveService(uses.getServiceName(), uses);
            if (resolved == null) {
                continue;
            }
            uses.setResolvedType(resolved.type);
            String binary = resolved.type.getName();
            if (!seen.add(binary)) {
                error("duplicate uses of service " + binary, uses);
            }
            if (resolved.type.isEnum()) {
                error("uses directive specifies an enum class " + binary, uses);
            } else if (!isAccessible(resolved.type)) {
                error("service " + binary + " is not accessible in module " + declaration.getName(), uses);
            }
        }
    }

    private void checkProvides() {
        Set<String> seen = new HashSet<>();
        for (Provides provides : declaration.getProvides()) {
            ResolvedType service = resolveService(provides.getServiceName(), provides);
            if (service == null) {
                continue;
            }
            provides.setResolvedService(service.type);
            String serviceName = service.type.getName();
            if (!seen.add(serviceName)) {
                error("duplicate provides of service " + serviceName, provides);
            }
            // An enum service is legal when a provider() method returns it. uses rejects enums.
            if (!isAccessible(service.type)) {
                error("service " + serviceName + " is not accessible in module " + declaration.getName(), provides);
            }
            Set<String> providersSeen = new HashSet<>();
            List<ClassNode> resolvedProviders = new ArrayList<>();
            for (String providerName : provides.getProviderNames()) {
                ResolvedType provider = resolveService(providerName, provides);
                if (provider == null) {
                    continue;
                }
                resolvedProviders.add(provider.type);
                String binary = provider.type.getName();
                if (!providersSeen.add(binary)) {
                    error("duplicate provider " + binary, provides);
                    continue;
                }
                checkProvider(provides, service.type, provider);
            }
            provides.setResolvedProviders(resolvedProviders);
        }
    }

    private void checkProvider(final Provides provides, final ClassNode service, final ResolvedType provider) {
        ClassNode type = provider.type;
        String name = type.getName();
        if (!Modifier.isPublic(type.getModifiers())) {
            error("provider " + name + " must be public", provides);
        }
        if (!isTopLevelOrStatic(type)) {
            error("provider " + name + " must be a top-level or static nested type", provides);
        }
        if (!provider.inThisModule) {
            error("provider " + name + " must be declared in the current module", provides);
        }
        MethodNode providerMethod = findProviderMethod(type);
        if (providerMethod != null) {
            ClassNode returnType = providerMethod.getReturnType();
            if (!implementsInterfaceOrSubclassOf(returnType, service)) {
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
        if (!(type instanceof InnerClassNode inner)) {
            return true;
        }
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

    private boolean inThisModule(final ClassNode type) {
        if (compileUnit == null) {
            return false;
        }
        return compileUnit.getClass(type.getName()) != null;
    }

    /**
     * A public type declared in this module is accessible. A public type from
     * another module is accessible when that module is read and exports the
     * type's package to the current module (JLS §6.6 / §7.7).
     */
    private boolean isAccessible(final ClassNode type) {
        if (!Modifier.isPublic(type.getModifiers())) {
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

    private ResolvedType resolveService(final String sourceName, final Directive where) {
        String qualified = qualify(sourceName, where);
        if (qualified == null) {
            return null;
        }
        ResolvedType resolved = resolveBinary(qualified);
        if (resolved == null) {
            error("unable to resolve class " + sourceName, where);
        }
        return resolved;
    }

    /**
     * Simple names follow the modular compilation unit's imports and {@code java.lang}.
     * Qualified names are used as written.
     */
    private String qualify(final String sourceName, final Directive where) {
        if (sourceName.indexOf('.') >= 0 || moduleNode == null) {
            return sourceName;
        }
        ImportNode single = moduleNode.getImport(sourceName);
        if (single != null) {
            return single.getType().getName();
        }
        List<String> candidates = new ArrayList<>();
        collectStarCandidates(moduleNode.getStarImports(), sourceName, candidates);
        collectStarCandidates(moduleNode.getModuleStarImports(), sourceName, candidates);
        String fromJavaLang = "java.lang." + sourceName;
        if (resolveBinary(fromJavaLang) != null && !candidates.contains(fromJavaLang)) {
            candidates.add(fromJavaLang);
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        if (candidates.size() > 1) {
            error("reference to " + sourceName + " is ambiguous", where);
            return null;
        }
        error("unable to resolve class " + sourceName, where);
        return null;
    }

    private void collectStarCandidates(final List<ImportNode> stars, final String simple, final List<String> candidates) {
        for (ImportNode star : stars) {
            String packageName = star.getPackageName();
            if (packageName == null || packageName.isEmpty()) {
                continue;
            }
            if (!packageName.endsWith(".")) {
                packageName = packageName + ".";
            }
            String qualified = packageName + simple;
            if (resolveBinary(qualified) != null && !candidates.contains(qualified)) {
                candidates.add(qualified);
            }
        }
    }

    private ResolvedType resolveBinary(final String sourceName) {
        String[] parts = sourceName.split("\\.");
        for (int classStart = parts.length - 1; classStart >= 0; classStart--) {
            String binary = binaryName(parts, classStart);
            ResolvedType found = lookup(binary);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String binaryName(final String[] parts, final int classStart) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < classStart; i++) {
            if (i > 0) {
                binary.append('.');
            }
            binary.append(parts[i]);
        }
        if (classStart > 0) {
            binary.append('.');
        }
        for (int i = classStart; i < parts.length; i++) {
            if (i > classStart) {
                binary.append('$');
            }
            binary.append(parts[i]);
        }
        return binary.toString();
    }

    private ResolvedType lookup(final String binary) {
        if (compileUnit != null) {
            ClassNode found = compileUnit.getClass(binary);
            if (found != null) {
                return new ResolvedType(found, true);
            }
        }
        ClassLoader loader = compileUnit != null ? compileUnit.getClassLoader()
                : (source.getClassLoader() != null ? source.getClassLoader() : ClassLoader.getSystemClassLoader());
        try {
            Class<?> loaded = Class.forName(binary, false, loader);
            return new ResolvedType(ClassHelper.make(loaded), false);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private void error(final String message, final ASTNode node) {
        source.addErrorAndContinue(new SyntaxException(message, node));
    }

    private static final class ResolvedType {
        private final ClassNode type;
        private final boolean inThisModule;

        private ResolvedType(final ClassNode type, final boolean inThisModule) {
            this.type = type;
            this.inThisModule = inThisModule;
        }
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
