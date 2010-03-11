/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.util;

public abstract class AbstractConcurrentMap<K, V> extends AbstractConcurrentMapBase {

    public AbstractConcurrentMap(Object segmentInfo) {
        super(segmentInfo);
    }

    public Segment segmentFor (int hash) {
        return (Segment) super.segmentFor(hash);
    }

    public V get(K key) {
        int hash = hash(key);
        return (V) segmentFor(hash).get(key, hash);
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

    public abstract static class Segment<K,V> extends AbstractConcurrentMapBase.Segment {

        protected Segment(int initialCapacity) {
            super(initialCapacity);
        }

        public final V get(K key, int hash) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K,V> e = (Entry) o;
                    if (e.isEqual(key, hash)) {
                        return e.getValue();
                    }
                }
                else {
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i < arr.length; i++) {
                        Entry<K,V> e = (Entry<K,V>) arr[i];
                        if (e != null && e.isEqual(key, hash)) {
                            return e.getValue();
                        }
                    }
                }
            }
            return null;
        }

        public final Entry<K,V> getOrPut(K key, int hash, V value) {
            Object[] tab = table;
            Object o = tab[hash & (tab.length - 1)];
            if (o != null) {
                if (o instanceof Entry) {
                    Entry<K,V> e = (Entry) o;
                    if (e.isEqual(key, hash)) {
                        return e;
                    }
                }
                else {
                    Object arr [] = (Object[]) o;
                    for (int i = 0; i < arr.length; i++) {
                        Entry<K,V> e = (Entry<K,V>) arr[i];
                        if (e != null && e.isEqual(key, hash)) {
                            return e;
                        }
                    }
                }
            }
            return put(key, hash, value);
        }

        public final Entry put(K key, int hash, V value) {
            lock();
            try {
                int c = count;
                if (c++ > threshold) {
                    rehash();
                }

                Object[] tab = table;
                int index = hash & (tab.length - 1);
                Object o = tab[index];
                if (o != null) {
                    if (o instanceof Entry) {
                        Entry e = (Entry) o;
                        if (e.isEqual(key, hash)) {
                            e.setValue(value);
                            return e;
                        }
                        else {
                            Object arr [] = new Object [2];
                            final Entry ee = createEntry(key, hash, value);
                            arr [0] = ee;
                            arr [1] = e;
                            tab[index] = arr;
                            count = c;
                            return ee;
                        }
                    }
                    else {
                        Object arr [] = (Object[]) o;
                        for (int i = 0; i < arr.length; i++) {
                            Entry e = (Entry) arr[i];
                            if (e != null && e.isEqual(key, hash)) {
                                e.setValue(value);
                                return e;
                            }
                        }

                        final Entry ee = createEntry(key, hash, value);
                        for (int i = 0; i < arr.length; i++) {
                            Entry e = (Entry) arr[i];
                            if (e == null) {
                                arr [i] = ee;
                                count = c;
                                return ee;
                            }
                        }

                        Object newArr [] = new Object[arr.length+1];
                        newArr [0] = ee;
                        System.arraycopy(arr, 0, newArr, 1, arr.length);
                        tab [index] = newArr;
                        count = c;
                        return ee;
                    }
                }

                Entry e = createEntry(key, hash, value);
                tab[index] = e;
                count = c; // write-volatile
                return e;
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

    public interface Entry<K, V> extends AbstractConcurrentMapBase.Entry<V>{
        boolean isEqual(K key, int hash);
    }
}
