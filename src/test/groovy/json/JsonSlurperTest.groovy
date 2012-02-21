/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.json

/**
 * @author Guillaume Laforge
 */
class JsonSlurperTest extends GroovyTestCase {

    def parser = new JsonSlurper()

    void testJsonShouldStartWithCurlyOrBracket() {
        def msg = shouldFail(JsonException) {
            parser.parseText("true")
        }

        assert msg.contains('A JSON payload should start with')
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
}
