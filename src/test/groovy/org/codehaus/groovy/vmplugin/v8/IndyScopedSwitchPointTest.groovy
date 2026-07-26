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
import static org.junit.jupiter.api.Assertions.assertNotSame
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
    void invalidateSwitchPoints_bulkInvalidatesAndRotatesLegacyField() {
        SwitchPoint classSp = ClassInfo.getClassInfo(LegacyHost).indySwitchPoint
        SwitchPoint legacyBefore = IndyInterface.switchPoint
        assertFalse(classSp.hasBeenInvalidated())

        IndyInterface.invalidateSwitchPoints()

        assertTrue(classSp.hasBeenInvalidated())
        assertNotSame(legacyBefore, IndyInterface.switchPoint)
        assertTrue(legacyBefore.hasBeenInvalidated())
        assertFalse(IndyInterface.switchPoint.hasBeenInvalidated())
    }

    @Test
    void perClassMetaClassChange_doesNotRotateLegacySwitchPoint() {
        // Legacy IndyInterface.switchPoint is only rotated on category /
        // invalidateCallSites bulk paths — not on type-scoped MetaClass changes
        // (GROOVY-12191 review: external guards on the field miss per-class events).
        SwitchPoint legacyBefore = IndyInterface.switchPoint
        SwitchPoint classSp = ClassInfo.getClassInfo(LegacyMissHost).indySwitchPoint
        assertFalse(legacyBefore.hasBeenInvalidated())
        assertFalse(classSp.hasBeenInvalidated())

        def mc = new ExpandoMetaClass(LegacyMissHost, true, true)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(LegacyMissHost, mc)
        try {
            assertTrue(classSp.hasBeenInvalidated())
            assertTrue(legacyBefore.is(IndyInterface.switchPoint),
                    'legacy field must not rotate on per-class MetaClass change')
            assertFalse(legacyBefore.hasBeenInvalidated(),
                    'legacy field must stay valid across type-scoped invalidation')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(LegacyMissHost)
        }
    }

    @Test
    void invalidateSwitchPoints_concurrentRotation_doesNotOrphanLiveSwitchPoint() {
        // Restore of synchronized (IndyInterface.class) around legacy rotation:
        // concurrent bulk invalidations must not leave a reader-held SP live forever.
        int threads = 8
        def start = new java.util.concurrent.CyclicBarrier(threads)
        def done = new java.util.concurrent.CountDownLatch(threads)
        def errors = new java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
        def observed = java.util.Collections.synchronizedList(new ArrayList<SwitchPoint>())

        threads.times {
            Thread.start {
                try {
                    start.await()
                    SwitchPoint seen = IndyInterface.switchPoint
                    observed.add(seen)
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

        // Every SwitchPoint a thread observed before/during rotation must end
        // invalidated, or equal the final live field (if observed after last write).
        SwitchPoint live = IndyInterface.switchPoint
        assertFalse(live.hasBeenInvalidated())
        for (SwitchPoint sp : observed) {
            if (sp !== live) {
                assertTrue(sp.hasBeenInvalidated(),
                        'orphaned legacy SwitchPoint left live after concurrent rotation')
            }
        }
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
    static class LegacyMissHost {}
    static class PluginHost {}
    static class UnscopedHost {}
    static class RegistryHost {}
    /** Java-style host so {@link CachedMethod#find} is straightforward. */
    static class ColdHost {
        String ping() { 'ok' }
    }
}
