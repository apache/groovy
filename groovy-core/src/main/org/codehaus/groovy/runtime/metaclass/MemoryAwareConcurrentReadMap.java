/*
  File: ConcurrentReaderHashMap

  Written by Doug Lea. Adapted and released, under explicit
  permission, from JDK1.2 HashMap.java and Hashtable.java which
  carries the following copyright:

 * Copyright 1997 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.

  History:
  Date       Who                What
  28oct1999  dl               Created
  14dec1999  dl               jmm snapshot
  19apr2000  dl               use barrierLock
  12jan2001  dl               public release
  17nov2001  dl               Minor tunings
  20may2002  dl               BarrierLock can now be serialized.
  09dec2002  dl               Fix interference checks.
  23jun2004  dl               Avoid bad array sizings in view toArray methods
  02jul2007  blackdrag        adaption of package name to Groovy project
 */

package org.codehaus.groovy.runtime.metaclass;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;


/**
 * This Map is astripped down version of ConcurrentReaderHashMap with
 * small modifications here and there.
 * It is no full Map, it does have put/get/remove, but no iterators.
 * This map is intended to hold values and keys as SoftReference. If
 * one of value or key are removed, so will be complete entry. This map
 * will not use the equals method to compare keys, think of it as a
 * IdentityHashMap with features of concurrency and memory aware caching.
 * As  ConcurrentReaderHashMap also does this implementation prefere read
 * operations and tries not to lock if possible. SoftReferenced values
 * are only removed from the map if the map goes into a synchronization
 * block on this. This may affect reads, but only in rare cases.
 **/
public class MemoryAwareConcurrentReadMap {


    /*
    The basic strategy is an optimistic-style scheme based on
    the guarantee that the hash table and its lists are always
    kept in a consistent enough state to be read without locking:

     * Read operations first proceed without locking, by traversing the
       apparently correct list of the apparently correct bin. If an
       entry is found, but not invalidated (value field null), it is
       returned. If not found, operations must recheck (after a memory
       barrier) to make sure they are using both the right list and
       the right table (which can change under resizes). If
       invalidated, reads must acquire main update lock to wait out
       the update, and then re-traverse.

     * All list additions are at the front of each bin, making it easy
       to check changes, and also fast to traverse.  Entry next
       pointers are never assigned. Remove() builds new nodes when
       necessary to preserve this.

     * Remove() (also clear()) invalidates removed nodes to alert read
       operations that they must wait out the full modifications.

     */

    /** A Serializable class for barrier lock **/
    protected static class BarrierLock implements java.io.Serializable { }

    /**
     * Lock used only for its memory effects.
     **/
    protected final BarrierLock barrierLock = new BarrierLock();

    /**
     * field written to only to guarantee lock ordering.
     **/
    protected transient Object lastWrite;

    /**
     * Force a memory synchronization that will cause
     * all readers to see table. Call only when already
     * holding main synch lock.
     **/
    protected final void recordModification(Object x) { 
        synchronized(barrierLock) {
            lastWrite = x;
        }
    }

    /**
     * Get ref to table; the reference and the cells it
     * accesses will be at least as fresh as from last
     * use of barrierLock
     **/
    protected final Entry[] getTableForReading() { 
        synchronized(barrierLock) {
            return table; 
        }
    }


    /**
     * The default initial number of table slots for this table (32).
     * Used when not otherwise specified in constructor.
     **/
    public static final int DEFAULT_INITIAL_CAPACITY = 32; 


    /**
     * The minimum capacity, used if a lower value is implicitly specified
     * by either of the constructors with arguments.  
     * MUST be a power of two.
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The default load factor for this table (1.0).
     * Used when not otherwise specified in constructor.
     **/

    public static final float DEFAULT_LOAD_FACTOR = 0.75f; 


    /**
     * The hash table data.
     */
    protected transient Entry[] table;

