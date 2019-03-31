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
package org.codehaus.groovy.ast.tools

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsTestCase
import static org.codehaus.groovy.ast.tools.WideningCategories.*
import static org.codehaus.groovy.ast.ClassHelper.*
import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode

class WideningCategoriesTest extends GenericsTestCase {

    void testBuildCommonTypeWithNullClassNode() {
        ClassNode a = null
        ClassNode b = make(Serializable)
        assert lowestUpperBound(a,b) == null
        assert lowestUpperBound(b,a) == null
    }

    void testBuildCommonTypeWithObjectClassNode() {
        ClassNode a = OBJECT_TYPE
        ClassNode b = make(Serializable)
        assert lowestUpperBound(a,b) == OBJECT_TYPE
        assert lowestUpperBound(b,a) == OBJECT_TYPE
    }

    void testBuildCommonTypeWithVoidClassNode() {
        ClassNode a = VOID_TYPE
        ClassNode b = VOID_TYPE
        assert lowestUpperBound(a,b) == VOID_TYPE
        assert lowestUpperBound(b,a) == VOID_TYPE
    }

    void testBuildCommonTypeWithVoidClassNodeAndAnyNode() {
        ClassNode a = VOID_TYPE
        ClassNode b = make(Set)
        assert lowestUpperBound(a,b) == OBJECT_TYPE
        assert lowestUpperBound(b,a) == OBJECT_TYPE
    }

    void testBuildCommonTypeWithIdenticalInterfaces() {
        ClassNode a = make(Serializable)
        ClassNode b = make(Serializable)
        assert lowestUpperBound(a,b) == make(Serializable)
    }

    void testBuildCommonTypeWithOneInterfaceInheritsFromOther() {
        ClassNode a = make(Set)
        ClassNode b = make(SortedSet)
        assert lowestUpperBound(a,b) == make(Set)
        assert lowestUpperBound(b,a) == make(Set)
    }

    void testBuildCommonTypeWithTwoIncompatibleInterfaces() {
        ClassNode a = make(Set)
        ClassNode b = make(Map)
        assert lowestUpperBound(a,b) == OBJECT_TYPE
        assert lowestUpperBound(b,a) == OBJECT_TYPE
    }

    void testBuildCommonTypeWithOneClassAndOneImplementedInterface() {
        ClassNode a = make(Set)
        ClassNode b = make(HashSet)
        assert lowestUpperBound(a,b) == make(Set)
        assert lowestUpperBound(b,a) == make(Set)
    }

    void testBuildCommonTypeWithOneClassAndNoImplementedInterface() {
        ClassNode a = make(Map)
        ClassNode b = make(HashSet)
        assert lowestUpperBound(a,b) == OBJECT_TYPE
        assert lowestUpperBound(b,a) == OBJECT_TYPE
    }

    void testBuildCommonTypeWithTwoClassesWithoutSuperClass() {
        ClassNode a = make(ClassA)
        ClassNode b = make(ClassB)
        assert lowestUpperBound(a,b) == make(GroovyObject) // GroovyObject because Groovy classes implicitly implement GroovyObject
        assert lowestUpperBound(b,a) == make(GroovyObject)
    }

    void testBuildCommonTypeWithIdenticalPrimitiveTypes() {
        [int_TYPE, long_TYPE, short_TYPE, boolean_TYPE, float_TYPE, double_TYPE].each {
            ClassNode a = it
            ClassNode b = it
            assert lowestUpperBound(a,b) == it
            assert lowestUpperBound(b,a) == it
        }
    }

    void testBuildCommonTypeWithPrimitiveTypeAndItsBoxedVersion() {
        [int_TYPE, long_TYPE, short_TYPE, boolean_TYPE, float_TYPE, double_TYPE].each {
            ClassNode a = it
            ClassNode b = getWrapper(it)
            assert lowestUpperBound(a,b) == getWrapper(it)
            assert lowestUpperBound(b,a) == getWrapper(it)
        }
    }


    void testBuildCommonTypeWithTwoIdenticalClasses() {
        ClassNode a = make(HashSet)
        ClassNode b = make(HashSet)
        assert lowestUpperBound(a,b) == make(HashSet)
        assert lowestUpperBound(b,a) == make(HashSet)
    }

    void testBuildCommonTypeWithOneClassInheritsFromAnother() {
        ClassNode a = make(HashSet)
        ClassNode b = make(LinkedHashSet)
        assert lowestUpperBound(a,b) == make(HashSet)
        assert lowestUpperBound(b,a) == make(HashSet)
    }

    void testBuildCommonTypeWithTwoInterfacesSharingOneParent() {
        ClassNode a = make(InterfaceCA)
        ClassNode b = make(InterfaceDA)
        assert lowestUpperBound(a,b) == make(InterfaceA)
        assert lowestUpperBound(b,a) == make(InterfaceA)
    }

    void testBuildCommonTypeWithTwoInterfacesSharingTwoParents() {
        ClassNode a = make(InterfaceCAB)
        ClassNode b = make(InterfaceDAB)
        assert lowestUpperBound(a,b).interfaces as Set == [make(InterfaceA), make(InterfaceB)] as Set
        assert lowestUpperBound(b,a).interfaces as Set == [make(InterfaceA), make(InterfaceB)] as Set
    }

