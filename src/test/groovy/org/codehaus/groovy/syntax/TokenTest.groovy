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
package org.codehaus.groovy.syntax

import groovy.test.GroovyTestCase

/**
 * Unit tests for {@link Token}.
 */
class TokenTest extends GroovyTestCase {

    private static final int LINE = 11
    private static final int COLUMN = 33

    void testNothing() {
    }

    void testConstruct() {
        def token = new Token(42, "forty-two", LINE, COLUMN)

        assertEquals(42, token.getType())
        assertEquals("forty-two", token.getText())
        assertEquals(LINE, token.getStartLine())
        assertEquals(COLUMN, token.getStartColumn())
    }

    void testGetMeaning() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        assertEquals(Types.PLUS, token.getMeaning())
    }

    void testSetMeaning() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        token.setMeaning(Types.MINUS)
        assertEquals(Types.MINUS, token.getMeaning())
        assertEquals(Types.PLUS, token.getType()) // Type unchanged
    }

    void testEofTokenImmutable() {
        def eof = Token.EOF
        eof.setMeaning(Types.PLUS)
        assertEquals(Types.EOF, eof.getMeaning()) // Should not change
        eof.setText("changed")
        assertEquals("", eof.getText()) // Should not change
    }

    void testNullTokenImmutable() {
        def nullToken = Token.NULL
        nullToken.setMeaning(Types.PLUS)
        assertEquals(Types.UNKNOWN, nullToken.getMeaning()) // Should not change
        nullToken.setText("changed")
        assertEquals("", nullToken.getText()) // Should not change
    }

    void testDup() {
        def original = new Token(Types.IDENTIFIER, "myVar", LINE, COLUMN)
        original.setMeaning(Types.LEFT_SQUARE_BRACKET)

        def copy = original.dup()

        assertEquals(original.getType(), copy.getType())
        assertEquals(original.getText(), copy.getText())
        assertEquals(original.getMeaning(), copy.getMeaning())
        assertEquals(original.getStartLine(), copy.getStartLine())
        assertEquals(original.getStartColumn(), copy.getStartColumn())

        // Verify they are different objects
        assertNotSame(original, copy)
    }

    void testSize() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        assertEquals(1, token.size())
    }

    void testGetReturnsThis() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        assertSame(token, token.get(0))
    }

    void testGetWithInvalidIndexThrows() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        shouldFail(org.codehaus.groovy.GroovyBugError) { token.get(1) }
    }

    void testGetRoot() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        assertSame(token, token.getRoot())
    }

    void testGetRootText() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        assertEquals("+", token.getRootText())
    }

    void testAsReduction() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        def reduction = token.asReduction()
        assertNotNull(reduction)
        assertEquals(1, reduction.size())
        assertSame(token, reduction.getRoot())
    }

    void testAsReductionWithSecond() {
        def token = new Token(Types.PLUS, "+", LINE, COLUMN)
        def second = new Token(Types.INTEGER_NUMBER, "5", LINE, COLUMN + 1)
        def reduction = token.asReduction(second)
        assertEquals(2, reduction.size())
        assertSame(token, reduction.getRoot())
        assertSame(second, reduction.get(1))
    }

    void testAsReductionWithThird() {
        def first = new Token(Types.PLUS, "+", LINE, COLUMN)
        def second = new Token(Types.INTEGER_NUMBER, "5", LINE, COLUMN + 1)
        def third = new Token(Types.INTEGER_NUMBER, "3", LINE, COLUMN + 3)
        def reduction = first.asReduction(second, third)
        assertEquals(3, reduction.size())
        assertSame(first, reduction.getRoot())
        assertSame(second, reduction.get(1))
        assertSame(third, reduction.get(2))
    }

    void testAsReductionWithFourth() {
        def first = new Token(Types.PLUS, "+", LINE, COLUMN)
        def second = new Token(Types.INTEGER_NUMBER, "5", LINE, COLUMN + 1)
        def third = new Token(Types.INTEGER_NUMBER, "3", LINE, COLUMN + 3)
        def fourth = new Token(Types.INTEGER_NUMBER, "7", LINE, COLUMN + 5)
        def reduction = first.asReduction(second, third, fourth)
        assertEquals(4, reduction.size())
    }

    void testNewKeyword() {
        def ifToken = Token.newKeyword("if", LINE, COLUMN)
        assertNotNull(ifToken)
        assertEquals(Types.KEYWORD_IF, ifToken.getType())
        assertEquals("if", ifToken.getText())
    }

    void testNewKeywordNotKeyword() {
        def notKeyword = Token.newKeyword("notAKeyword", LINE, COLUMN)
        assertNull(notKeyword)
    }

    void testNewString() {
        def stringToken = Token.newString("hello", LINE, COLUMN)
        assertEquals(Types.STRING, stringToken.getType())
        assertEquals("hello", stringToken.getText())
    }

    void testNewIdentifier() {
        def idToken = Token.newIdentifier("myVariable", LINE, COLUMN)
        assertEquals(Types.IDENTIFIER, idToken.getType())
        assertEquals("myVariable", idToken.getText())
    }

    void testNewInteger() {
        def intToken = Token.newInteger("42", LINE, COLUMN)
        assertEquals(Types.INTEGER_NUMBER, intToken.getType())
        assertEquals("42", intToken.getText())
    }

    void testNewDecimal() {
        def decimalToken = Token.newDecimal("3.14", LINE, COLUMN)
        assertEquals(Types.DECIMAL_NUMBER, decimalToken.getType())
        assertEquals("3.14", decimalToken.getText())
    }

    void testNewSymbolByType() {
        def plusToken = Token.newSymbol(Types.PLUS, LINE, COLUMN)
        assertEquals(Types.PLUS, plusToken.getType())
        assertEquals("+", plusToken.getText())
    }

    void testNewSymbolByText() {
        def plusToken = Token.newSymbol("+", LINE, COLUMN)
        assertEquals(Types.PLUS, plusToken.getType())
        assertEquals("+", plusToken.getText())
    }

    void testNewPlaceholder() {
        def placeholder = Token.newPlaceholder(Types.SYNTH_METHOD)
        assertEquals(Types.UNKNOWN, placeholder.getType())
        assertEquals(Types.SYNTH_METHOD, placeholder.getMeaning())
        assertEquals("", placeholder.getText())
        assertEquals(-1, placeholder.getStartLine())
        assertEquals(-1, placeholder.getStartColumn())
    }

    void testSetText() {
        def token = new Token(Types.STRING, "original", LINE, COLUMN)
        token.setText("changed")
        assertEquals("changed", token.getText())
    }

    void testKeywords() {
        // Test a selection of keywords
        assertKeyword("class", Types.KEYWORD_CLASS)
        assertKeyword("def", Types.KEYWORD_DEF)
        assertKeyword("if", Types.KEYWORD_IF)
        assertKeyword("else", Types.KEYWORD_ELSE)
        assertKeyword("while", Types.KEYWORD_WHILE)
        assertKeyword("for", Types.KEYWORD_FOR)
        assertKeyword("return", Types.KEYWORD_RETURN)
        assertKeyword("try", Types.KEYWORD_TRY)
        assertKeyword("catch", Types.KEYWORD_CATCH)
        assertKeyword("finally", Types.KEYWORD_FINALLY)
        assertKeyword("throw", Types.KEYWORD_THROW)
        assertKeyword("new", Types.KEYWORD_NEW)
        assertKeyword("true", Types.KEYWORD_TRUE)
        assertKeyword("false", Types.KEYWORD_FALSE)
        assertKeyword("null", Types.KEYWORD_NULL)
    }

    private void assertKeyword(String text, int expectedType) {
        def token = Token.newKeyword(text, LINE, COLUMN)
        assertNotNull("Expected keyword for: " + text, token)
        assertEquals(expectedType, token.getType())
        assertEquals(text, token.getText())
    }
}
