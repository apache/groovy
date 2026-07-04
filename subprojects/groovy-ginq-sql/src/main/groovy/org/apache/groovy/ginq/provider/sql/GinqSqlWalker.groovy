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
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode
import org.apache.groovy.ginq.provider.sql.render.AnsiDialect
import org.apache.groovy.ginq.provider.sql.render.RenderedSql
import org.apache.groovy.ginq.provider.sql.render.SqlRenderer
import org.apache.groovy.ginq.provider.sql.runtime.SqlGinqRuntime
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX

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

        SqlQueryNode queryNode = new GinqToSqlTranslator(sourceUnit).translate(expression)
        RenderedSql renderedSql = renderSql(queryNode, expression)

        MethodCallExpression executeCall = callX(
                classX(SQL_GINQ_RUNTIME_TYPE),
                'execute',
                args(dataSourceExpr, constX(renderedSql.sql), new ListExpression(renderedSql.parameters)))
        executeCall.setSourcePosition(expression)
        return executeCall
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
        if (configuration.containsKey('dialect')) {
            error('`dialect` is only supported by the jooq-sql provider', expression)
        }
    }

    /**
     * Renders the translated query to SQL text plus ordered parameters.
     */
    protected RenderedSql renderSql(SqlQueryNode queryNode, AbstractGinqExpression expression) {
        return new SqlRenderer(new AnsiDialect()).render(queryNode)
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

    protected void error(String message, org.codehaus.groovy.ast.ASTNode node) {
        collectSyntaxError(new GinqSyntaxError(message, node.lineNumber, node.columnNumber))
    }

    private static final ClassNode SQL_GINQ_RUNTIME_TYPE = ClassHelper.makeCached(SqlGinqRuntime)
}
