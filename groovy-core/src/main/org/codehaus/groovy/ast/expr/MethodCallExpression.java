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

import groovy.lang.MetaMethod;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * A method call on an object or class
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodCallExpression extends Expression {

    private Expression objectExpression;
    private Expression method;
    private Expression arguments;
    private boolean spreadSafe = false;
    private boolean safe = false;
    private boolean implicitThis;
    
    public static Expression NO_ARGUMENTS = new TupleExpression();

    public MetaMethod getMetaMethod() {
        return metaMethod;
    }

    private MetaMethod metaMethod = null;

    public MethodCallExpression(Expression objectExpression, String method, Expression arguments) {
        this(objectExpression,new ConstantExpression(method),arguments);
    }
    
    public MethodCallExpression(Expression objectExpression, Expression method, Expression arguments) {
        this.objectExpression = objectExpression;
        this.method = method;
        this.arguments = arguments;
        //TODO: set correct type here
        // if setting type and a methodcall is the last expresssion in a method,
        // then the method will return null if the method itself is not void too!
        // (in bytecode after call: aconst_null, areturn)
        this.setType(ClassHelper.DYNAMIC_TYPE);
        this.setImplicitThis(true);
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMethodCallExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        MethodCallExpression answer =
            new MethodCallExpression(transformer.transform(objectExpression), transformer.transform(method), transformer.transform(arguments));
        answer.setSafe(safe);
        answer.setSourcePosition(this);
        return answer;
    }

    public Expression getArguments() {
        return arguments;
    }

    public Expression getMethod() {
        return method;
    }
    
    /**
     * This method returns the method name as String if it is no dynamic
     * calculated method name, but an constant.
     */
    public String getMethodAsString() {
        if (! (method instanceof ConstantExpression)) return null;
        ConstantExpression constant = (ConstantExpression) method;
        return constant.getText();
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public String getText() {
        return objectExpression.getText() + "." + method.getText() + arguments.getText();
    }

    /**
     * @return is this a safe method call, i.e. if true then if the source object is null
     * then this method call will return null rather than throwing a null pointer exception
     */
    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    public void setSpreadSafe(boolean value) {
        spreadSafe = value;
    }

    /**
     * @return true if no object expression was specified otherwise if 
     * some expression was specified for the object on which to evaluate
     * the method then return false
     */
    public boolean isImplicitThis() {
        return implicitThis;
    }

    public void setImplicitThis(boolean implicitThis) {
        this.implicitThis = implicitThis;
    }

    public String toString() {
        return super.toString()
            + "[object: "
            + objectExpression
            + " method: "
            + method
            + " arguments: "
            + arguments
            + "]";
    }

    public void setMethod(MetaMethod mmeth) {
        this.metaMethod = mmeth;
        super.setType(ClassHelper.make(mmeth.getReturnType()));
    }
}
