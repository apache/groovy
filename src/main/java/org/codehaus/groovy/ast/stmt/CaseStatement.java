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
 * Represents a case statement in a switch statement
 */
public class CaseStatement extends Statement {

    private Statement code;
    private Expression expression;
    
    public CaseStatement(Expression expression, Statement code) {
        this.expression = expression;
        this.code = code;
    }
    
    public Statement getCode() {
        return code;
    }

    public void setCode(Statement code) {
        this.code = code;
    }

    public Expression getExpression() {
        return expression;
    }
    
    public void setExpression(Expression e) {
        expression=e;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitCaseStatement(this);
    }
    
    public String toString() {
        return super.toString() + "[expression: " + expression + "; code: " + code + "]";
    }
}
