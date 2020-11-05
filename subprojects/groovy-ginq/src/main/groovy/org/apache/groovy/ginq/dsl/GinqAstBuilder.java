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
package org.apache.groovy.ginq.dsl;

import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression;
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression;
import org.apache.groovy.ginq.dsl.expression.DataSourceHolder;
import org.apache.groovy.ginq.dsl.expression.FilterExpression;
import org.apache.groovy.ginq.dsl.expression.FromExpression;
import org.apache.groovy.ginq.dsl.expression.GinqExpression;
import org.apache.groovy.ginq.dsl.expression.GroupExpression;
import org.apache.groovy.ginq.dsl.expression.HavingExpression;
import org.apache.groovy.ginq.dsl.expression.JoinExpression;
import org.apache.groovy.ginq.dsl.expression.LimitExpression;
import org.apache.groovy.ginq.dsl.expression.OnExpression;
import org.apache.groovy.ginq.dsl.expression.OrderExpression;
import org.apache.groovy.ginq.dsl.expression.SelectExpression;
import org.apache.groovy.ginq.dsl.expression.WhereExpression;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

/**
 * Build the AST for GINQ
 *
 * @since 4.0.0
 */
public class GinqAstBuilder extends CodeVisitorSupport implements SyntaxErrorReportable {
    private final Deque<GinqExpression> ginqExpressionStack = new ArrayDeque<>();
    private GinqExpression latestGinqExpression;
    private final SourceUnit sourceUnit;

