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
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.codehaus.groovy.ast.AstToTextHelper.getParametersText;
import static org.codehaus.groovy.ast.tools.ClosureUtils.hasImplicitParameter;

/**
 * Represents a closure expression such as {@code { statement }} or {@code { i -> statement }} or
 * {@code { i, x, String y -> statement }}. A closure is an anonymous function that can capture
 * local variables from its enclosing scope (closure variables). Closures support zero or more
 * parameters, with optional type annotations, and are executed in their own variable scope.
 *
 * @see Expression
 * @see Parameter
 * @see VariableScope
 * @see MethodCallExpression
 */
public class ClosureExpression extends Expression {

    private final Parameter[] parameters;
    private Statement code;
    private VariableScope variableScope;

    /**
     * Creates a closure expression with the specified parameters and code body.
     *
     * @param parameters an array of {@link Parameter} definitions for this closure, or null if the
     *                   closure has no explicit parameters (allowing implicit it parameter); may be
     *                   empty for a closure with no parameters
     * @param code the {@link Statement} representing the body of the closure; must not be null
     */
    public ClosureExpression(final Parameter[] parameters, final Statement code) {
        this.parameters = parameters;
        this.code = code;
        setType(ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
    }

    /**
     * Returns the code statement that represents the body of this closure.
     * This method can be used to inspect or analyze what actions the closure will perform.
     *
     * @return the code {@link Statement} representing the closure body; may be null
     */
    public Statement getCode() {
        return code;
    }

    /**
     * Sets the code statement for this closure body. This method can be used to modify
     * or add additional actions during closure execution, typically during AST transformations.
     *
     * @param code the new {@link Statement} representing the closure body; must not be null
     */
    public void setCode(final Statement code) {
        this.code = code;
    }

    /**
     * Returns the parameter definitions for this closure.
     *
     * @return an array of {@link Parameter} definitions, empty if no explicit parameters are provided
     *         (allowing implicit it parameter), or null if the closure has no parameters at all
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Indicates whether one or more explicit parameters are specified for this closure.
     *
     * @return true if explicit parameters are present; false if no explicit parameters are specified
     *         (in which case an implicit 'it' parameter may be available)
     */
    public boolean isParameterSpecified() {
        return (parameters != null && parameters.length > 0);
    }

    /**
     * Returns the variable scope associated with this closure. The variable scope tracks
     * accessible variables within the closure's execution context, including captured variables.
     *
     * @return the {@link VariableScope} for this closure; may be null if not yet initialized
     */
    public VariableScope getVariableScope() {
        return variableScope;
    }

    /**
     * Sets the variable scope for this closure.
     *
     * @param variableScope the {@link VariableScope} to associate with this closure; may be null
     */
    public void setVariableScope(final VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    @Override
    public String getText() {
        return toString("...");
    }

    @Override
    public String toString() {
        return super.toString() + this.toString(code == null ? "<null>" : code.toString());
    }

    private String toString(final String bodyText) {
        if (hasImplicitParameter(this)) return "{ " + bodyText + " }";
        return "{ " + getParametersText(parameters) + " -> " + bodyText + " }";
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        return this;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitClosureExpression(this);
    }
}
