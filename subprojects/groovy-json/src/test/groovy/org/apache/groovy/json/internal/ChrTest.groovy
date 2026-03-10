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
package org.apache.groovy.json.internal

import org.junit.jupiter.api.Test

import static org.apache.groovy.json.internal.Chr.copy
import static org.apache.groovy.json.internal.Chr.grow
import static org.apache.groovy.json.internal.Chr.lpad
import static org.junit.jupiter.api.Assertions.assertArrayEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue


class ChrTest {

    @Test
    void testLpad() {
        def results = lpad('abc'.toCharArray(), 5, '#' as char)

        assertArrayEquals(
                '##abc'.toCharArray(),
                results
        )
    }

    @Test
    void testIsIn() {
        def letters = 'abcd'.toCharArray()

        assertTrue Chr.in('a' as char, letters)
        assertFalse Chr.in('z' as char, letters)
    }

    @Test
    void testIsInAtOffset() {
        def letters = 'abcd'.toCharArray()

        assertFalse Chr.in('a' as char, 1, letters)
        assertTrue Chr.in('c' as char, 1, letters)
    }

    @Test
    void testIsInAtRange() {
        def letters = 'abcd'.toCharArray()

        assertFalse Chr.in('a' as char, 1, 2, letters)
        assertTrue Chr.in('c' as char, 1, 3, letters)
    }

    @Test
    void testGrow() {
        def letters = 'abcde'.toCharArray()
        letters = grow(letters, 21)

        assertEquals(
                'e' as char,
                letters[4]
        )

        assertEquals(
                'a' as char,
                letters[0]
        )

        assertEquals(
                26,
                letters.length
        )

        assertEquals(
                '\0' as char,
                letters[20]
        )
    }

    @Test
    void testGrowFast() {
        def letters = 'abcdef'.toCharArray()
        letters = grow(letters)

        assertEquals(
                'e' as char,
                letters[4]
        )

        assertEquals(
                'a' as char,
                letters[0]
        )

        assertEquals(
                12,
                letters.length
        )

        assertEquals(
                letters[9],
                '\0' as char
        )
    }

    @Test
    void testCopy() {
        def chars = 'abcde'.toCharArray()

        assertArrayEquals(
                chars,
                copy(chars)
        )
    }

    @Test
    void testByteCopyIntoCharArray() {
        def charArray = new char[1000]
        def bytes = "0123456789000".bytes

        Chr._idx(charArray, 0, bytes)

        char ch = charArray[0]
        assert ch == '0'

        ch = charArray[9]
        assert ch == '9'

        Chr._idx(charArray, 100, bytes, 0, 3)

        ch = charArray[100]
        assert ch == '0'

        ch = charArray[101]
        assert ch == '1'

        ch = charArray[102]
        assert ch == '2'

        Chr._idx(charArray, 200, bytes, 3, 6)

        ch = charArray[200]
        assert ch == '3' || die(" not '3' " + ch)

        ch = charArray[201]
        assert ch == '4' || die(" not '4' " + ch)

        ch = charArray[202]
        assert ch == '5' || die(" not '5' " + ch)
    }

    // --- Tests merged from Java ChrTest ---

    @Test
    void testArray() {
        char[] result = Chr.array('a' as char, 'b' as char, 'c' as char)
        assertArrayEquals(['a', 'b', 'c'] as char[], result)
    }

    @Test
    void testArrayEmpty() {
        char[] result = Chr.array()
        assertEquals(0, result.length)
    }

    @Test
    void testArraySingleChar() {
        char[] result = Chr.array('x' as char)
        assertArrayEquals(['x'] as char[], result)
    }

    @Test
    void testChars() {
        char[] result = Chr.chars("hello")
        assertArrayEquals(['h', 'e', 'l', 'l', 'o'] as char[], result)
    }

    @Test
    void testCharsEmpty() {
        char[] result = Chr.chars("")
        assertEquals(0, result.length)
    }

    @Test
    void testInCharFound() {
        char[] array = ['a', 'b', 'c', 'd'] as char[]
        assertTrue(Chr.in('b' as char, array))
        assertTrue(Chr.in('a' as char, array))
        assertTrue(Chr.in('d' as char, array))
    }

    @Test
    void testInCharNotFound() {
        char[] array = ['a', 'b', 'c'] as char[]
        assertFalse(Chr.in('x' as char, array))
        assertFalse(Chr.in('z' as char, array))
    }

