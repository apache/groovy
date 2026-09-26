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
 * groovy-toml applies Jackson 3's default {@code StreamReadConstraints} and
 * {@code StreamWriteConstraints}: at most 500 levels of nesting (Jackson 2 allowed 1000),
 * counting the root table as the first.
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
    void testParseAllowsNestingOf500Levels() {
        assert new TomlSlurper().parseText("a = ${nested(499)}").a == nestedList(499)
        assert new TomlSlurper().parseTextAs(Map, "a = ${nested(499)}").a == nestedList(499)
    }

    @Test
    void testParseRejectsNestingBeyond500Levels() {
        def e = shouldFail {
            new TomlSlurper().parseText("a = ${nested(500)}")
        }
        assert e.message.contains('500')
    }

    @Test
    void testBuilderWritesNestingOf500Levels() {
        def toml = new TomlBuilder()
        toml(a: nestedList(499))
        assert new TomlSlurper().parseText(toml.toString()).a == nestedList(499)
    }
}
