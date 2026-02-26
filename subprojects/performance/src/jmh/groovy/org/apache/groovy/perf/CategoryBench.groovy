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
package org.apache.groovy.perf

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests the performance of Groovy category usage patterns. Categories
 * are a key metaclass mechanism used heavily in Grails and other Groovy
 * frameworks: each {@code use(Category)} block temporarily modifies
 * method dispatch for the current thread.
 *
 * Every entry into and exit from a {@code use} block triggers
 * {@code invalidateSwitchPoints()}, causing global SwitchPoint
 * invalidation. In tight loops or frequently called code, this
 * creates significant overhead as all invokedynamic call sites must
 * re-link after each category scope change.
 *
 * Grails uses categories for date utilities, collection enhancements,
 * validation helpers, and domain class extensions.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class CategoryBench {
    static final int ITERATIONS = 100_000

    // Category that adds methods to String
    static class StringCategory {
        static String reverse(String self) {
            new StringBuilder(self).reverse().toString()
        }

        static String shout(String self) {
            self.toUpperCase() + '!'
        }

        static boolean isPalindrome(String self) {
            String reversed = new StringBuilder(self).reverse().toString()
            self == reversed
        }
    }

    // Category that adds methods to Integer
    static class MathCategory {
        static int doubled(Integer self) {
            self * 2
        }

        static boolean isEven(Integer self) {
            self % 2 == 0
        }

        static int factorial(Integer self) {
            (1..self).inject(1) { acc, val -> acc * val }
        }
    }

    // Category that adds methods to List
    static class CollectionCategory {
        static int sumAll(List self) {
            self.sum() ?: 0
        }

        static List doubled(List self) {
            self.collect { it * 2 }
        }
    }

    String testString
    List<Integer> testList

    @Setup(Level.Trial)
    void setup() {
        testString = "hello"
        testList = (1..10).toList()
    }

    // ===== BASELINE (no categories) =====

    /**
     * Baseline: direct method calls without any category usage.
     * Establishes the cost of normal method dispatch for comparison.
     */
    @Benchmark
    void baselineDirectCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += testString.length()
        }
        bh.consume(sum)
    }

    // ===== SINGLE CATEGORY =====

    /**
     * Single category block wrapping many calls. The category scope
     * is entered once and all calls happen inside it. This is the
     * most efficient category usage pattern - one enter/exit pair
     * for many method invocations.
     */
    @Benchmark
    void singleCategoryWrappingLoop(Blackhole bh) {
        int sum = 0
        use(StringCategory) {
            for (int i = 0; i < ITERATIONS; i++) {
                sum += testString.shout().length()
            }
        }
        bh.consume(sum)
    }

    /**
     * Category block entered on every iteration - the worst case.
     * Each iteration enters and exits the category scope, triggering
     * two SwitchPoint invalidations per iteration.
     *
     * This pattern appears in Grails when category-enhanced methods
     * are called from within request-scoped code that repeatedly
     * enters category scope.
     */
    @Benchmark
    void categoryInLoop(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            use(StringCategory) {
                sum += testString.shout().length()
            }
        }
        bh.consume(sum)
    }

    /**
     * Category enter/exit at moderate frequency - every 100 calls.
     * Simulates code where category scope is entered per-batch
     * rather than per-call.
     */
    @Benchmark
    void categoryPerBatch(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS / 100; i++) {
            use(StringCategory) {
                for (int j = 0; j < 100; j++) {
                    sum += testString.shout().length()
                }
            }
        }
        bh.consume(sum)
    }

    // ===== NESTED CATEGORIES =====

    /**
     * Nested category scopes - multiple categories active at once.
     * Each nesting level adds another enter/exit invalidation pair.
     * Grails applications often have multiple category layers active
     * simultaneously (e.g., date utilities inside collection utilities
     * inside validation helpers).
     */
    @Benchmark
    void nestedCategories(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            use(StringCategory) {
                use(MathCategory) {
                    sum += testString.shout().length() + i.doubled()
                }
            }
        }
        bh.consume(sum)
    }

    /**
     * Nested categories with the outer scope wrapping the loop.
     * Only the inner category enters/exits per iteration.
     */
    @Benchmark
    void nestedCategoryOuterWrapping(Blackhole bh) {
        int sum = 0
        use(StringCategory) {
            for (int i = 0; i < ITERATIONS; i++) {
                use(MathCategory) {
                    sum += testString.shout().length() + i.doubled()
                }
            }
        }
        bh.consume(sum)
    }

    // ===== MULTIPLE SIMULTANEOUS CATEGORIES =====

    /**
     * Multiple categories applied simultaneously via use(Cat1, Cat2).
     * Single enter/exit but with more method resolution complexity.
     */
    @Benchmark
    void multipleCategoriesSimultaneous(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            use(StringCategory, MathCategory) {
                sum += testString.shout().length() + i.doubled()
            }
        }
        bh.consume(sum)
    }

    /**
     * Three categories simultaneously - heavier resolution load.
     */
    @Benchmark
    void threeCategoriesSimultaneous(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            use(StringCategory, MathCategory, CollectionCategory) {
                sum += testString.shout().length() + i.doubled() + testList.sumAll()
            }
        }
        bh.consume(sum)
    }

    // ===== CATEGORY WITH OUTSIDE CALLS =====

    /**
     * Method calls both inside and outside category scope.
     * The outside calls exercise call sites that were invalidated
     * when the category scope was entered/exited. This measures
     * the collateral damage of category usage on non-category code.
     */
    @Benchmark
    void categoryWithOutsideCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            // Call outside category scope
            sum += testString.length()

            // Enter/exit category scope (triggers invalidation)
            use(StringCategory) {
                sum += testString.shout().length()
            }

            // Call outside again - call site was invalidated by use() above
            sum += testString.length()
        }
        bh.consume(sum)
    }

    /**
     * Baseline for category-with-outside-calls: same work without
     * the category block. Shows how much the category enter/exit
     * overhead costs for the surrounding non-category calls.
     */
    @Benchmark
    void baselineEquivalentWithoutCategory(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += testString.length()
            sum += testString.toUpperCase().length() + 1  // same work as shout()
            sum += testString.length()
        }
        bh.consume(sum)
    }

    // ===== CATEGORY METHOD RESOLUTION =====

    /**
     * Category method that shadows an existing method.
     * Tests the overhead of category method resolution when the
     * category method name matches a method already on the class.
     */
    @Benchmark
    void categoryShadowingExistingMethod(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            use(StringCategory) {
                // reverse() exists on String AND in StringCategory
                sum += testString.reverse().length()
            }
        }
        bh.consume(sum)
    }
}
