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
package org.apache.groovy.ginq.provider.sql.jooq

import groovy.sql.Sql
import org.apache.groovy.ginq.provider.sql.GinqSqlTestSupport
import org.apache.groovy.ginq.provider.sql.GinqSqlTranslationTest.CapturingSql
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlLiteral
import org.apache.groovy.ginq.provider.sql.ir.SqlParam
import org.apache.groovy.ginq.provider.sql.ir.SqlProjection
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Asserts the dialect-specific SQL the jooq-sql provider generates, e.g.
 * H2's alias rendering without {@code AS} and lowercase keywords.
 */
class JooqSqlTranslationTest {
    private Sql realDb
    private CapturingSql db

    @BeforeEach
    void setUp() {
        realDb = GinqSqlTestSupport.newPopulatedDb()
        db = new CapturingSql(realDb)
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(realDb)
    }

    @Test
    void testH2Rendering() {
        def threshold = 2500
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.salary > threshold
            orderby e.name
            limit 1, 2
            select e.name
        }
        assert 'select e.name from employees e where e.salary > ? ' +
                'order by e.name asc nulls last offset ? rows fetch next ? rows only' == db.sqlText
        assert [2500, 1, 2] == db.params
    }

    @Test
    void testSetOperationWithTrailingOrderByAndLimit() {
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
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
        assert 'select a.name, a.salary from employees a where a.deptId = ? ' +
                'union select b.name, b.salary from employees b where b.deptId = ? ' +
                'order by 2 desc nulls last, 1 asc nulls last offset ? rows fetch next ? rows only' == db.sqlText
        assert [1, 2, 1, 2] == db.params
    }

    @Test
    void testGroupByAggregatesAndLike() {
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.deptId != null && e.name.contains('a')
            groupby e.deptId into g
            having g.count() > 1
            select g.deptId, g.count() as cnt
        }
        assert "select e.deptId deptId, count(*) cnt from employees e " +
                "where (e.deptId is not null and e.name like ? escape '!') " +
                'group by e.deptId having count(*) > ?' == db.sqlText
        assert ['%a%', 1] == db.params
    }

    @Test
    void testCoalesceCountDistinctAndExtremes() {
        def floor = 4500
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where coalesce(e.deptId, 0) > 0
            select coalesce(e.name, 'x') as display, Math.max(e.salary, floor) as hi, Math.min(e.salary, floor) as lo
        }
        assert 'select coalesce(e.name, ?) display, greatest(e.salary, ?) hi, least(e.salary, ?) lo ' +
                'from employees e where coalesce(e.deptId, ?) > ?' == db.sqlText
        assert ['x', 4500, 4500, 0, 0] == db.params

        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            groupby e.deptId
            select e.deptId, count(distinct(e.salary)) as kinds
        }
        assert 'select e.deptId, count(distinct e.salary) kinds from employees e group by e.deptId' == db.sqlText
    }

    @Test
    void testCastAndIndexOf() {
        def needle = 'li'
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.id == 1
            select e.name as label, cast(e.id, 'DECIMAL') / 2 as half, e.name.indexOf(needle) as at
        }
        assert 'select e.name label, (cast(cast(e.id as decimal) as double) / cast(? as double)) half, ' +
                '(position(? in e.name) - 1) at from employees e where e.id = ?' == db.sqlText
        assert [2, 'li', 1] == db.params
    }

    @Test
    void testCastAndPositionDialectSpellings() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(new SqlCast(new SqlColumn('e', 'id'), 'DECIMAL'))
        query.projections << new SqlProjection(new SqlBinary('-',
                new SqlFunction('POSITION', [new SqlParam(new ConstantExpression('a')), new SqlColumn('e', 'name')]),
                new SqlLiteral(1)))

        def h2 = JooqRenderer.render(query, 'H2').sql.toLowerCase()
        assert h2.contains('cast(e.id as decimal)')
        assert h2.contains('position(')
        assert h2.contains('in e.name')

        def mysql = JooqRenderer.render(query, 'MYSQL').sql.toLowerCase()
        assert mysql.contains('locate(')
        assert !mysql.contains('position(')

        def sqlite = JooqRenderer.render(query, 'SQLITE').sql.toLowerCase()
        assert sqlite.contains('instr(')
        assert !sqlite.contains('position(')
        assert !sqlite.contains('locate(')
    }

    @Test
    void testSqliteRendersScalarMaxMin() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(new SqlFunction('GREATEST', [new SqlColumn('e', 'salary'), new SqlParam(new ConstantExpression(1))]))
        query.projections << new SqlProjection(new SqlFunction('LEAST', [new SqlColumn('e', 'salary'), new SqlParam(new ConstantExpression(2))]))
        query.projections << new SqlProjection(new SqlFunction('COUNT', [new SqlColumn('e', 'deptId')], true))
        def sql = JooqRenderer.render(query, 'SQLITE').sql.toLowerCase()
        assert sql.contains('max(')
        assert sql.contains('min(')
        assert sql.contains('count(distinct')
        assert !sql.contains('greatest')
        assert !sql.contains('least')
    }

    @Test
    void testSchemaQualifiedTableIsTwoIdentifiers() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('hr.employees', 'e')
        query.projections << new SqlProjection(new SqlColumn('e', 'id'))
        def sql = JooqRenderer.render(query, 'H2').sql.toLowerCase()
        assert sql.contains('from hr.employees e')
        assert !sql.contains('"hr.employees"')
    }
}
