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
package org.codehaus.groovy.util;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractConcurrentMapBase {
    protected static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MAX_SEGMENTS = 1 << 16;
    static final int RETRIES_BEFORE_LOCK = 2;
    final int segmentMask;
    final int segmentShift;
    protected final Segment[] segments;

    public AbstractConcurrentMapBase(Object segmentInfo) {
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
            this.segments[i] = createSegment(segmentInfo, cap);
    }

    protected abstract Segment createSegment(Object segmentInfo, int cap);

    protected static <K> int hash(K key) {
        int h = System.identityHashCode(key);
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
        for (Segment segment : segments) {
            segment.lock();
            try {
                for (int j = 0; j < segment.table.length; j++) {
                    Object o = segment.table[j];
                    if (o != null) {
                        if (o instanceof Entry) {
                            count++;
                        } else {
                            Object arr[] = (Object[]) o;
                            count += arr.length;
                        }
                    }
                }
            } finally {
                segment.unlock();
            }
        }
        return count;
    }

    public int size() {
        int count = 0;
        for (Segment segment : segments) {
            segment.lock();
            try {
                for (int j = 0; j < segment.table.length; j++) {
                    Object o = segment.table[j];
                    if (o != null) {
                        if (o instanceof Entry) {
                            Entry e = (Entry) o;
                            if (e.isValid())
                                count++;
                        } else {
                            Object arr[] = (Object[]) o;
                            for (Object value : arr) {
                                Entry info = (Entry) value;
                                if (info != null && info.isValid())
                                    count++;
                            }
                        }
                    }
                }
            } finally {
                segment.unlock();
            }
        }
        return count;
    }

    public Collection values() {
        Collection result = new LinkedList();
        for (Segment segment : segments) {
            segment.lock();
            try {
                for (int j = 0; j < segment.table.length; j++) {
                    Object o = segment.table[j];
                    if (o != null) {
                        if (o instanceof Entry) {
                            Entry e = (Entry) o;
                            if (e.isValid())
                                result.add(e);
                        } else {
                            Object arr[] = (Object[]) o;
                            for (Object value : arr) {
                                Entry info = (Entry) value;
                                if (info != null && info.isValid())
                                    result.add(info);
                            }
                        }
                    }
                }
            } finally {
                segment.unlock();
            }
        }
        return result;
    }

    public static class Segment extends LockableObject {
        private static final long serialVersionUID = -4128828550135386431L;
        volatile int count;

        int threshold;

        protected volatile Object[] table;

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
                        for (Object value : arr) {
                            Entry info = (Entry) value;
                            if (info != null) {
                                if (info != e) {
                                    if (info.isValid()) {
                                        res = put(info, res);
                                    } else {
                                        newCount--;
                                    }
                                } else {
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

        void rehashIfThresholdExceeded() {
            if(count > threshold) {
                rehash();
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
            for (Object o : oldTable) {
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e.isValid()) {
                            int index = e.getHash() & sizeMask;
                            put(e, index, newTable);
                            newCount++;
                        }
                    } else {
                        Object arr[] = (Object[]) o;
                        for (Object value : arr) {
                            Entry e = (Entry) value;
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

        private static void put(Entry ee, int index, Object[] tab) {
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

        private static Object put(Entry ee, Object o) {
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

    public interface Entry<V> {
        V getValue();

        void setValue(V value);

        int getHash();

        boolean isValid();
    }
}
