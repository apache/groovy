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
package org.codehaus.groovy.runtime.memoize;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentCommonCacheTest {
    @Test
    public void get() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertEquals("Daniel", sc.get("name"));
        assertEquals("Male", sc.get("gender"));
        assertEquals("Shanghai", sc.get("city"));
        assertNull(sc.get("foo"));
    }

    @Test
    public void put() {
        ConcurrentCommonCache<String, String> sc = new ConcurrentCommonCache<>();

        assertNull(sc.put("name", "Daniel"));
        assertEquals("Daniel", sc.get("name"));

        assertEquals("Daniel", sc.put("name", "sunlan"));
        assertEquals("sunlan", sc.get("name"));
    }

    @Test
    public void getAndPut() {
        ConcurrentCommonCache<String, String> sc = new ConcurrentCommonCache<>();

        EvictableCache.ValueProvider vp =
                (EvictableCache.ValueProvider<String, String>) key -> "Chinese";

        assertEquals("Chinese", sc.getAndPut("language", vp,false));
        assertNull(sc.get("language"));

        assertEquals("Chinese", sc.getAndPut("language", vp));
        assertEquals("Chinese", sc.get("language"));
    }

    @Test
    public void values() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertArrayEquals(new String[] {"Daniel", "Male", "Shanghai"}, sc.values().toArray(new String[0]));
    }

    @Test
    public void keys() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertArrayEquals(new String[] {"name", "gender", "city"}, sc.keys().toArray(new String[0]));
    }

    @Test
    public void containsKey() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertTrue(sc.containsKey("name"));
    }

    @Test
    public void size() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertEquals(3, sc.size());
    }

    @Test
    public void remove() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new HashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertEquals("Shanghai", sc.remove("city"));
        assertNull(sc.get("city"));
    }

    @Test
    public void clear() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        assertArrayEquals(new String[] {"Daniel", "Male", "Shanghai"}, sc.clearAll().values().toArray(new String[0]));
    }

    @Test
    public void cleanUpNullReferences() {
        ConcurrentCommonCache<String, String> sc =
                new ConcurrentCommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", null)
                        )
                );

        sc.cleanUpNullReferences();
        assertArrayEquals(new String[] {"Daniel", "Male"}, sc.values().toArray(new String[0]));
    }

    @Test
    public void testLruCache() {
        ConcurrentCommonCache<String, String> sc = new ConcurrentCommonCache<>(3);
        sc.put("a", "1");
        sc.put("b", "2");
        sc.put("c", "3");
        sc.put("a", "4");
        sc.put("d", "5");
        assertEquals(3, sc.size());
        assertEquals("3", sc.get("c"));
        assertEquals("4", sc.get("a"));
        assertEquals("5", sc.get("d"));
    }

    @Test
    public void testFifoCache() {
        ConcurrentCommonCache<String, String> sc = new ConcurrentCommonCache<>(3, 3, EvictableCache.EvictionStrategy.FIFO);
        sc.put("a", "1");
        sc.put("b", "2");
        sc.put("c", "3");
        sc.put("a", "4");
        sc.put("d", "5");
        assertArrayEquals(new String[] {"b", "c", "d"}, sc.keys().toArray(new String[0]));
        assertEquals("2", sc.get("b"));
        assertEquals("3", sc.get("c"));
        assertEquals("5", sc.get("d"));
    }

    @Test
    public void testAccessCacheConcurrently() throws InterruptedException {
        final ConcurrentCommonCache<Integer, Integer> m = new ConcurrentCommonCache<>();

        final int threadNum = 30;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final CountDownLatch countDownLatch2 = new CountDownLatch(threadNum);

        final AtomicInteger cnt = new AtomicInteger(0);

        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();

                    m.getAndPut(123, k -> cnt.getAndIncrement());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch2.countDown();
                }
            }).start();
        }

        countDownLatch.countDown();
        countDownLatch2.await();

        assertEquals(1, cnt.get());
    }
}
