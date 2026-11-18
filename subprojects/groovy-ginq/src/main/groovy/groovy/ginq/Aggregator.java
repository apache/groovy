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

/**
 * Reduces a {@link Query} to one value. This is the fluent counterpart of the
 * GINQ {@code agg} function: the argument is the group, or the whole query when
 * there is no grouping.
 *
 * <pre>
 * Ginq.from(orders).aggregate { rows -&gt; rows.sum { it.amount } }
 * </pre>
 *
 * Named aggregates used from the SQL-like DSL are a separate extension point,
 * {@code org.apache.groovy.ginq.provider.GinqAggregate}.
 *
 * @param <T> the element type
 * @param <R> the aggregate type
 * @since 7.0.0
 */
@Incubating
@FunctionalInterface
public interface Aggregator<T, R> {

    /**
     * Aggregates {@code rows}.
     *
     * @param rows the query or group to reduce; reading it is terminal
     * @return the aggregate value
     */
    R apply(Query<T> rows);
}
