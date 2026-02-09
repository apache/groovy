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


/**
 * Demonstrates the use of the default named parameter in a closure
 */
class ClosureWithDefaultParamTest {

    void methodWithDefaultParam(example = 'default') {
        assert 'default' == example
    }

    @Test
    void testListCollect() {
        def list = [1, 2, 3, 4]
        def answer = list.collect { it * 2 }

        assert answer.size() == 4

        def expected = [2, 4, 6, 8]
        assert answer == expected
    }

    @Test
    void testMapCollect() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.collect { it.key + it.value }

        // lest sort the results since maps are in hash code order
        answer = answer.sort()

        assert answer.size() == 4
        assert answer == [3, 6, 9, 12]
        assert answer.get(0) == 3
        assert answer.get(1) == 6
        assert answer.get(2) == 9
        assert answer.get(3) == 12
    }

    @Test
    void testListFind() {
        def list = ["a", "b", "c"]
        def answer = list.find { it == "b" }
        assert answer == "b"

        answer = list.find { it == "z" }
        assert answer == null
    }

    @Test
    void testMapFind() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.find { it.value == 6 }
        assert answer != null
        assert answer.key == 3
        assert answer.value == 6

        answer = map.find { it.value == 0 }
        assert answer == null
    }

    @Test
    void testListFindAll() {
        def list = [20, 5, 40, 2]
        def answer = list.findAll { it < 10 }

        assert answer.size() == 2
        assert answer == [5, 2]
    }

    @Test
    void testMapFindAll() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.findAll { it.value > 5 }

        assert answer.size() == 2

        def keys = answer.collect { it.key }
        def values = answer.collect { it.value }

        // maps are in hash order so lets sort the results
        keys.sort()
        values.sort()

        assert keys == [3, 4]
        assert values == [6, 8]
    }

    @Test
    void testListEach() {
        def count = 0

        def list = [1, 2, 3, 4]
        list.each { count = count + it }

        assert count == 10

        list.each { count = count + it }

        assert count == 20
    }

    @Test
    void testMapEach() {
        def count = 0

        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        map.each { count = count + it.value }

        assert count == 20
    }

    @Test
    void testListEvery() {
        assert [1, 2, 3, 4].every { it < 5 }
        assert [1, 2, 7, 4].every { it < 5 } == false
    }

    @Test
    void testListAny() {
        assert [1, 2, 3, 4].any { it < 5 }
        assert [1, 2, 3, 4].any { it > 3 }
        assert [1, 2, 3, 4].any { it > 5 } == false
    }

    @Test
    void testJoin() {
        def value = [1, 2, 3].join('-')
        assert value == "1-2-3"
    }

    @Test
    void testListReverse() {
        def value = [1, 2, 3, 4].reverse()
        assert value == [4, 3, 2, 1]
    }

    @Test
    void testEachLine() {
        def file = new File("src/test/groovy/groovy/Bar.groovy")

        file.eachLine { assert it != null }
    }

    @Test
    void testReadLines() {
        def file = new File("src/test/groovy/groovy/Bar.groovy")

        def lines = file.readLines()

        assert lines != null
        assert lines.size() > 0
    }

    @Test
    void testEachFile() {
        def file = new File("src/test/groovy/groovy")

        file.eachFile { assert it.getName() }
    }
}
