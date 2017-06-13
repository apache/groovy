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

import java.util.Map;

/**
 * An interface to mark a node being able to handle metadata
 */
public interface NodeMetaDataHandler {
    /**
     * Gets the node meta data.
     *
     * @param key - the meta data key
     * @return the node meta data value for this key
     */
    <T> T getNodeMetaData(Object key);

    /**
     * Copies all node meta data from the other node to this one
     *
     * @param other - the other node
     */
    void copyNodeMetaData(NodeMetaDataHandler other);

    /**
     * Sets the node meta data.
     *
     * @param key   - the meta data key
     * @param value - the meta data value
     * @throws GroovyBugError if key is null or there is already meta
     *                        data under that key
     */
    void setNodeMetaData(Object key, Object value);

    /**
     * Sets the node meta data but allows overwriting values.
     *
     * @param key   - the meta data key
     * @param value - the meta data value
     * @return the old node meta data value for this key
     * @throws GroovyBugError if key is null
     */
    Object putNodeMetaData(Object key, Object value);

    /**
     * Removes a node meta data entry.
     *
     * @param key - the meta data key
     * @throws GroovyBugError if the key is null
     */
    void removeNodeMetaData(Object key);

    /**
     * Returns an unmodifiable view of the current node metadata.
     *
     * @return the node metadata. Always not null.
     */
    Map<?, ?> getNodeMetaData();

    Map<?, ?> getMetaDataMap();

    void setMetaDataMap(Map<?, ?> metaDataMap);
}
