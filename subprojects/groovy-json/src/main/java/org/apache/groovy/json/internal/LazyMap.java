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
package org.apache.groovy.json.internal;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This maps only builds once you ask for a key for the first time.
 * It is designed to not incur the overhead of creating a map unless needed.
 */
public class LazyMap extends AbstractMap<String, Object> {

    /**
     * System property controlling alternative hashing support in older JDK map implementations.
     */
    static final String JDK_MAP_ALTHASHING_SYSPROP = System.getProperty("jdk.map.althashing.threshold");

    /* Holds the actual map that will be lazily created. */
    private Map<String, Object> map;
    /* The size of the map. */
    private int size;
    /* The keys  stored in the map. */
    private String[] keys;
    /* The values stored in the map. */
    private Object[] values;

    /**
     * Creates a lazy map with the default key and value buffer size.
     */
    public LazyMap() {
        keys = new String[5];
        values = new Object[5];
    }

    /**
     * Creates a lazy map with a caller-supplied key and value buffer size.
     *
     * @param initialSize initial buffer size
     */
    public LazyMap(int initialSize) {
        keys = new String[initialSize];
        values = new Object[initialSize];
    }

    /**
     * Stores a mapping while keeping the compact array-backed representation until hydration is needed.
     *
     * @param key entry key
     * @param value entry value
     * @return previous value for {@code key}, or {@code null}
     */
    @Override
    public Object put(String key, Object value) {
        if (map == null) {
            for (int i = 0; i < size; i++) {
                String curKey = keys[i];
                if ((key == null && curKey == null)
                     || (key != null && key.equals(curKey))) {
                    Object val = values[i];
                    keys[i] = key;
                    values[i] = value;
                    return val;
                }
            }
            keys[size] = key;
            values[size] = value;
            size++;
            if (size == keys.length) {
                keys = grow(keys);
                values = grow(values);
            }
            return null;
        } else {
            return map.put(key, value);
        }
    }

    /**
     * Returns the hydrated entry set, building the backing map if necessary.
     *
     * @return hydrated entry set
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        buildIfNeeded();
        return map.entrySet();
    }

    /**
     * Returns the current size without hydrating when the compact representation is still active.
     *
     * @return map size
     */
    @Override
    public int size() {
        if (map == null) {
            return size;
        } else {
            return map.size();
        }
    }

    /**
     * Returns whether the map currently contains any entries.
     *
     * @return {@code true} when no entries are stored
     */
    @Override
    public boolean isEmpty() {
        if (map == null) {
            return size == 0;
        } else {
            return map.isEmpty();
        }
    }

    /**
     * Hydrates the backing map if needed and tests whether the value is present.
     *
     * @param value value to test
     * @return {@code true} when the value is present
     */
    @Override
    public boolean containsValue(Object value) {
        buildIfNeeded();
        return map.containsValue(value);
    }

    /**
     * Hydrates the backing map if needed and tests whether the key is present.
     *
     * @param key key to test
     * @return {@code true} when the key is present
     */
    @Override
    public boolean containsKey(Object key) {
        buildIfNeeded();
        return map.containsKey(key);
    }

    /**
     * Hydrates the backing map if needed and returns the value for the supplied key.
     *
     * @param key key to look up
     * @return mapped value, or {@code null}
     */
    @Override
    public Object get(Object key) {
        buildIfNeeded();
        return map.get(key);
    }

    private void buildIfNeeded() {
        if (map == null) {
            // added to avoid hash collision attack
            if (Sys.is1_8OrLater() || (Sys.is1_7() && JDK_MAP_ALTHASHING_SYSPROP != null)) {
                map = new LinkedHashMap<String, Object>(size, 0.01f);
            } else {
                map = new TreeMap<String, Object>();
            }

            for (int index = 0; index < size; index++) {
                map.put(keys[index], values[index]);
            }
            this.keys = null;
            this.values = null;
        }
    }

    /**
     * Hydrates the backing map if needed and removes the supplied key.
     *
     * @param key key to remove
     * @return removed value, or {@code null}
     */
    @Override
    public Object remove(Object key) {
        buildIfNeeded();
        return map.remove(key);
    }

    /**
     * Hydrates the backing map if needed and copies all supplied entries into it.
     *
     * @param m entries to copy
     */
    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map m) {
        buildIfNeeded();
        map.putAll(m);
    }

    /**
     * Clears the compact buffers or hydrated map, depending on the current state.
     */
    @Override
    public void clear() {
        if (map == null) {
            size = 0;
        } else {
            map.clear();
        }
    }

    /**
     * Hydrates the backing map if needed and returns its key set.
     *
     * @return hydrated key set
     */
    @Override
    public Set<String> keySet() {
        buildIfNeeded();
        return map.keySet();
    }

    /**
     * Hydrates the backing map if needed and returns its values collection.
     *
     * @return hydrated values collection
     */
    @Override
    public Collection<Object> values() {
        buildIfNeeded();
        return map.values();
    }

    /**
     * Hydrates the backing map if needed and compares it with another object.
     *
     * @param o other object
     * @return {@code true} when equal
     */
    @Override
    public boolean equals(Object o) {
        buildIfNeeded();
        return map.equals(o);
    }

    /**
     * Hydrates the backing map if needed and returns its hash code.
     *
     * @return hydrated hash code
     */
    @Override
    public int hashCode() {
        buildIfNeeded();
        return map.hashCode();
    }

    /**
     * Hydrates the backing map if needed and returns its string form.
     *
     * @return hydrated map text
     */
    @Override
    public String toString() {
        buildIfNeeded();
        return map.toString();
    }

    /**
     * Clones the hydrated backing map when one already exists.
     *
     * @return a clone of the hydrated backing map, or {@code null} when still compact
     * @throws CloneNotSupportedException never thrown by the current implementation
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object clone() throws CloneNotSupportedException {
        if (map == null) {
            return null;
        } else {
            if (map instanceof LinkedHashMap) {
                return ((LinkedHashMap) map).clone();
            } else {
                return new LinkedHashMap(this);
            }
        }
    }

    /**
     * Copies the compact entries into a new {@code LazyMap} and clears this instance.
     *
     * @return a copy containing the current compact entries
     */
    public LazyMap clearAndCopy() {
        LazyMap map = new LazyMap();
        for (int index = 0; index < size; index++) {
            map.put(keys[index], values[index]);
        }
        size = 0;
        return map;
    }

    /**
     * Doubles the capacity of the supplied array while preserving its component type.
     *
     * @param array array to grow
     * @param <V> component type
     * @return the expanded array
     */
    @SuppressWarnings("unchecked")
    public static <V> V[] grow(V[] array) {
        Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length * 2);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return (V[]) newArray;
    }
}
