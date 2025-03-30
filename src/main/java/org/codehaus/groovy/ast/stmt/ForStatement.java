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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Represents a for loop in Groovy.
 */
public class ForStatement extends Statement implements LoopingStatement {

    public static final Parameter FOR_LOOP_DUMMY = new Parameter(ClassHelper.OBJECT_TYPE, "forLoopDummyParameter");

    private final Parameter variable;
    private Expression collectionExpression;
    private Statement loopBlock;

    public ForStatement(final Parameter variable, final Expression collectionExpression, final Statement loopBlock) {
        this.variable = variable;
        setCollectionExpression(collectionExpression);
        setLoopBlock(loopBlock);
    }

    public void setCollectionExpression(final Expression collectionExpression) {
        this.collectionExpression = collectionExpression;
    }

    @Override
    public void setLoopBlock(final Statement loopBlock) {
        this.loopBlock = loopBlock;
    }

    //--------------------------------------------------------------------------

    public Parameter getVariable() {
        return variable;
    }

    public ClassNode getVariableType() {
        return variable.getType();
    }

    public Expression getCollectionExpression() {
        return collectionExpression;
    }

    @Override
    public Statement getLoopBlock() {
        return loopBlock;
    }

    //--------------------------------------------------------------------------

    private VariableScope scope;

    public VariableScope getVariableScope() {
        return this.scope;
    }

    public void setVariableScope(final VariableScope scope) {
       this.scope = scope;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitForLoop(this);
    }
}
