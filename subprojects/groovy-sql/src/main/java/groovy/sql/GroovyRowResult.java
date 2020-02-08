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
package groovy.sql;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents an extent of objects.
 * It's primarily used by methods of Groovy's {@link groovy.sql.Sql} class to return {@code ResultSet} data in map
 * form; allowing access to the result of a SQL query by the name of the column, or by the column number.
 */
public class GroovyRowResult extends GroovyObjectSupport implements Map<String, Object> {

    private final Map<String, Object> result;

    public GroovyRowResult(Map<String, Object> result) {
        this.result = result;
    }

    /**
     * Retrieve the value of the property by its (case-insensitive) name.
     *
     * @param property is the name of the property to look at
     * @return the value of the property
     */
    public Object getProperty(String property) {
        try {
            Object key = lookupKeyIgnoringCase(property);
            if (key != null) {
                return result.get(key);
            }
            throw new MissingPropertyException(property, GroovyRowResult.class);
        }
        catch (Exception e) {
            throw new MissingPropertyException(property, GroovyRowResult.class, e);
        }
    }

    private Object lookupKeyIgnoringCase(Object key) {
        // try some special cases first for efficiency
        if (result.containsKey(key))
            return key;
        if (!(key instanceof CharSequence))
            return null;
        String keyStr = key.toString();
        for (Object next : result.keySet()) {
            if (!(next instanceof String))
                continue;
            if (keyStr.equalsIgnoreCase((String)next))
                return next;
        }
        return null;
    }

    /**
     * Retrieve the value of the property by its index.
     * A negative index will count backwards from the last column.
     *
     * @param index is the number of the column to look at
     * @return the value of the property
     */
    public Object getAt(int index) {
        try {
            // a negative index will count backwards from the last column.
            if (index < 0)
                index += result.size();
            Iterator<Object> it = result.values().iterator();
            int i = 0;
            Object obj = null;
            while ((obj == null) && (it.hasNext())) {
                if (i == index)
                    obj = it.next();
                else
                    it.next();
                i++;
            }
            return obj;
        }
        catch (Exception e) {
            throw new MissingPropertyException(Integer.toString(index), GroovyRowResult.class, e);
        }
    }

    public String toString() {
        return result.toString();
    }

    /*
     * The following methods are needed for implementing the Map interface.
     * They are mostly delegating the request to the provided Map.
     */
     
    public void clear() {
        result.clear();
    }

    /**
     * Checks if the result contains (ignoring case) the given key.
     *
     * @param key the property name to look for
     * @return true if the result contains this property name
     */
    public boolean containsKey(Object key) {
        return lookupKeyIgnoringCase(key) != null;
    }

    public boolean containsValue(Object value) {
        return result.containsValue(value);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return result.entrySet();
    }

    public boolean equals(Object o) {
        return result.equals(o);
    }

    /**
     * Find the property value for the given name (ignoring case).
     *
     * @param property the name of the property to get
     * @return the property value
     */
    public Object get(Object property) {
        if (property instanceof String)
            return getProperty((String)property);
        return null;
    }

    public int hashCode() {
        return result.hashCode();
    }

    public boolean isEmpty() {
        return result.isEmpty();
    }

    public Set<String> keySet() {
        return result.keySet();
    }

    /**
     * Associates the specified value with the specified property name in this result.
     *
     * @param key the property name for the result
     * @param value the property value for the result
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public Object put(String key, Object value) {
        // avoid different case keys being added by explicit remove
        Object orig = remove(key);
        result.put(key, value);
        return orig;
    }

    /**
     * Copies all of the mappings from the specified map to this result.
     * If the map contains different case versions of the same (case-insensitive) key
     * only the last (according to the natural ordering of the supplied map) will remain
     * after the {@code putAll} method has returned.
     *
     * @param t the mappings to store in this result
     */
    public void putAll(Map<? extends String, ?> t) {
        // don't delegate to putAll since we want case handling from put
        for (Entry<? extends String, ?> next : t.entrySet()) {
            put(next.getKey(), next.getValue());
        }
    }

    public Object remove(Object rawKey) {
        return result.remove(lookupKeyIgnoringCase(rawKey));
    }

    public int size() {
        return result.size();
    }

    public Collection<Object> values() {
        return result.values();
    }
}
