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
package org.apache.groovy.util.concurrent;

import java.util.EnumSet;

/**
 * This is a basic implementation of a map able to forget its keys could be weak/soft/strong references. This
 * bases on {@link ConcurrentReferenceHashMap}, thus it is safe for concurrency.
 * This map compares keys through references.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 4.0.0
 */
public class ManagedIdentityConcurrentMap<K, V> extends ConcurrentReferenceHashMap<K, V> {
    private static final long serialVersionUID = 1046734443288902802L;

    /**
     * Creates a new, empty map with the key weak reference
     */
    public ManagedIdentityConcurrentMap() {
        this(ReferenceType.WEAK);
    }

    /**
     * Creates a new, empty map with the key weak reference and the specified initial capacity
     *
     * @param initialCapacity initial capacity
     */
    public ManagedIdentityConcurrentMap(int initialCapacity) {
        this(ReferenceType.WEAK, initialCapacity);
    }

    /**
     * Creates a new, empty map with the specified key reference type
     *
     * @param keyType key reference type
     */
    public ManagedIdentityConcurrentMap(ReferenceType keyType) {
        super(keyType, ReferenceType.STRONG, EnumSet.of(Option.IDENTITY_COMPARISONS));
    }

    /**
     * Creates a new, empty map with the specified key reference type and initial capacity
     *
     * @param keyType key reference type
     * @param initialCapacity the initial capacity
     */
    public ManagedIdentityConcurrentMap(ReferenceType keyType, int initialCapacity) {
        super(initialCapacity, keyType, ReferenceType.STRONG, EnumSet.of(Option.IDENTITY_COMPARISONS));
    }

    /**
     * Get the key specified value, or put the default value and return it if the key is absent
     *
     * @param key the key to look up
     * @param value the default value if the key is absent
     * @return the value
     */
    public V getOrPut(K key, V value) {
        return this.applyIfAbsent(key, k -> value);
    }
}
