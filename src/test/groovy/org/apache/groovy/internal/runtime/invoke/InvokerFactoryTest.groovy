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
package org.apache.groovy.internal.runtime.invoke

import org.apache.groovy.util.HiddenClassDefiner
import org.codehaus.groovy.reflection.CachedMethod
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.module.Configuration
import java.lang.module.ModuleFinder
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit tests for {@link InvokerFactory} / {@link InvokerBytecode}.
 *
 * <p>Uses {@code new CachedMethod(method)} so generation does not install a
 * trampoline on the interned MOP {@code CachedMethod}.
 */
final class InvokerFactoryTest {

    private static CachedMethod cm(Method m) {
        new CachedMethod(m)
    }

    private static DirectInvoker invoker(Method m) {
        DirectInvoker di = InvokerFactory.tryCreate(cm(m))
        assertNotNull(di, "expected DirectInvoker for ${m}")
        di
    }

    // -------------------------------------------------------------------------
    // Step 1 — InvokerFactory nestmate + INVOKE* (bootstrap public)
    // -------------------------------------------------------------------------

    @Test
    void testStringStartsWithIsInvokerFactoryNestmate() {
        DirectInvoker di = invoker(String.getMethod('startsWith', String))
        assertEquals(Boolean.TRUE, di.invoke('abc', ['a'] as Object[]))
        assertEquals(Boolean.FALSE, di.invoke('abc', ['z'] as Object[]))
        assertSame(InvokerFactory, di.class.nestHost)
        assertTrue(di.class.hidden)
    }

    @Test
    void testIntegerParseIntStatic() {
        DirectInvoker di = invoker(Integer.getMethod('parseInt', String))
        assertEquals(Integer.valueOf(42), di.invoke(null, ['42'] as Object[]))
        assertSame(InvokerFactory, di.class.nestHost)
    }

