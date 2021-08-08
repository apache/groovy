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
package org.codehaus.groovy.ast;

import junit.framework.TestCase;
import org.objectweb.asm.Opcodes;

import static groovy.test.GroovyAssert.isAtLeastJdk;

/**
 * Tests the ClassNode
 */
public class ClassNodeTest extends TestCase implements Opcodes {

    ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
    ClassNode innerClassNode = new InnerClassNode(classNode, "Foo$1", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);

    protected void setUp() throws Exception {
        classNode.addField("field", ACC_PUBLIC, ClassHelper.STRING_TYPE, null);
    }

    public void testOuterClass() {
        assertNull(classNode.getOuterClass());
        assertNotNull(innerClassNode.getOuterClass());
    }

    public void testOuterField() {
        assertNull(classNode.getOuterField("field"));
        assertNotNull(innerClassNode.getOuterField("field"));
    }

    public void testPackageName() {
        assertEquals("Package", null, classNode.getPackageName());

        ClassNode packageNode = new ClassNode("com.acme.Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        assertEquals("Package", "com.acme", packageNode.getPackageName());
    }

    public void testPermittedSubclasses() throws ClassNotFoundException {
        if (!isAtLeastJdk("17.0")) return;

        Class<?> clazz = Class.forName("java.lang.constant.ConstantDesc");
        ClassNode cn = new ClassNode(clazz);
        assertTrue(!cn.getPermittedSubclasses().isEmpty());
        assertTrue(cn.isSealed());

        cn = ClassHelper.make(clazz);
        assertTrue(!cn.getPermittedSubclasses().isEmpty());
        assertTrue(cn.isSealed());

        // some constructors of `ClassNode` will not trigger the lazy initialization
//        cn = ClassHelper.make("java.lang.constant.ConstantDesc");
//        assertTrue(!cn.getPermittedSubclasses().isEmpty());
//        assertTrue(cn.isSealed());
    }
}
