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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Variable;

/**
 * Represents a local variable reference, the simplest form of expression (e.g., "foo").
 * Variables can refer to local variables, parameters, fields, or special variables like {@code this}
 * and {@code super}. A variable expression may reference an accessed variable (either a local variable
 * or field), track closure sharing and context information, and support dynamic typing. Each user-defined
 * variable expression maintains its own instance to preserve line information for accurate error reporting.
 *
 * @see Expression
 * @see Variable
 * @see BinaryExpression
 * @see ClosureExpression
 */
public class VariableExpression extends Expression implements Cloneable, Variable {

    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final VariableExpression THIS_EXPRESSION = new VariableExpression("this");
    public static final VariableExpression SUPER_EXPRESSION = new VariableExpression("super");

    private int modifiers;
    private final String variable;
    private Variable accessedVariable;
    private final ClassNode originType;
    private boolean inStaticContext, isDynamicTyped, closureShare, useRef;

    /**
     * Creates a variable expression with the specified name and type annotation.
     *
     * @param name the variable name; must not be null
     * @param type the {@link ClassNode} representing the declared type of this variable; may be null
     *             for dynamically typed variables or if type inference is needed
     */
    public VariableExpression(final String name, final ClassNode type) {
        variable = name;
        originType = type;
        setType(ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type);
    }

    /**
     * Creates a variable expression with the specified name. The type is initially set to dynamic.
     *
     * @param name the variable name; must not be null
     */
    public VariableExpression(final String name) {
        this(name, ClassHelper.dynamicType());
    }

    /**
     * Creates a variable expression that references an existing {@link Variable}. This captures
     * the variable's name, type, modifiers, and marks this expression as accessing that variable.
     *
     * @param av the {@link Variable} to reference; must not be null
     */
    public VariableExpression(final Variable av) {
        this(av.getName(), av.getOriginType());
        setModifiers(av.getModifiers());
        setAccessedVariable(av);
    }

    /**
     * Creates a copy of this variable expression, preserving all attributes including
     * accessed variable, modifiers, closure sharing state, static context, and metadata.
     *
     * @return a clone of this {@link VariableExpression} with all properties copied
     */
    @Override
    public VariableExpression clone() {
        var copy = new VariableExpression(variable, originType);
        copy.setAccessedVariable(accessedVariable);
        copy.setModifiers(modifiers);

        copy.setClosureSharedVariable(closureShare);
        copy.setInStaticContext(inStaticContext);
        copy.setUseReferenceDirectly(useRef);

        copy.setSynthetic(isSynthetic());
        copy.setType(super.getType());
        copy.setSourcePosition(this);
        copy.copyNodeMetaData(this);

        return copy;
    }

    /**
     * Sets the variable that this expression accesses. This is typically used to link a variable
     * expression to the actual variable definition (local variable, field, or parameter).
     *
     * @param variable the {@link Variable} being accessed; may be null if this is not accessing
     *                 an existing variable
     */
    public void setAccessedVariable(final Variable variable) {
        accessedVariable = variable;
    }

    /**
     * Marks this variable as being shared within a closure context. Closure-shared variables
     * are accessible from within closure bodies and must be handled specially during compilation.
     * Example: in the code {@code def str = 'Hello'; def cl = { println str }}, the "str" variable
     * is closure-shared because it is referenced inside the closure.
     *
     * @param inClosure if true, marks this variable as referenced from a closure; false otherwise
     */
    @Override
    public void setClosureSharedVariable(final boolean inClosure) {
        closureShare = inClosure;
    }

