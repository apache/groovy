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
package org.apache.groovy.util

import groovy.lang.Closure
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

import static org.apache.groovy.util.Lambdas.curryWith

class LambdasTest {

    @Test
    void curryWithBiPredicate() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        Predicate<Integer> isEven = curryWith(divisibleBy, 2)

        assert isEven.test(4)
        assert !isEven.test(5)
    }

    @Test
    void curryWithBiFunction() {
        BiFunction<String, Integer, String> repeat = (s, n) -> s * n
        Function<String, String> triple = curryWith(repeat, 3)

        assert triple.apply('a') == 'aaa'
        assert triple.apply('xy') == 'xyxyxy'
    }

    @Test
    void curryWithBiConsumer() {
        List<String> sink = []
        BiConsumer<String, List<String>> addTo = (s, list) -> list << s
        Consumer<String> intoSink = curryWith(addTo, sink)

        intoSink.accept('a')
        intoSink.accept('b')
        assert sink == ['a', 'b']
    }

    @Test
    void resultIsBareSamNotClosure() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        def result = curryWith(divisibleBy, 2)

        assert result instanceof Predicate
        assert !(result instanceof Closure)
    }

    @Test
    void curryWithFeedsStreamFilter() {
        BiPredicate<Integer, Integer> greaterThan = (n, threshold) -> n > threshold

        List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                .filter(curryWith(greaterThan, 2))
                .toList()

        assert result == [3, 4, 5]
    }

    @Test
    void curryWithFeedsSamAcceptingDgm() {
        // partitionPoint(List, Predicate) is SAM-accepting DGM
        BiPredicate<Integer, Integer> lessThan = (n, threshold) -> n < threshold

        assert [1, 2, 3, 4, 5, 6].partitionPoint(curryWith(lessThan, 4)) == 3
    }

    @Test
    void curryWithFeedsFatFreeFindAllAndCollect() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        assert [1, 2, 3, 4, 5, 6].findAll(curryWith(divisibleBy, 2)) == [2, 4, 6]

        BiFunction<String, Integer, String> repeat = (s, n) -> s * n
        assert ['a', 'b', 'c'].collect(curryWith(repeat, 3)) == ['aaa', 'bbb', 'ccc']
    }

    @Test
    void fatFreeWithOverloadsMatchCurryWith() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        // findAll(Iterable, BiPredicate, param) bakes in the right-curry
        assert [1, 2, 3, 4, 5, 6].findAll(divisibleBy, 2) ==
                [1, 2, 3, 4, 5, 6].findAll(curryWith(divisibleBy, 2))

        // findAll(Set, BiPredicate, param) preserves the Set type
        def evens = ([1, 2, 3, 4, 5, 6] as Set).findAll(divisibleBy, 2)
        assert evens == ([2, 4, 6] as Set)
        assert evens instanceof Set

        // find(Iterable, BiPredicate, param) returns the first match
        assert [1, 2, 3, 4, 5, 6].find(divisibleBy, 3) ==
                [1, 2, 3, 4, 5, 6].find(curryWith(divisibleBy, 3))

        BiFunction<String, Integer, String> repeat = (s, n) -> s * n
        // collect(Iterable, BiFunction, param) bakes in the right-curry
        assert ['a', 'b', 'c'].collect(repeat, 3) ==
                ['a', 'b', 'c'].collect(curryWith(repeat, 3))
    }

    @Test
    void curryWithReturnsFreshFunctionsThatShareNoState() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        Predicate<Integer> isEven = curryWith(divisibleBy, 2)
        Predicate<Integer> isMultipleOf3 = curryWith(divisibleBy, 3)

        assert isEven.test(6) && isMultipleOf3.test(6)
        assert isEven.test(4) && !isMultipleOf3.test(4)
        assert !isEven.test(9) && isMultipleOf3.test(9)
    }

    @Test
    void curryWithUnderCompileStatic() {
        assert CompileStaticUsage.evensViaStream([1, 2, 3, 4, 5, 6]) == [2, 4, 6]
        assert CompileStaticUsage.tripledViaStream(['a', 'b']) == ['aaa', 'bbb']
    }

    @CompileStatic
    static class CompileStaticUsage {
        static List<Integer> evensViaStream(List<Integer> input) {
            BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
            input.stream().filter(curryWith(divisibleBy, 2)).toList()
        }

        static List<String> tripledViaStream(List<String> input) {
            BiFunction<String, Integer, String> repeat = (s, n) -> s * n
            input.stream().map(curryWith(repeat, 3)).toList()
        }
    }
}
