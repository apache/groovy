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
 */
public class SwitchStatement extends Statement {

    private Expression expression;
    private List<CaseStatement> caseStatements = new ArrayList<CaseStatement>();
    private Statement defaultStatement;
    

    public SwitchStatement(Expression expression) {
        this(expression, EmptyStatement.INSTANCE);
    }

    public SwitchStatement(Expression expression, Statement defaultStatement) {
        this.expression = expression;
        this.defaultStatement = defaultStatement;
    }

    public SwitchStatement(Expression expression, List<CaseStatement> caseStatements, Statement defaultStatement) {
        this.expression = expression;
        this.caseStatements = caseStatements;
        this.defaultStatement = defaultStatement;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitSwitch(this);
    }
    
    public List<CaseStatement> getCaseStatements() {
        return caseStatements;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression e) {
        expression=e;
    }
    
    public Statement getDefaultStatement() {
        return defaultStatement;
    }

    public void setDefaultStatement(Statement defaultStatement) {
        this.defaultStatement = defaultStatement;
    }

    public void addCase(CaseStatement caseStatement) {
        caseStatements.add(caseStatement);
    }

    /**
     * @return the case statement of the given index or null
     */
    public CaseStatement getCaseStatement(int idx) {
        if (idx >= 0 && idx < caseStatements.size()) {
            return caseStatements.get(idx);
        }
        return null;
    }
}
