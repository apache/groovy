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

import java.lang.ref.PhantomReference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle for all information we want to keep about the class
 *
 * @author Alex.Tkachman
 */
public class ClassInfo extends ManagedConcurrentMap.Entry<Class,ClassInfo> {

    private static final Set<ClassInfo> modifiedExpandos = new HashSet<ClassInfo>();

    private final LazyCachedClassRef cachedClassRef;
    private final LazyClassLoaderRef artifactClassLoader;
    private final LockableObject lock = new LockableObject();
    public final int hash;

    private volatile int version;

    private MetaClass strongMetaClass;
    private ManagedReference<MetaClass> weakMetaClass;
    MetaMethod[] dgmMetaMethods = CachedClass.EMPTY;
    MetaMethod[] newMetaMethods = CachedClass.EMPTY;
    private ManagedConcurrentMap perInstanceMetaClassMap;
    
    private static ReferenceBundle softBundle = ReferenceBundle.getSoftBundle();
    private static ReferenceBundle weakBundle = ReferenceBundle.getWeakBundle();
    private static final ClassInfoSet globalClassSet = new ClassInfoSet(softBundle);

    ClassInfo(ManagedConcurrentMap.Segment segment, Class klazz, int hash) {
        super (softBundle, segment, klazz, hash);

        if (ClassInfo.DebugRef.debug)
          new DebugRef(klazz);

        this.hash = hash;
        cachedClassRef = new LazyCachedClassRef(softBundle, this);
        artifactClassLoader = new LazyClassLoaderRef(softBundle, this);
    }

    public int getVersion() {
        return version;
    }

    public void incVersion() {
        version++;
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

    public static ClassInfo getClassInfo (Class cls) {
        ThreadLocalMapHandler handler = localMapRef.get();
        SoftReference<LocalMap> ref=null;
        if (handler!=null) ref = handler.get();
        LocalMap map=null;
        if (ref!=null) map = ref.get();
        if (map!=null) return map.get(cls);
        return (ClassInfo) globalClassSet.getOrPut(cls,null);
    }

    public static Collection<ClassInfo> getAllClassInfo () {
        ThreadLocalMapHandler handler = localMapRef.get();
        SoftReference<LocalMap> ref=null;
        if (handler!=null) ref = handler.get();
        LocalMap map=null;
        if (ref!=null) map = ref.get();
        if (map!=null) return map.values();
        return globalClassSet.values();
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
           weakMetaClass = new ManagedReference<MetaClass> (softBundle,answer);
        }
    }

    public MetaClass getMetaClassForClass() {
        return strongMetaClass != null ? strongMetaClass : weakMetaClass == null ? null : weakMetaClass.get();
    }

    private MetaClass getMetaClassUnderLock() {
        MetaClass answer = getStrongMetaClass();
        if (answer!=null) return answer;
        
        answer = getWeakMetaClass();
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        MetaClassRegistry.MetaClassCreationHandle mccHandle = metaClassRegistry.getMetaClassCreationHandler();
        
        if (answer != null) {
            boolean enableGloballyOn = (mccHandle instanceof ExpandoMetaClassCreationHandle);
            boolean cachedAnswerIsEMC = (answer instanceof ExpandoMetaClass);
            // if EMC.enableGlobally() is OFF, return whatever the cached answer is.
            // but if EMC.enableGlobally() is ON and the cached answer is not an EMC, come up with a fresh answer
            if(!enableGloballyOn || cachedAnswerIsEMC) {
                return answer;
            }
        }

        answer = mccHandle.create(get(), metaClassRegistry);
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
        cachedClassRef.clear();
        artifactClassLoader.clear();

        super.finalizeRef();
    }

