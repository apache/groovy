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
package org.codehaus.groovy.runtime.typehandling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for ShortTypeHandling class.
 */
class ShortTypeHandlingJUnit5Test {

    // castToClass tests
    @Test
    void testCastToClassWithNull() {
        assertNull(ShortTypeHandling.castToClass(null));
    }

    @Test
    void testCastToClassWithClass() {
        Class<?> result = ShortTypeHandling.castToClass(String.class);
        assertEquals(String.class, result);
    }

    @Test
    void testCastToClassWithClassName() {
        Class<?> result = ShortTypeHandling.castToClass("java.lang.String");
        assertEquals(String.class, result);
    }

    @Test
    void testCastToClassWithInvalidClassName() {
        assertThrows(GroovyCastException.class, () ->
            ShortTypeHandling.castToClass("invalid.class.Name"));
    }

    // castToString tests
    @Test
    void testCastToStringWithNull() {
        assertNull(ShortTypeHandling.castToString(null));
    }

    @Test
    void testCastToStringWithString() {
        assertEquals("hello", ShortTypeHandling.castToString("hello"));
    }

    @Test
    void testCastToStringWithObject() {
        assertEquals("42", ShortTypeHandling.castToString(42));
    }

    @Test
    void testCastToStringWithBooleanArray() {
        boolean[] arr = {true, false, true};
        assertEquals("[true, false, true]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithByteArray() {
        byte[] arr = {1, 2, 3};
        assertEquals("[1, 2, 3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithCharArray() {
        char[] arr = {'a', 'b', 'c'};
        assertEquals("abc", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithDoubleArray() {
        double[] arr = {1.1, 2.2, 3.3};
        assertEquals("[1.1, 2.2, 3.3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithFloatArray() {
        float[] arr = {1.1f, 2.2f, 3.3f};
        assertEquals("[1.1, 2.2, 3.3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithIntArray() {
        int[] arr = {1, 2, 3};
        assertEquals("[1, 2, 3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithLongArray() {
        long[] arr = {1L, 2L, 3L};
        assertEquals("[1, 2, 3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithShortArray() {
        short[] arr = {1, 2, 3};
        assertEquals("[1, 2, 3]", ShortTypeHandling.castToString(arr));
    }

    @Test
    void testCastToStringWithObjectArray() {
        Object[] arr = {"a", "b", "c"};
        assertEquals("[a, b, c]", ShortTypeHandling.castToString(arr));
    }

    // castToEnum tests
    enum TestEnum { VALUE_ONE, VALUE_TWO, VALUE_THREE }

    @Test
    void testCastToEnumWithNull() {
        assertNull(ShortTypeHandling.castToEnum(null, TestEnum.class));
    }

    @Test
    void testCastToEnumWithSameEnum() {
        TestEnum result = (TestEnum) ShortTypeHandling.castToEnum(TestEnum.VALUE_ONE, TestEnum.class);
        assertEquals(TestEnum.VALUE_ONE, result);
    }

    @Test
    void testCastToEnumWithString() {
        TestEnum result = (TestEnum) ShortTypeHandling.castToEnum("VALUE_TWO", TestEnum.class);
        assertEquals(TestEnum.VALUE_TWO, result);
    }

    @Test
    void testCastToEnumWithInvalidString() {
        assertThrows(IllegalArgumentException.class, () ->
            ShortTypeHandling.castToEnum("INVALID_VALUE", TestEnum.class));
    }

    @Test
    void testCastToEnumWithInvalidType() {
        assertThrows(GroovyCastException.class, () ->
            ShortTypeHandling.castToEnum(123, TestEnum.class));
    }

    // castToChar tests
    @Test
    void testCastToCharWithNull() {
        assertNull(ShortTypeHandling.castToChar(null));
    }

    @Test
    void testCastToCharWithCharacter() {
        assertEquals('X', ShortTypeHandling.castToChar('X'));
    }

    @Test
    void testCastToCharWithNumber() {
        assertEquals('A', ShortTypeHandling.castToChar(65));
    }

    @Test
    void testCastToCharWithSingleCharString() {
        assertEquals('Z', ShortTypeHandling.castToChar("Z"));
    }

    @Test
    void testCastToCharWithMultiCharString() {
        assertThrows(GroovyCastException.class, () ->
            ShortTypeHandling.castToChar("Hello"));
    }

    @Test
    void testCastToCharWithEmptyString() {
        assertThrows(GroovyCastException.class, () ->
            ShortTypeHandling.castToChar(""));
    }

    @Test
    void testCastToCharWithLongNumber() {
        assertEquals('a', ShortTypeHandling.castToChar(97L));
    }

    @Test
    void testCastToCharWithDoubleNumber() {
        assertEquals('A', ShortTypeHandling.castToChar(65.9));
    }
}
