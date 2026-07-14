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
 * {@code Closure.call}'s cached fast path covered only zero- and one-argument
 * overrides (GROOVY-11911, GROOVY-12164), so every multi-argument closure call
 * from Java — all {@code Map} iteration, {@code eachWithIndex}, {@code inject}
 * — took full metaclass dispatch. GROOVY-12165 generalises the cache to an
 * arity-indexed table (0..4) with per-argument instance-of guards: at each
 * arity an all-{@code Object} override wins outright, otherwise the single
 * unambiguous typed override dispatches when every argument is already an
 * instance of its declared type, and anything else (coercion, null, ambiguity)
 * falls through to the metaclass exactly as before. These tests pin both sides
 * of the guards at the new arities; iteration goes through DGM, whose Java
 * code invokes {@code closure.call(...)}, the path the cache serves.
 */
final class Groovy12165 {

    @Test
    void testMapIterationUntyped() {
        assertScript '''
            def m = [a: 1, b: 2, c: 3]
            int sum = 0
            def keys = []
            m.each { k, v -> keys << k; sum += (int) v }
            assert keys == ['a', 'b', 'c']
            assert sum == 6
            assert m.collect { k, v -> "$k$v".toString() } == ['a1', 'b2', 'c3']
        '''
    }

    @Test
    void testMapIterationTyped() {
        assertScript '''
            def m = [a: 1, b: 2]
            int sum = 0
            m.each { String k, Integer v -> sum += v }
            assert sum == 3
            assert m.any { String k, Integer v -> v == 2 }
            assert m.every { String k, Integer v -> v > 0 }
        '''
    }

    @Test
    void testEachWithIndexAndInject() {
        assertScript '''
            def seen = []
            ['a', 'b'].eachWithIndex { x, i -> seen << "$i:$x".toString() }
            assert seen == ['0:a', '1:b']
            assert [1, 2, 3, 4].inject(0) { a, x -> (int) a + (int) x } == 10
            assert [1, 2, 3].inject(1) { Integer a, Integer x -> a * x } == 6
        '''
    }

    @Test
    void testThreeArgumentClosure() {
        assertScript '''
            // Map#inject drives a three-parameter closure with (acc, key, value) from Java
            assert [a: 1, b: 2, c: 3].inject(0) { acc, k, v -> (int) acc + (int) v } == 6
            assert [a: 1, b: 2].inject(0) { Integer acc, String k, Integer v -> acc + v } == 3
        '''
    }

    @Test
    void testTypedGuardFallsThroughToCoercion() {
        assertScript '''
            // a GString key is NOT an instance of the String parameter type: the guard fails,
            // the call falls through to the metaclass, and its coercion produces a real String
            def name = 'a'
            def m = [("k${name}"): 1]
            assert m.keySet()[0] !instanceof String // premise: the key stays a GString
            def out = []
            m.each { String k, Integer v -> assert k.class == String; out << "$k=$v".toString() }
            assert out == ['ka=1']
            // null value is not an instance of Integer: metaclass path, arrives as null
            def m2 = [x: null]
            def seen = []
            m2.each { String k, Integer v -> seen << v }
            assert seen == [null]
        '''
    }

    @Test
    void testDefaultParametersPopulateMultipleArities() {
        assertScript '''
            def c = { Integer x, Integer y = 10, Integer z = 100 -> x + y + z }
            assert c.call(1) == 111
            assert c.call(1, 2) == 103
            assert c.call(1, 2, 3) == 6
        '''
    }

    @Test
    void testSameArityOverloadsStayOnMetaclass() {
        // two typed two-arg overrides are ambiguous: the cache must decline and the metaclass
        // fallback dispatches doCall — Map#collect drives call(k, v) from Java
        assertScript '''
            class Picky extends Closure<String> {
                Picky() { super(null) }
                String call(Integer a, Integer b) { 'ints' }
                String call(String a, String b) { 'strings' }
                String doCall(Object a, Object b) { 'doCall' }
            }
            assert [(1): 2].collect(new Picky()) == ['doCall']
        '''
    }

    @Test
    void testStaticMultiArgCallDeclarationIsNotAnOverride() {
        // a public static call(T, T) is not an instance override: dispatch keeps going
        // through the metaclass to instance doCall (same rule as one-arg, GROOVY-12164);
        // Map#collect drives the two-arg call(k, v) entry point from Java
        assertScript '''
            class TwoStatic extends Closure<String> {
                TwoStatic() { super(null) }
                static String call(Integer a, Integer b) { 'static' }
                String doCall(Integer a, Integer b) { 'instance' }
            }
            assert [(1): 2].collect(new TwoStatic()) == ['instance']
        '''
    }

    @Test
    void testMultiParameterDestructuringPreserved() {
        // a single List argument to a two-parameter closure still destructures
        assertScript '''
            def seen = []
            [[1, 'a'], [2, 'b']].each { Integer n, String s -> seen << "$n$s".toString() }
            assert seen == ['1a', '2b']
        '''
    }
}
