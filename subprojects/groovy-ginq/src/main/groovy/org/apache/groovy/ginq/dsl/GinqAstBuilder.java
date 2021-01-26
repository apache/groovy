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
import org.apache.groovy.ginq.dsl.expression.ShutdownExpression;
import org.apache.groovy.ginq.dsl.expression.WhereExpression;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Build the AST for GINQ
 *
 * @since 4.0.0
 */
public class GinqAstBuilder extends CodeVisitorSupport implements SyntaxErrorReportable {
    public static final String ROOT_GINQ_EXPRESSION = "__ROOT_GINQ_EXPRESSION";
    private final Deque<GinqExpression> ginqExpressionStack = new ArrayDeque<>();
    private GinqExpression latestGinqExpression;
    private final SourceUnit sourceUnit;

    public GinqAstBuilder(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    private final List<MethodCallExpression> ignoredMethodCallExpressionList = new ArrayList<>();

    private static final List<String> SHUTDOWN_OPTION_LIST = Arrays.asList("immediate", "abort");
    public AbstractGinqExpression buildAST(ASTNode astNode) {
        if (astNode instanceof BlockStatement) {
            List<Statement> statementList = ((BlockStatement) astNode).getStatements();
            if (1 == statementList.size()) {
                Statement statement = statementList.get(0);
                if (statement instanceof ExpressionStatement) {
                    Expression expression = ((ExpressionStatement) statement).getExpression();
                    if (expression instanceof MethodCallExpression && KW_SHUTDOWN.equals(((MethodCallExpression) expression).getMethodAsString())) {
                        List<Expression> argExpressionList = ((ArgumentListExpression) ((MethodCallExpression) expression).getArguments()).getExpressions();
                        if (1 == argExpressionList.size()) {
                            Expression argExpression = argExpressionList.get(0);
                            if (argExpression instanceof VariableExpression) {
                                int mode = SHUTDOWN_OPTION_LIST.indexOf(argExpression.getText());
                                if (-1 == mode) {
                                    this.collectSyntaxError(new GinqSyntaxError("Invalid option: " + argExpression.getText() + ". (supported options: " + SHUTDOWN_OPTION_LIST + ")",
                                            argExpression.getLineNumber(), argExpression.getColumnNumber()));
                                }
                                return new ShutdownExpression(expression, mode);
                            }
                        }
                    } else if (expression instanceof VariableExpression && KW_SHUTDOWN.equals(expression.getText())) {
                        return new ShutdownExpression(expression, SHUTDOWN_OPTION_LIST.indexOf("immediate"));
                    }
                }
            }
        }

        astNode.visit(this);
        return getGinqExpression(astNode);
    }

    private GinqExpression getGinqExpression(ASTNode astNode) {
        if (null == latestGinqExpression) {
            ASTNode node = ginqExpressionStack.isEmpty() ? astNode : ginqExpressionStack.peek();
            this.collectSyntaxError(new GinqSyntaxError("`select` clause is missing",
                    node.getLineNumber(), node.getColumnNumber()));
        }

        latestGinqExpression.visit(new GinqAstBaseVisitor() {
            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                ignoredMethodCallExpressionList.remove(call);
                super.visitMethodCallExpression(call);
            }
        });

        if (!ignoredMethodCallExpressionList.isEmpty()) {
            MethodCallExpression methodCallExpression = ignoredMethodCallExpressionList.get(0);
            this.collectSyntaxError(new GinqSyntaxError("Unknown clause: " + methodCallExpression.getMethodAsString(),
                    methodCallExpression.getLineNumber(), methodCallExpression.getColumnNumber()));
        }

        latestGinqExpression.putNodeMetaData(ROOT_GINQ_EXPRESSION, latestGinqExpression);

        return latestGinqExpression;
    }

