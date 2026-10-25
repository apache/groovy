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
package org.apache.groovy.contracts.tests.other

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for {@code @Spec} holes.
 */
class SpecTests {

    @Test
    void unfilledMethodThrows() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert

            class Dates {
                @Spec('Parse an ISO-8601 calendar date (yyyy-MM-dd).')
                static java.time.LocalDate parse(String text) { }
            }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { Dates.parse('2026-02-28') }
            assert err.message == 'unfilled @Spec: Dates.parse'
        '''
    }

    @Test
    void unfilledVoidMethodThrows() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert

            class Greeter {
                @Spec('Print a greeting.')
                void greet() { }
            }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { new Greeter().greet() }
            assert err.message == 'unfilled @Spec: Greeter.greet'
        '''
    }

    @Test
    void filledMethodIsUnchanged() {
        assertScript '''
            import groovy.contracts.Spec

            class Dates {
                @Spec(value = 'Parse an ISO-8601 calendar date (yyyy-MM-dd).', fill = 'abc123')
                static java.time.LocalDate parse(String text) { java.time.LocalDate.parse(text) }

                @Spec('Do nothing.')
                static void noop() { return }
            }

            assert Dates.parse('2026-02-28').monthValue == 2
            Dates.noop()
        '''
    }

    @Test
    void specRetainedForReflection() {
        assertScript '''
            import groovy.contracts.Spec

            class Dates {
                @Spec(value = 'Parse a date.', fill = 'abc123')
                static java.time.LocalDate parse(String text) { java.time.LocalDate.parse(text) }
            }

            def spec = Dates.getMethod('parse', String).getAnnotation(Spec)
            assert spec.value() == 'Parse a date.'
            assert spec.fill() == 'abc123'
        '''
    }

    @Test
    void abstractMethodIsUnchanged() {
        assertScript '''
            import groovy.contracts.Spec

            interface Parser {
                @Spec('Parse the text.')
                Object parse(String text)
            }

            abstract class Base {
                @Spec('Parse the text.')
                abstract Object parse(String text)
            }

            assert new Base() { Object parse(String text) { text } }.parse('x') == 'x'
            assert ({ it } as Parser).parse('y') == 'y'
        '''
    }

    @Test
    void unfilledMethodWithContracts() {
        assertScript '''
            import groovy.contracts.Ensures
            import groovy.contracts.Requires
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert
            import org.apache.groovy.contracts.PreconditionViolation

            class Dates {
                @Spec('Parse an ISO-8601 calendar date (yyyy-MM-dd).')
                @Requires({ text != null })
                @Ensures({ result.monthValue in 1..12 })
                static java.time.LocalDate parse(String text) { }
            }

            GroovyAssert.shouldFail(PreconditionViolation) { Dates.parse(null) }
            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { Dates.parse('2026-02-28') }
            assert err.message == 'unfilled @Spec: Dates.parse'
        '''
    }

    @Test
    void unfilledMethodUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert
            import groovy.transform.CompileStatic

            @CompileStatic
            class Counter {
                @Spec('Count the vowels in the text.')
                static int vowels(String text) { }
            }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { Counter.vowels('spec') }
            assert err.message == 'unfilled @Spec: Counter.vowels'
        '''
    }

    @Test
    void unfilledLoopsThrowWhenReached() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert

            class Loops {
                static int forLoop(int n) {
                    int total = 0
                    @Spec('Add the numbers from 1 to n to total.')
                    for (int i = 1; i <= n; i++) { }
                    total
                }

                static void whileLoop(boolean enter) {
                    @Spec('Never entered, but still unfilled.')
                    while (enter) { }
                }

                static void doWhileLoop() {
                    @Spec('Run once.')
                    do { } while (false)
                }

                static void bracelessBranch(boolean go) {
                    if (go)
                        @Spec('Only reached when go is true.')
                        for (x in [1, 2]) { }
                }
            }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { Loops.forLoop(3) }
            assert err.message == 'unfilled @Spec: Loops.forLoop, loop at line 8'
            err = GroovyAssert.shouldFail(UnsupportedOperationException) { Loops.whileLoop(false) }
            assert err.message == 'unfilled @Spec: Loops.whileLoop, loop at line 14'
            err = GroovyAssert.shouldFail(UnsupportedOperationException) { Loops.doWhileLoop() }
            assert err.message == 'unfilled @Spec: Loops.doWhileLoop, loop at line 19'
            Loops.bracelessBranch(false)
            err = GroovyAssert.shouldFail(UnsupportedOperationException) { Loops.bracelessBranch(true) }
            assert err.message == 'unfilled @Spec: Loops.bracelessBranch, loop at line 25'
        '''
    }

    @Test
    void filledLoopIsUnchanged() {
        assertScript '''
            import groovy.contracts.Spec

            int total = 0
            @Spec('Add the numbers from 1 to 3 to total.')
            for (int i = 1; i <= 3; i++) { total += i }
            assert total == 6
        '''
    }

    @Test
    void unfilledLocalThrowsWhenReached() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert

            class Words {
                static int longest(List<String> words) {
                    @Spec('The length of the longest word, or 0 if there are none.')
                    int result
                    result
                }

                static int shortest(List<String> words) {
                    @Spec('The length of the shortest word, or 0 if there are none.')
                    int result = words ? words*.size().min() : 0
                    result
                }
            }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { Words.longest(['a', 'bb']) }
            assert err.message == "unfilled @Spec: Words.longest, local variable 'result' at line 7"
            assert Words.shortest(['a', 'bb']) == 1
        '''
    }

    @Test
    void unfilledHolesInScript() {
        assertScript '''
            import groovy.contracts.Spec
            import groovy.test.GroovyAssert

            @Spec('Return the answer.')
            int answer() { }

            def err = GroovyAssert.shouldFail(UnsupportedOperationException) { answer() }
            assert err.message.startsWith('unfilled @Spec: ') && err.message.endsWith('.answer')
        '''
    }
}
