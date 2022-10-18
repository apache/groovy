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
import java.util.Map;
import java.util.function.Function;

/**
 * An interface to mark a node being able to handle metadata.
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

        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
            this.setMetaDataMap(metaDataMap);
        }
        return (T) metaDataMap.computeIfAbsent(key, valFn);
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
        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
            this.setMetaDataMap(metaDataMap);
        }

        metaDataMap.putAll(otherMetaDataMap);
    }

    /**
     * Sets the node metadata.
     *
     * @param key   the metadata key
     * @param value the metadata value
     * @throws GroovyBugError if key is null or there is already meta
     *                        data under that key
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

        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
            this.setMetaDataMap(metaDataMap);
        }
        return metaDataMap.put(key, value);
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
     * Returns an unmodifiable view of the current node metadata.
     *
     * @return the node metadata. Always not null.
     */
    default Map<?, ?> getNodeMetaData() {
        Map metaDataMap = this.getMetaDataMap();
        if (metaDataMap == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(metaDataMap);
    }

    Map<?, ?> getMetaDataMap();

    void setMetaDataMap(Map<?, ?> metaDataMap);
}
