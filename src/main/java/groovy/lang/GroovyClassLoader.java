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
/*
 * @todo multi threaded compiling of the same class but with different roots
 * for compilation... T1 compiles A, which uses B, T2 compiles B... mark A and B
 * as parsed and then synchronize compilation. Problems: How to synchronize?
 * How to get error messages?
 *
 */
package groovy.lang;

import groovy.util.CharsetToolkit;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.BytecodeProcessor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.memoize.EvictableCache;
import org.codehaus.groovy.runtime.memoize.StampedCommonCache;
import org.codehaus.groovy.runtime.memoize.UnlimitedConcurrentCache;
import org.codehaus.groovy.util.URLStreams;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * A ClassLoader which can load Groovy classes. The loaded classes are cached,
 * classes from other classloaders should not be cached. To be able to load a
 * script that was asked for earlier but was created later it is essential not
 * to keep anything like a "class not found" information for that class name.
 * This includes possible parent loaders. Classes that are not cached are always
 * reloaded.
 */
public class GroovyClassLoader extends URLClassLoader {
    private static final URL[] EMPTY_URL_ARRAY = new URL[0];
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * this cache contains the loaded classes or PARSING, if the class is currently parsed
     */
    protected final EvictableCache<String, Class> classCache = new UnlimitedConcurrentCache<String, Class>();

    /**
     * This cache contains mappings of file name to class. It is used
     * to bypass compilation.
     */
    protected final StampedCommonCache<String, Class> sourceCache = new StampedCommonCache<String, Class>();

    private final CompilerConfiguration config;
    private String sourceEncoding;
    private Boolean recompile;
    // use 1000000 as offset to avoid conflicts with names from the GroovyShell
    private static int scriptNameCounter = 1000000;

    private GroovyResourceLoader resourceLoader = new GroovyResourceLoader() {
        public URL loadGroovySource(final String filename) throws MalformedURLException {
            return AccessController.doPrivileged((PrivilegedAction<URL>) () -> {
                for (String extension : config.getScriptExtensions()) {
                    try {
                        URL ret = getSourceFile(filename, extension);
                        if (ret != null)
                            return ret;
                    } catch (Throwable t) {
                    }
                }
                return null;
            });
        }
    };

    /**
     * creates a GroovyClassLoader using the current Thread's context
     * Class loader as parent.
     */
    public GroovyClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * creates a GroovyClassLoader using the given ClassLoader as parent
     */
    public GroovyClassLoader(ClassLoader loader) {
        this(loader, null);
    }

    /**
     * creates a GroovyClassLoader using the given GroovyClassLoader as parent.
     * This loader will get the parent's CompilerConfiguration
     */
    public GroovyClassLoader(GroovyClassLoader parent) {
        this(parent, parent.config, false);
    }

    /**
     * creates a GroovyClassLoader.
     *
     * @param parent                    the parent class loader
     * @param config                    the compiler configuration
     * @param useConfigurationClasspath determines if the configurations classpath should be added
     */
    public GroovyClassLoader(ClassLoader parent, CompilerConfiguration config, boolean useConfigurationClasspath) {
        super(EMPTY_URL_ARRAY, parent);
        if (config == null) config = CompilerConfiguration.DEFAULT;
        this.config = config;
        if (useConfigurationClasspath) {
            for (String path : config.getClasspath()) {
                this.addClasspath(path);
            }
        }

        initSourceEncoding(config);
    }

    private void initSourceEncoding(CompilerConfiguration config) {
        sourceEncoding = config.getSourceEncoding();
        if (null ==  sourceEncoding) {
            // Keep the same default source encoding with the one used by #parseClass(InputStream, String)
            // TODO should we use org.codehaus.groovy.control.CompilerConfiguration.DEFAULT_SOURCE_ENCODING instead?
            sourceEncoding = CharsetToolkit.getDefaultSystemCharset().name();
        }
    }

