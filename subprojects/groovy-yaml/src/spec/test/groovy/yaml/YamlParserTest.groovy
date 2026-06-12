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

class YamlParserTest {

    @Test
    void testParse() {
        // tag::parse_text[]
        def ys = new YamlSlurper()
        def yaml = ys.parseText '''
language: groovy
sudo: required
dist: trusty

matrix:
  include:
    - jdk: openjdk10
    - jdk: oraclejdk9
    - jdk: oraclejdk8

before_script:
  - |
    unset _JAVA_OPTIONS

        '''

        assert 'groovy' == yaml.language
        assert 'required' == yaml.sudo
        assert 'trusty' == yaml.dist
        assert ['openjdk10', 'oraclejdk9', 'oraclejdk8'] ==  yaml.matrix.include.jdk
        assert ['unset _JAVA_OPTIONS'] == yaml.before_script*.trim()
        // end::parse_text[]
    }

    @Test
    void testBuildAndParse() {
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

        def ys = new YamlSlurper()
        def yaml = ys.parseText(builder.toString())

        assert 'HSV Maloo' == yaml.records.car.name
        assert 'Holden' == yaml.records.car.make
        assert 2006 == yaml.records.car.year
        assert 'Australia' == yaml.records.car.country
        assert 'http://example.org' == yaml.records.car.homepage
        assert 'speed' == yaml.records.car.record.type
        assert 'production pickup truck with speed of 271kph' == yaml.records.car.record.description
    }


    @Test
    void testParsePath() {
        def file = File.createTempFile('test','yml')
        file.deleteOnExit()
        file.text = '''
language: groovy
sudo: required
dist: trusty

matrix:
  include:
    - jdk: openjdk10
    - jdk: oraclejdk9
    - jdk: oraclejdk8
'''

        def ys = new YamlSlurper()
        def yaml = ys.parse(file.toPath())

        // check
        assert 'groovy' == yaml.language
        assert 'required' == yaml.sudo
        assert 'trusty' == yaml.dist
        assert ['openjdk10', 'oraclejdk9', 'oraclejdk8'] ==  yaml.matrix.include.jdk

    }

    // tag::typed_class[]
    static class ServerConfig {
        String host
        int port
    }
    // end::typed_class[]

    @Test
    void testParseTextAs() {
        // tag::typed_parsing[]
        def config = new YamlSlurper().parseTextAs(ServerConfig, '''\
host: localhost
port: 8080
''')
        assert config.host == 'localhost'
        assert config.port == 8080
        // end::typed_parsing[]
    }

    @Test
    void testParseAsFromReader() {
        def reader = new StringReader('host: localhost\nport: 8080')
        def config = new YamlSlurper().parseAs(ServerConfig, reader)
        assert config instanceof ServerConfig
        assert config.host == 'localhost'
        assert config.port == 8080
    }

    @Test
    void testParseAsFromFile() {
        def file = File.createTempFile('test', '.yml')
        file.deleteOnExit()
        file.text = 'host: localhost\nport: 9090'
        def config = new YamlSlurper().parseAs(ServerConfig, file)
        assert config.port == 9090
    }

    @Test
    void testParseAsFromPath() {
        def file = File.createTempFile('test', '.yml')
        file.deleteOnExit()
        file.text = 'host: example.com\nport: 443'
        def config = new YamlSlurper().parseAs(ServerConfig, file.toPath())
        assert config.host == 'example.com'
    }

    @Test
    void testParseAsFromInputStream() {
        def stream = new ByteArrayInputStream('host: localhost\nport: 3000'.bytes)
        def config = new YamlSlurper().parseAs(ServerConfig, stream)
        assert config.port == 3000
    }

    @Test
    void testParseMultiDocs() {
        def ys = new YamlSlurper()
        def yaml = ys.parseText '''\
---
language: groovy
version: 4
---
language: java
version: 8
        '''

        assert 'groovy' == yaml[0].language
        assert 4 == yaml[0].version
        assert 'java' == yaml[1].language
        assert 8 == yaml[1].version
    }

    // tag::temporal_class[]
    static class Event {
        java.time.OffsetDateTime created
        java.time.LocalDate effective
        java.time.LocalTime windowStart
        java.time.LocalDateTime updated
        String name
    }
    // end::temporal_class[]

    @Test
    void testTemporalTypedRoundTrip() {
        // tag::temporal_typed[]
        def original = new Event(
                created: java.time.OffsetDateTime.parse('1979-05-27T07:32:00-08:00'),
                effective: java.time.LocalDate.of(1979, 5, 27),
                windowStart: java.time.LocalTime.of(7, 32, 0),
                updated: java.time.LocalDateTime.of(1979, 5, 27, 7, 32, 0),
                name: 'demo')

        def yaml = YamlBuilder.toYaml(original)
        def parsed = new YamlSlurper().parseTextAs(Event, yaml)

        assert parsed.created == original.created            // OffsetDateTime, non-UTC offset preserved
        assert parsed.effective == original.effective        // LocalDate
        assert parsed.windowStart == original.windowStart    // LocalTime
        assert parsed.updated == original.updated            // LocalDateTime
        assert parsed.name == original.name
        // end::temporal_typed[]
        assert parsed.created instanceof java.time.OffsetDateTime
        assert parsed.effective instanceof java.time.LocalDate
        assert parsed.windowStart instanceof java.time.LocalTime
        assert parsed.updated instanceof java.time.LocalDateTime
        // the round-tripped offset must remain -08:00, not normalised to Z
        assert parsed.created.offset == java.time.ZoneOffset.ofHours(-8)
    }

    @Test
    void testTemporalUntypedStringPath() {
        // KNOWN LIMITATION: the untyped slurp path returns temporal values as
        // String, not java.time.* types. YamlSlurper routes the untyped path
        // through a YAML->JSON conversion, and JSON has no native temporal
        // types. Use the typed parseAs/parseTextAs API for java.time.* fidelity.
        // This test pins the current behaviour so a future change here is deliberate.
        def parsed = new YamlSlurper().parseText('''
created: 1979-05-27T07:32:00-08:00
effective: 1979-05-27
windowStart: 07:32:00
''')
        assert parsed.created == '1979-05-27T07:32:00-08:00'
        assert parsed.effective == '1979-05-27'
        assert parsed.windowStart == '07:32:00'
        assert parsed.created instanceof String
        assert parsed.effective instanceof String
        assert parsed.windowStart instanceof String
    }
}
