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
package org.codehaus.groovy.reflection

import org.apache.groovy.util.HiddenClassDefiner
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ALOAD
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17

/**
 * Covers the hidden-class path in {@link ClassLoaderForClassArtifacts}.
 */
class ClassLoaderForClassArtifactsTest {

    static class Host {
    }

    private static byte[] minimalBytes(String internalName) {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        def mv = cw.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    @Test
    void testDefinePrefersHiddenNestmateOfTarget() {
        def loader = new ClassLoaderForClassArtifacts(Host)
        String name = loader.createClassName('artifact')
        Class<?> cls = loader.define(name, minimalBytes(name.replace('.', '/')))
        assertNotNull(cls)
        if (HiddenClassDefiner.isEnabled()) {
            assertTrue(cls.isHidden())
            assertEquals(Host.nestHost, cls.nestHost)
            assertEquals(Host.packageName, cls.packageName)
        }
        assertNotNull(cls.getDeclaredConstructor().newInstance())
    }

    @Test
    void testDefineFallsBackForUnopenedPlatformHost() {
        // Nestmate of String fails without --add-opens. Visible defineClass
        // fallback must still produce a usable class.
        def loader = new ClassLoaderForClassArtifacts(String)
        String name = loader.createClassName('platformFallback')
        Class<?> cls = loader.define(name, minimalBytes(name.replace('.', '/')))
        assertNotNull(cls)
        assertNotNull(cls.getDeclaredConstructor().newInstance())
        if (!HiddenClassDefiner.canAttemptPrivateLookup(String)) {
            assertFalse(cls.isHidden(),
                    'unopened java.lang.String host must fall back to a visible class')
        }
    }

    @Test
    void testDefineClassAndGetConstructor() {
        def loader = new ClassLoaderForClassArtifacts(Host)
        String name = loader.createClassName('withCtor')
        // Public no-arg constructor — getConstructor contract
        def ctor = loader.defineClassAndGetConstructor(name, minimalBytes(name.replace('.', '/')))
        assertNotNull(ctor)
        assertNotNull(ctor.newInstance())

        assertNull(loader.defineClassAndGetConstructor(
                loader.createClassName('missing'),
                minimalBytes('org/codehaus/groovy/reflection/Missing'),
                String))
    }

    @Test
    void testCreateClassNameUniquenessAndJavaPrefix() {
        def loader = new ClassLoaderForClassArtifacts(Host)
        String first = loader.createClassName('m')
        String second = loader.createClassName('m')
        assertTrue(first.contains(Host.name))
        assertNotEquals(first, second)
        assertTrue(second.endsWith('$0'), "second name should carry counter suffix, was: $second")

        def javaLoader = new ClassLoaderForClassArtifacts(String)
        String javaName = javaLoader.createClassName('length')
        assertFalse(javaName.startsWith('java.'),
            'java.* artifacts must be renamed out of the restricted package')
        assertTrue(javaName.startsWith('java_lang_String'))
    }

    @Test
    void testCreateClassNameFromMethod() {
        def loader = new ClassLoaderForClassArtifacts(Host)
        def method = Object.getDeclaredMethod('toString')
        String name = loader.createClassName(method)
        assertTrue(name.contains('toString'))
    }

    @Test
    void testLoadClassFindsPreviouslyDefinedVisibleClass() {
        // When hidden path is used the binary name is not loadable; force a
        // platform host so fallback defines a visible class under `name`.
        def loader = new ClassLoaderForClassArtifacts(String)
        String name = loader.createClassName('loadable')
        Class<?> defined = loader.define(name, minimalBytes(name.replace('.', '/')))
        if (!defined.isHidden()) {
            assertEquals(defined, loader.loadClass(name))
        }
    }
}
