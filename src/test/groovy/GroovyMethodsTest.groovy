/*
 * Copyright 2003-2007 the original author or authors.
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
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

/** 
 * Tests the various new Groovy methods
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Dierk Koenig
 * @author Paul King
 * @author Joachim Baumann
 * @version $Revision$
 */
class GroovyMethodsTest extends GroovySwingTestCase {
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
        if (headless) return

        def d0 = new Dimension(100, 200)
        assert d0 == new Dimension(width: 100, height: 200)
        assert d0 == [100, 200] as Dimension
        assert d0 == [width: 100, height: 200] as Dimension
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
        assert new File('build.properties').size()
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

    String getCmd() {
        def cmd = "ls -l"
        if (System.properties.'os.name'.contains('Win')) {
            cmd = "cmd /c dir"
        }
        return cmd
    }

    void testExecuteCommandLineProcessUsingAString() {
        println "executing command: ${cmd}"
        def process = cmd.execute()
        // lets have an easier way to do this!
        def count = 0
        println "Read the following lines..."
        /** @todo we should simplify the following line!!! */
        new InputStreamReader(process.in).eachLine {line ->
            println line
            ++count
        }
        println ""
        process.waitFor()
        def value = process.exitValue()
        println "Exit value of command line is ${value}"
        assert count > 1
    }

    /*
    void testExecuteCommandLineProcessAndUseWaitForOrKill_FAILS_ON_WINDOWS() {
        if (System.properties.'os.name'.contains('Windows') && notYetImplemented()) return

        println "executing command: ${cmd}"

        def process = cmd.execute()

        process.consumeProcessOutput()
        process.waitForOrKill(1000)
        def value = process.exitValue()
        println "Exit value of command line is ${value}"

        process = cmd.execute()

        process.consumeProcessOutput()
        process.waitForOrKill(10) // This fails on RLW's workstation with parameter 1, >=8 is required.
        value = process.exitValue()
        println "Exit value of command line is ${value}"

    }
    */

    void testExecuteCommandLineUnderWorkingDirectory_FAILS() {if (notYetImplemented()) return

        def envp = java.util.Array.newInstance(String, 0)
        def workDir = new File(".")

        println "executing command: ${cmd} under the directory ${workDir.canonicalPath}"

        def process = cmd.execute(envp, workDir)

        // lets have an easier way to do this!
        def count = 0

        println "Read the following lines under the directory ${workDir} ..."

        /** @todo we should simplify the following line!!! */
        new InputStreamReader(process.in).eachLine {line ->
            println line
            ++count
        }
        println ""

        process.waitFor()
        def value = process.exitValue()
        println "Exit value of command line is ${value}"

        assert count > 1
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

    void testGroupByList() {
        def expected = [Integer: [1, 2], String: ["a", "b"], BigDecimal: [3.5, 4.6]]
        def list = [1, "a", 2, "b", 3.5, 4.6]
        def result = list.groupBy {it.class}
        assert [1, 2] == result[Integer]
        assert ["a", "b"] == result[String]
        assert [3.5, 4.6] == result[BigDecimal]
        assert 3 == result.size()
    }

    void testMapGroupEntriesBy() {
        def expectedKeys = [Integer: [1, 3], String: [2, 4], BigDecimal: [5, 6]]
        def expectedVals = [Integer: [1, 2], String: ["a", "b"], BigDecimal: [3.5, 4.6]]
        def map = [1: 1, 2: "a", 3: 2, 4: "b", 5: 3.5, 6: 4.6]
        def result = map.groupEntriesBy {entry -> entry.value.class}
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
        def f = new File('build.properties')
        def expected = f.text
        assert expected == f.withReader { r -> r.text }
    }

    void testFileWithInputStream() {
        def f = new File('build.properties')
        def buf = new byte[f.size()]
        assert buf.size() == f.withInputStream { i -> i.read(buf) }
    }

    void testUrlReader() {
        def u = new File('build.properties').toURL()
        def expected = u.text
        assert expected == u.withReader { r -> r.text }
    }

    void testUrlWithInputStream() {
        def f = new File('build.properties')
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
        def y = x.findAll{ it % 2 == 1 }
        assert y.size() == 2
        assert y.class == WackyList
    }
}

class WackyList extends LinkedList {
    WackyList(list) {super(list)}
    WackyList() {this([])}
}

class WackyHashCode {
    int hashCode() {return 1;}
}
