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
package groovy.ginq;

import org.apache.groovy.lang.annotation.Incubating;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A deferred in-memory query. Operators return a new query and do not read the
 * source; {@link #toList()}, the aggregates, and {@link #iterator()} do.
 * {@code orderBy} followed by {@code thenBy} builds one comparison.
 * <p>
 * This is the method syntax for GINQ. The SQL-like {@code GQ { ... }} syntax
 * remains the front end that can target other providers. Extension methods on
 * this interface are the supported way to add an operator without changing GINQ.
 *
 * @param <T> the element type
 * @since 7.0.0
 */
@Incubating
public interface Query<T> {

    /**
     * Keeps elements that match {@code predicate}. Same role as LINQ {@code Where}.
     *
     * @param predicate the filter
     * @return the filtered query
     */
    Query<T> where(Predicate<? super T> predicate);

    /**
     * Maps each element. Same role as LINQ {@code Select}.
     *
     * @param selector the projection
     * @param <R> the result element type
     * @return the projected query
     */
    <R> Query<R> select(Function<? super T, ? extends R> selector);

    /**
     * Maps each element to zero or more results and flattens them.
     * Same role as LINQ {@code SelectMany}.
     *
     * @param selector the collection selector; a {@code null} collection contributes nothing
     * @param <R> the result element type
     * @return the flattened query
     */
    <R> Query<R> selectMany(Function<? super T, ? extends Iterable<? extends R>> selector);

    /**
     * Maps each element to a collection and then each pair to a result.
     *
     * @param collectionSelector the collection selector
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the flattened query
     */
    <U, R> Query<R> selectMany(Function<? super T, ? extends Iterable<? extends U>> collectionSelector,
                               BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Orders by {@code keySelector}, ascending, nulls last. Replaces any order already set.
     *
     * @param keySelector the sort key; the key is compared with its natural order when the query runs
     * @return the ordered query
     */
    Query<T> orderBy(Function<? super T, ?> keySelector);

    /**
     * Orders by {@code keySelector}, descending, nulls last. Replaces any order already set.
     *
     * @param keySelector the sort key
     * @return the ordered query
     */
    Query<T> orderByDescending(Function<? super T, ?> keySelector);

    /**
     * Adds an ascending key. Requires a preceding {@link #orderBy} or {@link #orderByDescending}
     * on this query, with no operator in between.
     *
     * @param keySelector the secondary key
     * @return the ordered query
     * @throws IllegalStateException when this query has no order
     */
    Query<T> thenBy(Function<? super T, ?> keySelector);

    /**
     * Adds a descending key. Same requirement as {@link #thenBy}.
     *
     * @param keySelector the secondary key
     * @return the ordered query
     * @throws IllegalStateException when this query has no order
     */
    Query<T> thenByDescending(Function<? super T, ?> keySelector);

    /**
     * Groups elements by key. Same role as LINQ {@code GroupBy}.
     * A {@code null} key is one group.
     *
     * @param keySelector the group key
     * @param <K> the key type
     * @return a query of groups
     */
    <K> Query<Grouping<K, T>> groupBy(Function<? super T, ? extends K> keySelector);

    /**
     * Groups by key and keeps the groups matched by {@code having}.
     *
     * @param keySelector the group key
     * @param having the group filter, or {@code null} to keep every group
     * @param <K> the key type
     * @return a query of groups
     */
    <K> Query<Grouping<K, T>> groupBy(Function<? super T, ? extends K> keySelector,
                                      Predicate<? super Grouping<K, T>> having);

    /**
     * Equijoins {@code inner} and projects each matching pair.
     * The join is a hash join, so the keys are compared with {@code equals}.
     *
     * @param inner the inner source
     * @param outerKey key from this query
     * @param innerKey key from {@code inner}
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <K> the key type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, K, R> Query<R> join(Iterable<U> inner,
                            Function<? super T, ? extends K> outerKey,
                            Function<? super U, ? extends K> innerKey,
                            BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Inner-joins {@code inner} with an arbitrary predicate and projects each matching pair.
     *
     * @param inner the inner source
     * @param on the join predicate
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, R> Query<R> join(Iterable<U> inner,
                         BiPredicate<? super T, ? super U> on,
                         BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Equijoins {@code inner} from the left. An unmatched outer element is passed
     * with a {@code null} inner element.
     *
     * @param inner the inner source
     * @param outerKey key from this query
     * @param innerKey key from {@code inner}
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <K> the key type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, K, R> Query<R> leftJoin(Iterable<U> inner,
                                Function<? super T, ? extends K> outerKey,
                                Function<? super U, ? extends K> innerKey,
                                BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Left-joins {@code inner} with an arbitrary predicate.
     *
     * @param inner the inner source
     * @param on the join predicate
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, R> Query<R> leftJoin(Iterable<U> inner,
                             BiPredicate<? super T, ? super U> on,
                             BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Equijoins {@code inner} from the right. An unmatched inner element is passed
     * with a {@code null} outer element.
     *
     * @param inner the inner source
     * @param outerKey key from this query
     * @param innerKey key from {@code inner}
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <K> the key type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, K, R> Query<R> rightJoin(Iterable<U> inner,
                                 Function<? super T, ? extends K> outerKey,
                                 Function<? super U, ? extends K> innerKey,
                                 BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Right-joins {@code inner} with an arbitrary predicate.
     *
     * @param inner the inner source
     * @param on the join predicate
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, R> Query<R> rightJoin(Iterable<U> inner,
                              BiPredicate<? super T, ? super U> on,
                              BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Full equijoin. Unmatched elements on either side are passed with {@code null}
     * for the other element.
     *
     * @param inner the inner source
     * @param outerKey key from this query
     * @param innerKey key from {@code inner}
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <K> the key type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, K, R> Query<R> fullJoin(Iterable<U> inner,
                                Function<? super T, ? extends K> outerKey,
                                Function<? super U, ? extends K> innerKey,
                                BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Full join with an arbitrary predicate.
     *
     * @param inner the inner source
     * @param on the join predicate
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, R> Query<R> fullJoin(Iterable<U> inner,
                             BiPredicate<? super T, ? super U> on,
                             BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Cross-joins {@code inner} and projects each pair.
     *
     * @param inner the inner source
     * @param resultSelector the pair projection
     * @param <U> the inner element type
     * @param <R> the result element type
     * @return the joined query
     */
    <U, R> Query<R> crossJoin(Iterable<U> inner, BiFunction<? super T, ? super U, ? extends R> resultSelector);

    /**
     * Drops duplicate elements. Equality is {@code equals}.
     *
     * @return the distinct query
     */
    Query<T> distinct();

    /**
     * Set union. Drops duplicates.
     *
     * @param other the other source
     * @return the union
     */
    Query<T> union(Iterable<? extends T> other);

    /**
     * Set union with another query. Drops duplicates.
     *
     * @param other the other query
     * @return the union
     */
    Query<T> union(Query<? extends T> other);

    /**
     * Concatenates {@code other}, keeping duplicates. Same role as LINQ {@code Concat}.
     *
     * @param other the other source
     * @return the concatenation
     */
    Query<T> unionAll(Iterable<? extends T> other);

    /**
     * Concatenates another query, keeping duplicates.
     *
     * @param other the other query
     * @return the concatenation
     */
    Query<T> unionAll(Query<? extends T> other);

    /**
     * Set intersection.
     *
     * @param other the other source
     * @return the intersection
     */
    Query<T> intersect(Iterable<? extends T> other);

    /**
     * Set intersection with another query.
     *
     * @param other the other query
     * @return the intersection
     */
    Query<T> intersect(Query<? extends T> other);

    /**
     * Set difference. Same role as LINQ {@code Except}.
     *
     * @param other the other source
     * @return the difference
     */
    Query<T> minus(Iterable<? extends T> other);

    /**
     * Set difference with another query.
     *
     * @param other the other query
     * @return the difference
     */
    Query<T> minus(Query<? extends T> other);

    /**
     * Same as {@link #minus(Iterable)}.
     *
     * @param other the other source
     * @return the difference
     */
    default Query<T> except(Iterable<? extends T> other) {
        return minus(other);
    }

    /**
     * Same as {@link #minus(Query)}.
     *
     * @param other the other query
     * @return the difference
     */
    default Query<T> except(Query<? extends T> other) {
        return minus(other);
    }

    /**
     * Same as {@link #unionAll(Iterable)}.
     *
     * @param other the other source
     * @return the concatenation
     */
    default Query<T> concat(Iterable<? extends T> other) {
        return unionAll(other);
    }

    /**
     * Same as {@link #unionAll(Query)}.
     *
     * @param other the other query
     * @return the concatenation
     */
    default Query<T> concat(Query<? extends T> other) {
        return unionAll(other);
    }

    /**
     * Drops {@code offset} elements and then keeps {@code size} elements.
     *
     * @param offset the number of elements to drop; zero keeps from the start
     * @param size the maximum number of elements to keep
     * @return the limited query
     */
    Query<T> limit(long offset, long size);

    /**
     * Keeps at most {@code size} elements.
     *
     * @param size the maximum number of elements
     * @return the limited query
     */
    default Query<T> limit(long size) {
        return limit(0, size);
    }

    /**
     * Same as {@link #limit(long)}. LINQ calls this {@code Take}.
     *
     * @param count the maximum number of elements
     * @return the limited query
     */
    default Query<T> take(long count) {
        return limit(count);
    }

    /**
     * Drops the first {@code count} elements. LINQ calls this {@code Skip}.
     *
     * @param count the number of elements to drop
     * @return the remaining query
     */
    default Query<T> skip(long count) {
        return limit(count, Long.MAX_VALUE);
    }

    /**
     * Reads the query into a list. A second call returns the same list.
     *
     * @return the elements
     */
    List<T> toList();

    /**
     * Returns a stream over the elements. Applying a terminal stream operation
     * consumes a source that cannot be replayed.
     *
     * @return the element stream
     */
    Stream<T> stream();

    /**
     * Reads the query and returns an iterator over the materialized elements.
     * {@code Query} does not extend {@link Iterable}: {@code groupBy}, {@code count},
     * and {@code sum} would otherwise clash with the Groovy extension methods of that name.
     *
     * @return the elements
     */
    Iterator<T> iterator();

    /**
     * Returns whether the query has any elements.
     *
     * @return {@code true} when there is at least one element
     */
    boolean exists();

    /**
     * Same as {@link #exists()}.
     *
     * @return {@code true} when there is at least one element
     */
    default boolean any() {
        return exists();
    }

    /**
     * Returns whether any element matches {@code predicate}.
     *
     * @param predicate the filter
     * @return {@code true} when an element matches
     */
    boolean any(Predicate<? super T> predicate);

    /**
     * Returns whether every element matches {@code predicate}. An empty query returns {@code true}.
     *
     * @param predicate the filter
     * @return {@code true} when every element matches
     */
    boolean all(Predicate<? super T> predicate);

    /**
     * Returns the number of elements.
     *
     * @return the number of elements
     */
    long count();

    /**
     * Returns the number of elements whose selected value is not {@code null}.
     *
     * @param selector the value to count
     * @param <U> the selected type
     * @return the number of non-null selected values
     */
    <U> long count(Function<? super T, ? extends U> selector);

    /**
     * Returns the sum of the selected numbers. {@code null} selections are ignored.
     *
     * @param selector the number to sum
     * @return the sum, zero when nothing was summed
     */
    BigDecimal sum(Function<? super T, ? extends Number> selector);

    /**
     * Returns the average of the selected numbers.
     *
     * @param selector the number to average
     * @return the average, or {@code null} when nothing was averaged
     */
    BigDecimal avg(Function<? super T, ? extends Number> selector);

    /**
     * Returns the minimum selected value.
     *
     * @param selector the value to compare
     * @param <U> the value type
     * @return the minimum, or {@code null} when the query is empty
     */
    <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> selector);

    /**
     * Returns the maximum selected value.
     *
     * @param selector the value to compare
     * @param <U> the value type
     * @return the maximum, or {@code null} when the query is empty
     */
    <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> selector);

    /**
     * Returns the median of the selected numbers.
     *
     * @param selector the number
     * @return the median, or {@code null} when the query is empty
     */
    BigDecimal median(Function<? super T, ? extends Number> selector);

    /**
     * Reduces the query with {@code aggregator}. Same role as {@code agg} in a GINQ query
     * and as LINQ {@code Aggregate} over the whole sequence.
     *
     * @param aggregator the reduction
     * @param <R> the result type
     * @return the aggregate value
     */
    <R> R aggregate(Aggregator<T, R> aggregator);

    /**
     * Folds the elements, starting from {@code seed}. Same role as LINQ {@code Aggregate}.
     *
     * @param seed the initial value
     * @param accumulator combines the current value with the next element
     * @param <A> the accumulator type
     * @return the final value, which is {@code seed} when the query is empty
     */
    <A> A aggregate(A seed, BiFunction<? super A, ? super T, ? extends A> accumulator);
}
