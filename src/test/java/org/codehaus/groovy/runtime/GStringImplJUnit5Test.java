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
package org.codehaus.groovy.runtime;

import groovy.lang.GString;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for GStringImpl class.
 */
class GStringImplJUnit5Test {

    private GStringImpl createGString(String prefix, Object value, String suffix) {
        return new GStringImpl(new Object[]{value}, new String[]{prefix, suffix});
    }

    private GStringImpl createSimpleGString(String text) {
        return new GStringImpl(new Object[]{}, new String[]{text});
    }

    // Basic construction and toString tests
    @Test
    void testBasicConstruction() {
        GStringImpl gs = createGString("Hello ", "World", "!");
        assertEquals("Hello World!", gs.toString());
    }

    @Test
    void testEmptyGString() {
        GStringImpl gs = createSimpleGString("");
        assertEquals("", gs.toString());
    }

    @Test
    void testGStringWithNullValue() {
        GStringImpl gs = createGString("Value is: ", null, ".");
        assertEquals("Value is: null.", gs.toString());
    }

    @Test
    void testGStringWithMultipleValues() {
        GStringImpl gs = new GStringImpl(
            new Object[]{"John", 30},
            new String[]{"Name: ", ", Age: ", "."}
        );
        assertEquals("Name: John, Age: 30.", gs.toString());
    }

    // getStrings and getValues tests
    @Test
    void testGetStrings() {
        GStringImpl gs = createGString("A", "B", "C");
        String[] strings = gs.getStrings();
        assertArrayEquals(new String[]{"A", "C"}, strings);
    }

    @Test
    void testGetValues() {
        GStringImpl gs = createGString("A", "B", "C");
        Object[] values = gs.getValues();
        assertArrayEquals(new Object[]{"B"}, values);
    }

    // freeze tests
    @Test
    void testFreeze() {
        GStringImpl gs = createGString("Hello ", "World", "!");
        GString frozen = gs.freeze();
        assertEquals("Hello World!", frozen.toString());
    }

    @Test
    void testFrozenGStringReturnsCopies() {
        GStringImpl gs = createGString("Hello ", "World", "!");
        GStringImpl frozen = (GStringImpl) gs.freeze();
        
        // Frozen GString should return copies
        String[] strings1 = frozen.getStrings();
        String[] strings2 = frozen.getStrings();
        assertNotSame(strings1, strings2);
        assertArrayEquals(strings1, strings2);
    }

    // plus tests
    @Test
    void testPlus() {
        GStringImpl gs1 = createGString("Hello ", "World", "");
        GStringImpl gs2 = createGString("!", "", "");
        GString result = gs1.plus(gs2);
        assertEquals("Hello World!", result.toString());
    }

    // writeTo tests
    @Test
    void testWriteTo() throws Exception {
        GStringImpl gs = createGString("Hello ", "World", "!");
        StringWriter writer = new StringWriter();
        gs.writeTo(writer);
        assertEquals("Hello World!", writer.toString());
    }

    // String-like methods tests
    @Test
    void testTrim() {
        GStringImpl gs = createGString("  Hello ", "World", "  ");
        assertEquals("Hello World", gs.trim());
    }

    @Test
    void testIsEmpty() {
        GStringImpl empty = createSimpleGString("");
        GStringImpl nonEmpty = createSimpleGString("hello");
        assertTrue(empty.isEmpty());
        assertFalse(nonEmpty.isEmpty());
    }

    @Test
    void testIsBlank() {
        GStringImpl blank = createSimpleGString("   ");
        GStringImpl nonBlank = createSimpleGString("  hello  ");
        assertTrue(blank.isBlank());
        assertFalse(nonBlank.isBlank());
    }

    @Test
    void testLines() {
        GStringImpl gs = createSimpleGString("line1\nline2\nline3");
        var lines = gs.lines().collect(Collectors.toList());
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
    }

    @Test
    void testRepeat() {
        GStringImpl gs = createSimpleGString("ab");
        assertEquals("ababab", gs.repeat(3));
    }

    @Test
    void testRepeatZero() {
        GStringImpl gs = createSimpleGString("ab");
        assertEquals("", gs.repeat(0));
    }

    @Test
    void testStripLeading() {
        GStringImpl gs = createSimpleGString("  hello");
        assertEquals("hello", gs.stripLeading());
    }

