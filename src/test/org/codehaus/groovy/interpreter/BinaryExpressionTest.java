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

import groovy.util.GroovyTestCase;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerException;
import org.codehaus.groovy.syntax.Token;

/**
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BinaryExpressionTest extends GroovyTestCase {

    RuntimeContext context = new RuntimeContext();
    GroovyCodeVisitor visitor = new Interpreter(context);
    DummyBean bean = new DummyBean();

    Statement statement1 = new ExpressionStatement( new MethodCallExpression(new VariableExpression("bean"), "foo", new ConstantExpression("abcd")));
    Statement statement2 = new ExpressionStatement( new MethodCallExpression(new VariableExpression("bean"), "foo", new ConstantExpression("xyz")));

     public void testBinaryExpressions() {
        Object a = new Integer(2);
        Object b = new Integer(3);
        Object c = new Integer(2);
        
        assertBinaryExpression(true, a, Token.COMPARE_IDENTICAL, a);
        assertBinaryExpression(false, a, Token.COMPARE_IDENTICAL, b);
        assertBinaryExpression(false, a, Token.COMPARE_IDENTICAL, c);

        assertBinaryExpression(true, a, Token.COMPARE_EQUAL, a);
        assertBinaryExpression(true, a, Token.COMPARE_EQUAL, c);
        assertBinaryExpression(false, a, Token.COMPARE_EQUAL, b);

        assertBinaryExpression(true, a, Token.COMPARE_NOT_EQUAL, b);
        assertBinaryExpression(false, a, Token.COMPARE_NOT_EQUAL, a);
        assertBinaryExpression(false, a, Token.COMPARE_NOT_EQUAL, c);
        
        assertBinaryExpression(true, b, Token.COMPARE_GREATER_THAN, a);
        assertBinaryExpression(false, a, Token.COMPARE_GREATER_THAN, b);
        assertBinaryExpression(false, a, Token.COMPARE_GREATER_THAN, c);
        assertBinaryExpression(false, a, Token.COMPARE_GREATER_THAN, a);
        
        assertBinaryExpression(true, b, Token.COMPARE_GREATER_THAN_EQUAL, b);
        assertBinaryExpression(true, b, Token.COMPARE_GREATER_THAN_EQUAL, a);
        assertBinaryExpression(false, a, Token.COMPARE_GREATER_THAN_EQUAL, b);
        
        assertBinaryExpression(true, a, Token.COMPARE_LESS_THAN, b);
        assertBinaryExpression(false, a, Token.COMPARE_LESS_THAN, c);
        assertBinaryExpression(false, a, Token.COMPARE_LESS_THAN, a);
        
        assertBinaryExpression(true, a, Token.COMPARE_LESS_THAN_EQUAL, a);
        assertBinaryExpression(true, a, Token.COMPARE_LESS_THAN_EQUAL, b);
        assertBinaryExpression(true, a, Token.COMPARE_LESS_THAN_EQUAL, c);
        assertBinaryExpression(false, b, Token.COMPARE_LESS_THAN_EQUAL, a);
    }

    public void testNotComparableObjects() {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        assertBinaryExpression(true, a, Token.COMPARE_IDENTICAL, a);
        assertBinaryExpression(false, a, Token.COMPARE_IDENTICAL, b);
        assertBinaryExpression(false, a, Token.COMPARE_IDENTICAL, c);

        assertBinaryExpression(true, a, Token.COMPARE_EQUAL, a);
        assertBinaryExpression(false, a, Token.COMPARE_EQUAL, c);
        assertBinaryExpression(false, a, Token.COMPARE_EQUAL, b);

        assertBinaryExpression(true, a, Token.COMPARE_NOT_EQUAL, b);
        assertBinaryExpression(true, a, Token.COMPARE_NOT_EQUAL, c);
        assertBinaryExpression(false, a, Token.COMPARE_NOT_EQUAL, a);
    }

    public void testNotComparableObjectFailure() {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        assertBinaryExpressionFail(b, Token.COMPARE_GREATER_THAN, a);
        assertBinaryExpressionFail(a, Token.COMPARE_GREATER_THAN, b);
        assertBinaryExpressionFail(a, Token.COMPARE_GREATER_THAN, c);
        assertBinaryExpressionFail(a, Token.COMPARE_GREATER_THAN, a);
        
        assertBinaryExpressionFail(b, Token.COMPARE_GREATER_THAN_EQUAL, b);
        assertBinaryExpressionFail(b, Token.COMPARE_GREATER_THAN_EQUAL, a);
        assertBinaryExpressionFail(a, Token.COMPARE_GREATER_THAN_EQUAL, b);
        
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN, b);
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN, c);
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN, a);
        
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN_EQUAL, a);
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN_EQUAL, b);
        assertBinaryExpressionFail(a, Token.COMPARE_LESS_THAN_EQUAL, c);
        assertBinaryExpressionFail(b, Token.COMPARE_LESS_THAN_EQUAL, a);
    }
    
    protected void assertBinaryExpressionFail(Object left, int token, Object right) {
        try {
            assertBinaryExpression(true, left, token, right);
            fail("Should have failed as these objects are not comparable");
        }
        catch (InvokerException e) {
            // worked
        }
    }

    protected void assertBinaryExpression(boolean condition, Object left, int token, Object right) {
        BooleanExpression expression = new BooleanExpression( new BinaryExpression(new ConstantExpression(left), Token.newToken(token, 0, 0), new ConstantExpression(right)));
         
        IfStatement block = new IfStatement(expression, statement1, statement2);
        block.visit(visitor);
  
        Object expected = (condition) ? "abcd" : "xyz";
           
        assertEquals("bean buffer", expected, bean.getBuffer());
        
    }
    
    protected void setUp() throws Exception {
        context.setVariable("bean", bean);
    }
}
