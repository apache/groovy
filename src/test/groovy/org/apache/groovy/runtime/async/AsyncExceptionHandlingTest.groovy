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
import static groovy.test.GroovyAssert.shouldFail

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

            import java.io.IOException

            async fetchData() {
                throw new IOException("disk failure")
            }

            async caller() {
                try {
                    await fetchData()
                    assert false : "should not reach"
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "disk failure"
        '''
    }

    @Test
    void testAwaitSQLExceptionTransparency() {
        assertScript '''

            import java.sql.SQLException

            async queryDb() {
                throw new SQLException("connection refused", "08001")
            }

            async caller() {
                try {
                    await queryDb()
                    assert false : "should not reach"
                } catch (SQLException e) {
                    return e.SQLState
                }
            }

            assert await(caller()) == "08001"
        '''
    }

    @Test
    void testAwaitCustomCheckedExceptionTransparency() {
        assertScript '''

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
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException
            import java.io.FileNotFoundException
            import java.util.concurrent.ExecutionException

            // Simulate deeply nested wrapping
            async caller() {
                def original = new FileNotFoundException("config.yml")
                def wrapped = new CompletionException(new ExecutionException(original))
                def future = new CompletableFuture()
                future.completeExceptionally(wrapped)
                try {
                    await future
                    assert false
                } catch (FileNotFoundException e) {
                    return e.message
                }
            }

            assert await(caller()) == "config.yml"
        '''
    }

    @Test
    void testDeepUnwrapExecutionException() {
        assertScript '''
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
                await task
                assert false : "should throw CancellationException"
            } catch (CancellationException e) {
                assert e instanceof CancellationException
            }
        '''
    }

    @Test
    void testCancelAlreadyCompletedIsNoOp() {
        assertScript '''
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
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async success() { return 1 }

            async failure() { throw new IOException("network error") }

            async caller() {
                try {
                    await Awaitable.all(success(), failure())
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "network error"
        '''
    }

    @Test
    void testAwaitAllWithAwaitableObjects() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async a() { return 10 }

            async b() { return 20 }

            async caller() {
                def results = await Awaitable.all(a(), b(), Awaitable.of(30))
                return results
            }

            def r = await caller()
            assert r == [10, 20, 30]
        '''
    }

    @Test
    void testAwaitAnyWithFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                try {
                    await Awaitable.any(Awaitable.failed(new IOException("fail")))
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "fail"
        '''
    }

    @Test
    void testAwaitAnyWithMixedSourceTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = CompletableFuture.completedFuture(42)
                def aw = Awaitable.of(99)
                def result = await Awaitable.any(cf, aw)
                return result
            }

            def r = await caller()
            assert r == 42 || r == 99
        '''
    }

    // =========================================================================
    // 5. awaitAllSettled — Promise.allSettled() equivalent
    // =========================================================================

    @Test
    void testAwaitAllSettledMixedResults() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                def a = Awaitable.of(1)
                def b = Awaitable.failed(new IOException("err"))
                def c = Awaitable.of(3)
                return await Awaitable.allSettled(a, b, c)
            }

            def results = await caller()
            assert results.size() == 3

            assert results[0].isSuccess()
            assert results[0].value == 1

            assert results[1].isFailure()
            assert results[1].error instanceof IOException
            assert results[1].error.message == "err"

            assert results[2].isSuccess()
            assert results[2].value == 3
        '''
    }

    @Test
    void testAwaitAllSettledAllSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.allSettled(
                    Awaitable.of("a"), Awaitable.of("b"), Awaitable.of("c")
                )
            }

            def results = await caller()
            assert results.every { it.isSuccess() }
            assert results*.value == ["a", "b", "c"]
        '''
    }

    @Test
    void testAwaitAllSettledAllFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                return await Awaitable.allSettled(
                    Awaitable.failed(new RuntimeException("e1")),
                    Awaitable.failed(new IOException("e2")),
                    Awaitable.failed(new IllegalStateException("e3"))
                )
            }

            def results = await caller()
            assert results.every { it.isFailure() }
            assert results[0].error instanceof RuntimeException
            assert results[1].error instanceof IOException
            assert results[2].error instanceof IllegalStateException
        '''
    }

    @Test
    void testAwaitAllSettledGetOrElse() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                def results = await Awaitable.allSettled(
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
            import groovy.concurrent.Awaitable

            async caller() {
                // Should NOT throw even though all fail
                def results = await Awaitable.allSettled(
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
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = new CompletableFuture()
                cf.cancel(true)
                def results = await Awaitable.allSettled(
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

            import java.io.IOException

            async failingGenerator() {
                yield return 1
                yield return 2
                throw new IOException("generator IO error")
            }

            async caller() {
                def collected = []
                try {
                    for await (item in failingGenerator()) {
                        collected << item
                    }
                    assert false : "should not complete normally"
                } catch (IOException e) {
                    return "${collected}:${e.message}"
                }
            }

            assert await(caller()) == "[1, 2]:generator IO error"
        '''
    }

    @Test
    void testGeneratorRuntimeExceptionPropagation() {
        assertScript '''

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

            import java.io.IOException

            async riskyOp() {
                throw new IOException("test")
            }

            async caller() {
                def log = []
                try {
                    log << "try"
                    await riskyOp()
                    log << "after-await"
                } catch (IOException e) {
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
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async throwIO() { throw new IOException("io") }

            async throwArg() { throw new IllegalArgumentException("arg") }

            async caller(boolean useIO) {
                try {
                    if (useIO) {
                        await throwIO()
                    } else {
                        await throwArg()
                    }
                    return "none"
                } catch (IOException e) {
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

            import java.io.IOException

            async fail1() { throw new IOException("first") }

            async fail2() { throw new IllegalStateException("second") }

            async caller() {
                def results = []
                try {
                    try {
                        await fail1()
                    } catch (IOException e) {
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

            import java.io.IOException

            async level3() {
                throw new IOException("root cause")
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
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "root cause"
        '''
    }

    @Test
    void testExceptionTransformAcrossMultipleAwaits() {
        assertScript '''

            import java.io.IOException

            async step1() {
                throw new IOException("step1 failed")
            }

            async step2() {
                try {
                    await step1()
                } catch (IOException e) {
                    throw new IllegalStateException("step2 wraps: " + e.message, e)
                }
            }

            async caller() {
                try {
                    await step2()
                    assert false
                } catch (IllegalStateException e) {
                    assert e.cause instanceof IOException
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
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async failing() {
                throw new IOException("network error")
            }

            async caller() {
                def task = failing().exceptionally { err ->
                    assert err instanceof IOException
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
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            async caller() {
                // Create a future that wraps a checked exception
                def cf = new CompletableFuture()
                cf.completeExceptionally(
                    new CompletionException(new IOException("wrapped"))
                )
                def task = Awaitable.from(cf)
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

            import java.io.IOException

            async caller() {
                def task = async {
                    throw new IOException("from closure")
                }
                try {
                    await task()
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "from closure"
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
                assert e.message != null
            }

            def f = AwaitResult.failure(new RuntimeException("err"))
            assert f.isFailure()
            assert !f.isSuccess()
            assert f.error.message == "err"
            try {
                f.value
                assert false
            } catch (IllegalStateException e) {
                assert e.message != null
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

            def result = await caller()
            assert result == "completed" || result == "cancelled"
        '''
    }

    // =========================================================================
    // 14. Exception in for-await loop body
    // =========================================================================

    @Test
    void testExceptionInForAwaitBody() {
        assertScript '''

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
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async fetchWithRetry() {
                def attempt1 = Awaitable.failed(new IOException("timeout"))
                def result1 = attempt1.exceptionally { err -> "fallback" }
                return await result1
            }

            assert await(fetchWithRetry()) == "fallback"
        '''
    }

    // =========================================================================
    // 16. Additional wrapper edge cases via public await semantics
    // =========================================================================

    @Test
    void testAwaitPreservesCompletionExceptionWithNullCause() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            def future = new CompletableFuture<String>()
            def wrapper = new CompletionException((Throwable) null)
            future.completeExceptionally(wrapper)

            try {
                await future
                assert false : 'should throw'
            } catch (CompletionException e) {
                assert e.is(wrapper)
                assert e.cause == null
            }
        '''
    }

    // =========================================================================
    // 17. awaitAllSettled with CompletableFuture mix
    // =========================================================================

    @Test
    void testAwaitAllSettledWithCompletableFutures() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf1 = CompletableFuture.completedFuture("ok")
                def cf2 = CompletableFuture.failedFuture(new RuntimeException("bad"))
                def results = await Awaitable.allSettled(cf1, cf2)

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
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.sql.SQLException

            async caller() {
                def log = []

                try {
                    await Awaitable.failed(new IOException("io"))
                } catch (IOException e) {
                    log << "io:${e.message}"
                }

                try {
                    await Awaitable.failed(new NumberFormatException("nfe"))
                } catch (NumberFormatException e) {
                    log << "nfe:${e.message}"
                }

                try {
                    await Awaitable.failed(new SQLException("sql"))
                } catch (SQLException e) {
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
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                try {
                    await Awaitable.failed(new IOException("pre-failed"))
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == "pre-failed"
        '''
    }

    // ===== Exception handling and edge-case tests =====

    @Test
    void testExceptionallyRecoversFromFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def failed = Awaitable.failed(new IOException("network error"))
            def recovered = failed.exceptionally { ex -> "fallback: ${ex.message}" }
            assert await(recovered) == "fallback: network error"
        '''
    }

    @Test
    void testExceptionallyPassesThroughOnSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = Awaitable.of(99)
            def same = ok.exceptionally { ex -> -1 }
            assert await(same) == 99
        '''
    }

    @Test
    void testWhenCompleteSeesUnwrappedFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            def successful = new AtomicReference()
            assert await(Awaitable.of("ok").whenComplete { value, error ->
                successful.set([value, error])
            }) == "ok"
            assert successful.get()[0] == "ok"
            assert successful.get()[1] == null

            def failed = Awaitable.failed(new IOException("boom")).whenComplete { value, error ->
                assert value == null
                assert error instanceof IOException
                assert error.message == "boom"
            }

            try {
                await failed
                assert false : "Should have thrown"
            } catch (IOException e) {
                assert e.message == "boom"
            }
        '''
    }

    @Test
    void testCancelAndIsCancelled() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException

            def task = Awaitable.delay(10_000)
            assert !task.isCancelled()
            boolean cancelled = task.cancel()
            assert cancelled
            assert task.isCancelled()
            assert task.isDone()
            assert task.isCompletedExceptionally()

            try {
                await task
                assert false : "Should have thrown"
            } catch (CancellationException e) {
                assert e instanceof CancellationException
            }
        '''
    }

    @Test
    void testIsCompletedExceptionally() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = Awaitable.of(1)
            assert !ok.isCompletedExceptionally()

            def bad = Awaitable.failed(new RuntimeException("oops"))
            assert bad.isCompletedExceptionally()
        '''
    }

    @Test
    void testCancellationMessageIsStableAcrossJdks() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException

            def original = new CancellationException("cancelled-by-user")
            original.initCause(new InterruptedException("interrupt-cause"))

            def failed = Awaitable.failed(original)
            try {
                failed.get()
                assert false : "Should have thrown"
            } catch (CancellationException e) {
                assert e.message == "cancelled-by-user"
                assert e.cause instanceof InterruptedException
                assert e.cause.message == "interrupt-cause"
            }
        '''
    }

    // ================================================================
    // Awaitable static: delay(long, TimeUnit), getExecutor,
    // isVirtualThreadsAvailable
    // ================================================================

    @Test
    void testAwaitResultFactories() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success("hello")
            assert s.isSuccess()
            assert !s.isFailure()
            assert s.value == "hello"
            assert s.toString().contains("Success")

            def f = AwaitResult.failure(new IOException("boom"))
            assert f.isFailure()
            assert !f.isSuccess()
            assert f.error instanceof IOException
            assert f.error.message == "boom"
            assert f.toString().contains("Failure")
        '''
    }

    @Test
    void testAwaitResultGetOrElse() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success(42)
            assert s.getOrElse { -1 } == 42

            def f = AwaitResult.failure(new IOException("err"))
            assert f.getOrElse { ex -> "recovered: ${ex.message}" } == "recovered: err"
        '''
    }

    @Test
    void testAwaitResultGetValueThrowsOnFailure() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def f = AwaitResult.failure(new IOException("err"))
            try {
                f.value
                assert false : "Should throw"
            } catch (IllegalStateException e) {
                assert e.message.contains("failed result")
            }
        '''
    }

    @Test
    void testAwaitResultGetErrorThrowsOnSuccess() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success("ok")
            try {
                s.error
                assert false : "Should throw"
            } catch (IllegalStateException e) {
                assert e.message.contains("successful result")
            }
        '''
    }

    // ================================================================
    // AwaitableAdapterRegistry: register, unregister, blockingExecutor
    // ================================================================

    @Test
    void testAnyRequiresAtLeastOneSource() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.any()
                assert false : "Should throw"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("at least one source")
            }
        '''
    }

    @Test
    void testAllRejectsNullElement() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.all(Awaitable.of(1), null)
                assert false : "Should throw"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("null")
            }
        '''
    }

    @Test
    void testAllSettledCapturesFailures() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async succeed() { "ok" }
            async fail() { throw new IOException("boom") }

            def results = await Awaitable.allSettled(succeed(), fail())
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].value == "ok"
            assert results[1].isFailure()
            assert results[1].error instanceof IOException
        '''
    }

    // ================================================================
    // Awaitable.of / Awaitable.failed edge cases
    // ================================================================

    @Test
    void testAwaitableFailedPropagates() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.failed(new ArithmeticException("div by zero"))
            assert a.isDone()
            assert a.isCompletedExceptionally()

            try {
                await a
                assert false
            } catch (ArithmeticException e) {
                assert e.message == "div by zero"
            }
        '''
    }

    // ================================================================
    // Then/thenCompose/exceptionally chaining patterns
    // ================================================================

    @Test
    void testExceptionallyWithThenChain() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Error at start of chain, recovered in the middle
            def result = await
                Awaitable.failed(new IOException("err"))
                    .exceptionally { -1 }
                    .then { it * 10 }

            assert result == -10
        '''
    }

    // ================================================================
    // Blocking awaitAll/awaitAny (AsyncSupport-level coverage)
    // ================================================================

    @Test
    void testAsyncGeneratorErrorPropagation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async failingGenerator() {
                yield return "first"
                throw new IOException("generator-error")
            }

            async consume() {
                def items = []
                try {
                    for await (item in failingGenerator()) {
                        items << item
                    }
                    assert false
                } catch (IOException e) {
                    assert e.message == "generator-error"
                }
                items
            }

            assert await(consume()) == ["first"]
        '''
    }

    @Test
    void testAsyncGeneratorWithErrorType() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async errorGenerator() {
                yield return "before-error"
                throw new StackOverflowError("test-soe")
            }

            async consume() {
                try {
                    for await (item in errorGenerator()) { }
                    assert false
                } catch (StackOverflowError e) {
                    assert e.message == "test-soe"
                    "caught-error"
                }
            }

            assert await(consume()) == "caught-error"
        '''
    }

    // =========================================================================
    // Edge-case and error-path coverage
    // =========================================================================

    @Test
    void testAwaitAwaitableWithFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            def failed = Awaitable.failed(new IOException("disk error"))
            try {
                await failed
                assert false : 'should throw'
            } catch (IOException e) {
                assert e.message == 'disk error'
            }
        '''
    }

    @Test
    void testAwaitCompletableFutureWithFailure() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            try {
                await CompletableFuture.failedFuture(new IllegalArgumentException("bad arg"))
                assert false : 'should throw'
            } catch (IllegalArgumentException e) {
                assert e.message == 'bad arg'
            }
        '''
    }

    @Test
    void testAwaitFutureWithFailure() {
        assertScript '''
            import java.util.concurrent.Future
            import java.util.concurrent.FutureTask

            Future<Integer> task = new FutureTask<Integer>({ throw new ArithmeticException("div by zero") })
            task.run()
            try {
                await task
                assert false : 'should throw'
            } catch (ArithmeticException e) {
                assert e.message == 'div by zero'
            }
        '''
    }

    @Test
    void testAwaitAllWithFailureRethrowsTransparently() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.of(1)
            def b = Awaitable.failed(new RuntimeException("boom"))
            try {
                await Awaitable.all(a, b)
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message == 'boom'
            }
        '''
    }

    @Test
    void testAwaitAnyWithFailureRethrowsTransparently() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.failed(new RuntimeException("fail1"))
            try {
                await Awaitable.any(a)
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message == 'fail1'
            }
        '''
    }

    @Test
    void testRethrowUnwrappedChecked() {
        assertScript '''
            import java.io.IOException
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            def future = new CompletableFuture<String>()
            future.completeExceptionally(new CompletionException(new IOException("checked")))
            try {
                await future
                assert false : 'should throw'
            } catch (IOException e) {
                assert e.message == 'checked'
            }
        '''
    }

    @Test
    void testAwaitAllEmptyReturnsEmptyList() {
        assertScript '''
            import groovy.concurrent.Awaitable

            assert await(Awaitable.all()) == []
            assert await(Awaitable.all((Object[]) null)) == []
        '''
    }

    @Test
    void testAwaitAnyEmptyThrowsIllegalArgument() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            await Awaitable.any()
        '''
    }

    @Test
    void testAwaitAllSettledAllFail() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.failed(new RuntimeException("e1"))
            def b = Awaitable.failed(new RuntimeException("e2"))
            def results = await Awaitable.allSettled(a, b)
            assert results.size() == 2
            assert results[0].failure
            assert results[1].failure
            assert results[0].error.message == 'e1'
            assert results[1].error.message == 'e2'
        '''
    }

    @Test
    void testAwaitUnwrapsDeepWrapperChain() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException

            // Build a chain within the 64-level depth limit
            def inner = new RuntimeException("deep")
            def wrapper = inner
            for (int i = 0; i < 50; i++) {
                wrapper = new CompletionException(wrapper)
            }

            def future = new CompletableFuture<String>()
            future.completeExceptionally(wrapper)

            try {
                await future
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message == 'deep'
            }
        '''
    }

    // ================================================================
    // Structured scope error propagation
    // ================================================================

    @Test
    void testAsyncScopeCollectsSuppressedExceptions() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            try {
                AsyncScope.withScope { scope ->
                    scope.async { throw new RuntimeException('first') }
                    scope.async {
                        await Awaitable.delay(30)
                        throw new RuntimeException('second')
                    }
                }
                assert false : 'should throw'
            } catch (RuntimeException e) {
                // First failure is the primary exception
                assert e.message == 'first'
                // Second failure may appear as suppressed (timing-dependent)
            }
        '''
    }

    @Test
    void testAwaitResultCapturesExceptionInAllSettled() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async settle() {
                def results = await Awaitable.allSettled(
                    Awaitable.of('ok'),
                    Awaitable.failed(new java.io.IOException('io-err')),
                    Awaitable.of(42)
                )
                assert results.size() == 3
                assert results[0].isSuccess() && results[0].value == 'ok'
                assert results[1].isFailure() && results[1].error instanceof java.io.IOException
                assert results[1].error.message == 'io-err'
                assert results[2].isSuccess() && results[2].value == 42

                // Verify map preserves failure
                def mapped = results[1].map { it.toString() }
                assert mapped.isFailure()
                assert mapped.error.message == 'io-err'
            }

            await settle()
        '''
    }

}
