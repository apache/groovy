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

import groovy.test.NotYetImplemented
import org.junit.Test

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
        assert err.message.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> and I<java.lang.Number>')
    }

    // GROOVY-5106
    @Test
    void testReImplementsInterface2() {
        def err = shouldFail '''
            interface I<T> {}
            class X implements I<Number> {}
            class Y extends X implements I<String> {}
        '''
        assert err.message.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> and I<java.lang.Number>')
    }

    // GROOVY-11707
    @NotYetImplemented @Test
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
        assert err.message.contains('The interface Comparable cannot be implemented more than once with different arguments: java.lang.Comparable and java.lang.Comparable<java.lang.Object>')
    }

    // GROOVY-11803
    @Test
    void testDefaultInterfaceMethod1() {
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
    @Test
    void testDefaultInterfaceMethod2() {
        assertScript '''
            interface Foo {
                default int barSize() {
                    return bar.size()
                }
                default String getBar() {
                    return 'fizzbuzz'
                }
            }
            class Baz implements Foo {
            }

            assert new Baz().barSize() == 8
        '''
    }

    // GROOVY-11548
    @Test
    void testDefaultInterfaceMethod3() {
        assertScript '''
            interface A {
                default m() { 'A' }
            }
            class B {
                public final m() { 'B' }
            }
            class C extends B implements A {
            }

            assert new C().m() == 'B'
        '''
    }

    // GROOVY-11758, GROOVY-11830
    @Test
    void testSuperClassAndInterfaceMethod() {
        for (spec in ['protected final','protected','@PackageScope','private']) {
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
            assert err =~ /$spec method m\(\) from B cannot shadow the public method in A/
        }
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
