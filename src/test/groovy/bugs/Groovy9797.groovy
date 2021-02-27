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

/*
 * About bug fix:
 * According to the IEEE-754 floating point standard, the sign of a negative zero
 * must be preserved. However, when Groovy compiles code, it uses the ==
 * operator to compare floats and uses the special constant instruction for
 * (positive) zero if the float is equal to zero. The == operator does not
 * differentiate between positive and negative zero, which complies with the
 * standard. This fix uses Float.equals which can differentiate between positive
 * and negative zero.
 */

class Groovy9797 extends GroovyTestCase {
    // Test with string conversion
    void testFloatToString() {
        float negativeZero = -0.0f
        float positiveZero = 0.0f
        assertToString(negativeZero, '-0.0')
        assertToString(positiveZero, '0.0')
    }

    // Test with int bits
    void testNegativePositiveZeroFloatIntBitsNotSame() {
        int negativeZeroBits = Float.floatToIntBits(-0.0f)
        int positiveZeroBits = Float.floatToIntBits(0.0f)
        assertNotSame(negativeZeroBits, positiveZeroBits)
    }

    // Test with string conversion
    void testDoubleToString() {
        double negativeZero = -0.0d
        double positiveZero = 0.0d
        assertToString(negativeZero, '-0.0')
        assertToString(positiveZero, '0.0')
    }

    // Test with long bits
    void testNegativePositiveZeroDoubleLongBitsNotSame() {
        long negativeZeroBits = Double.doubleToLongBits(-0.0d)
        long positiveZeroBits = Double.doubleToLongBits(0.0d)
        assertNotSame(negativeZeroBits, positiveZeroBits)
    }
}
