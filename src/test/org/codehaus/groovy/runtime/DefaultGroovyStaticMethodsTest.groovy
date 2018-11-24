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
package org.codehaus.groovy.runtime

import java.util.stream.Collectors
import java.util.stream.Stream

import static groovy.lang.Tuple.collectors
import static groovy.lang.Tuple.tuple

/**
 * Tests for DefaultGroovyStaticMethods
 */
class DefaultGroovyStaticMethodsTest extends GroovyTestCase {

    void testCurrentTimeSeconds() {
	    long timeMillis = System.currentTimeMillis()
        long timeSeconds = System.currentTimeSeconds()
        long timeMillis2 = System.currentTimeMillis()
        assert timeMillis/1000 as long <= timeSeconds
        assert timeMillis2/1000 as long >= timeSeconds
    }

    void testFirst() {
        assert 2 == Stream.of(2, 3, 6, 5).collect(Collectors.first()).get()
    }

    void testLast() {
        assert 5 == Stream.of(2, 3, 6, 5).collect(Collectors.last()).get()
    }

    void testFirstAndLast() {
        Tuple2<Integer, Integer> t =
                Stream.of(2, 3, 6, 5)
                        .collect(collectors(Collectors.first(), Collectors.last()))
                        .map1(Optional::get).map2(Optional::get)
        assert tuple(2, 5) == t
    }

    void testCountDistinct() {
        Tuple2<Long, Long> t = Stream.of(2 , 3, 4, 5, 6, 2, 3, 4, 5, 6)
                        .collect(collectors(Collectors.count(), Collectors.countDistinct()))

        assert tuple(10L, 5L) == t
    }

    void testCountDistinctBy() {
        Tuple2<Long, Long> t = Stream.of('a', 'ab', 'abc', 'a', 'ab', 'abc')
                .collect(collectors(Collectors.count(), Collectors.countDistinctBy(String::length)))

        assert tuple(6L, 3L) == t
    }

    void testSum() {
        Tuple1<Integer> t = Stream.of(1, 2, 3)
                .collect(collectors(Collectors.sum()))
                .map1(Optional::get)

        assert tuple(6) == t
    }

    void testSumBy() {
        Tuple1<Integer> t = Stream.of('a', 'ab', 'abc', 'abcd')
                .collect(collectors(Collectors.sumBy(String::length)))
                .map1(Optional::get)

        assert tuple(10) == t
    }

    void testAvg() {
        Tuple1<Integer> t = Stream.of(1, 2, 3)
                .collect(collectors(Collectors.avg()))
                .map1(Optional::get)

        assert tuple(2) == t
    }

    void testAvgBy() {
        Tuple1<Integer> t = Stream.of('ab', 'abcd')
                .collect(collectors(Collectors.avgBy(String::length)))
                .map1(Optional::get)

        assert tuple(3) == t
    }

    void testMinAndMax() {
        Tuple2<Integer, Integer> t =
                Stream.of(2, 3, 6, 5)
                        .collect(collectors(Collectors.min(), Collectors.max()))
                        .map1(Optional::get).map2(Optional::get)

        assert tuple(2, 6) == t

        Tuple2<Integer, Integer> t2 =
                Stream.of('ab', 'c', 'abc', 'efgh', 'de', 'fgh')
                        .collect(collectors(Collectors.min((o1, o2) -> o1.length() <=> o2.length()), Collectors.max((o1, o2) -> o1.length() <=> o2.length())))
                        .map1(Optional::get).map2(Optional::get)

        assert tuple('c', 'efgh') == t2
    }

    void testMinByAndMaxBy() {
        Tuple2<Integer, Integer> t =
                Stream.of('ab', 'c', 'abc', 'efgh', 'de', 'fgh')
                        .collect(collectors(Collectors.minBy((o1, o2) -> o1 <=> o2, e -> e.length()), Collectors.maxBy((o1, o2) -> o1 <=> o2, e -> e.length())))
                        .map1(Optional::get).map2(Optional::get)

        assert tuple('c', 'efgh') == t
    }

    void testAllMatchAndNoneMatchAndAnyMatch() {
        Tuple3<Boolean, Boolean, Boolean> t =
                Stream.of(true, false, true)
                    .collect(collectors(Collectors.allMatch(), Collectors.noneMatch(), Collectors.anyMatch()))

        assert tuple(false, false, true) ==  t

        Tuple3<Boolean, Boolean, Boolean> t2 =
                Stream.of(2, 4, 6, 8, 10)
                        .collect(collectors(Collectors.allMatchBy(e -> 0 == e % 2), Collectors.noneMatchBy(e -> 1 == e % 2), Collectors.anyMatchBy(e -> e > 10)))

        assert tuple(true, true, false) ==  t2
    }
}
