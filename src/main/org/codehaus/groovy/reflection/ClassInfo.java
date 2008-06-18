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

import groovy.lang.ExpandoMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.stdclasses.*;
import org.codehaus.groovy.util.LazySoftReference;
import org.codehaus.groovy.util.LockableObject;
import org.codehaus.groovy.util.FinalizableRef;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Handle for all information we want to keep about the class
 *
 * @author Alex.Tkachman
 */
public class ClassInfo extends FinalizableRef.SoftRef<Class> {

    private final LazyCachedClassRef cachedClassRef;

    private MetaClass strongMetaClass;

    private SoftReference<MetaClass> weakMetaClass;

    private static final Object NONE = new Object();

    private volatile int version;

    private final LazySoftReference staticMetaClassField;

    private final LazyClassLoaderRef artifactClassLoader;

    private static final HashSet<ClassInfo> modifiedExpandos = new HashSet<ClassInfo>();

    private final LockableObject lock = new LockableObject();

    MetaMethod[] dgmMetaMethods = CachedClass.EMPTY;

    MetaMethod[] newMetaMethods = CachedClass.EMPTY;

    public final int hash;
    public ClassInfo next;
    private InstanceMap perInstanceMetaClassMap;

    ClassInfo(Class klazz, int hash, ClassInfo next) {
        super (klazz);
        new DebugRef(klazz);
        this.next = next;
        this.hash = hash;
        cachedClassRef = new LazyCachedClassRef(this);
        staticMetaClassField = new LazyStaticMetaClassFieldRef(this);
        artifactClassLoader = new LazyClassLoaderRef(this);
    }

    ClassInfo(ClassInfo src, ClassInfo next) {
        super(src.get());
        this.next = next;
        this.hash = src.hash;
        version = src.version;

        cachedClassRef = new LazyCachedClassRef(this, src);
        staticMetaClassField = new LazyStaticMetaClassFieldRef(this, src);
        artifactClassLoader = new LazyClassLoaderRef(this, src);
        weakMetaClass = src.weakMetaClass;
        strongMetaClass = src.strongMetaClass;
        if (strongMetaClass instanceof ExpandoMetaClass) {
            modifiedExpandos.remove(src);
            modifiedExpandos.add(this);
        }
        dgmMetaMethods = src.dgmMetaMethods;
        newMetaMethods = src.newMetaMethods;
        perInstanceMetaClassMap = src.perInstanceMetaClassMap;
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

        if (strongMetaClass instanceof ExpandoMetaClass)
          modifiedExpandos.remove(this);

        strongMetaClass = answer;

        if (strongMetaClass instanceof ExpandoMetaClass)
          modifiedExpandos.add(this);

        weakMetaClass = null;
        updateMetaClass();
    }

    private void updateMetaClass() {
        final Object smf = staticMetaClassField.get();
        if (smf != null && smf != NONE)
          ((CachedField)smf).setProperty(null,null);
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

        updateMetaClass();
    }

    public MetaClass getMetaClassForClass() {
        return strongMetaClass != null ? strongMetaClass : weakMetaClass == null ? null : weakMetaClass.get();
    }

    public static int size () {
        return globalClassSet.size();
    }

