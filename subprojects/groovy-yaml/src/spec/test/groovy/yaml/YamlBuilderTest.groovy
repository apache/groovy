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
package groovy.yaml

import org.junit.jupiter.api.Test

class YamlBuilderTest {

    @Test
    void testBuild() {
        // tag::build_text[]
        def builder = new YamlBuilder()
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

        assert builder.toString() == '''---
records:
  car:
    name: "HSV Maloo"
    make: "Holden"
    year: 2006
    country: "Australia"
    homepage: "http://example.org"
    record:
      type: "speed"
      description: "production pickup truck with speed of 271kph"
'''
        // end::build_text[]
    }

    // tag::typed_writing[]
    static class ServerConfig {
        String host
        int port
    }

    @Test
    void testToYaml() {
        def config = new ServerConfig(host: 'localhost', port: 8080)
        def yaml = YamlBuilder.toYaml(config)
        assert yaml.contains('host:')
        assert yaml.contains('localhost')
        assert yaml.contains('port:')
        assert yaml.contains('8080')
    }
    // end::typed_writing[]

    @Test
    void testTypedRoundTrip() {
        def original = new ServerConfig(host: 'example.com', port: 443)
        def yaml = YamlBuilder.toYaml(original)
        def parsed = new YamlSlurper().parseTextAs(ServerConfig, yaml)
        assert parsed.host == 'example.com'
        assert parsed.port == 443
    }
}