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

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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
        primeLatestHitCount(callSite, receiver, wrapper, IndyInterface.INDY_OPTIMIZE_THRESHOLD)

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
            callSite, IndyInterfaceCallSiteTargetTest, 'foo',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 1, args
        )

        assertSame(wrapper.cachedMethodHandle, methodHandle)
        assertNotSame(callSite.defaultTarget, callSite.target)
        assertEquals(0L, wrapper.latestHitCount)
    }

    @Test
    void testFromCacheHandleLeavesDefaultTargetAfterFallbackCutoff() {
        assertFallbackCutoffLeavesDefaultTarget(true)
        assertFallbackCutoffLeavesDefaultTarget(false)
    }

    @Test
    void testResetFallbackCountAdvancesRound() {
        CacheableCallSite callSite = newCallSite(MethodType.methodType(Object, Object))
        assertEquals(0L, callSite.fallbackRound.get())
        callSite.resetFallbackCount()
        assertEquals(1L, callSite.fallbackRound.get())
        callSite.resetFallbackCount()
        assertEquals(2L, callSite.fallbackRound.get())
    }

    @Test
    void testFromCacheHandleSkipsTargetChangesWhenCachedWrapperCannotSetTarget() {
        MethodType type = MethodType.methodType(Object, IndyInterfaceCallSiteTargetTest)
        CacheableCallSite callSite = newCallSite(type)
        def receiver = new IndyInterfaceCallSiteTargetTest()
        Object[] args = [receiver] as Object[]
        MethodHandleWrapper wrapper = newCachedWrapper(type, 'uncacheable-result', 'ignored-target', null, false)

        cacheWrapper(callSite, receiver, wrapper)

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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
                MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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
        MethodHandle handleA = IndyInterface.fromCacheHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsA
        )
        assertEquals(ClassA.bar(), handleA.invokeWithArguments([argsA] as Object[]))

        Object[] argsB = [ClassB] as Object[]
        MethodHandle handleB = IndyInterface.fromCacheHandle(
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
        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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
        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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

        MethodHandle selectedHandle = IndyInterface.selectMethodHandle(
            callSite, InstanceStaticCallTarget, 'valueOf',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, args
        )

        assertEquals(InstanceStaticCallTarget.valueOf('abc'), selectedHandle.invokeWithArguments([args] as Object[]))
        MethodHandleWrapper cachedWrapper = requireCachedWrapper(callSite, receiver)
        assertTrue(Modifier.isStatic(cachedWrapper.method.modifiers))
        assertSame(callSite.defaultTarget, callSite.target)

        MethodHandle cachedHandle = IndyInterface.fromCacheHandle(
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
        MethodHandle handleA = IndyInterface.selectMethodHandle(
            callSite, ClassA, 'bar',
            IndyInterface.CallType.METHOD.getOrderNumber(),
            Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1, argsA
        )
        assertEquals(ClassA.bar(), handleA.invokeWithArguments([argsA] as Object[]))

        Object[] argsB = [ClassB] as Object[]
        MethodHandle handleB = IndyInterface.selectMethodHandle(
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

        MethodHandle methodHandle = IndyInterface.selectMethodHandle(
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
        callSite.put(IndyInterface.receiverCacheKey(receiver), wrapper)
    }

    private static void primeLatestHitCount(CacheableCallSite callSite, Object receiver, MethodHandleWrapper wrapper, long value) {
        assertSame(wrapper, callSite.getAndPut(IndyInterface.receiverCacheKey(receiver), { wrapper }, IndyInterfaceCallSiteTargetTest))
        wrapper.@latestHitCount.set(value)
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
        primeLatestHitCount(callSite, receiver, wrapper, IndyInterface.INDY_OPTIMIZE_THRESHOLD)
        callSite.fallbackRound.set(IndyInterface.INDY_FALLBACK_CUTOFF + 1L)
        if (startAwayFromDefaultTarget) {
            callSite.target = targetHandle(type, 'non-default-target')
        }

        MethodHandle methodHandle = IndyInterface.fromCacheHandle(
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
        MethodHandleWrapper wrapper = callSite.getAndPut(IndyInterface.receiverCacheKey(receiver), { key ->
            providerCalls.incrementAndGet()
            MethodHandleWrapper.getNullMethodHandleWrapper()
        }, IndyInterfaceCallSiteTargetTest)
        assertEquals(0, providerCalls.get())
        wrapper
    }

    private static String receiverClassName(Object receiver) {
        if (receiver == null) return 'org.codehaus.groovy.runtime.NullObject'
        if (receiver instanceof Class) return 'java.lang.Class:' + ((Class<?>) receiver).getName()
        return receiver.getClass().name
    }


}
