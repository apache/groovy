/*
 * Copyright 2003-2012 the original author or authors.
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

import java.awt.Dimension
import java.util.concurrent.LinkedBlockingQueue
import org.codehaus.groovy.util.StringUtil

/** 
 * Tests the various new Groovy methods
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Dierk Koenig
 * @author Paul King
 * @author Joachim Baumann
 * @author Mike Dillon
 * @author Tim Yates
 * @author Dinko Srkoc
 */
class GroovyMethodsTest extends GroovyTestCase {
    void testCollect() {
        assert [2, 4, 6].collect {it * 2} == [4, 8, 12]
        def answer = [2, 4, 6].collect(new Vector()) {it * 2}
        assert answer[0] == 4
        assert answer[1] == 8
        assert answer[2] == 12
        assert [1: 'a', 2: 'b', 3: 'c'].collect {k, v -> k + v} == ['1a', '2b', '3c']
        assert [1: 'a', 2: 'b', 3: 'c'].collect {it.getKey() + "*" + it.getValue()} == ['1*a', '2*b', '3*c']
    }

    void testGStringIsCase() {
        def one = 'one'
        def two = 'two'
        assert 'onetwo'.isCase('onetwo')
        assert 'onetwo'.isCase("one$two")
        assert 'onetwo'.isCase("$one$two")
        assert "one$two".isCase('onetwo')
        assert "$one$two".isCase('onetwo')
        assert "one${two}".isCase("${one}two")
    }

    void testSpreadDot() {
        def animals = ['cat', 'dog']
        assert animals*.size() == [3, 3]
        animals = [1:'cat', 2:'dog']
        assert animals.values()*.size() == [3, 3]
        animals = ['cat', 'dog'] as Vector
        assert animals.elements()*.size() == [3, 3]
    }

    void testCollectAll() {
        def animalLists= [["ant", "mouse", "elephant"], ["deer", "monkey"]]
        assert animalLists*.size() == [3, 2]
        assert animalLists.collect{ it.size() } == [3, 2]
        assert animalLists.collectAll{ it.size() } == [[3, 5, 8], [4, 6]]
    }

    void testAsCoercion() {
        if (HeadlessTestSupport.headless) return

        def d0 = new Dimension(100, 200)
        assert d0 == new Dimension(width: 100, height: 200)
        assert d0 == [100, 200] as Dimension
        assert d0 == [width: 100, height: 200] as Dimension
    }

    void testAsCoercionPropogatesCauseMessage() {
        try {
            ['one hundred', 200] as Dimension
        } catch (Exception e) {
            assert e.message.contains("java.lang.String")
        }
    }

    void testAsCoercionInterface() {
        def letters = ['a', 'b', 'c']
        def ol = new ObserverLike()
        def o = new Observable()
        o.addObserver(ol as Observer) // addObserver takes Observer as param
        letters.each{ o.changed = true; o.notifyObservers(it) }
        assert ol.observed == letters
    }

    private class ObserverLike {
      def observed = []
      void update(Observable o, arg) {observed << arg }
    }

    void testCombinations() {
        def lists = [['a', 'b'], [1, 2, 3]]
        def sets = [['a', 'b'] as Set, [1, 2, 3] as Set]
        def expected = [['a', 1], ['a', 2], ['a', 3],
                        ['b', 1], ['b', 2], ['b', 3]] as Set

        assert lists.combinations() as Set == expected
        assert sets.combinations() as Set == expected
        lists = [['a', 'b'], 3]
        assert lists.combinations() as Set == [['a', 3], ['b', 3]] as Set
    }

    void testTranspose() {
        def list1 = [['a', 'b'], [1, 2, 3]]
        def list2 = [['a', 'b', 'c'], [1, 2]]
        def expected = [['a', 1], ['b', 2]]
        assert list1.transpose() == expected
        assert list2.transpose() == expected
    }

    void testSum() {
        assert [].sum() == null
        assert [null].sum() == null
        assert [1].sum() == 1
        assert [1, 2, 3].sum() == 6
        def nums = [1, 2, 3, 4] as Integer[]
        assert nums.sum() == 10
        assert nums.sum(5) == 15
        assert [1, 2, 3].sum("") == "123"
        assert [[1, 2], [3, 4], [5, 6]].sum() == [1, 2, 3, 4, 5, 6]
        assert [[1, 2], [3, 4], [5, 6]].sum("") == "[1, 2][3, 4][5, 6]"

        assert [].sum {it.length()} == null
        assert [null].sum {it.toString()} == 'null'
        assert ["abc"].sum {it.length()} == 3
        assert ["a", "bc", "def"].sum {it.length()} == 6
        assert ["a", "bc", "def"].sum("") {it.length()} == "123"
        assert [[1, 2], [3, 4], [5, 6]].sum {it.size()} == 6
        assert [[1, 2], [3, 4], [5, 6]].sum {list -> list.collect {it * 2}} == [2, 4, 6, 8, 10, 12]
        def result = []
        [[1, 2], [3, 4], [5, 6]].each {list -> result << list.collect {it * 2}}
        assert result.sum() == [2, 4, 6, 8, 10, 12]
        assert [[1, 2], [3, 4], [5, 6]].sum {list -> list.collect {it * 2}} == [2, 4, 6, 8, 10, 12]
    }

    void testSumForIteratorWithList() {
        def result = [1, 2, 3].iterator().sum([])
        assert result == [1, 2, 3]
    }

    void testEachLineString() {
        def twolines = 'one\ntwo'
        def result = ''
        twolines.eachLine{ line, count -> result += "$count: $line\n" }
        assert result == '0: one\n1: two\n'
    }

