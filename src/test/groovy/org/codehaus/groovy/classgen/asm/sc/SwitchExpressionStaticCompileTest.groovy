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

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Static-compilation bytecode and runtime shape of first-class switch
 * expressions: tableswitch / lookupswitch when the selector and labels permit
 * it, and a resolved {@code isCase} otherwise.
 */
final class SwitchExpressionStaticCompileTest extends AbstractBytecodeTestCase {

    @Test
    void staticDenseIntSwitchUsesTableSwitch() {
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
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticStringSwitchUsesLookupSwitchThenTableSwitch() {
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
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert bytecode.hasSequence(['TABLESWITCH'])
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
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticEnumSwitchDispatchesByNameNotOrdinal() {
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
    void staticRangeLabelUsesIsCaseNotAdapterOnlyWhenResolved() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(int n) {
                switch (n) {
                    case 1        -> 'one'
                    case 300..400 -> 'range'
                    default       -> 'other'
                }
            }
        ''')
        assert bytecode.toString().contains('isCase')
    }

    @Test
    void staticReferenceSelectorWrittenByClosure() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m() {
                int x = 1
                def cl = { x = 3 }
                cl()
                switch (x) {
                    case 1 -> 10
                    case 3 -> 30
                    default -> 0
                }
            }
            assert m() == 30
        '''
    }

    @Test
    void staticIntegerReferenceSelectorWrittenByClosure() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m() {
                Integer x = 1
                def cl = { x = 3 }
                cl()
                switch (x) {
                    case 1 -> 10
                    case 3 -> 30
                    default -> 0
                }
            }
            assert m() == 30
        '''
    }

    @Test
    void staticNullIntegerSelectorUsesDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(Integer n) {
                switch (n) {
                    case 1 -> 'one'
                    case 2 -> 'two'
                    default -> 'dflt'
                }
            }
            assert m(1) == 'one'
            assert m(null) == 'dflt'
        '''
    }

    @Test
    void staticColonFallThroughSharesFollowingArm() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m(int n) {
                return switch (n) {
                    case 1:
                    case 2:
                        yield 12
                    case 3:
                        yield 3
                    default:
                        yield 0
                }
            }
            assert m(1) == 12
            assert m(2) == 12
            assert m(3) == 3
            assert m(4) == 0
        '''
    }

    @Test
    void staticIsCaseUsesMatchingInstanceOverload() {
        assertScript '''
            class Matcher {
                boolean isCase(String s) { s == 's' }
                boolean isCase(Integer n) { n == 2 }
            }

            @groovy.transform.CompileStatic
            String fromInt(Integer n) {
                switch (n) {
                    case new Matcher() -> 'hit'
                    default -> 'miss'
                }
            }

            @groovy.transform.CompileStatic
            String fromString(String s) {
                switch (s) {
                    case new Matcher() -> 'hit'
                    default -> 'miss'
                }
            }

            assert fromInt(2) == 'hit'
            assert fromInt(3) == 'miss'
            assert fromString('s') == 'hit'
            assert fromString('x') == 'miss'
        '''
    }

    @Test
    void staticEnumConstantWithClassBodyStillDispatchesByName() {
        assertScript '''
            @groovy.transform.CompileStatic
            enum Flag {
                ON { String desc() { 'on' } },
                OFF { String desc() { 'off' } }
                abstract String desc()
            }

            @groovy.transform.CompileStatic
            String m(Flag f) {
                switch (f) {
                    case Flag.ON -> 'on'
                    case Flag.OFF -> 'off'
                }
            }
            assert m(Flag.ON) == 'on'
            assert m(Flag.OFF) == 'off'
        '''
    }

    @Test
    void staticDuplicateIntCaseIsError() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case 1 -> 10
                    case 1 -> 20
                    default -> 0
                }
            }
        '''
        assert err.message.contains('Duplicate case label')
    }
}