    private void setLatestGinqExpressionClause(AbstractGinqExpression ginqExpressionClause) {
        GinqExpression ginqExpression = ginqExpressionStack.peek();
        ginqExpression.putNodeMetaData(__LATEST_GINQ_EXPRESSION_CLAUSE, ginqExpressionClause);
    }

    private AbstractGinqExpression getLatestGinqExpressionClause(MethodCallExpression call) {
        GinqExpression ginqExpression = ginqExpressionStack.peek();
        if (null == ginqExpression) {
            this.collectSyntaxError(new GinqSyntaxError("One `from` is expected and must be the first clause",
                    call.getLineNumber(), call.getColumnNumber()));
        }
        return ginqExpression.getNodeMetaData(__LATEST_GINQ_EXPRESSION_CLAUSE);
    }

    private boolean visitingOverClause;

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        final String methodName = call.getMethodAsString();
        if ("over".equals(methodName)) {
            visitingOverClause = true;
        }
        super.visitMethodCallExpression(call);
        if ("over".equals(methodName)) {
            visitingOverClause = false;
        }

        if (!KEYWORD_SET.contains(methodName)) {
            ignoredMethodCallExpressionList.add(call);
            return;
        }

        if (KW_FROM.equals(methodName)) {
            final GinqExpression ginqExpression = new GinqExpression();
            ginqExpression.setSourcePosition(call);
            ginqExpressionStack.push(ginqExpression); // store the result
        }

        GinqExpression currentGinqExpression = ginqExpressionStack.peek();
        AbstractGinqExpression latestGinqExpressionClause = getLatestGinqExpressionClause(call);

