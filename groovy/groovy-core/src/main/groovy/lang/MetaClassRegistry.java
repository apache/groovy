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
package groovy.lang;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A registery of MetaClass instances which caches introspection &
 * reflection information and allows methods to be dynamically added to
 * existing classes at runtime
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaClassRegistry {
    private Map metaClasses = Collections.synchronizedMap(new WeakHashMap());
    private boolean useAccessible;
    private Map loaderMap = Collections.synchronizedMap(new WeakHashMap());
    private GroovyClassLoader loader =
            (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new GroovyClassLoader(getClass().getClassLoader());
                }
            });

    public static final int LOAD_DEFAULT = 0;
    public static final int DONT_LOAD_DEFAULT = 1;
    private static MetaClassRegistry instanceInclude;
    private static MetaClassRegistry instanceExclude;


    public MetaClassRegistry() {
        this(true);
    }

    public MetaClassRegistry(int loadDefault) {
        if (loadDefault == LOAD_DEFAULT) {
            this.useAccessible = true;
            // lets register the default methods
            lookup(DefaultGroovyMethods.class).registerInstanceMethods();
            lookup(DefaultGroovyStaticMethods.class).registerStaticMethods();
            checkInitialised();
        }
        else {
            this.useAccessible = true;
            // do nothing to avoid loading DefaultGroovyMethod
        }
    }

    /**
     * @param useAccessible defines whether or not the {@link java.lang.reflect.AccessibleObject.setAccessible();}
     *                      method will be called to enable access to all methods when using reflection
     */
    public MetaClassRegistry(boolean useAccessible) {
        this.useAccessible = useAccessible;

        // lets register the default methods
        lookup(DefaultGroovyMethods.class).registerInstanceMethods();
        lookup(DefaultGroovyStaticMethods.class).registerStaticMethods();
        checkInitialised();
    }

    public MetaClass getMetaClass(Class theClass) {
        synchronized (theClass) {
            MetaClass answer = (MetaClass) metaClasses.get(theClass);
            if (answer == null) {
                try {
                    answer = new MetaClass(this, theClass);
                    answer.checkInitialised();
                }
                catch (IntrospectionException e) {
                    throw new GroovyRuntimeException("Could not introspect class: " + theClass.getName() + ". Reason: " + e,
                            e);
                }
                metaClasses.put(theClass, answer);
            }
            return answer;
        }
    }

    public void removeMetaClass(Class theClass) {
        metaClasses.remove(theClass);
    }


    /**
     * Registers a new MetaClass in the registry to customize the type
     *
     * @param theClass
     * @param theMetaClass
     */
    public void setMetaClass(Class theClass, MetaClass theMetaClass) {
        metaClasses.put(theClass, theMetaClass);
    }

    public boolean useAccessible() {
        return useAccessible;
    }

    /**
     * A helper class to load meta class bytecode into the class loader
     */
    public Class loadClass(final String name, final byte[] bytecode) throws ClassNotFoundException {
        return (Class) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return getGroovyLoader(loader).defineClass(name, bytecode, getClass().getProtectionDomain());
            }
        });
    }

    public Class loadClass(final ClassLoader loader, final String name, final byte[] bytecode) throws ClassNotFoundException {
        return (Class) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return getGroovyLoader(loader).defineClass(name, bytecode, getClass().getProtectionDomain());
            }
        });
         }

    public Class loadClass(ClassLoader loader, String name) throws ClassNotFoundException {
        return getGroovyLoader(loader).loadClass(name);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return getGroovyLoader(loader).loadClass(name);
    }

    private GroovyClassLoader getGroovyLoader(ClassLoader loader) {
        if (loader instanceof GroovyClassLoader) {
            return (GroovyClassLoader) loader;
        }
        
        synchronized (loaderMap) {
            GroovyClassLoader groovyLoader = (GroovyClassLoader) loaderMap.get(loader);
            if (groovyLoader == null) {
                if (loader == null || loader == getClass().getClassLoader()) {
                    groovyLoader = this.loader;
                }
                else {
                    // lets check that the class loader can see the Groovy classes
                    // if so we'll use that, otherwise lets use the local class loader
                    try {
                        loader.loadClass(getClass().getName());

                        // thats fine, lets use the loader
                        groovyLoader = new GroovyClassLoader(loader);
                    }
                    catch (ClassNotFoundException e) {

                        // we can't see the groovy classes here
                        // so lets try create a new loader
                        ClassLoader localLoader = getClass().getClassLoader();
                        groovyLoader = new GroovyClassLoader(localLoader);
                    }
                }
                loaderMap.put(loader, groovyLoader);
            }

            return groovyLoader;
        }
    }

    /**
     * Ensures that all the registered MetaClass instances are initalized
     */
    void checkInitialised() {
        // lets copy all the classes in the repository right now 
        // to avoid concurrent modification exception
        List list = new ArrayList(metaClasses.values());
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MetaClass metaClass = (MetaClass) iter.next();
            metaClass.checkInitialised();
        }
    }

    /**
     * Used by MetaClass when registering new methods which avoids initializing the MetaClass instances on lookup
     */
    MetaClass lookup(Class theClass) {
        MetaClass answer = (MetaClass) metaClasses.get(theClass);
        if (answer == null) {
            try {
                answer = new MetaClass(this, theClass);
            }
            catch (IntrospectionException e) {
                throw new GroovyRuntimeException("Could not introspect class: " + theClass.getName() + ". Reason: " + e,
                        e);
            }
            metaClasses.put(theClass, answer);
        }
        return answer;
    }


    public MetaMethod getDefinedMethod(Class theClass, String methodName, Class[] args, boolean isStatic) {
        MetaClass metaclass = this.getMetaClass(theClass);
        if (metaclass == null) {
            return null;
        }
        else {
            if (isStatic) {
                return metaclass.retrieveStaticMethod(methodName, args);
            }
            else {
                return metaclass.retrieveMethod(methodName, args);
            }
        }
    }

    public Constructor getDefinedConstructor(Class theClass, Class[] args) {
        MetaClass metaclass = this.getMetaClass(theClass);
        if (metaclass == null) {
            return null;
        }
        else {
            return metaclass.retrieveConstructor(args);
        }
    }

    /**
     * Singleton of MetaClassRegistry. Shall we use threadlocal to store the instance?
     *
     * @param includeExtension
     * @return
     */
    public static MetaClassRegistry getIntance(int includeExtension) {
        if (includeExtension != DONT_LOAD_DEFAULT) {
            if (instanceInclude == null) {
                instanceInclude = new MetaClassRegistry();
            }
            return instanceInclude;
        }
        else {
            if (instanceExclude == null) {
                instanceExclude = new MetaClassRegistry(DONT_LOAD_DEFAULT);
            }
            return instanceExclude;
        }
    }
}
