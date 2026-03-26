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

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertEquals

final class Groovy2556 {

    private final String TEST_NAME = 'someName'
    private final String SOME_METHOD_VALUE = 'someMethodValue'

    private String s
    private count = 0
    private Map names = [:]

    private getCount() {
        count++
    }

    String someMethod() {
        s = SOME_METHOD_VALUE
    }

    String addName(String name) {
        names[name] = name
    }

    String addNameWithReturn(String name) {
        return names[name] = name
    }

    //--------------------------------------------------------------------------

    @Test
    void testCompile () {
        assertScript '''
            def map = [2:0]
            assert 33 == (map[2] += 33)
        '''
    }

    @Test
    void testAssignmentWithString() {
        assertEquals(SOME_METHOD_VALUE, someMethod())
    }

    @Test
    void testAssignmentWithMap() {
        assertEquals(TEST_NAME, addName(TEST_NAME))
    }

    @Test
    void testAssignmentWithReturnMap() {
        assertEquals(TEST_NAME, addNameWithReturn(TEST_NAME))
    }

    @Test
    void testArrayAssignment() {
        def arr = [*0..4]
        assertEquals 33, (arr[2] = 33)
        assertEquals 55, (arr[2] += 22)
    }

    @Test
    void testArrayAssignmentInClosure() {
        def arr = [*0..4]
        assertEquals 55, { arr[2] = 55 }.call()
        assertEquals 88, { arr[2] += 33 }.call()
    }

    @Test
    void testVarAssignment() {
        def var = 1
        assertEquals 77, (var = 77)
    }

    @Test
    void testVarAssignmentInClosure() {
        def var = 1
        assertEquals 22, { var = 22 }.call()
        assertEquals 55, { var += 33 }.call()
    }

    @Test
    void testReusableExpression() {
        def arr = (1..5).toArray()
        assertEquals 34, (arr[getCount()] += 33)
        assertEquals 34, arr[0]
        assertEquals  2, arr[getCount()]
    }
}
