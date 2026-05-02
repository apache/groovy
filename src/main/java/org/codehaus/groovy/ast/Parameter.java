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

import static org.objectweb.asm.Opcodes.ACC_MANDATED;

/**
 * Represents a parameter in a method or constructor declaration.
 * Parameters support optional type information (defaults to {@code java.lang.Object} if not specified),
 * default values for optional parameters, and closure variable sharing.
 * Parameters implement the {@link Variable} interface to integrate with variable scope tracking.
 *
 * @see Variable
 * @see MethodNode
 * @see ConstructorNode
 */
public class Parameter extends AnnotatedNode implements Variable {

    public static final Parameter[] EMPTY_ARRAY = {};

    private ClassNode type;
    private final String name;
    private ClassNode originType;
    private boolean dynamicTyped;
    private boolean closureShare;
    private Expression defaultValue;
    private boolean hasDefaultValue;
    private boolean inStaticContext;
    private int modifiers;

    /**
     * Creates a parameter with the specified type and name.
     *
     * @param type the {@link ClassNode} representing the parameter's type
     * @param name the parameter name (never null)
     */
    public Parameter(ClassNode type, String name) {
        this.name = name;
        this.setType(type);
        this.originType = type;
    }

    /**
     * Creates a parameter with a type, name, and default value expression.
     * The parameter is marked as having a default value which makes it optional.
     *
     * @param type the {@link ClassNode} representing the parameter's type
     * @param name the parameter name (never null)
     * @param defaultValue an {@link Expression} providing the default value for this optional parameter
     */
    public Parameter(ClassNode type, String name, Expression defaultValue) {
        this(type, name);
        this.setInitialExpression(defaultValue);
    }

    @Override
    public String toString() {
        return super.toString() + "[name: " + name + (type == null ? "" : ", type: " + type.toString(false)) + ", hasDefaultValue: " + this.hasInitialExpression() + "]";
    }

    /**
     * Returns the parameter name.
     *
     * @return the name of this parameter
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this parameter.
     * If the type has not been set, a dynamic type is returned.
     *
     * @return the {@link ClassNode} representing this parameter's type
     */
    @Override
    public ClassNode getType() {
        return type;
    }

    /**
     * Sets the type of this parameter. If the type is dynamically typed,
     * the parameter is marked as dynamically typed.
     *
     * @param type the {@link ClassNode} representing this parameter's type
     * @see ClassHelper#isDynamicTyped(ClassNode)
     */
    public void setType(ClassNode type) {
        this.type = type;
        dynamicTyped = dynamicTyped || ClassHelper.isDynamicTyped(type);
    }

    /**
     * Returns the default value expression for this parameter, or null if no default is specified.
     *
     * @return the default value {@link Expression}, or null
     */
    public Expression getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns true if this parameter has been assigned a default value expression.
     *
     * @return true if a default value is present
     */
    @Override
    public boolean hasInitialExpression() {
        return hasDefaultValue;
    }

    /**
     * Returns the default value expression for this parameter, or null if no default is specified.
     * This is an alias for {@link #getDefaultValue()}.
     *
     * @return the default value {@link Expression}, or null
     */
    @Override
    public Expression getInitialExpression() {
        return defaultValue;
    }

    /**
     * Sets the default value expression for this optional parameter.
     * If the expression is null, the parameter is marked as not having a default value.
     *
     * @param init the default value {@link Expression}, or null to remove a default value
     */
    public void setInitialExpression(final Expression init) {
        defaultValue = init;
        hasDefaultValue = (init != null);
    }

    /**
     * Returns true if this parameter is declared in a static context (static method or static initializer).
     *
     * @return true if in a static context
     */
    @Override
    public boolean isInStaticContext() {
        return inStaticContext;
    }

    /**
     * Marks this parameter as being declared in a static context.
     *
     * @param inStaticContext true if this parameter is in a static context
     */
    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    /**
     * Returns true if this parameter has dynamic type information, meaning its type
     * could not be determined at compile time.
     *
     * @return true if dynamically typed
     */
    @Override
    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    /**
     * Returns true if this parameter is a closure-shared variable, meaning it is captured
     * and accessed by nested closures.
     *
     * @return true if shared with closures
     */
    @Override
    public boolean isClosureSharedVariable() {
        return closureShare;
    }

    /**
     * Marks this parameter as being shared with nested closures, affecting how it is handled
     * in bytecode generation and variable access patterns.
     *
     * @param inClosure true if this parameter is shared with closures
     */
    @Override
    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;
    }

    /**
     * Returns the modifiers (access flags) for this parameter as per {@code org.objectweb.asm.Opcodes}.
     * May include flags like {@code ACC_FINAL} or {@code ACC_MANDATED}.
     *
     * @return the modifier flags
     * @see org.objectweb.asm.Opcodes
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Sets the modifiers (access flags) for this parameter.
     *
     * @param modifiers the modifier flags from {@code org.objectweb.asm.Opcodes}
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Returns the original type of this parameter before any wrapping of primitive types.
     * This preserves the distinction between primitive and boxed types.
     *
     * @return the original {@link ClassNode} before primitive wrapping
     */
    @Override
    public ClassNode getOriginType() {
        return originType;
    }

    /**
     * Sets the original type of this parameter before primitive type wrapping.
     *
     * @param cn the original {@link ClassNode}
     */
    public void setOriginType(ClassNode cn) {
        originType = cn;
    }

    /**
     * Returns true if this parameter is implicit in the source code, such as the receiver parameter
     * in instance methods or parameters generated by the compiler. This is determined by the
     * {@code ACC_MANDATED} modifier flag as per {@code java.lang.reflect.Parameter#isImplicit()}.
     *
     * @return true if this is an implicit parameter
     * @see java.lang.reflect.Parameter#isImplicit()
     * @since 5.0.0
     */
    public boolean isImplicit() {
        return (getModifiers() & ACC_MANDATED) != 0;
    }

    /**
     * Returns true if this parameter represents a receiver parameter (named "this")
     * as specified by JSR 308. Receiver parameters allow annotations on instance method receivers.
     *
     * @return true if this is a receiver parameter
     * @since 5.0.0
     */
    public boolean isReceiver() {
        return "this".equals(getName()); // JSR 308
    }
}
