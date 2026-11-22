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

    // GROOVY-12612
    @Test
    void testInvokeTraitMethodOnExplicitThisFromTraitClosure() {
        assertScript shell, '''
            class D {
                String getName() { 'D' }
                String run(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<String> block) {
                    block.resolveStrategy = Closure.DELEGATE_FIRST
                    block.delegate = this
                    block.call()
                }
            }
            trait S {
                String getSup() { 'S' }
            }
            trait T extends S {
                String getName() { 'T' }
                String greet(String s, int i) { getName() + s + i }
                private String p() { 'p' }
                String m1() { Closure<String> c = { -> this.getName() }; c() }
                String m2() { Closure<String> c = { -> this.greet('x', 1) }; c() }
                String m3() { Closure<String> c = { -> this.p() }; c() }
                String m4() { Closure<String> c = { -> this.getSup() }; c() }
                String m5() { Closure<String> c = { -> Closure<String> d = { -> this.getName() }; d() }; c() }
                String m6() { 's'.with { this.getName() + it } }
                String m7() { Closure<String> c = { -> this?.getName() }; c() }
                String m8() { new D().run { -> this.getName() } } // "this" is never the delegate
            }
            class C implements T {
                @Override
                String getName() { 'C' }
            }

            def c = new C()
            assert c.m1() == 'C'
            assert c.m2() == 'Cx1'
            assert c.m3() == 'p'
            assert c.m4() == 'S'
            assert c.m5() == 'C'
            assert c.m6() == 'Cs'
            assert c.m7() == 'C'
            assert c.m8() == 'C'
        '''
    }

    // GROOVY-12612
    @Test
    void testInvokeTraitMethodOnExplicitThisFromTraitLambda() {
        assertScript shell, '''import java.util.function.Supplier
            trait T {
                String getName() { 'T' }
                String m1() { Supplier<String> s = () -> this.getName(); s.get() }
                String m2() { Closure<String> c = () -> this.getName(); c() }
                String m3() { Closure<String> c = { -> Supplier<String> s = () -> this.getName(); s.get() }; c() }
                String m4() { Supplier<String> s = () -> { Closure<String> c = { -> this.getName() }; c() }; s.get() }
            }
            class C implements T {
                @Override
                String getName() { 'C' }
            }

            def c = new C()
            assert c.m1() == 'C'
            assert c.m2() == 'C'
            assert c.m3() == 'C'
            assert c.m4() == 'C'
        '''
    }

    // GROOVY-12612
    @Test
    void testInvokeMethodOnExplicitThisFromStaticTraitMethodClosure() {
        assertScript shell, '''
            trait T {
                static String s() { 's' }
                static String m1() { Closure<String> c = { -> this.s() }; c() }
                static String m2() { Closure<String> c = { -> this.getSimpleName() }; c() }
                static String m3() { this.getSimpleName() }
            }
            class C implements T {
            }

            assert C.m1() == 's'
            assert C.m2() == 'C'
            assert C.m3() == 'C'
        '''
    }

    // GROOVY-12612
    @Test
    void testInvokeTraitMethodOnExplicitThisFromTraitClosureOrLambda() {
        ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'].each { mode ->
            assertScript """import java.util.function.Supplier
                $mode
                trait S {
                    String getSup() { 'S' }
                }
                $mode
                trait T extends S {
                    String getName() { 'T' }
                    String greet(String s, int i) { getName() + s + i }
                    String m1() { Closure<String> c = { -> this.getName() }; c() }
                    String m2() { Closure<String> c = { -> this.greet('x', 1) }; c() }
                    String m3() { Closure<String> c = { -> this.getSup() }; c() }
                    String m4() { 's'.with { this.getName() + it } }
                    String m5() { Supplier<String> s = () -> this.getName(); s.get() }
                    String m6() { Closure<String> c = () -> this.getName(); c() }
                    static String m7() { Closure<String> c = { -> this.getSimpleName() }; c() }
                }
                class C implements T {
                    @Override
                    String getName() { 'C' }
                }

                def c = new C()
                assert c.m1() == 'C'
                assert c.m2() == 'Cx1'
                assert c.m3() == 'S'
                assert c.m4() == 'Cs'
                assert c.m5() == 'C'
                assert c.m6() == 'C'
                assert C.m7() == 'C'
            """
        }
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

    // GROOVY-12602
    @Test
    void testInvokeClassMethodFromTraitLambdaInStaticMethod() {
        assertScript shell, '''import java.util.function.Supplier
            trait T {
                static String m1() { Supplier<String> s = () -> getSimpleName(); s.get() }
                static String m2() { Supplier<String> s = () -> simpleName; s.get() }
                static String m3() { Supplier<String> s = () -> { Closure<String> c = { -> getSimpleName() }; c() }; s.get() }
                static String m4() { Supplier<String> s = () -> { Supplier<String> t = () -> getSimpleName(); t.get() }; s.get() }
                static String m5() { Closure<String> c = { -> getSimpleName() }; c() }
            }
            class C implements T {
            }

            assert C.m1() == 'C'
            assert C.m2() == 'C'
            assert C.m3() == 'C'
            assert C.m4() == 'C'
            assert C.m5() == 'C'
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
                    String who(Class c, int i) { 'delegate' }
                    String tag(T t, int i) { 'delegate' }
                }
                $mode
                trait T {
                    static String who(int i) { this.simpleName }
                    private String tag(int i) { getClass().simpleName }
                    def m() { new Dg().with { who(1) } }
                    def n() { new Dg().with { this.who(1) } }
                    def p() { new Dg().with { tag(1) } }
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

    // GROOVY-12563
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

    // GROOVY-12563
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

    // GROOVY-12563
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

    // GROOVY-12563
    @Test
    void testStaticClosureFieldCalledAsMethod() {
        String script = '''
            import java.util.function.Supplier
            trait T {
                static Closure<String> f = { 'f' }
                static Closure<String> g = { String s -> s * 2 }
                static Closure<String> field = { f() + '!' }
                Closure<String> instanceField = { f() + '?' }
                static String staticMethod() { f() + g('x') }
                static String explicitThis() { this.f() }
                String instanceMethod() { f() + g('y') }
                static String inClosure() { def c = { def inner = { f() }; inner() }; c() }
                String inInstanceClosure() { def c = { f() }; c() }
                static String inLambda() { Supplier<String> s = () -> f(); s.get() }
            }
            class C implements T {
            }

            // a static closure field of a trait is the field of the class the trait is applied to
            assert C.staticMethod() == 'fxx'
            assert C.explicitThis() == 'f'
            assert new C().instanceMethod() == 'fyy'
            assert C.inClosure() == 'f'
            assert new C().inInstanceClosure() == 'f'
            assert C.inLambda() == 'f'
            assert C.field() == 'f!'
            assert new C().instanceField() == 'f?'
        '''
        assertScript shell, script
        assertScript script.replace('trait T', '@groovy.transform.TypeChecked trait T')
        assertScript script
    }

    // GROOVY-12563
    @Test
    void testExplicitThisInDynamicTraitClosure() {
        assertScript '''
            class Base {
                String base() { 'base' }
            }
            class Other {
                String m() { 'other' }
            }
            @groovy.transform.SelfType(Base)
            trait T {
                String name = 'n'
                String m() { 'T.m ' + name }
                String over() { 'T.over' }
                static String sm(String s) { 'T.sm' + s }
                def out(o) { o }
                def explicitThis() { def c = { [this.m(), this.over(), this.base(), this.name, this.getClass().simpleName, this.toString()] }; c() }
                def implicitThis() { def c = { [m(), over(), base(), name] }; c() }
                def nested() { def c = { def d = { [this.m(), this.over(), this, out(this), this.out(this)] }; d() }; c() }
                def delegateFirst() { def c = { [this.m(), m()] }; c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = new Other(); c() }
                static staticThis() { def c = { [this.sm('!'), this.getSimpleName(), this, this.toString()] }; c() }
                static staticNested() { def c = { def d = { [this.sm('?'), this.getSimpleName(), this] }; d() }; c() }
            }
            class C extends Base implements T {
                String over() { 'C.over' }
                String toString() { 'C!' }
            }

            def c = new C()
            // explicit this in a closure of a trait method is the instance or class the trait is applied to, as this alone and an implicit this are
            assert c.explicitThis() == ['T.m n', 'C.over', 'base', 'n', 'C', 'C!']
            assert c.implicitThis() == ['T.m n', 'C.over', 'base', 'n']
            def (m, over, self, out, explicitOut) = c.nested()
            assert m == 'T.m n' && over == 'C.over' && self.is(c) && out.is(c) && explicitOut.is(c)
            // the delegate does not take part in an explicit this
            assert c.delegateFirst() == ['T.m n', 'other']
            assert C.staticThis() == ['T.sm!', 'C', C, 'class C']
            assert C.staticNested() == ['T.sm?', 'C', C]
        '''
    }

    // GROOVY-12563
    @Test
    void testStaticBooleanPropertyAndExplicitAccessorsInTraitClosure() {
        String script = '''
            import java.util.function.Supplier
            trait S {
                static Closure<String> s = { 's' }
            }
            trait T extends S {
                static boolean flag
                static Closure<String> aB = { 'aB' }
                static boolean isReady() { flag }
                static Closure<String> getG() { { -> 'g' + flag } }
                static boolean inClosure() { def c = { flag }; c() }
                static boolean inNestedClosure() { def c = { def d = { flag }; d() }; c() }
                static boolean inLambda() { Supplier<Boolean> s = () -> flag; s.get() }
                static boolean accessors() { def c = { isFlag() && getFlag() && isReady() }; c() }
                static String callG() { g() }
                static String callGInClosure() { def c = { g() }; c() }
                static String callAB() { def c = { aB() }; c() }
                static String callS() { def c = { s() }; c() }
                boolean instanceFlag() { def c = { flag }; c() }
            }
            class C implements T {
            }
            class D implements T {
            }

            // a static boolean property of a trait, which has an isser, is that of the class the trait is applied to
            C.flag = true
            assert C.inClosure() && C.inNestedClosure() && C.inLambda() && C.accessors() && new C().instanceFlag()
            assert !D.inClosure() && !D.inNestedClosure() && !D.inLambda() && !D.accessors() && !new D().instanceFlag()
            // a static closure property with an explicit getter only, or declared in a super trait
            assert C.callG() == 'gtrue' && C.callGInClosure() == 'gtrue' && D.callGInClosure() == 'gfalse'
            assert C.callAB() == 'aB'
            C.s = { 'c' }
            assert C.callS() == 'c' && D.callS() == 's'
        '''
        assertScript shell, script
        assertScript script.replace('trait T', '@groovy.transform.TypeChecked trait T')
        assertScript script
    }
}
