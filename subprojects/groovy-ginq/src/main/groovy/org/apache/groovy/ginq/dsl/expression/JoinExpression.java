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
package org.apache.groovy.ginq.dsl.expression;

import org.apache.groovy.ginq.dsl.GinqAstVisitor;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.List;

/**
 * Represents join expression
 *
 * @since 4.0.0
 */
public class JoinExpression extends DataSourceExpression implements DataSourceHolder {
    /** Clause name for a smart inner join. */
    public static final String SMART_INNER_JOIN = "join";
    /** Clause name for a regular inner join. */
    public static final String INNER_JOIN = "innerjoin";
    /** Clause name for an inner hash join. */
    public static final String INNER_HASH_JOIN = "innerhashjoin";
    private static final String LEFT_JOIN = "leftjoin";
    /** Clause name for a left hash join. */
    public static final String LEFT_HASH_JOIN = "lefthashjoin";
    private static final String CROSS_JOIN = "crossjoin";
    /** All supported join clause names. */
    public static final List<String> JOIN_NAME_LIST =
            List.of(SMART_INNER_JOIN, INNER_JOIN, INNER_HASH_JOIN, LEFT_JOIN, LEFT_HASH_JOIN, "rightjoin", "righthashjoin", "fulljoin", "fullhashjoin", CROSS_JOIN);

    private final String joinName;
    private OnExpression onExpression;
    private DataSourceExpression dataSourceExpression;

    /**
     * Creates a join clause.
     *
     * @param joinName the join keyword
     * @param aliasExpr the join alias
     * @param dataSourceExpr the join data source
     */
    public JoinExpression(String joinName, Expression aliasExpr, Expression dataSourceExpr) {
        super(aliasExpr, dataSourceExpr);
        this.joinName = joinName;
    }

    /**
     * Checks whether the supplied method name denotes a join clause.
     *
     * @param methodName the method name to test
     * @return {@code true} if the method name is a join clause
     */
    public static boolean isJoinExpression(String methodName) {
        return JOIN_NAME_LIST.contains(methodName);
    }

    /**
     * Indicates whether this clause is a cross join.
     *
     * @return {@code true} for a cross join
     */
    public boolean isCrossJoin() {
        return CROSS_JOIN.equals(joinName);
    }

    /**
     * Indicates whether this clause is a smart inner join.
     *
     * @return {@code true} for a smart inner join
     */
    public boolean isSmartInnerJoin() {
        return SMART_INNER_JOIN.equals(joinName);
    }

    /**
     * Indicates whether this clause is an inner join.
     *
     * @return {@code true} for an inner join
     */
    public boolean isInnerJoin() {
        return INNER_JOIN.equals(joinName);
    }

    /**
     * Indicates whether this clause is an inner hash join.
     *
     * @return {@code true} for an inner hash join
     */
    public boolean isInnerHashJoin() {
        return INNER_HASH_JOIN.equals(joinName);
    }

    /**
     * Indicates whether this clause is a left join.
     *
     * @return {@code true} for a left join
     */
    public boolean isLeftJoin() {
        return LEFT_JOIN.equals(joinName);
    }

    /**
     * Indicates whether this clause is a left hash join.
     *
     * @return {@code true} for a left hash join
     */
    public boolean isLeftHashJoin() {
        return LEFT_HASH_JOIN.equals(joinName);
    }

    /**
     * Accepts a visitor for this clause.
     *
     * @param visitor the visitor to accept
     * @param <R> the visit result type
     * @return the visit result
     */
    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitJoinExpression(this);
    }

    /**
     * Returns the join keyword.
     *
     * @return the join keyword
     */
    public String getJoinName() {
        return joinName;
    }

    /**
     * Returns the optional {@code on} clause.
     *
     * @return the {@code on} clause, or {@code null}
     */
    public OnExpression getOnExpression() {
        return onExpression;
    }

    /**
     * Sets the optional {@code on} clause.
     *
     * @param onExpression the clause to set
     */
    public void setOnExpression(OnExpression onExpression) {
        this.onExpression = onExpression;
    }

    /**
     * Returns the preceding data-source expression.
     *
     * @return the preceding data source
     */
    @Override
    public DataSourceExpression getDataSourceExpression() {
        return dataSourceExpression;
    }

    /**
     * Sets the preceding data-source expression.
     *
     * @param dataSourceExpression the preceding data source
     */
    @Override
    public void setDataSourceExpression(DataSourceExpression dataSourceExpression) {
        this.dataSourceExpression = dataSourceExpression;
    }

    /**
     * Returns the textual GINQ form of this clause.
     *
     * @return the clause text
     */
    @Override
    public String getText() {
        return joinName + " " + aliasExpr.getText()
                + " in " + (dataSourceExpr instanceof GinqExpression ? "(" + dataSourceExpr.getText() + ")" : dataSourceExpr.getText())
                + (null == onExpression ? "" : " " + onExpression.getText());
    }

    /**
     * Returns the textual form of this clause.
     *
     * @return the clause text
     */
    @Override
    public String toString() {
        return getText();
    }
}
