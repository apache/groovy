package org.codehaus.groovy.util;

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

        return getLocked();
    }

    private T getLocked () {
        lock ();
        try {
            SoftReference<T> resRef = value;
            T res;
            if (resRef != null && (res = resRef.get()) != null)
                return res;

            res = initValue();
            value = new MySoftRef<T>(res);
            return res;
        }
        finally {
            unlock();
        }
    }

    public void set (T newVal) {
        if (newVal == null)
          value = null;
        else
          value = new MySoftRef<T>(newVal);
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

    protected void finalizeRef() {
        value = null;
    }

    public String toString() {
        T res = getNullable();
        if (res == null)
          return "<null>";
        else
          return res.toString();
    }

    private class MySoftRef<T> extends FinalizableRef.SoftRef<T> {
        public MySoftRef(T res) {
            super(res);
        }

        public void finalizeRef() {
            LazySoftReference.this.finalizeRef();
        }
    }
}
