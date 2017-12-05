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
package groovy.lang;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for Map which allows a default value to be specified.
 *
 * @author Paul King
 * @since 1.7.1
 */
public final class MapWithDefault<K, V> implements Map<K, V> {
    private final Map<K, V> delegate;
    private final Closure<V> initClosure;

    private MapWithDefault(Map<K, V> m, Closure<V> initClosure) {
        delegate = m;
        this.initClosure = initClosure;
    }

    public static <K, V> Map<K, V> newInstance(Map<K, V> m, Closure<V> initClosure) {
        return new MapWithDefault<K, V>(m, initClosure);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (!containsKey(key)) {
            delegate.put((K) key, getDefaultValue(key));
        }
        return delegate.get(key);
    }

    @Override
    public V put(K key, V value) {
        return (value == getDefaultValue(key)) ? remove(key)
                                               : delegate.put(key, value);
    }

    private V getDefaultValue(Object key) {
        return (V) initClosure.call(key);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}