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
package groovy.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyResourceLoader;
import groovy.lang.Script;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.gse.DependencyTracker;
import org.codehaus.groovy.tools.gse.StringSetMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Specific script engine able to reload modified scripts as well as dealing properly
 * with dependent scripts.
 */
public class GroovyScriptEngine implements ResourceConnector {
    private static final ClassLoader CL_STUB = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> new ClassLoader() {});

    private static final URL[] EMPTY_URL_ARRAY = new URL[0];

    private static class LocalData {
        CompilationUnit cu;
        final StringSetMap dependencyCache = new StringSetMap();
        final Map<String, String> precompiledEntries = new HashMap<String, String>();
    }

    private static WeakReference<ThreadLocal<LocalData>> localData = new WeakReference<ThreadLocal<LocalData>>(null);

    private static synchronized ThreadLocal<LocalData> getLocalData() {
        ThreadLocal<LocalData> local = localData.get();
        if (local != null) return local;
        local = new ThreadLocal<LocalData>();
        localData = new WeakReference<ThreadLocal<LocalData>>(local);
        return local;
    }

    private final URL[] roots;
    private final ResourceConnector rc;
    private final ClassLoader parentLoader;
    private GroovyClassLoader groovyLoader;
    private final Map<String, ScriptCacheEntry> scriptCache = new ConcurrentHashMap<String, ScriptCacheEntry>();
    private CompilerConfiguration config;

    {
        config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        config.setSourceEncoding(CompilerConfiguration.DEFAULT_SOURCE_ENCODING);
    }


    //TODO: more finals?

    private static class ScriptCacheEntry {
        private final Class scriptClass;
        private final long lastModified, lastCheck;
        private final Set<String> dependencies;
        private final boolean sourceNewer;

        public ScriptCacheEntry(Class clazz, long modified, long lastCheck, Set<String> depend, boolean sourceNewer) {
            this.scriptClass = clazz;
            this.lastModified = modified;
            this.lastCheck = lastCheck;
            this.dependencies = depend;
            this.sourceNewer = sourceNewer;
        }

        public ScriptCacheEntry(ScriptCacheEntry old, long lastCheck, boolean sourceNewer) {
            this(old.scriptClass, old.lastModified, lastCheck, old.dependencies, sourceNewer);
        }
    }

    private class ScriptClassLoader extends GroovyClassLoader {


        public ScriptClassLoader(GroovyClassLoader loader) {
            super(loader);
        }

        public ScriptClassLoader(ClassLoader loader, CompilerConfiguration config) {
            super(loader, config, false);
            setResLoader();
        }

        private void setResLoader() {
            final GroovyResourceLoader rl = getResourceLoader();
            setResourceLoader(className -> {
                String filename;
                for (String extension : getConfig().getScriptExtensions()) {
                    filename = className.replace('.', File.separatorChar) + "." + extension;
                    try {
                        URLConnection dependentScriptConn = rc.getResourceConnection(filename);
                        return dependentScriptConn.getURL();
                    } catch (ResourceException e) {
                        //TODO: maybe do something here?
                    }
                }
                return rl.loadGroovySource(className);
            });
        }

        @Override
        protected CompilationUnit createCompilationUnit(CompilerConfiguration configuration, CodeSource source) {
            CompilationUnit cu = super.createCompilationUnit(configuration, source);
            LocalData local = getLocalData().get();
            local.cu = cu;
            final StringSetMap cache = local.dependencyCache;
            final Map<String, String> precompiledEntries = local.precompiledEntries;

            // "." is used to transfer compilation dependencies, which will be
            // recollected later during compilation
            for (String depSourcePath : cache.get(".")) {
                try {
                    cache.get(depSourcePath);
                    cu.addSource(getResourceConnection(depSourcePath).getURL());
                } catch (ResourceException e) {
                    /* ignore */
                }
            }

            // remove all old entries including the "." entry
            cache.clear();

            cu.addPhaseOperation((final SourceUnit sourceUnit, final GeneratorContext context, final ClassNode classNode) -> {
               // GROOVY-4013: If it is an inner class, tracking its dependencies doesn't really
               // serve any purpose and also interferes with the caching done to track dependencies
               if (classNode.getOuterClass() != null) return;
               DependencyTracker dt = new DependencyTracker(sourceUnit, cache, precompiledEntries);
               dt.visitClass(classNode);
            }, Phases.CLASS_GENERATION);

            cu.setClassNodeResolver(new ClassNodeResolver() {
                @Override
                public LookupResult findClassNode(String origName, CompilationUnit compilationUnit) {
                    CompilerConfiguration cc = compilationUnit.getConfiguration();
                    String name = origName.replace('.', '/');
                    for (String ext : cc.getScriptExtensions()) {
                        try {
                            String finalName = name + "." + ext;
                            URLConnection conn = rc.getResourceConnection(finalName);
                            URL url = conn.getURL();
                            String path = url.toExternalForm();
                            ScriptCacheEntry entry = scriptCache.get(path);
                            Class clazz = null;
                            if (entry != null) clazz = entry.scriptClass;
                            if (GroovyScriptEngine.this.isSourceNewer(entry)) {
                                try {
                                    SourceUnit su = compilationUnit.addSource(url);
                                    return new LookupResult(su, null);
                                } finally {
                                    forceClose(conn);
                                }
                            } else {
                                precompiledEntries.put(origName, path);
                            }
                            if (clazz != null) {
                                ClassNode cn = new ClassNode(clazz);
                                return new LookupResult(null, cn);
                            }
                        } catch (ResourceException re) {
                            // skip
                        }
                    }
                    return super.findClassNode(origName, compilationUnit);
                }
            });

            return cu;
        }

        @Override
        public Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource) throws CompilationFailedException {
            synchronized (sourceCache) {
                return doParseClass(codeSource);
            }
        }

        private Class<?> doParseClass(GroovyCodeSource codeSource) {
            // local is kept as hard reference to avoid garbage collection
            ThreadLocal<LocalData> localTh = getLocalData();
            LocalData localData = new LocalData();
            localTh.set(localData);
            StringSetMap cache = localData.dependencyCache;
            Class<?> answer = null;
            try {
                updateLocalDependencyCache(codeSource, localData);
                answer = super.parseClass(codeSource, false);
                updateScriptCache(localData);
            } finally {
                cache.clear();
                localTh.remove();
            }
            return answer;
        }

        private void updateLocalDependencyCache(GroovyCodeSource codeSource, LocalData localData) {
            // we put the old dependencies into local cache so createCompilationUnit
            // can pick it up. We put that entry under the name "."
            ScriptCacheEntry origEntry = scriptCache.get(codeSource.getName());
            Set<String> origDep = null;
            if (origEntry != null) origDep = origEntry.dependencies;
            if (origDep != null) {
                Set<String> newDep = new HashSet<String>(origDep.size());
                for (String depName : origDep) {
                    ScriptCacheEntry dep = scriptCache.get(depName);
                    if (origEntry == dep || GroovyScriptEngine.this.isSourceNewer(dep)) {
                        newDep.add(depName);
                    }
                }
                StringSetMap cache = localData.dependencyCache;
                cache.put(".", newDep);
            }
        }

        private void updateScriptCache(LocalData localData) {
            StringSetMap cache = localData.dependencyCache;
            cache.makeTransitiveHull();
            long time = getCurrentTime();
            Set<String> entryNames = new HashSet<String>();
            for (Map.Entry<String, Set<String>> entry : cache.entrySet()) {
                String className = entry.getKey();
                Class clazz = getClassCacheEntry(className);
                if (clazz == null) continue;

                String entryName = getPath(clazz, localData.precompiledEntries);
                if (entryNames.contains(entryName)) continue;
                entryNames.add(entryName);
                Set<String> value = convertToPaths(entry.getValue(), localData.precompiledEntries);
                long lastModified;
                try {
                    lastModified = getLastModified(entryName);
                } catch (ResourceException e) {
                    lastModified = time;
                }
                ScriptCacheEntry cacheEntry = new ScriptCacheEntry(clazz, lastModified, time, value, false);
                scriptCache.put(entryName, cacheEntry);
            }
        }

        private String getPath(Class clazz, Map<String, String> precompiledEntries) {
            CompilationUnit cu = getLocalData().get().cu;
            String name = clazz.getName();
            ClassNode classNode = cu.getClassNode(name);
            if (classNode == null) {
                // this is a precompiled class!
                String path = precompiledEntries.get(name);
                if (path == null) throw new GroovyBugError("Precompiled class " + name + " should be available in precompiled entries map, but was not.");
                return path;
            } else {
                return classNode.getModule().getContext().getName();
            }
        }

        private Set<String> convertToPaths(Set<String> orig, Map<String, String> precompiledEntries) {
            Set<String> ret = new HashSet<String>();
            for (String className : orig) {
                Class clazz = getClassCacheEntry(className);
                if (clazz == null) continue;
                ret.add(getPath(clazz, precompiledEntries));
            }
            return ret;
        }
    }

    /**
     * Simple testing harness for the GSE. Enter script roots as arguments and
     * then input script names to run them.
     *
     * @param urls an array of URLs
     * @throws Exception if something goes wrong
     */
    public static void main(String[] urls) throws Exception {
        GroovyScriptEngine gse = new GroovyScriptEngine(urls);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while (true) {
            System.out.print("groovy> ");
            if ((line = br.readLine()) == null || line.equals("quit")) {
                break;
            }
            try {
                System.out.println(gse.run(line, new Binding()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize a new GroovyClassLoader with a default or
     * constructor-supplied parentClassLoader.
     *
     * @return the parent classloader used to load scripts
     */
    private GroovyClassLoader initGroovyLoader() {
        GroovyClassLoader groovyClassLoader =
                AccessController.doPrivileged((PrivilegedAction<ScriptClassLoader>) () -> {
                    if (parentLoader instanceof GroovyClassLoader) {
                        return new ScriptClassLoader((GroovyClassLoader) parentLoader);
                    } else {
                        return new ScriptClassLoader(parentLoader, config);
                    }
                });
        for (URL root : roots) groovyClassLoader.addURL(root);
        return groovyClassLoader;
    }

    /**
     * Get a resource connection as a <code>URLConnection</code> to retrieve a script
     * from the <code>ResourceConnector</code>.
     *
     * @param resourceName name of the resource to be retrieved
     * @return a URLConnection to the resource
     * @throws ResourceException
     */
    public URLConnection getResourceConnection(String resourceName) throws ResourceException {
        // Get the URLConnection
        URLConnection groovyScriptConn = null;

        ResourceException se = null;
        for (URL root : roots) {
            URL scriptURL = null;
            try {
                scriptURL = new URL(root, resourceName);
                groovyScriptConn = openConnection(scriptURL);

                break; // Now this is a bit unusual
            } catch (MalformedURLException e) {
                String message = "Malformed URL: " + root + ", " + resourceName;
                if (se == null) {
                    se = new ResourceException(message);
                } else {
                    se = new ResourceException(message, se);
                }
            } catch (IOException e1) {
                String message = "Cannot open URL: " + root + resourceName;
                groovyScriptConn = null;
                if (se == null) {
                    se = new ResourceException(message);
                } else {
                    se = new ResourceException(message, se);
                }
            }
        }

        if (se == null) se = new ResourceException("No resource for " + resourceName + " was found");

        // If we didn't find anything, report on all the exceptions that occurred.
        if (groovyScriptConn == null) throw se;
        return groovyScriptConn;
    }

    private static URLConnection openConnection(URL scriptURL) throws IOException {
        URLConnection urlConnection = scriptURL.openConnection();
        verifyInputStream(urlConnection);

        return scriptURL.openConnection();
    }

    /**
     * This method closes a {@link URLConnection} by getting its {@link InputStream} and calling the
     * {@link InputStream#close()} method on it. The {@link URLConnection} doesn't have a close() method
     * and relies on garbage collection to close the underlying connection to the file.
     * Relying on garbage collection could lead to the application exhausting the number of files the
     * user is allowed to have open at any one point in time and cause the application to crash
     * ({@link java.io.FileNotFoundException} (Too many open files)).
     * Hence the need for this method to explicitly close the underlying connection to the file.
     *
     * @param urlConnection the {@link URLConnection} to be "closed" to close the underlying file descriptors.
     */
    private static void forceClose(URLConnection urlConnection) {
        if (urlConnection != null) {
            // We need to get the input stream and close it to force the open
            // file descriptor to be released. Otherwise, we will reach the limit
            // for number of files open at one time.

            try {
                verifyInputStream(urlConnection);
            } catch (Exception e) {
                // Do nothing: We were not going to use it anyway.
            }
        }
    }

    private static void verifyInputStream(URLConnection urlConnection) throws IOException {
        try (InputStream in = urlConnection.getInputStream()) {
        }
    }

    /**
     * The groovy script engine will run groovy scripts and reload them and
     * their dependencies when they are modified. This is useful for embedding
     * groovy in other containers like games and application servers.
     *
     * @param roots This an array of URLs where Groovy scripts will be stored. They should
     *              be laid out using their package structure like Java classes
     */
    private GroovyScriptEngine(URL[] roots, ClassLoader parent, ResourceConnector rc) {
        if (roots == null) roots = EMPTY_URL_ARRAY;
        this.roots = roots;
        if (rc == null) rc = this;
        this.rc = rc;
        if (parent == CL_STUB) parent = this.getClass().getClassLoader();
        this.parentLoader = parent;
        this.groovyLoader = initGroovyLoader();
    }

    public GroovyScriptEngine(URL[] roots) {
        this(roots, CL_STUB, null);
    }

    public GroovyScriptEngine(URL[] roots, ClassLoader parentClassLoader) {
        this(roots, parentClassLoader, null);
    }

    public GroovyScriptEngine(String[] urls) throws IOException {
        this(createRoots(urls), CL_STUB, null);
    }

    private static URL[] createRoots(String[] urls) throws MalformedURLException {
        if (urls == null) return null;
        URL[] roots = new URL[urls.length];
        for (int i = 0; i < roots.length; i++) {
            if (urls[i].contains("://")) {
                roots[i] = new URL(urls[i]);
            } else {
                roots[i] = new File(urls[i]).toURI().toURL();
            }
        }
        return roots;
    }

    public GroovyScriptEngine(String[] urls, ClassLoader parentClassLoader) throws IOException {
        this(createRoots(urls), parentClassLoader, null);
    }

    public GroovyScriptEngine(String url) throws IOException {
        this(new String[]{url});
    }

    public GroovyScriptEngine(String url, ClassLoader parentClassLoader) throws IOException {
        this(new String[]{url}, parentClassLoader);
    }

    public GroovyScriptEngine(ResourceConnector rc) {
        this(null, CL_STUB, rc);
    }

    public GroovyScriptEngine(ResourceConnector rc, ClassLoader parentClassLoader) {
        this(null, parentClassLoader, rc);
    }

    /**
     * Get the <code>ClassLoader</code> that will serve as the parent ClassLoader of the
     * {@link GroovyClassLoader} in which scripts will be executed. By default, this is the
     * ClassLoader that loaded the <code>GroovyScriptEngine</code> class.
     *
     * @return the parent classloader used to load scripts
     */
    public ClassLoader getParentClassLoader() {
        return parentLoader;
    }

    /**
     * Get the class of the scriptName in question, so that you can instantiate
     * Groovy objects with caching and reloading.
     *
     * @param scriptName resource name pointing to the script
     * @return the loaded scriptName as a compiled class
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    public Class loadScriptByName(String scriptName) throws ResourceException, ScriptException {
        URLConnection conn = rc.getResourceConnection(scriptName);
        String path = conn.getURL().toExternalForm();
        ScriptCacheEntry entry = scriptCache.get(path);
        Class clazz = null;
        if (entry != null) clazz = entry.scriptClass;
        try {
            if (isSourceNewer(entry)) {
                try {
                    String encoding = conn.getContentEncoding() != null ? conn.getContentEncoding() : config.getSourceEncoding();
                    String content = IOGroovyMethods.getText(conn.getInputStream(), encoding);
                    clazz = groovyLoader.parseClass(content, path);
                } catch (IOException e) {
                    throw new ResourceException(e);
                }
            }
        } finally {
            forceClose(conn);
        }
        return clazz;
    }

    /**
     * Run a script identified by name with a single argument.
     *
     * @param scriptName name of the script to run
     * @param argument   a single argument passed as a variable named <code>arg</code> in the binding
     * @return a <code>toString()</code> representation of the result of the execution of the script
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    public String run(String scriptName, String argument) throws ResourceException, ScriptException {
        Binding binding = new Binding();
        binding.setVariable("arg", argument);
        Object result = run(scriptName, binding);
        return result == null ? "" : result.toString();
    }

    /**
     * Run a script identified by name with a given binding.
     *
     * @param scriptName name of the script to run
     * @param binding    the binding to pass to the script
     * @return an object
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    public Object run(String scriptName, Binding binding) throws ResourceException, ScriptException {
        return createScript(scriptName, binding).run();
    }

    /**
     * Creates a Script with a given scriptName and binding.
     *
     * @param scriptName name of the script to run
     * @param binding    the binding to pass to the script
     * @return the script object
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    public Script createScript(String scriptName, Binding binding) throws ResourceException, ScriptException {
        return InvokerHelper.createScript(loadScriptByName(scriptName), binding);
    }

    private long getLastModified(String scriptName) throws ResourceException {
        URLConnection conn = rc.getResourceConnection(scriptName);
        long lastMod = 0;
        try {
            lastMod = conn.getLastModified();
        } finally {
            // getResourceConnection() opening the inputstream, let's ensure all streams are closed
            forceClose(conn);
        }
        return lastMod;
    }

    protected boolean isSourceNewer(ScriptCacheEntry entry) {
        if (entry == null) return true;

        long mainEntryLastCheck = entry.lastCheck;
        long now = 0;

        boolean returnValue = false;
        for (String scriptName : entry.dependencies) {
            ScriptCacheEntry depEntry = scriptCache.get(scriptName);
            if (depEntry.sourceNewer) return true;

            // check if maybe dependency was recompiled, but this one here not
            if (mainEntryLastCheck < depEntry.lastModified) {
                returnValue = true;
                continue;
            }

            if (now == 0) now = getCurrentTime();
            long nextSourceCheck = depEntry.lastCheck + config.getMinimumRecompilationInterval();
            if (nextSourceCheck > now) continue;

            long lastMod;
            try {
                lastMod = getLastModified(scriptName);
            } catch (ResourceException e) {
                /*
                Class A depends on class B and they both are compiled once.  If class A is then
                loaded again from loadScriptByName(scriptName) after class B and all references to
                it have been deleted from the root, this exception will occur.  It is still valid
                and necessary to attempt a recompile of class A.
                */
                return true;
            }
            if (depEntry.lastModified < lastMod) {
                depEntry = new ScriptCacheEntry(depEntry, lastMod, true);
                scriptCache.put(scriptName, depEntry);
                returnValue = true;
            } else {
                depEntry = new ScriptCacheEntry(depEntry, now, false);
                scriptCache.put(scriptName, depEntry);
            }
        }

        return returnValue;
    }

    /**
     * Returns the GroovyClassLoader associated with this script engine instance.
     * Useful if you need to pass the class loader to another library.
     *
     * @return the GroovyClassLoader
     */
    public GroovyClassLoader getGroovyClassLoader() {
        return groovyLoader;
    }

    /**
     * @return a non null compiler configuration
     */
    public CompilerConfiguration getConfig() {
        return config;
    }

    /**
     * sets a compiler configuration
     *
     * @param config - the compiler configuration
     * @throws NullPointerException if config is null
     */
    public void setConfig(CompilerConfiguration config) {
        if (config == null) throw new NullPointerException("configuration cannot be null");
        this.config = config;
        this.groovyLoader = initGroovyLoader();
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
