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
package org.codehaus.groovy.runtime.typehandling

import groovy.test.GroovyTestCase

import static org.codehaus.groovy.runtime.typehandling.NumberMath.getMath

/**
 * Basic NumberMath test.
 * @see org.codehaus.groovy.runtime.typehandling.NumberMath
 */
class NumberMathTest extends GroovyTestCase {

    void testPromotions() {
        def C = '1'.toCharacter()
        def B = new Byte("1")
        def I = new Integer(1)
        def L = new Long(1)
        def F = new Float("1.0")
        def D = new Double("1.0")
        def BI = new BigInteger("1")
        def BD = new BigDecimal("1.0")

        //+, -, and * all promote the same way, so sample the matrix
        assert C + B instanceof Integer
        assert C - BD instanceof BigDecimal
        assert B + C instanceof Integer
        assert B + I instanceof Integer
        assert B + F instanceof Double

        assert I + I instanceof Integer
        assert I - F instanceof Double
        assert I * D instanceof Double
        assert I + BI instanceof BigInteger
        assert I - BD instanceof BigDecimal

        assert F * L instanceof Double
        assert D + L instanceof Double
        assert BI - L instanceof BigInteger
        assert BD * L instanceof BigDecimal

        assert F + F instanceof Double
        assert F - BI instanceof Double
        assert F * BD instanceof Double

        assert F + D instanceof Double
        assert BI - D instanceof Double
        assert BD * D instanceof Double

        assert BI + BI instanceof BigInteger
        assert BD - BI instanceof BigDecimal
        assert BD * BD instanceof BigDecimal

        //Division (/) promotes differently so change the expected results:
        assert I / I instanceof BigDecimal
        assert I / F instanceof Double
        assert I / D instanceof Double
        assert I / BI instanceof BigDecimal
        assert I / BD instanceof BigDecimal

        assert F / L instanceof Double
        assert D / L instanceof Double
        assert BI / L instanceof BigDecimal
        assert BD / L instanceof BigDecimal

        assert F / F instanceof Double
        assert F / BI instanceof Double
        assert F / BD instanceof Double

        assert F / D instanceof Double
        assert BI / D instanceof Double
        assert BD / D instanceof Double

        assert BI / BI instanceof BigDecimal
        assert BD / BI instanceof BigDecimal
        assert BD / BD instanceof BigDecimal
    }

    void testOperations() {
        def I1 = new Integer(1)
        def I2 = new Integer(2)
        def I3 = new Integer(3)
        def L1 = new Long(1)
        def L2 = new Long(2)
        def L3 = new Long(3)
        def F1 = new Float("1.0")
        def F2 = new Float("2.0")
        def D1 = new Double("1.0")
        def D2 = new Double("2.0")
        def BI1 = new BigInteger("1")
        def BI2 = new BigInteger("2")
        def BD1 = new BigDecimal("1.0")
        def BD2 = new BigDecimal("2.0")
        def BD20 = new BigDecimal("2.00")

        assert I1 / I2 instanceof BigDecimal
        assert I1 / I2 == new BigDecimal("0.5")
        assert F1 / F2 instanceof Double
        assertEquals F1 / F2, 0.5, 0.0000000001
        assert D1 / D2 instanceof Double
        assertEquals D1 / D2, 0.5, 0.0000000001

        assert I1.intdiv(I2) instanceof Integer
        assert I1.intdiv(I2) == 0

        assert I3.intdiv(I2) instanceof Integer
        assert I3.intdiv(I2) == 1

        assert L1.intdiv(I2) instanceof Long
        assert L1.intdiv(I2) == 0

        assert L3.intdiv(L2) instanceof Long
        assert L3.intdiv(L2) == 1

        assert BI1.intdiv(BI2) instanceof BigInteger
        assert BI1.intdiv(BI2) == 0

        assert I1 / I3 instanceof BigDecimal
        assert I1 / I3 == new BigDecimal("0.3333333333")

        assert I2 / I3 instanceof BigDecimal
        assert I2 / I3 == new BigDecimal("0.6666666667")

        assert I1 / BD2 instanceof BigDecimal

        //Test keeping max scale of (L, R or 10)
        def BBD1 = new BigDecimal("0.12345678901234567")
        assert BD1 + BBD1 == new BigDecimal("1.12345678901234567")

        def BBD2 = new BigDecimal(".000000000000000008")
        assert BBD1 + BBD2 == new BigDecimal("0.123456789012345678")
    }

    void testUnsupportedIntDivision() {
        try {
            1.0.intdiv(3)
        } catch (UnsupportedOperationException ignore) {
            return
        }
        fail("Should catch an UnsupportedOperationException")

        try {
            1.0G.intdiv(3)
        } catch (UnsupportedOperationException ignore) {
            return
        }
        fail("Should catch an UnsupportedOperationException")
    }


    private static class MyNumber extends Number {
        def n

        MyNumber(n) {
            this.n = n
        }

        int intValue() { n }

        long longValue() { n }

        float floatValue() { n }

        double doubleValue() { n }

        int hashCode() { -n }

        boolean equals(other) {
            if (other instanceof MyNumber) {
                return n == other.n
            }
            return false
        }

        String toString() { return Double.toString(floatValue()) }
    }

    void testGetMathCustom() {
        assert getMath(1G) == BigIntegerMath.INSTANCE;
        assert getMath(1.0G) == BigDecimalMath.INSTANCE;
        assert getMath(Long.valueOf(1)) == LongMath.INSTANCE;
        assert getMath(Integer.valueOf(1)) == IntegerMath.INSTANCE;
        assert getMath(Short.valueOf("1")) == IntegerMath.INSTANCE;
        assert getMath(Byte.valueOf("1")) == IntegerMath.INSTANCE;
        assert getMath(Double.valueOf(1.0)) == FloatingPointMath.INSTANCE;
        assert getMath(Float.valueOf(1.0f)) == FloatingPointMath.INSTANCE;

        MyNumber num = new MyNumber(42);
        assert getMath(num) == BigDecimalMath.INSTANCE;

        assert getMath(num, Integer.valueOf(25)) == BigDecimalMath.INSTANCE;
        assert getMath(num, Double.valueOf(25.0)) == FloatingPointMath.INSTANCE;
        assert getMath(Integer.valueOf(25), num) == BigDecimalMath.INSTANCE;
        assert getMath(Double.valueOf(25.0), num) == FloatingPointMath.INSTANCE;
        assert getMath(num, 25) == BigDecimalMath.INSTANCE;
        assert getMath(25, num) == BigDecimalMath.INSTANCE;
    }

    void testGetMath() {
        assert 20 == new Short("10") << 1
        assert 2 == new Byte("1") << 1
    }

    void testLongDivAssign() {
        long d = 100L
        d /= 33L
        assert d.class == Long.class
    }

    void testIntegerPlusCastException() {
        shouldFail(ClassCastException) {
            Integer i = 12
            i += " angry men"
        }
    }
}
