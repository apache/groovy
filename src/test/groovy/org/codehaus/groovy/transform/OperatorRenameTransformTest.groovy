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

    // GROOVY-9848: @OperatorRename can opt a scope into key-based map membership (the Groovy 6
    // default). Groovy 5's own default `in` stays value-based (isCase).
    void testMembershipRenameOptsIntoKeyMembership() {
        assertScript '''
            assert !('b' in [a:1, b:0])              // Groovy 5 default: value-based (0 is falsy)

            @groovy.transform.OperatorRename(isIn='containsKey')
            def keyBased() {
                assert 'b' in [a:1, b:0]             // opted in: containsKey
                assert !('z' in [a:1, b:0])
            }
            keyBased()
        '''
    }

    // GROOVY-9848 / GROOVY-2456: `isIn='contains'` opts a scope into substring string membership
    // (the Groovy 6 default for char sequences). It also leaves collections/ranges unchanged
    // (their `contains` already matches the default `in`); only maps are unsupported (no `contains`).
    void testMembershipRenameOptsIntoSubstringMembership() {
        assertScript '''
            assert !('ell' in 'hello')               // Groovy 5 default: equality (no substring)

            @groovy.transform.OperatorRename(isIn='contains')
            def substring() {
                assert 'ell' in 'hello'              // opted in: String.contains
                assert !('xyz' in 'hello')
                assert 'llo' in "he${'llo'}"         // GString
                assert 2 in [1, 2, 3]                // collections unchanged (Collection.contains)
                assert 3 in (1..5)                   // ranges unchanged
            }
            substring()
        '''
    }

    void testMembershipRenameToIsCaseIsHarmlessOnGroovy5() {
        assertScript '''
            @groovy.transform.OperatorRename(isIn='isCase', isNotIn='isNotCase')
            def f() {
                assert !('b' in [a:1, b:0])          // no-op vs Groovy 5 default (still value-based)
                assert 'a' in [a:1, b:0]
                assert 'b' !in [a:1, b:0]
            }
            f()
        '''
    }

    void testMembershipRenameUnderCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            @groovy.transform.OperatorRename(isIn='containsKey')
            boolean has(Map<String,Integer> m, String k) { k in m }
            assert has([a:1, b:0], 'b')              // containsKey -> true even for value 0
            assert !has([a:1], 'z')
        '''
    }

    // The ScriptBytecodeAdapter.isIn helper backported for forward binary compatibility with
    // Groovy 6 in-operator codegen (Map -> containsKey, CharSequence -> contains, else -> isCase).
    void testScriptBytecodeAdapterIsInHelper() {
        assertScript '''
            import org.codehaus.groovy.runtime.ScriptBytecodeAdapter as SBA
            assert  SBA.isIn('b', [a:1, b:0])        // map -> containsKey (value 0 irrelevant)
            assert !SBA.isIn('z', [a:1, b:0])
            assert  SBA.isIn('ell', 'hello')         // char sequence -> contains
            assert  SBA.isIn(2, [1, 2, 3])           // collection -> contains (via isCase)
            assert  SBA.isNotIn('z', [a:1])
            // does not mutate a withDefault map
            def dm = [:].withDefault{ 1 }
            assert !SBA.isIn('x', dm)
            assert !dm.containsKey('x')
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
