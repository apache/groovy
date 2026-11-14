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
package org.apache.groovy.parser.antlr4.internal.atnmanager;

import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.groovy.util.SystemUtil;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manage ATN to avoid memory leak.
 * <p>
 * The mutable prediction caches (DFA states, prediction contexts, LL(1) table) live on a
 * <em>private</em> ATN owned by a softly referenced {@link AtnWrapper}, not on the
 * generated parser's static ATN. That single decision provides the memory-pressure
 * response: while parsers are active the wrapper is strongly reachable through their
 * interpreters, and once this copy of the runtime goes idle the whole cache graph is only
 * softly reachable, so the collector reclaims it directly — no cleaner thread (which
 * would pin the defining class loader for the life of the JVM, GROOVY-12142), and no
 * clear-on-next-parse canary (which never fires for an idle copy: a build daemon caches
 * one runtime copy per distinct classpath, and every idle copy's fully warmed cache
 * stayed strongly reachable until the daemon spent more time collecting than parsing).
 * <p>
 * Two deterministic mechanisms bound the cache while a copy is active, and they compose:
 * <ul>
 * <li>a <em>size ceiling</em> — {@code groovy.antlr4.cache.size} DFA states retained
 *     across the shared ATN; exceeding it replaces the wrapper with a fresh one. The
 *     default scales with the heap so a large daemon keeps a warm working set while a
 *     small JVM stays protected.</li>
 * <li>a <em>parse counter</em> — replaces the wrapper every
 *     {@code groovy.antlr4.cache.threshold} parses, for workloads that want a fixed
 *     cadence. Off by default.</li>
 * </ul>
 * A replaced wrapper is never mutated: parses in flight finish undisturbed on the old
 * ATN, which becomes collectable as they complete. Nothing is cleared in place, so
 * parsing no longer serialises against a write lock.
 */
public abstract class AtnManager {
    // Retained for binary compatibility with earlier releases in which parsing held the
    // read side while an in-place cache clear held the write side. An over-limit cache is
    // now replaced rather than cleared, so nothing acquires these locks any more.
    private static final ReentrantReadWriteLock RRWL = new ReentrantReadWriteLock(true);
    /**
     * Read lock of the fair lock kept for binary compatibility.
     * Parsing does not acquire it. An over-limit cache is replaced rather than cleared
     * under the matching write lock.
     */
    public static final ReentrantReadWriteLock.ReadLock READ_LOCK = RRWL.readLock();

    private static final String DFA_CACHE_THRESHOLD_OPT = "groovy.antlr4.cache.threshold";
    /**
     * Parses between deterministic replacements when {@value #DFA_CACHE_THRESHOLD_OPT} is not
     * set. Off by default: a blind parse counter cannot tell a large cache from a small warm
     * one, so it drops warm state regardless and taxes builds that were never in trouble.
     * {@link #DFA_CACHE_SIZE_LIMIT_OPT} bounds the cache by what it actually holds instead.
     * Set the property to a positive value for a fixed, predictable cadence, or to a negative
     * value to switch off dropping altogether (the caches then live on the static ATN and are
     * never released).
     */
    private static final long DFA_CACHE_THRESHOLD_DEFAULT = 0L;
    private static final long DFA_CACHE_THRESHOLD;
    private static final boolean CACHE_DROPPING_ENABLED;
    private static final String DFA_CACHE_SIZE_LIMIT_OPT = "groovy.antlr4.cache.size";
    /**
     * DFA states retained across the shared ATN before the wrapper is replaced.
     * <p>
     * The default scales with the heap: parsing at scale wants its working set — a
     * multi-module documentation or compile pass reaches the high tens of thousands of states
     * (~4KB per state all-in, prediction contexts and configs tracking states at stable
     * ratios) — while a small JVM must be protected from the cache alone filling it. The
     * scale is ~32 states per MB of max heap (≈ an eighth of the heap at the ~4KB/state
     * observed cost), floored at 20,000 so tiny heaps keep a usable cache, and capped at
     * 500,000. A 512MB heap lands on the floor; 2GB allows ~64k states; a 5GB build daemon
     * allows ~164k, which keeps a full multi-module documentation pass warm.
     * <p>
     * Unlike GC-driven release the ceiling does not depend on {@code SoftReference} policy,
     * so it behaves identically on HotSpot and on SubstrateVM, where that policy differs.
     * Set the property to a positive value to fix the ceiling, or to {@code 0} or a negative
     * value to disable it.
     */
    private static final long DFA_CACHE_SIZE_LIMIT_DEFAULT;
    private static final long DFA_CACHE_SIZE_LIMIT;
    private volatile SoftReference<AtnWrapper> atnWrapperSoftReference; // NOSONAR java:S3077 — SoftReference is an immutable holder; volatile publishes the replacement

