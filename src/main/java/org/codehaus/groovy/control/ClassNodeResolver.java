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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.decompiled.AsmDecompiler;
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.util.URLStreams;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pluggable lookup of class names to a {@link ClassNode} or a {@link SourceUnit}.
 * <p>
 * An instance is installed on a {@link CompilationUnit} via
 * {@link CompilationUnit#setClassNodeResolver(ClassNodeResolver)}. The compilation
 * unit then sets the resolver on {@link ResolveVisitor} for each resolving pass.
 * {@link ResolveVisitor} prepares the name and asks this resolver whether the
 * class exists. A {@link SourceUnit} result means the compiler should add that
 * source to the compilation queue; a {@link ClassNode} result completes resolving
 * for that name. The outcome is wrapped in {@link LookupResult}.
 * <p>
 * The default lookup prefers ASM decompilation of a {@code .class} resource so a
 * type can be described without linking it in the JVM. Class-loader lookup is the
 * fallback when decompilation is disabled or the class exists only in memory.
 * {@link NoClassDefFoundError} during class-loader lookup is recovered by
 * decompiling the still-present bytecode or by compiling a groovy source of the
 * same name; an unrecoverable error is rethrown with the looked-up name in the
 * message and is not cached as a miss.
 * <p>
 * Lookups are cached. Override {@link #cacheClass(String, ClassNode)} and
 * {@link #getFromClassCache(String)} to disable or replace the cache. Custom
 * lookup logic belongs in {@link #findClassNode(String, CompilationUnit)}; the
 * entry point is {@link #resolveName(String, CompilationUnit)}.
 */
public class ClassNodeResolver {

    /**
     * Helper class to return either a SourceUnit or ClassNode.
     */
    public static class LookupResult {
        private final SourceUnit su;
        private final ClassNode cn;
        /**
         * creates a new LookupResult. You are not supposed to supply
         * a SourceUnit and a ClassNode at the same time
         */
        public LookupResult(SourceUnit su, ClassNode cn) {
            this.su = su;
            this.cn = cn;
            if (su==null && cn==null) throw new IllegalArgumentException("Either the SourceUnit or the ClassNode must not be null.");
            if (su!=null && cn!=null) throw new IllegalArgumentException("SourceUnit and ClassNode cannot be set at the same time.");
        }
        /**
         * returns true if a ClassNode is stored
         */
        public boolean isClassNode() { return cn!=null; }
        /**
         * returns true if a SourceUnit is stored
         */
        public boolean isSourceUnit() { return su!=null; }
        /**
         * returns the SourceUnit
         */
        public SourceUnit getSourceUnit() { return su; }
        /**
         * returns the ClassNode
         */
        public ClassNode getClassNode() { return cn; }
    }

    // Map to store cached classes
    private final Map<String, ClassNode> cachedClasses = new HashMap<>();
    /**
     * Internal helper used to indicate a cache hit for a class that does not exist.
     * This way further lookups through a slow {@link #findClassNode(String, CompilationUnit)}
     * path can be avoided.
     * WARNING: This class is not to be used outside of ClassNodeResolver.
     */
    protected static final ClassNode NO_CLASS = new ClassNode("NO_CLASS", Opcodes.ACC_PUBLIC,ClassHelper.OBJECT_TYPE) {
        @Override
        public void setRedirect(ClassNode cn) {
            throw new GroovyBugError("This is a dummy class node only! Never use it for real classes.");
        }
    };

    // Map to store the package-info of resolved packages (GROOVY-12207)
    private final Map<String, PackageNode> cachedPackages = new HashMap<>();
    /**
     * Internal helper used to indicate a cache hit for a package that has no
     * {@code package-info.class} (or none carrying annotations). This provides
     * negative caching so a missing/annotation-free package-info is looked up
     * from the class loader only once.
     * WARNING: This node is not to be used outside of ClassNodeResolver.
     */
    private static final PackageNode NO_PACKAGE = new PackageNode("NO_PACKAGE");

    /**
     * Resolves a class name to a {@link SourceUnit} or {@link ClassNode}.
     * Returns {@code null} if neither is found.
     * <p>
     * The cache is consulted first. A cached {@link #NO_CLASS} is returned as
     * {@code null}. On a cache miss {@link #findClassNode(String, CompilationUnit)}
     * is called. A {@link ClassNode} result is cached; a {@link SourceUnit} result
     * is not, because {@link ResolveVisitor} will subsequently find that class in
     * the compilation queue. A miss is cached as {@link #NO_CLASS} so the slow
     * lookup path is not repeated.
     *
     * @param name the fully qualified class name
     * @param compilationUnit the current compilation unit
     * @return the lookup result, or {@code null} if the name cannot be resolved
     */
    public LookupResult resolveName(final String name, final CompilationUnit compilationUnit) {
        ClassNode type = getFromClassCache(name);
        if (type != null) {
            if (type == NO_CLASS) return null;
            return new LookupResult(null, type);
        }

        LookupResult result = findClassNode(name, compilationUnit);
        if (result != null) {
            if (result.isClassNode()) {
                cacheClass(name, result.getClassNode());
            }
            return result;
        } else {
            cacheClass(name, NO_CLASS);
            return null;
        }
    }

    /**
     * caches a ClassNode
     * @param name - the name of the class
     * @param res - the ClassNode for that name
     */
    public void cacheClass(final String name, final ClassNode res) {
        cachedClasses.put(name, res);
    }

    /**
     * returns whatever is stored in the class cache for the given name
     * @param name - the name of the class
     * @return the result of the lookup, which may be null
     */
    public ClassNode getFromClassCache(final String name) {
        // We use here the class cache cachedClasses to prevent
        // calls to ClassLoader#loadClass. Disabling this cache will
        // cause a major performance hit.
        ClassNode cached = cachedClasses.get(name);
        return cached;
    }

    /**
     * Resolves a package name to a {@link PackageNode} carrying the annotations found on the
     * package's compiled {@code package-info.class}, if any (GROOVY-12207). This makes
     * package-level annotations of precompiled dependencies (e.g. JSpecify's {@code @NullMarked})
     * visible to type checkers and AST transforms.
     * <p>
     * The {@code package-info.class} is located on the compilation unit's class path and decompiled
     * on demand using the same ASM infrastructure as ordinary classes; results are cached per
     * resolver, including a negative cache for packages that have no (annotation-bearing)
     * package-info. Returns {@code null} if the package has no such metadata.
     *
     * @param packageName the fully qualified package name (no trailing dot), e.g. {@code "foo.bar"}
     * @param compilationUnit the current {@link CompilationUnit}
     * @return a {@link PackageNode} with the package's annotations, or {@code null} if none
     */
    public synchronized PackageNode resolvePackage(final String packageName, final CompilationUnit compilationUnit) {
        if (packageName == null || packageName.isEmpty() || compilationUnit == null) {
            return null;
        }
        PackageNode cached = cachedPackages.get(packageName);
        if (cached != null) {
            return cached == NO_PACKAGE ? null : cached;
        }
        PackageNode result = findPackageInfo(packageName, compilationUnit);
        cachedPackages.put(packageName, result == null ? NO_PACKAGE : result);
        return result;
    }

    /**
     * Loads and decompiles {@code <packageName>/package-info.class} from the compilation unit's
     * class loader, copying its (class-level) annotations onto a fresh {@link PackageNode}. The
     * annotations of a {@code package-info} type are stored as ordinary class annotations in the
     * bytecode, so the existing decompiler pipeline reads them without special handling.
     *
     * @return a populated {@link PackageNode}, or {@code null} if there is no package-info, it has
     *         no annotations, or it could not be read (including a class-format error)
     */
    private PackageNode findPackageInfo(final String packageName, final CompilationUnit compilationUnit) {
        GroovyClassLoader loader = compilationUnit.getClassLoader();
        if (loader == null) {
            return null;
        }
        String fileName = packageName.replace('.', '/') + "/package-info.class";
        URL resource = loader.getResource(fileName);
        if (resource == null) {
            return null;
        }
        try {
            DecompiledClassNode packageInfo = new DecompiledClassNode(
                    AsmDecompiler.parseClass(resource), new AsmReferenceResolver(this, compilationUnit));
            if (!packageInfo.getName().equals(packageName + ".package-info")) {
                // this may happen under Windows/macOS because getResource is case-insensitive there!
                return null;
            }
            List<AnnotationNode> annotations = packageInfo.getAnnotations();
            if (annotations == null || annotations.isEmpty()) {
                return null;
            }
            // the trailing dot matches the naming convention used for source-compiled packages
            // (see AstBuilder.visitPackageDeclaration / ASTHelper.setPackage), so a decompiled
            // package and its source equivalent share the same PackageNode.getName()
            PackageNode packageNode = new PackageNode(packageName + ".");
            packageNode.addAnnotations(annotations);
            return packageNode;
        } catch (IOException e) {
            // fall through; treat as no package metadata available
            return null;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            // class format error or similar from ASM; ignore for resolution purposes
            return null;
        }
    }

    /**
     * Extension point for custom lookup logic. The default implementation uses
     * the compilation unit class loader: ASM decompilation of a {@code .class}
     * resource first, then {@link ClassLoader#loadClass(String)}, then a groovy
     * source of the same name if that source is newer than the loaded class
     * (or if no class was found).
     * <p>
     * {@link NoClassDefFoundError} from class loading is not treated as a miss.
     * The class under lookup is present; a referenced type is not. The default
     * implementation recovers by decompiling the still-present bytecode or by
     * compiling a groovy source of the same name.
     *
     * @param name the fully qualified class name
     * @param compilationUnit the current compilation unit
     * @return the lookup result, or {@code null} if {@code compilationUnit} is
     *         {@code null} or the name cannot be resolved
     */
    public LookupResult findClassNode(final String name, final CompilationUnit compilationUnit) {
        return compilationUnit == null ? null : tryAsLoaderClassOrScript(name, compilationUnit);
    }

    /**
     * This method is used to realize the lookup of a class using the compilation
     * unit class loader. Should no class be found we fall back to a script lookup.
     * If a class is found we check if there is also a script and maybe use that
     * one in case it is newer.<p/>
     *
     * Two class search strategies are possible: by ASM decompilation or by usual Java classloading.
     * The latter is slower but is unavoidable for scripts executed in dynamic environments where
     * the referenced classes might only be available in the classloader, not on disk.
     * Class loading that fails with {@link NoClassDefFoundError} is recovered without
     * treating the name as absent.
     */
    private LookupResult tryAsLoaderClassOrScript(final String name, final CompilationUnit compilationUnit) {
        GroovyClassLoader loader = compilationUnit.getClassLoader();
        Map<String, Boolean> options = compilationUnit.configuration.getOptimizationOptions();
        boolean noClassLoaderResolving = Boolean.FALSE.equals(options.get("classLoaderResolving"));

        if (!Boolean.FALSE.equals(options.get("asmResolving"))) {
            LookupResult result = findDecompiled(name, compilationUnit, loader, noClassLoaderResolving);
            if (result != null) {
                return result;
            }
        }

        if (!noClassLoaderResolving) {
            return findByClassLoading(name, compilationUnit, loader);
        }

        return tryAsScript(name, compilationUnit, null);
    }

    /**
     * Search for classes using class loading.
     * <p>
     * Script files are not considered by {@link GroovyClassLoader#loadClass(String, boolean, boolean)}
     * here ({@code lookupScriptFiles} is {@code false}) so that loader cannot start a nested
     * {@link CompilationUnit}. A {@link NoClassDefFoundError} means the class itself was found
     * but could not be linked; {@link #recoverFromNoClassDefFoundError} handles that case.
     */
    private LookupResult findByClassLoading(final String name, final CompilationUnit compilationUnit, final GroovyClassLoader loader) {
        Class<?> cls;
        try {
            // NOTE: it's important to do no lookup against script files
            // here since the GroovyClassLoader would create a new CompilationUnit
            cls = loader.loadClass(name, false, true);
        } catch (ClassNotFoundException cnfe) {
            return tryAsScript(name, compilationUnit, null);
        } catch (CompilationFailedException cfe) {
            throw new GroovyBugError("The lookup for " + name + " caused a failed compilation. There should not have been any compilation from this call.", cfe);
        } catch (NoClassDefFoundError ncdfe) {
            return recoverFromNoClassDefFoundError(name, compilationUnit, loader, ncdfe);
        }
        if (cls == null) return null;
        // NOTE: even if we found a class we still give a possible script a chance
        // to recompile, but only when this loader was not the instance that defined it.
        ClassNode cn = ClassHelper.make(cls);
        if (cls.getClassLoader() != loader) {
            return tryAsScript(name, compilationUnit, cn);
        }
        return new LookupResult(null,cn);
    }

    /**
     * Recovers from {@link NoClassDefFoundError} thrown while defining or linking
     * {@code name}. The class being looked up is present — a referenced type is not —
     * so treating the failure as a miss would poison {@link #resolveName} with
     * {@link #NO_CLASS} and hide bytecode or a groovy source that can still be used.
     * <p>
     * Recovery order:
     * <ol>
     *   <li>ASM decompilation of matching bytecode, which does not link the class,
     *       so a missing superclass or interface does not prevent producing a
     *       {@link ClassNode}.</li>
     *   <li>If a {@code .class} resource exists for this path but declares a
     *       different binary name (the JVM {@code defineClass} name check, also
     *       seen on case-insensitive filesystems), treat the lookup as a miss and
     *       fall through to script lookup. Detection uses the bytecode name, not
     *       {@link NoClassDefFoundError} text.</li>
     *   <li>A groovy source of the same name, added to the compilation queue.</li>
     * </ol>
     * If none of those succeed the error is rethrown with {@code name} in the
     * message so callers see both the class under lookup and the missing type.
     */
    private LookupResult recoverFromNoClassDefFoundError(final String name, final CompilationUnit compilationUnit,
            final GroovyClassLoader loader, final NoClassDefFoundError ncdfe) {
        LookupResult decompiled = findDecompiled(name, compilationUnit, loader, false);
        if (decompiled != null) {
            return decompiled;
        }
        LookupResult script = tryAsScript(name, compilationUnit, null);
        if (script != null || classFileDeclaresDifferentName(name, compilationUnit, loader)) {
            return script;
        }
        throw wrapNoClassDefFoundError(name, ncdfe);
    }

    /**
     * True when a {@code .class} resource exists for {@code name}'s path but the
     * bytecode's binary name is different. That is the JVM {@code defineClass}
     * check (JVMS 5.3.5), without inspecting {@link NoClassDefFoundError} text:
     * HotSpot's {@code "wrong name"} phrase is an English C format string, OpenJ9
     * does not use it, and the message is not a stable API.
     */
    private boolean classFileDeclaresDifferentName(final String name, final CompilationUnit compilationUnit,
            final GroovyClassLoader loader) {
        String fileName = name.replace('.', '/') + ".class";
        URL resource = loader.getResource(fileName);
        if (resource == null) {
            return false;
        }
        try {
            DecompiledClassNode node = new DecompiledClassNode(
                    AsmDecompiler.parseClass(resource), new AsmReferenceResolver(this, compilationUnit));
            return !name.equals(node.getName());
        } catch (IOException | IllegalArgumentException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Wraps {@code cause} so the class under lookup is visible in the message.
     * {@link NoClassDefFoundError#getMessage()} names the missing dependency,
     * not the class that was being resolved.
     */
    private static NoClassDefFoundError wrapNoClassDefFoundError(final String name, final NoClassDefFoundError cause) {
        String missing = cause.getMessage();
        String message = (missing == null || missing.isEmpty())
                ? "Unable to resolve class " + name + " due to a missing dependency"
                : "Unable to resolve class " + name + " due to missing dependency " + missing;
        NoClassDefFoundError error = new NoClassDefFoundError(message);
        error.initCause(cause);
        return error;
    }

    /**
     * Search for classes using ASM decompiler
     */
    private LookupResult findDecompiled(final String name, final CompilationUnit compilationUnit, final GroovyClassLoader loader, boolean failOnUnexpectedParseClassException) {
        ClassNode node = ClassHelper.make(name);
        if (node.isResolved()) {
            return new LookupResult(null, node);
        }

        DecompiledClassNode asmClass = null;
        String fileName = name.replace('.', '/') + ".class";
        URL resource = loader.getResource(fileName);
        if (resource != null) {
            try {
                asmClass = new DecompiledClassNode(AsmDecompiler.parseClass(resource), new AsmReferenceResolver(this, compilationUnit));
                if (!asmClass.getName().equals(name)) {
                    // this may happen under Windows because getResource is case-insensitive under that OS!
                    asmClass = null;
                }
            } catch (IOException e) {
                // fall through and attempt other search strategies
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                // class format error or similar from ASM (unsupported version,
                // truncated bytes, ...). If we do not try other means we should
                // report this error to the user.
                if (failOnUnexpectedParseClassException) {
                    String detail = e.getMessage();
                    String message = (detail == null || detail.isEmpty())
                            ? "Failed to parse class " + name
                            : "Failed to parse class " + name + ": " + detail;
                    throw new IllegalArgumentException(message, e);
                }
            }
        }

        if (asmClass != null) {
            if (isFromAnotherClassLoader(loader, fileName)) {
                return tryAsScript(name, compilationUnit, asmClass);
            }

            return new LookupResult(null, asmClass);
        }
        return null;
    }

    private static boolean isFromAnotherClassLoader(final GroovyClassLoader loader, final String fileName) {
        ClassLoader parent = loader.getParent();
        return parent != null && parent.getResource(fileName) != null;
    }

    /**
     * Tries to find a script using the compilation unit class loader.
     */
    private static LookupResult tryAsScript(final String name, final CompilationUnit compilationUnit, final ClassNode oldClass) {
        LookupResult lr = null;
        if (oldClass != null) {
            lr = new LookupResult(null, oldClass);
        }
        if (name.startsWith("java.")) {
            return lr;
        }
        int i = name.indexOf('$');
        if (i != -1) {
            return lr;
        }

        // try to find a script from classpath
        GroovyClassLoader gcl = compilationUnit.getClassLoader();
        URL url = null;
        try {
            url = gcl.getResourceLoader().loadGroovySource(name);
        } catch (MalformedURLException e) {
            // fall through and let the URL be null
        }
        if (url != null && (oldClass == null || isSourceNewer(url, oldClass))) {
            SourceUnit sourceUnit = compilationUnit.addSource(url);
            lr = new LookupResult(sourceUnit, null);
        }
        return lr;
    }

    /**
     * Gets the time stamp of a class.
     * <p>
     * NOTE: copied from GroovyClassLoader
     */
    private static long getTimeStamp(final ClassNode cls) {
        if (!(cls instanceof DecompiledClassNode)) {
            return Verifier.getTimestamp(cls.getTypeClass());
        }
        return ((DecompiledClassNode) cls).getCompilationTimeStamp();
    }

    /**
     * Returns true if the source in URL is newer than the class.
     * <p>
     * {@code file:} URLs use {@link File#lastModified()} because
     * {@link java.net.URLConnection#getLastModified()} often reports {@code -1}.
     * Other protocols use {@link URLStreams#getUncachedLastModified(URL)}.
     * <p>
     * NOTE: copied from GroovyClassLoader
     */
    private static boolean isSourceNewer(final URL source, final ClassNode cls) {
        try {
            long lastMod;
            // Special handling for file:// protocol, as getLastModified() often reports
            // incorrect results (-1)
            if ("file".equals(source.getProtocol())) {
                // Coerce the file URL to a File
                String path = source.getPath().replace('/', File.separatorChar).replace('|', ':');
                File file = new File(path);
                lastMod = file.lastModified();
            } else {
                lastMod = URLStreams.getUncachedLastModified(source);
            }
            return lastMod > getTimeStamp(cls);
        } catch (IOException e) {
            // if the stream can't be opened, let's keep the old reference
            return false;
        }
    }
}
