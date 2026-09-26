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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Jackson 3 lowered its default nesting depth to 500 and raised its default string length to
 * 100,000,000; groovy-toml keeps the limits it had on Jackson 2: 1000 levels, the same bound
 * JsonSlurper and XmlParser apply, and 20,000,000 characters.
 */
class TomlLimitsTest {

    private static String nested(int depth) {
        '[' * depth + ']' * depth
    }

    private static List nestedList(int depth) {
        def list = []
        (depth - 1).times { list = [list] }
        list
    }

    @Test
    void testParseAllowsNestingOf1000Levels() {
        // the root table is the first level
        assert new TomlSlurper().parseText("a = ${nested(999)}").a == nestedList(999)
    }

    @Test
    void testParseRejectsNestingBeyond1000Levels() {
        def e = shouldFail {
            new TomlSlurper().parseText("a = ${nested(1000)}")
        }
        assert e.message.contains('1000')
    }

    @Test
    void testParseAsAllowsNestingOf1000Levels() {
        assert new TomlSlurper().parseTextAs(Map, "a = ${nested(999)}").a == nestedList(999)
    }

    @Test
    void testBuilderWritesNestingOf1000Levels() {
        def toml = new TomlBuilder()
        toml(a: nestedList(999))
        assert new TomlSlurper().parseText(toml.toString()).a == nestedList(999)
    }

    @Test
    void testStringLengthLimitIsKept() {
        assert new TomlSlurper().parseText('a = "' + 'x' * 19_000_000 + '"').a.size() == 19_000_000
        def e = shouldFail {
            new TomlSlurper().parseText('a = "' + 'x' * 21_000_000 + '"')
        }
        assert e.message.contains('20000000')
    }
}
