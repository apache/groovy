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

import java.util.Objects;
import java.util.function.Function;

/**
 * The default implementation of {@link WindowDefinition}
 *
 * @param <T> the type of {@link Queryable} element
 * @param <U> the type of field to sort
 * @since 4.0.0
 */
class WindowDefinitionImpl<T, U extends Comparable<? super U>> implements WindowDefinition<T, U> {
    private final Function<? super T, ?> partitionBy;
    private final Queryable.Order<? super T, ? extends U> orderBy;
    private final Tuple2<Long, Long> rows;
    private final Tuple2<? extends U, ? extends U> range;

    public WindowDefinitionImpl(Function<? super T, ?> partitionBy, Queryable.Order<? super T, ? extends U> orderBy,
                                Tuple2<Long, Long> rows, Tuple2<? extends U, ? extends U> range) {
        this.partitionBy = partitionBy;
        this.orderBy = orderBy;
        this.rows = rows;
        this.range = range;
    }

    public WindowDefinitionImpl(Function<? super T, ?> partitionBy) {
        this(partitionBy, null, DEFAULT_ROWS, null);
    }

    public WindowDefinitionImpl(Queryable.Order<? super T, ? extends U> orderBy) {
        this((T t) -> Queryable.NULL, orderBy, DEFAULT_ROWS, null);
    }

    public WindowDefinitionImpl(Function<? super T, ?> partitionBy, Queryable.Order<? super T, ? extends U> orderBy) {
        this(partitionBy, orderBy, DEFAULT_ROWS, null);
    }

    public WindowDefinitionImpl(Function<? super T, ?> partitionBy, Queryable.Order<? super T, ? extends U> orderBy,
                                Tuple2<Long, Long> rows) {
        this(partitionBy, orderBy, rows, null);
    }

    @Override
    public Function<? super T, ?> partitionBy() {
        return partitionBy;
    }

    @Override
    public Queryable.Order<? super T, ? extends U> orderBy() {
        return orderBy;
    }

    @Override
    public Tuple2<Long, Long> rows() {
        return rows;
    }

    @Override
    public Tuple2<? extends U, ? extends U> range() {
        return range;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public WindowDefinition<T, U> setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WindowDefinitionImpl)) return false;
        WindowDefinitionImpl<?, ?> that = (WindowDefinitionImpl<?, ?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private String id;
}
