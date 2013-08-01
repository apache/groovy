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

class ListTest extends GroovyTestCase {

    void testList() {
        def x = [10, 11]
        assert x.size() == 2
        x.add("cheese")
        assert x.size() == 3
        assert x.contains(10)
        assert x.contains(11)
        assert x.contains("cheese")
        assert x.get(0) == 10
        assert x.get(1) == 11
        assert x.get(2) == "cheese"

        // subscript operator
        assert x[0] == 10
        assert x[1] == 11
        assert x[2] == "cheese"

        x[3] = 12
        assert x[3] == 12
        assert x.contains("cheese")
        assert x.contains(10)
    }

    void testEmptyList() {
        def x = []
        assert x.size() == 0
        x.add("cheese")
        assert x.get(0) == "cheese"
        assert x.size() == 1
        assert x[0] == "cheese"
    }

    void testSubscript() {
        def x = []
        x[1] = 'cheese'
        assert x[0] == null
        assert x[1] == 'cheese'

        x[2] = 'gromit'
        x[3] = 'wallace'
        assert x.size() == 4

        x[-1] = 'nice'
        assert x[3] == 'nice'

        x[-2] = 'cheese'
        assert x[2] == 'cheese'
    }

    void testClosure() {
        def l = [1, 2, 3, "abc"]
        def result = ''
        def block = { i -> result += i }
        l.each(block)
        assert result == '123abc'
        l.each { i -> result += i }
        assert result == '123abc123abc'
    }

    void testMax() {
        def l = [1, 2, 5, 3, 7, 1]
        assert l.max() == 7

        l = [7, 2, 3]
        assert l.max() == 7

        l = [1, 2, 7]
        assert l.max() == 7

        // GROOVY-1006        
        l = [1, 3.2, 4L, (short) 7]
        assert l.max() == (short) 7
    }

    void testMin() {
        def l = [6, 4, 5, 1, 7, 2]
        assert l.min() == 1

        l = [7, 1, 3]
        assert l.min() == 1

        l = [1, 2, 7]
        assert l.min() == 1

        // GROOVY-1006        
        l = [(long) 1, 3.2, 4L, (short) 7]
        assert l.min() == (long) 1
    }

    void testPlus() {
        def l1 = [6, 4, 5, 1, 7, 2]
        def l2 = [6, 4, 5, 1, 7, [4, 5]]
        def l3 = l1 + l2
        assert l3 == [6, 4, 5, 1, 7, 2, 6, 4, 5, 1, 7, [4, 5]]

        l1 = [1, 5.2, 9]
        l2 = [3, 4L]
        l3 = [1, 5.2, 9, 3, 4L]
        assert l1 + l2 == l3
    }

    void testPlusOneElement() {
        def l1 = [6, 4, 5, 1, 7, 2]
        def l2 = "erererer"
        assert l1 + l2 == [6, 4, 5, 1, 7, 2, "erererer"]
    }

    void testListAppend() {
        def list = [1, 2]
        list << 3 << 4 << 5
        assert list == [1, 2, 3, 4, 5]
        def x = [] << 'a' << 'hello' << [2, 3] << 5
        assert x == ['a', 'hello', [2, 3], 5]
    }

    void testTimes() {
        def l = [4, 7, 8]
        assert l * 3 == [4, 7, 8, 4, 7, 8, 4, 7, 8]
    }

    // GROOVY-1006
    void testMinus() {
        def l1 = [1, 1, 2, 2, 3, 3, 3, 4, 5, 3, 5]
        def l2 = [1, 2.0, 4L]
        assert l1 - l2 == [3, 3, 3, 5, 3, 5]
    }

    // GROOVY-1006
    void testMinusDifferentTypes() {
        def l1 = [1, 1, "wrer", 2, 3, 3, "wrewer", 4, 5, "w", "w"]
        def l2 = [1, 2, "w"]
        assert l1 - l2 == ["wrer", 3, 3, "wrewer", 4, 5]
    }

    void testMinusEmptyCollection() {
        // GROOVY-790
        def list = [1, 1]
        assert list - [] == list

        // GROOVY-1006    
        list = [1, 2, 2, 3, 1]
        assert list - [] == list
    }

