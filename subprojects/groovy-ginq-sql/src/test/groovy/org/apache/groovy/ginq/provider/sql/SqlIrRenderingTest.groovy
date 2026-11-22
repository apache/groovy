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

import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlCountStar
import org.apache.groovy.ginq.provider.sql.ir.SqlExists
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlIn
import org.apache.groovy.ginq.provider.sql.ir.SqlInQuery
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
import org.apache.groovy.ginq.provider.sql.ir.SqlScalarQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlSetQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlStar
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.apache.groovy.ginq.provider.sql.ir.SqlUnary
import org.apache.groovy.ginq.provider.sql.ir.SqlWindow
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowBound
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowCall
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowFrame
import org.apache.groovy.ginq.provider.sql.jooq.JooqRenderer
import org.apache.groovy.ginq.provider.sql.render.AnsiDialect
import org.apache.groovy.ginq.provider.sql.render.SqlRenderer
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

class SqlIrRenderingTest {

    @Test
    void fullJoinRewriteKeepsPredicatesInsideTheUnion() {
        SqlQuery probe = new SqlQuery()
        probe.from = new SqlTableRef('departments', 'd')
        probe.projections << new SqlProjection(new SqlFunction('COUNT', [new SqlColumn('d', 'id')], true))

        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.joins << new SqlJoin(SqlJoin.Type.FULL, new SqlTableRef('departments', 'd'),
                new SqlBinary('=', col('e', 'deptId'), col('d', 'id')))
        query.where = new SqlBinary('AND',
                new SqlUnary('NOT', new SqlIsNull(col('e', 'name'), false)),
                new SqlBinary('AND',
                        new SqlLike(col('e', 'name'), new SqlParam(new ConstantExpression('%a%'))),
                        new SqlBinary('AND',
                                new SqlIn(col('e', 'salary'), [new SqlLiteral(4000), new SqlLiteral(5000)], false),
                                new SqlBinary('OR',
                                        new SqlExists(probe),
                                        new SqlBinary('=', col('e', 'salary'),
                                                new SqlScalarQuery(countStar('employees', 'z')))))))
        query.projections << new SqlProjection(new SqlCaseWhen(
                new SqlBinary('>', col('e', 'salary'), new SqlLiteral(4000)),
                new SqlCast(col('e', 'name'), 'VARCHAR'),
                new SqlLiteral('low')), 'label')
        query.projections << new SqlProjection(new SqlFunction('UPPER', [col('e', 'name')]))
        query.groupBy << col('e', 'name')
        query.having = new SqlBinary('>', new SqlCountStar(), new SqlLiteral(0))
        query.orderBy << new SqlOrderSpec(col('e', 'name'), false, false)
        query.fetch = new SqlLiteral(5)

        String sql = render(MixedSourceLayout.layoutComplete(query))
        String upper = sql.toUpperCase(Locale.ROOT)
        assert upper.contains('UNION ALL')
        assert !upper.contains('FULL OUTER JOIN')
        assert upper.contains('NOT ')
        assert upper.contains('LIKE')
        assert upper.contains(' IN (')
        assert upper.contains('EXISTS')
        assert upper.contains('CASE')
        assert upper.contains('CAST')
        assert upper.contains('UPPER')
        assert upper.contains('GROUP BY')
        assert upper.contains('HAVING')
        assert upper.contains('ORDER BY')
        assert upper.contains('FETCH')
    }

    @Test
    void setQueryCopiesOnlyTheSideWhoseFullJoinChanged() {
        SqlQuery plain = new SqlQuery()
        plain.from = new SqlTableRef('employees', 'a')
        plain.projections << new SqlProjection(col('a', 'name'))

        SqlSetQuery set = new SqlSetQuery(plain, SqlSetQuery.SetOp.UNION, fullJoin())
        set.orderBy << new SqlOrderSpec(new SqlOrdinal(1), true, true)
        set.offset = new SqlLiteral(1)
        set.fetch = new SqlLiteral(2)

        SqlQueryNode laid = MixedSourceLayout.layoutComplete(set)
        assert laid instanceof SqlSetQuery
        String sql = render(laid)
        assert sql.toUpperCase(Locale.ROOT).contains('UNION ALL')
        assert sql.toUpperCase(Locale.ROOT).contains('UNION')
        assert sql.toUpperCase(Locale.ROOT).contains('ORDER BY')
        assert sql.toUpperCase(Locale.ROOT).contains('OFFSET')
        assert !sql.toUpperCase(Locale.ROOT).contains('FULL OUTER JOIN')
    }

