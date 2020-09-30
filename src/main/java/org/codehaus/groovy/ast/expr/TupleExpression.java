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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TupleExpression extends Expression implements Iterable<Expression> {

    private final List<Expression> expressions;

    public TupleExpression() {
        this(0);
    }

    public TupleExpression(final Expression expr) {
        this(1);
        addExpression(expr);
    }

    public TupleExpression(final Expression expr1, final Expression expr2) {
        this(2);
        addExpression(expr1);
        addExpression(expr2);
    }

    public TupleExpression(final Expression expr1, final Expression expr2, final Expression expr3) {
        this(3);
        addExpression(expr1);
        addExpression(expr2);
        addExpression(expr3);
    }

    public TupleExpression(final int capacity) {
        this.expressions = new ArrayList<>(capacity);
    }

    public TupleExpression(final List<Expression> expressions) {
        this.expressions = expressions;
    }

    public TupleExpression(final Expression[] expressionArray) {
        this(expressionArray.length);
        expressions.addAll(Arrays.asList(expressionArray));
    }

    public TupleExpression addExpression(final Expression expression) {
        expressions.add(expression);
        return this;
    }

    public Expression getExpression(final int i) {
        return expressions.get(i);
    }

    public List<Expression> getExpressions() {
        return expressions;
        // TODO: return Collections.unmodifiableList(expressions);
        // see also org.codehaus.groovy.ast.expr.MethodCallExpression.NO_ARGUMENTS
    }

    @Override
    public Iterator<Expression> iterator() {
        // TODO: return getExpressions().iterator();
        return Collections.unmodifiableList(expressions).iterator();
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitTupleExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new TupleExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        StringBuilder buffer = new StringBuilder("(");
        boolean first = true;
        for (Expression expression : getExpressions()) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(expression.getText());
        }
        buffer.append(")");
        return buffer.toString();
    }

    public String toString() {
        return super.toString() + getExpressions();
    }
}
