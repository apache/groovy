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

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unquoted scalars resolve by the YAML 1.2 JSON schema, as Jackson 3 resolves them:
 * only lowercase {@code true}, {@code false} and {@code null} and plain decimal numbers
 * take a type, and YAML 1.1 forms such as {@code off}, {@code ~} and {@code 0x1F} stay strings.
 */
class YamlScalarResolutionTest {

    static class Flags {
        boolean enabled
    }

    private static Object value(String scalar) {
        new YamlSlurper().parseText("k: $scalar").k
    }

    @Test
    void testOnlyLowercaseTrueAndFalseAreBooleans() {
        assert value('true') instanceof Boolean && value('true') == true
        assert value('false') instanceof Boolean && value('false') == false
        ['True', 'TRUE', 'False', 'FALSE'].each {
            assert value(it) == it
        }
    }

    @Test
    void testYaml11BooleanWordsAreStrings() {
        ['yes', 'no', 'on', 'off', 'Yes', 'NO', 'On', 'OFF', 'y', 'n'].each {
            assert value(it) == it
        }
        // a non-empty String is truthy, so a YAML 1.1 'off' no longer reads as false
        assert new YamlSlurper().parseText('enabled: off').enabled
    }

    @Test
    void testOnlyLowercaseNullIsNull() {
        assert value('null') == null
        ['~', 'Null', 'NULL'].each {
            assert value(it) == it
        }
    }

    @Test
    void testOnlyPlainDecimalNumbersAreNumbers() {
        assert value('12') == 12
        assert value('-3') == -3
        assert value('1.5') == 1.5
        ['0x1F', '017', '1_000', '+12'].each {
            assert value(it) == it
        }
    }

    @Test
    void testQuotedScalarsAreStrings() {
        assert value('"true"') == 'true'
        assert value("'12'") == '12'
        assert value('"null"') == 'null'
    }

    @Test
    void testTypedMapParseResolvesScalarsTheSameWay() {
        assert new YamlSlurper().parseTextAs(Map, 'a: true\nb: off\nc: ~\nd: 0x1F\n') ==
                [a: true, b: 'off', c: '~', d: '0x1F']
    }

    @Test
    void testTypedBooleanPropertyRejectsYaml11Words() {
        assert new YamlSlurper().parseTextAs(Flags, 'enabled: false').enabled == false
        def e = shouldFail {
            new YamlSlurper().parseTextAs(Flags, 'enabled: off')
        }
        assert e.message.contains('"off"')
    }
}
