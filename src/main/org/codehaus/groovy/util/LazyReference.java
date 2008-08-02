package org.codehaus.groovy.util;

/**
 * Reference with lazy initialization under lock
 */
public abstract class LazyReference<T> extends LockableObject {
    private T value;

    public T get() {
        T res = value;
        if (res != null)
            return res;

        return getLocked();
    }

    private T getLocked () {
        lock ();
        try {
            T res = initValue();
            value = res;
            return res;
        }
        finally {
            unlock();
        }
    }

    public T getNullable() {
        return value;
    }

    public abstract T initValue();
}
