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

import groovy.lang.Tuple2
import groovy.transform.CompileDynamic
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from

final class WindowImplTest {

    @Test
    void testNullPartitionKeyGroupsTogether() {
        assert [[1, 2], [2, 2], [3, 1]] == GQ {
            from r in [[k: null, n: 1], [k: null, n: 2], [k: 1, n: 3]]
            select r.n, (count() over(partitionby r.k))
        }.toList()
    }

    @Test
    void testRankUsesEveryOrderKey() {
        assert [[1, 2, 3, 2], [1, 1, 1, 1], [1, 1, 1, 1], [2, 0, 4, 3]] == GQ {
            from r in [[a: 1, b: 2], [a: 1, b: 1], [a: 1, b: 1], [a: 2, b: 0]]
            select r.a, r.b,
                    (rank() over(orderby r.a, r.b)),
                    (denseRank() over(orderby r.a, r.b))
        }.toList()

        assert [[1, null, null, null, null], [2, null, null, null, null]] == GQ {
            from n in [1, 2]
            select n, (rank() over()), (denseRank() over()), (percentRank() over()), (cumeDist() over())
        }.toList()

        assert [1] == GQ {
            from n in [7]
            select (percentRank() over(orderby n))
        }.toList()
    }

    @Test
    void testLeadAndNtileUseThePartition() {
        assert [[1, 2], [2, 3], [3, 4], [4, null]] == GQ {
            from n in [1, 2, 3, 4]
            select n, (lead(n) over(orderby n rows -1, 1))
        }.toList()

        assert [[1, 0], [2, 0], [3, 1], [4, 1]] == GQ {
            from n in [1, 2, 3, 4]
            select n, (ntile(2) over(orderby n rows 0, 0))
        }.toList()

        assert [[1, 1], [2, 2]] == GQ {
            from n in [1, 2]
            select n, (lead(n, 0) over(orderby n))
        }.toList()
    }

    @Test
    void testNthValueRejectsANegativeIndex() {
        def err = shouldFail(IllegalArgumentException) {
            GQ {
                from n in [1, 2]
                select (nthValue(n, -1) over(orderby n))
            }.toList()
        }
        assert err.message.contains('index cannot be less than 0')
    }

    @Test
    void testDirectBoundsForEmptyPeersAndMissedIntervals() {
        def order = new Queryable.Order({ Integer n -> n }, true)
        def finite = new WindowDefinitionImpl([order], new ValueBound(-1, 1))
        def numbers = [Tuple.tuple(1, 0L), Tuple.tuple(2, 1L)]
        assert null == WindowImpl.getValidRowBound(finite, 0, null, numbers)

        def nulls = [Tuple.tuple(null, 0L), Tuple.tuple(null, 1L)]
        RowBound peers = WindowImpl.getValidRowBound(finite, 0, 5, nulls)
        assert 0L == peers.lower
        assert 1L == peers.upper

        def backwards = new WindowDefinitionImpl([order], new ValueBound(2, -1))
        def mixed = [Tuple.tuple(1, 0L), Tuple.tuple(3, 1L), Tuple.tuple(null, 2L)]
        assert null == WindowImpl.getValidRowBound(backwards, 0, 2, mixed)

        Comparator<Integer> ordering = { Integer a, Integer b -> a <=> b }
        def empty = new WindowImpl(Tuple.tuple(1, 0L), 0, [], ordering, [])
        assert 0L == empty.ntile(4)
        assert null == empty.percentRank()
        assert null == empty.cumeDist()
        def one = new WindowImpl(Tuple.tuple(1, 0L), 0, [1], ordering, [1])
        assert BigDecimal.ONE == one.percentRank()
        assert 1 == one.lead({ Integer n -> n }, 0L, null)
    }

