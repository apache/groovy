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

import groovy.concurrent.ParallelScope;
import groovy.concurrent.Pool;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * DGM-like extension methods that add parallel collection operations
 * to {@link Collection}.
 * <p>
 * These methods use {@link Pool#current()} to obtain the current pool
 * (typically set by {@link ParallelScope#withPool}).
 * If no pool is current, they use {@link ForkJoinPool#commonPool()}.
 * <p>
 * A fork-join pool ({@link Pool#fixed(int)}, {@link Pool#cpu()},
 * {@link ParallelScope#withPool(int, Function)}, or the common pool)
 * runs a Java parallel stream on that pool. Parallelism is a hard cap:
 * callbacks are not wrapped in {@code managedBlock}, so CPU-bound work
 * does not create spare workers, and the common pool is not enlarged.
 * A blocking callback can stall that pool when every worker blocks while
 * a sibling task is still queued. Size a fork-join pool to at least one
 * more thread than the number of concurrent blockers, or use a pool that
 * is not a fork-join pool for waiting work.
 * <p>
 * {@link Pool#virtual()}, and {@link Pool#io()} when virtual threads are
 * available, are not fork-join pools. The operation then submits one task
 * per element to that pool. Blocking callbacks stay on the caller's pool
 * instead of falling back to the common pool. Before virtual threads,
 * {@link Pool#io()} is a fixed fork-join pool and has the same cap as
 * {@link Pool#fixed(int)}. CPU-bound work should stay on {@link Pool#fixed(int)}
 * or {@link Pool#cpu()}.
 * <p>
 * A nested operation stays on the same pool. A worker created for a pool
 * carries that pool, and {@link Pool#current()} reports it when the thread
 * has no explicit binding, so a callback does not rebind the pool on each
 * element. On a bounded executor that does not create extra threads, the
 * nested operation runs on the worker already executing the outer callback.
 * That worker would otherwise wait for a task queued behind it.
 * {@link Pool#virtual()} still submits one task per element.
 * <p>
 * On the executor path, a short-circuit ({@code findParallel},
 * {@code findAnyParallel}, {@code anyParallel}, {@code everyParallel}) or a
 * sibling failure interrupts callbacks that are still running. Interrupting
 * a thread blocked on an interruptible channel closes that channel. A
 * fork-join pool does not interrupt a running callback.
 * <p>
 * A matching null element, a null reduction, and a null group key are
 * preserved on a fork-join pool and on an executor pool. An empty
 * search or reduction returns {@code null}. {@code maxParallel} skips null
 * elements, matching sequential {@code max(Comparator)}. {@code minParallel}
 * passes nulls to the comparator, matching sequential {@code min(Comparator)}.
 * <p>
 * Inspired by GPars' {@code GParsPoolUtil} category methods.
 *
 * @since 6.0.0
 */
public class ParallelCollectionExtensions {

    // Cached Collector: stateless config object; the per-call mutable ArrayList
    // is produced fresh by the Supplier inside collect(), so sharing this
    // instance across threads is safe and saves a per-call allocation.
    // toCollection(ArrayList::new) is used (rather than toList()) because the
    // JDK contract on Collectors.toList() doesn't guarantee a mutable result or
    // a concrete ArrayList type, while these GDK methods promise both.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Collector TO_LIST = Collectors.toCollection(ArrayList::new);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Collector<T, ?, List<T>> toListCollector() {
        return (Collector) TO_LIST;
    }

    // Groovy truth, as the IDENTITY closure applies it in sequential findAll(), find(), grep(),
    // any() and every().
    private static final Predicate<Object> TRUTHY = DefaultTypeTransformation::castToBoolean;

    // Adds as sequential sum() does: plus is dispatched through the left operand's metaclass.
    private static final BinaryOperator<Object> PLUS =
            (left, right) -> InvokerHelper.getMetaClass(left).invokeMethod(left, "plus", new Object[]{right});

    // Natural ordering as sequential min() and max() apply it: nulls are skipped, values compare as
    // Groovy's < and > do, and a tie keeps the earlier element.
    private static final BinaryOperator<Object> NATURAL_MIN = (left, right) -> natural(left, right, true);
    private static final BinaryOperator<Object> NATURAL_MAX = (left, right) -> natural(left, right, false);

    @SuppressWarnings("unchecked")
    private static <T> Predicate<T> truthy() {
        return (Predicate<T>) TRUTHY;
    }

    @SuppressWarnings("unchecked")
    private static <T> BinaryOperator<T> operator(BinaryOperator<Object> operator) {
        return (BinaryOperator<T>) operator;
    }

    private static Object natural(Object left, Object right, boolean min) {
        if (right == null) {
            return left;
        }
        if (left == null) {
            return right;
        }
        boolean replace = min ? ScriptBytecodeAdapter.compareLessThan(right, left)
                : ScriptBytecodeAdapter.compareGreaterThan(right, left);
        return replace ? right : left;
    }

    // ---- Iteration ------------------------------------------------------

    /**
     * Iterates over the collection in parallel, applying the action
     * to each element.
     */
    public static <T> void eachParallel(Collection<T> self, Consumer<T> action) {
        require(self, action);
        withPool((pool, fjp) -> {
            fjp.submit(() -> self.parallelStream().forEach(action)).join();
            return null;
        }, pool -> {
            ParallelExecutorTasks.forEach(pool, self, action);
            return null;
        });
    }

    // ---- Transformation -------------------------------------------------

    /**
     * Transforms each element in parallel, returning a new list.
     */
    public static <T, R> List<R> collectParallel(Collection<T> self, Function<T, R> transform) {
        require(self, transform);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().map(transform).collect(toListCollector())).join(),
                pool -> ParallelExecutorTasks.map(pool, self, transform)
        );
    }

    /**
     * Flattens a collection of collections into a list, in parallel, as sequential
     * {@code collectMany()} does. Outer encounter order is preserved, as is the order inside
     * each element collection. An element that is not a collection fails as it does
     * sequentially.
     * <pre class="language-groovy groovyTestCase">
     * assert [[1, 2], [3], []].collectManyParallel() == [1, 2, 3]
     * </pre>
     *
     * @since 6.0.1
     */
    @SuppressWarnings("unchecked")
    public static <T, E> List<T> collectManyParallel(Collection<E> self) {
        return collectManyParallel(self, element -> (Collection<T>) element);
    }

    // ---- Filtering ------------------------------------------------------

    /**
     * Filters the collection in parallel, returning elements that match.
     */
    public static <T> List<T> findAllParallel(Collection<T> self, Predicate<T> filter) {
        require(self, filter);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().filter(filter).collect(toListCollector())).join(),
                pool -> ParallelExecutorTasks.filter(pool, self, filter)
        );
    }

    /**
     * Returns the elements that are true according to Groovy truth, in parallel, as sequential
     * {@code findAll()} does. Encounter order is preserved.
     * <pre class="language-groovy groovyTestCase">
     * assert [1, 0, null, 'a', '', [2], []].findAllParallel() == [1, 'a', [2]]
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> List<T> findAllParallel(Collection<T> self) {
        return findAllParallel(self, truthy());
    }

    /**
     * Finds the first element matching the predicate in encounter order.
     * Although evaluation happens in parallel, the result is the matching
     * element with the lowest index. A matching {@code null} and no match
     * both return {@code null}. Use {@link #findAnyParallel} if any
     * match will do — it may be faster as it avoids ordering constraints.
     */
    public static <T> T findParallel(Collection<T> self, Predicate<T> filter) {
        require(self, filter);
        return withPool(
                (pool, fjp) -> findMatch(fjp, self, filter, true),
                pool -> ParallelExecutorTasks.findFirst(pool, self, filter)
        );
    }

    /**
     * Finds the first element that is true according to Groovy truth, in encounter order, as
     * sequential {@code find()} does. No match returns {@code null}.
     * <pre class="language-groovy groovyTestCase">
     * assert [null, 0, '', 42, 43].findParallel() == 42
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> T findParallel(Collection<T> self) {
        return findParallel(self, truthy());
    }

    /**
     * Finds any element matching the predicate. May be faster than
     * {@link #findParallel} as it does not preserve encounter order.
     * A matching {@code null} and no match both return {@code null}.
     */
    public static <T> T findAnyParallel(Collection<T> self, Predicate<T> filter) {
        require(self, filter);
        return withPool(
                (pool, fjp) -> findMatch(fjp, self, filter, false),
                pool -> ParallelExecutorTasks.findAny(pool, self, filter)
        );
    }

    // ---- Predicates -----------------------------------------------------

    /**
     * Returns {@code true} if any element matches the predicate.
     */
    public static <T> boolean anyParallel(Collection<T> self, Predicate<T> predicate) {
        require(self, predicate);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().anyMatch(predicate)).join(),
                pool -> ParallelExecutorTasks.anyMatch(pool, self, predicate)
        );
    }

    /**
     * Returns {@code true} if any element is true according to Groovy truth, as sequential
     * {@code any()} does.
     * <pre class="language-groovy groovyTestCase">
     * assert [0, null, 'a'].anyParallel()
     * assert ![0, null, ''].anyParallel()
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> boolean anyParallel(Collection<T> self) {
        return anyParallel(self, truthy());
    }

    /**
     * Returns {@code true} if all elements match the predicate.
     */
    public static <T> boolean everyParallel(Collection<T> self, Predicate<T> predicate) {
        require(self, predicate);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().allMatch(predicate)).join(),
                pool -> ParallelExecutorTasks.allMatch(pool, self, predicate)
        );
    }

    /**
     * Returns {@code true} if every element is true according to Groovy truth, as sequential
     * {@code every()} does. An empty collection returns {@code true}.
     * <pre class="language-groovy groovyTestCase">
     * assert [1, 'a', [2]].everyParallel()
     * assert ![1, 0].everyParallel()
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> boolean everyParallel(Collection<T> self) {
        return everyParallel(self, truthy());
    }

    /**
     * Counts elements matching the predicate.
     */
    public static <T> long countParallel(Collection<T> self, Predicate<T> predicate) {
        require(self, predicate);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().filter(predicate).count()).join(),
                pool -> ParallelExecutorTasks.count(pool, self, predicate)
        );
    }

    // ---- Aggregation ----------------------------------------------------

    /**
     * Finds the minimum element by natural ordering, as sequential {@code min()} does:
     * null elements are skipped, elements compare as Groovy's {@code <} does (so numbers of
     * different types compare by value), and the earliest of equal elements is returned.
     * An empty collection, or one of only nulls, returns {@code null}.
     * <pre class="language-groovy groovyTestCase">
     * assert [4, null, 2.5, 3G].minParallel() == 2.5
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> T minParallel(Collection<T> self) {
        Objects.requireNonNull(self, "self must not be null");
        return reduce(self, operator(NATURAL_MIN));
    }

    /**
     * Finds the minimum element using the given comparator.
     * When several elements compare equal, the earliest in encounter
     * order is returned. An empty collection returns {@code null}.
     * A one-element collection returns that element, including {@code null},
     * without calling the comparator.
     * <p>
     * Null elements are passed to the comparator, matching sequential
     * {@code min(Comparator)}. {@link #maxParallel} skips nulls instead,
     * matching sequential {@code max(Comparator)}.
     */
    public static <T> T minParallel(Collection<T> self, Comparator<T> comparator) {
        require(self, comparator);
        return withPool(
                (pool, fjp) -> fold(fjp, self, (left, right) -> ParallelExecutorTasks.pick(comparator, left, right, true)),
                pool -> ParallelExecutorTasks.min(pool, self, comparator)
        );
    }

    /**
     * Finds the element with the minimum key, as sequential {@code min} does with a
     * one-parameter closure. Keys compare as Groovy's {@code <} does, and a {@code null} key
     * is the least. When several elements have the minimum key, the earliest in encounter
     * order is returned. Each key is computed once. An empty collection returns {@code null}.
     * <p>
     * A closure or lambda with one parameter selects this method and one with two parameters
     * selects {@link #minParallel(Collection, Comparator)}, as does a method reference by the
     * number of arguments its method takes.
     * <pre class="language-groovy groovyTestCase">
     * assert ['hello', 'hi', 'hey'].minParallel { it.size() } == 'hi'
     * assert ['hello', 'hi', 'hey'].minParallel(String::size) == 'hi'
     * </pre>
     *
     * @param keyExtractor computes the key an element is compared by
     * @since 7.0.0
     */
    public static <T> T minParallel(Collection<T> self, Function<? super T, ?> keyExtractor) {
        require(self, keyExtractor);
        return extremeByKey(self, keyExtractor, true);
    }

    /**
     * Finds the maximum element by natural ordering, as sequential {@code max()} does:
     * null elements are skipped, elements compare as Groovy's {@code >} does (so numbers of
     * different types compare by value), and the earliest of equal elements is returned.
     * An empty collection, or one of only nulls, returns {@code null}.
     * <pre class="language-groovy groovyTestCase">
     * assert [4, null, 2.5, 3G].maxParallel() == 4
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> T maxParallel(Collection<T> self) {
        Objects.requireNonNull(self, "self must not be null");
        return reduce(self, operator(NATURAL_MAX));
    }

    /**
     * Finds the maximum element using the given comparator.
     * When several elements compare equal, the earliest in encounter
     * order is returned. An empty collection returns {@code null}.
     * A one-element collection returns that element, including {@code null},
     * without calling the comparator.
     * <p>
     * Null elements are skipped, matching sequential {@code max(Comparator)}.
     * A collection of only nulls returns {@code null} and does not call the
     * comparator. {@link #minParallel} passes nulls through, matching
     * sequential {@code min(Comparator)}.
     */
    public static <T> T maxParallel(Collection<T> self, Comparator<T> comparator) {
        require(self, comparator);
        return withPool(
                (pool, fjp) -> fold(fjp, self, (left, right) -> ParallelExecutorTasks.pick(comparator, left, right, false)),
                pool -> ParallelExecutorTasks.max(pool, self, comparator)
        );
    }

    /**
     * Finds the element with the maximum key, as sequential {@code max} does with a
     * one-parameter closure. Keys compare as Groovy's {@code >} does, and a {@code null} key
     * is the least, so it is chosen only when every key is {@code null}. When several elements
     * have the maximum key, the earliest in encounter order is returned. Each key is computed
     * once. An empty collection returns {@code null}.
     * <p>
     * A closure or lambda with one parameter selects this method and one with two parameters
     * selects {@link #maxParallel(Collection, Comparator)}, as does a method reference by the
     * number of arguments its method takes.
     * <pre class="language-groovy groovyTestCase">
     * assert ['hello', 'hi', 'hey'].maxParallel { it.size() } == 'hello'
     * assert ['hello', 'hi', 'hey'].maxParallel(String::size) == 'hello'
     * </pre>
     *
     * @param keyExtractor computes the key an element is compared by
     * @since 7.0.0
     */
    public static <T> T maxParallel(Collection<T> self, Function<? super T, ?> keyExtractor) {
        require(self, keyExtractor);
        return extremeByKey(self, keyExtractor, false);
    }

    /**
     * Reduces the collection in parallel using the given operator.
     * The operator must be associative. An empty collection returns {@code null}.
     * A one-element collection returns that element, including {@code null},
     * without calling the operator. A {@code null} operator result is returned
     * as {@code null}.
     */
    public static <T> T sumParallel(Collection<T> self, BinaryOperator<T> accumulator) {
        require(self, accumulator);
        return reduce(self, accumulator);
    }

    /**
     * Adds the elements together in parallel, as sequential {@code sum()} does: each addition
     * calls {@code plus} on the left operand. An empty collection returns {@code null}, and a
     * one-element collection returns that element without calling {@code plus}.
     * <p>
     * The result matches {@code sum()} only when {@code plus} is associative for the elements.
     * It is for integers, big decimals, strings and lists, but floating-point addition rounds,
     * so a parallel sum of {@code float} or {@code double} values can differ from
     * {@code sum()} in the last digits.
     * <pre class="language-groovy groovyTestCase">
     * assert [1, 2, 3, 4].sumParallel() == 10
     * assert ['a', 'b', 'c'].sumParallel() == 'abc'
     * </pre>
     *
     * @since 6.0.1
     */
    @SuppressWarnings("unchecked")
    public static Object sumParallel(Collection<?> self) {
        Objects.requireNonNull(self, "self must not be null");
        return reduce((Collection<Object>) self, PLUS);
    }

    /**
     * Adds the results of applying {@code transform} to each element, in parallel, as
     * sequential {@code sum} does with a closure: each addition calls {@code plus} on the left
     * operand. The elements are transformed and added in one pass. An empty collection returns
     * {@code null}, and a one-element collection returns its transformed value without calling
     * {@code plus}. As for {@link #sumParallel(Collection)}, the result matches the sequential
     * sum only when {@code plus} is associative for the transformed values.
     * <p>
     * A closure or lambda with one parameter selects this method and one with two parameters
     * selects {@link #sumParallel(Collection, BinaryOperator)}, as does a method reference by
     * the number of arguments its method takes.
     * <pre class="language-groovy groovyTestCase">
     * assert ['a', 'bb', 'ccc'].sumParallel { it.size() } == 6
     * assert ['a', 'bb', 'ccc'].sumParallel(String::size) == 6
     * assert [1, 2, 3].sumParallel { a, b -&gt; a * b } == 6
     * </pre>
     *
     * @param transform computes the value added for each element
     * @since 7.0.0
     */
    public static <T> Object sumParallel(Collection<T> self, Function<? super T, ?> transform) {
        require(self, transform);
        return mapReduce(self, transform, PLUS);
    }

    /**
     * Groups elements by the classifier function in parallel.
     * A {@code null} key is kept. Keys follow first-seen order, and within
     * each group elements stay in encounter order.
     */
    public static <T, K> Map<K, List<T>> groupByParallel(Collection<T> self,
                                                          Function<T, K> classifier) {
        require(self, classifier);
        return withPool((pool, fjp) -> {
            List<T> list = ParallelExecutorTasks.randomAccess(self);
            Object[] keys = new Object[list.size()];
            fjp.submit(() -> IntStream.range(0, list.size()).parallel()
                    .forEach(index -> keys[index] = classifier.apply(list.get(index)))).join();
            return ParallelExecutorTasks.groupInOrder(list, keys);
        }, pool -> ParallelExecutorTasks.groupBy(pool, self, classifier));
    }

    // ---- Tier 2: additional operations ------------------------------------

    /**
     * Iterates over the collection in parallel with element indices.
     * Index assignment is based on the collection's iteration order, but
     * execution order is not guaranteed.
     */
    public static <T> void eachWithIndexParallel(Collection<T> self, BiConsumer<T, Integer> action) {
        require(self, action);
        List<T> list = self instanceof List ? (List<T>) self : new ArrayList<>(self);
        withPool((pool, fjp) -> {
            fjp.submit(() ->
                    IntStream.range(0, list.size()).parallel()
                            .forEach(i -> action.accept(list.get(i), i))).join();
            return null;
        }, pool -> {
            ParallelExecutorTasks.forEachIndexed(pool, list, action);
            return null;
        });
    }

    /**
     * Transforms each element into a collection and flattens the results
     * in parallel (parallel flatMap). Outer encounter order is preserved,
     * as is the order inside each returned collection.
     */
    public static <T, R> List<R> collectManyParallel(Collection<T> self,
                                                      Function<T, ? extends Collection<R>> transform) {
        require(self, transform);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream()
                        .flatMap(element -> transform.apply(element).stream())
                        .collect(toListCollector())).join(),
                pool -> ParallelExecutorTasks.flatMap(pool, self, transform)
        );
    }

    /**
     * Partitions the collection into two lists: elements that match the
     * predicate and elements that don't. Both lists keep encounter order.
     *
     * @return a list of two lists: [matching, non-matching]
     */
    public static <T> List<List<T>> splitParallel(Collection<T> self, Predicate<T> predicate) {
        require(self, predicate);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> {
                    Map<Boolean, List<T>> parts = self.parallelStream()
                            .collect(Collectors.partitioningBy(predicate));
                    return List.of(parts.get(true), parts.get(false));
                }).join(),
                pool -> ParallelExecutorTasks.partition(pool, self, predicate)
        );
    }

    /**
     * Reduces the collection in parallel with an identity value.
     * <p>
     * {@code seed} is the identity of {@code accumulator}, and the
     * accumulator must be associative. This matches
     * {@code Stream.reduce(identity, accumulator)}: a parallel execution
     * may apply {@code seed} once per chunk and then combine the chunks.
     * A seed that is not an identity does not match a sequential
     * {@code inject}. An empty collection returns {@code seed}.
     *
     * @param seed        the identity value of {@code accumulator}
     * @param accumulator an associative reduction function
     */
    public static <T> T injectParallel(Collection<T> self, T seed, BinaryOperator<T> accumulator) {
        require(self, accumulator);
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().reduce(seed, accumulator)).join(),
                pool -> ParallelExecutorTasks.inject(pool, self, seed, accumulator)
        );
    }

    /**
     * Filters elements using Groovy's {@code isCase} pattern matching
     * in parallel. Supports the same filter types as Groovy's {@code grep}:
     * Class, regex Pattern, Range, Collection, Closure, etc.
     * Encounter order is preserved. A null filter is allowed and matches
     * the same elements as sequential {@code grep(null)}. An element is kept
     * when {@code isCase} is Groovy-true, not only when it returns
     * {@code Boolean.TRUE}.
     *
     * @param filter the pattern to match against (uses {@code isCase}); may be null
     */
    public static <T> List<T> grepParallel(Collection<T> self, Object filter) {
        Objects.requireNonNull(self, "self must not be null");
        Predicate<T> matches = element -> DefaultTypeTransformation.castToBoolean(
                InvokerHelper.invokeMethod(filter, "isCase", element));
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().filter(matches).collect(toListCollector())).join(),
                pool -> ParallelExecutorTasks.filter(pool, self, matches)
        );
    }

    /**
     * Returns the elements that are true according to Groovy truth, in parallel, as sequential
     * {@code grep()} does. Encounter order is preserved.
     * <pre class="language-groovy groovyTestCase">
     * assert [1, 0, null, 'a', ''].grepParallel() == [1, 'a']
     * </pre>
     *
     * @since 6.0.1
     */
    public static <T> List<T> grepParallel(Collection<T> self) {
        return findAllParallel(self, truthy());
    }

    // ---- Internal -------------------------------------------------------

    private static void require(Collection<?> self, Object callback) {
        Objects.requireNonNull(self, "self must not be null");
        Objects.requireNonNull(callback, "callback must not be null");
    }

    /** An ordered, null-preserving reduction on the current pool. */
    private static <T> T reduce(Collection<T> self, BinaryOperator<T> operator) {
        return withPool(
                (pool, fjp) -> fold(fjp, self, operator),
                pool -> ParallelExecutorTasks.reduce(pool, self, operator)
        );
    }

    /** Maps each element and reduces the results in one ordered, null-preserving pass on the current pool. */
    private static <T, R> R mapReduce(Collection<T> self, Function<? super T, ? extends R> fn, BinaryOperator<R> operator) {
        return withPool(
                (pool, fjp) -> fjp.submit(() -> self.parallelStream().<R>map(fn).collect(
                        Fold<R>::new,
                        (fold, element) -> fold.absorb(element, operator),
                        (left, right) -> left.combine(right, operator)
                ).finish()).join(),
                pool -> ParallelExecutorTasks.mapReduce(pool, self, fn, operator)
        );
    }

    /**
     * The element with the least ({@code min}) or greatest key, comparing keys as sequential
     * {@code min} and {@code max} with a one-parameter closure do. Replacing only on a strict
     * comparison keeps the earliest of equal keys in an encounter-order reduction.
     */
    private static <T> T extremeByKey(Collection<T> self, Function<? super T, ?> keyExtractor, boolean min) {
        Keyed<T> found = mapReduce(self, element -> new Keyed<>(element, keyExtractor.apply(element)),
                (left, right) -> (min ? ScriptBytecodeAdapter.compareLessThan(right.key, left.key)
                        : ScriptBytecodeAdapter.compareLessThan(left.key, right.key)) ? right : left);
        return found == null ? null : found.element;
    }

    /**
     * Runs {@code forkJoin} on the current fork-join pool, or on the common
     * pool when no pool is bound. The pool passed to {@code forkJoin} is
     * {@code null} for the common pool. A pool that is not a fork-join pool
     * runs {@code executor} on that pool; it is not redirected at the common pool.
     */
    private static <R> R withPool(BiFunction<Pool, ForkJoinPool, R> forkJoin, Function<Pool, R> executor) {
        Pool pool = Pool.current();
        if (pool == null) {
            return forkJoin.apply(null, ForkJoinPool.commonPool());
        }
        ForkJoinPool fjp = forkJoinPool(pool);
        if (fjp != null) {
            return forkJoin.apply(pool, fjp);
        }
        return executor.apply(pool);
    }

    /**
     * The pool's fork-join pool, or {@code null} when the pool is an
     * ordinary executor. Only {@link UnsupportedOperationException}
     * from {@link Pool#asForkJoinPool()} selects the executor path; every
     * other failure propagates.
     */
    private static ForkJoinPool forkJoinPool(Pool pool) {
        try {
            return pool.asForkJoinPool();
        } catch (UnsupportedOperationException notForkJoin) {
            return null;
        }
    }

    /**
     * {@code findFirst} and {@code findAny} reject a null element.
     * The box keeps a matching null distinct from an empty result until
     * the caller unwraps it. Both are {@code null} to the caller.
     */
    private static <T> T findMatch(ForkJoinPool fjp, Collection<T> self,
                                   Predicate<T> filter, boolean earliest) {
        return fjp.submit(() -> {
            Stream<Hit<T>> matches = self.parallelStream().filter(filter).map(Hit::new);
            Hit<T> found = (earliest ? matches.findFirst() : matches.findAny()).orElse(null);
            return found == null ? null : found.value;
        }).join();
    }

    /**
     * Ordered fold that can return null. {@code Stream.reduce} cannot:
     * it wraps the result in {@code Optional.of}. The first element of a
     * partition is stored without calling the operator.
     */
    private static <T> T fold(ForkJoinPool fjp, Collection<T> self, BinaryOperator<T> operator) {
        return fjp.submit(() -> self.parallelStream().collect(
                Fold<T>::new,
                (fold, element) -> fold.absorb(element, operator),
                (left, right) -> left.combine(right, operator)
        ).finish()).join();
    }

    /** An element with its computed key. */
    private static final class Keyed<T> {
        private final T element;
        private final Object key;

        private Keyed(T element, Object key) {
            this.element = element;
            this.key = key;
        }
    }

    /** A non-null holder so a matching null element survives {@code Optional.of}. */
    private static final class Hit<T> {
        private final T value;

        private Hit(T value) {
            this.value = value;
        }
    }

    /**
     * Partial reduction. An unseen fold is the identity of the combiner.
     * {@code Stream.collect} requires that even when a split does not
     * pass an empty side.
     */
    static final class Fold<T> {
        private boolean seen;
        private T value;

        void absorb(T element, BinaryOperator<T> operator) {
            if (!seen) {
                seen = true;
                value = element;
                return;
            }
            value = operator.apply(value, element);
        }

        Fold<T> combine(Fold<T> right, BinaryOperator<T> operator) {
            if (!right.seen) {
                return this;
            }
            if (!seen) {
                seen = true;
                value = right.value;
                return this;
            }
            value = operator.apply(value, right.value);
            return this;
        }

        T finish() {
            return seen ? value : null;
        }
    }
}
