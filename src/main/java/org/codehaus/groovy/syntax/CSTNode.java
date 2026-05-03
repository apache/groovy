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

import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.GroovyBugError;

import java.io.PrintWriter;
import java.io.Writer;


/**
 * Abstract base class for nodes in the concrete syntax tree (CST) produced by parsing.
 * Every CST node has a {@link Token} as its root element, which indicates the node's type.
 * This class provides methods for querying node meaning, type, structure, and converting
 * between different node representations.
 *
 * @see Token
 * @see Reduction
 * @see Types
 */
public abstract class CSTNode {

    //---------------------------------------------------------------------------
    // NODE IDENTIFICATION AND MEANING

    /**
     * Returns the current semantic meaning (interpretation) of this node.
     * For nodes without a root, returns {@link Types#UNKNOWN}.
     *
     * @return the node's meaning type from {@link Types}
     */
    public int getMeaning() {
        return getRoot(true).getMeaning();
    }

    /**
     * Sets the semantic meaning (interpretation) of this node.
     * The meaning may differ from the node's actual type and is often assigned
     * during semantic analysis after parsing.
     *
     * @param meaning the new meaning type from {@link Types}
     * @return this node for convenience chaining
     */
    public CSTNode setMeaning(int meaning) {
        getRoot().setMeaning(meaning);
        return this;
    }

    /**
     * Returns the actual syntactic type of this node as determined by the parser.
     * For nodes without a root, returns {@link Types#UNKNOWN}.
     *
     * @return the node's type from {@link Types}
     */
    public int getType() {
        return getRoot(true).getType();
    }

    /**
     * Returns {@code true} if this node can be coerced to the specified type.
     * This is determined by the type hierarchy defined in {@link Types}.
     *
     * @param type the type to check against
     * @return {@code true} if coercion is possible
     */
    public boolean canMean(int type) {
        return Types.canMean(getMeaning(), type);
    }

    /**
     * Returns {@code true} if this node's meaning matches the specified type.
     *
     * @param type the type to check
     * @return {@code true} if the node's meaning is of the specified type
     */
    public boolean isA(int type) {
        return Types.ofType(getMeaning(), type);
    }

