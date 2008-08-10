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
package org.codehaus.groovy.reflection;

import groovy.lang.*;
import org.codehaus.groovy.reflection.stdclasses.*;
import org.codehaus.groovy.util.*;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle for all information we want to keep about the class
 *
 * @author Alex.Tkachman
 */
public class ClassInfo extends ConcurrentSoftMap.Entry<Class,ClassInfo> {

    private final LazyCachedClassRef cachedClassRef;

    private MetaClass strongMetaClass;

    private SoftReference<MetaClass> weakMetaClass;

    private static final Object NONE = new Object();

    private volatile int version;

    private final LazyClassLoaderRef artifactClassLoader;

    private static final HashSet<ClassInfo> modifiedExpandos = new HashSet<ClassInfo>();

    private final LockableObject lock = new LockableObject();

    MetaMethod[] dgmMetaMethods = CachedClass.EMPTY;

    MetaMethod[] newMetaMethods = CachedClass.EMPTY;

    public final int hash;
    private ConcurrentWeakMap perInstanceMetaClassMap;

    ClassInfo(ConcurrentSoftMap.Segment segment, Class klazz, int hash) {
        super (segment, klazz, hash);

        if (ClassInfo.DebugRef.debug)
          new DebugRef(klazz);

        this.hash = hash;
        cachedClassRef = new LazyCachedClassRef(this);
        artifactClassLoader = new LazyClassLoaderRef(this);
    }

    public int getVersion() {
        return version;
    }

    public ExpandoMetaClass getModifiedExpando() {
        return strongMetaClass == null ? null : strongMetaClass instanceof ExpandoMetaClass ? (ExpandoMetaClass)strongMetaClass : null;
    }

    public static void clearModifiedExpandos() {
        for (Iterator<ClassInfo> it = modifiedExpandos.iterator(); it.hasNext(); ) {
            ClassInfo info = it.next();
            it.remove();
            info.setStrongMetaClass(null);
        }
    }

    public CachedClass getCachedClass() {
        return cachedClassRef.get();
    }

    public ClassLoaderForClassArtifacts getArtifactClassLoader() {
        return artifactClassLoader.get();
    }

    private static final ClassInfoSet globalClassSet = new ClassInfoSet();

    public static ClassInfo getClassInfo (Class cls) {
        return localMap.get().get(cls);
    }

    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    public void setStrongMetaClass(MetaClass answer) {
        version++;

        if (strongMetaClass instanceof ExpandoMetaClass) {
          ((ExpandoMetaClass)strongMetaClass).inRegistry = false;
          modifiedExpandos.remove(this);
        }

        strongMetaClass = answer;

        if (strongMetaClass instanceof ExpandoMetaClass) {
          ((ExpandoMetaClass)strongMetaClass).inRegistry = true;
          modifiedExpandos.add(this);
        }

        weakMetaClass = null;
    }

    public MetaClass getWeakMetaClass() {
        return weakMetaClass == null ? null : weakMetaClass.get();
    }

    public void setWeakMetaClass(MetaClass answer) {
        version++;

        strongMetaClass = null;
        if (answer == null) {
           weakMetaClass = null;
        } else {
           weakMetaClass = new SoftReference<MetaClass> (answer);
        }
    }

    public MetaClass getMetaClassForClass() {
        return strongMetaClass != null ? strongMetaClass : weakMetaClass == null ? null : weakMetaClass.get();
    }

    private MetaClass getMetaClassUnderLock() {
        MetaClass answer;
        answer = getMetaClassForClass();
        if (answer != null) return answer;

        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        answer = ((MetaClassRegistryImpl) metaClassRegistry).metaClassCreationHandle.create(get(), metaClassRegistry);
        answer.initialize();

        if (GroovySystem.isKeepJavaMetaClasses()) {
            setStrongMetaClass(answer);
        } else {
            setWeakMetaClass(answer);
        }
        return answer;
    }

    public final MetaClass getMetaClass() {
        MetaClass answer = getMetaClassForClass();
        if (answer != null) return answer;

        lock();
        try {
            return getMetaClassUnderLock();
        } finally {
            unlock();
        }
    }

