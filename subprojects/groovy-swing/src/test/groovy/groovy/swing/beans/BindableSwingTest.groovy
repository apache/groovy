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

import groovy.swing.GroovySwingTestCase

class BindableSwingTest extends GroovySwingTestCase {
    void testExtendsComponent() {
        testInEDT {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable
                import javax.swing.JPanel

                class BindableTestBean6 extends JPanel {
                    @Bindable String testField
                    
                    /*
                    // if the following stub added, the illegal access warnings can be fixed
                    // java.awt.Component.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
                    // should we add this kind of stubs automatically?
                    void firePropertyChange(String propertyName,
                                      Object oldValue, Object newValue) {
                    
                        super.firePropertyChange(propertyName, oldValue, newValue)                  
                    }
                    */
                }

                sb = new BindableTestBean6()
                sb.testField = "bar"
                changed = false
                sb.propertyChange = {changed = true}
                sb.testField = "foo"
                assert changed
            """)
        }
    }
}