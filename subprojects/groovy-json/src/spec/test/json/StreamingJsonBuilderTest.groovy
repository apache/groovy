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

import groovy.test.GroovyTestCase

class StreamingJsonBuilderTest extends GroovyTestCase {

    void testStreamingJsonBuilder() {
        assertScript """
            import groovy.json.*
            @Grapes([
                @Grab('com.google.code.gson:gson:2.3.1'), //required by json-unit
                @Grab('net.javacrumbs.json-unit:json-unit:1.5.2')])
            import net.javacrumbs.jsonunit.JsonAssert

            // tag::json_string[]
            String carRecords = '''
                {
                    "records": {
                    "car": {
                        "name": "HSV Maloo",
                        "make": "Holden",
                        "year": 2006,
                        "country": "Australia",
                        "record": {
                          "type": "speed",
                          "description": "production pickup truck with speed of 271kph"
                        }
                      }
                  }
                }
            '''
            // end::json_string[]
            
            // tag::streaming_json_builder[]
            StringWriter writer = new StringWriter()
            StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
            builder.records {
              car {
                    name 'HSV Maloo'
                    make 'Holden'
                    year 2006 
                    country 'Australia'
                    record {
                        type 'speed'
                        description 'production pickup truck with speed of 271kph'
                    }
              }
            }
            String json = JsonOutput.prettyPrint(writer.toString())
            // end::streaming_json_builder[]
            
            // tag::json_assert[]
            JsonAssert.assertJsonEquals(json, carRecords)
            // end::json_assert[]
       """
    }

    void testStreamingJsonBuilderWithGenerator() {
        assertScript '''
            import groovy.json.*
            // tag::streaming_json_builder_generator[]
            def generator = new JsonGenerator.Options()
                    .excludeNulls()
                    .excludeFieldsByName('make', 'country', 'record')
                    .excludeFieldsByType(Number)
                    .addConverter(URL) { url -> "http://groovy-lang.org" }
                    .build()

            StringWriter writer = new StringWriter()
            StreamingJsonBuilder builder = new StreamingJsonBuilder(writer, generator)

            builder.records {
              car {
                    name 'HSV Maloo'
                    make 'Holden'
                    year 2006
                    country 'Australia'
                    homepage new URL('http://example.org')
                    record {
                        type 'speed'
                        description 'production pickup truck with speed of 271kph'
                    }
              }
            }

            assert writer.toString() == '{"records":{"car":{"name":"HSV Maloo","homepage":"http://groovy-lang.org"}}}'
            // end::streaming_json_builder_generator[]
        '''
    }
}