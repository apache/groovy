/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Represents a standard for loop in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ForStatement extends Statement {
    public static final Parameter FOR_LOOP_DUMMY = new Parameter(ClassHelper.OBJECT_TYPE,"forLoopDummyParameter");

    private Parameter variable;
    private Expression collectionExpression;
    private Statement loopBlock;
    private VariableScope scope;
    

    public ForStatement(Parameter variable, Expression collectionExpression, Statement loopBlock) {
        this.variable = variable; 
        this.collectionExpression = collectionExpression;
        this.loopBlock = loopBlock;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitForLoop(this);
    }
    
    public Expression getCollectionExpression() {
        return collectionExpression;
    }

    public Statement getLoopBlock() {
        return loopBlock;
    }

    public Parameter getVariable() {
        return variable;
    }
    
    public ClassNode getVariableType() {
        return variable.getType();
    }
    
    public void setCollectionExpression(Expression collectionExpression) {
        this.collectionExpression = collectionExpression;
    }

    public void setVariableScope(VariableScope variableScope) {
       scope = variableScope;        
    }

    public VariableScope getVariableScope() {
        return scope;
    }
}
