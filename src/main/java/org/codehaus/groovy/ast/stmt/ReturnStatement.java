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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;

import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;

/**
 * A return statement
 */
public class ReturnStatement extends Statement {
    /**
     * Only used for synthetic return statements emitted by the compiler.
     * For comparisons use isReturningNullOrVoid() instead.
     */
    public static final ReturnStatement RETURN_NULL_OR_VOID = new ReturnStatement(nullX());

    private Expression expression;

    public ReturnStatement(final ExpressionStatement statement) {
        this(statement.getExpression());
        copyStatementLabels(statement);
    }

    public ReturnStatement(final Expression expression) {
        setExpression(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public String getText() {
        return "return " + expression.getText();
    }

    public boolean isReturningNullOrVoid() {
        return expression instanceof ConstantExpression
            && ((ConstantExpression) expression).isNullExpression();
    }

    @Override
    public String toString() {
        return super.toString() + "[expression:" + expression + "]";
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitReturnStatement(this);
    }
}
