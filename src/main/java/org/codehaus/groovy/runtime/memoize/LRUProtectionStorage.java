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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Protects stored resources from eviction from memory following the LRU (Last Recently Used) strategy.
 * If the maximum size has been reached all newly added elements will cause the oldest element to be removed from the storage
 * in order not to exceed the maximum capacity.
 * The touch method can be used to renew an element and move it to the from the LRU queue.
 */
final class LRUProtectionStorage extends LinkedHashMap<Object, Object> implements ProtectionStorage {
    private static final long serialVersionUID = 1L;

    private final int maxSize;

    public LRUProtectionStorage(final int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * The eldest entry should be removed when we reached the maximum cache size
     */
    @Override
    protected synchronized boolean removeEldestEntry(final Map.Entry<Object, Object> eldest) {
        return size() > maxSize;
    }

    /**
     * The touch method can be used to renew an element and move it to the from of the LRU queue.
     *
     * @param key   The key of the element to renew
     * @param value A value to newly associate with the key
     */
    @Override
    public synchronized void touch(final Object key, final Object value) {
        remove(key);
        put(key, value);
    }

    /**
     * Makes sure the retrieved object is moved to the head of the LRU list
     */
    @Override
    public synchronized Object get(final Object key) {
        final Object value = remove(key);
        if (value != null) put(key, value);
        return value;
    }
    
    /**
     * Performs a shallow clone
     *
     * @return The cloned instance
     */
    @Override
    public Object clone() {
        return super.clone();
    }
}
