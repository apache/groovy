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
 * Represents a case statement within a {@link SwitchStatement}.
 * A case statement consists of a pattern (expression) to match against the switch control expression
 * and the statement to execute if the pattern matches. In Groovy, case patterns support various
 * expression types for flexible matching behavior.
 *
 * @see {@link SwitchStatement}
 * @see {@link Statement}
 */
public class CaseStatement extends Statement {

    private Statement code;
    private Expression expression;

    /**
     * Constructs a case statement with the given expression pattern and code block.
     *
     * @param expression the {@link Expression} pattern to match against the switch control expression
     * @param code the {@link Statement} to execute if the pattern matches
     */
    public CaseStatement(Expression expression, Statement code) {
        this.expression = expression;
        this.code = code;
    }

    /**
     * Returns the statement executed if this case pattern matches.
     *
     * @return the {@link Statement} associated with this case
     */
    public Statement getCode() {
        return code;
    }

    /**
     * Sets the statement executed if this case pattern matches.
     *
     * @param code the {@link Statement} to execute
     */
    public void setCode(Statement code) {
        this.code = code;
    }

    /**
     * Returns the pattern expression matched against the switch control expression.
     *
     * @return the pattern {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the pattern expression matched against the switch control expression.
     *
     * @param e the pattern {@link Expression}
     */
    public void setExpression(Expression e) {
        expression=e;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitCaseStatement(this);
    }

    @Override
    public String toString() {
        return super.toString() + "[expression: " + expression + "; code: " + code + "]";
    }
}
