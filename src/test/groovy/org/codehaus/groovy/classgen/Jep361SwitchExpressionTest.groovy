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
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * JEP 361 (https://openjdk.org/jeps/361) conformance for first-class switch
 * expressions. Each scenario from the JEP is executed as dynamic Groovy and
 * again under {@code @CompileStatic}.
 */
final class Jep361SwitchExpressionTest {

    private static void assertBoth(final String script) {
        assertScript(script)
        new GroovyShell(staticConfig()).evaluate(script)
    }

    private static void shouldFailBoth(final String script, final String snippet) {
        shouldFailBoth(MultipleCompilationErrorsException, script, snippet)
    }

    private static void shouldFailBoth(final Class<? extends Throwable> type,
            final String script) {
        shouldFail(type, script)
        shouldFail(type) { new GroovyShell(staticConfig()).evaluate(script) }
    }

    private static void shouldFailBoth(final Class<? extends Throwable> type,
            final String script, final String snippet) {
        def dynamic = shouldFail(type, script)
        assert type.isInstance(dynamic)
        assert dynamic.message.contains(snippet)
        def statik = shouldFail(type) { new GroovyShell(staticConfig()).evaluate(script) }
        assert type.isInstance(statik)
        assert statik.message.contains(snippet)
    }

    private static CompilerConfiguration staticConfig() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        return config
    }

    //--------------------------------------------------------------------------
    // Arrow labels (JEP 361 "Arrow labels")

    @Test
    void howManyArrowStatement() {
        // JEP: switch used as a statement with case L ->; 1/2/3 → one/two/many
        assertBoth '''
            def out = []
            [1, 2, 3].each { int k ->
                switch (k) {
                    case 1  -> out.add('one')
                    case 2  -> out.add('two')
                    default -> out.add('many')
                }
            }
            assert out == ['one', 'two', 'many']
        '''
    }

    @Test
    void commaSeparatedArrowStatement() {
        // JEP motivation: case MONDAY, FRIDAY, SUNDAY -> ...
        assertBoth '''
            import java.time.DayOfWeek
            import static java.time.DayOfWeek.*

            def letters = { DayOfWeek day ->
                int n = -1
                switch (day) {
                    case MONDAY, FRIDAY, SUNDAY -> n = 6
                    case TUESDAY                -> n = 7
                    case THURSDAY, SATURDAY     -> n = 8
                    case WEDNESDAY              -> n = 9
                }
                n
            }
            assert letters(MONDAY) == 6
            assert letters(FRIDAY) == 6
            assert letters(SUNDAY) == 6
            assert letters(TUESDAY) == 7
            assert letters(THURSDAY) == 8
            assert letters(SATURDAY) == 8
            assert letters(WEDNESDAY) == 9
        '''
    }

    @Test
    void colonSwitchStatementNeedNotBeExhaustive() {
        // JEP: switch statements are not required to be exhaustive.
        // Groovy's arrow form is always a switch expression, so this uses colon
        // labels (the statement production).
        assertBoth '''
            int n = 0
            switch (2) {
                case 1:
                    n = 1
            }
            assert n == 0
        '''
    }

    @Test
    void arrowArmMayBeExpressionBlockOrThrow() {
        assertBoth '''
            def m = { int k ->
                switch (k) {
                    case 1 -> 'expr'
                    case 2 -> {
                        def x = 'block'
                        yield x
                    }
                    default -> throw new IllegalArgumentException('nope')
                }
            }
            assert m(1) == 'expr'
            assert m(2) == 'block'
            try {
                m(3)
                assert false: 'expected IllegalArgumentException'
            } catch (IllegalArgumentException e) {
                assert e.message == 'nope'
            }
        '''
    }

    @Test
    void arrowArmLocalsDoNotLeakAcrossArms() {
        // JEP: locals to the right of -> must be in a block and are not in
        // scope for other arms; the same name can be reused.
        assertBoth '''
            import java.time.DayOfWeek
            import static java.time.DayOfWeek.*

            def m = { DayOfWeek day ->
                switch (day) {
                    case MONDAY, TUESDAY -> {
                        int temp = 1
                        yield temp
                    }
                    case WEDNESDAY, THURSDAY -> {
                        int temp = 2
                        yield temp
                    }
                    default -> {
                        int temp = 3
                        yield temp
                    }
                }
            }
            assert m(MONDAY) == 1
            assert m(WEDNESDAY) == 2
            assert m(FRIDAY) == 3
        '''
    }

    @Test
    void multiStatementArrowArmWithoutBlockIsError() {
        // JEP: the code to the right of -> is an expression, a block, or throw
        shouldFailBoth '''
            def r = switch (6) {
                case 6 -> def x = 'a'; yield x
                default -> 'z'
            }
        ''', 'Expect only 1 statement'
    }

    //--------------------------------------------------------------------------
    // Switch expressions (JEP 361 "Switch expressions")

    @Test
    void howManySwitchExpression() {
        // JEP: the howMany rewrite that feeds a single println
        assertBoth '''
            def howMany = { int k ->
                switch (k) {
                    case 1  -> 'one'
                    case 2  -> 'two'
                    default -> 'many'
                }
            }
            assert howMany(1) == 'one'
            assert howMany(2) == 'two'
            assert howMany(3) == 'many'
        '''
    }

    @Test
    void switchExpressionAsMethodArgument() {
        assertBoth '''
            def log = []
            log.add(switch (2) {
                case 1  -> 'one'
                case 2  -> 'two'
                default -> 'many'
            })
            assert log == ['two']
        '''
    }

    @Test
    void numLettersDayExpression() {
        // JEP: int numLetters = switch (day) { case MONDAY, FRIDAY, SUNDAY -> 6; ... }
        assertBoth '''
            import java.time.DayOfWeek
            import static java.time.DayOfWeek.*

            int numLetters = switch (MONDAY) {
                case MONDAY, FRIDAY, SUNDAY -> 6
                case TUESDAY                -> 7
                case THURSDAY, SATURDAY     -> 8
                case WEDNESDAY              -> 9
            }
            assert numLetters == 6
            numLetters = switch (TUESDAY) {
                case MONDAY, FRIDAY, SUNDAY -> 6
                case TUESDAY                -> 7
                case THURSDAY, SATURDAY     -> 8
                case WEDNESDAY              -> 9
            }
            assert numLetters == 7
            numLetters = switch (WEDNESDAY) {
                case MONDAY, FRIDAY, SUNDAY -> 6
                case TUESDAY                -> 7
                case THURSDAY, SATURDAY     -> 8
                case WEDNESDAY              -> 9
            }
            assert numLetters == 9
        '''
    }

    @Test
    void standaloneTypeFromArms() {
        // JEP: if there is no target type, a standalone type is computed from the arms
        assertBoth '''
            CharSequence cs = switch (1) {
                case 1 -> 'hello'
                default -> new StringBuilder('x')
            }
            assert cs.toString() in ['hello', 'x']
        '''
    }

    //--------------------------------------------------------------------------
    // Yielding a value (JEP 361 "Yielding a value")

    @Test
    void yieldInArrowBlockWithLocals() {
        // JEP: default -> { int k = day.toString().length(); yield result; }
        assertBoth '''
            import java.time.DayOfWeek
            import static java.time.DayOfWeek.*

            def f = { int k -> k + 1 }
            int j = switch (WEDNESDAY) {
                case MONDAY  -> 0
                case TUESDAY -> 1
                default      -> {
                    int k = WEDNESDAY.toString().length()
                    int result = f(k)
                    yield result
                }
            }
            assert j == f('WEDNESDAY'.length())
            assert switch (MONDAY) {
                case MONDAY  -> 0
                case TUESDAY -> 1
                default      -> 99
            } == 0
        '''
    }

    @Test
    void colonSwitchExpressionWithYield() {
        // JEP: case "Foo": yield 1; case "Bar": yield 2; default: println; yield 0
        assertBoth '''
            def log = []
            int foo = switch ('Foo') {
                case 'Foo':
                    yield 1
                case 'Bar':
                    yield 2
                default:
                    log.add('Neither Foo nor Bar, hmmm...')
                    yield 0
            }
            int bar = switch ('Bar') {
                case 'Foo':
                    yield 1
                case 'Bar':
                    yield 2
                default:
                    log.add('Neither Foo nor Bar, hmmm...')
                    yield 0
            }
            int other = switch ('Qux') {
                case 'Foo':
                    yield 1
                case 'Bar':
                    yield 2
                default:
                    log.add('Neither Foo nor Bar, hmmm...')
                    yield 0
            }
            assert foo == 1
            assert bar == 2
            assert other == 0
            assert log == ['Neither Foo nor Bar, hmmm...']
        '''
    }

    @Test
    void colonFallThroughInSwitchExpression() {
        assertBoth '''
            def log = []
            int bar = switch ('Bar') {
                case 'Foo':
                    yield 1
                case 'Bar':
                    log.add('Bar!!')
                case 'Baz':
                    yield 2
                default:
                    yield 0
            }
            int baz = switch ('Baz') {
                case 'Foo':
                    yield 1
                case 'Bar':
                    log.add('Bar!!')
                case 'Baz':
                    yield 2
                default:
                    yield 0
            }
            assert bar == 2
            assert baz == 2
            assert log == ['Bar!!']
        '''
    }

    @Test
    void commaSeparatedColonLabels() {
        assertBoth '''
            assert 10 == switch (1) {
                case 1, 2, 3:
                    yield 10
                default:
                    yield 0
            }
            assert 10 == switch (3) {
                case 1, 2, 3:
                    yield 10
                default:
                    yield 0
            }
            assert 0 == switch (4) {
                case 1, 2, 3:
                    yield 10
                default:
                    yield 0
            }
        '''
    }

    @Test
    void yieldStatementTakesPrecedenceOverMethodNamedYield() {
        // JEP: yield(x) is a yield statement; qualify the method with this
        assertBoth '''
            class C {
                int yield(int x) { x + 100 }
                int run() {
                    int a = switch (1) {
                        case 1 -> {
                            yield(1)
                        }
                        default -> 0
                    }
                    int b = this.yield(1)
                    assert a == 1
                    assert b == 101
                    return a + b
                }
            }
            assert new C().run() == 102
        '''
    }

    @Test
    void staticYieldMethodIsQualifiedByTypeName() {
        assertBoth '''
            class C {
                static int yield(int x) { x + 100 }
                static int run() {
                    int a = switch (1) {
                        case 1 -> {
                            yield(1)
                        }
                        default -> 0
                    }
                    int b = C.yield(1)
                    assert a == 1
                    assert b == 101
                    return a
                }
            }
            assert C.run() == 1
        '''
    }

    @Test
    void yieldOutsideSwitchExpressionIsAMethodCall() {
        // JEP: yield is a restricted identifier. Groovy only parses a yield
        // statement inside a switch expression; elsewhere `yield x` is a call.
        assertBoth '''
            def yield = { int x -> x + 5 }
            assert yield(1) == 6
        '''
    }

    //--------------------------------------------------------------------------
    // Exhaustiveness (JEP 361 "Exhaustiveness")

    @Test
    void arrowBlockWithoutYieldIsError() {
        // JEP: case MONDAY -> { println(...); }  // ERROR, no yield
        shouldFailBoth '''
            def day = 1
            int i = switch (day) {
                case 1 -> {
                    if (true) {
                        def unused = 'Monday'
                    }
                }
                default -> 1
            }
        ''', 'yield'
    }

    @Test
    void colonGroupWithoutYieldIsError() {
        // JEP: default: println(...);  // ERROR, group has no yield
        shouldFailBoth '''
            def day = 1
            int i = switch (day) {
                case 1, 2, 3:
                    yield 0
                default:
                    def unused = 0
            }
        ''', 'yield'
    }

    @Test
    void exhaustiveEnumNeedsNoDefault() {
        assertBoth '''
            import java.time.DayOfWeek
            import static java.time.DayOfWeek.*

            int letters = switch (WEDNESDAY) {
                case MONDAY, FRIDAY, SUNDAY -> 6
                case TUESDAY                -> 7
                case THURSDAY, SATURDAY     -> 8
                case WEDNESDAY              -> 9
            }
            assert letters == 9
        '''
    }

    @Test
    void dynamicNonExhaustiveThrowsAtRuntime() {
        // JEP requires exhaustiveness; dynamic Groovy checks at run time
        def err = shouldFail(IllegalStateException, '''
            def r = switch (99) {
                case 1 -> 1
            }
        ''')
        assert err.message.contains('does not cover')
    }

    @Test
    void staticNonExhaustiveIsCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell(staticConfig()).evaluate('''
                def r = switch (99) {
                    case 1 -> 1
                }
            ''')
        }
        assert err.message.contains('does not cover all possible input values')
    }

    @Test
    void throwCompletesAnArm() {
        shouldFailBoth(IllegalStateException, '''
            def r = switch (9) {
                case 1 -> 1
                default -> throw new IllegalStateException('Wat: 9')
            }
        ''')
    }

    //--------------------------------------------------------------------------
    // Control transfer (JEP 361: break / yield / return / continue)

    @Test
    void continueThroughSwitchExpressionIsError() {
        // JEP: continue z from inside a switch expression
        shouldFailBoth '''
            int MAX = 3
            z:
            for (int i = 0; i < MAX; ++i) {
                int k = switch (i) {
                    case 0:
                        yield 1
                    case 1:
                        yield 2
                    default:
                        continue z
                }
            }
        ''', 'continue'
    }

    @Test
    void returnThroughSwitchExpressionIsError() {
        shouldFailBoth '''
            def m = {
                def r = switch (1) {
                    case 1 -> {
                        return 1
                    }
                    default -> 0
                }
            }
            m()
        ''', 'does not support `return`'
    }

    @Test
    void unlabeledBreakThroughSwitchExpressionIsError() {
        shouldFailBoth '''
            def r = switch (1) {
                case 1:
                    break
                default:
                    yield 0
            }
        ''', 'does not support `break`'
    }

    @Test
    void breakStillTargetsASwitchStatement() {
        assertBoth '''
            int n = 0
            switch (1) {
                case 1:
                    n = 1
                    break
                default:
                    n = 2
            }
            assert n == 1
        '''
    }

    @Test
    void mixingArrowAndColonLabelsIsError() {
        shouldFailBoth '''
            def r = switch (6) {
                case 6 -> 'a'
                case 8:
                    yield 'b'
                default -> 'c'
            }
        ''', 'cannot be used together'
    }
}
