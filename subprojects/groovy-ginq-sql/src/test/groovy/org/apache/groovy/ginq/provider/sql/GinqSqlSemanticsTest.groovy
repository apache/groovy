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
import org.apache.groovy.ginq.provider.sql.render.Dialects
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.SQLException

import static groovy.test.GroovyAssert.shouldFail

/**
 * The cases called out for the SQL backend: mixed-mode reconstruction,
 * null comparisons, operators whose type cannot be decided, and dialect
 * push-down.
 */
class GinqSqlSemanticsTest {
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
    void testJoinedSourcesKeepBothNameColumns() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            where e.name == 'Alice'
            orderby e.name
            select e.name.reverse(), d.name.reverse()
        }
        assert [['ecilA', 'veD']] == result.toList()
    }

    @Test
    void testMixedJoinKeyDoesNotHideAColumnOfTheSameName() {
        realDb.execute('create table tags (__ginq_mixed_key varchar(20), name varchar(50), deptId integer)')
        realDb.execute("insert into tags (__ginq_mixed_key, name, deptId) values ('keep-me', 'Dev', 1)")
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: realDb) {
            from e in 'employees'
            join t in 'tags' on e.deptId == t.deptId
            where e.name == 'Alice'
            select e.name.reverse(), t.__ginq_mixed_key
        }
        assert [['ecilA', 'keep-me']] == result.toList()
    }

    @Test
    void testDuplicateSourceLabelsAreRejected() {
        def duplicate = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect(
                    'SELECT e.*, d.* FROM employees e INNER JOIN departments d ON e.deptId = d.id',
                    [['name', 'name'], ['id']],
                    '"' as char)
        }
        assert duplicate.message.contains('duplicate column labels')
        assert duplicate.message.contains('name')
    }

    @Test
    void testStarProjectionIsRewrittenToNamedColumns() {
        def sql = SqlGinqRuntime.rewriteStarSelect(
                'SELECT e.*, d.* FROM employees e INNER JOIN departments d ON e.deptId = d.id WHERE e.name = ?',
                [['id', 'na"me'], ['id', 'name']],
                '"' as char)
        assert 'SELECT e."id" AS "__ginq0_0", e."na""me" AS "__ginq0_1", d."id" AS "__ginq1_0", d."name" AS "__ginq1_1" ' +
                'FROM employees e INNER JOIN departments d ON e.deptId = d.id WHERE e.name = ?' == sql

        def nested = SqlGinqRuntime.rewriteStarSelect(
                'SELECT v.* FROM (SELECT name FROM employees) v WHERE v.name = ?',
                [['name']],
                '"' as char)
        assert 'SELECT v."name" AS "__ginq0_0" FROM (SELECT name FROM employees) v WHERE v.name = ?' == nested
    }

    @Test
    void testNullParameterMatchesNullColumn() {
        def wanted = null
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId == wanted
            orderby e.name
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NOT DISTINCT FROM ? ORDER BY e.name ASC NULLS LAST' == db.sqlText
        assert [null] == db.params
        assert ['Dave'] == result.collect { it.name }

        def present = 1
        db.sqlTexts.clear()
        def named = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId == present
            orderby e.name
            select e.name
        }
        assert db.sqlText.contains('IS NOT DISTINCT FROM')
        assert ['Alice', 'Bob'] == named.collect { it.name }
    }

    @Test
    void testNotInRangeIncludesNull() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId !in 1..3
            orderby e.name
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NULL OR e.deptId < ? OR e.deptId > ? ORDER BY e.name ASC NULLS LAST' == db.sqlText
        assert [1, 3] == db.params
        assert ['Dave'] == result.collect { it.name }
    }

    @Test
    void testNotInListIncludesNull() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId !in [1, 2]
            orderby e.name
            select e.name
        }
        assert db.sqlText.contains('e.deptId IS NULL OR e.deptId NOT IN (?, ?)')
        assert ['Dave', 'Frank'] == result.collect { it.name }
    }

    @Test
    void testComparisonDoesNotTreatNullAsLessThanAValue() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId < 1
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId < ?' == db.sqlText
        assert [] == result.toList()
    }

    @Test
    void testCapturedStringPlusIsNotArithmetic() {
        def err = shouldFail '''\
            def suffix = '!'
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.name + suffix
            }
        '''
        assert err.message.contains('Operator `+`')
        assert err.message.contains('mixedMode: true')

        def suffix = '!'
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.name == 'Bob'
            select e.name + suffix
        }
        assert 'SELECT e.* FROM employees e WHERE e.name IS NOT DISTINCT FROM ?' == db.sqlText
        assert ['Bob'] == db.params
        assert ['Bob!'] == result.toList()
    }

    @Test
    void testStringCastAndNumericAnchorStillPushDown() {
        def suffix = '!'
        def bonus = 500
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.name == 'Bob'
            select e.name + (suffix as String) as decorated, e.salary + (bonus as int) as bumped
        }
        assert db.sqlText.contains('e.name || ?')
        assert db.sqlText.contains('e.salary + ?')
        assert !db.sqlText.contains('e.name +')
        assert [['Bob!', 3500]] == result.collect { [it.decorated, it.bumped] }
    }

    @Test
    void testTwoColumnsAreNotPushedForPlus() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.salary + e.deptId
            }
        '''
        assert err.message.contains('Operator `+`')
        assert err.message.contains('Two columns')
    }

    @Test
    void testCapturedTextMethodIsNotPushed() {
        def err = shouldFail '''\
            def suffix = '!'
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select suffix.length()
            }
        '''
        assert err.message.contains('`length()`')
        assert err.message.contains('captured variable')

        def suffix = '!'
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.name == 'Alice'
            select suffix.length()
        }
        assert [1] == result.toList()
    }

    @Test
    void testSqliteFloorStaysOutOfPushedSql() {
        def recorded = new RecordingSql(realDb)
        def rows = GQL(provider: 'native-sql', dialect: 'sqlite', mixedMode: true, dataSource: recorded) {
            from e in 'employees'
            where e.salary > 1000
            select e.salary.floor()
        }
        assert [] == rows.toList()
        assert 1 == recorded.sqlTexts.size()
        assert !recorded.sqlTexts[0].contains('FLOOR')
        assert recorded.sqlTexts[0].contains('WHERE')
        assert recorded.sqlTexts[0].contains('salary')

        def length = new RecordingSql(realDb)
        GQL(provider: 'native-sql', dialect: 'sqlite', dataSource: length) {
            from e in 'employees'
            select e.name.length()
        }
        assert length.sqlTexts[0].contains('LENGTH(')
        assert !length.sqlTexts[0].contains('CHAR_LENGTH')
        assert !length.sqlTexts[0].contains('FLOOR')
    }

    @Test
    void testJooqSqliteUsesTheSameFunctionSupport() {
        def recorded = new RecordingSql(realDb)
        GQL(provider: 'jooq-sql', dialect: 'SQLITE', mixedMode: true, dataSource: recorded) {
            from e in 'employees'
            where e.salary > 1000
            select e.salary.ceil()
        }
        assert 1 == recorded.sqlTexts.size()
        assert !recorded.sqlTexts[0].toUpperCase(Locale.ROOT).contains('CEIL')
        assert recorded.sqlTexts[0].toLowerCase(Locale.ROOT).contains('where')
    }

    @Test
    void testFunctionSupportIsShared() {
        assert !FunctionSupport.supports('sqlite', 'FLOOR')
        assert !FunctionSupport.supports('SQLITE', 'CEILING')
        assert FunctionSupport.supports('sqlite', 'CHAR_LENGTH')
        assert FunctionSupport.supports('sqlite', 'UPPER')
        assert FunctionSupport.supports('h2', 'FLOOR')
        assert FunctionSupport.supports('postgres', 'CEILING')
        assert !Dialects.resolve('sqlite').supportsFunction('FLOOR')
        assert Dialects.resolve('sqlite').supportsFunction('UPPER')
        assert Dialects.resolve('h2').supportsFunction('CEILING')
        assert 'sqlite' == SqlCapabilities.of('SQLITE').dialectName()
    }

    private static class RecordingSql extends Sql {
        final List<String> sqlTexts = []

        RecordingSql(Sql parent) {
            super(parent)
        }

        @Override
        List<GroovyRowResult> rows(String sql, List<?> params) {
            sqlTexts << sql
            return []
        }
    }
}
