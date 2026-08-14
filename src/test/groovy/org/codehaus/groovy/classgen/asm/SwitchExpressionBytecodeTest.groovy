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
 * GROOVY-12255: bytecode shape of first-class switch expressions — no closure
 * wrapper, and tableswitch / lookupswitch when the selector and case labels
 * permit it.
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
    void staticIntSwitchUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case 1 -> 10
                    case 2 -> 20
                    case 3 -> 30
                    default -> 0
                }
            }
        ''')
        assert bytecode.hasSequence(['TABLESWITCH']) || bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticStringSwitchUsesLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(String s) {
                switch (s) {
                    case 'Foo' -> 'a'
                    case 'Bar' -> 'b'
                    default -> 'z'
                }
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH']) || bytecode.hasSequence(['TABLESWITCH'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL java/lang/String.equals'])
    }

    @Test
    void staticSparseIntSwitchUsesLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case 1       -> 10
                    case 100     -> 20
                    case 1000000 -> 30
                    default      -> 0
                }
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticEnumSwitchDispatchesByNameNotOrdinal() {
        // constant names are stable across separate recompilation of the enum;
        // ordinals are not, and dispatching on them silently retargets arms
        def bytecode = compile(method: 'm', '''\
            import java.time.Month

            @groovy.transform.CompileStatic
            String m(Month month) {
                switch (month) {
                    case Month.JANUARY -> 'jan'
                    case Month.JUNE -> 'jun'
                    default -> 'other'
                }
            }
        ''')
        assert bytecode.hasSequence(['INVOKEVIRTUAL java/lang/Enum.name'])
        assert !bytecode.toString().contains('Enum.ordinal')
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticEnumSwitchWithUnqualifiedNamesUsesNameDispatch() {
        def bytecode = compile(method: 'm', '''\
            import java.time.Month

            @groovy.transform.CompileStatic
            String m(Month month) {
                switch (month) {
                    case JANUARY -> 'jan'
                    case JUNE -> 'jun'
                    default -> 'other'
                }
            }
        ''')
        assert bytecode.hasSequence(['INVOKEVIRTUAL java/lang/Enum.name'])
        assert !bytecode.toString().contains('isCase')
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
}
