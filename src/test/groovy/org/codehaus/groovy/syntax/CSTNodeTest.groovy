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

import org.codehaus.groovy.GroovyBugError
import org.junit.jupiter.api.Test

import java.io.PrintWriter
import java.io.StringWriter

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for CSTNode class (via Token and Reduction implementations).
 */
class CSTNodeTest {

    @Test
    void testTokenAsCSTNode() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)

        assertNotNull(token)
        assertEquals(Types.PLUS, token.getType())
        assertEquals(Types.PLUS, token.getMeaning())
    }

    @Test
    void testGetMeaning() {
        def token = Token.newSymbol(Types.STAR, 1, 1)
        assertEquals(Types.STAR, token.getMeaning())
    }

    @Test
    void testSetMeaning() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        def result = token.setMeaning(Types.MINUS)

        assertEquals(Types.MINUS, token.getMeaning())
        assertSame(token, result)
    }

    @Test
    void testGetType() {
        def token = Token.newIdentifier("test", 1, 1)
        assertEquals(Types.IDENTIFIER, token.getType())
    }

    @Test
    void testCanMean() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)

        assertTrue(token.canMean(Types.PLUS))
    }

    @Test
    void testIsA() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertTrue(token.isA(Types.PLUS))
    }

    @Test
    void testIsOneOf() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)

        int[] types = [Types.MINUS, Types.PLUS, Types.STAR]
        assertTrue(token.isOneOf(types))

        int[] otherTypes = [Types.MINUS, Types.STAR]
        assertFalse(token.isOneOf(otherTypes))
    }

    @Test
    void testIsAllOf() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)

        int[] types = [Types.PLUS]
        assertTrue(token.isAllOf(types))
    }

    @Test
    void testGetMeaningAs() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)

        int[] types = [Types.MINUS, Types.PLUS]
        assertEquals(Types.PLUS, token.getMeaningAs(types))

        int[] otherTypes = [Types.MINUS, Types.STAR]
        assertEquals(Types.UNKNOWN, token.getMeaningAs(otherTypes))
    }

    @Test
    void testIsEmpty() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertFalse(token.isEmpty())

        // Token.NULL is not actually "empty" - it's a valid token with type UNKNOWN
        assertFalse(Token.NULL.isEmpty())
    }

    @Test
    void testSize() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertEquals(1, token.size())
    }

    @Test
    void testHasChildren() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertFalse(token.hasChildren())

        def reduction = new Reduction(token)
        assertFalse(reduction.hasChildren())

        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))
        assertTrue(reduction.hasChildren())
    }

    @Test
    void testChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        assertEquals(0, reduction.children())

        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))
        assertEquals(1, reduction.children())
    }

    @Test
    void testGet() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        def child = Token.newSymbol(Types.MINUS, 2, 2)
        reduction.add(child)

        assertNotNull(reduction.get(0))
        assertSame(child, reduction.get(1))
        assertNull(reduction.get(99))
    }

    @Test
    void testGetSafe() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))

        def safeMissing = reduction.get(99, true)
        assertSame(Token.NULL, safeMissing)

        def unsafeMissing = reduction.get(99, false)
        assertNull(unsafeMissing)
    }

    @Test
    void testGetRoot() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertSame(token, token.getRoot())

        def reduction = new Reduction(token)
        assertSame(token, reduction.getRoot())
    }

    @Test
    void testGetRootSafe() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertSame(token, token.getRoot(true))
        assertSame(token, token.getRoot(false))
    }

    @Test
    void testGetRootText() {
        def token = Token.newIdentifier("myvar", 1, 1)
        assertEquals("myvar", token.getRootText())
    }

    @Test
    void testGetDescription() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        def desc = token.getDescription()
        assertNotNull(desc)
    }

    @Test
    void testGetStartLine() {
        def token = Token.newSymbol(Types.PLUS, 5, 10)
        assertEquals(5, token.getStartLine())
    }

    @Test
    void testGetStartColumn() {
        def token = Token.newSymbol(Types.PLUS, 5, 10)
        assertEquals(10, token.getStartColumn())
    }

    @Test
    void testMarkAsExpressionOnToken() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertThrows(GroovyBugError, () -> token.markAsExpression())
    }

    @Test
    void testIsAnExpression() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertFalse(token.isAnExpression())
    }

    @Test
    void testAddOnToken() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertThrows(GroovyBugError, () -> token.add(Token.NULL))
    }

    @Test
    void testSetOnToken() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertThrows(GroovyBugError, () -> token.set(0, Token.NULL))
    }

    @Test
    void testReductionAdd() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        def child = Token.newSymbol(Types.MINUS, 2, 2)

        def result = reduction.add(child)
        assertSame(child, result)
        assertEquals(2, reduction.size())
    }

    @Test
    void testReductionSet() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        def child1 = Token.newSymbol(Types.MINUS, 2, 2)
        def child2 = Token.newSymbol(Types.STAR, 3, 3)

        reduction.add(child1)
        reduction.set(1, child2)

        assertSame(child2, reduction.get(1))
    }

    @Test
    void testAddChildrenOf() {
        def source = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        source.add(Token.newSymbol(Types.MINUS, 2, 2))
        source.add(Token.newSymbol(Types.STAR, 3, 3))

        def target = new Reduction(Token.newSymbol(Types.DIVIDE, 4, 4))
        target.addChildrenOf(source)

        assertEquals(3, target.size()) // root + 2 children
    }

    @Test
    void testAsReduction() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        def reduction = token.asReduction()

        assertNotNull(reduction)
        assertSame(token, reduction.getRoot())

        // Calling asReduction on Reduction returns self
        def existingReduction = new Reduction(token)
        assertSame(existingReduction, existingReduction.asReduction())
    }

    @Test
    void testToString() {
        def token = Token.newIdentifier("test", 1, 5)
        def str = token.toString()
        assertNotNull(str)
        assertTrue(str.contains("IDENTIFIER") || str.contains("test"))
    }

    @Test
    void testWriteToWriter() {
        def token = Token.newIdentifier("test", 1, 5)
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)

        token.write(pw)
        pw.flush()

        def output = sw.toString()
        assertNotNull(output)
        assertTrue(output.length() > 0)
    }

    @Test
    void testWriteWithChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)

        reduction.write(pw)
        pw.flush()

        def output = sw.toString()
        assertNotNull(output)
    }

    @Test
    void testTokenNullConstant() {
        def nullToken = Token.NULL

        // Token.NULL is not empty - it's a valid token with type UNKNOWN
        assertFalse(nullToken.isEmpty())
        assertEquals(Types.UNKNOWN, nullToken.getType())
    }

    @Test
    void testMatches() {
        def token = Token.newSymbol(Types.PLUS, 1, 1)
        assertTrue(token.matches(Types.PLUS))
        assertFalse(token.matches(Types.MINUS))
    }

    @Test
    void testMatchesWithChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))

        assertTrue(reduction.matches(Types.PLUS, Types.MINUS))
        assertFalse(reduction.matches(Types.PLUS, Types.STAR))
    }

    @Test
    void testMatchesTwoChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))
        reduction.add(Token.newSymbol(Types.STAR, 3, 3))

        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR))
    }

    @Test
    void testMatchesThreeChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))
        reduction.add(Token.newSymbol(Types.STAR, 3, 3))
        reduction.add(Token.newSymbol(Types.DIVIDE, 4, 4))

        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR, Types.DIVIDE))
    }

    @Test
    void testMatchesFourChildren() {
        def reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1))
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2))
        reduction.add(Token.newSymbol(Types.STAR, 3, 3))
        reduction.add(Token.newSymbol(Types.DIVIDE, 4, 4))
        reduction.add(Token.newSymbol(Types.MOD, 5, 5))

        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR, Types.DIVIDE, Types.MOD))
    }
}
