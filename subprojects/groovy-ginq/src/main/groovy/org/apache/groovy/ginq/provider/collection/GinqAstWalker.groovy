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
package org.apache.groovy.ginq.provider.collection

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.GinqGroovyMethods
import org.apache.groovy.ginq.dsl.GinqAstBaseVisitor
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.GinqAstVisitor
import org.apache.groovy.ginq.dsl.GinqSyntaxError
import org.apache.groovy.ginq.dsl.SyntaxErrorReportable
import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.FromExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.GroupExpression
import org.apache.groovy.ginq.dsl.expression.HavingExpression
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.dsl.expression.LimitExpression
import org.apache.groovy.ginq.dsl.expression.OnExpression
import org.apache.groovy.ginq.dsl.expression.OrderExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.ShutdownExpression
import org.apache.groovy.ginq.dsl.expression.WhereExpression
import org.apache.groovy.ginq.provider.collection.runtime.NamedRecord
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
import org.apache.groovy.ginq.provider.collection.runtime.RowBound
import org.apache.groovy.ginq.provider.collection.runtime.ValueBound
import org.apache.groovy.ginq.provider.collection.runtime.WindowDefinition
import org.apache.groovy.util.Maps
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Types
import org.objectweb.asm.Opcodes

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.stream.Collectors

import static groovy.lang.Tuple.tuple
import static org.apache.groovy.ginq.dsl.GinqAstBuilder.GINQ_SELECT_DISTINCT
import static org.codehaus.groovy.ast.ClassHelper.dynamicType
import static org.codehaus.groovy.ast.ClassHelper.makeCached
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.declX
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX
import static org.codehaus.groovy.ast.tools.GeneralUtils.lambdaX
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Visit AST of GINQ to generate target method calls for GINQ.
 *
 * @since 4.0.0
 */
@CompileStatic
class GinqAstWalker implements GinqAstVisitor<Expression>, SyntaxErrorReportable {

    GinqAstWalker(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    private GinqExpression getCurrentGinqExpression() {
        ginqExpressionStack.peek()
    }

    @Override
    MethodCallExpression visitGinqExpression(GinqExpression ginqExpression) {
        if (!ginqExpression) {
            throw new GroovyBugError("`ginqExpression` should not be null") // should never happen!
        }

        ginqExpressionStack.push(ginqExpression)

        DataSourceExpression resultDataSourceExpression
        MethodCallExpression resultMethodCallReceiver

        FromExpression fromExpression = currentGinqExpression.fromExpression
        resultDataSourceExpression = fromExpression
        MethodCallExpression fromMethodCallExpression = visitFromExpression(fromExpression)
        resultMethodCallReceiver = fromMethodCallExpression

        for (JoinExpression joinExpression : currentGinqExpression.joinExpressionList) {
            joinExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            joinExpression.dataSourceExpression = resultDataSourceExpression

            resultDataSourceExpression = joinExpression
            resultMethodCallReceiver = visitJoinExpression(joinExpression)
        }

        WhereExpression whereExpression = currentGinqExpression.whereExpression
        if (whereExpression) {
            whereExpression.dataSourceExpression = resultDataSourceExpression
            whereExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            MethodCallExpression whereMethodCallExpression = visitWhereExpression(whereExpression)
            resultMethodCallReceiver = whereMethodCallExpression
        }

        addDummyGroupExpressionIfNecessary()
        GroupExpression groupExpression = currentGinqExpression.groupExpression
        if (groupExpression) {
            groupExpression.dataSourceExpression = resultDataSourceExpression
            groupExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            MethodCallExpression groupMethodCallExpression = visitGroupExpression(groupExpression)
            resultMethodCallReceiver = groupMethodCallExpression
        }

        OrderExpression orderExpression = currentGinqExpression.orderExpression
        if (orderExpression) {
            orderExpression.dataSourceExpression = resultDataSourceExpression
            orderExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            MethodCallExpression orderMethodCallExpression = visitOrderExpression(orderExpression)
            resultMethodCallReceiver = orderMethodCallExpression
        }

        LimitExpression limitExpression = currentGinqExpression.limitExpression
        if (limitExpression) {
            limitExpression.dataSourceExpression = resultDataSourceExpression
            limitExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            MethodCallExpression limitMethodCallExpression = visitLimitExpression(limitExpression)
            resultMethodCallReceiver = limitMethodCallExpression
        }

        SelectExpression selectExpression = currentGinqExpression.selectExpression
        selectExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
        selectExpression.dataSourceExpression = resultDataSourceExpression
        MethodCallExpression selectMethodCallExpression = visitSelectExpression(selectExpression)

        List<Statement> statementList = []
        boolean isRootGinqExpression = ginqExpression === ginqExpression.getNodeMetaData(GinqAstBuilder.ROOT_GINQ_EXPRESSION)
        boolean useWindowFunction = isRootGinqExpression && isUseWindowFunction(ginqExpression)
        if (useWindowFunction) {
            statementList << stmt(callX(QUERYABLE_HELPER_TYPE, 'setVar', args(constX(USE_WINDOW_FUNCTION), constX(TRUE_STR))))
        }

        boolean parallelEnabled = isRootGinqExpression && isParallel()
        if (parallelEnabled) {
            statementList << stmt(callX(QUERYABLE_HELPER_TYPE, 'setVar', args(constX(PARALLEL), constX(TRUE_STR))))
        }

        statementList << declS(
                localVarX(metaDataMapName).tap{ modifiers |= Opcodes.ACC_FINAL },
                callX(MAPS_TYPE, "of", args(
                        constX(MD_ALIAS_NAME_LIST), aliasNameListExpression,
                        constX(MD_GROUP_NAME_LIST), groupNameListExpression,
                        constX(MD_SELECT_NAME_LIST), selectNameListExpression
                ))
        )
        if (rowNumberUsed) {
            statementList << declS(localVarX(rowNumberName), ctorX(ATOMIC_LONG_TYPE, constX(-1L)))
        }

        Boolean distinct = ginqExpression.getNodeMetaData(GINQ_SELECT_DISTINCT)
        if (distinct) selectMethodCallExpression = callX(selectMethodCallExpression, "distinct")

        final resultName = "__r${System.nanoTime()}"
        statementList << tryCatchS(
                block(
                        declS(localVarX(resultName).tap{ modifiers |= Opcodes.ACC_FINAL }, selectMethodCallExpression),
                        returnS(varX(resultName))
                ),
                block(
                        parallelEnabled ? invokeRemoveVarMethod(PARALLEL) : EmptyStatement.INSTANCE,
                        useWindowFunction ? invokeRemoveVarMethod(USE_WINDOW_FUNCTION) : EmptyStatement.INSTANCE
                )
        )

        def resultLambda = lambdaX(null, block(statementList as Statement[]))
        def result = parallelEnabled
                        ? callX(callX(QUERYABLE_HELPER_TYPE, 'submit', args(resultLambda)), "get")
                        : callX(resultLambda, "call")

        ginqExpressionStack.pop()
        return result
    }

    private static Statement invokeRemoveVarMethod(String varName) {
        stmt(callX(QUERYABLE_HELPER_TYPE, 'removeVar', args(constX(varName))))
    }

    private boolean isParallel() {
        return TRUE_STR == configuration.get(GinqGroovyMethods.CONF_PARALLEL)
    }

    private boolean isUseWindowFunction(GinqExpression ginqExpression) {
        boolean useWindowFunction = false

        ginqExpression.visit(new GinqAstBaseVisitor() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if (isOverMethodCall(call)) {
                    useWindowFunction = true
                } else {
                    super.visitMethodCallExpression(call)
                }
            }
        })

