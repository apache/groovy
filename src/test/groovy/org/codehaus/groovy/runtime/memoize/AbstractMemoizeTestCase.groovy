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
package org.codehaus.groovy.runtime.memoize

import groovy.test.GroovyTestCase

abstract class AbstractMemoizeTestCase extends GroovyTestCase {

    volatile int counter = 0

    AbstractMemoizeTestCase() {
        super()
    }

    void testCorrectness() {
        Closure cl = { it * 2 }
        Closure mem = buildMemoizeClosure(cl)
        assert 10 == mem(5)
        assert 4 == mem(2)
    }

    abstract Closure buildMemoizeClosure(Closure cl)

    void testNullParams() {
        Closure cl = { 2 }
        Closure mem = cl.memoize()
        assert 2 == mem(5)
        assert 2 == mem(2)
        assert 2 == mem(null)
    }

    void testNullResult() {
        Closure cl = { counter++; if (it == 5) return null else return 2 }
        Closure mem = cl.memoize()
        assert counter == 0
        assert null == mem(5)
        assert counter == 1
        assert 2 == mem(2)
        assert counter == 2
        assert null == mem(5)
        assert 2 == mem(2)
        assert counter == 2
    }

    void testNoParams() {
        Closure cl = { -> 2 }
        Closure mem = cl.memoize()
        assert 2 == mem()
        assert 2 == mem()
    }

    void testCaching() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoize()
        assert 10 == mem(5)
        assert flag
        flag = false
        assert 4 == mem(2)
        assert flag
        flag = false

        assert 4 == mem(2)
        assert 4 == mem(2)
        assert 10 == mem(5)
        assert !flag

        assert 6 == mem(3)
        assert flag
        flag = false
        assert 6 == mem(3)
        assert !flag
    }

    void testComplexParameter() {
        def callFlag = []

        Closure cl = { a, b, c ->
            callFlag << true
            c
        }
        Closure mem = cl.memoize()
        checkParams(mem, callFlag, [1, 2, 3], 3)
        checkParams(mem, callFlag, [1, 2, 4], 4)
        checkParams(mem, callFlag, [1, [2], 4], 4)
        checkParams(mem, callFlag, [[1: '1'], [2], 4], 4)
        checkParams(mem, callFlag, [[1, 2], 2, 4], 4)
        checkParams(mem, callFlag, [[1, 2], null, 4], 4)
        checkParams(mem, callFlag, [null, null, 4], 4)
        checkParams(mem, callFlag, [null, null, null], null)
        checkParams(mem, callFlag, [null, [null], null], null)
    }

    def checkParams(Closure mem, callFlag, args, desiredResult) {
        assertEquals desiredResult, mem( * args )
        assert !callFlag.empty
        callFlag.clear()
        assert desiredResult == mem(*args)
        assert callFlag.empty
    }
}
