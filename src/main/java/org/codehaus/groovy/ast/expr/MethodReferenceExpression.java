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

/**
 * Represents a method reference or a constructor reference,
 * e.g. System.out::println OR Objects::requireNonNull OR Integer::new OR int[]::new
 */
public class MethodReferenceExpression extends MethodPointerExpression {
    public MethodReferenceExpression(Expression expression, Expression methodName) {
        super(expression, methodName);
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodReferenceExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret;
        Expression mname = transformer.transform(methodName);
        if (expression == null) {
            ret = new MethodReferenceExpression(VariableExpression.THIS_EXPRESSION, mname);
        } else {
            ret = new MethodReferenceExpression(transformer.transform(expression), mname);
        }
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        Expression expression = this.getExpression();
        Expression methodName = this.getMethodName();

        if (expression == null) {
            return "::" + methodName;
        } else {
            return expression.getText() + "::" + methodName.getText();
        }
    }
}
