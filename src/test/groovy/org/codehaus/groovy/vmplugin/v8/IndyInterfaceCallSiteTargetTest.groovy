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
package org.codehaus.groovy.vmplugin.v8

import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue


final class IndyInterfaceCallSiteTargetTest {

    private String foo() {
        return 'foo-result'
    }

    private static String staticFoo() {
        return 'static-foo-result'
    }

    private static String staticEcho(String value) {
        return "static-echo-$value"
    }

    private static final class PerInstanceMetaClassStaticTarget {
        private static String ping() {
            return 'per-instance-static-result'
        }
    }

    private static final class ClassA {
        private static String bar() { return 'bar-from-A' }
    }

    private static final class ClassB {
        private static String bar() { return 'bar-from-B' }
    }

    private static final class InstanceStaticCallTarget {
        private static String valueOf(String value) { return "instance-static-$value" }
    }

    @Test
    void testDeprecatedFromCacheRelinksTargetImmediatelyForStaticClassReceiver() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [IndyInterfaceCallSiteTargetTest] as Object[]

        Object result = IndyInterface.fromCache(
            callSite,
            IndyInterfaceCallSiteTargetTest,
            'staticFoo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE,
            Boolean.FALSE,
            Boolean.FALSE,
            1,
            args
        )

        assertEquals(staticFoo(), result)
        assertNotSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandleKeepsDefaultTargetForSpreadCall() {
        MethodType type = MethodType.methodType(Object, Class, Object[])
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [IndyInterfaceCallSiteTargetTest, ['bar'] as Object[]] as Object[]

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite,
            IndyInterfaceCallSiteTargetTest,
            'staticEcho',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE,
            Boolean.FALSE,
            Boolean.TRUE,
            1,
            args
        )

        assertEquals(staticEcho('bar'), methodHandle.invokeWithArguments([args] as Object[]))
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandlePromotesCachedInstanceTargetAfterOptimizeThreshold() {
        MethodType type = MethodType.methodType(Object, IndyInterfaceCallSiteTargetTest)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new IndyInterfaceCallSiteTargetTest()
        Object[] args = [receiver] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(
            type, 'cached-instance-result', 'optimized-target-result',
            CachedMethod.find(IndyInterfaceCallSiteTargetTest.getDeclaredMethod('foo')), true
        )

        cacheWrapper(callSite, receiver, wrapper)
        primeLatestHitCount(callSite, receiver, wrapper, readIndyLong('INDY_OPTIMIZE_THRESHOLD'))

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(wrapper.targetMethodHandle, callSite.target)
        assertEquals(0L, wrapper.latestHitCount)
    }

    @Test
    void testFromCacheHandleLeavesDefaultTargetAfterFallbackCutoff() {
        assertFallbackCutoffLeavesDefaultTarget(true)
        assertFallbackCutoffLeavesDefaultTarget(false)
    }

    @Test
    void testFromCacheHandleSkipsTargetChangesWhenCachedWrapperCannotSetTarget() {
        MethodType type = MethodType.methodType(Object, IndyInterfaceCallSiteTargetTest)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new IndyInterfaceCallSiteTargetTest()
        Object[] args = [receiver] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(type, 'uncacheable-result', 'ignored-target', null, false)

        cacheWrapper(callSite, receiver, wrapper)

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandleReturnsNullHandleForSafeNavigationReceiver() {
        MethodType type = MethodType.methodType(Object, Object)
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [null] as Object[]

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertEquals(null, methodHandle.invokeWithArguments([args] as Object[]))
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandleUsesFallbackForPerInstanceMetaClassStaticReceiver() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [PerInstanceMetaClassStaticTarget] as Object[]
        MetaClassRegistryImpl registry = (MetaClassRegistryImpl) GroovySystem.metaClassRegistry
        ExpandoMetaClass emc = new ExpandoMetaClass(PerInstanceMetaClassStaticTarget, false, true)
        emc.initialize()

        registry.setMetaClass((Object) PerInstanceMetaClassStaticTarget, emc)
        try {
            2.times {
                MethodHandle methodHandle = invokeFromCacheHandle(
                    callSite, PerInstanceMetaClassStaticTarget, 'ping',
                    IndyInterface.CallType.METHOD.getOrderNumber(),
                    Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
                )

                assertEquals(PerInstanceMetaClassStaticTarget.ping(), methodHandle.invokeWithArguments([args] as Object[]))
            }
            assertSame(callSite.defaultTarget, callSite.target)
        } finally {
            registry.setMetaClass((Object) PerInstanceMetaClassStaticTarget, (MetaClass) null)
        }
    }

    @Test
    void testFromCacheHandleLeavesAlreadyLinkedCachedTargetUntouched() {
        MethodType type = MethodType.methodType(Object, IndyInterfaceCallSiteTargetTest)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new IndyInterfaceCallSiteTargetTest()
        Object[] args = [receiver] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(
            type, 'already-linked-result', 'linked-target-result',
            CachedMethod.find(IndyInterfaceCallSiteTargetTest.getDeclaredMethod('foo')), true
        )

        cacheWrapper(callSite, receiver, wrapper)
        callSite.target = wrapper.targetMethodHandle

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(wrapper.targetMethodHandle, callSite.target)
    }

    @Test
    void testFromCacheHandleDoesNotRelinkWhenCallSiteParamIsObjectEvenIfReceiverIsClass() {
        MethodType type = MethodType.methodType(Object, Object)
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [ClassA] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(
            type, 'class-a-result', 'class-a-target',
            CachedMethod.find(ClassA.getDeclaredMethod('bar')), true
        )

        cacheWrapper(callSite, ClassA, wrapper)

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandleDistinguishesDifferentClassReceivers() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)

        Object[] argsA = [ClassA] as Object[]
        MethodHandle handleA = invokeFromCacheHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsA
        )
        assertEquals(ClassA.bar(), handleA.invokeWithArguments([argsA] as Object[]))

