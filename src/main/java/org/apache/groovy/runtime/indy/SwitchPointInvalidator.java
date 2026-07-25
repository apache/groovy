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

import java.lang.invoke.SwitchPoint;
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
 * Hierarchy fan-out and other bulk paths should prefer {@link #detachLive()} and a
 * single {@link SwitchPoint#invalidateAll(SwitchPoint[])} over many individual
 * {@link #invalidate()} calls (GROOVY-12191).
 * <p>
 * There is no permanent failover: after scoping invalidation to a single class
 * (plus subtypes), deopt storms from a process-wide SwitchPoint no longer apply,
 * so a simpler replace-on-invalidate model is sufficient.
 *
 * @since 6.0.0
 */
public final class SwitchPointInvalidator {

    /**
     * Reusable one-element array for single-SP {@link SwitchPoint#invalidateAll},
     * guarded by {@link #SINGLE_INVALIDATE_LOCK}. Invalidation is rare relative to
     * steady-state dispatch, so the lock is not on a hot path.
     */
    private static final Object SINGLE_INVALIDATE_LOCK = new Object();
    private static final SwitchPoint[] SINGLE_INVALIDATE_BUF = new SwitchPoint[1];

    /** {@code null} means no live switch point (lazy allocation on next get). */
    private final AtomicReference<SwitchPoint> current = new AtomicReference<>();
    private final AtomicInteger retirementCount = new AtomicInteger();

    /**
     * Creates an invalidator.
     */
    public SwitchPointInvalidator() {
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
            if (current.compareAndSet(null, created)) {
                return created;
            }
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
            retirementCount.incrementAndGet();
        }
        return sp;
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
     * Invalidates a single live switch point, reusing a process-wide one-element
     * buffer under a short lock to avoid allocating {@code SwitchPoint[]} on the
     * rare invalidation path.
     *
     * @param sp candidate switch point (may be {@code null})
     */
    static void invalidateIfLive(final SwitchPoint sp) {
        if (sp != null && !sp.hasBeenInvalidated()) {
            synchronized (SINGLE_INVALIDATE_LOCK) {
                SINGLE_INVALIDATE_BUF[0] = sp;
                try {
                    SwitchPoint.invalidateAll(SINGLE_INVALIDATE_BUF);
                } finally {
                    SINGLE_INVALIDATE_BUF[0] = null;
                }
            }
        }
    }
}
