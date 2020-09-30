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
 * Represents a do { ... } while (condition) loop in Groovy
 */
public class DoWhileStatement extends Statement implements LoopingStatement {

    private BooleanExpression booleanExpression;
    private Statement loopBlock;
    

    public DoWhileStatement(BooleanExpression booleanExpression, Statement loopBlock) {
        this.booleanExpression = booleanExpression;
        this.loopBlock = loopBlock;
    }
    
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitDoWhileLoop(this);
    }
    
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    @Override
    public Statement getLoopBlock() {
        return loopBlock;
    }
    public void setBooleanExpression(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    @Override
    public void setLoopBlock(Statement loopBlock) {
        this.loopBlock = loopBlock;
    }
}
