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
 * Two independent mechanisms drop the shared DFA cache, and they compose:
 * <ul>
 * <li>a <em>GC canary</em> — the softly referenced {@link AtnWrapper}. Its collection
 *     signals memory pressure, so the cache is dropped when the next parse observes the
 *     cleared reference. Active unless clearing is switched off entirely.</li>
 * <li>a <em>parse counter</em> — clears every {@code groovy.antlr4.cache.threshold}
 *     parses, bounding the cache deterministically rather than waiting for the collector.
 *     Off by default.</li>
 * </ul>
 * The two used to be mutually exclusive: setting a threshold silently switched the canary
 * off, so a value chosen to bound growth removed the only mechanism that responded to
 * actual memory pressure and could make matters worse (GROOVY-12142).
 */
public abstract class AtnManager {
    private static final ReentrantReadWriteLock RRWL = new ReentrantReadWriteLock(true);
    private static final ReentrantReadWriteLock.WriteLock WRITE_LOCK = RRWL.writeLock();
    public static final ReentrantReadWriteLock.ReadLock READ_LOCK = RRWL.readLock();
    private static final String DFA_CACHE_THRESHOLD_OPT = "groovy.antlr4.cache.threshold";
    /**
     * Parses between deterministic clears when {@value #DFA_CACHE_THRESHOLD_OPT} is not set.
     * <p>
     * The GC canary alone is not sufficient out of the box: it is only observed on the parse
     * path, so under sustained pressure the cache can grow unchecked between observations —
     * a long-lived daemon parsing concurrently across many modules then spends its time in GC
     * rather than parsing. A deterministic ceiling bounds the cache regardless of collector
     * timing. Set the property to {@code 0} to restore canary-only behaviour, or to a negative
     * value to disable clearing altogether.
     * <p>
     * Off by default: a blind parse counter cannot tell a large cache from a small warm one, so
     * it clears regardless and taxes builds that were never in trouble. {@link
     * #DFA_CACHE_SIZE_LIMIT_OPT} bounds the cache by what it actually holds instead. This
     * remains available for workloads that want a fixed, predictable clearing cadence.
     */
    private static final long DFA_CACHE_THRESHOLD_DEFAULT = 0L;
    private static final long DFA_CACHE_THRESHOLD;
    private static final boolean GC_CANARY_ENABLED;
    private static final String DFA_CACHE_SIZE_LIMIT_OPT = "groovy.antlr4.cache.size";
    /**
     * DFA states retained across the shared ATN before the cache is dropped.
     * <p>
     * This is the default ceiling because it is the only one of the three mechanisms that costs
     * nothing when the cache is small: a project that never reaches the limit never clears, so
     * it keeps a fully warm cache, while a long-lived daemon parsing at scale stays bounded.
     * Unlike the GC canary it does not depend on {@code SoftReference} policy, so it behaves
     * identically on HotSpot and on SubstrateVM, where that policy differs; and unlike the
     * removed cleaner thread it starts nothing, so it cannot pin a container's class loader
     * (GROOVY-12142).
     * <p>
     * Calibration: parsing 6849 Groovy sources with no ceiling grows the cache to ~179,000
     * states / ~13.5M ATNConfigs. States track configs at a stable ~1:75, so this limit bounds
     * the cache at roughly 1.5M configs. Set the property to {@code 0} or a negative value to
     * disable the size ceiling.
     */
    private static final long DFA_CACHE_SIZE_LIMIT_DEFAULT = 20_000L;
    private static final long DFA_CACHE_SIZE_LIMIT;
    private SoftReference<AtnWrapper> atnWrapperSoftReference;

    static {
        long t = SystemUtil.getLongSafe(DFA_CACHE_THRESHOLD_OPT, DFA_CACHE_THRESHOLD_DEFAULT);
        // A negative threshold has always meant "never clear the DFA cache". Preserve that
        // escape hatch explicitly: it now switches off both mechanisms, because the canary
        // is no longer implied by a zero threshold.
        GC_CANARY_ENABLED = gcCanaryEnabled(t);
        DFA_CACHE_THRESHOLD = counterThreshold(t);

        long limit = SystemUtil.getLongSafe(DFA_CACHE_SIZE_LIMIT_OPT, DFA_CACHE_SIZE_LIMIT_DEFAULT);
        DFA_CACHE_SIZE_LIMIT = Math.max(limit, 0L);
    }

