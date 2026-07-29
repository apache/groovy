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
import groovy.lang.ExpandoMetaClass;
import groovy.lang.ExpandoMetaClassCreationHandle;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaClassRegistryChangeEvent;
import groovy.lang.MutableMetaClass;
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
 * <b>Model (intentionally minimal for 6.0):</b> one SwitchPoint domain per
 * class, attached to {@link ClassInfo}. This domain stands for the
 * <em>class-level MetaClass generation</em> observed by monomorphic indy sites
 * for receivers of that runtime class. Linked handles carry a single
 * {@code guardWithTest} — matching the pre-6.0 monomorphic shape — while
 * avoiding process-wide deopt on unrelated MetaClass churn.
 * <p>
 * <b>What this is not (yet):</b> SwitchPoints do not live on {@link MetaClass}
 * instances themselves. A MetaClass-owned guard would be the natural place for
 * MetaClass-specific logic (including custom MetaClass implementations and
 * true per-instance domains). That is a larger redesign tracked as a follow-up
 * JIRA to GROOVY-12191 (MetaClass-owned SwitchPoints; to be filed on the PR):
 * today per-instance MetaClass already forces uncacheable PIC entries
 * ({@code canSetTarget=false}), and class-level generation is retired through
 * {@link ClassInfo}. The policy below is the conservative, MetaClass-<em>aware</em>
 * approximation on top of the class-domain model.
 *
 * <h2>Why hierarchy fan-out exists (and what it is not)</h2>
 * <p>
 * Hierarchy fan-out is <em>not</em> “{@code MetaClassImpl} shares one method table
 * up the hierarchy”. Each class still has its own MetaClass / {@code ClassInfo}
 * domain. Fan-out exists because <em>selection</em> can observe <em>ancestor</em>
 * MetaClass state in two distinct ways:
 * <ol>
 *   <li><b>Missing-method path only (live):</b>
 *       {@code MetaClassImpl.findMethodInClassHierarchy} returns immediately
 *       unless some strong MetaClass in the receiver hierarchy is a
 *       <em>modified</em> {@link MutableMetaClass} (EMC is the common case).
 *       When that gate opens, a method that was a miss on the receiver class
 *       can be found on a parent / interface / array-lattice ancestor.
 *       Already-linked subtype sites that cached a miss (or an older target)
 *       must re-select — that is the SwitchPoint hierarchy case.
 *       Present methods on a {@code MetaClassImpl} child keep winning for
 *       applicable arguments after hierarchy re-link (the walk does not rebuild
 *       MetaClass tables). An {@link ExpandoMetaClass} child can still select a
 *       more specific ancestor overload for a signature the child does not
 *       declare (e.g. child {@code m2(Object)} vs parent {@code m2(Integer)}).</li>
 *   <li><b>Construction-time snapshot (MetaClassImpl init):</b>
 *       when a {@code MetaClassImpl} is first built <em>after</em> an ancestor
 *       already carries expando / “new” meta methods, those methods can be
 *       copied into the child’s method index. A sibling class whose MetaClass
 *       was built <em>before</em> the ancestor change keeps the older view.
 *       That timing gap is pre-existing MOP behaviour; retiring a SwitchPoint
 *       re-links against the <em>same</em> MetaClass instance and does not
 *       rebuild its method tables. Fixing the gap (making {@code MetaClassImpl}
 *       immune to other MetaClasses / removing the hierarchy walk) is a
 *       separate semantic redesign, not this PR’s SwitchPoint scoping.</li>
 * </ol>
 * Pure {@code MetaClassImpl} ↔ {@code MetaClassImpl} (or null) class replace
 * therefore stays <b>exact-class</b>: it does not open the missing-method walk
 * and must not fan out. Most “metaclass changed on the receiver” cases are
 * likewise exact-class — the SwitchPoint retires because <em>that</em> class’s
 * MetaClass changed, not because of hierarchy versioning.
 *
 * <h2>When do we invalidate, and how wide?</h2>
 *
 * <table border="1" summary="Invalidation policy">
 *   <tr><th>Event</th><th>Domain retired</th><th>Why</th></tr>
 *   <tr>
 *     <td>EMC method/property update ({@link ClassInfo#incVersion()})</td>
 *     <td>class + loaded subtypes / implementors / array lattice</td>
 *     <td>In-place EMC update can make a previously missing name resolvable on
 *         subtypes via the missing-method hierarchy walk (and can change
 *         already-linked miss targets). Present methods on the child keep
 *         winning after re-link.</td>
 *   </tr>
 *   <tr>
 *     <td>Registry replace where old or new MC is {@link ExpandoMetaClass}</td>
 *     <td>class + hierarchy</td>
 *     <td>Installing / removing EMC changes cross-class MOP visibility for
 *         already-linked subtype sites (miss path / EMC children).</td>
 *   </tr>
 *   <tr>
 *     <td>Registry replace while global EMC creation handle is active</td>
 *     <td>class + hierarchy</td>
 *     <td>Every class MetaClass is an EMC; inheritance refresh can republish
 *         super methods into subtypes.</td>
 *   </tr>
 *   <tr>
 *     <td>Registry replace of an interface or array type</td>
 *     <td>class + hierarchy (array lattice indexed)</td>
 *     <td>Interface / array MetaClasses participate in the same hierarchy walk
 *         (e.g. {@code Object[].metaClass.foo} is visible on {@code String[]}).</td>
 *   </tr>
 *   <tr>
 *     <td>Class replace where <em>both</em> old and new MetaClass are
 *         hierarchy-local (null, or unmodified {@code MetaClassImpl} /
 *         non-EMC subclass with {@code isModified()==false})</td>
 *     <td><b>exact class only</b></td>
 *     <td>Each class owns its own tables; parent replace does not open
 *         {@code findMethodInClassHierarchy}. Over-fan-out would re-link
 *         subtype sites without a MOP-visibility reason.</td>
 *   </tr>
 *   <tr>
 *     <td>Class replace involving unknown / custom MetaClass kinds</td>
 *     <td>class + hierarchy</td>
 *     <td>Correctness-first: custom MetaClasses may publish cross-class state;
 *         exact-only is an allow-list, not a deny-list of EMC alone.</td>
 *   </tr>
 *   <tr>
 *     <td>Per-instance MetaClass change</td>
 *     <td>exact class only</td>
 *     <td>Only that class's monomorphic sites must re-select; per-instance
 *         dispatch is already uncacheable in the PIC. Subtypes are unaffected.</td>
 *   </tr>
 *   <tr>
 *     <td>Category enter/leave, {@code invalidateCallSites()}, unscoped event</td>
 *     <td>all loaded class domains</td>
 *     <td>Category state is process-wide; no second hot-path guard.</td>
 *   </tr>
 * </table>
 *
 * <p>
 * <b>Over- vs under-invalidation:</b> exact-class is an <em>allow-list</em>
 * (hierarchy-local MetaClasses only). Ambiguous or custom MetaClass kinds fan
 * out. {@link #invalidateClass(Class)} / {@code incVersion} always fan out
 * (EMC in-place updates). Linked sites for a method that did not change may
 * still re-link when their receiver class domain is retired — that is the
 * monomorphic SwitchPoint trade-off (same as pre-6.0, but scoped). Hierarchy
 * fan-out deliberately over-invalidates subtype sites whose present methods
 * are unchanged, so that sites which previously linked a <em>miss</em> cannot
 * stay stale; the registry cannot cheaply know which names were linked.
 * <p>
 * <b>Scalability:</b> hierarchy fan-out is {@code O(|loaded subtypes of T|)}
 * via {@link ClassHierarchyIndex}. Category bulk remains a full ClassInfo walk
 * with cheap no-op detaches for domains that never allocated a SwitchPoint.
 * <p>
 * Install guards with
 * {@link #guardWithMopSwitchPoints(MethodHandle, MethodHandle, Object)}
 * (public / test entry). Production Selector wiring uses
 * {@code IndyInterface.applyMopSwitchPoints}.
 * Optional stats logging: {@code -Dgroovy.indy.invalidation.stats=true}.
 *
 * @since 6.0.0
 */
public final class IndyInvalidation {

    private static final Logger LOG = Logger.getLogger(IndyInvalidation.class.getName());
    private static final boolean STATS_LOG = SystemUtil.getBooleanSafe("groovy.indy.invalidation.stats");

    private static final AtomicLong CLASS_INVALIDATIONS = new AtomicLong();
    private static final AtomicLong CATEGORY_INVALIDATIONS = new AtomicLong();
    private static final AtomicLong EXACT_ONLY_INVALIDATIONS = new AtomicLong();
    private static final AtomicLong HIERARCHY_INVALIDATIONS = new AtomicLong();

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
     * subtype/implementor (hierarchy fan-out). No-op when {@code type} is
     * {@code null}. Does not bump {@link ClassInfo#getVersion()} on subtypes.
     * <p>
     * Used by {@link ClassInfo#incVersion()} (EMC method updates) and any
     * caller that needs the conservative full-hierarchy policy. Registry
     * listeners prefer {@link #invalidateForMetaClassChange(MetaClassRegistryChangeEvent)}
     * so pure {@code MetaClassImpl} replacements stay exact-class.
     *
     * @param type the class whose MetaClass changed
     */
    public static void invalidateClass(final Class<?> type) {
        if (type == null) {
            return;
        }
        invalidateClassHierarchy(type);
        CLASS_INVALIDATIONS.incrementAndGet();
        HIERARCHY_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("class SwitchPoint hierarchy invalidated for " + type.getName()
                    + "; total=" + CLASS_INVALIDATIONS.get());
        }
    }

    /**
     * Retires only {@code type}'s SwitchPoint domain (no subtype fan-out).
     * Used when MetaClass change cannot affect cross-class MOP visibility
     * (pure {@code MetaClassImpl} class replace, per-instance MetaClass).
     *
     * @param type the class whose domain is retired; no-op when {@code null}
     */
    public static void invalidateClassExact(final Class<?> type) {
        if (type == null) {
            return;
        }
        ClassInfo root = ClassInfo.getClassInfo(type);
        SwitchPoint sp = root.detachLiveIndySwitchPoint();
        if (sp != null) {
            SwitchPointInvalidator.invalidateIfLive(sp);
        }
        CLASS_INVALIDATIONS.incrementAndGet();
        EXACT_ONLY_INVALIDATIONS.incrementAndGet();
        if (STATS_LOG && LOG.isLoggable(Level.FINE)) {
            LOG.fine("exact-class SwitchPoint invalidated for " + type.getName()
                    + "; totalExact=" + EXACT_ONLY_INVALIDATIONS.get());
        }
    }

    /**
     * Registry-driven invalidation with MetaClass-aware fan-out policy.
     * See class javadoc table for the decision matrix.
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
            // Per-instance MC: only this class's monomorphic sites re-select.
            invalidateClassExact(type);
            return;
        }
        if (needsHierarchyFanOut(type, event.getOldMetaClass(), event.getNewMetaClass())) {
            invalidateClass(type);
        } else {
            invalidateClassExact(type);
        }
    }

    /**
     * Whether a class-level MetaClass change for {@code type} can affect
     * already-linked sites on subtypes / array-covariance descendants.
     * <p>
     * Hierarchy fan-out is the default. Exact-class is an <em>allow-list</em>:
     * both old and new MetaClass must be {@linkplain #isHierarchyLocalMetaClass
     * hierarchy-local}, and {@code type} must not be an interface/array, and
     * global EMC must be off. Unknown / custom MetaClass kinds therefore fan
     * out (correctness-first).
     * <p>
     * Aligns with {@code MetaClassImpl.findMethodInClassHierarchy}: the live
     * hierarchy walk opens only when a modified {@link MutableMetaClass} is
     * present (EMC install/remove/update). Pure unmodified
     * {@code MetaClassImpl} pairs never open that walk, so they stay
     * exact-class. Fan-out is still required for the missing-method case even
     * when a subtype already has <em>other</em> methods of the same name with
     * different signatures — a previously unlinked {@code child.m2("")} has no
     * site to invalidate, but a previously linked miss must re-select.
     *
     * @param type  the class being updated (not {@code null})
     * @param oldMc previous MetaClass (may be {@code null})
     * @param newMc new MetaClass (may be {@code null} on remove)
     * @return {@code true} if subtype fan-out is required
     */
    public static boolean needsHierarchyFanOut(
            final Class<?> type, final MetaClass oldMc, final MetaClass newMc) {
        if (type.isInterface() || type.isArray()) {
            return true;
        }
        if (isGlobalEmcEnabled()) {
            return true;
        }
        // Exact-only only when BOTH sides are hierarchy-local.
        return !isHierarchyLocalMetaClass(oldMc) || !isHierarchyLocalMetaClass(newMc);
    }

    /**
     * Whether {@code mc} cannot publish cross-class MOP state into subtype
     * selection via the live missing-method hierarchy walk (or EMC inheritance).
     * Hierarchy-local means:
     * <ul>
     *   <li>{@code null} (absent / removed), or</li>
     *   <li>unwrapped {@code MetaClassImpl} that is not an
     *       {@link ExpandoMetaClass} and reports {@code isModified()==false}.</li>
     * </ul>
     * EMC, modified mutable MetaClasses, and unknown MetaClass kinds return
     * {@code false} so fan-out remains correctness-first.
     * <p>
     * Note: a hierarchy-local {@code MetaClassImpl} may still <em>contain</em>
     * methods snapshotted from an ancestor at construction time; that snapshot
     * is fixed for the MetaClass instance’s lifetime and is not refreshed by
     * SwitchPoint retirement. See class javadoc.
     *
     * @param mc MetaClass to classify (may be {@code null})
     * @return {@code true} if a replace involving only this kind needs no fan-out
     */
    public static boolean isHierarchyLocalMetaClass(final MetaClass mc) {
        MetaClass current = unwrapMetaClass(mc);
        if (current == null) {
            return true;
        }
        // EMC always participates in cross-class visibility (even before the
        // first method marks isModified), so it is never hierarchy-local.
        if (current instanceof ExpandoMetaClass) {
            return false;
        }
        if (current instanceof MutableMetaClass mutable && mutable.isModified()) {
            return false;
        }
        // Unmodified MetaClassImpl (and non-EMC subclasses with isModified==false).
        return current instanceof MetaClassImpl;
    }

    /**
     * {@code true} when the registry creates {@link ExpandoMetaClass} instances
     * by default ({@link ExpandoMetaClass#enableGlobally()}).
     */
    static boolean isGlobalEmcEnabled() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        return registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle;
    }

    /**
     * Whether {@code mc} is (or wraps) an {@link ExpandoMetaClass}.
     * Wrappers are unwrapped via {@link AdaptingMetaClass#getAdaptee()} or
     * {@link DelegatingMetaClass#getAdaptee()} ({@code HandleMetaClass}).
     */
    static boolean isExpandoMetaClass(final MetaClass mc) {
        return unwrapMetaClass(mc) instanceof ExpandoMetaClass;
    }

    /**
     * Unwraps {@link AdaptingMetaClass} / {@link DelegatingMetaClass} wrappers
     * (bounded depth) to the underlying MetaClass.
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
     * <p>
     * Public entry for tests and any code outside {@code vmplugin.v8}. Production
     * call-site linking goes through {@code IndyInterface.applyMopSwitchPoints}
     * (same {@code guardWithTest} after {@link #classSwitchPointFor(Object)}).
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
     * See {@link #guardWithMopSwitchPoints(MethodHandle, MethodHandle, Object)}.
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
     * ({@link #invalidateClass(Class)} / {@link #invalidateClassExact(Class)} /
     * {@link #invalidateForMetaClassChange} / {@link #invalidateUnscoped()} events).
     *
     * @return class invalidation count
     */
    public static long classInvalidationCount() {
        return CLASS_INVALIDATIONS.get();
    }

    /**
     * Returns how many class invalidations used hierarchy fan-out.
     *
     * @return hierarchy fan-out event count
     */
    public static long hierarchyInvalidationCount() {
        return HIERARCHY_INVALIDATIONS.get();
    }

    /**
     * Returns how many class invalidations were exact-class only.
     *
     * @return exact-only event count
     */
    public static long exactOnlyInvalidationCount() {
        return EXACT_ONLY_INVALIDATIONS.get();
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
        EXACT_ONLY_INVALIDATIONS.set(0);
        HIERARCHY_INVALIDATIONS.set(0);
    }
}
