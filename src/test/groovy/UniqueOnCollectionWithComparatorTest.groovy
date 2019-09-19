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

class UniqueOnCollectionWithComparatorTest extends GroovyTestCase {

    void testUniqueOnIterator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator({ a, b -> Math.abs(a) <=> Math.abs(b) })
        def it = list.iterator().unique(comparator)
        assert it instanceof Iterator
        def result = it.toList()
        assert result == [-1, 0]
    }

    void testUniqueWithComparatorList() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator({ a, b -> Math.abs(a) <=> Math.abs(b) })
        assert list.unique(comparator) == [-1, 0]
    }

    void testUniqueWithComparatorSet() {
        def set = [-1, 0, 1] as Set
        def comparator = new ClosureComparator({ a, b -> Math.abs(a) <=> Math.abs(b) })
        assert set.unique(comparator).size() == 2
    }

    /** GROOVY-4742 */
    void testImmutableUniqueWithComparatorList() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator({ a, b -> Math.abs(a) <=> Math.abs(b) })
        def result = list.unique(false, comparator)
        assert result == [-1, 0]
        assert list == [-1, 0, 1, 1, 0, -1]
    }

    /** GROOVY-4742 */
    void testImmutableUniqueWithComparatorSet() {
        def set = [-1, 0, 1] as Set
        def comparator = new ClosureComparator({ a, b -> Math.abs(a) <=> Math.abs(b) })
        def result = set.unique(false, comparator).size()
        assert result == 2
        assert set == [-1, 0, 1] as Set
    }

    /** GROOVY-4742 */
    void testImmutableUniqueWithComparator() {
        def comparator = [compare: { p1, p2 -> p1.lname <=> p2.lname ?: p1.fname <=> p2.fname }] as Comparator

        def a = [fname: "John", lname: "Taylor"]
        def b = [fname: "Clark", lname: "Taylor"]
        def c = [fname: "Tom", lname: "Cruz"]
        def d = [fname: "Clark", lname: "Taylor"]

        def list = [a, b, c, d]
        List list2 = list.unique(false, comparator)
        assert (list2 != list && list2 == [a, b, c])
    }
}