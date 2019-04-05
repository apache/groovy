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
package org.codehaus.groovy.runtime.memoize;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache backed by a ConcurrentHashMap
 */
@ThreadSafe
public final class UnlimitedConcurrentCache<K, V> implements EvictableCache<K, V>, Serializable {
    private static final long serialVersionUID = -857220494475488328L;
    private final ConcurrentHashMap<K, V> map;

    /**
     * Constructs a cache with unlimited size
     */
    public UnlimitedConcurrentCache() {
        map = new ConcurrentHashMap<K, V>();
    }

    /**
     * Constructs a cache with unlimited size and set its initial capacity
     * @param initialCapacity the initial capacity
     */
    public UnlimitedConcurrentCache(int initialCapacity) {
        map = new ConcurrentHashMap<K, V>(initialCapacity);
    }

    /**
     * Constructs a cache and initialize the cache with the specified map
     * @param m the map to initialize the cache
     */
    public UnlimitedConcurrentCache(Map<? extends K, ? extends V> m) {
        this();
        map.putAll(m);
    }

    /**
     * Remove the cached value by the key
     *
     * @param key
     * @return returns the removed value
     */
    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Clear the cache
     *
     * @return returns the content of the cleared map
     */
    @Override
    public Map<K, V> clearAll() {
        Map<K, V> result = new LinkedHashMap<K, V>(map.size());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            boolean removed = map.remove(key, value);

            if (removed) {
                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * Get all cached values
     *
     * @return all cached values
     */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Get all keys associated to cached values
     *
     * @return all keys
     */
    @Override
    public Set<K> keys() {
        return map.keySet();
    }

    /**
     * Determines if the cache contains an entry for the specified key.
     *
     * @param key key whose presence in this cache is to be tested.
     * @return true if the cache contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Get the size of the cache
     *
     * @return the size of the cache
     */
    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Associates the specified value with the specified key in the cache.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return null, or the old value if the key associated with the specified key.
     */
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * Gets a value from the cache
     *
     * @param key the key whose associated value is to be returned
     * @return the value, or null, if it does not exist.
     */
    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * Try to get the value from cache.
     * If not found, create the value by {@link MemoizeCache.ValueProvider} and put it into the cache, at last return the value.
     *
     * @param key
     * @param valueProvider provide the value if the associated value not found
     * @return the cached value
     */
    @Override
    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider) {
        return map.computeIfAbsent(key, valueProvider::provide);
    }

    /**
     * Replying on the ConcurrentHashMap thread-safe iteration implementation the method will remove all entries holding
     * SoftReferences to gc-evicted objects.
     */
    @Override
    public void cleanUpNullReferences() {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            Object entryVal = entry.getValue();
            if (entryVal instanceof SoftReference && ((SoftReference) entryVal).get() == null) {
                map.remove(entry.getKey(), entryVal);
            }
        }
    }
}
