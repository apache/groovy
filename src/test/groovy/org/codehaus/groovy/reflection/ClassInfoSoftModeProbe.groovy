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
package org.codehaus.groovy.reflection

import groovy.lang.GroovySystem

import java.lang.invoke.SwitchPoint
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.zip.Inflater

/**
 * GROOVY-12281 child-process probe for {@code -Dgroovy.use.classvalue=soft}:
 * exercises the global {@link ClassInfo#globalClassValue} in soft mode.
 * Invoked from {@link ClassInfoSoftModeTest}; prints {@code OK} on success.
 * <p>
 * Forced clears simulate GC clearing of the memoized SoftReference. A manual
 * {@code clear()} is a conservative superset of GC behavior (a collector never
 * clears a reference to a strongly reachable object), so passing here implies
 * correctness under any GC schedule.
 */
final class ClassInfoSoftModeProbe {

    static class IndyHost {
        def ping() { 'pong' }
    }

    /** Clears the global store's memoized SoftReference for {@code type}. */
    private static void clearSoft(Class<?> type) {
        def gcvField = ClassInfo.getDeclaredField('globalClassValue')
        gcvField.accessible = true
        def gcv = gcvField.get(null)
        assert gcv instanceof GroovyClassValueSoft : "probe requires -Dgroovy.use.classvalue=soft, found ${gcv.getClass().simpleName}"
        def storeField = GroovyClassValueSoft.getDeclaredField('store')
        storeField.accessible = true
        ClassValue store = storeField.get(gcv)
        ((SoftReference) store.get(type)).clear()
    }

    private static boolean awaitCollected(WeakReference<?> ref) {
        for (int i = 0; i < 100 && ref.get() != null; i++) {
            System.gc()
            byte[][] pressure = new byte[64][]
            for (int j = 0; j < pressure.length; j++) {
                pressure[j] = new byte[1 << 16]
            }
            Thread.sleep(10)
        }
        return ref.get() == null
    }

    static void main(String[] args) {
        resurrectionPreservesIdentityAndVersion()
        dgmTargetClassInfoIsRooted()
        strongMetaClassSurvivesClear()
        perInstanceMetaClassSurvivesClear()
        pristineClassInfoIsCollectedAndRecreatedWorking()
        classicCallSiteStaysSoundAcrossClearAndRelinksOnChange()
        indyDomainContinuityAcrossRecreation()
        println 'OK'
    }

    /**
     * The split-brain check: a ClassInfo captured by any holder must be
     * returned as-is after the ClassValue's soft reference clears, with its
     * version untouched, so captured version guards stay sound.
     */
    private static void resurrectionPreservesIdentityAndVersion() {
        ClassInfo before = ClassInfo.getClassInfo(String)   // strong local ref: "captured by a call site"
        int version = before.version
        clearSoft(String)
        ClassInfo after = ClassInfo.getClassInfo(String)
        assert after.is(before) : 'live ClassInfo must be resurrected, not replaced'
        assert after.version == version : 'resurrection must not disturb the version guard stamp'
    }

