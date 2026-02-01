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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Reduction}.
 */
class ReductionJUnit5Test {

    @Test
    void testConstructorWithToken() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        assertEquals(1, reduction.size());
        assertSame(root, reduction.getRoot());
    }

    @Test
    void testEmptyReduction() {
        assertTrue(Reduction.EMPTY.isEmpty());
        assertEquals(0, Reduction.EMPTY.size());
        assertNull(Reduction.EMPTY.getRoot());
    }

    @Test
    void testNewContainer() {
        Reduction container = Reduction.newContainer();
        assertFalse(container.isEmpty());
        assertEquals(1, container.size());
        assertSame(Token.NULL, container.getRoot());
    }

    @Test
    void testAdd() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        Token child = new Token(Types.INTEGER_NUMBER, "5", 1, 3);
        reduction.add(child);
        
        assertEquals(2, reduction.size());
        assertSame(child, reduction.get(1));
    }

    @Test
    void testAddMultiple() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        Token child1 = new Token(Types.INTEGER_NUMBER, "5", 1, 3);
        Token child2 = new Token(Types.INTEGER_NUMBER, "3", 1, 5);
        reduction.add(child1);
        reduction.add(child2);
        
        assertEquals(3, reduction.size());
        assertSame(child1, reduction.get(1));
        assertSame(child2, reduction.get(2));
    }

    @Test
    void testSetAtIndex() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        Token child = new Token(Types.INTEGER_NUMBER, "5", 1, 3);
        reduction.set(1, child);
        
        assertEquals(2, reduction.size());
        assertSame(child, reduction.get(1));
    }

    @Test
    void testSetAtGapIndex() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        Token child = new Token(Types.INTEGER_NUMBER, "5", 1, 3);
        reduction.set(5, child);
        
        assertEquals(6, reduction.size());
        assertNull(reduction.get(1));
        assertNull(reduction.get(2));
        assertNull(reduction.get(3));
        assertNull(reduction.get(4));
        assertSame(child, reduction.get(5));
    }

    @Test
    void testSetNonTokenAsRootThrows() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        Reduction nestedReduction = Reduction.newContainer();
        
        assertThrows(GroovyBugError.class, () -> reduction.set(0, nestedReduction));
    }

    @Test
    void testRemove() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        Token child1 = new Token(Types.INTEGER_NUMBER, "5", 1, 3);
        Token child2 = new Token(Types.INTEGER_NUMBER, "3", 1, 5);
        reduction.add(child1);
        reduction.add(child2);
        
        CSTNode removed = reduction.remove(1);
        
        assertSame(child1, removed);
        assertEquals(2, reduction.size());
        assertSame(child2, reduction.get(1));
    }

    @Test
    void testRemoveRootThrows() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        assertThrows(GroovyBugError.class, () -> reduction.remove(0));
    }

    @Test
    void testGetBeyondSize() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        assertNull(reduction.get(10));
    }

    @Test
    void testMarkAsExpression() {
        // Use PLUS operator which is not a complex expression type by default
        Token root = Token.newSymbol(Types.PLUS, 1, 1);
        Reduction reduction = new Reduction(root);
        
        assertFalse(reduction.isAnExpression());
        reduction.markAsExpression();
        assertTrue(reduction.isAnExpression());
    }

    @Test
    void testIsAnExpressionWithComplexExpression() {
        Token root = Token.newSymbol(Types.LEFT_PARENTHESIS, 1, 1);
        Reduction reduction = new Reduction(root);
        // Types.LEFT_PARENTHESIS is categorized as a complex expression type
        // Test the behavior
        assertFalse(reduction.isAnExpression());
    }

    @Test
    void testAsReduction() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        assertSame(reduction, reduction.asReduction());
    }

    @Test
    void testSize() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        assertEquals(1, reduction.size());
        
        reduction.add(new Token(Types.INTEGER_NUMBER, "1", 1, 3));
        assertEquals(2, reduction.size());
        
        reduction.add(new Token(Types.INTEGER_NUMBER, "2", 1, 5));
        assertEquals(3, reduction.size());
    }

    @Test
    void testHasChildren() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        assertFalse(reduction.hasChildren());
        
        reduction.add(new Token(Types.INTEGER_NUMBER, "1", 1, 3));
        assertTrue(reduction.hasChildren());
    }

    @Test
    void testChildren() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        assertEquals(0, reduction.children());
        
        reduction.add(new Token(Types.INTEGER_NUMBER, "1", 1, 3));
        assertEquals(1, reduction.children());
        
        reduction.add(new Token(Types.INTEGER_NUMBER, "2", 1, 5));
        assertEquals(2, reduction.children());
    }

    @Test
    void testGetWithSafe() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        // Safe mode returns Token.NULL for out of bounds
        CSTNode result = reduction.get(10, true);
        assertSame(Token.NULL, result);
        
        // Regular mode returns null
        assertNull(reduction.get(10, false));
    }

    @Test
    void testGetRoot() {
        Token root = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction = new Reduction(root);
        
        assertSame(root, reduction.getRoot());
        assertSame(root, reduction.getRoot(false));
        assertSame(root, reduction.getRoot(true));
    }

    @Test
    void testEmptyReductionGetRootSafe() {
        // Safe mode on empty reduction
        Token result = Reduction.EMPTY.getRoot(true);
        assertSame(Token.NULL, result);
    }

    @Test
    void testAddChildrenOf() {
        Token root1 = new Token(Types.PLUS, "+", 1, 1);
        Reduction reduction1 = new Reduction(root1);
        
        Token root2 = new Token(Types.MINUS, "-", 1, 1);
        Reduction reduction2 = new Reduction(root2);
        Token child1 = new Token(Types.INTEGER_NUMBER, "1", 1, 3);
        Token child2 = new Token(Types.INTEGER_NUMBER, "2", 1, 5);
        reduction2.add(child1);
        reduction2.add(child2);
        
        reduction1.addChildrenOf(reduction2);
        
        assertEquals(3, reduction1.size());
        assertSame(child1, reduction1.get(1));
        assertSame(child2, reduction1.get(2));
    }
}
