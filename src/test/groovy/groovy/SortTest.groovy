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

class SortTest extends GroovyTestCase {

    // GROOVY-1956
    void testSortWithNull() {
        // normal case, should sort in place and return result
        def x = [1, 2, 3, 1, 2, 3, null, 'a', null]
        assert x.is(x.sort())
        def y = x.sort()
        assert (y == x && x == [null, null, 1, 1, 2, 2, 3, 3, 'a'])

        // transitivity
        x = [1, 2, 3, 1, 2, 3, null, 'a', null]
        x.unique().sort()
        y = [1, 2, 3, 1, 2, 3, null, 'a', null]
        y.sort().unique()
        assert (x == y && y == [null, 1, 2, 3, 'a'])
    }

    // GROOVY-1956
    void testSortWithNullUsingOrderBy() {
        def x = [1, 2, 'Z', 'a', null]
        def y = x.sort()
        assert y == [null, 1, 2, 'Z', 'a']
        def z = x.sort { it?.respondsTo('toUpperCase') ? it?.toUpperCase() : it }
        assert z == [null, 1, 2, 'a', 'Z']
    }

    void testSortWithOrderBy() {
        def list = getPeople()
        def order = new OrderBy({ it.cheese })
        list.sort(true, order)
        assert list[0].name == 'Joe'
        assert list[-1].name == 'Chris'
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']
    }

    void testSortWithClosure() {
        def list = getPeople()
        list.sort { it.cheese }
        assert list.name == ['Joe', 'Bob', 'James', 'Chris']
    }

    void testArraySort() {
        def s = "The quick brown fox jumped over the lazy dog"
        def words = s.split()
        assert words.sort() == ['The', 'brown', 'dog', 'fox', 'jumped', 'lazy', 'over', 'quick', 'the'] as String[]
        assert words.sort(new IgnoreCaseComparator()) == ['brown', 'dog', 'fox', 'jumped', 'lazy', 'over', 'quick', 'The', 'the'] as String[]
        words = s.split() // back to a known order
        assert words.sort {
            it.size()
        } == ['The', 'fox', 'the', 'dog', 'over', 'lazy', 'quick', 'brown', 'jumped'] as String[]
    }

    void testSortClassHierarchy() {
        def aFooList = [
                new AFoo(5),
                new AFoo(7),
                new ABar(4),
                new ABar(6)
        ]
        def sorted = aFooList.sort()
        assert sorted.collect { it.class } == [ABar, AFoo, ABar, AFoo]
        assert sorted.collect { it.key } == (4..7).toList()
    }

    def getPeople() {
        def answer = []
        answer << new Expando(name: 'James', cheese: 'Edam', location: 'London')
        answer << new Expando(name: 'Bob', cheese: 'Cheddar', location: 'Atlanta')
        answer << new Expando(name: 'Chris', cheese: 'Red Leicester', location: 'London')
        answer << new Expando(name: 'Joe', cheese: 'Brie', location: 'London')
        return answer
    }

}

class AFoo implements Comparable {
    int key

    AFoo(int key) { this.key = key }

    int compareTo(Object rhs) { key - rhs.key }

    String toString() { this.class.name + ": " + key }
}

class ABar extends AFoo {
    ABar(int x) { super(x) }
}

class IgnoreCaseComparator implements Comparator {
    int compare(Object o1, Object o2) {
        return o1.toUpperCase() <=> o2.toUpperCase()
    }
}
