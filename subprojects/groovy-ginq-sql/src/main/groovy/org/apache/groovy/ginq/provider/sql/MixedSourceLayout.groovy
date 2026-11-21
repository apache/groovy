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
import groovy.transform.PackageScope

import java.util.LinkedHashMap
import java.util.LinkedHashSet
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
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
import org.apache.groovy.ginq.provider.sql.ir.SqlProjection
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.ir.SqlScalarQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlSetQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlStar
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.apache.groovy.ginq.provider.sql.ir.SqlTableSource
import org.apache.groovy.ginq.provider.sql.ir.SqlUnary
import org.codehaus.groovy.GroovyBugError

/**
 * Puts a presence column on each mixed-mode source and, when the dialect has
 * no {@code FULL OUTER JOIN}, nests each full join as its own union. The
 * union is a derived table, and the next join reads that table. A later
 * outer join sees the whole left subjoin as absent only when every presence
 * column inside it is null.
 */
@PackageScope
@CompileStatic
class MixedSourceLayout {
    static final String PRESENCE = '__ginq_present'
    /**
     * Planner reference to the synthetic presence marker. It is not a user
     * column, so renaming the marker does not rewrite a real {@code __ginq_present}.
     */
    static final String MARKER = '__ginq_marker'
    private static final String SRC = '__ginq_src'

    private final List<String> aliasNames
    private final boolean userProjections
    private final LayoutContext context
    private final Map<String, LinkedHashSet<String>> usedColumns = new LinkedHashMap<>()
    private final Map<String, Integer> starProbesByAlias = new LinkedHashMap<>()
    private List<SqlTableSource> originals = []

    private MixedSourceLayout(List<String> aliasNames, boolean userProjections, LayoutContext context) {
        this.aliasNames = aliasNames
        this.userProjections = userProjections
        this.context = context
    }

    /**
     * Wraps every source with a presence column. When {@code emulateFull} is
     * set, each full join becomes
     * {@code (left LEFT JOIN right) UNION ALL (left RIGHT JOIN right WHERE left is absent)}
     * and that union is the left input of the following join.
     */
    static SqlQueryNode layout(SqlQuery query, List<String> aliasNames, boolean emulateFull) {
        return new MixedSourceLayout(aliasNames, false, new LayoutContext()).apply(query, emulateFull)
    }

    /**
     * Rewrites a fully translated query so a dialect without {@code FULL OUTER JOIN}
     * still executes it. Nested queries are rewritten too, including a full join
     * that lives only inside {@code EXISTS}, {@code IN} or a scalar subquery.
     * The outermost select list stays the user's projections; presence columns
     * exist only inside the emulated unions.
     */
    static SqlQueryNode layoutComplete(SqlQueryNode node) {
        LayoutContext context = new LayoutContext()
        SqlQueryNode laid = layOut(node, context)
        attachProbes(laid, context.probes)
        return laid
    }

    private static SqlQueryNode layOut(SqlQueryNode node, LayoutContext context) {
        if (node instanceof SqlSetQuery) {
            SqlSetQuery set = (SqlSetQuery) node
            SqlQueryNode left = layOut(set.left, context)
            SqlQuery right = (SqlQuery) layOut(set.right, context)
            if (left === set.left && right === set.right) {
                return set
            }
            SqlSetQuery copy = new SqlSetQuery(left, set.op, right)
            copy.orderBy.addAll(set.orderBy)
            copy.offset = set.offset
            copy.fetch = set.fetch
            return copy
        }
        SqlQuery query = (SqlQuery) node
        relayQuery(query, context)
        if (!query.joins.any { SqlJoin join -> join.type == SqlJoin.Type.FULL }) {
            return query
        }
        List<String> names = [query.from.alias]
        for (SqlJoin join : query.joins) {
            names.add(join.table.alias)
        }
        return new MixedSourceLayout(names, true, context).completeNest(query)
    }

