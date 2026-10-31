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
import org.apache.groovy.ginq.GinqGroovyMethods
import org.apache.groovy.ginq.dsl.GinqAstVisitor
import org.apache.groovy.ginq.dsl.GinqSyntaxError
import org.apache.groovy.ginq.dsl.SyntaxErrorReportable
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.FilterExpression
import org.apache.groovy.ginq.dsl.expression.FromExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.GroupExpression
import org.apache.groovy.ginq.dsl.expression.HavingExpression
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.dsl.expression.LimitExpression
import org.apache.groovy.ginq.dsl.expression.OnExpression
import org.apache.groovy.ginq.dsl.expression.OrderExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression
import org.apache.groovy.ginq.dsl.expression.ShutdownExpression
import org.apache.groovy.ginq.dsl.expression.WhereExpression
import org.apache.groovy.ginq.provider.collection.GinqAstWalker
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.render.AnsiDialect
import org.apache.groovy.ginq.provider.sql.render.Dialect
import org.apache.groovy.ginq.provider.sql.render.Dialects
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.ginq.provider.sql.render.SqlRenderer
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.lambdaX
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * The native SQL provider: transforms a GINQ query into a call executing the
 * equivalent SQL, rendered at compile time with all user values bound as
 * positional parameters, against the configured {@code dataSource}.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class GinqSqlWalker implements GinqAstVisitor<Expression>, SyntaxErrorReportable {
    private final SourceUnit sourceUnit
    private Map<String, String> configuration = [:]
    private Map<String, Expression> configurationExpressions = [:]
    private Dialect dialect = new AnsiDialect()

    GinqSqlWalker(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    @Override
    SourceUnit getSourceUnit() {
        return sourceUnit
    }

    @Override
    Expression visit(AbstractGinqExpression expression) {
        if ('true' == configuration.get(GinqGroovyMethods.CONF_PARALLEL)) {
            error("`parallel` is not supported by the ${providerName} provider", expression)
        }
        if (expression instanceof ShutdownExpression) {
            error("`shutdown` is not supported by the ${providerName} provider", expression)
        }
        Expression dataSourceExpr = configurationExpressions.get(GinqGroovyMethods.CONF_DATA_SOURCE)
        if (dataSourceExpr == null) {
            error("the ${providerName} provider requires a `dataSource`, e.g. GQ(provider: '${providerName}', dataSource: db) {...}; " +
                    'note that `@GQ` does not support `dataSource`', expression)
        }
        validateOptions(expression)
        if (sourceUnit.errorCollector.hasErrors()) {
            return constX(null)
        }

        if (isMixedMode()) {
            return visitMixed(expression, dataSourceExpr)
        }

        SqlQueryNode queryNode = translator().translate(expression)
        if (queryNode == null || sourceUnit.errorCollector.hasErrors()) {
            return constX(null)
        }
        RenderedSql renderedSql = renderSql(queryNode, expression)
        if (renderedSql == null) {
            return constX(null)
        }
        return executeExpression(renderedSql, dataSourceExpr, expression)
    }

    private Expression visitMixed(AbstractGinqExpression expression, Expression dataSourceExpr) {
        int before = sourceUnit.errorCollector.errorCount
        MixedPlan plan = translator().planMixed(expression)
        if (plan == null || sourceUnit.errorCollector.errorCount != before) {
            return constX(null)
        }
        if (plan.fullQuery != null) {
            RenderedSql renderedSql = renderSql(plan.fullQuery, expression)
            if (renderedSql == null) {
                return constX(null)
            }
            return executeExpression(renderedSql, dataSourceExpr, expression)
        }
        return emitMixed(plan, expression, dataSourceExpr)
    }

    private Expression emitMixed(MixedPlan plan, AbstractGinqExpression expression, Expression dataSourceExpr) {
        GinqExpression tail = plan.tail
        if (plan.columnCountQueries.isEmpty()) {
            RenderedSql prefixSql = renderSql(plan.prefix, expression)
            if (prefixSql == null) {
                return constX(null)
            }
            tail.fromExpression.dataSourceExpr = executeExpression(prefixSql, dataSourceExpr, expression)
            materializeQuery(tail)
            if (sourceUnit.errorCollector.hasErrors()) {
                return constX(null)
            }
            return runCollection(tail)
        }

        String rowsName = "__ginqRows${mixedSeq++}"
        VariableExpression rowsVar = varX(rowsName)
        List<Expression> counts = []
        for (SqlQuery countQuery : plan.columnCountQueries) {
            RenderedSql rendered = renderSql(countQuery, expression)
            if (rendered == null) {
                return constX(null)
            }
            counts << callX(classX(SQL_GINQ_RUNTIME_TYPE), 'columnCount',
                    args(dataSourceExpr, constX(rendered.sql), new ListExpression(rendered.parameters)))
        }
        RenderedSql prefixSql = renderSql(plan.prefix, expression)
        if (prefixSql == null) {
            return constX(null)
        }
        Expression zip = callX(classX(SQL_GINQ_RUNTIME_TYPE), 'zipAliases', args(
                dataSourceExpr,
                constX(prefixSql.sql),
                new ListExpression(prefixSql.parameters),
                new ListExpression(counts)))
        tail.fromExpression.dataSourceExpr = callX(rowsVar, 'get', constX(0))
        for (int i = 0; i < plan.pushedJoins; i++) {
            tail.joinExpressionList[i].dataSourceExpr = callX(rowsVar, 'get', constX(i + 1))
        }
        materializeQuery(tail)
        if (sourceUnit.errorCollector.hasErrors()) {
            return constX(null)
        }
        Expression tailExec = runCollection(tail)
        Expression wrapper = callX(lambdaX(params(), block(
                declS(localVarX(rowsName), zip),
                returnS(tailExec)
        )), 'call')
        wrapper.setSourcePosition(expression)
        return wrapper
    }

    private Expression runCollection(GinqExpression tail) {
        GinqAstWalker walker = new GinqAstWalker(sourceUnit)
        walker.configuration = Collections.<String, String>emptyMap()
        walker.configurationExpressions = Collections.<String, Expression>emptyMap()
        return (Expression) walker.visit(tail)
    }

    private MethodCallExpression executeExpression(RenderedSql renderedSql, Expression dataSourceExpr, AbstractGinqExpression expression) {
        MethodCallExpression executeCall = callX(
                classX(SQL_GINQ_RUNTIME_TYPE),
                'execute',
                args(dataSourceExpr, constX(renderedSql.sql), new ListExpression(renderedSql.parameters)))
        executeCall.setSourcePosition(expression)
        return executeCall
    }

    private boolean isMixedMode() {
        return 'true' == configuration.get(NativeSqlGinqProvider.CONF_MIXED_MODE)
    }

    /**
     * Returns the provider name used in diagnostics.
     */
    protected String getProviderName() {
        return 'native-sql'
    }

    /**
     * Validates provider-specific options.
     */
    protected void validateOptions(AbstractGinqExpression expression) {
        String dialectName = configuration.get(NativeSqlGinqProvider.CONF_DIALECT)
        if (dialectName != null) {
            Dialect resolved = Dialects.resolve(dialectName)
            if (resolved == null) {
                error("`${dialectName}` is not a native SQL dialect (supported: ${Dialects.SUPPORTED}). " +
                        "For other databases use `provider: 'jooq-sql'` with a jOOQ SQLDialect name.", expression)
            } else {
                dialect = resolved
            }
        }
        validateMixedMode(expression)
    }

    /**
     * Checks the shared {@code mixedMode} option. Absent means strict mode.
     */
    protected void validateMixedMode(AbstractGinqExpression expression) {
        String mixedMode = configuration.get(NativeSqlGinqProvider.CONF_MIXED_MODE)
        if (mixedMode != null && !('true' == mixedMode || 'false' == mixedMode)) {
            error('`mixedMode` must be `true` or `false`', expression)
        }
    }

    /**
     * Renders the translated query to SQL text plus ordered parameters.
     */
    protected RenderedSql renderSql(SqlQueryNode queryNode, AbstractGinqExpression expression) {
        try {
            return new SqlRenderer(dialect).render(queryNode)
        } catch (IllegalArgumentException e) {
            error(e.message, expression)
            return null
        }
    }

    private GinqToSqlTranslator translator() {
        return new GinqToSqlTranslator(sourceUnit, providerName)
    }

    @Override
    Expression visitGinqExpression(GinqExpression ginqExpression) {
        return visit(ginqExpression)
    }

    @Override
    Expression visitSetOperationExpression(SetOperationExpression setOperationExpression) {
        return visit(setOperationExpression)
    }

    @Override
    Expression visitShutdownExpression(ShutdownExpression shutdownExpression) {
        return visit(shutdownExpression)
    }

    @Override
    Expression visitFromExpression(FromExpression fromExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitJoinExpression(JoinExpression joinExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitOnExpression(OnExpression onExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitWhereExpression(WhereExpression whereExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitGroupExpression(GroupExpression groupExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitHavingExpression(HavingExpression havingExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitOrderExpression(OrderExpression orderExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitLimitExpression(LimitExpression limitExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    Expression visitSelectExpression(SelectExpression selectExpression) {
        throw new GroovyBugError('clauses are translated by GinqToSqlTranslator')
    }

    @Override
    void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration
    }

    @Override
    Map<String, String> getConfiguration() {
        return configuration
    }

    @Override
    void setConfigurationExpressions(Map<String, Expression> configurationExpressions) {
        this.configurationExpressions = configurationExpressions
    }

    @Override
    Map<String, Expression> getConfigurationExpressions() {
        return configurationExpressions
    }

    protected void error(String message, ASTNode node) {
        collectSyntaxError(new GinqSyntaxError(message, node.lineNumber, node.columnNumber))
    }

    /**
     * Replaces string table names that the in-memory tail would otherwise
     * iterate as character sequences. Sources already rewritten to the SQL
     * prefix result are left alone. Nested queries stay GINQ so the collection
     * provider can correlate them, but their tables are loaded with {@code SELECT *}.
     * Those names use the same plain-identifier check and qualified-name
     * rendering as a translated {@code from} clause.
     */
    private void materializeQuery(GinqExpression ginqExpression) {
        materializeSource(ginqExpression.fromExpression)
        if (sourceUnit.errorCollector.hasErrors()) {
            return
        }
        for (JoinExpression joinExpression : ginqExpression.joinExpressionList) {
            materializeSource(joinExpression)
            if (sourceUnit.errorCollector.hasErrors()) {
                return
            }
            if (joinExpression.onExpression != null) {
                joinExpression.onExpression.filterExpr = materialize(joinExpression.onExpression.filterExpr)
            }
        }
        materializeFilter(ginqExpression.whereExpression)
        GroupExpression groupExpression = ginqExpression.groupExpression
        if (groupExpression != null) {
            materializeTuple(groupExpression.classifierExpr)
            materializeFilter(groupExpression.havingExpression)
        }
        if (ginqExpression.orderExpression != null) {
            materializeTuple(ginqExpression.orderExpression.ordersExpr)
        }
        if (ginqExpression.limitExpression != null) {
            materializeTuple(ginqExpression.limitExpression.offsetAndSizeExpr)
        }
        materializeTuple(ginqExpression.selectExpression.projectionExpr)
    }

    private void materializeSet(SetOperationExpression setOperation) {
        AbstractGinqExpression left = setOperation.left
        if (left instanceof GinqExpression) {
            materializeQuery((GinqExpression) left)
        } else if (left instanceof SetOperationExpression) {
            materializeSet((SetOperationExpression) left)
        }
        materializeQuery(setOperation.right)
    }

    private void materializeFilter(FilterExpression filter) {
        if (filter != null) {
            filter.filterExpr = materialize(filter.filterExpr)
        }
    }

    private void materializeSource(DataSourceExpression source) {
        Expression dataSource = source.dataSourceExpr
        if (dataSource instanceof ConstantExpression && dataSource.value instanceof String) {
            source.dataSourceExpr = selectAll(source, (String) dataSource.value)
        } else if (dataSource instanceof GinqExpression) {
            materializeQuery((GinqExpression) dataSource)
        } else if (dataSource instanceof SetOperationExpression) {
            materializeSet((SetOperationExpression) dataSource)
        }
    }

    private void materializeTuple(Expression expr) {
        if (expr instanceof TupleExpression) {
            List<Expression> expressions = ((TupleExpression) expr).expressions
            for (int i = 0; i < expressions.size(); i++) {
                expressions.set(i, materialize(expressions.get(i)))
            }
        }
    }

    private Expression materialize(Expression expr) {
        if (expr == null) {
            return null
        }
        return ((ListExpression) new ListExpression(Collections.singletonList(expr)).transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                if (expression instanceof GinqExpression) {
                    materializeQuery((GinqExpression) expression)
                    return expression
                }
                if (expression instanceof SetOperationExpression) {
                    materializeSet((SetOperationExpression) expression)
                    return expression
                }
                if (expression instanceof ClosureExpression) {
                    materializeStatement(((ClosureExpression) expression).code)
                    return expression
                }
                return expression.transformExpression(this)
            }
        })).getExpression(0)
    }

    private void materializeStatement(Statement statement) {
        if (statement instanceof BlockStatement) {
            for (Statement child : ((BlockStatement) statement).statements) {
                materializeStatement(child)
            }
        } else if (statement instanceof ExpressionStatement) {
            ((ExpressionStatement) statement).expression = materialize(((ExpressionStatement) statement).expression)
        } else if (statement instanceof ReturnStatement) {
            ((ReturnStatement) statement).expression = materialize(((ReturnStatement) statement).expression)
        }
    }

    private Expression selectAll(DataSourceExpression source, String tableName) {
        if (!GinqToSqlTranslator.plainTableName(tableName)) {
            error(GinqToSqlTranslator.invalidTableNameMessage(tableName), source)
            return constX(null)
        }
        Expression dataSourceExpr = configurationExpressions.get(GinqGroovyMethods.CONF_DATA_SOURCE)
        return callX(classX(SQL_GINQ_RUNTIME_TYPE), 'execute',
                args(dataSourceExpr, constX('SELECT * FROM ' + dialect.qualifiedName(tableName)), new ListExpression()))
    }

    private int mixedSeq

    private static final ClassNode SQL_GINQ_RUNTIME_TYPE = ClassHelper.makeCached(SqlGinqRuntime)
}
