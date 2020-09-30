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
 * Represents a constant expression such as null, true, false.
 */
public class ConstantExpression extends Expression {
    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final ConstantExpression NULL = new ConstantExpression(null);
    public static final ConstantExpression TRUE = new ConstantExpression(Boolean.TRUE);
    public static final ConstantExpression FALSE = new ConstantExpression(Boolean.FALSE);
    public static final ConstantExpression EMPTY_STRING = new ConstantExpression("");
    public static final ConstantExpression PRIM_TRUE = new ConstantExpression(Boolean.TRUE, true);
    public static final ConstantExpression PRIM_FALSE = new ConstantExpression(Boolean.FALSE, true);

    // the following fields are only used internally; there are no user-defined expressions of the same kind
    public static final ConstantExpression VOID = new ConstantExpression(Void.class);
    public static final ConstantExpression EMPTY_EXPRESSION = new ConstantExpression(null);

    private final Object value;
    private String constantName;

    public ConstantExpression(Object value) {
        this(value, false);
    }

    public ConstantExpression(Object value, boolean keepPrimitive) {
        this.value = value;
        if (value != null) {
            if (keepPrimitive) {
                if (value instanceof Integer) {
                    setType(ClassHelper.int_TYPE);
                } else if (value instanceof Long) {
                    setType(ClassHelper.long_TYPE);
                } else if (value instanceof Boolean) {
                    setType(ClassHelper.boolean_TYPE);
                } else if (value instanceof Double) {
                    setType(ClassHelper.double_TYPE);
                } else if (value instanceof Float) {
                    setType(ClassHelper.float_TYPE);
                } else if (value instanceof Character) {
                    setType(ClassHelper.char_TYPE);
                } else {
                    setType(ClassHelper.make(value.getClass()));
                }
                //TODO: more cases here
            } else {
                setType(ClassHelper.make(value.getClass()));
            }
        }
    }

    public String toString() {
        return super.toString() + "[" + value + "]";
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstantExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    /**
     * @return the value of this constant expression
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String getText() {
        return (value == null ? "null" : value.toString());
    }

    public String getConstantName() {
        return constantName;
    }

    public void setConstantName(String constantName) {
        this.constantName = constantName;
    }

    public boolean isNullExpression() {
        return value == null;
    }

    public boolean isTrueExpression() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalseExpression() {
        return Boolean.FALSE.equals(value);
    }

    public boolean isEmptyStringExpression() {
        return "".equals(value);
    }
}
