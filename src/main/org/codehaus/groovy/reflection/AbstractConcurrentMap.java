package org.codehaus.groovy.reflection;

public abstract class AbstractConcurrentMap<K, V> {
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16;
    static final int RETRIES_BEFORE_LOCK = 2;
    final int segmentMask;
    final int segmentShift;
    final Segment[] segments;

    public AbstractConcurrentMap() {
        int sshift = 0;
        int ssize = 1;
        while (ssize < 16) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        this.segments = new Segment[ssize];

        int c = 512 / ssize;
        if (c * ssize < 512)
            ++c;
        int cap = 1;
        while (cap < c)
            cap <<= 1;

        for (int i = 0; i < this.segments.length; ++i)
            this.segments[i] = createSegment(cap);
    }

    protected abstract Segment<K,V> createSegment(int cap);

    static <K> int hash(K key) {
        int h = key.hashCode();
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        return h;
    }

    final Segment<K,V> segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    public int fullSize() {
        int count = 0;
        for (int i = 0; i < segments.length; i++) {
            for (int j = 0; j < segments[i].table.length; j++) {
                for (Entry e = segments[i].table[j]; e != null; e = e.getNext())
                    count++;
            }
        }
        return count;
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < segments.length; i++) {
            for (int j = 0; j < segments[i].table.length; j++) {
                for (Entry e = segments[i].table[j]; e != null; e = e.getNext())
                  if (e.isValid())
                    count++;
            }
        }
        return count;
    }

    public V get(K key) {
        int hash = hash(key);
        return segmentFor(hash).get(key, hash);
    }

    public Object getOrPut(K key, V value) {
        int hash = hash(key);
        return segmentFor(hash).getOrPut(key, hash, value);
    }

    public void put(K key, V value) {
        int hash = hash(key);
        segmentFor(hash).put(key, hash, value);
    }

    abstract static class Segment<K,V> extends LockableObject{
        volatile int count;

        int threshold;

        volatile Entry<K,V>[] table;

        Segment(int initialCapacity) {
            setTable(new Entry[initialCapacity]);
        }

        void setTable(Entry<K,V>[] newTable) {
            threshold = (int)(newTable.length * 0.75f);
            table = newTable;
        }

        Entry<K,V> getFirst(int hash) {
            Entry<K,V>[] tab = table;
            return tab[hash & (tab.length - 1)];
        }

        V get(K key, int hash) {
            Entry<K,V> e = getFirst(hash);
            while (e != null) {
                if (e.isEqual(key,hash)) {
                    return e.getValue();
                }
                e = e.getNext();
            }
            return null;
        }

        V getOrPut(K key, int hash, V value) {
            Entry<K,V> e = getFirst(hash);
            while (e != null) {
                if (e.isEqual(key,hash)) {
                    return e.getValue();
                }
                e = e.getNext();
            }
            put (key, hash, value);
            return value;
        }

        void put(K key, int hash, V value) {
            lock();
            try {
                int c = count;
                if (c++ > threshold) {
                    rehash();
                }

                Entry<K,V>[] tab = table;
                int index = hash & (tab.length - 1);
                Entry<K,V> first = tab[index];
                Entry<K,V> e = first;
                while (e != null) {
                    if (e.isEqual(key,hash)) {
                        e.setValue(value);
                        return;
                    }
                    e = e.getNext();
                }

                tab[index] = createEntry(key, hash, value, first);
                count = c; // write-volatile
            } finally {
                unlock();
            }
        }

        void rehash() {
            Entry<K,V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
                return;

            int newCount = 0;
            for (int i = 0; i < oldCapacity ; i++) {
                Entry<K,V> first = null;
                for (Entry<K,V> e = oldTable[i]; e != null; ) {
                   if (e.isValid()) {
                       if (first == null)
                         first = e;

                       Entry<K,V> ee = e.getNext();
                       while (ee != null && !ee.isValid())
                         ee = ee.getNext();
                       e.setNext(ee);
                       e = ee;
                       newCount++;
                   }
                   else {
                     e = e.getNext();
                   }
                }

                oldTable [i] = first;
            }

            if (newCount+1 < threshold) {
                count = newCount;
                return;
            }

            Entry<K,V>[] newTable = new Entry[oldCapacity << 1];
            int sizeMask = newTable.length - 1;
            newCount = 0;
            for (int i = 0; i < oldCapacity ; i++) {
                for (Entry<K,V> e = oldTable[i]; e != null; e = e.getNext()) {
                   int idx = e.getHash() & sizeMask;
                   final Entry<K,V> next = newTable[idx];
                   if (next == null && e.getNext() == null)
                     newTable[idx] = e;
                   else
                     newTable[idx] = createEntry(e, next);
                   newCount++;
                }
            }

            threshold = (int)(newTable.length * 0.75f);

            table = newTable;
            count = newCount;
        }

        protected abstract Entry<K,V> createEntry(Entry<K,V> e, Entry<K,V> next);

        protected abstract Entry<K,V> createEntry(K key, int hash, V value, Entry<K,V> first);
    }

    static interface Entry<K,V> {
        Entry<K,V> getNext ();

        V getValue ();

        boolean isValid ();

        boolean isEqual (K key, int hash);

        void setValue(V value);

        void setNext(Entry<K,V> ee);

        int getHash();
    }
}
