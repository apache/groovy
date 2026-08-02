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
package org.codehaus.groovy.runtime

import org.apache.groovy.util.HiddenClassDefiner
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter

import java.lang.invoke.MethodHandles

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
 * Unit tests for {@link ProxyClassDefiner} policy and define path.
 */
class ProxyClassDefinerTest {

    abstract static class ConcreteHost {
        abstract void m()
    }

    private static byte[] publicNoArgClass(String internalName) {
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
    void testMayDefineHiddenRejectsInterfaceAggregates() {
        // Object super + user interface + no delegate → must stay nameable
        assertFalse(ProxyClassDefiner.mayDefineHidden(
                Object, null, [Object, Iterator] as Set))
    }

    @Test
    void testMayDefineHiddenAllowsConcreteSuper() {
        if (!HiddenClassDefiner.isEnabled()) return
        assertTrue(ProxyClassDefiner.mayDefineHidden(
                ConcreteHost, null, [ConcreteHost] as Set))
    }

    @Test
    void testMayDefineHiddenAllowsTypedDelegate() {
        if (!HiddenClassDefiner.isEnabled()) return
        assertTrue(ProxyClassDefiner.mayDefineHidden(
                Object, ArrayList, [Object, List, ArrayList] as Set))
    }

    @Test
    void testPreferredForeignHostPrefersDelegateThenSuper() {
        assertEquals(ArrayList, ProxyClassDefiner.preferredForeignHost(
                Object, ArrayList, [Object, ArrayList] as Set))
        assertEquals(ConcreteHost, ProxyClassDefiner.preferredForeignHost(
                ConcreteHost, null, [ConcreteHost] as Set))
        assertNull(ProxyClassDefiner.preferredForeignHost(
                Object, null, [Object, Iterator] as Set))
    }

    @Test
    void testLoaderCanResolveParentButNotChild() {
        def childLoader = new GroovyClassLoader(this.class.classLoader)
        Class<?> childType = childLoader.parseClass('class ChildType {}')
        // Parent loader cannot see a type defined by a child loader
        assertFalse(ProxyClassDefiner.loaderCanResolve(String, childType))
        // Child type's loader can see itself
        assertTrue(ProxyClassDefiner.loaderCanResolve(childType, childType))
        // Bootstrap types always visible
        assertTrue(ProxyClassDefiner.loaderCanResolve(childType, String))
    }

    @Test
    void testDefineFallsBackToVisibleWhenHiddenDisabledOrUnsafe() {
        def bytes = publicNoArgClass('org/codehaus/groovy/runtime/VisibleOnly')
        def fallback = { String name, byte[] b ->
            def cl = new GroovyClassLoader(this.class.classLoader)
            cl.defineClass(name, b)
        }
        // Interface-aggregate shape → visible path
        def result = ProxyClassDefiner.define(
                bytes,
                'org.codehaus.groovy.runtime.VisibleOnly',
                Object,
                null,
                [Object, Iterator] as Set,
                MethodHandles.lookup(),
                fallback,
                new Class<?>[0])
        assertNotNull(result.type)
        assertFalse(result.hidden)
        assertNotNull(result.constructor)
    }

    @Test
    void testDefineCanProduceHiddenForConcreteSuper() {
        if (!HiddenClassDefiner.isEnabled()) return
        def bytes = publicNoArgClass('org/codehaus/groovy/runtime/MaybeHidden')
        def fallback = { String name, byte[] b ->
            new GroovyClassLoader(this.class.classLoader).defineClass(name, b)
        }
        def result = ProxyClassDefiner.define(
                bytes,
                'org.codehaus.groovy.runtime.MaybeHidden',
                ConcreteHost,
                null,
                [ConcreteHost] as Set,
                MethodHandles.lookup(),
                fallback,
                new Class<?>[0])
        assertNotNull(result.type)
        // Own LOOKUP (this test class) may not host ConcreteHost package;
        // foreign host ConcreteHost should succeed when privateLookupIn works.
        if (result.hidden) {
            assertTrue(result.type.isHidden())
            assertEquals(ConcreteHost.nestHost, result.type.nestHost)
        }
        assertNotNull(result.constructor)
    }
}
