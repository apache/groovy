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
package bugs

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-11470: after flow-typing a {@code Comparable<Integer>} with a
 * {@code String}, assignment to {@code Comparable<? super Integer>} used
 * to be accepted and then fail at runtime. The assignment is now rejected,
 * as is the simpler wildcard assignment from the ticket discussion.
 */
final class Groovy11470 {

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testFlowTypedComparableIsNotAssignableToSuperInteger(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            void test() {
                def x = m()
                if (true) {
                    x = ""
                }
                Comparable<? super Integer> p = x
                p.compareTo(1)
            }
            Comparable<Integer> m() {
                return 1
            }
        """
        assert err.message.contains('Incompatible generic argument types')
        assert err.message.contains('Comparable<? super java.lang.Integer>')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testComparableExtendsSerializableIsNotAssignableToSuperInteger(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            void test(Comparable<? extends Serializable> c1) {
                Comparable<? super Integer> c2 = c1
            }
        """
        assert err.message.contains('Incompatible generic argument types')
        assert err.message.contains('Cannot assign java.lang.Comparable<? extends java.io.Serializable>')
        assert err.message.contains('Comparable<? super java.lang.Integer>')
    }
}
