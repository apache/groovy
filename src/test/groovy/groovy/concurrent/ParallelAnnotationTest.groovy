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

import java.util.concurrent.ForkJoinPool

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assumptions.assumeTrue

final class ParallelAnnotationTest {

    @Test
    void testBasicParallelFor() {
        assertScript '''
            import groovy.transform.Parallel
            import java.util.concurrent.CopyOnWriteArrayList

            def results = new CopyOnWriteArrayList()

            @Parallel
            for (item in [1, 2, 3, 4, 5]) {
                results << item * 10
            }

            assert results.sort() == [10, 20, 30, 40, 50]
        '''
    }

    @Test
    void testParallelForStructuredCompletion() {
        assertScript '''
            import groovy.transform.Parallel
            import java.util.concurrent.atomic.AtomicInteger

            def counter = new AtomicInteger(0)

            @Parallel
            for (item in 1..100) {
                counter.incrementAndGet()
            }

            assert counter.get() == 100
        '''
    }

    @Test
    void testParallelForWithPool() {
        assertScript '''
            import groovy.transform.Parallel
            import groovy.concurrent.ParallelScope
            import groovy.concurrent.Pool
            import java.util.concurrent.CopyOnWriteArrayList

            def results = new CopyOnWriteArrayList()

            ParallelScope.withPool(Pool.cpu()) { scope ->
                @Parallel
                for (n in [10, 20, 30]) {
                    results << n + 1
                }
            }

            assert results.sort() == [11, 21, 31]
        '''
    }

    @Test
    void testParallelForWithRange() {
        assertScript '''
            import groovy.transform.Parallel
            import java.util.concurrent.atomic.AtomicInteger

            def sum = new AtomicInteger(0)

            @Parallel
            for (n in 1..10) {
                sum.addAndGet(n)
            }

            assert sum.get() == 55
        '''
    }

    @Test
    void testParallelForInClass() {
        assertScript '''
            import groovy.transform.Parallel
            import java.util.concurrent.CopyOnWriteArrayList

            class Worker {
                def results = new CopyOnWriteArrayList()

                void process(items) {
                    @Parallel
                    for (item in items) {
                        results << item * 2
                    }
                }
            }

            def w = new Worker()
            w.process([1, 2, 3, 4])
            assert w.results.sort() == [2, 4, 6, 8]
        '''
    }

    @Test
    void testParallelForRunsConcurrently() {
        // Single-core containers (common in CI) set the fork-join common
        // pool parallelism to 1. A concurrency assertion is meaningless in
        // that environment — skip.
        int parallelism = ForkJoinPool.commonPool().parallelism
        assumeTrue(parallelism >= 2,
                "common pool parallelism is $parallelism — skipping concurrency check")

        // Force deterministic concurrent execution via a barrier: each
        // iteration must rendezvous with another before proceeding. If the
        // @Parallel loop runs on a single thread, the first await() will
        // time out — eliminating the split-heuristic flakiness of simply
        // counting thread names over a small data set.
        assertScript '''
            import groovy.transform.Parallel
            import java.util.concurrent.CopyOnWriteArraySet
            import java.util.concurrent.CyclicBarrier
            import java.util.concurrent.TimeUnit

            int parties = 2
            def threadNames = new CopyOnWriteArraySet()
            def barrier = new CyclicBarrier(parties)

            @Parallel
            for (item in 1..parties) {
                threadNames << Thread.currentThread().name
                barrier.await(5, TimeUnit.SECONDS)
            }

            assert threadNames.size() == parties
        '''
    }
}
