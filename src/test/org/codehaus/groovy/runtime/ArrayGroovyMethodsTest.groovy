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

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for ArrayGroovyMethods
 */
class ArrayGroovyMethodsTest {

    @Test
    void testTailErrorCases() {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]").tail()
            }
            assert ex.message == "Cannot access tail() for an empty array"
        }
    }

    @Test
    void testInitErrorCases() {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]").init()
            }
            assert ex.message == "Cannot access init() for an empty array"
        }
    }

    @Test
    void testLastErrorCases() {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]").last()
            }
            assert ex.message == "Cannot access last() for an empty array"
        }
    }

    @Test
    void testFirstErrorCases() {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]").first()
            }
            assert ex.message == "Cannot access first() for an empty array"
        }
    }

    @Test
    void testHeadErrorCases() {
        for (primType in ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']) {
            def ex = shouldFail(NoSuchElementException) {
                Eval.me("new $primType[0]").head()
            }
            assert ex.message == "Cannot access head() for an empty array"
        }
    }

}
