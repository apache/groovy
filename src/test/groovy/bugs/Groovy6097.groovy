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

import groovy.test.NotYetImplemented
import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy6097 {

    @Test @NotYetImplemented
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

    @Test @NotYetImplemented
    void testSuperIsser2() {
        assertScript '''
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
    }
}
