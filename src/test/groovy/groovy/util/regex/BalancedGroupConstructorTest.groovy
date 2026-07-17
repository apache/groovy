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
package groovy.util.regex

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Package-private constructor contracts for {@link BalancedGroup}.
 * Lives in the same package so tests can exercise the package-private
 * constructors without widening them to public API.
 *
 * User-facing behaviour is covered by {@code bugs.Groovy12133}.
 */
final class BalancedGroupConstructorTest {

    @Test
    void testConstructorWiresParentAndRejectsReparenting() {
        BalancedGroup child = new BalancedGroup('(E)', null)
        BalancedGroup parent = new BalancedGroup('(D(E))', [child])

        assertSame(parent, child.parent)
        assertEquals(1, parent.children.size())
        assertSame(child, parent.children[0])
        assertEquals(0, child.start)
        assertEquals(3, child.end)
        assertEquals(0, parent.depth)
        assertEquals(1, child.depth)

        assertThrows(IllegalArgumentException, () -> new BalancedGroup('other', [child]))
        assertThrows(NullPointerException, () -> new BalancedGroup(null, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('ab', 0, 1, 0, 1, null)) // length mismatch
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', -1, 1, 0, 1, null))
    }

    @Test
    void testConstructorRejectsInvalidFullRangeAndEndBeforeStart() {
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 3, 1, 0, 1, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 0, 1, 3, 1, null))
        assertThrows(IllegalArgumentException, () -> new BalancedGroup('a', 0, 1, -1, 0, null))
        assertThrows(NullPointerException, () -> new BalancedGroup('a', 0, 1, 0, 1, [null]))
    }

    @Test
    void testConstructorAcceptsEmptyListAndAbsoluteOffsets() {
        BalancedGroup leaf = new BalancedGroup('leaf', [])
        assertTrue(leaf.children.isEmpty())
        assertEquals(0, leaf.start)
        assertEquals(4, leaf.end)
        assertEquals(0, leaf.fullStart)
        assertEquals(4, leaf.fullEnd)
        assertEquals(0, leaf.depth)

        BalancedGroup absolute = new BalancedGroup('xy', 10, 12, 8, 15, null)
        assertEquals('xy', absolute.matchedString)
        assertEquals(10, absolute.start)
        assertEquals(12, absolute.end)
        assertEquals(8, absolute.fullStart)
        assertEquals(15, absolute.fullEnd)
        assertEquals(2, absolute.length)
        assertEquals(0, absolute.depth)
    }

    @Test
    void testDepthCascadesWhenParentIsLaterAttached() {
        // Mirrors bottom-up assembly in BalancedGroup.find: inner nodes are
        // constructed first (depth relative to a temporary root), then the
        // outer node attaches them and must recompute descendant depths.
        BalancedGroup grandChild = new BalancedGroup('(E)', null)
        BalancedGroup child = new BalancedGroup('(D(E))', [grandChild])
        assertEquals(1, grandChild.depth)

        BalancedGroup root = new BalancedGroup('(A(D(E)))', [child])
        assertEquals(0, root.depth)
        assertEquals(1, child.depth)
        assertEquals(2, grandChild.depth)
        assertSame(root, child.parent)
        assertSame(child, grandChild.parent)
    }
}
