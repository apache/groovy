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
package org.codehaus.groovy.transform

import groovy.test.GroovyShellTestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException

/**
 * Tests for the {@code @SafeRegex} AST transform.
 */
class SafeRegexTransformTest extends GroovyShellTestCase {

    void testMatchingSemanticsPreservedWithinScope() {
        assertScript '''
            import java.util.regex.Matcher

            @groovy.transform.SafeRegex(millis = 500)
            def check() {
                assert 'groovy' ==~ /gro+vy/
                assert !('java' ==~ /gro+vy/)
                assert 'foo' ==~ /f(o+)/
                assert Matcher.lastMatcher.group(1) == 'oo'   // ==~ still records the last matcher

                def m = 'abc 123' =~ /(\\d+)/                 // =~ still yields a matcher
                assert m instanceof Matcher
                assert m.find() && m.group(1) == '123'
            }
            check()
        '''
    }

    void testMatchOperatorTimesOut() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            @groovy.transform.SafeRegex(millis = 100)
            def check(String input) {
                input ==~ /(.*,){16}X/
            }

            shouldFail(groovy.util.regex.RegexTimeoutException) {
                check('1,' * 40)
            }
        '''
    }

    void testFindOperatorTimesOut() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            @groovy.transform.SafeRegex(millis = 100)
            def check(String input) {
                def m = input =~ /(.*,){16}X/
                m.find()
            }

            shouldFail(groovy.util.regex.RegexTimeoutException) {
                check('1,' * 40)
            }
        '''
    }

    void testClassLevelAnnotationCoversAllMethods() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            @groovy.transform.SafeRegex(millis = 100)
            class Handler {
                boolean check(String input) {
                    input ==~ /(.*,){16}X/
                }
                boolean friendly(String input) {
                    input ==~ /gro+vy/
                }
            }

            def handler = new Handler()
            assert handler.friendly('groovy')
            shouldFail(groovy.util.regex.RegexTimeoutException) {
                handler.check('1,' * 40)
            }
        '''
    }

    void testClosureWithinScopeIsRewritten() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            @groovy.transform.SafeRegex(millis = 100)
            def check(String input) {
                def matched = { it ==~ /(.*,){16}X/ }
                matched(input)
            }

            shouldFail(groovy.util.regex.RegexTimeoutException) {
                check('1,' * 40)
            }
        '''
    }

    void testCompileStaticCombination() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            @groovy.transform.CompileStatic
            @groovy.transform.SafeRegex(millis = 100)
            class Handler {
                boolean check(String input) {
                    input ==~ /(.*,){16}X/
                }
                boolean friendly(String input) {
                    input ==~ /gro+vy/
                }
            }

            def handler = new Handler()
            assert handler.friendly('groovy')
            shouldFail(groovy.util.regex.RegexTimeoutException) {
                handler.check('1,' * 40)
            }
        '''
    }

    void testDefaultTimeoutAppliedWhenNoMillisGiven() {
        assertScript '''
            @groovy.transform.SafeRegex
            def check() {
                assert 'groovy' ==~ /gro+vy/
            }
            check()
        '''
    }

    void testDefaultTimeoutUnaffectedByEarlierExplicitMillis() {
        // one transform instance visits all annotated nodes of a class, so an
        // explicit millis must not leak into a later bare annotation's default
        assertScript '''
            class Handler {
                @groovy.transform.SafeRegex(millis = 5)
                boolean explicit() {
                    'groovy' ==~ /gro+vy/
                }

                @groovy.transform.SafeRegex
                boolean bare(String input) {
                    def m = input =~ /b/
                    sleep 50        // well past 5 ms, well within the 1000 ms default
                    m.find()        // enough reads over the input to consult the clock
                }
            }

            def handler = new Handler()
            assert handler.explicit()
            assert !handler.bare('a' * 2000)
        '''
    }

    void testOperandEvaluationOrderPreserved() {
        assertScript '''
            def order = []

            @groovy.transform.SafeRegex(millis = 500)
            def matchIt(Closure input, Closure pattern) {
                input() ==~ pattern()
            }

            @groovy.transform.SafeRegex(millis = 500)
            def findIt(Closure input, Closure pattern) {
                input() =~ pattern()
            }

            assert matchIt({ order << 'input'; 'groovy' }, { order << 'pattern'; /gro+vy/ })
            assert order == ['input', 'pattern']

            order.clear()
            def m = findIt({ order << 'input'; 'abc 123' }, { order << 'pattern'; /(\\d+)/ })
            assert m.find()
            assert order == ['input', 'pattern']
        '''
    }

    void testUnannotatedCodeIsUntouched() {
        assertScript '''
            import java.util.regex.Matcher

            def unguarded() {
                def m = 'abc' =~ /b/
                assert m instanceof Matcher
                assert 'groovy' ==~ /gro+vy/
            }
            unguarded()
        '''
    }

    void testNonPositiveMillisIsCompileError() {
        shouldFail(MultipleCompilationErrorsException) {
            evaluate '''
                @groovy.transform.SafeRegex(millis = 0)
                def check() { 'groovy' ==~ /gro+vy/ }
            '''
        }
    }

    void testNonConstantMillisIsCompileError() {
        shouldFail(MultipleCompilationErrorsException) {
            evaluate '''
                int timeout = 100
                @groovy.transform.SafeRegex(millis = timeout)
                def check() { 'groovy' ==~ /gro+vy/ }
            '''
        }
    }

    void testLocalVariableAnnotationGuardsInitializer() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            def friendly() {
                @groovy.transform.SafeRegex(millis = 500)
                def ok = 'groovy' ==~ /gro+vy/
                ok
            }
            assert friendly()

            def check(String input) {
                @groovy.transform.SafeRegex(millis = 100)
                def matched = input ==~ /(.*,){16}X/
                matched
            }
            shouldFail(groovy.util.regex.RegexTimeoutException) {
                check('1,' * 40)
            }
        '''
    }

    void testLocalVariableAnnotationCoversClosureInInitializer() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            def check(String input) {
                @groovy.transform.SafeRegex(millis = 100)
                def matched = { it ==~ /(.*,){16}X/ }
                matched(input)
            }
            shouldFail(groovy.util.regex.RegexTimeoutException) {
                check('1,' * 40)
            }
        '''
    }

    void testInstanceFieldInitializerGuarded() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            class Handler {
                @groovy.transform.SafeRegex(millis = 500)
                boolean friendly = 'groovy' ==~ /gro+vy/

                @groovy.transform.SafeRegex(millis = 100)
                boolean evil = ('1,' * 40) ==~ /(.*,){16}X/
            }

            shouldFail(groovy.util.regex.RegexTimeoutException) {
                new Handler()
            }
        '''
    }

    void testStaticFieldInitializerGuarded() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            class Holder {
                @groovy.transform.SafeRegex(millis = 100)
                static boolean evil = ('1,' * 40) ==~ /(.*,){16}X/
            }

            // a timeout in <clinit> surfaces as ExceptionInInitializerError
            def err = shouldFail(ExceptionInInitializerError) {
                Holder.evil
            }
            assert err.cause instanceof groovy.util.regex.RegexTimeoutException
        '''
    }
}
