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

/**
 * Static-compilation bytecode and runtime shape of switch <em>statements</em>
 * (GROOVY-12405): the same {@code tableswitch} / {@code lookupswitch} dispatch
 * a switch expression gets, with statement fall-through preserved, and the
 * sequential {@code isCase} chain everywhere else.
 */
final class SwitchStatementStaticCompileTest extends AbstractBytecodeTestCase {

    @Test
    void staticDenseIntStatementUsesTableSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(int n) {
                String r = 'none'
                switch (n) {
                    case 1: r = 'one'; break
                    case 2: r = 'two'; break
                    case 3: r = 'three'; break
                    default: r = 'many'
                }
                r
            }
        ''')
        assert bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticSparseIntStatementUsesLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(int n) {
                String r = 'none'
                switch (n) {
                    case -100:    r = 'a'; break
                    case 0:       r = 'b'; break
                    case 1000000: r = 'c'; break
                }
                r
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticStringStatementUsesLookupSwitch() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(String s) {
                String r = 'none'
                switch (s) {
                    case 'Foo': r = 'a'; break
                    case 'Bar': r = 'b'; break
                }
                r
            }
        ''')
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert bytecode.hasSequence(['INVOKEVIRTUAL java/lang/String.equals'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticEnumStatementDispatchesByName() {
        def bytecode = compile(method: 'm', '''\
            enum Color { RED, GREEN, BLUE }
            @groovy.transform.CompileStatic
            String m(Color c) {
                String r = 'none'
                switch (c) {
                    case Color.RED:   r = 'r'; break
                    case Color.GREEN: r = 'g'; break
                }
                r
            }
        ''')
        assert bytecode.hasSequence(['INVOKEVIRTUAL java/lang/Enum.name'])
        assert bytecode.hasSequence(['LOOKUPSWITCH'])
        assert !bytecode.toString().contains('isCase')
    }

    @Test
    void staticNonConstantLabelKeepsIsCase() {
        def bytecode = compile(method: 'm', '''\
            @groovy.transform.CompileStatic
            String m(Object o, Object label) {
                String r = 'none'
                switch (o) {
                    case label: r = 'hit'; break
                }
                r
            }
        ''')
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
    }

    @Test
    void duplicateLabelsKeepFirstMatchWins() {
        // a jump table cannot hold a repeated key, so the dispatcher declines
        // and the sequential chain is kept. Deliberately not statically
        // compiled: a duplicate constant label is a type-checking error
        // (GROOVY-12289 for expressions, GROOVY-12406 for statements), so the
        // guard only ever matters where that check has not run
        assertScript '''
            String m(int n) {
                String r = 'none'
                switch (n) {
                    case 1: r = 'first'; break
                    case 1: r = 'second'; break
                }
                r
            }
            assert m(1) == 'first'
            assert m(2) == 'none'
        '''
    }

    @Test
    void dispatchedStatementStillFallsThrough() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(int n) {
                def r = new StringBuilder()
                switch (n) {
                    case 1: r << 'a'
                    case 2: r << 'b'; break
                    case 3: r << 'c'
                    case 4: r << 'd'
                }
                r.toString()
            }
            assert m(1) == 'ab'
            assert m(2) == 'b'
            assert m(3) == 'cd'
            assert m(4) == 'd'
            assert m(5) == ''
        '''
    }

    @Test
    void dispatchedStatementHonoursEmptyCasesAndDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(int n) {
                String r = 'none'
                switch (n) {
                    case 1:
                    case 2: r = 'low'; break
                    case 3:
                    case 4: r = 'high'; break
                    default: r = 'other'
                }
                r
            }
            assert m(1) == 'low' && m(2) == 'low'
            assert m(3) == 'high' && m(4) == 'high'
            assert m(9) == 'other'
        '''
    }

    @Test
    void dispatchedStatementHonoursBreakContinueAndLabels() {
        assertScript '''
            @groovy.transform.CompileStatic
            String loop() {
                def r = new StringBuilder()
                for (int x = 0; x < 4; x++) {
                    switch (x) {
                        case 1: continue
                        case 2: r << 'two'; break
                        case 3: break
                        default: r << x
                    }
                    r << '.'
                }
                r.toString()
            }
            @groovy.transform.CompileStatic
            String labelled() {
                def r = new StringBuilder()
                outer: for (int x = 0; x < 3; x++) {
                    switch (x) {
                        case 1: break outer
                        default: r << x
                    }
                }
                r.toString()
            }
            assert loop() == '0.two..'
            assert labelled() == '0'
        '''
    }

    @Test
    void dispatchedStatementTakesDefaultForNullSelector() {
        assertScript '''
            enum Color { RED, GREEN }
            @groovy.transform.CompileStatic
            String forString(String s) {
                String r = 'none'
                switch (s) {
                    case 'a': r = 'A'; break
                    default: r = 'other'
                }
                r
            }
            @groovy.transform.CompileStatic
            String forEnum(Color c) {
                String r = 'none'
                switch (c) {
                    case Color.RED: r = 'r'; break
                }
                r
            }
            @groovy.transform.CompileStatic
            String forWrapper(Integer i) {
                String r = 'none'
                switch (i) {
                    case 1: r = 'one'; break
                }
                r
            }
            assert forString(null) == 'other'
            assert forEnum(null) == 'none'
            assert forWrapper(null) == 'none'
        '''
    }

    @Test
    void dispatchedStatementSupportsNarrowerIntegralSelectors() {
        assertScript '''
            @groovy.transform.CompileStatic
            String forChar(char c) {
                String r = 'none'
                switch (c) {
                    case 'a' as char: r = 'A'; break
                    case 'b' as char: r = 'B'; break
                }
                r
            }
            @groovy.transform.CompileStatic
            String forByte(byte b) {
                String r = 'none'
                switch (b) {
                    case 1: r = 'one'; break
                    case 2: r = 'two'; break
                }
                r
            }
            assert forChar('a' as char) == 'A' && forChar('z' as char) == 'none'
            assert forByte((byte) 2) == 'two' && forByte((byte) 9) == 'none'
        '''
    }

    @Test
    void dispatchedStatementHandlesHashCollisions() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(String s) {
                String r = 'none'
                switch (s) {
                    case 'Aa': r = 'x'; break
                    case 'BB': r = 'y'; break
                }
                r
            }
            assert 'Aa'.hashCode() == 'BB'.hashCode()
            assert m('Aa') == 'x'
            assert m('BB') == 'y'
            assert m('Cc') == 'none'
        '''
    }

    @Test
    void dispatchedStatementSupportsReturnAndThrowArms() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(int n) {
                switch (n) {
                    case 1: return 'one'
                    case 2: throw new IllegalStateException('two')
                }
                'none'
            }
            assert m(1) == 'one'
            assert m(3) == 'none'
            try { m(2); assert false } catch (IllegalStateException e) { assert e.message == 'two' }
        '''
    }

    @Test
    void dispatchedStatementNests() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(int i, int j) {
                String r = 'none'
                switch (i) {
                    case 1:
                        switch (j) {
                            case 9: r = 'nine'; break
                            default: r = 'other'
                        }
                        break
                    case 2: r = 'two'; break
                }
                r
            }
            assert m(1, 9) == 'nine'
            assert m(1, 5) == 'other'
            assert m(2, 0) == 'two'
            assert m(3, 0) == 'none'
        '''
    }

    @Test
    void dynamicStatementIsUnaffected() {
        def bytecode = compile(method: 'm', '''\
            String m(int n) {
                String r = 'none'
                switch (n) {
                    case 1: r = 'one'; break
                }
                r
            }
        ''')
        assert !bytecode.hasSequence(['TABLESWITCH'])
        assert !bytecode.hasSequence(['LOOKUPSWITCH'])
    }
}
