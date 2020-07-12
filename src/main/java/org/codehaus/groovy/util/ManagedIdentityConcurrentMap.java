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
package org.codehaus.groovy.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a basic implementation of a map able to forget its keys. This
 * map uses internally a ConcurrentHashMap, thus should be safe for concurrency.
 * hashcode and equals are used to find the entries and should thus be implemented
 * properly for the keys. This map does not support null keys.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 4.0.0
 */
public class ManagedIdentityConcurrentMap<K, V> {
    private final ConcurrentHashMap<ManagedReference<K>, V> internalMap;
    private final ReferenceBundle bundle;

    public ManagedIdentityConcurrentMap(ReferenceBundle bundle) {
        this.internalMap = new ConcurrentHashMap<>();
        this.bundle = bundle;
    }

    /**
     * Returns the value stored for the given key at the point of call.
     *
     * @param key a non null key
     * @return the value stored in the map for the given key
     */
    public V get(K key) {
        return internalMap.get(new ManagedIdentityKey<K>(key));
    }

    /**
     * Sets a new value for a given key. an older value is overwritten.
     *
     * @param key   a non null key
     * @param value the new value
     */
    public V put(final K key, V value) {
        return internalMap.put(new ManagedIdentityKey<K>(key), value);
    }

    /**
     * Returns the values of the map
     */
    public Collection<V> values() {
        return internalMap.values();
    }

    /**
     * Remove the key specified entry
     *
     * @param key the key to look up
     * @return the removed value
     */
    public V remove(K key) {
        return internalMap.remove(new ManagedIdentityKey<K>(key));
    }

    /**
     * Get the key specified value, or put the default value and return it if the key is absent
     *
     * @param key the key to look up
     * @param value the default value if the key is absent
     * @return the value
     */
    public V getOrPut(K key, V value) {
        return internalMap.computeIfAbsent(new ManagedIdentityKey<K>(key), k -> value);
    }

    /**
     * Returns the map size
     */
    public Object size() {
        return internalMap.size();
    }

    /**
     * Check if the map is empty or not
     *
     * @return {@code true} when the map is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    /**
     * Represents identity key of {@link ManagedIdentityConcurrentMap}
     *
     * @param <K> the key type
     */
    private class ManagedIdentityKey<K> extends ManagedReference<K> {
        private final int hashCode;

        private ManagedIdentityKey(K key) {
            super(bundle, key);
            this.hashCode = hash(key);
        }

        @Override
        public void finalizeReference() {
            internalMap.remove(this);
            super.finalizeReference();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ManagedIdentityKey)) return false;
            return get() == ((ManagedIdentityKey<?>) o).get();
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        private int hash(K key) {
            int h = (null == key) ? 0 : System.identityHashCode(key);
            h += (h << 15) ^ 0xffffcd7d;
            h ^= (h >>> 10);
            h += (h << 3);
            h ^= (h >>> 6);
            h += (h << 2) + (h << 14);
            h ^= (h >>> 16);
            return h;
        }
    }
}
