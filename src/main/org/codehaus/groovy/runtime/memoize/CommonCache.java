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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * Represents a simple key-value cache, which is thread safe and backed by a {@link java.util.Map} instance
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 *
 * @since 2.5.0
 */
public class CommonCache<K, V> implements EvictableCache<K, V> {
    private final Map<K, V> map;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwl.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwl.writeLock();

    /**
     * Constructs a cache with unlimited size
     */
    public CommonCache() {
        this(new LinkedHashMap<K, V>());
    }

    /**
     * Constructs a cache with limited size
     * @param initialCapacity initial capacity of the LRU cache
     * @param maxSize max size of the LRU cache
     * @param accessOrder the ordering mode - <tt>true</tt> for access-order, <tt>false</tt> for insertion-order, see the parameter accessOrder of {@link LinkedHashMap#LinkedHashMap(int, float, boolean)}
     */
    public CommonCache(final int initialCapacity, final int maxSize, final boolean accessOrder) {
        this(new LinkedHashMap<K, V>(initialCapacity, 0.75f, accessOrder) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }

    /**
     * Constructs a LRU cache with the specified initial capacity and max size.
     * The LRU cache is slower than {@link LRUCache} but will not put same value multi-times concurrently
     * @param initialCapacity initial capacity of the LRU cache
     * @param maxSize max size of the LRU cache
     */
    public CommonCache(final int initialCapacity, final int maxSize) {
        this(initialCapacity, maxSize, true);
    }

    /**
     * Constructs a LRU cache with the default initial capacity(16)
     * @param maxSize max size of the LRU cache
     * @see #CommonCache(int, int)
     */
    public CommonCache(final int maxSize) {
        this(16, maxSize);
    }

    /**
     * Constructs a cache backed by the specified {@link java.util.Map} instance
     * @param map the {@link java.util.Map} instance
     */
    public CommonCache(Map<K, V> map) {
        this.map = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key) {
        if (null == key) {
            return null;
        }

        readLock.lock();
        try {
            return map.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return map.put(key, value);
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndPut(K key, ValueProvider<K, V> valueProvider) {
        return getAndPut(key, valueProvider, true);
    }

    public V getAndPut(K key, ValueProvider<K, V> valueProvider, boolean shouldCache) {
        if (null == key) {
            return null;
        }

        V value;

        readLock.lock();
        try {
            value = map.get(key);
            if (null != value) {
                return value;
            }
        } finally {
            readLock.unlock();
        }

        writeLock.lock();
        try {
            // try to find the cached value again
            value = map.get(key);
            if (null != value) {
                return value;
            }

            value = null == valueProvider ? null : valueProvider.provide(key);
            if (shouldCache && null != value) {
                map.put(key, value);
            }
        } finally {
            writeLock.unlock();
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        readLock.lock();
        try {
            return map.values();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keys() {
        readLock.lock();
        try {
            return map.keySet();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            return map.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(K key) {
        writeLock.lock();
        try {
            return map.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> clear() {
        Collection<V> values;

        writeLock.lock();
        try {
            values = map.values();
            map.clear();
        } finally {
            writeLock.unlock();
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUpNullReferences() {
        writeLock.lock();
        try {
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
        } finally {
            writeLock.unlock();
        }
    }
}
