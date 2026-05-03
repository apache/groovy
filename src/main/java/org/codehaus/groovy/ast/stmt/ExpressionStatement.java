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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.Expression;


/**
 * Represents an expression statement that executes an expression where the return value is ignored.
 * An expression statement wraps an {@link Expression} for use as a statement in contexts where
 * an expression must be treated as a statement (e.g., method calls, assignments, or other
 * side-effect-producing expressions). The expression is evaluated and its result is discarded.
 *
 * @see {@link Expression}
 * @see {@link Statement}
 * @see {@link ReturnStatement}
 */
public class ExpressionStatement extends Statement {

    private Expression expression;

    /**
     * Constructs an expression statement with the given expression.
     *
     * @param expression the {@link Expression} to execute; must not be null
     * @throws IllegalArgumentException if the expression is null
     */
    public ExpressionStatement(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression cannot be null");
        }
        this.expression = expression;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitExpressionStatement(this);
    }

    /**
     * Returns the expression executed by this statement.
     *
     * @return the {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the expression executed by this statement.
     *
     * @param expression the {@link Expression}
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String getText() {
        return expression.getText();
    }

    @Override
    public String toString() {
        return super.toString() + "[expression:" + expression + "]";
    }

}