    void testEachLineStringWithStartingLine() {
        def twolines = 'one\ntwo'
        def result = ''
        twolines.eachLine(1){ line, count -> result += "$count: $line\n" }
        assert result == '1: one\n2: two\n'
    }

    void testSumForIteratorWithInt() {
        def result = [1, 2, 3].iterator().sum(0)
        assert result == 6
    }

    void testReverseForIterator() {
        def listIterator = [1, 2, 3].iterator()
        def result = []
        def revIterator = listIterator.reverse()
        assert revIterator instanceof Iterator
        revIterator.each{
            result << it
        }
        assert result == [3, 2, 1]
    }

    void testReverseForObjectArrays() {
        Object[] numbers = [1, 2, 3]
        def result = numbers.reverse()
        assert result == [3, 2, 1]
    }

    void testJoin() {
        assert [2, 4, 6].join("-") == "2-4-6"
        assert ["edam", "cheddar", "brie"].join(", ") == 'edam, cheddar, brie'
        assert ["abc", 5, 2.34].join(", ") == "abc, 5, 2.34"
    }

    void testTimes() {
        def count = 0
        5.times {i -> count = count + i}
        assert count == 10
        count = 0
        def temp = 5
        temp.times {i -> count = count + i}
        assert count == 10
    }

    void testArraySubscript() {
        def list = [1, 2, 3, 4]
        def array = list.toArray()
        def value = array[2]
        assert value == 3
        array[0] = 9
        assert array[0] == 9
        assert array[0..<0] == []
    }

    void testToCharacterMethod() {
        def s = 'c'
        def x = s.toCharacter()
        assert x instanceof Character
    }

    void testGetCharsMethod() {
        def s = 'abc'
        def x = s.chars
        assert x instanceof char[]
        assert x.size() == 3
    }

    void testCharacterToMethods() {
        char c1 = 'a'
        char c2 = 'Z'
        assert c1.toUpperCase() == 'A'
        assert c2.toLowerCase() == 'z'
    }

    void testCharacterIsMethods() {
        def f = false
        def t = true
        checkProperties('a', f, t, f, f, t, t)
        checkProperties('Z', t, f, f, f, t, t)
        checkProperties('5', f, f, f, t, f, t)
        checkProperties(' ', f, f, t, f, f, f)
        checkProperties('&', f, f, f, f, f, f)
    }

    private def checkProperties(String s, boolean isUpperCase, boolean isLowerCase, boolean isWhitespace,
                            boolean isDigit, boolean isLetter, boolean isLetterOrDigit) {
        char c = s as char
        assert c.isUpperCase() == isUpperCase
        assert c.isLowerCase() == isLowerCase
        assert c.isWhitespace() == isWhitespace
        assert c.isLetter() == isLetter
        assert c.isDigit() == isDigit
        assert c.isLetterOrDigit() == isLetterOrDigit
    }

    void testPutAtRange() {
        def list
        list = ['a', 'b', 'c']; list[4] = 'x'; assert list == ["a", "b", "c", null, "x"]
        list = ['a', 'b', 'c']; list[1..2] = ['x', 'y']; assert list == ["a", "x", "y"]
        list = ['a', 'b', 'c']; list[1..2] = 'x'; assert list == ["a", "x"]
        list = ['a', 'b', 'c']; list[4..5] = ['x', 'y']; assert list == ["a", "b", "c", null, "x", "y"]
        list = ['a', 'b', 'c']; list[4..5] = 'x'; assert list == ["a", "b", "c", null, "x"]
    }

    void testGetAtRange() {
        def list = ['a', 'b', 'c']
        assert list[1..2] == ['b', 'c']
        assert list[0..<0] == []
    }

    void testCharSequenceGetAt() {
        def x = "matrix"
        assert x[0, 5..0] == 'mxirtam'
        assert x[3..0, 0..3] == 'rtammatr'
        assert x[2..-4, 3..-4, 3..-3] == 'trtr'
        assert x[-1..-3, -3..-1] == 'xirrix'
        assert x[0..<0] == ''
    }

    void testListGrep() {
        def list = ["James", "Bob", "Guillaume", "Sam"]
        def answer = list.grep(~".*a.*")
        assert answer == ["James", "Guillaume", "Sam"]
        answer = list.grep(~"B.b")
        assert answer == ["Bob"]
    }

    void testCollectionToList() {
        def c = [1, 2, 3, 4, 5] // but it's a list
        def l = c.toList()
        assert l.containsAll(c)
        assert c.size() == l.size()
    }

    void testIteratorToList() {
        def c = [1, 2, 3, 4, 5]
        def l = c.iterator().toList()
        assert l.containsAll(c)
        assert c.size() == l.size()
    }

    void testIteratorSize() {
        def c = [1, 2, 3, 4, 5]
        def it = c.iterator()
        assert c.size() == it.size()
    }

    void testEnumerationToList() {
        def c = [1, 2, 3, 4, 5]
        def v = new Vector()
        c.each {
            v.add(it)
        }
        def l = v.elements().toList()
        assert l.containsAll(c)
        assert c.size() == l.size()
    }

    void testStringToList() {
        String s = 'hello 10'
        def gs = "hello ${5 + 5}"
        def expected = ["h", "e", "l", "l", "o", " ", "1", "0"]
        assert s.toList() == expected
        assert s as String[] == expected
        assert gs as String[] == expected
    }

    void testCollectionAsList() {
        Integer[] nums = [1, 2, 3, 4, 5]
        def numList = nums as List
        nums.each {assert numList.contains(it)}
        assert nums.size() == numList.size()
    }

