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

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a basic implementation of a map able to forget its values. This
 * map uses internally a ConcurrentHashMap, thus should be safe for concurrency.
 * hashcode and equals are used to find the entries and should thus be implemented
 * properly for the keys. This map does not support null keys.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ManagedConcurrentValueMap<K,V> {
    private final ConcurrentHashMap<K,ManagedReference<V>> internalMap;
    private ReferenceBundle bundle;
    public ManagedConcurrentValueMap(ReferenceBundle bundle){
        this.bundle = bundle;
        internalMap = new ConcurrentHashMap<K, ManagedReference<V>>();
    }
    
    /**
     * Sets a new bundle used for reference creation. Be warned that
     * older entries will not be changed by this
     * @param bundle - the ReferenceBundle
     */
    public void setBundle(ReferenceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns the value stored for the given key at the point of call.
     * @param key a non null key
     * @return the value stored in the map for the given key
     */
    public V get(K key) {
        ManagedReference<V> ref = internalMap.get(key);
        if (ref!=null) return ref.get();
        return null;
    }

    /**
     * Sets a new value for a given key. an older value is overwritten.
     * @param key a non null key
     * @param value the new value
     */
    public void put(final K key, V value) {
        ManagedReference<V> ref = new ManagedReference<V>(bundle, value) {
            @Override
            public void finalizeReference() {
                internalMap.remove(key, this);
                super.finalizeReference();
            }
        };
        internalMap.put(key, ref);
    }
    
}
