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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12399: a {@code switch} with arrow arms is a statement or an
 * expression by position, as in Java (JEP 361), not by syntax. A switch
 * whose value is not used does nothing when unmatched; a switch in
 * implicit-return position keeps its value and yields {@code null} when
 * unmatched, as in 4.x/5.x; every other expression position keeps the
 * strict rules of GROOVY-12255.
 */
final class Groovy12399 {

    private static void assertBoth(final String script) {
        assertScript(script)
        new GroovyShell(staticConfig()).evaluate(script)
    }

    private static CompilerConfiguration staticConfig() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        return config
    }

    @Test
    void statementWithExpressionArmDoesNothingWhenUnmatched() {
        assertBoth '''
            def f(int i) {
                def r = 'none'
                switch (i) {
                    case 1 -> r = 'one'
                }
                r
            }
            assert f(1) == 'one'
            assert f(42) == 'none'
        '''
    }

    @Test
    void statementWithClassLabelDoesNothingWhenUnmatched() {
        assertBoth '''
            def f(Object o) {
                def r = 'none'
                switch (o) {
                    case String -> r = 'str'
                }
                r
            }
            assert f('x') == 'str'
            assert f(42) == 'none'
        '''
    }

    @Test
    void statementWithSingleStatementBlockArmDoesNothingWhenUnmatched() {
        assertBoth '''
            def f(int i) {
                def r = 'none'
                switch (i) {
                    case 1 -> { r = 'one' }
                }
                r
            }
            assert f(42) == 'none'
        '''
    }

    @Test
    void statementAsLastStatementOfLoopBody() {
        assertBoth '''
            def log = []
            for (i in [1, 2, 3]) {
                switch (i) {
                    case 2 -> log << 'two'
                }
            }
            assert log == ['two']
        '''
    }

    @Test
    void statementInsideIfBranchNotInReturnPosition() {
        assertBoth '''
            def f(boolean b, int i) {
                def r = 'none'
                if (b) {
                    switch (i) {
                        case 1 -> r = 'one'
                    }
                    r = r + '!'
                }
                r
            }
            assert f(true, 42) == 'none!'
        '''
    }

    @Test
    void statementNeedsNoDefaultWhenTypeChecked() {
        assertScript '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def f(Object o) {
                def r = 'none'
                switch (o) {
                    case String -> r = 'str'
                }
                r
            }
            assert f(42) == 'none'
        '''
    }

    @Test
    void expressionPositionsStillThrowWhenUnmatched() {
        for (form in ['def r = switch (i) { case 1 -> \'one\' }; r',
                      'return switch (i) { case 1 -> \'one\' }',
                      'Objects.toString(switch (i) { case 1 -> \'one\' })']) {
            assertScript """
                import static groovy.test.GroovyAssert.shouldFail
                def f(int i) {
                    $form
                }
                assert f(1) == 'one'
                def e = shouldFail(IllegalStateException) { f(42) }
                assert e.message.contains('does not cover the value 42')
            """
        }
    }

    @Test
    void expressionPositionsStillRequireExhaustivenessWhenTypeChecked() {
        for (form in ['def r = switch (i) { case 1 -> \'one\' }; r',
                      'return switch (i) { case 1 -> \'one\' }']) {
            def err = shouldFail """
                import groovy.transform.TypeChecked
                @TypeChecked
                def f(int i) {
                    $form
                }
            """
            assert err.message.contains('does not cover all possible input values')
        }
    }

    @Test
    void implicitReturnKeepsValueAndYieldsNullWhenUnmatched() {
        assertBoth '''
            def f(int i) {
                switch (i) {
                    case 1 -> 'one'
                }
            }
            assert f(1) == 'one'
            assert f(42) == null
        '''
    }

    @Test
    void implicitReturnInClosure() {
        assertBoth '''
            def names = [1, 2, 3].collect {
                switch (it) {
                    case 1 -> 'one'
                    case 2 -> 'two'
                }
            }
            assert names == ['one', 'two', null]
        '''
    }

    @Test
    void implicitReturnAsLastScriptStatement() {
        assert new GroovyShell().evaluate('''
            def i = 1
            switch (i) {
                case 1 -> 'one'
            }
        ''') == 'one'
        assert new GroovyShell().evaluate('''
            def i = 42
            switch (i) {
                case 1 -> 'one'
            }
        ''') == null
    }

    @Test
    void implicitReturnThroughIfElseTryAndColonSwitch() {
        assertBoth '''
            def viaIf(boolean b, int i) {
                if (b) {
                    switch (i) { case 1 -> 'one' }
                } else {
                    'else'
                }
            }
            assert viaIf(true, 1) == 'one'
            assert viaIf(true, 42) == null
            assert viaIf(false, 1) == 'else'

            def viaTry(int i) {
                try {
                    switch (i) { case 1 -> 'one' }
                } catch (e) {
                    'caught'
                }
            }
            assert viaTry(1) == 'one'
            assert viaTry(42) == null

            def viaColonSwitch(int outer, int i) {
                switch (outer) {
                    case 0:
                        switch (i) { case 1 -> 'one' }
                        break
                    default:
                        'other'
                }
            }
            assert viaColonSwitch(0, 1) == 'one'
            assert viaColonSwitch(0, 42) == null
            assert viaColonSwitch(9, 1) == 'other'
        '''
    }

    @Test
    void implicitReturnNeedsNoDefaultWhenTypeChecked() {
        assertScript '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def f(int i) {
                switch (i) {
                    case 1 -> 'one'
                }
            }
            assert f(1) == 'one'
            assert f(42) == null
        '''
    }

    @Test
    void implicitReturnWithNonYieldingBlockArmIsAStatement() {
        assertBoth '''
            def f(int i) {
                switch (i) {
                    case 1 -> { println 'one' }
                }
            }
            assert f(1) == null
            assert f(42) == null
        '''
    }
}
