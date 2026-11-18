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

import static groovy.test.GroovyAssert.shouldFail

/**
 * Runs one GINQ clause shape on the collection provider and on H2
 * ({@code native-sql} and {@code jooq-sql}) and requires the frozen
 * window results to agree.
 */
class GinqWindowProviderAgreementTest {
    private Sql db
    private List<Map> oneRow
    private List<Map> running
    private List<Map> scores
    private List<Map> blanks
    private List<Map> seq

    @BeforeEach
    void setUp() {
        db = GinqSqlTestSupport.newPopulatedDb()
        oneRow = rows(7)
        running = rows(10, 20, 30)
        scores = rows(60, 60, 80, 80, 100)
        blanks = [[n: null], [n: null]]
        seq = rows(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        store('one_row', oneRow)
        store('running', running)
        store('scores', scores)
        store('blanks', blanks)
        store('seq', seq)
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void testOneRowNumberingIsZeroBased() {
        agreeOn('one_row', oneRow, ['rn', 'nth'], '''
            orderby r.n
            select (rowNumber() over(orderby r.n)) as rn, (nthValue(r.n, 0) over(orderby r.n)) as nth
        ''', [[0, 7]])
    }

    @Test
    void testNtileZeroStaysOnBucketZero() {
        agreeOn('seq', seq, ['n', 'bucket'], '''
            orderby r.n
            select r.n, (ntile(0) over(orderby r.n)) as bucket
        ''', seq.collect { [it.n, 0] })
    }

    @Test
    void testNtileUsesTheZeroBasedFormula() {
        agreeOn('seq', seq, ['n', 'bucket'], '''
            orderby r.n
            select r.n, (ntile(4) over(orderby r.n)) as bucket
        ''', seq.collect { row -> [row.n, (4 * (row.n - 1)).intdiv(10)] })
    }

    @Test
    void testOrderByWithoutAFrameIsTheWholePartition() {
        def expected = [[10, 60, 20], [20, 60, 20], [30, 60, 20]]
        agreeOn('running', running, ['n', 'total', 'mean'], '''
            orderby r.n
            select r.n, (sum(r.n) over(orderby r.n)) as total, (avg(r.n) over(orderby r.n)) as mean
        ''', expected)
        assert expected.every { it[1] == 60 }
    }

    @Test
    void testAllNullWindowSumIsZero() {
        agreeOn('blanks', blanks, ['total'], '''
            select (sum(r.n) over()) as total
        ''', [[0], [0]])
    }

    @Test
    void testPercentRankAgreesOnTheFrozenFormula() {
        agreeOn('one_row', oneRow, ['pct'], '''
            select (percentRank() over(orderby r.n)) as pct
        ''', [[1]])
        // ranks 1, 1, 3, 3, 5 over 5 rows: (rank - 1) / (size - 1)
        agreeOn('scores', scores, ['n', 'pct'], '''
            orderby r.n
            select r.n, (percentRank() over(orderby r.n)) as pct
        ''', [[60, 0], [60, 0], [80, 0.5], [80, 0.5], [100, 1]])
    }

    @Test
    void testEmptyFrameAverageStaysNull() {
        agreeOn('running', running, ['mean'], '''
            orderby r.n
            select (avg(r.n) over(orderby r.n rows 5, 6)) as mean
        ''', [[null], [null], [null]])
    }

    @Test
    void testRankFrameIsHonoredInMemoryAndRejectedBySql() {
        def framed = '''
            orderby r.n
            select r.n, (rank() over(orderby r.n rows 0, 0)) as rk
        '''
        agree(collection(running, framed), [[10, 1], [20, 1], [30, 1]])
        agree(collection(running, '''
            orderby r.n
            select r.n, (rank() over(orderby r.n)) as rk
        '''), [[10, 1], [20, 2], [30, 3]])

        def numbered = '''
            orderby r.n
            select r.n, (rowNumber() over(orderby r.n rows 0, 0)) as rn
        '''
        agree(collection(running, numbered), [[10, 0], [20, 1], [30, 2]])

        def nativeRank = sqlError('native-sql', 'running', framed)
        assert nativeRank.message.contains('does not accept')
        assert nativeRank.message.contains('GINQ applies that frame')
        def jooqRank = sqlError('jooq-sql', 'running', framed)
        assert jooqRank.message.contains('does not accept')
        assert jooqRank.message.contains('GINQ applies that frame')

        def nativeNumber = sqlError('native-sql', 'running', numbered)
        assert nativeNumber.message.contains('does not accept')
        assert !nativeNumber.message.contains('GINQ applies that frame')
        def jooqNumber = sqlError('jooq-sql', 'running', numbered)
        assert jooqNumber.message.contains('does not accept')
        assert !jooqNumber.message.contains('GINQ applies that frame')
    }

    @Test
    void testRankWithoutOrderByIsNullInMemoryAndRejectedBySql() {
        agree(collection(running, '''
            select (rank() over()) as rk,
                    (denseRank() over()) as dense,
                    (percentRank() over()) as pct,
                    (cumeDist() over()) as cume
        '''), [[null, null, null, null], [null, null, null, null], [null, null, null, null]])

        def rank = 'select (rank() over())'
        assert sqlError('native-sql', 'running', rank).message.contains('needs `orderby`')
        assert sqlError('jooq-sql', 'running', rank).message.contains('needs `orderby`')

        def percent = 'select (percentRank() over(partitionby r.n))'
        assert sqlError('native-sql', 'running', percent).message.contains('needs `orderby`')
        assert sqlError('jooq-sql', 'running', percent).message.contains('needs `orderby`')
    }

    private void agreeOn(String table, List<Map> source, List<String> columns, String clauses, List expected) {
        agree(collection(source, clauses), expected)
        agree(project(query('native-sql', table, clauses), columns), expected)
        agree(project(query('jooq-sql', table, clauses), columns), expected)
    }

    private List collection(List<Map> source, String clauses) {
        evaluate("""
            GQ {
                from r in rows
                ${clauses}
            }.toList()
        """, [rows: source])
    }

    private List query(String provider, String table, String clauses) {
        String call = provider == 'jooq-sql'
                ? "GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db)"
                : "GQL(provider: 'native-sql', dataSource: db)"
        evaluate("""
            ${call} {
                from r in '${checked(table)}'
                ${clauses}
            }
        """, [db: db])
    }

    private static Throwable sqlError(String provider, String table, String clauses) {
        String call = provider == 'jooq-sql'
                ? "GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db)"
                : "GQL(provider: 'native-sql', dataSource: db)"
        shouldFail """
            ${call} {
                from r in '${checked(table)}'
                ${clauses}
            }
        """
    }

    private static Object evaluate(String script, Map binding) {
        new GroovyShell(new Binding(binding)).evaluate(script)
    }

    private static List project(List rows, List<String> columns) {
        rows.collect { row -> columns.collect { name -> row[name] } }
    }

    private static String checked(String table) {
        assert table ==~ /[A-Za-z_][A-Za-z0-9_]*/
        table
    }

    private void store(String name, List<Map> values) {
        db.execute "create table ${checked(name)} (n int)".toString()
        values.each { row ->
            db.execute "insert into ${checked(name)} (n) values (?)", [row.n]
        }
    }

    private static List<Map> rows(int... values) {
        values.collect { [n: it] }
    }

    private static void agree(List actual, List expected) {
        assert expected.size() == actual.size()
        expected.eachWithIndex { Object want, int i ->
            same(want, actual[i])
        }
    }

    private static void same(Object want, Object got) {
        if (want instanceof List) {
            List wanted = (List) want
            List found = got instanceof List ? (List) got : got.toList()
            assert wanted.size() == found.size()
            wanted.eachWithIndex { Object cell, int j -> same(cell, found[j]) }
            return
        }
        if (want == null) {
            assert got == null
            return
        }
        if (want instanceof Number) {
            assert got instanceof Number
            assert (((Number) want) as BigDecimal).subtract(((Number) got) as BigDecimal).abs() < 0.0000001G
            return
        }
        assert want == got
    }
}
