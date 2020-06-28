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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9608 {

    @Test
    void testGetFieldOnSuper() {
        assertScript '''
            import org.codehaus.groovy.runtime.ScriptBytecodeAdapter

            class A {
              public x = 'A'
            }
            class B extends A {
              public x = 'B'
            }

            def a = new A()
            def ax = a.metaClass.getAttribute(A, a, 'x', false)
            assert ax == 'A'

            def b = new B()
            def bx = b.metaClass.getAttribute(B, b, 'x', false)
            assert bx == 'B'

            try {
                def bSuperX = b.metaClass.getAttribute(A, b, 'x', true)
                assert bSuperX == 'A'
                bSuperX = ScriptBytecodeAdapter.getFieldOnSuper(A, b, 'x')
                assert bSuperX == 'A'
            } catch (MissingFieldException e) {
                // TODO: Why can't these read public/protected field from super?
            }
        '''
    }
}
