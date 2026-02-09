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
package org.codehaus.groovy.util

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for FastArray class.
 */
class FastArrayTest {

    @Test
    void testDefaultConstructor() {
        def fa = new FastArray()
        assertEquals(0, fa.size())
        assertTrue(fa.isEmpty())
    }

    @Test
    void testConstructorWithCapacity() {
        def fa = new FastArray(100)
        assertEquals(0, fa.size())
        assertTrue(fa.isEmpty())
    }

    @Test
    void testConstructorWithCollection() {
        def list = ["a", "b", "c"]
        def fa = new FastArray(list)
        assertEquals(3, fa.size())
        assertEquals("a", fa.get(0))
        assertEquals("b", fa.get(1))
        assertEquals("c", fa.get(2))
    }

    @Test
    void testConstructorWithArray() {
        Object[] array = ["x", "y", "z"]
        def fa = new FastArray(array)
        assertEquals(3, fa.size())
        assertEquals("x", fa.get(0))
    }

    @Test
    void testAdd() {
        def fa = new FastArray()
        fa.add("first")
        assertEquals(1, fa.size())
        assertEquals("first", fa.get(0))

        fa.add("second")
        assertEquals(2, fa.size())
        assertEquals("second", fa.get(1))
    }

    @Test
    void testAddWithGrowth() {
        def fa = new FastArray(2)
        fa.add("1")
        fa.add("2")
        fa.add("3") // This should trigger growth

        assertEquals(3, fa.size())
        assertEquals("3", fa.get(2))
    }

    @Test
    void testAddFromEmptyArray() {
        def fa = new FastArray(0)
        fa.add("item")
        assertEquals(1, fa.size())
        assertEquals("item", fa.get(0))
    }

    @Test
    void testGet() {
        def fa = new FastArray()
        fa.add("zero")
        fa.add("one")
        fa.add("two")

        assertEquals("zero", fa.get(0))
        assertEquals("one", fa.get(1))
        assertEquals("two", fa.get(2))
    }

    @Test
    void testSet() {
        def fa = new FastArray()
        fa.add("original")

        fa.set(0, "modified")
        assertEquals("modified", fa.get(0))
    }

    @Test
    void testSize() {
        def fa = new FastArray()
        assertEquals(0, fa.size())

        fa.add("a")
        assertEquals(1, fa.size())

        fa.add("b")
        fa.add("c")
        assertEquals(3, fa.size())
    }

    @Test
    void testClear() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")
        fa.add("c")
        assertEquals(3, fa.size())

        fa.clear()
        assertEquals(0, fa.size())
        assertTrue(fa.isEmpty())
    }

    @Test
    void testIsEmpty() {
        def fa = new FastArray()
        assertTrue(fa.isEmpty())

        fa.add("item")
        assertFalse(fa.isEmpty())

        fa.clear()
        assertTrue(fa.isEmpty())
    }

    @Test
    void testAddAllFastArray() {
        def fa1 = new FastArray()
        fa1.add("a")
        fa1.add("b")

        def fa2 = new FastArray()
        fa2.add("c")
        fa2.add("d")

        fa1.addAll(fa2)

        assertEquals(4, fa1.size())
        assertEquals("a", fa1.get(0))
        assertEquals("b", fa1.get(1))
        assertEquals("c", fa1.get(2))
        assertEquals("d", fa1.get(3))
    }

    @Test
    void testAddAllEmptyFastArray() {
        def fa1 = new FastArray()
        fa1.add("a")

        def fa2 = new FastArray()

        fa1.addAll(fa2)
        assertEquals(1, fa1.size())
    }

    @Test
    void testAddAllList() {
        def fa = new FastArray()
        fa.add("first")

        def list = ["second", "third"]
        fa.addAll(list)

        assertEquals(3, fa.size())
        assertEquals("second", fa.get(1))
        assertEquals("third", fa.get(2))
    }

    @Test
    void testAddAllObjectArray() {
        def fa = new FastArray()
        fa.add("start")

        Object[] array = ["middle", "end"]
        fa.addAll(array, array.length)

        assertEquals(3, fa.size())
    }

    @Test
    void testCopy() {
        def original = new FastArray()
        original.add("a")
        original.add("b")
        original.add("c")

        def copy = original.copy()

        assertEquals(original.size(), copy.size())
        assertEquals(original.get(0), copy.get(0))
        assertEquals(original.get(1), copy.get(1))
        assertEquals(original.get(2), copy.get(2))

        // Verify it's a real copy, not the same reference
        copy.set(0, "modified")
        assertEquals("a", original.get(0))
        assertEquals("modified", copy.get(0))
    }

    @Test
    void testRemove() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")
        fa.add("c")

        fa.remove(1)

        assertEquals(2, fa.size())
        assertEquals("a", fa.get(0))
        assertEquals("c", fa.get(1))
    }

    @Test
    void testRemoveFirst() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")
        fa.add("c")

        fa.remove(0)

        assertEquals(2, fa.size())
        assertEquals("b", fa.get(0))
    }

    @Test
    void testRemoveLast() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")
        fa.add("c")

        fa.remove(2)

        assertEquals(2, fa.size())
        assertEquals("b", fa.get(1))
    }

    @Test
    void testToListEmpty() {
        def fa = new FastArray()
        def list = fa.toList()
        assertTrue(list.isEmpty())
    }

    @Test
    void testToListSingle() {
        def fa = new FastArray()
        fa.add("only")

        def list = fa.toList()
        assertEquals(1, list.size())
        assertEquals("only", list.get(0))
    }

    @Test
    void testToListMultiple() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")
        fa.add("c")

        def list = fa.toList()
        assertEquals(3, list.size())
        assertEquals("a", list.get(0))
        assertEquals("b", list.get(1))
        assertEquals("c", list.get(2))
    }

    @Test
    void testGetArray() {
        def fa = new FastArray()
        fa.add("x")
        fa.add("y")

        def array = fa.getArray()
        assertNotNull(array)
        assertEquals("x", array[0])
        assertEquals("y", array[1])
    }

    @Test
    void testToStringEmpty() {
        def fa = new FastArray()
        assertEquals("[]", fa.toString())
    }

    @Test
    void testToStringNonEmpty() {
        def fa = new FastArray()
        fa.add("a")
        fa.add("b")

        def str = fa.toString()
        assertTrue(str.contains("a"))
        assertTrue(str.contains("b"))
    }

    @Test
    void testClone() {
        def original = new FastArray()
        original.add("1")
        original.add("2")

        def clone = original.clone()

        assertEquals(original.size(), clone.size())
        assertEquals(original.get(0), clone.get(0))

        // Verify it's independent
        clone.add("3")
        assertEquals(2, original.size())
        assertEquals(3, clone.size())
    }

    @Test
    void testEmptyList() {
        assertNotNull(FastArray.EMPTY_LIST)
        assertEquals(0, FastArray.EMPTY_LIST.size())
        assertTrue(FastArray.EMPTY_LIST.isEmpty())
    }

    @Test
    void testAddNull() {
        def fa = new FastArray()
        fa.add(null)
        assertEquals(1, fa.size())
        assertNull(fa.get(0))
    }

    @Test
    void testMixedTypes() {
        def fa = new FastArray()
        fa.add("string")
        fa.add(42)
        fa.add(3.14)
        fa.add(true)

        assertEquals(4, fa.size())
        assertEquals("string", fa.get(0))
        assertEquals(42, fa.get(1))
        assertEquals(3.14, fa.get(2))
        assertEquals(true, fa.get(3))
    }
}
