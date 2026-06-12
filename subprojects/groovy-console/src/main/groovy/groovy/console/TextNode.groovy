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
package groovy.console

import groovy.transform.CompileStatic

/**
 * Represents a plain text node for use in the AST tree made by ASTBrowser.
 */
@CompileStatic
class TextNode {
    /** Value displayed for this node. */
    Object userObject
    /** Additional name/value metadata shown for this node. */
    List<List<?>> properties
    /** Parent node in the text tree. */
    TextNode parent
    /** Child nodes in insertion order. */
    List children

    /**
     * Creates a node with the supplied display value.
     *
     * @param userObject value displayed for the node
     */
    TextNode(Object userObject) {
        this.userObject = userObject
        children = new ArrayList<TextNode>()
    }

    /**
     * Creates a node with the supplied display value and metadata.
     *
     * @param userObject value displayed for the node
     * @param properties metadata associated with the node
     */
    TextNode(Object userObject, List<List<?>> properties) {
        this(userObject)
        this.properties = properties
    }

    /**
     * Adds a child node.
     *
     * @param child child node to append
     */
    void add(TextNode child) {
        children << child
    }

    /**
     * Sets the parent node.
     *
     * @param newParent parent node reference
     */
    void setParent(TextNode newParent) {
        parent = newParent
    }

    /**
     * Returns the display text for this node.
     *
     * @return the node text
     */
    String toString() {
        userObject ? userObject.toString() : 'null'
    }
}
