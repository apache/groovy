/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

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

import java.util.ArrayList;

import org.codehaus.groovy.GroovyTestCase;
import org.codehaus.groovy.interpreter.Interpreter;
import org.codehaus.groovy.interpreter.RuntimeContext;
import org.codehaus.groovy.ast.*;

/**
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ForLoopTest extends GroovyTestCase {

    public void testForLoopWithArray() {
        Object[] array = { "A", "B", "C" };
        testLoop(array);
    }

    public void testForLoopWithList() {
        ArrayList list = new ArrayList();
        list.add("A");
        list.add("B");
        list.add("C");
        testLoop(list);
    }

    protected void testLoop(Object collection) {
        RuntimeContext context = new RuntimeContext();
        context.setVariable("fooCollection", collection);
        DummyBean bean = new DummyBean();
        context.setVariable("bean", bean);

        GroovyCodeVisitor visitor = new Interpreter(context);

    
        Statement statement = new ExpressionStatement( new MethodCallExpression(new VariableExpression("bean"), "foo", new VariableExpression("i")));
        Expression expression = new VariableExpression("fooCollection");
        ForLoop loop = new ForLoop("i", expression, statement);
        loop.visit(visitor);
        
        assertEquals("invocations count", "ABC", bean.getBuffer());
    }
}
