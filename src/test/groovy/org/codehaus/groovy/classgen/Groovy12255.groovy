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
package org.codehaus.groovy.classgen

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12255: first-class switch expressions (JEP 361) for dynamic and static Groovy.
 * Compiles as {@code SwitchExpression} / {@code YieldStatement}, not as a
 * closure wrapping a switch statement.
 */
final class Groovy12255 {

    @Test
    void arrowExpressionArms() {
        assertScript '''
            def letter = switch (2) {
                case 1 -> 'a'
                case 2 -> 'b'
                default -> 'z'
            }
            assert letter == 'b'
        '''
    }

    @Test
    void commaSeparatedArrowLabels() {
        assertScript '''
            def n = switch (8) {
                case 6, 8, 10 -> 3
                default -> 0
            }
            assert n == 3
        '''
    }

    @Test
    void yieldInArrowBlock() {
        assertScript '''
            def n = switch (2) {
                case 1 -> 10
                case 2 -> {
                    int doubled = 2 * 10
                    yield doubled
                }
                default -> 0
            }
            assert n == 20
        '''
    }

    @Test
    void colonStyleWithYieldAndFallThrough() {
        assertScript '''
            def s = 'Bar'
            int result = switch (s) {
                case 'Foo':
                    yield 1
                case 'Bar':
                    // fall through
                case 'Baz':
                    yield 2
                default:
                    yield 0
            }
            assert result == 2
        '''
    }

    @Test
    void throwFromArm() {
        def err = shouldFail(RuntimeException, '''
            def x = 9
            def r = switch (x) {
                case 1 -> 1
                default -> throw new RuntimeException('nope')
            }
        ''')
        assert err.message == 'nope'
    }

    @Test
    void unmatchedSelectorThrows() {
        def err = shouldFail(IllegalStateException, '''
            def r = switch (99) {
                case 1 -> 1
            }
        ''')
        assert err.message.contains('does not cover')
    }

    @Test
    void groovyIsCaseMatching() {
        assertScript '''
            def r = switch ('abc') {
                case String -> 'str'
                case Integer -> 'int'
                default -> 'other'
            }
            assert r == 'str'

            r = switch (5) {
                case 1..10 -> 'range'
                default -> 'out'
            }
            assert r == 'range'

            r = switch ('hello') {
                case ~/h.*/ -> 're'
                default -> 'no'
            }
            assert r == 're'

            r = switch (4) {
                case { it % 2 == 0 } -> 'even'
                default -> 'odd'
            }
            assert r == 'even'
        '''
    }

    @Test
    void nestedSwitchExpressions() {
        assertScript '''
            def r = switch (1) {
                case 1 -> switch (2) {
                    case 2 -> 'inner'
                    default -> 'x'
                }
                default -> 'outer'
            }
            assert r == 'inner'
        '''
    }

    @Test
    void usedAsStatement() {
        assertScript '''
            int n = 0
            switch (1) {
                case 1 -> n += 1
                default -> n += 10
            }
            assert n == 1
        '''
    }

    @Test
    void assignToOuterLocal() {
        assertScript '''
            int acc = 0
            def r = switch (1) {
                case 1 -> {
                    acc = 7
                    yield acc
                }
                default -> 0
            }
            assert r == 7
            assert acc == 7
        '''
    }

    @Test
    void compileStaticArrowAndYield() {
        assertScript '''
            @groovy.transform.CompileStatic
            def meth(int a) {
                switch (a) {
                    case 1 -> 'one'
                    case 2 -> {
                        yield 'two'
                    }
                    default -> 'many'
                }
            }
            assert meth(1) == 'one'
            assert meth(2) == 'two'
            assert meth(9) == 'many'
        '''
    }

    @Test
    void compileStaticStringSwitch() {
        assertScript '''
            @groovy.transform.CompileStatic
            String partner(String person) {
                switch (person) {
                    case 'Romeo' -> 'Juliet'
                    case 'Adam' -> 'Eve'
                    default -> 'Unknown'
                }
            }
            assert partner('Romeo') == 'Juliet'
            assert partner('Adam') == 'Eve'
            assert partner('X') == 'Unknown'
        '''
    }

    @Test
    void compileStaticEnumSwitch() {
        assertScript '''
            import java.time.Month
            import static java.time.Month.*

            @groovy.transform.CompileStatic
            String quarter(Month month) {
                switch (month) {
                    case JANUARY, FEBRUARY, MARCH -> 'Q1'
                    case APRIL, MAY, JUNE -> 'Q2'
                    case JULY, AUGUST, SEPTEMBER -> 'Q3'
                    case OCTOBER, NOVEMBER, DECEMBER -> 'Q4'
                }
            }
            assert quarter(JUNE) == 'Q2'
            assert quarter(DECEMBER) == 'Q4'
        '''
    }

    @Test
    void yieldMethodNameOutsideSwitch() {
        assertScript '''
            def yield(String msg) { msg }
            assert yield('ok') == 'ok'
        '''
    }

    @Test
    void primitiveResult() {
        assertScript '''
            int n = switch (2) {
                case 1 -> 10
                case 2 -> 20
                default -> 0
            }
            assert n == 20
        '''
    }

    @Test
    void yieldInsideTryFinally() {
        assertScript '''
            def log = []
            def r = switch (1) {
                case 1 -> {
                    try {
                        yield 42
                    } finally {
                        log << 'fin'
                    }
                }
                default -> 0
            }
            assert r == 42
            assert log == ['fin']
        '''
    }

