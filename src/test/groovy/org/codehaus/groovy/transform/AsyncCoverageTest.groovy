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

import groovy.concurrent.AsyncStream
import groovy.concurrent.AwaitResult
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.AwaitableAdapterRegistry
import org.apache.groovy.runtime.async.AsyncStreamGenerator
import org.apache.groovy.runtime.async.AsyncSupport
import org.apache.groovy.runtime.async.GroovyPromise
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Flow
import java.util.concurrent.FutureTask
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static groovy.test.GroovyAssert.assertScript

/**
 * Comprehensive coverage tests for Groovy's async/await API surface.
 * <p>
 * Targets the untested public methods of {@code Awaitable},
 * {@code AwaitResult}, {@code GroovyPromise}, {@code AsyncStream},
 * and {@code AwaitableAdapterRegistry}, as well as combinator edge cases and chaining patterns.
 */
class AsyncCoverageTest {

    @AfterEach
    void cleanup() {
        Awaitable.setExecutor(null)
    }

    // ================================================================
    // Awaitable instance methods: then, thenCompose, exceptionally
    // ================================================================

    @Test
    void testThenTransformsResult() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def fetchValue() { 10 }

            def doubled = fetchValue().then { it * 2 }
            assert doubled instanceof Awaitable
            assert await(doubled) == 20
        '''
    }

    @Test
    void testThenComposeFlattensNestedAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def fetchId() { 42 }
            async def fetchName(int id) { "user-${id}" }

            def result = fetchId().thenCompose { id -> fetchName(id) }
            assert result instanceof Awaitable
            assert await(result) == "user-42"
        '''
    }

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
    void testThenAcceptRunsSideEffect() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            def seen = new AtomicInteger()
            await(Awaitable.of(21).thenAccept { seen.set(it * 2) })
            assert seen.get() == 42
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
                await(failed)
                assert false : "Should have thrown"
            } catch (IOException e) {
                assert e.message == "boom"
            }
        '''
    }

    @Test
    void testHandleProjectsSuccessAndFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = Awaitable.of(5).handle { value, error ->
                assert error == null
                value * 2
            }
            assert await(ok) == 10

            def recovered = Awaitable.failed(new IOException("boom")).handle { value, error ->
                assert value == null
                "fallback: ${error.message}"
            }
            assert await(recovered) == "fallback: boom"
        '''
    }

    @Test
    void testTimeoutCombinators() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            assert await(Awaitable.of("fast").orTimeout(1, TimeUnit.SECONDS)) == "fast"

            try {
                await(Awaitable.delay(10_000).then { "late" }.orTimeout(50, TimeUnit.MILLISECONDS))
                assert false : "Should have timed out"
            } catch (TimeoutException e) {
                assert e.message.contains("Timed out after 50")
            }

            try {
                await(Awaitable.timeout(new CompletableFuture<String>(), 50, TimeUnit.MILLISECONDS))
                assert false : "Should have timed out"
            } catch (TimeoutException e) {
                assert e.message.contains("Timed out after 50")
            }

            def fallback = await(Awaitable.timeoutOr(
                Awaitable.delay(10_000).then { "late" },
                "cached",
                50,
                TimeUnit.MILLISECONDS
            ))
            assert fallback == "cached"
            assert await(Awaitable.of("value").completeOnTimeout("fallback", 1, TimeUnit.SECONDS)) == "value"
        '''
    }

    // ================================================================
    // Awaitable instance methods: isDone, cancel, isCancelled,
    // isCompletedExceptionally, toCompletableFuture, get(timeout)
    // ================================================================

    @Test
    void testIsDoneReflectsCompletionState() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def completed = Awaitable.of("done")
            assert completed.isDone()

            def pending = Awaitable.delay(5000)
            assert !pending.isDone()
            pending.cancel()
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
                await(task)
                assert false : "Should have thrown"
            } catch (CancellationException e) {
                // expected
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
    void testToCompletableFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async def compute() { 42 }

            def awaitable = compute()
            def cf = awaitable.toCompletableFuture()
            assert cf instanceof CompletableFuture
            assert cf.get() == 42
        '''
    }

    @Test
    void testGetWithTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            def quick = Awaitable.of("fast")
            assert quick.get(1, TimeUnit.SECONDS) == "fast"

            def slow = Awaitable.delay(10_000)
            try {
                slow.get(50, TimeUnit.MILLISECONDS)
                assert false : "Should timeout"
            } catch (TimeoutException e) {
                // expected
            } finally {
                slow.cancel()
            }
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
    void testDelayWithTimeUnit() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit

            long start = System.currentTimeMillis()
            await Awaitable.delay(100, TimeUnit.MILLISECONDS)
            long elapsed = System.currentTimeMillis() - start
            assert elapsed >= 80 : "delay should wait ~100ms"
        '''
    }

    @Test
    void testGetExecutorReturnsNonNull() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executor

            def exec = Awaitable.getExecutor()
            assert exec != null
            assert exec instanceof Executor
        '''
    }

    @Test
    void testIsVirtualThreadsAvailable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Just verify it returns a boolean without throwing
            def result = Awaitable.isVirtualThreadsAvailable()
            assert result instanceof Boolean
        '''
    }

    @Test
    void testCloseStreamHelperClosesStream() {
        AtomicBoolean closed = new AtomicBoolean(false)
        AsyncSupport.closeStream(new AsyncStream<String>() {
            @Override
            Awaitable<Boolean> moveNext() {
                Awaitable.of(false)
            }

            @Override
            String getCurrent() {
                null
            }

            @Override
            void close() {
                closed.set(true)
            }
        })
        assert closed.get()
        AsyncSupport.closeStream(null)
    }

    @Test
    void testSetExecutorAndRestore() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors

            class CustomExecTest {
                async def getThreadName() {
                    Thread.currentThread().name
                }
            }

            def pool = Executors.newSingleThreadExecutor { r ->
                new Thread(r, "custom-exec-thread")
            }
            try {
                Awaitable.setExecutor(pool)
                assert Awaitable.getExecutor() == pool

                def name = await(new CustomExecTest().getThreadName())
                assert name == "custom-exec-thread"
            } finally {
                Awaitable.setExecutor(null)
                pool.shutdown()
            }
        '''
    }

    // ================================================================
    // AwaitResult: success/failure factories, getOrElse, toString
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
    void testAdapterRegistryRegisterAndUnregister() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry

            // Create a custom adapter for a custom type
            class CustomResult { String data }

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) {
                    CustomResult.isAssignableFrom(type)
                }
                def <T> Awaitable<T> toAwaitable(Object source) {
                    Awaitable.of(((CustomResult) source).data)
                }
            }

            // Register and verify it works
            def handle = AwaitableAdapterRegistry.register(adapter)
            try {
                def result = AwaitableAdapterRegistry.toAwaitable(new CustomResult(data: "hello"))
                assert await(result) == "hello"
            } finally {
                handle.close()
            }

            // After close, the adapter should be removed
            try {
                AwaitableAdapterRegistry.toAwaitable(new CustomResult(data: "fail"))
                assert false : "Should throw"
            } catch (IllegalArgumentException e) {
                // expected
            }
        '''
    }

    @Test
    void testAdapterRegistryExplicitUnregister() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { false }
                def <T> Awaitable<T> toAwaitable(Object source) { null }
            }

            AwaitableAdapterRegistry.register(adapter)
            assert AwaitableAdapterRegistry.unregister(adapter) == true
            assert AwaitableAdapterRegistry.unregister(adapter) == false
        '''
    }

    @Test
    void testAdapterRegistrySetBlockingExecutor() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapterRegistry
            import java.util.concurrent.Executors
            import java.util.concurrent.FutureTask

            def pool = Executors.newSingleThreadExecutor()
            try {
                // Set blocking executor so Future adaptation uses it
                AwaitableAdapterRegistry.setBlockingExecutor(pool)

                // Create a plain java.util.concurrent.Future
                def future = new FutureTask<String>({ "from-blocking-future" })
                pool.submit(future)

                def aw = AwaitableAdapterRegistry.toAwaitable(future)
                assert await(aw) == "from-blocking-future"
            } finally {
                AwaitableAdapterRegistry.setBlockingExecutor(null)
                pool.shutdown()
            }
        '''
    }

    // ================================================================
    // AsyncStream: empty()
    // ================================================================

    @Test
    void testAsyncStreamEmpty() {
        assertScript '''
            import groovy.concurrent.AsyncStream

            def stream = AsyncStream.empty()
            assert await(stream.moveNext()) == false
            assert stream.getCurrent() == null
        '''
    }

    // ================================================================
    // GroovyPromise: toString states
    // ================================================================

    @Test
    void testGroovyPromiseToString() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf1 = new CompletableFuture<String>()
            def p1 = new GroovyPromise(cf1)
            assert p1.toString() == "GroovyPromise{pending}"

            cf1.complete("done")
            assert p1.toString() == "GroovyPromise{completed}"

            def cf2 = new CompletableFuture<String>()
            cf2.completeExceptionally(new RuntimeException("err"))
            def p2 = new GroovyPromise(cf2)
            assert p2.toString() == "GroovyPromise{failed}"
        '''
    }

    @Test
    void testGroovyPromiseOfFactory() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture("hello")
            def promise = GroovyPromise.of(cf)
            assert promise instanceof GroovyPromise
            assert promise.get() == "hello"
        '''
    }

    // ================================================================
    // Combinator edge cases: all/any/allSettled with empty/null
    // ================================================================

    @Test
    void testAllWithEmptySources() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await(Awaitable.all())
            assert results == []
        '''
    }

    @Test
    void testAllSettledWithEmptySources() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await(Awaitable.allSettled())
            assert results == []
        '''
    }

    @Test
    void testAnyWithSingleSource() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = await(Awaitable.any(Awaitable.of(42)))
            assert result == 42
        '''
    }

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

            async def succeed() { "ok" }
            async def fail() { throw new IOException("boom") }

            def results = await(Awaitable.allSettled(succeed(), fail()))
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
    void testAwaitableOfNull() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.of(null)
            assert a.isDone()
            assert await(a) == null
        '''
    }

    @Test
    void testAwaitableFailedPropagates() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.failed(new ArithmeticException("div by zero"))
            assert a.isDone()
            assert a.isCompletedExceptionally()

            try {
                await(a)
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
    void testChainingThenMultipleTimes() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = await(
                Awaitable.of(5)
                    .then { it * 2 }
                    .then { it + 3 }
                    .then { "result=$it" }
            )
            assert result == "result=13"
        '''
    }

    @Test
    void testThenComposeChain() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = await(
                Awaitable.of(1)
                    .thenCompose { Awaitable.of(it + 10) }
                    .thenCompose { Awaitable.of(it * 2) }
            )
            assert result == 22
        '''
    }

    @Test
    void testExceptionallyWithThenChain() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Error at start of chain, recovered in the middle
            def result = await(
                Awaitable.failed(new IOException("err"))
                    .exceptionally { -1 }
                    .then { it * 10 }
            )
            assert result == -10
        '''
    }

    // ================================================================
    // Blocking awaitAll/awaitAny (AsyncSupport-level coverage)
    // ================================================================

    @Test
    void testBlockingAwaitAllWithMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            // all() accepts CompletableFuture alongside Awaitable
            def cf = CompletableFuture.completedFuture("from-cf")
            def aw = Awaitable.of("from-awaitable")

            async def compute() { "from-async" }

            def results = await Awaitable.all(cf, aw, compute())
            assert results == ["from-cf", "from-awaitable", "from-async"]
        '''
    }

    @Test
    void testAnyWithMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture("winner")
            def slow = Awaitable.delay(5000).then { "slow" }

            def result = await Awaitable.any(cf, slow)
            assert result == "winner"
            slow.cancel()
        '''
    }

    // ================================================================
    // Defer cleanup coverage
    // ================================================================

    @Test
    void testDeferExecutesOnReturn() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CopyOnWriteArrayList

            class DeferTest {
                def log = new CopyOnWriteArrayList<String>()

                async def withCleanup() {
                    defer { log.add("cleanup") }
                    log.add("work")
                    "result"
                }
            }

            def t = new DeferTest()
            assert await(t.withCleanup()) == "result"
            assert t.log == ["work", "cleanup"]
        '''
    }

    @Test
    void testDeferExecutesOnException() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CopyOnWriteArrayList

            class DeferFailTest {
                def log = new CopyOnWriteArrayList<String>()

                async def failing() {
                    defer { log.add("cleanup") }
                    throw new IOException("oops")
                }
            }

            def t = new DeferFailTest()
            try {
                await t.failing()
            } catch (IOException e) {
                // expected
            }
            assert t.log == ["cleanup"]
        '''
    }

    // ================================================================
    // for await coverage
    // ================================================================

    @Test
    void testForAwaitWithIterable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def process() {
                def items = []
                for await (item in [1, 2, 3]) {
                    items << item
                }
                items
            }

            assert await(process()) == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitWithYieldReturn() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def generate() {
                yield return "a"
                yield return "b"
                yield return "c"
            }

            async def consume() {
                def items = []
                for await (item in generate()) {
                    items << item
                }
                items
            }

            assert await(consume()) == ["a", "b", "c"]
        '''
    }

    // ================================================================
    // Async closure / lambda coverage
    // ================================================================

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



    // ================================================================
    // 1. AsyncSupport.await(Awaitable) — InterruptedException branch
    // ================================================================

    @Test
    void testAwaitAwaitableInterruptedThrowsCancellation() {
        def cf = new CompletableFuture<String>()
        def awaitable = new GroovyPromise<String>(cf)
        Thread.currentThread().interrupt()
        try {
            AsyncSupport.await(awaitable)
            assert false : 'Should have thrown'
        } catch (CancellationException e) {
            assert e.message.contains('Interrupted')
            assert e.cause instanceof InterruptedException
        } finally {
            Thread.interrupted() // clear interrupt flag
            cf.cancel(true)
        }
    }

    // ================================================================
    // 2. AsyncSupport.await(CompletableFuture) — cancel + exceptional
    // ================================================================

    @Test
    void testAwaitCancelledCompletableFuture() {
        def cf = new CompletableFuture<String>()
        cf.cancel(true)
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (CancellationException ignored) {
            // expected — covers line 217-218
        }
    }

    @Test
    void testAwaitExceptionalCompletableFuture() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new IOException('test-io'))
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (IOException e) {
            assert e.message == 'test-io'
        }
    }

    // ================================================================
    // 3. AsyncSupport.await(CompletionStage) — line 231
    // ================================================================

    @Test
    void testAwaitCompletionStage() {
        // Create a pure CompletionStage wrapper so Groovy dispatches to await(CompletionStage)
        def cf = CompletableFuture.completedFuture('from-stage')
        CompletionStage<String> stage = new CompletionStage<String>() {
            @Delegate CompletionStage<String> delegate = cf
        }
        assert AsyncSupport.await(stage) == 'from-stage'
    }

    // ================================================================
    // 4. AsyncSupport.await(Future) — plain Future path
    // ================================================================

    @Test
    void testAwaitPlainFutureSuccess() {
        def ft = new FutureTask<String>({ 'hello' } as Callable<String>)
        ft.run()
        assert AsyncSupport.await((java.util.concurrent.Future<String>) ft) == 'hello'
    }

    @Test
    void testAwaitPlainFutureFailed() {
        def ft = new FutureTask<String>({ throw new IOException('future-error') } as Callable<String>)
        ft.run()
        try {
            AsyncSupport.await((java.util.concurrent.Future<String>) ft)
            assert false
        } catch (IOException e) {
            assert e.message == 'future-error'
        }
    }

    @Test
    void testAwaitPlainFutureCancelled() {
        def ft = new FutureTask<String>({ 'never' } as Callable<String>)
        ft.cancel(true)
        try {
            AsyncSupport.await((java.util.concurrent.Future<String>) ft)
            assert false
        } catch (CancellationException ignored) {}
    }

    @Test
    void testAwaitPlainFutureInterrupted() {
        // FutureTask that blocks, allowing interrupt to fire
        def ft = new FutureTask<String>({
            Thread.sleep(60_000)
            'never'
        } as Callable<String>)
        // Don't run it — it will block on get(), and we set interrupt
        Thread.currentThread().interrupt()
        try {
            AsyncSupport.await((java.util.concurrent.Future<String>) ft)
            assert false
        } catch (CancellationException e) {
            assert e.message.contains('Interrupted')
            assert e.cause instanceof InterruptedException
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    void testAwaitFutureWithCompletableFuture() {
        // Pass CF as Future to hit line 245: instanceof CompletableFuture shortcut
        def cf = CompletableFuture.completedFuture('via-cf')
        assert AsyncSupport.await((java.util.concurrent.Future<String>) cf) == 'via-cf'
    }

    // ================================================================
    // 5. AsyncSupport.await(Object) — null, Closure, adapter branches
    // ================================================================

    @Test
    void testAwaitObjectNull() {
        assert AsyncSupport.await((Object) null) == null
    }

    @Test
    void testAwaitObjectAwaitable() {
        assert AsyncSupport.await((Object) Awaitable.of('aw')) == 'aw'
    }

    @Test
    void testAwaitObjectCompletableFuture() {
        assert AsyncSupport.await((Object) CompletableFuture.completedFuture('cf')) == 'cf'
    }

    @Test
    void testAwaitObjectCompletionStage() {
        // Wrap so it's NOT instanceof CompletableFuture — hits line 277
        def cf = CompletableFuture.completedFuture('cs!')
        CompletionStage<String> stage = new CompletionStage<String>() {
            @Delegate CompletionStage<String> delegate = cf
        }
        assert AsyncSupport.await((Object) stage) == 'cs!'
    }

    @Test
    void testAwaitObjectPlainFuture() {
        def ft = new FutureTask<String>({ 'ft' } as Callable<String>)
        ft.run()
        assert AsyncSupport.await((Object) ft) == 'ft'
    }

    @Test
    void testAwaitObjectClosureThrows() {
        try {
            AsyncSupport.await((Object) { 42 })
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Cannot await a Closure')
            assert e.message.contains('myClosure()')
        }
    }

    @Test
    void testAwaitObjectViaAdapter() {
        // SubmissionPublisher is a Flow.Publisher — hits adapter fallback
        def pub = new SubmissionPublisher<String>()
        Thread.start {
            Thread.sleep(100)
            pub.submit('adapted')
            pub.close()
        }
        assert AsyncSupport.await((Object) pub) == 'adapted'
    }

    // ================================================================
    // 6. AsyncSupport.async(Closure) — line 359
    // ================================================================

    @Test
    void testAsyncClosure() {
        Awaitable<Integer> aw = AsyncSupport.async({ 42 + 8 })
        assert aw.get() == 50
    }

    // ================================================================
    // 7. AsyncSupport.executeAsyncVoid — error path
    // ================================================================

    @Test
    void testExecuteAsyncVoidSuccess() {
        def flag = new CompletableFuture<Boolean>()
        Awaitable<Void> aw = AsyncSupport.executeAsyncVoid({ flag.complete(true) }, AsyncSupport.getExecutor())
        aw.get()
        assert flag.get() == true
    }

    @Test
    void testExecuteAsyncVoidError() {
        Awaitable<Void> aw = AsyncSupport.executeAsyncVoid(
            { throw new RuntimeException('boom') }, AsyncSupport.getExecutor())
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof RuntimeException
            assert e.cause.message == 'boom'
        }
    }

    // ================================================================
    // 8. Blocking awaitAll
    // ================================================================

    @Test
    void testAwaitAllHappyPath() {
        def results = AsyncSupport.awaitAll(Awaitable.of('a'), Awaitable.of('b'))
        assert results == ['a', 'b']
    }

    @Test
    void testAwaitAllEmpty() {
        assert AsyncSupport.awaitAll() == []
    }

    @Test
    void testAwaitAllNullArray() {
        assert AsyncSupport.awaitAll(null as Object[]) == []
    }

    @Test
    void testAwaitAllNullElement() {
        try {
            AsyncSupport.awaitAll(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAwaitAllWithFailure() {
        try {
            AsyncSupport.awaitAll(Awaitable.of('ok'), Awaitable.failed(new IOException('fail')))
            assert false
        } catch (IOException e) {
            assert e.message == 'fail'
        }
    }

    // ================================================================
    // 9. Blocking awaitAny
    // ================================================================

    @Test
    void testAwaitAnyHappyPath() {
        def result = AsyncSupport.awaitAny(Awaitable.of('first'), Awaitable.delay(5000))
        assert result == 'first'
    }

    @Test
    void testAwaitAnyEmpty() {
        try {
            AsyncSupport.awaitAny()
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('at least one')
        }
    }

    @Test
    void testAwaitAnyNullElement() {
        try {
            AsyncSupport.awaitAny(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAwaitAnyWithFailure() {
        try {
            AsyncSupport.awaitAny(Awaitable.failed(new ArithmeticException('math')))
            assert false
        } catch (ArithmeticException e) {
            assert e.message == 'math'
        }
    }

    // ================================================================
    // 10. Blocking awaitAllSettled
    // ================================================================

    @Test
    void testAwaitAllSettledMixed() {
        def results = AsyncSupport.awaitAllSettled(
            Awaitable.of('good'),
            Awaitable.failed(new IOException('bad'))
        )
        assert results.size() == 2
        assert results[0].isSuccess()
        assert results[0].value == 'good'
        assert results[1].isFailure()
        assert results[1].error instanceof IOException
    }

    @Test
    void testAwaitAllSettledEmpty() {
        assert AsyncSupport.awaitAllSettled() == []
    }

    @Test
    void testAwaitAllSettledNullArray() {
        assert AsyncSupport.awaitAllSettled(null as Object[]) == []
    }

    @Test
    void testAwaitAllSettledNullElement() {
        try {
            AsyncSupport.awaitAllSettled(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAwaitAllSettledWithCancellation() {
        def cf = new CompletableFuture<String>()
        cf.cancel(true)
        def results = AsyncSupport.awaitAllSettled(Awaitable.of('ok'), cf)
        assert results.size() == 2
        assert results[0].isSuccess()
        assert results[1].isFailure()
        assert results[1].error instanceof CancellationException
    }

    // ================================================================
    // 11. anyAsync null element
    // ================================================================

    @Test
    void testAnyAsyncNullElement() {
        try {
            AsyncSupport.anyAsync(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAnyAsyncEmptyThrows() {
        try {
            AsyncSupport.anyAsync()
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('at least one')
        }
    }

    // ================================================================
    // 12. allSettledAsync null element
    // ================================================================

    @Test
    void testAllSettledAsyncNullElement() {
        try {
            AsyncSupport.allSettledAsync(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAllSettledAsyncEmpty() {
        def aw = AsyncSupport.allSettledAsync()
        assert aw.get() == []
    }

    @Test
    void testAllSettledAsyncMixed() {
        def cf = new CompletableFuture<String>()
        cf.cancel(true)
        def aw = AsyncSupport.allSettledAsync(Awaitable.of('ok'), cf)
        def results = aw.get()
        assert results[0].isSuccess()
        assert results[0].value == 'ok'
        assert results[1].isFailure()
        assert results[1].error instanceof CancellationException
    }

    // ================================================================
    // allAsync — direct tests
    // ================================================================

    @Test
    void testAllAsyncHappyPath() {
        def aw = AsyncSupport.allAsync(Awaitable.of('a'), Awaitable.of('b'))
        assert aw.get() == ['a', 'b']
    }

    @Test
    void testAllAsyncEmpty() {
        def aw = AsyncSupport.allAsync()
        assert aw.get() == []
    }

    @Test
    void testAllAsyncNullElement() {
        try {
            AsyncSupport.allAsync(Awaitable.of(1), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('index 1')
        }
    }

    @Test
    void testAllAsyncWithCompletionStage() {
        // Pure CompletionStage wrapper — hits toCompletableFuture line 534
        def cf = CompletableFuture.completedFuture('cs')
        CompletionStage<String> stage = new CompletionStage<String>() {
            @Delegate CompletionStage<String> delegate = cf
        }
        def aw = AsyncSupport.allAsync(stage, Awaitable.of('aw'))
        assert aw.get() == ['cs', 'aw']
    }

    @Test
    void testAwaitAllWithCompletionStage() {
        // Blocking awaitAll with pure CompletionStage — hits toCompletableFuture line 534
        def cf = CompletableFuture.completedFuture('val')
        CompletionStage<String> stage = new CompletionStage<String>() {
            @Delegate CompletionStage<String> delegate = cf
        }
        def results = AsyncSupport.awaitAll(stage)
        assert results == ['val']
    }

    // ================================================================
    // 13. delay null unit  + other delay paths
    // ================================================================

    @Test
    void testDelayNullUnit() {
        try {
            AsyncSupport.delay(1, null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    @Test
    void testDelayNegative() {
        try {
            AsyncSupport.delay(-1)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('negative')
        }
    }

    @Test
    void testDelayZero() {
        def aw = AsyncSupport.delay(0, TimeUnit.MILLISECONDS)
        assert aw.get() == null
    }

    @Test
    void testDelayPositive() {
        def aw = AsyncSupport.delay(10, TimeUnit.MILLISECONDS)
        assert aw.get() == null
    }

    // ================================================================
    // 14. yieldReturn null generator  + outside context
    // ================================================================

    @Test
    void testYieldReturnNullGenerator() {
        try {
            AsyncSupport.yieldReturn(null, 'x')
            assert false
        } catch (IllegalStateException e) {
            assert e.message.contains('outside')
        }
    }

    @Test
    void testYieldReturnOutsideContext() {
        try {
            AsyncSupport.yieldReturn('value')
            assert false
        } catch (IllegalStateException e) {
            assert e.message.contains('yield return')
        }
    }

    @Test
    void testYieldReturnWithValidGenerator() {
        def gen = new AsyncStreamGenerator<String>()
        // yield blocks until consumer takes, so run in background
        Thread.start { AsyncSupport.yieldReturn(gen, 'hello') ; gen.complete() }
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 'hello'
        assert gen.moveNext().get() == false
    }

    // ================================================================
    // 15. defer null scope/action
    // ================================================================

    @Test
    void testDeferNullScope() {
        try {
            AsyncSupport.defer(null, { println 'cleanup' })
            assert false
        } catch (IllegalStateException e) {
            assert e.message.contains('async method')
        }
    }

    @Test
    void testDeferNullAction() {
        try {
            AsyncSupport.defer(new ArrayDeque(), null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    @Test
    void testDeferAndExecuteScope() {
        def scope = AsyncSupport.createDeferScope()
        def log = []
        AsyncSupport.defer(scope, { log << 'first' })
        AsyncSupport.defer(scope, { log << 'second' })
        AsyncSupport.executeDeferScope(scope)
        // LIFO order
        assert log == ['second', 'first']
    }

    // ================================================================
    // 16. executeDeferScope multiple exceptions
    // ================================================================

    @Test
    void testExecuteDeferScopeMultipleExceptions() {
        def scope = AsyncSupport.createDeferScope()
        scope.push({ throw new IOException('first') })
        scope.push({ throw new ArithmeticException('second') })
        try {
            AsyncSupport.executeDeferScope(scope)
            assert false
        } catch (ArithmeticException e) {
            // second runs first (LIFO)
            assert e.message == 'second'
            assert e.suppressed.length == 1
            assert e.suppressed[0] instanceof IOException
            assert e.suppressed[0].message == 'first'
        }
    }

    @Test
    void testExecuteDeferScopeNull() {
        // Should be no-op
        AsyncSupport.executeDeferScope(null)
    }

    @Test
    void testExecuteDeferScopeEmpty() {
        AsyncSupport.executeDeferScope(new ArrayDeque())
    }

    // ================================================================
    // 17. rethrowUnwrapped with Error  — indirect via await
    // ================================================================

    @Test
    void testRethrowUnwrappedWithError() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new StackOverflowError('soe'))
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (StackOverflowError e) {
            assert e.message == 'soe'
        }
    }

    @Test
    void testRethrowUnwrappedWithCheckedException() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new IOException('checked'))
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (IOException e) {
            assert e.message == 'checked'
        }
    }

    // ================================================================
    // 18. wrapForFuture with CompletionException
    //     — indirect via executeAsync with a closure that throws CE
    // ================================================================

    @Test
    void testWrapForFutureWithCompletionException() {
        // wrapForFuture returns existing CompletionException as-is
        def ce = new CompletionException('already-wrapped', null)
        Awaitable<String> aw = AsyncSupport.executeAsync(
            { throw ce } as Closure<String>, AsyncSupport.getExecutor())
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            // CompletionException(null cause) → ExecutionException wraps the CE itself
            assert e.cause instanceof CompletionException
        }
    }

    @Test
    void testWrapForFutureWithRegularException() {
        // wrapForFuture wraps non-CE into CompletionException(original)
        Awaitable<String> aw = AsyncSupport.executeAsync(
            { throw new RuntimeException('plain') } as Closure<String>, AsyncSupport.getExecutor())
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            // CF.get() wraps CompletionException in ExecutionException; the original is the cause chain
            assert e.cause instanceof RuntimeException
            assert e.cause.message == 'plain'
        }
    }

    // ================================================================
    // 19. deepUnwrap — direct call
    // ================================================================

    @Test
    void testDeepUnwrap() {
        def root = new IOException('root')
        def wrapped = new CompletionException(new ExecutionException(root))
        assert AsyncSupport.deepUnwrap(wrapped).is(root)
    }

    @Test
    void testDeepUnwrapNoWrapping() {
        def ex = new RuntimeException('plain')
        assert AsyncSupport.deepUnwrap(ex).is(ex)
    }

    // ================================================================
    // 20. AsyncStreamGenerator — direct construction tests
    // ================================================================

    @Test
    void testAsyncStreamGeneratorYieldAndComplete() {
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.yield('a')
            gen.yield('b')
            gen.complete()
        }
        def items = []
        while (gen.moveNext().get()) {
            items << gen.getCurrent()
        }
        assert items == ['a', 'b']
    }

    @Test
    void testAsyncStreamGeneratorErrorItem() {
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.yield('ok')
            gen.error(new IOException('gen-err'))
        }
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 'ok'
        try {
            gen.moveNext()
            assert false
        } catch (IOException e) {
            assert e.message == 'gen-err'
        }
    }

    @Test
    void testAsyncStreamGeneratorErrorWithErrorType() {
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.error(new StackOverflowError('test-soe'))
        }
        try {
            gen.moveNext()
            assert false
        } catch (StackOverflowError e) {
            assert e.message == 'test-soe'
        }
    }

    @Test
    void testAsyncStreamGeneratorErrorWithNull() {
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.error(null) // should be treated as NullPointerException
        }
        try {
            gen.moveNext()
            assert false
        } catch (NullPointerException e) {
            assert e.message.contains('null error')
        }
    }

    // ================================================================
    // 21. AsyncStreamGenerator — InterruptedException paths
    // ================================================================

    @Test
    void testAsyncStreamGeneratorYieldInterrupted() {
        def gen = new AsyncStreamGenerator<String>()
        // Interrupt before yield — the put() will throw InterruptedException
        def result = new CompletableFuture<String>()
        Thread.start {
            Thread.currentThread().interrupt()
            try {
                gen.yield('x')
                result.complete('no-exception')
            } catch (CancellationException e) {
                result.complete(e.message)
            }
        }
        assert result.get().contains('Interrupted')
    }

    @Test
    void testAsyncStreamGeneratorCompleteInterrupted() {
        def gen = new AsyncStreamGenerator<String>()
        // complete() catches InterruptedException and uses offer()
        def done = new CompletableFuture<Boolean>()
        Thread.start {
            Thread.currentThread().interrupt()
            gen.complete()
            done.complete(true)
        }
        // The complete might use offer, so try to consume; the gen should signal done
        done.get(5, TimeUnit.SECONDS)
    }

    @Test
    void testAsyncStreamGeneratorErrorInterrupted() {
        def gen = new AsyncStreamGenerator<String>()
        def done = new CompletableFuture<Boolean>()
        Thread.start {
            Thread.currentThread().interrupt()
            gen.error(new IOException('err'))
            done.complete(true)
        }
        done.get(5, TimeUnit.SECONDS)
    }

    /**
     * Demonstrates why producer thread tracking is essential.
     *
     * Scenario: the producer is blocked in {@code yield()} (waiting for the
     * consumer to call {@code moveNext()}) when the consumer exits the
     * {@code for await} loop early and calls {@code close()}.
     *
     * Without thread tracking, {@code close()} has no way to interrupt the
     * producer's blocking {@code SynchronousQueue.put()}, and the producer
     * thread would hang indefinitely — a permanent thread leak.
     *
     * With thread tracking, {@code close()} interrupts the producer thread,
     * which detects the closed state and exits promptly via
     * {@code CancellationException("Async stream was closed")}.
     */
    @Test
    void testCloseInterruptsBlockedProducerThread() {
        def gen = new AsyncStreamGenerator<Integer>()
        def producerExit = new CompletableFuture<String>()
        def producerBlocked = new java.util.concurrent.CountDownLatch(1)

        def producerThread = Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)    // will be consumed
                producerBlocked.countDown()
                gen.yield(2)    // will block — consumer won't take this
                producerExit.complete('no-exception')
            } catch (CancellationException e) {
                producerExit.complete(e.message)
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // Consume the first element only
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 1

        // Wait until the producer is blocked on yield(2)
        producerBlocked.await(5, TimeUnit.SECONDS)
        Thread.sleep(50) // give put() time to block

        // Close the stream — this must interrupt the blocked producer
        gen.close()

        // The producer thread should exit promptly with the expected message
        assert producerExit.get(5, TimeUnit.SECONDS) == 'Async stream was closed'
        producerThread.join(5000)
        assert !producerThread.isAlive() : 'Producer thread should have terminated'
    }

    /**
     * Demonstrates why consumer thread tracking is essential.
     *
     * Scenario: the consumer is blocked in {@code moveNext()} (waiting for
     * the producer to yield the next element) when an external agent calls
     * {@code close()} from a different thread.
     *
     * Without thread tracking, the consumer's blocking
     * {@code SynchronousQueue.take()} cannot be interrupted and would hang
     * indefinitely.
     *
     * With thread tracking, {@code close()} interrupts the consumer, and
     * {@code moveNext()} detects the closed state and returns
     * {@code Awaitable.of(false)} instead of throwing.
     */
    @Test
    void testCloseInterruptsBlockedConsumerThread() {
        def gen = new AsyncStreamGenerator<Integer>()
        def consumerResult = new CompletableFuture<Boolean>()
        def consumerBlocked = new java.util.concurrent.CountDownLatch(1)

        // Start a consumer thread that will block in moveNext()
        // because no producer will ever yield anything
        Thread.start {
            consumerBlocked.countDown()
            def hasNext = gen.moveNext().get()  // blocks waiting for producer
            consumerResult.complete(hasNext)
        }

        // Wait until the consumer is blocking in moveNext()
        consumerBlocked.await(5, TimeUnit.SECONDS)
        Thread.sleep(50) // give take() time to block

        // Close from a different thread — must interrupt the blocked consumer
        gen.close()

        // moveNext() should return false (not throw), indicating stream exhaustion
        assert consumerResult.get(5, TimeUnit.SECONDS) == false
    }

    // ================================================================
    // 22. AwaitableAdapterRegistry — null inputs
    // ================================================================

    @Test
    void testToAwaitableNull() {
        try {
            AwaitableAdapterRegistry.toAwaitable(null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    @Test
    void testToAsyncStreamNull() {
        try {
            AwaitableAdapterRegistry.toAsyncStream(null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    // ================================================================
    // 23. AsyncStream passthrough
    // ================================================================

    @Test
    void testToAsyncStreamPassthrough() {
        AsyncStream<String> stream = AsyncStream.empty()
        assert AwaitableAdapterRegistry.toAsyncStream(stream).is(stream)
    }

    // ================================================================
    // 24. Unknown type — toAwaitable / toAsyncStream
    // ================================================================

    @Test
    void testToAwaitableUnsupportedType() {
        try {
            AwaitableAdapterRegistry.toAwaitable(new Object())
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('No AwaitableAdapter found')
        }
    }

    @Test
    void testToAsyncStreamUnsupportedType() {
        try {
            AwaitableAdapterRegistry.toAsyncStream(42)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('No AsyncStream adapter')
        }
    }

    // ================================================================
    // 25. Flow.Publisher — onError/onComplete in publisherToAwaitable
    // ================================================================

    @Test
    void testFlowPublisherToAwaitableOnNext() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(pub)
        pub.submit('hello')
        pub.close()
        assert aw.get() == 'hello'
    }

    @Test
    void testFlowPublisherToAwaitableOnError() {
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onError(new IOException('pub-error'))
            }
        }
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(pub)
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
            assert e.cause.message == 'pub-error'
        }
    }

    @Test
    void testFlowPublisherToAwaitableOnCompleteWithoutValue() {
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onComplete()
            }
        }
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(pub)
        assert aw.get() == null
    }

    // ================================================================
    // 26. BuiltInAdapter — plain Future
    // ================================================================

    @Test
    void testFutureAdapterAlreadyDone() {
        def ft = new FutureTask<String>({ 'already-done' } as Callable<String>)
        ft.run()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
        assert aw.get() == 'already-done'
    }

    @Test
    void testFutureAdapterNotYetDone() {
        def ft = new FutureTask<String>({ 'async-done' } as Callable<String>)
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
        Thread.start { Thread.sleep(50); ft.run() }
        assert aw.get() == 'async-done'
    }

    @Test
    void testFutureAdapterWithException() {
        def ft = new FutureTask<String>({ throw new ArithmeticException('div-zero') } as Callable<String>)
        ft.run()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof ArithmeticException
        }
    }

    @Test
    void testFutureAdapterWithBlockingExecutor() {
        // Test the blockingExecutor path
        def ft = new FutureTask<String>({ 'with-executor' } as Callable<String>)
        AwaitableAdapterRegistry.setBlockingExecutor(AsyncSupport.getExecutor())
        try {
            Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
            Thread.start { Thread.sleep(50); ft.run() }
            assert aw.get() == 'with-executor'
        } finally {
            AwaitableAdapterRegistry.setBlockingExecutor(null)
        }
    }

    // ================================================================
    // 27. AwaitableAdapter — default toAsyncStream throws
    // ================================================================

    @Test
    void testAwaitableAdapterDefaultToAsyncStreamThrows() {
        def adapter = new AwaitableAdapter() {
            boolean supportsAwaitable(Class<?> type) { false }
            def <T> Awaitable<T> toAwaitable(Object source) { null }
        }
        try {
            adapter.toAsyncStream('anything')
            assert false
        } catch (UnsupportedOperationException e) {
            assert e.message.contains('not supported')
        }
    }

    @Test
    void testAwaitableAdapterDefaultSupportsAsyncStream() {
        def adapter = new AwaitableAdapter() {
            boolean supportsAwaitable(Class<?> type) { false }
            def <T> Awaitable<T> toAwaitable(Object source) { null }
        }
        assert adapter.supportsAsyncStream(String) == false
    }

    // ================================================================
    // Awaitable passthrough in toAwaitable
    // ================================================================

    @Test
    void testToAwaitablePassthrough() {
        Awaitable<String> aw = Awaitable.of('pass')
        assert AwaitableAdapterRegistry.toAwaitable(aw).is(aw)
    }

    // ================================================================
    // AsyncSupport.toAsyncStream(Object) — null and passthrough
    // ================================================================

    @Test
    void testAsyncSupportToAsyncStreamNull() {
        AsyncStream<String> s = AsyncSupport.toAsyncStream(null)
        assert s.is(AsyncStream.empty())
    }

    @Test
    void testToAsyncStreamPassthroughViaAsyncSupport() {
        AsyncStream<String> stream = AsyncStream.empty()
        assert AsyncSupport.toAsyncStream(stream).is(stream)
    }

    @Test
    void testToAsyncStreamFromIterable() {
        AsyncStream<Integer> stream = AsyncSupport.toAsyncStream([1, 2, 3])
        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == [1, 2, 3]
    }

    @Test
    void testToAsyncStreamFromIterator() {
        AsyncStream<Integer> stream = AsyncSupport.toAsyncStream([4, 5, 6].iterator())
        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == [4, 5, 6]
    }

    // ================================================================
    // Flow.Publisher to AsyncStream — direct tests
    // ================================================================

    @Test
    void testFlowPublisherToAsyncStreamDirect() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AwaitableAdapterRegistry.toAsyncStream(pub)
        Thread.start {
            Thread.sleep(50)
            pub.submit('a')
            pub.submit('b')
            pub.close()
        }
        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == ['a', 'b']
    }

    @Test
    void testFlowPublisherToAsyncStreamWithError() {
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                s.onSubscribe(new Flow.Subscription() {
                    int count = 0
                    void request(long n) {
                        count++
                        if (count == 1) s.onNext('item1')
                        else s.onError(new IOException('stream-err'))
                    }
                    void cancel() {}
                })
            }
        }
        AsyncStream<String> stream = AwaitableAdapterRegistry.toAsyncStream(pub)
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 'item1'
        try {
            stream.moveNext().get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
        }
    }

    // ================================================================
    // Executor configuration
    // ================================================================

    @Test
    void testGetSetExecutor() {
        def original = AsyncSupport.getExecutor()
        assert original != null
        def custom = { Runnable r -> r.run() } as java.util.concurrent.Executor
        AsyncSupport.setExecutor(custom)
        assert AsyncSupport.getExecutor().is(custom)
        AsyncSupport.setExecutor(null) // reset
        assert AsyncSupport.getExecutor() != null
    }

    // ================================================================
    // AwaitableAdapter register/unregister
    // ================================================================

    @Test
    void testAdapterRegisterAndUnregister() {
        def adapter = new AwaitableAdapter() {
            boolean supportsAwaitable(Class<?> type) { false }
            def <T> Awaitable<T> toAwaitable(Object source) { null }
        }
        def handle = AwaitableAdapterRegistry.register(adapter)
        handle.close()
        assert !AwaitableAdapterRegistry.unregister(adapter) // already removed
    }

    // ================================================================
    // AwaitResult — cover all methods
    // ================================================================

    @Test
    void testAwaitResultSuccess() {
        def r = AwaitResult.success('val')
        assert r.isSuccess()
        assert !r.isFailure()
        assert r.value == 'val'
        assert r.getOrElse({ 'fallback' }) == 'val'
        assert r.toString().contains('Success')
        try { r.error; assert false } catch (IllegalStateException ignored) {}
    }

    @Test
    void testAwaitResultFailure() {
        def ex = new IOException('err')
        def r = AwaitResult.failure(ex)
        assert r.isFailure()
        assert !r.isSuccess()
        assert r.error.is(ex)
        assert r.getOrElse({ t -> t.message }) == 'err'
        assert r.toString().contains('Failure')
        try { r.value; assert false } catch (IllegalStateException ignored) {}
    }

    // ================================================================
    // generateAsyncStream — direct test
    // ================================================================

    @Test
    void testGenerateAsyncStream() {
        AsyncStream<Integer> stream = AsyncSupport.generateAsyncStream({ gen ->
            gen.yield(1)
            gen.yield(2)
        })
        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == [1, 2]
    }

    @Test
    void testGenerateAsyncStreamWithError() {
        AsyncStream<Integer> stream = AsyncSupport.generateAsyncStream({ gen ->
            gen.yield(1)
            throw new IOException('gen-fail')
        })
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 1
        try {
            stream.moveNext()
            assert false
        } catch (IOException e) {
            assert e.message == 'gen-fail'
        }
    }

    // ================================================================
    // wrapAsync — direct test
    // ================================================================

    @Test
    void testWrapAsync() {
        def wrapped = AsyncSupport.wrapAsync({ x -> x * 2 })
        Awaitable<Integer> aw = wrapped.call(21)
        assert aw.get() == 42
    }

    @Test
    void testWrapAsyncError() {
        def wrapped = AsyncSupport.wrapAsync({ throw new RuntimeException('wrap-err') })
        Awaitable<Object> aw = wrapped.call()
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof RuntimeException
            assert e.cause.message == 'wrap-err'
        }
    }

    // ================================================================
    // wrapAsyncGenerator — direct test
    // ================================================================

    @Test
    void testWrapAsyncGenerator() {
        def wrapped = AsyncSupport.wrapAsyncGenerator({ gen, Object[] args ->
            gen.yield('x')
            gen.yield('y')
        })
        AsyncStream<String> stream = wrapped.call()
        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == ['x', 'y']
    }

    // ================================================================
    // Awaitable.of / Awaitable.failed — factory coverage
    // ================================================================

    @Test
    void testAwaitableOf() {
        def aw = Awaitable.of('hello')
        assert aw.isDone()
        assert !aw.isCancelled()
        assert !aw.isCompletedExceptionally()
        assert aw.get() == 'hello'
        assert aw.get(1, TimeUnit.SECONDS) == 'hello'
    }

    @Test
    void testAwaitableFailed() {
        def aw = Awaitable.failed(new IOException('err'))
        assert aw.isDone()
        assert aw.isCompletedExceptionally()
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
        }
    }

    // ================================================================
    // Awaitable combinators — coverage for delegation to AsyncSupport
    // ================================================================

    @Test
    void testAwaitableAll() {
        def aw = Awaitable.all(Awaitable.of('x'), Awaitable.of('y'))
        assert aw.get() == ['x', 'y']
    }

    @Test
    void testAwaitableAny() {
        def aw = Awaitable.any(Awaitable.of('first'), Awaitable.delay(5000))
        assert aw.get() == 'first'
    }

    @Test
    void testAwaitableAllSettled() {
        def aw = Awaitable.allSettled(
            Awaitable.of('ok'),
            Awaitable.failed(new IOException('err'))
        )
        def results = aw.get()
        assert results[0].isSuccess()
        assert results[1].isFailure()
    }

    @Test
    void testAwaitableDelay() {
        def aw = Awaitable.delay(10)
        assert aw.get() == null
    }

    // ================================================================
    // Awaitable — then/thenCompose/exceptionally
    // ================================================================

    @Test
    void testAwaitableThen() {
        def aw = Awaitable.of(5).then({ it * 2 })
        assert aw.get() == 10
    }

    @Test
    void testAwaitableThenCompose() {
        def aw = Awaitable.of(5).thenCompose({ Awaitable.of(it + 1) })
        assert aw.get() == 6
    }

    @Test
    void testAwaitableExceptionally() {
        def aw = Awaitable.failed(new IOException('err')).exceptionally({ 'recovered' })
        assert aw.get() == 'recovered'
    }

    @Test
    void testAwaitableToCompletableFuture() {
        def cf = Awaitable.of('val').toCompletableFuture()
        assert cf instanceof CompletableFuture
        assert cf.get() == 'val'
    }

    @Test
    void testAwaitableCancel() {
        def cf = new CompletableFuture<String>()
        def aw = new GroovyPromise<String>(cf)
        aw.cancel()
        assert aw.isCancelled()
    }

    // ================================================================
    // Tests that require async/await syntax (assertScript)
    // ================================================================

    @Test
    void testAsyncGeneratorErrorPropagation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def failingGenerator() {
                yield return "first"
                throw new IOException("generator-error")
            }

            async def consume() {
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

            async def errorGenerator() {
                yield return "before-error"
                throw new StackOverflowError("test-soe")
            }

            async def consume() {
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

    @Test
    void testDeferScopeMultipleExceptionsViaAsyncDef() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class DeferMultiError {
                async def run() {
                    defer { throw new IOException("first") }
                    defer { throw new ArithmeticException("second") }
                    "ok"
                }
            }

            try {
                await new DeferMultiError().run()
                assert false
            } catch (ArithmeticException e) {
                assert e.message == "second"
                assert e.suppressed.length == 1
                assert e.suppressed[0] instanceof IOException
            }
        '''
    }

    @Test
    void testExecuteAsyncVoidViaAsyncDef() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class VoidService {
                async void doWork() {
                    throw new IOException("void-error")
                }
            }

            def svc = new VoidService()
            def task = svc.doWork()
            try {
                await(task)
                assert false
            } catch (IOException e) {
                assert e.message == "void-error"
            }
        '''
    }

    @Test
    void testAwaitInsideNestedClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def process() {
                def items = [1, 2, 3]
                def results = items.collect { num ->
                    await(Awaitable.of(num * 10))
                }
                results
            }

            assert await(process()) == [10, 20, 30]
        '''
    }

    @Test
    void testAsyncGeneratorClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class GenHelper {
                async def generate() {
                    yield return "x"
                    yield return "y"
                }
            }

            async def consume() {
                def h = new GenHelper()
                def items = []
                for await (item in h.generate()) {
                    items << item
                }
                items
            }

            assert await(consume()) == ["x", "y"]
        '''
    }

    @Test
    void testAsyncGeneratorMultipleYields() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def numbers() {
                for (int i = 1; i <= 5; i++) {
                    yield return i
                }
            }

            async def sum() {
                int total = 0
                for await (n in numbers()) {
                    total += n
                }
                total
            }

            assert await(sum()) == 15
        '''
    }

    @Test
    void testForAwaitWithPublisher() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.SubmissionPublisher

            def pub = new SubmissionPublisher<String>()

            Thread.start {
                Thread.sleep(50)
                pub.submit("a")
                pub.submit("b")
                pub.submit("c")
                pub.close()
            }

            async def consume(pub) {
                def items = []
                for await (item in pub) {
                    items << item
                }
                items
            }

            assert await(consume(pub)) == ["a", "b", "c"]
        '''
    }

    // ================================================================
    // Additional coverage: AsyncStreamGenerator moveNext interrupted
    // ================================================================

    @Test
    void testAsyncStreamGeneratorMoveNextInterrupted() {
        def gen = new AsyncStreamGenerator<String>()
        // queue.take() will throw InterruptedException when interrupted
        Thread.currentThread().interrupt()
        try {
            gen.moveNext()
            assert false
        } catch (CancellationException e) {
            assert e.message.contains('Interrupted during moveNext')
        } finally {
            Thread.interrupted() // clear flag
        }
    }

    // ================================================================
    // Additional coverage: toCompletableFuture with adapter fallback
    // ================================================================

    @Test
    void testAwaitAllWithFlowPublisher() {
        // SubmissionPublisher goes through adapter fallback in toCompletableFuture
        def pub = new SubmissionPublisher<String>()
        Thread.start {
            Thread.sleep(100)
            pub.submit('pub-val')
            pub.close()
        }
        def results = AsyncSupport.awaitAll(Awaitable.of('aw'), pub)
        assert results == ['aw', 'pub-val']
    }

    // ================================================================
    // Additional coverage: await(Awaitable) ExecutionException path
    // ================================================================

    @Test
    void testAwaitAwaitableExecutionException() {
        // Create Awaitable whose get() throws the exception directly
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new IOException('wrapped'))
        def aw = new GroovyPromise<String>(cf)
        try {
            AsyncSupport.await(aw)
            assert false
        } catch (IOException e) {
            assert e.message == 'wrapped'
        }
    }

    // ================================================================
    // Additional coverage: await(CompletableFuture) CompletionException path
    // ================================================================

    @Test
    void testAwaitCompletableFutureWithWrappedException() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new ArithmeticException('math'))
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (ArithmeticException e) {
            assert e.message == 'math'
        }
    }

    // ================================================================
    // Additional coverage: await(Future) ExecutionException path
    // ================================================================

    @Test
    void testAwaitPlainFutureExecutionException() {
        def ft = new FutureTask<String>({ throw new ArithmeticException('div-zero') } as Callable<String>)
        ft.run()
        try {
            AsyncSupport.await((java.util.concurrent.Future<String>) ft)
            assert false
        } catch (ArithmeticException e) {
            assert e.message == 'div-zero'
        }
    }

    // ================================================================
    // Additional coverage: BuiltInAdapter.completeFrom exception
    // ================================================================

    @Test
    void testFutureAdapterCompleteFromException() {
        def ft = new FutureTask<String>({ throw new IOException('ft-err') } as Callable<String>)
        ft.run()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
        try {
            aw.get()
            assert false
        } catch (Exception e) {
            // IOException wrapped in ExecutionException, then unwrapped
            assert e instanceof IOException || e instanceof java.util.concurrent.ExecutionException
        }
    }

    // ================================================================
    // Additional coverage: BuiltInAdapter non-done Future
    // ================================================================

    @Test
    void testFutureAdapterNotDone() {
        def ft = new FutureTask<String>({ 'delayed' } as Callable<String>)
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(ft)
        // Run the FutureTask after adapter wraps it
        Thread.start {
            Thread.sleep(50)
            ft.run()
        }
        assert aw.get() == 'delayed'
    }

    // ================================================================
    // Additional coverage: Flow.Publisher onComplete with no items
    // ================================================================

    @Test
    void testFlowPublisherOnCompleteNoItems() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(pub)
        // Close immediately — triggers onComplete with no onNext
        pub.close()
        assert aw.get() == null
    }

    // ================================================================
    // Additional coverage: Flow.Publisher onError
    // ================================================================

    @Test
    void testFlowPublisherOnError() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = AwaitableAdapterRegistry.toAwaitable(pub)
        Thread.start {
            Thread.sleep(50)
            pub.closeExceptionally(new IOException('pub-err'))
        }
        try {
            aw.get()
            assert false
        } catch (Exception e) {
            // The IOException is wrapped by publisherToAwaitable
            assert e instanceof IOException || e.cause instanceof IOException
        }
    }

    // ================================================================
    // Additional: Flow.Publisher→AsyncStream moveNext InterruptedException
    // ================================================================

    @Test
    void testFlowPublisherAsyncStreamMoveNextInterrupted() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AwaitableAdapterRegistry.toAsyncStream(pub)
        // Interrupt before moveNext to trigger InterruptedException in queue.take()
        Thread.currentThread().interrupt()
        try {
            stream.moveNext().get()
            assert false : 'Expected CancellationException'
        } catch (CancellationException e) {
            // moveNext() throws CancellationException directly (not via CF),
            // ensuring deterministic message across all JDK versions.
            assert e.message.contains('Interrupted')
            assert e.cause instanceof InterruptedException
            // Interrupt flag is restored per Java convention.
            assert Thread.currentThread().isInterrupted()
        } finally {
            Thread.interrupted() // clear flag
            pub.close()
        }
    }

    // ================================================================
    // Additional: Flow.Publisher→AsyncStream onError path
    // ================================================================

    @Test
    void testFlowPublisherAsyncStreamOnError() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AwaitableAdapterRegistry.toAsyncStream(pub)
        Thread.start {
            Thread.sleep(50)
            pub.closeExceptionally(new IOException('stream-err'))
        }
        try {
            stream.moveNext().get()
            assert false
        } catch (Exception e) {
            assert e instanceof IOException || e.cause instanceof IOException
        }
    }

    // ================================================================
    // Additional: Flow.Publisher→AsyncStream onComplete path
    // ================================================================

    @Test
    void testFlowPublisherAsyncStreamOnComplete() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AwaitableAdapterRegistry.toAsyncStream(pub)
        Thread.start {
            Thread.sleep(50)
            pub.submit('item1')
            Thread.sleep(50)
            pub.close()
        }
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 'item1'
        assert stream.moveNext().get() == false
    }
}
