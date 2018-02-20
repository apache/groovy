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
package org.apache.groovy.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MapsTest {

    @Test
    public void inverse() {
        Map<String, Integer> map = Maps.of("a", 1, "b", 2, "c", 3);
        Map<Integer, String> inversedMap = Maps.inverse(map);

        Assert.assertEquals(map.size(), inversedMap.size());
        for (Map.Entry<Integer, String> entry : inversedMap.entrySet()) {
            Assert.assertEquals(map.get(entry.getValue()), entry.getKey());
        }

        try {
            Maps.inverse(Maps.of("a", 1, "b", 2, "c", 2));
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("duplicated key found: 2"));
        }

        Map<Integer, String> inversedMap2 = Maps.inverse(Maps.of("a", 1, "b", 2, "c", 2), true);
        Assert.assertEquals(2, inversedMap2.size());
        Assert.assertEquals("a", inversedMap2.get(1));
        Assert.assertEquals("c", inversedMap2.get(2));
    }
}