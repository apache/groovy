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
 * Unit tests for {@link IndyInvalidation} — single class-domain SwitchPoint model.
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
    void invalidateClass_fansOutToSubtypes() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(Parent).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(Child).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint

        IndyInvalidation.invalidateClass(Parent)

        assertTrue(parentSp.hasBeenInvalidated())
        assertTrue(childSp.hasBeenInvalidated())
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_null_isNoOp() {
        long before = IndyInvalidation.classInvalidationCount()
        IndyInvalidation.invalidateClass(null)
        assertEquals(before, IndyInvalidation.classInvalidationCount())
    }

    @Test
    void invalidateClassHierarchy_finalClass_emptyDescendantsStillRetiresRoot() {
        // Finals have no indexed subtypes; single-path fan-out still retires only the root.
        SwitchPoint stringSp = ClassInfo.getClassInfo(String).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        IndyInvalidation.invalidateClassHierarchy(String)
        assertTrue(stringSp.hasBeenInvalidated())
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClassHierarchy_objectArray_fansOutToReferenceArrays() {
        // Arrays are final per Class.getModifiers(), but Object[] is still a
        // MOP-relevant supertype of every reference array (GROOVY-12191 review).
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        SwitchPoint integerArraySp = ClassInfo.getClassInfo(Integer[]).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        assertFalse(objectArraySp.hasBeenInvalidated())
        assertFalse(stringArraySp.hasBeenInvalidated())

        IndyInvalidation.invalidateClassHierarchy(Object[])

        assertTrue(objectArraySp.hasBeenInvalidated())
        assertTrue(stringArraySp.hasBeenInvalidated(), 'String[] must retire when Object[] domain is invalidated')
        assertTrue(integerArraySp.hasBeenInvalidated(), 'Integer[] must retire when Object[] domain is invalidated')
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClassHierarchy_stringArray_doesNotRetireUnrelatedArrays() {
        // String[] has no array subtypes; sibling array types must stay live.
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        SwitchPoint integerArraySp = ClassInfo.getClassInfo(Integer[]).indySwitchPoint
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(String[])

        assertTrue(stringArraySp.hasBeenInvalidated())
        assertFalse(integerArraySp.hasBeenInvalidated())
        assertFalse(objectArraySp.hasBeenInvalidated(), 'supertype Object[] must not be retired by String[] invalidation')
    }

    @Test
    void invalidateClassHierarchy_primitiveArray_retiresOnlyThatArrayType() {
        // Primitive arrays are final and have no subtypes, but must not take the
        // non-array final short-circuit incorrectly relative to Object[] handling.
        SwitchPoint intArraySp = ClassInfo.getClassInfo(int[]).indySwitchPoint
        SwitchPoint longArraySp = ClassInfo.getClassInfo(long[]).indySwitchPoint
        SwitchPoint objectArraySp = ClassInfo.getClassInfo(Object[]).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(int[])

        assertTrue(intArraySp.hasBeenInvalidated())
        assertFalse(longArraySp.hasBeenInvalidated())
        assertFalse(objectArraySp.hasBeenInvalidated())
    }

    @Test
    void invalidateClass_objectArray_viaApi_fansOut() {
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        IndyInvalidation.invalidateClass(Object[])
        assertTrue(stringArraySp.hasBeenInvalidated())
    }

    @Test
    void guardWithMopSwitchPoints_stringArrayFallsBackOnObjectArrayInvalidate() {
        def target = MethodHandles.constant(int, 11)
        def fallback = MethodHandles.constant(int, 12)
        def receiver = new String[0]
        def guarded = IndyInvalidation.guardWithMopSwitchPoints(target, fallback, receiver)
        assertEquals(11, guarded.invokeWithArguments())

        IndyInvalidation.invalidateClass(Object[])
        assertEquals(12, guarded.invokeWithArguments(),
                'String[]-linked site must fall back after Object[] MetaClass-domain invalidation')
    }

    @Test
    void invalidateClassHierarchy_interfaceArray_fansOutToImplementorArrays() {
        // Same final-array trap as Object[]: CharSequence[] is final yet
        // assignable-from String[] (interface-array covariance).
        SwitchPoint charSeqArraySp = ClassInfo.getClassInfo(CharSequence[]).indySwitchPoint
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        SwitchPoint integerArraySp = ClassInfo.getClassInfo(Integer[]).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(CharSequence[])

        assertTrue(charSeqArraySp.hasBeenInvalidated())
        assertTrue(stringArraySp.hasBeenInvalidated(),
                'String[] must retire when CharSequence[] domain is invalidated')
        assertFalse(integerArraySp.hasBeenInvalidated())
    }

    @Test
    void invalidateClassHierarchy_multiDimObjectArray_fansOut() {
        // Multi-dim arrays are also final classes; Object[][] still fans out to String[][].
        SwitchPoint object2dSp = ClassInfo.getClassInfo(Object[][]).indySwitchPoint
        SwitchPoint string2dSp = ClassInfo.getClassInfo(String[][]).indySwitchPoint
        SwitchPoint string1dSp = ClassInfo.getClassInfo(String[]).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(Object[][])

        assertTrue(object2dSp.hasBeenInvalidated())
        assertTrue(string2dSp.hasBeenInvalidated(),
                'String[][] must retire when Object[][] domain is invalidated')
        assertFalse(string1dSp.hasBeenInvalidated(),
                '1-D String[] is not assignable-from Object[][]')
    }

    @Test
    void invalidateCategory_bulkInvalidatesClassDomains() {
        SwitchPoint classSp = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        long catCount = IndyInvalidation.categoryInvalidationCount()

        IndyInvalidation.invalidateCategory()

        assertTrue(classSp.hasBeenInvalidated())
        assertEquals(catCount + 1, IndyInvalidation.categoryInvalidationCount())
        // Fresh SP available after bulk retire
        SwitchPoint fresh = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        assertFalse(fresh.hasBeenInvalidated())
    }

    @Test
    void invalidateUnscoped_retiresLoadedClassSwitchPoints() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        long before = IndyInvalidation.classInvalidationCount()
        IndyInvalidation.invalidateUnscoped()
        assertTrue(spA.hasBeenInvalidated())
        assertTrue(spB.hasBeenInvalidated())
        assertEquals(before + 1, IndyInvalidation.classInvalidationCount())
    }

    @Test
    void classInfoIncVersion_invalidatesClassDomain_notSiblings() {
        SwitchPoint spA = ClassInfo.getClassInfo(ClassA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        int versionBefore = ClassInfo.getClassInfo(ClassA).version

        ClassInfo.getClassInfo(ClassA).incVersion()

        assertEquals(versionBefore + 1, ClassInfo.getClassInfo(ClassA).version)
        assertTrue(spA.hasBeenInvalidated())
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

        // Category maps to bulk class-domain invalidation (single-guard model).
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
        assertTrue(IndyInvalidation.classInvalidationCount() > 0)
        assertTrue(IndyInvalidation.categoryInvalidationCount() > 0)
        IndyInvalidation.resetCountersForTesting()
        assertEquals(0, IndyInvalidation.classInvalidationCount())
        assertEquals(0, IndyInvalidation.categoryInvalidationCount())
    }

    @Test
    void setStrongMetaClass_firstInstall_bumpsVersionWithoutSwitchPoint() {
        def info = ClassInfo.getClassInfo(LocalGen)
        info.setStrongMetaClass(null)
        info.setWeakMetaClass(null)
        SwitchPoint sp = info.indySwitchPoint
        assertFalse(sp.hasBeenInvalidated())
        int versionBefore = info.version

        def mc = new groovy.lang.MetaClassImpl(LocalGen)
        mc.initialize()
        info.setStrongMetaClass(mc)

        assertEquals(versionBefore + 1, info.version)
        assertFalse(sp.hasBeenInvalidated(), 'first install must not invalidate SwitchPoint')
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
    void invalidateClassHierarchy_interface_fansOutToImplementors() {
        SwitchPoint ifaceSp = ClassInfo.getClassInfo(Marker).indySwitchPoint
        SwitchPoint implSp = ClassInfo.getClassInfo(MarkerImpl).indySwitchPoint
        SwitchPoint otherSp = ClassInfo.getClassInfo(ClassB).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(Marker)

        assertTrue(ifaceSp.hasBeenInvalidated())
        assertTrue(implSp.hasBeenInvalidated())
        assertFalse(otherSp.hasBeenInvalidated())
    }

    @Test
    void invalidateClassHierarchy_primitive_emptyDescendantsStillRetiresRoot() {
        // Primitives have no indexed subtypes; single-path fan-out still retires the root.
        SwitchPoint intSp = ClassInfo.getClassInfo(int).indySwitchPoint
        SwitchPoint sibling = ClassInfo.getClassInfo(ClassB).indySwitchPoint
        IndyInvalidation.invalidateClassHierarchy(int)
        assertTrue(intSp.hasBeenInvalidated())
        assertFalse(sibling.hasBeenInvalidated())
    }

    @Test
    void invalidateClassHierarchy_whenRootAlreadyDetached_stillFansOut() {
        // Root detach returns null; subtypes still retire (covers rootSp == null branch).
        def parentInfo = ClassInfo.getClassInfo(ParentAlreadyDetached)
        def childInfo = ClassInfo.getClassInfo(ChildAlreadyDetached)
        SwitchPoint parentSp = parentInfo.indySwitchPoint
        SwitchPoint childSp = childInfo.indySwitchPoint
        parentInfo.detachLiveIndySwitchPoint() // retire without invalidateAll
        assertTrue(parentSp != null)
        IndyInvalidation.invalidateClassHierarchy(ParentAlreadyDetached)
        // parent was already detached (not necessarily invalidated); child must be retired
        assertTrue(childSp.hasBeenInvalidated())
    }

    @Test
    void invalidateAllLoaded_whenNoLiveSwitchPoints_isNoOpBatch() {
        // Detach every tracked type used by this suite so the bulk batch is empty.
        ClassInfo.getClassInfo(ClassA).detachLiveIndySwitchPoint()
        ClassInfo.getClassInfo(ClassB).detachLiveIndySwitchPoint()
        IndyInvalidation.invalidateAllLoadedClassSwitchPoints() // empty-batch early return
        // still callable and safe
        IndyInvalidation.invalidateAllLoadedClassSwitchPoints()
    }

    @Test
    void newClassInvalidator_createsIndependentDomain() {
        def a = IndyInvalidation.newClassInvalidator()
        def b = IndyInvalidation.newClassInvalidator()
        SwitchPoint spa = a.switchPoint
        SwitchPoint spb = b.switchPoint
        a.invalidate()
        assertTrue(spa.hasBeenInvalidated())
        assertFalse(spb.hasBeenInvalidated())
    }

    @Test
    void statsLogging_enabled_inChildProcess() {
        // STATS_LOG is fixed at class init; cover the fine-log branches in a fresh JVM.
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Dgroovy.indy.invalidation.stats=true',
                '-Djava.util.logging.config.file=', // allow programmatic FINE below via default
                '-cp', cp,
                'org.apache.groovy.runtime.indy.IndyInvalidationStatsProbe')
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = proc.inputStream.text
        assertEquals(0, proc.waitFor(), "stats probe failed: $out")
        assertTrue(out.contains('OK'), out)
    }

    @Test
    void invalidateClassHierarchy_skipsClearedClassInfoWeakRefs() {
        // Force a ClassInfo whose weak Class ref is cleared so the scan hits loaded == null.
        def info = ClassInfo.getClassInfo(ClearedRefHost)
        info.indySwitchPoint // ensure live
        def field = ClassInfo.getDeclaredField('classRef')
        field.accessible = true
        def wr = (WeakReference) field.get(info)
        wr.clear()
        assertEquals(null, info.theClass)
        // Must not throw; other ClassInfos still scanned.
        IndyInvalidation.invalidateClassHierarchy(Parent)
    }

    @Test
    void classInfoIncVersion_whenTheClassCleared_invalidatesLocally() {
        def info = ClassInfo.getClassInfo(ClearedIncVersionHost)
        SwitchPoint sp = info.indySwitchPoint
        def field = ClassInfo.getDeclaredField('classRef')
        field.accessible = true
        ((WeakReference) field.get(info)).clear()
        assertEquals(null, info.theClass)
        info.incVersion() // else branch: invalidateIndySwitchPoint only
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void setPerInstanceMetaClass_retiresClassSwitchPoint() {
        def type = Groovy12191PerInstHost
        def info = ClassInfo.getClassInfo(type)
        // Construct before installing a custom class-level MetaClass.
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
        SwitchPoint sp = info.indySwitchPoint
        info.setWeakMetaClass(mc) // first weak install
        assertFalse(sp.hasBeenInvalidated())
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

    private static final class ClassA {}
    private static final class ClassB {}
    private static class Parent {}
    private static final class Child extends Parent {}
    private static class ParentAlreadyDetached {}
    private static final class ChildAlreadyDetached extends ParentAlreadyDetached {}
    private static final class LocalGen {}
    private static final class LocalGenReplace {}
    private static final class LocalGenWeak {}
    private static final class WeakOnlyHost {}
    private static final class ClearedRefHost {}
    private static final class ClearedIncVersionHost {}
    private static interface Marker {}
    private static final class MarkerImpl implements Marker {}
}

/** Top-level host for per-instance MetaClass SwitchPoint coverage. */
class Groovy12191PerInstHost {}

