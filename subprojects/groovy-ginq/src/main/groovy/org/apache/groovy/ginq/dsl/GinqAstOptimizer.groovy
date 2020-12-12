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
package org.apache.groovy.ginq.dsl

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.dsl.expression.DataSourceExpression
import org.apache.groovy.ginq.dsl.expression.FromExpression
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.dsl.expression.JoinExpression
import org.apache.groovy.ginq.dsl.expression.SelectExpression
import org.apache.groovy.ginq.dsl.expression.WhereExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import java.util.stream.Collectors
/**
 * Optimize the execution plan of GINQ through transforming AST.
 * <p>
 * Note:The optimizer only optimizes the AST for inner/left joins for now, e.g.
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
 *    select n1, n2
 * </pre>
 *
 * @since 4.0.0
 */
@CompileStatic
class GinqAstOptimizer extends GinqAstBaseVisitor {

    @Override
    Void visitGinqExpression(GinqExpression ginqExpression) {
        super.visitGinqExpression(ginqExpression)

        if (!ginqExpression.joinExpressionList) {
            return null
        }

        List<DataSourceExpression> dataSourcesToOptimize = findDataSourcesToOptimize(ginqExpression)
        if (!dataSourcesToOptimize) {
            return null
        }

        List<Expression> candidatesToOptimize = findCandidatesToOptimize(ginqExpression.whereExpression)
        if (!candidatesToOptimize) {
            return null
        }

        final List<String> aliasesToOptimize =
                (List<String>) dataSourcesToOptimize.stream()
                        .map((DataSourceExpression e) -> e.aliasExpr.text)
                        .collect(Collectors.toList())

        List<DataSourceExpression> allDataSourceList = [].tap {
            it << ginqExpression.fromExpression
            it.addAll(ginqExpression.joinExpressionList)
        }

        final List<String> allAliasList =
                (List<String>) allDataSourceList.stream()
                        .map((DataSourceExpression e) -> e.aliasExpr.text)
                        .collect(Collectors.toList())

        WhereExpression whereExpression = ginqExpression.whereExpression
        if (whereExpression) {
            boolean transformed = transformFromClause(candidatesToOptimize, aliasesToOptimize, allAliasList, dataSourcesToOptimize)
            if (transformed) {
                transformWhereClause(whereExpression, ginqExpression)
            }
        }

        return null
    }

    private static List<DataSourceExpression> findDataSourcesToOptimize(GinqExpression ginqExpression) {
        List<DataSourceExpression> optimizingDataSourceExpressionList = []
        optimizingDataSourceExpressionList << ginqExpression.fromExpression
        for (JoinExpression joinExpression : ginqExpression.joinExpressionList) {
            if (joinExpression.smartInnerJoin || joinExpression.innerJoin || joinExpression.innerHashJoin) {
                optimizingDataSourceExpressionList << joinExpression
            } else if (joinExpression.leftJoin || joinExpression.leftHashJoin) {
                break
            } else {
                optimizingDataSourceExpressionList.clear()
                break
            }
        }
        return optimizingDataSourceExpressionList
    }

    private static String constantLiteral(ConstantExpression constantExpression) {
        if (constantExpression.value instanceof CharSequence) {
            return "'''${constantExpression.value}'''"
        }

        return constantExpression.text
    }

    private void transformWhereClause(WhereExpression whereExpression, GinqExpression ginqExpression) {
        List<Expression> candidates = findCandidatesToOptimize(whereExpression)
        List<Expression> nonOptimizedCandidates = candidates.grep { Expression e ->
            if (e instanceof ConstantExpression && e.value) {
                return false
            }

            if (e instanceof NotExpression && e.expression instanceof ConstantExpression && !((ConstantExpression) e.expression).value) {
                return false
            }

            if (e instanceof BinaryExpression && e.leftExpression instanceof ConstantExpression && e.rightExpression instanceof ConstantExpression) {
                try {
                    def result = new GroovyShell().evaluate(
                            "${constantLiteral((ConstantExpression) e.leftExpression)} $e.operation.text ${constantLiteral((ConstantExpression) e.rightExpression)}")
                    if (result) {
                        return false
                    }
                } catch (ignored) {
                }
            }

            Boolean optimize = e.getNodeMetaData(TO_OPTIMIZE)
            if (null == optimize || !optimize) {
                return true
            }

            return false
        }

        if (nonOptimizedCandidates) {
            whereExpression.filterExpr = constructFilterExpr(nonOptimizedCandidates)
        } else {
            ginqExpression.whereExpression = null
        }
    }