    void testCollectionAsLinkedList() {
        Integer[] nums = [1, 2, 3, 4, 5]
        def numList = nums as LinkedList
        nums.each {assert numList.contains(it)}
        assert nums.size() == numList.size()
        assert numList.class == LinkedList.class
    }

    void testArrayListAsLinkedList() {
        ArrayList nums = [1, 2, 3, 4, 5]
        def result = nums as LinkedList
        assert result == [1, 2, 3, 4, 5]
        assert result.class == LinkedList
    }

    void testFileSize() {
        assert new File('gradle.properties').size()
    }

    void testMatcherSize() {
        assertEquals 3, ('aaa' =~ /./).count
        assertEquals 3, ('aaa' =~ /./).size()
        assertEquals 1, ('a' =~ /./).size()
        assertEquals 0, ('a' =~ /x/).size()
    }

    void testJoinString() {
        String[] arr = ["a", "b", "c", "d"]
        def joined = arr.join(", ")
        assert joined == "a, b, c, d"
    }

    void testReverseEachForList() {
        def l = ["cheese", "loves", "Guillaume"]
        def expected = ["Guillaume", "loves", "cheese"]
        def answer = []
        l.reverseEach {answer << it}
        assert answer == expected
    }

    void testReverseEachForArray() {
        String[] items = ["cheese", "loves", "Guillaume"]
        String[] expected = ["Guillaume", "loves", "cheese"]
        def answer = []
        items.reverseEach {answer << it}
        assert answer == expected
        assert items.reverse() == expected
    }

    void testGrep() {
        def list = ["Guillaume", "loves", "cheese"]
        def answer = list.grep(~".*ee.*")
        assert answer == ["cheese"]
        list = [123, "abc", 4.56]
        answer = list.grep(String)
        assert answer == ["abc"]
        list = [4, 2, 7, 3, 6, 2]
        answer = list.grep(2..3)
        assert answer == [2, 3, 2]
    }

    void testMapGetWithDefault() {
        def map = [:]
        assert map.foo == null
        map.get("foo", []).add(123)
        assert map.foo == [123]
        map.get("bar", [:]).get("xyz", [:]).cheese = 123
        assert map.bar.xyz.cheese == 123
        assert map.size() == 2
    }

    void testDisplaySystemProperties() {
        println "System properties are..."
        def properties = System.properties
        def keys = properties.keySet().sort()
        for (k in keys) {
            println "${k} = ${properties[k]}"
        }
    }

    void testInForLists() {
        def list = ['a', 'b', 'c']
        assert 'b' in list
        assert !('d' in list)
    }

    void testFirstLastHeadTailForLists() {
        def list = ['a', 'b', 'c']
        assert 'a' == list.first()
        assert 'c' == list.last()
        assert 'a' == list.head()
        assert ['b', 'c'] == list.tail()
        assert list.size() == 3
    }

    void testPushPopForLists() {
        def list = ['a', 'b', 'c']
        assert list.push('d')
        assert list.size() == 4
        assert list.pop() == 'd'
        assert list.size() == 3
    }

    void testInForArrays() {
        String[] array = ['a', 'b', 'c']
        assert 'b' in array
        assert !('d' in array)
    }

    void testMax() {
        assert [-5, -3, -1, 0, 2, 4].max {it * it} == -5
    }

    void testMin() {
        assert [-5, -3, -1, 0, 2, 4].min {it * it} == 0
    }

    void testSort() {
        assert [-5, -3, -1, 0, 2, 4].sort {it * it} == [0, -1, 2, -3, 4, -5]
    }

    void testMaxForIterator() {
        assert [-5, -3, -1, 0, 2, 4].collect{ it * it }.iterator().max() == 25
    }

    void testMinForIterator() {
        assert [-5, -3, -1, 0, 2, 4].collect{ it * it }.iterator().min() == 0
    }

    void testMinForObjectArray() {
        Integer[] numbers = [-5, -3, -1, 0, 2, 4]
        def result = numbers.min()
        assert result == -5
        result = numbers.min{it * it}
        assert result == 0
    }

    void testMaxForObjectArray() {
        Integer[] numbers = [-5, -3, -1, 0, 2, 4]
        def result = numbers.max()
        assert result == 4
        result = numbers.max{it * it}
        assert result == -5
    }

    void testCountForIterator() {
        assert [1, 2, 3, 2, 1].iterator().count(2) == 2
    }

    void testCountForPrimitiveArray() {
        int[] nums = [1, 2, 3, 2, 1]
        assert nums.count(2) == 2
    }

    void testCountForCollection() {
        def nums = [1, 2, 3, 2, 1]
        assert nums.count(2) == 2
    }

    void testCountForString() {
        def string = 'google'
        assert string.count('g') == 2
    }

    void testCountForStringEdgeCase_GROOVY5858() {
        def blank6 = ' ' * 6
        8.times { assert blank6.count(' ' * it) == 7 - it }
    }

    void testJoinForIterator() {
        assert ['a', 'b', 'c', 'a'].iterator().join('-') == 'a-b-c-a'
    }

    void testSortForIterator() {
        def result = []
        def iterator = [-5, -3, -1, 0, 2, 4].iterator().sort {it * it}
        assert iterator instanceof Iterator
        iterator.each {
            result << it
        }
        assert result == [0, -1, 2, -3, 4, -5]
    }

    void testReplaceAllClosure() {
        assert "1 a 2 b 3 c 4".replaceAll("\\p{Digit}") {it * 2} == "11 a 22 b 33 c 44"
    }

    void testReplaceAllClosurePattern() {
        assert "1 a 2 b 3 c 4".replaceAll(~"\\p{Digit}") {it * 2} == "11 a 22 b 33 c 44"
    }

