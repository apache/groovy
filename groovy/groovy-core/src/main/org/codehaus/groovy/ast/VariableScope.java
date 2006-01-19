/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.ast;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a variable scope. This is primarily used to determine variable sharing
 * across method and closure boundaries.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Jochen Theodorou
 * @version $Revision$
 */
public class VariableScope  {
    private Map declaredVariables = new HashMap();
    private Map referencedLocalVariables = new HashMap();
    private Map referencedClassVariables = new HashMap();
 
    private boolean inStaticContext = false;
    private boolean resolvesDynamic = false; 
    private ClassNode clazzScope;
    private VariableScope parent;

    public VariableScope() {
    }
    public VariableScope(VariableScope parent) {
        this.parent = parent;
    }
    public Map getDeclaredVariables() {
        return declaredVariables;
    }
    public Variable getDeclaredVariable(String name) {
        return (Variable) declaredVariables.get(name);
    }
    public Map getReferencedLocalVariables() {
        return referencedLocalVariables;
    }
    
    public boolean isReferencedLocalVariable(String name) {
        return referencedLocalVariables.containsKey(name);
    }
    
    public Map getReferencedClassVariables() {
        return referencedClassVariables;
    }
    
    public boolean isReferencedClassVariable(String name) {
        return referencedClassVariables.containsKey(name);
    }
    public VariableScope getParent() {
        return parent;
    }

    public boolean isInStaticContext() {
        return inStaticContext;
    }

    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public boolean isResolvingDynamic() {
        return resolvesDynamic;
    }

    public void setDynamicResolving(boolean resolvesDynamic) {
        this.resolvesDynamic = resolvesDynamic;
    }

    public void setClassScope(ClassNode node) {
        this.clazzScope = node;
    }
    
    public ClassNode getClassScope(){
        return clazzScope;
    }
    
    public boolean isClassScope(){
        return clazzScope!=null;
    }
    
    public boolean isRoot() {
        return parent==null;
    }
    
    public VariableScope copy() {
        VariableScope copy = new VariableScope();
        copy.clazzScope = clazzScope;
        copy.declaredVariables.putAll(declaredVariables);
        copy.inStaticContext = inStaticContext;
        copy.parent = parent;
        copy.referencedClassVariables.putAll(referencedClassVariables);
        copy.referencedLocalVariables.putAll(referencedLocalVariables);
        copy.resolvesDynamic = resolvesDynamic;
        return copy;
    }
}