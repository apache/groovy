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

    // GROOVY-12624
    @Test
    void testInvokeTraitStaticMethodFromDelegateClosureInInstanceMethod() {
        for (mode in ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']) {
            assertScript """
                $mode
                trait T {
                    static String who() { this.simpleName }
                    def m() { new Object().with { who() } }
                    def n() { 'x'.with { this.who() } }
                    def p() { [1].collect { who() + it } }
                    def q() { java.util.function.Supplier<String> s = () -> who(); s.get() }
                    @groovy.transform.Virtual
                    static String vwho() { this.simpleName }
                    def r() { new Object().with { vwho() } }
                }
                class C implements T {
                }
                class D implements T {
                }

                assert new C().m() == 'C'
                assert new D().m() == 'D'
                assert new C().n() == 'C'
                assert new D().n() == 'D'
                assert new C().p() == ['C1']
                assert new D().p() == ['D1']
                assert new C().q() == 'C'
                assert new D().q() == 'D'
                assert new C().r() == 'C'
                assert new D().r() == 'D'
            """
        }
    }

    // GROOVY-12624
    @Test
    void testInvokeSuperTraitStaticMethodFromDelegateClosureInInstanceMethod() {
        for (mode in ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']) {
            assertScript """
                $mode
                trait S {
                    static String who() { this.simpleName }
                }
                $mode
                trait T extends S {
                    def m() { new Object().with { who() } }
                }
                class C implements T {
                }
                class D implements T {
                }

                assert new C().m() == 'C'
                assert new D().m() == 'D'
            """
        }
    }

    // GROOVY-12624
    @Test
    void testReadTraitStaticFieldFromDelegateClosureInInstanceMethod() {
        for (mode in ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']) {
            assertScript """
                $mode
                trait T {
                    static String label = 'T'
                    public static String tag = 'T'
                    def m() { new Object().with { label } }
                    def n() { new Object().with { tag } }
                    def p() { new Object().with { label = 'p'; tag = 'p' } }
                }
                class C implements T {
                }
                class D implements T {
                }

                C.label = 'C'
                D.label = 'D'
                assert new C().m() == 'C'
                assert new D().m() == 'D'
                assert new C().n() == 'T'
                assert new D().n() == 'T'
                new C().p()
                assert new C().m() == 'p'
                assert new C().n() == 'p'
                assert new D().m() == 'D'
                assert new D().n() == 'T'
            """
        }
    }

    // GROOVY-12624
    @Test
    void testInvokeTraitMethodFromDelegateClosureNotOnDelegateWithSelfParameter() {
        for (mode in ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']) {
            assertScript """
                class Dg {
                    String who(Class c) { 'delegate' }
                    String tag(T t) { 'delegate' }
                }
                $mode
                trait T {
                    static String who() { this.simpleName }
                    private String tag() { getClass().simpleName }
                    def m() { new Dg().with { who() } }
                    def n() { new Dg().with { this.who() } }
                    def p() { new Dg().with { tag() } }
                }
                class C implements T {
                }
                class D implements T {
                }

                assert new C().m() == 'C'
                assert new D().m() == 'D'
                assert new C().n() == 'C'
                assert new C().p() == 'C'
                assert new D().p() == 'D'
            """
        }
    }
}
