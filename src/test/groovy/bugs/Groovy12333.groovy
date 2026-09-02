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
package bugs

import groovy.util.regex.BalancedGroup
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12333: findBalancedGroups resolves nesting depth without recursing over each subtree.
 *
 * find() assembles the tree bottom-up, so a node is attached to a new parent once per enclosing
 * delimiter. Assigning depth eagerly re-walked the whole subtree on every attach, which is
 * quadratic in the nesting and recursed one frame per level.
 */
final class Groovy12333 {

    private static String chain(int depth) {
        '(' * depth + ')' * depth
    }

    private static BalancedGroup innermost(BalancedGroup group) {
        BalancedGroup node = group
        while (!node.children.isEmpty()) node = node.children[0]
        node
    }

    /** Runs on a deliberately small stack, so recursion per nesting level shows up as a failure. */
    private static void onSmallStack(Closure body) {
        Throwable failure = null
        Thread thread = new Thread(null, { try { body() } catch (Throwable t) { failure = t } }, 'small-stack', 128 * 1024)
        thread.start()
        thread.join()
        if (failure != null) throw failure
    }

    @Test
    void testDeeplyNestedInputResolvesWithoutRecursingPerLevel() {
        // depth 3000 overflows a 128KB stack once anything walks the tree a frame at a time
        onSmallStack {
            int depth = 3000
            List<BalancedGroup> roots = chain(depth).findBalancedGroups(/\(/, /\)/)

            assertEquals(1, roots.size())
            assertEquals(0, roots[0].depth)
            assertEquals(depth - 1, innermost(roots[0]).depth)
        }
    }

    @Test
    void testDeeplyNestedInputDoesNotRetainOverlappingCopies() {
        // Groups nest, so their spans overlap; a copy of its own text per node made retention
        // quadratic in the nesting -- 64KB of nesting held ~990MB, and enough of it exhausted the
        // heap before anything could read the result.
        int depth = 50000
        List<BalancedGroup> roots = chain(depth).findBalancedGroups(/\(/, /\)/)

        assertEquals(1, roots.size())
        assertEquals(depth * 2, roots[0].matchedString.length())
        assertEquals('()', innermost(roots[0]).matchedString)
    }

    @Test
    void testMatchedStringIsSlicedFromTheSource() {
        BalancedGroup root = 'noise (a(b)c) noise'.findBalancedGroups(/\(/, /\)/)[0]

        assertEquals('(a(b)c)', root.matchedString)
        assertEquals('(b)', root.children[0].matchedString)
        // repeated reads are equal but need not be the same instance
        assertEquals(root.matchedString, root.matchedString)
        assertEquals(6, root.start)
        assertEquals(13, root.end)
    }

    @Test
    void testDepthIsCorrectAtEveryLevel() {
        BalancedGroup node = '((((a))))'.findBalancedGroups(/\(/, /\)/)[0]
        for (int expected = 0; expected < 4; expected++) {
            assertEquals(expected, node.depth)
            if (expected < 3) node = node.children[0]
        }
        assertTrue(node.children.isEmpty())
    }

    @Test
    void testDepthIsStableWhenReadRepeatedlyAndOutOfOrder() {
        // resolution caches into the nodes it walks past, so order of access must not matter
        BalancedGroup root = chain(500).findBalancedGroups(/\(/, /\)/)[0]
        BalancedGroup deep = innermost(root)

        assertEquals(499, deep.depth)   // deepest first, resolving the whole chain
        assertEquals(0, root.depth)
        assertEquals(499, deep.depth)   // and again, from the cache
        assertEquals(1, root.children[0].depth)
    }

    @Test
    void testSiblingsShareDepthIndependently() {
        List<BalancedGroup> roots = '(a)((b))(c)'.findBalancedGroups(/\(/, /\)/)

        assertEquals(3, roots.size())
        roots.each { assertEquals(0, it.depth) }
        assertEquals(1, roots[1].children[0].depth)
        assertTrue(roots[0].children.isEmpty())
    }

    @Test
    void testDepthAfterOrphanPromotion() {
        // dangling opener: completed children are promoted outward, so their depth changes
        List<BalancedGroup> roots = '(A (B) (C(D)'.findBalancedGroups(/\(/, /\)/)

        assertTrue(roots.size() >= 1)
        roots.each { assertEquals(0, it.depth) }
    }
}
