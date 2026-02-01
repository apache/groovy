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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for CharBuf class.
 */
class CharBufTest {

    @Test
    void testCreateWithCapacity() {
        CharBuf buf = CharBuf.create(100);
        assertNotNull(buf);
        assertEquals(0, buf.length());
    }

    @Test
    void testCreateWithCharArray() {
        char[] chars = "hello".toCharArray();
        CharBuf buf = CharBuf.create(chars);
        assertNotNull(buf);
        // create() with char[] sets the capacity but location is still 0
        // Content must be added separately
        assertEquals(0, buf.length());
    }

    @Test
    void testCreateExact() {
        CharBuf buf = CharBuf.createExact(100);
        assertNotNull(buf);
    }

    @Test
    void testAddString() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello");
        assertEquals(5, buf.length());
        assertEquals("hello", buf.toString());
    }

    @Test
    void testAddStringChain() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello").add(" ").add("world");
        assertEquals("hello world", buf.toString());
    }

    @Test
    void testAddStringMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addString("test");
        assertEquals("test", buf.toString());
    }

    @Test
    void testAddInt() {
        CharBuf buf = CharBuf.create(16);
        buf.add(42);
        assertEquals("42", buf.toString());
    }

    @Test
    void testAddIntSpecialCases() {
        CharBuf buf1 = CharBuf.create(16);
        buf1.addInt(0);
        assertEquals("0", buf1.toString());

        CharBuf buf2 = CharBuf.create(16);
        buf2.addInt(1);
        assertEquals("1", buf2.toString());

        CharBuf buf3 = CharBuf.create(16);
        buf3.addInt(-1);
        assertEquals("-1", buf3.toString());
    }

    @Test
    void testAddIntCaching() {
        CharBuf buf = CharBuf.create(16);
        buf.addInt(42);
        buf.addInt(42); // Should use cached value
        assertEquals("4242", buf.toString());
    }

    @Test
    void testAddBoolean() {
        CharBuf bufTrue = CharBuf.create(16);
        bufTrue.add(true);
        assertEquals("true", bufTrue.toString());

        CharBuf bufFalse = CharBuf.create(16);
        bufFalse.add(false);
        assertEquals("false", bufFalse.toString());
    }

    @Test
    void testAddBooleanMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addBoolean(true);
        assertEquals("true", buf.toString());
    }

    @Test
    void testAddByte() {
        CharBuf buf = CharBuf.create(16);
        buf.add((byte) 42);
        assertEquals("42", buf.toString());
    }

    @Test
    void testAddByteMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addByte((byte) 10);
        assertEquals("10", buf.toString());
    }

    @Test
    void testAddShort() {
        CharBuf buf = CharBuf.create(16);
        buf.add((short) 1000);
        assertEquals("1000", buf.toString());
    }

    @Test
    void testAddShortMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addShort((short) 500);
        assertEquals("500", buf.toString());
    }

    @Test
    void testAddLong() {
        CharBuf buf = CharBuf.create(16);
        buf.add(9999999999L);
        assertEquals("9999999999", buf.toString());
    }

    @Test
    void testAddDouble() {
        CharBuf buf = CharBuf.create(16);
        buf.add(3.14);
        assertTrue(buf.toString().startsWith("3.14"));
    }

    @Test
    void testAddDoubleMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addDouble(2.5);
        buf.addDouble(2.5); // Should use caching
        String result = buf.toString();
        assertTrue(result.contains("2.5"));
    }

    @Test
    void testAddFloat() {
        CharBuf buf = CharBuf.create(16);
        buf.add(1.5f);
        assertTrue(buf.toString().contains("1.5"));
    }

    @Test
    void testAddFloatMethod() {
        CharBuf buf = CharBuf.create(16);
        buf.addFloat(2.5f);
        buf.addFloat(2.5f); // Should use caching
        assertTrue(buf.toString().contains("2.5"));
    }

    @Test
    void testAddChar() {
        CharBuf buf = CharBuf.create(16);
        buf.addChar('A');
        assertEquals("A", buf.toString());
    }

    @Test
    void testAddCharFromByte() {
        CharBuf buf = CharBuf.create(16);
        buf.addChar((byte) 65);
        assertEquals("A", buf.toString());
    }

    @Test
    void testAddCharFromInt() {
        CharBuf buf = CharBuf.create(16);
        buf.addChar(65);
        assertEquals("A", buf.toString());
    }

    @Test
    void testAddCharFromShort() {
        CharBuf buf = CharBuf.create(16);
        buf.addChar((short) 65);
        assertEquals("A", buf.toString());
    }

    @Test
    void testAddLine() {
        CharBuf buf = CharBuf.create(32);
        buf.addLine("hello");
        assertEquals("hello\n", buf.toString());
    }

    @Test
    void testAddLineCharSequence() {
        CharBuf buf = CharBuf.create(32);
        CharSequence cs = "world";
        buf.addLine(cs);
        assertEquals("world\n", buf.toString());
    }

    @Test
    void testAddCharArray() {
        CharBuf buf = CharBuf.create(16);
        buf.add("test".toCharArray());
        assertEquals("test", buf.toString());
    }

    @Test
    void testAddChars() {
        CharBuf buf = CharBuf.create(16);
        buf.addChars("data".toCharArray());
        assertEquals("data", buf.toString());
    }

    @Test
    void testAddQuoted() {
        CharBuf buf = CharBuf.create(16);
        buf.addQuoted("hello".toCharArray());
        assertEquals("\"hello\"", buf.toString());
    }

    @Test
    void testAddJsonEscapedString() {
        CharBuf buf = CharBuf.create(32);
        buf.addJsonEscapedString("hello");
        assertEquals("\"hello\"", buf.toString());
    }

    @Test
    void testAddJsonEscapedStringWithSpecialChars() {
        CharBuf buf = CharBuf.create(64);
        buf.addJsonEscapedString("hello\nworld\t!");
        String result = buf.toString();
        assertTrue(result.contains("\\n"));
        assertTrue(result.contains("\\t"));
    }

    @Test
    void testAddJsonEscapedStringWithUnicodeDisabled() {
        CharBuf buf = CharBuf.create(32);
        buf.addJsonEscapedString("hello", true);
        assertEquals("\"hello\"", buf.toString());
    }

    @Test
    void testCharSequenceInterface() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello");

        assertEquals(5, buf.length());
        assertEquals('h', buf.charAt(0));
        assertEquals('e', buf.charAt(1));
        assertEquals('o', buf.charAt(4));
    }

    @Test
    void testSubSequence() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello world");
        CharSequence sub = buf.subSequence(0, 5);
        assertEquals("hello", sub.toString());
    }

    @Test
    void testWriterWrite() throws IOException {
        CharBuf buf = CharBuf.create(16);
        buf.write("test".toCharArray(), 0, 4);
        assertEquals("test", buf.toString());
    }

    @Test
    void testWriterWritePartial() throws IOException {
        CharBuf buf = CharBuf.create(16);
        buf.write("hello world".toCharArray(), 6, 5);
        assertEquals("world", buf.toString());
    }

    @Test
    void testWriterFlush() throws IOException {
        CharBuf buf = CharBuf.create(16);
        buf.add("test");
        buf.flush(); // Should not throw
        assertEquals("test", buf.toString());
    }

    @Test
    void testWriterClose() throws IOException {
        CharBuf buf = CharBuf.create(16);
        buf.add("test");
        buf.close(); // Should not throw
        assertEquals("test", buf.toString());
    }

    @Test
    void testToCharArray() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello");
        // toCharArray returns the underlying buffer which is larger than content
        char[] chars = buf.toCharArray();
        assertNotNull(chars);
        assertTrue(chars.length >= 5);
    }

    @Test
    void testAutoExpansion() {
        CharBuf buf = CharBuf.create(4);
        buf.add("this is a much longer string that exceeds initial capacity");
        assertEquals("this is a much longer string that exceeds initial capacity", buf.toString());
    }

    @Test
    void testAutoExpansionWithChars() {
        CharBuf buf = CharBuf.create(4);
        for (int i = 0; i < 100; i++) {
            buf.addChar('x');
        }
        assertEquals(100, buf.length());
    }

    @Test
    void testReadForRecycleWithSmallLength() {
        CharBuf buf = CharBuf.create(16);
        buf.add("hello");
        char[] result = buf.readForRecycle();
        assertNotNull(result);
        // Should return the underlying buffer for recycling
    }

    @Test
    void testEmptyBuffer() {
        CharBuf buf = CharBuf.create(16);
        assertEquals(0, buf.length());
        assertEquals("", buf.toString());
    }

    @Test
    void testToStringWithBuffer() {
        char[] initial = "initial".toCharArray();
        CharBuf buf = new CharBuf(initial);
        // Constructor sets capacity from buffer but location is 0
        assertEquals("", buf.toString());
        // Add content to see it
        buf.add("test");
        assertEquals("test", buf.toString());
    }

    @Test
    void testConstructorWithBytes() {
        byte[] bytes = "hello".getBytes();
        CharBuf buf = new CharBuf(bytes);
        // Constructor sets the buffer but location is 0
        assertEquals("", buf.toString());
        // Add content after construction
        buf.add("world");
        assertEquals("world", buf.toString());
    }

    @Test
    void testMultipleAdditions() {
        CharBuf buf = CharBuf.create(16);
        buf.add("one")
           .add(2)
           .add(true)
           .addChar('-')
           .add(3.14);
        String result = buf.toString();
        assertTrue(result.startsWith("one2true-3.14"));
    }
}
