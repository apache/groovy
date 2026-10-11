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
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.tools.WideningCategories
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

import static org.codehaus.groovy.ast.ClassHelper.*
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

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
