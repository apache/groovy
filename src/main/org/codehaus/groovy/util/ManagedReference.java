package org.codehaus.groovy.util;

/**
 * 
 * 
 * @author Alex Tkachman
 * @author Jochen Theodorou
 */
public class ManagedReference<T> implements Finalizable {
    private static ReferenceManager NULL_MANAGER = new ReferenceManager(null){};
    private Reference<T,ManagedReference<T>> ref;
    private ReferenceManager manager;
    
    public ManagedReference(ReferenceType type, ReferenceManager rmanager, T value) {
        if (rmanager==null) rmanager = NULL_MANAGER;
        this.manager = rmanager;
        this.ref = type.createReference(value, this, rmanager.getReferenceQueue());
        rmanager.afterReferenceCreation(ref);
    }
    
    public ManagedReference(ReferenceBundle bundle, T value) {
        this(bundle.getType(),bundle.getManager(),value);
    }
    
    public final T get() {
        return ref.get();
    }
    
    public final void clear() {
        ref.clear();
        manager.removeStallEntries();
    }
    
    public void finalizeReference(){
        clear();
    }
}   