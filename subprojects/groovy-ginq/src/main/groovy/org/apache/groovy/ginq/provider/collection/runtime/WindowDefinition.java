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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Represents window definition, which will define the result set to construct the window
 *
 * @param <T> the type of {@link Window} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
public interface WindowDefinition<T, U extends Comparable<? super U>> {

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of() {
        return new WindowDefinitionImpl<>();
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param partitionBy partition definition
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy) {
        return new WindowDefinitionImpl<>(partitionBy);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param orderBy order definition
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Queryable.Order<? super T, ? extends U> orderBy) {
        return new WindowDefinitionImpl<T, U>(Collections.singletonList(orderBy));
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param orderBy order definition
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(List<Queryable.Order<? super T, ? extends U>> orderBy) {
        return new WindowDefinitionImpl<>(orderBy);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param partitionBy partition definition
     * @param orderBy order definition
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy, List<Queryable.Order<? super T, ? extends U>> orderBy) {
        return new WindowDefinitionImpl<>(partitionBy, orderBy);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param partitionBy partition definition
     * @param orderBy order definition
     * @param rows the window bounds
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy, List<Queryable.Order<? super T, ? extends U>> orderBy, RowBound rows) {
        return new WindowDefinitionImpl<>(partitionBy, orderBy, rows);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param orderBy order definition
     * @param rows the window bounds
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(List<Queryable.Order<? super T, ? extends U>> orderBy, RowBound rows) {
        return new WindowDefinitionImpl<>(orderBy, rows);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param partitionBy partition definition
     * @param orderBy order definition
     * @param range the window bounds
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy, List<Queryable.Order<? super T, ? extends U>> orderBy, ValueBound<? extends U> range) {
        return new WindowDefinitionImpl<>(partitionBy, orderBy, range);
    }

    /**
     * Factory method to create {@link WindowDefinition} instance
     *
     * @param orderBy order definition
     * @param range the window bounds
     * @param <T> the type of {@link Queryable} element
     * @param <U> the type of field to sort
     * @return the {@link WindowDefinition} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(List<Queryable.Order<? super T, ? extends U>> orderBy, ValueBound<? extends U> range) {
        return new WindowDefinitionImpl<>(orderBy, range);
    }

    /**
     * Define partition, similar to SQL's {@code partition by} of window definition
     *
     * @return partition definition
     */
    default Function<? super T, ?> partitionBy() {
        return (T t) -> Queryable.NULL;
    }

    /**
     * Define order, similar to SQL's {@code order by} of window definition
     *
     * @return order definition
     * @since 4.0.0
     */
    default List<Queryable.Order<? super T, ? extends U>> orderBy() {
        return Collections.emptyList();
    }

    /**
     * Define the window bounds by offsets, similar to MySQL's {@code rows between 1 preceding and 1 following } of window definition
     *
     * @return rows definition
     * @since 4.0.0
     */
    default RowBound rows() {
        return RowBound.DEFAULT;
    }

    /**
     * Define the window bounds by values, similar to MySQL's {@code range between 1.0 preceding and 1.0 following } of window definition
     *
     * @return range definition
     * @since 4.0.0
     */
    default ValueBound<? extends U> range() {
        return null;
    }

    /**
     * Get the id of window definition
     *
     * @return the id of window definition
     * @since 4.0.0
     */
    Object getId();

    /**
     * Set the id of window definition
     *
     * @param id the id of window definition
     * @return self, i.e. current {@link WindowDefinition} instance
     * @since 4.0.0
     */
    WindowDefinition<T, U> setId(Object id);
}
