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
package gls.statements

import gls.CompilableTestSupport

class MultipleAssignmentTest extends CompilableTestSupport {

    void testList() {
        def list = [1, 2]
        def a, b

        (a, b) = list
        assert a == 1
        assert b == 2

        (a, b) = [3, 4]
        assert a == 3
        assert b == 4
    }

    void testArray() {
        def array = [1, 2] as int[]
        def a, b

        (a, b) = array
        assert a == 1
        assert b == 2
    }

    def foo() {[1, 2]}

    void testMethod() {
        def a, b

        (a, b) = foo()
        assert a == 1
        assert b == 2
    }

    void testMethodOverflow() {
        def a, b = 3

        (a) = foo()
        assert a == 1
        assert b == 3
    }

    void testMethodUnderflow() {
        def a, b, c = 4

        (a, b, c) = foo()
        assert a == 1
        assert b == 2
        assert c == null
    }

    void testChainedMultiAssignment() {
        def a, b, c, d
        (c, d) = (a, b) = [1, 2]
        assert [a, b] == [1, 2]
        assert [c, d] == [1, 2]
        (c, d) = a = (a, b) = [3, 4]
        assert [c, d] == [3, 4]
    }
}