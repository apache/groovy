/*
 * Copyright 2003-2014 the original author or authors.
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
 *
 * Derived from Boon all rights granted to Groovy project for this fork.
 */
package groovy.json.internal;

import java.lang.reflect.Array;
import java.util.*;

/**
 * This maps only builds once you ask for a key for the first time.
 * It is designed to not incur the overhead of creating a map unless needed.
 *
 * @author Rick Hightower
 */
public class LazyMap extends AbstractMap<String, Object> {

    /* Holds the actual map that will be lazily created. */
    private Map<String, Object> map;
    /* The size of the map. */
    private int size;
    /* The keys  stored in the map. */
    private String[] keys;
    /* The values stored in the map. */
    private Object[] values;

    public LazyMap() {
        keys = new String[5];
        values = new Object[5];
    }

    public LazyMap(int initialSize) {
        keys = new String[initialSize];
        values = new Object[initialSize];

    }

    public Object put(String key, Object value) {
        if (map == null) {
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

    public Set<Entry<String, Object>> entrySet() {
        buildIfNeeded();
        return map.entrySet();
    }

    public int size() {
        if (map == null) {
            return size;
        } else {
            return map.size();
        }
    }

    public boolean isEmpty() {
        if (map == null) {
            return size == 0;
        } else {
            return map.isEmpty();
        }
    }

    public boolean containsValue(Object value) {
        buildIfNeeded();
        return map.containsValue(value);
    }

    public boolean containsKey(Object key) {
        buildIfNeeded();
        return map.containsKey(key);
    }

    public Object get(Object key) {
        buildIfNeeded();
        return map.get(key);
    }

    private void buildIfNeeded() {
        if (map == null) {

            /** added to avoid hash collision attack. */
            if (Sys.is1_7OrLater() && System.getProperty("jdk.map.althashing.threshold") != null) {
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

    public Object remove(Object key) {
        buildIfNeeded();
        return map.remove(key);
    }

    public void putAll(Map m) {
        buildIfNeeded();
        map.putAll(m);
    }

    public void clear() {
        if (map == null) {
            size = 0;
        } else {
            map.clear();
        }
    }

    public Set<String> keySet() {
        buildIfNeeded();
        return map.keySet();

    }

    public Collection<Object> values() {

        buildIfNeeded();

        return map.values();

    }

    public boolean equals(Object o) {
        buildIfNeeded();
        return map.equals(o);
    }

    public int hashCode() {
        buildIfNeeded();
        return map.hashCode();
    }

    public String toString() {

        buildIfNeeded();
        return map.toString();
    }

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

    public LazyMap clearAndCopy() {
        LazyMap map = new LazyMap();
        for (int index = 0; index < size; index++) {
            map.put(keys[index], values[index]);
        }
        size = 0;
        return map;
    }

    public static <V> V[] grow(V[] array) {
        Object newArray = Array.newInstance(array.getClass().getComponentType(),
                array.length * 2);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return (V[]) newArray;
    }

}

