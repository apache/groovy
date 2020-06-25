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

public class ReferenceManager {
    private static class ThreadedReferenceManager extends ReferenceManager {
        private final Thread thread;
        private volatile boolean shouldRun = true; 
        public ThreadedReferenceManager(ReferenceQueue queue) {
            super(queue);
            thread = new Thread(() -> {
                ReferenceQueue queue1 = getReferenceQueue();
                java.lang.ref.Reference r=null;
                while (shouldRun) {
                    try {
                        r = queue1.remove(1000);
                    }  catch (InterruptedException e) {
                        break;
                    }
                    if (r==null) continue;

                    if (r instanceof Reference) {
                        Reference ref = (Reference) r;
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
        @Override
        public void stopThread() {
            shouldRun = false;
            thread.interrupt();
        }        
        @Override
        public String toString() {
            return "ReferenceManager(threaded, thread="+thread+")";
        }
    }

    public static ReferenceManager createThreadedManager(ReferenceQueue queue) {
        return new ThreadedReferenceManager(queue);
    }
    public static ReferenceManager createIdlingManager(ReferenceQueue queue) {
        return new ReferenceManager(queue);
    }
    public static ReferenceManager createCallBackedManager(ReferenceQueue queue) {
        return new CallBackedManager(queue);
    }

    private static class CallBackedManager extends ReferenceManager {

        private static final ConcurrentHashMap<ReferenceQueue, ReferenceManager> queuesInProcess =
                new ConcurrentHashMap<ReferenceQueue, ReferenceManager>(4, 0.9f, 2);

        public CallBackedManager(ReferenceQueue queue) {
            super(queue);
        }

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

                if (r instanceof Reference) {
                    Reference ref = (Reference) r;
                    Finalizable holder = ref.getHandler();
                    if (holder!=null) holder.finalizeReference();
                }
                r.clear();
                r=null;
            }
        }

        @Override
        public void afterReferenceCreation(Reference r) {
            removeStallEntries();
        }

        @Override
        public String toString() {
            return "ReferenceManager(callback)";
        }
    }

    public static ReferenceManager createThresholdedIdlingManager(final ReferenceQueue queue, final ReferenceManager callback, final int threshold) {
        if (threshold<0) throw new IllegalArgumentException("threshold must not be below 0.");
       
        return new ReferenceManager(queue){
            private final AtomicInteger refCnt = new AtomicInteger();
            private volatile ReferenceManager manager = createIdlingManager(queue);
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
            @Override
            public void removeStallEntries() {
                manager.removeStallEntries();
            }
            @Override
            public void stopThread() {
                manager.stopThread();
            }
            @Override
            public String toString() {
                return "ReferenceManager(thresholded, current manager="+manager+", threshold="+refCnt.get()+"/"+threshold+")";
            }
        };        
    }  
    
    private final ReferenceQueue queue;
    
    public ReferenceManager(ReferenceQueue queue) {
        this.queue = queue;
    }
       
    protected ReferenceQueue getReferenceQueue() {
        return queue;
    }
    
    public void afterReferenceCreation(Reference r) {}
    public void removeStallEntries() {}
    public void stopThread() {}
    
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
