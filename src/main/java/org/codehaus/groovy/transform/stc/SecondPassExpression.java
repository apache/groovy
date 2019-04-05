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

import org.codehaus.groovy.ast.expr.Expression;

import java.util.Objects;

/**
 * An utility class used to wrap an expression with additional metadata used by the type checker.
 * In particular, this is used to detect closure shared variables misuses. We need in some circumstances
 * to store the method call expression and its argument types.
 */
class SecondPassExpression<T> {
    private final Expression expression;
    private final T data;

    SecondPassExpression(final Expression expression) {
        this.expression = expression;
        this.data = null;
    }

    SecondPassExpression(final Expression expression, final T data) {
        this.data = data;
        this.expression = expression;
    }

    public T getData() {
        return data;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SecondPassExpression that = (SecondPassExpression) o;

        if (!Objects.equals(data, that.data)) return false;
        if (!expression.equals(that.expression)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
