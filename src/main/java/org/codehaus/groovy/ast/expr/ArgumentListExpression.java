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
import org.codehaus.groovy.ast.Parameter;

import java.util.List;

/**
 * Represents one or more arguments being passed into a method
 */
public class ArgumentListExpression extends TupleExpression {

    public static final Object[] EMPTY_ARRAY = {
    };
    
    public static final ArgumentListExpression EMPTY_ARGUMENTS = new ArgumentListExpression();

    public ArgumentListExpression() {
    }

    public ArgumentListExpression(List<Expression> expressions) {
        super(expressions);
    }

    public ArgumentListExpression(Expression[] expressions) {
        super(expressions);
    }

    public ArgumentListExpression(Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            addExpression(new VariableExpression(parameter));
        }
    }
    
    public ArgumentListExpression(Expression expr) {
        super(expr);
    }

    public ArgumentListExpression(Expression expr1, Expression expr2) {
        super(expr1, expr2);
    }

    public ArgumentListExpression(Expression expr1, Expression expr2, Expression expr3) {
        super(expr1, expr2, expr3);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ArgumentListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }
    
    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitArgumentlistExpression(this);
    }
}
