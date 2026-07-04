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

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlCountStar
import org.apache.groovy.ginq.provider.sql.ir.SqlDerivedTable
import org.apache.groovy.ginq.provider.sql.ir.SqlExists
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
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
import org.apache.groovy.ginq.provider.sql.ir.SqlTableSource
import org.apache.groovy.ginq.provider.sql.ir.SqlUnary
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.expr.Expression
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.JoinType
import org.jooq.Param
import org.jooq.SQLDialect
import org.jooq.Select
import org.jooq.SelectFieldOrAsterisk
import org.jooq.SelectQuery
import org.jooq.SortField
import org.jooq.Table
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL

import java.util.regex.Matcher

/**
 * Renders the SQL IR through jOOQ at compile time: the IR is mapped onto
 * jOOQ's query model and rendered for the requested {@link SQLDialect} with
 * named parameters, which are then converted back to positional {@code ?}
 * placeholders so execution is identical to the native provider.
 * <p>
 * The imperative {@link SelectQuery} model API is used with static compilation
 * throughout: jOOQ query objects implement {@link Iterable}, so dynamic
 * dispatch could select Groovy GDK extension methods, e.g. {@code groupBy},
 * over jOOQ's own.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class JooqRenderer {
    private final SQLDialect dialect
    private final List<Expression> paramExpressions = []

    private JooqRenderer(SQLDialect dialect) {
        this.dialect = dialect
    }

    static RenderedSql render(SqlQueryNode queryNode, String dialectName) {
        return new JooqRenderer(resolveDialect(dialectName)).doRender(queryNode)
    }

    private static SQLDialect resolveDialect(String dialectName) {
        if (!dialectName) return SQLDialect.DEFAULT
        try {
            return SQLDialect.valueOf(dialectName.toUpperCase(Locale.ROOT))
        } catch (IllegalArgumentException ignore) {
            throw new IllegalArgumentException("Unknown dialect: ${dialectName}. " +
                    "(supported dialects: ${SQLDialect.values()*.name()})")
        }
    }

    private RenderedSql doRender(SqlQueryNode queryNode) {
        DSLContext ctx = DSL.using(dialect, new Settings().withRenderQuotedNames(RenderQuotedNames.NEVER))
        Select select = buildQueryNode(ctx, queryNode)
        return positionalize(ctx.renderNamedParams(select))
    }

    /**
     * Converts jOOQ's named parameter placeholders back to positional {@code ?}
     * placeholders, collecting the parameter expressions in placeholder order.
     */
    private RenderedSql positionalize(String namedSql) {
        List<Expression> ordered = []
        StringBuilder sb = new StringBuilder()
        Matcher m = namedSql =~ /:gqp(\d+)/
        int last = 0
        while (m.find()) {
            sb.append(namedSql, last, m.start()).append('?')
            ordered << paramExpressions[m.group(1) as int]
            last = m.end()
        }
        sb.append(namedSql, last, namedSql.length())
        return new RenderedSql(sb.toString(), ordered)
    }

    private Select buildQueryNode(DSLContext ctx, SqlQueryNode queryNode) {
        if (!(queryNode instanceof SqlSetQuery)) {
            return buildQuery(ctx, (SqlQuery) queryNode)
        }
        SqlSetQuery setQuery = (SqlSetQuery) queryNode
        Select left = buildQueryNode(ctx, setQuery.left)
        Select right = buildQuery(ctx, setQuery.right)
        Select combined
        switch (setQuery.op) {
            case SqlSetQuery.SetOp.UNION: combined = left.union(right); break
            case SqlSetQuery.SetOp.UNION_ALL: combined = left.unionAll(right); break
            case SqlSetQuery.SetOp.INTERSECT: combined = left.intersect(right); break
            case SqlSetQuery.SetOp.MINUS: combined = left.except(right); break
            default: throw new GroovyBugError("Unknown set operation: ${setQuery.op}")
        }
        if (setQuery.orderBy || setQuery.offset != null || setQuery.fetch != null) {
            // jOOQ's set operations return the combining query, which accepts the
            // trailing ORDER BY/LIMIT of the whole result via the model API
            if (!(combined instanceof SelectQuery)) {
                throw new GroovyBugError("jOOQ set operation result is not a SelectQuery: ${combined.getClass().name}")
            }
            SelectQuery combinedQuery = (SelectQuery) combined
            for (SqlOrderSpec spec : setQuery.orderBy) {
                combinedQuery.addOrderBy(buildSortField(ctx, spec))
            }
            if (setQuery.offset != null) combinedQuery.addOffset(limitParam((SqlParam) setQuery.offset))
            if (setQuery.fetch != null) combinedQuery.addLimit(limitParam((SqlParam) setQuery.fetch))
        }
        return combined
    }

    private SelectQuery buildQuery(DSLContext ctx, SqlQuery query) {
        SelectQuery selectQuery = ctx.selectQuery()
        for (SqlProjection projection : query.projections) {
            selectQuery.addSelect(buildProjection(ctx, projection))
        }
        if (query.distinct) selectQuery.setDistinct(true)
        selectQuery.addFrom(buildTableSource(ctx, query.from))
        for (SqlJoin join : query.joins) {
            Table table = buildTableSource(ctx, join.table)
            switch (join.type) {
                case SqlJoin.Type.INNER: selectQuery.addJoin(table, JoinType.JOIN, buildCondition(ctx, join.on)); break
                case SqlJoin.Type.LEFT: selectQuery.addJoin(table, JoinType.LEFT_OUTER_JOIN, buildCondition(ctx, join.on)); break
                case SqlJoin.Type.RIGHT: selectQuery.addJoin(table, JoinType.RIGHT_OUTER_JOIN, buildCondition(ctx, join.on)); break
                case SqlJoin.Type.FULL: selectQuery.addJoin(table, JoinType.FULL_OUTER_JOIN, buildCondition(ctx, join.on)); break
                case SqlJoin.Type.CROSS: selectQuery.addJoin(table, JoinType.CROSS_JOIN); break
                default: throw new GroovyBugError("Unknown join type: ${join.type}")
            }
        }
        if (query.where != null) selectQuery.addConditions(buildCondition(ctx, query.where))
        for (SqlExpr keyExpr : query.groupBy) {
            selectQuery.addGroupBy(buildField(ctx, keyExpr))
        }
        if (query.having != null) selectQuery.addHaving(buildCondition(ctx, query.having))
        for (SqlOrderSpec spec : query.orderBy) {
            selectQuery.addOrderBy(buildSortField(ctx, spec))
        }
        if (query.offset != null) selectQuery.addOffset(limitParam((SqlParam) query.offset))
        if (query.fetch != null) selectQuery.addLimit(limitParam((SqlParam) query.fetch))
        return selectQuery
    }

    private SelectFieldOrAsterisk buildProjection(DSLContext ctx, SqlProjection projection) {
        if (projection.expr instanceof SqlStar) {
            SqlStar star = (SqlStar) projection.expr
            return star.tableAlias ? DSL.table(DSL.name(star.tableAlias)).asterisk() : DSL.asterisk()
        }
        Field field = buildField(ctx, projection.expr)
        return projection.alias ? field.as(projection.alias) : field
    }

    private Table buildTableSource(DSLContext ctx, SqlTableSource tableSource) {
        if (tableSource instanceof SqlTableRef) {
            return DSL.table(DSL.name(tableSource.tableName)).as(tableSource.alias)
        }
        if (tableSource instanceof SqlDerivedTable) {
            return buildQuery(ctx, tableSource.query).asTable(tableSource.alias)
        }
        throw new GroovyBugError("Unknown table source: ${tableSource?.getClass()?.name}")
    }

    private SortField buildSortField(DSLContext ctx, SqlOrderSpec spec) {
        Field field = buildField(ctx, spec.expr)
        SortField sortField = spec.asc ? field.asc() : field.desc()
        return spec.nullsLast ? sortField.nullsLast() : sortField.nullsFirst()
    }

    private Condition buildCondition(DSLContext ctx, SqlExpr expr) {
        if (expr instanceof SqlBinary) {
            switch (expr.op) {
                case 'AND': return buildCondition(ctx, expr.left).and(buildCondition(ctx, expr.right))
                case 'OR': return buildCondition(ctx, expr.left).or(buildCondition(ctx, expr.right))
                case '=': return buildField(ctx, expr.left).eq(buildField(ctx, expr.right))
                case '<>': return buildField(ctx, expr.left).ne(buildField(ctx, expr.right))
                case '<': return buildField(ctx, expr.left).lt(buildField(ctx, expr.right))
                case '<=': return buildField(ctx, expr.left).le(buildField(ctx, expr.right))
                case '>': return buildField(ctx, expr.left).gt(buildField(ctx, expr.right))
                case '>=': return buildField(ctx, expr.left).ge(buildField(ctx, expr.right))
            }
        }
        if (expr instanceof SqlIsNull) {
            Field field = buildField(ctx, expr.expr)
            return expr.negated ? field.isNotNull() : field.isNull()
        }
        if (expr instanceof SqlIn) {
            Field field = buildField(ctx, expr.expr)
            List<Field> values = expr.values.collect { SqlExpr e -> buildField(ctx, e) }
            return expr.negated ? field.notIn(values) : field.in(values)
        }
        if (expr instanceof SqlInQuery) {
            Field field = buildField(ctx, expr.expr)
            Select subQuery = buildQuery(ctx, expr.query)
            return expr.negated ? field.notIn(subQuery) : field.in(subQuery)
        }
        if (expr instanceof SqlLike) {
            return buildField(ctx, expr.expr).like(buildField(ctx, expr.pattern)).escape('!' as char)
        }
        if (expr instanceof SqlExists) {
            return DSL.exists(buildQuery(ctx, expr.query))
        }
        if (expr instanceof SqlUnary && 'NOT' == expr.op) {
            return DSL.not(buildCondition(ctx, expr.operand))
        }
        return DSL.condition(buildField(ctx, expr))
    }

    private Field buildField(DSLContext ctx, SqlExpr expr) {
        if (expr instanceof SqlColumn) {
            return expr.tableAlias ? DSL.field(DSL.name(expr.tableAlias, expr.column)) : DSL.field(DSL.name(expr.column))
        }
        if (expr instanceof SqlParam) {
            return registerParam((SqlParam) expr)
        }
        if (expr instanceof SqlLiteral) {
            if (expr.value != null && expr.value.toString().contains(':gqp')) {
                throw new IllegalArgumentException("literals containing ':gqp' are not supported by the jooq-sql provider")
            }
            return DSL.inline(expr.value)
        }
        if (expr instanceof SqlOrdinal) {
            return DSL.inline(expr.position)
        }
        if (expr instanceof SqlCountStar) {
            return DSL.count()
        }
        if (expr instanceof SqlFunction) {
            return buildFunction(ctx, (SqlFunction) expr)
        }
        if (expr instanceof SqlCaseWhen) {
            return DSL.when(buildCondition(ctx, expr.condition), buildField(ctx, expr.thenExpr))
                    .otherwise(buildField(ctx, expr.elseExpr))
        }
        if (expr instanceof SqlScalarQuery) {
            return DSL.field(buildQuery(ctx, expr.query))
        }
        if (expr instanceof SqlBinary) {
            switch (expr.op) {
                case '+': return buildField(ctx, expr.left).add(buildField(ctx, expr.right))
                case '-': return buildField(ctx, expr.left).sub(buildField(ctx, expr.right))
                case '*': return buildField(ctx, expr.left).mul(buildField(ctx, expr.right))
                case '/': return buildField(ctx, expr.left).div(buildField(ctx, expr.right))
                case '||': return DSL.concat(buildField(ctx, expr.left), buildField(ctx, expr.right))
                default: return DSL.field(buildCondition(ctx, expr))
            }
        }
        if (expr instanceof SqlUnary) {
            if ('-' == expr.op) return buildField(ctx, expr.operand).neg()
            return DSL.field(buildCondition(ctx, expr))
        }
        // condition-shaped expressions used as values
        if (expr instanceof SqlIsNull || expr instanceof SqlIn || expr instanceof SqlInQuery
                || expr instanceof SqlLike || expr instanceof SqlExists) {
            return DSL.field(buildCondition(ctx, expr))
        }
        throw new GroovyBugError("Unknown SQL expression: ${expr?.getClass()?.name}")
    }

    private Field buildFunction(DSLContext ctx, SqlFunction function) {
        List<Field> fields = function.args.collect { SqlExpr e -> buildField(ctx, e) }
        switch (function.name) {
            case 'UPPER': return DSL.upper(fields[0])
            case 'LOWER': return DSL.lower(fields[0])
            case 'TRIM': return DSL.trim(fields[0])
            case 'CHAR_LENGTH': return DSL.charLength(fields[0])
            case 'ABS': return DSL.abs(fields[0])
            case 'MOD': return fields[0].mod(fields[1])
            case 'REPLACE': return DSL.replace(fields[0], fields[1], fields[2])
            case 'SUBSTRING': return fields.size() > 2
                    ? DSL.substring(fields[0], fields[1], fields[2])
                    : DSL.substring(fields[0], fields[1])
            case 'COUNT': return DSL.count(fields[0])
            case 'SUM': return DSL.sum(fields[0])
            case 'AVG': return DSL.avg(fields[0])
            case 'MIN': return DSL.min(fields[0])
            case 'MAX': return DSL.max(fields[0])
            default: return DSL.function(function.name, Object, fields as Field[])
        }
    }

    private Param registerParam(SqlParam param) {
        int index = paramExpressions.size()
        paramExpressions << param.valueExpr
        return DSL.param('gqp' + index, Object)
    }

    // limit/offset parameters carry a placeholder value: jOOQ eagerly folds a
    // null/zero-valued limit into a `where false` optimization
    private Param limitParam(SqlParam param) {
        int index = paramExpressions.size()
        paramExpressions << param.valueExpr
        return DSL.param('gqp' + index, 1)
    }
}
