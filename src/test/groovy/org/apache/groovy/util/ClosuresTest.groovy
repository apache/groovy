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

import static org.apache.groovy.util.Closures.curryWith
import static org.apache.groovy.util.Closures.from

class ClosuresTest {

    // ---- from(Predicate/Function/Consumer) -----------------------------

    @Test
    void fromLiftsPredicateIntoHybrid() {
        Predicate<Integer> isEven = n -> n % 2 == 0
        def lifted = from(isEven)

        assert lifted instanceof Predicate
        assert lifted instanceof Closure
        assert lifted.test(4)
        assert !lifted.test(5)
    }

    @Test
    void fromLiftsFunctionIntoHybrid() {
        Function<Integer, Integer> twice = n -> n * 2
        def lifted = from(twice)

        assert lifted instanceof Function
        assert lifted instanceof Closure
        assert lifted.apply(3) == 6
    }

    @Test
    void fromLiftsConsumerIntoHybrid() {
        List<String> sink = []
        Consumer<String> intoSink = s -> sink << s
        def lifted = from(intoSink)

        assert lifted instanceof Consumer
        assert lifted instanceof Closure
        lifted.accept('a')
        assert sink == ['a']
    }

    @Test
    void fromIsIdempotentForAlreadyHybrid() {
        Predicate<Integer> isEven = n -> n % 2 == 0
        def first = from(isEven)
        def second = from(first)

        assert second.is(first)
    }

    // ---- from feeds DGM (Closure-accepting) ---------------------------

    @Test
    void fromFeedsDgmFindAll() {
        Predicate<Integer> isEven = n -> n % 2 == 0
        assert [1, 2, 3, 4, 5].findAll(from(isEven)) == [2, 4]
    }

    @Test
    void fromFeedsDgmCollect() {
        Function<Integer, Integer> twice = n -> n * 2
        assert [1, 2, 3].collect(from(twice)) == [2, 4, 6]
    }

    @Test
    void fromFeedsDgmEach() {
        List<String> sink = []
        Consumer<String> intoSink = s -> sink << s
        ['a', 'b', 'c'].each(from(intoSink))
        assert sink == ['a', 'b', 'c']
    }

    // ---- from feeds JDK stream/SAM APIs --------------------------------

    @Test
    void fromFeedsStreamFilter() {
        Predicate<Integer> isEven = n -> n % 2 == 0
        assert Stream.of(1, 2, 3, 4, 5).filter(from(isEven)).toList() == [2, 4]
    }

    // ---- curryWith composes from + Lambdas.curryWith -------------------

    @Test
    void curryWithBiPredicateReturnsHybrid() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        def result = curryWith(divisibleBy, 2)

        assert result instanceof Predicate
        assert result instanceof Closure
        assert result.test(4)
        assert !result.test(5)
    }

    @Test
    void curryWithBiFunctionReturnsHybrid() {
        BiFunction<String, Integer, String> repeat = (s, n) -> s * n
        def result = curryWith(repeat, 3)

        assert result instanceof Function
        assert result instanceof Closure
        assert result.apply('a') == 'aaa'
    }

    @Test
    void curryWithBiConsumerReturnsHybrid() {
        List<String> sink = []
        BiConsumer<String, List<String>> addTo = (s, list) -> list << s
        def result = curryWith(addTo, sink)

        assert result instanceof Consumer
        assert result instanceof Closure
        result.accept('a')
        assert sink == ['a']
    }

    // ---- curryWith feeds DGM ------------------------------------------

    @Test
    void curryWithFeedsDgmFindAll() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0

        assert [1, 2, 3, 4, 5, 6].findAll(curryWith(divisibleBy, 2)) == [2, 4, 6]
        assert [1, 2, 3, 4, 5, 6].findAll(curryWith(divisibleBy, 3)) == [3, 6]
    }

    @Test
    void curryWithFeedsDgmCollect() {
        BiFunction<String, Integer, String> repeat = (s, n) -> s * n

        assert ['a', 'b', 'c'].collect(curryWith(repeat, 3)) == ['aaa', 'bbb', 'ccc']
    }

    @Test
    void curryWithFeedsDgmEach() {
        List<String> sink = []
        BiConsumer<String, List<String>> addTo = (s, list) -> list << s

        ['a', 'b', 'c'].each(curryWith(addTo, sink))
        assert sink == ['a', 'b', 'c']
    }

    // ---- curryWith feeds JDK streams ----------------------------------

    @Test
    void curryWithFeedsStreamFilter() {
        BiPredicate<Integer, Integer> greaterThan = (n, threshold) -> n > threshold

        List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                .filter(curryWith(greaterThan, 2))
                .toList()

        assert result == [3, 4, 5]
    }

    // ---- @CompileStatic --------------------------------------------------

    @Test
    void curryWithUnderCompileStatic() {
        assert CompileStaticUsage.evensViaFindAll([1, 2, 3, 4, 5, 6]) == [2, 4, 6]
        assert CompileStaticUsage.evensViaStream([1, 2, 3, 4, 5, 6]) == [2, 4, 6]
        assert CompileStaticUsage.doublesViaCollect([1, 2, 3, 4]) == [false, true, false, true]
    }

    @CompileStatic
    static class CompileStaticUsage {
        static List<Integer> evensViaFindAll(List<Integer> input) {
            BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
            input.findAll(curryWith(divisibleBy, 2))
        }

        static List<Integer> evensViaStream(List<Integer> input) {
            BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
            input.stream().filter(curryWith(divisibleBy, 2)).toList()
        }

        static List<Boolean> doublesViaCollect(List<Integer> input) {
            BiFunction<Integer, Integer, Boolean> divisibleBy = (n, d) -> n % d == 0
            input.collect(curryWith(divisibleBy, 2))
        }
    }
}
