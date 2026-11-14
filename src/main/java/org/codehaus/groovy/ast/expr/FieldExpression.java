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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import static java.util.Objects.requireNonNull;

/**
 * Represents a field access such as the expression "this.foo".
 */
public class FieldExpression extends Expression {

    private final FieldNode field;
    private boolean useRef;

    /**
     * Creates an access to {@code field}.
     *
     * Fails with {@link NullPointerException} if {@code field} is {@code null}.
     *
     * @param field the field being read or written
     */
    public FieldExpression(final FieldNode field) {
        this.field = requireNonNull(field);
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitFieldExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        return this;
    }

    /**
     * The field this expression reads or writes.
     *
     * @return the field passed to the constructor
     */
    public FieldNode getField() {
        return field;
    }

    /**
     * The name of {@link #getField()}.
     *
     * @return that field's name
     */
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public String getText() {
        return "this." + getFieldName();
    }

    @Override
    public ClassNode getType() {
        return field.getType();
    }

    @Override
    public void setType(final ClassNode type) {
        field.setType(type);
    }

    /**
     * Whether the field's declared type is dynamic.
     *
     * @return what {@link FieldNode#isDynamicTyped()} reports for this field
     */
    public boolean isDynamicTyped() {
        return field.isDynamicTyped();
    }

    /**
     * Whether a holder field is read or written as the {@code Reference}
     * itself. {@code false}, the default, loads or stores the value inside that
     * reference.
     *
     * @return {@code true} when the reference wrapper is used directly
     */
    public boolean isUseReferenceDirectly() {
        return useRef;
    }

    /**
     * Records whether a holder field is loaded or stored as its
     * {@code Reference} instead of the value that reference contains.
     *
     * @param useRef {@code true} to use the reference wrapper directly
     */
    public void setUseReferenceDirectly(final boolean useRef) {
        this.useRef = useRef;
    }

    @Override
    public String toString() {
        return "field(" + getType().toString(false) + " " + getFieldName() + ")";
    }
}