    @Test
    void nullSelectorUsesDefaultDynamically() {
        assertScript '''
            def r = switch (null) {
                case 1 -> 'one'
                default -> 'none'
            }
            assert r == 'none'
        '''
    }

    @Test
    void defaultOnly() {
        assertScript '''
            assert 7 == switch (99) {
                default -> 7
            }
        '''
    }

    @Test
    void tryFinallyAroundSwitchExpression() {
        assertScript '''
            def log = []
            def r = null
            try {
                r = switch (1) {
                    case 1 -> 42
                    default -> 0
                }
            } finally {
                log << 'outer'
            }
            assert r == 42
            assert log == ['outer']
        '''
    }

    @Test
    void synchronizedAroundSwitchExpression() {
        assertScript '''
            def lock = new Object()
            def r
            synchronized (lock) {
                r = switch (1) {
                    case 1 -> 7
                    default -> 0
                }
            }
            assert r == 7
        '''
    }

    @Test
    void compileStaticDefiniteAssignmentAfterYield() {
        assertScript '''
            @groovy.transform.CompileStatic
            int meth(int n) {
                int x
                int r = switch (n) {
                    case 1 -> {
                        x = 1
                        yield 10
                    }
                    default -> {
                        x = 2
                        yield 20
                    }
                }
                return r + x
            }
            assert meth(1) == 11
            assert meth(0) == 22
        '''
    }

    @Test
    void nestedExpressionInsideSwitchStatementDifferentEnums() {
        assertScript '''
            enum Color { RED, BLUE }
            enum Size { S, L }

            @groovy.transform.CompileStatic
            int meth(Color color, Size size) {
                switch (color) {
                    case RED:
                        return switch (size) {
                            case S -> 1
                            case L -> 2
                        }
                    case BLUE:
                        return switch (size) {
                            case S -> 3
                            case L -> 4
                        }
                }
            }
            assert meth(Color.RED, Size.S) == 1
            assert meth(Color.BLUE, Size.L) == 4
        '''
    }

    @Test
    void returnInsideLoopInSwitchExpressionIsError() {
        def err = shouldFail('''
            def r = switch (1) {
                case 1 -> {
                    for (;;) {
                        return 1
                    }
                }
                default -> 0
            }
        ''')
        assert err.message.contains('does not support `return`')
    }

    @Test
    void switchExpressionInsideClosure() {
        assertScript '''
            def r = { int n ->
                switch (n) {
                    case 1 -> 'one'
                    default -> 'other'
                }
            }(1)
            assert r == 'one'
        '''
    }

    @Test
    void compileStaticNullStringSelectorUsesDefault() {
        assertScript '''
            @groovy.transform.CompileStatic
            String m(String s) {
                switch (s) {
                    case 'Foo' -> 'a'
                    case 'Bar' -> 'b'
                    default    -> 'dflt'
                }
            }
            assert m('Foo') == 'a'
            assert m(null) == 'dflt'
        '''
    }

    @Test
    void compileStaticNullEnumSelectorUsesDefault() {
        assertScript '''
            import java.time.DayOfWeek

            @groovy.transform.CompileStatic
            String m(DayOfWeek d) {
                switch (d) {
                    case DayOfWeek.MONDAY -> 'mon'
                    default -> 'dflt'
                }
            }
            assert m(DayOfWeek.MONDAY) == 'mon'
            assert m(null) == 'dflt'
        '''
    }

    @Test
    void compileStaticNullSelectorOnExhaustiveEnumThrows() {
        def err = shouldFail(IllegalStateException, '''
            enum Flag { ON, OFF }

            @groovy.transform.CompileStatic
            String m(Flag f) {
                switch (f) {
                    case Flag.ON  -> 'on'
                    case Flag.OFF -> 'off'
                }
            }
            m(null)
        ''')
        assert err.message.contains('does not cover')
    }

    @Test
    void compileStaticNullIntegerSelectorUsesDefault() {
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
    void labeledBreakOutOfSwitchExpressionIsError() {
        def err = shouldFail('''
            outer:
            while (true) {
                def r = switch (1) {
                    case 1 -> {
                        for (;;) { break outer }
                        yield -1
                    }
                    default -> 0
                }
            }
        ''')
        assert err.message.contains("cannot break to label 'outer'")
    }

    @Test
    void labeledContinueOutOfSwitchExpressionIsError() {
        def err = shouldFail('''
            outer:
            while (true) {
                def r = switch (1) {
                    case 1 -> {
                        for (;;) { continue outer }
                        yield -1
                    }
                    default -> 0
                }
            }
        ''')
        assert err.message.contains("cannot continue to label 'outer'")
    }

    @Test
    void labeledBreakToArmLocalLoopIsAllowed() {
        assertScript '''
            def r = switch (1) {
                case 1 -> {
                    int n = 0
                    inner:
                    for (;;) {
                        n += 1
                        if (n > 2) break inner
                    }
                    yield n
                }
                default -> 0
            }
            assert r == 3
        '''
    }

    @Test
    void yieldThroughNestedClosureIsError() {
        def err = shouldFail('''
            def r = switch (1) {
                case 1 -> {
                    def c = { yield 1 }
                    yield c()
                }
                default -> 0
            }
        ''')
        assert err.message.contains('yield cannot jump through a closure or lambda')
    }

    @Test
    void typeCheckedUnmatchedIsError() {
        def err = shouldFail('''
            @groovy.transform.TypeChecked
            def meth(int a) {
                switch (a) {
                    case 1 -> 'one'
                }
            }
        ''')
        assert err.message.contains('does not cover all possible input values')
    }
}
