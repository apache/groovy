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
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.syntax.Token;

public class TupleListTest extends TestSupport {

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

        Object bean = fooClass.getDeclaredConstructor().newInstance();
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
