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
package groovy.swing.beans

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.util.HeadlessTestSupport.isHeadless
import static org.junit.jupiter.api.Assumptions.assumeFalse

final class BindableSwingTest {

    @BeforeEach
    void setUp() {
        assumeFalse(isHeadless())
    }

    // GROOVY-8339, GROOVY-10070
    @Test
    void testExtendsComponent() {
        assertScript '''
            class BindableTestBean extends javax.swing.JPanel {
                @groovy.beans.Bindable String testValue
            }

            changed = false

            def bean = new BindableTestBean(testValue: 'foo')
            bean.propertyChange = {changed = true}
            bean.testValue = 'bar'
            assert changed
        '''
    }

    // GROOVY-11876
    @Test
    void testBindableProperties() {
        assertScript '''
            import groovy.beans.Bindable
            import groovy.swing.SwingBuilder

            class Main {
                @Bindable
                static class Bean {
                    String foo = 'bar'
                }

                static def  bean1 = new Bean()
                static Bean bean2 = new Bean()

                static main(args) {
                    new SwingBuilder().edt {
                        def label1 = label(text: bind { bean1.foo })
                        def label2 = label(text: bind { bean2.foo })

                        assert label1.text == 'bar'
                        bean1.foo = 'baz'
                        assert label1.text == 'baz'

                        assert label2.text == 'bar'
                        bean2.foo = 'baz'
                        assert label2.text == 'baz'
                    }
                }
            }
        '''
    }
}