    static {
        long t = SystemUtil.getLongSafe(DFA_CACHE_THRESHOLD_OPT, DFA_CACHE_THRESHOLD_DEFAULT);
        // A negative threshold has always meant "never drop the DFA cache". Preserve that
        // escape hatch explicitly: it switches off both the counter and the size ceiling.
        CACHE_DROPPING_ENABLED = gcCanaryEnabled(t);
        DFA_CACHE_THRESHOLD = counterThreshold(t);

        DFA_CACHE_SIZE_LIMIT_DEFAULT = heapScaledSizeLimit();
        long limit = SystemUtil.getLongSafe(DFA_CACHE_SIZE_LIMIT_OPT, DFA_CACHE_SIZE_LIMIT_DEFAULT);
        DFA_CACHE_SIZE_LIMIT = Math.max(limit, 0L);
    }

    /** Default size ceiling for the current JVM: ~32 states per MB of max heap, in [20k, 500k]. */
    static long heapScaledSizeLimit() {
        long maxBytes = Runtime.getRuntime().maxMemory();
        if (maxBytes == Long.MAX_VALUE) return 500_000L;
        long scaled = (maxBytes >> 20) * 32;
        return Math.max(20_000L, Math.min(500_000L, scaled));
    }

    /** Whether the cache is bounded by how much it holds. Zero disables the ceiling. */
    private static boolean isSizeLimitEnabled() {
        return 0 != DFA_CACHE_SIZE_LIMIT;
    }

    /**
     * Whether cache dropping is active for the given raw threshold. Only an explicit
     * negative value ("never drop") switches it off; in particular a positive threshold
     * leaves the size ceiling on, so the two mechanisms compose. (The name reflects the
     * former GC-canary mechanism this policy gated; the softly referenced wrapper now
     * plays that role structurally.)
     */
    static boolean gcCanaryEnabled(final long rawThreshold) {
        return rawThreshold >= 0;
    }

    /** Parses between deterministic replacements for the given raw threshold; 0 means off. */
    static long counterThreshold(final long rawThreshold) {
        return Math.max(rawThreshold, 0L);
    }

    /**
     * Whether the parse counter is active. Zero (the default) leaves the size ceiling as the
     * only deterministic mechanism; any positive value adds a fixed cadence on top of it.
     */
    private static boolean isThresholdCleanupEnabled() {
        return 0 != DFA_CACHE_THRESHOLD;
    }

    /** Whether this manager's caches may be dropped at all (global policy and per-manager switch). */
    final boolean droppingEnabled() {
        return CACHE_DROPPING_ENABLED && shouldClearDfaCache();
    }

    /**
     * Returns the ATN from the current wrapper.
     * {@link AtnWrapper#checkAndReplace()} may publish a fresh wrapper for a later
     * call, but the ATN returned here is still the one that was current. Parses
     * already holding it keep using it.
     *
     * @return the current wrapper's ATN
     */
    public ATN getATN() {
        return getAtnWrapper().checkAndReplace();
    }

    /**
     * Creates the wrapper installed when {@link #getAtnWrapper()} finds no live reference.
     *
     * @return a wrapper owning a fresh ATN
     */
    protected abstract AtnWrapper createAtnWrapper();

