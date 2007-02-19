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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.classgen.ReflectorGenerator;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.MemoryAwareConcurrentReadMap;
import org.codehaus.groovy.runtime.MethodHelper;
import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.runtime.ReflectorLoader;
import org.objectweb.asm.ClassWriter;

/**
 * A registery of MetaClass instances which caches introspection &
 * reflection information and allows methods to be dynamically added to
 * existing classes at runtime
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author John Wilson
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @version $Revision$
 */
public class MetaClassRegistryImpl implements MetaClassRegistry{
    private MemoryAwareConcurrentReadMap metaClasses = new MemoryAwareConcurrentReadMap();
    private MemoryAwareConcurrentReadMap loaderMap = new MemoryAwareConcurrentReadMap();
    private boolean useAccessible;
    
    private LinkedList instanceMethods = new LinkedList();
    private LinkedList staticMethods = new LinkedList();

    public static final int LOAD_DEFAULT = 0;
    public static final int DONT_LOAD_DEFAULT = 1;
    private static MetaClassRegistry instanceInclude;
    private static MetaClassRegistry instanceExclude;

    public MetaClassRegistryImpl() {
        this(LOAD_DEFAULT, true);
    }

    public MetaClassRegistryImpl(int loadDefault) {
        this(loadDefault, true);
    }

    /**
     * @param useAccessible defines whether or not the {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)}
     *                      method will be called to enable access to all methods when using reflection
     */
    public MetaClassRegistryImpl(boolean useAccessible) {
        this(LOAD_DEFAULT, useAccessible);
    }
    
    public MetaClassRegistryImpl(final int loadDefault, final boolean useAccessible) {
        this.useAccessible = useAccessible;
        
        if (loadDefault == LOAD_DEFAULT) {
            // lets register the default methods
            registerMethods(DefaultGroovyMethods.class, true);
            registerMethods(DefaultGroovyStaticMethods.class, false);
        }
        
        // Initialise the registry with the MetaClass for java.lang.Object
        // This is needed this MetaClass is used to create The Metaclasses for all the classes which subclass Object
        // Note that the user can replace the standard MetaClass with a custom MetaClass by including 
        // groovy.runtime.metaclass.java.lang.ObjectMetaClass in the classpath
        
        metaClasses.putStrong(Object.class, GroovySystem.objectMetaClass);
   }
    
    private void registerMethods(final Class theClass, final boolean useInstanceMethods) {
        Method[] methods = theClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (MethodHelper.isStatic(method)) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    if (useInstanceMethods) {
                        instanceMethods.add(method);
                    } else {
                        staticMethods.add(method);
                    }
                }
            }
        }
    }

    public MetaClass getMetaClass(Class theClass) {
        synchronized (theClass) {
        MetaClass answer = (MetaClass) metaClasses.get(theClass);
        
            if (answer == null) {
                answer = getMetaClassFor(theClass);
                answer.initialize();
                metaClasses.put(theClass, answer);
            }
            
            return answer;
        }
    }

    public void removeMetaClass(Class theClass) {
        synchronized (theClass) {
            metaClasses.remove(theClass);
        }
    }


    /**
     * Registers a new MetaClass in the registry to customize the type
     *
     * @param theClass
     * @param theMetaClass
     */
    public void setMetaClass(Class theClass, MetaClass theMetaClass) {
        synchronized(theClass) {
            metaClasses.putStrong(theClass, theMetaClass);
        }
    }

    public boolean useAccessible() {
        return useAccessible;
    }

    private ReflectorLoader getReflectorLoader(final ClassLoader loader) {
        synchronized (loaderMap) {
            ReflectorLoader reflectorLoader = (ReflectorLoader) loaderMap.get(loader);
            if (reflectorLoader == null) {
                reflectorLoader = (ReflectorLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return new ReflectorLoader(loader);
                    }
                }); 
                loaderMap.put(loader, reflectorLoader);
            }
            return reflectorLoader;
        }
    }

    /**
     * Used by MetaClass when registering new methods which avoids initializing the MetaClass instances on lookup
     */
    MetaClass lookup(Class theClass) {
        synchronized (theClass) {
            MetaClass answer = (MetaClass) metaClasses.get(theClass);
            if (answer == null) {
                answer = getMetaClassFor(theClass);
                metaClasses.put(theClass, answer);
            }
            return answer;
        }
    }

    /**
     * Find a MetaClass for the class
     * Use the MetaClass of the superclass of the class to create the MetaClass
     * 
     * @param theClass
     * @return An instance of the MetaClass which will handle this class
     */
    private MetaClass getMetaClassFor(final Class theClass) {
    final Class theSuperClass = theClass.getSuperclass();
    
        if (theSuperClass == null) {
            // The class is an interface - use Object's Metaclass
            return GroovySystem.objectMetaClass.createMetaClass(theClass, this);
        } else {
            return getMetaClass(theSuperClass).createMetaClass(theClass, this);
        }
    }

    /**
     * Singleton of MetaClassRegistry. Shall we use threadlocal to store the instance?
     *
     * @param includeExtension
     */
    public static MetaClassRegistry getInstance(int includeExtension) {
        if (includeExtension != DONT_LOAD_DEFAULT) {
            if (instanceInclude == null) {
                instanceInclude = new MetaClassRegistryImpl();
            }
            return instanceInclude;
        }
        else {
            if (instanceExclude == null) {
                instanceExclude = new MetaClassRegistryImpl(DONT_LOAD_DEFAULT);
            }
            return instanceExclude;
        }
    }

    public synchronized Reflector loadReflector(final Class theClass, List methods) {
        final String name = getReflectorName(theClass);
        ClassLoader loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader loader = theClass.getClassLoader();
                if (loader == null) loader = this.getClass().getClassLoader();
                return loader;
            }
        });
        final ReflectorLoader rloader = getReflectorLoader(loader);
        Class ref = rloader.getLoadedClass(name);
        if (ref == null) {
            /*
             * Lets generate it && load it.
             */                        
            ReflectorGenerator generator = new ReflectorGenerator(methods);
            ClassWriter cw = new ClassWriter(true);
            generator.generate(cw, name);
            final byte[] bytecode = cw.toByteArray();
            ref = (Class) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return rloader.defineClass(name, bytecode, getClass().getProtectionDomain());
                }
            }); 
        }
        try {
            return (Reflector) ref.newInstance();
        } catch (Exception e) {
            throw new GroovyRuntimeException("Could not generate and load the reflector for class: " + name + ". Reason: " + e, e);
        }
    }
    
    private String getReflectorName(Class theClass) {
        String className = theClass.getName();
        String packagePrefix = "gjdk.";
        String name = packagePrefix + className + "_GroovyReflector";
        if (theClass.isArray()) {
               Class clazz = theClass;
               name = packagePrefix;
               int level = 0;
               while (clazz.isArray()) {
                  clazz = clazz.getComponentType();
                  level++;
               }
            String componentName = clazz.getName();
            name = packagePrefix + componentName + "_GroovyReflectorArray";
            if (level>1) name += level;
        }
        return name;
    }

    public List getInstanceMethods() {
        return instanceMethods;
    }

    public List getStaticMethods() {
        return staticMethods;
    }
}
