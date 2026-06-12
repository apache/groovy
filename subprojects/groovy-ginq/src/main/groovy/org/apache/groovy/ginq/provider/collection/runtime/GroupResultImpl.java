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
package org.apache.groovy.ginq.provider.collection.runtime;

import java.io.Serial;

/**
 * Default implementation of {@link GroupResult}.
 *
 * @param <K> the type of the group key
 * @param <T> the type of the grouped elements
 * @since 6.0.0
 */
class GroupResultImpl<K, T> extends QueryableCollection<T> implements GroupResult<K, T> {
    @Serial private static final long serialVersionUID = -4637595210702145661L;

    private final K key;

    /**
     * Creates a group result.
     *
     * @param key the group key
     * @param group the grouped elements
     */
    GroupResultImpl(K key, Queryable<T> group) {
        super(group.toList());
        this.key = key;
    }

    /**
     * Returns the group key, unwrapping single-value named keys.
     *
     * @return the group key
     */
    @SuppressWarnings("unchecked")
    @Override
    public K getKey() {
        // For single-key groupby, the classifier wraps the key in a NamedRecord;
        // unwrap it so g.key returns the raw value rather than a single-element tuple
        if (key instanceof NamedRecord && ((NamedRecord<?, ?>) key).size() == 1) {
            return (K) ((NamedRecord<?, ?>) key).get(0);
        }
        return key;
    }

    /**
     * Resolves a named key component for grouped results created with aliases.
     *
     * @param name the key component name
     * @return the named key component
     */
    @Override
    public Object get(String name) {
        if (key instanceof NamedRecord) {
            return ((NamedRecord<?, ?>) key).get(name);
        }
        throw new UnsupportedOperationException(
                "get(String) is only supported for groupby with named keys (using 'as' aliases). Use getKey() for single-key.");
    }
}