    private List<Expression> findCandidatesToOptimize(WhereExpression whereExpression) {
        if (!whereExpression) {
            return Collections.emptyList()
        }

        boolean toOptimize = true
        List<Expression> candidatesToOptimize = []

        if (isCandidate(whereExpression.filterExpr)) {
            candidatesToOptimize << whereExpression.filterExpr
        } else {
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
            })
        }

        if (!toOptimize) {
            candidatesToOptimize.clear()
        }

        return candidatesToOptimize
    }
    private static boolean isCandidate(Expression expression) {
        if (expression instanceof BinaryExpression && expression.operation.type in LOGICAL_OP_TYPE_LIST) {
            return false
        }

        return true
    }

    private boolean transformFromClause(List<Expression> candidatesToOptimize, List<String> optimizingAliasList, List<String> allAliasList, List<DataSourceExpression> optimizingDataSourceExpressionList) {
        Map<String, List<Expression>> conditionsToOptimize = [:]
        candidatesToOptimize.stream()
                .forEach(e ->
                        collectConditionsToOptimize(e, allAliasList, optimizingAliasList, conditionsToOptimize))

        boolean transformed = false
        conditionsToOptimize.forEach((String alias, List<Expression> conditions) -> {
            if (!optimizingAliasList.contains(alias)) return

            DataSourceExpression dataSourceExpression =
                    optimizingDataSourceExpressionList.grep { DataSourceExpression e -> e.aliasExpr.text == alias }[0]

            if (dataSourceExpression) {
                GinqExpression contructedGinqExpression = new GinqExpression()
                String constructedAlias = "alias${System.nanoTime()}"

                List<Expression> transformedConditions =
                        conditions.stream()
                                .map(e -> correctVars(e, alias, constructedAlias))
                                .collect(Collectors.toList())

                if (transformedConditions) {
                    transformed = true
                }

                contructedGinqExpression.fromExpression =
                        new FromExpression(new VariableExpression(constructedAlias), dataSourceExpression.dataSourceExpr)
                contructedGinqExpression.whereExpression =
                        new WhereExpression(constructFilterExpr(transformedConditions))
                contructedGinqExpression.selectExpression =
                        new SelectExpression(
                                new ArgumentListExpression(
                                        Collections.singletonList(
                                                (Expression) new VariableExpression(constructedAlias))))

                dataSourceExpression.dataSourceExpr = contructedGinqExpression
            }
        })

        return transformed
    }

    private Expression correctVars(Expression expression, final String alias, final String constructedAlias) {
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

    private Expression constructFilterExpr(List<Expression> conditions) {
        if (!conditions) throw new IllegalArgumentException("The argument `conditions` should not be empty")
        if (1 == conditions.size()) return conditions[0]

        if (2 == conditions.size()) {
            return new BinaryExpression(conditions[0], new Token(Types.LOGICAL_AND, '&&', -1, -1), conditions[1])
        }

        def condition = conditions[0]
        def remainingCondition = constructFilterExpr(conditions[1..-1])
        return new BinaryExpression(condition, new Token(Types.LOGICAL_AND, '&&', -1, -1), remainingCondition)
    }

    private void collectConditionsToOptimize(Expression expression, List<String> allAliasList, List<String> optimizingAliasList, Map<String, List<Expression>> conditionsToOptimize) {
        boolean toAdd = true
        Set<String> usedAliasSet = new HashSet<>()
        expression.visit(new GinqAstBaseVisitor() {
            @Override
            void visitVariableExpression(VariableExpression variableExpression) {
                if (!allAliasList.contains(variableExpression.text)) {
                    toAdd = false
                    return
                }

                usedAliasSet << variableExpression.text

                super.visitVariableExpression(variableExpression)
            }
        })

        if (usedAliasSet.size() != 1) {
            return
        }

        final alias = usedAliasSet[0]
        if (!optimizingAliasList.contains(alias)) {
            return
        }

        if (toAdd) {
            expression.putNodeMetaData(TO_OPTIMIZE, true)
            conditionsToOptimize.computeIfAbsent(alias, k -> []).add(expression)
        }
    }

    private static final List<Integer> LOGICAL_OP_TYPE_LIST = [Types.LOGICAL_AND, Types.LOGICAL_OR]
    private static final String TO_OPTIMIZE = "TO_OPTIMIZE"
}
