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
package groovy.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StringEscapeUtils}.
 */
class StringEscapeUtilsTest {

    @Test
    void testEscapeJavaNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    @Test
    void testEscapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    @Test
    void testEscapeJavaBasicString() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"));
    }

    @Test
    void testEscapeJavaDoubleQuote() {
        assertEquals("\\\"hello\\\"", StringEscapeUtils.escapeJava("\"hello\""));
    }

    @Test
    void testEscapeJavaBackslash() {
        assertEquals("path\\\\to\\\\file", StringEscapeUtils.escapeJava("path\\to\\file"));
    }

    @Test
    void testEscapeJavaNewline() {
        assertEquals("line1\\nline2", StringEscapeUtils.escapeJava("line1\nline2"));
    }

    @Test
    void testEscapeJavaTab() {
        assertEquals("col1\\tcol2", StringEscapeUtils.escapeJava("col1\tcol2"));
    }

    @Test
    void testEscapeJavaCarriageReturn() {
        assertEquals("line1\\rline2", StringEscapeUtils.escapeJava("line1\rline2"));
    }

    @Test
    void testEscapeJavaFormFeed() {
        assertEquals("page1\\fpage2", StringEscapeUtils.escapeJava("page1\fpage2"));
    }

    @Test
    void testEscapeJavaBackspace() {
        assertEquals("back\\bspace", StringEscapeUtils.escapeJava("back\bspace"));
    }

    @Test
    void testEscapeJavaSingleQuoteNotEscaped() {
        assertEquals("it's", StringEscapeUtils.escapeJava("it's"));
    }

    @Test
    void testEscapeJavaForwardSlashNotEscaped() {
        assertEquals("path/to/file", StringEscapeUtils.escapeJava("path/to/file"));
    }

    @Test
    void testEscapeJavaUnicodeHigh() {
        // Characters > 0xfff
        String input = "\u4e2d\u6587"; // Chinese characters
        String result = StringEscapeUtils.escapeJava(input);
        assertTrue(result.contains("\\u"));
    }

    @Test
    void testEscapeJavaUnicodeMid() {
        // Characters > 0xff and <= 0xfff
        String input = "\u0100"; // Latin Extended-A
        String result = StringEscapeUtils.escapeJava(input);
        assertTrue(result.contains("\\u0100"));
    }

    @Test
    void testEscapeJavaUnicodeLow() {
        // Characters >= 0x7f and <= 0xff
        String input = "\u007f"; // DEL character
        String result = StringEscapeUtils.escapeJava(input);
        assertTrue(result.contains("\\u007F") || result.contains("\\u007f"));
    }

    @Test
    void testEscapeJavaControlCharacter() {
        // Control characters < 32 (not the standard escape sequences)
        String input = "\u0001"; // SOH
        String result = StringEscapeUtils.escapeJava(input);
        assertTrue(result.contains("\\u0001"));
    }

    @Test
    void testEscapeJavaControlCharacterBetween15And31() {
        String input = "\u0010"; // DLE
        String result = StringEscapeUtils.escapeJava(input);
        assertTrue(result.contains("\\u0010"));
    }

    @Test
    void testEscapeJavaToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "hello\"world");
        assertEquals("hello\\\"world", writer.toString());
    }

    @Test
    void testEscapeJavaToWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testEscapeJavaToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException.class, () -> 
            StringEscapeUtils.escapeJava(null, "test"));
    }

    @Test
    void testEscapeJavaScript() {
        assertEquals("it\\'s", StringEscapeUtils.escapeJavaScript("it's"));
    }

    @Test
    void testEscapeJavaScriptForwardSlash() {
        assertEquals("path\\/to\\/file", StringEscapeUtils.escapeJavaScript("path/to/file"));
    }

    @Test
    void testEscapeJavaScriptNull() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
    }

    @Test
    void testEscapeJavaScriptToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "it's \"quoted\"");
        assertEquals("it\\'s \\\"quoted\\\"", writer.toString());
    }

    @Test
    void testEscapeJavaScriptToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException.class, () -> 
            StringEscapeUtils.escapeJavaScript(null, "test"));
    }

    @Test
    void testUnescapeJavaNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    @Test
    void testUnescapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.unescapeJava(""));
    }

    @Test
    void testUnescapeJavaBasicString() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"));
    }

    @Test
    void testUnescapeJavaDoubleQuote() {
        assertEquals("\"hello\"", StringEscapeUtils.unescapeJava("\\\"hello\\\""));
    }

    @Test
    void testUnescapeJavaSingleQuote() {
        assertEquals("'hello'", StringEscapeUtils.unescapeJava("\\'hello\\'"));
    }

    @Test
    void testUnescapeJavaBackslash() {
        assertEquals("path\\to\\file", StringEscapeUtils.unescapeJava("path\\\\to\\\\file"));
    }

    @Test
    void testUnescapeJavaNewline() {
        assertEquals("line1\nline2", StringEscapeUtils.unescapeJava("line1\\nline2"));
    }

    @Test
    void testUnescapeJavaTab() {
        assertEquals("col1\tcol2", StringEscapeUtils.unescapeJava("col1\\tcol2"));
    }

    @Test
    void testUnescapeJavaCarriageReturn() {
        assertEquals("line1\rline2", StringEscapeUtils.unescapeJava("line1\\rline2"));
    }

    @Test
    void testUnescapeJavaFormFeed() {
        assertEquals("page1\fpage2", StringEscapeUtils.unescapeJava("page1\\fpage2"));
    }

    @Test
    void testUnescapeJavaBackspace() {
        assertEquals("back\bspace", StringEscapeUtils.unescapeJava("back\\bspace"));
    }

    @Test
    void testUnescapeJavaUnicode() {
        assertEquals("\u0041", StringEscapeUtils.unescapeJava("\\u0041"));
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
    }

    @Test
    void testUnescapeJavaUnicodeMultiple() {
        assertEquals("AB", StringEscapeUtils.unescapeJava("\\u0041\\u0042"));
    }

    @Test
    void testUnescapeJavaMixedContent() {
        assertEquals("hello\nworld\ttab", StringEscapeUtils.unescapeJava("hello\\nworld\\ttab"));
    }

    @Test
    void testUnescapeJavaInvalidUnicode() {
        assertThrows(RuntimeException.class, () -> 
            StringEscapeUtils.unescapeJava("\\uXYZQ"));
    }

    @Test
    void testUnescapeJavaTrailingBackslash() {
        assertEquals("trailing\\", StringEscapeUtils.unescapeJava("trailing\\"));
    }

    @Test
    void testUnescapeJavaUnknownEscape() {
        // Unknown escape sequences should just return the character after backslash
        assertEquals("x", StringEscapeUtils.unescapeJava("\\x"));
    }

    @Test
    void testUnescapeJavaToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "hello\\\"world");
        assertEquals("hello\"world", writer.toString());
    }

    @Test
    void testUnescapeJavaToWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testUnescapeJavaToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException.class, () -> 
            StringEscapeUtils.unescapeJava(null, "test"));
    }

    @Test
    void testUnescapeJavaScriptSameAsJava() {
        String escaped = "hello\\\"world\\'test";
        assertEquals(StringEscapeUtils.unescapeJava(escaped), 
                     StringEscapeUtils.unescapeJavaScript(escaped));
    }

    @Test
    void testUnescapeJavaScriptNull() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
    }

    @Test
    void testUnescapeJavaScriptToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "hello\\nworld");
        assertEquals("hello\nworld", writer.toString());
    }

    @Test
    void testRoundTripJava() {
        String original = "Hello\n\"World\"\t\\Test/";
        String escaped = StringEscapeUtils.escapeJava(original);
        String unescaped = StringEscapeUtils.unescapeJava(escaped);
        assertEquals(original, unescaped);
    }

    @Test
    void testRoundTripJavaScript() {
        String original = "Hello'World";
        String escaped = StringEscapeUtils.escapeJavaScript(original);
        String unescaped = StringEscapeUtils.unescapeJavaScript(escaped);
        assertEquals(original, unescaped);
    }

    @Test
    void testConstructor() {
        // Test that constructor can be called (even though it's typically used statically)
        StringEscapeUtils utils = new StringEscapeUtils();
        assertNotNull(utils);
    }

    @Test
    void testAllControlCharacters() {
        // Test all control characters (0x00 to 0x1f except standard escapes)
        for (int i = 0; i < 32; i++) {
            if (i != '\b' && i != '\t' && i != '\n' && i != '\f' && i != '\r') {
                String input = String.valueOf((char) i);
                String escaped = StringEscapeUtils.escapeJava(input);
                assertTrue(escaped.startsWith("\\u000") || escaped.startsWith("\\u00"), 
                    "Control char " + i + " should be unicode escaped");
            }
        }
    }
}
