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

import java.io.Serial;
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
 * @since 4.0.0
 */
class WindowImpl<T> extends QueryableCollection<T> implements Window<T> {

    /**
     * Creates a window for the current record.
     *
     * @param currentRecord the current record and row index
     * @param index the current zero-based row index
     * @param list the frame contents, the rows aggregates and value functions see
     * @param comparator the full window order, or {@code null} when the window is unordered.
     *        Ranking uses every key. A single key is not required
     * @param orderedPartition the ordered partition. {@code lead}, {@code lag}, {@code rowNumber}
     *        and {@code ntile} address this list, not the frame
     */
    WindowImpl(Tuple2<T, Long> currentRecord, int index, List<T> list,
               Comparator<? super T> comparator, List<T> orderedPartition) {
        super(list);
        this.currentRecord = currentRecord;
        this.comparator = comparator;
        this.index = index;
        this.list = list;
        this.orderedPartition = orderedPartition;
    }

    /**
     * Returns the current row number inside the partition.
     *
     * @return the current row number
     */
    @Override
    public long rowNumber() {
        return index;
    }

    @Override
    public <V> V lead(Function<? super T, ? extends V> extractor, long lead, V def) {
        if (0L == lead) {
            return extractor.apply(currentRecord.getV1());
        }
        // The offset is within the ordered partition. The frame list is shorter
        // and is not indexed by the partition position.
        long target = (long) index + lead;
        if (0L <= target && target < orderedPartition.size()) {
            return extractor.apply(orderedPartition.get((int) target));
        }
        return def;
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
        return nthValue(extractor, size - 1L);
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

    /**
     * Returns the rank of the current row.
     *
     * @return the rank, or {@code null} when no ordering exists
     */
    @Override
    public Long rank() {
        if (null == comparator) {
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


    /**
     * Returns the dense rank of the current row.
     *
     * @return the dense rank, or {@code null} when no ordering exists
     */
    @Override
    public Long denseRank() {
        if (null == comparator) {
            return null;
        }

        long result = 1L;
        // The window list is ordered, so a new preceding key differs from the
        // adjacent row. The sentinel must not go through the key extractor:
        // `orderby e.prop` would dereference a null row.
        T previous = null;
        for (T t : list) {
            if (comparator.compare(currentRecord.getV1(), t) > 0
                    && (null == previous || comparator.compare(previous, t) != 0)) {
                result++;
            }
            previous = t;
        }
        return result;
    }

    /**
     * Returns the percent rank of the current row.
     *
     * @return the percent rank, or {@code null} when no ordering exists
     */
    @Override
    public BigDecimal percentRank() {
        if (null == comparator) {
            return null;
        }

        final int size = list.size();
        if (0 == size) {
            return null;
        }
        if (1 == size) {
            return BigDecimal.ONE;
        }

        return toBigDecimal(rank() - 1).divide(toBigDecimal(size - 1), 16, RoundingMode.HALF_UP);
    }

    /**
     * Returns the cumulative distribution of the current row.
     *
     * @return the cumulative distribution, or {@code null} when no ordering exists
     */
    @Override
    public BigDecimal cumeDist() {
        if (null == comparator || list.isEmpty()) {
            return null;
        }
        long cnt = list.stream()
                        .filter(e -> comparator.compare(currentRecord.getV1(), e) >= 0)
                        .count();
        return toBigDecimal(cnt).divide(toBigDecimal(list.size()), 16, RoundingMode.HALF_UP);
    }

    /**
     * Assigns the current row to a bucket.
     *
     * @param bucketCnt the number of buckets
     * @return the bucket number
     */
    @Override
    public long ntile(long bucketCnt) {
        int size = orderedPartition.size();
        if (0 == size) {
            return 0L;
        }
        // 0-based buckets over the partition. rowNumber() is the partition index,
        // so the denominator is the partition size even when a frame is present.
        return bucketCnt * rowNumber() / size;
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

    /**
     * Resolves valid row bounds for the supplied window definition.
     *
     * @param windowDefinition the window definition
     * @param index the current row index
     * @param value the current order value
     * @param listWithIndex the partition values with indexes
     * @param <T> the row type
     * @param <U> the order value type
     * @return the effective row bounds, or {@code null} if none apply
     */
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
            boolean numericBounds = (lower instanceof Number || null == lower) && (upper instanceof Number || null == upper);
            final List<Order<? super T, ? extends U>> orderList = windowDefinition.orderBy();
            if (numericBounds && null != orderList && 1 == orderList.size() && (value instanceof Number || null == value)) {
                if (listWithIndex.isEmpty()) {
                    return null;
                }
                Order<? super T, ? extends U> order = orderList.get(0);
                List<U> keys = listWithIndex.stream()
                        .map(e -> order.getKeyExtractor().apply(e.getV1()))
                        .collect(Collectors.toList());
                int keyCount = keys.size();
                int[] span = numericSpan(keys, order.isNullsLast());
                int numericStart = span[0];
                int numericEnd = span[1];
                boolean hasNumeric = numericStart <= numericEnd;
                boolean hasNull = order.isNullsLast() ? numericEnd < keyCount - 1 : numericStart > 0;

                if (null == value) {
                    // A null plus an offset is null. The offset bounds therefore
                    // address the null peer group; an unbounded bound addresses
                    // the partition edge in that value direction.
                    RowBound bound = nullKeyRange(lower, upper, keyCount, numericStart, numericEnd, order.isNullsLast(), order.isAsc());
                    if (null == bound) {
                        return null;
                    }
                    firstIndex = bound.getLower();
                    lastIndex = bound.getUpper();
                } else if (!hasNull) {
                    long[] indexes = numericRangeIndexes(order.isAsc(), (Number) value, (Number) lower, (Number) upper, keys);
                    if (null == indexes) {
                        return null;
                    }
                    firstIndex = indexes[0];
                    lastIndex = indexes[1];
                } else if (!hasNumeric) {
                    // Every key is null, so the null peer group is the partition.
                    RowBound bound = nullKeyRange(lower, upper, keyCount, numericStart, numericEnd, order.isNullsLast(), order.isAsc());
                    if (null == bound) {
                        return null;
                    }
                    firstIndex = bound.getLower();
                    lastIndex = bound.getUpper();
                } else {
                    List<U> numericKeys = keys.subList(numericStart, numericEnd + 1);
                    long[] indexes = numericRangeIndexes(order.isAsc(), (Number) value, (Number) lower, (Number) upper, numericKeys);
                    if (null == indexes) {
                        return null;
                    }
                    int numericSize = numericKeys.size();
                    if ((indexes[0] < 0 && indexes[1] < 0) || (indexes[0] >= numericSize && indexes[1] >= numericSize)) {
                        return null;
                    }
                    // Null keys are not inside a finite numeric interval. Include
                    // them only when an unbounded side already reaches the numeric
                    // edge they sit against, so the frame stays one contiguous slice.
                    long absFirst = numericStart + Math.max(indexes[0], 0L);
                    long absLast = numericStart + Math.min(indexes[1], numericSize - 1L);
                    // Nulls are not numbers. Include the null peer group only when
                    // an unbounded bound runs off the numeric edge that group sits
                    // against. Ascending nulls-last puts that group after the high
                    // values; descending nulls-last puts it after the low values.
                    boolean ascOrder = order.isAsc();
                    boolean coversLow = ascOrder ? absFirst == numericStart : absLast == numericEnd;
                    boolean coversHigh = ascOrder ? absLast == numericEnd : absFirst == numericStart;
                    if (order.isNullsLast()) {
                        boolean throughTrailing = ascOrder ? coversHigh && null == upper : coversLow && null == lower;
                        if (throughTrailing) {
                            absLast = keyCount - 1L;
                        }
                    } else {
                        boolean throughLeading = ascOrder ? coversLow && null == lower : coversHigh && null == upper;
                        if (throughLeading) {
                            absFirst = 0L;
                        }
                    }
                    if (absFirst > absLast) {
                        return null;
                    }
                    firstIndex = absFirst;
                    lastIndex = absLast;
                }
            } else if (!(null == lower && null == upper)) {
                throw new IllegalArgumentException(
                        "range bounds apply to one numeric order key; this order key or these bounds are not numeric");
            }
        }

        if ((firstIndex < 0 && lastIndex < 0) || (firstIndex >= size && lastIndex >= size)) {
            return null;
        }
        long from = Math.max(firstIndex, 0L);
        long to = Math.min(lastIndex, size - 1L);
        // A frame that ends before it starts is empty. Clamping each side
        // independently would ask limit() for a negative length.
        if (from > to) {
            return null;
        }
        return new RowBound(from, to);
    }

    /**
     * Finds the contiguous numeric keys. Null keys are grouped at one end by the
     * window order ({@code NULLS LAST} by default, {@code NULLS FIRST} otherwise).
     *
     * @param keys ordered window keys
     * @param nullsLast whether null keys sort after non-null keys
     * @param <U> the order value type
     * @return {@code [numericStart, numericEnd]}; the end is before the start when every key is null
     */
    private static <U> int[] numericSpan(List<U> keys, boolean nullsLast) {
        int size = keys.size();
        int numericStart;
        int numericEnd;
        if (nullsLast) {
            numericEnd = size - 1;
            while (numericEnd >= 0 && null == keys.get(numericEnd)) {
                numericEnd--;
            }
            numericStart = 0;
        } else {
            numericStart = 0;
            while (numericStart < size && null == keys.get(numericStart)) {
                numericStart++;
            }
            numericEnd = size - 1;
        }
        return new int[] {numericStart, numericEnd};
    }

    /**
     * Resolves a numeric {@code range} frame against keys that are all non-null numbers.
     * A null offset is an unbounded bound. Returns {@code null} when the interval misses
     * every key. The indexes follow {@code keys} and may lie outside {@code [0, size)}.
     *
     * @param asc whether the keys are ascending
     * @param value the current order value
     * @param lower the lower offset, or {@code null} for unbounded preceding
     * @param upper the upper offset, or {@code null} for unbounded following
     * @param keys non-null numeric keys in window order
     * @param <U> the order value type
     * @return the relative {@code [first, last]} indexes, or {@code null} when the frame is empty
     */
    private static <U extends Comparable<? super U>> long[] numericRangeIndexes(boolean asc, Number value, Number lower, Number upper, List<U> keys) {
        int size = keys.size();
        if (0 == size) {
            return null;
        }
        // Null offsets are truly unbounded. A long sentinel would drop values
        // outside the long range, such as 1e20.
        BigDecimal lowerValue = null == lower ? null : toBigDecimal(plus(value, lower));
        BigDecimal upperValue = null == upper ? null : toBigDecimal(plus(value, upper));
        BigDecimal smallest = toBigDecimal((Number) (asc ? keys.get(0) : keys.get(size - 1)));
        BigDecimal largest = toBigDecimal((Number) (asc ? keys.get(size - 1) : keys.get(0)));
        if ((null != upperValue && upperValue.compareTo(smallest) < 0)
                || (null != lowerValue && lowerValue.compareTo(largest) > 0)) {
            return null;
        }
        long firstIndex;
        long lastIndex;
        if (asc) {
            firstIndex = null == lowerValue ? 0L : getIndexByValue(lowerValue, true, keys);
            lastIndex = null == upperValue ? size - 1L : getIndexByValue(upperValue, false, keys);
        } else {
            List<U> reversedList = new ReversedList<>(keys);
            lastIndex = null == lowerValue ? size - 1L : size - 1L - getIndexByValue(lowerValue, true, reversedList);
            firstIndex = null == upperValue ? 0L : size - 1L - getIndexByValue(upperValue, false, reversedList);
        }
        return new long[] {firstIndex, lastIndex};
    }

    /**
     * Range frame for a null current key. Offset bounds select the null peer group.
     * Unbounded bounds select the edges of the partition.
     *
     * @param lower the lower offset, or {@code null} when unbounded
     * @param upper the upper offset, or {@code null} when unbounded
     * @param size the partition size
     * @param numericStart index of the first numeric key
     * @param numericEnd index of the last numeric key
     * @param nullsLast whether null keys sort last
     * @param asc whether the window order is ascending
     * @return the frame, or {@code null} when it is empty
     */
    private static RowBound nullKeyRange(Object lower, Object upper, int size, int numericStart, int numericEnd, boolean nullsLast, boolean asc) {
        long peerStart = nullsLast ? numericEnd + 1L : 0L;
        long peerEnd = nullsLast ? size - 1L : numericStart - 1L;
        if (peerStart > peerEnd) {
            return null;
        }
        // A finite offset stays on the null peer group. An unbounded low or high
        // bound runs to the partition edge in that value direction, which is the
        // opposite end of the list when the order is descending.
        boolean lowUnbounded = null == lower;
        boolean highUnbounded = null == upper;
        long start = asc
                ? (lowUnbounded ? 0L : peerStart)
                : (highUnbounded ? 0L : peerStart);
        long end = asc
                ? (highUnbounded ? size - 1L : peerEnd)
                : (lowUnbounded ? size - 1L : peerEnd);
        if (start > end) {
            return null;
        }
        return new RowBound(start, end);
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
                if (value.compareTo(toBigDecimal((Number) list.get(i))) != 0) {
                    break;
                }
            }
            valueIndex = i + 1;
        } else {
            int i = valueIndex + 1;
            for (int n = list.size(); i < n; i++) {
                if (value.compareTo(toBigDecimal((Number) list.get(i))) != 0) {
                    break;
                }
            }
            valueIndex = i - 1;
        }

        return valueIndex;
    }

    /**
     * Converts row orders into tuple-based orders.
     *
     * @param orderList the row orders
     * @param <T> the row type
     * @param <U> the order value type
     * @return the tuple-based orders
     */
    static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(List<Queryable.Order<? super T, ? extends U>> orderList) {
        return orderList.stream()
                .map(order -> new Order<Tuple2<T, Long>, U>(t -> order.getKeyExtractor().apply(t.getV1()), order.isAsc(), order.isNullsLast()))
                .collect(Collectors.toList());
    }

    /**
     * Converts the orders of a window definition into tuple-based orders.
     *
     * @param windowDefinition the window definition
     * @param <T> the row type
     * @param <U> the order value type
     * @return the tuple-based orders
     */
    static <T, U extends Comparable<? super U>> List<Order<Tuple2<T, Long>, U>> composeOrders(WindowDefinition<T, U> windowDefinition) {
        return composeOrders(windowDefinition.orderBy());
    }

    private final Tuple2<T, Long> currentRecord;
    private final Comparator<? super T> comparator;
    private final int index;
    private final List<T> list;
    private final List<T> orderedPartition;
    @Serial private static final long serialVersionUID = -3458969297047398621L;
}
