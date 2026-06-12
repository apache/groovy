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
 * Represents a throw statement that raises an exception.
 * A throw statement evaluates an {@link #getExpression() exception expression} and throws
 * the resulting exception object to interrupt normal execution flow and transfer control
 * to an appropriate exception handler.
 *
 * @see {@link CatchStatement}
 * @see {@link TryCatchStatement}
 * @see {@link Statement}
 */
public class ThrowStatement extends Statement {

    private Expression expression;

    /**
     * Constructs a throw statement with the given exception expression.
     *
     * @param expression the {@link Expression} that evaluates to the exception to throw
     */
    public ThrowStatement(Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the exception expression to be thrown.
     *
     * @return the {@link Expression} that evaluates to an exception object
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the exception expression to be thrown.
     *
     * @param expression the {@link Expression} that evaluates to an exception object
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String getText() {
        return "throw " + expression.getText();
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitThrowStatement(this);
    }
}
