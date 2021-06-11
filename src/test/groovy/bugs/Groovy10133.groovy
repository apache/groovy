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

final class Groovy10133 {

    @Test
    void testGetterVersusIsser1() {
        ['boolean', 'Boolean'].each { bool ->
            assertScript """
                class C {
                    $bool isX() { false }
                    $bool getX() { true }

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
            """
        }
    }

    @Test
    void testGetterVersusIsser2() {
        ['boolean', 'Boolean'].each { bool ->
            assertScript """
                class C {
                    $bool x = false
                    $bool getX() { true }

                    void test1() {
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
            """
        }
    }

    @Test
    void testGetterVersusIsser3() {
        // GROOVY-9382: no "getX" if "isX" declared for boolean
        [/*'boolean',*/ 'Boolean'].each { bool ->
            assertScript """
                class C {
                    $bool x = true
                    $bool isX() { false }

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
            """
        }
    }

    @Test
    void testGetterVersusIsser4() {
        ['boolean', 'Boolean'].each { bool ->
            assertScript """
                class C {
                    $bool x = false
                }

                class D extends C {
                    $bool getX() { true }
                    // TODO: warning for no "isX" override

                    void test() {
                        assert x
                        assert this.x
                        assert !super.x
                    }
                }

                new D().test()
                assert new D().x
            """
        }
    }

    @Test
    void testGetterVersusIsser5() {
        ['boolean', 'Boolean'].each { bool ->
            assertScript """
                class C {
                    $bool x = true
                }

                class D extends C {
                    $bool isX() { false }
                    // TODO: warning for no "getX" override

                    void test() {
                        assert x
                        assert this.x
                        assert super.x
                    }
                }

                new D().test()
                assert new D().x
            """
        }
    }
}
