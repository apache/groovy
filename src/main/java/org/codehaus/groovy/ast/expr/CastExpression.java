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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.Objects;

/**
 * Represents a type cast expression in Groovy.
 * Supports both explicit casts (e.g., {@code (String) obj}) and coercion expressions (e.g., {@code obj as String}).
 * The expression can operate in strict mode (using Java CHECKCAST) or loose mode (using Groovy coercion semantics).
 * Autoboxing may be ignored for certain optimizations.
 * 
 * @see {@link Expression} for the target expression being cast
 * @see {@link ClassNode} for the target type
 */
public class CastExpression extends Expression {

    /**
     * The expression being cast.
     */
    private Expression expression;
    /**
     * Whether to ignore autoboxing/unboxing conversions during the cast.
     */
    private final boolean ignoreAutoboxing;

    /**
     * Whether this is a coercion (using {@code as}) rather than a strict cast.
     */
    private boolean coerce;
    /**
     * Whether to use strict Java cast (CHECKCAST) instead of Groovy coercion.
     */
    private boolean strict;

    /**
     * Creates a cast expression with coercion semantics using the {@code as} operator.
     * 
     * @param type the target type for coercion (non-null)
     * @param expression the expression being coerced
     * @return a cast expression configured for coercion
     */
    public static CastExpression asExpression(final ClassNode type, final Expression expression) {
        CastExpression answer = new CastExpression(type, expression);
        answer.setCoerce(true);
        return answer;
    }

    /**
     * Creates a cast expression for the specified type and expression.
     * 
     * @param type the target type for the cast (non-null)
     * @param expression the expression being cast
     */
    public CastExpression(final ClassNode type, final Expression expression) {
        this(type, expression, false);
    }

    /**
     * Creates a cast expression with optional autoboxing control.
     * 
     * @param type the target type for the cast (non-null)
     * @param expression the expression being cast
     * @param ignoreAutoboxing whether to ignore autoboxing/unboxing conversions
     */
    public CastExpression(final ClassNode type, final Expression expression, final boolean ignoreAutoboxing) {
        this.expression = expression;
        this.ignoreAutoboxing = ignoreAutoboxing;
        super.setType(Objects.requireNonNull(type));
    }

    /**
     * Returns the expression being cast.
     * 
     * @return the source expression (may be null)
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the expression being cast.
     * 
     * @param expression the new expression
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Indicates whether autoboxing is ignored for this cast.
     * 
     * @return true if autoboxing should be ignored, false otherwise
     */
    public boolean isIgnoringAutoboxing() {
        return ignoreAutoboxing;
    }

    /**
     * Indicates whether this is a coercion expression (using {@code as}) rather than a strict cast.
     * 
     * @return true if this uses coercion semantics, false for explicit casting
     */
    public boolean isCoerce() {
        return coerce;
    }

    /**
     * Sets whether this expression uses coercion (loose Groovy semantics) or strict casting.
     * 
     * @param coerce true for coercion, false for explicit cast
     */
    public void setCoerce(final boolean coerce) {
        this.coerce = coerce;
    }

    /**
     * Indicates whether this uses strict Java cast (CHECKCAST bytecode) instead of Groovy coercion.
     * In strict mode, the compiler disables Groovy coercion semantics and relies on Java's type checking.
     * 
     * @return true if strict casting is enabled, false otherwise
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Sets strict casting mode.
     * If true, the compiler generates a strict Java cast (CHECKCAST) and disables Groovy coercion semantics.
     * 
     * @param strict true for strict Java-like casting, false for Groovy coercion
     */
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getText() + "]";
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitCastExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        CastExpression ret = new CastExpression(getType(), transformer.transform(expression), isIgnoringAutoboxing());
        ret.setCoerce(this.isCoerce());
        ret.setStrict(this.isStrict());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        if (isCoerce()) {
            return expression.getText() + " as " + getType().toString(false);
        }
        return "(" + getType().toString(false) + ") " + expression.getText();
    }

    /**
     * Overridden to prevent changing the target type after construction.
     * 
     * @throws UnsupportedOperationException always, as the type is fixed at construction
     */
    @Override
    public void setType(final ClassNode type) {
        throw new UnsupportedOperationException();
    }
}
