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

import groovy.test.GroovyTestCase

class Groovy5272Bug extends GroovyTestCase {
    /**
     * In Groovy-5272, there are chances that the following test fails.
     */
    void testShouldNeverFail() {
        10.times {
            assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface InterfaceB extends InterfaceA {
                String FOO="Foo B";
            }

            // Fails randomly
            assert InterfaceA.FOO!=InterfaceB.FOO
            '''
        }
    }

    void testShouldNeverFail2() {
        10.times {
            assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface AnotherInterface extends InterfaceA {
                String FOO="Foo B";
            }

            // Fails randomly
            assert InterfaceA.FOO!=AnotherInterface.FOO
            '''
        }
    }

    void testResolvingAmbiguousStaticFieldShouldAlwaysReturnTheSameValue() {
        10.times {
        assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface InterfaceB extends InterfaceA {
                String FOO="Foo B";
            }
            public interface InterfaceC extends InterfaceA {
                String FOO="Foo C";
            }

            class A implements InterfaceB, InterfaceC {
            }

            assert A.FOO == "Foo C"
            '''
        }
    }
    
    void testResolveConstantInSuperInterfaceWithExpando() {
        assertScript '''
            ExpandoMetaClass.enableGlobally()
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            assert Bar.FOO == 'FOO'
            ExpandoMetaClass.disableGlobally()
        '''
    }

    void testResolveConstantInSuperInterfaceWithoutExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            assert Bar.FOO == 'FOO'
        '''
    }

    void testResolveConstantInClassWithSuperInterfaceWithoutExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            class Baz implements Bar {}
            assert Baz.FOO == 'FOO'
        '''
    }

    void testResolveConstantInClassWithSuperInterfaceWithExpando() {
        assertScript '''
            ExpandoMetaClass.enableGlobally()
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            class Baz implements Bar {}
            assert Baz.FOO == 'FOO'
            ExpandoMetaClass.disableGlobally()
        '''
    }

}
