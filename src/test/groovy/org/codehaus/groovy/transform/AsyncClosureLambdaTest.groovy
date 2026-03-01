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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for async closures and async lambda expressions.
 * <p>
 * Syntax:
 * <ul>
 *   <li>{@code async { body }} — immediately executes body async, returns Awaitable</li>
 *   <li>{@code async { x, y -> body }} — creates async closure, returns Closure&lt;Awaitable&gt;</li>
 *   <li>{@code async (x, y) -> { body }} — creates async lambda, returns Closure&lt;Awaitable&gt;</li>
 *   <li>Both support {@code yield return} for AsyncStream generation</li>
 * </ul>
 *
 * @since 6.0.0
 */
final class AsyncClosureLambdaTest {

    // =========================================================================
    // 1. Async Closure — immediate execution (no params)
    // =========================================================================

    @Test
    void testAsyncClosureImmediateExecution() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            def result = async { 42 }
            assert result instanceof Awaitable
            assert await(result) == 42
        '''
    }

    @Test
    void testAsyncClosureWithExplicitEmptyParams() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            def result = async { -> "hello" }
            assert result instanceof Awaitable
            assert await(result) == "hello"
        '''
    }

    @Test
    void testAsyncClosureRunsOnSeparateThread() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def mainThread = Thread.currentThread().name
            def awaitable = async { Thread.currentThread().name }
            def asyncThread = await(awaitable)
            assert asyncThread != mainThread
        '''
    }

    @Test
    void testAsyncClosureWithComputation() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def awaitable = async {
                def sum = 0
                for (i in 1..100) { sum += i }
                sum
            }
            assert await(awaitable) == 5050
        '''
    }

    @Test
    void testAsyncClosureWithAwaitInside() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def fetchValue() { return 10 }

            def result = async {
                def v = await(fetchValue())
                v * 2
            }
            assert await(result) == 20
        '''
    }

    @Test
    void testMultipleAsyncClosuresParallel() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def a = async { Thread.sleep(50); 1 }
            def b = async { Thread.sleep(50); 2 }
            def c = async { Thread.sleep(50); 3 }

            def results = awaitAll(a, b, c)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncClosureAssignedToVariable() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            def task = async { 100 + 200 }
            // Can use the Awaitable later
            assert task instanceof Awaitable
            def val = await(task)
            assert val == 300
        '''
    }

    @Test
    void testAsyncClosureReturnsNull() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def result = async { null }
            assert await(result) == null
        '''
    }

    // =========================================================================
    // 2. Async Closure — with parameters (deferred execution)
    // =========================================================================

    @Test
    void testAsyncClosureWithParams() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def asyncAdd = async { x, y -> x + y }
            def result = asyncAdd(10, 20)
            assert await(result) == 30
        '''
    }

    @Test
    void testAsyncClosureWithSingleParam() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def asyncDouble = async { n -> n * 2 }
            assert await(asyncDouble(21)) == 42
        '''
    }

    @Test
    void testAsyncClosureWithParamsCalledMultipleTimes() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def asyncSquare = async { n -> n * n }
            def r1 = await(asyncSquare(3))
            def r2 = await(asyncSquare(5))
            def r3 = await(asyncSquare(7))
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
            import static groovy.concurrent.AsyncUtils.*

            def asyncConcat = async { a, b, sep -> "${a}${sep}${b}" }
            assert await(asyncConcat("hello", "world", " ")) == "hello world"
        '''
    }

    @Test
    void testAsyncClosurePassedAsArgument() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def applyAsync(fn, x) {
                return await(fn(x))
            }

            def asyncInc = async { n -> n + 1 }
            assert applyAsync(asyncInc, 41) == 42
        '''
    }

    @Test
    void testAsyncClosureStoredInCollection() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

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
            import static groovy.concurrent.AsyncUtils.*

            def asyncDouble = async (n) -> { n * 2 }
            assert await(asyncDouble(21)) == 42
        '''
    }

    @Test
    void testAsyncLambdaWithMultipleParams() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def asyncAdd = async (a, b) -> { a + b }
            assert await(asyncAdd(10, 32)) == 42
        '''
    }

    @Test
    void testAsyncLambdaWithSingleExpression() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

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
            import static groovy.concurrent.AsyncUtils.*

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
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.AsyncStream

            def stream = async {
                yield return 1
                yield return 2
                yield return 3
            }
            assert stream instanceof AsyncStream

            def items = []
            while (await(stream.moveNext())) {
                items << stream.current
            }
            assert items == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncClosureGeneratorWithForAwait() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def collect() {
                def stream = async {
                    yield return "a"
                    yield return "b"
                    yield return "c"
                }
                def items = []
                for await (item in stream) {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def collect() {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def test() {
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

            def result = await(test())
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def collect() {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def caller() {
                def task = async { throw new java.io.IOException("async closure error") }
                try {
                    await task
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "async closure error"
        '''
    }

    @Test
    void testAsyncClosureWithParamsExceptionTransparency() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def caller() {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def caller() {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def caller() {
                def stream = async {
                    yield return 1
                    throw new java.io.IOException("generator error")
                }
                def items = []
                try {
                    for await (item in stream) {
                        items << item
                    }
                    assert false
                } catch (java.io.IOException e) {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def compute() {
                def a = await async { 10 }
                def b = await async { 20 }
                return a + b
            }

            assert await(compute()) == 30
        '''
    }

    @Test
    void testAwaitAsyncClosureWithParamsInAsyncMethod() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def compute() {
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
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def pipeline() {
                def step1 = async { 10 }
                def v1 = await step1

                def step2 = async { v1 * 2 }
                def v2 = await step2

                def step3 = async { v2 + 1 }
                return await step3
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
            import static groovy.concurrent.AsyncUtils.*

            def task = async { [1, 2, 3].collect { it * it } }
            assert await(task) == [1, 4, 9]
        '''
    }

    @Test
    void testAsyncClosureWithMapResult() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def task = async { [name: "Groovy", version: 6] }
            def result = await(task)
            assert result.name == "Groovy"
            assert result.version == 6
        '''
    }

    @Test
    void testAsyncClosureWithStringInterpolation() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def greet = async { name -> "Hello, ${name}!" }
            assert await(greet("World")) == "Hello, World!"
        '''
    }

    // =========================================================================
    // 9. Async closure/lambda used with awaitAll/awaitAny
    // =========================================================================

    @Test
    void testAwaitAllWithAsyncClosures() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def results = awaitAll(
                async { 1 },
                async { 2 },
                async { 3 }
            )
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitAnyWithAsyncClosures() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def result = awaitAny(
                async { Thread.sleep(100); "slow" },
                async { "fast" }
            )
            // "fast" should usually win, but either is valid
            assert result == "fast" || result == "slow"
        '''
    }

    @Test
    void testAwaitAllSettledWithAsyncClosures() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def results = awaitAllSettled(
                async { 42 },
                async { throw new RuntimeException("fail") }
            )
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
            import static groovy.concurrent.AsyncUtils.*

            def x = 100
            def task = async { x + 1 }
            assert await(task) == 101
        '''
    }

    @Test
    void testAsyncClosureWithParamCapturingOuter() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def base = 1000
            def asyncAdd = async { n -> base + n }
            assert await(asyncAdd(42)) == 1042
        '''
    }

    @Test
    void testNestedAsyncClosures() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def outer = async {
                def inner = async { 42 }
                await(inner)
            }
            assert await(outer) == 42
        '''
    }

    @Test
    void testAsyncClosureInLoopBody() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def tasks = (1..5).collect { n -> async { n * n } }
            def results = awaitAll(tasks as Object[])
            assert results == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testAsyncClosureWithMethodCallChaining() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            def task = async { "hello world" }
            assert task instanceof Awaitable
            def chained = task.then { it.toUpperCase() }
            assert await(chained) == "HELLO WORLD"
        '''
    }

    @Test
    void testAsyncClosureWithExceptionally() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def task = async { throw new RuntimeException("boom") }
            def recovered = task.exceptionally { err -> "recovered: ${err.message}" }
            assert await(recovered) == "recovered: boom"
        '''
    }

    @Test
    void testAsyncClosureGeneratorFactoryPattern() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            @Async
            def collect() {
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
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CancellationException

            def task = async { Thread.sleep(100); 42 }
            task.cancel()
            assert task.isCancelled()
            assert task.isCompletedExceptionally()
            try {
                await(task)
                assert false
            } catch (CancellationException e) {
                // expected
            }
        '''
    }
}
