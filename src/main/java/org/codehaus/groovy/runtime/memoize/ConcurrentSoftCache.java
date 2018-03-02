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
import java.util.Map;

/**
 * Represents concurrent cache holding SoftReference instance as value
 *
 * @param <K> key type
 * @param <V> real value type
 */
public class ConcurrentSoftCache<K, V> extends ConcurrentCommonCache<K, SoftReference<V>> {
    private static final long serialVersionUID = 5646536868666351819L;


    /**
     * Constructs a cache with unlimited size
     */
    public ConcurrentSoftCache() {
        super();
    }

    /**
     * Constructs a cache with limited size
     *
     * @param initialCapacity  initial capacity of the cache
     * @param maxSize          max size of the cache
     * @param evictionStrategy LRU or FIFO, see {@link org.codehaus.groovy.runtime.memoize.EvictableCache.EvictionStrategy}
     */
    public ConcurrentSoftCache(int initialCapacity, int maxSize, EvictionStrategy evictionStrategy) {
        super(initialCapacity, maxSize, evictionStrategy);
    }

    /**
     * Constructs a LRU cache with the specified initial capacity and max size.
     * The LRU cache is slower than {@link LRUCache}
     *
     * @param initialCapacity initial capacity of the LRU cache
     * @param maxSize         max size of the LRU cache
     */
    public ConcurrentSoftCache(int initialCapacity, int maxSize) {
        super(initialCapacity, maxSize);
    }

    /**
     * Constructs a LRU cache with the default initial capacity(16)
     *
     * @param maxSize max size of the LRU cache
     * @see #ConcurrentSoftCache(int, int)
     */
    public ConcurrentSoftCache(int maxSize) {
        super(maxSize);
    }

    /**
     * Constructs a cache backed by the specified {@link java.util.Map} instance
     *
     * @param map the {@link java.util.Map} instance
     */
    public ConcurrentSoftCache(Map<K, SoftReference<V>> map) {
        super(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValue(SoftReference<V> value) {
        if (null == value) {
            return null;
        }

        return value.get();
    }
}
