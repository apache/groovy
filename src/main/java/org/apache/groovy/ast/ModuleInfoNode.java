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

import groovy.lang.GroovyRuntimeException;
import groovy.transform.Internal;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.FileReaderSource;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.io.URLReaderSource;

import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final String GROOVY_FILE_NAME = "module-info.groovy";
    private static final String JAVA_FILE_NAME = "module-info.java";

    private final String name;
    private final boolean open;
    private final List<Directive> directives;
    private ModuleNode sourceModule;
    private Set<String> reads = Set.of();
    private boolean readsReady;
    private boolean resolutionFailed;
    private boolean typesResolved;
    private ModuleFinder accessibilityFinder;
    // Extra package exports granted to this module via --add-exports (sourceModule -> packages).
    private Map<String, Set<String>> extraExports = Map.of();
    // Packages owned by any module (explicit or automatic) on --upgrade-module-path / --module-path.
    private Set<String> boundedPackages = Set.of();
    // Whether --add-reads <self>=ALL-UNNAMED was specified.
    private boolean readsUnnamed;
    // Packages exported to this module by modules in reads (plus all packages of
    // automatic modules in reads and any matching --add-exports). Built once per resolution.
    private Set<String> accessiblePackages;
    // Packages of explicit modules the finder can observe. Automatic modules on
    // the legacy compilation classpath are omitted because the Groovy runtime
    // lives there; automatic modules on --module-path are tracked in boundedPackages.
    private Set<String> explicitPackages;
    // Collector annotations that were expanded into concrete annotations, retained
    // so ModuleInfoVerifier can still verify accessibility of the collector type.
    private List<AnnotationNode> expandedCollectors = List.of();
    // Types resolved inside annotation member expressions before constant inlining
    // replaces them (e.g., KeyEvent.VK_ENTER -> 10).
    private List<Map.Entry<ClassNode, ASTNode>> annotationTypeRefs = List.of();

    public ModuleInfoNode(final String name, final boolean open, final List<Directive> directives) {
        this.name = name;
        this.open = open;
        this.directives = Collections.unmodifiableList(new ArrayList<>(directives));
    }

    /**
     * Whether {@code sourceUnit} represents a modular compilation unit
     * ({@code module-info.groovy} or {@code module-info.java}).
     * <p>
     * When the source is backed by a {@link FileReaderSource} or {@link URLReaderSource},
     * the underlying file name or URI path segment is inspected directly instead of
     * splitting the descriptive {@link SourceUnit#getName()}. Only non-file, non-URL
     * sources (such as in-memory strings) fall back to {@code SourceUnit#getName()},
     * splitting on {@code '/'} and the host platform separator so a literal backslash in
     * a POSIX file name such as {@code x\module-info.groovy} is not treated as a directory.
     *
     * @param sourceUnit the source unit to inspect, or {@code null}
     * @return {@code true} when {@code sourceUnit} is a module descriptor source
     * @since 7.0.0
     */
    public static boolean isModuleInfoSource(final SourceUnit sourceUnit) {
        return isModuleInfoFileName(sourceFileName(sourceUnit));
    }

    /**
     * Whether {@code file} is a modular compilation unit file.
     * Uses {@link File#getName()} directly, respecting the host filesystem separator.
     *
     * @param file the source file, or {@code null}
     * @return {@code true} when {@code file} is named {@code module-info.groovy} or {@code module-info.java}
     * @since 7.0.0
     */
    public static boolean isModuleInfoSource(final File file) {
        return file != null && isModuleInfoFileName(file.getName());
    }

    /**
     * Whether {@code name} is the file name or logical path of a modular compilation unit.
     * A path is reduced to its last segment using {@code '/'} and {@link File#separatorChar},
     * so {@code src/module-info.groovy} matches everywhere while {@code x\module-info.groovy}
     * does not match on POSIX systems where {@code '\'} is a legal file-name character.
     */
    public static boolean isModuleInfoSource(final String name) {
        return isModuleInfoFileName(leafFileName(name, File.separatorChar));
    }

    private static boolean isModuleInfoFileName(final String fileName) {
        return GROOVY_FILE_NAME.equals(fileName) || JAVA_FILE_NAME.equals(fileName);
    }

    /**
     * Extracts the leaf source file name for {@code sourceUnit}, preferring the
     * backing {@link FileReaderSource} or {@link URLReaderSource} over the
     * descriptive {@link SourceUnit#getName()}.
     *
     * @param sourceUnit the source unit, or {@code null}
     * @return the leaf file name, or {@code null} when unavailable
     * @since 7.0.0
     */
    @Internal
    public static String sourceFileName(final SourceUnit sourceUnit) {
        if (sourceUnit == null) {
            return null;
        }
        ReaderSource reader = sourceUnit.getSource();
        if (reader instanceof FileReaderSource fileSource) {
            File file = fileSource.getFile();
            return file != null ? file.getName() : null;
        }
        if (reader instanceof URLReaderSource urlSource) {
            try {
                return leafFileNameFromUri(urlSource.getURI());
            } catch (GroovyRuntimeException ignored) {
                // URLs with unencoded characters (e.g., spaces via legacy File.toURL()) fail URI conversion;
                // fall back to SourceUnit#getName() instead of aborting parsing of ordinary Groovy files.
            }
        }
        return leafFileName(sourceUnit.getName(), File.separatorChar);
    }

    static String leafFileNameFromUri(final URI uri) {
        if (uri == null) {
            return null;
        }
        String path;
        boolean opaque = uri.isOpaque();
        if (opaque) {
            // URI.getSchemeSpecificPart() is always non-null and excludes any #fragment.
            path = uri.getSchemeSpecificPart();
            int query = path.indexOf('?');
            if (query >= 0) {
                path = path.substring(0, query);
            }
        } else {
            path = uri.getPath();
        }
        if (path.isEmpty()) {
            return null;
        }
        int sep = path.lastIndexOf('/');
        if (opaque) {
            sep = Math.max(sep, path.lastIndexOf('!'));
        }
        String leaf = sep >= 0 ? path.substring(sep + 1) : path;
        return leaf.isEmpty() ? null : leaf;
    }

    static String leafFileName(final String name, final char platformSeparator) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        int sep = name.lastIndexOf('/');
        if (platformSeparator != '/') {
            sep = Math.max(sep, name.lastIndexOf(platformSeparator));
        }
        String leaf = sep >= 0 ? name.substring(sep + 1) : name;
        return leaf.isEmpty() ? null : leaf;
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
     * Records {@code @AnnotationCollector} annotations that were expanded so
     * module verification can still check their accessibility.
     *
     * @param collectors the expanded collector annotations
     * @since 7.0.0
     */
    @Internal
    public void setExpandedCollectors(final List<AnnotationNode> collectors) {
        this.expandedCollectors = collectors == null || collectors.isEmpty()
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(collectors));
    }

    /**
     * {@code @AnnotationCollector} annotations expanded from this declaration.
     *
     * @return an unmodifiable list of expanded collector annotations
     * @since 7.0.0
     */
    @Internal
    public List<AnnotationNode> getExpandedCollectors() {
        return expandedCollectors;
    }

    /**
     * Records a type resolved inside an annotation member expression so
     * module verification can still check its accessibility after constant inlining.
     *
     * @param type the resolved type
     * @param node the AST node where the type was referenced
     * @since 7.0.0
     */
    @Internal
    public void addAnnotationTypeRef(final ClassNode type, final ASTNode node) {
        if (type == null || node == null) {
            return;
        }
        if (annotationTypeRefs.isEmpty()) {
            annotationTypeRefs = new ArrayList<>();
        }
        annotationTypeRefs.add(Map.entry(type, node));
    }

    /**
     * Types recorded while resolving annotation member expressions on this declaration.
     *
     * @return an unmodifiable list of type-to-node entries
     * @since 7.0.0
     */
    @Internal
    public List<Map.Entry<ClassNode, ASTNode>> getAnnotationTypeRefs() {
        return Collections.unmodifiableList(annotationTypeRefs);
    }

    /**
     * The module finder adopted from the latest module-graph resolution, or {@code null}.
     *
     * @return the accessibility module finder, or {@code null}
     * @since 7.0.0
     */
    @Internal
    public ModuleFinder getAccessibilityFinder() {
        return accessibilityFinder;
    }

    /**
     * Extra package exports granted to this module via {@code --add-exports}
     * ({@code sourceModule -> exported packages}).
     *
     * @return an unmodifiable map of extra exports
     * @since 7.0.0
     */
    @Internal
    public Map<String, Set<String>> getExtraExports() {
        return extraExports;
    }

    /**
     * Whether {@code --add-reads <self>=ALL-UNNAMED} was configured.
     *
     * @return {@code true} when unnamed-module readability was explicitly granted
     * @since 7.0.0
     */
    @Internal
    public boolean isReadsUnnamed() {
        return readsUnnamed;
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
        adoptResolution(reads, finder, failed, Map.of(), Set.of(), false);
    }

    /**
     * Stores the compile-time readability set together with JEP 261 command-line
     * overrides ({@code --add-exports}, {@code --add-reads ALL-UNNAMED}, and
     * packages bounded by {@code --module-path} / {@code --upgrade-module-path}).
     *
     * @param reads modules readable from this one, or {@code null}
     * @param finder finder used to test exports, or {@code null} when resolution failed
     * @param failed whether resolution failed
     * @param extraExports extra exports targeting this module via {@code --add-exports}
     * @param boundedPackages packages of modules (explicit and automatic) on {@code --module-path}
     * @param readsUnnamed whether {@code --add-reads <self>=ALL-UNNAMED} is active
     * @since 7.0.0
     */
    @Internal
    public void adoptResolution(final Set<String> reads, final ModuleFinder finder, final boolean failed,
            final Map<String, Set<String>> extraExports, final Set<String> boundedPackages,
            final boolean readsUnnamed) {
        this.reads = reads == null || reads.isEmpty() ? Set.of() : Set.copyOf(reads);
        this.accessibilityFinder = finder;
        this.extraExports = extraExports == null || extraExports.isEmpty() ? Map.of() : Map.copyOf(extraExports);
        this.boundedPackages = boundedPackages == null || boundedPackages.isEmpty() ? Set.of() : Set.copyOf(boundedPackages);
        this.readsUnnamed = readsUnnamed;
        this.accessiblePackages = null;
        this.explicitPackages = null;
        this.resolutionFailed = failed;
        this.readsReady = !failed;
    }

    /**
     * Whether a public type from another module is accessible from this one
     * (JLS §6.6 / §7.7).
     *
     * @param type the resolved type, or {@code null}
     * @return {@code true} when the type may be referenced
     * @since 7.0.0
     */
    public boolean canAccess(final ClassNode type) {
        return canAccess(type, null);
    }

    /**
     * Whether {@code type} is accessible from {@code accessor} inside this module
     * (JLS §6.6 / §7.7). Types declared in this compilation are accessible
     * even when their package is not exported. A package owned by an explicit
     * module (or any module on {@code --module-path}) is accessible only when a
     * module this one reads exports that package to it (including via
     * {@code --add-exports}). A package that no explicit or module-path module
     * owns stays accessible so the Groovy runtime on the legacy compilation
     * classpath remains reachable. Within an accessible package from a read
     * module, public types (with public enclosing types) and {@code protected}
     * nested types inherited by {@code accessor} are accessible. Returns
     * {@code true} when the readability set is not ready, so a failed module
     * graph does not cascade into type errors.
     *
     * @param type the resolved type, or {@code null}
     * @param accessor the class referencing {@code type}, or {@code null} when there is no class context
     * @return {@code true} when the type may be referenced
     * @since 7.0.0
     */
    public boolean canAccess(final ClassNode type, final ClassNode accessor) {
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
        String pkg = resolved.getPackageName();
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        if (!isPackageExportedToSelf(pkg)) {
            return !ownedByBoundedModule(pkg);
        }
        return isVisibleType(resolved, accessor);
    }

    /**
     * Whether {@code pkg} is accessible to source classes in this module:
     * exported to this module by a module it reads, or not owned by any
     * observable explicit or module-path module.
     *
     * @param pkg the package name
     * @return {@code true} when the package may be imported or referenced
     * @since 7.0.0
     */
    @Internal
    public boolean canAccessPackage(final String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        if (!readsReady || accessibilityFinder == null) {
            return true;
        }
        return isPackageExportedToSelf(pkg) || !ownedByBoundedModule(pkg);
    }

    /**
     * Whether any module in {@link #getReads()} exports {@code pkg} to this module
     * (including via {@code --add-exports} or a read automatic module).
     *
     * @param pkg the package name
     * @return {@code true} when {@code pkg} is exported to this module by a read dependency
     * @since 7.0.0
     */
    @Internal
    public boolean isPackageExportedToSelf(final String pkg) {
        if (pkg == null || pkg.isEmpty() || !readsReady || accessibilityFinder == null) {
            return false;
        }
        return accessiblePackages().contains(pkg);
    }

    private Set<String> accessiblePackages() {
        Set<String> packages = accessiblePackages;
        if (packages == null) {
            packages = computeAccessiblePackages(reads, accessibilityFinder, name, extraExports);
            accessiblePackages = packages;
        }
        return packages;
    }

    @Internal
    public static Set<String> computeAccessiblePackages(final Set<String> reads, final ModuleFinder finder, final String moduleName) {
        return computeAccessiblePackages(reads, finder, moduleName, Map.of());
    }

    @Internal
    public static Set<String> computeAccessiblePackages(final Set<String> reads, final ModuleFinder finder,
            final String moduleName, final Map<String, Set<String>> extraExports) {
        if (reads.isEmpty() || finder == null) {
            return Set.of();
        }
        Set<String> packages = new LinkedHashSet<>();
        for (String dependency : reads) {
            addExportedPackages(finder, dependency, moduleName, packages);
            if (extraExports != null) {
                Set<String> added = extraExports.get(dependency);
                if (added != null) {
                    packages.addAll(added);
                }
            }
        }
        return Set.copyOf(packages);
    }

    private static void addExportedPackages(final ModuleFinder finder, final String dependency,
            final String moduleName, final Set<String> packages) {
        Optional<ModuleReference> reference;
        try {
            reference = finder.find(dependency);
        } catch (RuntimeException ignored) {
            return;
        }
        if (reference.isEmpty()) {
            return;
        }
        ModuleDescriptor descriptor = reference.get().descriptor();
        if (descriptor.isAutomatic()) {
            packages.addAll(descriptor.packages());
            return;
        }
        for (ModuleDescriptor.Exports exports : descriptor.exports()) {
            if (exports.targets().isEmpty() || exports.targets().contains(moduleName)) {
                packages.add(exports.source());
            }
        }
    }

    /**
     * Whether an explicit module (anywhere observable) or any module on
     * {@code --module-path} / {@code --upgrade-module-path} owns {@code pkg}.
     * Automatic modules on the legacy compilation classpath are not a boundary
     * for ordinary type references so the Groovy runtime remains reachable.
     */
    private boolean ownedByBoundedModule(final String pkg) {
        if (boundedPackages.contains(pkg)) {
            return true;
        }
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

    private static boolean isVisibleType(final ClassNode type, final ClassNode accessor) {
        if (isPublicIncludingEnclosing(type)) {
            return true;
        }
        if (accessor == null) {
            return false;
        }
        boolean samePackage = Objects.equals(type.getPackageName(), accessor.getPackageName());
        return isVisibleMember(type, accessor, samePackage);
    }

    private static boolean isVisibleMember(final ClassNode type, final ClassNode accessor, final boolean samePackage) {
        int mods = effectiveModifiers(type);
        if (Modifier.isPrivate(mods)) {
            return false;
        }
        ClassNode outer = enclosingType(type);
        if (outer == null) {
            return Modifier.isPublic(mods) || samePackage;
        }
        if (!isVisibleMember(outer, accessor, samePackage)) {
            return false;
        }
        if (Modifier.isPublic(mods)) {
            return true;
        }
        if (Modifier.isProtected(mods)) {
            return samePackage || isSubclassContext(accessor, outer);
        }
        return samePackage;
    }

    private static boolean isSubclassContext(final ClassNode accessor, final ClassNode owner) {
        for (ClassNode current = accessor; current != null; current = current.getOuterClass()) {
            if (current.isDerivedFrom(owner) || (owner.isInterface() && current.implementsInterface(owner))) {
                return true;
            }
        }
        return false;
    }

    private static ClassNode enclosingType(final ClassNode type) {
        ClassNode outer = type.getOuterClass();
        if (outer != null) {
            return outer;
        }
        if (type instanceof InnerClassNode) {
            return null;
        }
        if (type instanceof DecompiledClassNode decompiled) {
            String enclosing = decompiled.getMemberDeclaringClassName();
            return enclosing != null ? decompiled.resolveClassNullable(enclosing) : null;
        }
        Class<?> loaded = resolvedClass(type);
        if (loaded == null) {
            return null;
        }
        try {
            Class<?> declaring = loaded.getDeclaringClass();
            return declaring != null ? ClassHelper.make(declaring) : null;
        } catch (LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static int effectiveModifiers(final ClassNode type) {
        if (!(type instanceof InnerClassNode) && !(type instanceof DecompiledClassNode)) {
            Class<?> loaded = resolvedClass(type);
            if (loaded != null) {
                try {
                    return loaded.getModifiers();
                } catch (LinkageError | RuntimeException ignored) {
                    // Fall back to AST modifiers if the class's InnerClasses metadata is inconsistent.
                }
            }
        }
        return type.getModifiers();
    }

    /**
     * Whether {@code type} and every enclosing class are {@code public}.
     *
     * @param type the type to inspect
     * @return {@code true} when {@code type} and all enclosing types are public
     * @since 7.0.0
     */
    @Internal
    public static boolean isPublicIncludingEnclosing(final ClassNode type) {
        for (ClassNode current = type; current != null; current = enclosingType(current)) {
            if (!Modifier.isPublic(effectiveModifiers(current))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the loaded JVM class for {@code type}, or {@code null} when
     * {@code type} is unresolved or cannot be loaded by the runtime loader.
     *
     * @param type the class node
     * @return the JVM class, or {@code null}
     * @since 7.0.0
     */
    @Internal
    public static Class<?> resolvedClass(final ClassNode type) {
        if (!type.isResolved()) {
            return null;
        }
        try {
            return type.getTypeClass();
        } catch (RuntimeException | LinkageError | GroovyBugError ignored) {
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
