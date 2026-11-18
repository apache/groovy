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
package org.apache.groovy.ginq.fluent

import groovy.ginq.Ginq
import org.apache.groovy.ginq.fluent.GinqQuery
import org.apache.groovy.ginq.provider.GinqProvider
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
import org.junit.jupiter.api.Test

class GinqQuerySourceTest {

    @Test
    void fromAnExistingQueryReturnsThatQuery() {
        IterableQuery original = new IterableQuery(Queryable.from([1, 2, 2]))
        assert GinqQuery.from(original).is(original)
        assert original.distinct().toList() == [1, 2]
    }

    @Test
    void joinAcceptsAQueryPassedAsAnIterable() {
        Iterable<Integer> inner = new IterableQuery(Queryable.from([10, 20]))
        List<Integer> sums = Ginq.of(1).crossJoin(inner) { Integer a, Integer b -> a + b }.toList()
        assert sums.toSet() == [11, 21] as Set
    }

    @Test
    void emptySourceVarianceIsNullAndAParallelStreamStaysParallel() {
        assert Queryable.from([]).varp { it } == null
        assert Queryable.from([null, null]).var { it } == null
        QueryableHelper.setVar('parallel', 'true')
        try {
            assert Queryable.from([1, 2, 3]).stream().parallel
        } finally {
            QueryableHelper.setVar('parallel', 'false')
        }
    }

    @Test
    void defaultProviderHasNoExtraOptionsOrDependencies() {
        GinqProvider provider = new GinqProvider() {
            @Override
            String getName() { 'example' }

            @Override
            String getAstWalkerClassName() { 'example.Walker' }
        }
        assert provider.additionalOptionNames.empty
        assert provider.unavailableReason(GinqQuerySourceTest.classLoader) == null
    }

    private static final class IterableQuery extends GinqQuery<Integer> implements Iterable<Integer> {
        IterableQuery(Queryable<Integer> ready) {
            super(ready, null)
        }

        @Override
        Iterator<Integer> iterator() {
            return toList().iterator()
        }
    }
}
