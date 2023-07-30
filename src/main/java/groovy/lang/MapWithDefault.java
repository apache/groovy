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
 * A wrapper for Map which allows a default value to be specified using a closure.
 * Normally not instantiated directly but used via the DGM <code>withDefault</code> method.
 *
 * @since 1.7.1
 */
public final class MapWithDefault<K, V> implements Map<K, V> {

    private final Map<K, V> delegate;
    private final Closure<V> initClosure;
    private final boolean autoGrow;
    private final boolean autoShrink;

    private MapWithDefault(Map<K, V> m, Closure<V> initClosure, boolean autoGrow, boolean autoShrink) {
        delegate = m;
        this.initClosure = initClosure;
        this.autoGrow = autoGrow;
        this.autoShrink = autoShrink;
    }

    /**
     * Decorates the given Map allowing a default value to be specified.
     *
     * @param m           a Map to wrap
     * @param initClosure the closure which when passed the <code>key</code> returns the default value
     * @return the wrapped Map
     */
    public static <K, V> Map<K, V> newInstance(Map<K, V> m, Closure<V> initClosure) {
        return new MapWithDefault<>(m, initClosure, true, false);
    }

    /**
     * Decorates the given Map allowing a default value to be specified.
     * Allows the behavior to be configured using {@code autoGrow} and {@code autoShrink} parameters.
     * The value of {@code autoShrink} doesn't alter any values in the initial wrapped map, but you
     * can start with an empty map and use {@code putAll} if you really need the minimal backing map value.
     *
     * @param m           a Map to wrap
     * @param autoGrow    when true, also mutate the map adding in this value; otherwise, don't mutate the map, just return to calculated value
     * @param autoShrink  when true, ensure the key will be removed if attempting to store the default value using put or putAll
     * @param initClosure the closure which when passed the <code>key</code> returns the default value
     * @return the wrapped Map
     * @since 4.0.1
     */
    public static <K, V> Map<K, V> newInstance(Map<K, V> m, boolean autoGrow, boolean autoShrink, Closure<V> initClosure) {
        return new MapWithDefault<>(m, initClosure, autoGrow, autoShrink);
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

    /**
     * Returns the value to which the specified key is mapped,
     * or the default value as specified by the initializing closure
     * if this map contains no mapping for the key.
     *
     * If <code>autoGrow</code> is true and the initializing closure is called,
     * the map is modified to contain the new key and value so that the calculated
     * value is effectively cached if needed again.
     * Otherwise, the map will be unchanged.
     */
    @Override
    public V get(Object key) {
        if (delegate.containsKey(key)) {
            return delegate.get(key);
        }
        V value = getDefaultValue(key);
        if (autoGrow) {
            delegate.put((K)key, value);
        }
        return value;
    }

    private V getDefaultValue(Object key) {
        return initClosure.call(new Object[]{key});
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * If <code>autoShrink</code> is true, the initializing closure is called
     * and if it evaluates to the value being stored, the value will not be stored
     * and indeed any existing value will be removed. This can be useful when trying
     * to keep the memory requirements small for large key sets where only a spare
     * number of entries differ from the default.
     *
     * @return the previous value associated with {@code key} if any, otherwise {@code null}.
     */
    @Override
    public V put(K key, V value) {
        if (autoShrink && value.equals(getDefaultValue(key))) {
            return remove(key);
        }
        return delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
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
    public Set<Map.Entry<K, V>> entrySet() {
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
