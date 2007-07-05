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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import org.codehaus.groovy.classgen.ReflectorGenerator;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.Reflector;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

/**
 * A registry of MetaClass instances which caches introspection &
 * reflection information and allows methods to be dynamically added to
 * existing classes at runtime
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author John Wilson
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author Graeme Rocher
 *
 * @version $Revision$
 */
public class MetaClassRegistryImpl implements MetaClassRegistry{
    private volatile int constantMetaClassCount = 0;
    private ConcurrentReaderHashMap constantMetaClasses = new ConcurrentReaderHashMap();
    private MemoryAwareConcurrentReadMap weakMetaClasses = new MemoryAwareConcurrentReadMap();
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

        installMetaClassCreationHandle();
   }

    /**
     * Looks for a class called 'groovy.runtime.metaclass.CustomMetaClassCreationHandle' and if it exists uses it as the MetaClassCreationHandle
     * otherwise uses the default
     *
     * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle
     */
    private void installMetaClassCreationHandle() {
	       try {
	           final Class customMetaClassHandle = Class.forName("groovy.runtime.metaclass.CustomMetaClassCreationHandle");
	           final Constructor customMetaClassHandleConstructor = customMetaClassHandle.getConstructor(new Class[]{});
				 this.metaClassCreationHandle = (MetaClassCreationHandle)customMetaClassHandleConstructor.newInstance(new Object[]{});
	       } catch (final ClassNotFoundException e) {
	           this.metaClassCreationHandle = new MetaClassCreationHandle();
	       } catch (final Exception e) {
	           throw new GroovyRuntimeException("Could not instantiate custom Metaclass creation handle: "+ e, e);
	       }
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
        MetaClass answer=null;
        if (constantMetaClassCount!=0) answer = (MetaClass) constantMetaClasses.get(theClass);
        if (answer==null) answer = (MetaClass) weakMetaClasses.get(theClass);
        if (answer!=null) return answer;
       
        synchronized (theClass) {
            answer = getMetaClassFor(theClass);
            answer.initialize();
            weakMetaClasses.put(theClass, answer);
            return answer;
        }
    }

    public void removeMetaClass(Class theClass) {
        Object answer=null;
        if (constantMetaClassCount!=0) answer = constantMetaClasses.remove(theClass);
        if (answer==null) {
            weakMetaClasses.remove(theClass);
        } else {
            synchronized(theClass) {
                constantMetaClassCount--;
            }
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
            constantMetaClassCount++;
            constantMetaClasses.put(theClass, theMetaClass);
        }
    }

    public boolean useAccessible() {
        return useAccessible;
    }

    /**
     * create Reflector loader instance if not in map. This method
     * is only used with a lock on "this" and since loaderMap is not
     * used anywhere else no sync is needed here
     */
    private ReflectorLoader getReflectorLoader(final ClassLoader loader) {
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

    /**
     * Find a MetaClass for the class
     * Use the MetaClass of the superclass of the class to create the MetaClass
     * 
     * @param theClass
     * @return An instance of the MetaClass which will handle this class
     */
    private MetaClass getMetaClassFor(final Class theClass) {
        return metaClassCreationHandle.create(theClass,this);
    }

    // the following is experimental code, not intended for stable use yet
    private MetaClassCreationHandle metaClassCreationHandle = new MetaClassCreationHandle();
    /**
     * Gets a handle internally used to create MetaClass implementations
     * WARNING: experimental code, likely to change soon
     * @return the handle
     */
    public MetaClassCreationHandle getMetaClassCreationHandler() {
        return metaClassCreationHandle;
    }
    /**
     * Sets a handle internally used to create MetaClass implementations.
     * When replacing the handle with a custom version, you should
     * resuse the old handle to keep custom logic and to use the
     * default logic as fallback.
     * WARNING: experimental code, likely to change soon
     * @param handle the handle
     */
    public void setMetaClassCreationHandle(MetaClassCreationHandle handle) {
        metaClassCreationHandle = handle;
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
