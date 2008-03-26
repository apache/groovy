package org.codehaus.groovy.reflection;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reference with lazy initialization under lock
 */
public abstract class LazyReference<T> {

    private volatile T value;
    private final Lock lock;

    public LazyReference() {
        this(new ReentrantLock());
    }

    public LazyReference(Lock lock) {
        this.lock = lock;
    }

    T get () {
        T t = value;
        if (t == null) {
            lock.lock();
            try {
                t = value;
                if (t != null)
                  return t;

                t = initValue();
                value = t;
                return t;
            }
            finally {
                lock.unlock();
            }
        }
        return t;
    }

    public abstract T initValue();
}
