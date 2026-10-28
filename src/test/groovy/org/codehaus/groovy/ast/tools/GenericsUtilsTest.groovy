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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import java.util.function.BiFunction

import static org.junit.jupiter.api.Assertions.assertThrows

final class GenericsUtilsTest {

    private static List<ClassNode> compile(String code) {
        def compiler = new AstStringCompiler()
        compiler.compile(code, CompilePhase.INSTRUCTION_SELECTION, false).tail()
    }

    private static ClassNode findClassNode(String name, classNodes) {
        classNodes.find { it.name == name }
    }

    //--------------------------------------------------------------------------

    @Test
    void testFindParameterizedType1() {
        def classNodeList = compile '''
            class Base<T, S> {}
            class Derived extends Base<String, List> {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType2() {
        def classNodeList = compile '''
            class Base<T, S> {}
            class Derived2 extends Base<String, List> {}
            class Derived extends Derived2 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType3() {
        def classNodeList = compile '''
            class Base0 {}
            class Base<T, S> extends Base0 {}
            class Derived2 extends Base<String, List> {}
            class Derived extends Derived2 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType4() {
        def classNodeList = compile '''
            interface Base<T, S> {}
            class Derived2 implements Base<String, List> {}
            class Derived extends Derived2 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType5() {
        def classNodeList = compile '''
            interface Base<T, S> {}
            interface Base2 extends Base<String, List> {}
            class Derived2 implements Base2 {}
            class Derived extends Derived2 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType6() {
        def classNodeList = compile '''
            interface Base<T, S> {}
            interface Base2 extends Base<String, List> {}
            class Derived2 implements Base2 {}
            class Derived3 extends Derived2 {}
            class Derived extends Derived3 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    @Test
    void testFindParameterizedType7() {
        def classNodeList = compile '''
            interface Base0 {}
            interface Base<T, S> extends Base0 {}
            interface Base2 extends Base<String, List> {}
            class Derived2 implements Base2 {}
            class Derived3 extends Derived2 {}
            class Derived extends Derived3 {}
        '''
        ClassNode target = findClassNode('Base', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.name == 'Base'
        assert result.isUsingGenerics()
        assert result.genericsTypes.length == 2
        assert result.genericsTypes[0].type.name == 'java.lang.String'
        assert result.genericsTypes[1].type.name == 'java.util.List'
        assert result.redirect() === target
    }

    // GROOVY-9945
    @Test
    void testFindParameterizedType8() {
        def classNodeList = compile '''
            interface I<T> {}
            class A<T> implements I<String> {}
            class B<T> extends A<T> {}
            class C extends B<Number> {}
        '''
        ClassNode target = findClassNode('A', classNodeList)
        ClassNode source = findClassNode('C', classNodeList)
        ClassNode result = GenericsUtils.findParameterizedType(target, source)

        assert result.toString(false) == 'A<java.lang.Number>'
    }

    @Test
    void testMakeDeclaringAndActualGenericsTypeMapOfExactType() {
        def classNodeList = compile '''
            import java.util.function.*
            interface Derived extends BinaryOperator<Integer> {}
        '''
        ClassNode target = ClassHelper.makeWithoutCaching(BiFunction)
        ClassNode source = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(target, source)

        assert m.entrySet().find { it.key.name == 'T' }.value.type.name == 'java.lang.Integer'
        assert m.entrySet().find { it.key.name == 'U' }.value.type.name == 'java.lang.Integer'
        assert m.entrySet().find { it.key.name == 'R' }.value.type.name == 'java.lang.Integer'
    }

    @Test
    void testMakeDeclaringAndActualGenericsTypeMapOfExactType2() {
        def classNodeList = compile '''
            interface IBase<T, U> {}
            class Base<U> implements IBase<String, U> {}
            class Derived extends Base<Integer> {}
        '''
        ClassNode target = findClassNode('IBase', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(target, source)

        assert m.size() == 2
        assert m.entrySet().find { it.key.name == 'T' }.value.type.name == 'java.lang.String'
        assert m.entrySet().find { it.key.name == 'U' }.value.type.name == 'java.lang.Integer'
    }

    @Test
    void testMakeDeclaringAndActualGenericsTypeMapOfExactType3() {
        def classNodeList = compile '''
            interface IBase<T, U, R> {}
            class Base<X,Y> implements IBase<Y,String,X> {}
            class Derived extends Base<Boolean, Integer> {}
        '''
        ClassNode target = findClassNode('IBase', classNodeList)
        ClassNode source = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(target, source)

        assert m.size() == 3
        assert m.entrySet().find { it.key.name == 'R' }.value.type.name == 'java.lang.Boolean'
        assert m.entrySet().find { it.key.name == 'T' }.value.type.name == 'java.lang.Integer'
        assert m.entrySet().find { it.key.name == 'U' }.value.type.name == 'java.lang.String'
    }

    @Test
    void testParameterizeSAM1() {
        def classNodeList = compile '''
            import java.util.function.*
            interface T extends Function<String, Integer> {}
        '''
        ClassNode samType = findClassNode('java.util.function.Function', findClassNode('T', classNodeList).interfaces)

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo[0].length == 1
        assert typeInfo[0][0] == ClassHelper.STRING_TYPE

        assert typeInfo[1] == ClassHelper.Integer_TYPE
    }

    @Test
    void testParameterizeSAM2() {
        def classNodeList = compile '''
            import java.util.function.*
            interface T extends BinaryOperator<Integer> {}
        '''
        ClassNode samType = findClassNode('java.util.function.BinaryOperator', findClassNode('T', classNodeList).interfaces)

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 2
        assert typeInfo.v1[0] == ClassHelper.Integer_TYPE
        assert typeInfo.v1[1] == ClassHelper.Integer_TYPE

        assert typeInfo.v2 == ClassHelper.Integer_TYPE
    }

    // GROOVY-10813
    @Test
    void testParameterizeSAMWithRawType() {
        def classNodeList = compile '''
            interface I extends java.util.function.BinaryOperator {
            }
        '''
        ClassNode samType = findClassNode('java.util.function.BinaryOperator', findClassNode('I', classNodeList).interfaces)

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 2
        assert typeInfo.v1[0].toString(false) == 'java.lang.Object'
        assert typeInfo.v1[1].toString(false) == 'java.lang.Object'

        assert typeInfo.v2.toString(false) == 'java.lang.Object'
    }

    @Test
    void testParameterizeSAMWithRawTypeWithUpperBound() {
        def classNodeList = compile '''
            interface I<T extends CharSequence> {
                T apply(T input);
            }
            abstract class A implements I {
            }
        '''
        ClassNode samType = findClassNode('I', findClassNode('A', classNodeList).interfaces)

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 1
        assert typeInfo.v1[0].toString(false) == 'java.lang.CharSequence'

        assert typeInfo.v2.toString(false) == 'java.lang.CharSequence'
    }

    @Test
    void testParameterizeSAMWithRawTypeWithUpperBounds() {
        def classNodeList = compile '''
            interface I<T extends CharSequence & Serializable> {
                T apply(T input);
            }
            abstract class A implements I {
            }
        '''
        ClassNode samType = findClassNode('I', findClassNode('A', classNodeList).interfaces)

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 1
        assert typeInfo.v1[0].toString(false) == 'java.lang.CharSequence'

        assert typeInfo.v2.toString(false) == 'java.lang.CharSequence'
    }

    // GROOVY-10067, GROOVY-11057
    @Test
    void testParameterizeType1() {
        def classNodeList = compile '''
            @groovy.transform.TypeChecked
            void test() {
                def map = [:]
            }
        '''
        // get the intermediate type of the map literal (LinkedHashMap<#K,#V>)
        def node = classNodeList[0].getDeclaredMethod('test').code.statements[0]
        def type = new StaticTypeCheckingVisitor(classNodeList[0].module.context,
                        classNodeList[0]).getType(node.expression.rightExpression)

        ClassNode mapType = GenericsUtils.parameterizeType(type, ClassHelper.MAP_TYPE)

        assert mapType == ClassHelper.MAP_TYPE
        assert mapType.genericsTypes.length == 2
        assert mapType.genericsTypes[0].name == '#K'
        assert mapType.genericsTypes[1].name == '#V'
        assert mapType.genericsTypes[0].type.unresolvedName == '#K'
        assert mapType.genericsTypes[1].type.unresolvedName == '#V'
        assert mapType.genericsTypes[0].type.name == 'java.lang.Object'
        assert mapType.genericsTypes[1].type.name == 'java.lang.Object'
    }

    // GROOVY-10067, GROOVY-11057
    @Test
    void testParameterizeType2() {
        def classNodeList = compile '''
            @groovy.transform.TypeChecked
            void test() {
                def list = []
            }
        '''
        // get the intermediate type of the list literal (ArrayList<#E>)
        def node = classNodeList[0].getDeclaredMethod('test').code.statements[0]
        def type = new StaticTypeCheckingVisitor(classNodeList[0].module.context,
                        classNodeList[0]).getType(node.expression.rightExpression)

        ClassNode listType = GenericsUtils.parameterizeType(type, ClassHelper.LIST_TYPE)

        assert listType == ClassHelper.LIST_TYPE
        assert listType.genericsTypes.length == 1
        assert listType.genericsTypes[0].name == '#E'
        assert listType.genericsTypes[0].type.unresolvedName == '#E'
        assert listType.genericsTypes[0].type.name == 'java.lang.Object'
    }

    @Test // GROOVY-12319
    void testApplyGenericsSpecFillsMissingFromBounds() {
        def classNodes = compile '''
            class BoundBox<T extends Number> {}
        '''
        ClassNode boundBox = findClassNode('BoundBox', classNodes).getPlainNodeReference()
        boundBox.redirect = findClassNode('BoundBox', classNodes)
        GenericsUtils.applyGenericsSpec(boundBox, [:])
        assert boundBox.genericsTypes[0].type.name.contains('Number')

        ClassNode list = ClassHelper.LIST_TYPE.getPlainNodeReference()
        GenericsUtils.applyGenericsSpec(list, [:])
        assert list.genericsTypes.length == 1
        assert list.genericsTypes[0].type == ClassHelper.OBJECT_TYPE

        GenericsUtils.applyGenericsSpec(list, [E: ClassHelper.STRING_TYPE])
        assert list.genericsTypes[0].type == ClassHelper.STRING_TYPE

        ClassNode object = ClassHelper.OBJECT_TYPE.getPlainNodeReference()
        GenericsUtils.applyGenericsSpec(object, [E: ClassHelper.STRING_TYPE])
        assert object.genericsTypes == null
    }

    @Test // GROOVY-12319
    void testInferGenericsSpecFromOverridesAndDiamondTarget() {
        assert GenericsUtils.diamondTargetOfAnonymousClass(null) == null

        ClassNode noDiamond = new InnerClassNode(ClassHelper.OBJECT_TYPE, 'C$1', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        noDiamond.anonymous = true
        assert GenericsUtils.diamondTargetOfAnonymousClass(noDiamond) == null

        ClassNode diamondIface = ClassHelper.LIST_TYPE.getPlainNodeReference()
        diamondIface.genericsTypes = GenericsType.EMPTY_ARRAY
        ClassNode anon = new InnerClassNode(ClassHelper.OBJECT_TYPE, 'C$2', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE, [diamondIface] as ClassNode[], MixinNode.EMPTY_ARRAY)
        anon.anonymous = true
        assert GenericsUtils.diamondTargetOfAnonymousClass(anon) === diamondIface

        Map empty = GenericsUtils.inferGenericsSpecFromOverrides(anon, ClassHelper.OBJECT_TYPE.getPlainNodeReference(), [:])
        assert empty.isEmpty()

        def classNodes = compile '''
            interface Box<T> {
                T get()
            }
            class Host {
                def m() {
                    new Box<>() {
                        @Override
                        int get() { 1 }
                    }
                }
            }
        '''
        ClassNode host = findClassNode('Host', classNodes)
        ClassNode aic = host.innerClasses.next()
        ClassNode target = GenericsUtils.diamondTargetOfAnonymousClass(aic) ?: aic.unresolvedInterfaces[0]
        Map spec = GenericsUtils.inferGenericsSpecFromOverrides(aic, target, [:])
        assert spec.T == ClassHelper.Integer_TYPE || spec.T?.name == 'java.lang.Integer'
    }

    @Test // GROOVY-12319
    void testPushEnclosingConstructorCall() {
        def classNodes = compile '''
            class C {}
        '''
        def visitor = new StaticTypeCheckingVisitor(classNodes[0].module.context, classNodes[0])
        def ctx = visitor.typeCheckingContext
        def ctor = new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, new ArgumentListExpression())
        ctx.pushEnclosingMethodCall(ctor)
        assert ctx.getEnclosingMethodCall() === ctor
        ctx.popEnclosingMethodCall()

        def ex = assertThrows(IllegalArgumentException) {
            ctx.pushEnclosingMethodCall(new ConstantExpression('x'))
        }
        assert ex.message.contains('constructor call')
    }

    @Test // GROOVY-12319
    void testInferOverridesSkipsUnrelatedNameAndArityMatches() {
        ClassNode box = ClassHelper.makeWithoutCaching('Box')
        def t = ClassHelper.makeWithoutCaching('T')
        t.genericsPlaceHolder = true
        box.genericsTypes = [new GenericsType(t)] as GenericsType[]
        def declared = new MethodNode('convert', Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE,
                [new Parameter(ClassHelper.STRING_TYPE, 's')] as Parameter[], ClassNode.EMPTY_ARRAY, null)
        def unrelated = new MethodNode('convert', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                [new Parameter(ClassHelper.MAP_TYPE, 'm')] as Parameter[], ClassNode.EMPTY_ARRAY, null)
        def superMethod = new MethodNode('convert', Opcodes.ACC_PUBLIC, t,
                [new Parameter(t, 'v')] as Parameter[], ClassNode.EMPTY_ARRAY, null)
        ClassNode impl = ClassHelper.makeWithoutCaching('Impl')
        impl.addMethod(declared)
        impl.addMethod(unrelated)
        ClassNode superType = ClassHelper.makeWithoutCaching('Super')
        superType.redirect = box
        superType.addMethod(superMethod)
        Map spec = GenericsUtils.inferGenericsSpecFromOverrides(impl, superType, [:])
        assert spec['T'] == ClassHelper.STRING_TYPE
    }

    @Test // GROOVY-12319
    void testInferOverridesSkipsUnrelatedParameterErasure() {
        def t = ClassHelper.makeWithoutCaching('T')
        t.genericsPlaceHolder = true
        ClassNode box = ClassHelper.makeWithoutCaching('Box2')
        box.genericsTypes = [new GenericsType(t)] as GenericsType[]
        ClassNode listOfT = ClassHelper.LIST_TYPE.getPlainNodeReference()
        listOfT.genericsTypes = [new GenericsType(t)] as GenericsType[]
        ClassNode superList = ClassHelper.makeWithoutCaching('SuperList')
        superList.redirect = box
        superList.addMethod(new MethodNode('convert', Opcodes.ACC_PUBLIC, t,
                [new Parameter(listOfT, 'xs')] as Parameter[], ClassNode.EMPTY_ARRAY, null))
        ClassNode impl2 = ClassHelper.makeWithoutCaching('Impl2')
        impl2.addMethod(new MethodNode('convert', Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE,
                [new Parameter(ClassHelper.Integer_TYPE, 'n')] as Parameter[], ClassNode.EMPTY_ARRAY, null))
        Map skipped = GenericsUtils.inferGenericsSpecFromOverrides(impl2, superList, [:])
        assert skipped['T'] == null
    }

    @Test // GROOVY-12319
    void testInferOverridesBoxesPrimitiveActualType() {
        def t = ClassHelper.makeWithoutCaching('T')
        t.genericsPlaceHolder = true
        t.redirect = ClassHelper.OBJECT_TYPE
        ClassNode box = ClassHelper.makeWithoutCaching('Box')
        box.genericsTypes = [new GenericsType(t)] as GenericsType[]
        ClassNode superType = ClassHelper.makeWithoutCaching('Super')
        superType.redirect = box
        superType.addMethod(new MethodNode('n', Opcodes.ACC_PUBLIC, t, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        ClassNode impl = ClassHelper.makeWithoutCaching('Impl')
        impl.addMethod(new MethodNode('n', Opcodes.ACC_PUBLIC, ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        Map spec = GenericsUtils.inferGenericsSpecFromOverrides(impl, superType, [:])
        assert spec['T'] == ClassHelper.Integer_TYPE
    }

    // GROOVY-11521
    @Test
    void testParameterizeTypePreservesExtendsWildcard() {
        def classNodes = compile '''
            class Holder {
                ArrayList<? extends Number> list
                ArrayList<? extends List<String>> listOfLists
                HashMap<String, ? extends Number> map
                ArrayList<String> concrete
                ArrayList<List<? extends Number>> nested
            }
        '''
        ClassNode list = fieldType(classNodes, 'Holder', 'list')
        ClassNode asList = GenericsUtils.parameterizeType(list, ClassHelper.LIST_TYPE)
        assert asList.toString(false) == 'java.util.List<? extends java.lang.Number>'
        assert asList.redirect() == ClassHelper.LIST_TYPE
        assert asList.genericsTypes[0].wildcard
        assert asList.genericsTypes[0].upperBounds[0].name == 'java.lang.Number'
        assert asList.genericsTypes[0].lowerBound == null

        ClassNode asCollection = GenericsUtils.parameterizeType(list, ClassHelper.COLLECTION_TYPE)
        assert asCollection.toString(false) == 'java.util.Collection<? extends java.lang.Number>'
        assert asCollection.genericsTypes[0].wildcard

        ClassNode asIterable = GenericsUtils.parameterizeType(list, ClassHelper.ITERABLE_TYPE)
        assert asIterable.toString(false) == 'java.lang.Iterable<? extends java.lang.Number>'
        assert asIterable.genericsTypes[0].wildcard

        // projecting an already-parameterized supertype keeps the wildcard
        ClassNode again = GenericsUtils.parameterizeType(asList, ClassHelper.COLLECTION_TYPE)
        assert again.toString(false) == 'java.util.Collection<? extends java.lang.Number>'

        ClassNode listOfLists = fieldType(classNodes, 'Holder', 'listOfLists')
        assert GenericsUtils.parameterizeType(listOfLists, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<? extends java.util.List<java.lang.String>>'

        ClassNode map = fieldType(classNodes, 'Holder', 'map')
        ClassNode asMap = GenericsUtils.parameterizeType(map, ClassHelper.MAP_TYPE)
        assert asMap.toString(false) == 'java.util.Map<java.lang.String, ? extends java.lang.Number>'
        assert !asMap.genericsTypes[0].wildcard
        assert asMap.genericsTypes[0].type.name == 'java.lang.String'
        assert asMap.genericsTypes[1].wildcard
        assert asMap.genericsTypes[1].upperBounds[0].name == 'java.lang.Number'

        // a wildcard nested inside a concrete type argument is unchanged
        ClassNode nested = fieldType(classNodes, 'Holder', 'nested')
        assert GenericsUtils.parameterizeType(nested, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<java.util.List<? extends java.lang.Number>>'

        ClassNode concrete = fieldType(classNodes, 'Holder', 'concrete')
        assert GenericsUtils.parameterizeType(concrete, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<java.lang.String>'
    }

    // GROOVY-11521
    @Test
    void testParameterizeTypePreservesSuperWildcard() {
        def classNodes = compile '''
            class Holder {
                ArrayList<? super Number> list
                HashMap<String, ? super Number> map
            }
        '''
        ClassNode list = fieldType(classNodes, 'Holder', 'list')
        ClassNode asList = GenericsUtils.parameterizeType(list, ClassHelper.LIST_TYPE)
        assert asList.toString(false) == 'java.util.List<? super java.lang.Number>'
        assert asList.genericsTypes[0].wildcard
        assert asList.genericsTypes[0].lowerBound.name == 'java.lang.Number'
        assert asList.genericsTypes[0].upperBounds == null

        ClassNode asCollection = GenericsUtils.parameterizeType(list, ClassHelper.COLLECTION_TYPE)
        assert asCollection.toString(false) == 'java.util.Collection<? super java.lang.Number>'

        ClassNode map = fieldType(classNodes, 'Holder', 'map')
        assert GenericsUtils.parameterizeType(map, ClassHelper.MAP_TYPE).toString(false) ==
                'java.util.Map<java.lang.String, ? super java.lang.Number>'
    }

    // GROOVY-11521
    @Test
    void testParameterizeTypePropagatesWildcardFromTypeVariable() {
        def classNodes = compile '''
            class Box<T> extends ArrayList<T> {}
            class Mid<E> extends ArrayList<E> {}
            class Box2<T> extends Mid<T> {}
            class Use {
                Box<? extends CharSequence> box
                Box2<? super Integer> box2
                Box<String> concrete
            }
        '''
        ClassNode box = fieldType(classNodes, 'Use', 'box')
        assert GenericsUtils.parameterizeType(box, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<? extends java.lang.CharSequence>'
        assert GenericsUtils.parameterizeType(box, ClassHelper.COLLECTION_TYPE).toString(false) ==
                'java.util.Collection<? extends java.lang.CharSequence>'

        ClassNode box2 = fieldType(classNodes, 'Use', 'box2')
        assert GenericsUtils.parameterizeType(box2, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<? super java.lang.Integer>'

        ClassNode concrete = fieldType(classNodes, 'Use', 'concrete')
        assert GenericsUtils.parameterizeType(concrete, ClassHelper.LIST_TYPE).toString(false) ==
                'java.util.List<java.lang.String>'
    }

    // GROOVY-11521
    @Test
    void testCorrectToGenericsSpecRecurseStopsSelfReferentialWildcard() {
        ClassNode result = correctListOfSelfReferentialWildcard(false)
        assert result.toString(false) == 'java.util.List<? extends Foo<T>>'
        assert result.genericsTypes[0].wildcard
        assert result.genericsTypes[0].lowerBound == null
        assert result.genericsTypes[0].upperBounds[0].name == 'Foo'
        assert result.genericsTypes[0].upperBounds[0].genericsTypes[0].placeholder
        assert result.genericsTypes[0].upperBounds[0].genericsTypes[0].name == 'T'

        ClassNode lower = correctListOfSelfReferentialWildcard(true)
        assert lower.toString(false) == 'java.util.List<? super Foo<T>>'
        assert lower.genericsTypes[0].wildcard
        assert lower.genericsTypes[0].upperBounds == null
        assert lower.genericsTypes[0].lowerBound.name == 'Foo'
        assert lower.genericsTypes[0].lowerBound.genericsTypes[0].placeholder
    }

    // GROOVY-11521
    @Test
    void testCorrectToGenericsSpecRecurseStillSubstitutesOtherPlaceholdersInWildcardBound() {
        ClassNode t = placeholder('T')
        ClassNode u = placeholder('U')
        ClassNode fooOfU = ClassHelper.makeWithoutCaching('Foo').getPlainNodeReference()
        fooOfU.genericsTypes = [new GenericsType(u)] as GenericsType[]
        ClassNode carrier = wildcardCarrier(fooOfU, null)

        ClassNode listOfT = ClassHelper.LIST_TYPE.getPlainNodeReference()
        listOfT.genericsTypes = [new GenericsType(t)] as GenericsType[]

        ClassNode result = GenericsUtils.correctToGenericsSpecRecurse([T: carrier, U: ClassHelper.STRING_TYPE], listOfT)
        assert result.toString(false) == 'java.util.List<? extends Foo<java.lang.String>>'
        assert result.genericsTypes[0].wildcard
        assert result.genericsTypes[0].upperBounds[0].genericsTypes[0].type == ClassHelper.STRING_TYPE
    }

    private static ClassNode correctListOfSelfReferentialWildcard(boolean lowerBound) {
        ClassNode t = placeholder('T')
        ClassNode fooOfT = ClassHelper.makeWithoutCaching('Foo').getPlainNodeReference()
        fooOfT.genericsTypes = [new GenericsType(t)] as GenericsType[]
        ClassNode carrier = lowerBound ? wildcardCarrier(null, fooOfT) : wildcardCarrier(fooOfT, null)

        ClassNode listOfT = ClassHelper.LIST_TYPE.getPlainNodeReference()
        listOfT.genericsTypes = [new GenericsType(t)] as GenericsType[]
        return GenericsUtils.correctToGenericsSpecRecurse([T: carrier], listOfT)
    }

    private static ClassNode placeholder(String name) {
        ClassNode placeholder = ClassHelper.makeWithoutCaching(name)
        placeholder.genericsPlaceHolder = true
        return placeholder
    }

    private static ClassNode wildcardCarrier(ClassNode upper, ClassNode lower) {
        ClassNode qmark = ClassHelper.makeWithoutCaching('?')
        GenericsType wildcard = new GenericsType(qmark, upper == null ? null : [upper] as ClassNode[], lower)
        wildcard.wildcard = true
        ClassNode carrier = ClassHelper.makeWithoutCaching(Object, false)
        carrier.genericsTypes = [wildcard] as GenericsType[]
        return carrier
    }

    private static ClassNode fieldType(List<ClassNode> classNodes, String owner, String name) {
        ClassNode cn = findClassNode(owner, classNodes).getDeclaredField(name).type
        assert cn != null
        return cn
    }
}