    /**
     * The enforced E3 invariant: a ClassInfo holding registry-written DGM/extension
     * method arrays is strongly rooted, so it can never be soft-collected and a
     * recreated instance never needs to rebuild those arrays.
     */
    private static void dgmTargetClassInfoIsRooted() {
        def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(String))
        clearSoft(String)
        System.gc()
        assert weak.get() != null : 'DGM-target ClassInfo must be rooted (non-reclaimable)'
        assert ClassInfo.getClassInfo(String).is(weak.get())
        assert 'abc'.reverse() == 'cba' : 'String DGM dispatch intact'
    }

    /** User metaclass customizations must survive value clearing (dirty root). */
    private static void strongMetaClassSurvivesClear() {
        CRC32.metaClass.twiddle = { -> 42 }
        try {
            def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(CRC32))
            clearSoft(CRC32)
            System.gc()
            assert weak.get() != null : 'ClassInfo with installed MetaClass must be rooted'
            assert new CRC32().twiddle() == 42 : 'EMC customization must survive the clear'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(CRC32)
        }
    }

    /** Per-instance metaclasses are equally non-reconstructible state. */
    private static void perInstanceMetaClassSurvivesClear() {
        def receiver = new Adler32()
        receiver.metaClass.spin = { -> 7 }
        try {
            def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(Adler32))
            clearSoft(Adler32)
            System.gc()
            assert weak.get() != null : 'ClassInfo with per-instance MetaClass must be rooted'
            assert receiver.spin() == 7 : 'per-instance customization must survive the clear'
        } finally {
            receiver.metaClass = null
        }
    }

    /**
     * The unpinning payoff: a pristine ClassInfo (no user MetaClass state, no
     * DGM arrays) really is collected once cleared, and dispatch afterwards
     * works against a fresh instance — hierarchy DGM methods included.
     */
    private static void pristineClassInfoIsCollectedAndRecreatedWorking() {
        def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(Inflater))
        clearSoft(Inflater)
        assert awaitCollected(weak) : 'pristine ClassInfo should be collectable once cleared'
        def inflater = new Inflater()
        try {
            assert inflater.with { 'fresh' } == 'fresh' : 'Object-hierarchy DGM dispatch on the fresh ClassInfo'
        } finally {
            inflater.end()
        }
    }

    /**
     * Legacy classic call sites (groovy-callsite on the classpath, e.g. from
     * jars compiled by older Groovy) capture the ClassInfo instance and its
     * version at link time. Resurrection keeps that capture sound across a
     * clear, and a later MetaClass change must still be observed via the
     * version guard on the same instance. Without resurrection the stale site
     * would answer 'cba' forever.
     */
    private static void classicCallSiteStaysSoundAcrossClearAndRelinksOnChange() {
        // groovy-callsite is runtime-only for core (as for legacy-compiled jars
        // in the wild), so drive it reflectively
        def csaClass = Class.forName('org.codehaus.groovy.runtime.callsite.CallSiteArray')
        def csa = csaClass.getConstructor(Class, String[]).newInstance(ClassInfoSoftModeProbe, ['reverse'] as String[])
        def noparam = csaClass.NOPARAM
        assert csa.array[0].call('abc', noparam) == 'cba'
        // the linked site (csa.array[0] after the first call) now captures ClassInfo(String)+version
        clearSoft(String)
        System.gc()
        assert csa.array[0].call('abc', noparam) == 'cba' : 'linked site stays correct across the clear'
        String.metaClass.reverse = { -> 'emc' }
        try {
            assert csa.array[0].call('abc', noparam) == 'emc' : 'version guard on the resurrected instance must observe the change'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(String)
        }
        assert csa.array[0].call('abc', noparam) == 'cba'
    }

    /**
     * The E4 re-homing check: indy guards capture only the SwitchPoint, so a
     * guard can outlive its ClassInfo. Domain identity is keyed by Class, so a
     * mutation applied through the successor ClassInfo must deterministically
     * retire a SwitchPoint handed out by the collected predecessor. Without
     * re-homing, the successor would invalidate a fresh domain and this
     * SwitchPoint would survive, leaving the stale guard installed until the
     * lazy reference-queue pump.
     */
    private static void indyDomainContinuityAcrossRecreation() {
        ClassInfo before = ClassInfo.getClassInfo(IndyHost)
        SwitchPoint sp = before.indySwitchPoint
        def weak = new WeakReference<ClassInfo>(before)
        before = null
        clearSoft(IndyHost)
        assert awaitCollected(weak) : 'unrooted ClassInfo with a linked domain should be collectable'
        ClassInfo successor = ClassInfo.getClassInfo(IndyHost)
        successor.incVersion()
        assert sp.hasBeenInvalidated() : "predecessor's SwitchPoint must be retired by a mutation through the successor"
    }
}
