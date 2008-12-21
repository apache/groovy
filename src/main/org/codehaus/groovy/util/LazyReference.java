package org.codehaus.groovy.util;

import org.codehaus.groovy.util.ManagedReference.ReferenceBundle;


/**
 * Soft reference with lazy initialization under lock
 */
public abstract class LazyReference<T> extends LockableObject {
    private final static ManagedReference INIT = new ManagedReference(ReferenceType.HARD,null,null){}; 
    private final static ManagedReference NULL_REFERENCE = new ManagedReference(ReferenceType.HARD,null,null){};
    private ManagedReference<T> reference = INIT;
    private final ReferenceBundle bundle;
    
    public LazyReference(ReferenceBundle bundle) { 
        this.bundle = bundle;
    }
    
    public T get() {
        ManagedReference<T> resRef = reference;
        if (resRef == INIT) return getLocked(false);
        if (resRef == NULL_REFERENCE) return null;
        T res = resRef.get();
        // res== null means it got collected
        if (res==null) return getLocked(true);
        return res;
    }

    private T getLocked (boolean force) {
        lock ();
        try {
            ManagedReference<T> resRef = reference;
            if (!force && resRef != INIT) return resRef.get();
            T res = initValue();
            if (res == null) {
                reference = NULL_REFERENCE;
            } else {
                reference = new ManagedReference<T>(bundle,res);
            }
            return res;
        } finally {
            unlock();
        }
    }

    public void clear() {
        reference = INIT;
    }

    public abstract T initValue();

    public String toString() {
        T res = reference.get();
        if (res == null)
          return "<null>";
        else
          return res.toString();
    }
}
