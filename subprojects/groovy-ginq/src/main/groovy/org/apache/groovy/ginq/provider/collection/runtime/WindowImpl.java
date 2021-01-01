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
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
class WindowImpl<T, U extends Comparable<? super U>> extends QueryableCollection<T> implements Window<T> {
    private static final long serialVersionUID = -3458969297047398621L;
    private final Tuple2<T, Long> currentRecord;
    private final Function<? super T, ? extends U> keyExtractor;
    private final int index;
    private final U value;
    private final List<T> list;

    static <T, U extends Comparable<? super U>> Window<T> newInstance(Tuple2<T, Long> currentRecord, Queryable<Tuple2<T, Long>> partition, WindowDefinition<T, U> windowDefinition) {
        Function<? super T, ? extends U> keyExtractor;
        final List<Order<? super T, ? extends U>> orderList = windowDefinition.orderBy();
        if (null != orderList && 1 == orderList.size()) {
            keyExtractor = orderList.get(0).getKeyExtractor();
        } else {
            keyExtractor = null;
        }

        List<Tuple2<T, Long>> listWithIndex = partition.orderBy(composeOrders(windowDefinition)).toList();

        int tmpIndex = null == orderList || orderList.isEmpty()
                ? binarySearch(listWithIndex, currentRecord, comparing(Tuple2::getV2))
                : binarySearch(listWithIndex, currentRecord, makeComparator(composeOrders(orderList)).thenComparing(Tuple2::getV2));
        int index = tmpIndex >= 0 ? tmpIndex : -tmpIndex - 1;

        long size = partition.size();
        RowBound validRowBound = getValidRowBound(windowDefinition, index, size);
        List<T> list = null == validRowBound ? Collections.emptyList()
                                  : from(listWithIndex.stream().map(Tuple2::getV1).collect(Collectors.toList()))
                                      .limit(validRowBound.getLower(), validRowBound.getUpper() - validRowBound.getLower() + 1)
                                      .toList();

        return new WindowImpl<>(currentRecord, index, list, keyExtractor);
    }

    private WindowImpl(Tuple2<T, Long> currentRecord, int index, List<T> list, Function<? super T, ? extends U> keyExtractor) {
        super(list);
        this.currentRecord = currentRecord;
        this.keyExtractor = keyExtractor;
        this.index = index;
        this.value = null == keyExtractor ? null : keyExtractor.apply(currentRecord.getV1());
        this.list = list;
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
            field = extractor.apply(list.get(index + (int) lead));
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
        if (list.isEmpty()) {
            return null;
        }

        return extractor.apply(list.get(0));
    }

    @Override
    public <V> V lastValue(Function<? super T, ? extends V> extractor) {
        if (list.isEmpty()) {
            return null;
        }

        return extractor.apply(list.get(list.size() - 1));
    }

    @Override
    public long rank() {
        long result = 1L;
        if (null == value || null == keyExtractor) {
            return -1;
        }
        for (T t : list) {
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
        for (T t : list) {
            U v = keyExtractor.apply(t);
            if (null != v && value.compareTo(v) > 0 && (null == latestV || v.compareTo(latestV) != 0)) {
                result++;
            }
            latestV = v;
        }
        return result;
    }

    private static <T, U extends Comparable<? super U>> long getFirstIndex(WindowDefinition<T, U> windowDefinition, int index) {
        RowBound rowBound = windowDefinition.rows();
        final Long lower = rowBound.getLower();
        return null == lower || Long.MIN_VALUE == lower ? 0 : index + lower;
    }

    private static <T, U extends Comparable<? super U>> long getLastIndex(WindowDefinition<T, U> windowDefinition, int index, long size) {
        RowBound rowBound = windowDefinition.rows();
        final Long upper = rowBound.getUpper();
        return null == upper || Long.MAX_VALUE == upper ? size - 1 : index + upper;
    }

    private static <T, U extends Comparable<? super U>> RowBound getValidRowBound(WindowDefinition<T, U> windowDefinition, int index, long size) {
        long firstIndex = getFirstIndex(windowDefinition, index);
        long lastIndex = getLastIndex(windowDefinition, index, size);
        if ((firstIndex < 0 && lastIndex < 0) || (firstIndex >= size && lastIndex >= size)) {
            return null;
        }
        return new RowBound(Math.max(firstIndex, 0), Math.min(lastIndex, size - 1));
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
