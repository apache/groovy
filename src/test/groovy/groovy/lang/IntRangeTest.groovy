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
import groovy.transform.CompileStatic

/**
 * Provides unit tests for the <code>IntRange</code> class.
 */
final class IntRangeTest extends GroovyTestCase {

    void testCreateTooBigRange() {
        assert new IntRange(1, Integer.MAX_VALUE).size() == Integer.MAX_VALUE // biggest allowed
        try {
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
        assert new IntRange(true, true, 0, 0).size() == 1
        assert new IntRange(false, true, 0, 0).size() == 0
        assert new IntRange(false, true, 0, 1).size() == 1
        assert new IntRange(false, false, 0, 0).size() == 0
        assert new IntRange(false, false, 0, 1).size() == 0
    }

    void testSubListBorders() {
        // reminder: RangeInfo stores "to+1" for direct use in places like List#subList and CharSequence#subSequence
        new IntRange(true, true, 1, 5).subListBorders(-1).with{ assert it.from == 1 && it.to == 6 && !it.reverse }
        new IntRange(false, true, 1, 5).subListBorders(-1).with{ assert it.from == 2 && it.to == 6 && !it.reverse }
        new IntRange(true, false, 1, 5).subListBorders(-1).with{ assert it.from == 1 && it.to == 5 && !it.reverse }
        new IntRange(false, false, 1, 5).subListBorders(-1).with{ assert it.from == 2 && it.to == 5 && !it.reverse }
        new IntRange(true, true, 5, 1).subListBorders(-1).with{ assert it.from == 1 && it.to == 6 && it.reverse }
        new IntRange(false, true, 5, 1).subListBorders(-1).with{ assert it.from == 1 && it.to == 5 && it.reverse }
        new IntRange(true, false, 5, 1).subListBorders(-1).with{ assert it.from == 2 && it.to == 6 && it.reverse }
        new IntRange(false, false, 5, 1).subListBorders(-1).with{ assert it.from == 2 && it.to == 5 && it.reverse }
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
        assert a[-3..-2]   == [2, 3]
        assert a[-3..<-2]  == [2]
        assert a[-3<..2]   == [3]
        assert a[-3<..<-2] == []

        assert a[2..-3]    == [3, 2]

        assert a[1..-1]    == [2, 3, 4]
        assert a[1..<-1]   == [2, 3]
        assert a[1<..-1]   == [3, 4]
        assert a[1<..<-1]  == [3]

        assert a[-2..<1]   == [3]
        assert a[-2<..1]   == [2]
        assert a[-2<..<1]  == []

        assert a[-2..<-3]  == [3]
        assert a[-2<..-3]  == [2]
        assert a[-2<..<-3] == []

        assert a[5..<5]    == []
        assert a[5<..5]    == []
        assert a[5<..<5]   == []
        assert a[5<..<6]   == []
        assert a[-5..<-5]  == []
    }

    void testInclusiveRangesWithNegativesAndPositivesStrings() {
        def items = 'abcde'
        assert items[1..-2]    == 'bcd'
        assert items[1..<-2]   == 'bc'
        assert items[1<..-2]   == 'cd'
        assert items[1<..<-2]  == 'c'

        assert items[-3..<-2]  == 'c'
        assert items[-3<..-2]  == 'd'
        assert items[-3<..<-2] == ''

        assert items[-2..-4]   == 'dcb'
        assert items[-2..<-4]  == 'dc'
        assert items[-2<..-4]  == 'cb'
        assert items[-2<..<-4] == 'c'

        assert items[2..<2]    == ''
        assert items[2<..2]    == ''
        assert items[2<..<2]   == ''
        assert items[2<..<3]   == ''

        assert items[-2..<-2]  == ''
        assert items[-2<..-2]  == ''
        assert items[-2<..<-2] == ''
        assert items[-2<..<-3] == ''
    }

    void testInclusiveRangesWithNegativesAndPositivesPrimBoolArray() {
        boolean[] bs = [true, false, true, true]
        assert bs[-3..-2]   == [false, true]
        assert bs[-3..<-2]  == [false]
        assert bs[-3<..-2]  == [true]
        assert bs[-3<..<-2] == []

        assert bs[2..-3]    == [true, false]

        assert bs[1..-1]    == [false, true, true]
        assert bs[1..<-1]   == [false, true]
        assert bs[1<..-1]   == [true, true]
        assert bs[1<..<-1]  == [true]

        assert bs[-2..<1]   == [true]
        assert bs[-2<..1]   == [false]
        assert bs[-2<..<1]  == []

        assert bs[-2..<-3]  == [true]
        assert bs[-2<..-3]  == [false]
        assert bs[-2<..<-3] == []
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

        assert bs[6..17].toString()    == '{0, 4, 8, 10, 11}'
        assert bs[6..<17].toString()   == '{0, 4, 8, 10}'
        assert bs[6<..17].toString()   == '{3, 7, 9, 10}'
        assert bs[6<..<17].toString()  == '{3, 7, 9}'

        assert bs[17..6].toString()    == '{0, 1, 3, 7, 11}'
        assert bs[17..<6].toString()   == '{0, 1, 3, 7}'
        assert bs[17<..6].toString()   == '{0, 2, 6, 10}'
        assert bs[17<..<6].toString()  == '{0, 2, 6}'

        assert bs[-1..-7].toString()   == '{0, 5, 6}'
        assert bs[-1..<-7].toString()  == '{0, 5}'
        assert bs[-1<..-7].toString()  == '{4, 5}'
        assert bs[-1<..<-7].toString() == '{4}'

        assert bs[20..<-8].toString()  == '{2, 3}'
        assert bs[20<..-8].toString()  == '{1, 2, 3}'
        assert bs[20<..<-8].toString() == '{1, 2}'
    }

    // GROOVY-8704
    void testSerialization() {
        def baos = new ByteArrayOutputStream()
        baos.withObjectOutputStream { oos -> oos.writeObject([4..1, 2..<5]) }
        def bais = new ByteArrayInputStream(baos.toByteArray())
        bais.withObjectInputStream { ois -> assert ois.readObject() == [4..1, 2..<5] }
    }

    void testHashCode() {
        def maxRange = new IntRange(1,Integer.MAX_VALUE)
        def rangeWithName = [:]
        rangeWithName.put(maxRange, "maxRange")
        def dupRange = new IntRange(1,Integer.MAX_VALUE)
        assertEquals(rangeWithName.get(dupRange), "maxRange")
    }

    void testEquals() {
        IntRange r1 = new IntRange(0, 10)
        IntRange r2 = new IntRange(0, 10)
        assert r1.equals(r2)
        assert r2.equals(r1)

        r1 = new IntRange(true, false, 0, 10)
        r2 = new IntRange(true, false, 0, 10)
        assert r1.equals(r2)
        assert r2.equals(r1)

        r1 = new IntRange(false, 1, 11)
        r2 = new IntRange(1, 10)
        assert !r1.equals(r2)
        assert !r2.equals(r1)

        r1 = new IntRange(false, 1, 10)
        r2 = new IntRange(1, 10)
        assert !r1.equals(r2)
        // As before GROOVY-9649
        assert r2.equals(r1)

        r1 = new IntRange(false, true, -1, 10)
        r2 = new IntRange(1, 10)
        assert !r1.equals(r2)
        assert !r2.equals(r1)

        r1 = new IntRange(true, true, 10, 0)
        r2 = new IntRange(0, 10, true)
        assert !r1.equals(r2)
        assert !r2.equals(r1)

        r1 = new IntRange(0, 10, true)
        r2 = new IntRange(0, 10, false)
        assert !r1.equals(r2)
        assert !r2.equals(r1)
    }

    // GROOVY-10496
    void testBy() {
        IntRange ir = new IntRange(5, 10)
        assert ir == [5, 6, 7, 8, 9, 10]
        assert ir.step(2) == [5, 7, 9]

        NumberRange nr = ir.by(2)
        assert nr == [5, 7, 9]

        ir = new IntRange(false, false, 5, 10)
        assert ir == [6, 7, 8, 9]

        nr = ir.by(2)
        nr == [6, 8, 10]
    }

    // GROOVY-10496
    @CompileStatic
    void testSC() {
        IntRange ir = 1<..<5
        assert ir == [2, 3, 4]
    }
}
