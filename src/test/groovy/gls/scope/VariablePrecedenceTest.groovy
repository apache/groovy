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
package gls.scope

import groovy.test.GroovyTestCase

/**
*  test case based on GROOVY-3069
*/
class VariablePrecedenceTest extends GroovyTestCase {
    final String CLOSURE_STR = '[Closure]'
    final String CLASS_METHOD_STR = '[ClassMethod]'

    def void testClosureParamPrecedenceExplicitClosureType() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithExplicitClosureType(cl)
    }

    def void testClosureParamPrecedenceImplicitClosureType() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithImplicitClosureType(cl)
    }

    def void testClosureLocalVarPrecedenceExplicitClosureType() {
        Closure method = { CLOSURE_STR }

        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    def void testClosureLocalVarPrecedenceImplicitClosureType() {
        def method = { CLOSURE_STR }

        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String method() {
        return CLASS_METHOD_STR
    }

    String checkPrecendenceWithExplicitClosureType(Closure method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String checkPrecendenceWithImplicitClosureType(method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }
}
