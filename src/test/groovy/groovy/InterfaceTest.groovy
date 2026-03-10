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

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class InterfaceTest {

    @Test
    void testGenericsInInterfaceMembers1() {
        assertScript '''
            interface I {
                def <T>                      T m1(T x)
                def <U extends CharSequence> U m2(U x)
                def <V, W>                   V m3(W x)
                def <N extends Number>    void m4(   )
            }

            print 'works'
        '''
    }

    @Test
    void testGenericsInInterfaceMembers2() {
        shouldFail '''
            interface I {
                def <?> m(x)
            }
        '''
    }

    @Test
    void testGenericsInInterfaceMembers3() {
        shouldFail '''
            interface I {
                def <? extends CharSequence> m(x)
            }
        '''
    }

    // GROOVY-5106
    @Test
    void testReImplementsInterface1() {
        def err = shouldFail '''
            interface I<T> {}
            interface J<T> extends I<T> {}
            class X implements I<String>, J<Number> {}
        '''
        assert err.message.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> (via X) and I<java.lang.Number> (via J)')
    }

    // GROOVY-5106
    @Test
    void testReImplementsInterface2() {
        def err = shouldFail '''
            interface I<T> {}
            class X implements I<Number> {}
            class Y extends X implements I<String> {}
        '''
        assert err.message.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> (via Y) and I<java.lang.Number> (via X)')
    }

    // GROOVY-11707
    @Test
    void testReImplementsInterface3() {
        assertScript '''
            abstract class A implements Comparable {
            }
            abstract class B extends A implements Comparable {
            }

            print 'works'
        '''
    }

    // GROOVY-11736
    @Test
    void testReImplementsInterface4() {
        def err = shouldFail '''
            abstract class A implements Comparable<Object> {
            }
            abstract class B extends A implements Comparable {
            }
        '''
        assert err.message.contains('The interface Comparable cannot be implemented more than once with different arguments: java.lang.Comparable (via B) and java.lang.Comparable<java.lang.Object> (via A)')
    }

    // GROOVY-11803
    @Test
    void testDefaultInterfaceMethod() {
        assertScript '''
            interface Foo {
                default int barSize() {
                    return bar.size()
                }
                String getBar()
            }
            class Baz implements Foo {
                final String bar = 'BAR'
            }

            assert new Baz().barSize() == 3
        '''
    }

    // GROOVY-11803
    @ParameterizedTest
    @ValueSource(strings=['default','private','static'])
    void testDefaultInterfaceMethod2(String kind) {
        assertScript """
            interface Foo {
                default int barSize() {
                    return bar.size()
                }
                $kind String getBar() {
                    return 'fizzbuzz'
                }
            }
            class Baz implements Foo {
            }

            assert new Baz().barSize() == 8
        """
    }

    // GROOVY-11548
    @ParameterizedTest
    @ValueSource(strings=['def','final','public'])
    void testDefaultInterfaceMethod3(String spec) {
        assertScript """
            interface A {
                default m() { 'A' }
            }
            class B {
                $spec m() { 'B' }
            }
            class C extends B implements A {
            }

            assert new C().m() == 'B'
        """
    }

    // GROOVY-10060
    @Test
    void testPrivateInterfaceMethod() {
        assertScript '''
            interface Foo {
                default foo() { Foo.this.hello('Foo#foo') }
                @groovy.transform.CompileStatic
                default baz() { hello('Foo#baz') }
                private hello(where) { "hello from $where"}
            }
            class Parent {
                public bar() {
                    hello 'Parent#bar'
                }
                private hello(where) { "howdy from $where"}
            }
            class Impl1 extends Parent implements Foo {
                def baz() { 'hi from Impl1#baz' }
            }
            class Impl2 extends Parent implements Foo {
            }

            def impl1 = new Impl1()
            assert impl1.baz() == 'hi from Impl1#baz'
            assert impl1.bar() == 'howdy from Parent#bar'
            assert impl1.foo() == 'hello from Foo#foo'
            def impl2 = new Impl2()
            assert impl2.baz() == 'hello from Foo#baz'
            assert impl2.bar() == 'howdy from Parent#bar'
            assert impl2.foo() == 'hello from Foo#foo'
        '''
    }

    // GROOVY-11237
    @Test
    void testPublicStaticInterfaceMethod() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail
            interface Foo {
                static hello(where) { "hello $where" }
                static String BAR = 'bar'
                       String BAZ = 'baz' // implicit static
            }

            assert Foo.hello('world') == 'hello world'
            assert Foo.BAR == 'bar'
            assert Foo.BAZ == 'baz'

            shouldFail(MissingMethodException) {
                Foo.getBAR()
            }
            shouldFail(MissingMethodException) {
                Foo.getBAZ()
            }
        '''
    }

    @Test
    void testPublicStaticInterfaceConstant1() {
        String interfaces = '''
            interface A {
                String FOO = 'A'
            }
            interface B {
                String FOO = 'B'
            }
        '''
        assertScript interfaces + '''
            class C implements A, B {
                def m() {
                    FOO
                }
            }
            assert new C().m() == 'B'
        '''
        assertScript interfaces + '''
            class C implements B, A {
                def m() {
                    FOO
                }
            }
            assert new C().m() == 'B'
        '''
    }

    @Test
    void testPublicStaticInterfaceConstant2() {
        String interfaces = '''
            interface A {
                String FOO = 'A'
            }
            interface B {
                String FOO = 'B'
            }
            interface X extends B { }
        '''
        assertScript interfaces + '''
            class C implements A, X {
                def m() {
                    FOO
                }
            }
            assert new C().m() == 'B'
        '''
        assertScript interfaces + '''
            class C implements X, A {
                def m() {
                    FOO
                }
            }
            assert new C().m() == 'B'
        '''
    }

    // GROOVY-5272
    @RepeatedTest(10)
    void testPublicStaticInterfaceConstant3() {
        assertScript '''
            interface A {
                String FOO = 'A'
            }
            interface B extends A {
                String FOO = 'B'
            }

            assert A.FOO != B.FOO // was non-deterministic
        '''
    }

    // GROOVY-5272
    @RepeatedTest(10)
    void testPublicStaticInterfaceConstant4() {
        assertScript '''
            interface B {
                String FOO = 'B'
            }
            interface A extends B {
                String FOO = 'A'
            }

            assert A.FOO != B.FOO // was non-deterministic
        '''
    }

    // GROOVY-5272
    @RepeatedTest(10)
    void testPublicStaticInterfaceConstant5() {
        assertScript '''
            interface A {
                String FOO = 'A'
            }
            interface B extends A {
                String FOO = 'B'
            }
            interface C extends A {
                String FOO = 'C'
            }
            class X implements B, C {
            }

            assert X.FOO == 'C'
        '''
    }

    @Test
    void testConstantInSuperInterfaceNoExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo {
            }

            assert Bar.FOO == 'FOO'
        '''
    }

    @Test
    void testConstantInSuperInterfaceYesExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo {
            }

            ExpandoMetaClass.enableGlobally()
            try {
                assert Bar.FOO == 'FOO'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
        '''
    }

    @Test
    void testConstantInSuperSuperInterfaceNoExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo {
            }
            class Baz implements Bar {
            }

            assert Baz.FOO == 'FOO'
        '''
    }

    @Test
    void testConstantInSuperSuperInterfaceYesExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo {
            }
            class Baz implements Bar {
            }

            ExpandoMetaClass.enableGlobally()
            try {
                assert Baz.FOO == 'FOO'
            } finally {
                ExpandoMetaClass.disableGlobally()
            }
        '''
    }

    // GROOVY-11758, GROOVY-11830
    @ParameterizedTest
    @ValueSource(strings=['protected final','protected','@PackageScope','private'])
    void testSuperClassAndInterfaceMethod(String spec) {
        def err = shouldFail """import groovy.transform.*
            interface A {
                def m()
            }
            class B {
                $spec def m() { 'B' }
            }
            class C extends B implements A {
            }
        """
        spec = spec.split()[0]
        if (spec == '@PackageScope') spec = 'package-private'
        assert err.message =~ /$spec method m\(\) from B cannot shadow the public method in A/
    }

    // GROOVY-11753
    @Test
    void testSuperClassCovariantOfParameterizedInterface() {
        assertScript '''
            class A extends B {
            }
            class B implements C<String> {
                static class NestMate {
                }
                @Override
                void p(String s) {
                    print(s)
                }
            }
            interface C<T> {
                void p(T t)
            }

            new A().p("")
        '''
    }
}
