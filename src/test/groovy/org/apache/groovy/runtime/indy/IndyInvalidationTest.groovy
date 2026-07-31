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
package org.apache.groovy.runtime.indy

import groovy.lang.ExpandoMetaClass
import groovy.lang.GroovySystem
import groovy.lang.MetaClass
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.runtime.NullObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.SwitchPoint
import java.lang.ref.WeakReference

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit tests for {@link IndyInvalidation} — MetaClass-owned SwitchPoint model
 * with exact-class stock policy and bulk invalidation for custom MetaClasses
 * (GROOVY-12191).
 */
final class IndyInvalidationTest {

    @BeforeEach
    void resetCounters() {
        IndyInvalidation.resetCountersForTesting()
    }

    @Test
    void switchPointClassFor_null_classReceiver_andInstance() {
        assertSame(NullObject, IndyInvalidation.switchPointClassFor(null))
        assertSame(String, IndyInvalidation.switchPointClassFor(String))
        assertSame(String, IndyInvalidation.switchPointClassFor('hi'))
        assertSame(Integer, IndyInvalidation.switchPointClassFor(42))
    }

    @Test
    void invalidateClass_onlyAffectsThatClass_notSiblings() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        assertFalse(spA.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())

        long before = IndyInvalidation.classInvalidationCount()
        IndyInvalidation.invalidateClass(ClassA)
        assertTrue(spA.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())
        assertEquals(before + 1, IndyInvalidation.classInvalidationCount())

