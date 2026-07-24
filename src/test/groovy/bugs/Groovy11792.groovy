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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-11792: for-in loop variables captured by closures, lambdas, or AICs
 * (GROOVY-11818) should observe the value from the iteration in which the
 * capture was created, not the final loop value.
 * <p>
 * Dynamic lambdas compile as closures ({@code LambdaWriter} → {@code ClosureWriter}).
 * Under {@code @CompileStatic}, SAM-targeted lambdas use
 * {@code StaticTypesLambdaWriter} (native functional-interface bytecode) but still
 * share for-in variables via {@code groovy.lang.Reference}, so the same per-iteration
 * recapture policy applies.
 * <p>
 * Classgen patterns (fresh {@code Reference} vs in-place {@code Reference#set}) are
 * asserted in {@link Groovy11792BytecodeTest}.
 */
@CompileStatic
final class Groovy11792 {

    /**
     * Evaluates {@code script} with historical for-in capture
     * ({@link CompilerConfiguration#setForLoopCaptureEnabled(boolean) setForLoopCaptureEnabled(false)}).
     */
    private static void evaluateWithLegacyCapture(final String script) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.setForLoopCaptureEnabled(false)
        new GroovyShell(config).evaluate(script)
    }

    @Test
    void testForInClosureCapture() {
        assertScript '''
            import java.util.function.Supplier

            def numbers = [1, 2, 3]
            List<Supplier> suppliers = []
            for (n in numbers) {
                Supplier s = { n * n }
                suppliers << s
            }
            assert suppliers.collect { it.get() } == [1, 4, 9]
        '''
    }

    @Test
    void testForInClosureCaptureColonSyntax() {
        assertScript '''
            def numbers = [1, 2, 3]
            def closures = []
            for (Integer n : numbers) {
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 4, 9]
        '''
    }

    @Test
    void testForInClosureCaptureCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                List<Integer> numbers = [1, 2, 3]
                List<Closure<Integer>> closures = []
                for (Integer n in numbers) {
                    closures << { n * n }
                }
                return closures.collect { it.call() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    // GROOVY-11818
    @Test
    void testForInAnonymousInnerClassCapture() {
        assertScript '''
            interface Face {
                int get()
            }
            def numbers = [1, 2, 3]
            List<Face> faces = []
            for (n in numbers) {
                faces << new Face() {
                    int get() { n * n }
                }
            }
            assert faces.collect { it.get() } == [1, 4, 9]
        '''
    }

    // GROOVY-11818
    @Test
    void testForInAnonymousInnerClassCaptureCompileStatic() {
        assertScript '''
            interface Face {
                int get()
            }
            @groovy.transform.CompileStatic
            class Runner {
                static List<Integer> run() {
                    List<Integer> numbers = [1, 2, 3]
                    List<Face> faces = []
                    for (Integer n in numbers) {
                        faces << new Face() {
                            int get() { n * n }
                        }
                    }
                    return faces.collect { it.get() }
                }
            }
            assert Runner.run() == [1, 4, 9]
        '''
    }

    @Test
    void testForInIndexedCapture() {
        assertScript '''
            def items = ['a', 'b', 'c']
            def valueClosures = []
            def indexClosures = []
            for (i, v in items) {
                valueClosures << { v }
                indexClosures << { i }
            }
            assert valueClosures.collect { it() } == ['a', 'b', 'c']
            assert indexClosures.collect { it() } == [0, 1, 2]
        '''
    }

    @Test
    void testForInIndexedCaptureCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                List<String> items = ['a', 'b', 'c']
                List<Closure<String>> valueClosures = []
                List<Closure<Integer>> indexClosures = []
                for (int i, String v in items) {
                    valueClosures << { v }
                    indexClosures << { i }
                }
                return [valueClosures.collect { it.call() }, indexClosures.collect { it.call() }]
            }
            def (values, indexes) = execute()
            assert values == ['a', 'b', 'c']
            assert indexes == [0, 1, 2]
        '''
    }

    @Test
    void testForInArrayCaptureCompileStatic() {
        // Exercises StaticTypesStatementWriter#writeOptimizedForEachLoop
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                int[] numbers = [1, 2, 3] as int[]
                List<Closure<Integer>> closures = []
                for (int n in numbers) {
                    closures << { n * n }
                }
                return closures.collect { it.call() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInArrayIndexedCaptureCompileStatic() {
        // SC array path + shared index (incrementForLoopIndexVariable)
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                int[] numbers = [10, 20, 30] as int[]
                List<Closure<Integer>> valueClosures = []
                List<Closure<Integer>> indexClosures = []
                for (int i, int n in numbers) {
                    valueClosures << { n }
                    indexClosures << { i }
                }
                return [valueClosures.collect { it.call() }, indexClosures.collect { it.call() }]
            }
            def (values, indexes) = execute()
            assert values == [10, 20, 30]
            assert indexes == [0, 1, 2]
        '''
    }

    @Test
    void testForInEnumerationCaptureCompileStatic() {
        // Exercises StaticTypesStatementWriter#writeEnumerationBasedForEachLoop
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                Enumeration<Integer> numbers = Collections.enumeration([1, 2, 3])
                List<Closure<Integer>> closures = []
                for (Integer n in numbers) {
                    closures << { n * n }
                }
                return closures.collect { it.call() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInEnumerationIndexedCaptureCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            def execute() {
                Enumeration<Integer> numbers = Collections.enumeration([10, 20, 30])
                List<Closure<Integer>> valueClosures = []
                List<Closure<Integer>> indexClosures = []
                for (int i, Integer n in numbers) {
                    valueClosures << { n }
                    indexClosures << { i }
                }
                return [valueClosures.collect { it.call() }, indexClosures.collect { it.call() }]
            }
            def (values, indexes) = execute()
            assert values == [10, 20, 30]
            assert indexes == [0, 1, 2]
        '''
    }

    @Test
    void testForInLambdaSamCapture() {
        // Dynamic mode: lambda → ClosureWriter path with SAM assignment
        assertScript '''
            import java.util.function.Supplier

            def numbers = [1, 2, 3]
            List suppliers = []
            for (n in numbers) {
                Supplier s = () -> n * n
                suppliers << s
            }
            assert suppliers.collect { it.get() } == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaAsClosureCapture() {
        // Dynamic mode without SAM target: lambda is a Closure
        assertScript '''
            def numbers = [1, 2, 3]
            def closures = []
            for (n in numbers) {
                closures << (() -> n * n)
            }
            assert closures.collect { it() } == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaSamCaptureCompileStatic() {
        // SC lambda / SAM conversion capturing the for-in variable
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Integer> numbers = [1, 2, 3]
                List<Supplier<Integer>> suppliers = []
                for (Integer n in numbers) {
                    Supplier<Integer> s = () -> n * n
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaIndexedCaptureCompileStatic() {
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<String> items = ['a', 'b', 'c']
                List<Supplier<String>> values = []
                List<Supplier<Integer>> indexes = []
                for (int i, String v in items) {
                    Supplier<String> vs = () -> v
                    Supplier<Integer> is = () -> i
                    values << vs
                    indexes << is
                }
                return [values.collect { it.get() }, indexes.collect { it.get() }]
            }
            def (v, i) = execute()
            assert v == ['a', 'b', 'c']
            assert i == [0, 1, 2]
        '''
    }

    @Test
    void testForInLambdaArrayCaptureCompileStatic() {
        // StaticTypesStatementWriter#writeOptimizedForEachLoop + native lambda
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                int[] numbers = [1, 2, 3] as int[]
                List<Supplier<Integer>> suppliers = []
                for (int n in numbers) {
                    Supplier<Integer> s = () -> n * n
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaEnumerationCaptureCompileStatic() {
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                Enumeration<Integer> numbers = Collections.enumeration([1, 2, 3])
                List<Supplier<Integer>> suppliers = []
                for (Integer n in numbers) {
                    Supplier<Integer> s = () -> n * n
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaNestedCaptureCompileStatic() {
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Supplier<Integer>> suppliers = []
                for (Integer i in [1, 2]) {
                    for (Integer j in [10, 20]) {
                        Supplier<Integer> s = () -> i + j
                        suppliers << s
                    }
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [11, 21, 12, 22]
        '''
    }

    @Test
    void testForInLambdaSameIterationMutationCompileStatic() {
        // Mutable Reference within one iteration remains shared across captures
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Supplier<Integer>> suppliers = []
                for (Integer n in [1, 2, 3]) {
                    Supplier<Integer> s = () -> n
                    n = n + 10
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [11, 12, 13]
        '''
    }

    @Test
    void testForInLambdaFunctionCaptureCompileStatic() {
        assertScript '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
            def execute() {
                List<Function<Integer, Integer>> fns = []
                for (Integer n in [1, 2, 3]) {
                    Function<Integer, Integer> f = (Integer x) -> n + x
                    fns << f
                }
                return fns.collect { it.apply(10) }
            }
            assert execute() == [11, 12, 13]
        '''
    }

    @Test
    void testForInLambdaDirectListAddCompileStatic() {
        // SAM conversion via typed List#add argument (no intermediate local)
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Supplier<Integer>> suppliers = new ArrayList<>()
                for (Integer n in [1, 2, 3]) {
                    suppliers.add(() -> n * n)
                }
                List out = []
                for (Supplier<Integer> s : suppliers) {
                    out << s.get()
                }
                return out
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testForInLambdaColonSyntaxCompileStatic() {
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Integer> numbers = [1, 2, 3]
                List<Supplier<Integer>> suppliers = []
                for (Integer n : numbers) {
                    Supplier<Integer> s = () -> n * n
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [1, 4, 9]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutLambda() {
        evaluateWithLegacyCapture '''
            import java.util.function.Supplier

            def suppliers = []
            for (n in [1, 2, 3]) {
                Supplier s = () -> n * n
                suppliers << s
            }
            assert suppliers.collect { it.get() } == [9, 9, 9]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutLambdaCompileStatic() {
        evaluateWithLegacyCapture '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Integer> numbers = [1, 2, 3]
                List<Supplier<Integer>> suppliers = []
                for (Integer n in numbers) {
                    Supplier<Integer> s = () -> n * n
                    suppliers << s
                }
                return suppliers.collect { it.get() }
            }
            assert execute() == [9, 9, 9]
        '''
    }

    @Test
    void testNestedForInCapture() {
        assertScript '''
            def closures = []
            for (i in [1, 2]) {
                for (j in [10, 20]) {
                    closures << { i + j }
                }
            }
            assert closures.collect { it() } == [11, 21, 12, 22]
        '''
    }

    @Test
    void testSameIterationMutationStillShared() {
        // Closures created in the same iteration still share one Reference,
        // so an assignment after capture remains visible within that iteration.
        assertScript '''
            def closures = []
            for (n in [1, 2, 3]) {
                def c = { n }
                n = n + 10
                closures << c
            }
            assert closures.collect { it() } == [11, 12, 13]
        '''
    }

    @Test
    void testNonSharedLoopVariableUnchanged() {
        // Loop variable not captured by a deferred closure — plain assignment path.
        assertScript '''
            def sum = 0
            for (n in [1, 2, 3]) {
                sum += n
            }
            assert sum == 6
        '''
    }

    @Test
    void testLegacySharedCaptureOptOut() {
        evaluateWithLegacyCapture '''
            import java.util.function.Supplier

            def numbers = [1, 2, 3]
            List suppliers = []
            for (n in numbers) {
                Supplier s = { n * n }
                suppliers << s
            }
            // Historical behaviour: every supplier sees the final loop value.
            assert suppliers.collect { it.get() } == [9, 9, 9]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutIndexed() {
        evaluateWithLegacyCapture '''
            def items = ['a', 'b', 'c']
            def valueClosures = []
            def indexClosures = []
            for (i, v in items) {
                valueClosures << { v }
                indexClosures << { i }
            }
            assert valueClosures.collect { it() } == ['c', 'c', 'c']
            assert indexClosures.collect { it() } == [2, 2, 2]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutArrayCompileStatic() {
        evaluateWithLegacyCapture '''
            @groovy.transform.CompileStatic
            def execute() {
                int[] numbers = [1, 2, 3] as int[]
                List<Closure<Integer>> closures = []
                for (int n in numbers) {
                    closures << { n * n }
                }
                return closures.collect { it.call() }
            }
            assert execute() == [9, 9, 9]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutIndexedCompileStatic() {
        // SC indexed for-in with shared index uses storeForLoopVariable after increment
        evaluateWithLegacyCapture '''
            @groovy.transform.CompileStatic
            def execute() {
                List<String> items = ['a', 'b', 'c']
                List<Closure<String>> valueClosures = []
                List<Closure<Integer>> indexClosures = []
                for (int i, String v in items) {
                    valueClosures << { v }
                    indexClosures << { i }
                }
                return [valueClosures.collect { it.call() }, indexClosures.collect { it.call() }]
            }
            def (values, indexes) = execute()
            assert values == ['c', 'c', 'c']
            assert indexes == [2, 2, 2]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutEnumerationCompileStatic() {
        evaluateWithLegacyCapture '''
            @groovy.transform.CompileStatic
            def execute() {
                Enumeration<Integer> numbers = Collections.enumeration([1, 2, 3])
                List<Closure<Integer>> closures = []
                for (Integer n in numbers) {
                    closures << { n * n }
                }
                return closures.collect { it.call() }
            }
            assert execute() == [9, 9, 9]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutArrayIndexedCompileStatic() {
        // SC optimized array path + shared index with recapture disabled
        evaluateWithLegacyCapture '''
            @groovy.transform.CompileStatic
            def execute() {
                int[] numbers = [10, 20, 30] as int[]
                List<Closure<Integer>> valueClosures = []
                List<Closure<Integer>> indexClosures = []
                for (int i, int n in numbers) {
                    valueClosures << { n }
                    indexClosures << { i }
                }
                return [valueClosures.collect { it.call() }, indexClosures.collect { it.call() }]
            }
            def (values, indexes) = execute()
            assert values == [30, 30, 30]
            assert indexes == [2, 2, 2]
        '''
    }

    @Test
    void testForInRangeCapture() {
        assertScript '''
            def closures = []
            for (n in 1..3) {
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 4, 9]
        '''
    }

    @Test
    void testForInEmptyCollection() {
        assertScript '''
            def closures = []
            for (n in []) {
                closures << { n }
            }
            assert closures.isEmpty()
        '''
    }

    @Test
    void testForInSingleElementCapture() {
        assertScript '''
            def closures = []
            for (n in [7]) {
                closures << { n * 2 }
            }
            assert closures.collect { it() } == [14]
        '''
    }

    @Test
    void testForInCaptureWithContinue() {
        // continue must not break per-iteration capture for later iterations
        assertScript '''
            def closures = []
            for (n in [1, 2, 3, 4]) {
                if (n % 2 == 0) continue
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 9]
        '''
    }

    @Test
    void testForInCaptureWithBreak() {
        assertScript '''
            def closures = []
            for (n in [1, 2, 3, 4]) {
                if (n == 3) break
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 4]
        '''
    }

    @Test
    void testAllOptimizationsOffDoesNotDisableForLoopCapture() {
        // "all" -> false is a performance switch; for-in recapture stays on
        def config = new CompilerConfiguration()
        config.optimizationOptions['all'] = Boolean.FALSE
        assert config.isForLoopCaptureEnabled()
        def shell = new GroovyShell(config)
        shell.evaluate '''
            def closures = []
            for (n in [1, 2, 3]) {
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 4, 9]
        '''
    }

    @Test
    void testOuterSharedVariableStillMutable() {
        // Non-loop shared variables must continue to use a single Reference.
        assertScript '''
            def x = 1
            def c = { x }
            x = 2
            assert c() == 2
        '''
    }

    @Test
    void testEachStillPerCallBinding() {
        assertScript '''
            def closures = []
            [1, 2, 3].each { n ->
                closures << { n * n }
            }
            assert closures.collect { it() } == [1, 4, 9]
        '''
    }

    @Test
    void testClassicForLoopCaptureStillShared() {
        // Classic C-style for is intentionally unchanged (single shared binding).
        assertScript '''
            def closures = []
            for (int i = 0; i < 3; i++) {
                closures << { i }
            }
            assert closures.collect { it() } == [3, 3, 3]
        '''
    }

    @Test
    void testWhileLoopCaptureStillShared() {
        // while loops are intentionally unchanged (single shared binding).
        assertScript '''
            def closures = []
            def i = 0
            while (i < 3) {
                closures << { i }
                i++
            }
            assert closures.collect { it() } == [3, 3, 3]
        '''
    }

    @Test
    void testForInLambdaIndexedCapture() {
        assertScript '''
            import java.util.function.Supplier

            def values = []
            def indexes = []
            for (i, v in ['a', 'b', 'c']) {
                Supplier vs = () -> v
                Supplier is = () -> i
                values << vs
                indexes << is
            }
            assert values.collect { it.get() } == ['a', 'b', 'c']
            assert indexes.collect { it.get() } == [0, 1, 2]
        '''
    }

    @Test
    void testForInLambdaDualCaptureSameIterationMutation() {
        // Two lambdas in one iteration share one Reference; mutation is visible to both.
        assertScript '''
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def execute() {
                List<Supplier<Integer>> a = []
                List<Supplier<Integer>> b = []
                for (Integer n in [1, 2, 3]) {
                    Supplier<Integer> sa = () -> n
                    n = n + 100
                    Supplier<Integer> sb = () -> n
                    a << sa
                    b << sb
                }
                return [a.collect { it.get() }, b.collect { it.get() }]
            }
            def (ra, rb) = execute()
            assert ra == [101, 102, 103]
            assert rb == [101, 102, 103]
        '''
    }

    @Test
    void testLegacySharedCaptureOptOutAnonymousInnerClass() {
        evaluateWithLegacyCapture '''
            interface Face {
                int get()
            }
            def faces = []
            for (n in [1, 2, 3]) {
                faces << new Face() {
                    int get() { n * n }
                }
            }
            assert faces.collect { it.get() } == [9, 9, 9]
        '''
    }
}
