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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for ValueContainer class.
 */
class ValueContainerJUnit5Test {

    // Static constant tests
    @Test
    void testTrueConstant() {
        assertNotNull(ValueContainer.TRUE);
        assertTrue(ValueContainer.TRUE.booleanValue());
        assertEquals(true, ValueContainer.TRUE.toValue());
    }

    @Test
    void testFalseConstant() {
        assertNotNull(ValueContainer.FALSE);
        assertFalse(ValueContainer.FALSE.booleanValue());
        assertEquals(false, ValueContainer.FALSE.toValue());
    }

    @Test
    void testNullConstant() {
        assertNotNull(ValueContainer.NULL);
        assertNull(ValueContainer.NULL.toValue());
        assertNull(ValueContainer.NULL.stringValue());
    }

    // Constructor tests
    @Test
    void testConstructorWithType() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertTrue(vc.booleanValue());
    }

    @Test
    void testConstructorWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        ValueContainer vc = new ValueContainer(map);
        
        assertTrue(vc.isContainer());
        assertEquals(map, vc.toValue());
    }

    @Test
    void testConstructorWithList() {
        List<Object> list = Arrays.asList("a", "b", "c");
        ValueContainer vc = new ValueContainer(list);
        
        assertTrue(vc.isContainer());
        assertEquals(list, vc.toValue());
    }

    @Test
    void testConstructorWithValueAndType() {
        ValueContainer vc = new ValueContainer("test", Type.STRING, false);
        assertEquals("test", vc.value);
        assertEquals(Type.STRING, vc.type);
        assertFalse(vc.decodeStrings);
    }

    // booleanValue tests
    @Test
    void testBooleanValueTrue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertTrue(vc.booleanValue());
    }

    @Test
    void testBooleanValueFalse() {
        ValueContainer vc = new ValueContainer(Type.FALSE);
        assertFalse(vc.booleanValue());
    }

    // stringValue tests
    @Test
    void testStringValueNull() {
        ValueContainer vc = new ValueContainer(Type.NULL);
        assertNull(vc.stringValue());
    }

    @Test
    void testStringValueTrue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals("TRUE", vc.stringValue());
    }

    @Test
    void testStringValueFalse() {
        ValueContainer vc = new ValueContainer(Type.FALSE);
        assertEquals("FALSE", vc.stringValue());
    }

    // stringValueEncoded tests
    @Test
    void testStringValueEncoded() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals("TRUE", vc.stringValueEncoded());
    }

    // toString tests
    @Test
    void testToStringTrue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals("TRUE", vc.toString());
    }

    @Test
    void testToStringFalse() {
        ValueContainer vc = new ValueContainer(Type.FALSE);
        assertEquals("FALSE", vc.toString());
    }

    @Test
    void testToStringNull() {
        ValueContainer vc = new ValueContainer(Type.NULL);
        assertEquals("NULL", vc.toString());
    }

    // toValue tests
    @Test
    void testToValueWithExistingValue() {
        Map<String, Object> map = new HashMap<>();
        ValueContainer vc = new ValueContainer(map);
        assertSame(map, vc.toValue());
    }

    @Test
    void testToValueTrue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(true, vc.toValue());
    }

    @Test
    void testToValueFalse() {
        ValueContainer vc = new ValueContainer(Type.FALSE);
        assertEquals(false, vc.toValue());
    }

    @Test
    void testToValueNull() {
        ValueContainer vc = new ValueContainer(Type.NULL);
        assertNull(vc.toValue());
    }

    // toEnum tests
    @Test
    void testToEnum() {
        ValueContainer vc = new ValueContainer(TestEnum.VALUE_A, Type.STRING, false);
        assertEquals(TestEnum.VALUE_A, vc.toEnum(TestEnum.class));
    }

    enum TestEnum { VALUE_A, VALUE_B }

    // isContainer tests
    @Test
    void testIsContainerMap() {
        ValueContainer vc = new ValueContainer(new HashMap<String, Object>());
        assertTrue(vc.isContainer());
    }

    @Test
    void testIsContainerList() {
        ValueContainer vc = new ValueContainer(Arrays.asList("a", "b"));
        assertTrue(vc.isContainer());
    }

    @Test
    void testIsContainerFalseForPrimitive() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertFalse(vc.isContainer());
    }

    // chop tests - should do nothing
    @Test
    void testChop() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        vc.chop(); // Should not throw
        assertTrue(vc.booleanValue()); // State unchanged
    }

    // CharSequence methods tests
    @Test
    void testCharValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals('\0', vc.charValue());
    }

    @Test
    void testLength() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(0, vc.length());
    }

    @Test
    void testCharAt() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals('0', vc.charAt(0));
    }

    @Test
    void testSubSequence() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals("", vc.subSequence(0, 0));
    }

    // dateValue tests
    @Test
    void testDateValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertNull(vc.dateValue());
    }

    // Numeric value tests
    @Test
    void testByteValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(0, vc.byteValue());
    }

    @Test
    void testShortValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(0, vc.shortValue());
    }

    @Test
    void testBigDecimalValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertNull(vc.bigDecimalValue());
    }

    @Test
    void testBigIntegerValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertNull(vc.bigIntegerValue());
    }

    @Test
    void testDoubleValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(0.0, vc.doubleValue());
    }

    @Test
    void testFloatValue() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertEquals(0.0f, vc.floatValue());
    }

    // intValue and longValue throw exceptions by design
    @Test
    void testIntValueThrows() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertThrows(Exceptions.JsonInternalException.class, () -> vc.intValue());
    }

    @Test
    void testLongValueThrows() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        assertThrows(Exceptions.JsonInternalException.class, () -> vc.longValue());
    }

    // Multiple toValue calls cache the result
    @Test
    void testToValueCachesResult() {
        ValueContainer vc = new ValueContainer(Type.TRUE);
        Object first = vc.toValue();
        Object second = vc.toValue();
        assertSame(first, second);
    }
}
