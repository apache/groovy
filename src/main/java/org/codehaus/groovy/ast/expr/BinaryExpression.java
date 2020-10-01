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

/**
 * Represents two expressions and an operation
 */
public class BinaryExpression extends Expression {

    private Expression leftExpression;
    private Expression rightExpression;
    private final Token operation;
    private boolean safe = false;

    public BinaryExpression(Expression leftExpression,
                            Token operation,
                            Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.operation = operation;
        this.rightExpression = rightExpression;
    }

    public BinaryExpression(Expression leftExpression,
                            Token operation,
                            Expression rightExpression,
                            boolean safe) {
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

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    public Token getOperation() {
        return operation;
    }

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

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    /**
     * Creates an assignment expression in which the specified expression
     * is written into the specified variable name.
     */

    public static BinaryExpression newAssignmentExpression(Variable variable, Expression rhs) {
        VariableExpression lhs = new VariableExpression(variable);
        Token operator = Token.newPlaceholder(Types.ASSIGN);

        return new BinaryExpression(lhs, operator, rhs);
    }


    /**
     * Creates variable initialization expression in which the specified expression
     * is written into the specified variable name.
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
