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
 * Proves the window-function extension point without registering any GINQ
 * window function on {@link SqlFunctionRegistry#standard()}. A copy of the
 * registry supplies a fake {@code rowNumber} mapping; the walker must build
 * {@link SqlWindowCall} and the renderers must emit {@code OVER}.
 */
class SqlWindowTranslationTest {

    @Test
    void testWalkerBuildsOverForARegisteredWindowFunction() {
        def parsed = parse('''
            from e in 'nums'
            select (rowNumber() over(orderby n))
        ''')
        def query = translate(parsed, rowNumberRegistry())
        assert noErrors(parsed)

        assert query.projections.size() == 1
        assert query.projections[0].expr instanceof SqlWindowCall
        SqlWindowCall call = (SqlWindowCall) query.projections[0].expr
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
        assert 'SELECT ROW_NUMBER() OVER (ORDER BY ? ASC NULLS LAST) FROM nums e' == ansi.sql
        assert ['n'] == ansi.parameters*.text

        def mysql = new SqlRenderer(Dialects.resolve('mysql')).render(query)
        assert 'SELECT ROW_NUMBER() OVER (ORDER BY (? IS NULL) ASC, ? ASC) FROM `nums` `e`' == mysql.sql
        assert ['n', 'n'] == mysql.parameters*.text

        def jooq = JooqRenderer.render(query, 'H2').sql
        assert 'select ROW_NUMBER() over (order by ? asc nulls last) from nums e' == jooq
    }

    @Test
    void testEmptyOverRendersEmptyParentheses() {
        def parsed = parse('''
            from e in 'nums'
            select (rowNumber() over())
        ''')
        def query = translate(parsed, rowNumberRegistry())
        assert noErrors(parsed)
        SqlWindowCall call = (SqlWindowCall) query.projections[0].expr
        assert call.window.partitionBy.empty
        assert call.window.orderBy.empty
        assert call.window.frame == null
        assert 'SELECT ROW_NUMBER() OVER () FROM nums e' == new SqlRenderer(new AnsiDialect()).render(query).sql
        assert 'select ROW_NUMBER() over () from nums e' == JooqRenderer.render(query, 'H2').sql
    }

    @Test
    void testPartitionOrderAndRowsFrame() {
        def registry = rowNumberRegistry()
        registry.register(SqlFunctionMapping.window('sum', 'SUM', 1))
        def parsed = parse('''
            from e in 'employees'
            select (rowNumber() over(partitionby e.deptId, e.name.toUpperCase() orderby e.salary in desc rows -1, 1)),
                   (sum(e.salary) over(orderby e.salary range null, 0))
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
            select (sum(e.salary) over(partitionby e.deptId)), (rowNumber() over(orderby e.salary))
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
            translate(parsed, rowNumberRegistry())
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
            translate(parsed, rowNumberRegistry())
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

    private static SqlFunctionRegistry rowNumberRegistry() {
        def registry = SqlFunctionRegistry.standard().copy()
        registry.register(SqlFunctionMapping.window('rowNumber', 'ROW_NUMBER'))
        return registry
    }

    private static SqlQuery translate(Parsed parsed, SqlFunctionRegistry registry) {
        GinqSqlWalker walker = new GinqSqlWalker(parsed.sourceUnit)
        if (registry != null) walker.setFunctions(registry)
        SqlQueryNode node = walker.translate(parsed.ginq)
        assert node instanceof SqlQuery
        return (SqlQuery) node
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
        assert built instanceof GinqExpression
        return new Parsed(sourceUnit, (GinqExpression) built)
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
        final GinqExpression ginq

        private Parsed(SourceUnit sourceUnit, GinqExpression ginq) {
            this.sourceUnit = sourceUnit
            this.ginq = ginq
        }
    }
}