    private SqlQueryNode apply(SqlQuery query, boolean emulateFull) {
        query.from = wrap(query.from)
        List<SqlJoin> wrapped = []
        for (SqlJoin join : query.joins) {
            wrapped.add(new SqlJoin(join.type, wrap(join.table), join.on))
        }
        query.joins.clear()
        query.joins.addAll(wrapped)
        if (!emulateFull) {
            return query
        }
        return nest(query)
    }

    private SqlQueryNode completeNest(SqlQuery query) {
        originals = [query.from]
        for (SqlJoin join : query.joins) {
            originals.add(join.table)
        }
        registerStars(query)
        collectUsed(query)
        return apply(query, true)
    }

    private static void relayQuery(SqlQuery query, LayoutContext context) {
        if (query.from instanceof SqlDerivedTable) {
            SqlDerivedTable derived = (SqlDerivedTable) query.from
            SqlQueryNode inner = layOut(derived.query, context)
            if (inner !== derived.query) {
                query.from = new SqlDerivedTable(inner, derived.alias)
            }
        }
        List<SqlJoin> joins = []
        boolean joinsChanged = false
        for (SqlJoin join : query.joins) {
            SqlExpr on = relayExpr(join.on, context)
            SqlTableSource table = join.table
            if (table instanceof SqlDerivedTable) {
                SqlDerivedTable derived = (SqlDerivedTable) table
                SqlQueryNode inner = layOut(derived.query, context)
                if (inner !== derived.query) {
                    table = new SqlDerivedTable(inner, derived.alias)
                }
            }
            if (on !== join.on || table !== join.table) {
                joins.add(new SqlJoin(join.type, table, on))
                joinsChanged = true
            } else {
                joins.add(join)
            }
        }
        if (joinsChanged) {
            query.joins.clear()
            query.joins.addAll(joins)
        }
        for (int i = 0; i < query.projections.size(); i++) {
            SqlProjection projection = query.projections[i]
            SqlExpr expr = relayExpr(projection.expr, context)
            if (expr !== projection.expr) {
                query.projections.set(i, new SqlProjection(expr, projection.alias))
            }
        }
        query.where = relayExpr(query.where, context)
        for (int i = 0; i < query.groupBy.size(); i++) {
            query.groupBy.set(i, relayExpr(query.groupBy[i], context))
        }
        query.having = relayExpr(query.having, context)
        for (int i = 0; i < query.orderBy.size(); i++) {
            SqlOrderSpec spec = query.orderBy[i]
            SqlExpr expr = relayExpr(spec.expr, context)
            if (expr !== spec.expr) {
                query.orderBy.set(i, new SqlOrderSpec(expr, spec.asc, spec.nullsLast))
            }
        }
    }

