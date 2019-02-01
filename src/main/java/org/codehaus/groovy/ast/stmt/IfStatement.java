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
 * Represents an if (condition) { ... } else { ... } statement in Groovy
 */
public class IfStatement extends Statement {

    private BooleanExpression booleanExpression;
    private Statement ifBlock;
    private Statement elseBlock;
    

    public IfStatement(BooleanExpression booleanExpression, Statement ifBlock, Statement elseBlock) {
        this.booleanExpression = booleanExpression;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitIfElse(this);
    }
    
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }
    
    public Statement getIfBlock() {
        return ifBlock;
    }

    public Statement getElseBlock() {
        return elseBlock;
    }

    public void setBooleanExpression(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    public void setIfBlock(Statement statement) {
        ifBlock = statement;
    }

    public void setElseBlock(Statement statement) {
        elseBlock = statement;
    }
}
