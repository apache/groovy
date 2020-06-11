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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy7242 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testWriteTraitPropertyFromTraitClosure() {
        assertScript '''
            trait T {
                void p() {
                    [1].each { x = it }
                }
                int x
            }

            class C implements T {
            }

            def c = new C()
            c.p()
            assert c.x == 1
        '''
    }

    void testCallTraitMethodFromTraitClosure() {
        assertScript '''
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
    }

    void testCallTraitMethodFromTraitClosure_ImplicitParameter() {
        assertScript '''
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
    void testCallPrivateTraitMethodFromTraitClosure() {
        assertScript '''
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
    void testCallTraitMethodFromTraitClosureInMapConstructor() {
        assertScript '''
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

    // GROOVY-9586
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
}
