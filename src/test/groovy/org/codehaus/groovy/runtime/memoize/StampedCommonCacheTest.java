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

public class StampedCommonCacheTest {
    @Test
    public void get() {
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc = new StampedCommonCache<>();

        assertNull(sc.put("name", "Daniel"));
        assertEquals("Daniel", sc.get("name"));

        assertEquals("Daniel", sc.put("name", "sunlan"));
        assertEquals("sunlan", sc.get("name"));
    }

    @Test
    public void getAndPut() {
        StampedCommonCache<String, String> sc = new StampedCommonCache<>();

        EvictableCache.ValueProvider vp =
                (EvictableCache.ValueProvider<String, String>) key -> "Chinese";

        assertEquals("Chinese", sc.getAndPut("language", vp,false));
        assertNull(sc.get("language"));

        assertEquals("Chinese", sc.getAndPut("language", vp));
        assertEquals("Chinese", sc.get("language"));
    }

    @Test
    public void values() {
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc =
                new StampedCommonCache<>(
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
        StampedCommonCache<String, String> sc = new StampedCommonCache<>(3);
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
        StampedCommonCache<String, String> sc = new StampedCommonCache<>(3, 3, EvictableCache.EvictionStrategy.FIFO);
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
        final StampedCommonCache<Integer, Integer> m = new StampedCommonCache<>();

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
