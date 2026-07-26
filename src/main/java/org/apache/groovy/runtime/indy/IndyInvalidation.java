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

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.NullObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scoped invokedynamic {@link SwitchPoint} invalidation for the Groovy MOP
 * (GROOVY-12191).
 * <p>
 * <b>Model (intentionally minimal):</b> one SwitchPoint domain per class,
 * attached to {@link ClassInfo}. MetaClass changes for type {@code T} retire
 * {@code T}'s SwitchPoint and those of loaded subtypes/implementors (hierarchy
 * fan-out). Category enter/leave and unscoped MetaClass events bulk-retire
 * every loaded class SwitchPoint — there is no second, process-wide category
 * SwitchPoint on the hot path. Linked handles therefore always carry a single
 * guard, matching the pre-6.0 monomorphic shape while avoiding global deopt on
 * unrelated MetaClass churn.
 * <p>
 * <b>Scalability:</b> hierarchy fan-out is {@code O(|loaded subtypes of T|)}
 * via {@link ClassHierarchyIndex} (built once per {@link ClassInfo}), not
 * {@code O(|all loaded classes|)}. Category bulk remains a full ClassInfo walk
 * with cheap no-op detaches for domains that never allocated a SwitchPoint.
 * <p>
 * Install guards with
 * {@link #guardWithMopSwitchPoints(MethodHandle, MethodHandle, Object)}.
 * Optional stats logging: {@code -Dgroovy.indy.invalidation.stats=true}.
 *
 * @since 6.0.0
 */
public final class IndyInvalidation {

    private static final Logger LOG = Logger.getLogger(IndyInvalidation.class.getName());
    private static final boolean STATS_LOG = SystemUtil.getBooleanSafe("groovy.indy.invalidation.stats");

    private static final AtomicLong CLASS_INVALIDATIONS = new AtomicLong();
    private static final AtomicLong CATEGORY_INVALIDATIONS = new AtomicLong();

    private static final SwitchPoint[] EMPTY_SWITCH_POINTS = new SwitchPoint[0];

    private IndyInvalidation() {
    }

    /**
     * Creates a per-class SwitchPoint invalidator for {@link ClassInfo}.
     *
     * @return a new invalidator
     */
    public static SwitchPointInvalidator newClassInvalidator() {
        return new SwitchPointInvalidator();
    }

    /**
     * Invalidates SwitchPoints for category enter/leave and
     * {@code VMPlugin.invalidateCallSites()}.
     * <p>
     * Implemented as a bulk class-domain invalidation so every linked site
     * re-links under the new category state. There is no separate category
     * SwitchPoint on the hot path.
     */
    public static void invalidateCategory() {
        invalidateAllLoadedClassSwitchPoints();
        CATEGORY_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("category invalidation via bulk class SwitchPoints; total="
                    + CATEGORY_INVALIDATIONS.get());
        }
    }

    /**
     * Invalidates the per-class SwitchPoint for {@code type} and every loaded
     * subtype/implementor. No-op when {@code type} is {@code null}.
     * Does not bump {@link ClassInfo#getVersion()} on subtypes.
     *
     * @param type the class whose MetaClass changed
     */
    public static void invalidateClass(final Class<?> type) {
        if (type == null) {
            return;
        }
        invalidateClassHierarchy(type);
        CLASS_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("class SwitchPoint hierarchy invalidated for " + type.getName()
                    + "; total=" + CLASS_INVALIDATIONS.get());
        }
    }

    /**
     * Invalidates SwitchPoints for {@code type} and all loaded classes assignable
     * from {@code type}. One path for every kind of type: detach the root domain,
     * then detach every descendant registered in {@link ClassHierarchyIndex}
     * (empty for leaves such as finals and primitives; non-empty for classes,
     * interfaces, and array covariance including {@code Object[]} /
     * interface-component arrays). Batched through
     * {@link SwitchPoint#invalidateAll(SwitchPoint[])}.
     *
     * @param type the root of the invalidation fan-out
     */
    public static void invalidateClassHierarchy(final Class<?> type) {
        ClassInfo root = ClassInfo.getClassInfo(type);
        List<SwitchPoint> batch = new ArrayList<>();
        try {
            SwitchPoint rootSp = root.detachLiveIndySwitchPoint();
            if (rootSp != null) {
                batch.add(rootSp);
            }
            List<ClassInfo> descendants = new ArrayList<>();
            ClassHierarchyIndex.collectDescendants(type, descendants);
            for (ClassInfo info : descendants) {
                SwitchPoint sp = info.detachLiveIndySwitchPoint();
                if (sp != null) {
                    batch.add(sp);
                }
            }
        } finally {
            // Always invalidate detached SPs even if the walk fails mid-way.
            invalidateBatch(batch);
        }
    }

    /**
     * Detaches and invalidates every live per-class SwitchPoint.
     * Used for category enter/leave and unattributed MetaClass changes.
     * Walks all loaded {@link ClassInfo} instances; {@code detachLive} is a
     * cheap no-op when a domain never allocated a SwitchPoint.
     */
    public static void invalidateAllLoadedClassSwitchPoints() {
        List<SwitchPoint> batch = new ArrayList<>();
        try {
            for (ClassInfo info : ClassInfo.getAllClassInfo()) {
                SwitchPoint sp = info.detachLiveIndySwitchPoint();
                if (sp != null) {
                    batch.add(sp);
                }
            }
        } finally {
            invalidateBatch(batch);
        }
    }

    /**
     * Invalidates every live class-domain SwitchPoint. Used when a MetaClass
     * registry event carries no {@code Class} attribution.
     */
    public static void invalidateUnscoped() {
        invalidateAllLoadedClassSwitchPoints();
        CLASS_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("unscoped SwitchPoint invalidation; total=" + CLASS_INVALIDATIONS.get());
        }
    }

    private static void invalidateBatch(final List<SwitchPoint> batch) {
        if (batch.isEmpty()) {
            return;
        }
        SwitchPoint.invalidateAll(batch.toArray(EMPTY_SWITCH_POINTS));
    }

    /**
     * Resolves the class used for the per-class SwitchPoint domain of a receiver.
     * {@code null} maps to {@link NullObject}; a {@link Class} receiver uses itself.
     *
     * @param receiver the call receiver (may be {@code null})
     * @return the class whose {@link ClassInfo} SwitchPoint guards this site
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
     * Returns the per-class switch point for the given receiver.
     *
     * @param receiver the call receiver (may be {@code null})
     * @return the class-domain switch point
     */
    public static SwitchPoint classSwitchPointFor(final Object receiver) {
        return ClassInfo.getClassInfo(switchPointClassFor(receiver)).getIndySwitchPoint();
    }

    /**
     * Returns the per-class switch point for the given class.
     *
     * @param type the class (must not be {@code null})
     * @return the class-domain switch point
     */
    public static SwitchPoint classSwitchPointFor(final Class<?> type) {
        return ClassInfo.getClassInfo(type).getIndySwitchPoint();
    }

    /**
     * Installs a per-class SwitchPoint guard on {@code handle}.
     * MetaClass or category changes that retire that class's SwitchPoint cause
     * the site to take {@code fallback}.
     *
     * @param handle   the fast-path handle
     * @param fallback the re-link / fallback handle
     * @param receiver the receiver used to select the class-domain switch point
     * @return the guarded handle
     */
    public static MethodHandle guardWithMopSwitchPoints(
            final MethodHandle handle, final MethodHandle fallback, final Object receiver) {
        return guardWithMopSwitchPoints(handle, fallback, switchPointClassFor(receiver));
    }

    /**
     * Installs a per-class SwitchPoint guard on {@code handle}.
     *
     * @param handle        the fast-path handle
     * @param fallback      the re-link / fallback handle
     * @param receiverClass the class whose MetaClass domain guards this site
     * @return the guarded handle
     */
    public static MethodHandle guardWithMopSwitchPoints(
            final MethodHandle handle, final MethodHandle fallback, final Class<?> receiverClass) {
        SwitchPoint classSp = ClassInfo.getClassInfo(receiverClass).getIndySwitchPoint();
        return classSp.guardWithTest(handle, fallback);
    }

    /**
     * Returns the total number of scoped class invalidations
     * ({@link #invalidateClass(Class)} / {@link #invalidateUnscoped()} events).
     *
     * @return class invalidation count
     */
    public static long classInvalidationCount() {
        return CLASS_INVALIDATIONS.get();
    }

    /**
     * Returns the total number of category-style bulk invalidations.
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
    }
}
