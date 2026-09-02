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

import groovy.lang.GroovyClassLoader
import org.apache.groovy.util.HiddenClassDefiner
import org.codehaus.groovy.reflection.CachedMethod
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import org.objectweb.asm.Opcodes

import java.lang.reflect.Method

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
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

    private static void restoreProperty(String name, String previous) {
        if (previous == null) {
            System.clearProperty(name)
        } else {
            System.setProperty(name, previous)
        }
    }
}
