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

import org.apache.groovy.runtime.indy.IndyInvalidation
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.SwitchPoint

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Coverage for GROOVY-12191 wiring in {@link IndyInterface} and package-local
 * cold-tier validity (same package as production classes under test).
 */
final class IndyScopedSwitchPointTest {

    @Test
    void registryListener_withClass_invalidatesHierarchy() {
        // Ensure the MetaClass registry listener path (type != null) runs.
        SwitchPoint sp = ClassInfo.getClassInfo(RegistryHost).indySwitchPoint
        def mc = new ExpandoMetaClass(RegistryHost, true, true)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(RegistryHost, mc)
        assertTrue(sp.hasBeenInvalidated())
        GroovySystem.metaClassRegistry.removeMetaClass(RegistryHost)
    }

    @Test
    void indyLogging_enabled_inChildProcess_coversLogBranches() {
        // LOG_ENABLED is frozen at IndyInterface class init — cover the logging
        // branches in a fresh JVM with -Dgroovy.indy.logging=true.
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Dgroovy.indy.logging=true',
                '-cp', cp,
                'org.codehaus.groovy.vmplugin.v8.IndyLoggingProbe')
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = proc.inputStream.text
        def code = proc.waitFor()
        assertEquals(0, code, "probe failed: $out")
        assertTrue(out.contains('OK'), out)
    }

    @Test
    void applyMopSwitchPoints_installsClassGuard() {
        def target = MethodHandles.constant(int, 1)
        def fallback = MethodHandles.constant(int, 2)
        def receiver = new ApplyHost()
        def guarded = IndyInterface.applyMopSwitchPoints(target, fallback, receiver)
        assertEquals(1, guarded.invokeWithArguments())
        IndyInvalidation.invalidateClass(ApplyHost)
        assertEquals(2, guarded.invokeWithArguments())
    }

    @Test
    void applyMopSwitchPoints_matchesPublicGuardApi() {
        // Production path (applyMopSwitchPoints) and public IndyInvalidation API
        // must install the same class-domain SwitchPoint.
        def target = MethodHandles.constant(int, 7)
        def fallback = MethodHandles.constant(int, 8)
        def receiver = new ApplyHost()
        def viaInterface = IndyInterface.applyMopSwitchPoints(target, fallback, receiver)
        def viaInvalidation = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, receiver)
        assertEquals(7, viaInterface.invokeWithArguments())
        assertEquals(7, viaInvalidation.invokeWithArguments())
        IndyInvalidation.invalidateClass(ApplyHost)
        assertEquals(8, viaInterface.invokeWithArguments())
        assertEquals(8, viaInvalidation.invokeWithArguments())
    }

    @Test
    void invalidateSwitchPoints_bulkInvalidatesClassDomains() {
        // Legacy process-wide IndyInterface.switchPoint is removed; bulk path
        // only retires per-class domains (GROOVY-12191 / blackdrag review).
        SwitchPoint classSp = ClassInfo.getClassInfo(LegacyHost).indySwitchPoint
        assertFalse(classSp.hasBeenInvalidated())

        IndyInterface.invalidateSwitchPoints()

        assertTrue(classSp.hasBeenInvalidated())
        SwitchPoint fresh = ClassInfo.getClassInfo(LegacyHost).indySwitchPoint
        assertFalse(fresh.hasBeenInvalidated())
    }

    @Test
    void invalidateSwitchPoints_concurrentBulk_isSafe() {
        int threads = 8
        def start = new java.util.concurrent.CyclicBarrier(threads)
        def done = new java.util.concurrent.CountDownLatch(threads)
        def errors = new java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
        SwitchPoint before = ClassInfo.getClassInfo(LegacyHost).indySwitchPoint

        threads.times {
            Thread.start {
                try {
                    start.await()
                    IndyInterface.invalidateSwitchPoints()
                } catch (Throwable t) {
                    errors.add(t)
                } finally {
                    done.countDown()
                }
            }
        }
        assertTrue(done.await(30, java.util.concurrent.TimeUnit.SECONDS))
        assertTrue(errors.isEmpty(), "concurrent invalidate failed: $errors")
        assertTrue(before.hasBeenInvalidated())
    }

    @Test
    void vmPlugin_invalidateCallSites_routesToScopedCategoryPath() {
        SwitchPoint classSp = ClassInfo.getClassInfo(PluginHost).indySwitchPoint
        VMPluginFactory.plugin.invalidateCallSites()
        assertTrue(classSp.hasBeenInvalidated())
    }

    @Test
    void registryListener_unscoped_path_viaInvalidateUnscoped() {
        SwitchPoint sp = ClassInfo.getClassInfo(UnscopedHost).indySwitchPoint
        // Null-class registry events call IndyInvalidation.invalidateUnscoped().
        IndyInvalidation.invalidateUnscoped()
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void coldReflective_isValidFor_falseWhenSwitchPointInvalidated() {
        def sp = ClassInfo.getClassInfo(ColdHost).indySwitchPoint
        def cold = newColdWrapper(sp)
        def args = [new ColdHost()] as Object[]
        assertTrue(cold.isValidFor(args))
        SwitchPoint.invalidateAll(sp)
        assertFalse(cold.isValidFor(args))
    }

    @Test
    void coldReflective_isValidFor_falseOnArityMismatch() {
        def sp = ClassInfo.getClassInfo(ColdHost).indySwitchPoint
        def cold = newColdWrapper(sp)
        assertFalse(cold.isValidFor([new ColdHost(), 'extra'] as Object[]))
    }

    @Test
    void coldReflective_isValidFor_falseOnClassMismatch() {
        def sp = ClassInfo.getClassInfo(ColdHost).indySwitchPoint
        def cold = newColdWrapper(sp)
        assertFalse(cold.isValidFor(['not-a-ColdHost'] as Object[]))
    }

    @Test
    void coldReflective_picWrite_usesUncacheableSentinelWhenUncacheable() {
        // Mirrors invokeColdReflective / selectMethodHandle PIC policy:
        // uncacheable wrappers must not be stored under the receiver class key.
        def type = MethodType.methodType(Object, Object)
        def site = new CacheableCallSite(type, MethodHandles.lookup())
        def uncacheable = new MethodHandleWrapper(
                MethodHandles.constant(Object, 'cached'),
                MethodHandles.identity(Object).asType(type),
                null,
                false)
        assertFalse(uncacheable.isCanSetTarget())
        def key = ColdHost.name
        def sentinel = MethodHandleWrapper.uncacheablePicSentinel
        site.put(key, uncacheable.isCanSetTarget() ? uncacheable : sentinel)
        // Next PIC hit must observe the sentinel, not the uncacheable wrapper.
        def fromPic = site.getAndPut(key, { k -> uncacheable })
        assertSame(sentinel, fromPic)
    }

    /**
     * Minimal cold wrapper for validity tests (same package as production class).
     */
    private static ColdReflectiveMethodHandleWrapper newColdWrapper(SwitchPoint sp) {
        def javaMethod = ColdHost.getDeclaredMethod('ping')
        def metaMethod = CachedMethod.find(javaMethod)
        assert metaMethod != null
        def ctor = ColdReflectiveMethodHandleWrapper.getDeclaredConstructor(
                groovy.lang.MetaMethod, CacheableCallSite, Class, String, int,
                Boolean, Boolean, Boolean, groovy.lang.MetaClass, SwitchPoint, Class[])
        ctor.accessible = true
        def type = MethodType.methodType(Object, Object)
        def site = new CacheableCallSite(type, MethodHandles.lookup())
        def mc = GroovySystem.metaClassRegistry.getMetaClass(ColdHost)
        (ColdReflectiveMethodHandleWrapper) ctor.newInstance(
                metaMethod, site, ColdHost, 'ping', IndyInterface.CallType.METHOD.orderNumber,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, mc, sp, [ColdHost] as Class[])
    }

    static class ApplyHost {}
    static class LegacyHost {}
    static class PluginHost {}
    static class UnscopedHost {}
    static class RegistryHost {}
    /** Java-style host so {@link CachedMethod#find} is straightforward. */
    static class ColdHost {
        String ping() { 'ok' }
    }
}
