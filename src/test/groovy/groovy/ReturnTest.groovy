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

/**
 * Tests the use of returns in Groovy
 */
class ReturnTest extends GroovyTestCase {
    void testIntegerReturnValues() {
        def value = foo(5)
        assert value == 10
    }

    void testBooleanReturnValues() {
        def value = bar(6)
        assert value
    }

    def foo(x) {
        return (x * 2)
    }

    def bar(x) {
        return x > 5
    }

    void testVoidReturn() {
        explicitVoidReturn()
        implicitVoidReturn()
        explicitVoidReturnWithoutFinalReturn()
        implicitVoidReturnWithoutFinalReturn()
    }

    void explicitVoidReturn() {
        return
    }

    def implicitVoidReturn() {
        return
    }

    void explicitVoidReturnWithoutFinalReturn() {
        def x = 4
        if (x == 3) {
            return
        } else {
            try {
                x = 3
                return
            } finally {
                //do nothing
            }
        }
    }

    def implicitVoidReturnWithoutFinalReturn() {
        def x = 4
        if (x == 3) {
            return
        } else {
            try {
                x = 3
                return
            } finally {
                //do nothing
            }
        }
    }
}
