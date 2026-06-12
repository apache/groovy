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

    /**
     * Creates a property node representing a Java property (field + getter/setter).
     * The property is synthesized from a field name, type, and optionally initial value expression,
     * with getter and setter blocks.
     *
     * @param name the property name
     * @param modifiers ASM modifier flags (applied to the property, with static flag removed from field)
     * @param type the property type as a {@link ClassNode}
     * @param owner the {@link ClassNode} that declares this property
     * @param initialValueExpression optional initial value expression
     * @param getterBlock optional statement block for the getter method body
     * @param setterBlock optional statement block for the setter method body
     */
    public PropertyNode(final String name, final int modifiers, final ClassNode type, final ClassNode owner, final Expression initialValueExpression, final Statement getterBlock, final Statement setterBlock) {
        this(new FieldNode(name, modifiers & ACC_STATIC, type, owner, initialValueExpression), modifiers, getterBlock, setterBlock);
    }

    /**
     * Creates a property node from an existing field and getter/setter blocks.
     * This constructor allows properties to be built from pre-existing field definitions.
     *
     * @param field the {@link FieldNode} backing this property
     * @param modifiers ASM modifier flags for the property
     * @param getterBlock optional statement block for the getter method body
     * @param setterBlock optional statement block for the setter method body
     */
    public PropertyNode(final FieldNode field, final int modifiers, final Statement getterBlock, final Statement setterBlock) {
        this.field = field;
        this.modifiers = modifiers;
        this.getterBlock = getterBlock;
        this.setterBlock = setterBlock;
    }

    /**
     * Returns the backing field node for this property.
     *
     * @return the {@link FieldNode} associated with this property
     */
    public FieldNode getField() {
        return field;
    }

    /**
     * Sets the backing field node for this property.
     *
     * @param field the {@link FieldNode} to associate with this property
     */
    public void setField(final FieldNode field) {
        this.field = field;
    }

    /**
     * Returns the ASM modifier flags for this property.
     *
     * @return ASM opcode flags representing this property's modifiers
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Sets the ASM modifier flags for this property.
     *
     * @param modifiers ASM opcode flags to set
     */
    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Returns the statement block that implements the getter method.
     * Returns null if no explicit getter implementation is provided.
     *
     * @return the getter block statement, or null
     */
    public Statement getGetterBlock() {
        return getterBlock;
    }

    /**
     * Sets the statement block that implements the getter method.
     *
     * @param getterBlock the getter implementation statement
     */
    public void setGetterBlock(final Statement getterBlock) {
        this.getterBlock = getterBlock;
    }

    /**
     * Returns the statement block that implements the setter method.
     * Returns null if no explicit setter implementation is provided.
     *
     * @return the setter block statement, or null
     */
    public Statement getSetterBlock() {
        return setterBlock;
    }

    /**
     * Sets the statement block that implements the setter method.
     *
     * @param setterBlock the setter implementation statement
     */
    public void setSetterBlock(final Statement setterBlock) {
        this.setterBlock = setterBlock;
    }

    /**
     * Returns the explicitly set getter method name for this property.
     * Returns null if no explicit name has been set (use {@link #getGetterNameOrDefault()} instead).
     *
     * @return the explicit getter name, or null if not set
     * @since 4.0.0
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * Sets an explicit getter method name for this property.
     * Overrides the default naming convention (getFoo/isFoo).
     * Throws IllegalArgumentException if the name is null or empty.
     *
     * @param getterName the getter method name (non-null, non-empty)
     * @throws IllegalArgumentException if name is null or empty
     * @since 4.0.0
     */
    public void setGetterName(final String getterName) {
        if (getterName == null || getterName.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-null non-empty getter name is required");
        }
        this.getterName = getterName.trim();
    }

    /**
     * Returns the explicitly set setter method name for this property.
     * Returns null if no explicit name has been set (use {@link #getSetterNameOrDefault()} instead).
     *
     * @return the explicit setter name, or null if not set
     * @since 4.0.0
     */
    public String getSetterName() {
        return setterName;
    }

    /**
     * Sets an explicit setter method name for this property.
     * Overrides the default naming convention (setFoo).
     * Throws IllegalArgumentException if the name is null or empty.
     *
     * @param setterName the setter method name (non-null, non-empty)
     * @throws IllegalArgumentException if name is null or empty
     * @since 4.0.0
     */
    public void setSetterName(final String setterName) {
        if (setterName == null || setterName.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-null non-empty setter name is required");
        }
        this.setterName = setterName.trim();
    }

    /**
     * Returns the property name (delegated to backing field).
     *
     * @return the property identifier
     */
    @Override
    public String getName() {
        return field.getName();
    }

    /**
     * Returns the property type (delegated to backing field).
     *
     * @return the property's {@link ClassNode} type
     */
    @Override
    public ClassNode getType() {
        return field.getType();
    }

    /**
     * Returns the original property type before any transformations (delegated to backing field).
     *
     * @return the original {@link ClassNode} type
     */
    @Override
    public ClassNode getOriginType() {
        return field.getOriginType();
    }

    /**
     * Sets the property type (updates backing field's type).
     *
     * @param t the new property {@link ClassNode} type
     */
    public void setType(final ClassNode t) {
        field.setType(t);
    }

    /**
     * Returns the initial value expression for this property (delegated to backing field).
     *
     * @return the initial value {@link Expression}, or null if absent
     */
    @Override
    public Expression getInitialExpression() {
        return field.getInitialExpression();
    }

    /**
     * Checks whether this property has an initial value expression (delegated to backing field).
     *
     * @return true if an initializer expression is present
     */
    @Override
    public boolean hasInitialExpression() {
        return field.hasInitialExpression();
    }

    /**
     * Checks whether this property is in a static context (delegated to backing field).
     *
     * @return true if the backing field is static
     */
    @Override
    public boolean isInStaticContext() {
        return field.isInStaticContext();
    }

    /**
     * Checks whether this property has dynamic typing (delegated to backing field).
     *
     * @return true if the backing field's type is dynamically typed
     */
    @Override
    public boolean isDynamicTyped() {
        return field.isDynamicTyped();
    }

    /**
     * Returns the default getter method name for this property.
     * If an explicit name has been set, returns that; otherwise returns the conventional name.
     * For a property {@code foo}, the default name is {@code getFoo} except for a boolean property where
     * {@code isFoo} is the default if no {@code getFoo} method exists in the declaring class.
     *
     * @return the getter method name (either explicit or default)
     * @since 4.0.0
     */
    public String getGetterNameOrDefault() {
        if (getterName != null) return getterName;
        String defaultName = "get" + capitalize(getName());
        if (ClassHelper.isPrimitiveBoolean(getOriginType())
                && !getDeclaringClass().hasMethod(defaultName, Parameter.EMPTY_ARRAY)) {
            defaultName = "is" + capitalize(getName());
        }
        return defaultName;
    }

    /**
     * Returns the default setter method name for this property.
     * If an explicit name has been set, returns that; otherwise returns the conventional name.
     * For a property {@code foo}, the default name is {@code setFoo}.
     *
     * @return the setter method name (either explicit or default)
     * @since 4.0.0
     */
    public String getSetterNameOrDefault() {
        return setterName != null ? setterName : MetaProperty.getSetterName(getName());
    }
}
