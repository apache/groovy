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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy8964 {

    @Test
    void testInstanceVarargMethodNotMaskedByStaticMethodWithSameNumberOfArgs() {
        assertScript '''
            class C {
                def m(String... args) {
                    'vararg'
                }
                static m(List<String> args, File workDirectory, Appendable out, Appendable err) {
                    'multi'
                }
                def test() {
                    m('a', 'b', 'c', 'd')
                }
            }

            assert new C().test() == 'vararg'
        '''
    }

    @Test // GROOVY-9737
    void testInstanceMethodNotMaskedByStaticMethodWithSameNumberOfArgs1() {
        assertScript '''
            abstract class A {
                static void m(Integer i) {}
                protected void m(String s) {}
            }

            @groovy.transform.CompileStatic
            class C extends A {
                void test() {
                    m('') // ClassCastException: class java.lang.Class cannot be cast to class A
                }
            }

            new C().test()
        '''
    }

    @Test // GROOVY-9737
    void testInstanceMethodNotMaskedByStaticMethodWithSameNumberOfArgs2() {
        assertScript '''
            import groovy.bugs.Groovy8964.A
            @groovy.transform.CompileStatic
            class C extends A {
                void test() {
                    m('') // VerifyError: Bad access to protected data in invokevirtual
                }
            }

            new C().test()
        '''
    }

    static abstract class A {
        static void m(Integer i) {}
        protected void m(String s) {}
    }
}
