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

class ClosureUsingOuterVariablesTest extends GroovyTestCase {

    void testUseOfOuterVariable() {

        def x = 123
        def y = "hello"

        def closure = { i ->
            assert x == 123
            assert y == 'hello'
            assert i == 321
        }
        closure.call(321)
    }

    /*
    TODO: is this a valid test case?
    void testInnerVariablesVisibleInOuterScope() {

       closure = { z = 456 }
       closure.call(321)

       assert z == 456
   }
   */

    void testModifyingOuterVariable() {

        def m = 123

        def closure = { m = 456 }
        closure.call(321)

        assert m == 456
    }

    void testCounting() {
        def sum = 0

        [1, 2, 3, 4].each { sum = sum + it }

        assert sum == 10
    }

    void testExampleUseOfClosureScopes() {
        def a = 123
        def b
        def c = { b = a + it }
        c(5)

        assert b == a + 5
    }

    void testExampleUseOfClosureScopesUsingEach() {
        def a = 123
        def b
        [5].each { b = a + it }

        assert b == a + 5
    }
}
