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
import org.apache.groovy.ginq.dsl.expression.WhereExpression
import org.apache.groovy.ginq.provider.collection.runtime.NamedRecord
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
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
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Types

import java.util.stream.Collectors

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX
import static org.codehaus.groovy.ast.tools.GeneralUtils.lambdaX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

/**
 * Visit AST of GINQ to generate target method calls for GINQ
 *
 * @since 4.0.0
 */
@CompileStatic
class GinqAstWalker implements GinqAstVisitor<Expression>, SyntaxErrorReportable {
    private final SourceUnit sourceUnit
    private final Deque<GinqExpression> ginqExpressionStack = new ArrayDeque<>()

    GinqAstWalker(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    private GinqExpression getCurrentGinqExpression() {
        ginqExpressionStack.peek()
    }

    @Override
    MethodCallExpression visitGinqExpression(GinqExpression ginqExpression) {
        if (!ginqExpression) {
            this.collectSyntaxError(new GinqSyntaxError("`select` clause is missing", -1, -1))
        }

        ginqExpressionStack.push(ginqExpression)

        DataSourceExpression resultDataSourceExpression
        MethodCallExpression resultMethodCallReceiver

        FromExpression fromExpression = currentGinqExpression.fromExpression
        resultDataSourceExpression = fromExpression
        MethodCallExpression fromMethodCallExpression = this.visitFromExpression(fromExpression)
        resultMethodCallReceiver = fromMethodCallExpression

        for (JoinExpression joinExpression : currentGinqExpression.joinExpressionList) {
            joinExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            joinExpression.dataSourceExpression = resultDataSourceExpression

            resultDataSourceExpression = joinExpression
            resultMethodCallReceiver = this.visitJoinExpression(resultDataSourceExpression)
        }

        WhereExpression whereExpression = currentGinqExpression.whereExpression
        if (whereExpression) {
            whereExpression.dataSourceExpression = resultDataSourceExpression
            whereExpression.putNodeMetaData(__METHOD_CALL_RECEIVER, resultMethodCallReceiver)
            MethodCallExpression whereMethodCallExpression = visitWhereExpression(whereExpression)
            resultMethodCallReceiver = whereMethodCallExpression
        }

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

        MethodCallExpression selectMethodCallExpression = this.visitSelectExpression(selectExpression)

        ginqExpressionStack.pop()

        return selectMethodCallExpression
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
            this.collectSyntaxError(
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
        return null // do nothing
    }

    private MethodCallExpression constructFromMethodCallExpression(Expression dataSourceExpr) {
        callX(
                makeQueryableCollectionClassExpression(),
                "from",
                args(
                        dataSourceExpr instanceof AbstractGinqExpression
                                ? this.visit((AbstractGinqExpression) dataSourceExpr)
                                : dataSourceExpr
                )
        )
    }

    private MethodCallExpression constructJoinMethodCallExpression(
            Expression receiver, JoinExpression joinExpression,
            OnExpression onExpression) {

        DataSourceExpression otherDataSourceExpression = joinExpression.dataSourceExpression
        Expression otherAliasExpr = otherDataSourceExpression.aliasExpr

        String otherParamName = otherAliasExpr.text
        Expression filterExpr = EmptyExpression.INSTANCE
        if (onExpression) {
            filterExpr = onExpression.getFilterExpr()
            Tuple2<String, Expression> paramNameAndLambdaCode = correctVariablesOfLambdaExpression(otherDataSourceExpression, filterExpr)
            otherParamName = paramNameAndLambdaCode.v1
            filterExpr = paramNameAndLambdaCode.v2
        }

        MethodCallExpression resultMethodCallExpression
        MethodCallExpression joinMethodCallExpression = callX(receiver, joinExpression.joinName.replace('join', 'Join'),
                args(
                        constructFromMethodCallExpression(joinExpression.dataSourceExpr),
                        null == onExpression ? EmptyExpression.INSTANCE : lambdaX(
                                params(
                                        param(ClassHelper.DYNAMIC_TYPE, otherParamName),
                                        param(ClassHelper.DYNAMIC_TYPE, joinExpression.aliasExpr.text)
                                ),
                                stmt(filterExpr)
                        )
                )
        )
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

    @Override
    MethodCallExpression visitWhereExpression(WhereExpression whereExpression) {
        DataSourceExpression dataSourceExpression = whereExpression.dataSourceExpression
        Expression fromMethodCallExpression = whereExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression filterExpr = whereExpression.getFilterExpr()

        filterExpr = filterExpr.transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                if (expression instanceof AbstractGinqExpression) {
                    return callX((Expression) GinqAstWalker.this.visit((AbstractGinqExpression) expression), "toList")
                }

                return expression.transformExpression(this)
            }
        })

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
        ConstructorCallExpression namedListCtorCallExpression = constructNamedRecordCtorCallExpression(argumentExpressionList)

        LambdaExpression classifierLambdaExpression = constructLambdaExpression(dataSourceExpression, namedListCtorCallExpression)

        List<Expression> argList = new ArrayList<>()
        argList << classifierLambdaExpression

        this.currentGinqExpression.putNodeMetaData(__GROUPBY_VISITED, true)

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
        return null // do nothing
    }

