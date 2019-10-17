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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.decompiled.AsmDecompiler;
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.classgen.Verifier;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used as a pluggable way to resolve class names.
 * An instance of this class has to be added to {@link CompilationUnit} using 
 * {@link CompilationUnit#setClassNodeResolver(ClassNodeResolver)}. The 
 * CompilationUnit will then set the resolver on the {@link ResolveVisitor} each 
 * time new. The ResolveVisitor will prepare name lookup and then finally ask
 * the resolver if the class exists. This resolver then can return either a 
 * SourceUnit or a ClassNode. In case of a SourceUnit the compiler is notified
 * that a new source is to be added to the compilation queue. In case of a
 * ClassNode no further action than the resolving is done. The lookup result
 * is stored in the helper class {@link LookupResult}. This class provides a
 * class cache to cache lookups. If you don't want this, you have to override
 * the methods {@link ClassNodeResolver#cacheClass(String, ClassNode)} and 
 * {@link ClassNodeResolver#getFromClassCache(String)}. Custom lookup logic is
 * supposed to go into the method 
 * {@link ClassNodeResolver#findClassNode(String, CompilationUnit)} while the 
 * entry method is {@link ClassNodeResolver#resolveName(String, CompilationUnit)}
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
         * returns true if a SourecUnit is stored
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
    protected static final ClassNode NO_CLASS = new ClassNode("NO_CLASS", Opcodes.ACC_PUBLIC,ClassHelper.OBJECT_TYPE){
        public void setRedirect(ClassNode cn) {
            throw new GroovyBugError("This is a dummy class node only! Never use it for real classes.");
        }
    };
    
    /**
     * Resolves the name of a class to a SourceUnit or ClassNode. If no
     * class or source is found this method returns null. A lookup is done
     * by first asking the cache if there is an entry for the class already available
     * to then call {@link #findClassNode(String, CompilationUnit)}. The result 
     * of that method call will be cached if a ClassNode is found. If a SourceUnit
     * is found, this method will not be asked later on again for that class, because
     * ResolveVisitor will first ask the CompilationUnit for classes in the
     * compilation queue and it will find the class for that SourceUnit there then.
     * method return a ClassNode instead of a SourceUnit, the res 
     * @param name - the name of the class
     * @param compilationUnit - the current CompilationUnit
     * @return the LookupResult
     */
    public LookupResult resolveName(String name, CompilationUnit compilationUnit) {
        ClassNode res = getFromClassCache(name);
        if (res==NO_CLASS) return null;
        if (res!=null) return new LookupResult(null,res);
        LookupResult lr = findClassNode(name, compilationUnit);
        if (lr != null) {
            if (lr.isClassNode()) cacheClass(name, lr.getClassNode());
            return lr;
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
    public void cacheClass(String name, ClassNode res) {
        cachedClasses.put(name, res);
    }
    
    /**
     * returns whatever is stored in the class cache for the given name
     * @param name - the name of the class
     * @return the result of the lookup, which may be null
     */
    public ClassNode getFromClassCache(String name) {
        // We use here the class cache cachedClasses to prevent
        // calls to ClassLoader#loadClass. Disabling this cache will
        // cause a major performance hit.
        ClassNode cached = cachedClasses.get(name);
        return cached;
    }
    
    /**
     * Extension point for custom lookup logic of finding ClassNodes. Per default
     * this will use the CompilationUnit class loader to do a lookup on the class
     * path and load the needed class using that loader. Or if a script is found 
     * and that script is seen as "newer", the script will be used instead of the 
     * class.
     * 
     * @param name - the name of the class
     * @param compilationUnit - the current compilation unit
     * @return the lookup result
     */
    public LookupResult findClassNode(String name, CompilationUnit compilationUnit) {
        return tryAsLoaderClassOrScript(name, compilationUnit);
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
     */
    private LookupResult tryAsLoaderClassOrScript(String name, CompilationUnit compilationUnit) {
        GroovyClassLoader loader = compilationUnit.getClassLoader();

        Map<String, Boolean> options = compilationUnit.configuration.getOptimizationOptions();
        boolean useAsm = !Boolean.FALSE.equals(options.get("asmResolving"));
        boolean useClassLoader = !Boolean.FALSE.equals(options.get("classLoaderResolving"));

        LookupResult result = useAsm ? findDecompiled(name, compilationUnit, loader) : null;
        if (result != null) {
            return result;
        }

        if (!useClassLoader) {
            return tryAsScript(name, compilationUnit, null);
        }

        return findByClassLoading(name, compilationUnit, loader);
    }

    /**
     * Search for classes using class loading
     */
    private static LookupResult findByClassLoading(String name, CompilationUnit compilationUnit, GroovyClassLoader loader) {
        Class cls;
        try {
            // NOTE: it's important to do no lookup against script files
            // here since the GroovyClassLoader would create a new CompilationUnit
            cls = loader.loadClass(name, false, true);
        } catch (ClassNotFoundException cnfe) {
            LookupResult lr = tryAsScript(name, compilationUnit, null);
            return lr;
        } catch (CompilationFailedException cfe) {
            throw new GroovyBugError("The lookup for " + name + " caused a failed compilation. There should not have been any compilation from this call.", cfe);
        }
        //TODO: the case of a NoClassDefFoundError needs a bit more research
        // a simple recompilation is not possible it seems. The current class
        // we are searching for is there, so we should mark that somehow.
        // Basically the missing class needs to be completely compiled before
        // we can again search for the current name.
        /*catch (NoClassDefFoundError ncdfe) {
            cachedClasses.put(name,SCRIPT);
            return false;
        }*/
        if (cls == null) return null;
        //NOTE: we might return false here even if we found a class,
        //      because  we want to give a possible script a chance to
        //      recompile. This can only be done if the loader was not
        //      the instance defining the class.
        ClassNode cn = ClassHelper.make(cls);
        if (cls.getClassLoader() != loader) {
            return tryAsScript(name, compilationUnit, cn);
        }
        return new LookupResult(null,cn);
    }

    /**
     * Search for classes using ASM decompiler
     */
    private LookupResult findDecompiled(String name, CompilationUnit compilationUnit, GroovyClassLoader loader) {
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
                    // this may happen under Windows because getResource is case insensitive under that OS!
                    asmClass = null;
                }
            } catch (IOException e) {
                // fall through and attempt other search strategies
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

    private static boolean isFromAnotherClassLoader(GroovyClassLoader loader, String fileName) {
        ClassLoader parent = loader.getParent();
        return parent != null && parent.getResource(fileName) != null;
    }

    /**
     * try to find a script using the compilation unit class loader.
     */
    private static LookupResult tryAsScript(String name, CompilationUnit compilationUnit, ClassNode oldClass) {
        LookupResult lr = null;
        if (oldClass!=null) {
            lr = new LookupResult(null, oldClass);
        }
        
        if (name.startsWith("java.")) return lr;
        //TODO: don't ignore inner static classes completely
        if (name.indexOf('$') != -1) return lr;
        
        // try to find a script from classpath*/
        GroovyClassLoader gcl = compilationUnit.getClassLoader();
        URL url = null;
        try {
            url = gcl.getResourceLoader().loadGroovySource(name);
        } catch (MalformedURLException e) {
            // fall through and let the URL be null
        }
        if (url != null && ( oldClass==null || isSourceNewer(url, oldClass))) {
            SourceUnit su = compilationUnit.addSource(url);
            return new LookupResult(su,null);
        }
        return lr;
    }

    /**
     * get the time stamp of a class
     * NOTE: copied from GroovyClassLoader
     */
    private static long getTimeStamp(ClassNode cls) {
        if (!(cls instanceof DecompiledClassNode)) {
            return Verifier.getTimestamp(cls.getTypeClass());
        }

        return ((DecompiledClassNode) cls).getCompilationTimeStamp();
    }

    /**
     * returns true if the source in URL is newer than the class
     * NOTE: copied from GroovyClassLoader
     */
    private static boolean isSourceNewer(URL source, ClassNode cls) {
        try {
            long lastMod;

            // Special handling for file:// protocol, as getLastModified() often reports
            // incorrect results (-1)
            if (source.getProtocol().equals("file")) {
                // Coerce the file URL to a File
                String path = source.getPath().replace('/', File.separatorChar).replace('|', ':');
                File file = new File(path);
                lastMod = file.lastModified();
            } else {
                URLConnection conn = source.openConnection();
                lastMod = conn.getLastModified();
                conn.getInputStream().close();
            }
            return lastMod > getTimeStamp(cls);
        } catch (IOException e) {
            // if the stream can't be opened, let's keep the old reference
            return false;
        }
    }
}
