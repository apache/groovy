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
 * Represents a local variable name, the simplest form of expression. e.g.&#160;"foo".
 */
public class VariableExpression extends Expression implements Variable {
    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final VariableExpression THIS_EXPRESSION = new VariableExpression("this", ClassHelper.DYNAMIC_TYPE);
    public static final VariableExpression SUPER_EXPRESSION = new VariableExpression("super", ClassHelper.DYNAMIC_TYPE);

    private final String variable;
    private int modifiers;
    private boolean inStaticContext;
    private boolean isDynamicTyped=false;
    private Variable accessedVariable;
    boolean closureShare=false;
    boolean useRef=false;
    private final ClassNode originType;

    public Variable getAccessedVariable() {
        return accessedVariable;
    }

    public void setAccessedVariable(Variable origin) {
        this.accessedVariable = origin;
    }

    public VariableExpression(String variable, ClassNode type) {
        this.variable = variable;
        originType = type;
        setType(ClassHelper.getWrapper(type));
    }
    
    public VariableExpression(String variable) {
        this(variable, ClassHelper.DYNAMIC_TYPE);
    }
    
    public VariableExpression(Variable variable) {
        this(variable.getName(), variable.getOriginType());
        setAccessedVariable(variable);
        setModifiers(variable.getModifiers());
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitVariableExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    @Override
    public String getText() {
        return variable;
    }
    
    @Override
    public String getName() {
        return variable;
    }

    public String toString() {
        return super.toString() + "[variable: " + variable + (this.isDynamicTyped() ? "" : " type: " + getType()) + "]";
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
    public boolean isInStaticContext() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.isInStaticContext();
        return inStaticContext;
    }
    
    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    /**
     * Set the type of this variable. If you call this method from an AST transformation and that
     * the {@link #getAccessedVariable() accessed variable} is ({@link #isClosureSharedVariable() shared},
     * this operation is unsafe and may lead to a verify error at compile time. Instead, set the type of
     * the {@link #getAccessedVariable() accessed variable}
     * @param cn the type to be set on this variable
     */
    @Override
    public void setType(ClassNode cn){
        super.setType(cn);
        isDynamicTyped |= ClassHelper.DYNAMIC_TYPE==cn;
    }
    
    @Override
    public boolean isDynamicTyped() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.isDynamicTyped();
        return isDynamicTyped;
    }

    /**
     * Tells if this variable or the accessed variable is used in a closure context, like in the following
     * example :
     * <pre>def str = 'Hello'
     * def cl = { println str }
     * </pre>
     * The "str" variable is closure shared.
     * @return true if this variable is used in a closure
     */
    @Override
    public boolean isClosureSharedVariable() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.isClosureSharedVariable();
        return closureShare;
    }

    /**
     * Use this method to tell if a variable is used in a closure, like in the following example:
     * <pre>def str = 'Hello'
     * def cl = { println str }
     * </pre>
     * The "str" variable is closure shared. The variable expression inside the closure references an
     * accessed variable "str" which must have the closure shared flag set.
     * @param inClosure tells if this variable is later referenced in a closure
     */
    @Override
    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;        
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * For internal use only. This flag is used by compiler internals and should probably
     * be converted to a node metadata in future.
     * @param useRef
     */
    public void setUseReferenceDirectly(boolean useRef) {
        this.useRef = useRef;        
    }
    
    /**
     * For internal use only. This flag is used by compiler internals and should probably
     * be converted to a node metadata in future.
     */
    public boolean isUseReferenceDirectly() {
        return useRef;
    }
    
    @Override
    public ClassNode getType() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.getType();
        return super.getType();
    }

    /**
     * Returns the type which was used when this variable expression was created. For example,
     * {@link #getType()} may return a boxed type while this method would return the primitive type.
     * @return the type which was used to define this variable expression
     */
    @Override
    public ClassNode getOriginType() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.getOriginType();
        return originType;
    }

    public boolean isThisExpression() {
        return "this".equals(variable);
    }

    public boolean isSuperExpression() {
        return "super".equals(variable);
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
}
