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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for LazyMap class.
 */
class LazyMapJUnit5Test {

    private LazyMap map;

    @BeforeEach
    void setUp() {
        map = new LazyMap();
    }

    // Constructor tests
    @Test
    void testDefaultConstructor() {
        LazyMap lazyMap = new LazyMap();
        assertTrue(lazyMap.isEmpty());
        assertEquals(0, lazyMap.size());
    }

    @Test
    void testConstructorWithInitialSize() {
        LazyMap lazyMap = new LazyMap(10);
        assertTrue(lazyMap.isEmpty());
        assertEquals(0, lazyMap.size());
    }

    // put tests
    @Test
    void testPut() {
        assertNull(map.put("key1", "value1"));
        assertEquals(1, map.size());
    }

    @Test
    void testPutReturnsPreviousValue() {
        map.put("key", "value1");
        Object previous = map.put("key", "value2");
        assertEquals("value1", previous);
        assertEquals("value2", map.get("key"));
    }

    @Test
    void testPutMultipleEntries() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        assertEquals(3, map.size());
    }

    @Test
    void testPutWithNullKey() {
        map.put(null, "value");
        assertEquals("value", map.get(null));
    }

    @Test
    void testPutWithNullValue() {
        map.put("key", null);
        assertNull(map.get("key"));
        assertTrue(map.containsKey("key"));
    }

    @Test
    void testPutReplaceNullKey() {
        map.put(null, "value1");
        Object previous = map.put(null, "value2");
        assertEquals("value1", previous);
        assertEquals("value2", map.get(null));
    }

    // get tests
    @Test
    void testGet() {
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testGetNonExistentKey() {
        assertNull(map.get("nonexistent"));
    }

    @Test
    void testGetTriggersMapBuild() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        // get should trigger the internal map to be built
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // size and isEmpty tests
    @Test
    void testSize() {
        assertEquals(0, map.size());
        map.put("key1", "value1");
        assertEquals(1, map.size());
        map.put("key2", "value2");
        assertEquals(2, map.size());
    }

    @Test
    void testIsEmpty() {
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertFalse(map.isEmpty());
    }

    @Test
    void testIsEmptyAfterClear() {
        map.put("key", "value");
        map.clear();
        assertTrue(map.isEmpty());
    }

    // containsKey tests
    @Test
    void testContainsKey() {
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test
    void testContainsKeyWithNullKey() {
        map.put(null, "value");
        assertTrue(map.containsKey(null));
    }

    // containsValue tests
    @Test
    void testContainsValue() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("nonexistent"));
    }

    @Test
    void testContainsValueWithNullValue() {
        map.put("key", null);
        assertTrue(map.containsValue(null));
    }

    // remove tests
    @Test
    void testRemove() {
        map.put("key", "value");
        Object removed = map.remove("key");
        assertEquals("value", removed);
        assertFalse(map.containsKey("key"));
    }

    @Test
    void testRemoveNonExistent() {
        assertNull(map.remove("nonexistent"));
    }

    // clear tests
    @Test
    void testClearBeforeBuild() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    void testClearAfterBuild() {
        map.put("key", "value");
        map.get("key"); // trigger build
        map.clear();
        assertEquals(0, map.size());
    }

    // putAll tests
    @Test
    void testPutAll() {
        Map<String, Object> other = new HashMap<>();
        other.put("key1", "value1");
        other.put("key2", "value2");
        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // keySet tests
    @Test
    void testKeySet() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }

    // values tests
    @Test
    void testValues() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        Collection<Object> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    // entrySet tests
    @Test
    void testEntrySet() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        assertEquals(2, entries.size());
    }

    // equals and hashCode tests
    @Test
    void testEquals() {
        map.put("key", "value");
        Map<String, Object> other = new HashMap<>();
        other.put("key", "value");
        assertEquals(map, other);
    }

    @Test
    void testHashCode() {
        map.put("key", "value");
        Map<String, Object> other = new HashMap<>();
        other.put("key", "value");
        assertEquals(map.hashCode(), other.hashCode());
    }

    // toString tests
    @Test
    void testToString() {
        map.put("key", "value");
        String str = map.toString();
        assertTrue(str.contains("key"));
        assertTrue(str.contains("value"));
    }

    // clone tests
    @Test
    void testCloneBeforeBuild() throws CloneNotSupportedException {
        // clone returns null if map hasn't been built yet
        Object cloned = map.clone();
        assertNull(cloned);
    }

    @Test
    void testCloneAfterBuild() throws CloneNotSupportedException {
        map.put("key", "value");
        map.get("key"); // trigger build
        Object cloned = map.clone();
        assertNotNull(cloned);
        assertTrue(cloned instanceof Map);
    }

    // clearAndCopy tests
    @Test
    void testClearAndCopy() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        LazyMap copy = map.clearAndCopy();
        
        // Original should be cleared
        assertEquals(0, map.size());
        
        // Copy should have the original values
        assertEquals(2, copy.size());
        assertEquals("value1", copy.get("key1"));
        assertEquals("value2", copy.get("key2"));
    }

    // grow tests
    @Test
    void testGrow() {
        String[] original = {"a", "b", "c"};
        String[] grown = LazyMap.grow(original);
        assertEquals(6, grown.length);
        assertEquals("a", grown[0]);
        assertEquals("b", grown[1]);
        assertEquals("c", grown[2]);
    }

    // Test array growth when adding many items
    @Test
    void testArrayGrowthOnManyPuts() {
        // Add more than initial capacity (5) to trigger growth
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        assertEquals(10, map.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, map.get("key" + i));
        }
    }

    // Test lazy build behavior
    @Test
    void testLazyBuildBehavior() {
        // Before any get/contains calls, the internal map isn't built
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertEquals(2, map.size()); // size works without building
        
        // Trigger build
        map.get("key1");
        
        // Operations should still work after build
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
        map.put("key3", "value3");
        assertEquals(3, map.size());
    }

    // Test with different value types
    @Test
    void testWithDifferentValueTypes() {
        map.put("string", "text");
        map.put("number", 42);
        map.put("bool", true);
        map.put("null", null);
        map.put("nested", new HashMap<String, Object>());
        
        assertEquals("text", map.get("string"));
        assertEquals(42, map.get("number"));
        assertEquals(true, map.get("bool"));
        assertNull(map.get("null"));
        assertTrue(map.get("nested") instanceof Map);
    }

    // Test duplicate key handling
    @Test
    void testDuplicateKeyHandlingBeforeBuild() {
        map.put("key", "value1");
        map.put("key", "value2");
        map.put("key", "value3");
        
        assertEquals(1, map.size());
        assertEquals("value3", map.get("key"));
    }
}
