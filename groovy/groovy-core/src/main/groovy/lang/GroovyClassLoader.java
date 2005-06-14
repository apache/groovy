/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package groovy.lang;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A ClassLoader which can load Groovy classes
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan </a>
 * @author Guillaume Laforge
 * @author Steve Goetze
 * @author Bing Ran
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
 * @version $Revision$
 */
public class GroovyClassLoader extends SecureClassLoader {

    private Map cache = new HashMap();

    /**
     * Mirror the value in the superclass since it's private and we need to
     * access it for the classpath.
     */
    private String[] _searchPaths;

    public void removeFromCache(Class aClass) {
        cache.remove(aClass);
    }

    private class PARSING {
    }

    private class NOT_RESOLVED {
    }

    private CompilerConfiguration config;

    private String[] searchPaths;

    private List additionalPaths = new ArrayList();

    public GroovyClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public GroovyClassLoader(ClassLoader loader) {
        this(loader, new CompilerConfiguration());
    }

    public GroovyClassLoader(GroovyClassLoader parent) {
        this(parent, parent.config);
    }

    public GroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
        super(loader);
        this.config = config;
    }

    /**
     * Loads the given class node returning the implementation Class
     *
     * @param classNode
     * @return
     */
    public Class defineClass(ClassNode classNode, String file) {
        return defineClass(classNode, file, "/groovy/defineClass");
    }

    /**
     * Loads the given class node returning the implementation Class
     *
     * @param classNode
     * @return
     */
    public Class defineClass(ClassNode classNode, String file, String newCodeBase) {
        CodeSource codeSource = null;
        try {
            codeSource = new CodeSource(new URL("file", "", newCodeBase), (java.security.cert.Certificate[]) null);
        } catch (MalformedURLException e) {
            //swallow
        }

        //
        // BUG: Why is this passing getParent() as the ClassLoader???

        CompilationUnit unit = new CompilationUnit(config, codeSource, getParent());
        try {
            ClassCollector collector = createCollector(unit);

            unit.addClassNode(classNode);
            unit.setClassgenCallback(collector);
            unit.compile(Phases.CLASS_GENERATION);

            return collector.generatedClass;
        } catch (CompilationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the given file into a Java class capable of being run
     *
     * @param file the file name to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(File file) throws CompilationFailedException, IOException {
        return parseClass(new GroovyCodeSource(file));
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text     the text of the script/class to parse
     * @param fileName the file name to use as the name of the class
     * @return the main class defined in the given script
     */
    public Class parseClass(String text, String fileName) throws CompilationFailedException {
        return parseClass(new ByteArrayInputStream(text.getBytes()), fileName);
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text the text of the script/class to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(String text) throws CompilationFailedException {
        return parseClass(new ByteArrayInputStream(text.getBytes()), "script" + System.currentTimeMillis() + ".groovy");
    }

    /**
     * Parses the given character stream into a Java class capable of being run
     *
     * @param in an InputStream
     * @return the main class defined in the given script
     */
    public Class parseClass(InputStream in) throws CompilationFailedException {
        return parseClass(in, "script" + System.currentTimeMillis() + ".groovy");
    }

    public Class parseClass(final InputStream in, final String fileName) throws CompilationFailedException {
        //For generic input streams, provide a catch-all codebase of
        // GroovyScript
        //Security for these classes can be administered via policy grants with
        // a codebase
        //of file:groovy.script
        GroovyCodeSource gcs = (GroovyCodeSource) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyCodeSource(in, fileName, "/groovy/script");
            }
        });
        return parseClass(gcs);
    }


    public Class parseClass(GroovyCodeSource codeSource) throws CompilationFailedException {
        return parseClass(codeSource, true);
    }

    /**
     * Parses the given code source into a Java class capable of being run
     *
     * @return the main class defined in the given script
     */
    public Class parseClass(GroovyCodeSource codeSource, boolean shouldCache) throws CompilationFailedException {
        String name = codeSource.getName();
        Class answer = null;
        //ASTBuilder.resolveName can call this recursively -- for example when
        // resolving a Constructor
        //invocation for a class that is currently being compiled.
        synchronized (cache) {
            answer = (Class) cache.get(name);
            if (answer != null) {
                return (answer == PARSING.class ? null : answer);
            } else {
                cache.put(name, PARSING.class);
            }
        }
        //Was neither already loaded nor compiling, so compile and add to
        // cache.
        try {
            CompilationUnit unit = new CompilationUnit(config, codeSource.getCodeSource(), this);
            // try {
            ClassCollector collector = createCollector(unit);

            if (codeSource.getFile()==null) {
                unit.addSource(name, codeSource.getInputStream());
            } else {
                unit.addSource(codeSource.getFile());
            }
            unit.setClassgenCallback(collector);
            unit.compile(Phases.CLASS_GENERATION);

            answer = collector.generatedClass;
            // }
            // catch( CompilationFailedException e ) {
            //     throw new RuntimeException( e );
            // }
        } finally {
            synchronized (cache) {
                if (answer == null || !shouldCache) {
                    cache.remove(name);
                } else {
                    cache.put(name, answer);
                }
            }
            try {
                codeSource.getInputStream().close();
            } catch (IOException e) {
                throw new GroovyRuntimeException("unable to close stream",e);
            }
        }
        return answer;
    }

    /**
     * Using this classloader you can load groovy classes from the system
     * classpath as though they were already compiled. Note that .groovy classes
     * found with this mechanism need to conform to the standard java naming
     * convention - i.e. the public class inside the file must match the
     * filename and the file must be located in a directory structure that
     * matches the package structure.
     */
    protected Class findClass(final String name) throws ClassNotFoundException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String className = name.replace('/', '.');
            int i = className.lastIndexOf('.');
            if (i != -1) {
                sm.checkPackageDefinition(className.substring(0, i));
            }
        }
        try {
            return (Class) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException {
                    return findGroovyClass(name);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        }
    }

    protected Class findGroovyClass(String name) throws ClassNotFoundException {
        //Use a forward slash here for the path separator. It will work as a
        // separator
        //for the File class on all platforms, AND it is required as a jar file
        // entry separator.
        String filename = name.replace('.', '/') + ".groovy";
        String[] paths = getClassPath();
        // put the absolute classname in a File object so we can easily
        // pluck off the class name and the package path
        File classnameAsFile = new File(filename);
        // pluck off the classname without the package
        String classname = classnameAsFile.getName();
        String pkg = classnameAsFile.getParent();
        String pkgdir;
        for (int i = 0; i < paths.length; i++) {
            String pathName = paths[i];
            File path = new File(pathName);
            if (path.exists()) {
                if (path.isDirectory()) {
                    // patch to fix case preserving but case insensitive file
                    // systems (like macosx)
                    // JIRA issue 414
                    //
                    // first see if the file even exists, no matter what the
                    // case is
                    File nocasefile = new File(path, filename);
                    if (!nocasefile.exists())
                        continue;

                    // now we know the file is there is some form or another, so
                    // let's look up all the files to see if the one we're
                    // really
                    // looking for is there
                    if (pkg == null)
                        pkgdir = pathName;
                    else
                        pkgdir = pathName + "/" + pkg;
                    File pkgdirF = new File(pkgdir);
                    // make sure the resulting path is there and is a dir
                    if (pkgdirF.exists() && pkgdirF.isDirectory()) {
                        File files[] = pkgdirF.listFiles();
                        for (int j = 0; j < files.length; j++) {
                            // do the case sensitive comparison
                            if (files[j].getName().equals(classname)) {
                                try {
                                    return parseClass(files[j]);
                                } catch (CompilationFailedException e) {
                                    throw new ClassNotFoundException("Syntax error in groovy file: " + files[j].getAbsolutePath(), e);
                                } catch (IOException e) {
                                    throw new ClassNotFoundException("Error reading groovy file: " + files[j].getAbsolutePath(), e);
                                }
                            }
                        }
                    }
                } else {
                    try {
                        JarFile jarFile = new JarFile(path);
                        JarEntry entry = jarFile.getJarEntry(filename);
                        if (entry != null) {
                            byte[] bytes = extractBytes(jarFile, entry);
                            Certificate[] certs = entry.getCertificates();
                            try {
                                return parseClass(new GroovyCodeSource(new ByteArrayInputStream(bytes), filename, path, certs));
                            } catch (CompilationFailedException e1) {
                                throw new ClassNotFoundException("Syntax error in groovy file: " + filename, e1);
                            }
                        }

                    } catch (IOException e) {
                        // Bad jar in classpath, ignore
                    }
                }
            }
        }
        throw new ClassNotFoundException(name);
    }

    //Read the bytes from a non-null JarEntry. This is done here because the
    // entry must be read completely
    //in order to get verified certificates, which can only be obtained after a
    // full read.
    private byte[] extractBytes(JarFile jarFile, JarEntry entry) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        try {
            BufferedInputStream bis = new BufferedInputStream(jarFile.getInputStream(entry));
            while ((b = bis.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException ioe) {
            throw new GroovyRuntimeException("Could not read the jar bytes for " + entry.getName());
        }
        return baos.toByteArray();
    }

      /**
       * Workaround for Groovy-835
       */
      protected String[] getClassPath() {
        if (null == _searchPaths) {
          final String classpath;
          if(null != config && null != config.getClasspath()) {
            //there's probably a better way to do this knowing the internals of
            //Groovy, but it works for now
            final List paths = config.getClasspath();
            final StringBuffer sb = new StringBuffer();
            for(Iterator iter = paths.iterator(); iter.hasNext(); ) {
              sb.append(iter.next().toString());
              sb.append(File.pathSeparatorChar);
            }
            //remove extra path separator
            sb.deleteCharAt(sb.length()-1);
            classpath = sb.toString();
          } else {
            classpath = System.getProperty("java.class.path", ".");
          }
          final List pathList = new ArrayList();
          expandClassPath(pathList, null, classpath);
          _searchPaths = new String[pathList.size()];
          _searchPaths = (String[]) pathList.toArray(_searchPaths);
        }
        return _searchPaths;
      }

    /**
     * @param pathList
     * @param classpath
     */
    protected void expandClassPath(List pathList, String base, String classpath) {

        // checking against null prevents an NPE when recursevely expanding the
        // classpath
        // in case the classpath is malformed
        if (classpath != null) {

            // Sun's convention for the class-path attribute is to seperate each
            // entry with spaces
            // but some libraries don't respect that convention and add commas,
            // colons, semi-colons
            String[] paths = classpath.split("[\\ ,:;]");

            for (int i = 0; i < paths.length; i++) {
                if (paths.length > 0) {
                    File path = null;

                    if ("".equals(base)) {
                        path = new File(paths[i]);
                    } else {
                        path = new File(base, paths[i]);
                    }

                    if (path.exists()) {
                        if (!path.isDirectory()) {
                            try {
                                JarFile jar = new JarFile(path);
                                pathList.add(paths[i]);

                                Manifest manifest = jar.getManifest();
                                if (manifest != null) {
                                    Attributes classPathAttributes = manifest.getMainAttributes();
                                    String manifestClassPath = classPathAttributes.getValue("Class-Path");

                                    if (manifestClassPath != null)
                                        expandClassPath(pathList, paths[i], manifestClassPath);
                                }
                            } catch (IOException e) {
                                // Bad jar, ignore
                                continue;
                            }
                        } else {
                            pathList.add(paths[i]);
                        }
                    }
                }
            }
        }
    }

    /**
     * A helper method to allow bytecode to be loaded. spg changed name to
     * defineClass to make it more consistent with other ClassLoader methods
     */
    protected Class defineClass(String name, byte[] bytecode, ProtectionDomain domain) {
        return defineClass(name, bytecode, 0, bytecode.length, domain);
    }

    protected ClassCollector createCollector(CompilationUnit unit) {
        return new ClassCollector(this, unit);
    }

    public static class ClassCollector extends CompilationUnit.ClassgenCallback {
        private Class generatedClass;

        private GroovyClassLoader cl;

        private CompilationUnit unit;

        protected ClassCollector(GroovyClassLoader cl, CompilationUnit unit) {
            this.cl = cl;
            this.unit = unit;
        }

        protected Class onClassNode(ClassWriter classWriter, ClassNode classNode) {
            byte[] code = classWriter.toByteArray();

            Class theClass = cl.defineClass(classNode.getName(), code, 0, code.length, unit.getAST().getCodeSource());
            
            if (generatedClass == null) {
                generatedClass = theClass;
            }

            return theClass;
        }

        public void call(ClassVisitor classWriter, ClassNode classNode) {
            onClassNode((ClassWriter) classWriter, classNode);
        }
    }

    /**
     * open up the super class define that takes raw bytes
     *  
     */
    public Class defineClass(String name, byte[] b) {
        return super.defineClass(name, b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     *      Implemented here to check package access prior to returning an
     *      already loaded class. todo : br shall we search for the source
     *      groovy here to see if the soource file has been updated first?
     */
    protected synchronized Class loadClass(final String name, boolean resolve) throws ClassNotFoundException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String className = name.replace('/', '.');
            int i = className.lastIndexOf('.');
            if (i != -1) {
                sm.checkPackageAccess(className.substring(0, i));
            }
        }
        
        Class cls = null;
        ClassNotFoundException last = null;
        try {
            cls = super.loadClass(name, resolve);
    
            boolean recompile = false;
            if (getTimeStamp(cls) < Long.MAX_VALUE) {
                Class[] inters = cls.getInterfaces();                
                for (int i = 0; i < inters.length; i++) {
                    if (inters[i].getName().equals(GroovyObject.class.getName())) {
                        recompile=true;
                        break;
                    }
                }                
            }
            if (!recompile) return cls;
        } catch (ClassNotFoundException cnfe) {
            last = cnfe; 
        }
        
        // try groovy file
        try {
            File source = (File) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return getSourceFile(name);
                }
            });
            if (source != null) {
                if ((cls!=null && isSourceNewer(source, cls)) || (cls==null)) {
                    cls = parseClass(source);
                }
            }
        } catch (Exception e) {
            synchronized (cache) {
                cache.put(name, NOT_RESOLVED.class);
            }
            throw new ClassNotFoundException("Failed to parse groovy file: " + name, e);
        }
        if (cls==null) {
            if (last==null) throw new AssertionError(true);
            throw last;            
        }        
        return cls;
    }

    private long getTimeStamp(Class cls) {
        Field field;
        Long o;
        try {
            field = cls.getField(Verifier.__TIMESTAMP);
            o = (Long) field.get(null);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            return Long.MAX_VALUE;
        }
        return o.longValue();
    }

    //    static class ClassWithTimeTag {
    //        final static ClassWithTimeTag NOT_RESOLVED = new ClassWithTimeTag(null,
    // 0);
    //        Class cls;
    //        long lastModified;
    //
    //        public ClassWithTimeTag(Class cls, long lastModified) {
    //            this.cls = cls;
    //            this.lastModified = lastModified;
    //        }
    //    }

    private File getSourceFile(String name) {
        File source = null;
        String filename = name.replace('.', '/') + ".groovy";
        String[] paths = getClassPath();
        for (int i = 0; i < paths.length; i++) {
            String pathName = paths[i];
            File path = new File(pathName);
            if (path.exists()) { // case sensitivity depending on OS!
                if (path.isDirectory()) {
                    File file = new File(path, filename);
                    if (file.exists()) {
                        // file.exists() might be case insensitive. Let's do
                        // case sensitive match for the filename
                        boolean fileExists = false;
                        int sepp = filename.lastIndexOf('/');
                        String fn = filename;
                        if (sepp >= 0) {
                            fn = filename.substring(++sepp);
                        }
                        File parent = file.getParentFile();
                        String[] files = parent.list();
                        for (int j = 0; j < files.length; j++) {
                            if (files[j].equals(fn)) {
                                fileExists = true;
                                break;
                            }
                        }

                        if (fileExists) {
                            source = file;
                            break;
                        }
                    }
                }
            }
        }
        return source;
    }

    private boolean isSourceNewer(File source, Class cls) {
        return source.lastModified() > getTimeStamp(cls);
    }

    public void addClasspath(String path) {
        additionalPaths.add(path);
        searchPaths = null;
    }
}
