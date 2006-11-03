/*
 * WeakValueMap.java created on 31.10.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.codehaus.groovy.runtime;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class ReferenceMap extends WeakHashMap {
    
    private static class HardReference extends SoftReference {
        private Object value;
        public HardReference(Object arg) {
            super(null);
            value = arg;
        }
        public Object get() {
            return value;
        }        
    }
    
    private ReferenceQueue queue = new ReferenceQueue();
    
    public ReferenceMap() {
        super();
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }
    
    public Set entrySet() {
        throw new UnsupportedOperationException();
    }
    
    public Object get(Object key) {
        Object ret = super.get(key);
        if (ret!=null) {
            SoftReference weak = (SoftReference) ret;
            ret = weak.get();
            if (ret==null) remove(key);
        }        
        return ret;
    }
    
    public Object put(Object key, Object value) {
        removeDereferencedEntries();
        if (value!=null) {
            value = new SoftReference(value,queue);
        }        
        return super.put(key, value);
    }
    
    public Object putStrong(Object key, Object value) {
        removeDereferencedEntries();
        if (value!=null) {
            value = new HardReference(value);
        }        
        return super.put(key, value);
    }
    
    public Collection values() {
        removeDereferencedEntries();
        Collection origColl = super.values();
        ArrayList newColl = new ArrayList(origColl.size());
        for (Iterator iter = origColl.iterator(); iter.hasNext();) {
            SoftReference element = (SoftReference) iter.next();
            if (element!=null) {
                Object strong = element.get();
                if (strong==null) continue;
                newColl.add(strong);
            } else {
                newColl.add(null);
            }            
        }        
        return newColl;
    }
    
    private void removeDereferencedEntries(){
        SoftReference e;
        while ( (e = (SoftReference) queue.poll()) != null) {
            Object strong = e.get();
            if (strong==null) continue;
            remove(strong);
        }
    }
}