    @Test
    void testEmptyFrameAggregatesAreNull() {
        assert [null, null, null] == GQ {
            from n in [1, 2, 3]
            select (avg(n) over(orderby n rows 5, 6))
        }.toList()

        assert [null, null, null] == GQ {
            from n in [1, 2, 3]
            select (stdevp(n) over(orderby n rows 0, 0))
        }.toList()

        assert [null, null, null] == GQ {
            from n in [1, 2, 3]
            select (percentRank() over(orderby n rows 5, 6))
        }.toList()

        assert [null, null] == GQ {
            from n in [null, null]
            select (avg(n) over())
        }.toList()

        assert [0, 0, 0] == GQ {
            from n in [1, 2, 3]
            select (count() over(orderby n rows 5, 3))
        }.toList()
    }

    @Test
    void testRangeFramesAroundNullsAndLargeValues() {
        assert [[1.0d, 2L], [1.0e20d, 1L]] == GQ {
            from n in [1.0d, 1.0e20d]
            select n, (count() over(orderby n range 0, null))
        }.toList()

        assert [[null, 2L], [null, 2L]] == GQ {
            from n in [null, null]
            select n, (count() over(orderby n range -1, 1))
        }.toList()

        // Descending order keeps the value interval. An unbounded low bound
        // reaches the nulls that sort after the small values.
        assert [[1, 2L, 1], [null, 1L, 0], [3, 3L, 4]] == GQ {
            from n in [1, null, 3]
            select n,
                    (count() over(orderby n in desc range null, 0)),
                    (sum(n) over(orderby n in desc range null, 0))
        }.toList()

        assert [[1, 2L, 4], [null, 3L, 4], [3, 1L, 3]] == GQ {
            from n in [1, null, 3]
            select n,
                    (count() over(orderby n in desc range 0, null)),
                    (sum(n) over(orderby n in desc range 0, null))
        }.toList()

        assert [[1, 3L, 4], [null, 1L, 0], [3, 2L, 3]] == GQ {
            from n in [1, null, 3]
            select n,
                    (count() over(orderby n in desc(nullsfirst) range 0, null)),
                    (sum(n) over(orderby n in desc(nullsfirst) range 0, null))
        }.toList()
    }

    @Test
    void testNonNumericRangeIsRejected() {
        def err = shouldFail(IllegalArgumentException) {
            GQ {
                from s in ['a', 'b']
                select (count() over(orderby s range 1, 2))
            }.toList()
        }
        assert err.message.contains('numeric order key')

        assert [2, 2] == GQ {
            from s in ['a', 'b']
            select (count() over(orderby s range null, null))
        }.toList()
    }

    @Test
    void testEmptyPartitionHasNoRowBound() {
        WindowDefinition<Integer, Integer> definition = new WindowDefinitionImpl<>(
                [new Queryable.Order<>({ Integer n -> n }, true)],
                new ValueBound<>(0, null))
        List<Tuple2<Integer, Long>> empty = Collections.<Tuple2<Integer, Long>>emptyList()
        assert null == WindowImpl.getValidRowBound(definition, 0, Integer.valueOf(1), empty)
    }

    @Test
    @CompileDynamic
    void testNullGroupKeyInSequentialAndParallelGrouping() {
        assert grouped([null, 'a', null]) == [[null, 2L], ['a', 1L]] as Set

        def previous = QueryableHelper.getVar('parallel')
        QueryableHelper.setVar('parallel', 'true')
        def parallelGroups
        def into
        try {
            assert QueryableHelper.isParallel()
            // Grouping itself honors parallel mode. select() does not: it expects
            // the macro's future protocol, so turn parallel off before reading.
            parallelGroups = from([null, 'a', null]).groupBy(e -> e)
            into = from([null, 'b', null]).groupByInto(e -> e)
        } finally {
            QueryableHelper.setVar('parallel', previous)
        }
        assert parallelGroups.select((e, q) -> Tuple.tuple(e.v1, e.v2.count())).toList() as Set == [[null, 2L], ['a', 1L]] as Set
        def intoRows = into.toList()
        assert 2 == intoRows.size()
        assert 2L == intoRows.find { it.key == null }.count()
        assert 1L == intoRows.find { it.key == 'b' }.count()
    }

    @CompileDynamic
    private static Set grouped(List values) {
        from(values).groupBy(e -> e)
                .select((e, q) -> Tuple.tuple(e.v1, e.v2.count()))
                .toList() as Set
    }
}
