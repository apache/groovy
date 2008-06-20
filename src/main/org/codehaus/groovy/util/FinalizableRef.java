package org.codehaus.groovy.util;

import java.lang.ref.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.LinkedHashSet;

public interface FinalizableRef {

    public void finalizeRef ();

    public static final ReferenceQueue finalizableQueue = new MyReferenceQueue();

    public static abstract class SoftRef<T> extends SoftReference<T> implements FinalizableRef {
        public SoftRef(T referent) {
            super(referent, finalizableQueue);
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
            super(referent, finalizableQueue);
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
            super(referent, finalizableQueue);
        }

        public void finalizeRef () {
        }
    }

    public static abstract class DebugRef<T> extends PhantomRef<T> {
        private static final ConcurrentHashMap<FinalizableRef,Object> anchor = new ConcurrentHashMap<FinalizableRef,Object> ();
        private static final Object ANCHOR = new Object();

        public DebugRef(T referent) {
            super(referent);
            anchor.put(this, ANCHOR);
        }

        public void finalizeRef () {
            anchor.remove(this);
        }
    }

    public static class MyReferenceQueue extends ReferenceQueue {
        public MyReferenceQueue() {
          Thread thread = new Thread() {
              public void run() {
                  while (true) {
                    try {
                        final FinalizableRef ref = (FinalizableRef) remove();
                        ref.finalizeRef();
                    }
                    catch (Throwable t) {//
                    }
                  }
              }
          };
          thread.setDaemon(true);
          thread.start();
        }
    }
}
