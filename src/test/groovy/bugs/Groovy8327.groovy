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
package groovy.bugs

import groovy.test.NotYetImplemented
import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy8327 {

    @Test
    void testCallStaticMethodInThisConstructor() {
        assertScript '''
            class A {
                static String f = '123'
                static String g() { 'abc' }
                A() {
                    this(g() + getF())
                }
                A(a) { assert a == 'abc123' }
            }

            new A()
        '''
    }

    @Test
    void testCallStaticMethodInSuperConstructor() {
        assertScript '''
            class A {
                A(a) { assert a == 'abc123' }
            }

            class B extends A {
                static String f = '123'
                static String g() { 'abc' }
                B() {
                    super(g() + getF())
                }
            }

            new B()
        '''
    }

    @Test
    void testCallSuperStaticMethodInSuperConstructor() {
        assertScript '''
            class A {
                static String f = '123'
                static String g() { 'abc' }
                A(a) { assert a == 'abc123' }
            }

            class B extends A {
                B() {
                    super(g() + getF())
                }
            }

            new B()
        '''
    }

    // GROOVY-8327:

    @Test
    void testCallStaticMethodInClosureParamOfThisConstructor() {
        assertScript '''
            class A {
                static String f = '123'
                static String g() { 'abc' }
                A() {
                    this({ -> g() + getF()})
                }
                A(a) { assert a() == 'abc123' }
            }

            new A()
        '''
    }

    @Test
    void testCallStaticMethodInClosureParamOfSuperConstructor() {
        assertScript '''
            class A {
                A(a) { assert a() == 'abc123' }
            }

            class B extends A {
                static String f = '123'
                static String g() { 'abc' }
                B() {
                    super({ -> g() + getF() })
                }
            }

            new B()
        '''
    }

    @Test
    void testCallSuperStaticMethodInClosureParamOfSuperConstructor() {
        assertScript '''
            class A {
                static String f = '123'
                static String g() { 'abc' }
                A(a) { assert a() == 'abc123' }
            }

            class B extends A {
                B() {
                    super({ -> g() + getF() })
                }
            }

            new B()
        '''
    }

    // GROOVY-9591:

    @Test
    void testTapExpressionAsSpecialConstructorArgument1() {
        assertScript '''
            @groovy.transform.ToString
            class A {
                A(x) {}
                def b
            }
            class C {
                C() {
                    this(new A(null).tap { b = 42 })
                }
                C(x) {
                    assert x.toString() == 'A(42)'
                }
            }
            new C()
        '''
    }

    @NotYetImplemented @Test
    void testTapExpressionAsSpecialConstructorArgument2() {
        assertScript '''
            @groovy.transform.ToString
            class A {
                A(x) {}
                def b
                void init() { b = 42 }
            }
            class C {
                C() {
                    this(new A(null).tap { init() })
                }
                C(x) {
                    assert x.toString() == 'A(42)'
                }
            }
            new C()
        '''
    }

    @Test
    void testWithExpressionAsSpecialConstructorArgument1() {
        assertScript '''
            @groovy.transform.ToString
            class A {
                A(x) {}
                def b
            }
            class C {
                C() {
                    this(new A(null).with { b = 42; return it })
                }
                C(x) {
                    assert x.toString() == 'A(42)'
                }
            }
            new C()
        '''
    }

    @NotYetImplemented @Test
    void testWithExpressionAsSpecialConstructorArgument2() {
        assertScript '''
            @groovy.transform.ToString
            class A {
                A(x) {}
                def b
                void init() { b = 42 }
            }
            class C {
                C() {
                    this(new A(null).with { init(); return it })
                }
                C(x) {
                    assert x.toString() == 'A(42)'
                }
            }
            new C()
        '''
    }
}