    /**
     * Returns the softly referenced wrapper, creating one when the reference is
     * missing or has been cleared. The check outside the lock is repeated inside
     * it, so concurrent misses install a single wrapper.
     *
     * @return the live wrapper, never a cleared reference
     */
    protected AtnWrapper getAtnWrapper() {
        SoftReference<AtnWrapper> ref = atnWrapperSoftReference;
        AtnWrapper atnWrapper = (null == ref) ? null : ref.get();
        if (null == atnWrapper) {
            synchronized (this) {
                ref = atnWrapperSoftReference;
                atnWrapper = (null == ref) ? null : ref.get();
                if (null == atnWrapper) {
                    atnWrapper = createAtnWrapper();
                    atnWrapperSoftReference = new SoftReference<>(atnWrapper);
                }
            }
        }
        return atnWrapper;
    }

    /**
     * Install a fresh wrapper if {@code observed} is still current. Parses in flight keep
     * the old ATN through their interpreters and finish undisturbed; the retired cache
     * graph becomes collectable as they complete. The identity check makes concurrent
     * observations of the same crossing collapse into a single replacement.
     */
    private void replaceAtnWrapper(final AtnWrapper observed) {
        synchronized (this) {
            SoftReference<AtnWrapper> ref = atnWrapperSoftReference;
            if (null != ref && ref.get() == observed) {
                atnWrapperSoftReference = new SoftReference<>(createAtnWrapper());
            }
        }
    }

    /**
     * Whether this manager participates in cache dropping.
     * Dropping also requires the global policy: a negative
     * {@code groovy.antlr4.cache.threshold} switches it off.
     *
     * @return {@code true} when this manager's caches may be retired
     */
    protected abstract boolean shouldClearDfaCache();

    /**
     * One ATN and the counters that decide when to retire it.
     * The manager holds the wrapper in a {@link SoftReference}. Replacement
     * publishes another wrapper; this instance and its ATN are left as they are
     * so parses already using them can finish.
     */
    protected class AtnWrapper {
        private final ATN atn;
        private final AtomicLong counter = new AtomicLong(0);
        private final AtomicBoolean replacing = new AtomicBoolean();

        /**
         * Retains {@code atn}. The parse counter starts at zero and the retirement
         * flag starts clear.
         *
         * @param atn the ATN whose decision DFAs this wrapper owns
         */
        public AtnWrapper(ATN atn) {
            this.atn = atn;
        }

        /**
         * Returns this wrapper's ATN. When dropping is off, that is all this does.
         * When the parse-counter threshold is positive, the counter is incremented
         * and a multiple of the threshold retires this wrapper before the size
         * check. When the DFA-state ceiling is enabled and the summed
         * {@code states.size()} of the decision DFAs is over it, one thread retires
         * this wrapper; the flag is not cleared, so a later call does not retire it
         * again. Retirement publishes a new wrapper for a later {@link AtnManager#getATN()}.
         * This call still returns {@code atn}.
         *
         * @return this wrapper's ATN
         */
        public ATN checkAndReplace() {
            if (!droppingEnabled()) {
                return atn;
            }

            if (isThresholdCleanupEnabled() && 0 == counter.incrementAndGet() % DFA_CACHE_THRESHOLD) {
                replaceAtnWrapper(this);
                return atn;
            }

            // Exactly one thread retires a wrapper per crossing: without the guard every
            // concurrent parser sees the same over-limit count and each installs its own
            // fresh wrapper, discarding the warmth the previous replacement just started
            // to accumulate.
            if (isSizeLimitEnabled() && dfaStateCount() > DFA_CACHE_SIZE_LIMIT
                    && replacing.compareAndSet(false, true)) {
                // not reset: this wrapper is retired for good once replaced
                replaceAtnWrapper(this);
            }

            return atn;
        }

        /**
         * States currently retained across the shared ATN's decision DFAs. Cheap enough to read
         * on every parse: one size read per decision, against a whole source unit's parsing.
         */
        private long dfaStateCount() {
            long n = 0;
            DFA[] decisionToDFA = atn.decisionToDFA;
            if (null != decisionToDFA) {
                for (DFA dfa : decisionToDFA) {
                    if (null != dfa) n += dfa.states.size();
                }
            }
            return n;
        }
    }
}
