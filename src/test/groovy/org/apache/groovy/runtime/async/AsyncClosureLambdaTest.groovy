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
package org.apache.groovy.runtime.async

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for async closures and async lambda expressions.
 * <p>
 * Syntax:
 * <ul>
 *   <li>{@code async { body }} — returns Closure&lt;Awaitable&gt; that must be called with () to execute</li>
 *   <li>{@code async { x, y -> body }} — creates async closure, returns Closure&lt;Awaitable&gt;</li>
 *   <li>{@code async (x, y) -> { body }} — creates async lambda, returns Closure&lt;Awaitable&gt;</li>
 *   <li>Both support {@code yield return} for AsyncStream generation</li>
 * </ul>
 *
 * @since 6.0.0
 */
final class AsyncClosureLambdaTest {

    // =========================================================================
    // 1. Async Closure — no params (returns Closure that must be called)
    // =========================================================================

    @Test
    void testAsyncClosureImmediateExecution() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = async { 42 }
            assert result instanceof Closure
            assert result() instanceof Awaitable
            assert await(result()) == 42
        '''
    }

    @Test
    void testAsyncClosureWithExplicitEmptyParams() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = async { -> "hello" }
            assert result instanceof Closure
            assert await(result()) == "hello"
        '''
    }

    @Test
    void testAsyncClosureRunsOnSeparateThread() {
        assertScript '''
            def mainThread = Thread.currentThread().name
            def awaitable = async { Thread.currentThread().name }
            def asyncThread = await awaitable()
            assert asyncThread != mainThread
        '''
    }

    @Test
    void testAsyncClosureWithComputation() {
        assertScript '''
            def awaitable = async {
                def sum = 0
                for (i in 1..100) { sum += i }
                sum
            }
            assert await(awaitable()) == 5050
        '''
    }

    @Test
    void testAsyncClosureWithAwaitInside() {
        assertScript '''
            async fetchValue() { return 10 }

            def result = async {
                def v = await fetchValue()
                v * 2
            }
            assert await(result()) == 20
        '''
    }

    @Test
    void testMultipleAsyncClosuresParallel() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = async { Thread.sleep(50); 1 }
            def b = async { Thread.sleep(50); 2 }
            def c = async { Thread.sleep(50); 3 }

            def results = await Awaitable.all(a(), b(), c())
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncClosureAssignedToVariable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async { 100 + 200 }
            // Can use the Awaitable later
            assert task instanceof Closure
            def val = await task()
            assert val == 300
        '''
    }

    @Test
    void testAsyncClosureReturnsNull() {
        assertScript '''
            def result = async { null }
            assert await(result()) == null
        '''
    }

    // =========================================================================
    // 2. Async Closure — with parameters (deferred execution)
    // =========================================================================

    @Test
    void testAsyncClosureWithParams() {
        assertScript '''
            def asyncAdd = async { x, y -> x + y }
            def result = asyncAdd(10, 20)
            assert await(result) == 30
        '''
    }

    @Test
    void testAsyncClosureWithSingleParam() {
        assertScript '''
            def asyncDouble = async { n -> n * 2 }
            assert await(asyncDouble(21)) == 42
        '''
    }

    @Test
    void testAsyncClosureWithParamsCalledMultipleTimes() {
        assertScript '''
            def asyncSquare = async { n -> n * n }
            def r1 = await asyncSquare(3)
            def r2 = await asyncSquare(5)
            def r3 = await asyncSquare(7)
            assert r1 == 9
            assert r2 == 25
            assert r3 == 49
        '''
    }

    @Test
    void testAsyncClosureWithParamsIsClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def asyncFn = async { x -> x.toUpperCase() }
            assert asyncFn instanceof Closure
            def result = asyncFn("hello")
            assert result instanceof Awaitable
        '''
    }

    @Test
    void testAsyncClosureWithParamsAndStringOps() {
        assertScript '''
            def asyncConcat = async { a, b, sep -> "${a}${sep}${b}" }
            assert await(asyncConcat("hello", "world", " ")) == "hello world"
        '''
    }

    @Test
    void testAsyncClosurePassedAsArgument() {
        assertScript '''
            def applyAsync(fn, x) {
                return await fn(x)
            }

            def asyncInc = async { n -> n + 1 }
            assert applyAsync(asyncInc, 41) == 42
        '''
    }

    @Test
    void testAsyncClosureStoredInCollection() {
        assertScript '''
            def ops = [
                add: async { a, b -> a + b },
                mul: async { a, b -> a * b },
                sub: async { a, b -> a - b }
            ]

            assert await(ops.add(3, 4)) == 7
            assert await(ops.mul(3, 4)) == 12
            assert await(ops.sub(10, 4)) == 6
        '''
    }

    // =========================================================================
    // 3. Async Lambda Expression
    // =========================================================================

    @Test
    void testAsyncLambdaWithSingleParam() {
        assertScript '''
            def asyncDouble = async (n) -> { n * 2 }
            assert await(asyncDouble(21)) == 42
        '''
    }

    @Test
    void testAsyncLambdaWithMultipleParams() {
        assertScript '''
            def asyncAdd = async (a, b) -> { a + b }
            assert await(asyncAdd(10, 32)) == 42
        '''
    }

    @Test
    void testAsyncLambdaWithSingleExpression() {
        assertScript '''
            def asyncInc = async (n) -> n + 1
            assert await(asyncInc(41)) == 42
        '''
    }

    @Test
    void testAsyncLambdaIsClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fn = async (x) -> { x * x }
            assert fn instanceof Closure
            assert fn(5) instanceof Awaitable
        '''
    }

    @Test
    void testAsyncLambdaCalledMultipleTimes() {
        assertScript '''
            def factorial = async (n) -> {
                def result = 1
                for (i in 2..n) { if (n >= 2) result *= i }
                result
            }

            assert await(factorial(5)) == 120
            assert await(factorial(6)) == 720
            assert await(factorial(10)) == 3628800
        '''
    }

    // =========================================================================
    // 4. Async Closure with yield return (AsyncStream generator)
    // =========================================================================

    @Test
    void testAsyncClosureGeneratorImmediate() {
        assertScript '''
            import groovy.concurrent.AsyncStream

            def gen = async {
                yield return 1
                yield return 2
                yield return 3
            }
            assert gen instanceof Closure : "Expected Closure but got ${gen.getClass().name}"
            def stream = gen()
            assert stream instanceof AsyncStream

            def items = []
            while (await stream.moveNext()) {
                items << stream.current
            }
            assert items == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncClosureGeneratorWithForAwait() {
        assertScript '''
            async collect() {
                def gen = async {
                    yield return "a"
                    yield return "b"
                    yield return "c"
                }
                def items = []
                for await (item in gen()) {
                    items << item
                }
                return items
            }

            assert await(collect()) == ["a", "b", "c"]
        '''
    }

    @Test
    void testAsyncClosureGeneratorWithParams() {
        assertScript '''
            async collect() {
                def range = async { start, end ->
                    for (i in start..end) {
                        yield return i
                    }
                }
                def items = []
                for await (item in range(1, 5)) {
                    items << item
                }
                return items
            }

            assert await(collect()) == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testAsyncClosureGeneratorWithParamsCalledMultipleTimes() {
        assertScript '''
            async test() {
                def repeat = async { value, times ->
                    for (i in 1..times) {
                        yield return value
                    }
                }
                def r1 = []
                for await (item in repeat("x", 3)) { r1 << item }
                def r2 = []
                for await (item in repeat("y", 2)) { r2 << item }
                return [r1, r2]
            }

            def result = await test()
            assert result[0] == ["x", "x", "x"]
            assert result[1] == ["y", "y"]
        '''
    }

    // =========================================================================
    // 5. Async Lambda with yield return
    // =========================================================================

    @Test
    void testAsyncLambdaGeneratorWithForAwait() {
        assertScript '''
            async collect() {
                def squares = async (n) -> {
                    for (i in 1..n) {
                        yield return i * i
                    }
                }
                def items = []
                for await (item in squares(4)) {
                    items << item
                }
                return items
            }

            assert await(collect()) == [1, 4, 9, 16]
        '''
    }

    // =========================================================================
    // 6. Exception handling in async closures/lambdas
    // =========================================================================

    @Test
    void testAsyncClosureExceptionTransparency() {
        assertScript '''
            import java.io.IOException

            async caller() {
                def task = async { throw new IOException("async closure error") }
                try {
                    await task()
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "async closure error"
        '''
    }

    @Test
    void testAsyncClosureWithParamsExceptionTransparency() {
        assertScript '''
            async caller() {
                def asyncDiv = async { a, b ->
                    if (b == 0) throw new ArithmeticException("division by zero")
                    a / b
                }
                try {
                    await asyncDiv(10, 0)
                    assert false
                } catch (ArithmeticException e) {
                    return e.message
                }
            }

            assert await(caller()) == "division by zero"
        '''
    }

    @Test
    void testAsyncLambdaExceptionHandling() {
        assertScript '''
            async caller() {
                def failOp = async (msg) -> { throw new IllegalStateException(msg) }
                try {
                    await failOp("lambda error")
                    assert false
                } catch (IllegalStateException e) {
                    return e.message
                }
            }

            assert await(caller()) == "lambda error"
        '''
    }

    @Test
    void testAsyncClosureGeneratorExceptionPropagation() {
        assertScript '''
            import java.io.IOException

            async caller() {
                def gen = async {
                    yield return 1
                    throw new IOException("generator error")
                }
                def items = []
                try {
                    for await (item in gen()) {
                        items << item
                    }
                    assert false
                } catch (IOException e) {
                    return "${items}:${e.message}"
                }
            }

            assert await(caller()) == "[1]:generator error"
        '''
    }

    // =========================================================================
    // 7. Async closure/lambda used with await syntax
    // =========================================================================

    @Test
    void testAwaitAsyncClosureInAsyncMethod() {
        assertScript '''
            async compute() {
                def taskA = async { 10 }
                def a = await taskA()
                def taskB = async { 20 }
                def b = await taskB()
                return a + b
            }

            assert await(compute()) == 30
        '''
    }

    @Test
    void testAwaitAsyncClosureWithParamsInAsyncMethod() {
        assertScript '''
            async compute() {
                def multiply = async { x, y -> x * y }
                def a = await multiply(6, 7)
                return a
            }

            assert await(compute()) == 42
        '''
    }

    @Test
    void testAsyncClosuresComposed() {
        assertScript '''
            async pipeline() {
                def step1 = async { 10 }
                def v1 = await step1()

                def step2 = async { v1 * 2 }
                def v2 = await step2()

                def step3 = async { v2 + 1 }
                return await step3()
            }

            assert await(pipeline()) == 21
        '''
    }

    // =========================================================================
    // 8. Async closure/lambda with complex types
    // =========================================================================

    @Test
    void testAsyncClosureWithListResult() {
        assertScript '''
            def task = async { [1, 2, 3].collect { it * it } }
            assert await(task()) == [1, 4, 9]
        '''
    }

    @Test
    void testAsyncClosureWithMapResult() {
        assertScript '''
            def task = async { [name: "Groovy", version: 6] }
            def result = await task()
            assert result.name == "Groovy"
            assert result.version == 6
        '''
    }

    @Test
    void testAsyncClosureWithStringInterpolation() {
        assertScript '''
            def greet = async { name -> "Hello, ${name}!" }
            assert await(greet("World")) == "Hello, World!"
        '''
    }

    // =========================================================================
    // 9. Async closure/lambda used with Awaitable.all/any/allSettled
    // =========================================================================

    @Test
    void testAwaitAllWithAsyncClosures() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def t1 = async { 1 }
            def t2 = async { 2 }
            def t3 = async { 3 }
            def results = await Awaitable.all(t1(), t2(), t3())
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitAnyWithAsyncClosures() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def slowTask = async { Thread.sleep(100); "slow" }
            def fastTask = async { "fast" }
            def result = await Awaitable.any(slowTask(), fastTask())
            // "fast" should usually win, but either is valid
            assert result == "fast" || result == "slow"
        '''
    }

    @Test
    void testAwaitAllSettledWithAsyncClosures() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = async { 42 }
            def fail = async { throw new RuntimeException("fail") }
            def results = await Awaitable.allSettled(ok(), fail())
            assert results[0].isSuccess()
            assert results[0].value == 42
            assert results[1].isFailure()
            assert results[1].error instanceof RuntimeException
        '''
    }

    // =========================================================================
    // 10. Edge cases
    // =========================================================================

    @Test
    void testAsyncClosureCapturingOuterVariable() {
        assertScript '''
            def x = 100
            def task = async { x + 1 }
            assert await(task()) == 101
        '''
    }

    @Test
    void testAsyncClosureWithParamCapturingOuter() {
        assertScript '''
            def base = 1000
            def asyncAdd = async { n -> base + n }
            assert await(asyncAdd(42)) == 1042
        '''
    }

    @Test
    void testNestedAsyncClosures() {
        assertScript '''
            def outer = async {
                def inner = async { 42 }
                await inner()
            }
            assert await(outer()) == 42
        '''
    }

    @Test
    void testAsyncClosureInLoopBody() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def tasks = (1..5).collect { n ->
                def t = async { n * n }
                t()
            }
            def results = await Awaitable.all(tasks as Object[])
            assert results == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testAsyncClosureWithMethodCallChaining() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async { "hello world" }
            assert task instanceof Closure
            def chained = task().then { it.toUpperCase() }
            assert await(chained) == "HELLO WORLD"
        '''
    }

    @Test
    void testAsyncClosureWithExceptionally() {
        assertScript '''
            def task = async { throw new RuntimeException("boom") }
            def recovered = task().exceptionally { err -> "recovered: ${err.message}" }
            assert await(recovered) == "recovered: boom"
        '''
    }

    @Test
    void testAsyncClosureGeneratorFactoryPattern() {
        assertScript '''
            async collect() {
                // Factory that creates fibonacci async streams
                def fibonacci = async { limit ->
                    def a = 0, b = 1
                    for (i in 0..<limit) {
                        yield return a
                        def temp = a + b
                        a = b
                        b = temp
                    }
                }
                def items = []
                for await (item in fibonacci(8)) {
                    items << item
                }
                return items
            }

            assert await(collect()) == [0, 1, 1, 2, 3, 5, 8, 13]
        '''
    }

    @Test
    void testAsyncClosureCancellation() {
        assertScript '''
            import java.util.concurrent.CancellationException

            def taskDef = async { Thread.sleep(100); 42 }
            def task = taskDef()
            task.cancel()
            assert task.isCancelled()
            assert task.isCompletedExceptionally()
            try {
                await task
                assert false
            } catch (CancellationException e) {
                assert e instanceof CancellationException
            }
        '''
    }

    // ===== Async closure tests =====

    @Test
    void testAsyncClosureExplicitCall() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async { 1 + 2 }
            assert await(task()) == 3
        '''
    }

    @Test
    void testAsyncClosureWithParameters() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def multiply = async { int a, int b -> a * b }
            assert await(multiply(6, 7)) == 42
        '''
    }

    // =========================================================================
    // Edge-case and error-path coverage
    // =========================================================================

    @Test
    void testNestedAsyncClosuresDepthTracking() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async nested() {
                def outer = async { ->
                    def inner = async { ->
                        await Awaitable.of(42)
                    }
                    await inner()
                }
                await outer()
            }

            assert nested().get() == 42
        '''
    }

    @Test
    void testAsyncClosureWithParamsYieldReturn() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async paramYield() {
                def gen = async { int start, int count ->
                    for (int i = start; i < start + count; i++) {
                        yield return i
                    }
                }

                def items = []
                for await (item in gen(10, 3)) {
                    items << item
                }
                assert items == [10, 11, 12]
            }

            paramYield().get()
        '''
    }

}
