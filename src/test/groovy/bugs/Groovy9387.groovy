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
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.Assume.assumeFalse

@CompileStatic
final class Groovy9387 {

    private static final String SUPPORT_ADAPTER = '''
        class BuilderSupportAdapter extends BuilderSupport {
            def createNode(a, b) {}
            def createNode(a) {}
            def createNode(a, Map b) {}
            def createNode(a, Map b, c) {}
            void setParent(a, b) {}
        }
    '''

    @Test
    void testThisPropertySet() {
        assertScript SUPPORT_ADAPTER + '''
            class C extends BuilderSupportAdapter {
                String value = 'abc'
                void test() {
                    def b = { -> this.'value' = 'def' }
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
    void testThisSetProperty() {
        assertScript SUPPORT_ADAPTER + '''
            class C extends BuilderSupportAdapter {
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
        assertScript SUPPORT_ADAPTER + '''
            class C extends BuilderSupportAdapter {
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
