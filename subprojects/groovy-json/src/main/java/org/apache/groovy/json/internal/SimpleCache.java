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

import org.codehaus.groovy.runtime.memoize.CommonCache;
import org.codehaus.groovy.runtime.memoize.EvictableCache;

public class SimpleCache<K, V> implements Cache<K, V> {
    private EvictableCache<K, V> cache;

    public SimpleCache(final int limit, CacheType type) {
        if (type.equals(CacheType.LRU)) {
            cache = new CommonCache<K, V>(limit);
        } else {
            cache = new CommonCache<K, V>(CommonCache.DEFAULT_INITIAL_CAPACITY, limit, EvictableCache.EvictionStrategy.FIFO);
        }
    }

    public SimpleCache(final int limit) {
        this(limit, CacheType.LRU);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    //For testing only

    @Override
    public V getSilent(K key) {
        V value = cache.get(key);
        if (value != null) {
            cache.remove(key);
            cache.put(key, value);
        }
        return value;
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    public String toString() {
        return cache.toString();
    }
}
