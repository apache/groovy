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

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlCountStar
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlIn
import org.apache.groovy.ginq.provider.sql.ir.SqlIsNull
import org.apache.groovy.ginq.provider.sql.ir.SqlJoin
import org.apache.groovy.ginq.provider.sql.ir.SqlLike
import org.apache.groovy.ginq.provider.sql.ir.SqlLiteral
import org.apache.groovy.ginq.provider.sql.ir.SqlOrderSpec
import org.apache.groovy.ginq.provider.sql.ir.SqlOrdinal
import org.apache.groovy.ginq.provider.sql.ir.SqlParam
import org.apache.groovy.ginq.provider.sql.ir.SqlProjection
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.ir.SqlSetQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlStar
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.apache.groovy.ginq.provider.sql.ir.SqlUnary
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.render.AnsiDialect
import org.apache.groovy.ginq.provider.sql.render.Dialect
import org.apache.groovy.ginq.provider.sql.render.Dialects
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.ginq.provider.sql.render.SqlRenderer
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
class SqlRendererTest {

    @Test
    void testSingleTable() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.where = new SqlBinary('>', col('e', 'salary'), param(1000))
        query.projections << new SqlProjection(col('e', 'name'))

        def rendered = render(query)
        assert 'SELECT e.name FROM employees e WHERE e.salary > ?' == rendered.sql
        assert [1000] == values(rendered)
    }

    @Test
    void testDistinctOrderByOffsetFetch() {
        def query = new SqlQuery()
        query.distinct = true
        query.from = new SqlTableRef('employees', 'e')
        query.where = new SqlBinary('>', col('e', 'salary'), param(1000))
        query.orderBy << new SqlOrderSpec(col('e', 'salary'), false, true)
        query.orderBy << new SqlOrderSpec(col('e', 'name'), true, false)
        query.offset = param(2)
        query.fetch = param(5)
        query.projections << new SqlProjection(col('e', 'name'))

        def rendered = render(query)
        assert 'SELECT DISTINCT e.name FROM employees e WHERE e.salary > ? ' +
                'ORDER BY e.salary DESC NULLS LAST, e.name ASC NULLS FIRST ' +
                'OFFSET ? ROWS FETCH NEXT ? ROWS ONLY' == rendered.sql
        assert [1000, 2, 5] == values(rendered)
    }

    @Test
    void testFetchWithoutOffset() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.fetch = param(3)
        query.projections << new SqlProjection(new SqlStar('e'))

        assert 'SELECT e.* FROM employees e FETCH FIRST ? ROWS ONLY' == render(query).sql
    }

    @Test
    void testJoins() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.joins << new SqlJoin(SqlJoin.Type.INNER, new SqlTableRef('departments', 'd'),
                new SqlBinary('=', col('e', 'deptId'), col('d', 'id')))
        query.joins << new SqlJoin(SqlJoin.Type.LEFT, new SqlTableRef('locations', 'l'),
                new SqlBinary('=', col('d', 'locId'), col('l', 'id')))
        query.joins << new SqlJoin(SqlJoin.Type.CROSS, new SqlTableRef('grades', 'g'), null)
        query.projections << new SqlProjection(col('e', 'name'))

        assert 'SELECT e.name FROM employees e ' +
                'INNER JOIN departments d ON e.deptId = d.id ' +
                'LEFT JOIN locations l ON d.locId = l.id ' +
                'CROSS JOIN grades g' == render(query).sql
    }

    @Test
    void testGroupByHavingAggregates() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.groupBy << col('e', 'deptId')
        query.having = new SqlBinary('>', new SqlCountStar(), param(1))
        query.projections << new SqlProjection(col('e', 'deptId'))
        query.projections << new SqlProjection(new SqlCountStar(), 'cnt')
        query.projections << new SqlProjection(new SqlFunction('AVG', [col('e', 'salary')]), 'avgSalary')

        def rendered = render(query)
        assert 'SELECT e.deptId, COUNT(*) AS cnt, AVG(e.salary) AS avgSalary FROM employees e ' +
                'GROUP BY e.deptId HAVING COUNT(*) > ?' == rendered.sql
        assert [1] == values(rendered)
    }

    @Test
    void testSetOperationsChainingAndMinus() {
        def a = simpleQuery('a')
        def b = simpleQuery('b')
        def c = simpleQuery('c')
        def setQuery = new SqlSetQuery(new SqlSetQuery(a, SqlSetQuery.SetOp.UNION, b), SqlSetQuery.SetOp.MINUS, c)

        assert '(SELECT a.id FROM a a UNION SELECT b.id FROM b b) EXCEPT SELECT c.id FROM c c' == render(setQuery).sql
    }

    @Test
    void testUnionAllAndIntersect() {
        def setQuery = new SqlSetQuery(simpleQuery('a'), SqlSetQuery.SetOp.UNION_ALL, simpleQuery('b'))
        assert 'SELECT a.id FROM a a UNION ALL SELECT b.id FROM b b' == render(setQuery).sql

        setQuery = new SqlSetQuery(simpleQuery('a'), SqlSetQuery.SetOp.INTERSECT, simpleQuery('b'))
        assert 'SELECT a.id FROM a a INTERSECT SELECT b.id FROM b b' == render(setQuery).sql
    }

    @Test
    void testPrecedenceParentheses() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('t', 't')
        def aOrB = new SqlBinary('OR', new SqlBinary('=', col('t', 'a'), param(1)), new SqlBinary('=', col('t', 'b'), param(2)))
        query.where = new SqlBinary('AND', aOrB, new SqlBinary('=', col('t', 'c'), param(3)))
        query.projections << new SqlProjection(col('t', 'a'))

        assert 'SELECT t.a FROM t t WHERE (t.a = ? OR t.b = ?) AND t.c = ?' == render(query).sql
    }

    @Test
    void testNonAssociativeRightOperand() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('t', 't')
        def bMinusC = new SqlBinary('-', col('t', 'b'), col('t', 'c'))
        query.projections << new SqlProjection(new SqlBinary('-', col('t', 'a'), bMinusC))

        assert 'SELECT t.a - (t.b - t.c) FROM t t' == render(query).sql
    }

    @Test
    void testIsNullInNotAndFunctions() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('t', 't')
        def isNull = new SqlIsNull(col('t', 'a'), false)
        def isNotNull = new SqlIsNull(col('t', 'b'), true)
        def inList = new SqlIn(col('t', 'c'), [param(1), param(2)], false)
        def notInList = new SqlIn(col('t', 'd'), [param(3)], true)
        def notExpr = new SqlUnary('NOT', new SqlBinary('=', new SqlFunction('UPPER', [col('t', 'e')]), param('X')))
        query.where = new SqlBinary('AND',
                new SqlBinary('AND',
                        new SqlBinary('AND',
                                new SqlBinary('AND', isNull, isNotNull), inList), notInList), notExpr)
        query.projections << new SqlProjection(col('t', 'a'))

        def rendered = render(query)
        assert 'SELECT t.a FROM t t WHERE t.a IS NULL AND t.b IS NOT NULL ' +
                'AND t.c IN (?, ?) AND t.d NOT IN (?) AND NOT (UPPER(t.e) = ?)' == rendered.sql
        assert [1, 2, 3, 'X'] == values(rendered)
    }

    @Test
    void testParameterOrderingAcrossClauses() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.joins << new SqlJoin(SqlJoin.Type.INNER, new SqlTableRef('departments', 'd'),
                new SqlBinary('AND',
                        new SqlBinary('=', col('e', 'deptId'), col('d', 'id')),
                        new SqlBinary('>', col('d', 'budget'), param(100))))
        query.where = new SqlBinary('>', col('e', 'salary'), param(200))
        query.groupBy << col('d', 'name')
        query.having = new SqlBinary('>', new SqlCountStar(), param(300))
        query.orderBy << new SqlOrderSpec(new SqlCountStar(), true, true)
        query.offset = param(400)
        query.fetch = param(500)
        query.projections << new SqlProjection(new SqlBinary('+', col('d', 'name'), param(50)))

        assert [50, 100, 200, 300, 400, 500] == values(render(query))
    }

    @Test
    void testLikeAndCaseWhen() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('t', 't')
        query.where = new SqlLike(col('t', 'name'), param('%x%'))
        query.projections << new SqlProjection(
                new SqlCaseWhen(new SqlBinary('>', col('t', 'salary'), param(1000)), param('high'), param('low')), 'band')

        def rendered = render(query)
        assert "SELECT CASE WHEN t.salary > ? THEN ? ELSE ? END AS band FROM t t " +
                "WHERE t.name LIKE ? ESCAPE '!'" == rendered.sql
        assert [1000, 'high', 'low', '%x%'] == values(rendered)
    }

    @Test
    void testSubstringRendersFromFor() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('t', 't')
        query.projections << new SqlProjection(new SqlFunction('SUBSTRING', [col('t', 'name'), param(1), param(3)]))

        assert 'SELECT SUBSTRING(t.name FROM ? FOR ?) FROM t t' == render(query).sql
    }

    @Test
    void testSetQueryWithOrdinalOrderByAndLimit() {
        def setQuery = new SqlSetQuery(simpleQuery('a'), SqlSetQuery.SetOp.UNION, simpleQuery('b'))
        setQuery.orderBy << new SqlOrderSpec(new SqlOrdinal(1), false, true)
        setQuery.offset = param(1)
        setQuery.fetch = param(2)

        def rendered = render(setQuery)
        assert 'SELECT a.id FROM a a UNION SELECT b.id FROM b b ' +
                'ORDER BY 1 DESC NULLS LAST OFFSET ? ROWS FETCH NEXT ? ROWS ONLY' == rendered.sql
        assert [1, 2] == values(rendered)
    }

    @Test
    void testDialectChoosesLimitPlaceholderOrder() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.offset = param(2)
        query.fetch = param(5)
        query.projections << new SqlProjection(col('e', 'name'))

        def rendered = new SqlRenderer(new LimitThenOffsetDialect()).render(query)
        assert 'SELECT e.name FROM employees e LIMIT ? OFFSET ?' == rendered.sql
        assert [5, 2] == values(rendered)
    }

    @Test
    void testNativeDialectsQuoteLimitAndFunctions() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('hr.employees', 'e')
        query.where = new SqlFunction('MOD', [col('e', 'salary'), param(2)])
        query.orderBy << new SqlOrderSpec(col('e', 'name'), true, true)
        query.offset = param(1)
        query.fetch = param(2)
        query.projections << new SqlProjection(new SqlFunction('CHAR_LENGTH', [col('e', 'name')]))
        query.projections << new SqlProjection(new SqlFunction('SUBSTRING', [col('e', 'name'), param(1), param(3)]))

        def mysql = new SqlRenderer(Dialects.resolve('mysql')).render(query)
        assert 'SELECT CHAR_LENGTH(`e`.`name`), SUBSTRING(`e`.`name` FROM ? FOR ?) FROM `hr`.`employees` `e` ' +
                'WHERE MOD(`e`.`salary`, ?) ORDER BY (`e`.`name` IS NULL) ASC, `e`.`name` ASC ' +
                'LIMIT ? OFFSET ?' == mysql.sql
        assert [1, 3, 2, 2, 1] == values(mysql)

        def sqlite = new SqlRenderer(Dialects.resolve('SQLITE')).render(query)
        assert 'SELECT LENGTH("e"."name"), SUBSTR("e"."name", ?, ?) FROM "hr"."employees" "e" ' +
                'WHERE "e"."salary" % ? ORDER BY ("e"."name" IS NULL) ASC, "e"."name" ASC ' +
                'LIMIT ? OFFSET ?' == sqlite.sql
        assert [1, 3, 2, 2, 1] == values(sqlite)

        def postgres = new SqlRenderer(Dialects.resolve('PostgreSQL')).render(query)
        assert postgres.sql.contains('FROM "hr"."employees" "e"')
        assert postgres.sql.contains('OFFSET ? ROWS FETCH NEXT ? ROWS ONLY')
        assert postgres.sql.contains('CHAR_LENGTH("e"."name")')
        assert [1, 3, 2, 1, 2] == values(postgres)

        assert Dialects.resolve('nope') == null
        assert Dialects.resolve('h2') instanceof AnsiDialect
    }

    @Test
    void testCountDistinctAndGreatestLeast() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(new SqlFunction('COUNT', [col('e', 'deptId')], true))
        query.projections << new SqlProjection(new SqlFunction('GREATEST', [col('e', 'salary'), param(1)]))
        query.projections << new SqlProjection(new SqlFunction('LEAST', [col('e', 'salary'), param(2)]))

        def rendered = render(query)
        assert 'SELECT COUNT(DISTINCT e.deptId), GREATEST(e.salary, ?), LEAST(e.salary, ?) FROM employees e' == rendered.sql
        assert [1, 2] == values(rendered)

        def sqlite = new SqlRenderer(Dialects.resolve('sqlite')).render(query)
        assert 'SELECT COUNT(DISTINCT "e"."deptId"), MAX("e"."salary", ?), MIN("e"."salary", ?) FROM "employees" "e"' == sqlite.sql
        assert !sqlite.sql.contains('GREATEST')
        assert !sqlite.sql.contains('LEAST')

        def mysql = new SqlRenderer(Dialects.resolve('mysql')).render(query)
        assert mysql.sql.contains('COUNT(DISTINCT `e`.`deptId`)')
        assert mysql.sql.contains('GREATEST(`e`.`salary`, ?)')
        assert mysql.sql.contains('LEAST(`e`.`salary`, ?)')
    }

    @Test
    void testSqliteRejectsFloorAndCeiling() {
        def floor = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('sqlite')).render(functionQuery('FLOOR'))
        }
        assert floor.message.contains('FLOOR')
        assert floor.message.contains('sqlite')
        assert !floor.message.contains('SELECT')

        def ceiling = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('sqlite')).render(functionQuery('CEILING'))
        }
        assert ceiling.message.contains('CEILING')
        assert ceiling.message.contains('sqlite')

        def ansi = new SqlRenderer(Dialects.resolve('ansi')).render(functionQuery('FLOOR'))
        assert ansi.sql.contains('FLOOR(')
        def h2 = new SqlRenderer(Dialects.resolve('h2')).render(functionQuery('CEILING'))
        assert h2.sql.contains('CEILING(')
    }

    @Test
    void testCastAndSearchPosition() {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(new SqlCast(col('e', 'id'), 'DECIMAL(10,2)'))
        query.projections << new SqlProjection(new SqlBinary('-',
                new SqlFunction('POSITION', [param('li'), col('e', 'name')]),
                new SqlLiteral(1)))

        def rendered = render(query)
        assert 'SELECT CAST(e.id AS DECIMAL(10,2)), POSITION(? IN e.name) - 1 FROM employees e' == rendered.sql
        assert ['li'] == values(rendered)

        def mysql = new SqlRenderer(Dialects.resolve('mysql')).render(query)
        assert mysql.sql.contains('CAST(`e`.`id` AS DECIMAL(10,2))')
        assert mysql.sql.contains('LOCATE(?, `e`.`name`) - 1')
        assert !mysql.sql.contains('POSITION')

        def sqlite = new SqlRenderer(Dialects.resolve('sqlite')).render(query)
        assert sqlite.sql.contains('CAST("e"."id" AS DECIMAL(10,2))')
        assert sqlite.sql.contains('INSTR("e"."name", ?) - 1')
        assert !sqlite.sql.contains('POSITION')
        assert !sqlite.sql.contains('LOCATE')
    }

    private static SqlQuery functionQuery(String name) {
        def query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(new SqlFunction(name, [col('e', 'salary')]))
        return query
    }

    private static SqlQuery simpleQuery(String table) {
        def query = new SqlQuery()
        query.from = new SqlTableRef(table, table)
        query.projections << new SqlProjection(col(table, 'id'))
        return query
    }

    private static SqlColumn col(String alias, String column) {
        new SqlColumn(alias, column)
    }

    private static SqlParam param(Object value) {
        new SqlParam(new ConstantExpression(value))
    }

    private static RenderedSql render(SqlQueryNode queryNode) {
        new SqlRenderer(new AnsiDialect()).render(queryNode)
    }

    private static List<Object> values(RenderedSql renderedSql) {
        renderedSql.parameters.collect { ((ConstantExpression) it).value }
    }

    private static class LimitThenOffsetDialect implements Dialect {
        @Override
        void renderLimit(SqlExpr offset, SqlExpr fetch, Dialect.LimitRender out) {
            if (fetch != null) {
                out.text(' LIMIT ')
                out.expr(fetch)
            }
            if (offset != null) {
                out.text(' OFFSET ')
                out.expr(offset)
            }
        }
    }
}
