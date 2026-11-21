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
package org.apache.groovy.ast;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ModuleNode;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * AST for a JPMS module declaration (JLS §7.7), produced from
 * {@code module-info.groovy} (or {@code module-info.java} fed to the Groovy parser).
 * The declaration is a member of the {@link CompileUnit}, not a class. The
 * compilation pipeline visits it through its own phase operation. It is never
 * entered in a module's class list, so a transform that walks classes does not
 * see it.
 */
public final class ModuleInfoNode extends AnnotatedNode {

    /**
     * Binary name of the class file that carries this declaration (JVMS §4.7.25).
     */
    public static final String CLASS_FILE_NAME = "module-info";

    private final String name;
    private final boolean open;
    private final List<Directive> directives;
    private ModuleNode sourceModule;
    private Set<String> reads = Set.of();
    private boolean readsReady;
    private boolean resolutionFailed;
    private boolean typesResolved;
    private transient ModuleFinder accessibilityFinder;
    // Packages of explicit modules the finder can observe. Automatic modules are
    // omitted: the compilation classpath is observed that way, and the language
    // runtime lives there. Built once per resolution.
    private transient Set<String> explicitPackages;

    public ModuleInfoNode(final String name, final boolean open, final List<Directive> directives) {
        this.name = name;
        this.open = open;
        this.directives = Collections.unmodifiableList(new ArrayList<>(directives));
    }

    /**
     * Whether {@code name} is the file name of a modular compilation unit.
     * A path is reduced to its last segment so {@code src/module-info.groovy} matches.
     */
    public static boolean isModuleInfoSource(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        String file = slash >= 0 ? name.substring(slash + 1) : name;
        return "module-info.groovy".equals(file) || "module-info.java".equals(file);
    }

    /**
     * The source module this declaration was parsed from, or {@code null} when
     * the declaration was built without a source.
     *
     * @return the owning source module, or {@code null}
     * @since 7.0.0
     */
    public ModuleNode getSourceModule() {
        return sourceModule;
    }

    /**
     * Records the source module that parsed this declaration.
     * The module is not asked to adopt the declaration; that link is
     * {@link ModuleNode#setModuleDeclaration}.
     *
     * @param sourceModule the owning source module, or {@code null}
     * @since 7.0.0
     */
    public void setSourceModule(final ModuleNode sourceModule) {
        this.sourceModule = sourceModule;
    }

    /**
     * Modules readable from this one after the module graph has been resolved.
     * Empty until {@link #isReadsReady()} is true.
     *
     * @return the readable module names
     * @since 7.0.0
     */
    public Set<String> getReads() {
        return reads;
    }

    /**
     * Whether {@link #getReads()} may be used to test accessibility.
     * False when resolution failed or has not run. Callers then skip the
     * accessibility test instead of reporting a cascade of errors.
     *
     * @return {@code true} when a resolved readability set is available
     * @since 7.0.0
     */
    public boolean isReadsReady() {
        return readsReady;
    }

    /**
     * Whether resolving the module graph failed.
     *
     * @return {@code true} when the graph could not be resolved
     * @since 7.0.0
     */
    public boolean isResolutionFailed() {
        return resolutionFailed;
    }

    /**
     * Whether {@code uses} and {@code provides} types have already been resolved.
     * A later pass must not resolve them again.
     *
     * @return {@code true} after directive types have been resolved
     * @since 7.0.0
     */
    public boolean hasResolvedTypes() {
        return typesResolved;
    }

    /**
     * Records that directive types have been resolved.
     * Unresolved directives stay {@code null}; the error was already reported.
     *
     * @since 7.0.0
     */
    public void markTypesResolved() {
        this.typesResolved = true;
    }

    /**
     * Stores the compile-time readability set. {@code failed} clears
     * {@link #isReadsReady()} so later type resolution does not cascade.
     *
     * @param reads modules readable from this one, or {@code null}
     * @param finder finder used to test exports, or {@code null} when resolution failed
     * @param failed whether resolution failed
     * @since 7.0.0
     */
    public void adoptResolution(final Set<String> reads, final ModuleFinder finder, final boolean failed) {
        this.reads = reads == null || reads.isEmpty() ? Set.of() : Set.copyOf(reads);
        this.accessibilityFinder = finder;
        this.explicitPackages = null;
        this.resolutionFailed = failed;
        this.readsReady = !failed;
    }

