package org.codehaus.groovy.reflection;

import java.lang.ref.SoftReference;

/**
 * Soft reference with lazy initialization under lock
 */
public abstract class LazySoftReference<T> extends LockableObject {
    private SoftReference<T> value;

    public T get() {
        SoftReference<T> resRef = value;
        T res;
        if (resRef != null && (res = resRef.get()) != null)
            return res;

        lock ();
        try {
            res = initValue();
            value = new SoftReference<T> (res);
            return res;
        }
        finally {
            unlock();
        }
    }

    public void set (T newVal) {
        value = new SoftReference<T> (newVal);
    }

    public T getNullable() {
        SoftReference<T> resRef = value;
        T res;
        if (resRef == null || (res = resRef.get()) == null) {
            return null;
        }
        return res;
    }

    public abstract T initValue();
}
