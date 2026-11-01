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
 * Mixed-mode execution: the translatable clause prefix is pushed to H2 and
 * the rest runs through the collection provider. Strict mode stays the default.
 */
class GinqSqlMixedModeTest {
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
    void testFullyTranslatableQueryStaysSql() {
        def threshold = 2500
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > threshold
            orderby e.name
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.salary > ? ORDER BY e.name ASC NULLS LAST' == db.sqlText
        assert [2500] == db.params
        assert ['Alice', 'Bob', 'Carol', 'Eve', 'Frank'] == result.collect { it.name }
    }

    @Test
    void testSelectFallsBackAfterPushedFilterOrderAndLimit() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > 2000
            orderby e.salary in desc, e.name
            limit 2
            select e.name.reverse()
        }
        assert 'SELECT e.* FROM employees e WHERE e.salary > ? ' +
                'ORDER BY e.salary DESC NULLS LAST, e.name ASC NULLS LAST FETCH FIRST ? ROWS ONLY' == db.sqlText
        assert [2000, 2] == db.params
        assert ['knarF', 'ecilA'] == result.toList()
    }

    @Test
    void testWhereClauseIsNotSplitMidPredicate() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > 5000 && e.name.reverse() == 'knarF'
            select e.name
        }
        assert 'SELECT e.* FROM employees e' == db.sqlText
        assert [] == db.params
        assert ['Frank'] == result.toList()
    }

    @Test
    void testCapturedValueStaysBoundWhenPrefixIsPushed() {
        def name = "Alice' OR '1'='1"
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.name == name
            select e.name.reverse()
        }
        assert 'SELECT e.* FROM employees e WHERE e.name = ?' == db.sqlText
        assert ["Alice' OR '1'='1"] == db.params
        assert [] == result.toList()
    }

    @Test
    void testGroupByRunsInMemoryWhenSelectDoesNotTranslate() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.name.reverse() != ''
            groupby e.deptId into g
            orderby g.deptId
            select g.deptId, g.count()
        }
        assert 'SELECT e.* FROM employees e' == db.sqlText
        def rows = result.toList().collect { [it[0], it[1]] }
        // collection-provider orderby, which puts nulls last
        assert [[1, 2], [2, 2], [3, 1], [null, 1]] == rows
    }

    @Test
    void testInMemoryCollectionJoin() {
        def bonuses = [[deptId: 1, label: 'dev'], [deptId: 3, label: 'sup']]
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join b in bonuses on e.deptId == b.deptId
            orderby e.name
            select e.name, b.label
        }
        assert 'SELECT e.* FROM employees e' == db.sqlText
        assert [['Alice', 'dev'], ['Bob', 'dev'], ['Frank', 'sup']] == result.toList()
    }

    @Test
    void testPushedLeftJoinKeepsUnmatchedRows() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            leftjoin d in 'departments' on e.deptId == d.id
            orderby e.name
            select e.name, d.name?.reverse()
        }
        assert [['Alice', 'veD'], ['Bob', 'veD'], ['Carol', 'selaS'], ['Dave', null], ['Eve', 'selaS'], ['Frank', 'troppuS']] ==
                result.toList()
    }

    @Test
    void testWindowFunctionInMemory() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.deptId == 1
            orderby e.name
            select e.name, (rowNumber() over(orderby e.name))
        }
        assert 'SELECT e.* FROM employees e WHERE e.deptId = ? ORDER BY e.name ASC NULLS LAST' == db.sqlText
        assert [1] == db.params
        assert [['Alice', 0], ['Bob', 1]] == result.toList()
    }

    @Test
    void testCorrelatedSubqueryInTheInMemoryTail() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.name.reverse().endsWith('A') && (from d in 'departments' where d.id == e.deptId select d.id).exists()
            select e.name
        }
        assert ['SELECT e.* FROM employees e', 'SELECT * FROM departments'] == db.sqlTexts
        assert [[], []] == db.paramLists
        assert ['Alice'] == result.toList()
    }

    @Test
    void testUntranslatableJoinSubqueryRunsInMemory() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join v in (from d in 'departments' where d.name.reverse() == 'veD' select d.id, d.name as label) on e.deptId == v.id
            orderby e.name
            select e.name, v.label
        }
        assert ['SELECT e.* FROM employees e', 'SELECT * FROM departments'] == db.sqlTexts
        assert [[], []] == db.paramLists
        assert [['Alice', 'Dev'], ['Bob', 'Dev']] == result.toList()
    }

    @Test
    void testJooqProviderUsesTheSameSplit() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > 5000
            select e.name.reverse()
        }
        assert ['knarF'] == result.toList()
    }

    @Test
    void testJooqPushedJoin() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            where e.name == 'Alice'
            select e.name.reverse(), d.name
        }
        assert [['ecilA', 'Dev']] == result.toList()
    }

    @Test
    void testStrictModeStillRejectsUntranslatableMethod() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.name.reverse()
            }
        '''
        assert err.message.contains('Method `reverse` cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testExplicitStrictModeRejectsUntranslatableMethod() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: false, dataSource: db) {
                from e in 'employees'
                select e.name.reverse()
            }
        '''
        assert err.message.contains('Method `reverse` cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testMixedModeMustBeBoolean() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: 'yes', dataSource: db) {
                from e in 'employees'
                select e.name
            }
        '''
        assert err.message.contains('`mixedMode` must be `true` or `false`')
    }

    @Test
    void testMixedModeFromMustBeSql() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: true, dataSource: db) {
                from n in [1, 2, 3]
                select n
            }
        '''
        assert err.message.contains('mixed mode requires the `from` source to translate to SQL')
        assert err.message.contains('string literals')
    }

    @Test
    void testMixedModeNestedTableNameIsValidated() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: true, dataSource: db) {
                from e in 'employees'
                where e.name.reverse() == 'x' && (from d in 'departments; delete from departments' where d.id == e.deptId select d.id).exists()
                select e.name
            }
        '''
        assert err.message.contains('table name `departments; delete from departments` must be a simple identifier')
        assert !err.message.contains('SELECT * FROM departments;')
    }

    @Test
    void testMixedModeDeferredJoinTableNameIsValidated() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: true, dataSource: db) {
                from e in 'employees'
                join d in 'departments; delete from departments' on e.deptId == d.id
                select e.name.reverse()
            }
        '''
        assert err.message.contains('table name `departments; delete from departments` must be a simple identifier')
        assert !err.message.contains('SELECT * FROM departments;')
    }

    @Test
    void testMixedModeDoesNotSplitSetOperations() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', mixedMode: true, dataSource: db) {
                from a in 'employees'
                select a.name.reverse()
                union
                from b in 'employees'
                select b.name
            }
        '''
        assert err.message.contains('mixed mode cannot split a set operation')
        assert err.message.contains('Method `reverse` cannot be translated to SQL')
    }

    @Test
    void testHavingKeepsGroupingInMemory() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > 2000
            groupby e.deptId into g
            having g.count() > 1 && 'x'.reverse() == 'x'
            select g.deptId, g.count()
        }
        assert 'SELECT e.* FROM employees e WHERE e.salary > ?' == db.sqlText
        assert [2000] == db.params
        assert [[1, 2], [2, 2]] as Set == result.toList().collect { [it[0], it[1]] } as Set
    }

    @Test
    void testSecondJoinStaysInMemory() {
        def bonuses = [[deptId: 1, label: 'dev'], [deptId: 3, label: 'sup']]
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            join b in bonuses on e.deptId == b.deptId
            where e.salary > 4000
            orderby e.name
            select e.name as employee, d.name as dept, b.label
        }
        // the pushed join is executed through the zip path, not Sql.rows
        assert [] == db.sqlTexts
        assert [['Alice', 'Dev', 'dev'], ['Frank', 'Support', 'sup']] ==
                result.collect { [it.employee, it.dept, it.label] }
    }

    @Test
    void testUntranslatableOrderKeepsLimitInMemory() {
        def result = GQL(provider: 'native-sql', mixedMode: true, dataSource: db) {
            from e in 'employees'
            where e.salary > 0
            orderby e.name.reverse()
            limit 1
            select e.name
        }
        assert 'SELECT e.* FROM employees e WHERE e.salary > ?' == db.sqlText
        assert [0] == db.params
        assert ['Bob'] == result.toList()
    }
}