    /**
     * Sets whether this variable is accessed in a static context.
     *
     * @param inStaticContext if true, indicates this variable is in a static context; false otherwise
     */
    public void setInStaticContext(final boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    /**
     * Sets the access modifiers for this variable (e.g., public, private, protected).
     *
     * @param modifiers the access modifier flags from java.lang.reflect.Modifier
     */
    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Sets the declared type for this variable. If this variable {@link #getAccessedVariable() accesses}
     * a {@link #isClosureSharedVariable() shared variable}, modifying this variable's type is unsafe and may
     * lead to verification errors at compile time. In such cases, set the type on the
     * {@link #getAccessedVariable() accessed variable} instead.
     *
     * @param type the {@link ClassNode} representing the variable's type; must not be null
     */
    @Override
    public void setType(final ClassNode type) {
        super.setType(type);
        isDynamicTyped |= ClassHelper.isDynamicTyped(type);
    }

    /**
     * For internal compiler use only. This flag indicates whether to use the variable reference
     * directly without dereferencing. This is used by compiler internals and should probably
     * be converted to node metadata in the future.
     *
     * @param useRef if true, use the reference directly; false otherwise
     */
    public void setUseReferenceDirectly(final boolean useRef) {
        this.useRef = useRef;
    }

    /**
     * Returns the variable that this expression accesses. If not explicitly set, returns null,
     * indicating this expression represents a standalone variable reference without a linked definition.
     *
     * @return the accessed {@link Variable}, or null if this is not accessing another variable
     */
    public Variable getAccessedVariable() {
        return accessedVariable;
    }

    /**
     * Returns null because variable expressions do not have initial expressions.
     * Initial expressions are associated with {@link Variable} instances, not expressions.
     *
     * @return always null
     */
    @Override
    public Expression getInitialExpression() {
        return null;
    }

    /**
     * Indicates that this variable expression does not have an initial expression.
     *
     * @return always false
     */
    @Override
    public boolean hasInitialExpression() {
        return false;
    }

    /**
     * Returns the access modifiers for this variable.
     *
     * @return the modifier flags as defined in java.lang.reflect.Modifier
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Returns the variable name represented by this expression.
     *
     * @return the variable name; never null
     */
    @Override
    public String getName() {
        return variable;
    }

    /**
     * Returns the variable name as a string representation of this expression.
     *
     * @return the variable name
     */
    @Override
    public String getText() {
        return variable;
    }

    /**
     * Returns the type of this variable. If this variable accesses another variable, returns the
     * accessed variable's type. Otherwise returns the type set on this expression.
     *
     * @return the {@link ClassNode} representing the variable's type; never null (defaults to dynamic type)
     */
    @Override
    public ClassNode getType() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.getType();
        }
        return super.getType();
    }

    /**
     * Returns the original type used when this variable expression was created. For example,
     * {@link #getType()} may return a boxed type while this method returns the primitive type.
     * If this variable accesses another variable, returns the accessed variable's origin type.
     *
     * @return the original {@link ClassNode} representing the variable's declared type; may be null
     *         for dynamically typed variables
     */
    @Override
    public ClassNode getOriginType() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.getOriginType();
        }
        return originType;
    }

    /**
     * Indicates whether this variable or the accessed variable is shared in a closure context.
     * Closure-shared variables are accessible from within closure bodies.
     * Example: in the code {@code def str = 'Hello'; def cl = { println str }}, this would return
     * true for the "str" variable expression inside the closure.
     *
     * @return true if this variable is accessed from a closure; false otherwise
     */
    @Override
    public boolean isClosureSharedVariable() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isClosureSharedVariable();
        }
        return closureShare;
    }

    /**
     * Indicates whether this variable or the accessed variable uses dynamic typing.
     * Dynamically typed variables have their types resolved at runtime rather than compile time.
     *
     * @return true if this variable is dynamically typed; false otherwise
     */
    @Override
    public boolean isDynamicTyped() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isDynamicTyped();
        }
        return isDynamicTyped;
    }

    /**
     * Indicates whether this variable or the accessed variable is accessed in a static context.
     * Static context variables cannot reference instance variables or methods.
     *
     * @return true if this variable is in a static context; false otherwise
     */
    @Override
    public boolean isInStaticContext() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isInStaticContext();
        }
        return inStaticContext;
    }

    /**
     * Indicates whether this variable expression represents the {@code super} keyword.
     *
     * @return true if this is a super expression; false otherwise
     */
    public boolean isSuperExpression() {
        return "super".equals(variable);
    }

    /**
     * Indicates whether this variable expression represents the {@code this} keyword.
     *
     * @return true if this is a this expression; false otherwise
     */
    public boolean isThisExpression() {
        return "this".equals(variable);
    }

    /**
     * For internal compiler use only. Returns whether to use the variable reference directly
     * without dereferencing. This flag is used by compiler internals and should probably be
     * converted to node metadata in the future.
     *
     * @return true if using reference directly; false otherwise
     */
    public boolean isUseReferenceDirectly() {
        return useRef;
    }

    @Override
    public String toString() {
        return super.toString() + "[variable: " + getName() + (isDynamicTyped() ? "" : " type: " + getType()) + "]";
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        return this;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitVariableExpression(this);
    }
}
