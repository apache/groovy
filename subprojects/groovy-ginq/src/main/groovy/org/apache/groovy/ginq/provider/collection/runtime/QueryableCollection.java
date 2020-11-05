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

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import groovy.transform.Internal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from;

/**
 * Represents the queryable collections
 *
 * @param <T> the type of Queryable element
 * @since 4.0.0
 */
@Internal
class QueryableCollection<T> implements Queryable<T>, Serializable {
    private static final long serialVersionUID = -5067092453136522893L;
    private Iterable<T> sourceIterable;
    private Stream<T> sourceStream;

    QueryableCollection(Iterable<T> sourceIterable) {
        this.sourceIterable = sourceIterable;
    }

    QueryableCollection(Stream<T> sourceStream) {
        this.sourceStream = sourceStream;
    }

    public Iterator<T> iterator() {
        if (null != sourceIterable) {
            return sourceIterable.iterator();
        }

        return sourceStream.iterator();
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).setReusable();
        }

        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p ->
                                queryable.stream()
                                        .filter(c -> joiner.test(p, c))
                                        .map(c -> Tuple.tuple(p, c)));

        return from(stream);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).setReusable();
        }

        return outerJoin(this, queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).setReusable();
        }

        return outerJoin(queryable, this, (a, b) -> joiner.test(b, a)).select(e -> Tuple.tuple(e.getV2(), e.getV1()));
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).setReusable();
        }

        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p ->
                                queryable.stream()
                                        .map(c -> Tuple.tuple(p, c)));

        return from(stream);
    }

    @Override
    public Queryable<T> where(Predicate<? super T> filter) {
        Stream<T> stream = this.stream().filter(filter);

        return from(stream);
    }

    @Override
    public <K> Queryable<Tuple2<K, Queryable<T>>> groupBy(Function<? super T, ? extends K> classifier, Predicate<? super Tuple2<? extends K, Queryable<? extends T>>> having) {
        Stream<Tuple2<K, Queryable<T>>> stream =
                this.stream()
                        .collect(Collectors.groupingBy(classifier, Collectors.toList()))
                        .entrySet().stream()
                        .filter(m -> null == having || having.test(Tuple.tuple(m.getKey(), from(m.getValue()))))
                        .map(m -> Tuple.tuple(m.getKey(), from(m.getValue())));

        return from(stream);
    }

    @Override
    public <U extends Comparable<? super U>> Queryable<T> orderBy(Order<? super T, ? extends U>... orders) {
        Comparator<T> comparator = null;
        for (int i = 0, n = orders.length; i < n; i++) {
            Order<? super T, ? extends U> order = orders[i];
            Comparator<U> ascOrDesc = order.isAsc() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            comparator =
                    0 == i
                            ? Comparator.comparing(order.getKeyExtractor(), ascOrDesc)
                            : comparator.thenComparing(order.getKeyExtractor(), ascOrDesc);
        }

        if (null == comparator) {
            return this;
        }

        return from(this.stream().sorted(comparator));
    }

    @Override
    public Queryable<T> limit(long offset, long size) {
        Stream<T> stream = this.stream().skip(offset).limit(size);

        return from(stream);
    }

    @Override
    public <U> Queryable<U> select(Function<? super T, ? extends U> mapper) {
        Stream<U> stream = this.stream().map(mapper);

        final Queryable<U> queryable = from(stream);
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).setReusable();
        }

        return queryable;
    }

    @Override
    public Queryable<T> distinct() {
        Stream<T> stream = this.stream().distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> unionAll(Queryable<? extends T> queryable) {
        Stream<T> stream = Stream.concat(this.stream(), queryable.stream());

        return from(stream);
    }

    @Override
    public Queryable<T> intersect(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> queryable.stream().anyMatch(b -> b.equals(a))).distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> minus(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> queryable.stream().noneMatch(b -> b.equals(a))).distinct();

        return from(stream);
    }

    @Override
    public Long count() {
        return agg(q -> q.stream().count());
    }

    @Override
    public <U> Long count(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .count());
    }

    @Override
    public BigDecimal sum(Function<? super T, ? extends Number> mapper) {
        return agg(q -> this.stream()
                .map(e -> {
                    Number n = mapper.apply(e);
                    if (null == n) return BigDecimal.ZERO;

                    return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString());
                }).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Override
    public <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    @Override
    public <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    @Override
    public <U> U agg(Function<? super Queryable<? extends T>, ? extends U> mapper) {
        return mapper.apply(this);
    }

    private static <T, U> Queryable<Tuple2<T, U>> outerJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                queryable1.stream()
                        .flatMap(p ->
                                queryable2.stream()
                                        .map(c -> joiner.test(p, c) ? c : null)
                                        .reduce(new ArrayList<U>(), (r, e) -> {
                                            int size = r.size();
                                            if (0 == size) {
                                                r.add(e);
                                                return r;
                                            }

                                            int lastIndex = size - 1;
                                            Object lastElement = r.get(lastIndex);

                                            if (null != e) {
                                                if (null == lastElement) {
                                                    r.set(lastIndex, e);
                                                } else {
                                                    r.add(e);
                                                }
                                            }

                                            return r;
                                        }, (i, o) -> o).stream()
                                        .map(c -> null == c ? Tuple.tuple(p, null) : Tuple.tuple(p, c)));

        return from(stream);
    }


    @Override
    public List<T> toList() {
        if (sourceIterable instanceof List) {
            return (List<T>) sourceIterable;
        }

        final List<T> result = stream().collect(Collectors.toList());
        sourceIterable = result;

        return result;
    }

    @Override
    public long size() {
        return stream().count();
    }

    @Override
    public Stream<T> stream() {
        if (isReusable()) {
            return toStream(sourceIterable);  // we have to create new stream every time because Java stream can not be reused
        }

        return sourceStream;
    }

    private static <T> Stream<T> toStream(Iterable<T> sourceIterable) {
        return StreamSupport.stream(sourceIterable.spliterator(), false);
    }

    private static <T> Iterable<T> toIterable(Stream<T> sourceStream) {
        return sourceStream.collect(Collectors.toList());
    }

    private boolean isReusable() {
        return null != sourceIterable;
    }

    private void setReusable() {
        if (null != this.sourceIterable) return;

        this.sourceIterable = toIterable(this.sourceStream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryableCollection)) return false;
        QueryableCollection<?> that = (QueryableCollection<?>) o;
        return toList().equals(that.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toList());
    }

    @Override
    public String toString() {
        return AsciiTableMaker.makeAsciiTable(this);
    }
}
