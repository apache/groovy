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
package org.codehaus.groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

import static org.codehaus.groovy.ast.ClassHelper.*
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor.TYPECHECKING_INFO_NODE
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_RETURN_TYPE

/**
 * Unit tests for signature codecs.
 */
final class SignatureCodecTest {

    private SignatureCodec codec = StaticTypeCheckingVisitor.SignatureCodecFactory.getCodec(1, this.class.classLoader)

    private static final List<ClassNode> types = [
        OBJECT_TYPE, STRING_TYPE, GSTRING_TYPE,
        int_TYPE, float_TYPE, double_TYPE, Number_TYPE,
        MAP_TYPE, LIST_TYPE, RANGE_TYPE, PATTERN_TYPE, SCRIPT_TYPE
    ]

    @ParameterizedTest @FieldSource('types')
    void testVariousSimpleClassNodes(ClassNode cn) {
        String signature = codec.encode(cn)
        assert codec.decode(signature) == cn
    }

    @Test
    void testCodecWithGenericType() {
        ClassNode cn = LIST_TYPE.getPlainNodeReference()
        cn.setGenericsTypes(new GenericsType(STRING_TYPE))
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
        assert decoded.genericsTypes[0].type == STRING_TYPE
    }

    @Test
    void testCodecWithLUBoundType() {
        ClassNode cn = lowestUpperBound([LIST_TYPE, COMPARABLE_TYPE])
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
    }

    // GROOVY-12445
    @Test
    void testUnknownVersionYieldsDeclaredReturnTypeWithoutReadingBytes() {
        def loader = this.class.classLoader
        def factory = StaticTypeCheckingVisitor.SignatureCodecFactory
        assert factory.getCodec(0, loader) == null
        assert factory.getCodec(2, loader) == null
        assert factory.getCodec(-1, loader) == null
        assert factory.getCodec(Integer.MAX_VALUE, loader) == null
        assert factory.getCodec(1, loader) != null

        ClassNode declared = STRING_TYPE
        ClassNode inferred = factory.inferredReturnOrDeclared(2, 'this is not base64', loader, declared)
        assert inferred === declared
        assert factory.inferredReturnOrDeclared(0, 'this is not base64', loader, declared) === declared
        assert factory.inferredReturnOrDeclared(-1, 'this is not base64', loader, declared) === declared

        String encoded = codec.encode(STRING_TYPE)
        ClassNode fromVersion1 = factory.inferredReturnOrDeclared(1, encoded, loader, OBJECT_TYPE)
        assert fromVersion1 == STRING_TYPE
        assert fromVersion1 !== OBJECT_TYPE
    }

    // GROOVY-12445
    @Test
    void testAddTypeCheckingInfoAnnotationSkipsUnknownVersionWithoutEncoding() {
        def loader = this.class.classLoader
        def visitor = typeCheckingVisitor()
        MethodNode method = methodWithInferredReturn(STRING_TYPE)

        visitor.addTypeCheckingInfoAnnotation(method, 2, loader)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()

        visitor.addTypeCheckingInfoAnnotation(method, 0, loader)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()

        visitor.addTypeCheckingInfoAnnotation(method, -1, loader)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()

        visitor.addTypeCheckingInfoAnnotation(method, (SignatureCodec) null)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()

        def nullEncode = new SignatureCodec() {
            @Override String encode(ClassNode node) { null }
            @Override ClassNode decode(String signature) { null }
        }
        visitor.addTypeCheckingInfoAnnotation(method, nullEncode)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()
    }

    // GROOVY-12445
    @Test
    void testAddTypeCheckingInfoAnnotationWritesVersion1Payload() {
        def loader = this.class.classLoader
        def visitor = typeCheckingVisitor()
        MethodNode method = methodWithInferredReturn(STRING_TYPE)

        visitor.addTypeCheckingInfoAnnotation(method)
        def annotations = method.getAnnotations(TYPECHECKING_INFO_NODE)
        assert annotations.size() == 1
        assert annotations[0].getMember('version').value == 1
        String encoded = annotations[0].getMember('inferredType').value
        assert StaticTypeCheckingVisitor.SignatureCodecFactory.inferredReturnOrDeclared(1, encoded, loader, OBJECT_TYPE) == STRING_TYPE

        visitor.addTypeCheckingInfoAnnotation(method)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).size() == 1
    }

    // GROOVY-12445
    @Test
    void testAddTypeCheckingInfoAnnotationSkipsConstructorAndMissingInferredType() {
        def loader = this.class.classLoader
        def visitor = typeCheckingVisitor()

        def ctor = new ConstructorNode(0, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        ctor.putNodeMetaData(INFERRED_RETURN_TYPE, STRING_TYPE)
        visitor.addTypeCheckingInfoAnnotation(ctor, 1, loader)
        assert ctor.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()

        MethodNode method = new MethodNode('m', 0, STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        visitor.addTypeCheckingInfoAnnotation(method, 1, loader)
        assert method.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()
    }

    @Test
    void testCodecWithUnionType() {
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, LIST_TYPE)
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
    }

    @Test
    void testCodecWithUnionTypeAndGenerics() {
        ClassNode list = LIST_TYPE.getPlainNodeReference()
        list.setGenericsTypes(new GenericsType(STRING_TYPE))
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, list)
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
        assert decoded.delegates[1] == LIST_TYPE
        assert decoded.delegates[1].genericsTypes[0].type == STRING_TYPE
    }

    private static StaticTypeCheckingVisitor typeCheckingVisitor() {
        SourceUnit source = SourceUnit.create('Dummy.groovy', '')
        ClassNode cn = new ClassNode('Dummy', 0, OBJECT_TYPE)
        new StaticTypeCheckingVisitor(source, cn)
    }

    private static MethodNode methodWithInferredReturn(ClassNode inferred) {
        MethodNode method = new MethodNode('m', 0, STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        method.putNodeMetaData(INFERRED_RETURN_TYPE, inferred)
        return method
    }
}
