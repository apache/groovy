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
package bugs

import groovy.lang.GString
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.GStringImpl
import org.codehaus.groovy.runtime.InvokerHelper
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * A map literal used to keep a GString key, while a subscript renders a
 * GString to a String before {@code Map.get}. The entry was visible to
 * iteration and invisible to {@code map["${key}"]} and {@code map[key]}.
 * The literal now stores that rendering. {@code Map.put} is unchanged, so a
 * GString placed that way stays distinct from the equivalent String.
 */
final class Groovy3676 {

    // GROOVY-3676
    @Test
    void testLiteralIsReadableByTheSameGStringAndByTheString() {
        assertScript '''
            key1 = "VARIABLE_1"
            a1 = [ "${key1}" : "YES 3" ]
            def seen = []
            a1.eachWithIndex { entry, index -> seen << "$entry" }
            assert seen == ['VARIABLE_1=YES 3']
            assert a1["${key1}"] == "YES 3"
            assert a1[key1] == "YES 3"
            assert a1.VARIABLE_1 == "YES 3"
            assert a1.get(key1) == "YES 3"
            assert a1.containsKey(key1)
        '''
    }

    // GROOVY-3676
    @Test
    void testLiteralKeyIsTheRenderedString() {
        assertScript '''
            def key = 'a'
            def value = "${'letter'} ${key}"
            def m = ["${key}": value]
            assert m.keySet().iterator().next() instanceof String
            assert m.keySet().iterator().next() == 'a'
            assert m["${key}"] == 'letter a'
            assert m['a'] == 'letter a'
            assert m.get('a') == 'letter a'
            assert m.get("${key}") == null
            assert !m.containsKey("${key}")
            assert m == [a: 'letter a']
            assert value instanceof GString
            assert m['a'].is(value)
        '''
    }

    // GROOVY-3676
    @Test
    void testRenderedKeyDoesNotFollowLaterChanges() {
        assertScript '''
            def buf = new StringBuilder('ab')
            def m = ["$buf": 'v']
            buf.append('c')
            assert m['ab'] == 'v'
            assert m['abc'] == null
            assert m.keySet().iterator().next() == 'ab'
            assert !m.containsKey("$buf")
        '''
    }

    // GROOVY-3676
    @Test
    void testKeysThatRenderTheSameTextAreOneEntry() {
        assertScript '''
            def m = ["${'a'}": 1, b: 2, a: 3]
            assert m.size() == 2
            assert m['a'] == 3
            assert m['b'] == 2
            assert m.keySet() as List == ['a', 'b']
        '''
    }

    // GROOVY-3676
    @Test
    void testNamedArgumentAndPropertyReadUseTheRenderedKey() {
        assertScript '''
            def take(Map args) { args.name }
            assert take("${'name'}": 'Ada') == 'Ada'
            def m = ["${'name'}": 'Ada']
            assert m."${'name'}" == 'Ada'
        '''
    }

    // GROOVY-3676
    @Test
    void testPutStillKeepsAGStringDistinctFromItsString() {
        assertScript '''
            def g = "${'SOME KEY'}"
            def map = [:]
            map.put(g, 'value')
            assert map.get('SOME KEY') == null
            assert map.get(g) == 'value'
            assert map[g] == null
            assert map['SOME KEY'] == null
            assert map.keySet().iterator().next().is(g)
        '''
    }

    // GROOVY-3676
    @Test
    void testSpreadCopiesAStoredGStringAndALiteralRenders() {
        assertScript '''
            def g = "${'a'}"
            def src = [:]
            src.put(g, 'from-gstring')
            src.put('a', 'from-string')
            def copy = [*:src, "${'b'}": 'bee']
            assert copy.size() == 3
            assert copy.get(g) == 'from-gstring'
            assert copy.get('a') == 'from-string'
            assert copy['b'] == 'bee'
            assert copy.keySet().any { it.is(g) }

            def k = 'a'
            def over = [*:["${k}": 1], "${k}": 2]
            assert over.size() == 1
            assert over[k] == 2
            assert over.keySet().iterator().next() instanceof String
        '''
    }

    // GROOVY-3676
    @Test
    void testOtherKeysAndNullAreStoredAsThemselves() {
        def map = InvokerHelper.createMap([null, 'nil', 1, 'n', true, 'b'] as Object[])
        assert map[null] == 'nil'
        assert map[1] == 'n'
        assert map[true] == 'b'
        assert map.size() == 3

        def trailing = InvokerHelper.createMap(['only-key'] as Object[])
        assert trailing.isEmpty()
    }

    // GROOVY-3676
    @Test
    void testCustomGStringIsRenderedOnce() {
        def calls = 0
        def g = new GString(['z'] as Object[]) {
            @Override
            String[] getStrings() { ['', ''] as String[] }

            @Override
            String toString() {
                calls++
                'z'
            }
        }
        Object[] values = new Object[4]
        values[0] = g
        values[1] = 'v'
        values[2] = new GStringImpl(new Object[] {'a'}, new String[] {'', ''})
        values[3] = 'w'
        def map = InvokerHelper.createMap(values)
        assert calls == 1
        assert map == [z: 'v', a: 'w']
        assert map.keySet().every { it instanceof String }
    }

    // GROOVY-3676
    @Test
    void testCompileStaticLiteralLookup() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def read(Map map, Object key) { map[key] }

            def key1 = 'VARIABLE_1'
            def a1 = ["${key1}": 'YES 3']
            assert a1["${key1}"] == 'YES 3'
            assert a1[key1] == 'YES 3'
            assert read(a1, key1) == 'YES 3'
            assert read(a1, "${key1}") == 'YES 3'
            assert a1.keySet().iterator().next() instanceof String
        '''
    }

    // GROOVY-3676
    @Test
    void testObjectGetAtFindsAStringKeyWhenNoGStringEntryExists() {
        def g = "${'a'}"
        def map = new LinkedHashMap()
        map.put('a', 'yes')
        assert readKey(map, g) == 'yes'

        map.put('a', null)
        assert readKey(map, g) == null
        assert map.containsKey('a')

        def blanks = new LinkedHashMap()
        blanks.put('', 'empty')
        blanks.put(null, 'nil')
        assert readKey(blanks, "${''}") == 'empty'
        assert readKey(blanks, null) == 'nil'
    }

    // GROOVY-3676
    @Test
    void testObjectGetAtKeepsAStoredGStringAheadOfTheSameString() {
        def g = "${'a'}"
        def map = new LinkedHashMap()
        map.put(g, 'from-gstring')
        map.put('a', 'from-string')
        assert readKey(map, g) == 'from-gstring'

        map.clear()
        map.put(g, null)
        assert readKey(map, g) == null
        assert map.containsKey(g)
        assert !map.containsKey('a')

        assert readKey(new LinkedHashMap(), "${'missing'}") == null
        assert readKey([1: 'n'], 1) == 'n'
    }

    @CompileStatic
    private static Object readKey(Map map, Object key) {
        map[key]
    }
}
