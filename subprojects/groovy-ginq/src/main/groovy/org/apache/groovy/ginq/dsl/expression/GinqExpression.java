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
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents GINQ expression, which has the following structure:
 * <pre>
 *     ginq
 *     |__ from
 *     |__ [innerjoin/leftjoin/rightjoin/fulljoin/crossjoin]*
 *     |   |__ on
 *     |__ [where]
 *     |__ [groupby]
 *     |   |__ [having]
 *     |__ [orderby]
 *     |__ [limit]
 *     |__ select
 * </pre>
 * (<strong>Note:</strong> [ ] means optional)<br/>
 *
 * @since 4.0.0
 */
public class GinqExpression extends AbstractGinqExpression {
    private FromExpression fromExpression;
    private final List<JoinExpression> joinExpressionList = new ArrayList<>(2);
    private WhereExpression whereExpression;
    private GroupExpression groupExpression;
    private OrderExpression orderExpression;
    private LimitExpression limitExpression;
    private SelectExpression selectExpression;

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        ((GinqAstVisitor) visitor).visitGinqExpression(this);
    }

    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitGinqExpression(this);
    }

    public FromExpression getFromExpression() {
        return fromExpression;
    }

    public void setFromExpression(FromExpression fromExpression) {
        this.fromExpression = fromExpression;
    }

    public List<JoinExpression> getJoinExpressionList() {
        return joinExpressionList;
    }

    public void addJoinExpression(JoinExpression joinExpression) {
        joinExpressionList.add(joinExpression);
    }

    public void setWhereExpression(WhereExpression whereExpression) {
        this.whereExpression = whereExpression;
    }

    public WhereExpression getWhereExpression() {
        return whereExpression;
    }

    public GroupExpression getGroupExpression() {
        return groupExpression;
    }

    public void setGroupExpression(GroupExpression groupExpression) {
        this.groupExpression = groupExpression;
    }

    public OrderExpression getOrderExpression() {
        return orderExpression;
    }

    public void setOrderExpression(OrderExpression orderExpression) {
        this.orderExpression = orderExpression;
    }

    public LimitExpression getLimitExpression() {
        return limitExpression;
    }

    public void setLimitExpression(LimitExpression limitExpression) {
        this.limitExpression = limitExpression;
    }

    public SelectExpression getSelectExpression() {
        return selectExpression;
    }

    public void setSelectExpression(SelectExpression selectExpression) {
        this.selectExpression = selectExpression;
    }

    @Override
    public String getText() {
        return fromExpression.getText() + " "
                + (joinExpressionList.isEmpty() ? "" : joinExpressionList.stream().map(e -> e.getText()).collect(Collectors.joining(" ")) + " ")
                + (null == whereExpression ? "" : whereExpression.getText() + " ")
                + (null == groupExpression ? "" : groupExpression.getText() + " ")
                + (null == orderExpression ? "" : orderExpression.getText() + " ")
                + (null == limitExpression ? "" : limitExpression.getText() + " ")
                + selectExpression.getText();
    }

    @Override
    public String toString() {
        return getText();
    }
}
