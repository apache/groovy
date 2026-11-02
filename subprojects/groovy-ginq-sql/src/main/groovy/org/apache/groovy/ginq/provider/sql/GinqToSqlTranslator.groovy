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
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.GinqSyntaxError
import org.apache.groovy.ginq.dsl.SyntaxErrorReportable
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.FromExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.GroupExpression
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.dsl.expression.OnExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCaseWhen
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
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
import org.apache.groovy.ginq.provider.sql.ir.SqlWindow
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowBound
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowCall
import org.apache.groovy.ginq.provider.sql.ir.SqlWindowFrame
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
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

import java.math.BigDecimal
import java.math.BigInteger

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX
import static org.codehaus.groovy.ast.tools.GeneralUtils.minusX
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Translates the GINQ AST into the SQL IR. In strict mode every construct
 * either translates cleanly or is rejected with a compile-time error. With
 * {@link #recoverUntranslatable} set, constructs that have no SQL equivalent
 * raise {@link Untranslatable} so mixed mode can move that clause in memory.
 * <p>
 * {@code fn(...) over(...)} is a window call. It is translated only when
 * {@code fn} is registered as {@link SqlFunctionMapping.Kind#WINDOW}.
 * {@link SqlFunctionRegistry#standard()} registers none, so the call is
 * rejected with a stable "window functions are deferred" message and
 * mixed mode keeps running it in memory. A follow-up registers the GINQ
 * window functions; this class already builds {@link SqlWindowCall}.
 *
 * @since 7.0.0
 */
@Incubating
@CompileStatic
class GinqToSqlTranslator implements SyntaxErrorReportable {
    private final SourceUnit sourceUnit
    private final GinqToSqlTranslator outerScope
    private final String providerName
    private final SqlFunctionRegistry functions
    private final List<String> tableAliases = []
    private String groupAlias
    private final Map<String, SqlExpr> groupKeysByName = [:]
    private String aggregateLambdaParam
    private boolean recoverUntranslatable
    private final SqlFunctionSite calls = new CallSite()

    GinqToSqlTranslator(SourceUnit sourceUnit, String providerName = 'native-sql') {
        this(sourceUnit, providerName, SqlFunctionRegistry.standard())
    }

    /**
     * @param functions scalar and window mappings. {@link SqlFunctionRegistry#standard()}
     * registers no window functions. A copy with {@link SqlFunctionMapping.Kind#WINDOW}
     * entries is how a caller, including a test, turns {@code over} into
     * {@link SqlWindowCall} without changing the sealed built-in set.
     */
    @PackageScope
    GinqToSqlTranslator(SourceUnit sourceUnit, String providerName, SqlFunctionRegistry functions) {
        this.sourceUnit = sourceUnit
        this.outerScope = null
        this.providerName = providerName
        this.functions = requireFunctions(functions)
    }

    private GinqToSqlTranslator(SourceUnit sourceUnit, GinqToSqlTranslator outerScope) {
        this.sourceUnit = sourceUnit
        this.outerScope = outerScope
        this.providerName = outerScope.providerName
        this.functions = outerScope.functions
        this.recoverUntranslatable = outerScope.recoverUntranslatable
    }

    private static SqlFunctionRegistry requireFunctions(SqlFunctionRegistry functions) {
        if (functions == null) {
            throw new IllegalArgumentException('functions is required')
        }
        return functions
    }

    private GinqToSqlTranslator nest(boolean correlated) {
        GinqToSqlTranslator child = correlated
                ? new GinqToSqlTranslator(sourceUnit, this)
                : new GinqToSqlTranslator(sourceUnit, providerName, functions)
        child.recoverUntranslatable = recoverUntranslatable
        return child
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
        reject("`${expression.text}` is not supported by the ${providerName} provider", expression)
        return null
    }

    private SqlSetQuery translateSetOperation(SetOperationExpression setOperationExpression, boolean outermost) {
        AbstractGinqExpression leftExpression = setOperationExpression.left
        SqlQueryNode left = leftExpression instanceof SetOperationExpression
                ? translateSetOperation(leftExpression, false)
                : nest(false).translateQuery(requireGinqExpression(leftExpression), QueryContext.INNER_OPERAND)
        SqlQuery right = nest(false).translateQuery(setOperationExpression.right,
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
        if (a instanceof SqlCast) {
            SqlCast other = (SqlCast) b
            return a.sqlType == other.sqlType && sameExpr(a.expr, other.expr)
        }
        return false // parameters and parameter-bearing expressions are never comparable
    }

    private GinqExpression requireGinqExpression(AbstractGinqExpression expression) {
        if (!(expression instanceof GinqExpression)) {
            error("`${expression.text}` is not supported by the ${providerName} provider", expression)
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
            SqlQuery query = nest(false).translateQuery(dataSourceExpr, QueryContext.TOP_LEVEL)
            return new SqlDerivedTable(query, alias)
        }
        if (dataSourceExpr instanceof AbstractGinqExpression) {
            reject("set operations are not supported as table sources by the ${providerName} provider", dataSourceExpr)
            return null
        }
        if (dataSourceExpr instanceof ConstantExpression && dataSourceExpr.value instanceof String) {
            String tableName = (String) dataSourceExpr.value
            if (!plainTableName(tableName)) {
                reject(invalidTableNameMessage(tableName), dataSourceExpr, false)
                return null
            }
            return new SqlTableRef(tableName, alias)
        }
        reject("table references must be string literals or subqueries with the ${providerName} provider, e.g. `from ${alias} in 'employees'`", dataSourceExpr)
        return null
    }

    private SqlQuery correlatedSubQuery(GinqExpression ginqExpression) {
        return nest(true).translateQuery(ginqExpression, QueryContext.TOP_LEVEL)
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
        for (SqlOrderSpec orderSpec : translateOrderList(ordersExpr)) {
            query.orderBy << orderSpec
        }
    }

    // Shared by query ORDER BY and window ORDER BY so asc/desc/nulls stay one path.
    private List<SqlOrderSpec> translateOrderList(Expression ordersExpr) {
        List<SqlOrderSpec> specs = []
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
                    error("`${orderOption}` is not an order direction; use `asc` or `desc`", orderOptionExpr)
                }
                asc = 'asc' == orderOption
                if (orderOptionExpr instanceof MethodCallExpression) {
                    List<Expression> optionArgs = ((TupleExpression) orderOptionExpr.arguments).expressions
                    String nullsOption = 1 == optionArgs.size() ? optionArgs[0].text : null
                    if (nullsOption !in NULLS_OPTION_LIST) {
                        error('`asc`/`desc` takes `nullsfirst` or `nullslast`', orderOptionExpr.arguments)
                    }
                    nullsLast = 'nullslast' == nullsOption
                }
            }
            specs << new SqlOrderSpec(translateExpr(target), asc, nullsLast)
        }
        return specs
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
            reject("`${expr.text}` cannot be used as an expression with the ${providerName} provider", expr)
            return null
        }
        // NotExpression extends BooleanExpression; the negation has to be kept
        if (expr instanceof NotExpression) return new SqlUnary('NOT', translateExpr(expr.expression))
        if (expr instanceof BooleanExpression) return translateExpr(expr.expression)
        if (expr instanceof TernaryExpression) return translateTernary(expr)
        if (expr instanceof BinaryExpression) return translateBinary(expr)
        if (expr instanceof UnaryMinusExpression) return new SqlUnary('-', translateExpr(expr.expression))
        if (expr instanceof PropertyExpression) return translateProperty(expr)
        if (expr instanceof MethodCallExpression) return translateMethodCall(expr)
        if (expr instanceof ConstantExpression) return new SqlParam(expr)
        if (expr instanceof VariableExpression) return translateVariable(expr)
        reject("Expression `${expr.text}` cannot be translated to SQL by the ${providerName} provider", expr)
        return null
    }

    private SqlExpr translateTernary(TernaryExpression expr) {
        if (expr instanceof ElvisOperatorExpression) {
            // truthiness is not SQL null, so ?: is not COALESCE
            reject("the Elvis operator cannot be translated to SQL by the ${providerName} provider; " +
                    'Groovy truthiness is not SQL null. Use `coalesce(...)` or an explicit ternary with a `== null` check', expr)
            return null
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
                if (right instanceof RangeExpression) {
                    return translateRangeMember(left, (RangeExpression) right, '!in' == op, expr)
                }
                if (!(right instanceof ListExpression)) {
                    reject("only list literals, constant ranges and subqueries are supported on the right-hand side of `in` by the ${providerName} provider", right)
                    return null
                }
                List<Expression> elements = ((ListExpression) right).expressions
                if (elements.isEmpty()) {
                    // `x in []` is false and `x !in []` is true, including when x is null
                    return new SqlBinary('=', new SqlLiteral(1), new SqlLiteral('!in' == op ? 1 : 0))
                }
                return translateInList(left, elements, '!in' == op)
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
                reject("Operator `${op}` cannot be translated to SQL by the ${providerName} provider", expr)
                return null
        }
    }

    // `x in [null, 1]` is true for a null x. SQL `IN` is unknown when the list
    // contains null, and `NOT IN` is unknown for every row, so null members
    // become IS NULL / IS NOT NULL and the remaining values stay in the list.
    private SqlExpr translateInList(Expression left, List<Expression> elements, boolean negated) {
        List<SqlExpr> values = []
        boolean includesNull = false
        for (Expression element : elements) {
            if (isNullLiteral(element)) {
                includesNull = true
            } else {
                values.add(translateExpr(element))
            }
        }
        SqlExpr member = translateExpr(left)
        if (values.isEmpty()) {
            return new SqlIsNull(member, negated)
        }
        SqlIn inList = new SqlIn(member, values, negated)
        if (!includesNull) {
            return inList
        }
        return new SqlBinary(negated ? 'AND' : 'OR', new SqlIsNull(member, negated), inList)
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
            reject("Expression `${expr.text}` cannot be translated to SQL by the ${providerName} provider", expr)
            return null
        }
        String property = expr.propertyAsString
        if (!simpleIdentifier(property)) {
            error("column `${property}` must be a simple identifier", expr)
            return null
        }
        Expression object = expr.objectExpression
        if (object instanceof VariableExpression) {
            String name = object.text
            if (name == aggregateLambdaParam) {
                if (name in tableAliases) return new SqlColumn(name, property)
                if (1 == tableAliases.size()) return new SqlColumn(tableAliases[0], property)
                error("cannot resolve `${expr.text}`: name the aggregate lambda parameter after a table alias for multi-table queries", expr)
                return null
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
            reject("whole-row reference `${name}` is only supported as a top-level `select` projection", expr)
            return null
        }
        if ('_rn' == name) {
            reject("`_rn` is a collection-provider row number and has no equivalent in the ${providerName} provider", expr)
            return null
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
        if (isWindowOver(call)) {
            return translateWindow(call)
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
        SqlFunctionMapping mapping = method == null ? null : functions.find(method, functionKind(call))
        if (mapping != null) {
            return mapping.translate(call, args, calls)
        }
        reject("Method `${method}` cannot be translated to SQL by the ${providerName} provider", call)
        return null
    }

    // Math.max is a static call. An implicit-this max/min is the aggregate, handled above.
    // A table alias named Math is not the class.
    private SqlFunctionMapping.Kind functionKind(MethodCallExpression call) {
        if (!call.implicitThis && isMathReceiver(call.objectExpression)) {
            return SqlFunctionMapping.Kind.MATH
        }
        if (call.implicitThis) {
            return SqlFunctionMapping.Kind.FREE
        }
        return SqlFunctionMapping.Kind.RECEIVER
    }

    // Same shape the collection provider treats as a window: fn(...).over() or fn(...).over(spec).
    // Checked before aggregates so sum(x) over(...) is not a grouped SUM.
    private static boolean isWindowOver(MethodCallExpression call) {
        if ('over' != call.methodAsString) return false
        if (!(call.objectExpression instanceof MethodCallExpression)) return false
        if (!(call.arguments instanceof TupleExpression)) return false
        return ((TupleExpression) call.arguments).expressions.size() < 2
    }

    private SqlExpr translateWindow(MethodCallExpression overCall) {
        MethodCallExpression functionCall = (MethodCallExpression) overCall.objectExpression
        String name = functionCall.methodAsString
        SqlFunctionMapping mapping = name == null ? null : functions.find(name, SqlFunctionMapping.Kind.WINDOW)
        if (mapping == null) {
            reject("${WINDOW_DEFERRED_PREFIX}${providerName} provider", overCall)
            return null
        }
        if (!(functionCall.arguments instanceof TupleExpression)) {
            reject("`${name}` cannot be translated to SQL by the ${providerName} provider", functionCall)
            return null
        }
        List<Expression> functionArgs = ((TupleExpression) functionCall.arguments).expressions
        SqlExpr function = mapping.translate(functionCall, functionArgs, calls)
        if (function == null) return null
        SqlWindow window = translateWindowSpec(overCall)
        if (window == null) return null
        return new SqlWindowCall(function, window)
    }

    private SqlWindow translateWindowSpec(MethodCallExpression overCall) {
        List<Expression> args = ((TupleExpression) overCall.arguments).expressions
        if (args.isEmpty()) {
            return new SqlWindow([], [], null)
        }
        WindowClauseFinder finder = new WindowClauseFinder()
        args.get(0).visit(finder)
        finder.dropNestedArguments()
        if (finder.unknown) {
            MethodCallExpression unknown = finder.unknown.get(0)
            reject("Unknown window clause: `${unknown.methodAsString ?: unknown.text}`", unknown)
            return null
        }
        if (finder.partitionBy.isEmpty() && finder.orderBy.isEmpty() && finder.rows.isEmpty() && finder.range.isEmpty()) {
            reject('`over` expects `partitionby`, `orderby`, `rows` or `range`', overCall)
            return null
        }
        if (finder.partitionBy.size() > 1) {
            reject('`partitionby` appears more than once in `over`', finder.partitionBy.get(1))
            return null
        }
        if (finder.orderBy.size() > 1) {
            reject('`orderby` appears more than once in `over`', finder.orderBy.get(1))
            return null
        }
        if (finder.rows.size() > 1) {
            reject('`rows` appears more than once in `over`', finder.rows.get(1))
            return null
        }
        if (finder.range.size() > 1) {
            reject('`range` appears more than once in `over`', finder.range.get(1))
            return null
        }
        if (finder.rows && finder.range) {
            reject('`rows` and `range` cannot both be used in one `over`', finder.range.get(0))
            return null
        }
        MethodCallExpression orderCall = finder.orderBy ? finder.orderBy.get(0) : null
        List<SqlOrderSpec> orderSpecs = []
        if (orderCall != null) {
            if (!(orderCall.arguments instanceof TupleExpression)) {
                reject('`orderby` in `over` must list its keys', orderCall)
                return null
            }
            orderSpecs = translateOrderList(orderCall.arguments)
            for (SqlOrderSpec spec : orderSpecs) {
                if (spec.expr == null) return null
            }
        }
        MethodCallExpression frameCall = finder.rows ? finder.rows.get(0) : (finder.range ? finder.range.get(0) : null)
        SqlWindowFrame frame = null
        if (frameCall != null) {
            boolean rows = 'rows' == frameCall.methodAsString
            if (finder.range && orderSpecs.size() != 1) {
                reject(orderCall == null
                        ? '`orderby` is expected when using `range`'
                        : 'Only one field is expected in the `orderby` clause when using `range`', frameCall)
                return null
            }
            if (!(frameCall.arguments instanceof TupleExpression)) {
                reject("Both lower bound and upper bound are expected for `${frameCall.methodAsString}`", frameCall)
                return null
            }
            List<Expression> bounds = ((TupleExpression) frameCall.arguments).expressions
            if (bounds.size() != 2) {
                reject("Both lower bound and upper bound are expected for `${frameCall.methodAsString}`", frameCall)
                return null
            }
            SqlWindowFrame.Unit unit = rows ? SqlWindowFrame.Unit.ROWS : SqlWindowFrame.Unit.RANGE
            SqlWindowBound lower = translateFrameBound(bounds.get(0), true, unit)
            if (lower == null) return null
            SqlWindowBound upper = translateFrameBound(bounds.get(1), false, unit)
            if (upper == null) return null
            frame = new SqlWindowFrame(unit, lower, upper)
        }
        List<SqlExpr> partition = []
        if (finder.partitionBy) {
            MethodCallExpression partitionCall = finder.partitionBy.get(0)
            if (!(partitionCall.arguments instanceof TupleExpression)) {
                reject('`partitionby` in `over` must list its expressions', partitionCall)
                return null
            }
            for (Expression part : ((TupleExpression) partitionCall.arguments).expressions) {
                SqlExpr sql = translateExpr(part)
                if (sql == null) return null
                partition << sql
            }
        }
        return new SqlWindow(partition, orderSpecs, frame)
    }

    // GINQ: null is unbounded, 0 is the current row, negative precedes, positive follows.
    // Only literals. A variable bound would hide its sign inside a parameter, and
    // PRECEDING/FOLLOWING is syntax, so mixed mode keeps that clause in memory.
    private SqlWindowBound translateFrameBound(Expression expr, boolean lower, SqlWindowFrame.Unit unit) {
        if (expr instanceof ConstantExpression && null == ((ConstantExpression) expr).value) {
            return lower ? SqlWindowBound.unboundedPreceding() : SqlWindowBound.unboundedFollowing()
        }
        Number signed = signedFrameNumber(expr)
        if (signed == null) {
            reject('window frame bounds must be literals: a number, `0` for the current row, or `null` for an unbounded bound', expr)
            return null
        }
        BigDecimal decimal = frameDecimal(signed)
        if (decimal == null) {
            reject('window frame bounds must be finite numbers', expr)
            return null
        }
        if (SqlWindowFrame.Unit.ROWS == unit && !wholeFrameNumber(decimal)) {
            reject('`rows` bounds must be whole numbers', expr)
            return null
        }
        int sign = decimal.signum()
        if (sign == 0) return SqlWindowBound.currentRow()
        SqlLiteral magnitude = new SqlLiteral(frameMagnitude(decimal))
        return sign < 0 ? SqlWindowBound.preceding(magnitude) : SqlWindowBound.following(magnitude)
    }

    private static Number signedFrameNumber(Expression expr) {
        if (expr instanceof ConstantExpression && ((ConstantExpression) expr).value instanceof Number) {
            return (Number) ((ConstantExpression) expr).value
        }
        if (expr instanceof UnaryMinusExpression) {
            Expression inner = ((UnaryMinusExpression) expr).expression
            if (inner instanceof ConstantExpression && ((ConstantExpression) inner).value instanceof Number) {
                BigDecimal decimal = frameDecimal((Number) ((ConstantExpression) inner).value)
                return decimal == null ? null : decimal.negate()
            }
        }
        return null
    }

    private static BigDecimal frameDecimal(Number number) {
        if (number instanceof BigDecimal) return (BigDecimal) number
        if (number instanceof BigInteger) return new BigDecimal((BigInteger) number)
        if (number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte) {
            return BigDecimal.valueOf(number.longValue())
        }
        if (number instanceof Double || number instanceof Float) {
            double d = number.doubleValue()
            if (Double.isNaN(d) || Double.isInfinite(d)) return null
            return new BigDecimal(number.toString())
        }
        return null
    }

    private static boolean wholeFrameNumber(BigDecimal decimal) {
        return decimal.stripTrailingZeros().scale() <= 0
    }

    private static Object frameMagnitude(BigDecimal decimal) {
        BigDecimal magnitude = decimal.abs()
        try {
            return magnitude.intValueExact()
        } catch (ArithmeticException ignored) {
            try {
                return magnitude.longValueExact()
            } catch (ArithmeticException ignoredLong) {
                return magnitude.stripTrailingZeros()
            }
        }
    }

    private SqlExpr translateLike(MethodCallExpression call, List<Expression> argList, boolean matchBefore, boolean matchAfter) {
        if (1 != argList.size()) {
            reject("`${call.methodAsString}` expects a single argument", call)
            return null
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
            reject('`substring` expects one or two arguments (a 0-based begin index, and an optional exclusive end index)', call)
            return null
        }
        SqlExpr receiver = translateExpr(call.objectExpression)
        List<SqlExpr> functionArgs = [receiver, shiftedIndex(argList[0])]
        if (2 == argList.size()) {
            functionArgs << substringLength(argList[0], argList[1])
        }
        return new SqlFunction('SUBSTRING', functionArgs)
    }

    // Groovy's 0-based begin index becomes SQL's 1-based FROM position.
    // Literals and captured variables stay a single bound parameter; a column
    // index is shifted in SQL so it can still be pushed down.
    private SqlExpr shiftedIndex(Expression beginExpr) {
        SqlExpr translated = translateExpr(beginExpr)
        if (translated instanceof SqlParam) {
            return new SqlParam(plusX(((SqlParam) translated).valueExpr, constX(1)))
        }
        return new SqlBinary('+', translated, new SqlLiteral(1))
    }

    private SqlExpr substringLength(Expression beginExpr, Expression endExpr) {
        SqlExpr begin = translateExpr(beginExpr)
        SqlExpr end = translateExpr(endExpr)
        if (begin instanceof SqlParam && end instanceof SqlParam) {
            return new SqlParam(minusX(((SqlParam) end).valueExpr, ((SqlParam) begin).valueExpr))
        }
        return new SqlBinary('-', end, begin)
    }

    private SqlExpr translateRangeMember(Expression left, RangeExpression range, boolean negated, Expression reported) {
        Expression fromExpr = range.from
        Expression toExpr = range.to
        if (!(fromExpr instanceof ConstantExpression) || !(toExpr instanceof ConstantExpression)) {
            reject('`in` against a range is translated only when both bounds are compile-time constants, ' +
                    'because a descending Groovy range still contains every value between its endpoints. ' +
                    'Write `x >= low && x <= high` when the order is known', reported)
            return null
        }
        Object fromVal = ((ConstantExpression) fromExpr).value
        Object toVal = ((ConstantExpression) toExpr).value
        if (!(fromVal instanceof Comparable) || !(toVal instanceof Comparable)) {
            reject('`in` against a range requires comparable constant bounds', reported)
            return null
        }
        int cmp
        try {
            cmp = (fromVal <=> toVal)
        } catch (RuntimeException ignored) {
            reject('`in` against a range requires bounds that can be compared with each other', reported)
            return null
        }
        boolean ascending = cmp <= 0
        SqlExpr value = translateExpr(left)
        SqlExpr low = new SqlParam(ascending ? fromExpr : toExpr)
        SqlExpr high = new SqlParam(ascending ? toExpr : fromExpr)
        String lowOp = '>='
        String highOp = '<='
        if (!range.inclusive) {
            // the exclusive end is the original `to`, which may be the lower bound of a descending range
            if (ascending) {
                highOp = '<'
            } else {
                lowOp = '>'
            }
        }
        SqlExpr predicate = new SqlBinary('AND',
                new SqlBinary(lowOp, value, low),
                new SqlBinary(highOp, value, high))
        return negated ? new SqlUnary('NOT', predicate) : predicate
    }

    private Expression requireParamValue(Expression expr, String method) {
        SqlExpr translated = translateExpr(expr)
        if (!(translated instanceof SqlParam)) {
            reject("the arguments of `${method}` must be literals or captured variables", expr)
            return null
        }
        return ((SqlParam) translated).valueExpr
    }

    // java.lang.Math.max/min are binary. A table alias named Math is not the class.
    private boolean isMathReceiver(Expression object) {
        if (object instanceof ClassExpression) {
            String name = ((ClassExpression) object).type.name
            return 'java.lang.Math' == name || 'Math' == name
        }
        String text = object.text
        if ('java.lang.Math' == text) return true
        return 'Math' == text && !(text in tableAliases)
    }

    private SqlExpr translateAggregate(MethodCallExpression call, List<Expression> args, boolean lambdaStyle) {
        String method = call.methodAsString
        String functionName = AGGREGATE_FUNCTION_MAP[method]
        if (functionName == null) {
            reject("`${method}` is not a supported aggregate function (supported: ${AGGREGATE_FUNCTION_MAP.keySet()})", call)
            return null
        }
        if ('count' == method && args.empty) {
            return new SqlCountStar()
        }
        if (1 != args.size()) {
            reject("aggregate function `${method}` expects a single argument", call)
            return null
        }
        Expression arg = args[0]
        if (!lambdaStyle) {
            return translatePlainAggregate(call, method, functionName, arg)
        }
        return translateLambdaAggregate(call, method, functionName, arg)
    }

    private SqlExpr translatePlainAggregate(MethodCallExpression call, String method, String functionName, Expression arg) {
        MethodCallExpression distinct = distinctCall(arg)
        if (distinct != null) {
            Expression inner = countDistinctArgument(call, method, distinct)
            if (inner == null) return null
            return new SqlFunction(functionName, [translateExpr(inner)], true)
        }
        return finishAggregate(method, new SqlFunction(functionName, [translateExpr(arg)]))
    }

    private SqlExpr translateLambdaAggregate(MethodCallExpression call, String method, String functionName, Expression arg) {
        boolean countDistinct = false
        ClosureExpression closure
        MethodCallExpression wrapped = distinctCall(arg)
        if (wrapped != null) {
            Expression inner = countDistinctArgument(call, method, wrapped)
            if (inner == null) return null
            if (!(inner instanceof ClosureExpression)) {
                reject("aggregate function `count` expects a lambda argument, e.g. `${groupAlias}.count(distinct(e -> e.salary))`", inner)
                return null
            }
            closure = (ClosureExpression) inner
            countDistinct = true
        } else if (!(arg instanceof ClosureExpression)) {
            reject("aggregate function `${method}` expects a lambda argument, e.g. `${groupAlias}.${method}(e -> e.salary)`", arg)
            return null
        } else {
            closure = (ClosureExpression) arg
        }
        String param = closure.parameters ? closure.parameters[0].name : 'it'
        Expression body = extractSingleExpression(closure)
        if (body == null) return null
        if (!countDistinct) {
            MethodCallExpression bodyDistinct = distinctCall(body)
            if (bodyDistinct != null) {
                body = countDistinctArgument(call, method, bodyDistinct)
                if (body == null) return null
                countDistinct = true
            }
        }
        String previousParam = aggregateLambdaParam
        aggregateLambdaParam = param
        try {
            return finishAggregate(method, new SqlFunction(functionName, [translateExpr(body)], countDistinct))
        } finally {
            aggregateLambdaParam = previousParam
        }
    }

    // count(distinct(expr)) and g.count(distinct(e -> expr)); other aggregates stay plain
    private Expression countDistinctArgument(MethodCallExpression aggregate, String method, MethodCallExpression distinct) {
        if ('count' != method) {
            reject('distinct aggregates are supported only for `count`', aggregate)
            return null
        }
        List<Expression> inner = ((TupleExpression) distinct.arguments).expressions
        if (1 != inner.size()) {
            reject('`count(distinct(...))` expects a single expression', distinct)
            return null
        }
        return inner[0]
    }

    private static MethodCallExpression distinctCall(Expression expr) {
        if (!(expr instanceof MethodCallExpression)) return null
        MethodCallExpression call = (MethodCallExpression) expr
        if (call.implicitThis && 'distinct' == call.methodAsString) return call
        return null
    }

    // Groovy sum treats null as zero, so an all-null group is 0 rather than SQL null
    private static SqlExpr finishAggregate(String method, SqlExpr call) {
        if ('sum' == method) {
            return new SqlFunction('COALESCE', [call, new SqlLiteral(0)])
        }
        return call
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
        reject('aggregate lambdas must consist of a single expression', closure)
        return null
    }

    private String knownAliases() {
        List<String> known = new ArrayList<>(tableAliases)
        if (groupAlias) known << groupAlias
        return known.toString()
    }

    /**
     * Plans {@code expression} for mixed mode. A fully translatable query is
     * returned as {@link MixedPlan#fullQuery}. Otherwise the plan splits at the
     * first clause that does not translate: {@code from}, then each join, then
     * {@code where}, then {@code orderby} and {@code limit} when the query has
     * no {@code groupby}. {@code groupby} stays in memory whenever anything
     * from {@code groupby} onward cannot be translated, so group results keep
     * collection-provider semantics. Set operations are not split.
     */
    @PackageScope
    MixedPlan planMixed(AbstractGinqExpression expression) {
        int before = errorCount()
        recoverUntranslatable = true
        try {
            SqlQueryNode full = translate(expression)
            if (errorCount() != before) {
                return null
            }
            if (full != null) {
                return MixedPlan.complete(full)
            }
            return null
        } catch (Untranslatable untranslatable) {
            if (errorCount() != before) {
                return null
            }
            if (expression instanceof SetOperationExpression) {
                error('mixed mode cannot split a set operation (`union`/`unionall`/`intersect`/`minus`); ' +
                        untranslatable.message, untranslatable.node ?: expression)
                return null
            }
            if (!(expression instanceof GinqExpression)) {
                error(untranslatable.message, untranslatable.node ?: expression)
                return null
            }
            GinqToSqlTranslator splitter = new GinqToSqlTranslator(sourceUnit, providerName, functions)
            splitter.recoverUntranslatable = true
            return splitter.planSplit((GinqExpression) expression)
        } finally {
            recoverUntranslatable = false
        }
    }

    private MixedPlan planSplit(GinqExpression ginq) {
        int before = errorCount()
        SqlQuery query = new SqlQuery()
        FromExpression fromExpression = ginq.fromExpression
        SqlTableSource from
        try {
            from = tableSource(fromExpression)
        } catch (Untranslatable untranslatable) {
            error('mixed mode requires the `from` source to translate to SQL (a string table name or a fully translatable subquery). ' +
                    untranslatable.message, untranslatable.node ?: fromExpression)
            return null
        }
        if (aborted(before) || from == null) {
            return null
        }
        query.from = from
        tableAliases << fromExpression.aliasExpr.text

        int pushedJoins = 0
        List<JoinExpression> joins = ginq.joinExpressionList
        for (int i = 0; i < joins.size(); i++) {
            int aliasSnapshot = tableAliases.size()
            try {
                SqlJoin join = translateJoin(joins[i])
                if (aborted(before) || join == null) {
                    trimAliases(aliasSnapshot)
                    return null
                }
                query.joins << join
                pushedJoins++
            } catch (Untranslatable ignored) {
                trimAliases(aliasSnapshot)
                break
            }
        }
        if (aborted(before)) {
            return null
        }

        boolean wherePushed = false
        if (pushedJoins == joins.size()) {
            if (ginq.whereExpression == null) {
                wherePushed = true
            } else {
                try {
                    SqlExpr where = translateExpr(ginq.whereExpression.filterExpr)
                    if (aborted(before)) {
                        return null
                    }
                    query.where = where
                    wherePushed = true
                } catch (Untranslatable ignored) {
                    query.where = null
                }
            }
        }
        if (aborted(before)) {
            return null
        }

        // orderby/limit apply to source rows only when there is no groupby.
        // A groupby that cannot stay in SQL (because this query did not translate
        // fully) is evaluated in memory, and so are the orderby/limit that follow it.
        boolean orderPushed = false
        boolean limitPushed = false
        if (wherePushed && ginq.groupExpression == null) {
            if (ginq.orderExpression == null) {
                orderPushed = true
            } else {
                int orderSnapshot = query.orderBy.size()
                try {
                    translateOrders(ginq.orderExpression.ordersExpr, query)
                    if (aborted(before)) {
                        return null
                    }
                    orderPushed = true
                } catch (Untranslatable ignored) {
                    while (query.orderBy.size() > orderSnapshot) {
                        query.orderBy.remove(query.orderBy.size() - 1)
                    }
                }
            }
            if (orderPushed) {
                if (ginq.limitExpression == null) {
                    limitPushed = true
                } else {
                    try {
                        translateLimit(ginq.limitExpression.offsetAndSizeExpr, query)
                        if (aborted(before)) {
                            return null
                        }
                        limitPushed = true
                    } catch (Untranslatable ignored) {
                        query.offset = null
                        query.fetch = null
                    }
                }
            }
        }
        if (aborted(before)) {
            return null
        }

        List<String> aliases = [fromExpression.aliasExpr.text]
        for (int i = 0; i < pushedJoins; i++) {
            aliases << joins[i].aliasExpr.text
        }
        for (String alias : aliases) {
            query.projections << new SqlProjection(new SqlStar(alias))
        }

        List<SqlQuery> columnCountQueries = []
        if (aliases.size() > 1) {
            columnCountQueries << countQuery(query.from)
            for (SqlJoin join : query.joins) {
                columnCountQueries << countQuery(join.table)
            }
        }

        GinqExpression tail = new GinqExpression()
        tail.fromExpression = fromExpression
        tail.putNodeMetaData(GinqAstBuilder.ROOT_GINQ_EXPRESSION, tail)
        if (Boolean.TRUE == ginq.getNodeMetaData(GinqAstBuilder.GINQ_SELECT_DISTINCT)) {
            tail.putNodeMetaData(GinqAstBuilder.GINQ_SELECT_DISTINCT, Boolean.TRUE)
        }
        String previousAlias = fromExpression.aliasExpr.text
        for (int i = 0; i < joins.size(); i++) {
            JoinExpression original = joins[i]
            if (i < pushedJoins) {
                JoinExpression zipped = new JoinExpression('join', original.aliasExpr, original.dataSourceExpr)
                zipped.onExpression = new OnExpression(eqX(
                        propX(varX(previousAlias), SqlGinqRuntime.MIXED_KEY),
                        propX(varX(original.aliasExpr.text), SqlGinqRuntime.MIXED_KEY)))
                tail.addJoinExpression(zipped)
            } else {
                tail.addJoinExpression(original)
            }
            previousAlias = original.aliasExpr.text
        }
        if (!wherePushed && ginq.whereExpression != null) {
            tail.whereExpression = ginq.whereExpression
        }
        if (ginq.groupExpression != null) {
            tail.groupExpression = ginq.groupExpression
        }
        if (!orderPushed && ginq.orderExpression != null) {
            tail.orderExpression = ginq.orderExpression
        }
        if (!limitPushed && ginq.limitExpression != null) {
            tail.limitExpression = ginq.limitExpression
        }
        tail.selectExpression = ginq.selectExpression
        return MixedPlan.split(query, columnCountQueries, tail, pushedJoins)
    }

    private static SqlQuery countQuery(SqlTableSource source) {
        SqlQuery query = new SqlQuery()
        query.projections << new SqlProjection(new SqlStar())
        if (source instanceof SqlTableRef) {
            query.from = new SqlTableRef(source.tableName, source.alias)
        } else if (source instanceof SqlDerivedTable) {
            query.from = new SqlDerivedTable(source.query, source.alias)
        } else {
            return null
        }
        query.where = new SqlBinary('=', new SqlLiteral(1), new SqlLiteral(0))
        return query
    }

    private boolean aborted(int before) {
        return errorCount() != before
    }

    private int errorCount() {
        return sourceUnit.errorCollector.errorCount
    }

    private void trimAliases(int size) {
        while (tableAliases.size() > size) {
            tableAliases.remove(tableAliases.size() - 1)
        }
    }

    /**
     * Reports a construct that has no SQL translation. Mixed-mode planning
     * catches {@link Untranslatable}; strict mode records a compile error.
     */
    private void reject(String message, ASTNode node) {
        reject(message, node, true)
    }

    /**
     * @param hintMixedMode append how to enable in-memory evaluation. Omitted
     * while planning mixed mode, and for failures mixed mode cannot repair.
     */
    private void reject(String message, ASTNode node, boolean hintMixedMode) {
        if (recoverUntranslatable) {
            throw new Untranslatable(message, node)
        }
        error(hintMixedMode ? message + MIXED_HINT : message, node)
    }

    @PackageScope
    static String invalidTableNameMessage(String tableName) {
        return "table name `${tableName}` must be a simple identifier or a schema-qualified name such as `hr.employees`. " +
                'A quoting `table()` helper is not supported yet'
    }

    @PackageScope
    static boolean plainTableName(String name) {
        if (!name) return false
        for (String part : name.split('\\.', -1)) {
            if (!simpleIdentifier(part)) return false
        }
        return true
    }

    private static boolean simpleIdentifier(String part) {
        if (!part) return false
        for (int i = 0; i < part.length(); i++) {
            char ch = part.charAt(i)
            // ASCII letter, digit or underscore. Char literals are Strings under static compilation.
            boolean letter = (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || ch == 95
            boolean digit = ch >= 48 && ch <= 57
            if (!(letter || (i > 0 && digit))) return false
        }
        return true
    }

    private void error(String message, ASTNode node) {
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
    private static final String MIXED_HINT =
            '. Rewrite it in supported SQL, or set `mixedMode: true` to evaluate that clause in memory.'
    private static final String WINDOW_DEFERRED_PREFIX =
            'window functions are deferred and are not translated by the '

    // Collects partitionby/orderby/rows/range and any other call in the over argument.
    // Calls nested inside a clause's own arguments are not clauses (e.name.toUpperCase()).
    @CompileStatic
    private static class WindowClauseFinder extends CodeVisitorSupport {
        private final List<MethodCallExpression> partitionBy = []
        private final List<MethodCallExpression> orderBy = []
        private final List<MethodCallExpression> rows = []
        private final List<MethodCallExpression> range = []
        private final List<MethodCallExpression> unknown = []

        @Override
        void visitMethodCallExpression(MethodCallExpression call) {
            super.visitMethodCallExpression(call)
            String name = call.methodAsString
            if ('partitionby' == name) {
                partitionBy.add(call)
            } else if ('orderby' == name) {
                orderBy.add(call)
            } else if ('rows' == name) {
                rows.add(call)
            } else if ('range' == name) {
                range.add(call)
            } else {
                unknown.add(call)
            }
        }

        private void dropNestedArguments() {
            List<Expression> arguments = []
            for (MethodCallExpression call : partitionBy) arguments.add(call.arguments)
            for (MethodCallExpression call : orderBy) arguments.add(call.arguments)
            for (MethodCallExpression call : rows) arguments.add(call.arguments)
            for (MethodCallExpression call : range) arguments.add(call.arguments)
            if (arguments.isEmpty()) return
            new ListExpression(arguments).visit(new CodeVisitorSupport() {
                @Override
                void visitMethodCallExpression(MethodCallExpression call) {
                    drop(partitionBy, call)
                    drop(orderBy, call)
                    drop(rows, call)
                    drop(range, call)
                    drop(unknown, call)
                    super.visitMethodCallExpression(call)
                }
            })
        }

        private static void drop(List<MethodCallExpression> calls, MethodCallExpression target) {
            Iterator<MethodCallExpression> it = calls.iterator()
            while (it.hasNext()) {
                if (it.next().is(target)) it.remove()
            }
        }
    }

    @CompileStatic
    private class CallSite implements SqlFunctionSite {
        @Override
        SqlExpr expr(Expression expression) {
            return translateExpr(expression)
        }

        @Override
        void fail(String message, ASTNode node) {
            reject(message, node)
        }

        @Override
        SqlExpr like(MethodCallExpression call, List<Expression> args, boolean matchBefore, boolean matchAfter) {
            return translateLike(call, args, matchBefore, matchAfter)
        }

        @Override
        SqlExpr substring(MethodCallExpression call, List<Expression> args) {
            return translateSubstring(call, args)
        }
    }
}
