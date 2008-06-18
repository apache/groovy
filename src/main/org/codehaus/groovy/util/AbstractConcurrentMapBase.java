package org.codehaus.groovy.util;

public abstract class AbstractConcurrentMapBase {
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16;
    static final int RETRIES_BEFORE_LOCK = 2;
    final int segmentMask;
    final int segmentShift;
    final Segment[] segments;

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

        return ((Object[]) o).length;
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

    Segment segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    public void removeEntry (Entry e) {
        segmentFor(e.getHash()).removeEntry(e);
    }

    private int countSize(Object o) {
        if (o == null)
            return 0;

        if (o instanceof Entry)
            return ((Entry) o).isValid() ? 1 : 0;

        final Object[] arr = (Object[]) o;
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            Entry entry = (Entry) arr[i];
            if (entry.isValid())
                count++;
        }
        return count;
    }                                                                                                   

    abstract static class Segment extends LockableObject {
        volatile int count;

        int threshold;

        volatile Object[] table;

        Segment(int initialCapacity) {
            setTable(new Object[initialCapacity]);
        }

        void setTable(Object[] newTable) {
            threshold = (int) (newTable.length * 0.75f);
            table = newTable;
        }

        void removeEntry (Entry entry) {
            lock ();
            try {
                Object[] tab = table;
                int hash = entry.getHash();
                final int index = hash & (tab.length - 1);
                Object o = tab[index];
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e == entry) {
                            tab[index] = null;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i != arr.length; ++i) {
                          Entry e = (Entry) arr [i];
                          if (e == entry) {
                              if (arr.length == 2) {
                                if (i == 0) {
                                    tab[index] = arr[1];
                                }
                                else {
                                    tab[index] = arr[0];
                                }
                              }
                              else {
                                  Object newArr [] = new Object[arr.length-1];
                                  System.arraycopy(arr, 0,   newArr, 0, i);
                                  System.arraycopy(arr, i+1, newArr, i, arr.length-i-1);
                              }
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
                        Object[] arr  = (Object[]) o;
                        for (int j = 0; j < arr.length; j++) {
                            Entry e = (Entry) arr[j];
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

        void rehash(Object[] newTable, int sizeMask, Entry e) {
            int index = e.getHash() & sizeMask;
            final Object kvEntry = newTable[index];
            if (kvEntry == null)
                newTable[index] = e;
            else {
                if (kvEntry instanceof Entry) {
                    Object[] arr = new Object[2];
                    arr[0] = (Entry) kvEntry;
                    arr[1] = e;
                    newTable[index] = arr;
                } else {
                    Object arr[] = (Object[]) kvEntry;
                    Object newArr[] = new Object[arr.length + 1];
                    arr[0] = e;
                    System.arraycopy(arr, 0, newArr, 1, arr.length);
                    newTable[index] = arr;
                }
            }
        }
    }

    static interface Entry<V> {
        V getValue();

        void setValue(V value);

        int getHash();

        boolean isValid();
    }
}
