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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NumberValue}.
 */
class NumberValueTest {

    @Test
    void testIntegerValue() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(42, value.intValue());
        assertEquals(42L, value.longValue());
        assertEquals(42.0, value.doubleValue(), 0.0001);
        assertEquals(42.0f, value.floatValue(), 0.0001f);
    }

    @Test
    void testLongValue() {
        char[] buffer = "9876543210".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(9876543210L, value.longValue());
    }

    @Test
    void testDoubleValue() {
        char[] buffer = "3.14159".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(3.14159, value.doubleValue(), 0.00001);
    }

    @Test
    void testFloatValue() {
        char[] buffer = "2.5".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(2.5f, value.floatValue(), 0.0001f);
    }

    @Test
    void testByteValue() {
        char[] buffer = "127".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals((byte) 127, value.byteValue());
    }

    @Test
    void testShortValue() {
        char[] buffer = "1000".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals((short) 1000, value.shortValue());
    }

    @Test
    void testBigDecimalValue() {
        char[] buffer = "123.456789".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        BigDecimal expected = new BigDecimal("123.456789");
        assertEquals(expected, value.bigDecimalValue());
    }

    @Test
    void testBigIntegerValue() {
        char[] buffer = "123456789012345678901234567890".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        BigInteger expected = new BigInteger("123456789012345678901234567890");
        assertEquals(expected, value.bigIntegerValue());
    }

    @Test
    void testStringValue() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("42", value.stringValue());
    }

    @Test
    void testStringValueEncoded() {
        char[] buffer = "123".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("123", value.stringValueEncoded());
    }

    @Test
    void testToString() {
        char[] buffer = "999".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals("999", value.toString());
    }

    @Test
    void testToStringWithOffset() {
        char[] buffer = "___42___".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 3, 5, buffer);
        assertEquals("42", value.toString());
    }

    @Test
    void testToValue() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        Object result = value.toValue();
        assertEquals(42, result);
    }

    @Test
    void testToValueDouble() {
        char[] buffer = "3.14".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        Object result = value.toValue();
        assertTrue(result instanceof BigDecimal);
    }

    @Test
    void testToValueLong() {
        char[] buffer = "9876543210".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        Object result = value.toValue();
        assertEquals(9876543210L, result);
    }

    @Test
    void testToValueCached() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        Object result1 = value.toValue();
        Object result2 = value.toValue();
        assertSame(result1, result2);
    }

    @Test
    void testIsContainer() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertFalse(value.isContainer());
    }

    @Test
    void testChop() {
        char[] buffer = "___42___".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 3, 5, buffer);
        value.chop();
        assertEquals("42", value.toString());
        // Call chop again to test idempotency
        value.chop();
        assertEquals("42", value.toString());
    }

    @Test
    void testChopOnConstruction() {
        char[] buffer = "___42___".toCharArray();
        NumberValue value = new NumberValue(true, Type.INTEGER, 3, 5, buffer);
        assertEquals("42", value.toString());
        assertEquals(42, value.intValue());
    }

    @Test
    void testCharValue() {
        char[] buffer = "5".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals('5', value.charValue());
    }

    @Test
    void testDateValue() {
        char[] buffer = "1609459200000".toCharArray(); // 2021-01-01 00:00:00 UTC
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertNotNull(value.dateValue());
    }

    @Test
    void testBooleanValue() {
        char[] buffer = "true".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        // Boolean.parseBoolean("true") returns true
        assertTrue(value.booleanValue());
    }

    @Test
    void testBooleanValueFalse() {
        char[] buffer = "0".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        // Boolean.parseBoolean("0") returns false
        assertFalse(value.booleanValue());
    }

    @Test
    void testEquals() {
        char[] buffer = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(value1, value2);
    }

    @Test
    void testEqualsSameObject() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(value, value);
    }

    @Test
    void testEqualsNull() {
        char[] buffer = "42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertNotEquals(null, value);
    }

    @Test
    void testNotEqualsDifferentIndex() {
        char[] buffer = "1234".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, 2, buffer);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 2, 4, buffer);
        assertNotEquals(value1, value2);
    }

    @Test
    void testNotEqualsDifferentType() {
        char[] buffer = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        NumberValue value2 = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertNotEquals(value1, value2);
    }

    @Test
    void testHashCode() {
        char[] buffer = "42".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(value1.hashCode(), value2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        char[] buffer1 = "42".toCharArray();
        char[] buffer2 = "43".toCharArray();
        NumberValue value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1);
        NumberValue value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2);
        assertNotEquals(value1.hashCode(), value2.hashCode());
    }

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
    void testNegativeInteger() {
        char[] buffer = "-42".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(-42, value.intValue());
    }

    @Test
    void testNegativeDouble() {
        char[] buffer = "-3.14".toCharArray();
        NumberValue value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer);
        assertEquals(-3.14, value.doubleValue(), 0.0001);
    }

    @Test
    void testZero() {
        char[] buffer = "0".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        assertEquals(0, value.intValue());
        assertEquals(0L, value.longValue());
        assertEquals(0.0, value.doubleValue(), 0.0001);
    }

    @Test
    void testSingleMinusThrows() {
        char[] buffer = "-".toCharArray();
        assertThrows(RuntimeException.class, () -> 
            new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer));
    }

    @Test
    void testToEnumByOrdinal() {
        char[] buffer = "1".toCharArray();
        NumberValue value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer);
        TestEnum result = value.toEnum(TestEnum.class);
        assertEquals(TestEnum.TWO, result);
    }

    // Helper enum for testing
    enum TestEnum {
        ONE, TWO, THREE
    }
}
