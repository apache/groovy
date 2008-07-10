/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Variable;

/**
 * Represents a local variable name, the simplest form of expression. e.g.&nbsp;"foo".
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VariableExpression extends Expression implements Variable {
    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final VariableExpression THIS_EXPRESSION = new VariableExpression("this", ClassHelper.DYNAMIC_TYPE);
    public static final VariableExpression SUPER_EXPRESSION = new VariableExpression("super", ClassHelper.DYNAMIC_TYPE);

    private String variable;
    private boolean inStaticContext;
    private boolean isDynamicTyped=false;
    private Variable accessedVariable;
    boolean closureShare=false;
    private ClassNode originType;

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
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitVariableExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    public String getText() {
        return variable;
    }
    
    public String getName() {
        return variable;
    }

    /**
     * @return true if this variable is dynamically typed
     */
    public String toString() {
        return super.toString() + "[variable: " + variable + (this.isDynamicTyped() ? "" : " type: " + getType()) + "]";
    }

    public Expression getInitialExpression() {
        return null;
    }

    public boolean hasInitialExpression() {
        return false;
    }
    
    public boolean isInStaticContext() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.isInStaticContext();
        return inStaticContext;
    }
    
    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public void setType(ClassNode cn){
        super.setType(cn);
        isDynamicTyped |= ClassHelper.DYNAMIC_TYPE==cn;
    }
    
    public boolean isDynamicTyped() {
        return isDynamicTyped;
    }

    public boolean isClosureSharedVariable() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.isClosureSharedVariable();
        return closureShare;
    }
    
    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;        
    }
    
    public ClassNode getType() {
        if (accessedVariable!=null && accessedVariable!=this) return accessedVariable.getType();
        return super.getType();
    }
    
    public ClassNode getOriginType() {
    	return originType;
    }

    public boolean isThisExpression() {
        return "this".equals(variable);
    }

    public boolean isSuperExpression() {
        return "super".equals(variable);
    }
}
