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
 * GROOVY-11476: {@code <T> T m(T, T)} applied to {@code Double} and
 * {@code Integer} used to be inferred in a way that accepted
 * {@code compareTo(new Object())} and then failed at runtime. The
 * call is now rejected.
 */
final class Groovy11476 {

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testNumberComparableInferredFromDoubleAndIntegerCannotCompareToObject(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            void test() {
                m((Double) 2.0, (Integer) 1).compareTo(new Object())
            }
            <T> T m(T x, T y) { return x }
        """
        assert err.message.contains('Cannot call')
        assert err.message.contains('compareTo')
        assert err.message.contains('with arguments [java.lang.Object]')
    }
}
