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
final class Groovy9387 {

    @Test @NotYetImplemented
    void testThisSetProperty() {
        assertScript '''
            class C extends BuilderSupport {
                def createNode(a, b) {}
                def createNode(a) {}
                def createNode(a, Map b) {}
                def createNode(a, Map b, c) {}
                void setParent(a, b) {}

                String value = 'abc'

                void test() {
                    def b = { -> this.setProperty('value', 'def') }
                    b()
                }
            }

            def c = new C()
            def v = c.value
            assert v == 'abc'

            c.test()
            v = c.value
            assert v == 'def'
        '''
    }

    @Test
    void testThatSetProperty() {
        assertScript '''
            class C extends BuilderSupport {
                def createNode(a, b) {}
                def createNode(a) {}
                def createNode(a, Map b) {}
                def createNode(a, Map b, c) {}
                void setParent(a, b) {}

                private C that = this
                String value = 'abc'

                void test() {
                    def b = { -> that.setProperty('value', 'def') }
                    b()
                }
            }

            def c = new C()
            def v = c.value
            assert v == 'abc'

            c.test()
            v = c.value
            assert v == 'def'
        '''
    }
}
