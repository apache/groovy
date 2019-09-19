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

import groovy.test.GroovyTestCase

/**
 * Test for the spread dot operator "*.".
 *
 * For an example,
 *          list*.property
 * means
 *          list.collect { it?.property }
 */
class SpreadDotTest extends GroovyTestCase {
    void testSpreadDot() {
        def m1 = ["a": 1, "b": 2]
        def m2 = ["a": 11, "b": 22]
        def m3 = ["a": 111, "b": 222]
        def x = [m1, m2, m3]
        assert x*.a == [1, 11, 111]
        assert x*."a" == [1, 11, 111]
        assert x == [m1, m2, m3]

        def m4 = null
        x << m4
        assert x*.a == [1, 11, 111, null]
        assert x*."a" == [1, 11, 111, null]
        assert x == [m1, m2, m3, null]

        Date checkDate = new Date()
        def d = new SpreadDotDemo()
        x << d
        assert x*."a"[4] >= checkDate
        assert x == [m1, m2, m3, null, d]

        def y = new SpreadDotDemo2()
        assert y."a" == 'Attribute Get a'
        assert y.a == 'Attribute Get a'

        x << y
        assert x*."a"[5] == 'Attribute Get a'
        assert x == [m1, m2, m3, null, d, y]
    }

    void testSpreadDot2() {
        def a = new SpreadDotDemo()
        def b = new SpreadDotDemo2()
        def x = [a, b]

        assert x*.fnB("1") == [a.fnB("1"), b.fnB("1")]
        assert [a, b]*.fnB() == [a.fnB(), b.fnB()]
    }

    void testSpreadDotArrays() {
        def a = new SpreadDotDemo()
        def b = new SpreadDotDemo2()
        Object[] x = [a, b]

        assert x*.fnB("1") == [a.fnB("1"), b.fnB("1")]
        assert [a, b]*.fnB() == [a.fnB(), b.fnB()]

        int[] nums = [3, 4, 5]
        assert nums*.toString() == ['3', '4', '5']

        boolean[] answers = [true, false, true]
        assert answers*.toString() == ['true', 'false', 'true']

        String[] pets = ['ant', 'bear', 'camel']
        assert pets*.length() == nums
    }

    void testSpreadDotOnArrays2() {
        def books = [Book1, Book2, Book3] as Object[]

        books*.metaClass*.foo = { "Hello, ${delegate.class.name}" }

        assertEquals "Hello, groovy.Book1", new Book1().foo()
        assertEquals "Hello, groovy.Book2", new Book2().foo()
        assertEquals "Hello, groovy.Book3", new Book3().foo()
    }

    void testSpreadDotAdvanced() {
        assertEquals([3, 3], ['cat', 'dog']*.size())
        assertEquals([3, 3], (['cat', 'dog'] as Vector)*.size())
        assertEquals([3, 3], (['cat', 'dog'] as String[])*.size())
        assertEquals([3, 3], (['cat', 'dog'] as Vector).elements()*.size())
        assertEquals([1, 1, 1], 'zoo'*.size())
        assertEquals(Object, new Object().getClass())
        assertEquals([Object], new Object()*.getClass())
        assertEquals('Large', new Shirt().size())
        assertEquals(['Large'], new Shirt()*.size())
    }

    void testSpreadDotMap() {
        def map = [A: "one", B: "two", C: "three"]
        assert map.collect { child -> child.value.size() } == [3, 3, 5]
        assert map*.value*.size() == [3, 3, 5]
        assert map*.getKey() == ['A', 'B', 'C']
    }

    void testSpreadDotAttribute() {
        def s = new Singlet()
        assert s.size == 1
        assert s.@size == 12
        def wardrobe = [s, s]
        assert wardrobe*.size == [1, 1]
        assert wardrobe*.@size == [12, 12]
    }

    void testNewLine() {
        def x = [a: 1, b: 2]
        def y = x
                *.value
                *.toString()
        assert y == ['1', '2']
        def z = x
                *.value
                .sum()
        assert z == 3

        x = [new Singlet(), new Singlet()]
        y = x
                *.@size
        assert y == [12, 12]
    }
}

class SpreadDotDemo {
    java.util.Date getA() {
        return new Date()
    }

    String fnB() {
        return "bb"
    }

    String fnB(String m) {
        return "BB$m"
    }
}

class SpreadDotDemo2 {
    String getAttribute(String key) {
        return "Attribute $key"
    }

    String get(String key) {
        return getAttribute("Get $key")
    }

    String fnB() {
        return "cc"
    }

    String fnB(String m) {
        return "CC$m"
    }
}


class Book1 {}

class Book2 {}

class Book3 {}

class Shirt {
    def size() { 'Large' }
}

class Singlet {
    private size = 12

    def getSize() { 1 }
}
