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
package org.apache.groovy.ginq.provider.sql.render

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
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.expr.Expression

/**
 * Renders the SQL IR to SQL text with positional {@code ?} placeholders,
 * collecting the parameter value expressions in emission order so they are
 * guaranteed to match the placeholders.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class SqlRenderer {
    private final Dialect dialect

    SqlRenderer(Dialect dialect) {
        this.dialect = dialect
    }

    RenderedSql render(SqlQueryNode queryNode) {
        StringBuilder sb = new StringBuilder()
        List<Expression> parameters = []
        renderQueryNode(queryNode, sb, parameters)
        return new RenderedSql(sb.toString(), parameters)
    }

    private void renderQueryNode(SqlQueryNode queryNode, StringBuilder sb, List<Expression> parameters) {
        if (queryNode instanceof SqlSetQuery) {
            // parenthesize a set-query left operand to preserve GINQ's left-to-right
            // chaining, since INTERSECT binds tighter than UNION/EXCEPT in SQL
            boolean parenthesizeLeft = queryNode.left instanceof SqlSetQuery
            if (parenthesizeLeft) sb.append('(')
            renderQueryNode(queryNode.left, sb, parameters)
            if (parenthesizeLeft) sb.append(')')
            sb.append(' ').append(setOperator(queryNode.op)).append(' ')
            renderQuery(queryNode.right, sb, parameters)
            renderOrderAndLimit(queryNode.orderBy, queryNode.offset, queryNode.fetch, sb, parameters)
        } else {
            renderQuery((SqlQuery) queryNode, sb, parameters)
        }
    }

    private String setOperator(SqlSetQuery.SetOp op) {
        switch (op) {
            case SqlSetQuery.SetOp.UNION: return 'UNION'
            case SqlSetQuery.SetOp.UNION_ALL: return 'UNION ALL'
            case SqlSetQuery.SetOp.INTERSECT: return 'INTERSECT'
            case SqlSetQuery.SetOp.MINUS: return dialect.minusOperator()
            default: throw new GroovyBugError("Unknown set operation: ${op}")
        }
    }

    private void renderQuery(SqlQuery query, StringBuilder sb, List<Expression> parameters) {
        sb.append('SELECT ')
        if (query.distinct) sb.append('DISTINCT ')
        query.projections.eachWithIndex { SqlProjection projection, int i ->
            if (i > 0) sb.append(', ')
            renderExpr(projection.expr, sb, parameters)
            if (projection.alias) sb.append(' AS ').append(dialect.identifier(projection.alias))
        }

        sb.append(' FROM ')
        renderTableSource(query.from, sb, parameters)

        for (SqlJoin join : query.joins) {
            sb.append(' ').append(join.type.sqlText).append(' ')
            renderTableSource(join.table, sb, parameters)
            if (join.on != null) {
                sb.append(' ON ')
                renderExpr(join.on, sb, parameters)
            }
        }

        if (query.where != null) {
            sb.append(' WHERE ')
            renderExpr(query.where, sb, parameters)
        }

        if (query.groupBy) {
            sb.append(' GROUP BY ')
            query.groupBy.eachWithIndex { SqlExpr keyExpr, int i ->
                if (i > 0) sb.append(', ')
                renderExpr(keyExpr, sb, parameters)
            }
        }

        if (query.having != null) {
            sb.append(' HAVING ')
            renderExpr(query.having, sb, parameters)
        }

        renderOrderAndLimit(query.orderBy, query.offset, query.fetch, sb, parameters)
    }

    private void renderOrderAndLimit(List<SqlOrderSpec> orderBy, SqlExpr offset, SqlExpr fetch, StringBuilder sb, List<Expression> parameters) {
        if (orderBy) {
            sb.append(' ORDER BY ')
            orderBy.eachWithIndex { SqlOrderSpec orderSpec, int i ->
                if (i > 0) sb.append(', ')
                renderExpr(orderSpec.expr, sb, parameters)
                sb.append(orderSpec.asc ? ' ASC' : ' DESC')
                sb.append(orderSpec.nullsLast ? ' NULLS LAST' : ' NULLS FIRST')
            }
        }

        if (offset != null) {
            sb.append(' OFFSET ')
            renderExpr(offset, sb, parameters)
            sb.append(' ROWS')
        }
        if (fetch != null) {
            sb.append(offset != null ? ' FETCH NEXT ' : ' FETCH FIRST ')
            renderExpr(fetch, sb, parameters)
            sb.append(' ROWS ONLY')
        }
    }

    private void renderExpr(SqlExpr expr, StringBuilder sb, List<Expression> parameters) {
        if (expr instanceof SqlColumn) {
            if (expr.tableAlias) sb.append(dialect.identifier(expr.tableAlias)).append('.')
            sb.append(dialect.identifier(expr.column))
        } else if (expr instanceof SqlParam) {
            sb.append('?')
            parameters.add(expr.valueExpr)
        } else if (expr instanceof SqlLiteral) {
            renderLiteral(expr.value, sb)
        } else if (expr instanceof SqlBinary) {
            int prec = precedence(expr.op)
            renderOperand(expr.left, prec, sb, parameters)
            sb.append(' ').append(expr.op).append(' ')
            // at equal precedence, the right operand of a non-associative operator needs parentheses
            int rightPrec = expr.op in NON_ASSOCIATIVE_OPS ? prec + 1 : prec
            renderOperand(expr.right, rightPrec, sb, parameters)
        } else if (expr instanceof SqlUnary) {
            sb.append(expr.op == 'NOT' ? 'NOT ' : expr.op)
            renderOperand(expr.operand, PREC_UNARY, sb, parameters)
        } else if (expr instanceof SqlIsNull) {
            renderOperand(expr.expr, PREC_UNARY, sb, parameters)
            sb.append(expr.negated ? ' IS NOT NULL' : ' IS NULL')
        } else if (expr instanceof SqlIn) {
            renderOperand(expr.expr, PREC_UNARY, sb, parameters)
            sb.append(expr.negated ? ' NOT IN (' : ' IN (')
            expr.values.eachWithIndex { SqlExpr valueExpr, int i ->
                if (i > 0) sb.append(', ')
                renderExpr(valueExpr, sb, parameters)
            }
            sb.append(')')
        } else if (expr instanceof SqlExists) {
            sb.append('EXISTS (')
            renderQuery(expr.query, sb, parameters)
            sb.append(')')
        } else if (expr instanceof SqlInQuery) {
            renderOperand(expr.expr, PREC_UNARY, sb, parameters)
            sb.append(expr.negated ? ' NOT IN (' : ' IN (')
            renderQuery(expr.query, sb, parameters)
            sb.append(')')
        } else if (expr instanceof SqlScalarQuery) {
            sb.append('(')
            renderQuery(expr.query, sb, parameters)
            sb.append(')')
        } else if (expr instanceof SqlLike) {
            renderOperand(expr.expr, PREC_UNARY, sb, parameters)
            sb.append(' LIKE ')
            renderExpr(expr.pattern, sb, parameters)
            // pattern values are escaped with '!' at runtime, see SqlGinqRuntime#likePattern
            sb.append(" ESCAPE '!'")
        } else if (expr instanceof SqlCaseWhen) {
            sb.append('CASE WHEN ')
            renderExpr(expr.condition, sb, parameters)
            sb.append(' THEN ')
            renderExpr(expr.thenExpr, sb, parameters)
            sb.append(' ELSE ')
            renderExpr(expr.elseExpr, sb, parameters)
            sb.append(' END')
        } else if (expr instanceof SqlOrdinal) {
            sb.append(expr.position)
        } else if (expr instanceof SqlFunction) {
            if ('SUBSTRING' == expr.name && expr.args.size() > 1) {
                // ANSI form: SUBSTRING(x FROM start [FOR length])
                sb.append(dialect.functionName(expr.name)).append('(')
                renderExpr(expr.args[0], sb, parameters)
                sb.append(' FROM ')
                renderExpr(expr.args[1], sb, parameters)
                if (expr.args.size() > 2) {
                    sb.append(' FOR ')
                    renderExpr(expr.args[2], sb, parameters)
                }
                sb.append(')')
                return
            }
            sb.append(dialect.functionName(expr.name)).append('(')
            expr.args.eachWithIndex { SqlExpr argExpr, int i ->
                if (i > 0) sb.append(', ')
                renderExpr(argExpr, sb, parameters)
            }
            sb.append(')')
        } else if (expr instanceof SqlCountStar) {
            sb.append('COUNT(*)')
        } else if (expr instanceof SqlStar) {
            if (expr.tableAlias) sb.append(dialect.identifier(expr.tableAlias)).append('.')
            sb.append('*')
        } else {
            throw new GroovyBugError("Unknown SQL expression: ${expr?.getClass()?.name}")
        }
    }

    private void renderTableSource(SqlTableSource tableSource, StringBuilder sb, List<Expression> parameters) {
        if (tableSource instanceof SqlTableRef) {
            sb.append(dialect.identifier(tableSource.tableName))
        } else if (tableSource instanceof SqlDerivedTable) {
            sb.append('(')
            renderQuery(tableSource.query, sb, parameters)
            sb.append(')')
        } else {
            throw new GroovyBugError("Unknown table source: ${tableSource?.getClass()?.name}")
        }
        sb.append(' ').append(dialect.identifier(tableSource.alias))
    }

    private static void renderLiteral(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append('NULL')
        } else if (value instanceof String || value instanceof Character) {
            sb.append("'").append(value.toString().replace("'", "''")).append("'")
        } else if (value instanceof Boolean) {
            sb.append(value ? 'TRUE' : 'FALSE')
        } else if (value instanceof Number) {
            sb.append(value)
        } else {
            throw new GroovyBugError("Unsupported SQL literal: ${value.getClass().name}")
        }
    }

    private void renderOperand(SqlExpr operand, int parentPrecedence, StringBuilder sb, List<Expression> parameters) {
        boolean parenthesize = operand instanceof SqlBinary && precedence(operand.op) < parentPrecedence
        if (parenthesize) sb.append('(')
        renderExpr(operand, sb, parameters)
        if (parenthesize) sb.append(')')
    }

    private static int precedence(String op) {
        switch (op) {
            case 'OR': return 1
            case 'AND': return 2
            case '=': case '<>': case '<': case '<=': case '>': case '>=': return 4
            case '+': case '-': case '||': return 5
            case '*': case '/': return 6
            default: return PREC_UNARY
        }
    }

    private static final List<String> NON_ASSOCIATIVE_OPS = ['-', '/']
    private static final int PREC_UNARY = 7
}
