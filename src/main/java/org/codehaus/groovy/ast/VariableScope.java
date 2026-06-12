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
 * Manages variable scope tracking for a given code block, tracking declared and referenced variables
 * to determine variable sharing patterns across closure and method boundaries.
 * Maintains hierarchical scope information with parent references, supports both class-level and
 * statement-level scopes, and tracks static context for proper variable access semantics.
 *
 * @see Variable
 * @see org.codehaus.groovy.ast.expr.VariableExpression
 */
public class VariableScope {

    private VariableScope parent;
    private ClassNode classScope;
    private boolean inStaticContext;

    private Map<String, Variable> declaredVariables = Collections.emptyMap();
    private Map<String, Variable> referencedLocalVariables = Collections.emptyMap();
    private Map<String, Variable> referencedClassVariables = Collections.emptyMap();

    /**
     * Creates a root variable scope with no parent.
     */
    public VariableScope() {
        super();
    }

    /**
     * Creates a variable scope with a parent scope, enabling hierarchical scope traversal.
     *
     * @param parent the parent {@link VariableScope}, or null for a root scope
     */
    public VariableScope(final VariableScope parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent scope, or null if this is a root scope.
     *
     * @return the parent {@link VariableScope}, or null
     */
    public VariableScope getParent() {
        return parent;
    }

    /**
     * Returns true if this is a root scope (no parent).
     *
     * @return true if this scope has no parent
     */
    public boolean isRoot() {
        return (parent == null);
    }

    /**
     * Returns the {@link ClassNode} if this scope corresponds to a class body, or null
     * for method bodies, block statements, or other non-class scopes.
     *
     * @return the class scope {@link ClassNode}, or null if not a class scope
     */
    public ClassNode getClassScope() {
        return classScope;
    }

    /**
     * Returns true if this scope corresponds to a class body scope (as opposed to a method,
     * block statement, or other non-class scope).
     *
     * @return true if this is a class scope
     */
    public boolean isClassScope() {
        return (classScope != null);
    }

    /**
     * Sets the {@link ClassNode} for this scope if it represents a class body.
     *
     * @param classScope the class {@link ClassNode}
     */
    public void setClassScope(final ClassNode classScope) {
        this.classScope = classScope;
    }

    /**
     * Returns true if this scope is in a static context (static initializers, static methods, etc.).
     *
     * @return true if in a static context
     */
    public boolean isInStaticContext() {
        return inStaticContext;
    }

    /**
     * Marks this scope as being in a static context.
     *
     * @param inStaticContext true if in a static context
     */
    public void setInStaticContext(final boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    /**
     * Returns a variable declared in this scope by name, or null if not found.
     *
     * @param name the variable name
     * @return the {@link Variable}, or null if not declared in this scope
     */
    public Variable getDeclaredVariable(final String name) {
        return declaredVariables.get(name);
    }

    /**
     * Returns a local variable referenced by this scope by name, or null if not found.
     * Referenced local variables are those accessed from parent scopes.
     *
     * @param name the variable name
     * @return the {@link Variable}, or null if not referenced
     */
    public Variable getReferencedLocalVariable(final String name) {
        return referencedLocalVariables.get(name);
    }

    /**
     * Returns a class variable referenced by this scope by name, or null if not found.
     * Referenced class variables are those accessed from the class scope.
     *
     * @param name the variable name
     * @return the {@link Variable}, or null if not referenced
     */
    public Variable getReferencedClassVariable(final String name) {
        return referencedClassVariables.get(name);
    }

    /**
     * Returns true if the specified variable name is in the referenced local variables map.
     *
     * @param name the variable name
     * @return true if referenced locally
     */
    public boolean isReferencedLocalVariable(final String name) {
        return referencedLocalVariables.containsKey(name);
    }

    /**
     * Returns true if the specified variable name is in the referenced class variables map.
     *
     * @param name the variable name
     * @return true if referenced from the class scope
     */
    public boolean isReferencedClassVariable(final String name) {
        return referencedClassVariables.containsKey(name);
    }

    /**
     * Returns an unmodifiable map of variables declared in this scope.
     * The map is empty if no variables are declared.
     *
     * @return an unmodifiable map of declared variables
     */
    public Map<String, Variable> getDeclaredVariables() {
        if (declaredVariables == Collections.EMPTY_MAP) {
            return declaredVariables;
        } else {
            return Collections.unmodifiableMap(declaredVariables);
        }
    }

    /**
     * Returns an unmodifiable map of class variables referenced by this scope.
     * The map is empty if no class variables are referenced.
     *
     * @return an unmodifiable map of referenced class variables
     */
    public Map<String, Variable> getReferencedClassVariables() {
        if (referencedClassVariables == Collections.EMPTY_MAP) {
            return referencedClassVariables;
        } else {
            return Collections.unmodifiableMap(referencedClassVariables);
        }
    }

    /**
     * Returns the count of local variables referenced by this scope.
     *
     * @return the number of referenced local variables
     */
    public int getReferencedLocalVariablesCount() {
        return referencedLocalVariables.size();
    }

    /**
     * Returns an unmodifiable iterator over declared variables in this scope.
     * The remove operation is not supported.
     *
     * @return an iterator over declared variables
     */
    public Iterator<Variable> getDeclaredVariablesIterator() {
        return getDeclaredVariables().values().iterator();
    }

    /**
     * Returns a modifiable iterator over local variables referenced by this scope.
     * The remove operation is supported and removes the variable from the scope.
     *
     * @return an iterator over referenced local variables
     */
    public Iterator<Variable> getReferencedLocalVariablesIterator() {
        return referencedLocalVariables.values().iterator();
    }

    /**
     * Returns an unmodifiable iterator over class variables referenced by this scope.
     * The remove operation is not supported.
     *
     * @return an iterator over referenced class variables
     */
    public Iterator<Variable> getReferencedClassVariablesIterator() {
        return getReferencedClassVariables().values().iterator();
    }

    /**
     * Adds a variable as declared in this scope. If the declared variables map is not yet
     * initialized, it creates a linked hash map to preserve insertion order.
     *
     * @param var the {@link Variable} to declare
     */
    public void putDeclaredVariable(final Variable var) {
        if (declaredVariables == Collections.EMPTY_MAP)
            declaredVariables = new LinkedHashMap<>();
        declaredVariables.put(var.getName(), var);
    }

    /**
     * Adds a variable as a referenced local variable in this scope.
     * Referenced local variables are those accessed from parent or enclosing scopes.
     *
     * @param var the {@link Variable} to reference
     */
    public void putReferencedLocalVariable(final Variable var) {
        if (referencedLocalVariables == Collections.EMPTY_MAP)
            referencedLocalVariables = new LinkedHashMap<>();
        referencedLocalVariables.put(var.getName(), var);
    }

    /**
     * Adds a variable as a referenced class variable in this scope.
     * Referenced class variables are those accessed from the class scope.
     *
     * @param var the {@link Variable} to reference
     */
    public void putReferencedClassVariable(final Variable var) {
        if (referencedClassVariables == Collections.EMPTY_MAP)
            referencedClassVariables = new LinkedHashMap<>();
        referencedClassVariables.put(var.getName(), var);
    }

    /**
     * Removes a class variable reference from this scope by name.
     *
     * @param name the variable name to remove
     * @return the removed {@link Variable}, or null if not found
     */
    public Object removeReferencedClassVariable(final String name) {
        if (referencedClassVariables.isEmpty()) {
            return null;
        } else {
            return referencedClassVariables.remove(name);
        }
    }

    /**
     * Creates a shallow copy of this variable scope with the same parent, class scope, static context,
     * and copies of all variable maps. The returned scope is independent from the original and may
     * be safely modified without affecting the source scope.
     *
     * @return a new {@link VariableScope} with copied configuration
     */
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
