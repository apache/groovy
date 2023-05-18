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
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.junit.Test

final class GenericsUtilsTest {

    private static List<ClassNode> compile(String code) {
        def compiler = new org.codehaus.groovy.ast.builder.AstStringCompiler()
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
        ClassNode target = ClassHelper.makeWithoutCaching(java.util.function.BiFunction)
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
            @groovy.transform.CompileStatic
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
            @groovy.transform.CompileStatic
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
}