    @Test
    void testPublicInstanceOnTestSubject() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('ping'))
        assertEquals('pong', di.invoke(new DirectInvokerSubjects(), null))
        assertSame(InvokerFactory, di.class.nestHost)
    }

    @Test
    void testPublicStaticOnTestSubject() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('staticPing'))
        assertEquals('static-pong', di.invoke(null, null))
    }

    @Test
    void testVoidReturnIsNull() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('noop'))
        assertNull(di.invoke(new DirectInvokerSubjects(), null))
    }

    @Test
    void testPrimitiveArgsAndReturn() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('add', int, int))
        assertEquals(Integer.valueOf(7), di.invoke(new DirectInvokerSubjects(), [3, 4] as Object[]))
    }

    @Test
    void testBooleanPrimitive() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('flag', boolean))
        assertEquals(Boolean.TRUE, di.invoke(new DirectInvokerSubjects(), [true] as Object[]))
    }

    @Test
    void testInterfaceDefaultMethod() {
        DirectInvoker di = invoker(DirectInvokerSubjects.Defaults.getMethod('greet'))
        assertEquals('hello', di.invoke(new DirectInvokerSubjects.DefaultsImpl(), null))
        assertEquals(Opcodes.INVOKEINTERFACE, InvokerBytecode.invokeOpcode(
                DirectInvokerSubjects.Defaults.getMethod('greet')))
    }

    @Test
    void testInterfaceStaticMethod() {
        DirectInvoker di = invoker(DirectInvokerSubjects.Defaults.getMethod('staticGreet'))
        assertEquals('static-hello', di.invoke(null, null))
        assertEquals(Opcodes.INVOKESTATIC, InvokerBytecode.invokeOpcode(
                DirectInvokerSubjects.Defaults.getMethod('staticGreet')))
    }

    @Test
    void testPrivateInterfaceMethodUsesInvokeInterface() {
        Method hidden = DirectInvokerSubjects.Defaults.getDeclaredMethod('hidden')
        assertEquals(Opcodes.INVOKEINTERFACE, InvokerBytecode.invokeOpcode(hidden))
        DirectInvoker di = invoker(hidden)
        assertEquals('hidden-iface', di.invoke(new DirectInvokerSubjects.DefaultsImpl(), null))
        assertEquals(DirectInvokerSubjects.Defaults.nestHost, di.class.nestHost)
    }

    @Test
    void testNullArgumentsTreatedAsEmptyByCaller() {
        // Bytecode assumes non-null args; CachedMethod.invokeGenerated substitutes EMPTY_ARRAY.
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('ping'))
        assertEquals('pong', di.invoke(new DirectInvokerSubjects(), new Object[0]))
    }

    @Test
    void testVarargsAlreadyPacked() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('join', String, String[]))
        assertEquals('ab', di.invoke(new DirectInvokerSubjects(), ['a', ['b'] as String[]] as Object[]))
    }

    // -------------------------------------------------------------------------
    // Step 2 — declaring-class nestmate (non-public members)
    // -------------------------------------------------------------------------

    @Test
    void testPrivateInstanceNestHostIsDeclaringClass() {
        Method secret = DirectInvokerSubjects.getDeclaredMethod('secret')
        DirectInvoker di = invoker(secret)
        assertEquals('secret', di.invoke(new DirectInvokerSubjects(), null))
        assertEquals(DirectInvokerSubjects.nestHost, di.class.nestHost)
        assertEquals(Opcodes.INVOKEVIRTUAL, InvokerBytecode.invokeOpcode(secret))
    }

    @Test
    void testPrivateStaticUsesInvokeStatic() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getDeclaredMethod('staticSecret'))
        assertEquals('static-secret', di.invoke(null, null))
        assertSame(DirectInvokerSubjects, di.class.nestHost)
        assertEquals(Opcodes.INVOKESTATIC, InvokerBytecode.invokeOpcode(
                DirectInvokerSubjects.getDeclaredMethod('staticSecret')))
    }

    @Test
    void testProtectedInstance() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getDeclaredMethod('protectedPing'))
        assertEquals('protected-pong', di.invoke(new DirectInvokerSubjects(), null))
        assertSame(DirectInvokerSubjects, di.class.nestHost)
    }

    @Test
    void testPackagePrivateInstance() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getDeclaredMethod('packagePing'))
        assertEquals('package-pong', di.invoke(new DirectInvokerSubjects(), null))
        assertSame(DirectInvokerSubjects, di.class.nestHost)
    }

    @Test
    void testPublicMethodOnNonPublicClass() {
        DirectInvoker di = invoker(DirectInvokerSubjects.PackageHost.getMethod('visible'))
        assertEquals('pkg-visible', di.invoke(new DirectInvokerSubjects.PackageHost(), null))
        // Nested class nest host is the outermost class, not PackageHost itself.
        assertEquals(DirectInvokerSubjects.PackageHost.nestHost, di.class.nestHost)
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(DirectInvokerSubjects.PackageHost.getMethod('visible'))))
    }

    @Test
    void testGroovyClassLoaderPublicMethodIsDeclaringClassNestmate() {
        GroovyClassLoader gcl = new GroovyClassLoader(InvokerFactory.classLoader)
        try {
            Class<?> host = gcl.parseClass('''
                package gcl.directinvoker
                class GclDirectInvokerHost { String ping() { "gcl-pong" } }
            ''')
            DirectInvoker di = invoker(host.getMethod('ping'))
            assertEquals('gcl-pong', di.invoke(host.getDeclaredConstructor().newInstance(), null))
            // Child-loader types may land on Step 2 (declaring-class nestmate)
            // or Step 4 (ClassLoaderForClassArtifacts visible class) when
            // privateLookupIn into the GroovyClassLoader type is unavailable.
        } finally {
            gcl.close()
        }
    }

    // -------------------------------------------------------------------------
    // Step 3 — classData encoding (forced)
    // -------------------------------------------------------------------------

    @Test
    void testClassDataEncodingInvokesPublicMethod() {
        DirectInvoker di = InvokerFactory.tryCreateClassData(
                cm(DirectInvokerSubjects.getMethod('echo', String)))
        assertNotNull(di)
        assertEquals('xyz', di.invoke(new DirectInvokerSubjects(), ['xyz'] as Object[]))
        assertSame(InvokerFactory, di.class.nestHost)
    }

    @Test
    void testClassDataEncodingStaticPrimitive() {
        DirectInvoker di = InvokerFactory.tryCreateClassData(
                cm(Integer.getMethod('parseInt', String)))
        assertNotNull(di)
        assertEquals(Integer.valueOf(7), di.invoke(null, ['7'] as Object[]))
    }

    // -------------------------------------------------------------------------
    // Step 4 — visible ClassLoaderForClassArtifacts (forced)
    // -------------------------------------------------------------------------

    @Test
    void testVisibleArtifactEncodingOnGroovyClassLoaderHost() {
        GroovyClassLoader gcl = new GroovyClassLoader(InvokerFactory.classLoader)
        try {
            Class<?> host = gcl.parseClass('''
                package gcl.directinvoker
                class GclVisibleArtifactHost { String ping() { "gcl-visible" } }
            ''')
            DirectInvoker di = InvokerFactory.tryCreateVisibleArtifact(cm(host.getMethod('ping')))
            assertNotNull(di, 'Step 4 must define a visible artifact for a GCL host')
            assertFalse(di.class.hidden)
            assertEquals('gcl-visible', di.invoke(host.getDeclaredConstructor().newInstance(), null))
        } finally {
            gcl.close()
        }
    }

    // -------------------------------------------------------------------------
    // Gates
    // -------------------------------------------------------------------------

    @Test
    void testCallerSensitiveIsNotGenerated() {
        assertNull(InvokerFactory.tryCreate(cm(Class.getMethod('forName', String))))
    }

    @Test
    void testAbstractIsNotGenerated() {
        assertNull(InvokerFactory.tryCreate(
                cm(DirectInvokerSubjects.AbstractHost.getMethod('abs'))))
    }

    @Test
    void testNullMethodIsNotGenerated() {
        assertNull(InvokerFactory.tryCreate(null))
        assertNull(InvokerFactory.tryCreateClassData(null))
        assertNull(InvokerFactory.tryCreateVisibleArtifact(null))
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testKillSwitchDisablesGeneration() {
        String previous = System.getProperty(InvokerFactory.PROPERTY_DISABLE)
        try {
            System.setProperty(InvokerFactory.PROPERTY_DISABLE, 'true')
            assertFalse(InvokerFactory.generationAllowed())
            assertNull(InvokerFactory.tryCreate(cm(DirectInvokerSubjects.getMethod('ping'))))
        } finally {
            restoreProperty(InvokerFactory.PROPERTY_DISABLE, previous)
        }
        assertTrue(InvokerFactory.generationAllowed())
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testHiddenClassKillSwitchDisablesGeneration() {
        String previous = System.getProperty(org.apache.groovy.util.HiddenClassDefiner.PROPERTY_DISABLE)
        try {
            System.setProperty(org.apache.groovy.util.HiddenClassDefiner.PROPERTY_DISABLE, 'true')
            assertFalse(InvokerFactory.generationAllowed())
            assertNull(InvokerFactory.tryCreate(cm(DirectInvokerSubjects.getMethod('ping'))))
        } finally {
            restoreProperty(org.apache.groovy.util.HiddenClassDefiner.PROPERTY_DISABLE, previous)
        }
        assertTrue(InvokerFactory.generationAllowed())
    }

    @Test
    void testPrivateJavaLangMethodStickyFailsOnStockJdk() {
        // Private java.lang member: Step 1 no, Step 2 usually no, Step 3 unreflect
        // fails under strong encapsulation, Step 4 refused for bootstrap.
        Method m = String.declaredMethods.find { method ->
            java.lang.reflect.Modifier.isPrivate(method.modifiers) &&
                    !method.synthetic &&
                    !java.lang.reflect.Modifier.isAbstract(method.modifiers)
        }
        if (m == null) {
            return
        }
        CachedMethod cached = cm(m)
        if (cached.callerSensitive) {
            assertNull(InvokerFactory.tryCreate(cached))
            return
        }
        DirectInvoker di = InvokerFactory.tryCreate(cached)
        // Stock modular JDK: null. --add-opens may succeed.
        if (di != null) {
            assertNotNull(di.class)
        }
    }

    @Test
    void testIsPubliclyInvocableFromInvokerFactory() {
        assertTrue(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(String.getMethod('startsWith', String))))
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(DirectInvokerSubjects.getDeclaredMethod('secret'))))
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(DirectInvokerSubjects.PackageHost.getMethod('visible'))))
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(DirectInvokerSubjects.getMethod('takesPackageHost', DirectInvokerSubjects.PackageHost))))
    }

    @Test
    void testLoaderCanResolveBootstrapAndSelf() {
        assertTrue(InvokerFactory.loaderCanResolve(InvokerFactory, String))
        assertTrue(InvokerFactory.loaderCanResolve(InvokerFactory, int))
        assertTrue(InvokerFactory.loaderCanResolve(InvokerFactory, DirectInvoker))
        assertFalse(InvokerFactory.loaderCanResolve(String, DirectInvoker))
        assertTrue(InvokerFactory.loaderCanResolve(null, int))
        assertFalse(InvokerFactory.loaderCanResolve(null, DirectInvoker))
    }

    @Test
    void testInvokeExactDescriptor() {
        Method ping = DirectInvokerSubjects.getMethod('ping')
        assertEquals('(Lorg/apache/groovy/internal/runtime/invoke/DirectInvokerSubjects;)Ljava/lang/String;',
                InvokerBytecode.invokeExactDescriptor(ping))
        Method parse = Integer.getMethod('parseInt', String)
        assertEquals('(Ljava/lang/String;)I', InvokerBytecode.invokeExactDescriptor(parse))
        Method add = DirectInvokerSubjects.getMethod('add', int, int)
        assertEquals('(Lorg/apache/groovy/internal/runtime/invoke/DirectInvokerSubjects;II)I',
                InvokerBytecode.invokeExactDescriptor(add))
    }

    @Test
    void testInvokeOpcodePublicVirtual() {
        assertEquals(Opcodes.INVOKEVIRTUAL, InvokerBytecode.invokeOpcode(
                DirectInvokerSubjects.getMethod('ping')))
    }

    @Test
    void testTargetExceptionsPropagateAsThrown() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('boomRuntime'))
        IllegalStateException ex = assertThrows(IllegalStateException) {
            di.invoke(new DirectInvokerSubjects(), null)
        }
        assertEquals('runtime-boom', ex.message)
    }

    // -------------------------------------------------------------------------
    // Primitive boxing / identity / descriptors (generated trampoline)
    // -------------------------------------------------------------------------

    @Test
    void testAllPrimitiveReturnsAndArgsAreBoxed() {
        DirectInvokerSubjects s = new DirectInvokerSubjects()
        assertEquals(Byte.valueOf((byte) 42),
                invoker(DirectInvokerSubjects.getMethod('boxedByte', byte)).invoke(s, [(byte) 42] as Object[]))
        assertEquals(Short.valueOf((short) 7),
                invoker(DirectInvokerSubjects.getMethod('boxedShort', short)).invoke(s, [(short) 7] as Object[]))
        assertEquals(Character.valueOf('Z' as char),
                invoker(DirectInvokerSubjects.getMethod('boxedChar', char)).invoke(s, ['Z' as char] as Object[]))
        assertEquals(Long.valueOf(99L),
                invoker(DirectInvokerSubjects.getMethod('boxedLong', long)).invoke(s, [99L] as Object[]))
        assertEquals(Float.valueOf(1.5f),
                invoker(DirectInvokerSubjects.getMethod('boxedFloat', float)).invoke(s, [1.5f] as Object[]))
        assertEquals(Double.valueOf(2.25d),
                invoker(DirectInvokerSubjects.getMethod('boxedDouble', double)).invoke(s, [2.25d] as Object[]))
    }

    @Test
    void testObjectIdentityAndIntArrayRoundTrip() {
        DirectInvokerSubjects s = new DirectInvokerSubjects()
        Object sentinel = new Object()
        assertSame(sentinel, invoker(DirectInvokerSubjects.getMethod('identity', Object)).invoke(s, [sentinel] as Object[]))
        int[] ints = [1, 2, 3] as int[]
        assertSame(ints, invoker(DirectInvokerSubjects.getMethod('copyInts', int[])).invoke(s, [ints] as Object[]))
    }

    @Test
    void testStaticVoidReturnIsNull() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('staticNoop'))
        assertNull(di.invoke(null, null))
        assertEquals(Opcodes.INVOKESTATIC, InvokerBytecode.invokeOpcode(
                DirectInvokerSubjects.getMethod('staticNoop')))
        assertEquals('()V', InvokerBytecode.invokeExactDescriptor(
                DirectInvokerSubjects.getMethod('staticNoop')))
    }

    @Test
    void testInvokeExactDescriptorVoidAndArray() {
        assertEquals(
                '(Lorg/apache/groovy/internal/runtime/invoke/DirectInvokerSubjects;)V',
                InvokerBytecode.invokeExactDescriptor(DirectInvokerSubjects.getMethod('noop')))
        assertEquals(
                '(Lorg/apache/groovy/internal/runtime/invoke/DirectInvokerSubjects;[I)[I',
                InvokerBytecode.invokeExactDescriptor(DirectInvokerSubjects.getMethod('copyInts', int[])))
        assertEquals(
                '(Lorg/apache/groovy/internal/runtime/invoke/DirectInvokerSubjects;Ljava/lang/Object;)Ljava/lang/Object;',
                InvokerBytecode.invokeExactDescriptor(DirectInvokerSubjects.getMethod('identity', Object)))
    }

    @Test
    void testNextInternalNameIsUniqueAndInInvokerPackage() {
        String a = InvokerBytecode.nextInternalName()
        String b = InvokerBytecode.nextInternalName()
        assertNotEquals(a, b)
        assertTrue(a.startsWith('org/apache/groovy/internal/runtime/invoke/MHInvoker$'))
        assertTrue(b.startsWith('org/apache/groovy/internal/runtime/invoke/MHInvoker$'))
    }

    @Test
    void testGeneratedClassShape() {
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('ping'))
        Class<?> cls = di.class
        assertTrue(Modifier.isPublic(cls.modifiers))
        assertTrue(Modifier.isFinal(cls.modifiers))
        assertTrue(cls.synthetic)
        assertTrue(DirectInvoker.isAssignableFrom(cls))
        assertTrue(cls.hidden)
    }

    @Test
    void testIsPubliclyInvocableRejectsNonPublicReturnType() {
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(
                cm(DirectInvokerSubjects.getMethod('getPackageHost'))))
        DirectInvoker di = invoker(DirectInvokerSubjects.getMethod('getPackageHost'))
        assertEquals('pkg-visible', di.invoke(new DirectInvokerSubjects(), null).visible())
        // Non-public return type fails Step 1, so this is a declaring-class nestmate.
        assertEquals(DirectInvokerSubjects.nestHost, di.class.nestHost)
    }

    @Test
    void testLoaderCanResolveNullType() {
        assertTrue(InvokerFactory.loaderCanResolve(InvokerFactory, null))
        assertTrue(InvokerFactory.loaderCanResolve(null, null))
    }

    // -------------------------------------------------------------------------
    // Forced Step 3 through defineSteps (hidden non-public nestmate)
    // -------------------------------------------------------------------------

    @Test
    void testDefineStepsFallsThroughToClassDataForHiddenNonPublicHost() {
        byte[] bytes = emitStringPingClass(
                'org/apache/groovy/internal/runtime/invoke/Step3HiddenHost',
                'ping', 'step3-pong', false)
        Class<?> hiddenHost = HiddenClassDefiner.tryDefineNestmate(InvokerFactory.LOOKUP, bytes, true)
        assertNotNull(hiddenHost)
        // Hidden-class binary names are not Class.forName-loadable: call
        // Class methods through Java reflection so Groovy MOP never dispatches
        // on the hidden type.
        assertTrue((Boolean) Class.getMethod('isHidden').invoke(hiddenHost))
        assertFalse(Modifier.isPublic((Integer) Class.getMethod('getModifiers').invoke(hiddenHost)))
        assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(hiddenHost))

        Method ping = javaGetMethod(hiddenHost, 'ping')
        assertFalse(InvokerFactory.isPubliclyInvocableFromInvokerFactory(cm(ping)))
        DirectInvoker di = InvokerFactory.tryCreate(cm(ping))
        assertNotNull(di, 'Step 3 classData must succeed when Steps 1–2 cannot')
        assertTrue(di.class.hidden)
        assertSame(InvokerFactory, di.class.nestHost)
        // The trampoline CHECKCASTs the receiver to the hidden host name, which is
        // not Class.forName-loadable — do not invoke. Production CachedMethods
        // wrap ordinary types; this fixture exists to force the Step 3 fall-through.
    }

    // -------------------------------------------------------------------------
    // Forced Step 4 through defineSteps (exported, unopened named module)
    // -------------------------------------------------------------------------

    @Test
    void testDefineStepsFallsThroughToVisibleArtifactForUnopenedNamedModule() {
        Path root = Files.createTempDirectory('groovy-invoker-step4-')
        try {
            String moduleName = 'groovy.invoker.step4'
            String pkg = 'cov.step4'
            String binary = pkg + '.Host'
            Files.createDirectories(root.resolve('cov/step4'))
            Files.write(root.resolve('module-info.class'), emitModuleInfo(moduleName, 'cov/step4'))
            Files.write(root.resolve('cov/step4/Host.class'),
                    emitStringPingClass('cov/step4/Host', 'ping', 'step4-pong', true))

            ModuleFinder finder = ModuleFinder.of(root)
            ModuleLayer parent = ModuleLayer.boot()
            Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of(moduleName))
            ModuleLayer layer = parent.defineModulesWithOneLoader(cf, InvokerFactory.classLoader)
            Class<?> host = layer.findLoader(moduleName).loadClass(binary)
            assertTrue(Modifier.isPublic(host.modifiers))
            assertFalse(HiddenClassDefiner.canAttemptPrivateLookup(host),
                    'exported-but-unopened package must fail privateLookupIn')
            assertFalse(InvokerFactory.loaderCanResolve(InvokerFactory, host),
                    'child module loader is not visible from InvokerFactory')
            assertTrue(InvokerFactory.loaderCanResolve(host, DirectInvoker),
                    'module loader parent is the Groovy loader, so DirectInvoker is visible')

            DirectInvoker di = InvokerFactory.tryCreate(cm(host.getMethod('ping')))
            assertNotNull(di, 'Step 4 visible artifact must succeed when Steps 1–3 cannot')
            assertFalse(di.class.hidden)
            assertEquals('step4-pong', di.invoke(host.getConstructor().newInstance(), null))
        } finally {
            deleteRecursively(root)
        }
    }

    // -------------------------------------------------------------------------
    // Isolated loader: all four steps fail, reflective invoke still works
    // -------------------------------------------------------------------------

    @Test
    void testIsolatedBootstrapChildCannotGenerate() {
        Class<?> iso = defineIsolatedPublic('iso.IsoHost', 'ping', 'iso-pong')
        CachedMethod cached = cm(iso.getMethod('ping'))
        assertNull(InvokerFactory.tryCreate(cached))
        assertTrue(HiddenClassDefiner.canAttemptPrivateLookup(iso))
        assertFalse(InvokerFactory.loaderCanResolve(iso, DirectInvoker))
        Object instance = iso.getConstructor().newInstance()
        assertEquals('iso-pong', cached.invoke(instance, null))
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testCachedMethodStickyFailsWhenAllDefineStepsFail() {
        Class<?> iso = defineIsolatedPublic('iso.StickyHost', 'ping', 'sticky-pong')
        String previous = System.getProperty(InvokerFactory.PROPERTY_THRESHOLD)
        String disable = System.getProperty(InvokerFactory.PROPERTY_DISABLE)
        try {
            System.setProperty(InvokerFactory.PROPERTY_THRESHOLD, '0')
            System.clearProperty(InvokerFactory.PROPERTY_DISABLE)
            CachedMethod cached = new CachedMethod(iso.getMethod('ping'))
            Object instance = iso.getConstructor().newInstance()
            assertEquals('sticky-pong', cached.invoke(instance, null))
            def attempted = CachedMethod.getDeclaredField('invokerAttempted')
            attempted.accessible = true
            assertTrue((Boolean) attempted.get(cached))
            def invokerField = CachedMethod.getDeclaredField('invoker')
            invokerField.accessible = true
            assertNull(invokerField.get(cached))
            assertEquals('sticky-pong', cached.invoke(instance, null))
        } finally {
            restoreProperty(InvokerFactory.PROPERTY_THRESHOLD, previous)
            restoreProperty(InvokerFactory.PROPERTY_DISABLE, disable)
        }
    }

    // -------------------------------------------------------------------------
    // classData / visible-artifact helpers: disable, bootstrap, exceptions
    // -------------------------------------------------------------------------

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testTryCreateClassDataAndVisibleArtifactHonourKillSwitch() {
        String previous = System.getProperty(InvokerFactory.PROPERTY_DISABLE)
        try {
            System.setProperty(InvokerFactory.PROPERTY_DISABLE, 'true')
            assertNull(InvokerFactory.tryCreateClassData(cm(DirectInvokerSubjects.getMethod('ping'))))
            assertNull(InvokerFactory.tryCreateVisibleArtifact(cm(DirectInvokerSubjects.getMethod('ping'))))
        } finally {
            restoreProperty(InvokerFactory.PROPERTY_DISABLE, previous)
        }
    }

    @Test
    void testTryCreateVisibleArtifactOnBootstrapHostFails() {
        // Artifact loader parent is bootstrap, so DirectInvoker is not resolvable.
        assertNull(InvokerFactory.tryCreateVisibleArtifact(cm(String.getMethod('length'))))
    }

    @Test
    void testTryCreateVisibleArtifactOnSameModuleHostInvokes() {
        DirectInvoker di = InvokerFactory.tryCreateVisibleArtifact(cm(DirectInvokerSubjects.getMethod('ping')))
        assertNotNull(di)
        assertEquals('pong', di.invoke(new DirectInvokerSubjects(), null))
    }

    @Test
    void testClassDataEncodingVoidAndPrimitive() {
        DirectInvoker v = InvokerFactory.tryCreateClassData(cm(DirectInvokerSubjects.getMethod('noop')))
        assertNotNull(v)
        assertNull(v.invoke(new DirectInvokerSubjects(), new Object[0]))
        DirectInvoker add = InvokerFactory.tryCreateClassData(
                cm(DirectInvokerSubjects.getMethod('add', int, int)))
        assertNotNull(add)
        assertEquals(Integer.valueOf(9), add.invoke(new DirectInvokerSubjects(), [4, 5] as Object[]))
    }

    @Test
    void testTryCreateSwallowsExceptionFromDefineSteps() {
        CachedMethod exploding = new CachedMethod(DirectInvokerSubjects.getMethod('ping')) {
            @Override
            Method getCachedMethod() {
                throw new IllegalStateException('define-boom')
            }
        }
        assertNull(InvokerFactory.tryCreate(exploding))
    }

    @Test
    void testTryCreateSwallowsLinkageErrorFromDefineSteps() {
        CachedMethod exploding = new CachedMethod(DirectInvokerSubjects.getMethod('ping')) {
            @Override
            Method getCachedMethod() {
                throw new NoClassDefFoundError('define-ncdfe')
            }
        }
        assertNull(InvokerFactory.tryCreate(exploding))
    }

    @Test
    void testTryCreateDoesNotSwallowAssertionError() {
        CachedMethod exploding = new CachedMethod(DirectInvokerSubjects.getMethod('ping')) {
            @Override
            Method getCachedMethod() {
                throw new AssertionError('must-propagate')
            }
        }
        AssertionError ex = assertThrows(AssertionError) {
            InvokerFactory.tryCreate(exploding)
        }
        assertEquals('must-propagate', ex.message)
    }

    @Test
    void testTryCreateClassDataSwallowsException() {
        CachedMethod exploding = new CachedMethod(DirectInvokerSubjects.getMethod('ping')) {
            @Override
            Method getCachedMethod() {
                throw new IllegalStateException('classdata-boom')
            }
        }
        assertNull(InvokerFactory.tryCreateClassData(exploding))
    }

    @Test
    void testTryCreateVisibleArtifactSwallowsException() {
        CachedMethod exploding = new CachedMethod(DirectInvokerSubjects.getMethod('ping')) {
            @Override
            Method getCachedMethod() {
                throw new IllegalStateException('visible-boom')
            }
        }
        assertNull(InvokerFactory.tryCreateVisibleArtifact(exploding))
    }

    // -------------------------------------------------------------------------
    // Private helpers via reflection (null / failure instantiate paths)
    // -------------------------------------------------------------------------

    @Test
    void testInstantiateClassNullAndWrongTypeAndMissingCtor() {
        Method instantiate = InvokerFactory.getDeclaredMethod('instantiate', Class)
        instantiate.accessible = true
        assertNull(instantiate.invoke(null, (Class) null))
        assertNull(instantiate.invoke(null, String))
        assertNull(instantiate.invoke(null, DirectInvokerSubjects.NoNoArgConstructor))
    }

    @Test
    void testInstantiateLookupNullMissingCtorAndNonInvoker() {
        Method instantiate = InvokerFactory.getDeclaredMethod('instantiate', Lookup)
        instantiate.accessible = true
        assertNull(instantiate.invoke(null, (Lookup) null))

        Lookup noArg = MethodHandles.privateLookupIn(
                DirectInvokerSubjects.NoNoArgConstructor, InvokerFactory.LOOKUP)
        assertNull(instantiate.invoke(null, noArg))

        Lookup subjects = MethodHandles.privateLookupIn(
                DirectInvokerSubjects, InvokerFactory.LOOKUP)
        assertNull(instantiate.invoke(null, subjects))
    }

    @Test
    void testInstantiateLookupRethrowsConstructorError() {
        Method instantiate = InvokerFactory.getDeclaredMethod('instantiate', Lookup)
        instantiate.accessible = true
        Lookup lookup = MethodHandles.privateLookupIn(
                DirectInvokerSubjects.ThrowsErrorOnConstruct, InvokerFactory.LOOKUP)
        InvocationTargetException ex = assertThrows(InvocationTargetException) {
            instantiate.invoke(null, lookup)
        }
        assertInstanceOf(AssertionError, ex.cause)
        assertEquals('ctor-error', ex.cause.message)
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testDefineInvokeStarOnLookupReturnsNullWhenHiddenClassesDisabled() {
        Method define = InvokerFactory.getDeclaredMethod('defineInvokeStarOnLookup', Method, Lookup)
        define.accessible = true
        String previous = System.getProperty(HiddenClassDefiner.PROPERTY_DISABLE)
        try {
            System.setProperty(HiddenClassDefiner.PROPERTY_DISABLE, 'true')
            assertNull(define.invoke(null, DirectInvokerSubjects.getMethod('ping'), InvokerFactory.LOOKUP))
        } finally {
            restoreProperty(HiddenClassDefiner.PROPERTY_DISABLE, previous)
        }
    }

    @Test
    void testDefineInvokeStarOnDeclaringClassOfJavaLangDoesNotThrow() {
        Method define = InvokerFactory.getDeclaredMethod('defineInvokeStarOnDeclaringClass', Method, Class)
        define.accessible = true
        Object result = define.invoke(null, String.getMethod('length'), String)
        if (result != null) {
            assertEquals(Integer.valueOf(1), ((DirectInvoker) result).invoke('x', null))
        }
    }

    @Test
    void testTryStepPredicatesOnIsolatedAndBootstrapHosts() {
        Method tryStep1 = InvokerFactory.getDeclaredMethod(
                'tryStep1', CachedMethod, Method, Class, Class[], Class)
        Method tryStep2 = InvokerFactory.getDeclaredMethod('tryStep2', Method, Class)
        Method tryStep3 = InvokerFactory.getDeclaredMethod(
                'tryStep3', CachedMethod, Class, Class[], Class)
        Method tryStep4 = InvokerFactory.getDeclaredMethod('tryStep4', CachedMethod, Method, Class)
        Method canResolve = InvokerFactory.getDeclaredMethod(
                'canResolveInvokeTypes', Class, Class, Class[], Class)
        [tryStep1, tryStep2, tryStep3, tryStep4, canResolve]*.accessible = true

        Method ping = DirectInvokerSubjects.getMethod('ping')
        CachedMethod pingCm = cm(ping)
        DirectInvoker step1 = (DirectInvoker) tryStep1.invoke(
                null, pingCm, ping, DirectInvokerSubjects, new Class[0], String)
        assertNotNull(step1)
        assertEquals('pong', step1.invoke(new DirectInvokerSubjects(), new Object[0]))

        Method secret = DirectInvokerSubjects.getDeclaredMethod('secret')
        assertNull(tryStep1.invoke(
                null, cm(secret), secret, DirectInvokerSubjects, new Class[0], String))

        DirectInvoker step2 = (DirectInvoker) tryStep2.invoke(null, secret, DirectInvokerSubjects)
        assertNotNull(step2)
        assertEquals('secret', step2.invoke(new DirectInvokerSubjects(), null))
        assertNull(tryStep2.invoke(null, String.getMethod('length'), String))

        DirectInvoker step3 = (DirectInvoker) tryStep3.invoke(
                null, pingCm, DirectInvokerSubjects, new Class[0], String)
        assertNotNull(step3)
        assertEquals('pong', step3.invoke(new DirectInvokerSubjects(), new Object[0]))
        assertSame(InvokerFactory, step3.class.nestHost)

        assertNull(tryStep4.invoke(null, cm(String.getMethod('length')), String.getMethod('length'), String))
        // loader can see DirectInvoker but the member is not publicly invocable
        assertNull(tryStep4.invoke(null, cm(secret), secret, DirectInvokerSubjects))

        Class<?> iso = defineIsolatedPublic('iso.StepPredHost', 'ping', 'iso')
        Method isoPing = iso.getMethod('ping')
        CachedMethod isoCm = cm(isoPing)
        assertNull(tryStep1.invoke(null, isoCm, isoPing, iso, new Class[0], String))
        assertNull(tryStep2.invoke(null, isoPing, iso))
        assertNull(tryStep3.invoke(null, isoCm, iso, new Class[0], String))
        assertNull(tryStep4.invoke(null, isoCm, isoPing, iso))

        assertTrue((Boolean) canResolve.invoke(null, InvokerFactory, String, new Class[0], String))
        assertTrue((Boolean) canResolve.invoke(null, InvokerFactory, String, [int] as Class[], int))
        assertFalse((Boolean) canResolve.invoke(null, InvokerFactory, iso, new Class[0], String))
        assertFalse((Boolean) canResolve.invoke(null, InvokerFactory, String, [iso] as Class[], String))
        assertFalse((Boolean) canResolve.invoke(null, InvokerFactory, String, new Class[0], iso))
    }

    @Test
    void testUtilityClassConstructorsArePresent() {
        [InvokerFactory, InvokerBytecode].each { Class<?> type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            assertNotNull(ctor.newInstance())
        }
    }

    private static void restoreProperty(String name, String previous) {
        if (previous == null) {
            System.clearProperty(name)
        } else {
            System.setProperty(name, previous)
        }
    }

    /**
     * {@link Class#getMethod(String, Class[])} via Java reflection so Groovy
     * indy does not dispatch against a hidden declaring class.
     */
    private static Method javaGetMethod(Class<?> type, String name) {
        (Method) Class.getMethod('getMethod', String, Class[]).invoke(type, name, new Class[0])
    }

    private static Class<?> defineIsolatedPublic(String binaryName, String methodName, String returned) {
        String internal = binaryName.replace('.', '/')
        byte[] bytes = emitStringPingClass(internal, methodName, returned, true)
        ClassLoader iso = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name == binaryName) {
                    return defineClass(name, bytes, 0, bytes.length)
                }
                throw new ClassNotFoundException(name)
            }
        }
        iso.loadClass(binaryName)
    }

    private static byte[] emitStringPingClass(
            String internalName, String methodName, String returned, boolean pub) {
        int acc = Opcodes.ACC_SUPER | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC
        if (pub) {
            acc |= Opcodes.ACC_PUBLIC
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
        cw.visit(Opcodes.V17, acc, internalName, null, 'java/lang/Object', null)
        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, '<init>', '()V', null, null)
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0)
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false)
        init.visitInsn(Opcodes.RETURN)
        init.visitMaxs(0, 0)
        init.visitEnd()
        MethodVisitor ping = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, '()Ljava/lang/String;', null, null)
        ping.visitCode()
        ping.visitLdcInsn(returned)
        ping.visitInsn(Opcodes.ARETURN)
        ping.visitMaxs(0, 0)
        ping.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static byte[] emitModuleInfo(String moduleName, String exportedInternal) {
        ClassWriter cw = new ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_MODULE, 'module-info', null, null, null)
        ModuleVisitor mv = cw.visitModule(moduleName, 0, null)
        mv.visitRequire('java.base', Opcodes.ACC_MANDATED, null)
        mv.visitExport(exportedInternal, 0)
        mv.visitEnd()
        cw.visitEnd()
        cw.toByteArray()
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return
        }
        Files.walk(root).sorted { a, b -> b <=> a }.forEach { Files.deleteIfExists(it) }
    }
}
