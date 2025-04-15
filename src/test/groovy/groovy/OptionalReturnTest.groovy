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

class OptionalReturnTest extends GroovyTestCase {

    def y

    void testSingleExpression() {
        def value = foo()

        assert value == 'fooReturn'
    }

    void testLastExpressionIsSimple() {
        def value = bar()

        assert value == 'barReturn'
    }

    void testLastExpressionIsBooleanExpression() {
        def value = foo2()

        assert value

        value = foo3()

        assert value == false
    }

    void testLastExpressionIsAssignment() {
        def value = assign()

        assert value == 'assignReturn'

        value = assignField()

        assert value == 'assignFieldReturn'
    }

    void testLastExpressionIsMethodCall() {
        def value = methodCall()

        assert value == 'fooReturn'
    }

    void testEmptyExpression() {
        def value = nullReturn()

        assert value == null
    }

    //  now this is not a compile time error in jsr-03

    void testVoidMethod() {
        def value = voidMethod()

        assert value == null
    }

    void testNonAssignmentLastExpressions() {
        def value = lastIsAssert()

        assert value == null
    }

    def foo() {
        'fooReturn'
    }

    def bar() {
        def x = 'barReturn'
        x
    }

    def foo2() {
        def x = 'cheese'
        x == 'cheese'
    }

    def foo3() {
        def x = 'cheese'
        x == 'edam'
    }

    def assign() {
        def x = 'assignReturn'
    }

    def assignField() {
        y = 'assignFieldReturn'
    }

    def nullReturn() {
    }

    def lastIsAssert() {
        assert 1 == 1
    }

    def methodCall() {
        foo()
    }

    void voidMethod() {
        foo()
    }
}
