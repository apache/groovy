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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Test

/**
 * Tests that the reflection path ({@code VMPlugin#configureClassNode}) ingests type-use
 * annotations from {@code java.lang.reflect.AnnotatedType}, mirroring the checks that
 * {@link AsmDecompilerTest} performs for the bytecode decompiler path (GROOVY-12206).
 * The same fixture classes from {@code AsmDecompilerTestData.java} are used, but here
 * they are loaded via reflection rather than decompiled from the class file.
 */
final class ReflectionTypeAnnotationsTest {

    private static final ClassNode node = ClassHelper.make(MoreTypeAnnotations)

    @Test
    void "type use annotations on member types"() {
        assert annos(node.getDeclaredField('annotatedField').type) == ['field']
        assert annos(node.getDeclaredField('genericField').type) == []
        assert annos(node.getDeclaredField('genericField').type.genericsTypes[0].type) == ['fieldArg']
        assert annos(node.getDeclaredField('nestedGenericField').type.genericsTypes[1].type.genericsTypes[0].type) == ['nested']

        assert annos(node.getDeclaredMethods('returnAnnotated')[0].returnType) == ['return']
        assert annos(node.getDeclaredMethods('returnGenericAnnotated')[0].returnType.genericsTypes[0].type) == ['returnArg']

        def params = node.getDeclaredMethods('params')[0].parameters
        assert annos(params[0].type) == ['p0']
        assert annos(params[1].type) == []
        assert annos(params[2].type.genericsTypes[0].type) == ['p2']

        def exceptions = node.getDeclaredMethods('throwsAnnotated')[0].exceptions
        assert annos(exceptions[0]) == []
        assert annos(exceptions[1]) == ['ex']

        assert annos(node.getDeclaredMethods('typeParamAnnotated')[0].genericsTypes[0].type) == ['mtp']
    }

    @Test
    void "type use annotations on array member types"() {
        def arrayType = node.getDeclaredField('arrayField').type
        assert arrayType.array
        assert annos(arrayType) == ['arr']
        assert annos(arrayType.componentType) == ['elem']

        def primitiveArrayType = node.getDeclaredField('primitiveArrayField').type
        assert annos(primitiveArrayType) == ['primArr']
        assert annos(primitiveArrayType.componentType) == []

        assert annos(node.getDeclaredField('genericArrayField').type.genericsTypes[0].type.componentType) == ['arrayArg']
    }

    @Test
    void "type use annotations on wildcard bounds"() {
        def upper = node.getDeclaredField('upperBoundField').type.genericsTypes[0]
        assert upper.wildcard
        assert annos(upper.upperBounds[0]) == ['upper']

        def lower = node.getDeclaredField('lowerBoundField').type.genericsTypes[0]
        assert lower.wildcard
        assert annos(lower.lowerBound) == ['lower']

        def wild = node.getDeclaredField('wildcardField').type.genericsTypes[0]
        assert wild.wildcard
        assert annos(wild.type) == ['wild']
    }

    @Test
    void "type use annotations on supertypes and type parameters"() {
        assert annos(node.unresolvedSuperClass) == ['super']
        def intf = node.interfaces[0]
        assert annos(intf) == ['intf']
        assert annos(intf.genericsTypes[0].type) == ['intfArg']
        assert annos(node.genericsTypes[0].type) == ['tp']

        // shared nodes must not pick up the per-use annotations
        assert ClassHelper.STRING_TYPE.typeAnnotations.isEmpty()
        assert ClassHelper.int_TYPE.typeAnnotations.isEmpty()
    }

    private static List<String> annos(ClassNode type) {
        type.typeAnnotations.collect { ((ConstantExpression) it.getMember('value')).text }
    }
}
