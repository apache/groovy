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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy7876Bug extends GroovyTestCase {
    void testClassCastExceptionsFromCompareToShouldNotLeakOutOfEqualityCheck() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            enum E1 {A, B, C}
            enum E2 {D, E, F}
            class Holder<T> implements Comparable<T> {
                final T thing
                Holder(T thing) { this.thing = thing }
                int compareTo(T other) { thing.compareTo(other.thing) }
            }
            def a = new Holder<E1>(E1.A)
            def d = new Holder<E2>(E2.D)

            // control cases
            assert E1.A != E2.D
            shouldFail(IllegalArgumentException) {
                E1.A <=> E2.D
            }

            // holder cases
            assert a != d // invokes compareTo
            shouldFail(IllegalArgumentException) {
                a <=> d
            }
        '''
    }
}
