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
package org.codehaus.groovy.runtime

import groovy.lang.GString
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for GStringImpl class.
 */
class GStringImplTest {

    private GStringImpl createGString(String prefix, Object value, String suffix) {
        return new GStringImpl(new Object[]{value}, new String[]{prefix, suffix})
    }

    private GStringImpl createSimpleGString(String text) {
        return new GStringImpl(new Object[]{}, new String[]{text})
    }

    // Basic construction and toString tests
    @Test
    void testBasicConstruction() {
        def gs = createGString("Hello ", "World", "!")
        assertEquals("Hello World!", gs.toString())
    }

    @Test
    void testEmptyGString() {
        def gs = createSimpleGString("")
        assertEquals("", gs.toString())
    }

    @Test
    void testGStringWithNullValue() {
        def gs = createGString("Value is: ", null, ".")
        assertEquals("Value is: null.", gs.toString())
    }

    @Test
    void testGStringWithMultipleValues() {
        def gs = new GStringImpl(
            new Object[]{"John", 30},
            new String[]{"Name: ", ", Age: ", "."}
        )
        assertEquals("Name: John, Age: 30.", gs.toString())
    }

    // getStrings and getValues tests
    @Test
    void testGetStrings() {
        def gs = createGString("A", "B", "C")
        def strings = gs.getStrings()
        assertArrayEquals(new String[]{"A", "C"}, strings)
    }

    @Test
    void testGetValues() {
        def gs = createGString("A", "B", "C")
        def values = gs.getValues()
        assertArrayEquals(new Object[]{"B"}, values)
    }

    // freeze tests
    @Test
    void testFreeze() {
        def gs = createGString("Hello ", "World", "!")
        def frozen = gs.freeze()
        assertEquals("Hello World!", frozen.toString())
    }

    @Test
    void testFrozenGStringReturnsCopies() {
        def gs = createGString("Hello ", "World", "!")
        def frozen = (GStringImpl) gs.freeze()

        // Frozen GString should return copies
        def strings1 = frozen.getStrings()
        def strings2 = frozen.getStrings()
        assertNotSame(strings1, strings2)
        assertArrayEquals(strings1, strings2)
    }

    // plus tests
    @Test
    void testPlus() {
        def gs1 = createGString("Hello ", "World", "")
        def gs2 = createGString("!", "", "")
        def result = gs1.plus(gs2)
        assertEquals("Hello World!", result.toString())
    }

    // writeTo tests
    @Test
    void testWriteTo() throws Exception {
        def gs = createGString("Hello ", "World", "!")
        def writer = new StringWriter()
        gs.writeTo(writer)
        assertEquals("Hello World!", writer.toString())
    }

    // String-like methods tests
    @Test
    void testTrim() {
        def gs = createGString("  Hello ", "World", "  ")
        assertEquals("Hello World", gs.trim())
    }

    @Test
    void testIsEmpty() {
        def empty = createSimpleGString("")
        def nonEmpty = createSimpleGString("hello")
        assertTrue(empty.isEmpty())
        assertFalse(nonEmpty.isEmpty())
    }

    @Test
    void testIsBlank() {
        def blank = createSimpleGString("   ")
        def nonBlank = createSimpleGString("  hello  ")
        assertTrue(blank.isBlank())
        assertFalse(nonBlank.isBlank())
    }

    @Test
    void testLines() {
        def gs = createSimpleGString("line1\nline2\nline3")
        def lines = gs.lines().collect(Collectors.toList())
        assertEquals(3, lines.size())
        assertEquals("line1", lines.get(0))
        assertEquals("line2", lines.get(1))
        assertEquals("line3", lines.get(2))
    }

    @Test
    void testRepeat() {
        def gs = createSimpleGString("ab")
        assertEquals("ababab", gs.repeat(3))
    }

    @Test
    void testRepeatZero() {
        def gs = createSimpleGString("ab")
        assertEquals("", gs.repeat(0))
    }

    @Test
    void testStripLeading() {
        def gs = createSimpleGString("  hello")
        assertEquals("hello", gs.stripLeading())
    }

    @Test
    void testStripTrailing() {
        def gs = createSimpleGString("hello  ")
        assertEquals("hello", gs.stripTrailing())
    }

    @Test
    void testStrip() {
        def gs = createSimpleGString("  hello  ")
        assertEquals("hello", gs.strip())
    }

