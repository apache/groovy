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

import static org.apache.groovy.json.internal.Exceptions.die;

/**
 * This map is for object serialization mainly.
 * The idea is the final conversion of
 * the Value objects are delayed until the last possible moment, i.e., just before injected into a bean.
 */
public class ValueMapImpl extends AbstractMap<String, Value> implements ValueMap<String, Value> {

    /**
     * The internal map to hold the Value map.
     */
    private Map<String, Value> map = null;

    /**
     * The items held in the map.
     */
    private Entry<String, Value>[] items = new Entry[20];

    /* The current length of the map. */
    private int len = 0;

    /**
     * Add a MapItemValue to the map.
     *
     * @param miv map value item.
     */

    public void add(MapItemValue miv) {
        if (len >= items.length) {
            items = LazyMap.grow(items);
        }
        items[len] = miv;
        len++;
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

    /**
     * Get the items for the key.
     *
     * @param key
     * @return the items for the given key
     */

    public Value get(Object key) {
        /* If the length is under and we are asking for the key, then just look for the key. Don't build the map. */
        if (map == null && items.length < 20) {
            for (Object item : items) {
                MapItemValue miv = (MapItemValue) item;
                if (key.equals(miv.name.toValue())) {
                    return miv.value;
                }
            }
            return null;
        } else {
            if (map == null) buildIfNeededMap();
            return map.get(key);
        }
    }

    public Value put(String key, Value value) {
        die("Not that kind of map");
        return null;
    }

    /**
     * If the map has not been built yet, then we just return a fake entry set.
     */

    public Set<Entry<String, Value>> entrySet() {
        buildIfNeededMap();
        return map.entrySet();
    }

    /**
     * Build the map if requested to, it does this lazily.
     */
    private void buildIfNeededMap() {
        if (map == null) {
            map = new HashMap<>(items.length);

            for (Entry<String, Value> miv : items) {
                if (miv == null) {
                    break;
                }
                map.put(miv.getKey(), miv.getValue());
            }
        }
    }

    /**
     * Return a collection of values.
     */
    public Collection<Value> values() {
        this.buildIfNeededMap();
        return map.values();
    }

    /**
     * Return the size of the map. Use the map if it has already been created.
     *
     * @return size
     */
    public int size() {
        this.buildIfNeededMap();
        return map.size();
    }
}
