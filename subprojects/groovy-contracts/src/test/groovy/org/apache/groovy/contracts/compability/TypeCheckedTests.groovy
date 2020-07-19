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
package org.apache.groovy.contracts.compability

import groovy.test.GroovyShellTestCase

class TypeCheckedTests extends GroovyShellTestCase {

    void testPrecondition() {
        evaluate '''
            import groovy.contracts.*

            @groovy.transform.TypeChecked
            class A {
                @Requires({ some?.size() > 0 })
                def op(String some) {
                }
            }

            def a = new A()
        '''

        evaluate '''
            import groovy.contracts.*

            @groovy.transform.TypeChecked
            class A {
                @Requires({ some?.size() > 0 })
                def op(def some) {
                }
            }

            def a = new A()
        '''
    }

    void testPostcondition() {
        evaluate '''
            import groovy.contracts.*

            @groovy.transform.TypeChecked
            class A {

                @Ensures({ result.size() > 0 })
                String op(String some) {
                    some
                }
            }

            def a = new A()
        '''

        evaluate '''
            import groovy.contracts.*

            @groovy.transform.TypeChecked
            class A {
                private int i = 12

                @Ensures({ old.i + 2 == 12 })
                def op(String some) {
                    some
                }
            }

            def a = new A()
        '''
    }

    void testClassInvariant() {
        evaluate '''
            import groovy.contracts.*

            @groovy.transform.TypeChecked
            @Invariant({ i >= 0 })
            class A {
                private int i = 12
            }

            def a = new A()
        '''
    }
}
