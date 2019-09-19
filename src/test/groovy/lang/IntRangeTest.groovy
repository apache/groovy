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
package groovy.lang

import groovy.test.GroovyTestCase;

/**
 * Provides unit tests for the <code>IntRange</code> class.
 */
class IntRangeTest extends GroovyTestCase {

    void testCreateTooBigRange() {
        try {
            assert new IntRange(1, Integer.MAX_VALUE).size() == Integer.MAX_VALUE // biggest allowed
            new IntRange(0, Integer.MAX_VALUE) // too big
            fail("too large range accepted")
        }
        catch (IllegalArgumentException ignore) {
            assert ignore.message == 'A range must have no more than 2147483647 elements but attempted 2147483648 elements'
        }
    }

    /**
     * Tests providing invalid arguments to the protected constructor.
     */
    void testInvalidArgumentsToConstructor() {
        try {
            new IntRange(2, 1, true)
            fail("invalid range created")
        }
        catch (IllegalArgumentException ignore) {
            assertTrue("expected exception thrown", true)
        }
    }

    void testSizeEdgeCases() {
        assert new IntRange(false, 0, 0).size() == 0
        assert new IntRange(true, 0, 0).size() == 1
        assert new IntRange(false, 0, 1).size() == 1
        assert new IntRange(true, 0, 1).size() == 2
    }

    /**
     * Tests getting the to and from values as <code>int</code>s.
     */
    void testGetToFromInt() {
        final int from = 3, to = 7
        final IntRange range = new IntRange(from, to)
        assertEquals("wrong 'from'", from, range.getFromInt())
        assertEquals("wrong 'to'", to, range.getToInt())
    }

    void test_Step_ShouldNotOverflowForIntegerMaxValue() {
        (Integer.MAX_VALUE..Integer.MAX_VALUE).step(1) {
            assert it == Integer.MAX_VALUE
        }
    }

    void test_Step_ShouldNotOverflowForIntegerMinValue() {
        (Integer.MIN_VALUE..Integer.MIN_VALUE).step(-1) {
            assert it == Integer.MIN_VALUE
        }
    }

    void test_Step_ShouldNotOverflowForBigSteps(){
        (0..2000000000).step(1000000000) {
            assert it >= 0
        }

        (0..-2000000000).step(-1000000000) {
            assert it <= 0
        }
    }

    void testInclusiveRangesWithNegativesAndPositives() {
        final a = [1, 2, 3, 4]
        assert a[-3..-2] == [2, 3]
        assert a[-3..<-2] == [2]
        assert a[2..-3] == [3, 2]
        assert a[1..-1] == [2, 3, 4]
        assert a[1..<-1] == [2, 3]
        assert a[-2..<1] == [3]
        assert a[-2..<-3] == [3]
        assert a[5..<5] == []
        assert a[-5..<-5] == []
    }

    void testInclusiveRangesWithNegativesAndPositivesStrings() {
        def items = 'abcde'
        assert items[1..-2]   == 'bcd'
        assert items[1..<-2]  == 'bc'
        assert items[-3..<-2] == 'c'
        assert items[-2..-4]  == 'dcb'
        assert items[-2..<-4] == 'dc'
        assert items[2..<2] == ''
        assert items[-2..<-2] == ''
    }

    void testInclusiveRangesWithNegativesAndPositivesPrimBoolArray() {
        boolean[] bs = [true, false, true, true]
        assert bs[-3..-2]  == [false, true]
        assert bs[-3..<-2] == [false]
        assert bs[2..-3]   == [true, false]
        assert bs[1..-1]   == [false, true, true]
        assert bs[1..<-1]  == [false, true]
        assert bs[-2..<1]  == [true]
        assert bs[-2..<-3] == [true]
    }

    void testInclusiveRangesWithNegativesAndPositivesBitset() {
        int bits = 0b100001110100010001111110
        int numBits = 24
        def bs = new BitSet()
        numBits.times{ index -> bs[index] = (bits & 0x1).asBoolean(); bits >>= 1 }
        bs[3..5] = false
        assert bs.toString() == '{1, 2, 6, 10, 14, 16, 17, 18, 23}'
        assert bs[bs.length()-1] == true
        assert bs[-1] == true
        assert bs[6..17].toString() == '{0, 4, 8, 10, 11}'
        assert bs[6..<17].toString() == '{0, 4, 8, 10}'
        assert bs[17..6].toString() == '{0, 1, 3, 7, 11}'
        assert bs[17..<6].toString() == '{0, 1, 3, 7}'
        assert bs[-1..-7].toString() == '{0, 5, 6}'
        assert bs[-1..<-7].toString() == '{0, 5}'
        assert bs[20..<-8].toString() == '{2, 3}'
    }

    void testHashCode(){
        def maxRange = new IntRange(1,Integer.MAX_VALUE)
        def rangeWithName = [:]
        rangeWithName.put(maxRange, "maxRange")
        def dupRange = new IntRange(1,Integer.MAX_VALUE)
        assertEquals(rangeWithName.get(dupRange), "maxRange")
    }

    // GROOVY-8704
    void testSerialization() {
        def baos = new ByteArrayOutputStream()
        baos.withObjectOutputStream { oos -> oos.writeObject([4..1, 2..<5]) }
        def bais = new ByteArrayInputStream(baos.toByteArray())
        bais.withObjectInputStream { ois -> assert ois.readObject() == [4..1, 2..<5] }
    }

}
