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
package bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9609 {

    @Test
    void testGetPropertyOnSuper() {
        assertScript '''
            import org.codehaus.groovy.runtime.ScriptBytecodeAdapter

            class A {
                def getX() { 'A' }
            }
            class B extends A {
                def getX() { 'B' }
            }

            def b = new B()
            def bSuperX = b.metaClass.getProperty(B, b, 'x', true, false)
            assert bSuperX == 'A'

            bSuperX = ScriptBytecodeAdapter.getPropertyOnSuper(B, b, 'x')
            assert bSuperX == 'A'
        '''
    }
}
