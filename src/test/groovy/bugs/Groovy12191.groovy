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
package bugs

import groovy.lang.MetaClassImpl
import org.apache.groovy.runtime.indy.IndyInvalidation
import org.codehaus.groovy.reflection.ClassInfo
import org.junit.jupiter.api.Test

import java.lang.invoke.SwitchPoint

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12191: scoped indy SwitchPoint invalidation.
 * Metaclass changes for class A must not invalidate class B's SwitchPoint;
 * category enter/leave bulk-invalidates class SwitchPoints so category methods
 * become visible to previously linked sites. Stock MetaClassImpl/EMC changes
 * are exact-class only — no parent→child SwitchPoint fan-out (PR #2736).
 */
final class Groovy12191 {

    @Test
    void metaclassChange_invalidatesOnlyAffectedClassSwitchPoint() {
        SwitchPoint spA = ClassInfo.getClassInfo(TypeA).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(TypeB).indySwitchPoint
        assertFalse(spA.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())

        TypeA.metaClass.extra = { -> 'from-emc' }
        assertTrue(spA.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())

        assertScript '''
            class TypeA { }
            class TypeB {
                String id() { 'B' }
            }
            TypeA.metaClass.extra = { -> 'from-emc' }
            assert new TypeA().extra() == 'from-emc'
            assert new TypeB().id() == 'B'
        '''
    }

    @Test
    void registrySetMetaClass_invalidatesClassDomain() {
        SwitchPoint sp = ClassInfo.getClassInfo(TypeC).indySwitchPoint
        def mc = new ExpandoMetaClass(TypeC, true, true)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(TypeC, mc)
        assertTrue(sp.hasBeenInvalidated())
    }

    @Test
    void categoryEnterLeave_bulkInvalidatesClassSwitchPoints() {
        SwitchPoint classSp = ClassInfo.getClassInfo(TypeD).indySwitchPoint

        use(TypeDCategory) {
            assert 'decorated' == new TypeD().label()
        }

        assertTrue(classSp.hasBeenInvalidated())
    }

    @Test
    void categoryMethods_areVisibleToDynamicCalls() {
        assertScript '''
            class Host {
                String name() { 'host' }
            }
            class HostCategory {
                static String name(Host self) { 'category' }
            }
            def h = new Host()
            assert h.name() == 'host'
            use (HostCategory) {
                assert h.name() == 'category'
            }
            assert h.name() == 'host'
        '''
    }

    @Test
    void indyCalls_pickUpNewMetaClassMethodsAfterChange() {
        assertScript '''
            class Svc {
                int add(int a, int b) { a + b }
            }
            def s = new Svc()
            assert s.add(1, 2) == 3
            Svc.metaClass.mul = { int a, int b -> a * b }
            s.metaClass = null
            assert s.mul(3, 4) == 12
            assert s.add(3, 4) == 7
            assert new Svc().mul(2, 5) == 10
        '''
    }

    @Test
    void parentMetaClassChange_doesNotInvalidateSubclassSwitchPoint() {
        // Stock policy: parent EMC mutation retires only the parent's domain.
        // Child sites stay warm; the live miss route observes the new method.
        SwitchPoint childSp = ClassInfo.getClassInfo(HierChild).indySwitchPoint
        assertFalse(childSp.hasBeenInvalidated())

        HierParent.metaClass.hello = { -> 'from-parent' }

        assertFalse(childSp.hasBeenInvalidated(),
                'parent EMC must not retire subclass SwitchPoint (stock exact-class)')
        assertScript '''
            class HierParent {}
            class HierChild extends HierParent {}
            HierParent.metaClass.hello = { -> 'from-parent' }
            assert new HierChild().hello() == 'from-parent'
        '''
    }

    @Test
    void parentMetaClassImplReplace_doesNotFanOutToSubclassSwitchPoint() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(HierParentMcImpl).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(HierChildMcImpl).indySwitchPoint
        assertFalse(parentSp.hasBeenInvalidated())
        assertFalse(childSp.hasBeenInvalidated())

        def mc = new MetaClassImpl(HierParentMcImpl)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(HierParentMcImpl, mc)
        try {
            assertTrue(parentSp.hasBeenInvalidated(),
                    'MetaClassImpl replacement must retire the exact class domain')
            assertFalse(childSp.hasBeenInvalidated(),
                    'pure MetaClassImpl parent replace must not retire subclass SwitchPoint')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(HierParentMcImpl)
        }
    }

    @Test
    void parentEmcReplace_doesNotFanOutToSubclassSwitchPoint() {
        SwitchPoint childSp = ClassInfo.getClassInfo(HierChildEmc).indySwitchPoint
        assertFalse(childSp.hasBeenInvalidated())

        def emc = new ExpandoMetaClass(HierParentEmc, true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(HierParentEmc, emc)
        try {
            assertFalse(childSp.hasBeenInvalidated(),
                    'EMC install on parent must not retire subclass SwitchPoint')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(HierParentEmc)
        }
    }

    @Test
    void unrelatedTypeMetaClassChange_doesNotInvalidateSiblingHierarchy() {
        SwitchPoint hotSp = ClassInfo.getClassInfo(HierChildScope).indySwitchPoint
        assertFalse(hotSp.hasBeenInvalidated())

        TypeC.metaClass.siblingOnly = { -> 1 }

        assertFalse(hotSp.hasBeenInvalidated(),
                'MetaClass change on an unrelated type must not retire HierChildScope')
    }

    @Test
    void indyCalls_overrideViaFreshMetaClassInstance() {
        assertScript '''
            class Svc {
                String greet() { 'hello' }
            }
            def s = new Svc()
            assert s.greet() == 'hello'
            def emc = new ExpandoMetaClass(Svc, true, true)
            emc.greet = { -> 'world' }
            emc.initialize()
            GroovySystem.metaClassRegistry.setMetaClass(Svc, emc)
            s.metaClass = null
            assert s.greet() == 'world'
        '''
    }

    @Test
    void unrelatedMetaClassChurn_doesNotInvalidateHotClass() {
        SwitchPoint hotSp = ClassInfo.getClassInfo(TypeD).indySwitchPoint
        assertFalse(hotSp.hasBeenInvalidated())

        10.times { i ->
            TypeC.metaClass."dyn${i}" = { -> i }
        }
        assertFalse(hotSp.hasBeenInvalidated())
    }

    @Test
    void objectArrayMetaClassChange_doesNotInvalidateStringArraySwitchPoint() {
        // Stock exact-class: Object[] EMC does not retire String[] domain.
        // Dispatch still sees the method via the live miss hierarchy walk.
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        assertFalse(stringArraySp.hasBeenInvalidated())

        def emc = new ExpandoMetaClass(Object[], true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(Object[], emc)
        try {
            assertFalse(stringArraySp.hasBeenInvalidated(),
                    'Object[] MetaClass change must not retire String[] SwitchPoint')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(Object[])
        }
    }

    @Test
    void incVersion_scopesToExactClass_notGlobal() {
        SwitchPoint hotSp = ClassInfo.getClassInfo(TypeD).indySwitchPoint
        SwitchPoint targetSp = ClassInfo.getClassInfo(TypeC).indySwitchPoint
        assertFalse(hotSp.hasBeenInvalidated())
        assertFalse(targetSp.hasBeenInvalidated())

        ClassInfo.getClassInfo(TypeC).incVersion()

        assertTrue(targetSp.hasBeenInvalidated())
        assertFalse(hotSp.hasBeenInvalidated(),
                'incVersion must not act as a process-wide flush (GROOVY-12191)')
    }

    /**
     * Blackdrag scenario matrix (PR #2736): {@code call(c)} uses receiver class C.
     * Mutations on B/A must not retire C's domain; methods added on B/A must not
     * become visible on a pure C instance. Stock policy is exact-class throughout.
     */
    @Test
    void blackdragScenario_callOnC_notInvalidatedBySubclassMetaClassChurn() {
        def c = new BdC()
        assert c.fooC() == 'fooC'
        new BdB()
        new BdA()
        SwitchPoint spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(BdB).indySwitchPoint
        SwitchPoint spA = ClassInfo.getClassInfo(BdA).indySwitchPoint
        assertFalse(spC.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())
        assertFalse(spA.hasBeenInvalidated())

        // B.metaClass = EMC + method — exact-class B only (not A, not C).
        def emcB = new ExpandoMetaClass(BdB, true, true)
        emcB.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(BdB, emcB)
        try {
            assertTrue(spB.hasBeenInvalidated())
            assertFalse(spA.hasBeenInvalidated(), 'EMC on B must not fan out to A')
            assertFalse(spC.hasBeenInvalidated(), 'EMC on B must not retire C (supertype)')
            assert c.fooC() == 'fooC'
            assert !c.metaClass.respondsTo(c, 'fooB0')

            spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
            spA = ClassInfo.getClassInfo(BdA).indySwitchPoint
            BdB.metaClass.fooB0 = { -> 'fooB0' }
            // in-place EMC update retires B's domain again; A/C stay warm
            assertFalse(spA.hasBeenInvalidated(), 'B EMC update must not fan out to A')
            assertFalse(spC.hasBeenInvalidated())
            assert new BdB().fooB0() == 'fooB0'
            assert new BdA().fooB0() == 'fooB0' // live hierarchy walk
            assert !c.metaClass.respondsTo(c, 'fooB0')
            assert c.fooC() == 'fooC'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(BdB)
        }

        spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
        BdA.metaClass.fooA = { -> 'fooA' }
        try {
            assertFalse(spC.hasBeenInvalidated())
            assert new BdA().fooA() == 'fooA'
            assert !c.metaClass.respondsTo(c, 'fooA')
            assert c.fooC() == 'fooC'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(BdA)
        }

        spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
        def b = new BdB()
        def emcInst = new ExpandoMetaClass(BdB, false, true)
        emcInst.initialize()
        emcInst.fooB2 = { -> 'fooB2' }
        b.metaClass = emcInst
        assertFalse(spC.hasBeenInvalidated(),
                'per-instance MetaClass on B must not retire C SwitchPoint')
        assert b.fooB2() == 'fooB2'
        assert b.fooC() == 'fooC'
        assert !c.metaClass.respondsTo(c, 'fooB2')
        assert c.fooC() == 'fooC'
        b.metaClass = null
    }

    @Test
    void objectArrayEmcMethod_visibleOnStringArray() {
        assertScript '''
            Object[].metaClass.arrHello = { -> 'arr' }
            try {
                assert (new Object[0]).arrHello() == 'arr'
                assert (new String[0]).arrHello() == 'arr'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Object[])
            }
        '''
    }

    /**
     * Behaviour-only: linked miss on String[] then Object[] EMC add must become
     * visible without String[] SwitchPoint retirement (PR #2736 experiment).
     */
    @Test
    void linkedMiss_stringArray_thenObjectArrayEmcAdd_isVisible() {
        assertScript '''
            def probe(x) {
                try { return x.arrHello() } catch (MissingMethodException e) { return 'miss' }
            }
            String[] arr = ['a', 'b']
            200.times { assert probe(arr) == 'miss' }
            try {
                Object[].metaClass.arrHello = { -> 'array-visible' }
                assert probe(arr) == 'array-visible'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Object[])
                GroovySystem.metaClassRegistry.removeMetaClass(String[])
            }
        '''
    }

    /**
     * Blackdrag PR #2736 comment (corrected parent→child direction): hierarchy
     * walk is for <em>missing</em> methods only. Present methods on the child
     * keep winning; a new overload on the parent is found without needing a
     * prior site; MetaClassImpl construction timing yields C vs D divergence
     * (pre-existing MOP, not fixed by SwitchPoint scoping).
     *
     * @see <a href="https://github.com/apache/groovy/pull/2736#issuecomment-5108446270">PR comment</a>
     */
    @Test
    void blackdragScenario_missingMethodOnly_andMetaClassImplTiming() {
        assertScript '''
            class A {}
            class B extends A {
                def m1() { 1 }
                def m2(Integer x) { 2 }
            }
            class C extends A {
                def m2(x) { 40 }
            }
            class D extends A {
                def m2(x) { 50 }
            }
            def c = new C()
            def b = new B()
            assert b.m1() == 1
            assert b.m2(0) == 2

            A.metaClass.m1 = { -> -1 }
            A.metaClass.m2 = { Integer x -> -2 }
            A.metaClass.m2 = { String x -> -3 }
            try {
                def a = new A()
                assert a.m1() == -1
                assert a.m2(0) == -2
                assert a.m2('') == -3

                b.metaClass = null
                assert b.m1() == 1
                assert b.m2(0) == 2
                assert b.m2('') == -3

                assert c.m2(null) == 40
                assert c.m2(0) == 40
                assert c.m2('') == 40
                c = new C()
                assert c.m2(null) == 40
                assert c.m2(0) == 40
                assert c.m2('') == 40

                c.metaClass.m1 = { -> -40 }
                assert c.m1() == -40
                assert c.m2(null) == 40
                assert c.m2(0) == -2
                assert c.m2('') == -3

                def d = new D()
                assert d.m2(null) == 50
                assert d.m2(0) == -2
                assert d.m2('') == -3
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(A)
                GroovySystem.metaClassRegistry.removeMetaClass(B)
                GroovySystem.metaClassRegistry.removeMetaClass(C)
                GroovySystem.metaClassRegistry.removeMetaClass(D)
            }
        '''
    }

    /**
     * Blackdrag callsite-caching matrix: receiver MetaClass change (exact class)
     * drives re-link; parent EMC remove does not rewrite a child MetaClassImpl
     * snapshot. Stock policy does not require hierarchy SwitchPoint for the
     * miss path.
     */
    @Test
    void blackdragScenario_callsiteCaching_metaClassChangeNotHierarchyVersion() {
        assertScript '''
            import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
            import groovy.lang.MetaClassImpl
            import groovy.lang.ExpandoMetaClass
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class A {}
            class C extends A {
                def m2(x) { 40 }
            }
            def call(x, y) { x.m2(y) }
            def c = new C()

            A.metaClass.m2 = { Integer x -> -2 }
            A.metaClass.m2 = { String x -> -3 }
            try {
                assert call(c, 0) == 40
                c = new C()
                assert call(c, 0) == 40

                def mc = C.metaClass.delegate
                assert mc instanceof MetaClassImpl

                SwitchPoint spC = ClassInfo.getClassInfo(C).indySwitchPoint
                c.metaClass.m1 = { -> -40 }
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == -2

                def emc = c.metaClass.delegate
                assert emc instanceof ExpandoMetaClass

                spC = ClassInfo.getClassInfo(C).indySwitchPoint
                GroovySystem.metaClassRegistry.setMetaClass(C, mc)
                c.metaClass = null
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == 40

                def registry = MetaClassRegistryImpl.getInstance(0)
                spC = ClassInfo.getClassInfo(C).indySwitchPoint
                registry.removeMetaClass(C)
                c.metaClass = null
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == -2
                def newMc = c.metaClass.delegate
                assert newMc instanceof MetaClassImpl
                assert newMc != mc

                // Reset A: stock exact-class — C's SwitchPoint is NOT retired.
                // Child MetaClassImpl still holds the construction-time snapshot.
                SwitchPoint spAfterA = ClassInfo.getClassInfo(C).indySwitchPoint
                A.metaClass = null
                assert !spAfterA.hasBeenInvalidated() :
                        'parent EMC remove must not retire child SwitchPoint'
                assert call(c, 0) == -2

                def catched = false
                try {
                    new A().m2(0)
                } catch (MissingMethodException ignored) {
                    catched = true
                }
                assert catched

                spC = ClassInfo.getClassInfo(C).indySwitchPoint
                GroovySystem.metaClassRegistry.setMetaClass(C, emc)
                c.metaClass = null
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == -2
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(A)
                GroovySystem.metaClassRegistry.removeMetaClass(C)
            }
        '''
    }

    /**
     * Linked hit via live miss route: after parent EMC is removed, the next
     * call misses again — without requiring child SwitchPoint retirement.
     */
    @Test
    void parentEmcRemoveAfterLinkedHit_childCallSiteMissesAgain() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class ParentHit {}
            class ChildHit extends ParentHit {}
            def call(x) { x.hello() }
            def c = new ChildHit()
            ParentHit.metaClass.hello = { -> 'from-parent' }
            try {
                assert call(c) == 'from-parent'
                SwitchPoint childSp = ClassInfo.getClassInfo(ChildHit).indySwitchPoint
                GroovySystem.metaClassRegistry.removeMetaClass(ParentHit)
                assert !childSp.hasBeenInvalidated() :
                        'parent remove must not retire child SwitchPoint'
                def missed = false
                try {
                    call(c)
                } catch (MissingMethodException ignored) {
                    missed = true
                }
                assert missed
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ParentHit)
                GroovySystem.metaClassRegistry.removeMetaClass(ChildHit)
            }
        '''
    }

    /**
     * Linked miss then parent EMC add: live miss route observes the new method
     * without child SwitchPoint retirement.
     */
    @Test
    void parentEmcAfterLinkedMiss_childCallSiteSeesNewMethod() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class ParentMiss {}
            class ChildMiss extends ParentMiss {}
            def call(x) { x.hello() }
            def c = new ChildMiss()
            def missed = false
            try {
                call(c)
            } catch (MissingMethodException ignored) {
                missed = true
            }
            assert missed

            SwitchPoint childSp = ClassInfo.getClassInfo(ChildMiss).indySwitchPoint
            ParentMiss.metaClass.hello = { -> 'from-parent' }
            try {
                assert !childSp.hasBeenInvalidated()
                assert call(c) == 'from-parent'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ParentMiss)
                GroovySystem.metaClassRegistry.removeMetaClass(ChildMiss)
            }
        '''
    }

    /**
     * Present child method must keep winning after parent EMC add (no SP
     * fan-out needed; re-link would not change the outcome either).
     */
    @Test
    void parentEmc_doesNotReplacePresentChildMethod() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class ParentPresent {}
            class ChildPresent extends ParentPresent {
                def m2(x) { 40 }
            }
            def call(x, y) { x.m2(y) }
            def c = new ChildPresent()
            assert call(c, 0) == 40

            SwitchPoint childSp = ClassInfo.getClassInfo(ChildPresent).indySwitchPoint
            ParentPresent.metaClass.m2 = { Integer x -> -2 }
            try {
                assert !childSp.hasBeenInvalidated()
                assert call(c, 0) == 40
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ParentPresent)
                GroovySystem.metaClassRegistry.removeMetaClass(ChildPresent)
            }
        '''
    }

    /**
     * Pure Java inheritance: parent EMC visible on subtype via live hierarchy
     * walk, without subtype SwitchPoint retirement.
     */
    @Test
    void javaSubtype_seesParentEmc_withoutHierarchyFanOut() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            def lhm = new LinkedHashMap()
            def call(x) { x.jHello() }
            def missed = false
            try {
                call(lhm)
            } catch (MissingMethodException ignored) {
                missed = true
            }
            assert missed

            SwitchPoint lhmSp = ClassInfo.getClassInfo(LinkedHashMap).indySwitchPoint
            HashMap.metaClass.jHello = { -> 'jh' }
            try {
                assert new HashMap().jHello() == 'jh'
                assert !lhmSp.hasBeenInvalidated()
                assert call(lhm) == 'jh'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(HashMap)
                GroovySystem.metaClassRegistry.removeMetaClass(LinkedHashMap)
            }
        '''
    }

    static class TypeA {}
    static class TypeB {
        String id() { 'B' }
    }
    static class TypeC {}
    static class TypeD {
        String label() { 'plain' }
    }
    static class TypeDCategory {
        static String label(TypeD self) { 'decorated' }
    }
    static class HierParent {}
    static class HierChild extends HierParent {}
    static class HierParentMcImpl {}
    static class HierChildMcImpl extends HierParentMcImpl {}
    static class HierParentEmc {}
    static class HierChildEmc extends HierParentEmc {}
    static class HierParentScope {}
    static class HierChildScope extends HierParentScope {}
    /** blackdrag scenario types: A extends B extends C */
    static class BdC {
        String fooC() { 'fooC' }
    }
    static class BdB extends BdC {}
    static class BdA extends BdB {}

    /**
     * SwitchPoint domain is owned by the MetaClass instance, not solely by
     * ClassInfo. ClassInfo.getIndySwitchPoint delegates to the installed
     * MetaClass when present.
     */
    @Test
    void metaClassOwnsSwitchPoint_singleDomainViaIdentityMap() {
        def info = ClassInfo.getClassInfo(McOwnerHost)
        try {
            info.strongMetaClass = null
            info.weakMetaClass = null
            def mc = new MetaClassImpl(McOwnerHost)
            mc.initialize()
            SwitchPoint onMc = IndyInvalidation.switchPointForMetaClass(mc)
            info.strongMetaClass = mc
            assertSame(onMc, info.indySwitchPoint)
            assertSame(onMc, IndyInvalidation.classSwitchPointFor(McOwnerHost))
            def custom = new groovy.lang.DelegatingMetaClass(mc) {}
            assertSame(onMc, IndyInvalidation.switchPointForMetaClass(custom))
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(McOwnerHost)
        }
    }

    /**
     * MetaClassImpl continues to observe ancestor MetaClass state on the
     * missing-method path ({@code Object.metaClass.foo} visible on subtypes).
     */
    @Test
    void metaClassImplStillObservesAncestorEmc_objectMetaClassPattern() {
        assertScript '''
            class FollowUp2Child {}
            try {
                Object.metaClass.followUp2Marker = { -> 'from-object' }
                assert new Object().followUp2Marker() == 'from-object'
                assert new FollowUp2Child().followUp2Marker() == 'from-object'
                assert 'x'.followUp2Marker() == 'from-object'
            } finally {
                Object.metaClass = null
                GroovySystem.metaClassRegistry.removeMetaClass(FollowUp2Child)
            }
        '''
    }

    // ------------------------------------------------------------------
    // Property-site probes (blackdrag: "True we need to be sure the
    // property case is covered." — PR #2736 follow-up)
    // ------------------------------------------------------------------

    /**
     * Linked property miss on child, then parent EMC property add: live property
     * miss walk observes the new property without child SwitchPoint retirement.
     */
    @Test
    void property_linkedMissThenParentEmcAdd_isVisible() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class PropParent {}
            class PropChild extends PropParent {}
            def probe(x) {
                try { return x.onlyOnParentProp } catch (MissingPropertyException e) { return 'miss' }
            }
            def c = new PropChild()
            200.times { assert probe(c) == 'miss' }
            SwitchPoint childSp = ClassInfo.getClassInfo(PropChild).indySwitchPoint
            try {
                PropParent.metaClass.onlyOnParentProp = 'now-visible'
                assert !childSp.hasBeenInvalidated() :
                        'parent property EMC must not retire child SwitchPoint'
                assert probe(c) == 'now-visible'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(PropParent)
                GroovySystem.metaClassRegistry.removeMetaClass(PropChild)
            }
        '''
    }

    /**
     * Linked property hit via live miss route, then parent EMC remove: next
     * access misses again without child SwitchPoint retirement.
     */
    @Test
    void property_parentEmcRemoveAfterLinkedHit_missesAgain() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class PropParentHit {}
            class PropChildHit extends PropParentHit {}
            def probe(x) {
                try { return x.helloProp } catch (MissingPropertyException e) { return 'miss' }
            }
            def c = new PropChildHit()
            PropParentHit.metaClass.helloProp = 'from-parent'
            try {
                assert probe(c) == 'from-parent'
                SwitchPoint childSp = ClassInfo.getClassInfo(PropChildHit).indySwitchPoint
                GroovySystem.metaClassRegistry.removeMetaClass(PropParentHit)
                assert !childSp.hasBeenInvalidated()
                assert probe(c) == 'miss'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(PropParentHit)
                GroovySystem.metaClassRegistry.removeMetaClass(PropChildHit)
            }
        '''
    }

    /**
     * Array-lattice <em>method</em> form of a property getter on Object[] is
     * visible on String[] via the live miss route (same as
     * {@link #linkedMiss_stringArray_thenObjectArrayEmcAdd_isVisible}). Plain
     * {@code arr.foo} on a Java array is GPath collect, not MetaClass property
     * miss — so property-style EMC on arrays is exercised through getXxx.
     */
    @Test
    void propertyGetter_linkedMiss_stringArray_thenObjectArrayEmcAdd_isVisible() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            def probe(x) {
                try { return x.getArrOnlyProp() } catch (MissingMethodException e) { return 'miss' }
            }
            def arr = new String[0]
            50.times { assert probe(arr) == 'miss' }
            SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
            try {
                Object[].metaClass.getArrOnlyProp = { -> 'array-visible' }
                assert !stringArraySp.hasBeenInvalidated()
                assert probe(arr) == 'array-visible'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Object[])
                GroovySystem.metaClassRegistry.removeMetaClass(String[])
            }
        '''
    }

    /**
     * Present child property keeps winning after parent EMC adds a same-name
     * property (construction-time / present-property selection).
     */
    @Test
    void property_parentEmc_doesNotReplacePresentChildProperty() {
        assertScript '''
            import org.codehaus.groovy.reflection.ClassInfo
            import java.lang.invoke.SwitchPoint

            class PropParentPresent {}
            class PropChildPresent extends PropParentPresent {
                def getM2() { 40 }
            }
            def probe(x) { x.m2 }
            def c = new PropChildPresent()
            assert probe(c) == 40

            SwitchPoint childSp = ClassInfo.getClassInfo(PropChildPresent).indySwitchPoint
            PropParentPresent.metaClass.m2 = -2
            try {
                assert !childSp.hasBeenInvalidated()
                assert probe(c) == 40
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(PropParentPresent)
                GroovySystem.metaClassRegistry.removeMetaClass(PropChildPresent)
            }
        '''
    }

    /**
     * Exact-class property update on the receiver class itself must retire that
     * class domain and make the property visible (same as methods).
     */
    @Test
    void property_receiverEmcAdd_invalidatesOwnSwitchPointAndIsVisible() {
        SwitchPoint sp = ClassInfo.getClassInfo(PropOwnHost).indySwitchPoint
        assertFalse(sp.hasBeenInvalidated())
        PropOwnHost.metaClass.ownProp = 'yes'
        try {
            assertTrue(sp.hasBeenInvalidated())
            assert new PropOwnHost().ownProp == 'yes'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(PropOwnHost)
        }
    }

    static class McOwnerHost {}
    static class PropOwnHost {}
}
