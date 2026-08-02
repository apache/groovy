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
package org.apache.groovy.util

import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ALOAD
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17

/**
 * Unit tests for {@link HiddenClassDefiner}.
 *
 * <p>The preferred production path is {@code tryDefineNestmate(Lookup, ...)}
 * with a lookup captured in this test class itself (caller-sensitive). The
 * foreign-host overload is covered separately as a best-effort path.
 *
 * @since 6.0.0
 */
class HiddenClassDefinerTest {

    /**
     * Full-privilege lookup for this class. A bare {@code MethodHandles.lookup()}
     * reached through Groovy's indy path can resolve to a synthetic
     * {@code $$InjectedInvoker} hidden class on some JDKs; pin to this class
     * with {@code privateLookupIn} so nest-host assertions are stable.
     * Production Java call sites capture {@code MethodHandles.lookup()} in a
     * {@code static final} field of the nest-host class itself (see
     * {@code ReflectorLoader}, {@code ProxyGeneratorAdapter}).
     */
    private static final Lookup LOOKUP = MethodHandles.privateLookupIn(
            HiddenClassDefinerTest, MethodHandles.lookup())

    private static byte[] minimalClassBytes(String internalName) {
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

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    @Test
    void testHiddenClassesEnabledByDefault() {
        assertTrue(HiddenClassDefiner.isEnabled())
        assertFalse(HiddenClassDefiner.HIDDEN_CLASSES_DISABLED)
    }

    // -------------------------------------------------------------------------
    // Preferred API: caller-owned Lookup
    // -------------------------------------------------------------------------

    @Test
    void testTryDefineNestmateWithLookup() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupNest1')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden)
        assertTrue(hidden.isHidden())
        assertTrue(hidden.name.contains('/'))
        assertEquals(HiddenClassDefinerTest.nestHost, hidden.nestHost)
        assertTrue(HiddenClassDefinerTest.isNestmateOf(hidden))
    }

    @Test
    void testLookupPathUsesLookupClassLoaderAndPackage() {
        // Template package differs from host package — must be rewritten
        byte[] bytes = minimalClassBytes('unrelated/pkg/TemplateName')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden)
        assertSame(HiddenClassDefinerTest.classLoader, hidden.classLoader)
        assertEquals(HiddenClassDefinerTest.packageName, hidden.packageName)
    }

    @Test
    void testHiddenClassIsNotDiscoverable() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupNest2')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden)

        // Binary-name prefix (before the JVM '/' suffix) must not resolve either
        String binaryPrefix = hidden.name.substring(0, hidden.name.indexOf('/'))
        assertThrows(ClassNotFoundException) {
            Class.forName(binaryPrefix)
        }
        assertThrows(ClassNotFoundException) {
            hidden.classLoader.loadClass(binaryPrefix)
        }
        assertThrows(ClassNotFoundException) {
            Class.forName(hidden.name)
        }
    }

    @Test
    void testHiddenClassCanBeInstantiated() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupNest3')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden.getDeclaredConstructor().newInstance())
    }

    @Test
    void testTryDefineNestmateWithLookupRejectsNulls() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupNull')
        assertNull(HiddenClassDefiner.tryDefineNestmate((Lookup) null, bytes, true))
        assertNull(HiddenClassDefiner.tryDefineNestmate(LOOKUP, null, true))
        // Corrupt class-file bytes: soft API must not throw (ClassReader may
        // raise IllegalArgumentException or similar on garbage input).
        assertNull(HiddenClassDefiner.tryDefineNestmate(LOOKUP, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, true))
    }

    // -------------------------------------------------------------------------
    // Best-effort API: foreign host
    // -------------------------------------------------------------------------

    @Test
    void testTryDefineNestmateWithForeignHost() {
        // Same-module unnamed class — privateLookupIn from HiddenClassDefiner works
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/ForeignHost1')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(HiddenClassDefinerTest, bytes, true)
        assertNotNull(hidden)
        assertTrue(hidden.isHidden())
        assertEquals(HiddenClassDefinerTest.nestHost, hidden.nestHost)
    }

    @Test
    void testForeignHostRejectsUnusableTypes() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/ForeignReject')
        assertNull(HiddenClassDefiner.tryDefineNestmate((Class) null, bytes, true))
        assertNull(HiddenClassDefiner.tryDefineNestmate(Integer.TYPE, bytes, true))
        assertNull(HiddenClassDefiner.tryDefineNestmate(String[].class, bytes, true))

        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden)
        // Hidden class cannot host further nestmates via the Class overload
        assertNull(HiddenClassDefiner.tryDefineNestmate(hidden, bytes, true))
    }

    @Test
    void testForeignHostIntoJavaBaseTypicallyFails() {
        // java.lang is not open to unnamed modules → privateLookupIn fails → null
        // (unless the JVM was launched with --add-opens). Soft API must not throw.
        byte[] bytes = minimalClassBytes('java/lang/ShouldNotAppear')
        Class<?> result = HiddenClassDefiner.tryDefineNestmate(String, bytes, true)
        // On a stock JDK this is null; if the environment opens java.lang, a
        // hidden class is still a valid outcome. Never throw.
        if (result != null) {
            assertTrue(result.isHidden())
        }
    }
}
