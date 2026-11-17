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
package org.apache.groovy.ginq.provider.collection.runtime;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.transform.Internal;
import org.apache.groovy.internal.util.Supplier;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static groovy.lang.Tuple.tuple;
import static java.lang.Math.sqrt;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from;
import static org.apache.groovy.ginq.provider.collection.runtime.WindowImpl.composeOrders;
import static org.codehaus.groovy.runtime.typehandling.NumberMath.toBigDecimal;

/**
 * Represents the queryable collections
 *
 * @param <T> the type of Queryable element
 * @since 4.0.0
 */
@Internal
class QueryableCollection<T> implements Queryable<T>, Serializable {

    /**
     * Creates a queryable collection backed by an iterable.
     *
     * @param sourceIterable the source elements
     */
    QueryableCollection(Iterable<T> sourceIterable) {
        this.sourceIterable = sourceIterable;
    }

    /**
     * Creates a queryable collection backed by a stream.
     *
     * @param sourceStream the source stream
     */
    QueryableCollection(Stream<T> sourceStream) {
        this.sourceStream = sourceStream;
    }

    /**
     * Returns an iterator over the current source.
     *
     * @return an iterator over the elements
     */
    public Iterator<T> iterator() {
        readLock.lock();
        try {
            if (null != sourceIterable) {
                return sourceIterable.iterator();
            }

            return sourceStream.iterator();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Nested-loop inner join. For each element of this source the other source is read,
     * materializing it first when it is a {@link QueryableCollection}, and pairs that
     * {@code joiner} accepts are returned as {@link Tuple2} values.
     *
     * @param queryable the source to join with
     * @param joiner the pair predicate
     * @param <U> the element type of {@code queryable}
     * @return the matching pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p -> {
                            if (queryable instanceof QueryableCollection) {
                                ((QueryableCollection) queryable).makeReusable();
                            }

                            return queryable.stream()
                                    .filter(c -> joiner.test(p, c))
                                    .map(c -> tuple(p, c));
                        });

        return from(stream);
    }

