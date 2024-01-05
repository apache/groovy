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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class TraitWithClosureOrLambda {

    @Test
    void testTraitClosure1() {
        assertScript '''
            @groovy.transform.CompileStatic
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

        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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

        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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

        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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

        assertScript '''
            @groovy.transform.CompileStatic
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

    // GROOVY-7456
    @Test
    void testInvokePrivateTraitMethodFromTraitClosure() {
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            class Foo {
                Closure bar
            }
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            class C {
                def m(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=C) Closure<?> block) {
                    block.setResolveStrategy(Closure.OWNER_ONLY)
                    block.setDelegate(this)
                    return block.call()
                }
                def x() { 'C' }
            }
            @groovy.transform.CompileStatic
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
        assertScript '''
            class C {
                def m(@DelegatesTo(strategy=Closure.OWNER_ONLY, type='Void') Closure<?> block) {
                    block.setResolveStrategy(Closure.OWNER_ONLY)
                    block.setDelegate(null)
                    return block.call()
                }
                def x() { 'C' }
            }
            @groovy.transform.CompileStatic
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
            assertScript """
                class C {
                    String s
                }
                @groovy.transform.CompileStatic
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

        def err = shouldFail '''
            @groovy.transform.CompileStatic
            trait T {
                def obj = new Object().tap {
                    config(it)
                }
                static void config(String s) {
                }
            }
        '''
        assert err.message.contains('Cannot find matching method T$Trait$Helper#config')
    }

    // GROOVY-11265
    @Test
    void testTraitFunctionalInterfaceLambda1() {
        assertScript '''import java.util.function.Function
            @groovy.transform.CompileStatic
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
        assertScript '''import java.util.function.Function
            @groovy.transform.CompileStatic
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
}
