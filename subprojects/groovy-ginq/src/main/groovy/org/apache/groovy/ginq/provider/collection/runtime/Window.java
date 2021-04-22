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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from;

/**
 * Represents window which stores elements used by window functions
 *
 * @param <T> the type of {@link Window} element
 * @since 4.0.0
 */
public interface Window<T> extends Queryable<T> {
    /**
     * Factory method to create {@link Window} instance
     *
     * @param currentRecord current record
     * @param partition the partition where the window is constructed
     * @param windowDefinition window definition
     * @param <T> the type of {@link Window} element
     * @param <U> the type of field to sort
     * @return the {@link Window} instance
     * @since 4.0.0
     */
    static <T, U extends Comparable<? super U>> Window<T> of(Tuple2<T, Long> currentRecord, Partition<Tuple2<T, Long>> partition, WindowDefinition<T, U> windowDefinition) {
        final List<Order<? super T, ? extends U>> orderList = windowDefinition.orderBy();
        Order<? super T, ? extends U> order;
        if (null != orderList && 1 == orderList.size()) {
            order = orderList.get(0);
        } else {
            order = null;
        }

        List<Tuple2<T, Long>> listWithIndex = partition.toList();

        int tmpIndex = null == orderList || orderList.isEmpty()
                ? binarySearch(listWithIndex, currentRecord, comparing(Tuple2::getV2))
                : binarySearch(listWithIndex, currentRecord, WindowImpl.makeComparator(WindowImpl.composeOrders(orderList)).thenComparing(Tuple2::getV2));
        int index = tmpIndex >= 0 ? tmpIndex : -tmpIndex - 1;
        U value = null == order ? null : order.getKeyExtractor().apply(currentRecord.getV1());

        RowBound validRowBound = WindowImpl.getValidRowBound(windowDefinition, index, value, listWithIndex);
        List<T> list = null == validRowBound ? Collections.emptyList()
                : from(listWithIndex.stream().map(Tuple2::getV1).collect(Collectors.toList()))
                .limit(validRowBound.getLower(), validRowBound.getUpper() - validRowBound.getLower() + 1)
                .toList();

        return new WindowImpl<>(currentRecord, index, value, list, order);
    }

    /**
     * Returns row number in the window, similar to SQL's {@code row_number()}
     *
     * @return the row number
     * @since 4.0.0
     */
    long rowNumber();

    /**
     * Returns the next value in the window, similar to SQL's {@code lead()}
     *
     * @param extractor field extractor
     * @param <V> the type of field
     * @return the next value
     * @since 4.0.0
     */
    default <V> V lead(Function<? super T, ? extends V> extractor) {
        return lead(extractor, 1, null);
    }

    /**
     * Returns the next value in the window, similar to SQL's {@code lead()}
     *
     * @param extractor field extractor
     * @param lead the offset
     * @param <V> the type of field
     * @return the next value
     * @since 4.0.0
     */
    default <V> V lead(Function<? super T, ? extends V> extractor, long lead) {
        return lead(extractor, lead, null);
    }

    /**
     * Returns the next value by {@code lead} in the window, similar to SQL's {@code lead()}
     *
     * @param <V> the type of field
     * @param extractor field extractor
     * @param lead the offset
     * @param def the default value
     * @return the next value by {@code lead}
     * @since 4.0.0
     */
    <V> V lead(Function<? super T, ? extends V> extractor, long lead, V def);

    /**
     * Returns the previous value in the window, similar to SQL's {@code lag()}
     *
     * @param extractor field extractor
     * @param <V> the type of field
     * @return the previous value
     * @since 4.0.0
     */
    default <V> V lag(Function<? super T, ? extends V> extractor) {
        return lag(extractor, 1, null);
    }

    /**
     * Returns the previous value in the window, similar to SQL's {@code lag()}
     *
     * @param extractor field extractor
     * @param lag the offset
     * @param <V> the type of field
     * @return the previous value
     * @since 4.0.0
     */
    default <V> V lag(Function<? super T, ? extends V> extractor, long lag) {
        return lag(extractor, lag, null);
    }

    /**
     * Returns the previous value by {@code lag} in the window, similar to SQL's {@code lag()}
     *
     * @param <V> the type of field
     * @param extractor field extractor
     * @param lag the offset
     * @param def the default value
     * @return the previous value by {@code lag}
     * @since 4.0.0
     */
    <V> V lag(Function<? super T, ? extends V> extractor, long lag, V def);

    /**
     * Returns the first value in the window
     *
     * @param <V> the type of field
     * @param extractor field extractor
     * @return the first value
     * @since 4.0.0
     */
    <V> V firstValue(Function<? super T, ? extends V> extractor);

    /**
     * Returns the last value in the window
     *
     * @param <V> the type of field
     * @param extractor field extractor
     * @return the last value
     * @since 4.0.0
     */
    <V> V lastValue(Function<? super T, ? extends V> extractor);

    /**
     * Returns the nth value in the window
     *
     * @param <V> the type of field
     * @param extractor field extractor
     * @param index index for value to fetch, starting with {@code 0}
     * @return the nth value
     * @since 4.0.0
     */
    <V> V nthValue(Function<? super T, ? extends V> extractor, long index);

    /**
     * Returns the rank in the window
     *
     * @return the rank
     * @since 4.0.0
     */
    Long rank();

    /**
     * Returns the dense rank in the window
     *
     * @return the dense rank
     * @since 4.0.0
     */
    Long denseRank();

    /**
     * Returns the percent rank in the window
     *
     * @return the percent rank
     * @since 4.0.0
     */
    BigDecimal percentRank();

    /**
     * Returns the cumulative distribution of a value in the window
     *
     * @return the cumulative distribution of a value
     * @since 4.0.0
     */
    BigDecimal cumeDist();

    /**
     * Distributes rows of an ordered window into a pre-defined number of roughly equal buckets
     *
     * @param bucketCnt bucket count
     * @return bucket index starting with {@code 0}
     * @since 4.0.0
     */
    long ntile(long bucketCnt);
}
