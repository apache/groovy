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
import groovy.json.JsonDateHandling
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

class JsonTest {

    @Test
    void testParseText() {
        // tag::parse_text[]
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "name": "John Doe" } /* some comment */')

        assert object instanceof Map
        assert object.name == 'John Doe'
        // end::parse_text[]
    }

    @Test
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

    @Test
    void testParseTextWithList() {
        // tag::parse_list[]
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "myList": [4, 8, 15, 16, 23, 42] }')

        assert object instanceof Map
        assert object.myList instanceof List
        assert object.myList == [4, 8, 15, 16, 23, 42]
        // end::parse_list[]
    }

    @Test
    void testSetType() {
        // tag::set_type[]
        def jsonSlurper = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)
        def object = jsonSlurper.parseText('{ "myList": [4, 8, 15, 16, 23, 42] }')

        assert object instanceof Map
        assert object.myList instanceof List
        assert object.myList == [4, 8, 15, 16, 23, 42]
        // end::set_type[]
    }

    @Test
    void testCheckDates() {
        // tag::check_dates[]
        def json = '{"when":"2026-09-04T10:00:00.000Z"}'

        assert new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)
                .parseText(json).when instanceof Date

        assert new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)
                .setCheckDates(false)
                .parseText(json).when instanceof String

        assert new JsonSlurper(type: JsonParserType.CHAR_BUFFER)
                .parseText(json).when instanceof String        // never converts
        // end::check_dates[]

        // tag::date_handling[]
        def offsetJson = '{"when":"2026-09-04T10:00:00+10:00"}'
        def slurp = { JsonDateHandling h ->
            new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)
                    .setDateHandling(h)
                    .parseText(offsetJson).when
        }

        assert slurp(JsonDateHandling.STRING) instanceof String
        assert slurp(JsonDateHandling.UTIL_DATE) instanceof Date              // the default
        assert slurp(JsonDateHandling.INSTANT) instanceof java.time.Instant

        // only OFFSET_DATE_TIME keeps the offset the document carried
        def odt = slurp(JsonDateHandling.OFFSET_DATE_TIME)
        assert odt instanceof java.time.OffsetDateTime
        assert odt.offset == java.time.ZoneOffset.ofHours(10)
        // end::date_handling[]

        // the deprecated boolean maps onto two of the four, on its own
        def fresh = { new JsonSlurper(type: JsonParserType.INDEX_OVERLAY) }
        assert fresh().dateHandling == JsonDateHandling.UTIL_DATE
        assert fresh().setCheckDates(false).dateHandling == JsonDateHandling.STRING
        assert fresh().setCheckDates(true).dateHandling == JsonDateHandling.UTIL_DATE

        // but it cannot undo a choice made through setDateHandling, in either order
        def wider = JsonDateHandling.OFFSET_DATE_TIME
        assert fresh().setDateHandling(wider).setCheckDates(true).dateHandling == wider
        assert fresh().setDateHandling(wider).setCheckDates(false).dateHandling == wider
        assert fresh().setCheckDates(true).setDateHandling(wider).dateHandling == wider

        // isCheckDates reports the handling in effect
        assert !fresh().setDateHandling(JsonDateHandling.STRING).isCheckDates()
        assert fresh().setDateHandling(JsonDateHandling.INSTANT).isCheckDates()

        // a date carrying no time is left alone by every parser type
        JsonParserType.values().each {
            assert new JsonSlurper(type: it).parseText('{"d":"2026-09-04"}').d instanceof String
        }
        // LAX converts as INDEX_OVERLAY does; CHARACTER_SOURCE does not
        assert new JsonSlurper(type: JsonParserType.LAX).parseText(json).when instanceof Date
        assert new JsonSlurper(type: JsonParserType.CHARACTER_SOURCE).parseText(json).when instanceof String
    }

    @Test
    void testJsonOutput() {
        // tag::json_output[]
        def json = JsonOutput.toJson([name: 'John Doe', age: 42])

        assert json == '{"name":"John Doe","age":42}'
        // end::json_output[]
    }

    @Test
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

    @Test
    void testJsonOutputWithGenerator() {
        assertScript '''
        import groovy.json.*

        // tag::json_output_generator[]
        class Person {
            String name
            String title
            int age
            String password
            Date dob
            URL favoriteUrl
        }

        Person person = new Person(name: 'John', title: null, age: 21, password: 'secret',
                                    dob: Date.parse('yyyy-MM-dd', '1984-12-15'),
                                    favoriteUrl: new URL('http://groovy-lang.org/'))

        def generator = new JsonGenerator.Options()
            .excludeNulls()
            .dateFormat('yyyy@MM')
            .excludeFieldsByName('age', 'password')
            .excludeFieldsByType(URL)
            .build()

        assert generator.toJson(person) == '{"name":"John","dob":"1984@12"}'
        // end::json_output_generator[]
        '''
    }

    @Test
    void testJsonOutputConverter() {
        assertScript '''
        import groovy.json.*
        import static groovy.test.GroovyAssert.shouldFail

        // tag::json_output_converter[]
        class Person {
            String name
            URL favoriteUrl
        }

        Person person = new Person(name: 'John', favoriteUrl: new URL('http://groovy-lang.org/json.html#_jsonoutput'))

        def generator = new JsonGenerator.Options()
            .addConverter(URL) { URL u, String key ->
                if (key == 'favoriteUrl') {
                    u.getHost()
                } else {
                    u
                }
            }
            .build()

        assert generator.toJson(person) == '{"name":"John","favoriteUrl":"groovy-lang.org"}'

        // No key available when generating a JSON Array
        def list = [new URL('http://groovy-lang.org/json.html#_jsonoutput')]
        assert generator.toJson(list) == '["http://groovy-lang.org/json.html#_jsonoutput"]'

        // First parameter to the converter must match the type for which it is registered
        shouldFail(IllegalArgumentException) {
            new JsonGenerator.Options()
                .addConverter(Date) { Calendar cal -> }
        }
        // end::json_output_converter[]
        '''
    }

    @Test
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

    // tag::typed_classes[]
    static class ServerConfig {
        String host
        int port
        boolean debug
    }

    static class AppConfig {
        String name
        ServerConfig server
    }

    enum Color { RED, GREEN, BLUE }

    static class Item { String name; Color color }
    // end::typed_classes[]

    @Test
    void testTypedCoercion() {
        // tag::typed_coercion[]
        def json = '{"host":"localhost","port":8080,"debug":true}'
        def config = new JsonSlurper().parseText(json) as ServerConfig

        assert config instanceof ServerConfig
        assert config.host == 'localhost'
        assert config.port == 8080
        assert config.debug == true
        // end::typed_coercion[]
    }

    @Test
    void testTypedCoercionNested() {
        // tag::typed_coercion_nested[]
        def json = '{"name":"myapp","server":{"host":"localhost","port":9090,"debug":false}}'
        def config = new JsonSlurper().parseText(json) as AppConfig

        assert config.name == 'myapp'
        assert config.server instanceof ServerConfig
        assert config.server.host == 'localhost'
        assert config.server.port == 9090
        // end::typed_coercion_nested[]
    }

    @Test
    void testTypedCoercionEnum() {
        // tag::typed_coercion_enum[]
        def item = new JsonSlurper().parseText('{"name":"widget","color":"GREEN"}') as Item
        assert item.color == Color.GREEN
        // end::typed_coercion_enum[]
    }

    @Test
    void testJacksonDirectUsage() {
        // tag::jackson_direct[]
        // For advanced cases (typed collections, date parsing, @JsonProperty),
        // use jackson-databind directly:
        //
        // @Grab('com.fasterxml.jackson.core:jackson-databind')
        // import com.fasterxml.jackson.databind.ObjectMapper
        //
        // def config = new ObjectMapper().readValue(jsonString, ServerConfig)
        // end::jackson_direct[]
    }

}
