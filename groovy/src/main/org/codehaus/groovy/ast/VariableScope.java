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