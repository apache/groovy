/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.control;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.Verifier;
import org.objectweb.asm.Opcodes;

public class ClassNodeResolver {
    
    public static class LookupResult {
        private SourceUnit su;
        private ClassNode cn;
        public LookupResult(SourceUnit su, ClassNode cn) {
            this.su = su;
            this.cn = cn;
        }
        public boolean isClassNode() { return cn!=null; }
        public boolean isSourceUnit() { return su!=null; }
        public SourceUnit getSourceUnit() { return su; }
        public ClassNode getClassNode() { return cn; }
    }
    
    private Map<String,ClassNode> cachedClasses = new HashMap();
    protected static final ClassNode NO_CLASS = new ClassNode("NO_CLASS", Opcodes.ACC_PUBLIC,ClassHelper.OBJECT_TYPE){
        public void setRedirect(ClassNode cn) {
            throw new GroovyBugError("This is a dummy class node only! Never use it for real classes.");
        };
    };
    
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
    
    public void cacheClass(String name, ClassNode res) {
        cachedClasses.put(name, res);
    }
    
    public ClassNode getFromClassCache(String name) {
        // We use here the class cache cachedClasses to prevent
        // calls to ClassLoader#loadClass. Disabling this cache will
        // cause a major performance hit.
        ClassNode cached = cachedClasses.get(name);
        return cached;
    }
    
    public LookupResult findClassNode(String name, CompilationUnit compilationUnit) {
        return tryAsLoaderClassOrScript(name, compilationUnit);
    }
    
    private LookupResult tryAsLoaderClassOrScript(String name, CompilationUnit compilationUnit) {
        GroovyClassLoader loader = compilationUnit.getClassLoader();
        Class cls;
        try {
            // NOTE: it's important to do no lookup against script files
            // here since the GroovyClassLoader would create a new CompilationUnit
            cls = loader.loadClass(name, false, true);
        } catch (ClassNotFoundException cnfe) {
            LookupResult lr = tryAsScript(name, compilationUnit, null);
            return lr;
        } catch (CompilationFailedException cfe) {
            throw new GroovyBugError("The lookup for "+name+" caused a failed compilaton. There should not have been any compilation from this call.");
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
        if (cls.getClassLoader() != loader) {
            return tryAsScript(name, compilationUnit, cls);
        }
        ClassNode cn = ClassHelper.make(cls);
        return new LookupResult(null,cn); 
    }
    
    private LookupResult tryAsScript(String name, CompilationUnit compilationUnit, Class oldClass) {
        LookupResult lr = null;
        if (oldClass!=null) {
            ClassNode cn = ClassHelper.make(oldClass);
            lr = new LookupResult(null,cn);
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

    // NOTE: copied from GroovyClassLoader
    private long getTimeStamp(Class cls) {
        return Verifier.getTimestamp(cls);
    }

    // NOTE: copied from GroovyClassLoader
    private boolean isSourceNewer(URL source, Class cls) {
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
