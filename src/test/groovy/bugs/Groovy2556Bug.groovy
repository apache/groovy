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

import groovy.test.GroovyTestCase

class Groovy2556Bug extends GroovyTestCase {
    final String SOME_METHOD_VALUE = 'someMethodValue'
    final String TEST_NAME = 'someName'

    String s

    Map names

    void setUp() {
        names = [:]
    }

    private count = 0

    private getCount () {
        count++;
    }

    void testCompile () {
        new GroovyShell().parse ("""
        def arr = [2:0]
        assert 33 == (arr[2] += 33)
        """).run ()
    }

    void testAssignmentWithString() {
        assertEquals(SOME_METHOD_VALUE, someMethod())
    }

    void testAssignmentWithMap() {
        assertEquals(TEST_NAME, addName(TEST_NAME))
    }

    void testAssignmentWithReturnMap() {
        assertEquals(TEST_NAME, addNameWithReturn(TEST_NAME))
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

    void testArrayAssignment() {
        def arr = [*0..4]
        assert 33 == (arr[2] = 33)
        assert 55 == (arr[2] += 22)
    }

    void testArrayAssignmentInClosure() {
        def arr = [*0..4]
        assert 55 == { arr[2] = 55 }.call()
        assert 88 == { arr[2] += 33 }.call()
    }

    void testVarAssignment() {
        def var = 1
        assert 77 == ( var = 77)
    }

    void testVarAssignmentInClosure() {
        def var = 1
        assert 22 == { var = 22 }.call()
        assert 55 == { var += 33 }.call()
    }

    void testReusableExpression() {
        def arr = [*1..5]
        assert 34 == (arr[getCount()] += 33)
        assert 34 == arr [0]
        assert 2 == arr [getCount()]
    }
}

