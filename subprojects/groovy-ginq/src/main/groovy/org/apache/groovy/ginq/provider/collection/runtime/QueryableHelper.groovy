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
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.CompileStatic

import java.util.stream.Collectors

import static Queryable.from

/**
 * Helper for {@link Queryable}
 *
 * @since 4.0.0
 */
@CompileStatic
class QueryableHelper {
    /**
     * Make {@link Queryable} instance's data source records being able to access via aliases
     *
     * @param queryable the original {@link Queryable} instance
     * @param aliasList the aliases of clause {@code from} and joins
     * @return the result {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<SourceRecord<T>> navigate(Queryable<? extends T> queryable, List<String> aliasList) {
        List<SourceRecord<T>> sourceRecordList =
                queryable.stream()
                        .map(e -> new SourceRecord<T>(e, aliasList))
                        .collect(Collectors.toList())

        return from(sourceRecordList)
    }

    /**
     * Returns single value of list
     *
     * @param list the list
     * @return the single value
     * @throws TooManyValuesException if list contains more than one values
     * @since 4.0.0
     */
    static <T> T singleValue(final List<? extends T> list) {
        int size = list.size()

        if (0 == size) {
            return null
        }
        if (1 == size) {
            return list.get(0)
        }

        throw new TooManyValuesException("subquery returns more than one value: $list")
    }

    private QueryableHelper() {}
}
