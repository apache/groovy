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

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a simple key-value cache, which is NOT thread safe and backed by a {@link java.util.Map} instance
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 * @since 2.5.0
 */
public class CommonCache<K, V> implements EvictableCache<K, V>, ValueConvertable<V, Object>, Serializable {
    private static final long serialVersionUID = 934699400232698324L;
    /**
     * The default load factor
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * The default initial capacity
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<K, V> map;

    /**
     * Constructs a cache with unlimited size
     */
    public CommonCache() {
        this(new LinkedHashMap<>());
    }

    /**
     * Constructs a cache with limited size
     *
     * @param initialCapacity  initial capacity of the cache
     * @param maxSize          max size of the cache
     * @param evictionStrategy LRU or FIFO, see {@link org.codehaus.groovy.runtime.memoize.EvictableCache.EvictionStrategy}
     */
    public CommonCache(final int initialCapacity, final int maxSize, final EvictionStrategy evictionStrategy) {
        this(new LinkedHashMap<K, V>(initialCapacity, DEFAULT_LOAD_FACTOR, EvictionStrategy.LRU == evictionStrategy) {
            private static final long serialVersionUID = -8012450791479726621L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }

    /**
     * Constructs a LRU cache with the specified initial capacity and max size.
     * The LRU cache is slower than {@link LRUCache}
     *
     * @param initialCapacity initial capacity of the LRU cache
     * @param maxSize         max size of the LRU cache
     */
    public CommonCache(int initialCapacity, int maxSize) {
        this(initialCapacity, maxSize, EvictionStrategy.LRU);
    }

    /**
     * Constructs a LRU cache with the default initial capacity
     *
     * @param maxSize max size of the LRU cache
     * @see #CommonCache(int, int)
     */
    public CommonCache(final int maxSize) {
        this(DEFAULT_INITIAL_CAPACITY, maxSize);
    }

    /**
     * Constructs a cache backed by the specified {@link java.util.Map} instance
     *
     * @param map the {@link java.util.Map} instance
     */
    public CommonCache(Map<K, V> map) {
        this.map = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider) {
        return getAndPut(key, valueProvider, true);
    }

    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider, boolean shouldCache) {
        V value = get(key);
        if (null != convertValue(value)) {
            return value;
        }

        value = null == valueProvider ? null : valueProvider.provide(key);
        if (shouldCache && null != convertValue(value)) {
            put(key, value);
        }

        return value;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Set<K> keys() {
        return map.keySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Map<K, V> clearAll() {
        Map<K, V> result = new LinkedHashMap<>(map);
        map.clear();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUpNullReferences() {
        List<K> keys = new LinkedList<>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (null == value
                    || (value instanceof SoftReference && null == ((SoftReference) value).get())
                    || (value instanceof WeakReference && null == ((WeakReference) value).get())) {
                keys.add(key);
            }
        }

        for (K key : keys) {
            map.remove(key);
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValue(V value) {
        return value;
    }
}
