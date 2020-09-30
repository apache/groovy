/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.reflection;

import groovy.lang.Closure;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.ExpandoMetaClassCreationHandle;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap;
import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;
import org.codehaus.groovy.reflection.stdclasses.ArrayCachedClass;
import org.codehaus.groovy.reflection.stdclasses.BigDecimalCachedClass;
import org.codehaus.groovy.reflection.stdclasses.BigIntegerCachedClass;
import org.codehaus.groovy.reflection.stdclasses.BooleanCachedClass;
import org.codehaus.groovy.reflection.stdclasses.ByteCachedClass;
import org.codehaus.groovy.reflection.stdclasses.CachedClosureClass;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.reflection.stdclasses.CharacterCachedClass;
import org.codehaus.groovy.reflection.stdclasses.DoubleCachedClass;
import org.codehaus.groovy.reflection.stdclasses.FloatCachedClass;
import org.codehaus.groovy.reflection.stdclasses.IntegerCachedClass;
import org.codehaus.groovy.reflection.stdclasses.LongCachedClass;
import org.codehaus.groovy.reflection.stdclasses.NumberCachedClass;
import org.codehaus.groovy.reflection.stdclasses.ObjectCachedClass;
import org.codehaus.groovy.reflection.stdclasses.ShortCachedClass;
import org.codehaus.groovy.reflection.stdclasses.StringCachedClass;
import org.codehaus.groovy.util.Finalizable;
import org.codehaus.groovy.util.LazyReference;
import org.codehaus.groovy.util.LockableObject;
import org.codehaus.groovy.util.ManagedConcurrentLinkedQueue;
import org.codehaus.groovy.util.ManagedReference;
import org.codehaus.groovy.util.ReferenceBundle;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle for all information we want to keep about the class
 * <p>
 * This class handles caching internally and its advisable to not store
 * references directly to objects of this class.  The static factory method
 * {@link ClassInfo#getClassInfo(Class)} should be used to retrieve an instance
 * from the cache.  Internally the {@code Class} associated with a {@code ClassInfo}
 * instance is kept as {@link WeakReference}, so it not safe to reference
 * and instance without the Class being either strongly or softly reachable.
 */
public class ClassInfo implements Finalizable {

    private final LazyCachedClassRef cachedClassRef;
    private final LazyClassLoaderRef artifactClassLoader;
    private final LockableObject lock = new LockableObject();
    public final int hash = -1;
    private final WeakReference<Class<?>> classRef;
    private final AtomicInteger version = new AtomicInteger();
    private MetaClass strongMetaClass;
    private ManagedReference<MetaClass> weakMetaClass;
    MetaMethod[] dgmMetaMethods = MetaMethod.EMPTY_ARRAY;
    MetaMethod[] newMetaMethods = MetaMethod.EMPTY_ARRAY;
    private ManagedIdentityConcurrentMap<Object, MetaClass> perInstanceMetaClassMap;

    private static final ReferenceBundle softBundle = ReferenceBundle.getSoftBundle();
    private static final ReferenceBundle weakBundle = ReferenceBundle.getWeakBundle();

    private static final ManagedConcurrentLinkedQueue<ClassInfo> modifiedExpandos =
            new ManagedConcurrentLinkedQueue<ClassInfo>(weakBundle);

    private static final GroovyClassValue<ClassInfo> globalClassValue = GroovyClassValueFactory.createGroovyClassValue(new ComputeValue<ClassInfo>(){
        @Override
        public ClassInfo computeValue(Class<?> type) {
            ClassInfo ret = new ClassInfo(type);
            globalClassSet.add(ret);
            return ret;
        }
    });

    private static final GlobalClassSet globalClassSet = new GlobalClassSet();

    ClassInfo(Class klazz) {
        this.classRef = new WeakReference<Class<?>>(klazz);
        cachedClassRef = new LazyCachedClassRef(softBundle, this);
        artifactClassLoader = new LazyClassLoaderRef(softBundle, this);
    }

    public int getVersion() {
        return version.get();
    }

    public void incVersion() {
        version.incrementAndGet();
        VMPluginFactory.getPlugin().invalidateCallSites();
    }

    public ExpandoMetaClass getModifiedExpando() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        MetaClass strongRef = strongMetaClass;
        return strongRef == null ? null : strongRef instanceof ExpandoMetaClass ? (ExpandoMetaClass)strongRef : null;
    }

    public static void clearModifiedExpandos() {
        for (Iterator<ClassInfo> itr = modifiedExpandos.iterator(); itr.hasNext(); ) {
            ClassInfo info = itr.next();
            itr.remove();
            info.setStrongMetaClass(null);
        }
    }

    /**
     * Returns the {@code Class} associated with this {@code ClassInfo}.
     * <p>
     * This method can return {@code null} if the {@code Class} is no longer reachable
     * through any strong or soft references.  A non-null return value indicates that this
     * {@code ClassInfo} is valid.
     *
     * @return the {@code Class} associated with this {@code ClassInfo}, else {@code null}
     */
    public final Class<?> getTheClass() {
        return classRef.get();
    }

    public CachedClass getCachedClass() {
        return cachedClassRef.get();
    }

    public ClassLoaderForClassArtifacts getArtifactClassLoader() {
        return artifactClassLoader.get();
    }

    public static ClassInfo getClassInfo (Class cls) {
        return globalClassValue.get(cls);
    }

    /**
     * Removes a {@code ClassInfo} from the cache.
     *
     * This is useful in cases where the Class is parsed from a script, such as when
     * using GroovyClassLoader#parseClass, and is executed for its result but the Class
     * is not retained or cached.  Removing the {@code ClassInfo} associated with the Class
     * will make the Class and its ClassLoader eligible for garbage collection sooner that
     * it would otherwise.
     *
     * @param cls the Class associated with the ClassInfo to remove
     *            from cache
     */
    public static void remove(Class<?> cls) {
        globalClassValue.remove(cls);
    }

    public static Collection<ClassInfo> getAllClassInfo () {
        return getAllGlobalClassInfo();
    }

    public static void onAllClassInfo(ClassInfoAction action) {
        for (ClassInfo classInfo : getAllGlobalClassInfo()) {
            action.onClassInfo(classInfo);
        }
    }

    private static Collection<ClassInfo> getAllGlobalClassInfo() {
        return globalClassSet.values();
    }

    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    public void setStrongMetaClass(MetaClass answer) {
        version.incrementAndGet();

        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        MetaClass strongRef = strongMetaClass;

        if (strongRef instanceof ExpandoMetaClass) {
            ((ExpandoMetaClass)strongRef).inRegistry = false;
            for (Iterator<ClassInfo> itr = modifiedExpandos.iterator(); itr.hasNext(); ) {
                ClassInfo info = itr.next();
                if(info == this) {
                    itr.remove();
                }
            }
        }

        strongMetaClass = answer;

        if (answer instanceof ExpandoMetaClass) {
            ((ExpandoMetaClass)answer).inRegistry = true;
            modifiedExpandos.add(this);
        }

        replaceWeakMetaClassRef(null);
    }

    public MetaClass getWeakMetaClass() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        ManagedReference<MetaClass> weakRef = weakMetaClass;
        return weakRef == null ? null : weakRef.get();
    }

    public void setWeakMetaClass(MetaClass answer) {
        version.incrementAndGet();

        strongMetaClass = null;
        ManagedReference<MetaClass> newRef = null;
        if (answer != null) {
            newRef = new ManagedReference<MetaClass> (softBundle,answer);
        }
        replaceWeakMetaClassRef(newRef);
    }

    private void replaceWeakMetaClassRef(ManagedReference<MetaClass> newRef) {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        ManagedReference<MetaClass> weakRef = weakMetaClass;
        if (weakRef != null) {
            weakRef.clear();
        }
        weakMetaClass = newRef;
    }

    public MetaClass getMetaClassForClass() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        MetaClass strongMc = strongMetaClass;
        if (strongMc!=null) return strongMc;
        MetaClass weakMc = getWeakMetaClass();
        if (isValidWeakMetaClass(weakMc)) {
            return weakMc;
        }
        return null;
    }

    private MetaClass getMetaClassUnderLock() {
        MetaClass answer = getStrongMetaClass();
        if (answer!=null) return answer;

        answer = getWeakMetaClass();
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        MetaClassRegistry.MetaClassCreationHandle mccHandle = metaClassRegistry.getMetaClassCreationHandler();

        if (isValidWeakMetaClass(answer, mccHandle)) {
            return answer;
        }

        answer = mccHandle.create(classRef.get(), metaClassRegistry);
        answer.initialize();

        if (GroovySystem.isKeepJavaMetaClasses()) {
            setStrongMetaClass(answer);
        } else {
            setWeakMetaClass(answer);
        }
        return answer;
    }

    private static boolean isValidWeakMetaClass(MetaClass metaClass) {
        return isValidWeakMetaClass(metaClass, GroovySystem.getMetaClassRegistry().getMetaClassCreationHandler());
    }

    /**
     * if EMC.enableGlobally() is OFF, return whatever the cached answer is.
     * but if EMC.enableGlobally() is ON and the cached answer is not an EMC, come up with a fresh answer
     */
    private static boolean isValidWeakMetaClass(MetaClass metaClass, MetaClassRegistry.MetaClassCreationHandle mccHandle) {
        if(metaClass==null) return false;
        boolean enableGloballyOn = (mccHandle instanceof ExpandoMetaClassCreationHandle);
        boolean cachedAnswerIsEMC = (metaClass instanceof ExpandoMetaClass);
        return (!enableGloballyOn || cachedAnswerIsEMC);
    }

    /**
     * Returns the {@code MetaClass} for the {@code Class} associated with this {@code ClassInfo}.
     * If no {@code MetaClass} exists one will be created.
     * <p>
     * It is not safe to call this method without a {@code Class} associated with this {@code ClassInfo}.
     * It is advisable to aways retrieve a ClassInfo instance from the cache by using the static
     * factory method {@link ClassInfo#getClassInfo(Class)} to ensure the referenced Class is
     * strongly reachable.
     *
     * @return a {@code MetaClass} instance
     */
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
        return getMetaClass();
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
            if (klazz.isArray())
              cachedClass = new ArrayCachedClass(klazz, classInfo);
            else if (klazz == Boolean.class) {
                cachedClass = new BooleanCachedClass(klazz, classInfo, true);
            } else if (klazz == Character.class) {
                cachedClass = new CharacterCachedClass(klazz, classInfo, true);
            } else if (Closure.class.isAssignableFrom(klazz)) {
                cachedClass = new CachedClosureClass (klazz, classInfo);
            } else if (isSAM(klazz)) {
                cachedClass = new CachedSAMClass(klazz, classInfo);
            } else {
                cachedClass = new CachedClass(klazz, classInfo);
            }
        }
        return cachedClass;
    }

    private static boolean isSAM(Class<?> c) {
        return CachedSAMClass.getSAMMethod(c) !=null;
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

        return perInstanceMetaClassMap.get(obj);
    }

    public void setPerInstanceMetaClass(Object obj, MetaClass metaClass) {
        version.incrementAndGet();

        if (metaClass != null) {
            if (perInstanceMetaClassMap == null)
              perInstanceMetaClassMap = new ManagedIdentityConcurrentMap<>();

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

    private static class LazyCachedClassRef extends LazyReference<CachedClass> {
        private static final long serialVersionUID = -1400274148849287400L;
        private final ClassInfo info;

        LazyCachedClassRef(ReferenceBundle bundle, ClassInfo info) {
            super(bundle);
            this.info = info;
        }

        @Override
        public CachedClass initValue() {
            return createCachedClass(info.classRef.get(), info);
        }
    }

    private static class LazyClassLoaderRef extends LazyReference<ClassLoaderForClassArtifacts> {
        private static final long serialVersionUID = 1639196133085420609L;
        private final ClassInfo info;

        LazyClassLoaderRef(ReferenceBundle bundle, ClassInfo info) {
            super(bundle);
            this.info = info;
        }

        @Override
        public ClassLoaderForClassArtifacts initValue() {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoaderForClassArtifacts>) () -> new ClassLoaderForClassArtifacts(info.classRef.get()));
        }
    }

    @Override
    public void finalizeReference() {
        setStrongMetaClass(null);
        cachedClassRef.clear();
        artifactClassLoader.clear();
    }

    private static class GlobalClassSet {

        private final ManagedConcurrentLinkedQueue<ClassInfo> items = new ManagedConcurrentLinkedQueue<ClassInfo>(weakBundle);

        public int size(){
            return values().size();
        }

        public int fullSize(){
            return values().size();
        }

        public Collection<ClassInfo> values(){
            return items.values();
        }

        public void add(ClassInfo value){
            items.add(value);
        }

    }

    public interface ClassInfoAction {
        void onClassInfo(ClassInfo classInfo);
    }
}
