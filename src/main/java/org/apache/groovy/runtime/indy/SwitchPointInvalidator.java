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

import groovy.transform.Internal;

import java.lang.invoke.SwitchPoint;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lazily allocates a {@link SwitchPoint} and retires it on demand.
 * <p>
 * Call sites install the switch point returned by {@link #getSwitchPoint()} in
 * {@code guardWithTest} chains. {@link #invalidate()} retires the live switch
 * point so linked sites fall back and re-link; the next {@link #getSwitchPoint()}
 * allocates a fresh one.
 * <p>
 * Bulk paths (category enter/leave, custom MetaClass events) should prefer
 * {@link #detachLive()} and a single {@link SwitchPoint#invalidateAll(SwitchPoint[])}
 * over many individual {@link #invalidate()} calls.
 * <p>
 * There is no permanent failover: stock invalidation is scoped to a single
 * class, so deopt storms from a process-wide SwitchPoint do not apply and a
 * simple replace-on-invalidate model is sufficient.
 * <p>
 * A process-wide registry tracks every live SwitchPoint so bulk retirement is
 * O(live domains), and is skipped entirely while the registry is empty — e.g.
 * classic-only bytecode never links an indy MOP guard. Registration precedes
 * publication, so an empty (or entry-less) observation proves no guard chain
 * holds a live SwitchPoint that the observer could have needed to retire.
 * <p>
 * Registry entries hold their invalidator strongly; the invalidator's owner is
 * responsible for retiring the domain when it is discarded (see the reclaim
 * anchor on {@code ClassInfo}'s class-level domain), which is what removes the
 * entry. A guard chain keeps only the SwitchPoint's internal invoker alive,
 * not the SwitchPoint object, so the registry entry is what keeps a
 * still-installed guard retirable until its domain is explicitly retired.
 *
 * <h2>Layering</h2>
 * This is the <em>mechanism</em> half of the MOP invalidation subsystem and
 * makes no policy decisions. It has exactly two supported consumers:
 * {@link org.codehaus.groovy.reflection.ClassInfo}, which owns domain
 * instances (allocation, local invalidate, detach), and
 * {@link IndyInvalidation}, which owns invalidation width policy, reclaim
 * anchoring and per-Class domain continuity. On its own this class guarantees
 * only the single-domain lifecycle documented above (single-use SwitchPoints;
 * registration precedes publication); guarantees about domain identity across
 * ClassInfo recreation exist only under {@link IndyInvalidation}'s
 * management. It is not an extension point and may change incompatibly.
 *
 * @since 6.0.0
 */
@Internal
public final class SwitchPointInvalidator {

    /**
     * Reusable one-element array for single-SP {@link SwitchPoint#invalidateAll},
     * guarded by {@link #SINGLE_INVALIDATE_LOCK}. Invalidation is rare relative to
     * steady-state dispatch, so the lock is not on a hot path.
     */
    private static final Object SINGLE_INVALIDATE_LOCK = new Object();
    private static final SwitchPoint[] SINGLE_INVALIDATE_BUF = new SwitchPoint[1];

    /**
     * Registry of all live SwitchPoints, each mapped to its owning invalidator.
     * Keyed by SwitchPoint — each has a single-use lifecycle (allocated once,
     * detached once), so a removal can never clobber a successor's entry the
     * way an invalidator-keyed registry could (ABA on re-allocation).
     * <p>
     * Invariant: any SwitchPoint published in {@link #current} has an entry
     * here, established by registering <em>before</em> the publishing CAS.
     * Entries are removed only by the party that detaches the SwitchPoint
     * (or by the allocator when its publishing CAS loses), so an entry whose
     * SwitchPoint is not (yet) current is transient and self-resolving.
     */
    private static final ConcurrentHashMap<SwitchPoint, SwitchPointInvalidator> LIVE =
            new ConcurrentHashMap<>();

    /** {@code null} means no live switch point (lazy allocation on next get). */
    private final AtomicReference<SwitchPoint> current = new AtomicReference<>();
    private final AtomicInteger retirementCount = new AtomicInteger();

    /**
     * Opaque object kept strongly reachable for as long as this invalidator is
     * reachable. The registry holds live domains' invalidators strongly, so an
     * owner can park a cleanup reference here (e.g. {@code ClassInfo}'s domain
     * reclaim) and rely on it surviving the owner itself while a live
     * SwitchPoint remains registered.
     * <p>
     * Held through an {@link AtomicReference} so parking the reclaim is a
     * volatile publication and stays reachable for as long as this invalidator
     * does.
     */
    private final AtomicReference<Object> reclaimAnchor = new AtomicReference<>();

    /**
     * Creates an invalidator with no live SwitchPoint until the first
     * {@link #getSwitchPoint()} call. Field initializers own all state.
     */
    public SwitchPointInvalidator() {
        // Intentionally empty: AtomicReference / AtomicInteger fields are ready.
    }

    /**
     * Parks {@code anchor} so it stays strongly reachable while this
     * invalidator is reachable. See {@link #reclaimAnchor}.
     *
     * @param anchor the object to keep reachable (may be {@code null} to clear)
     */
    public void setReclaimAnchor(final Object anchor) {
        this.reclaimAnchor.set(anchor);
    }

    /**
     * Returns a live switch point to install in a call-site guard chain.
     * Lazily allocates on first use after construction or after retirement.
     *
     * @return a live switch point (never {@code null})
     */
    public SwitchPoint getSwitchPoint() {
        for (;;) {
            SwitchPoint sp = current.get();
            if (sp != null) {
                return sp;
            }
            SwitchPoint created = new SwitchPoint();
            // Register before publish: a bulk path that finds no entry is then
            // guaranteed the SwitchPoint was not visible to any guard, so
            // skipping it is safe.
            LIVE.put(created, this);
            if (current.compareAndSet(null, created)) {
                return created;
            }
            LIVE.remove(created); // lost the publishing race; never live
        }
    }

    /**
     * Invalidates the current live switch point (if any) so linked sites re-link.
     * A subsequent {@link #getSwitchPoint()} allocates a fresh switch point.
     */
    public void invalidate() {
        SwitchPoint retired = detachLive();
        invalidateIfLive(retired);
    }

    /**
     * Atomically detaches the live switch point for bulk
     * {@link SwitchPoint#invalidateAll} without invalidating it yet.
     * The caller <em>must</em> invalidate any non-null return value.
     *
     * @return the detached live switch point, or {@code null} if none was live
     */
    public SwitchPoint detachLive() {
        SwitchPoint sp = current.getAndSet(null);
        if (sp != null) {
            LIVE.remove(sp);
            retirementCount.incrementAndGet();
        }
        return sp;
    }

    /**
     * Detaches {@code sp} only if it is still this invalidator's current live
     * SwitchPoint, deregistering it on success. Lets bulk drains claim exactly
     * one retirement per SwitchPoint without racing owner-side
     * {@link #detachLive()} calls.
     *
     * @param sp the candidate SwitchPoint (must not be {@code null})
     * @return {@code true} if this call detached {@code sp}; the caller must
     *         then invalidate it
     */
    boolean detachIfCurrent(final SwitchPoint sp) {
        if (current.compareAndSet(sp, null)) {
            LIVE.remove(sp);
            retirementCount.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Returns how many live switch points have been retired.
     *
     * @return the retirement count
     */
    public int getRetirementCount() {
        return retirementCount.get();
    }

    /**
     * Whether any live SwitchPoint is currently registered. A {@code false}
     * return guarantees no guard chain anywhere holds a SwitchPoint the caller
     * could have needed to retire (registration precedes publication), so
     * process-wide bulk retirement may be skipped. The check re-arms once all
     * domains retire.
     *
     * @return {@code true} while at least one SwitchPoint is registered live
     */
    static boolean hasLiveSwitchPoints() {
        return !LIVE.isEmpty();
    }

    /**
     * Detaches every registered live SwitchPoint into {@code out} for a single
     * bulk {@link SwitchPoint#invalidateAll} — O(live domains). The caller
     * <em>must</em> invalidate everything added to {@code out}.
     * <p>
     * Entries whose SwitchPoint is no longer (or not yet) current are left in
     * place: they are either about to be published (removing them would strand
     * a live SwitchPoint unregistered, invisible to all future drains) or are
     * being removed by their detaching owner. The weakly consistent iteration
     * may miss a SwitchPoint published mid-drain; sites linking concurrently
     * read the current category state at link time.
     *
     * @param out destination list (must not be {@code null})
     */
    static void drainLive(final List<SwitchPoint> out) {
        LIVE.forEach((sp, inv) -> {
            if (inv.detachIfCurrent(sp)) {
                out.add(sp);
            }
        });
    }

    /**
     * Number of currently registered live SwitchPoints (tests).
     *
     * @return the live registry size
     */
    static int liveSwitchPointCount() {
        return LIVE.size();
    }

    /**
     * Whether {@code sp} is currently registered live (tests).
     *
     * @param sp SwitchPoint to look up
     * @return {@code true} if registered
     */
    static boolean isRegistered(final SwitchPoint sp) {
        return LIVE.containsKey(sp);
    }

    /**
     * Invalidates {@code sp} when non-null and still valid.
     *
     * @param sp candidate switch point (may be {@code null})
     */
    public static void invalidateIfLive(final SwitchPoint sp) {
        if (sp != null && !sp.hasBeenInvalidated()) {
            synchronized (SINGLE_INVALIDATE_LOCK) {
                SINGLE_INVALIDATE_BUF[0] = sp;
                try {
                    // AOT-safe: stamp always advances; real invalidateAll only on a JVM
                    AotDispatch.invalidateAll(SINGLE_INVALIDATE_BUF);
                } finally {
                    SINGLE_INVALIDATE_BUF[0] = null;
                }
            }
        }
    }
}
