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

import org.apache.groovy.toml.util.TomlConverter
import org.junit.jupiter.api.Test

import java.time.ZoneId

import static groovy.test.GroovyAssert.shouldFail

class TomlArrayRoundTripTest {

    private static Object roundTrip(Object built) {
        return new TomlSlurper().parseText(built.toString())
    }

    @Test
    void testEmptyRootArray() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml([])

        assert toml.toString() == ' = []\n'
        assert roundTrip(toml) == []
        assert TomlBuilder.toToml([]) == ' = []\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml([])) == []
    }

    @Test
    void testNestedRootArray() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml([[1, 2], [], [3]])

        assert toml.toString() == ' = [[1, 2], [], [3]]\n'
        assert roundTrip(toml) == [[1, 2], [], [3]]
        assert TomlBuilder.toToml([[1, 2], [], [3]]) == ' = [[1, 2], [], [3]]\n'
    }

    @Test
    void testDeeplyNestedRootArray() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml([[[[1]]], []])

        assert toml.toString() == ' = [[[[1]]], []]\n'
        assert roundTrip(toml) == [[[[1]]], []]
    }

    @Test
    void testRootArrayOfTables() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml([[name: 'Guillaume'], [name: 'Jochen'], [name: 'Paul']])

        assert toml.toString() == " = [{name = 'Guillaume'}, {name = 'Jochen'}, {name = 'Paul'}]\n"
        assert roundTrip(toml) == toml.content
    }

    @Test
    void testEmptyAndNestedArraysInsideObject() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml {
            empty([])
            nested([[1, 2], [], [3]])
        }

        assert toml.toString() == '''\
empty = []
nested = [[1, 2], [], [3]]
'''
        assert roundTrip(toml) == toml.content
    }

    @Test
    void testEmptyRootObjectRoundTrip() {
        // GROOVY-12738
        def toml = new TomlBuilder()
        toml([:])

        assert toml.toString() == ' = {}\n'
        assert roundTrip(toml) == [:]
    }

    @Test
    void testEmptyRootObjectAndScalars() {
        // GROOVY-12738
        assert TomlBuilder.toToml([:]) == ' = {}\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml([:])) == [:]
        assert TomlBuilder.toToml('hello') == " = 'hello'\n"
        assert new TomlSlurper().parseText(TomlBuilder.toToml('hello')) == 'hello'
        assert TomlBuilder.toToml(1) == ' = 1\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml(1)) == 1
        assert TomlBuilder.toToml(true) == ' = true\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml(true)) == true
        // TOML has no null. A root null is written as an empty string, and reads back as one.
        assert new TomlBuilder().toString() == " = ''\n"
        assert TomlBuilder.toToml(null) == " = ''\n"
        assert new TomlSlurper().parseText(TomlBuilder.toToml(null)) == ''
    }

    @Test
    void testNamedArrayIsNotUnwrapped() {
        // GROOVY-12738
        assert new TomlSlurper().parseText('v = [1, 2, 3]\n') == [v: [1, 2, 3]]
        assert new TomlSlurper().parseText("name = ' = [1, 2]'\n").name == ' = [1, 2]'
        // A quoted empty key is a real key. It is not the historical root form.
        assert new TomlSlurper().parseText("'' = [1, 2]\n") == ['': [1, 2]]
    }

    @Test
    void testRootTableIsNotWrapped() {
        // GROOVY-12738
        assert TomlBuilder.toToml([a: 1]) == 'a = 1\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml([host: 'localhost', port: 8080])) == [host: 'localhost', port: 8080]
    }

    @Test
    void testRootScalarsJacksonDoesNotTreatAsTables() {
        // GROOVY-12738
        assert TomlBuilder.toToml(URI.create('http://example.org')) == " = 'http://example.org'\n"
        assert new TomlSlurper().parseText(TomlBuilder.toToml(URI.create('http://example.org'))) == 'http://example.org'
        assert TomlBuilder.toToml(ZoneId.of('UTC')) == " = 'UTC'\n"
        assert new TomlSlurper().parseText(TomlBuilder.toToml(ZoneId.of('UTC'))) == 'UTC'
        // An empty Optional used to be written as an empty document, with no error.
        assert TomlBuilder.toToml(Optional.empty()) == " = ''\n"
        assert new TomlSlurper().parseText(TomlBuilder.toToml(Optional.empty())) == ''
        assert TomlBuilder.toToml(Optional.of('x')) == " = 'x'\n"
    }

    @Test
    void testRootIterableIsAnArray() {
        // GROOVY-12738
        Iterable<Integer> values = new Iterable<Integer>() {
            @Override
            Iterator<Integer> iterator() {
                [1, 2].iterator()
            }
        }
        assert TomlBuilder.toToml(values) == ' = [1, 2]\n'
        assert new TomlSlurper().parseText(TomlBuilder.toToml(values)) == [1, 2]
    }

    @Test
    void testHistoricalRootValueKeepsAnInnerCarrierKey() {
        // GROOVY-12738
        assert new TomlSlurper().parseText(' = {v = 1}\n') == [v: 1]
    }

    @Test
    void testHistoricalRootWithAnotherKeyIsRejected() {
        // GROOVY-12738
        def e = shouldFail(TomlRuntimeException) {
            new TomlSlurper().parseText('''\
 = [1]
name = 'x'
''')
        }
        assert e.message.contains('document root')
    }

    @Test
    void testUnreadableSource() {
        // GROOVY-12738
        // The reader failure is wrapped for Java callers. A Groovy call site
        // unwraps that wrapper, so the exception observed here is the IOException.
        def e = shouldFail(IOException) {
            new TomlSlurper().parse(new FilterReader(Reader.nullReader()) {
                @Override
                int read(char[] cbuf, int off, int len) {
                    throw new IOException('boom')
                }
            })
        }
        assert e.message == 'boom'
    }

    @Test
    void testRootWithoutPropertiesIsRejected() {
        // GROOVY-12738
        def e = shouldFail {
            TomlBuilder.toToml(new Object())
        }
        assert e.message.contains('serializer')
    }

    @Test
    void testBrokenJsonIsRejected() {
        // GROOVY-12738
        def e = shouldFail {
            TomlConverter.convertJsonToToml(new StringReader('{'))
        }
        assert e.message
    }
}
