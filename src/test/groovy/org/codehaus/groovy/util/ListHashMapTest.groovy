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
package org.codehaus.groovy.util

import org.junit.Test

final class ListHashMapTest {

    private final ListHashMap lhm = new ListHashMap(2)

    @Test
    void testEmptyAtFirst() {
        assert lhm.isEmpty()
        assert lhm.size() == 0
        assert lhm.@keys.length == 2
    }

    @Test
    void testInsertElement() {
        lhm.put('a', 'a')
        assert lhm.size() == 1
        assert lhm.@innerMap == null
    }

    @Test
    void testInsertTwoElements() {
        lhm.put('a', 'a')
        lhm.put('b', 'b')
        assert lhm.size() == 2
        assert lhm.@innerMap == null
    }

    @Test
    void testInsertWithSameKey() {
        assert lhm.put('a', 'a') == null
        assert lhm.put('a', 'b') == 'a'
        assert lhm.size() == 1
        assert lhm.get('a') == 'b'
        assert lhm.@innerMap == null
    }

    @Test
    void testSwitchToInnerMap() {
        assert lhm.put('a', 'a') == null
        assert lhm.put('b', 'b') == null
        assert lhm.put('c', 'c') == null
        assert lhm.size() == 3
        assert lhm.@innerMap != null
        assert lhm.@innerMap.size() == 3
    }

    @Test
    void testSwitchToInnerMapThenFallbackToList() {
        testSwitchToInnerMap()
        assert lhm.remove('c') == 'c'
        assert lhm.size() == 2
        assert lhm.@innerMap == null
        assert lhm.keySet() == ['a','b'] as Set
    }

    @Test
    void testPutNullValue() {
        lhm.put('a', 'xx')
        lhm.put('a', null)
        assert lhm.a == null
        assert lhm.isEmpty()
    }

    @Test
    void testRemoveNullValue() {
        testPutNullValue()
        assert lhm.remove('a') == null
        assert lhm.isEmpty()
    }

    @Test
    void testPutAll() {
        lhm.putAll(a: '1', b: '2', c: '3')
        assert lhm.size() == 3
        assert lhm.@innerMap != null
        assert lhm.@innerMap.size() == 3
        assert lhm.keySet() == ['a','b','c'] as Set
        assert lhm.values()  as Set == ['1','2','3'] as Set
    }

    @Test
    void testPutAllTwice() {
        testPutAll()
        testPutAll()
    }

    @Test
    void testRemoveAll() {
        testPutAll()
        assert lhm.remove('a') == '1'
        assert lhm.remove('b') == '2'
        assert lhm.remove('c') == '3'
        assert lhm.isEmpty()
        assert lhm.@innerMap == null
    }

    @Test
    void testRemoveFirstShiftsKeyValuesAndClearsArraySlot() {
        lhm.putAll(a: '1', b: '2')
        assert lhm.size() == 2
        assert lhm.@innerMap == null

        lhm.remove('a')
        assert lhm.size() == 1
        assert lhm.@keys[0] == 'b'
        assert lhm.@values[0] == '2'
        assert lhm.@keys[1] == null
        assert lhm.@values[1] == null

        lhm.put('c', '3')
        assert lhm.size() == 2
        assert lhm.@keys[0] == 'b'
        assert lhm.@values[0] == '2'
        assert lhm.@keys[1] == 'c'
        assert lhm.@values[1] == '3'
    }

    @Test
    void testRemoveLastClearsLastArraySlot() {
        lhm.putAll(a: '1', b: '2')
        assert lhm.size() == 2
        assert lhm.@innerMap == null

        lhm.remove('b')
        assert lhm.size() == 1
        assert lhm.@keys[0] == 'a'
        assert lhm.@values[0] == '1'
        assert lhm.@keys[1] == null
        assert lhm.@values[1] == null

        lhm.put('c', '3')
        assert lhm.size() == 2
        assert lhm.@keys[0] == 'a'
        assert lhm.@values[0] == '1'
        assert lhm.@keys[1] == 'c'
        assert lhm.@values[1] == '3'
    }

    @Test
    void testSwitchToInnerMapClearsArrays() {
        lhm.putAll(a: '1', b: '2')
        assert lhm.size() == 2
        assert lhm.@keys[0] == 'a'
        assert lhm.@keys[1] == 'b'
        assert lhm.@innerMap == null

        lhm.put('c', '3')
        assert lhm.size() == 3
        assert lhm.@innerMap != null
        assert lhm.@keys[0] == null
        assert lhm.@keys[1] == null
        assert lhm.@values[0] == null
        assert lhm.@values[1] == null
    }

    @Test
    void testContainsKey() {
        lhm.putAll(a: '1', b: '2')
        assert lhm.containsKey('b')
        assert !lhm.containsKey('c')
    }

    @Test
    void testContainsValue() {
        lhm.putAll(a: '1', b: '2')
        assert lhm.containsValue('2')
        assert !lhm.containsValue('3')
    }

    @Test(expected = UnsupportedOperationException)
    void testCannotModifyInnerMapViaKeySet() {
        testSwitchToInnerMap()
        lhm.keySet().clear()
    }

    @Test(expected = UnsupportedOperationException)
    void testCannotModifyInnerMapViaEntrySet() {
        testSwitchToInnerMap()
        lhm.entrySet().clear()
    }

    @Test(expected = UnsupportedOperationException)
    void testCannotModifyInnerMapViaValueCol() {
        testSwitchToInnerMap()
        lhm.values().clear()
    }
}
