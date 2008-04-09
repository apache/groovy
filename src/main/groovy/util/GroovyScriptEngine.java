/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Specific script engine able to reload modified scripts as well as dealing properly with dependent scripts.
 *
 * @author sam
 * @author Marc Palmer
 * @author Guillaume Laforge
 */
public class GroovyScriptEngine implements ResourceConnector {

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
            if ((line = br.readLine()) == null || line.equals("quit"))
                break;
            try {
                System.out.println(gse.run(line, new Binding()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private URL[] roots;
    private Map scriptCache = Collections.synchronizedMap(new HashMap());
    private ResourceConnector rc;

    private static ThreadLocal currentCacheEntryHolder = new ThreadLocal();
    private GroovyClassLoader groovyLoader = null;

    private static class ScriptCacheEntry {
        private Class scriptClass;
        private long lastModified;
        private Map dependencies = new HashMap();
    }

    /**
     * Initialize a new GroovyClassLoader with the parentClassLoader passed as a parameter.
     * A GroovyScriptEngine should only use one GroovyClassLoader but since in version
     * prior to 1.0-RC-01 you could set a new parentClassLoader
     * Ultimately groovyLoader should be final and only set in the constructor
     *
     * @param parentClassLoader the class loader to use
     */
    private void initGroovyLoader(final ClassLoader parentClassLoader) {
        if (groovyLoader == null || groovyLoader.getParent() != parentClassLoader) {
            groovyLoader =
                    (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return new GroovyClassLoader(parentClassLoader) {
                                protected Class findClass(String className) throws ClassNotFoundException {
                                    String filename = className.replace('.', File.separatorChar) + ".groovy";
                                    URLConnection dependentScriptConn = null;
                                    try {
                                        dependentScriptConn = rc.getResourceConnection(filename);
                                        ScriptCacheEntry currentCacheEntry = (ScriptCacheEntry) currentCacheEntryHolder.get();
                                        currentCacheEntry.dependencies.put(
                                                dependentScriptConn.getURL(),
                                                new Long(dependentScriptConn.getLastModified()));
                                        return parseClass(dependentScriptConn.getInputStream(), filename);
                                    } catch (ResourceException e1) {
                                        throw new ClassNotFoundException("Could not read " + className + ": " + e1);
                                    } catch (CompilationFailedException e2) {
                                        throw new ClassNotFoundException("Syntax error in " + className + ": " + e2);
                                    } catch (IOException e3) {
                                        throw new ClassNotFoundException("Problem reading " + className + ": " + e3);
                                    } finally {
                                        try {
                                            if (dependentScriptConn != null && dependentScriptConn.getInputStream() != null) {
                                                dependentScriptConn.getInputStream().close();
                                            }
                                        } catch (IOException e) {
                                            // IGNORE
                                        }
                                    }
                                }
                            };
                        }
                    });
        }
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
        for (int i = 0; i < roots.length; i++) {
            URL scriptURL = null;
            try {
                scriptURL = new URL(roots[i], resourceName);
                groovyScriptConn = scriptURL.openConnection();

                // Make sure we can open it, if we can't it doesn't exist.
                // Could be very slow if there are any non-file:// URLs in there
                groovyScriptConn.getInputStream();

                break; // Now this is a bit unusual

            } catch (MalformedURLException e) {
                String message = "Malformed URL: " + roots[i] + ", " + resourceName;
                if (se == null) {
                    se = new ResourceException(message);
                } else {
                    se = new ResourceException(message, se);
                }
            } catch (IOException e1) {
                String message = "Cannot open URL: " + scriptURL;
                if (se == null) {
                    se = new ResourceException(message);
                } else {
                    se = new ResourceException(message, se);
                }
            }
        }

        // If we didn't find anything, report on all the exceptions that occurred.
        if (groovyScriptConn == null) {
            throw se;
        }

        return groovyScriptConn;
    }

    /**
     * The groovy script engine will run groovy scripts and reload them and
     * their dependencies when they are modified. This is useful for embedding
     * groovy in other containers like games and application servers.
     *
     * @param roots This an array of URLs where Groovy scripts will be stored. They should
     *              be layed out using their package structure like Java classes
     */
    public GroovyScriptEngine(URL[] roots) {
        this.roots = roots;
        this.rc = this;
        initGroovyLoader(getClass().getClassLoader());
    }

    public GroovyScriptEngine(URL[] roots, ClassLoader parentClassLoader) {
        this(roots);
        initGroovyLoader(parentClassLoader);
    }

    public GroovyScriptEngine(String[] urls) throws IOException {
        roots = new URL[urls.length];
        for (int i = 0; i < roots.length; i++) {
            if(urls[i].indexOf("://") != -1) {
                roots[i] = new URL(urls[i]);
            } else {
                roots[i] = new File(urls[i]).toURI().toURL();
            }
        }
        this.rc = this;
        initGroovyLoader(getClass().getClassLoader());
    }

    public GroovyScriptEngine(String[] urls, ClassLoader parentClassLoader) throws IOException {
        this(urls);
        initGroovyLoader(parentClassLoader);
    }

    public GroovyScriptEngine(String url) throws IOException {
        this(new String[]{url});
    }

    public GroovyScriptEngine(String url, ClassLoader parentClassLoader) throws IOException {
        this(url);
        initGroovyLoader(parentClassLoader);
    }

    public GroovyScriptEngine(ResourceConnector rc) {
        this.rc = rc;
        initGroovyLoader(getClass().getClassLoader());
    }

    public GroovyScriptEngine(ResourceConnector rc, ClassLoader parentClassLoader) {
        this(rc);
        initGroovyLoader(parentClassLoader);
    }

    /**
     * Get the <code>ClassLoader</code> that will serve as the parent ClassLoader of the
     * {@link GroovyClassLoader} in which scripts will be executed. By default, this is the
     * ClassLoader that loaded the <code>GroovyScriptEngine</code> class.
     *
     * @return parent classloader used to load scripts
     */
    public ClassLoader getParentClassLoader() {
        return groovyLoader.getParent();
    }

    /**
     * @param parentClassLoader ClassLoader to be used as the parent ClassLoader for scripts executed by the engine
     * @deprecated
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        if (parentClassLoader == null) {
            throw new IllegalArgumentException("The parent class loader must not be null.");
        }
        initGroovyLoader(parentClassLoader);
    }

    /**
     * Get the class of the scriptName in question, so that you can instantiate Groovy objects with caching and reloading.
     *
     * @param scriptName resource name pointing to the script
     * @return the loaded scriptName as a compiled class
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException if there is a problem parsing the script
     */
    public Class loadScriptByName(String scriptName) throws ResourceException, ScriptException {
        scriptName = scriptName.replace('.', File.separatorChar) + ".groovy";
        ScriptCacheEntry entry = updateCacheEntry(scriptName);
        return entry.scriptClass;
    }


    /**
     * Get the class of the scriptName in question, so that you can instantiate Groovy objects with caching and reloading.
     *
     * @param scriptName resource name pointing to the script
     * @param parentClassLoader the class loader to use when loading the script
     * @return the loaded scriptName as a compiled class
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException if there is a problem parsing the script
     * @deprecated
     */
    public Class loadScriptByName(String scriptName, ClassLoader parentClassLoader)
            throws ResourceException, ScriptException {
        initGroovyLoader(parentClassLoader);
        return loadScriptByName(scriptName);
    }

    /**
     * Locate the class and reload it or any of its dependencies
     *
     * @param scriptName resource name pointing to the script
     * @return the cache entry for scriptName
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException if there is a problem parsing the script
     */
    private ScriptCacheEntry updateCacheEntry(String scriptName)
            throws ResourceException, ScriptException {
        ScriptCacheEntry entry;

        scriptName = scriptName.intern();
        synchronized (scriptName) {

            URLConnection groovyScriptConn = rc.getResourceConnection(scriptName);

            // URL last modified
            long lastModified = groovyScriptConn.getLastModified();
            // Check the cache for the scriptName
            entry = (ScriptCacheEntry) scriptCache.get(scriptName);

            if (entry == null || entry.lastModified < lastModified || dependencyOutOfDate(entry)) {
                // Make a new entry
                ScriptCacheEntry currentCacheEntry = new ScriptCacheEntry();
                currentCacheEntryHolder.set(currentCacheEntry);
                InputStream in = null;

                try {
                    in = groovyScriptConn.getInputStream();
                    currentCacheEntry.scriptClass = groovyLoader.parseClass(in, scriptName);
                } catch (Exception e) {
                    throw new ScriptException("Could not parse scriptName: " + scriptName, e);
                } finally {
                    currentCacheEntryHolder.set(null);
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        // Do nothing: Just want to make sure it is closed
                    }
                }

                currentCacheEntry.lastModified = lastModified;
                scriptCache.put(scriptName, currentCacheEntry);
                entry = currentCacheEntry;
                currentCacheEntry = null;
            }
        }
        return entry;
    }

    private boolean dependencyOutOfDate(ScriptCacheEntry entry) {
        if (entry != null) {
            for (Iterator i = entry.dependencies.keySet().iterator(); i.hasNext();) {
                URLConnection urlc = null;
                URL url = (URL) i.next();
                try {
                    urlc = url.openConnection();
                    urlc.setDoInput(false);
                    urlc.setDoOutput(false);
                    long dependentLastModified = urlc.getLastModified();
                    if (dependentLastModified > ((Long) entry.dependencies.get(url)).longValue()) {
                        return true;
                    }
                } catch (IOException ioe) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Run a script identified by name with a single argument.
     *
     * @param scriptName name of the script to run
     * @param argument   a single argument passed as a variable named <code>arg</code> in the binding
     * @return a <code>toString()</code> representation of the result of the execution of the script
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException if there is a problem parsing the script
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
     * @throws ScriptException if there is a problem parsing the script
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
     * @throws ScriptException if there is a problem parsing the script
     */
    public Script createScript(String scriptName, Binding binding) throws ResourceException, ScriptException {
        ScriptCacheEntry entry = updateCacheEntry(scriptName);
        scriptName = scriptName.intern();
        return InvokerHelper.createScript(entry.scriptClass, binding);
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
}