        return useWindowFunction
    }

    private static boolean isAggregateFunction(Expression expression) {
        Expression expr = expression
        if (expression instanceof CastExpression) {
            expr = expression.expression
        }

        if (expr instanceof MethodCallExpression) {
            MethodCallExpression call = (MethodCallExpression) expr
            if (call.implicitThis && AGG_FUNCTION_NAME_LIST.contains(call.methodAsString)) {
                def argumentCnt = ((ArgumentListExpression) call.getArguments()).getExpressions().size()
                if (1 == argumentCnt || (FUNCTION_COUNT == call.methodAsString && 0 == argumentCnt)) {
                    return true
                }
            }
        }

        return false
    }

    private static boolean isOverMethodCall(Expression expression) {
        return expression instanceof MethodCallExpression
            && expression.methodAsString == 'over'
            && expression.objectExpression instanceof MethodCallExpression
            && ((ArgumentListExpression) expression.arguments).getExpressions().size() < 2
    }

    private void addDummyGroupExpressionIfNecessary() {
        if (currentGinqExpression.groupExpression) {
            return
        }

        boolean hasAggFunctionInSelect = false
        SelectExpression selectExpression = currentGinqExpression.selectExpression
        selectExpression.projectionExpr.visit(new GinqAstBaseVisitor() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if (isAggregateFunction(call)) {
                    hasAggFunctionInSelect = true
                } else if (!isOverMethodCall(call)) {
                    super.visitMethodCallExpression(call)
                }
            }

            @Override
            void visitListOfExpressions(List<? extends Expression> list) {
                list?.forEach(expr -> {
                    if (expr !instanceof AbstractGinqExpression) // do not visit subquery
                        expr.visit(this)
                })
            }
        })

        if (hasAggFunctionInSelect) {
            currentGinqExpression.groupExpression =
                    new GroupExpression(
                            new ArgumentListExpression(
                                    Collections.singletonList(
                                            (Expression) fieldX(QUERYABLE_TYPE, "NULL"))))
        }
    }

    @Override
    MethodCallExpression visitFromExpression(FromExpression fromExpression) {
        MethodCallExpression fromMethodCallExpression = constructFromMethodCallExpression(fromExpression.dataSourceExpr)
        fromMethodCallExpression.setSourcePosition(fromExpression)

        return fromMethodCallExpression
    }

    @Override
    MethodCallExpression visitJoinExpression(JoinExpression joinExpression) {
        Expression receiver = joinExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        OnExpression onExpression = joinExpression.onExpression

        if (!onExpression && !joinExpression.crossJoin) {
            collectSyntaxError(
                    new GinqSyntaxError(
                            "`on` clause is expected for `" + joinExpression.joinName + "`",
                            joinExpression.getLineNumber(), joinExpression.getColumnNumber()
                    )
            )
        }

        MethodCallExpression joinMethodCallExpression = constructJoinMethodCallExpression(receiver, joinExpression, onExpression)
        joinMethodCallExpression.setSourcePosition(joinExpression)

        return joinMethodCallExpression
    }

    @Override
    MethodCallExpression visitOnExpression(OnExpression onExpression) {
        // do nothing
    }

    private MethodCallExpression constructFromMethodCallExpression(Expression dataSourceExpr) {
        callX(
                classX(QUERYABLE_TYPE),
                'from',
                args(
                        dataSourceExpr instanceof AbstractGinqExpression
                                ? visit((AbstractGinqExpression) dataSourceExpr)
                                : dataSourceExpr
                )
        )
    }

    private MethodCallExpression constructJoinMethodCallExpression(Expression receiver, JoinExpression joinExpression, OnExpression onExpression) {
        DataSourceExpression otherDataSourceExpression = joinExpression.dataSourceExpression
        Expression otherAliasExpr = otherDataSourceExpression.aliasExpr

        String otherParamName = otherAliasExpr.text
        List<DeclarationExpression> declarationExpressionList = Collections.emptyList()
        Expression filterExpr = EmptyExpression.INSTANCE
        if (onExpression) {
            filterExpr = onExpression.getFilterExpr()
            Tuple3<String, List<DeclarationExpression>, Expression> paramNameAndLambdaCode = correctVariablesOfLambdaExpression(otherDataSourceExpression, filterExpr)
            otherParamName = paramNameAndLambdaCode.v1
            declarationExpressionList = paramNameAndLambdaCode.v2
            filterExpr = paramNameAndLambdaCode.v3
        }

        List<Statement> statementList = []
        statementList.addAll(declarationExpressionList.stream().map(e -> stmt(e)).collect(Collectors.toList()))


        List<Expression> argumentExpressionList = []
        argumentExpressionList << constructFromMethodCallExpression(joinExpression.dataSourceExpr)
        def joinName = joinExpression.joinName

        List<BinaryExpression> equalExpressionList = collectEqualExpressionForHashJoin(onExpression)
        if (joinExpression.smartInnerJoin) {
            joinName = equalExpressionList ? JoinExpression.INNER_HASH_JOIN : JoinExpression.INNER_JOIN
        }

        if (joinName.toLowerCase().contains('hash')) {
            List<Expression> leftExpressionList = []
            List<Expression> rightExpressionList = []

            if (onExpression.filterExpr !instanceof BinaryExpression) {
                collectSyntaxError(
                        new GinqSyntaxError(
                                "Only binary expressions(`==`, `&&`) are allowed in `on` clause of hash join",
                                onExpression.filterExpr.getLineNumber(), onExpression.filterExpr.getColumnNumber()
                        )
                )
            }

            if (!equalExpressionList) {
                collectEqualExpressionForHashJoin(onExpression,
                        expression -> collectSyntaxError(
                                new GinqSyntaxError(
                                        "`" + expression.operation.text + "` is not allowed in `on` clause of hash join",
                                        expression.getLineNumber(), expression.getColumnNumber()
                                )
                        ))
            }

            equalExpressionList.forEach((BinaryExpression expression) -> {
                collectHashJoinFields([expression.leftExpression, expression.rightExpression], joinExpression.aliasExpr.text, leftExpressionList, rightExpressionList)
            })

            statementList.add(stmt(listX(leftExpressionList)))
            argumentExpressionList << lambdaX(
                    params(
                            param(dynamicType(), otherParamName)
                    ),
                    block(statementList as Statement[])
            )

            argumentExpressionList << lambdaX(
                    params(
                            param(dynamicType(), joinExpression.aliasExpr.text)
                    ),
                    block(stmt(listX(rightExpressionList)))
            )
        } else {
            statementList.add(stmt(filterExpr))
            argumentExpressionList << (null == onExpression ? EmptyExpression.INSTANCE : lambdaX(
                    params(
                            param(dynamicType(), otherParamName),
                            param(dynamicType(), joinExpression.aliasExpr.text)
                    ),
                    block(statementList as Statement[])))
        }

        MethodCallExpression resultMethodCallExpression
        MethodCallExpression joinMethodCallExpression =
                callX(receiver,
                        joinName
                                .replace('join', 'Join')
                                .replace('hash', 'Hash'),
                        args(argumentExpressionList))
        resultMethodCallExpression = joinMethodCallExpression

        if (joinExpression.crossJoin) {
            // cross join does not need `on` clause
            Expression lastArgumentExpression = ((ArgumentListExpression) joinMethodCallExpression.arguments).getExpressions().removeLast()
            if (EmptyExpression.INSTANCE !== lastArgumentExpression) {
                throw new GroovyBugError("Wrong argument removed")
            }
        }

        return resultMethodCallExpression
    }

    private List<BinaryExpression> collectEqualExpressionForHashJoin(OnExpression onExpression, Consumer<BinaryExpression> errorCollector=null) {
        if (!onExpression) return Collections.emptyList()

        List<BinaryExpression> equalExpressionList = []
        boolean valid = true

        onExpression.filterExpr.visit(new CodeVisitorSupport() {
            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                if (Types.LOGICAL_AND == expression.operation.type) {
                    super.visitBinaryExpression(expression)
                } else if (Types.COMPARE_EQUAL == expression.operation.type) {
                    equalExpressionList << expression
                } else {
                    valid = false
                    if (errorCollector) {
                        errorCollector.accept(expression)
                    }
                }
            }
        })

        if (!valid) {
            equalExpressionList.clear()
        }

        return equalExpressionList
    }

    private void collectHashJoinFields(List<Expression> expressionList, String joinAliasName, List<Expression> leftExpressionList, List<Expression> rightExpressionList) {
        expressionList.each {expression ->
            List<Expression> foundVariableExpressionList = []
            expression.visit(new CodeVisitorSupport() {
                @Override
                void visitVariableExpression(VariableExpression expr) {
                    if (expr.text.charAt(0).isLowerCase()) {
                        foundVariableExpressionList << expr
                    }
                    super.visitVariableExpression(expr)
                }
            })
            def variableNameList = foundVariableExpressionList.collect { it.text }
            if (1 != variableNameList.size()) {
                collectSyntaxError(
                        new GinqSyntaxError(
                                "Only one alias expected at each side of `==`, but found: ${variableNameList}",
                                expression.getLineNumber(), expression.getColumnNumber()
                        )
                )
            }

            String aliasName = variableNameList[0]
            if (aliasName == joinAliasName) {
                rightExpressionList << expression
            } else if (dataSourceAliasList.contains(aliasName)) {
                leftExpressionList << expression
            } else {
                collectSyntaxError(
                        new GinqSyntaxError(
                                "Unknown alias: ${aliasName}",
                                expression.getLineNumber(), expression.getColumnNumber()
                        )
                )
            }
        }
    }

    @Override
    MethodCallExpression visitWhereExpression(WhereExpression whereExpression) {
        DataSourceExpression dataSourceExpression = whereExpression.getDataSourceExpression()
        Expression fromMethodCallExpression = whereExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression filterExpr = whereExpression.getFilterExpr()

        // construct the `ListExpression` instance to transform `filterExpr` in the same time
        filterExpr = ((ListExpression) new ListExpression(Collections.singletonList(filterExpr)).transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                if (expression instanceof AbstractGinqExpression) {
                    return visit((AbstractGinqExpression) expression)
                }

                if (expression instanceof BinaryExpression) {
                    if (expression.operation.type in [Types.KEYWORD_IN, Types.COMPARE_NOT_IN]) {
                        if (expression.rightExpression instanceof AbstractGinqExpression) {
                            expression.rightExpression = callX(visit((AbstractGinqExpression) expression.rightExpression), "toList")
                            return expression
                        }
                    }
                }

                return expression.transformExpression(this)
            }
        })).getExpression(0)

        def whereMethodCallExpression = callXWithLambda(fromMethodCallExpression, "where", dataSourceExpression, filterExpr)
        whereMethodCallExpression.setSourcePosition(whereExpression)

        return whereMethodCallExpression
    }

    @Override
    MethodCallExpression visitGroupExpression(GroupExpression groupExpression) {
        DataSourceExpression dataSourceExpression = groupExpression.dataSourceExpression
        Expression groupMethodCallReceiver = groupExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression classifierExpr = groupExpression.classifierExpr

        List<Expression> argumentExpressionList = ((ArgumentListExpression) classifierExpr).getExpressions()
        ConstructorCallExpression namedListCtorCallExpression = constructNamedRecordCtorCallExpression(argumentExpressionList, MD_GROUP_NAME_LIST)
        LambdaExpression classifierLambdaExpression = constructLambdaExpression(dataSourceExpression, namedListCtorCallExpression)
        List<Expression> argList = [classifierLambdaExpression] as List<Expression>

        getCurrentGinqExpression().putNodeMetaData(__GROUPBY_VISITED, true)

        HavingExpression havingExpression = groupExpression.havingExpression
        if (havingExpression) {
            Expression filterExpr = havingExpression.filterExpr
            LambdaExpression havingLambdaExpression = constructLambdaExpression(dataSourceExpression, filterExpr)
            argList << havingLambdaExpression
        }

        MethodCallExpression groupMethodCallExpression = callX(groupMethodCallReceiver, "groupBy", args(argList))
        groupMethodCallExpression.setSourcePosition(groupExpression)

        return groupMethodCallExpression
    }

    @Override
    Expression visitHavingExpression(HavingExpression havingExpression) {
        // do nothing
    }

    @Override
    MethodCallExpression visitOrderExpression(OrderExpression orderExpression) {
        DataSourceExpression dataSourceExpression = orderExpression.dataSourceExpression
        Expression orderMethodCallReceiver = orderExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression ordersExpr = orderExpression.ordersExpr

        List<Expression> orderCtorCallExpressions = constructOrderCtorCallExpressions(ordersExpr, dataSourceExpression)
        def orderMethodCallExpression = callX(orderMethodCallReceiver, "orderBy", args(orderCtorCallExpressions))
        orderMethodCallExpression.sourcePosition = orderExpression
        return orderMethodCallExpression
    }

    private List<Expression> constructOrderCtorCallExpressions(Expression ordersExpr, DataSourceExpression dataSourceExpression) {
        return ((ArgumentListExpression) ordersExpr).expressions.collect { e ->
            Expression target = e
            boolean asc = true
            boolean nullsLast = true
            if (e instanceof BinaryExpression && e.operation.type == Types.KEYWORD_IN) {
                target = e.leftExpression

                String orderOption =
                        e.rightExpression instanceof MethodCallExpression
                                ? ((MethodCallExpression) e.rightExpression).methodAsString
                                : e.rightExpression.text
                if (!ORDER_OPTION_LIST.contains(orderOption)) {
                    collectSyntaxError(
                            new GinqSyntaxError(
                                    "Invalid order: " + orderOption + ", `asc`/`desc` is expected",
                                    e.rightExpression.getLineNumber(), e.rightExpression.getColumnNumber()
                            )
                    )
                }

                asc = 'asc' == orderOption
                if (e.rightExpression instanceof MethodCallExpression) {
                    MethodCallExpression mce = (MethodCallExpression) e.rightExpression
                    String nullsOption = null
                    List<Expression> argList = ((ArgumentListExpression) mce.arguments).getExpressions()
                    if (1 == argList.size()) {
                        Expression nullsOptionExpression = argList[0]
                        if (nullsOptionExpression instanceof VariableExpression) {
                            nullsOption = nullsOptionExpression.text
                            if (!NULLS_OPTION_LIST.contains(nullsOption)) {
                                collectSyntaxError(
                                        new GinqSyntaxError(
                                                "Invalid nulls order: " + nullsOption + ", `nullslast`/`nullsfirst` is expected",
                                                nullsOptionExpression.getLineNumber(), nullsOptionExpression.getColumnNumber()
                                        )
                                )
                            }
                        }
                    } else {
                        collectSyntaxError(
                                new GinqSyntaxError(
                                        "Only `nullslast`/`nullsfirst` is expected",
                                        mce.arguments.getLineNumber(), mce.arguments.getColumnNumber()
                                )
                        )
                    }
                    if (nullsOption) {
                        nullsLast = 'nullslast' == nullsOption
                    }
                }
            }

            LambdaExpression lambdaExpression = constructLambdaExpression(dataSourceExpression, target)

            (Expression) ctorX(ORDER_TYPE, args(lambdaExpression, constX(asc), constX(nullsLast)))
        }
    }

    @Override
    MethodCallExpression visitLimitExpression(LimitExpression limitExpression) {
        Expression limitMethodCallReceiver = limitExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression offsetAndSizeExpr = limitExpression.getOffsetAndSizeExpr()

        def limitMethodCallExpression = callX(limitMethodCallReceiver, "limit", offsetAndSizeExpr)
        limitMethodCallExpression.setSourcePosition(limitExpression)
        return limitMethodCallExpression
    }

    @Override
    MethodCallExpression visitSelectExpression(SelectExpression selectExpression) {
        currentGinqExpression.putNodeMetaData(__VISITING_SELECT, true)
        Expression selectMethodReceiver = selectExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        DataSourceExpression dataSourceExpression = selectExpression.dataSourceExpression
        Expression projectionExpr = selectExpression.getProjectionExpr()

        List<Expression> expressionList = ((TupleExpression) projectionExpr).getExpressions()
        validateGroupCols(expressionList)

        boolean hasRnVariable = false
        boolean hasOverMethodCallExpression = false
        projectionExpr.visit(new GinqAstBaseVisitor() {
            @Override
            void visitVariableExpression(VariableExpression expression) {
                if (_RN == expression.text) {
                    hasRnVariable = true
                }
                super.visitVariableExpression(expression)
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if (isOverMethodCall(call)) {
                    hasOverMethodCallExpression = true
                } else {
                    super.visitMethodCallExpression(call)
                }
            }
        })

        final boolean enableCount = !hasRnVariable && hasOverMethodCallExpression

        Expression lambdaCode = expressionList.get(0)
        def expressionListSize = expressionList.size()
        if (expressionListSize > 1 || (expressionListSize == 1 && lambdaCode instanceof CastExpression)) {
            ConstructorCallExpression namedListCtorCallExpression = constructNamedRecordCtorCallExpression(expressionList, MD_SELECT_NAME_LIST)
            lambdaCode = namedListCtorCallExpression
        }

        final boolean parallel = isParallel()
        final VariableExpression supplyAsyncLambdaParam = varX(supplyAsyncLambdaParamName)
        lambdaCode = ((ListExpression) new ListExpression(Collections.singletonList(lambdaCode)).transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                if (expression instanceof VariableExpression) {
                    if (_RN == expression.text) {
                        getCurrentGinqExpression().putNodeMetaData(__RN_USED, true)
                        return parallel ? supplyAsyncLambdaParam : callX(varX(getRowNumberName()), 'get')
                    }
                }

                if (expression instanceof AbstractGinqExpression) {
                    return callX(
                            classX(QUERYABLE_HELPER_TYPE), "singleValue",
                            visit((AbstractGinqExpression) expression)
                    )
                }

                if (expression instanceof MethodCallExpression) {
                    if (isOverMethodCall(expression)) {
                        if (expression.objectExpression instanceof MethodCallExpression) {
                            VariableExpression wqVar = varX(getWindowQueryableName())

                            String lambdaParamName = getLambdaParamName(dataSourceExpression, lambdaCode)
                            VariableExpression currentRecordVar = varX(lambdaParamName)

                            getCurrentGinqExpression().putNodeMetaData(__VISITING_WINDOW_FUNCTION, true)
                            def windowFunctionMethodCallExpression = (MethodCallExpression) expression.objectExpression

                            Expression result = null
                            if (windowFunctionMethodCallExpression.methodAsString in WINDOW_FUNCTION_LIST) {
                                def argumentListExpression = (ArgumentListExpression) windowFunctionMethodCallExpression.arguments
                                List<Expression> argumentExpressionList = []
                                if (windowFunctionMethodCallExpression.methodAsString !in [FUNCTION_ROW_NUMBER, FUNCTION_RANK, FUNCTION_DENSE_RANK, FUNCTION_PERCENT_RANK, FUNCTION_CUME_DIST] && argumentListExpression.expressions) {
                                    def windowFunctionLambdaCode = argumentListExpression.getExpression(0)
                                    def windowFunctionLambdaName = '__wfp'
                                    def rootObjectExpression = findRootObjectExpression(windowFunctionLambdaCode)

                                    windowFunctionLambdaCode = ((ListExpression) (new ListExpression(Collections.singletonList(windowFunctionLambdaCode)).transformExpression(new ExpressionTransformer() {
                                        @Override
                                        Expression transform(Expression expr) {
                                            if (expr instanceof VariableExpression) {
                                                def isJoin = dataSourceExpression instanceof JoinExpression
                                                if (rootObjectExpression.text == expr.text) {
                                                    if (isJoin) {
                                                        windowFunctionLambdaName = getLambdaParamName(dataSourceExpression, expr)
                                                        return correctVars(dataSourceExpression, windowFunctionLambdaName, expr)
                                                    }

                                                    return varX(windowFunctionLambdaName)
                                                } else if (FUNCTION_AGG == windowFunctionMethodCallExpression.methodAsString && _G == expr.text) {
                                                    if (isJoin) {
                                                        windowFunctionLambdaName = getLambdaParamName(dataSourceExpression, expr)
                                                    }

                                                    return callX(
                                                            classX(QUERYABLE_HELPER_TYPE),
                                                            "navigate",
                                                            args(varX(windowFunctionLambdaName), getMetaDataMethodCall(MD_ALIAS_NAME_LIST))
                                                    )
                                                }
                                            }
                                            return expr.transformExpression(this)
                                        }
                                    }))).getExpression(0)

                                    if (windowFunctionMethodCallExpression.methodAsString in [FUNCTION_NTILE]) {
                                        argumentExpressionList << argumentListExpression.getExpression(0)
                                    } else {
                                        argumentExpressionList << lambdaX(
                                                params(param(dynamicType(), windowFunctionLambdaName)),
                                                block(stmt(windowFunctionLambdaCode))
                                        )
                                    }

                                    if (windowFunctionMethodCallExpression.methodAsString in [FUNCTION_LEAD, FUNCTION_LAG, FUNCTION_NTH_VALUE]) {
                                        List<Expression> exprList = argumentListExpression.getExpressions()
                                        if (exprList.size() > 1) {
                                            argumentExpressionList.addAll(exprList.subList(1, exprList.size()))
                                        }
                                    }
                                }

                                def windowDefinitionFactoryMethodCallExpression = constructWindowDefinitionFactoryMethodCallExpression(expression, dataSourceExpression)
                                Expression newObjectExpression = callX(wqVar, 'over', args(
                                        callX(TUPLE_TYPE, 'tuple', args(currentRecordVar, parallel ? supplyAsyncLambdaParam : getRowNumberMethodCall())),
                                        windowDefinitionFactoryMethodCallExpression
                                ))
                                result = callX(
                                        newObjectExpression,
                                        windowFunctionMethodCallExpression.methodAsString,
                                        args(argumentExpressionList)
                                )
                            } else {
                                collectSyntaxError(new GinqSyntaxError(
                                        "Unsupported window function: `${windowFunctionMethodCallExpression.methodAsString}`",
                                        windowFunctionMethodCallExpression.getLineNumber(), windowFunctionMethodCallExpression.getColumnNumber()
                                ))
                            }

                            getCurrentGinqExpression().putNodeMetaData(__VISITING_WINDOW_FUNCTION, false)

                            return result
                        }
                    }
                }

                return expression.transformExpression(this)
            }
        })).getExpression(0)

        List<Expression> extra = []
        if (enableCount || rowNumberUsed) {
            currentGinqExpression.putNodeMetaData(__RN_USED, true)
            extra << callX(varX(rowNumberName), 'getAndIncrement')
        }

        def selectMethodCallExpression = callXWithLambda(selectMethodReceiver, "select", dataSourceExpression, parallel, lambdaCode, extra, param(dynamicType(), getWindowQueryableName()))

        currentGinqExpression.putNodeMetaData(__VISITING_SELECT, false)

        return selectMethodCallExpression
    }

    @Override
    Expression visitShutdownExpression(ShutdownExpression shutdownExpression) {
        return callX(classX(makeCached(QueryableHelper)), 'shutdown', constX(shutdownExpression.mode))
    }

    private MethodCallExpression getRowNumberMethodCall() {
        callX(varX(rowNumberName), 'get')
    }

    private MethodCallExpression constructWindowDefinitionFactoryMethodCallExpression(MethodCallExpression methodCallExpression, DataSourceExpression dataSourceExpression) {
        Expression classifierExpr = null
        Expression orderExpr = null
        Expression rowsExpr = null
        Expression rangeExpr = null
        ArgumentListExpression argumentListExpression = (ArgumentListExpression) methodCallExpression.arguments
        if (1 == argumentListExpression.getExpressions().size()) {
            final List<MethodCallExpression> ignoredMethodCallExpressionList = []

            argumentListExpression.visit(new CodeVisitorSupport() {
                @Override
                void visitMethodCallExpression(MethodCallExpression call) {
                    super.visitMethodCallExpression(call)
                    if ('partitionby' == call.methodAsString) {
                        classifierExpr = call.arguments
                    } else if ('orderby' == call.methodAsString) {
                        orderExpr = call.arguments
                    } else if ('rows' == call.methodAsString) {
                        rowsExpr = call.arguments
                    } else if ('range' == call.methodAsString) {
                        rangeExpr = call.arguments
                    } else {
                        ignoredMethodCallExpressionList << call
                    }
                }
            })

            validateWindowClause(classifierExpr, orderExpr, rowsExpr, rangeExpr, ignoredMethodCallExpressionList)
        }

        List<Expression> argumentExpressionList = []

        if (classifierExpr) {
            List<Expression> expressionList = ((ArgumentListExpression) classifierExpr).getExpressions()
            LambdaExpression classifierLambdaExpression = constructLambdaExpression(dataSourceExpression, new ListExpression(expressionList))
            argumentExpressionList << classifierLambdaExpression
        }

        if (orderExpr) {
            def orderCtorCallExpressions = constructOrderCtorCallExpressions(orderExpr, dataSourceExpression)
            argumentExpressionList << new ListExpression(orderCtorCallExpressions)
        }

        if (rowsExpr && rangeExpr) {
            collectSyntaxError(new GinqSyntaxError(
                    "`rows` and `range` cannot be used in the same time",
                    rangeExpr.getLineNumber(), rangeExpr.getColumnNumber()
            ))
        }

        if (rowsExpr) {
            if (2 != ((ArgumentListExpression) rowsExpr).getExpressions().size()) {
                collectSyntaxError(new GinqSyntaxError(
                        "Both lower bound and upper bound are expected for `rows`",
                        rowsExpr.getLineNumber(), rowsExpr.getColumnNumber()
                ))
            }

            def rowBoundCtorCallExpression = ctorX(ROWBOUND_TYPE, rowsExpr)
            argumentExpressionList << rowBoundCtorCallExpression
        }

        if (rangeExpr) {
            if (2 != ((ArgumentListExpression) rangeExpr).getExpressions().size()) {
                collectSyntaxError(new GinqSyntaxError(
                        "Both lower bound and upper bound are expected for `range`",
                        rangeExpr.getLineNumber(), rangeExpr.getColumnNumber()
                ))
            }

            if (!orderExpr) {
                collectSyntaxError(new GinqSyntaxError(
                        "`orderby` is expected when using `range`",
                        rangeExpr.getLineNumber(), rangeExpr.getColumnNumber()
                ))
            }
            if (((ArgumentListExpression) orderExpr).getExpressions().size() != 1) {
                collectSyntaxError(new GinqSyntaxError(
                        "Only one field is expected in the `orderby` clause when using `range`",
                        orderExpr.getLineNumber(), orderExpr.getColumnNumber()
                ))
            }

            def valueBoundCtorCallExpression = ctorX(VALUEBOUND_TYPE, rangeExpr)
            argumentExpressionList << valueBoundCtorCallExpression
        }

        String partitionByClauseText = ""
        String orderByClauseText = ""
        argumentListExpression.visit(new GinqAstBaseVisitor() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if ('partitionby' == call.methodAsString) {
                    partitionByClauseText = call.text
                } else if ('orderby' == call.methodAsString) {
                    orderByClauseText = call.text
                }

                super.visitMethodCallExpression(call)
            }
        })

        callX(
                callX(classX(WINDOW_DEFINITION_TYPE), 'of', args(argumentExpressionList)),
                'setId',
                callX(TUPLE_TYPE, 'tuple', args(
                        constX(partitionByClauseText),
                        constX(orderByClauseText),
                        constX(argumentListExpression.text)
                ))
        )
    }

    private void validateWindowClause(Expression classifierExpr, Expression orderExpr, Expression rowsExpr, Expression rangeExpr, List<? extends MethodCallExpression> ignoredMethodCallExpressionList) {
        new ListExpression(Arrays.asList(classifierExpr, orderExpr, rowsExpr, rangeExpr).grep(e -> null != e)).visit(new GinqAstBaseVisitor() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                ignoredMethodCallExpressionList.remove(call)
                super.visitMethodCallExpression(call)
            }
        })

        if (ignoredMethodCallExpressionList) {
            MethodCallExpression ignoredMce = ignoredMethodCallExpressionList.get(0)
            collectSyntaxError(new GinqSyntaxError(
                    "Unknown window clause: `${ignoredMce.methodAsString}`",
                    ignoredMce.getLineNumber(), ignoredMce.getColumnNumber()
            ))
        }
    }

    private int windowQueryableNameSeq = 0
    private String getWindowQueryableName() {
        String name = (String) currentGinqExpression.getNodeMetaData(__WINDOW_QUERYABLE_NAME)

        if (!name) {
            name = "${__WINDOW_QUERYABLE_NAME}${windowQueryableNameSeq++}"
            currentGinqExpression.putNodeMetaData(__WINDOW_QUERYABLE_NAME, name)
        }

        return name
    }

    private static boolean isExpression(final Expression expr, final Class... expressionTypes) {
        Arrays.stream(expressionTypes).anyMatch(clazz -> {
            Expression tmpExpr = expr
            if (expr instanceof CastExpression) {
                tmpExpr = expr.expression
            }
            return clazz.isCase(tmpExpr)
        })
    }

    private void validateGroupCols(List<Expression> expressionList) {
        if (groupByVisited) {
            for (Expression expression : expressionList) {
                new ListExpression(Collections.singletonList(expression)).transformExpression(new ExpressionTransformer() {
                    @Override
                    Expression transform(Expression expr) {
                        if (transformCol(expr).v2.text in getGroupNameList()) {
                            return expr
                        }
                        if (isExpression(expr, VariableExpression, PropertyExpression)) {
                            def text = expr instanceof PropertyExpression ? expr.propertyAsString : expr.text
                            if (Character.isUpperCase(text.charAt(0))) {
                                return expr
                            }
                            def rootObjectExpression = findRootObjectExpression(expr).text
                            if (rootObjectExpression !in getGroupNameList() && rootObjectExpression in getAliasNameList()) {
                                collectSyntaxError(new GinqSyntaxError(
                                        "`${expr.text}` is not in the `groupby` clause",
                                        expr.getLineNumber(), expr.getColumnNumber()
                                ))
                            }
                        } else if (isExpression(expr, MethodCallExpression)) {
                            if (isAggregateFunction(expr)) {
                                return expr
                            }
                            if (((MethodCallExpression) expr).implicitThis) {
                                collectSyntaxError(new GinqSyntaxError(
                                        "`${expr instanceof CastExpression ? expr.expression.text : expr.text}` is not an aggregate function",
                                        expr.getLineNumber(), expr.getColumnNumber()
                                ))
                            }
                        } else if (isExpression(expr, AbstractGinqExpression)) {
                            collectSyntaxError(new GinqSyntaxError(
                                    "sub-query could not be used in the `select` clause with `groupby`",
                                    expr.getLineNumber(), expr.getColumnNumber()
                            ))
                        }
                        return expr.transformExpression(this)
                    }
                })
            }
        }
    }

    private static Tuple2<Expression, Expression> transformCol(Expression e) {
        Expression elementExpression = e
        Expression nameExpression = null
        if (e instanceof CastExpression) {
            elementExpression = e.expression
            nameExpression = constX(e.type.text)
        } else if (e instanceof PropertyExpression) {
            if (e.property instanceof ConstantExpression) {
                elementExpression = e
                nameExpression = constX(e.property.text)
            } else if (e.property instanceof GStringExpression) {
                elementExpression = e
                nameExpression = e.property
            }
        }
        if (nameExpression == null) {
            nameExpression = constX(e.text)
        }
        return tuple(elementExpression, nameExpression)
    }

    private ConstructorCallExpression constructNamedRecordCtorCallExpression(List<Expression> expressionList, String metaDataKey) {
        int expressionListSize = expressionList.size()
        List<Expression> elementExpressionList = new ArrayList<>(expressionListSize)
        List<Expression> nameExpressionList = new ArrayList<>(expressionListSize)
        for (Expression e : expressionList) {
            Tuple2<Expression, Expression> elementAndName = transformCol(e)

            elementExpressionList << elementAndName.v1
            nameExpressionList << elementAndName.v2
        }

        def nameListExpression = new ListExpression(nameExpressionList)
        currentGinqExpression.putNodeMetaData(metaDataKey, nameListExpression)

        ConstructorCallExpression namedRecordCtorCallExpression =
                ctorX(NAMED_RECORD_TYPE,
                        args(
                                new ListExpression(elementExpressionList),
                                getMetaDataMethodCall(metaDataKey), getMetaDataMethodCall(MD_ALIAS_NAME_LIST)
                        )
                )
        return namedRecordCtorCallExpression
    }

    private int metaDataMapNameSeq = 0
    private String getMetaDataMapName() {
        String name = (String) currentGinqExpression.getNodeMetaData(__META_DATA_MAP_NAME_PREFIX)

        if (!name) {
            name = "${__META_DATA_MAP_NAME_PREFIX}${metaDataMapNameSeq++}"
            currentGinqExpression.putNodeMetaData(__META_DATA_MAP_NAME_PREFIX, name)
        }

        return name
    }

    private int rowNumberNameSeq = 0
    private String getRowNumberName() {
        String name = (String) currentGinqExpression.getNodeMetaData(__ROW_NUMBER_NAME_PREFIX)

        if (!name) {
            name = "${__ROW_NUMBER_NAME_PREFIX}${rowNumberNameSeq++}"
            currentGinqExpression.putNodeMetaData(__ROW_NUMBER_NAME_PREFIX, name)
        }

        return name
    }

    private int supplyAsyncLambdaParamNameSeq = 0
    private String getSupplyAsyncLambdaParamName() {
        String name = (String) currentGinqExpression.getNodeMetaData(SUPPLY_ASYNC_LAMBDA_PARAM_NAME_PREFIX)

        if (!name) {
            name = "${SUPPLY_ASYNC_LAMBDA_PARAM_NAME_PREFIX}${supplyAsyncLambdaParamNameSeq++}"
            currentGinqExpression.putNodeMetaData(SUPPLY_ASYNC_LAMBDA_PARAM_NAME_PREFIX, name)
        }

        return name
    }

    private MethodCallExpression getMetaDataMethodCall(String key) {
        callX(varX(metaDataMapName), 'get', constX(key))
    }

    private MethodCallExpression putMetaDataMethodCall(String key, Expression value) {
        callX(varX(metaDataMapName), 'put', args(constX(key), value))
    }

    private ListExpression getSelectNameListExpression() {
        (ListExpression) (currentGinqExpression.getNodeMetaData(MD_SELECT_NAME_LIST) ?: [])
    }

    private ListExpression getGroupNameListExpression() {
        (ListExpression) (currentGinqExpression.getNodeMetaData(MD_GROUP_NAME_LIST) ?: [])
    }

    private List<String> getGroupNameList() {
        groupNameListExpression.expressions*.text
    }

    private ListExpression getAliasNameListExpression() {
        new ListExpression(aliasExpressionList)
    }

    private List<String> getAliasNameList() {
        aliasExpressionList*.text
    }

    private List<Expression> getAliasExpressionList() {
        dataSourceAliasList.collect { (Expression) constX(it) }
    }

    private List<String> getDataSourceAliasList() {
        List<DataSourceExpression> dataSourceExpressionList = []
        dataSourceExpressionList << currentGinqExpression.fromExpression
        dataSourceExpressionList.addAll(currentGinqExpression.joinExpressionList)
        dataSourceExpressionList.stream().map(e -> e.aliasExpr.text).collect(Collectors.toList())
    }

    private Tuple2<List<DeclarationExpression>, Expression> correctVariablesOfGinqExpression(DataSourceExpression dataSourceExpression, Expression expr) {
        String lambdaParamName = expr.getNodeMetaData(__LAMBDA_PARAM_NAME)
        if (null == lambdaParamName) {
            throw new GroovyBugError("lambdaParamName is null. dataSourceExpression:${dataSourceExpression}, expr:${expr}")
        }

        boolean isJoin = dataSourceExpression instanceof JoinExpression
        boolean isGroup = groupByVisited
        List<DeclarationExpression> declarationExpressionList = Collections.emptyList()
        if (isJoin || isGroup) {
            def variableNameSet = new HashSet<String>()
            expr.visit(new GinqAstBaseVisitor() {
                @Override
                void visitVariableExpression(VariableExpression expression) {
                    variableNameSet << expression.text
                    super.visitVariableExpression(expression)
                }
            })

            def lambdaParam = varX(lambdaParamName)
            Map<String, Expression> aliasToAccessPathMap = findAliasAccessPath(dataSourceExpression, lambdaParam)
            declarationExpressionList =
                    aliasToAccessPathMap.entrySet().stream()
                            .filter(e -> variableNameSet.contains(e.key))
                            .map(e -> {
                                def v = localVarX(e.key).tap { modifiers |= Opcodes.ACC_FINAL }

                                if (isGroup) {
                                    return declX(v, propX(varX(__SOURCE_RECORD), e.key))
                                } else {
                                    return declX(v, e.value)
                                }
                            })
                            .collect(Collectors.toList())
        }

        // correct itself and its children nodes
        // The synthetic lambda parameter `__t` represents the element from the result datasource of joining, e.g. `n1` innerJoin `n2`
        // The element from first datasource(`n1`) is referenced via `_t.v1`
        // and the element from second datasource(`n2`) is referenced via `_t.v2`
        expr = ((ListExpression) (new ListExpression(Collections.singletonList(expr)).transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                Expression transformedExpression = correctVars(dataSourceExpression, lambdaParamName, expression)
                if (null == transformedExpression) {
                    return expression
                }
                if (transformedExpression !== expression) {
                    return transformedExpression
                }

                return expression.transformExpression(this)
            }
        }))).getExpression(0)

        tuple(declarationExpressionList, expr)
    }

    private boolean isExternalVariable(Expression rootObjectExpression) {
        rootObjectExpression.text != _G  && rootObjectExpression.text !in groupNameList && rootObjectExpression.text !in aliasNameList
    }

    private Expression correctVars(DataSourceExpression dataSourceExpression, String lambdaParamName, Expression expression) {
        boolean groupByVisited = isGroupByVisited()
        Expression transformedExpression = null

        if (expression instanceof PropertyExpression) {
            if (Character.isUpperCase(expression.propertyAsString.charAt(0))) {
                return transformedExpression
            }

            if (isExternalVariable(findRootObjectExpression(expression))) {
                return transformedExpression
            }
        } else if (expression instanceof VariableExpression) {
            if (expression.isThisExpression()) return expression
            if (expression.text in [__SOURCE_RECORD, __GROUP]) return expression
            if (expression.text && Character.isUpperCase(expression.text.charAt(0))) return expression // type should not be transformed
            if (expression.text.startsWith(__META_DATA_MAP_NAME_PREFIX)) return expression

            if (groupByVisited) { //  groupby
                if (isExternalVariable(expression)) {
                    return expression
                }

                // in #1, we will correct receiver of built-in aggregate functions
                // the correct receiver is `__t.v2`, so we should not replace `__t` here
                if (lambdaParamName != expression.text) {
                    if (visitingAggregateFunctionStack) {
                        if (FUNCTION_AGG == visitingAggregateFunctionStack.peek() && _G == expression.text) {
                            transformedExpression =
                                    callX(
                                        classX(QUERYABLE_HELPER_TYPE),
                                            "navigate",
                                        args(varX(lambdaParamName), getMetaDataMethodCall(MD_ALIAS_NAME_LIST))
                                    )
                        } else {
                            transformedExpression = varX(lambdaParamName)
                        }
                    } else {
                        if (groupNameListExpression.getExpressions().stream().map(e -> e.text).anyMatch(e -> e == expression.text)
                                && aliasNameListExpression.getExpressions().stream().map(e -> e.text).allMatch(e -> e != expression.text)
                        ) {
                            // replace `gk` in the groupby with `__t.v1.gk`, note: __t.v1 stores the group key
                            transformedExpression = propX(varX(__SOURCE_RECORD), expression.text)
                        }
                    }
                }
            } else {
                if (visitingWindowFunction) {
                    boolean isJoin = dataSourceExpression instanceof JoinExpression
                    if (isJoin) {
                        Map<String, Expression>  aliasAccessPathMap = findAliasAccessPath(dataSourceExpression, varX(lambdaParamName))
                        transformedExpression = aliasAccessPathMap.get(expression.text)
                    }
                }
            }
        } else if (expression instanceof MethodCallExpression) {
            // #1
            if (groupByVisited) { // groupby
                if (isAggregateFunction(expression)) {
                    String methodName = expression.methodAsString
                    visitingAggregateFunctionStack.push(methodName)
                    if (FUNCTION_COUNT == methodName && ((TupleExpression) expression.arguments).getExpressions().isEmpty()) { // Similar to count(*) in SQL
                        expression.objectExpression = varX(__GROUP)
                        transformedExpression = expression
                    } else if (methodName in AGG_FUNCTION_NAME_LIST) {
                        Expression lambdaCode = ((TupleExpression) expression.arguments).getExpression(0)
                        lambdaCode.putNodeMetaData(__LAMBDA_PARAM_NAME, findRootObjectExpression(lambdaCode).text)
                        transformedExpression =
                                callXWithLambda(
                                        varX(__GROUP), methodName,
                                        dataSourceExpression, lambdaCode)
                    }
                    visitingAggregateFunctionStack.pop()
                }
            }
        }

        if (null != transformedExpression) {
            return transformedExpression
        }

        expression
    }

    private static Map<String, Expression> findAliasAccessPath(DataSourceExpression dataSourceExpression, Expression prop) {
        boolean isJoin = dataSourceExpression instanceof JoinExpression
        if (!isJoin) {
            return Maps.of(dataSourceExpression.aliasExpr.text, prop)
        }

        /*
         * `n1`(`from` node) join `n2` join `n3`  will construct a join tree:
         *
         *  __t (join node)
         *    |__ v2 (n3)
         *    |__ v1 (join node)
         *         |__ v2 (n2)
         *         |__ v1 (n1) (`from` node)
         *
         * Note: `__t` is a tuple with 2 elements
         * so  `n3`'s access path is `__t.v2`
         * and `n2`'s access path is `__t.v1.v2`
         * and `n1`'s access path is `__t.v1.v1`
         *
         * The following code shows how to construct the access path for variables
         */
        Map<String, Expression> aliasToAccessPathMap = new LinkedHashMap<>()
        for (DataSourceExpression dse = dataSourceExpression; dse instanceof JoinExpression; dse = dse.dataSourceExpression) {
            DataSourceExpression otherDataSourceExpression = dse.dataSourceExpression
            Expression firstAliasExpr = otherDataSourceExpression?.aliasExpr ?: EmptyExpression.INSTANCE
            Expression secondAliasExpr = dse.aliasExpr

            aliasToAccessPathMap.put(secondAliasExpr.text, propX(prop, 'v2'))

            if (otherDataSourceExpression instanceof JoinExpression) {
                prop = propX(prop, 'v1')
            } else {
                aliasToAccessPathMap.put(firstAliasExpr.text, propX(prop, 'v1'))
            }
        }

        aliasToAccessPathMap
    }

    private static Expression findRootObjectExpression(Expression expression) {
        if (expression instanceof PropertyExpression) {
            Expression expr = expression
            for (; expr instanceof PropertyExpression; expr = ((PropertyExpression) expr).objectExpression) {}
            return expr
        }
        expression
    }

    private final Deque<String> visitingAggregateFunctionStack = new ArrayDeque<>()

    @Override
    Expression visit(AbstractGinqExpression expression) {
        expression.accept(this)
    }

    private MethodCallExpression callXWithLambda(Expression receiver, String methodName, DataSourceExpression dataSourceExpression, Expression lambdaCode, Parameter... extraParams) {
        callXWithLambda(receiver, methodName, dataSourceExpression, lambdaCode, Collections.emptyList(), extraParams)
    }

    private MethodCallExpression callXWithLambda(Expression receiver, String methodName, DataSourceExpression dataSourceExpression, boolean async = false, Expression lambdaCode, List<Expression> extraLambdaCode, Parameter... extraParams) {
        LambdaExpression lambdaExpression = constructLambdaExpression(dataSourceExpression, async, lambdaCode, extraParams)

        if (extraLambdaCode) {
            ((BlockStatement) lambdaExpression.code).getStatements().addAll(0, extraLambdaCode.collect { stmt(it) })
        }

        callXWithLambda(receiver, methodName, lambdaExpression)
    }

    private LambdaExpression constructLambdaExpression(DataSourceExpression dataSourceExpression, boolean async = false, Expression lambdaCode, Parameter... extraParams) {
        Tuple3<String, List<DeclarationExpression>, Expression> paramNameAndLambdaCode = correctVariablesOfLambdaExpression(dataSourceExpression, lambdaCode)

        List<DeclarationExpression> declarationExpressionList = paramNameAndLambdaCode.v2
        List<Statement> statementList = []
        if (!visitingWindowFunction) {
            statementList.addAll(declarationExpressionList.stream().map(e -> stmt(e)).collect(Collectors.toList()))
        }

        def transformedLambdCode = paramNameAndLambdaCode.v3
        if (async) {
            ArgumentListExpression  argumentListExpression
            argumentListExpression = rowNumberUsed
                                        ? args(lambdaX(params(param(dynamicType(), supplyAsyncLambdaParamName)),
                                                        stmt(transformedLambdCode)),
                                                rowNumberUsed ? getRowNumberMethodCall() : nullX())
                                        : args(lambdaX(stmt(transformedLambdCode)))

            transformedLambdCode = callX(
                    classX(QUERYABLE_HELPER_TYPE),
                    "supplyAsync",
                    argumentListExpression
            )
        }
        statementList.add(stmt(transformedLambdCode))

        def paramList = [param(dynamicType(), paramNameAndLambdaCode.v1)]
        if (extraParams) {
            paramList.addAll(Arrays.asList(extraParams))
        }

        lambdaX(
                params(paramList as Parameter[]),
                block(statementList as Statement[])
        )
    }

    private int lambdaParamSeq = 0
    private String generateLambdaParamName() {
        "__t_${lambdaParamSeq++}"
    }

    private String getLambdaParamName(DataSourceExpression dataSourceExpression, Expression lambdaCode) {
        boolean groupByVisited = isGroupByVisited()
        String lambdaParamName
        if (dataSourceExpression instanceof JoinExpression || groupByVisited || visitingWindowFunction) {
            lambdaParamName = lambdaCode.getNodeMetaData(__LAMBDA_PARAM_NAME)
            if (!lambdaParamName || visitingAggregateFunctionStack || visitingWindowFunction) {
                lambdaParamName = generateLambdaParamName()
            }
        } else {
            lambdaParamName = dataSourceExpression.aliasExpr.text
        }
        lambdaCode.putNodeMetaData(__LAMBDA_PARAM_NAME, lambdaParamName)
        lambdaParamName
    }

    private Tuple3<String, List<DeclarationExpression>, Expression> correctVariablesOfLambdaExpression(DataSourceExpression dataSourceExpression, Expression lambdaCode) {
        boolean groupByVisited = isGroupByVisited()
        List<DeclarationExpression> declarationExpressionList = Collections.emptyList()
        String lambdaParamName = getLambdaParamName(dataSourceExpression, lambdaCode)
        if (dataSourceExpression instanceof JoinExpression || groupByVisited) {
            Tuple2<List<DeclarationExpression>, Expression> declarationAndLambdaCode = correctVariablesOfGinqExpression(dataSourceExpression, lambdaCode)
            if (!visitingAggregateFunctionStack) {
                declarationExpressionList = declarationAndLambdaCode.v1

                if (groupByVisited) {
                    final sourceRecordDecl =
                            declX(localVarX(__SOURCE_RECORD).tap{ modifiers |= Opcodes.ACC_FINAL },
                                    propX(varX(lambdaParamName), 'v1'))
                    declarationExpressionList.add(0, sourceRecordDecl)

                    final groupDecl =
                            declX(localVarX(__GROUP).tap{ modifiers |= Opcodes.ACC_FINAL },
                                    propX(varX(lambdaParamName), 'v2'))
                    declarationExpressionList.add(1, groupDecl)
                }
            }
            lambdaCode = declarationAndLambdaCode.v2
        } else {
            if (visitingWindowFunction) {
                lambdaCode = ((ListExpression) (new ListExpression(Collections.singletonList(lambdaCode)).transformExpression(new ExpressionTransformer() {
                    @Override
                    Expression transform(Expression expr) {
                        if (expr instanceof VariableExpression) {
                            if (dataSourceExpression.aliasExpr.text == expr.text) {
                                return varX(lambdaParamName)
                            }
                        }
                        return expr.transformExpression(this)
                    }
                }))).getExpression(0)
            }
        }

        if (lambdaCode instanceof ConstructorCallExpression) {
            if (NAMEDRECORD_CLASS_NAME == lambdaCode.type.redirect().name) {
                // store the source record
                lambdaCode = callX(lambdaCode, 'sourceRecord', varX(lambdaParamName))
            }
        }

        tuple(lambdaParamName, declarationExpressionList, lambdaCode)
    }

    private boolean isGroupByVisited() {
        return currentGinqExpression.getNodeMetaData(__GROUPBY_VISITED) ?: false
    }

    private boolean isVisitingSelect() {
        currentGinqExpression.getNodeMetaData(__VISITING_SELECT) ?: false
    }

    private boolean isVisitingWindowFunction() {
        currentGinqExpression.getNodeMetaData(__VISITING_WINDOW_FUNCTION) ?: false
    }

    private boolean isRowNumberUsed() {
        currentGinqExpression.getNodeMetaData(__RN_USED)  ?: false
    }

    private static MethodCallExpression callXWithLambda(Expression receiver, String methodName, LambdaExpression lambdaExpression) {
        callX(
                receiver,
                methodName,
                lambdaExpression
        )
    }

    final SourceUnit sourceUnit

    private Map<String, String> configuration
    @Override
    void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration
    }
    @Override
    Map<String, String> getConfiguration() {
        return configuration
    }

    private final Deque<GinqExpression> ginqExpressionStack = new ArrayDeque<>()

    private static final ClassNode MAPS_TYPE = makeWithoutCaching(Maps.class)
    private static final ClassNode QUERYABLE_TYPE = makeWithoutCaching(Queryable.class)
    private static final ClassNode ORDER_TYPE = makeWithoutCaching(Queryable.Order.class)
    private static final ClassNode NAMED_RECORD_TYPE = makeWithoutCaching(NamedRecord.class)
    private static final ClassNode QUERYABLE_HELPER_TYPE = makeWithoutCaching(QueryableHelper.class)
    private static final ClassNode WINDOW_DEFINITION_TYPE = makeWithoutCaching(WindowDefinition.class)
    private static final ClassNode ROWBOUND_TYPE = makeCached(RowBound.class)
    private static final ClassNode VALUEBOUND_TYPE = makeWithoutCaching(ValueBound.class)
    private static final ClassNode ATOMIC_LONG_TYPE = makeCached(AtomicLong.class)
    private static final ClassNode TUPLE_TYPE = makeWithoutCaching(Tuple.class)

    private static final List<String> ORDER_OPTION_LIST = Arrays.asList('asc', 'desc')
    private static final List<String> NULLS_OPTION_LIST = Arrays.asList('nullslast', 'nullsfirst')
    private static final String FUNCTION_COUNT = 'count'
    private static final String FUNCTION_MIN = 'min'
    private static final String FUNCTION_MAX = 'max'
    private static final String FUNCTION_SUM = 'sum'
    private static final String FUNCTION_AVG = 'avg'
    private static final String FUNCTION_LIST = 'list'
    private static final String FUNCTION_MEDIAN = 'median'
    private static final String FUNCTION_STDEV = 'stdev'
    private static final String FUNCTION_STDEVP = 'stdevp'
    private static final String FUNCTION_VAR = 'var'
    private static final String FUNCTION_VARP = 'varp'
    private static final String FUNCTION_AGG = 'agg'
    private static final List<String> AGG_FUNCTION_NAME_LIST = [FUNCTION_COUNT, FUNCTION_MIN, FUNCTION_MAX, FUNCTION_SUM, FUNCTION_AVG, FUNCTION_MEDIAN, FUNCTION_STDEV, FUNCTION_STDEVP, FUNCTION_VAR, FUNCTION_VARP, FUNCTION_LIST, FUNCTION_AGG]

    private static final String FUNCTION_ROW_NUMBER = 'rowNumber'
    private static final String FUNCTION_LEAD = 'lead'
    private static final String FUNCTION_LAG = 'lag'
    private static final String FUNCTION_FIRST_VALUE = 'firstValue'
    private static final String FUNCTION_LAST_VALUE = 'lastValue'
    private static final String FUNCTION_NTH_VALUE = 'nthValue'
    private static final String FUNCTION_RANK = 'rank'
    private static final String FUNCTION_DENSE_RANK = 'denseRank'
    private static final String FUNCTION_PERCENT_RANK = 'percentRank'
    private static final String FUNCTION_CUME_DIST = 'cumeDist'
    private static final String FUNCTION_NTILE = 'ntile'
    private static final List<String> WINDOW_FUNCTION_LIST = [FUNCTION_COUNT, FUNCTION_MIN, FUNCTION_MAX, FUNCTION_SUM, FUNCTION_AVG, FUNCTION_MEDIAN, FUNCTION_STDEV, FUNCTION_STDEVP, FUNCTION_VAR, FUNCTION_VARP, FUNCTION_AGG,
                                                              FUNCTION_ROW_NUMBER, FUNCTION_LEAD, FUNCTION_LAG, FUNCTION_FIRST_VALUE, FUNCTION_LAST_VALUE, FUNCTION_NTH_VALUE, FUNCTION_RANK, FUNCTION_DENSE_RANK, FUNCTION_PERCENT_RANK, FUNCTION_CUME_DIST, FUNCTION_NTILE]

    private static final String NAMEDRECORD_CLASS_NAME = NamedRecord.class.name

    private static final String USE_WINDOW_FUNCTION = 'useWindowFunction'
    private static final String PARALLEL = 'parallel'
    private static final String TRUE_STR = 'true'

    private static final String __METHOD_CALL_RECEIVER = "__METHOD_CALL_RECEIVER"
    private static final String __GROUPBY_VISITED = "__GROUPBY_VISITED"
    private static final String __VISITING_SELECT = "__VISITING_SELECT"
    private static final String __VISITING_WINDOW_FUNCTION = "__VISITING_WINDOW_FUNCTION"
    private static final String __LAMBDA_PARAM_NAME = "__LAMBDA_PARAM_NAME"
    private static final String  __RN_USED = '__RN_USED'
    private static final String __META_DATA_MAP_NAME_PREFIX = '__metaDataMap_'
    private static final String __WINDOW_QUERYABLE_NAME = '__wq_'
    private static final String __ROW_NUMBER_NAME_PREFIX = '__rowNumber_'
    private static final String SUPPLY_ASYNC_LAMBDA_PARAM_NAME_PREFIX = "__salp_"
    private static final String __SOURCE_RECORD = "__sourceRecord"
    private static final String __GROUP = "__group"
    private static final String MD_GROUP_NAME_LIST = "groupNameList"
    private static final String MD_SELECT_NAME_LIST = "selectNameList"
    private static final String MD_ALIAS_NAME_LIST = 'aliasNameList'

    private static final String _G = '_g' // the implicit variable representing grouped `Queryable` object
    private static final String _RN = '_rn' // the implicit variable representing row number
}
