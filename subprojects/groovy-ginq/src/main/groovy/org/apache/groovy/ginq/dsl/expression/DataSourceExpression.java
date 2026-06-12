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

import org.codehaus.groovy.ast.expr.Expression;

/**
 * Represents data source expression
 *
 * @since 4.0.0
 */
public abstract class DataSourceExpression extends AbstractGinqExpression implements DataSourceHolder {
    /** Alias expression bound to the data source. */
    protected final Expression aliasExpr;
    /** Expression yielding the data source. */
    protected Expression dataSourceExpr;

    /**
     * Creates a data-source expression.
     *
     * @param aliasExpr the alias expression
     * @param dataSourceExpr the source expression
     */
    public DataSourceExpression(Expression aliasExpr, Expression dataSourceExpr) {
        this.aliasExpr = aliasExpr;
        this.dataSourceExpr = dataSourceExpr;
    }

    /**
     * Returns the alias expression.
     *
     * @return the alias expression
     */
    public Expression getAliasExpr() {
        return aliasExpr;
    }

    /**
     * Returns the data-source expression.
     *
     * @return the data-source expression
     */
    public Expression getDataSourceExpr() {
        return dataSourceExpr;
    }

    /**
     * Replaces the data-source expression.
     *
     * @param dataSourceExpr the new data-source expression
     */
    public void setDataSourceExpr(Expression dataSourceExpr) {
        this.dataSourceExpr = dataSourceExpr;
    }
}
