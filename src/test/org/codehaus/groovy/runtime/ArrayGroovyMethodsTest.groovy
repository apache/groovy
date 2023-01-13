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
package org.codehaus.groovy.runtime

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for ArrayGroovyMethods
 */
class ArrayGroovyMethodsTest {

    @Test
    void firstErrorCases() {
        assertNoSuchElementForAllPrimitiveEmptyArrays('first')
    }

    @Test
    void headErrorCases() {
        assertNoSuchElementForAllPrimitiveEmptyArrays('head')
    }

    @Test
    void initErrorCases() {
        assertUnsupportedOperationForAllPrimitiveEmptyArrays('init')
    }

    @Test
    void lastErrorCases() {
        assertNoSuchElementForAllPrimitiveEmptyArrays('last')
    }

    @Test
    void maxErrorCases() {
        assertUnsupportedOperationForGivenPrimitiveEmptyArrays('max', ['int', 'long', 'double'])
    }

    @Test
    void minErrorCases() {
        assertUnsupportedOperationForGivenPrimitiveEmptyArrays('max', ['int', 'long', 'double'])
    }

    @Test
    void tailErrorCases() {
        assertUnsupportedOperationForAllPrimitiveEmptyArrays('tail')
    }

    @Test
    void testArrayAsBooleanForNull() {
        for (type in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            assertScript """
            @groovy.transform.CompileStatic
            def method() {
                $type[] array = null
                assert !array.asBoolean()
            }

            method()
            """
        }
    }

    private static assertUnsupportedOperationForAllPrimitiveEmptyArrays(String method) {
        assertUnsupportedOperationForGivenPrimitiveEmptyArrays(method,
            ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double'])
    }


    private static assertUnsupportedOperationForGivenPrimitiveEmptyArrays(String method, ArrayList<String> types) {
        for (primType in types) {
            def ex = shouldFail(UnsupportedOperationException) {
                Eval.me("new $primType[0]")."$method"()
            }
            assert ex.message == "Accessing $method() is unsupported for an empty array"
        }
    }

    private static assertNoSuchElementForAllPrimitiveEmptyArrays(String method) {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]")."$method"()
            }
            assert ex.message == "Cannot access $method() for an empty array"
        }
    }

}
