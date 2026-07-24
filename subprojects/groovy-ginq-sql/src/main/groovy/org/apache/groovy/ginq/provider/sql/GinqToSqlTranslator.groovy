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
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Types

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.minusX
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX

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
    private final GinqToSqlTranslator outerScope
    private final List<String> tableAliases = []
    private String groupAlias
    private final Map<String, SqlExpr> groupKeysByName = [:]
    private String aggregateLambdaParam

    GinqToSqlTranslator(SourceUnit sourceUnit, GinqToSqlTranslator outerScope = null) {
        this.sourceUnit = sourceUnit
        this.outerScope = outerScope
    }

    @Override
    SourceUnit getSourceUnit() {
        return sourceUnit
    }

    SqlQueryNode translate(AbstractGinqExpression expression) {
        if (expression instanceof SetOperationExpression) {
            return translateSetOperation(expression, true)
        }
        if (expression instanceof GinqExpression) {
            return translateQuery(expression, QueryContext.TOP_LEVEL)
        }
        error("`${expression.text}` is not supported by the native-sql provider", expression)
        return null
    }

    private SqlSetQuery translateSetOperation(SetOperationExpression setOperationExpression, boolean outermost) {
        AbstractGinqExpression leftExpression = setOperationExpression.left
        SqlQueryNode left = leftExpression instanceof SetOperationExpression
                ? translateSetOperation(leftExpression, false)
                : new GinqToSqlTranslator(sourceUnit).translateQuery(requireGinqExpression(leftExpression), QueryContext.INNER_OPERAND)
        SqlQuery right = new GinqToSqlTranslator(sourceUnit).translateQuery(setOperationExpression.right,
                outermost ? QueryContext.FINAL_OPERAND : QueryContext.INNER_OPERAND)

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
        SqlSetQuery setQuery = new SqlSetQuery(left, op, right)
        if (outermost) {
            hoistOrderAndLimit(setQuery, setOperationExpression.right)
        }
        return setQuery
    }

    /**
     * Moves the final operand's {@code orderby}/{@code limit} onto the set operation
     * result, matching SQL's trailing {@code ORDER BY} semantics. Order keys must match
     * a select-list item and are rendered as ordinals since alias-qualified references
     * are not visible after a set operation.
     */
    private void hoistOrderAndLimit(SqlSetQuery setQuery, GinqExpression finalOperand) {
        SqlQuery right = setQuery.right
        if (right.orderBy) {
            for (SqlOrderSpec orderSpec : right.orderBy) {
                int index = right.projections.findIndexOf { SqlProjection projection -> sameExpr(projection.expr, orderSpec.expr) }
                if (index < 0) {
                    error('`orderby` on a set operation result must reference columns of the final `select` clause',
                            finalOperand.orderExpression)
                }
                setQuery.orderBy << new SqlOrderSpec(new SqlOrdinal(index + 1), orderSpec.asc, orderSpec.nullsLast)
            }
            right.orderBy.clear()
        }
        setQuery.offset = right.offset
        setQuery.fetch = right.fetch
        right.offset = null
        right.fetch = null
    }

    private static boolean sameExpr(SqlExpr a, SqlExpr b) {
        if (a.class != b.class) return false
        if (a instanceof SqlColumn) {
            SqlColumn other = (SqlColumn) b
            return a.tableAlias == other.tableAlias && a.column == other.column
        }
        if (a instanceof SqlCountStar) return true
        if (a instanceof SqlStar) return a.tableAlias == ((SqlStar) b).tableAlias
        if (a instanceof SqlFunction) {
            SqlFunction other = (SqlFunction) b
            return a.name == other.name && a.args.size() == other.args.size() &&
                    (0..<a.args.size()).every { int i -> sameExpr(a.args[i], other.args[i]) }
        }
        if (a instanceof SqlBinary) {
            SqlBinary other = (SqlBinary) b
            return a.op == other.op && sameExpr(a.left, other.left) && sameExpr(a.right, other.right)
        }
        if (a instanceof SqlUnary) {
            SqlUnary other = (SqlUnary) b
            return a.op == other.op && sameExpr(a.operand, other.operand)
        }
        if (a instanceof SqlIsNull) {
            SqlIsNull other = (SqlIsNull) b
            return a.negated == other.negated && sameExpr(a.expr, other.expr)
        }
        return false // parameters and parameter-bearing expressions are never comparable
    }

    private GinqExpression requireGinqExpression(AbstractGinqExpression expression) {
        if (!(expression instanceof GinqExpression)) {
            error("`${expression.text}` is not supported by the native-sql provider", expression)
        }
        return (GinqExpression) expression
    }

    private SqlQuery translateQuery(GinqExpression ginqExpression, QueryContext context) {
        SqlQuery query = new SqlQuery()

        query.from = tableSource(ginqExpression.fromExpression)
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
            if (QueryContext.INNER_OPERAND == context) {
                error('`orderby` within set operation operands is only supported on the final operand, ' +
                        'where it orders the combined result', ginqExpression.orderExpression)
            }
            translateOrders(ginqExpression.orderExpression.ordersExpr, query)
        }

        if (ginqExpression.limitExpression != null) {
            if (QueryContext.INNER_OPERAND == context) {
                error('`limit` within set operation operands is only supported on the final operand, ' +
                        'where it limits the combined result', ginqExpression.limitExpression)
            }
            translateLimit(ginqExpression.limitExpression.offsetAndSizeExpr, query)
        }

        translateSelect(ginqExpression, query)

        return query
    }

    private SqlTableSource tableSource(DataSourceExpression dataSourceExpression) {
        Expression dataSourceExpr = dataSourceExpression.dataSourceExpr
        String alias = dataSourceExpression.aliasExpr.text
        if (dataSourceExpr instanceof GinqExpression) {
            // no outer scope: standard SQL derived tables cannot be correlated
            SqlQuery query = new GinqToSqlTranslator(sourceUnit).translateQuery(dataSourceExpr, QueryContext.TOP_LEVEL)
            return new SqlDerivedTable(query, alias)
        }
        if (dataSourceExpr instanceof AbstractGinqExpression) {
            error('set operations are not supported as table sources by the native-sql provider', dataSourceExpr)
        }
        if (dataSourceExpr instanceof ConstantExpression && dataSourceExpr.value instanceof String) {
            return new SqlTableRef((String) dataSourceExpr.value, alias)
        }
        error("table references must be string literals or subqueries with the native-sql provider, e.g. `from ${alias} in 'employees'`", dataSourceExpr)
        return null
    }

    private SqlQuery correlatedSubQuery(GinqExpression ginqExpression) {
        return new GinqToSqlTranslator(sourceUnit, this).translateQuery(ginqExpression, QueryContext.TOP_LEVEL)
    }

    private boolean isKnownAlias(String name) {
        return name in tableAliases || (outerScope != null && outerScope.isKnownAlias(name))
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

        SqlTableSource table = tableSource(joinExpression)
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
        groupAlias = groupExpression.intoAlias // null for classic (no `into`) groupby

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
                if (alias == null) {
                    alias = groupKeyProjectionAlias(expr)
                }
                query.projections << new SqlProjection(translateExpr(expr), alias)
            }
        }
    }

    // a projected group key keeps its GINQ name as the column label, since the
    // underlying expression's own label, e.g. `e.deptId` for `groupby e.deptId as dept`,
    // may differ from the name the query author expects
    private String groupKeyProjectionAlias(Expression expr) {
        if (expr instanceof VariableExpression && groupKeysByName.containsKey(expr.text)) {
            return expr.text
        }
        if (expr instanceof PropertyExpression && groupAlias != null
                && expr.objectExpression instanceof VariableExpression
                && groupAlias == expr.objectExpression.text
                && expr.property instanceof ConstantExpression) {
            return expr.propertyAsString
        }
        return null
    }

    private SqlExpr translateExpr(Expression expr) {
        if (expr instanceof GinqExpression) {
            return new SqlScalarQuery(correlatedSubQuery(expr))
        }
        if (expr instanceof AbstractGinqExpression) {
            error("`${expr.text}` cannot be used as an expression with the native-sql provider", expr)
        }
        if (expr instanceof BooleanExpression) return translateExpr(expr.expression)
        if (expr instanceof TernaryExpression) return translateTernary(expr)
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

    private SqlExpr translateTernary(TernaryExpression expr) {
        if (expr instanceof ElvisOperatorExpression) {
            error('the Elvis operator cannot be translated to SQL by the native-sql provider; ' +
                    'use an explicit ternary with a `== null` check', expr)
        }
        return new SqlCaseWhen(
                translateExpr(expr.booleanExpression),
                translateCaseBranch(expr.trueExpression),
                translateCaseBranch(expr.falseExpression))
    }

    // CASE branch values that are compile-time constants are inlined as literals
    // (never runtime values, so parameter safety is preserved) since databases
    // cannot infer a type for `THEN ? ELSE ?`
    private SqlExpr translateCaseBranch(Expression expr) {
        if (expr instanceof ConstantExpression) {
            return new SqlLiteral(expr.value)
        }
        return translateExpr(expr)
    }

    private SqlExpr translateBinary(BinaryExpression expr) {
        String op = expr.operation.text
        Expression left = expr.leftExpression
        Expression right = expr.rightExpression
        switch (op) {
            case 'in':
            case '!in':
                if (right instanceof GinqExpression) {
                    return new SqlInQuery(translateExpr(left), correlatedSubQuery(right), '!in' == op)
                }
                if (!(right instanceof ListExpression)) {
                    error('only list literals and subqueries are supported on the right-hand side of `in` by the native-sql provider', right)
                }
                List<SqlExpr> values = ((ListExpression) right).expressions.collect { Expression e -> translateExpr(e) }
                return new SqlIn(translateExpr(left), values, '!in' == op)
            case '==':
            case '!=':
                boolean negated = '!=' == op
                if (isNullLiteral(right)) return new SqlIsNull(translateExpr(left), negated)
                if (isNullLiteral(left)) return new SqlIsNull(translateExpr(right), negated)
                return new SqlBinary(negated ? '<>' : '=', translateExpr(left), translateExpr(right))
            case '+':
                if (isStringy(left) || isStringy(right)) {
                    return new SqlBinary('||', translateExpr(left), translateExpr(right))
                }
                return new SqlBinary(op, translateExpr(left), translateExpr(right))
            case '<':
            case '<=':
            case '>':
            case '>=':
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

    // best-effort compile-time check that `+` denotes string concatenation
    private static boolean isStringy(Expression expr) {
        if (expr instanceof ConstantExpression && expr.value instanceof String) return true
        if (expr instanceof BinaryExpression && '+' == expr.operation.text) {
            return isStringy(expr.leftExpression) || isStringy(expr.rightExpression)
        }
        return false
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
            if (isKnownAlias(name)) { // an outer-scope alias, i.e. a correlated reference
                return new SqlColumn(name, property)
            }
        }
        error("Unknown alias in `${expr.text}`; known aliases: ${knownAliases()}", expr)
        return null
    }

    private SqlExpr translateVariable(VariableExpression expr) {
        String name = expr.text
        if (isKnownAlias(name) || name == groupAlias) {
            error("whole-row reference `${name}` is only supported as a top-level `select` projection", expr)
        }
        if ('_rn' == name) {
            error('`_rn` is not supported by the native-sql provider', expr)
        }
        SqlExpr keyExpr = groupKeysByName[name]
        if (keyExpr != null) { // an `as` alias of a groupby key, e.g. `groupby e.deptId as dept ... select dept`
            return keyExpr
        }
        return new SqlParam(expr) // a captured variable becomes a bound parameter
    }

    private SqlExpr translateMethodCall(MethodCallExpression call) {
        String method = call.methodAsString
        List<Expression> args = ((TupleExpression) call.arguments).expressions
        if ('over' == method) {
            error('window functions are not yet supported by the native-sql provider', call)
        }
        if ('exists' == method && args.empty && call.objectExpression instanceof GinqExpression) {
            return new SqlExists(correlatedSubQuery((GinqExpression) call.objectExpression))
        }
        if (groupAlias != null && !call.implicitThis
                && call.objectExpression instanceof VariableExpression && groupAlias == call.objectExpression.text) {
            return translateAggregate(call, args, true)
        }
        if (call.implicitThis && AGGREGATE_FUNCTION_MAP.containsKey(method)) {
            return translateAggregate(call, args, false)
        }
        if (!call.implicitThis) {
            switch (method) {
                case 'contains':
                    return translateLike(call, args, true, true)
                case 'startsWith':
                    return translateLike(call, args, false, true)
                case 'endsWith':
                    return translateLike(call, args, true, false)
                case 'substring':
                    return translateSubstring(call, args)
                case 'replace':
                    if (2 != args.size()) {
                        error('`replace` expects two arguments', call)
                    }
                    return new SqlFunction('REPLACE',
                            [translateExpr(call.objectExpression), translateExpr(args[0]), translateExpr(args[1])])
            }
            String functionName = method == null ? null : FUNCTION_MAP[method]
            if (functionName != null) {
                if (args) {
                    error("Method `${method}` with arguments cannot be translated to SQL by the native-sql provider", call)
                }
                return new SqlFunction(functionName, [translateExpr(call.objectExpression)])
            }
        }
        error("Method `${method}` cannot be translated to SQL by the native-sql provider", call)
        return null
    }

    private SqlExpr translateLike(MethodCallExpression call, List<Expression> argList, boolean matchBefore, boolean matchAfter) {
        if (1 != argList.size()) {
            error("`${call.methodAsString}` expects a single argument", call)
        }
        SqlExpr receiver = translateExpr(call.objectExpression)
        Expression valueExpr = requireParamValue(argList[0], call.methodAsString)
        // wildcards in the runtime value are escaped so it always matches literally
        Expression patternExpr = callX(classX(SQL_GINQ_RUNTIME_TYPE), 'likePattern',
                new ArgumentListExpression(valueExpr, constX(matchBefore), constX(matchAfter)))
        return new SqlLike(receiver, new SqlParam(patternExpr))
    }

    private SqlExpr translateSubstring(MethodCallExpression call, List<Expression> argList) {
        if (argList.size() !in [1, 2]) {
            error('`substring` expects one or two arguments', call)
        }
        SqlExpr receiver = translateExpr(call.objectExpression)
        Expression beginExpr = requireParamValue(argList[0], 'substring')
        List<SqlExpr> functionArgs = [receiver]
        // Groovy's 0-based begin index becomes SQL's 1-based FROM position
        functionArgs << new SqlParam(plusX(beginExpr, constX(1)))
        if (2 == argList.size()) {
            Expression endExpr = requireParamValue(argList[1], 'substring')
            // Groovy's exclusive end index becomes SQL's FOR length
            functionArgs << new SqlParam(minusX(endExpr, beginExpr))
        }
        return new SqlFunction('SUBSTRING', functionArgs)
    }

    private Expression requireParamValue(Expression expr, String method) {
        SqlExpr translated = translateExpr(expr)
        if (!(translated instanceof SqlParam)) {
            error("the arguments of `${method}` must be literals or captured variables", expr)
        }
        return ((SqlParam) translated).valueExpr
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

    private static enum QueryContext {
        TOP_LEVEL, INNER_OPERAND, FINAL_OPERAND
    }

    private static final ClassNode SQL_GINQ_RUNTIME_TYPE = ClassHelper.makeCached(SqlGinqRuntime)
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
