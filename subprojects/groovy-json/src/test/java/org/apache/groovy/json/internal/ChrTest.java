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
package org.apache.groovy.json.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Chr class (char array utilities).
 */
class ChrTest {

    @Test
    void testArray() {
        char[] result = Chr.array('a', 'b', 'c');
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test
    void testArrayEmpty() {
        char[] result = Chr.array();
        assertEquals(0, result.length);
    }

    @Test
    void testArraySingleChar() {
        char[] result = Chr.array('x');
        assertArrayEquals(new char[]{'x'}, result);
    }

    @Test
    void testChars() {
        char[] result = Chr.chars("hello");
        assertArrayEquals(new char[]{'h', 'e', 'l', 'l', 'o'}, result);
    }

    @Test
    void testCharsEmpty() {
        char[] result = Chr.chars("");
        assertEquals(0, result.length);
    }

    @Test
    void testInCharFound() {
        char[] array = {'a', 'b', 'c', 'd'};
        assertTrue(Chr.in('b', array));
        assertTrue(Chr.in('a', array));
        assertTrue(Chr.in('d', array));
    }

    @Test
    void testInCharNotFound() {
        char[] array = {'a', 'b', 'c'};
        assertFalse(Chr.in('x', array));
        assertFalse(Chr.in('z', array));
    }

    @Test
    void testInIntFound() {
        char[] array = {'a', 'b', 'c'};
        assertTrue(Chr.in((int) 'a', array));
        assertTrue(Chr.in((int) 'b', array));
    }

    @Test
    void testInIntNotFound() {
        char[] array = {'a', 'b', 'c'};
        assertFalse(Chr.in((int) 'x', array));
    }

    @Test
    void testInWithOffset() {
        char[] array = {'a', 'b', 'c', 'd', 'e'};
        // Search from offset 2 (starting at 'c')
        assertTrue(Chr.in('c', 2, array));
        assertTrue(Chr.in('d', 2, array));
        assertTrue(Chr.in('e', 2, array));
        assertFalse(Chr.in('a', 2, array));
        assertFalse(Chr.in('b', 2, array));
    }

    @Test
    void testInWithOffsetAndEnd() {
        char[] array = {'a', 'b', 'c', 'd', 'e'};
        // Search from offset 1 to 3 (searching 'b', 'c')
        assertTrue(Chr.in('b', 1, 3, array));
        assertTrue(Chr.in('c', 1, 3, array));
        assertFalse(Chr.in('a', 1, 3, array));
        assertFalse(Chr.in('d', 1, 3, array));
    }

    @Test
    void testGrowWithSize() {
        char[] array = {'a', 'b', 'c'};
        char[] grown = Chr.grow(array, 5);
        
        assertEquals(8, grown.length);
        assertEquals('a', grown[0]);
        assertEquals('b', grown[1]);
        assertEquals('c', grown[2]);
    }

    @Test
    void testGrowDouble() {
        char[] array = {'a', 'b', 'c'};
        char[] grown = Chr.grow(array);
        
        assertEquals(6, grown.length);
        assertEquals('a', grown[0]);
        assertEquals('b', grown[1]);
        assertEquals('c', grown[2]);
    }

    @Test
    void testCopy() {
        char[] array = {'h', 'e', 'l', 'l', 'o'};
        char[] copy = Chr.copy(array);
        
        assertArrayEquals(array, copy);
        assertNotSame(array, copy);
    }

    @Test
    void testCopyWithOffsetAndLength() {
        char[] array = {'h', 'e', 'l', 'l', 'o'};
        char[] copy = Chr.copy(array, 1, 3);
        
        assertArrayEquals(new char[]{'e', 'l', 'l'}, copy);
    }

    @Test
    void testAddChar() {
        char[] array = {'a', 'b'};
        char[] result = Chr.add(array, 'c');
        
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test
    void testAddString() {
        char[] array = {'h', 'i'};
        char[] result = Chr.add(array, " there");
        
        assertArrayEquals(new char[]{'h', 'i', ' ', 't', 'h', 'e', 'r', 'e'}, result);
    }

    @Test
    void testAddStringBuilder() {
        char[] array = {'a', 'b'};
        StringBuilder sb = new StringBuilder("cd");
        char[] result = Chr.add(array, sb);
        
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd'}, result);
    }

    @Test
    void testAddTwoArrays() {
        char[] array1 = {'a', 'b'};
        char[] array2 = {'c', 'd', 'e'};
        char[] result = Chr.add(array1, array2);
        
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e'}, result);
    }

    @Test
    void testAddMultipleArrays() {
        char[] arr1 = {'a', 'b'};
        char[] arr2 = {'c'};
        char[] arr3 = {'d', 'e', 'f'};
        
        char[] result = Chr.add(arr1, arr2, arr3);
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, result);
    }

    @Test
    void testAddMultipleArraysWithNull() {
        char[] arr1 = {'a', 'b'};
        char[] arr2 = null;
        char[] arr3 = {'c', 'd'};
        
        char[] result = Chr.add(arr1, arr2, arr3);
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd'}, result);
    }

    @Test
    void testLpad() {
        char[] input = {'1', '2', '3'};
        char[] result = Chr.lpad(input, 6, '0');
        
        assertArrayEquals(new char[]{'0', '0', '0', '1', '2', '3'}, result);
    }

    @Test
    void testLpadNoChange() {
        char[] input = {'1', '2', '3'};
        char[] result = Chr.lpad(input, 3, '0');
        
        assertSame(input, result);
    }

    @Test
    void testLpadSmallerSize() {
        char[] input = {'1', '2', '3'};
        char[] result = Chr.lpad(input, 2, '0');
        
        assertSame(input, result);
    }

    @Test
    void testContains() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        
        assertTrue(Chr.contains(chars, 'b', 0, 5));
        assertTrue(Chr.contains(chars, 'a', 0, 5));
        assertTrue(Chr.contains(chars, 'e', 0, 5));
        assertFalse(Chr.contains(chars, 'x', 0, 5));
    }

