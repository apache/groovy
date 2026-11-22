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
 * Currying a method or closure that has default parameters, then calling it
 * directly or through a metaClass, keeps the overloads those defaults produce.
 */
final class Groovy3351Test {

    // GROOVY-3351
    @Test
    void testDirectCallsKeepDefaultParameters() {
        assertScript '''
            class C {
                static String m(String a, String b = a, Map extra = [:]) { "a=$a b=$b extra=$extra" }
            }
            def curried = C.&m.curry('A')
            assert curried() == 'a=A b=A extra=[:]'
            assert curried('B') == 'a=A b=B extra=[:]'
            assert curried('B', [k: 1]) == 'a=A b=B extra=[k:1]'

            def rcurried = C.&m.rcurry([z: 1])
            assert rcurried('A', 'B') == 'a=A b=B extra=[z:1]'
            assert rcurried.clone()('A', 'B') == 'a=A b=B extra=[z:1]'

            def ncurried = C.&m.ncurry(1, 'B')
            assert ncurried('A') == 'a=A b=B extra=[:]'
            assert ncurried('A', [k: 1]) == 'a=A b=B extra=[k:1]'
        '''
    }

    // GROOVY-3351
    @Test
    void testMetaClassCurryRcurryAndNcurry() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail
            class C {
                static String m(String a, String b, String c = b) { "$a $b $c" }
            }
            try {
                C.metaClass.foo = C.&m.curry('A')
                assert new C().foo('B') == 'A B B'
                assert new C().foo('B', 'C') == 'A B C'

                C.metaClass.bar = C.&m.rcurry('Z')
                assert new C().bar('A') == 'A Z Z'
                assert new C().bar('A', 'B') == 'A B Z'

                C.metaClass.baz = C.&m.ncurry(1, 'B')
                assert new C().baz('A') == 'A B B'
                assert new C().baz('A', 'C') == 'A B C'

                C.metaClass.qux = C.&m.ncurry(2, 'Z')
                assert new C().qux('A', 'B') == 'A B Z'
                shouldFail(MissingMethodException) { new C().qux('A') }
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testSeveralDefaultParametersAndNestedCurry() {
        assertScript '''
            class C {
                static String m(String a, String b = 'B', String c = 'C', String d = 'D') {
                    "$a $b $c $d"
                }
            }
            try {
                C.metaClass.foo = C.&m.curry('A')
                assert new C().foo() == 'A B C D'
                assert new C().foo('b') == 'A b C D'
                assert new C().foo('b', 'c') == 'A b c D'
                assert new C().foo('b', 'c', 'd') == 'A b c d'

                C.metaClass.bar = C.&m.curry('A').rcurry('Z')
                assert new C().bar() == 'A Z C D'
                assert new C().bar('b') == 'A b Z D'
                assert new C().bar('b', 'c') == 'A b c Z'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testOverloadsAndGStringOnMetaClass() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail
            class C {
                static String a(Integer b, Integer c, Integer d) { "3Int: $b $c $d" }
                static String a(b, c, d) { "3Obj: $b $c $d" }
                static String a(b, c) { "2Obj: $b $c" }
                static String a(b) { "1Obj: $b" }
                static String pair(int left, String right) { "s $left $right" }
                static String pair(int left, int right) { "i $left $right" }
            }
            try {
                C.metaClass.foo = C.&a.curry(0)
                assert new C().foo(1, 2) == '3Int: 0 1 2'
                assert new C().foo('x', 2) == '3Obj: 0 x 2'
                assert new C().foo(1) == '2Obj: 0 1'
                assert new C().foo() == '1Obj: 0'
                assert new C().foo("${'x'}") == '2Obj: 0 x'

                C.metaClass.bar = C.&pair.rcurry('x')
                assert new C().bar(1) == 's 1 x'
                C.metaClass.bar = C.&pair.rcurry(5)
                assert new C().bar(1) == 'i 1 5'

                shouldFail(MissingMethodException) { new C().foo(1, 2, 3, 4) }
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testPrimitiveDefaultsAndShorterOverloadSkipped() {
        assertScript '''
            class C {
                static String m() { 'zero' }
                static String m(int a, int b = a + 1) { "$a $b" }
            }
            try {
                C.metaClass.foo = C.&m.curry(2)
                assert new C().foo() == '2 3'
                assert new C().foo(5) == '2 5'

                C.metaClass.same = C.&m.curry()
                assert new C().same(4, 9) == '4 9'
                assert new C().same(4) == '4 5'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testClosureWithDefaultsAndDelegate() {
        assertScript '''
            def c = { String a, String b = a -> "$a-$b-${delegate.name}" }
            class Box { String name = 'box' }
            try {
                Box.metaClass.foo = c.curry('A')
                assert new Box().foo() == 'A-A-box'
                assert new Box().foo('B') == 'A-B-box'

                def direct = c.curry('A')
                direct.delegate = [name: 'direct']
                assert direct() == 'A-A-direct'
                assert direct('B') == 'A-B-direct'

                def lazy = { String a, String b, String d = b -> "$a $b $d" }.rcurry('Z')
                assert lazy('A') == 'A Z Z'
                assert lazy.clone()('A') == 'A Z Z'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Box)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testInstanceMethodAndClassMethodReference() {
        assertScript '''
            class C {
                String id
                String m(String a, String b = a) { "$id:$a:$b" }
            }
            def bound = new C(id: 'bound')
            assert bound.&m.curry('A')() == 'bound:A:A'
            assert bound.&m.curry('A')('B') == 'bound:A:B'

            class Box {}
            try {
                Box.metaClass.foo = bound.&m.curry('A')
                assert new Box().foo() == 'bound:A:A'
                assert new Box().foo('B') == 'bound:A:B'

                Box.metaClass.mid = String.&substring.curry('hello')
                assert new Box().mid(1) == 'ello'
                assert new Box().mid(1, 4) == 'ell'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Box)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testStaticMetaClassAndUncurriedMethodClosure() {
        assertScript '''
            class C {
                static String m(String a, String b = a) { "$a $b" }
            }
            try {
                C.metaClass.plain = C.&m
                assert new C().plain('A') == 'A A'
                assert new C().plain('A', 'B') == 'A B'

                C.metaClass.static.foo = C.&m.curry('A')
                assert C.foo() == 'A A'
                assert C.foo('B') == 'A B'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testVarargsComposedClosureAndConstructor() {
        assertScript '''
            class C {
                static String m(String a, String b, String... rest) {
                    "a=$a b=$b rest=${rest.toList()}"
                }
                final String value
                C(String a, String b = a, int n = 1) { value = "$a $b $n" }
            }
            def direct = C.&m.curry('A')
            assert direct('B') == 'a=A b=B rest=[]'
            assert direct('B', 'C', 'D') == 'a=A b=B rest=[C, D]'

            def ctor = C.&new.curry('A')
            assert ctor().value == 'A A 1'
            assert ctor('B').value == 'A B 1'
            assert ctor('B', 4).value == 'A B 4'

            def inc = { b -> b + 1 }
            def twice = { a -> a * 2 }
            def composed = (inc >> twice).curry(10)

            class Box {}
            try {
                Box.metaClass.foo = direct
                assert new Box().foo('B') == 'a=A b=B rest=[]'
                assert new Box().foo('B', 'C', 'D') == 'a=A b=B rest=[C, D]'

                Box.metaClass.make = ctor
                assert new Box().make().value == 'A A 1'
                assert new Box().make('B').value == 'A B 1'
                assert new Box().make('B', 4).value == 'A B 4'

                Box.metaClass.calc = composed
                assert new Box().calc() == 22

                def rows = (int[][]).&new.curry(2)
                assert rows(3).length == 2
                assert rows(3)[0].length == 3
                Box.metaClass.grid = rows
                assert new Box().grid(3).length == 2
                assert new Box().grid(3)[0].length == 3
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Box)
            }
        '''
    }

    // GROOVY-3351
    @Test
    void testDehydratedCurriedMethodClosureCanBeRegistered() {
        assertScript '''
            class C {
                static String m(String a, String b = a) { "$a $b" }
            }
            class Box {}
            try {
                Box.metaClass.foo = C.&m.curry('A').dehydrate()
                assert Box.metaClass.respondsTo(new Box(), 'foo')
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Box)
            }
        '''
    }
}
