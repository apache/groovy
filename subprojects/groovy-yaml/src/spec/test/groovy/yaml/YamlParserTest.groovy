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
}
