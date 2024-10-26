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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a {@link Map} that is optimized for a small number of entries. For
 * a number of entries up to {@code listSize} the entries are stored in arrays.
 * After {@code listSize} entries are exceeded storage switches internally to a
 * {@link Map} and converts back to being array based when its size is less than
 * or equal to {@code listSize}.
 * <p>
 * Null keys or values are not supported.
 * <p>
 * This class is not thread-safe!
 */
public class ListHashMap<K,V> implements Map<K,V> {

    private final K[] keys;
    private final V[] values;
    private Map<K,V> innerMap;
    private int size;

    public ListHashMap() {
        this(3);
    }

    @SuppressWarnings("unchecked")
    public ListHashMap(int listSize) {
        keys = (K[]) new Object[listSize];
        values = (V[]) new Object[listSize];
    }

    @Override
    public void clear() {
        innerMap = null;
        clearArrays();
        size = 0;
    }

    private void clearArrays() {
        for (int i = 0, n = keys.length; i < n; i += 1) {
            values[i] = null;
            keys[i] = null;
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (key != null) {
            if (innerMap != null) {
                return innerMap.containsKey(key);
            }
            for (int i = 0; i < size; i += 1) {
                if (key.equals(keys[i])) return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value != null) {
            if (innerMap != null) {
                return innerMap.containsValue(value);
            }
            for (int i = 0; i < size; i += 1) {
                if (value.equals(values[i])) return true;
            }
        }
        return false;
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return (innerMap != null ? Collections.unmodifiableMap(innerMap) : toMap()).entrySet();
    }

    @Override
    public V get(Object key) {
        if (key != null) {
            if (innerMap != null) {
                return innerMap.get(key);
            }
            for (int i = 0; i < size; i += 1) {
                if (key.equals(keys[i])) return values[i];
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return (size == 0);
    }

    @Override
    public Set<K> keySet() {
        return (innerMap != null ? Collections.unmodifiableMap(innerMap) : toMap()).keySet();
    }

    @Override
    public V put(K key, V value) {
        if (key != null) {
            if (value == null) {
                return remove(key);
            }
            if (innerMap != null) {
                V old = innerMap.put(key, value);
                size = innerMap.size();
                return old;
            }
            for (int i = 0; i < size; i += 1) {
                if (key.equals(keys[i])) {
                    V old = values[i];
                    values[i] = value;
                    return old;
                }
            }
            if (size < keys.length) {
                values[size] = value;
                keys[size] = key;
            } else { // evolve
                Map<K,V> map = toMap();
                map.put(key, value);
                innerMap = map;
                clearArrays();
            }
            size += 1;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        if (key != null) {
            if (innerMap != null) {
                V value = innerMap.remove(key);
                if (value != null) {
                    size = innerMap.size();
                    if (size <= keys.length) { // devolve
                        size = 0; Set<Entry<K,V>> entries = innerMap.entrySet(); innerMap = null;
                        for (Entry<? extends K, ? extends V> entry : entries) {
                            values[size] = entry.getValue();
                            keys[size] = entry.getKey();
                            size += 1;
                        }
                    }
                }
                return value;
            }
            for (int i = 0; i < size; i += 1) {
                if (key.equals(keys[i])) {
                    V value = values[i];
                    size -= 1;
                    // if last element is not being removed, shift the last element into this slot
                    if (i < size) {
                        values[i] = values[size];
                        keys[i] = keys[size];
                    }
                    values[size] = null;
                    keys[size] = null;
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private Map<K,V> toMap() {
        Map<K,V> m = new HashMap<>((int) (size / 0.75) + 1);
        for (int i = 0; i < size; i += 1) {
            m.put(keys[i], values[i]);
        }
        return m;
    }

    @Override
    public Collection<V> values() {
        return (innerMap != null ? Collections.unmodifiableMap(innerMap) : toMap()).values();
    }
}
