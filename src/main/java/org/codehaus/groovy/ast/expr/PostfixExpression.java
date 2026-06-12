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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.syntax.Token;

/**
 * Represents a postfix unary expression like {@code i++} or {@code value--}.
 * The operation is applied after the value is used, returning the original value before modification.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code counter++} - return counter then increment it
 *   <li>{@code index--} - return index then decrement it
 * </ul>
 *
 * @see PrefixExpression for pre-increment/pre-decrement operators
 * @see UnaryMinusExpression
 * @see UnaryPlusExpression
 */
public class PostfixExpression extends Expression {

    private final Token operation;
    private Expression expression;

    /**
     * Creates a postfix expression.
     *
     * @param expression the expression to apply the operation to
     * @param operation the operator token ({@code ++} or {@code --})
     */
    public PostfixExpression(final Expression expression, final Token operation) {
        this.operation = operation;
        setExpression(expression);
    }

    /**
     * Sets the expression to apply the postfix operation to.
     *
     * @param expression the expression, typically a variable or property
     */
    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression being modified by the postfix operation.
     *
     * @return the expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Returns the operator token.
     *
     * @return the {@code ++} or {@code --} token
     */
    public Token getOperation() {
        return operation;
    }

    /**
     * Returns a string representation of this postfix expression.
     *
     * @return the text representation, e.g., "(foo++)"
     */
    @Override
    public String getText() {
        return "(" + getExpression().getText() + getOperation().getText() + ")";
    }

    /**
     * Returns the type of this postfix expression, which is the type of the operand.
     *
     * @return the type of the inner expression
     */
    @Override
    public ClassNode getType() {
        return getExpression().getType();
    }

    /**
     * Returns a debug string representation.
     *
     * @return a string like "PostfixExpression[foo++]"
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getExpression() + getOperation() + "]";
    }

    /**
     * Transforms this expression by applying the given transformer to the inner expression,
     * creating a new postfix expression with the transformed operand.
     *
     * @param transformer the {@link ExpressionTransformer} to apply
     * @return a new postfix expression with transformed inner expression
     */
    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new PostfixExpression(transformer.transform(getExpression()), getOperation());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Accepts a {@link GroovyCodeVisitor} using the visitor pattern.
     *
     * @param visitor the visitor to accept
     */
    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitPostfixExpression(this);
    }
}
