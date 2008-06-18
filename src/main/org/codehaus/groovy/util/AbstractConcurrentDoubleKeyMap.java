package org.codehaus.groovy.util;

public abstract class AbstractConcurrentDoubleKeyMap<K1,K2,V> extends AbstractConcurrentMapBase {
    public AbstractConcurrentDoubleKeyMap() {
    }

    static <K1,K2> int hash(K1 key1, K2 key2) {
        int h = 31*key1.hashCode() + key2.hashCode();
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        return h;
    }

    public V get(K1 key1, K2 key2) {
        int hash = hash(key1, key2);
        return segmentFor(hash).get(key1, key2, hash);
    }

    public Entry<K1,K2,V> getOrPut(K1 key1, K2 key2, V value) {
        int hash = hash(key1,key2);
        return segmentFor(hash).getOrPut(key1, key2, hash, value);
    }

    public void put(K1 key1, K2 key2, V value) {
        int hash = hash(key1, key2);
        segmentFor(hash).put(key1, key2, hash).setValue(value);
    }

    public void remove(K1 key1, K2 key2) {
        int hash = hash(key1, key2);
        segmentFor(hash).remove(key1, key2, hash);
    }

    final Segment<K1,K2,V> segmentFor(int hash) {
        return (Segment<K1,K2,V>) segments[(hash >>> segmentShift) & segmentMask];
    }

    abstract static class Segment<K1,K2,V> extends AbstractConcurrentMapBase.Segment {
        Segment(int initialCapacity) {
            super(initialCapacity);
        }

        V get(K1 key1, K2 key2, int hash) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K1,K2,V> e = (Entry<K1,K2,V>) o;
                    if (e.isEqual(key1,key2,hash)) {
                        return e.getValue();
                    }
                }
                else {
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K1,K2,V> e = (Entry<K1,K2,V>) arr [i];
                      if (e != null && e.isEqual(key1, key2, hash))
                        return e.getValue();
                    }
                }
            }
            return null;
        }

        Entry<K1,K2,V> getOrPut(K1 key1, K2 key2, int hash, V value) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K1,K2,V> e = (Entry<K1,K2,V>) o;
                    if (e.isEqual(key1,key2,hash)) {
                        return e;
                    }
                }
                else {
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i != arr.length; ++i) {
                      Entry<K1,K2,V> e = (Entry<K1,K2,V>) arr [i];
                      if (e != null && e.isEqual(key1, key2, hash))
                        return e;
                    }
                }
            }

            final Entry<K1,K2,V> kvEntry = put(key1, key2, hash);
            kvEntry.setValue(value);
            return kvEntry;
        }

        Entry<K1,K2,V> put(K1 key1, K2 key2, int hash) {
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
                        final Entry<K1,K2,V> e = (Entry<K1,K2,V>) o;
                        if (e.isEqual(key1,key2,hash)) {
                            return e;
                        }
                        final Object[] arr = new Object[2];
                        final Entry<K1,K2,V> res = createEntry(key1, key2, hash);
                        arr [0] = res;
                        arr [1] = e;
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i != arr.length; ++i) {
                          Entry<K1,K2,V> e = (Entry<K1,K2,V>) arr [i];
                          if (e != null && e.isEqual(key1, key2, hash)) {
                            return e;
                          }
                        }
                        final Object[] newArr = new Object[arr.length+1];
                        final Entry<K1,K2,V> res = createEntry(key1,key2, hash);
                        arr [0] = res;
                        System.arraycopy(arr, 0, newArr, 1, arr.length);
                        tab[index] = arr;
                        count = c; // write-volatile
                        return res;
                    }
                }

                final Entry<K1,K2,V> res = createEntry(key1, key2, hash);
                tab[index] = res;
                count = c; // write-volatile
                return res;

            } finally {
                unlock();
            }
        }

        public void remove(K1 key1, K2 key2, int hash) {
            lock();
            try {
                int c = count-1;
                final Object[] tab = table;
                final int index = hash & (tab.length - 1);
                Object o = tab[index];

                if (o != null) {
                    if (o instanceof Entry) {
                        if (((Entry<K1,K2,V>)o).isEqual(key1, key2, hash)) {
                          tab[index] = null;
                          count = c;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i < arr.length; i++) {
                            Entry<K1,K2,V> e = (Entry<K1,K2,V>) arr[i];
                            if (e != null && e.isEqual(key1, key2, hash)) {
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

        protected abstract Entry<K1,K2,V> createEntry(K1 key1, K2 key2, int hash);
    }

    static interface Entry<K1, K2, V> extends AbstractConcurrentMapBase.Entry<V>{
        boolean isEqual(K1 key1, K2 key2, int hash);
    }
}
