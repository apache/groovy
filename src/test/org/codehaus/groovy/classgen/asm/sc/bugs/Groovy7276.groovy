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

import groovy.test.NotYetImplemented
import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy7276 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldGoThroughPrivateBridgeMethod1() {
        ['i', 'i++'].each {
            assertScript """
                class Foo {
                    private int i = 1
                    int m() { new String().with { $it } }
                }
                assert new Foo().m() == 1
            """
        }
    }

    void testShouldGoThroughPrivateBridgeMethod2() {
        ['i', 'i++'].each { // GROOVY-7304
            assertScript """
                class Foo {
                    private int i = 1
                    int m() { new String().with { $it } }
                }
                class Bar extends Foo {
                }
                assert new Bar().m() == 1
            """
        }
    }

    void testShouldGoThroughPrivateBridgeMethod3() {
        ['++i', 'i+=1', 'i=i+1'].each {
            assertScript """
                class Foo {
                    private int i = 1
                    int m() { new String().with { $it } }
                }
                assert new Foo().m() == 2
            """
        }
    }

    // GROOVY-7304
    void testShouldGoThroughPrivateBridgeMethod4() {
        ['++i', 'i+=1', 'i=i+1'].each {
            assertScript """
                class Foo {
                    private int i = 1
                    int m() { new String().with { $it } }
                }
                class Bar extends Foo {
                }
                assert new Bar().m() == 2
            """
        }
    }

    void testShouldGoThroughPrivateBridgeMethod5() {
        assertScript '''
            class Foo {
                private int i = 1
                private int pvI() { return i }
                int m() { new String().with { pvI() } }
            }
            assert new Foo().m() == 1
        '''
    }

    void testShouldGoThroughPrivateBridgeMethod6() {
        assertScript '''
            class Foo {
                private int i = 1
                private int pvI() { return i }
                int m() { new String().with { pvI() } }
            }
            class Bar extends Foo {
            }
            assert new Bar().m() == 1
        '''
    }

    void testPrivateAccessInInnerClass() {
        assertScript '''
            class Outer {
                private static class Inner {
                    private Set<String> variablesToCheck = []
                    private void checkAssertions(String name) {
                        Runnable r = {
                            def candidates = variablesToCheck.findAll { it == name }
                        }
                        r.run()
                    }
                }
                static void test() {
                    new Inner().checkAssertions('name')
                }
            }
            Outer.test()
        '''
    }
}