    public GinqAstBuilder(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    public GinqExpression getGinqExpression() {
        return latestGinqExpression;
    }

    private void setLatestGinqExpressionClause(AbstractGinqExpression ginqExpressionClause) {
        GinqExpression ginqExpression = ginqExpressionStack.peek();
        ginqExpression.putNodeMetaData(__LATEST_GINQ_EXPRESSION_CLAUSE, ginqExpressionClause);
    }

    private AbstractGinqExpression getLatestGinqExpressionClause() {
        GinqExpression ginqExpression = ginqExpressionStack.peek();
        if (null == ginqExpression) {
            this.collectSyntaxError(new GinqSyntaxError("`from` clause is missing", -1, -1));
        }
        return ginqExpression.getNodeMetaData(__LATEST_GINQ_EXPRESSION_CLAUSE);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);
        final String methodName = call.getMethodAsString();

        if ("from".equals(methodName)) {
            ginqExpressionStack.push(new GinqExpression()); // store the result
        }

        GinqExpression currentGinqExpression = ginqExpressionStack.peek();
        AbstractGinqExpression latestGinqExpressionClause = getLatestGinqExpressionClause();

        if ("from".equals(methodName)  || JoinExpression.isJoinExpression(methodName)) {
            ArgumentListExpression arguments = (ArgumentListExpression) call.getArguments();
            if (arguments.getExpressions().size() != 1) {
                this.collectSyntaxError(
                        new GinqSyntaxError(
                                "Only 1 argument expected for `" + methodName + "`, e.g. `" + methodName + " n in nums`",
                                call.getLineNumber(), call.getColumnNumber()
                        )
                );
            }
            final Expression expression = arguments.getExpression(0);
            if (!(expression instanceof BinaryExpression
                    && ((BinaryExpression) expression).getOperation().getType() == Types.KEYWORD_IN)) {
                this.collectSyntaxError(
                        new GinqSyntaxError(
                                "`in` is expected for `" + methodName + "`, e.g. `" + methodName + " n in nums`",
                                call.getLineNumber(), call.getColumnNumber()
                        )
                );
            }
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            Expression aliasExpr = binaryExpression.getLeftExpression();
            Expression dataSourceExpr;
            if (null == latestGinqExpression) {
                dataSourceExpr = binaryExpression.getRightExpression();
            } else {
                // use the nested ginq expresion and clear it
                dataSourceExpr = latestGinqExpression;
                latestGinqExpression = null;
            }

            DataSourceExpression dataSourceExpression;
            if ("from".equals(methodName)) {
                dataSourceExpression = new FromExpression(aliasExpr, dataSourceExpr);
                currentGinqExpression.setFromExpression((FromExpression) dataSourceExpression);
            } else {
                dataSourceExpression = new JoinExpression(methodName, aliasExpr, dataSourceExpr);
                currentGinqExpression.addJoinExpression((JoinExpression) dataSourceExpression);
            }
            dataSourceExpression.setSourcePosition(call.getMethod());
            setLatestGinqExpressionClause(dataSourceExpression);

            return;
        }

        if ("where".equals(methodName) || "on".equals(methodName) || "having".equals(methodName)) {
            Expression filterExpr = ((ArgumentListExpression) call.getArguments()).getExpression(0);

            // construct `ListExpression` instance to visit `filterExpr` as well
            new ListExpression(Collections.singletonList(filterExpr)).transformExpression(new ExpressionTransformer() {
                @Override
                public Expression transform(Expression expression) {
                    if (isSelectMethodCallExpression(expression)) {
                        return expression;
                    }

                    if (expression instanceof BinaryExpression) {
                        final BinaryExpression binaryExpression = (BinaryExpression) expression;
                        if (binaryExpression.getOperation().getType() == Types.KEYWORD_IN) {
                            if (null != latestGinqExpression && isSelectMethodCallExpression(binaryExpression.getRightExpression())) {
                                // use the nested ginq and clear it
                                binaryExpression.setRightExpression(latestGinqExpression);
                                latestGinqExpression = null;
                                return binaryExpression;
                            }
                        }
                    }

                    return expression.transformExpression(this);
                }
            });

            FilterExpression filterExpression;
            if ("where".equals(methodName)) {
                filterExpression = new WhereExpression(filterExpr);
            } else if ("on".equals(methodName)) {
                filterExpression = new OnExpression(filterExpr);
            } else {
                filterExpression = new HavingExpression(filterExpr);
            }

            filterExpression.setSourcePosition(call.getMethod());

            if (latestGinqExpressionClause instanceof JoinExpression && filterExpression instanceof OnExpression) {
                ((JoinExpression) latestGinqExpressionClause).setOnExpression((OnExpression) filterExpression);
            } else if (latestGinqExpressionClause instanceof DataSourceHolder && filterExpression instanceof WhereExpression) {
                if (null != currentGinqExpression.getGroupExpression() || null != currentGinqExpression.getOrderExpression() || null != currentGinqExpression.getLimitExpression()) {
                    this.collectSyntaxError(new GinqSyntaxError(
                            "The preceding clause of `" + methodName + "` should be `from`/" + "join clause",
                            call.getLineNumber(), call.getColumnNumber()
                    ));
                }
                currentGinqExpression.setWhereExpression((WhereExpression) filterExpression);
            } else if (latestGinqExpressionClause instanceof GroupExpression && filterExpression instanceof HavingExpression) {
                ((GroupExpression) latestGinqExpressionClause).setHavingExpression((HavingExpression) filterExpression);
            } else {
                this.collectSyntaxError(new GinqSyntaxError(
                        "The preceding clause of `" + methodName + "` should be " + ("on".equals(methodName) ? "" : "`from`/") + "join clause",
                        call.getLineNumber(), call.getColumnNumber()
                ));
            }

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                if (latestGinqExpressionClause instanceof DataSourceExpression) {
                    filterExpression.setDataSourceExpression(((DataSourceExpression) latestGinqExpressionClause));
                } else {
                    filterExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
                }
            }
            setLatestGinqExpressionClause(filterExpression);

            return;
        }

        if ("groupby".equals(methodName)) {
            GroupExpression groupExpression = new GroupExpression(call.getArguments());
            groupExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setGroupExpression(groupExpression);

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                groupExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(groupExpression);

            return;
        }

        if ("orderby".equals(methodName)) {
            OrderExpression orderExpression = new OrderExpression(call.getArguments());
            orderExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setOrderExpression(orderExpression);

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                orderExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(orderExpression);

            return;
        }

        if ("limit".equals(methodName)) {
            LimitExpression limitExpression = new LimitExpression(call.getArguments());
            limitExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setLimitExpression(limitExpression);

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                limitExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(limitExpression);

            return;
        }

        if ("select".equals(methodName)) {
            SelectExpression selectExpression = new SelectExpression(call.getArguments());
            selectExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setSelectExpression(selectExpression);

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                selectExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(selectExpression);

            latestGinqExpression = ginqExpressionStack.pop();
            latestGinqExpression.setSourcePosition(call);

            return;
        }
    }

    private static boolean isSelectMethodCallExpression(Expression expression) {
        return expression instanceof MethodCallExpression && "select".equals(((MethodCallExpression) expression).getMethodAsString());
    }

    @Override
    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    private static final String __LATEST_GINQ_EXPRESSION_CLAUSE = "__latestGinqExpressionClause";
}