    @Test
    void testStripTrailing() {
        GStringImpl gs = createSimpleGString("hello  ");
        assertEquals("hello", gs.stripTrailing());
    }

    @Test
    void testStrip() {
        GStringImpl gs = createSimpleGString("  hello  ");
        assertEquals("hello", gs.strip());
    }

    @Test
    void testCodePointAt() {
        GStringImpl gs = createSimpleGString("ABC");
        assertEquals('A', gs.codePointAt(0));
        assertEquals('B', gs.codePointAt(1));
    }

    @Test
    void testCodePointBefore() {
        GStringImpl gs = createSimpleGString("ABC");
        assertEquals('A', gs.codePointBefore(1));
        assertEquals('B', gs.codePointBefore(2));
    }

    @Test
    void testCodePointCount() {
        GStringImpl gs = createSimpleGString("ABCDE");
        assertEquals(3, gs.codePointCount(1, 4));
    }

    @Test
    void testOffsetByCodePoints() {
        GStringImpl gs = createSimpleGString("ABCDE");
        assertEquals(3, gs.offsetByCodePoints(1, 2));
    }

    @Test
    void testGetChars() {
        GStringImpl gs = createSimpleGString("Hello");
        char[] dst = new char[3];
        gs.getChars(1, 4, dst, 0);
        assertArrayEquals(new char[]{'e', 'l', 'l'}, dst);
    }

    @Test
    void testGetBytes() {
        GStringImpl gs = createSimpleGString("Hello");
        byte[] bytes = gs.getBytes(StandardCharsets.UTF_8);
        assertEquals(5, bytes.length);
    }

    @Test
    void testContentEqualsStringBuffer() {
        GStringImpl gs = createSimpleGString("Hello");
        assertTrue(gs.contentEquals(new StringBuffer("Hello")));
        assertFalse(gs.contentEquals(new StringBuffer("World")));
    }

    @Test
    void testContentEqualsCharSequence() {
        GStringImpl gs = createSimpleGString("Hello");
        assertTrue(gs.contentEquals("Hello"));
        assertFalse(gs.contentEquals("World"));
    }

    @Test
    void testEqualsIgnoreCase() {
        GStringImpl gs = createSimpleGString("Hello");
        assertTrue(gs.equalsIgnoreCase("HELLO"));
        assertTrue(gs.equalsIgnoreCase("hello"));
        assertFalse(gs.equalsIgnoreCase("World"));
    }

    @Test
    void testCompareTo() {
        GStringImpl gs = createSimpleGString("B");
        assertTrue(gs.compareTo("A") > 0);
        assertTrue(gs.compareTo("C") < 0);
        assertEquals(0, gs.compareTo("B"));
    }

    @Test
    void testCompareToIgnoreCase() {
        GStringImpl gs = createSimpleGString("b");
        assertEquals(0, gs.compareToIgnoreCase("B"));
    }

