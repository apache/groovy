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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

/**
 * Represents a simple key-value cache, which is thread safe and backed by a {@link Map} instance.
 * StampedCommonCache has better performance than {@link ConcurrentCommonCache},
 * but it is not reentrant, in other words, <b>it may cause deadlock</b> if {@link #getAndPut(Object, MemoizeCache.ValueProvider)}
 * or {@link #getAndPut(Object, MemoizeCache.ValueProvider, boolean)} is called recursively:
 * readlock -> upgrade to writelock -> readlock (fails to get and waits forever)
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 * @since 3.0.0
 */
@ThreadSafe
public class StampedCommonCache<K, V> implements EvictableCache<K, V>, ValueConvertable<V, Object>, Serializable {

    private static final long serialVersionUID = 6760742552334555146L;
    private final StampedLock sl = new StampedLock();
    private final CommonCache<K, V> commonCache;

    /**
     * Constructs a cache with unlimited size
     */
    public StampedCommonCache() {
        commonCache = new CommonCache<K, V>();
    }

    /**
     * Constructs a cache with limited size
     *
     * @param initialCapacity  initial capacity of the cache
     * @param maxSize          max size of the cache
     * @param evictionStrategy LRU or FIFO, see {@link EvictableCache.EvictionStrategy}
     */
    public StampedCommonCache(int initialCapacity, int maxSize, EvictionStrategy evictionStrategy) {
        commonCache = new CommonCache<K, V>(initialCapacity, maxSize, evictionStrategy);
    }

    /**
     * Constructs a LRU cache with the specified initial capacity and max size.
     * The LRU cache is slower than {@link LRUCache}
     *
     * @param initialCapacity initial capacity of the LRU cache
     * @param maxSize         max size of the LRU cache
     */
    public StampedCommonCache(int initialCapacity, int maxSize) {
        commonCache = new CommonCache<K, V>(initialCapacity, maxSize);
    }

    /**
     * Constructs a LRU cache with the default initial capacity(16)
     *
     * @param maxSize max size of the LRU cache
     * @see #StampedCommonCache(int, int)
     */
    public StampedCommonCache(int maxSize) {
        commonCache = new CommonCache<K, V>(maxSize);
    }

    /**
     * Constructs a cache backed by the specified {@link Map} instance
     *
     * @param map the {@link Map} instance
     */
    public StampedCommonCache(Map<K, V> map) {
        commonCache = new CommonCache<K, V>(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(final Object key) {
        return doWithReadLock(c -> c.get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(final K key, final V value) {
        return doWithWriteLock(c -> c.put(key, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider) {
        return getAndPut(key, valueProvider, true);
    }

    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider, boolean shouldCache) {
        V value;

        // try optimistic read first, which is non-blocking
        long optimisticReadStamp = sl.tryOptimisticRead();
        value = commonCache.get(key);
        if (sl.validate(optimisticReadStamp)) {
            if (null != convertValue(value)) {
                return value;
            }
        }

        long stamp = sl.readLock();
        try {
            // if stale, read again
            if (!sl.validate(optimisticReadStamp)) {
                value = commonCache.get(key);
                if (null != convertValue(value)) {
                    return value;
                }
            }

            long ws = sl.tryConvertToWriteLock(stamp); // the new local variable `ws` is necessary here!
            if (0L == ws) { // Failed to convert read lock to write lock
                sl.unlockRead(stamp);
                stamp = sl.writeLock();

                // try to read again
                value = commonCache.get(key);
                if (null != convertValue(value)) {
                    return value;
                }
            } else {
                stamp = ws;
            }

            value = compute(key, valueProvider, shouldCache);
        } finally {
            sl.unlock(stamp);
        }

        return value;
    }

    private V compute(K key, ValueProvider<? super K, ? extends V> valueProvider, boolean shouldCache) {
        V value = null == valueProvider ? null : valueProvider.provide(key);
        if (shouldCache && null != convertValue(value)) {
            commonCache.put(key, value);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        return doWithReadLock(c -> c.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return doWithReadLock(c -> c.entrySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keys() {
        return doWithReadLock(c -> c.keys());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        return doWithReadLock(c -> c.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return doWithReadLock(c -> c.containsValue(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return doWithReadLock(c -> c.size());
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key) {
        return doWithWriteLock(c -> c.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        doWithWriteLock(c -> {
            c.putAll(m);
            return null;
        });
    }

    @Override
    public Set<K> keySet() {
        return keys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<K, V> clearAll() {
        return doWithWriteLock(c -> c.clearAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUpNullReferences() {
        doWithWriteLock(c -> {
            c.cleanUpNullReferences();
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValue(V value) {
        return value;
    }

    /**
     * deal with the backed cache guarded by write lock
     * @param action the content to complete
     */
    private <R> R doWithWriteLock(Action<K, V, R> action) {
        long stamp = sl.writeLock();
        try {
            return action.doWith(commonCache);
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    /**
     * deal with the backed cache guarded by read lock
     * @param action the content to complete
     */
    private <R> R doWithReadLock(Action<K, V, R> action) {
        long stamp = sl.tryOptimisticRead();
        R result = action.doWith(commonCache);

        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                result = action.doWith(commonCache);
            } finally {
                sl.unlockRead(stamp);
            }
        }

        return result;
    }
}
