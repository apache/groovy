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

import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import org.junit.Test

import static groovy.json.JsonOutput.toJson
import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail

final class JsonOutputTest {

    @Test // GROOVY-5918
    void testExpando() {
        assert toJson(new Expando(a: 42)) == '{"a":42}'
        assert new JsonBuilder(new Expando(a: 42)).toString() == '{"a":42}'
    }

    @Test
    void testBooleanValues() {
        assert toJson(Boolean.TRUE) == "true"
        assert toJson(Boolean.FALSE) == "false"
        assert toJson(true) == "true"
        assert toJson(false) == "false"
    }

    @Test
    void testNullValue() {
        assert toJson(null) == "null"
        // test every overloaded version
        assert toJson((Object) null) == "null"
        assert toJson((Boolean) null) == 'null'
        assert toJson((Number) null) == 'null'
        assert toJson((Character) null) == 'null'
        assert toJson((String) null) == 'null'
        assert toJson((Date) null) == 'null'
        assert toJson((Calendar) null) == 'null'
        assert toJson((UUID) null) == 'null'
        assert toJson((URL) null) == 'null'
        assert toJson((Closure) null) == 'null'
        assert toJson((Expando) null) == 'null'
        assert toJson((Map) null) == 'null'
    }

    @Test
    void testNumbers() {
        assert toJson(-1) == "-1"
        assert toJson(1) == "1"
        assert toJson(0) == "0"
        assert toJson(100) == "100"
        assert toJson(100) == "100"

        assert toJson((short) 100) == "100"
        assert toJson((byte) 100) == "100"

        // Long
        assert toJson(1000000000000000000) == "1000000000000000000"

        // BigInteger
        assert toJson(1000000000000000000000000) == "1000000000000000000000000"

        // BigDecimal
        assert toJson(0.0) == "0.0"

        // Double
        assert toJson(Math.PI) == "3.141592653589793"

        // Float
        assert toJson(1.2345f) == "1.2345"

        // exponent
        assert toJson(1234.1234e12) == "1.2341234E+15"

        shouldFail { toJson(Double.NaN) }
        shouldFail { toJson(Double.POSITIVE_INFINITY) }
        shouldFail { toJson(Double.NEGATIVE_INFINITY) }
        shouldFail { toJson(Float.NaN) }
        shouldFail { toJson(Float.POSITIVE_INFINITY) }
        shouldFail { toJson(Float.NEGATIVE_INFINITY) }
    }

    @Test
    void testEmptyListOrArray() {
        assert toJson([]) == "[]"
        assert toJson([] as Object[]) == "[]"
    }

    @Test
    void testListOfPrimitives() {
        assert toJson([true, false, null, true, 4, 1.1234]) == "[true,false,null,true,4,1.1234]"
        assert toJson([true, [false, null], true, [4, [1.1234]]]) == "[true,[false,null],true,[4,[1.1234]]]"
    }

    @Test
    void testPrimitiveArray() {
        assert toJson([1, 2, 3, 4] as byte[]) == "[1,2,3,4]"
        assert toJson([1, 2, 3, 4] as short[]) == "[1,2,3,4]"
        assert toJson([1, 2, 3, 4] as int[]) == "[1,2,3,4]"
        assert toJson([1, 2, 3, 4] as long[]) == "[1,2,3,4]"
    }

    @Test
    void testEmptyMap() {
        assert toJson([:]) == "{}"
    }

    @Test
    void testMap() {
        assert toJson([a: 1]) == '{"a":1}'
        assert toJson([a: 1, b: 2]) == '{"a":1,"b":2}'
        assert toJson([a: 1, b: true, c: null, d: [], e: 'hello']) == '{"a":1,"b":true,"c":null,"d":[],"e":"hello"}'
    }

    @Test
    void testString() {
        assert toJson("") == '""'

        assert toJson("a") == '"a"'
        assert toJson("abcdef") == '"abcdef"'

        assert toJson("\b") == '"\\b"'
        assert toJson("\f") == '"\\f"'
        assert toJson("\n") == '"\\n"'
        assert toJson("\r") == '"\\r"'
        assert toJson("\t") == '"\\t"'

        assert toJson('"') == '"\\""'
//        assert toJson("/") == '"\\/"'
        assert toJson("\\") == '"\\\\"'

        assert toJson("\u0001") == '"\\u0001"'
        assert toJson("\u0002") == '"\\u0002"'
        assert toJson("\u0003") == '"\\u0003"'
        assert toJson("\u0004") == '"\\u0004"'
        assert toJson("\u0005") == '"\\u0005"'
        assert toJson("\u0006") == '"\\u0006"'
        assert toJson("\u0007") == '"\\u0007"'
        assert toJson("\u0010") == '"\\u0010"'
        assert toJson("\u0011") == '"\\u0011"'
        assert toJson("\u0012") == '"\\u0012"'
        assert toJson("\u0013") == '"\\u0013"'
        assert toJson("\u0014") == '"\\u0014"'
        assert toJson("\u0015") == '"\\u0015"'
        assert toJson("\u0016") == '"\\u0016"'
        assert toJson("\u0017") == '"\\u0017"'
        assert toJson("\u0018") == '"\\u0018"'
        assert toJson("\u0019") == '"\\u0019"'
    }

