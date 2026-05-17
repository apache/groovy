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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.junit.jupiter.api.Test

import java.lang.reflect.Array

final class Groovy10156 extends AbstractBytecodeTestCase {

    @Test
    void testEnumSwitchWithBreakDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                int result = 0
                switch (e) {
                    case E.ONE:
                        result = 1
                        break
                    case E.TWO:
                        result = 2
                        break
                    case E.THREE:
                        result = 3
                        break
                    default:
                        result = 4
                }
                result
            }
        ''', 'ONE, TWO, THREE')
    }

    @Test
    void testEnumSwitchWithReturnDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        return 1
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithThrowDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        throw new IllegalStateException('one')
                    case E.TWO:
                        throw new IllegalStateException('two')
                    default:
                        throw new IllegalStateException('default')
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithNestedSwitchWhereAllPathsReturnDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e, boolean flag) {
                switch (e) {
                    case E.ONE:
                        switch (flag) {
                            case true:
                                return 1
                            default:
                                return 2
                        }
                    case E.TWO:
                        return 3
                    default:
                        return 4
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithContinueDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E[] values) {
                int result = 0
                outer:
                for (E e : values) {
                    switch (e) {
                        case E.ONE:
                            result += 1
                            continue outer
                        case E.TWO:
                            result += 2
                            break
                        default:
                            result += 4
                    }
                    result += 8
                }
                result
            }
        ''', 'ONE, TWO, THREE')
    }

    @Test
    void testEnumSwitchWithTryCatchFinallyWhereAllPathsReturnDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnexpectedUnreachableRethrowInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        try {
                            return 1
                        } catch (RuntimeException ignored) {
                            return 2
                        } finally {
                        }
                    default:
                        return 3
                }
            }
        ''', 'ONE')
    }

    @Test
    void testEnumSwitchWithSynchronizedReturnDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        synchronized (this) {
                            return 1
                        }
                    default:
                        return 2
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithInfiniteWhileDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        while (true) {
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithInfiniteDoWhileDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        do {
                        } while (true)
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithConstantFalseWhileStillFallsThrough() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        while (false) {
                            return 99
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
            assert c.test(enumValue(loader, 'THREE')) == 3
        }
    }

    @Test
    void testEnumSwitchWithClassicForConstantFalseStillFallsThrough() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        for (; false ;) {
                            return 99
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
            assert c.test(enumValue(loader, 'THREE')) == 3
        }
    }

    @Test
    void testEnumSwitchWithForInLoopStillFallsThrough() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        for (value in [] as List<Integer>) {
                            return 99
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
            assert c.test(enumValue(loader, 'THREE')) == 3
        }
    }

    @Test
    void testEnumSwitchWithInfiniteClassicForDoesNotEmitSyntheticFallthroughJumps() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        for (;;) {
                            return 1
                        }
                    default:
                        return 2
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithEmptyCaseStillFallsThrough() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testEnumSwitchWithMixedBreakAndFallThroughPreservesSemantics() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e, boolean stopEarly) {
                int result = 0
                switch (e) {
                    case E.ONE:
                        if (stopEarly) break
                        result = 1
                    case E.TWO:
                        return result + 2
                    default:
                        return 4
                }
                return result + 8
            }
        ''', 'ONE, TWO, THREE')
    }

    @Test
    void testLabeledSwitchRemainsValid() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e) {
                outer:
                switch (e) {
                    case E.ONE:
                        return 1
                    default:
                        return 2
                }
            }
        ''')
    }

    @Test
    void testNestedLabeledSwitchRemainsValid() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e, boolean flag) {
                switch (e) {
                    case E.ONE:
                        inner:
                        switch (flag) {
                            case true:
                                return 2
                            default:
                                return 1
                        }
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testSwitchFlowAnalysisHandlesLabeledBlockBreaksInCaseBodies() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e, boolean stopEarly) {
                switch (e) {
                    case E.ONE:
                        inner: {
                            if (stopEarly) {
                                break inner
                            }
                            return 1
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE'), false) == 1
            assert c.test(enumValue(loader, 'ONE'), true) == 2
            assert c.test(enumValue(loader, 'TWO'), false) == 2
            assert c.test(enumValue(loader, 'THREE'), false) == 3
        }
    }

    @Test
    void testSwitchFlowAnalysisHandlesTryCatchFinallyInCaseBodies() {
        assertNoUnexpectedUnreachableRethrowInEnumSwitchMethod('''
            int test(E e, boolean flag) {
                switch (e) {
                    case E.ONE:
                        try {
                            if (flag) return 1
                        } catch (RuntimeException ignored) {
                            return 2
                        } finally {
                        }
                    case E.TWO:
                        return 3
                    default:
                        return 4
                }
            }
        ''')
    }

    @Test
    void testSwitchFlowAnalysisHandlesTryFinallyWithoutCatch() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        try {
                            return 1
                        } finally {
                        }
                    default:
                        return 2
                }
            }
        ''') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 1
            assert c.test(enumValue(loader, 'TWO')) == 2
        }
    }

    @Test
    void testSwitchFlowAnalysisHandlesTryCatchFallthroughInCaseBodies() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        try {
                            throw new RuntimeException('boom')
                        } catch (RuntimeException ignored) {
                        } finally {
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
        }
    }

    @Test
    void testSwitchFlowAnalysisHandlesTryCatchWithoutFinallyAndLocalTypeMerges() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            Object test(E e) {
                switch (e) {
                    case E.ONE:
                        Object value
                        try {
                            value = System.nanoTime()
                            value = ''
                        } catch (RuntimeException ignored) {
                        }
                        return value
                    default:
                        return null
                }
            }
        ''') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == ''
            assert c.test(enumValue(loader, 'TWO')) == null
        }
    }

    @Test
    void testSwitchFlowAnalysisHandlesSynchronizedBlocksInCaseBodies() {
        assertNoUnreachableBytecodeInEnumSwitchMethod('''
            int test(E e, boolean flag) {
                switch (e) {
                    case E.ONE:
                        synchronized (this) {
                            if (flag) {
                                return 1
                            }
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''')
    }

    @Test
    void testSwitchFlowAnalysisHandlesInfiniteWhileWithBreakInCaseBodies() {
        assertRunEnumSwitchAssertions('''
            int test(E e, boolean stopEarly) {
                switch (e) {
                    case E.ONE:
                        while (true) {
                            if (stopEarly) break
                            return 1
                        }
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE'), false) == 1
            assert c.test(enumValue(loader, 'ONE'), true) == 2
            assert c.test(enumValue(loader, 'TWO'), false) == 2
            assert c.test(enumValue(loader, 'THREE'), false) == 3
        }
    }

    @Test
    void testLabeledBreakToSwitchInsideLoopStillAllowsNormalCompletion() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                int result = 0
                synchronized (this) {
                    outer:
                    switch (e) {
                        case E.ONE:
                            for (int i = 0; i < 3; i++) {
                                if (i == 1) break outer
                            }
                            return 99
                        default:
                            return -1
                        }
                    result = 7
                }
                return result
            }
        ''') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 7
            assert c.test(enumValue(loader, 'TWO')) == -1
        }
    }

    @Test
    void testEnumSwitchWithDoWhileContinueStillFallsThrough() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        do {
                            continue
                        } while (false)
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
            assert c.test(enumValue(loader, 'THREE')) == 3
        }
    }

    @Test
    void testEnumSwitchWithLabeledDoWhileContinueStillFallsThrough() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E e) {
                switch (e) {
                    case E.ONE:
                        loop:
                        do {
                            continue loop
                        } while (false)
                    case E.TWO:
                        return 2
                    default:
                        return 3
                }
            }
        ''', 'ONE, TWO, THREE') { loader ->
            def c = newTestSubject(loader)
            assert c.test(enumValue(loader, 'ONE')) == 2
            assert c.test(enumValue(loader, 'TWO')) == 2
            assert c.test(enumValue(loader, 'THREE')) == 3
        }
    }

    @Test
    void testSwitchFlowAnalysisKeepsOuterLabeledContinueAbrupt() {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions('''
            int test(E[] values) {
                int result = 0
                outer:
                for (E e : values) {
                    switch (e) {
                        case E.ONE:
                            do {
                                result += 1
                                continue outer
                            } while (false)
                        default:
                            result += 10
                    }
                    result += 100
                }
                return result
            }
        ''') { loader ->
            def c = newTestSubject(loader)
            def enumType = loadEnumType(loader)
            def pair = Array.newInstance(enumType, 2)
            Array.set(pair, 0, enumValue(loader, 'ONE'))
            Array.set(pair, 1, enumValue(loader, 'TWO'))
            def single = Array.newInstance(enumType, 1)
            Array.set(single, 0, enumValue(loader, 'ONE'))
            assert c.test(pair) == 111
            assert c.test(single) == 1
        }
    }

    private void assertNoUnreachableBytecodeInTestMethod(final String source) {
        def unreachable = compileAndFindUnreachableInstructions(classNamePattern: 'C', method: 'test', source)
        assert unreachable.isEmpty(): "Unexpected unreachable instructions: ${unreachable}\n${sequence}"
    }

    private void assertNoUnexpectedUnreachableRethrowInTestMethod(final String source) {
        def unexpected = compileAndFindUnreachableInstructions(classNamePattern: 'C', method: 'test', source)
                .findAll { !it.endsWith(':ATHROW') }
        assert unexpected.isEmpty(): "Unexpected unreachable instructions: ${unexpected}\n${sequence}"
    }

    private void assertNoUnreachableBytecodeAndRunAssertions(final String source, final Closure<?> assertions) {
        assertNoUnreachableBytecodeInTestMethod(source)
        assertRunAssertions(source, assertions)
    }

    private void assertNoUnreachableBytecodeInEnumSwitchMethod(final String methodSource, final String enumConstants = 'ONE, TWO') {
        assertNoUnreachableBytecodeInTestMethod(enumSwitchScript(methodSource, enumConstants))
    }

    private void assertNoUnexpectedUnreachableRethrowInEnumSwitchMethod(final String methodSource, final String enumConstants = 'ONE, TWO') {
        assertNoUnexpectedUnreachableRethrowInTestMethod(enumSwitchScript(methodSource, enumConstants))
    }

    private void assertNoUnreachableBytecodeAndRunEnumSwitchAssertions(final String methodSource, final Closure<?> assertions) {
        assertNoUnreachableBytecodeAndRunEnumSwitchAssertions(methodSource, 'ONE, TWO', assertions)
    }

    private void assertNoUnreachableBytecodeAndRunEnumSwitchAssertions(final String methodSource, final String enumConstants, final Closure<?> assertions) {
        assertNoUnreachableBytecodeAndRunAssertions(enumSwitchScript(methodSource, enumConstants), assertions)
    }

    private void assertRunEnumSwitchAssertions(final String methodSource, final Closure<?> assertions) {
        assertRunEnumSwitchAssertions(methodSource, 'ONE, TWO', assertions)
    }

    private void assertRunEnumSwitchAssertions(final String methodSource, final String enumConstants, final Closure<?> assertions) {
        assertRunAssertions(enumSwitchScript(methodSource, enumConstants), assertions)
    }

    private void assertRunAssertions(final String source, final Closure<?> assertions) {
        def loader = new GroovyClassLoader(this.class.classLoader)
        try {
            loader.parseClass(source, 'Groovy10156Runtime.groovy')
            assertions.call(loader)
        } finally {
            loader.close()
        }
    }

    private static String enumSwitchScript(final String methodSource, final String enumConstants = 'ONE, TWO') {
        """
            import groovy.transform.CompileStatic

            @CompileStatic
            enum E {
                ${enumConstants}
            }

            @CompileStatic
            class C {
            ${indent(methodSource.stripIndent().trim())}
            }
        """.stripIndent()
    }

    private static String indent(final String source, final int spaces = 4) {
        def prefix = ' ' * spaces
        source.readLines().collect { line -> line ? prefix + line : line }.join('\n')
    }

    private static Object newTestSubject(final GroovyClassLoader loader) {
        loader.loadClass('C').getDeclaredConstructor().newInstance()
    }

    private static Class<?> loadEnumType(final GroovyClassLoader loader) {
        loader.loadClass('E')
    }

    private static Object enumValue(final GroovyClassLoader loader, final String constantName) {
        Enum.valueOf(loadEnumType(loader), constantName)
    }

}
