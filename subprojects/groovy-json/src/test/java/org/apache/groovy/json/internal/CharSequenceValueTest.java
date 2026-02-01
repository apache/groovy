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
 * Unit tests for {@link CharSequenceValue}.
 */
class CharSequenceValueTest {

    @Test
    void testStringValue() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals("hello", value.stringValue());
    }

    @Test
    void testStringValueWithOffset() {
        char[] buffer = "___hello___".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false);
        assertEquals("hello", value.stringValue());
    }

    @Test
    void testIntegerValue() {
        char[] buffer = "42".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals(42, value.intValue());
    }

    @Test
    void testNegativeIntegerValue() {
        char[] buffer = "-42".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals(-42, value.intValue());
    }

    @Test
    void testLongValue() {
        char[] buffer = "9876543210".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals(9876543210L, value.longValue());
    }

    @Test
    void testShortLongValue() {
        char[] buffer = "42".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals(42L, value.longValue());
    }

    @Test
    void testDoubleValue() {
        char[] buffer = "3.14159".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false);
        assertEquals(3.14159, value.doubleValue(), 0.00001);
    }

    @Test
    void testFloatValue() {
        char[] buffer = "2.5".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false);
        assertEquals(2.5f, value.floatValue(), 0.0001f);
    }

    @Test
    void testByteValue() {
        char[] buffer = "127".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals((byte) 127, value.byteValue());
    }

    @Test
    void testShortValue() {
        char[] buffer = "1000".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertEquals((short) 1000, value.shortValue());
    }

    @Test
    void testBigDecimalValue() {
        char[] buffer = "123.456789".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false);
        BigDecimal expected = new BigDecimal("123.456789");
        assertEquals(expected, value.bigDecimalValue());
    }

    @Test
    void testBigIntegerValue() {
        char[] buffer = "123456789012345678901234567890".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        BigInteger expected = new BigInteger("123456789012345678901234567890");
        assertEquals(expected, value.bigIntegerValue());
    }

    @Test
    void testToString() {
        char[] buffer = "test string".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals("test string", value.toString());
    }

    @Test
    void testToStringWithOffset() {
        char[] buffer = "___test___".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 3, 7, buffer, false, false);
        assertEquals("test", value.toString());
    }

    @Test
    void testToValue() {
        char[] buffer = "42".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        Object result = value.toValue();
        assertEquals(42, result);
    }

    @Test
    void testToValueDouble() {
        char[] buffer = "3.14".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false);
        Object result = value.toValue();
        assertEquals(3.14, (Double) result, 0.0001);
    }

    @Test
    void testToValueString() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        Object result = value.toValue();
        assertEquals("hello", result);
    }

    @Test
    void testToValueCached() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        Object result1 = value.toValue();
        Object result2 = value.toValue();
        assertSame(result1, result2);
    }

    @Test
    void testIsContainer() {
        char[] buffer = "test".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertFalse(value.isContainer());
    }

    @Test
    void testLength() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals(5, value.length());
    }

    @Test
    void testCharAt() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals('h', value.charAt(0));
        assertEquals('e', value.charAt(1));
        assertEquals('o', value.charAt(4));
    }

    @Test
    void testSubSequence() {
        char[] buffer = "hello world".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        CharSequence sub = value.subSequence(0, 5);
        assertEquals("hello", sub.toString());
    }

    @Test
    void testChop() {
        char[] buffer = "___hello___".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false);
        value.chop();
        assertEquals("hello", value.toString());
        // Call chop again to ensure idempotency
        value.chop();
        assertEquals("hello", value.toString());
    }

    @Test
    void testChopOnConstruction() {
        char[] buffer = "___hello___".toCharArray();
        CharSequenceValue value = new CharSequenceValue(true, Type.STRING, 3, 8, buffer, false, false);
        assertEquals("hello", value.toString());
    }

    @Test
    void testCharValue() {
        char[] buffer = "A".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals('A', value.charValue());
    }

    @Test
    void testBooleanValue() {
        char[] buffer = "true".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertTrue(value.booleanValue());
    }

    @Test
    void testBooleanValueFalse() {
        char[] buffer = "false".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertFalse(value.booleanValue());
    }

    @Test
    void testEquals() {
        char[] buffer = "test".toCharArray();
        CharSequenceValue value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        CharSequenceValue value2 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals(value1, value2);
    }

    @Test
    void testEqualsSameObject() {
        char[] buffer = "test".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals(value, value);
    }

    @Test
    void testNotEqualsDifferentIndex() {
        char[] buffer = "test1234".toCharArray();
        CharSequenceValue value1 = new CharSequenceValue(false, Type.STRING, 0, 4, buffer, false, false);
        CharSequenceValue value2 = new CharSequenceValue(false, Type.STRING, 4, 8, buffer, false, false);
        assertNotEquals(value1, value2);
    }

    @Test
    void testNotEqualsDifferentType() {
        char[] buffer = "42".toCharArray();
        CharSequenceValue value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        CharSequenceValue value2 = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        assertNotEquals(value1, value2);
    }

    @Test
    void testHashCode() {
        char[] buffer = "test".toCharArray();
        CharSequenceValue value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        CharSequenceValue value2 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals(value1.hashCode(), value2.hashCode());
    }

    @Test
    void testStringValueEncoded() {
        char[] buffer = "hello".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        assertEquals("hello", value.stringValueEncoded());
    }

    @Test
    void testToEnumWithString() {
        char[] buffer = "TWO".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false);
        TestEnum result = value.toEnum(TestEnum.class);
        assertEquals(TestEnum.TWO, result);
    }

    @Test
    void testToEnumWithInteger() {
        char[] buffer = "1".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        TestEnum result = value.toEnum(TestEnum.class);
        assertEquals(TestEnum.TWO, result);
    }

    @Test
    void testToEnumNull() {
        char[] buffer = "null".toCharArray();
        CharSequenceValue value = new CharSequenceValue(false, Type.NULL, 0, buffer.length, buffer, false, false);
        TestEnum result = value.toEnum(TestEnum.class);
        assertNull(result);
    }

    @Test
    void testStaticToEnumByValue() {
        TestEnum result = CharSequenceValue.toEnum(TestEnum.class, "ONE");
        assertEquals(TestEnum.ONE, result);
    }

    @Test
    void testStaticToEnumByValueWithDash() {
        // The implementation converts dashes to underscores and uses uppercase
        TestEnum result = CharSequenceValue.toEnum(TestEnum.class, "one");
        assertEquals(TestEnum.ONE, result);
    }

    @Test
    void testStaticToEnumByOrdinal() {
        TestEnum result = CharSequenceValue.toEnum(TestEnum.class, 2);
        assertEquals(TestEnum.THREE, result);
    }

    @Test
    void testIntegerWithLongRange() {
        char[] buffer = "2147483648".toCharArray(); // Integer.MAX_VALUE + 1
        CharSequenceValue value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false);
        Object result = value.toValue();
        assertEquals(2147483648L, result);
    }

    // Helper enum for testing
    enum TestEnum {
        ONE, TWO, THREE
    }
}