    @Test
    void testGString() {
        assert toJson("1 + 2 = ${1 + 2}") == '"1 + 2 = 3"'
    }

    @Test
    void testStringBuilderBuffer() {
        assert toJson(new StringBuilder().append(14).append(' March ').append(2014)) == '"14 March 2014"'
        assert toJson(new StringBuffer().append(14).append(' March ').append(2014)) == '"14 March 2014"'
    }

    @Test
    void testCharArray() {
        char[] charArray = ['a', 'b', 'c']

        assert toJson(charArray) == '["a","b","c"]'
    }

    @Test
    void testDate() {
        def d = Date.parse("yyyy/MM/dd HH:mm:ss Z", "2008/03/04 13:50:00 +0100")

        assert toJson(d) == '"2008-03-04T12:50:00+0000"'
    }

    @Test
    void testURL() {
        assert toJson(new URL("http://glaforge.appspot.com")) == '"http://glaforge.appspot.com"'
        assert toJson(new URL('file', '', 'C:\\this\\is\\windows\\path')) == '"file:C:\\\\this\\\\is\\\\windows\\\\path"' // GROOVY-6560
    }

    @Test
    void testCalendar() {
        def c = GregorianCalendar.getInstance(TimeZone.getTimeZone('GMT+1'))
        c.clearTime()
        c.set(year: 2008, month: Calendar.MARCH, date: 4, hourOfDay: 13, minute: 50)

        assert toJson(c) == '"2008-03-04T12:50:00+0000"'
    }

    @Test
    void testComplexObject() {
        assert toJson([name: 'Guillaume', age: 33, address: [line1: "1 main street", line2: "", zip: 1234], pets: ['dog', 'cat']]) ==
                '{"name":"Guillaume","age":33,"address":{"line1":"1 main street","line2":"","zip":1234},"pets":["dog","cat"]}'

        assert toJson([[:], [:]]) == '[{},{}]'
    }

    @Test
    void testClosure() {
        assert toJson({
            a 1
            b {
                c 2
                d {
                    e 3, {
                        f 4
                    }
                }
            }
        }) == '{"a":1,"b":{"c":2,"d":{"e":[3,{"f":4}]}}}'
    }

    @Test
    void testIteratorEnumeration() {
        assert toJson([1, 2, 3].iterator()) == '[1,2,3]'
        assert toJson(Collections.enumeration([1, 2, 3])) == '[1,2,3]'
    }

    @Test
    void testPrettyPrint() {
        def json = new JsonBuilder()

        json.trends {
            "2010-06-22 17:20" ([
                    name: "Groovy rules",
                    query: "Groovy rules"
            ], {
                name "#worldcup"
                query "#worldcup"
            }, [
                    name: "Uruguai",
                    query: "Uruguai"
            ])
            "2010-06-22 06:20" ({
                name "#groovy"
                query "#groovy"
            }, [
                    name: "#java",
                    query: "#java"
            ])
        }

        assert json.toPrettyString() == """\
            {
                "trends": {
                    "2010-06-22 17:20": [
                        {
                            "name": "Groovy rules",
                            "query": "Groovy rules"
                        },
                        {
                            "name": "#worldcup",
                            "query": "#worldcup"
                        },
                        {
                            "name": "Uruguai",
                            "query": "Uruguai"
                        }
                    ],
                    "2010-06-22 06:20": [
                        {
                            "name": "#groovy",
                            "query": "#groovy"
                        },
                        {
                            "name": "#java",
                            "query": "#java"
                        }
                    ]
                }
            }""".stripIndent()
    }

    private stripWhiteSpace(String str) {
        return str.replaceAll(~/\s/, '')
    }

    @Test
    void testPrettyPrintStringZeroLen() {
        def tree = [myStrings: [str3: 'abc', str0: '']]
        def result = stripWhiteSpace(new JsonBuilder(tree).toPrettyString())
        def expected = stripWhiteSpace('{ "myStrings":{ "str3":"abc","str0":"" } }')
        assert result == expected
    }

    @Test
    void testPrettyPrintDoubleQuoteEscape() {
        def json = new JsonBuilder()
        json.text { content 'abc"def' }
        assert json.toPrettyString() == """\
            {
                "text": {
                    "content": "abc\\"def"
                }
            }""".stripIndent()
    }

