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
import org.junit.jupiter.api.Test

final class Groovy7325Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    @Test
    void testGenericIdentityWithClosure() {
        assertScript '''
            static <T> T itself(T self) { self }

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                assert node.rightExpression.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            Integer i = itself(2)

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                assert node.rightExpression.getNodeMetaData(INFERRED_TYPE) == CLOSURE_TYPE
            })
            Closure c = itself {'foo'}
        '''
    }

    @Test
    void testShouldNotThrowIllegalAccessToProtectedData() {
        shouldFailWithMessages '''
            class C {
                private final Set<String> history = []
                Set<String> getHistory() {
                    (Set<String>) history.clone()
                }
            }

            def set = new C().history
        ''',
        'Cannot access method: clone() of class: java.lang.Object'
    }

    // GROOVY-12568
    @Test
    void testProtectedMethodOfAnotherPackageThroughReceiverOfSuperclass() {
        assertScript '''package p
            class A {
                protected String im() { 'im' }
                protected String getQp() { 'qp' }
                protected void setQs(String s) { }
            }
            true
        '''
        shouldFailWithMessages '''package q
            class B extends p.A {
                static String test(p.A a) {
                    a.im()
                }
            }
        ''',
        'Cannot access method: im() of class: p.A'

        shouldFailWithMessages '''package q
            class B extends p.A {
                static String test(p.A a) {
                    a.qp
                }
            }
        ''',
        'Cannot access method: getQp() of class: p.A'

        shouldFailWithMessages '''package q
            class B extends p.A {
                static void test(p.A a) {
                    a.qs = 'v'
                }
            }
        ''',
        'Cannot access method: setQs(java.lang.String) of class: p.A'

        shouldFailWithMessages '''package q
            class B extends p.A {
                static java.util.function.Supplier<String> test(p.A a) {
                    a::im
                }
            }
        ''',
        'Cannot access method: im of class: p.A'

        shouldFailWithMessages '''package q
            class B extends p.A {
                static java.util.function.Function<p.A, String> test() {
                    p.A::im
                }
            }
        ''',
        'Cannot access method: im of class: p.A'
    }

    // GROOVY-12568
    @Test
    void testProtectedMembersOfAnotherPackageThroughReceiverOfSubclass() {
        assertScript '''package p
            class A {
                protected String im() { 'im' }
                protected static String sm() { 'sm' }
                protected String getQp() { 'qp' }
                protected String f = 'f'
            }
            true
        '''
        assertScript '''package q
            import java.util.function.*

            class B extends p.A {
                List test(B b, p.A a) {
                    Supplier<String> bound = this::im
                    Function<B, String> unbound = B::im
                    a.@f = 'w' // field of a receiver of the superclass: written through the meta-class
                    [im(), this.im(), super.im(), b.im(), sm(), p.A.sm(), qp, b.qp, bound.get(), unbound.apply(b), a.@f]
                }
            }
            assert new B().test(new B(), new B()) == ['im', 'im', 'im', 'im', 'sm', 'sm', 'qp', 'qp', 'im', 'im', 'w']
        '''
    }
}
