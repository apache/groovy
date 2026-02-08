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
package org.codehaus.groovy.ast

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.junit.Test

final class MethodNodeTest {

    private static ClassNode fromString(String source) {
        new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, source)[1]
    }

    @Test
    void testGetText1() {
        String result = fromString('def method(){}').getMethod('method').text
        assert result == 'public java.lang.Object method() { ... }'
    }

    @Test
    void testGetText2() {
        def script = fromString '''
            private static final <T extends Number> T method(String p1, int p2 = 1) throws Exception, IOException { }
        '''
        String result = script.getMethods('method')[0].text
        assert result == 'private static final <T extends java.lang.Number> T method(java.lang.String p1, int p2 = 1) throws java.lang.Exception, java.io.IOException { ... }'
    }

    @Test
    void testToString() {
        String result = new MethodNode('foo', 0, null, null, null, null)
        assert result.endsWith('[java.lang.Object foo()]')

        Parameter[] params = [new Parameter(ClassHelper.int_TYPE, 'i'), new Parameter(ClassHelper.int_TYPE.makeArray(), 'j')]
        result = new MethodNode('foo', 0, ClassHelper.STRING_TYPE.makeArray(), params, null, null).tap {
            declaringClass = ClassHelper.OBJECT_TYPE
        }
        assert result.endsWith('java.lang.String[] foo(int, int[]) from java.lang.Object]')
    }

    @Test // GROOVY-10862
    void testAnnotationDefault() {
        MethodNode mn = ClassHelper.OBJECT_TYPE.getMethod('toString')

        assert !mn.hasAnnotationDefault()
        assert mn.getCode() == null

        mn = ClassHelper.DEPRECATED_TYPE.getMethod('since')

        assert mn.hasAnnotationDefault()
        assert mn.getCode() != null
        mn.code.expression.with {
            assert type == ClassHelper.STRING_TYPE
            assert value == ''
        }
    }

    @Test
    void testIsDynamicReturnType1() {
        def methodNode = new MethodNode('foo', 0, ClassHelper.dynamicType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert methodNode.isDynamicReturnType()
    }

    @Test
    void testIsDynamicReturnType2() {
        def methodNode = new MethodNode('foo', 0, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert !methodNode.isDynamicReturnType()
    }

    @Test
    void testIsDynamicReturnType3() {
        def methodNode = new MethodNode('foo', 0, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert !methodNode.isDynamicReturnType()
    }

    @Test
    void testIsDynamicReturnType4() {
        def methodNode = new MethodNode('foo', 0, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert !methodNode.isDynamicReturnType()
        assert methodNode.returnType != null
    }
}
