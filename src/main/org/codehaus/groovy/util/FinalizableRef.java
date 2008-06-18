package org.codehaus.groovy.util;

import java.lang.ref.*;

public interface FinalizableRef {

    void finalizeRef ();

    static final ReferenceQueue finalizableQueue = new MyReferenceQueue();

    static abstract class SoftRef<T> extends SoftReference<T> implements FinalizableRef {
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

    static abstract class WeakRef<T> extends WeakReference<T> implements FinalizableRef {
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

    static abstract class PhantomRef<T> extends PhantomReference<T> implements FinalizableRef {
        public PhantomRef(T referent) {
            super(referent, finalizableQueue);
        }

        public void finalizeRef () {
        }
    }

    public static class MyReferenceQueue extends ReferenceQueue {
        public MyReferenceQueue() {
          Thread thread = new Thread() {
              public void run() {
                  while (true) {
                    try {
                        ((FinalizableRef)remove()).finalizeRef();
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
