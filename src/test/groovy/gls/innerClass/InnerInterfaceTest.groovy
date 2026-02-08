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
package gls.innerClass

import groovy.test.GroovyTestCase

/**
 * Tests on inner interface usage
 */
class InnerInterfaceTest extends GroovyTestCase {

    void testStaticInnerInterfaceInAClass() {
        assertScript '''
            class Foo4422V1 {
                static class Bar {
                    def bar(){}
                }
                static interface Baz {
                    String TEST = ""
                    def baz()
                }
            }

            class BazImpl implements Foo4422V1.Baz {
                def baz(){}
            }
            assert Foo4422V1.Bar != null
            assert Foo4422V1.Baz != null
            assert Foo4422V1.Bar.getMethod('bar') != null
            assert Foo4422V1.Baz.getMethod('baz') != null
            assert Foo4422V1.Baz.getField('TEST') != null
            assert BazImpl != null
        '''
    }

    void testStaticInnerInterfaceInAnInterface() {
        assertScript '''
            interface Foo4422V2 {
                static interface Baz {}
            }

            assert Foo4422V2.Baz != null
        '''
    }

    void testNonStaticInnerInterfaceInAClass() {
        assertScript '''
            class Foo4422V3 {
                class Bar {}
                interface Baz {}
            }

            assert Foo4422V3.Bar != null
            assert Foo4422V3.Baz != null
        '''
    }

    // GROOVY-5989
    void testReferenceToInterfaceNestedInterface() {
        assertScript '''
            class MyMap extends HashMap {
                // Entry not just Map.Entry should work on next line
                Entry firstEntry() { entrySet().iterator().next() }
                static void main(args) {
                    def m = new MyMap()
                    m.a = 42
                    assert m.firstEntry().toString() == 'a=42'
                }
            }
        '''
    }
}