    void testObjectSleep() {
        long start = System.currentTimeMillis()
        sleep 1000
        long slept = System.currentTimeMillis() - start
        long epsilon = 120
        assert (slept > 1000 - epsilon) && (slept < 1000 + epsilon):   \
               "should have slept for 1s (+/- " + epsilon + "ms) but was ${slept}ms"
    }

    void testObjectSleepInterrupted() {
        def interruptor = new groovy.TestInterruptor(Thread.currentThread())
        new Thread(interruptor).start()
        long start = System.currentTimeMillis()
        sleep 1000
        long slept = System.currentTimeMillis() - start
        long epsilon = 150
        assert (slept > 1000 - epsilon) && (slept < 1000 + epsilon):   \
               "should have slept for 1s (+/- " + epsilon + "ms) but was ${slept}ms"
    }

    void testObjectSleepWithOnInterruptHandler() {
        def log = ''
        def interruptor = new groovy.TestInterruptor(Thread.currentThread())
        new Thread(interruptor).start()
        long start = System.currentTimeMillis()
        sleep(2000) {log += it.toString()}
        long slept = System.currentTimeMillis() - start
        assert slept < 2000, "should have been interrupted but slept ${slept}ms > 2s"
        assertEquals 'java.lang.InterruptedException: sleep interrupted', log.toString()
    }

    void testObjectSleepWithOnInterruptHandlerContinueSleeping() {
        def log = ''
        def interruptor = new groovy.TestInterruptor(Thread.currentThread())
        new Thread(interruptor).start()
        long start = System.currentTimeMillis()
        sleep(2000) {
            log += it.toString()
            false // continue sleeping
        }
        long slept = System.currentTimeMillis() - start
        short allowedError = 4 // ms
        assert slept + allowedError >= 2000, "should have slept for at least 2s but only slept for ${slept}ms"
        assertEquals 'java.lang.InterruptedException: sleep interrupted', log.toString()
    }

    void testObjectIdentity() {
        def a = new Object()
        def b = a
        assert a.is(b)
        assert !a.is(null)
        assert !1.is(2)
        // naive impl would fall for this trap
        assert !new WackyHashCode().is(new WackyHashCode())
    }

    void testGroupByListIdentity() {
        def items = [1, 2, 'foo']
        assert items.groupBy() == [1:[1], 2:[2], foo:['foo']]
        assert items.groupBy([]) == [1:[1], 2:[2], foo:['foo']]
        assert items.groupBy(Closure.IDENTITY) == [1:[1], 2:[2], foo:['foo']]
        assert items.groupBy({it}) == [1:[1], 2:[2], foo:['foo']]
        assert items.groupBy({it},{it}) == [1:[1:[1]], 2:[2:[2]], foo:[foo:['foo']]]
    }

    void testGroupByList() {
        def expected = [Integer: [1, 2], String: ["a", "b"], BigDecimal: [3.5, 4.6]]
        def list = [1, "a", 2, "b", 3.5, 4.6]
        def result = list.groupBy {it.class}
        assert [1, 2] == result[Integer]
        assert ["a", "b"] == result[String]
        assert [3.5, 4.6] == result[BigDecimal]
        assert 3 == result.size()
    }

    void testGroupByListMultipleCriteria() {
        def list1 = [1, 'a', 2, 'b', 3.5, 4.6]
        def result1 = list1.groupBy({ it.class }, { it.class == Integer ? 'integer' : 'non-integer' })
        assert [integer: [1, 2]] == result1[Integer]
        assert ['non-integer': ['a', 'b']] == result1[String]
        assert ['non-integer': [3.5, 4.6]] == result1[BigDecimal]

        def list2 = [
                [aa: 11, bb: 22, cc: 33],
                [aa: 11, bb: 22, cc: 44],
                [aa: 11, bb: 33, cc: 55],
                [aa: 22, bb: 22, cc: 66],
                [aa: 22, bb: 22, cc: 77],
                [aa: 22, bb: 33, cc: 77]
        ]
        def result2 = list2.groupBy([{ it.aa }, { it.bb }, { it.cc }])
        assert [[aa: 11, bb: 22, cc: 33]]== result2[11][22][33]
        assert [77: [[aa: 22, bb: 33, cc: 77]]] == result2[22][33]
    }

    void testGroupByMapIdentity() {
        def map = [a:1, b:2]
        def entries = map.entrySet()
        def result = map.groupBy()
        def groupedKeys = result.keySet()
        assert entries == groupedKeys
        def entry_a = entries.find{ it.key == 'a' }
        assert result[entry_a] == [a:1]
    }

    void testMapGroupEntriesBy() {
        def expectedKeys = [Integer: [1, 3], String: [2, 4], BigDecimal: [5, 6]]
        def expectedVals = [Integer: [1, 2], String: ["a", "b"], BigDecimal: [3.5, 4.6]]
        def map = [1: 1, 2: "a", 3: 2, 4: "b", 5: 3.5, 6: 4.6]
        def result = map.groupEntriesBy {Map.Entry entry -> entry.value.class}
        assert expectedKeys.Integer == result[Integer].collect {it.key}
        assert expectedVals.Integer == result[Integer].collect {it.value}
        assert expectedKeys.String == result[String].collect {it.key}
        assert expectedVals.String == result[String].collect {it.value}
        assert expectedKeys.BigDecimal == result[BigDecimal].collect {it.key}
        assert expectedVals.BigDecimal == result[BigDecimal].collect {it.value}
        assert 3 == result.size()
    }

