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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Objects;

/**
 * A utility class used to wrap an expression with additional metadata used by the type checker.
 * In particular, this is used to detect closure shared variables misuses. We need in some circumstances
 * to store the method call expression and its argument types.
 * <p>
 * The second pass also needs to find the same private members as the first pass, which depend on
 * where and through what the expression was checked, not only on the types assigned to the variable:
 * the declared type of the receiver variable, through which statically compiled code may use a private
 * member of that type although flow typing narrowed the variable to a subclass, and the class whose
 * code the expression is, which decides the private members it may use.
 */
class SecondPassExpression<T> {
    private final Expression expression;
    private final T data;
    private final ClassNode declaredReceiverType;
    private final ClassNode enclosingClass;

    SecondPassExpression(final Expression expression) {
        this(expression, null, null, null);
    }

    SecondPassExpression(final Expression expression, final T data) {
        this(expression, data, null, null);
    }

    /**
     * Wraps an expression for the second pass.
     *
     * @param expression the expression to check again
     * @param data the auxiliary data, such as the argument types of a call, or {@code null}
     * @param declaredReceiverType the declared type of the receiver variable through which the first pass
     *        found a private member of that type, or {@code null}, see {@link #getDeclaredReceiverType()}
     * @param enclosingClass the class whose code the first pass checked the expression in, or {@code null}
     */
    SecondPassExpression(final Expression expression, final T data, final ClassNode declaredReceiverType, final ClassNode enclosingClass) {
        this.data = data;
        this.expression = expression;
        this.declaredReceiverType = declaredReceiverType;
        this.enclosingClass = enclosingClass;
    }

    /**
     * Returns the class whose code the first pass checked the expression in,
     * which decides the private members it may use, or {@code null}.
     */
    ClassNode getEnclosingClass() {
        return enclosingClass;
    }

    /**
     * Returns the declared type of the receiver variable through which the first
     * pass found a private member of that type, or {@code null}.
     */
    ClassNode getDeclaredReceiverType() {
        return declaredReceiverType;
    }

    /**
     * Returns the auxiliary data captured for the second pass.
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the expression revisited during the second pass.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Compares the wrapped expression and auxiliary data. The declared receiver type and the enclosing
     * class are left out: both follow from the expression, which is one node of one class, with one
     * receiver variable, so two wrappers of the same expression never differ in them.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SecondPassExpression that = (SecondPassExpression) o;

        if (!Objects.equals(data, that.data)) return false;
        if (!expression.equals(that.expression)) return false;

        return true;
    }

    /**
     * Returns the hash code of the wrapped expression and auxiliary data, which {@link #equals} compares.
     */
    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
