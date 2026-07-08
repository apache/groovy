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
package gls.closures

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for break/continue inside closure arguments to cooperating iterator
 * methods (GROOVY-12126).
 */
final class LoopControlTest {

    private static final GroovyShell CS_SHELL = new GroovyShell(
            new CompilerConfiguration().addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic)))

    /** Runs the script dynamically and under @CompileStatic. */
    private static void assertScriptBoth(String text) {
        assertScript(text)
        assertScript(CS_SHELL, text)
    }

    @Test
    void testEachBreakContinue() {
        assertScriptBoth '''
            def seen = []
            [1, 2, 3, 4, 5].each {
                if (it == 2) continue
                if (it == 4) break
                seen << it
            }
            assert seen == [1, 3]
        '''
    }

    @Test
    void testTimesUptoStep() {
        assertScriptBoth '''
            def seen = []
            5.times {
                if (it == 3) break
                seen << it
            }
            assert seen == [0, 1, 2]

            seen = []
            1.upto(9) {
                if (it % 2 == 0) continue
                if (it > 5) break
                seen << it
            }
            assert seen == [1, 3, 5]

            seen = []
            0.step(10, 2) {
                if (it == 4) continue
                if (it == 8) break
                seen << it
            }
            assert seen == [0, 2, 6]
        '''
    }

    @Test
    void testCollectExcludesAndSkips() {
        assertScriptBoth '''
            assert [1, 2, 3, 4].collect {
                if (it == 2) continue
                if (it == 4) break
                it * 10
            } == [10, 30]
        '''
    }

    @Test
    void testFindAllAndInject() {
        assertScriptBoth '''
            assert [1, 2, 3, 4, 5].findAll {
                if (it == 4) break
                it % 2 == 1
            } == [1, 3]

            assert [1, 2, 3, 4].inject(0) { acc, it ->
                if (it == 2) continue
                if (it == 4) break
                acc + it
            } == 4
        '''
    }

    @Test
    void testMapIteration() {
        assertScriptBoth '''
            def seen = []
            [a: 1, b: 2, c: 3].each { k, v ->
                if (v == 2) continue
                if (v == 3) break
                seen << k
            }
            assert seen == ['a']
        '''
    }

    @Test
    void testNestedClosuresBindInnermostIteration() {
        assertScriptBoth '''
            def seen = []
            [[1, 2, 3], [4, 5]].each { row ->
                row.each { cell ->
                    if (cell == 2 || cell == 5) break
                    seen << cell
                }
            }
            assert seen == [1, 4]
        '''
    }

    @Test
    void testBreakBoundToRealLoopInsideClosureIsUntouched() {
        assertScriptBoth '''
            def seen = []
            [1, 2].each { outer ->
                for (i in 10..14) {
                    if (i == 12) break
                    seen << i
                }
                seen << outer
            }
            assert seen == [10, 11, 1, 10, 11, 2]
        '''
    }

    @Test
    void testContinueBoundToRealLoopInsideClosureIsUntouched() {
        assertScriptBoth '''
            def seen = []
            [1].each {
                for (i in 1..5) {
                    if (i % 2 == 0) continue
                    seen << i
                }
            }
            assert seen == [1, 3, 5]
        '''
    }

    @Test
    void testBreakBoundToSwitchInsideClosureIsUntouched() {
        assertScriptBoth '''
            def seen = []
            [1, 2, 3].each {
                switch (it) {
                    case 2:
                        seen << 'two'
                        break
                    default:
                        seen << it
                }
            }
            assert seen == [1, 'two', 3]
        '''
    }

    @Test
    void testUncooperativeCalleeEscapesLoudlyWhenDynamic() {
        assertScript '''
            import org.codehaus.groovy.runtime.LoopControl

            try {
                'x'.with { break }
                assert false, 'expected LoopControl to escape'
            } catch (LoopControl expected) {
                assert expected.message.contains('does not support loop control')
            }
        '''
    }

    @Test
    void testUncooperativeCalleeIsCompileErrorUnderStaticTypeChecking() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            @groovy.transform.TypeChecked
            void m() {
                'x'.with { break }
            }
            m()
        '''
        assert err.message.contains('is not marked @SupportsLoopControl')
    }

    @Test
    void testBreakOutsideCallArgumentClosureIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def c = { break }
        '''
        assert err.message.contains('break inside a closure is only allowed when the closure is a direct argument of a method call')
    }

    @Test
    void testContinueOutsideCallArgumentClosureIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def c = { continue }
        '''
        assert err.message.contains('continue inside a closure is only allowed when the closure is a direct argument of a method call')
    }

    @Test
    void testLabeledBreakInClosureIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            outer:
            for (i in 1..3) {
                [1].each { break outer }
            }
        '''
        assert err.message.contains('labeled break is not allowed inside a closure')
    }

    @Test
    void testBreakInClosureInsideLexicalLoopBindsIteration() {
        // previously leaked through LabelVerifier and broke at codegen; now well-defined
        assertScriptBoth '''
            def seen = []
            for (i in 1..2) {
                [10, 20, 30].each {
                    if (it == 20) break
                    seen << it
                }
            }
            assert seen == [10, 10]
        '''
    }
}
