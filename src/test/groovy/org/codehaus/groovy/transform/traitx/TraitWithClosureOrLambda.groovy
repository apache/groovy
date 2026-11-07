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
package org.codehaus.groovy.transform.traitx

import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class TraitWithClosureOrLambda {

    private final GroovyShell shell = GroovyShell.withConfig {
        ast CompileStatic
    }

    @Test
    void testTraitClosure1() {
        assertScript shell, '''
            trait T {
                int f(int x) {
                    def impl = { -> 2*x }
                    impl.call()
                }
            }
            class C implements T {
            }

            assert new C().f(4) == 8
        '''

        assertScript shell, '''
            trait T {
                int f(int x) {
                    def impl = () -> { 2*x }
                    impl.call()
                }
            }
            class C implements T {
            }

            assert new C().f(4) == 8
        '''
    }

    @Test
    void testTraitClosure2() {
        assertScript shell, '''
            trait T {
                int x
                int f() {
                    def impl = { -> 2*x }
                    impl.call()
                }
            }
            class C implements T {
            }

            assert new C(x: 4).f() == 8
        '''

        assertScript shell, '''
            trait T {
                int x
                int f() {
                    def impl = () -> { 2*x }
                    impl.call()
                }
            }
            class C implements T {
            }

            assert new C(x: 4).f() == 8
        '''
    }

    @Test
    void testTraitClosure3() {
        assertScript shell, '''
            trait T {
                final String greeting = 'Welcome!'
                Closure greeter() {
                    return { -> greeting }
                }
            }
            class C implements T {
            }

            def c = new C()
            def greeter = c.greeter()
            assert greeter.thisObject.is(c)
            assert greeter.call() == 'Welcome!'
        '''

        assertScript shell, '''
            trait T {
                final String greeting = 'Welcome!'
                Closure greeter() {
                    return () -> greeting
                }
            }
            class C implements T {
            }

            def c = new C()
            def greeter = c.greeter()
            assert greeter.thisObject.is(c)
            assert greeter.call() == 'Welcome!'
        '''
    }

    // GROOVY-7242
    @Test
    void testWriteTraitPropertyFromTraitClosure() {
        assertScript shell, '''
            trait T {
                int x
                void p() {
                    [1].each { x = it }
                }
            }
            class C implements T {
            }

            def c = new C()
            c.p()
            assert c.x == 1
        '''
    }

    // GROOVY-7242
    @Test
    void testInvokeTraitMethodsFromTraitClosure() {
        assertScript shell, '''
            trait T {
                def f() {
                    ['a'].collect { String s -> g(s) }
                }
                String g(String s) {
                    s.toUpperCase()
                }
            }
            class C implements T {
            }

            def c = new C()
            assert c.f() == ['A']
        '''

        assertScript shell, '''
            trait T {
                def f() {
                    ['a'].collect { g(it) }
                }
                String g(String s) {
                    s.toUpperCase()
                }
            }
            class C implements T {
            }

            def c = new C()
            assert c.f() == ['A']
        '''
    }

    // GROOVY-7456,  GROOVY-7797
    @Test
    void testInvokePrivateTraitMethodFromTraitClosure() {
        assertScript shell, '''
            trait T {
                def f() {
                    ['a'].collect { String s -> g(s) }
                }
                private String g(String s) {
                    s.toUpperCase()
                }
            }
            class C implements T {
            }

            def c = new C()
            assert c.f() == ['A']
        '''
    }

    // GROOVY-7512
    @Test
    void testInvokeTraitMethodFromTraitClosureInMapConstructor() {
        assertScript shell, '''
            class Foo {
                Closure bar
            }
            trait T {
                Foo getFoo() {
                    new Foo(bar: { ->
                        baz 'xyz' // ClassCastException: java.lang.Class cannot be cast to T
                    })
                }
                def baz(text) {
                    text
                }
            }
            class C implements T {
            }

            Foo foo = new C().foo
            assert foo.bar.call() == 'xyz'
        '''
    }

    // GROOVY-8127
    @Test
    void testCoercedClosureField() {
        assertScript shell, '''
            trait T {
                public final Runnable foo = { println new Date() } as Runnable
                void doRun() { foo.run() }
            }
            class C implements T {
            }

            new C().doRun()
        '''
    }

    // GROOVY-8127
    @Test
    void testCoercedClosureFieldWritesOtherField() {
        assertScript shell, '''
            trait T {
                String result = ''
                public final Runnable bar = { result = 'changeme' } as Runnable
                void doRun() { bar.run() }
            }
            class C implements T {
            }

            def c = new C(); c.doRun()
            assert c.result == 'changeme'
        '''
    }

    // GROOVY-9586
    @Test
    void testDelegateVsOwnerMethodFromTraitClosure1() {
        assertScript shell, '''
            class C {
                def m(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=C) Closure<?> block) {
                    block.setResolveStrategy(Closure.OWNER_ONLY)
                    block.setDelegate(this)
                    return block.call()
                }
                def x() { 'C' }
            }
            trait T {
                def test() {
                    new C().m { -> x() } // "x" must come from delegate
                }
                def x() { 'T' }
            }
            class U implements T {
            }

            assert new U().test() == 'C'
        '''
    }

    // GROOVY-9586
    @Test
    void testDelegateVsOwnerMethodFromTraitClosure2() {
        assertScript shell, '''
            class C {
                def m(@DelegatesTo(strategy=Closure.OWNER_ONLY, type='Void') Closure<?> block) {
                    block.setResolveStrategy(Closure.OWNER_ONLY)
                    block.setDelegate(null)
                    return block.call()
                }
                def x() { 'C' }
            }
            trait T {
                def test() {
                    new C().m { -> x() } // "x" must come from owner
                }
                def x() { 'T' }
            }
            class U implements T {
            }

            assert new U().test() == 'T'
        '''
    }

    // GROOVY-10106
    @Test
    void testCallStaticOrPrivateMethodInTraitFieldInitializer() {
        ['private', 'static', 'private static'].each { mods ->
            assertScript shell, """
                class C {
                    String s
                }
                trait T {
                    final C c = new C().tap {
                        config(it)
                    }
                    $mods void config(C c) {
                        c.s = 'x'
                    }
                }
                class U implements T {
                }

                def c = new U().c
                assert c.s == 'x'
            """
        }

        def err = shouldFail shell, '''
            trait T {
                def obj = new Object().tap {
                    config(it)
                }
                static void config(String s) {
                }
            }
        '''
        assert err.message.contains('Cannot find matching method ')
    }

    // GROOVY-11265
    @Test
    void testTraitFunctionalInterfaceLambda1() {
        assertScript shell, '''import java.util.function.Function
            trait T {
                abstract int f(int x)
                Function<Integer,Integer> times(int multiplicand) {
                    return (multiplier) -> { multiplier * multiplicand }
                }
            }
            class C implements T {
                @Override
                int f(int x) {
                    times(2).apply(x)
                }
            }

            assert new C().f(8) == 16
        '''
    }

    // GROOVY-11265
    @Test
    void testTraitFunctionalInterfaceLambda2() {
        assertScript shell, '''import java.util.function.Function
            trait T {
                final int xx = 1
                abstract int f(int x)
                Function<Integer,Integer> times(int multiplicand) {
                    return (multiplier) -> (multiplier + xx) * multiplicand;
                }
            }
            class C implements T {
                @Override
                int f(int x) {
                    times(2).apply(x)
                }
            }

            assert new C().f(8) == 18
        '''
    }

    // GROOVY-11267
    @Test
    void testInvokeTraitStaticMethodFromTraitClosure() {
        assertScript shell, '''
            trait T {
                @groovy.transform.CompileDynamic
                static one() {
                    def me = this
                    two('good') + ' ' +
                    'bad '.with { me.two(it) } +
                    'ugly'.with {    two(it) }
                }
                static two(String s) {
                    three(s)
                }
                static three(String s) {
                    return s
                }
            }
            class C implements T {
            }

            assert C.one() == 'good bad ugly'
        '''
    }

    // GROOVY-12602
    @Test
    void testInvokeTraitMethodFromTraitLambda() {
        assertScript shell, '''import java.util.function.*
            trait T {
                String getName() { 'T' }
                String m1() { Supplier<String> s = () -> getName(); s.get() }
                String m2() { Supplier<String> s = () -> name; s.get() }
                String m3() { Function<String,String> f = (x) -> getName() + x; f.apply('!') }
                String m4() { Supplier<String> s = () -> { Supplier<String> t = () -> getName(); t.get() }; s.get() }
                String m5() { Supplier<String> s = () -> { Closure<String> c = { -> getName() }; c() }; s.get() }
                String m6() { Closure<String> c = { -> Supplier<String> s = () -> getName(); s.get() }; c() }
                String m7() { Closure<String> c = { -> this.name }; Supplier<String> s = () -> getName(); s.get() + c() }
                String m8() { Supplier<String> s = this::getName; s.get() }
            }
            class C implements T {
                @Override
                String getName() { 'C' }
            }

            def c = new C()
            assert c.m1() == 'C'
            assert c.m2() == 'C'
            assert c.m3() == 'C!'
            assert c.m4() == 'C'
            assert c.m5() == 'C'
            assert c.m6() == 'C'
            assert c.m7() == 'CC'
            assert c.m8() == 'C'
        '''
    }

    // GROOVY-12602
    @Test
    void testInvokeSuperTraitMethodFromTraitLambda() {
        assertScript shell, '''import java.util.function.Supplier
            trait T {
                String getName() { 'T' }
            }
            trait U extends T {
                String m1() { Supplier<String> s = () -> getName(); s.get() }
                String m2() { Supplier<String> s = () -> name; s.get() }
                String m3() { Supplier<String> s = () -> this.name; s.get() }
                String m4() { Closure<String> c = { -> getName() }; c() }
            }
            class C implements U {
            }

            def c = new C()
            assert c.m1() == 'T'
            assert c.m2() == 'T'
            assert c.m3() == 'T'
            assert c.m4() == 'T'
        '''
    }

    // GROOVY-12602
    @Test
    void testInvokeTraitMethodFromTraitLambdaInFieldInitializer() {
        assertScript shell, '''import java.util.function.Supplier
            trait T {
                final Supplier<String> greeter = () -> 'Hello ' + getName()
                String getName() { 'T' }
            }
            class C implements T {
            }

            assert new C().greeter.get() == 'Hello T'
        '''
    }

    // GROOVY-12602
    @Test
    void testInvokeTraitStaticOrPrivateMethodFromTraitLambda() {
        assertScript shell, '''import java.util.function.Supplier
            trait T {
                private String p() { 'p' }
                static String s() { 's' }
                String m1() { Supplier<String> f = () -> p(); f.get() }
                String m2() { Supplier<String> f = () -> s(); f.get() }
                static String m3() { Supplier<String> f = () -> s(); f.get() }
            }
            class C implements T {
            }

            assert new C().m1() == 'p'
            assert new C().m2() == 's'
            assert C.m3() == 's'
        '''
    }

    // GROOVY-12602
    @Test
    void testInvokeTraitMethodFromTraitLambdaOrClosure() {
        ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'].each { mode ->
            assertScript """import java.util.function.Supplier
                $mode
                trait T {
                    String getName() { 'T' }
                    String m1() { Supplier<String> s = () -> getName(); s.get() }
                    String m2() { Supplier<String> s = () -> name; s.get() }
                    String m3() { Closure<String> c = { -> getName() }; c() }
                    String m4() { Closure<String> c = () -> getName(); c() }
                    String m5() { new StringBuilder('x').with((sb) -> sb.toString() + getName()) }
                }
                class C implements T {
                }

                def c = new C()
                assert c.m1() == 'T'
                assert c.m2() == 'T'
                assert c.m3() == 'T'
                assert c.m4() == 'T'
                assert c.m5() == 'xT'
            """
        }
    }
}
