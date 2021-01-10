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

import java.util.List;

/**
 * Represents partition
 *
 * @param <T> the type of element
 * @since 4.0.0
 */
class PartitionImpl<T> extends QueryableCollection<T> implements Partition<T> {
    private static final long serialVersionUID = -3650144225768070117L;

    public static <T, U extends Comparable<? super U>> Partition<Tuple2<T, Long>> newInstance(List<Tuple2<T, Long>> listWithIndex) {
        return new PartitionImpl<>(listWithIndex);
    }

    PartitionImpl(Iterable<T> sourceIterable) {
        super(sourceIterable);
    }
}
