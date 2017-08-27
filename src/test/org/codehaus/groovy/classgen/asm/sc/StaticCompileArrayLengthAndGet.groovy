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

class StaticCompileArrayLengthAndGet extends AbstractBytecodeTestCase {
    void testShouldCompileArrayLengthStatically() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m(Object[] arr) {
                return arr.length
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD 1','ARRAYLENGTH']
        )
        def obj = clazz.newInstance()
        assert obj.m([4,5,6] as Object[]) == 3
    }

    void testArrayGet1() {
        if (config.indyEnabled) return;
        // this test is done with indy in another tests case
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m(int[] arr) {
                return arr[0]
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ICONST_0', 'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet', 'IRETURN']
        )
        def obj = clazz.newInstance()
        assert obj.m([4,5,6] as int[]) == 4

    }

    void testArraySet1() {
        if (config.indyEnabled) return;
        // this test is done with indy in another tests case
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            void m(int[] arr) {
                arr[0] = 666
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['SIPUSH 666','ISTORE','ALOAD','ICONST_0','ILOAD','INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet']
        )
        def obj = clazz.newInstance()
        int[] arr = [1,2,3]
        obj.m(arr)
        assert arr[0] == 666

    }
}
