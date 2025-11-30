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

import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the various Closure methods in Groovy
 */
final class ClosureMethodTest {

    @Test
    void testListCollect() {
        def list = [1, 2, 3, 4]
        def answer = list.collect { item -> return item * 2 }
        assert answer.size() == 4
        def expected = [2, 4, 6, 8]
        assert answer == expected
    }

    @Test
    void testMapCollect() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.collect { e -> return e.key + e.value }
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
    void testObjectFindResult() {
        def oneToTenObjectIterator = {
            def i = 1
            [hasNext: { i <= 10 }, next: { i++ }] as Iterator
        }

        assert "Found 3" == oneToTenObjectIterator().findResult {
            if (it > 2) return "Found $it"
        }

        assert false == oneToTenObjectIterator().findResult {
            if (it > 2) return false
        }

        assert null == oneToTenObjectIterator().findResult {
            if (false) return "won't get here, should return null"
        }

        assert "Found 3" == oneToTenObjectIterator().findResult("default") {
            if (it > 2) return "Found $it"
        }

        assert "default" == oneToTenObjectIterator().findResult("default") {
            if (false) return "won't get here, should return null"
        }
    }

    @Test
    void testListFind() {
        def list = ["a", "b", "c"]
        def answer = list.find { item -> item == "b" }
        assert answer == "b"
        answer = list.find { item -> item == "z" }
        assert answer == null
    }

    @Test
    void testListFindResult() {
        Collection<Integer> oneThroughFive = [1, 2, 3, 4, 5]

        assert "I found 4" == oneThroughFive.findResult {
            if (it > 3) return "I found $it"
        }

        assert null == oneThroughFive.findResult {
            if (it > 8) return "I found $it"
        }

        assert null == [].findResult {
            if (it) return "I found $it"
        }

        assert false == oneThroughFive.findResult {
            if (it > 3) return false  // a result of false is a valid result
        }

        assert "I found 3" == oneThroughFive.findResult("default") {
            if (it > 2) return "I found $it"
        }

        assert "default" == oneThroughFive.findResult("default") {
            if (it > 8) return "I found $it"
        }
    }

    @Test
    void testMapFind() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.find { entry -> entry.value == 6 }
        assert answer != null
        assert answer.key == 3
        assert answer.value == 6
        answer = map.find { entry -> entry.value == 0 }
        assert answer == null
        answer = map.find { k, v -> v > 5 }
        assert answer instanceof Map.Entry
        assert answer.key == 3
        assert answer.value == 6

