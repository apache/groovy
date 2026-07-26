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
}