    public static int fullSize () {
        return globalClassSet.fullSize();
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
              perInstanceMetaClassMap = new InstanceMap ();

            perInstanceMetaClassMap.put(obj, metaClass);
        }
        else {
            if (perInstanceMetaClassMap != null) {
              perInstanceMetaClassMap.remove(obj);
              if (perInstanceMetaClassMap.isEmpty())
                perInstanceMetaClassMap = null;
            }
        }
    }

    public boolean hasPerInstanceMetaClasses () {
        return perInstanceMetaClassMap != null;
    }

    public void finalizeRef() {
        globalClassSet.segmentFor(hash).clean(this);
    }

    public static class ClassInfoSet {

        static final int MAXIMUM_CAPACITY = 1 << 30;
        static final int MAX_SEGMENTS = 1 << 16;
        static final int RETRIES_BEFORE_LOCK = 2;

        final int segmentMask;
        final int segmentShift;
        final Segment[] segments;

        public ClassInfoSet() {

            int sshift = 0;
            int ssize = 1;
            while (ssize < 16) {
                ++sshift;
                ssize <<= 1;
            }
            segmentShift = 32 - sshift;
            segmentMask = ssize - 1;
            this.segments = new Segment[ssize];

            int c = 512 / ssize;
            if (c * ssize < 512)
                ++c;
            int cap = 1;
            while (cap < c)
                cap <<= 1;

            for (int i = 0; i < this.segments.length; ++i)
                this.segments[i] = new Segment(cap);
        }

        static int hash(Class x) {
            int h = x.getName().hashCode();
            h += ~(h << 9);
            h ^=  (h >>> 14);
            h +=  (h << 4);
            h ^=  (h >>> 10);
            return h;
        }

        final Segment segmentFor(int hash) {
            return segments[(hash >>> segmentShift) & segmentMask];
        }

        static final class Segment extends LockableObject {

            volatile int count;

            int threshold;

            volatile ClassInfo[] table;

            Segment(int initialCapacity) {
                setTable(new ClassInfo[initialCapacity]);
            }

            void setTable(ClassInfo[] newTable) {
                threshold = (int)(newTable.length * 0.75f);
                table = newTable;
            }

            ClassInfo getFirst(int hash) {
                ClassInfo[] tab = table;
                return tab[hash & (tab.length - 1)];
            }

            ClassInfo get(Class key, int hash) {
                ClassInfo e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && e.get() == key) {
                        return e;
                    }
                    e = e.next;
                }
                return put (key, hash);
            }

            ClassInfo put(Class key, int hash) {
                lock();
                try {
                    int c = count;
                    if (c++ > threshold) {
                        rehash();
                    }

                    ClassInfo[] tab = table;
                    int index = hash & (tab.length - 1);
                    ClassInfo first = tab[index];
                    ClassInfo e = first;
                    while (e != null) {
                        if (e.hash == hash && e.get() == key) {
                            return e;
                        }
                        e = e.next;
                    }

                    e = new ClassInfo(key, hash, first);
                    tab[index] = e;
                    count = c; // write-volatile
                    return e;
                } finally {
                    unlock();
                }
            }

            void rehash() {
                ClassInfo[] oldTable = table;
                int oldCapacity = oldTable.length;
                if (oldCapacity >= MAXIMUM_CAPACITY)
                    return;

                int newCount = 0;
                for (int i = 0; i < oldCapacity ; i++) {
                    ClassInfo first = null;
                    for (ClassInfo e = oldTable[i]; e != null; ) {
                       if (e.get() != null) {
                           if (first == null)
                             first = e;

                           ClassInfo ee = e.next;
                           while (ee != null && ee.get() == null)
                             ee = ee.next;
                           e.next = ee;
                           e = ee;
                           newCount++;
                       }
                       else {
                         e = e.next;
                       }
                    }

                    oldTable [i] = first;
                }

                if (newCount+1 < threshold) {
                    count = newCount;
                    return;
                }

                ClassInfo[] newTable = new ClassInfo[oldCapacity << 1];
                int sizeMask = newTable.length - 1;
                newCount = 0;
                for (int i = 0; i < oldCapacity ; i++) {
                    for (ClassInfo e = oldTable[i]; e != null; e = e.next) {
                       int idx = e.hash & sizeMask;
                       final ClassInfo next = newTable[idx];
                       if (next == null && e.next == null)
                         newTable[idx] = e;
                       else
                         newTable[idx] = new ClassInfo(e, next);
                       newCount++;
                    }
                }

                threshold = (int)(newTable.length * 0.75f);

                table = newTable;
                count = newCount;
            }

            public void clean(ClassInfo classInfo) {
                lock ();
                try {
                    ClassInfo[] oldTable = table;
                    ClassInfo first = null;
                    int index = classInfo.hash & (oldTable.length - 1);
                    for (ClassInfo e = oldTable[index]; e != null; ) {
                       if (e.get() != null) {
                           if (first == null)
                             first = e;

                           ClassInfo ee = e.next;
                           while (ee != null && ee.get() == null)
                             ee = ee.next;
                           e.next = ee;
                           e = ee;
//                           newCount++;
                       }
                       else {
                         e = e.next;
                       }
                    }

                    oldTable [index] = first;
                }
                finally {
                    unlock();
                }
            }
        }

        public int fullSize() {
            int count = 0;
            for (int i = 0; i < segments.length; i++) {
                for (int j = 0; j < segments[i].table.length; j++) {
                    for (ClassInfo e = segments[i].table[j]; e != null; e = e.next)
                        count++;
                }
            }
            return count;
        }

        public int size() {
            int count = 0;
            for (int i = 0; i < segments.length; i++) {
                for (int j = 0; j < segments[i].table.length; j++) {
                    for (ClassInfo e = segments[i].table[j]; e != null; e = e.next)
                      if (e.get() != null)
                        count++;
                }
            }
            return count;
        }

        public ClassInfo get(Class key) {
            int hash = hash(key);
            return segmentFor(hash).get(key, hash);
        }

        public ClassInfo get(Class key, int hash) {
            return segmentFor(hash).get(key, hash);
        }
    }

    private static class LocalMap extends HashMap<Class,ClassInfo> {

        private static final int CACHE_SIZE = 5;

        public final Thread myThread = Thread.currentThread();

        private int nextCacheEntry;

        private final ClassInfo[] cache = new ClassInfo[CACHE_SIZE];
        private static final ClassInfo NOINFO = new ClassInfo(null,0,null);

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

            return putToCache(globalClassSet.get(key));
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

        LazyCachedClassRef(ClassInfo info, ClassInfo src) {
            this.info = info;
            final CachedClass cc = src.cachedClassRef.getNullable();
            if (cc != null) {
                cc.classInfo = info;
                set (cc);
            }
        }

        public CachedClass initValue() {
            return createCachedClass(info.get(), info);
        }

        protected void finalizeRef() {
            super.finalizeRef();
        }
    }

    private static class LazyStaticMetaClassFieldRef extends LazySoftReference {
        private final ClassInfo info;

        LazyStaticMetaClassFieldRef(ClassInfo info) {
            this.info = info;
        }

        LazyStaticMetaClassFieldRef(ClassInfo info, ClassInfo src) {
            this.info = info;
            final Object cc = src.staticMetaClassField.getNullable();
            if (cc != null) {
                set(cc);
            }
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

        LazyClassLoaderRef(ClassInfo info, ClassInfo src) {
            this.info = info;
            final ClassLoaderForClassArtifacts cc = src.artifactClassLoader.getNullable();
            if (cc != null) {
                set (cc);
            }
        }

        public ClassLoaderForClassArtifacts initValue() {
            return new ClassLoaderForClassArtifacts(info.get());
        }
    }

    // TODO: custom map would be better here - get rid of InstanceRef
    // we can't use WeakHashMap because it use both == and equals for comparision, which is too agressive
    // we need == only
    private static class InstanceMap extends WeakHashMap {
    }

    private static class DebugRef extends FinalizableRef.PhantomRef {
        final String name;
        public DebugRef(Class klazz) {
            super(klazz);
            name = klazz == null ? "<null>" : klazz.getName();
        }

        public void finalizeRef() {
            System.out.println(name + " unloaded");
        }
    }

}
