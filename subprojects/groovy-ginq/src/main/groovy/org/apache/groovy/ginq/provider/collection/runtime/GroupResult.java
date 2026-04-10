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

import groovy.transform.Internal;

/**
 * Represents a group result from a {@code groupby...into} clause.
 * Extends {@link Queryable} to provide aggregate and query methods
 * on the group's elements, with a {@code key} property for accessing
 * the group key.
 *
 * @param <K> the type of the group key
 * @param <T> the type of the grouped elements
 * @since 6.0.0
 */
@Internal
public interface GroupResult<K, T> extends Queryable<T> {

    /**
     * Returns the group key.
     * For single-key groupby, this is the raw key value.
     * For multi-key groupby, this is a {@link NamedRecord} with named access.
     *
     * @return the group key
     */
    K getKey();

    /**
     * Returns a named component of the group key.
     * Enables {@code g.name} property-style access and {@code g.get("name")} calls.
     * For multi-key groupby, looks up the named component in the key record.
     *
     * @param name the key component name (from {@code as} alias in groupby)
     * @return the value of the named key component
     * @throws UnsupportedOperationException if this is a single-key group without aliases
     */
    Object get(String name);

    /**
     * Subscript operator for accessing named key components.
     * Enables {@code g["name"]} syntax.
     *
     * @param name the key component name
     * @return the value of the named key component
     */
    default Object getAt(String name) {
        return get(name);
    }

    /**
     * Factory method to create a {@link GroupResult} instance.
     *
     * @param key the group key
     * @param group the grouped elements as a Queryable
     * @param <K> the type of the group key
     * @param <T> the type of the grouped elements
     * @return a new GroupResult
     */
    static <K, T> GroupResult<K, T> of(K key, Queryable<T> group) {
        return new GroupResultImpl<>(key, group);
    }
}
