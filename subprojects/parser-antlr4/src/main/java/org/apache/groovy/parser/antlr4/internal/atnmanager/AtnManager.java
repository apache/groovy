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
    private static final int DEFAULT_DFA_CACHE_THRESHOLD = 64;
    private static final int MIN_DFA_CACHE_THRESHOLD = 2;
    private static final int DFA_CACHE_THRESHOLD;

    static {
        int t = DEFAULT_DFA_CACHE_THRESHOLD;

        try {
            t = Integer.parseInt(System.getProperty(DFA_CACHE_THRESHOLD_OPT));

            // cache threshold should be at least MIN_DFA_CACHE_THRESHOLD for better performance
            t = t < MIN_DFA_CACHE_THRESHOLD ? MIN_DFA_CACHE_THRESHOLD : t;
        } catch (Exception e) {
            // ignored
        }

        DFA_CACHE_THRESHOLD = t;
    }


    public abstract ATN getATN();

    protected abstract boolean shouldClearDfaCache();

    protected class AtnWrapper {
        private final ATN atn;
        private final AtomicLong counter = new AtomicLong(0);

        public AtnWrapper(ATN atn) {
            this.atn = atn;
        }

        public ATN checkAndClear() {
            if (!shouldClearDfaCache()) {
                return atn;
            }

            if (0 != counter.incrementAndGet() % DFA_CACHE_THRESHOLD) {
                return atn;
            }

            WRITE_LOCK.lock();
            try {
                atn.clearDFA();
            } finally {
                WRITE_LOCK.unlock();
            }

            return atn;
        }
    }
}
