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
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IfElseTest extends TestSupport {

    public void testLoop() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, ClassHelper.STRING_TYPE, classNode, null, null, null));

        classNode.addProperty(new PropertyNode("result", ACC_PUBLIC, ClassHelper.STRING_TYPE, classNode, null, null, null));

        BooleanExpression expression =
                new BooleanExpression(
                        new BinaryExpression(
                                new FieldExpression(
                                        new FieldNode("bar", ACC_PRIVATE, ClassHelper.STRING_TYPE, classNode, ConstantExpression.NULL)),
                                Token.newSymbol("==", 0, 0),
                                new ConstantExpression("abc")));

        Statement trueStatement =
                new ExpressionStatement(
                        new BinaryExpression(
                                new FieldExpression(
                                        new FieldNode("result", ACC_PRIVATE, ClassHelper.STRING_TYPE, classNode, ConstantExpression.NULL)),
                                Token.newSymbol("=", 0, 0),
                                new ConstantExpression("worked")));

        Statement falseStatement = createPrintlnStatement(new ConstantExpression("false"));

        IfStatement statement = new IfStatement(expression, trueStatement, falseStatement);
        classNode.addMethod(new MethodNode("ifDemo", ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statement));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        assertSetProperty(bean, "bar", "abc");

        System.out.println("################ Now about to invoke method");

        Object[] array = {
        };

        InvokerHelper.invokeMethod(bean, "ifDemo", array);

        System.out.println("################ Done");

        assertGetProperty(bean, "result", "worked");
    }
}