    /**
     * Whether a public type from another module is accessible from this one
     * (JLS §6.6 / §7.7). Types declared in this compilation are accessible
     * even when their package is not exported. A package owned by an explicit
     * module is accessible only when a module this one reads exports that
     * package to it. A package that no explicit module owns stays accessible:
     * the compilation classpath is observed as automatic modules, and that is
     * where the language runtime is found. Returns {@code true} when the
     * readability set is not ready, so a failed module graph does not cascade
     * into type errors.
     *
     * @param type the resolved type, or {@code null}
     * @return {@code true} when the type may be referenced
     * @since 7.0.0
     */
    public boolean canAccess(final ClassNode type) {
        if (type == null) {
            return true;
        }
        ClassNode resolved = type.redirect();
        while (resolved.isArray()) {
            resolved = resolved.getComponentType();
        }
        if (resolved.isGenericsPlaceHolder() || ClassHelper.isPrimitiveType(resolved)) {
            return true;
        }
        if (declaredInThisCompilation(resolved)) {
            return true;
        }
        if (!readsReady || accessibilityFinder == null) {
            return true;
        }
        if (!isPublicIncludingEnclosing(resolved)) {
            return false;
        }
        String pkg = resolved.getPackageName();
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        for (String dependency : reads) {
            Optional<ModuleReference> reference;
            try {
                reference = accessibilityFinder.find(dependency);
            } catch (RuntimeException ignored) {
                continue;
            }
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
                        && (exports.targets().isEmpty() || exports.targets().contains(name))) {
                    return true;
                }
            }
        }
        return !ownedByExplicitModule(pkg);
    }

    /**
     * Whether an explicit module observable to this compilation owns {@code pkg}.
     * Automatic modules are not a boundary for ordinary type references.
     */
    private boolean ownedByExplicitModule(final String pkg) {
        Set<String> packages = explicitPackages;
        if (packages == null) {
            packages = explicitPackages(accessibilityFinder);
            explicitPackages = packages;
        }
        return packages.contains(pkg);
    }

    private static Set<String> explicitPackages(final ModuleFinder finder) {
        Set<ModuleReference> observed;
        try {
            observed = finder.findAll();
        } catch (RuntimeException ignored) {
            return Set.of();
        }
        Set<String> packages = new LinkedHashSet<>();
        for (ModuleReference reference : observed) {
            ModuleDescriptor descriptor = reference.descriptor();
            if (!descriptor.isAutomatic()) {
                packages.addAll(descriptor.packages());
            }
        }
        return Set.copyOf(packages);
    }

    private boolean declaredInThisCompilation(final ClassNode type) {
        CompileUnit unit = sourceModule != null ? sourceModule.getUnit() : null;
        if (unit == null) {
            return false;
        }
        if (type.getCompileUnit() == unit || unit.getClass(type.getName()) != null) {
            return true;
        }
        ModuleNode owner = type.getModule();
        return owner != null && owner.getUnit() == unit;
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

    public String getName() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public List<Directive> getDirectives() {
        return directives;
    }

    public List<Requires> getRequires() {
        return ofKind(Requires.class);
    }

    public List<Exports> getExports() {
        return ofKind(Exports.class);
    }

    public List<Opens> getOpens() {
        return ofKind(Opens.class);
    }

    public List<Uses> getUses() {
        return ofKind(Uses.class);
    }

    public List<Provides> getProvides() {
        return ofKind(Provides.class);
    }

    private <T extends Directive> List<T> ofKind(final Class<T> kind) {
        List<T> answer = new ArrayList<>();
        for (Directive directive : directives) {
            if (kind.isInstance(directive)) {
                answer.add(kind.cast(directive));
            }
        }
        return answer;
    }

    /**
     * One module directive. Subclasses carry the directive's operands.
     */
    public abstract static class Directive extends ASTNode {
    }

    /**
     * {@code requires [transitive] [static] ModuleName}.
     * Modifier order in source is not significant.
     */
    public static final class Requires extends Directive {
        private final String moduleName;
        private final boolean transitive;
        private final boolean staticPhase;

        public Requires(final String moduleName, final boolean transitive, final boolean staticPhase) {
            this.moduleName = moduleName;
            this.transitive = transitive;
            this.staticPhase = staticPhase;
        }

        public String getModuleName() {
            return moduleName;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public boolean isStaticPhase() {
            return staticPhase;
        }
    }

    /**
     * {@code exports PackageName [to ModuleName {, ModuleName}]}.
     * An empty target list is an unqualified export.
     */
    public static final class Exports extends PackageDirective {
        public Exports(final String packageName, final List<String> targets) {
            super(packageName, targets);
        }
    }

    /**
     * {@code opens PackageName [to ModuleName {, ModuleName}]}.
     * An empty target list is an unqualified open.
     */
    public static final class Opens extends PackageDirective {
        public Opens(final String packageName, final List<String> targets) {
            super(packageName, targets);
        }
    }

    /**
     * Shared shape of {@code exports} and {@code opens}.
     */
    public abstract static class PackageDirective extends Directive {
        private final String packageName;
        private final List<String> targets;

        protected PackageDirective(final String packageName, final List<String> targets) {
            this.packageName = packageName;
            this.targets = Collections.unmodifiableList(new ArrayList<>(targets));
        }

        public String getPackageName() {
            return packageName;
        }

        /**
         * Friend modules of a qualified directive. Empty when the directive is unqualified.
         */
        public List<String> getTargets() {
            return targets;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }
    }

    /**
     * {@code uses TypeName}. {@link #getResolvedType()} is filled in during semantic analysis.
     */
    public static final class Uses extends Directive {
        private final String serviceName;
        private ClassNode resolvedType;

        public Uses(final String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public ClassNode getResolvedType() {
            return resolvedType;
        }

        public void setResolvedType(final ClassNode resolvedType) {
            this.resolvedType = resolvedType;
        }
    }

    /**
     * {@code provides TypeName with TypeName {, TypeName}}.
     * Resolved types are filled in during semantic analysis.
     */
    public static final class Provides extends Directive {
        private final String serviceName;
        private final List<String> providerNames;
        private ClassNode resolvedService;
        private List<ClassNode> resolvedProviders = List.of();

        public Provides(final String serviceName, final List<String> providerNames) {
            this.serviceName = serviceName;
            this.providerNames = Collections.unmodifiableList(new ArrayList<>(providerNames));
        }

        public String getServiceName() {
            return serviceName;
        }

        public List<String> getProviderNames() {
            return providerNames;
        }

        public ClassNode getResolvedService() {
            return resolvedService;
        }

        public void setResolvedService(final ClassNode resolvedService) {
            this.resolvedService = resolvedService;
        }

        public List<ClassNode> getResolvedProviders() {
            return resolvedProviders;
        }

        public void setResolvedProviders(final List<ClassNode> resolvedProviders) {
            this.resolvedProviders = Collections.unmodifiableList(new ArrayList<>(resolvedProviders));
        }
    }
}
