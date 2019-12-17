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
 * Records declared and referenced variabes for a given scope.  Helps determine
 * variable sharing across closure and method boundaries.
 */
public class VariableScope {

    private VariableScope parent;
    private ClassNode classScope;
    private boolean inStaticContext;

    private Map<String, Variable> declaredVariables = Collections.emptyMap();
    private Map<String, Variable> referencedLocalVariables = Collections.emptyMap();
    private Map<String, Variable> referencedClassVariables = Collections.emptyMap();

    public VariableScope() {
        super();
    }

    public VariableScope(final VariableScope parent) {
        this.parent = parent;
    }

    public VariableScope getParent() {
        return parent;
    }

    public boolean isRoot() {
        return (parent == null);
    }

    /**
     * Non-null iff this scope corresponds to a class; as opposed to a method, "if" statement, block statement, etc.
     */
    public ClassNode getClassScope() {
        return classScope;
    }

    /**
     * Returns true iff this scope corresponds to a class; as opposed to a method, "if" statement, block statement, etc.
     */
    public boolean isClassScope() {
        return (classScope != null);
    }

    public void setClassScope(final ClassNode classScope) {
        this.classScope = classScope;
    }

    public boolean isInStaticContext() {
        return inStaticContext;
    }

    public void setInStaticContext(final boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    //

    public Variable getDeclaredVariable(final String name) {
        return declaredVariables.get(name);
    }

    public Variable getReferencedLocalVariable(final String name) {
        return referencedLocalVariables.get(name);
    }

    public Variable getReferencedClassVariable(final String name) {
        return referencedClassVariables.get(name);
    }

    public boolean isReferencedLocalVariable(final String name) {
        return referencedLocalVariables.containsKey(name);
    }

    public boolean isReferencedClassVariable(final String name) {
        return referencedClassVariables.containsKey(name);
    }

    /**
     * Gets a map containing the variables declared in this scope. This map cannot be modified.
     *
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
     * Gets a map containing the class variables referenced by this scope. This not can not be modified.
     *
     * @return a map containing the class variable references
     */
    public Map<String, Variable> getReferencedClassVariables() {
        if (referencedClassVariables == Collections.EMPTY_MAP) {
            return referencedClassVariables;
        } else {
            return Collections.unmodifiableMap(referencedClassVariables);
        }
    }

    public int getReferencedLocalVariablesCount() {
        return referencedLocalVariables.size();
    }

    /**
     * Gets an iterator for the declared class variables. The remove operation is not supported.
     *
     * @return an iterator for the declared variables
     */
    public Iterator<Variable> getDeclaredVariablesIterator() {
        return getDeclaredVariables().values().iterator();
    }

    /**
     * Gets an iterator for the referenced local variables. The remove operation *is* supported.
     *
     * @return an iterator for the referenced local variables
     */
    public Iterator<Variable> getReferencedLocalVariablesIterator() {
        return referencedLocalVariables.values().iterator();
    }

    /**
     * Gets an iterator for the referenced class variables. The remove operation is not supported.
     *
     * @return an iterator for the referenced class variables
     */
    public Iterator<Variable> getReferencedClassVariablesIterator() {
        return getReferencedClassVariables().values().iterator();
    }

    public void putDeclaredVariable(final Variable var) {
        if (declaredVariables == Collections.EMPTY_MAP)
            declaredVariables = new LinkedHashMap<>();
        declaredVariables.put(var.getName(), var);
    }

    public void putReferencedLocalVariable(final Variable var) {
        if (referencedLocalVariables == Collections.EMPTY_MAP)
            referencedLocalVariables = new LinkedHashMap<>();
        referencedLocalVariables.put(var.getName(), var);
    }

    public void putReferencedClassVariable(final Variable var) {
        if (referencedClassVariables == Collections.EMPTY_MAP)
            referencedClassVariables = new LinkedHashMap<>();
        referencedClassVariables.put(var.getName(), var);
    }

    public Object removeReferencedClassVariable(final String name) {
        if (referencedClassVariables.isEmpty()) {
            return null;
        } else {
            return referencedClassVariables.remove(name);
        }
    }

    //

    public VariableScope copy() {
        VariableScope that = new VariableScope(parent);
        that.classScope = this.classScope;
        that.inStaticContext = this.inStaticContext;
        if (!this.declaredVariables.isEmpty()) {
            that.declaredVariables = new LinkedHashMap<>(this.declaredVariables);
        }
        if (!this.referencedLocalVariables.isEmpty()) {
            that.referencedLocalVariables = new LinkedHashMap<>(this.referencedLocalVariables);
        }
        if (!this.referencedClassVariables.isEmpty()) {
            that.referencedClassVariables = new LinkedHashMap<>(this.referencedClassVariables);
        }
        return that;
    }
}
