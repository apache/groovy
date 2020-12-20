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

import java.util.function.Function;

/**
 * Represents window definition, which will define the result set to construct the window
 *
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
public interface WindowDefinition<T, U extends Comparable<? super U>> {
    Tuple2<Long, Long> DEFAULT_ROWS = Tuple.tuple(Long.MIN_VALUE, Long.MAX_VALUE);

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
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy, Queryable.Order<? super T, ? extends U> orderBy) {
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
    static <T, U extends Comparable<? super U>> WindowDefinition<T, U> of(Function<? super T, ?> partitionBy, Queryable.Order<? super T, ? extends U> orderBy, Tuple2<Long, Long> rows) {
        return new WindowDefinitionImpl<>(partitionBy, orderBy, rows);
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
    Queryable.Order<? super T, ? extends U> orderBy();

    /**
     * Define the window bounds by offsets, similar to MySQL's {@code rows between 1 preceding and 1 following } of window definition
     *
     * @return rows definition
     * @since 4.0.0
     */
    default Tuple2<Long, Long> rows() {
        return DEFAULT_ROWS;
    }

    /**
     * Define the window bounds by values, similar to MySQL's {@code range between 1.0 preceding and 1.0 following } of window definition
     *
     * @return range definition
     * @since 4.0.0
     */
    default Tuple2<? extends U, ? extends U> range() {
        return null;
    }
}
