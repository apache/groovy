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
package org.codehaus.groovy.ast.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import groovy.lang.Closure;

/**
 * Represents a method pointer on an object such as
 * foo.&bar which means find the method pointer on foo for the method called "bar"
 * which is equivalent to
 * <code>
 * foo.metaClass.getMethodPointer(foo, "bar")
 * 
 * @version $Revision$
 */
public class MethodPointerExpression extends Expression {

    private Expression expression;
    private String methodName;

    public MethodPointerExpression(Expression expression, String methodName) {
        this.expression = expression;
        this.methodName = methodName;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getMethodName() {
        return methodName;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodPointerExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return new MethodPointerExpression(transformer.transform(expression), methodName);
    }

    protected void resolveType(AsmClassGenerator resolver) {
        expression.resolve(resolver);
        setTypeClass(expression.getTypeClass());
    }

    public String getText() {
		return expression.getText() + ".&" + methodName;
	}

    public String getType() {
        return "groovy.lang.Closure";
    }

    public boolean isDynamic() {
        return false;
    }

    public Class getTypeClass() {
        return Closure.class;
    }
}
