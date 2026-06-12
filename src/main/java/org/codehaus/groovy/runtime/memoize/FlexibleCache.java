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

/**
 * An EvictableCache supporting a getAndPut variant with additional control over caching.
 *
 * @since 5.0.0
 */
public interface FlexibleCache<K, V> extends EvictableCache<K, V> {
    /**
     * Returns the value associated with {@code key}, creating it with the
     * supplied provider when necessary and optionally caching the result.
     *
     * @param key the key to look up
     * @param valueProvider supplies a value when the key is not cached
     * @param shouldCache whether a newly created value should be stored
     * @return the cached or newly created value
     */
    V getAndPut(K key, ValueProvider<? super K, ? extends V> valueProvider, boolean shouldCache);
}
