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

import java.util.Objects;

/**
 * Wrap objects to provide the basic equals and hashCode function, which are essential to collections(especially to collections based on hash)
 * @param <K> the type of key
 *
 * @since 2.5.0
 */
public class CacheKey<K> {
    private K key;

    public CacheKey(K key) {
        if (null == key) {
            throw new IllegalArgumentException("key can not be null");
        }

        this.key = key;
    }

    public K getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey<?> cacheKey = (CacheKey<?>) o;
        return Objects.equals(key, cacheKey.key);
    }

    @Override
    public int hashCode() {
        return key.getClass().getCanonicalName().hashCode();
    }
}
