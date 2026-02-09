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
package groovy.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for CharsetToolkit class.
 */
class CharsetToolkitTest {

    @TempDir
    Path tempDir

    // Constructor tests
    @Test
    void testConstructorWithEmptyFile() throws IOException {
        def file = tempDir.resolve("empty.txt").toFile()
        file.createNewFile()

        def toolkit = new CharsetToolkit(file)
        assertNotNull(toolkit.getCharset())
    }

    @Test
    void testConstructorWithSmallFile() throws IOException {
        def file = tempDir.resolve("small.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        assertNotNull(toolkit.getCharset())
    }

    @Test
    void testConstructorWithLargeFile() throws IOException {
        def file = tempDir.resolve("large.txt").toFile()
        def sb = new StringBuilder()
        for (int i = 0; i < 5000; i++) {
            sb.append("Hello World\n")
        }
        Files.writeString(file.toPath(), sb.toString())

        def toolkit = new CharsetToolkit(file)
        assertNotNull(toolkit.getCharset())
    }

    // getDefaultSystemCharset tests
    @Test
    void testGetDefaultSystemCharset() {
        def charset = CharsetToolkit.getDefaultSystemCharset()
        assertNotNull(charset)
        assertEquals(Charset.defaultCharset(), charset)
    }

    // setDefaultCharset tests
    @Test
    void testSetDefaultCharset() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        toolkit.setDefaultCharset(StandardCharsets.ISO_8859_1)
        assertEquals(StandardCharsets.ISO_8859_1, toolkit.getDefaultCharset())
    }

