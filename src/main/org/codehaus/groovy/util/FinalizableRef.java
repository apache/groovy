package org.codehaus.groovy.util;

import java.lang.ref.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import java.util.LinkedHashSet;

public interface FinalizableRef {

    public void finalizeRef ();

    public static abstract class SoftRef<T> extends SoftReference<T> implements FinalizableRef {
        public SoftRef(T referent) {
            super(referent, MyReferenceQueue.getInstance());
        }

        public String toString() {
            T res = get ();
            if (res == null)
              return "<null>";
            else
              return res.toString();
        }

        public void finalizeRef () {
        }
    }

    public static abstract class WeakRef<T> extends WeakReference<T> implements FinalizableRef {
        public WeakRef(T referent) {
            super(referent, MyReferenceQueue.getInstance());
        }

        public String toString() {
            T res = get ();
            if (res == null)
              return "<null>";
            else
              return res.toString();
        }

        public void finalizeRef () {
        }
    }

    public static abstract class PhantomRef<T> extends PhantomReference<T> implements FinalizableRef {
        public PhantomRef(T referent) {
            super(referent, MyReferenceQueue.getInstance());
        }

        public void finalizeRef () {
        }
    }

    /**
     * Phantom reference, which will be kept alive while not finalized.
     * There is no need to store this references anywhere because they will be automatically put in to internal set.
     * Subclasses should overide finalizeRef () method and call super.finalizeRef () to do any additional cleanup or dyagnostic
     */
    public static abstract class DebugRef<T> extends PhantomRef<T> {
        private static final ConcurrentHashMap<FinalizableRef,Object> anchor = new ConcurrentHashMap<FinalizableRef,Object> ();
        private static final Object ANCHOR = new Object();

        public DebugRef(T referent) {
            super(referent);
            anchor.put(this, ANCHOR);
        }

        public void finalizeRef () {
            super.clear();
            anchor.remove(this);
        }
    }

    public static class MyReferenceQueue extends ReferenceQueue {
        private static final MyReferenceQueue finalizableQueue = new MyReferenceQueue();

        private AtomicInteger refCnt = new AtomicInteger();

        private AtomicReference<Thread> thread = new AtomicReference<Thread> ();

        public MyReferenceQueue() {
        }

        private ReferenceQueue getMe () {
            if (thread.get() == null) {
                final Thread newThread = new MyThread();
                if (thread.compareAndSet(null, newThread)) {
                    newThread.setContextClassLoader(null);
                    newThread.setDaemon(true);
                    newThread.setName(FinalizableRef.class.getName());
                    newThread.start();
                }
            }

            refCnt.incrementAndGet();
            return this;
        }

        public static ReferenceQueue getInstance() {
            return finalizableQueue.getMe ();
        }

        private class MyThread extends Thread {
            public void run() {
                while (true) {
                    try {
                        FinalizableRef ref = (FinalizableRef) remove(5000);
                        if (ref == null) {
                            if (refCnt.get() == 0) {
                                thread.compareAndSet(this, null);
                                return;
                            }
                        } else {
                            refCnt.decrementAndGet();
                            ref.finalizeRef();
                        }
                    }
                    catch (Throwable t) {//
                    }
                }
            }
        }
    }
}
