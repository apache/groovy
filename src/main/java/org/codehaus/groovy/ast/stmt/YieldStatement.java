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
import org.codehaus.groovy.ast.expr.SwitchExpression;

/**
 * Represents a {@code yield} statement that produces the value of an enclosing
 * {@link SwitchExpression}. Unlike {@link ReturnStatement}, {@code yield} does
 * not return from the enclosing method; it completes the switch expression and
 * transfers the yielded value to the expression's join point (JEP 361 /
 * GROOVY-12255).
 *
 * @see SwitchExpression
 * @see ReturnStatement
 * @since 6.0.0
 */
public class YieldStatement extends Statement {

    private Expression expression;

    /**
     * Constructs a yield statement with the given result expression.
     *
     * @param expression the value produced for the enclosing switch expression
     */
    public YieldStatement(final Expression expression) {
        setExpression(expression);
    }

    /**
     * Returns the expression whose value becomes the switch-expression result.
     *
     * @return the yielded {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the expression whose value becomes the switch-expression result.
     *
     * @param expression the yielded {@link Expression}
     */
    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public String getText() {
        return "yield " + expression.getText();
    }

    @Override
    public String toString() {
        return super.toString() + "[expression:" + expression + "]";
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitYieldStatement(this);
    }
}
