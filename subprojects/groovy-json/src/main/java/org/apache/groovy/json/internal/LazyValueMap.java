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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.apache.groovy.json.internal.Exceptions.die;

/**
 * This class is important to the performance of the parser.
 * It stores Value objects in a map where they are evaluated lazily.
 * This is great for JSONPath types of application, and Object Serialization but not for maps that are going to be stored in a cache.
 * <p/>
 * This is because the Value construct is a type of index overlay that merely tracks where the token is located in the buffer,
 * and what if any thing we noted about it (like can be converted to a decimal number, etc.).
 * <p/>
 * To mitigate memory leaks this class along with CharSequenceValue implement two constructs, namely,
 * chop,  and lazyChop.
 * <p/>
 * A chop is when we convert backing buffer of a Value object into a smaller buffer.
 * A lazyChop is when we do a chop but only when a get operation is called.
 * <p/>
 * The lazyChop is performed on the tree that is touched by the JSONPath expression or its ilk.
 * <p/>
 * The chop operation can be done during parsing or lazily by storing the values in this construct.
 */
public class LazyValueMap extends AbstractMap<String, Object> implements ValueMap<String, Object> {

    /**
     * holds the map that gets lazily created on first access.
     */
    private Map<String, Object> map = null;
    /**
     * holds the list of items that we are managing.
     */
    private Entry<String, Value>[] items;
    /**
     * Holds the current number mapping managed by this map.
     */
    private int len = 0;
    /**
     * Holds whether or not we ae in lazy chop mode or not.
     */
    private final boolean lazyChop;

    /**
     * Keep track if this map has already been chopped so we don't waste time trying to chop it again.
     */
    boolean mapChopped = false;

    public LazyValueMap(boolean lazyChop) {
        this.items = new Entry[5];
        this.lazyChop = lazyChop;
    }

    public LazyValueMap(boolean lazyChop, int initialSize) {
        this.items = new Entry[initialSize];
        this.lazyChop = lazyChop;
    }

    /**
     * Adds a new MapItemValue to the mapping.
     *
     * @param miv miv we are adding.
     */
    public final void add(MapItemValue miv) {
        if (len >= items.length) {
            items = LazyMap.grow(items);
        }
        items[len] = miv;
        len++;
    }

    /**
     * Gets the item by key from the mapping.
     *
     * @param key to lookup
     * @return the item for the given key
     */
    public final Object get(Object key) {
        Object object = null;

        /* if the map is null, then we create it. */
        if (map == null) {
            buildMap();
        }
        object = map.get(key);

        lazyChopIfNeeded(object);
        return object;
    }

    /**
     * If in lazy chop mode, and the object is a Lazy Value Map or a ValueList
     * then we force a chop operation for each of its items.
     */
    private void lazyChopIfNeeded(Object object) {
        if (lazyChop) {
            if (object instanceof LazyValueMap) {
                LazyValueMap m = (LazyValueMap) object;
                m.chopMap();
            } else if (object instanceof ValueList) {
                ValueList list = (ValueList) object;
                list.chopList();
            }
        }
    }

    /**
     * Chop this map.
     */
    public final void chopMap() {
        /* if it has been chopped then you have to return. */
        if (mapChopped) {
            return;
        }
        mapChopped = true;

        /* If the internal map was not create yet, don't. We can chop the value w/o creating the internal map.*/
        if (this.map == null) {
            for (int index = 0; index < len; index++) {
                MapItemValue entry = (MapItemValue) items[index];

                Value value = entry.getValue();
                if (value == null) continue;
                if (value.isContainer()) {
                    chopContainer(value);
                } else {
                    value.chop();
                }
            }
        } else {
            /* Iterate through the map and do the same thing. Make sure children and children of children are chopped.  */
            for (Map.Entry<String, Object> entry : map.entrySet()) {

                Object object = entry.getValue();
                if (object instanceof Value) {
                    Value value = (Value) object;
                    if (value.isContainer()) {
                        chopContainer(value);
                    } else {
                        value.chop();
                    }
                } else if (object instanceof LazyValueMap) {
                    LazyValueMap m = (LazyValueMap) object;
                    m.chopMap();
                } else if (object instanceof ValueList) {
                    ValueList list = (ValueList) object;
                    list.chopList();
                }
            }
        }
    }

    /* We need to chop up this child container. */
    private static void chopContainer(Value value) {
        Object obj = value.toValue();
        if (obj instanceof LazyValueMap) {
            LazyValueMap map = (LazyValueMap) obj;
            map.chopMap();
        } else if (obj instanceof ValueList) {
            ValueList list = (ValueList) obj;
            list.chopList();
        }
    }

    public Value put(String key, Object value) {
        die("Not that kind of map");
        return null;
    }

    public Set<Entry<String, Object>> entrySet() {
        if (map == null) {
            buildMap();
        }
        return map.entrySet();
    }

    private void buildMap() {
        // added to avoid hash collision attack
        if (Sys.is1_8OrLater() || (Sys.is1_7() && LazyMap.JDK_MAP_ALTHASHING_SYSPROP != null)) {
            map = new HashMap<>(items.length);
        } else {
            map = new TreeMap<>();
        }

        for (Entry<String, Value> miv : items) {
            if (miv == null) {
                break;
            }
            map.put(miv.getKey(), miv.getValue().toValue());
        }

        len = 0;
        items = null;
    }

    public Collection<Object> values() {
        if (map == null) buildMap();
        return map.values();
    }

    public int size() {
        if (map == null) buildMap();
        return map.size();
    }

    public String toString() {
        if (map == null) buildMap();
        return map.toString();
    }

    public int len() {
        return len;
    }

    public boolean hydrated() {
        return map != null;
    }

    public Entry<String, Value>[] items() {
        return items;
    }
}
