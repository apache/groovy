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

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a method pointer on an object such as
 * {@code foo.&bar} which means find the method pointer for the {@code bar} method on the {@code foo} instance.
 * This is equivalent to:
 * <code>
 * foo.metaClass.getMethodPointer(foo, "bar")
 * </code>
 */
public class MethodPointerExpression extends Expression {
    protected final Expression expression;
    protected final Expression methodName;

    public MethodPointerExpression(Expression expression, Expression methodName) {
        this.expression = expression;
        this.methodName = methodName;
        setType(ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
    }

    public Expression getExpression() {
        if (expression == null)
            return VariableExpression.THIS_EXPRESSION;
        else
            return expression;
    }

    public Expression getMethodName() {
        return methodName;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodPointerExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret;
        Expression mname = transformer.transform(methodName);
        if (expression == null) {
            ret = new MethodPointerExpression(VariableExpression.THIS_EXPRESSION, mname);
        } else {
            ret = new MethodPointerExpression(transformer.transform(expression), mname);
        }
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        if (expression == null) {
            return "&" + methodName;
        } else {
            return expression.getText() + ".&" + methodName.getText();
        }
    }

    public boolean isDynamic() {
        return false;
    }

    public Class getTypeClass() {
        return Closure.class;
    }
}
