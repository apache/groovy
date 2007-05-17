package org.codehaus.groovy.runtime.metaclass;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class MemoryAwareConcurrentReadMap {
 
    private interface Ref {
        public Object get();
        public void clear();
        public Entry getEntry();
    }
    
    private static class HardRef implements Ref {
        private Object value;
        private Entry entry;
        public HardRef(Object value,Entry e) {
            this.value = value;
            this.entry = e;
        }
        public Object get() {
            return value;
        }

        public void clear() {
            value = null;
            entry = null;
        }
        
        public Entry getEntry() {
            return entry;
        }        
    }
    
    private static class SoftRef extends SoftReference implements Ref {
        private Entry entry;
        public SoftRef(Object v, Entry e, ReferenceQueue queue) {
            super(v,queue);
            entry = e;
        }
        
        public void clear() {
            super.clear();
            entry = null;
        }

        public Entry getEntry() {
            return entry;
        }
    }
    
    private class Entry {
        private final int hash;
        private Ref value;
        private Ref key;
        private Entry next;
        
        public Entry(Object key, Object value, int hash, boolean hard) {
            this.hash = hash;
            setValue(key,value,hard);
        }
        public boolean isValid() {
            Object v = value!=null?value.get():null;
            Object k = key!=null?key.get():null;
            return v!=null && k!=null;
        }
        
        public Entry invalidate() {
            if (value!=null) {value.clear(); value = null;}
            if (key!=null) {key.clear(); key = null;}
            Entry tmp = next;
            next = null;
            return tmp;
        }
        
        public Object getKey() {
            return key!=null?key.get():null;
        }
        
        public Object getValue() {
            return value!=null?value.get():null;
        }
        
        public void setValue(Object key, Object value, boolean hard) {
            if (hard) {
                this.value = new HardRef(value,this);
                this.key = new HardRef(key,this);
            } else {
                this.value = new SoftRef(value,this,queue);
                this.key = new SoftRef(key,this,queue);
            }       
        }
    }
    
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private volatile long concurrentReads = 0;
    private Object writeLock = new Object();
    
    private Entry[] table;
    private int tableSize;
    private int size;
    private ReferenceQueue queue;
    private int threshold;
    
    public MemoryAwareConcurrentReadMap() {
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
        queue = new ReferenceQueue();
        tableSize = table.length;
        threshold = newThreshold();
    }
    
    private int newThreshold() {
        return (int) (tableSize*DEFAULT_LOAD_FACTOR);
    }

    public void put(Object key, Object value) {
        if (value == null) {
            remove(key);
            return;
        }

        putNonBlocking(key,value,false);
    }
    
    /**
     * call never without setting the write lock
     */
    private void putNonBlocking(Object key, Object value, boolean hard) {
        synchronized( writeLock) {
            Entry[] table = this.table;
            int size = this.size;
            
            removeDereferencedEntries();
            int hash = key.hashCode();

            // find existing entry to replace
            int index = index(hash, table.length);

            Entry current=table[index];
            Entry prev = null;
            while (current!=null) {
                if (hash==current.hash) {
                    Object oldKey = current.getKey();
                    if (!current.isValid()) {
                        if (prev!=null) {
                            prev.next = current.next;
                        } else {
                            table[index] = current.next;
                        }
                        current = current.invalidate();
                        size--;
                        continue;
                    }
                    if (key==oldKey | key.equals(oldKey)) {
                        current.setValue(oldKey,value,hard);
                        return;
                    }
                }
                prev = current;
                current = current.next;
            }

            Entry newEntry = new Entry(key,value,hash,hard);
            Entry oldEntry = table[index];
            newEntry.next = oldEntry;
            table[index] = newEntry;

            this.size = size+1;
            if (size>threshold) rehash();
        }
    }
    
    private void rehash() {
        if (size<threshold) return;
        waitForWriteState();
        removeStaleEntries();
        if (size<threshold) return;
        tableSize *= 2;
        Entry[] newTable = new Entry[tableSize];
        transfer(table,newTable);
        table = newTable;
        threshold = newThreshold();
    }
    
    private void removeDereferencedEntries(){
        SoftRef e;
        while ( (e = (SoftRef) queue.poll()) != null) {
            Entry entry = e.getEntry();
            e.clear();
            if (entry!=null) removeEntry(entry);
        }
    }

    private int removeStaleEntries() {
        int count = 0;
        
        for (int i=0; i<table.length; i++) {
            Entry current = table[i];
            Entry prev = null;
            while (current!=null) {
                if (current.isValid()) {
                    prev = current;
                    current = current.next;
                    continue;
                } else {
                    count++;
                    current = current.invalidate();
                    if (prev!=null) {
                        prev.next = current;
                    } else {
                        table[i] = current;
                    }
                }        
            }
        }
        size -= count;
        
        return count;
    }

    private void transfer(Entry[] oldTable, Entry[] newTable) {
        for (int oldIndex=0; oldIndex<oldTable.length; oldIndex++) {
            Entry current = oldTable[oldIndex];
            while (current!=null) {
                int index = index(current.hash,newTable.length);
                Entry newHead = newTable[index];
                Entry newEntry = current;
                current=current.next;
                newEntry.next = newHead;
                newTable[index] = newEntry;                
            }
        }
    }
    
    public void remove(Object key) {
        synchronized(writeLock) {
            waitForWriteState();
            removeDereferencedEntries();
            int hash = key.hashCode();
            
            // find existing entry to replace
            int index = index(hash,table.length);
            Entry current = table[index];
            Entry prev = null;
            
            while (current!=null) {
                if (hash==current.hash) {
                    Object oldKey = current.getKey();
                    if (!current.isValid()) {
                        current = current.invalidate();
                        if (prev!=null) {
                            prev.next = current;
                        } else {
                            table[index] = current;
                        }
                        size--;
                        continue;
                    }
                    if (key==oldKey || key.equals(oldKey)) {
                        current = current.invalidate();
                        if (prev!=null) {
                            prev.next = current;
                        } else {
                            table[index] = current;
                        }
                        size--;
                        return;
                    }
                }                
                prev = current;
                current = current.next;
            }
        }
    }
    
    private void removeEntry(Entry e) {
        int hash = e.hash;
        
        // find existing entry to replace
        int index = index(hash,table.length);
        Entry current = table[index];
        Entry prev = null;
        
        while (current!=null && current!=e) {
            prev = current;
            current = current.next;
        }
        if (current==null) return;
        current = current.invalidate();
        if (prev==null) {
            table[index] = current;
        } else {
            prev.next = current;
        }
        size--;
    }

    private void waitForWriteState() {
        // the loop is needed
        // given two reading threads (TR1,TR2), 
        // one writing Thread TW and TR1 is in unlockWrite,
        // TR2 waits for the monitor in lockWrite,
        // TW waits for a notify. If now TR1 releases
        // the monitor, and TR2 is running, then TW
        // will run as soon as TR2 is leaving lockWrite
        // This means a concurrent erite and read would
        // happen. lockWrite modifies concurrentReads to !=0
        // so if that is rechecked TW goes to sleep again and
        // will be notified as soon as TR2 has passed 
        // unlockWrite.
        synchronized (writeLock) {
            while (concurrentReads!=0) {
                try {
                    writeLock.wait();
                } catch (InterruptedException e) {}
            }
        }
    }

    public void putStrong(Object key, Object value) {
        if (value == null) {
            remove(key);
            return;
        }

        putNonBlocking(key,value,true);
    }
    
    private void lockWrite() {
        // no write should happen while a read
        // is done. concurrentReads==0 means a write
        // is allowed, concurrentReads!=0 means we
        // have a read and the write must wait
        // concurrentReads gives the number of 
        // currently running reads
        synchronized (writeLock) {
            concurrentReads++;
        }
    }
    
    private void unlockWrite(){
        // after the read is finnished we decrease
        // the number of reads by one. If it reaches
        // 0 we wake up the waiting write threads.
        // We wake up all of them, because if we have
        // two waiting wrtier threads only one would been
        // waked up by a notify. The next write would 
        // have to wait till the next read, before it is
        // waked up again... which may mean to wait forever
        synchronized (writeLock) {
            concurrentReads--;
            if (concurrentReads==0) writeLock.notifyAll();
        }
    }
    
    public Object get(Object key) {
        int hash = key.hashCode();
        lockWrite();        
        try {
            int index = index(hash,table.length);

            for (Entry current = table[index]; current!=null; current=current.next) {
                if (hash!=current.hash) continue;
                Object oldKey = current.getKey();
                Object oldValue = current.getValue();
                if (!current.isValid()) continue;
                if (key==oldKey || key.equals(oldKey)) {
                    return oldValue;
                }
            }
        } finally {
            unlockWrite();
        }
        return null;
    }
    
    private int index(int hash, int max) {
        return hash & max-1;
    }

    public int getSize() {
        return size;
    }
    
}
