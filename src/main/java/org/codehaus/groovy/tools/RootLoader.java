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
package org.codehaus.groovy.tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This ClassLoader should be used as root of class loaders. Any
 * RootLoader does have its own classpath. When searching for a
 * class or resource this classpath will be used. Parent
 * Classloaders are ignored first. If a class or resource
 * can't be found in the classpath of the RootLoader, then parent is
 * checked.
 * <p>
 * <b>Note:</b> this is very against the normal behavior of
 * classloaders. Normal is to first check parent and then look in
 * the resources you gave this classloader.
 * <p>
 * It's possible to add urls to the classpath at runtime through {@link #addURL(URL)}.
 * <p>
 * <b>Why using RootLoader?</b>
 * If you have to load classes with multiple classloaders and a
 * classloader does know a class which depends on a class only
 * a child of this loader does know, then you won't be able to
 * load the class. To load the class the child is not allowed
 * to redirect its search for the class to the parent first.
 * That way the child can load the class. If the child does not
 * have all classes to do this, this fails of course.
 * <p>
 * For example:
 * <p>
 * <pre>
 *       parentLoader   (has classpath: a.jar;c.jar)
 *           |
 *           |
 *       childLoader    (has classpath: a.jar;b.jar;c.jar)
 * </pre>
 *
 * class C (from c.jar) extends B (from b.jar)
 *
 * childLoader.find("C")
 * <pre>
 * --&gt; parentLoader does know C.class, try to load it
 * --&gt; to load C.class it has to load B.class
 * --&gt; parentLoader is unable to find B.class in a.jar or c.jar
 * --&gt; NoClassDefFoundException!
 * </pre>
 *
 * if childLoader had tried to load the class by itself, there
 * would be no problem. Changing childLoader to be a RootLoader
 * instance will solve that problem.
 */
public class RootLoader extends URLClassLoader {

    private static final String ORG_W3C_DOM_NODE = "org.w3c.dom.Node";
    private final Map<String, Class<?>> customClasses = new HashMap<>();

    /**
     * Constructs a {@code RootLoader} without classpath.
     *
     * @param parent the parent Loader
     */
    public RootLoader(final ClassLoader parent) {
        this(new URL[0], parent);
    }

    /**
     * Constructs a {@code RootLoader} with a parent loader and an array of URLs
     * as its classpath.
     */
    public RootLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        // major hack here!!
        try {
            customClasses.put(ORG_W3C_DOM_NODE, super.loadClass(ORG_W3C_DOM_NODE, false));
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Constructs a {@code RootLoader} with a {@link LoaderConfiguration} object
     * which holds the classpath.
     */
    public RootLoader(final LoaderConfiguration lc) {
        this(Optional.ofNullable(RootLoader.class.getClassLoader()).orElseGet(ClassLoader::getSystemClassLoader));

        Thread.currentThread().setContextClassLoader(this);

        for (URL url : lc.getClassPathUrls()) {
            addURL(url);
        }
        // TODO M12N eventually defer this until later when we have a full Groovy
        // environment and use normal Grape.grab()
        String groovyHome = System.getProperty("groovy.home");
        for (String url : lc.getGrabUrls()) {
            Map<String, Object> grabParts = GrapeUtil.getIvyParts(url);
            String group   = (String) grabParts.get("group");
            String module  = (String) grabParts.get("module");
            String version = (String) grabParts.get("version");
            File jar = new File(groovyHome + "/repo/" + group + "/" + module + "/jars/" + module + "-" + version + ".jar");
            try {
                addURL(jar.toURI().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) return c;
        c = customClasses.get(name);
        if (c != null) return c;

        try {
            c = super.findClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (c == null)
            c = super.loadClass(name, resolve);

        if (resolve)
            resolveClass(c);

        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(final String name) {
        URL url = findResource(name);
        if (url == null)
            url = super.getResource(name);
        return url;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Collapses entries that resolve to the same canonical jar file so callers
     * such as {@link java.util.ServiceLoader} don't see the same providers
     * twice when the launcher's startup classpath and the {@code load} entries
     * in {@code groovy-starter.conf} reference the same jar (GROOVY-11978).
     */
    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        LinkedHashSet<URL> result = new LinkedHashSet<>();
        Set<File> seenJars = new HashSet<>();
        appendUnique(findResources(name), result, seenJars);
        ClassLoader parent = getParent();
        Enumeration<URL> parentResources = (parent != null)
                ? parent.getResources(name)
                : ClassLoader.getSystemResources(name);
        appendUnique(parentResources, result, seenJars);
        return Collections.enumeration(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    private static void appendUnique(Enumeration<URL> in, Set<URL> out, Set<File> seenJars) {
        while (in.hasMoreElements()) {
            URL u = in.nextElement();
            File jar = jarFileOf(u);
            if (jar == null) {
                out.add(u); // directory or non-file URL — URL identity is enough
            } else if (seenJars.add(jar)) {
                out.add(u); // first occurrence of this jar wins
            }
        }
    }

    private static File jarFileOf(URL u) {
        if (!"jar".equals(u.getProtocol())) return null;
        String s = u.toString();
        int bang = s.indexOf("!/");
        if (bang < 0) return null;
        String inner = s.substring(4, bang); // strip "jar:" prefix
        if (!inner.startsWith("file:")) return null;
        try {
            return canonicalFile(new File(URI.create(inner)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static File canonicalFile(File f) {
        try {
            // Path.toRealPath follows symlinks on every platform; File.getCanonicalFile
            // only does so on UNIX, which is why we don't use it here.
            return f.toPath().toRealPath().toFile();
        } catch (IOException e) {
            try {
                return f.getCanonicalFile();
            } catch (IOException e2) {
                return f.getAbsoluteFile();
            }
        }
    }
}
