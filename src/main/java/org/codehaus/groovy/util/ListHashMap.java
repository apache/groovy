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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a {@link Map} that is optimized for a small number of
 * entries.  For a number of entries up to {@code listSize} the entries
 * are stored in arrays.  After {@code listSize} entries are exceeded
 * storage switches internally to a {@link Map} and converts back
 * to being array based when its size is less than or equal to {@code listSize}.
 *
 * Null keys or values are not supported.
 *
 * This class is not thread safe.
 */
public class ListHashMap<K,V> implements Map<K,V> {
    private final Object[] listKeys;
    private final Object[] listValues;
    private int size = 0;
    private Map<K,V> innerMap;
    private final int maxListFill;

    public ListHashMap() {
        this(3);
    }

    public ListHashMap(int listSize){
        this.listKeys = new Object[listSize];
        this.listValues = new Object[listSize];
        maxListFill = listSize;
    }

    @Override
    public void clear() {
        innerMap = null;
        clearArrays();
        size = 0;
    }

    private void clearArrays() {
        for (int i=0; i<maxListFill; i++) {
            listValues[i] = null;
            listKeys[i] = null;
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (size == 0) {
            return false;
        }
        if (innerMap == null) {
            for (int i=0; i<size; i++) {
                if (listKeys[i].equals(key)) return true;
            }
            return false;
        } else {
            return innerMap.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if (size == 0) {
            return false;
        }
        if (innerMap == null) {
            for (int i=0; i<size; i++) {
                if (listValues[i].equals(value)) return true;
            }
            return false;
        } else {
            return innerMap.containsValue(value);
        }
    }

    private Map<K,V> makeMap() {
        Map<K,V> m = new HashMap();
        for (int i=0; i<size; i++) {
            m.put((K) listKeys[i], (V) listValues[i]);
        }
        return m;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Map m = innerMap!=null?innerMap:makeMap();
        return m.entrySet();
    }

    @Override
    public V get(Object key) {
        if(size==0) return null;
        if (innerMap==null) {
            for (int i=0; i<size; i++) {
                if (listKeys[i].equals(key)) return (V) listValues[i];
            }
            return null;
        } else {
            return innerMap.get(key);
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Set<K> keySet() {
        Map m = innerMap!=null?innerMap:makeMap();
        return m.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (innerMap==null) {
            for (int i=0; i<size; i++) {
                if (listKeys[i].equals(key)) {
                    V old = (V) listValues[i];
                    listValues[i] = value;
                    return old;
                }
            }
            if (size<maxListFill) {
                listKeys[size] = key;
                listValues[size] = value;
                size++;
                return null;
            } else {
                innerMap = makeMap();
                // Switched over to Map so need to clear array references
                clearArrays();
            }
        }
        V val = (V) innerMap.put(key, value);
        size = innerMap.size();
        return val;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        if (innerMap==null) {
            for (int i=0; i<size; i++) {
                if (listKeys[i].equals(key)) {
                    V old = (V) listValues[i];
                    size--;
                    // If last element is not being removed shift the last element into this slot
                    if (i < size) {
                        listValues[i] = listValues[size];
                        listKeys[i] = listKeys[size];
                    }
                    listValues[size] = null;
                    listKeys[size] = null;
                    return old;
                }
            }
            return null;
        } else {
            V old = innerMap.remove(key);
            size = innerMap.size();
            if (size<=maxListFill) {
                mapToList();
            }
            return old;
        }
    }

    private void mapToList() {
        int i = 0;
        for (Entry<? extends K,? extends V> entry : innerMap.entrySet()) {
            listKeys[i] = entry.getKey();
            listValues[i] = entry.getValue();
            i++;
        }
        size = innerMap.size();
        innerMap = null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Collection<V> values() {
        if (innerMap == null) {
            List<V> list = new ArrayList<V>(size);
            for (int i = 0; i < size; i++) {
                list.add((V) listValues[i]);
            }
            return list;
        } else {
            return innerMap.values();
        }
    }

}