    /**
     * The total number of mappings in the hash table.
     */
    protected transient int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is always (int)(capacity * loadFactor).)
     *
     * @serial
     */
    protected int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    protected float loadFactor;

    
    private ReferenceQueue queue;
    
    /**
     * Returns the appropriate capacity (power of two) for the specified 
     * initial capacity argument.
     */
    private int p2capacity(int initialCapacity) {
        int cap = initialCapacity;

        // Compute the appropriate capacity
        int result;
        if (cap > MAXIMUM_CAPACITY || cap < 0) {
            result = MAXIMUM_CAPACITY;
        } else {
            result = MINIMUM_CAPACITY;
            while (result < cap)
                result <<= 1;
        }
        return result;
    }

    /**
     * Return hash code for Object x. Since we are using power-of-two
     * tables, it is worth the effort to improve hashcode via
     * the same multiplicative scheme as used in IdentityHashMap.
     */
    private static int hash(Object x) {
        int h = x.hashCode();
        // Multiply by 127 (quickly, via shifts), and mix in some high
        // bits to help guard against bunching of codes that are
        // consecutive or equally spaced.
        return ((h << 7) - h + (h >>> 9) + (h >>> 17));
    }

    /** 
     * Check for referential equality, null allowed 
     **/
    protected boolean eq(Object x, Object y) {
        return x == y;
    }

    /**
     * Constructs a new, empty map with the specified initial 
     * capacity and the specified load factor. 
     *
     * @param initialCapacity the initial capacity
     *  The actual initial capacity is rounded to the nearest power of two.
     * @param loadFactor  the load factor of the ConcurrentReaderHashMap
     * @throws IllegalArgumentException  if the initial maximum number 
     *               of elements is less
     *               than zero, or if the load factor is nonpositive.
     */
    public MemoryAwareConcurrentReadMap(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Illegal Load factor: "+
                    loadFactor);
        this.loadFactor = loadFactor;

        int cap = p2capacity(initialCapacity);

        table = new Entry[cap];
        threshold = (int)(cap * loadFactor);
        
        queue = new ReferenceQueue();
    }

    /**
     * Constructs a new, empty map with the specified initial 
     * capacity and default load factor.
     *
     * @param   initialCapacity   the initial capacity of the 
     *                            ConcurrentReaderHashMap.
     * @throws    IllegalArgumentException if the initial maximum number 
     *              of elements is less
     *              than zero.
     */
    public MemoryAwareConcurrentReadMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty map with a default initial capacity
     * and load factor.
     */
    public MemoryAwareConcurrentReadMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public synchronized int size() {
        return count;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public synchronized boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns the value to which the specified key is mapped in this table.
     *
     * @param   key   a key in the table.
     * @return  the value to which the key is mapped in this table;
     *          <code>null</code> if the key is not mapped to any value in
     *          this table.
     * @exception  NullPointerException  if the key is
     *               <code>null</code>.
     * @see     #put(Object, Object)
     */
    public Object get(Object key) {

        // throw null pointer exception if key null
        int hash = hash(key);

        /* 
       Start off at the apparently correct bin.  If entry is found, we
       need to check after a barrier anyway.  If not found, we need a
       barrier to check if we are actually in right bin. So either
       way, we encounter only one barrier unless we need to retry.
       And we only need to fully synchronize if there have been
       concurrent modifications.
         */

        Entry[] tab = table;
        int index = hash & (tab.length - 1);
        Entry first = tab[index];
        Entry e = first;

        for (;;) {
            if (e == null) {

                // If key apparently not there, check to
                // make sure this was a valid read

                Entry[] reread = getTableForReading();
                if (tab == reread && first == tab[index])
                    return null;
                else {
                    // Wrong list -- must restart traversal at new first
                    tab = reread;
                    e = first = tab[index = hash & (tab.length-1)];
                }
                continue;
            }
            
            Object eKey = e.getKey();
            Object eValue = e.getValue();

            if (e.hash == hash && eq(key, eKey)) {
                if (e.value != DUMMY_REF) return eValue;

                // Entry was invalidated during deletion. But it could
                // have been re-inserted, so we must retraverse.
                // To avoid useless contention, get lock to wait out modifications
                // before retraversing.
                
                
                synchronized(this) {
                    if (eKey==null && eValue==null) expungeStaleEntries();
                    tab = table;
                }
                e = first = tab[index = hash & (tab.length-1)];

            } 
            else 
                e = e.next;
        }
    }
    
    /**
     * Maps the specified <code>key</code> to the specified 
     * <code>value</code> in this table. Neither the key nor the 
     * value can be <code>null</code>. <p>
     *
     * The value can be retrieved by calling the <code>get</code> method 
     * with a key that is equal to the original key. 
     *
     * @param      key     the table key.
     * @param      value   the value.
     * @return     the previous value of the specified key in this table,
     *             or <code>null</code> if it did not have one.
     * @exception  NullPointerException  if the key or value is
     *               <code>null</code>.
     * @see     #get(Object)
     */
    public Object put(Object key, Object value) {
        if (value == null) 
            throw new NullPointerException();

        int hash = hash(key);
        Entry[] tab = table;
        int index = hash & (tab.length-1);
        Entry first = tab[index];
        Entry e;

        for (e = first; e != null; e = e.next)
            if (e.hash == hash && eq(key, e.getKey()))
                break;

        synchronized(this) {
            if (tab == table) {
                if (e == null) {
                    //  make sure we are adding to correct list
                    if (first == tab[index]) {
                        //  Add to front of list
                        Entry newEntry = new Entry(hash, key, value, first, queue);
                        tab[index] = newEntry;
                        if (++count >= threshold) rehash();
                        else recordModification(newEntry);
                        return null;
                    }
                }
                else {
                    Object oldValue = e.getValue();
                    if (first == tab[index] && oldValue != null) {
                        e.setValue(e.value);
                        return oldValue;
                    }
                }
            }

            // retry if wrong list or lost race against concurrent remove
            return sput(key, value, hash);
        }
    }


    /**
     * Continuation of put(), called only when synch lock is
     * held and interference has been detected.
     **/
    protected Object sput(Object key, Object value, int hash) { 
        expungeStaleEntries();

        Entry[] tab = table;
        int index = hash & (tab.length-1);
        Entry first = tab[index];
        Entry e = first;

        for (;;) {
            if (e == null) {
                Entry newEntry = new Entry(hash, key, value, first, queue);
                tab[index] = newEntry;
                if (++count >= threshold) rehash();
                else recordModification(newEntry);
                return null;
            }
            else if (e.hash == hash && eq(key, e.getKey())) {
                Object oldValue = e.getValue(); 
                e.setValue(e.value);
                return oldValue;
            }
            else
                e = e.next;
        }
    }


    /**
     * Rehashes the contents of this map into a new table
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     */
    protected void rehash() { 
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE; // avoid retriggering
            return;
        }

        int newCapacity = oldCapacity << 1;
        int mask = newCapacity - 1;
        threshold = (int)(newCapacity * loadFactor);

        Entry[] newTable = new Entry[newCapacity];
        /*
         * Reclassify nodes in each list to new Map.  Because we are
         * using power-of-two expansion, the elements from each bin
         * must either stay at same index, or move to
         * oldCapacity+index. We also eliminate unnecessary node
         * creation by catching cases where old nodes can be reused
         * because their next fields won't change. Statistically, at
         * the default threshhold, only about one-sixth of them need
         * cloning. (The nodes they replace will be garbage
         * collectable as soon as they are no longer referenced by any
         * reader thread that may be in the midst of traversing table
         * right now.)
         */

        for (int i = 0; i < oldCapacity ; i++) {
            // We need to guarantee that any existing reads of old Map can
            //  proceed. So we cannot yet null out each bin.  
            Entry e = oldTable[i];

            if (e != null) {
                int idx = e.hash & mask;
                Entry next = e.next;

                //  Single node on list
                if (next == null) 
                    newTable[idx] = e;

                else {    
                    // Reuse trailing consecutive sequence of all same bit
                    Entry lastRun = e;
                    int lastIdx = idx;
                    for (Entry last = next; last != null; last = last.next) {
                        int k = last.hash & mask;
                        if (k != lastIdx) {
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIdx] = lastRun;

                    // Clone all remaining nodes
                    for (Entry p = e; p != lastRun; p = p.next) {
                        int k = p.hash & mask;
                        newTable[k] = new Entry(p.hash, p.getKey(), 
                                p.getValue(), newTable[k], queue);
                    }
                }
            }
        }

        table = newTable;
        recordModification(newTable);
    }

    /**
     * Removes the key (and its corresponding value) from this 
     * table. This method does nothing if the key is not in the table.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this table,
     *          or <code>null</code> if the key did not have a mapping.
     * @exception  NullPointerException  if the key is
     *               <code>null</code>.
     */
    public Object remove(Object key) {
        /*
      Find the entry, then 
        1. Set value field to null, to force get() to retry
        2. Rebuild the list without this entry.
           All entries following removed node can stay in list, but
           all preceeding ones need to be cloned.  Traversals rely
           on this strategy to ensure that elements will not be
          repeated during iteration.
         */


        int hash = hash(key);
        Entry[] tab = table;
        int index = hash & (tab.length-1);
        Entry first = tab[index];
        Entry e = first;

        for (e = first; e != null; e = e.next) 
            if (e.hash == hash && eq(key, e.getKey())) 
                break;


        synchronized(this) {
            if (tab == table) {
                if (e == null) {
                    if (first == tab[index])
                        return null;
                }
                else {
                    Object oldValue = e.getValue();
                    if (first == tab[index] && oldValue != null) {
                        e.setValue(null);
                        count--;

                        Entry head = e.next;
                        for (Entry p = first; p != e; p = p.next) 
                            head = new Entry(p.hash, p.key, p.value, head, queue);

                        tab[index] = head;
                        recordModification(head);
                        return oldValue;
                    }
                }
            }

            // Wrong list or interference
            return sremove(key, hash);
        }
    }

    /**
     * Continuation of remove(), called only when synch lock is
     * held and interference has been detected.
     **/
    protected Object sremove(Object key, int hash) {
        expungeStaleEntries();
        
        Entry[] tab = table;
        int index = hash & (tab.length-1);
        Entry first = tab[index];

        for (Entry e = first; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.getKey())) {
                Object oldValue = e.getValue();
                e.setValue(null);
                count--;
                Entry head = e.next;
                for (Entry p = first; p != e; p = p.next) 
                    head = new Entry(p.hash, p.getKey(), p.getValue(), head, queue);

                tab[index] = head;
                recordModification(head);
                return oldValue;
            }
        }
        return null;
    }

    /**
     * Removes all mappings from this map.
     */
    public synchronized void clear() {
        Entry tab[] = table;
        for (int i = 0; i < tab.length ; ++i) { 

            // must invalidate all to force concurrent get's to wait and then retry
            for (Entry e = tab[i]; e != null; e = e.next) 
                e.setValue(null); 

            tab[i] = null;
        }
        count = 0;
        recordModification(tab);
    }

    /**
     * Removes entries from the ReferenceQueue for keys and values
     * of this map. This method is thought to be called only with
     * an already existing lock on "this". 
     * 
     * The method expects SoftRef instances in the queue. It uses
     * the entry field to control if the Entry is already removed
     * map. If the entry is null the removal is skipped. 
     */
    private void expungeStaleEntries() {
        SoftRef ref;
        Entry[] tab = table;
        
        while ((ref=(SoftRef)queue.poll())!=null) {
            Entry entry = ref.entry;
            // if entry== null, then it is already deleted
            // form the map
            if (entry == null) continue;
            ref.entry = null;
            // if neither entry.key nor entry.value == ref then
            // the entry was reused, but the value has become invalid
            if (entry.key!=ref && entry.value!=ref) continue;
            int hash = entry.hash;
            int index = hash & (tab.length-1);
            Entry first = tab[index];

            for (Entry e = first; e != null; e = e.next) {
                if (e==entry) {
                    entry.key.clear();
                    entry.setValue(null);
                    
                    count--;
                    
                    Entry head = e.next;
                    for (Entry p = first; p != e; p = p.next) 
                        head = new Entry(p.hash, p.key, p.value, head);
                    
                    tab[index] = head;
                    recordModification(head);
                    break;
                }
            } 
        }
    }
    
    /**
     * Reference class used to support get()
     */
    private static interface Reference {
        Object get();
    }
    
    /**
     * A dummy to replace the SoftReference if needed
     */
    private static class DummyRef implements Reference {
        public Object get() {
            return null;
        }
    }    
    
    // constant for DummyRef, no need to keep more than one
    // it is not critical if more than one is created here
    private static final Reference DUMMY_REF = new DummyRef();
    
    /**
     * A SoftReference representing a key or value of the map. The
     * instance keeps a pointer to the entry it is sotring a 
     * key or value for. This is used to identify the entry we 
     * need to remove 
     * @see CopyOfMemoryAwareConcurrentReadMap#expungeStaleEntries() 
     */
    private static class SoftRef extends SoftReference implements Reference {
        private volatile Entry entry;
        public SoftRef(Entry e, Object v, ReferenceQueue q) {
            super(v,q);
            entry = e;
        }
        public void clear() {
            super.clear();
            entry=null;
        }
    }

    /**
     * ConcurrentReaderHashMap collision list entry.
     */
    private static class Entry {

        /* 
       The use of volatile for value field ensures that
       we can detect status changes without synchronization.
       The other fields are never changed, and are
       marked as final. 
         */
        private final int hash;
        private final SoftRef key;
        private final Entry next;
        private volatile Reference value;

        Entry(int hash, Object key, Object value, Entry next, ReferenceQueue queue) {
            this.hash = hash;
            this.key = new SoftRef(this,key,queue);
            this.next = next;
            this.value = new SoftRef(this,value,queue);
        }

        Entry(int hash, SoftRef key, Reference value, Entry next) {
            this.hash = hash;
            this.key = key;
            key.entry = this;
            this.next = next;
            this.value = DUMMY_REF;
            this.setValue(value);
        }
        
        // Map.Entry Ops 

        public Object getKey() {
            return key.get();
        }

        public Object getValue() {
            return value.get(); 
        }

        public Object setValue(Reference value) {
            Object oldValue = this.value.get();
            if (value == null || value == DUMMY_REF) {
                this.value = DUMMY_REF;
            } else {
                SoftRef ref = (SoftRef) value;
                ref.entry = this;
                this.value = value;
            }
            return oldValue;
        }
    }
       
}