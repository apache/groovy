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

import java.util.List;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;

/**
 * Represents one or more arguments being passed into a method
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ArgumentListExpression extends TupleExpression {

    public static final Object[] EMPTY_ARRAY = {
    };
    
    public static final ArgumentListExpression EMPTY_ARGUMENTS = new ArgumentListExpression();

    public ArgumentListExpression() {
    }

    public ArgumentListExpression(List expressions) {
        super(expressions);
    }

    public ArgumentListExpression(Expression[] expressions) {
        super(expressions);
    }

    public ArgumentListExpression(Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            addExpression(new VariableExpression(parameter.getName()));
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
        return ret;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArgumentlistExpression(this);
    }
}
