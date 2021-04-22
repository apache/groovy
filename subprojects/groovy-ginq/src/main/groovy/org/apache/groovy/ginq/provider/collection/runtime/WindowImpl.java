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
import org.apache.groovy.util.ReversedList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.binarySearch;
import static org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus.plus;
import static org.codehaus.groovy.runtime.typehandling.NumberMath.toBigDecimal;

/**
 * Represents window which stores elements used by window functions
 *
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
class WindowImpl<T, U extends Comparable<? super U>> extends QueryableCollection<T> implements Window<T> {

    WindowImpl(Tuple2<T, Long> currentRecord, int index, U value, List<T> list, Order<? super T, ? extends U> order) {
        super(list);
        this.currentRecord = currentRecord;
        this.order = order;
        this.comparator = null == order ? null : makeComparator(order);
        this.index = index;
        this.value = value;
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
        } else if (0 <= index + lead && index + lead < list.size()) {
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
        return nthValue(extractor, 0);
    }

    @Override
    public <V> V lastValue(Function<? super T, ? extends V> extractor) {
        final int size = list.size();
        if (0 == size) {
            return null;
        }
        return nthValue(extractor, size - 1);
    }

    @Override
    public <V> V nthValue(Function<? super T, ? extends V> extractor, long index) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be less than 0: " + index);
        }
        if (index > list.size() - 1) {
            return null;
        }

        return extractor.apply(list.get((int) index));
    }

    @Override
    public Long rank() {
        if (null == order) {
            return null;
        }

        long result = 1L;
        for (T t : list) {
            if (comparator.compare(currentRecord.getV1(), t) > 0) {
                result++;
            }
        }
        return result;

    }


    @Override
    public Long denseRank() {
        if (null == order) {
            return null;
        }

        long result = 1L;
        T latest = null;
        for (T t : list) {
            if (comparator.compare(currentRecord.getV1(), t) > 0 && comparator.compare(latest, t) != 0) {
                result++;
            }
            latest = t;
        }
        return result;
    }

    @Override
    public BigDecimal percentRank() {
        if (null == order) {
            return null;
        }

        final int size = list.size();
        if (1 == size) {
            return BigDecimal.ONE;
        }

        Long r = rank();
        if (null == r) {
            return null;
        }

        return toBigDecimal(r - 1).divide(toBigDecimal(size - 1), 16, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal cumeDist() {
        if (null == order) {
            return null;
        }
        long cnt = list.stream()
                        .filter(e -> comparator.compare(currentRecord.getV1(), e) >= 0)
                        .count();
        return toBigDecimal(cnt).divide(toBigDecimal(list.size()), 16, RoundingMode.HALF_UP);
    }

    @Override
    public long ntile(long bucketCnt) {
        return bucketCnt * rowNumber() / list.size();
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

    static <T, U extends Comparable<? super U>> RowBound getValidRowBound(WindowDefinition<T, U> windowDefinition, int index, U value, List<Tuple2<T, Long>> listWithIndex) {
        int size = listWithIndex.size();
        long firstIndex = 0;
        long lastIndex = size - 1;
        if (null != windowDefinition.rows() && RowBound.DEFAULT != windowDefinition.rows()) {
            firstIndex = getFirstIndex(windowDefinition, index);
            lastIndex = getLastIndex(windowDefinition, index, size);
        } else if (null != windowDefinition.range() && null != windowDefinition.orderBy()) {
            ValueBound<? extends U> valueBound = windowDefinition.range();
            U lower = valueBound.getLower();
            U upper = valueBound.getUpper();
            if (value instanceof Number && (lower instanceof Number || null == lower) && (upper instanceof Number || null == upper)) {
                final List<Order<? super T, ? extends U>> orderList = windowDefinition.orderBy();
                if (orderList.size() == 1) {
                    Order<? super T, ? extends U> order = orderList.get(0);

                    if (listWithIndex.isEmpty()) {
                        return null;
                    }
                    int flag = order.isAsc() ? 1 : -1;
                    BigDecimal firstElement = toBigDecimal((Number) order.getKeyExtractor().apply(listWithIndex.get(0).getV1()));
                    BigDecimal lastElement = toBigDecimal((Number) order.getKeyExtractor().apply(listWithIndex.get(size - 1).getV1()));

                    BigDecimal lowerValue = null == lower ? MIN_VALUE : toBigDecimal(plus((Number) value, (Number) lower));
                    BigDecimal upperValue = null == upper ? MAX_VALUE : toBigDecimal(plus((Number) value, (Number) upper));
                    if ((flag * lowerValue.compareTo(firstElement) < 0 && flag * upperValue.compareTo(firstElement) < 0)
                            || (flag * lowerValue.compareTo(lastElement) > 0 && flag * upperValue.compareTo(lastElement) > 0)) {
                        return null;
                    }

                    List<U> list =
                            listWithIndex.stream()
                                    .map(e -> order.getKeyExtractor().apply(e.getV1()))
                                    .collect(Collectors.toList());
                    if (order.isAsc()) {
                        firstIndex = getIndexByValue(lowerValue, true, list);
                        lastIndex = getIndexByValue(upperValue, false, list);
                    } else {
                        final List<U> reversedList = new ReversedList<>(list);
                        lastIndex = size - 1 - getIndexByValue(lowerValue, true, reversedList);
                        firstIndex = size - 1 - getIndexByValue(upperValue, false, reversedList);
                    }
                }
            }
        }

        if ((firstIndex < 0 && lastIndex < 0) || (firstIndex >= size && lastIndex >= size)) {
            return null;
        }
        return new RowBound(Math.max(firstIndex, 0), Math.min(lastIndex, size - 1));
    }

    private static <T, U extends Comparable<? super U>> long getIndexByValue(BigDecimal value, boolean isLower, List<U> list) {
        int tmpIndex = binarySearch(list, value, Comparator.comparing(u -> toBigDecimal((Number) u)));
        int valueIndex;
        if (tmpIndex >= 0) {
            valueIndex = tmpIndex;
        } else {
            valueIndex = -tmpIndex - 1;
            if (!isLower) {
                valueIndex = valueIndex - 1;
                if (valueIndex < 0) {
                    valueIndex = 0;
                }
            }
        }

        if (isLower) {
            int i = valueIndex - 1;
            for (; i >= 0; i--) {
                if (!value.equals(toBigDecimal((Number) list.get(i)))) {
                    break;
                }
            }
            valueIndex = i + 1;
        } else {
            int i = valueIndex + 1;
            for (int n = list.size(); i < n; i++) {
                if (!value.equals(toBigDecimal((Number) list.get(i)))) {
                    break;
                }
            }
            valueIndex = i - 1;
        }

        return valueIndex;
    }

    static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(List<Queryable.Order<? super T, ? extends U>> orderList) {
        return orderList.stream()
                .map(order -> new Order<Tuple2<T, Long>, U>(t -> order.getKeyExtractor().apply(t.getV1()), order.isAsc(), order.isNullsLast()))
                .collect(Collectors.toList());
    }

    static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(WindowDefinition<T, U> windowDefinition) {
        return composeOrders(windowDefinition.orderBy());
    }

    private final Tuple2<T, Long> currentRecord;
    private final Order<? super T, ? extends U> order;
    private final Comparator<? super T> comparator;
    private final int index;
    private final U value;
    private final List<T> list;
    private static final BigDecimal MIN_VALUE = toBigDecimal(Long.MIN_VALUE);
    private static final BigDecimal MAX_VALUE = toBigDecimal(Long.MAX_VALUE);
    private static final long serialVersionUID = -3458969297047398621L;
}
