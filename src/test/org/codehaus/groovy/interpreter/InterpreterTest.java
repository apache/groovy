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

package org.codehaus.groovy.interpreter;

import groovy.lang.GroovyTestCase;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.runtime.InvokerException;

/**
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InterpreterTest extends GroovyTestCase {

    RuntimeContext context = new RuntimeContext();
    DummyBean bean = new DummyBean();
    Interpreter visitor = new Interpreter(context);

    public void testEvaluateConstantExpressions() {
        assertExpressionEquals("foo", new ConstantExpression("foo"));
        assertExpressionEquals(null, ConstantExpression.NULL);
        assertExpressionEquals(Boolean.TRUE, ConstantExpression.TRUE);
        assertExpressionEquals(Boolean.FALSE, ConstantExpression.FALSE);
        assertExpressionEquals(bean, new ConstantExpression(bean));
    }

    public void testEvaluateVariableExpressions() {
        assertExpressionEquals("abc", new VariableExpression("x"));
        assertExpressionEquals(new Integer(123), new VariableExpression("y"));
    }

    public void testMethodCallExpressionWithNoArguments() {
        //visitor.visitMethodCallExpression(new MethodCallExpression(new ConstantExpression(bean), "isBooleanTrue", ConstantExpression.NULL));
        
        
        assertExpressionEquals(Boolean.TRUE, new MethodCallExpression(new ConstantExpression(bean), "isBooleanTrue", ConstantExpression.NULL));
        assertExpressionEquals(Boolean.TRUE, new MethodCallExpression(new VariableExpression("bean"), "isBooleanTrue", ConstantExpression.NULL));
    }

    public void testMethodCallExpressionOnMethodThatThrowsException() {
        try {
            visitor.visitMethodCallExpression(
                new MethodCallExpression(new VariableExpression("bean"), "throwMethod", new VariableExpression("x")));
            fail("Should have thrown an interpreter exception");
        }
        catch (InterpreterException e) {
            // worked
        }
    }

    public void testMethodCallExpressionWithUnknownMethod() {
        try {
            visitor.visitMethodCallExpression(
                new MethodCallExpression(new VariableExpression("bean"), "unknown", new VariableExpression("x")));
            fail("Should have thrown an interpreter exception");
        }
        catch (InvokerException e) {
            // worked
        }
    }

    public void testMethodCallExpressionOnNullObject() {
        try {
            visitor.visitMethodCallExpression(
                new MethodCallExpression(new VariableExpression("unknown"), "foo", new VariableExpression("x")));
            fail("Should have thrown an interpreter exception");
        }
        catch (InterpreterException e) {
            // worked
        }
    }

    public void testInterpreterException() throws Throwable {
        try {
            throw new InterpreterException("message");
        }
        catch (InterpreterException e) {
            // worked
            assertEquals("message", e.getMessage());
        }
        try {
            throw new InterpreterException("message", new NullPointerException());
        }
        catch (InterpreterException e) {
            // worked
            assertEquals("message", e.getMessage());
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    
    protected void assertExpressionEquals(Object expected, Expression expression) {
        Object value = visitor.evaluate(expression);
        assertEquals("Expression: " + expression, expected, value);
    }

    protected void setUp() throws Exception {
        context.setVariable("bean", bean);
        context.setVariable("x", "abc");
        context.setVariable("y", new Integer(123));
    }

}