    void testBuildCommonTypeWithTwoInterfacesSharingTwoParentsAndOneDifferent() {
        ClassNode a = make(InterfaceCAB)
        ClassNode b = make(InterfaceDABE)
        assert lowestUpperBound(a,b).interfaces as Set == [make(InterfaceA), make(InterfaceB)] as Set
        assert lowestUpperBound(b,a).interfaces as Set == [make(InterfaceA), make(InterfaceB)] as Set
    }

    void testBuildCommonTypeFromTwoClassesInDifferentBranches() {
        ClassNode a = make(ClassA1)
        ClassNode b = make(ClassB1)
        assert lowestUpperBound(a,b) == make(GroovyObject)
        assert lowestUpperBound(b,a) == make(GroovyObject)
    }

    void testBuildCommonTypeFromTwoClassesInDifferentBranchesAndOneCommonInterface() {
        ClassNode a = make(ClassA1_Serializable)
        ClassNode b = make(ClassB1_Serializable)
        assert lowestUpperBound(a,b).interfaces as Set == [make(Serializable), make(GroovyObject)] as Set
        assert lowestUpperBound(b,a).interfaces as Set == [make(Serializable), make(GroovyObject)] as Set
    }

    void testBuildCommonTypeFromTwoClassesWithCommonSuperClassAndOneCommonInterface() {
        ClassNode a = make(BottomA)
        ClassNode b = make(BottomB)
        ClassNode type = lowestUpperBound(a, b)
        assert type.name =~ /.*Top/
        assert type.superClass == make(Top) // includes interface GroovyObject
        assert type.interfaces as Set == [make(Serializable)] as Set // extra interface
        type = lowestUpperBound(b, a)
        assert type.name =~ /.*Top/
        assert type.superClass == make(Top)
        assert type.interfaces as Set == [make(Serializable)] as Set
    }

    void testStringWithGString() {
        ClassNode a = make(String)
        ClassNode b = make(GString)
        ClassNode type = lowestUpperBound(a,b)
        assert type.interfaces as Set == [make(CharSequence), make(Comparable), make(Serializable)] as Set
    }

    void testDistinctPrimitiveTypes() {
        ClassNode a = int_TYPE // primitive int
        ClassNode b = long_TYPE // primitive long
        assert lowestUpperBound(a,b) == long_TYPE
    }

    void testIdenticalPrimitiveTypes() {
        ClassNode a = int_TYPE // primitive int
        ClassNode b = int_TYPE // primitive int
        assert lowestUpperBound(a,b) == int_TYPE
        assert lowestUpperBound(b,a) == int_TYPE
    }

    void testLUBWithTwoInterfacesAndSameGenericArg() {
        ClassNode a = extractTypesFromCode("List<String> type").type
        ClassNode b = extractTypesFromCode("List<String> type").type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub == make(List)
        assert lub.genericsTypes.length == 1
        assert lub.genericsTypes[0].type == STRING_TYPE
    }

    void testLUBWithTwoInterfacesAndCommonSuperClassGenericArg() {
        ClassNode a = extractTypesFromCode("List<Integer> type").type
        ClassNode b = extractTypesFromCode("List<Long> type").type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub == make(List)
        assert lub.genericsTypes.length == 1
        assert lub.genericsTypes[0].wildcard
        assert lub.genericsTypes[0].upperBounds[0].superClass == Number_TYPE
        assert make(Comparable) in lub.genericsTypes[0].upperBounds[0].interfaces
    }

    void testLUBWithTwoInterfacesAndSingleCommonInterface() {
        ClassNode a = extractTypesFromCode("List<Set> type").type
        ClassNode b = extractTypesFromCode("List<List> type").type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub == make(List)
        assert lub.genericsTypes.length == 1
        assert lub.genericsTypes[0].wildcard
        assert lub.genericsTypes[0].upperBounds[0] == make(Collection)
    }

    void testLUBWithTwoInterfacesAndNestedSingleCommonInterface() {
        ClassNode a = extractTypesFromCode("Collection<List<Set>> type").type
        ClassNode b = extractTypesFromCode("Collection<List<SortedSet>> type").type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub == make(Collection)
        assert lub.genericsTypes.length == 1
        def nestedType = lub.genericsTypes[0].type
        assert nestedType == make(List)
        assert nestedType.genericsTypes.length==1
        assert nestedType.genericsTypes[0].wildcard
        assert nestedType.genericsTypes[0].upperBounds[0] == make(Set)
    }

    void testLUBWithTwoArgumentTypesSharingOneInterfaceNotImplementedBySuperClass() {
        // BottomA extends Top implements Serializable
        // BottomB extends Top implements Serializable
        // Top does not implement Serializable
        ClassNode a = extractTypesFromCode('List<org.codehaus.groovy.ast.tools.WideningCategoriesTest.BottomA> type').type
        ClassNode b = extractTypesFromCode('List<org.codehaus.groovy.ast.tools.WideningCategoriesTest.BottomB> type').type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub == LIST_TYPE // List<? extends Top>
        assert lub.genericsTypes.length == 1
        assert lub.genericsTypes[0].wildcard
        ClassNode genericType = lub.genericsTypes[0].upperBounds[0]
        assert genericType instanceof LowestUpperBoundClassNode
        assert genericType.superClass == make(Top)
        assert genericType.interfaces == [make(Serializable)]
    }

