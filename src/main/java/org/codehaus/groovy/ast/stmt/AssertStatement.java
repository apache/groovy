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
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;

import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;

/**
 * Represents an assert statement that enforces a condition with an optional error message.
 * An assert statement evaluates a boolean condition and throws an {@code AssertionError}
 * if the condition is false. An optional message expression may be provided to customize
 * the error message. Assert statements are typically used to verify invariants and debug assumptions.
 *
 * <p>Example: {@code assert i != 0 : "should never be zero"}</p>
 *
 * @see {@link BooleanExpression}
 * @see {@link Expression}
 * @see {@link Statement}
 */
public class AssertStatement extends Statement {

    private BooleanExpression booleanExpression;
    private Expression messageExpression;

    /**
     * Constructs an assert statement with the given condition and no message.
     *
     * @param booleanExpression the {@link BooleanExpression} condition to assert
     */
    public AssertStatement(BooleanExpression booleanExpression) {
        this(booleanExpression, nullX());
    }

    /**
     * Constructs an assert statement with the given condition and message expression.
     *
     * @param booleanExpression the {@link BooleanExpression} condition to assert
     * @param messageExpression the {@link Expression} that evaluates to an optional error message;
     *                           if null or a null expression, no custom message is used
     */
    public AssertStatement(BooleanExpression booleanExpression, Expression messageExpression) {
        this.booleanExpression = booleanExpression;
        this.messageExpression = messageExpression;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitAssertStatement(this);
    }

    /**
     * Returns the message expression that provides an optional error message.
     *
     * @return the {@link Expression} that evaluates to the error message, or a null expression if not provided
     */
    public Expression getMessageExpression() {
        return messageExpression;
    }

    /**
     * Returns the boolean condition to be asserted.
     *
     * @return the {@link BooleanExpression} to evaluate
     */
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * Sets the boolean condition to be asserted.
     *
     * @param booleanExpression the {@link BooleanExpression} to evaluate
     */
    public void setBooleanExpression(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    /**
     * Sets the message expression that provides an optional error message.
     *
     * @param messageExpression the {@link Expression} that evaluates to the error message
     */
    public void setMessageExpression(Expression messageExpression) {
        this.messageExpression = messageExpression;
    }
}
