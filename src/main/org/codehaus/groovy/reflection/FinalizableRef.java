package org.codehaus.groovy.reflection;

import java.lang.ref.*;

public interface FinalizableRef {
    void finalizeRef();

    static final ReferenceQueue finalizableQueue = new MyReferenceQueue();

    static class SoftRef<T> extends SoftReference<T> implements FinalizableRef{
        public SoftRef(T referent) {
            super(referent, finalizableQueue);
        }

        public void finalizeRef() {
        }
    }

    static class WeakRef<T> extends WeakReference<T> implements FinalizableRef{
        public WeakRef(T referent) {
            super(referent, finalizableQueue);
        }

        public void finalizeRef() {
        }
    }

    static class PhantomRef<T> extends PhantomReference<T> implements FinalizableRef{
        public PhantomRef(T referent) {
            super(referent, finalizableQueue);
        }

        public void finalizeRef() {
        }
    }

    public static class MyReferenceQueue extends ReferenceQueue {
        public MyReferenceQueue() {
          new CollectorThread().start();
        }

        static class CollectorThread extends Thread {
            public CollectorThread() {
                setDaemon(true);
                setPriority(Thread.MAX_PRIORITY-2);
            }

            public void run() {
                while (true) {
                    Reference ref;
                    while ((ref = finalizableQueue.poll()) != null)
                      ((FinalizableRef)ref).finalizeRef();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }
}
