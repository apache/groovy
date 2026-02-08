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

import static org.apache.groovy.json.internal.Chr.*

import groovy.test.GroovyTestCase

class ChrTest extends GroovyTestCase {

    void testLpad() {
        def results = lpad('abc'.toCharArray(), 5, '#' as char)

        assertArrayEquals(
                '##abc'.toCharArray(),
                results
        )
    }

    void testIsIn() {
        def letters = 'abcd'.toCharArray()

        assertTrue Chr.in('a' as char, letters)
        assertFalse Chr.in('z' as char, letters)
    }

    void testIsInAtOffset() {
        def letters = 'abcd'.toCharArray()

        assertFalse Chr.in('a' as char, 1, letters)
        assertTrue Chr.in('c' as char, 1, letters)
    }

    void testIsInAtRange() {
        def letters = 'abcd'.toCharArray()

        assertFalse Chr.in('a' as char, 1, 2, letters)
        assertTrue Chr.in('c' as char, 1, 3, letters)
    }

    void testGrow() {
        def letters = 'abcde'.toCharArray()
        letters = grow(letters, 21)

        assertEquals(
                'e',
                letters[4]
        )

        assertEquals(
                'a',
                letters[0]
        )

        assertEquals(
                26,
                letters.length
        )

        assertEquals(
                '\0',
                letters[20]
        )
    }

    void testGrowFast() {
        def letters = 'abcdef'.toCharArray()
        letters = grow(letters)

        assertEquals(
                'e',
                letters[4]
        )

        assertEquals(
                'a',
                letters[0]
        )

        assertEquals(
                12,
                letters.length
        )

        assertEquals(
                letters[9],
                '\0'
        )
    }

    void testCopy() {
        def chars = 'abcde'.toCharArray()

        assertArrayEquals(
                chars,
                copy(chars)
        )
    }

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

    protected assertArrayEquals(char[] expected, char[] actual) {
        assertArrayEquals((Object[]) expected, (Object[]) actual)
    }
}
