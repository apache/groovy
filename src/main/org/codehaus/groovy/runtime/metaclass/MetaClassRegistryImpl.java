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

import groovy.lang.*;
import org.codehaus.groovy.reflection.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.codehaus.groovy.util.FastArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A registry of MetaClass instances which caches introspection &
 * reflection information and allows methods to be dynamically added to
 * existing classes at runtime
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author John Wilson
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author Graeme Rocher
 * @author Alex Tkachman
 *
 * @version $Revision$
 */
public class MetaClassRegistryImpl implements MetaClassRegistry{
    private boolean useAccessible;

    private FastArray instanceMethods = new FastArray();
    private FastArray staticMethods = new FastArray();

    private AtomicInteger version = new AtomicInteger();

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
            HashMap map = new HashMap();

            // lets register the default methods
            registerMethods(DefaultGroovyMethods.class, true, true, map);
            Class[] pluginDGMs = VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods();
            for (int i=0; i<pluginDGMs.length; i++) {
                registerMethods(pluginDGMs[i], false, true, map);
            }
            registerMethods(DefaultGroovyStaticMethods.class, false, false, map);

            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry e = (Map.Entry) it.next();
                CachedClass cls = (CachedClass) e.getKey();
                ArrayList list = (ArrayList) e.getValue();
                cls.setNewMopMethods(list);
            }
        }

        installMetaClassCreationHandle();

        final MetaClass emcMetaClass = metaClassCreationHandle.create(ExpandoMetaClass.class, this);
        emcMetaClass.initialize();
        ClassInfo.getClassInfo(ExpandoMetaClass.class).setStrongMetaClass(emcMetaClass);
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
    
    private void registerMethods(final Class theClass, final boolean useMethodrapper, final boolean useInstanceMethods, Map map) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(theClass).getMethods();

        if (useMethodrapper) {
            // Here we instanciate objects representing MetaMethods for DGM methods.
            // Calls for such meta methods done without reflection, so more effectively.
            // It gives 7-8% improvement for benchmarks involving just several ariphmetic operations
            for (int i = 0; ; ++i) {
                try {
                    final String className = "org.codehaus.groovy.runtime.dgm$" + i;
                    final Class aClass = Class.forName(className);
                    createMetaMethodFromClass(map, aClass);
                }
                catch(ClassNotFoundException e){
                    break;
                }
            }

            final Class[] additionals = DefaultGroovyMethods.additionals;
            for (int i = 0; i != additionals.length; ++i ) {
                createMetaMethodFromClass(map, additionals[i]);
            }
        }
        else
        {
            for (int i = 0; i < methods.length; i++) {
                CachedMethod method = methods[i];
                final int mod = method.getModifiers();
                if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
                    CachedClass[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length > 0) {
                        ArrayList arr = (ArrayList) map.get(paramTypes[0]);
                        if (arr == null) {
                            arr = new ArrayList(4);
                            map.put(paramTypes[0],arr);
                        }
                        if (useInstanceMethods) {
                            final NewInstanceMetaMethod metaMethod = new NewInstanceMetaMethod(method);
                            arr.add(metaMethod);
                            instanceMethods.add(metaMethod);
                        } else {
                            final NewStaticMetaMethod metaMethod = new NewStaticMetaMethod(method);
                            arr.add(metaMethod);
                            staticMethods.add(metaMethod);
                        }
                    }
                }
            }
        }
    }

    private void createMetaMethodFromClass(Map map, Class aClass) {
        try {
            MetaMethod method = (MetaMethod) aClass.newInstance();
            final CachedClass declClass = method.getDeclaringClass();
            ArrayList arr = (ArrayList) map.get(declClass);
            if (arr == null) {
                arr = new ArrayList(4);
                map.put(declClass,arr);
            }
            arr.add(method);
            instanceMethods.add(method);
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public final MetaClass getMetaClass(Class theClass) {

        final ClassInfo info = ClassInfo.getClassInfo(theClass);

        MetaClass answer = info.getMetaClassForClass();
        if (answer != null)
            return answer;

        info.lock();
        try {
            return getMetaClassUnderLock(theClass, info);
        }
        finally {
            info.unlock();
        }
    }

    private MetaClass getMetaClassUnderLock(Class theClass, ClassInfo info) {
        MetaClass answer;
        answer = info.getMetaClassForClass();
        if (answer != null)
            return answer;

        answer = metaClassCreationHandle.create(theClass, this);
        answer.initialize();

        if (GroovySystem.isKeepJavaMetaClasses()) {
            info.setStrongMetaClass(answer);
        } else {
            info.setWeakMetaClass(answer);
        }
        return answer;
    }

    public void removeMetaClass(Class theClass) {
        version.incrementAndGet();

        final ClassInfo info = ClassInfo.getClassInfo(theClass);

        info.lock();
        try {
            info.setStrongMetaClass(null);
        }
        finally {
            info.unlock();
        }
    }

    public MetaClass getMetaClass(Object obj) {
        Class theClass = obj.getClass ();
        final ClassInfo info = ClassInfo.getClassInfo(theClass);

        info.lock();
        try {
            final MetaClass instanceMetaClass = info.getPerInstanceMetaClass(obj);
            if (instanceMetaClass != null)
              return instanceMetaClass;

            return getMetaClassUnderLock(theClass, info);
        }
        finally {
            info.unlock();
        }
    }

    /**
     * Registers a new MetaClass in the registry to customize the type
     *
     * @param theClass
     * @param theMetaClass
     */
    public void setMetaClass(Class theClass, MetaClass theMetaClass) {
        version.incrementAndGet();

        final ClassInfo info = ClassInfo.getClassInfo(theClass);

        info.lock();
        try {
            info.setStrongMetaClass(theMetaClass);
        }
        finally {
            info.unlock();
        }
    }


    public void setMetaClass(Object obj, MetaClass theMetaClass) {
        version.incrementAndGet();

        Class theClass = obj.getClass ();
        final ClassInfo info = ClassInfo.getClassInfo(theClass);

        info.lock();
        try {
            info.setPerInstanceMetaClass(obj, theMetaClass);
        }
        finally {
            info.unlock();
        }
    }


    public boolean useAccessible() {
        return useAccessible;
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
		if(handle == null) throw new IllegalArgumentException("Cannot set MetaClassCreationHandle to null value!");
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

    public FastArray getInstanceMethods() {
        return instanceMethods;
    }

    public FastArray getStaticMethods() {
        return staticMethods;
    }
}