    void testIntersect() {
        def l1 = [1, 1, "wrer", 2, 3, 3, "wrewer", 4, 5, "w", "w"]
        def l2 = [1, 2, "f", "w"]
        assert l1.intersect(l2) == [1, 2, "w"]

        // GROOVY-1006    
        l1 = [1, 1.0, "wrer", 2, 3, 3L, "wrewer", 4, 5, "w", "w"]
        l2 = [(double) 1, 2L, "f", "w"]
        assert l1.intersect(l2) == [1, 2, "w"]
    }

    // GROOVY-1006
    void testListEqual() {
        assert [1, 2.0, 3L, (short) 4] == [1, 2, 3, 4]
    }

    // GROOVY-1006
    void testSortNumbersMixedType() {
        assert [1, (short) 3, 4L, 2.9, (float) 5.2].sort() == [1, 2.9, (short) 3, 4L, (float) 5.2]
    }

    // GROOVY-1006
    void testUnique() {
        def a = [1, 4L, 1.0]
        def b = a.unique()
        assert (b == a && a == [1, 4])
        a = [1, "foo", (short) 3, 4L, 1.0, (float) 3.0]
        b = a.unique()
        assert (b == a && a == [1, "foo", (short) 3, 4L])
    }

    // incorporates GROOVY-2904 and GROOVY-3102
    void testListFlatten() {
        def orig = [[[4, 5, 6, [46, 7, "erer"]], null, 4, [3, 6, 78]], 4]
        def flat = orig.flatten()
        assert flat == [4, 5, 6, 46, 7, "erer", null, 4, 3, 6, 78, 4]
    }

    void testFlattenListOfMaps() {
        def orig = [[a: 1, b: 2], [c: 3, d: 4]]
        def flat = orig.flatten()
        assert flat == orig
    }

    void testFlattenListOfArrays() {
        def orig = ["one".toList().toArray(), "two".toList().toArray()]
        def flat = orig.flatten()
        assert flat == ["o", "n", "e", "t", "w", "o"]
    }

    // incorporates GROOVY-2904 and GROOVY-3102
    void testFlattenListWithSuppliedClosure() {
        def orig = [[[4, 5, 6, [46, 7, "erer"]], null, 4, [3, 6, 78]], 4]
        def notSelfAsList = { def ans = it.iterator().toList(); ans != [it] ? ans : it }
        def flat = orig.flatten(notSelfAsList)
        assert flat == [4, 5, 6, 46, 7, "e", "r", "e", "r", 4, 3, 6, 78, 4]
    }

    void testFlattenListOfMapsWithClosure() {
        def orig = [[a: 1, b: 2], [c: 3, d: 4]]
        def flat = orig.flatten { it instanceof Map ? it.values() : it }
        assert flat == [1, 2, 3, 4]
        flat = orig.flatten { it instanceof Map ? it.keySet() : it }
        assert flat == ["a", "b", "c", "d"]
    }

    void testFlattenWithRanges() {
        def flat = [1, 3, 20..24, 33].flatten()
        assert flat == [1, 3, 20, 21, 22, 23, 24, 33]
    }

    void testListsAndRangesCompare() {
        def l = [1, 2, 3]
        def r = 1..3
        assert r == l
        assert l == r
    }

    void testRemove() {
        def l = ['a', 'b', 'c']
        l.remove(1)
        assert l == ['a', 'c']
        l.remove(0)
        assert l == ['c']
        assert l.size() == 1
    }

    void testPop() {
        def l = []
        l << 'a' << 'b'
        def value = l.pop()
        assert value == 'b'
        assert l == ['a']

        l.add('c')
        value = l.pop()
        assert value == 'c'
        value = l.pop()
        assert value == 'a'
        try {
            l.pop()
            fail("Should have thrown an exception")
        }
        catch (NoSuchElementException e) {
            assert e.message.contains('Cannot pop() an empty List')
        }
    }

