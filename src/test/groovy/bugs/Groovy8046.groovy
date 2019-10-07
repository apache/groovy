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

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy8046 {

    @Test
    void testFieldShouldNotHavePrimitiveVoidType() {
        def err = shouldFail '''
            class MyClass {
                void field
            }
        '''

        assert err =~ /The field 'field' has invalid type void|void is not allowed here/
    }

    @Test
    void testParameterShouldNotHavePrimitiveVoidType() {
        def err = shouldFail '''
            class MyClass {
                int foo(void param) {}
            }
        '''

        assert err =~ /The parameter 'param' in method 'int foo\(void\)' has invalid type void|void is not allowed here/
    }

    @Test
    void testLocalVariableShouldNotHavePrimitiveVoidType() {
        def err = shouldFail '''
            class MyClass {
                def foo() {
                    void bar = null
                }
            }
        '''

        assert err =~ /The variable 'bar' has invalid type void|void is not allowed here/
    }
}
