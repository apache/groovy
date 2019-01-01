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
package groovy

/**
 * A test case for switch statement with different types
 */
class SwitchWithDifferentTypesTest extends GroovyTestCase {

    void testSwitchWithIntValues() {
        assertSwitch(1, 2, 3, 4)
    }

    void testSwitchWithDoubleValues() {
        assertSwitch(1.5, 2.4, 3.2, 4.1)
    }

    void testSwitchWithStringValues() {
        assertSwitch("abc", "def", "xyz", "unknown")
    }

    void testSwitchWithMixedTypeValues() {
        assertSwitch("abc", new Date(), 5.32, 23)
    }

    void testSwitchWithMixedNumberValues() {
        def i = 1 as Integer
        def bi = 1 as BigInteger
        def bd1 = 1.0 as BigDecimal
        def bd2 = 1.0000 as BigDecimal
        assertSwitchMixed(i, bi, 2, 3)
        assertSwitchMixed(i, bd1, 2, 3)
        assertSwitchMixed(i, bd2, 2, 3)
        assertSwitchMixed(bi, i, 2, 3)
        assertSwitchMixed(bi, bd1, 2, 3)
        assertSwitchMixed(bi, bd2, 2, 3)
        assertSwitchMixed(bd1, i, 2, 3)
        assertSwitchMixed(bd1, bi, 2, 3)
        assertSwitchMixed(bd1, bd2, 2, 3)
        assertSwitchMixed(bd2, i, 2, 3)
        assertSwitchMixed(bd2, bi, 2, 3)
        assertSwitchMixed(bd2, bd1, 2, 3)
    }

    void assertSwitchMixed(switchValue, matchingValue, nonmatchingValue1, nonmatchingValue2) {
        assertSwitchMatch1(switchValue, matchingValue, nonmatchingValue1, nonmatchingValue2)
        assertSwitchMatch2(switchValue, nonmatchingValue1, matchingValue, nonmatchingValue2)
        assertSwitchMatch3(switchValue, nonmatchingValue1, nonmatchingValue2, matchingValue)
    }

    void assertSwitch(a, b, c, d) {
        assertSwitchMatch1(a, a, b, c)
        assertSwitchMatch2(b, a, b, c)
        assertSwitchMatch3(c, a, b, c)
        assertSwitchMatchDefault(d, a, b, c)
    }

    void assertSwitchMatch1(value, case1Value, case2Value, case3Value) {
        switch (value) {
            case case1Value:
                // worked
                break
            case case2Value:
                failNotEquals(value, case2Value)
                break
            case case3Value:
                failNotEquals(value, case3Value)
                break
            default:
                failNotDefault(value)
                break
        }
    }

    void assertSwitchMatch2(value, case1Value, case2Value, case3Value) {
        switch (value) {
            case case1Value:
                failNotEquals(value, case1Value)
                break
            case case2Value:
                // worked
                break
            case case3Value:
                failNotEquals(value, case3Value)
                break
            default:
                failNotDefault(value)
                break
        }
    }

    void assertSwitchMatch3(value, case1Value, case2Value, case3Value) {
        switch (value) {
            case case1Value:
                failNotEquals(value, case1Value)
                break
            case case2Value:
                failNotEquals(value, case2Value)
                break
            case case3Value:
                // worked
                break
            default:
                failNotDefault(value)
                break
        }
    }

    void assertSwitchMatchDefault(value, case1Value, case2Value, case3Value) {
        switch (value) {
            case case1Value:
                failNotEquals(value, case1Value)
                break
            case case2Value:
                failNotEquals(value, case2Value)
                break
            case case3Value:
                failNotEquals(value, case3Value)
                break
            default:
                // worked
                break
        }
    }

    void failNotEquals(value, expectedCaseValue) {
        fail("value: " + value + " is not equal to case value: " + expectedCaseValue)
    }

    void failNotDefault(value) {
        fail("value: " + value + " should not match the default switch clause")
    }
}
