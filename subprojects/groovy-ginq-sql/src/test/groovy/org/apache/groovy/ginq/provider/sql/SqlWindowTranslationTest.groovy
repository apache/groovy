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

import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlCountStar
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlLiteral
import org.apache.groovy.ginq.provider.sql.ir.SqlOrderSpec
import org.apache.groovy.ginq.provider.sql.ir.SqlParam
import org.apache.groovy.ginq.provider.sql.ir.SqlProjection
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.ir.SqlSetQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.apache.groovy.ginq.provider.sql.ir.SqlWindow
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowBound
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowCall
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowFrame
import org.apache.groovy.ginq.provider.sql.jooq.JooqRenderer
import org.apache.groovy.ginq.provider.sql.render.AnsiDialect
import org.apache.groovy.ginq.provider.sql.render.Dialects
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.ginq.provider.sql.render.SqlRenderer
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Built-in window functions translate to {@code OVER}. A copy of the registry
 * can still add a plain mapping; that mapping is not rewritten with the
 * built-in adjustments.
 */
class SqlWindowTranslationTest {

    @Test
    void testWalkerBuildsOverForARegisteredWindowFunction() {
        def parsed = parse('''
            from e in 'nums'
            select (rowNumber() over(orderby n))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)

        assert query.projections.size() == 1
        assert query.projections[0].expr instanceof SqlBinary
        SqlBinary adjusted = (SqlBinary) query.projections[0].expr
        assert '-' == adjusted.op
        assert adjusted.right instanceof SqlLiteral
        assert 1 == adjusted.right.value
        assert adjusted.left instanceof SqlWindowCall
        SqlWindowCall call = (SqlWindowCall) adjusted.left
        assert call.function instanceof SqlFunction
        assert 'ROW_NUMBER' == call.function.name
        assert call.function.args.empty
        assert call.window.partitionBy.empty
        assert call.window.frame == null
        assert 1 == call.window.orderBy.size()
        assert call.window.orderBy[0].asc
        assert call.window.orderBy[0].nullsLast
        assert call.window.orderBy[0].expr instanceof SqlParam
        assert 'n' == call.window.orderBy[0].expr.valueExpr.text
        assert query.orderBy.empty

        def ansi = new SqlRenderer(new AnsiDialect()).render(query)
        assert 'SELECT ROW_NUMBER() OVER (ORDER BY ? ASC NULLS LAST) - 1 FROM nums e' == ansi.sql
        assert ['n'] == ansi.parameters*.text

        def mysql = new SqlRenderer(Dialects.resolve('mysql')).render(query)
        assert 'SELECT ROW_NUMBER() OVER (ORDER BY (? IS NULL) ASC, ? ASC) - 1 FROM `nums` `e`' == mysql.sql
        assert ['n', 'n'] == mysql.parameters*.text

        def jooq = JooqRenderer.render(query, 'H2').sql
        assert 'select (ROW_NUMBER() over (order by ? asc nulls last) - 1) from nums e' == jooq
    }

    @Test
    void testEmptyOverRendersEmptyParentheses() {
        def parsed = parse('''
            from e in 'nums'
            select (rowNumber() over())
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        SqlBinary adjusted = (SqlBinary) query.projections[0].expr
        SqlWindowCall call = (SqlWindowCall) adjusted.left
        assert call.window.partitionBy.empty
        assert call.window.orderBy.empty
        assert call.window.frame == null
        assert 'SELECT ROW_NUMBER() OVER () - 1 FROM nums e' == new SqlRenderer(new AnsiDialect()).render(query).sql
        assert 'select (ROW_NUMBER() over () - 1) from nums e' == JooqRenderer.render(query, 'H2').sql
    }

    @Test
    void testPartitionOrderAndRowsFrame() {
        def registry = plainRegistry()
        def parsed = parse('''
            from e in 'employees'
            select (myNumber() over(partitionby e.deptId, e.name.toUpperCase() orderby e.salary in desc rows -1, 1)),
                   (mySum(e.salary) over(orderby e.salary range null, 0))
        ''')
        def query = translate(parsed, registry)
        assert noErrors(parsed)

        SqlWindowCall rowNumber = (SqlWindowCall) query.projections[0].expr
        assert 'ROW_NUMBER' == rowNumber.function.name
        assert rowNumber.window.partitionBy[0] instanceof SqlColumn
        assert 'deptId' == rowNumber.window.partitionBy[0].column
        assert rowNumber.window.partitionBy[1] instanceof SqlFunction
        assert 'UPPER' == rowNumber.window.partitionBy[1].name
        assert !rowNumber.window.orderBy[0].asc
        assert rowNumber.window.orderBy[0].nullsLast
        assert SqlWindowFrame.Unit.ROWS == rowNumber.window.frame.unit
        assert SqlWindowBound.Kind.PRECEDING == rowNumber.window.frame.lower.kind
        assert 1 == rowNumber.window.frame.lower.offset.value
        assert SqlWindowBound.Kind.FOLLOWING == rowNumber.window.frame.upper.kind
        assert 1 == rowNumber.window.frame.upper.offset.value

        SqlWindowCall sum = (SqlWindowCall) query.projections[1].expr
        assert 'SUM' == sum.function.name
        assert sum.function.args[0] instanceof SqlColumn
        assert SqlWindowFrame.Unit.RANGE == sum.window.frame.unit
        assert SqlWindowBound.Kind.UNBOUNDED_PRECEDING == sum.window.frame.lower.kind
        assert SqlWindowBound.Kind.CURRENT_ROW == sum.window.frame.upper.kind

        def ansi = new SqlRenderer(new AnsiDialect()).render(query)
        assert 'SELECT ROW_NUMBER() OVER (PARTITION BY e.deptId, UPPER(e.name) ORDER BY e.salary DESC NULLS LAST ' +
                'ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING), ' +
                'SUM(e.salary) OVER (ORDER BY e.salary ASC NULLS LAST ' +
                'RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) FROM employees e' == ansi.sql

        def jooq = JooqRenderer.render(query, 'H2').sql
        assert 'select ROW_NUMBER() over (partition by e.deptId, upper(e.name) order by e.salary desc nulls last ' +
                'rows between 1 preceding and 1 following), ' +
                'sum(e.salary) over (order by e.salary asc nulls last ' +
                'range between unbounded preceding and current row) from employees e' == jooq
    }

    @Test
    void testUnregisteredOverStaysDeferred() {
        def parsed = parse('''
            from e in 'employees'
            select (median(e.salary) over(partitionby e.deptId)), (stdev(e.salary) over(orderby e.salary))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('window functions are deferred and are not translated by the native-sql provider')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testRegisteredWindowRejectsANonLiteralFrameBound() {
        def parsed = parse('''
            from e in 'employees'
            select (rowNumber() over(orderby e.salary rows bound, 1))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('window frame bounds must be literals')
        assert !err.message.contains('window functions are deferred')
    }

    @Test
    void testRegisteredWindowRejectsRowsAndRangeTogether() {
        def parsed = parse('''
            from e in 'employees'
            select (rowNumber() over(orderby e.salary rows -1, 1 range -1, 0))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('`rows` and `range` cannot both be used in one `over`')
    }

    @Test
    void testRendererContractForFramesAndCountStar() {
        def countStar = queryOf(new SqlWindowCall(new SqlCountStar(), new SqlWindow([], [], null)))
        assert 'SELECT COUNT(*) OVER () FROM employees e' == ansi(countStar).sql
        assert 'select count(*) over () from employees e' == JooqRenderer.render(countStar, 'H2').sql

        def distinct = queryOf(new SqlWindowCall(
                new SqlFunction('COUNT', [new SqlColumn('e', 'id')], true),
                new SqlWindow([], [], null)))
        assert 'SELECT COUNT(DISTINCT e.id) OVER () FROM employees e' == ansi(distinct).sql

        def paramOffset = queryOf(new SqlWindowCall(
                new SqlFunction('ROW_NUMBER', []),
                new SqlWindow([], [new SqlOrderSpec(new SqlColumn('e', 'id'), true, true)],
                        new SqlWindowFrame(SqlWindowFrame.Unit.ROWS,
                                SqlWindowBound.preceding(new SqlParam(new ConstantExpression(1))),
                                SqlWindowBound.currentRow()))))
        assert 'SELECT ROW_NUMBER() OVER (ORDER BY e.id ASC NULLS LAST ROWS BETWEEN ? PRECEDING AND CURRENT ROW) FROM employees e' ==
                ansi(paramOffset).sql
        def jooqRejected = shouldFail(IllegalArgumentException) {
            JooqRenderer.render(paramOffset, 'H2')
        }
        assert jooqRejected.message.contains('integer literal')

        def negative = shouldFail(IllegalArgumentException) {
            SqlWindowBound.preceding(new SqlLiteral(-1))
        }
        assert negative.message.contains('non-negative')

        for (SqlWindowFrame.Unit unit : SqlWindowFrame.Unit.values()) {
            for (SqlWindowBound.Kind lower : SqlWindowBound.Kind.values()) {
                for (SqlWindowBound.Kind upper : SqlWindowBound.Kind.values()) {
                    SqlWindowFrame frame = new SqlWindowFrame(unit, bound(lower, 2), bound(upper, 3))
                    def ordered = queryOf(new SqlWindowCall(new SqlFunction('ROW_NUMBER', []),
                            new SqlWindow([], [new SqlOrderSpec(new SqlColumn('e', 'id'), true, true)], frame)))
                    def bare = queryOf(new SqlWindowCall(new SqlFunction('ROW_NUMBER', []),
                            new SqlWindow([], [], frame)))
                    String fragment = unit.name() + ' BETWEEN ' + boundText(lower, 2) + ' AND ' + boundText(upper, 3)
                    assert ansi(ordered).sql.contains(fragment)
                    assert ansi(bare).sql.contains(fragment)
                    assert JooqRenderer.render(ordered, 'H2').sql.toLowerCase().contains(fragment.toLowerCase())
                    assert JooqRenderer.render(bare, 'H2').sql.toLowerCase().contains(fragment.toLowerCase())
                }
            }
        }
    }

    @Test
    void testBuiltInWindowFunctions() {
        def parsed = parse('''
            from e in 'employees'
            select (rank() over(partitionby e.deptId orderby e.salary)),
                   (denseRank() over(orderby e.salary in desc)),
                   (cumeDist() over(orderby e.salary)),
                   (lead(e.name) over(orderby e.id)),
                   (lead(e.name, 2, 'NONE') over(orderby e.id)),
                   (lag(e.salary, 1) over(orderby e.id)),
                   (firstValue(e.name) over(partitionby e.deptId orderby e.name)),
                   (lastValue(e.name) over(orderby e.name)),
                   (nthValue(e.name, 0) over(orderby e.id)),
                   (nthValue(e.name, 2) over(orderby e.id)),
                   (count() over(orderby e.id)),
                   (count(e.deptId) over()),
                   (count(distinct(e.salary)) over(partitionby e.deptId)),
                   (sum(e.salary) over(orderby e.id)),
                   (sum(e -> e.salary) over(partitionby e.deptId)),
                   (avg(e.salary) over(partitionby e.deptId)),
                   (min(e.salary) over(partitionby e.deptId)),
                   (max(e.salary) over())
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def sql = ansi(query).sql
        def jooq = JooqRenderer.render(query, 'H2').sql.toLowerCase()

        assert sql.contains('RANK() OVER (PARTITION BY e.deptId ORDER BY e.salary ASC NULLS LAST)')
        assert sql.contains('DENSE_RANK() OVER (ORDER BY e.salary DESC NULLS LAST)')
        assert sql.contains('CUME_DIST() OVER (ORDER BY e.salary ASC NULLS LAST)')
        assert sql.contains('LEAD(e.name) OVER (ORDER BY e.id ASC NULLS LAST)')
        assert sql.contains('LEAD(e.name, ?, ?) OVER (ORDER BY e.id ASC NULLS LAST)')
        assert sql.contains('LAG(e.salary, ?) OVER (ORDER BY e.id ASC NULLS LAST)')
        assert sql.contains('FIRST_VALUE(e.name) OVER (PARTITION BY e.deptId ORDER BY e.name ASC NULLS LAST ' +
                'ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)')
        assert sql.contains('LAST_VALUE(e.name) OVER (ORDER BY e.name ASC NULLS LAST ' +
                'ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)')
        assert sql.contains('NTH_VALUE(e.name, ?) OVER (ORDER BY e.id ASC NULLS LAST ' +
                'ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)')
        assert sql.contains('COUNT(*) OVER (ORDER BY e.id ASC NULLS LAST ' +
                'ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)')
        assert sql.contains('COUNT(e.deptId) OVER ()')
        assert sql.contains('COUNT(DISTINCT e.salary) OVER (PARTITION BY e.deptId)')
        assert sql.contains('COALESCE(SUM(e.salary) OVER (ORDER BY e.id ASC NULLS LAST ' +
                'ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING), 0)')
        assert sql.contains('COALESCE(SUM(e.salary) OVER (PARTITION BY e.deptId), 0)')
        assert sql.contains('AVG(e.salary) OVER (PARTITION BY e.deptId)')
        assert sql.contains('MIN(e.salary) OVER (PARTITION BY e.deptId)')
        assert sql.contains('MAX(e.salary) OVER ()')
        assert !sql.contains('window functions are deferred')

        assert jooq.contains('rank() over (partition by e.deptid order by e.salary asc nulls last)')
        assert jooq.contains('nth_value(e.name, ?) over (order by e.id asc nulls last rows between unbounded preceding and unbounded following)')
        assert jooq.contains('count(*) over (order by e.id asc nulls last rows between unbounded preceding and unbounded following)')
        assert jooq.contains('coalesce(sum(e.salary) over (order by e.id asc nulls last rows between unbounded preceding and unbounded following), 0)')
        assert jooq.contains('lead(e.name, ?, ?) over (order by e.id asc nulls last)')
    }

    @Test
    void testNtileUsesGinqBucketsAndPercentRankKeepsASingleton() {
        def parsed = parse('''
            from e in 'employees'
            select (ntile(4) over(partitionby e.deptId orderby e.salary)),
                   (percentRank() over(partitionby e.deptId orderby e.salary))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def sql = ansi(query).sql
        assert sql.contains('(CAST(? * (ROW_NUMBER() OVER (PARTITION BY e.deptId ORDER BY e.salary ASC NULLS LAST) - 1) AS BIGINT) ' +
                '/ CAST(COUNT(*) OVER (PARTITION BY e.deptId) AS BIGINT))')
        assert !sql.contains('FLOOR')
        assert sql.contains('CASE WHEN COUNT(*) OVER (PARTITION BY e.deptId) = 1 THEN 1 ' +
                'ELSE PERCENT_RANK() OVER (PARTITION BY e.deptId ORDER BY e.salary ASC NULLS LAST) END')

        def sqlite = new SqlRenderer(Dialects.resolve('sqlite')).render(query).sql
        assert sqlite.contains('AS INTEGER')
        assert !sqlite.contains('FLOOR')

        def jooq = JooqRenderer.render(query, 'H2').sql.toLowerCase()
        assert !jooq.contains('floor')
        assert jooq.contains('bigint')
        assert jooq.contains('row_number() over (partition by e.deptid order by e.salary asc nulls last)')
        assert jooq.contains('count(*) over (partition by e.deptid)')
        assert jooq.contains('percent_rank() over (partition by e.deptid order by e.salary asc nulls last)')
        assert jooq.contains('case when')
    }

    @Test
    void testRowsFrameOnAnAggregateAndExplicitBounds() {
        def parsed = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.id rows -1, 1)),
                   (avg(e.salary) over(orderby e.salary range null, 0)),
                   (min(e.salary) over(rows 0, null))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def sql = ansi(query).sql
        assert sql.contains('COALESCE(SUM(e.salary) OVER (ORDER BY e.id ASC NULLS LAST ' +
                'ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING), 0)')
        assert sql.contains('AVG(e.salary) OVER (ORDER BY e.salary ASC NULLS LAST ' +
                'RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)')
        assert sql.contains('MIN(e.salary) OVER (ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)')
        def jooq = JooqRenderer.render(query, 'H2').sql.toLowerCase()
        assert jooq.contains('sum(e.salary) over (order by e.id asc nulls last rows between 1 preceding and 1 following)')
        assert jooq.contains('avg(e.salary) over (order by e.salary asc nulls last range between unbounded preceding and current row)')
    }

    @Test
    void testRankingFunctionRejectsAFrame() {
        def parsed = parse('''
            from e in 'employees'
            select (rank() over(orderby e.salary rows -1, 1))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('does not accept `rows`')
        assert err.message.contains('GINQ applies that frame')
        assert !err.message.contains('window functions are deferred')
    }

    @Test
    void testOffsetFunctionsRejectAFrameWithoutApplyingIt() {
        def parsed = parse('''
            from e in 'employees'
            select (lead(e.name) over(orderby e.id rows -1, 1)),
                   (rowNumber() over(orderby e.id rows 0, 0))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('does not accept `rows`')
        assert err.message.contains('SQL defines that function without a frame')
        assert !err.message.contains('GINQ applies')
    }

    @Test
    void testRankWithoutOrderStaysInMemory() {
        def parsed = parse('''
            from e in 'employees'
            select (denseRank() over(partitionby e.deptId))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('needs `orderby`')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testFrameThatEndsBeforeItStarts() {
        def parsed = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows 1, -1))
        ''')
        def err = shouldFail(MultipleCompilationErrorsException) {
            translate(parsed, null)
        }
        assert err.message.contains('ends before it starts')
    }

    @Test
    void testNthValueAndOffsetsRejectIllegalLiterals() {
        def negativeIndex = parse('''
            from e in 'employees'
            select (nthValue(e.name, -1) over(orderby e.id))
        ''')
        def indexErr = shouldFail(MultipleCompilationErrorsException) {
            translate(negativeIndex, null)
        }
        assert indexErr.message.contains('whole number >= 0')

        def negativeLead = parse('''
            from e in 'employees'
            select (lead(e.name, -2) over(orderby e.id))
        ''')
        def leadErr = shouldFail(MultipleCompilationErrorsException) {
            translate(negativeLead, null)
        }
        assert leadErr.message.contains('offset must be a whole number >= 0')
    }

    @Test
    void testWindowOnlyInSelectAndOrderBy() {
        def where = parse('''
            from e in 'employees'
            where (rowNumber() over(orderby e.id)) > 0
            select e.name
        ''')
        def whereErr = shouldFail(MultipleCompilationErrorsException) {
            translate(where, null)
        }
        assert whereErr.message.contains('only supported in `select` and `orderby`')
        assert !whereErr.message.contains('mixedMode: true')

        def nested = parse('''
            from e in 'employees'
            select (sum(rowNumber() over(orderby e.id)) over())
        ''')
        def nestedErr = shouldFail(MultipleCompilationErrorsException) {
            translate(nested, null)
        }
        assert nestedErr.message.contains('cannot be nested')

        def insideAggregate = parse('''
            from e in 'employees'
            select (sum((rowNumber() over(orderby e.id))))
        ''')
        def aggregateErr = shouldFail(MultipleCompilationErrorsException) {
            translate(insideAggregate, null)
        }
        assert aggregateErr.message.contains('nested inside an aggregate')
        assert !aggregateErr.message.contains('mixedMode: true')

        def ordered = parse('''
            from e in 'employees'
            orderby (rowNumber() over(orderby e.salary))
            select e.name
        ''')
        def query = translate(ordered, null)
        assert noErrors(ordered)
        assert ansi(query).sql.contains('ORDER BY ROW_NUMBER() OVER (ORDER BY e.salary ASC NULLS LAST) - 1')
    }

    @Test
    void testSetOperationOrdersByAWindowProjection() {
        def parsed = parse('''
            from a in 'employees'
            select (rowNumber() over(orderby a.id))
            union
            from b in 'employees'
            orderby (rowNumber() over(orderby b.id))
            select (rowNumber() over(orderby b.id))
        ''')
        def node = translateNode(parsed)
        assert noErrors(parsed)
        assert node instanceof SqlSetQuery
        def sql = new SqlRenderer(new AnsiDialect()).render(node).sql
        assert sql.contains('UNION')
        assert sql.endsWith('ORDER BY 1 ASC NULLS LAST')
        assert sql.count('ROW_NUMBER()') == 2
    }

    @Test
    void testSqliteRejectsANumericRangeOffset() {
        def parsed = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary range -2, 0))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def err = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('sqlite')).render(query)
        }
        assert err.message.contains('numeric RANGE offset')
        def jooqErr = shouldFail(IllegalArgumentException) {
            JooqRenderer.render(query, 'SQLITE')
        }
        assert jooqErr.message.contains('numeric RANGE offset')
        assert new SqlRenderer(Dialects.resolve('sqlite')).render(queryOf(new SqlWindowCall(
                new SqlFunction('SUM', [new SqlColumn('e', 'salary')]),
                new SqlWindow([], [new SqlOrderSpec(new SqlColumn('e', 'salary'), true, true)],
                        new SqlWindowFrame(SqlWindowFrame.Unit.RANGE,
                                SqlWindowBound.unboundedPreceding(), SqlWindowBound.currentRow()))))).sql
                .contains('RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW')
    }

    @Test
    void testDescendingRangeIsMirroredIntoSortOrder() {
        def parsed = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary in desc range -2, 0)),
                   (sum(e.salary) over(orderby e.salary in desc range 1, 2)),
                   (sum(e.salary) over(orderby e.salary in desc range null, 1)),
                   (sum(e.salary) over(orderby e.salary in desc range 1, null)),
                   (sum(e.salary) over(orderby e.salary in desc rows -1, 1))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def sql = ansi(query).sql
        assert sql.contains('RANGE BETWEEN CURRENT ROW AND 2 FOLLOWING')
        assert sql.contains('RANGE BETWEEN 2 PRECEDING AND 1 PRECEDING')
        assert sql.contains('RANGE BETWEEN 1 PRECEDING AND UNBOUNDED FOLLOWING')
        assert sql.contains('RANGE BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING')
        assert sql.contains('ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING')
        def jooq = JooqRenderer.render(query, 'H2').sql.toLowerCase()
        assert jooq.contains('range between current row and 2 following')
        assert jooq.contains('rows between 1 preceding and 1 following')
    }

    @Test
    void testSeveralOrderKeysAndNullsFirst() {
        def parsed = parse('''
            from e in 'employees'
            select (rank() over(orderby e.salary, e.name in asc(nullsfirst)))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def sql = ansi(query).sql
        assert sql.contains('RANK() OVER (ORDER BY e.salary ASC NULLS LAST, e.name ASC NULLS FIRST)')
    }

    @Test
    void testMySqlRejectsANumericRangeOffset() {
        def parsed = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary range -2, 0))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        def err = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('mysql')).render(query)
        }
        assert err.message.contains('numeric RANGE offset')
        assert err.message.contains('extra sort key')
        def maria = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('mariadb')).render(query)
        }
        assert maria.message.contains('mariadb')
        def jooqErr = shouldFail(IllegalArgumentException) {
            JooqRenderer.render(query, 'MYSQL')
        }
        assert jooqErr.message.contains('extra sort key')

        def rows = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows -1, 1))
        ''')
        def rowsQuery = translate(rows, null)
        def mysqlRows = new SqlRenderer(Dialects.resolve('mysql')).render(rowsQuery).sql
        assert mysqlRows.contains('ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING')
        assert mysqlRows.contains('IS NULL')
    }

    @Test
    void testSqliteRejectsCountDistinctOver() {
        def parsed = parse('''
            from e in 'employees'
            select (count(distinct(e.salary)) over(partitionby e.deptId))
        ''')
        def query = translate(parsed, null)
        assert noErrors(parsed)
        assert ansi(query).sql.contains('COUNT(DISTINCT e.salary) OVER (PARTITION BY e.deptId)')
        def err = shouldFail(IllegalArgumentException) {
            new SqlRenderer(Dialects.resolve('sqlite')).render(query)
        }
        assert err.message.contains('COUNT(DISTINCT)')
        def jooqErr = shouldFail(IllegalArgumentException) {
            JooqRenderer.render(query, 'SQLITE')
        }
        assert jooqErr.message.contains('COUNT(DISTINCT)')
    }

    @Test
    void testRepeatedAndUnknownWindowClauses() {
        def repeated = parse('''
            from e in 'employees'
            select (rowNumber() over(partitionby e.deptId partitionby e.active orderby e.id))
        ''')
        def repeatedErr = shouldFail(MultipleCompilationErrorsException) {
            translate(repeated, null)
        }
        assert repeatedErr.message.contains('more than once')

        def both = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows -1, 1 range -1, 1))
        ''')
        def bothErr = shouldFail(MultipleCompilationErrorsException) {
            translate(both, null)
        }
        assert bothErr.message.contains('cannot both be used')

        def unknown = parse('''
            from e in 'employees'
            select (rowNumber() over(orderby e.id groups 1))
        ''')
        def unknownErr = shouldFail(MultipleCompilationErrorsException) {
            translate(unknown, null)
        }
        assert unknownErr.message.contains('Unknown window clause')

        def noOrder = parse('''
            from e in 'employees'
            select (sum(e.salary) over(range 1, 2))
        ''')
        def noOrderErr = shouldFail(MultipleCompilationErrorsException) {
            translate(noOrder, null)
        }
        assert noOrderErr.message.contains('`orderby` is expected when using `range`')

        def twoKeys = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary, e.name range -1, 1))
        ''')
        def twoKeysErr = shouldFail(MultipleCompilationErrorsException) {
            translate(twoKeys, null)
        }
        assert twoKeysErr.message.contains('Only one field is expected')

        def fraction = parse('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.id rows 0.5, 1))
        ''')
        def fractionErr = shouldFail(MultipleCompilationErrorsException) {
            translate(fraction, null)
        }
        assert fractionErr.message.contains('whole numbers')
    }

    private static SqlFunctionRegistry plainRegistry() {
        def registry = SqlFunctionRegistry.standard().copy()
        registry.register(SqlFunctionMapping.window('myNumber', 'ROW_NUMBER'))
        registry.register(SqlFunctionMapping.window('mySum', 'SUM', 1))
        return registry
    }

    private static SqlQuery translate(Parsed parsed, SqlFunctionRegistry registry) {
        SqlQueryNode node = translateNode(parsed, registry)
        assert node instanceof SqlQuery
        return (SqlQuery) node
    }

    private static SqlQueryNode translateNode(Parsed parsed, SqlFunctionRegistry registry = null) {
        GinqSqlWalker walker = new GinqSqlWalker(parsed.sourceUnit)
        if (registry != null) walker.setFunctions(registry)
        return walker.translate(parsed.ginq)
    }

    private static Parsed parse(String query) {
        String code = """
            def hello() {
                def gqc = {
                    ${query}
                }
            }
        """
        SourceUnit sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource('window.groovy', code)
            compile(Phases.CONVERSION)
        }.ast
        def method = ast.classes[0].methods.find { it.name == 'hello' }
        ExpressionStatement statement = (ExpressionStatement) ((BlockStatement) method.code).statements[0]
        DeclarationExpression declaration = (DeclarationExpression) statement.expression
        ClosureExpression closure = (ClosureExpression) declaration.rightExpression
        AbstractGinqExpression built = new GinqAstBuilder(sourceUnit).buildAST(closure.code)
        assert built instanceof GinqExpression || built instanceof SetOperationExpression
        return new Parsed(sourceUnit, built)
    }

    private static boolean noErrors(Parsed parsed) {
        return parsed.sourceUnit.errorCollector.errorCount == 0
    }

    private static SqlWindowBound bound(SqlWindowBound.Kind kind, int offset) {
        switch (kind) {
            case SqlWindowBound.Kind.UNBOUNDED_PRECEDING: return SqlWindowBound.unboundedPreceding()
            case SqlWindowBound.Kind.UNBOUNDED_FOLLOWING: return SqlWindowBound.unboundedFollowing()
            case SqlWindowBound.Kind.CURRENT_ROW: return SqlWindowBound.currentRow()
            case SqlWindowBound.Kind.PRECEDING: return SqlWindowBound.preceding(new SqlLiteral(offset))
            case SqlWindowBound.Kind.FOLLOWING: return SqlWindowBound.following(new SqlLiteral(offset))
            default: throw new AssertionError(kind)
        }
    }

    private static String boundText(SqlWindowBound.Kind kind, int offset) {
        switch (kind) {
            case SqlWindowBound.Kind.UNBOUNDED_PRECEDING: return 'UNBOUNDED PRECEDING'
            case SqlWindowBound.Kind.UNBOUNDED_FOLLOWING: return 'UNBOUNDED FOLLOWING'
            case SqlWindowBound.Kind.CURRENT_ROW: return 'CURRENT ROW'
            case SqlWindowBound.Kind.PRECEDING: return offset + ' PRECEDING'
            case SqlWindowBound.Kind.FOLLOWING: return offset + ' FOLLOWING'
            default: throw new AssertionError(kind)
        }
    }

    private static SqlQuery queryOf(SqlExpr expr) {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', 'e')
        query.projections << new SqlProjection(expr)
        return query
    }

    private static RenderedSql ansi(SqlQuery query) {
        return new SqlRenderer(new AnsiDialect()).render(query)
    }

    private static class Parsed {
        final SourceUnit sourceUnit
        final AbstractGinqExpression ginq

        private Parsed(SourceUnit sourceUnit, AbstractGinqExpression ginq) {
            this.sourceUnit = sourceUnit
            this.ginq = ginq
        }
    }
}
