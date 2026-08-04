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
import groovy.transform.Internal;
import org.apache.groovy.runtime.indy.IndyInvalidation;
import org.apache.groovy.runtime.indy.SwitchPointInvalidator;
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

import java.io.Serial;
import java.lang.invoke.SwitchPoint;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle for all information we want to keep about the class
 * <p>
 * This class handles caching internally and it's advisable to not store
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
    /**
     * Pending indy domain used only while no class-level MetaClass is installed
     * (defineClass / pre-MC link). Retired on first MetaClass install. Once a
     * MetaClass exists, domains live in {@link IndyInvalidation}'s MetaClass
     * identity map only — this field is not used for post-MC generations.
     */
    private final SwitchPointInvalidator pendingIndySwitchPoint =
            IndyInvalidation.newPendingInvalidator();
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
        /**
         * Creates a new {@code ClassInfo} for the given class type.
         * Initializes cache and registers the class information in the global set.
         *
         * @param type the class to create information for
         * @return the newly created {@code ClassInfo}
         */
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

    /**
     * Returns the version number of this class information.
     * The version increments when the metaclass is modified (e.g., methods/properties added).
     *
     * @return the current version
     */
    public int getVersion() {
        return version.get();
    }

    /**
     * Increments the version number and invalidates this class's MetaClass
     * SwitchPoint domain (exact class only). Called when metaclass modifications
     * occur (e.g., adding methods to an {@code ExpandoMetaClass}).
     * <p>
     * <strong>Behavioral change in 6.0 (GROOVY-12191):</strong> this method no longer
     * triggers a process-wide call-site flush. It scopes invalidation to this class
     * only. Subtype sites are not retired: stock missing-method / missing-property
     * hierarchy walks re-resolve live on the dynamic route (see
     * {@link IndyInvalidation}). Callers that previously relied on
     * {@code incVersion()} as a global “flush everything” hammer should invoke
     * {@link org.codehaus.groovy.vmplugin.VMPlugin#invalidateCallSites()} (or
     * {@link IndyInvalidation#invalidateCategory()}) explicitly when a bulk flush
     * is required. Unrelated classes keep their optimized targets.
     * <p>
     * Category enter/leave still bulk-invalidates MetaClass SwitchPoints via
     * {@link org.codehaus.groovy.vmplugin.VMPlugin#invalidateCallSites()}.
     * SwitchPoint policy is owned by {@link IndyInvalidation}; this method only
     * bumps generation then delegates exact-class invalidation.
     */
    public void incVersion() {
        version.incrementAndGet();
        Class<?> type = getTheClass();
        if (type != null) {
            IndyInvalidation.invalidateClass(type);
        } else {
            invalidateIndySwitchPoint();
        }
    }

    /**
     * Bumps {@link #getVersion()} and retires SwitchPoint domains as needed.
     * <ul>
     *   <li>First MetaClass install: retire pending domain only (sites linked
     *       pre-MC re-select onto the MetaClass domain).</li>
     *   <li>Replace/clear: retire this ClassInfo’s domains locally. When the
     *       change also fires a MetaClass registry event, the listener applies
     *       width policy ({@link IndyInvalidation#invalidateForMetaClassChange})
     *       — often a second detach of an already-empty domain (idempotent).</li>
     * </ul>
     */
    private void bumpGenerationLocal(final boolean retireSwitchPoint) {
        version.incrementAndGet();
        if (retireSwitchPoint) {
            // Local domain ownership: width policy is IndyInvalidation's job.
            invalidateIndySwitchPoint();
        } else {
            // First MetaClass install — retire pending pre-MC domain only.
            SwitchPoint pending = pendingIndySwitchPoint.detachLive();
            SwitchPointInvalidator.invalidateIfLive(pending);
        }
    }

    /**
     * Whether this class currently has a class-level MetaClass (strong or weak).
     */
    private boolean hasClassLevelMetaClass() {
        if (strongMetaClass != null) {
            return true;
        }
        ManagedReference<MetaClass> weakRef = weakMetaClass;
        return weakRef != null && weakRef.get() != null;
    }

    /**
     * Returns the SwitchPoint for monomorphic indy MOP guards on this class
     * (GROOVY-12191). Delegates to {@link IndyInvalidation#classSwitchPointFor}.
     *
     * @return MetaClass domain if installed, otherwise pending domain
     * @since 6.0.0
     */
    @Internal
    public SwitchPoint getIndySwitchPoint() {
        Class<?> type = getTheClass();
        if (type == null) {
            return getPendingIndySwitchPoint();
        }
        return IndyInvalidation.classSwitchPointFor(type);
    }

    /**
     * Pending domain for pre-MetaClass link. Live SwitchPoint allocated lazily.
     *
     * @return pending SwitchPoint
     * @since 6.0.0
     */
    @Internal
    public SwitchPoint getPendingIndySwitchPoint() {
        return pendingIndySwitchPoint.getSwitchPoint();
    }

    /**
     * Invalidates this class's class-level MetaClass domain and pending domain
     * without bumping {@link #getVersion()}. Prefer {@link #incVersion()} when
     * the MetaClass actually changed.
     *
     * @since 6.0.0
     */
    @Internal
    public void invalidateIndySwitchPoint() {
        List<SwitchPoint> batch = new ArrayList<>(2);
        collectLiveIndySwitchPoints(batch);
        if (!batch.isEmpty()) {
            // AOT-safe: stamp always advances; real invalidateAll only on a JVM
            org.apache.groovy.runtime.indy.AotDispatch.invalidateAll(batch.toArray(new SwitchPoint[0]));
        }
    }

    /**
     * Detaches live SwitchPoint(s) for this class into {@code out}: the installed
     * MetaClass domain (if any) and the pending domain (if live). Does not create
     * a MetaClass.
     *
     * @param out destination list (must not be {@code null})
     * @since 6.0.0
     */
    @Internal
    public void collectLiveIndySwitchPoints(final List<SwitchPoint> out) {
        IndyInvalidation.collectLiveForMetaClass(getMetaClassForClass(), out);
        SwitchPoint pending = pendingIndySwitchPoint.detachLive();
        if (pending != null) {
            out.add(pending);
        }
    }

    /**
     * Detaches live SwitchPoint(s) for this class. Prefer
     * {@link #collectLiveIndySwitchPoints} for bulk paths.
     *
     * @return one detached SwitchPoint, or {@code null}; additional live domains
     *         are invalidated immediately so they are not orphaned
     * @since 6.0.0
     */
    @Internal
    public SwitchPoint detachLiveIndySwitchPoint() {
        List<SwitchPoint> batch = new ArrayList<>(2);
        collectLiveIndySwitchPoints(batch);
        if (batch.isEmpty()) {
            return null;
        }
        SwitchPoint first = batch.get(0);
        for (int i = 1; i < batch.size(); i++) {
            SwitchPointInvalidator.invalidateIfLive(batch.get(i));
        }
        return first;
    }

    /**
     * Returns the modified {@code ExpandoMetaClass} for this class, if one exists.
     *
     * @return the modified expando metaclass, or {@code null} if not modified or not an ExpandoMetaClass
     */
    public ExpandoMetaClass getModifiedExpando() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        MetaClass strongRef = strongMetaClass;
        return strongRef == null ? null : strongRef instanceof ExpandoMetaClass ? (ExpandoMetaClass)strongRef : null;
    }

    /**
     * Clears all modified {@code ExpandoMetaClass} instances for all classes.
     * Removes strong references to metaclasses and disassociates them from their class information.
     */
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

    /**
     * Returns the cached class representation for this class.
     * Lazily initializes on first access.
     *
     * @return the cached class
     */
    public CachedClass getCachedClass() {
        return cachedClassRef.get();
    }

    /**
     * Returns the class loader for loading class artifacts (e.g., compiled bytecode).
     * Lazily initializes on first access.
     *
     * @return the artifact class loader
     */
    public ClassLoaderForClassArtifacts getArtifactClassLoader() {
        return artifactClassLoader.get();
    }

    /**
     * Retrieves the {@code ClassInfo} for the given class.
     * Lazily creates one if not already cached.
     *
     * @param cls the class to get information for
     * @return the class information, or {@code null} if {@code cls} is {@code null}
     */
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

    /**
     * Returns all cached class information across the runtime.
     *
     * @return a collection of all cached class info objects
     */
    public static Collection<ClassInfo> getAllClassInfo () {
        return getAllGlobalClassInfo();
    }

    /**
     * Executes the given action on all cached class information.
     * Allows processing of all classes currently tracked by the runtime.
     *
     * @param action the action to execute on each ClassInfo
     */
    public static void onAllClassInfo(ClassInfoAction action) {
        for (ClassInfo classInfo : getAllGlobalClassInfo()) {
            action.onClassInfo(classInfo);
        }
    }

    private static Collection<ClassInfo> getAllGlobalClassInfo() {
        return globalClassSet.values();
    }

    /**
     * Returns the strong (immutable) metaclass for this class, if one has been set.
     * A strong reference keeps the metaclass in memory regardless of garbage collection.
     *
     * @return the strong metaclass, or {@code null} if not set
     */
    public MetaClass getStrongMetaClass() {
        return strongMetaClass;
    }

    /**
     * Sets the strong (immutable) metaclass for this class.
     * Increments the version number and manages ExpandoMetaClass registry.
     * <p>
     * On replacement or clear, also invalidates this class's indy SwitchPoint
     * (GROOVY-12191). First install ({@code null →} MC) only bumps version;
     * exact-class invalidation for the registry event is applied by MetaClass
     * registry listeners or {@link #incVersion()}.
     *
     * @param answer the metaclass to set, or {@code null} to clear
     */
    public void setStrongMetaClass(MetaClass answer) {
        // Version always; SwitchPoint when replacing/clearing an established MC.
        // Exact-class (stock) or bulk (custom) policy is applied by registry
        // listeners / incVersion (GROOVY-12191). First install (null → MC) only
        // bumps version.
        boolean replaceOrClear = hasClassLevelMetaClass() || answer == null;
        bumpGenerationLocal(replaceOrClear);

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

    /**
     * Returns the weak (mutable) metaclass for this class, if one has been set.
     * A weak reference allows garbage collection of the metaclass when no longer needed.
     *
     * @return the weak metaclass, or {@code null} if not set or if it has been garbage collected
     */
    public MetaClass getWeakMetaClass() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        ManagedReference<MetaClass> weakRef = weakMetaClass;
        return weakRef == null ? null : weakRef.get();
    }

    /**
     * Sets the weak (mutable) metaclass for this class.
     * Clears the strong metaclass and increments the version number.
     * <p>
     * On replacement or clear, also invalidates this class's indy SwitchPoint
     * (GROOVY-12191). First install only bumps version.
     *
     * @param answer the metaclass to set, or {@code null} to clear
     */
    public void setWeakMetaClass(MetaClass answer) {
        // Version always; SwitchPoint when replacing/clearing (GROOVY-12191).
        boolean replaceOrClear = hasClassLevelMetaClass() || answer == null;
        bumpGenerationLocal(replaceOrClear);

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

    /**
     * Returns the most appropriate metaclass for this class.
     * Prefers strong metaclass if available, then weak metaclass if valid, otherwise the default.
     *
     * @return the metaclass for this class
     */
    public MetaClass getMetaClassForClass() {
        // safe value here to avoid multiple reads with possibly
        // differing values due to concurrency
        MetaClass strongMc = strongMetaClass;
        if (strongMc!=null) return strongMc;
        MetaClass weakMc = getWeakMetaClass();
        // During GroovySystem bootstrap the registry is not yet published;
        // treat a non-null weak MC as valid rather than querying the handler.
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        if (registry == null) {
            return weakMc;
        }
        if (isValidWeakMetaClass(weakMc, registry.getMetaClassCreationHandler())) {
            return weakMc;
        }
        return null;
    }

    /**
     * Creates or returns the class-level MetaClass while holding {@link #lock}.
     * Callers must already hold the lock ({@link #getMetaClass()}).
     * <p>
     * Mirrors {@link #getMetaClassForClass()} bootstrap safety: when
     * {@link GroovySystem#getMetaClassRegistry()} is not yet published, a
     * cached weak MetaClass is returned if present; otherwise creation is
     * impossible and an {@link IllegalStateException} is thrown rather than
     * an NPE on a null registry (Sonar javabugs:S2259 / PR #2736).
     */
    private MetaClass getMetaClassUnderLock() {
        MetaClass answer = getStrongMetaClass();
        if (answer != null) {
            return answer;
        }

        answer = getWeakMetaClass();
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        // Same bootstrap window as getMetaClassForClass: registry may be null
        // while GroovySystem is still initializing.
        if (metaClassRegistry == null) {
            if (answer != null) {
                return answer;
            }
            throw new IllegalStateException(
                    "MetaClassRegistry is not yet initialized; cannot create MetaClass for "
                            + getTheClass());
        }
        MetaClassRegistry.MetaClassCreationHandle mccHandle =
                metaClassRegistry.getMetaClassCreationHandler();

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

    /**
     * if EMC.enableGlobally() is OFF, return whatever the cached answer is.
     * but if EMC.enableGlobally() is ON and the cached answer is not an EMC, come up with a fresh answer
     */
    private static boolean isValidWeakMetaClass(MetaClass metaClass, MetaClassRegistry.MetaClassCreationHandle mccHandle) {
        if (metaClass == null) {
            return false;
        }
        boolean enableGloballyOn = (mccHandle instanceof ExpandoMetaClassCreationHandle);
        boolean cachedAnswerIsEMC = (metaClass instanceof ExpandoMetaClass);
        return (!enableGloballyOn || cachedAnswerIsEMC);
    }

    /**
     * Returns the {@code MetaClass} for the {@code Class} associated with this {@code ClassInfo}.
     * If no {@code MetaClass} exists one will be created.
     * <p>
     * It is not safe to call this method without a {@code Class} associated with this {@code ClassInfo}.
     * It is advisable to always retrieve a ClassInfo instance from the cache by using the static
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

    /**
     * Returns the metaclass for an object.
     * If the object has a per-instance metaclass, returns that; otherwise returns this class's metaclass.
     *
     * @param obj the object to get the metaclass for
     * @return the metaclass for the object
     */
    public MetaClass getMetaClass(Object obj) {
        final MetaClass instanceMetaClass = getPerInstanceMetaClass(obj);
        if (instanceMetaClass != null)
            return instanceMetaClass;
        return getMetaClass();
    }

    /**
     * Returns the number of cached class information entries.
     *
     * @return the count of cached ClassInfo instances
     */
    public static int size () {
        return globalClassSet.size();
    }

    /**
     * Returns the total size including soft-referenced (potentially garbage-collectable) entries.
     *
     * @return the full count of class information entries
     */
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

    /**
     * Acquires a lock for this class information.
     * Used to synchronize modifications to metaclass and per-instance metaclass maps.
     */
    public void lock () {
        lock.lock();
    }

    /**
     * Releases the lock for this class information.
     */
    public void unlock () {
        lock.unlock();
    }

    /**
     * Returns the per-instance metaclass for the given object, if one has been set.
     *
     * @param obj the object to get the per-instance metaclass for
     * @return the per-instance metaclass, or {@code null} if not set
     */
    public MetaClass getPerInstanceMetaClass(Object obj) {
        if (perInstanceMetaClassMap == null)
          return null;

        return perInstanceMetaClassMap.get(obj);
    }

    /**
     * Sets the per-instance metaclass for the given object.
     * Per-instance metaclasses override the class-level metaclass for that specific object.
     *
     * @param obj the object to associate with a metaclass
     * @param metaClass the metaclass to set, or {@code null} to remove the association
     */
    public void setPerInstanceMetaClass(Object obj, MetaClass metaClass) {
        // Per-instance MetaClass changes always retire the class SwitchPoint so
        // sites that may observe instance-level dispatch re-link (GROOVY-12191).
        bumpGenerationLocal(true);

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

    /**
     * Returns whether this class has any per-instance metaclasses associated with its objects.
     *
     * @return {@code true} if one or more per-instance metaclasses have been set; {@code false} otherwise
     */
    public boolean hasPerInstanceMetaClasses () {
        return perInstanceMetaClassMap != null;
    }

    private static class LazyCachedClassRef extends LazyReference<CachedClass> {
        @Serial
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
        @Serial
        private static final long serialVersionUID = 1639196133085420609L;
        private final ClassInfo info;

        LazyClassLoaderRef(ReferenceBundle bundle, ClassInfo info) {
            super(bundle);
            this.info = info;
        }

        @Override
        public ClassLoaderForClassArtifacts initValue() {
            return new ClassLoaderForClassArtifacts(info.classRef.get());
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

    /**
     * Functional interface for performing actions on {@code ClassInfo} instances.
     */
    public interface ClassInfoAction {
        /**
         * Performs an action on the given {@code ClassInfo}.
         *
         * @param classInfo the class information to act upon
         */
        void onClassInfo(ClassInfo classInfo);
    }
}
