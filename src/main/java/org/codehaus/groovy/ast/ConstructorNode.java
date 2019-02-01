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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;


/**
 * Represents a constructor declaration
 */
public class ConstructorNode extends MethodNode {
    
    public ConstructorNode(int modifiers, Statement code) {
        this(modifiers, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code);
    }
    
    public ConstructorNode(int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        super("<init>",modifiers,ClassHelper.VOID_TYPE,parameters,exceptions,code);
        
        // This variable scope is thrown out and replaced with a different one during semantic analysis.
        VariableScope scope = new VariableScope();
        for (int i = 0; i < parameters.length; i++) {
            scope.putDeclaredVariable(parameters[i]);
        }
        this.setVariableScope(scope);
    }
    
    public boolean firstStatementIsSpecialConstructorCall() {
        Statement code = getFirstStatement();
        if (!(code instanceof ExpressionStatement)) return false;

        Expression expression = ((ExpressionStatement) code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return false;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        return cce.isSpecialCall();
    }

}
