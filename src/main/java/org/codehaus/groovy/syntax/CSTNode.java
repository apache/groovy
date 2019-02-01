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
 * An abstract base class for nodes in the concrete syntax tree that is
 * the result of parsing.  Note that the CSTNode is inextricably linked
 * with the Token in that every CSTNode has a Token as it's root.
 *
 * @see antlr.Parser
 * @see Token
 * @see org.codehaus.groovy.syntax.Reduction
 * @see org.codehaus.groovy.syntax.Types
 */
public abstract class CSTNode {

    //---------------------------------------------------------------------------
    // NODE IDENTIFICATION AND MEANING


    /**
     * Returns the meaning of this node.  If the node isEmpty(), returns
     * the type of Token.NULL.
     */
    public int getMeaning() {
        return getRoot(true).getMeaning();
    }

    /**
     * Sets the meaning for this node (and it's root Token).  Not
     * valid if the node isEmpty().  Returns the node, for convenience.
     */
    public CSTNode setMeaning(int meaning) {
        getRoot().setMeaning(meaning);
        return this;
    }

    /**
     * Returns the actual type of the node.  If the node isEmpty(), returns
     * the type of Token.NULL.
     */
    public int getType() {
        return getRoot(true).getType();
    }

    /**
     * Returns true if the node can be coerced to the specified type.
     */
    public boolean canMean(int type) {
        return Types.canMean(getMeaning(), type);
    }

    /**
     * Returns true if the node's meaning matches the specified type.
     */
    public boolean isA(int type) {
        return Types.ofType(getMeaning(), type);
    }

    /**
     * Returns true if the node's meaning matches any of the specified types.
     */
    public boolean isOneOf(int[] types) {
        int meaning = getMeaning();
        for (int i = 0; i < types.length; i++) {
            if (Types.ofType(meaning, types[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the node's meaning matches all of the specified types.
     */
    public boolean isAllOf(int[] types) {
        int meaning = getMeaning();
        for (int i = 0; i < types.length; i++) {
            if (!Types.ofType(meaning, types[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the first matching meaning of the specified types.
     * Returns Types.UNKNOWN if there are no matches.
     */
    public int getMeaningAs(int[] types) {

        for (int i = 0; i < types.length; i++) {
            if (isA(types[i])) {
                return types[i];
            }
        }

        return Types.UNKNOWN;
    }

    //---------------------------------------------------------------------------
    // TYPE SUGAR

    /**
     * Returns true if the node matches the specified type.  Effectively
     * a synonym for <code>isA()</code>.  Missing nodes are Token.NULL.
     */
    boolean matches(int type) {
        return isA(type);
    }

    /**
     * Returns true if the node and it's first child match the specified
     * types.  Missing nodes are Token.NULL.
     */
    boolean matches(int type, int child1) {
        return isA(type) && get(1, true).isA(child1);
    }

    /**
     * Returns true if the node and it's first and second child match the
     * specified types.  Missing nodes are Token.NULL.
     */
    boolean matches(int type, int child1, int child2) {
        return matches(type, child1) && get(2, true).isA(child2);
    }

    /**
     * Returns true if the node and it's first three children match the
     * specified types.  Missing nodes are Token.NULL.
     */
    boolean matches(int type, int child1, int child2, int child3) {
        return matches(type, child1, child2) && get(3, true).isA(child3);
    }

    /**
     * Returns true if the node an it's first four children match the
     * specified types.  Missing nodes have type Types.NULL.
     */
    boolean matches(int type, int child1, int child2, int child3, int child4) {
        return matches(type, child1, child2, child3) && get(4, true).isA(child4);
    }

    //---------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns true if the node is completely empty (no root, even).
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the number of elements in the node (including root).
     */
    public abstract int size();

    /**
     * Returns true if the node has any non-root elements.
     */
    public boolean hasChildren() {
        return children() > 0;
    }

    /**
     * Returns the number of non-root elements in the node.
     */
    public int children() {
        int size = size();
        if (size > 1) {
            return size - 1;
        }
        return 0;
    }

    /**
     * Returns the specified element, or null.
     */
    public abstract CSTNode get(int index);

    /**
     * Returns the specified element, or Token.NULL if
     * safe is set and the specified element is null (or doesn't exist).
     */
    public CSTNode get(int index, boolean safe) {
        CSTNode element = get(index);

        if (element == null && safe) {
            element = Token.NULL;
        }

        return element;
    }

    /**
     * Returns the root of the node.  By convention, all nodes have
     * a Token as the first element (or root), which indicates the type
     * of the node.  May return null if the node <code>isEmpty()</code>.
     */
    public abstract Token getRoot();

    /**
     * Returns the root of the node, the Token that indicates it's
     * type.  Returns a Token.NULL if safe and the actual root is null.
     */
    public Token getRoot(boolean safe) {
        Token root = getRoot();

        if (root == null && safe) {
            root = Token.NULL;
        }

        return root;
    }

    /**
     * Returns the text of the root.  Uses <code>getRoot(true)</code>
     * to get the root, so you will only receive null in return if the
     * root token returns it.
     */
    public String getRootText() {
        Token root = getRoot(true);
        return root.getText();
    }

    /**
     * Returns a description of the node.
     */
    public String getDescription() {
        return Types.getDescription(getMeaning());
    }

    /**
     * Returns the starting line of the node.  Returns -1
     * if not known.
     */
    public int getStartLine() {
        return getRoot(true).getStartLine();
    }

    /**
     * Returns the starting column of the node.  Returns -1
     * if not known.
     */
    public int getStartColumn() {
        return getRoot(true).getStartColumn();
    }

    /**
     * Marks the node a complete expression.  Not all nodes support this operation!
     */
    public void markAsExpression() {
        throw new GroovyBugError("markAsExpression() not supported for this CSTNode type");
    }

    /**
     * Returns true if the node is a complete expression.
     */
    public boolean isAnExpression() {
        return isA(Types.SIMPLE_EXPRESSION);
    }

    //---------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Adds an element to the node.  Returns the element for convenience.
     * Not all nodes support this operation!
     */
    public CSTNode add(CSTNode element) {
        throw new GroovyBugError("add() not supported for this CSTNode type");
    }

    /**
     * Adds all children of the specified node to this one.  Not all
     * nodes support this operation!
     */
    public void addChildrenOf(CSTNode of) {
        for (int i = 1; i < of.size(); i++) {
            add(of.get(i));
        }
    }

    /**
     * Sets an element node in at the specified index.  Returns the element
     * for convenience.  Not all nodes support this operation!
     */
    public CSTNode set(int index, CSTNode element) {
        throw new GroovyBugError("set() not supported for this CSTNode type");
    }

    /**
     * Creates a <code>Reduction</code> from this node.  Returns self if the
     * node is already a <code>Reduction</code>.
     */
    public abstract Reduction asReduction();

    //---------------------------------------------------------------------------
    // STRING CONVERSION

    /**
     * Formats the node as a <code>String</code> and returns it.
     */
    public String toString() {
        Writer string = new StringBuilderWriter();
        write(new PrintWriter(string));

        return string.toString();
    }

    /**
     * Formats the node and writes it to the specified <code>Writer</code>.
     */
    public void write(PrintWriter writer) {
        write(writer, "");
    }

    /**
     * Formats the node and writes it to the specified <code>Writer</code>.
     * The indent is prepended to each output line, and is increased for each
     * recursion.
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

        if (indent.length() > 0) {
            writer.println(")");
        } else {
            writer.print(")");
        }
    }
}