    @Test
    void inQueryAndStarAreRewrittenWithTheOuterFullJoin() {
        SqlQuery inner = new SqlQuery()
        inner.from = new SqlTableRef('departments', 'd2')
        inner.projections << new SqlProjection(col('d2', 'id'))
        inner.where = new SqlBinary('=', col('d2', 'id'), col('e', 'deptId'))

        SqlQuery query = fullJoin()
        query.where = new SqlInQuery(col('e', 'deptId'), inner, true)
        query.projections.clear()
        query.projections << new SqlProjection(new SqlStar('e'))
        query.projections << new SqlProjection(col('d', 'name'), 'dn')

        String sql = render(MixedSourceLayout.layoutComplete(query))
        String upper = sql.toUpperCase(Locale.ROOT)
        assert upper.contains('NOT IN') || upper.contains('NOT (')
        assert upper.contains('UNION ALL')
        assert !upper.contains('FULL OUTER JOIN')
    }

    @Test
    void jooqRendersEachFrameBoundAndEachSetOperator() {
        String rows = jooq(windowQuery(SqlWindowFrame.Unit.ROWS,
                SqlWindowBound.preceding(new SqlLiteral(1)),
                SqlWindowBound.following(new SqlLiteral(0))))
        assert rows.toLowerCase(Locale.ROOT).contains('rows between 1 preceding and current row')

        String range = jooq(windowQuery(SqlWindowFrame.Unit.RANGE,
                SqlWindowBound.unboundedPreceding(),
                SqlWindowBound.unboundedFollowing()))
        assert range.toLowerCase(Locale.ROOT).contains('range between unbounded preceding and unbounded following')

        String current = jooq(windowQuery(SqlWindowFrame.Unit.ROWS,
                SqlWindowBound.currentRow(),
                SqlWindowBound.currentRow()))
        assert current.toLowerCase(Locale.ROOT).contains('rows between current row and current row')

        Map<SqlSetQuery.SetOp, String> words = [
                (SqlSetQuery.SetOp.UNION)     : 'union',
                (SqlSetQuery.SetOp.UNION_ALL) : 'union all',
                (SqlSetQuery.SetOp.INTERSECT) : 'intersect',
                (SqlSetQuery.SetOp.MINUS)     : 'except'
        ]
        for (SqlSetQuery.SetOp op : SqlSetQuery.SetOp.values()) {
            SqlSetQuery set = new SqlSetQuery(plain('a'), op, plain('b'))
            set.orderBy << new SqlOrderSpec(new SqlOrdinal(1), false, true)
            set.offset = new SqlParam(new ConstantExpression(1))
            set.fetch = new SqlParam(new ConstantExpression(2))
            String sql = JooqRenderer.render(set, 'H2').sql.toLowerCase(Locale.ROOT)
            assert sql.contains(words[op])
            assert sql.contains('order by')
            assert sql.contains('offset')
        }

        String following = jooq(windowQuery(SqlWindowFrame.Unit.ROWS,
                SqlWindowBound.following(new SqlLiteral(1)),
                SqlWindowBound.preceding(new SqlLiteral(2))))
        assert following.toLowerCase(Locale.ROOT).contains('1 following')
        assert following.toLowerCase(Locale.ROOT).contains('2 preceding')

        def fraction = shouldFail(IllegalArgumentException) {
            jooq(windowQuery(SqlWindowFrame.Unit.ROWS,
                    SqlWindowBound.preceding(new SqlLiteral(1.5d)),
                    SqlWindowBound.currentRow()))
        }
        assert fraction.message.contains('Java int')

        assert '`' as char == JooqRenderer.identifierQuote('MYSQL')
        assert '"' as char == JooqRenderer.identifierQuote('H2')
        assert !FunctionSupport.supports('h2', '')
        assert !FunctionSupport.supports('h2', null)
        assert 'postgres' == FunctionSupport.canonical('PostgreSQL')
        assert 'oracle' == FunctionSupport.canonical('Oracle')
    }

    private static SqlQuery fullJoin() {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.joins << new SqlJoin(SqlJoin.Type.FULL, new SqlTableRef('departments', 'd'),
                new SqlBinary('=', col('e', 'deptId'), col('d', 'id')))
        query.projections << new SqlProjection(col('e', 'name'), 'en')
        return query
    }

    private static SqlQuery plain(String alias) {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', alias)
        query.projections << new SqlProjection(col(alias, 'name'))
        return query
    }

    private static SqlQuery countStar(String table, String alias) {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef(table, alias)
        query.projections << new SqlProjection(new SqlCountStar())
        return query
    }

    private static SqlQuery windowQuery(SqlWindowFrame.Unit unit, SqlWindowBound lower, SqlWindowBound upper) {
        SqlQuery query = plain('e')
        SqlWindow window = new SqlWindow(
                [col('e', 'deptId')],
                [new SqlOrderSpec(col('e', 'salary'), true, true)],
                new SqlWindowFrame(unit, lower, upper))
        query.projections << new SqlProjection(new SqlWindowCall(
                new SqlFunction('SUM', [col('e', 'salary')]), window), 'near')
        return query
    }

    private static SqlColumn col(String alias, String name) {
        new SqlColumn(alias, name)
    }

    private static String render(SqlQueryNode node) {
        new SqlRenderer(new AnsiDialect()).render(node).sql
    }

    private static String jooq(SqlQueryNode node) {
        JooqRenderer.render(node, 'H2').sql
    }
}
