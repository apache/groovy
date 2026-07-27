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
import org.junit.jupiter.api.Test

import java.lang.invoke.SwitchPoint

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Structural and integration tests for {@link ClassHierarchyIndex}.
 */
final class ClassHierarchyIndexTest {

    @Test
    void collectStrictSupertypes_classHierarchy() {
        def supers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(IndexChild, supers)
        assertTrue(supers.contains(IndexParent))
        assertTrue(supers.contains(Object))
        assertTrue(supers.contains(IndexMarker))
        assertFalse(supers.contains(IndexChild))
        assertFalse(supers.contains(IndexSibling))
    }

    @Test
    void collectStrictSupertypes_arrayCovariance() {
        def supers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(String[], supers)
        assertTrue(supers.contains(Object[]), 'Object[] is a covariant super of String[]')
        assertTrue(supers.contains(CharSequence[]), 'CharSequence[] via component interfaces')
        assertTrue(supers.contains(Serializable[]))
        assertTrue(supers.contains(Object))
        assertTrue(supers.contains(Cloneable))
        assertTrue(supers.contains(Serializable))
        assertFalse(supers.contains(String[]))
        assertFalse(supers.contains(Integer[]))
    }

    @Test
    void collectStrictSupertypes_multiDimArray() {
        def supers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(String[][], supers)
        assertTrue(supers.contains(Object[][]))
        assertTrue(supers.contains(Object[]), 'String[][] <: Object[]')
        assertTrue(supers.contains(CharSequence[][]))
        assertFalse(supers.contains(CharSequence[]), 'String[][] is not <: CharSequence[]')
        assertFalse(supers.contains(String[][]))
    }

    @Test
    void collectStrictSupertypes_primitiveArray() {
        def supers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(int[], supers)
        assertTrue(supers.contains(Object))
        assertTrue(supers.contains(Cloneable))
        assertTrue(supers.contains(Serializable))
        assertFalse(supers.contains(Object[]), 'int[] is not <: Object[]')
        assertFalse(supers.contains(long[]))
    }

    @Test
    void index_registersDescendantsUnderAncestors() {
        // Force ClassInfo construction (registers in the index).
        ClassInfo.getClassInfo(IndexParent)
        ClassInfo.getClassInfo(IndexChild)
        ClassInfo.getClassInfo(IndexSibling)
        ClassInfo.getClassInfo(IndexMarkerImpl)

        def underParent = []
        ClassHierarchyIndex.collectDescendants(IndexParent, underParent)
        assertTrue(underParent.any { it.theClass == IndexChild })
        assertFalse(underParent.any { it.theClass == IndexSibling })

        def underMarker = []
        ClassHierarchyIndex.collectDescendants(IndexMarker, underMarker)
        assertTrue(underMarker.any { it.theClass == IndexChild || it.theClass == IndexMarkerImpl },
                'implementors indexed under IndexMarker')
    }

    @Test
    void index_arrayDescendants_underObjectArrayAndInterfaceArray() {
        ClassInfo.getClassInfo(String[])
        ClassInfo.getClassInfo(Integer[])
        ClassInfo.getClassInfo(Object[])

        def underObjectArray = []
        ClassHierarchyIndex.collectDescendants(Object[], underObjectArray)
        assertTrue(underObjectArray.any { it.theClass == String[] })
        assertTrue(underObjectArray.any { it.theClass == Integer[] })

        def underCharSeqArray = []
        ClassHierarchyIndex.collectDescendants(CharSequence[], underCharSeqArray)
        assertTrue(underCharSeqArray.any { it.theClass == String[] })
        assertFalse(underCharSeqArray.any { it.theClass == Integer[] })
    }

    @Test
    void invalidateViaIndex_fansOutWithoutMissingDescendants() {
        SwitchPoint parentSp = ClassInfo.getClassInfo(IndexFanParent).indySwitchPoint
        SwitchPoint childSp = ClassInfo.getClassInfo(IndexFanChild).indySwitchPoint
        SwitchPoint otherSp = ClassInfo.getClassInfo(IndexFanOther).indySwitchPoint

        IndyInvalidation.invalidateClassHierarchy(IndexFanParent)

        assertTrue(parentSp.hasBeenInvalidated())
        assertTrue(childSp.hasBeenInvalidated())
        assertFalse(otherSp.hasBeenInvalidated())
    }

    @Test
    void invalidateViaIndex_finalLeaf_emptyDescendantsStillRetiresRoot() {
        // Finals have no indexed subtypes; single-path fan-out still retires the root.
        SwitchPoint stringSp = ClassInfo.getClassInfo(String).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(IndexFanOther).indySwitchPoint
        IndyInvalidation.invalidateClassHierarchy(String)
        assertTrue(stringSp.hasBeenInvalidated())
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void invalidateViaIndex_primitive_emptyDescendantsStillRetiresRoot() {
        SwitchPoint intSp = ClassInfo.getClassInfo(int).indySwitchPoint
        SwitchPoint siblingSp = ClassInfo.getClassInfo(IndexFanOther).indySwitchPoint
        IndyInvalidation.invalidateClassHierarchy(int)
        assertTrue(intSp.hasBeenInvalidated())
        assertFalse(siblingSp.hasBeenInvalidated())
    }

    @Test
    void collectStrictSupertypes_objectAndPrimitive_areEmpty() {
        def objectSupers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(Object, objectSupers)
        assertTrue(objectSupers.isEmpty())

        def intSupers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(int, intSupers)
        assertTrue(intSupers.isEmpty())

        def nullSupers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(null, nullSupers)
        assertTrue(nullSupers.isEmpty())
    }

    @Test
    void collectDescendants_unknownAncestor_isEmpty() {
        def out = []
        ClassHierarchyIndex.collectDescendants(NeverRegisteredAncestor, out)
        assertTrue(out.isEmpty())
    }

    @Test
    void collectStrictSupertypes_interface_includesObject() {
        def supers = new LinkedHashSet<Class<?>>()
        ClassHierarchyIndex.collectStrictSupertypes(IndexMarker, supers)
        assertTrue(supers.contains(Object))
    }

    // --- fixtures ---

    private static class IndexParent {}
    private static final class IndexChild extends IndexParent implements IndexMarker {}
    private static final class IndexSibling {}
    private static interface IndexMarker {}
    private static final class IndexMarkerImpl implements IndexMarker {}

    private static class IndexFanParent {}
    private static final class IndexFanChild extends IndexFanParent {}
    private static final class IndexFanOther {}
    /** Never passed to ClassInfo.getClassInfo — no index entry. */
    private static final class NeverRegisteredAncestor {}
}
