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
package org.apache.groovy.ginq.dsl.expression;

import org.apache.groovy.ginq.dsl.GinqAstVisitor;

/**
 * Represents a set operation (union, unionall, intersect, minus) combining two GINQ expressions.
 *
 * @since 6.0.0
 */
public class SetOperationExpression extends AbstractGinqExpression {

    private final AbstractGinqExpression left;
    private final String operation;
    private final GinqExpression right;

    /**
     * Creates a set-operation expression.
     *
     * @param left the left query
     * @param operation the set-operation name
     * @param right the right query
     */
    public SetOperationExpression(AbstractGinqExpression left, String operation, GinqExpression right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    /**
     * Returns the left query.
     *
     * @return the left query
     */
    public AbstractGinqExpression getLeft() {
        return left;
    }

    /**
     * Returns the operation name.
     *
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns the right query.
     *
     * @return the right query
     */
    public GinqExpression getRight() {
        return right;
    }

    /**
     * Accepts a visitor for this expression.
     *
     * @param visitor the visitor to accept
     * @param <R> the visit result type
     * @return the visit result
     */
    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitSetOperationExpression(this);
    }

    /**
     * Returns the textual GINQ form of this expression.
     *
     * @return the expression text
     */
    @Override
    public String getText() {
        return left.getText() + " " + operation + " " + right.getText();
    }

    /**
     * Returns the textual form of this expression.
     *
     * @return the expression text
     */
    @Override
    public String toString() {
        return getText();
    }
}
