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

import java.sql.SQLException

import static groovy.test.GroovyAssert.shouldFail

import java.util.Locale
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Mixed-mode outer joins must expose an unmatched alias as null, the same
 * way the collection provider does, including when later clauses stay in memory.
 */
class GinqSqlMixedOuterJoinTest {
    private Sql db

    @BeforeEach
    void setUp() {
        db = GinqSqlTestSupport.newPopulatedDb()
        db.execute("insert into departments (id, name) values (9, 'Empty')")
        db.execute("insert into departments (id, name) values (10, 'Vacant')")
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void testLeftJoinUnmatchedAliasIsNull() {
        def employees = db.rows('select * from employees')
        def departments = db.rows('select * from departments')
        def memory = GQL {
            from e in employees
            leftjoin d in departments on e.deptId == d.id
            orderby e.name, (d == null ? 'missing' : d.name)
            select e.name, (d == null ? 'missing' : d.name), d == null, d != null
        }.toList()
        def sql = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            leftjoin d in 'departments' on e.deptId == d.id
            orderby e.name, (d == null ? 'missing' : d.name)
            select e.name, (d == null ? 'missing' : d.name), d == null, d != null
        }.toList()
        assert memory == sql
        assert ['Dave', 'missing', true, false] in sql
        assert sql.findAll { it[2] } == [['Dave', 'missing', true, false]]
        assert sql.findAll { !it[2] }.every { it[3] && it[1] != 'missing' }
    }

