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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.syntax.Token;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TupleListTest extends TestSupport {

    public void testIterateOverTuple() throws Exception {
        TupleExpression listExpression = new TupleExpression();
        listExpression.addExpression(new ConstantExpression("a"));
        listExpression.addExpression(new ConstantExpression("b"));
        listExpression.addExpression(new ConstantExpression("c"));
        assertIterate("iterateOverTuple", listExpression);
    }

    public void testIterateOverList() throws Exception {
        ListExpression listExpression = new ListExpression();
        listExpression.addExpression(new ConstantExpression("a"));
        listExpression.addExpression(new ConstantExpression("b"));
        listExpression.addExpression(new ConstantExpression("c"));
        listExpression.addExpression(new ConstantExpression("a"));
        listExpression.addExpression(new ConstantExpression("b"));
        listExpression.addExpression(new ConstantExpression("c"));
        assertIterate("iterateOverList", listExpression);
    }

    public void testIterateOverMap() throws Exception {
        MapExpression mapExpression = new MapExpression();
        mapExpression.addMapEntryExpression(new ConstantExpression("a"), new ConstantExpression("x"));
        mapExpression.addMapEntryExpression(new ConstantExpression("b"), new ConstantExpression("y"));
        mapExpression.addMapEntryExpression(new ConstantExpression("c"), new ConstantExpression("z"));
        assertIterate("iterateOverMap", mapExpression);
    }

    protected void assertIterate(String methodName, Expression listExpression) throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, ClassHelper.STRING_TYPE, classNode, null, null, null));

        Statement loopStatement = createPrintlnStatement(new VariableExpression("i"));

        BlockStatement block = new BlockStatement();
        block.addStatement(new ExpressionStatement(new DeclarationExpression(new VariableExpression("list"), Token.newSymbol("=", 0, 0), listExpression)));
        block.addStatement(new ForStatement(new Parameter(ClassHelper.DYNAMIC_TYPE, "i"), new VariableExpression("list"), loopStatement));
        classNode.addMethod(new MethodNode(methodName, ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        System.out.println("################ Now about to invoke method");

        try {
            InvokerHelper.invokeMethod(bean, methodName, null);
        }
        catch (InvokerInvocationException e) {
            System.out.println("Caught: " + e.getCause());
            e.getCause().printStackTrace();
            fail("Should not have thrown an exception");
        }
        System.out.println("################ Done");
    }

}
