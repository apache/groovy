package org.codehaus.groovy.util;

public abstract class AbstractConcurrentMapBase {
    protected static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16;
    static final int RETRIES_BEFORE_LOCK = 2;
    final int segmentMask;
    final int segmentShift;
    protected final Segment[] segments;

    public AbstractConcurrentMapBase() {
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

    protected abstract Segment createSegment(int cap);

    protected static <K> int hash(K key) {
        int h = key.hashCode();
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        return h;
    }

    public Segment segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    public int fullSize() {
        int count = 0;
        for (int i = 0; i < segments.length; i++) {
            segments[i].lock();
            try {
                for (int j = 0; j < segments[i].table.length; j++) {
                    Object o = segments[i].table [j];
                    if (o != null) {
                        if (o instanceof Entry) {
                            count++;
                        }
                        else {
                            Object arr [] = (Object[]) o;
                            count += arr.length;
                        }
                    }
                }
            }
            finally {
                segments[i].unlock();
            }
        }
        return count;
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < segments.length; i++) {
            segments[i].lock();
            try {
                for (int j = 0; j < segments[i].table.length; j++) {
                    Object o = segments[i].table [j];
                    if (o != null) {
                        if (o instanceof Entry) {
                            Entry e = (Entry) o;
                            if (e.isValid())
                              count++;
                        }
                        else {
                            Object arr [] = (Object[]) o;
                            for (int k = 0; k < arr.length; k++) {
                                Entry info = (Entry) arr[k];
                                if (info != null && info.isValid())
                                    count++;
                            }
                        }
                    }
                }
            }
            finally {
                segments[i].unlock();
            }
        }
        return count;
    }

    protected static class Segment extends LockableObject {
        volatile int count;

        int threshold;

        volatile Object[] table;

        protected Segment(int initialCapacity) {
            setTable(new Object[initialCapacity]);
        }

        void setTable(Object[] newTable) {
            threshold = (int) (newTable.length * 0.75f);
            table = newTable;
        }

        void removeEntry (Entry e) {
            lock ();
            int newCount = count;
            try {
                Object [] tab = table;
                int index = e.getHash() & (tab.length-1);
                Object o = tab[index];
                if (o != null) {
                    if (o instanceof Entry) {
                        if (o == e) {
                            tab [index] = null;
                            newCount--;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        Object res = null;
                        for (int i = 0; i < arr.length; i++) {
                            Entry info = (Entry) arr[i];
                            if (info != null) {
                                if(info != e) {
                                  if (info.isValid()) {
                                     res = put(info, res);
                                  }
                                  else {
                                      newCount--;
                                  }
                                }
                                else {
                                  newCount--;
                                }
                            }
                        }
                        tab [index] = res;
                    }
                    count = newCount;
                }
            }
            finally {
                unlock();
            }
        }

        void rehash() {
            Object[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
                return;

            int newCount = 0;
            for (int i = 0; i < oldCapacity ; i++) {
                Object o = oldTable [i];
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e.isValid()) {
                            newCount++;
                        }
                        else {
                            oldTable[i] = null;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        int localCount = 0;
                        for (int index = 0; index < arr.length; index++) {
                            Entry e = (Entry) arr[index];
                            if (e != null && e.isValid()) {
                                localCount++;
                            }
                            else {
                                arr [index] = null;
                            }
                        }
                        if (localCount == 0)
                          oldTable[i] = null;
                        else
                          newCount += localCount;
                    }
                }
            }

            Object[] newTable = new Object[newCount+1 < threshold ? oldCapacity : oldCapacity << 1];
            int sizeMask = newTable.length - 1;
            newCount = 0;
            for (int i = 0; i < oldCapacity ; i++) {
                Object o = oldTable[i];
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e.isValid()) {
                            int index = e.getHash() & sizeMask;
                            put(e, index, newTable);
                            newCount++;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int j = 0; j < arr.length; j++) {
                            Entry e = (Entry) arr[j];
                            if (e != null && e.isValid()) {
                                int index = e.getHash() & sizeMask;
                                put(e, index, newTable);
                                newCount++;
                            }
                        }
                    }
                }
            }

            threshold = (int)(newTable.length * 0.75f);

            table = newTable;
            count = newCount;
        }

        private void put(Entry ee, int index, Object[] tab) {
            Object o = tab[index];
            if (o != null) {
                if (o instanceof Entry) {
                    Object arr [] = new Object [2];
                    arr [0] = ee;
                    arr [1] = (Entry) o;
                    tab[index] = arr;
                    return;
                }
                else {
                    Object arr [] = (Object[]) o;
                    Object newArr [] = new Object[arr.length+1];
                    newArr [0] = ee;
                    System.arraycopy(arr, 0, newArr, 1, arr.length);
                    tab [index] = newArr;
                    return;
                }
            }
            tab[index] = ee;
        }

        private Object put(Entry ee, Object o) {
            if (o != null) {
                if (o instanceof Entry) {
                    Object arr [] = new Object [2];
                    arr [0] = ee;
                    arr [1] = (Entry) o;
                    return arr;
                }
                else {
                    Object arr [] = (Object[]) o;
                    Object newArr [] = new Object[arr.length+1];
                    newArr [0] = ee;
                    System.arraycopy(arr, 0, newArr, 1, arr.length);
                    return newArr;
                }
            }
            return ee;
        }
    }

    static interface Entry<V> {
        V getValue();

        void setValue(V value);

        int getHash();

        boolean isValid();
    }
}
