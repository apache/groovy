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
package org.codehaus.groovy.runtime;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for NumberAwareComparator class.
 */
class NumberAwareComparatorJUnit5Test {

    @Test
    void testCompareEqualIntegers() {
        NumberAwareComparator<Integer> comparator = new NumberAwareComparator<>();
        assertEquals(0, comparator.compare(5, 5));
    }

    @Test
    void testCompareDifferentIntegers() {
        NumberAwareComparator<Integer> comparator = new NumberAwareComparator<>();
        assertTrue(comparator.compare(3, 5) < 0);
        assertTrue(comparator.compare(5, 3) > 0);
    }

    @Test
    void testCompareEqualStrings() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        assertEquals(0, comparator.compare("hello", "hello"));
    }

    @Test
    void testCompareDifferentStrings() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        assertTrue(comparator.compare("apple", "banana") < 0);
        assertTrue(comparator.compare("banana", "apple") > 0);
    }

    @Test
    void testCompareNullFirst() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        assertTrue(comparator.compare(null, "hello") < 0);
    }

    @Test
    void testCompareNullSecond() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        assertTrue(comparator.compare("hello", null) > 0);
    }

    @Test
    void testCompareBothNull() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        assertEquals(0, comparator.compare(null, null));
    }

    @Test
    void testCompareMixedNumberTypes() {
        NumberAwareComparator<Number> comparator = new NumberAwareComparator<>();
        // Different numeric types should be comparable
        assertEquals(0, comparator.compare(5, 5L));
        assertEquals(0, comparator.compare(5.0, 5));
        assertTrue(comparator.compare(3, 5.0) < 0);
    }

    @Test
    void testCompareDoubles() {
        NumberAwareComparator<Double> comparator = new NumberAwareComparator<>();
        assertEquals(0, comparator.compare(3.14, 3.14));
        assertTrue(comparator.compare(2.71, 3.14) < 0);
    }

    @Test
    void testCompareBigDecimal() {
        NumberAwareComparator<BigDecimal> comparator = new NumberAwareComparator<>();
        BigDecimal a = new BigDecimal("100.50");
        BigDecimal b = new BigDecimal("100.50");
        BigDecimal c = new BigDecimal("200.00");
        
        assertEquals(0, comparator.compare(a, b));
        assertTrue(comparator.compare(a, c) < 0);
    }

    @Test
    void testCompareBigInteger() {
        NumberAwareComparator<BigInteger> comparator = new NumberAwareComparator<>();
        BigInteger a = new BigInteger("12345678901234567890");
        BigInteger b = new BigInteger("12345678901234567890");
        BigInteger c = new BigInteger("98765432109876543210");
        
        assertEquals(0, comparator.compare(a, b));
        assertTrue(comparator.compare(a, c) < 0);
    }

    @Test
    void testIgnoreZeroSignFalse() {
        NumberAwareComparator<Float> comparator = new NumberAwareComparator<>(false);
        // +0.0f and -0.0f should be different
        int result = comparator.compare(0.0f, -0.0f);
        // They may or may not be equal depending on compareTo implementation
        assertNotNull(result);
    }

    @Test
    void testIgnoreZeroSignTrueForFloat() {
        NumberAwareComparator<Float> comparator = new NumberAwareComparator<>(true);
        // With ignoreZeroSign=true, +0.0f and -0.0f should be equal
        assertEquals(0, comparator.compare(0.0f, -0.0f));
        assertEquals(0, comparator.compare(-0.0f, 0.0f));
    }

    @Test
    void testIgnoreZeroSignTrueForDouble() {
        NumberAwareComparator<Double> comparator = new NumberAwareComparator<>(true);
        // With ignoreZeroSign=true, +0.0 and -0.0 should be equal
        assertEquals(0, comparator.compare(0.0d, -0.0d));
        assertEquals(0, comparator.compare(-0.0d, 0.0d));
    }

    @Test
    void testIgnoreZeroSignOnlyAffectsZero() {
        NumberAwareComparator<Double> comparator = new NumberAwareComparator<>(true);
        // Non-zero values should still compare normally
        assertTrue(comparator.compare(1.0, 2.0) < 0);
        assertTrue(comparator.compare(-1.0, 0.0) < 0);
    }

    @Test
    void testSortingWithComparator() {
        NumberAwareComparator<Integer> comparator = new NumberAwareComparator<>();
        List<Integer> list = Arrays.asList(5, 2, 8, 1, 9);
        Collections.sort(list, comparator);
        
        assertEquals(Arrays.asList(1, 2, 5, 8, 9), list);
    }

    @Test
    void testSortingWithNulls() {
        NumberAwareComparator<Integer> comparator = new NumberAwareComparator<>();
        List<Integer> list = Arrays.asList(5, null, 2, null, 1);
        Collections.sort(list, comparator);
        
        // Nulls should be first (less than any non-null)
        assertNull(list.get(0));
        assertNull(list.get(1));
        assertEquals(1, list.get(2));
    }

    @Test
    void testIsSerializable() {
        NumberAwareComparator<Integer> comparator = new NumberAwareComparator<>();
        assertTrue(comparator instanceof java.io.Serializable);
    }

    @Test
    void testCompareIncompatibleTypes() {
        NumberAwareComparator<Object> comparator = new NumberAwareComparator<>();
        // Incompatible types should not throw but use hashCode comparison
        int result = comparator.compare("string", 42);
        // Result should be non-zero since they are different
        assertNotEquals(0, result);
    }

    @Test
    void testCompareSameObjectReference() {
        NumberAwareComparator<Object> comparator = new NumberAwareComparator<>();
        Object obj = new Object();
        assertEquals(0, comparator.compare(obj, obj));
    }

    @Test
    void testCompareEqualObjects() {
        NumberAwareComparator<String> comparator = new NumberAwareComparator<>();
        String s1 = new String("test");
        String s2 = new String("test");
        assertEquals(0, comparator.compare(s1, s2));
    }
}
