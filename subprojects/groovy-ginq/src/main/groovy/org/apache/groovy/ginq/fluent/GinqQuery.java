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
package org.apache.groovy.ginq.fluent;

import groovy.lang.Tuple2;
import groovy.transform.Internal;
import groovy.ginq.Aggregator;
import groovy.ginq.Grouping;
import groovy.ginq.Query;
import org.apache.groovy.ginq.provider.collection.runtime.GroupResult;
import org.apache.groovy.ginq.provider.collection.runtime.Queryable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * In-memory {@link Query} backed by {@link Queryable}. Operators record work and
 * do not read the source. {@link #materialize()} runs that work, and the terminal
 * methods are what call it. Ordering keys stay on the query so {@code thenBy}
 * is one comparison. Grouping runs at that same point, not when {@code groupBy} is called.
 *
 * @param <T> the element type
 * @since 7.0.0
 */
@Internal
public class GinqQuery<T> implements Query<T> {

    private final Supplier<Queryable<T>> sourceSupplier;
    private final List<Queryable.Order<T, ?>> orders;
    private Queryable<T> resolved;

    GinqQuery(Queryable<T> ready, List<Queryable.Order<T, ?>> orders) {
        this(() -> ready, orders);
    }

    GinqQuery(Supplier<Queryable<T>> sourceSupplier, List<Queryable.Order<T, ?>> orders) {
        this.sourceSupplier = sourceSupplier;
        this.orders = orders;
    }

    /**
     * Queries {@code source}.
     *
     * @param source the elements
     * @param <T> the element type
     * @return the query
     */
    public static <T> Query<T> from(Iterable<T> source) {
        Objects.requireNonNull(source, "source");
        if (source instanceof GinqQuery<?> query) {
            @SuppressWarnings("unchecked")
            Query<T> typed = (Query<T>) query;
            return typed;
        }
        return new GinqQuery<>(() -> Queryable.from(source), null);
    }

    /**
     * Queries {@code source} in array order.
     *
     * @param source the elements
     * @param <T> the element type
     * @return the query
     */
    public static <T> Query<T> from(T[] source) {
        Objects.requireNonNull(source, "source");
        return new GinqQuery<>(() -> Queryable.from(source), null);
    }

    /**
     * Queries {@code source}.
     *
     * @param source the elements
     * @param <T> the element type
     * @return the query
     */
    public static <T> Query<T> from(Stream<T> source) {
        Objects.requireNonNull(source, "source");
        return new GinqQuery<>(() -> Queryable.from(source), null);
    }

    /**
     * Continues {@code source}.
     *
     * @param source the query
     * @param <T> the element type
     * @return {@code source}
     */
    public static <T> Query<T> from(Query<T> source) {
        Objects.requireNonNull(source, "source");
        return source;
    }

    /**
     * Queries the given elements.
     *
     * @param elements the elements
     * @param <T> the element type
     * @return the query
     */
    @SafeVarargs
    public static <T> Query<T> of(T... elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        return from(Arrays.asList(elements));
    }

    /**
     * Returns a query with no elements.
     *
     * @param <T> the element type
     * @return an empty query
     */
    public static <T> Query<T> empty() {
        return new GinqQuery<>(Queryable.emptyQueryable(), null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Queryable<T> materialize() {
        if (resolved != null) {
            return resolved;
        }
        Queryable<T> base = sourceSupplier.get();
        if (orders != null && !orders.isEmpty()) {
            Queryable.Order[] array = orders.toArray(Queryable.Order.EMPTY_ARRAY);
            base = base.orderBy(array);
        }
        resolved = base;
        return resolved;
    }

    @Override
    public Query<T> where(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return new GinqQuery<>(() -> materialize().where(predicate), null);
    }

    @Override
    public <R> Query<R> select(Function<? super T, ? extends R> selector) {
        Objects.requireNonNull(selector, "selector");
        return new GinqQuery<>(() -> materialize().select((item, rows) -> selector.apply(item)), null);
    }

    @Override
    public <R> Query<R> selectMany(Function<? super T, ? extends Iterable<? extends R>> selector) {
        Objects.requireNonNull(selector, "selector");
        return new GinqQuery<>(() -> {
            Stream<R> flattened = materialize().stream().flatMap(item -> {
                Iterable<? extends R> inner = selector.apply(item);
                if (inner == null) {
                    return Stream.<R>empty();
                }
                return StreamSupport.stream(inner.spliterator(), false).map(value -> value);
            });
            return Queryable.from(flattened);
        }, null);
    }

    @Override
    public <U, R> Query<R> selectMany(Function<? super T, ? extends Iterable<? extends U>> collectionSelector,
                                      BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(collectionSelector, "collectionSelector");
        Objects.requireNonNull(resultSelector, "resultSelector");
        return selectMany(item -> {
            Iterable<? extends U> inner = collectionSelector.apply(item);
            if (inner == null) {
                return List.of();
            }
            List<R> mapped = new ArrayList<>();
            for (U element : inner) {
                mapped.add(resultSelector.apply(item, element));
            }
            return mapped;
        });
    }

    @Override
    public Query<T> orderBy(Function<? super T, ?> keySelector) {
        return appendOrder(keySelector, true, true);
    }

    @Override
    public Query<T> orderByDescending(Function<? super T, ?> keySelector) {
        return appendOrder(keySelector, false, true);
    }

    @Override
    public Query<T> thenBy(Function<? super T, ?> keySelector) {
        requireOrder();
        return appendOrder(keySelector, true, false);
    }

    @Override
    public Query<T> thenByDescending(Function<? super T, ?> keySelector) {
        requireOrder();
        return appendOrder(keySelector, false, false);
    }

    @Override
    public <K> Query<Grouping<K, T>> groupBy(Function<? super T, ? extends K> keySelector) {
        return groupBy(keySelector, null);
    }

    @Override
    public <K> Query<Grouping<K, T>> groupBy(Function<? super T, ? extends K> keySelector,
                                             Predicate<? super Grouping<K, T>> having) {
        Objects.requireNonNull(keySelector, "keySelector");
        return new GinqQuery<>(() -> {
            Queryable<GroupResult<K, T>> grouped = materialize().groupByInto(keySelector);
            Queryable<Grouping<K, T>> wrapped = grouped.select((group, rows) -> new GinqGrouping<>(group));
            return having == null ? wrapped : wrapped.where(having);
        }, null);
    }

    @Override
    public <U, K, R> Query<R> join(Iterable<U> inner,
                                   Function<? super T, ? extends K> outerKey,
                                   Function<? super U, ? extends K> innerKey,
                                   BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        requireJoinKeys(outerKey, innerKey, resultSelector);
        return project(() -> materialize().innerHashJoin(queryableOf(inner), outerKey, innerKey), resultSelector);
    }

    @Override
    public <U, R> Query<R> join(Iterable<U> inner,
                                BiPredicate<? super T, ? super U> on,
                                BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(on, "on");
        Objects.requireNonNull(resultSelector, "resultSelector");
        return project(() -> materialize().innerJoin(queryableOf(inner), on), resultSelector);
    }

    @Override
    public <U, K, R> Query<R> leftJoin(Iterable<U> inner,
                                       Function<? super T, ? extends K> outerKey,
                                       Function<? super U, ? extends K> innerKey,
                                       BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        requireJoinKeys(outerKey, innerKey, resultSelector);
        return project(() -> materialize().leftHashJoin(queryableOf(inner), outerKey, innerKey), resultSelector);
    }

    @Override
    public <U, R> Query<R> leftJoin(Iterable<U> inner,
                                    BiPredicate<? super T, ? super U> on,
                                    BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(on, "on");
        Objects.requireNonNull(resultSelector, "resultSelector");
        return project(() -> materialize().leftJoin(queryableOf(inner), on), resultSelector);
    }

    @Override
    public <U, K, R> Query<R> rightJoin(Iterable<U> inner,
                                        Function<? super T, ? extends K> outerKey,
                                        Function<? super U, ? extends K> innerKey,
                                        BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        requireJoinKeys(outerKey, innerKey, resultSelector);
        return project(() -> materialize().rightHashJoin(queryableOf(inner), outerKey, innerKey), resultSelector);
    }

    @Override
    public <U, R> Query<R> rightJoin(Iterable<U> inner,
                                     BiPredicate<? super T, ? super U> on,
                                     BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(on, "on");
        Objects.requireNonNull(resultSelector, "resultSelector");
        return project(() -> materialize().rightJoin(queryableOf(inner), on), resultSelector);
    }

    @Override
    public <U, K, R> Query<R> fullJoin(Iterable<U> inner,
                                       Function<? super T, ? extends K> outerKey,
                                       Function<? super U, ? extends K> innerKey,
                                       BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        requireJoinKeys(outerKey, innerKey, resultSelector);
        return project(() -> materialize().fullHashJoin(queryableOf(inner), outerKey, innerKey), resultSelector);
    }

    @Override
    public <U, R> Query<R> fullJoin(Iterable<U> inner,
                                    BiPredicate<? super T, ? super U> on,
                                    BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(on, "on");
        Objects.requireNonNull(resultSelector, "resultSelector");
        return project(() -> materialize().fullJoin(queryableOf(inner), on), resultSelector);
    }

    @Override
    public <U, R> Query<R> crossJoin(Iterable<U> inner, BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        Objects.requireNonNull(resultSelector, "resultSelector");
        return project(() -> materialize().crossJoin(queryableOf(inner)), resultSelector);
    }

    @Override
    public Query<T> distinct() {
        return later(base -> base.distinct());
    }

    @Override
    public Query<T> union(Iterable<? extends T> other) {
        return later(base -> base.union(queryableOf(other)));
    }

    @Override
    public Query<T> union(Query<? extends T> other) {
        return later(base -> base.union(queryableOf(other)));
    }

    @Override
    public Query<T> unionAll(Iterable<? extends T> other) {
        return later(base -> base.unionAll(queryableOf(other)));
    }

    @Override
    public Query<T> unionAll(Query<? extends T> other) {
        return later(base -> base.unionAll(queryableOf(other)));
    }

    @Override
    public Query<T> intersect(Iterable<? extends T> other) {
        return later(base -> base.intersect(queryableOf(other)));
    }

    @Override
    public Query<T> intersect(Query<? extends T> other) {
        return later(base -> base.intersect(queryableOf(other)));
    }

    @Override
    public Query<T> minus(Iterable<? extends T> other) {
        return later(base -> base.minus(queryableOf(other)));
    }

    @Override
    public Query<T> minus(Query<? extends T> other) {
        return later(base -> base.minus(queryableOf(other)));
    }

    @Override
    public Query<T> limit(long offset, long size) {
        return later(base -> base.limit(offset, size));
    }

    @Override
    public List<T> toList() {
        return materialize().toList();
    }

    @Override
    public Stream<T> stream() {
        return materialize().stream();
    }

    public Iterator<T> iterator() {
        return toList().iterator();
    }

    @Override
    public boolean exists() {
        return materialize().exists();
    }

    @Override
    public boolean any(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return materialize().where(predicate).exists();
    }

    @Override
    public boolean all(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return !any(predicate.negate());
    }

    @Override
    public long count() {
        return materialize().count();
    }

    @Override
    public <U> long count(Function<? super T, ? extends U> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().count(selector);
    }

    @Override
    public BigDecimal sum(Function<? super T, ? extends Number> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().sum(selector);
    }

    @Override
    public BigDecimal avg(Function<? super T, ? extends Number> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().avg(selector);
    }

    @Override
    public <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().min(selector);
    }

    @Override
    public <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().max(selector);
    }

    @Override
    public BigDecimal median(Function<? super T, ? extends Number> selector) {
        Objects.requireNonNull(selector, "selector");
        return materialize().median(selector);
    }

    @Override
    public <R> R aggregate(Aggregator<T, R> aggregator) {
        Objects.requireNonNull(aggregator, "aggregator");
        return aggregator.apply(this);
    }

    @Override
    public <A> A aggregate(A seed, BiFunction<? super A, ? super T, ? extends A> accumulator) {
        Objects.requireNonNull(accumulator, "accumulator");
        A current = seed;
        for (T element : toList()) {
            current = accumulator.apply(current, element);
        }
        return current;
    }

    private Query<T> later(Function<Queryable<T>, Queryable<T>> operator) {
        return new GinqQuery<>(() -> operator.apply(materialize()), null);
    }

    private void requireOrder() {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("thenBy requires a preceding orderBy or orderByDescending");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Query<T> appendOrder(Function<? super T, ?> keySelector, boolean ascending, boolean reset) {
        Objects.requireNonNull(keySelector, "keySelector");
        List<Queryable.Order<T, ?>> next = new ArrayList<>();
        if (!reset && orders != null) {
            next.addAll(orders);
        }
        next.add(new Queryable.Order(keySelector, ascending));
        return new GinqQuery<>(sourceSupplier, Collections.unmodifiableList(next));
    }

    private <U, R> Query<R> project(Supplier<Queryable<Tuple2<T, U>>> joined,
                                    BiFunction<? super T, ? super U, ? extends R> resultSelector) {
        return new GinqQuery<>(() -> joined.get().select((pair, rows) -> resultSelector.apply(pair.getV1(), pair.getV2())), null);
    }

    private static <K> void requireJoinKeys(Function<?, ? extends K> outerKey,
                                            Function<?, ? extends K> innerKey,
                                            Object resultSelector) {
        Objects.requireNonNull(outerKey, "outerKey");
        Objects.requireNonNull(innerKey, "innerKey");
        Objects.requireNonNull(resultSelector, "resultSelector");
    }

    @SuppressWarnings("unchecked")
    private static <E> Queryable<E> queryableOf(Query<? extends E> source) {
        Objects.requireNonNull(source, "source");
        if (source instanceof GinqQuery<?> query) {
            return (Queryable<E>) query.materialize();
        }
        return Queryable.from((List<E>) source.toList());
    }

    @SuppressWarnings("unchecked")
    private static <E> Queryable<E> queryableOf(Iterable<? extends E> source) {
        Objects.requireNonNull(source, "source");
        if (source instanceof GinqQuery<?> query) {
            return (Queryable<E>) query.materialize();
        }
        if (source instanceof Queryable<?> queryable) {
            return (Queryable<E>) queryable;
        }
        return Queryable.from((Iterable<E>) source);
    }
}
