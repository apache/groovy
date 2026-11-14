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

    /**
     * Shared empty object array for a call that passes no arguments.
     */
    public static final Object[] EMPTY_ARRAY = {
    };

    public static final ArgumentListExpression EMPTY_ARGUMENTS = new ArgumentListExpression();

    /**
     * Creates an argument list with no expressions.
     */
    public ArgumentListExpression() {
    }

    /**
     * Creates an argument list from the supplied expressions, in order.
     *
     * @param expressions the argument expressions; the list is stored by the superclass
     */
    public ArgumentListExpression(List<Expression> expressions) {
        super(expressions);
    }

    /**
     * Creates an argument list from the supplied expressions, in order.
     *
     * @param expressions the argument expressions
     */
    public ArgumentListExpression(Expression[] expressions) {
        super(expressions);
    }

    /**
     * Creates an argument list of variable references, one per parameter, in order.
     *
     * @param parameters parameters turned into {@link VariableExpression}s
     */
    public ArgumentListExpression(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            addExpression(new VariableExpression(parameter));
        }
    }

    /**
     * Creates an argument list holding a single expression.
     *
     * @param expr the sole argument
     */
    public ArgumentListExpression(Expression expr) {
        super(expr);
    }

    /**
     * Creates an argument list holding two expressions, in the given order.
     *
     * @param expr1 the first argument
     * @param expr2 the second argument
     */
    public ArgumentListExpression(Expression expr1, Expression expr2) {
        super(expr1, expr2);
    }

    /**
     * Creates an argument list holding three expressions, in the given order.
     *
     * @param expr1 the first argument
     * @param expr2 the second argument
     * @param expr3 the third argument
     */
    public ArgumentListExpression(Expression expr1, Expression expr2, Expression expr3) {
        super(expr1, expr2, expr3);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ArgumentListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArgumentlistExpression(this);
    }
}
