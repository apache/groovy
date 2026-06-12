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

import java.util.Map;

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
public class ASTNode implements NodeMetaDataHandler {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;

    private volatile Map<?, ?> metaDataMap;

    /**
     * Accepts a code visitor for AST traversal and transformation.
     * Subclasses must implement this method to support visitor pattern-based processing.
     * The visitor pattern enables decoupling of AST structure from processing logic.
     *
     * @param visitor the {@link GroovyCodeVisitor} to process this node
     * @throws RuntimeException if visitor pattern support is not implemented for this node type
     */
    public void visit(final GroovyCodeVisitor visitor) {
        throw new RuntimeException("No visit() method implemented for class: " + getClass().getName());
    }

    /**
     * Returns a human-readable text representation of this AST node.
     * Used for debugging and error messages. Default implementation returns a message
     * indicating the representation is not yet implemented for this node type.
     *
     * @return text representation of this node, or placeholder for unimplemented types
     */
    public String getText() {
        Class<?> nodeType = getClass();
        if (nodeType.isAnonymousClass()) nodeType = nodeType.getSuperclass();
        return "<not implemented yet for class: " + nodeType.getName() + ">";
    }

    /**
     * Returns the line number where this AST node begins in the source file.
     * Line numbers start from 1. Returns -1 if position information is not available
     * (for synthetic or generated nodes).
     *
     * @return the starting line number, or -1 if not available
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the starting line number for this AST node in the source file.
     * Line numbers are 1-indexed. Use -1 to indicate unavailable position.
     *
     * @param lineNumber the starting line number to set
     */
    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the column number where this AST node begins in the source file.
     * Column numbers are 0-indexed. Returns -1 if position information is unavailable
     * (for synthetic or generated nodes).
     *
     * @return the starting column number, or -1 if not available
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Sets the starting column number for this AST node in the source file.
     * Column numbers are 0-indexed. Use -1 to indicate unavailable position.
     *
     * @param columnNumber the starting column number to set
     */
    public void setColumnNumber(final int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Returns the line number where this AST node ends in the source file.
     * Line numbers start from 1. Returns -1 if position information is unavailable.
     * The end position is typically inclusive (last line of the node's span).
     *
     * @return the ending line number, or -1 if not available
     */
    public int getLastLineNumber() {
        return lastLineNumber;
    }

    /**
     * Sets the ending line number for this AST node in the source file.
     * Line numbers are 1-indexed. Use -1 to indicate unavailable position.
     * The end position should typically be inclusive (last line of the node's span).
     *
     * @param lastLineNumber the ending line number to set
     */
    public void setLastLineNumber(final int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    /**
     * Returns the column number where this AST node ends in the source file.
     * Column numbers are 0-indexed. Returns -1 if position information is unavailable.
     * The end position is typically exclusive (one past the last character).
     *
     * @return the ending column number, or -1 if not available
     */
    public int getLastColumnNumber() {
        return lastColumnNumber;
    }

    /**
     * Sets the ending column number for this AST node in the source file.
     * Column numbers are 0-indexed. Use -1 to indicate unavailable position.
     * The end position should typically be exclusive (one past the last character).
     *
     * @param lastColumnNumber the ending column number to set
     */
    public void setLastColumnNumber(final int lastColumnNumber) {
        this.lastColumnNumber = lastColumnNumber;
    }

    /**
     * Sets the source position information using another ASTNode as reference.
     * Copies all position data (line/column start and end) from the source node,
     * enabling consistent source location tracking for synthetic or transformed nodes.
     *
     * @param node the reference node providing position information
     */
    public void setSourcePosition(final ASTNode node) {
        this.lineNumber = node.getLineNumber();
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
    }

    /**
     * Copies all node metadata from another ASTNode.
     * Metadata is arbitrary information attached by compiler phases or AST transforms
     * for inter-phase communication. This method performs a deep metadata transfer.
     *
     * @param other the source node to copy metadata from
     */
    public void copyNodeMetaData(final ASTNode other) {
        copyNodeMetaData((NodeMetaDataHandler) other);
    }

    /**
     * Returns the metadata map for this node.
     * Metadata stores arbitrary phase-specific information attached during compilation.
     * Returns null if no metadata has been set.
     *
     * @return the metadata map, or null if empty
     */
    @Override
    public Map<?, ?> getMetaDataMap() {
        return metaDataMap;
    }

    /**
     * Sets the metadata map for this node.
     * Replaces any existing metadata. Metadata stores arbitrary phase-specific
     * information for inter-phase communication during compilation.
     *
     * @param metaDataMap the metadata map to set
     */
    @Override
    public void setMetaDataMap(final Map<?, ?> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }
}
