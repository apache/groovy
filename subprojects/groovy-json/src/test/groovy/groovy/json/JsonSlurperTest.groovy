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

import groovy.test.GroovyTestCase
import org.apache.groovy.json.internal.Value

class JsonSlurperTest extends GroovyTestCase {

    def parser

    void setUp() {
        parser = new JsonSlurper()
    }

    void testJsonShouldStartWithCurlyOrBracket() {
        /* We can handle parsing boolean, numbers, and such. */
        parser.parseText("true")
    }

    void testEmptyStructures() {
        assert parser.parseText("[]") == []
        assert parser.parseText("{}") == [:]
    }

    void testArrayWithSimpleValues() {
        assert parser.parseText('[123, "abc", true, false, null]') == [123, "abc", true, false, null]

        shouldFail(JsonException) {
            parser.parseText('[123 "abc"]')
        }

        shouldFail(JsonException) {
            parser.parseText('[123, "abc"')
        }
    }

    void testAddingToEmptyList() {
        def list = parser.parseText('[]')
        list << "Hello"
        list << "World"
        assert list == ["Hello", "World"]
    }

    void testParseNum() {
        int i = parser.parseText('123')
        int i2 = 123
        assert i == i2
    }

    void testNegNum() {
        int i = parser.parseText('-123')
        int i2 = -123
        assert i == i2
    }

    void testNegNumWithSpace() {
        int i = parser.parseText('   -123')
        int i2 = -123
        assert i == i2
    }

    void testLargeNegNumWithSpace() {
        int i = parser.parseText('   -1234567891')
        int i2 = -1234567891
        assert i == i2
    }

    void testWithSpaces() {
        int num = ((Number) parser.parseText("           123")).intValue()
        int num2 = 123
        boolean ok = num == num2 || die("" + num)
    }

    void testParseLargeNum() {
        long num = parser.parseText("" + Long.MAX_VALUE)
        long num2 = Long.MAX_VALUE
        assert num == num2
    }

    void testParseSmallNum() {
        long num = parser.parseText("" + Long.MIN_VALUE)
        long num2 = Long.MIN_VALUE
        assert num == num2
    }

    void testParseLargeDecimal() {
        double num = parser.parseText("" + Double.MAX_VALUE)
        double num2 = Double.MAX_VALUE
        assert num == num2
    }

    void testParseSmallDecimal() {
        double num = parser.parseText("" + Double.MIN_VALUE)
        double num2 = Double.MIN_VALUE
        assert num == num2
    }

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

    void testNull() {
        if (parser.type in [JsonParserType.CHAR_BUFFER, JsonParserType.CHARACTER_SOURCE]) {
            assert parser.parseText("null") == null
        } else {
            assert parser.parseText("null").toValue() == null
        }
    }

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

    void testObjectWithSimpleValues() {
        assert parser.parseText('{"a": 1, "b" : true , "c":false, "d": null}') == [a: 1, b: true, c: false, d: null]

        shouldFail { parser.parseText('{true}') }
        shouldFail { parser.parseText('{"a"}') }
        shouldFail { parser.parseText('{"a":true') }
        shouldFail { parser.parseText('{"a":}') }
        shouldFail { parser.parseText('{"a":"b":"c"}') }
        shouldFail { parser.parseText('{:true}') }
    }

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

    void testParseWithByteArray() {
        def slurper = new JsonSlurper()

        assert slurper.parse('{"a":true}'.bytes) == [a: true]

    }

    void testJsonDate() {
        def o = new JsonSlurper().
            setType(JsonParserType.INDEX_OVERLAY).
            setCheckDates(true).
            parseText(JsonOutput.toJson([a : new Date()]))

        assertEquals(Date.class, o.a.class)
    }

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

}
