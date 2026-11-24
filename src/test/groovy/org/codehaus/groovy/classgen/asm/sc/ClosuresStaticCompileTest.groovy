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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ClosuresSTCTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation: closures.
 */
final class ClosuresStaticCompileTest extends ClosuresSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5584
    @Test
    void testEachOnMapClosure() {
        assertScript '''
            def test() {
                def result = ""
                [a:1, b:3].each { key, value -> result += "$key$value" }
                assert result == "a1b3"
            }
            test()
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodsInClosureWithoutThisObject() {
        assertScript '''
            class C {
                String toString() { 'C' }
                List<Closure> closures() {
                    [{ -> toString() }, { -> hashCode() }, { -> equals(this) }, { -> getClass() }]
                }
            }

            C c = new C()
            List<Closure> closures = c.closures()
            Closure copy = closures[0].dehydrate()
            assert copy() == copy.toString()
            copy = closures[1].dehydrate()
            assert copy() == copy.hashCode()
            assert closures[2].dehydrate()() == false
            copy = closures[3].dehydrate()
            assert copy() == copy.getClass()

            // without a this object, the owner's method is called
            assert closures[0].rehydrate(null, c, null)() == 'C'
            assert closures[1].rehydrate(null, c, null)() == c.hashCode()
            assert closures[2].rehydrate(null, c, null)() == false
            assert closures[3].rehydrate(null, c, null)() == C

            assert closures[0]() == 'C'
            assert closures[1]() == c.hashCode()
            assert closures[2]() == true
            assert closures[3]() == C
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodsInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class C {
                String toString() { 'C' }
                int hashCode() { 42 }
                boolean equals(Object o) { o instanceof C }
                List<Object> m() {
                    C that = new C()
                    List<Object> result = []
                    'x'.with {
                        Supplier<String> s = () -> toString()
                        Supplier<Integer> h = () -> hashCode()
                        Supplier<Boolean> e = () -> equals(that)
                        Supplier<Class> k = () -> getClass()
                        Supplier<String> n = () -> { { -> toString() }() }
                        result << s.get() << h.get() << e.get() << k.get() << n.get()
                    }
                    result
                }
                static String sm() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }

            assert new C().m() == ['C', 42, true, C, 'C']
            assert C.sm() == 'class C'
        '''
    }

    // GROOVY-12576
    @Test
    void testInheritedObjectMethodInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class C {
                String m() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }
            trait T {
                String m() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }
            class K implements T {
                String toString() { 'K' }
            }

            C c = new C()
            assert c.m() == c.toString()
            assert c.m().startsWith('C@')
            assert new K().m() == 'K'
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodInLambdaInsideDelegateOnlyClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String name
                String toString() { name }
                static <T> T withC(@DelegatesTo(value = C, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new C(name: 'delegate')
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                static <T> T withD(@DelegatesTo(value = D, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                String sameType() {
                    withC { Supplier<String> s = () -> toString(); s.get() }
                }
                String otherType() {
                    withD { Supplier<String> s = () -> toString(); s.get() }
                }
            }

            C c = new C(name: 'this')
            assert c.sameType() == 'this'
            assert c.otherType() == 'this'
        '''
    }

    // GROOVY-12576
    @Test
    void testInheritedObjectMethodInClosureWithoutThisObject() {
        assertScript '''
            class C {
                Closure<String> closure() {
                    return { -> toString() }
                }
            }

            C c = new C()
            Closure<String> closure = c.closure()
            assert closure() == c.toString()

            Closure<String> copy = closure.dehydrate()
            assert copy() == copy.toString()

            // without a this object, the owner's method is called
            assert closure.rehydrate(null, c, null)() == c.toString()
            assert closure.rehydrate('o', 'o', 'o')() == 'o'
        '''
    }

    // GROOVY-12576
    @Test
    void testInstanceExtensionMethodInClosureWithoutThisObject() {
        assertScript '''
            class C {
                String toString() { 'C!' }
                Closure<String> closure() {
                    return { -> inspect() }
                }
            }

            C c = new C()
            Closure<String> closure = c.closure()
            assert closure() == 'C!'

            Closure<String> copy = closure.dehydrate()
            assert copy() == copy.toString()

            // without a this object, the extension method is called on the owner
            assert closure.rehydrate(null, c, null)() == 'C!'
            assert closure.rehydrate('o', 'o', 'o')() == "'o'"
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String toString() { 'C' }
                List<Object> m() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
                static List<Object> sm() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new C().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'C'
                assert result[3].toString().startsWith('<C@')
                assert result[4] == 'C'

                result = C.sm()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'class C'
                assert result[3].toString().startsWith('<java.lang.Class@')
                assert result[4] == 'class C'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bca', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosureInTrait() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            trait T {
                List<Object> m() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
            }
            class K implements T {
                String toString() { 'K' }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new K().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'K'
                assert result[3].toString().startsWith('<K@')
                assert result[4] == 'K'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosureInScript() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }

            String toString() { 'script' }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = []
                new D().with {
                    Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                    Supplier<Object> s2 = () -> sprintf('%s', 'd')
                    Supplier<Object> s3 = () -> inspect()
                    Supplier<Object> s4 = () -> with { it.toString() }
                    result << s1.get() << s2.get() << s3.get() << s4.get()
                }
                assert result == [null, 'd', 'script', 'script']
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideDelegateOnlyClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String toString() { 'C' }
                static <T> T withD(@DelegatesTo(value = D, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                List<Object> m() {
                    List<Object> result = []
                    withD {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
                void uncalled() {
                    withD {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                    }
                }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new C().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'C'
                assert result[3].toString().startsWith('<C@')
                assert result[4] == 'C'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // a method of the delegate of a closure, called in a lambda inside the closure
    @Test
    void testDelegateMethodInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String hi() { 'hi from D' }
                String toString(int n) { 'D' * n }
            }
            class C {
                List<Object> m() {
                    List<Object> result = []
                    new D().with {
                        Supplier<String> s1 = () -> hi()
                        Supplier<String> s2 = () -> toString(2)
                        result << s1.get() << s2.get()
                    }
                    [1, 2, 3].with {
                        Supplier<Integer> s = () -> size()
                        result << s.get()
                    }
                    'abcd'.with {
                        Supplier<Integer> s1 = () -> length()
                        Supplier<String> s2 = () -> toUpperCase()
                        result << s1.get() << s2.get()
                    }
                    42.with {
                        Supplier<String> s = () -> toString(7)
                        result << s.get()
                    }
                    result
                }
            }

            assert new C().m() == ['hi from D', 'DD', 3, 4, 'ABCD', '7']
        '''
    }

    @Test
    void testDelegatePropertyInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String name = 'd'
                int count
            }
            class C {
                String label = 'c'
                List<Object> m() {
                    D d = new D()
                    List<Object> result = []
                    d.with {
                        Supplier<String> s1 = () -> name
                        Supplier<String> s2 = () -> name + label
                        Runnable r = () -> { name = 'e'; count += 2 }
                        result << s1.get() << s2.get()
                        r.run()
                        result << s1.get()
                    }
                    result << d.name << d.count
                    result
                }
            }

            assert new C().m() == ['d', 'dc', 'e', 'e', 2]
        '''
    }

    // the closure's resolve strategy decides, as for a call directly in the closure
    @Test
    void testDelegateMethodInLambdaInsideClosureWithResolveStrategy() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String both() { 'D.both' }
                String onlyD() { 'D.onlyD' }
                String toString() { 'D' }
            }
            class C {
                String both() { 'C.both' }
                String onlyC() { 'C.onlyC' }
                String toString() { 'C' }
                static <T> T delegateOnly(@DelegatesTo(value = D, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                static <T> T delegateFirst(@DelegatesTo(value = D, strategy = Closure.DELEGATE_FIRST) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_FIRST
                    block.call()
                }
                static <T> T ownerFirst(@DelegatesTo(D) Closure<T> block) {
                    block.delegate = new D()
                    block.call()
                }
                List<String> m() {
                    List<String> result = []
                    delegateOnly {
                        Supplier<String> s1 = () -> both()
                        Supplier<String> s2 = () -> onlyD()
                        Supplier<String> s3 = () -> toString()
                        result << s1.get() << s2.get() << s3.get()
                    }
                    delegateFirst {
                        Supplier<String> s1 = () -> both()
                        Supplier<String> s2 = () -> onlyD()
                        Supplier<String> s3 = () -> onlyC()
                        Supplier<String> s4 = () -> toString()
                        result << s1.get() << s2.get() << s3.get() << s4.get()
                    }
                    ownerFirst {
                        Supplier<String> s1 = () -> both()
                        Supplier<String> s2 = () -> onlyD()
                        Supplier<String> s3 = () -> onlyC()
                        Supplier<String> s4 = () -> toString()
                        result << s1.get() << s2.get() << s3.get() << s4.get()
                    }
                    result
                }
            }

            assert new C().m() == ['D.both', 'D.onlyD', 'C',
                                   'D.both', 'D.onlyD', 'C.onlyC', 'C',
                                   'C.both', 'D.onlyD', 'C.onlyC', 'C']
        '''
    }

    // the delegate is read when the lambda is called
    @Test
    void testDelegateSetAfterLambdaInsideClosureIsCreated() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String name
                String hi() { "hi from $name" }
            }
            class C {
                static List<String> twice(@DelegatesTo(D) Closure<Supplier<String>> block) {
                    block.delegate = new D(name: 'first')
                    Supplier<String> s = block.call()
                    List<String> result = [s.get()]
                    block.delegate = new D(name: 'second')
                    result << s.get()
                }
                List<String> m() {
                    twice { (Supplier<String>) (() -> hi() + '/' + name) }
                }
            }

            assert new C().m() == ['hi from first/first', 'hi from second/second']
        '''
    }

    @Test
    void testDelegateMethodInNestedLambdasAndClosures() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String hi() { 'D' }
            }
            class E {
                String ho() { 'E' }
            }
            class C {
                String hey() { 'C' }
                List<String> m() {
                    List<String> result = []
                    new D().with {
                        // lambda in closure in closure
                        Closure<String> c = { -> Supplier<String> s = () -> hi() + hey(); s.get() }
                        result << c()
                        new E().with {
                            Supplier<String> s = () -> hi() + ho() + hey()
                            result << s.get()
                        }
                        // closure in lambda in closure
                        Supplier<String> s1 = () -> { Closure<String> k = { -> hi() + hey() }; k() }
                        result << s1.get()
                        // lambda in lambda in closure
                        Supplier<String> s2 = () -> { Supplier<String> t = () -> hi() + hey(); t.get() }
                        result << s2.get()
                        Supplier<String> s3 = () -> { Supplier<String> t = () -> { Closure<String> k = { -> Supplier<String> u = () -> hi() + hey(); u.get() }; k() }; t.get() }
                        result << s3.get()
                        // closure with its own delegate in lambda in closure
                        Supplier<String> s4 = () -> new E().with { hi() + ho() + hey() }
                        result << s4.get()
                        Supplier<String> s5 = () -> new E().with { Supplier<String> t = () -> hi() + ho() + hey(); t.get() }
                        result << s5.get()
                    }
                    result
                }
            }

            assert new C().m() == ['DC', 'DEC', 'DC', 'DC', 'DC', 'DEC', 'DEC']
        '''
    }

    @Test
    void testDelegateMethodInLambdaInsideClosureInStaticTraitAndScriptCode() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String name = 'd'
                String hi() { 'hi' }
            }
            class C {
                static String sm() {
                    new D().with { Supplier<String> s = () -> hi() + name; s.get() }
                }
                static String sf = new D().with { Supplier<String> s = () -> hi() + name; s.get() }
                String f = new D().with { Supplier<String> s = () -> hi() + name; s.get() }
                String p
                C() {
                    p = new D().with { Supplier<String> s = () -> hi() + name; s.get() }
                }
            }
            trait T {
                String who() { 'T' }
                String tm() {
                    new D().with { Supplier<String> s = () -> hi() + name + who(); s.get() }
                }
                static String tsm() {
                    new D().with { Supplier<String> s = () -> hi() + name; s.get() }
                }
            }
            class U implements T {
            }

            assert C.sm() == 'hid'
            assert C.sf == 'hid'
            assert new C().f == 'hid'
            assert new C().p == 'hid'
            assert new U().tm() == 'hidT'
            assert U.tsm() == 'hid'

            String scriptMethod() {
                new D().with { Supplier<String> s = () -> hi() + name; s.get() }
            }
            assert scriptMethod() == 'hid'
            assert new D().with { Supplier<String> s = () -> hi() + name; s.get() } == 'hid'
        '''
    }

    @Test
    void testDelegateMethodInLambdaInsideRehydratedClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String name
                String hi() { "hi from $name" }
            }
            class C {
                static Closure<String> block(@DelegatesTo(D) Closure<String> block) {
                    block
                }
                Closure<String> viaLambda() {
                    block { -> Supplier<String> s = () -> hi(); s.get() }
                }
                Closure<String> direct() {
                    block { -> hi() }
                }
            }

            C c = new C()
            for (Closure<String> closure : [c.viaLambda(), c.direct()]) {
                closure.delegate = new D(name: 'one')
                assert closure() == 'hi from one'
                assert closure.rehydrate(new D(name: 'two'), c, c)() == 'hi from two'
                assert closure.rehydrate(new D(name: 'three'), null, null)() == 'hi from three'
                groovy.test.GroovyAssert.shouldFail(NullPointerException) {
                    closure.dehydrate()()
                }
            }
        '''
    }

    // GROOVY-12576: a method of Object in a closure or lambda of an inner class is not the outer instance's
    @Test
    void testObjectMethodInClosureOrLambdaOfInnerClass() {
        assertScript '''
            import java.util.function.Supplier

            class Outer {
                @Override String toString() { 'outer' }
                @Override int hashCode() { 11 }
                @Override boolean equals(Object that) { true }
                String hello() { 'hello from outer' }

                class Inner {
                    @Override String toString() { 'inner' }
                    Closure<String> viaToString() { { -> toString() } }
                    Closure<Integer> viaHashCode() { { -> hashCode() } }
                    Closure<Boolean> viaEquals() { { -> equals('other') } }
                    Closure<String> viaOuterMethod() { { -> hello() } }
                    String viaLambda() { Supplier<String> s = () -> toString(); s.get() }
                    String viaLambdaInClosure() {
                        Closure<String> c = { -> Supplier<String> s = () -> toString(); s.get() }
                        c.call()
                    }
                }
                class Plain {
                    Closure<String> viaToString() { { -> toString() } }
                    class Deeper {
                        Closure<String> viaToString() { { -> toString() } }
                    }
                }
            }

            def outer = new Outer()
            def inner = new Outer.Inner(outer)
            assert inner.viaToString().call() == 'inner'
            assert inner.viaHashCode().call() == inner.hashCode()
            assert !inner.viaEquals().call()
            assert inner.viaOuterMethod().call() == 'hello from outer'
            assert inner.viaLambda() == 'inner'
            assert inner.viaLambdaInClosure() == 'inner'

            def plain = new Outer.Plain(outer)
            assert plain.viaToString().call() == plain.toString()
            def deeper = new Outer.Plain.Deeper(plain)
            assert deeper.viaToString().call() == deeper.toString()

            def closure = plain.viaToString()
            assert closure.dehydrate().call().toString().contains('_closure')
            assert closure.rehydrate('context', 'context', 'context').call() == 'context'
            assert closure.rehydrate('delegate', 'owner', null).call() == 'owner'
        '''
    }

    // a serializable lambda does not hold the closure, so the closure's delegate need not be serializable
    @Test
    void testSerializableLambdaInsideClosureDoesNotHoldTheClosure() {
        assertScript '''
            import java.util.function.Supplier

            interface SerializableSupplier extends Supplier<String>, Serializable {}

            class D {
                String hi() { 'hi' }
            }
            class C implements Serializable {
                String m(boolean useDelegate) {
                    new D().with {
                        SerializableSupplier s = () -> useDelegate ? hi() : 'skipped'
                        def bytes = new ByteArrayOutputStream()
                        bytes.withObjectOutputStream { it.writeObject(s) }
                        def copy = new ByteArrayInputStream(bytes.toByteArray()).newObjectInputStream(C.classLoader).readObject()
                        ((SerializableSupplier) copy).get()
                    }
                }
            }

            assert new C().m(false) == 'skipped'
        '''
    }

    // a lambda that uses no name of the delegate keeps its shape, and its owner is the enclosing instance either way
    @Test
    void testLambdaInsideClosureCapturesClosureOnlyWhenNeeded() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String hi() { 'D' }
                String println(String s) { "D.println($s)" }
            }
            class C {
                String hey() { 'C' }
                String toString() { 'C' }
                List<List<Object>> m() {
                    List<List<Object>> result = []
                    new D().with {
                        // no capture
                        List<Object> identities = []
                        for (int i = 0; i < 3; i += 1) {
                            Supplier<String> s = () -> 'x'
                            identities << System.identityHashCode(s)
                        }
                        result << identities.unique()
                        // the enclosing instance only
                        Supplier<List<Object>> s1 = () -> [hey(), hey(), owner, delegate, thisObject, metaClass.theClass.declaredFields*.name]
                        result << s1.get()
                        // the enclosing instance and the closure
                        Supplier<List<Object>> s2 = () -> [hi(), hey(), toString(), owner, delegate, thisObject, metaClass.theClass.declaredFields*.name, println('x')]
                        result << s2.get()
                    }
                    result
                }
            }

            C c = new C()
            def (List<Object> n, List<Object> r1, List<Object> r2) = c.m()
            assert n.size() == 1
            assert r1[0..1] == ['C', 'C'] && r1[2..4].every { it.is(c) }
            assert !((List) r1[5]).contains('$enclosingClosure')
            assert r2[0..2] == ['D', 'C', 'C'] && r2[3..5].every { it.is(c) } && r2[7] == 'D.println(x)'
            assert ((List) r2[6]).contains('$enclosingClosure')
        '''
    }

    // GROOVY-5811
    @Test
    void testClosureExceptionUnwrap() {
        assertScript '''
            @groovy.transform.InheritConstructors
            public class MyException extends Exception {}

            void foo() {
                throw new MyException()
            }

            void bar() {
                boolean caught = false
                try {
                    def cl = { foo() }
                    cl()
                } catch (MyException e) {
                    caught = true
                } finally {
                    assert caught
                }
            }

            bar()
        '''
    }

    // GROOVY-6522
    @Test
    void testShouldCallClosure() {
        assertScript '''
            class Sample {
                Closure formatFirstName, formatLastName

                void doStuff (String fname, String lname, Closure callback) {
                    formatFirstName(fname) { String fnameError, String formattedFname ->
                        formatLastName(lname) { String lnameError, String formattedLname ->
                            String errors = "${fnameError ? "${fnameError}, " : ''}${lnameError ?: ''}"
                            callback(errors, formattedFname, formattedLname)
                        }
                    }
                }
            }

            Closure ffn = { String fname, Closure callback ->
                String firstInitial = fname?.substring(0,1)
                if (!firstInitial)
                    callback('invalid first name', null)
                else
                    callback(null, firstInitial.toLowerCase())
            }

            Closure fln = { String lname, Closure callback ->
                String lastPrefix = lname?.size() > 2 ? lname.substring(0,3) : null
                if (!lastPrefix)
                    callback('invalid last name', null)
                else
                    callback(null, lastPrefix.toLowerCase())
            }

            Sample sample = new Sample(formatFirstName: ffn, formatLastName: fln)

            sample.doStuff('John', 'Doe') { String errors, String formattedFname, String formattedLname ->
                if (errors)
                    println errors
                else
                    println "${formattedFname}.${formattedLname}"
            }
        '''
    }

    @Test
    void testMethodVersusPropertyOnCompoundAssignmentTarget() {
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*

            class Pogo {
                String string = ''
                void setString(String string) {
                    this.string = string
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                node = node.code.statements[0].expression
                assert node instanceof MethodCallExpression

                node = node.arguments.expressions[0].code.statements[0].expression
                assert node instanceof BinaryExpression

                assert node.leftExpression instanceof PropertyExpression
                def target = node.leftExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)

                assert target != null && target.name == 'setString'
            })
            void test(Pogo pogo) {
                pogo.with {
                    string += 'x'
                }
            }

            test(new Pogo())
        '''
    }

    @Test
    void testObjectMethodInLambdaInDehydratedClosure() {
        assertScript '''
            class C {
                @Override String toString() { 'c' }
                String m() {
                    def k = { -> java.util.function.Supplier<String> s = () -> toString(); s.get() }.dehydrate()
                    k.call()
                }
            }
            assert new C().m().contains('_closure')
        '''
    }

    @Test
    void testObjectMethodFollowsRehydratedClosure() {
        for (packed in ['', '@groovy.transform.PackedClosures']) {
            for (body in ['{ -> toString() }', '{ -> java.util.function.Supplier<String> s = () -> toString(); s.get() }']) {
                assertScript """
                    class D { @Override String toString() { 'D' } }
                    class O { @Override String toString() { 'O' } }
                    class T { @Override String toString() { 'T' } }
                    @groovy.transform.CompileStatic
                    $packed
                    class C {
                        @Override String toString() { 'C' }
                        List m() {
                            Closure k = $body
                            [
                                k.call(),
                                k.rehydrate(new D(), new O(), new T()).call(),
                                k.rehydrate(null, new O(), null).call(),
                                k.rehydrate(null, this, null).call(),
                                k.rehydrate(new D(), null, null).call().toString().contains('_closure'),
                                k.rehydrate(null, null, new T()).call().toString().contains('_closure'),
                                k.dehydrate().call().toString().contains('_closure'),
                            ]
                        }
                    }
                    assert new C().m() == ['C', 'T', 'O', 'C', true, true, true]
                """
            }
        }
    }

    @Test
    void testPrintInLambdaInClosure() {
        assertScript '''
            class C {
                def m() {
                    def sw = new StringWriter()
                    def k = { -> java.util.function.Supplier<String> s = () -> sprintf('%s', 'x'); s.get() }.dehydrate()
                    sw << k.call()
                    sw.toString()
                }
            }
            assert new C().m() == 'x'
        '''
    }

    @Test
    void testSerializableLambdaInRehydratedClosure() {
        assertScript '''
            import java.util.function.Supplier
            interface SerializableSupplier extends Supplier<String>, Serializable {}
            class O { @Override String toString() { 'O' } }
            class T { @Override String toString() { 'T' } }
            @groovy.transform.CompileStatic
            class C {
                @Override String toString() { 'C' }
                List m() {
                    Closure<String> k = { -> SerializableSupplier s = () -> toString(); s.get() }
                    [
                        k.call(),
                        k.rehydrate(null, new O(), new T()).call(),
                        k.rehydrate(null, new O(), null).call().toString().contains('lambda'),
                        k.dehydrate().call().toString().contains('lambda'),
                    ]
                }
            }
            assert new C().m() == ['C', 'T', true, true]
        '''
    }

    @Test
    void testSerializableLambdaInClosureKeepsItsForm() {
        assertScript '''
            import java.util.function.Supplier
            interface SerializableSupplier extends Supplier<String>, Serializable {}
            class C implements Serializable {
                @Override String toString() { 'C' }
                def m() {
                    def k = { ->
                        SerializableSupplier s = () -> toString()
                        s
                    }
                    def out = new ByteArrayOutputStream()
                    new ObjectOutputStream(out).withCloseable { it.writeObject(k.call()) }
                    def read = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject()
                    assert !java.lang.reflect.Proxy.isProxyClass(read.getClass())
                    ((Supplier<String>) read).get()
                }
            }
            assert new C().m() == 'C'
        '''
    }
}