    /**
     * creates a GroovyClassLoader using the given ClassLoader as parent.
     */
    public GroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
        this(loader, config, true);
    }

    public void setResourceLoader(GroovyResourceLoader resourceLoader) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("Resource loader must not be null!");
        }
        this.resourceLoader = resourceLoader;
    }

    public GroovyResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Loads the given class node returning the implementation Class.
     * <p>
     * WARNING: this compilation is not synchronized
     *
     * @param classNode
     * @return a class
     */
    public Class defineClass(ClassNode classNode, String file, String newCodeBase) {
        CodeSource codeSource = null;
        try {
            codeSource = new CodeSource(new URL("file", "", newCodeBase), (java.security.cert.Certificate[]) null);
        } catch (MalformedURLException e) {
            //swallow
        }

        CompilationUnit unit = createCompilationUnit(config, codeSource);
        ClassCollector collector = createCollector(unit, classNode.getModule().getContext());
        try {
            unit.addClassNode(classNode);
            unit.setClassgenCallback(collector);
            unit.compile(Phases.CLASS_GENERATION);
            definePackageInternal(collector.generatedClass.getName());
            return collector.generatedClass;
        } catch (CompilationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if this class loader has compatible {@link CompilerConfiguration}
     * with the provided one.
     * @param config the compiler configuration to test for compatibility
     * @return {@code true} if the provided config is exactly the same instance
     * of {@link CompilerConfiguration} as this loader has
     */
    public boolean hasCompatibleConfiguration(CompilerConfiguration config) {
      return this.config == config;
    }

    /**
     * Parses the given file into a Java class capable of being run
     *
     * @param file the file name to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(File file) throws CompilationFailedException, IOException {
        return parseClass(new GroovyCodeSource(file, config.getSourceEncoding()));
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text     the text of the script/class to parse
     * @param fileName the file name to use as the name of the class
     * @return the main class defined in the given script
     */
    public Class parseClass(final String text, final String fileName) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () -> new GroovyCodeSource(text, fileName, "/groovy/script"));
        gcs.setCachable(false);
        return parseClass(gcs);
    }

    /**
     * Parses the given text into a Java class capable of being run
     *
     * @param text the text of the script/class to parse
     * @return the main class defined in the given script
     */
    public Class parseClass(String text) throws CompilationFailedException {
        try {
            return parseClass(text, "Script_" + EncodingGroovyMethods.md5(text) + ".groovy");
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyBugError("Failed to generate md5", e); // should never happen
        }
    }

    public synchronized String generateScriptName() {
        scriptNameCounter++;
        return "script" + scriptNameCounter + ".groovy";
    }

    public Class parseClass(final Reader reader, final String fileName) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () -> {
            try {
                String scriptText = IOGroovyMethods.getText(reader);
                return new GroovyCodeSource(scriptText, fileName, "/groovy/script");
            } catch (IOException e) {
                throw new RuntimeException("Impossible to read the content of the reader for file named: " + fileName, e);
            }
        });
        return parseClass(gcs);
    }

    public Class parseClass(GroovyCodeSource codeSource) throws CompilationFailedException {
        return parseClass(codeSource, codeSource.isCachable());
    }

    /**
     * Parses the given code source into a Java class. If there is a class file
     * for the given code source, then no parsing is done, instead the cached class is returned.
     *
     * @param shouldCacheSource if true then the generated class will be stored in the source cache
     * @return the main class defined in the given script
     */
    public Class parseClass(final GroovyCodeSource codeSource, boolean shouldCacheSource) throws CompilationFailedException {
        // it's better to cache class instances by the source code
        // GCL will load the unique class instance for the same source code
        // and avoid occupying Permanent Area/Metaspace repeatedly
        String cacheKey = genSourceCacheKey(codeSource);

        return sourceCache.getAndPut(
                cacheKey,
                key -> doParseClass(codeSource),
                shouldCacheSource
        );
    }

    private String genSourceCacheKey(GroovyCodeSource codeSource) {
        StringBuilder strToDigest;

        String scriptText = codeSource.getScriptText();
        if (null != scriptText) {
            strToDigest = new StringBuilder((int) (scriptText.length() * 1.2));
            strToDigest.append("scriptText:").append(scriptText);

            CodeSource cs = codeSource.getCodeSource();
            if (null != cs) {
                strToDigest.append("/codeSource:").append(cs);
            }
        } else {
            strToDigest = new StringBuilder(32);
            // if the script text is null, i.e. the script content is invalid
            // use the name as cache key for the time being to trigger the validation by `groovy.lang.GroovyClassLoader.validate`
            // note: the script will not be cached due to the invalid script content,
            //       so it does not matter even if cache key is not the md5 value of script content
            strToDigest.append("name:").append(codeSource.getName());
        }

        try {
            return EncodingGroovyMethods.md5(strToDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyRuntimeException(e); // should never reach here!
        }
    }

    private Class doParseClass(GroovyCodeSource codeSource) {
        validate(codeSource);
        Class answer;  // Was neither already loaded nor compiling, so compile and add to cache.
        CompilationUnit unit = createCompilationUnit(config, codeSource.getCodeSource());
        if (recompile!=null && recompile || recompile==null && config.getRecompileGroovySource()) {
            unit.addFirstPhaseOperation(TimestampAdder.INSTANCE, CompilePhase.CLASS_GENERATION.getPhaseNumber());
        }
        SourceUnit su = null;
        File file = codeSource.getFile();
        if (file != null) {
            su = unit.addSource(file);
        } else {
            URL url = codeSource.getURL();
            if (url != null) {
                su = unit.addSource(url);
            } else {
                su = unit.addSource(codeSource.getName(), codeSource.getScriptText());
            }
        }

        ClassCollector collector = createCollector(unit, su);
        unit.setClassgenCallback(collector);
        int goalPhase = Phases.CLASS_GENERATION;
        if (config != null && config.getTargetDirectory() != null) goalPhase = Phases.OUTPUT;
        unit.compile(goalPhase);

        answer = collector.generatedClass;
        String mainClass = su.getAST().getMainClassName();
        for (Object o : collector.getLoadedClasses()) {
            Class clazz = (Class) o;
            String clazzName = clazz.getName();
            definePackageInternal(clazzName);
            setClassCacheEntry(clazz);
            if (clazzName.equals(mainClass)) answer = clazz;
        }
        return answer;
    }

    private static void validate(GroovyCodeSource codeSource) {
        if (codeSource.getFile() == null) {
            if (codeSource.getScriptText() == null) {
                throw new IllegalArgumentException("Script text to compile cannot be null!");
            }
        }
    }

    private void definePackageInternal(String className) {
        int i = className.lastIndexOf('.');
        if (i != -1) {
            String pkgName = className.substring(0, i);
            java.lang.Package pkg = getPackage(pkgName);
            if (pkg == null) {
                definePackage(pkgName, null, null, null, null, null, null, null);
            }
        }
    }

    /**
     * gets the currently used classpath.
     *
     * @return a String[] containing the file information of the urls
     * @see #getURLs()
     */
    protected String[] getClassPath() {
        //workaround for Groovy-835
        URL[] urls = getURLs();
        String[] ret = new String[urls.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = urls[i].getFile();
        }
        return ret;
    }

    protected PermissionCollection getPermissions(CodeSource codeSource) {
        PermissionCollection perms;
        try {
            try {
                perms = super.getPermissions(codeSource);
            } catch (SecurityException e) {
                // We lied about our CodeSource and that makes URLClassLoader unhappy.
                perms = new Permissions();
            }

            ProtectionDomain myDomain = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
                public ProtectionDomain run() {
                    return getClass().getProtectionDomain();
                }
            });
            PermissionCollection myPerms = myDomain.getPermissions();
            if (myPerms != null) {
                for (Enumeration<Permission> elements = myPerms.elements(); elements.hasMoreElements();) {
                    perms.add(elements.nextElement());
                }
            }
        } catch (Throwable e) {
            // We lied about our CodeSource and that makes URLClassLoader unhappy.
            perms = new Permissions();
        }
        perms.setReadOnly();
        return perms;
    }

    public static class InnerLoader extends GroovyClassLoader {
        private final GroovyClassLoader delegate;
        private final long timeStamp;

        public InnerLoader(GroovyClassLoader delegate) {
            super(delegate);
            this.delegate = delegate;
            timeStamp = System.currentTimeMillis();
        }

        @Override
        public void addClasspath(String path) {
            delegate.addClasspath(path);
        }

        @Override
        public void clearCache() {
            delegate.clearCache();
        }

        @Override
        public URL findResource(String name) {
            return delegate.findResource(name);
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            return delegate.findResources(name);
        }

        @Override
        public Class[] getLoadedClasses() {
            return delegate.getLoadedClasses();
        }

        @Override
        public URL getResource(String name) {
            return delegate.getResource(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

        @Override
        public GroovyResourceLoader getResourceLoader() {
            return delegate.getResourceLoader();
        }

        @Override
        public URL[] getURLs() {
            return delegate.getURLs();
        }

        @Override
        public Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve) throws ClassNotFoundException, CompilationFailedException {
            Class c = findLoadedClass(name);
            if (c != null) return c;
            return delegate.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
        }

        @Override
        public Class parseClass(GroovyCodeSource codeSource, boolean shouldCache) throws CompilationFailedException {
            return delegate.parseClass(codeSource, shouldCache);
        }

        @Override
        public void setResourceLoader(GroovyResourceLoader resourceLoader) {
            delegate.setResourceLoader(resourceLoader);
        }

        @Override
        public void addURL(URL url) {
            delegate.addURL(url);
        }

        @Override
        public Class defineClass(ClassNode classNode, String file, String newCodeBase) {
            return delegate.defineClass(classNode, file, newCodeBase);
        }

        @Override
        public Class parseClass(File file) throws CompilationFailedException, IOException {
            return delegate.parseClass(file);
        }

        @Override
        public Class parseClass(String text, String fileName) throws CompilationFailedException {
            return delegate.parseClass(text, fileName);
        }

        @Override
        public Class parseClass(String text) throws CompilationFailedException {
            return delegate.parseClass(text);
        }

        @Override
        public String generateScriptName() {
            return delegate.generateScriptName();
        }

        @Override
        public Class parseClass(Reader reader, String fileName) throws CompilationFailedException {
            return delegate.parseClass(reader, fileName);
        }

        @Override
        public Class parseClass(GroovyCodeSource codeSource) throws CompilationFailedException {
            return delegate.parseClass(codeSource);
        }

        @Override
        public Class defineClass(String name, byte[] b) {
            return delegate.defineClass(name, b);
        }

        @Override
        public Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript) throws ClassNotFoundException, CompilationFailedException {
            return delegate.loadClass(name, lookupScriptFiles, preferClassOverScript);
        }

        @Override
        public void setShouldRecompile(Boolean mode) {
            delegate.setShouldRecompile(mode);
        }

        @Override
        public Boolean isShouldRecompile() {
            return delegate.isShouldRecompile();
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return delegate.getResources(name);
        }

        @Override
        public void setDefaultAssertionStatus(boolean enabled) {
            delegate.setDefaultAssertionStatus(enabled);
        }

        @Override
        public void setPackageAssertionStatus(String packageName, boolean enabled) {
            delegate.setPackageAssertionStatus(packageName, enabled);
        }

        @Override
        public void setClassAssertionStatus(String className, boolean enabled) {
            delegate.setClassAssertionStatus(className, enabled);
        }

        @Override
        public void clearAssertionStatus() {
            delegate.clearAssertionStatus();
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                delegate.close();
            }
        }

        public long getTimeStamp() {
            return timeStamp;
        }
    }

    /**
     * creates a new CompilationUnit. If you want to add additional
     * phase operations to the CompilationUnit (for example to inject
     * additional methods, variables, fields), then you should overwrite
     * this method.
     *
     * @param config the compiler configuration, usually the same as for this class loader
     * @param source the source containing the initial file to compile, more files may follow during compilation
     * @return the CompilationUnit
     */
    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
        return new CompilationUnit(config, source, this);
    }

    /**
     * creates a ClassCollector for a new compilation.
     *
     * @param unit the compilationUnit
     * @param su   the SourceUnit
     * @return the ClassCollector
     */
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        InnerLoader loader = AccessController.doPrivileged((PrivilegedAction<InnerLoader>) () -> new InnerLoader(GroovyClassLoader.this));
        return new ClassCollector(loader, unit, su);
    }

    public static class ClassCollector implements CompilationUnit.ClassgenCallback {
        private Class generatedClass;
        private final GroovyClassLoader cl;
        private final SourceUnit su;
        private final CompilationUnit unit;
        private final Collection<Class> loadedClasses;

        protected ClassCollector(InnerLoader cl, CompilationUnit unit, SourceUnit su) {
            this.cl = cl;
            this.unit = unit;
            this.loadedClasses = new ArrayList<Class>();
            this.su = su;
        }

        public GroovyClassLoader getDefiningClassLoader() {
            return cl;
        }

        protected Class createClass(byte[] code, ClassNode classNode) {
            BytecodeProcessor bytecodePostprocessor = unit.getConfiguration().getBytecodePostprocessor();
            byte[] fcode = code;
            if (bytecodePostprocessor!=null) {
                fcode = bytecodePostprocessor.processBytecode(classNode.getName(), fcode);
            }
            GroovyClassLoader cl = getDefiningClassLoader();
            Class theClass = cl.defineClass(classNode.getName(), fcode, 0, fcode.length, unit.getAST().getCodeSource());
            this.loadedClasses.add(theClass);

            if (generatedClass == null) {
                ModuleNode mn = classNode.getModule();
                SourceUnit msu = null;
                if (mn != null) msu = mn.getContext();
                ClassNode main = null;
                if (mn != null) main = mn.getClasses().get(0);
                if (msu == su && main == classNode) generatedClass = theClass;
            }

            return theClass;
        }

        protected Class onClassNode(ClassWriter classWriter, ClassNode classNode) {
            byte[] code = classWriter.toByteArray();
            return createClass(code, classNode);
        }

        public void call(ClassVisitor classWriter, ClassNode classNode) {
            onClassNode((ClassWriter) classWriter, classNode);
        }

        public Collection getLoadedClasses() {
            return this.loadedClasses;
        }
    }

    /**
     * open up the super class define that takes raw bytes
     */
    public Class defineClass(String name, byte[] b) {
        return super.defineClass(name, b, 0, b.length);
    }

    /**
     * loads a class from a file or a parent classloader.
     * This method does call loadClass(String, boolean, boolean, boolean)
     * with the last parameter set to false.
     *
     * @throws CompilationFailedException if compilation was not successful
     */
    public Class loadClass(final String name, boolean lookupScriptFiles, boolean preferClassOverScript)
            throws ClassNotFoundException, CompilationFailedException {
        return loadClass(name, lookupScriptFiles, preferClassOverScript, false);
    }

    /**
     * gets a class from the class cache. This cache contains only classes loaded through
     * this class loader or an InnerLoader instance. If no class is stored for a
     * specific name, then the method should return null.
     *
     * @param name of the class
     * @return the class stored for the given name
     * @see #removeClassCacheEntry(String)
     * @see #setClassCacheEntry(Class)
     * @see #clearCache()
     */
    protected Class getClassCacheEntry(String name) {
        return classCache.get(name);
    }

    /**
     * sets an entry in the class cache.
     *
     * @param cls the class
     * @see #removeClassCacheEntry(String)
     * @see #getClassCacheEntry(String)
     * @see #clearCache()
     */
    protected void setClassCacheEntry(Class cls) {
        classCache.put(cls.getName(), cls);
    }

    /**
     * removes a class from the class cache.
     *
     * @param name of the class
     * @see #getClassCacheEntry(String)
     * @see #setClassCacheEntry(Class)
     * @see #clearCache()
     */
    protected void removeClassCacheEntry(String name) {
        classCache.remove(name);
    }

    /**
     * adds a URL to the classloader.
     *
     * @param url the new classpath element
     */
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * Indicates if a class is recompilable. Recompilable means, that the classloader
     * will try to locate a groovy source file for this class and then compile it again,
     * adding the resulting class as entry to the cache. Giving null as class is like a
     * recompilation, so the method should always return true here. Only classes that are
     * implementing GroovyObject are compilable and only if the timestamp in the class
     * is lower than Long.MAX_VALUE.
     * <p>
     * NOTE: First the parent loaders will be asked and only if they don't return a
     * class the recompilation will happen. Recompilation also only happen if the source
     * file is newer.
     *
     * @param cls the class to be tested. If null the method should return true
     * @return true if the class should be compiled again
     * @see #isSourceNewer(URL, Class)
     */
    protected boolean isRecompilable(Class cls) {
        if (cls == null) return true;
        if (cls.getClassLoader() == this) return false;
        if (recompile == null && !config.getRecompileGroovySource()) return false;
        if (recompile != null && !recompile) return false;
        if (!GroovyObject.class.isAssignableFrom(cls)) return false;
        long timestamp = getTimeStamp(cls);
        return timestamp != Long.MAX_VALUE;
    }

    /**
     * sets if the recompilation should be enable. There are 3 possible
     * values for this. Any value different than null overrides the
     * value from the compiler configuration. true means to recompile if needed
     * false means to never recompile.
     *
     * @param mode the recompilation mode
     * @see CompilerConfiguration
     */
    public void setShouldRecompile(Boolean mode) {
        recompile = mode;
    }

    /**
     * gets the currently set recompilation mode. null means, the
     * compiler configuration is used. False means no recompilation and
     * true means that recompilation will be done if needed.
     *
     * @return the recompilation mode
     */
    public Boolean isShouldRecompile() {
        return recompile;
    }

    /**
     * loads a class from a file or a parent classloader.
     *
     * @param name                  of the class to be loaded
     * @param lookupScriptFiles     if false no lookup at files is done at all
     * @param preferClassOverScript if true the file lookup is only done if there is no class
     * @param resolve               see {@link java.lang.ClassLoader#loadClass(java.lang.String, boolean)}
     * @return the class found or the class created from a file lookup
     * @throws ClassNotFoundException     if the class could not be found
     * @throws CompilationFailedException if the source file could not be compiled
     */
    public Class loadClass(final String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
            throws ClassNotFoundException, CompilationFailedException {
        // look into cache
        Class cls = getClassCacheEntry(name);

        // enable recompilation?
        boolean recompile = isRecompilable(cls);
        if (!recompile) return cls;

        // try parent loader
        ClassNotFoundException last = null;
        try {
            Class parentClassLoaderClass = super.loadClass(name, resolve);
            // always return if the parent loader was successful
            if (cls != parentClassLoaderClass) return parentClassLoaderClass;
        } catch (ClassNotFoundException cnfe) {
            last = cnfe;
        } catch (NoClassDefFoundError ncdfe) {
            if (ncdfe.getMessage().indexOf("wrong name") > 0) {
                last = new ClassNotFoundException(name);
            } else {
                throw ncdfe;
            }
        }

        // check security manager
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String className = name.replace('/', '.');
            int i = className.lastIndexOf('.');
            // no checks on the sun.reflect classes for reflection speed-up
            // in particular ConstructorAccessorImpl, MethodAccessorImpl, FieldAccessorImpl and SerializationConstructorAccessorImpl
            // which are generated at runtime by the JDK
            if (i != -1 && !className.startsWith("sun.reflect.")) {
                sm.checkPackageAccess(className.substring(0, i));
            }
        }

        // prefer class if no recompilation
        if (cls != null && preferClassOverScript) return cls;

        // at this point the loading from a parent loader failed
        // and we want to recompile if needed.
        if (lookupScriptFiles) {
            // try groovy file
            try {
                // check if recompilation already happened.
                final Class classCacheEntry = getClassCacheEntry(name);
                if (classCacheEntry != cls) return classCacheEntry;
                URL source = resourceLoader.loadGroovySource(name);
                // if recompilation fails, we want cls==null
                Class oldClass = cls;
                cls = null;
                cls = recompile(source, name, oldClass);
            } catch (IOException ioe) {
                last = new ClassNotFoundException("IOException while opening groovy source: " + name, ioe);
            } finally {
                if (cls == null) {
                    removeClassCacheEntry(name);
                } else {
                    setClassCacheEntry(cls);
                }
            }
        }

        if (cls == null) {
            // no class found, there should have been an exception before now
            if (last == null) throw new AssertionError(true);
            throw last;
        }
        return cls;
    }

    /**
     * (Re)Compiles the given source.
     * This method starts the compilation of a given source, if
     * the source has changed since the class was created. For
     * this isSourceNewer is called.
     *
     * @param source    the source pointer for the compilation
     * @param className the name of the class to be generated
     * @param oldClass  a possible former class
     * @return the old class if the source wasn't new enough, the new class else
     * @throws CompilationFailedException if the compilation failed
     * @throws IOException                if the source is not readable
     * @see #isSourceNewer(URL, Class)
     */
    protected Class recompile(URL source, String className, Class oldClass) throws CompilationFailedException, IOException {
        if (source != null) {
            // found a source, compile it if newer
            if (oldClass == null || isSourceNewer(source, oldClass)) {
                String name = source.toExternalForm();

                sourceCache.remove(name);

                if (isFile(source)) {
                    try {
                        return parseClass(new GroovyCodeSource(new File(source.toURI()), sourceEncoding));
                    } catch (URISyntaxException e) {
                        // do nothing and fall back to the other version
                    }
                }
                return parseClass(new InputStreamReader(URLStreams.openUncachedStream(source), sourceEncoding), name);
            }
        }
        return oldClass;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Implemented here to check package access prior to returning an
     * already loaded class.
     *
     * @throws CompilationFailedException if the compilation failed
     * @throws ClassNotFoundException     if the class was not found
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    protected Class loadClass(final String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name, true, true, resolve);
    }

    /**
     * gets the time stamp of a given class. For groovy
     * generated classes this usually means to return the value
     * of the static field __timeStamp. If the parameter doesn't
     * have such a field, then Long.MAX_VALUE is returned
     *
     * @param cls the class
     * @return the time stamp
     */
    protected long getTimeStamp(Class cls) {
        return Verifier.getTimestamp(cls);
    }

    /**
     * This method will take a file name and try to "decode" any URL encoded characters.  For example
     * if the file name contains any spaces this method call will take the resulting %20 encoded values
     * and convert them to spaces.
     * <p>
     * This method was added specifically to fix defect:  Groovy-1787.  The defect involved a situation
     * where two scripts were sitting in a directory with spaces in its name.  The code would fail
     * when the class loader tried to resolve the file name and would choke on the URLEncoded space values.
     */
    private static String decodeFileName(String fileName) {
        String decodedFile = fileName;
        try {
            decodedFile = URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Encountered an invalid encoding scheme when trying to use URLDecoder.decode() inside of the GroovyClassLoader.decodeFileName() method.  Returning the unencoded URL.");
            System.err.println("Please note that if you encounter this error and you have spaces in your directory you will run into issues.  Refer to GROOVY-1787 for description of this bug.");
        }

        return decodedFile;
    }

    private static boolean isFile(URL ret) {
        return ret != null && ret.getProtocol().equals("file");
    }

    private static File getFileForUrl(URL ret, String filename) {
        String fileWithoutPackage = filename;
        if (fileWithoutPackage.indexOf('/') != -1) {
            int index = fileWithoutPackage.lastIndexOf('/');
            fileWithoutPackage = fileWithoutPackage.substring(index + 1);
        }
        return fileReallyExists(ret, fileWithoutPackage);
    }

    private static File fileReallyExists(URL ret, String fileWithoutPackage) {
        File path;
        try {
            /* fix for GROOVY-5809 */
            path = new File(ret.toURI());
        } catch(URISyntaxException e) {
            path = new File(decodeFileName(ret.getFile()));
        }
        path = path.getParentFile();
        if (path.exists() && path.isDirectory()) {
            File file = new File(path, fileWithoutPackage);
            if (file.exists()) {
                // file.exists() might be case insensitive. Let's do
                // case sensitive match for the filename
                File parent = file.getParentFile();
                for (String child : parent.list()) {
                    if (child.equals(fileWithoutPackage)) return file;
                }
            }
        }
        //file does not exist!
        return null;
    }

    private URL getSourceFile(String name, String extension) {
        String filename = name.replace('.', '/') + "." + extension;
        URL ret = getResource(filename);
        if (isFile(ret) && getFileForUrl(ret, filename) == null) return null;
        return ret;
    }

    /**
     * Decides if the given source is newer than a class.
     *
     * @param source the source we may want to compile
     * @param cls    the former class
     * @return true if the source is newer, false else
     * @throws IOException if it is not possible to open an
     *                     connection for the given source
     * @see #getTimeStamp(Class)
     */
    protected boolean isSourceNewer(URL source, Class cls) throws IOException {
        long lastMod;

        // Special handling for file:// protocol, as getLastModified() often reports
        // incorrect results (-1)
        if (isFile(source)) {
            // Coerce the file URL to a File
            // See ClassNodeResolver.isSourceNewer for another method that replaces '|' with ':'.
            // WTF: Why is this done and where is it documented?
            String path = source.getPath().replace('/', File.separatorChar).replace('|', ':');
            File file = new File(path);
            lastMod = file.lastModified();
        } else {
            URLConnection conn = source.openConnection();
            lastMod = conn.getLastModified();
            conn.getInputStream().close();
        }
        long classTime = getTimeStamp(cls);
        return classTime + config.getMinimumRecompilationInterval() < lastMod;
    }

    /**
     * adds a classpath to this classloader.
     *
     * @param path is a jar file or a directory.
     * @see #addURL(URL)
     */
    public void addClasspath(final String path) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {

            URI newURI;
            try {
                newURI = new URI(path);
                // check if we can create a URL from that URI
                newURI.toURL();
            } catch (URISyntaxException | IllegalArgumentException | MalformedURLException e) {
                // the URI has a false format, so lets try it with files ...
                newURI=new File(path).toURI();
            }

            URL[] urls = getURLs();
            for (URL url : urls) {
                // Do not use URL.equals.  It uses the network to resolve names and compares ip addresses!
                // That is a violation of RFC and just plain evil.
                // http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
                // http://docs.oracle.com/javase/7/docs/api/java/net/URL.html#equals(java.lang.Object)
                // "Since hosts comparison requires name resolution, this operation is a blocking operation.
                // Note: The defined behavior for equals is known to be inconsistent with virtual hosting in HTTP."
                try {
                    if (newURI.equals(url.toURI())) return null;
                } catch (URISyntaxException e) {
                    // fail fast! if we got a malformed URI the Classloader has to tell it
                    throw new RuntimeException( e );
                }
            }
            try {
                addURL(newURI.toURL());
            } catch (MalformedURLException e) {
                // fail fast! if we got a malformed URL the Classloader has to tell it
                throw new RuntimeException( e );
            }
            return null;
        });
    }

    /**
     * <p>Returns all Groovy classes loaded by this class loader.
     *
     * @return all classes loaded by this class loader
     */
    public Class[] getLoadedClasses() {
        return classCache.values().toArray(EMPTY_CLASS_ARRAY);
    }

    /**
     * Removes all classes from the class cache.
     * <p>
     * In addition to internal caches this method also clears any
     * previously set MetaClass information for the given set of
     * classes being removed.
     *
     * @see #getClassCacheEntry(String)
     * @see #setClassCacheEntry(Class)
     * @see #removeClassCacheEntry(String)
     */
    public void clearCache() {
        Map<String, Class> clearedClasses = classCache.clearAll();

        sourceCache.clear();

        for (Map.Entry<String, Class> entry : clearedClasses.entrySet()) {
            // Another Thread may be using an instance of this class
            // (for the first time) requiring a ClassInfo lock and
            // classloading which would require a lock on classCache.
            // The following locks on ClassInfo and to avoid deadlock
            // should not be done with a classCache lock.
            InvokerHelper.removeClass(entry.getValue());
        }
    }

    /**
     * Closes this GroovyClassLoader and clears any caches it maintains.
     * <p>
     * No use should be made of this instance after this method is
     * invoked. Any classes that are already loaded are still accessible.
     *
     * @throws IOException
     * @see URLClassLoader#close()
     * @see #clearCache()
     * @since 2.5.0
     */
    @Override
    public void close() throws IOException {
        super.close();
        clearCache();
    }

    private static class TimestampAdder implements CompilationUnit.IPrimaryClassNodeOperation, Opcodes {
        private static final TimestampAdder INSTANCE = new TimestampAdder();

        private TimestampAdder() {}

        protected void addTimeStamp(ClassNode node) {
            if (node.getDeclaredField(Verifier.__TIMESTAMP) == null) { // in case if verifier visited the call already
                FieldNode timeTagField = new FieldNode(
                        Verifier.__TIMESTAMP,
                        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                        ClassHelper.long_TYPE,
                        //"",
                        node,
                        new ConstantExpression(System.currentTimeMillis()));
                // alternatively, FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
                timeTagField.setSynthetic(true);
                node.addField(timeTagField);

                timeTagField = new FieldNode(
                        Verifier.__TIMESTAMP__ + System.currentTimeMillis(),
                        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                        ClassHelper.long_TYPE,
                        //"",
                        node,
                        new ConstantExpression((long) 0));
                // alternatively, FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
                timeTagField.setSynthetic(true);
                node.addField(timeTagField);
            }
        }

        @Override
        public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
            if ((classNode.getModifiers() & Opcodes.ACC_INTERFACE) > 0) {
                // does not apply on interfaces
                return;
            }
            if (!(classNode instanceof InnerClassNode)) {
                addTimeStamp(classNode);
            }
        }
    }
}
