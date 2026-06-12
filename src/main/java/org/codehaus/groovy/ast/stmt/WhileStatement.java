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

/**
 * Represents a while (condition) { ... } loop in Groovy.
 * The while loop repeatedly evaluates a {@link BooleanExpression} at the start of each iteration
 * and executes the loop body only if the condition is true. If the condition is false on the first
 * evaluation, the loop body never executes.
 *
 * @see LoopingStatement
 * @see Statement
 * @see BooleanExpression
 * @see DoWhileStatement
 */
public class WhileStatement extends Statement implements LoopingStatement {

    private BooleanExpression booleanExpression;
    private Statement loopBlock;

    /**
     * Constructs a WhileStatement with a condition and loop body.
     *
     * @param booleanExpression
     *      the {@link BooleanExpression} evaluated at the start of each iteration
     * @param loopBlock
     *      the {@link Statement} to execute while the condition is true
     */
    public WhileStatement(BooleanExpression booleanExpression, Statement loopBlock) {
        this.booleanExpression = booleanExpression;
        this.loopBlock = loopBlock;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitWhileLoop(this);
    }

    /**
     * Returns the {@link BooleanExpression} that is evaluated at the start of each iteration.
     *
     * @return the loop condition {@link BooleanExpression}}
     */
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * Returns the loop body {@link Statement}}.
     *
     * @return the loop block {@link Statement}}
     */
    @Override
    public Statement getLoopBlock() {
        return loopBlock;
    }

    /**
     * Sets the {@link BooleanExpression}} to evaluate at the start of each iteration.
     *
     * @param booleanExpression
     *      the loop condition {@link BooleanExpression}}
     */
    public void setBooleanExpression(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    /**
     * Sets the loop body {@link Statement}}.
     *
     * @param loopBlock
     *      the loop block {@link Statement}}
     */
    @Override
    public void setLoopBlock(Statement loopBlock) {
        this.loopBlock = loopBlock;
    }
}
