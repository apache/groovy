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
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlDerivedTable
import org.apache.groovy.ginq.provider.sql.ir.SqlExists
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlIn
import org.apache.groovy.ginq.provider.sql.ir.SqlIsNull
import org.apache.groovy.ginq.provider.sql.ir.SqlJoin
import org.apache.groovy.ginq.provider.sql.ir.SqlLike
import org.apache.groovy.ginq.provider.sql.ir.SqlLiteral
import org.apache.groovy.ginq.provider.sql.ir.SqlOrderSpec
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
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.SQLException

import static groovy.test.GroovyAssert.shouldFail

class SqlTranslationReachabilityTest {
    private Sql db

    @BeforeEach
    void setUp() {
        db = GinqSqlTestSupport.newPopulatedDb()
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void windowBoundsLiteralsErrorsAndDescendingRanges() {
        String mirrored = sql('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary in desc range -2, 3))
        ''')
        assert mirrored.toUpperCase(Locale.ROOT).contains('3 PRECEDING')
        assert mirrored.toUpperCase(Locale.ROOT).contains('2 FOLLOWING')

        String unbounded = sql('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows null, 0))
        ''')
        assert unbounded.toUpperCase(Locale.ROOT).contains('UNBOUNDED PRECEDING')
        assert unbounded.toUpperCase(Locale.ROOT).contains('CURRENT ROW')

        String huge = sql('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.id rows -9223372036854775808, 9223372036854775807))
        ''')
        assert huge.toUpperCase(Locale.ROOT).contains('UNBOUNDED PRECEDING')
        assert huge.toUpperCase(Locale.ROOT).contains('UNBOUNDED FOLLOWING')

        assert failure('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows bound, 1))
        ''').contains('literals')

        assert failure('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows 2, -1))
        ''').contains('ends before it starts')

        assert failure('''
            from e in 'employees'
            select (sum(e.salary) over(orderby e.salary rows 1.5, 2))
        ''').contains('whole numbers')
    }

    @Test
    void operandKindsCastsAndRejectedOperators() {
        String text = sql('''
            from e in 'employees'
            where e.id == 1
            select e.name + '!' as label, (e.salary as double) as wide, (e.salary as BigDecimal) as money,
                   e.name.substring(1) as tail, e.name.length() + 1 as n
        ''')
        assert text.contains('||')
        assert text.toUpperCase(Locale.ROOT).contains('DOUBLE')
        assert text.toUpperCase(Locale.ROOT).contains('DECIMAL')
        assert text.toUpperCase(Locale.ROOT).contains('SUBSTRING')

        String branches = sql('''
            from e in 'employees'
            where e.id in [] || e.id !in [null] || !(e.name == null)
            select e.salary > 1000 ? 'high' : e.name
        ''')
        assert branches.toUpperCase(Locale.ROOT).contains('CASE')
        assert branches.contains('1 = 0') || branches.contains('1 = 1')

        assert failure('''
            from e in 'employees'
            select e.name ?: 'missing'
        ''').contains('Elvis')

        assert failure('''
            from e in 'employees'
            where e.salary & 1
            select e.name
        ''').contains('Operator')

        assert failure('''
            from e in 'employees'
            select e.name.substring(1, 2, 3)
        ''').contains('substring')

        assert failure('''
            from e in 'employees'
            select e.name + e.salary
        ''').contains('not translated')
    }

    @Test
    void setOperationsHoistMatchingOrderAndRejectTheRest() {
        String ordered = sql('''
            from e in 'employees'
            select e.name
            union
            from e in 'employees'
            orderby e.name
            select e.name
        ''')
        assert ordered.toUpperCase(Locale.ROOT).contains('UNION')
        assert ordered.toUpperCase(Locale.ROOT).contains('ORDER BY 1')

        String minus = render(new SqlSetQuery(plain('a'), SqlSetQuery.SetOp.MINUS, plain('b')))
        assert minus.toUpperCase(Locale.ROOT).contains('EXCEPT') || minus.toUpperCase(Locale.ROOT).contains('MINUS')

        assert failure('''
            from e in 'employees'
            select e.name
            union
            from e in 'employees'
            orderby e.salary
            select e.name
        ''').contains('orderby')

        Parsed left = parse('''
            from e in 'employees'
            select e.name
        ''')
        Parsed right = parse('''
            from d in 'departments'
            select d.name
        ''')
        SetOperationExpression unknown = new SetOperationExpression(left.ginq, 'except', (GinqExpression) right.ginq)
        def err = shouldFail {
            new GinqSqlWalker(left.sourceUnit).translate(unknown)
        }
        assert err.message.contains('Unknown set operation')
    }

    @Test
    void jooqRendersCastTypesFunctionsAndRejectsANonLiteralOffset() {
        for (String type : ['BOOLEAN', 'TINYINT', 'SMALLINT', 'INTEGER', 'INT', 'BIGINT', 'REAL', 'FLOAT',
                            'DOUBLE', 'DOUBLE PRECISION', 'DECIMAL', 'NUMERIC', 'CHAR', 'CHARACTER',
                            'VARCHAR', 'CHARACTER VARYING', 'DATE', 'TIME', 'TIMESTAMP', 'CLOB', 'BLOB',
                            'BINARY', 'VARBINARY', 'VARCHAR(12)', 'DECIMAL(10)', 'DECIMAL(10,2)']) {
            String rendered = jooq(queryOf(new SqlCast(col('e', 'name'), type))).toUpperCase(Locale.ROOT)
            assert rendered.contains('CAST')
            String expected = type.takeWhile { it != '(' }
            String fragment = expected == 'INTEGER' ? 'INT'
                    : expected == 'DOUBLE PRECISION' ? 'DOUBLE'
                    : expected == 'CHARACTER' ? 'CHAR'
                    : expected == 'CHARACTER VARYING' ? 'VARCHAR'
                    : expected
            assert rendered.contains(fragment)
        }

        assert jooq(queryOf(new SqlFunction('COALESCE', [col('e', 'name'), new SqlLiteral('x'), new SqlLiteral('y')])))
                .toLowerCase(Locale.ROOT).contains('coalesce')
        assert jooq(queryOf(new SqlFunction('COUNT', [col('e', 'id')], true)))
                .toLowerCase(Locale.ROOT).contains('distinct')
        assert jooq(queryOf(new SqlFunction('SUBSTRING', [col('e', 'name'), new SqlLiteral(1)])))
                .toLowerCase(Locale.ROOT).contains('substring')
        assert jooq(queryOf(new SqlFunction('MOD', [col('e', 'salary'), new SqlLiteral(2)])))
                .toLowerCase(Locale.ROOT).contains('mod')
        assert jooq(queryOf(new SqlFunction('ROUND', [col('e', 'salary'), new SqlLiteral(2)])))
                .toLowerCase(Locale.ROOT).contains('round')
        assert JooqRenderer.render(queryOf(new SqlFunction('POSITION', [new SqlLiteral('a'), col('e', 'name')])), 'SQLITE')
                .sql.toLowerCase(Locale.ROOT).contains('instr')
        assert JooqRenderer.render(queryOf(new SqlFunction('GREATEST', [col('e', 'salary'), new SqlLiteral(1), new SqlLiteral(2)])), 'SQLITE')
                .sql.toLowerCase(Locale.ROOT).contains('max')
        assert JooqRenderer.render(queryOf(new SqlFunction('POSITION', [new SqlLiteral('a'), col('e', 'name')])), 'MYSQL')
                .sql.toLowerCase(Locale.ROOT).contains('locate')

        SqlQuery window = plain('e')
        window.projections << new SqlProjection(new SqlWindowCall(
                new SqlFunction('SUM', [col('e', 'salary')]),
                new SqlWindow([], [], new SqlWindowFrame(SqlWindowFrame.Unit.ROWS,
                        SqlWindowBound.preceding(col('e', 'n')),
                        SqlWindowBound.currentRow()))), 'near')
        def offset = shouldFail {
            JooqRenderer.render(window, 'H2')
        }
        assert offset.message.contains('integer literal') || offset.message.contains('offset')

        String custom = jooq(queryOf(new SqlFunction('NOT_A_FUNC', [col('e', 'name')])))
        assert custom.toLowerCase(Locale.ROOT).contains('not_a_func')

        String chars = render(queryOf(new SqlLiteral('a\'b' as Character)))
        assert chars.contains("'a'") || chars.contains("a")
        String flag = render(queryOf(new SqlLiteral(Boolean.TRUE)))
        assert flag.toUpperCase(Locale.ROOT).contains('TRUE')

        SqlQuery inner = plain('i')
        SqlQuery outer = new SqlQuery()
        outer.from = new SqlDerivedTable(new SqlSetQuery(inner, SqlSetQuery.SetOp.UNION, plain('j')), 'u')
        outer.projections << new SqlProjection(new SqlStar('u'))
        String derived = render(outer)
        assert derived.toUpperCase(Locale.ROOT).contains('UNION')
        assert derived.contains('(')
    }

    @Test
    void mixedModeRejectsMalformedSqlAndRebuildsRows() {
        assert message('SELECT a.* FROM (SELECT 1').contains('unclosed parenthesis')
        assert message('SELECT a.* FROM "employees a').contains('unclosed quoted')
        assert message('SELECT a.* FROM hr.+ a').contains('dangling qualifier')
        assert message('SELECT a.* FROM employees').contains('no alias')
        assert message('SELECT a.* FROM ').contains('missing a source')
        assert message('SELECT a.* FROM .t a').contains('no table name')

        String expanded = SqlGinqRuntime.expandAndExecute(db,
                'select q.__ginqcol_0_name from (select name as "__ginqstarcol_0_name" from employees where id = ?) q',
                [1], [['name']], '"').toList()
        assert expanded.toString().contains('Alice')

        def outside = shouldFail(SQLException) {
            SqlGinqRuntime.expandAndExecute(db, 'select q.__ginqsrc_3 from employees q', [], [['name']], '"')
        }
        assert outside.message.contains('outside the column layout')

        def unknown = shouldFail(SQLException) {
            SqlGinqRuntime.expandAndExecute(db, 'select q.__ginqcol_0_missing from employees q', [], [['name']], '"')
        }
        assert unknown.message.contains('unknown column')

        def blank = shouldFail(SQLException) {
            SqlGinqRuntime.prepareMixedSql(
                    'SELECT __ginq_src.*, 1 AS __ginq_marker FROM employees __ginq_src WHERE __ginqref_0_',
                    [['id']], '"' as char)
        }
        assert blank.message.contains('blank') || blank.message.contains('unknown')

        assert SqlGinqRuntime.columnCount(db, 'select id, name from employees where id = ?', [1]) == 2

        def zipped = SqlGinqRuntime.zipAliases(db,
                'SELECT e.*, d.* FROM employees e LEFT JOIN departments d ON e.deptId = d.id WHERE e.id = ?',
                [4],
                [['id', 'name', 'salary', 'deptId'], ['id', 'name']],
                '')
        assert zipped[0][0].get('name') == 'Dave'
        assert zipped[1][0] == null
        assert SqlGinqRuntime.mixedOrdinal(zipped[0][0]) == 0
    }

    @Test
    void nestedFullJoinsRewriteEveryExpressionShape() {
        SqlQuery nested = new SqlQuery()
        nested.from = new SqlTableRef('offices', 'o')
        nested.projections << new SqlProjection(col('o', 'id'))
        nested.where = new SqlBinary('=', col('e', 'id'), col('o', 'id'))

        SqlQuery query = new SqlQuery()
        query.distinct = true
        query.from = new SqlTableRef('employees', 'e')
        query.joins << new SqlJoin(SqlJoin.Type.FULL, new SqlTableRef('departments', 'd'),
                new SqlBinary('=', col('e', 'deptId'), col('d', 'id')))
        query.joins << new SqlJoin(SqlJoin.Type.FULL, new SqlDerivedTable(plain('o'), 'off'),
                new SqlBinary('=', col('e', 'id'), col('off', 'id')))
        query.where = new SqlBinary('AND',
                new SqlUnary('NOT', new SqlIsNull(col('e', 'name'), false)),
                new SqlBinary('OR',
                        new SqlLike(col('e', 'name'), new SqlLiteral('%a%')),
                        new SqlIn(col('e', 'salary'), [new SqlLiteral(1), col('e', 'salary')], false)))
        query.projections << new SqlProjection(new SqlCaseWhen(
                new SqlBinary('>', col('e', 'salary'), new SqlLiteral(0)),
                new SqlCast(new SqlFunction('UPPER', [col('e', 'name')], true), 'VARCHAR'),
                new SqlLiteral('x')), 'label')
        query.projections << new SqlProjection(new SqlExists(nested))
        query.projections << new SqlProjection(new SqlScalarQuery(plain('s')))
        query.groupBy << col('e', 'deptId')
        query.having = new SqlBinary('>', col('e', 'salary'), new SqlLiteral(0))
        query.orderBy << new SqlOrderSpec(col('e', 'name'), true, true)
        query.fetch = new SqlLiteral(3)

        String laid = render(MixedSourceLayout.layoutComplete(query))
        String upper = laid.toUpperCase(Locale.ROOT)
        assert upper.contains('UNION ALL')
        assert !upper.contains('FULL OUTER JOIN')
        assert upper.contains('__GINQ')
        assert upper.contains('EXISTS')
        assert upper.contains('CASE')
        assert upper.contains('LIKE')

        SqlSetQuery both = new SqlSetQuery(query, SqlSetQuery.SetOp.INTERSECT, fullOf('x', 'y'))
        String copied = render(MixedSourceLayout.layoutComplete(both))
        assert copied.toUpperCase(Locale.ROOT).contains('INTERSECT')
        assert copied.toUpperCase(Locale.ROOT).count('UNION ALL') >= 2
    }

    private static SqlQuery fullOf(String leftAlias, String rightAlias) {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', leftAlias)
        query.joins << new SqlJoin(SqlJoin.Type.FULL, new SqlTableRef('departments', rightAlias),
                new SqlBinary('=', col(leftAlias, 'id'), col(rightAlias, 'id')))
        query.projections << new SqlProjection(col(leftAlias, 'name'))
        return query
    }

    private static String message(String sqlText) {
        def err = shouldFail {
            SqlGinqRuntime.prepareMixedSql(sqlText, [['id']], '"' as char)
        }
        return err.message
    }

    private static String sql(String query) {
        Parsed parsed = parse(query)
        SqlQueryNode node = new GinqSqlWalker(parsed.sourceUnit).translate(parsed.ginq)
        assert parsed.sourceUnit.errorCollector.errorCount == 0
        return render(node)
    }

    private static String failure(String query) {
        Parsed parsed = parse(query)
        def err = shouldFail {
            new GinqSqlWalker(parsed.sourceUnit).translate(parsed.ginq)
        }
        return err.message
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
            sourceUnit = addSource('reach.groovy', code)
            compile(Phases.CONVERSION)
        }.ast
        def method = ast.classes[0].methods.find { it.name == 'hello' }
        ExpressionStatement statement = (ExpressionStatement) ((BlockStatement) method.code).statements[0]
        def declaration = statement.expression
        def closure = declaration.rightExpression
        AbstractGinqExpression built = new GinqAstBuilder(sourceUnit).buildAST(closure.code)
        return new Parsed(sourceUnit, built)
    }

    private static SqlQuery plain(String alias) {
        SqlQuery query = new SqlQuery()
        query.from = new SqlTableRef('employees', alias)
        query.projections << new SqlProjection(col(alias, 'name'))
        return query
    }

    private static SqlQuery queryOf(SqlExpr expr) {
        SqlQuery query = plain('e')
        query.projections.clear()
        query.projections << new SqlProjection(expr)
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

    private static final class Parsed {
        final SourceUnit sourceUnit
        final AbstractGinqExpression ginq

        private Parsed(SourceUnit sourceUnit, AbstractGinqExpression ginq) {
            this.sourceUnit = sourceUnit
            this.ginq = ginq
        }
    }
}