    void testMapGroupBy() {
        def map = [1: 1, 2: "a", 3: 2, 4: "b", 5: 3.5, 6: 4.6]
        def result = map.groupBy {entry -> entry.value.class}
        assert 3 == result.size()
        assert result[BigDecimal] == [5:3.5, 6:4.6]
        assert result[String] == [2:'a', 4:'b']
        assert result[Integer] == [1:1, 3:2]
    }

    void testMapGroupByMultipleCriteria() {
        def map1 = [1: 1, 2: "a", 3: 2, 4: "b", 5: 3.5, 6: 4.6]
        def result1 = map1.groupBy({entry -> entry.value.class}, {entry -> entry.key + 1})

        assert 3 == result1.size()
        assert result1[BigDecimal] == [6: [5: 3.5], 7: [6: 4.6]]
        assert result1[String] == [3: [2: "a"], 5: [4: "b"]]
        assert result1[Integer][2] == [1: 1]
        assert result1[Integer][4] == [3: 2]

        def map2 = [aa: 11, bb: 22, cc: 33]
        def result2 = map2.groupBy({ it.key }, { it.key.next() }, { it.key.next().next() })

        assert result2.size() == 3
        assert result2['aa']['ab']['ac'] == [aa: 11]
        assert result2['bb']['bc']['bd'] == [bb: 22]
        assert result2['cc']['cd']['ce'] == [cc: 33]
    }

    def leftCol = ["2"]
    def rightCol = ["1", "2", "3"]

    void testList() {
        def lst = [] as LinkedList
        doIt(lst)
    }

    void testSetWithExplicitCoercion() {
        def set = [] as HashSet
        doIt(set)
    }

    void testSetWithImplicitCoercion() {
        Set set = []
        doIt(set)
    }

    void testVector() {
        def vctr = [] as Vector
        doIt(vctr)
    }

    void doIt(col) {
        col.clear();
        col.addAll(leftCol);
        // not really concerned about  correctness, rather that the method can be called, however..
        assert col.intersect(rightCol) == ["2"]
    }

    void testFileWithReader() {
        def f = new File('gradle.properties')
        def expected = f.text
        assert expected == f.withReader { r -> r.text }
    }

    void testFileWithInputStream() {
        def f = new File('gradle.properties')
        def buf = new byte[f.size()]
        assert buf.size() == f.withInputStream { i -> i.read(buf) }
    }

    void testUrlReader() {
        def u = new File('gradle.properties').toURL()
        def expected = u.text
        assert expected == u.withReader { r -> r.text }
    }

    void testUrlWithInputStream() {
        def f = new File('gradle.properties')
        def u = f.toURL()
        def buf = new byte[f.size()]
        assert buf.size() == u.withInputStream { i -> i.read(buf) } 
    }

    void testMinus() {
        // collections and lists remove all - deemed most common behavior for these
        assert [1, 2, 1, 3] - 1 == [2, 3]
        // strings remove first - deemed most common behavior for strings
        assert 'abcda.ce' - /a.c/ == 'abcde'
        assert 'abcda.ce' - ~/a.c/ == 'da.ce'
        // should handle nulls too
        assert [null] - [1] == [null]
    }

    void testListSplit() {
        def nums = 1..6
        def (evens, odds) = nums.split{ it % 2 == 0 }
        assert evens == [2, 4, 6]
        assert odds == [1, 3, 5]
        def things = ['3', 'cat', '7', 'dog', '11']
        def (numbers, others) = things.split{ it.isNumber() }
        assert numbers == ['3', '7', '11']
        assert others == ['cat', 'dog']
    }

    void testListDerivativesAreRetainedWithCommonOperators() {
        def x = new WackyList([1, 2, 3])
        assert x.size() == 3
        x += 4
        assert x.sum() == 10
        assert x.size() == 4
        assert x.class == WackyList
        def y = x.findAll{ it % 2 == 1 } as WackyList
        assert y.size() == 2
        assert y.class == WackyList
    }

    void testEachOnEnumClassIteratesThroughTheValuesOfTheEnum() {
        def expected = Suit.values().toList()
        def answer = []
        Suit.each { answer << it }
        assert answer == expected
    }

    void testForLoopWithEnumClassIteratesThroughTheValuesOfTheEnum() {
        def expected = Suit.values().toList()
        def answer = []
        for (s in Suit) {
            answer << s
        }
        assert answer == expected
    }

    void testAsEnumType() {
        assert Suit.HEARTS == ("HEARTS" as Suit)

        shouldFail(IllegalArgumentException) {
            "FOO" as Suit
        }
    }

    void testJavaEnumType() {
        def x = Language.English
        x++
        assert x == Language.French
        x = Language.English
        x--
        assert x == Language.Spanish
        assert Language.French in Language.English..Language.Spanish
    }

    void testStringBuilderPlusPutAtSizeLeftShift() {
        def sb = new StringBuilder('foo')
        assert sb + 'bar' == 'foobar'
        sb << 'baz'
        assert sb.size() == 6
        def result = sb.toString()
        assert result == 'foobaz'
        sb[3..4] = 'abc'
        result = sb.toString()
        assert result == 'fooabcz'
        sb[6..<6] = 'xy'
        result = sb.toString()
        assert result == 'fooabcxyz'
    }

    void testDefiningQueue() {
        def result = [1, 2, 3, 4, 5] as Queue
        assert result instanceof Queue
        assert result.size() == 5
        assert result.sum() == 15
    }

