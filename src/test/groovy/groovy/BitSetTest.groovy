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

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertTrue


class BitSetTest{

    @Test
    void testSubscriptOperator() {
        def bitSet = new BitSet()

        bitSet[2] = true
        bitSet[3] = true

        assertBitFalse bitSet, 0
        assertBitFalse bitSet, 1
        assertBitTrue  bitSet, 2
        assertBitTrue  bitSet, 3
        assertBitFalse bitSet, 4
    }

    @Test
    void testSubscriptAssignmentWithRange() {
        def bitSet = new BitSet()

        bitSet[2..4] = true

        assertBitFalse bitSet, 0
        assertBitFalse bitSet, 1
        assertBitTrue  bitSet, 2
        assertBitTrue  bitSet, 3
        assertBitTrue  bitSet, 4
        assertBitFalse bitSet, 5
    }

    @Test
    void testSubscriptAssignmentWithReverseRange() {
        def bitSet = new BitSet()

        bitSet[4..2] = true

        assertBitFalse bitSet, 0
        assertBitFalse bitSet, 1
        assertBitTrue  bitSet, 2
        assertBitTrue  bitSet, 3
        assertBitTrue  bitSet, 4
        assertBitFalse bitSet, 5
    }

    @Test
    void testSubscriptAccessWithRange() {
        def bitSet = new BitSet()

        bitSet[7] = true
        bitSet[11] = true

        def subSet = bitSet[5..11]

        assertTrue subSet instanceof BitSet, 'subSet should have been a BitSet'

        assertNotSame bitSet, subSet, 'subSet should not have been the same object'

        // the last true bit should be at index 6
        assertEquals 7, subSet.length(), 'result had wrong logical size'

        assertBitFalse subSet, 0
        assertBitFalse subSet, 1
        assertBitTrue  subSet, 2
        assertBitFalse subSet, 3
        assertBitFalse subSet, 4
        assertBitFalse subSet, 5
        assertBitTrue  subSet, 6
    }

    @Test
    void testSubscriptAccessWithReverseRange() {
        def bitSet = new BitSet()

        bitSet[3] = true
        bitSet[4] = true

        def subSet = bitSet[8..2]

        assertTrue subSet instanceof BitSet, 'subSet should have been a BitSet'

        assertNotSame bitSet, subSet, 'subSet should not have been the same object'

        // the last true bit should be at index 5
        assertEquals 6, subSet.length(), 'result had wrong logical size'

        assertBitFalse subSet, 0
        assertBitFalse subSet, 1
        assertBitFalse subSet, 2
        assertBitFalse subSet, 3
        assertBitTrue  subSet, 4
        assertBitTrue  subSet, 5
        assertBitFalse subSet, 6
    }

    @Test
    void testAnd() {
        def a = new BitSet()
        a[2] = true
        a[3] = true
        def b = new BitSet()
        b[1] = true
        b[3] = true
        def c = a & b
        assertBitFalse c, 0
        assertBitFalse c, 1
        assertBitFalse c, 2
        assertBitTrue  c, 3
    }

    @Test
    void testOr() {
        def a = new BitSet()
        a[2] = true
        a[3] = true
        def b = new BitSet()
        b[1] = true
        b[3] = true
        def c = a | b
        assertBitFalse c, 0
        assertBitTrue  c, 1
        assertBitTrue  c, 2
        assertBitTrue  c, 3
    }

    @Test
    void testXor() {
        def a = new BitSet()
        a[2] = true
        a[3] = true
        def b = new BitSet()
        b[1] = true
        b[3] = true
        def c = a ^ b
        assertBitFalse c, 0
        assertBitTrue  c, 1
        assertBitTrue  c, 2
        assertBitFalse c, 3
    }

    @Test
    void testBitwiseNegate() {
        def a = new BitSet()
        a[2] = true
        a[3] = true
        def b = ~a
        assertBitTrue  b, 0
        assertBitTrue  b, 1
        assertBitFalse b, 2
        assertBitFalse b, 3
    }

    @Test
    void testLeftShift() {
        def testCases = [
            [1, -12L],
            [3, 271],
            [1, (Long.MAX_VALUE/2 - 10).longValue()],
            [1, (Long.MAX_VALUE/2 + 10).longValue()]
        ]
        testCases.each { int operand, long value ->
            def a = BitSet.valueOf(value)
            def b = BitSet.valueOf(value << operand)
            def c = BitSet.valueOf(value >> operand)
            def d = BitSet.valueOf(value >>> operand)
            assert b == a << operand
            assert c == a >> operand
            assert d == a >>> operand
        }
    }

    private assertBitTrue(bitset, index) {
        assertTrue  bitset[index], 'index ' + index + ' should have been true'
    }

    private assertBitFalse(bitset, index) {
        assertFalse bitset[index], 'index ' + index + ' should have been false'
    }
}
