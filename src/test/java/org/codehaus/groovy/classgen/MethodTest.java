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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;

public class MethodTest extends TestSupport {

    public void testMethods() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null));

        Statement statementA = new ReturnStatement(new ConstantExpression("calledA"));
        Statement statementB = new ReturnStatement(new ConstantExpression("calledB"));
        Statement emptyStatement = new BlockStatement();

        classNode.addMethod(new MethodNode("a", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementA));
        classNode.addMethod(new MethodNode("b", ACC_PUBLIC, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, statementB));

        classNode.addMethod(new MethodNode("noReturnMethodA", ACC_PUBLIC, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, emptyStatement));
        classNode.addMethod(new MethodNode("noReturnMethodB", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, emptyStatement));

        classNode.addMethod(new MethodNode("c", ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, emptyStatement));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.getDeclaredConstructor().newInstance();
        assertTrue("Created instance of class: " + bean, bean != null);

        assertCallMethod(bean, "a", "calledA");
        assertCallMethod(bean, "b", "calledB");
        assertCallMethod(bean, "noReturnMethodA", null);
        assertCallMethod(bean, "noReturnMethodB", null);
        assertCallMethod(bean, "c", null);
    }

    protected void assertCallMethod(Object object, String method, Object expected) {
        Object value = InvokerHelper.invokeMethod(object, method, new Object[0]);
        assertEquals("Result of calling method: " + method + " on: " + object + " with empty list", expected, value);

        value = InvokerHelper.invokeMethod(object, method, null);
        assertEquals("Result of calling method: " + method + " on: " + object + " with null", expected, value);
    }
}
