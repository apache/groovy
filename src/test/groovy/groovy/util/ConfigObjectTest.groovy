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
package groovy.util

import org.junit.jupiter.api.Test


class ConfigObjectTest {

    @Test
    void test_isSet_Returns_true_for_Boolean_option_with_value_true() {
        def config = new ConfigSlurper().parse('foo { booleanTrue=true }')
        assert config.foo.isSet('booleanTrue')
    }

    @Test
    void test_isSet_Returns_true_for_Boolean_option_with_value_false() {
        def config = new ConfigSlurper().parse('foo { booleanFalse=false }')
        assert config.foo.isSet('booleanFalse')
    }

    @Test
    void test_isSet_Returns_true_for_nonempty_String_option() {
        def config = new ConfigSlurper().parse('foo { string="hello" }')
        assert config.foo.isSet('string')
    }

    @Test
    void test_isSet_Returns_true_for_empty_String_option() {
        def config = new ConfigSlurper().parse("foo { emptyString='' }")
        assert config.foo.isSet('emptyString')
    }

    @Test
    void test_isSet_Returns_true_for_nonempty_List_option() {
        def config = new ConfigSlurper().parse("foo { list=['a', 'b'] }")
        assert config.foo.isSet('list')
    }

    @Test
    void test_isSet_Returns_true_for_empty_List_option() {
        def config = new ConfigSlurper().parse('foo { emptyList=[] }')
        assert config.foo.isSet('emptyList')
    }

    @Test
    void test_isSet_Returns_true_for_nonempty_nested_block() {
        ConfigObject config = new ConfigSlurper().parse('foo { nestedBlock { setting=true } }')
        assert config.foo.isSet('nestedBlock')
    }

    @Test
    void test_isSet_Returns_false_for_nonexisting_option() {
        def config = new ConfigSlurper().parse('foo { }')
        assert config.foo.isSet('nonexisting') == false
    }

    @Test
    void test_isSet_Returns_false_for_unset_option() {
        def config = new ConfigSlurper().parse('foo { unset }')
        assert config.foo.isSet('unset') == false
    }

    @Test
    void test_isSet_Returns_false_for_empty_nested_block() {
        def config = new ConfigSlurper().parse('foo { emptyNestedBlock { } }')
        assert config.foo.isSet('emptyNestedBlock') == false
    }

    @Test
    void test_prettyPrint() {
        def configString = '''\
development {
    rabbitmq {
        active=true
        hostname='localhost'
    }
}'''

        def config = new ConfigSlurper().parse(configString)
        assert config == new ConfigSlurper().parse(config.prettyPrint())
    }

    // GROOVY-12273: writeTo documents a round trip with ConfigSlurper.parse, which compiles the
    // output as a Groovy script. Anything written as source rather than as a literal is read
    // back as whatever it happens to parse as, up to and including running.
    private static void assertRoundTrips(String description, ConfigObject original) {
        def written = new StringWriter()
        original.writeTo(written)
        def text = written.toString()
        def reparsed
        try {
            reparsed = new ConfigSlurper().parse(text)
        } catch (Exception e) {
            assert false, "$description did not parse back: ${e.message}\n$text"
        }
        // The contract is that data survives, not that types do: a value with no literal form,
        // such as a StringBuilder, necessarily comes back as its text.
        assert asText(reparsed) == asText(original), "$description changed across the round trip\n$text"
    }

    private static Object asText(Object value) {
        if (value instanceof CharSequence) return value.toString()
        if (value instanceof Map) return value.collectEntries { k, v -> [(asText(k)): asText(v)] }
        if (value instanceof Collection) return value.collect { asText(it) }
        if (value != null && value.class.array) return value.collect { asText(it) }
        value
    }

    private static ConfigObject configOf(Map entries) {
        def config = new ConfigObject()
        entries.each { k, v -> config.put(k, v) }
        config
    }

    @Test
    void testKeysThatAreNotIdentifiersRoundTrip() {
        [
            'a keyword'          : 'class',
            'a space'            : 'a b',
            'a quote'            : "a'b",
            'a backslash'        : 'a\\b',
            'a dot'              : 'a.b',
            'a leading digit'    : '1abc',
            'a hyphen'           : 'a-b',
            'empty'              : '',
        ].each { description, key ->
            assertRoundTrips("key with $description", configOf([(key): 1]))
        }
    }

    @Test
    void testKeyCannotSmuggleCodeIntoTheRoundTrip() {
        def marker = 'groovy.test.configObjectInjection'
        System.clearProperty(marker)
        // Concatenated rather than interpolated: a ConfigObject key must be a String.
        def key = 'x = System.setProperty(\'' + marker + '\', \'yes\'); y'

        assertRoundTrips('key holding an assignment', configOf([(key): 1]))
        assert System.getProperty(marker) == null, 'a key was executed rather than read as data'
    }

    @Test
    void testNestedConfigUnderAKeyThatIsNotAnIdentifierRoundTrips() {
        def nested = new ConfigObject()
        nested.p = 1
        nested.q = 2
        assertRoundTrips('nested block under an awkward key', configOf(['a b': nested]))
    }

    @Test
    void testValuesWhoseTextContainsADollarRoundTrip() {
        def dollar = '$'
        [
            'String'            : 'costs $5',
            'GString'           : "costs ${dollar}5",
            'StringBuilder'     : new StringBuilder('costs $5'),
            'GString in a list' : ["a ${dollar}b"],
            'GString in a map'  : [k: "a ${dollar}b"],
        ].each { description, value ->
            assertRoundTrips("value of type $description", configOf([foo: value]))
        }
    }

    @Test
    void testAwkwardKeysInsideFlattenedKeyChainsRoundTrip() {
        // single-entry chains are written as dotted prefixes; every component must be rendered,
        // and a chain opening with a quoted key needs a receiver to parse as navigation
        def middle = new ConfigObject()
        middle.x.'a b'.y = 1
        assertRoundTrips('awkward key in the middle of a chain', middle)

        def leading = new ConfigObject()
        leading.'a b'.c.d = 1
        assertRoundTrips('awkward key opening a chain', leading)
    }

    @Test
    void testArrayValuesRoundTripAsLists() {
        // arrays have no literal form; they are written as list literals and read back as lists
        [
            'String[]'            : ['one', 'two'] as String[],
            'int[]'               : [1, 2, 3] as int[],
            'array inside a list' : [[1, 2] as int[]],
        ].each { description, value ->
            assertRoundTrips("value of type $description", configOf([foo: value]))
        }
    }

    @Test
    void testValuesThatWriteAsThemselvesAreUnchanged() {
        def written = new StringWriter()
        configOf([i: 42, d: 1.5, b: true, s: 'plain']).writeTo(written)
        def text = written.toString()
        assert text.contains('i=42')
        assert text.contains('d=1.5')
        assert text.contains('b=true')
        assert text.contains("s='plain'")
    }
}
