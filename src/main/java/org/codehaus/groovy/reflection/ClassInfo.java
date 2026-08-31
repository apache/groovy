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
     * The class-level indy SwitchPoint domain: one domain per class, covering
     * the pre-MetaClass link window and every installed MetaClass generation.
     * Owned here so exact-class invalidation can always reach the domain — in
     * particular after a soft/weak MetaClass object has been collected while
     * linked guards remain installed. Retired (and re-allocated on next link)
     * whenever the MetaClass state of this class changes.
     */
    private final SwitchPointInvalidator indySwitchPointDomain = new SwitchPointInvalidator();

    /**
     * Whether {@link #indySwitchPointDomain} has its reclaim anchor installed
     * (see {@link IndyInvalidation#anchorClassDomain}). Set on first link so
     * classes that never install an indy MOP guard pay nothing. The benign
     * race (two threads anchoring) just creates a second idempotent reclaim.
     */
    private volatile boolean indyDomainAnchored;
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

    /**
     * Whether {@link #globalClassValue} can collect a ClassInfo while its
     * class is still alive ({@code -Dgroovy.use.classvalue=soft}, GROOVY-12281
     * investigation prototype). Reclaimable values need two cooperating pieces
     * here: ephemeron pinning of instances carrying non-reconstructible state
     * ({@link #updateReclaimability()}) and per-Class indy domain continuity
     * (see {@link #indyDomain()}).
     */
    private static final boolean RECLAIMABLE_CLASS_VALUES = globalClassValue.valuesReclaimable();

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
     * Bumps {@link #getVersion()} and retires the current class-domain
     * SwitchPoint generation so linked sites re-select against the new
     * MetaClass state. Retiring an unallocated generation is a no-op, so this
     * is safe (and cheap) for first installs as well as replace/clear. When
     * the change also fires a MetaClass registry event, the listener applies
     * width policy ({@link IndyInvalidation#invalidateForMetaClassChange}) —
     * often a second detach of an already-empty domain (idempotent).
     */
    private void bumpGenerationLocal() {
        version.incrementAndGet();
        // Local domain ownership: width policy is IndyInvalidation's job.
        invalidateIndySwitchPoint();
    }

    /**
     * Resolves the SwitchPoint domain all indy guard operations act on.
     * <p>
     * Default mode: the instance-owned {@link #indySwitchPointDomain}, whose
     * lifetime equals this ClassInfo's — sound because the strong ClassValue
     * keeps the instance alive as long as its class.
     * <p>
     * Reclaimable values (GROOVY-12281, soft mode): the per-Class domain from
     * {@link IndyInvalidation#classDomainFor}, seeded with the local domain on
     * first touch. Domain identity then survives ClassInfo recreation: a
     * successor instance adopts its predecessor's domain, so a MetaClass
     * change applied through the successor deterministically retires guards
     * that were linked under the predecessor (POJO direct-dispatch guards
     * capture only the SwitchPoint invoker, never this instance, so they can
     * outlive it — the lazy {@code DomainReclaim} pump alone would leave an
     * unbounded stale-guard window). When the class itself has been collected
     * no receiver can reach any guard, so the local domain suffices.
     */
    private SwitchPointInvalidator indyDomain() {
        if (RECLAIMABLE_CLASS_VALUES) {
            Class<?> type = getTheClass();
            if (type != null) {
                return IndyInvalidation.classDomainFor(type, indySwitchPointDomain);
            }
        }
        return indySwitchPointDomain;
    }

    /**
     * Returns the SwitchPoint for monomorphic indy MOP guards on this class:
     * the current generation of the class-level domain, which covers both the
     * pre-MetaClass link window and installed MetaClass generations.
     *
     * @return the class-domain SwitchPoint
     * @since 6.0.0
     */
    @Internal
    public SwitchPoint getIndySwitchPoint() {
        SwitchPointInvalidator domain = indyDomain();
        if (!indyDomainAnchored) {
            if (RECLAIMABLE_CLASS_VALUES) {
                // Reclaimable values retire domains when the Class dies, not
                // when a ClassInfo instance does: instances are replaceable
                // (their successor adopts the same domain), classes are not.
                Class<?> type = getTheClass();
                if (type != null) {
                    IndyInvalidation.anchorClassDomainToClass(type, domain);
                }
            } else {
                IndyInvalidation.anchorClassDomain(this, indySwitchPointDomain);
            }
            indyDomainAnchored = true;
        }
        return domain.getSwitchPoint();
    }

    /**
     * Invalidates this class's domain SwitchPoint without bumping
     * {@link #getVersion()}. Prefer {@link #incVersion()} when the MetaClass
     * actually changed.
     *
     * @since 6.0.0
     */
    @Internal
    public void invalidateIndySwitchPoint() {
        indyDomain().invalidate();
    }

    /**
     * Detaches this class's live domain SwitchPoint (if any) into {@code out}.
     * Does not create a MetaClass.
     *
     * @param out destination list (must not be {@code null})
     * @since 6.0.0
     */
    @Internal
    public void collectLiveIndySwitchPoints(final List<SwitchPoint> out) {
        SwitchPoint live = indyDomain().detachLive();
        if (live != null) {
            out.add(live);
        }
    }

    /**
     * Detaches this class's live domain SwitchPoint. The caller must
     * invalidate any non-null return value.
     *
     * @return the detached SwitchPoint, or {@code null} if none was live
     * @since 6.0.0
     */
    @Internal
    public SwitchPoint detachLiveIndySwitchPoint() {
        return indyDomain().detachLive();
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
        // A hard detach drops the whole association — including any pin the
        // detached instance held there — so undeploy semantics need no
        // store-specific handling.
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
     * Increments the version number, retires the class-domain SwitchPoint
     * generation, and manages the ExpandoMetaClass registry.
     *
     * @param answer the metaclass to set, or {@code null} to clear
     */
    public void setStrongMetaClass(MetaClass answer) {
        // Version always; the class-domain SwitchPoint generation is retired so
        // sites linked against the previous MetaClass state re-select (a no-op
        // when no generation was allocated). Exact-class (stock) or bulk
        // (custom) width policy is applied by registry listeners / incVersion.
        bumpGenerationLocal();

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
        updateReclaimability();
    }

    /**
     * Reclaimability bookkeeping (GROOVY-12281): while this ClassInfo carries
     * state that could not be reconstructed after collection — an installed
     * class-level MetaClass, per-instance MetaClasses, or registry-written
     * DGM/extension method arrays — it is pinned <em>inside its own
     * association</em> ({@link GroovyClassValue#pin}), so it lives exactly as
     * long as its class: an immortal platform key retains it (it must — the
     * state is not reconstructible), while a dropped script class releases it
     * together with its loader. A global strong root would get the second half
     * wrong, extending dirty script classes (and their loaders) to the
     * runtime's lifetime — the "reverse" leak raised in review of PR #2820.
     * The DGM condition also enforces the registry-rooting invariant by
     * construction rather than by audit: any instance holding non-empty MOP
     * arrays is pinned, so a recreated instance never needs to rebuild them.
     * Removal is conservative: lingering weak entries in the per-instance map
     * merely delay unpinning, which is the safe direction (today's default
     * retains every ClassInfo for its class's lifetime). No-op unless the
     * value store reclaims values ({@link GroovyClassValue#valuesReclaimable}).
     */
    void updateReclaimability() {
        Class<?> type = getTheClass();
        if (type == null) return;
        if (strongMetaClass != null
                || (perInstanceMetaClassMap != null && !perInstanceMetaClassMap.isEmpty())
                || dgmMetaMethods.length != 0
                || newMetaMethods.length != 0) {
            globalClassValue.pin(type, this);
        } else {
            globalClassValue.unpin(type, this);
        }
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
     * Clears the strong metaclass, increments the version number, and retires
     * the class-domain SwitchPoint generation.
     *
     * @param answer the metaclass to set, or {@code null} to clear
     */
    public void setWeakMetaClass(MetaClass answer) {
        bumpGenerationLocal();

        strongMetaClass = null;
        ManagedReference<MetaClass> newRef = null;
        if (answer != null) {
            newRef = new ManagedReference<MetaClass> (weakBundle,answer);
        }
        replaceWeakMetaClassRef(newRef);
        updateReclaimability();
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
        // sites that may observe instance-level dispatch re-link.
        bumpGenerationLocal();

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
        updateReclaimability();
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