    private static SqlExpr relayExpr(SqlExpr expr, LayoutContext context) {
        if (expr == null) {
            return null
        }
        if (expr instanceof SqlBinary) {
            SqlExpr left = relayExpr(expr.left, context)
            SqlExpr right = relayExpr(expr.right, context)
            return left === expr.left && right === expr.right ? expr : new SqlBinary(expr.op, left, right)
        }
        if (expr instanceof SqlUnary) {
            SqlExpr operand = relayExpr(expr.operand, context)
            return operand === expr.operand ? expr : new SqlUnary(expr.op, operand)
        }
        if (expr instanceof SqlIsNull) {
            SqlExpr inner = relayExpr(expr.expr, context)
            return inner === expr.expr ? expr : new SqlIsNull(inner, expr.negated)
        }
        if (expr instanceof SqlIn) {
            SqlExpr inner = relayExpr(expr.expr, context)
            List<SqlExpr> values = relayList(expr.values, context)
            return inner === expr.expr && values === expr.values ? expr : new SqlIn(inner, values, expr.negated)
        }
        if (expr instanceof SqlLike) {
            SqlExpr value = relayExpr(expr.expr, context)
            SqlExpr pattern = relayExpr(expr.pattern, context)
            return value === expr.expr && pattern === expr.pattern ? expr : new SqlLike(value, pattern)
        }
        if (expr instanceof SqlFunction) {
            List<SqlExpr> args = relayList(expr.args, context)
            return args === expr.args ? expr : new SqlFunction(expr.name, args, expr.distinct)
        }
        if (expr instanceof SqlCast) {
            SqlExpr inner = relayExpr(expr.expr, context)
            return inner === expr.expr ? expr : new SqlCast(inner, expr.sqlType)
        }
        if (expr instanceof SqlCaseWhen) {
            SqlExpr condition = relayExpr(expr.condition, context)
            SqlExpr thenExpr = relayExpr(expr.thenExpr, context)
            SqlExpr elseExpr = relayExpr(expr.elseExpr, context)
            return condition === expr.condition && thenExpr === expr.thenExpr && elseExpr === expr.elseExpr
                    ? expr : new SqlCaseWhen(condition, thenExpr, elseExpr)
        }
        if (expr instanceof SqlScalarQuery) {
            SqlQuery laid = (SqlQuery) layOut(expr.query, context)
            return laid === expr.query ? expr : new SqlScalarQuery(laid)
        }
        if (expr instanceof SqlExists) {
            SqlQuery laid = (SqlQuery) layOut(expr.query, context)
            return laid === expr.query ? expr : new SqlExists(laid)
        }
        if (expr instanceof SqlInQuery) {
            SqlExpr inner = relayExpr(expr.expr, context)
            SqlQuery laid = (SqlQuery) layOut(expr.query, context)
            return inner === expr.expr && laid === expr.query ? expr : new SqlInQuery(inner, laid, expr.negated)
        }
        return expr
    }

    private static List<SqlExpr> relayList(List<SqlExpr> exprs, LayoutContext context) {
        List<SqlExpr> copy = null
        for (int i = 0; i < exprs.size(); i++) {
            SqlExpr relayed = relayExpr(exprs[i], context)
            if (copy == null && relayed !== exprs[i]) {
                copy = new ArrayList<>(exprs.subList(0, i))
            }
            if (copy != null) {
                copy.add(relayed)
            }
        }
        return copy == null ? exprs : copy
    }

    private static void attachProbes(SqlQueryNode node, List<SqlQuery> probes) {
        if (probes.isEmpty()) {
            return
        }
        List<SqlQuery> copy = new ArrayList<>(probes)
        if (node instanceof SqlQuery) {
            ((SqlQuery) node).starProbes = copy
        } else if (node instanceof SqlSetQuery) {
            ((SqlSetQuery) node).starProbes = copy
        }
    }

    private void registerStars(SqlQuery query) {
        for (SqlProjection projection : query.projections) {
            if (!(projection.expr instanceof SqlStar)) {
                continue
            }
            String alias = ((SqlStar) projection.expr).tableAlias
            if (alias == null || !aliasNames.contains(alias) || starProbesByAlias.containsKey(alias)) {
                continue
            }
            int index = aliasNames.indexOf(alias)
            int id = context.probes.size()
            context.probes.add(countQuery(originals[index]))
            starProbesByAlias.put(alias, id)
        }
    }

    private static SqlQuery countQuery(SqlTableSource source) {
        SqlQuery query = new SqlQuery()
        query.projections.add(new SqlProjection(new SqlStar()))
        if (source instanceof SqlTableRef) {
            query.from = new SqlTableRef(source.tableName, source.alias)
        } else if (source instanceof SqlDerivedTable) {
            query.from = new SqlDerivedTable(source.query, source.alias)
        } else {
            throw new GroovyBugError('unknown mixed-mode source ' + source.getClass().name)
        }
        query.where = new SqlBinary('=', new SqlLiteral(1), new SqlLiteral(0))
        return query
    }

    private void collectUsed(SqlQuery query) {
        collectQuery(query)
    }

    private void collectQuery(SqlQuery query) {
        if (query == null) {
            return
        }
        for (SqlProjection projection : query.projections) {
            collectExpr(projection.expr)
        }
        collectExpr(query.where)
        for (SqlExpr key : query.groupBy) {
            collectExpr(key)
        }
        collectExpr(query.having)
        for (SqlOrderSpec spec : query.orderBy) {
            collectExpr(spec.expr)
        }
        for (SqlJoin join : query.joins) {
            collectExpr(join.on)
        }
    }

