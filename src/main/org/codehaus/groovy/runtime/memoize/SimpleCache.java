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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * Represents a simple cache, which is thread safe and backed by {@link java.util.HashMap}
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 *
 * @since 2.5.0
 */
public class SimpleCache<K, V> implements EvictableCache<K, V> {
    private final Map<K, V> map;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwl.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwl.writeLock();

    public SimpleCache() {
        this(new HashMap<>());
    }

    public SimpleCache(Map<K, V> map) {
        this.map = map;
    }

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

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return map.put(key, value);
        } finally {
            writeLock.unlock();
        }

    }

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

    @Override
    public Collection<V> values() {
        readLock.lock();
        try {
            return map.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<K> keys() {
        readLock.lock();
        try {
            return map.keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V remove(K key) {
        writeLock.lock();
        try {
            return map.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

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

    @Override
    public void cleanUpNullReferences() {
        writeLock.lock();
        try {
            List<K> keys = new LinkedList<>();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (null == entry.getValue()) {
                    keys.add(entry.getKey());
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