    @Test
    void testRightJoinUnmatchedAliasIsNull() {
        def employees = db.rows('select * from employees')
        def departments = db.rows('select * from departments')
        def memory = GQL {
            from e in employees
            rightjoin d in departments on e.deptId == d.id
            orderby (e == null ? 'missing' : e.name), d.name
            select (e == null ? 'missing' : e.name), d.name, e == null, e != null
        }.toList()
        def sql = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            rightjoin d in 'departments' on e.deptId == d.id
            orderby (e == null ? 'missing' : e.name), d.name
            select (e == null ? 'missing' : e.name), d.name, e == null, e != null
        }.toList()
        assert memory == sql
        assert ['missing', 'Empty', true, false] in sql
        assert ['missing', 'Vacant', true, false] in sql
        assert sql.findAll { it[2] }.collect { it[1] }.sort() == ['Empty', 'Vacant']
        assert !sql.any { it[0] == 'Dave' }
    }

    @Test
    void testFullJoinUnmatchedAliasIsNullOnEitherSide() {
        def employees = db.rows('select * from employees')
        def departments = db.rows('select * from departments')
        def excluded = -1
        def memory = GQL {
            from e in employees
            fulljoin d in departments on e.deptId == d.id && d.id != excluded
            orderby (e == null ? 'missing' : e.name), (d == null ? 'missing' : d.name)
            select (e == null ? 'missing' : e.name), (d == null ? 'missing' : d.name), e == null, d == null
        }.toList()
        def sql = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id && d.id != excluded
            orderby (e == null ? 'missing' : e.name), (d == null ? 'missing' : d.name)
            select (e == null ? 'missing' : e.name), (d == null ? 'missing' : d.name), e == null, d == null
        }.toList()
        assert memory == sql
        assert ['Dave', 'missing', false, true] in sql
        assert ['missing', 'Empty', true, false] in sql
        assert ['missing', 'Vacant', true, false] in sql
        assert !sql.any { it[2] && it[3] }
    }

    @Test
    void testHashOuterJoinsUseTheSameNullAlias() {
        def employees = db.rows('select * from employees')
        def departments = db.rows('select * from departments')
        def memory = GQL {
            from e in employees
            lefthashjoin d in departments on e.deptId == d.id
            orderby e.name
            select e.name, (d == null ? 'missing' : d?.name), d == null
        }.toList()
        def sql = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            lefthashjoin d in 'departments' on e.deptId == d.id
            orderby e.name
            select e.name, (d == null ? 'missing' : d?.name), d == null
        }.toList()
        assert memory == sql
        assert ['Dave', 'missing', true] in sql

        def right = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            righthashjoin d in 'departments' on e.deptId == d.id
            select (e == null ? 'missing' : e.name), d.name, e == null, e != null
        }.toList()
        assert right.findAll { it[2] }.collect { it[1] }.sort() == ['Empty', 'Vacant']
        assert right.findAll { it[2] }.every { it[0] == 'missing' && !it[3] }
    }

    @Test
    void testInnerAndCrossJoinsDoNotInventNullAliases() {
        def inner = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            orderby e.name, d.name
            select e.name, (d == null), (d != null)
        }.toList()
        assert inner.every { !it[1] && it[2] }
        assert !inner.any { it[0] == 'Dave' }
        assert 5 == inner.size()

        def crossed = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            crossjoin d in 'departments'
            select e == null, d == null
        }.toList()
        assert 6 * 5 == crossed.size()
        assert crossed.every { !it[0] && !it[1] }
    }

    @Test
    void testAllNullColumnsStayAPresentRow() {
        db.execute('create table anchors (id integer)')
        db.execute('insert into anchors (id) values (1)')
        db.execute('create table blanks (id integer, note varchar(20))')
        db.execute('insert into blanks (id, note) values (null, null)')

        def present = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from a in 'anchors'
            join b in 'blanks' on a.id == 1
            select b == null, b != null, b.id, b.note
        }.toList()
        assert [[false, true, null, null]] == present

        def absent = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from a in 'anchors'
            leftjoin b in 'blanks' on a.id == b.id
            select b == null, b != null, (b == null ? 'missing' : b.note)
        }.toList()
        assert [[true, false, 'missing']] == absent
    }

    @Test
    void testPresenceNameDoesNotHideARealColumn() {
        db.execute('create table marks (__ginq_present integer, name varchar(20))')
        db.execute("insert into marks (__ginq_present, name) values (7, 'kept')")
        db.execute('create table anchors (id integer)')
        db.execute('insert into anchors (id) values (1)')

        def present = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from a in 'anchors'
            join m in 'marks' on a.id == 1
            select m == null, m.__ginq_present, m.name
        }.toList()
        assert [[false, 7, 'kept']] == present

        def absent = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from a in 'anchors'
            leftjoin m in 'marks' on 1 == 0
            select m == null, (m == null ? 'missing' : m.name)
        }.toList()
        assert [[true, 'missing']] == absent
    }

    @Test
    void testUserPresenceColumnIsNotRewrittenToTheMarker() {
        db.execute('create table people (id integer, name varchar(20))')
        db.execute("insert into people (id, name) values (1, 'Alice'), (7, 'Gina'), (8, 'Hugo')")
        db.execute('create table marks ("__ginq_present" integer, name varchar(20))')
        db.execute('insert into marks ("__ginq_present", name) values (7, \'seven\'), (8, \'eight\')')
        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'people'
            leftjoin m in 'marks' on e.id == m.__ginq_present
            select e.name, (m == null ? 'missing' : m.name)
        }.toList()
        assert ['Gina', 'seven'] in rows.collect { [it[0], it[1]] }
        assert ['Hugo', 'eight'] in rows.collect { [it[0], it[1]] }
        assert ['Alice', 'missing'] in rows.collect { [it[0], it[1]] }
        assert !rows.any { it[0] == 'Alice' && it[1] != 'missing' }
    }

    @Test
    void testCompleteFullJoinKeepsAUserPresenceColumn() {
        db.execute('create table people (id integer, name varchar(20))')
        db.execute("insert into people (id, name) values (1, 'Alice'), (7, 'Gina'), (8, 'Hugo')")
        db.execute('create table marks ("__ginq_present" integer, name varchar(20))')
        db.execute('insert into marks ("__ginq_present", name) values (7, \'seven\'), (8, \'eight\')')
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'people'
            fulljoin m in 'marks' on e.id == m.__ginq_present
            select e.name as en, m.name as mn, m.__ginq_present as mp
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert captured.sqlText.contains('1 AS "__ginq_marker"')
        assert !captured.sqlText.contains('1 AS "__ginq_present"')
        assert [['Alice', null, null], ['Gina', 'seven', 7], ['Hugo', 'eight', 8]] as Set ==
                rows.collect { [it.en, it.mn, it.mp] } as Set
        assert 3 == rows.size()
    }

    @Test
    void testSeveralUnmatchedAliasesStayIndexAligned() {
        db.execute('create table anchors (id integer, name varchar(20))')
        db.execute("insert into anchors (id, name) values (1, 'one'), (2, 'two')")

        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from a in 'anchors'
            leftjoin b in 'departments' on 1 == 0
            leftjoin c in 'departments' on 1 == 0
            orderby a.name
            select a.name, b == null, c == null, (b == null ? 'no-b' : b.name), (c == null ? 'no-c' : c.name)
        }.toList()
        assert [['one', true, true, 'no-b', 'no-c'], ['two', true, true, 'no-b', 'no-c']] == rows
    }

    @Test
    void testInMemoryTailSeesNullAlias() {
        def labels = [[dept: 'Dev', tag: 'code'], [dept: 'none', tag: 'gap']]
        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            leftjoin d in 'departments' on e.deptId == d.id
            innerjoin t in labels on (d == null ? 'none' : d.name) == t.dept
            where e.name == 'Alice' || e.name == 'Dave'
            orderby e.name
            select e.name, (d == null ? 'missing' : d.name), t.tag, d == null, d != null
        }.toList()
        assert [['Alice', 'Dev', 'code', false, true], ['Dave', 'missing', 'gap', true, false]] == rows
    }

    @Test
    void testGroupByAndOrderByOnANullAlias() {
        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            rightjoin d in 'departments' on e.deptId == d.id
            groupby (e == null ? 'missing' : e.name) into g
            orderby g.key
            select g.key, g.count()
        }.toList()
        def missing = rows.find { it[0] == 'missing' }
        assert missing != null
        assert 2 == missing[1]
        assert rows.any { it[0] == 'Alice' && it[1] == 1 }
    }

    @Test
    void testCompleteFullJoinIncludesBothUnmatchedSides() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e.name as en, d.name as dn
        }.toList()
        def upper = captured.sqlText.toUpperCase(Locale.ROOT)
        assert !upper.contains('FULL OUTER JOIN')
        assert !upper.contains('FULL JOIN')
        assert upper.contains('UNION ALL')
        assert rows.any { it.en == 'Dave' && it.dn == null }
        assert rows.any { it.en == null && it.dn == 'Empty' }
        assert rows.any { it.en == null && it.dn == 'Vacant' }
        assert rows.any { it.en == 'Alice' && it.dn == 'Dev' }
        assert 8 == rows.size()
    }

    @Test
    void testCompleteFullJoinChainOrderAndLimit() {
        db.execute('create table aa (id integer)')
        db.execute('create table bb (id integer)')
        db.execute('create table cc (id integer)')
        db.execute('insert into aa (id) values (1), (2)')
        db.execute('insert into bb (id) values (1), (3)')
        db.execute('insert into cc (id) values (3), (4)')
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def limited = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            fulljoin c in 'cc' on b.id == c.id
            orderby a.id, b.id, c.id
            limit 3
            select a.id as aId, b.id as bId, c.id as cId
        }.toList()
        def upper = captured.sqlText.toUpperCase(Locale.ROOT)
        assert !upper.contains('FULL OUTER JOIN')
        assert !upper.contains('FULL JOIN')
        assert upper.contains('UNION ALL')
        assert upper.contains('ORDER BY')
        assert upper.contains('FETCH')
        assert !upper.contains('ORDER BY A.ID')
        assert captured.sqlText.indexOf('UNION ALL') < upper.indexOf('ORDER BY')
        assert [[1, 1, null], [2, null, null], [null, 3, 3]] == limited.collect { [it.aId, it.bId, it.cId] }

        def all = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            fulljoin c in 'cc' on b.id == c.id
            select a.id as aId, b.id as bId, c.id as cId
        }.toList()
        assert 4 == all.size()
        assert all.any { it.aId == null && it.bId == null && it.cId == 4 }
    }

    @Test
    void testCompleteFullJoinWhereStaysInSql() {
        def floor = 4000
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where e.salary > floor
            select e.name as en, d.name as dn
        }.toList()
        assert captured.sqlText.toUpperCase(Locale.ROOT).contains('UNION ALL')
        assert !captured.sqlText.toUpperCase(Locale.ROOT).contains('FULL JOIN')
        assert !captured.sqlText.toUpperCase(Locale.ROOT).contains('FULL OUTER JOIN')
        assert [4000] == captured.params
        assert ['Alice', 'Eve', 'Frank'] as Set == rows.collect { it.en } as Set
        assert rows.every { it.dn != null }
        assert 3 == rows.size()
    }

    @Test
    void testCompleteFullJoinCorrelatedExistsProjectsTheOuterColumn() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where (from x in 'employees' where x.name == e.name && x.salary > 4500 select x.id).exists()
            select e.id as eid, d.name as dn
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert [4500] == captured.params
        assert [[1, 'Dev'], [5, 'Sales'], [6, 'Support']] as Set == rows.collect { [it.eid, it.dn] } as Set
        assert 3 == rows.size()
    }

    @Test
    void testCompleteFullJoinInSubqueryProjectsTheOuterColumn() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where e.name in (from x in 'employees' where x.salary > 4500 select x.name)
            select e.id as eid, d.name as dn
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert [4500] == captured.params
        assert [[1, 'Dev'], [5, 'Sales'], [6, 'Support']] as Set == rows.collect { [it.eid, it.dn] } as Set
        assert 3 == rows.size()
    }

    @Test
    void testCompleteFullJoinScalarSubqueryProjectsTheOuterColumn() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e.name as en, (from x in 'departments' where x.id == e.id select x.name) as xn
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        def named = rows.findAll { it.en != null }.collectEntries { [it.en, it.xn] }
        assert ['Alice': 'Dev', 'Bob': 'Sales', 'Carol': 'Support', 'Dave': null, 'Eve': null, 'Frank': null] == named
        assert 2 == rows.count { it.en == null && it.xn == null }
        assert 8 == rows.size()
    }

    @Test
    void testCompleteFullJoinSubqueryGroupAndOrderProjectTheOuterColumn() {
        def ordered = new GinqSqlTranslationTest.CapturingSql(db)
        def byOrder = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: ordered) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where (from x in 'employees' where x.id == e.id orderby e.salary select x.id).exists()
            select e.id as eid, d.name as dn
        }.toList()
        assertNoNativeFullJoin(ordered.sqlText)
        assert [[1, 'Dev'], [2, 'Dev'], [3, 'Sales'], [4, null], [5, 'Sales'], [6, 'Support']] as Set ==
                byOrder.collect { [it.eid, it.dn] } as Set
        assert 6 == byOrder.size()

        def grouped = new GinqSqlTranslationTest.CapturingSql(db)
        def byGroup = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: grouped) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where (from x in 'employees' where x.salary > 4500 groupby x.name, e.name having count() > 0 select x.name).exists()
            select e.id as eid, d.name as dn
        }.toList()
        assertNoNativeFullJoin(grouped.sqlText)
        assert [[1, 'Dev'], [2, 'Dev'], [3, 'Sales'], [4, null], [5, 'Sales'], [6, 'Support'], [null, 'Empty'], [null, 'Vacant']] as Set ==
                byGroup.collect { [it.eid, it.dn] } as Set
        assert 8 == byGroup.size()
    }

    private static void assertNoNativeFullJoin(String sql) {
        def upper = sql.toUpperCase(Locale.ROOT)
        assert upper.contains('UNION ALL')
        assert !upper.contains('FULL JOIN')
        assert !upper.contains('FULL OUTER JOIN')
    }

    @Test
    void testGStringJdbcUrlSelectsTheDialect() {
        def bucket = 'ginqmem'
        def h2 = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: keep(h2, "jdbc:h2:mem:${bucket}")) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e.name as en, d.name as dn
        }.toList()
        assertNoNativeFullJoin(h2.sqlText)
        assert rows.any { it.en == 'Alice' && it.dn == 'Dev' }
        assert rows.any { it.en == 'Dave' && it.dn == null }

        def host = 'db.example'
        def postgres = new GinqSqlTranslationTest.CapturingSql(db)
        shouldFail(SQLException) {
            GQL(provider: 'native-sql', mixedMode: true, dataSource: keep(postgres, "jdbc:postgresql://${host}/db")) {
                from e in 'employees'
                fulljoin d in 'departments' on e.deptId == d.id
                select e.name as en, d.name as dn
            }.toList()
        }
        assert postgres.sqlText.toUpperCase(Locale.ROOT).contains('FULL OUTER JOIN')
    }

    @Test
    void testOmittedDialectFullJoinRunsOnH2() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e.name as en, d.name as dn
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert rows.any { it.en == 'Dave' && it.dn == null }
        assert rows.any { it.en == null && it.dn == 'Empty' }
        assert rows.any { it.en == 'Alice' && it.dn == 'Dev' }
        assert 8 == rows.size()
    }

    @Test
    void testPlanSplitFullJoinSqlHasNoNativeFullJoin() {
        // The split prefix is executed through JDBC directly. H2 rejects
        // FULL JOIN, so a successful bag means the prefix was the nested union.
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select (e == null ? 'missing' : e.name), (d == null ? null : d.name)
        }.toList()
        assert rows.any { it[0] == 'missing' && it[1] == 'Empty' }
        assert rows.any { it[0] == 'Dave' && it[1] == null }
        assert rows.any { it[0] == 'Alice' && it[1] == 'Dev' }
    }

    @Test
    void testCompleteFullJoinWholeRowSelect() {
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: captured) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e, d
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert rows.every { !it.keySet().any { String key -> key.toLowerCase(Locale.ROOT).contains('present') } }
        assert rows.every { it.values().size() == 7 }
        def alice = rows.find { cell(it, 'e_NAME') == 'Alice' }
        assert cell(alice, 'd_NAME') == 'Dev'
        assert cell(alice, 'e_SALARY') == 5000
        def dave = rows.find { cell(it, 'e_NAME') == 'Dave' }
        assert cell(dave, 'e_SALARY') == 2000
        assert cell(dave, 'd_NAME') == null
        def empty = rows.find { cell(it, 'd_NAME') == 'Empty' }
        assert cell(empty, 'e_NAME') == null
        assert 8 == rows.size()

        def namedCapture = new GinqSqlTranslationTest.CapturingSql(db)
        def named = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: namedCapture) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            select e.name, d
        }.toList()
        def triples = named.collect { it.values().toList() }
        assert triples.any { it[0] == 'Dave' && it[1] == null }
        assert triples.any { it[0] == null && it[2] == 'Empty' }
        assert triples.any { it[0] == 'Alice' && it[2] == 'Dev' }
        assert triples.every { it.size() == 3 }
        def namedAlice = named.find { cell(it, 'name') == 'Alice' }
        assert cell(namedAlice, 'd_NAME') == 'Dev'
        def namedDave = named.find { cell(it, 'name') == 'Dave' }
        assert cell(namedDave, 'd_NAME') == null
        assert 8 == named.size()
        def lower = namedCapture.sqlText.toLowerCase(Locale.ROOT)
        assert lower.contains('__ginq_0_name')
        assert lower.contains('__ginqstarcol_0_name')
    }

    private static Object cell(row, String key) {
        def found = row.keySet().find { String name -> name.equalsIgnoreCase(key) }
        return found == null ? null : row[found]
    }

    static Sql keep(Sql db, String url) {
        return db
    }

    @Test
    void testStrictFullJoinChainRunsOnH2() {
        db.execute('create table aa (id integer)')
        db.execute('create table bb (id integer)')
        db.execute('create table cc (id integer)')
        db.execute('insert into aa (id) values (1), (2)')
        db.execute('insert into bb (id) values (1), (3)')
        db.execute('insert into cc (id) values (3), (4)')
        def captured = new GinqSqlTranslationTest.CapturingSql(db)
        def full = GQL(provider: 'native-sql', dialect: 'h2', dataSource: captured) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            fulljoin c in 'cc' on b.id == c.id
            select a.id as aId, b.id as bId, c.id as cId
        }.toList()
        assertNoNativeFullJoin(captured.sqlText)
        assert [[1, 1, null], [2, null, null], [null, 3, 3], [null, null, 4]] as Set ==
                full.collect { [it.aId, it.bId, it.cId] } as Set
        assert 4 == full.size()

        def rightCaptured = new GinqSqlTranslationTest.CapturingSql(db)
        def right = GQL(provider: 'native-sql', dialect: 'h2', dataSource: rightCaptured) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            rightjoin c in 'cc' on b.id == c.id
            select a.id as aId, b.id as bId, c.id as cId
        }.toList()
        assertNoNativeFullJoin(rightCaptured.sqlText)
        assert [[null, 3, 3], [null, null, 4]] as Set == right.collect { [it.aId, it.bId, it.cId] } as Set
        assert 2 == right.size()
    }

    @Test
    void testFullJoinOnlyInsideSubqueryRunsOnH2() {
        db.execute('create table nums (id integer)')
        db.execute('create table aa (id integer)')
        db.execute('create table bb (id integer)')
        db.execute('insert into nums (id) values (1), (2), (3)')
        db.execute('insert into aa (id) values (1), (2)')
        db.execute('insert into bb (id) values (1), (3)')
        def mixedExists = new GinqSqlTranslationTest.CapturingSql(db)
        def existsRows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: mixedExists) {
            from n in 'nums'
            where (from a in 'aa' fulljoin b in 'bb' on a.id == b.id where a.id == n.id select a.id).exists()
            select n.id as id
        }.toList()
        assertNoNativeFullJoin(mixedExists.sqlText)
        assert [1, 2] as Set == existsRows.collect { it.id } as Set

        def mixedIn = new GinqSqlTranslationTest.CapturingSql(db)
        def inRows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: mixedIn) {
            from n in 'nums'
            where n.id in (from a in 'aa' fulljoin b in 'bb' on a.id == b.id select a.id)
            select n.id as id
        }.toList()
        assertNoNativeFullJoin(mixedIn.sqlText)
        assert [1, 2] as Set == inRows.collect { it.id } as Set

        def mixedScalar = new GinqSqlTranslationTest.CapturingSql(db)
        def scalarRows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: mixedScalar) {
            from n in 'nums'
            select n.id as id, (from a in 'aa' fulljoin b in 'bb' on a.id == b.id where a.id == n.id select b.id) as bid
        }.toList()
        assertNoNativeFullJoin(mixedScalar.sqlText)
        assert [[1, 1], [2, null], [3, null]] as Set == scalarRows.collect { [it.id, it.bid] } as Set

        def strictExists = new GinqSqlTranslationTest.CapturingSql(db)
        def strictRows = GQL(provider: 'native-sql', dialect: 'h2', dataSource: strictExists) {
            from n in 'nums'
            where (from a in 'aa' fulljoin b in 'bb' on a.id == b.id where a.id == n.id select a.id).exists()
            select n.id as id
        }.toList()
        assertNoNativeFullJoin(strictExists.sqlText)
        assert [1, 2] as Set == strictRows.collect { it.id } as Set

        def strictIn = new GinqSqlTranslationTest.CapturingSql(db)
        GQL(provider: 'native-sql', dialect: 'h2', dataSource: strictIn) {
            from n in 'nums'
            where n.id in (from a in 'aa' fulljoin b in 'bb' on a.id == b.id select a.id)
            select n.id as id
        }
        assertNoNativeFullJoin(strictIn.sqlText)

        def strictScalar = new GinqSqlTranslationTest.CapturingSql(db)
        def strictScalarRows = GQL(provider: 'native-sql', dialect: 'h2', dataSource: strictScalar) {
            from n in 'nums'
            select n.id as id, (from a in 'aa' fulljoin b in 'bb' on a.id == b.id where a.id == n.id select b.id) as bid
        }.toList()
        assertNoNativeFullJoin(strictScalar.sqlText)
        assert [[1, 1], [2, null], [3, null]] as Set == strictScalarRows.collect { [it.id, it.bid] } as Set
    }

    @Test
    void testNestedFullJoinMatchesTheCollectionProvider() {
        db.execute('create table aa (id integer)')
        db.execute('create table bb (id integer)')
        db.execute('create table cc (id integer)')
        db.execute('insert into aa (id) values (1), (2)')
        db.execute('insert into bb (id) values (1), (3)')
        db.execute('insert into cc (id) values (3), (4)')
        // A full-joined with B, then with C: one row for each of (1,1), (2), (3,3) and (4).
        def expectFull = [
                [false, 1, false, 1, true, null],
                [false, 2, true, null, true, null],
                [true, null, false, 3, false, 3],
                [true, null, true, null, false, 4],
        ]
        def sqlFull = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            fulljoin c in 'cc' on b.id == c.id
            select (a == null), (a?.id) as aId, (b == null), (b?.id) as bId, (c == null), (c?.id) as cId
        }.toList()
        assert bag(expectFull) == bag(sqlFull)
        assert sqlFull.count { it[4] == false && it[5] == 4 } == 1
        assert sqlFull.count { it[2] == false && it[3] == 3 && it[5] == 3 } == 1

        def expectRight = [
                [true, null, false, 3, false, 3],
                [true, null, true, null, false, 4],
        ]
        def sqlRight = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            rightjoin c in 'cc' on b.id == c.id
            select (a == null), (a?.id) as aId, (b == null), (b?.id) as bId, (c == null), (c?.id) as cId
        }.toList()
        assert bag(expectRight) == bag(sqlRight)

        def sqlHash = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from a in 'aa'
            fullhashjoin b in 'bb' on a.id == b.id
            righthashjoin c in 'cc' on b.id == c.id
            select (a == null), (a?.id) as aId, (b == null), (b?.id) as bId, (c == null), (c?.id) as cId
        }.toList()
        assert bag(expectRight) == bag(sqlHash)

        def jooq = GQL(provider: 'jooq-sql', dialect: 'H2', mixedMode: true, dataSource: db) {
            from a in 'aa'
            fulljoin b in 'bb' on a.id == b.id
            fulljoin c in 'cc' on b.id == c.id
            select (a == null), (a?.id) as aId, (b == null), (b?.id) as bId, (c == null), (c?.id) as cId
        }.toList()
        assert bag(expectFull) == bag(jooq)
    }

    @Test
    void testFullJoinPushesTranslatableOrderAndLimit() {
        def employees = db.rows('select * from employees')
        def departments = db.rows('select * from departments')
        def joined = GQL {
            from e in employees
            fulljoin d in departments on e.deptId == d.id
            select (e == null ? null : e.name), (d == null ? null : d.name), (e == null), (d == null)
        }.toList()
        def memory = joined.sort { a, b ->
            int byName = compareNullsLast(a[0], b[0])
            byName != 0 ? byName : compareNullsLast(a[1], b[1])
        }.take(4)
        def sql = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            orderby e.name, d.name
            limit 4
            select (e == null ? null : e.name), (d == null ? null : d.name), (e == null), (d == null)
        }.toList()
        assert memory == sql
        assert 4 == sql.size()
        assert ['Alice', 'Dev', false, false] == sql[0]
        assert sql.every { it[2] == false }
    }

    @Test
    void testPushedWhereOnAFullJoinRepeatsBranchParameters() {
        def floor = 4000
        def rows = GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            fulljoin d in 'departments' on e.deptId == d.id
            where e.salary > floor
            orderby e.name
            select (e == null ? 'missing' : e.name), d == null
        }.toList()
        assert ['Alice', 'Eve', 'Frank'] == rows.collect { it[0] }
        assert rows.every { it[1] == false }
    }

    private static int compareNullsLast(Object left, Object right) {
        if (left == null && right == null) {
            return 0
        }
        if (left == null) {
            return 1
        }
        if (right == null) {
            return -1
        }
        return left.toString() <=> right.toString()
    }

    private static List<String> bag(List rows) {
        rows.collect { row -> (0..<row.size()).collect { row[it] }.toString() }.sort()
    }

    @Test
    void testJooqOuterJoinAliasIsNull() {
        def rows = GQL(provider: 'jooq-sql', dialect: 'H2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            rightjoin d in 'departments' on e.deptId == d.id
            orderby d.name, (e == null ? 'missing' : e.name)
            select (e == null ? 'missing' : e.name), d.name, e == null, e != null
        }.toList()
        assert rows.findAll { it[2] }.collect { it[1] }.sort() == ['Empty', 'Vacant']
        assert rows.findAll { it[2] }.every { it[0] == 'missing' && !it[3] }
        assert rows.any { it[0] == 'Alice' && it[1] == 'Dev' && !it[2] && it[3] }
    }
}
