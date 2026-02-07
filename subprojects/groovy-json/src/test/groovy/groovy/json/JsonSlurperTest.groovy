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

import org.apache.groovy.json.internal.Value
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class JsonSlurperTest {

    def parser

    @BeforeEach
    void setUp() {
        parser = new JsonSlurper()
    }

    @Test
    void testJsonShouldStartWithCurlyOrBracket() {
        /* We can handle parsing boolean, numbers, and such. */
        parser.parseText("true")
    }

    @Test
    void testEmptyStructures() {
        assert parser.parseText("[]") == []
        assert parser.parseText("{}") == [:]
    }

    @Test
    void testArrayWithSimpleValues() {
        assert parser.parseText('[123, "abc", true, false, null]') == [123, "abc", true, false, null]

        shouldFail(JsonException) {
            parser.parseText('[123 "abc"]')
        }

        shouldFail(JsonException) {
            parser.parseText('[123, "abc"')
        }
    }

    @Test
    void testAddingToEmptyList() {
        def list = parser.parseText('[]')
        list << "Hello"
        list << "World"
        assert list == ["Hello", "World"]
    }

    @Test
    void testParseNum() {
        int i = parser.parseText('123')
        int i2 = 123
        assert i == i2
    }

    @Test
    void testNegNum() {
        int i = parser.parseText('-123')
        int i2 = -123
        assert i == i2
    }

    @Test
    void testNegNumWithSpace() {
        int i = parser.parseText('   -123')
        int i2 = -123
        assert i == i2
    }

    @Test
    void testLargeNegNumWithSpace() {
        int i = parser.parseText('   -1234567891')
        int i2 = -1234567891
        assert i == i2
    }

    @Test
    void testWithSpaces() {
        int num = ((Number) parser.parseText("           123")).intValue()
        int num2 = 123
        boolean ok = num == num2 || die("" + num)
    }

    @Test
    void testParseLargeNum() {
        long num = parser.parseText("" + Long.MAX_VALUE)
        long num2 = Long.MAX_VALUE
        assert num == num2
    }

    @Test
    void testParseSmallNum() {
        long num = parser.parseText("" + Long.MIN_VALUE)
        long num2 = Long.MIN_VALUE
        assert num == num2
    }

    @Test
    void testParseLargeDecimal() {
        double num = parser.parseText("" + Double.MAX_VALUE)
        double num2 = Double.MAX_VALUE
        assert num == num2
    }

    @Test
    void testParseSmallDecimal() {
        double num = parser.parseText("" + Double.MIN_VALUE)
        double num2 = Double.MIN_VALUE
        assert num == num2
    }

    @Test
    void testOutputTypes() {
        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText('"hello"').class == String
        } else {
            assert parser.parseText('"hello"') instanceof CharSequence
        }

        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText('123.45').class == BigDecimal
        } else {
            assert parser.parseText('123.45') instanceof Number
        }

        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText('123').class == Integer
        } else {
            assert parser.parseText('123') instanceof Number
        }

        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText('12345678912345').class == Long
        } else {
            assert parser.parseText('12345678912345') instanceof Number
        }

        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText('true').class == Boolean
        } else {
            assert parser.parseText('true') instanceof Value
        }

        assert parser.parseText('[1,2,3,4]') instanceof List

        assert parser.parseText('{"message":"Hello"}') instanceof Map

        if (parser.type in [JsonParserType.INDEX_OVERLAY, JsonParserType.LAX]) {
            assert parser.parseText('null') instanceof Value
        }
    }

    @Test
    void testNull() {
        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText("null") == null
        } else {
            assert parser.parseText("null").toValue() == null
        }
    }

    @Test
    void testBoolean() {
        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText("true") == true
            assert parser.parseText("false") == false
        } else {
            assert parser.parseText("true").toValue() == true
            assert parser.parseText("false").toValue() == false
        }
    }

    void exactly312Test() {
        assert parser.parseText('22') == 22
        assert parser.parseText('-22') == -22
        assert parser.parseText('-22.0065') == -22.0065
    }

    @Test
    void testArrayOfArrayWithSimpleValues() {
        assert parser.parseText('[1, 2, 3, ["a", "b", "c", [true, false], "d"], 4]') ==
                [1, 2, 3, ["a", "b", "c", [true, false], "d"], 4]

        shouldFail(JsonException) { parser.parseText('[') }
        shouldFail(JsonException) { parser.parseText('[,]') }
        shouldFail(JsonException) { parser.parseText('[1') }
        shouldFail(JsonException) { parser.parseText('[1,') }
        shouldFail(JsonException) { parser.parseText('[1, 2') }
        shouldFail(JsonException) { parser.parseText('[1, 2, [3, 4]') }
    }

    @Test
    void testObjectWithSimpleValues() {
        assert parser.parseText('{"a": 1, "b" : true , "c":false, "d": null}') == [a: 1, b: true, c: false, d: null]

        shouldFail { parser.parseText('{true}') }
        shouldFail { parser.parseText('{"a"}') }
        shouldFail { parser.parseText('{"a":true') }
        shouldFail { parser.parseText('{"a":}') }
        shouldFail { parser.parseText('{"a":"b":"c"}') }
        shouldFail { parser.parseText('{:true}') }
    }

    @Test
    void testComplexObject() {
        assert parser.parseText('''
            {
                "response": {
                    "status": "ok",
                    "code": 200,
                    "chuncked": false,
                    "content-type-supported": ["text/html", "text/plain"],
                    "headers": {
                        "If-Last-Modified": "2010"
                    }
                }
            }
        ''') == [
                response: [
                        status: "ok",
                        code: 200,
                        chuncked: false,
                        "content-type-supported": ["text/html", "text/plain"],
                        headers: [
                                "If-Last-Modified": "2010"
                        ]
                ]
        ]
    }

    @Test
    void testListOfObjects() {
        assert parser.parseText('''
            [
                {
                    "firstname": "Guillaume",
                    "lastname": "Laforge"
                },
                {
                    "firstname": "Paul",
                    "lastname": "King"
                },
                {
                    "firstname": "Jochen",
                    "lastname": "Theodorou"
                }
            ]
        ''') == [
                [
                        firstname: "Guillaume",
                        lastname: "Laforge"
                ],
                [
                        firstname: "Paul",
                        lastname: "King"
                ],
                [
                        firstname: "Jochen",
                        lastname: "Theodorou"
                ]
        ]
    }

    @Test
    void testNullEmptyMalformedPayloads() {
        shouldFail(IllegalArgumentException) { parser.parseText(null)   }
        shouldFail(IllegalArgumentException) { parser.parseText("")     }

        shouldFail(JsonException) { parser.parseText("[")           }
        shouldFail(JsonException) { parser.parseText("[a")          }
        shouldFail(JsonException) { parser.parseText('{"')          }
        shouldFail(JsonException) { parser.parseText('{"key"')      }
        shouldFail(JsonException) { parser.parseText('{"key":')     }
        shouldFail(JsonException) { parser.parseText('{"key":1')    }
        shouldFail(JsonException) { parser.parseText('[')           }
        shouldFail(JsonException) { parser.parseText('[a')          }
        shouldFail(JsonException) { parser.parseText('["a"')        }
        shouldFail(JsonException) { parser.parseText('["a", ')      }
        shouldFail(JsonException) { parser.parseText('["a", true')  }
        shouldFail(JsonException) { parser.parseText('[-]') }
    }

    @Test
    void testBackSlashEscaping() {
        def json = new JsonBuilder()

        json.person {
            name "Guill\\aume"
            age 33
            pets "Hector", "Felix"
        }

        def jsonstring = json.toString()

        def slurper = new JsonSlurper()
        assert slurper.parseText(jsonstring).person.name == "Guill\\aume"

        assert parser.parseText('{"a":"\\\\"}') == [a: '\\']
        assert parser.parseText('{"a":"C:\\\\\\"Documents and Settings\\"\\\\"}') == [a: 'C:\\"Documents and Settings"\\']
        assert parser.parseText('{"a":"c:\\\\GROOVY5144\\\\","y":"z"}') == [a: 'c:\\GROOVY5144\\', y: 'z']

        assert parser.parseText('["c:\\\\GROOVY5144\\\\","d"]') == ['c:\\GROOVY5144\\', 'd']

        shouldFail(JsonException) {
            parser.parseText('{"a":"c:\\\"}')
        }
    }

    @Test
    void testParseWithByteArray() {
        def slurper = new JsonSlurper()

        assert slurper.parse('{"a":true}'.bytes) == [a: true]

    }

    @Test
    void testJsonDate() {
        def o = new JsonSlurper().
            setType(JsonParserType.INDEX_OVERLAY).
            setCheckDates(true).
            parseText(JsonOutput.toJson([a : new Date()]))

        assertEquals(Date.class, o.a.class)
    }

    @Test
    void testInvalidNumbers() {
        shouldFail(JsonException) { parser.parseText('[1.1.1]') }
        shouldFail(JsonException) { parser.parseText('{"num": 1a}') }
        shouldFail(JsonException) { parser.parseText('{"num": 1A}') }
        shouldFail(JsonException) { parser.parseText('{"num": -1a}') }
        shouldFail(JsonException) { parser.parseText('[98ab9]') }
        shouldFail(JsonException) { parser.parseText('[-98ab9]') }
        shouldFail(JsonException) { parser.parseText('[12/25/1980]') }

        // TODO: Exception class differs from this point by parser type
        // Probably something to be addressed at some point.
        Class exceptional = JsonException
        if (parser.type == JsonParserType.CHAR_BUFFER) {
            exceptional = NumberFormatException
        }

        shouldFail(exceptional) { parser.parseText('{"num": 1980-12-25}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2ee5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2EE5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2Ee5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2e++5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2e--5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2e+-5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2+e5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2E5+}') }
        shouldFail(exceptional) { parser.parseText('{"num": 1.2e5+}') }
        shouldFail(exceptional) { parser.parseText('{"num": 37e-5.5}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6.92e}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6.92E}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6.92e-}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6.92e+}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6+}') }
        shouldFail(exceptional) { parser.parseText('{"num": 6-}') }
    }


    @Test
    void testParsePath() {
        def file = File.createTempFile('test','json')
        file.deleteOnExit()
        file.text = '''
            {
                "response": {
                    "status": "ok",
                    "code": 200,
                    "chuncked": false,
                    "content-type-supported": ["text/html", "text/plain"],
                    "headers": {
                        "If-Last-Modified": "2010"
                    }
                }
            }
        '''

        and:
        def result = new JsonSlurper().parse(file.toPath())
        assert result == [
                response: [
                        status: "ok",
                        code: 200,
                        chuncked: false,
                        "content-type-supported": ["text/html", "text/plain"],
                        headers: [
                                "If-Last-Modified": "2010"
                        ]
                ] ]

    }

    @Test
    void testParseStringEndedWithRightCurlyBrace() {
        def jsonSlurper = new JsonSlurper()
        def inValid = """
                            {
                            "a":1,
                            "b": {
                                "c":2
                            }"""
        shouldFail (JsonException) { jsonSlurper.parseText(inValid) }

        def valid = """
                            {
                            "a":1,
                            "b": {
                                "c":2
                            }}"""
        assert jsonSlurper.parseText(valid) == [a: 1, b:[c:2]]
    }

    @Test
    void testParseTextSimpleObject() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"name":"John","age":30}')

        assertNotNull(result)
        assertTrue(result instanceof Map)
        def map = (Map) result
        assertEquals("John", map.get("name"))
        assertEquals(30, map.get("age"))
    }

    @Test
    void testParseTextSimpleArray() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText("[1, 2, 3, 4, 5]")

        assertNotNull(result)
        assertTrue(result instanceof List)
        def list = (List) result
        assertEquals(5, list.size())
        assertEquals(1, list.get(0))
        assertEquals(5, list.get(4))
    }

    @Test
    void testParseTextNestedObject() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"person":{"name":"Jane","address":{"city":"NYC"}}}')

        assertNotNull(result)
        def map = (Map) result
        def person = (Map) map.get("person")
        assertEquals("Jane", person.get("name"))
        def address = (Map) person.get("address")
        assertEquals("NYC", address.get("city"))
    }

    @Test
    void testParseTextWithNull() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"value":null}')

        def map = (Map) result
        assertNull(map.get("value"))
    }

    @Test
    void testParseTextWithBoolean() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"active":true,"deleted":false}')

        def map = (Map) result
        assertEquals(true, map.get("active"))
        assertEquals(false, map.get("deleted"))
    }

    @Test
    void testParseTextWithFloat() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"price":19.99}')

        def map = (Map) result
        def price = map.get("price")
        assertTrue(price instanceof Number)
        assertEquals(19.99, ((Number) price).doubleValue(), 0.001)
    }

    @Test
    void testParseTextEmptyObject() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText("{}")

        assertTrue(result instanceof Map)
        def map = (Map) result
        assertTrue(map.isEmpty())
    }

    @Test
    void testParseTextEmptyArray() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText("[]")

        assertTrue(result instanceof List)
        def list = (List) result
        assertTrue(list.isEmpty())
    }

    @Test
    void testParseTextNullThrowsException() {
        def slurper = new JsonSlurper()
        shouldFail(IllegalArgumentException) { slurper.parseText(null) }
    }

    @Test
    void testParseTextEmptyStringThrowsException() {
        def slurper = new JsonSlurper()
        shouldFail(IllegalArgumentException) { slurper.parseText("") }
    }

    @Test
    void testParseReader() {
        def slurper = new JsonSlurper()
        def reader = new StringReader('{"test":123}')
        def result = slurper.parse(reader)

        assertNotNull(result)
        def map = (Map) result
        assertEquals(123, map.get("test"))
    }

    @Test
    void testParseReaderNullThrowsException() {
        def slurper = new JsonSlurper()
        shouldFail(IllegalArgumentException) { slurper.parse((Reader) null) }
    }

    @Test
    void testParseInputStream() {
        def slurper = new JsonSlurper()
        def is = new ByteArrayInputStream('{"key":"value"}'.getBytes(StandardCharsets.UTF_8))
        def result = slurper.parse(is)

        assertNotNull(result)
        def map = (Map) result
        assertEquals("value", map.get("key"))
    }

    @Test
    void testParseInputStreamWithCharset() {
        def slurper = new JsonSlurper()
        def is = new ByteArrayInputStream('{"key":"value"}'.getBytes(StandardCharsets.UTF_8))
        def result = slurper.parse(is, "UTF-8")

        assertNotNull(result)
        def map = (Map) result
        assertEquals("value", map.get("key"))
    }

    @Test
    void testParseFile() throws IOException {
        def slurper = new JsonSlurper()
        def jsonFile = File.createTempFile('test', '.json')
        jsonFile.deleteOnExit()
        jsonFile.text = '{"file":"test"}'

        def result = slurper.parse(jsonFile)

        assertNotNull(result)
        def map = (Map) result
        assertEquals("test", map.get("file"))
    }

    @Test
    void testParseFileWithCharset() throws IOException {
        def slurper = new JsonSlurper()
        def jsonFile = File.createTempFile('test2', '.json')
        jsonFile.deleteOnExit()
        jsonFile.text = '{"file":"test2"}'

        def result = slurper.parse(jsonFile, "UTF-8")

        assertNotNull(result)
        def map = (Map) result
        assertEquals("test2", map.get("file"))
    }

    @Test
    void testGetAndSetType() {
        def slurper = new JsonSlurper()

        assertEquals(JsonParserType.CHAR_BUFFER, slurper.getType())

        slurper.setType(JsonParserType.INDEX_OVERLAY)
        assertEquals(JsonParserType.INDEX_OVERLAY, slurper.getType())

        slurper.setType(JsonParserType.LAX)
        assertEquals(JsonParserType.LAX, slurper.getType())
    }

    @Test
    void testGetAndSetChop() {
        def slurper = new JsonSlurper()

        assertFalse(slurper.isChop())

        slurper.setChop(true)
        assertTrue(slurper.isChop())

        slurper.setChop(false)
        assertFalse(slurper.isChop())
    }

    @Test
    void testGetAndSetLazyChop() {
        def slurper = new JsonSlurper()

        assertTrue(slurper.isLazyChop())

        slurper.setLazyChop(false)
        assertFalse(slurper.isLazyChop())

        slurper.setLazyChop(true)
        assertTrue(slurper.isLazyChop())
    }

    @Test
    void testGetAndSetCheckDates() {
        def slurper = new JsonSlurper()

        assertTrue(slurper.isCheckDates())

        slurper.setCheckDates(false)
        assertFalse(slurper.isCheckDates())

        slurper.setCheckDates(true)
        assertTrue(slurper.isCheckDates())
    }

    @Test
    void testGetAndSetMaxSizeForInMemory() {
        def slurper = new JsonSlurper()

        assertEquals(2000000, slurper.getMaxSizeForInMemory())

        slurper.setMaxSizeForInMemory(1000000)
        assertEquals(1000000, slurper.getMaxSizeForInMemory())
    }

    @Test
    void testFluentAPI() {
        def slurper = new JsonSlurper()
            .setType(JsonParserType.INDEX_OVERLAY)
            .setChop(true)
            .setLazyChop(false)
            .setCheckDates(false)
            .setMaxSizeForInMemory(500000)

        assertEquals(JsonParserType.INDEX_OVERLAY, slurper.getType())
        assertTrue(slurper.isChop())
        assertFalse(slurper.isLazyChop())
        assertFalse(slurper.isCheckDates())
        assertEquals(500000, slurper.getMaxSizeForInMemory())
    }

    @Test
    void testParseWithIndexOverlayType() {
        def slurper = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY)
        def result = slurper.parseText('{"type":"overlay"}')

        def map = (Map) result
        assertEquals("overlay", map.get("type"))
    }

    @Test
    void testParseWithLaxType() {
        def slurper = new JsonSlurper().setType(JsonParserType.LAX)
        def result = slurper.parseText('{"type":"lax"}')

        def map = (Map) result
        assertEquals("lax", map.get("type"))
    }

    @Test
    void testParseWithCharacterSourceType() {
        def slurper = new JsonSlurper().setType(JsonParserType.CHARACTER_SOURCE)
        def result = slurper.parseText('{"type":"source"}')

        def map = (Map) result
        assertEquals("source", map.get("type"))
    }

    @Test
    void testParseStringWithEscapedCharacters() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"text":"line1\\nline2\\ttab"}')

        def map = (Map) result
        def text = (String) map.get("text")
        assertTrue(text.contains("\n"))
        assertTrue(text.contains("\t"))
    }

    @Test
    void testParseStringWithUnicode() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"text":"Hello \\u0041"}')

        def map = (Map) result
        assertEquals("Hello A", map.get("text"))
    }

    @Test
    void testParseLargeNumber() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"big":9999999999999999999}')

        def map = (Map) result
        assertNotNull(map.get("big"))
    }

    @Test
    void testParseNegativeNumber() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"negative":-42}')

        def map = (Map) result
        assertEquals(-42, map.get("negative"))
    }

    @Test
    void testParseScientificNotation() {
        def slurper = new JsonSlurper()
        def result = slurper.parseText('{"sci":1.5e10}')

        def map = (Map) result
        def sci = map.get("sci")
        assertTrue(sci instanceof Number)
        assertEquals(1.5e10, ((Number) sci).doubleValue(), 1e5)
    }

    @Test
    void testParseComplexStructure() {
        def slurper = new JsonSlurper()
        def json = '{"users":[{"name":"Alice","roles":["admin","user"]},{"name":"Bob","roles":["user"]}]}'
        def result = slurper.parseText(json)

        def map = (Map) result
        def users = (List) map.get("users")
        assertEquals(2, users.size())

        def alice = (Map) users.get(0)
        assertEquals("Alice", alice.get("name"))
        def aliceRoles = (List) alice.get("roles")
        assertEquals(2, aliceRoles.size())
    }

    @Test
    void testJsonParserTypeValues() {
        def types = JsonParserType.values()
        assertTrue(types.length >= 4)

        assertEquals(JsonParserType.CHAR_BUFFER, JsonParserType.valueOf("CHAR_BUFFER"))
        assertEquals(JsonParserType.INDEX_OVERLAY, JsonParserType.valueOf("INDEX_OVERLAY"))
        assertEquals(JsonParserType.LAX, JsonParserType.valueOf("LAX"))
        assertEquals(JsonParserType.CHARACTER_SOURCE, JsonParserType.valueOf("CHARACTER_SOURCE"))
    }

}
