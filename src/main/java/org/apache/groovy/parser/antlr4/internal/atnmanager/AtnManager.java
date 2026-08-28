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
import org.apache.groovy.util.SystemUtil;

import java.lang.ref.SoftReference;
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
    private static final long DFA_CACHE_THRESHOLD;
    private static final boolean GC_CANARY_ENABLED;
    private SoftReference<AtnWrapper> atnWrapperSoftReference;

    static {
        long t = SystemUtil.getLongSafe(DFA_CACHE_THRESHOLD_OPT, 0L);
        // A negative threshold has always meant "never clear the DFA cache". Preserve that
        // escape hatch explicitly: it now switches off both mechanisms, because the canary
        // is no longer implied by a zero threshold.
        GC_CANARY_ENABLED = gcCanaryEnabled(t);
        DFA_CACHE_THRESHOLD = counterThreshold(t);
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

        public AtnWrapper(ATN atn) {
            this.atn = atn;
        }

        public ATN checkAndClear() {
            if (!shouldClearDfaCache() || !isThresholdCleanupEnabled()) {
                return atn;
            }

            if (0 != counter.incrementAndGet() % DFA_CACHE_THRESHOLD) {
                return atn;
            }

            clearDFA();

            return atn;
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
