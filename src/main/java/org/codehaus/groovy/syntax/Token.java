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
 * A {@link CSTNode} produced by the Lexer during lexical analysis.
 * Represents a single token in the token stream, including its type, text content,
 * and position in the source.
 *
 * @see Reduction
 * @see Types
 */
public class Token extends CSTNode {

    /** Sentinel token indicating end-of-file. */
    public static final Token EOF = new Token(Types.EOF, "", -1, -1);
    /** Sentinel token for null/unknown positions. */
    public static final Token NULL = new Token(Types.UNKNOWN, "", -1, -1);

    //--------------------------------------------------------------------------

    /** The actual type identified by the lexer. */
    private final int type;
    /** An interpretation applied to the token after lexical analysis. */
    private int meaning;

    /** The text content of the token. */
    private String text;
    /** The source line number on which the token begins (1-based). */
    private final int startLine;
    /** The source column number on which the token begins (1-based). */
    private final int startColumn;

    /**
     * Constructs a Token with the specified type, text, and source position.
     *
     * @param type the token type from {@link Types}
     * @param text the token's text content
     * @param startLine the source line number (1-based, or -1 if unknown)
     * @param startColumn the source column number (1-based, or -1 if unknown)
     */
    public Token(final int type, final String text, final int startLine, final int startColumn) {
        this.text = text;
        this.type = type;
        this.meaning = type;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    /**
     * Creates a shallow copy of this Token, preserving type, text, position, and meaning.
     *
     * @return a new Token with the same attributes as this one
     */
    public Token dup() {
        Token token = new Token(this.type, this.text, this.startLine, this.startColumn);
        token.setMeaning(this.meaning);
        return token;
    }

    //--------------------------------------------------------------------------
    // NODE IDENTIFICATION AND MEANING

    /**
     * Returns the current meaning (interpretation) of this token.
     * May differ from {@link #getType()} if the meaning has been reassigned.
     *
     * @return the token's current meaning type
     */
    @Override
    public int getMeaning() {
        return meaning;
    }

    /**
     * Sets the meaning (interpretation) for this token.
     * Has no effect on the sentinel tokens ({@link #EOF} and {@link #NULL}).
     *
     * @param meaning the new meaning type from {@link Types}
     * @return this token, for convenience chaining
     */
    @Override
    public CSTNode setMeaning(final int meaning) {
        if (this != EOF && this != NULL)
            this.meaning = meaning;
        return this;
    }

    /**
     * Returns the actual type of this token as determined by the lexer.
     *
     * @return the token type from {@link Types}
     */
    @Override
    public int getType() {
        return type;
    }

    //--------------------------------------------------------------------------
    // MEMBER ACCESS

    /**
     * Returns the number of elements in this node, which is always 1 (the token itself).
     *
     * @return 1
     */
    @Override
    public int size() {
        return 1;
    }

    /**
     * Returns the element at the specified index.
     * Only index 0 (the token itself) is valid.
     *
     * @param index the element index
     * @return this token if index is 0
     * @throws GroovyBugError if index is greater than 0
     */
    @Override
    public CSTNode get(int index) {
        if (index > 0) {
            throw new GroovyBugError("attempt to access Token element other than root");
        }

        return this;
    }

    /**
     * Returns the root token of this node, which is always this token itself.
     *
     * @return this token
     */
    @Override
    public Token getRoot() {
        return this;
    }

    /**
     * Returns the text content of this token.
     * Equivalent to {@link #getText()}.
     *
     * @return the token's text content
     */
    @Override
    public String getRootText() {
        return text;
    }

    /**
     * Returns the text content of this token.
     *
     * @return the token's text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of this token.
     * Has no effect on the sentinel tokens ({@link #EOF} and {@link #NULL}).
     *
     * @param text the new text content
     */
    public void setText(String text) {
        if (this != EOF && this != NULL) this.text = text;
    }

    /**
     * Returns the starting line number of this token in the source.
     *
     * @return the line number (1-based), or -1 if not known
     */
    @Override
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the starting column number of this token in the source.
     *
     * @return the column number (1-based), or -1 if not known
     */
    @Override
    public int getStartColumn() {
        return startColumn;
    }

    //--------------------------------------------------------------------------
    // OPERATIONS

    /**
     * Converts this token to a {@link Reduction} with this token as the root.
     *
     * @return a new Reduction containing this token as its root element
     */
    @Override
    public Reduction asReduction() {
        return new Reduction(this);
    }

    /**
     * Converts this token to a {@link Reduction} with this token as the root
     * and the specified node as the second element.
     *
     * @param second the second element to add to the reduction
     * @return a new Reduction containing this token and the second element
     */
    public Reduction asReduction(CSTNode second) {
        Reduction created = asReduction();
        created.add(second);
        return created;
    }

    /**
     * Converts this token to a {@link Reduction} with this token as the root
     * and the specified nodes as the second and third elements.
     *
     * @param second the second element to add to the reduction
     * @param third the third element to add to the reduction
     * @return a new Reduction containing this token and the specified elements
     */
    public Reduction asReduction(CSTNode second, CSTNode third) {
        Reduction created = asReduction(second);
        created.add(third);
        return created;
    }

    /**
     * Converts this token to a {@link Reduction} with this token as the root
     * and the specified nodes as the second, third, and fourth elements.
     *
     * @param second the second element to add to the reduction
     * @param third the third element to add to the reduction
     * @param fourth the fourth element to add to the reduction
     * @return a new Reduction containing this token and the specified elements
     */
    public Reduction asReduction(CSTNode second, CSTNode third, CSTNode fourth) {
        Reduction created = asReduction(second, third);
        created.add(fourth);
        return created;
    }

    //--------------------------------------------------------------------------
    // TOKEN FACTORIES

    /**
     * Factory method to create a keyword token if the given text represents a known keyword.
     *
     * @param text the text to check as a keyword
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token with the appropriate keyword type, or {@code null} if the text is not a keyword
     */
    public static Token newKeyword(final String text, final int startLine, final int startColumn) {
        int type = Types.lookupKeyword(text);
        if (type != Types.UNKNOWN) {
            return new Token(type, text, startLine, startColumn);
        }
        return null;
    }

    /**
     * Factory method to create a string literal token.
     *
     * @param text the string content
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing a string literal
     */
    public static Token newString(final String text, final int startLine, final int startColumn) {
        return new Token(Types.STRING, text, startLine, startColumn);
    }

    /**
     * Factory method to create an identifier token.
     *
     * @param text the identifier name
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing an identifier
     */
    public static Token newIdentifier(final String text, final int startLine, final int startColumn) {
        return new Token(Types.IDENTIFIER, text, startLine, startColumn);
    }

    /**
     * Factory method to create an integer literal token.
     *
     * @param text the integer literal text (may include prefix and suffix)
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing an integer literal
     */
    public static Token newInteger(final String text, final int startLine, final int startColumn) {
        return new Token(Types.INTEGER_NUMBER, text, startLine, startColumn);
    }

    /**
     * Factory method to create a decimal number literal token.
     *
     * @param text the decimal literal text
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing a decimal number literal
     */
    public static Token newDecimal(final String text, final int startLine, final int startColumn) {
        return new Token(Types.DECIMAL_NUMBER, text, startLine, startColumn);
    }

    /**
     * Factory method to create a symbol token from a type constant.
     * The symbol text is looked up from the Types registry.
     *
     * @param type the symbol type from {@link Types}
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing a symbol
     */
    public static Token newSymbol(final int type, final int startLine, final int startColumn) {
        return new Token(type, Types.getText(type), startLine, startColumn);
    }

    /**
     * Factory method to create a symbol token from text.
     * The symbol type is looked up from the Types registry.
     *
     * @param text the symbol text (e.g., "+", "-", "{")
     * @param startLine the source line number (1-based)
     * @param startColumn the source column number (1-based)
     * @return a Token representing a symbol
     */
    public static Token newSymbol(final String text, final int startLine, final int startColumn) {
        return new Token(Types.lookupSymbol(text), text, startLine, startColumn);
    }

    /**
     * Factory method to create a placeholder token with a specific meaning but unknown text.
     * Used internally during parsing to hold semantic information.
     *
     * @param meaning the token's meaning type from {@link Types}
     * @return a placeholder Token with empty text and unknown type
     */
    public static Token newPlaceholder(final int meaning) {
        Token token = new Token(Types.UNKNOWN, "", -1, -1);
        token.setMeaning(meaning);
        return token;
    }
}
