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

        Object bean = fooClass.getDeclaredConstructor().newInstance();
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
