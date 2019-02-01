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
package org.codehaus.groovy.syntax;

import org.codehaus.groovy.GroovyBugError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A syntax reduction, produced by the <code>Parser</code>.
 *
 * @see antlr.Parser
 * @see Token
 * @see CSTNode
 * @see Types
 */
public class Reduction extends CSTNode {
    public static final Reduction EMPTY = new Reduction();

    //---------------------------------------------------------------------------
    // INITIALIZATION AND SUCH

    private List elements = null;    // The set of child nodes
    private boolean marked = false;   // Used for completion marking by some parts of the parser

    /**
     * Initializes the <code>Reduction</code> with the specified root.
     */
    public Reduction(Token root) {
        elements = new ArrayList();
        set(0, root);
    }

    /**
     * Initializes the <code>Reduction</code> to empty.
     */
    private Reduction() {
        elements = Collections.EMPTY_LIST;
    }

    /**
     * Creates a new <code>Reduction</code> with <code>Token.NULL</code>
     * as it's root.
     */
    public static Reduction newContainer() {
        return new Reduction(Token.NULL);
    }

    //---------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns true if the node is completely empty (no root, even).
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of elements in the node.
     */
    public int size() {
        return elements.size();
    }

    /**
     * Returns the specified element, or null.
     */
    public CSTNode get(int index) {
        CSTNode element = null;

        if (index < size()) {
            element = (CSTNode) elements.get(index);
        }

        return element;
    }

    /**
     * Returns the root of the node, the Token that indicates it's
     * type.  Returns null if there is no root (usually only if the
     * node is a placeholder of some kind -- see isEmpty()).
     */
    public Token getRoot() {
        if (size() > 0) {
            return (Token) elements.get(0);
        } else {
            return null;
        }
    }

    /**
     * Marks the node a complete expression.
     */
    public void markAsExpression() {
        marked = true;
    }

    /**
     * Returns true if the node is a complete expression.
     */
    public boolean isAnExpression() {
        if (isA(Types.COMPLEX_EXPRESSION)) {
            return true;
        }

        return marked;
    }

    //---------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Adds an element to the node.
     */
    public CSTNode add(CSTNode element) {
        return set(size(), element);
    }

    /**
     * Sets an element in at the specified index.
     */
    public CSTNode set(int index, CSTNode element) {

        if (elements == null) {
            throw new GroovyBugError("attempt to set() on a EMPTY Reduction");
        }

        if (index == 0 && !(element instanceof Token)) {

            //
            // It's not the greatest of design that the interface allows this, but it
            // is a tradeoff with convenience, and the convenience is more important.

            throw new GroovyBugError("attempt to set() a non-Token as root of a Reduction");
        }


        //
        // Fill slots with nulls, if necessary.

        int count = elements.size();
        if (index >= count) {
            for (int i = count; i <= index; i++) {
                elements.add(null);
            }
        }

        //
        // Then set in the element.

        elements.set(index, element);

        return element;
    }

    /**
     * Removes a node from the <code>Reduction</code>.  You cannot remove
     * the root node (index 0).
     */
    public CSTNode remove(int index) {
        if (index < 1) {
            throw new GroovyBugError("attempt to remove() root node of Reduction");
        }

        return (CSTNode) elements.remove(index);
    }

    /**
     * Creates a <code>Reduction</code> from this node.  Returns self if the
     * node is already a <code>Reduction</code>.
     */
    public Reduction asReduction() {
        return this;
    }
}
