/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.codehaus.groovy.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * This ClassLoader should be used as root of class loaders. Any
 * RootLoader does have it's own classpath. When searching for a 
 * class or resource this classpath will be used. Parent 
 * Classloaders are ignored first. If a class or resource 
 * can't be found in the classpath of the RootLoader, then parent is
 * checked.
 * 
 * <b>Note:</b> this is very against the normal behavior of 
 * classloaders. Normal is to frist check parent and then look in
 * the ressources you gave this classloader.
 * 
 * It's possible to add urls to the classpath at runtime through
 * @see #addURL(URL)
 * 
 * <b>Why using RootLoader?</b>
 * If you have to load classes with multiple classloaders and a
 * classloader does know a class which depends on a class only 
 * a child of this loader does know, then you won't be able to 
 * load the class. To load the class the child is not allowed 
 * to redirect it's search for the class to the parent first.
 * That way the child can load the class. If the child does not
 * have all classes to do this, this fails of course.
 *  
 * For example:
 *  
 *  <pre>
 *  parentLoader   (has classpath: a.jar;c.jar)
 *      |
 *      |
 *  childLoader    (has classpath: a.jar;b.jar;c.jar)
 *  </pre>
 *  
 *  class C (from c.jar) extends B (from b.jar)
 *  
 *  childLoader.find("C")
 *  --> parentLoader does know C.class, try to load it
 *  --> to load C.class it has to load B.class
 *  --> parentLoader is unable to find B.class in a.jar or c.jar
 *  --> NoClassDefFoundException!
 *  
 *  if childLoader had tried to load the class by itself, there
 *  would be no problem. Changing childLoader to be a RootLoader 
 *  instance will solve that problem.
 *   
 * @author Jochen Theodorou
 */
public class RootLoader extends ClassLoader {

    private InnerLoader inner;
    
    private class InnerLoader extends URLClassLoader {
        public InnerLoader(URL[] urls) {
            super(urls,null);
        }        
        public void addPathEntry(URL url) {
            addURL(url);
        }
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException cnfe) {
                return RootLoader.this.loadClassByName(name,true,resolve);
            }
        }
    }
    
    /**
     * constructs a new RootLoader without classpath
     * @param parent the parent Loader
     */   
    private RootLoader(ClassLoader parent) {
        super(parent);
    }
    
    /**
     * constructs a new RootLoader with a parent loader and an
     * array of URLs as classpath
     */
    public RootLoader(URL[] urls, ClassLoader parent) {
        this(parent);
        inner = new InnerLoader(urls);
    }
    
    /**
     * constructs a new RootLoader with a @see LoaderConfiguration
     * object which holds the classpath
     */
    public RootLoader(LoaderConfiguration lc) {
        this(RootLoader.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(this);
        inner = new InnerLoader(lc.getClassPathUrls());
    }

    /**
     * loads a class using the name of the class
     */
    protected synchronized Class loadClass(final String name, boolean resolve) throws ClassNotFoundException {
        return loadClassByName(name,false,resolve);
    }
    
    /**
     * method to avoid endless loops
     */
    private Class loadClassByName(String name, boolean ignoreInner, boolean resolve) throws ClassNotFoundException {
        // if the searched class can't be found in inner, then try the 
        // old behavior which searches in parent first
        if (!ignoreInner) {
            try {
                return inner.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // fall through
            }
        }
        try {
            return super.loadClass(name,resolve);
        } catch (NoClassDefFoundError ncdfe) {
            if (ncdfe.getMessage().indexOf("wrong name")>0) {
                throw new ClassNotFoundException(name);
            } else {
                throw ncdfe;
            }
        }
    }
    
    /**
     * returns the URL of a resource, or null if it is not found
     */
    public URL getResource(String name) {
        URL url = inner.getResource(name);
        if (url==null) url = super.getResource(name);
        return url;
    }    
    
    /**
     * returns an Enumeration of all found ressources. Resources found
     * in the classpath of this loader are at the beginning of the
     * returned enumeration
     */
    protected Enumeration findResources(String name) throws IOException {
        final Enumeration enum1 = inner.findResources(name);
        final Enumeration enum2 = super.findResources(name);
        return new Enumeration() {
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
 
    /**
     * adds an url to the classpath of this classloader
     */
    public void addURL(URL url) {
        inner.addPathEntry(url);
    }
    
    /**
     * returns all classpath entries of this classloader
     */
    public URL[] getURLs() {
        return inner.getURLs();
    }
}