    void testLUBWithTwoParameterizedTypesSharingOneInterfaceNotImplementedBySuperClass() {
        // PTopInt extends PTop<Integer> implements Serializable
        // PTopLong extends PTop<Long> implements Serializable
        // PTop<E> does not implement Serializable
        ClassNode a = extractTypesFromCode('org.codehaus.groovy.ast.tools.WideningCategoriesTest.PTopInt type').type
        ClassNode b = extractTypesFromCode('org.codehaus.groovy.ast.tools.WideningCategoriesTest.PTopLong type').type
        ClassNode lub = lowestUpperBound(a,b)
        assert lub instanceof LowestUpperBoundClassNode // a virtual class which extends PTop<? extends Number> and implements Serializable
        assert lub.unresolvedSuperClass == make(PTop)
        assert lub.unresolvedSuperClass.genericsTypes.length == 1
        assert lub.unresolvedSuperClass.genericsTypes[0].wildcard // ? extends Number
        ClassNode genericType = lub.unresolvedSuperClass.genericsTypes[0].upperBounds[0]
        assert genericType == Long_TYPE
    }


    void testCommonAssignableType() {
        def typeA = extractTypesFromCode('LinkedList type').type
        def typeB = extractTypesFromCode('List type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType == make(List)
    }

    void testCommonAssignableType2() {
        def typeA = extractTypesFromCode('LinkedHashSet type').type
        def typeB = extractTypesFromCode('List type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType == make(Collection)
    }

    void testCommonAssignableTypeWithGenerics() {
        def typeA = extractTypesFromCode('LinkedHashSet<String> type').type
        def typeB = extractTypesFromCode('List<String> type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType == make(Collection)
    }

    void testLUBOfTwoListTypes() {
        def typeA = extractTypesFromCode('ArrayList type').type
        def typeB = extractTypesFromCode('LinkedList type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType instanceof LowestUpperBoundClassNode
        assert superType.superClass == make(AbstractList)
        assert superType.interfaces as Set == [make(Serializable), make(Cloneable)] as Set
    }

    void testLUBOfTwoListTypesWithSameGenerics() {
        def typeA = extractTypesFromCode('ArrayList<String> type').type
        def typeB = extractTypesFromCode('LinkedList<String> type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType instanceof LowestUpperBoundClassNode
        assert superType.superClass == make(AbstractList)
        assert superType.interfaces as Set == [make(Serializable), make(Cloneable)] as Set
        assert superType.genericsTypes.length == 1
        assert superType.genericsTypes[0].type == STRING_TYPE

    }

    void testLUBOfTwoListTypesWithDistinctGenerics() {
        def typeA = extractTypesFromCode('ArrayList<String> type').type
        def typeB = extractTypesFromCode('LinkedList<Integer> type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType instanceof LowestUpperBoundClassNode
        assert superType.superClass == make(AbstractList)
        assert superType.interfaces as Set == [make(Serializable), make(Cloneable)] as Set
        assert superType.genericsTypes.length == 1
        def type = superType.genericsTypes[0]
        assert type.wildcard
        assert type.upperBounds[0] instanceof LowestUpperBoundClassNode
        [Comparable, Serializable].each {
            assert make(it) in type.upperBounds[0].interfaces
        }
    }

    void testLUBOfArrayTypes() {
        def typeA = extractTypesFromCode('Number[] type').type
        def typeB = extractTypesFromCode('Integer[] type').type
        def superType = lowestUpperBound(typeA, typeB)
        assert superType.isArray()
        def component = superType.getComponentType()
        assert component == make(Number)
    }

    // ---------- Classes and Interfaces used in this unit test ----------------
    private static interface InterfaceA {}
    private static interface InterfaceB {}
    private static interface InterfaceE {}
    private static interface InterfaceCA extends InterfaceA {}
    private static interface InterfaceDA extends InterfaceA {}
    private static interface InterfaceCAB extends InterfaceA, InterfaceB {}
    private static interface InterfaceDAB extends InterfaceA, InterfaceB {}
    private static interface InterfaceDABE extends InterfaceA, InterfaceB, InterfaceE {}

    private static class ClassA {}
    private static class ClassB {}
    private static class ClassA1 extends ClassA {}
    private static class ClassB1 extends ClassB {}
    private static class ClassA1_Serializable extends ClassA implements Serializable {}
    private static class ClassB1_Serializable extends ClassB implements Serializable {}

    private static class Top {}
    private static class BottomA extends Top implements Serializable {}
    private static class BottomB extends Top implements Serializable {}

    private static class PTop<E> {}
    private static class PTopInt extends PTop<Integer> implements Serializable {}
    private static class PTopLong extends PTop<Long>  implements Serializable {}
}
