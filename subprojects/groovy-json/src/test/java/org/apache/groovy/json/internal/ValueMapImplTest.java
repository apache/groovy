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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for ValueMapImpl class.
 */
class ValueMapImplTest {

    private ValueMapImpl valueMap;

    @BeforeEach
    void setUp() {
        valueMap = new ValueMapImpl();
    }

    // Simple Value implementation for testing
    private static Value createStringValue(final String str) {
        return new Value() {
            @Override
            public byte byteValue() { return 0; }
            @Override
            public short shortValue() { return 0; }
            @Override
            public int intValue() { return 0; }
            @Override
            public long longValue() { return 0; }
            @Override
            public BigDecimal bigDecimalValue() { return null; }
            @Override
            public BigInteger bigIntegerValue() { return null; }
            @Override
            public float floatValue() { return 0; }
            @Override
            public double doubleValue() { return 0; }
            @Override
            public boolean booleanValue() { return false; }
            @Override
            public Date dateValue() { return null; }
            @Override
            public String stringValue() { return str; }
            @Override
            public String stringValueEncoded() { return str; }
            @Override
            public Object toValue() { return str; }
            @Override
            public <T extends Enum> T toEnum(Class<T> cls) { return null; }
            @Override
            public boolean isContainer() { return false; }
            @Override
            public void chop() { }
            @Override
            public char charValue() { return str != null && str.length() > 0 ? str.charAt(0) : 0; }
            @Override
            public String toString() { return str; }
        };
    }

    private MapItemValue createEntry(String key, String value) {
        return new MapItemValue(createStringValue(key), createStringValue(value));
    }

    @Test
    void testInitialState() {
        assertEquals(0, valueMap.len());
        assertFalse(valueMap.hydrated());
    }

    @Test
    void testAdd() {
        MapItemValue entry = createEntry("key1", "value1");
        valueMap.add(entry);

        assertEquals(1, valueMap.len());
    }

    @Test
    void testAddMultiple() {
        valueMap.add(createEntry("key1", "value1"));
        valueMap.add(createEntry("key2", "value2"));
        valueMap.add(createEntry("key3", "value3"));

        assertEquals(3, valueMap.len());
    }

    @Test
    void testItems() {
        MapItemValue entry1 = createEntry("a", "1");
        MapItemValue entry2 = createEntry("b", "2");

        valueMap.add(entry1);
        valueMap.add(entry2);

        Map.Entry<String, Value>[] items = valueMap.items();
        assertNotNull(items);
        assertEquals(entry1, items[0]);
        assertEquals(entry2, items[1]);
    }

    @Test
    void testGetBeforeHydration() {
        MapItemValue entry = createEntry("mykey", "myvalue");
        valueMap.add(entry);

        Value result = valueMap.get("mykey");
        assertNotNull(result);
        assertEquals("myvalue", result.toString());
    }

    @Test
    void testGetNotFound() {
        valueMap.add(createEntry("key1", "value1"));

        Value result = valueMap.get("nonexistent");
        assertNull(result);
    }

    @Test
    void testHydratedAfterEntrySet() {
        valueMap.add(createEntry("key1", "value1"));
        assertFalse(valueMap.hydrated());

        valueMap.entrySet();
        assertTrue(valueMap.hydrated());
    }

    @Test
    void testEntrySet() {
        valueMap.add(createEntry("a", "1"));
        valueMap.add(createEntry("b", "2"));

        Set<Map.Entry<String, Value>> entrySet = valueMap.entrySet();
        assertEquals(2, entrySet.size());
    }

    @Test
    void testValues() {
        valueMap.add(createEntry("key1", "val1"));
        valueMap.add(createEntry("key2", "val2"));

        Collection<Value> values = valueMap.values();
        assertEquals(2, values.size());
    }

    @Test
    void testSize() {
        valueMap.add(createEntry("a", "1"));
        valueMap.add(createEntry("b", "2"));
        valueMap.add(createEntry("c", "3"));

        assertEquals(3, valueMap.size());
    }

    @Test
    void testSizeEmpty() {
        assertEquals(0, valueMap.size());
    }

    @Test
    void testPutThrowsException() {
        assertThrows(Exceptions.JsonInternalException.class, () -> {
            valueMap.put("key", createStringValue("value"));
        });
    }

    @Test
    void testGetAfterHydration() {
        valueMap.add(createEntry("key1", "value1"));
        valueMap.add(createEntry("key2", "value2"));

        // Force hydration by calling entrySet
        valueMap.entrySet();

        // Now get should use the internal map
        Value result = valueMap.get("key1");
        assertNotNull(result);
        assertEquals("value1", result.toString());
    }

    @Test
    void testAddMoreThanInitialCapacity() {
        // The initial capacity is 20, test adding more
        for (int i = 0; i < 25; i++) {
            valueMap.add(createEntry("key" + i, "value" + i));
        }

        assertEquals(25, valueMap.len());
        assertEquals(25, valueMap.size());
    }

    @Test
    void testGetMultipleKeys() {
        valueMap.add(createEntry("first", "1st"));
        valueMap.add(createEntry("second", "2nd"));
        valueMap.add(createEntry("third", "3rd"));

        assertEquals("1st", valueMap.get("first").toString());
        assertEquals("2nd", valueMap.get("second").toString());
        assertEquals("3rd", valueMap.get("third").toString());
    }

    @Test
    void testEntrySetMultipleCalls() {
        valueMap.add(createEntry("x", "y"));

        Set<Map.Entry<String, Value>> set1 = valueMap.entrySet();
        Set<Map.Entry<String, Value>> set2 = valueMap.entrySet();

        // Both should return the same map's entry set
        assertEquals(set1.size(), set2.size());
    }

    @Test
    void testValuesContents() {
        valueMap.add(createEntry("a", "alpha"));
        valueMap.add(createEntry("b", "beta"));

        Collection<Value> values = valueMap.values();

        boolean foundAlpha = false;
        boolean foundBeta = false;
        for (Value v : values) {
            if ("alpha".equals(v.toString())) foundAlpha = true;
            if ("beta".equals(v.toString())) foundBeta = true;
        }

        assertTrue(foundAlpha);
        assertTrue(foundBeta);
    }

    @Test
    void testEmptyEntrySet() {
        Set<Map.Entry<String, Value>> entrySet = valueMap.entrySet();
        assertTrue(entrySet.isEmpty());
    }

    @Test
    void testEmptyValues() {
        Collection<Value> values = valueMap.values();
        assertTrue(values.isEmpty());
    }

    @Test
    void testItemsArray() {
        Map.Entry<String, Value>[] items = valueMap.items();
        assertNotNull(items);
        // Initial array has capacity 20
        assertEquals(20, items.length);
    }
}
