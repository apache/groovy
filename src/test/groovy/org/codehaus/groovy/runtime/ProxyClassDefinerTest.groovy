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

    private static byte[] classWithMapCtor(String internalName) {
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        def mv = cw.visitMethod(ACC_PUBLIC, '<init>', '(Ljava/util/Map;)V', null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 2)
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
    void testMayDefineHiddenWithoutUserInterfaces() {
        // null / only Object / GroovyObject → no user interface → may be hidden
        if (!HiddenClassDefiner.isEnabled()) return
        assertTrue(ProxyClassDefiner.mayDefineHidden(Object, null, null))
        assertTrue(ProxyClassDefiner.mayDefineHidden(Object, null, [Object] as Set))
        assertTrue(ProxyClassDefiner.mayDefineHidden(Object, null, [Object, GroovyObject] as Set))
    }

    @Test
    void testPreferredForeignHostPrefersOpenHostsAndSkipsPlatform() {
        // Application host in an open / unnamed package is preferred
        assertEquals(ConcreteHost, ProxyClassDefiner.preferredForeignHost(
                ConcreteHost, null, [ConcreteHost] as Set))

        // Delegate preferred over super when both are candidates
        assertEquals(ConcreteHost, ProxyClassDefiner.preferredForeignHost(
                Object, ConcreteHost, [Object, ConcreteHost] as Set))

        // No usable foreign host for pure interface aggregate shape
        assertNull(ProxyClassDefiner.preferredForeignHost(
                Object, null, [Object, Iterator] as Set))

        // ArrayList lives in java.base and is not open to the runtime on a stock
        // JDK → must not be selected as foreign host (avoids a doomed
        // privateLookupIn). Own Lookup / visible fallback handle it.
        if (!HiddenClassDefiner.canAttemptPrivateLookup(ArrayList)) {
            assertNull(ProxyClassDefiner.preferredForeignHost(
                    Object, ArrayList, [Object, ArrayList] as Set),
                    'unopened java.util.ArrayList must not be a foreign host')
        }
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
        // null / primitive types are always "resolvable"
        assertTrue(ProxyClassDefiner.loaderCanResolve(String, null))
        assertTrue(ProxyClassDefiner.loaderCanResolve(String, Integer.TYPE))
    }

    @Test
    void testCanResolveAllRequiresEveryDependency() {
        assertTrue(ProxyClassDefiner.canResolveAll(
                ConcreteHost, ConcreteHost, null, [ConcreteHost] as Set))
        assertTrue(ProxyClassDefiner.canResolveAll(
                ConcreteHost, ConcreteHost, null, null))

        def childLoader = new GroovyClassLoader(this.class.classLoader)
        Class<?> childType = childLoader.parseClass('class Unseen {}')
        // Host in parent loader cannot see a child-defined type
        assertFalse(ProxyClassDefiner.canResolveAll(
                String, Object, null, [childType] as Set))
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
        // Foreign host ConcreteHost should succeed when privateLookupIn works.
        if (result.hidden) {
            assertTrue(result.type.isHidden())
            assertEquals(ConcreteHost.nestHost, result.type.nestHost)
        }
        assertNotNull(result.constructor)
    }

    @Test
    void testDefineUsesOwnLookupWhenForeignHostUnopened() {
        if (!HiddenClassDefiner.isEnabled()) return
        // Typed delegate is ArrayList (java.base). Foreign try is skipped when
        // the package is not open; own Lookup from this test class may still
        // host the nestmate if every dependency is resolvable.
        def bytes = publicNoArgClass('org/codehaus/groovy/runtime/OwnLookupHost')
        def fallback = { String name, byte[] b ->
            new GroovyClassLoader(this.class.classLoader).defineClass(name, b)
        }
        def ownLookup = MethodHandles.privateLookupIn(
                ProxyClassDefinerTest, MethodHandles.lookup())
        def result = ProxyClassDefiner.define(
                bytes,
                'org.codehaus.groovy.runtime.OwnLookupHost',
                Object,
                ArrayList,
                [Object, ArrayList] as Set,
                ownLookup,
                fallback,
                new Class<?>[0])
        assertNotNull(result.type)
        assertNotNull(result.constructor)
        if (result.hidden && !HiddenClassDefiner.canAttemptPrivateLookup(ArrayList)) {
            // Nest host is the own-lookup class, not ArrayList
            assertEquals(ProxyClassDefinerTest.nestHost, result.type.nestHost)
        }
    }

    @Test
    void testAcceptRejectsMissingPublicConstructor() {
        if (!HiddenClassDefiner.isEnabled()) return
        // Class with only no-arg ctor, but we ask for Map ctor → accept() nulls out
        // and define falls back to visible (which also won't have Map ctor unless we define it).
        def bytes = publicNoArgClass('org/codehaus/groovy/runtime/NoMapCtor')
        def fallback = { String name, byte[] b ->
            new GroovyClassLoader(this.class.classLoader).defineClass(name, b)
        }
        def result = ProxyClassDefiner.define(
                bytes,
                'org.codehaus.groovy.runtime.NoMapCtor',
                ConcreteHost,
                null,
                [ConcreteHost] as Set,
                MethodHandles.lookup(),
                fallback,
                new Class<?>[]{Map})
        // Visible fallback also has no Map ctor → constructor is null
        assertNotNull(result.type)
        assertNull(result.constructor)
        assertFalse(result.hidden)
    }

    @Test
    void testResolvePublicConstructor() {
        assertNotNull(ProxyClassDefiner.resolvePublicConstructor(String, new Class<?>[]{String}))
        assertNull(ProxyClassDefiner.resolvePublicConstructor(String, new Class<?>[]{Object}))
    }

    @Test
    void testDefineHiddenWithMatchingCtorArgs() {
        if (!HiddenClassDefiner.isEnabled()) return
        def bytes = classWithMapCtor('org/codehaus/groovy/runtime/MapCtorHidden')
        def fallback = { String name, byte[] b ->
            new GroovyClassLoader(this.class.classLoader).defineClass(name, b)
        }
        def result = ProxyClassDefiner.define(
                bytes,
                'org.codehaus.groovy.runtime.MapCtorHidden',
                ConcreteHost,
                null,
                [ConcreteHost] as Set,
                MethodHandles.lookup(),
                fallback,
                new Class<?>[]{Map})
        assertNotNull(result.type)
        assertNotNull(result.constructor)
        if (result.hidden) {
            assertTrue(result.type.isHidden())
        }
    }
}
