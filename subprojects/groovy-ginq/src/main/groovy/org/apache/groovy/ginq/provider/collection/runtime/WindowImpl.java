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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;

/**
 * Represents window which stores elements used by window functions
 *
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
class WindowImpl<T, U extends Comparable<? super U>> extends QueryableCollection<T> implements Window<T> {
    private static final long serialVersionUID = -3458969297047398621L;
    private final Tuple2<T, Long> currentRecord;
    private final int index;
    private final WindowDefinition<T, U> windowDefinition;
    private final U value;
    private final Function<? super T, ? extends U> keyExtractor;

    WindowImpl(Tuple2<T, Long> currentRecord, Queryable<Tuple2<T, Long>> partition, WindowDefinition<T, U> windowDefinition) {
        super(partition.orderBy(composeOrders(windowDefinition)));
        this.currentRecord = currentRecord;
        this.windowDefinition = windowDefinition;

        List<Tuple2<T, Long>> sortedList = listWithIndex;
        final List<Order<? super T, ? extends U>> order = windowDefinition.orderBy();
        if (null != order && 1 == order.size()) {
            this.keyExtractor = order.get(0).getKeyExtractor();
            this.value = keyExtractor.apply(currentRecord.getV1());
        } else {
            this.keyExtractor = null;
            this.value = null;
        }

        List<Queryable.Order<? super T, ? extends U>> orderList = windowDefinition.orderBy();
        int tmpIndex = null == orderList || orderList.isEmpty()
                        ? binarySearch(sortedList, currentRecord, comparing(Tuple2::getV2))
                        : binarySearch(sortedList, currentRecord, makeComparator(composeOrders(orderList)).thenComparing(Tuple2::getV2));

        this.index = tmpIndex >= 0 ? tmpIndex : -tmpIndex - 1;
    }

    @Override
    public long rowNumber() {
        return index;
    }

    @Override
    public <V> V lead(Function<? super T, ? extends V> extractor, long lead, V def) {
        V field;
        if (0 == lead) {
            field = extractor.apply(currentRecord.getV1());
        } else if (0 <= index + lead && index + lead < this.size()) {
            field = extractor.apply(this.toList().get(index + (int) lead));
        } else {
            field = def;
        }
        return field;
    }

    @Override
    public <V> V lag(Function<? super T, ? extends V> extractor, long lag, V def) {
        return lead(extractor, -lag, def);
    }

    @Override
    public <V> V firstValue(Function<? super T, ? extends V> extractor) {
        long lastIndex = getLastIndex();
        if (lastIndex < 0) {
            return null;
        }
        long firstIndex = getFirstIndex();
        if (firstIndex >= this.size()) {
            return null;
        }
        int resultIndex = (int) Math.max(0, firstIndex);
        return extractor.apply(this.toList().get(resultIndex));
    }

    @Override
    public <V> V lastValue(Function<? super T, ? extends V> extractor) {
        long firstIndex = getFirstIndex();
        long size = this.size();
        if (firstIndex >= size) {
            return null;
        }
        long lastIndex = getLastIndex();
        if (lastIndex < 0) {
            return null;
        }
        int resultIndex = (int) Math.min(size - 1, lastIndex);
        return extractor.apply(this.toList().get(resultIndex));
    }

    @Override
    public long rank() {
        long result = 1L;
        if (null == value || null == keyExtractor) {
            return -1;
        }
        for (T t : this.toList()) {
            U v = keyExtractor.apply(t);
            if (value.compareTo(v) > 0) {
                result++;
            }
        }
        return result;
    }

    @Override
    public long denseRank() {
        long result = 1L;
        if (null == value || null == keyExtractor) {
            return -1;
        }
        U latestV = null;
        for (T t : this.toList()) {
            U v = keyExtractor.apply(t);
            if (null != v && value.compareTo(v) > 0 && (null == latestV || v.compareTo(latestV) != 0)) {
                result++;
            }
            latestV = v;
        }
        return result;
    }

    private long getFirstIndex() {
        RowBound rowBound = windowDefinition.rows();
        long firstRowIndex;
        final Long lower = rowBound.getLower();
        if (null == lower || Long.MIN_VALUE == lower) {
            firstRowIndex = 0;
        } else {
            firstRowIndex = index + lower;
        }
        return firstRowIndex;
    }

    private long getLastIndex() {
        RowBound rowBound = windowDefinition.rows();
        long lastRowIndex;
        long size = this.size();
        final Long upper = rowBound.getUpper();
        if (null == upper || Long.MAX_VALUE == upper) {
            lastRowIndex = size - 1;
        } else {
            lastRowIndex = index + upper;
        }
        return lastRowIndex;
    }

    private static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(List<Queryable.Order<? super T, ? extends U>> orderList) {
        return orderList.stream()
                .map(order -> new Order<Tuple2<T, Long>, U>(t -> order.getKeyExtractor().apply(t.getV1()), order.isAsc()))
                .collect(Collectors.toList());
    }

    private static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(WindowDefinition<T, U> windowDefinition) {
        return composeOrders(windowDefinition.orderBy());
    }
}
