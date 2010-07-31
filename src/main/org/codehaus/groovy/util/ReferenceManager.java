/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.util;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceManager {
    private static class ThreadedReferenceManager extends ReferenceManager {
        private final Thread thread;
        private volatile boolean shouldRun = true; 
        public ThreadedReferenceManager(ReferenceQueue queue) {
            super(queue);
            thread = new Thread() {
                public void run() {
                    ReferenceQueue queue = getReferenceQueue();
                    java.lang.ref.Reference r=null;
                    while (shouldRun) {
                        try {
                            r = queue.remove(1000);
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
                }
            };
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
        return new ReferenceManager(queue){
            @Override
            public void removeStallEntries() {
                ReferenceQueue queue = getReferenceQueue();
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
        };
    }
    public static ReferenceManager createThresholdedIdlingManager(final ReferenceQueue queue, final ReferenceManager callback, final int threshold) {
        if (threshold<0) throw new IllegalArgumentException("threshold must not be below 0.");
       
        return new ReferenceManager(queue){
            private AtomicInteger refCnt = new AtomicInteger();
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
                }
            }
            @Override
            public String toString() {
                return "ReferenceManager(thresholded, current manager="+manager+", threshold="+refCnt.get()+"/"+threshold+")";
            }
        };        
    }  
    
    private ReferenceQueue queue;
    
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
    
    private static final ReferenceBundle SOFT_BUNDLE, WEAK_BUNDLE;
    static {
        ReferenceQueue queue = new ReferenceQueue();
        ReferenceManager callBack = ReferenceManager.createCallBackedManager(queue);
        ReferenceManager manager  = ReferenceManager.createThresholdedIdlingManager(queue, callBack, 500);
        SOFT_BUNDLE = new ReferenceBundle(manager, ReferenceType.SOFT);
        WEAK_BUNDLE = new ReferenceBundle(manager, ReferenceType.WEAK);
    }
    
    public static ReferenceBundle getDefaultSoftBundle() {
        return SOFT_BUNDLE;
    }
    
    public static ReferenceBundle getDefaultWeakBundle() {
        return WEAK_BUNDLE;
    }
}
