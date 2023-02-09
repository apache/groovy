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

import org.junit.Test

import static groovy.swing.GroovySwingTestCase.testInEDT

final class VetoableSwingTest {

    // GROOVY-8339, GROOVY-10070
    @Test
    void testExtendsComponent() {
        testInEDT {
            new GroovyShell().evaluate '''
                class VetoableTestBean extends javax.swing.JPanel {
                    @groovy.beans.Vetoable String testValue
                }

                changed = false

                def bean = new VetoableTestBean(testValue: 'bar')
                bean.vetoableChange = {changed = true}
                bean.testValue = 'foo'
                assert changed
            '''
        }
    }
}