    public MetaClass getMetaClass(Object obj) {
        final MetaClass instanceMetaClass = getPerInstanceMetaClass(obj);
        if (instanceMetaClass != null)
            return instanceMetaClass;

        lock();
        try {
            return getMetaClassUnderLock();
        } finally {
            unlock();
        }
    }

    public static int size () {
        return globalClassSet.size();
    }

    public static int fullSize () {
        return globalClassSet.fullSize();
    }

    public void finalizeRef() {
        setStrongMetaClass(null);
        cachedClassRef.set(null);
        artifactClassLoader.set(null);

        super.finalizeRef();
    }

    private static CachedClass createCachedClass(Class klazz, ClassInfo classInfo) {
        if (klazz == Object.class)
            return new ObjectCachedClass(classInfo);

        if (klazz == String.class)
            return new StringCachedClass(classInfo);

        CachedClass cachedClass;
        if (Number.class.isAssignableFrom(klazz) || klazz.isPrimitive()) {
            if (klazz == Number.class)
              cachedClass = new NumberCachedClass(klazz, classInfo);
            else
                if (klazz == Integer.class || klazz == Integer.TYPE)
                  cachedClass = new IntegerCachedClass(klazz, classInfo);
                else
                  if (klazz == Double.class || klazz == Double.TYPE )
                    cachedClass = new DoubleCachedClass(klazz, classInfo);
                  else
                      if (klazz == BigDecimal.class )
                        cachedClass = new BigDecimalCachedClass(klazz, classInfo);
                      else
                          if (klazz == Long.class || klazz == Long.TYPE)
                            cachedClass = new LongCachedClass(klazz, classInfo);
                          else
                              if (klazz == Float.class || klazz == Float.TYPE)
                                cachedClass = new FloatCachedClass(klazz, classInfo);
                              else
                                  if (klazz == Short.class || klazz == Short.TYPE)
                                    cachedClass = new ShortCachedClass(klazz, classInfo);
                                  else
                                      if (klazz == Boolean.TYPE)
                                        cachedClass = new BooleanCachedClass(klazz, classInfo);
                                      else
                                          if (klazz == Character.TYPE)
                                            cachedClass = new CharacterCachedClass(klazz, classInfo);
                                          else
                                              if (klazz == BigInteger.class)
                                                cachedClass = new BigIntegerCachedClass(klazz, classInfo);
                                              else
                                                if (klazz == Byte.class)
                                                  cachedClass = new ByteCachedClass(klazz, classInfo);
                                                else
                                                  cachedClass = new CachedClass(klazz, classInfo);
        }
        else
            if (klazz.getName().charAt(0) == '[')
              cachedClass = new ArrayCachedClass(klazz, classInfo);
            else
                if (klazz == Boolean.class)
                  cachedClass = new BooleanCachedClass(klazz, classInfo);
                else
                    if (klazz == Character.class)
                      cachedClass = new CharacterCachedClass(klazz, classInfo);
                    else
                      if (Closure.class.isAssignableFrom(klazz))
                        cachedClass = new CachedClosureClass (klazz, classInfo);
                      else
                        cachedClass = new CachedClass(klazz, classInfo);

        return cachedClass;
    }

    public void lock () {
        lock.lock();
    }

    public void unlock () {
        lock.unlock();
    }

    public MetaClass getPerInstanceMetaClass(Object obj) {
        if (perInstanceMetaClassMap == null)
          return null;

        return (MetaClass) perInstanceMetaClassMap.get(obj);
    }

    public void setPerInstanceMetaClass(Object obj, MetaClass metaClass) {
        version++;

        if (metaClass != null) {
            if (perInstanceMetaClassMap == null)
              perInstanceMetaClassMap = new ConcurrentWeakMap ();

            perInstanceMetaClassMap.put(obj, metaClass);
        }
        else {
            if (perInstanceMetaClassMap != null) {
              perInstanceMetaClassMap.remove(obj);
            }
        }
    }

    public boolean hasPerInstanceMetaClasses () {
        return perInstanceMetaClassMap != null;
    }

    public static class ClassInfoSet extends ConcurrentSoftMap<Class,ClassInfo> {

