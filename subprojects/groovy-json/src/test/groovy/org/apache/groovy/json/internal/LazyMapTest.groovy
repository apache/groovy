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

import groovy.test.GroovyTestCase

class LazyMapTest extends GroovyTestCase {

    private LazyMap map

    void setUp() {
        super.setUp()
        map = new LazyMap()
    }

    // GROOVY-7302
    void testSizeWhenNoBackingMapCreated() {
        def map = new LazyMap()
        map.someProperty = "1"
        map.someProperty = "2"
        map.someProperty = "3"
        assert map.size() == 1
        map.someProperty2 = "4"
        assert map.size() == 2
    }

    void testSizeWhenLazyCreated() {
        def map = new LazyMap()
        map.someProperty1 = '1'
        assert map.@map == null
        map.someProperty2 = '2'
        assert map.@map == null
        map.someProperty3 = '3'
        assert map.@map == null
        map.someProperty4 = '4'
        assert map.@map == null
        map.someProperty5 = '5'
        assert map.@map == null
        map.someProperty6 = '6'
        assert map.@map == null
        map.someProperty7 = '7'
        assert map.someProperty6 == '6'
        assert map.@map?.size() == 7
    }

    void testDefaultConstructor() {
        def lazyMap = new LazyMap()
        assertTrue(lazyMap.isEmpty())
        assertEquals(0, lazyMap.size())
    }

    void testConstructorWithInitialSize() {
        def lazyMap = new LazyMap(10)
        assertTrue(lazyMap.isEmpty())
        assertEquals(0, lazyMap.size())
    }

    void testPut() {
        assertNull(map.put("key1", "value1"))
        assertEquals(1, map.size())
    }

    void testPutReturnsPreviousValue() {
        map.put("key", "value1")
        def previous = map.put("key", "value2")
        assertEquals("value1", previous)
        assertEquals("value2", map.get("key"))
    }

