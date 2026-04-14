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
}
