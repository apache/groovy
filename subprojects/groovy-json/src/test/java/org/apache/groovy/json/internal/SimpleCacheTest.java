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

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for SimpleCache class.
 */
class SimpleCacheTest {

    @Test
    void testConstructorWithLimit() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        assertNotNull(cache);
        assertEquals(0, cache.size());
    }

    @Test
    void testConstructorWithLimitAndLRUType() {
        SimpleCache<String, String> cache = new SimpleCache<>(10, CacheType.LRU);
        assertNotNull(cache);
    }

    @Test
    void testConstructorWithLimitAndFIFOType() {
        SimpleCache<String, String> cache = new SimpleCache<>(10, CacheType.FIFO);
        assertNotNull(cache);
    }

    @Test
    void testPutAndGet() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    void testGetNonExistent() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        assertNull(cache.get("nonexistent"));
    }

    @Test
    void testPutMultiple() {
        SimpleCache<String, Integer> cache = new SimpleCache<>(10);
        
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        
        assertEquals(3, cache.size());
        assertEquals(1, cache.get("one"));
        assertEquals(2, cache.get("two"));
        assertEquals(3, cache.get("three"));
    }

    @Test
    void testPutOverwrite() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        
        cache.put("key", "original");
        assertEquals("original", cache.get("key"));
        
        cache.put("key", "updated");
        assertEquals("updated", cache.get("key"));
    }

    @Test
    void testRemove() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        
        cache.put("key", "value");
        assertEquals(1, cache.size());
        
        cache.remove("key");
        assertNull(cache.get("key"));
    }

    @Test
    void testRemoveNonExistent() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        // Should not throw
        assertDoesNotThrow(() -> cache.remove("nonexistent"));
    }

    @Test
    void testSize() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        assertEquals(0, cache.size());
        
        cache.put("a", "1");
        assertEquals(1, cache.size());
        
        cache.put("b", "2");
        cache.put("c", "3");
        assertEquals(3, cache.size());
    }

    @Test
    void testGetSilent() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        
        cache.put("key", "value");
        
        // getSilent should return the value without affecting LRU order
        String value = cache.getSilent("key");
        assertEquals("value", value);
    }

    @Test
    void testGetSilentNonExistent() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        assertNull(cache.getSilent("nonexistent"));
    }

    @Test
    void testToString() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        cache.put("key", "value");
        
        String str = cache.toString();
        assertNotNull(str);
    }

    @Test
    void testEvictionLRU() {
        // Small cache that will evict
        SimpleCache<Integer, String> cache = new SimpleCache<>(3, CacheType.LRU);
        
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        
        // Access 1 to make it recently used
        cache.get(1);
        
        // Add 4, should evict least recently used (2)
        cache.put(4, "four");
        
        assertEquals(3, cache.size());
        assertNotNull(cache.get(1)); // 1 should still be there
        assertNotNull(cache.get(4)); // 4 should be there
    }

    @Test
    void testEvictionFIFO() {
        // Small cache that will evict
        SimpleCache<Integer, String> cache = new SimpleCache<>(3, CacheType.FIFO);
        
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        
        // Add 4, should evict first in (1)
        cache.put(4, "four");
        
        assertEquals(3, cache.size());
        assertNotNull(cache.get(4)); // 4 should be there
    }

    @Test
    void testWithNullValue() {
        SimpleCache<String, String> cache = new SimpleCache<>(10);
        
        // Null values are not supported - throws NullPointerException
        assertThrows(NullPointerException.class, () -> {
            cache.put("key", null);
        });
    }

    @Test
    void testCacheTypeLRU() {
        assertEquals(CacheType.LRU, CacheType.valueOf("LRU"));
    }

    @Test
    void testCacheTypeFIFO() {
        assertEquals(CacheType.FIFO, CacheType.valueOf("FIFO"));
    }

    @Test
    void testLargeCache() {
        SimpleCache<Integer, Integer> cache = new SimpleCache<>(1000);
        
        for (int i = 0; i < 500; i++) {
            cache.put(i, i * 2);
        }
        
        assertEquals(500, cache.size());
        assertEquals(0, cache.get(0));
        assertEquals(998, cache.get(499));
    }
}
