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
    public static final String SMART_INNER_JOIN = "join";
    public static final String INNER_JOIN = "innerjoin";
    public static final String INNER_HASH_JOIN = "innerhashjoin";
    private static final String LEFT_JOIN = "leftjoin";
    public static final String LEFT_HASH_JOIN = "lefthashjoin";
    private static final String CROSS_JOIN = "crossjoin";
    public static final List<String> JOIN_NAME_LIST =
            List.of(SMART_INNER_JOIN, INNER_JOIN, INNER_HASH_JOIN, LEFT_JOIN, LEFT_HASH_JOIN, "rightjoin", "righthashjoin", "fulljoin", "fullhashjoin", CROSS_JOIN);

    private final String joinName;
    private OnExpression onExpression;
    private DataSourceExpression dataSourceExpression;

    public JoinExpression(String joinName, Expression aliasExpr, Expression dataSourceExpr) {
        super(aliasExpr, dataSourceExpr);
        this.joinName = joinName;
    }

    public static boolean isJoinExpression(String methodName) {
        return JOIN_NAME_LIST.contains(methodName);
    }

    public boolean isCrossJoin() {
        return CROSS_JOIN.equals(joinName);
    }

    public boolean isSmartInnerJoin() {
        return SMART_INNER_JOIN.equals(joinName);
    }

    public boolean isInnerJoin() {
        return INNER_JOIN.equals(joinName);
    }

    public boolean isInnerHashJoin() {
        return INNER_HASH_JOIN.equals(joinName);
    }

    public boolean isLeftJoin() {
        return LEFT_JOIN.equals(joinName);
    }

    public boolean isLeftHashJoin() {
        return LEFT_HASH_JOIN.equals(joinName);
    }

    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitJoinExpression(this);
    }

    public String getJoinName() {
        return joinName;
    }

    public OnExpression getOnExpression() {
        return onExpression;
    }

    public void setOnExpression(OnExpression onExpression) {
        this.onExpression = onExpression;
    }

    @Override
    public DataSourceExpression getDataSourceExpression() {
        return dataSourceExpression;
    }

    @Override
    public void setDataSourceExpression(DataSourceExpression dataSourceExpression) {
        this.dataSourceExpression = dataSourceExpression;
    }

    @Override
    public String getText() {
        return joinName + " " + aliasExpr.getText()
                + " in " + (dataSourceExpr instanceof GinqExpression ? "(" + dataSourceExpr.getText() + ")" : dataSourceExpr.getText())
                + (null == onExpression ? "" : " " + onExpression.getText());
    }

    @Override
    public String toString() {
        return getText();
    }
}
