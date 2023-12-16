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
package gls.invocation

import org.codehaus.groovy.control.CompilationFailedException
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ConstructorDelegationTest {

    @Test
    void testThisCallWithParameter() {
        assertScript '''
            class C {
                C() {
                    this("bar")
                }
                C(String x) {
                    foo = x
                }
                def foo
            }

            def c = new C()
            assert c.foo == "bar"
        '''
    }

    @Test
    void testThisCallWithoutParameter() {
        assertScript '''
            class C {
                C() {
                    foo = "bar"
                }
                C(String x) {
                    this()
                    foo = x
                }
                def foo
            }

            def c = new C("foo")
            assert c.foo == "foo"
        '''
    }

    @Test
    void testThisConstructorCallNotFirst() {
        shouldFail CompilationFailedException, '''
            class C {
                C() {
                    println 'dummy statement'
                    this(19)
                }
                C(int b) {
                    println 'other statement'
                }
            }

            null
        '''
    }

    @Test
    void testSuperConstructorCallNotFirst() {
        shouldFail CompilationFailedException, '''
            class C {
                public C() {
                    println 'dummy statement'
                    super()
                }
            }

            null
        '''
    }

    // GROOVY-9857
    @Test
    void testImplicitSuperConstructorCallChecks() {
        String base = '''
            abstract class A {
                A(boolean b) {
                }
            }
        '''

        def err = shouldFail CompilationFailedException, base + '''
            class C extends A {
                C() {
                }
            }
        '''
        assert err =~ /Implicit super constructor A\(\) is undefined. Must explicitly invoke another constructor./

        err = shouldFail CompilationFailedException, base + '''
            class C extends A {
            }
        '''
        assert err =~ /Implicit super constructor A\(\) is undefined for generated constructor. Must define an explicit constructor./
    }

    // GROOVY-3128
    @Test
    void testConstructorDelegationWithThisOrSuperInArgs() {
        // all 4 cases below were compiling earlier but giving VerifyError at runtime

        shouldFail CompilationFailedException, '''
            class MyClosure1 extends Closure {
                MyClosure1() {
                    super(this)
                }
                void run() { println 'running' }
            }
        '''

        shouldFail CompilationFailedException, '''
            class MyClosure2 extends Closure {
                MyClosure2() {
                    super(super)
                }
                void run() { println 'running' }
            }
        '''

        shouldFail CompilationFailedException, '''
            class MyClosure3 extends Closure {
                MyClosure3() {
                    this(this)
                }
                MyClosure3(owner) {
                }
                void run() { println 'running' }
            }
        '''

        shouldFail CompilationFailedException, '''
            class MyClosure4 extends Closure {
                MyClosure4() {
                    this(super)
                }
                MyClosure4(owner) {
                }
                void run() { println 'running' }
            }
        '''
    }

    // GROOVY-6285
    @Test
    void testDelegateDisambiguation() {
        assertScript '''
            class E1 extends Exception {
                E1() {
                    super()
                    info += "called E1();"
                }
                E1(String s) {
                    super(s)
                    info += "called E1(String) with $s;"
                }
                E1(Throwable t) {
                    super(t)
                    info += "called E1(Throwable) with $t;"
                }
                public String info = ""
            }

            class E2 extends E1 {
                E2() {
                    super()
                    info += "called E2();"
                }
                E2(String s) {
                    super(s)
                    info += "called E2(String) with $s;"
                }
            }

            assert new E1(               ).info == 'called E1();'
            assert new E1((Throwable)null).info == 'called E1(Throwable) with null;'

            assert new E2(    ).info == 'called E1();called E2();'
            assert new E2(null).info == 'called E1(String) with null;called E2(String) with null;'
        '''
    }

    // GROOVY-6618
    @Test
    void testVariadicConstructorCall() {
        assertScript '''
            class C {
                C() {
                    this("foo", 1)
                }
                C(String s, Integer[] array) {
                    info = array
                }
                public info
            }

            assert new C().info == [1]
        '''

        assertScript '''
            class C {
                C() {
                    this("foo", null)
                }
                C(String s, Integer[] array) {
                    info = array
                }
                public info
            }

            assert new C().info == null
        '''

        assertScript '''
            class C {
                C() {
                    this("foo", 1, 2, 3)
                }
                C(String s, Integer[] array) {
                    info = array
                }
                public info
            }

            assert new C().info == [1,2,3]
        '''

        assertScript '''
            class C {
                C() {
                    this("foo", new Integer[]{1,2,3})
                }
                C(String s, Integer[] array) {
                    info = array
                }
                public info
            }

            assert new C().info == [1,2,3]
        '''

        assertScript '''
            class C {
                C() {
                    this("foo")
                }
                C(String s, Integer[] array) {
                    info = array
                }
                public info
            }

            assert new C().info == []
        '''
    }
}
