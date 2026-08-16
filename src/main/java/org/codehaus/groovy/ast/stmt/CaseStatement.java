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
 * Represents a case arm of a {@link SwitchStatement} or a
 * {@link org.codehaus.groovy.ast.expr.SwitchExpression}.
 * A case consists of a pattern (expression) to match against the switch
 * selector and the statement to execute if the pattern matches.
 *
 * @see {@link SwitchStatement}
 * @see {@link Statement}
 */
public class CaseStatement extends Statement {

    private Statement code;
    private Expression expression;
    private boolean arrow;

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

    /**
     * Indicates whether this case uses an arrow label ({@code case L ->}) rather
     * than a colon label ({@code case L:}). Arrow labels do not fall through
     * (JEP 361).
     *
     * @return {@code true} if this case was written with {@code ->}
     * @since 6.0.0
     */
    public boolean isArrow() {
        return arrow;
    }

    /**
     * Marks this case as an arrow label ({@code case L ->}) or a colon label.
     *
     * @param arrow {@code true} if this case was written with {@code ->}
     * @since 6.0.0
     */
    public void setArrow(final boolean arrow) {
        this.arrow = arrow;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitCaseStatement(this);
    }

    @Override
    public String toString() {
        return super.toString() + "[expression: " + expression + "; code: " + code + "; arrow: " + arrow + "]";
    }
}
