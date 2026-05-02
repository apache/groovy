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
package org.codehaus.groovy.util;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates cleanup of managed references using idling, callback-driven, or threaded processing.
 */
public class ReferenceManager {
    private static class ThreadedReferenceManager extends ReferenceManager {
        private final Thread thread;
        private volatile boolean shouldRun = true;

        /**
         * Creates a manager backed by a daemon thread that drains the supplied queue.
         *
         * @param queue the queue monitored by the cleanup thread
         */
        public ThreadedReferenceManager(ReferenceQueue queue) {
            super(queue);
            thread = new Thread(() -> {
                ReferenceQueue queue1 = getReferenceQueue();
                java.lang.ref.Reference r=null;
                while (shouldRun) {
                    try {
                        r = queue1.remove(1000);
                    }  catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (r==null) continue;

                    if (r instanceof Reference ref) {
                        Finalizable holder = ref.getHandler();
                        if (holder!=null) holder.finalizeReference();
                    }
                    r.clear();
                    r=null;
                }
            });
            thread.setContextClassLoader(null);
            thread.setDaemon(true);
            thread.setName(ThreadedReferenceManager.class.getName());
            thread.start();
        }

        /** {@inheritDoc} */
        @Override
        public void stopThread() {
            shouldRun = false;
            thread.interrupt();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ReferenceManager(threaded, thread="+thread+")";
        }
    }

    /**
     * Creates a manager backed by a daemon thread that drains the supplied queue.
     *
     * @param queue the queue to drain
     * @return a threaded reference manager
     */
    public static ReferenceManager createThreadedManager(ReferenceQueue queue) {
        return new ThreadedReferenceManager(queue);
    }

    /**
     * Creates a manager that only processes queued references when asked explicitly.
     *
     * @param queue the queue to inspect
     * @return an idling reference manager
     */
    public static ReferenceManager createIdlingManager(ReferenceQueue queue) {
        return new ReferenceManager(queue);
    }

    /**
     * Creates a manager that removes stale entries when new references are created.
     *
     * @param queue the queue to inspect
     * @return a callback-backed reference manager
     */
    public static ReferenceManager createCallBackedManager(ReferenceQueue queue) {
        return new CallBackedManager(queue);
    }

    private static class CallBackedManager extends ReferenceManager {

        private static final ConcurrentHashMap<ReferenceQueue, ReferenceManager> queuesInProcess =
                new ConcurrentHashMap<ReferenceQueue, ReferenceManager>(4, 0.9f, 2);

        /**
         * Creates a callback-driven manager for the supplied queue.
         *
         * @param queue the queue whose stale references should be drained on demand
         */
        public CallBackedManager(ReferenceQueue queue) {
            super(queue);
        }

        /** {@inheritDoc} */
        @Override
        public void removeStallEntries() {
            ReferenceQueue queue = getReferenceQueue();
            if (queuesInProcess.putIfAbsent(queue, this) == null) {
                try {
                    removeStallEntries0(queue);
                } finally {
                    queuesInProcess.remove(queue);
                }
            }
        }

        private static void removeStallEntries0(ReferenceQueue queue) {
            for(;;) {
                java.lang.ref.Reference r = queue.poll();
                if (r==null) break;

                if (r instanceof Reference ref) {
                    Finalizable holder = ref.getHandler();
                    if (holder!=null) holder.finalizeReference();
                }
                r.clear();
                r=null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void afterReferenceCreation(Reference r) {
            removeStallEntries();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ReferenceManager(callback)";
        }
    }

    /**
     * Creates a manager that starts in idling mode and switches to the callback manager after a threshold.
     *
     * @param queue the queue to inspect
     * @param callback the manager to delegate to after the threshold is crossed
     * @param threshold the number of created references before switching managers
     * @return a thresholded reference manager
     */
    public static ReferenceManager createThresholdedIdlingManager(final ReferenceQueue queue, final ReferenceManager callback, final int threshold) {
        if (threshold<0) throw new IllegalArgumentException("threshold must not be below 0.");

        return new ReferenceManager(queue){
            private AtomicInteger refCnt = new AtomicInteger();
            private volatile ReferenceManager manager = createIdlingManager(queue);

            /** {@inheritDoc} */
            @Override
            public void afterReferenceCreation(Reference r) {
                if (manager==callback) {
                    callback.afterReferenceCreation(r);
                    return;
                }
                // we use the idle manager, so let us use the reference counter
                // we change the manager once the threshold is reached. There is
                // a small chance that the value will go beyond Integer.MAX_VALUE
                // so we check for values below 0 too. If threshold is low, then
                // this is unlikely to happen. If threshold is high, then we
                // have all negative values as fall back
                int count = refCnt.incrementAndGet();
                if (count>threshold || count<0) {
                    manager = callback;
                    callback.afterReferenceCreation(r);
                }
            }

            /** {@inheritDoc} */
            @Override
            public void removeStallEntries() {
                manager.removeStallEntries();
            }

            /** {@inheritDoc} */
            @Override
            public void stopThread() {
                manager.stopThread();
            }

            /** {@inheritDoc} */
            @Override
            public String toString() {
                return "ReferenceManager(thresholded, current manager="+manager+", threshold="+refCnt.get()+"/"+threshold+")";
            }
        };
    }

    private ReferenceQueue queue;

    /**
     * Creates a manager for the supplied queue.
     *
     * @param queue the queue containing collected references, or {@code null} when not queue-backed
     */
    public ReferenceManager(ReferenceQueue queue) {
        this.queue = queue;
    }

    /**
     * Returns the queue monitored by this manager.
     *
     * @return the managed reference queue, or {@code null}
     */
    protected ReferenceQueue getReferenceQueue() {
        return queue;
    }

    /**
     * Hook invoked after a new managed reference has been created.
     *
     * @param r the created reference
     */
    public void afterReferenceCreation(Reference r) {}

    /**
     * Removes any currently queued stale references.
     */
    public void removeStallEntries() {}

    /**
     * Stops any background processing owned by this manager.
     */
    public void stopThread() {}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ReferenceManager(idling)";
    }

    /**
     * @deprecated use {@link ReferenceBundle#getSoftBundle()}
     */
    @Deprecated
    public static ReferenceBundle getDefaultSoftBundle() {
        return ReferenceBundle.getSoftBundle();
    }

    /**
     * @deprecated use {@link ReferenceBundle#getWeakBundle()}
     */
    @Deprecated
    public static ReferenceBundle getDefaultWeakBundle() {
        return ReferenceBundle.getWeakBundle();
    }
}
