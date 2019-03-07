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
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.objectweb.asm.Opcodes

import junit.framework.TestCase
import org.codehaus.groovy.control.CompilePhase

/**
 * Tests the VariableExpressionNode
 */
class MethodNodeTest extends TestCase implements Opcodes {

    void testGetTextSimple() {
        def ast = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, false, '''

        def myMethod() {
        }
'''
        assert ast[1].@methods.get('myMethod')[0].text ==
                    'public java.lang.Object myMethod()  { ... }'
    }
    
    void testGetTextAdvanced() {
        def ast = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, false, '''

        private static final <T> T myMethod(String p1, int p2 = 1) throws Exception, IOException {
        }
'''
        assert ast[1].@methods.get('myMethod')[0].text ==
                    'private static final java.lang.Object myMethod(java.lang.String p1, int p2 = 1) throws java.lang.Exception, java.io.IOException { ... }'
    }

    void testIsDynamicReturnTypeExplicitObject() {
        def methodNode = new MethodNode('foo', ACC_PUBLIC, new ClassNode(Object.class), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
    }
    
    void testIsDynamicReturnTypeDYNAMIC_TYPE() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert methodNode.isDynamicReturnType()
    }
    
    void testIsDynamicReturnTypeVoid() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
    }
    
    void testIsDynamicReturnTypNull() {
        MethodNode methodNode = new MethodNode('foo', ACC_PUBLIC, null, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement())
        assert !methodNode.isDynamicReturnType()
        assertNotNull(methodNode.getReturnType())
    }
}
