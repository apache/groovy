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
 * Tests for non-local return from closures via {@code return@target} (GROOVY-12126).
 */
final class NonLocalReturnTest {

    private static final GroovyShell CS_SHELL = new GroovyShell(
            new CompilerConfiguration().addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic)))

    /** Runs the script dynamically and under @CompileStatic. */
    private static void assertScriptBoth(String text) {
        assertScript(text)
        assertScript(CS_SHELL, text)
    }

    @Test
    void testReturnFromNestedClosures() {
        assertScriptBoth '''
            def firstMatch(List<List<Integer>> rows, Closure<Boolean> pred) {
                rows.each { row ->
                    row.each { cell ->
                        if (pred(cell)) return@firstMatch cell
                    }
                }
                null
            }
            assert firstMatch([[1, 2], [3, 4]]) { Integer it -> it > 2 } == 3
            assert firstMatch([[1, 2], [3, 4]]) { Integer it -> it > 9 } == null
        '''
    }

    @Test
    void testReturnWithoutValue() {
        assertScriptBoth '''
            def m(List<Integer> items) {
                def seen = []
                items.each {
                    if (it > 2) return@m seen
                    seen << it
                }
                seen << 'end'
                seen
            }
            assert m([1, 2, 3, 4]) == [1, 2]
            assert m([1, 2]) == [1, 2, 'end']
        '''
    }

    @Test
    void testVoidMethod() {
        assertScriptBoth '''
            class C {
                List seen = []
                void scan(List<Integer> items) {
                    items.each {
                        if (it == 0) return@scan
                        seen << it
                    }
                    seen << 'end'
                }
            }
            def c = new C()
            c.scan([1, 0, 2])
            assert c.seen == [1]
        '''
    }

    @Test
    void testPrimitiveReturnType() {
        assertScriptBoth '''
            int firstBig(List<Integer> items) {
                items.each { if (it > 10) return@firstBig it }
                -1
            }
            assert firstBig([1, 20, 3]) == 20
            assert firstBig([1, 2, 3]) == -1
        '''
    }

    @Test
    void testStaticMethod() {
        assertScriptBoth '''
            class Util {
                static firstEven(List<Integer> nums) {
                    nums.each { if (it % 2 == 0) return@firstEven it }
                    null
                }
            }
            assert Util.firstEven([1, 3, 4, 5]) == 4
        '''
    }

    @Test
    void testTargetAtMethodLevelIsPlainReturn() {
        assertScriptBoth '''
            def m() {
                return@m 42
            }
            assert m() == 42
        '''
    }

    @Test
    void testRecursionUsesPerActivationToken() {
        assertScriptBoth '''
            def search(int depth) {
                [1].each {
                    if (depth == 0) return@search 'hit'
                }
                'wrapped(' + search(depth - 1) + ')'
            }
            assert search(2) == 'wrapped(wrapped(hit))'
        '''
    }

    @Test
    void testFinallyRunsDuringUnwind() {
        assertScriptBoth '''
            def m(List log, List<Integer> items) {
                try {
                    items.each {
                        log << it
                        if (it == 2) return@m 'done'
                    }
                } finally {
                    log << 'fin'
                }
                'end'
            }
            def log = []
            assert m(log, [1, 2, 3]) == 'done'
            assert log == [1, 2, 'fin']
        '''
    }

    @Test
    void testReturnFromScriptBody() {
        assertScriptBoth '''
            def seen = []
            [1, 2, 3].each {
                seen << it
                if (it == 2) {
                    assert seen == [1, 2]
                    return@script
                }
            }
            throw new IllegalStateException('should not be reached')
        '''
    }

    @Test
    void testEscapedClosureSurfacesLoudly() {
        assertScriptBoth '''
            import org.codehaus.groovy.runtime.NonLocalReturn

            def maker(List<Closure> stash) {
                stash << { -> return@maker 1 }
                'made'
            }
            List<Closure> stash = []
            assert maker(stash) == 'made'
            try {
                stash[0].call()
                assert false, 'expected NonLocalReturn to escape'
            } catch (NonLocalReturn expected) {
            }
        '''
    }

    @Test
    void testStaleActivationRethrowsNotHijacks() {
        assertScriptBoth '''
            import org.codehaus.groovy.runtime.NonLocalReturn

            def maker(List<Closure> stash, boolean capture) {
                if (capture) {
                    stash << { -> return@maker 'stolen' }
                    return 'made'
                }
                stash[0].call()
                'not reached'
            }
            List<Closure> stash = []
            assert maker(stash, true) == 'made'
            try {
                maker(stash, false)
                assert false, 'expected NonLocalReturn to escape the mismatched activation'
            } catch (NonLocalReturn expected) {
            }
        '''
    }

    @Test
    void testUnknownTargetIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            def m() {
                [1].each { return@nosuch 1 }
            }
        '''
        assert err.message.contains("no lexically enclosing method named 'nosuch'")
    }

    @Test
    void testCrossClassBoundaryIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class C {
                def outer(items) {
                    def r = new Runnable() {
                        void run() {
                            [1].each { return@outer 1 }
                        }
                    }
                    r.run()
                }
            }
        '''
        assert err.message.contains('cannot return across a class boundary')
    }

    @Test
    void testConstructorIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class C {
                C() {
                    [1].each { return@C 1 }
                }
            }
        '''
        assert err.message.contains('only allowed inside a method body')
    }

    @Test
    void testScriptTargetOutsideScriptBodyIsCompileError() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class C {
                def m() {
                    [1].each { return@script 1 }
                }
            }
        '''
        assert err.message.contains("no lexically enclosing method named 'script'")
    }
}
