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

import gls.CompilableTestSupport

final class InterfaceTest extends CompilableTestSupport {

    void testGenericsInInterfaceMembers() {
        // control
        shouldCompile '''
            interface I {
                def <T>                      T m1(T x)
                def <U extends CharSequence> U m2(U x)
                def <V, W>                   V m3(W x)
                def <N extends Number>    void m4(   )
            }
        '''
        // erroneous
        shouldNotCompile 'interface I { def <?> m(x) }'
        shouldNotCompile 'interface I { def <? extends CharSequence> m(x) }'
    }

    // GROOVY-5106
    void testReImplementsInterface1() {
        def err = shouldFail '''
            interface I<T> {}
            interface J<T> extends I<T> {}
            class X implements I<String>, J<Number> {}
        '''
        assert err.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> and I<java.lang.Number>')
    }

    // GROOVY-5106
    void testReImplementsInterface2() {
        def err = shouldFail '''
            interface I<T> {}
            class X implements I<Number> {}
            class Y extends X implements I<String> {}
        '''
        assert err.contains('The interface I cannot be implemented more than once with different arguments: I<java.lang.String> and I<java.lang.Number>')
    }

    // GROOVY-10060
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
}
