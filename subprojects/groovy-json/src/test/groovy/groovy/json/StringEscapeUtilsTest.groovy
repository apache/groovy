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
package groovy.json

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for {@link StringEscapeUtils}.
 */
class StringEscapeUtilsTest {

    // escapeJava tests (from JUnit5)
    @Test
    void testEscapeJavaNull() {
        assertNull(StringEscapeUtils.escapeJava(null))
    }

    @Test
    void testEscapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""))
    }

    @Test
    void testEscapeJavaSimpleText() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"))
    }

    @Test
    void testEscapeJavaBasicString() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"))
    }

    @Test
    void testEscapeJavaDoubleQuotes() {
        assertEquals("They didn't say, \\\"Stop!\\\"",
            StringEscapeUtils.escapeJava("They didn't say, \"Stop!\""))
    }

    @Test
    void testEscapeJavaDoubleQuote() {
        assertEquals("\\\"hello\\\"", StringEscapeUtils.escapeJava("\"hello\""))
    }

    @Test
    void testEscapeJavaBackslash() {
        assertEquals("path\\\\to\\\\file", StringEscapeUtils.escapeJava("path\\to\\file"))
    }

    @Test
    void testEscapeJavaNewline() {
        assertEquals("line1\\nline2", StringEscapeUtils.escapeJava("line1\nline2"))
    }

    @Test
    void testEscapeJavaTab() {
        assertEquals("col1\\tcol2", StringEscapeUtils.escapeJava("col1\tcol2"))
    }

    @Test
    void testEscapeJavaCarriageReturn() {
        assertEquals("line1\\rline2", StringEscapeUtils.escapeJava("line1\rline2"))
    }

    @Test
    void testEscapeJavaBackspace() {
        assertEquals("back\\bspace", StringEscapeUtils.escapeJava("back\bspace"))
    }

    @Test
    void testEscapeJavaFormFeed() {
        assertEquals("form\\ffeed", StringEscapeUtils.escapeJava("form\ffeed"))
    }

    @Test
    void testEscapeJavaFormFeedFromJava() {
        assertEquals("page1\\fpage2", StringEscapeUtils.escapeJava("page1\fpage2"))
    }

    @Test
    void testEscapeJavaUnicodeHigh() {
        // Characters > 0xfff - uses uppercase hex
        assertEquals("\\u4E2D", StringEscapeUtils.escapeJava("\u4e2d")) // Chinese character
    }

    @Test
    void testEscapeJavaUnicodeHighFromJava() {
        // Characters > 0xfff
        def input = "\u4e2d\u6587" // Chinese characters
        def result = StringEscapeUtils.escapeJava(input)
        assertTrue(result.contains("\\u"))
    }

    @Test
    void testEscapeJavaUnicodeMedium() {
        // Characters > 0xff and <= 0xfff - uses uppercase hex
        assertEquals("\\u03B1", StringEscapeUtils.escapeJava("\u03b1")) // Greek alpha
    }

    @Test
    void testEscapeJavaUnicodeMid() {
        // Characters > 0xff and <= 0xfff
        def input = "\u0100" // Latin Extended-A
        def result = StringEscapeUtils.escapeJava(input)
        assertTrue(result.contains("\\u0100"))
    }

    @Test
    void testEscapeJavaUnicodeLow() {
        // Characters >= 0x7f and <= 0xff - uses uppercase hex
        assertEquals("\\u00A9", StringEscapeUtils.escapeJava("\u00a9")) // Copyright symbol
    }

    @Test
    void testEscapeJavaUnicodeLowFromJava() {
        // Characters >= 0x7f and <= 0xff
        def input = "\u007f" // DEL character
        def result = StringEscapeUtils.escapeJava(input)
        assertTrue(result.contains("\\u007F") || result.contains("\\u007f"))
    }

    @Test
    void testEscapeJavaControlChars() {
        // Control chars < 32 that aren't special - uses uppercase hex
        assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"))
        assertEquals("\\u001F", StringEscapeUtils.escapeJava("\u001f"))
    }

    @Test
    void testEscapeJavaControlCharacter() {
        // Control characters < 32 (not the standard escape sequences)
        def input = "\u0001" // SOH
        def result = StringEscapeUtils.escapeJava(input)
        assertTrue(result.contains("\\u0001"))
    }

    @Test
    void testEscapeJavaControlCharacterBetween15And31() {
        def input = "\u0010" // DLE
        def result = StringEscapeUtils.escapeJava(input)
        assertTrue(result.contains("\\u0010"))
    }

    @Test
    void testEscapeJavaSingleQuoteNotEscaped() {
        // In Java, single quotes are not escaped
        assertEquals("don't", StringEscapeUtils.escapeJava("don't"))
    }

    @Test
    void testEscapeJavaSingleQuoteNotEscapedFromJava() {
        assertEquals("it's", StringEscapeUtils.escapeJava("it's"))
    }

    @Test
    void testEscapeJavaForwardSlashNotEscaped() {
        assertEquals("path/to/file", StringEscapeUtils.escapeJava("path/to/file"))
    }

    // escapeJava with Writer tests (from JUnit5)
    @Test
    void testEscapeJavaToWriter() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.escapeJava(writer, "test\\string\n")
        assertEquals("test\\\\string\\n", writer.toString())
    }

    @Test
    void testEscapeJavaToWriterFromJava() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.escapeJava(writer, "hello\"world")
        assertEquals("hello\\\"world", writer.toString())
    }

    @Test
    void testEscapeJavaToWriterNull() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.escapeJava(writer, null)
        assertEquals("", writer.toString())
    }

    @Test
    void testEscapeJavaToWriterNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.escapeJava(null, "test")
        })
    }

    @Test
    void testEscapeJavaToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.escapeJava((Writer) null, "test")
        })
    }

    // escapeJavaScript tests
    @Test
    void testEscapeJavaScript() {
        assertEquals("it\\'s", StringEscapeUtils.escapeJavaScript("it's"))
    }

    @Test
    void testEscapeJavaScriptNull() {
        assertNull(StringEscapeUtils.escapeJavaScript(null))
    }

    @Test
    void testEscapeJavaScriptEmpty() {
        assertEquals("", StringEscapeUtils.escapeJavaScript(""))
    }

    @Test
    void testEscapeJavaScriptSingleQuotes() {
        // In JavaScript, single quotes ARE escaped
        assertEquals("don\\'t", StringEscapeUtils.escapeJavaScript("don't"))
    }

    @Test
    void testEscapeJavaScriptDoubleQuotes() {
        assertEquals("\\\"quoted\\\"", StringEscapeUtils.escapeJavaScript("\"quoted\""))
    }

    @Test
    void testEscapeJavaScriptForwardSlash() {
        // In JavaScript, forward slashes ARE escaped
        assertEquals("path\\/to\\/file", StringEscapeUtils.escapeJavaScript("path/to/file"))
    }

    @Test
    void testEscapeJavaScriptBackslash() {
        assertEquals("back\\\\slash", StringEscapeUtils.escapeJavaScript("back\\slash"))
    }

    @Test
    void testEscapeJavaScriptControlChars() {
        assertEquals("line\\nbreak", StringEscapeUtils.escapeJavaScript("line\nbreak"))
        assertEquals("tab\\tstop", StringEscapeUtils.escapeJavaScript("tab\tstop"))
    }

    // escapeJavaScript with Writer tests
    @Test
    void testEscapeJavaScriptToWriter() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.escapeJavaScript(writer, "test'string/path")
        assertEquals("test\\'string\\/path", writer.toString())
    }

    @Test
    void testEscapeJavaScriptToWriterFromJava() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.escapeJavaScript(writer, "it's \"quoted\"")
        assertEquals("it\\'s \\\"quoted\\\"", writer.toString())
    }

    @Test
    void testEscapeJavaScriptToWriterNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.escapeJavaScript(null, "test")
        })
    }

    @Test
    void testEscapeJavaScriptToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.escapeJavaScript((Writer) null, "test")
        })
    }

    // unescapeJava tests (from JUnit5)
    @Test
    void testUnescapeJavaNull() {
        assertNull(StringEscapeUtils.unescapeJava(null))
    }

    @Test
    void testUnescapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.unescapeJava(""))
    }

    @Test
    void testUnescapeJavaSimpleText() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"))
    }

    @Test
    void testUnescapeJavaBasicString() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"))
    }

    @Test
    void testUnescapeJavaDoubleQuotes() {
        assertEquals("\"quoted\"", StringEscapeUtils.unescapeJava("\\\"quoted\\\""))
    }

    @Test
    void testUnescapeJavaDoubleQuote() {
        assertEquals("\"hello\"", StringEscapeUtils.unescapeJava("\\\"hello\\\""))
    }

    @Test
    void testUnescapeJavaBackslash() {
        assertEquals("path\\to\\file", StringEscapeUtils.unescapeJava("path\\\\to\\\\file"))
    }

    @Test
    void testUnescapeJavaNewline() {
        assertEquals("line1\nline2", StringEscapeUtils.unescapeJava("line1\\nline2"))
    }

    @Test
    void testUnescapeJavaTab() {
        assertEquals("col1\tcol2", StringEscapeUtils.unescapeJava("col1\\tcol2"))
    }

    @Test
    void testUnescapeJavaCarriageReturn() {
        assertEquals("line1\rline2", StringEscapeUtils.unescapeJava("line1\\rline2"))
    }

    @Test
    void testUnescapeJavaBackspace() {
        assertEquals("back\bspace", StringEscapeUtils.unescapeJava("back\\bspace"))
    }

    @Test
    void testUnescapeJavaFormFeed() {
        assertEquals("form\ffeed", StringEscapeUtils.unescapeJava("form\\ffeed"))
    }

    @Test
    void testUnescapeJavaFormFeedFromJava() {
        assertEquals("page1\fpage2", StringEscapeUtils.unescapeJava("page1\\fpage2"))
    }

    @Test
    void testUnescapeJavaSingleQuote() {
        assertEquals("don't", StringEscapeUtils.unescapeJava("don\\'t"))
    }

    @Test
    void testUnescapeJavaSingleQuoteFromJava() {
        assertEquals("'hello'", StringEscapeUtils.unescapeJava("\\'hello\\'"))
    }

    @Test
    void testUnescapeJavaUnicode() {
        assertEquals("\u4e2d", StringEscapeUtils.unescapeJava("\\u4e2d"))
    }

    @Test
    void testUnescapeJavaUnicodeFromJava() {
        assertEquals("\u0041", StringEscapeUtils.unescapeJava("\\u0041"))
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"))
    }

    @Test
    void testUnescapeJavaUnicodeMultiple() {
        assertEquals("AB", StringEscapeUtils.unescapeJava("\\u0041\\u0042"))
    }

    @Test
    void testUnescapeJavaUnicodeLowerCase() {
        assertEquals("\u00a9", StringEscapeUtils.unescapeJava("\\u00a9"))
    }

    @Test
    void testUnescapeJavaTrailingBackslash() {
        assertEquals("trailing\\", StringEscapeUtils.unescapeJava("trailing\\"))
    }

    @Test
    void testUnescapeJavaUnknownEscape() {
        // Unknown escapes are passed through
        assertEquals("x", StringEscapeUtils.unescapeJava("\\x"))
    }

    @Test
    void testUnescapeJavaInvalidUnicode() {
        assertThrows(RuntimeException, { ->
            StringEscapeUtils.unescapeJava("\\uZZZZ")
        })
    }

    @Test
    void testUnescapeJavaMixedContent() {
        assertEquals("hello\nworld\ttab", StringEscapeUtils.unescapeJava("hello\\nworld\\ttab"))
    }

    // unescapeJava with Writer tests
    @Test
    void testUnescapeJavaToWriter() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.unescapeJava(writer, "test\\\\string\\n")
        assertEquals("test\\string\n", writer.toString())
    }

    @Test
    void testUnescapeJavaToWriterFromJava() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.unescapeJava(writer, "hello\\\"world")
        assertEquals("hello\"world", writer.toString())
    }

    @Test
    void testUnescapeJavaToWriterNull() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.unescapeJava(writer, null)
        assertEquals("", writer.toString())
    }

    @Test
    void testUnescapeJavaToWriterNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.unescapeJava(null, "test")
        })
    }

    @Test
    void testUnescapeJavaToWriterWithNullWriter() {
        assertThrows(IllegalArgumentException, { ->
            StringEscapeUtils.unescapeJava((Writer) null, "test")
        })
    }

    // unescapeJavaScript tests
    @Test
    void testUnescapeJavaScriptNull() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null))
    }

    @Test
    void testUnescapeJavaScriptSameAsJava() {
        assertEquals(StringEscapeUtils.unescapeJava("test\\nstring"),
            StringEscapeUtils.unescapeJavaScript("test\\nstring"))
    }

    @Test
    void testUnescapeJavaScriptSameAsJavaFromJava() {
        def escaped = "hello\\\"world\\'test"
        assertEquals(StringEscapeUtils.unescapeJava(escaped),
                     StringEscapeUtils.unescapeJavaScript(escaped))
    }

    @Test
    void testUnescapeJavaScriptToWriter() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.unescapeJavaScript(writer, "test\\nstring")
        assertEquals("test\nstring", writer.toString())
    }

    @Test
    void testUnescapeJavaScriptToWriterFromJava() throws IOException {
        def writer = new StringWriter()
        StringEscapeUtils.unescapeJavaScript(writer, "hello\\nworld")
        assertEquals("hello\nworld", writer.toString())
    }

    // Round-trip tests
    @Test
    void testRoundTripJava() {
        def original = "Hello\nWorld\t\"quoted\"\\backslash"
        def escaped = StringEscapeUtils.escapeJava(original)
        def unescaped = StringEscapeUtils.unescapeJava(escaped)
        assertEquals(original, unescaped)
    }

    @Test
    void testRoundTripJavaScript() {
        def original = "Hello\nWorld\t'quoted'/slash"
        def escaped = StringEscapeUtils.escapeJavaScript(original)
        def unescaped = StringEscapeUtils.unescapeJavaScript(escaped)
        assertEquals(original, unescaped)
    }

    @Test
    void testRoundTripJavaScriptFromJava() {
        def original = "Hello'World"
        def escaped = StringEscapeUtils.escapeJavaScript(original)
        def unescaped = StringEscapeUtils.unescapeJavaScript(escaped)
        assertEquals(original, unescaped)
    }

    @Test
    void testRoundTripUnicode() {
        def original = "Hello \u4e2d\u6587 World"
        def escaped = StringEscapeUtils.escapeJava(original)
        def unescaped = StringEscapeUtils.unescapeJava(escaped)
        assertEquals(original, unescaped)
    }

    // Constructor test (from JUnit5)
    @Test
    void testConstructor() {
        // Public constructor is allowed
        assertDoesNotThrow({ -> new StringEscapeUtils() } as org.junit.jupiter.api.function.Executable)
    }

    @Test
    void testConstructorFromJava() {
        // Test that constructor can be called (even though it's typically used statically)
        def utils = new StringEscapeUtils()
        assertNotNull(utils)
    }

    // Edge cases (from JUnit5)
    @Test
    void testEscapeJavaAllControlChars() {
        def controlChars = "\b\n\t\f\r"
        def escaped = StringEscapeUtils.escapeJava(controlChars)
        assertEquals("\\b\\n\\t\\f\\r", escaped)
    }

    @Test
    void testUnescapeJavaConsecutiveEscapes() {
        assertEquals("\n\n\n", StringEscapeUtils.unescapeJava("\\n\\n\\n"))
    }

    @Test
    void testEscapeMixedContent() {
        def mixed = "Line 1\nLine 2\tTabbed \"quoted\""
        def escaped = StringEscapeUtils.escapeJava(mixed)
        assertTrue(escaped.contains("\\n"))
        assertTrue(escaped.contains("\\t"))
        assertTrue(escaped.contains("\\\""))
    }

    // From Java: all control characters
    @Test
    void testAllControlCharacters() {
        // Test all control characters (0x00 to 0x1f except standard escapes)
        for (int i = 0; i < 32; i++) {
            if (i != '\b' && i != '\t' && i != '\n' && i != '\f' && i != '\r') {
                def input = String.valueOf((char) i)
                def escaped = StringEscapeUtils.escapeJava(input)
                assertTrue(escaped.startsWith("\\u000") || escaped.startsWith("\\u00"),
                    "Control char " + i + " should be unicode escaped")
            }
        }
    }
}
