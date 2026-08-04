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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ahead-of-time link mode for indy dispatch (incubating).
 * <p>
 * GraalVM native image supports every {@code java.lang.invoke} building block Groovy's indy
 * runtime uses <em>except</em> retargeting an existing call site: both
 * {@code MutableCallSite.setTarget} and {@code SwitchPoint.invalidateAll} fail with
 * {@code Unsupported method java.lang.invoke.MethodHandleNatives.setCallSiteTargetNormal}.
 * In Groovy's design those two primitives only ever install or invalidate <em>caches</em> —
 * the dispatch semantics live entirely in method selection — so under AOT the runtime links
 * every site once to its cache-consulting default path ({@code ConstantCallSite}) and carries
 * freshness in data instead:
 * <ul>
 *   <li>a global {@linkplain #stamp() invalidation stamp}, bumped wherever the JVM path would
 *       invalidate SwitchPoints;</li>
 *   <li>a stamp captured per cached {@code MethodHandleWrapper} at selection time and compared
 *       on every PIC hit — a mismatch is treated as a cache miss and re-selects;</li>
 *   <li>a pre-selection sample guarding the PIC write itself: when the stamp moves while a
 *       selection runs, the sentinel is cached instead of the wrapper, since the wrapper's
 *       construction-time stamp would postdate an invalidation its selection may have missed
 *       (SwitchPoint guards are immune to this window — their token is acquired during
 *       selection and mutated by the invalidation itself).</li>
 * </ul>
 * The JVM path is untouched: sites link mutable exactly as before, and the stamp is written
 * but never read. Coarser than the scoped SwitchPoint invalidation of GROOVY-12191 (any
 * invalidation flushes every AOT PIC entry on next hit), which is safe — staleness is
 * impossible, over-invalidation just re-selects.
 * <p>
 * The retargeting restriction is particular to GraalVM native image — other ahead-of-time
 * or checkpointed runtimes (ART, CRaC, HotSpot AOT caches) retarget call sites normally and
 * never need this mode — so auto-detection probes only GraalVM's image-code property. The
 * mode itself relies on nothing GraalVM-specific; {@link #FORCE_PROPERTY} is the opt-in for
 * any runtime that turns out to share the restriction.
 *
 * @since 6.0.0
 */
public final class AotDispatch {

    /**
     * Diagnostic knob: forces AOT link mode on a regular JVM so the whole mode can be
     * exercised by ordinary tests without a native build.
     * <p>
     * Set it at JVM startup (or before any Groovy code runs) and leave it alone. Because
     * {@link #isAotLinkRequested()} is re-evaluated per link and per invalidation, flipping
     * the property on mid-run suppresses the real {@link SwitchPoint#invalidateAll} that
     * sites already linked in mutable mode depend on — their guards never fire and they
     * dispatch stale. (Flipping it off is merely wasteful: sites linked while it was on keep
     * consulting the stamp, which keeps advancing, so they stay correct but never regain the
     * retargeting fast path.)
     */
    public static final String FORCE_PROPERTY = "groovy.indy.aot.link";

    private static final AtomicLong STAMP = new AtomicLong();

    private AotDispatch() {
    }

    /**
     * Whether sites should link in AOT mode. Evaluated per call and never cached in a static:
     * under native image this class may be initialized at image build time, where
     * {@code org.graalvm.nativeimage.imagecode} reports {@code buildtime} — caching would bake
     * the wrong answer into the image heap. Callers are all link-time or invalidation-time
     * (cold); per-invocation code reads the site-local flag captured at link time instead.
     */
    public static boolean isAotLinkRequested() {
        return "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"))
                || Boolean.getBoolean(FORCE_PROPERTY);
    }

    /** The current global invalidation stamp. */
    public static long stamp() {
        return STAMP.get();
    }

    /**
     * Invalidates the given switch points, AOT-safely: the global stamp is always advanced
     * (so AOT-linked sites observe the change on their next PIC hit), and the actual
     * {@link SwitchPoint#invalidateAll} — which native image cannot execute — runs only
     * outside AOT mode. All indy invalidation funnels through here.
     *
     * @param switchPoints the points to invalidate; may be empty
     */
    public static void invalidateAll(final SwitchPoint[] switchPoints) {
        STAMP.incrementAndGet();
        if (!isAotLinkRequested()) {
            SwitchPoint.invalidateAll(switchPoints);
        }
    }
}
