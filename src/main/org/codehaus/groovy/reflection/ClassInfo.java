package org.codehaus.groovy.reflection;

import groovy.lang.MetaClass;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClassInfo extends ReentrantLock {

    private MetaClass strongMetaClass;
    SoftReference<CachedClass> cachedClassRef;
    SoftReference<MetaClass> weakMetaClass;

    public ClassInfo(Class klazz, MultiClassInfoRecord mcir) {
        cachedClassRef = new SoftRef<CachedClass> (createCachedClass(klazz), mcir);
    }

    private static class SoftRef<T> extends SoftReference<T> {
        private static ReferenceQueue queue = new ReferenceQueue();

        private MultiClassInfoRecord mcir;

        public SoftRef(T referent, MultiClassInfoRecord mcir) {
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
          return cc.getCachedClass();

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

            cc = createCachedClass(klazz);
            cachedClassRef = new SoftReference<CachedClass> (cc);
            return cc;
        }
        finally {
            unlock();
        }
    }

    private static CachedClass createCachedClass(Class klazz) {
        if (klazz == Object.class)
            return ReflectionCache.OBJECT_CLASS;

        if (klazz == String.class)
            return ReflectionCache.STRING_CLASS;

        CachedClass cachedClass;
        if (Number.class.isAssignableFrom(klazz) || klazz.isPrimitive()) {
            if (klazz == Number.class)
              cachedClass = new CachedClass.NumberCachedClass(klazz);
            else
                if (klazz == Integer.class || klazz == Integer.TYPE)
                  cachedClass = new CachedClass.IntegerCachedClass(klazz);
                else
                  if (klazz == Double.class || klazz == Double.TYPE )
                    cachedClass = new CachedClass.DoubleCachedClass(klazz);
                  else
                      if (klazz == BigDecimal.class )
                        cachedClass = new CachedClass.BigDecimalCachedClass(klazz);
                      else
                          if (klazz == Long.class || klazz == Long.TYPE)
                            cachedClass = new CachedClass.LongCachedClass(klazz);
                          else
                              if (klazz == Float.class || klazz == Float.TYPE)
                                cachedClass = new CachedClass.FloatCachedClass(klazz);
                              else
                                  if (klazz == Short.class || klazz == Short.TYPE)
                                    cachedClass = new CachedClass.ShortCachedClass(klazz);
                                  else
                                      if (klazz == Boolean.TYPE)
                                        cachedClass = new CachedClass.BooleanCachedClass(klazz);
                                      else
                                          if (klazz == Character.TYPE)
                                            cachedClass = new CachedClass.CharacterCachedClass(klazz);
                                          else
                                              if (klazz == BigInteger.class)
                                                cachedClass = new CachedClass.BigIntegerCachedClass(klazz);
                                              else
                                                if (klazz == Byte.class)
                                                  cachedClass = new CachedClass.ByteCachedClass(klazz);
                                                else
                                                  cachedClass = new CachedClass(klazz);
        }
        else
            if (klazz.getName().charAt(0) == '[')
              cachedClass = new CachedClass.ArrayCachedClass (klazz);
            else
                if (klazz == Boolean.class)
                  cachedClass = new CachedClass.BooleanCachedClass(klazz);
                else
                    if (klazz == Character.class)
                      cachedClass = new CachedClass.CharacterCachedClass(klazz);
                    else
                      cachedClass = new CachedClass(klazz);

        return cachedClass;
    }

    private static ClassMap classMap = new ClassMap();

    public static ClassInfo getClassInfo (Class cls) {
        return classMap.getClassInfo (cls);
    }

    public void setStrongMetaClass(MetaClass strongMetaClass) {
        this.strongMetaClass = strongMetaClass;
        weakMetaClass = null;
    }

    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    public MetaClass getWeakMetaClass() {
        return weakMetaClass == null ? null : weakMetaClass.get();
    }

    
    public MetaClass getMetaClassForClass() {
        return strongMetaClass != null ? strongMetaClass : weakMetaClass == null ? null : weakMetaClass.get();
    }



    public void setWeakMetaClass(MetaClass answer) {
        strongMetaClass = null;
        if (answer == null) {
           weakMetaClass = null;
        }else {
           weakMetaClass = new SoftReference<MetaClass> (answer);
        }
    }

    public static int size () {
        int count = 0;
        for (MultiClassInfoRecord infoRecord : classMap.values()) {
            count += infoRecord.size ();
        }

        return count;
    }

    public static int fullSize () {
        int count = 0;
        for (MultiClassInfoRecord infoRecord : classMap.values()) {
            count += infoRecord.fullSize ();
        }

        return count;
    }

    private static class ClassMap extends ConcurrentHashMap<String,MultiClassInfoRecord> {

        MultiClassInfoRecord DUMMY = new MultiClassInfoRecord();
        Lock lock = new ReentrantLock();

        ClassInfo getClassInfo (Class cls) {
            final String name = cls.getName();
            MultiClassInfoRecord infoRecord = putIfAbsent(name, DUMMY);
            if (infoRecord == null || infoRecord == DUMMY) {
                SoftRef.expungeStaleEntries();

                MultiClassInfoRecord newRecord = new MultiClassInfoRecord();
                infoRecord = putIfAbsent(name, newRecord);
                if (infoRecord == DUMMY) {
                    lock.lock();
                    try {
                       put (name, newRecord);
                       infoRecord = newRecord;
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }

            return infoRecord.get(cls);
        }
    }

    public static class MultiClassInfoRecord extends ReentrantLock{
        /**
         * Either ClassInfo or ClassInfo []
         */
        private Object data;

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
            finally {
                unlock();
            }
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
            for (ClassInfo classInfo : d)
              count += d.length;

            return count;
        }
    }
}
