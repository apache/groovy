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

package org.apache.groovy.util.concurrentlinkedhashmap;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConcurrentLinkedHashMapTest {
    @Test
    public void computeIfAbsent() {
        ConcurrentLinkedHashMap m = new ConcurrentLinkedHashMap.Builder<>()
                .maximumWeightedCapacity(3)
                .build();

        assertEquals(1, m.computeIfAbsent("a", k -> 1));
        assertEquals(1, m.computeIfAbsent("a", k -> 2));

        assertEquals(1, m.get("a"));

        assertEquals(3, m.computeIfAbsent("b", k -> 3));
        assertEquals(4, m.computeIfAbsent("c", k -> 4));
        assertEquals(5, m.computeIfAbsent("d", k -> 5));
        assertEquals(5, m.computeIfAbsent("d", k -> 6));

        assertArrayEquals(new String[] {"b", "c", "d"}, m.keySet().toArray(new String[0]));
        assertArrayEquals(new Integer[] {3, 4, 5}, m.values().toArray(new Integer[0]));
    }

}