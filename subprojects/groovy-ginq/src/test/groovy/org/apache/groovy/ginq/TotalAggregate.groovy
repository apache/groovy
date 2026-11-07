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
package org.apache.groovy.ginq

import org.apache.groovy.ginq.provider.GinqAggregate
import org.apache.groovy.ginq.provider.collection.runtime.Queryable

import java.util.function.Function

/**
 * Test aggregate registered as {@code total}. Sums the selected numbers in a group.
 */
class TotalAggregate implements GinqAggregate {

    @Override
    String name() {
        return 'total'
    }

    @Override
    Object apply(Queryable<?> group, Function<Object, Object> selector) {
        BigDecimal sum = BigDecimal.ZERO
        for (Object row : group.toList()) {
            Object value = selector == null ? row : selector.apply(row)
            if (value instanceof Number) {
                sum = sum.add(new BigDecimal(value.toString()))
            }
        }
        return sum
    }
}
