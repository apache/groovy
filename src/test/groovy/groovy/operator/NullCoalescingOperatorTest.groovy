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
package groovy.operator

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript

final class NullCoalescingOperatorTest {

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testNullOnlyFallback(String mode) {
        assertScript """
            $mode
            void test() {
                assert (null ?? 10) == 10
                assert (0 ?? 10) == 0
                assert (false ?? true) == false
                assert ('' ?? 'default') == ''
                assert ([] ?? [1, 2]) == []
                assert ([:] ?? [enabled: true]) == [:]
                assert ('x' ?? 'default') == 'x'
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testLeftEvaluatedOnceRightOnlyWhenNull(String mode) {
        assertScript """
            $mode
            void test() {
                int lefts = 0, rights = 0
                Integer value = 0
                Closure<Integer> left = { -> lefts++; value }
                Closure<Integer> fallback = { -> rights++; 10 }

                assert (left() ?? fallback()) == 0
                assert lefts == 1 && rights == 0

                value = null
                assert (left() ?? fallback()) == 10
                assert lefts == 2 && rights == 1
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testPrimitiveLeftNeverFallsBack(String mode) {
        assertScript """
            $mode
            void test() {
                int rights = 0
                int zero = 0
                assert (zero ?? rights++) == 0
                assert rights == 0
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testComposesWithSafeChain(String mode) {
        assertScript """
            $mode
            void test() {
                String s = null
                assert (s??.trim().toUpperCase() ?? 'unknown') == 'unknown'
                s = ' abc '
                assert (s??.trim().toUpperCase() ?? 'unknown') == 'ABC'
                s = '  '
                assert (s??.trim() ?? 'unknown') == ''
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testInferredType(String mode) {
        assertScript """
            $mode
            void test() {
                String s = null
                String t = s ?? 'fallback'
                assert t.length() == 8
                Integer n = null
                Integer m = n ?? 3
                assert m + 1 == 4
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testAssignOnlyWhenNull(String mode) {
        assertScript """
            $mode
            void test() {
                Integer retries = 0
                retries ??= 3
                assert retries == 0
                retries = null
                assert (retries ??= 3) == 3
                assert retries == 3

                String s = null
                s ??= 'set'
                assert s.length() == 3
                s = ''
                s ??= 'set'
                assert s == ''

                Map<String, Object> config = [enabled: false]
                config.enabled ??= true
                config.missing ??= 'filled'
                assert config == [enabled: false, missing: 'filled']
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.CompileStatic'])
    void testAssignEvaluatesOperandsOnce(String mode) {
        assertScript """
            $mode
            void test() {
                List<String> order = []
                List<Integer> data = [0, null, 30]
                Closure<List<Integer>> receiver = { -> order << 'receiver'; data }
                Closure<Integer> index = { int i -> order << 'index'; i }
                Closure<Integer> value = { -> order << 'value'; 9 }

                assert (receiver()[index(0)] ??= value()) == 0
                assert order == ['receiver', 'index']

                order.clear()
                assert (receiver()[index(1)] ??= value()) == 9
                assert order == ['receiver', 'index', 'value']
                assert data == [0, 9, 30]
            }
            test()
        """
    }

    @Test
    void testPrecedenceAndAssociativity() {
        assertScript '''
            def a = null, b = null, c = 3
            assert (a ?? b ?? c) == 3             // right-associative
            assert (a ?? b ?: 'elvis') == 'elvis' // a ?? (b ?: 'elvis')
            assert (a ?? 0 ?: 'elvis') == 'elvis'
            assert (a ?? false || true) == true   // a ?? (false || true)
            assert (null ?? 1 + 2) == 3           // a ?? (1 + 2)
            def x = 0 ?? 1 ? 'yes' : 'no'         // 0 ?? (1 ? 'yes' : 'no')
            assert x == 0
        '''
    }

    @Test
    void testLayout() {
        assertScript '''
            def a = null
            def b = a
                ?? 'next line'
            assert b == 'next line'
            def c = a ??
                'after operator'
            assert c == 'after operator'
            assert (a??[0]) == [0]                // ?? then a list, not ?[
            assert (a ?? .5) == 0.5
            a??=1                                 // ??= rather than ?? then =
            assert a == 1
        '''
    }
}
