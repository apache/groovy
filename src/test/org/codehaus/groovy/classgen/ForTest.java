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
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

public class ForTest extends TestSupport {

    public void testNonLoop() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));

        Parameter[] parameters = {new Parameter(ClassHelper.OBJECT_TYPE, "coll")};

        Statement statement = createPrintlnStatement(new VariableExpression("coll"));
        classNode.addMethod(new MethodNode("oneParamDemo", ACC_PUBLIC, ClassHelper.VOID_TYPE, parameters, ClassNode.EMPTY_ARRAY, statement));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        System.out.println("################ Now about to invoke a method without looping");
        Object value = new Integer(10000);

        try {
            InvokerHelper.invokeMethod(bean, "oneParamDemo", new Object[]{value});
        } catch (InvokerInvocationException e) {
            System.out.println("Caught: " + e.getCause());
            e.getCause().printStackTrace();
            fail("Should not have thrown an exception");
        }
        System.out.println("################ Done");
    }


    public void testLoop() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));

        Parameter[] parameters = {new Parameter(ClassHelper.OBJECT_TYPE.makeArray(), "coll")};

        Statement loopStatement = createPrintlnStatement(new VariableExpression("i"));

        ForStatement statement = new ForStatement(new Parameter(ClassHelper.OBJECT_TYPE, "i"), new VariableExpression("coll"), loopStatement);
        classNode.addMethod(new MethodNode("iterateDemo", ACC_PUBLIC, ClassHelper.VOID_TYPE, parameters, ClassNode.EMPTY_ARRAY, statement));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        System.out.println("################ Now about to invoke a method with looping");
        Object[] array = {new Integer(1234), "abc", "def"};

        try {
            InvokerHelper.invokeMethod(bean, "iterateDemo", new Object[]{array});
        } catch (InvokerInvocationException e) {
            System.out.println("Caught: " + e.getCause());
            e.getCause().printStackTrace();
            fail("Should not have thrown an exception");
        }
        System.out.println("################ Done");
    }

    public void testManyParam() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));

        Parameter[] parameters = {new Parameter(ClassHelper.OBJECT_TYPE, "coll1"), new Parameter(ClassHelper.OBJECT_TYPE, "coll2"), new Parameter(ClassHelper.OBJECT_TYPE, "coll3")};

        BlockStatement statement = new BlockStatement();
        statement.addStatement(createPrintlnStatement(new VariableExpression("coll1")));
        statement.addStatement(createPrintlnStatement(new VariableExpression("coll2")));
        statement.addStatement(createPrintlnStatement(new VariableExpression("coll3")));

        classNode.addMethod(new MethodNode("manyParamDemo", ACC_PUBLIC, ClassHelper.VOID_TYPE, parameters, ClassNode.EMPTY_ARRAY, statement));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        System.out.println("################ Now about to invoke a method with many parameters");
        Object[] array = {new Integer(1000 * 1000), "foo-", "bar~"};

        try {
            InvokerHelper.invokeMethod(bean, "manyParamDemo", array);
        } catch (InvokerInvocationException e) {
            System.out.println("Caught: " + e.getCause());
            e.getCause().printStackTrace();
            fail("Should not have thrown an exception");
        }
        System.out.println("################ Done");
    }
}
