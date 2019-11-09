/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.metaclass;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A hash table that supports mostly-concurrent reading, but
 * exclusive writing.  Because reads are not limited to periods
 * without writes, a concurrent reader policy is weaker than a classic
 * reader/writer policy, but is generally faster and allows more
 * concurrency. This class is a good choice especially for tables that
 * are mainly created by one thread during the start-up phase of a
 * program, and from then on, are mainly read (with perhaps occasional
 * additions or removals) in many threads.  If you also need concurrency
 * among writes, consider instead using ConcurrentHashMap.
 * <p>
 *
 * Successful retrievals using get(key) and containsKey(key) usually
 * run without locking. Unsuccessful ones (i.e., when the key is not
 * present) do involve brief synchronization (locking).  Also, the
 * size and isEmpty methods are always synchronized.
 *
 * <p> Because retrieval operations can ordinarily overlap with
 * writing operations (i.e., put, remove, and their derivatives),
 * retrievals can only be guaranteed to return the results of the most
 * recently <em>completed</em> operations holding upon their
 * onset. Retrieval operations may or may not return results
 * reflecting in-progress writing operations.  However, the retrieval
 * operations do always return consistent results -- either those
 * holding before any single modification or after it, but never a
 * nonsense result.  For aggregate operations such as putAll and
 * clear, concurrent reads may reflect insertion or removal of only
 * some entries. In those rare contexts in which you use a hash table
 * to synchronize operations across threads (for example, to prevent
 * reads until after clears), you should either encase operations
 * in synchronized blocks, or instead use java.util.Hashtable.
 *
 * <p>
 *
 * This class also supports optional guaranteed
 * exclusive reads, simply by surrounding a call within a synchronized
 * block, as in <br> 
 * <code>ConcurrentReaderHashMap t; ... Object v; <br>
 * synchronized(t) { v = t.get(k); } </code> <br>
 *
 * But this is not usually necessary in practice. For
 * example, it is generally inefficient to write:
 *
 * <pre>
 *   ConcurrentReaderHashMap t; ...            // Inefficient version
 *   Object key; ...
 *   Object value; ...
 *   synchronized(t) { 
 *     if (!t.containsKey(key))
 *       t.put(key, value);
 *       // other code if not previously present
 *     }
 *     else {
 *       // other code if it was previously present
 *     }
 *   }
 *</pre>
 * Instead, if the values are intended to be the same in each case, just take advantage of the fact that put returns
 * null if the key was not previously present:
 * <pre>
 *   ConcurrentReaderHashMap t; ...                // Use this instead
 *   Object key; ...
 *   Object value; ...
 *   Object oldValue = t.put(key, value);
 *   if (oldValue == null) {
 *     // other code if not previously present
 *   }
 *   else {
 *     // other code if it was previously present
 *   }
 *</pre>
 * <p>
 *
 * Iterators and Enumerations (i.e., those returned by
 * keySet().iterator(), entrySet().iterator(), values().iterator(),
 * keys(), and elements()) return elements reflecting the state of the
 * hash table at some point at or since the creation of the
 * iterator/enumeration.  They will return at most one instance of
 * each element (via next()/nextElement()), but might or might not
 * reflect puts and removes that have been processed since they were
 * created.  They do <em>not</em> throw ConcurrentModificationException.
 * However, these iterators are designed to be used by only one
 * thread at a time. Sharing an iterator across multiple threads may
 * lead to unpredictable results if the table is being concurrently
 * modified.  Again, you can ensure interference-free iteration by
 * enclosing the iteration in a synchronized block.  <p>
 *
 * This class may be used as a direct replacement for any use of
 * java.util.Hashtable that does not depend on readers being blocked
 * during updates. Like Hashtable but unlike java.util.HashMap,
 * this class does NOT allow <tt>null</tt> to be used as a key or
 * value.  This class is also typically faster than ConcurrentHashMap
 * when there is usually only one thread updating the table, but 
 * possibly many retrieving values from it.
 * <p>
 *
 * Implementation note: A slightly faster implementation of
 * this class will be possible once planned Java Memory Model
 * revisions are in place.
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 *
 * @author Adapted from ConcurrentHashMap (Doug Lea)
 * @author adapted by the Groovy community
 */