    @Override
    MethodCallExpression visitOrderExpression(OrderExpression orderExpression) {
        DataSourceExpression dataSourceExpression = orderExpression.dataSourceExpression
        Expression orderMethodCallReceiver = orderExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression ordersExpr = orderExpression.ordersExpr

        List<Expression> argumentExpressionList = ((ArgumentListExpression) ordersExpr).getExpressions()
        List<Expression> orderCtorCallExpressions = argumentExpressionList.stream().map(e -> {
            Expression target = e
            boolean asc = true
            if (e instanceof BinaryExpression && e.operation.type == Types.KEYWORD_IN) {
                target = e.leftExpression
                asc = 'asc' == e.rightExpression.text
            }

            LambdaExpression lambdaExpression = constructLambdaExpression(dataSourceExpression, target)

            return ctorX(ClassHelper.make(Queryable.Order.class), args(lambdaExpression, new ConstantExpression(asc)))
        }).collect(Collectors.toList())

        def orderMethodCallExpression = callX(orderMethodCallReceiver, "orderBy", args(orderCtorCallExpressions))
        orderMethodCallExpression.setSourcePosition(orderExpression)

        return orderMethodCallExpression
    }

    @Override
    MethodCallExpression visitLimitExpression(LimitExpression limitExpression) {
        Expression limitMethodCallReceiver = limitExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        Expression offsetAndSizeExpr = limitExpression.offsetAndSizeExpr

        def limitMethodCallExpression = callX(limitMethodCallReceiver, "limit", offsetAndSizeExpr)
        limitMethodCallExpression.setSourcePosition(limitExpression)

        return limitMethodCallExpression
    }

    @Override
    MethodCallExpression visitSelectExpression(SelectExpression selectExpression) {
        Expression selectMethodReceiver = selectExpression.getNodeMetaData(__METHOD_CALL_RECEIVER)
        DataSourceExpression dataSourceExpression = selectExpression.dataSourceExpression
        Expression projectionExpr = selectExpression.getProjectionExpr()

        List<Expression> expressionList = ((TupleExpression) projectionExpr).getExpressions()
        Expression lambdaCode
        if (expressionList.size() > 1) {
            ConstructorCallExpression namedListCtorCallExpression = constructNamedRecordCtorCallExpression(expressionList)
            lambdaCode = namedListCtorCallExpression
        } else {
            lambdaCode = expressionList.get(0)
        }

        return callXWithLambda(selectMethodReceiver, "select", dataSourceExpression, lambdaCode)
    }

    private ConstructorCallExpression constructNamedRecordCtorCallExpression(List<Expression> expressionList) {
        int expressionListSize = expressionList.size()
        List<Expression> elementExpressionList = new ArrayList<>(expressionListSize)
        List<Expression> nameExpressionList = new ArrayList<>(expressionListSize)
        for (Expression e : expressionList) {
            Expression elementExpression = e
            Expression nameExpression = new ConstantExpression(e.text)

            if (e instanceof CastExpression) {
                elementExpression = e.expression
                nameExpression = new ConstantExpression(e.type.text)
            } else if (e instanceof PropertyExpression) {
                if (e.property instanceof ConstantExpression) {
                    elementExpression = e
                    nameExpression = new ConstantExpression(e.property.text)
                } else if (e.property instanceof GStringExpression) {
                    elementExpression = e
                    nameExpression = e.property
                }
            }
            elementExpressionList << elementExpression
            nameExpressionList << nameExpression
        }

        ConstructorCallExpression namedRecordCtorCallExpression =
                ctorX(ClassHelper.make(NamedRecord.class), args(new ListExpression(elementExpressionList),
                        new ListExpression(nameExpressionList), new ListExpression(aliasExpressionList)))
        return namedRecordCtorCallExpression
    }

