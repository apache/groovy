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
package groovy.operator

import groovy.test.GroovyTestCase

/**
 * Test Math Power Operation in Classic/New Groovy
 */
class PowerOperatorsTest extends GroovyTestCase {

    void testConstantPowerOperation() {
        assert 2**5 == 32
        assert -2**5 == -32
        assert 3**4 == 81
        assert -3**4 == -81
        assert 3**-4 == 3.power(-4)
        assert -3**-4 == -3.power(-4)
        assert 7**2 - 7 * 3 + 2 == 30         //  49 - 21 + 2 = 30
        assert -7**2 - 7 * 3 + 2 == -68       // -49 - 21 + 2 = -68
        assert -(7**2) - 7 * 3 + 2 == -68     // -49 - 21 + 2 = -68
        assert (-7)**2 - 7 * 3 + 2 == 30     //  49 - 21 + 2 = 30
    }

    void testPowerOperation() {
        def x = 9
        --x
        assert x == 8
        println(--x)
        assert x == 7
        println(--x)
        assert x == 6
        println((--x)**3)
        assert x == 5
        assert (--x)**3 == 64
        assert (-x**3) == -64
        assert x == 4
        assert (++x)**3 == 125
        assert x == 5
        assert (x++)**3 == 125
        assert x == 6
        println((x++)**3)
        assert x == 7
        println(x)
        println("${x**2}")
        println("${-x**2}")
        assert x == 7
        println("${(--x)**2}")
        assert x == 6
        assert (--x)**2 + x * 2 - 1 == 34      // 5**2 + 5*2 - 1 = 34
        assert x == 5
        assert (x--)**2 + x * 2 - 1 == 32      // 5**2 + 4*2 - 1 = 32
        assert x == 4
    }

    void testConstantPowerAssignmentOperation() {
        def x = 5
        x **= 2
        assert x == 25
        assert x**2 == 625
        assert -x**2 != 625
        assert -x**2 == -625
    }

    void testPowerAssignmentOperation() {
        def x = 5
        def y = 2
        x **= y
        assert x == 25
        assert x**y == 625
        assert x**-1 == 1 / 25
        assert x**-y == 1 / 625
        assert x**-y == x**(-y)
    }

    void testPowerConversions() {
        assert (2**5).class == Integer
        assert (2l**5).class == Long
        assert (2.0d**5).class == Integer
        assert (2.1d**5).class == Double
        assert (new BigInteger("2")**5).class == BigInteger
        assert (new BigDecimal("2")**5).class == BigDecimal

        assert (2**-1).class == Double
        assert (2l**-1).class == Double
        assert (2.0d**-1).class == Double
        assert (new BigInteger("2")**-1).class == Double
        assert (new BigDecimal("2")**-1).class == Double

        assert (2**31).class == BigInteger
        assert (2l**63).class == BigInteger
        assert (2**31) == new BigInteger("2").pow(31)
        assert (2l**63) == new BigInteger("2").pow(63)
    }
}