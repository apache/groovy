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
                // a small chance that the value will go beyond Integer.MAX_VALUe
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
}
