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
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.hasSequence(['TABLESWITCH'])
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
        assert bytecode.toString().contains('DefaultGroovyMethods')
        assert !bytecode.toString().contains('ScriptBytecodeAdapter.isCase')
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
    void staticCommaArrowLabelsShareOneArm() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case 6, 8, 10 -> 3
                    default -> 0
                }
            }
            assert m(6) == 3
            assert m(8) == 3
            assert m(10) == 3
            assert m(7) == 0
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
    void staticNullCaseLabelUsesRuntimeIsCase() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(Object o) {
                switch (o) {
                    case null -> 'n'
                    default -> 'd'
                }
            }
            assert m(null) == 'n'
            assert m(1) == 'd'
        '''
    }

    @Test
    void staticFlowTypedClassLabelSelectsClassIsCase() {
        // `Object label = String` is flow-typed to Class, so method selection
        // binds DGM.isCase(Class, Object) rather than equals.
        assertScript '''
            @groovy.transform.CompileStatic
            String m(Object o) {
                Object label = String
                switch (o) {
                    case label -> 's'
                    default -> 'd'
                }
            }
            assert m('x') == 's'
            assert m(1) == 'd'
        '''
    }

    @Test
    void staticDeclaredObjectLabelUsesEqualsIsCase() {
        // A parameter whose static type is Object has no more precise flow type,
        // so isCase is DGM.isCase(Object, Object) — equals — matching `label.isCase(o)`.
        assertScript '''
            @groovy.transform.CompileStatic
            String m(Object o, Object label) {
                switch (o) {
                    case label -> 's'
                    default -> 'd'
                }
            }
            assert m('x', String) == 'd'
            assert m(String, String) == 's'
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
    void staticListGetAtStringSelectorStillDispatches() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(List<String> names) {
                switch (names[0]) {
                    case 'Foo' -> 'a'
                    default    -> 'z'
                }
            }
            assert m(['Foo']) == 'a'
            assert m(['Bar']) == 'z'
        '''
    }

    @Test
    void staticListGetAtIntegerSelectorStillDispatches() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m(List<Integer> nums) {
                switch (nums[0]) {
                    case 1 -> 10
                    default -> 0
                }
            }
            assert m([1]) == 10
            assert m([2]) == 0
        '''
    }

    @Test
    void staticMapGetAtStringSelectorStillDispatches() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(Map<String, String> names) {
                switch (names['k']) {
                    case 'Foo' -> 'a'
                    default    -> 'z'
                }
            }
            assert m([k: 'Foo']) == 'a'
            assert m([k: 'Bar']) == 'z'
        '''
    }

    @Test
    void staticNullLabelOnPrimitiveSelectorNeverMatches() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(int n) {
                switch (n) {
                    case null -> 'n'
                    case 1    -> 'one'
                    default   -> 'd'
                }
            }
            assert m(1) == 'one'
            assert m(0) == 'd'
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
    void staticCommaArrowDenseIntUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case 6, 8, 10 -> 3
                    default -> 0
                }
            }
        ''')
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
        assert findUnreachableInstructions(classBytes, 'm') == []
    }

    @Test
    void staticColonFallThroughDenseIntUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
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
        ''')
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
    }

    @Test
    void staticNegativeDenseIntUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case -1 -> -10
                    case  0 -> 0
                    case  1 -> 10
                    default -> 99
                }
            }
        ''')
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
    }

    @Test
    void staticWideIntSpanUsesLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                switch (n) {
                    case -2000000000 -> -1
                    case  2000000000 -> 1
                    default -> 0
                }
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.hasSequence(['TABLESWITCH'])
    }

    @Test
    void staticManyStringCasesStillAvoidTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(String s) {
                switch (s) {
                    case 'a' -> 'A'
                    case 'b' -> 'B'
                    case 'c' -> 'C'
                    case 'd' -> 'D'
                    case 'e' -> 'E'
                    case 'f' -> 'F'
                    case 'g' -> 'G'
                    case 'h' -> 'H'
                    default  -> 'z'
                }
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.hasSequence(['TABLESWITCH'])
    }

    @Test
    void staticStringHashCollisionUsesSingleLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(String s) {
                switch (s) {
                    case 'Aa' -> 'first'
                    case 'BB' -> 'second'
                    default   -> 'none'
                }
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert bytecode.instructions.count { it.startsWith('INVOKEVIRTUAL java/lang/String.equals') } == 2
    }

    @Test
    void staticColonEmptyCaseFallsIntoDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m(int n) {
                return switch (n) {
                    case 1:
                    case 2:
                    default:
                        yield 0
                }
            }
            assert m(1) == 0
            assert m(2) == 0
            assert m(3) == 0
        '''
    }

    @Test
    void staticColonEmptyCaseThenDefaultUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            int m(int n) {
                return switch (n) {
                    case 1:
                    default:
                        yield 0
                }
            }
        ''')
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
    }

    @Test
    void staticColonArmIfFallsThroughToCompletingDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m(int n, boolean cond) {
                return switch (n) {
                    case 1:
                        if (cond) yield 10
                    default:
                        yield 0
                }
            }
            assert m(1, true) == 10
            assert m(1, false) == 0
            assert m(2, false) == 0
        '''
    }

    @Test
    void staticStringColonEmptyCaseFallsIntoDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(String s) {
                return switch (s) {
                    case 'a':
                    default:
                        yield 'd'
                }
            }
            assert m('a') == 'd'
            assert m('b') == 'd'
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
