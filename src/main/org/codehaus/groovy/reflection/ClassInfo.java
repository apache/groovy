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

import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.stdclasses.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alex.Tkachman
 */
public class ClassInfo extends LockableObject {

    private volatile MetaClass strongMetaClass;
    private volatile SoftReference<CachedClass> cachedClassRef;
    private volatile SoftReference<MetaClass> weakMetaClass;

    private static final Object NONE = new Object();

    private final LazyReference staticMetaClassField = new LazyReference() {
        public Object initValue() {
            final CachedClass aClass = getCachedClass(getCls());

            final CachedField[] cachedFields = aClass.getFields();
            for (CachedField cachedField : cachedFields) {
                if (cachedField.getName().startsWith("$staticMetaClass") && cachedField.getType() == MetaClass.class && cachedField.isStatic()) {
                    return cachedField;
                }
            }

            return NONE;
        }
    };

    private int version;
    private boolean modifiedExpando;

    public ClassInfo(Class klazz, GlobalMultiClassInfoRecord mcir) {
        final CachedClass aClass = createCachedClass(klazz, this);
        cachedClassRef = new SoftRef<CachedClass> (aClass, mcir);
    }

    public int getVersion() {
        return version;
    }

    public boolean isModifiedExpando() {
        return modifiedExpando;
    }

    public void setModifiedExpando(boolean modifiedExpando) {
        this.modifiedExpando = modifiedExpando;
    }

    public static void clearModifiedExpandos() {
    }

    private static class SoftRef<T> extends SoftReference<T> {
        private static ReferenceQueue queue = new ReferenceQueue();

        private GlobalMultiClassInfoRecord mcir;

        public SoftRef(T referent, GlobalMultiClassInfoRecord mcir) {
            super(referent, queue);
            this.mcir = mcir;
        }

        public void clear() {
            super.clear();
            mcir.optimize();
            mcir = null;
        }

        public static void expungeStaleEntries() {
            SoftRef ref;
            while ((ref=(SoftRef)queue.poll())!=null) {
                ref.clear();
            }
        }
    }

    public Class getCls () {
        if (strongMetaClass != null)
          return strongMetaClass.getTheClass();

        SoftReference<CachedClass> ccr;
        CachedClass cc;
        if ((ccr = cachedClassRef) != null && (cc = ccr.get()) != null)
          return cc.getTheClass();

        SoftReference<MetaClass> mcr;
        MetaClass mc;
        if ((mcr = weakMetaClass) != null && (mc = mcr.get()) != null)
          return mc.getTheClass();

        return null;
    }

    public CachedClass getCachedClass(Class klazz) {
        SoftReference<CachedClass> ccr;
        CachedClass cc;
        if ((ccr = cachedClassRef) != null && (cc = ccr.get()) != null)
          return cc;

        lock ();
        try {
            if ((ccr = cachedClassRef) != null && (cc = ccr.get()) != null)
              return cc;

            cc = createCachedClass(klazz, this);
            cachedClassRef = new SoftReference<CachedClass> (cc);
            return cc;
        }
        finally {
            unlock();
        }
    }

    private static final GlobalClassMap globalClassMap = new GlobalClassMap();

    public static ClassInfo getClassInfo (Class cls) {
        return localClassMap.get().getClassInfo (cls);
    }

    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    public void setStrongMetaClass(MetaClass answer) {
        version++;

        this.strongMetaClass = answer;
        weakMetaClass = null;

        updateMetaClass(answer);
    }

    private void updateMetaClass(MetaClass answer) {
        final Object smf = staticMetaClassField.get();
        if (smf != NONE)
          ((CachedField)smf).setProperty(null,answer);

        modifiedExpando = false;
    }

    public MetaClass getWeakMetaClass() {
        return weakMetaClass == null ? null : weakMetaClass.get();
    }

