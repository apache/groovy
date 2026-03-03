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
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.runtime.DummyBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PropertyTest extends TestSupport {

    @Test
    void testFields() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        classNode.addField("x", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, null);
        classNode.addField("y", ACC_PUBLIC, ClassHelper.Integer_TYPE, null);
        classNode.addField("z", ACC_PRIVATE, ClassHelper.STRING_TYPE, null);

        Class<?> fooClass = loadClass(classNode);
        assertTrue(fooClass != null, "Loaded a new class");

        assertField(fooClass, "x", Modifier.PUBLIC, ClassHelper.OBJECT_TYPE);
        assertField(fooClass, "y", Modifier.PUBLIC, ClassHelper.Integer_TYPE);
        assertField(fooClass, "z", Modifier.PRIVATE, ClassHelper.STRING_TYPE);
    }

    @Test
    void testProperties() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC + ACC_SUPER, ClassHelper.OBJECT_TYPE);
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, ClassHelper.STRING_TYPE, classNode, null, null, null));

        Class<?> fooClass = loadClass(classNode);
        assertTrue(fooClass != null, "Loaded a new class");

        Object bean = fooClass.getDeclaredConstructor().newInstance();
        assertTrue(bean != null, "Managed to create bean");

        assertField(fooClass, "bar", 0, ClassHelper.STRING_TYPE);

        assertGetProperty(bean, "bar", null);
        assertSetProperty(bean, "bar", "newValue");
    }

    @Test
    void testInheritedProperties() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC + ACC_SUPER, ClassHelper.make(DummyBean.class));
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, ClassHelper.STRING_TYPE, classNode, null, null, null));

        Class<?> fooClass = loadClass(classNode);
        assertTrue(fooClass != null, "Loaded a new class");

        Object bean = fooClass.getDeclaredConstructor().newInstance();
        assertTrue(bean != null, "Managed to create bean");

        assertField(fooClass, "bar", 0, ClassHelper.STRING_TYPE);

        assertGetProperty(bean, "name", "James");
        assertSetProperty(bean, "name", "Bob");

        assertGetProperty(bean, "bar", null);
        assertSetProperty(bean, "bar", "newValue");
    }
}