    @Test
    void testInIntFound() {
        char[] array = ['a', 'b', 'c'] as char[]
        assertTrue(Chr.in((int) 'a', array))
        assertTrue(Chr.in((int) 'b', array))
    }

    @Test
    void testInIntNotFound() {
        char[] array = ['a', 'b', 'c'] as char[]
        assertFalse(Chr.in((int) 'x', array))
    }

    @Test
    void testInWithOffset() {
        char[] array = ['a', 'b', 'c', 'd', 'e'] as char[]
        assertTrue(Chr.in('c' as char, 2, array))
        assertTrue(Chr.in('d' as char, 2, array))
        assertTrue(Chr.in('e' as char, 2, array))
        assertFalse(Chr.in('a' as char, 2, array))
        assertFalse(Chr.in('b' as char, 2, array))
    }

    @Test
    void testInWithOffsetAndEnd() {
        char[] array = ['a', 'b', 'c', 'd', 'e'] as char[]
        assertTrue(Chr.in('b' as char, 1, 3, array))
        assertTrue(Chr.in('c' as char, 1, 3, array))
        assertFalse(Chr.in('a' as char, 1, 3, array))
        assertFalse(Chr.in('d' as char, 1, 3, array))
    }

    @Test
    void testGrowWithSize() {
        char[] array = ['a', 'b', 'c'] as char[]
        char[] grown = Chr.grow(array, 5)

        assertEquals(8, grown.length)
        assertEquals('a' as char, grown[0])
        assertEquals('b' as char, grown[1])
        assertEquals('c' as char, grown[2])
    }

    @Test
    void testGrowDouble() {
        char[] array = ['a', 'b', 'c'] as char[]
        char[] grown = Chr.grow(array)

        assertEquals(6, grown.length)
        assertEquals('a' as char, grown[0])
        assertEquals('b' as char, grown[1])
        assertEquals('c' as char, grown[2])
    }

    @Test
    void testCopyNotSame() {
        char[] array = ['h', 'e', 'l', 'l', 'o'] as char[]
        char[] copied = Chr.copy(array)

        assertArrayEquals(array, copied)
        assertNotSame(array, copied)
    }

    @Test
    void testCopyWithOffsetAndLength() {
        char[] array = ['h', 'e', 'l', 'l', 'o'] as char[]
        char[] copied = Chr.copy(array, 1, 3)

        assertArrayEquals(['e', 'l', 'l'] as char[], copied)
    }

    @Test
    void testAddChar() {
        char[] array = ['a', 'b'] as char[]
        char[] result = Chr.add(array, 'c' as char)

        assertArrayEquals(['a', 'b', 'c'] as char[], result)
    }

    @Test
    void testAddString() {
        char[] array = ['h', 'i'] as char[]
        char[] result = Chr.add(array, " there")

        assertArrayEquals(['h', 'i', ' ', 't', 'h', 'e', 'r', 'e'] as char[], result)
    }

    @Test
    void testAddStringBuilder() {
        char[] array = ['a', 'b'] as char[]
        StringBuilder sb = new StringBuilder("cd")
        char[] result = Chr.add(array, sb)

        assertArrayEquals(['a', 'b', 'c', 'd'] as char[], result)
    }

    @Test
    void testAddTwoArrays() {
        char[] array1 = ['a', 'b'] as char[]
        char[] array2 = ['c', 'd', 'e'] as char[]
        char[] result = Chr.add(array1, array2)

        assertArrayEquals(['a', 'b', 'c', 'd', 'e'] as char[], result)
    }

    @Test
    void testAddMultipleArrays() {
        char[] arr1 = ['a', 'b'] as char[]
        char[] arr2 = ['c'] as char[]
        char[] arr3 = ['d', 'e', 'f'] as char[]

        char[] result = Chr.add(arr1, arr2, arr3)
        assertArrayEquals(['a', 'b', 'c', 'd', 'e', 'f'] as char[], result)
    }

    @Test
    void testAddMultipleArraysWithNull() {
        char[] arr1 = ['a', 'b'] as char[]
        char[] arr2 = null
        char[] arr3 = ['c', 'd'] as char[]

        char[] result = Chr.add(arr1, arr2, arr3)
        assertArrayEquals(['a', 'b', 'c', 'd'] as char[], result)
    }

    @Test
    void testLpadNumeric() {
        char[] input = ['1', '2', '3'] as char[]
        char[] result = Chr.lpad(input, 6, '0' as char)

        assertArrayEquals(['0', '0', '0', '1', '2', '3'] as char[], result)
    }