    void testPutMultipleEntries() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        map.put("key3", "value3")
        assertEquals(3, map.size())
    }

    void testPutWithNullKey() {
        map.put(null, "value")
        assertEquals("value", map.get(null))
    }

    void testPutWithNullValue() {
        map.put("key", null)
        assertNull(map.get("key"))
        assertTrue(map.containsKey("key"))
    }

    void testPutReplaceNullKey() {
        map.put(null, "value1")
        def previous = map.put(null, "value2")
        assertEquals("value1", previous)
        assertEquals("value2", map.get(null))
    }

    void testGet() {
        map.put("key", "value")
        assertEquals("value", map.get("key"))
    }

    void testGetNonExistentKey() {
        assertNull(map.get("nonexistent"))
    }

    void testGetTriggersMapBuild() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        // get should trigger the internal map to be built
        assertEquals("value1", map.get("key1"))
        assertEquals("value2", map.get("key2"))
    }

    void testSize() {
        assertEquals(0, map.size())
        map.put("key1", "value1")
        assertEquals(1, map.size())
        map.put("key2", "value2")
        assertEquals(2, map.size())
    }

    void testIsEmpty() {
        assertTrue(map.isEmpty())
        map.put("key", "value")
        assertFalse(map.isEmpty())
    }

    void testIsEmptyAfterClear() {
        map.put("key", "value")
        map.clear()
        assertTrue(map.isEmpty())
    }

    void testContainsKey() {
        map.put("key", "value")
        assertTrue(map.containsKey("key"))
        assertFalse(map.containsKey("nonexistent"))
    }

    void testContainsKeyWithNullKey() {
        map.put(null, "value")
        assertTrue(map.containsKey(null))
    }

    void testContainsValue() {
        map.put("key", "value")
        assertTrue(map.containsValue("value"))
        assertFalse(map.containsValue("nonexistent"))
    }

    void testContainsValueWithNullValue() {
        map.put("key", null)
        assertTrue(map.containsValue(null))
    }

    void testRemove() {
        map.put("key", "value")
        def removed = map.remove("key")
        assertEquals("value", removed)
        assertFalse(map.containsKey("key"))
    }

    void testRemoveNonExistent() {
        assertNull(map.remove("nonexistent"))
    }

    void testClearBeforeBuild() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        map.clear()
        assertEquals(0, map.size())
        assertTrue(map.isEmpty())
    }

    void testClearAfterBuild() {
        map.put("key", "value")
        map.get("key") // trigger build
        map.clear()
        assertEquals(0, map.size())
    }

    void testPutAll() {
        def other = [:]
        other.put("key1", "value1")
        other.put("key2", "value2")
        map.putAll(other)
        assertEquals(2, map.size())
        assertEquals("value1", map.get("key1"))
        assertEquals("value2", map.get("key2"))
    }

    void testKeySet() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        def keys = map.keySet()
        assertEquals(2, keys.size())
        assertTrue(keys.contains("key1"))
        assertTrue(keys.contains("key2"))
    }

    void testValues() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        def values = map.values()
        assertEquals(2, values.size())
        assertTrue(values.contains("value1"))
        assertTrue(values.contains("value2"))
    }

    void testEntrySet() {
        map.put("key1", "value1")
        map.put("key2", "value2")
        def entries = map.entrySet()
        assertEquals(2, entries.size())
    }

    void testEquals() {
        map.put("key", "value")
        def other = [:]
        other.put("key", "value")
        assertEquals(map, other)
    }

    void testHashCode() {
        map.put("key", "value")
        def other = [:]
        other.put("key", "value")
        assertEquals(map.hashCode(), other.hashCode())
    }

    void testToString() {
        map.put("key", "value")
        def str = map.toString()
        assertTrue(str.contains("key"))
        assertTrue(str.contains("value"))
    }

    void testCloneBeforeBuild() throws CloneNotSupportedException {
        // clone returns null if map hasn't been built yet
        def cloned = map.clone()
        assertNull(cloned)
    }

    void testCloneAfterBuild() throws CloneNotSupportedException {
        map.put("key", "value")
        map.get("key") // trigger build
        def cloned = map.clone()
        assertNotNull(cloned)
        assertTrue(cloned instanceof Map)
    }

    void testClearAndCopy() {
        map.put("key1", "value1")
        map.put("key2", "value2")

        def copy = map.clearAndCopy()

        // Original should be cleared
        assertEquals(0, map.size())

        // Copy should have the original values
        assertEquals(2, copy.size())
        assertEquals("value1", copy.get("key1"))
        assertEquals("value2", copy.get("key2"))
    }

    void testGrow() {
        String[] original = ["a", "b", "c"]
        def grown = LazyMap.grow(original)
        assertEquals(6, grown.length)
        assertEquals("a", grown[0])
        assertEquals("b", grown[1])
        assertEquals("c", grown[2])
    }

    void testArrayGrowthOnManyPuts() {
        // Add more than initial capacity (5) to trigger growth
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i)
        }
        assertEquals(10, map.size())
        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, map.get("key" + i))
        }
    }

    void testLazyBuildBehavior() {
        // Before any get/contains calls, the internal map isn't built
        map.put("key1", "value1")
        map.put("key2", "value2")
        assertEquals(2, map.size()) // size works without building

        // Trigger build
        map.get("key1")

        // Operations should still work after build
        assertEquals("value1", map.get("key1"))
        assertEquals("value2", map.get("key2"))
        map.put("key3", "value3")
        assertEquals(3, map.size())
    }

    void testWithDifferentValueTypes() {
        map.put("string", "text")
        map.put("number", 42)
        map.put("bool", true)
        map.put("null", null)
        map.put("nested", [:])

        assertEquals("text", map.get("string"))
        assertEquals(42, map.get("number"))
        assertEquals(true, map.get("bool"))
        assertNull(map.get("null"))
        assertTrue(map.get("nested") instanceof Map)
    }

    void testDuplicateKeyHandlingBeforeBuild() {
        map.put("key", "value1")
        map.put("key", "value2")
        map.put("key", "value3")

        assertEquals(1, map.size())
        assertEquals("value3", map.get("key"))
    }
}
