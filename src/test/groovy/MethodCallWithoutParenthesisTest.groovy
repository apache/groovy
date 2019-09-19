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
package groovy

import groovy.test.GroovyTestCase

class MethodCallWithoutParenthesisTest extends GroovyTestCase {

    def flag

    void testMethodCallWithOneParam() {
        flag = false
        
        methodWithOneParam "hello"
        
        assert flag
    }
    
    void testMethodCallWithOneParamUsingThis() {
        flag = false
        
        this.methodWithOneParam "hello"
        
        assert flag
    }
    
    void methodWithOneParam(text) {
        assert text == "hello"
        flag = true
    }
    
    void testMethodCallWithTwoParams() {
        methodWithTwoParams 5, 6

        // not allowed in New Groovy
        // value = methodWithTwoParams 5, 6
        def value = methodWithTwoParams(5, 6)

        assert value == 11
    }
    
    void testMethodCallWithTwoParamsUsingThis() {
        def value = this.methodWithTwoParams(5, 6)
        
        assert value == 11
    }

    def methodWithTwoParams(a, b) {
        return a + b
    }
}