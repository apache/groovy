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
import org.apache.groovy.ginq.dsl.GinqAstBaseVisitor
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.FromExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.WhereExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import java.util.stream.Collectors

/**
 * Optimize the execution plan of GINQ through transforming AST.
 * <p>
 * Note:The optimizer only optimizes the AST for inner joins for now, e.g.
 *
 * <pre>
 *    from n1 in nums1
 *    innerjoin n2 in nums2 on n1 == n2
 *    where n1 > 1 && n2 <= 3
 *    select n1, n2
 * </pre>
 * will be optimized to
 * <pre>
 *    from n1 in (
 *      from alias86329782 in nums1
 *      where alias86329782 > 1
 *      select alias86329782
 *    )
 *    innerjoin n2 in (
 *      from alias23802906 in nums2
 *      where alias23802906 <= 3
 *      select alias23802906
 *    ) on n1 == n2
 *    where true && true  // omit the original filters with `true`, we could optimize further, e.g. omit the `where` totally
 *    select n1, n2
 * </pre>
 *
 * @since 4.0.0
 */
@CompileStatic
class GinqAstOptimizer extends GinqAstBaseVisitor {

    @Override
    Void visitGinqExpression(GinqExpression ginqExpression) {
        if (!ginqExpression.joinExpressionList) {
            return null
        }

        boolean allInnerJoin = ginqExpression.joinExpressionList.every {it.innerJoin}
        if (!allInnerJoin) return

        List<DataSourceExpression> dataSourceExpressionList = [].tap {
            it << ginqExpression.fromExpression
            it.addAll(ginqExpression.joinExpressionList)
        }

        final List<String> aliasList =
                (List<String>) dataSourceExpressionList.stream()
                        .map((DataSourceExpression e) -> e.aliasExpr.text)
                        .collect(Collectors.toList())

        super.visitGinqExpression(ginqExpression)

        WhereExpression whereExpression = ginqExpression.whereExpression
        if (whereExpression) {
            boolean toOptimize = true
            Map<String, List<Expression>> conditionsToOptimize = [:]
            List<Expression> candidatesToOptimize = []

            whereExpression.filterExpr.visit(new GinqAstBaseVisitor() {
                @Override
                void visitBinaryExpression(BinaryExpression expression) {
                    if (!toOptimize) return

                    if (Types.LOGICAL_OR == expression.getOperation().getType()) {
                        toOptimize = false
                        return
                    }

                    if (Types.LOGICAL_AND == expression.getOperation().getType()) {
                        if (isCandidate(expression.leftExpression)) {
                            candidatesToOptimize << expression.leftExpression
                        }

                        if (isCandidate(expression.rightExpression)) {
                            candidatesToOptimize << expression.rightExpression
                        }
                    }

                    super.visitBinaryExpression(expression)
                }

                static boolean isCandidate(Expression expression) {
                    if (expression instanceof BinaryExpression && expression.operation.type in [Types.LOGICAL_AND, Types.LOGICAL_OR]) {
                        return false
                    }

                    return true
                }
            })

            candidatesToOptimize.stream()
                    .forEach(e -> collectConditionsToOptimize(e, aliasList, conditionsToOptimize))

            conditionsToOptimize.forEach((String alias, List<Expression> conditions) -> {
                DataSourceExpression dataSourceExpression =
                        dataSourceExpressionList.grep { DataSourceExpression e -> e.aliasExpr.text == alias }[0]

                if (dataSourceExpression) {
                    GinqExpression contructedGinqExpression = new GinqExpression()
                    String constructedAlias = "alias${System.nanoTime()}"

                    List<Expression> transformedConditions =
                            conditions.stream()
                                    .map(e -> correctVars(e, alias, constructedAlias))
                                    .collect(Collectors.toList())

                    contructedGinqExpression.fromExpression =
                            new FromExpression(new VariableExpression(constructedAlias), dataSourceExpression.dataSourceExpr)
                    contructedGinqExpression.whereExpression =
                            new WhereExpression(contructFilterExpr(transformedConditions))
                    contructedGinqExpression.selectExpression =
                            new SelectExpression(
                                    new ArgumentListExpression(
                                            Collections.singletonList(
                                                    (Expression) new VariableExpression(constructedAlias))))

                    dataSourceExpression.dataSourceExpr = contructedGinqExpression
                }
            })

            whereExpression.filterExpr =
                    ((ListExpression) new ListExpression(Collections.singletonList(whereExpression.filterExpr))
                            .transformExpression(new ExpressionTransformer() {
                                @Override
                                Expression transform(Expression expression) {
                                    if (expression.getNodeMetaData(TO_OPTIMIZE)) {
                                        return ConstantExpression.TRUE
                                    }

                                    return expression.transformExpression(this)
                                }
                            })).getExpression(0)
        };

        return null
    }

    Expression correctVars(Expression expression, final String alias, final String constructedAlias) {
        ((ListExpression) new ListExpression(Collections.singletonList(expression)).transformExpression(new ExpressionTransformer() {
            @Override
            Expression transform(Expression expr) {
                if (expr instanceof VariableExpression) {
                    if (alias == expr.text) {
                        return new VariableExpression(constructedAlias)
                    }
                }

                return expr.transformExpression(this)
            }
        })).getExpression(0)
    }

    private Expression contructFilterExpr(List<Expression> conditions) {
        if (!conditions) throw new IllegalArgumentException("The argument `conditions` should not be empty")
        if (1 == conditions.size()) return conditions[0]

        if (2 == conditions.size()) {
            return new BinaryExpression(conditions[0], new Token(Types.LOGICAL_AND, '&&', -1, -1), conditions[1])
        }

        def condition = conditions[0]
        def remainingCondition = contructFilterExpr(conditions[1..-1])
        return new BinaryExpression(condition, new Token(Types.LOGICAL_AND, '&&', -1, -1), remainingCondition)
    }

    private void collectConditionsToOptimize(Expression expression, List<String> aliasList, Map<String, List<Expression>> conditionsToOptimize) {
        boolean toAdd = true
        Set<String> usedAliasSet = new HashSet<>()
        expression.visit(new GinqAstBaseVisitor() {
            @Override
            void visitVariableExpression(VariableExpression variableExpression) {
                if (!aliasList.contains(variableExpression.text)) {
                    toAdd = false
                    return
                }

                usedAliasSet << variableExpression.text

                super.visitVariableExpression(variableExpression)
            }
        })

        if (usedAliasSet.size() > 1) {
            toAdd = false
        }

        if (toAdd) {
            expression.putNodeMetaData(TO_OPTIMIZE, true)
            conditionsToOptimize.computeIfAbsent(usedAliasSet[0], k -> []).add(expression)
        }
    }

    private static final String TO_OPTIMIZE = "TO_OPTIMIZE"
}
