package org.codehaus.groovy.control;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class CompilerClassLoader extends ClassLoader {

    private static final URL[] EMPTY_URL_ARRAY = new URL[0];
    private ClassLoader parent;
    private InnerLoader inner;
    private Map map;
    
    private class InnerLoader extends URLClassLoader{
        public InnerLoader() {
            super(EMPTY_URL_ARRAY);
        }

        public void addPath(String path) throws MalformedURLException{
            addURL( new File( path ).toURL() );
        }        
        
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = (Class) map.get(name);
            if (c!=null) return c;
            try {
                // we prefer to search on the given paths first
                c = super.findClass(name);
                map.put(name,c);
                return c;
            } catch (ClassNotFoundException cnfe) {
                c = parent.loadClass(name);
                map.put(name,c);
                return c;
            } 
        }   
        
        public Class loadClass(String name) throws ClassNotFoundException {
            return loadClass(name,false);
        }
        
        protected Class findClass(String name) throws ClassNotFoundException {
            return loadClass(name,false);
        }
        
    }
    
    
    
    public CompilerClassLoader()
    {
        super(Thread.currentThread().getContextClassLoader());
        parent = Thread.currentThread().getContextClassLoader();
        inner = new InnerLoader();
        map = new HashMap();
        //super(EMPTY_URL_ARRAY);
    }

    public void addPath(String path)
        throws MalformedURLException
    {
        inner.addPath(path);
    }
    
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return inner.loadClass(name);
    }
    
    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name,false);
    }
    
    protected Class findClass(String name) throws ClassNotFoundException {
        return loadClass(name,false);
    }
}
