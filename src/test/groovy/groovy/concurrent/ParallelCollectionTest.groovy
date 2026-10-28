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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class ParallelCollectionTest {

    @Test
    void testEachParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.CopyOnWriteArrayList

            def results = new CopyOnWriteArrayList()
            ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..10).toList().eachParallel { results << it }
            }
            assert results.sort() == (1..10).toList()
        '''
    }

    @Test
    void testEachParallelRunsConcurrently() {
        // CyclicBarrier.await blocks the worker. A pool whose size equals the
        // party count can leave the forked sibling queued while every worker
        // waits. One spare thread lets that sibling run. The pool does not
        // grow past its parallelism.
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.CopyOnWriteArraySet
            import java.util.concurrent.CyclicBarrier
            import java.util.concurrent.TimeUnit

            int parties = 2
            int threads = parties + 1
            def pool = Pool.fixed(threads)
            def fjp = pool.asForkJoinPool()
            try {
                ParallelScope.withPool(pool) { scope ->
                    def threadNames = new CopyOnWriteArraySet()
                    20.times {
                        def barrier = new CyclicBarrier(parties)

                        // IntRange path: same collection shape as `@Parallel for (i in 1..n)`
                        (1..parties).eachParallel { item ->
                            threadNames << Thread.currentThread().name
                            barrier.await(5, TimeUnit.SECONDS)
                        }
                    }

                    assert threadNames.size() >= parties
                    assert threadNames.size() <= threads
                }
                assert fjp.parallelism == threads
                assert fjp.poolSize <= threads
            } finally {
                pool.close()
            }
        '''
    }

    @Test
    void testEachWithIndexParallelRunsConcurrently() {
        // Same spare-worker rendezvous as testEachParallelRunsConcurrently,
        // on the indexed path eachWithIndexParallel uses. The pool does not
        // grow past its parallelism.
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.CyclicBarrier
            import java.util.concurrent.TimeUnit

            int parties = 2
            int threads = parties + 1
            def pool = Pool.fixed(threads)
            def fjp = pool.asForkJoinPool()
            try {
                ParallelScope.withPool(pool) { scope ->
                    10.times {
                        def barrier = new CyclicBarrier(parties)
                        def items = [0, 1]
                        items.eachWithIndexParallel { item, index ->
                            assert item == index
                            barrier.await(5, TimeUnit.SECONDS)
                        }
                    }
                }
                assert fjp.parallelism == threads
                assert fjp.poolSize <= threads
            } finally {
                pool.close()
            }
        '''
    }

    @Test
    void testFixedPoolDoesNotGrowForCpuWork() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.atomic.AtomicLong

            def pool = Pool.fixed(2)
            def fjp = pool.asForkJoinPool()
            def names = ConcurrentHashMap.newKeySet()
            def sink = new AtomicLong()
            try {
                ParallelScope.withPool(pool) { scope ->
                    (1..50_000).toList().eachParallel { value ->
                        names.add(Thread.currentThread().name)
                        long x = value
                        int i = 0
                        while (i < 200) {
                            x = (x * 31) + i
                            i++
                        }
                        sink.addAndGet(x)
                    }
                }
                assert names.size() >= 1
                assert names.size() <= 2
                assert fjp.parallelism == 2
                assert fjp.poolSize <= fjp.parallelism
                assert sink.get() != 0
            } finally {
                pool.close()
            }
        '''
    }

    // GROOVY-12457
    @Test
    void testNestedParallelStaysOnTheForkJoinPool() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.ForkJoinWorkerThread

            def pool = Pool.fixed(4)
            def expected = pool.asForkJoinPool()
            def workers = ConcurrentHashMap.newKeySet()
            try {
                Pool.withCurrent(pool) {
                    (1..32).toList().eachParallel {
                        assert Pool.current().is(pool)
                        (1..4).toList().eachParallel {
                            def thread = Thread.currentThread()
                            assert thread instanceof ForkJoinWorkerThread
                            assert thread.pool.is(expected)
                            assert Pool.current().is(pool)
                            workers.add(thread)
                        }
                    }
                    def other = Pool.fixed(2)
                    try {
                        def switched = new java.util.concurrent.atomic.AtomicReference()
                        [1].collectParallel {
                            Pool.withCurrent(other) {
                                [2].collectParallel {
                                    switched.set(Pool.current())
                                    2
                                }
                            }
                        }
                        assert switched.get().is(other)
                    } finally {
                        other.close()
                    }
                }
                assert !workers.isEmpty()
                assert workers.every { !it.name.contains('commonPool') }
            } finally {
                pool.close()
            }
        '''
    }

    @Test
    void testCollectParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..5).toList().collectParallel { it * 10 }
            }
            assert result.sort() == [10, 20, 30, 40, 50]
        '''
    }

    @Test
    void testFindAllParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..20).toList().findAllParallel { it % 3 == 0 }
            }
            assert result.sort() == [3, 6, 9, 12, 15, 18]
        '''
    }

    @Test
    void testFindAnyParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..100).toList().findAnyParallel { it > 50 }
            }
            assert result > 50
        '''
    }

    @Test
    void testAnyParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            ParallelScope.withPool(Pool.cpu()) { scope ->
                assert (1..10).toList().anyParallel { it == 5 }
                assert !(1..10).toList().anyParallel { it > 100 }
            }
        '''
    }

    @Test
    void testEveryParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            ParallelScope.withPool(Pool.cpu()) { scope ->
                assert (1..10).toList().everyParallel { it > 0 }
                assert !(1..10).toList().everyParallel { it > 5 }
            }
        '''
    }

    @Test
    void testCountParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def count = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..20).toList().countParallel { it % 2 == 0 }
            }
            assert count == 10
        '''
    }

    @Test
    void testMinParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                [3, 1, 4, 1, 5, 9, 2, 6].minParallel { a, b -> a <=> b }
            }
            assert result == 1
        '''
    }

    @Test
    void testMaxParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                [3, 1, 4, 1, 5, 9, 2, 6].maxParallel { a, b -> a <=> b }
            }
            assert result == 9
        '''
    }

    @Test
    void testSumParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..100).toList().sumParallel { a, b -> a + b }
            }
            assert result == 5050
        '''
    }

    @Test
    void testGroupByParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..10).toList().groupByParallel { it % 2 == 0 ? 'even' : 'odd' }
            }
            assert result['even'].sort() == [2, 4, 6, 8, 10]
            assert result['odd'].sort() == [1, 3, 5, 7, 9]
        '''
    }

    @Test
    void testParallelWithoutExplicitPool() {
        assertScript '''
            // Falls back to ForkJoinPool.commonPool()
            def result = (1..5).toList().collectParallel { it * 2 }
            assert result.sort() == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testCommonPoolRunsEveryCallbackShape() {
        assertScript '''
            def list = [1, 2, 3]
            def seen = new boolean[3]
            list.eachParallel { }
            assert list.collectParallel { it } == [1, 2, 3]
            assert list.findAllParallel { it > 1 } == [2, 3]
            assert list.findParallel { it == 2 } == 2
            assert list.findAnyParallel { it > 0 } != null
            assert list.anyParallel { it == 3 }
            assert list.everyParallel { it > 0 }
            assert list.countParallel { it > 1 } == 2L
            assert list.minParallel { a, b -> a <=> b } == 1
            assert list.maxParallel { a, b -> a <=> b } == 3
            assert [1, 2, 3].sumParallel { a, b -> a + b } == 6
            def grouped = list.groupByParallel { it % 2 == 0 ? 'e' : 'o' }
            assert grouped.e == [2]
            assert grouped.o.toSet() == [1, 3] as Set
            list.eachWithIndexParallel { item, index ->
                assert item == list[index]
                seen[index] = true
            }
            assert seen.every { it }
            assert list.collectManyParallel { [it, it] } == [1, 1, 2, 2, 3, 3]
            assert list.splitParallel { it % 2 == 0 } == [[2], [1, 3]]
            assert list.injectParallel(0) { a, b -> a + b } == 6
            assert list.grepParallel(Integer) == [1, 2, 3]
        '''
    }

    // === Tier 2 ===

    @Test
    void testEachWithIndexParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.ConcurrentHashMap

            def map = new ConcurrentHashMap()
            ParallelScope.withPool(Pool.cpu()) { scope ->
                ['a', 'b', 'c'].eachWithIndexParallel { item, idx -> map[idx] = item }
            }
            assert map == [0: 'a', 1: 'b', 2: 'c']
        '''
    }

    @Test
    void testCollectManyParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                [[1, 2], [3, 4], [5]].collectManyParallel { it.collect { n -> n * 10 } }
            }
            assert result.sort() == [10, 20, 30, 40, 50]
        '''
    }

    @Test
    void testSplitParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..10).toList().splitParallel { it % 2 == 0 }
            }
            assert result[0].sort() == [2, 4, 6, 8, 10]  // matching
            assert result[1].sort() == [1, 3, 5, 7, 9]    // non-matching
        '''
    }

    @Test
    void testInjectParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..10).toList().injectParallel(0) { a, b -> a + b }
            }
            assert result == 55
        '''
    }

    @Test
    void testGrepParallelWithClass() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                [1, 'two', 3, 'four', 5].grepParallel(String)
            }
            assert result.sort() == ['four', 'two']
        '''
    }

    @Test
    void testGrepParallelWithRange() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                (1..20).toList().grepParallel(5..10)
            }
            assert result.sort() == [5, 6, 7, 8, 9, 10]
        '''
    }

    @Test
    void testGrepParallelWithRegex() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                ['cat', 'car', 'dog', 'cart'].grepParallel(~/ca.*/)
            }
            assert result.sort() == ['car', 'cart', 'cat']
        '''
    }

    @Test
    void testNullResultsAgreeOnForkJoinAndExecutorPools() {
        assertScript '''
            import groovy.concurrent.Pool

            import java.util.concurrent.atomic.AtomicInteger

            def check = { Pool pool ->
                def run = { Closure body ->
                    pool == null ? body() : Pool.withCurrent(pool, body)
                }
                run {
                    assert [].findParallel { true } == null
                    assert [].findAnyParallel { true } == null
                    assert [].minParallel { a, b -> 0 } == null
                    assert [].maxParallel { a, b -> 0 } == null
                    assert [].sumParallel { a, b -> a } == null
                    assert [].groupByParallel { it }.isEmpty()

                    def sawNull = new AtomicInteger()
                    assert [null, 'a'].findParallel { item ->
                        if (item == null) {
                            sawNull.incrementAndGet()
                        }
                        item == null
                    } == null
                    assert sawNull.get() > 0

                    sawNull.set(0)
                    assert ['a', null].findAnyParallel { item ->
                        if (item == null) {
                            sawNull.incrementAndGet()
                        }
                        item == null
                    } == null
                    assert sawNull.get() > 0

                    def calls = new AtomicInteger()
                    def unused = { a, b ->
                        calls.incrementAndGet()
                        throw new AssertionError('one element must not call the operator')
                    }
                    assert [null].minParallel(unused) == null
                    assert [null].maxParallel(unused) == null
                    assert [null].sumParallel(unused) == null
                    assert calls.get() == 0

                    def nullsFirst = { a, b ->
                        if (a == null && b == null) return 0
                        if (a == null) return -1
                        if (b == null) return 1
                        a <=> b
                    }
                    assert [null, 'b', null].minParallel(nullsFirst) == null
                    assert [null, 'b', null].maxParallel(nullsFirst) == 'b'
                    assert ['a', 'b'].sumParallel { a, b -> null } == null

                    int n = Runtime.runtime.availableProcessors() * 3
                    def parts = (0..<n).collect { "v$it|" }
                    assert parts.sumParallel { a, b -> a + b } == parts.join('')
                    def ties = (0..<n).collect { new String('same') }
                    assert ties.minParallel { a, b -> a <=> b }.is(ties[0])
                    assert ties.maxParallel { a, b -> a <=> b }.is(ties[0])

                    def grouped = [1, 2, 3, 4].groupByParallel { it % 2 == 0 ? 'e' : null }
                    assert grouped[null] == [1, 3]
                    assert grouped.e == [2, 4]
                    assert grouped.keySet().toList() == [null, 'e']
                    def byValue = [3, 1, 2, 3, 1].groupByParallel { it }
                    assert byValue.keySet().toList() == [3, 1, 2]
                    assert byValue[3] == [3, 3]

                    def nullCalls = new AtomicInteger()
                    def counting = { a, b ->
                        nullCalls.incrementAndGet()
                        0
                    }
                    int width = Runtime.runtime.availableProcessors() * 3
                    def nulls = (0..<width).collect { null }
                    assert nulls.maxParallel(counting) == null
                    assert nullCalls.get() == 0
                    assert [null, null].minParallel(counting) == null
                    assert nullCalls.get() > 0
                    def strict = { a, b ->
                        if (a == null || b == null) {
                            throw new AssertionError('max must not compare null')
                        }
                        a <=> b
                    }
                    def mixed = (0..<width).collect { it % 2 == 0 ? null : it }
                    assert mixed.maxParallel(strict) == mixed.findAll { it != null }.max()
                    assert [null, 1].maxParallel(Comparator.naturalOrder()) == 1
                    assert [1, null, 2].grepParallel(null) == [null]
                    def odds = new Object() {
                        def isCase(value) { ((Number) value) % 2 }
                    }
                    def nothing = new Object() {
                        def isCase(value) { null }
                    }
                    assert [1, 2, 3, 4].grepParallel(odds) == [1, 3]
                    assert [1, 2, 3].grepParallel(nothing) == []
                    def wide = (1..64).toList()
                    def byParity = wide.groupByParallel { it % 2 }
                    assert byParity[0] == wide.findAll { it % 2 == 0 }
                    assert byParity[1] == wide.findAll { it % 2 != 0 }
                }
            }

            check(null)
            def fixed = Pool.fixed(2)
            def virtual = Pool.virtual()
            try {
                check(fixed)
                check(virtual)
            } finally {
                fixed.close()
                virtual.close()
            }
        '''
    }

    @Test
    void testFindParallel() {
        assertScript '''
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool

            def result = ParallelScope.withPool(Pool.cpu()) { scope ->
                [1, 2, 3, 4, 5].findParallel { it > 3 }
            }
            assert result == 4

            def none = ParallelScope.withPool(Pool.cpu()) { scope ->
                [1, 2, 3].findParallel { it > 10 }
            }
            assert none == null
        '''
    }

    @Test
    void testVirtualPoolRunsParallelWorkOnItsOwnThreads() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.ConcurrentHashMap

            def pool = Pool.virtual()
            def threads = ConcurrentHashMap.newKeySet()
            def caller = Thread.currentThread()
            try {
                def ordered = Pool.withCurrent(pool) {
                    def list = (1..12).toList()
                    def touch = { threads.add(Thread.currentThread()) }
                    list.eachParallel { touch() }
                    def collected = list.collectParallel { touch(); it * 2 }
                    def found = list.findAllParallel { touch(); it % 2 == 0 }
                    def first = list.findParallel { touch(); it > 9 }
                    def anyHit = list.findAnyParallel { touch(); it > 9 }
                    def any = list.anyParallel { touch(); it == 12 }
                    def every = list.everyParallel { touch(); it > 0 }
                    def counted = list.countParallel { touch(); it % 2 == 0 }
                    def least = list.minParallel { a, b -> a <=> b }
                    def most = list.maxParallel { a, b -> a <=> b }
                    def sum = ['a', 'b', 'c', 'd'].sumParallel { a, b -> a + b }
                    def grouped = [1, 2, 3].groupByParallel { it % 2 == 0 ? 'e' : 'o' }
                    def indexed = new Object[3]
                    new LinkedHashSet([10, 20, 30]).eachWithIndexParallel { item, index ->
                        touch()
                        indexed[index] = item
                    }
                    def flat = [1, 2].collectManyParallel { touch(); [it, it] }
                    def parts = [1, 2, 3, 4].splitParallel { touch(); it % 2 == 0 }
                    def injected = list.injectParallel(0) { a, b -> a + b }
                    def untouched = [].injectParallel(5) { a, b ->
                        throw new IllegalStateException('empty inject must not call the operator')
                    }
                    def grepped = [1, 'a', 2, 'b'].grepParallel(String)
                    def linked = new LinkedList([1, 2, 3]).collectParallel { touch(); it }
                    [1].eachParallel {
                        touch()
                        [2].eachParallel { touch() }
                    }
                    [collected, found, first, anyHit, any, every, counted, least, most, sum,
                     grouped, indexed.toList(), flat, parts, injected, untouched, grepped, linked]
                }

                assert ordered[0] == (1..12).collect { it * 2 }
                assert ordered[1] == (1..12).findAll { it % 2 == 0 }
                assert ordered[2] == 10
                assert ordered[3] > 9
                assert ordered[4]
                assert ordered[5]
                assert ordered[6] == 6
                assert ordered[7] == 1
                assert ordered[8] == 12
                assert ordered[9] == 'abcd'
                assert ordered[10].o == [1, 3]
                assert ordered[10].e == [2]
                assert ordered[11] == [10, 20, 30]
                assert ordered[12] == [1, 1, 2, 2]
                assert ordered[13][0] == [2, 4]
                assert ordered[13][1] == [1, 3]
                assert ordered[14] == (1..12).sum()
                assert ordered[15] == 5
                assert ordered[16] == ['a', 'b']
                assert ordered[17] == [1, 2, 3]

                assert threads.any { it != caller }
                threads.each { thread ->
                    if (thread == caller) {
                        return
                    }
                    assert !thread.name.contains('commonPool')
                    assert !thread.name.contains('ForkJoinPool')
                    if (pool.usesVirtualThreads()) {
                        assert thread.virtual
                    } else {
                        assert thread.name.startsWith('groovy-pool-virtual-')
                    }
                }
            } finally {
                pool.close()
            }
        '''
    }

    @Test
    void testIoPoolIsUsedForParallelWork() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.ConcurrentHashMap

            def pool = Pool.io()
            def threads = ConcurrentHashMap.newKeySet()
            def caller = Thread.currentThread()
            try {
                Pool.withCurrent(pool) {
                    (1..8).toList().eachParallel { threads.add(Thread.currentThread()) }
                }
                threads.remove(caller)
                assert !threads.isEmpty()
                try {
                    def fjp = pool.asForkJoinPool()
                    assert fjp.parallelism == pool.poolSize
                    threads.each { thread ->
                        assert thread.name.contains('ForkJoinPool')
                        assert !thread.name.contains('commonPool')
                    }
                } catch (UnsupportedOperationException expected) {
                    assert pool.usesVirtualThreads()
                    threads.each { thread ->
                        assert thread.virtual
                        assert !thread.name.contains('commonPool')
                        assert !thread.name.contains('ForkJoinPool')
                    }
                }
            } finally {
                pool.close()
            }
        '''
    }

    @Test
    void testParallelMethodsRejectNullArguments() {
        assertScript '''
            import org.codehaus.groovy.runtime.ParallelCollectionExtensions

            import static groovy.test.GroovyAssert.shouldFail

            def self = shouldFail(NullPointerException) {
                ParallelCollectionExtensions.eachParallel(null) { }
            }
            assert self.message == 'self must not be null'

            def callback = shouldFail(NullPointerException) {
                ParallelCollectionExtensions.eachParallel([1], null)
            }
            assert callback.message == 'callback must not be null'

            def missingSelf = shouldFail(NullPointerException) {
                ParallelCollectionExtensions.grepParallel(null, Integer)
            }
            assert missingSelf.message == 'self must not be null'

            assert [1, null, 2].grepParallel(null) == [null]
            assert [1, null, 2].grep(null) == [null]
        '''
    }

    @Test
    void testForkJoinLookupFailurePropagates() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.ForkJoinPool

            import static groovy.test.GroovyAssert.shouldFail

            def pool = new Pool() {
                void execute(Runnable command) {
                    throw new AssertionError('executor path should not run')
                }
                int getPoolSize() { 1 }
                int getActiveCount() { 0 }
                boolean usesVirtualThreads() { false }
                ForkJoinPool asForkJoinPool() {
                    throw new IllegalStateException('broken pool')
                }
                void shutdown() { }
            }
            def thrown = shouldFail(IllegalStateException) {
                Pool.withCurrent(pool) {
                    [1, 2].eachParallel { }
                }
            }
            assert thrown.message == 'broken pool'
        '''
    }

    @Test
    void testShutdownPoolRejectsParallelWork() {
        assertScript '''
            import groovy.concurrent.Pool

            import static groovy.test.GroovyAssert.shouldFail

            def pool = Pool.virtual()
            pool.shutdown()
            shouldFail(java.util.concurrent.RejectedExecutionException) {
                Pool.withCurrent(pool) {
                    [1, 2, 3].eachParallel { }
                }
            }
        '''
    }

    @Test
    void testSynchronousPoolDoesNotDeadlock() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.ForkJoinPool

            def pool = new Pool() {
                void execute(Runnable command) { command.run() }
                int getPoolSize() { 1 }
                int getActiveCount() { 0 }
                boolean usesVirtualThreads() { false }
                ForkJoinPool asForkJoinPool() {
                    throw new UnsupportedOperationException('synchronous executor')
                }
                void shutdown() { }
            }
            def result = Pool.withCurrent(pool) {
                def nested = []
                [1].eachParallel {
                    [2, 3].eachParallel { nested << it }
                }
                def collected = [1, 2, 3].collectParallel { it }
                assert Pool.current().is(pool)
                [nested, collected]
            }
            assert result == [[2, 3], [1, 2, 3]]
        '''
    }

    @Test
    void testCollectManyOnAnExecutorRejectsANullInnerCollection() {
        assertScript '''
            import groovy.concurrent.Pool

            import static groovy.test.GroovyAssert.shouldFail

            def pool = Pool.virtual()
            try {
                def thrown = shouldFail(NullPointerException) {
                    Pool.withCurrent(pool) {
                        [1].collectManyParallel { null }
                    }
                }
                assert thrown.message == 'transform returned null'
            } finally {
                pool.close()
            }
        '''
    }
}
