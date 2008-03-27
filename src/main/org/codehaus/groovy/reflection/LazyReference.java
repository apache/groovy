package org.codehaus.groovy.reflection;

/**
 * Reference with lazy initialization under lock
 */
public abstract class LazyReference<T> extends LockableObject {
    private volatile T value;

    public T get() {
        T res = value;
        if (res != null)
            return res;

        lock ();
        try {
            value = initValue();
            return value;
        }
        finally {
            unlock();
        }
    }

    public abstract T initValue();
}
