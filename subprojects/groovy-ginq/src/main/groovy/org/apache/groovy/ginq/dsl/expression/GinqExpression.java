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
 *     |__ [groupby] [into]
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

    /**
     * Dispatches this expression to a GINQ-aware Groovy visitor.
     *
     * @param visitor the visitor to invoke
     */
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        ((GinqAstVisitor) visitor).visitGinqExpression(this);
    }

    /**
     * Accepts a GINQ visitor.
     *
     * @param visitor the visitor to accept
     * @param <R> the visit result type
     * @return the visit result
     */
    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitGinqExpression(this);
    }

    /**
     * Returns the {@code from} clause.
     *
     * @return the {@code from} clause
     */
    public FromExpression getFromExpression() {
        return fromExpression;
    }

    /**
     * Sets the {@code from} clause.
     *
     * @param fromExpression the clause to set
     */
    public void setFromExpression(FromExpression fromExpression) {
        this.fromExpression = fromExpression;
    }

    /**
     * Returns the join clauses.
     *
     * @return the join clauses
     */
    public List<JoinExpression> getJoinExpressionList() {
        return joinExpressionList;
    }

    /**
     * Adds a join clause.
     *
     * @param joinExpression the clause to add
     */
    public void addJoinExpression(JoinExpression joinExpression) {
        joinExpressionList.add(joinExpression);
    }

    /**
     * Sets the {@code where} clause.
     *
     * @param whereExpression the clause to set
     */
    public void setWhereExpression(WhereExpression whereExpression) {
        this.whereExpression = whereExpression;
    }

    /**
     * Returns the {@code where} clause.
     *
     * @return the {@code where} clause
     */
    public WhereExpression getWhereExpression() {
        return whereExpression;
    }

    /**
     * Returns the {@code groupby} clause.
     *
     * @return the {@code groupby} clause
     */
    public GroupExpression getGroupExpression() {
        return groupExpression;
    }

    /**
     * Sets the {@code groupby} clause.
     *
     * @param groupExpression the clause to set
     */
    public void setGroupExpression(GroupExpression groupExpression) {
        this.groupExpression = groupExpression;
    }

    /**
     * Returns the {@code orderby} clause.
     *
     * @return the {@code orderby} clause
     */
    public OrderExpression getOrderExpression() {
        return orderExpression;
    }

    /**
     * Sets the {@code orderby} clause.
     *
     * @param orderExpression the clause to set
     */
    public void setOrderExpression(OrderExpression orderExpression) {
        this.orderExpression = orderExpression;
    }

    /**
     * Returns the {@code limit} clause.
     *
     * @return the {@code limit} clause
     */
    public LimitExpression getLimitExpression() {
        return limitExpression;
    }

    /**
     * Sets the {@code limit} clause.
     *
     * @param limitExpression the clause to set
     */
    public void setLimitExpression(LimitExpression limitExpression) {
        this.limitExpression = limitExpression;
    }

    /**
     * Returns the {@code select} clause.
     *
     * @return the {@code select} clause
     */
    public SelectExpression getSelectExpression() {
        return selectExpression;
    }

    /**
     * Sets the {@code select} clause.
     *
     * @param selectExpression the clause to set
     */
    public void setSelectExpression(SelectExpression selectExpression) {
        this.selectExpression = selectExpression;
    }

    /**
     * Returns the textual GINQ form of this expression.
     *
     * @return the expression text
     */
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

    /**
     * Returns the textual form of this expression.
     *
     * @return the expression text
     */
    @Override
    public String toString() {
        return getText();
    }
}
