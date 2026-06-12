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
 * Represents a synchronized statement that provides mutual exclusion for a code block.
 * A synchronized statement acquires a lock on the monitor associated with the control
 * {@link #getExpression() expression} before executing the code block, ensuring that
 * only one thread can execute the synchronized block at a time for the given monitor object.
 *
 * @see {@link Expression}
 * @see {@link Statement}
 */
public class SynchronizedStatement extends Statement {

    private Statement code;
    private Expression expression;

    /**
     * Constructs a synchronized statement with the given monitor expression and code block.
     *
     * @param expression the {@link Expression} that evaluates to the monitor object to synchronize on
     * @param code the {@link Statement} to execute under mutual exclusion
     */
    public SynchronizedStatement(Expression expression, Statement code) {
        this.expression = expression;
        this.code = code;
    }

    /**
     * Returns the statement executed within the synchronized block.
     *
     * @return the {@link Statement} executed under mutual exclusion
     */
    public Statement getCode() {
        return code;
    }

    /**
     * Sets the statement executed within the synchronized block.
     *
     * @param statement the {@link Statement} to execute under mutual exclusion
     */
    public void setCode(Statement statement) {
        code = statement;
    }

    /**
     * Returns the monitor expression whose lock is acquired for synchronization.
     *
     * @return the monitor {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitSynchronizedStatement(this);
    }

    /**
     * Sets the monitor expression whose lock is acquired for synchronization.
     *
     * @param expression the monitor {@link Expression}
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}