    @Test
    void testLpadNoChange() {
        char[] input = ['1', '2', '3'] as char[]
        char[] result = Chr.lpad(input, 3, '0' as char)

        assertSame(input, result)
    }

    @Test
    void testLpadSmallerSize() {
        char[] input = ['1', '2', '3'] as char[]
        char[] result = Chr.lpad(input, 2, '0' as char)

        assertSame(input, result)
    }

    @Test
    void testContains() {
        char[] chars = ['a', 'b', 'c', 'd', 'e'] as char[]

        assertTrue(Chr.contains(chars, 'b' as char, 0, 5))
        assertTrue(Chr.contains(chars, 'a' as char, 0, 5))
        assertTrue(Chr.contains(chars, 'e' as char, 0, 5))
        assertFalse(Chr.contains(chars, 'x' as char, 0, 5))
    }

    @Test
    void testContainsWithStartAndLength() {
        char[] chars = ['a', 'b', 'c', 'd', 'e'] as char[]

        assertTrue(Chr.contains(chars, 'b' as char, 1, 3))
        assertTrue(Chr.contains(chars, 'c' as char, 1, 3))
        assertTrue(Chr.contains(chars, 'd' as char, 1, 3))
        assertFalse(Chr.contains(chars, 'a' as char, 1, 3))
        assertFalse(Chr.contains(chars, 'e' as char, 1, 3))
    }

    @Test
    void testIdxWithByteArray() {
        char[] buffer = new char[10]
        byte[] bytes = [65, 66, 67] as byte[]

        Chr._idx(buffer, 2, bytes)

        assertEquals('A' as char, buffer[2])
        assertEquals('B' as char, buffer[3])
        assertEquals('C' as char, buffer[4])
    }

    @Test
    void testIdxWithCharArray() {
        char[] buffer = new char[10]
        char[] input = ['x', 'y', 'z'] as char[]

        Chr._idx(buffer, 3, input)

        assertEquals('x' as char, buffer[3])
        assertEquals('y' as char, buffer[4])
        assertEquals('z' as char, buffer[5])
    }

    @Test
    void testIdxWithCharArrayAndLength() {
        char[] buffer = new char[10]
        char[] input = ['x', 'y', 'z', 'w'] as char[]

        Chr._idx(buffer, 1, input, 2)

        assertEquals('x' as char, buffer[1])
        assertEquals('y' as char, buffer[2])
        assertEquals(0 as char, buffer[3])
    }

    @Test
    void testIdxWithByteArrayRange() {
        char[] buffer = new char[10]
        byte[] bytes = [65, 66, 67, 68, 69] as byte[]

        Chr._idx(buffer, 0, bytes, 1, 4)

        assertEquals('B' as char, buffer[0])
        assertEquals('C' as char, buffer[1])
        assertEquals('D' as char, buffer[2])
    }

    @Test
    void testInEmptyArray() {
        char[] array = [] as char[]
        assertFalse(Chr.in('a' as char, array))
    }

    @Test
    void testGrowEmptyArray() {
        char[] array = [] as char[]
        char[] grown = Chr.grow(array, 5)
        assertEquals(5, grown.length)
    }

    @Test
    void testCopyEmptyArray() {
        char[] array = [] as char[]
        char[] copied = Chr.copy(array)
        assertEquals(0, copied.length)
    }

    @Test
    void testAddToEmptyArray() {
        char[] array = [] as char[]
        char[] result = Chr.add(array, 'a' as char)
        assertArrayEquals(['a'] as char[], result)
    }

    @Test
    void testAddEmptyString() {
        char[] array = ['a', 'b'] as char[]
        char[] result = Chr.add(array, "")
        assertArrayEquals(['a', 'b'] as char[], result)
    }

    @Test
    void testCharsUnicode() {
        char[] result = Chr.chars("日本語")
        assertEquals(3, result.length)
        assertEquals('日' as char, result[0])
        assertEquals('本' as char, result[1])
        assertEquals('語' as char, result[2])
    }

    @Test
    void testAddEmptyArrays() {
        char[] arr1 = [] as char[]
        char[] arr2 = [] as char[]
        char[] result = Chr.add(arr1, arr2)
        assertEquals(0, result.length)
    }

    protected assertArrayEquals(char[] expected, char[] actual) {
        assertArrayEquals((Object[]) expected, (Object[]) actual)
    }
}