    public void setWeakMetaClass(MetaClass answer) {
        version++;

        strongMetaClass = null;
        if (answer == null) {
           weakMetaClass = null;
        }else {
           weakMetaClass = new SoftReference<MetaClass> (answer);
        }

        updateMetaClass(answer);
    }

    public MetaClass getMetaClassForClass() {
        return strongMetaClass != null ? strongMetaClass : weakMetaClass == null ? null : weakMetaClass.get();
    }

    public static int size () {
        int count = 0;
        for (GlobalMultiClassInfoRecord infoRecord : globalClassMap.values()) {
            count += infoRecord.size ();
        }

        return count;
    }

    public static int fullSize () {
        int count = 0;
        for (GlobalMultiClassInfoRecord infoRecord : globalClassMap.values()) {
            count += infoRecord.fullSize ();
        }

        return count;
    }

    private static class GlobalClassMap extends ConcurrentHashMap<String, GlobalMultiClassInfoRecord> {

        ClassInfo getClassInfo (Class cls) {
            final String name = cls.getName();
            GlobalMultiClassInfoRecord infoRecord = get(name);
            if (infoRecord == null) {
                GlobalMultiClassInfoRecord newRecord = new GlobalMultiClassInfoRecord();
                infoRecord = putIfAbsent(name, newRecord);
                if (infoRecord == null) {
                    infoRecord = newRecord;
                }
            }

            return infoRecord.get(cls);
        }
    }

    private static class LocalClassMap extends HashMap<String,LocalMultiClassInfoRecord> {

        private static final int CACHE_SIZE = 5;
        private final ClassInfo cache [] = new ClassInfo [CACHE_SIZE];
        private int nextCacheEntry;

        ClassInfo getClassInfo (Class cls) {

            for (int i = 0; i != CACHE_SIZE; ++i) {
                final ClassInfo info = cache[i];
                if (info != null && info.getCls() == cls)
                  return info;
            }

            final String name = cls.getName();
            LocalMultiClassInfoRecord infoRecord = get(name);
            if (infoRecord == null) {
                infoRecord = new LocalMultiClassInfoRecord();
                put(name, infoRecord);
            }

            final ClassInfo info = infoRecord.getIfPresent(infoRecord.data, cls);
            if (info != null) {
                return putToCache(info);
            }

            return putToCache(infoRecord.put(globalClassMap.getClassInfo(cls)));
        }

        private ClassInfo putToCache(ClassInfo info) {
            cache [nextCacheEntry++] = info;
            if (nextCacheEntry == CACHE_SIZE)
              nextCacheEntry = 0;
            return info;
        }
    }

    public static class LocalMultiClassInfoRecord {
        /**
         * Either null or ClassInfo or ClassInfo []
         */
        private Object data;

        ClassInfo put(ClassInfo ci) {
            if (data == null) {
              data = ci;
              return ci;
            }

            if (data instanceof ClassInfo) {
                data = new ClassInfo[] { ci, (ClassInfo)data };
                return ci;
            }

            ClassInfo d [] = (ClassInfo[])data;
            final ClassInfo [] resArr = new ClassInfo[d.length+1];
            resArr [0] = ci;
            System.arraycopy(d, 0, resArr, 1, d.length);
            this.data = resArr;

            return ci;
        }

        private ClassInfo getIfPresent(Object data, Class klazz) {
            if (data == null) {
              return null;
            }

            if (data instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) data;
                if (classInfo.getCls () == klazz)
                  return classInfo;

                return null;
            }

            ClassInfo d [] = (ClassInfo[]) data;
            for (ClassInfo classInfo : d) {
                if (classInfo.getCls () == klazz)
                    return classInfo;
            }

