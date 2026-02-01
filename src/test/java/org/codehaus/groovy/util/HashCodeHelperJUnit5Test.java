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
package org.codehaus.groovy.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for HashCodeHelper class.
 */
class HashCodeHelperJUnit5Test {

    @Test
    void testInitHash() {
        int hash = HashCodeHelper.initHash();
        // The initial hash should be the SEED value (127)
        assertEquals(127, hash);
    }

    @Test
    void testUpdateHashWithBoolean() {
        int hash = HashCodeHelper.initHash();
        
        int hashTrue = HashCodeHelper.updateHash(hash, true);
        int hashFalse = HashCodeHelper.updateHash(hash, false);
        
        // true and false should produce different hashes
        assertNotEquals(hashTrue, hashFalse);
        
        // Same value should produce same hash
        assertEquals(hashTrue, HashCodeHelper.updateHash(hash, true));
        assertEquals(hashFalse, HashCodeHelper.updateHash(hash, false));
    }

    @Test
    void testUpdateHashWithChar() {
        int hash = HashCodeHelper.initHash();
        
        int hashA = HashCodeHelper.updateHash(hash, 'a');
        int hashB = HashCodeHelper.updateHash(hash, 'b');
        
        assertNotEquals(hashA, hashB);
        assertEquals(hashA, HashCodeHelper.updateHash(hash, 'a'));
    }

    @Test
    void testUpdateHashWithCharacter() {
        int hash = HashCodeHelper.initHash();
        
        int hashChar = HashCodeHelper.updateHash(hash, Character.valueOf('x'));
        int hashNull = HashCodeHelper.updateHash(hash, (Character) null);
        
        assertNotEquals(hashChar, hashNull);
        // Null should be treated as 0
        assertEquals(HashCodeHelper.updateHash(hash, (char) 0), hashNull);
    }

