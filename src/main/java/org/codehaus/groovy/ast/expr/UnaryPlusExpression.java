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

/**
 * A unary plus expression, rendered as {@code +(operand)}. The type of
 * this node is the type of the operand.
 */
public class UnaryPlusExpression extends Expression {

    private final Expression expression;

    /**
     * Creates a unary plus over {@code expression}.
     *
     * @param expression operand of the plus
     */
    public UnaryPlusExpression(final Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the operand of this plus.
     *
     * @return the operand
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public String getText() {
        return "+(" + getExpression().getText() + ")";
    }

    @Override
    public ClassNode getType() {
        return getExpression().getType(); // TODO: promote byte, char and short to int
    }

    /**
     * Reports that the operand is not a dynamic expression. This
     * implementation always returns false.
     *
     * @return {@code false}
     */
    @Deprecated
    public boolean isDynamic() {
        return false;
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new UnaryPlusExpression(transformer.transform(getExpression()));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitUnaryPlusExpression(this);
    }
}
