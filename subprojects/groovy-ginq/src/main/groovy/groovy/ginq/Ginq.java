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

import org.apache.groovy.ginq.fluent.GinqQuery;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.stream.Stream;

/**
 * Starts a fluent GINQ query. The operators on {@link Query} follow LINQ's method
 * syntax ({@code where}, {@code select}, {@code orderBy}, {@code groupBy}, {@code join})
 * and run on the same in-memory runtime as {@code GQ { ... }}. They are deferred
 * until a terminal operation such as {@link Query#toList()}.
 * <p>
 * {@code GQ} remains the SQL-like syntax and is the front end used by other
 * providers, including SQL. Use that syntax when the query must be translated.
 * Use {@code Ginq.from} when the caller wants generic types under {@code @CompileStatic}.
 *
 * <pre>
 * List&lt;String&gt; names = Ginq.from(people)
 *         .where { it.age &gt;= 18 }
 *         .orderBy { it.name }
 *         .select { it.name }
 *         .toList()
 * </pre>
 *
 * @since 7.0.0
 */
@Incubating
public final class Ginq {

    private Ginq() {
    }

    /**
     * Queries {@code source}.
     *
     * @param source the elements
     * @param <T> the element type
     * @return a query over {@code source}
     * @throws NullPointerException when {@code source} is null
     */
    public static <T> Query<T> from(Iterable<T> source) {
        return GinqQuery.from(source);
    }

    /**
     * Queries the elements of {@code source}, in array order.
     *
     * @param source the elements
     * @param <T> the element type
     * @return a query over {@code source}
     * @throws NullPointerException when {@code source} is null
     */
    public static <T> Query<T> from(T[] source) {
        return GinqQuery.from(source);
    }

    /**
     * Queries {@code source}. The stream is read on the first terminal operation
     * and later terminals replay that read, because a stream can be read only once.
     *
     * @param source the elements
     * @param <T> the element type
     * @return a query over {@code source}
     * @throws NullPointerException when {@code source} is null
     */
    public static <T> Query<T> from(Stream<T> source) {
        return GinqQuery.from(source);
    }

    /**
     * Returns {@code source} when it is already a GINQ query, otherwise queries its elements.
     *
     * @param source the query to continue
     * @param <T> the element type
     * @return {@code source}
     * @throws NullPointerException when {@code source} is null
     */
    public static <T> Query<T> from(Query<T> source) {
        return GinqQuery.from(source);
    }

    /**
     * Queries the given elements.
     *
     * @param elements the elements, which may be empty
     * @param <T> the element type
     * @return a query over {@code elements}
     */
    @SafeVarargs
    public static <T> Query<T> of(T... elements) {
        return GinqQuery.of(elements);
    }

    /**
     * Returns a query with no elements.
     *
     * @param <T> the element type
     * @return an empty query
     */
    public static <T> Query<T> empty() {
        return GinqQuery.empty();
    }
}
