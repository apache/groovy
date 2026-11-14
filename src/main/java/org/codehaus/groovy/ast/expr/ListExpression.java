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

    /**
     * Creates an empty list expression. The element list can be filled
     * later with {@link #addExpression}.
     */
    public ListExpression() {
        this(new ArrayList<Expression>());
    }

    /**
     * Creates a list expression that holds {@code expressions} directly.
     * The type of this node is {@link List}.
     *
     * @param expressions elements of the list; later calls mutate this list
     */
    public ListExpression(List<Expression> expressions) {
        this.expressions = expressions;
        //TODO: get the type's of the expressions to specify the
        // list type to List<X> if possible.
        setType(ClassHelper.LIST_TYPE);
    }

    /**
     * Appends {@code expression} to the list held by this node.
     *
     * @param expression element to append
     */
    public void addExpression(Expression expression) {
        expressions.add(expression);
    }

    /**
     * Returns the element list held by this node. Callers may mutate it.
     *
     * @return the elements, in source order
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * Records whether this list was parsed as a bracket-delimited sequence
     * of elements. The parser sets this for a multi-element index, a
     * named-argument list, or an enum constant with more than one named
     * argument, and clears it for a one-element spread index. A newly
     * built list is not wrapped.
     *
     * @param value {@code true} when the list is that bracketed form
     */
    public void setWrapped(boolean value) {
        wrapped = value;
    }

    /**
     * Reports the flag stored by {@link #setWrapped}.
     *
     * @return {@code true} when this list is a bracket-delimited sequence
     */
    public boolean isWrapped() {
        return wrapped;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitListExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ListExpression(transformExpressions(getExpressions(), transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Returns the element at index {@code i}. An index outside the list fails.
     *
     * @param i zero-based index into {@link #getExpressions}
     * @return the element at that index
     */
    public Expression getExpression(int i) {
        return expressions.get(i);
    }

    @Override
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

    @Override
    public String toString() {
        return super.toString() + expressions;
    }
}
