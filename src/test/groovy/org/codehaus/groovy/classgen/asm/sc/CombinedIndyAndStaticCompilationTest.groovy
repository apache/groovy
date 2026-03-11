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

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config
import static org.junit.Assume.assumeTrue

/**
 * Tests for combined static compilation and indy code.
 */
final class CombinedIndyAndStaticCompilationTest extends AbstractBytecodeTestCase {

    void testArrayRead() {
        assumeTrue(config.indyEnabled)

        for (String type : ['byte','short','int','long','float','double','boolean','char']) {
            def bytecode = compile(method:'test', """
            @groovy.transform.CompileStatic
            void test() {
                ${type}[] array = new ${type}[10]
                ${type} x = array[0]
            }
            """)
            int offset = bytecode.indexOf('--BEGIN--') + 4
            assert bytecode.indexOf('INVOKEDYNAMIC', offset) > offset
            assert bytecode.indexOf('INVOKEDYNAMIC', offset) < bytecode.indexOf('--END--')
        }
    }

    void testArrayWrite() {
        assumeTrue(config.indyEnabled)

        for (String type : ['byte','short','int','long','float','double','boolean','char']) {
            def bytecode = compile(method:'test', """
            @groovy.transform.CompileStatic
            void test() {
                ${type}[] array = new ${type}[10]
                array[0] = 1
            }
            """)
            int offset = bytecode.indexOf('--BEGIN--') + 4
            assert bytecode.indexOf('INVOKEDYNAMIC', offset) > offset
            assert bytecode.indexOf('INVOKEDYNAMIC', offset) < bytecode.indexOf('--END--')
        }
    }

    void testNegativeIndex() {
        for (String type : ['byte','short','int','long','float','double','char']) {
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
    void testCompileStaticAndDefaultParameter() {
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
}