        public ClassInfoSet() {
        }

        protected Segment createSegment(int cap) {
            return new Segment(cap);
        }

        static final class Segment extends ConcurrentSoftMap.Segment<Class,ClassInfo> {
            Segment(int initialCapacity) {
                super(initialCapacity);
            }

            protected ClassInfo createEntry(Class key, int hash, ClassInfo unused) {
                return new ClassInfo(this, key, hash);
            }
        }
    }

    private static class LocalMap extends HashMap<Class,ClassInfo> {

        private static final int CACHE_SIZE = 5;

        public final Thread myThread = Thread.currentThread();

        private int nextCacheEntry;

        private final ClassInfo[] cache = new ClassInfo[CACHE_SIZE];
        private static final ClassInfo NOINFO = new ClassInfo(null,null,0);

        private LocalMap() {
            for (int i = 0; i < cache.length; i++) {
                cache[i] = NOINFO;
            }
        }

        public ClassInfo get(Class key) {
            ClassInfo info = getFromCache(key);
            if (info != null)
              return info;

            info = super.get(key);
            if (info != null)
              return putToCache(info);

            return putToCache((ClassInfo) globalClassSet.getOrPut(key,null));
        }

        private ClassInfo getFromCache (Class klazz) {
            for (int i = 0, k = nextCacheEntry-1; i < cache.length; i++, k--) {
                if (k < 0)
                  k += CACHE_SIZE;

                final ClassInfo info = cache[k];
                if (klazz == info.get()) {
                    nextCacheEntry = k+1;
                    if (nextCacheEntry == CACHE_SIZE)
                      nextCacheEntry = 0;
                    return info;
                }
            }
            return null;
        }

        private ClassInfo putToCache (ClassInfo classInfo) {
            cache [nextCacheEntry++] = classInfo;
            if (nextCacheEntry == CACHE_SIZE)
              nextCacheEntry = 0;
            return classInfo;
        }
    }

    private static final ThreadLocal<LocalMap> localMap = new ThreadLocal<LocalMap> () {
        LocalMap recentThreadMap;

        protected LocalMap initialValue() {
            return new LocalMap();
        }

        public LocalMap get() {
            LocalMap recent = recentThreadMap;
            if (recent != null && recent.myThread == Thread.currentThread())
              return recent;
            else {
                final LocalMap res = super.get();
                recentThreadMap = res;
                return res;
            }
        }
    };

    private static class LazyCachedClassRef extends LazySoftReference<CachedClass> {
        private final ClassInfo info;

        LazyCachedClassRef(ClassInfo info) {
            this.info = info;
        }

        public CachedClass initValue() {
            return createCachedClass(info.get(), info);
        }
    }

    private static class LazyStaticMetaClassFieldRef extends LazySoftReference {
        private final ClassInfo info;

        LazyStaticMetaClassFieldRef(ClassInfo info) {
            this.info = info;
        }

        public Object initValue() {
            final CachedClass aClass = info.getCachedClass();

            final CachedField[] cachedFields = aClass.getFields();
            for (CachedField cachedField : cachedFields) {
                if (cachedField.getName().startsWith("$staticMetaClass") && cachedField.getType() == SoftReference.class && cachedField.isStatic()) {
                    return cachedField;
                }
            }

            return NONE;
        }
    }

    private static class LazyClassLoaderRef extends LazySoftReference<ClassLoaderForClassArtifacts> {
        private final ClassInfo info;

        LazyClassLoaderRef(ClassInfo info) {
            this.info = info;
        }

        public ClassLoaderForClassArtifacts initValue() {
            return new ClassLoaderForClassArtifacts(info.get());
        }
    }

    private static class DebugRef extends FinalizableRef.DebugRef<Class> {
        public final static boolean debug = false;

        static final AtomicInteger count = new AtomicInteger();

        final String name;

        public DebugRef(Class klazz) {
            super(klazz);
            name = klazz == null ? "<null>" : klazz.getName();
            count.incrementAndGet();
        }

        public void finalizeRef() {
            System.out.println(name + " unloaded " + count.decrementAndGet() + " classes kept");
            super.finalizeRef();
        }
    }
}
