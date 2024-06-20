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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public final class ClassNodeTest {

    private final ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
    private final ClassNode innerClassNode = new InnerClassNode(classNode, "Foo$1", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);

    @Before
    public void setUp() {
        classNode.addField("field", ACC_PUBLIC, ClassHelper.STRING_TYPE, null);
    }

    @Test // GROOVY-11413
    public void testArrayClass() {
        ClassNode array1 = ClassHelper.make(Integer[].class);
        ClassNode array2 = classNode.makeArray();

        assertEquals(array1.getModifiers(), array2.getModifiers());
        assertEquals(array1.getSuperClass(), array2.getSuperClass());
        assertArrayEquals(array1.getInterfaces(), array2.getInterfaces());

        // members; TODO: "length" and "clone()"
        assertTrue(array1.getFields().isEmpty());
        assertTrue(array2.getFields().isEmpty());
        assertTrue(array1.getMethods().isEmpty());
        assertTrue(array2.getMethods().isEmpty());
    }

    @Test
    public void testOuterClass() {
        assertNull(classNode.getOuterClass());
        assertNotNull(innerClassNode.getOuterClass());
    }

    @Test
    public void testOuterField() {
        assertNull(classNode.getOuterField("field"));
        assertNotNull(innerClassNode.getOuterField("field"));
    }

    @Test // GROOVY-10763
    public void testSuperClass() {
        assertTrue(classNode.isPrimaryClassNode());
        assertFalse(classNode.isUsingGenerics());

        ClassNode superClass = GenericsUtils.makeClassSafe0(ClassHelper.REFERENCE_TYPE, ClassHelper.STRING_TYPE.asGenericsType());
        classNode.setSuperClass(superClass);

        assertTrue("'using generics' not updated", classNode.isUsingGenerics());
    }

    @Test // GROOVY-10763
    public void testInterfaces() {
        assertTrue(classNode.isPrimaryClassNode());
        assertFalse(classNode.isUsingGenerics());

        ClassNode[] interfaces = {GenericsUtils.makeClassSafe0(ClassHelper.ITERABLE_TYPE, ClassHelper.STRING_TYPE.asGenericsType())};
        classNode.setInterfaces(interfaces);

        assertTrue("'using generics' not updated", classNode.isUsingGenerics());
    }

    @Test
    public void testPackageName() {
        assertEquals("Package", null, classNode.getPackageName());

        ClassNode packageNode = new ClassNode("com.acme.Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        assertEquals("Package", "com.acme", packageNode.getPackageName());
    }

    @Test
    public void testPlainReference() {
        ClassNode arrayNode = classNode.getPlainNodeReference().makeArray().makeArray();
        ClassNode plainNode = arrayNode.getPlainNodeReference();

        assertTrue(plainNode.isArray());
        assertTrue(plainNode.getComponentType().isArray());
        assertEquals(arrayNode.getComponentType(), plainNode.getComponentType());
        assertEquals(classNode, plainNode.getComponentType().getComponentType());

        assertEquals(arrayNode.getName(), plainNode.getName());
        assertEquals(arrayNode.toString(), plainNode.toString());
        assertEquals(arrayNode.getComponentType().getName(), plainNode.getComponentType().getName());
        assertEquals(arrayNode.getComponentType().toString(), plainNode.getComponentType().toString());
    }

    @Test
    public void testTypeAnnotations() {
        var annotation = new AnnotationNode(ClassHelper.make(Deprecated.class));
        // TYPE_USE annotations are recoreded as class annotations, not type annotations
        assertThrows(GroovyBugError.class, () -> classNode.addTypeAnnotation(annotation));

        ClassNode reference = classNode.getPlainNodeReference();
        reference.addTypeAnnotation(annotation);
        assertEquals(1, reference.getTypeAnnotations().size());
        assertEquals(0, classNode.getTypeAnnotations().size());
    }

    @Test
    public void testPermittedSubclasses() throws Exception {
        assumeTrue(Runtime.version().feature() >= 17);

        Class<?>  c  = Class.forName("java.lang.constant.ConstantDesc");
        ClassNode cn = new ClassNode(c);
        assertTrue(!cn.getPermittedSubclasses().isEmpty());
        assertTrue(cn.isSealed());

        cn = ClassHelper.make(c);
        assertTrue(!cn.getPermittedSubclasses().isEmpty());
        assertTrue(cn.isSealed());

        /* some constructors of ClassNode do not trigger the lazy initialization
        cn = ClassHelper.make("java.lang.constant.ConstantDesc");
        assertTrue(!cn.getPermittedSubclasses().isEmpty());
        assertTrue(cn.isSealed());
        */
    }
}
