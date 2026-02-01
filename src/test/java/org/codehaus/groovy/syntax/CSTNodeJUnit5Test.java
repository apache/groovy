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
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for CSTNode class (via Token and Reduction implementations).
 */
class CSTNodeJUnit5Test {

    @Test
    void testTokenAsCSTNode() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        
        assertNotNull(token);
        assertEquals(Types.PLUS, token.getType());
        assertEquals(Types.PLUS, token.getMeaning());
    }

    @Test
    void testGetMeaning() {
        Token token = Token.newSymbol(Types.STAR, 1, 1);
        assertEquals(Types.STAR, token.getMeaning());
    }

    @Test
    void testSetMeaning() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        CSTNode result = token.setMeaning(Types.MINUS);
        
        assertEquals(Types.MINUS, token.getMeaning());
        assertSame(token, result);
    }

    @Test
    void testGetType() {
        Token token = Token.newIdentifier("test", 1, 1);
        assertEquals(Types.IDENTIFIER, token.getType());
    }

    @Test
    void testCanMean() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        
        assertTrue(token.canMean(Types.PLUS));
    }

    @Test
    void testIsA() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertTrue(token.isA(Types.PLUS));
    }

    @Test
    void testIsOneOf() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        
        int[] types = {Types.MINUS, Types.PLUS, Types.STAR};
        assertTrue(token.isOneOf(types));
        
        int[] otherTypes = {Types.MINUS, Types.STAR};
        assertFalse(token.isOneOf(otherTypes));
    }

    @Test
    void testIsAllOf() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        
        int[] types = {Types.PLUS};
        assertTrue(token.isAllOf(types));
    }

    @Test
    void testGetMeaningAs() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        
        int[] types = {Types.MINUS, Types.PLUS};
        assertEquals(Types.PLUS, token.getMeaningAs(types));
        
        int[] otherTypes = {Types.MINUS, Types.STAR};
        assertEquals(Types.UNKNOWN, token.getMeaningAs(otherTypes));
    }

    @Test
    void testIsEmpty() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertFalse(token.isEmpty());
        
        // Token.NULL is not actually "empty" - it's a valid token with type UNKNOWN
        assertFalse(Token.NULL.isEmpty());
    }

    @Test
    void testSize() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertEquals(1, token.size());
    }

    @Test
    void testHasChildren() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertFalse(token.hasChildren());
        
        Reduction reduction = new Reduction(token);
        assertFalse(reduction.hasChildren());
        
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        assertTrue(reduction.hasChildren());
    }

    @Test
    void testChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        assertEquals(0, reduction.children());
        
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        assertEquals(1, reduction.children());
    }

    @Test
    void testGet() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        Token child = Token.newSymbol(Types.MINUS, 2, 2);
        reduction.add(child);
        
        assertNotNull(reduction.get(0));
        assertSame(child, reduction.get(1));
        assertNull(reduction.get(99));
    }

    @Test
    void testGetSafe() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        
        CSTNode safeMissing = reduction.get(99, true);
        assertSame(Token.NULL, safeMissing);
        
        CSTNode unsafeMissing = reduction.get(99, false);
        assertNull(unsafeMissing);
    }

    @Test
    void testGetRoot() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertSame(token, token.getRoot());
        
        Reduction reduction = new Reduction(token);
        assertSame(token, reduction.getRoot());
    }

    @Test
    void testGetRootSafe() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertSame(token, token.getRoot(true));
        assertSame(token, token.getRoot(false));
    }

    @Test
    void testGetRootText() {
        Token token = Token.newIdentifier("myvar", 1, 1);
        assertEquals("myvar", token.getRootText());
    }

    @Test
    void testGetDescription() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        String desc = token.getDescription();
        assertNotNull(desc);
    }

    @Test
    void testGetStartLine() {
        Token token = Token.newSymbol(Types.PLUS, 5, 10);
        assertEquals(5, token.getStartLine());
    }

    @Test
    void testGetStartColumn() {
        Token token = Token.newSymbol(Types.PLUS, 5, 10);
        assertEquals(10, token.getStartColumn());
    }

    @Test
    void testMarkAsExpressionOnToken() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertThrows(GroovyBugError.class, () -> token.markAsExpression());
    }

    @Test
    void testIsAnExpression() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertFalse(token.isAnExpression());
    }

    @Test
    void testAddOnToken() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertThrows(GroovyBugError.class, () -> token.add(Token.NULL));
    }

    @Test
    void testSetOnToken() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertThrows(GroovyBugError.class, () -> token.set(0, Token.NULL));
    }

    @Test
    void testReductionAdd() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        Token child = Token.newSymbol(Types.MINUS, 2, 2);
        
        CSTNode result = reduction.add(child);
        assertSame(child, result);
        assertEquals(2, reduction.size());
    }

    @Test
    void testReductionSet() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        Token child1 = Token.newSymbol(Types.MINUS, 2, 2);
        Token child2 = Token.newSymbol(Types.STAR, 3, 3);
        
        reduction.add(child1);
        reduction.set(1, child2);
        
        assertSame(child2, reduction.get(1));
    }

    @Test
    void testAddChildrenOf() {
        Reduction source = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        source.add(Token.newSymbol(Types.MINUS, 2, 2));
        source.add(Token.newSymbol(Types.STAR, 3, 3));
        
        Reduction target = new Reduction(Token.newSymbol(Types.DIVIDE, 4, 4));
        target.addChildrenOf(source);
        
        assertEquals(3, target.size()); // root + 2 children
    }

    @Test
    void testAsReduction() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        Reduction reduction = token.asReduction();
        
        assertNotNull(reduction);
        assertSame(token, reduction.getRoot());
        
        // Calling asReduction on Reduction returns self
        Reduction existingReduction = new Reduction(token);
        assertSame(existingReduction, existingReduction.asReduction());
    }

    @Test
    void testToString() {
        Token token = Token.newIdentifier("test", 1, 5);
        String str = token.toString();
        assertNotNull(str);
        assertTrue(str.contains("IDENTIFIER") || str.contains("test"));
    }

    @Test
    void testWriteToWriter() {
        Token token = Token.newIdentifier("test", 1, 5);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        token.write(pw);
        pw.flush();
        
        String output = sw.toString();
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    @Test
    void testWriteWithChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        reduction.write(pw);
        pw.flush();
        
        String output = sw.toString();
        assertNotNull(output);
    }

    @Test
    void testTokenNullConstant() {
        Token nullToken = Token.NULL;
        
        // Token.NULL is not empty - it's a valid token with type UNKNOWN
        assertFalse(nullToken.isEmpty());
        assertEquals(Types.UNKNOWN, nullToken.getType());
    }

    @Test
    void testMatches() {
        Token token = Token.newSymbol(Types.PLUS, 1, 1);
        assertTrue(token.matches(Types.PLUS));
        assertFalse(token.matches(Types.MINUS));
    }

    @Test
    void testMatchesWithChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        
        assertTrue(reduction.matches(Types.PLUS, Types.MINUS));
        assertFalse(reduction.matches(Types.PLUS, Types.STAR));
    }

    @Test
    void testMatchesTwoChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        reduction.add(Token.newSymbol(Types.STAR, 3, 3));
        
        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR));
    }

    @Test
    void testMatchesThreeChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        reduction.add(Token.newSymbol(Types.STAR, 3, 3));
        reduction.add(Token.newSymbol(Types.DIVIDE, 4, 4));
        
        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR, Types.DIVIDE));
    }

    @Test
    void testMatchesFourChildren() {
        Reduction reduction = new Reduction(Token.newSymbol(Types.PLUS, 1, 1));
        reduction.add(Token.newSymbol(Types.MINUS, 2, 2));
        reduction.add(Token.newSymbol(Types.STAR, 3, 3));
        reduction.add(Token.newSymbol(Types.DIVIDE, 4, 4));
        reduction.add(Token.newSymbol(Types.MOD, 5, 5));
        
        assertTrue(reduction.matches(Types.PLUS, Types.MINUS, Types.STAR, Types.DIVIDE, Types.MOD));
    }
}
