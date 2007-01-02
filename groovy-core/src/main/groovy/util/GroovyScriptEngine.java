/*
 * $Id$version Jan 9, 2004 12:19:58 PM $user Exp $
 * 
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
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
package groovy.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * @param urls
     * @throws Exception
     */
    public static void main(String[] urls) throws Exception {
        URL[] roots = new URL[urls.length];
        for (int i = 0; i < roots.length; i++) {
            roots[i] = new File(urls[i]).toURL();
        }
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
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
    private ClassLoader parentClassLoader = getClass().getClassLoader();

    private static class ScriptCacheEntry {
        private Class scriptClass;
        private long lastModified;
        private Map dependencies = new HashMap();
    }

    /**
     * Get a resource connection as a <code>URLConnection</code> to retrieve a script
     * from the <code>ResourceConnector</code>
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
     * be layed out using their package structure like Java classes 
     */
    public GroovyScriptEngine(URL[] roots) {
        this.roots = roots;
        this.rc = this;
    }

    public GroovyScriptEngine(URL[] roots, ClassLoader parentClassLoader) {
        this(roots);
        this.parentClassLoader = parentClassLoader;
    }

    public GroovyScriptEngine(String[] urls) throws IOException {
        roots = new URL[urls.length];
        for (int i = 0; i < roots.length; i++) {
            roots[i] = new File(urls[i]).toURL();
        }
        this.rc = this;
    }

    public GroovyScriptEngine(String[] urls, ClassLoader parentClassLoader) throws IOException {
        this(urls);
        this.parentClassLoader = parentClassLoader;
    }

    public GroovyScriptEngine(String url) throws IOException {
        roots = new URL[1];
        roots[0] = new File(url).toURL();
        this.rc = this;
    }

    public GroovyScriptEngine(String url, ClassLoader parentClassLoader) throws IOException {
        this(url);
        this.parentClassLoader = parentClassLoader;
    }

    public GroovyScriptEngine(ResourceConnector rc) {
        this.rc = rc;
    }

    public GroovyScriptEngine(ResourceConnector rc, ClassLoader parentClassLoader) {
        this(rc);
        this.parentClassLoader = parentClassLoader;
    }

    /**
     * Get the <code>ClassLoader</code> that will serve as the parent ClassLoader of the
     * {@link GroovyClassLoader} in which scripts will be executed. By default, this is the
     * ClassLoader that loaded the <code>GroovyScriptEngine</code> class.
     *
     * @return parent classloader used to load scripts
     */
    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    /**
     * @param parentClassLoader ClassLoader to be used as the parent ClassLoader for scripts executed by the engine
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        if (parentClassLoader == null) {
            throw new IllegalArgumentException("The parent class loader must not be null.");
        }
        this.parentClassLoader = parentClassLoader;
    }

    /**
     * Get the class of the scriptName in question, so that you can instantiate Groovy objects with caching and reloading.
     *
     * @param scriptName
     * @return the loaded scriptName as a compiled class
     * @throws ResourceException
     * @throws ScriptException
     */
    public Class loadScriptByName(String scriptName) throws ResourceException, ScriptException {
        return loadScriptByName( scriptName, getClass().getClassLoader());
    }


    /**
     * Get the class of the scriptName in question, so that you can instantiate Groovy objects with caching and reloading.
     *
     * @param scriptName
     * @return the loaded scriptName as a compiled class
     * @throws ResourceException
     * @throws ScriptException
     */
    public Class loadScriptByName(String scriptName, ClassLoader parentClassLoader)
            throws ResourceException, ScriptException {
        scriptName = scriptName.replace('.', File.separatorChar) + ".groovy";
        ScriptCacheEntry entry = updateCacheEntry(scriptName, parentClassLoader);
        return entry.scriptClass;
    }

    /**
     * Locate the class and reload it or any of its dependencies
     *
     * @param scriptName
     * @param parentClassLoader
     * @return the scriptName cache entry
     * @throws ResourceException
     * @throws ScriptException
     */
    private ScriptCacheEntry updateCacheEntry(String scriptName, final ClassLoader parentClassLoader)
            throws ResourceException, ScriptException
    {
        ScriptCacheEntry entry;

        scriptName = scriptName.intern();
        synchronized (scriptName) {

            URLConnection groovyScriptConn = rc.getResourceConnection(scriptName);

            // URL last modified
            long lastModified = groovyScriptConn.getLastModified();
            // Check the cache for the scriptName
            entry = (ScriptCacheEntry) scriptCache.get(scriptName);
            // If the entry isn't null check all the dependencies

            boolean dependencyOutOfDate = false;
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
                            dependencyOutOfDate = true;
                            break;
                        }
                    } catch (IOException ioe) {
                        dependencyOutOfDate = true;
                        break;
                    }
                }
            }

            if (entry == null || entry.lastModified < lastModified || dependencyOutOfDate) {
                // Make a new entry
                entry = new ScriptCacheEntry();

                // Closure variable
                final ScriptCacheEntry finalEntry = entry;

                // Compile the scriptName into an object
                GroovyClassLoader groovyLoader =
                        (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return new GroovyClassLoader(parentClassLoader) {
                                    protected Class findClass(String className) throws ClassNotFoundException {
                                        String filename = className.replace('.', File.separatorChar) + ".groovy";
                                        URLConnection dependentScriptConn = null;
                                        try {
                                            dependentScriptConn = rc.getResourceConnection(filename);
                                            finalEntry.dependencies.put(
                                                    dependentScriptConn.getURL(),
                                                    new Long(dependentScriptConn.getLastModified()));
                                        } catch (ResourceException e1) {
                                            throw new ClassNotFoundException("Could not read " + className + ": " + e1);
                                        }
                                        try {
                                            return parseClass(dependentScriptConn.getInputStream(), filename);
                                        } catch (CompilationFailedException e2) {
                                            throw new ClassNotFoundException("Syntax error in " + className + ": " + e2);
                                        } catch (IOException e2) {
                                            throw new ClassNotFoundException("Problem reading " + className + ": " + e2);
                                        }
                                    }
                                };
                            }
                        });

                try {
                    entry.scriptClass = groovyLoader.parseClass(groovyScriptConn.getInputStream(), scriptName);
                } catch (Exception e) {
                    throw new ScriptException("Could not parse scriptName: " + scriptName, e);
                }
                entry.lastModified = lastModified;
                scriptCache.put(scriptName, entry);
            }
        }
        return entry;
    }

    /**
     * Run a script identified by name.
     *
     * @param scriptName name of the script to run
     * @param argument a single argument passed as a variable named <code>arg</code> in the binding
     * @return a <code>toString()</code> representation of the result of the execution of the script
     * @throws ResourceException
     * @throws ScriptException
     */
    public String run(String scriptName, String argument) throws ResourceException, ScriptException {
        Binding binding = new Binding();
        binding.setVariable("arg", argument);
        Object result = run(scriptName, binding);
        return result == null ? "" : result.toString();
    }

    /**
     * Run a script identified by name.
     *
     * @param scriptName name of the script to run
     * @param binding binding to pass to the script
     * @return an object
     * @throws ResourceException
     * @throws ScriptException
     */
    public Object run(String scriptName, Binding binding) throws ResourceException, ScriptException {

        ScriptCacheEntry entry = updateCacheEntry(scriptName, getParentClassLoader());
        Script scriptObject = InvokerHelper.createScript(entry.scriptClass, binding);
        return scriptObject.run();
    }
}