    @Test
    void testSetDefaultCharsetNull() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        toolkit.setDefaultCharset(null)
        // Should fall back to system default
        assertEquals(CharsetToolkit.getDefaultSystemCharset(), toolkit.getDefaultCharset())
    }

    // getEnforce8Bit/setEnforce8Bit tests
    @Test
    void testEnforce8BitDefault() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        assertTrue(toolkit.getEnforce8Bit())
    }

    @Test
    void testSetEnforce8Bit() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        toolkit.setEnforce8Bit(false)
        assertFalse(toolkit.getEnforce8Bit())
    }

    // UTF-8 BOM tests
    @Test
    void testHasUTF8Bom() throws IOException {
        def file = tempDir.resolve("utf8bom.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xEF, 0xBB, 0xBF] as byte[]) // UTF-8 BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_8))
        }

        def toolkit = new CharsetToolkit(file)
        assertTrue(toolkit.hasUTF8Bom())
        assertEquals(StandardCharsets.UTF_8, toolkit.getCharset())
    }

    @Test
    void testNoUTF8Bom() throws IOException {
        def file = tempDir.resolve("nobom.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        assertFalse(toolkit.hasUTF8Bom())
    }

    // UTF-16 LE BOM tests
    @Test
    void testHasUTF16LEBom() throws IOException {
        def file = tempDir.resolve("utf16le.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xFF, 0xFE] as byte[]) // UTF-16 LE BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_16LE))
        }

        def toolkit = new CharsetToolkit(file)
        assertTrue(toolkit.hasUTF16LEBom())
        assertEquals(StandardCharsets.UTF_16LE, toolkit.getCharset())
    }

    @Test
    void testNoUTF16LEBom() throws IOException {
        def file = tempDir.resolve("nobom.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        assertFalse(toolkit.hasUTF16LEBom())
    }

    // UTF-16 BE BOM tests
    @Test
    void testHasUTF16BEBom() throws IOException {
        def file = tempDir.resolve("utf16be.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xFE, 0xFF] as byte[]) // UTF-16 BE BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_16BE))
        }

        def toolkit = new CharsetToolkit(file)
        assertTrue(toolkit.hasUTF16BEBom())
        assertEquals(StandardCharsets.UTF_16BE, toolkit.getCharset())
    }

    @Test
    void testNoUTF16BEBom() throws IOException {
        def file = tempDir.resolve("nobom.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        assertFalse(toolkit.hasUTF16BEBom())
    }

    // BOM with short buffer tests
    @Test
    void testBomCheckWithOneByte() throws IOException {
        def file = tempDir.resolve("onebyte.txt").toFile()
        Files.write(file.toPath(), [0x48] as byte[]) // Just 'H'

        def toolkit = new CharsetToolkit(file)
        assertFalse(toolkit.hasUTF8Bom())
        assertFalse(toolkit.hasUTF16LEBom())
        assertFalse(toolkit.hasUTF16BEBom())
    }

    @Test
    void testBomCheckWithTwoBytes() throws IOException {
        def file = tempDir.resolve("twobytes.txt").toFile()
        Files.write(file.toPath(), [0x48, 0x69] as byte[]) // "Hi"

        def toolkit = new CharsetToolkit(file)
        assertFalse(toolkit.hasUTF8Bom())
        assertFalse(toolkit.hasUTF16LEBom())
        assertFalse(toolkit.hasUTF16BEBom())
    }

    // US-ASCII detection tests
    @Test
    void testUsAsciiDetectionWithEnforce8BitTrue() throws IOException {
        def file = tempDir.resolve("ascii.txt").toFile()
        Files.writeString(file.toPath(), "Hello World") // Pure ASCII

        def toolkit = new CharsetToolkit(file)
        toolkit.setEnforce8Bit(true)
        // Should return default charset instead of US-ASCII
        assertNotEquals(StandardCharsets.US_ASCII, toolkit.getCharset())
    }

    @Test
    void testUsAsciiDetectionWithEnforce8BitFalse() throws IOException {
        def file = tempDir.resolve("ascii.txt").toFile()
        Files.writeString(file.toPath(), "Hello World") // Pure ASCII

        def toolkit = new CharsetToolkit(file)
        toolkit.setEnforce8Bit(false)
        assertEquals(StandardCharsets.US_ASCII, toolkit.getCharset())
    }

    // UTF-8 detection (without BOM) tests
    @Test
    void testUtf8DetectionTwoByteSequence() throws IOException {
        def file = tempDir.resolve("utf8-2byte.txt").toFile()
        // Create enough content to trigger UTF-8 detection
        def sb = new StringBuilder()
        for (int i = 0; i < 100; i++) {
            sb.append("Héllo Wörld ") // Contains two-byte UTF-8 sequences
        }
        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8)

        def toolkit = new CharsetToolkit(file)
        assertEquals(StandardCharsets.UTF_8, toolkit.getCharset())
    }

    @Test
    void testUtf8DetectionThreeByteSequence() throws IOException {
        def file = tempDir.resolve("utf8-3byte.txt").toFile()
        // Create content with three-byte UTF-8 sequences (Chinese characters)
        def sb = new StringBuilder()
        for (int i = 0; i < 100; i++) {
            sb.append("你好世界 ") // Chinese characters are 3-byte UTF-8 sequences
        }
        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8)

        def toolkit = new CharsetToolkit(file)
        assertEquals(StandardCharsets.UTF_8, toolkit.getCharset())
    }

    @Test
    void testUtf8DetectionFourByteSequence() throws IOException {
        def file = tempDir.resolve("utf8-4byte.txt").toFile()
        // Create content with four-byte UTF-8 sequences (emoji)
        def sb = new StringBuilder()
        for (int i = 0; i < 100; i++) {
            sb.append("\uD83D\uDE00 ") // Grinning face emoji is a 4-byte UTF-8 sequence
        }
        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8)

        def toolkit = new CharsetToolkit(file)
        assertEquals(StandardCharsets.UTF_8, toolkit.getCharset())
    }

    // Invalid UTF-8 detection
    @Test
    void testInvalidUtf8FallsBackToDefault() throws IOException {
        def file = tempDir.resolve("invalid-utf8.txt").toFile()
        // Write bytes that look like start of UTF-8 multi-byte but aren't valid
        new FileOutputStream(file).withCloseable { fos ->
            // Write some content with invalid UTF-8 sequence
            def content = new byte[100]
            for (int i = 0; i < content.length - 1; i++) {
                content[i] = (byte) 'A' // ASCII
            }
            // Add an invalid high-order byte without proper continuation
            content[50] = (byte) 0xC0 // Start of 2-byte sequence
            content[51] = (byte) 0x20 // Space (not a valid continuation byte)
            fos.write(content)
        }

        def toolkit = new CharsetToolkit(file)
        // Should fall back to default charset since it's not valid UTF-8
        assertNotNull(toolkit.getCharset())
    }

    // getReader tests
    @Test
    void testGetReaderAscii() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello World")

        def toolkit = new CharsetToolkit(file)
        def reader = toolkit.getReader()

        assertNotNull(reader)
        assertEquals("Hello World", reader.readLine())
        reader.close()
    }

    @Test
    void testGetReaderUtf8WithBom() throws IOException {
        def file = tempDir.resolve("utf8bom.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xEF, 0xBB, 0xBF] as byte[]) // UTF-8 BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_8))
        }

        def toolkit = new CharsetToolkit(file)
        def reader = toolkit.getReader()

        assertNotNull(reader)
        // BOM should be skipped
        def line = reader.readLine()
        assertEquals("Hello", line)
        reader.close()
    }

    @Test
    void testGetReaderUtf16LEWithBom() throws IOException {
        def file = tempDir.resolve("utf16le.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xFF, 0xFE] as byte[]) // UTF-16 LE BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_16LE))
        }

        def toolkit = new CharsetToolkit(file)
        def reader = toolkit.getReader()

        assertNotNull(reader)
        reader.close()
    }

    @Test
    void testGetReaderUtf16BEWithBom() throws IOException {
        def file = tempDir.resolve("utf16be.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xFE, 0xFF] as byte[]) // UTF-16 BE BOM
            fos.write("Hello".getBytes(StandardCharsets.UTF_16BE))
        }

        def toolkit = new CharsetToolkit(file)
        def reader = toolkit.getReader()

        assertNotNull(reader)
        reader.close()
    }

    // getAvailableCharsets tests
    @Test
    void testGetAvailableCharsets() {
        def charsets = CharsetToolkit.getAvailableCharsets()

        assertNotNull(charsets)
        assertTrue(charsets.length > 0)

        // Should contain common charsets
        def hasUtf8 = false
        for (Charset charset : charsets) {
            if (StandardCharsets.UTF_8 == charset) {
                hasUtf8 = true
                break
            }
        }
        assertTrue(hasUtf8)
    }

    // getCharset caching test
    @Test
    void testGetCharsetCaching() throws IOException {
        def file = tempDir.resolve("test.txt").toFile()
        Files.writeString(file.toPath(), "Hello")

        def toolkit = new CharsetToolkit(file)
        def first = toolkit.getCharset()
        def second = toolkit.getCharset()

        assertSame(first, second) // Should return cached value
    }

    // Edge case: file with just BOM
    @Test
    void testFileWithOnlyUtf8Bom() throws IOException {
        def file = tempDir.resolve("bom-only.txt").toFile()
        new FileOutputStream(file).withCloseable { fos ->
            fos.write([0xEF, 0xBB, 0xBF] as byte[]) // UTF-8 BOM only
        }

        def toolkit = new CharsetToolkit(file)
        assertTrue(toolkit.hasUTF8Bom())
        assertEquals(StandardCharsets.UTF_8, toolkit.getCharset())
    }

    // Edge case: file with mixed content
    @Test
    void testLongAsciiFileWithEnforce8BitFalse() throws IOException {
        def file = tempDir.resolve("long-ascii.txt").toFile()
        def sb = new StringBuilder()
        for (int i = 0; i < 1000; i++) {
            sb.append("abcdefghij") // Pure ASCII
        }
        Files.writeString(file.toPath(), sb.toString())

        def toolkit = new CharsetToolkit(file)
        toolkit.setEnforce8Bit(false)
        assertEquals(StandardCharsets.US_ASCII, toolkit.getCharset())
    }
}
