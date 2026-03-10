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
package org.codehaus.groovy.util

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

final class HashCodeHelperTest {

    @Test
    void testUpdateHash() {
        assert 158 == HashCodeHelper.updateHash(1, new Character('c' as char))
        assert 59  == HashCodeHelper.updateHash(1, (Character) null)
    }

    @Test
    void testInitHash() {
        def hash = HashCodeHelper.initHash()
        // The initial hash should be the SEED value (127)
        assertEquals(127, hash)
    }

    @Test
    void testUpdateHashWithBoolean() {
        def hash = HashCodeHelper.initHash()

        def hashTrue = HashCodeHelper.updateHash(hash, true)
        def hashFalse = HashCodeHelper.updateHash(hash, false)

        // true and false should produce different hashes
        assertNotEquals(hashTrue, hashFalse)

        // Same value should produce same hash
        assertEquals(hashTrue, HashCodeHelper.updateHash(hash, true))
        assertEquals(hashFalse, HashCodeHelper.updateHash(hash, false))
    }

    @Test
    void testUpdateHashWithChar() {
        def hash = HashCodeHelper.initHash()

        def hashA = HashCodeHelper.updateHash(hash, (char) 'a')
        def hashB = HashCodeHelper.updateHash(hash, (char) 'b')

        assertNotEquals(hashA, hashB)
        assertEquals(hashA, HashCodeHelper.updateHash(hash, (char) 'a'))
    }

    @Test
    void testUpdateHashWithCharacter() {
        def hash = HashCodeHelper.initHash()

        def hashChar = HashCodeHelper.updateHash(hash, Character.valueOf((char) 'x'))
        def hashNull = HashCodeHelper.updateHash(hash, (Character) null)

        assertNotEquals(hashChar, hashNull)
        // Null should be treated as 0
        assertEquals(HashCodeHelper.updateHash(hash, (char) 0), hashNull)
    }

