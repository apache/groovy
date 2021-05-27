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

final class Groovy5239 {

    static class PublicStatic {
        static who() { 'PublicStatic' }
    }

    @Test @groovy.test.NotYetImplemented
    void testStaticImportVersusDelegateMethod() {
        assertScript '''
            import static groovy.bugs.Groovy5239.PublicStatic.who

            class C {
                def who() {
                    'C'
                }
            }

            new C().with {
                assert 'C' == who()
            }
        '''
    }

    @Test
    void testStaticImportVersusOuterClassMethod1() {
        assertScript '''
            import static groovy.bugs.Groovy5239.PublicStatic.who

            class C {
                def who() {
                    'C'
                }
                void test() {
                    assert 'C' == who()
                    new D().test()
                }

                class D {
                    void test() {
                        assert 'C' == who() // resolves to static import
                    }
                }
            }

            new C().test()
        '''
    }

    @Test
    void testStaticImportVersusOuterClassMethod2() {
        assertScript '''
            import static groovy.bugs.Groovy5239.PublicStatic.who

            class C {
                def who() {
                    'C'
                }
            }

            class D extends C {
                void test() {
                    assert 'C' == who()
                    new E().test()
                }

                class E {
                    void test() {
                        assert 'C' == who() // resolves to static import
                    }
                }
            }

            new D().test()
        '''
    }
}
