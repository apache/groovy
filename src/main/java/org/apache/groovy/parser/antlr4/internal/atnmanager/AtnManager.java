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
 * Manage ATN to avoid memory leak
 */
public abstract class AtnManager {
    private static final ReentrantReadWriteLock RRWL = new ReentrantReadWriteLock(true);
    private static final ReentrantReadWriteLock.WriteLock WRITE_LOCK = RRWL.writeLock();
    public static final ReentrantReadWriteLock.ReadLock READ_LOCK = RRWL.readLock();
    private static final String DFA_CACHE_THRESHOLD_OPT = "groovy.antlr4.cache.threshold";
    private static final long DFA_CACHE_THRESHOLD;
    private SoftReference<AtnWrapper> atnWrapperSoftReference;

    static {
        long t = SystemUtil.getLongSafe(DFA_CACHE_THRESHOLD_OPT, 0L);
        if (t < 0) {
            t = Long.MAX_VALUE;
        }

        DFA_CACHE_THRESHOLD = t;
    }

    private static boolean isSmartCleanupEnabled() {
        return 0 == DFA_CACHE_THRESHOLD;
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
                if (shouldClearDfaCache() && isSmartCleanupEnabled()) {
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
            if (!shouldClearDfaCache() || isSmartCleanupEnabled()) {
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
