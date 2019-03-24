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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list expression [1, 2, 3] which creates a mutable List
 */
public class ListExpression extends Expression {
    private final List<Expression> expressions;
    private boolean wrapped = false;

    public ListExpression() {
        this(new ArrayList<Expression>());
    }

    public ListExpression(List<Expression> expressions) {
        this.expressions = expressions;
        //TODO: get the type's of the expressions to specify the
        // list type to List<X> if possible.
        setType(ClassHelper.LIST_TYPE);
    }

    public void addExpression(Expression expression) {
        expressions.add(expression);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setWrapped(boolean value) {
        wrapped = value;
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitListExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public Expression getExpression(int i) {
        return expressions.get(i);
    }

    public String getText() {
        StringBuilder buffer = new StringBuilder("[");
        boolean first = true;
        for (Expression expression : expressions) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }

            buffer.append(expression.getText());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String toString() {
        return super.toString() + expressions;
    }
}
