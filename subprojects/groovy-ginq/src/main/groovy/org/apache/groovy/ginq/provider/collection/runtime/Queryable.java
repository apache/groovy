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
import groovy.transform.Internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents the queryable objects, e.g. Java collections
 *
 * @param <T> the type of Queryable element
 * @since 4.0.0
 */
@Internal
public interface Queryable<T> {
    /**
     * Factory method to create {@link Queryable} instance
     *
     * @param iterable iterable object, e.g. {@link List}
     * @param <T> the type of element
     * @return the {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<T> from(Iterable<T> iterable) {
        return new QueryableCollection<>(iterable);
    }

    /**
     * Factory method to create {@link Queryable} instance
     *
     * @param array array object
     * @param <T> the type of element
     * @return the {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<T> from(T[] array) {
        return new QueryableCollection<>(Arrays.asList(array));
    }

    /**
     * Factory method to create {@link Queryable} instance
     *
     * @param sourceStream stream object
     * @param <T> the type of element
     * @return the {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<T> from(Stream<T> sourceStream) {
        return new QueryableCollection<>(sourceStream);
    }

    /**
     * Returns the original {@link Queryable} instance directly
     *
     * @param queryable queryable object
     * @param <T> the type of element
     * @return the {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<T> from(Queryable<T> queryable) {
        return queryable;
    }

    /**
     * Inner join another {@link Queryable} instance, similar to SQL's {@code inner join}
     *
     * @param queryable another {@link Queryable} instance
     * @param joiner join condition
     * @param <U> the type of element from another {@link Queryable} instance
     * @return the join result
     * @since 4.0.0
     */
    <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner);

    /**
     * Left join another {@link Queryable} instance, similar to SQL's {@code left join}
     *
     * @param queryable another {@link Queryable} instance
     * @param joiner join condition
     * @param <U> the type of element from another {@link Queryable} instance
     * @return the join result
     * @since 4.0.0
     */
    <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner);

    /**
     * Right join another {@link Queryable} instance, similar to SQL's {@code right join}
     *
     * @param queryable another {@link Queryable} instance
     * @param joiner join condition
     * @param <U> the type of element from another {@link Queryable} instance
     * @return the join result
     * @since 4.0.0
     */
    <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner);

    /**
     * Full join another {@link Queryable} instance, similar to SQL's {@code full join}
     *
     * @param queryable another {@link Queryable} instance
     * @param joiner join condition
     * @param <U> the type of element from another {@link Queryable} instance
     * @return the join result
     * @since 4.0.0
     */
    default <U> Queryable<Tuple2<T, U>> fullJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        Queryable<Tuple2<T, U>> lj = this.leftJoin(queryable, joiner);
        Queryable<Tuple2<T, U>> rj = this.rightJoin(queryable, joiner);
        return lj.union(rj);
    }

    /**
     * Cross join another {@link Queryable} instance, similar to SQL's {@code cross join}
     *
     * @param queryable another {@link Queryable} instance
     * @param <U> the type of element from another {@link Queryable} instance
     * @return the join result
     * @since 4.0.0
     */
    <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable);

    /**
     * Filter {@link Queryable} instance via some condition, similar to SQL's {@code where}
     *
     * @param filter the filter condition
     * @return filter result
     * @since 4.0.0
     */
    Queryable<T> where(Predicate<? super T> filter);

    /**
     * Group by {@link Queryable} instance, similar to SQL's {@code group by}
     *
     * @param classifier the classifier for group by
     * @param having the filter condition
     * @param <K> the type of group key
     * @return the result of group by
     * @since 4.0.0
     */
    <K> Queryable<Tuple2<K, Queryable<T>>> groupBy(Function<? super T, ? extends K> classifier, Predicate<? super Tuple2<? extends K, Queryable<? extends T>>> having);

    /**
     * Group by {@link Queryable} instance without {@code having} clause, similar to SQL's {@code group by}
     *
     * @param classifier the classifier for group by
     * @param <K> the type of group key
     * @return the result of group by
     * @since 4.0.0
     */
    default <K> Queryable<Tuple2<K, Queryable<T>>> groupBy(Function<? super T, ? extends K> classifier) {
        return groupBy(classifier, null);
    }

    /**
     * Sort {@link Queryable} instance, similar to SQL's {@code order by}
     *
     * @param orders the order rules for sorting
     * @param <U> the type of field to sort
     * @return the result of order by
     * @since 4.0.0
     */
    <U extends Comparable<? super U>> Queryable<T> orderBy(Order<? super T, ? extends U>... orders);

    /**
     * Paginate {@link Queryable} instance, similar to MySQL's {@code limit}
     *
     * @param offset the start position
     * @param size the size to take
     * @return the result of paginating
     * @since 4.0.0
     */
    Queryable<T> limit(long offset, long size);

    /**
     * Paginate {@link Queryable} instance, similar to MySQL's {@code limit}
     *
     * @param size the size to take
     * @return the result of paginating
     * @since 4.0.0
     */
    default Queryable<T> limit(long size) {
        return limit(0, size);
    }

    /**
     * Project {@link Queryable} instance, similar to SQL's {@code select}
     *
     * @param mapper project fields
     * @param <U> the type of project record
     * @return the result of projecting
     * @since 4.0.0
     */
    <U> Queryable<U> select(Function<? super T, ? extends U> mapper);

    /**
     * Eliminate duplicated records, similar to SQL's {@code distinct}
     *
     * @return the distinct result
     * @since 4.0.0
     */
    Queryable<T> distinct();

    /**
     * Union another {@link Queryable} instance, similar to SQL's {@code union}
     *
     * @param queryable the other {@link Queryable} instance
     * @return the union result
     * @since 4.0.0
     */
    default Queryable<T> union(Queryable<? extends T> queryable) {
        return this.unionAll(queryable).distinct();
    }

    /**
     * Union all another {@link Queryable} instance, similar to SQL's {@code union all}
     *
     * @param queryable the other {@link Queryable} instance
     * @return the union all result
     * @since 4.0.0
     */
    Queryable<T> unionAll(Queryable<? extends T> queryable);

    /**
     * Intersect another {@link Queryable} instance, similar to SQL's {@code intersect}
     *
     * @param queryable the other {@link Queryable} instance
     * @return the intersect result
     * @since 4.0.0
     */
    Queryable<T> intersect(Queryable<? extends T> queryable);

    /**
     * Minus another {@link Queryable} instance, similar to SQL's {@code minus}
     *
     * @param queryable the other {@link Queryable} instance
     * @return the minus result
     * @since 4.0.0
     */
    Queryable<T> minus(Queryable<? extends T> queryable);

    //  Built-in aggregate functions {
    /**
     * Aggreate function {@code count}, similar to SQL's {@code count}
     *
     * @return count result
     * @since 4.0.0
     */
    Long count();

    /**
     * Aggregate function {@code count}, similar to SQL's {@code count}
     * Note: if the chosen field is {@code null}, the field will not be counted
     *
     * @param mapper choose the field to count
     * @return count result
     * @since 4.0.0
     */
    <U> Long count(Function<? super T, ? extends U> mapper);

    /**
     * Aggregate function {@code sum}, similar to SQL's {@code sum}
     *
     * @param mapper choose the field to sum
     * @return sum result
     * @since 4.0.0
     */
    BigDecimal sum(Function<? super T, ? extends Number> mapper);

    /**
     * Aggregate function {@code min}, similar to SQL's {@code min}
     *
     * @param mapper choose the field to find the minimum
     * @param <U> the field type
     * @return min result
     * @since 4.0.0
     */
    <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> mapper);

    /**
     * Aggregate function {@code max}, similar to SQL's {@code max}
     *
     * @param mapper choose the field to find the maximum
     * @param <U> the field type
     * @return min result
     * @since 4.0.0
     */
    <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> mapper);

    /**
     * The most powerful aggregate function in GINQ, it will receive the grouped result({@link Queryable} instance) and apply any processing
     *
     * @param mapper map the grouped result({@link Queryable} instance) to aggregate result
     * @param <U> the type aggregate result
     * @return aggregate result
     * @since 4.0.0
     */
    <U> U agg(Function<? super Queryable<? extends T>, ? extends U> mapper);
    // } Built-in aggregate functions

    /**
     * Convert the {@link Queryable} instance to {@link List<T>} instance
     *
     * @return the result list
     * @since 4.0.0
     */
    List<T> toList();

    /**
     * Returns the count of elements of the {@link Queryable} instance
     *
     * @return the count of elements of the {@link Queryable} instance
     * @since 4.0.0
     */
    long size();

    /**
     * Create {@link Stream<T>} object for the {@link Queryable} instance
     *
     * @return the result stream
     * @since 4.0.0
     */
    default Stream<T> stream() {
        return toList().stream();
    }

    /**
     * Represents an order rule
     *
     * @param <T> the type of element from {@link Queryable} instance
     * @param <U> the type of field to sort
     * @since 4.0.0
     */
    class Order<T, U extends Comparable<? super U>> {
        private final Function<? super T, ? extends U> keyExtractor;
        private final boolean asc;

        public Order(Function<? super T, ? extends U> keyExtractor, boolean asc) {
            this.keyExtractor = keyExtractor;
            this.asc = asc;
        }

        public Function<? super T, ? extends U> getKeyExtractor() {
            return keyExtractor;
        }

        public boolean isAsc() {
            return asc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Order)) return false;
            Order<?, ?> order = (Order<?, ?>) o;
            return asc == order.asc &&
                    keyExtractor.equals(order.keyExtractor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyExtractor, asc);
        }
    }
}
