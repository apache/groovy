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
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.GinqSyntaxError
import org.apache.groovy.ginq.dsl.SyntaxErrorReportable
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.GroupExpression
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlColumn
import org.apache.groovy.ginq.provider.sql.ir.SqlCountStar
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlIn
import org.apache.groovy.ginq.provider.sql.ir.SqlIsNull
import org.apache.groovy.ginq.provider.sql.ir.SqlJoin
import org.apache.groovy.ginq.provider.sql.ir.SqlOrderSpec
import org.apache.groovy.ginq.provider.sql.ir.SqlParam
import org.apache.groovy.ginq.provider.sql.ir.SqlProjection
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.ir.SqlSetQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlStar
import org.apache.groovy.ginq.provider.sql.ir.SqlTableRef
import org.apache.groovy.ginq.provider.sql.ir.SqlUnary
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Types

/**
 * Translates the GINQ AST into the SQL IR, in strict mode: every construct
 * either translates cleanly or is rejected with a compile-time error.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class GinqToSqlTranslator implements SyntaxErrorReportable {
    private final SourceUnit sourceUnit
    private final List<String> tableAliases = []
    private String groupAlias
    private final Map<String, SqlExpr> groupKeysByName = [:]
    private String aggregateLambdaParam

    GinqToSqlTranslator(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    @Override
    SourceUnit getSourceUnit() {
        return sourceUnit
    }

    SqlQueryNode translate(AbstractGinqExpression expression) {
        if (expression instanceof SetOperationExpression) {
            return translateSetOperation(expression)
        }
        if (expression instanceof GinqExpression) {
            return translateQuery(expression, false)
        }
        error("`${expression.text}` is not supported by the native-sql provider", expression)
        return null
    }

    private SqlSetQuery translateSetOperation(SetOperationExpression setOperationExpression) {
        AbstractGinqExpression leftExpression = setOperationExpression.left
        SqlQueryNode left = leftExpression instanceof SetOperationExpression
                ? translateSetOperation(leftExpression)
                : new GinqToSqlTranslator(sourceUnit).translateQuery(requireGinqExpression(leftExpression), true)
        SqlQuery right = new GinqToSqlTranslator(sourceUnit).translateQuery(setOperationExpression.right, true)

        SqlSetQuery.SetOp op
        switch (setOperationExpression.operation) {
            case 'union': op = SqlSetQuery.SetOp.UNION; break
            case 'unionall': op = SqlSetQuery.SetOp.UNION_ALL; break
            case 'intersect': op = SqlSetQuery.SetOp.INTERSECT; break
            case 'minus': op = SqlSetQuery.SetOp.MINUS; break
            default:
                error("Unknown set operation: ${setOperationExpression.operation}", setOperationExpression)
                return null
        }
        return new SqlSetQuery(left, op, right)
    }

    private GinqExpression requireGinqExpression(AbstractGinqExpression expression) {
        if (!(expression instanceof GinqExpression)) {
            error("`${expression.text}` is not supported by the native-sql provider", expression)
        }
        return (GinqExpression) expression
    }

    private SqlQuery translateQuery(GinqExpression ginqExpression, boolean setOperationOperand) {
        SqlQuery query = new SqlQuery()

        query.from = tableRef(ginqExpression.fromExpression)
        tableAliases << ginqExpression.fromExpression.aliasExpr.text

        for (JoinExpression joinExpression : ginqExpression.joinExpressionList) {
            query.joins << translateJoin(joinExpression)
        }

        if (ginqExpression.whereExpression != null) {
            query.where = translateExpr(ginqExpression.whereExpression.filterExpr)
        }

        GroupExpression groupExpression = ginqExpression.groupExpression
        if (groupExpression != null) {
            translateGroup(groupExpression, query)
        }

        if (ginqExpression.orderExpression != null) {
            if (setOperationOperand) {
                error('`orderby` within set operation operands is not supported by the native-sql provider', ginqExpression.orderExpression)
            }
            translateOrders(ginqExpression.orderExpression.ordersExpr, query)
        }

        if (ginqExpression.limitExpression != null) {
            if (setOperationOperand) {
                error('`limit` within set operation operands is not supported by the native-sql provider', ginqExpression.limitExpression)
            }
            translateLimit(ginqExpression.limitExpression.offsetAndSizeExpr, query)
        }

        translateSelect(ginqExpression, query)

        return query
    }

    private SqlTableRef tableRef(DataSourceExpression dataSourceExpression) {
        Expression dataSourceExpr = dataSourceExpression.dataSourceExpr
        String alias = dataSourceExpression.aliasExpr.text
        if (dataSourceExpr instanceof AbstractGinqExpression) {
            error('subqueries are not yet supported by the native-sql provider', dataSourceExpr)
        }
        if (dataSourceExpr instanceof ConstantExpression && dataSourceExpr.value instanceof String) {
            return new SqlTableRef((String) dataSourceExpr.value, alias)
        }
        error("table references must be string literals with the native-sql provider, e.g. `from ${alias} in 'employees'`", dataSourceExpr)
        return null
    }

    private SqlJoin translateJoin(JoinExpression joinExpression) {
        SqlJoin.Type type
        switch (joinExpression.joinName) {
            case 'join':
            case 'innerjoin':
            case 'innerhashjoin':
                type = SqlJoin.Type.INNER; break
            case 'leftjoin':
            case 'lefthashjoin':
                type = SqlJoin.Type.LEFT; break
            case 'rightjoin':
            case 'righthashjoin':
                type = SqlJoin.Type.RIGHT; break
            case 'fulljoin':
            case 'fullhashjoin':
                type = SqlJoin.Type.FULL; break
            case 'crossjoin':
                type = SqlJoin.Type.CROSS; break
            default:
                error("Unknown join: ${joinExpression.joinName}", joinExpression)
                return null
        }

        SqlTableRef table = tableRef(joinExpression)
        tableAliases << joinExpression.aliasExpr.text

        SqlExpr on = null
        if (joinExpression.onExpression != null) {
            on = translateExpr(joinExpression.onExpression.filterExpr)
        } else if (SqlJoin.Type.CROSS != type) {
            error("`on` clause is expected for `${joinExpression.joinName}`", joinExpression)
        }
        return new SqlJoin(type, table, on)
    }

    private void translateGroup(GroupExpression groupExpression, SqlQuery query) {
        groupAlias = groupExpression.intoAlias
        if (!groupAlias) {
            error('`groupby` without `into` is not yet supported by the native-sql provider; use `groupby ... into g`', groupExpression)
        }

        for (Expression classifier : ((TupleExpression) groupExpression.classifierExpr).expressions) {
            String name
            Expression valueExpr
            if (classifier instanceof CastExpression) {
                name = classifier.type.text
                valueExpr = classifier.expression
            } else if (classifier instanceof PropertyExpression && classifier.property instanceof ConstantExpression) {
                name = classifier.propertyAsString
                valueExpr = classifier
            } else {
                name = classifier.text
                valueExpr = classifier
            }
            SqlExpr keyExpr = translateExpr(valueExpr)
            query.groupBy << keyExpr
            groupKeysByName[name] = keyExpr
        }

        if (groupExpression.havingExpression != null) {
            query.having = translateExpr(groupExpression.havingExpression.filterExpr)
        }
    }

    private void translateOrders(Expression ordersExpr, SqlQuery query) {
        for (Expression orderExpr : ((TupleExpression) ordersExpr).expressions) {
            Expression target = orderExpr
            boolean asc = true
            boolean nullsLast = true
            if (orderExpr instanceof BinaryExpression && orderExpr.operation.type == Types.KEYWORD_IN) {
                target = orderExpr.leftExpression
                Expression orderOptionExpr = orderExpr.rightExpression
                String orderOption = orderOptionExpr instanceof MethodCallExpression
                        ? orderOptionExpr.methodAsString
                        : orderOptionExpr.text
                if (orderOption !in ORDER_OPTION_LIST) {
                    error("Invalid order: ${orderOption}, `asc`/`desc` is expected", orderOptionExpr)
                }
                asc = 'asc' == orderOption
                if (orderOptionExpr instanceof MethodCallExpression) {
                    List<Expression> optionArgs = ((TupleExpression) orderOptionExpr.arguments).expressions
                    String nullsOption = 1 == optionArgs.size() ? optionArgs[0].text : null
                    if (nullsOption !in NULLS_OPTION_LIST) {
                        error('Only `nullslast`/`nullsfirst` is expected', orderOptionExpr.arguments)
                    }
                    nullsLast = 'nullslast' == nullsOption
                }
            }
            query.orderBy << new SqlOrderSpec(translateExpr(target), asc, nullsLast)
        }
    }

    private void translateLimit(Expression offsetAndSizeExpr, SqlQuery query) {
        List<Expression> expressions = offsetAndSizeExpr instanceof TupleExpression
                ? offsetAndSizeExpr.expressions
                : [offsetAndSizeExpr]
        if (1 == expressions.size()) {
            query.fetch = new SqlParam(expressions[0])
        } else {
            query.offset = new SqlParam(expressions[0])
            query.fetch = new SqlParam(expressions[1])
        }
    }

    private void translateSelect(GinqExpression ginqExpression, SqlQuery query) {
        SelectExpression selectExpression = ginqExpression.selectExpression
        Boolean distinct = ginqExpression.getNodeMetaData(GinqAstBuilder.GINQ_SELECT_DISTINCT)
        query.distinct = Boolean.TRUE == distinct

        for (Expression projection : ((TupleExpression) selectExpression.projectionExpr).expressions) {
            String alias = null
            Expression expr = projection
            if (projection instanceof CastExpression) {
                alias = projection.type.text
                expr = projection.expression
            }
            if (expr instanceof VariableExpression && expr.text in tableAliases) {
                if (alias != null) {
                    error("whole-row projection `${expr.text}` cannot be aliased", projection)
                }
                query.projections << new SqlProjection(new SqlStar(expr.text))
            } else {
                query.projections << new SqlProjection(translateExpr(expr), alias)
            }
        }
    }

    private SqlExpr translateExpr(Expression expr) {
        if (expr instanceof AbstractGinqExpression) {
            error('subqueries are not yet supported by the native-sql provider', expr)
        }
        if (expr instanceof BinaryExpression) return translateBinary(expr)
        if (expr instanceof NotExpression) return new SqlUnary('NOT', translateExpr(expr.expression))
        if (expr instanceof UnaryMinusExpression) return new SqlUnary('-', translateExpr(expr.expression))
        if (expr instanceof PropertyExpression) return translateProperty(expr)
        if (expr instanceof MethodCallExpression) return translateMethodCall(expr)
        if (expr instanceof ConstantExpression) return new SqlParam(expr)
        if (expr instanceof VariableExpression) return translateVariable(expr)
        error("Expression `${expr.text}` cannot be translated to SQL by the native-sql provider", expr)
        return null
    }

    private SqlExpr translateBinary(BinaryExpression expr) {
        String op = expr.operation.text
        Expression left = expr.leftExpression
        Expression right = expr.rightExpression
        switch (op) {
            case 'in':
            case '!in':
                if (!(right instanceof ListExpression)) {
                    error('only list literals are supported on the right-hand side of `in` by the native-sql provider', right)
                }
                List<SqlExpr> values = ((ListExpression) right).expressions.collect { Expression e -> translateExpr(e) }
                return new SqlIn(translateExpr(left), values, '!in' == op)
            case '==':
            case '!=':
                boolean negated = '!=' == op
                if (isNullLiteral(right)) return new SqlIsNull(translateExpr(left), negated)
                if (isNullLiteral(left)) return new SqlIsNull(translateExpr(right), negated)
                return new SqlBinary(negated ? '<>' : '=', translateExpr(left), translateExpr(right))
            case '<':
            case '<=':
            case '>':
            case '>=':
            case '+':
            case '-':
            case '*':
            case '/':
                return new SqlBinary(op, translateExpr(left), translateExpr(right))
            case '&&':
                return new SqlBinary('AND', translateExpr(left), translateExpr(right))
            case '||':
                return new SqlBinary('OR', translateExpr(left), translateExpr(right))
            case '%':
                return new SqlFunction('MOD', [translateExpr(left), translateExpr(right)])
            default:
                error("Operator `${op}` cannot be translated to SQL by the native-sql provider", expr)
                return null
        }
    }

    private static boolean isNullLiteral(Expression expr) {
        return expr instanceof ConstantExpression && null == expr.value
    }

    private SqlExpr translateProperty(PropertyExpression expr) {
        if (!(expr.property instanceof ConstantExpression)) {
            error("Expression `${expr.text}` cannot be translated to SQL by the native-sql provider", expr)
        }
        String property = expr.propertyAsString
        Expression object = expr.objectExpression
        if (object instanceof VariableExpression) {
            String name = object.text
            if (name == aggregateLambdaParam) {
                if (name in tableAliases) return new SqlColumn(name, property)
                if (1 == tableAliases.size()) return new SqlColumn(tableAliases[0], property)
                error("cannot resolve `${expr.text}`: name the aggregate lambda parameter after a table alias for multi-table queries", expr)
            }
            if (name in tableAliases) {
                return new SqlColumn(name, property)
            }
            if (name == groupAlias) {
                SqlExpr keyExpr = groupKeysByName[property]
                if (keyExpr == null) {
                    error("`${expr.text}` is not in the `groupby` clause", expr)
                }
                return keyExpr
            }
        }
        error("Unknown alias in `${expr.text}`; known aliases: ${knownAliases()}", expr)
        return null
    }

    private SqlExpr translateVariable(VariableExpression expr) {
        String name = expr.text
        if (name in tableAliases || name == groupAlias) {
            error("whole-row reference `${name}` is only supported as a top-level `select` projection", expr)
        }
        if ('_rn' == name) {
            error('`_rn` is not supported by the native-sql provider', expr)
        }
        return new SqlParam(expr) // a captured variable becomes a bound parameter
    }

    private SqlExpr translateMethodCall(MethodCallExpression call) {
        String method = call.methodAsString
        List<Expression> args = ((TupleExpression) call.arguments).expressions
        if ('over' == method) {
            error('window functions are not yet supported by the native-sql provider', call)
        }
        if (groupAlias != null && !call.implicitThis
                && call.objectExpression instanceof VariableExpression && groupAlias == call.objectExpression.text) {
            return translateAggregate(call, args, true)
        }
        if (call.implicitThis && AGGREGATE_FUNCTION_MAP.containsKey(method)) {
            return translateAggregate(call, args, false)
        }
        String functionName = method == null ? null : FUNCTION_MAP[method]
        if (!call.implicitThis && functionName != null) {
            if (args) {
                error("Method `${method}` with arguments cannot be translated to SQL by the native-sql provider", call)
            }
            return new SqlFunction(functionName, [translateExpr(call.objectExpression)])
        }
        error("Method `${method}` cannot be translated to SQL by the native-sql provider", call)
        return null
    }

    private SqlExpr translateAggregate(MethodCallExpression call, List<Expression> args, boolean lambdaStyle) {
        String method = call.methodAsString
        String functionName = AGGREGATE_FUNCTION_MAP[method]
        if (functionName == null) {
            error("`${method}` is not a supported aggregate function (supported: ${AGGREGATE_FUNCTION_MAP.keySet()})", call)
        }
        if ('count' == method && args.empty) {
            return new SqlCountStar()
        }
        if (1 != args.size()) {
            error("aggregate function `${method}` expects a single argument", call)
        }
        Expression arg = args[0]
        if (!lambdaStyle) {
            return new SqlFunction(functionName, [translateExpr(arg)])
        }
        if (!(arg instanceof ClosureExpression)) {
            error("aggregate function `${method}` expects a lambda argument, e.g. `${groupAlias}.${method}(e -> e.salary)`", arg)
        }
        ClosureExpression closure = (ClosureExpression) arg
        String param = closure.parameters ? closure.parameters[0].name : 'it'
        Expression body = extractSingleExpression(closure)
        String previousParam = aggregateLambdaParam
        aggregateLambdaParam = param
        try {
            return new SqlFunction(functionName, [translateExpr(body)])
        } finally {
            aggregateLambdaParam = previousParam
        }
    }

    private Expression extractSingleExpression(ClosureExpression closure) {
        Statement code = closure.code
        if (code instanceof BlockStatement && 1 == code.statements.size()) {
            code = code.statements[0]
        }
        if (code instanceof ExpressionStatement) {
            return code.expression
        }
        if (code instanceof ReturnStatement) {
            return code.expression
        }
        error('aggregate lambdas must consist of a single expression', closure)
        return null
    }

    private String knownAliases() {
        List<String> known = new ArrayList<>(tableAliases)
        if (groupAlias) known << groupAlias
        return known.toString()
    }

    private void error(String message, org.codehaus.groovy.ast.ASTNode node) {
        collectSyntaxError(new GinqSyntaxError(message, node.lineNumber, node.columnNumber))
    }

    private static final List<String> ORDER_OPTION_LIST = ['asc', 'desc']
    private static final List<String> NULLS_OPTION_LIST = ['nullsfirst', 'nullslast']
    private static final Map<String, String> AGGREGATE_FUNCTION_MAP = [
            count: 'COUNT',
            sum  : 'SUM',
            avg  : 'AVG',
            min  : 'MIN',
            max  : 'MAX',
    ]
    private static final Map<String, String> FUNCTION_MAP = [
            toUpperCase: 'UPPER',
            toLowerCase: 'LOWER',
            trim       : 'TRIM',
            length     : 'CHAR_LENGTH',
            size       : 'CHAR_LENGTH',
            abs        : 'ABS',
    ]
}
