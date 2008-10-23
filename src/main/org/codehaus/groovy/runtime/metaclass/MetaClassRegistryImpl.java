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
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.FastArray;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

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
    private volatile int constantMetaClassCount = 0;
    private ConcurrentReaderHashMap constantMetaClasses = new ConcurrentReaderHashMap();
    private MemoryAwareConcurrentReadMap weakMetaClasses = new MemoryAwareConcurrentReadMap();
    private boolean useAccessible;

    private FastArray instanceMethods = new FastArray();
    private FastArray staticMethods = new FastArray();

    private volatile Integer version = new Integer(0);

    /*
       We keep references to meta classes already known to this thread.
       It allows us to avoid synchronization. When we need to ask global registry
       we do sync but usually it is enough to check if global registry has the
       same version as when we asked last time (neither removeMetaClass
       nor setMetaClass were called), if version changed we prefer to forget
       everything we know in the thread and start again (most likely it happens not too often).
       Unfortunately, we have to keep it in weak map to avoid possible leak of classes.
     */
    private class LocallyKnownClasses extends WeakHashMap {
        int version;

        public static final int CACHE_SIZE = 5;
        final MetaClass cache [] = new MetaClass[CACHE_SIZE];
        int nextCacheEntry;

        public MetaClass getMetaClass(Class theClass) {
            final int regv = MetaClassRegistryImpl.this.version.intValue();
            if (version != regv) {
              clear ();
            }
            else {
                MetaClass mc = checkCache(theClass);
                if (mc != null)
                  return mc;

                mc = checkMap(theClass);
                if (mc != null)
                  return mc;
            }

            return getFromGlobal(theClass);
        }

        private MetaClass checkCache(Class theClass) {
            for (int i = 0; i != CACHE_SIZE; i++) {
                final MetaClass metaClass = cache[i];
                if (metaClass != null && metaClass.getTheClass() == theClass) {
                    return metaClass;
                }
            }
            return null;
        }

        private MetaClass checkMap(Class theClass) {
            MetaClass mc;
            final SoftReference ref = (SoftReference) get(theClass);
            if (ref != null && (mc = (MetaClass) ref.get()) != null) {
                putToCache(mc);
                return mc;
            }
            return null;
        }

        private MetaClass getFromGlobal(Class theClass) {
            MetaClass answer = getGlobalMetaClass(theClass);
            put(theClass, answer);
            version = MetaClassRegistryImpl.this.version.intValue();
            return answer;
        }

        public Object put(Object key, Object value) {
            putToCache((MetaClass) value);
            return super.put(key, new SoftReference(value));
        }

        private void putToCache(MetaClass value) {
            cache [nextCacheEntry++] = value;
            if (nextCacheEntry == CACHE_SIZE)
              nextCacheEntry = 0;
        }

        public void clear() {
            for (int i = 0; i < cache.length; i++) {
                cache [i] = null;
            }
            super.clear();
        }
    }

    private MyThreadLocal locallyKnown = new MyThreadLocal();

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
            registerMethods(DefaultGroovyMethods.class, true, map);
            Class[] pluginDGMs = VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods();
            for (int i=0; i<pluginDGMs.length; i++) {
                registerMethods(pluginDGMs[i], true, map);
            }
            registerMethods(DefaultGroovyStaticMethods.class, false, map);

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
        constantMetaClasses.put(ExpandoMetaClass.class,emcMetaClass);
        constantMetaClassCount = 1;
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
    
    private void registerMethods(final Class theClass, final boolean useInstanceMethods, Map map) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(theClass).getMethods();

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

    private MetaClass getGlobalMetaClass (Class theClass) {
        MetaClass answer=null;
        if (constantMetaClassCount!=0) answer = (MetaClass) constantMetaClasses.get(theClass);
        if (answer!=null) return answer;
        answer = (MetaClass) weakMetaClasses.get(theClass);
        if (answer!=null) return answer;

        synchronized (theClass) {
            answer = (MetaClass) weakMetaClasses.get(theClass);
            if (answer!=null) return answer;
    
            // We've got a lock on the Class and we need to be sure that we're in
            // the ReflectionCache before we call MetaClass.initialize().
            // There is probably another place to do this, but I want to be sure...
            final CachedClass forEffect = ReflectionCache.getCachedClass(theClass);
            
            answer = metaClassCreationHandle.create(theClass, this);
            answer.initialize();
            if (GroovySystem.isKeepJavaMetaClasses()) {
                constantMetaClassCount++;
                constantMetaClasses.put(theClass,answer);
            } else {
                weakMetaClasses.put(theClass, answer);
            }
        }
        return answer;
    }

    public MetaClass getMetaClass(Class theClass) {
        return locallyKnown.getMetaClass(theClass);
    }

    public synchronized void removeMetaClass(Class theClass) {
        version = new Integer (version.intValue()+1);

        Object answer=null;
        if (constantMetaClassCount!=0) answer = constantMetaClasses.remove(theClass);
        if (answer==null) {
            weakMetaClasses.remove(theClass);
        } else {
            constantMetaClassCount--;
        }

        ReflectionCache.getCachedClass(theClass).setStaticMetaClassField (null);
    }

    /**
     * Registers a new MetaClass in the registry to customize the type
     *
     * @param theClass
     * @param theMetaClass
     */
    public synchronized void setMetaClass(Class theClass, MetaClass theMetaClass) {
        version = new Integer (version.intValue()+1);

        constantMetaClassCount++;
        constantMetaClasses.put(theClass, theMetaClass);

        ReflectionCache.getCachedClass(theClass).setStaticMetaClassField (theMetaClass);
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

    private class MyThreadLocal extends ThreadLocal {
        private volatile LocallyKnownClasses myClasses = new LocallyKnownClasses();
        private Thread myThread = Thread.currentThread();

        protected Object initialValue() {
            return new LocallyKnownClasses();
        }

        public MetaClass getMetaClass (Class theClass) {
            return ((LocallyKnownClasses)get()).getMetaClass(theClass);
        }

        public Object get() {
            if (Thread.currentThread() != myThread)
              return super.get();
            else
              return myClasses;
        }
    }
}
