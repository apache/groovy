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
package org.apache.groovy.runtime.indy;

import groovy.lang.AdaptingMetaClass;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistryChangeEvent;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.util.ManagedReference;
import org.codehaus.groovy.util.ReferenceBundle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scoped invokedynamic {@link SwitchPoint} invalidation for the Groovy MOP
 * (GROOVY-12191).
 * <p>
 * <b>Domain:</b> one SwitchPoint domain per {@link Class}, owned by its
 * {@link ClassInfo} and covering every MetaClass generation of that class
 * (pre-MetaClass link included). The domain outlives the MetaClass
 * <em>object</em>, so exact-class invalidation still reaches installed guards
 * after a soft/weak MetaClass has been collected. Monomorphic indy sites
 * install a single {@code guardWithTest} on the class-level domain observed
 * at link time.
 *
 * <h2>Width policy (two axes only)</h2>
 * <ul>
 *   <li><b>Exact class</b> — {@link #invalidateClass(Class)}: stock
 *       {@code MetaClassImpl} / EMC changes (including {@link ClassInfo#incVersion()},
 *       registry replace, per-instance MetaClass).</li>
 *   <li><b>All loaded</b> — process-wide retire used for category enter/leave
 *       ({@link #invalidateCategory()}), unattributed registry events
 *       ({@link #invalidateUnscoped()}), and non-stock custom MetaClass kinds
 *       ({@link #invalidateBulk()}).</li>
 * </ul>
 * There is <em>no</em> parent→child SwitchPoint fan-out. Indy selection consults
 * only the receiver’s own MetaClass tables; ancestor-dependent resolution on
 * the missing-method / missing-property path re-enters
 * {@code MetaClass.invokeMethod} / property miss and walks the hierarchy live
 * from the registry each call. Construction-time snapshots of ancestor expando
 * methods are pre-existing MOP behaviour and are not refreshed by SwitchPoint
 * retirement. See also {@link MetaClassImpl#findMethodInClassHierarchy}.
 * <p>
 * Optional stats: {@code -Dgroovy.indy.invalidation.stats=true}.
 * Production guards: {@code IndyInterface.applyMopSwitchPoints}; tests may use
 * {@link #guardWithMopSwitchPoints}.
 *
 * @since 6.0.0
 */
public final class IndyInvalidation {

    private static final Logger LOG = Logger.getLogger(IndyInvalidation.class.getName());
    private static final boolean STATS_LOG = SystemUtil.getBooleanSafe("groovy.indy.invalidation.stats");

    /** Exact-class invalidation events ({@link #invalidateClass(Class)}). */
    private static final AtomicLong CLASS_INVALIDATIONS = new AtomicLong();
    /** Category enter/leave bulk events ({@link #invalidateCategory()}). */
    private static final AtomicLong CATEGORY_INVALIDATIONS = new AtomicLong();
    /**
     * Non-category process-wide bulk events
     * ({@link #invalidateBulk()} / {@link #invalidateUnscoped()}).
     */
    private static final AtomicLong BULK_INVALIDATIONS = new AtomicLong();

    private static final SwitchPoint[] EMPTY_SWITCH_POINTS = new SwitchPoint[0];

    private IndyInvalidation() {
    }

    /**
     * Anchors a reclaim reference for a ClassInfo-owned class domain: when the
     * ClassInfo (and with it the class) becomes unreachable while the domain
     * still has a live registered SwitchPoint, the reference is delivered by
     * the shared {@link ReferenceBundle weak-bundle} manager and the domain is
     * retired — installed guards keep only the SwitchPoint's internal invoker
     * reachable, so retiring the orphan forces straggler sites to re-link,
     * which is always safe.
     * <p>
     * Reachability: the live-SwitchPoint registry holds the invalidator
     * strongly, and the invalidator holds the reclaim reference, so cleanup
     * stays reachable exactly as long as there is something to clean; once the
     * domain is retired the whole chain becomes collectable.
     *
     * @param owner  the ClassInfo owning the domain (must not be {@code null})
     * @param domain the class-level domain (must not be {@code null})
     */
    public static void anchorClassDomain(final ClassInfo owner, final SwitchPointInvalidator domain) {
        domain.setReclaimAnchor(new DomainReclaim(owner, domain));
    }

    /**
     * Weak reference to a domain's owning ClassInfo whose collection retires
     * the domain. Processed by the shared weak-bundle {@code ReferenceManager},
     * which is pumped by managed-reference creation across the runtime.
     */
    private static final class DomainReclaim extends ManagedReference<ClassInfo> {
        private final SwitchPointInvalidator domain;

        DomainReclaim(final ClassInfo owner, final SwitchPointInvalidator domain) {
            super(ReferenceBundle.getWeakBundle(), owner);
            this.domain = domain;
        }

        @Override
        public void finalizeReference() {
            domain.invalidate();
            super.finalizeReference();
        }
    }

    // -------------------------------------------------------------------------
    // Width: exact class
    // -------------------------------------------------------------------------

    /**
     * Retires only {@code type}'s MetaClass SwitchPoint domain (exact class;
     * no subtype fan-out). No-op when {@code type} is {@code null}. Does not
     * bump {@link ClassInfo#getVersion()}.
     * <p>
     * Used by {@link ClassInfo#incVersion()}, stock registry MetaClass changes,
     * and per-instance MetaClass changes. Non-stock MetaClass kinds use
     * {@link #invalidateBulk()} instead.
     *
     * @param type the class whose MetaClass changed
     */
    public static void invalidateClass(final Class<?> type) {
        if (type == null) {
            return;
        }
        List<SwitchPoint> batch = new ArrayList<>(1);
        collectLiveForClass(type, batch);
        invalidateBatch(batch);
        CLASS_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("exact-class SwitchPoint invalidated for " + type.getName()
                    + "; totalClass=" + CLASS_INVALIDATIONS.get());
        }
    }

    // -------------------------------------------------------------------------
    // Width: all loaded (one primitive, reason-labelled public entries)
    // -------------------------------------------------------------------------

    /**
     * Category enter/leave and {@code VMPlugin.invalidateCallSites()}.
     * Retires every loaded class-level domain so sites re-link under the new
     * category state. No separate category SwitchPoint on the hot path.
     */
    public static void invalidateCategory() {
        retireLiveDomains();
        CATEGORY_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("category bulk invalidation; total=" + CATEGORY_INVALIDATIONS.get());
        }
    }

    /**
     * Process-wide bulk retirement for non-stock (custom) MetaClass registry
     * events — correctness-first when selection may consult state outside the
     * stock miss walk. Rare.
     */
    public static void invalidateBulk() {
        retireLiveDomains();
        BULK_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("custom-MetaClass bulk invalidation; totalBulk=" + BULK_INVALIDATIONS.get());
        }
    }

    /**
     * Process-wide bulk retirement when a MetaClass registry event carries no
     * {@code Class} attribution.
     */
    public static void invalidateUnscoped() {
        retireLiveDomains();
        BULK_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("unscoped bulk invalidation; totalBulk=" + BULK_INVALIDATIONS.get());
        }
    }

    /**
     * Detaches and invalidates every live class domain via the live-SwitchPoint
     * registry — O(live domains). Shared implementation for all process-wide
     * entries.
     * <p>
     * Skipped entirely while the registry is empty, e.g. classic-only bytecode
     * never installs indy MOP guards: registration precedes SwitchPoint
     * publication, so an empty observation proves there was nothing to retire.
     */
    private static void retireLiveDomains() {
        if (!SwitchPointInvalidator.hasLiveSwitchPoints()) {
            return;
        }
        List<SwitchPoint> batch = new ArrayList<>();
        try {
            SwitchPointInvalidator.drainLive(batch);
        } finally {
            invalidateBatch(batch);
        }
    }

    // -------------------------------------------------------------------------
    // Registry policy
    // -------------------------------------------------------------------------

    /**
     * Registry-driven invalidation: exact class for stock MetaClass kinds and
     * per-instance changes; process-wide bulk for custom MetaClass kinds or
     * unattributed events. See class javadoc.
     *
     * @param event the registry change event (must not be {@code null})
     */
    public static void invalidateForMetaClassChange(final MetaClassRegistryChangeEvent event) {
        Class<?> type = event.getClassToUpdate();
        if (type == null) {
            invalidateUnscoped();
            return;
        }
        if (event.isPerInstanceMetaClassChange()) {
            invalidateClass(type);
            return;
        }
        if (needsBulkInvalidation(event.getOldMetaClass(), event.getNewMetaClass())) {
            invalidateBulk();
        } else {
            invalidateClass(type);
        }
    }

    /**
     * Whether a class-level MetaClass change involving {@code oldMc}/{@code newMc}
     * requires process-wide bulk invalidation.
     * <p>
     * Bulk is reserved for <em>non-stock</em> MetaClass kinds: not
     * {@link MetaClassImpl} after adapter unwrap. Stock pairs stay exact-class.
     *
     * @param oldMc previous MetaClass (may be {@code null})
     * @param newMc new MetaClass (may be {@code null} on remove)
     * @return {@code true} if bulk invalidation is required
     */
    public static boolean needsBulkInvalidation(final MetaClass oldMc, final MetaClass newMc) {
        return !isStockMetaClass(oldMc) || !isStockMetaClass(newMc);
    }

    /**
     * Whether {@code mc} is a stock MOP MetaClass for which exact-class
     * invalidation is sufficient: {@code null}, or unwrapped
     * {@link MetaClassImpl} (includes EMC and subclasses). Any other kind is
     * non-stock and triggers bulk invalidation on registry replace.
     *
     * @param mc MetaClass to classify (may be {@code null})
     * @return {@code true} if exact-class invalidation is sufficient
     */
    public static boolean isStockMetaClass(final MetaClass mc) {
        MetaClass current = unwrapMetaClass(mc);
        return current == null || current instanceof MetaClassImpl;
    }

    /**
     * Unwraps {@link AdaptingMetaClass} / {@link DelegatingMetaClass} wrappers
     * (bounded depth) to the underlying MetaClass.
     *
     * @param mc MetaClass to unwrap (may be {@code null})
     * @return the unwrapped MetaClass, or {@code null}
     */
    static MetaClass unwrapMetaClass(final MetaClass mc) {
        MetaClass current = mc;
        // Bound unwrap depth: HandleMetaClass is typically one level; be defensive.
        for (int i = 0; i < 4 && current != null; i++) {
            MetaClass adaptee = adapteeOf(current);
            if (adaptee == null || adaptee == current) {
                return current;
            }
            current = adaptee;
        }
        return current;
    }

    private static MetaClass adapteeOf(final MetaClass mc) {
        if (mc instanceof AdaptingMetaClass adapting) {
            return adapting.getAdaptee();
        }
        if (mc instanceof DelegatingMetaClass delegating) {
            return delegating.getAdaptee();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Domain access
    // -------------------------------------------------------------------------

    /**
     * Collects the live SwitchPoint (if any) of {@code type}'s class domain.
     * Does not create a MetaClass and does not allocate a domain generation.
     *
     * @param type class to inspect (must not be {@code null})
     * @param out  destination list
     */
    public static void collectLiveForClass(final Class<?> type, final List<SwitchPoint> out) {
        ClassInfo.getClassInfo(type).collectLiveIndySwitchPoints(out);
    }

    private static void invalidateBatch(final List<SwitchPoint> batch) {
        if (batch.isEmpty()) {
            return;
        }
        // AOT-safe: advances the AotDispatch stamp; the real invalidateAll runs only on a JVM
        AotDispatch.invalidateAll(batch.toArray(EMPTY_SWITCH_POINTS));
    }

    // -------------------------------------------------------------------------
    // Link-time domain resolution and guards
    // -------------------------------------------------------------------------

    /**
     * Resolves the class used for the MetaClass SwitchPoint domain of a receiver.
     * {@code null} maps to {@link NullObject}; a {@link Class} receiver uses itself.
     *
     * @param receiver the call receiver (may be {@code null})
     * @return the class whose class-level MetaClass SwitchPoint guards this site
     */
    public static Class<?> switchPointClassFor(final Object receiver) {
        if (receiver == null) {
            return NullObject.class;
        }
        if (receiver instanceof Class<?> c) {
            return c;
        }
        return receiver.getClass();
    }

    /**
     * Returns the class-domain SwitchPoint for a MetaClass. The domain belongs
     * to {@link MetaClass#getTheClass() the class}, not the MetaClass instance,
     * so every generation (and every adapter wrapping) of a class's MetaClass
     * shares one domain.
     *
     * @param mc MetaClass (must not be {@code null})
     * @return the class-domain SwitchPoint
     */
    public static SwitchPoint switchPointForMetaClass(final MetaClass mc) {
        Objects.requireNonNull(mc, "mc");
        return classSwitchPointFor(mc.getTheClass());
    }

    /**
     * Retires the class domain of {@code mc}'s class (if a generation was
     * allocated). Does not bump counters (local domain retire; width policy is
     * separate).
     *
     * @param mc MetaClass (may be {@code null})
     */
    public static void invalidateMetaClass(final MetaClass mc) {
        if (mc == null) {
            return;
        }
        List<SwitchPoint> batch = new ArrayList<>(1);
        collectLiveForClass(mc.getTheClass(), batch);
        invalidateBatch(batch);
    }

    /**
     * Returns the class-level MetaClass SwitchPoint for the given receiver.
     *
     * @param receiver the call receiver (may be {@code null})
     * @return the MetaClass-domain switch point
     */
    public static SwitchPoint classSwitchPointFor(final Object receiver) {
        return classSwitchPointFor(switchPointClassFor(receiver));
    }

    /**
     * Returns the SwitchPoint for the given class at link time: the current
     * generation of the ClassInfo-owned class domain (defineClass-safe — does
     * not create or consult a MetaClass).
     *
     * @param type the class (must not be {@code null})
     * @return the SwitchPoint for monomorphic MOP guards
     */
    public static SwitchPoint classSwitchPointFor(final Class<?> type) {
        return ClassInfo.getClassInfo(type).getIndySwitchPoint();
    }

    /**
     * Installs a MetaClass SwitchPoint guard on {@code handle}.
     * Public entry for tests; production uses {@code IndyInterface.applyMopSwitchPoints}.
     *
     * @param handle   the fast-path handle
     * @param fallback the re-link / fallback handle
     * @param receiver the receiver used to select the MetaClass-domain switch point
     * @return the guarded handle
     */
    public static MethodHandle guardWithMopSwitchPoints(
            final MethodHandle handle, final MethodHandle fallback, final Object receiver) {
        return guardWithMopSwitchPoints(handle, fallback, switchPointClassFor(receiver));
    }

    /**
     * Installs a MetaClass SwitchPoint guard on {@code handle}.
     *
     * @param handle        the fast-path handle
     * @param fallback      the re-link / fallback handle
     * @param receiverClass the class whose class-level MetaClass domain guards this site
     * @return the guarded handle
     */
    public static MethodHandle guardWithMopSwitchPoints(
            final MethodHandle handle, final MethodHandle fallback, final Class<?> receiverClass) {
        return classSwitchPointFor(receiverClass).guardWithTest(handle, fallback);
    }

    // -------------------------------------------------------------------------
    // Stats (optional; tests)
    // -------------------------------------------------------------------------

    /**
     * Exact-class invalidation event count ({@link #invalidateClass(Class)}).
     *
     * @return class invalidation count
     */
    public static long classInvalidationCount() {
        return CLASS_INVALIDATIONS.get();
    }

    /**
     * Non-category process-wide bulk event count
     * ({@link #invalidateBulk()} / {@link #invalidateUnscoped()}).
     *
     * @return bulk invalidation count
     */
    public static long bulkInvalidationCount() {
        return BULK_INVALIDATIONS.get();
    }

    /**
     * Category-style bulk invalidation count ({@link #invalidateCategory()}).
     *
     * @return category invalidation count
     */
    public static long categoryInvalidationCount() {
        return CATEGORY_INVALIDATIONS.get();
    }

    /**
     * Resets process-wide counters (tests only). Does not reset live switch points.
     */
    public static void resetCountersForTesting() {
        CLASS_INVALIDATIONS.set(0);
        CATEGORY_INVALIDATIONS.set(0);
        BULK_INVALIDATIONS.set(0);
    }
}
