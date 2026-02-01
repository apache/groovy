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
 * JUnit 5 tests for StringEscapeUtils class.
 */
class StringEscapeUtilsJUnit5Test {

    // escapeJava tests
    @Test
    void testEscapeJavaNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    @Test
    void testEscapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    @Test
    void testEscapeJavaSimpleText() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"));
    }

    @Test
    void testEscapeJavaDoubleQuotes() {
        assertEquals("They didn't say, \\\"Stop!\\\"", 
            StringEscapeUtils.escapeJava("They didn't say, \"Stop!\""));
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
    void testEscapeJavaBackspace() {
        assertEquals("back\\bspace", StringEscapeUtils.escapeJava("back\bspace"));
    }

    @Test
    void testEscapeJavaFormFeed() {
        assertEquals("form\\ffeed", StringEscapeUtils.escapeJava("form\ffeed"));
    }

    @Test
    void testEscapeJavaUnicodeHigh() {
        // Characters > 0xfff - uses uppercase hex
        assertEquals("\\u4E2D", StringEscapeUtils.escapeJava("\u4e2d")); // Chinese character
    }

    @Test
    void testEscapeJavaUnicodeMedium() {
        // Characters > 0xff and <= 0xfff - uses uppercase hex
        assertEquals("\\u03B1", StringEscapeUtils.escapeJava("\u03b1")); // Greek alpha
    }

    @Test
    void testEscapeJavaUnicodeLow() {
        // Characters >= 0x7f and <= 0xff - uses uppercase hex
        assertEquals("\\u00A9", StringEscapeUtils.escapeJava("\u00a9")); // Copyright symbol
    }

    @Test
    void testEscapeJavaControlChars() {
        // Control chars < 32 that aren't special - uses uppercase hex
        assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"));
        assertEquals("\\u001F", StringEscapeUtils.escapeJava("\u001f"));
    }

    @Test
    void testEscapeJavaSingleQuoteNotEscaped() {
        // In Java, single quotes are not escaped
        assertEquals("don't", StringEscapeUtils.escapeJava("don't"));
    }

    @Test
    void testEscapeJavaForwardSlashNotEscaped() {
        assertEquals("path/to/file", StringEscapeUtils.escapeJava("path/to/file"));
    }

    // escapeJava with Writer tests
    @Test
    void testEscapeJavaToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "test\\string\n");
        assertEquals("test\\\\string\\n", writer.toString());
    }

    @Test
    void testEscapeJavaToWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testEscapeJavaToWriterNullWriter() {
        assertThrows(IllegalArgumentException.class, () ->
            StringEscapeUtils.escapeJava(null, "test"));
    }

    // escapeJavaScript tests
    @Test
    void testEscapeJavaScriptNull() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
    }

    @Test
    void testEscapeJavaScriptEmpty() {
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
    }

    @Test
    void testEscapeJavaScriptSingleQuotes() {
        // In JavaScript, single quotes ARE escaped
        assertEquals("don\\'t", StringEscapeUtils.escapeJavaScript("don't"));
    }

    @Test
    void testEscapeJavaScriptDoubleQuotes() {
        assertEquals("\\\"quoted\\\"", StringEscapeUtils.escapeJavaScript("\"quoted\""));
    }

    @Test
    void testEscapeJavaScriptForwardSlash() {
        // In JavaScript, forward slashes ARE escaped
        assertEquals("path\\/to\\/file", StringEscapeUtils.escapeJavaScript("path/to/file"));
    }

    @Test
    void testEscapeJavaScriptBackslash() {
        assertEquals("back\\\\slash", StringEscapeUtils.escapeJavaScript("back\\slash"));
    }

    @Test
    void testEscapeJavaScriptControlChars() {
        assertEquals("line\\nbreak", StringEscapeUtils.escapeJavaScript("line\nbreak"));
        assertEquals("tab\\tstop", StringEscapeUtils.escapeJavaScript("tab\tstop"));
    }

    // escapeJavaScript with Writer tests
    @Test
    void testEscapeJavaScriptToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "test'string/path");
        assertEquals("test\\'string\\/path", writer.toString());
    }

    @Test
    void testEscapeJavaScriptToWriterNullWriter() {
        assertThrows(IllegalArgumentException.class, () ->
            StringEscapeUtils.escapeJavaScript(null, "test"));
    }

    // unescapeJava tests
    @Test
    void testUnescapeJavaNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    @Test
    void testUnescapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.unescapeJava(""));
    }

    @Test
    void testUnescapeJavaSimpleText() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"));
    }

    @Test
    void testUnescapeJavaDoubleQuotes() {
        assertEquals("\"quoted\"", StringEscapeUtils.unescapeJava("\\\"quoted\\\""));
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
    void testUnescapeJavaBackspace() {
        assertEquals("back\bspace", StringEscapeUtils.unescapeJava("back\\bspace"));
    }

    @Test
    void testUnescapeJavaFormFeed() {
        assertEquals("form\ffeed", StringEscapeUtils.unescapeJava("form\\ffeed"));
    }

    @Test
    void testUnescapeJavaSingleQuote() {
        assertEquals("don't", StringEscapeUtils.unescapeJava("don\\'t"));
    }

    @Test
    void testUnescapeJavaUnicode() {
        assertEquals("\u4e2d", StringEscapeUtils.unescapeJava("\\u4e2d"));
    }

    @Test
    void testUnescapeJavaUnicodeLowerCase() {
        assertEquals("\u00a9", StringEscapeUtils.unescapeJava("\\u00a9"));
    }

    @Test
    void testUnescapeJavaTrailingBackslash() {
        assertEquals("trailing\\", StringEscapeUtils.unescapeJava("trailing\\"));
    }

    @Test
    void testUnescapeJavaUnknownEscape() {
        // Unknown escapes are passed through
        assertEquals("x", StringEscapeUtils.unescapeJava("\\x"));
    }

    @Test
    void testUnescapeJavaInvalidUnicode() {
        assertThrows(RuntimeException.class, () ->
            StringEscapeUtils.unescapeJava("\\uZZZZ"));
    }

    // unescapeJava with Writer tests
    @Test
    void testUnescapeJavaToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "test\\\\string\\n");
        assertEquals("test\\string\n", writer.toString());
    }

    @Test
    void testUnescapeJavaToWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testUnescapeJavaToWriterNullWriter() {
        assertThrows(IllegalArgumentException.class, () ->
            StringEscapeUtils.unescapeJava(null, "test"));
    }

    // unescapeJavaScript tests
    @Test
    void testUnescapeJavaScriptNull() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
    }

    @Test
    void testUnescapeJavaScriptSameAsJava() {
        assertEquals(StringEscapeUtils.unescapeJava("test\\nstring"),
            StringEscapeUtils.unescapeJavaScript("test\\nstring"));
    }

    @Test
    void testUnescapeJavaScriptToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "test\\nstring");
        assertEquals("test\nstring", writer.toString());
    }

    // Round-trip tests
    @Test
    void testRoundTripJava() {
        String original = "Hello\nWorld\t\"quoted\"\\backslash";
        String escaped = StringEscapeUtils.escapeJava(original);
        String unescaped = StringEscapeUtils.unescapeJava(escaped);
        assertEquals(original, unescaped);
    }

    @Test
    void testRoundTripJavaScript() {
        String original = "Hello\nWorld\t'quoted'/slash";
        String escaped = StringEscapeUtils.escapeJavaScript(original);
        String unescaped = StringEscapeUtils.unescapeJavaScript(escaped);
        assertEquals(original, unescaped);
    }

    @Test
    void testRoundTripUnicode() {
        String original = "Hello \u4e2d\u6587 World";
        String escaped = StringEscapeUtils.escapeJava(original);
        String unescaped = StringEscapeUtils.unescapeJava(escaped);
        assertEquals(original, unescaped);
    }

    // Constructor test
    @Test
    void testConstructor() {
        // Public constructor is allowed
        assertDoesNotThrow(() -> new StringEscapeUtils());
    }

    // Edge cases
    @Test
    void testEscapeJavaAllControlChars() {
        String controlChars = "\b\n\t\f\r";
        String escaped = StringEscapeUtils.escapeJava(controlChars);
        assertEquals("\\b\\n\\t\\f\\r", escaped);
    }

    @Test
    void testUnescapeJavaConsecutiveEscapes() {
        assertEquals("\n\n\n", StringEscapeUtils.unescapeJava("\\n\\n\\n"));
    }

    @Test
    void testEscapeMixedContent() {
        String mixed = "Line 1\nLine 2\tTabbed \"quoted\"";
        String escaped = StringEscapeUtils.escapeJava(mixed);
        assertTrue(escaped.contains("\\n"));
        assertTrue(escaped.contains("\\t"));
        assertTrue(escaped.contains("\\\""));
    }
}
