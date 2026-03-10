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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for JsonSlurperClassic class.
 */
class JsonSlurperClassicTest {

    private JsonSlurperClassic slurper

    @BeforeEach
    void setUp() {
        slurper = new JsonSlurperClassic()
    }

    // parseText tests
    @Test
    void testParseTextWithNull() {
        assertThrows(IllegalArgumentException, { -> slurper.parseText(null) })
    }

    @Test
    void testParseTextWithEmptyString() {
        assertThrows(IllegalArgumentException, { -> slurper.parseText("") })
    }

    @Test
    void testParseTextSimpleObject() {
        def result = (Map) slurper.parseText('{"name":"John"}')
        assertEquals("John", result.get("name"))
    }

    @Test
    void testParseTextSimpleArray() {
        def result = (List) slurper.parseText("[1, 2, 3]")
        assertEquals(3, result.size())
        assertEquals(1, result.get(0))
    }

    @Test
    void testParseTextNestedObject() {
        def json = '{"person":{"name":"John","age":30}}'
        def result = (Map) slurper.parseText(json)
        def person = (Map) result.get("person")
        assertEquals("John", person.get("name"))
        assertEquals(30, person.get("age"))
    }

    @Test
    void testParseTextNestedArray() {
        def json = '{"matrix":[[1,2],[3,4]]}'
        def result = (Map) slurper.parseText(json)
        def matrix = (List) result.get("matrix")
        assertEquals(2, matrix.size())
        assertEquals([1, 2], matrix.get(0))
    }

    @Test
    void testParseTextWithAllTypes() {
        def json = '{"string":"hello","number":42,"float":3.14,"bool":true,"null":null}'
        def result = (Map) slurper.parseText(json)
        assertEquals("hello", result.get("string"))
        assertEquals(42, result.get("number"))
        // JsonSlurperClassic uses BigDecimal for decimals
        assertEquals(new java.math.BigDecimal("3.14"), result.get("float"))
        assertEquals(true, result.get("bool"))
        assertNull(result.get("null"))
    }

    @Test
    void testParseTextEmptyObject() {
        def result = (Map) slurper.parseText("{}")
        assertTrue(result.isEmpty())
    }

