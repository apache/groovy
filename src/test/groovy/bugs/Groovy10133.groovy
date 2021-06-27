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
import static groovy.test.GroovyAssert.shouldFail

final class Groovy10133 {

    @Test
    void testGetterVersusIsser1() {
        assertScript '''
            class C {
                boolean isX() { true }
                boolean getX() { false }

                void test1() {
                    assert x
                    assert this.x
                }
            }

            new C().test1()
            assert new C().x

            class D extends C {
                void test2() {
                    assert x
                    assert this.x
                    assert !super.x // GROOVY-6097
                }
            }

            new D().test1()
            new D().test2()
            assert new D().x
        '''
    }

    @Test
    void testGetterVersusIsser2() {
        assertScript '''
            class C {
                boolean x = false
                boolean getX() { true }
                // no isX() if getX() declared

                void test1() {
                    // direct
                    assert !x
                    assert !this.x
                }
            }

            new C().test1()
            assert new C().x

            class D extends C {
                void test2() {
                    assert x
                    assert this.x
                    assert super.x
                }
            }

            new D().test1()
            new D().test2()
            assert new D().x
        '''
    }

    @Test
    void testGetterVersusIsser3() {
        assertScript '''
            class C {
                boolean x = false
                boolean isX() { true }
                // no getX() if isX() declared

                void test1() {
                    // direct
                    assert !x
                    assert !this.x
                }
            }

            new C().test1()
            assert new C().x

            class D extends C {
                void test2() {
                    assert x
                    assert this.x
                    try {
                        assert super.x // GROOVY-6097
                        assert false : 'remove catch'
                    } catch (MissingMethodException mme) {
                    }
                }
            }

            new D().test1()
            new D().test2()
            assert new D().x
        '''
    }

    @Test
    void testGetterVersusIsser4() {
        assertScript '''
            class C {
                boolean x = true
            }

            class D extends C {
                boolean getX() { false }
                // TODO: warning for no "isX" override

                void test() {
                    assert x
                    assert this.x
                    assert super.x
                }
            }

            new D().test()
            assert new D().x
        '''
    }

    @Test
    void testGetterVersusIsser5() {
        assertScript '''
            class C {
                boolean x = false
            }

            class D extends C {
                boolean isX() { true }
                // TODO: warning for no "getX" override

                void test() {
                    assert x
                    assert this.x
                    assert !super.x
                }
            }

            new D().test()
            assert new D().x
        '''
    }

    @Test
    void testGetterVersusIsser6() {
        assertScript '''
            class C {
                boolean isX() { true }
                boolean getX() { false }

                @groovy.transform.CompileStatic
                void test1() {
                    assert x
                    assert this.x
                }
            }

            class D extends C {
                @groovy.transform.CompileStatic
                void test2() {
                    assert x
                    assert this.x
                    assert !super.x // GROOVY-6097
                }
            }

            @groovy.transform.CompileStatic
            void test() {
                new C().test1()
                assert new C().x

                new D().test1()
                new D().test2()
                assert new D().x
            }
            test()
        '''
    }

    @Test
    void testGetterVersusIsser7() {
        assertScript '''
            class C {
                Boolean isX() { Boolean.FALSE }
                Boolean getX() { Boolean.TRUE }

                void test1() {
                    assert x
                    assert this.x
                }
            }

            new C().test1()
            assert new C().x

            class D extends C {
                void test2() {
                    assert x
                    assert this.x
                    assert super.x
                }
            }

            new D().test1()
            new D().test2()
            assert new D().x
        '''
    }

    @Test // GROOVY-9382
    void testGetterVersusIsser8() {
        shouldFail MissingPropertyException, '''
            class C {
                Boolean isX() { null }

                void test() {
                    x
                }
            }
            new C().test()
        '''

        def err = shouldFail '''
            class C {
                Boolean isX() { null }

                @groovy.transform.TypeChecked
                void test() {
                    x
                }
            }
        '''
        assert err =~ /The variable \[x\] is undeclared/

        err = shouldFail '''
            class C {
                Boolean isX() { null }

                @groovy.transform.TypeChecked
                void test() {
                    new C().x
                }
            }
        '''
        assert err =~ /No such property: x for class: C/
    }
}