    @Test
    void testCodePointAt() {
        def gs = createSimpleGString("ABC")
        assertEquals((int) 'A', gs.codePointAt(0))
        assertEquals((int) 'B', gs.codePointAt(1))
    }

    @Test
    void testCodePointBefore() {
        def gs = createSimpleGString("ABC")
        assertEquals((int) 'A', gs.codePointBefore(1))
        assertEquals((int) 'B', gs.codePointBefore(2))
    }

    @Test
    void testCodePointCount() {
        def gs = createSimpleGString("ABCDE")
        assertEquals(3, gs.codePointCount(1, 4))
    }

    @Test
    void testOffsetByCodePoints() {
        def gs = createSimpleGString("ABCDE")
        assertEquals(3, gs.offsetByCodePoints(1, 2))
    }

    @Test
    void testGetChars() {
        def gs = createSimpleGString("Hello")
        char[] dst = new char[3]
        gs.getChars(1, 4, dst, 0)
        assertArrayEquals(new char[]{'e', 'l', 'l'}, dst)
    }

    @Test
    void testGetBytes() {
        def gs = createSimpleGString("Hello")
        def bytes = gs.getBytes(StandardCharsets.UTF_8)
        assertEquals(5, bytes.length)
    }

    @Test
    void testContentEqualsStringBuffer() {
        def gs = createSimpleGString("Hello")
        assertTrue(gs.contentEquals(new StringBuffer("Hello")))
        assertFalse(gs.contentEquals(new StringBuffer("World")))
    }

    @Test
    void testContentEqualsCharSequence() {
        def gs = createSimpleGString("Hello")
        assertTrue(gs.contentEquals("Hello"))
        assertFalse(gs.contentEquals("World"))
    }

    @Test
    void testEqualsIgnoreCase() {
        def gs = createSimpleGString("Hello")
        assertTrue(gs.equalsIgnoreCase("HELLO"))
        assertTrue(gs.equalsIgnoreCase("hello"))
        assertFalse(gs.equalsIgnoreCase("World"))
    }

    @Test
    void testCompareTo() {
        def gs = createSimpleGString("B")
        assertTrue(gs.compareTo("A") > 0)
        assertTrue(gs.compareTo("C") < 0)
        assertEquals(0, gs.compareTo("B"))
    }

    @Test
    void testCompareToIgnoreCase() {
        def gs = createSimpleGString("b")
        assertEquals(0, gs.compareToIgnoreCase("B"))
    }

