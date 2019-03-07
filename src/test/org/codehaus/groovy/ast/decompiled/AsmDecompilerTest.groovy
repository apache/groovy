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
package org.codehaus.groovy.ast.decompiled

import junit.framework.TestCase
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.decompiled.support.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.ClassNodeResolver
import org.codehaus.groovy.control.CompilationUnit
import org.objectweb.asm.Opcodes

class AsmDecompilerTest extends TestCase {

    void "test decompile class"() {
        ClassNode node = decompile()
        assert AsmDecompilerTestData.name == node.name
        assert (node.modifiers & Opcodes.ACC_PUBLIC) != 0

        assert !node.genericsPlaceHolder
        assert node.usingGenerics

        def t = node.genericsTypes[0]
        assert t.name == 'T'
        assert t.placeholder
        assert t.type.name == Object.name
        assert t.type.genericsPlaceHolder
        assert !t.lowerBound

        def list = t.upperBounds[0]
        assert list.name == List.name
        assert list.usingGenerics
        assert !list.genericsPlaceHolder

        def wildcard = list.genericsTypes[0]
        assert wildcard.wildcard
        assert !wildcard.type.genericsPlaceHolder //todo?
        assert !wildcard.upperBounds

        def tRef = wildcard.lowerBound
        assert tRef.genericsPlaceHolder
        assert tRef.usingGenerics
        assert tRef.name == Object.name
        assert tRef.genericsTypes[0].name == 'T'

        def v = node.genericsTypes[1]
        assert v.name == 'V'
        assert v.placeholder
        assert v.upperBounds[0].toString() == Object.name
    }

    void "test method object return type"() {
        def method = decompile().getDeclaredMethod("objectMethod")
        assert method.returnType.name == ClassNode.name
    }

    void "test method primitive array return type"() {
        def method = decompile().getDeclaredMethod("primitiveArrayMethod")
        assert method.returnType.array
        assert method.returnType.componentType.array
        assert method.returnType.componentType.componentType == ClassHelper.int_TYPE
    }

    void "test method parameters and exceptions"() {
        def method = decompile().getDeclaredMethods("withParametersThrowing")[0]

        assert method.parameters.length == 2
        assert method.parameters[0].type == ClassHelper.int_TYPE
        assert method.parameters[1].type.componentType.name == AsmDecompilerTestData.name

        assert method.exceptions.length == 1
        assert method.exceptions[0].name == IOException.name
    }

    void "test field"() {
        def field = decompile().getDeclaredField("aField")
        assert field.type.name == Object.name
    }

    void "test constructor"() {
        def constructor = decompile().getDeclaredConstructors()[0]
        assert constructor.parameters[0].type == ClassHelper.boolean_TYPE
    }

    void "test supers"() {
        def node = decompile()
        assert node.superClass.name == SuperClass.name
        assert !node.superClass.usingGenerics

        assert node.interfaces[0].name == Intf.name
        assert node.interfaces[0].usingGenerics

        def map = node.interfaces[0].genericsTypes[0]
        assert !map.placeholder
        assert map.name == Map.name
        assert !map.lowerBound
        assert !map.upperBounds

        assert node.interfaces[0].redirect().genericsTypes[0].name == 'S'

        def t = map.type.genericsTypes[0]
        assert t.placeholder
        assert t.name == 'T'
        assert t.type.usingGenerics

        def string = map.type.genericsTypes[1]
        assert string.name == String.name
        assert !string.type.usingGenerics
    }

    void "test simple class annotations"() {
        def node = decompile().annotations[0]

        assert node.classNode.name == Anno.name

        assert ((ConstantExpression) node.members.stringAttr).value == "s"
        assert !node.members.booleanAttr
        assert ((ClassExpression) node.members.clsAttr).type.name == String.name

        assert ((PropertyExpression) node.members.enumAttr).propertyAsString == 'BAR'
        assert ((PropertyExpression) node.members.enumAttr).objectExpression.type.name == SomeEnum.name
    }

    void "test member annotations"() {
        def node = decompile()
        assert node.getDeclaredField("aField").annotations[0].classNode.name == Anno.name
        assert node.getDeclaredMethod("objectMethod").annotations[0].classNode.name == Anno.name
        assert !node.getDeclaredMethods("withParametersThrowing")[0].annotations
    }

    void "test parameter annotations"() {
        def node = decompile()
        def params = node.getDeclaredMethods("withParametersThrowing")[0].parameters
        assert params[0].annotations.collect { it.classNode.name } == [Anno.name]
        assert !params[1].annotations
    }

