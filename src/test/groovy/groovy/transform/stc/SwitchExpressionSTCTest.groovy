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
package groovy.transform.stc

import org.junit.jupiter.api.Test

/**
 * Static type checking for first-class switch expressions (GROOVY-12255 / JEP 361).
 */
class SwitchExpressionSTCTest extends StaticTypeCheckingTestCase {

    @Test
    void testInferredTypeFromArms() {
        assertScript '''
            String s = switch (1) {
                case 1 -> 'a'
                default -> 'b'
            }
            assert s == 'a'
        '''
    }

    @Test
    void testIntResult() {
        assertScript '''
            int n = switch (2) {
                case 1 -> 10
                case 2 -> 20
                default -> 0
            }
            assert n == 20
        '''
    }

    @Test
    void testExhaustiveEnumNeedsNoDefault() {
        assertScript '''
            import java.time.Month
            import static java.time.Month.*

            String q(Month m) {
                switch (m) {
                    case JANUARY, FEBRUARY, MARCH -> 'Q1'
                    case APRIL, MAY, JUNE -> 'Q2'
                    case JULY, AUGUST, SEPTEMBER -> 'Q3'
                    case OCTOBER, NOVEMBER, DECEMBER -> 'Q4'
                }
            }
            assert q(JUNE) == 'Q2'
        '''
    }

    @Test
    void testNonExhaustiveIsError() {
        shouldFailWithMessages '''
            def r = switch (1) {
                case 1 -> 'a'
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testYieldTypesAreUnified() {
        assertScript '''
            CharSequence cs = switch (1) {
                case 1 -> 'hello'
                default -> new StringBuilder('x')
            }
            assert cs.toString() in ['hello', 'x']
        '''
    }

    @Test
    void testNestedExpressionInsideSwitchStatementDifferentEnums() {
        assertScript '''
            enum Color { RED, BLUE }
            enum Size { S, L }

            int meth(Color color, Size size) {
                switch (color) {
                    case RED:
                        return switch (size) {
                            case S -> 1
                            case L -> 2
                        }
                    case BLUE:
                        return switch (size) {
                            case S -> 3
                            case L -> 4
                        }
                }
            }
            assert meth(Color.RED, Size.S) == 1
            assert meth(Color.BLUE, Size.L) == 4
        '''
    }

    @Test
    void testAssignmentToOuterLocal() {
        assertScript '''
            int acc = 0
            int r = switch (1) {
                case 1 -> {
                    acc = 4
                    yield acc + 1
                }
                default -> 0
            }
            assert r == 5
            assert acc == 4
        '''
    }
}
