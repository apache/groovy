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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test

/**
 * Dynamic bytecode shape of first-class switch expressions: sequential
 * {@code isCase} matching and no closure wrapper.
 */
final class SwitchExpressionBytecodeTest extends AbstractBytecodeTestCase {

    @Test
    void noClosureAllocationForSwitchExpression() {
        // Probe the current closure bytecode so the negative assertions
        // below stay meaningful if closure codegen changes (indy, naming).
        def closureBytecode = compile(method: 'run', '''\
            def c = { -> 'closure' }
            c()
        ''')
        def closureText = closureBytecode.toString()
        assert closureText.contains('$_run_closure')

        def switchBytecode = compile(method: 'run', '''\
            def r = switch (1) {
                case 1 -> 'a'
                default -> 'z'
            }
        ''')
        def switchText = switchBytecode.toString()
        assert !switchText.contains('$_run_closure')
        if (closureText.contains('InnerClassNode')) {
            assert !switchText.contains('InnerClassNode')
        }
    }

    @Test
    void dynamicSwitchUsesIsCase() {
        def bytecode = compile(method: 'run', '''\
            def x = 'abc'
            def r = switch (x) {
                case String -> 1
                default -> 2
            }
        ''')
        assert bytecode.toString().contains('isCase') || bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.isCase'])
    }

    @Test
    void dynamicIntSwitchDoesNotUseTableSwitch() {
        def bytecode = compile(method: 'run', '''\
            def r = switch (1) {
                case 1 -> 10
                case 2 -> 20
                default -> 0
            }
        ''')
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert bytecode.toString().contains('isCase') || bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.isCase'])
    }
}