    @Test
    void testRegionMatches() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.regionMatches(6, "World", 0, 5))
        assertFalse(gs.regionMatches(6, "Earth", 0, 5))
    }

    @Test
    void testRegionMatchesIgnoreCase() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.regionMatches(true, 6, "WORLD", 0, 5))
    }

    @Test
    void testStartsWithOffset() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.startsWith("World", 6))
        assertFalse(gs.startsWith("Hello", 6))
    }

    @Test
    void testStartsWith() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.startsWith("Hello"))
        assertFalse(gs.startsWith("World"))
    }

    @Test
    void testEndsWith() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.endsWith("World"))
        assertFalse(gs.endsWith("Hello"))
    }

    @Test
    void testIndexOfChar() {
        def gs = createSimpleGString("Hello")
        assertEquals(1, gs.indexOf((int) 'e'))
        assertEquals(-1, gs.indexOf((int) 'x'))
    }

    @Test
    void testIndexOfCharFromIndex() {
        def gs = createSimpleGString("Hello")
        assertEquals(2, gs.indexOf((int) 'l', 0))
        assertEquals(3, gs.indexOf((int) 'l', 3))
    }

    @Test
    void testLastIndexOfChar() {
        def gs = createSimpleGString("Hello")
        assertEquals(3, gs.lastIndexOf((int) 'l'))
    }

    @Test
    void testLastIndexOfCharFromIndex() {
        def gs = createSimpleGString("Hello")
        assertEquals(2, gs.lastIndexOf((int) 'l', 2))
    }

    @Test
    void testIndexOfString() {
        def gs = createSimpleGString("Hello World")
        assertEquals(6, gs.indexOf("World"))
    }

    @Test
    void testIndexOfStringFromIndex() {
        def gs = createSimpleGString("Hello Hello")
        assertEquals(6, gs.indexOf("Hello", 1))
    }

    @Test
    void testLastIndexOfString() {
        def gs = createSimpleGString("Hello Hello")
        assertEquals(6, gs.lastIndexOf("Hello"))
    }

    @Test
    void testLastIndexOfStringFromIndex() {
        def gs = createSimpleGString("Hello Hello")
        assertEquals(0, gs.lastIndexOf("Hello", 5))
    }

    @Test
    void testSubstring() {
        def gs = createSimpleGString("Hello World")
        assertEquals("World", gs.substring(6))
    }

    @Test
    void testSubstringRange() {
        def gs = createSimpleGString("Hello World")
        assertEquals("World", gs.substring(6, 11))
    }

    @Test
    void testConcat() {
        def gs = createSimpleGString("Hello")
        assertEquals("Hello World", gs.concat(" World"))
    }

    @Test
    void testReplaceChar() {
        def gs = createSimpleGString("Hello")
        assertEquals("Hallo", gs.replace((char) 'e', (char) 'a'))
    }

    @Test
    void testMatches() {
        def gs = createSimpleGString("Hello123")
        assertTrue(gs.matches("\\w+\\d+"))
        assertFalse(gs.matches("\\d+"))
    }

    @Test
    void testContains() {
        def gs = createSimpleGString("Hello World")
        assertTrue(gs.contains("World"))
        assertFalse(gs.contains("Earth"))
    }

    @Test
    void testReplaceFirst() {
        def gs = createSimpleGString("Hello Hello")
        assertEquals("Hi Hello", gs.replaceFirst("Hello", "Hi"))
    }

    @Test
    void testReplaceAll() {
        def gs = createSimpleGString("Hello Hello")
        assertEquals("Hi Hi", gs.replaceAll("Hello", "Hi"))
    }

    @Test
    void testReplaceCharSequence() {
        def gs = createSimpleGString("Hello World")
        assertEquals("Hello Earth", gs.replace("World", "Earth"))
    }

    @Test
    void testSplitWithLimit() {
        def gs = createSimpleGString("a,b,c,d")
        def parts = gs.split(",", 2)
        assertEquals(2, parts.length)
        assertEquals("a", parts[0])
        assertEquals("b,c,d", parts[1])
    }

    @Test
    void testSplit() {
        def gs = createSimpleGString("a,b,c")
        def parts = gs.split(",")
        assertEquals(3, parts.length)
    }

    @Test
    void testToLowerCaseWithLocale() {
        def gs = createSimpleGString("HELLO")
        assertEquals("hello", gs.toLowerCase(Locale.ENGLISH))
    }

    @Test
    void testToLowerCase() {
        def gs = createSimpleGString("HELLO")
        assertEquals("hello", gs.toLowerCase())
    }

    @Test
    void testToUpperCaseWithLocale() {
        def gs = createSimpleGString("hello")
        assertEquals("HELLO", gs.toUpperCase(Locale.ENGLISH))
    }

    @Test
    void testToUpperCase() {
        def gs = createSimpleGString("hello")
        assertEquals("HELLO", gs.toUpperCase())
    }

    @Test
    void testToCharArray() {
        def gs = createSimpleGString("Hello")
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, gs.toCharArray())
    }

    @Test
    void testIntern() {
        def gs = createSimpleGString("Hello")
        def interned = gs.intern()
        assertSame(interned, "Hello".intern())
    }

    // Caching behavior tests
    @Test
    void testCacheableWithImmutableValues() {
        // GString with immutable values should be cacheable
        def gs = new GStringImpl(new Object[]{"immutable"}, new String[]{"prefix:", ""})
        def str1 = gs.toString()
        def str2 = gs.toString()
        assertEquals(str1, str2)
    }

    @Test
    void testNonCacheableAfterGetStrings() {
        def gs = createGString("Hello ", "World", "!")
        gs.toString() // might cache
        gs.getStrings() // should invalidate cache on non-frozen
        def result = gs.toString()
        assertEquals("Hello World!", result)
    }

    @Test
    void testNonCacheableAfterGetValues() {
        def gs = createGString("Hello ", "World", "!")
        gs.toString() // might cache
        gs.getValues() // should invalidate cache on non-frozen
        def result = gs.toString()
        assertEquals("Hello World!", result)
    }
}
