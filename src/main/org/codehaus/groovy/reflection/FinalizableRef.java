package org.codehaus.groovy.reflection;

import java.lang.ref.*;

public final class FinalizableRef {
    static final ReferenceQueue finalizableQueue = new MyReferenceQueue();

    static class SoftRef<T> extends SoftReference<T> {
        public SoftRef(T referent) {
            super(referent, finalizableQueue);
        }
    }

    static class WeakRef<T> extends WeakReference<T> {
        public WeakRef(T referent) {
            super(referent, finalizableQueue);
        }
    }

    static class PhantomRef<T> extends PhantomReference<T> {
        public PhantomRef(T referent) {
            super(referent, finalizableQueue);
        }
    }

    public static class MyReferenceQueue extends ReferenceQueue {
        public MyReferenceQueue() {
          Thread thread = new Thread() {
              public void run() {
                  while (true) {
                    try {
                        remove().clear();
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