    /**
     * Returns {@code true} if this node's meaning matches any of the specified types.
     *
     * @param types an array of types to check
     * @return {@code true} if the node's meaning matches at least one of the types
     */
    public boolean isOneOf(int[] types) {
        int meaning = getMeaning();
        for (int type : types) {
            if (Types.ofType(meaning, type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if this node's meaning matches all of the specified types.
     *
     * @param types an array of types to check
     * @return {@code true} if the node's meaning matches all of the types
     */
    public boolean isAllOf(int[] types) {
        int meaning = getMeaning();
        for (int type : types) {
            if (!Types.ofType(meaning, type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the first matching meaning of the specified types.
     * Useful for determining which of several possible meanings this node has.
     *
     * @param types an array of types to check
     * @return the first matching type, or {@link Types#UNKNOWN} if no match is found
     */
    public int getMeaningAs(int[] types) {

        for (int type : types) {
            if (isA(type)) {
                return type;
            }
        }

        return Types.UNKNOWN;
    }

    //---------------------------------------------------------------------------
    // TYPE SUGAR

    /**
     * Returns {@code true} if this node matches the specified type.
     * Equivalent to {@link #isA(int)}. Missing nodes are represented as {@link Token#NULL}.
     *
     * @param type the type to match
     * @return {@code true} if the node matches the type
     */
    boolean matches(int type) {
        return isA(type);
    }

    /**
     * Returns {@code true} if this node and its first child match the specified types.
     * Missing nodes are represented as {@link Token#NULL}.
     *
     * @param type the type to match for this node
     * @param child1 the type to match for the first child
     * @return {@code true} if the node and child match the types
     */
    boolean matches(int type, int child1) {
        return isA(type) && get(1, true).isA(child1);
    }

    /**
     * Returns {@code true} if this node and its first two children match the specified types.
     * Missing nodes are represented as {@link Token#NULL}.
     *
     * @param type the type to match for this node
     * @param child1 the type to match for the first child
     * @param child2 the type to match for the second child
     * @return {@code true} if the node and children match the types
     */
    boolean matches(int type, int child1, int child2) {
        return matches(type, child1) && get(2, true).isA(child2);
    }

    /**
     * Returns {@code true} if this node and its first three children match the specified types.
     * Missing nodes are represented as {@link Token#NULL}.
     *
     * @param type the type to match for this node
     * @param child1 the type to match for the first child
     * @param child2 the type to match for the second child
     * @param child3 the type to match for the third child
     * @return {@code true} if the node and children match the types
     */
    boolean matches(int type, int child1, int child2, int child3) {
        return matches(type, child1, child2) && get(3, true).isA(child3);
    }

    /**
     * Returns {@code true} if this node and its first four children match the specified types.
     * Missing nodes are represented as {@link Token#NULL}.
     *
     * @param type the type to match for this node
     * @param child1 the type to match for the first child
     * @param child2 the type to match for the second child
     * @param child3 the type to match for the third child
     * @param child4 the type to match for the fourth child
     * @return {@code true} if the node and children match the types
     */
    boolean matches(int type, int child1, int child2, int child3, int child4) {
        return matches(type, child1, child2, child3) && get(4, true).isA(child4);
    }

    //---------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns {@code true} if this node is completely empty (no root element).
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the number of elements in this node (including the root).
     *
     * @return the number of elements
     */
    public abstract int size();

    /**
     * Returns {@code true} if this node has any children (non-root elements).
     *
     * @return {@code true} if the node has children
     */
    public boolean hasChildren() {
        return children() > 0;
    }

    /**
     * Returns the number of child elements (excluding the root).
     *
     * @return the number of children
     */
    public int children() {
        int size = size();
        if (size > 1) {
            return size - 1;
        }
        return 0;
    }

    /**
     * Returns the element at the specified index, or {@code null} if not found.
     *
     * @param index the element index (0 for root)
     * @return the element at the index, or {@code null}
     */
    public abstract CSTNode get(int index);

    /**
     * Returns the element at the specified index, or {@link Token#NULL} if
     * the element is not found and {@code safe} is {@code true}.
     *
     * @param index the element index
     * @param safe if {@code true}, returns {@link Token#NULL} instead of {@code null} for missing elements
     * @return the element at the index, {@link Token#NULL} if safe and missing, or {@code null}
     */
    public CSTNode get(int index, boolean safe) {
        CSTNode element = get(index);

        if (element == null && safe) {
            element = Token.NULL;
        }

        return element;
    }

    /**
     * Returns the root token of this node. By convention, all nodes have
     * a {@link Token} as their root element (at index 0), which indicates
     * the syntactic type of the node.
     *
     * @return the root token, or {@code null} if the node is empty
     */
    public abstract Token getRoot();

    /**
     * Returns the root token of this node, returning {@link Token#NULL} if
     * the actual root is {@code null} and {@code safe} is {@code true}.
     *
     * @param safe if {@code true}, returns {@link Token#NULL} instead of {@code null}
     * @return the root token
     */
    public Token getRoot(boolean safe) {
        Token root = getRoot();

        if (root == null && safe) {
            root = Token.NULL;
        }

        return root;
    }

    /**
     * Returns the text content of the root token.
     * Uses {@code getRoot(true)} to ensure a non-null root.
     *
     * @return the text of the root token
     */
    public String getRootText() {
        Token root = getRoot(true);
        return root.getText();
    }

    /**
     * Returns a human-readable description of this node's semantic meaning.
     *
     * @return a description string from {@link Types}
     */
    public String getDescription() {
        return Types.getDescription(getMeaning());
    }

    /**
     * Returns the starting line number of this node in the source.
     *
     * @return the line number (1-based), or -1 if not known
     */
    public int getStartLine() {
        return getRoot(true).getStartLine();
    }

    /**
     * Returns the starting column number of this node in the source.
     *
     * @return the column number (1-based), or -1 if not known
     */
    public int getStartColumn() {
        return getRoot(true).getStartColumn();
    }

    /**
     * Marks this node as a complete expression.
     * Not all node types support this operation.
     *
     * @throws GroovyBugError if this node type does not support marking
     */
    public void markAsExpression() {
        throw new GroovyBugError("markAsExpression() not supported for this CSTNode type");
    }

    /**
     * Returns {@code true} if this node represents a complete expression.
     *
     * @return {@code true} if this is an expression
     */
    public boolean isAnExpression() {
        return isA(Types.SIMPLE_EXPRESSION);
    }

    //---------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Adds an element to this node.
     * Not all node types support this operation.
     *
     * @param element the element to add
     * @return the added element
     * @throws GroovyBugError if this node type does not support adding
     */
    public CSTNode add(CSTNode element) {
        throw new GroovyBugError("add() not supported for this CSTNode type");
    }

    /**
     * Adds all children of the specified node to this node.
     * Skips the root element and copies only the children.
     *
     * @param of the source node whose children are to be copied
     */
    public void addChildrenOf(CSTNode of) {
        for (int i = 1; i < of.size(); i++) {
            add(of.get(i));
        }
    }

    /**
     * Sets the element at the specified index.
     * Not all node types support this operation.
     *
     * @param index the element index
     * @param element the element to set
     * @return the set element
     * @throws GroovyBugError if this node type does not support setting
     */
    public CSTNode set(int index, CSTNode element) {
        throw new GroovyBugError("set() not supported for this CSTNode type");
    }

    /**
     * Converts this node to a {@link Reduction}.
     * If this node is already a Reduction, returns itself.
     *
     * @return this node as a Reduction
     */
    public abstract Reduction asReduction();

    //---------------------------------------------------------------------------
    // STRING CONVERSION

    /**
     * Returns a formatted string representation of this node and its children.
     *
     * @return the formatted node tree as a string
     */
    @Override
    public String toString() {
        Writer string = new StringBuilderWriter();
        write(new PrintWriter(string));

        return string.toString();
    }

    /**
     * Writes a formatted representation of this node to the specified writer.
     *
     * @param writer the {@link PrintWriter} to write to
     */
    public void write(PrintWriter writer) {
        write(writer, "");
    }

    /**
     * Writes a formatted representation of this node to the specified writer,
     * with indentation for readability. The indentation is increased for each
     * level of recursion to show the tree structure.
     *
     * @param writer the {@link PrintWriter} to write to
     * @param indent the indentation string to prepend to each line
     */
    protected void write(PrintWriter writer, String indent) {
        writer.print("(");

        if (!isEmpty()) {
            Token root = getRoot(true);
            int type = root.getType();
            int meaning = root.getMeaning();


            //
            // Display our type, text, and (optional) meaning

            writer.print(Types.getDescription(type));

            if (meaning != type) {
                writer.print(" as ");
                writer.print(Types.getDescription(meaning));
            }

            if (getStartLine() > -1) {
                writer.print(" at " + getStartLine() + ":" + getStartColumn());
            }

            String text = root.getText();
            int length = text.length();
            if (length > 0) {
                writer.print(": ");
                if (length > 40) {
                    text = text.substring(0, 17) + "..." + text.substring(length - 17, length);
                }

                writer.print(" \"");
                writer.print(text);
                writer.print("\" ");
            } else if (children() > 0) {
                writer.print(": ");
            }


            //
            // Recurse to display the children.

            int count = size();
            if (count > 1) {
                writer.println("");

                String indent1 = indent + "  ";
                String indent2 = indent + "   ";
                for (int i = 1; i < count; i++) {
                    writer.print(indent1);
                    writer.print(i);
                    writer.print(": ");

                    get(i, true).write(writer, indent2);
                }

                writer.print(indent);
            }
        }

        if (!indent.isEmpty()) {
            writer.println(")");
        } else {
            writer.print(")");
        }
    }
}
