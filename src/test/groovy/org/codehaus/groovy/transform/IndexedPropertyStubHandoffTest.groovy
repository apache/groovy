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
package org.codehaus.groovy.transform

import groovy.transform.IndexedProperty
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.transform.ImmutableASTTransformation.IMMUTABLE_BREADCRUMB
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

final class IndexedPropertyStubHandoffTest {

    // GROOVY-12726
    // The setter placeholder is installed on a hand-built AST. The joint-compilation
    // test does not produce that setter on the stub, so this is the case that shows
    // the transform removing it.
    @Test
    void testImmutableFieldDropsPlaceholderSetter() {
        assertPlaceholderSetterRemoved(listOf(STRING_TYPE), 'items', 'setItems', 'getItems', STRING_TYPE)
        assertPlaceholderSetterRemoved(STRING_TYPE.makeArray(), 'tags', 'setTags', 'getTags', STRING_TYPE)
    }

    @Test
    void testTransformIgnoresForeignAnnotationAndNonField() {
        ClassNode type = new ClassNode('Box', ACC_PUBLIC, OBJECT_TYPE)
        SourceUnit source = sourceUnit()
        new IndexedPropertyASTTransformation().visit(
                [new AnnotationNode(ClassHelper.make(Override)), type] as ASTNode[], source)
        new IndexedPropertyASTTransformation().visit(
                [annotation(), type] as ASTNode[], source)
        assert type.methods.empty
        assert !source.errorCollector.hasErrors()
    }

    @Test
    void testStubberSkipsUnusableFieldsAndIsIdempotent() {
        IndexedPropertyASTStubber stubber = new IndexedPropertyASTStubber()
        ClassNode ignored = new ClassNode('Ignored', ACC_PUBLIC, OBJECT_TYPE)
        stubber.visit([new AnnotationNode(ClassHelper.make(Override)), propertyField(ignored, 'items', listOf(STRING_TYPE))] as ASTNode[], null)
        assert ignored.getDeclaredMethod('getItems', params(param(int_TYPE, 'index'))) == null

        stubber.visit([annotation(), new MethodNode('foo', ACC_PUBLIC, VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)] as ASTNode[], null)
        stubber.visit([annotation(), new FieldNode('items', ACC_PUBLIC, listOf(STRING_TYPE), null, null)] as ASTNode[], null)

        ClassNode plain = new ClassNode('Plain', ACC_PUBLIC, OBJECT_TYPE)
        FieldNode bare = new FieldNode('items', ACC_PUBLIC, listOf(STRING_TYPE), plain, null)
        plain.addField(bare)
        stubber.visit([annotation(), bare] as ASTNode[], null)
        assert plain.getDeclaredMethod('getItems', params(param(int_TYPE, 'index'))) == null

        ClassNode text = new ClassNode('Text', ACC_PUBLIC, OBJECT_TYPE)
        stubber.visit([annotation(), propertyField(text, 'name', STRING_TYPE)] as ASTNode[], null)
        assert text.getDeclaredMethod('getName', params(param(int_TYPE, 'index'))) == null

        ClassNode box = new ClassNode('Box', ACC_PUBLIC, OBJECT_TYPE)
        FieldNode items = propertyField(box, 'items', listOf(STRING_TYPE))
        stubber.visit([annotation(), items] as ASTNode[], null)
        stubber.visit([annotation(), items] as ASTNode[], null)
        assert box.methods.count { it.name == 'getItems' } == 1
        assert box.methods.count { it.name == 'setItems' } == 1
    }

    private static void assertPlaceholderSetterRemoved(ClassNode fieldType, String fieldName, String setter, String getter, ClassNode component) {
        ClassNode type = new ClassNode(fieldName.capitalize(), ACC_PUBLIC, OBJECT_TYPE)
        FieldNode field = propertyField(type, fieldName, fieldType)
        field.setNodeMetaData(IMMUTABLE_BREADCRUMB, Boolean.TRUE)
        Parameter[] index = params(param(int_TYPE, 'index'))
        Parameter[] setterParams = params(param(int_TYPE, 'index'), param(component, 'value'))
        StubberSupport.addStubMethod(type, getter, ACC_PUBLIC, component, index, ClassNode.EMPTY_ARRAY, returnS(constX(null)))
        StubberSupport.addStubMethod(type, setter, ACC_PUBLIC, VOID_TYPE, setterParams, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)

        SourceUnit source = sourceUnit()
        new IndexedPropertyASTTransformation().visit([annotation(), field] as ASTNode[], source)

        assert type.getDeclaredMethod(setter, setterParams) == null
        MethodNode getterNode = type.getDeclaredMethod(getter, params(param(int_TYPE, 'index')))
        assert getterNode != null
        assert !StubberSupport.isStub(getterNode)
        assert !source.errorCollector.hasErrors()
    }

    @Test
    void testListComponentTypeFallsBackForRawAndMultiParameterLists() {
        ClassNode raw = ClassHelper.LIST_TYPE.getPlainNodeReference()
        assert IndexedPropertyASTTransformation.getComponentTypeForList(raw) == OBJECT_TYPE

        ClassNode multiple = ClassHelper.LIST_TYPE.getPlainNodeReference()
        multiple.setGenericsTypes(new GenericsType[] {new GenericsType(STRING_TYPE), new GenericsType(int_TYPE)})
        assert IndexedPropertyASTTransformation.getComponentTypeForList(multiple) == OBJECT_TYPE

        assert IndexedPropertyASTTransformation.getComponentTypeForList(listOf(STRING_TYPE)) == STRING_TYPE
    }

    private static FieldNode propertyField(ClassNode type, String name, ClassNode fieldType) {
        type.addProperty(name, ACC_PUBLIC, fieldType, null, null, null)
        type.getField(name)
    }

    private static ClassNode listOf(ClassNode component) {
        ClassNode list = ClassHelper.LIST_TYPE.getPlainNodeReference()
        list.setGenericsTypes(new GenericsType[] {new GenericsType(component)})
        list
    }

    private static AnnotationNode annotation() {
        new AnnotationNode(ClassHelper.make(IndexedProperty))
    }

    private static SourceUnit sourceUnit() {
        CompilerConfiguration config = new CompilerConfiguration()
        new SourceUnit('Box.groovy', 'class Box {}', config, new GroovyClassLoader(), new ErrorCollector(config))
    }
}