            return null;
        }
    }

    public static class GlobalMultiClassInfoRecord extends LockableObject {
        /**
         * Either null or ClassInfo or ClassInfo []
         */
        private volatile Object data;

        public ClassInfo get (Class klazz) {
            final ClassInfo info = getUnlocked(data, klazz);
            if (info != null)
              return info;

            return getLocked(klazz);
        }

        private ClassInfo getLocked(Class klazz) {
            SoftRef.expungeStaleEntries();

            lock ();
            try {
                return getOrPutWithoutLock(klazz);
            }
            finally {
                unlock();
            }
        }

        private ClassInfo getOrPutWithoutLock(Class klazz) {
            final Object data = this.data;
            if (data == null) {
                final ClassInfo res = new ClassInfo(klazz, this);
                this.data = res;
                return res;
            }

            if (data instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) data;
                final Class cls = classInfo.getCls();
                if (cls == klazz)
                  return classInfo;

                if (cls == null) {
                    final ClassInfo res = new ClassInfo(klazz, this);
                    this.data = res;
                    return res;
                }

                ClassInfo [] d = new ClassInfo[2];
                final ClassInfo res = new ClassInfo(klazz, this);
                d [0] = res;
                d [1] = classInfo;
                this.data = d;
                return res;
            }

            ClassInfo d [] = (ClassInfo[]) data;
            for (ClassInfo classInfo : d) {
                final Class cls = classInfo.getCls();
                if (cls == klazz) {
                    return classInfo;
                }
            }

            final ClassInfo [] resArr = new ClassInfo[d.length+1];
            final ClassInfo res = new ClassInfo(klazz, this);
            resArr [0] = res;
            System.arraycopy(d, 0, resArr, 1, d.length);
            this.data = resArr;
            return res;
        }

        private void optimize() {
            lock ();
            try {
                if (data == null)
                  return;

                if (data instanceof ClassInfo) {
                    ClassInfo classInfo = (ClassInfo) data;
                    if (classInfo.getCls() == null)
                       data = null;
                    return;
                }

                ClassInfo d [] = (ClassInfo[])data;
                ArrayList<ClassInfo> list = new ArrayList<ClassInfo> (d.length);
                for (ClassInfo classInfo : d) {
                    final Class cls = classInfo.getCls();
                    if (cls != null)
                        list.add(classInfo);
                }

                if (list.size() == 0)
                  this.data = null;
                else
                    if (list.size() == 1)
                      this.data = list.get(0);
                    else
                      this.data = list.toArray(new ClassInfo[list.size()]);
            }
            finally {
                unlock();
            }
        }

        private ClassInfo getUnlocked(Object data, Class klazz) {
            if (data == null) {
              return null;
            }

            if (data instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) data;
                if (classInfo.getCls () == klazz)
                  return classInfo;

                return null;
            }

            ClassInfo d [] = (ClassInfo[]) data;
            for (ClassInfo classInfo : d) {
                if (classInfo.getCls () == klazz)
                    return classInfo;
            }

            return null;
        }

        public int size() {
            Object data = this.data;
            if (data == null) {
              return 0;
            }

            if (data instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) data;
                return classInfo.getCls() != null ? 1 : 0;
            }

            ClassInfo d [] = (ClassInfo[]) data;
            int count = 0;
            for (ClassInfo classInfo : d)
              count += classInfo.getCls() != null ? 1 : 0;

            return count;
        }

        public int fullSize() {
            Object data = this.data;
            if (data == null) {
              return 0;
            }

            if (data instanceof ClassInfo) {
                return 1;
            }

            ClassInfo d [] = (ClassInfo[]) data;
            int count = 0;
            count += d.length;

            return count;
        }
    }

    private static final ThreadLocal<LocalClassMap> localClassMap = new ThreadLocal<LocalClassMap> () {
        private final Thread myThread = Thread.currentThread();
        private final LocalClassMap myMap = new LocalClassMap();

        protected LocalClassMap initialValue() {
            if (Thread.currentThread() == myThread)
              return myMap;
            else
              return new LocalClassMap();
        }

        public LocalClassMap get() {
            if (Thread.currentThread() == myThread)
              return myMap;
            else
              return super.get();
        }
    };

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
}