public class ConcurrentReaderHashMap 
  extends AbstractMap 
  implements Cloneable, Serializable {
  private static final long serialVersionUID = -3225682440765612861L;


  /*
    The basic strategy is an optimistic-style scheme based on
    the guarantee that the hash table and its lists are always
    kept in a consistent enough state to be read without locking:

    * Read operations first proceed without locking, by traversing the
       apparently correct list of the apparently correct bin. If an
       entry is found, but not invalidated (value field null), it is
       returned. If not found, operations must recheck (after a memory
       barrier) to make sure they are using both the right list and
       the right table (which can change under re-sizes). If
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
  protected static class BarrierLock implements java.io.Serializable {
      private static final long serialVersionUID = -2159505361622844863L;
  }

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
   * holding main sync lock.
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
   * MUST be a power of two &lt;= 1&lt;&lt;30.
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

  /**
   * Returns the appropriate capacity (power of two) for the specified 
   * initial capacity argument.
   */
  private static int p2capacity(int initialCapacity) {
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
   * Check for equality of non-null references x and y. 
   **/
  protected boolean eq(Object x, Object y) {
    return x == y || x.equals(y);
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
   *               than zero, or if the load factor is non-positive.
   */
  public ConcurrentReaderHashMap(int initialCapacity, float loadFactor) {
    if (loadFactor <= 0)
      throw new IllegalArgumentException("Illegal Load factor: "+
                                         loadFactor);
    this.loadFactor = loadFactor;

    int cap = p2capacity(initialCapacity);

    table = new Entry[cap];
    threshold = (int)(cap * loadFactor);
  }

  /**
   * Constructs a new, empty map with the specified initial 
   * capacity and default load factor.
   *
   * @param   initialCapacity   the initial capacity of the 
   *                            ConcurrentReaderHashMap.
   * @throws    IllegalArgumentException if the initial maximum number 
   *              of elements is less than zero.
   */
  public ConcurrentReaderHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new, empty map with a default initial capacity
   * and load factor.
   */
  public ConcurrentReaderHashMap() {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new map with the same mappings as the given map.  The
   * map is created with a capacity of twice the number of mappings in
   * the given map or 16 (whichever is greater), and a default load factor.
   */
  public ConcurrentReaderHashMap(Map t) {
        this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, 16),
             DEFAULT_LOAD_FACTOR);
    putAll(t);
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
   * @exception  NullPointerException  if the key is <code>null</code>.
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

      }

      else if (e.hash == hash && eq(key, e.key)) {
        Object value = e.value;
        if (value != null) 
          return value;

        // Entry was invalidated during deletion. But it could
        // have been re-inserted, so we must re-traverse.
        // To avoid useless contention, get lock to wait out modifications
        // before re-traversing.

        synchronized(this) {
          tab = table;
        }
        e = first = tab[index = hash & (tab.length-1)];

      }
      else
        e = e.next;
    }
  }


  /**
   * Tests if the specified object is a key in this table.
   * 
   * @param   key   possible key.
   * @return  <code>true</code> if and only if the specified object 
   *          is a key in this table, as determined by the 
   *          <tt>equals</tt> method; <code>false</code> otherwise.
   * @exception  NullPointerException  if the key is <code>null</code>.
   * @see     #contains(Object)
   */
  public boolean containsKey(Object key) {
    return get(key) != null;
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
   * @exception  NullPointerException  if the key or value is <code>null</code>.
   * @see     Object#equals(Object)
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
      if (e.hash == hash && eq(key, e.key))
        break;

    synchronized(this) {
      if (tab == table) {
        if (e == null) {
          //  make sure we are adding to correct list
          if (first == tab[index]) {
            //  Add to front of list
            Entry newEntry = new Entry(hash, key, value, first);
            tab[index] = newEntry;
            if (++count >= threshold) rehash();
            else recordModification(newEntry);
            return null;
          }
        }
        else {
          Object oldValue = e.value; 
          if (first == tab[index] && oldValue != null) {
            e.value = value;
            return oldValue;
          }
        }
      }
      
      // retry if wrong list or lost race against concurrent remove
      return sput(key, value, hash);
    }
  }


  /**
   * Continuation of put(), called only when sync lock is
   * held and interference has been detected.
   **/
  protected Object sput(Object key, Object value, int hash) { 

    Entry[] tab = table;
    int index = hash & (tab.length-1);
    Entry first = tab[index];
    Entry e = first;

    for (;;) {
      if (e == null) {
        Entry newEntry = new Entry(hash, key, value, first);
        tab[index] = newEntry;
        if (++count >= threshold) rehash();
        else recordModification(newEntry);
        return null;
      }
      else if (e.hash == hash && eq(key, e.key)) {
        Object oldValue = e.value; 
        e.value = value;
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
      threshold = Integer.MAX_VALUE; // avoid re-triggering
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
     * the default threshold, only about one-sixth of them need
     * cloning. (The nodes they replace will be garbage
     * collectible as soon as they are no longer referenced by any
     * reader thread that may be in the midst of traversing table
     * right now.)
     */

      for (Entry e : oldTable) {
          // We need to guarantee that any existing reads of old Map can
          // proceed. So we cannot yet null out each bin.
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
                      newTable[k] = new Entry(p.hash, p.key,
                              p.value, newTable[k]);
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
           all preceding ones need to be cloned.  Traversals rely
           on this strategy to ensure that elements will not be
          repeated during iteration.
    */
          

    int hash = hash(key);
    Entry[] tab = table;
    int index = hash & (tab.length-1);
    Entry first = tab[index];
    Entry e = first;
      
    for (e = first; e != null; e = e.next) 
      if (e.hash == hash && eq(key, e.key)) 
        break;


    synchronized(this) {
      if (tab == table) {
        if (e == null) {
          if (first == tab[index])
            return null;
        }
        else {
          Object oldValue = e.value;
          if (first == tab[index] && oldValue != null) {
            e.value = null;
            count--;
            
            Entry head = e.next;
            for (Entry p = first; p != e; p = p.next) 
              head = new Entry(p.hash, p.key, p.value, head);
            
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
   * Continuation of remove(), called only when sync lock is
   * held and interference has been detected.
   **/
  protected Object sremove(Object key, int hash) {
    Entry[] tab = table;
    int index = hash & (tab.length-1);
    Entry first = tab[index];
      
    for (Entry e = first; e != null; e = e.next) {
      if (e.hash == hash && eq(key, e.key)) {
        Object oldValue = e.value;
        e.value = null;
        count--;
        Entry head = e.next;
        for (Entry p = first; p != e; p = p.next) 
          head = new Entry(p.hash, p.key, p.value, head);
        
        tab[index] = head;
        recordModification(head);
        return oldValue;
      }
    }
    return null;
  }


  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the
   * specified value. Note: This method requires a full internal
   * traversal of the hash table, and so is much slower than
   * method <tt>containsKey</tt>.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the
   * specified value.  
   * @exception  NullPointerException  if the value is <code>null</code>.
   */
  public boolean containsValue(Object value) {
    if (value == null) throw new NullPointerException();

    Entry tab[] = getTableForReading();

      for (Entry entry : tab) {
          for (Entry e = entry; e != null; e = e.next)
              if (value.equals(e.value))
                  return true;
      }

    return false;
  }

  /**
   * Tests if some key maps into the specified value in this table.
   * This operation is more expensive than the <code>containsKey</code>
   * method.<p>
   *
   * Note that this method is identical in functionality to containsValue,
   * (which is part of the Map interface in the collections framework).
   * 
   * @param      value   a value to search for.
   * @return     <code>true</code> if and only if some key maps to the
   *             <code>value</code> argument in this table as 
   *             determined by the <tt>equals</tt> method;
   *             <code>false</code> otherwise.
   * @exception  NullPointerException  if the value is <code>null</code>.
   * @see        #containsKey(Object)
   * @see        #containsValue(Object)
   * @see      Map
   */
  public boolean contains(Object value) {
    return containsValue(value);
  }


  /**
   * Copies all of the mappings from the specified map to this one.
   * 
   * These mappings replace any mappings that this map had for any of the
   * keys currently in the specified Map.
   *
   * @param t Mappings to be stored in this map.
   */
  public synchronized void putAll(Map t) {
    int n = t.size();
    if (n == 0)
      return;

    // Expand enough to hold at least n elements without resizing.
    // We can only resize table by factor of two at a time.
    // It is faster to rehash with fewer elements, so do it now.
    while (n >= threshold)
      rehash();

      for (Object o : t.entrySet()) {
          Map.Entry entry = (Map.Entry) o;
          Object key = entry.getKey();
          Object value = entry.getValue();
          put(key, value);
      }
  }


  /**
   * Removes all mappings from this map.
   */
  public synchronized void clear() {
    Entry tab[] = table;
    for (int i = 0; i < tab.length ; ++i) { 

      // must invalidate all to force concurrent get's to wait and then retry
      for (Entry e = tab[i]; e != null; e = e.next) 
        e.value = null; 

      tab[i] = null;
    }
    count = 0;
    recordModification(tab);
  }

  /**
   * Returns a shallow copy of this 
   * <tt>ConcurrentReaderHashMap</tt> instance: the keys and
   * values themselves are not cloned.
   *
   * @return a shallow copy of this map.
   */
  public synchronized Object clone() {
    try { 
      ConcurrentReaderHashMap t = (ConcurrentReaderHashMap)super.clone();

      t.keySet = null;
      t.entrySet = null;
      t.values = null;

      Entry[] tab = table;
      t.table = new Entry[tab.length];
      Entry[] ttab = t.table;

      for (int i = 0; i < tab.length; ++i) {
        Entry first = null;
        for (Entry e = tab[i]; e != null; e = e.next) 
          first = new Entry(e.hash, e.key, e.value, first);
        ttab[i] = first;
      }

      return t;
    } 
    catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  // Views

  protected transient Set keySet = null;
  protected transient Set entrySet = null;
  protected transient Collection values = null;

  /**
   * Returns a set view of the keys contained in this map.  The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa.  The set supports element removal, which removes the
   * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
   * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
   * <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this map.
   */
  public Set keySet() {
    Set ks = keySet;
    return (ks != null)? ks : (keySet = new KeySet());
  }
  
  private class KeySet extends AbstractSet {
    public Iterator iterator() {
      return new KeyIterator();
    }
    public int size() {
      return ConcurrentReaderHashMap.this.size();
    }
    public boolean contains(Object o) {
      return ConcurrentReaderHashMap.this.containsKey(o);
    }
    public boolean remove(Object o) {
      return ConcurrentReaderHashMap.this.remove(o) != null;
    }
    public void clear() {
      ConcurrentReaderHashMap.this.clear();
    }
    public Object[] toArray() {
      Collection c = new ArrayList();
        c.addAll(this);
      return c.toArray();
    }
    public Object[] toArray(Object[] a) {
      Collection c = new ArrayList();
      c.addAll(this);
      return c.toArray(a);
    }
  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
  public Collection values() {
    Collection vs = values;
    return (vs != null)? vs : (values = new Values());
  }
  
  private class Values extends AbstractCollection {
    public Iterator iterator() {
      return new ValueIterator();
    }
    public int size() {
      return ConcurrentReaderHashMap.this.size();
    }
    public boolean contains(Object o) {
      return ConcurrentReaderHashMap.this.containsValue(o);
    }
    public void clear() {
      ConcurrentReaderHashMap.this.clear();
    }
    public Object[] toArray() {
      Collection c = new ArrayList();
      c.addAll(this);
      return c.toArray();
    }
    public Object[] toArray(Object[] a) {
      Collection c = new ArrayList();
      c.addAll(this);
      return c.toArray(a);
    }
  }

  /**
   * Returns a collection view of the mappings contained in this map.  Each
   * element in the returned collection is a <tt>Map.Entry</tt>.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   */
  public Set entrySet() {
    Set es = entrySet;
    return (es != null) ? es : (entrySet = new EntrySet());
  }

  private class EntrySet extends AbstractSet {
    public Iterator iterator() {
      return new HashIterator();
    }
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry entry = (Map.Entry)o;
      Object v = ConcurrentReaderHashMap.this.get(entry.getKey());
      return v != null && v.equals(entry.getValue());
    }
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      return ConcurrentReaderHashMap.this.findAndRemoveEntry((Map.Entry)o);
    }
    public int size() {
      return ConcurrentReaderHashMap.this.size();
    }
    public void clear() {
      ConcurrentReaderHashMap.this.clear();
    }
    public Object[] toArray() {
      Collection c = new ArrayList();
      c.addAll(this);
      return c.toArray();
    }
    public Object[] toArray(Object[] a) {
      Collection c = new ArrayList();
      c.addAll(this);
      return c.toArray(a);
    }
  }

  /**
   * Helper method for entrySet.remove
   **/
  protected synchronized boolean findAndRemoveEntry(Map.Entry entry) {
    Object key = entry.getKey();
    Object v = get(key);
    if (v != null && v.equals(entry.getValue())) {
      remove(key);
      return true;
    }
    else
      return false;
  }

  /**
   * Returns an enumeration of the keys in this table.
   *
   * @return  an enumeration of the keys in this table.
   * @see     Enumeration
   * @see     #elements()
   * @see   #keySet()
   * @see   Map
   */
  public Enumeration keys() {
    return new KeyIterator();
  }

  /**
   * Returns an enumeration of the values in this table.
   * Use the Enumeration methods on the returned object to fetch the elements
   * sequentially.
   *
   * @return  an enumeration of the values in this table.
   * @see     java.util.Enumeration
   * @see     #keys()
   * @see   #values()
   * @see   Map
   */
  public Enumeration elements() {
    return new ValueIterator();
  }


  /**
   * ConcurrentReaderHashMap collision list entry.
   */
  protected static class Entry implements Map.Entry {

    /* 
       The use of volatile for value field ensures that
       we can detect status changes without synchronization.
       The other fields are never changed, and are
       marked as final. 
    */
    protected final int hash;
    protected final Object key;
    protected final Entry next;
    protected volatile Object value;

    Entry(int hash, Object key, Object value, Entry next) {
      this.hash = hash;
      this.key = key;
      this.next = next;
      this.value = value;
    }

    // Map.Entry Ops 

    public Object getKey() {
      return key;
    }

    /**
     * Get the value.  Note: In an entrySet or entrySet.iterator,
     * unless the set or iterator is used under synchronization of the
     * table as a whole (or you can otherwise guarantee lack of
     * concurrent modification), <tt>getValue</tt> <em>might</em>
     * return null, reflecting the fact that the entry has been
     * concurrently removed. However, there are no assurances that
     * concurrent removals will be reflected using this method.
     * 
     * @return     the current value, or null if the entry has been 
     * detectably removed.
     **/
    public Object getValue() {
      return value; 
    }

    /**
     * Set the value of this entry.  Note: In an entrySet or
     * entrySet.iterator), unless the set or iterator is used under
     * synchronization of the table as a whole (or you can otherwise
     * guarantee lack of concurrent modification), <tt>setValue</tt>
     * is not strictly guaranteed to actually replace the value field
     * obtained via the <tt>get</tt> operation of the underlying hash
     * table in multi-threaded applications.  If iterator-wide
     * synchronization is not used, and any other concurrent
     * <tt>put</tt> or <tt>remove</tt> operations occur, sometimes
     * even to <em>other</em> entries, then this change is not
     * guaranteed to be reflected in the hash table. (It might, or it
     * might not. There are no assurances either way.)
     *
     * @param      value   the new value.
     * @return     the previous value, or null if entry has been detectably
     * removed.
     * @exception  NullPointerException  if the value is <code>null</code>.
     * 
     **/
    public Object setValue(Object value) {
      if (value == null)
        throw new NullPointerException();
      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry)o;
      return (key.equals(e.getKey()) && value.equals(e.getValue()));
    }
    
    public int hashCode() {
      return  key.hashCode() ^ value.hashCode();
    }
    
    public String toString() {
      return key + "=" + value;
    }

  }

  protected class HashIterator implements Iterator, Enumeration {
    protected final Entry[] tab;           // snapshot of table
    protected int index;                   // current slot 
    protected Entry entry = null;          // current node of slot
    protected Object currentKey;           // key for current node
    protected Object currentValue;         // value for current node
    protected Entry lastReturned = null;   // last node returned by next

    protected HashIterator() {
      tab = ConcurrentReaderHashMap.this.getTableForReading();
      index = tab.length - 1;
    }

    public boolean hasMoreElements() { return hasNext(); }
    public Object nextElement() { return next(); }


    public boolean hasNext() {

      /*
        currentKey and currentValue are set here to ensure that next()
        returns normally if hasNext() returns true. This avoids
        surprises especially when final element is removed during
        traversal -- instead, we just ignore the removal during
        current traversal.  
      */

      for (;;) {
        if (entry != null) {
          Object v = entry.value;
          if (v != null) {
            currentKey = entry.key;
            currentValue = v;
            return true;
          }
          else
            entry = entry.next;
        }

        while (entry == null && index >= 0)
          entry = tab[index--];

        if (entry == null) {
          currentKey = currentValue = null;
          return false;
        }
      }
    }

    protected Object returnValueOfNext() { return entry; }

    public Object next() {
      if (currentKey == null && !hasNext())
        throw new NoSuchElementException();

      Object result = returnValueOfNext();
      lastReturned = entry;
      currentKey = currentValue = null;
      entry = entry.next;
      return result;
    }

    public void remove() {
      if (lastReturned == null)
        throw new IllegalStateException();
      ConcurrentReaderHashMap.this.remove(lastReturned.key);
      lastReturned = null;
    }

  }


  protected class KeyIterator extends HashIterator {
    protected Object returnValueOfNext() { return currentKey; }
  }
  
  protected class ValueIterator extends HashIterator {
    protected Object returnValueOfNext() { return currentValue; }
  }
  


  /**
   * Save the state of the <tt>ConcurrentReaderHashMap</tt> 
   * instance to a stream (i.e.,
   * serialize it).
   *
   * @param s the stream
   * @serialData The <i>capacity</i> of the
   * ConcurrentReaderHashMap (the length of the
   * bucket array) is emitted (int), followed  by the
   * <i>size</i> of the ConcurrentReaderHashMap (the number of key-value
   * mappings), followed by the key (Object) and value (Object)
   * for each key-value mapping represented by the ConcurrentReaderHashMap
   * The key-value mappings are emitted in no particular order.
   */
  private synchronized void writeObject(java.io.ObjectOutputStream s)
    throws IOException  {
    // Write out the threshold, loadfactor, and any hidden stuff
    s.defaultWriteObject();
    
    // Write out number of buckets
    s.writeInt(table.length);
    
    // Write out size (number of Mappings)
    s.writeInt(count);
    
    // Write out keys and values (alternating)
    for (int index = table.length-1; index >= 0; index--) {
      Entry entry = table[index];
      
      while (entry != null) {
        s.writeObject(entry.key);
        s.writeObject(entry.value);
        entry = entry.next;
      }
    }
  }

  /**
   * Reconstitute the <tt>ConcurrentReaderHashMap</tt> 
   * instance from a stream (i.e.,
   * deserialize it).
   *
   * @param s the stream
   */
  private synchronized void readObject(java.io.ObjectInputStream s)
    throws IOException, ClassNotFoundException  {
    // Read in the threshold, loadfactor, and any hidden stuff
    s.defaultReadObject();

    // Read in number of buckets and allocate the bucket array;
    int numBuckets = s.readInt();
    table = new Entry[numBuckets];
    
    // Read in size (number of Mappings)
    int size = s.readInt();
    
    // Read the keys and values, and put the mappings in the table
    for (int i=0; i<size; i++) {
      Object key = s.readObject();
      Object value = s.readObject();
      put(key, value);
    }
  }
  
  /** 
   * @return the number of slots in this table
   **/
  public synchronized int capacity() {
    return table.length;
  }

  /** 
   * @return the load factor
   **/
  public float loadFactor() {
    return loadFactor;
  }
}