    void testBoolCoerce() {
        // Explicit coercion
        assertFalse((Boolean) [])
        assertTrue((Boolean) [1])

        // Implicit coercion in statements
        List list = null
        if (list) {
            fail("null should have evaluated to false, but didn't")
        }
        list = []
        if (list) {
            fail("[] should have evaluated to false, but didn't")
        }
        list = [1]
        if (!list) {
            fail("[1] should have evaluated to true, but didn't")
        }
    }

    // see also SubscriptTest
    void testGetAtRange() {
        def list = [0, 1, 2, 3]
        assert list[0..3] == list         , 'full list'
        assert list[0..0] == [0]          , 'one element range'
        assert list[0..<0] == []          , 'empty range'
        assert list[3..0] == [3, 2, 1, 0] , 'reverse range'
        assert list[3..<0] == [3, 2, 1]   , 'reverse exclusive range'
        assert list[-2..-1] == [2, 3]     , 'negative index range'
        assert list[-2..<-1] == [2]       , 'negative index range exclusive'
        assert list[-1..-2] == [3, 2]     , 'negative index range reversed'
        assert list[-1..<-2] == [3]       , 'negative index range reversed exclusive'  // aaaahhhhh !
        assert list[0..-1] == list        , 'pos - neg value'
        assert list[0..<-1] == [0]        , 'pos - neg value exclusive -> empty'
        assert list[0..<-2] == list       , 'pos - neg value exclusive -> full'
        shouldFail(GroovyRuntimeException) { list[null] }
        shouldFail(IndexOutOfBoundsException) { list[5..6] }
    }

    void testPutAtSplice() {
        // usual assignments
        def list = [0, 1, 2, 3]
        list[1, 2] = [11, 12]
        assert list == [0, 11, 12, 3], 'same length assignment'
        list = [0, 1, 2, 3]
        shouldFail(IllegalArgumentException) {
            list[1, 1] = [11]
        }
        list = [0, 1, 2, 3]
        shouldFail(IllegalArgumentException) {
            list[1, 0] = []
        }
        // assignments outside current bounds
        list = [0, 1, 2, 3]
        list[-1, -2] = [-1, -2]
        assert list == [0, 1, -2, -1], 'left of left border'
        list = [0, 1, 2, 3]
        list[3, 4] = [3, 4]
        assert list == [0, 1, 2, 3, 4]
    }

    void testPutAtRange() {
        // usual assignments
        def list = [0, 1, 2, 3]
        list[1..2] = [11, 12]
        assert list == [0, 11, 12, 3], 'same length assignment'
        list = [0, 1, 2, 3]
        list[1..1] = [11]
        assert list == [0, 11, 2, 3], 'length 1 assignment'
        list = [0, 1, 2, 3]
        list[0..<0] = []
        assert list == [0, 1, 2, 3], 'length 0 assignment, empty splice'
        // assignments at bounds
        list = [0, 1, 2, 3]
        list[0..0] = [10]
        assert list == [10, 1, 2, 3], 'left border assignment'
        list = [0, 1, 2, 3]
        list[3..3] = [13]
        assert list == [0, 1, 2, 13], 'right border assignment'
        // assignments outside current bounds
        list = [0, 1, 2, 3]
        list[-1..-1] = [-1]
        assert list == [0, 1, 2, -1], 'left of left border'
        list = [0, 1, 2, 3]
        list[3..4] = [3, 4]
        assert list == [0, 1, 2, 3, 4]
        // structural changes
        list = [0, 1, 2, 3]
        list[1..2] = ['x']
        assert list == [0, 'x', 3], 'compacting'
        list = [0, 1, 2, 3]
        list[1..2] = ['x', 'x', 'x']
        assert list == [0, 'x', 'x', 'x', 3], 'extending'
    }

    void testCrazyPutAtRange() {
        def list = []
        list[0..<0] = [0, 1, 2, 3]
        assert list == [0, 1, 2, 3], 'fill by empty Range'
        list = [0, 1, 2, 3]
        list[3..0] = []
        assert list == [], 'delete by reverse Range'
        list = [0, 1, 2, 3]
        list[-4..-1] = []
        assert list == [], 'delete by negativ Range'
        list = [0, 1, 2, 3]
        list[0..-1] = []
        assert list == [], 'delete by pos-negativ Range'
    }