    private void collectExpr(SqlExpr expr) {
        if (expr == null) {
            return
        }
        if (expr instanceof SqlColumn) {
            SqlColumn column = (SqlColumn) expr
            if (column.tableAlias != null && aliasNames.contains(column.tableAlias)) {
                LinkedHashSet<String> columns = usedColumns.get(column.tableAlias)
                if (columns == null) {
                    columns = new LinkedHashSet<>()
                    usedColumns.put(column.tableAlias, columns)
                }
                columns.add(column.column)
            }
            return
        }
        if (expr instanceof SqlBinary) {
            collectExpr(expr.left)
            collectExpr(expr.right)
        } else if (expr instanceof SqlUnary) {
            collectExpr(expr.operand)
        } else if (expr instanceof SqlIsNull) {
            collectExpr(expr.expr)
        } else if (expr instanceof SqlIn) {
            collectExpr(expr.expr)
            for (SqlExpr value : expr.values) {
                collectExpr(value)
            }
        } else if (expr instanceof SqlLike) {
            collectExpr(expr.expr)
            collectExpr(expr.pattern)
        } else if (expr instanceof SqlFunction) {
            for (SqlExpr arg : expr.args) {
                collectExpr(arg)
            }
        } else if (expr instanceof SqlCast) {
            collectExpr(expr.expr)
        } else if (expr instanceof SqlCaseWhen) {
            collectExpr(expr.condition)
            collectExpr(expr.thenExpr)
            collectExpr(expr.elseExpr)
        } else if (expr instanceof SqlScalarQuery) {
            collectQuery(((SqlScalarQuery) expr).query)
        } else if (expr instanceof SqlExists) {
            collectQuery(((SqlExists) expr).query)
        } else if (expr instanceof SqlInQuery) {
            SqlInQuery inQuery = (SqlInQuery) expr
            collectExpr(inQuery.expr)
            collectQuery(inQuery.query)
        }
    }

    private SqlQueryNode nest(SqlQuery query) {
        Rel rel = new Rel()
        rel.from = query.from
        rel.direct.add(aliasNames[0])
        rel.output.add(0)
        for (int j = 0; j < query.joins.size(); j++) {
            SqlJoin join = query.joins[j]
            int index = j + 1
            if (join.type == SqlJoin.Type.FULL) {
                rel = nestFull(rel, join, index)
            } else {
                appendJoin(rel, join, index)
            }
        }
        return userProjections ? finishComplete(rel, query) : finish(rel, query)
    }

    private void appendJoin(Rel rel, SqlJoin join, int index) {
        rel.joins.add(new SqlJoin(join.type, join.table, rewriteExpr(join.on, rel, aliasNames[index])))
        rel.direct.add(aliasNames[index])
        rel.output.add(index)
    }

    private Rel nestFull(Rel rel, SqlJoin join, int index) {
        List<SqlProjection> projections = project(rel)
        if (userProjections) {
            projections.addAll(directColumns(index, aliasNames[index]))
        } else {
            projections.add(new SqlProjection(new SqlStar(aliasNames[index])))
        }
        SqlExpr on = rewriteExpr(join.on, rel, aliasNames[index])
        SqlQuery left = materialize(rel, SqlJoin.Type.LEFT, join.table, on, projections, null)
        SqlQuery right = materialize(rel, SqlJoin.Type.RIGHT, join.table, on, projections, absence(rel))
        SqlSetQuery union = new SqlSetQuery(left, SqlSetQuery.SetOp.UNION_ALL, right)
        int first = rel.output[0]
        String container = '__ginqf' + first + '_' + (index + 1)
        Rel next = new Rel()
        next.from = new SqlDerivedTable(union, container)
        next.container = container
        next.output.addAll(rel.output)
        next.output.add(index)
        return next
    }

