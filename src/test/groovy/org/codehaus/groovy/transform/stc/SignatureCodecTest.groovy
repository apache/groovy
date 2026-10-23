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
import org.codehaus.groovy.ast.tools.WideningCategories
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

import static org.codehaus.groovy.ast.ClassHelper.COMPARABLE_TYPE
import static org.codehaus.groovy.ast.ClassHelper.GSTRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.PATTERN_TYPE
import static org.codehaus.groovy.ast.ClassHelper.RANGE_TYPE
import static org.codehaus.groovy.ast.ClassHelper.SCRIPT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching
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
    void testCodecWithUnionTypeWritesStableBound() {
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, LIST_TYPE)
        String signature = codec.encode(cn)
        assertFirstTagIsNotUnion(signature)
        ClassNode decoded = codec.decode(signature)
        assert decoded == OBJECT_TYPE
        assert !(decoded instanceof UnionTypeClassNode)
    }

    @Test
    void testCodecWithUnionTypeWritesCommonSuperclass() {
        ClassNode cn = new UnionTypeClassNode(make(RuntimeException), make(Exception))
        String signature = codec.encode(cn)
        assertFirstTagIsNotUnion(signature)
        ClassNode decoded = codec.decode(signature)
        assert decoded == make(Exception)
        assert !(decoded instanceof UnionTypeClassNode)
    }

    @Test
    void testCodecWithUnionTypeAndGenericsDoesNotLeakUnion() {
        ClassNode list = LIST_TYPE.getPlainNodeReference()
        list.setGenericsTypes(new GenericsType(STRING_TYPE))
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, list)
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert !(decoded instanceof UnionTypeClassNode)
        assert !decoded.name.startsWith('<UnionType:')
    }

    @Test
    void testCodecWithUnionTypeWritesSingleSharedInterface() {
        ClassNode marker = make(SharedMarker)
        ClassNode left = make('stc.MarkerLeft')
        left.interfaces = [marker] as ClassNode[]
        ClassNode right = make('stc.MarkerRight')
        right.interfaces = [marker] as ClassNode[]
        ClassNode cn = new UnionTypeClassNode(left, right)
        String signature = codec.encode(cn)
        assertFirstTagIsNotUnion(signature)
        ClassNode decoded = codec.decode(signature)
        assert decoded == marker
        assert decoded != OBJECT_TYPE
    }

    @Test
    void testHistoricalUnionPayloadWidensToObjectForDisjointTypes() {
        String signature = historicalUnionPayload(STRING_TYPE, LIST_TYPE)
        ClassNode decoded = codec.decode(signature)
        assert decoded == OBJECT_TYPE
        assert !(decoded instanceof UnionTypeClassNode)
    }

    @Test
    void testHistoricalUnionPayloadWidensToCommonSuperclass() {
        String signature = historicalUnionPayload(make(RuntimeException), make(Exception))
        ClassNode decoded = codec.decode(signature)
        assert decoded == make(Exception)
        assert !(decoded instanceof UnionTypeClassNode)
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

    @Test
    void testHistoricalUnionPayloadWithEmptyDelegateCountIsObject() {
        def baos = new ByteArrayOutputStream()
        def dos = new DataOutputStream(baos)
        dos.writeUTF('UnionTypeClassNode')
        dos.writeInt(0)
        dos.flush()
        String signature = EncodingGroovyMethods.encodeBase64(baos.toByteArray()).toString()
        assert codec.decode(signature) == OBJECT_TYPE
    }

    @Test
    void testHistoricalUnionPayloadWithNegativeDelegateCountIsObject() {
        def baos = new ByteArrayOutputStream()
        def dos = new DataOutputStream(baos)
        dos.writeUTF('UnionTypeClassNode')
        dos.writeInt(-1)
        dos.flush()
        String signature = EncodingGroovyMethods.encodeBase64(baos.toByteArray()).toString()
        assert codec.decode(signature) == OBJECT_TYPE
    }

    @Test
    void testCodecWithEmptyUnionDelegatesWritesObject() {
        assert codec.decode(codec.encode(withDelegates(ClassNode.EMPTY_ARRAY))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithNullUnionDelegatesWritesObject() {
        assert codec.decode(codec.encode(withDelegates(null))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithNestedUnionWritesStableBound() {
        ClassNode inner = new UnionTypeClassNode(STRING_TYPE, LIST_TYPE)
        ClassNode outer = new UnionTypeClassNode(inner)
        String signature = codec.encode(outer)
        assertFirstTagIsNotUnion(signature)
        ClassNode decoded = codec.decode(signature)
        assert decoded == OBJECT_TYPE
        assert !(decoded instanceof UnionTypeClassNode)
    }

    @Test
    void testCodecWithUnionWhoseLubIsNullWritesObject() {
        assert codec.decode(codec.encode(withDelegates([STRING_TYPE, null] as ClassNode[]))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithUnionOfLubWithObjectSuperWritesObject() {
        ClassNode lub = new WideningCategories.LowestUpperBoundClassNode(
            'Virtual$Object', OBJECT_TYPE, make(SharedMarker), make(SecondMarker))
        assert codec.decode(codec.encode(new UnionTypeClassNode(lub))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithUnionOfLubWithNamedSuperWritesSuper() {
        ClassNode extra = make(SharedMarker)
        ClassNode base = make(Exception)
        ClassNode lub = new WideningCategories.LowestUpperBoundClassNode('Virtual$Ex', base, extra)
        assert codec.decode(codec.encode(new UnionTypeClassNode(lub))) == base
    }

    @Test
    void testCodecWithUnionOfLubWithNullSuperWritesObject() {
        ClassNode lub = new WideningCategories.LowestUpperBoundClassNode('Virtual$Null', OBJECT_TYPE) {
            @Override
            ClassNode getUnresolvedSuperClass(boolean deref) {
                null
            }
        }
        assert codec.decode(codec.encode(new UnionTypeClassNode(lub))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithUnionOfTypesSharingSuperclassAndInterfaceWritesSuper() {
        ClassNode extra = make(SharedMarker)
        ClassNode base = make(Exception)
        ClassNode left = makeWithoutCaching('stc.LubLeft')
        left.superClass = base
        left.interfaces = [extra] as ClassNode[]
        ClassNode right = makeWithoutCaching('stc.LubRight')
        right.superClass = base
        right.interfaces = [extra] as ClassNode[]
        assert lowestUpperBound([left, right]) instanceof WideningCategories.LowestUpperBoundClassNode
        ClassNode decoded = codec.decode(codec.encode(new UnionTypeClassNode(left, right)))
        assert decoded == base
        assert !(decoded instanceof UnionTypeClassNode)
        assert !(decoded instanceof WideningCategories.LowestUpperBoundClassNode)
    }

    @Test
    void testCodecWithSingleArmUnionWritesThatType() {
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE)
        String signature = codec.encode(cn)
        assertFirstTagIsNotUnion(signature)
        assert codec.decode(signature) == STRING_TYPE
    }

    @Test
    void testCodecWithUnionOfPlaceholderNamedTypeWritesObject() {
        ClassNode placeholder = makeWithoutCaching('<T>')
        assert codec.decode(codec.encode(new UnionTypeClassNode(placeholder))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithUnionOfEmptyNamedTypeWritesObject() {
        ClassNode empty = makeWithoutCaching('')
        assert codec.decode(codec.encode(new UnionTypeClassNode(empty))) == OBJECT_TYPE
    }

    @Test
    void testCodecWithUnionOfNullNamedTypeWritesObject() {
        ClassNode unnamed = makeWithoutCaching('stc.Unnamed')
        ClassNode cn = new UnionTypeClassNode(unnamed)
        unnamed.setName(null)
        assert codec.decode(codec.encode(cn)) == OBJECT_TYPE
    }

    private static void assertFirstTagIsNotUnion(String signature) {
        byte[] bytes = EncodingGroovyMethods.decodeBase64(signature)
        def dis = new DataInputStream(new ByteArrayInputStream(bytes))
        assert dis.readUTF() != 'UnionTypeClassNode'
        assert !new String(bytes, 'ISO-8859-1').contains('UnionTypeClassNode')
    }

    private String historicalUnionPayload(ClassNode... arms) {
        def baos = new ByteArrayOutputStream()
        def dos = new DataOutputStream(baos)
        dos.writeUTF('UnionTypeClassNode')
        dos.writeInt(arms.length)
        for (ClassNode arm : arms) {
            dos.write(EncodingGroovyMethods.decodeBase64(codec.encode(arm)))
        }
        dos.flush()
        EncodingGroovyMethods.encodeBase64(baos.toByteArray()).toString()
    }

    private static UnionTypeClassNode withDelegates(ClassNode[] arms) {
        new UnionTypeClassNode(STRING_TYPE) {
            @Override
            ClassNode[] getDelegates() {
                arms
            }
        }
    }

    interface SharedMarker {}

    interface SecondMarker {}
}
