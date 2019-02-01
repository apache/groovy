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
import java.util.Objects;

/**
 * Base class for any AST node. This class supports basic information used in all nodes of the AST:
 * <ul>
 * <li> line and column number information. Usually a node represents a certain
 * area in a text file determined by a starting position and an ending position.
 * For nodes that do not represent this, this information will be -1. A node can
 * also be configured in its line/col information using another node through 
 * setSourcePosition(otherNode).</li>
 * <li> every node can store meta data. A phase operation or transform can use 
 * this to transport arbitrary information to another phase operation or 
 * transform. The only requirement is that the other phase operation or transform
 * runs after the part storing the information. If the information transport is 
 * done it is strongly recommended to remove that meta data.</li> 
 * <li> a text representation of this node trough getText(). This was in the
 * past used for assertion messages. Since the usage of power asserts this 
 * method will not be called for this purpose anymore and might be removed in
 * future versions of Groovy</li>
 * </ul>
 */
public class ASTNode {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;
    private ListHashMap metaDataMap = null;

    public void visit(GroovyCodeVisitor visitor) {
        throw new RuntimeException("No visit() method implemented for class: " + getClass().getName());
    }

    public String getText() {
        return "<not implemented yet for class: " + getClass().getName() + ">";
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    public int getLastColumnNumber() {
        return lastColumnNumber;
    }

    public void setLastColumnNumber(int lastColumnNumber) {
        this.lastColumnNumber = lastColumnNumber;
    }
    
    /**
     * Sets the source position using another ASTNode.
     * The sourcePosition consists of a line/column pair for
     * the start and a line/column pair for the end of the
     * expression or statement 
     * 
     * @param node - the node used to configure the position information
     */
    public void setSourcePosition(ASTNode node) {
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
        this.lineNumber = node.getLineNumber();
    }
    
    /**
     * Gets the node meta data. 
     * 
     * @param key - the meta data key
     * @return the node meta data value for this key
     */
    public <T> T getNodeMetaData(Object key) {
        if (metaDataMap == null) {
            return (T) null;
        }
        return (T) metaDataMap.get(key);
    }

    /**
     * Copies all node meta data from the other node to this one
     * @param other - the other node
     */
    public void copyNodeMetaData(ASTNode other) {
        if (other.metaDataMap == null) {
            return;
        }
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
        }
        metaDataMap.putAll(other.metaDataMap);
    }
    
    /**
     * Sets the node meta data. 
     * 
     * @param key - the meta data key
     * @param value - the meta data value
     * @throws GroovyBugError if key is null or there is already meta 
     *                        data under that key
     */
    public void setNodeMetaData(Object key, Object value) {
        if (key==null) throw new GroovyBugError("Tried to set meta data with null key on "+this+".");
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
        }
        Object old = metaDataMap.put(key,value);
        if (old!=null) throw new GroovyBugError("Tried to overwrite existing meta data "+this+".");
    }

    /**
     * Sets the node meta data but allows overwriting values.
     *
     * @param key   - the meta data key
     * @param value - the meta data value
     * @return the old node meta data value for this key
     * @throws GroovyBugError if key is null
     */
    public Object putNodeMetaData(Object key, Object value) {
        if (key == null) throw new GroovyBugError("Tried to set meta data with null key on " + this + ".");
        if (metaDataMap == null) {
            metaDataMap = new ListHashMap();
        }
        return metaDataMap.put(key, value);
    }

    /**
     * Removes a node meta data entry.
     * 
     * @param key - the meta data key
     * @throws GroovyBugError if the key is null
     */
    public void removeNodeMetaData(Object key) {
        if (key==null) throw new GroovyBugError("Tried to remove meta data with null key "+this+".");
        if (metaDataMap == null) {
            return;
        }
        metaDataMap.remove(key);
    }

    /**
     * Returns an unmodifiable view of the current node metadata.
     * @return the node metadata. Always not null.
     */
    public Map<?,?> getNodeMetaData() {
        if (metaDataMap==null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(metaDataMap);
    }

    public ListHashMap getMetaDataMap() {
        return metaDataMap;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, columnNumber, lastLineNumber, lastColumnNumber);
    }
}