    private SqlQuery materialize(Rel rel, SqlJoin.Type type, SqlTableSource right, SqlExpr on,
                                 List<SqlProjection> projections, SqlExpr where) {
        SqlQuery query = new SqlQuery()
        query.from = rel.from
        query.joins.addAll(rel.joins)
        query.joins.add(new SqlJoin(type, right, on))
        query.projections.addAll(projections)
        query.where = where
        return query
    }

    private List<SqlProjection> project(Rel rel) {
        if (userProjections) {
            List<SqlProjection> projections = []
            for (int index : rel.output) {
                projections.addAll(passedColumns(rel, index))
            }
            return projections
        }
        List<SqlProjection> projections = []
        if (rel.container != null) {
            projections.add(new SqlProjection(new SqlStar(rel.container)))
        }
        for (String alias : rel.direct) {
            projections.add(new SqlProjection(new SqlStar(alias)))
        }
        return projections
    }

    private List<SqlProjection> directColumns(int index, String alias) {
        List<SqlProjection> projections = []
        projections.add(new SqlProjection(new SqlColumn(alias, MARKER), presenceOut(index)))
        Integer probe = starProbesByAlias.get(alias)
        if (probe != null) {
            projections.add(new SqlProjection(new SqlColumn(alias, '__ginqsrc_' + probe)))
            return projections
        }
        LinkedHashSet<String> columns = usedColumns.get(alias)
        if (columns != null) {
            for (String column : columns) {
                projections.add(new SqlProjection(new SqlColumn(alias, column), columnOut(index, column)))
            }
        }
        return projections
    }

    private List<SqlProjection> passedColumns(Rel rel, int index) {
        String alias = aliasNames[index]
        if (rel.direct.contains(alias)) {
            return directColumns(index, alias)
        }
        List<SqlProjection> projections = []
        projections.add(new SqlProjection(new SqlColumn(rel.container, presenceOut(index)), presenceOut(index)))
        Integer probe = starProbesByAlias.get(alias)
        if (probe != null) {
            projections.add(new SqlProjection(new SqlColumn(rel.container, '__ginqpass_' + probe)))
            return projections
        }
        LinkedHashSet<String> columns = usedColumns.get(alias)
        if (columns != null) {
            for (String column : columns) {
                projections.add(new SqlProjection(new SqlColumn(rel.container, columnOut(index, column)), columnOut(index, column)))
            }
        }
        return projections
    }

    private static String presenceOut(int index) {
        return '__ginq_p' + index
    }

    private static String columnOut(int index, String column) {
        return '__ginq_' + index + '_' + column
    }

    private SqlExpr absence(Rel rel) {
        SqlExpr acc = null
        for (int index : rel.output) {
            SqlExpr column
            if (rel.direct.contains(aliasNames[index])) {
                column = new SqlColumn(aliasNames[index], MARKER)
            } else {
                column = new SqlColumn(rel.container, userProjections ? presenceOut(index) : '__ginqabs_' + index)
            }
            SqlExpr pred = new SqlIsNull(column, false)
            acc = acc == null ? pred : new SqlBinary('AND', acc, pred)
        }
        return acc
    }

    private SqlQueryNode finish(Rel rel, SqlQuery original) {
        SqlExpr where = rewriteExpr(original.where, rel)
        List<SqlOrderSpec> orders = []
        for (SqlOrderSpec spec : original.orderBy) {
            orders.add(rewriteOrder(spec, rel))
        }
        if (rel.joins.isEmpty() && rel.container != null) {
            SqlQuery wrapper = new SqlQuery()
            wrapper.from = rel.from
            wrapper.projections.add(new SqlProjection(new SqlStar(rel.container)))
            wrapper.where = where
            wrapper.orderBy.addAll(orders)
            wrapper.offset = original.offset
            wrapper.fetch = original.fetch
            return wrapper
        }
        SqlQuery query = new SqlQuery()
        query.from = rel.from
        query.joins.addAll(rel.joins)
        query.projections.addAll(project(rel))
        query.where = where
        query.orderBy.addAll(orders)
        query.offset = original.offset
        query.fetch = original.fetch
        return query
    }

