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

class DefaultParamClosureTest extends GroovyTestCase {

    void testDefaultParameters() {
        // Default parameters working for closures 
    def doSomething = { a, b = 'defB', c = 'defC' ->
            return a + "-" + b + "-" + c
        }

        def value = doSomething("X", "Y", "Z")
        assert value == "X-Y-Z"

        value = doSomething("X", "Y")
        assert value == "X-Y-defC"

        value = doSomething("X")
        assert value == "X-defB-defC"

        shouldFail { doSomething() }
    }

    void testDefaultTypedParameters() {
    // Handle typed parameters
    def doTypedSomething = { String a = 'defA', String b = 'defB', String c = 'defC' ->
            return a + "-" + b + "-" + c
        }
    
        def value = doTypedSomething("X", "Y", "Z")
        assert value == "X-Y-Z"
        
        value = doTypedSomething("X", "Y")
        assert value == "X-Y-defC"
        
        value = doTypedSomething("X")
        assert value == "X-defB-defC"
        
        value = doTypedSomething()
        assert value == "defA-defB-defC"
    }

}