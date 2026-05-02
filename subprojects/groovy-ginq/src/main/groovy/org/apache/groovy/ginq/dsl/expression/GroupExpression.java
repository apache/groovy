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

/**
 * Represents group by expression
 *
 * @since 4.0.0
 */
public class GroupExpression extends ProcessExpression {
    private final Expression classifierExpr;
    private HavingExpression havingExpression;
    private String intoAlias;

    /**
     * Creates a {@code groupby} clause.
     *
     * @param classifierExpr the grouping classifier
     */
    public GroupExpression(Expression classifierExpr) {
        this.classifierExpr = classifierExpr;
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
        return visitor.visitGroupExpression(this);
    }

    /**
     * Returns the grouping classifier.
     *
     * @return the grouping classifier
     */
    public Expression getClassifierExpr() {
        return classifierExpr;
    }

    /**
     * Returns the optional {@code having} clause.
     *
     * @return the {@code having} clause, or {@code null}
     */
    public HavingExpression getHavingExpression() {
        return havingExpression;
    }

    /**
     * Sets the optional {@code having} clause.
     *
     * @param havingExpression the clause to set
     */
    public void setHavingExpression(HavingExpression havingExpression) {
        this.havingExpression = havingExpression;
    }

    /**
     * Returns the {@code into} alias.
     *
     * @return the alias, or {@code null}
     *
     * @since 6.0.0
     */
    public String getIntoAlias() {
        return intoAlias;
    }

    /**
     * Sets the {@code into} alias.
     *
     * @param intoAlias the alias to set
     *
     * @since 6.0.0
     */
    public void setIntoAlias(String intoAlias) {
        this.intoAlias = intoAlias;
    }

    /**
     * Returns the textual GINQ form of this clause.
     *
     * @return the clause text
     */
    @Override
    public String getText() {
        return "groupby " + classifierExpr.getText() +
                (null == intoAlias ? "" : " into " + intoAlias) +
                (null == havingExpression ? "" : " " + havingExpression.getText());
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
