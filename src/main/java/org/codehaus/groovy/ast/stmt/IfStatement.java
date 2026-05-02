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

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an if (condition) { then-block } else { else-block } conditional statement in Groovy.
 * The if statement evaluates a boolean {@link BooleanExpression} and executes one of two code paths:
 * the if block if the condition is true, or the else block (if present) if the condition is false.
 * The else block defaults to an empty statement if not explicitly provided.
 *
 * @see Statement
 * @see BooleanExpression
 * @see BlockStatement
 * @see EmptyStatement
 */
public class IfStatement extends Statement {

    private BooleanExpression booleanExpression;
    private Statement ifBlock;
    private Statement elseBlock;

    /**
     * Constructs an IfStatement with a condition and then/else code blocks.
     *
     * @param booleanExpression
     *      the {@link BooleanExpression} to evaluate; must not be null
     * @param ifBlock
     *      the {@link Statement} to execute if the condition is true; must not be null
     * @param elseBlock
     *      the {@link Statement} to execute if the condition is false, or null for no else clause
     */
    public IfStatement(final BooleanExpression booleanExpression, final Statement ifBlock, final Statement elseBlock) {
        setBooleanExpression(booleanExpression);
        setIfBlock(ifBlock);
        setElseBlock(elseBlock);
    }

    /**
     * Sets the {@link BooleanExpression} that is evaluated to determine control flow.
     *
     * @param booleanExpression
     *      the {@link BooleanExpression} to evaluate; must not be null
     * @throws NullPointerException if booleanExpression is null
     */
    public void setBooleanExpression(final BooleanExpression booleanExpression) {
        this.booleanExpression = Objects.requireNonNull(booleanExpression);
    }

    /**
     * Sets the {@link Statement} to execute when the condition is true.
     *
     * @param statement
     *      the then-block {@link Statement}; must not be null
     * @throws NullPointerException if statement is null
     */
    public void setIfBlock(final Statement statement) {
        ifBlock = Objects.requireNonNull(statement);
    }

    /**
     * Sets the {@link Statement} to execute when the condition is false.
     * If null is provided, the else block is replaced with an empty statement.
     *
     * @param statement
     *      the else-block {@link Statement}, or null for no else clause
     */
    public void setElseBlock(final Statement statement) {
        elseBlock = Optional.ofNullable(statement).orElse(EmptyStatement.INSTANCE);
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitIfElse(this);
    }

    /**
     * Returns the {@link BooleanExpression} that is evaluated to determine which code path to execute.
     *
     * @return the {@link BooleanExpression} representing the condition
     */
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * Returns the {@link Statement} to execute when the condition is true.
     *
     * @return the then-block {@link Statement}
     */
    public Statement getIfBlock() {
        return ifBlock;
    }

    /**
     * Returns the {@link Statement} to execute when the condition is false.
     * Returns {@link EmptyStatement#INSTANCE} if no else clause was specified.
     *
     * @return the else-block {@link Statement}
     */
    public Statement getElseBlock() {
        return elseBlock;
    }

    @Override
    public String getText() {
        Statement thenStmt = getIfBlock(), elseStmt = getElseBlock();

        StringBuilder text = new StringBuilder(64);
        text.append("if (");
        text.append(getBooleanExpression().getText());
        text.append(") ");
        text.append(thenStmt.getText());
        if (!elseStmt.isEmpty()) {
            if (!(thenStmt instanceof BlockStatement)) {
                text.append(';');
            }
            text.append(" else ");
            text.append(elseStmt.getText());
        }
        return text.toString();
    }
}
