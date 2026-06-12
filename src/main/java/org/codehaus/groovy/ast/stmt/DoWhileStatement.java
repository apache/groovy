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
 * Represents a do { ... } while (condition) loop in Groovy.
 * A do-while loop executes its body unconditionally on the first iteration, then evaluates
 * a {@link BooleanExpression} after each iteration to decide whether to repeat.
 * The loop body always executes at least once, differentiating it from a {@link WhileStatement}.
 *
 * @see LoopingStatement
 * @see Statement
 * @see BooleanExpression
 * @see WhileStatement
 */
public class DoWhileStatement extends Statement implements LoopingStatement {

    private BooleanExpression booleanExpression;
    private Statement loopBlock;

    /**
     * Constructs a DoWhileStatement with a loop body and condition.
     *
     * @param booleanExpression
     *      the {@link BooleanExpression}} evaluated after each iteration
     * @param loopBlock
     *      the {@link Statement}} to execute; always runs at least once
     */
    public DoWhileStatement(BooleanExpression booleanExpression, Statement loopBlock) {
        this.booleanExpression = booleanExpression;
        this.loopBlock = loopBlock;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitDoWhileLoop(this);
    }

    /**
     * Returns the {@link BooleanExpression}} evaluated after each iteration.
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
     * Sets the {@link BooleanExpression}} to evaluate after each iteration.
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
