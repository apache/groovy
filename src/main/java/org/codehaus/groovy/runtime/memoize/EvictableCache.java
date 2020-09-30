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
import java.util.Map;
import java.util.Set;

/**
 * Represents an evictable memoize cache with its essential methods
 * @param <K> type of the keys
 * @param <V> type of the values
 *
 * @since 2.5.0
 */
public interface EvictableCache<K, V> extends MemoizeCache<K, V>, Map<K, V>/* */ {
    /**
     * Remove the cached value by the key
     * @param key of the cached value
     * @return returns the removed value
     */
    @Override
    V remove(Object key);

    /**
     * Clear the cache
     * @return returns the content of the cleared map
     */
    Map<K, V> clearAll();

    /**
     * Clear the cache
     * @see #clearAll()
     */
    @Override
    default void clear() {
        clearAll();
    }

    /**
     * Get all cached values
     * @return all cached values
     */
    @Override
    Collection<V> values();

    /**
     * Get all keys associated to cached values
     * @return all keys
     */
    Set<K> keys();

    /**
     * Determines if the cache contains an entry for the specified key.
     * @param key key whose presence in this cache is to be tested.
     * @return true if the cache contains a mapping for the specified key
     */
    @Override
    boolean containsKey(Object key);

    /**
     * Get the size of the cache
     * @return the size of the cache
     */
    @Override
    int size();

    /**
     * Represents a eviction strategy for the cache with limited size
     */
    enum EvictionStrategy {
        /**
         * The oldest entry(i.e. the Less Recently Used entry) will be evicted
         */
        LRU,

        /**
         * Entries are evicted in the same order as they come in
         */
        FIFO
    }

    /**
     * Represents the action to deal with the cache
     *
     * @param <K> key type
     * @param <V> value type
     * @param <R> result type
     *
     * @since 3.0.0
     */
    @FunctionalInterface
    interface Action<K, V, R> {
        /**
         * Deal with the cache
         * @param evictableCache
         */
        R doWith(EvictableCache<K, V> evictableCache);
    }
}