        Object[] argsB = [ClassB] as Object[]
        MethodHandle handleB = invokeFromCacheHandle(
            callSite, ClassB, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsB
        )
        assertEquals(ClassB.bar(), handleB.invokeWithArguments([argsB] as Object[]))
    }

    @Test
    void testFromCacheHandleRelinksImmediatelyForClassTypedCallSiteWithStaticMethod() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)
        MethodHandleWrapper wrapper = newCachedWrapper(
            type, 'cached-static-result', 'static-target-result',
            CachedMethod.find(ClassA.getDeclaredMethod('bar')), true
        )

        cacheWrapper(callSite, ClassA, wrapper)

        Object[] args = [ClassA] as Object[]
        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(wrapper.targetMethodHandle, callSite.target)
    }

    @Test
    void testFromCacheHandleKeepsDefaultTargetForClassTypedCallSiteWhenMethodMetadataIsMissing() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)
        MethodHandleWrapper wrapper = newCachedWrapper(type, 'cached-class-result', 'ignored-target', null, true)

        cacheWrapper(callSite, ClassA, wrapper)

        Object[] args = [ClassA] as Object[]
        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testFromCacheHandleDoesNotRelinkStaticMethodInvokedThroughInstanceReceiver() {
        MethodType type = MethodType.methodType(Object, InstanceStaticCallTarget, String)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new InstanceStaticCallTarget()
        Object[] args = [receiver, 'abc'] as Object[]

        MethodHandle selectedHandle = invokeSelectMethodHandle(
            callSite, InstanceStaticCallTarget, 'valueOf',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertEquals(InstanceStaticCallTarget.valueOf('abc'), selectedHandle.invokeWithArguments([args] as Object[]))
        MethodHandleWrapper cachedWrapper = requireCachedWrapper(callSite, receiver)
        assertTrue(Modifier.isStatic(cachedWrapper.method.modifiers))
        assertSame(callSite.defaultTarget, callSite.target)

        MethodHandle cachedHandle = invokeFromCacheHandle(
            callSite, InstanceStaticCallTarget, 'valueOf',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertSame(cachedWrapper.cachedMethodHandle, cachedHandle)
        assertEquals(InstanceStaticCallTarget.valueOf('abc'), cachedHandle.invokeWithArguments([args] as Object[]))
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testSelectMethodHandleCachesDistinctEntriesForDifferentClassReceivers() {
        MethodType type = MethodType.methodType(Object, Class)
        CacheableCallSite callSite = newCallSite(type)

        Object[] argsA = [ClassA] as Object[]
        MethodHandle handleA = invokeSelectMethodHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsA
        )
        assertEquals(ClassA.bar(), handleA.invokeWithArguments([argsA] as Object[]))

        Object[] argsB = [ClassB] as Object[]
        MethodHandle handleB = invokeSelectMethodHandle(
            callSite, ClassB, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsB
        )
        assertEquals(ClassB.bar(), handleB.invokeWithArguments([argsB] as Object[]))

        MethodHandleWrapper cachedA = requireCachedWrapper(callSite, ClassA)
        MethodHandleWrapper cachedB = requireCachedWrapper(callSite, ClassB)
        assertNotSame(cachedA, cachedB)
        assertEquals(ClassA.bar(), cachedA.cachedMethodHandle.invokeWithArguments([argsA] as Object[]))
        assertEquals(ClassB.bar(), cachedB.cachedMethodHandle.invokeWithArguments([argsB] as Object[]))
        assertSame(callSite.defaultTarget, callSite.target)
    }

    @Test
    void testSelectMethodHandleStoresSentinelForUncacheableSpreadCall() {
        MethodType type = MethodType.methodType(Object, Class, Object[])
        CacheableCallSite callSite = newCallSite(type)
        Object[] args = [IndyInterfaceCallSiteTargetTest, ['bar'] as Object[]] as Object[]

        MethodHandle methodHandle = invokeSelectMethodHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'staticEcho',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, 1, args
        )

        assertEquals(staticEcho('bar'), methodHandle.invokeWithArguments([args] as Object[]))
        assertSame(MethodHandleWrapper.getNullMethodHandleWrapper(), requireCachedWrapper(callSite, IndyInterfaceCallSiteTargetTest))
        assertSame(callSite.defaultTarget, callSite.target)
    }

    private static CacheableCallSite newCallSite(MethodType type) {
        CacheableCallSite callSite = new CacheableCallSite(type, MethodHandles.lookup())
        MethodHandle dummyTarget = targetHandle(type, null)
        callSite.target = dummyTarget
        callSite.defaultTarget = dummyTarget
        callSite.fallbackTarget = dummyTarget
        return callSite
    }

    private static MethodHandleWrapper newCachedWrapper(MethodType type, Object cachedValue, Object targetValue, MetaMethod method, boolean canSetTarget) {
        new MethodHandleWrapper(cachedHandle(cachedValue), targetHandle(type, targetValue), method, canSetTarget)
    }

    private static MethodHandle cachedHandle(Object value) {
        MethodHandles.dropArguments(MethodHandles.constant(Object, value), 0, Object[])
    }

    private static MethodHandle targetHandle(MethodType type, Object value) {
        MethodHandles.dropArguments(MethodHandles.constant(Object, value), 0, type.parameterArray())
    }

    private static void cacheWrapper(CacheableCallSite callSite, Object receiver, MethodHandleWrapper wrapper) {
        callSite.put(receiverClassName(receiver), wrapper)
    }

    private static void primeLatestHitCount(CacheableCallSite callSite, Object receiver, MethodHandleWrapper wrapper, long value) {
        assertSame(wrapper, callSite.getAndPut(receiverClassName(receiver), { wrapper }))
        latestHitCountField().get(wrapper).set(value)
    }

    private static void assertFallbackCutoffLeavesDefaultTarget(boolean startAwayFromDefaultTarget) {
        MethodType type = MethodType.methodType(Object, IndyInterfaceCallSiteTargetTest)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new IndyInterfaceCallSiteTargetTest()
        Object[] args = [receiver] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(
            type, 'cached-instance-result', 'optimized-target-result',
            CachedMethod.find(IndyInterfaceCallSiteTargetTest.getDeclaredMethod('foo')), true
        )

        cacheWrapper(callSite, receiver, wrapper)
        primeLatestHitCount(callSite, receiver, wrapper, readIndyLong('INDY_OPTIMIZE_THRESHOLD'))
        callSite.fallbackRound.set(readIndyLong('INDY_FALLBACK_CUTOFF') + 1L)
        if (startAwayFromDefaultTarget) {
            callSite.target = targetHandle(type, 'non-default-target')
        }

        MethodHandle methodHandle = invokeFromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertSame(callSite.defaultTarget, callSite.target)
        assertEquals(0L, wrapper.latestHitCount)
    }

    private static MethodHandleWrapper requireCachedWrapper(CacheableCallSite callSite, Object receiver) {
        AtomicInteger providerCalls = new AtomicInteger()
        MethodHandleWrapper wrapper = callSite.getAndPut(receiverClassName(receiver), { key ->
            providerCalls.incrementAndGet()
            MethodHandleWrapper.getNullMethodHandleWrapper()
        })
        assertEquals(0, providerCalls.get())
        wrapper
    }

    private static String receiverClassName(Object receiver) {
        if (receiver == null) return 'org.codehaus.groovy.runtime.NullObject'
        if (receiver instanceof Class) return 'java.lang.Class:' + ((Class<?>) receiver).getName()
        return receiver.getClass().name
    }

    private static long readIndyLong(String fieldName) {
        Field field = IndyInterface.getDeclaredField(fieldName)
        field.accessible = true
        field.getLong(null)
    }

    private static Field latestHitCountField() {
        Field field = MethodHandleWrapper.getDeclaredField('latestHitCount')
        field.accessible = true
        field
    }

    private static MethodHandle invokeFromCacheHandle(CacheableCallSite callSite, Class<?> sender, String methodName,
            int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
        Method method = IndyInterface.getDeclaredMethod('fromCacheHandle',
            CacheableCallSite, Class, String, Integer.TYPE, Boolean, Boolean, Boolean, Object, Object[]
        )
        method.accessible = true
        return (MethodHandle) method.invoke(null,
            callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments
        )
    }

    private static MethodHandle invokeSelectMethodHandle(CacheableCallSite callSite, Class<?> sender, String methodName,
            int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
        Method method = IndyInterface.getDeclaredMethod('selectMethodHandle',
            CacheableCallSite, Class, String, Integer.TYPE, Boolean, Boolean, Boolean, Object, Object[]
        )
        method.accessible = true
        return (MethodHandle) method.invoke(null,
            callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments
        )
    }
}
