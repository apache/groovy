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
package org.codehaus.groovy.classgen;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Token;

/**
 * A visitor which figures out which variables are in scope
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VariableScopeCodeVisitor extends CodeVisitorSupport {

    private Set declaredVariables = new HashSet();
    private Set referencedVariables = new HashSet();
    private VariableScopeCodeVisitor closureVisitor;
    private Set parameterSet = new HashSet();

    public VariableScopeCodeVisitor() {
    }

    public Set getDeclaredVariables() {
        return declaredVariables;
    }

    public Set getReferencedVariables() {
        return referencedVariables;
    }

    public Set getParameterSet() {
        return parameterSet;
    }

    public VariableScopeCodeVisitor getClosureVisitor() {
        if (closureVisitor == null) {
            closureVisitor = new VariableScopeCodeVisitor();
        }
        return closureVisitor;
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (expression.getOperation().getType() == Token.EQUAL && leftExpression instanceof VariableExpression) {
            declareVariable((VariableExpression) leftExpression);
        }
        else {
            leftExpression.visit(this);
        }
        expression.getRightExpression().visit(this);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        getClosureVisitor().setParameters(expression.getParameters());
        expression.getCode().visit(getClosureVisitor());
    }

    public void visitVariableExpression(VariableExpression expression) {
        // check for undeclared variables?
        String variable = expression.getVariable();
        /*
        if (!parameterSet.contains(variable)) {
            referencedVariables.add(variable);
        }
        */
        referencedVariables.add(variable);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        Expression exp = expression.getExpression();
        if (exp instanceof VariableExpression) {
            declareVariable((VariableExpression) exp);
        }
        else {
            exp.visit(this);
        }
    }

    protected void setParameters(Parameter[] parameters) {
        parameterSet.clear();
        for (int i = 0; i < parameters.length; i++) {
            parameterSet.add(parameters[i].getName());
        }
    }

    protected void declareVariable(VariableExpression varExp) {
        String variable = varExp.getVariable();
        /*
        if (!parameterSet.contains(variable)) {
            declaredVariables.add(variable);
            referencedVariables.add(variable);
        }
        */
        declaredVariables.add(variable);
        referencedVariables.add(variable);
    }

}
