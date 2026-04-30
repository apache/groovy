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
package groovy.lang;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serial;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper to turn a list with an even number of elements into a Map.
 */
public class SpreadMap extends HashMap {
    @Serial private static final long serialVersionUID = 3827653766235954251L;
    private int hashCode;

    /**
     * Creates an immutable spread map from alternating key/value entries.
     *
     * @param values alternating keys and values
     */
    public SpreadMap(Object[] values) {
        int i = 0;
        while (i < values.length) {
            super.put(values[i++], values[i++]);
        }
    }

    /**
     * Creates an immutable spread map from an existing map.
     *
     * @param map the source map
     */
    public SpreadMap(Map map) {
        super(map);
    }

    /**
     * @since 1.8.0
     * @param list the list to make spreadable
     */
    public SpreadMap(List list) {
        this(list.toArray());
    }

    /**
     * Always throws because spread maps are immutable.
     *
     * @param key the key to add
     * @param value the value to add
     * @return never returns normally
     */
    @Override
    public Object put(Object key, Object value) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so ("
                                   + key + ": " + value + ") cannot be added.");
    }

    /**
     * Always throws because spread maps are immutable.
     *
     * @param key the key to remove
     * @return never returns normally
     */
    @Override
    public Object remove(Object key) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the key ("
                                   + key + ") cannot be deleted.");
    }

    /**
     * Always throws because spread maps are immutable.
     *
     * @param t the entries to add
     */
    @Override
    public void putAll(Map t) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the map ("
                                   + t + ") cannot be put in this spreadMap.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof SpreadMap) {
            return equals((SpreadMap) that);
        }
        return false;
    }

    /**
     * Compares this spread map with another spread map using Groovy equality.
     *
     * @param that the other spread map
     * @return {@code true} if the maps are equal
     */
    public boolean equals(SpreadMap that) {
        if (that == null) return false;

        if (size() == that.size()) {
            for (Object e : entrySet()) {
                Map.Entry entry = (Map.Entry) e;
                Object key = entry.getKey();
                if (!DefaultTypeTransformation.compareEqual(entry.getValue(), that.get(key))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            for (Object key : keySet()) {
                int hash = (key != null) ? key.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
        }
        return hashCode;
    }

    /**
     * @return the string expression of <code>this</code>
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "*:[:]";
        }
        StringBuilder sb = new StringBuilder("*:[");
        Iterator iter = keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            sb.append(key).append(":").append(get(key));
            if (iter.hasNext())
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