    @Test
    void testContainsWithStartAndLength() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        
        // Only search within 'b', 'c', 'd' (start=1, length=3)
        assertTrue(Chr.contains(chars, 'b', 1, 3));
        assertTrue(Chr.contains(chars, 'c', 1, 3));
        assertTrue(Chr.contains(chars, 'd', 1, 3));
        assertFalse(Chr.contains(chars, 'a', 1, 3));
        assertFalse(Chr.contains(chars, 'e', 1, 3));
    }

    @Test
    void testIdxWithByteArray() {
        char[] buffer = new char[10];
        byte[] bytes = {65, 66, 67}; // A, B, C
        
        Chr._idx(buffer, 2, bytes);
        
        assertEquals('A', buffer[2]);
        assertEquals('B', buffer[3]);
        assertEquals('C', buffer[4]);
    }

    @Test
    void testIdxWithCharArray() {
        char[] buffer = new char[10];
        char[] input = {'x', 'y', 'z'};
        
        Chr._idx(buffer, 3, input);
        
        assertEquals('x', buffer[3]);
        assertEquals('y', buffer[4]);
        assertEquals('z', buffer[5]);
    }

    @Test
    void testIdxWithCharArrayAndLength() {
        char[] buffer = new char[10];
        char[] input = {'x', 'y', 'z', 'w'};
        
        Chr._idx(buffer, 1, input, 2);
        
        assertEquals('x', buffer[1]);
        assertEquals('y', buffer[2]);
        assertEquals(0, buffer[3]); // Not written
    }

    @Test
    void testIdxWithByteArrayRange() {
        char[] buffer = new char[10];
        byte[] bytes = {65, 66, 67, 68, 69}; // A, B, C, D, E
        
        // Copy bytes[1] to bytes[3] (B, C, D) starting at buffer[0]
        Chr._idx(buffer, 0, bytes, 1, 4);
        
        assertEquals('B', buffer[0]);
        assertEquals('C', buffer[1]);
        assertEquals('D', buffer[2]);
    }

    @Test
    void testInEmptyArray() {
        char[] array = {};
        assertFalse(Chr.in('a', array));
    }

    @Test
    void testGrowEmptyArray() {
        char[] array = {};
        char[] grown = Chr.grow(array, 5);
        assertEquals(5, grown.length);
    }

    @Test
    void testCopyEmptyArray() {
        char[] array = {};
        char[] copy = Chr.copy(array);
        assertEquals(0, copy.length);
    }

    @Test
    void testAddToEmptyArray() {
        char[] array = {};
        char[] result = Chr.add(array, 'a');
        assertArrayEquals(new char[]{'a'}, result);
    }

    @Test
    void testAddEmptyString() {
        char[] array = {'a', 'b'};
        char[] result = Chr.add(array, "");
        assertArrayEquals(new char[]{'a', 'b'}, result);
    }

    @Test
    void testCharsUnicode() {
        char[] result = Chr.chars("日本語");
        assertEquals(3, result.length);
        assertEquals('日', result[0]);
        assertEquals('本', result[1]);
        assertEquals('語', result[2]);
    }

    @Test
    void testAddEmptyArrays() {
        char[] arr1 = {};
        char[] arr2 = {};
        char[] result = Chr.add(arr1, arr2);
        assertEquals(0, result.length);
    }
}
