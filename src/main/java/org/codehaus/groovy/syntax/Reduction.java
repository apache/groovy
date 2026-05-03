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
 * A syntax reduction produced by the Parser during syntactic analysis.
 * Represents a node in the concrete syntax tree (CST) that contains a root
 * {@link Token} and zero or more child {@link CSTNode} elements.
 *
 * @see Token
 * @see CSTNode
 * @see Types
 */
public class Reduction extends CSTNode {
    /** Singleton empty reduction (read-only). */
    public static final Reduction EMPTY = new Reduction();

    //---------------------------------------------------------------------------
    // INITIALIZATION AND SUCH

    /** The list of child nodes (including root at index 0). */
    private List elements = null;
    /** Flag used by some parser implementations to mark expression completion. */
    private boolean marked = false;

    /**
     * Constructs a Reduction with the specified root token.
     *
     * @param root the root {@link Token} of this reduction
     */
    public Reduction(Token root) {
        elements = new ArrayList();
        set(0, root);
    }

    /**
     * Constructs an empty Reduction (private; use {@link #EMPTY} or {@link #newContainer()}).
     */
    private Reduction() {
        elements = Collections.EMPTY_LIST;
    }

    /**
     * Creates a new Reduction with {@link Token#NULL} as its root.
     * Useful for creating container nodes with no specific type.
     *
     * @return a new Reduction with a null root token
     */
    public static Reduction newContainer() {
        return new Reduction(Token.NULL);
    }

    //---------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns {@code true} if this reduction is completely empty (no elements, not even a root).
     *
     * @return {@code true} if empty
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of elements in this reduction (including the root token).
     *
     * @return the number of elements
     */
    @Override
    public int size() {
        return elements.size();
    }

    /**
     * Returns the element at the specified index, or {@code null} if the index is out of bounds.
     *
     * @param index the element index (0 for root)
     * @return the element at the index, or {@code null} if not found
     */
    @Override
    public CSTNode get(int index) {
        CSTNode element = null;

        if (index < size()) {
            element = (CSTNode) elements.get(index);
        }

        return element;
    }

    /**
     * Returns the root token of this reduction.
     * The root is always at index 0 and indicates the syntactic type.
     *
     * @return the root token, or {@code null} if this reduction is empty
     */
    @Override
    public Token getRoot() {
        if (size() > 0) {
            return (Token) elements.get(0);
        } else {
            return null;
        }
    }

    /**
     * Marks this reduction as a complete expression.
     * This flag is used by the parser to distinguish complete expressions
     * from partial constructs.
     */
    @Override
    public void markAsExpression() {
        marked = true;
    }

    /**
     * Returns {@code true} if this node represents a complete expression.
     * A reduction is considered an expression if it has the {@link Types#COMPLEX_EXPRESSION}
     * meaning or has been explicitly marked as an expression.
     *
     * @return {@code true} if this is an expression
     */
    @Override
    public boolean isAnExpression() {
        if (isA(Types.COMPLEX_EXPRESSION)) {
            return true;
        }

        return marked;
    }

    //---------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Adds an element to the end of this reduction.
     *
     * @param element the element to add
     * @return the added element
     */
    @Override
    public CSTNode add(CSTNode element) {
        return set(size(), element);
    }

    /**
     * Sets the element at the specified index.
     * If the index is beyond the current size, intermediate positions are filled with {@code null}.
     * The root element (index 0) must be a {@link Token}.
     *
     * @param index the element index
     * @param element the element to set
     * @return the set element
     * @throws GroovyBugError if trying to set a non-Token as root, or if operating on {@link #EMPTY}
     */
    @Override
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
     * Removes the element at the specified index.
     * Cannot remove the root element (index 0).
     *
     * @param index the element index (must be &gt; 0)
     * @return the removed element
     * @throws GroovyBugError if trying to remove the root element
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
    @Override
    public Reduction asReduction() {
        return this;
    }
}
