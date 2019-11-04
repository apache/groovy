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

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

public class UnlimitedConcurrentCacheTest {
    @Test
    public void get() {
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
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
        UnlimitedConcurrentCache<String, String> sc = new UnlimitedConcurrentCache<>();

        Assert.assertNull(sc.put("name", "Daniel"));
        Assert.assertEquals("Daniel", sc.get("name"));

        Assert.assertEquals("Daniel", sc.put("name", "sunlan"));
        Assert.assertEquals("sunlan", sc.get("name"));
    }

    @Test
    public void getAndPut() {
        UnlimitedConcurrentCache<String, String> sc = new UnlimitedConcurrentCache<>();

        EvictableCache.ValueProvider vp =
                (EvictableCache.ValueProvider<String, String>) key -> "Chinese";

        Assert.assertEquals("Chinese", sc.getAndPut("language", vp));
        Assert.assertEquals("Chinese", sc.get("language"));
    }

    @Test
    public void values() {
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals(new TreeSet<>(Arrays.asList("Daniel", "Male", "Shanghai")), new TreeSet<>(sc.values()));
    }

    @Test
    public void keys() {
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals(new TreeSet<>(Arrays.asList("name", "gender", "city")), new TreeSet<>(sc.keys()));
    }

    @Test
    public void containsKey() {
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
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
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
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
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
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
        UnlimitedConcurrentCache<String, String> sc =
                new UnlimitedConcurrentCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", "Shanghai")
                        )
                );

        Assert.assertEquals(new TreeSet<>(Arrays.asList("Daniel", "Male", "Shanghai")), new TreeSet<>(sc.clearAll().values()));
    }

    @Test
    public void cleanUpNullReferences() {
        UnlimitedConcurrentCache<String, Object> sc =
                new UnlimitedConcurrentCache<>(
                        new LinkedHashMap<>(
                                Maps.of("name", "Daniel",
                                        "gender", "Male",
                                        "city", new SoftReference(null))
                        )
                );

        sc.cleanUpNullReferences();
        Assert.assertEquals(new TreeSet<>(Arrays.asList("Daniel", "Male")), new TreeSet<>(sc.values()));
    }

}
