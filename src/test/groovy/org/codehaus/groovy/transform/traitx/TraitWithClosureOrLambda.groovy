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

    // GROOVY-12492
    @Test
    void testOwnerAndThisObjectOfTraitClosure() {
        String script = '''
            trait T {
                String name() { 't' }
                Closure field = { owner }
                def ownerProperty() { def c = { owner }; c() }
                def thisObjectProperty() { def c = { thisObject }; c() }
                def delegateProperty() { def c = { delegate }; c() }
                def ownerGetter() { def c = { getOwner() }; c() }
                def thisObjectGetter() { def c = { getThisObject() }; c() }
                def nested() { def c = { def inner = { thisObject }; inner() }; c() }
                def nestedOwner() { def c = { def inner = { owner }; inner() }; c() }
                String ownerMethod() { def c = { owner.name() }; c() }
                static staticOwner() { def c = { owner }; c() }
            }
            class C implements T {
                String name() { 'c' }
            }

            def c = new C()
            // a closure written in a trait method is rehydrated to the instance the trait is applied to
            [c.ownerProperty(), c.thisObjectProperty(), c.delegateProperty(), c.ownerGetter(), c.thisObjectGetter(), c.nested(), c.field()].each {
                assert it.is(c)
            }
            assert c.nestedOwner() instanceof Closure
            assert c.ownerMethod() == 'c'
            assert C.staticOwner() == C
        '''
        assertScript shell, script
        assertScript script.replace('trait T', '@groovy.transform.TypeChecked trait T')

        assertScript shell, '''
            class Base {
                String base() { 'base' }
            }
            @groovy.transform.SelfType(Base)
            trait T {
                String bases() { def c = { owner.base() + ' ' + thisObject.base() }; c() }
            }
            class C extends Base implements T {
            }

            assert new C().bases() == 'base base'
        '''
    }

    // GROOVY-12492
    @Test
    void testClosureInTraitLambda() {
        String script = '''
            import java.util.function.Supplier
            trait T {
                String name = 'n'
                String helper() { 'h-' + name }
                static String staticHelper() { 'sh' }
                Supplier<List> field = () -> { def c = { [thisObject, owner] }; c() }
                List objects() { Supplier<List> s = () -> { def c = { [thisObject, owner, delegate] }; c() }; s.get() }
                def ownerAlone() { Supplier<Object> s = () -> { def c = { owner }; c() }; s.get() }
                String calls() { Supplier<String> s = () -> { def c = { helper() + ' ' + name }; c() }; s.get() }
                String lambdaCalls() { Supplier<String> s = () -> helper(); s.get() }
                def inClosure() { def outer = { Supplier<Object> s = () -> { def c = { thisObject }; c() }; s.get() }; outer() }
                def inLambda() { Supplier<Object> s = () -> { Supplier<Object> t = () -> { def c = { thisObject }; c() }; t.get() }; s.get() }
                static List staticObjects() { Supplier<List> s = () -> { def c = { [thisObject, staticHelper()] }; c() }; s.get() }
            }
            class C implements T {
            }

            def c = new C()
            // a closure in a lambda gets the instance the trait is applied to as its this object, and the lambda as its owner
            def (thisObject, owner, delegate) = c.objects()
            assert thisObject.is(c) && owner instanceof Closure && delegate.is(owner)
            assert c.ownerAlone() instanceof Closure
            assert c.field.get()[0].is(c)
            assert c.calls() == 'h-n n'
            assert c.lambdaCalls() == 'h-n'
            assert c.inClosure().is(c)
            assert c.inLambda().is(c)
            assert C.staticObjects() == [C, 'sh']
        '''
        assertScript shell, script
        assertScript script.replace('trait T', '@groovy.transform.TypeChecked trait T')
    }

    // GROOVY-12492
    @Test
    void testExplicitThisMethodCallInTraitClosure() {
        String script = '''
            import java.util.function.Supplier
            class Base {
                String base() { 'base' }
            }
            @groovy.transform.SelfType(Base)
            trait T {
                String name = 'n'
                String helper(String s = '') { 'h' + s + name }
                abstract String impl()
                Closure field = { this.helper('-field-') }
                String noArgument() { def c = { this.helper() }; c() }
                String argument() { def c = { this.helper('-') }; c() }
                String implemented() { def c = { this.impl() + this.base() }; c() }
                String objectMethods() { def c = { this.toString() + ' ' + this.getClass().simpleName }; c() }
                String nested() { def c = { def inner = { this.helper('-nested-') }; inner() }; c() }
                String inLambda() { Supplier<String> s = () -> this.helper('-lambda-'); s.get() }
                static String staticMethod() { def c = { this.getSimpleName() }; c() }
            }
            class C extends Base implements T {
                String impl() { 'impl ' }
                String toString() { 'C!' }
            }

            def c = new C()
            // this in a trait closure is the instance the trait is applied to, also as the receiver of a call
            assert c.field() == 'h-field-n'
            assert c.noArgument() == 'hn'
            assert c.argument() == 'h-n'
            assert c.implemented() == 'impl base'
            assert c.objectMethods() == 'C! C'
            assert c.nested() == 'h-nested-n'
            assert c.inLambda() == 'h-lambda-n'
            assert C.staticMethod() == 'C'
        '''
        assertScript shell, script
        assertScript script.replace('trait T', '@groovy.transform.TypeChecked trait T')
        assertScript script
    }
}
