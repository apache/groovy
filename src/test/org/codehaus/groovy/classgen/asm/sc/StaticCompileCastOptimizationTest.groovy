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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * Unit tests for static compilation: DGM method calls.
 */
class StaticCompileCastOptimizationTest extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldOptimizeAsTypeToSimpleCast() {
        try {
            assertScript '''
                int x = 2
                long y = x as long // asType, where it could be a cast
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('I2L')
        }
    }

    void testShouldOptimizeCharToLongAsTypeToSimpleCast() {
        try {
            assertScript '''
                char x = 2
                long y = x as long // asType, where it could be a cast
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('I2L')
        }
    }

    void testShouldOptimizeLongToCharAsTypeToSimpleCast() {
        try {
            assertScript '''
                long x = 2L
                char y = x as char // asType, where it could be a cast
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('L2I') && bytecode.contains('I2C')
        }
    }

    void testShouldOptimizeListLiteralToArrayCast() {
        try {
            assertScript '''
                def x = ['a','b','c'] as String[]
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('ANEWARRAY java/lang/String')
        }
    }

    void testShouldOptimizeListLiteralToArrayCastWithIncompatibleElementType() {
        try {
            assertScript '''
                def x = ['a','b',new Date()] as String[]
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('ANEWARRAY java/lang/String')
        }
    }

    void testShouldOptimizeListLiteralToArrayCastThroughParameter() {
        try {
            assertScript '''
                int foo(String[] args) {
                    args.length
                }
                assert foo(['a','b',new Date()] as String[]) == 3
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /ShouldOptimize/ }.value[1]
            assert bytecode.contains('ANEWARRAY java/lang/String')
        }
    }
}

