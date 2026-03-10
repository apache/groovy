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
package org.apache.groovy.json.internal

import org.junit.jupiter.api.Test

import java.math.BigDecimal
import java.math.BigInteger

import static org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for {@link NumberValue}.
 */
class NumberValueTest {

    // Constructor tests (from JUnit5)
    @Test
    void testDefaultConstructor() {
        def value = new NumberValue()
        assertNotNull(value)
    }

    @Test
    void testTypeConstructor() {
        def value = new NumberValue(Type.INTEGER)
        assertNotNull(value)
    }

    @Test
    void testBufferConstructorWithChop() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(true, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(12345, value.intValue())
    }

    @Test
    void testBufferConstructorWithoutChop() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(12345, value.intValue())
    }

    @Test
    void testBufferConstructorPartialRange() {
        char[] buffer = "abc12345xyz".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 3, 8, buffer)
        assertEquals(12345, value.intValue())
    }

    @Test
    void testBufferConstructorPartialRangeWithChop() {
        char[] buffer = "abc12345xyz".toCharArray()
        def value = new NumberValue(true, Type.INTEGER, 3, 8, buffer)
        assertEquals(12345, value.intValue())
    }

    @Test
    void testBufferConstructorSingleMinus() {
        char[] buffer = "-".toCharArray()
        assertThrows(Exception, { ->
            new NumberValue(false, Type.INTEGER, 0, 1, buffer)
        })
    }

    // intValue tests
    @Test
    void testIntegerValue() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(42, value.intValue())
        assertEquals(42L, value.longValue())
        assertEquals(42.0, value.doubleValue(), 0.0001)
        assertEquals(42.0f, value.floatValue(), 0.0001f)
    }

    @Test
    void testIntValuePositive() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(42, value.intValue())
    }

    @Test
    void testIntValueNegative() {
        char[] buffer = "-42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(-42, value.intValue())
    }

    @Test
    void testIntValueZero() {
        char[] buffer = "0".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(0, value.intValue())
    }

    @Test
    void testNegativeInteger() {
        char[] buffer = "-42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(-42, value.intValue())
    }

    // longValue tests
    @Test
    void testLongValue() {
        char[] buffer = "9876543210".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(9876543210L, value.longValue())
    }

    @Test
    void testLongValuePositive() {
        char[] buffer = "9876543210".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(9876543210L, value.longValue())
    }

    @Test
    void testLongValueNegative() {
        char[] buffer = "-9876543210".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(-9876543210L, value.longValue())
    }

    @Test
    void testLongValueFromSmallInt() {
        char[] buffer = "100".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(100L, value.longValue())
    }

    // doubleValue tests
    @Test
    void testDoubleValue() {
        char[] buffer = "3.14159".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(3.14159, value.doubleValue(), 0.00001)
    }

    @Test
    void testDoubleValueInteger() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(42.0, value.doubleValue(), 0.0001)
    }

    @Test
    void testDoubleValueDecimal() {
        char[] buffer = "3.14159".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(3.14159, value.doubleValue(), 0.00001)
    }

    @Test
    void testDoubleValueNegative() {
        char[] buffer = "-2.5".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(-2.5, value.doubleValue(), 0.0001)
    }

    @Test
    void testNegativeDouble() {
        char[] buffer = "-3.14".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(-3.14, value.doubleValue(), 0.0001)
    }

    // floatValue tests
    @Test
    void testFloatValue() {
        char[] buffer = "2.5".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(2.5f, value.floatValue(), 0.0001f)
    }

    @Test
    void testFloatValueDecimal() {
        char[] buffer = "3.14".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(3.14f, value.floatValue(), 0.01f)
    }

    // byteValue tests
    @Test
    void testByteValue() {
        char[] buffer = "127".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals((byte) 127, value.byteValue())
    }

    // shortValue tests
    @Test
    void testShortValue() {
        char[] buffer = "1000".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals((short) 1000, value.shortValue())
    }

    // charValue tests
    @Test
    void testCharValue() {
        char[] buffer = "65".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals('6' as char, value.charValue()) // First char of buffer
    }

    @Test
    void testCharValueSingle() {
        char[] buffer = "5".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals('5' as char, value.charValue())
    }

    // bigDecimalValue tests
    @Test
    void testBigDecimalValue() {
        char[] buffer = "123.456789".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        def expected = new BigDecimal("123.456789")
        assertEquals(expected, value.bigDecimalValue())
    }

    @Test
    void testBigDecimalValueInteger() {
        char[] buffer = "123456789".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(new BigDecimal("123456789"), value.bigDecimalValue())
    }

    @Test
    void testBigDecimalValueDecimal() {
        char[] buffer = "123.456789".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertEquals(new BigDecimal("123.456789"), value.bigDecimalValue())
    }

    // bigIntegerValue tests
    @Test
    void testBigIntegerValue() {
        char[] buffer = "123456789012345678901234567890".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(new BigInteger("123456789012345678901234567890"), value.bigIntegerValue())
    }

    // stringValue tests
    @Test
    void testStringValue() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("12345", value.stringValue())
    }

    @Test
    void testStringValueFromJava() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("42", value.stringValue())
    }

    @Test
    void testStringValueEncoded() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("12345", value.stringValueEncoded())
    }

    @Test
    void testStringValueEncodedFromJava() {
        char[] buffer = "123".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("123", value.stringValueEncoded())
    }

    // booleanValue tests
    @Test
    void testBooleanValue() {
        char[] buffer = "true".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        // Boolean.parseBoolean("true") returns true
        assertTrue(value.booleanValue())
    }

    @Test
    void testBooleanValueTrue() {
        char[] buffer = "true".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertTrue(value.booleanValue())
    }

    @Test
    void testBooleanValueFalse() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertFalse(value.booleanValue())
    }

    @Test
    void testBooleanValueFalseZero() {
        char[] buffer = "0".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        // Boolean.parseBoolean("0") returns false
        assertFalse(value.booleanValue())
    }

    // dateValue tests
    @Test
    void testDateValue() {
        // Use a timestamp
        long timestamp = 1609459200000L // 2021-01-01 00:00:00 UTC
        char[] buffer = String.valueOf(timestamp).toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def date = value.dateValue()
        assertNotNull(date)
    }

    // toValue tests
    @Test
    void testToValue() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def result = value.toValue()
        assertEquals(42, result)
    }

    @Test
    void testToValueInteger() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(42, value.toValue())
    }

    @Test
    void testToValueLong() {
        char[] buffer = "9876543210".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(9876543210L, value.toValue())
    }

    @Test
    void testToValueDouble() {
        char[] buffer = "3.14".toCharArray()
        def value = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        def result = value.toValue()
        assertTrue(result instanceof BigDecimal)
    }

    @Test
    void testToValueCached() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def first = value.toValue()
        def second = value.toValue()
        assertSame(first, second) // Should return cached value
    }

    // isContainer test
    @Test
    void testIsContainer() {
        def value = new NumberValue(Type.INTEGER)
        assertFalse(value.isContainer())
    }

    @Test
    void testIsContainerFromJava() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertFalse(value.isContainer())
    }

    // toString tests
    @Test
    void testToString() {
        char[] buffer = "999".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("999", value.toString())
    }

    @Test
    void testToStringWithOffset() {
        char[] buffer = "___42___".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 3, 5, buffer)
        assertEquals("42", value.toString())
    }

    @Test
    void testToStringFullBuffer() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(true, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals("12345", value.toString())
    }

    @Test
    void testToStringPartialBuffer() {
        char[] buffer = "abc12345xyz".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 3, 8, buffer)
        assertEquals("12345", value.toString())
    }

    // chop tests
    @Test
    void testChop() {
        char[] buffer = "abc12345xyz".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 3, 8, buffer)
        value.chop()
        assertEquals("12345", value.toString())
    }

    @Test
    void testChopOnConstruction() {
        char[] buffer = "___42___".toCharArray()
        def value = new NumberValue(true, Type.INTEGER, 3, 5, buffer)
        assertEquals("42", value.toString())
        assertEquals(42, value.intValue())
    }

    @Test
    void testChopMultipleTimes() {
        char[] buffer = "12345".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        value.chop()
        value.chop() // Should be idempotent
        assertEquals("12345", value.toString())
    }

    @Test
    void testChopIdempotentFromJava() {
        char[] buffer = "___42___".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 3, 5, buffer)
        value.chop()
        assertEquals("42", value.toString())
        // Call chop again to test idempotency
        value.chop()
        assertEquals("42", value.toString())
    }

    // equals tests
    @Test
    void testEquals() {
        char[] buffer = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(value1, value2)
    }

    @Test
    void testEqualsSameObject() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(value, value)
    }

    @Test
    void testEqualsSameInstance() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(value, value)
    }

    @Test
    void testEqualsSameValues() {
        char[] buffer1 = "42".toCharArray()
        char[] buffer2 = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2)
        assertEquals(value1, value2)
    }

    @Test
    void testEqualsDifferentValues() {
        char[] buffer1 = "42".toCharArray()
        char[] buffer2 = "43".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2)
        assertNotEquals(value1, value2)
    }

    @Test
    void testEqualsDifferentTypes() {
        char[] buffer = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def value2 = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertNotEquals(value1, value2)
    }

    @Test
    void testEqualsNotValue() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertNotEquals(value, "42")
    }

    @Test
    void testEqualsNull() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertNotEquals(null, value)
    }

    @Test
    void testNotEqualsDifferentIndex() {
        char[] buffer = "1234".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, 2, buffer)
        def value2 = new NumberValue(false, Type.INTEGER, 2, 4, buffer)
        assertNotEquals(value1, value2)
    }

    @Test
    void testNotEqualsDifferentType() {
        char[] buffer = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def value2 = new NumberValue(false, Type.DOUBLE, 0, buffer.length, buffer)
        assertNotEquals(value1, value2)
    }

    // hashCode tests
    @Test
    void testHashCode() {
        char[] buffer = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(value1.hashCode(), value2.hashCode())
    }

    @Test
    void testHashCodeConsistent() {
        char[] buffer = "42".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def hash1 = value.hashCode()
        def hash2 = value.hashCode()
        assertEquals(hash1, hash2)
    }

    @Test
    void testHashCodeEqualObjects() {
        char[] buffer1 = "42".toCharArray()
        char[] buffer2 = "42".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2)
        assertEquals(value1.hashCode(), value2.hashCode())
    }

    @Test
    void testHashCodeDifferent() {
        char[] buffer1 = "42".toCharArray()
        char[] buffer2 = "43".toCharArray()
        def value1 = new NumberValue(false, Type.INTEGER, 0, buffer1.length, buffer1)
        def value2 = new NumberValue(false, Type.INTEGER, 0, buffer2.length, buffer2)
        assertNotEquals(value1.hashCode(), value2.hashCode())
    }

    // zero tests
    @Test
    void testZero() {
        char[] buffer = "0".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        assertEquals(0, value.intValue())
        assertEquals(0L, value.longValue())
        assertEquals(0.0, value.doubleValue(), 0.0001)
    }

    // single minus throws
    @Test
    void testSingleMinusThrows() {
        char[] buffer = "-".toCharArray()
        assertThrows(RuntimeException, { ->
            new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        })
    }

    // toEnum tests
    @Test
    void testToEnumByOrdinal() {
        char[] buffer = "1".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def result = value.toEnum(TestEnum)
        assertEquals(TestEnum.VALUE2, result)
    }

    @Test
    void testToEnumStaticByOrdinal() {
        assertEquals(TestEnum.VALUE1, NumberValue.toEnum(TestEnum, 0))
        assertEquals(TestEnum.VALUE2, NumberValue.toEnum(TestEnum, 1))
    }

    @Test
    void testToEnumByOrdinalFromJava() {
        char[] buffer = "1".toCharArray()
        def value = new NumberValue(false, Type.INTEGER, 0, buffer.length, buffer)
        def result = value.toEnum(TestEnumJava)
        assertEquals(TestEnumJava.TWO, result)
    }

    // Test enums for testing
    enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    enum TestEnumJava {
        ONE, TWO, THREE
    }
}
