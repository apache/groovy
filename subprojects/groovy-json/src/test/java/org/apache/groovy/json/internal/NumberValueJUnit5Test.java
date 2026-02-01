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
package org.apache.groovy.json.internal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for NumberValue class.
 */
class NumberValueJUnit5Test {

    // Constructor tests
    @Test
    void testDefaultConstructor() {
        NumberValue value = new NumberValue();
        assertNotNull(value);
    }

    @Test
    void testTypeConstructor() {
        NumberValue value = new NumberValue(Type.INTEGER);
        assertNotNull(value);
    }

    @Test
    void testBufferConstructorWithChop() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(true, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(12345, value.intValue());
    }

    @Test
    void testBufferConstructorWithoutChop() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(12345, value.intValue());
    }

    @Test
    void testBufferConstructorPartialRange() {
        char[] buffer = "abc12345xyz".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 3, 8, buffer);
        assertEquals(12345, value.intValue());
    }

    @Test
    void testBufferConstructorPartialRangeWithChop() {
        char[] buffer = "abc12345xyz".toCharArray();
        NumberValue value = new NumberValue(true, Type.INTEGER, 3, 8, buffer);
        assertEquals(12345, value.intValue());
    }

    @Test
    void testBufferConstructorSingleMinus() {
        char[] buffer = "-".toCharArray();
        assertThrows(Exception.class, () -> 
            new NumberValue(false, Type.INTEGER, 0, 1, buffer));
    }

    // intValue tests
    @Test
    void testIntValuePositive() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(42, value.intValue());
    }

    @Test
    void testIntValueNegative() {
        char[] buffer = "-42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(-42, value.intValue());
    }

    @Test
    void testIntValueZero() {
        char[] buffer = "0".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(0, value.intValue());
    }

    // longValue tests
    @Test
    void testLongValuePositive() {
        char[] buffer = "9876543210".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(9876543210L, value.longValue());
    }

    @Test
    void testLongValueNegative() {
        char[] buffer = "-9876543210".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(-9876543210L, value.longValue());
    }

    @Test
    void testLongValueFromSmallInt() {
        char[] buffer = "100".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(100L, value.longValue());
    }

    // doubleValue tests
    @Test
    void testDoubleValueInteger() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(42.0, value.doubleValue(), 0.0001);
    }

    @Test
    void testDoubleValueDecimal() {
        char[] buffer = "3.14159".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(3.14159, value.doubleValue(), 0.00001);
    }

    @Test
    void testDoubleValueNegative() {
        char[] buffer = "-2.5".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(-2.5, value.doubleValue(), 0.0001);
    }

    // floatValue tests
    @Test
    void testFloatValueDecimal() {
        char[] buffer = "3.14".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(3.14f, value.floatValue(), 0.01f);
    }

    // byteValue tests
    @Test
    void testByteValue() {
        char[] buffer = "127".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals((byte) 127, value.byteValue());
    }

    // shortValue tests
    @Test
    void testShortValue() {
        char[] buffer = "1000".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals((short) 1000, value.shortValue());
    }

    // charValue tests
    @Test
    void testCharValue() {
        char[] buffer = "65".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals('6', value.charValue()); // First char of buffer
    }

    // bigDecimalValue tests
    @Test
    void testBigDecimalValueInteger() {
        char[] buffer = "123456789".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(new BigDecimal("123456789"), value.bigDecimalValue());
    }

    @Test
    void testBigDecimalValueDecimal() {
        char[] buffer = "123.456789".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(new BigDecimal("123.456789"), value.bigDecimalValue());
    }

    // bigIntegerValue tests
    @Test
    void testBigIntegerValue() {
        char[] buffer = "123456789012345678901234567890".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(new BigInteger("123456789012345678901234567890"), value.bigIntegerValue());
    }

    // stringValue tests
    @Test
    void testStringValue() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("12345", value.stringValue());
    }

    @Test
    void testStringValueEncoded() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("12345", value.stringValueEncoded());
    }

    // booleanValue tests
    @Test
    void testBooleanValueTrue() {
        char[] buffer = "true".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertTrue(value.booleanValue());
    }

    @Test
    void testBooleanValueFalse() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertFalse(value.booleanValue());
    }

    // dateValue tests
    @Test
    void testDateValue() {
        // Use a timestamp
        long timestamp = 1609459200000L; // 2021-01-01 00:00:00 UTC
        char[] buffer = String.valueOf(timestamp).toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        Date date = value.dateValue();
        assertNotNull(date);
    }

    // toValue tests
    @Test
    void testToValueInteger() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(42, value.toValue());
    }

    @Test
    void testToValueLong() {
        char[] buffer = "9876543210".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(9876543210L, value.toValue());
    }

    @Test
    void testToValueDouble() {
        char[] buffer = "3.14".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        Object result = value.toValue();
        assertTrue(result instanceof BigDecimal);
    }

    @Test
    void testToValueCached() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        Object first = value.toValue();
        Object second = value.toValue();
        assertSame(first, second); // Should return cached value
    }

    // isContainer test
    @Test
    void testIsContainer() {
        NumberValue value = new NumberValue(Type.INTEGER);
        assertFalse(value.isContainer());
    }

    // toEnum tests
    @Test
    void testToEnumByOrdinal() {
        char[] buffer = "1".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        TestEnum result = value.toEnum(TestEnum.class);
        assertEquals(TestEnum.VALUE2, result);
    }

    @Test
    void testToEnumStaticByOrdinal() {
        assertEquals(TestEnum.VALUE1, NumberValue.toEnum(TestEnum.class, 0));
        assertEquals(TestEnum.VALUE2, NumberValue.toEnum(TestEnum.class, 1));
    }

    // toString tests
    @Test
    void testToStringFullBuffer() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(true, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("12345", value.toString());
    }

    @Test
    void testToStringPartialBuffer() {
        char[] buffer = "abc12345xyz".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 3, 8, buffer);
        assertEquals("12345", value.toString());
    }

    // chop tests
    @Test
    void testChop() {
        char[] buffer = "abc12345xyz".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 3, 8, buffer);
        value.chop();
        assertEquals("12345", value.toString());
    }

    @Test
    void testChopMultipleTimes() {
        char[] buffer = "12345".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        value.chop();
        value.chop(); // Should be idempotent
        assertEquals("12345", value.toString());
    }

    // equals tests
    @Test
    void testEqualsSameInstance() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(value, value);
    }

    @Test
    void testEqualsSameValues() {
        char[] buffer1 = "42".toCharArray();
        char[] buffer2 = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2);
        assertEquals(value1, value2);
    }

    @Test
    void testEqualsDifferentValues() {
        char[] buffer1 = "42".toCharArray();
        char[] buffer2 = "43".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2);
        assertNotEquals(value1, value2);
    }

    @Test
    void testEqualsDifferentTypes() {
        char[] buffer = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        NumberValue value2 = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertNotEquals(value1, value2);
    }

    @Test
    void testEqualsNotValue() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertNotEquals(value, "42");
    }

    // hashCode tests
    @Test
    void testHashCodeConsistent() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        int hash1 = value.hashCode();
        int hash2 = value.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeEqualObjects() {
        char[] buffer1 = "42".toCharArray();
        char[] buffer2 = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2);
        assertEquals(value1.hashCode(), value2.hashCode());
    }

    // Test enum for toEnum tests
    enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }
}
