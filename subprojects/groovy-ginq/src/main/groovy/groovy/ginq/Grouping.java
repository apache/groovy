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
 * One group produced by {@link Query#groupBy}. The group is itself a query over
 * its elements, and {@link #getKey()} is the value the key selector returned.
 * {@code count()} and the other aggregates therefore describe the group, which
 * is the same idea as LINQ's {@code IGrouping}.
 *
 * @param <K> the key type
 * @param <T> the element type
 * @since 7.0.0
 */
@Incubating
public interface Grouping<K, T> extends Query<T> {

    /**
     * Returns the group key.
     *
     * @return the key
     */
    K getKey();
}