    void testLinkedBlockingQueue() {
        def q = [1, 2, 3, 4, 5, 6, 7, 8, 9] as LinkedBlockingQueue
        assert q.size() == 9
        assert q.class == LinkedBlockingQueue
        q = (q + 10) as LinkedBlockingQueue
        assert q.size() == 10
        assert q.class == LinkedBlockingQueue
        def r = q.findAll{ it % 2 == 0 } as LinkedBlockingQueue
        assert r.size() == 5
        assert r.class == LinkedBlockingQueue
        def s = ((r as LinkedList) - [4, 6]) as LinkedBlockingQueue
        assert s.size() == 3
        assert s.class == LinkedBlockingQueue
        [2, 8, 10].each{ assert it in s }
    }

    void testSubsequences() {
        def items = [1, 2, 3]
        assert items.subsequences() == [[1, 2, 3], [1, 3], [2, 3], [1, 2], [1], [2], [3]] as Set
        assert "frog".toList().subsequences() == [
                ['f', 'r', 'o', 'g'],
                ['f', 'r', 'o'],
                ['f', 'r', 'g'],
                ['f', 'o', 'g'],
                ['r', 'o', 'g'],
                ['f', 'g'],
                ['r', 'g'],
                ['o', 'g'],
                ['f', 'o'],
                ['r', 'o'],
                ['f', 'r'],
                ['f'],
                ['r'],
                ['o'],
                ['g']
        ] as Set
    }

    void testPrettyPrintingRecursiveStructures() {
        def m = [a:1]
        m.self = m
        assert m.toMapString() == '[a:1, self:(this Map)]'

        def l = ['first']
        l << l
        assert l.toListString() == '[first, (this Collection)]'

        def animals = ['ant', 'bee', 'cat']
        animals << [pets:animals]
        assert animals.toListString(5) == '[ant, ...]'
        assert animals.toListString(10) == '[ant, bee, ...]'
        assert animals.toListString(15) == '[ant, bee, cat, ...]'
        assert animals.toListString(20) == '[ant, bee, cat, [pets:[...]]]'
        assert animals.toListString(40) == '[ant, bee, cat, [pets:[ant, bee, cat, [pets:[...]]]]]'
    }

    void testRemoveAll() {
        def items = [1, 2, 3, 4]
        assert items.removeAll{ it % 2 == 0 }
        assert items == [1, 3]
    }

    void testRetainAll() {
        def items = [1, 2, 3, 4]
        assert items.retainAll{ it % 2 == 0 }
        assert items == [2, 4]
    }

