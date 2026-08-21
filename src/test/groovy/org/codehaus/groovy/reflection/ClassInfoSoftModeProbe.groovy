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
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.invoke.SwitchPoint
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.zip.Deflater
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

    /**
     * The global store's slot content for {@code type}: a SoftReference for a
     * reclaimable value, or the ClassInfo itself when pinned (dirty state).
     */
    private static Object slotContent(Class<?> type) {
        def gcvField = ClassInfo.getDeclaredField('globalClassValue')
        gcvField.accessible = true
        def gcv = gcvField.get(null)
        assert gcv instanceof GroovyClassValueSoft : "probe requires -Dgroovy.use.classvalue=soft, found ${gcv.getClass().simpleName}"
        def storeField = GroovyClassValueSoft.getDeclaredField('store')
        storeField.accessible = true
        ClassValue store = storeField.get(gcv)
        store.get(type).get()
    }

    /** Clears the global store's memoized SoftReference for {@code type}. A pinned slot has none. */
    private static void clearSoft(Class<?> type) {
        def content = slotContent(type)
        assert content instanceof SoftReference : "a pinned slot cannot be cleared by GC (${type.name})"
        ((SoftReference) content).clear()
    }

    /**
     * Asserts the strongest statement available about non-reclaimable state:
     * the slot holds the ClassInfo itself, so no GC schedule can clear it —
     * while the class lives, exactly like the default strong ClassValue.
     */
    private static void assertPinned(Class<?> type) {
        assert slotContent(type) instanceof ClassInfo : "ClassInfo for ${type.name} should be pinned in its association"
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

    /**
     * Applies real memory pressure: the collector clears soft references
     * before throwing {@link OutOfMemoryError}, so generic soft caches
     * (for example {@code ClassInfo}'s lazy CachedClass/loader references)
     * release their referents — the "on memory pressure" premise of the
     * reverse scenario. Mild GC alone retains them in every mode.
     */
    private static void applySevereMemoryPressure() {
        def hold = []
        try {
            while (true) { hold << new byte[1 << 20] }
        } catch (OutOfMemoryError expected) {
            hold = null
        }
    }

    static void main(String[] args) {
        resurrectionPreservesIdentityAndVersion()
        dgmTargetClassInfoIsPinned()
        strongMetaClassPinsAndUnpinsWithItsState()
        perInstanceMetaClassPinsClassInfo()
        pristineClassInfoIsCollectedAndRecreatedWorking()
        classicCallSiteStaysSoundAcrossClearAndRelinksOnChange()
        indyDomainContinuityAcrossRecreation()
        dirtyScriptClassDiesWithItsLoader()
        println 'OK'
    }

    /**
     * The split-brain check: a ClassInfo captured by any holder must be
     * returned as-is after the slot's soft reference clears, with its
     * version untouched, so captured version guards stay sound. Uses a class
     * with no registry-written DGM arrays, whose slot is therefore soft.
     */
    private static void resurrectionPreservesIdentityAndVersion() {
        ClassInfo before = ClassInfo.getClassInfo(Deflater)   // strong local ref: "captured by a call site"
        int version = before.version
        clearSoft(Deflater)
        ClassInfo after = ClassInfo.getClassInfo(Deflater)
        assert after.is(before) : 'live ClassInfo must be resurrected, not replaced'
        assert after.version == version : 'resurrection must not disturb the version guard stamp'
    }

    /**
     * The enforced E3 invariant: a ClassInfo holding registry-written DGM/extension
     * method arrays is pinned inside its own association — the slot holds the
     * instance itself, so no GC schedule can clear it while the class lives and
     * a recreated instance never needs to rebuild those arrays.
     */
    private static void dgmTargetClassInfoIsPinned() {
        def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(String))
        assertPinned(String)
        System.gc()
        assert weak.get() != null : 'DGM-target ClassInfo must be pinned (non-reclaimable)'
        assert ClassInfo.getClassInfo(String).is(weak.get())
        assert 'abc'.reverse() == 'cba' : 'String DGM dispatch intact'
    }

    /**
     * User metaclass customizations are non-reconstructible: installing one
     * pins the ClassInfo in its association; removing it unpins, restoring
     * reclaimability — the pin follows the state, not the class.
     */
    private static void strongMetaClassPinsAndUnpinsWithItsState() {
        CRC32.metaClass.twiddle = { -> 42 }
        try {
            def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(CRC32))
            assertPinned(CRC32)
            System.gc()
            assert weak.get() != null : 'ClassInfo with installed MetaClass must be pinned'
            assert new CRC32().twiddle() == 42 : 'EMC customization must survive GC'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(CRC32)
        }
        assert slotContent(CRC32) instanceof SoftReference : 'removing the MetaClass must unpin the ClassInfo'
    }

    /** Per-instance metaclasses are equally non-reconstructible state. */
    private static void perInstanceMetaClassPinsClassInfo() {
        def receiver = new Adler32()
        receiver.metaClass.spin = { -> 7 }
        try {
            def weak = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(Adler32))
            assertPinned(Adler32)
            System.gc()
            assert weak.get() != null : 'ClassInfo with per-instance MetaClass must be pinned'
            assert receiver.spin() == 7 : 'per-instance customization must survive GC'
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
     * would answer the original result forever. The receiver is a POJO with no
     * registry-written DGM arrays, so its slot is soft (clearable); a pinned
     * receiver like {@code String} can never be cleared in the first place.
     */
    private static void classicCallSiteStaysSoundAcrossClearAndRelinksOnChange() {
        // groovy-callsite is runtime-only for core (as for legacy-compiled jars
        // in the wild), so drive it reflectively
        def csaClass = Class.forName('org.codehaus.groovy.runtime.callsite.CallSiteArray')
        def csa = csaClass.getConstructor(Class, String[]).newInstance(ClassInfoSoftModeProbe, ['toString'] as String[])
        def noparam = csaClass.NOPARAM
        def joiner = new StringJoiner('-')
        assert csa.array[0].call(joiner, noparam) == ''
        // the linked site (csa.array[0] after the first call) now captures ClassInfo(StringJoiner)+version
        clearSoft(StringJoiner)
        System.gc()
        assert csa.array[0].call(joiner, noparam) == '' : 'linked site stays correct across the clear'
        StringJoiner.metaClass.toString = { -> 'emc' }
        try {
            assert csa.array[0].call(joiner, noparam) == 'emc' : 'version guard on the resurrected instance must observe the change'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(StringJoiner)
        }
        assert csa.array[0].call(joiner, noparam) == ''
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

    /** Installs an EMC method statically, replicating {@code cls.metaClass.extra = { -> 42 }}. */
    @CompileStatic
    private static void installExtra(Class<?> cls) {
        def emc = new ExpandoMetaClass(cls, true, true)
        emc.initialize()
        emc.setProperty('extra', { -> 42 })
        GroovySystem.metaClassRegistry.setMetaClass(cls, emc)
    }

    /**
     * The "reverse" scenario (Jochen, PR #2820 review): the Groovy runtime
     * stays alive while script loaders come and go, and a script installs an
     * EMC on a class it created. The pin lives inside the class's own
     * association, so on memory pressure dropping the loader must release the
     * class, its ClassInfo, the EMC and the loader itself — exactly as the
     * default strong ClassValue does. A global strong root would fail this:
     * it would extend every EMC-dirty script class to the runtime's lifetime.
     * <p>
     * Compiled statically on purpose: a dynamic call in this long-lived probe
     * class would link its invokedynamic call-site guards against the script
     * classes, retaining them from the call site — a receiver-side inline-cache
     * effect present in every mode, not the association lifetime this scenario
     * isolates. Locals are nulled for the same reason: the last iteration's
     * frame slots stay reachable through the collection loop below.
     */
    @CompileStatic
    private static void dirtyScriptClassDiesWithItsLoader() {
        List<WeakReference<ClassLoader>> loaderRefs = []
        List<WeakReference<Class>> classRefs = []
        for (int i = 0; i < 3; i++) {
            def gcl = new GroovyClassLoader()
            Class cls = gcl.parseClass("class ReverseScripted${i} { def hi() { 'hi' } }")
            installExtra(cls)
            def obj = cls.getDeclaredConstructor().newInstance()
            assert InvokerHelper.invokeMethod(obj, 'extra', null) == 42 : 'EMC on the script-created class must dispatch'
            assertPinned(cls)
            loaderRefs << new WeakReference<ClassLoader>(gcl)
            classRefs << new WeakReference<Class>(cls)
            obj = null; cls = null; gcl = null
        }
        applySevereMemoryPressure()
        loaderRefs.each { WeakReference<ClassLoader> ref ->
            assert awaitCollected(ref) : 'dropped script loader with an EMC-dirty class must be collectable (reverse scenario)'
        }
        classRefs.each { WeakReference<Class> ref ->
            assert ref.get() == null : 'the script class must go with its loader'
        }
    }
}
