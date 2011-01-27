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
}
