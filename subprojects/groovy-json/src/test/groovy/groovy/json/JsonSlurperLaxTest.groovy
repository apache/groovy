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

class JsonSlurperLaxTest extends JsonSlurperTest {

    void setUp() {
        parser = new JsonSlurper().setType(JsonParserType.LAX)
    }

    void testNullEmptyMalformedPayloads() {
        shouldFail(IllegalArgumentException) { parser.parseText(null) }
        shouldFail(IllegalArgumentException) { parser.parseText("") }

        shouldFail(JsonException) { parser.parseText("[") }
        shouldFail(JsonException) { parser.parseText('{"') }
        shouldFail(JsonException) { parser.parseText("[a") }
        shouldFail(JsonException) { parser.parseText('{a"') }
        shouldFail(JsonException) { parser.parseText("[\"a\"") }
        shouldFail(JsonException) { parser.parseText('{"a"') }
        shouldFail(JsonException) { parser.parseText('[-]') }
    }

    void testObjectWithSimpleValues() {
        assert parser.parseText('{"a": 1, "b" : true , "c":false, "d": null}') == [a: 1, b: true, c: false, d: null]

        parser.parseText('{true}')
        shouldFail { parser.parseText('{"a"}') }
        parser.parseText('{"a":true')
        parser.parseText('{"a":}')
        shouldFail {parser.parseText('{"a":"b":"c"}') }
        parser.parseText('{:true}')
    }

    void testArrayOfArrayWithSimpleValues() {
        assert parser.parseText('[1, 2, 3, ["a", "b", "c", [true, false], "d"], 4]') ==
                [1, 2, 3, ["a", "b", "c", [true, false], "d"], 4]

        shouldFail(JsonException) { parser.parseText('[') }
        parser.parseText('[,]')
        shouldFail(JsonException) { parser.parseText('[1') }
        parser.parseText('[1,')
        shouldFail(JsonException) { parser.parseText('[1, 2') }
        parser.parseText('[1, 2, [3, 4]')
    }

    void testBackSlashEscaping() {
        def json = new JsonBuilder()

        json.person {
            name "Guill\\aume"
            age 33
            pets "Hector", "Felix"
        }

        def jsonstring = json.toString()

        assert parser.parseText(jsonstring).person.name == "Guill\\aume"

        assert parser.parseText('{"a":"\\\\"}') == [a: '\\']
        assert parser.parseText('{"a":"C:\\\\\\"Documents and Settings\\"\\\\"}') == [a: 'C:\\"Documents and Settings"\\']
        assert parser.parseText('{"a":"c:\\\\GROOVY5144\\\\","y":"z"}') == [a: 'c:\\GROOVY5144\\', y: 'z']

        assert parser.parseText('["c:\\\\GROOVY5144\\\\","d"]') == ['c:\\GROOVY5144\\', 'd']

        parser.parseText('{"a":"c:\\\"}')
    }

    void testLaxCommentsAndKeys() {
        String jsonString = """
            {
            foo:bar,    //look mom no quotes
            'foo1': 'bar1',  //I can do single quotes if I want to
            /** This is a story of Foo */
            "foo2": "bar2",   //Back to two quotes
            # Do you like my foo?           // We support # for comments too, # puts the axe in lax.
            "whoisfoo":fooisyou,
            can this work: sure why not?,
            flag : true,
            flag2 : false,
            strings : [we, are, string, here, us, roar],
            he said : '"fire all your guns at once baby, and explode into the night"',
            "going deeper" : [
                "nestedArrays", // needs comments
                "anotherThing" // commented
                // only one comment
                ,
                "a last thing" // explain that too
            ],
            the : end
            }
        """

        Map<String, Object> map = parser.parseText(jsonString)
        assert map.foo == "bar"
        assert map.foo1 == "bar1"
        assert map.foo2 == "bar2"
        assert map.whoisfoo == "fooisyou"
        assert map['can this work'] == "sure why not?"
        assert map.flag == true
        assert map.flag2 == false
        assert map.strings == ["we", "are", "string", "here", "us", "roar"]
        assert map["he said"] == '"fire all your guns at once baby, and explode into the night"'
        assert map.the == "end"
    }

    @Override
    void testInvalidNumbers() {
        // should be parsed as Strings
        assert parser.parseText('{"num": 1a}').num == '1a'
        assert parser.parseText('{"num": -1a}').num == '-1a'
        assert parser.parseText('[98ab9]')[0] == '98ab9'
        assert parser.parseText('[12/25/1980]')[0] == '12/25/1980'
        assert parser.parseText('{"num": 1980-12-25}').num == '1980-12-25'
        assert parser.parseText('{"num": 1.2ee5}').num == '1.2ee5'
        assert parser.parseText('{"num": 1.2EE5}').num == '1.2EE5'
        assert parser.parseText('{"num": 1.2Ee5}').num == '1.2Ee5'
        assert parser.parseText('{"num": 1.2e++5}').num == '1.2e++5'
        assert parser.parseText('{"num": 1.2e--5}').num == '1.2e--5'
        assert parser.parseText('{"num": 1.2e+-5}').num == '1.2e+-5'
        assert parser.parseText('{"num": 1.2+e5}').num == '1.2+e5'
        assert parser.parseText('{"num": 1.2E5+}').num == '1.2E5+'
        assert parser.parseText('{"num": 1.2e5+}').num == '1.2e5+'
        assert parser.parseText('{"num": 37e-5.5}').num == '37e-5.5'
        assert parser.parseText('{"num": 6.92e}').num == '6.92e'
        assert parser.parseText('{"num": 6.92E}').num == '6.92E'
        assert parser.parseText('{"num": 6.92e-}').num == '6.92e-'
        assert parser.parseText('{"num": 6.92e+}').num == '6.92e+'
        assert parser.parseText('{"num": 6+}').num == '6+'
        assert parser.parseText('{"num": 6-}').num == '6-'
    }

    void testGroovy7728() {
        String jsonString = '''
            {
                "enterpriseDomain": "@example.com"
                ,"enterpriseId": "123456"
                ,"clientId": "abcdefghijklmnopqrstuvwxyz123456"
                ,"clientSecret": "abcdefghijklmnopqrstuvwxyz123456"
                ,"keyId": "12345678"
                ,"keyFileName": "/etc/PrintToBox/PrintToBox_private_key.pem"
                ,"keyPassword": "12345678901234567890"
                // ,"appUserId": "123456789"

                //  Optional parameters with defaults shown
                // ,"baseFolderName": "PrintToBox"
            }
        '''
        assert !parser.parseText(jsonString).containsKey('appUserId')
    }

}
