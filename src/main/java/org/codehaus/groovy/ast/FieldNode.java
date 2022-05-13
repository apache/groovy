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

    public static FieldNode newStatic(Class theClass, String name) throws SecurityException, NoSuchFieldException {
        Field field = theClass.getField(name);
        ClassNode fldType = ClassHelper.make(field.getType());
        return new FieldNode(name, ACC_PUBLIC | ACC_STATIC, fldType, ClassHelper.make(theClass), null);
    }

    protected FieldNode() {}

    public FieldNode(String name, int modifiers, ClassNode type, ClassNode owner, Expression initialValueExpression) {
        this.name = name;
        this.modifiers = modifiers;
        this.setType(type);
        this.owner = owner;
        this.initialValueExpression = initialValueExpression;
    }

    @Override
    public Expression getInitialExpression() {
        return initialValueExpression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
        this.originType = type;
        dynamicTyped |= ClassHelper.isDynamicTyped(type);
    }

    public ClassNode getOwner() {
        return owner;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean holder) {
        this.holder = holder;
    }

    @Override
    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * @return true if the field is an enum
     */
    public boolean isEnum() {
        return (getModifiers() & ACC_ENUM) != 0;
    }

    /**
     * @param owner The owner to set.
     */
    public void setOwner(ClassNode owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasInitialExpression() {
        return initialValueExpression != null;
    }

    @Override
    public boolean isInStaticContext() {
        return isStatic();
    }

    public Expression getInitialValueExpression() {
        return initialValueExpression;
    }

    public void setInitialValueExpression(Expression initialValueExpression) {
        this.initialValueExpression = initialValueExpression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().getName().equals("org.codehaus.groovy.ast.decompiled.LazyFieldNode")) {
            return obj.equals(this);
        }
        return super.equals(obj);
    }

    @Override
    public ClassNode getOriginType() {
        return originType;
    }

    public void setOriginType(ClassNode cn) {
        originType = cn;
    }

    public void rename(String name) {
        getDeclaringClass().renameField(this.name, name);
        this.name = name;
    }
}
