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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Prototype for GROOVY-9848: decouple the {@code in} membership operator from {@code isCase}.
 * {@code in} becomes key-based for maps ({@code containsKey}) and substring-based for
 * char sequences (GROOVY-2456), while {@code switch}/{@code grep} keep the value-based
 * {@code isCase} classification. {@code isCase(Map)} is also guarded so classification
 * never mutates a {@code withDefault} map.
 */
final class Groovy9848 {

    @Test
    void testInOnMapIsKeyBased() {
        assertScript '''
            def map = [a:1, b:0, c:false]
            assert 'a' in map           // key present, truthy value
            assert 'b' in map           // key present, value 0 (was: false under old truthy semantics)
            assert 'c' in map           // key present, value false (was: false)
            assert 'z' !in map          // key absent
        '''
    }

    @Test
    void testInOnCharSequenceIsContains() {
        assertScript '''
            assert 'ell'  in 'hello'    // GROOVY-2456
            assert 'xyz' !in 'hello'
            assert 'lo'   in "hel${'lo'}"
        '''
    }

    @Test
    void testInOnCollectionsUnchanged() {
        assertScript '''
            assert 2 in [1, 2, 3]
            assert 5 !in [1, 2, 3]
            assert 3 in (1..5)
            assert 'a' in (['a','b'] as Set)
        '''
    }

    @Test
    void testSwitchStillUsesIsCaseValueSemantics() {
        assertScript '''
            String classify(k, m) { switch (k) { case m -> 'yes'; default -> 'no' } }
            def m = [a:1, b:0]
            assert classify('a', m) == 'yes'   // truthy value
            assert classify('b', m) == 'no'    // value 0 is falsy -> isCase false (UNCHANGED)
            // the deliberate divergence: `in` is key-based, `switch/case` is value-based
            assert ('b' in m) && classify('b', m) == 'no'
        '''
    }

    @Test
    void testGrepStillUsesIsCaseValueFilter() {
        assertScript '''
            def predicate = [apple:true, banana:true, lemon:false, orange:false, pear:true]
            def fruit = ['apple', 'lemon', 'pear', 'orange']
            assert fruit.grep(predicate) == ['apple', 'pear']   // value-based filter UNCHANGED
        '''
    }

    @Test
    void testInDoesNotMutateWithDefaultMap() {
        assertScript '''
            def dm = [:].withDefault{ 42 }
            assert 'x' !in dm
            assert !dm.containsKey('x')         // membership test did not auto-grow
        '''
    }

    @Test
    void testSwitchGrepDoNotMutateWithDefaultMap() {
        assertScript '''
            def dm = [:].withDefault{ 42 }
            def r = switch ('y') { case dm -> 'a'; default -> 'b' }
            assert r == 'b'
            assert !dm.containsKey('y')         // isCase(Map) guard prevented auto-grow
            def dm2 = [:].withDefault{ 99 }
            ['p', 'q'].grep(dm2)
            assert dm2.isEmpty()
        '''
    }

    @Test
    void testInUnderCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            String check() {
                Map<String,Integer> m = [a:1, b:0]
                assert 'b' in m                 // key-based -> true
                assert 'z' !in m
                assert 'ell' in 'hello'         // char-sequence contains
                assert 2 in [1, 2, 3]           // collection unchanged
                'ok'
            }
            assert check() == 'ok'
        '''
    }

    @Test
    void testInUnderCompileStaticWithObjectTypedReceiver() {
        // when the static receiver type is Object, dispatch resolves at runtime via isIn,
        // so Map/CharSequence/collection membership is still correct (production-path check)
        assertScript '''
            @groovy.transform.CompileStatic
            boolean check(Object container, Object key) { key in container }
            assert  check([a:1, b:0], 'b')   // map -> containsKey
            assert !check([a:1], 'z')
            assert  check([1, 2, 3], 2)       // collection -> contains
            assert  check('hello', 'ell')     // char sequence -> contains
        '''
    }

    @Test
    void testSwitchUnderCompileStaticStillIsCase() {
        assertScript '''
            @groovy.transform.CompileStatic
            String classify(String k, Map<String,Integer> m) {
                switch (k) { case m -> 'yes'; default -> 'no' }
            }
            assert classify('a', [a:1, b:0]) == 'yes'
            assert classify('b', [a:1, b:0]) == 'no'   // value-based, unchanged
        '''
    }
}