        answer = map.find { k, v -> k == 2 }
        assert answer instanceof Map.Entry
        assert answer.key == 2
        assert answer.value == 4
    }

    @Test
    void testMapFindResult() {
        def oneThroughFourMap = [a: 1, b: 2, c: 3, d: 4]

        assert "I found c:3" == oneThroughFourMap.findResult { entry ->
            if (entry.value > 2) return "I found ${entry.key}:${entry.value}"
        }

        assert "I found c:3" == oneThroughFourMap.findResult { key, value ->
            if (value > 2) return "I found ${key}:${value}"
        }

        assert false == oneThroughFourMap.findResult {
            if (it.value > 2) return false // a result of false is a valid result
        }

        assert "I found c:3" == oneThroughFourMap.findResult("default") { key, value ->
            if (value > 2) return "I found ${key}:${value}"
        }

        assert "default" == oneThroughFourMap.findResult("default") { key, value ->
            if (value > 10) return "I found ${key}:${value}"
        }
    }

    @Test
    void testListFindAll() {
        def list = [20, 5, 40, 2]
        def answer = list.findAll { item -> item < 10 }
        assert answer.size() == 2
        assert answer == [5, 2]
    }

    @Test
    void testMapFindAll() {
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        def answer = map.findAll { entry -> entry.value > 5 }
        assert answer.size() == 2
        def keys = answer.collect { entry -> entry.key }
        def values = answer.collect { entry -> entry.value }
        // maps are in hash order so lets sort the results
        keys.sort()
        values.sort()
        assert keys == [3, 4], "Expected [3, 4] but was $keys"
        assert values == [6, 8], "Expected [6, 8] but was $values"
    }

    @Test
    void testMapEach() {
        def count = 0
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        map.each { e -> count = count + e.value }
        assert count == 20
        map.each { e -> count = count + e.value + e.key }
        assert count == 50
    }

    @Test
    void testMapEachWith2Params() {
        def count = 0
        def map = [1: 2, 2: 4, 3: 6, 4: 8]
        map.each { key, value -> count = count + value }
        assert count == 20
        map.each { key, value -> count = count + value + key }
        assert count == 50
    }

    @Test
    void testListEach() {
        def count = 0
        def list = [1, 2, 3, 4]
        list.each({ item -> count = count + item })
        assert count == 10
        list.each { item -> count = count + item }
        assert count == 20
    }

    @Test
    void testListEvery() {
        assert [1, 2, 3, 4].every { i -> return i < 5 }
        assert [1, 2, 7, 4].every { i -> i < 5 } == false
        assert [a: 1, b: 2, c: 3].every { k, v -> k < 'd' && v < 4 }
        assert ![a: 1, b: 2, c: 3].every { k, v -> k < 'd' && v < 3 }
    }

    @Test
    void testListAny() {
        assert [1, 2, 3, 4].any { i -> return i < 5 }
        assert [1, 2, 3, 4].any { i -> i > 3 }
        assert [1, 2, 3, 4].any { i -> i > 5 } == false
        assert [a: 1, b: 2, c: 3].any { k, v -> k == 'c' }
        def isThereAFourValue = [a: 1, b: 2, c: 3].any { k, v -> v == 4 }
        assert !isThereAFourValue
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
    void testListInject() {
        def value = [1, 2, 3].inject('counting: ') { str, item -> str + item }
        assert value == "counting: 123"
        value = [1, 2, 3].inject(0) { c, item -> c + item }
        assert value == 6
        value = ([1, 2, 3, 4] as Object[]).inject(0) { c, item -> c + item }
        assert value == 10
    }

    @Test
    void testOneArgListInject() {
        // Check basic functionality
        def value = [1, 2, 3].inject { c, item -> c + item }
        assert value == 6

        // Check a use-case
        value = [['tim', 'dave', 'chris'],
                 ['stuart', 'harry', 'tim'],
                 ['bert', 'tim', 'ernie']]
        assert value.inject { a, b -> a.intersect(b) } == ['tim']

        // Check edges
        shouldFail(NoSuchElementException) {
            [].inject { a, b -> a + b } == null
        }
        assert [1].inject { a, b -> a + b } == 1
        assert [1, 2].inject { a, b -> a + b } == 3
    }

    @Test
    void testOneArgObjectInject() {
        def value = ([1, 2, 3, 4] as Object[]).inject { c, item -> c + item }
        assert value == 10

        shouldFail(NoSuchElementException) {
            ([] as Object[]).inject { c, item -> c + item }
        }

        value = ([1] as Object[]).inject { c, item -> c + item }
        assert value == 1

        def i = 1
        def iter = [hasNext: { -> i < 5 }, next: { -> i++ }] as Iterator
        assert iter.inject { a, b -> a * b } == 24

        shouldFail(NoSuchElementException) {
            iter = [hasNext: { -> false }, next: { -> null }] as Iterator
            iter.inject { a, b -> a * b }
        }

        i = 1
        iter = [hasNext: { -> i <= 1 }, next: { -> i++ }] as Iterator
        assert iter.inject { a, b -> a * b } == 1
    }

    @Test
    void testOldAndNewStylesYieldSameResults() {
        def items = [1000, 200, 30, 4]
        def twice = { int x -> x * 2 }
        def checkEqual = { int a, b -> assert a == b; a }
        def sum = DGM.&sum
        def addThreeWays = [
                items.sum(),
                items.inject(0, sum),
                items.inject(sum)
        ]
        assert addThreeWays == [1234] * 3
        addThreeWays.inject(checkEqual)

        def addTwiceFourWays = [
                items.inject(0) { int acc, next -> acc + twice(next) },
                items.collect(twice).sum(),
                items.collect(twice).inject(0, sum),
                items.collect(twice).inject(sum)
        ]
        assert addTwiceFourWays == [2468] * 4
        addTwiceFourWays.inject(checkEqual)
    }

    @Test
    void testObjectInject() {
        def value = [1: 1, 2: 2, 3: 3].inject('counting: ') { str, item -> str + item.value }
        assert value == "counting: 123"
        value = [1: 1, 2: 2, 3: 3].inject(0) { c, item -> c + item.value }
        assert value == 6
    }

    @Test
    void testIteratorInject() {
        def value = [1: 1, 2: 2, 3: 3].iterator().inject('counting: ') { str, item -> str + item.value }
        assert value == "counting: 123"
        value = [1: 1, 2: 2, 3: 3].iterator().inject(0) { c, item -> c + item.value }
        assert value == 6
    }

    @Test
    void testInspect() {
        def text = [1, 2, 'three'].inspect()
        assert text == "[1, 2, 'three']"
    }

    @Test
    void testTokenize() {
        def text = "hello-there-how-are-you"
        def answer = []
        for (i in text.tokenize('-')) {
            answer.add(i)
        }
        assert answer == ['hello', 'there', 'how', 'are', 'you']
    }

    @Test
    void testUpto() {
        def answer = []
        1.upto(5) { answer.add(it) }
        assert answer == [1, 2, 3, 4, 5]
    }
}