    /** Whether the cache is bounded by how much it holds. Zero disables the ceiling. */
    private static boolean isSizeLimitEnabled() {
        return 0 != DFA_CACHE_SIZE_LIMIT;
    }

    /**
     * Whether the GC canary is active for the given raw threshold. Only an explicit
     * negative value ("never clear") switches it off; in particular a positive threshold
     * leaves it on, so the two mechanisms compose.
     */
    static boolean gcCanaryEnabled(final long rawThreshold) {
        return rawThreshold >= 0;
    }

    /** Parses between deterministic clears for the given raw threshold; 0 means off. */
    static long counterThreshold(final long rawThreshold) {
        return Math.max(rawThreshold, 0L);
    }

    /**
     * Whether the parse counter is active. Zero (the default) leaves the GC canary as the
     * only mechanism; any positive value adds a deterministic ceiling on top of it.
     */
    private static boolean isThresholdCleanupEnabled() {
        return 0 != DFA_CACHE_THRESHOLD;
    }

    public ATN getATN() {
        return getAtnWrapper().checkAndClear();
    }

    protected abstract AtnWrapper createAtnWrapper();

    protected AtnWrapper getAtnWrapper() {
        return getAtnWrapper(true);
    }

    private AtnWrapper getAtnWrapper(final boolean useSoftRef) {
        if (!useSoftRef) {
            return createAtnWrapper();
        }

        AtnWrapper atnWrapper;
        synchronized (this) {
            if (null == atnWrapperSoftReference) {
                atnWrapper = createAtnWrapper();
                atnWrapperSoftReference = new SoftReference<>(atnWrapper);
            } else if (null == (atnWrapper = atnWrapperSoftReference.get())) {
                // The softly referenced wrapper is a GC canary: its collection
                // signals memory pressure, so drop the shared DFA cache along
                // with allocating the replacement. Detected here on the parse
                // path rather than by a reference-queue thread — a cleanup
                // thread per manager can never terminate and so pins the
                // defining class loader for the life of the JVM, leaking every
                // container redeployment (GROOVY-12142).
                atnWrapper = createAtnWrapper();
                if (shouldClearDfaCache() && GC_CANARY_ENABLED) {
                    atnWrapper.clearDFA();
                }
                atnWrapperSoftReference = new SoftReference<>(atnWrapper);
            }
        }
        return atnWrapper;
    }

    protected abstract boolean shouldClearDfaCache();

    protected class AtnWrapper {
        private final ATN atn;
        private final AtomicLong counter = new AtomicLong(0);
        private final AtomicBoolean clearing = new AtomicBoolean();

        public AtnWrapper(ATN atn) {
            this.atn = atn;
        }

        public ATN checkAndClear() {
            if (!shouldClearDfaCache()) {
                return atn;
            }

            if (isThresholdCleanupEnabled() && 0 == counter.incrementAndGet() % DFA_CACHE_THRESHOLD) {
                clearDFA();
                return atn;
            }

            // Exactly one thread clears per crossing. Without the guard every concurrent
            // parser sees the same over-limit count and queues its own clear on the fair
            // write lock, so a single crossing becomes a herd of clear-and-rebuild cycles
            // that blocks every reader and churns the whole cache repeatedly.
            if (isSizeLimitEnabled() && dfaStateCount() > DFA_CACHE_SIZE_LIMIT
                    && clearing.compareAndSet(false, true)) {
                try {
                    clearDfaIfStillOverLimit();
                } finally {
                    clearing.set(false);
                }
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

        private void clearDfaIfStillOverLimit() {
            WRITE_LOCK.lock();
            try {
                // re-check: a clear may have completed while this thread queued for the lock
                if (dfaStateCount() > DFA_CACHE_SIZE_LIMIT) {
                    atn.clearDFA();
                }
            } finally {
                WRITE_LOCK.unlock();
            }
        }

        public void clearDFA() {
            WRITE_LOCK.lock();
            try {
                atn.clearDFA();
            } finally {
                WRITE_LOCK.unlock();
            }
        }
    }
}