        if (KW_FROM.equals(methodName)  || JoinExpression.isJoinExpression(methodName)) {
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
            Expression dataSourceExpr = binaryExpression.getRightExpression();

            DataSourceExpression dataSourceExpression;
            if (KW_FROM.equals(methodName)) {
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

        if (KW_WHERE.equals(methodName) || KW_ON.equals(methodName) || KW_HAVING.equals(methodName)) {
            Expression filterExpr = ((ArgumentListExpression) call.getArguments()).getExpression(0);

            FilterExpression filterExpression;
            if (KW_WHERE.equals(methodName)) {
                filterExpression = new WhereExpression(filterExpr);
            } else if (KW_ON.equals(methodName)) {
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
                        "The preceding clause of `" + methodName + "` should be " + (KW_ON.equals(methodName) ? "" : "`from`/") + "join clause",
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

        if (KW_EXISTS.equals(methodName)) {
            if (null != latestGinqExpression) {
                ArgumentListExpression argumentListExpression = (ArgumentListExpression) call.getArguments();
                if (argumentListExpression.getExpressions().isEmpty() && isSelectMethodCallExpression(call.getObjectExpression())) {
                    call.setObjectExpression(latestGinqExpression);
                    // use the nested ginq and clear it
                    latestGinqExpression = null;
                }
            }
        }

        if (KW_GROUPBY.equals(methodName)) {
            GroupExpression groupExpression = new GroupExpression(call.getArguments());
            groupExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setGroupExpression(groupExpression);

            if (latestGinqExpressionClause instanceof OrderExpression) {
                this.collectSyntaxError(new GinqSyntaxError(
                        "The clause `" + methodName + "` should be in front of `orderby`",
                        call.getLineNumber(), call.getColumnNumber()
                ));
            } else if (latestGinqExpressionClause instanceof LimitExpression) {
                this.collectSyntaxError(new GinqSyntaxError(
                        "The clause `" + methodName + "` should be in front of `limit`",
                        call.getLineNumber(), call.getColumnNumber()
                ));
            }

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                groupExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(groupExpression);

            return;
        }

        if (KW_ORDERBY.equals(methodName) && !visitingOverClause) {
            OrderExpression orderExpression = new OrderExpression(call.getArguments());
            orderExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setOrderExpression(orderExpression);

            if (latestGinqExpressionClause instanceof LimitExpression) {
                this.collectSyntaxError(new GinqSyntaxError(
                        "The clause `" + methodName + "` should be in front of `limit`",
                        call.getLineNumber(), call.getColumnNumber()
                ));
            }

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                orderExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(orderExpression);

            return;
        }

        if (KW_LIMIT.equals(methodName)) {
            LimitExpression limitExpression = new LimitExpression(call.getArguments());
            limitExpression.setSourcePosition(call.getMethod());

            currentGinqExpression.setLimitExpression(limitExpression);

            if (latestGinqExpressionClause instanceof DataSourceHolder) {
                limitExpression.setDataSourceExpression(((DataSourceHolder) latestGinqExpressionClause).getDataSourceExpression());
            }
            setLatestGinqExpressionClause(limitExpression);

            return;
        }

        if (KW_SELECT.equals(methodName)) {
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

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);

        final int opType = expression.getOperation().getType();
        if (opType == Types.KEYWORD_IN || opType == Types.COMPARE_NOT_IN) {
            if (null != latestGinqExpression && isSelectMethodCallExpression(expression.getRightExpression())) {
                // use the nested ginq and clear it
                expression.setRightExpression(latestGinqExpression);
                latestGinqExpression = null;
            }
        }
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (KEYWORD_SET.contains(expression.getText())) {
            this.collectSyntaxError(
                    new GinqSyntaxError(
                            "Invalid syntax found in `" + expression.getText() + "' clause",
                            expression.getLineNumber(), expression.getColumnNumber()
                    )
            );
        }

        super.visitVariableExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        final String typeName = expression.getLeftExpression().getType().getNameWithoutPackage();
        if (KEYWORD_SET.contains(typeName)) {
            this.collectSyntaxError(
                    new GinqSyntaxError(
                            "`" + typeName + "` clause cannot contain assignment expression",
                            expression.getLineNumber(), expression.getColumnNumber()
                    )
            );
        }
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        super.visitCastExpression(expression);

        if (null != latestGinqExpression && isSelectMethodCallExpression(expression.getExpression())) {
            // use the nested ginq and clear it
            expression.setExpression(latestGinqExpression);
            latestGinqExpression = null;
        }
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        List<Expression> list = expression.getExpressions();
        if (list != null) {
            for (int i = 0, n = list.size(); i < n; i++) {
                Expression expr = list.get(i);
                expr.visit(this);

                if (null != latestGinqExpression && isSelectMethodCallExpression(expr)) {
                    // use the nested ginq and clear it
                    list.set(i, latestGinqExpression);
                    latestGinqExpression = null;
                }
            }
        }
    }

    private static boolean isSelectMethodCallExpression(Expression expression) {
        return expression instanceof MethodCallExpression && KW_SELECT.equals(((MethodCallExpression) expression).getMethodAsString());
    }

    @Override
    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    private static final String __LATEST_GINQ_EXPRESSION_CLAUSE = "__latestGinqExpressionClause";

    private static final String KW_FROM = "from";
    private static final String KW_WHERE = "where";
    private static final String KW_ON = "on";
    private static final String KW_HAVING = "having";
    private static final String KW_EXISTS = "exists";
    private static final String KW_GROUPBY = "groupby";
    private static final String KW_ORDERBY = "orderby";
    private static final String KW_LIMIT = "limit";
    private static final String KW_SELECT = "select";
    private static final String KW_SHUTDOWN = "shutdown";
    private static final Set<String> KEYWORD_SET = new HashSet<>();
    static {
        KEYWORD_SET.addAll(Arrays.asList(KW_FROM, KW_WHERE, KW_ON, KW_HAVING, KW_EXISTS, KW_GROUPBY, KW_ORDERBY, KW_LIMIT, KW_SELECT, KW_SHUTDOWN));
        KEYWORD_SET.addAll(JoinExpression.JOIN_NAME_LIST);
    }
}