    private SqlQuery finishComplete(Rel rel, SqlQuery original) {
        SqlQuery query = new SqlQuery()
        query.distinct = original.distinct
        query.from = rel.from
        query.joins.addAll(rel.joins)
        for (SqlProjection projection : original.projections) {
            String alias = projection.alias
            if ((alias == null || alias.isEmpty()) && projection.expr instanceof SqlColumn) {
                alias = ((SqlColumn) projection.expr).column
            }
            query.projections.add(new SqlProjection(rewriteExpr(projection.expr, rel), alias))
        }
        query.where = rewriteExpr(original.where, rel)
        for (SqlExpr key : original.groupBy) {
            query.groupBy.add(rewriteExpr(key, rel))
        }
        query.having = rewriteExpr(original.having, rel)
        for (SqlOrderSpec spec : original.orderBy) {
            query.orderBy.add(new SqlOrderSpec(rewriteExpr(spec.expr, rel), spec.asc, spec.nullsLast))
        }
        query.offset = original.offset
        query.fetch = original.fetch
        return query
    }

    private SqlOrderSpec rewriteOrder(SqlOrderSpec spec, Rel rel) {
        if (!(spec.expr instanceof SqlColumn)) {
            throw new GroovyBugError('emulated full join order key is not a column')
        }
        SqlColumn column = (SqlColumn) spec.expr
        if (rel.direct.contains(column.tableAlias)) {
            return spec
        }
        int index = aliasNames.indexOf(column.tableAlias)
        String qualifier = rel.container != null ? rel.container : column.tableAlias
        return new SqlOrderSpec(new SqlColumn(qualifier, '__ginqord_' + index + '_' + column.column), spec.asc, spec.nullsLast)
    }

    private SqlExpr rewriteExpr(SqlExpr expr, Rel rel, String alsoDirect = null) {
        if (expr == null || rel.container == null) {
            return expr
        }
        if (expr instanceof SqlColumn) {
            SqlColumn column = (SqlColumn) expr
            if (column.column != null && (column.column.startsWith('__ginqsrc_')
                    || column.column.startsWith('__ginqpass_')
                    || column.column.startsWith('__ginqstar_')
                    || column.column.startsWith('__ginqcol_')
                    || (column.tableAlias != null && column.tableAlias.startsWith('__ginq')))) {
                return column
            }
            if (column.tableAlias == null || column.tableAlias == alsoDirect
                    || rel.direct.contains(column.tableAlias) || column.tableAlias == rel.container) {
                return column
            }
            int index = aliasNames.indexOf(column.tableAlias)
            if (index < 0) {
                return column
            }
            Integer probe = starProbesByAlias.get(column.tableAlias)
            if (userProjections && probe != null) {
                return new SqlColumn(rel.container, '__ginqcol_' + probe + '_' + column.column)
            }
            String exposed = userProjections ? columnOut(index, column.column) : '__ginqref_' + index + '_' + column.column
            return new SqlColumn(rel.container, exposed)
        }
        if (expr instanceof SqlStar) {
            String alias = ((SqlStar) expr).tableAlias
            Integer probe = alias == null ? null : starProbesByAlias.get(alias)
            if (!userProjections || probe == null || rel.direct.contains(alias)) {
                return expr
            }
            return new SqlColumn(rel.container, '__ginqstar_' + probe + '_' + alias)
        }
        if (expr instanceof SqlBinary) {
            return new SqlBinary(expr.op, rewriteExpr(expr.left, rel, alsoDirect), rewriteExpr(expr.right, rel, alsoDirect))
        }
        if (expr instanceof SqlUnary) {
            return new SqlUnary(expr.op, rewriteExpr(expr.operand, rel, alsoDirect))
        }
        if (expr instanceof SqlIsNull) {
            return new SqlIsNull(rewriteExpr(expr.expr, rel, alsoDirect), expr.negated)
        }
        if (expr instanceof SqlIn) {
            List<SqlExpr> values = []
            for (SqlExpr value : expr.values) {
                values.add(rewriteExpr(value, rel, alsoDirect))
            }
            return new SqlIn(rewriteExpr(expr.expr, rel, alsoDirect), values, expr.negated)
        }
        if (expr instanceof SqlLike) {
            return new SqlLike(rewriteExpr(expr.expr, rel, alsoDirect), rewriteExpr(expr.pattern, rel, alsoDirect))
        }
        if (expr instanceof SqlFunction) {
            List<SqlExpr> args = []
            for (SqlExpr arg : expr.args) {
                args.add(rewriteExpr(arg, rel, alsoDirect))
            }
            return new SqlFunction(expr.name, args, expr.distinct)
        }
        if (expr instanceof SqlCast) {
            return new SqlCast(rewriteExpr(expr.expr, rel, alsoDirect), expr.sqlType)
        }
        if (expr instanceof SqlCaseWhen) {
            return new SqlCaseWhen(rewriteExpr(expr.condition, rel, alsoDirect), rewriteExpr(expr.thenExpr, rel, alsoDirect), rewriteExpr(expr.elseExpr, rel, alsoDirect))
        }
        if (expr instanceof SqlScalarQuery) {
            return new SqlScalarQuery(rewriteQuery(expr.query, rel, alsoDirect))
        }
        if (expr instanceof SqlExists) {
            return new SqlExists(rewriteQuery(expr.query, rel, alsoDirect))
        }
        if (expr instanceof SqlInQuery) {
            return new SqlInQuery(rewriteExpr(expr.expr, rel, alsoDirect), rewriteQuery(expr.query, rel, alsoDirect), expr.negated)
        }
        return expr
    }

