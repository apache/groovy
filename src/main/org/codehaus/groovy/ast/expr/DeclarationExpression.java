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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.syntax.Token;

/**
 * Represents a local variable name declaration, an expression like 
 * "def foo" or with type "String foo".
 * 
 * @author Jochen Theodorou
 * @version $Revision$
 */
public class DeclarationExpression extends BinaryExpression {
    
    public DeclarationExpression(VariableExpression left, Token operation, Expression right) {
        super(left,operation,right);
    }
    
    public DeclarationExpression(Expression left, Token operation, Expression right) {
        super(left,operation,right);
        check(left,right);
    }
    
    private void check(Expression left,Expression right) {
        if (left instanceof VariableExpression) {
            //nothing
        } else if (left instanceof TupleExpression) {
            //nothing
            TupleExpression tuple = (TupleExpression) left;
            if (tuple.getExpressions().size()==0) throw new GroovyBugError("one element required for left side");
        } else {
            throw new GroovyBugError("illegal left expression for declaration: "+left);
        }
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitDeclarationExpression(this);
    }
    
    public VariableExpression getVariableExpression() {
        return (VariableExpression) this.getLeftExpression();
    }
    
    public void setLeftExpression(Expression leftExpression) {
        check(leftExpression,getRightExpression());
        super.setLeftExpression(leftExpression);
    }
    
    public void setRightExpression(Expression rightExpression) {
        check(getLeftExpression(),rightExpression);
        super.setRightExpression(rightExpression);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression left = getLeftExpression();
        Expression ret = new DeclarationExpression(transformer.transform(getLeftExpression()), getOperation(), transformer.transform(getRightExpression()));
        ret.setSourcePosition(this);
        return ret;        
    }
    
    public boolean isMultipleAssignmentDeclaration() {
        return getLeftExpression() instanceof ArgumentListExpression;
    }
}