    void "test primitive array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.intArrayAttr).expressions
        assert list.collect { ((ConstantExpression) it).value } == [4, 2]
    }

    void "test class array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.classArrayAttr).expressions
        assert list.collect { ((ClassExpression) it).type.name } == [AsmDecompilerTestData.name]
    }

    void "test annotation array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.annoArrayAttr).expressions
        assert list.size() == 2
        list.each {
            assert it.type.name == InnerAnno.name
            assert it instanceof AnnotationConstantExpression
        }

        def annotationNode = (AnnotationNode) ((AnnotationConstantExpression) list[1]).value
        assert ((ConstantExpression) annotationNode.members.booleanAttr).value == false
    }

    void "test annotation default method"() {
        def method = decompile(Anno.name).getDeclaredMethod('booleanAttr')
        assert method.hasAnnotationDefault()
        assert method.code
    }

    void "test annotation retention and targets"() {
        def anno = decompile().getAnnotations(decompile(Anno.name))[0]
        assert anno.hasRuntimeRetention()
        assert !anno.hasClassRetention()
        assert !anno.hasSourceRetention()

        assert anno.isTargetAllowed(AnnotationNode.METHOD_TARGET)
        assert anno.isTargetAllowed(AnnotationNode.TYPE_TARGET)
        assert !anno.isTargetAllowed(AnnotationNode.LOCAL_VARIABLE_TARGET)
    }

    static enum TestEnum {
        SOURCE, CLASS, RUNTIME
    }

    void "test enum field"() {
        def node = decompile(TestEnum.name).plainNodeReference
        for (s in ['SOURCE', 'CLASS', 'RUNTIME']) {
            def field = node.getDeclaredField(s)
            assert field
            assert field.type == node
        }
    }

    void "test generic method"() {
        def method = decompile().getDeclaredMethods("genericMethod")[0]

        assert method.genericsTypes.size() == 2
        assert method.genericsTypes[0].name == 'A'
        assert method.genericsTypes[1].name == 'B'
        assert method.genericsTypes[1].upperBounds[0].name == IOException.name

        def param1Type = method.parameters[0].type
        assert param1Type.genericsPlaceHolder
        assert param1Type.genericsTypes[0].name == 'A'
        assert param1Type.genericsTypes[0].placeholder

        def param2Type = method.parameters[1].type
        assert !param2Type.genericsPlaceHolder
        assert param2Type.array
        assert param2Type.componentType == ClassHelper.int_TYPE

        def exception = method.exceptions[0]
        assert exception.genericsPlaceHolder
        assert exception.genericsTypes[0].name == 'B'
        assert exception.genericsTypes[0].placeholder

        def ret = method.returnType
        assert ret.name == List.name
        assert ret.usingGenerics

        def wildcard = ret.genericsTypes[0]
        assert wildcard.wildcard
        assert wildcard.upperBounds[0].name == Object.name
    }

    void "test non-generic exceptions"() {
        def method = decompile().getDeclaredMethods("nonGenericExceptions")[0]
        assert method.exceptions[0].name == IOException.name
    }

    void "test non-generic parameters"() {
        def method = decompile().getDeclaredMethods("nonGenericParameters")[0]
        assert method.parameters[0].type == ClassHelper.boolean_TYPE
    }

    void "test generic field"() {
        def type = decompile().getDeclaredField("genericField").type
        assert type.name == List.name
        assert type.usingGenerics

        def tRef = type.genericsTypes[0].type
        assert tRef.genericsPlaceHolder
        assert tRef.usingGenerics
        assert tRef.name == Object.name
        assert tRef.genericsTypes[0].name == 'T'
    }

    void "test non-trivial erasure"() {
        def cls = decompile(NonTrivialErasure.name)

        def method = cls.getDeclaredMethods("method")[0]
        assert method.returnType.toString() == 'V -> java.lang.RuntimeException'
        assert method.parameters[0].type.toString() == 'V -> java.lang.RuntimeException'
        assert method.exceptions[0].toString() == 'V -> java.lang.RuntimeException'

        def field = cls.getDeclaredField("field")
        assert field.type.toString() == 'V -> java.lang.RuntimeException'
    }

    static class SomeInnerclass {}

    void "test static inner class"() {
        assert (decompile(SomeInnerclass.name).modifiers & Opcodes.ACC_STATIC) != 0
    }

    void "test static inner classes with same name"() {
        ClassNode cn = decompile(Groovy8632Abstract.Builder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) != 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) != 0

        cn = decompile(Groovy8632.Builder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) != 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) == 0

        cn = decompile(Groovy8632Groovy.Builder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) != 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) == 0
    }

    void "test static inner with dollar"() {
        assert (decompile(AsmDecompilerTestData.Inner$WithDollar.name).modifiers & Opcodes.ACC_STATIC) != 0
    }

    void "test inner classes with same name"() {
        ClassNode cn = decompile(Groovy8632Abstract.InnerBuilder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) == 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) != 0

        cn = decompile(Groovy8632.InnerBuilder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) == 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) == 0

        cn = decompile(Groovy8632Groovy.InnerBuilder.name)
        assert (cn.modifiers & Opcodes.ACC_STATIC) == 0
        assert (cn.modifiers & Opcodes.ACC_ABSTRACT) == 0
    }

    void "test private inner class"() {
        ClassNode cn = decompile(Groovy8632.InnerPrivate.name)
        assert (cn.modifiers & Opcodes.ACC_PRIVATE) != 0
        assert (cn.modifiers & Opcodes.ACC_PUBLIC) == 0
    }

    void "test protected inner class"() {
        ClassNode cn = decompile(Groovy8632.InnerProtected.name)
        assert (cn.modifiers & Opcodes.ACC_PROTECTED) != 0
        assert (cn.modifiers & Opcodes.ACC_PUBLIC) == 0
    }

    void "test non-parameterized generics"() {
        assert decompile().getDeclaredMethod("nonParameterizedGenerics").genericsTypes == null
    }

    void "test non-static parameterized inner"() {
        def asmType = decompile().getDeclaredMethod("returnInner").returnType
        def jvmType = new ClassNode(AsmDecompilerTestData).getDeclaredMethod("returnInner").returnType
        assert asmType == jvmType
        assert asmType.genericsTypes.collect { it.name } == jvmType.genericsTypes.collect { it.name }
    }

    private static ClassNode decompile(String cls = AsmDecompilerTestData.name) {
        def classFileName = cls.replace('.', '/') + '.class'
        def resource = AsmDecompilerTest.classLoader.getResource(classFileName)
        def stub = AsmDecompiler.parseClass(resource)

        def unit = new CompilationUnit(new GroovyClassLoader(AsmDecompilerTest.classLoader))
        return new DecompiledClassNode(stub, new AsmReferenceResolver(new ClassNodeResolver(), unit))
    }

}

