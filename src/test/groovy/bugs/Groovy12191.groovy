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

import org.codehaus.groovy.reflection.ClassInfo
import org.junit.jupiter.api.Test

import java.lang.invoke.SwitchPoint

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12191: scoped indy SwitchPoint invalidation.
 * Metaclass changes for class A must not invalidate class B's SwitchPoint;
 * category enter/leave bulk-invalidates class SwitchPoints so category methods
 * become visible to previously linked sites.
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

        // Category enter/leave bulk-invalidates class domains (single-guard model).
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
    void parentMetaClassChange_invalidatesSubclassSwitchPoint() {
        // Hierarchy fan-out is required for cross-class MOP visibility:
        // Parent.metaClass (EMC) mutations must retire Child-linked sites so
        // new Child().hello() re-selects. This is not MetaClassImpl sharing a
        // table up the hierarchy — each class keeps its own MetaClass.
        SwitchPoint childSp = ClassInfo.getClassInfo(HierChild).indySwitchPoint
        assertFalse(childSp.hasBeenInvalidated())

        HierParent.metaClass.hello = { -> 'from-parent' }

        assertTrue(childSp.hasBeenInvalidated())
        assertScript '''
            class HierParent {}
            class HierChild extends HierParent {}
            HierParent.metaClass.hello = { -> 'from-parent' }
            assert new HierChild().hello() == 'from-parent'
        '''
    }

    @Test
    void parentMetaClassImplReplace_doesNotFanOutToSubclassSwitchPoint() {
        // Pure MetaClassImpl ↔ MetaClassImpl replace has no cross-class EMC
        // visibility: each class owns its own tables. Hierarchy fan-out would
        // only re-link subtype sites without a MOP reason (blackdrag review).
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
    void parentEmcReplace_fansOutToSubclassSwitchPoint() {
        // Installing EMC on Parent publishes methods into Child via hierarchy
        // walk — subtype sites must re-link.
        SwitchPoint childSp = ClassInfo.getClassInfo(HierChildEmc).indySwitchPoint
        assertFalse(childSp.hasBeenInvalidated())

        def emc = new ExpandoMetaClass(HierParentEmc, true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(HierParentEmc, emc)
        try {
            assertTrue(childSp.hasBeenInvalidated(),
                    'EMC install on parent must retire subclass SwitchPoint')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(HierParentEmc)
        }
    }

    @Test
    void unrelatedTypeMetaClassChange_doesNotInvalidateSiblingHierarchy() {
        // Hierarchy fan-out is only to subtypes of the changed type — not global.
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

        // Churn an unrelated type — scoped invalidation must leave TypeD alone.
        10.times { i ->
            TypeC.metaClass."dyn${i}" = { -> i }
        }
        assertFalse(hotSp.hasBeenInvalidated())
    }

    @Test
    void objectArrayMetaClassChange_invalidatesStringArraySwitchPoint() {
        // Array types are final, yet Object[] is the MOP supertype of reference
        // arrays — scoped invalidation must fan out (GROOVY-12191 review).
        SwitchPoint stringArraySp = ClassInfo.getClassInfo(String[]).indySwitchPoint
        assertFalse(stringArraySp.hasBeenInvalidated())

        def emc = new ExpandoMetaClass(Object[], true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(Object[], emc)
        try {
            assertTrue(stringArraySp.hasBeenInvalidated(),
                    'Object[] MetaClass change must retire String[] SwitchPoint')
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(Object[])
        }
    }

    @Test
    void incVersion_scopesToClassHierarchy_notGlobal() {
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
     * become visible on a pure C instance. Documents old-vs-new blast radius and
     * actual MOP visibility.
     */
    @Test
    void blackdragScenario_callOnC_notInvalidatedBySubclassMetaClassChurn() {
        SwitchPoint spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
        SwitchPoint spB = ClassInfo.getClassInfo(BdB).indySwitchPoint
        SwitchPoint spA = ClassInfo.getClassInfo(BdA).indySwitchPoint
        assertFalse(spC.hasBeenInvalidated())
        assertFalse(spB.hasBeenInvalidated())
        assertFalse(spA.hasBeenInvalidated())

        def c = new BdC()
        assert c.fooC() == 'fooC'

        // B.metaClass = EMC + method — fans out to A (subtype of B), not to C (supertype).
        def emcB = new ExpandoMetaClass(BdB, true, true)
        emcB.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(BdB, emcB)
        try {
            assertTrue(spB.hasBeenInvalidated())
            assertTrue(spA.hasBeenInvalidated(), 'EMC on B must fan out to A')
            assertFalse(spC.hasBeenInvalidated(), 'EMC on B must not retire C (supertype)')
            assert c.fooC() == 'fooC'
            assert !c.metaClass.respondsTo(c, 'fooB0')

            spC = ClassInfo.getClassInfo(BdC).indySwitchPoint
            spA = ClassInfo.getClassInfo(BdA).indySwitchPoint
            BdB.metaClass.fooB0 = { -> 'fooB0' }
            assertTrue(spA.hasBeenInvalidated(), 'B EMC update (incVersion) fans out to A')
            assertFalse(spC.hasBeenInvalidated())
            assert new BdB().fooB0() == 'fooB0'
            assert new BdA().fooB0() == 'fooB0'
            assert !c.metaClass.respondsTo(c, 'fooB0')
            assert c.fooC() == 'fooC'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(BdB)
        }

        // A.fooA = {} — exact class A (+ subtypes of A if any), not C.
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

        // Per-instance EMC on a B instance — does not affect C domain or visibility on c.
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
        // Empirical MOP fact that justifies array lattice fan-out for EMC.
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
            // Declared at script level (not inside try): construction-time snapshot
            // of A is taken when D's MetaClass is first built *after* A has EMC.
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

                // Present methods on B are unchanged (not replaced by parent EMC).
                b.metaClass = null
                assert b.m1() == 1
                assert b.m2(0) == 2
                // Missing m2(String) on B is found via hierarchy walk — first call,
                // so no prior monomorphic site needed invalidation for this name.
                assert b.m2('') == -3

                // C was MetaClassImpl-initialized before A gained EMC methods:
                // present m2(Object) handles Integer/String/null; parent overloads
                // are not selected for this MetaClass instance.
                assert c.m2(null) == 40
                assert c.m2(0) == 40
                assert c.m2('') == 40
                c = new C()
                assert c.m2(null) == 40
                assert c.m2(0) == 40
                assert c.m2('') == 40

                // Promote C to EMC: missing-method / EMC inheritance sees A.
                c.metaClass.m1 = { -> -40 }
                assert c.m1() == -40
                assert c.m2(null) == 40
                assert c.m2(0) == -2
                assert c.m2('') == -3

                // D first used after A was modified: MetaClassImpl construction can
                // snapshot ancestor expando methods (timing gap vs C).
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
     * snapshot; hierarchy SwitchPoint on parent EMC is for miss re-select, not
     * for rebuilding MetaClassImpl tables.
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
                // C MetaClassImpl from before / without snapshot of A overloads.
                assert call(c, 0) == 40
                c = new C()
                assert call(c, 0) == 40

                def mc = C.metaClass.delegate
                assert mc instanceof MetaClassImpl

                SwitchPoint spC = ClassInfo.getClassInfo(C).indySwitchPoint
                // Receiver class becomes EMC → class-domain retire (MC changed).
                c.metaClass.m1 = { -> -40 }
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == -2

                def emc = c.metaClass.delegate
                assert emc instanceof ExpandoMetaClass

                // Restore old MetaClassImpl via registry (class-level replace).
                // Instance-only metaClass= may clear a per-instance MC without a
                // class-domain SwitchPoint event; class-level replace always does.
                spC = ClassInfo.getClassInfo(C).indySwitchPoint
                GroovySystem.metaClassRegistry.setMetaClass(C, mc)
                c.metaClass = null
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == 40

                // Late init: remove C's MetaClass so a fresh MetaClassImpl is built
                // while A still has EMC → construction-time snapshot sees A.
                def registry = MetaClassRegistryImpl.getInstance(0)
                spC = ClassInfo.getClassInfo(C).indySwitchPoint
                registry.removeMetaClass(C)
                c.metaClass = null
                assert spC.hasBeenInvalidated()
                assert call(c, 0) == -2
                def newMc = c.metaClass.delegate
                assert newMc instanceof MetaClassImpl
                assert newMc != mc

                // Reset A: hierarchy fans out and re-links, but child MetaClassImpl
                // still holds the construction-time snapshot — SP does not rebuild MC.
                SwitchPoint spAfterA = ClassInfo.getClassInfo(C).indySwitchPoint
                A.metaClass = null
                assert spAfterA.hasBeenInvalidated()
                assert call(c, 0) == -2

                def catched = false
                try {
                    new A().m2(0)
                } catch (MissingMethodException ignored) {
                    catched = true
                }
                assert catched

                // Re-install the EMC that was created while A still had methods.
                // blackdrag's draft expected "clueless about A" → 40, but measured
                // EMC retains ClosureMetaMethods copied from A (m2 Integer/String),
                // so resolution stays -2 — similar retention to MetaClassImpl
                // construction-time snapshot, not live super lookup.
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
     * Hierarchy fan-out is required so a previously linked hit on a parent EMC
     * method re-selects after that parent EMC is removed (miss path inverse).
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
                assert childSp.hasBeenInvalidated()
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
     * Hierarchy fan-out is required so a previously linked miss on the child
     * re-selects after parent EMC install (missing-method path).
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
                assert childSp.hasBeenInvalidated()
                assert call(c) == 'from-parent'
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ParentMiss)
                GroovySystem.metaClassRegistry.removeMetaClass(ChildMiss)
            }
        '''
    }

    /**
     * Hierarchy SwitchPoint re-link must not change present-method resolution on
     * a MetaClassImpl child when the parent later gains EMC overloads.
     */
    @Test
    void parentEmc_doesNotReplacePresentChildMethod_afterHierarchyRelink() {
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
                assert childSp.hasBeenInvalidated() // hierarchy fan-out (over-invalidate OK)
                // Present m2(Object) still wins for Integer after re-link.
                assert call(c, 0) == 40
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ParentPresent)
                GroovySystem.metaClassRegistry.removeMetaClass(ChildPresent)
            }
        '''
    }

    /**
     * Pure Java inheritance matches Groovy for parent EMC visibility
     * ({@code LinkedHashMap} extends {@code HashMap}): hierarchy fan-out is
     * required so already-linked subtype sites re-select (blackdrag: same for
     * Java-based classes).
     */
    @Test
    void javaSubtype_seesParentEmc_andHierarchyFanOut() {
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
                assert lhmSp.hasBeenInvalidated()
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
}
