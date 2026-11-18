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

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.SQLException
import java.util.Locale

import static groovy.test.GroovyAssert.shouldFail

/**
 * GINQ SQL queries compiled from source while the test runs.
 * Each script selects its provider and dialect itself.
 */
class GinqSqlRuntimeCompilationTest {
    private Sql realDb
    private GinqSqlTranslationTest.CapturingSql db

    @BeforeEach
    void setUp() {
        realDb = GinqSqlTestSupport.newPopulatedDb()
        db = new GinqSqlTranslationTest.CapturingSql(realDb)
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(realDb)
    }

    @Test
    void fullJoinOnH2KeepsBothUnmatchedSides() {
        realDb.execute("insert into departments (id, name) values (9, 'Empty')")
        realDb.execute("insert into departments (id, name) values (10, 'Vacant')")
        def rows = eval('''
            GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
                from e in 'employees'
                fulljoin d in 'departments' on e.deptId == d.id
                select e.name as en, d.name as dn
            }
        ''')
        def upper = db.sqlText.toUpperCase(Locale.ROOT)
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
    void fullJoinChainOrdersLimitsAndThenRightJoins() {
        realDb.execute('create table aa (id integer)')
        realDb.execute('create table bb (id integer)')
        realDb.execute('create table cc (id integer)')
        realDb.execute('insert into aa (id) values (1), (2)')
        realDb.execute('insert into bb (id) values (1), (3)')
        realDb.execute('insert into cc (id) values (3), (4)')
        def limited = eval('''
            GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
                from a in 'aa'
                fulljoin b in 'bb' on a.id == b.id
                fulljoin c in 'cc' on b.id == c.id
                orderby a.id, b.id, c.id
                limit 3
                select a.id as aId, b.id as bId, c.id as cId
            }
        ''')
        def upper = db.sqlText.toUpperCase(Locale.ROOT)
        assert !upper.contains('FULL OUTER JOIN')
        assert upper.contains('UNION ALL')
        assert upper.contains('ORDER BY')
        assert upper.contains('FETCH')
        assert db.sqlText.indexOf('UNION ALL') < upper.indexOf('ORDER BY')
        assert [[1, 1, null], [2, null, null], [null, 3, 3]] == limited.collect { [it.aId, it.bId, it.cId] }

        def right = eval('''
            GQL(provider: 'native-sql', dialect: 'h2', dataSource: db) {
                from a in 'aa'
                fulljoin b in 'bb' on a.id == b.id
                rightjoin c in 'cc' on b.id == c.id
                select a.id as aId, b.id as bId, c.id as cId
            }
        ''')
        assert !db.sqlText.toUpperCase(Locale.ROOT).contains('FULL OUTER JOIN')
        assert [[null, 3, 3], [null, null, 4]] as Set == right.collect { [it.aId, it.bId, it.cId] } as Set
    }

    @Test
    void fullJoinProjectsWholeRowsAndNestedPredicates() {
        realDb.execute("insert into departments (id, name) values (9, 'Empty')")
        def rows = eval('''
            GQL(provider: 'native-sql', dialect: 'h2', mixedMode: true, dataSource: db) {
                from e in 'employees'
                fulljoin d in 'departments' on e.deptId == d.id
                where e.salary > 0 && (from x in 'employees' where x.id == e.id select x.id).exists() && e.deptId in (from d2 in 'departments' where d2.id > 0 select d2.id) && e.salary >= (from z in 'employees' select min(z.salary))
                select e, d.name as dn, (e.salary > 4000 ? e.name.toUpperCase() : e.name) as label
            }
        ''')
        def upper = db.sqlText.toUpperCase(Locale.ROOT)
        assert !upper.contains('FULL OUTER JOIN')
        assert upper.contains('UNION ALL')
        assert upper.contains('EXISTS')
        assert upper.contains(' IN (')
        assert upper.contains('CASE')
        assert rows.any { cell(it, 'e_NAME') == 'Alice' && cell(it, 'dn') == 'Dev' && cell(it, 'label') == 'ALICE' }
        assert rows.every { cell(it, 'e_NAME') != 'Dave' }
    }

    @Test
    void pushedOuterJoinKeepsANullAliasForTheInMemoryTail() {
        def rows = eval('''
            GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
                from e in 'employees'
                leftjoin d in 'departments' on e.deptId == d.id
                orderby e.name
                select e.name, d?.name?.reverse()
            }
        ''')
        assert [['Alice', 'veD'], ['Bob', 'veD'], ['Carol', 'selaS'], ['Dave', null], ['Eve', 'selaS'], ['Frank', 'troppuS']] ==
                rows.toList()
    }

    @Test
    void derivedTablesSetOperationsAndWindowsExecuteOnH2() {
        def derived = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from v in (from e in 'employees' where e.salary >= 4000 select e.name, e.salary)
                orderby v.name
                select v.name
            }
        ''')
        assert ['Alice', 'Carol', 'Eve', 'Frank'] == derived.collect { it.name }
        assert db.sqlText.contains('SELECT')

        def combined = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from a in 'employees'
                where a.deptId == 1
                select a.name
                union
                from b in 'employees'
                where b.deptId == 2
                orderby b.name
                limit 1, 1
                select b.name
            }
        ''')
        assert db.sqlText.toUpperCase(Locale.ROOT).contains('UNION')
        assert db.sqlText.toUpperCase(Locale.ROOT).contains('ORDER BY')
        assert ['Bob'] == combined.collect { it.name }