    @Test
    void testUpdateHashWithInt() {
        def hash = HashCodeHelper.initHash()

        def hash1 = HashCodeHelper.updateHash(hash, 42)
        def hash2 = HashCodeHelper.updateHash(hash, 43)

        assertNotEquals(hash1, hash2)
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 42))
    }

    @Test
    void testUpdateHashWithInteger() {
        def hash = HashCodeHelper.initHash()

        def hashInt = HashCodeHelper.updateHash(hash, Integer.valueOf(100))
        def hashNull = HashCodeHelper.updateHash(hash, (Integer) null)

        assertNotEquals(hashInt, hashNull)
        assertEquals(HashCodeHelper.updateHash(hash, 0), hashNull)
    }

    @Test
    void testUpdateHashWithLong() {
        def hash = HashCodeHelper.initHash()

        def hash1 = HashCodeHelper.updateHash(hash, 123456789L)
        def hash2 = HashCodeHelper.updateHash(hash, 987654321L)

        assertNotEquals(hash1, hash2)
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 123456789L))
    }

    @Test
    void testUpdateHashWithLongObject() {
        def hash = HashCodeHelper.initHash()

        def hashLong = HashCodeHelper.updateHash(hash, Long.valueOf(999L))
        def hashNull = HashCodeHelper.updateHash(hash, (Long) null)

        assertNotEquals(hashLong, hashNull)
        assertEquals(HashCodeHelper.updateHash(hash, 0L), hashNull)
    }

    @Test
    void testUpdateHashWithFloat() {
        def hash = HashCodeHelper.initHash()

        def hash1 = HashCodeHelper.updateHash(hash, 3.14f)
        def hash2 = HashCodeHelper.updateHash(hash, 2.71f)

        assertNotEquals(hash1, hash2)
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 3.14f))
    }

    @Test
    void testUpdateHashWithFloatObject() {
        def hash = HashCodeHelper.initHash()

        def hashFloat = HashCodeHelper.updateHash(hash, Float.valueOf(1.5f))
        def hashNull = HashCodeHelper.updateHash(hash, (Float) null)

        assertNotEquals(hashFloat, hashNull)
        assertEquals(HashCodeHelper.updateHash(hash, 0f), hashNull)
    }

    @Test
    void testUpdateHashWithDouble() {
        def hash = HashCodeHelper.initHash()

        def hash1 = HashCodeHelper.updateHash(hash, 3.14159265d)
        def hash2 = HashCodeHelper.updateHash(hash, 2.71828182d)

        assertNotEquals(hash1, hash2)
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 3.14159265d))
    }

    @Test
    void testUpdateHashWithDoubleObject() {
        def hash = HashCodeHelper.initHash()

        def hashDouble = HashCodeHelper.updateHash(hash, Double.valueOf(99.99d))
        def hashNull = HashCodeHelper.updateHash(hash, (Double) null)

        assertNotEquals(hashDouble, hashNull)
        assertEquals(HashCodeHelper.updateHash(hash, 0d), hashNull)
    }

    @Test
    void testUpdateHashWithObject() {
        def hash = HashCodeHelper.initHash()

        def hashStr1 = HashCodeHelper.updateHash(hash, (Object) "hello")
        def hashStr2 = HashCodeHelper.updateHash(hash, (Object) "world")
        def hashNull = HashCodeHelper.updateHash(hash, (Object) null)

        assertNotEquals(hashStr1, hashStr2)
        assertNotEquals(hashStr1, hashNull)
    }

    @Test
    void testUpdateHashWithObjectArray() {
        def hash = HashCodeHelper.initHash()

        Object[] arr1 = ["a", "b", "c"]
        Object[] arr2 = ["x", "y", "z"]

        def hashArr1 = HashCodeHelper.updateHash(hash, (Object) arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, (Object) arr2)

        assertNotEquals(hashArr1, hashArr2)
    }

    @Test
    void testUpdateHashWithBooleanArray() {
        def hash = HashCodeHelper.initHash()

        boolean[] arr1 = [true, false, true]
        boolean[] arr2 = [false, true, false]
        boolean[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithCharArray() {
        def hash = HashCodeHelper.initHash()

        char[] arr1 = ['a', 'b', 'c'] as char[]
        char[] arr2 = ['x', 'y', 'z'] as char[]
        char[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithByteArray() {
        def hash = HashCodeHelper.initHash()

        byte[] arr1 = [1, 2, 3] as byte[]
        byte[] arr2 = [4, 5, 6] as byte[]
        byte[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithShortArray() {
        def hash = HashCodeHelper.initHash()

        short[] arr1 = [100, 200, 300] as short[]
        short[] arr2 = [400, 500, 600] as short[]
        short[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithIntArray() {
        def hash = HashCodeHelper.initHash()

        int[] arr1 = [1, 2, 3] as int[]
        int[] arr2 = [4, 5, 6] as int[]
        int[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithLongArray() {
        def hash = HashCodeHelper.initHash()

        long[] arr1 = [1L, 2L, 3L] as long[]
        long[] arr2 = [4L, 5L, 6L] as long[]
        long[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithFloatArray() {
        def hash = HashCodeHelper.initHash()

        float[] arr1 = [1.0f, 2.0f, 3.0f] as float[]
        float[] arr2 = [4.0f, 5.0f, 6.0f] as float[]
        float[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testUpdateHashWithDoubleArray() {
        def hash = HashCodeHelper.initHash()

        double[] arr1 = [1.0d, 2.0d, 3.0d] as double[]
        double[] arr2 = [4.0d, 5.0d, 6.0d] as double[]
        double[] nullArr = null

        def hashArr1 = HashCodeHelper.updateHash(hash, arr1)
        def hashArr2 = HashCodeHelper.updateHash(hash, arr2)
        def hashNull = HashCodeHelper.updateHash(hash, nullArr)

        assertNotEquals(hashArr1, hashArr2)
        assertNotEquals(hashArr1, hashNull)
    }

    @Test
    void testChainingHashUpdates() {
        def hash = HashCodeHelper.initHash()
        hash = HashCodeHelper.updateHash(hash, 42)
        hash = HashCodeHelper.updateHash(hash, (Object) "test")
        hash = HashCodeHelper.updateHash(hash, true)
        hash = HashCodeHelper.updateHash(hash, 3.14d)

        // Verify we get a valid hash value
        assertNotEquals(0, hash)

        // Same sequence should produce same result
        def hash2 = HashCodeHelper.initHash()
        hash2 = HashCodeHelper.updateHash(hash2, 42)
        hash2 = HashCodeHelper.updateHash(hash2, (Object) "test")
        hash2 = HashCodeHelper.updateHash(hash2, true)
        hash2 = HashCodeHelper.updateHash(hash2, 3.14d)

        assertEquals(hash, hash2)
    }

    @Test
    void testOrderMatters() {
        def hash1 = HashCodeHelper.initHash()
        hash1 = HashCodeHelper.updateHash(hash1, 1)
        hash1 = HashCodeHelper.updateHash(hash1, 2)

        def hash2 = HashCodeHelper.initHash()
        hash2 = HashCodeHelper.updateHash(hash2, 2)
        hash2 = HashCodeHelper.updateHash(hash2, 1)

        // Order should matter
        assertNotEquals(hash1, hash2)
    }

    @Test
    void testEmptyArrays() {
        def hash = HashCodeHelper.initHash()

        def hashEmptyInt = HashCodeHelper.updateHash(hash, new int[0])
        def hashEmptyLong = HashCodeHelper.updateHash(hash, new long[0])

        // Empty arrays of same type should produce same hash
        assertEquals(hashEmptyInt, HashCodeHelper.updateHash(hash, new int[0]))

        // Note: Empty arrays may produce the same hash code due to Arrays.hashCode() behavior
        // Both int[0] and long[0] have hashCode of 1
        assertEquals(hashEmptyInt, hashEmptyLong)
    }

    @Test
    void testSpecialDoubleValues() {
        def hash = HashCodeHelper.initHash()

        def hashNaN = HashCodeHelper.updateHash(hash, Double.NaN)
        def hashPosInf = HashCodeHelper.updateHash(hash, Double.POSITIVE_INFINITY)
        def hashNegInf = HashCodeHelper.updateHash(hash, Double.NEGATIVE_INFINITY)

        // All should be different
        assertNotEquals(hashNaN, hashPosInf)
        assertNotEquals(hashNaN, hashNegInf)
        assertNotEquals(hashPosInf, hashNegInf)
    }

    @Test
    void testSpecialFloatValues() {
        def hash = HashCodeHelper.initHash()

        def hashNaN = HashCodeHelper.updateHash(hash, Float.NaN)
        def hashPosInf = HashCodeHelper.updateHash(hash, Float.POSITIVE_INFINITY)
        def hashNegInf = HashCodeHelper.updateHash(hash, Float.NEGATIVE_INFINITY)

        // All should be different
        assertNotEquals(hashNaN, hashPosInf)
        assertNotEquals(hashNaN, hashNegInf)
        assertNotEquals(hashPosInf, hashNegInf)
    }
}
