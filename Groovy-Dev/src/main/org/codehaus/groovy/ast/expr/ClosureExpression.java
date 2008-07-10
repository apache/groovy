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
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;


/**
 * Represents a closure creation expression such as { statement; } 
 * or { i : statement; } or { i, x, String y: statement }
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClosureExpression extends Expression {
    
    private Parameter[] parameters;
    private Statement code;
    private VariableScope variableScope;
    
    public ClosureExpression(Parameter[] parameters, Statement code) {
        this.parameters = parameters;
        this.code = code;
        super.setType(ClassHelper.CLOSURE_TYPE);
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

    public Statement getCode() {
        return code;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public boolean isParameterSpecified() {
        return parameters != null && parameters.length > 0;
    }
    
    public VariableScope getVariableScope() {
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

}
