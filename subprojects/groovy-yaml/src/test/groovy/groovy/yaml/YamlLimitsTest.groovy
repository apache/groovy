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
 * Jackson 3 lowered its default nesting depth to 500; groovy-yaml keeps the 1000 levels
 * it allowed on Jackson 2, the same bound JsonSlurper and XmlParser apply.
 */
class YamlLimitsTest {

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
        assert new YamlSlurper().parseText(nested(1000)) == nestedList(1000)
    }

    @Test
    void testParseRejectsNestingBeyond1000Levels() {
        def e = shouldFail {
            new YamlSlurper().parseText(nested(1001))
        }
        assert e.message.contains('1000')
    }

    @Test
    void testParseAsAllowsNestingOf1000Levels() {
        assert new YamlSlurper().parseTextAs(List, nested(1000)) == nestedList(1000)
    }

    @Test
    void testBuilderWritesNestingOf1000Levels() {
        def yaml = new YamlBuilder()
        yaml(nestedList(1000))
        assert new YamlSlurper().parseText(yaml.toString()) == nestedList(1000)
        assert new YamlSlurper().parseText(YamlBuilder.toYaml(nestedList(1000))) == nestedList(1000)
    }
}
