package org.codehaus.groovy.util;

public abstract class AbstractConcurrentMap<K, V> extends AbstractConcurrentMapBase {
    public AbstractConcurrentMap() {
    }

    static <K> int hash(K key) {
        int h = key.hashCode();
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        return h;
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
        segmentFor(hash).put(key, hash, value);
    }

    public void remove(K key) {
        int hash = hash(key);
        segmentFor(hash).remove(key, hash);
    }

    final Segment<K,V> segmentFor(int hash) {
        return (Segment<K,V>) segments[(hash >>> segmentShift) & segmentMask];
    }

    protected abstract static class Segment<K,V> extends AbstractConcurrentMapBase.Segment {
        protected Segment(int initialCapacity) {
            super(initialCapacity);
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
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K,V> e = (Entry<K,V>) arr [i];
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
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K,V> e = (Entry<K,V>) arr [i];
                      if (e != null && e.isEqual(key, hash))
                        return e;
                    }
                }
            }

            final Entry<K, V> kvEntry = put(key, hash, value);
            kvEntry.setValue(value);
            return kvEntry;
        }

        Entry<K,V> put(K key, int hash, V value) {
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
                            e.setValue(value);
                            return e;
                        }
                        final Entry[] arr = new Entry[2];
                        final Entry<K, V> res = createEntry(key, hash, value);
                        arr [0] = res;
                        arr [1] = e;
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i != arr.length; ++i) {
                          Entry<K,V> e = (Entry<K,V>) arr [i];
                          if (e != null && e.isEqual(key, hash)) {
                            e.setValue(value);
                            return e;
                          }
                        }
                        final Object[] newArr = new Object[arr.length+1];
                        final Entry<K, V> res = createEntry(key, hash, value);
                        arr [0] = res;
                        System.arraycopy(arr, 0, newArr, 1, arr.length);
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                }

                final Entry<K, V> res = createEntry(key, hash, value);
                tab[index] = res;
                count = c; // write-volatile
                return res;

            } finally {
                unlock();
            }
        }

        public void remove(K key, int hash) {
            lock();
            try {
                int c = count-1;
                final Object[] tab = table;
                final int index = hash & (tab.length - 1);
                Object o = tab[index];

                if (o != null) {
                    if (o instanceof Entry) {
                        if (((Entry<K,V>)o).isEqual(key, hash)) {
                          tab[index] = null;
                          count = c;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i < arr.length; i++) {
                            Entry<K,V> e = (Entry<K,V>) arr[i];
                            if (e != null && e.isEqual(key, hash)) {
                                arr [i] = null;
                                count = c;
                                break;
                            }
                        }
                    }
                }
            }
            finally {
                unlock();
            }
        }

        protected abstract Entry<K,V> createEntry(K key, int hash, V value);
    }

    protected static interface Entry<K, V> extends AbstractConcurrentMapBase.Entry<V>{
        boolean isEqual(K key, int hash);
    }
}
