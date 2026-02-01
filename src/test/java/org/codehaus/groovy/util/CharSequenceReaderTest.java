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
package org.codehaus.groovy.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CharSequenceReader}.
 */
class CharSequenceReaderTest {

    @Test
    void testReadSingleCharacter() {
        CharSequenceReader reader = new CharSequenceReader("abc");
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('c', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    void testReadIntoArray() {
        CharSequenceReader reader = new CharSequenceReader("hello world");
        char[] buffer = new char[5];
        int count = reader.read(buffer, 0, 5);
        assertEquals(5, count);
        assertArrayEquals("hello".toCharArray(), buffer);
    }

    @Test
    void testReadIntoArrayWithOffset() {
        CharSequenceReader reader = new CharSequenceReader("test");
        char[] buffer = new char[10];
        buffer[0] = 'x';
        buffer[1] = 'y';
        int count = reader.read(buffer, 2, 4);
        assertEquals(4, count);
        assertEquals('x', buffer[0]);
        assertEquals('y', buffer[1]);
        assertEquals('t', buffer[2]);
        assertEquals('e', buffer[3]);
        assertEquals('s', buffer[4]);
        assertEquals('t', buffer[5]);
    }

    @Test
    void testReadBeyondEnd() {
        CharSequenceReader reader = new CharSequenceReader("ab");
        char[] buffer = new char[5];
        int count = reader.read(buffer, 0, 5);
        assertEquals(2, count);
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);

        count = reader.read(buffer, 0, 5);
        assertEquals(-1, count);
    }

    @Test
    void testMarkAndReset() {
        CharSequenceReader reader = new CharSequenceReader("abcdef");
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        reader.mark(100);
        assertEquals('c', reader.read());
        assertEquals('d', reader.read());
        reader.reset();
        assertEquals('c', reader.read());
        assertEquals('d', reader.read());
    }

    @Test
    void testMarkSupported() {
        CharSequenceReader reader = new CharSequenceReader("test");
        assertTrue(reader.markSupported());
    }

    @Test
    void testSkip() {
        CharSequenceReader reader = new CharSequenceReader("abcdefgh");
        assertEquals('a', reader.read());
        long skipped = reader.skip(3);
        assertEquals(3, skipped);
        assertEquals('e', reader.read());
    }

    @Test
    void testSkipBeyondEnd() {
        CharSequenceReader reader = new CharSequenceReader("abc");
        long skipped = reader.skip(10);
        assertEquals(3, skipped);
        assertEquals(-1, reader.read());
    }

    @Test
    void testSkipNegativeThrows() {
        CharSequenceReader reader = new CharSequenceReader("test");
        assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
    }

    @Test
    void testSkipAtEnd() {
        CharSequenceReader reader = new CharSequenceReader("a");
        reader.read();
        long skipped = reader.skip(5);
        assertEquals(-1, skipped);
    }

    @Test
    void testClose() {
        CharSequenceReader reader = new CharSequenceReader("test");
        reader.read();
        reader.read();
        reader.mark(10);
        reader.close();
        // After close, reader resets to start and mark is cleared
        assertEquals('t', reader.read());
    }

    @Test
    void testToString() {
        String input = "hello world";
        CharSequenceReader reader = new CharSequenceReader(input);
        assertEquals(input, reader.toString());
    }

    @Test
    void testNullInput() {
        CharSequenceReader reader = new CharSequenceReader(null);
        assertEquals(-1, reader.read());
        assertEquals("", reader.toString());
    }

    @Test
    void testEmptyInput() {
        CharSequenceReader reader = new CharSequenceReader("");
        assertEquals(-1, reader.read());
    }

    @Test
    void testReadArrayWithNullThrows() {
        CharSequenceReader reader = new CharSequenceReader("test");
        assertThrows(NullPointerException.class, () -> reader.read(null, 0, 1));
    }

    @Test
    void testReadArrayWithInvalidBoundsThrows() {
        CharSequenceReader reader = new CharSequenceReader("test");
        char[] buffer = new char[5];
        assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, -1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 3, 5));
    }

    @Test
    void testStringBuilder() {
        StringBuilder sb = new StringBuilder("StringBuilder content");
        CharSequenceReader reader = new CharSequenceReader(sb);
        char[] buffer = new char[13];
        reader.read(buffer, 0, 13);
        assertEquals("StringBuilder", new String(buffer));
    }

    @Test
    void testZeroLengthRead() {
        CharSequenceReader reader = new CharSequenceReader("test");
        char[] buffer = new char[5];
        int count = reader.read(buffer, 0, 0);
        assertEquals(0, count);
    }
}
