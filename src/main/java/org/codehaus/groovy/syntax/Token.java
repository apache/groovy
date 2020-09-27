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

/**
 * A <code>CSTNode</code> produced by the <code>Lexer</code>.
 *
 * @see Reduction
 * @see Types
 */
public class Token extends CSTNode {
    public static final Token NULL = new Token();
    public static final Token EOF = new Token(Types.EOF, "", -1, -1);

    //---------------------------------------------------------------------------
    // TOKEN INITIALIZATION AND SUCH

    private int type = Types.UNKNOWN;  // the actual type identified by the lexer
    private int meaning = Types.UNKNOWN;  // an interpretation applied to the token after the fact

    private String text = "";             // the text of the token
    private int startLine = -1;             // the source line on which the token begins
    private int startColumn = -1;             // the source column on which the token begins

    /**
     * Initializes the Token with the specified information.
     */
    public Token(int type, String text, int startLine, int startColumn) {
        this.type = type;
        this.meaning = type;
        this.text = text;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    /**
     * Initializes the NULL Token.
     */
    private Token() {
    }

    /**
     * Returns a copy of this Token.
     */
    public Token dup() {
        Token token = new Token(this.type, this.text, this.startLine, this.startColumn);
        token.setMeaning(this.meaning);

        return token;
    }

    //---------------------------------------------------------------------------
    // NODE IDENTIFICATION AND MEANING

    /**
     * Returns the meaning of this node.  If the node isEmpty(), returns
     * the type of Token.NULL.
     */
    public int getMeaning() {
        return meaning;
    }

    /**
     * Sets the meaning for this node (and its root Token).  Not
     * valid if the node isEmpty().  Returns this token, for
     * convenience.
     */
    public CSTNode setMeaning(int meaning) {
        this.meaning = meaning;
        return this;
    }

    /**
     * Returns the actual type of the node.  If the node isEmpty(), returns
     * the type of Token.NULL.
     */
    public int getType() {
        return type;
    }

    //---------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns the number of elements in the node (including root).
     */
    public int size() {
        return 1;
    }

    /**
     * Returns the specified element, or null.
     */
    public CSTNode get(int index) {
        if (index > 0) {
            throw new GroovyBugError("attempt to access Token element other than root");
        }

        return this;
    }

    /**
     * Returns the root of the node.  By convention, all nodes have
     * a Token as the first element (or root), which indicates the type
     * of the node.  May return null if the node <code>isEmpty()</code>.
     */
    public Token getRoot() {
        return this;
    }

    /**
     * Returns the text of the root node.  Uses <code>getRoot(true)</code>
     * to get the root, so you will only receive null in return if the
     * root token returns it.
     */
    public String getRootText() {
        return text;
    }

    /**
     * Returns the text of the token.  Equivalent to
     * <code>getRootText()</code> when called directly.
     */
    public String getText() {
        return text;
    }

    /**
     * Not advisable, but if you need to adjust the token's text, this
     * will do it.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the starting line of the node.  Returns -1
     * if not known.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the starting column of the node.  Returns -1
     * if not known.
     */
    public int getStartColumn() {
        return startColumn;
    }

    //---------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Creates a <code>Reduction</code> from this token.  Returns self if the
     * node is already a <code>Reduction</code>.
     */
    public Reduction asReduction() {
        return new Reduction(this);
    }

    /**
     * Creates a <code>Reduction</code> from this token, adding the supplied
     * node as the second element.
     */
    public Reduction asReduction(CSTNode second) {
        Reduction created = asReduction();
        created.add(second);
        return created;
    }

    /**
     * Creates a <code>Reduction</code> from this token, adding the supplied
     * nodes as the second and third element, respectively.
     */
    public Reduction asReduction(CSTNode second, CSTNode third) {
        Reduction created = asReduction(second);
        created.add(third);
        return created;
    }

    /**
     * Creates a <code>Reduction</code> from this token, adding the supplied
     * nodes as the second, third, and fourth element, respectively.
     */
    public Reduction asReduction(CSTNode second, CSTNode third, CSTNode fourth) {
        Reduction created = asReduction(second, third);
        created.add(fourth);
        return created;
    }

    //---------------------------------------------------------------------------
    // TOKEN FACTORIES

    /**
     * Creates a token that represents a keyword.  Returns null if the
     * specified text isn't a keyword.
     */
    public static Token newKeyword(String text, int startLine, int startColumn) {

        int type = Types.lookupKeyword(text);
        if (type != Types.UNKNOWN) {
            return new Token(type, text, startLine, startColumn);
        }

        return null;
    }

    /**
     * Creates a token that represents a double-quoted string.
     */
    public static Token newString(String text, int startLine, int startColumn) {
        return new Token(Types.STRING, text, startLine, startColumn);
    }

    /**
     * Creates a token that represents an identifier.
     */
    public static Token newIdentifier(String text, int startLine, int startColumn) {
        return new Token(Types.IDENTIFIER, text, startLine, startColumn);
    }

    /**
     * Creates a token that represents an integer.
     */
    public static Token newInteger(String text, int startLine, int startColumn) {
        return new Token(Types.INTEGER_NUMBER, text, startLine, startColumn);
    }

    /**
     * Creates a token that represents a decimal number.
     */
    public static Token newDecimal(String text, int startLine, int startColumn) {
        return new Token(Types.DECIMAL_NUMBER, text, startLine, startColumn);
    }

    /**
     * Creates a token that represents a symbol, using a library for the text.
     */
    public static Token newSymbol(int type, int startLine, int startColumn) {
        return new Token(type, Types.getText(type), startLine, startColumn);
    }

    /**
     * Creates a token that represents a symbol, using a library for the type.
     */
    public static Token newSymbol(String type, int startLine, int startColumn) {
        return new Token(Types.lookupSymbol(type), type, startLine, startColumn);
    }

    /**
     * Creates a token with the specified meaning.
     */
    public static Token newPlaceholder(int type) {
        Token token = new Token(Types.UNKNOWN, "", -1, -1);
        token.setMeaning(type);

        return token;
    }

}
