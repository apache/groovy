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
 * Represents the from expression
 *
 * @since 4.0.0
 */
public class FromExpression extends DataSourceExpression {
    /**
     * Creates a {@code from} clause.
     *
     * @param aliasExpr the alias expression
     * @param dataSourceExpr the source expression
     */
    public FromExpression(Expression aliasExpr, Expression dataSourceExpr) {
        super(aliasExpr, dataSourceExpr);
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
        return visitor.visitFromExpression(this);
    }

    /**
     * Returns this clause as its own data-source expression.
     *
     * @return this instance
     */
    @Override
    public DataSourceExpression getDataSourceExpression() {
        return this;
    }

    /**
     * Rejects replacing the {@code from} clause with another data source.
     *
     * @param dataSourceExpression the ignored replacement
     */
    @Override
    public void setDataSourceExpression(DataSourceExpression dataSourceExpression) {
        throw new UnsupportedOperationException(FromExpression.class.getName() + " can not be set to another DataSourceExpression instance");
    }

    /**
     * Returns the textual GINQ form of this clause.
     *
     * @return the clause text
     */
    @Override
    public String getText() {
        return "from " + aliasExpr.getText() + " in "
                + (dataSourceExpr instanceof GinqExpression
                        ? "(" + dataSourceExpr.getText() + ")"
                        : dataSourceExpr.getText());
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
