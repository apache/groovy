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
package org.codehaus.groovy.transform.traitx

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy_6697 {

    @Test
    void testShouldAllowTraitSuperPropertyNotation() {
        assertScript '''
            trait A {
                String foo
            }

            class C implements A {
                void setBar(String foo) {
                    // This fails with "Caught: groovy.lang.MissingPropertyException: No such property: super for class: A"
                    A.super.foo = foo

                    // This work
                    //A.super.setFoo(foo)
                }

                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.bar = 'bar'
            assert c.yell() == 'BAR!!'
        '''
    }

    @Test
    void testShouldAllowTraitSuperPropertyNotationWithAmbiguousCall() {
        assertScript '''
            trait A {
                String foo
            }

            trait B {
                String foo
            }

            class C implements A,B {
                void setBar(String foo) {
                    A.super.foo = foo+'Bar'
                }
                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.foo = 'foo'
            c.bar = 'bar'
            assert c.yell() == 'FOO!!'
        '''
    }

    @Test
    void testShouldAllowTraitSuperPropertyNotationWithAmbiguousCall2() {
        assertScript '''
            trait A {
                String foo
            }

            trait B {
                String foo
            }

            class C implements A,B {
                void setBar(String foo) {
                    B.super.foo = foo+'Bar'
                }
                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.foo = 'foo'
            c.bar = 'bar'
            assert c.yell() == 'BARBAR!!'
        '''
    }

    @Test
    void testShouldAllowTraitSuperSetterNotation() {
        assertScript '''
            trait A {
                String foo
            }

            class C implements A {
                void setBar(String foo) {
                    A.super.setFoo(foo)
                }
                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.bar = 'bar'
            assert c.yell() == 'BAR!!'
        '''
    }

    @Test
    void testShouldAllowTraitSuperSetterNotationWithAmbiguousCall() {
        assertScript '''
            trait A {
                String foo
            }

            trait B {
                String foo
            }

            class C implements A,B {
                void setBar(String foo) {
                    A.super.setFoo(foo+'Bar')
                }
                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.foo = 'foo'
            c.bar = 'bar'
            assert c.yell() == 'FOO!!'
        '''
    }

    @Test
    void testShouldAllowTraitSuperSetterNotationWithAmbiguousCall2() {
        assertScript '''
            trait A {
                String foo
            }

            trait B {
                String foo
            }

            class C implements A,B {
                void setBar(String foo) {
                    B.super.setFoo(foo+'Bar')
                }
                def yell() {
                    foo.toUpperCase() + "!!"
                }
            }

            def c = new C()
            c.foo = 'foo'
            c.bar = 'bar'
            assert c.yell() == 'BARBAR!!'
        '''
    }
}
