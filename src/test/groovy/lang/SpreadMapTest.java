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
package groovy.lang;

import groovy.test.GroovyTestCase;

import java.util.Map;

/**
 * Tests the SpreadMap implementation.
 */
public class SpreadMapTest extends GroovyTestCase {
    Map map;
        
    public void setUp() {
        String[] list = new String[] { "key", "value", "name", "tim" };
        map = new SpreadMap(list);
    }
    
    public void testOriginal() {
        assertEquals(2, map.size());
        assertEquals("value", map.get("key"));
        assertEquals("tim", map.get("name"));
    }

    public void testMapMethods() {
        assertEquals(2, map.keySet().size());
        assertEquals(2, map.values().size());
        assertEquals(true, map.containsKey("key"));
        assertEquals(true, map.containsValue("tim"));
        assertEquals(2, map.entrySet().size());
        assertEquals(false, map.isEmpty());
    }
}