    @Test
    void testPrettyPrintUnicodeEscapeVariants() {
        def map = [name: "Järry"]
        def json = JsonOutput.toJson(map)
        // with escaping
        assert JsonOutput.prettyPrint(json) == '''\
            {
                "name": "J\\u00e4rry"
            }'''.stripIndent()
        // with escaping disabled
        assert JsonOutput.prettyPrint(json, true) == '''\
            {
                "name": "Järry"
            }'''.stripIndent()
    }

    @Test
    void testSerializePogos() {
        def city = new JsonCity("Paris", [
                new JsonDistrict(1, [
                        new JsonStreet("Saint-Honore", JsonStreetKind.street),
                        new JsonStreet("de l'Opera", JsonStreetKind.avenue)
                ] as JsonStreet[]),
                new JsonDistrict(2, [
                        new JsonStreet("des Italiens", JsonStreetKind.boulevard),
                        new JsonStreet("Bonne Nouvelle", JsonStreetKind.boulevard)
                ] as JsonStreet[])
        ])

        assert JsonOutput.prettyPrint(toJson(city)) == '''\
            {
                "name": "Paris",
                "districts": [
                    {
                        "number": 1,
                        "streets": [
                            {
                                "streetName": "Saint-Honore",
                                "kind": "street"
                            },
                            {
                                "streetName": "de l'Opera",
                                "kind": "avenue"
                            }
                        ]
                    },
                    {
                        "number": 2,
                        "streets": [
                            {
                                "streetName": "des Italiens",
                                "kind": "boulevard"
                            },
                            {
                                "streetName": "Bonne Nouvelle",
                                "kind": "boulevard"
                            }
                        ]
                    }
                ]
            }'''.stripIndent()
    }

    @Test
    void testMapWithNullKey() {
        shouldFail IllegalArgumentException, {
            toJson([(null): 1])
        }
    }

    @Test // GROOVY-5247
    void testTreeMap() {
        def m = new TreeMap()
        m.a = 1
        assert toJson(m) == '{"a":1}'
    }

    @Test
    void testDeclaredPropertiesField() {
        String json = toJson(new JsonObject(name: "pillow", properties: [state: "fluffy", color: "white"]))
        assert json == '{"name":"pillow","properties":{"state":"fluffy","color":"white"}}'
    }

    @Test // GROOVY-5494
    void testDeclaredPropertiesMethod() {
        String json = toJson(new Pogo5494(name: "foo"))
        assert json == '{"name":"foo","properties":0}'
    }

    static class Pogo5494 {
        String name
        int getProperties() { return 0 }
    }

    @Test
    void testCharacter() {
        assert toJson('a' as char) == '"a"'
        assert toJson('"' as char) == '"\\""'
        assert toJson('\b' as char) == '"\\b"'
        assert toJson('\f' as char) == '"\\f"'
        assert toJson('\n' as char) == '"\\n"'
        assert toJson('\r' as char) == '"\\r"'
        assert toJson('\t' as char) == '"\\t"'
        assert toJson('\\' as char) == '"\\\\"'
        assert toJson(1 as char) == '"\\u0001"'
        assert toJson('\u0002' as char) == '"\\u0002"'
    }

    @Test
    void testEmptyValue() {
        assert toJson('') == '""'
        assert toJson(['']) == '[""]'
        assert toJson(['': '']) == '{"":""}'
        assert toJson(new Expando('': '')) == '{"":""}'
    }

    @Test
    void testSpecialCharEscape() {
        // Map
        assert toJson(['"': 0]) == '{"\\"":0}'
        assert toJson(['\b': 0]) == '{"\\b":0}'
        assert toJson(['\f': 0]) == '{"\\f":0}'
        assert toJson(['\n': 0]) == '{"\\n":0}'
        assert toJson(['\r': 0]) == '{"\\r":0}'
        assert toJson(['\t': 0]) == '{"\\t":0}'
        assert toJson(['\\': 0]) == '{"\\\\":0}'
        assert toJson([(1 as char): 0]) == '{"\\u0001":0}'
        assert toJson(['\u0002': 0]) == '{"\\u0002":0}'

        // Expando
        assert toJson(new Expando('"': 0)) == '{"\\"":0}'
        assert toJson(new Expando('\b': 0)) == '{"\\b":0}'
        assert toJson(new Expando('\f': 0)) == '{"\\f":0}'
        assert toJson(new Expando('\n': 0)) == '{"\\n":0}'
        assert toJson(new Expando('\r': 0)) == '{"\\r":0}'
        assert toJson(new Expando('\t': 0)) == '{"\\t":0}'
        assert toJson(new Expando('\\': 0)) == '{"\\\\":0}'
        assert toJson(new Expando((1 as char): 0)) == '{"\\u0001":0}'
        assert toJson(new Expando('\u0002': 0)) == '{"\\u0002":0}'

        // Closure
        assert toJson({'"' 0}) == '{"\\"":0}'
        assert toJson({'\b' 0}) == '{"\\b":0}'
        assert toJson({'\f' 0}) == '{"\\f":0}'
        assert toJson({'\n' 0}) == '{"\\n":0}'
        assert toJson({'\r' 0}) == '{"\\r":0}'
        assert toJson({'\t' 0}) == '{"\\t":0}'
        assert toJson({'\\' 0}) == '{"\\\\":0}'
        assert toJson({'\1' 0}) == '{"\\u0001":0}'
        assert toJson({'\u0002' 0}) == '{"\\u0002":0}'
    }

