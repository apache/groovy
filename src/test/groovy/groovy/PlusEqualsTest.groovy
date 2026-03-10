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
package groovy

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue


class PlusEqualsTest {

    @Test
    void testIntegerPlusEquals() {
        def x = 1
        def y = 2
        x += y

        assert x == 3

        y += 10

        assert y == 12
    }

    @Test
    void testCharacterPlusEquals() {
        Character x = 1
        Character y = 2
        x += y

        assert x == 3

        y += 10

        assert y == 12
    }

    @Test
    void testNumberPlusEquals() {
        def x = 1.2
        def y = 2
        x += y

        assert x == 3.2

        y += 10.1

        assert y == 12.1
    }

    @Test
    void testStringPlusEquals() {
        def x = "bbc"
        def y = 2
        x += y

        assert x == "bbc2"

        def foo = "nice cheese"
        foo += " gromit"

        assert foo == "nice cheese gromit"
    }

    @Test
    void testSortedSetPlusEquals() {
        def sortedSet = new TreeSet()
        sortedSet += 'abc'
        assertTrue sortedSet instanceof SortedSet,
                   'sortedSet should have been a SortedSet'
        sortedSet += ['def', 'ghi']
        assertTrue sortedSet instanceof SortedSet,
                   'sortedSet should have been a SortedSet'
        assertEquals 3, sortedSet.size(),
                     'sortedSet had wrong number of elements'
    }
}
