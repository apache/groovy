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
package org.codehaus.groovy.runtime;

import groovy.concurrent.Pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * DGM-like extension methods that add parallel collection operations
 * to {@link Collection}.
 * <p>
 * These methods use {@link Pool#current()} to obtain the current pool
 * (typically set by {@link groovy.concurrent.ParallelScope#withPool}).
 * If no pool is current, they fall back to {@link ForkJoinPool#commonPool()}.
 * <p>
 * All methods delegate to Java parallel streams with pool isolation:
 * the stream operations run on the current pool's {@link ForkJoinPool},
 * not the common pool.
 * <p>
 * Inspired by GPars' {@code GParsPoolUtil} category methods.
 *
 * @since 6.0.0
 */
public class ParallelCollectionExtensions {

    // ---- Iteration ------------------------------------------------------

    /**
     * Iterates over the collection in parallel, applying the action
     * to each element.
     */
    public static <T> void eachParallel(Collection<T> self, Consumer<T> action) {
        withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().forEach(action)).join()
        );
    }

    // ---- Transformation -------------------------------------------------

    /**
     * Transforms each element in parallel, returning a new list.
     */
    public static <T, R> List<R> collectParallel(Collection<T> self, Function<T, R> transform) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().map(transform).collect(Collectors.toList())).join()
        );
    }

    // ---- Filtering ------------------------------------------------------

    /**
     * Filters the collection in parallel, returning elements that match.
     */
    public static <T> List<T> findAllParallel(Collection<T> self, Predicate<T> filter) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().filter(filter).collect(Collectors.toList())).join()
        );
    }

    /**
     * Finds the first element matching the predicate in encounter order.
     * Although evaluation happens in parallel, the result is the matching
     * element with the lowest index. Use {@link #findAnyParallel} if any
     * match will do — it may be faster as it avoids ordering constraints.
     */
    public static <T> T findParallel(Collection<T> self, Predicate<T> filter) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().filter(filter).findFirst().orElse(null)).join()
        );
    }

    /**
     * Finds any element matching the predicate. May be faster than
     * {@link #findParallel} as it does not preserve encounter order.
     */
    public static <T> T findAnyParallel(Collection<T> self, Predicate<T> filter) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().filter(filter).findAny().orElse(null)).join()
        );
    }

    // ---- Predicates -----------------------------------------------------

    /**
     * Returns {@code true} if any element matches the predicate.
     */
    public static <T> boolean anyParallel(Collection<T> self, Predicate<T> predicate) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().anyMatch(predicate)).join()
        );
    }

    /**
     * Returns {@code true} if all elements match the predicate.
     */
    public static <T> boolean everyParallel(Collection<T> self, Predicate<T> predicate) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().allMatch(predicate)).join()
        );
    }

    /**
     * Counts elements matching the predicate.
     */
    public static <T> long countParallel(Collection<T> self, Predicate<T> predicate) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().filter(predicate).count()).join()
        );
    }

    // ---- Aggregation ----------------------------------------------------

    /**
     * Finds the minimum element using the given comparator.
     */
    public static <T> T minParallel(Collection<T> self, Comparator<T> comparator) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().min(comparator).orElse(null)).join()
        );
    }

    /**
     * Finds the maximum element using the given comparator.
     */
    public static <T> T maxParallel(Collection<T> self, Comparator<T> comparator) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().max(comparator).orElse(null)).join()
        );
    }

    /**
     * Reduces the collection in parallel using the given operator.
     */
    public static <T> T sumParallel(Collection<T> self, BinaryOperator<T> accumulator) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().reduce(accumulator).orElse(null)).join()
        );
    }

    /**
     * Groups elements by the classifier function in parallel.
     */
    public static <T, K> Map<K, List<T>> groupByParallel(Collection<T> self,
                                                          Function<T, K> classifier) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream()
                        .collect(Collectors.groupingBy(classifier))).join()
        );
    }

    // ---- Tier 2: additional operations ------------------------------------

    /**
     * Iterates over the collection in parallel with element indices.
     * Index assignment is based on the collection's iteration order, but
     * execution order is not guaranteed.
     */
    public static <T> void eachWithIndexParallel(Collection<T> self, BiConsumer<T, Integer> action) {
        List<T> list = self instanceof List ? (List<T>) self : new ArrayList<>(self);
        withCurrentFJP(fjp -> {
            fjp.submit(() ->
                    IntStream.range(0, list.size()).parallel()
                            .forEach(i -> action.accept(list.get(i), i))).join();
            return null;
        });
    }

    /**
     * Transforms each element into a collection and flattens the results
     * in parallel (parallel flatMap).
     */
    public static <T, R> List<R> collectManyParallel(Collection<T> self,
                                                      Function<T, ? extends Collection<R>> transform) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream()
                        .flatMap(e -> transform.apply(e).stream())
                        .collect(Collectors.toList())).join()
        );
    }

    /**
     * Partitions the collection into two lists: elements that match the
     * predicate and elements that don't.
     *
     * @return a list of two lists: [matching, non-matching]
     */
    public static <T> List<List<T>> splitParallel(Collection<T> self, Predicate<T> predicate) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> {
                    Map<Boolean, List<T>> parts = self.parallelStream()
                            .collect(Collectors.partitioningBy(predicate));
                    return List.of(parts.get(true), parts.get(false));
                }).join()
        );
    }

    /**
     * Reduces the collection in parallel with a seed value.
     * <p>
     * <b>Note:</b> the accumulator must be associative for correct parallel
     * results. Non-associative accumulators will produce undefined results.
     *
     * @param seed        the initial value
     * @param accumulator an associative reduction function
     */
    public static <T> T injectParallel(Collection<T> self, T seed, BinaryOperator<T> accumulator) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream().reduce(seed, accumulator)).join()
        );
    }

    /**
     * Filters elements using Groovy's {@code isCase} pattern matching
     * in parallel. Supports the same filter types as Groovy's {@code grep}:
     * Class, regex Pattern, Range, Collection, Closure, etc.
     *
     * @param filter the pattern to match against (uses {@code isCase})
     */
    public static <T> List<T> grepParallel(Collection<T> self, Object filter) {
        return withCurrentFJP(fjp ->
                fjp.submit(() -> self.parallelStream()
                        .filter(e -> InvokerHelper.invokeMethod(filter, "isCase", e) != Boolean.FALSE)
                        .collect(Collectors.toList())).join()
        );
    }

    // ---- Internal -------------------------------------------------------

    /**
     * Obtains the current ForkJoinPool from Pool.current() or falls back
     * to the common pool, then executes the operation on it.
     */
    private static <R> R withCurrentFJP(Function<ForkJoinPool, R> operation) {
        Pool pool = Pool.current();
        ForkJoinPool fjp;
        if (pool != null) {
            try {
                fjp = pool.asForkJoinPool();
            } catch (UnsupportedOperationException e) {
                fjp = ForkJoinPool.commonPool();
            }
        } else {
            fjp = ForkJoinPool.commonPool();
        }
        return operation.apply(fjp);
    }
}
