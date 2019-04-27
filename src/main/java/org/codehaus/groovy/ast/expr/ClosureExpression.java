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

import org.codehaus.groovy.ast.AstToTextHelper;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents a closure expression such as <pre>{ statement }</pre>
 * or { i {@code ->} statement } or { i, x, String y {@code ->}  statement }
 */
public class ClosureExpression extends Expression {
    
    private final Parameter[] parameters;
    private Statement code;
    private VariableScope variableScope;
    
    public ClosureExpression(Parameter[] parameters, Statement code) {
        this.parameters = parameters;
        this.code = code;
        super.setType(ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitClosureExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }
    
    public String toString() {
        return super.toString() + InvokerHelper.toString(parameters) + "{ " + code + " }";
    }

    /**
     * This gets the code statement of the closure. You can read this method to find out what actions
     * the closure is going to perform.
     *
     * @return the code statement of the closure
     */
    public Statement getCode() {
        return code;
    }

    /**
     * This sets the code statement of the closure. You can use this method in order to add more actions
     * during the closure execution.
     *
     * @param code the new Statement
     */
    public void setCode(Statement code) {
        this.code = code;
    }

    /**
     * @return an array of zero (for implicit it) or more (when explicit args given) parameters or null otherwise (representing explicit no args)
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * @return true if one or more explicit parameters are supplied
     */
    public boolean isParameterSpecified() {
        return parameters != null && parameters.length > 0;
    }
    
    public VariableScope getVariableScope() {
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    @Override
    public String getText() {
        String paramText = AstToTextHelper.getParametersText(parameters);
        if (paramText.length() > 0) {
            return "{ " + paramText + " -> ... }";
        } else {
            return "{ -> ... }";
        }
    }
}
