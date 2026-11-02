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
package org.apache.groovy.ginq.provider.sql

import groovy.sql.Sql
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Executes window queries against H2. The native and jOOQ providers must
 * both push {@code OVER} down and agree on GINQ's results.
 */
class GinqSqlWindowExecutionTest {
    private Sql db

    @BeforeEach
    void setUp() {
        db = GinqSqlTestSupport.newPopulatedDb()
        db.execute 'create table nums (n int)'
        [1, 2, 5, 5].each { db.execute 'insert into nums (n) values (?)', [it] }
        db.execute 'create table seq (n int)'
        (1..10).each { db.execute 'insert into seq (n) values (?)', [it] }
        db.execute 'create table scores (n int)'
        [60, 60, 80, 80, 100].each { db.execute 'insert into scores (n) values (?)', [it] }
        db.execute 'create table gaps (n int)'
        [1, 2, null, 3].each { db.execute 'insert into gaps (n) values (?)', [it] }
        db.execute 'create table keyed (k int, n int)'
        [[1, null], [1, null], [2, 4], [2, null]].each {
            db.execute 'insert into keyed (k, n) values (?, ?)', it
        }
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void testRowNumberIsZeroBased() {
        def expected = [['Alice', 0], ['Bob', 1], ['Carol', 2], ['Dave', 3], ['Eve', 4], ['Frank', 5]]
        assert expected == names(nativeRows())
        assert expected == names(jooqRows())
    }

    @Test
    void testPartitionOrderNullsAndRanks() {
        def partitions = [['Alice', 0], ['Bob', 1], ['Carol', 0], ['Dave', 0], ['Eve', 1], ['Frank', 0]]
        assert partitions == nativePartitionNumbers()
        assert partitions == jooqPartitionNumbers()

        def ranks = [[2000, 1, 1], [3000, 2, 2], [4000, 3, 3], [5000, 4, 4], [5000, 4, 4], [6000, 6, 5]]
        assert ranks == nativeRanks()
        assert ranks == jooqRanks()

        def gaps = [[1, 0, 3, null], [2, 1, 2, 1], [3, 2, 1, 2], [null, 3, 0, 3]]
        assert gaps == nativeGaps()
        assert gaps == jooqGaps()
    }

    @Test
    void testLeadLagAndValueFunctions() {
        def offsets = [
                ['Alice', 'Bob', 'NONE', 'Carol'],
                ['Bob', 'Carol', 'Alice', 'Dave'],
                ['Carol', 'Dave', 'Bob', 'Eve'],
                ['Dave', 'Eve', 'Carol', 'Frank'],
                ['Eve', 'Frank', 'Dave', null],
                ['Frank', null, 'Eve', null]
        ]
        assert offsets == nativeOffsets()
        assert offsets == jooqOffsets()

        def values = [
                ['Alice', 'Alice', 'Bob', 'Alice', 'Bob'],
                ['Bob', 'Alice', 'Bob', 'Alice', 'Bob'],
                ['Carol', 'Carol', 'Eve', 'Carol', 'Eve'],
                ['Dave', 'Dave', 'Dave', 'Dave', null],
                ['Eve', 'Carol', 'Eve', 'Carol', 'Eve'],
                ['Frank', 'Frank', 'Frank', 'Frank', null]
        ]
        assert values == nativeValues()
        assert values == jooqValues()
    }

    @Test
    void testAggregatesUseTheWholePartitionUnlessAFrameIsWritten() {
        def totals = nativeTotals()
        assert totals.every { it[1] == 6 && it[2] == 25000 && it[3] == 5 }
        assert totals == jooqTotals()

        def byDept = [
                ['Alice', 2, 8000, 4000, 3000, 5000, 2],
                ['Bob', 2, 8000, 4000, 3000, 5000, 2],
                ['Carol', 2, 9000, 4500, 4000, 5000, 2],
                ['Dave', 1, 2000, 2000, 2000, 2000, 1],
                ['Eve', 2, 9000, 4500, 4000, 5000, 2],
                ['Frank', 1, 6000, 6000, 6000, 6000, 1]
        ]
        assert byDept == nativeByDept()
        assert byDept == jooqByDept()

        assert [[1, 0], [1, 0], [2, 4], [2, 4]] == nativeNullSums()
        assert [[1, 0], [1, 0], [2, 4], [2, 4]] == jooqNullSums()
    }

    @Test
    void testRowsAndRangeFrames() {
        def rows = [[1, 3, 1, 2], [2, 6, 1, 3], [3, 5, 2, 3]]
        assert rows == nativeRowFrame()
        assert rows == jooqRowFrame()

        def range = [[1, 1, 1], [2, 2, 3], [5, 2, 10], [5, 2, 10]]
        assert range == nativeRange()
        assert range == jooqRange()
    }

    @Test
    void testNtilePercentRankAndCumeDist() {
        def buckets = (1..10).collect { n ->
            int index = n - 1
            [n, (4 * index).intdiv(10)]
        }
        assert buckets == nativeNtile()
        assert buckets == jooqNtile()

        def scores = [[60, 0, 0.4], [60, 0, 0.4], [80, 0.5, 0.8], [80, 0.5, 0.8], [100, 1, 1]]
        assert close(scores, nativeScores())
        assert close(scores, jooqScores())

        def single = [
                ['Alice', 1, 1],
                ['Bob', 0, 0.5],
                ['Carol', 0, 0.5],
                ['Dave', 1, 1],
                ['Eve', 1, 1],
                ['Frank', 1, 1]
        ]
        assert close(single, nativeSingle())
        assert close(single, jooqSingle())
    }

    private List nativeRows() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name, (rowNumber() over(orderby e.name)) as rn
        }
    }

    private List jooqRows() {
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name, (rowNumber() over(orderby e.name)) as rn
        }
    }

    private static List names(List rows) {
        rows.collect { [it.name, it.rn] }
    }

    private List nativePartitionNumbers() {
        rows(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name, (rowNumber() over(partitionby e.deptId orderby e.name)) as rn
        })
    }

    private List jooqPartitionNumbers() {
        rows(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name, (rowNumber() over(partitionby e.deptId orderby e.name)) as rn
        })
    }

    private List nativeRanks() {
        ranks(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.salary, e.id
            select e.salary, (rank() over(orderby e.salary)) as r, (denseRank() over(orderby e.salary)) as d
        })
    }

    private List jooqRanks() {
        ranks(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.salary, e.id
            select e.salary, (rank() over(orderby e.salary)) as r, (denseRank() over(orderby e.salary)) as d
        })
    }

    private static List ranks(List rows) {
        rows.collect { [it.salary, it.r, it.d] }
    }

    private List nativeGaps() {
        gaps(GQL(provider: 'native-sql', dataSource: db) {
            from g in 'gaps'
            orderby g.n
            select g.n,
                    (rowNumber() over(orderby g.n)) as ascLast,
                    (rowNumber() over(orderby g.n in desc(nullsfirst))) as descFirst,
                    (lag(g.n) over(orderby g.n)) as prev
        })
    }

    private List jooqGaps() {
        gaps(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from g in 'gaps'
            orderby g.n
            select g.n,
                    (rowNumber() over(orderby g.n)) as ascLast,
                    (rowNumber() over(orderby g.n in desc(nullsfirst))) as descFirst,
                    (lag(g.n) over(orderby g.n)) as prev
        })
    }

    private static List gaps(List rows) {
        rows.collect { [it.n, it.ascLast, it.descFirst, it.prev] }
    }

    private List nativeOffsets() {
        offsets(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.id
            select e.name,
                    (lead(e.name) over(orderby e.id)) as nxt,
                    (lag(e.name, 1, 'NONE') over(orderby e.id)) as prev,
                    (lead(e.name, 2) over(orderby e.id)) as skip
        })
    }

    private List jooqOffsets() {
        offsets(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.id
            select e.name,
                    (lead(e.name) over(orderby e.id)) as nxt,
                    (lag(e.name, 1, 'NONE') over(orderby e.id)) as prev,
                    (lead(e.name, 2) over(orderby e.id)) as skip
        })
    }

    private static List offsets(List rows) {
        rows.collect { [it.name, it.nxt, it.prev, it.skip] }
    }

    private List nativeValues() {
        values(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (firstValue(e.name) over(partitionby e.deptId orderby e.name)) as firstName,
                    (lastValue(e.name) over(partitionby e.deptId orderby e.name)) as lastName,
                    (nthValue(e.name, 0) over(partitionby e.deptId orderby e.name)) as nth0,
                    (nthValue(e.name, 1) over(partitionby e.deptId orderby e.name)) as nth1
        })
    }

    private List jooqValues() {
        values(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (firstValue(e.name) over(partitionby e.deptId orderby e.name)) as firstName,
                    (lastValue(e.name) over(partitionby e.deptId orderby e.name)) as lastName,
                    (nthValue(e.name, 0) over(partitionby e.deptId orderby e.name)) as nth0,
                    (nthValue(e.name, 1) over(partitionby e.deptId orderby e.name)) as nth1
        })
    }

    private static List values(List rows) {
        rows.collect { [it.name, it.firstName, it.lastName, it.nth0, it.nth1] }
    }

    private List nativeTotals() {
        totals(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (count() over(orderby e.salary)) as cnt,
                    (sum(e.salary) over(orderby e.salary)) as total,
                    (count(e.deptId) over()) as present
        })
    }

    private List jooqTotals() {
        totals(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (count() over(orderby e.salary)) as cnt,
                    (sum(e.salary) over(orderby e.salary)) as total,
                    (count(e.deptId) over()) as present
        })
    }

    private static List totals(List rows) {
        rows.collect { [it.name, it.cnt, it.total, it.present] }
    }

    private List nativeByDept() {
        byDept(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (count() over(partitionby e.deptId)) as cnt,
                    (sum(e.salary) over(partitionby e.deptId)) as total,
                    (avg(e.salary) over(partitionby e.deptId)) as avgSalary,
                    (min(e.salary) over(partitionby e.deptId)) as lo,
                    (max(e.salary) over(partitionby e.deptId)) as hi,
                    (count(distinct(e.salary)) over(partitionby e.deptId)) as kinds
        })
    }

    private List jooqByDept() {
        byDept(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (count() over(partitionby e.deptId)) as cnt,
                    (sum(e.salary) over(partitionby e.deptId)) as total,
                    (avg(e.salary) over(partitionby e.deptId)) as avgSalary,
                    (min(e.salary) over(partitionby e.deptId)) as lo,
                    (max(e.salary) over(partitionby e.deptId)) as hi,
                    (count(distinct(e.salary)) over(partitionby e.deptId)) as kinds
        })
    }

    private static List byDept(List rows) {
        rows.collect { [it.name, it.cnt, it.total, it.avgSalary, it.lo, it.hi, it.kinds] }
    }

    private List nativeNullSums() {
        nullSums(GQL(provider: 'native-sql', dataSource: db) {
            from k in 'keyed'
            orderby k.k, k.n
            select k.k, (sum(k.n) over(partitionby k.k)) as total
        })
    }

    private List jooqNullSums() {
        nullSums(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from k in 'keyed'
            orderby k.k, k.n
            select k.k, (sum(k.n) over(partitionby k.k)) as total
        })
    }

    private static List nullSums(List rows) {
        rows.collect { [it.k, it.total] }
    }

    private List nativeRowFrame() {
        frames(GQL(provider: 'native-sql', dataSource: db) {
            from s in 'seq'
            where s.n <= 3
            orderby s.n
            select s.n,
                    (sum(s.n) over(orderby s.n rows -1, 1)) as total,
                    (firstValue(s.n) over(orderby s.n rows -1, 1)) as first,
                    (lastValue(s.n) over(orderby s.n rows -1, 1)) as last
        })
    }

    private List jooqRowFrame() {
        frames(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from s in 'seq'
            where s.n <= 3
            orderby s.n
            select s.n,
                    (sum(s.n) over(orderby s.n rows -1, 1)) as total,
                    (firstValue(s.n) over(orderby s.n rows -1, 1)) as first,
                    (lastValue(s.n) over(orderby s.n rows -1, 1)) as last
        })
    }

    private static List frames(List rows) {
        rows.collect { [it.n, it.total, it.first, it.last] }
    }

    private List nativeRange() {
        range(GQL(provider: 'native-sql', dataSource: db) {
            from n in 'nums'
            orderby n.n
            select n.n,
                    (count() over(orderby n.n range -2, 0)) as cnt,
                    (sum(n.n) over(orderby n.n range -2, 0)) as total
        })
    }

    private List jooqRange() {
        range(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from n in 'nums'
            orderby n.n
            select n.n,
                    (count() over(orderby n.n range -2, 0)) as cnt,
                    (sum(n.n) over(orderby n.n range -2, 0)) as total
        })
    }

    private static List range(List rows) {
        rows.collect { [it.n, it.cnt, it.total] }
    }

    private List nativeNtile() {
        ntile(GQL(provider: 'native-sql', dataSource: db) {
            from s in 'seq'
            orderby s.n
            select s.n, (ntile(4) over(orderby s.n)) as bucket
        })
    }

    private List jooqNtile() {
        ntile(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from s in 'seq'
            orderby s.n
            select s.n, (ntile(4) over(orderby s.n)) as bucket
        })
    }

    private static List ntile(List rows) {
        rows.collect { [it.n, it.bucket] }
    }

    private List nativeScores() {
        scores(GQL(provider: 'native-sql', dataSource: db) {
            from s in 'scores'
            orderby s.n
            select s.n,
                    (percentRank() over(orderby s.n)) as pct,
                    (cumeDist() over(orderby s.n)) as cume
        })
    }

    private List jooqScores() {
        scores(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from s in 'scores'
            orderby s.n
            select s.n,
                    (percentRank() over(orderby s.n)) as pct,
                    (cumeDist() over(orderby s.n)) as cume
        })
    }

    private static List scores(List rows) {
        rows.collect { [it.n, it.pct, it.cume] }
    }

    private List nativeSingle() {
        single(GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (percentRank() over(partitionby e.deptId orderby e.salary)) as pct,
                    (cumeDist() over(partitionby e.deptId orderby e.salary)) as cume
        })
    }

    private List jooqSingle() {
        single(GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            select e.name,
                    (percentRank() over(partitionby e.deptId orderby e.salary)) as pct,
                    (cumeDist() over(partitionby e.deptId orderby e.salary)) as cume
        })
    }

    private static List single(List rows) {
        rows.collect { [it.name, it.pct, it.cume] }
    }

    private static List rows(List result) {
        result.collect { [it.name, it.rn] }
    }

    private static boolean close(List<List> expected, List actual) {
        assert expected.size() == actual.size()
        expected.eachWithIndex { List want, int i ->
            List got = (List) actual[i]
            assert want.size() == got.size()
            want.eachWithIndex { Object cell, int j ->
                Object value = got[j]
                if (cell instanceof BigDecimal || cell instanceof Double) {
                    assert value != null
                    assert Math.abs(((Number) value).doubleValue() - ((Number) cell).doubleValue()) < 0.0000001d
                } else {
                    assert cell == value
                }
            }
        }
        return true
    }
}
