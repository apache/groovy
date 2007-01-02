/*
 * UnreferencedVariable.java created on 14.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.Expression;

public class DynamicVariable implements Variable {

    private String name;
    private boolean closureShare = false;
    private boolean staticContext = false;
    
    public DynamicVariable(String name, boolean context) {
        this.name = name;
        staticContext = context;
    }
    
    public ClassNode getType() {
        return ClassHelper.DYNAMIC_TYPE;
    }

    public String getName() {
        return name;
    }

    public Expression getInitialExpression() {
        return null;
    }

    public boolean hasInitialExpression() {
        return false;
    }

    public boolean isInStaticContext() {
        return staticContext;
    }

    public boolean isDynamicTyped() {
        return true;
    }

    public boolean isClosureSharedVariable() {
        return closureShare;
    }

    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;        
    }

}
