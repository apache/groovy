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
package groovy.toml


import groovy.test.GroovyTestCase

class TomlParserTest extends GroovyTestCase {

    void testParse() {
        // tag::parse_text[]
        def ts = new TomlSlurper()
        def toml = ts.parseText '''
language = "groovy"
sudo = "required"
dist = "trusty"
before_script = [ "unset _JAVA_OPTIONS\\n\\n    \\n" ]

[[matrix.include]]
jdk = "openjdk10"

[[matrix.include]]
jdk = "oraclejdk9"

[[matrix.include]]
jdk = "oraclejdk8"
'''

        assert 'groovy' == toml.language
        assert 'required' == toml.sudo
        assert 'trusty' == toml.dist
        assert ['openjdk10', 'oraclejdk9', 'oraclejdk8'] ==  toml.matrix.include.jdk
        assert ['unset _JAVA_OPTIONS'] == toml.before_script*.trim()
        // end::parse_text[]
    }

    void testBuildAndParse() {
        def builder = new TomlBuilder()
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

        def ts = new TomlSlurper()
        def toml = ts.parseText(builder.toString())

        assert 'HSV Maloo' == toml.records.car.name
        assert 'Holden' == toml.records.car.make
        assert 2006 == toml.records.car.year
        assert 'Australia' == toml.records.car.country
        assert 'http://example.org' == toml.records.car.homepage
        assert 'speed' == toml.records.car.record.type
        assert 'production pickup truck with speed of 271kph' == toml.records.car.record.description
    }


    void testParsePath() {
        def file = File.createTempFile('test','yml')
        file.deleteOnExit()
        file.text = '''
language = "groovy"
sudo = "required"
dist = "trusty"

[[matrix.include]]
jdk = "openjdk10"

[[matrix.include]]
jdk = "oraclejdk9"

[[matrix.include]]
jdk = "oraclejdk8"
'''

        def ts = new TomlSlurper()
        def toml = ts.parse(file.toPath())

        // check
        assert 'groovy' == toml.language
        assert 'required' == toml.sudo
        assert 'trusty' == toml.dist
        assert ['openjdk10', 'oraclejdk9', 'oraclejdk8'] ==  toml.matrix.include.jdk

    }
}
