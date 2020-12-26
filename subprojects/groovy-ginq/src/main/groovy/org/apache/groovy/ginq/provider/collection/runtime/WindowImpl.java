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

import java.util.List;
import java.util.function.Function;

/**
 * Represents window which stores elements used by window functions
 *
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
class WindowImpl<T, U extends Comparable<? super U>> extends QueryableCollection<T> implements Window<T> {
    private static final long serialVersionUID = -3458969297047398621L;
    private final T currentRecord;
    private final long index;
    private final U value;
    private final WindowDefinition<T, U> windowDefinition;

    WindowImpl(T currentRecord, Queryable<T> partition, WindowDefinition<T, U> windowDefinition) {
        super(partition.orderBy(windowDefinition.orderBy().toArray(Order.EMPTY_ARRAY)).toList());
        this.currentRecord = currentRecord;
        this.windowDefinition = windowDefinition;

        List<T> sortedList = this.toList();
        final List<Order<? super T, ? extends U>> order = windowDefinition.orderBy();
        if (null != order && 1 == order.size()) {
            this.value = order.get(0).getKeyExtractor().apply(currentRecord);
        } else {
            this.value = null;
        }

        int tmpIndex = -1;
        for (int i = 0, n = sortedList.size(); i < n; i++) {
            if (currentRecord == sortedList.get(i)) {
                tmpIndex = i;
                break;
            }
        }

        this.index = tmpIndex;
    }

    @Override
    public long rowNumber() {
        return index;
    }

    @Override
    public <V> V lead(Function<? super T, ? extends V> extractor, long lead) {
        V field = null;
        if (0 == lead) {
            field = extractor.apply(currentRecord);
        } else if (0 <= index + lead && index + lead < this.size()) {
            field = extractor.apply(this.toList().get((int) index + (int) lead));
        }

        return field;
    }

    @Override
    public <V> V lag(Function<? super T, ? extends V> extractor, long lag) {
        return lead(extractor, -lag);
    }

    @Override
    public <V> V firstValue(Function<? super T, ? extends V> extractor) {
        long firstIndex = getFirstIndex();
        return extractor.apply(this.toList().get((int) firstIndex));
    }

    @Override
    public <V> V lastValue(Function<? super T, ? extends V> extractor) {
        long lastIndex = getLastIndex();
        return extractor.apply(this.toList().get((int) lastIndex));
    }

    private long getFirstIndex() {
        RowBound rowBound = windowDefinition.rows();
        long firstRowIndex;
        final Long lower = rowBound.getLower();
        if (null == lower || Long.MIN_VALUE == lower) {
            firstRowIndex = 0;
        } else {
            firstRowIndex = index + lower;
            firstRowIndex = Math.max(firstRowIndex, 0);
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
            lastRowIndex = Math.min(lastRowIndex, size - 1);
        }
        return lastRowIndex;
    }
}
