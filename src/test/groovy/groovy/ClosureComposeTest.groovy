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
package groovy

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for Closure composition
 */
final class ClosureComposeTest {

    @Test
    void testComposeMultiply() {
        assertScript '''
            def twice = { a -> a * 2 }
            def thrice = { a -> a * 3 }
            def times6 = twice >> thrice
            assert times6(3) == 18
        '''
    }

    @Test
    void testReverseComposeAndCallShortcut() {
        assertScript '''
            def twice = { a -> a * 2 }
            def thrice = { a -> a * 3 }
            assert twice << thrice << 3 == 18
        '''
    }

    @Test
    void testComposeAndLonghand() {
        assertScript '''
            def twice = { a -> a * 2 }
            def inc = { b -> b + 1 }
            def f = inc >> twice
            def g = { x -> twice(inc(x)) }
            assert f(10) == 22
            assert g(10) == 22
        '''
    }

    @Test // GROOVY-4512
    void testClosureCompositionInstance() {
        def inst = new ComposeTestHelper()
        assert inst.composedA.call() == 42
        assert inst.composedA() == 42
        assert inst.composedB.call(3) == 122
        assert inst.composedB(3) == 122
    }

    static class ComposeTestHelper {
        def closure1 = { 40 }
        def closure2 = { it * 40 }
        def closure3 = { it + 2 }
        def composedA = closure1 >> closure3
        def composedB = closure2 >> closure3
    }

    @Test
    void testComposeWithMethodClosure() {
        assertScript '''
            def s2c = { it.chars[0] }
            def p = Integer.&toHexString >> s2c >> Character.&toUpperCase
            assert p(15) == 'F'
        '''
    }

    @Test
    void testComposeWithMultipleArgs() {
        assertScript '''
            def multiply = { a, b -> a * b }
            def identity = { a -> [a, a] }
            def sq = identity >> multiply
            assert (1..5).collect { sq(it) } == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testReverseCompositionWithMultipleArgs() {
        assertScript '''
            def multiply = { a, b -> a * b }
            def identity = { a -> [a, a] }
            def sq = multiply << identity
            assert (1..5).collect { sq(it) } == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testComposeWithCurriedClosures() {
        assertScript '''
            def add3 = { a, b, c -> a + b + c }
            def add2plus10 = add3.curry(10)
            def multBoth = { a, b, c -> [a * c, b * c] }
            def twiceBoth = multBoth.rcurry(2)
            def twiceBothPlus10 = twiceBoth >> add2plus10
            assert twiceBothPlus10(5, 10) == 40
        '''
    }

    @Test // GROOVY-4994: failed with MissingPropertyException
    void testDelegate() {
        assertScript '''
            def a = { foo }
            def b = { bar }

            class O {
                def foo = 'foo'
                def bar = 'bar'
            }

            def ab = a >> b
            ab.delegate = new O()
            ab()
        '''
    }

    // GROOVY-12673
    @Test
    void testThisObject() {
        assertScript '''
            class O {
                def make() { return { x -> x + 1 } }
            }
            def o = new O()
            def c = o.make()

            assert (c >> { it * 2 }).thisObject === o
            assert (c << { it * 2 }).thisObject === this
            assert c.andThen { it * 2 }.thisObject === o
            assert c.composeSelf().thisObject === o
            assert (c >> { it * 2 }).getThisObject() === o

            @groovy.transform.CompileStatic
            def thisObjectOf(Closure c) { (c >> { it }).thisObject }
            assert thisObjectOf(c) === o
        '''
    }

    @Test
    void testAndThenCS() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            void test() {
                Function<String, String> lower = String::toLowerCase
                Function<String, String> upper = String::toUpperCase
                Function<String, String> lu = lower.andThen(upper)
                Function<? super String, String> ul = upper.andThen(lower)
                assert lower('Hi') == ul('Hi')
                assert upper('Hi') == lu('Hi')
            }
            test()
        '''
    }

    @Test
    void testAndThen() {
        assertScript '''
            def lower = String::toLowerCase
            def upper = String::toUpperCase
            def lu1 = lower.rightShift(upper)
            def ul1 = upper.rightShift(lower)
            assert lower('Hi') == ul1('Hi')
            assert upper('Hi') == lu1('Hi')
            def lu2 = lower.andThen(upper)
            def ul2 = upper.andThen(lower)
            assert lower('Hi') == ul2('Hi')
            assert upper('Hi') == lu2('Hi')
        '''
    }

    @Test
    void testAndThenSelf() {
        assertScript '''
            def inc = String::next
            def inc2 = inc.andThenSelf()
            def inc4 = inc.andThenSelf(3)
            assert inc('abc') == 'abd'
            assert inc2('abc') == 'abe'
            assert inc4('abc') == 'abg'
        '''
    }

    @Test
    void testComposeCS() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            void test() {
                Function<String, String> lower = String::toLowerCase
                Function<String, String> upper = String::toUpperCase
                Function<String, String> ul = lower.compose(upper)
                Function<String, ? extends String> lu = upper.compose(lower)
                assert lower('Hi') == ul('Hi')
                assert upper('Hi') == lu('Hi')
            }
            test()
        '''
    }

    @Test
    void testCompose() {
        assertScript '''
            def lower = String::toLowerCase
            def upper = String::toUpperCase
            def ul1 = lower.leftShift(upper)
            def lu1 = upper.leftShift(lower)
            assert lower('Hi') == ul1('Hi')
            assert upper('Hi') == lu1('Hi')
            def ul2 = lower.compose(upper)
            def lu2 = upper.compose(lower)
            assert lower('Hi') == ul2('Hi')
            assert upper('Hi') == lu2('Hi')
        '''
    }

    @Test
    void testComposeSelf() {
        assertScript '''
            def inc = String::next
            def inc2 = inc.composeSelf()
            def inc4 = inc.composeSelf(3)
            assert inc('abc') == 'abd'
            assert inc2('abc') == 'abe'
            assert inc4('abc') == 'abg'
        '''
    }

    // GROOVY-12665
    @Test
    void testRehydrateComposedClosure() {
        assertScript '''
            class O { def foo() { 'o' } }
            class D { def foo() { 'd' } }
            def o = new O(), d = new D()
            def first = { x, y -> foo() + x + y }
            def second = { s -> s + foo() }

            assert (first >> second).rehydrate(d, o, o)(1, 2) == 'o12o'
            assert (second << first).rehydrate(d, o, o)(1, 2) == 'o12o'

            def c = first >> second
            c.resolveStrategy = Closure.DELEGATE_FIRST
            def r = c.rehydrate(d, o, o)
            assert r !== c
            assert r.delegate === d
            assert r.resolveStrategy == Closure.DELEGATE_FIRST
            assert r(1, 2) == 'd12d'
            assert c.delegate === this
        '''
    }

    // GROOVY-12665
    @Test
    void testDehydrateComposedClosure() {
        assertScript '''
            class O { def foo() { 'o' } }
            def c = { x, y -> x + y } >> { it * 2 }
            def k = c.dehydrate()
            assert k.delegate === null
            assert k(1, 2) == 6

            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ({ x, y -> foo() + x + y } >> { it + '!' }).dehydrate()
            }
            def read
            new ByteArrayInputStream(baos.toByteArray()).withObjectInputStream(this.class.classLoader) {
                it.eachObject { read = it }
            }
            def o = new O()
            assert read.rehydrate(o, o, o)(1, 2) == 'o12!'
        '''
    }
}
