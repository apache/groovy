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

    // GROOVY-9848: @OperatorRename as a porting vehicle to restore pre-9848 value-based `in`
    void testMembershipRenameRestoresLegacyInForMaps() {
        assertScript '''
            assert 'b' in [a:1, b:0]                 // default (post-9848): key-based

            @groovy.transform.OperatorRename(isIn='isCase', isNotIn='isNotCase')
            def legacy() {
                assert !('b' in [a:1, b:0])          // reverted: value 0 is falsy
                assert 'a' in [a:1, b:0]             // value 1 is truthy
                assert 'b' !in [a:1, b:0]
            }
            legacy()
        '''
    }

    void testMembershipRenameRestoresLegacyInForStrings() {
        assertScript '''
            assert 'ell' in 'hello'                  // default (post-9848): substring

            // set both isIn and isNotIn so `in` / `!in` stay consistent
            @groovy.transform.OperatorRename(isIn='isCase', isNotIn='isNotCase')
            def legacy() {
                assert !('ell' in 'hello')           // reverted: equality
                assert 'ell' !in 'hello'             // and !in agrees
                assert 'hello' in 'hello'
            }
            legacy()
        '''
    }

    // isIn and isNotIn are independent (like the other operators): renaming only one leaves the
    // other on the default, so for maps/char-sequences `in` and `!in` can disagree. Set both to
    // stay consistent (see the isIn javadoc).
    void testMembershipRenameInAndNotInAreIndependent() {
        assertScript '''
            @groovy.transform.OperatorRename(isIn='isCase')   // only `in` reverted; `!in` stays default
            def partial(Map m) { ['b' in m, 'b' !in m] }
            def (inResult, notInResult) = partial([a:1, b:0])
            assert inResult == false       // isCase: value 0 is falsy
            assert notInResult == false    // default isNotIn: 'b' IS a key -> !containsKey -> false
            assert inResult == notInResult // both false -- inconsistent without setting isNotIn too
        '''
    }

    void testMembershipRenameToArbitraryMethod() {
        assertScript '''
            @groovy.transform.OperatorRename(isIn='containsKey')
            def f(Map m, key) { key in m }           // `a in b` dispatches on the right operand: b.containsKey(a)
            assert f([a:1, b:0], 'b')                // containsKey -> true even for value 0
            assert !f([a:1], 'z')
        '''
    }

    void testMembershipRenameLeavesOtherReceiversEquivalent() {
        assertScript '''
            @groovy.transform.OperatorRename(isIn='isCase')
            def f() {
                assert 2 in [1, 2, 3]                // list.isCase(2) -> contains, unchanged
                assert 5 !in [1, 2, 3]
            }
            f()
        '''
    }

    void testMembershipRenameUnderCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            @groovy.transform.OperatorRename(isIn='isCase', isNotIn='isNotCase')
            String legacy(Map<String,Integer> m, String k) {
                (k in m) ? 'truthy' : 'falsy'        // reverted to value-based under static too
            }
            assert legacy([a:1, b:0], 'a') == 'truthy'
            assert legacy([a:1, b:0], 'b') == 'falsy'   // value 0
            assert legacy([a:1, b:0], 'z') == 'falsy'   // absent
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
