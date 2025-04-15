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
package org.codehaus.groovy.transform

import groovy.test.GroovyShellTestCase

/**
 * Tests for the {@code @OperatorRename} AST transform.
 */
class OperatorRenameTransformTest extends GroovyShellTestCase {

    void testOperatorRename() {
        assertScript '''
            @groovy.transform.OperatorRename(multiply="plus", plus="multiply")
            def doNotDoThis() {
                assert 3 * 4 == 7
                assert 3 + 4 == 12
                assert 3 * 4 + 5 == 35
                assert 3 * (4 + 5) == 23
            }

            doNotDoThis()
        '''
    }

    void testOperatorRenameSimulateMatrixNamesFromCommonLibs() {
        assertScript '''
            @groovy.transform.TupleConstructor
            // To avoid extra test dependencies, we create a dummy class acting like an integer
            // but with method names like some common matrix libraries. It has:
            // * 'mult' like Ejml matrices
            // * 'add' like Commons Math matrices
            // * 'sub' like Nd4j matrices
            class DummyNumber {
                @Delegate Integer delegate

                Integer mult(DummyNumber other) {
                    delegate * other.delegate
                }

                Integer sub(DummyNumber other) {
                    delegate - other.delegate
                }

                Integer add(DummyNumber other) {
                    delegate + other.delegate
                }

                Integer mod(DummyNumber other) {
                    delegate % other.delegate
                }
            }

            @groovy.transform.OperatorRename(multiply="mult", minus="sub", plus="add", remainder="mod")
            def method() {
                assert new DummyNumber(4) * new DummyNumber(3) == 12
                assert new DummyNumber(4) + new DummyNumber(3) == 7
                assert new DummyNumber(4) - new DummyNumber(3) == 1
                assert new DummyNumber(7) % new DummyNumber(5) == 2
            }

            method()
        '''
    }

}
