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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a variable scope. This is primarily used to determine variable sharing
 * across method and closure boundaries.
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
        if (!declaredVariables.isEmpty()) {
          copy.declaredVariables = new LinkedHashMap<String, Variable>(declaredVariables);
        }
        copy.inStaticContext = inStaticContext;
        copy.parent = parent;
        if (!referencedClassVariables.isEmpty()) {
            copy.referencedClassVariables = new LinkedHashMap<String, Variable>(referencedClassVariables);
        }
        if (!referencedLocalVariables.isEmpty()) {
            copy.referencedLocalVariables = new LinkedHashMap<String, Variable>(referencedLocalVariables);
        }
        copy.resolvesDynamic = resolvesDynamic;
        return copy;
    }

    public void putDeclaredVariable(Variable var) {
        if (declaredVariables == Collections.EMPTY_MAP)
          declaredVariables = new LinkedHashMap<String, Variable>();
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
          referencedLocalVariables = new LinkedHashMap<String, Variable>();
        referencedLocalVariables.put(var.getName(), var);
    }

    public void putReferencedClassVariable(Variable var) {
        if (referencedClassVariables == Collections.EMPTY_MAP)
          referencedClassVariables = new LinkedHashMap<String, Variable>();
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

    /**
     * Gets a map containing the variables declared in this scope.
     * This map cannot be modified.
     * @return a map containing the declared variable references
     */
    public Map<String, Variable> getDeclaredVariables() {
        if (declaredVariables == Collections.EMPTY_MAP) {
            return declaredVariables;
        } else {
            return Collections.unmodifiableMap(declaredVariables);
        }
    }

    /**
     * Gets an iterator for the declared class variables. The remove
     * operation is not supported.
     * @return an iterator for the declared variables
     */
    public Iterator<Variable> getDeclaredVariablesIterator() {
        return getDeclaredVariables().values().iterator();
    }
}
