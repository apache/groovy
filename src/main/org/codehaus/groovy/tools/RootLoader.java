/*
 * RootLoader.java created on 10.08.2005
 *
 */
package org.codehaus.groovy.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * This ClassLoader should be used as root of class loaders.  
 * @author Jochen Theodorou
 */
public class RootLoader extends ClassLoader {

    private ClassLoader parent; 
    private URLClassLoader inner;
    
    private RootLoader(ClassLoader parent) {
        super(parent);
    }
    
    public RootLoader(URL[] urls, ClassLoader parent) {
        this(parent);
        inner = new URLClassLoader(urls,null);
    }
    
    public RootLoader(LoaderConfiguration lc) {
        this(RootLoader.class.getClassLoader());
        inner = new URLClassLoader(lc.getClassPathUrls(),null);
    }

    protected synchronized Class loadClass(final String name, boolean resolve) throws ClassNotFoundException {
        // only check the parent if the searched class can't be found
        try {
            return inner.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            // fall through
        }
        return super.loadClass(name,resolve);
    }
    
    public URL getResource(String name) {
        URL url = inner.getResource(name);
        url = super.getResource(name);
        return url;
    }
    
    
    protected Enumeration findResources(String name) throws IOException {
        final Enumeration enum1 = inner.findResources(name);
        final Enumeration enum2 = super.findResources(name);
        return new Enumeration(){
            public boolean hasMoreElements() {
                return enum1.hasMoreElements() || enum2.hasMoreElements();
            }
            public Object nextElement() {
                if (enum1.hasMoreElements()) return enum1.nextElement();
                if (enum2.hasMoreElements()) return enum2.nextElement();
                return null;
            }
        };
    }
    
}
