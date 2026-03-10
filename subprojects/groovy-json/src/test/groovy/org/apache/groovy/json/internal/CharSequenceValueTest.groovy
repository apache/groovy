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
 * Unit tests for {@link CharSequenceValue}.
 */
class CharSequenceValueTest {

    // Constructor tests (from JUnit5)
    @Test
    void testConstructorWithChop() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    @Test
    void testConstructorWithoutChop() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    @Test
    void testConstructorPartialRange() {
        char[] buffer = "xxxhelloyyy".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    @Test
    void testConstructorPartialRangeWithChop() {
        char[] buffer = "xxxhelloyyy".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 3, 8, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    // CharSequence interface tests (from JUnit5)
    @Test
    void testLength() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals(5, value.length())
    }

    @Test
    void testCharAt() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals('h' as char, value.charAt(0))
        assertEquals('e' as char, value.charAt(1))
        assertEquals('o' as char, value.charAt(4))
    }

    @Test
    void testSubSequence() {
        char[] buffer = "hello world".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def sub = value.subSequence(0, 5)
        assertEquals("hello", sub.toString())
    }

    // stringValue tests
    @Test
    void testStringValue() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.stringValue())
    }

    @Test
    void testStringValueWithOffset() {
        char[] buffer = "___hello___".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false)
        assertEquals("hello", value.stringValue())
    }

    @Test
    void testStringValueSimple() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.stringValue())
    }

    @Test
    void testStringValueWithDecodeStrings() {
        char[] buffer = "hello\\nworld".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, true, false)
        // When decodeStrings is true, it should decode escape sequences
        assertNotNull(value.stringValue())
    }

    @Test
    void testStringValueEncoded() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.stringValueEncoded())
    }

    // intValue tests
    @Test
    void testIntegerValue() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(42, value.intValue())
    }

    @Test
    void testNegativeIntegerValue() {
        char[] buffer = "-42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(-42, value.intValue())
    }

    @Test
    void testIntValuePositive() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(42, value.intValue())
    }

    @Test
    void testIntValueNegative() {
        char[] buffer = "-42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(-42, value.intValue())
    }

    @Test
    void testIntValueZero() {
        char[] buffer = "0".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(0, value.intValue())
    }

    // longValue tests
    @Test
    void testLongValue() {
        char[] buffer = "9876543210".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(9876543210L, value.longValue())
    }

    @Test
    void testShortLongValue() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(42L, value.longValue())
    }

    @Test
    void testLongValueLargeNumber() {
        char[] buffer = "9876543210".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(9876543210L, value.longValue())
    }

    @Test
    void testLongValueSmallNumber() {
        char[] buffer = "100".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(100L, value.longValue())
    }

    // doubleValue tests
    @Test
    void testDoubleValue() {
        char[] buffer = "3.14159".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(3.14159, value.doubleValue(), 0.00001)
    }

    @Test
    void testDoubleValueNegative() {
        char[] buffer = "-2.5".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(-2.5, value.doubleValue(), 0.0001)
    }

    // floatValue tests
    @Test
    void testFloatValue() {
        char[] buffer = "3.14".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(3.14f, value.floatValue(), 0.01f)
    }

    @Test
    void testFloatValueFromJava() {
        char[] buffer = "2.5".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(2.5f, value.floatValue(), 0.0001f)
    }

    // byteValue tests
    @Test
    void testByteValue() {
        char[] buffer = "100".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals((byte) 100, value.byteValue())
    }

    @Test
    void testByteValueMax() {
        char[] buffer = "127".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals((byte) 127, value.byteValue())
    }

    // shortValue tests
    @Test
    void testShortValue() {
        char[] buffer = "1000".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals((short) 1000, value.shortValue())
    }

    // charValue tests
    @Test
    void testCharValue() {
        char[] buffer = "ABC".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals('A' as char, value.charValue())
    }

    @Test
    void testCharValueSingle() {
        char[] buffer = "A".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals('A' as char, value.charValue())
    }

    // booleanValue tests
    @Test
    void testBooleanValue() {
        char[] buffer = "true".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertTrue(value.booleanValue())
    }

    @Test
    void testBooleanValueTrue() {
        char[] buffer = "true".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertTrue(value.booleanValue())
    }

    @Test
    void testBooleanValueFalse() {
        char[] buffer = "false".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertFalse(value.booleanValue())
    }

    @Test
    void testBooleanValueOther() {
        char[] buffer = "something".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertFalse(value.booleanValue()) // parseBoolean returns false for non-"true" strings
    }

    // bigDecimalValue tests
    @Test
    void testBigDecimalValue() {
        char[] buffer = "123.456".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(new BigDecimal("123.456"), value.bigDecimalValue())
    }

    @Test
    void testBigDecimalValueFromJava() {
        char[] buffer = "123.456789".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        def expected = new BigDecimal("123.456789")
        assertEquals(expected, value.bigDecimalValue())
    }

    // bigIntegerValue tests
    @Test
    void testBigIntegerValue() {
        char[] buffer = "123456789012345678901234567890".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(new BigInteger("123456789012345678901234567890"), value.bigIntegerValue())
    }

    // toValue tests
    @Test
    void testToValue() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        def result = value.toValue()
        assertEquals(42, result)
    }

    @Test
    void testToValueInteger() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(42, value.toValue())
    }

    @Test
    void testToValueLong() {
        char[] buffer = "9876543210".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertEquals(9876543210L, value.toValue())
    }

    @Test
    void testToValueDouble() {
        char[] buffer = "3.14".toCharArray()
        def value = new CharSequenceValue(false, Type.DOUBLE, 0, buffer.length, buffer, false, false)
        assertEquals(3.14, value.toValue())
    }

    @Test
    void testToValueString() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.toValue())
    }

    @Test
    void testToValueCached() {
        char[] buffer = "42".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        def first = value.toValue()
        def second = value.toValue()
        assertSame(first, second)
    }

    @Test
    void testToValueCachedString() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def result1 = value.toValue()
        def result2 = value.toValue()
        assertSame(result1, result2)
    }

    // isContainer test
    @Test
    void testIsContainer() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertFalse(value.isContainer())
    }

    // toString tests
    @Test
    void testToString() {
        char[] buffer = "test string".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("test string", value.toString())
    }

    @Test
    void testToStringWithOffset() {
        char[] buffer = "___test___".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 3, 7, buffer, false, false)
        assertEquals("test", value.toString())
    }

    @Test
    void testToStringFullBuffer() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    @Test
    void testToStringPartialBuffer() {
        char[] buffer = "xxxhelloyyy".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    // toEnum tests
    @Test
    void testToEnumWithString() {
        char[] buffer = "TWO".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def result = value.toEnum(TestEnum)
        assertEquals(TestEnum.TWO, result)
    }

    @Test
    void testToEnumWithInteger() {
        char[] buffer = "1".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        def result = value.toEnum(TestEnum)
        assertEquals(TestEnum.TWO, result)
    }

    @Test
    void testToEnumNull() {
        char[] buffer = "null".toCharArray()
        def value = new CharSequenceValue(false, Type.NULL, 0, buffer.length, buffer, false, false)
        def result = value.toEnum(TestEnum)
        assertNull(result)
    }

    @Test
    void testToEnumFromString() {
        char[] buffer = "VALUE2".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def result = value.toEnum(TestEnumJUnit5)
        assertEquals(TestEnumJUnit5.VALUE2, result)
    }

    @Test
    void testToEnumFromInteger() {
        char[] buffer = "1".toCharArray()
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        def result = value.toEnum(TestEnumJUnit5)
        assertEquals(TestEnumJUnit5.VALUE2, result)
    }

    @Test
    void testStaticToEnumByValue() {
        def result = CharSequenceValue.toEnum(TestEnum, "ONE")
        assertEquals(TestEnum.ONE, result)
    }

    @Test
    void testStaticToEnumByValueWithDash() {
        def result = CharSequenceValue.toEnum(TestEnum, "one")
        assertEquals(TestEnum.ONE, result)
    }

    @Test
    void testStaticToEnumByOrdinal() {
        def result = CharSequenceValue.toEnum(TestEnum, 2)
        assertEquals(TestEnum.THREE, result)
    }

    @Test
    void testToEnumStaticFromString() {
        assertEquals(TestEnumJUnit5.VALUE1, CharSequenceValue.toEnum(TestEnumJUnit5, "VALUE1"))
        assertEquals(TestEnumJUnit5.VALUE2, CharSequenceValue.toEnum(TestEnumJUnit5, "VALUE2"))
    }

    @Test
    void testToEnumStaticFromStringWithHyphens() {
        assertEquals(TestEnumWithHyphens.SOME_VALUE, CharSequenceValue.toEnum(TestEnumWithHyphens, "some-value"))
    }

    @Test
    void testToEnumStaticFromOrdinal() {
        assertEquals(TestEnumJUnit5.VALUE1, CharSequenceValue.toEnum(TestEnumJUnit5, 0))
        assertEquals(TestEnumJUnit5.VALUE2, CharSequenceValue.toEnum(TestEnumJUnit5, 1))
    }

    // chop tests
    @Test
    void testChop() {
        char[] buffer = "xxxhelloyyy".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 3, 8, buffer, false, false)
        value.chop()
        assertEquals("hello", value.toString())
    }

    @Test
    void testChopOnConstruction() {
        char[] buffer = "___hello___".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 3, 8, buffer, false, false)
        assertEquals("hello", value.toString())
    }

    @Test
    void testChopMultipleTimes() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        value.chop()
        value.chop() // Should be idempotent
        assertEquals("hello", value.toString())
    }

    @Test
    void testChopAlreadyChopped() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(true, Type.STRING, 0, buffer.length, buffer, false, false)
        value.chop() // Already chopped by constructor
        assertEquals("hello", value.toString())
    }

    // equals tests
    @Test
    void testEquals() {
        char[] buffer = "test".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals(value1, value2)
    }

    @Test
    void testEqualsSameObject() {
        char[] buffer = "test".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals(value, value)
    }

    @Test
    void testEqualsSameInstance() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals(value, value)
    }

    @Test
    void testEqualsSameValues() {
        char[] buffer1 = "hello".toCharArray()
        char[] buffer2 = "hello".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer1.length, buffer1, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, buffer2.length, buffer2, false, false)
        assertEquals(value1, value2)
    }

    @Test
    void testEqualsDifferentValues() {
        char[] buffer1 = "hello".toCharArray()
        char[] buffer2 = "world".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer1.length, buffer1, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, buffer2.length, buffer2, false, false)
        assertNotEquals(value1, value2)
    }

    @Test
    void testEqualsDifferentTypes() {
        char[] buffer = "hello".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertNotEquals(value1, value2)
    }

    @Test
    void testEqualsDifferentEndIndex() {
        char[] buffer = "hello".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, 3, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, 5, buffer, false, false)
        assertNotEquals(value1, value2)
    }

    @Test
    void testEqualsNotValue() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertNotEquals(value, "hello")
    }

    @Test
    void testNotEqualsDifferentIndex() {
        char[] buffer = "test1234".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, 4, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 4, 8, buffer, false, false)
        assertNotEquals(value1, value2)
    }

    @Test
    void testNotEqualsDifferentType() {
        char[] buffer = "42".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        assertNotEquals(value1, value2)
    }

    // hashCode tests
    @Test
    void testHashCode() {
        char[] buffer = "test".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        assertEquals(value1.hashCode(), value2.hashCode())
    }

    @Test
    void testHashCodeConsistent() {
        char[] buffer = "hello".toCharArray()
        def value = new CharSequenceValue(false, Type.STRING, 0, buffer.length, buffer, false, false)
        def hash1 = value.hashCode()
        def hash2 = value.hashCode()
        assertEquals(hash1, hash2)
    }

    @Test
    void testHashCodeEqualObjects() {
        char[] buffer1 = "hello".toCharArray()
        char[] buffer2 = "hello".toCharArray()
        def value1 = new CharSequenceValue(false, Type.STRING, 0, buffer1.length, buffer1, false, false)
        def value2 = new CharSequenceValue(false, Type.STRING, 0, buffer2.length, buffer2, false, false)
        assertEquals(value1.hashCode(), value2.hashCode())
    }

    // Integer with long range
    @Test
    void testIntegerWithLongRange() {
        char[] buffer = "2147483648".toCharArray() // Integer.MAX_VALUE + 1
        def value = new CharSequenceValue(false, Type.INTEGER, 0, buffer.length, buffer, false, false)
        def result = value.toValue()
        assertEquals(2147483648L, result)
    }

    // Helper enums for testing
    enum TestEnum {
        ONE, TWO, THREE
    }

    enum TestEnumJUnit5 {
        VALUE1, VALUE2, VALUE3
    }

    enum TestEnumWithHyphens {
        SOME_VALUE, ANOTHER_VALUE
    }
}
