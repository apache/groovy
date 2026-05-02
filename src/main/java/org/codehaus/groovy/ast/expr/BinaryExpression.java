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
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import static java.util.Objects.requireNonNull;

/**
 * Represents a binary operation between two expressions, such as addition, subtraction, comparison,
 * or array/map access. The operation is specified by a {@link Token} indicating the operation type
 * (e.g., {@code +}, {@code -}, {@code ==}, {@code []}). Supports safe operations where null objects
 * are handled gracefully, returning null instead of throwing an exception.
 *
 * @see Expression
 * @see VariableExpression
 * @see MethodCallExpression
 */
public class BinaryExpression extends Expression {

    private Expression leftExpression;
    private Expression rightExpression;
    private final Token operation;
    private boolean safe = false;

    /**
     * Creates a binary expression with the given left operand, operation, and right operand.
     *
     * @param leftExpression the left operand of the binary operation; must not be null
     * @param operation the operation token (e.g., {@link Types#PLUS}, {@link Types#MINUS});
     *                  may not be null
     * @param rightExpression the right operand of the binary operation; must not be null
     */
    public BinaryExpression(final Expression leftExpression, final Token operation, final Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.operation = requireNonNull(operation);
    }

    /**
     * Creates a binary expression with optional safe navigation support.
     *
     * @param leftExpression the left operand of the binary operation; must not be null
     * @param operation the operation token; may not be null
     * @param rightExpression the right operand of the binary operation; must not be null
     * @param safe if true, enables safe navigation (e.g., null-safe array access with {@code ?[]})
     */
    public BinaryExpression(final Expression leftExpression, final Token operation, final Expression rightExpression, final boolean safe) {
        this(leftExpression, operation, rightExpression);
        this.safe = safe;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + leftExpression + operation + rightExpression + "]";
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitBinaryExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new BinaryExpression(transformer.transform(leftExpression), operation, transformer.transform(rightExpression), safe);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Returns the left operand of this binary expression.
     *
     * @return the left operand {@link Expression}; never null
     */
    public Expression getLeftExpression() {
        return leftExpression;
    }

    /**
     * Sets the left operand of this binary expression.
     *
     * @param leftExpression the new left operand; must not be null
     */
    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    /**
     * Sets the right operand of this binary expression.
     *
     * @param rightExpression the new right operand; must not be null
     */
    public void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    /**
     * Returns the operation token representing the binary operation.
     *
     * @return the operation {@link Token} (e.g., {@link Types#PLUS}, {@link Types#EQUAL}); never null
     */
    public Token getOperation() {
        return operation;
    }

    /**
     * Returns the right operand of this binary expression.
     *
     * @return the right operand {@link Expression}; never null
     */
    public Expression getRightExpression() {
        return rightExpression;
    }

    @Override
    public String getText() {
        if (operation.getType() == Types.LEFT_SQUARE_BRACKET) {
            return leftExpression.getText() + (safe ? "?" : "") + "[" + rightExpression.getText() + "]";
        }
        return "(" + leftExpression.getText() + " " + operation.getText() + " " + rightExpression.getText() + ")";
    }

    /**
     * Indicates whether this binary expression uses safe navigation. Safe operations return null
     * instead of throwing a NullPointerException when the left operand is null (e.g., {@code obj?.property}).
     *
     * @return true if safe navigation is enabled; false otherwise
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Sets whether this binary expression should use safe navigation.
     *
     * @param safe true to enable safe navigation; false otherwise
     */
    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    /**
     * Creates an assignment expression in which the specified expression
     * is written into the specified variable. This is a factory method for
     * constructing assignment operations like {@code x = value}.
     *
     * @param variable the target variable for assignment; must not be null
     * @param rhs the right-hand side expression to assign; must not be null
     * @return a new {@link BinaryExpression} representing the assignment
     */
    public static BinaryExpression newAssignmentExpression(Variable variable, Expression rhs) {
        VariableExpression lhs = new VariableExpression(variable);
        Token operator = Token.newPlaceholder(Types.ASSIGN);

        return new BinaryExpression(lhs, operator, rhs);
    }


    /**
     * Creates a variable initialization expression in which the specified expression
     * is written into the specified variable name with an optional type annotation.
     * This is a factory method for constructing typed variable declarations like {@code String x = value}.
     *
     * @param variable the variable name to initialize; must not be null
     * @param type the type annotation for the variable; may be null for untyped variables
     * @param rhs the right-hand side expression to assign; must not be null
     * @return a new {@link BinaryExpression} representing the initialization
     */
    public static BinaryExpression newInitializationExpression(String variable, ClassNode type, Expression rhs) {
        VariableExpression lhs = new VariableExpression(variable);

        if (type != null) {
            lhs.setType(type);
        }

        Token operator = Token.newPlaceholder(Types.ASSIGN);

        return new BinaryExpression(lhs, operator, rhs);
    }

}
