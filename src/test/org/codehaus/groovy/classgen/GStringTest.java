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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.syntax.Token;

public class GStringTest extends TestSupport {

    public void testConstructor() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);

        //Statement printStatement = createPrintlnStatement(new VariableExpression("str"));

        // simulate "Hello ${user}!"
        GStringExpression compositeStringExpr = new GStringExpression("hello ${user}!");
        compositeStringExpr.addString(new ConstantExpression("Hello "));
        compositeStringExpr.addValue(new VariableExpression("user"));
        compositeStringExpr.addString(new ConstantExpression("!"));
        BlockStatement block = new BlockStatement();
        block.addStatement(
                new ExpressionStatement(
                        new DeclarationExpression(
                                new VariableExpression("user"),
                                Token.newSymbol("=", -1, -1),
                                new ConstantExpression("World"))));
        block.addStatement(
                new ExpressionStatement(
                        new DeclarationExpression(new VariableExpression("str"), Token.newSymbol("=", -1, -1), compositeStringExpr)));
        block.addStatement(
                new ExpressionStatement(
                        new MethodCallExpression(VariableExpression.THIS_EXPRESSION, "println", new VariableExpression("str"))));

        block.addStatement(
                new ExpressionStatement(
                        new DeclarationExpression(
                                new VariableExpression("text"),
                                Token.newSymbol("=", -1, -1),
                                new MethodCallExpression(new VariableExpression("str"), "toString", MethodCallExpression.NO_ARGUMENTS))));

        block.addStatement(
                new AssertStatement(
                        new BooleanExpression(
                                new BinaryExpression(
                                        new VariableExpression("text"),
                                        Token.newSymbol("==", -1, -1),
                                        new ConstantExpression("Hello World!"))),
                        new ConstantExpression("Assertion failed") // TODO FIX if empty, AssertionWriter fails because source text is null
                )
        );
        classNode.addMethod(new MethodNode("stringDemo", ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        //Object[] array = { new Integer(1234), "abc", "def" };

        try {
            InvokerHelper.invokeMethod(bean, "stringDemo", null);
        }
        catch (InvokerInvocationException e) {
            System.out.println("Caught: " + e.getCause());
            e.getCause().printStackTrace();
            fail("Should not have thrown an exception");
        }
    }
}
