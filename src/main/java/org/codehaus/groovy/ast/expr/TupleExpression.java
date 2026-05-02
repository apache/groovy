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

/**
 * Represents a tuple or grouped list of expressions, typically used for method arguments,
 * multiple assignment targets, or other contexts requiring multiple values.
 * A tuple expression is a simple container for an ordered list of {@link Expression} objects
 * and implements {@link Iterable} for convenient traversal.
 * 
 * @see {@link Expression} for the contained expressions
 * @see {@link MethodCallExpression} for usage in method arguments
 * @see {@link ConstructorCallExpression} for usage in constructor arguments
 */
public class TupleExpression extends Expression implements Iterable<Expression> {

    /**
     * The list of expressions contained in this tuple.
     */
    private final List<Expression> expressions;

    /**
     * Creates an empty tuple expression with zero initial capacity.
     */
    public TupleExpression() {
        this(0);
    }

    /**
     * Creates a tuple expression containing a single expression.
     * 
     * @param expr the expression to add (non-null)
     */
    public TupleExpression(final Expression expr) {
        this(1);
        addExpression(expr);
    }

    /**
     * Creates a tuple expression containing two expressions.
     * 
     * @param expr1 the first expression (non-null)
     * @param expr2 the second expression (non-null)
     */
    public TupleExpression(final Expression expr1, final Expression expr2) {
        this(2);
        addExpression(expr1);
        addExpression(expr2);
    }

    /**
     * Creates a tuple expression containing three expressions.
     * 
     * @param expr1 the first expression (non-null)
     * @param expr2 the second expression (non-null)
     * @param expr3 the third expression (non-null)
     */
    public TupleExpression(final Expression expr1, final Expression expr2, final Expression expr3) {
        this(3);
        addExpression(expr1);
        addExpression(expr2);
        addExpression(expr3);
    }

    /**
     * Creates a tuple expression with pre-allocated capacity.
     * 
     * @param capacity the initial capacity for the internal list
     */
    public TupleExpression(final int capacity) {
        this.expressions = new ArrayList<>(capacity);
    }

    /**
     * Creates a tuple expression from an existing list of expressions.
     * 
     * @param expressions the list of expressions (non-null)
     */
    public TupleExpression(final List<Expression> expressions) {
        this.expressions = expressions;
    }

    /**
     * Creates a tuple expression from an array of expressions.
     * 
     * @param expressionArray the array of expressions (non-null)
     */
    public TupleExpression(final Expression[] expressionArray) {
        this(expressionArray.length);
        expressions.addAll(Arrays.asList(expressionArray));
    }

    /**
     * Adds an expression to this tuple.
     * 
     * @param expression the expression to add (non-null)
     * @return this tuple expression for method chaining
     */
    public TupleExpression addExpression(final Expression expression) {
        expressions.add(expression);
        return this;
    }

    /**
     * Returns the expression at the specified index.
     * 
     * @param i the index of the expression
     * @return the expression at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Expression getExpression(final int i) {
        return expressions.get(i);
    }

    /**
     * Returns the list of all expressions in this tuple.
     * Note: The returned list may be mutable; modifications affect this tuple.
     * 
     * @return a list of expressions (non-null)
     */
    public List<Expression> getExpressions() {
        return expressions;
        // TODO: return Collections.unmodifiableList(expressions);
        // see also org.codehaus.groovy.ast.expr.MethodCallExpression.NO_ARGUMENTS
    }

    /**
     * Returns an unmodifiable iterator over the expressions in this tuple.
     * 
     * @return an iterator of expressions
     */
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

    @Override
    public String toString() {
        return super.toString() + getExpressions();
    }
}
