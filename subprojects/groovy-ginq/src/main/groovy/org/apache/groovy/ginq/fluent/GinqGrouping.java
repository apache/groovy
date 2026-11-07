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
package org.apache.groovy.ginq.fluent;

import groovy.transform.Internal;
import groovy.ginq.Grouping;
import org.apache.groovy.ginq.provider.collection.runtime.GroupResult;

/**
 * {@link Grouping} backed by a GINQ group.
 *
 * @param <K> the key type
 * @param <T> the element type
 * @since 7.0.0
 */
@Internal
final class GinqGrouping<K, T> extends GinqQuery<T> implements Grouping<K, T> {

    private final K key;

    GinqGrouping(GroupResult<K, T> group) {
        super(group, null);
        this.key = group.getKey();
    }

    @Override
    public K getKey() {
        return key;
    }
}
