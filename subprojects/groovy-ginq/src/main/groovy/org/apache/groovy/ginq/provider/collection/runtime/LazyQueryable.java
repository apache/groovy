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

import groovy.lang.Tuple2;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Queryable} whose pipeline is built on first use.
 * <p>
 * Statically checked {@code GQ} expressions return one of these. The supplier
 * closes over the caller's locals and is invoked by the first terminal or
 * consuming operation, so those locals are read when the query runs rather than
 * when the expression is constructed. The built pipeline is cached, matching
 * {@link QueryableCollection#toList()}'s single materialization.
 *
 * @param <T> the element type
 * @since 7.0.0
 */
public final class LazyQueryable<T> implements Queryable<T> {

    private Supplier<? extends Queryable<? extends T>> supplier;
    private Queryable<T> delegate;

    private LazyQueryable(Supplier<? extends Queryable<? extends T>> supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns a query that calls {@code body} the first time it is used.
     * {@code captured} is copied immediately, so later changes to the caller's
     * locals are not visible. A local that the query itself writes is passed
     * in that array as a one-element holder, so the write stays visible.
     * That matches a dynamic {@code GQ} expression, which binds read-only
     * locals when the pipeline is built.
     *
     * @param captured values read at the query expression; may be null or empty
     * @param body builds the pipeline from those values; invoked at most once
     * @param <T> the element type
     * @return the deferred query
     * @throws NullPointerException when {@code body} is null
     */
    public static <T> Queryable<T> defer(Object[] captured, Function<Object[], ? extends Queryable<? extends T>> body) {
        Objects.requireNonNull(body, "body");
        Object[] snapshot = captured == null ? new Object[0] : captured.clone();
        return new LazyQueryable<>(() -> body.apply(snapshot));
    }

    @SuppressWarnings("unchecked")
    private Queryable<T> unwrap() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    Queryable<? extends T> built = supplier.get();
                    supplier = null;
                    if (built == null) {
                        throw new IllegalStateException("GINQ query supplier returned null");
                    }
                    delegate = (Queryable<T>) built;
                }
            }
        }
        return delegate;
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return unwrap().innerJoin(queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return unwrap().innerHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return unwrap().leftJoin(queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return unwrap().leftHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return unwrap().rightJoin(queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return unwrap().rightHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> fullJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return unwrap().fullJoin(queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> fullHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return unwrap().fullHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable) {
        return unwrap().crossJoin(queryable);
    }

    @Override
    public Queryable<T> where(Predicate<? super T> filter) {
        return unwrap().where(filter);
    }

    @Override
    public Queryable<Tuple2<?, Queryable<T>>> groupBy(Function<? super T, ?> classifier, Predicate<? super Tuple2<?, Queryable<? extends T>>> having) {
        return unwrap().groupBy(classifier, having);
    }

    @Override
    public <K> Queryable<GroupResult<K, T>> groupByInto(Function<? super T, ? extends K> classifier, Predicate<? super GroupResult<K, T>> having) {
        return unwrap().groupByInto(classifier, having);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends Comparable<? super U>> Queryable<T> orderBy(Queryable.Order<? super T, ? extends U>... orders) {
        return unwrap().orderBy(orders);
    }

    @Override
    public Queryable<T> limit(long offset, long size) {
        return unwrap().limit(offset, size);
    }

    @Override
    public <U> Queryable<U> select(BiFunction<? super T, ? super Queryable<? extends T>, ? extends U> mapper) {
        return unwrap().select(mapper);
    }

    @Override
    public Queryable<T> distinct() {
        return unwrap().distinct();
    }

    @Override
    public Queryable<T> unionAll(Queryable<? extends T> queryable) {
        return unwrap().unionAll(queryable);
    }

    @Override
    public Queryable<T> intersect(Queryable<? extends T> queryable) {
        return unwrap().intersect(queryable);
    }

    @Override
    public Queryable<T> minus(Queryable<? extends T> queryable) {
        return unwrap().minus(queryable);
    }

    @Override
    public Long count() {
        return unwrap().count();
    }

    @Override
    public <U> Long count(Function<? super T, ? extends U> mapper) {
        return unwrap().count(mapper);
    }

    @Override
    public BigDecimal sum(Function<? super T, ? extends Number> mapper) {
        return unwrap().sum(mapper);
    }

    @Override
    public BigDecimal avg(Function<? super T, ? extends Number> mapper) {
        return unwrap().avg(mapper);
    }

    @Override
    public <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> mapper) {
        return unwrap().min(mapper);
    }

    @Override
    public <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> mapper) {
        return unwrap().max(mapper);
    }

    @Override
    public <U> List<U> list(Function<? super T, ? extends U> mapper) {
        return unwrap().list(mapper);
    }

    @Override
    public BigDecimal median(Function<? super T, ? extends Number> mapper) {
        return unwrap().median(mapper);
    }

    @Override
    public BigDecimal stdev(Function<? super T, ? extends Number> mapper) {
        return unwrap().stdev(mapper);
    }

    @Override
    public BigDecimal stdevp(Function<? super T, ? extends Number> mapper) {
        return unwrap().stdevp(mapper);
    }

    @Override
    public BigDecimal var(Function<? super T, ? extends Number> mapper) {
        return unwrap().var(mapper);
    }

    @Override
    public BigDecimal varp(Function<? super T, ? extends Number> mapper) {
        return unwrap().varp(mapper);
    }

    @Override
    public <U> U agg(Function<? super Queryable<? extends T>, ? extends U> mapper) {
        return unwrap().agg(mapper);
    }

    @Override
    public List<T> toList() {
        return unwrap().toList();
    }

    @Override
    public long size() {
        return unwrap().size();
    }

    @Override
    public Stream<T> stream() {
        return unwrap().stream();
    }

    /**
     * Materializes and iterates. Provided so a deferred query can be used from dynamic Groovy,
     * which looks up {@code iterator()} even though {@link Queryable} does not declare it.
     *
     * @return an iterator over the materialized elements
     */
    public Iterator<T> iterator() {
        return toList().iterator();
    }

    /**
     * Same coercions as {@link QueryableCollection#asType(Class)}.
     *
     * @param type the requested type
     * @return the coerced value
     */
    public Object asType(Class<?> type) {
        if (type == LazyQueryable.class || type == Queryable.class) {
            return this;
        }
        Queryable<T> built = unwrap();
        if (built instanceof QueryableCollection<?> collection) {
            return collection.asType(type);
        }
        return built;
    }

    /**
     * Returns one materialized element.
     *
     * @param index the element index
     * @return the element at that index
     */
    public T getAt(int index) {
        return toList().get(index);
    }

    /**
     * Compares the materialized elements with another query or collection.
     *
     * @param other the object to compare
     * @return {@code true} when the materialized elements are equal
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        Queryable<T> built = unwrap();
        if (other instanceof LazyQueryable<?> lazy) {
            return built.equals(lazy.unwrap());
        }
        return built.equals(other);
    }

    /**
     * Returns the hash code of the materialized query.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return unwrap().hashCode();
    }

    /**
     * Returns the same text as the materialized query.
     *
     * @return the string form
     */
    @Override
    public String toString() {
        return String.valueOf(unwrap());
    }

    @Override
    public <U extends Comparable<? super U>> Window<T> over(Tuple2<T, Long> currentRecord, WindowDefinition<T, U> windowDefinition) {
        return unwrap().over(currentRecord, windowDefinition);
    }
}
