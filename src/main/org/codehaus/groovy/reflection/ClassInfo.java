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
import org.codehaus.groovy.reflection.stdclasses.*;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;

/**
 * It is handle for all information we want to keep about the class
 *
 * @author Alex.Tkachman
 */
public class ClassInfo extends SoftReference<Class> {

    private Class strongClass;

    private MetaClass strongMetaClass;

    private SoftReference<CachedClass> cachedClassRef;

    private SoftReference<MetaClass> weakMetaClass;

    private static final Object NONE = new Object();

    private volatile int version;

    private ExpandoMetaClass modifiedExpando;

    private final LazySoftReference staticMetaClassField = new LazySoftReference() {
        public Object initValue() {
            final CachedClass aClass = getCachedClass(ClassInfo.this.get());

            final CachedField[] cachedFields = aClass.getFields();
            for (CachedField cachedField : cachedFields) {
                if (cachedField.getName().startsWith("$staticMetaClass") && cachedField.getType() == SoftReference.class && cachedField.isStatic()) {
                    return cachedField;
                }
            }

            return NONE;
        }
    };

    private static final HashSet<ClassInfo> modifiedExpandos = new HashSet<ClassInfo>();

    private final LockableObject lock = new LockableObject();

    public final int hash;
    public ClassInfo next;

    public ClassInfo(Class klazz, int hash, ClassInfo next) {
        super (klazz);
        this.next = next;
        this.hash = hash;
    }

    public ClassInfo(ClassInfo src, ClassInfo next) {
        this(src.get(), src.hash, next);
        cachedClassRef = src.cachedClassRef;
        weakMetaClass = src.weakMetaClass;
        strongMetaClass = src.strongMetaClass;
        strongClass = src.strongClass;
        version = src.version;
        modifiedExpando = src.modifiedExpando;

        CachedClass cc;
        if (cachedClassRef != null && (cc = cachedClassRef.get()) != null)
          cc.classInfo = this;
    }

    public int getVersion() {
        return version;
    }

    public ExpandoMetaClass getModifiedExpando() {
        return modifiedExpando;
    }

    public void setModifiedExpando(ExpandoMetaClass modifiedExpando) {
        this.modifiedExpando = modifiedExpando;
        if (modifiedExpando != null)
          modifiedExpandos.add (this);
    }

    public static void clearModifiedExpandos() {
        for (ClassInfo info : modifiedExpandos) {
            info.setModifiedExpando(null);
        }
        modifiedExpandos.clear();
    }

    public CachedClass getCachedClass(Class klazz) {
        SoftReference<CachedClass> ccr;
        CachedClass cc;
        if ((ccr = cachedClassRef) != null && (cc = ccr.get()) != null)
          return cc;

        lock.lock();
        try {
            if ((ccr = cachedClassRef) != null && (cc = ccr.get()) != null)
              return cc;

            cc = createCachedClass(klazz, this);
            cachedClassRef = new SoftReference (cc);
            return cc;
        }
        finally {
            lock.unlock();
        }
    }

    private static ClassSet globalClassSet = new ClassSet ();

    public static ClassInfo getClassInfo (Class cls) {
        return globalClassSet.get(cls);
    }

    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    public void setStrongMetaClass(MetaClass answer) {
        version++;

        this.strongMetaClass = answer;
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

    public static class ClassSet {

        static final int MAXIMUM_CAPACITY = 1 << 30;
        static final int MAX_SEGMENTS = 1 << 16;
        static final int RETRIES_BEFORE_LOCK = 2;

        final int segmentMask;
        final int segmentShift;
        final Segment[] segments;

        public ClassSet () {

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
    }
}