    private static CachedClass createCachedClass(Class klazz, ClassInfo classInfo) {
        if (klazz == Object.class)
            return new ObjectCachedClass(classInfo);

        if (klazz == String.class)
            return new StringCachedClass(classInfo);

        CachedClass cachedClass;
        if (Number.class.isAssignableFrom(klazz) || klazz.isPrimitive()) {
            if (klazz == Number.class) {
                cachedClass = new NumberCachedClass(klazz, classInfo);
            } else if (klazz == Integer.class || klazz ==  Integer.TYPE) {
                cachedClass = new IntegerCachedClass(klazz, classInfo, klazz==Integer.class);
            } else if (klazz == Double.class || klazz == Double.TYPE) {
                cachedClass = new DoubleCachedClass(klazz, classInfo, klazz==Double.class);
            } else if (klazz == BigDecimal.class) {
                cachedClass = new BigDecimalCachedClass(klazz, classInfo);
            } else if (klazz == Long.class || klazz == Long.TYPE) {
                cachedClass = new LongCachedClass(klazz, classInfo, klazz==Long.class);
            } else if (klazz == Float.class || klazz == Float.TYPE) { 
                cachedClass = new FloatCachedClass(klazz, classInfo, klazz==Float.class);
            } else if (klazz == Short.class || klazz == Short.TYPE) {
                cachedClass = new ShortCachedClass(klazz, classInfo, klazz==Short.class);
            } else if (klazz == Boolean.TYPE) {
                cachedClass = new BooleanCachedClass(klazz, classInfo, false);
            } else if (klazz == Character.TYPE) {
                cachedClass = new CharacterCachedClass(klazz, classInfo, false);
            } else if (klazz == BigInteger.class) {
                cachedClass = new BigIntegerCachedClass(klazz, classInfo);
            } else if (klazz == Byte.class || klazz == Byte.TYPE) {
                cachedClass = new ByteCachedClass(klazz, classInfo, klazz==Byte.class);
            } else {
                cachedClass = new CachedClass(klazz, classInfo);
            }
        } else {
            if (klazz.getName().charAt(0) == '[')
              cachedClass = new ArrayCachedClass(klazz, classInfo);
            else if (klazz == Boolean.class) {
                cachedClass = new BooleanCachedClass(klazz, classInfo, true);
            } else if (klazz == Character.class) {
                cachedClass = new CharacterCachedClass(klazz, classInfo, true);
            } else if (Closure.class.isAssignableFrom(klazz)) {
                cachedClass = new CachedClosureClass (klazz, classInfo);
            } else {
                cachedClass = new CachedClass(klazz, classInfo);
            }
        }
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
              perInstanceMetaClassMap = new ManagedConcurrentMap(ReferenceBundle.getWeakBundle()); 

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

    public static class ClassInfoSet extends ManagedConcurrentMap<Class,ClassInfo> {
        public ClassInfoSet(ReferenceBundle bundle) {
            super(bundle);
        }

        protected Segment createSegment(Object segmentInfo,  int cap) {
            ReferenceBundle bundle = (ReferenceBundle) segmentInfo;
            if (bundle==null) throw new IllegalArgumentException("bundle must not be null ");

            return new Segment(bundle, cap);
        }

        static final class Segment extends ManagedConcurrentMap.Segment<Class,ClassInfo> {
            Segment(ReferenceBundle bundle, int initialCapacity) {
                super(bundle, initialCapacity);
            }

            protected ClassInfo createEntry(Class key, int hash, ClassInfo unused) {
                return new ClassInfo(this, key, hash);
            }
        }
    }

    private static final class LocalMap extends HashMap<Class,ClassInfo> {

        private static final int CACHE_SIZE = 5;

        // We use a PhantomReference or a WeakReference for the Thread
        // because the ThreadLocal manages a map with the thread as key.
        // If we make a strong reference to the thread here, then it is 
        // possible, that the map cannot be cleaned. If the number of 
        // threads is not limited, then this map may consume too much memory
        // This reference here is unmanaged (queue==null) because if the map 
        // key gets collected, the reference will too. 
        private final PhantomReference<Thread> myThread = new PhantomReference(Thread.currentThread(),null);

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

    private static class ThreadLocalMapHandler extends ThreadLocal<SoftReference<LocalMap>> {
        SoftReference<LocalMap> recentThreadMapRef;
        
        protected SoftReference<LocalMap> initialValue() {
            return new SoftReference(new LocalMap(),null);
        }

        public SoftReference<LocalMap> get() {
            SoftReference<LocalMap> mapRef = recentThreadMapRef;
            LocalMap recent = null;
            if (mapRef!=null) recent = mapRef.get();
            // we don't need to handle myThread.get()==null, because in that
            // case the thread has been collected, meaning the entry for the
            // thread is invalid anyway, so it is valid if recent has a 
            // different value. 
            if (recent != null && recent.myThread.get() == Thread.currentThread()) {
                return mapRef;
            } else {
                SoftReference<LocalMap> ref = super.get();
                recentThreadMapRef = ref;
                return ref;
            }
        }
    }
    
    private static final WeakReference<ThreadLocalMapHandler> localMapRef;
    static {
        ThreadLocalMapHandler localMap = new ThreadLocalMapHandler();
        localMapRef = new WeakReference<ThreadLocalMapHandler>(localMap,null);
    }

    private static class LazyCachedClassRef extends LazyReference<CachedClass> {
        private final ClassInfo info;

        LazyCachedClassRef(ReferenceBundle bundle, ClassInfo info) {
            super(bundle);
            this.info = info;
        }

        public CachedClass initValue() {
            return createCachedClass(info.get(), info);
        }
    }

    private static class LazyClassLoaderRef extends LazyReference<ClassLoaderForClassArtifacts> {
        private final ClassInfo info;

        LazyClassLoaderRef(ReferenceBundle bundle, ClassInfo info) {
            super(bundle);
            this.info = info;
        }

        public ClassLoaderForClassArtifacts initValue() {
            return new ClassLoaderForClassArtifacts(info.get());
        }
    }

    private static class DebugRef extends ManagedReference<Class> {
        public static final boolean debug = false;

        private static final AtomicInteger count = new AtomicInteger();

        final String name;

        public DebugRef(Class klazz) {
            super(softBundle, klazz);
            name = klazz == null ? "<null>" : klazz.getName();
            count.incrementAndGet();
        }

        public void finalizeRef() {
            System.out.println(name + " unloaded " + count.decrementAndGet() + " classes kept");
            super.finalizeReference();
        }
    }
}
