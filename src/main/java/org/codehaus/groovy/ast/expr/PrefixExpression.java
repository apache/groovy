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
 * Represents a prefix unary expression like {@code ++i} or {@code --value}.
 * The operation is applied to the expression before the value is used.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code ++counter} - increment counter and return new value
 *   <li>{@code --index} - decrement index and return new value
 * </ul>
 *
 * @see PostfixExpression for post-increment/post-decrement operators
 * @see UnaryMinusExpression
 * @see UnaryPlusExpression
 */
public class PrefixExpression extends Expression {

    private final Token operation;
    private Expression expression;

    /**
     * Creates a prefix expression.
     *
     * @param operation the operator token ({@code ++} or {@code --})
     * @param expression the expression to apply the operation to
     */
    public PrefixExpression(final Token operation, final Expression expression) {
        this.operation = operation;
        setExpression(expression);
    }

    /**
     * Sets the expression to apply the prefix operation to.
     *
     * @param expression the expression, typically a variable or property
     */
    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression being modified by the prefix operation.
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
     * Returns a string representation of this prefix expression.
     *
     * @return the text representation, e.g., "(++foo)"
     */
    @Override
    public String getText() {
        return "(" + getOperation().getText() + getExpression().getText() + ")";
    }

    /**
     * Returns the type of this prefix expression, which is the type of the operand.
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
     * @return a string like "PrefixExpression[++foo]"
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getOperation() + getExpression() + "]";
    }

    /**
     * Transforms this expression by applying the given transformer to the inner expression,
     * creating a new prefix expression with the transformed operand.
     *
     * @param transformer the {@link ExpressionTransformer} to apply
     * @return a new prefix expression with transformed inner expression
     */
    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new PrefixExpression(getOperation(), transformer.transform(getExpression()));
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
        visitor.visitPrefixExpression(this);
    }
}