    void testPermutations() {
        def items = [1, 2, 3]
        assert items.permutations() == [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]] as Set
        items = "frog".toList()
        def ans = [] as Set
        items.eachPermutation {
            ans << it
        }
        assert ans == [
                ['f', 'r', 'o', 'g'],
                ['f', 'r', 'g', 'o'],
                ['f', 'o', 'r', 'g'],
                ['f', 'o', 'g', 'r'],
                ['f', 'g', 'r', 'o'],
                ['f', 'g', 'o', 'r'],
                ['r', 'f', 'o', 'g'],
                ['r', 'f', 'g', 'o'],
                ['r', 'o', 'f', 'g'],
                ['r', 'o', 'g', 'f'],
                ['r', 'g', 'f', 'o'],
                ['r', 'g', 'o', 'f'],
                ['o', 'r', 'f', 'g'],
                ['o', 'r', 'g', 'f'],
                ['o', 'f', 'r', 'g'],
                ['o', 'f', 'g', 'r'],
                ['o', 'g', 'r', 'f'],
                ['o', 'g', 'f', 'r'],
                ['g', 'r', 'o', 'f'],
                ['g', 'r', 'f', 'o'],
                ['g', 'o', 'r', 'f'],
                ['g', 'o', 'f', 'r'],
                ['g', 'f', 'r', 'o'],
                ['g', 'f', 'o', 'r'],
        ] as Set
    }

    void testStringTranslate() {
        assert StringUtil.tr("abcdefghijklmn", "abcdefghijklmn", "ABCDEFGHIJKLMN") == "ABCDEFGHIJKLMN"
        assert StringUtil.tr("abcdefghijklmn", "abc", "ABC") == "ABCdefghijklmn"
        assert StringUtil.tr("abcdefghijklmn", "ace", "ACE") == "AbCdEfghijklmn"
        assert StringUtil.tr("abcdefghijklmn", "afghn", "AFGHN") == "AbcdeFGHijklmN"
        assert StringUtil.tr("abcdefghijklmn", "xyz", "XYZ") == "abcdefghijklmn"
        assert StringUtil.tr("abcdefghijklmn", "", "") == "abcdefghijklmn"
    }

    void testTr_Expand() {
        assert StringUtil.tr("abcdefghijklmn", "a-n", "ABCDEFGHIJKLMN") == "ABCDEFGHIJKLMN"
        assert StringUtil.tr("abcdefghijklmn", "abcdefghijklmn", "A-N") == "ABCDEFGHIJKLMN"
        assert StringUtil.tr("abcdefghijklmn", "a-n", "A-N") == "ABCDEFGHIJKLMN"
        assert StringUtil.tr("abcdefghijklmn", "a-c", "A-C") == "ABCdefghijklmn"
        assert StringUtil.tr("abcdefghijklmn", "af-hn", "AF-HN") == "AbcdeFGHijklmN"
        assert StringUtil.tr("abcdefghijklmn", "x-z", "X-Z") == "abcdefghijklmn"
        assert StringUtil.tr("abcdefghijklmn", "a-cfj-l", "A-CFJ-L") == "ABCdeFghiJKLmn"
        assert StringUtil.tr("-abc-", "-", "*") == "*abc*"
        assert StringUtil.tr("-abcdef--ghijklmn-", "-cfj-l", "*CFJ-L") == "*abCdeF**ghiJKLmn*"
        assert StringUtil.tr("abcdefghijklmn", "a-n", "A-C") == "ABCCCCCCCCCCCC"
        assert StringUtil.tr("abcdefghijklmn", "a-c", "A-N") == "ABCdefghijklmn"
        assert 'hello'.tr('aeiou', 'AEIOU') == 'hEllO'
        assert 'Hello World!'.tr('a-z', 'A') == 'HAAAA WAAAA!'
        assert 'Hello World!'.tr('a-z', 'A-Z') == 'HELLO WORLD!'
        assert 'Hello World!'.tr('z-a', 'Z-A') == 'HELLO WORLD!'
        assert 'Hello World!'.tr('lloo', '1234') == 'He224 W4r2d!'
        assert 'Hello-World!'.tr('a-d-f-m-s', '_') == 'He__o_Wor__!'
        assert 'Hello-World!'.tr('--a', 'Z') == 'ZelloZZorld!'
        assert 'Hello, World!'.tr(' --', 'Z') == 'HelloZZWorldZ'
    }

    void testListTake() {
        def data = [
            new ArrayList( [ 1, 2, 3 ] ),
            new LinkedList( [ 1, 2, 3 ] ),
            new Stack() {{ addAll( [ 1, 2, 3 ] ) }},
            new Vector( [ 1, 2, 3 ] ),
        ]
        data.each {
            assert it.take( -1 ) == []
            assert it.take(  0 ) == []
            assert it.take(  2 ) == [ 1, 2 ]
            assert it.take(  4 ) == [ 1, 2, 3 ]
        }
    }

    void testArrayTake() {
        String[] items = [ 'ant', 'bee', 'cat' ]

        assert items.take( -1 ) == [] as String[]
        assert items.take(  0 ) == [] as String[]
        assert items.take(  2 ) == [ 'ant', 'bee' ] as String[]
        assert items.take(  4 ) == [ 'ant', 'bee', 'cat' ] as String[]
    }

    void testMapTake() {
        def data = [
            [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ],
            new TreeMap( [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ] ),
        ]
        data.each {
            assert it.take( -1 ) == [:]
            assert it.take(  0 ) == [:]
            assert it.take(  2 ) == [ 'ant':10, 'bee':20 ]
            assert it.take(  4 ) == [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ]
            assert it.take(  5 ) == [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ]
        }
    }

    void testIteratorTake() {
        int a = 1
        Iterator items = [ hasNext:{ true }, next:{ a++ } ] as Iterator

        assert items.take( -1 ).collect { it } == []
        assert items.take(  0 ).collect { it } == []
        assert items.take(  2 ).collect { it } == [ 1, 2 ]
        assert items.take(  4 ).collect { it } == [ 3, 4, 5, 6 ]
    }

    void testCharSequenceTake() {
        def data = [ 'groovy',      // String
                     "${'groovy'}", // GString
                     java.nio.CharBuffer.wrap( 'groovy' ),
                     new StringBuffer( 'groovy' ),
                     new StringBuilder( 'groovy' ) ]
        data.each {
            // Need toString() as CharBuffer.subSequence returns a java.nio.StringCharBuffer
            assert it.take( -1 ).toString() == ''
            assert it.take(  0 ).toString() == ''
            assert it.take(  3 ).toString() == 'gro'
            assert it.take(  6 ).toString() == 'groovy'
            assert it.take( 10 ).toString() == 'groovy'
        }
    }

    void testListDrop() {
        def data = [
            new ArrayList( [ 1, 2, 3 ] ),
            new LinkedList( [ 1, 2, 3 ] ),
            new Stack() {{ addAll( [ 1, 2, 3 ] ) }},
            new Vector( [ 1, 2, 3 ] ),
        ]
        data.each {
            assert it.drop( -1 ) == [ 1, 2, 3 ]
            assert it.drop(  0 ) == [ 1, 2, 3 ]
            assert it.drop(  2 ) == [ 3 ]
            assert it.drop(  4 ) == []
        }
    }

    void testArrayDrop() {
        String[] items = [ 'ant', 'bee', 'cat' ]

        assert items.drop(  2 ) == [ 'cat' ] as String[]
        assert items.drop(  4 ) == [] as String[]
        assert items.drop(  0 ) == [ 'ant', 'bee', 'cat' ] as String[]
        assert items.drop( -1 ) == [ 'ant', 'bee', 'cat' ] as String[]
    }

    void testMapDrop() {
        def data = [
            [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ],
            new TreeMap( [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ] ),
        ]
        data.each {
            assert it.drop( -1 ) == [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ]
            assert it.drop(  0 ) == [ 'ant':10, 'bee':20, 'cat':30, 'dog':40 ]
            assert it.drop(  2 ) == [ 'cat':30, 'dog':40 ]
            assert it.drop(  4 ) == [ : ]
            assert it.drop(  5 ) == [ : ]
        }
    }

    void testIteratorDrop() {
        int a = 1
        Iterator items = [ hasNext:{ a < 6 }, next:{ a++ } ] as Iterator

        assert items.drop( 0 ).collect { it } == [ 1, 2, 3, 4, 5 ]
        a = 1
        assert items.drop( 2 ).collect { it } == [ 3, 4, 5 ]
        a = 1
        assert items.drop( 4 ).collect { it } == [ 5 ]
        a = 1
        assert items.drop( 5 ).collect { it } == []
    }

    void testCharSequenceDrop() {
        def data = [ 'groovy',      // String
                     "${'groovy'}", // GString
                     java.nio.CharBuffer.wrap( 'groovy' ),
                     new StringBuffer( 'groovy' ),
                     new StringBuilder( 'groovy' ) ]
        data.each {
            // Need toString() as CharBuffer.subSequence returns a java.nio.StringCharBuffer
            assert it.drop( -1 ).toString() == 'groovy'
            assert it.drop(  0 ).toString() == 'groovy'
            assert it.drop(  3 ).toString() == 'ovy'
            assert it.drop(  6 ).toString() == ''
            assert it.drop( 10 ).toString() == ''
        }
    }

    void testTakeDropClassSymmetry() {
        // NOTES:
        // - Cannot test plain HashMap, as Groovy will always default to a LinkedHashMap
        //   See org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.java:183
        // - Cannot test StringBuffer or StringBuilder, as X.subSequence in Java returns String
        def data = [
          // Lists
          (java.util.ArrayList)     : new ArrayList( [ 1, 2, 3 ] ),
          (java.util.LinkedList)    : new LinkedList( [ 1, 2, 3 ] ),
          (java.util.Stack)         : new Stack() {{ addAll( [ 1, 2, 3 ] ) }},
          (java.util.Vector)        : new Vector( [ 1, 2, 3 ] ),
          // Maps
          (java.util.LinkedHashMap) : new LinkedHashMap( [ a:1, b:2, c:3 ] ),
          (java.util.TreeMap)       : new TreeMap( [ a:1, b:2, c:3 ] ),
          (java.util.Hashtable)     : new Hashtable( [ a:1, b:2, c:3 ] ),
          // Iterators
          (java.util.Iterator)      : [ hasNext:{ true }, next:{ 'groovy' } ] as Iterator,
          // CharSequences
          (java.lang.String)        : new String( 'groovy' ),
          (java.nio.CharBuffer)     : java.nio.CharBuffer.wrap( 'groovy' ),
        ]
        data.each { Class clazz, object ->
            assert clazz.isInstance( object.take( 5 ) )
            assert clazz.isInstance( object.drop( 5 ) )
        }
    }

    void testContainsForPrimitiveArrays() {
        boolean[] bools = [false]
        byte[] bytes = [1, 3]
        short[] shorts = [1, 3]
        int[] ints = [1, 3]
        long[] longs = [1, 3]
        float[] floats = [1.0f, 3.0f]
        double[] doubles = [1.0d, 3.0d]
        char[] chars = ['a' as char, 'c' as char]

        assert bools.contains(false)
        assert !bools.contains(true)
        assert bytes.contains(3)
        assert !bytes.contains(2)
        assert shorts.contains(3)
        assert !shorts.contains(2)
        assert ints.contains(3)
        assert !ints.contains(2)
        assert longs.contains(3)
        assert !longs.contains(2)
        assert longs.contains(3)
        assert !longs.contains(2)
        assert floats.contains(3.0f)
        assert !floats.contains(2.0f)
        assert doubles.contains(3.0d)
        assert !doubles.contains(2.0d)
        assert chars.contains('c' as char)
        assert !chars.contains('b' as char)
    }

    void testCollectEntriesIterator() {
        def items = ['a', 'bb', 'ccc'].iterator()
        def map = items.collectEntries { [it, it.size()] }
        assert map == [a: 1, bb: 2, ccc: 3]
    }

    void testCollectEntriesIterable() {
        def things = new Things()
        def map = things.collectEntries { [it.toLowerCase(), it.toUpperCase()] }
        assert map == [a: 'A', b: 'B', c: 'C']
    }

    void testArrayContains() {
        String[] vowels = ['a', 'e', 'i', 'o', 'u']
        assert vowels.contains('u')
        assert !vowels.contains('x')
    }

    void testListTakeWhile() {
        def data = [
            new ArrayList( [ 1, 3, 2 ] ),
            new LinkedList( [ 1, 3, 2 ] ),
            new Stack() {{ addAll( [ 1, 3, 2 ] ) }},
            new Vector( [ 1, 3, 2 ] ),
        ]
        data.each {
            assert it.takeWhile{ it < 0 } == []
            assert it.takeWhile{ it < 1 } == []
            assert it.takeWhile{ it < 3 } == [ 1 ]
            assert it.takeWhile{ it < 4 } == [ 1, 3, 2 ]
        }
    }

    void testArrayTakeWhile() {
        String[] items = [ 'ant', 'bee', 'cat' ]

        assert items.takeWhile{ it == '' } == [] as String[]
        assert items.takeWhile{ it != 'cat' } == [ 'ant', 'bee' ] as String[]
        assert items.takeWhile{ it != '' } == [ 'ant', 'bee', 'cat' ] as String[]
    }

    void testIteratorTakeWhile() {
        int a = 1
        Iterator items = [ hasNext:{ true }, next:{ a++ } ] as Iterator

        assert items.takeWhile{ it < 5 }.collect { it } == [ 1, 2, 3, 4 ]
    }

    void testCharSequenceTakeWhile() {
        def data = [ 'groovy',      // String
                     "${'groovy'}", // GString
                     java.nio.CharBuffer.wrap( 'groovy' ),
                     new StringBuffer( 'groovy' ),
                     new StringBuilder( 'groovy' ) ]
        data.each {
            // Need toString() as CharBuffer.subSequence returns a java.nio.StringCharBuffer
            assert it.takeWhile{ it == '' }.toString() == ''
            assert it.takeWhile{ it != 'v' }.toString() == 'groo'
            assert it.takeWhile{ it }.toString() == 'groovy'
        }
    }
}

class WackyList extends LinkedList {
    WackyList(list) {super(list)}
    WackyList() {this([])}
}

class WackyHashCode {
    int hashCode() {return 1;}
}

class Things implements Iterable<String> {
    Iterator iterator() {
        ["a", "B", "c"].iterator()
    }
}

enum Suit { HEARTS, CLUBS, SPADES, DIAMONDS }
