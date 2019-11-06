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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class CommonCacheTest {
    @Test
    public void get() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals("Daniel", sc.get("name"));
        Assert.assertEquals("Male", sc.get("gender"));
        Assert.assertEquals("Shanghai", sc.get("city"));
        Assert.assertNull(sc.get("foo"));
    }

    @Test
    public void put() {
        CommonCache<String, String> sc = new CommonCache<>();

        Assert.assertNull(sc.put("name", "Daniel"));
        Assert.assertEquals("Daniel", sc.get("name"));

        Assert.assertEquals("Daniel", sc.put("name", "sunlan"));
        Assert.assertEquals("sunlan", sc.get("name"));
    }

    @Test
    public void getAndPut() {
        CommonCache<String, String> sc = new CommonCache<>();

        EvictableCache.ValueProvider vp =
                (EvictableCache.ValueProvider<String, String>) key -> "Chinese";

        Assert.assertEquals("Chinese", sc.getAndPut("language", vp,false));
        Assert.assertNull(sc.get("language"));

        Assert.assertEquals("Chinese", sc.getAndPut("language", vp));
        Assert.assertEquals("Chinese", sc.get("language"));
    }

    @Test
    public void values() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertArrayEquals(new String[] {"Daniel", "Male", "Shanghai"}, sc.values().toArray(new String[0]));
    }

    @Test
    public void keys() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertArrayEquals(new String[] {"name", "gender", "city"}, sc.keys().toArray(new String[0]));
    }

    @Test
    public void containsKey() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertTrue(sc.containsKey("name"));
    }

    @Test
    public void size() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals(3, sc.size());
    }

    @Test
    public void remove() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new HashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals("Shanghai", sc.remove("city"));
        Assert.assertNull(sc.get("city"));
    }

    @Test
    public void clear() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertArrayEquals(new String[] {"Daniel", "Male", "Shanghai"}, sc.clearAll().values().toArray(new String[0]));
    }

    @Test
    public void cleanUpNullReferences() {
        CommonCache<String, String> sc =
                new CommonCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", null)
                        )
                );

        sc.cleanUpNullReferences();
        Assert.assertArrayEquals(new String[] {"Daniel", "Male"}, sc.values().toArray(new String[0]));
    }

    @Test
    public void testLruCache() {
        CommonCache<String, String> sc = new CommonCache<>(3);
        sc.put("a", "1");
        sc.put("b", "2");
        sc.put("c", "3");
        sc.put("a", "4");
        sc.put("d", "5");
        Assert.assertArrayEquals(new String[] {"c", "a", "d"}, sc.keys().toArray(new String[0]));
        Assert.assertEquals("3", sc.get("c"));
        Assert.assertEquals("4", sc.get("a"));
        Assert.assertEquals("5", sc.get("d"));
    }

    @Test
    public void testFifoCache() {
        CommonCache<String, String> sc = new CommonCache<>(3, 3, EvictableCache.EvictionStrategy.FIFO);
        sc.put("a", "1");
        sc.put("b", "2");
        sc.put("c", "3");
        sc.put("a", "4");
        sc.put("d", "5");
        Assert.assertArrayEquals(new String[] {"b", "c", "d"}, sc.keys().toArray(new String[0]));
        Assert.assertEquals("2", sc.get("b"));
        Assert.assertEquals("3", sc.get("c"));
        Assert.assertEquals("5", sc.get("d"));
    }
}