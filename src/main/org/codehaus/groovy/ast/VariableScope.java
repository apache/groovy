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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    private Map<String, Variable> declaredVariables = Collections.emptyMap();
    private Map<String, Variable> referencedLocalVariables = Collections.emptyMap();
    private Map<String, Variable> referencedClassVariables = Collections.emptyMap();
 
    private boolean inStaticContext = false;
    private boolean resolvesDynamic = false; 
    // Non-null iff this scope corresponds to a class, as opposed to a method, "if" statement,
    // block statement, etc.
    private ClassNode clazzScope;
    private VariableScope parent;

    public VariableScope() {
    }
    public VariableScope(VariableScope parent) {
        this.parent = parent;
    }

    public Variable getDeclaredVariable(String name) {
        return declaredVariables.get(name);
    }

    public boolean isReferencedLocalVariable(String name) {
        return referencedLocalVariables.containsKey(name);
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

    public void setClassScope(ClassNode node) {
        this.clazzScope = node;
    }
    
    /**
     * Non-null iff this scope corresponds to a class; as opposed to a method, "if" statement,
     * block statement, etc.
     */
    public ClassNode getClassScope(){
        return clazzScope;
    }
    
    /**
     * Returns true iff this scope corresponds to a class; as opposed to a method, "if" statement,
     * block statement, etc.
     */
    public boolean isClassScope(){
        return clazzScope!=null;
    }
    
    public boolean isRoot() {
        return parent==null;
    }
    
    public VariableScope copy() {
        VariableScope copy = new VariableScope();
        copy.clazzScope = clazzScope;
        if (declaredVariables.size() > 0) {
          copy.declaredVariables = new HashMap<String, Variable>();
          copy.declaredVariables.putAll(declaredVariables);
        }
        copy.inStaticContext = inStaticContext;
        copy.parent = parent;
        if (referencedClassVariables.size() > 0) {
            copy.referencedClassVariables = new HashMap<String, Variable>();
            copy.referencedClassVariables.putAll(referencedClassVariables);
        }
        if (referencedLocalVariables.size() > 0) {
            copy.referencedLocalVariables = new HashMap<String, Variable>();
            copy.referencedLocalVariables.putAll(referencedLocalVariables);
        }
        copy.resolvesDynamic = resolvesDynamic;
        return copy;
    }

    public void putDeclaredVariable(Variable var) {
        if (declaredVariables == Collections.EMPTY_MAP)
          declaredVariables = new HashMap<String, Variable>();
        declaredVariables.put(var.getName(), var);
    }

    public Iterator<Variable> getReferencedLocalVariablesIterator() {
        return referencedLocalVariables.values().iterator();
    }

    public int getReferencedLocalVariablesCount() {
        return referencedLocalVariables.size();
    }

    public Variable getReferencedLocalVariable(String name) {
        return referencedLocalVariables.get(name);
    }

    public void putReferencedLocalVariable(Variable var) {
        if (referencedLocalVariables == Collections.EMPTY_MAP)
          referencedLocalVariables = new HashMap<String, Variable>();
        referencedLocalVariables.put(var.getName(), var);
    }

    public void putReferencedClassVariable(Variable var) {
        if (referencedClassVariables == Collections.EMPTY_MAP)
          referencedClassVariables = new HashMap<String, Variable>();
        referencedClassVariables.put(var.getName(), var);
    }

    public Variable getReferencedClassVariable(String name) {
        return referencedClassVariables.get(name);
    }

    public Object removeReferencedClassVariable(String name) {
        if (referencedClassVariables == Collections.EMPTY_MAP)
          return null;
        else
          return referencedClassVariables.remove(name);
    }
    
    /**
     * Gets a map containing the class variables referenced 
     * by this scope. This not can not be modified.
     * @return a map containing the class variable references
     */
    public Map<String, Variable> getReferencedClassVariables() {
        if (referencedClassVariables == Collections.EMPTY_MAP) {
            return referencedClassVariables;
        } else {
            return Collections.unmodifiableMap(referencedClassVariables);
        }
    }
    
    /**
     * Gets an iterator for the referenced class variables. The
     * remove operation is not supported.
     * @return an iterator for the referenced class variables
     */
    public Iterator<Variable> getReferencedClassVariablesIterator() {
        return getReferencedClassVariables().values().iterator();
    }
}