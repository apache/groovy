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

import org.codehaus.groovy.ast.expr.Expression;

import java.lang.reflect.Field;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Represents a field (member variable)
 */
public class FieldNode extends AnnotatedNode implements Variable {

    private String name;
    private int modifiers;
    private ClassNode type;
    private ClassNode owner;
    private Expression initialValueExpression;
    private boolean dynamicTyped;
    private boolean holder;
    private ClassNode originType;

    /**
     * Creates a new FieldNode wrapping a reflection Field from a Java class.
     * Extracts type information from the reflected field and creates an AST representation
     * with public static access modifiers matching the source field.
     *
     * @param theClass the class containing the reflected field
     * @param name the field name
     * @return a new FieldNode representing the reflected field
     * @throws SecurityException if field access is denied
     * @throws NoSuchFieldException if the field does not exist
     */
    public static FieldNode newStatic(Class theClass, String name) throws SecurityException, NoSuchFieldException {
        Field field = theClass.getField(name);
        ClassNode fldType = ClassHelper.make(field.getType());
        return new FieldNode(name, ACC_PUBLIC | ACC_STATIC, fldType, ClassHelper.make(theClass), null);
    }

    /**
     * Creates a field node representing a class member variable.
     * The field's type can be dynamically determined based on the provided ClassNode.
     *
     * @param name the field name
     * @param modifiers ASM modifier flags (public, static, final, etc.)
     * @param type the field's {@link ClassNode} type
     * @param owner the {@link ClassNode} that declares this field
     * @param initialValueExpression optional initial value expression (null for no initializer)
     */
    public FieldNode(String name, int modifiers, ClassNode type, ClassNode owner, Expression initialValueExpression) {
        this.name = name;
        this.modifiers = modifiers;
        this.setType(type);
        this.owner = owner;
        this.initialValueExpression = initialValueExpression;
    }

    protected FieldNode() {}

    /**
     * Returns the initial value expression for this field.
     * The expression is evaluated when the field is initialized.
     * Returns null if no initializer is present.
     *
     * @return the {@link Expression} providing the initial value, or null
     */
    @Override
    public Expression getInitialExpression() {
        return initialValueExpression;
    }

    /**
     * Returns the field name.
     *
     * @return the field's identifier
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the declared type of this field as a {@link ClassNode}.
     * The type may be dynamically determined or may reference generics.
     *
     * @return the field's type node
     */
    @Override
    public ClassNode getType() {
        return type;
    }

    /**
     * Sets the field's type.
     * Updates both the current type and tracks the origin type.
     * If the type is dynamic, marks this field as dynamically typed.
     *
     * @param type the field's {@link ClassNode} type
     */
    public void setType(ClassNode type) {
        this.type = type;
        this.originType = type;
        dynamicTyped |= ClassHelper.isDynamicTyped(type);
    }

    /**
     * Returns the class that declares this field.
     *
     * @return the declaring {@link ClassNode}, or null if not yet set
     */
    public ClassNode getOwner() {
        return owner;
    }

    /**
     * Checks whether this field is a holder field.
     * Holder fields are used internally for certain compilation strategies.
     *
     * @return true if this field is marked as a holder
     */
    public boolean isHolder() {
        return holder;
    }

    /**
     * Marks this field as a holder field for internal compilation purposes.
     *
     * @param holder true to mark as a holder field
     */
    public void setHolder(boolean holder) {
        this.holder = holder;
    }

    /**
     * Checks whether this field's type is dynamically typed (unresolved type).
     * Dynamic typing occurs when the exact type cannot be determined at compile time.
     *
     * @return true if this field uses dynamic typing
     */
    @Override
    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    /**
     * Returns the ASM modifier flags for this field.
     * Flags include visibility (public/protected/private), static, final, transient, volatile, etc.
     *
     * @return ASM opcode flags representing this field's modifiers
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Sets the ASM modifier flags for this field.
     * Allows updating visibility or other modifier flags during compilation.
     *
     * @param modifiers ASM opcode flags to set
     */
    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Checks whether this field has an initial value expression.
     *
     * @return true if an initializer expression is present
     */
    @Override
    public boolean hasInitialExpression() {
        return initialValueExpression != null;
    }

    /**
     * Checks whether this field is in a static context.
     * A field is in a static context if it has the static modifier.
     *
     * @return true if this field is static
     */
    @Override
    public boolean isInStaticContext() {
        return isStatic();
    }

    /**
     * Checks whether this field is an enum constant.
     * Enum constants are fields with the ACC_ENUM access flag.
     *
     * @return true if this field is an enum constant
     */
    public boolean isEnum() {
        return (getModifiers() & ACC_ENUM) != 0;
    }

    /**
     * Sets the class that declares this field.
     * Used during AST construction to establish the ownership relationship.
     *
     * @param owner the declaring {@link ClassNode}
     */
    public void setOwner(ClassNode owner) {
        this.owner = owner;
    }

    /**
     * Returns the initial value expression for this field.
     * Provides direct access to the initializer expression if one is defined.
     *
     * @return the {@link Expression} providing initial value, or null if absent
     */
    public Expression getInitialValueExpression() {
        return initialValueExpression;
    }

    /**
     * Sets the initial value expression for this field.
     * The expression will be evaluated during field initialization.
     *
     * @param initialValueExpression the {@link Expression} for initialization, or null
     */
    public void setInitialValueExpression(Expression initialValueExpression) {
        this.initialValueExpression = initialValueExpression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && "org.codehaus.groovy.ast.decompiled.LazyFieldNode".equals(obj.getClass().getName())) {
            return obj.equals(this);
        }
        return super.equals(obj);
    }

    /**
     * Returns the original declared type of this field before any transformations.
     * Useful for preserving type information through compilation phases that may modify types.
     *
     * @return the {@link ClassNode} representing the original type
     */
    @Override
    public ClassNode getOriginType() {
        return originType;
    }

    /**
     * Sets the original type information for this field.
     * Typically set during type transformation to preserve original type metadata.
     *
     * @param cn the original {@link ClassNode} type
     */
    public void setOriginType(ClassNode cn) {
        originType = cn;
    }

    /**
     * Renames this field within its declaring class.
     * Updates both the AST node and the declaring class's field registry.
     *
     * @param name the new field name
     */
    public void rename(String name) {
        getDeclaringClass().renameField(this.name, name);
        this.name = name;
    }
}
