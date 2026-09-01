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
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle

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
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.ALOAD
import static org.objectweb.asm.Opcodes.ARETURN
import static org.objectweb.asm.Opcodes.ATHROW
import static org.objectweb.asm.Opcodes.DUP
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.NEW
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17

/**
 * Unit tests for {@link HiddenClassDefiner}.
 *
 * <p>The preferred production path is {@code tryDefineNestmate(Lookup, ...)}
 * with a lookup captured in this test class itself (caller-sensitive). The
 * foreign-host overload and its internal {@code canAttemptPrivateLookup}
 * pre-filter are covered as best-effort policy.
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
     * {@code static final} field of the nest-host class itself.
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
    // Status / enablement (runtime kill switch + native image)
    // -------------------------------------------------------------------------

    @Test
    void testHiddenClassesEnabledByDefault() {
        // Assumes no kill-switch / native-image properties from the test runner.
        assertTrue(HiddenClassDefiner.isEnabled())
        assertFalse(HiddenClassDefiner.isNativeImageRuntime())
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testIsEnabledHonoursKillSwitchAtRuntime() {
        String previous = System.getProperty(HiddenClassDefiner.PROPERTY_DISABLE)
        try {
            System.setProperty(HiddenClassDefiner.PROPERTY_DISABLE, 'true')
            assertFalse(HiddenClassDefiner.isEnabled())
            // Soft path must short-circuit without defining
            assertNull(HiddenClassDefiner.tryDefineNestmate(
                    LOOKUP, minimalClassBytes('org/apache/groovy/util/KillSwitch1'), true))
        } finally {
            if (previous == null) {
                System.clearProperty(HiddenClassDefiner.PROPERTY_DISABLE)
            } else {
                System.setProperty(HiddenClassDefiner.PROPERTY_DISABLE, previous)
            }
        }
        assertTrue(HiddenClassDefiner.isEnabled())
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testIsEnabledFalseUnderNativeImageRuntimeProperty() {
        String previous = System.getProperty(HiddenClassDefiner.PROPERTY_NATIVE_IMAGE_CODE)
        try {
            System.setProperty(HiddenClassDefiner.PROPERTY_NATIVE_IMAGE_CODE,
                    HiddenClassDefiner.NATIVE_IMAGE_CODE_RUNTIME)
            assertTrue(HiddenClassDefiner.isNativeImageRuntime())
            assertFalse(HiddenClassDefiner.isEnabled())
            assertNull(HiddenClassDefiner.tryDefineNestmate(
                    LOOKUP, minimalClassBytes('org/apache/groovy/util/NativeImg1'), true))
        } finally {
            if (previous == null) {
                System.clearProperty(HiddenClassDefiner.PROPERTY_NATIVE_IMAGE_CODE)
            } else {
                System.setProperty(HiddenClassDefiner.PROPERTY_NATIVE_IMAGE_CODE, previous)
            }
        }
        assertTrue(HiddenClassDefiner.isEnabled())
    }

    @Test
    void testUnsupportedFeatureErrorIsSoftFailedByName() {
        // Name-checked without a GraalVM dependency (see paulk-asert / native-image notes).
        Error fake = new Error('synthetic') {
            // subclass identity is wrong; match is by FQCN only
        }
        assertFalse(HiddenClassDefiner.isUnsupportedFeatureError(fake))
        assertFalse(HiddenClassDefiner.isUnsupportedFeatureError(null))

        // Load a synthetic Error subclass with the exact GraalVM FQCN
        Class<? extends Error> synthetic = defineSyntheticUnsupportedFeatureError()
        Error graalShaped = synthetic.getDeclaredConstructor(String).newInstance('no defineClass')
        assertTrue(HiddenClassDefiner.isUnsupportedFeatureError(graalShaped))

        // Same branch both tryDefineNestmate overloads use on Error
        assertNull(HiddenClassDefiner.softFailOrRethrow(graalShaped))
        Error other = new AssertionError('must rethrow')
        assertThrows(AssertionError) {
            HiddenClassDefiner.softFailOrRethrow(other)
        }
    }

    /**
     * Defines {@code com.oracle.svm.core.jdk.UnsupportedFeatureError} as a plain
     * Error subclass in a throwaway loader so soft-fail matching can be unit-tested
     * without GraalVM on the classpath.
     */
    private static Class<? extends Error> defineSyntheticUnsupportedFeatureError() {
        String internal = 'com/oracle/svm/core/jdk/UnsupportedFeatureError'
        def cw = new ClassWriter(0)
        cw.visit(V17, ACC_PUBLIC, internal, null, 'java/lang/Error', null)
        def mv = cw.visitMethod(ACC_PUBLIC, '<init>', '(Ljava/lang/String;)V', null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESPECIAL, 'java/lang/Error', '<init>', '(Ljava/lang/String;)V', false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(2, 2)
        mv.visitEnd()
        cw.visitEnd()
        byte[] bytes = cw.toByteArray()
        ClassLoader loader = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name == 'com.oracle.svm.core.jdk.UnsupportedFeatureError') {
                    return defineClass(name, bytes, 0, bytes.length)
                }
                throw new ClassNotFoundException(name)
            }
        }
        (Class<? extends Error>) loader.loadClass('com.oracle.svm.core.jdk.UnsupportedFeatureError')
    }

    @Test
    void testLookupClassDeterminesNestHostNotUtilityClass() {
        // Which class called MethodHandles.lookup() matters — the nest host is
        // lookup.lookupClass(), not HiddenClassDefiner (even though module rights
        // are the same for every runtime-captured lookup).
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/NestHostCheck')
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytes, true)
        assertNotNull(hidden)
        assertEquals(LOOKUP.lookupClass().nestHost, hidden.nestHost)
        assertFalse(hidden.nestHost == HiddenClassDefiner)
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
    // Module-open pre-filter + best-effort foreign host
    // -------------------------------------------------------------------------

    @Test
    void testCanAttemptPrivateLookupPrefilter() {
        // Usable same-module / unnamed application host
        assertTrue(HiddenClassDefiner.canAttemptPrivateLookup(HiddenClassDefinerTest))

        // Unusable shapes
        assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(null))
        assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(Integer.TYPE))
        assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(String[].class))

        // Nestmate of String needs private privileges into java.base.
        // On a stock modular JDK java.lang is not open to the runtime → false.
        // Environments that pass --add-opens may report true; either is valid,
        // but the pre-filter must match what privateLookupIn would allow.
        boolean stringOpen = String.module.isOpen('java.lang', HiddenClassDefiner.module)
        assertEquals(stringOpen, HiddenClassDefiner.canAttemptPrivateLookup(String),
                'pre-filter must mirror Module.isOpen for java.lang')

        // Hidden class cannot host further nestmates
        Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(
                LOOKUP, minimalClassBytes('org/apache/groovy/util/PrefilterHidden'), true)
        assertNotNull(hidden)
        assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(hidden))
    }

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
    void testForeignHostIntoJavaBaseDoesNotThrow() {
        // Try to create a nestmate for String — privateLookupIn into java.lang
        // fails on a stock modular JDK. Soft API must return null, never throw.
        byte[] bytes = minimalClassBytes('java/lang/ShouldNotAppear')
        Class<?> result = HiddenClassDefiner.tryDefineNestmate(String, bytes, true)
        if (!HiddenClassDefiner.canAttemptPrivateLookup(String)) {
            assertNull(result, 'unopened java.lang must soft-fail to null')
        } else if (result != null) {
            // Only when the JVM was launched with --add-opens java.base/java.lang=...
            assertTrue(result.isHidden())
        }
    }

    @Test
    void testForeignHostNullBytesRejected() {
        assertNull(HiddenClassDefiner.tryDefineNestmate(HiddenClassDefinerTest, null, true))
    }

    // -------------------------------------------------------------------------
    // Lookup-returning / classData overloads (DirectInvoker support)
    // -------------------------------------------------------------------------

    @Test
    void testTryDefineNestmateLookupReturnsHiddenLookup() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupRet1')
        Lookup hidden = HiddenClassDefiner.tryDefineNestmateLookup(LOOKUP, bytes, true)
        assertNotNull(hidden)
        assertTrue(hidden.lookupClass().hidden)
        assertEquals(HiddenClassDefinerTest.nestHost, hidden.lookupClass().nestHost)
        assertNotNull(hidden.lookupClass().getConstructor().newInstance())
    }

    @Test
    void testTryDefineNestmateLookupForeignHost() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupRetForeign')
        Lookup hidden = HiddenClassDefiner.tryDefineNestmateLookup(HiddenClassDefinerTest, bytes, true)
        assertNotNull(hidden)
        assertEquals(HiddenClassDefinerTest.nestHost, hidden.lookupClass().nestHost)
    }

    @Test
    void testTryDefineNestmateLookupRejectsNulls() {
        byte[] bytes = minimalClassBytes('org/apache/groovy/util/LookupRetNull')
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup((Lookup) null, bytes, true))
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup(LOOKUP, null, true))
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup((Class) null, bytes, true))
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup(HiddenClassDefinerTest, null, true))
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup(LOOKUP, new byte[]{0, 1, 2, 3}, true))
    }

    @Test
    void testTryDefineNestmateLookupStickyFailsClinitError() {
        byte[] bytes = throwingClinitBytes('org/apache/groovy/util/LookupRetClinit')
        assertNull(HiddenClassDefiner.tryDefineNestmateLookup(LOOKUP, bytes, true))
    }

    @Test
    void testTryDefineNestmateWithClassDataRoundTrip() {
        byte[] bytes = classDataGetterBytes('org/apache/groovy/util/ClassData1')
        Lookup hidden = HiddenClassDefiner.tryDefineNestmateWithClassData(
                LOOKUP, bytes, 'payload', true)
        assertNotNull(hidden)
        Object inst = hidden.lookupClass().getConstructor().newInstance()
        assertEquals('payload', hidden.lookupClass().getMethod('get').invoke(inst))
    }

    @Test
    void testTryDefineNestmateWithClassDataRejectsNullClassData() {
        byte[] bytes = classDataGetterBytes('org/apache/groovy/util/ClassDataNull')
        assertNull(HiddenClassDefiner.tryDefineNestmateWithClassData(LOOKUP, bytes, null, true))
        assertNull(HiddenClassDefiner.tryDefineNestmateWithClassData(null, bytes, 'x', true))
        assertNull(HiddenClassDefiner.tryDefineNestmateWithClassData(LOOKUP, null, 'x', true))
    }

    @Test
    void testTryDefineNestmateLookupIntoJavaBaseDoesNotThrow() {
        byte[] bytes = minimalClassBytes('java/lang/ShouldNotAppearLookup')
        Lookup result = HiddenClassDefiner.tryDefineNestmateLookup(String, bytes, true)
        if (!HiddenClassDefiner.canAttemptPrivateLookup(String)) {
            assertNull(result, 'unopened java.lang must soft-fail to null')
        } else if (result != null) {
            assertTrue(result.lookupClass().hidden)
        }
    }

    private static byte[] throwingClinitBytes(String internalName) {
        def cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
        cw.visit(V17, ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        def init = cw.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null)
        init.visitCode()
        init.visitVarInsn(ALOAD, 0)
        init.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        init.visitInsn(RETURN)
        init.visitMaxs(0, 0)
        init.visitEnd()
        def clinit = cw.visitMethod(ACC_STATIC, '<clinit>', '()V', null, null)
        clinit.visitCode()
        clinit.visitTypeInsn(NEW, 'java/lang/RuntimeException')
        clinit.visitInsn(DUP)
        clinit.visitLdcInsn('clinit-boom')
        clinit.visitMethodInsn(INVOKESPECIAL, 'java/lang/RuntimeException', '<init>', '(Ljava/lang/String;)V', false)
        clinit.visitInsn(ATHROW)
        clinit.visitMaxs(0, 0)
        clinit.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static byte[] classDataGetterBytes(String internalName) {
        def cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
        cw.visit(V17, ACC_PUBLIC, internalName, null, 'java/lang/Object', null)
        def init = cw.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null)
        init.visitCode()
        init.visitVarInsn(ALOAD, 0)
        init.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        init.visitInsn(RETURN)
        init.visitMaxs(0, 0)
        init.visitEnd()
        def bsm = new Handle(H_INVOKESTATIC, 'java/lang/invoke/MethodHandles', 'classData',
                '(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;', false)
        def get = cw.visitMethod(ACC_PUBLIC, 'get', '()Ljava/lang/Object;', null, null)
        get.visitCode()
        get.visitLdcInsn(new ConstantDynamic('_', 'Ljava/lang/String;', bsm))
        get.visitInsn(ARETURN)
        get.visitMaxs(0, 0)
        get.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }
}