    @Test
    void testUpdateHashWithInt() {
        int hash = HashCodeHelper.initHash();
        
        int hash1 = HashCodeHelper.updateHash(hash, 42);
        int hash2 = HashCodeHelper.updateHash(hash, 43);
        
        assertNotEquals(hash1, hash2);
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 42));
    }

    @Test
    void testUpdateHashWithInteger() {
        int hash = HashCodeHelper.initHash();
        
        int hashInt = HashCodeHelper.updateHash(hash, Integer.valueOf(100));
        int hashNull = HashCodeHelper.updateHash(hash, (Integer) null);
        
        assertNotEquals(hashInt, hashNull);
        assertEquals(HashCodeHelper.updateHash(hash, 0), hashNull);
    }

    @Test
    void testUpdateHashWithLong() {
        int hash = HashCodeHelper.initHash();
        
        int hash1 = HashCodeHelper.updateHash(hash, 123456789L);
        int hash2 = HashCodeHelper.updateHash(hash, 987654321L);
        
        assertNotEquals(hash1, hash2);
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 123456789L));
    }

    @Test
    void testUpdateHashWithLongObject() {
        int hash = HashCodeHelper.initHash();
        
        int hashLong = HashCodeHelper.updateHash(hash, Long.valueOf(999L));
        int hashNull = HashCodeHelper.updateHash(hash, (Long) null);
        
        assertNotEquals(hashLong, hashNull);
        assertEquals(HashCodeHelper.updateHash(hash, 0L), hashNull);
    }

    @Test
    void testUpdateHashWithFloat() {
        int hash = HashCodeHelper.initHash();
        
        int hash1 = HashCodeHelper.updateHash(hash, 3.14f);
        int hash2 = HashCodeHelper.updateHash(hash, 2.71f);
        
        assertNotEquals(hash1, hash2);
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 3.14f));
    }

    @Test
    void testUpdateHashWithFloatObject() {
        int hash = HashCodeHelper.initHash();
        
        int hashFloat = HashCodeHelper.updateHash(hash, Float.valueOf(1.5f));
        int hashNull = HashCodeHelper.updateHash(hash, (Float) null);
        
        assertNotEquals(hashFloat, hashNull);
        assertEquals(HashCodeHelper.updateHash(hash, 0f), hashNull);
    }

    @Test
    void testUpdateHashWithDouble() {
        int hash = HashCodeHelper.initHash();
        
        int hash1 = HashCodeHelper.updateHash(hash, 3.14159265);
        int hash2 = HashCodeHelper.updateHash(hash, 2.71828182);
        
        assertNotEquals(hash1, hash2);
        assertEquals(hash1, HashCodeHelper.updateHash(hash, 3.14159265));
    }

    @Test
    void testUpdateHashWithDoubleObject() {
        int hash = HashCodeHelper.initHash();
        
        int hashDouble = HashCodeHelper.updateHash(hash, Double.valueOf(99.99));
        int hashNull = HashCodeHelper.updateHash(hash, (Double) null);
        
        assertNotEquals(hashDouble, hashNull);
        assertEquals(HashCodeHelper.updateHash(hash, 0d), hashNull);
    }

    @Test
    void testUpdateHashWithObject() {
        int hash = HashCodeHelper.initHash();
        
        int hashStr1 = HashCodeHelper.updateHash(hash, "hello");
        int hashStr2 = HashCodeHelper.updateHash(hash, "world");
        int hashNull = HashCodeHelper.updateHash(hash, (Object) null);
        
        assertNotEquals(hashStr1, hashStr2);
        assertNotEquals(hashStr1, hashNull);
    }

    @Test
    void testUpdateHashWithObjectArray() {
        int hash = HashCodeHelper.initHash();
        
        Object[] arr1 = {"a", "b", "c"};
        Object[] arr2 = {"x", "y", "z"};
        
        int hashArr1 = HashCodeHelper.updateHash(hash, (Object) arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, (Object) arr2);
        
        assertNotEquals(hashArr1, hashArr2);
    }

    @Test
    void testUpdateHashWithBooleanArray() {
        int hash = HashCodeHelper.initHash();
        
        boolean[] arr1 = {true, false, true};
        boolean[] arr2 = {false, true, false};
        boolean[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithCharArray() {
        int hash = HashCodeHelper.initHash();
        
        char[] arr1 = {'a', 'b', 'c'};
        char[] arr2 = {'x', 'y', 'z'};
        char[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithByteArray() {
        int hash = HashCodeHelper.initHash();
        
        byte[] arr1 = {1, 2, 3};
        byte[] arr2 = {4, 5, 6};
        byte[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithShortArray() {
        int hash = HashCodeHelper.initHash();
        
        short[] arr1 = {100, 200, 300};
        short[] arr2 = {400, 500, 600};
        short[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithIntArray() {
        int hash = HashCodeHelper.initHash();
        
        int[] arr1 = {1, 2, 3};
        int[] arr2 = {4, 5, 6};
        int[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithLongArray() {
        int hash = HashCodeHelper.initHash();
        
        long[] arr1 = {1L, 2L, 3L};
        long[] arr2 = {4L, 5L, 6L};
        long[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithFloatArray() {
        int hash = HashCodeHelper.initHash();
        
        float[] arr1 = {1.0f, 2.0f, 3.0f};
        float[] arr2 = {4.0f, 5.0f, 6.0f};
        float[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testUpdateHashWithDoubleArray() {
        int hash = HashCodeHelper.initHash();
        
        double[] arr1 = {1.0, 2.0, 3.0};
        double[] arr2 = {4.0, 5.0, 6.0};
        double[] nullArr = null;
        
        int hashArr1 = HashCodeHelper.updateHash(hash, arr1);
        int hashArr2 = HashCodeHelper.updateHash(hash, arr2);
        int hashNull = HashCodeHelper.updateHash(hash, nullArr);
        
        assertNotEquals(hashArr1, hashArr2);
        assertNotEquals(hashArr1, hashNull);
    }

    @Test
    void testChainingHashUpdates() {
        int hash = HashCodeHelper.initHash();
        hash = HashCodeHelper.updateHash(hash, 42);
        hash = HashCodeHelper.updateHash(hash, "test");
        hash = HashCodeHelper.updateHash(hash, true);
        hash = HashCodeHelper.updateHash(hash, 3.14);
        
        // Verify we get a valid hash value
        assertNotEquals(0, hash);
        
        // Same sequence should produce same result
        int hash2 = HashCodeHelper.initHash();
        hash2 = HashCodeHelper.updateHash(hash2, 42);
        hash2 = HashCodeHelper.updateHash(hash2, "test");
        hash2 = HashCodeHelper.updateHash(hash2, true);
        hash2 = HashCodeHelper.updateHash(hash2, 3.14);
        
        assertEquals(hash, hash2);
    }

    @Test
    void testOrderMatters() {
        int hash1 = HashCodeHelper.initHash();
        hash1 = HashCodeHelper.updateHash(hash1, 1);
        hash1 = HashCodeHelper.updateHash(hash1, 2);
        
        int hash2 = HashCodeHelper.initHash();
        hash2 = HashCodeHelper.updateHash(hash2, 2);
        hash2 = HashCodeHelper.updateHash(hash2, 1);
        
        // Order should matter
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testEmptyArrays() {
        int hash = HashCodeHelper.initHash();
        
        int hashEmptyInt = HashCodeHelper.updateHash(hash, new int[0]);
        int hashEmptyLong = HashCodeHelper.updateHash(hash, new long[0]);
        
        // Empty arrays of same type should produce same hash
        assertEquals(hashEmptyInt, HashCodeHelper.updateHash(hash, new int[0]));
        
        // Note: Empty arrays may produce the same hash code due to Arrays.hashCode() behavior
        // Both int[0] and long[0] have hashCode of 1
        assertEquals(hashEmptyInt, hashEmptyLong);
    }

    @Test
    void testSpecialDoubleValues() {
        int hash = HashCodeHelper.initHash();
        
        int hashNaN = HashCodeHelper.updateHash(hash, Double.NaN);
        int hashPosInf = HashCodeHelper.updateHash(hash, Double.POSITIVE_INFINITY);
        int hashNegInf = HashCodeHelper.updateHash(hash, Double.NEGATIVE_INFINITY);
        
        // All should be different
        assertNotEquals(hashNaN, hashPosInf);
        assertNotEquals(hashNaN, hashNegInf);
        assertNotEquals(hashPosInf, hashNegInf);
    }

    @Test
    void testSpecialFloatValues() {
        int hash = HashCodeHelper.initHash();
        
        int hashNaN = HashCodeHelper.updateHash(hash, Float.NaN);
        int hashPosInf = HashCodeHelper.updateHash(hash, Float.POSITIVE_INFINITY);
        int hashNegInf = HashCodeHelper.updateHash(hash, Float.NEGATIVE_INFINITY);
        
        // All should be different
        assertNotEquals(hashNaN, hashPosInf);
        assertNotEquals(hashNaN, hashNegInf);
        assertNotEquals(hashPosInf, hashNegInf);
    }
}
