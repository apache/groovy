/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

/** 
 * Tests the various Closure methods in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodTest extends GroovyTestCase {

    void testListCollect() {
        def list = [1, 2, 3, 4]
        def answer = list.collect{item -> return item * 2 }
        assert answer.size() == 4
        def expected = [2, 4, 6, 8]
        assert answer == expected
    }

    void testMapCollect() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.collect{e-> return e.key + e.value }
        // lest sort the results since maps are in hash code order
        answer = answer.sort()
        assert answer.size() == 4
        assert answer == [3, 6, 9, 12]
        assert answer.get(0) == 3
        assert answer.get(1) == 6
        assert answer.get(2) == 9
        assert answer.get(3) == 12
    }

    void testListFind() {
        def list = ["a", "b", "c"]
        def answer = list.find{item-> item == "b" }
        assert answer == "b"
        answer = list.find{item-> item == "z" }
        assert answer == null
    }

    void testMapFind() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.find{entry -> entry.value == 6 }
        assert answer != null
        assert answer.key == 3
        assert answer.value == 6
        answer = map.find{entry -> entry.value == 0 }
        assert answer == null
        answer = map.find{ k, v -> v > 5 }
        assert answer instanceof Map.Entry
        assert answer.key == 3
        assert answer.value == 6

        answer = map.find{ k, v -> k == 2 }
        assert answer instanceof Map.Entry
        assert answer.key == 2
        assert answer.value == 4
    }

    void testListFindAll() {
        def list = [20, 5, 40, 2]
        def answer = list.findAll{item -> item < 10 }
        assert answer.size() == 2
        assert answer == [5, 2]
    }

    void testMapFindAll() {
        def map = [1:2, 2:4, 3:6, 4:8]
        def answer = map.findAll{ entry -> entry.value > 5 }
        assert answer.size() == 2
        def keys = answer.collect { entry -> entry.key }
        def values = answer.collect { entry -> entry.value }
        // maps are in hash order so lets sort the results
        keys.sort()
        values.sort()
        assert keys == [3, 4], "Expected [3, 4] but was $keys"
        assert values == [6, 8], "Expected [6, 8] but was $values"
    }

    void testMapEach() {
        def count = 0
        def map = [1:2, 2:4, 3:6, 4:8]
        map.each{e-> count = count + e.value }
        assert count == 20
        map.each{e-> count = count + e.value + e.key }
        assert count == 50
    }

    void testMapEachWith2Params() {
        def count = 0
        def map = [1:2, 2:4, 3:6, 4:8]
        map.each {key, value -> count = count + value }
        assert count == 20
        map.each {key, value -> count = count + value + key }
        assert count == 50
    }

    void testListEach() {
        def count = 0
        def list = [1, 2, 3, 4]
        list.each({item-> count = count + item })
        assert count == 10
        list.each{item-> count = count + item }
        assert count == 20
    }

    void testListEvery() {
        assert [1, 2, 3, 4].every {i-> return i < 5 }
        assert [1, 2, 7, 4].every {i-> i < 5 } == false
        assert [a:1, b:2, c:3].every {k,v -> k < 'd' && v < 4 }
        assert ![a:1, b:2, c:3].every {k,v -> k < 'd' && v < 3 }
    }

    void testListAny() {
        assert [1, 2, 3, 4].any {i-> return i < 5 }
        assert [1, 2, 3, 4].any {i-> i > 3 }
        assert [1, 2, 3, 4].any {i-> i > 5 } == false
        assert [a:1, b:2, c:3].any { k,v -> k == 'c' }
        def isThereAFourValue = [a:1, b:2, c:3].any{ k,v -> v == 4 }
        assert !isThereAFourValue
    }

    void testJoin() {
        def value = [1, 2, 3].join('-')
        assert value == "1-2-3"
    }

    void testListReverse() {
        def value = [1, 2, 3, 4].reverse()
        assert value == [4, 3, 2, 1]
    }

    void testListInject() {
        def value = [1, 2, 3].inject('counting: ') { str, item -> str + item }
        assert value == "counting: 123"
        value = [1, 2, 3].inject(0) { c, item -> c + item }
        assert value == 6
        value = ([1, 2, 3, 4] as Object[]).inject(0) { c, item -> c + item }
        assert value == 10
    }

    void testObjectInject() {
        def value = [1:1, 2:2, 3:3].inject('counting: ') { str, item -> str + item.value }
        assert value == "counting: 123"
        value = [1:1, 2:2, 3:3].inject(0) { c, item -> c + item.value }
        assert value == 6
    }

    void testIteratorInject() {
        def value = [1:1, 2:2, 3:3].iterator().inject('counting: ') { str, item -> str + item.value }
        assert value == "counting: 123"
        value = [1:1, 2:2, 3:3].iterator().inject(0) { c, item -> c + item.value }
        assert value == 6
    }

    void testDump() {
        def text = dump()
        println("Dumping object ${text}")
        assert text != null && text.startsWith("<")
    }

    void testInspect() {
        def text = [1, 2, 'three'].inspect()
        println("Inspecting ${text}")
        assert text == '[1, 2, "three"]'
    }

    void testTokenize() {
        def text = "hello-there-how-are-you"
        def answer = []
        for (i in text.tokenize('-')) {
            answer.add(i)
        }
        assert answer == ['hello', 'there', 'how', 'are', 'you']
    }
    
    void testUpto() {
        def answer = []
        1.upto(5) { answer.add(it) }
        assert answer == [1, 2, 3, 4, 5]
    }
}
