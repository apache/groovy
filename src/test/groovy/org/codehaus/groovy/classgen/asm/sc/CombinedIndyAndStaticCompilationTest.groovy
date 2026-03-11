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

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config
import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Tests for combined static compilation and indy code.
 */
final class CombinedIndyAndStaticCompilationTest extends AbstractBytecodeTestCase {

    @ParameterizedTest
    @ValueSource(strings=['byte','short','int','long','float','double','boolean','char'])
    void testArrayRead(String type) {
        assumeTrue config.indyEnabled

        def bytecode = compile method:'test', """
            @groovy.transform.CompileStatic
            void test() {
                ${type}[] array = new ${type}[10]
                ${type} x = array[0]
            }
        """
        int offset = bytecode.indexOf('--BEGIN--') + 4
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) > offset
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) < bytecode.indexOf('--END--')
    }

    @ParameterizedTest
    @ValueSource(strings=['byte','short','int','long','float','double','boolean','char'])
    void testArrayWrite(String type) {
        assumeTrue config.indyEnabled

        def bytecode = compile method:'test', """
            @groovy.transform.CompileStatic
            void test() {
                ${type}[] array = new ${type}[10]
                array[0] = 1
            }
        """
        int offset = bytecode.indexOf('--BEGIN--') + 4
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) > offset
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) < bytecode.indexOf('--END--')
    }

    @ParameterizedTest
    @ValueSource(strings=['byte','short','int','long','float','double','char'])
    void testNegativeIndex(String type) {
        assertScript """
            @groovy.transform.CompileStatic
            void test() {
                ${type}[] array = [0,1,2]
                assert array[0] == 0
                assert array[1] == 1
                assert array[2] == 2
                assert array[-1] == 2
                assert array[-2] == 1
                array[0] = 9
                assert array[0] == 9
                array[-1] = 8
                assert array[2] == 8
            }
            test()
        """
    }

    @Test
    void testNegativeIndexPrimitiveBoolean() {
        assertScript '''
            @groovy.transform.CompileStatic
            void test() {
                boolean[] array = [false, false, true]
                assert array[0] == false
                assert array[1] == false
                assert array[2] == true
                assert array[-1] == true
                assert array[-2] == false
                array[0] = true
                assert array[0] == true
                array[-1] = false
                assert array[2] == false
            }
            test()
        '''
    }

    // GROOVY-11872
    @Test
    void testCompileStaticAndMethodWithDefaultParameter() {
        def bytecode = compile '''
            class Foo {
                @groovy.transform.CompileStatic
                void bar(List list = baz()) {
                    for (item in list) {
                        println item
                    }
                }
                List baz() {
                    ['fizz','buzz']
                }
            }
        '''
        int offset = bytecode.indexOf('public bar()')
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) < 0
        assert bytecode.indexOf('INVOKEVIRTUAL', offset) > offset
        assert bytecode.indexOf('INVOKEVIRTUAL', offset) < bytecode.indexOf('RETURN', offset)
    }

    // GROOVY-11872
    @Test
    void testCompileStaticAndConstructorWithDefaultParameter() {
        def bytecode = compile '''
            class Foo {
                @groovy.transform.CompileStatic
                Foo(List list = bar()) {
                    for (item in list) {
                        println item
                    }
                }
                static List bar() {
                    ['fizz','buzz']
                }
            }
        '''
        int offset = bytecode.indexOf('public <init>()')
        assert bytecode.indexOf('INVOKEDYNAMIC', offset) < 0
        assert bytecode.indexOf('INVOKESTATIC ', offset) > offset
        assert bytecode.indexOf('INVOKESTATIC ', offset) < bytecode.indexOf('RETURN', offset)
    }
}
