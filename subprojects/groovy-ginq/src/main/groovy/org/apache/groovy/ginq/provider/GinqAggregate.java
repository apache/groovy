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
package org.apache.groovy.ginq.provider;

import org.apache.groovy.ginq.provider.collection.runtime.Queryable;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * A named aggregate that the GINQ DSL can call from {@code select} and {@code having}
 * after {@code groupby}, in addition to the built-in aggregates.
 * <p>
 * Implementations are discovered with {@link ServiceLoader}. The public type must have
 * a public no-arg constructor and be listed in
 * {@code META-INF/services/org.apache.groovy.ginq.provider.GinqAggregate}.
 * Built-in names ({@code count}, {@code sum}, {@code agg}, ...) are never overridden.
 * A call has either no argument ({@code total()}) or one argument ({@code total(n.amount)}),
 * which is evaluated once per element. The fluent API does not use this SPI;
 * pass a lambda to {@code Query#aggregate} instead.
 *
 * @since 7.0.0
 */
@Incubating
public interface GinqAggregate {

    /**
     * Returns the name used in a GINQ query, for example {@code total}.
     *
     * @return the aggregate name
     */
    String name();

    /**
     * Aggregates one group.
     *
     * @param group the elements of the group; the same instance may be read more than once
     * @param selector the argument expression as a function of one element, or {@code null}
     *                 when the call had no argument
     * @return the aggregate value
     */
    Object apply(Queryable<?> group, Function<Object, Object> selector);
}
