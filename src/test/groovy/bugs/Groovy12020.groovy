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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy12020 {

    @Test // GROOVY-12020
    void testEnumSetPlusReturnsEnumSet() {
        assertScript '''
            enum E { A, B, C, D }
            EnumSet<E> first = EnumSet.of(E.A, E.B, E.C)
            def sum = first + E.D
            assert sum instanceof EnumSet
            assert sum as List == [E.A, E.B, E.C, E.D]
        '''
    }

    @Test // GROOVY-12020
    void testEnumSetPlusInStaticInitializer() {
        // Reporter's original shape: EnumSet field built from `another + element`
        // under @CompileStatic, requiring an EnumSet-typed cast at the assignment.
        assertScript '''
            import groovy.transform.CompileStatic

            enum EnumTest {
                ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE

                public static final EnumSet<EnumTest> firstSet  = EnumSet.of(ONE, TWO, THREE, FOUR, FIVE)
                public static final EnumSet<EnumTest> secondSet = firstSet + SIX
            }

            @CompileStatic
            class Application {
                static void main(String[] args) {
                    assert EnumTest.secondSet instanceof EnumSet
                    assert EnumTest.secondSet as List == [EnumTest.ONE, EnumTest.TWO, EnumTest.THREE,
                                                          EnumTest.FOUR, EnumTest.FIVE, EnumTest.SIX]
                }
            }

            Application.main()
        '''
    }
}