    @Test
    void testRegionMatches() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.regionMatches(6, "World", 0, 5));
        assertFalse(gs.regionMatches(6, "Earth", 0, 5));
    }

    @Test
    void testRegionMatchesIgnoreCase() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.regionMatches(true, 6, "WORLD", 0, 5));
    }

    @Test
    void testStartsWithOffset() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.startsWith("World", 6));
        assertFalse(gs.startsWith("Hello", 6));
    }

    @Test
    void testStartsWith() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.startsWith("Hello"));
        assertFalse(gs.startsWith("World"));
    }

    @Test
    void testEndsWith() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.endsWith("World"));
        assertFalse(gs.endsWith("Hello"));
    }

    @Test
    void testIndexOfChar() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals(1, gs.indexOf('e'));
        assertEquals(-1, gs.indexOf('x'));
    }

    @Test
    void testIndexOfCharFromIndex() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals(2, gs.indexOf('l', 0));
        assertEquals(3, gs.indexOf('l', 3));
    }

    @Test
    void testLastIndexOfChar() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals(3, gs.lastIndexOf('l'));
    }

    @Test
    void testLastIndexOfCharFromIndex() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals(2, gs.lastIndexOf('l', 2));
    }

    @Test
    void testIndexOfString() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertEquals(6, gs.indexOf("World"));
    }

    @Test
    void testIndexOfStringFromIndex() {
        GStringImpl gs = createSimpleGString("Hello Hello");
        assertEquals(6, gs.indexOf("Hello", 1));
    }

    @Test
    void testLastIndexOfString() {
        GStringImpl gs = createSimpleGString("Hello Hello");
        assertEquals(6, gs.lastIndexOf("Hello"));
    }

    @Test
    void testLastIndexOfStringFromIndex() {
        GStringImpl gs = createSimpleGString("Hello Hello");
        assertEquals(0, gs.lastIndexOf("Hello", 5));
    }

    @Test
    void testSubstring() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertEquals("World", gs.substring(6));
    }

    @Test
    void testSubstringRange() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertEquals("World", gs.substring(6, 11));
    }

    @Test
    void testConcat() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals("Hello World", gs.concat(" World"));
    }

    @Test
    void testReplaceChar() {
        GStringImpl gs = createSimpleGString("Hello");
        assertEquals("Hallo", gs.replace('e', 'a'));
    }

    @Test
    void testMatches() {
        GStringImpl gs = createSimpleGString("Hello123");
        assertTrue(gs.matches("\\w+\\d+"));
        assertFalse(gs.matches("\\d+"));
    }

    @Test
    void testContains() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertTrue(gs.contains("World"));
        assertFalse(gs.contains("Earth"));
    }

    @Test
    void testReplaceFirst() {
        GStringImpl gs = createSimpleGString("Hello Hello");
        assertEquals("Hi Hello", gs.replaceFirst("Hello", "Hi"));
    }

    @Test
    void testReplaceAll() {
        GStringImpl gs = createSimpleGString("Hello Hello");
        assertEquals("Hi Hi", gs.replaceAll("Hello", "Hi"));
    }

    @Test
    void testReplaceCharSequence() {
        GStringImpl gs = createSimpleGString("Hello World");
        assertEquals("Hello Earth", gs.replace("World", "Earth"));
    }

    @Test
    void testSplitWithLimit() {
        GStringImpl gs = createSimpleGString("a,b,c,d");
        String[] parts = gs.split(",", 2);
        assertEquals(2, parts.length);
        assertEquals("a", parts[0]);
        assertEquals("b,c,d", parts[1]);
    }

    @Test
    void testSplit() {
        GStringImpl gs = createSimpleGString("a,b,c");
        String[] parts = gs.split(",");
        assertEquals(3, parts.length);
    }

    @Test
    void testToLowerCaseWithLocale() {
        GStringImpl gs = createSimpleGString("HELLO");
        assertEquals("hello", gs.toLowerCase(Locale.ENGLISH));
    }

    @Test
    void testToLowerCase() {
        GStringImpl gs = createSimpleGString("HELLO");
        assertEquals("hello", gs.toLowerCase());
    }

    @Test
    void testToUpperCaseWithLocale() {
        GStringImpl gs = createSimpleGString("hello");
        assertEquals("HELLO", gs.toUpperCase(Locale.ENGLISH));
    }

    @Test
    void testToUpperCase() {
        GStringImpl gs = createSimpleGString("hello");
        assertEquals("HELLO", gs.toUpperCase());
    }

    @Test
    void testToCharArray() {
        GStringImpl gs = createSimpleGString("Hello");
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, gs.toCharArray());
    }

    @Test
    void testIntern() {
        GStringImpl gs = createSimpleGString("Hello");
        String interned = gs.intern();
        assertSame(interned, "Hello".intern());
    }

    // Caching behavior tests
    @Test
    void testCacheableWithImmutableValues() {
        // GString with immutable values should be cacheable
        GStringImpl gs = new GStringImpl(new Object[]{"immutable"}, new String[]{"prefix:", ""});
        String str1 = gs.toString();
        String str2 = gs.toString();
        assertEquals(str1, str2);
    }

    @Test
    void testNonCacheableAfterGetStrings() {
        GStringImpl gs = createGString("Hello ", "World", "!");
        gs.toString(); // might cache
        gs.getStrings(); // should invalidate cache on non-frozen
        String result = gs.toString();
        assertEquals("Hello World!", result);
    }

    @Test
    void testNonCacheableAfterGetValues() {
        GStringImpl gs = createGString("Hello ", "World", "!");
        gs.toString(); // might cache
        gs.getValues(); // should invalidate cache on non-frozen
        String result = gs.toString();
        assertEquals("Hello World!", result);
    }
}
