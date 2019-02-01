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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.VariableScope;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a list of expressions used to
 * create closures. Example:
 * <code>
 * def foo = (1;2;;)
 * </code>
 * The right side is a ClosureListExpression consisting of
 * two ConstantExpressions for the values 1 and 2, and two
 * EmptyStatement entries. The ClosureListExpression defines a new 
 * variable scope. All created Closures share this scope.
 */
public class ClosureListExpression extends ListExpression {

    private VariableScope scope;
    
    public ClosureListExpression(List<Expression> expressions) {
        super(expressions);
        scope = new VariableScope();
    }
    
    public ClosureListExpression() {
        this(new ArrayList<Expression>(3));
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitClosureListExpression(this);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ClosureListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;       
    }
    
    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }
    
    public VariableScope getVariableScope() {
        return scope;
    }
    
    public String getText() {
        StringBuilder buffer = new StringBuilder("(");
        boolean first = true;
        for (Expression expression : getExpressions()) {
            if (first) {
                first = false;
            } else {
                buffer.append("; ");
            }
            
            buffer.append(expression.getText());
        }
        buffer.append(")");
        return buffer.toString();
    }
}
