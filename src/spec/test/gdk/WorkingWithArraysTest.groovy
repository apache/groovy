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
package gdk

import groovy.test.GroovyTestCase

class WorkingWithArraysTest extends GroovyTestCase {
    void testArrayLiterals() {
        // tag::array_literals[]
        Integer[] nums = [5, 6, 7, 8]
        assert nums[1] == 6
        assert nums.getAt(2) == 7                // alternative syntax
        assert nums[-1] == 8                     // negative indices
        assert nums instanceof Integer[]

        int[] primes = [2, 3, 5, 7]              // primitives
        assert primes instanceof int[]

        def evens = new int[]{2, 4, 6}           // alt syntax 1
        assert evens instanceof int[]

        def odds = [1, 3, 5] as int[]            // alt syntax 2
        assert odds instanceof int[]

        // empty array examples
        Integer[] emptyNums = []
        assert emptyNums instanceof Integer[] && emptyNums.size() == 0

        def emptyStrings = new String[]{}        // alternative syntax 1
        assert emptyStrings instanceof String[] && emptyStrings.size() == 0

        var emptyObjects = new Object[0]         // alternative syntax 2
        assert emptyObjects instanceof Object[] && emptyObjects.size() == 0
        // end::array_literals[]
    }

    void testArrayIteration() {
        // tag::array_each[]
        String[] vowels = ['a', 'e', 'i', 'o', 'u']
        var result = ''
        vowels.each {
            result += it
        }
        assert result == 'aeiou'
        result = ''
        vowels.eachWithIndex { v, i ->
            result += v * i         // index starts from 0
        }
        assert result == 'eiiooouuuu'
        // end::array_each[]
    }

    void testListCollect() {
        // tag::array_gdk[]
        int[] nums = [1, 2, 3]
        def doubled = nums.collect { it * 2 }
        assert doubled == [2, 4, 6] && doubled instanceof List
        def tripled = nums*.multiply(3)
        assert tripled == [3, 6, 9] && doubled instanceof List

        assert nums.any{ it > 2 }
        assert nums.every{ it < 4 }
        assert nums.average() == 2
        assert nums.min() == 1
        assert nums.max() == 3
        assert nums.sum() == 6
        assert nums.indices == [0, 1, 2]
        assert nums.swap(0, 2) == [3, 2, 1] as int[]
        // end::array_gdk[]
    }
}
