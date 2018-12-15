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
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases

import java.util.function.BiFunction

import static groovy.lang.Tuple.tuple

class GenericsUtilsTest extends GroovyTestCase {
    void testFindParameterizedType1() {
        def code = '''
        class Base<T, S> {}
        class Derived extends Base<String, List> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType2() {
        def code = '''
        class Base<T, S> {}
        class Derived2 extends Base<String, List> {}
        class Derived extends Derived2 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType3() {
        def code = '''
        class Base0 {}
        class Base<T, S> extends Base0 {}
        class Derived2 extends Base<String, List> {}
        class Derived extends Derived2 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType4() {
        def code = '''
        interface Base<T, S> {}
        class Derived2 implements Base<String, List> {}
        class Derived extends Derived2 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType5() {
        def code = '''
        interface Base<T, S> {}
        interface Base2 extends Base<String, List> {}
        class Derived2 implements Base2 {}
        class Derived extends Derived2 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType6() {
        def code = '''
        interface Base<T, S> {}
        interface Base2 extends Base<String, List> {}
        class Derived2 implements Base2 {}
        class Derived3 extends Derived2 {}
        class Derived extends Derived3 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testFindParameterizedType7() {
        def code = '''
        interface Base0 {}
        interface Base<T, S> extends Base0 {}
        interface Base2 extends Base<String, List> {}
        class Derived2 implements Base2 {}
        class Derived3 extends Derived2 {}
        class Derived extends Derived3 {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('Base', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        ClassNode parameterizedClass = GenericsUtils.findParameterizedType(genericsClass, actualReceiver, false)
        assert parameterizedClass.isUsingGenerics()
        assert 'Base' == parameterizedClass.name
        GenericsType[] genericsTypes = parameterizedClass.getGenericsTypes()
        assert 2 == genericsTypes.length
        assert 'java.lang.String' == genericsTypes[0].type.name
        assert 'java.util.List' == genericsTypes[1].type.name
        assert genericsClass.is(parameterizedClass.redirect())
    }

    void testMakeDeclaringAndActualGenericsTypeMapOfExactType() {
        def code = '''
        import java.util.function.*
        interface Derived extends BinaryOperator<Integer> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = ClassHelper.makeWithoutCaching(BiFunction.class)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(genericsClass, actualReceiver)

        assert m.entrySet().every { it.value.type.getTypeClass() == Integer }
        assert m.entrySet().grep { it.key.name == 'T' }[0].value.type.getTypeClass() == Integer
        assert m.entrySet().grep { it.key.name == 'U' }[0].value.type.getTypeClass() == Integer
        assert m.entrySet().grep { it.key.name == 'R' }[0].value.type.getTypeClass() == Integer
    }

    void testMakeDeclaringAndActualGenericsTypeMapOfExactType2() {
        def code = '''
        interface IBase<T, U> {}
        class Base<U> implements IBase<String, U> {}
        class Derived extends Base<Integer> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('IBase', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(genericsClass, actualReceiver)

        assert m.entrySet().grep { it.key.name == 'T' }[0].value.type.getTypeClass() == String
        assert m.entrySet().grep { it.key.name == 'U' }[0].value.type.getTypeClass() == Integer
    }

    void testMakeDeclaringAndActualGenericsTypeMapOfExactType3() {
        def code = '''
        interface IBase<T, U, R> {}
        class Base<X, Y> implements IBase<Y, String, X> {}
        class Derived extends Base<Boolean, Integer> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode genericsClass = findClassNode('IBase', classNodeList)
        ClassNode actualReceiver = findClassNode('Derived', classNodeList)

        Map<GenericsType, GenericsType> m = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(genericsClass, actualReceiver)
        println m

        assert m.entrySet().grep { it.key.name == 'X' }[0].value.type.getTypeClass() == Boolean
        assert m.entrySet().grep { it.key.name == 'Y' }[0].value.type.getTypeClass() == Integer
        assert m.entrySet().grep { it.key.name == 'T' }[0].value.type.getTypeClass() == Integer
        assert m.entrySet().grep { it.key.name == 'U' }[0].value.type.getTypeClass() == String
        assert m.entrySet().grep { it.key.name == 'R' }[0].value.type.getTypeClass() == Boolean
    }

    static ClassNode findClassNode(String name, List<ClassNode> classNodeList) {
        return classNodeList.find { it.name == name }
    }

    void testParameterizeSAM() {
        def code = '''
        import java.util.function.*
        interface T extends Function<String, Integer> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode parameterizedClassNode = findClassNode('T', classNodeList).getAllInterfaces().find { it.name.equals('java.util.function.Function') }

        Tuple2<ClassNode[], ClassNode> typeInfo = GenericsUtils.parameterizeSAM(parameterizedClassNode)
        assert 1 == typeInfo.getV1().length
        assert ClassHelper.STRING_TYPE == typeInfo.getV1()[0]
        assert ClassHelper.Integer_TYPE == typeInfo.getV2()
    }

    void testParameterizeSAM2() {
        def code = '''
        import java.util.function.*
        interface T extends BinaryOperator<Integer> {}
        '''
        def ast = new CompilationUnit().tap {
            addSource 'hello.groovy', code
            compile Phases.SEMANTIC_ANALYSIS
        }.ast

        def classNodeList = ast.getModules()[0].getClasses()
        ClassNode parameterizedClassNode = findClassNode('T', classNodeList).getAllInterfaces().find { it.name.equals('java.util.function.BinaryOperator') }

        Tuple2<ClassNode[], ClassNode> typeInfo = GenericsUtils.parameterizeSAM(parameterizedClassNode)
        assert 2 == typeInfo.getV1().length
        assert ClassHelper.Integer_TYPE == typeInfo.getV1()[0]
        assert ClassHelper.Integer_TYPE == typeInfo.getV1()[1]
        assert ClassHelper.Integer_TYPE == typeInfo.getV2()
    }
}
