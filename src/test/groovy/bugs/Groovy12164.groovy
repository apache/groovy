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
 * {@code Closure.call}'s cached fast path (GROOVY-11911) only recognised a
 * {@code call(Object)} override, so a closure with a <em>typed</em> parameter —
 * which generates {@code call(T)}/{@code doCall(T)} — fell back to full
 * metaclass dispatch on every call. GROOVY-12164 extends the cache with a
 * guarded typed override: an argument already an instance of the declared type
 * dispatches directly; anything needing Groovy coercion (GString to String,
 * number conversions, null) still goes through the metaclass. These tests pin
 * the behaviour either side of that guard — results must be indistinguishable
 * from metaclass dispatch. Iteration goes through DGM, whose Java code calls
 * {@code closure.call(item)}, the path the cache serves.
 */
final class Groovy12164 {

    @Test
    void testTypedParameterViaIteration() {
        assertScript '''
            int sum = 0
            [1, 2, 3, 4].each { Integer x -> sum += x }
            assert sum == 10
            assert [1, 2, 3].collect { Integer x -> x * x } == [1, 4, 9]
        '''
    }

    @Test
    void testTypedParameterDirectCall() {
        assertScript '''
            Closure<Integer> c = { Integer x -> x + 1 }
            assert c.call(41) == 42
            assert c(41) == 42
        '''
    }

    @Test
    void testNullArgumentStillDispatches() {
        // null is not an instance of the declared type: must fall through to the
        // metaclass and reach the body as null, exactly as before
        assertScript '''
            def seen = []
            [null, 2].each { Integer x -> seen << x }
            assert seen == [null, 2]
        '''
    }

    @Test
    void testCoercionStillApplies() {
        assertScript '''
            // GString -> String coercion happens on the metaclass path
            def name = 'world'
            def result = ["hello ${name}"].collect { String s -> s.toUpperCase() }
            assert result == ['HELLO WORLD']
            assert result[0] instanceof String
        '''
    }

    @Test
    void testNumberConversionMatchesMetaclassDispatch() {
        assertScript '''
            import groovy.test.GroovyAssert
            // The metaclass coerces some number shapes (BigDecimal literal to a double
            // parameter) and rejects others (Long to an int parameter) with a
            // MissingMethodException. The typed guard declines both to the metaclass,
            // so each keeps its pre-existing outcome.
            def seen = []
            [1.5, 2.5].each { double x -> seen << x }
            assert seen == [1.5d, 2.5d]
            GroovyAssert.shouldFail(MissingMethodException) {
                [1L].each { int x -> }
            }
        '''
    }

    @Test
    void testPrimitiveParameterExactMatch() {
        // an Integer argument to an int parameter passes the (boxed) guard and unboxes
        assertScript '''
            int sum = 0
            [1, 2, 3].each { int x -> sum += x }
            assert sum == 6
        '''
    }

    @Test
    void testDefaultParameterValues() {
        // default values generate overloaded doCall/call: the guard must not disturb selection
        assertScript '''
            def c = { Integer x, Integer y = 10 -> x + y }
            assert c.call(1) == 11
            assert c.call(1, 2) == 3
        '''
    }

    @Test
    void testVarargClosureCollection() {
        // an array-typed parameter is vararg collection, which stays metaclass work
        assertScript '''
            def c = { Object... items -> items.size() }
            assert c.call('a') == 1
            assert c.call('a', 'b') == 2
        '''
    }

    @Test
    void testMultiParameterDestructuring() {
        // a single List argument to a two-parameter closure destructures via the metaclass
        assertScript '''
            def seen = []
            [[1, 'a'], [2, 'b']].each { Integer n, String s -> seen << "$n$s".toString() }
            assert seen == ['1a', '2b']
        '''
    }

    @Test
    void testTypedSubclassOverride() {
        // a Closure subclass with a typed call override is served by the same cache
        assertScript '''
            class Doubler extends Closure<Integer> {
                Doubler() { super(null) }
                Integer call(Integer x) { x * 2 }
            }
            Closure<Integer> d = new Doubler()
            assert d.call(21) == 42
            assert [1, 2, 3].collect(d) == [2, 4, 6]
        '''
    }

    @Test
    void testStaticCallDeclarationIsNotAnOverride() {
        // A public static call(T) — or static call(Object) — is not an instance override:
        // the cached fast path must skip it (reflective invocation would silently ignore the
        // receiver) and keep dispatching instance doCall via the metaclass. Exercised through
        // DGM iteration, the Closure.call(Object...) entry point the cache serves; a direct
        // c.call(x) from Groovy code is metaclass dispatch, where selecting a static is legal.
        assertScript '''
            class TypedStatic extends Closure<String> {
                TypedStatic() { super(null) }
                static String call(Integer x) { 'static' }
                String doCall(Integer x) { 'instance' }
            }
            class ObjectStatic extends Closure<String> {
                ObjectStatic() { super(null) }
                static String call(Object x) { 'static' }
                String doCall(Object x) { 'instance' }
            }
            assert [1].collect(new TypedStatic())  == ['instance']
            assert [1].collect(new ObjectStatic()) == ['instance']
        '''
    }

    @Test
    void testExceptionPropagation() {
        // an exception thrown by the typed body surfaces unwrapped
        assertScript '''
            import groovy.test.GroovyAssert
            def boom = { Integer x -> throw new IllegalStateException('boom ' + x) }
            def e = GroovyAssert.shouldFail(IllegalStateException) {
                [7].each(boom)
            }
            assert e.message == 'boom 7'
        '''
    }
}