    // GROOVY-1128
    void testAsSynchronized() {
        def synclist = [].asSynchronized() << 1
        assert synclist == [1]
    }

    // GROOVY-1128
    void testAsImmutable() {
        def immlist = [1, 2, 3].asImmutable()
        assert immlist == [1, 2, 3]
        def testlist = ['a', 'b', 'c', 'd', 'e']
        assert testlist[immlist] == ['b', 'c', 'd']
        assert immlist[0] == 1
        assert immlist[0..-1] == immlist
        shouldFail(UnsupportedOperationException) {
            immlist << 1
        }
        shouldFail(UnsupportedOperationException) {
            immlist[0..<0] = [0]
        }
        shouldFail(UnsupportedOperationException) {
            immlist[0] = 1
        }
    }

    // GROOVY-4946
    void testWithLazyDefault() {
        def l1 = [].withLazyDefault { 42 }
        assert l1[0] == 42
        assert l1[2] == 42
        assert l1 == [42, null, 42]
        assert l1[-1] == 42
        assert l1[-3] == 42
        assert l1[1] == 42

        def l2 = [].withLazyDefault { 42 }
        assert l2[-1] == 42
        assert l2.size() == 1
        assert l2[0] == 42

        def l3 = [].withLazyDefault { it }
        assert l3[-1] == 0
        assert l3[1] == 1
        assert l3[3] == 3

        def l4 = [0, 1, null, 3].withLazyDefault { 42 }
        assert l4[0] == 0
        assert l4[1] == 1
        assert l4[3] == 3
        assert l4 == [0, 1, null, 3]

        def l5 = [].withLazyDefault { it }
        assert l5[-1] == 0
        assert l5[1] == 1
        assert l5[3] == 3
        assert l5[5] == 5
        assert l5 == [0, 1, null, 3, null, 5]

        def l6 = [].withLazyDefault { int index -> index }
        assert l6[-1] == 0
        assert l6[1] == 1
        assert l6[3] == 3
        assert l6[5] == 5
        assert l6[0..5] == [0, 1, null, 3, null, 5]
    }

    void testWithEagerDefault() {
        def l1 = [].withEagerDefault { 42 }
        assert l1[0] == 42
        assert l1[2] == 42
        assert l1 == [42, 42, 42]
        assert l1[1] == 42
        assert l1[-1] == 42
        assert l1[-3] == 42

        def l2 = [].withEagerDefault { 42 }
        assert l2[-1] == 42
        assert l2.size() == 1
        assert l2[0] == 42

        def l3 = [].withEagerDefault { it }
        assert l3[-1] == 0
        assert l3[1] == 1
        assert l3[3] == 3
        assert l3 == [0, 1, 2, 3]

        def l4 = [0, 1, null, 3].withEagerDefault { 42 }
        assert l4[0] == 0
        assert l4[1] == 1
        assert l4[3] == 3
        assert l4 == [0, 1, null, 3]

        def l5 = [].withEagerDefault { it }
        assert l5[-1] == 0
        assert l5[1] == 1
        assert l5[3] == 3
        assert l5[5] == 5
        assert l5 == [0, 1, 2, 3, 4, 5]

        def l6 = [].withEagerDefault { int index -> index }
        assert l6[-1] == 0
        assert l6[1] == 1
        assert l6[3] == 3
        assert l6[5] == 5
        assert l6[0..5] == [0, 1, 2, 3, 4, 5]
    }

    void testWithDefaultReturnWithDefaultSubList() {
        def l1 = [].withLazyDefault { 42 }
        assert l1[2] == 42
        assert l1 == [null, null, 42]

        def l2 = l1.subList(0, 2)
        assert l2 == [null, null]
        assert l2[2] == 42
        assert l2 == [null, null, 42]
    }

    void testWithDefaultRedirectsToWithLazyDefault() {
        def l1 = [].withDefault { 42 }
        assert l1[2] == 42
        assert l1 == [null, null, 42]
    }

    void testWithDefaultNullAsDefaultValue() {
        def l1 = [].withEagerDefault { null }
        assert l1[0] == null
        assert l1[2] == null
        assert l1 == [null, null, null]
    }
}
