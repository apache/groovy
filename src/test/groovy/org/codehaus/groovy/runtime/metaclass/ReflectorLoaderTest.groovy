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
package org.codehaus.groovy.runtime.metaclass

import org.codehaus.groovy.runtime.Reflector
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ALOAD
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17

/**
 * Minimal smoke coverage for deprecated {@link ReflectorLoader} binary-compat
 * surface — not a place to grow policy tests.
 */
@SuppressWarnings('deprecation')
class ReflectorLoaderTest {

    private static byte[] reflectorSubclassBytes(String internalName) {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internalName, null,
                Reflector.name.replace('.', '/'), null)
        def mv = cw.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL,
                Reflector.name.replace('.', '/'), '<init>', '()V', false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    @Test
    void testDefineClassIsVisibleAndCached() {
        def loader = new ReflectorLoader(this.class.classLoader)
        String name = ReflectorLoader.getReflectorName(StringBuilder)
        Class<?> cls = loader.defineClass(
                name,
                reflectorSubclassBytes(name.replace('.', '/')),
                this.class.protectionDomain)
        assertNotNull(cls)
        assertTrue(Reflector.isAssignableFrom(cls))
        assertFalse(cls.isHidden(), 'deprecated loader defines visible classes only')
        assertEquals(name, cls.name)
        assertNotNull(cls.getDeclaredConstructor().newInstance())
        assertEquals(cls, loader.getLoadedClass(name))
        assertNull(loader.getLoadedClass('no.such.Reflector'))
    }

    @Test
    void testGetReflectorNameForJavaAndUserTypes() {
        // Non-array java.* types get the gjdk. prefix (restricted package).
        assertEquals('gjdk.java.lang.String_GroovyReflector',
                ReflectorLoader.getReflectorName(String))
        // Arrays use getName() form "[L…;" which does not start with "java.", so
        // the historical path has no gjdk. prefix — preserve that contract.
        assertEquals('java.lang.String_GroovyReflectorArray',
                ReflectorLoader.getReflectorName(String[]))
        assertEquals('java.lang.String_GroovyReflectorArray2',
                ReflectorLoader.getReflectorName(String[][]))
        assertTrue(ReflectorLoader.getReflectorName(ReflectorLoaderTest).endsWith('_GroovyReflector'))
    }
}
