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
            final Object[] table = segments[i].table;
            for (int j = 0; j < table.length; j++) {
               count += countFull(table[j]);
            }
        }
        return count;
    }

    private int countFull(Object o) {
        if (o == null)
          return 0;

        if (o instanceof Entry)
          return 1;

        return ((Entry[])o).length;
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < segments.length; i++) {
            final Object[] table = segments[i].table;
            for (int j = 0; j < table.length; j++) {
               count += countSize(table[j]);
            }
        }
        return count;
    }

    private int countSize(Object o) {
        if (o == null)
          return 0;

        if (o instanceof Entry)
          return ((Entry)o).isValid() ? 1 : 0;

        final Entry[] arr = (Entry[]) o;
        int count = 0;
        for (Entry entry : arr) {
            if (entry.isValid())
              count++;
        }
        return count;
    }

    public V get(K key) {
        int hash = hash(key);
        return segmentFor(hash).get(key, hash);
    }

    public Entry<K,V> getOrPut(K key, V value) {
        int hash = hash(key);
        return segmentFor(hash).getOrPut(key, hash, value);
    }

    public void put(K key, V value) {
        int hash = hash(key);
        segmentFor(hash).put(key, hash).setValue(value);
    }

    public void remove(K key) {
        int hash = hash(key);
        segmentFor(hash).remove(key, hash);
    }

    abstract static class Segment<K,V> extends LockableObject{
        volatile int count;

        int threshold;

        volatile Object[] table;

        Segment(int initialCapacity) {
            setTable(new Object[initialCapacity]);
        }

        void setTable(Object[] newTable) {
            threshold = (int)(newTable.length * 0.75f);
            table = newTable;
        }

        V get(K key, int hash) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K,V> e = (Entry<K,V>) o;
                    if (e.isEqual(key,hash)) {
                        return e.getValue();
                    }
                }
                else {
                    Entry<K,V> arr [] = (Entry<K,V>[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K,V> e = arr [i];
                      if (e != null && e.isEqual(key, hash))
                        return e.getValue();
                    }
                }
            }
            return null;
        }

        Entry<K,V> getOrPut(K key, int hash, V value) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K,V> e = (Entry<K,V>) o;
                    if (e.isEqual(key,hash)) {
                        return e;
                    }
                }
                else {
                    Entry<K,V> arr [] = (Entry<K,V>[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K,V> e = arr [i];
                      if (e != null && e.isEqual(key, hash))
                        return e;
                    }
                }
            }

            final Entry<K, V> kvEntry = put(key, hash);
            kvEntry.setValue(value);
            return kvEntry;
        }

        Entry<K,V> put(K key, int hash) {
            lock();
            try {
                int c = count;
                if (c++ > threshold) {
                    rehash();
                }

                Object[] tab = table;
                final int index = hash & (tab.length - 1);
                final Object o = tab[index];
                if (o != null) {
                    if (o instanceof Entry) {
                        final Entry<K,V> e = (Entry<K,V>) o;
                        if (e.isEqual(key,hash)) {
                            return e;
                        }
                        final Entry[] arr = new Entry[2];
                        final Entry<K, V> res = createEntry(key, hash);
                        arr [0] = res;
                        arr [1] = e;
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                    else {
                        final Entry<K,V> arr [] = (Entry<K,V>[]) o;
                        for (int i = 0; i != arr.length; ++i) {
                          Entry<K,V> e = arr [i];
                          if (e != null && e.isEqual(key, hash)) {
                            return e;
                          }
                        }
                        final Entry[] newArr = new Entry[arr.length+1];
                        final Entry<K, V> res = createEntry(key, hash);
                        arr [0] = res;
                        System.arraycopy(arr, 0, newArr, 1, arr.length);
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                }

                final Entry<K, V> res = createEntry(key, hash);
                tab[index] = res;
                count = c; // write-volatile
                return res;

            } finally {
                unlock();
            }
        }

        void rehash() {
            Object[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
                return;

            Object[] newTable = new Object[oldCapacity << 1];
            int sizeMask = newTable.length - 1;
            int newCount = 0;

            for (int i = 0; i < oldCapacity ; i++) {
                Object o = oldTable[i];
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e.isValid()) {
                            newCount++;
                            rehash(newTable, sizeMask, e);
                        }
                    }
                    else {
                        Entry arr [] = (Entry[]) o;
                        for (int j = 0; j < arr.length; j++) {
                            Entry e = arr[j];
                            if (e != null && e.isValid()) {
                              newCount++;
                              rehash(newTable, sizeMask, e);
                            }
                        }
                    }
                }
            }

            threshold = (int)(newTable.length * 0.75f);
            table = newTable;
            count = newCount;
        }

        private static void rehash(Object[] newTable, int sizeMask, Entry e) {
            int index = e.getHash() & sizeMask;
            final Object kvEntry = newTable[index];
            if (kvEntry == null)
              newTable [index] = e;
            else {
              if (kvEntry instanceof Entry) {
                 Entry[] arr = new Entry[2];
                 arr [0] = (Entry) kvEntry;
                 arr [1] = e;
                 newTable [index] = arr;
              }
              else {
                 Entry arr [] = (Entry[]) kvEntry;
                 Entry newArr [] = new Entry[arr.length+1];
                 arr [0] = e;
                 System.arraycopy(arr, 0, newArr, 1, arr.length);
                 newTable[index] = arr;
              }
            }
        }

        public void remove(K key, int hash) {
//            lock();
//            try {
//                int c = count-1;
//                final Object[] tab = table;
//                final int index = hash & (tab.length - 1);
//                Object o = tab[index];
//
//                if (o != null) {
//                    if (o instanceof Entry) {
//                        if (((Entry<K,V>)o).isEqual(key, hash)) {
//                          tab[index] = null;
//                          count = c;
//                        }
//                    }
//                    else {
//                        Entry<K,V> arr [] = (Entry<K,V>[]) o;
//                        for (int i = 0; i < arr.length; i++) {
//                            Entry<K,V> e = arr[i];
//                            if (e != null && e.isEqual(key, hash)) {
//                                arr [i] = null;
//                                count = c;
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            finally {
//                unlock();
//            }
        }

        protected abstract Entry<K,V> createEntry(K key, int hash);
    }

    static interface Entry<K,V> {
        V getValue ();

        void setValue(V value);

        int getHash();

        boolean isValid ();

        boolean isEqual (K key, int hash);
    }
}