    private List<Expression> getAliasExpressionList() {
        dataSourceAliasList.stream()
                .map(e -> new ConstantExpression(e))
                .collect(Collectors.toList())
    }

    private List<String> getDataSourceAliasList() {
        List<DataSourceExpression> dataSourceExpressionList = []
        dataSourceExpressionList << currentGinqExpression.fromExpression
        dataSourceExpressionList.addAll(currentGinqExpression.joinExpressionList)

        return dataSourceExpressionList.stream().map(e -> e.aliasExpr.text).collect(Collectors.toList())
    }

    private Expression correctVariablesOfGinqExpression(DataSourceExpression dataSourceExpression, Expression expr) {
        String lambdaParamName = expr.getNodeMetaData(__LAMBDA_PARAM_NAME)
        if (null == lambdaParamName) {
            throw new GroovyBugError("lambdaParamName is null. dataSourceExpression:${dataSourceExpression}, expr:${expr}")
        }

        // (1) correct itself
        expr = correctVars(dataSourceExpression, lambdaParamName, expr)

        // (2) correct its children nodes
        // The synthetic lambda parameter `__t` represents the element from the result datasource of joining, e.g. `n1` innerJoin `n2`
        // The element from first datasource(`n1`) is referenced via `_t.v1`
        // and the element from second datasource(`n2`) is referenced via `_t.v2`
        expr = expr.transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expression) {
                Expression transformedExpression = correctVars(dataSourceExpression, lambdaParamName, expression)
                if (transformedExpression !== expression) {
                    return transformedExpression
                }

                return expression.transformExpression(this)
            }
        })

        return expr
    }

    private Expression correctVars(DataSourceExpression dataSourceExpression, String lambdaParamName, Expression expression) {
        boolean groupByVisited = isGroupByVisited()
        boolean isJoin = dataSourceExpression instanceof JoinExpression

        Expression transformedExpression = null
        if (expression instanceof VariableExpression) {
            if (expression.isThisExpression()) return expression
            if (expression.text && Character.isUpperCase(expression.text.charAt(0))) return expression // type should not be transformed

            if (groupByVisited) { //  groupby
                // in #1, we will correct receiver of built-in aggregate functions
                // the correct receiver is `__t.v2`, so we should not replace `__t` here
                if (lambdaParamName != expression.text) {
                    if (visitingAggregateFunction) {
                        if (_G == expression.text) {
                            transformedExpression =
                                    callX(
                                        new ClassExpression(ClassHelper.make(QueryableHelper.class)),
                                            "navigate",
                                        args(new VariableExpression(lambdaParamName), new ListExpression(aliasExpressionList))
                                    )
                        } else {
                            transformedExpression = isJoin
                                    ? correctVarsForJoin(dataSourceExpression, expression, new VariableExpression(lambdaParamName))
                                    : new VariableExpression(lambdaParamName)
                        }
                    } else {
                        // replace `gk` in the groupby with `__t.v1.gk`, note: __t.v1 stores the group key
                        transformedExpression = propX(propX(new VariableExpression(lambdaParamName), 'v1'), expression.text)
                    }
                }
            } else if (isJoin) {
                transformedExpression = correctVarsForJoin(dataSourceExpression, expression, new VariableExpression(lambdaParamName))
            }
        } else if (expression instanceof MethodCallExpression) {
            // #1
            if (groupByVisited) { // groupby
                if (expression.implicitThis) {
                    String methodName = expression.methodAsString
                    if ('count' == methodName && ((TupleExpression) expression.arguments).getExpressions().isEmpty()) { // Similar to count(*) in SQL
                        visitingAggregateFunction = true
                        expression.objectExpression = propX(new VariableExpression(lambdaParamName), 'v2')
                        transformedExpression = expression
                        visitingAggregateFunction = false
                    } else if (methodName in ['count', 'min', 'max', 'sum', 'agg'] && 1 == ((TupleExpression) expression.arguments).getExpressions().size()) {
                        visitingAggregateFunction = true
                        Expression lambdaCode = ((TupleExpression) expression.arguments).getExpression(0)
                        lambdaCode.putNodeMetaData(__LAMBDA_PARAM_NAME, findRootObjectExpression(lambdaCode).text)
                        transformedExpression =
                                callXWithLambda(
                                        propX(new VariableExpression(lambdaParamName), 'v2'), methodName,
                                        dataSourceExpression, lambdaCode)
                        visitingAggregateFunction = false
                    }
                }
            }
        }

        if (null != transformedExpression) {
            return transformedExpression
        }

        return expression
    }

    private Expression correctVarsForJoin(DataSourceExpression dataSourceExpression, Expression expression, Expression prop) {
        boolean isJoin = dataSourceExpression instanceof JoinExpression
        if (!isJoin) return expression

        Expression transformedExpression = null
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
        for (DataSourceExpression dse = dataSourceExpression;
             null == transformedExpression && dse instanceof JoinExpression;
             dse = dse.dataSourceExpression) {

            DataSourceExpression otherDataSourceExpression = dse.dataSourceExpression
            Expression firstAliasExpr = otherDataSourceExpression?.aliasExpr ?: EmptyExpression.INSTANCE
            Expression secondAliasExpr = dse.aliasExpr

            if (firstAliasExpr.text == expression.text && otherDataSourceExpression !instanceof JoinExpression) {
                transformedExpression = propX(prop, 'v1')
            } else if (secondAliasExpr.text == expression.text) {
                transformedExpression = propX(prop, 'v2')
            } else { // not found
                prop = propX(prop, 'v1')
            }
        }

        return transformedExpression
    }

    private static Expression findRootObjectExpression(Expression expression) {
        if (expression instanceof PropertyExpression) {
            Expression expr = expression
            for (; expr instanceof PropertyExpression; expr = ((PropertyExpression) expr).objectExpression) {}
            return expr
        }

        return expression
    }

    private boolean visitingAggregateFunction

    @Override
    Expression visit(AbstractGinqExpression expression) {
        return expression.accept(this)
    }

    private MethodCallExpression callXWithLambda(Expression receiver, String methodName, DataSourceExpression dataSourceExpression, Expression lambdaCode) {
        LambdaExpression lambdaExpression = constructLambdaExpression(dataSourceExpression, lambdaCode)

        callXWithLambda(receiver, methodName, lambdaExpression)
    }

    private LambdaExpression constructLambdaExpression(DataSourceExpression dataSourceExpression, Expression lambdaCode) {
        Tuple2<String, Expression> paramNameAndLambdaCode = correctVariablesOfLambdaExpression(dataSourceExpression, lambdaCode)

        lambdaX(
                params(param(ClassHelper.DYNAMIC_TYPE, paramNameAndLambdaCode.v1)),
                stmt(paramNameAndLambdaCode.v2)
        )
    }

    private int seq = 0
    private String generateLambdaParamName() {
        "__t_${seq++}"
    }

    private Tuple2<String, Expression> correctVariablesOfLambdaExpression(DataSourceExpression dataSourceExpression, Expression lambdaCode) {
        boolean groupByVisited = isGroupByVisited()

        String lambdaParamName
        if (dataSourceExpression instanceof JoinExpression || groupByVisited) {
            lambdaParamName = lambdaCode.getNodeMetaData(__LAMBDA_PARAM_NAME)
            if (!lambdaParamName || visitingAggregateFunction) {
                lambdaParamName = generateLambdaParamName()
            }

            lambdaCode.putNodeMetaData(__LAMBDA_PARAM_NAME, lambdaParamName)
            lambdaCode = correctVariablesOfGinqExpression(dataSourceExpression, lambdaCode)
        } else {
            lambdaParamName = dataSourceExpression.aliasExpr.text
            lambdaCode.putNodeMetaData(__LAMBDA_PARAM_NAME, lambdaParamName)
        }

        if (lambdaCode instanceof ConstructorCallExpression) {
            if (NamedRecord.class == lambdaCode.type.getTypeClass()) {
                // store the source record
                lambdaCode = callX(lambdaCode, 'sourceRecord', new VariableExpression(lambdaParamName))
            }
        }

        return Tuple.tuple(lambdaParamName, lambdaCode)
    }

    private boolean isGroupByVisited() {
        return currentGinqExpression.getNodeMetaData(__GROUPBY_VISITED) ?: false
    }

    private static MethodCallExpression callXWithLambda(Expression receiver, String methodName, LambdaExpression lambdaExpression) {
        callX(
                receiver,
                methodName,
                lambdaExpression
        )
    }

    private static ClassExpression makeQueryableCollectionClassExpression() {
        new ClassExpression(ClassHelper.make(Queryable.class))
    }

    @Override
    SourceUnit getSourceUnit() {
        sourceUnit
    }

    private static final String __METHOD_CALL_RECEIVER = "__methodCallReceiver"
    private static final String __GROUPBY_VISITED = "__groupByVisited"
    private static final String __LAMBDA_PARAM_NAME = "__LAMBDA_PARAM_NAME"
    private static final String _G = '_g' // the implicit variable representing grouped `Queryable` object
}
