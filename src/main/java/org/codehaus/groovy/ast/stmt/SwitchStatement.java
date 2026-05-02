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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a switch (object) { case value: ... case [1, 2, 3]: ...  default: ... } statement in Groovy.
 * A switch statement evaluates the control {@link #getExpression() expression} against a sequence of
 * {@link CaseStatement case} values and executes the code associated with the matching case, or the
 * {@link #getDefaultStatement() default statement} if no case matches.
 *
 * @see {@link CaseStatement}
 * @see {@link Statement}
 */
public class SwitchStatement extends Statement {

    private Expression expression;
    private List<CaseStatement> caseStatements = new ArrayList<CaseStatement>();
    private Statement defaultStatement;


    /**
     * Constructs a switch statement with the given control expression.
     * The default statement is initialized to {@link EmptyStatement#INSTANCE}.
     *
     * @param expression the expression to evaluate against case values
     * @see #SwitchStatement(Expression, Statement)
     */
    public SwitchStatement(Expression expression) {
        this(expression, EmptyStatement.INSTANCE);
    }

    /**
     * Constructs a switch statement with the given control expression and default statement.
     *
     * @param expression the expression to evaluate against case values
     * @param defaultStatement the statement executed when no case matches; may be {@link EmptyStatement#INSTANCE}
     * @see #SwitchStatement(Expression, List, Statement)
     */
    public SwitchStatement(Expression expression, Statement defaultStatement) {
        this.expression = expression;
        this.defaultStatement = defaultStatement;
    }

    /**
     * Constructs a switch statement with the given control expression, case statements, and default statement.
     *
     * @param expression the expression to evaluate against case values
     * @param caseStatements the list of {@link CaseStatement} objects representing case branches
     * @param defaultStatement the statement executed when no case matches
     */
    public SwitchStatement(Expression expression, List<CaseStatement> caseStatements, Statement defaultStatement) {
        this.expression = expression;
        this.caseStatements = caseStatements;
        this.defaultStatement = defaultStatement;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitSwitch(this);
    }

    /**
     * Returns the list of case statements in this switch.
     *
     * @return a list of {@link CaseStatement} objects; never null
     */
    public List<CaseStatement> getCaseStatements() {
        return caseStatements;
    }

    /**
     * Returns the control expression that is evaluated against case values.
     *
     * @return the control {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the control expression that is evaluated against case values.
     *
     * @param e the control {@link Expression}
     */
    public void setExpression(Expression e) {
        expression=e;
    }

    /**
     * Returns the statement executed when no case matches the control expression.
     *
     * @return the default {@link Statement}, or {@link EmptyStatement#INSTANCE} if not set
     */
    public Statement getDefaultStatement() {
        return defaultStatement;
    }

    /**
     * Sets the statement executed when no case matches the control expression.
     *
     * @param defaultStatement the default {@link Statement}
     */
    public void setDefaultStatement(Statement defaultStatement) {
        this.defaultStatement = defaultStatement;
    }

    /**
     * Adds a case statement to this switch.
     *
     * @param caseStatement the {@link CaseStatement} to add
     */
    public void addCase(CaseStatement caseStatement) {
        caseStatements.add(caseStatement);
    }

    /**
     * Returns the case statement at the given index.
     *
     * @param idx the index of the case statement to retrieve
     * @return the {@link CaseStatement} at the given index, or null if the index is out of bounds
     */
    public CaseStatement getCaseStatement(int idx) {
        if (idx >= 0 && idx < caseStatements.size()) {
            return caseStatements.get(idx);
        }
        return null;
    }
}
