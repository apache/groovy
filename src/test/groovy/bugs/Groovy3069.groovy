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

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.shouldFail

final class Groovy3069 {

    private static final String CLOSURE_STR = '[Closure]'
    private static final String CLASS_METHOD_STR = '[ClassMethod]'

    String method() {
        CLASS_METHOD_STR
    }

    String checkPrecendenceWithTypeSpecified(Closure method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String checkPrecendenceWithTypeNotSpecified(method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    //

    @Test
    void testClosureParamPrecedenceWithTypeSpecified() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithTypeSpecified(cl)
    }

    @Test
    void testClosureParamPrecedenceWithTypeNotSpecified() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithTypeNotSpecified(cl)
    }

    @Test
    void testClosureLocalVarPrecedenceExplicitClosureType() {
        Closure method = { CLOSURE_STR }
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    @Test
    void testClosureLocalVarPrecedenceImplicitClosureType() {
        def method = { CLOSURE_STR }
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    // GROOVY-11677
    @ParameterizedTest
    @ValueSource(strings=['CompileDynamic','TypeChecked','CompileStatic'])
    void testNonCallableVariable(String mode) {
        shouldFail """
            class C {
                @groovy.transform.$mode
                void test(List<String> names = ['fizz','buzz']) {
                    print(names  [0])
                    print(names()[0]) // error: not callable
                }
                List<String> names() {
                    ['foo','bar']
                }
            }
            new C().test()
        """
    }
}