    @Test
    void testParseTextEmptyArray() {
        def result = (List) slurper.parseText("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    void testParseTextArrayOfObjects() {
        def json = '[{"id":1},{"id":2}]'
        def result = (List) slurper.parseText(json)
        assertEquals(2, result.size())
        assertEquals(1, ((Map) result.get(0)).get("id"))
    }

    @Test
    void testParseTextWithEscapedCharacters() {
        def json = '{"message":"Hello\\nWorld"}'
        def result = (Map) slurper.parseText(json)
        assertEquals("Hello\nWorld", result.get("message"))
    }

    @Test
    void testParseTextWithUnicode() {
        def json = '{"greeting":"Hello \\u4e16\\u754c"}'
        def result = (Map) slurper.parseText(json)
        assertEquals("Hello 世界", result.get("greeting"))
    }

    @Test
    void testParseTextInvalidJson() {
        assertThrows(JsonException, { -> slurper.parseText("not json") })
    }

    @Test
    void testParseTextInvalidStartToken() {
        assertThrows(JsonException, { -> slurper.parseText('"just a string"') })
    }

    // parse(Reader) tests
    @Test
    void testParseReader() {
        def reader = new StringReader('{"key":"value"}')
        def result = (Map) slurper.parse(reader)
        assertEquals("value", result.get("key"))
    }

    @Test
    void testParseReaderArray() {
        def reader = new StringReader("[1, 2, 3]")
        def result = (List) slurper.parse(reader)
        assertEquals(3, result.size())
    }

    // parse(File) tests
    @Test
    void testParseFile(@TempDir Path tempDir) throws Exception {
        def jsonFile = tempDir.resolve("test.json")
        Files.writeString(jsonFile, '{"name":"test"}')

        def result = (Map) slurper.parse(jsonFile.toFile())
        assertEquals("test", result.get("name"))
    }

    @Test
    void testParseFileWithCharset(@TempDir Path tempDir) throws Exception {
        def jsonFile = tempDir.resolve("test.json")
        Files.write(jsonFile, '{"name":"test"}'.getBytes(StandardCharsets.UTF_8))

        def result = (Map) slurper.parse(jsonFile.toFile(), "UTF-8")
        assertEquals("test", result.get("name"))
    }

    @Test
    void testParseFileNonExistent() {
        def nonExistent = new File("non_existent_file.json")
        assertThrows(JsonException, { -> slurper.parse(nonExistent) })
    }

    // Complex JSON tests
    @Test
    void testParseComplexStructure() {
        def json = '''
            {
                "users": [
                    {"name": "Alice", "age": 25, "active": true},
                    {"name": "Bob", "age": 30, "active": false}
                ],
                "metadata": {
                    "total": 2,
                    "page": 1
                }
            }
            '''
        def result = (Map) slurper.parseText(json)

        def users = (List) result.get("users")
        assertEquals(2, users.size())

        def alice = (Map) users.get(0)
        assertEquals("Alice", alice.get("name"))
        assertEquals(25, alice.get("age"))
        assertEquals(true, alice.get("active"))

        def metadata = (Map) result.get("metadata")
        assertEquals(2, metadata.get("total"))
    }

    @Test
    void testParseNegativeNumbers() {
        def json = '{"value":-42,"float":-3.14}'
        def result = (Map) slurper.parseText(json)
        assertEquals(-42, result.get("value"))
        // JsonSlurperClassic uses BigDecimal for decimals
        assertEquals(new java.math.BigDecimal("-3.14"), result.get("float"))
    }

    @Test
    void testParseScientificNotation() {
        def json = '{"value":1.23e10}'
        def result = (Map) slurper.parseText(json)
        // JsonSlurperClassic uses BigDecimal for decimal notation
        assertEquals(new java.math.BigDecimal("1.23E+10"), result.get("value"))
    }

    @Test
    void testParseBooleans() {
        def json = '{"yes":true,"no":false}'
        def result = (Map) slurper.parseText(json)
        assertEquals(true, result.get("yes"))
        assertEquals(false, result.get("no"))
    }

    @Test
    void testParseMixedArray() {
        def json = '[1, "two", true, null, {"key":"value"}]'
        def result = (List) slurper.parseText(json)
        assertEquals(5, result.size())
        assertEquals(1, result.get(0))
        assertEquals("two", result.get(1))
        assertEquals(true, result.get(2))
        assertNull(result.get(3))
        assertTrue(result.get(4) instanceof Map)
    }

    @Test
    void testParseWithTrailingCommaInObject() {
        // JsonSlurperClassic appears to be lenient with trailing commas
        def result = (Map) slurper.parseText('{"key":"value",}')
        assertEquals("value", result.get("key"))
    }

    @Test
    void testParseWithTrailingCommaInArray() {
        // JsonSlurperClassic appears to be lenient with trailing commas
        def result = (List) slurper.parseText("[1, 2, 3,]")
        assertEquals(3, result.size())
    }

    @Test
    void testParseDeepNesting() {
        def json = '{"a":{"b":{"c":{"d":{"e":"deep"}}}}}'
        def result = (Map) slurper.parseText(json)
        def a = (Map) result.get("a")
        def b = (Map) a.get("b")
        def c = (Map) b.get("c")
        def d = (Map) c.get("d")
        assertEquals("deep", d.get("e"))
    }

    @Test
    void testParseDeepArrayNesting() {
        def json = '[[[["deep"]]]]'
        def result = (List) slurper.parseText(json)
        def l1 = (List) result.get(0)
        def l2 = (List) l1.get(0)
        def l3 = (List) l2.get(0)
        assertEquals("deep", l3.get(0))
    }

    @Test
    void testParseWhitespace() {
        def json = '  {  "key"  :  "value"  }  '
        def result = (Map) slurper.parseText(json)
        assertEquals("value", result.get("key"))
    }

    @Test
    void testParseMultilineJson() {
        def json = '''
            {
                "key": "value"
            }
            '''
        def result = (Map) slurper.parseText(json)
        assertEquals("value", result.get("key"))
    }

    @Test
    void testParseLargeNumbers() {
        def json = '{"big":9999999999999999999}'
        def result = (Map) slurper.parseText(json)
        assertNotNull(result.get("big"))
    }

    @Test
    void testParseZero() {
        def json = '{"zero":0}'
        def result = (Map) slurper.parseText(json)
        assertEquals(0, result.get("zero"))
    }

    @Test
    void testParseEmptyStringValue() {
        def json = '{"empty":""}'
        def result = (Map) slurper.parseText(json)
        assertEquals("", result.get("empty"))
    }

    @Test
    void testParseSpecialCharactersInString() {
        def json = '{"special":"tab\\there\\nnewline"}'
        def result = (Map) slurper.parseText(json)
        assertEquals("tab\there\nnewline", result.get("special"))
    }

    @Test
    void testParseQuoteInString() {
        def json = '{"quote":"say \\"hello\\""}'
        def result = (Map) slurper.parseText(json)
        assertEquals('say "hello"', result.get("quote"))
    }

    @Test
    void testParseBackslashInString() {
        def json = '{"path":"C:\\\\Users\\\\test"}'
        def result = (Map) slurper.parseText(json)
        assertEquals("C:\\Users\\test", result.get("path"))
    }

    @Test
    void testParseSlashInString() {
        def json = '{"url":"http:\\/\\/example.com"}'
        def result = (Map) slurper.parseText(json)
        assertEquals("http://example.com", result.get("url"))
    }
}
