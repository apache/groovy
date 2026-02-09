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

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for ValueContainer class.
 */
class ValueContainerTest {

    // Static constant tests
    @Test
    void testTrueConstant() {
        assertNotNull(ValueContainer.TRUE)
        assertTrue(ValueContainer.TRUE.booleanValue())
        assertEquals(true, ValueContainer.TRUE.toValue())
    }

    @Test
    void testFalseConstant() {
        assertNotNull(ValueContainer.FALSE)
        assertFalse(ValueContainer.FALSE.booleanValue())
        assertEquals(false, ValueContainer.FALSE.toValue())
    }

    @Test
    void testNullConstant() {
        assertNotNull(ValueContainer.NULL)
        assertNull(ValueContainer.NULL.toValue())
        assertNull(ValueContainer.NULL.stringValue())
    }

    // Constructor tests
    @Test
    void testConstructorWithType() {
        def vc = new ValueContainer(Type.TRUE)
        assertTrue(vc.booleanValue())
    }

    @Test
    void testConstructorWithMap() {
        def map = [:]
        map.put("key", "value")
        def vc = new ValueContainer(map)

        assertTrue(vc.isContainer())
        assertEquals(map, vc.toValue())
    }

    @Test
    void testConstructorWithList() {
        def list = ["a", "b", "c"]
        def vc = new ValueContainer(list)

        assertTrue(vc.isContainer())
        assertEquals(list, vc.toValue())
    }

    @Test
    void testConstructorWithValueAndType() {
        def vc = new ValueContainer("test", Type.STRING, false)
        assertEquals("test", vc.value)
        assertEquals(Type.STRING, vc.type)
        assertFalse(vc.decodeStrings)
    }

    // booleanValue tests
    @Test
    void testBooleanValueTrue() {
        def vc = new ValueContainer(Type.TRUE)
        assertTrue(vc.booleanValue())
    }

    @Test
    void testBooleanValueFalse() {
        def vc = new ValueContainer(Type.FALSE)
        assertFalse(vc.booleanValue())
    }

    // stringValue tests
    @Test
    void testStringValueNull() {
        def vc = new ValueContainer(Type.NULL)
        assertNull(vc.stringValue())
    }

    @Test
    void testStringValueTrue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals("TRUE", vc.stringValue())
    }

    @Test
    void testStringValueFalse() {
        def vc = new ValueContainer(Type.FALSE)
        assertEquals("FALSE", vc.stringValue())
    }

    // stringValueEncoded tests
    @Test
    void testStringValueEncoded() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals("TRUE", vc.stringValueEncoded())
    }

    // toString tests
    @Test
    void testToStringTrue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals("TRUE", vc.toString())
    }

    @Test
    void testToStringFalse() {
        def vc = new ValueContainer(Type.FALSE)
        assertEquals("FALSE", vc.toString())
    }

    @Test
    void testToStringNull() {
        def vc = new ValueContainer(Type.NULL)
        assertEquals("NULL", vc.toString())
    }

    // toValue tests
    @Test
    void testToValueWithExistingValue() {
        def map = [:]
        def vc = new ValueContainer(map)
        assertSame(map, vc.toValue())
    }

    @Test
    void testToValueTrue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(true, vc.toValue())
    }

    @Test
    void testToValueFalse() {
        def vc = new ValueContainer(Type.FALSE)
        assertEquals(false, vc.toValue())
    }

    @Test
    void testToValueNull() {
        def vc = new ValueContainer(Type.NULL)
        assertNull(vc.toValue())
    }

    // toEnum tests
    @Test
    void testToEnum() {
        def vc = new ValueContainer(TestEnum.VALUE_A, Type.STRING, false)
        assertEquals(TestEnum.VALUE_A, vc.toEnum(TestEnum))
    }

    enum TestEnum { VALUE_A, VALUE_B }

    // isContainer tests
    @Test
    void testIsContainerMap() {
        def vc = new ValueContainer([:])
        assertTrue(vc.isContainer())
    }

    @Test
    void testIsContainerList() {
        def vc = new ValueContainer(["a", "b"])
        assertTrue(vc.isContainer())
    }

    @Test
    void testIsContainerFalseForPrimitive() {
        def vc = new ValueContainer(Type.TRUE)
        assertFalse(vc.isContainer())
    }

    // chop tests - should do nothing
    @Test
    void testChop() {
        def vc = new ValueContainer(Type.TRUE)
        vc.chop() // Should not throw
        assertTrue(vc.booleanValue()) // State unchanged
    }

    // CharSequence methods tests
    @Test
    void testCharValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals('\0' as char, vc.charValue())
    }

    @Test
    void testLength() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(0, vc.length())
    }

    @Test
    void testCharAt() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals('0' as char, vc.charAt(0))
    }

    @Test
    void testSubSequence() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals("", vc.subSequence(0, 0))
    }

    // dateValue tests
    @Test
    void testDateValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertNull(vc.dateValue())
    }

    // Numeric value tests
    @Test
    void testByteValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(0, vc.byteValue())
    }

    @Test
    void testShortValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(0, vc.shortValue())
    }

    @Test
    void testBigDecimalValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertNull(vc.bigDecimalValue())
    }

    @Test
    void testBigIntegerValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertNull(vc.bigIntegerValue())
    }

    @Test
    void testDoubleValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(0.0, vc.doubleValue())
    }

    @Test
    void testFloatValue() {
        def vc = new ValueContainer(Type.TRUE)
        assertEquals(0.0f, vc.floatValue())
    }

    // intValue and longValue throw exceptions by design
    @Test
    void testIntValueThrows() {
        def vc = new ValueContainer(Type.TRUE)
        assertThrows(Exceptions.JsonInternalException, { -> vc.intValue() })
    }

    @Test
    void testLongValueThrows() {
        def vc = new ValueContainer(Type.TRUE)
        assertThrows(Exceptions.JsonInternalException, { -> vc.longValue() })
    }

    // Multiple toValue calls cache the result
    @Test
    void testToValueCachesResult() {
        def vc = new ValueContainer(Type.TRUE)
        def first = vc.toValue()
        def second = vc.toValue()
        assertSame(first, second)
    }
}