        def overlap = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from a in 'employees'
                where a.salary >= 5000
                select a.name
                intersect
                from b in 'employees'
                where b.deptId == 1
                select b.name
            }
        ''')
        assert ['Alice'] == overlap.collect { it.name }
        assert db.sqlText.toUpperCase(Locale.ROOT).contains('INTERSECT')

        def remaining = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from a in 'employees'
                where a.deptId == 1
                select a.name
                minus
                from b in 'employees'
                where b.salary < 4000
                select b.name
            }
        ''')
        assert ['Alice'] == remaining.collect { it.name }
        assert db.sqlText.toUpperCase(Locale.ROOT).contains('EXCEPT')

        def numbered = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.deptId == 1
                orderby e.name
                select e.name, (rowNumber() over(orderby e.name)) as rn,
                       (sum(e.salary) over(partitionby e.deptId orderby e.salary rows -1, 0)) as near
            }
        ''')
        assert db.sqlText.contains('ROW_NUMBER() OVER')
        assert db.sqlText.contains('ROWS')
        assert [['Alice', 0], ['Bob', 1]] == numbered.collect { [it.name, it.rn] }
        assert numbered.every { it.near instanceof Number }
    }

    @Test
    void mysqlSqliteAndPostgresRenderThroughTheProvider() {
        def sink = new RecordingSql(realDb)
        eval(sink, '''
            def bound = 1
            def rows = GQL(provider: 'native-sql', dialect: 'mysql', dataSource: db) {
                from e in 'employees'
                where e.name == bound && e.salary != bound
                orderby e.name
                limit 1, 2
                select e.name.length() as n
            }
            rows
        ''')
        def mysql = sink.sqlText
        assert mysql.contains('<=>')
        assert mysql.contains('NOT (')
        assert mysql.contains('LIMIT')
        assert mysql.contains('OFFSET')
        assert mysql.contains('`employees`')
        assert mysql.contains('CHAR_LENGTH')

        sink = new RecordingSql(realDb)
        eval(sink, '''
            def bound = 1
            def rows = GQL(provider: 'native-sql', dialect: 'sqlite', dataSource: db) {
                from e in 'employees'
                where e.name == bound && e.salary != bound
                orderby e.name
                limit 2
                select e.name.length() as n, (e.salary / 2) as half
            }
            rows
        ''')
        def sqlite = sink.sqlText
        assert sqlite.contains(' IS ')
        assert sqlite.contains(' IS NOT ')
        assert sqlite.contains('LIMIT')
        assert sqlite.contains('LENGTH(')
        assert sqlite.contains('REAL')

        sink = new RecordingSql(realDb)
        eval(sink, '''
            def rows = GQL(provider: 'native-sql', dialect: 'postgres', dataSource: db) {
                from e in 'employees'
                where e.name == 'Ada'
                orderby e.salary in desc
                limit 1, 2
                select e.salary / 2 as half
            }
            rows
        ''')
        def postgres = sink.sqlText
        assert postgres.contains('"employees"')
        assert postgres.contains('FETCH')
        assert postgres.contains('DOUBLE PRECISION')
        assert postgres.contains('IS NOT DISTINCT FROM')
    }

    @Test
    void jooqRendersJoinsWindowsCastsAndSetOperations() {
        def rows = eval('''
            def threshold = 2500
            def floor = 4500
            def needle = 'li'
            def listed = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
                from e in 'employees'
                join d in 'departments' on e.deptId == d.id
                where e.salary > threshold && e.name.contains('a')
                orderby e.salary in desc, e.name
                limit 1, 2
                select e.name, d.name as deptName,
                       cast(e.id, 'DECIMAL') / 2 as half,
                       e.name.indexOf(needle) as at,
                       (rowNumber() over(partitionby e.deptId orderby e.name)) as rn
            }
            def combined = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
                from a in 'employees'
                where a.deptId == 1
                select a.name, a.salary
                union
                from b in 'employees'
                where b.deptId == 2
                orderby b.salary in desc, b.name
                limit 1, 2
                select b.name, b.salary
            }
            [listed, combined]
        ''')
        assert db.sqlText.toLowerCase(Locale.ROOT).contains('union')
        assert db.sqlText.toLowerCase(Locale.ROOT).contains('offset')
        assert rows[0].size() <= 2
        assert rows[1].size() <= 2
        assert rows[0].every { it.name && it.deptName && it.half instanceof Number && it.at instanceof Number }
    }

    @Test
    void plusCastSubstringAndRangesFollowTheOperandKinds() {
        def rows = eval('''
            String suffix = '!'
            int bonus = 1
            def rows = GQL(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.id == 1
                select e.name + (suffix as String) as label, e.salary + (bonus as int) as pay,
                       (e.salary as long) as wide, (e.name as String) as text,
                       e.name.substring(1, 3) as mid, e.name.length() as n,
                       Math.max(e.salary, 1000) as hi, -e.salary as neg
            }
            rows
        ''')
        assert rows.size() == 1
        assert rows[0].label == 'Alice!'
        assert rows[0].pay == 5001
        assert rows[0].mid == 'li'
        assert rows[0].n == 5
        assert rows[0].hi == 5000
        assert rows[0].neg == -5000
        assert db.sqlText.contains('||')
        assert db.sqlText.contains('BIGINT')
        assert db.sqlText.contains('VARCHAR')

        def names = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.salary in 4000..5000 && e.deptId in [1, null] && e.name !in ['Bob']
                orderby e.name
                select e.name
            }
        ''')
        assert names.collect { it.name } == ['Alice']

        def added = eval('''
            GQL(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where (e.salary - 1000) in (from x in 'employees' select x.salary)
                orderby e.name
                select e.name
            }
        ''')
        assert added.collect { it.name }.toSet() == ['Alice', 'Bob', 'Carol', 'Eve', 'Frank'] as Set
        assert db.sqlText.toUpperCase(Locale.ROOT).contains(' IN (')

        def both = shouldFail {
            new GroovyShell(new Binding([db: db])).evaluate('''
                GQL(provider: 'native-sql', dataSource: db) {
                    from e in 'employees'
                    select e.name + e.salary
                }
            ''')
        }
        assert both.message.contains('not translated')

        def table = shouldFail {
            new GroovyShell(new Binding([db: db])).evaluate('''
                def table = 'employees'
                GQL(provider: 'native-sql', dataSource: db) {
                    from e in table
                    select e.name
                }
            ''')
        }
        assert table.message.contains('string literal')
    }

    private Object eval(String script) {
        eval(db, script)
    }

    private static Object eval(Object dataSource, String script) {
        new GroovyShell(new Binding([db: dataSource])).evaluate(script)
    }

    private static Object cell(row, String key) {
        def found = row.keySet().find { String name -> name.equalsIgnoreCase(key) }
        return found == null ? null : row[found]
    }

    static class RecordingSql extends Sql {
        String sqlText
        List params

        RecordingSql(Sql parent) {
            super(parent)
        }

        @Override
        List<GroovyRowResult> rows(String sql, List<?> params) throws SQLException {
            this.sqlText = sql
            this.params = new ArrayList(params)
            return []
        }
    }
}
