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
package json

import groovy.json.JsonOutput
import groovy.json.JsonParserType
import groovy.json.JsonSlurper

class JsonTest extends GroovyTestCase {

    void testParseText() {
        // tag::parse_text[]
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "name": "John Doe" } /* some comment */')

        assert object instanceof Map
        assert object.name == 'John Doe'
        // end::parse_text[]
    }

    void testParseTextWithNumber() {
        // tag::parse_number[]
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText '''
            { "simple": 123,
              "fraction": 123.66,
              "exponential": 123e12
            }'''

        assert object instanceof Map
        assert object.simple.class == Integer
        assert object.fraction.class == BigDecimal
        assert object.exponential.class == BigDecimal
        // end::parse_number[]
    }

    void testParseTextWithList() {
        // tag::parse_list[]
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "myList": [4, 8, 15, 16, 23, 42] }')

        assert object instanceof Map
        assert object.myList instanceof List
        assert object.myList == [4, 8, 15, 16, 23, 42]
        // end::parse_list[]
    }

    void testSetType() {
        // tag::set_type[]
        def jsonSlurper = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)
        def object = jsonSlurper.parseText('{ "myList": [4, 8, 15, 16, 23, 42] }')

        assert object instanceof Map
        assert object.myList instanceof List
        assert object.myList == [4, 8, 15, 16, 23, 42]
        // end::set_type[]
    }

    void testJsonOutput() {
        // tag::json_output[]
        def json = JsonOutput.toJson([name: 'John Doe', age: 42])

        assert json == '{"name":"John Doe","age":42}'
        // end::json_output[]
    }

    void testJsonOutputPogo() {
        assertScript '''
        import groovy.json.*

        // tag::json_output_pogo[]
        class Person { String name }

        def json = JsonOutput.toJson([ new Person(name: 'John'), new Person(name: 'Max') ])

        assert json == '[{"name":"John"},{"name":"Max"}]'
        // end::json_output_pogo[]
        '''
    }

    void testPrettyPrint() {
        // tag::pretty_print[]
        def json = JsonOutput.toJson([name: 'John Doe', age: 42])

        assert json == '{"name":"John Doe","age":42}'

        assert JsonOutput.prettyPrint(json) == '''\
        {
            "name": "John Doe",
            "age": 42
        }'''.stripIndent()
        // end::pretty_print[]
    }

}
