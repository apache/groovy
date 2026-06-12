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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

import static org.codehaus.groovy.ast.ClassHelper.*
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound

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
}
