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
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache backed by a ConcurrentHashMap
 *
 * @author Vaclav Pech
 */
public final class UnlimitedConcurrentCache<K, V> implements MemoizeCache<K, V> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();

    public V put(final K key, final V value) {
        return map.put(key, value);
    }

    public V get(final K key) {
        return map.get(key);
    }

    /**
     * The implementation of `getAndPut` is not atomic
     */
    @Override
    public V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider) {
        V value = this.get(key);

        if (null == value) {
            value = valueProvider.provide(key);
            this.put(key, value);
        }

        return value;
    }

    /**
     * Replying on the ConcurrentHashMap thread-safe iteration implementation the method will remove all entries holding
     * SoftReferences to gc-evicted objects.
     */
    public void cleanUpNullReferences() {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            Object entryVal = entry.getValue();
            if (entryVal instanceof SoftReference && ((SoftReference) entryVal).get() == null) {
                map.remove(entry.getKey(), entryVal);
            }
        }
    }
}