    private SqlQuery rewriteQuery(SqlQuery query, Rel rel, String alsoDirect = null) {
        SqlQuery copy = new SqlQuery()
        copy.distinct = query.distinct
        for (SqlProjection projection : query.projections) {
            copy.projections.add(new SqlProjection(rewriteExpr(projection.expr, rel, alsoDirect), projection.alias))
        }
        copy.from = query.from
        for (SqlJoin join : query.joins) {
            copy.joins.add(new SqlJoin(join.type, join.table, rewriteExpr(join.on, rel, alsoDirect)))
        }
        copy.where = rewriteExpr(query.where, rel, alsoDirect)
        for (SqlExpr key : query.groupBy) {
            copy.groupBy.add(rewriteExpr(key, rel, alsoDirect))
        }
        copy.having = rewriteExpr(query.having, rel, alsoDirect)
        for (SqlOrderSpec spec : query.orderBy) {
            copy.orderBy.add(new SqlOrderSpec(rewriteExpr(spec.expr, rel, alsoDirect), spec.asc, spec.nullsLast))
        }
        copy.offset = query.offset
        copy.fetch = query.fetch
        return copy
    }

    private SqlTableSource wrap(SqlTableSource source) {
        SqlQuery inner = new SqlQuery()
        inner.from = realias(source, SRC)
        inner.projections.add(new SqlProjection(new SqlStar(SRC)))
        // Complete-path SQL is executed as rendered, so the wrap must not
        // introduce 1 AS "__ginq_present" beside a user column of that name.
        // The split path still emits PRESENCE and lets prepareMixedSql rename
        // only the AS definition (and MARKER) when the user label collides.
        inner.projections.add(new SqlProjection(new SqlLiteral(1), userProjections ? MARKER : PRESENCE))
        return new SqlDerivedTable(inner, source.alias)
    }

    private static SqlTableSource realias(SqlTableSource source, String alias) {
        if (source instanceof SqlTableRef) {
            return new SqlTableRef(source.tableName, alias)
        }
        if (source instanceof SqlDerivedTable) {
            return new SqlDerivedTable(source.query, alias)
        }
        throw new GroovyBugError('unknown mixed-mode source ' + source.getClass().name)
    }

    private static final class Rel {
        SqlTableSource from
        final List<SqlJoin> joins = []
        final List<String> direct = []
        final List<Integer> output = []
        String container
    }

    private static final class LayoutContext {
        final List<SqlQuery> probes = []
    }
}
