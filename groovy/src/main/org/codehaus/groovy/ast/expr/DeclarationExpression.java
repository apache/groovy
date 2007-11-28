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

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitDeclarationExpression(this);
    }
    
    public VariableExpression getVariableExpression() {
        return (VariableExpression) this.getLeftExpression();
    }
    
    public void setLeftExpression(Expression leftExpression) {
        super.setLeftExpression((VariableExpression) leftExpression);
    }
    
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret =  new DeclarationExpression((VariableExpression) transformer.transform(getLeftExpression()), getOperation(), transformer.transform(getRightExpression()));
        ret.setSourcePosition(this);
        return ret;        
    }
}
