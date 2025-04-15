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

import groovy.transform.stc.ConstructorsSTCTest

/**
 * Unit tests for static compilation: constructors.
 */
class StaticCompileConstructorsTest extends ConstructorsSTCTest implements StaticCompilationTestSupport {

    void testMapConstructorError() {
        assertScript '''
            class C {
                static void test() {
                    new Person(name:"First")
                    first(new Person(name:"First"))
                }
                static Person first(Person p) {
                    p
                }
            }
            @groovy.transform.Canonical
            class Person {
                String name
            }

            C.test()
        '''
    }

    void testPrivateConstructorFromClosure() {
        assertScript '''
            class C {
                String s
                private C(String s) {
                    this.s = s
                }
                static C make(String s) {
                    def cl = { new C(s) }
                    cl()
                }
            }
            assert C.make('pls').s == 'pls'
        '''
    }

    void testPrivateConstructorFromStaticInnerClass() {
        assertScript '''
            class Foo {
                String s
                private Foo(String s) {
                    this.s = s
                }
                static class Bar {
                    static Foo makeFoo(String s) { new Foo(s) }
                }
            }
            assert Foo.Bar.makeFoo('pls').s == 'pls'
        '''
        String bar = astTrees['Foo$Bar'][1]
        assert bar.contains('NEW Foo')
        assert bar.contains('INVOKESPECIAL Foo.<init> (Ljava/lang/String;)V')
    }

    void testPrivateConstructorFromAnonymousInnerClass() {
        assertScript '''
            class Foo {
                String s
                private Foo(String s) {
                    this.s = s
                }
                static Foo makeFoo(String s) {
                    new Object() {
                        Foo makeFoo(String x) {
                            new Foo(x)
                        }
                    }.makeFoo(s)
                }
            }
            assert Foo.makeFoo('pls').s == 'pls'
        '''
    }

    // GROOVY-11119, GROOVY-11451
    @Override
    void testMapStyleConstructorWithOverloadedSetterName() {
        super.testMapStyleConstructorWithOverloadedSetterName()
        String  asserts = astTrees.find{ it.key != 'C' }.value[1]
        assert  asserts.contains('INVOKEVIRTUAL C.setP (Ljava/lang/String;)V')
        assert  asserts.contains('INVOKEVIRTUAL C.setP (Ljava/util/regex/Pattern;)V')
        assert !asserts.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setGroovyObjectProperty')
    }
}