    /**
     * Hash inner join. {@code queryable} is indexed by the hash of {@code fieldsExtractor2},
     * and each element of this source probes that table with {@code fieldsExtractor1}.
     * A pair is kept when the extracted values are equal. In parallel mode the index is
     * built before probing so the two sources are not queried in nested parallel.
     *
     * @param queryable the source to join with
     * @param fieldsExtractor1 key extracted from this source
     * @param fieldsExtractor2 key extracted from {@code queryable}
     * @param <U> the element type of {@code queryable}
     * @return the matching pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> innerHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        final ConcurrentObjectHolder<Map<Integer, List<Candidate<U>>>> hashTableHolder = new ConcurrentObjectHolder<>(createHashTableSupplier(queryable, fieldsExtractor2));
        if (isParallel()) hashTableHolder.getObject(); // avoid nested parallel querying, which results in deadlock sometimes
        Stream<Tuple2<T, U>> stream = this.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<Candidate<U>>> hashTable = hashTableHolder.getObject();

            // probe the hash table
            return probeHashTable(hashTable, p, fieldsExtractor1);
        });

        return from(stream);
    }

    private static final class Bucket<E> extends ArrayList<E> {
        @Serial private static final long serialVersionUID = 2813676753531316403L;
        Bucket(int initialCapacity) {
            super(initialCapacity);
        }
        static <E> Bucket<E> singletonBucket(E o) {
            Bucket<E> bucket = new Bucket<>(1);
            bucket.add(o);
            return bucket;
        }
    }

    private static final class Candidate<U> {
        private final U original;
        private final Object extracted;

        private Candidate(U original, Object extracted) {
            this.original = original;
            this.extracted = extracted;
        }
    }

    private static <U> Supplier<Map<Integer, List<Candidate<U>>>> createHashTableSupplier(Queryable<? extends U> queryable, Function<? super U, ?> fieldsExtractor2) {
        return () -> {
            Function<Candidate<U>, Integer> keyMapper = c -> hash(c.extracted);
            Function<Candidate<U>, List<Candidate<U>>> valueMapper = Bucket::singletonBucket;
            BinaryOperator<List<Candidate<U>>> mergeFunction = (oldBucket, newBucket) -> {
                oldBucket.addAll(newBucket);
                return oldBucket;
            };
            Collector<Candidate<U>, ?, ? extends Map<Integer, List<Candidate<U>>>> candidateMapCollector =
                    isParallel() ? Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction)
                                 : Collectors.toMap(keyMapper, valueMapper, mergeFunction);

            return queryable.stream()
                    .map(e -> new Candidate<U>(e, fieldsExtractor2.apply(e)))
                    .collect(candidateMapCollector);
        };
    }

    private static Integer hash(Object obj) {
        return Objects.hash(obj);
    }

    /**
     * Left outer join. Each element of this source is paired with the matches from
     * {@code queryable}, or with {@code null} when {@code joiner} accepts nothing.
     *
     * @param queryable the source to join with
     * @param joiner the pair predicate
     * @param <U> the element type of {@code queryable}
     * @return the left-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(this, queryable, joiner);
    }

    /**
     * Hash left outer join. Unmatched elements of this source are paired with {@code null}.
     *
     * @param queryable the source to join with
     * @param fieldsExtractor1 key extracted from this source
     * @param fieldsExtractor2 key extracted from {@code queryable}
     * @param <U> the element type of {@code queryable}
     * @return the left-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> leftHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return outerHashJoin(this, queryable, fieldsExtractor1, fieldsExtractor2);
    }

    /**
     * Right outer join. Implemented as a left outer join with the sources and the
     * joiner arguments swapped, then each pair is swapped back to (left, right).
     * An unmatched element of {@code queryable} is paired with {@code null}.
     *
     * @param queryable the source to join with
     * @param joiner the pair predicate, called as {@code (left, right)}
     * @param <U> the element type of {@code queryable}
     * @return the right-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(queryable, this, (a, b) -> joiner.test(b, a)).select((e, q) -> tuple(e.getV2(), e.getV1()));
    }

    /**
     * Hash right outer join. The extractors are swapped for the underlying left hash
     * join, then each pair is swapped back to (left, right).
     *
     * @param queryable the source to join with
     * @param fieldsExtractor1 key extracted from this source
     * @param fieldsExtractor2 key extracted from {@code queryable}
     * @param <U> the element type of {@code queryable}
     * @return the right-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> rightHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return outerHashJoin(queryable, this, fieldsExtractor2, fieldsExtractor1).select((e, q) -> tuple(e.getV2(), e.getV1()));
    }

    /**
     * Full outer join. Both sources are made reusable, then the left join and the
     * right join are combined with {@link Queryable#union}, so a matched pair is
     * kept once and an unmatched row from either side is paired with {@code null}.
     *
     * @param queryable the source to join with
     * @param joiner the pair predicate
     * @param <U> the element type of {@code queryable}
     * @return the full-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> fullJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).makeReusable();
        }
        this.makeReusable();

        Queryable<Tuple2<T, U>> lj = this.leftJoin(queryable, joiner);
        Queryable<Tuple2<T, U>> rj = this.rightJoin(queryable, joiner);
        return lj.union(rj);
    }

    /**
     * Hash full outer join. Both sources are made reusable, then the left hash join
     * and the right hash join are combined with {@link Queryable#union}.
     *
     * @param queryable the source to join with
     * @param fieldsExtractor1 key extracted from this source
     * @param fieldsExtractor2 key extracted from {@code queryable}
     * @param <U> the element type of {@code queryable}
     * @return the full-outer pairs
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> fullHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).makeReusable();
        }
        this.makeReusable();

        Queryable<Tuple2<T, U>> lj = this.leftHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
        Queryable<Tuple2<T, U>> rj = this.rightHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
        return lj.union(rj);
    }

    /**
     * Produces the Cartesian product with another queryable source.
     *
     * @param queryable the other source
     * @param <U> the element type of the other source
     * @return the joined result
     */
    @Override
    public <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p -> {
                            if (queryable instanceof QueryableCollection) {
                                ((QueryableCollection) queryable).makeReusable();
                            }

                            return queryable.stream()
                                    .map(c -> tuple(p, c));
                        });

        return from(stream);
    }

    /**
     * Keeps elements for which {@code filter} is true.
     *
     * @param filter the predicate applied to each element
     * @return the filtered queryable
     */
    @Override
    public Queryable<T> where(Predicate<? super T> filter) {
        Stream<T> stream = this.stream().filter(filter);

        return from(stream);
    }

    /**
     * Groups elements by {@code classifier}. Parallel execution uses a concurrent
     * grouping collector. When {@code having} is non-null, a group is kept only if
     * the predicate accepts the key and a queryable over that group's elements.
     * Each result row is a tuple of the key and that queryable.
     *
     * @param classifier the group key
     * @param having optional group filter, or null to keep every group
     * @return the grouped rows
     */
    @Override
    public Queryable<Tuple2<?, Queryable<T>>> groupBy(Function<? super T, ?> classifier, Predicate<? super Tuple2<?, Queryable<? extends T>>> having) {
        Collector<T, ?, ? extends Map<?, List<T>>> groupingBy =
                isParallel() ? Collectors.groupingByConcurrent(classifier, Collectors.toList())
                             : Collectors.groupingBy(classifier, Collectors.toList());

        Stream<Tuple2<?, Queryable<T>>> stream =
                this.stream()
                        .collect(groupingBy)
                        .entrySet().stream()
                        .filter(m -> null == having || having.test(tuple(m.getKey(), from(m.getValue()))))
                        .map(m -> tuple(m.getKey(), from(m.getValue())));

        return Group.of(stream);
    }

    /**
     * Groups elements by {@code classifier} into {@link GroupResult} values.
     * Each group's elements are materialized as a list so a later aggregate can
     * read the group again. When {@code having} is non-null, groups that fail
     * the predicate are dropped.
     *
     * @param classifier the group key
     * @param having optional group filter, or null to keep every group
     * @param <K> the key type
     * @return the grouped results
     */
    @Override
    public <K> Queryable<GroupResult<K, T>> groupByInto(Function<? super T, ? extends K> classifier, Predicate<? super GroupResult<K, T>> having) {
        Collector<T, ?, ? extends Map<K, List<T>>> groupingBy =
                isParallel() ? Collectors.groupingByConcurrent(classifier, Collectors.toList())
                             : Collectors.groupingBy(classifier, Collectors.toList());

        // Materialize group elements as lists so they can be iterated multiple times
        // (e.g., having g.count() > 1 followed by select g.count())
        Stream<GroupResult<K, T>> stream =
                this.stream()
                        .collect(groupingBy)
                        .entrySet().stream()
                        .map(m -> GroupResult.<K, T>of(m.getKey(), from(m.getValue())))
                        .filter(gr -> null == having || having.test(gr));

        return from(stream);
    }

    /**
     * Sorts by the supplied orders. When no orders are given, returns this queryable unchanged.
     *
     * @param orders the sort keys, first key most significant
     * @param <U> the key type
     * @return the sorted queryable, or this queryable when {@code orders} is empty
     */
    @SafeVarargs
    @Override
    public final <U extends Comparable<? super U>> Queryable<T> orderBy(Order<? super T, ? extends U>... orders) {
        Comparator<T> comparator = makeComparator(orders);
        if (null == comparator) {
            return this;
        }

        return from(this.stream().sorted(comparator));
    }

    /**
     * Builds a comparator from the listed orders.
     *
     * @param orders the sort keys
     * @param <T> the element type
     * @param <U> the key type
     * @return the comparator, or null when {@code orders} is empty
     */
    @SuppressWarnings("unchecked")
    protected static <T, U extends Comparable<? super U>> Comparator<T> makeComparator(List<? extends Order<? super T, ? extends U>> orders) {
        return makeComparator(orders.toArray(Order.EMPTY_ARRAY));
    }

    /**
     * Chains a comparator from the orders. The first order is the primary key.
     * Each key uses natural order or its reverse, and nulls sort first or last,
     * as that order specifies. Returns null when {@code orders} is null or empty.
     *
     * @param orders the sort keys
     * @param <T> the element type
     * @param <U> the key type
     * @return the chained comparator, or null when there is nothing to sort by
     */
    @SafeVarargs
    protected static <T, U extends Comparable<? super U>> Comparator<T> makeComparator(Order<? super T, ? extends U>... orders) {
        if (null == orders || 0 == orders.length) {
            return null;
        }
        Comparator<T> comparator = null;
        for (int i = 0, n = orders.length; i < n; i++) {
            Order<? super T, ? extends U> order = orders[i];
            Comparator<U> ascOrDesc = order.isAsc() ? naturalOrder() : reverseOrder();
            Comparator<U> nullsLastOrFirst = order.isNullsLast() ? nullsLast(ascOrDesc) : nullsFirst(ascOrDesc);
            comparator =
                    0 == i
                            ? Comparator.comparing(order.getKeyExtractor(), nullsLastOrFirst)
                            : comparator.thenComparing(order.getKeyExtractor(), nullsLastOrFirst);
        }
        return comparator;
    }

    /**
     * Applies offset and size limits to the current stream.
     *
     * @param offset the number of rows to skip
     * @param size the maximum number of rows to keep
     * @return the limited queryable
     */
    @Override
    public Queryable<T> limit(long offset, long size) {
        Stream<T> stream = this.stream().skip(offset).limit(size);

        return from(stream);
    }

    /**
     * Projects each element with {@code mapper}, passing the element and this queryable.
     * Parallel execution is turned off for the projection so row numbers are assigned
     * in order, then restored afterwards. When window functions are in use, or this
     * is a {@link Group}, the source is materialized first. An empty group is projected
     * as one row: the pair ({@link Queryable#NULL}, {@link Queryable#EMPTY_QUERYABLE}).
     * If the caller asked for parallel execution, the projected values are joined as
     * {@link CompletableFuture}s; an interrupt is restored, and any other failure is
     * wrapped in {@link GroovyRuntimeException}.
     *
     * @param mapper the projection, given the element and this queryable
     * @param <U> the projected type
     * @return the projected queryable
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U> Queryable<U> select(BiFunction<? super T, ? super Queryable<? extends T>, ? extends U> mapper) {
        final String originalParallel = QueryableHelper.getVar(PARALLEL);
        QueryableHelper.setVar(PARALLEL, FALSE_STR); // ensure the row number is generated sequentially
        try {
            boolean useWindowFunction = TRUE_STR.equals(QueryableHelper.getVar(USE_WINDOW_FUNCTION));
            if (useWindowFunction) {
                this.makeReusable();
            }
            Stream<U> stream = null;
            if (this instanceof Group) {
                this.makeReusable();
                if (0 == this.count()) {
                    stream = Stream.of((T) tuple(NULL, EMPTY_QUERYABLE)).map((T t) -> mapper.apply(t, this));
                }
            }
            if (null == stream) {
                stream = this.stream().map((T t) -> mapper.apply(t, this));
            }

            if (TRUE_STR.equals(originalParallel)) {
                // invoke `collect` to trigger the intermediate operator, which will create `CompletableFuture` instances
                stream = stream.collect(Collectors.toList()).parallelStream().map((U u) -> {
                    boolean interrupted = false;
                    try {
                        @SuppressWarnings("unchecked")
                        U result = (U) ((CompletableFuture) u).get();
                        return result;
                    } catch (InterruptedException | ExecutionException ex) {
                        if (ex instanceof InterruptedException) interrupted = true;
                        throw new GroovyRuntimeException(ex);
                    } finally {
                        if (interrupted) Thread.currentThread().interrupt();
                    }
                });
            }

            return from(stream);
        } finally {
            QueryableHelper.setVar(PARALLEL, originalParallel);
        }
    }

    /**
     * Removes duplicate elements.
     *
     * @return the distinct queryable
     */
    @Override
    public Queryable<T> distinct() {
        Stream<T> stream = this.stream().distinct();

        return from(stream);
    }

    /**
     * Appends all elements from another queryable.
     *
     * @param queryable the other queryable
     * @return the combined queryable
     */
    @Override
    public Queryable<T> unionAll(Queryable<? extends T> queryable) {
        Stream<T> stream = Stream.concat(this.stream(), queryable.stream());

        return from(stream);
    }

    /**
     * Keeps only elements that also occur in the other queryable.
     *
     * @param queryable the other queryable
     * @return the intersection
     */
    @Override
    public Queryable<T> intersect(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> {
            if (queryable instanceof QueryableCollection) {
                ((QueryableCollection) queryable).makeReusable();
            }

            return queryable.stream().anyMatch(b -> b.equals(a));
        }).distinct();

        return from(stream);
    }

    /**
     * Removes elements that also occur in the other queryable.
     *
     * @param queryable the other queryable
     * @return the difference
     */
    @Override
    public Queryable<T> minus(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> {
            if (queryable instanceof QueryableCollection) {
                ((QueryableCollection) queryable).makeReusable();
            }

            return queryable.stream().noneMatch(b -> b.equals(a));
        }).distinct();

        return from(stream);
    }

    // ------------------------------ BEGIN AGGREGATE FUNCTIONS --------------------------------
    /**
     * Counts all elements.
     *
     * @return the number of elements
     */
    @Override
    public Long count() {
        return agg(q -> q.stream().count());
    }

    /**
     * Counts mapped values that are not null.
     *
     * @param mapper the value to count
     * @param <U> the mapped type
     * @return how many mapped values are non-null
     */
    @Override
    public <U> Long count(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .count());
    }

    /**
     * Sums the mapped numbers as {@link BigDecimal}. A null mapped value contributes zero.
     *
     * @param mapper the number to add
     * @return the sum
     */
    @Override
    public BigDecimal sum(Function<? super T, ? extends Number> mapper) {
        return agg(q -> this.stream()
                .map(e -> {
                    Number n = mapper.apply(e);
                    if (null == n) return BigDecimal.ZERO;

                    return toBigDecimal(n);
                }).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /**
     * Averages the non-null mapped numbers. The sum is divided by how many non-null
     * values there were, at scale 16 with half-up rounding. The division throws
     * {@code ArithmeticException} when every mapped value is null.
     *
     * @param mapper the number to average
     * @return the average
     */
    @Override
    public BigDecimal avg(Function<? super T, ? extends Number> mapper) {
        Object[] result = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(NumberMath::toBigDecimal)
                .reduce(new Object[] {0L, BigDecimal.ZERO}, (r, e) -> {
                    r[0] = (Long) r[0] + 1;
                    r[1] = ((BigDecimal) r[1]).add(e);
                    return r;
                }, (o1, o2) -> o1)
        );

        return ((BigDecimal) result[1]).divide(toBigDecimal((Long) result[0]), 16, RoundingMode.HALF_UP);
    }

    /**
     * Returns the smallest non-null mapped value, or null when every mapped value is null.
     *
     * @param mapper the value to compare
     * @param <U> the mapped type
     * @return the minimum, or null
     */
    @Override
    public <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    /**
     * Returns the largest non-null mapped value, or null when every mapped value is null.
     *
     * @param mapper the value to compare
     * @param <U> the mapped type
     * @return the maximum, or null
     */
    @Override
    public <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    /**
     * Collects the non-null mapped values in encounter order.
     *
     * @param mapper the value to collect
     * @param <U> the mapped type
     * @return the non-null values
     */
    @Override
    public <U> List<U> list(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    }

    /**
     * Returns the median of the non-null mapped numbers. An empty selection returns null.
     * An odd count returns the middle value; an even count returns the mean of the two
     * middle values.
     *
     * @param mapper the number to take the median of
     * @return the median, or null when no non-null values were mapped
     */
    @Override
    public BigDecimal median(Function<? super T, ? extends Number> mapper) {
        List<BigDecimal> sortedNumList = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(NumberMath::toBigDecimal)
                .sorted()
                .collect(Collectors.toList())
        );

        int size = sortedNumList.size();
        if (0 == size) {
            return null;
        }

        int index = size / 2;
        BigDecimal num = sortedNumList.get(index);

        if (0 == size % 2) {
            return num.add(sortedNumList.get(index - 1)).divide(BD_TWO);
        }

        return num;
    }

    /**
     * Square root of {@link #var(Function)}. The root is taken in {@code double} precision.
     *
     * @param mapper the number to measure
     * @return that square root
     */
    @Override
    public BigDecimal stdev(Function<? super T, ? extends Number> mapper) {
        return sd(mapper, 0);
    }

    /**
     * Square root of {@link #varp(Function)}. The root is taken in {@code double} precision.
     *
     * @param mapper the number to measure
     * @return that square root
     */
    @Override
    public BigDecimal stdevp(Function<? super T, ? extends Number> mapper) {
        return sd(mapper, 1);
    }

    /**
     * Variance of the non-null mapped numbers: the sum of squared differences from
     * their average, divided by how many non-null values there were, at scale 16
     * with half-up rounding.
     *
     * @param mapper the number to measure
     * @return that variance
     */
    @Override
    public BigDecimal var(Function<? super T, ? extends Number> mapper) {
        return vr(mapper, 0);
    }

    /**
     * Variance of the non-null mapped numbers, dividing the sum of squared differences
     * from their average by one less than the count, at scale 16 with half-up rounding.
     *
     * @param mapper the number to measure
     * @return that variance
     */
    @Override
    public BigDecimal varp(Function<? super T, ? extends Number> mapper) {
        return vr(mapper, 1);
    }

    private BigDecimal vr(Function<? super T, ? extends Number> mapper, int diff) {
        BigDecimal avg = this.avg(mapper);
        Object[] result = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(e -> toBigDecimal(NumberNumberMinus.minus(e, avg)).pow(2))
                .reduce(new Object[]{0L, BigDecimal.ZERO}, (r, e) -> {
                    r[0] = (Long) r[0] + 1;
                    r[1] = ((BigDecimal) r[1]).add(e);
                    return r;
                }, (o1, o2) -> o1));

        return ((BigDecimal) result[1]).divide(toBigDecimal((Long) result[0] - diff), 16, RoundingMode.HALF_UP);
    }

    private BigDecimal sd(Function<? super T, ? extends Number> mapper, int diff) {
        // `BigDecimal.sqrt` is introduced since Java9, so we can not use it for now.
        return toBigDecimal(sqrt(vr(mapper, diff).doubleValue()));
    }

    /**
     * Applies {@code mapper} to this queryable and returns its result.
     *
     * @param mapper the function applied to this queryable
     * @param <U> the result type
     * @return the mapped result
     */
    @Override
    public <U> U agg(Function<? super Queryable<? extends T>, ? extends U> mapper) {
        return mapper.apply(this);
    }
    // ------------------------------ END AGGREGATE FUNCTIONS --------------------------------

    private static <T, U> Queryable<Tuple2<T, U>> outerJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                queryable1.stream()
                        .flatMap(p -> {
                            if (queryable2 instanceof QueryableCollection) {
                                ((QueryableCollection) queryable2).makeReusable();
                            }

                            List<Tuple2<T, U>> joinResultList =
                                    queryable2.stream()
                                            .filter(c -> joiner.test(p, c))
                                            .map(c -> tuple((T) p, (U) c))
                                            .collect(Collectors.toList());

                            return joinResultList.isEmpty() ? Stream.of(tuple(p, null)) : joinResultList.stream();
                        });

        return from(stream);
    }

    private static <T, U> Queryable<Tuple2<T, U>> outerHashJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        final ConcurrentObjectHolder<Map<Integer, List<Candidate<U>>>> hashTableHolder = new ConcurrentObjectHolder<>(createHashTableSupplier(queryable2, fieldsExtractor2));
        if (isParallel()) hashTableHolder.getObject(); // avoid nested parallel querying, which results in deadlock sometimes
        Stream<Tuple2<T, U>> stream = queryable1.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<Candidate<U>>> hashTable = hashTableHolder.getObject();

            // probe the hash table
            List<Tuple2<T, U>> joinResultList =
                    probeHashTable(hashTable, (T) p, fieldsExtractor1).collect(Collectors.toList());

            return joinResultList.isEmpty() ? Stream.of(tuple(p, null)) : joinResultList.stream();
        });

        return from(stream);
    }

    private static <T, U> Stream<Tuple2<T, U>> probeHashTable(Map<Integer, List<Candidate<U>>> hashTable, T p, Function<? super T, ?> fieldsExtractor1) {
        final Object otherFields = fieldsExtractor1.apply(p);
        final Integer h = hash(otherFields);

        List<Candidate<U>> candidateList = hashTable.get(h);
        if (null == candidateList) return Stream.empty();

        Stream<Candidate<U>> stream = candidateList.stream();
        if (isParallel()) stream = stream.parallel();

        return stream.filter(c -> Objects.equals(otherFields, c.extracted))
                     .map(c -> tuple(p, c.original));
    }

    /**
     * Materializes this queryable as a list.
     *
     * @return the materialized list
     */
    @Override
    public List<T> toList() {
        writeLock.lock();
        try {
            if (sourceIterable instanceof @SuppressWarnings("unchecked")List<T> list) {
                return list;
            }

            final List<T> result = stream().collect(Collectors.toList());
            sourceIterable = result;

            return result;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Counts the current elements.
     *
     * @return the number of elements
     */
    @Override
    public long size() {
        return stream().count();
    }

    /**
     * Returns a reusable stream over the current elements.
     *
     * @return the element stream
     */
    @Override
    public Stream<T> stream() {
        writeLock.lock();
        try {
            if (isReusable()) {
                sourceStream = toStream(sourceIterable);  // we have to create new stream every time because Java stream can not be reused
            }

            if (!sourceStream.isParallel() && isParallel()) {
                sourceStream = sourceStream.parallel();
            }

            return sourceStream;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Opens a window for the supplied current record.
     *
     * @param currentRecord the current record with its index
     * @param windowDefinition the window definition
     * @param <U> the order value type
     * @return the window for the current record
     */
    @Override
    public <U extends Comparable<? super U>> Window<T> over(Tuple2<T, Long> currentRecord, WindowDefinition<T, U> windowDefinition) {
        @SuppressWarnings("unchecked")
        final Tuple3<String, String, String> idTuple = (Tuple3<String, String, String>) windowDefinition.getId(); // (partitionId, orderId, windowDefinitionId)
        final String partitionId = idTuple.getV1();

        Partition<Tuple2<T, Long>> partition = partitionCache.computeIfAbsent(
                new PartitionCacheKey(windowDefinition.partitionBy().apply(currentRecord.getV1()), partitionId),
                partitionCacheKey -> from(Collections.singletonList(currentRecord)).innerHashJoin(
                        allPartitionCache.computeIfAbsent(partitionId, pid -> {
                            long[] rn = new long[]{0L};
                            List<Tuple2<T, Long>> listWithIndex =
                                    this.toList().stream()
                                            .map(e -> Tuple.tuple(e, rn[0]++))
                                            .collect(Collectors.toList());

                            final Queryable<Tuple2<?, Partition<Tuple2<T, Long>>>> q =
                                    from(listWithIndex)
                                            .groupBy(windowDefinition.partitionBy().compose(Tuple2::getV1))
                                            .select((e, x) -> Tuple.tuple(e.getV1(), Partition.of(e.getV2().toList())));
                            if (q instanceof QueryableCollection) {
                                ((QueryableCollection) q).makeReusable();
                            }
                            return q;
                        }), a -> partitionCacheKey.partitionKey, Tuple2::getV1
                ).select((e, q) -> e.getV2().getV2())
                        .stream()
                        .findFirst()
                        .orElse(Partition.emptyPartition())
        );

        final String orderId = idTuple.getV2();
        final SortedPartitionCacheKey<T> sortedPartitionCacheKey = new SortedPartitionCacheKey<>(partition, orderId);
        Partition<Tuple2<T, Long>> sortedPartition = sortedPartitionCache.computeIfAbsent(
                sortedPartitionCacheKey,
                sortedPartitionId -> Partition.of(partition.orderBy(composeOrders(windowDefinition)).toList())
        );

        return Window.of(currentRecord, sortedPartition, windowDefinition);
    }

    private static class PartitionCacheKey {
        private final Object partitionKey;
        private final String partitionId;

        PartitionCacheKey(Object partitionKey, String partitionId) {
            this.partitionKey = partitionKey;
            this.partitionId = partitionId;
        }

        /**
         * Matches keys that share the same partition value and partition id.
         *
         * @param o the object to compare
         * @return {@code true} when both the partition key and the partition id are equal
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PartitionCacheKey that)) return false;
            return partitionKey.equals(that.partitionKey) && partitionId.equals(that.partitionId);
        }

        /**
         * Hashes the partition key and the partition id.
         *
         * @return that hash
         */
        @Override
        public int hashCode() {
            return Objects.hash(partitionKey, partitionId);
        }
    }

    private static class SortedPartitionCacheKey<T> {
        private final Partition<Tuple2<T, Long>> partition;
        private final String orderId;

        SortedPartitionCacheKey(Partition<Tuple2<T, Long>> partition, String orderId) {
            this.partition = partition;
            this.orderId = orderId;
        }

        /**
         * Matches keys that refer to the same partition instance and the same order id.
         *
         * @param o the object to compare
         * @return {@code true} when the partition reference and the order id both match
         */
        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SortedPartitionCacheKey that)) return false;
            return partition == that.partition && orderId.equals(that.orderId);
        }

        /**
         * Hashes the partition's size and the order id.
         *
         * @return that hash
         */
        @Override
        public int hashCode() {
            return Objects.hash(partition.size(), orderId);
        }
    }

    private static <T> Stream<T> toStream(Iterable<T> sourceIterable) {
        return StreamSupport.stream(sourceIterable.spliterator(), isParallel());
    }

    private static boolean isParallel() {
        return QueryableHelper.isParallel();
    }

    private boolean isReusable() {
        readLock.lock();
        try {
            return null != sourceIterable;
        } finally {
            readLock.unlock();
        }
    }

    private void makeReusable() {
        if (null != this.sourceIterable) return;

        writeLock.lock();
        try {
            if (null != this.sourceIterable) return;

            this.sourceIterable = this.sourceStream.collect(Collectors.toList());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Coerces this queryable to a supported target type.
     *
     * @param clazz the requested target type
     * @return the coerced value
     */
    public Object asType(Class<?> clazz) {
        if (Queryable.class == clazz || QueryableCollection.class == clazz) {
            return this;
        }

        if (List.class == clazz || Collection.class == clazz || Iterable.class == clazz) {
            return toList();
        }
        if (ArrayList.class == clazz) {
            List<T> list = toList();
            if (list instanceof ArrayList) {
                return list;
            }
            return new ArrayList<>(list);
        }
        if (LinkedList.class == clazz || Deque.class == clazz || Queue.class == clazz) {
            List<T> list = toList();
            if (list instanceof LinkedList) {
                return list;
            }
            return new LinkedList<>(list);
        }

        if (clazz.isArray()) {
            return DefaultGroovyMethods.asType(toList(), clazz);
        }

        if (Set.class == clazz || LinkedHashSet.class == clazz) {
            return new LinkedHashSet<>(toList());
        }
        if (HashSet.class == clazz) {
            return new HashSet<>(toList());
        }
        if (TreeSet.class == clazz) {
            return new TreeSet<>(toList());
        }


        if (Stream.class == clazz) {
            return stream();
        }

        if (Iterator.class == clazz) {
            return iterator();
        }

        return DefaultGroovyMethods.asType(this, clazz);
    }

    /**
     * Compares this collection with another queryable collection.
     *
     * @param o the other object
     * @return {@code true} if both contain the same elements
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryableCollection<?> that)) return false;
        return toList().equals(that.toList());
    }

    /**
     * Returns the hash code of the materialized contents.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(toList());
    }

    /**
     * Returns the ASCII-table representation of this queryable.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return AsciiTableMaker.makeAsciiTable(this);
    }

    private final Map<String, Queryable<Tuple2<?, Partition<Tuple2<T, Long>>>>> allPartitionCache = new ConcurrentHashMap<>(4);
    private final Map<PartitionCacheKey, Partition<Tuple2<T, Long>>> partitionCache = new ConcurrentHashMap<>(4);
    private final Map<SortedPartitionCacheKey<T>, Partition<Tuple2<T, Long>>> sortedPartitionCache = new ConcurrentHashMap<>(4);
    private Stream<T> sourceStream;
    private volatile Iterable<T> sourceIterable;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private static final BigDecimal BD_TWO = BigDecimal.valueOf(2);
    private static final String USE_WINDOW_FUNCTION = "useWindowFunction";
    private static final String PARALLEL = "parallel";
    private static final String TRUE_STR = "true";
    private static final String FALSE_STR = "false";
    @Serial private static final long serialVersionUID = -5067092453136522893L;
}
