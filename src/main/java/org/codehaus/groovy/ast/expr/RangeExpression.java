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

/**
 * Represents a range expression for creating range objects with bounded endpoints.
 * Supports inclusive ranges (e.g., {@code 0..10}) and exclusive ranges (e.g., {@code 0..<10} or {@code <0..10}).
 * Range expressions are commonly used in for-loops (e.g., {@code for (i in 0..10)}) and other iteration contexts.
 * The type of a range expression is always {@link ClassHelper#RANGE_TYPE}.
 * 
 * @see {@link Expression} for the boundary expressions
 * @see {@link ClassHelper#RANGE_TYPE} for the range type
 */
public class RangeExpression extends Expression {
    /**
     * The starting value of the range.
     */
    private final Expression from;
    /**
     * The ending value of the range.
     */
    private final Expression to;
    /**
     * Whether the left endpoint is exclusive (i.e., using {@code <..}).
     */
    private final boolean exclusiveLeft;
    /**
     * Whether the right endpoint is exclusive (i.e., using {@code ..<}).
     */
    private final boolean exclusiveRight;

    /**
     * Creates an inclusive or exclusive range expression.
     * 
     * @param from the starting expression (non-null)
     * @param to the ending expression (non-null)
     * @param inclusive true for inclusive range (no exclusion), false for exclusive right endpoint
     */
    public RangeExpression(final Expression from, final Expression to, final boolean inclusive) {
        this(from, to, false, !inclusive);
    }

    /**
     * Creates a range expression with full control over inclusivity of both endpoints.
     * 
     * @param from the starting expression (non-null)
     * @param to the ending expression (non-null)
     * @param exclusiveLeft true if the left endpoint should be excluded (using {@code <..})
     * @param exclusiveRight true if the right endpoint should be excluded (using {@code ..<})
     */
    public RangeExpression(final Expression from, final Expression to, final boolean exclusiveLeft, final boolean exclusiveRight) {
        this.from = from; this.to = to;
        this.exclusiveLeft = exclusiveLeft;
        this.exclusiveRight = exclusiveRight;

        setType(ClassHelper.RANGE_TYPE.getPlainNodeReference());
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitRangeExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new RangeExpression(transformer.transform(getFrom()), transformer.transform(getTo()), isExclusiveLeft(), isExclusiveRight());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Returns the starting expression of the range.
     * 
     * @return the from expression
     */
    public Expression getFrom() {
        return from;
    }

    /**
     * Returns the ending expression of the range.
     * 
     * @return the to expression
     */
    public Expression getTo() {
        return to;
    }

    /**
     * Indicates whether this is an inclusive range (no exclusion on the right endpoint).
     * 
     * @return true if the range is inclusive on the right, false if exclusive
     */
    public boolean isInclusive() {
        return !isExclusiveRight();
    }

    /**
     * Indicates whether the left endpoint is exclusive (uses {@code <..}).
     * 
     * @return true if the left endpoint is excluded, false otherwise
     */
    public boolean isExclusiveLeft() {
        return exclusiveLeft;
    }

    /**
     * Indicates whether the right endpoint is exclusive (uses {@code ..<}).
     * 
     * @return true if the right endpoint is excluded, false otherwise
     */
    public boolean isExclusiveRight() {
        return exclusiveRight;
    }

    @Override
    public String getText() {
        return "(" + getFrom().getText() +
                (isExclusiveLeft() ? "<" : "") +
                ".." +
                (isExclusiveRight() ? "<" : "") +
                getTo().getText() + ")";
    }
}
