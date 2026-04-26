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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.util.ListHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An interface to mark a node being able to handle metadata.
 * <p>
 * The default {@link #newMetaDataMap()} returns a {@link ListHashMap} wrapped in
 * {@link Collections#synchronizedMap}, so concurrent compiles sharing an AST node
 * (e.g. built-in annotation {@code ClassNode}s cached by {@code ClassHelper})
 * cannot trip an {@code ArrayIndexOutOfBoundsException} during the
 * array-to-{@link HashMap} transition in {@code ListHashMap.put}. Implementers
 * that store the map in a field should declare it {@code volatile} so the
 * unsynchronized fast-path read in the default methods sees publish-time writes.
 *
 * @since 3.0.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface NodeMetaDataHandler {

    /**
     * Gets the node metadata.
     *
     * @param key the metadata key
     * @return the node metadata value for this key
     */
    default <T> T getNodeMetaData(Object key) {
        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            return null;
        }
        return (T) metaDataMap.get(key);
    }

    /**
     * Gets the node metadata.
     *
     * @param key the metadata key
     * @param valFn the metadata value supplier
     * @return the node metadata value for this key
     */
    default <T> T getNodeMetaData(Object key, Function<?, ? extends T> valFn) {
        if (key == null) throw new GroovyBugError("Tried to get/set meta data with null key on " + this + ".");

        return (T) getOrCreateMetaDataMap().computeIfAbsent(key, valFn);
    }

    /**
     * Copies all node metadata from the other node to this one
     *
     * @param other the other node
     */
    default void copyNodeMetaData(NodeMetaDataHandler other) {
        Map otherMetaDataMap = other.getMetaDataMap();
        if (otherMetaDataMap == null) {
            return;
        }
        // snapshot under the source map's mutex to honour the synchronizedMap iteration contract
        Map snapshot;
        synchronized (otherMetaDataMap) {
            if (otherMetaDataMap.isEmpty()) return;
            snapshot = new HashMap<>(otherMetaDataMap);
        }
        getOrCreateMetaDataMap().putAll(snapshot);
    }

    /**
     * Sets the node metadata.
     *
     * @param key   the metadata key
     * @param value the metadata value
     * @throws GroovyBugError if key is null or there is already metadata for key
     */
    default void setNodeMetaData(Object key, Object value) {
        Object old = putNodeMetaData(key, value);
        if (old != null) throw new GroovyBugError("Tried to overwrite existing meta data " + this + ".");
    }

    /**
     * Sets the node metadata but allows overwriting values.
     *
     * @param key   the metadata key
     * @param value the metadata value
     * @return the old node metadata value for this key
     * @throws GroovyBugError if key is null
     */
    default Object putNodeMetaData(Object key, Object value) {
        if (key == null) throw new GroovyBugError("Tried to set meta data with null key on " + this + ".");

        if (value == null) {
            Map metaDataMap = this.getMetaDataMap();
            return metaDataMap == null ? null : metaDataMap.remove(key);
        }
        return getOrCreateMetaDataMap().put(key, value);
    }

    /**
     * Removes a node metadata entry.
     *
     * @param key the metadata key
     * @throws GroovyBugError if the key is null
     */
    default void removeNodeMetaData(Object key) {
        if (key == null) throw new GroovyBugError("Tried to remove meta data with null key " + this + ".");

        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            return;
        }
        metaDataMap.remove(key);
    }

    /**
     * Returns an unmodifiable snapshot of the current node metadata.
     *
     * @return the node metadata. Always not null.
     */
    default Map<?, ?> getNodeMetaData() {
        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            return Collections.emptyMap();
        }
        // snapshot under the map's mutex to honour the synchronizedMap iteration contract
        synchronized (metaDataMap) {
            return Collections.unmodifiableMap(new HashMap<>(metaDataMap));
        }
    }

    /**
     * Returns the existing metadata map, creating one via {@link #newMetaDataMap()}
     * on first use. Lazy creation is guarded by a brief lock on {@code this} so
     * concurrent first-callers agree on a single map; subsequent callers see the
     * map via the (volatile) field read and skip the lock entirely.
     */
    private Map getOrCreateMetaDataMap() {
        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap != null) return metaDataMap;
        synchronized (this) {
            metaDataMap = this.getMetaDataMap();
            if (metaDataMap == null) {
                metaDataMap = this.newMetaDataMap();
                this.setMetaDataMap(metaDataMap);
            }
            return metaDataMap;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the underlying metadata map. The map returned by the default
     * {@link #newMetaDataMap()} is internally synchronized, so individual
     * {@code get}/{@code put}/{@code remove} calls are thread-safe; however,
     * per the {@link Collections#synchronizedMap} contract, iteration over the
     * returned map (or any of its {@code keySet}, {@code values}, or
     * {@code entrySet} views) must be done inside a
     * {@code synchronized (map) { ... }} block to avoid
     * {@code ConcurrentModificationException}.
     */
    Map<?, ?> getMetaDataMap();

    /**
     * Creates the backing metadata map. The default returns a {@link ListHashMap}
     * wrapped in {@link Collections#synchronizedMap} for thread-safe per-entry
     * access; subclasses may override to supply an alternative map (e.g. for
     * different memory/concurrency trade-offs).
     *
     * @since 5.0.0
     */
    default Map<?, ?> newMetaDataMap() {
        return Collections.synchronizedMap(new ListHashMap());
    }

    void setMetaDataMap(Map<?, ?> metaDataMap);
}
