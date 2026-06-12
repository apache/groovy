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

/**
 * Cache
 *
 * @param <KEY>   key
 * @param <VALUE> value
 */
public interface Cache<KEY, VALUE> {

    /**
     * Stores a value under the supplied key.
     *
     * @param key the cache key
     * @param value the cached value
     */
    void put(KEY key, VALUE value);

    /**
     * Retrieves a value and updates cache state as needed.
     *
     * @param key the cache key
     * @return the cached value, or {@code null} if absent
     */
    VALUE get(KEY key);

    /**
     * Retrieves a value using implementation-specific silent access semantics.
     *
     * @param key the cache key
     * @return the cached value, or {@code null} if absent
     */
    VALUE getSilent(KEY key);

    /**
     * Removes the value stored for the supplied key.
     *
     * @param key the cache key
     */
    void remove(KEY key);

    /**
     * Returns the current number of cached entries.
     *
     * @return the cache size
     */
    int size();
}
