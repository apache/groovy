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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy6097 {

    @Test
    void testSuperIsser1() {
        assertScript '''
            class A {
                boolean isBool() { true }
            }

            class B extends A {
                B() {
                    assert bool
                    assert this.bool
                    assert super.bool // MissingMethodException: No signature of method: B.getBool() is applicable for argument types: () values: []
                }
            }

            new B()
        '''
    }

    @Test // GROOVY-9382, GROOVY-10133
    void testSuperIsser2() {
        def err = shouldFail '''
            class A {
                Boolean isBool() { Boolean.TRUE }
            }

            class B extends A {
                B() {
                    assert bool
                    assert this.bool
                    assert super.bool // MissingMethodException: No signature of method: B.getBool() is applicable for argument types: () values: []
                }
            }

            new B()
        '''
        assert err =~ /MissingPropertyException: No such property: bool for class: B/
    }

    @Test
    void testSuperIsser3() {
        assertScript '''
            class A {
                boolean isBool() { true }
            }

            class B extends A {
                boolean isBool() { false }

                void test() {
                    assert !bool
                    assert !this.bool
                    assert super.bool
                }
            }

            new B().test()
        '''
    }

    @Test
    void testSuperIsser4() {
        assertScript '''
            class A {
                boolean isBool() { true }
            }

            class B extends A {
                boolean bool = false

                void test() {
                    assert !bool
                    assert !this.bool
                    assert super.bool
                }
            }

            new B().test()
        '''
    }
}
