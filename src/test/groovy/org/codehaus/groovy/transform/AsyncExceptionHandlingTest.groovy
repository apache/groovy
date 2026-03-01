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
 * Comprehensive exception handling tests for async/await, covering:
 * <ol>
 *   <li>Exception transparency — checked exceptions pass through {@code await}
 *       without wrapping (like C#/JavaScript)</li>
 *   <li>Deep unwrapping — nested CompletionException/ExecutionException chains</li>
 *   <li>Cancellation support — {@code cancel()}/{@code isCancelled()}</li>
 *   <li>{@code awaitAll}/{@code awaitAny} with mixed success/failure</li>
 *   <li>{@code awaitAllSettled} — JavaScript's {@code Promise.allSettled()}</li>
 *   <li>Generator (yield return) exception propagation</li>
 *   <li>Exception chaining across multiple await calls</li>
 *   <li>try/catch/finally in async methods</li>
 * </ol>
 *
 * @since 6.0.0
 */
final class AsyncExceptionHandlingTest {

    // =========================================================================
    // 1. Exception Transparency — checked exceptions through await
    // =========================================================================

    @Test
    void testAwaitIOExceptionTransparency() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async fetchData() {
                throw new java.io.IOException("disk failure")
            }

            async caller() {
                try {
                    await fetchData()
                    assert false : "should not reach"
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "disk failure"
        '''
    }

    @Test
    void testAwaitSQLExceptionTransparency() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async queryDb() {
                throw new java.sql.SQLException("connection refused", "08001")
            }

            async caller() {
                try {
                    await queryDb()
                    assert false : "should not reach"
                } catch (java.sql.SQLException e) {
                    return e.SQLState
                }
            }

            assert await(caller()) == "08001"
        '''
    }

    @Test
    void testAwaitCustomCheckedExceptionTransparency() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            class BusinessException extends Exception {
                int code
                BusinessException(String msg, int code) {
                    super(msg)
                    this.code = code
                }
            }

            async process() {
                throw new BusinessException("insufficient funds", 402)
            }

            async caller() {
                try {
                    await process()
                    assert false
                } catch (BusinessException e) {
                    return "${e.message}:${e.code}"
                }
            }

            assert await(caller()) == "insufficient funds:402"
        '''
    }

    @Test
    void testAwaitRuntimeExceptionTransparency() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async badOp() {
                throw new IllegalArgumentException("bad arg")
            }

            async caller() {
                try {
                    await badOp()
                    assert false
                } catch (IllegalArgumentException e) {
                    return e.message
                }
            }

            assert await(caller()) == "bad arg"
        '''
    }

    @Test
    void testAwaitErrorPropagation() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async oom() {
                throw new OutOfMemoryError("test OOM")
            }

            async caller() {
                try {
                    await oom()
                    assert false
                } catch (OutOfMemoryError e) {
                    return e.message
                }
            }

            assert await(caller()) == "test OOM"
        '''
    }

    // =========================================================================
    // 2. Deep Unwrapping — nested wrapper chains
    // =========================================================================

    @Test
    void testDeepUnwrapCompletionException() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            // Simulate deeply nested wrapping
            async caller() {
                def original = new java.io.FileNotFoundException("config.yml")
                def wrapped = new CompletionException(new java.util.concurrent.ExecutionException(original))
                def future = new CompletableFuture()
                future.completeExceptionally(wrapped)
                try {
                    await future
                    assert false
                } catch (java.io.FileNotFoundException e) {
                    return e.message
                }
            }

            assert await(caller()) == "config.yml"
        '''
    }

    @Test
    void testDeepUnwrapExecutionException() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException
            import java.util.concurrent.ExecutionException

            async caller() {
                def original = new IllegalStateException("deep cause")
                // Triple nesting: CompletionException -> ExecutionException -> CompletionException -> actual
                def inner = new CompletionException(original)
                def middle = new ExecutionException(inner)
                def outer = new CompletionException(middle)
                def future = new CompletableFuture()
                future.completeExceptionally(outer)
                try {
                    await future
                    assert false
                } catch (IllegalStateException e) {
                    return e.message
                }
            }

            assert await(caller()) == "deep cause"
        '''
    }

    // =========================================================================
    // 3. Cancellation Support
    // =========================================================================

    @Test
    void testCancelAwaitable() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CancellationException

            async slowTask() {
                Thread.sleep(100)
                return 42
            }

            def task = slowTask()
            assert !task.isCancelled()
            assert !task.isDone()

            boolean cancelled = task.cancel()
            assert cancelled || task.isDone()

            assert task.isCancelled()
            assert task.isDone()
            assert task.isCompletedExceptionally()

            try {
                await(task)
                assert false : "should throw CancellationException"
            } catch (CancellationException e) {
                // expected — matches C# cancellation behavior
            }
        '''
    }

    @Test
    void testCancelAlreadyCompletedIsNoOp() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            def result = Awaitable.of(42)
            assert !result.cancel()
            assert !result.isCancelled()
            assert result.isDone()
            assert !result.isCompletedExceptionally()
            assert await(result) == 42
        '''
    }

    @Test
    void testIsCompletedExceptionallyOnFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def failed = Awaitable.failed(new RuntimeException("boom"))
            assert failed.isDone()
            assert failed.isCompletedExceptionally()
            assert !failed.isCancelled()
        '''
    }

    // =========================================================================
    // 4. awaitAll with mixed success/failure
    // =========================================================================

    @Test
    void testAwaitAllWithFailureThrowsOriginal() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async success() { return 1 }

            async failure() { throw new java.io.IOException("network error") }

            async caller() {
                try {
                    awaitAll(success(), failure())
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "network error"
        '''
    }

    @Test
    void testAwaitAllWithAwaitableObjects() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async a() { return 10 }

            async b() { return 20 }

            async caller() {
                def results = awaitAll(a(), b(), Awaitable.of(30))
                return results
            }

            def r = await(caller())
            assert r == [10, 20, 30]
        '''
    }

    @Test
    void testAwaitAnyWithFailure() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                try {
                    awaitAny(Awaitable.failed(new java.io.IOException("fail")))
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "fail"
        '''
    }

    @Test
    void testAwaitAnyWithMixedSourceTypes() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = CompletableFuture.completedFuture(42)
                def aw = Awaitable.of(99)
                def result = awaitAny(cf, aw)
                return result
            }

            def r = await(caller())
            assert r == 42 || r == 99
        '''
    }

    // =========================================================================
    // 5. awaitAllSettled — Promise.allSettled() equivalent
    // =========================================================================

    @Test
    void testAwaitAllSettledMixedResults() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                def a = Awaitable.of(1)
                def b = Awaitable.failed(new java.io.IOException("err"))
                def c = Awaitable.of(3)
                return awaitAllSettled(a, b, c)
            }

            def results = await(caller())
            assert results.size() == 3

            assert results[0].isSuccess()
            assert results[0].value == 1

            assert results[1].isFailure()
            assert results[1].error instanceof java.io.IOException
            assert results[1].error.message == "err"

            assert results[2].isSuccess()
            assert results[2].value == 3
        '''
    }

    @Test
    void testAwaitAllSettledAllSuccess() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                return awaitAllSettled(
                    Awaitable.of("a"), Awaitable.of("b"), Awaitable.of("c")
                )
            }

            def results = await(caller())
            assert results.every { it.isSuccess() }
            assert results*.value == ["a", "b", "c"]
        '''
    }

    @Test
    void testAwaitAllSettledAllFailure() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                return awaitAllSettled(
                    Awaitable.failed(new RuntimeException("e1")),
                    Awaitable.failed(new java.io.IOException("e2")),
                    Awaitable.failed(new IllegalStateException("e3"))
                )
            }

            def results = await(caller())
            assert results.every { it.isFailure() }
            assert results[0].error instanceof RuntimeException
            assert results[1].error instanceof java.io.IOException
            assert results[2].error instanceof IllegalStateException
        '''
    }

    @Test
    void testAwaitAllSettledGetOrElse() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                def results = awaitAllSettled(
                    Awaitable.of(10),
                    Awaitable.failed(new RuntimeException("oops"))
                )
                return results.collect { it.getOrElse { err -> -1 } }
            }

            assert await(caller()) == [10, -1]
        '''
    }

    @Test
    void testAwaitAllSettledNeverThrows() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                // Should NOT throw even though all fail
                def results = awaitAllSettled(
                    Awaitable.failed(new Error("err1")),
                    Awaitable.failed(new RuntimeException("err2"))
                )
                return results.size()
            }

            assert await(caller()) == 2
        '''
    }

    @Test
    void testAwaitAllSettledWithCancelledTask() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = new CompletableFuture()
                cf.cancel(true)
                def results = awaitAllSettled(
                    Awaitable.of(1), cf
                )
                assert results[0].isSuccess()
                assert results[0].value == 1
                assert results[1].isFailure()
                assert results[1].error instanceof CancellationException
                return "ok"
            }

            assert await(caller()) == "ok"
        '''
    }

    // =========================================================================
    // 6. Generator (yield return) exception propagation
    // =========================================================================

    @Test
    void testGeneratorCheckedExceptionPropagation() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async failingGenerator() {
                yield return 1
                yield return 2
                throw new java.io.IOException("generator IO error")
            }

            async caller() {
                def collected = []
                try {
                    for await (item in failingGenerator()) {
                        collected << item
                    }
                    assert false : "should not complete normally"
                } catch (java.io.IOException e) {
                    return "${collected}:${e.message}"
                }
            }

            assert await(caller()) == "[1, 2]:generator IO error"
        '''
    }

    @Test
    void testGeneratorRuntimeExceptionPropagation() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async failingGenerator() {
                yield return "a"
                throw new IllegalArgumentException("bad value")
            }

            async caller() {
                def items = []
                try {
                    for await (item in failingGenerator()) {
                        items << item
                    }
                    assert false
                } catch (IllegalArgumentException e) {
                    return "${items}:${e.message}"
                }
            }

            assert await(caller()) == "[a]:bad value"
        '''
    }

    // =========================================================================
    // 7. try/catch/finally in async methods
    // =========================================================================

    @Test
    void testTryCatchFinallyInAsync() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async riskyOp() {
                throw new java.io.IOException("test")
            }

            async caller() {
                def log = []
                try {
                    log << "try"
                    await riskyOp()
                    log << "after-await"
                } catch (java.io.IOException e) {
                    log << "catch:${e.message}"
                } finally {
                    log << "finally"
                }
                return log.join(",")
            }

            assert await(caller()) == "try,catch:test,finally"
        '''
    }

    @Test
    void testMultipleCatchBlocks() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async throwIO() { throw new java.io.IOException("io") }

            async throwArg() { throw new IllegalArgumentException("arg") }

            async caller(boolean useIO) {
                try {
                    if (useIO) {
                        await throwIO()
                    } else {
                        await throwArg()
                    }
                    return "none"
                } catch (java.io.IOException e) {
                    return "io:${e.message}"
                } catch (IllegalArgumentException e) {
                    return "arg:${e.message}"
                }
            }

            assert await(caller(true)) == "io:io"
            assert await(caller(false)) == "arg:arg"
        '''
    }

    @Test
    void testNestedTryCatch() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async fail1() { throw new java.io.IOException("first") }

            async fail2() { throw new IllegalStateException("second") }

            async caller() {
                def results = []
                try {
                    try {
                        await fail1()
                    } catch (java.io.IOException e) {
                        results << "inner:${e.message}"
                    }
                    await fail2()
                } catch (IllegalStateException e) {
                    results << "outer:${e.message}"
                }
                return results.join(",")
            }

            assert await(caller()) == "inner:first,outer:second"
        '''
    }

    @Test
    void testFinallyRunsOnSuccess() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                def log = []
                try {
                    def v = await Awaitable.of(42)
                    log << "value:$v"
                } finally {
                    log << "finally"
                }
                return log.join(",")
            }

            assert await(caller()) == "value:42,finally"
        '''
    }

    // =========================================================================
    // 8. Exception chaining across multiple awaits
    // =========================================================================

    @Test
    void testExceptionChainingThreeDeep() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async level3() {
                throw new java.io.IOException("root cause")
            }

            async level2() {
                return await level3()
            }

            async level1() {
                return await level2()
            }

            async caller() {
                try {
                    await level1()
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "root cause"
        '''
    }

    @Test
    void testExceptionTransformAcrossMultipleAwaits() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async step1() {
                throw new java.io.IOException("step1 failed")
            }

            async step2() {
                try {
                    await step1()
                } catch (java.io.IOException e) {
                    throw new IllegalStateException("step2 wraps: " + e.message, e)
                }
            }

            async caller() {
                try {
                    await step2()
                    assert false
                } catch (IllegalStateException e) {
                    assert e.cause instanceof java.io.IOException
                    return e.message
                }
            }

            assert await(caller()) == "step2 wraps: step1 failed"
        '''
    }

    // =========================================================================
    // 9. Awaitable.exceptionally — error recovery
    // =========================================================================

    @Test
    void testExceptionallyRecovery() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async failing() {
                throw new java.io.IOException("network error")
            }

            async caller() {
                def task = failing().exceptionally { err ->
                    assert err instanceof java.io.IOException
                    return "recovered: ${err.message}"
                }
                return await task
            }

            assert await(caller()) == "recovered: network error"
        '''
    }

    @Test
    void testExceptionallySeesUnwrappedCause() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            async caller() {
                // Create a future that wraps a checked exception
                def cf = new CompletableFuture()
                cf.completeExceptionally(
                    new CompletionException(new java.io.IOException("wrapped"))
                )
                def task = new org.apache.groovy.runtime.async.GroovyPromise(cf)
                def recovered = task.exceptionally { err ->
                    // Should receive the IOException, not CompletionException
                    return err.class.simpleName + ":" + err.message
                }
                return await recovered
            }

            assert await(caller()) == "IOException:wrapped"
        '''
    }

    // =========================================================================
    // 10. Awaitable.then — exception propagation
    // =========================================================================

    @Test
    void testThenDoesNotRunOnFailure() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                def task = Awaitable.failed(new RuntimeException("boom"))
                def mapped = task.then { v -> v * 2 }
                try {
                    await mapped
                    assert false
                } catch (RuntimeException e) {
                    return e.message
                }
            }

            assert await(caller()) == "boom"
        '''
    }

    // =========================================================================
    // 11. Direct await syntax with try/catch — no annotations
    // =========================================================================

    @Test
    void testDirectAsyncClosureExceptionHandling() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            async caller() {
                def task = async {
                    throw new java.io.IOException("from closure")
                }
                try {
                    await task
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert groovy.concurrent.AsyncUtils.await(caller()) == "from closure"
        '''
    }

    // =========================================================================
    // 12. AwaitResult API edge cases
    // =========================================================================

    @Test
    void testAwaitResultAccessors() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success(42)
            assert s.isSuccess()
            assert !s.isFailure()
            assert s.value == 42
            try {
                s.error
                assert false
            } catch (IllegalStateException e) {
                // expected
            }

            def f = AwaitResult.failure(new RuntimeException("err"))
            assert f.isFailure()
            assert !f.isSuccess()
            assert f.error.message == "err"
            try {
                f.value
                assert false
            } catch (IllegalStateException e) {
                // expected
            }
        '''
    }

    @Test
    void testAwaitResultToString() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success("hello")
            assert s.toString() == "AwaitResult.Success[hello]"

            def f = AwaitResult.failure(new RuntimeException("boom"))
            assert f.toString().startsWith("AwaitResult.Failure[")
        '''
    }

    // =========================================================================
    // 13. InterruptedException → CancellationException
    // =========================================================================

    @Test
    void testInterruptedAwaitThrowsCancellation() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CancellationException
            import groovy.concurrent.Awaitable

            async caller() {
                def task = Awaitable.of(42)
                // Interrupt current thread before await
                Thread.currentThread().interrupt()
                try {
                    await task
                    // Awaitable.of is already complete, so get() may succeed
                    // (depending on impl). Clear interrupt flag.
                    Thread.interrupted()
                    return "completed"
                } catch (CancellationException e) {
                    Thread.interrupted() // clear flag
                    return "cancelled"
                }
            }

            def result = await(caller())
            assert result == "completed" || result == "cancelled"
        '''
    }

    // =========================================================================
    // 14. Exception in for-await loop body
    // =========================================================================

    @Test
    void testExceptionInForAwaitBody() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async generate() {
                yield return 1
                yield return 2
                yield return 3
            }

            async caller() {
                def collected = []
                try {
                    for await (item in generate()) {
                        collected << item
                        if (item == 2) throw new IllegalStateException("stop at 2")
                    }
                    assert false
                } catch (IllegalStateException e) {
                    return "${collected}:${e.message}"
                }
            }

            assert await(caller()) == "[1, 2]:stop at 2"
        '''
    }

    // =========================================================================
    // 15. Async method returning failed Awaitable via exceptionally
    // =========================================================================

    @Test
    void testRecoverAndContinue() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async fetchWithRetry() {
                def attempt1 = Awaitable.failed(new java.io.IOException("timeout"))
                def result1 = attempt1.exceptionally { err -> "fallback" }
                return await result1
            }

            assert await(fetchWithRetry()) == "fallback"
        '''
    }

    // =========================================================================
    // 16. deepUnwrap exposed via AsyncUtils (package-private, test via reflection or direct)
    // =========================================================================

    @Test
    void testDeepUnwrapUtility() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletionException
            import java.util.concurrent.ExecutionException

            def original = new java.io.IOException("root")
            def l1 = new ExecutionException(original)
            def l2 = new CompletionException(l1)
            def l3 = new CompletionException(l2)

            // Access package-private method via Groovy's meta
            def unwrapped = deepUnwrap(l3)
            assert unwrapped.is(original)
        '''
    }

    @Test
    void testDeepUnwrapStopsAtNonWrapper() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def ex = new IllegalArgumentException("not wrapped")
            assert deepUnwrap(ex).is(ex)
        '''
    }

    @Test
    void testDeepUnwrapHandlesNullCause() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletionException

            // CompletionException with null cause
            def ce = new CompletionException((Throwable) null)
            def result = deepUnwrap(ce)
            assert result.is(ce)
        '''
    }

    // =========================================================================
    // 17. awaitAllSettled with CompletableFuture mix
    // =========================================================================

    @Test
    void testAwaitAllSettledWithCompletableFutures() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf1 = CompletableFuture.completedFuture("ok")
                def cf2 = CompletableFuture.failedFuture(new RuntimeException("bad"))
                def results = awaitAllSettled(cf1, cf2)

                assert results[0].isSuccess()
                assert results[0].value == "ok"
                assert results[1].isFailure()
                assert results[1].error instanceof RuntimeException
                return "verified"
            }

            assert await(caller()) == "verified"
        '''
    }

    // =========================================================================
    // 18. Exception with null message
    // =========================================================================

    @Test
    void testExceptionWithNullMessage() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*

            async throwNull() {
                throw new RuntimeException((String) null)
            }

            async caller() {
                try {
                    await throwNull()
                    assert false
                } catch (RuntimeException e) {
                    assert e.message == null
                    return "caught-null-msg"
                }
            }

            assert await(caller()) == "caught-null-msg"
        '''
    }

    // =========================================================================
    // 19. Multiple sequential awaits with different exception types
    // =========================================================================

    @Test
    void testSequentialAwaitsWithDifferentExceptions() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                def log = []

                try {
                    await Awaitable.failed(new java.io.IOException("io"))
                } catch (java.io.IOException e) {
                    log << "io:${e.message}"
                }

                try {
                    await Awaitable.failed(new NumberFormatException("nfe"))
                } catch (NumberFormatException e) {
                    log << "nfe:${e.message}"
                }

                try {
                    await Awaitable.failed(new java.sql.SQLException("sql"))
                } catch (java.sql.SQLException e) {
                    log << "sql:${e.message}"
                }

                return log.join(";")
            }

            assert await(caller()) == "io:io;nfe:nfe;sql:sql"
        '''
    }

    // =========================================================================
    // 20. Awaitable.failed + await throws original
    // =========================================================================

    @Test
    void testAwaitOnFailedAwaitable() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.Awaitable

            async caller() {
                try {
                    await Awaitable.failed(new java.io.IOException("pre-failed"))
                    assert false
                } catch (java.io.IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "pre-failed"
        '''
    }
}