    @Test // GROOVY-7519
    void testFile() {
        def file  = File.createTempFile('test', 'file-json')
        file.deleteOnExit()
        assert toJson(file)

        def dir = File.createTempDir()
        dir.deleteOnExit()
        assert toJson(dir)
    }

    @Test // GROOVY-5169
    void testPropertyModifiers1() {
        assertScript '''
            class Pogo {
                public    String foo = 'foo'
                public    String getBar() { 'bar' }
                protected String getBaz() { 'baz' }
                private   String getBoo() { 'boo' }
            }

            String json = groovy.json.JsonOutput.toJson(new Pogo())
            assert json == '{"foo":"foo","bar":"bar"}'
        '''
    }

    @Test // GROOVY-7682
    void testStackOverflowError1() {
        assertScript '''
            @Grab('joda-time:joda-time:2.10.10')
            import org.joda.time.DateTime
            import org.joda.time.format.DateTimeFormat
            import org.joda.time.format.DateTimeFormatter

            DateTimeFormatter formatter = DateTimeFormat.forPattern('yyyy-MM-dd HH:mm:ss.SSS z')
            DateTime dt = formatter.parseDateTime('2015-11-20 13:37:21.123 EST')
            String json = groovy.json.JsonOutput.toJson(dt)
            net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals("""{
                "afterNow":false,
                "beforeNow":true,
                "centuryOfEra":20,
                "dayOfMonth":20,
                "dayOfWeek":5,
                "dayOfYear":324,
                "equalNow":false,
                "era":1,
                "hourOfDay":13,
                "millisOfDay":49041123,
                "millisOfSecond":123,
                "minuteOfDay":817,
                "minuteOfHour":37,
                "monthOfYear":11,
                "secondOfDay":49041,
                "secondOfMinute":21,
                "weekOfWeekyear":47,
                "weekyear":2015,
                "year":2015,
                "yearOfCentury":15,
                "yearOfEra":2015,
                "zone":{
                    "ID":"America/New_York",
                    "fixed":false,
                    "uncachedZone":{
                        "ID":"America/New_York",
                        "cachable":true,
                        "fixed":false
                    }
                }
            }""", json)
        '''
    }

    @Test // GROOVY-8371
    void testStackOverflowError2() {
        assertScript '''
            final  date = java.time.LocalDate.of(1970, 1, 1)
            String json = groovy.json.JsonOutput.toJson(date)
            net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals("""{
                "chronology":{
                    "calendarType":"iso8601",
                    "id":"ISO"
                    ''' + ((isAtLeastJdk("19.0")) ? ',"isoBased":true' : '' ) + '''
                },
                "dayOfMonth":1,
                "dayOfWeek":"THURSDAY",
                "dayOfYear":1,
                "era":"CE",
                "leapYear":false,
                "month":"JANUARY",
                "monthValue":1,
                "year":1970
            }""", json)
        '''
    }

    @Test // GROOVY-11167
    void testRecordsVsClass() {
        def a = new PersonA('Guillaume')
        def b = new PersonB('Jochen')
        def c = new PersonC('Eric')
        assert toJson([a,b,c]) == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Eric"}]'
    }
}

//------------------------------------------------------------------------------

@Canonical
class JsonCity {
    String name
    List<JsonDistrict> districts
}

@Canonical
class JsonDistrict {
    int number
    JsonStreet[] streets
}

@Canonical
class JsonStreet {
    String streetName
    JsonStreetKind kind
}

enum JsonStreetKind {
    street, boulevard, avenue
}

import groovy.transform.RecordOptions
import static groovy.transform.RecordTypeMode.*

@RecordOptions(mode=EMULATE)
record PersonA(String name) {}

// different JDK versions will cover the NATIVE case
record PersonB(String name) {}

@TupleConstructor
class PersonC {
    String name
}
