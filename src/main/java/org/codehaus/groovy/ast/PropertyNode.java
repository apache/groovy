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
package org.codehaus.groovy.ast;

import groovy.lang.MetaProperty;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Represents a property (member variable, a getter and setter)
 */
public class PropertyNode extends AnnotatedNode implements Variable {

    private FieldNode field;
    private int modifiers;

    private Statement getterBlock;
    private Statement setterBlock;
    private String getterName;
    private String setterName;

    public PropertyNode(final String name, final int modifiers, final ClassNode type, final ClassNode owner, final Expression initialValueExpression, final Statement getterBlock, final Statement setterBlock) {
        this(new FieldNode(name, modifiers & ACC_STATIC, type, owner, initialValueExpression), modifiers, getterBlock, setterBlock);
    }

    public PropertyNode(final FieldNode field, final int modifiers, final Statement getterBlock, final Statement setterBlock) {
        this.field = field;
        this.modifiers = modifiers;
        this.getterBlock = getterBlock;
        this.setterBlock = setterBlock;
    }

    public FieldNode getField() {
        return field;
    }

    public void setField(final FieldNode field) {
        this.field = field;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    public Statement getGetterBlock() {
        return getterBlock;
    }

    public void setGetterBlock(final Statement getterBlock) {
        this.getterBlock = getterBlock;
    }

    public Statement getSetterBlock() {
        return setterBlock;
    }

    public void setSetterBlock(final Statement setterBlock) {
        this.setterBlock = setterBlock;
    }

    /**
     * @since 4.0.0
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * If an explicit getterName has been set, return that, otherwise return the default name for the property.
     * For a property {@code foo}, the default name is {@code getFoo} except for a boolean property where
     * {@code isFoo} is the default if no {@code getFoo} method exists in the declaring class.
     *
     * @since 4.0.0
     */
    public String getGetterNameOrDefault() {
        if (getterName != null) return getterName;
        String defaultName = "get" + capitalize(getName());
        if (ClassHelper.boolean_TYPE.equals(getOriginType())
                && !getDeclaringClass().hasMethod(defaultName, Parameter.EMPTY_ARRAY)) {
            defaultName = "is" + capitalize(getName());
        }
        return defaultName;
    }

    /**
     * @since 4.0.0
     */
    public void setGetterName(final String getterName) {
        if (getterName == null || getterName.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-null non-empty getter name is required");
        }
        this.getterName = getterName.trim();
    }

    /**
     * @since 4.0.0
     */
    public String getSetterName() {
        return setterName;
    }

    /**
     * @since 4.0.0
     */
    public String getSetterNameOrDefault() {
        return setterName != null ? setterName : MetaProperty.getSetterName(getName());
    }

    /**
     * @since 4.0.0
     */
    public void setSetterName(final String setterName) {
        if (setterName == null || setterName.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-null non-empty setter name is required");
        }
        this.setterName = setterName.trim();
    }

    //--------------------------------------------------------------------------

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public ClassNode getType() {
        return field.getType();
    }

    @Override
    public ClassNode getOriginType() {
        return field.getOriginType();
    }

    public void setType(final ClassNode t) {
        field.setType(t);
    }

    @Override
    public Expression getInitialExpression() {
        return field.getInitialExpression();
    }

    @Override
    public boolean hasInitialExpression() {
        return field.hasInitialExpression();
    }

    @Override
    public boolean isInStaticContext() {
        return field.isInStaticContext();
    }

    @Override
    public boolean isDynamicTyped() {
        return field.isDynamicTyped();
    }
}
