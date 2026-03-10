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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.fail

class IntegerOperatorsTest {

    def x
    def y
    def z

    @Test
    void testPlus() {
        x = 2 + 2
        assert x == 4

        y = x + 1
        assert y == 5

        z = y + x + 1 + 2
        assert z == 12
    }

    @Test
    void testUnaryPlus() {
        x = 3
        y = +x
        assert y == 3
        y = x.unaryPlus()
        assert y == 3
    }

    @Test
    void testCharacterPlus() {
        Character c1 = 1
        Character c2 = 2

        x = c2 + 2
        assert x == 4

        x = 2 + c2
        assert x == 4

        x = c2 + c2
        assert x == 4

        y = x + c1
        assert y == 5

        y = c1 + x
        assert y == 5

        z = y + x + c1 + 2
        assert z == 12

        z = y + x + 1 + c2
        assert z == 12

        z = y + x + c1 + c2
        assert z == 12
    }

    @Test
    void testMinus() {
        x = 6 - 2
        assert x == 4

        y = x - 1
        assert y == 3
    }

    @Test
    void testUnaryMinus() {
        x = 3
        y = -x
        assert y == -3
        y = x.unaryMinus()
        assert y == -3
    }

    @Test
    void testBitwiseNegate() {
        x = 3
        y = ~x
        assert y == -4
        y = x.bitwiseNegate()
        assert y == -4
    }

    @Test
    void testCharacterMinus() {
        Character c1 = 1
        Character c2 = 2
        Character c6 = 6

        x = c6 - 2
        assert x == 4

        x = 6 - c2
        assert x == 4

        x = c6 - c2
        assert x == 4

        y = x - c1
        assert y == 3
    }

    @Test
    void testMultiply() {
        x = 3 * 2
        assert x == 6

        y = x * 2
        assert y == 12
    }

    @Test
    void testDivide() {
        x = 80 / 4
        assert x == 20.0 , "x = " + x

        y = x / 2
        assert y == 10.0 , "y = " + y
    }

    @Test
    void testIntegerDivide() {
        x = 52.intdiv(3)
        assert x == 17 , "x = " + x

        y = x.intdiv(2)
        assert y == 8 , "y = " + y

        y = 11
        y = y.intdiv(3)
        assert y == 3
    }

    @Test
    void testMod() {
        x = 100.mod(3)

        assert x == 1

        y = 11
        y = y.mod(3)
        assert y == 2
        y = -11
        y = y.mod(3)
        assert y == 1
    }

    @Test
    void testRemainder() {
        x = 100 % 3

        assert x == 1

        y = 11
        y %= 3
        assert y == 2
        y = -11
        y %= 3
        assert y == -2
    }

    @Test
    void testAnd() {
        x = 1 & 3

        assert x == 1

        x = 1.and(3)

        assert x == 1
    }

     @Test
     void testOr() {
         x = 1 | 3

         assert x == 3

         x = 1 | 4

         assert x == 5

         x = 1.or(3)

         assert x == 3

         x = 1.or(4)

         assert x ==5
    }

    @Test
    void testShiftOperators() {

        x = 8 >> 1
        assert x == 4
        assert x instanceof Integer

        x = 8 << 2
        assert x == 32
        assert x instanceof Integer

        x = 8L << 2
        assert x == 32
        assert x instanceof Long

        x = -16 >> 4
        assert x == -1

        x = -16 >>> 4
        assert x == 0xFFFFFFF

        //Ensure that the type of the right operand (shift distance) is ignored when calculating the
        //result.  This is how java works, and for these operators, it makes sense to keep that behavior.
        x = Integer.MAX_VALUE << 1L
        assert x == -2
        assert x instanceof Integer

        x = new Long(Integer.MAX_VALUE).longValue() << 1
        assert x == 0xfffffffe
        assert x instanceof Long

        //The left operand (shift value) must be an integral type
        try {
            x = 8.0F >> 2
            fail("Should catch UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }

        //The right operand (shift distance) must be an integral type
        try {
            x = 8 >> 2.0
            fail("Should catch UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }
    }

    @Test
    void testCompareBoxing() {
        assertScript """
            def proceed(){}
            def checkResponse() {return null}

            Integer responseCode = checkResponse()
            if (responseCode == 200) {
                proceed()
            }
        """
    }

    @Test
    void testCompareBoxingWithEMC() {
        try {
            assertScript """
                def proceed(){}
                def checkResponse() {return null}

                Integer.metaClass.foo = {1}
                Integer responseCode = checkResponse()
                if (responseCode == 200) {
                    proceed()
                }
            """
        } finally {
            Integer.metaClass = null
        }
    }

}
