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
 * Represents a local variable, the simplest form of expression. e.g. "foo".
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

    //

    public VariableExpression(final String name, final ClassNode type) {
        variable = name;
        originType = type;
        setType(ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type);
    }

    public VariableExpression(final String name) {
        this(name, ClassHelper.dynamicType());
    }

    public VariableExpression(final Variable av) {
        this(av.getName(), av.getOriginType());
        setModifiers(av.getModifiers());
        setAccessedVariable(av);
    }

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

    //

    public void setAccessedVariable(final Variable variable) {
        accessedVariable = variable;
    }

    /**
     * Use this method to tell if a variable is used in a closure, like in the following example:
     * <pre>def str = 'Hello'
     * def cl = { println str }
     * </pre>
     * The "str" variable is closure shared. The variable expression inside the closure references an
     * accessed variable "str" which must have the closure shared flag set.
     *
     * @param inClosure indicates if this variable is referenced from a closure
     */
    @Override
    public void setClosureSharedVariable(final boolean inClosure) {
        closureShare = inClosure;
    }

    public void setInStaticContext(final boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Set the type of this variable. If you call this method from an AST transformation and that
     * the {@link #getAccessedVariable() accessed variable} is ({@link #isClosureSharedVariable() shared},
     * this operation is unsafe and may lead to a verify error at compile time. Instead, set the type of
     * the {@link #getAccessedVariable() accessed variable}
     */
    @Override
    public void setType(final ClassNode type) {
        super.setType(type);
        isDynamicTyped |= ClassHelper.isDynamicTyped(type);
    }

    /**
     * For internal use only. This flag is used by compiler internals and should probably
     * be converted to a node metadata in the future.
     */
    public void setUseReferenceDirectly(final boolean useRef) {
        this.useRef = useRef;
    }

    //--------------------------------------------------------------------------

    public Variable getAccessedVariable() {
        return accessedVariable;
    }

    @Override
    public Expression getInitialExpression() {
        return null;
    }

    @Override
    public boolean hasInitialExpression() {
        return false;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public String getName() {
        return variable;
    }

    @Override
    public String getText() {
        return variable;
    }

    @Override
    public ClassNode getType() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.getType();
        }
        return super.getType();
    }

    /**
     * Returns the type which was used when this variable expression was created. For example,
     * {@link #getType()} may return a boxed type while this method would return the primitive type.
     *
     * @return the type which was used to define this variable expression
     */
    @Override
    public ClassNode getOriginType() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.getOriginType();
        }
        return originType;
    }

    /**
     * Tells if this variable or the accessed variable is used in a closure context, like in the following
     * example :
     * <pre>def str = 'Hello'
     * def cl = { println str }
     * </pre>
     * The "str" variable is closure shared.
     *
     * @return true if this variable is used in a closure
     */
    @Override
    public boolean isClosureSharedVariable() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isClosureSharedVariable();
        }
        return closureShare;
    }

    @Override
    public boolean isDynamicTyped() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isDynamicTyped();
        }
        return isDynamicTyped;
    }

    @Override
    public boolean isInStaticContext() {
        if (accessedVariable != null && accessedVariable != this) {
            return accessedVariable.isInStaticContext();
        }
        return inStaticContext;
    }

    public boolean isSuperExpression() {
        return "super".equals(variable);
    }

    public boolean isThisExpression() {
        return "this".equals(variable);
    }

    /**
     * For internal use only. This flag is used by compiler internals and should
     * probably be converted to a node metadata in the future.
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