        SwitchPoint spA2 = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        assertNotSame(spA, spA2)
        assertFalse(spA2.hasBeenInvalidated())
        assertSame(spB, ClassInfo.getClassInfo(ClassB).indySwitchPoint)
    }

    @Test
    void invalidateClass_doesNotFanOutToSubtypes() {
        // Stock policy: parent invalidation is exact-class only (PR #2736).
        SwitchPoint parentSp = ClassInfo.getClassInfo(Parent).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(Child).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint

        IndyInvalidation.invalidateClass(Parent)

        assertTrue(parentSp.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated(),
                'stock invalidateClass must not retire subtype SwitchPoints')
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_null_isNoOp() {
        long before = IndyInvalidation.classInvalidationCount()
        IndyInvalidation.invalidateClass(null)
        assertEquals(before, IndyInvalidation.classInvalidationCount())
    }

    @Test
    void invalidateClass_objectArray_doesNotRetireStringArray() {
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint

        IndyInvalidation.invalidateClass(Object[])

        assertTrue(objectArraySp.hasBeenInvalidated())
        assertFalse(stringArraySp.hasBeenInvalidated(),
                'array lattice fan-out is not required; miss path is live')
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_stringArray_doesNotRetireUnrelatedArrays() {
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        SwitchPoint integerArraySp = ClassInfo.getClassInfo(Integer[]).indySwitchPoint
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint

        IndyInvalidation.invalidateClass(String[])

        assertTrue(stringArraySp.hasBeenInvalidated())
        assertFalse(integerArraySp.hasBeenInvalidated())
        assertFalse(objectArraySp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_primitiveArray_retiresOnlyThatArrayType() {
        SwitchPoint intArraySp = ClassInfo.getClassInfo(int[]).indySwitchPoint
        SwitchPoint longArraySp = ClassInfo.getClassInfo(long[]).indySwitchPoint
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint

        IndyInvalidation.invalidateClass(int[])

        assertTrue(intArraySp.hasBeenInvalidated())
        assertFalse(longArraySp.hasBeenInvalidated())
        assertFalse(objectArraySp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_interface_doesNotFanOutToImplementors() {
        SwitchPoint ifaceSp = ClassInfo.getClassInfo(Marker).indySwitchPoint
        SwitchPoint implSp = ClassInfo.getClassInfo(MarkerImpl).indySwitchPoint
        SwitchPoint otherSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint

        IndyInvalidation.invalidateClass(Marker)

        assertTrue(ifaceSp.hasBeenInvalidated())
        assertFalse(implSp.hasBeenInvalidated())
        assertFalse(otherSp.hasBeenInvalidated())
    }

    @Test
    void guardWithMopSwitchPoints_stringArrayFallsBackOnOwnInvalidateOnly() {
        def target = MethodHandles.constant(int, 11)
        def fallback = MethodHandles.constant(int, 12)
        def receiver = new String[0]
        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, receiver)
        assertEquals(11, guarded.invokeWithArguments())

        // Parent array-type invalidation must not deopt String[] sites.
        IndyInvalidation.invalidateClass(Object[])
        assertEquals(11, guarded.invokeWithArguments(),
                'String[] site must stay linked when only Object[] domain retires')

        IndyInvalidation.invalidateClass(String[])
        assertEquals(12, guarded.invokeWithArguments(),
                'String[] site must fall back after its own domain retires')
    }

    @Test
    void invalidateCategory_bulkInvalidatesClassDomains() {
        SwitchPoint classSp = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        long catCount = IndyInvalidation.categoryInvalidationCount()

        IndyInvalidation.invalidateCategory()

        assertTrue(classSp.hasBeenInvalidated())
        assertEquals(catCount + 1, IndyInvalidation.categoryInvalidationCount())
        SwitchPoint fresh = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        assertFalse(fresh.hasBeenInvalidated())
    }

    @Test
    void invalidateUnscoped_retiresLoadedClassSwitchPoints() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        long bulkBefore = IndyInvalidation.bulkInvalidationCount()
        long classBefore = IndyInvalidation.classInvalidationCount()
        IndyInvalidation.invalidateUnscoped()
        assertTrue(spA.hasBeenInvalidated())
        assertTrue(spB.hasBeenInvalidated())
        assertEquals(bulkBefore + 1, IndyInvalidation.bulkInvalidationCount(),
                'unscoped is process-wide bulk (non-category)')
        assertEquals(classBefore, IndyInvalidation.classInvalidationCount(),
                'unscoped must not increment exact-class counter')
    }

    @Test
    void invalidateBulk_retiresAllLoadedClassDomains() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        long bulkBefore = IndyInvalidation.bulkInvalidationCount()
        long classBefore = IndyInvalidation.classInvalidationCount()
        long catBefore = IndyInvalidation.categoryInvalidationCount()

        IndyInvalidation.invalidateBulk()

        assertTrue(spA.hasBeenInvalidated())
        assertTrue(spB.hasBeenInvalidated())
        assertEquals(bulkBefore + 1, IndyInvalidation.bulkInvalidationCount())
        assertEquals(classBefore, IndyInvalidation.classInvalidationCount())
        assertEquals(catBefore, IndyInvalidation.categoryInvalidationCount())
    }

    @Test
    void invalidateCategory_doesNotIncrementBulkCounter() {
        long bulkBefore = IndyInvalidation.bulkInvalidationCount()
        IndyInvalidation.invalidateCategory()
        assertEquals(bulkBefore, IndyInvalidation.bulkInvalidationCount(),
                'category has its own counter; must not share bulk tally')
    }

    @Test
    void classInfoIncVersion_invalidatesClassDomain_notSiblingsOrSubtypes() {
        SwitchPoint spA = ClassInfo.getClassInfo(Parent).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(Child).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        int versionBefore = ClassInfo.getClassInfo(Parent).version

        ClassInfo.getClassInfo(Parent).incVersion()

        assertEquals(versionBefore + 1, ClassInfo.getClassInfo(Parent).version)
        assertTrue(spA.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())
    }

    @Test
    void guardWithMopSwitchPoints_fallsBackOnClassInvalidate() {
        def target = MethodHandles.constant(int, 1)
        def fallback = MethodHandles.constant(int, 2)
        def receiver = new ClassA()

        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, receiver)
        assertEquals(1, guarded.invokeWithArguments())

        IndyInvalidation.invalidateClass(ClassA)
        assertEquals(2, guarded.invokeWithArguments())
    }

    @Test
    void guardWithMopSwitchPoints_fallsBackOnCategoryInvalidate() {
        def target = MethodHandles.constant(int, 1)
        def fallback = MethodHandles.constant(int, 2)
        def receiver = new ClassA()

        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, receiver)
        assertEquals(1, guarded.invokeWithArguments())

        IndyInvalidation.invalidateCategory()
        assertEquals(2, guarded.invokeWithArguments())
    }

    @Test
    void guardWithMopSwitchPoints_classOverload() {
        def target = MethodHandles.constant(int, 3)
        def fallback = MethodHandles.constant(int, 4)
        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, ClassA)
        assertEquals(3, guarded.invokeWithArguments())
        IndyInvalidation.invalidateClass(ClassA)
        assertEquals(4, guarded.invokeWithArguments())
    }

    @Test
    void classSwitchPointFor_classAndReceiverOverloads() {
        SwitchPoint byClass = IndyInvalidation.classSwitchPointFor(String)
        SwitchPoint byReceiver = IndyInvalidation.classSwitchPointFor('x')
        assertSame(byClass, byReceiver)
        assertFalse(byClass.hasBeenInvalidated())
        SwitchPoint byNull = IndyInvalidation.classSwitchPointFor((Object) null)
        assertSame(ClassInfo.getClassInfo(NullObject).indySwitchPoint, byNull)
    }

    @Test
    void resetCountersForTesting_zerosCounters() {
        IndyInvalidation.invalidateClass(ClassA)
        IndyInvalidation.invalidateCategory()
        IndyInvalidation.invalidateBulk()
        IndyInvalidation.invalidateUnscoped()
        assertTrue(IndyInvalidation.classInvalidationCount() > 0)
        assertTrue(IndyInvalidation.categoryInvalidationCount() > 0)
        assertTrue(IndyInvalidation.bulkInvalidationCount() > 0)
        IndyInvalidation.resetCountersForTesting()
        assertEquals(0, IndyInvalidation.classInvalidationCount())
        assertEquals(0, IndyInvalidation.categoryInvalidationCount())
        assertEquals(0, IndyInvalidation.bulkInvalidationCount())
    }

    @Test
    void isStockMetaClass_andNeedsBulkInvalidation() {
        def mcImpl = new groovy.lang.MetaClassImpl(ClassA)
        mcImpl.initialize()
        def emc = new ExpandoMetaClass(ClassA, true, true)
        emc.initialize()
        def custom = new PureCustomMetaClass(ClassA)

        assertTrue(IndyInvalidation.isStockMetaClass(mcImpl))
        assertTrue(IndyInvalidation.isStockMetaClass(null))
        assertTrue(IndyInvalidation.isStockMetaClass(emc),
                'EMC is stock MetaClassImpl; exact-class is sufficient')
        assertFalse(IndyInvalidation.isStockMetaClass(custom),
                'non-MetaClassImpl custom kinds require bulk')

        assertFalse(IndyInvalidation.needsBulkInvalidation(mcImpl, mcImpl))
        assertFalse(IndyInvalidation.needsBulkInvalidation(null, mcImpl))
        assertFalse(IndyInvalidation.needsBulkInvalidation(null, emc),
                'EMC install is stock → exact-class')
        assertFalse(IndyInvalidation.needsBulkInvalidation(emc, mcImpl))
        assertFalse(IndyInvalidation.needsBulkInvalidation(mcImpl, mcImpl))
        assertTrue(IndyInvalidation.needsBulkInvalidation(mcImpl, custom),
                'custom MetaClass must bulk-invalidate')
        assertTrue(IndyInvalidation.needsBulkInvalidation(custom, null))
    }

    @Test
    void isStockMetaClass_unwrapsHandleMetaClass() {
        def emc = new ExpandoMetaClass(ClassA, true, true)
        emc.initialize()
        def handle = new org.codehaus.groovy.runtime.HandleMetaClass(emc)
        assertTrue(IndyInvalidation.isStockMetaClass(handle),
                'HandleMetaClass wrapping EMC must unwrap to stock MetaClassImpl')
        assertFalse(IndyInvalidation.needsBulkInvalidation(null, handle))
    }

    @Test
    void invalidateForMetaClassChange_exactForPureMetaClassImpl() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(PolicyParent).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(PolicyChild).indySwitchPoint
        def oldMc = new groovy.lang.MetaClassImpl(PolicyParent)
        oldMc.initialize()
        def newMc = new groovy.lang.MetaClassImpl(PolicyParent)
        newMc.initialize()
        def event = new groovy.lang.MetaClassRegistryChangeEvent(
                GroovySystem.metaClassRegistry, null, PolicyParent, oldMc, newMc)

        IndyInvalidation.invalidateForMetaClassChange(event)

        assertTrue(parentSp.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated(),
                'pure MetaClassImpl replace must stay exact-class')
    }

    @Test
    void invalidateForMetaClassChange_exactForEmc() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(PolicyParentEmc).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(PolicyChildEmc).indySwitchPoint
        def emc = new ExpandoMetaClass(PolicyParentEmc, true, true)
        emc.initialize()
        def event = new groovy.lang.MetaClassRegistryChangeEvent(
                GroovySystem.metaClassRegistry, null, PolicyParentEmc, null, emc)

        IndyInvalidation.invalidateForMetaClassChange(event)

        assertTrue(parentSp.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated(),
                'EMC install is stock → exact-class, no subtype fan-out')
    }

    @Test
    void invalidateForMetaClassChange_bulkForCustomMetaClass() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        def custom = new PureCustomMetaClass(ClassA)
        def event = new groovy.lang.MetaClassRegistryChangeEvent(
                GroovySystem.metaClassRegistry, null, ClassA, null, custom)

        long bulkBefore = IndyInvalidation.bulkInvalidationCount()
        IndyInvalidation.invalidateForMetaClassChange(event)

        assertTrue(spA.hasBeenInvalidated())
        assertTrue(spB.hasBeenInvalidated(),
                'custom MetaClass must bulk-retire loaded class domains')
        assertEquals(bulkBefore + 1, IndyInvalidation.bulkInvalidationCount())
    }

    @Test
    void invalidateForMetaClassChange_perInstance_exactOnly() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(PolicyParentPerInst).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(PolicyChildPerInst).indySwitchPoint
        def instance = new PolicyParentPerInst()
        def emc = new ExpandoMetaClass(PolicyParentPerInst, false, true)
        emc.initialize()
        def event = new groovy.lang.MetaClassRegistryChangeEvent(
                GroovySystem.metaClassRegistry, instance, PolicyParentPerInst, null, emc)

        assertTrue(event.isPerInstanceMetaClassChange())
        IndyInvalidation.invalidateForMetaClassChange(event)

        assertTrue(parentSp.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated(),
                'per-instance MetaClass must not fan out to subtypes')
    }

    @Test
    void invalidateForMetaClassChange_nullType_unscoped() {
        SwitchPoint sp = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        def event = new groovy.lang.MetaClassRegistryChangeEvent(
                GroovySystem.metaClassRegistry, null, null, null, null)
        IndyInvalidation.invalidateForMetaClassChange(event)
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void setStrongMetaClass_firstInstall_bumpsVersionWithoutSwitchPoint() {
        def info = ClassInfo.getClassInfo(LocalGen)
        info.setStrongMetaClass(null)
        info.setWeakMetaClass(null)
        int versionBefore = info.version

        def mc = new groovy.lang.MetaClassImpl(LocalGen)
        mc.initialize()
        SwitchPoint sp = IndyInvalidation.switchPointForMetaClass(mc)
        assertFalse(sp.hasBeenInvalidated())
        info.setStrongMetaClass(mc)

        assertEquals(versionBefore + 1, info.version)
        assertFalse(sp.hasBeenInvalidated(), 'first install must not invalidate MetaClass SwitchPoint')
        assertSame(sp, info.indySwitchPoint)
        info.setStrongMetaClass(null)
    }

    @Test
    void setStrongMetaClass_replace_invalidatesLocalSwitchPoint() {
        def info = ClassInfo.getClassInfo(LocalGenReplace)
        def mc1 = new groovy.lang.MetaClassImpl(LocalGenReplace)
        mc1.initialize()
        info.setStrongMetaClass(mc1)
        SwitchPoint sp = info.indySwitchPoint
        assertFalse(sp.hasBeenInvalidated())

        def mc2 = new groovy.lang.MetaClassImpl(LocalGenReplace)
        mc2.initialize()
        info.setStrongMetaClass(mc2)

        assertTrue(sp.hasBeenInvalidated(), 'replacement must invalidate SwitchPoint')
        info.setStrongMetaClass(null)
    }

    @Test
    void setWeakMetaClass_replace_invalidatesLocalSwitchPoint() {
        def info = ClassInfo.getClassInfo(LocalGenWeak)
        def mc1 = new groovy.lang.MetaClassImpl(LocalGenWeak)
        mc1.initialize()
        info.setWeakMetaClass(mc1)
        SwitchPoint sp = info.indySwitchPoint
        assertFalse(sp.hasBeenInvalidated())

        def mc2 = new groovy.lang.MetaClassImpl(LocalGenWeak)
        mc2.initialize()
        info.setWeakMetaClass(mc2)

        assertTrue(sp.hasBeenInvalidated())
        info.setWeakMetaClass(null)
    }

    @Test
    void bulkWhenNoLiveSwitchPoints_isSafeNoOp() {
        // Detach every tracked type used by this suite so the bulk batch is empty.
        ClassInfo.getClassInfo(ClassA).detachLiveIndySwitchPoint()
        ClassInfo.getClassInfo(ClassB).detachLiveIndySwitchPoint()
        IndyInvalidation.invalidateBulk()
        IndyInvalidation.invalidateCategory()
        IndyInvalidation.invalidateUnscoped()
    }

    @Test
    void newPendingInvalidator_createsIndependentDomain() {
        def a = IndyInvalidation.newPendingInvalidator()
        def b = IndyInvalidation.newPendingInvalidator()
        SwitchPoint spa = a.switchPoint
        SwitchPoint spb = b.switchPoint
        a.invalidate()
        assertTrue(spa.hasBeenInvalidated())
        assertFalse(spb.hasBeenInvalidated())
    }

    @Test
    void statsLogging_enabled_inChildProcess() {
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Dgroovy.indy.invalidation.stats=true',
                '-Djava.util.logging.config.file=',
                '-cp', cp,
                'org.apache.groovy.runtime.indy.IndyInvalidationStatsProbe')
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = proc.inputStream.text
        assertEquals(0, proc.waitFor(), "stats probe failed: $out")
        assertTrue(out.contains('OK'), out)
    }

    @Test
    void classInfoIncVersion_whenTheClassCleared_invalidatesLocally() {
        def info = ClassInfo.getClassInfo(ClearedIncVersionHost)
        SwitchPoint sp = info.indySwitchPoint
        def field = ClassInfo.getDeclaredField('classRef')
        field.accessible = true
        ((WeakReference) field.get(info)).clear()
        assertEquals(null, info.theClass)
        info.incVersion()
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void setPerInstanceMetaClass_retiresClassSwitchPoint() {
        def type = Groovy12191PerInstHost
        def info = ClassInfo.getClassInfo(type)
        def instance = new Groovy12191PerInstHost()
        SwitchPoint sp = info.indySwitchPoint
        assertFalse(sp.hasBeenInvalidated())
        def emc = new ExpandoMetaClass(type, false, true)
        emc.initialize()
        info.setPerInstanceMetaClass(instance, emc)
        assertTrue(sp.hasBeenInvalidated())
        info.setPerInstanceMetaClass(instance, null)
    }

    @Test
    void hasClassLevelMetaClass_weakOnly_firstInstallDoesNotRetire() {
        def info = ClassInfo.getClassInfo(WeakOnlyHost)
        info.setStrongMetaClass(null)
        info.setWeakMetaClass(null)
        def mc = new groovy.lang.MetaClassImpl(WeakOnlyHost)
        mc.initialize()
        SwitchPoint sp = IndyInvalidation.switchPointForMetaClass(mc)
        info.setWeakMetaClass(mc)
        assertFalse(sp.hasBeenInvalidated())
        assertSame(sp, info.indySwitchPoint)
        info.setWeakMetaClass(null)
    }

    @Test
    void guardWithMopSwitchPoints_nullReceiver_usesNullObjectDomain() {
        def target = MethodHandles.constant(int, 5)
        def fallback = MethodHandles.constant(int, 6)
        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, (Object) null)
        assertEquals(5, guarded.invokeWithArguments())
        IndyInvalidation.invalidateClass(NullObject)
        assertEquals(6, guarded.invokeWithArguments())
    }

    @Test
    void switchPointForMetaClass_identityMapDomain() {
        def mc = new groovy.lang.MetaClassImpl(ClassA)
        mc.initialize()
        SwitchPoint sp = IndyInvalidation.switchPointForMetaClass(mc)
        assertSame(sp, IndyInvalidation.switchPointForMetaClass(mc))
        assertFalse(sp.hasBeenInvalidated())
        IndyInvalidation.invalidateMetaClass(mc)
        assertTrue(sp.hasBeenInvalidated())
        assertFalse(IndyInvalidation.switchPointForMetaClass(mc).hasBeenInvalidated())
    }

    @Test
    void classSwitchPointFor_pendingUntilMetaClassInstalled() {
        def type = FreshMcHost
        def info = ClassInfo.getClassInfo(type)
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        SwitchPoint pending = IndyInvalidation.classSwitchPointFor(type)
        assertSame(info.pendingIndySwitchPoint, pending)
        assertFalse(pending.hasBeenInvalidated())
        def mc = new groovy.lang.MetaClassImpl(type)
        mc.initialize()
        info.strongMetaClass = mc
        assertTrue(pending.hasBeenInvalidated())
        SwitchPoint post = IndyInvalidation.classSwitchPointFor(type)
        assertSame(IndyInvalidation.switchPointForMetaClass(mc), post)
        assertFalse(post.hasBeenInvalidated())
        GroovySystem.metaClassRegistry.removeMetaClass(type)
    }

    @Test
    void classSwitchPointFor_doesNotForceMetaClassDuringLink() {
        def type = NoForceMcHost
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        assertEquals(null, ClassInfo.getClassInfo(type).metaClassForClass)
        IndyInvalidation.classSwitchPointFor(type)
        assertEquals(null, ClassInfo.getClassInfo(type).metaClassForClass)
        GroovySystem.metaClassRegistry.removeMetaClass(type)
    }

    @Test
    void customNonMetaClassImpl_usesSameIdentityDomain() {
        def impl = new groovy.lang.MetaClassImpl(ClassB)
        impl.initialize()
        def custom = new groovy.lang.DelegatingMetaClass(impl) {}
        SwitchPoint sp = IndyInvalidation.switchPointForMetaClass(custom)
        assertSame(sp, IndyInvalidation.switchPointForMetaClass(impl))
        IndyInvalidation.invalidateMetaClass(impl)
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void invalidateMetaClass_null_isNoOp() {
        IndyInvalidation.invalidateMetaClass(null)
    }

    private static final class ClassA {}
    private static final class ClassB {}
    private static class Parent {}
    private static final class Child extends Parent {}
    private static final class LocalGen {}
    private static final class LocalGenReplace {}
    private static final class LocalGenWeak {}
    private static final class WeakOnlyHost {}
    private static final class ClearedIncVersionHost {}
    private static interface Marker {}
    private static final class MarkerImpl implements Marker {}
    private static class PolicyParent {}
    private static final class PolicyChild extends PolicyParent {}
    private static class PolicyParentEmc {}
    private static final class PolicyChildEmc extends PolicyParentEmc {}
    private static class PolicyParentPerInst {}
    private static final class PolicyChildPerInst extends PolicyParentPerInst {}
    private static final class FreshMcHost {}
    private static final class NoForceMcHost {}
}

/** Top-level host for per-instance MetaClass SwitchPoint coverage. */
class Groovy12191PerInstHost {}

/**
 * Pure custom MetaClass (not a {@code MetaClassImpl}) used to exercise bulk
 * invalidation for non-stock kinds. {@code @Delegate} fills the MetaClass
 * surface; {@link IndyInvalidation#isStockMetaClass} sees this type itself
 * (not the adaptee), so it is classified as non-stock.
 */
class PureCustomMetaClass implements MetaClass {
    @Delegate
    final MetaClass delegate

    PureCustomMetaClass(Class theClass) {
        this.delegate = new groovy.lang.MetaClassImpl(theClass)
        this.delegate.initialize()
    }
}
