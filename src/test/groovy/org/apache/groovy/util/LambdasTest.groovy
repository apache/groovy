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

import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.DoublePredicate
import java.util.function.Function
import java.util.function.IntPredicate
import java.util.function.LongPredicate
import java.util.function.ObjIntConsumer
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
    void fatFreeLazyCollectingAndFindingAllOnInfiniteIterators() {
        Function<Integer, Integer> next = n -> n + 1
        // collecting(Iterator, Function) is lazy, so it terminates on an infinite source via take
        assert [1, 2, 3].repeat().collecting(next).take(6).toList() == [2, 3, 4, 2, 3, 4]

        Predicate<Integer> isEven = n -> n % 2 == 0
        // findingAll(Iterator, Predicate) is lazy too
        assert [1, 2, 3].repeat().findingAll(isEven).take(4).toList() == [2, 2, 2, 2]
    }

    @Test
    void fatFreeLazyVariantsMatchClosureVariants() {
        Function<Integer, Integer> next = n -> n + 1
        assert [1, 2, 3].iterator().collecting(next).toList() ==
            [1, 2, 3].iterator().collecting { it + 1 }.toList()

        Predicate<Integer> isEven = n -> n % 2 == 0
        assert [1, 2, 3, 4, 5, 6].iterator().findingAll(isEven).toList() ==
            [1, 2, 3, 4, 5, 6].iterator().findingAll { it % 2 == 0 }.toList()
    }

    @Test
    void fatFreeWithOverloadsForAnyAndEvery() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0

        // any(Iterable, BiPredicate, param) bakes in the right-curry
        assert [1, 2, 3].any(divisibleBy, 2)
        assert ![1, 3, 5].any(divisibleBy, 2)
        assert [1, 2, 3].any(divisibleBy, 2) ==
            [1, 2, 3].any(curryWith(divisibleBy, 2))

        // every(Iterable, BiPredicate, param) bakes in the right-curry
        assert [2, 4, 6].every(divisibleBy, 2)
        assert ![2, 3, 4].every(divisibleBy, 2)
        assert [2, 4, 6].every(divisibleBy, 2) ==
            [2, 4, 6].every(curryWith(divisibleBy, 2))
    }

    @Test
    void fatFreeCountOnIterableDirectAndWith() {
        Predicate<Integer> isEven = n -> n % 2 == 0
        // direct fat-free count(Iterable, Predicate)
        assert [2, 4, 2, 1, 3, 5, 2, 4, 3].count(isEven) == 5

        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        // count(Iterable, BiPredicate, param) bakes in the right-curry
        assert [2, 4, 2, 1, 3, 5, 2, 4, 3].count(divisibleBy, 2) ==
            [2, 4, 2, 1, 3, 5, 2, 4, 3].count(curryWith(divisibleBy, 2))
    }

    @Test
    void fatFreeDirectFindAndCountOnIterator() {
        Predicate<Integer> greaterThanOne = n -> n > 1
        assert [1, 2, 3].iterator().find(greaterThanOne) == 2
        assert [1, 2, 3].iterator().find(n -> (n as int) > 3) == null

        Predicate<Integer> isEven = n -> n % 2 == 0
        assert [2, 4, 2, 1, 3, 5, 2, 4, 3].iterator().count(isEven) == 5
        // Iterable count(Predicate) delegates to the Iterator variant
        assert [2, 4, 2, 1, 3, 5, 2, 4, 3].count(isEven) ==
            [2, 4, 2, 1, 3, 5, 2, 4, 3].iterator().count(isEven)
    }

    @Test
    void fatFreeWithVariantsForFindAndLazyIterators() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0

        // find(Iterator, BiPredicate, param) returns the first match
        assert [1, 2, 3, 4, 5, 6].iterator().find(divisibleBy, 3) == 3
        assert [1, 2, 3, 4, 5, 6].iterator().find(divisibleBy, 7) == null

        // findingAll(Iterator, BiPredicate, param) is lazy over an infinite source
        assert [1, 2, 3, 4, 5, 6].repeat().findingAll(divisibleBy, 3).take(4).toList() == [3, 6, 3, 6]

        // collecting(Iterator, BiFunction, param) is lazy over an infinite source
        BiFunction<Integer, Integer, Integer> add = (n, d) -> n + d
        assert [1, 2, 3].repeat().collecting(add, 10).take(6).toList() == [11, 12, 13, 11, 12, 13]
    }

    @Test
    void fatFreeWithVariantsMatchCurryWith() {
        BiPredicate<Integer, Integer> divisibleBy = (n, d) -> n % d == 0
        // no find(Iterator, Predicate) exists, so compare against Iterable find(Predicate)
        assert [1, 2, 3, 4, 5, 6].iterator().find(divisibleBy, 3) ==
            [1, 2, 3, 4, 5, 6].find(curryWith(divisibleBy, 3))
        assert [1, 2, 3, 4, 5, 6].iterator().findingAll(divisibleBy, 2).toList() ==
            [1, 2, 3, 4, 5, 6].iterator().findingAll(curryWith(divisibleBy, 2)).toList()

        BiFunction<Integer, Integer, Integer> add = (n, d) -> n + d
        assert [1, 2, 3].iterator().collecting(add, 10).toList() ==
            [1, 2, 3].iterator().collecting(curryWith(add, 10)).toList()
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

    @Test
    void fatFreeArrayObjectTwinsMatchClosure() {
        Integer[] nums = [1, 2, 3, 4, 5, 6]
        Function<Integer, Integer> dbl = n -> n * 2
        assert nums.collect(dbl) == nums.collect { it * 2 }
        assert nums.collect(['x'], dbl) == nums.collect(['x']) { it * 2 }

        Predicate<Integer> even = n -> n % 2 == 0
        assert nums.any(even) == nums.any { it % 2 == 0 }
        assert nums.every(even) == nums.every { it % 2 == 0 }
        assert nums.find(even) == nums.find { it % 2 == 0 }
        assert nums.findAll(even) == nums.findAll { it % 2 == 0 }
        assert nums.count(even) == nums.count { it % 2 == 0 }

        def acc = []
        (['a', 'b', 'c'] as String[]).each(acc::add)
        assert acc == ['a', 'b', 'c']
        def indexed = [:]
        ObjIntConsumer<String> rec = (item, i) -> { indexed[i] = item }
        (['a', 'b'] as String[]).eachWithIndex(rec)
        assert indexed == [0: 'a', 1: 'b']

        Function<String, Map.Entry> entry = w -> new MapEntry(w, w.size())
        assert (['a', 'bb'] as String[]).collectEntries(entry) == [a: 1, bb: 2]
        assert (['bb'] as String[]).collectEntries([a: 1], entry) == [a: 1, bb: 2]

        // GROOVY-10893: a null-returning transform inserts nothing (matches the Closure form)
        Function<String, Map.Entry> maybeEntry = w -> w.size() > 1 ? new MapEntry(w, w.size()) : null
        assert (['a', 'bb', 'ccc'] as String[]).collectEntries(maybeEntry) == [bb: 2, ccc: 3]
        assert (['a', 'bb'] as String[]).collectEntries([z: 0], maybeEntry) == [z: 0, bb: 2]

        // DGM collectEntries(Iterable, Map, Function) collector twin (skips null entries too)
        assert [1, 2, 3].collectEntries([z: 0], n -> n > 1 ? new MapEntry(n, n * n) : null) == [z: 0, 2: 4, 3: 9]
    }

    @Test
    void fatFreeArrayPrimitivePredicates() {
        int[] ints = [1, 2, 3, 4, 5]
        IntPredicate ie = n -> n % 2 == 0
        assert ints.any(ie)
        assert !ints.every(ie)
        assert ints.count(ie) == 2

        long[] longs = [1L, 2L, 3L, 4L]
        LongPredicate lg = n -> n > 2
        assert longs.any(lg)
        assert longs.count(lg) == 2

        double[] dbls = [1.0d, 2.0d, 3.0d, 4.0d]
        DoublePredicate dg = n -> n > 2
        assert dbls.count(dg) == 2

        // the value-count overload still works alongside the predicate twin
        assert ([1, 2, 2, 3, 2] as int[]).count(2) == 3
    }

    @Test
    void fatFreeArrayMethodReferenceUnderCompileStatic() {
        assert CompileStaticUsage.upperViaCollect(['a', 'bb'] as String[]) == ['A', 'BB']
        assert CompileStaticUsage.countEvenInts([1, 2, 3, 4] as int[]) == 2
    }

    @Test
    void fatFreeInjectMatchClosureVariants() {
        BinaryOperator<Integer> mult = (a, b) -> a * b
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b

        // inject(Object, BinaryOperator) catches an Iterator (which is not Iterable)
        assert [1, 2, 3, 4].iterator().inject(mult) == 24
        // inject(Iterator, initial, BiFunction)
        assert [1, 2, 3, 4].iterator().inject(0, add) == 10
        assert [1, 2, 3, 4].iterator().inject(0, add) == [1, 2, 3, 4].inject(0) { a, b -> a + b }
        // inject(Map, initial, BiFunction) folds over entries
        BiFunction<List, Map.Entry, List> collect = (list, e) -> list + [e.key] * e.value
        assert [a: 1, b: 2, c: 3].inject([], collect) == ['a', 'b', 'b', 'c', 'c', 'c']
    }

    @Test
    void fatFreeInjectAllMatchClosureVariants() {
        BinaryOperator<Integer> add = (a, b) -> a + b
        BiFunction<String, Integer, String> concat = (carry, next) -> carry + next

        assert (1..5).injectAll(add) == [3, 6, 10, 15]
        assert (1..5).injectAll(add) == (1..5).injectAll { a, b -> a + b }
        assert (1..3).injectAll('', concat) == ['1', '12', '123']
        assert (1..5).iterator().injectAll(add).toList() == [3, 6, 10, 15]
        assert (1..3).iterator().injectAll('', concat).toList() == ['1', '12', '123']

        BiFunction<String, Map.Entry, String> acc = (carry, e) -> carry + e.key * e.value
        assert [a: 1, b: 2, c: 3].injectAll('', acc) == ['a', 'abb', 'abbccc']
    }

    @Test
    void fatFreeInjectArrayVariants() {
        Integer[] nums = [1, 2, 3, 4]
        BinaryOperator<Integer> mult = (a, b) -> a * b
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b
        assert nums.inject(mult) == 24
        assert nums.inject(0, add) == 10
        assert nums.injectAll(mult) == [2, 6, 24]        // no-init uses a BinaryOperator
        assert nums.injectAll(0, add) == [1, 3, 6, 10]   // with-init uses a BiFunction
    }

    @Test
    void fatFreeInjectMethodReferenceUnderCompileStatic() {
        // method reference selects the BinaryOperator twins under @CompileStatic (GROOVY-12214)
        assert CompileStaticUsage.sumViaInject([1, 2, 3, 4]) == 10
        assert CompileStaticUsage.prefixSumViaInjectAll([1, 2, 3, 4, 5]) == [3, 6, 10, 15]
    }

    @Test
    void fatFreeGroupAMatchClosureVariants() {
        Function<Integer, List<Integer>> pair = n -> [n, n * 10]
        assert [1, 2, 3].collectMany(pair) == [1, 2, 3].collectMany { [it, it * 10] }
        assert [1, 2, 3].iterator().collectMany(pair).toList() == [1, 10, 2, 20, 3, 30]
        assert [1, 2, 3].iterator().collectingMany(pair).toList() == [1, 10, 2, 20, 3, 30]

        Function<Integer, Integer> parity = n -> n % 2
        assert [1, 2, 3, 4, 5].countBy(parity) == [1, 2, 3, 4, 5].countBy { it % 2 }
        assert [1, 2, 3, 4, 5, 6].groupBy(parity) == [1, 2, 3, 4, 5, 6].groupBy { it % 2 }

        Predicate<Integer> isEven = n -> n % 2 == 0
        assert [1, 2, 3, 4].split(isEven) == [1, 2, 3, 4].split { it % 2 == 0 }

        Predicate<Integer> lessThan3 = n -> n < 3
        assert [1, 2, 3, 0, 4].takeWhile(lessThan3) == [1, 2, 3, 0, 4].takeWhile { it < 3 }
        assert [1, 2, 3, 0, 4].dropWhile(lessThan3) == [1, 2, 3, 0, 4].dropWhile { it < 3 }
        assert [1, 2, 3, 0, 4].iterator().takeWhile(lessThan3).toList() == [1, 2]
        assert [1, 2, 3, 0, 4].iterator().dropWhile(lessThan3).toList() == [3, 0, 4]
    }

    @Test
    void fatFreeGroupAMapVariants() {
        BiFunction<String, Integer, Integer> valueParity = (k, v) -> v % 2
        assert [a: 1, b: 2, c: 3].countBy(valueParity) == [a: 1, b: 2, c: 3].countBy { k, v -> v % 2 }
        assert [a: 1, b: 2, c: 3].groupBy(valueParity) == [a: 1, b: 2, c: 3].groupBy { k, v -> v % 2 }
        assert [a: 1, b: 2, c: 3].collectMany((String k, Integer v) -> v < 3 ? [k] : []) == ['a', 'b']

        BiPredicate<String, Integer> valueLessThan3 = (k, v) -> v < 3
        assert [a: 1, b: 2, c: 3].takeWhile(valueLessThan3) == [a: 1, b: 2]
        assert [a: 1, b: 2, c: 3].dropWhile(valueLessThan3) == [c: 3]
    }

    @Test
    void fatFreeGroupAArrayVariants() {
        Integer[] nums = [1, 2, 3, 4, 5, 6]
        Function<Integer, Integer> parity = n -> n % 2
        assert nums.countBy(parity) == [1: 3, 0: 3]
        assert nums.groupBy(parity) == [1: [1, 3, 5], 0: [2, 4, 6]]
        assert nums.collectMany(n -> n % 2 ? [] : [n]) == [2, 4, 6]

        Predicate<Integer> isEven = n -> n % 2 == 0
        assert nums.split(isEven) == [[2, 4, 6], [1, 3, 5]]
        Predicate<Integer> lessThan3 = n -> n < 3
        assert nums.takeWhile(lessThan3).toList() == [1, 2]
        assert nums.dropWhile(lessThan3).toList() == [3, 4, 5, 6]
    }

    @Test
    void fatFreeGroupAMethodReferenceUnderCompileStatic() {
        // method reference selects the new countBy(Iterable, Function) twin under @CompileStatic (GROOVY-12214)
        assert CompileStaticUsage.countByLength(['a', 'bb', 'cc', 'ddd']) == [1: 1, 2: 2, 3: 1]
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

        static Integer sumViaInject(List<Integer> input) {
            input.inject(Integer::sum)
        }

        static List<Integer> prefixSumViaInjectAll(List<Integer> input) {
            input.injectAll(Integer::sum)
        }

        static Map<Integer, Integer> countByLength(List<String> input) {
            input.countBy(String::length)
        }

        static List<String> upperViaCollect(String[] input) {
            input.collect(String::toUpperCase)
        }

        static Number countEvenInts(int[] input) {
            input.count((int n) -> n % 2 == 0)
        }
    }
}
