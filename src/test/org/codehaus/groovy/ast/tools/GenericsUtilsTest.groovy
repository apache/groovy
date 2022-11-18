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
import org.junit.Test

final class GenericsUtilsTest {

    private static List<ClassNode> compile(String code) {
        def compiler = new org.codehaus.groovy.ast.builder.AstStringCompiler()
        compiler.compile(code, CompilePhase.SEMANTIC_ANALYSIS, false).tail()
    }

    private static ClassNode findClassNode(String name, List<ClassNode> list) {
        list.find { it.name == name }
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

    @Test // GROOVY-9945
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

        assert result.toString(false) == 'A <java.lang.Number>'
    }

    @Test
    void testMakeDeclaringAndActualGenericsTypeMapOfExactType() {
        def classNodeList = compile '''
            import java.util.function.*
            interface Derived extends BinaryOperator<Integer> {}
        '''
        ClassNode target = ClassHelper.makeWithoutCaching(java.util.function.BiFunction.class)
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
        ClassNode samType = findClassNode('T', classNodeList).interfaces.find { it.name == 'java.util.function.Function' }

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
        ClassNode samType = findClassNode('T', classNodeList).interfaces.find { it.name == 'java.util.function.BinaryOperator' }

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 2
        assert typeInfo.v1[0] == ClassHelper.Integer_TYPE
        assert typeInfo.v1[1] == ClassHelper.Integer_TYPE

        assert typeInfo.v2 == ClassHelper.Integer_TYPE
    }

    @Test // GROOVY-10813
    void testParameterizeSAMWithRawType() {
        def classNodeList = compile '''
            interface I extends java.util.function.BinaryOperator {
            }
        '''
        ClassNode samType = findClassNode('I', classNodeList).interfaces.find { it.name == 'java.util.function.BinaryOperator' }

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
        ClassNode samType = findClassNode('A', classNodeList).interfaces.find { it.name == 'I' }

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
        ClassNode samType = findClassNode('A', classNodeList).interfaces.find { it.name == 'I' }

        def typeInfo = GenericsUtils.parameterizeSAM(samType)

        assert typeInfo.v1.length == 1
        assert typeInfo.v1[0].toString(false) == 'java.lang.CharSequence'

        assert typeInfo.v2.toString(false) == 'java.lang.CharSequence'
    }
}
