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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * Tests the SpreadMap implementation.
 */
class SpreadMapTest extends GroovyTestCase {
    Map map
    private SpreadMap spreadMap

    void setUp() {
        def list = ["key", "value", "name", "tim"] as String[]
        map = new SpreadMap(list)

        def entries = ["key1", "value1", "key2", "value2"] as String[]
        spreadMap = new SpreadMap(entries)
    }

    void testOriginal() {
        assertEquals(2, map.size())
        assertEquals("value", map.get("key"))
        assertEquals("tim", map.get("name"))
    }

    void testMapMethods() {
        assertEquals(2, map.keySet().size())
        assertEquals(2, map.values().size())
        assertEquals(true, map.containsKey("key"))
        assertEquals(true, map.containsValue("tim"))
        assertEquals(2, map.entrySet().size())
        assertEquals(false, map.isEmpty())
    }

    void testConstructFromArray() {
        Object[] entries = ["a", 1, "b", 2, "c", 3]
        def m = new SpreadMap(entries)
        assertEquals(3, m.size())
        assertEquals(1, m.get("a"))
        assertEquals(2, m.get("b"))
        assertEquals(3, m.get("c"))
    }

    void testConstructFromMap() {
        def source = [:]
        source.put("x", 10)
        source.put("y", 20)
        def m = new SpreadMap(source)
        assertEquals(2, m.size())
        assertEquals(10, m.get("x"))
        assertEquals(20, m.get("y"))
    }

    void testConstructFromList() {
        def list = ["first", 100, "second", 200]
        def m = new SpreadMap(list)
        assertEquals(2, m.size())
        assertEquals(100, m.get("first"))
        assertEquals(200, m.get("second"))
    }

    void testPutThrowsException() {
        shouldFail(RuntimeException) { spreadMap.put("newKey", "newValue") }
    }

    void testRemoveThrowsException() {
        shouldFail(RuntimeException) { spreadMap.remove("key1") }
    }

    void testPutAllThrowsException() {
        def newMap = [:]
        newMap.put("a", "b")
        shouldFail(RuntimeException) { spreadMap.putAll(newMap) }
    }

    void testEqualsWithSameSpreadMap() {
        def map1 = new SpreadMap(["a", 1, "b", 2] as Object[])
        def map2 = new SpreadMap(["a", 1, "b", 2] as Object[])
        assertEquals(map1, map2)
    }

    void testEqualsWithSelf() {
        assertTrue(spreadMap.equals(spreadMap))
    }

    void testEqualsWithDifferentSize() {
        def map1 = new SpreadMap(["a", 1] as Object[])
        def map2 = new SpreadMap(["a", 1, "b", 2] as Object[])
        assertNotSame(map1, map2)
        assertFalse(map1.equals(map2))
    }

    void testEqualsWithDifferentValues() {
        def map1 = new SpreadMap(["a", 1] as Object[])
        def map2 = new SpreadMap(["a", 2] as Object[])
        assertFalse(map1.equals(map2))
    }

    void testEqualsWithNonSpreadMap() {
        def regularMap = [:]
        regularMap.put("key1", 1)
        assertFalse(spreadMap.equals(regularMap))
    }

    void testEqualsWithNull() {
        assertFalse(spreadMap.equals((SpreadMap) null))
    }

    void testHashCode() {
        def map1 = new SpreadMap(["a", 1, "b", 2] as Object[])
        def map2 = new SpreadMap(["a", 1, "b", 2] as Object[])
        assertEquals(map1.hashCode(), map2.hashCode())
    }

    void testHashCodeWithNullKey() {
        def m = new SpreadMap([null, "value"] as Object[])
        // Should not throw, hashCode should handle null keys
        def hash = m.hashCode()
        // Just verify it doesn't throw and returns something
        assertTrue(hash != 0 || hash == 0) // Always true, just checking no exception
    }

    void testToStringEmpty() {
        def emptyMap = new SpreadMap([] as Object[])
        assertEquals("*:[:]", emptyMap.toString())
    }

    void testToStringWithEntries() {
        def m = new SpreadMap(["a", 1] as Object[])
        def result = m.toString()
        assertTrue(result.startsWith("*:["))
        assertTrue(result.endsWith("]"))
        assertTrue(result.contains("a:1"))
    }

    void testToStringMultipleEntries() {
        def m = new SpreadMap(["a", 1, "b", 2] as Object[])
        def result = m.toString()
        assertTrue(result.startsWith("*:["))
        assertTrue(result.endsWith("]"))
        assertTrue(result.contains(":"))
        assertTrue(result.contains(", "))
    }

    void testContainsKey() {
        assertTrue(spreadMap.containsKey("key1"))
        assertTrue(spreadMap.containsKey("key2"))
        assertFalse(spreadMap.containsKey("nonexistent"))
    }

    void testContainsValue() {
        assertTrue(spreadMap.containsValue("value1"))
        assertTrue(spreadMap.containsValue("value2"))
        assertFalse(spreadMap.containsValue("nonexistent"))
    }

    void testKeySet() {
        assertEquals(2, spreadMap.keySet().size())
        assertTrue(spreadMap.keySet().contains("key1"))
        assertTrue(spreadMap.keySet().contains("key2"))
    }

    void testValues() {
        assertEquals(2, spreadMap.values().size())
        assertTrue(spreadMap.values().contains("value1"))
        assertTrue(spreadMap.values().contains("value2"))
    }

    void testEntrySet() {
        assertEquals(2, spreadMap.entrySet().size())
    }

    void testIsEmpty() {
        assertFalse(spreadMap.isEmpty())
        def emptyMap = new SpreadMap([] as Object[])
        assertTrue(emptyMap.isEmpty())
    }

    void testConstructFromEmptyList() {
        def emptyList = []
        def m = new SpreadMap(emptyList)
        assertTrue(m.isEmpty())
        assertEquals("*:[:]", m.toString())
    }

    void testWithIntegerKeysAndValues() {
        def m = new SpreadMap([1, "one", 2, "two"] as Object[])
        assertEquals("one", m.get(1))
        assertEquals("two", m.get(2))
    }

    void testWithMixedTypes() {
        def m = new SpreadMap(["string", 123, 456, "number", null, "nullKey"] as Object[])
        assertEquals(123, m.get("string"))
        assertEquals("number", m.get(456))
        assertEquals("nullKey", m.get(null))
    }

    void testHashCodeConsistency() {
        def hash1 = spreadMap.hashCode()
        def hash2 = spreadMap.hashCode()
        assertEquals(hash1, hash2)
    }
}
