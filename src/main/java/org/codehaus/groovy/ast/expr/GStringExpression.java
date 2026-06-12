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
 * Represents a GString (interpolated string) expression containing embedded values.
 * A GString consists of constant string parts and interpolated expression values that are evaluated at runtime.
 * For example, {@code "hello there ${user} how are you"} contains constant strings and user variable expressions.
 * The type of a GString expression is always {@link ClassHelper#GSTRING_TYPE}.
 * GStrings are expanded lazily during execution.
 * 
 * @see {@link ConstantExpression} for string constant parts
 * @see {@link Expression} for interpolated expressions
 * @see {@link ClassHelper#GSTRING_TYPE} for the GString type
 */
public class GStringExpression extends Expression {

    /**
     * The original verbatim text representation of the GString.
     */
    private final String verbatimText;
    /**
     * The constant string parts of the GString (alternating with values).
     */
    private final List<ConstantExpression> strings;
    /**
     * The interpolated expressions to be evaluated at runtime.
     */
    private final List<Expression> values;

    /**
     * Creates an empty GString expression with the given verbatim text.
     * 
     * @param verbatimText the original text representation (non-null)
     */
    public GStringExpression(String verbatimText) {
        this.verbatimText = verbatimText;
        super.setType(ClassHelper.GSTRING_TYPE);
        this.strings = new ArrayList<ConstantExpression>();
        this.values = new ArrayList<Expression>();
    }

    /**
     * Creates a GString expression with pre-populated strings and values.
     * 
     * @param verbatimText the original text representation (non-null)
     * @param strings the list of constant string parts (non-null)
     * @param values the list of interpolated expressions (non-null)
     */
    public GStringExpression(String verbatimText, List<ConstantExpression> strings, List<Expression> values) {
        this.verbatimText = verbatimText;
        this.strings = strings;
        this.values = values;
        super.setType(ClassHelper.GSTRING_TYPE);
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitGStringExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new GStringExpression(
                verbatimText,
                transformExpressions(strings, transformer, ConstantExpression.class),
                transformExpressions(values, transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String toString() {
        return super.toString() + "[strings: " + strings + " values: " + values + "]";
    }

    @Override
    public String getText() {
        return verbatimText;
    }

    /**
     * Returns the constant string parts of this GString.
     * These alternate with the interpolated values in the final string.
     * 
     * @return a list of constant string expressions (non-null)
     */
    public List<ConstantExpression> getStrings() {
        return strings;
    }

    /**
     * Returns the interpolated expressions in this GString.
     * These are evaluated at runtime and converted to strings.
     * 
     * @return a list of value expressions (non-null)
     */
    public List<Expression> getValues() {
        return values;
    }

    /**
     * Adds a constant string part to this GString.
     * 
     * @param text the constant string expression to add (non-null)
     * @throws NullPointerException if text is null
     */
    public void addString(ConstantExpression text) {
        if (text == null) {
            throw new NullPointerException("Cannot add a null text expression");
        }
        strings.add(text);
    }

    /**
     * Adds an interpolated expression (value) to this GString.
     * If this is the first value being added, an empty string constant is prepended to maintain alternation.
     * 
     * @param value the expression to add (non-null)
     */
    public void addValue(Expression value) {
        // If the first thing is a value, then we need a dummy empty string in front of it so that when we
        // toString it they come out in the correct order.
        if (strings.isEmpty())
            strings.add(ConstantExpression.EMPTY_STRING);
        values.add(value);
    }

    /**
     * Returns the interpolated expression at the specified index.
     * 
     * @param idx the index of the value
     * @return the expression at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Expression getValue(int idx) {
        return values.get(idx);
    }

    /**
     * Indicates whether this GString contains no interpolated values (only constant strings).
     * 
     * @return true if there are no interpolated values, false if there are embedded expressions
     */
    public boolean isConstantString() {
        return values.isEmpty();
    }

    /**
     * Converts this GString to a constant string expression by concatenating all string parts.
     * This is only valid if {@link #isConstantString()} returns true.
     * 
     * @return a {@link ConstantExpression} with the complete concatenated string value
     */
    public Expression asConstantString() {
        StringBuilder buffer = new StringBuilder();
        for (ConstantExpression expression : strings) {
            Object value = expression.getValue();
            if (value != null) {
                buffer.append(value);
            }
        }
        return new ConstantExpression(buffer.toString());
    }
}
