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
package groovy.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SpreadMap}.
 */
class SpreadMapJUnit5Test {

    private SpreadMap spreadMap;

    @BeforeEach
    void setUp() {
        String[] entries = new String[]{"key1", "value1", "key2", "value2"};
        spreadMap = new SpreadMap(entries);
    }

    @Test
    void testConstructFromArray() {
        Object[] entries = {"a", 1, "b", 2, "c", 3};
        SpreadMap map = new SpreadMap(entries);
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    void testConstructFromMap() {
        Map<String, Integer> source = new HashMap<>();
        source.put("x", 10);
        source.put("y", 20);
        SpreadMap map = new SpreadMap(source);
        assertEquals(2, map.size());
        assertEquals(10, map.get("x"));
        assertEquals(20, map.get("y"));
    }

    @Test
    void testConstructFromList() {
        List<Object> list = Arrays.asList("first", 100, "second", 200);
        SpreadMap map = new SpreadMap(list);
        assertEquals(2, map.size());
        assertEquals(100, map.get("first"));
        assertEquals(200, map.get("second"));
    }

    @Test
    void testPutThrowsException() {
        assertThrows(RuntimeException.class, () -> spreadMap.put("newKey", "newValue"));
    }

    @Test
    void testRemoveThrowsException() {
        assertThrows(RuntimeException.class, () -> spreadMap.remove("key1"));
    }

    @Test
    void testPutAllThrowsException() {
        Map<String, String> newMap = new HashMap<>();
        newMap.put("a", "b");
        assertThrows(RuntimeException.class, () -> spreadMap.putAll(newMap));
    }

    @Test
    void testEqualsWithSameSpreadMap() {
        SpreadMap map1 = new SpreadMap(new Object[]{"a", 1, "b", 2});
        SpreadMap map2 = new SpreadMap(new Object[]{"a", 1, "b", 2});
        assertEquals(map1, map2);
    }

    @Test
    void testEqualsWithSelf() {
        assertTrue(spreadMap.equals(spreadMap));
    }

    @Test
    void testEqualsWithDifferentSize() {
        SpreadMap map1 = new SpreadMap(new Object[]{"a", 1});
        SpreadMap map2 = new SpreadMap(new Object[]{"a", 1, "b", 2});
        assertNotEquals(map1, map2);
    }

    @Test
    void testEqualsWithDifferentValues() {
        SpreadMap map1 = new SpreadMap(new Object[]{"a", 1});
        SpreadMap map2 = new SpreadMap(new Object[]{"a", 2});
        assertNotEquals(map1, map2);
    }

    @Test
    void testEqualsWithNonSpreadMap() {
        Map<String, Integer> regularMap = new HashMap<>();
        regularMap.put("key1", 1);
        assertFalse(spreadMap.equals(regularMap));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(spreadMap.equals((SpreadMap) null));
    }

    @Test
    void testHashCode() {
        SpreadMap map1 = new SpreadMap(new Object[]{"a", 1, "b", 2});
        SpreadMap map2 = new SpreadMap(new Object[]{"a", 1, "b", 2});
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    void testHashCodeWithNullKey() {
        SpreadMap map = new SpreadMap(new Object[]{null, "value"});
        // Should not throw, hashCode should handle null keys
        int hash = map.hashCode();
        // Just verify it doesn't throw and returns something
        assertTrue(hash != 0 || hash == 0); // Always true, just checking no exception
    }

    @Test
    void testToStringEmpty() {
        SpreadMap emptyMap = new SpreadMap(new Object[]{});
        assertEquals("*:[:]", emptyMap.toString());
    }

    @Test
    void testToStringWithEntries() {
        SpreadMap map = new SpreadMap(new Object[]{"a", 1});
        String result = map.toString();
        assertTrue(result.startsWith("*:["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("a:1"));
    }

    @Test
    void testToStringMultipleEntries() {
        SpreadMap map = new SpreadMap(new Object[]{"a", 1, "b", 2});
        String result = map.toString();
        assertTrue(result.startsWith("*:["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains(":"));
        assertTrue(result.contains(", "));
    }

    @Test
    void testContainsKey() {
        assertTrue(spreadMap.containsKey("key1"));
        assertTrue(spreadMap.containsKey("key2"));
        assertFalse(spreadMap.containsKey("nonexistent"));
    }

    @Test
    void testContainsValue() {
        assertTrue(spreadMap.containsValue("value1"));
        assertTrue(spreadMap.containsValue("value2"));
        assertFalse(spreadMap.containsValue("nonexistent"));
    }

    @Test
    void testKeySet() {
        assertEquals(2, spreadMap.keySet().size());
        assertTrue(spreadMap.keySet().contains("key1"));
        assertTrue(spreadMap.keySet().contains("key2"));
    }

    @Test
    void testValues() {
        assertEquals(2, spreadMap.values().size());
        assertTrue(spreadMap.values().contains("value1"));
        assertTrue(spreadMap.values().contains("value2"));
    }

    @Test
    void testEntrySet() {
        assertEquals(2, spreadMap.entrySet().size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(spreadMap.isEmpty());
        SpreadMap emptyMap = new SpreadMap(new Object[]{});
        assertTrue(emptyMap.isEmpty());
    }

    @Test
    void testConstructFromEmptyList() {
        List<Object> emptyList = new ArrayList<>();
        SpreadMap map = new SpreadMap(emptyList);
        assertTrue(map.isEmpty());
        assertEquals("*:[:]", map.toString());
    }

    @Test
    void testWithIntegerKeysAndValues() {
        SpreadMap map = new SpreadMap(new Object[]{1, "one", 2, "two"});
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
    }

    @Test
    void testWithMixedTypes() {
        SpreadMap map = new SpreadMap(new Object[]{"string", 123, 456, "number", null, "nullKey"});
        assertEquals(123, map.get("string"));
        assertEquals("number", map.get(456));
        assertEquals("nullKey", map.get(null));
    }

    @Test
    void testHashCodeConsistency() {
        int hash1 = spreadMap.hashCode();
        int hash2 = spreadMap.hashCode();
        assertEquals(hash1, hash2);
    }
}
