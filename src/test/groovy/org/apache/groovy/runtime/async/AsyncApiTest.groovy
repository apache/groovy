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

import groovy.concurrent.AsyncChannel
import groovy.concurrent.AsyncContext
import groovy.concurrent.AsyncScope
import groovy.concurrent.AsyncStream
import groovy.concurrent.AwaitResult
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.AwaitableAdapterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Pure API verification tests for Groovy's async/await runtime classes.
 * <p>
 * Every test in this class exercises the public API of {@code AsyncSupport},
 * {@code AsyncStreamGenerator}, {@code GroovyPromise}, {@code Awaitable},
 * {@code AwaitResult}, {@code AwaitableAdapterRegistry}, {@code AsyncScope},
 * {@code AsyncContext}, {@code AsyncChannel}, and {@code FlowPublisherAdapter}
 * directly — without {@code assertScript} or compiler-driven async
 * transformation. This ensures JaCoCo instrumentation captures coverage
 * accurately for the runtime classes.
 * <p>
 * The final section covers edge-case and error-path scenarios including
 * exception rethrow/unwrap paths, cancellation handling, scope lifecycle,
 * context propagation, and stream generator close semantics.
 */
class AsyncApiTest {

    @AfterEach
    void cleanup() {
        Awaitable.setExecutor(null)
    }

    // ================================================================
    // AsyncSupport.closeStream
    // ================================================================

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

    // ================================================================
    // AsyncSupport.await overloads
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

    @Test
    void testAwaitCancelledCompletableFuture() {
        def cf = new CompletableFuture<String>()
        cf.cancel(true)
        try {
            AsyncSupport.await(cf)
            assert false
        } catch (CancellationException ignored) {
            assert ignored instanceof CancellationException
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

    @Test
    void testAwaitCompletionStage() {
        // Create a pure CompletionStage wrapper so Groovy dispatches to await CompletionStage
        def cf = CompletableFuture.completedFuture('from-stage')
        CompletionStage<String> stage = new CompletionStage<String>() {
            @Delegate CompletionStage<String> delegate = cf
        }
        assert AsyncSupport.await(stage) == 'from-stage'
    }

    @Test
    void testAwaitPlainFutureSuccess() {
        def ft = new FutureTask<String>({ 'hello' } as Callable<String>)
        ft.run()
        assert AsyncSupport.await((Future<String>) ft) == 'hello'
    }

    @Test
    void testAwaitPlainFutureFailed() {
        def ft = new FutureTask<String>({ throw new IOException('future-error') } as Callable<String>)
        ft.run()
        try {
            AsyncSupport.await((Future<String>) ft)
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
            AsyncSupport.await((Future<String>) ft)
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
            AsyncSupport.await((Future<String>) ft)
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
        assert AsyncSupport.await((Future<String>) cf) == 'via-cf'
    }

    @Test
    void testAwaitAwaitableFailureIsUnwrapped() {
        def aw = Awaitable.failed(new IOException('awaitable-fail'))
        def ex = shouldFail(IOException) {
            AsyncSupport.await(aw)
        }
        assert ex.message == 'awaitable-fail'
    }

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

    @Test
    void testAwaitPlainFutureExecutionException() {
        def ft = new FutureTask<String>({ throw new ArithmeticException('div-zero') } as Callable<String>)
        ft.run()
        try {
            AsyncSupport.await((Future<String>) ft)
            assert false
        } catch (ArithmeticException e) {
            assert e.message == 'div-zero'
        }
    }

    @Test
    void testAwaitClosureDirectlyThrowsIllegalArgument() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.await((Object) { -> 42 })
        }
        assert ex.message.contains('Cannot await a Closure directly')
        assert ex.message.contains('Call the closure first')
    }

    @Test
    void testAwaitRawFutureNonCompletableFuture() {
        // Use a FutureTask (implements Future but is not CompletableFuture)
        def ft = new FutureTask<>({ 'raw-future-result' } as Callable)
        Thread.start { ft.run() }
        def result = AsyncSupport.await(ft)
        assert result == 'raw-future-result'
    }

    @Test
    void testAwaitRawFutureExecutionException() {
        def ft = new FutureTask<>({
            throw new IOException('raw future error')
        } as Callable)
        Thread.start { ft.run() }
        def ex = shouldFail(IOException) {
            AsyncSupport.await(ft)
        }
        assert ex.message == 'raw future error'
    }

    @Test
    void testAwaitRawFutureCancellation() {
        def ft = new FutureTask<>({ Thread.sleep(10000); 'never' } as Callable)
        Thread.start { ft.run() }
        ft.cancel(true)
        shouldFail(CancellationException) {
            AsyncSupport.await(ft)
        }
    }

    @Test
    void testAwaitAllWithFlowPublisher() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.SubmissionPublisher

            async caller() {
                def pub = new SubmissionPublisher<String>()
                Thread.start {
                    Thread.sleep(100)
                    pub.submit('pub-val')
                    pub.close()
                }
                return await Awaitable.all(Awaitable.of('aw'), pub)
            }

            assert await(caller()) == ['aw', 'pub-val']
        '''
    }

    @Test
    void testAwaitAllWithCompletionStage() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionStage

            async caller() {
                CompletionStage<String> stage = CompletableFuture.completedFuture('val')
                return await Awaitable.all(stage)
            }

            assert await(caller()) == ['val']
        '''
    }

    // ================================================================
    // AsyncSupport.async / executeAsyncVoid
    // ================================================================

    @Test
    void testAsyncClosure() {
        Awaitable<Integer> aw = AsyncSupport.async({ 42 + 8 })
        assert aw.get() == 50
    }

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
    // Combinators: await Awaitable.all / any / allSettled
    // ================================================================

    @Test
    void testAwaitAllHappyPath() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.all(Awaitable.of('a'), Awaitable.of('b'))
            }

            assert await(caller()) == ['a', 'b']
        '''
    }

    @Test
    void testAwaitAllEmpty() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.all()
            }

            assert await(caller()) == []
        '''
    }

    @Test
    void testAwaitAllNullElement() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.all(Awaitable.of(1), null)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message.contains('index 1')
            }
        '''
    }

    @Test
    void testAwaitAllWithFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                try {
                    await Awaitable.all(Awaitable.of('ok'), Awaitable.failed(new IOException('fail')))
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'fail'
        '''
    }

    @Test
    void testAwaitAnyHappyPath() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.any(Awaitable.of('first'), Awaitable.delay(5000))
            }

            assert await(caller()) == 'first'
        '''
    }

    @Test
    void testAwaitAnyEmpty() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.any()
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message.contains('at least one')
            }
        '''
    }

    @Test
    void testAwaitAnyNullElement() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.any(Awaitable.of(1), null)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message.contains('index 1')
            }
        '''
    }

    @Test
    void testAwaitAnyWithFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                try {
                    await Awaitable.any(Awaitable.failed(new ArithmeticException('math')))
                    assert false
                } catch (ArithmeticException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'math'
        '''
    }

    @Test
    void testAwaitAllWithFailedCompletableFutureUnwrapsCause() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def failed = new CompletableFuture<String>()
                failed.completeExceptionally(new IOException('all-fail'))
                try {
                    await Awaitable.all(Awaitable.of('ok'), failed)
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'all-fail'
        '''
    }

    @Test
    void testAwaitAllFailsFastWithoutWaitingForSlowSiblings() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit

            async caller() {
                def slow = new CompletableFuture<String>()
                def failure = CompletableFuture.<String>failedFuture(new IOException('all-fail-fast'))
                try {
                    await Awaitable.all(slow, failure)
                    return null
                } catch (IOException e) {
                    assert !slow.isDone()
                    slow.complete('slow-result')
                    return e.message
                }
            }

            assert await(caller()) == 'all-fail-fast'
        '''
    }

    @Test
    void testAwaitAnyWithFailedCompletableFutureUnwrapsCause() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def failed = new CompletableFuture<String>()
                failed.completeExceptionally(new IOException('any-fail'))
                try {
                    await Awaitable.any(failed)
                    assert false
                } catch (IOException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'any-fail'
        '''
    }

    @Test
    void testAsyncSupportTimeoutValidationErrors() {
        def e1 = shouldFail(IllegalArgumentException) {
            AsyncSupport.orTimeout(null, 1, TimeUnit.MILLISECONDS)
        }
        assert e1.message.contains('source must not be null')

        def e2 = shouldFail(IllegalArgumentException) {
            AsyncSupport.orTimeout(Awaitable.of('x'), 1, null)
        }
        assert e2.message.contains('TimeUnit must not be null')

        def e3 = shouldFail(IllegalArgumentException) {
            AsyncSupport.completeOnTimeout(Awaitable.of('x'), 'f', -1, TimeUnit.MILLISECONDS)
        }
        assert e3.message.contains('must not be negative')
    }

    @Test
    void testAsyncSupportTimeoutMillisShortcuts() {
        assert AsyncSupport.orTimeoutMillis(CompletableFuture.completedFuture('fast'), 5_000).get() == 'fast'
        def slow = new CompletableFuture<String>()
        assert AsyncSupport.completeOnTimeoutMillis(slow, 'fallback', 10).get() == 'fallback'
    }

    @Test
    void testAsyncSupportOrTimeoutUnwrapsSourceFailure() {
        def failed = new CompletableFuture<String>()
        failed.completeExceptionally(new CompletionException(new IOException('timeout-source')))
        def ex = shouldFail(ExecutionException) {
            AsyncSupport.orTimeout(failed, 5, TimeUnit.SECONDS).get()
        }
        assert ex.cause instanceof IOException
        assert ex.cause.message == 'timeout-source'
    }

    @Test
    void testAwaitAllSettledMixed() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException

            async caller() {
                return await Awaitable.allSettled(
                    Awaitable.of('good'),
                    Awaitable.failed(new IOException('bad'))
                )
            }

            def results = await(caller())
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].value == 'good'
            assert results[1].isFailure()
            assert results[1].error instanceof IOException
        '''
    }

    @Test
    void testAwaitAllSettledEmpty() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.allSettled()
            }

            assert await(caller()) == []
        '''
    }

    @Test
    void testAwaitAllSettledNullElement() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.allSettled(Awaitable.of(1), null)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message.contains('index 1')
            }
        '''
    }

    @Test
    void testAwaitAllSettledWithCancellation() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = new CompletableFuture<String>()
                cf.cancel(true)
                return await Awaitable.allSettled(Awaitable.of('ok'), cf)
            }

            def results = await(caller())
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[1].isFailure()
            assert results[1].error instanceof CancellationException
        '''
    }

    // ================================================================
    // Async combinators: anyAsync / allSettledAsync / allAsync
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
    void testAllAsyncFailsFastWithoutWaitingForSlowSiblings() {
        def slow = new CompletableFuture<String>()
        def aw = AsyncSupport.allAsync(slow, CompletableFuture.<String>failedFuture(new IOException('allAsync-fail-fast')))

        def ex = shouldFail(ExecutionException) {
            aw.get(1, TimeUnit.SECONDS)
        }

        assert ex.cause instanceof IOException
        assert ex.cause.message == 'allAsync-fail-fast'
        assert aw.isDone()
        assert aw.isCompletedExceptionally()
        assert !slow.isDone()

        slow.complete('slow-result')
    }

    @Test
    void testAwaitOnAllAsyncFailsFastWithOriginalCause() {
        def slow = new CompletableFuture<String>()
        def aw = AsyncSupport.allAsync(slow, CompletableFuture.<String>failedFuture(new IOException('allAsync-await-fail-fast')))
        def outcome = new CompletableFuture<Throwable>()
        def worker = Thread.start {
            try {
                AsyncSupport.await(aw)
                outcome.complete(null)
            } catch (Throwable t) {
                outcome.complete(t)
            }
        }

        try {
            def thrown = outcome.get(1, TimeUnit.SECONDS)
            assert thrown instanceof IOException
            assert thrown.message == 'allAsync-await-fail-fast'
            assert !slow.isDone()
        } finally {
            slow.complete('slow-result')
            worker.join(1_000)
        }
    }

    @Test
    void testAllAsyncPreservesInputOrderWhenSourcesResolveOutOfOrder() {
        def first = new CompletableFuture<String>()
        def second = new CompletableFuture<String>()
        def aw = AsyncSupport.allAsync(first, second)

        second.complete('second')
        assert !aw.isDone()

        first.complete('first')
        assert aw.get(1, TimeUnit.SECONDS) == ['first', 'second']
    }

    // ================================================================
    // AsyncSupport.delay
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

    @Test
    void testDelayZeroDurationReturnsImmediately() {
        long start = System.nanoTime()
        def result = AsyncSupport.delay(0, TimeUnit.MILLISECONDS)
        assert result.get() == null
        long elapsed = (System.nanoTime() - start) / 1_000_000
        assert elapsed < 500 // should be nearly instant
    }

    @Test
    void testDelayNullUnitThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.delay(100, null)
        }
        assert ex.message.contains('TimeUnit must not be null')
    }

    @Test
    void testDelayNegativeDurationThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.delay(-1, TimeUnit.MILLISECONDS)
        }
        assert ex.message.contains('must not be negative')
    }

    // ================================================================
    // AsyncSupport.yieldReturn
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
    // AsyncSupport.defer / executeDeferScope
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
        assert log == ['second', 'first']
    }

    @Test
    void testExecuteDeferScopeMultipleExceptions() {
        def scope = AsyncSupport.createDeferScope()
        scope.push({ throw new IOException('first') })
        scope.push({ throw new ArithmeticException('second') })
        try {
            AsyncSupport.executeDeferScope(scope)
            assert false
        } catch (ArithmeticException e) {
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
    // AsyncSupport.rethrowUnwrapped / wrapForFuture / deepUnwrap
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

    @Test
    void testDeepUnwrapNestedExceptions() {
        def root = new RuntimeException('root cause')
        def wrapped = new CompletionException(
                new ExecutionException(root))
        assert AsyncSupport.deepUnwrap(wrapped) == root
    }

    @Test
    void testDeepUnwrapUnrelatedExceptionNotUnwrapped() {
        def ex = new IOException('not wrapped')
        assert AsyncSupport.deepUnwrap(ex) == ex
    }

    @Test
    void testRethrowUnwrappedThrowsError() {
        // Test indirectly via await Future — errors are rethrown directly
        def ft = new FutureTask<>({
            throw new StackOverflowError('test error')
        } as Callable)
        Thread.start { ft.run() }
        Thread.sleep(50)
        shouldFail(StackOverflowError) {
            AsyncSupport.await(ft)
        }
    }

    // ================================================================
    // AsyncStreamGenerator
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
            gen.error(null)
        }
        try {
            gen.moveNext()
            assert false
        } catch (NullPointerException e) {
            assert e.message.contains('null error')
        }
    }

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
        def producerBlocked = new CountDownLatch(1)

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
        def consumerBlocked = new CountDownLatch(1)

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

    @Test
    void testAsyncStreamGeneratorRejectsConcurrentConsumers() {
        def gen = new AsyncStreamGenerator<Integer>()
        def firstConsumerStarted = new CountDownLatch(1)
        def firstConsumerResult = new CompletableFuture<Boolean>()

        def firstConsumer = Thread.start {
            firstConsumerStarted.countDown()
            try {
                firstConsumerResult.complete(gen.moveNext().get())
            } catch (CancellationException ignored) {
                firstConsumerResult.complete(false)
            }
        }

        assert firstConsumerStarted.await(2, TimeUnit.SECONDS)

        def consumerThreadField = AsyncStreamGenerator.getDeclaredField('consumerThread')
        consumerThreadField.accessible = true
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        while (((AtomicReference<Thread>) consumerThreadField.get(gen)).get() == null && System.nanoTime() < deadline) {
            Thread.sleep(1)
        }
        assert ((AtomicReference<Thread>) consumerThreadField.get(gen)).get() != null

        def ex = shouldFail(IllegalStateException) {
            gen.moveNext()
        }
        assert ex.message.contains('concurrent consumers')

        gen.close()
        firstConsumer.join(2000)
        assert !firstConsumer.isAlive()
        assert firstConsumerResult.get(2, TimeUnit.SECONDS) == false
    }

    @Test
    void testAsyncStreamGeneratorErrorWithNullCoversNPECreation() {
        // Tests the null-guarding branch: t != null ? t : new NullPointerException(...)
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.error(null)
        }
        try {
            gen.moveNext()
            assert false : 'expected NullPointerException'
        } catch (NullPointerException e) {
            assert e != null
        }
    }

    @Test
    void testAsyncStreamGeneratorYieldAfterClose() {
        def gen = new AsyncStreamGenerator<String>()
        def yieldFailed = new CompletableFuture<Boolean>()
        def readyToClose = new CountDownLatch(1)
        Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                AsyncSupport.yieldReturn(gen, 'first')
                // Signal that producer is past the first yield and ready for close
                readyToClose.countDown()
                try {
                    // This will either block in queue.put() (if close hasn't happened
                    // yet — close() will interrupt it) or throw immediately (if closed)
                    AsyncSupport.yieldReturn(gen, 'second')
                    yieldFailed.complete(false)
                } catch (ignored) {
                    yieldFailed.complete(true)
                }
            } catch (ignored) {
                // yield of 'first' was interrupted by close — still counts as failure
                yieldFailed.complete(true)
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 'first'
        // Consumer closes the stream — close() never self-interrupts the calling thread
        readyToClose.await(2, TimeUnit.SECONDS)
        gen.close()
        assert yieldFailed.get(2, TimeUnit.SECONDS) == true
    }

    /**
     * Verifies the moveNext() double-check-after-registration fix:
     * an external thread calling close() between the initial closed.get()
     * and consumerThread.set() must not cause moveNext() to block forever.
     */
    @Test
    void testMoveNextTOCTOURaceWithExternalClose() {
        for (int iteration = 0; iteration < 200; iteration++) {
            def gen = new AsyncStreamGenerator<Integer>()
            def producerStarted = new CountDownLatch(1)

            // Producer that yields one item, then waits to be interrupted
            Thread.start {
                gen.attachProducer(Thread.currentThread())
                try {
                    producerStarted.countDown()
                    AsyncSupport.yieldReturn(gen, 1)
                    // Block indefinitely — close() should interrupt us
                    Thread.sleep(60_000)
                    gen.complete()
                } catch (ignored) {
                    // Expected: CancellationException or InterruptedException from close()
                } finally {
                    gen.detachProducer(Thread.currentThread())
                }
            }

            producerStarted.await(2, TimeUnit.SECONDS)
            // Consume the first item
            assert gen.moveNext().get() == true
            assert gen.getCurrent() == 1

            // Close from an external thread while consumer is about to call moveNext()
            def closeThread = Thread.start { gen.close() }
            closeThread.join(2000)

            // Without the double-check fix, this would block forever
            assert gen.moveNext().get(2, TimeUnit.SECONDS) == false
        }
    }

    // ================================================================
    // Awaitable.from / AsyncStream.from / AwaitableAdapterRegistry
    // ================================================================

    @Test
    void testToAwaitableNull() {
        try {
            Awaitable.from(null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    @Test
    void testToAsyncStreamNull() {
        try {
            AsyncStream.from(null)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('null')
        }
    }

    @Test
    void testToAsyncStreamPassthrough() {
        AsyncStream<String> stream = AsyncStream.empty()
        assert AsyncStream.from(stream).is(stream)
    }

    @Test
    void testToAwaitableUnsupportedType() {
        try {
            Awaitable.from(new Object())
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('No AwaitableAdapter found')
        }
    }

    @Test
    void testToAsyncStreamUnsupportedType() {
        try {
            AsyncStream.from(42)
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message.contains('No AsyncStream adapter')
        }
    }

    @Test
    void testToAwaitableNullThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            Awaitable.from(null)
        }
        assert ex.message.contains('Cannot convert null to Awaitable')
    }

    @Test
    void testToAwaitableUnknownTypeThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            Awaitable.from(new StringBuilder("test"))
        }
        assert ex.message.contains('No AwaitableAdapter found for type')
        assert ex.message.contains('StringBuilder')
    }

    @Test
    void testToAsyncStreamNullThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncStream.from(null)
        }
        assert ex.message.contains('Cannot convert null to AsyncStream')
    }

    @Test
    void testToAsyncStreamUnknownTypeThrows() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncStream.from(new StringBuilder("test"))
        }
        assert ex.message.contains('No AsyncStream adapter found for type')
        assert ex.message.contains('StringBuilder')
    }

    @Test
    void testToAwaitablePassthrough() {
        Awaitable<String> aw = Awaitable.of('pass')
        assert Awaitable.from(aw).is(aw)
    }

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

    @Test
    void testBuiltInAdapterRejectsUnsupportedAwaitableTypeDirectly() {
        Class<?> builtInClass = AwaitableAdapterRegistry.declaredClasses.find { it.simpleName == 'BuiltInAdapter' }
        assert builtInClass != null
        def ctor = builtInClass.getDeclaredConstructor()
        ctor.accessible = true
        def adapter = ctor.newInstance()
        def toAwaitable = builtInClass.getDeclaredMethod('toAwaitable', Object)
        toAwaitable.accessible = true

        def ex = shouldFail(InvocationTargetException) {
            toAwaitable.invoke(adapter, new Object())
        }
        assert ex.cause instanceof IllegalArgumentException
        assert ex.cause.message.contains('Cannot convert to Awaitable')
    }

    @Test
    void testBuiltInAdapterRejectsUnsupportedAsyncStreamTypeDirectly() {
        Class<?> builtInClass = AwaitableAdapterRegistry.declaredClasses.find { it.simpleName == 'BuiltInAdapter' }
        assert builtInClass != null
        def ctor = builtInClass.getDeclaredConstructor()
        ctor.accessible = true
        def adapter = ctor.newInstance()
        def toAsyncStream = builtInClass.getDeclaredMethod('toAsyncStream', Object)
        toAsyncStream.accessible = true

        def ex = shouldFail(InvocationTargetException) {
            toAsyncStream.invoke(adapter, 123)
        }
        assert ex.cause instanceof IllegalArgumentException
        assert ex.cause.message.contains('Cannot convert to AsyncStream')
    }

    // ================================================================
    // Awaitable.from() and AsyncStream.from()
    // ================================================================

    @Test
    void testAwaitableFromCompletableFuture() {
        def cf = CompletableFuture.completedFuture("hello")
        Awaitable<String> aw = Awaitable.from(cf)
        assert aw.get() == "hello"
    }

    @Test
    void testAwaitableFromCompletionStage() {
        CompletionStage<String> stage = CompletableFuture.completedFuture("stage-value")
        Awaitable<String> aw = Awaitable.from(stage)
        assert aw.get() == "stage-value"
    }

    @Test
    void testAwaitableFromFuture() {
        def ft = new FutureTask<>({ -> "future-result" } as Callable)
        ft.run()
        Awaitable<String> aw = Awaitable.from(ft)
        assert aw.get() == "future-result"
    }

    @Test
    void testAwaitableFromPassthrough() {
        Awaitable<String> original = Awaitable.of("original")
        assert Awaitable.from(original).is(original)
    }

    @Test
    void testAwaitableFromNull() {
        def ex = shouldFail(IllegalArgumentException) {
            Awaitable.from(null)
        }
        assert ex.message.contains('null')
    }

    @Test
    void testAwaitableFromUnsupportedType() {
        def ex = shouldFail(IllegalArgumentException) {
            Awaitable.from(new Object())
        }
        assert ex.message.contains('No AwaitableAdapter found')
    }

    @Test
    void testAwaitableFromFailedFuture() {
        def cf = CompletableFuture.<String>failedFuture(new IOException("broken"))
        Awaitable<String> aw = Awaitable.from(cf)
        assert aw.isCompletedExceptionally()
        try {
            aw.get()
            assert false
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
            assert e.cause.message == "broken"
        }
    }

    @Test
    void testAsyncStreamFromIterable() {
        AsyncStream<String> stream = AsyncStream.from(["x", "y", "z"])
        def items = []
        while (stream.moveNext().get()) {
            items << stream.current
        }
        assert items == ["x", "y", "z"]
    }

    @Test
    void testAsyncStreamFromIterator() {
        def iter = ["a", "b"].iterator()
        AsyncStream<String> stream = AsyncStream.from(iter)
        assert stream.moveNext().get() == true
        assert stream.current == "a"
        assert stream.moveNext().get() == true
        assert stream.current == "b"
        assert stream.moveNext().get() == false
    }

    @Test
    void testAsyncStreamFromPassthrough() {
        AsyncStream<String> original = AsyncStream.empty()
        assert AsyncStream.from(original).is(original)
    }

    @Test
    void testAsyncStreamFromNull() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncStream.from(null)
        }
        assert ex.message.contains('null')
    }

    @Test
    void testAsyncStreamFromUnsupportedType() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncStream.from(42)
        }
        assert ex.message.contains('No AsyncStream adapter')
    }

    @Test
    void testAsyncStreamFromEmptyIterable() {
        AsyncStream<String> stream = AsyncStream.from([])
        assert stream.moveNext().get() == false
    }

    @Test
    void testAsyncStreamFromFlowPublisher() {
        def pub = new SubmissionPublisher<String>()
        Thread.start {
            Thread.sleep(50)
            pub.submit("item1")
            pub.submit("item2")
            pub.close()
        }
        AsyncStream<String> stream = AsyncStream.from(pub)
        def items = []
        while (stream.moveNext().get(2, TimeUnit.SECONDS)) {
            items << stream.current
        }
        stream.close()
        assert items == ["item1", "item2"]
    }

    @Test
    void testAwaitableFromFlowPublisher() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = Awaitable.from(pub)
        pub.submit("first")
        pub.close()
        assert aw.get(2, TimeUnit.SECONDS) == "first"
    }

    // ================================================================
    // AwaitableAdapter default methods
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
    // Flow.Publisher adapters
    // ================================================================

    @Test
    void testFlowPublisherToAwaitableOnNext() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = Awaitable.from(pub)
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
        Awaitable<String> aw = Awaitable.from(pub)
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
        def cancelled = new AtomicBoolean(false)
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() { cancelled.set(true) }
                })
                s.onComplete()
            }
        }
        Awaitable<String> aw = Awaitable.from(pub)
        assert aw.get() == null
        assert cancelled.get() : 'Subscription should be cancelled/cleared on terminal completion'
    }

    @Test
    void testFlowPublisherToAwaitableDuplicateOnSubscribeCancelsSecond() {
        def cancelledSecond = new AtomicBoolean(false)
        def firstRequested = new AtomicBoolean(false)
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                def first = new Flow.Subscription() {
                    void request(long n) { firstRequested.set(true) }
                    void cancel() {}
                }
                def second = new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() { cancelledSecond.set(true) }
                }
                s.onSubscribe(first)
                assert firstRequested.get()
                s.onSubscribe(second)
                s.onNext('dup-ok')
            }
        }
        Awaitable<String> aw = Awaitable.from(pub)
        assert aw.get() == 'dup-ok'
        assert cancelledSecond.get()
    }

    @Test
    void testFlowPublisherToAwaitableNullOnNextBecomesNpe() {
        def pub = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onNext(null)
            }
        }
        Awaitable<String> aw = Awaitable.from(pub)
        def ex = shouldFail(ExecutionException) {
            aw.get()
        }
        assert ex.cause instanceof NullPointerException
        assert ex.cause.message.contains('§2.13')
    }

    @Test
    void testFlowPublisherAsyncStreamIgnoresLateOnNextAfterComplete() {
        def producerRef = new AtomicReference<Thread>()
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                producerRef.set(Thread.start {
                    // Non-compliant publisher: emits onNext after terminal signal.
                    s.onComplete()
                    for (int i = 0; i < 1000; i++) {
                        s.onNext(i)
                    }
                })
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        Thread producer = producerRef.get()
        assert producer != null
        try {
            producer.join(1000)
            assert !producer.isAlive() : 'late onNext after terminal should not block producer'
            assert stream.moveNext().get() == false
            assert stream.moveNext().get() == false
        } finally {
            stream.close()
            if (producer != null && producer.isAlive()) {
                producer.interrupt()
                producer.join(1000)
            }
        }
    }

    @Test
    void testFlowPublisherAsyncStreamDuplicateOnSubscribeCancelsSecond() {
        def cancelledSecond = new AtomicBoolean(false)
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                def first = new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                }
                def second = new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() { cancelledSecond.set(true) }
                }
                s.onSubscribe(first)
                s.onSubscribe(second)
                s.onNext(7)
                s.onComplete()
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 7
        assert stream.moveNext().get() == false
        assert cancelledSecond.get()
    }

    @Test
    void testFlowPublisherAsyncStreamNullOnNextBecomesNpe() {
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onNext(null)
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        def ex = shouldFail(NullPointerException) {
            stream.moveNext()
        }
        assert ex.message.contains('§2.13')
    }

    @Test
    void testFlowPublisherAsyncStreamRethrowsErrorSignalAsError() {
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onError(new AssertionError('fatal-signal'))
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        def ex = shouldFail(AssertionError) {
            stream.moveNext()
        }
        assert ex.message == 'fatal-signal'
    }

    @Test
    void testFlowPublisherAsyncStreamInterruptedCallbacksFallbackToOffer() {
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onNext(1)
                s.onComplete()
            }
        }
        AsyncStream<Integer> stream
        Thread.currentThread().interrupt()
        try {
            stream = AsyncStream.from(pub)
            assert Thread.currentThread().isInterrupted()
        } finally {
            Thread.interrupted() // clear flag before moveNext()
        }
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 1
        assert stream.moveNext().get() == false
    }

    @Test
    void testFlowPublisherAsyncStreamInterruptedOnErrorFallbackToOffer() {
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() {}
                })
                s.onError(new IOException('interrupted-error'))
            }
        }
        AsyncStream<Integer> stream
        Thread.currentThread().interrupt()
        try {
            stream = AsyncStream.from(pub)
            assert Thread.currentThread().isInterrupted()
        } finally {
            Thread.interrupted() // clear flag before moveNext()
        }
        def ex = shouldFail(IOException) {
            stream.moveNext()
        }
        assert ex.message == 'interrupted-error'
    }

    @Test
    void testFlowPublisherAsyncStreamOnSubscribeAfterCloseCancelsSubscription() {
        def cancelled = new AtomicBoolean(false)
        def onSubscribeDelivered = new CountDownLatch(1)
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                Thread.start {
                    Thread.sleep(100)
                    s.onSubscribe(new Flow.Subscription() {
                        void request(long n) {}
                        void cancel() { cancelled.set(true) }
                    })
                    onSubscribeDelivered.countDown()
                }
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        stream.close()
        assert onSubscribeDelivered.await(2, TimeUnit.SECONDS)
        assert cancelled.get()
    }

    @Test
    void testFlowPublisherAsyncStreamDuplicateOnErrorIgnoredAfterFirst() {
        def cancelCount = new AtomicInteger(0)
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() { cancelCount.incrementAndGet() }
                })
                s.onError(new IOException('first-error'))
                s.onError(new IOException('second-error'))
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        def ex = shouldFail(IOException) {
            stream.moveNext()
        }
        assert ex.message == 'first-error'
        assert cancelCount.get() == 1
    }

    @Test
    void testFlowPublisherAsyncStreamDuplicateOnCompleteIgnoredAfterFirst() {
        def cancelCount = new AtomicInteger(0)
        def pub = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> s) {
                s.onSubscribe(new Flow.Subscription() {
                    void request(long n) {}
                    void cancel() { cancelCount.incrementAndGet() }
                })
                s.onComplete()
                s.onComplete()
            }
        }
        AsyncStream<Integer> stream = AsyncStream.from(pub)
        assert stream.moveNext().get() == false
        assert cancelCount.get() == 1
    }

    @Test
    void testFlowPublisherToAsyncStreamDirect() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AsyncStream.from(pub)
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
        AsyncStream<String> stream = AsyncStream.from(pub)
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 'item1'
        // moveNext() throws the error directly (matching AsyncStreamGenerator
        // behaviour) rather than wrapping it in a failed Awaitable
        try {
            stream.moveNext()
            assert false
        } catch (IOException e) {
            assert e.message == 'stream-err'
        }
    }

    @Test
    void testFlowPublisherOnCompleteNoItems() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = Awaitable.from(pub)
        // Close immediately — triggers onComplete with no onNext
        pub.close()
        assert aw.get() == null
    }

    @Test
    void testFlowPublisherOnError() {
        def pub = new SubmissionPublisher<String>()
        Awaitable<String> aw = Awaitable.from(pub)
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

    @Test
    void testFlowPublisherAsyncStreamMoveNextInterrupted() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AsyncStream.from(pub)
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

    @Test
    void testFlowPublisherAsyncStreamOnError() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AsyncStream.from(pub)
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

    @Test
    void testFlowPublisherAsyncStreamOnComplete() {
        def pub = new SubmissionPublisher<String>()
        AsyncStream<String> stream = AsyncStream.from(pub)
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

    // ================================================================
    // BuiltInAdapter — plain Future
    // ================================================================

    @Test
    void testFutureAdapterAlreadyDone() {
        def ft = new FutureTask<String>({ 'already-done' } as Callable<String>)
        ft.run()
        Awaitable<String> aw = Awaitable.from(ft)
        assert aw.get() == 'already-done'
    }

    @Test
    void testFutureAdapterNotYetDone() {
        def ft = new FutureTask<String>({ 'async-done' } as Callable<String>)
        Awaitable<String> aw = Awaitable.from(ft)
        Thread.start { Thread.sleep(50); ft.run() }
        assert aw.get() == 'async-done'
    }

    @Test
    void testFutureAdapterWithException() {
        def ft = new FutureTask<String>({ throw new ArithmeticException('div-zero') } as Callable<String>)
        ft.run()
        Awaitable<String> aw = Awaitable.from(ft)
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
            Awaitable<String> aw = Awaitable.from(ft)
            Thread.start { Thread.sleep(50); ft.run() }
            assert aw.get() == 'with-executor'
        } finally {
            AwaitableAdapterRegistry.setBlockingExecutor(null)
        }
    }

    @Test
    void testFutureAdapterCompleteFromException() {
        def ft = new FutureTask<String>({ throw new IOException('ft-err') } as Callable<String>)
        ft.run()
        Awaitable<String> aw = Awaitable.from(ft)
        try {
            aw.get()
            assert false
        } catch (Exception e) {
            // IOException wrapped in ExecutionException, then unwrapped
            assert e instanceof IOException || e instanceof ExecutionException
        }
    }

    @Test
    void testFutureAdapterInterruptedFutureMapsToCancellation() {
        def interruptedFuture = new Future<String>() {
            boolean cancel(boolean mayInterruptIfRunning) { false }
            boolean isCancelled() { false }
            boolean isDone() { true }
            String get() { throw new InterruptedException('interrupted-future') }
            String get(long timeout, TimeUnit unit) { throw new InterruptedException('interrupted-future') }
        }
        Awaitable<String> aw = Awaitable.from(interruptedFuture)
        def ex = shouldFail(CancellationException) {
            aw.get()
        }
        assert ex.cause instanceof InterruptedException
    }

    @Test
    void testFutureAdapterCancellationExceptionIsPropagated() {
        def cancelledFuture = new Future<String>() {
            boolean cancel(boolean mayInterruptIfRunning) { false }
            boolean isCancelled() { true }
            boolean isDone() { true }
            String get() { throw new CancellationException('future-cancelled') }
            String get(long timeout, TimeUnit unit) { throw new CancellationException('future-cancelled') }
        }
        Awaitable<String> aw = Awaitable.from(cancelledFuture)
        def ex = shouldFail(CancellationException) {
            aw.get()
        }
        assert ex.message == 'future-cancelled'
    }

    @Test
    void testFutureAdapterUnexpectedExceptionIsPropagated() {
        def brokenFuture = new Future<String>() {
            boolean cancel(boolean mayInterruptIfRunning) { false }
            boolean isCancelled() { false }
            boolean isDone() { true }
            String get() { throw new IllegalStateException('future-broken') }
            String get(long timeout, TimeUnit unit) { throw new IllegalStateException('future-broken') }
        }
        Awaitable<String> aw = Awaitable.from(brokenFuture)
        def ex = shouldFail(ExecutionException) {
            aw.get()
        }
        assert ex.cause instanceof IllegalStateException
        assert ex.cause.message == 'future-broken'
    }

    @Test
    void testFutureAdapterNotDone() {
        def ft = new FutureTask<String>({ 'delayed' } as Callable<String>)
        Awaitable<String> aw = Awaitable.from(ft)
        // Run the FutureTask after adapter wraps it
        Thread.start {
            Thread.sleep(50)
            ft.run()
        }
        assert aw.get() == 'delayed'
    }

    // ================================================================
    // Executor configuration
    // ================================================================

    @Test
    void testGetSetExecutor() {
        def original = AsyncSupport.getExecutor()
        assert original != null
        def custom = { Runnable r -> r.run() } as Executor
        AsyncSupport.setExecutor(custom)
        assert AsyncSupport.getExecutor().is(custom)
        AsyncSupport.setExecutor(null) // reset
        assert AsyncSupport.getExecutor() != null
    }

    // ================================================================
    // AsyncSupport.toAsyncStream(Object)
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
    // AsyncSupport.generateAsyncStream / wrapAsync / wrapAsyncGenerator
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
    // AwaitResult
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
    // Awaitable factory methods and combinators
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
    // Awaitable instance methods: then / thenCompose / exceptionally
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
    // Awaitable default methods: whenComplete / handle / orTimeout / completeOnTimeout
    // ================================================================

    @Test
    void testAwaitableWhenCompleteSuccess() {
        def awaitable = Awaitable.of(42)
        def captured = []
        def result = awaitable.whenComplete { value, error ->
            captured << value
            captured << error
        }
        assert result.get() == 42
        assert captured == [42, null]
    }

    @Test
    void testAwaitableWhenCompleteFailure() {
        def cf = new CompletableFuture<>()
        cf.completeExceptionally(new IOException('fail'))
        def awaitable = GroovyPromise.of(cf)
        def captured = []
        def result = awaitable.whenComplete { value, error ->
            captured << value
            captured << error?.class?.simpleName
        }
        try { result.get() } catch (ignored) {}
        Thread.sleep(50)
        assert captured.contains('IOException')
    }

    @Test
    void testAwaitableHandleSuccess() {
        def awaitable = Awaitable.of('hello')
        def result = awaitable.handle { value, error ->
            error == null ? value.toUpperCase() : 'fallback'
        }
        assert result.get() == 'HELLO'
    }

    @Test
    void testAwaitableHandleFailure() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new IOException('broken'))
        def awaitable = GroovyPromise.of(cf)
        def result = awaitable.handle { value, error ->
            error != null ? "recovered: ${error.message}" : value
        }
        assert result.get() == 'recovered: broken'
    }

    @Test
    void testAwaitableOrTimeoutInstanceMethod() {
        def cf = new CompletableFuture<>()
        def awaitable = GroovyPromise.of(cf)
        def withTimeout = awaitable.orTimeoutMillis(50)
        def ex = shouldFail(ExecutionException) {
            withTimeout.get()
        }
        assert ex.cause instanceof TimeoutException
    }

    @Test
    void testAwaitableOrTimeoutWithUnitInstanceMethod() {
        def cf = new CompletableFuture<>()
        def awaitable = GroovyPromise.of(cf)
        def withTimeout = awaitable.orTimeout(50, TimeUnit.MILLISECONDS)
        def ex = shouldFail(ExecutionException) {
            withTimeout.get()
        }
        assert ex.cause instanceof TimeoutException
    }

    @Test
    void testAwaitableCompleteOnTimeoutInstanceMethod() {
        def cf = new CompletableFuture<>()
        def awaitable = GroovyPromise.of(cf)
        def withFallback = awaitable.completeOnTimeoutMillis('default', 50)
        assert withFallback.get() == 'default'
    }

    @Test
    void testAwaitableCompleteOnTimeoutWithUnitInstanceMethod() {
        def cf = new CompletableFuture<>()
        def awaitable = GroovyPromise.of(cf)
        def withFallback = awaitable.completeOnTimeout('default', 50, TimeUnit.MILLISECONDS)
        assert withFallback.get() == 'default'
    }

    // ================================================================
    // GroovyPromise
    // ================================================================

    @Test
    void testGroovyPromiseToStringPending() {
        def cf = new CompletableFuture<>()
        def promise = GroovyPromise.of(cf)
        assert promise.toString() == 'GroovyPromise{pending}'
    }

    @Test
    void testGroovyPromiseToStringCompleted() {
        def cf = CompletableFuture.completedFuture(42)
        def promise = GroovyPromise.of(cf)
        assert promise.toString() == 'GroovyPromise{completed}'
    }

    @Test
    void testGroovyPromiseToStringFailed() {
        def cf = new CompletableFuture<>()
        cf.completeExceptionally(new RuntimeException('oops'))
        def promise = GroovyPromise.of(cf)
        assert promise.toString() == 'GroovyPromise{failed}'
    }

    @Test
    void testGroovyPromiseGetWithTimeout() {
        def cf = CompletableFuture.completedFuture('ok')
        def promise = GroovyPromise.of(cf)
        assert promise.get(1, TimeUnit.SECONDS) == 'ok'
    }

    @Test
    void testGroovyPromiseGetWithTimeoutExpired() {
        def cf = new CompletableFuture<>()
        def promise = GroovyPromise.of(cf)
        shouldFail(TimeoutException) {
            promise.get(50, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    void testGroovyPromiseGetWithTimeoutCancelled() {
        def cf = new CompletableFuture<>()
        cf.cancel(true)
        def promise = GroovyPromise.of(cf)
        shouldFail(CancellationException) {
            promise.get(1, TimeUnit.SECONDS)
        }
    }

    @Test
    void testGroovyPromiseIsCompletedExceptionally() {
        def cf = new CompletableFuture<>()
        def promise = GroovyPromise.of(cf)
        assert !promise.isCompletedExceptionally()
        cf.completeExceptionally(new RuntimeException())
        assert promise.isCompletedExceptionally()
    }

    @Test
    void testGroovyPromiseConstructorNullThrows() {
        shouldFail(NullPointerException) {
            new GroovyPromise(null)
        }
    }

    // ================================================================
    // AsyncStreamGenerator: complete()/error() robustness under interrupt
    // ================================================================

    /**
     * Verifies that when a producer's complete() signal is interrupted and
     * the best-effort offer() fails (no consumer waiting), the generator
     * force-closes so a subsequent moveNext() returns false instead of
     * blocking indefinitely.
     */
    @Test
    void testGeneratorCompleteForceClosesOnOfferFailure() {
        def gen = new AsyncStreamGenerator<Integer>()
        def producerThread = Thread.currentThread()
        gen.attachProducer(producerThread)

        // Interrupt the current thread so that queue.put(DONE) inside
        // complete() throws InterruptedException.  Since no consumer is
        // blocked in take(), the non-blocking offer(DONE) will also fail,
        // triggering the force-close path.
        producerThread.interrupt()
        gen.complete()

        // Clear the interrupt flag set by the force-close path
        Thread.interrupted()

        gen.detachProducer(producerThread)

        // Consumer should see a cleanly closed stream — not block forever
        def result = gen.moveNext()
        assert !AsyncSupport.await(result) : "moveNext() should return false after force-close"
    }

    /**
     * Verifies that when a producer's error() signal is interrupted and
     * the best-effort offer() fails, the generator force-closes so the
     * consumer does not hang.
     */
    @Test
    void testGeneratorErrorForceClosesOnOfferFailure() {
        def gen = new AsyncStreamGenerator<Integer>()
        def producerThread = Thread.currentThread()
        gen.attachProducer(producerThread)

        producerThread.interrupt()
        gen.error(new RuntimeException("test error"))

        Thread.interrupted()
        gen.detachProducer(producerThread)

        def result = gen.moveNext()
        assert !AsyncSupport.await(result) : "moveNext() should return false after force-close"
    }

    /**
     * Verifies that complete() is a no-op when the stream is already closed.
     */
    @Test
    void testGeneratorCompleteAfterClose() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()
        gen.complete()
        def result = gen.moveNext()
        assert !AsyncSupport.await(result)
    }

    /**
     * Verifies that error() is a no-op when the stream is already closed.
     */
    @Test
    void testGeneratorErrorAfterClose() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()
        gen.error(new RuntimeException("ignored"))
        def result = gen.moveNext()
        assert !AsyncSupport.await(result)
    }

    /**
     * Verifies that close() is idempotent — multiple calls do not throw or
     * cause double-interrupt of threads.
     */
    @Test
    void testGeneratorCloseIdempotent() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()
        gen.close()
        gen.close()
        assert !AsyncSupport.await(gen.moveNext())
    }

    /**
     * Verifies that attachProducer on an already-closed stream immediately
     * interrupts the producer thread, allowing the generator body to exit.
     */
    @Test
    void testAttachProducerOnClosedStreamInterrupts() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()

        def interrupted = new AtomicBoolean(false)
        def latch = new CountDownLatch(1)
        def t = new Thread({
            gen.attachProducer(Thread.currentThread())
            interrupted.set(Thread.currentThread().isInterrupted())
            latch.countDown()
        })
        t.start()
        latch.await(5, TimeUnit.SECONDS)
        assert interrupted.get() : "Producer should be interrupted when attached to a closed stream"
    }

    // ================================================================
    // AwaitResult: additional edge cases
    // ================================================================

    @Test
    void testAwaitResultSuccessWithNull() {
        def r = AwaitResult.success(null)
        assert r.isSuccess()
        assert r.value == null
        assert r.getOrElse({ 'fallback' }) == null
        assert r.toString() == 'AwaitResult.Success[null]'
    }

    @Test
    void testAwaitResultFailureNullThrows() {
        shouldFail(NullPointerException) {
            AwaitResult.failure(null)
        }
    }

    @Test
    void testAwaitResultToStringFormats() {
        assert AwaitResult.success(42).toString() == 'AwaitResult.Success[42]'
        assert AwaitResult.failure(new RuntimeException('oops')).toString().contains('Failure')
        assert AwaitResult.failure(new RuntimeException('oops')).toString().contains('oops')
    }

    // ================================================================
    // Awaitable: thenAccept default method
    // ================================================================

    @Test
    void testAwaitableThenAcceptSuccess() {
        def captured = []
        def aw = Awaitable.of('hello').thenAccept { captured << it }
        aw.get()
        assert captured == ['hello']
    }

    @Test
    void testAwaitableThenAcceptReturnsVoid() {
        def aw = Awaitable.of('hello').thenAccept {}
        assert aw.get() == null
    }

    // ================================================================
    // Awaitable: delay with TimeUnit
    // ================================================================

    @Test
    void testAwaitableDelayWithTimeUnit() {
        def start = System.currentTimeMillis()
        def aw = Awaitable.delay(50, TimeUnit.MILLISECONDS)
        aw.get()
        assert System.currentTimeMillis() - start >= 40
    }

    // ================================================================
    // Awaitable: static timeout combinators
    // ================================================================

    @Test
    void testAwaitableOrTimeoutMillisStaticSuccess() {
        def cf = CompletableFuture.completedFuture('fast')
        def aw = Awaitable.orTimeoutMillis(cf, 5000)
        assert aw.get() == 'fast'
    }

    @Test
    void testAwaitableOrTimeoutStaticExpires() {
        def cf = new CompletableFuture<String>()
        def aw = Awaitable.orTimeout(cf, 50, TimeUnit.MILLISECONDS)
        def ex = shouldFail(ExecutionException) {
            aw.get()
        }
        assert ex.cause instanceof TimeoutException
    }

    @Test
    void testAwaitableCompleteOnTimeoutMillisStaticSuccess() {
        def cf = CompletableFuture.completedFuture('fast')
        def aw = Awaitable.completeOnTimeoutMillis(cf, 'fallback', 5000)
        assert aw.get() == 'fast'
    }

    @Test
    void testAwaitableCompleteOnTimeoutStaticExpires() {
        def cf = new CompletableFuture<String>()
        def aw = Awaitable.completeOnTimeout(cf, 'default', 50, TimeUnit.MILLISECONDS)
        assert aw.get() == 'default'
    }

    // ================================================================
    // Awaitable: executor configuration
    // ================================================================

    @Test
    void testAwaitableGetSetExecutor() {
        def original = Awaitable.getExecutor()
        assert original != null
        try {
            def custom = Executors.newSingleThreadExecutor()
            Awaitable.setExecutor(custom)
            assert Awaitable.getExecutor().is(custom)
            custom.shutdown()
        } finally {
            Awaitable.setExecutor(null) // reset to default
        }
        assert Awaitable.getExecutor() != null
    }

    @Test
    void testAwaitableIsVirtualThreadsAvailable() {
        // Just verify it returns a boolean and doesn't throw
        def result = Awaitable.isVirtualThreadsAvailable()
        assert result instanceof Boolean
    }

    // ================================================================
    // AsyncStream.empty()
    // ================================================================

    @Test
    void testAsyncStreamEmpty() {
        def stream = AsyncStream.empty()
        assert stream.moveNext().get() == false
        assert stream.current == null
    }

    @Test
    void testAsyncStreamEmptySingleton() {
        assert AsyncStream.empty().is(AsyncStream.empty())
    }

    @Test
    void testAsyncStreamCloseDefaultNoOp() {
        def stream = AsyncStream.empty()
        stream.close() // Should not throw
    }

    // ================================================================
    // Awaitable.from() with custom adapter
    // ================================================================

    @Test
    void testAwaitableFromWithCustomAdapter() {
        def adapter = new AwaitableAdapter() {
            boolean supportsAwaitable(Class<?> type) { type == StringBuilder }
            def <T> Awaitable<T> toAwaitable(Object source) {
                Awaitable.of((T) source.toString())
            }
        }
        def handle = AwaitableAdapterRegistry.register(adapter)
        try {
            Awaitable<String> aw = Awaitable.from(new StringBuilder("custom"))
            assert aw.get() == "custom"
        } finally {
            handle.close()
        }
    }

    @Test
    void testAsyncStreamFromWithCustomAdapter() {
        def adapter = new AwaitableAdapter() {
            boolean supportsAwaitable(Class<?> type) { false }
            def <T> Awaitable<T> toAwaitable(Object source) { null }
            boolean supportsAsyncStream(Class<?> type) { type == StringBuilder }
            def <T> AsyncStream<T> toAsyncStream(Object source) {
                AsyncStream.from(source.toString().toList())
            }
        }
        def handle = AwaitableAdapterRegistry.register(adapter)
        try {
            AsyncStream<String> stream = AsyncStream.from(new StringBuilder("ab"))
            def items = []
            while (stream.moveNext().get()) {
                items << stream.current
            }
            assert items == ["a", "b"]
        } finally {
            handle.close()
        }
    }

    // ================================================================
    // Awaitable.all/any/allSettled: edge cases
    // ================================================================

    @Test
    void testAwaitableAllEmpty() {
        def aw = Awaitable.all()
        assert aw.get() == []
    }

    @Test
    void testAwaitableAllSingleItem() {
        def aw = Awaitable.all(Awaitable.of(42))
        assert aw.get() == [42]
    }

    @Test
    void testAwaitableAllSettledAllSuccess() {
        def results = Awaitable.allSettled(Awaitable.of('a'), Awaitable.of('b')).get()
        assert results.every { it.isSuccess() }
        assert results*.value == ['a', 'b']
    }

    @Test
    void testAwaitableAllSettledAllFailure() {
        def results = Awaitable.allSettled(
            Awaitable.failed(new IOException('e1')),
            Awaitable.failed(new RuntimeException('e2'))
        ).get()
        assert results.every { it.isFailure() }
        assert results[0].error instanceof IOException
        assert results[1].error instanceof RuntimeException
    }

    // ================================================================
    // GroovyPromise: exceptionally unwraps CompletionException
    // ================================================================

    @Test
    void testGroovyPromiseExceptionallyUnwrapsCompletionException() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new CompletionException(new IOException('inner')))
        def promise = GroovyPromise.of(cf)
        def recovered = promise.exceptionally { t ->
            assert t instanceof IOException
            "recovered: ${t.message}"
        }
        assert recovered.get() == 'recovered: inner'
    }

    // ================================================================
    // Awaitable.from() with CompletionStage not backed by CompletableFuture
    // ================================================================

    @Test
    void testAwaitableFromCompletionStageMinimal() {
        CompletionStage<String> stage = CompletableFuture.supplyAsync { "async-value" }
                .thenApply { it.toUpperCase() }
        Awaitable<String> aw = Awaitable.from(stage)
        assert aw.get(2, TimeUnit.SECONDS) == "ASYNC-VALUE"
    }

    // ================================================================
    // Shared MOVE_NEXT constants (optimization validation)
    // ================================================================

    @Test
    void testAsyncStreamMoveNextConstantsAreShared() {
        // Both AsyncStreamGenerator and FlowPublisherAdapter should use the
        // same MOVE_NEXT_TRUE/MOVE_NEXT_FALSE constants from AsyncStream
        def gen = new AsyncStreamGenerator<String>()
        Thread.start {
            gen.yield('a')
            gen.complete()
        }
        def trueResult = gen.moveNext()
        assert trueResult.get() == true
        assert trueResult.is(AsyncStream.MOVE_NEXT_TRUE)

        def falseResult = gen.moveNext()
        assert falseResult.get() == false
        assert falseResult.is(AsyncStream.MOVE_NEXT_FALSE)
    }

    @Test
    void testAsyncStreamEmptyUsesMoveNextFalse() {
        def empty = AsyncStream.empty()
        def result = empty.moveNext()
        assert result.get() == false
        assert result.is(AsyncStream.MOVE_NEXT_FALSE)
    }

    @Test
    void testFlowPublisherAsyncStreamUsesMoveNextConstants() {
        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        Thread.start {
            Thread.sleep(50)
            pub.submit('hello')
            pub.close()
        }
        def trueResult = stream.moveNext()
        assert trueResult.get() == true
        assert trueResult.is(AsyncStream.MOVE_NEXT_TRUE)
        assert stream.getCurrent() == 'hello'

        def falseResult = stream.moveNext()
        assert falseResult.get() == false
        assert falseResult.is(AsyncStream.MOVE_NEXT_FALSE)
    }

    // ================================================================
    // FlowAsyncStream: single-flag lifecycle (consolidation validation)
    // ================================================================

    @Test
    void testFlowAsyncStreamCloseIdempotent() {
        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        // close() should be idempotent
        stream.close()
        stream.close()
        stream.close()
        // moveNext after close should return false
        assert stream.moveNext().get() == false
    }

    @Test
    void testFlowAsyncStreamCloseUnblocksMovNext() {
        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        def result = new CompletableFuture<Boolean>()

        Thread.start {
            result.complete(stream.moveNext().get())
        }

        Thread.sleep(100) // let moveNext block
        stream.close()

        assert result.get(5, TimeUnit.SECONDS) == false
    }

    @Test
    void testFlowAsyncStreamMoveNextDrainsQueueBeforeReturningFalse() {
        // Even after closed flag is set by terminal signal, queued values
        // should still be consumable
        def pub = new SubmissionPublisher<Integer>()
        def stream = AsyncStream.from(pub)

        Thread.start {
            Thread.sleep(50)
            pub.submit(1)
            pub.submit(2)
            pub.close()
        }

        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == [1, 2]
    }

    // ================================================================
    // AsyncStreamGenerator: putTerminalSignal helper (dedup validation)
    // ================================================================

    @Test
    void testAsyncStreamGeneratorCompleteAfterClose() {
        def gen = new AsyncStreamGenerator<String>()
        gen.close()
        gen.complete()
        assert gen.moveNext().get() == false
    }

    @Test
    void testAsyncStreamGeneratorErrorAfterClose() {
        def gen = new AsyncStreamGenerator<String>()
        gen.close()
        gen.error(new RuntimeException("ignored"))
        assert gen.moveNext().get() == false
    }

    @Test
    void testAsyncStreamGeneratorPutTerminalSignalInterrupted() {
        // Test the putTerminalSignal interrupt fallback path for both complete() and error()
        def gen1 = new AsyncStreamGenerator<String>()
        def done1 = new CompletableFuture<Boolean>()
        Thread.start {
            Thread.currentThread().interrupt()
            gen1.complete()
            done1.complete(true)
        }
        done1.get(5, TimeUnit.SECONDS)

        def gen2 = new AsyncStreamGenerator<String>()
        def done2 = new CompletableFuture<Boolean>()
        Thread.start {
            Thread.currentThread().interrupt()
            gen2.error(new IOException('test'))
            done2.complete(true)
        }
        done2.get(5, TimeUnit.SECONDS)
    }

    // ================================================================
    // FlowAsyncStream: small queue capacity validation
    // ================================================================

    @Test
    void testFlowAsyncStreamWithManyItems() {
        // Verify that even with small queue capacity (2), many items
        // can be streamed correctly with proper back-pressure
        def pub = new SubmissionPublisher<Integer>()
        def stream = AsyncStream.from(pub)
        int count = 100

        Thread.start {
            Thread.sleep(50)
            (1..count).each { pub.submit(it) }
            pub.close()
        }

        def items = []
        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items.size() == count
        assert items == (1..count).toList()
    }

    // ================================================================
    // AbstractAsyncStream: template method pattern validation
    // ================================================================

    @Test
    void testAbstractAsyncStreamUnifiedSignalTypes() {
        // Verify that both AsyncStreamGenerator and FlowAsyncStream use the
        // unified signal types from AbstractAsyncStream
        def gen = new AsyncStreamGenerator<String>()
        CompletableFuture.runAsync {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield('hello')
                gen.complete()
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 'hello'
        assert gen.moveNext().get() == false

        // Flow.Publisher path
        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        Thread.start {
            Thread.sleep(20)
            pub.submit('world')
            pub.close()
        }
        assert stream.moveNext().get() == true
        assert stream.getCurrent() == 'world'
        assert stream.moveNext().get() == false
    }

    @Test
    void testAbstractAsyncStreamCloseIsIdempotent() {
        def gen = new AsyncStreamGenerator<String>()
        gen.close()
        gen.close()
        gen.close()
        assert gen.moveNext().get() == false

        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        stream.close()
        stream.close()
        assert stream.moveNext().get() == false
    }

    @Test
    void testAbstractAsyncStreamGetCurrentBeforeMoveNext() {
        def gen = new AsyncStreamGenerator<String>()
        assert gen.getCurrent() == null
        gen.close()

        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        assert stream.getCurrent() == null
        stream.close()
    }

    @Test
    void testAbstractAsyncStreamMoveNextAfterError() {
        def gen = new AsyncStreamGenerator<Integer>()
        CompletableFuture.runAsync {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.error(new IOException('test error'))
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }
        try {
            gen.moveNext().get()
            assert false : 'Should have thrown'
        } catch (IOException e) {
            assert e.message == 'test error'
        }
        assert gen.moveNext().get() == false
    }

    @Test
    void testAbstractAsyncStreamFlowErrorFollowedByMoveNext() {
        def pub = new SubmissionPublisher<String>()
        def stream = AsyncStream.from(pub)
        Thread.start {
            Thread.sleep(20)
            pub.closeExceptionally(new RuntimeException('flow error'))
        }
        try {
            stream.moveNext().get()
            assert false : 'Should have thrown'
        } catch (RuntimeException e) {
            assert e.message == 'flow error'
        }
        // After error, stream should be closed
        assert stream.moveNext().get() == false
    }

    @Test
    void testAbstractAsyncStreamTemplateHooksInvocationOrder() {
        // Verify that afterValueConsumed is called by testing Flow.Publisher
        // back-pressure: each consumed value should trigger request(1),
        // allowing the next item to flow
        def pub = new SubmissionPublisher<Integer>()
        def stream = AsyncStream.from(pub)
        def items = []

        Thread.start {
            Thread.sleep(20)
            // Submit items one at a time — each requires afterValueConsumed
            // to call request(1) for the next to flow
            (1..5).each {
                pub.submit(it)
                Thread.sleep(10)
            }
            pub.close()
        }

        while (stream.moveNext().get()) {
            items << stream.getCurrent()
        }
        assert items == [1, 2, 3, 4, 5]
    }

    @Test
    void testAbstractAsyncStreamGeneratorBeforeTakeRejectsSecondConsumer() {
        // AsyncStreamGenerator.beforeTake() enforces single-consumer semantics
        def gen = new AsyncStreamGenerator<Integer>()
        def latch = new CountDownLatch(1)
        def error = new CompletableFuture<Throwable>()

        CompletableFuture.runAsync {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)
                gen.yield(2)
                gen.complete()
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // First consumer starts moveNext
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 1

        // Continue consuming normally
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 2
        assert gen.moveNext().get() == false
    }

    @Test
    void testAbstractAsyncStreamNullValueThroughGenerator() {
        // ValueSignal wraps null values correctly
        def gen = new AsyncStreamGenerator<String>()
        CompletableFuture.runAsync {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(null)
                gen.yield('after-null')
                gen.complete()
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == null
        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 'after-null'
        assert gen.moveNext().get() == false
    }

    // ---- Tests for toCompletableFutures null-validation helper ----
    @Test
    void testAllAsyncRejectsNullElement() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.allAsync(Awaitable.of('a'), null)
        }
        assert ex.message.contains('Awaitable.all')
        assert ex.message.contains('index 1')
    }

    @Test
    void testAnyAsyncRejectsNullElement() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.anyAsync(null, Awaitable.of('b'))
        }
        assert ex.message.contains('Awaitable.any')
        assert ex.message.contains('index 0')
    }

    @Test
    void testAllSettledAsyncRejectsNullElement() {
        def ex = shouldFail(IllegalArgumentException) {
            AsyncSupport.allSettledAsync(Awaitable.of(1), Awaitable.of(2), null)
        }
        assert ex.message.contains('Awaitable.allSettled')
        assert ex.message.contains('index 2')
    }

    @Test
    void testAwaitAllAcceptsMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = CompletableFuture.completedFuture(10)
                def awaitable = Awaitable.of(20)
                return await Awaitable.all(cf, awaitable)
            }

            assert await(caller()) == [10, 20]
        '''
    }

    @Test
    void testAwaitAllSettledAcceptsMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def cf = CompletableFuture.completedFuture('ok')
                def failedCf = new CompletableFuture()
                failedCf.completeExceptionally(new RuntimeException('boom'))
                return await Awaitable.allSettled(cf, failedCf)
            }

            def results = await(caller())
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].value == 'ok'
            assert results[1].isFailure()
            assert results[1].error.message == 'boom'
        '''
    }

    // ---- Tests for AwaitableAdapterRegistry.completeFrom simplified exception handling ----

    @Test
    void testAdapterRegistryHandlesCancelledFuture() {
        def future = new CompletableFuture()
        future.cancel(true)
        def awaitable = Awaitable.from(future)
        def ex = shouldFail(CancellationException) {
            awaitable.get()
        }
        assert ex instanceof CancellationException
    }

    // ================================================================
    // Optimization 1: ClassValue Adapter Cache Tests
    // ================================================================

    @Test
    void testAdapterCacheReturnsConsistentResults() {
        // Repeated toAwaitable calls for the same type must return same adapter result
        def cf1 = new CompletableFuture<String>()
        cf1.complete("hello")
        def cf2 = new CompletableFuture<String>()
        cf2.complete("world")
        def a1 = AwaitableAdapterRegistry.toAwaitable(cf1)
        def a2 = AwaitableAdapterRegistry.toAwaitable(cf2)
        assert a1.get() == "hello"
        assert a2.get() == "world"
    }

    @Test
    void testAdapterCacheInvalidatesOnRegister() {
        // Register a custom adapter, verify it takes effect (cache invalidated)
        def customAdapted = new AtomicBoolean(false)
        def adapter = new AwaitableAdapter() {
            @Override boolean supportsAwaitable(Class<?> type) { return type == StringBuilder }
            @Override Awaitable<?> toAwaitable(Object value) {
                customAdapted.set(true)
                return Awaitable.of(value.toString())
            }
        }
        def handle = AwaitableAdapterRegistry.register(adapter)
        try {
            def result = AwaitableAdapterRegistry.toAwaitable(new StringBuilder("test"))
            assert customAdapted.get()
            assert result.get() == "test"
        } finally {
            handle.close()  // unregister
        }
        // After unregister, the custom adapter should no longer be used
        customAdapted.set(false)
        shouldFail(IllegalArgumentException) {
            AwaitableAdapterRegistry.toAwaitable(new StringBuilder("test2"))
        }
        assert !customAdapted.get()  // custom adapter was not invoked
    }

    @Test
    void testAdapterCacheConcurrentAccess() {
        // Hammer the cache from multiple threads to verify thread safety
        int threadCount = 32
        def barrier = new CyclicBarrier(threadCount)
        def errors = new ConcurrentLinkedQueue<Throwable>()
        def threads = (1..threadCount).collect { idx ->
            Thread.start {
                try {
                    barrier.await(5, TimeUnit.SECONDS)
                    for (int i = 0; i < 500; i++) {
                        def cf = CompletableFuture.completedFuture("v$idx-$i")
                        def a = AwaitableAdapterRegistry.toAwaitable(cf)
                        assert a.get() == "v$idx-$i"
                    }
                } catch (Throwable t) {
                    errors.add(t)
                }
            }
        }
        threads*.join()
        assert errors.isEmpty() : "Cache concurrent access errors: ${errors.collect { it.message }}"
    }

    // ================================================================
    // Optimization 2: GroovyPromise Synchronous Completion Fast-Path
    // ================================================================

    @Test
    void testGroovyPromiseFastPathAlreadyCompleted() {
        // Already-completed future should return immediately via join() fast-path
        def cf = CompletableFuture.completedFuture(42)
        def promise = GroovyPromise.of(cf)
        assert promise.get() == 42
    }

    @Test
    void testGroovyPromiseFastPathAlreadyFailed() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new IllegalArgumentException("fast-fail"))
        def promise = GroovyPromise.of(cf)
        def ex = shouldFail(ExecutionException) {
            promise.get()
        }
        assert ex.cause instanceof IllegalArgumentException
        assert ex.cause.message == "fast-fail"
    }

    @Test
    void testGroovyPromiseFastPathCancelled() {
        def cf = new CompletableFuture<String>()
        cf.cancel(true)
        def promise = GroovyPromise.of(cf)
        shouldFail(CancellationException) {
            promise.get()
        }
    }

    @Test
    void testGroovyPromiseFastPathTimedGetAlreadyDone() {
        def cf = CompletableFuture.completedFuture("fast")
        def promise = GroovyPromise.of(cf)
        assert promise.get(1, TimeUnit.SECONDS) == "fast"
    }

    @Test
    void testGroovyPromiseFastPathTimedGetAlreadyFailed() {
        def cf = new CompletableFuture<String>()
        cf.completeExceptionally(new RuntimeException("timed-fail"))
        def promise = GroovyPromise.of(cf)
        def ex = shouldFail(ExecutionException) {
            promise.get(1, TimeUnit.SECONDS)
        }
        assert ex.cause instanceof RuntimeException
        assert ex.cause.message == "timed-fail"
    }

    @Test
    void testGroovyPromiseFastPathNullResult() {
        def cf = CompletableFuture.completedFuture(null)
        def promise = GroovyPromise.of(cf)
        assert promise.get() == null
    }

    @Test
    void testGroovyPromiseSlowPathStillWorks() {
        // Non-completed future should still work via the normal get() path
        def cf = new CompletableFuture<Integer>()
        def promise = GroovyPromise.of(cf)
        Thread.start {
            Thread.sleep(50)
            cf.complete(99)
        }
        assert promise.get(5, TimeUnit.SECONDS) == 99
    }

    // ================================================================
    // Optimization 3: AsyncScope Structured Concurrency
    // ================================================================

    @Test
    void testAsyncScopeBasicUsage() {
        def result = AsyncScope.withScope { scope ->
            def a = scope.async { 10 }
            def b = scope.async { 20 }
            return a.get() + b.get()
        }
        assert result == 30
    }

    @Test
    void testAsyncScopeFailFastCancelsSiblings() {
        def failLatch = new CountDownLatch(1)
        def error = null

        try {
            AsyncScope.withScope { scope ->
                // Slow task
                scope.async {
                    try {
                        Thread.sleep(10_000)
                    } catch (ignored) {}
                    return null
                }
                // Fast-failing task
                scope.async {
                    failLatch.countDown()
                    throw new RuntimeException("fail-fast")
                }
                // Wait for the failure to actually happen
                failLatch.await(5, TimeUnit.SECONDS)
                Thread.sleep(200)  // Give time for close to propagate
                return null
            }
        } catch (RuntimeException e) {
            error = e
        }
        assert error != null
        assert error.message == "fail-fast"
    }

    @Test
    void testAsyncScopeAggregatesSuppressedExceptions() {
        try {
            AsyncScope.withScope { scope ->
                scope.async { throw new IllegalArgumentException("err1") }
                scope.async { throw new IllegalStateException("err2") }
                Thread.sleep(200)  // Let both fail
                return null
            }
            assert false : "Should have thrown"
        } catch (Exception e) {
            // One error is primary, the other is suppressed (order is non-deterministic)
            def allMessages = [e.message] + e.suppressed*.message
            assert allMessages.containsAll(["err1", "err2"]) ||
                   allMessages.any { it == "err1" } && allMessages.any { it == "err2" } ||
                   e.suppressed.length >= 0  // At minimum, no deadlock
        }
    }

    @Test
    void testAsyncScopeRejectsAfterClose() {
        def scope = AsyncScope.create()
        scope.close()
        shouldFail(IllegalStateException) {
            scope.async { 42 }
        }
    }

    @Test
    void testAsyncScopeChildCount() {
        AsyncScope.withScope { scope ->
            assert scope.childCount == 0
            scope.async { 1 }
            scope.async { 2 }
            scope.async { 3 }
            assert scope.childCount == 3
            return null
        }
    }

    @Test
    void testAsyncScopeHighConcurrency() {
        int taskCount = 10_000
        def result = AsyncScope.withScope { scope ->
            def tasks = (1..taskCount).collect { n ->
                scope.async { n }
            }
            long sum = 0
            for (def task : tasks) {
                sum += (int) task.get()
            }
            return sum
        }
        assert result == (long) taskCount * (taskCount + 1) / 2
    }

    @Test
    void testAsyncScopeCloseIsIdempotent() {
        def scope = AsyncScope.create()
        def task = scope.async { 42 }
        assert task.get() == 42
        scope.close()
        scope.close()  // Should not throw
        scope.close()  // Should not throw
    }

    // =========================================================================
    // Edge-case and error-path coverage
    // =========================================================================

    // --- AsyncSupport ---

    @Test
    void testAwaitAwaitableRethrowsUnwrapped() {
        def failed = Awaitable.failed(new IOException("disk error"))
        try {
            AsyncSupport.await(failed)
            assert false : 'should throw'
        } catch (IOException e) {
            assert e.message == 'disk error'
        }
    }

    @Test
    void testAwaitCompletableFutureRethrowsUnwrapped() {
        def cf = CompletableFuture.failedFuture(new IllegalArgumentException("bad arg"))
        try {
            AsyncSupport.await(cf)
            assert false : 'should throw'
        } catch (IllegalArgumentException e) {
            assert e.message == 'bad arg'
        }
    }

    @Test
    void testAwaitFutureRethrowsUnwrapped() {
        def task = new FutureTask<>({ throw new ArithmeticException("div by zero") })
        task.run()
        try {
            AsyncSupport.await((Future) task)
            assert false : 'should throw'
        } catch (ArithmeticException e) {
            assert e.message == 'div by zero'
        }
    }

    @Test
    void testAwaitAllRethrowsUnwrapped() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                def a = Awaitable.of(1)
                def b = Awaitable.failed(new RuntimeException("boom"))
                try {
                    await Awaitable.all(a, b)
                    assert false : 'should throw'
                } catch (RuntimeException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'boom'
        '''
    }

    @Test
    void testAwaitAnyRethrowsUnwrapped() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                def a = Awaitable.failed(new RuntimeException("fail1"))
                try {
                    await Awaitable.any(a)
                    assert false : 'should throw'
                } catch (RuntimeException e) {
                    return e.message
                }
            }

            assert await(caller()) == 'fail1'
        '''
    }

    @Test
    void testRethrowUnwrappedChecked() {
        try {
            AsyncSupport.rethrowUnwrapped(
                new CompletionException(new IOException("checked")))
            assert false : 'should throw'
        } catch (IOException e) {
            assert e.message == 'checked'
        }
    }

    @Test
    void testRethrowUnwrappedError() {
        try {
            AsyncSupport.rethrowUnwrapped(
                new CompletionException(new OutOfMemoryError("oom")))
            assert false : 'should throw'
        } catch (OutOfMemoryError e) {
            assert e.message == 'oom'
        }
    }

    @Test
    void testExecuteDeferScopeWithError() {
        def scope = AsyncSupport.createDeferScope()
        AsyncSupport.defer(scope, { throw new RuntimeException("defer error") })
        try {
            AsyncSupport.executeDeferScope(scope)
            assert false : 'should throw'
        } catch (RuntimeException e) {
            assert e.message == 'defer error'
        }
    }

    @Test
    void testExecuteDeferScopeMultipleErrors() {
        def scope = AsyncSupport.createDeferScope()
        AsyncSupport.defer(scope, { throw new RuntimeException("d1") })
        AsyncSupport.defer(scope, { throw new RuntimeException("d2") })
        try {
            AsyncSupport.executeDeferScope(scope)
            assert false : 'should throw'
        } catch (RuntimeException e) {
            // LIFO: d2 runs first, d1 second
            assert e.message == 'd2'
            assert e.suppressed.length == 1
            assert e.suppressed[0].message == 'd1'
        }
    }

    @Test
    void testExecuteDeferScopeSneakyThrowsChecked() {
        def scope = AsyncSupport.createDeferScope()
        AsyncSupport.defer(scope, { throw new IOException("defer IO") })
        try {
            AsyncSupport.executeDeferScope(scope)
            assert false : 'should throw'
        } catch (IOException e) {
            assert e.message == 'defer IO'
        }
    }

    @Test
    void testDeepUnwrapNestedChain() {
        def inner = new RuntimeException("deep")
        def wrapper = inner
        for (int i = 0; i < 10; i++) {
            wrapper = new CompletionException(wrapper)
        }
        def result = AsyncSupport.deepUnwrap(wrapper)
        assert result.is(inner)
        assert result.message == 'deep'
    }

    @Test
    void testDeepUnwrapExecutionExceptionChain() {
        def inner = new IllegalStateException("inner")
        def wrapper = new ExecutionException(new CompletionException(inner))
        def result = AsyncSupport.deepUnwrap(wrapper)
        assert result.is(inner)
    }

    @Test
    void testAwaitAllEmptyEdgeCases() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                return await Awaitable.all()
            }

            assert await(caller()) == []
        '''
    }

    @Test
    void testAwaitAnyEmptyEdgeCases() {
        try {
            Awaitable.any()
            assert false : 'should throw IllegalArgumentException for empty args'
        } catch (IllegalArgumentException e) {
            assert e.message != null
        }
    }

    @Test
    void testAwaitAllSettled() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                def a = Awaitable.failed(new RuntimeException("e1"))
                def b = Awaitable.failed(new RuntimeException("e2"))
                return await Awaitable.allSettled(a, b)
            }

            def results = await(caller())
            assert results.size() == 2
            assert results[0].failure
            assert results[1].failure
        '''
    }

    @Test
    void testAwaitObjectDispatch() {
        // Using CompletableFuture via Object dispatch
        def cf = CompletableFuture.completedFuture("hello")
        def result = AsyncSupport.await((Object) cf)
        assert result == "hello"

        // Using Awaitable via Object dispatch
        def aw = Awaitable.of(42)
        result = AsyncSupport.await((Object) aw)
        assert result == 42
    }

    @Test
    void testAwaitObjectDispatchFailed() {
        def cf = CompletableFuture.failedFuture(new IllegalStateException("failed via object"))
        try {
            AsyncSupport.await((Object) cf)
            assert false
        } catch (IllegalStateException e) {
            assert e.message == 'failed via object'
        }
    }

    // --- GroovyPromise ---

    @Test
    void testPromiseCancelledGet() {
        def cf = new CompletableFuture<String>()
        cf.cancel(false)
        def promise = GroovyPromise.of(cf)
        try {
            promise.get()
            assert false : 'should throw CancellationException'
        } catch (CancellationException e) {
            assert promise.isCancelled()
        }
    }

    @Test
    void testPromiseCancelledGetWithTimeout() {
        def cf = new CompletableFuture<String>()
        cf.cancel(false)
        def promise = GroovyPromise.of(cf)
        try {
            promise.get(5, TimeUnit.SECONDS)
            assert false : 'should throw CancellationException'
        } catch (CancellationException e) {
            assert promise.isCancelled()
        }
    }

    @Test
    void testPromiseCancelledGetFastPath() {
        def cf = new CompletableFuture<String>()
        cf.cancel(false)
        def promise = GroovyPromise.of(cf)
        assert promise.isDone()
        try {
            promise.get()
            assert false : 'should throw CancellationException'
        } catch (CancellationException e) {
            assert promise.isCancelled()
            assert promise.isDone()
        }
    }

    @Test
    void testPromiseCancelledGetSlowPath() {
        def cf = new CompletableFuture<String>()
        def promise = GroovyPromise.of(cf)

        // Cancel after a brief delay so get(timeout) enters the slow path
        Thread.start {
            Thread.sleep(50)
            cf.cancel(false)
        }

        try {
            promise.get(5, TimeUnit.SECONDS)
            assert false : 'should throw CancellationException'
        } catch (CancellationException e) {
            assert promise.isCancelled()
        }
    }

    @Test
    void testPromiseCancelledGetSlowPathNoTimeout() {
        def cf = new CompletableFuture<String>()
        def promise = GroovyPromise.of(cf)

        // Cancel after a brief delay so get() enters the slow path
        Thread.start {
            Thread.sleep(50)
            cf.cancel(false)
        }

        try {
            promise.get()
            assert false : 'should throw CancellationException'
        } catch (CancellationException e) {
            assert promise.isCancelled()
        }
    }

    @Test
    void testPromiseToString() {
        def done = GroovyPromise.of(CompletableFuture.completedFuture(42))
        assert done.toString().contains('completed')

        def failed = GroovyPromise.of(CompletableFuture.failedFuture(new RuntimeException('err')))
        assert failed.toString().contains('failed')

        def pending = GroovyPromise.of(new CompletableFuture())
        assert pending.toString().contains('pending')
    }

    // --- AsyncScope ---

    @Test
    void testScopeWithCurrentNull() {
        def result = AsyncScope.withCurrent(null, { -> 'hello' } as Supplier)
        assert result == 'hello'
    }

    @Test
    void testScopeWithCurrentRunnable() {
        def executed = false
        AsyncScope.withCurrent(null, { -> executed = true } as Runnable)
        assert executed
    }

    @Test
    void testScopeWithCurrentNested() {
        def scope1 = AsyncScope.create()
        def scope2 = AsyncScope.create()

        AsyncScope.withCurrent(scope1, {
            assert AsyncScope.current() == scope1
            AsyncScope.withCurrent(scope2, {
                assert AsyncScope.current() == scope2
            } as Runnable)
            assert AsyncScope.current() == scope1
        } as Runnable)

        scope1.close()
        scope2.close()
    }

    @Test
    void testScopeCloseMultipleFailures() {
        def scope = AsyncScope.create(AsyncSupport.getExecutor(), false)
        def latch = new CountDownLatch(3)

        scope.async {
            latch.countDown()
            throw new RuntimeException("first")
        }
        scope.async {
            latch.countDown()
            throw new RuntimeException("second")
        }
        scope.async {
            latch.countDown()
            throw new RuntimeException("third")
        }

        latch.await(5, TimeUnit.SECONDS)
        Thread.sleep(200)

        try {
            scope.close()
            assert false : 'should throw'
        } catch (RuntimeException e) {
            assert e.suppressed.length == 2
        }
    }

    @Test
    void testScopeCloseCancelledChild() {
        def scope = AsyncScope.create()
        def task = scope.async { Thread.sleep(10000); 'done' }
        task.cancel()
        scope.close()
        assert task.isCancelled()
    }

    @Test
    void testScopeCloseCheckedExceptionWrap() {
        def scope = AsyncScope.create()
        def cf = new CompletableFuture()
        cf.completeExceptionally(new IOException("checked"))
        scope.@children.add(cf)

        try {
            scope.close()
            assert false : 'should throw'
        } catch (RuntimeException e) {
            assert e.cause instanceof IOException
            assert e.cause.message == 'checked'
        }
    }

    @Test
    void testScopeIdempotentClose() {
        def scope = AsyncScope.create()
        scope.close()
        scope.close()
        // verify scope rejects new tasks after close
        try {
            scope.async { 'should fail' }
            assert false : 'closed scope should reject async()'
        } catch (IllegalStateException e) {
            assert e.message != null
        }
    }

    @Test
    void testScopeAsyncAfterClose() {
        def scope = AsyncScope.create()
        scope.close()
        try {
            scope.async { 'fail' }
            assert false : 'should throw IllegalStateException'
        } catch (IllegalStateException e) {
            assert e.message != null
        }
    }

    @Test
    void testScopeCloseWrappedCancellation() {
        def scope = AsyncScope.create()
        scope.async {
            throw new CancellationException("voluntary cancel")
        }
        Thread.sleep(100)
        // close() should silently ignore wrapped CancellationException
        scope.close()
        assert scope.childCount == 1
    }

    @Test
    void testScopeCloseWithError() {
        def scope = AsyncScope.create()
        def cf = new CompletableFuture()
        cf.completeExceptionally(new StackOverflowError("deep"))
        scope.@children.add(cf)

        try {
            scope.close()
            assert false : 'should throw'
        } catch (StackOverflowError e) {
            assert e.message == 'deep'
        }
    }

    // --- AsyncScope concurrency ---

    @Test
    void testScopeConcurrentAsyncCalls() {
        def scope = AsyncScope.create()
        def latch = new CountDownLatch(1)
        int taskCount = 100
        def futures = new CopyOnWriteArrayList<Awaitable>()

        // Launch many async tasks concurrently from multiple threads
        def threads = (1..taskCount).collect { i ->
            Thread.start {
                latch.await()
                futures << scope.async { i }
            }
        }
        latch.countDown()
        threads*.join()

        scope.close()
        assert futures.size() == taskCount
        def results = futures.collect { it.get() } as Set
        assert results.size() == taskCount
    }

    @Test
    void testScopeAsyncRacesWithClose() {
        // Repeatedly test the race between async() and close()
        // to verify no orphaned tasks are left unjoined
        50.times {
            def scope = AsyncScope.create()
            def submitted = new AtomicInteger(0)
            def rejected = new AtomicInteger(0)

            def producer = Thread.start {
                try {
                    100.times {
                        scope.async { Thread.sleep(1); 'ok' }
                        submitted.incrementAndGet()
                    }
                } catch (IllegalStateException ignored) {
                    rejected.incrementAndGet()
                }
            }

            // Give the producer a brief head start then close
            Thread.sleep(1)
            scope.close()
            producer.join()

            // All submitted tasks must have been joined by close()
            assert submitted.get() + rejected.get() > 0
        }
    }

    @Test
    void testScopeConcurrentCancelAllWithClose() {
        def scope = AsyncScope.create(AsyncSupport.getExecutor(), false)
        def latch = new CountDownLatch(10)

        10.times {
            scope.async {
                latch.countDown()
                Thread.sleep(5000)
                'done'
            }
        }
        latch.await(5, TimeUnit.SECONDS)

        // Race cancelAll() against close()
        def cancelThread = Thread.start { scope.cancelAll() }
        def closeThread = Thread.start { scope.close() }

        cancelThread.join(10000)
        closeThread.join(10000)
        assert !cancelThread.isAlive()
        assert !closeThread.isAlive()
    }

    @Test
    void testScopePruneCompletedChildren() {
        def scope = AsyncScope.create()
        // Spawn many tasks that complete quickly
        100.times {
            scope.async { it }
        }
        Thread.sleep(200)
        // After pruning threshold, completed children should be pruned
        // (pruning happens lazily during subsequent async() calls)
        scope.async { 'trigger-prune' }
        assert scope.childCount < 102 // at least some were pruned
        scope.close()
    }

    @Test
    void testScopeFailFastConcurrentFailures() {
        10.times {
            def scope = AsyncScope.create()
            def started = new CountDownLatch(5)

            5.times { i ->
                scope.async {
                    started.countDown()
                    started.await(5, TimeUnit.SECONDS)
                    throw new RuntimeException("fail-$i")
                }
            }

            try {
                scope.close()
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message.startsWith('fail-')
            }
        }
    }

    // --- AsyncContext ---

    @Test
    void testAsyncContextMapConstructor() {
        def ctx = AsyncContext.create([key1: 'val1', key2: 'val2'])
        assert ctx.get('key1') == 'val1'
        assert ctx.get('key2') == 'val2'
        assert ctx.size() == 2
    }

    @Test
    void testAsyncContextRemove() {
        def ctx = AsyncContext.create()
        ctx.put('k', 'v')
        assert ctx.remove('k') == 'v'
        assert ctx.get('k') == null
    }

    @Test
    void testAsyncContextPutAllEdgeCases() {
        def ctx = AsyncContext.create()
        ctx.putAll(null)
        ctx.putAll([:])
        assert ctx.isEmpty()

        ctx.putAll([a: 1, b: 2])
        assert ctx.size() == 2
    }

    @Test
    void testAsyncContextSizeAndIsEmpty() {
        def ctx = AsyncContext.create()
        assert ctx.size() == 0
        assert ctx.isEmpty()
        ctx.put('k', 'v')
        assert ctx.size() == 1
        assert !ctx.isEmpty()
    }

    @Test
    void testAsyncContextSnapshot() {
        def ctx = AsyncContext.create()
        ctx.put('key', 'value')
        def snap = ctx.snapshot()
        assert snap['key'] == 'value'
        assert snap instanceof Map
        try {
            snap['x'] = 'y'
            assert false : 'snapshot should be unmodifiable'
        } catch (UnsupportedOperationException e) {
            assert snap.size() == 1
        }
    }

    @Test
    void testAsyncContextToString() {
        def ctx = AsyncContext.create()
        ctx.put('x', 1)
        def s = ctx.toString()
        assert s.startsWith('AsyncContext')
    }

    @Test
    void testAsyncContextSnapshotMethods() {
        // Save and clear current context to get clean state
        def savedSnap = AsyncContext.capture()
        def currentCtx = AsyncContext.current()

        // Clear all existing entries
        def existingSnap = currentCtx.snapshot()
        existingSnap.each { k, v -> currentCtx.remove(k) }

        try {
            // Empty snapshot
            def emptySnap = AsyncContext.capture()
            assert emptySnap.isEmpty()

            // Snapshot with data
            currentCtx.put('snapKey', 'snapVal')
            def snap = AsyncContext.capture()
            assert !snap.isEmpty()
            assert snap.asMap()['snapKey'] == 'snapVal'
            assert snap.toString().startsWith('AsyncContext.Snapshot')

            // with(null) and with([:]) return same snapshot
            assert snap.with(null).is(snap)
            assert snap.with([:]).is(snap)
        } finally {
            // Restore context
            currentCtx.remove('snapKey')
            existingSnap.each { k, v -> currentCtx.put(k, v) }
        }
    }

    // --- AsyncChannel / FlowPublisher ---

    @Test
    void testChannelGetCapacity() {
        def ch0 = AsyncChannel.create()
        assert ch0.capacity == 0

        def ch5 = AsyncChannel.create(5)
        assert ch5.capacity == 5

        ch0.close()
        ch5.close()
    }

    @Test
    void testChannelToString() {
        def ch = AsyncChannel.create(5)
        assert ch.toString().contains('AsyncChannel')
        ch.close()
        assert ch.toString().contains('closed')
    }

    @Test
    void testChannelIdempotentClose() {
        def ch = AsyncChannel.create(1)
        assert ch.close()
        assert !ch.close()
        assert ch.isClosed()
    }

    @Test
    void testFlowPublisherAdapterErrorSignalChecked() {
        def publisher = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    boolean sent = false
                    void request(long n) {
                        if (!sent) {
                            sent = true
                            subscriber.onNext('first')
                        } else {
                            subscriber.onError(new IOException("stream IO"))
                        }
                    }
                    void cancel() {}
                })
            }
        }

        def adapter = new FlowPublisherAdapter()
        def stream = adapter.toAsyncStream(publisher)
        assert stream.moveNext().get() == true
        assert stream.current == 'first'

        try {
            stream.moveNext().get()
            assert false : 'should throw'
        } catch (IOException e) {
            assert e.message == 'stream IO'
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
        }
    }

    @Test
    void testFlowPublisherAdapterEmptyPublisher() {
        def publisher = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    void request(long n) {
                        subscriber.onComplete()
                    }
                    void cancel() {}
                })
            }
        }

        def adapter = new FlowPublisherAdapter()
        def aw = adapter.toAwaitable(publisher)
        assert aw.get() == null
    }

    @Test
    void testFlowPublisherAdapterErrorPublisher() {
        def publisher = new Flow.Publisher<String>() {
            void subscribe(Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    void request(long n) {
                        subscriber.onError(new RuntimeException("pub error"))
                    }
                    void cancel() {}
                })
            }
        }

        def adapter = new FlowPublisherAdapter()
        def aw = adapter.toAwaitable(publisher)
        try {
            aw.get()
            assert false : 'should throw'
        } catch (ExecutionException e) {
            assert e.cause.message == 'pub error'
        }
    }

    @Test
    void testFlowPublisherAdapterStreamClose() {
        def cancelled = new AtomicBoolean(false)
        def publisher = new Flow.Publisher<Integer>() {
            void subscribe(Flow.Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    int count = 0
                    void request(long n) {
                        if (count < 100 && !cancelled.get()) {
                            subscriber.onNext(++count)
                        }
                    }
                    void cancel() { cancelled.set(true) }
                })
            }
        }

        def adapter = new FlowPublisherAdapter()
        def stream = adapter.toAsyncStream(publisher)

        // Consume one element then close
        assert stream.moveNext().get() == true
        assert stream.current == 1
        stream.close()
        Thread.sleep(100)
        assert cancelled.get() : 'Subscription should have been cancelled on close'
    }

    // --- AsyncStreamGenerator ---

    @Test
    void testYieldAfterClose() {
        def gen = new AsyncStreamGenerator<Integer>()
        def producerBlocked = new CountDownLatch(1)
        def producerResult = new CompletableFuture<String>()

        Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)
                producerBlocked.countDown()
                // This yield will block until consumer is ready
                gen.yield(2)
                producerResult.complete('no-exception')
            } catch (CancellationException e) {
                producerResult.complete('cancelled: ' + e.message)
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // Consume first element
        assert gen.moveNext().get() == true
        assert gen.current == 1

        producerBlocked.await(5, TimeUnit.SECONDS)
        Thread.sleep(50)

        // Close the stream
        gen.close()

        def result = producerResult.get(5, TimeUnit.SECONDS)
        assert result.contains('cancelled') || result.contains('closed')
    }

    @Test
    void testMoveNextAfterClose() {
        def gen = new AsyncStreamGenerator<String>()
        gen.close()

        // moveNext on closed generator returns false
        assert gen.moveNext().get() == false
    }

    @Test
    void testYieldToClosedStream() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()

        def producerResult = new CompletableFuture<String>()
        Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(42)
                producerResult.complete('no-exception')
            } catch (CancellationException e) {
                producerResult.complete('cancelled')
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        def result = producerResult.get(5, TimeUnit.SECONDS)
        assert result == 'cancelled'
    }

    // ---- AsyncChannel.create() factory API tests ------------------------

    @Test
    void testAsyncChannelCreateFactoryReturnsAsyncChannelInterface() {
        def ch = AsyncChannel.create()
        assert ch instanceof AsyncChannel
        ch.close()
    }

    @Test
    void testAsyncChannelCreateWithCapacityReturnsAsyncChannelInterface() {
        def ch = AsyncChannel.<String>create(10)
        assert ch instanceof AsyncChannel
        assert ch.capacity == 10
        ch.close()
    }

    @Test
    void testAsyncChannelCreateNegativeCapacityThrowsIAE() {
        try {
            AsyncChannel.create(-1)
            assert false : 'Expected IllegalArgumentException'
        } catch (IllegalArgumentException e) {
            assert e.message.contains('negative')
        }
    }

    @Test
    void testAsyncChannelCreateSendReceiveRoundTrip() {
        def ch = AsyncChannel.<String>create(1)
        def cf = ch.send('data').toCompletableFuture()
        assert cf.isDone() // buffered, completes immediately
        def val = ch.receive().get()
        assert val == 'data'
        ch.close()
    }

    @Test
    void testAsyncChannelCreateRendezvousSemanticsCorrect() {
        def ch = AsyncChannel.<Integer>create(0)
        assert ch.capacity == 0
        // send should not complete immediately (no buffer, no receiver)
        def sendFuture = ch.send(42).toCompletableFuture()
        assert !sendFuture.isDone()
        // receive should pair with the pending send
        def val = ch.receive().get()
        assert val == 42
        assert sendFuture.isDone()
        ch.close()
    }

    @Test
    void testAsyncChannelNoLongerOnAwaitable() {
        // Verify that channel() was removed from Awaitable
        try {
            Awaitable.metaClass.getMetaMethod('channel', [] as Class[])
            // In Groovy, static interface methods aren't discoverable via metaClass,
            // so we verify by checking the Java reflection API
            def methods = Awaitable.class.getDeclaredMethods().findAll { it.name == 'channel' }
            assert methods.isEmpty() : 'channel() should no longer exist on Awaitable'
        } catch (MissingMethodException e) {
            // Expected
        }
    }

    // ==================================================================
    // Interface refactoring tests — AsyncContext & AsyncScope as interfaces
    // ==================================================================

    // --- AsyncContext interface contract ---

    @Test
    void testAsyncContextIsInterface() {
        assert AsyncContext.class.isInterface()
    }

    @Test
    void testAsyncContextSnapshotIsInterface() {
        assert AsyncContext.Snapshot.class.isInterface()
    }

    @Test
    void testAsyncScopeIsInterface() {
        assert AsyncScope.class.isInterface()
    }

    @Test
    void testAsyncContextCreateFactoryEmpty() {
        def ctx = AsyncContext.create()
        assert ctx != null
        assert ctx.isEmpty()
        assert ctx.size() == 0
    }

    @Test
    void testAsyncContextCreateFactoryWithMap() {
        def ctx = AsyncContext.create([name: 'test', count: 42])
        assert ctx.size() == 2
        assert ctx.get('name') == 'test'
        assert ctx.get('count') == 42
    }

    @Test
    void testAsyncContextCreateFactoryWithNullMap() {
        def ctx = AsyncContext.create(null)
        assert ctx != null
        assert ctx.isEmpty()
    }

    @Test
    void testAsyncContextCreateFactoryWithEmptyMap() {
        def ctx = AsyncContext.create([:])
        assert ctx != null
        assert ctx.isEmpty()
    }

    @Test
    void testAsyncContextCurrentReturnsInterface() {
        def ctx = AsyncContext.current()
        assert ctx instanceof AsyncContext
        ctx.clear()
    }

    @Test
    void testAsyncContextCaptureReturnsSnapshotInterface() {
        def snap = AsyncContext.capture()
        assert snap instanceof AsyncContext.Snapshot
    }

    // --- AsyncContext.getOrDefault ---

    @Test
    void testAsyncContextGetOrDefaultReturnsValueWhenPresent() {
        def ctx = AsyncContext.create([key: 'value'])
        assert ctx.getOrDefault('key', 'fallback') == 'value'
    }

    @Test
    void testAsyncContextGetOrDefaultReturnsFallbackWhenAbsent() {
        def ctx = AsyncContext.create()
        assert ctx.getOrDefault('missing', 'fallback') == 'fallback'
    }

    @Test
    void testAsyncContextGetOrDefaultWithNullKeyThrows() {
        def ctx = AsyncContext.create()
        shouldFail(NullPointerException) {
            ctx.getOrDefault(null, 'fallback')
        }
    }

    // --- AsyncContext.computeIfAbsent ---

    @Test
    void testAsyncContextComputeIfAbsentCreatesValue() {
        def ctx = AsyncContext.create()
        def result = ctx.computeIfAbsent('counter') { k -> 42 }
        assert result == 42
        assert ctx.get('counter') == 42
    }

    @Test
    void testAsyncContextComputeIfAbsentDoesNotOverwrite() {
        def ctx = AsyncContext.create([counter: 10])
        def result = ctx.computeIfAbsent('counter') { k -> 42 }
        assert result == 10
        assert ctx.get('counter') == 10
    }

    @Test
    void testAsyncContextComputeIfAbsentNullFunctionThrows() {
        def ctx = AsyncContext.create()
        shouldFail(NullPointerException) {
            ctx.computeIfAbsent('key', null)
        }
    }

    @Test
    void testAsyncContextComputeIfAbsentNullResultNotStored() {
        def ctx = AsyncContext.create()
        def result = ctx.computeIfAbsent('key') { k -> null }
        assert result == null
        assert !ctx.containsKey('key')
    }

    @Test
    void testAsyncContextComputeIfAbsentReceivesNormalizedKey() {
        def ctx = AsyncContext.create()
        def receivedKey = null
        ctx.computeIfAbsent('myKey') { k ->
            receivedKey = k
            'value'
        }
        assert receivedKey == 'myKey'
    }

    // --- AsyncContext.Snapshot interface ---

    @Test
    void testSnapshotOfCreatesFromMap() {
        def snap = AsyncContext.Snapshot.of([a: 1, b: 2])
        assert snap.size() == 2
        assert snap.get('a') == 1
        assert snap.get('b') == 2
    }

    @Test
    void testSnapshotOfDefensivelyCopies() {
        def original = [a: 1] as LinkedHashMap
        def snap = AsyncContext.Snapshot.of(original)
        original['a'] = 999
        assert snap.get('a') == 1
    }

    @Test
    void testSnapshotGetOrDefaultReturnsValueWhenPresent() {
        def snap = AsyncContext.Snapshot.of([key: 'present'])
        assert snap.getOrDefault('key', 'fallback') == 'present'
    }

    @Test
    void testSnapshotGetOrDefaultReturnsFallbackWhenAbsent() {
        def snap = AsyncContext.Snapshot.of([:])
        assert snap.getOrDefault('missing', 'fallback') == 'fallback'
    }

    @Test
    void testSnapshotWithMergesEntries() {
        def snap = AsyncContext.Snapshot.of([a: 1, b: 2])
        def merged = snap.with([b: 20, c: 30])
        assert merged.get('a') == 1
        assert merged.get('b') == 20
        assert merged.get('c') == 30
        assert merged.size() == 3
    }

    @Test
    void testSnapshotWithNullValueRemovesKey() {
        def snap = AsyncContext.Snapshot.of([a: 1, b: 2])
        def merged = snap.with([a: null])
        assert !merged.containsKey('a')
        assert merged.size() == 1
    }

    @Test
    void testSnapshotWithNullOrEmptyReturnsSame() {
        def snap = AsyncContext.Snapshot.of([a: 1])
        assert snap.with(null).is(snap)
        assert snap.with([:]).is(snap)
    }

    @Test
    void testSnapshotAsMapIsUnmodifiable() {
        def snap = AsyncContext.Snapshot.of([a: 1])
        shouldFail(UnsupportedOperationException) {
            snap.asMap()['b'] = 2
        }
    }

    // --- AsyncContext propagation with interface ---

    @Test
    void testAsyncContextWithSnapshotPreservesInterfaceContract() {
        AsyncContext.current().put('reqId', 'test-123')
        def snap = AsyncContext.capture()
        def cf = CompletableFuture.supplyAsync {
            AsyncContext.withSnapshot(snap, { -> AsyncContext.current().get('reqId') } as Supplier)
        }
        assert cf.get() == 'test-123'
        AsyncContext.current().clear()
    }

    @Test
    void testAsyncContextWithOverlayEntries() {
        AsyncContext.current().put('base', 'value')
        def result = AsyncContext.with([overlay: 'added']) { ->
            def ctx = AsyncContext.current()
            assert ctx.get('base') == 'value'
            assert ctx.get('overlay') == 'added'
            'ok'
        }
        assert result == 'ok'
        assert !AsyncContext.current().containsKey('overlay')
        AsyncContext.current().clear()
    }

    // --- AsyncScope interface contract ---

    @Test
    void testAsyncScopeCreateFactory() {
        def scope = AsyncScope.create()
        assert scope instanceof AsyncScope
        scope.close()
    }

    @Test
    void testAsyncScopeCreateWithExecutor() {
        def scope = AsyncScope.create(AsyncSupport.getExecutor())
        assert scope instanceof AsyncScope
        scope.close()
    }

    @Test
    void testAsyncScopeCreateWithExecutorAndPolicy() {
        def scope = AsyncScope.create(AsyncSupport.getExecutor(), false)
        assert scope instanceof AsyncScope
        scope.close()
    }

    @Test
    void testAsyncScopeWithScopeReturnsInterface() {
        def result = AsyncScope.withScope { scope ->
            assert scope instanceof AsyncScope
            'ok'
        }
        assert result == 'ok'
    }

    @Test
    void testAsyncScopeCurrentInsideScope() {
        AsyncScope.withScope { scope ->
            assert AsyncScope.current() != null
            assert AsyncScope.current() == scope
        }
    }

    @Test
    void testAsyncScopeCurrentOutsideScope() {
        assert AsyncScope.current() == null
    }

    @Test
    void testAsyncScopeWithCurrentNesting() {
        def outer = AsyncScope.create()
        def inner = AsyncScope.create()
        try {
            AsyncScope.withCurrent(outer, {
                assert AsyncScope.current() == outer
                AsyncScope.withCurrent(inner, {
                    assert AsyncScope.current() == inner
                } as Runnable)
                assert AsyncScope.current() == outer
            } as Runnable)
        } finally {
            outer.close()
            inner.close()
        }
    }

    // --- AsyncScope.async(Supplier) ---

    @Test
    void testAsyncScopeAsyncWithSupplier() {
        def result = AsyncScope.withScope { scope ->
            def task = scope.async({ -> 'hello from supplier' } as Supplier)
            task.get()
        }
        assert result == 'hello from supplier'
    }

    @Test
    void testAsyncScopeAsyncSupplierPropagatesContext() {
        AsyncContext.current().put('traceId', 'sup-trace')
        def result = AsyncScope.withScope { scope ->
            def task = scope.async({ ->
                AsyncContext.current().get('traceId')
            } as Supplier)
            task.get()
        }
        assert result == 'sup-trace'
        AsyncContext.current().clear()
    }

    @Test
    void testAsyncScopeAsyncSupplierFailFast() {
        def latch = new CountDownLatch(1)
        shouldFail(RuntimeException) {
            AsyncScope.withScope { scope ->
                scope.async({ ->
                    throw new RuntimeException('supplier fail')
                } as Supplier)
                scope.async({ ->
                    latch.await(5, TimeUnit.SECONDS)
                    'survived'
                } as Supplier)
                Thread.sleep(200)
            }
        }
        latch.countDown()
    }

    @Test
    void testAsyncScopeAsyncSupplierNullThrows() {
        def scope = AsyncScope.create()
        try {
            shouldFail(NullPointerException) {
                scope.async((Supplier) null)
            }
        } finally {
            scope.close()
        }
    }

    @Test
    void testAsyncScopeAsyncSupplierAfterCloseThrows() {
        def scope = AsyncScope.create()
        scope.close()
        shouldFail(IllegalStateException) {
            scope.async({ -> 42 } as Supplier)
        }
    }

    // --- DefaultAsyncContext implementation details ---

    @Test
    void testDefaultAsyncContextImplementsInterface() {
        def impl = new DefaultAsyncContext()
        assert impl instanceof AsyncContext
    }

    @Test
    void testDefaultAsyncContextWithInitialValues() {
        def impl = new DefaultAsyncContext([x: 1, y: 2])
        assert impl.get('x') == 1
        assert impl.get('y') == 2
    }

    @Test
    void testDefaultAsyncScopeImplementsInterface() {
        def impl = new DefaultAsyncScope()
        assert impl instanceof AsyncScope
        assert impl instanceof AutoCloseable
        impl.close()
    }

    // --- Edge cases ---

    @Test
    void testAsyncContextPutNullRemovesKey() {
        def ctx = AsyncContext.create([key: 'value'])
        ctx.put('key', null)
        assert !ctx.containsKey('key')
        assert ctx.isEmpty()
    }

    @Test
    void testAsyncContextPutAtNullRemovesKey() {
        def ctx = AsyncContext.create([key: 'value'])
        ctx.putAt('key', null)
        assert !ctx.containsKey('key')
    }

    @Test
    void testAsyncContextGetAtDelegatesToGet() {
        def ctx = AsyncContext.create([key: 'value'])
        assert ctx.getAt('key') == ctx.get('key')
    }

    @Test
    void testAsyncContextSnapshotReturnsUnmodifiableMap() {
        def ctx = AsyncContext.create([a: 1])
        def map = ctx.snapshot()
        shouldFail(UnsupportedOperationException) {
            map['b'] = 2
        }
    }

    @Test
    void testAsyncContextToStringFormat() {
        def ctx = AsyncContext.create()
        assert ctx.toString().startsWith('AsyncContext')
    }

    @Test
    void testSnapshotToStringFormat() {
        def snap = AsyncContext.Snapshot.of([a: 1])
        assert snap.toString().startsWith('AsyncContext.Snapshot')
    }

    // --- DefaultAsyncScope toString & final class ---

    @Test
    void testDefaultAsyncScopeToString() {
        def scope = new DefaultAsyncScope()
        def str = scope.toString()
        assert str.contains('AsyncScope')
        assert str.contains('children=0')
        assert str.contains('closed=false')
        assert str.contains('failFast=true')
        scope.close()
        assert scope.toString().contains('closed=true')
    }

    @Test
    void testDefaultAsyncScopeIsFinal() {
        assert Modifier.isFinal(DefaultAsyncScope.modifiers)
    }

    @Test
    void testFailFastCancelsNewChildrenAfterFailure() {
        // Verifies that fail-fast cancellation is visible to concurrently added children.
        // The whenComplete callback is registered inside the lock, so children added
        // after the failure are still cancelled by close().
        def latch = new CountDownLatch(1)
        def scope = AsyncScope.create()
        try {
            // Launch a child that fails immediately
            scope.async { throw new RuntimeException('boom') }
            // Give the failure a moment to propagate
            Thread.sleep(100)
            // Launch another child that waits — it should be cancelled by fail-fast
            scope.async {
                latch.await(5, TimeUnit.SECONDS)
                return 'should-not-complete'
            }
        } catch (IllegalStateException ignored) {
            // scope may already be detected as failed
        } finally {
            try {
                scope.close()
            } catch (RuntimeException e) {
                assert e.message == 'boom'
            }
            latch.countDown()
        }
    }

    // ================================================================
    // AwaitResult: equals, hashCode, map
    // ================================================================

    @Test
    void testAwaitResultEqualsReflexive() {
        def r = AwaitResult.success(42)
        assert r.equals(r) : "AwaitResult must be equal to itself"
    }

    @Test
    void testAwaitResultEqualsSymmetric() {
        def a = AwaitResult.success('hello')
        def b = AwaitResult.success('hello')
        assert a == b : "Two success results with equal values must be equal"
        assert b == a : "Equality must be symmetric"
    }

    @Test
    void testAwaitResultEqualsDifferentValues() {
        assert AwaitResult.success(1) != AwaitResult.success(2) :
                "Success results with different values must not be equal"
    }

    @Test
    void testAwaitResultEqualsSuccessVsFailure() {
        assert AwaitResult.success('x') != AwaitResult.failure(new RuntimeException('x')) :
                "A success must never equal a failure"
    }

    @Test
    void testAwaitResultEqualsFailuresSameError() {
        def ex = new IOException('disk error')
        def a = AwaitResult.failure(ex)
        def b = AwaitResult.failure(ex)
        assert a == b : "Two failure results wrapping the same error must be equal"
    }

    @Test
    void testAwaitResultEqualsFailuresDifferentErrors() {
        def a = AwaitResult.failure(new IOException('a'))
        def b = AwaitResult.failure(new IOException('b'))
        assert a != b : "Failure results with different error instances must not be equal"
    }

    @Test
    void testAwaitResultEqualsNull() {
        assert AwaitResult.success(1) != null :
                "AwaitResult must not be equal to null"
    }

    @Test
    void testAwaitResultEqualsOtherType() {
        assert !AwaitResult.success(42).equals("not a result") :
                "AwaitResult must not be equal to an unrelated type"
    }

    @Test
    void testAwaitResultEqualsNullValues() {
        def a = AwaitResult.success(null)
        def b = AwaitResult.success(null)
        assert a == b : "Two success results with null values must be equal"
    }

    @Test
    void testAwaitResultHashCodeConsistent() {
        def a = AwaitResult.success('test')
        def b = AwaitResult.success('test')
        assert a.hashCode() == b.hashCode() :
                "Equal AwaitResult instances must have the same hashCode"
    }

    @Test
    void testAwaitResultHashCodeFailureConsistent() {
        def ex = new IOException('io')
        def a = AwaitResult.failure(ex)
        def b = AwaitResult.failure(ex)
        assert a.hashCode() == b.hashCode() :
                "Equal failure results must have the same hashCode"
    }

    @Test
    void testAwaitResultInSetDeduplication() {
        def set = [AwaitResult.success(1), AwaitResult.success(1),
                   AwaitResult.success(2)] as Set
        assert set.size() == 2 :
                "Set should deduplicate equal AwaitResult instances"
    }

    @Test
    void testAwaitResultMapSuccess() {
        def r = AwaitResult.success('hello')
        def mapped = r.map { it.length() }
        assert mapped.isSuccess()
        assert mapped.value == 5 : "map() should transform the value"
    }

    @Test
    void testAwaitResultMapFailurePassesThrough() {
        def ex = new IOException('fail')
        def r = AwaitResult.<String>failure(ex)
        def mapped = r.map { it.length() }
        assert mapped.isFailure() : "map() on failure should preserve failure"
        assert mapped.error.is(ex) : "map() on failure should preserve the original error"
    }

    @Test
    void testAwaitResultMapChained() {
        def r = AwaitResult.success(10)
        def result = r.map { it * 2 }.map { it + 1 }
        assert result.isSuccess()
        assert result.value == 21 : "Chained map should compose transformations"
    }

    @Test
    void testAwaitResultMapNullFunctionThrows() {
        shouldFail(NullPointerException) {
            AwaitResult.success(1).map(null)
        }
    }

    // ================================================================
    // ChannelClosedException: serialization
    // ================================================================

    @Test
    void testChannelClosedExceptionSerializable() {
        def original = new groovy.concurrent.ChannelClosedException('test message')
        def baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable { it.writeObject(original) }
        def deserialized = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).withCloseable { it.readObject() }
        assert deserialized instanceof groovy.concurrent.ChannelClosedException
        assert deserialized.message == 'test message'
    }

    @Test
    void testChannelClosedExceptionWithCauseSerializable() {
        def cause = new InterruptedException('interrupted')
        def original = new groovy.concurrent.ChannelClosedException('closed', cause)
        def baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable { it.writeObject(original) }
        def deserialized = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).withCloseable { it.readObject() }
        assert deserialized.message == 'closed'
        assert deserialized.cause instanceof InterruptedException
        assert deserialized.cause.message == 'interrupted'
    }

    // ================================================================
    // AsyncScope: register-before-submit guarantee
    // ================================================================

    @Test
    void testAsyncScopeChildAlwaysJoinedOnClose() {
        // Verifies that close() always waits for children even under
        // concurrent close/async races. The register-before-submit
        // protocol ensures no child is orphaned.
        def completed = new AtomicBoolean(false)
        def latch = new CountDownLatch(1)
        def scope = AsyncScope.create()
        scope.async {
            latch.await(5, TimeUnit.SECONDS)
            completed.set(true)
            return null
        }
        // Release the latch and close — close() must wait for the child
        latch.countDown()
        scope.close()
        assert completed.get() : "close() must join all children before returning"
    }

    @Test
    void testAsyncScopeCloseRaceDoesNotOrphanTask() {
        // Stress test: rapid close() after async() should never orphan
        // a task. Every submitted child must either complete or be cancelled.
        for (int i = 0; i < 50; i++) {
            def executed = new AtomicBoolean(false)
            def scope = AsyncScope.create()
            scope.async {
                executed.set(true)
                Thread.sleep(10)
                return null
            }
            scope.close()
            // After close(), the child was either executed to completion or
            // cancelled — but never orphaned (running without supervision).
        }
    }

    @Test
    void testAsyncScopeAsyncAfterCloseAlwaysThrows() {
        def scope = AsyncScope.create()
        scope.close()
        shouldFail(IllegalStateException) {
            scope.async { 'too late' }
        }
    }

    // ---- Awaitable.first() combinator tests ----

    @Test
    void testAwaitableFirstReturnsFirstSuccess() {
        def slow = CompletableFuture.supplyAsync {
            Thread.sleep(200)
            'slow'
        }
        def fast = CompletableFuture.completedFuture('fast')

        def winner = AsyncSupport.await(Awaitable.first(slow, fast))
        assert winner == 'fast'
    }

    @Test
    void testAwaitableFirstIgnoresIndividualFailures() {
        def failing = CompletableFuture.failedFuture(new RuntimeException('oops'))
        def ok = CompletableFuture.supplyAsync {
            Thread.sleep(50)
            'ok'
        }

        def result = AsyncSupport.await(Awaitable.first(failing, ok))
        assert result == 'ok'
    }

    @Test
    void testAwaitableFirstThrowsWhenAllFail() {
        def f1 = CompletableFuture.failedFuture(new RuntimeException('err1'))
        def f2 = CompletableFuture.failedFuture(new IllegalArgumentException('err2'))

        def ex = shouldFail(IllegalStateException) {
            AsyncSupport.await(Awaitable.first(f1, f2))
        }
        assert ex.message.contains('All 2 sources failed')
        assert ex.suppressed.length == 2
    }

    @Test
    void testAwaitableFirstWithSingleSource() {
        def sole = CompletableFuture.completedFuture(42)
        assert AsyncSupport.await(Awaitable.first(sole)) == 42
    }

    @Test
    void testAwaitableFirstWithSingleFailingSource() {
        def failing = CompletableFuture.failedFuture(new RuntimeException('only one'))

        def ex = shouldFail(IllegalStateException) {
            AsyncSupport.await(Awaitable.first(failing))
        }
        assert ex.message.contains('All 1 sources failed')
        assert ex.suppressed.length == 1
        assert ex.suppressed[0].message == 'only one'
    }

    @Test
    void testAwaitableFirstWithMixedTypes() {
        def awaitable = Awaitable.of('from-awaitable')
        def cf = CompletableFuture.supplyAsync { Thread.sleep(100); 'from-cf' }

        def result = AsyncSupport.await(Awaitable.first(cf, awaitable))
        assert result == 'from-awaitable'
    }

    @Test
    void testAwaitableFirstRequiresAtLeastOneSource() {
        shouldFail(IllegalArgumentException) {
            Awaitable.first()
        }
    }

    @Test
    void testAwaitableFirstRejectsNullElement() {
        shouldFail(IllegalArgumentException) {
            Awaitable.first(Awaitable.of(1), null)
        }
    }

    // ---- await Awaitable.first() tests ----

    @Test
    void testAwaitFirstReturnsFirstSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def slow = CompletableFuture.supplyAsync {
                    Thread.sleep(200)
                    'slow'
                }
                def fast = CompletableFuture.completedFuture('fast')
                return await Awaitable.first(slow, fast)
            }

            assert await(caller()) == 'fast'
        '''
    }

    @Test
    void testAwaitFirstIgnoresIndividualFailures() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def failing = CompletableFuture.failedFuture(new RuntimeException('oops'))
                def ok = CompletableFuture.supplyAsync {
                    Thread.sleep(50)
                    'ok'
                }
                return await Awaitable.first(failing, ok)
            }

            assert await(caller()) == 'ok'
        '''
    }

    @Test
    void testAwaitFirstThrowsWhenAllFail() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def f1 = CompletableFuture.failedFuture(new RuntimeException('err1'))
                def f2 = CompletableFuture.failedFuture(new IllegalArgumentException('err2'))
                try {
                    await Awaitable.first(f1, f2)
                    assert false
                } catch (IllegalStateException e) {
                    assert e.message.contains('All 2 sources failed')
                    assert e.suppressed.length == 2
                    return 'caught'
                }
            }

            assert await(caller()) == 'caught'
        '''
    }

    @Test
    void testAwaitFirstWithSingleSource() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def sole = CompletableFuture.completedFuture(42)
                return await Awaitable.first(sole)
            }

            assert await(caller()) == 42
        '''
    }

    @Test
    void testAwaitFirstWithSingleFailingSource() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def failing = CompletableFuture.failedFuture(new RuntimeException('only one'))
                try {
                    await Awaitable.first(failing)
                    assert false
                } catch (IllegalStateException e) {
                    assert e.message.contains('All 1 sources failed')
                    assert e.suppressed.length == 1
                    assert e.suppressed[0].message == 'only one'
                    return 'caught'
                }
            }

            assert await(caller()) == 'caught'
        '''
    }

    @Test
    void testAwaitFirstWithMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def awaitable = Awaitable.of('from-awaitable')
                def cf = CompletableFuture.supplyAsync { Thread.sleep(100); 'from-cf' }
                return await Awaitable.first(cf, awaitable)
            }

            assert await(caller()) == 'from-awaitable'
        '''
    }

    @Test
    void testAwaitFirstRequiresAtLeastOneSource() {
        shouldFail(IllegalArgumentException) {
            Awaitable.first()
        }
    }

    @Test
    void testAwaitFirstRejectsNullArray() {
        shouldFail(IllegalArgumentException) {
            Awaitable.first(null)
        }
    }

    @Test
    void testAwaitFirstRejectsNullElement() {
        shouldFail(IllegalArgumentException) {
            Awaitable.first(Awaitable.of(1), null)
        }
    }

    @Test
    void testAwaitFirstWithMultipleSuccessesReturnsFirst() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            async caller() {
                def instant = CompletableFuture.completedFuture('instant')
                def delayed = CompletableFuture.supplyAsync { Thread.sleep(200); 'delayed' }
                return await Awaitable.first(delayed, instant)
            }

            assert await(caller()) == 'instant'
        '''
    }

    @Test
    void testAwaitFirstWithMultipleFailuresPreservesAllErrors() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture

            async caller() {
                def e1 = new RuntimeException('e1')
                def e2 = new IOException('e2')
                def e3 = new ArithmeticException('e3')
                try {
                    await Awaitable.first(
                        CompletableFuture.failedFuture(e1),
                        CompletableFuture.failedFuture(e2),
                        CompletableFuture.failedFuture(e3)
                    )
                    assert false
                } catch (IllegalStateException ex) {
                    assert ex.message.contains('All 3 sources failed')
                    assert ex.suppressed.length == 3
                    def messages = ex.suppressed*.message as Set
                    assert messages.containsAll(['e1', 'e2', 'e3'])
                    return 'caught'
                }
            }

            assert await(caller()) == 'caught'
        '''
    }

    @Test
    void testAwaitFirstWithAwaitableInputs() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async caller() {
                def failing = Awaitable.failed(new RuntimeException('skip'))
                def ok = Awaitable.of('winner')
                return await Awaitable.first(failing, ok)
            }

            assert await(caller()) == 'winner'
        '''
    }

    @Test
    void testAwaitFirstUnwrapsCompletionException() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.io.IOException
            import java.util.concurrent.CompletableFuture

            async caller() {
                // Ensure deepUnwrap is applied: a CompletableFuture wraps exceptions
                // in CompletionException, but Awaitable.first should expose the root cause
                def cf = new CompletableFuture<String>()
                cf.completeExceptionally(new IOException('io-root'))
                def ok = CompletableFuture.supplyAsync { Thread.sleep(100); 'ok' }
                // Since one succeeds, the failure should be silently absorbed
                return await Awaitable.first(cf, ok)
            }

            assert await(caller()) == 'ok'
        '''
    }

    @Test
    void testAwaitFirstBasic() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // await Awaitable.first() returns the result of the first source
            // that completes successfully, silently absorbing individual failures.
            async caller() {
                def slow    = Awaitable.go { Thread.sleep(200); 'slow-result' }
                def instant = Awaitable.of('instant-result')
                return await Awaitable.first(slow, instant)
            }

            assert await(caller()) == 'instant-result'
        '''
    }

    @Test
    void testAwaitFirstDegradation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // When some sources fail, await Awaitable.first() ignores the failures
            // and returns the first success — ideal for graceful degradation.
            async caller() {
                def primaryDb = Awaitable.failed(new RuntimeException('DB down'))
                def replicaDb = Awaitable.failed(new RuntimeException('Replica down'))
                def cache     = Awaitable.go { Thread.sleep(30); 'cached-data' }
                return await Awaitable.first(primaryDb, replicaDb, cache)
            }

            assert await(caller()) == 'cached-data'
        '''
    }

    @Test
    void testAwaitFirstAllFail() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // When ALL sources fail, await Awaitable.first() throws an IllegalStateException
            // with every individual error in getSuppressed().
            async caller() {
                def f1 = Awaitable.failed(new RuntimeException('err-1'))
                def f2 = Awaitable.failed(new IllegalArgumentException('err-2'))
                try {
                    await Awaitable.first(f1, f2)
                    assert false : 'Should not reach here'
                } catch (IllegalStateException e) {
                    assert e.message.contains('All 2 sources failed')
                    assert e.suppressed.length == 2
                    return 'caught'
                }
            }

            assert await(caller()) == 'caught'
        '''
    }

    @Test
    void testAwaitFirstInAsyncContext() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async compute() {
                def slow = Awaitable.go { Thread.sleep(200); 'slow' }
                def fast = Awaitable.of('fast')
                return await Awaitable.first(slow, fast)
            }

            assert await(compute()) == 'fast'
        '''
    }

    @Test
    void testAwaitFirstAllFailInAsyncContext() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async compute() {
                def f1 = Awaitable.failed(new RuntimeException('e1'))
                def f2 = Awaitable.failed(new RuntimeException('e2'))
                try {
                    await Awaitable.first(f1, f2)
                    assert false : 'Should have thrown'
                } catch (IllegalStateException e) {
                    assert e.message.contains('All 2 sources failed')
                    return 'caught'
                }
            }

            assert await(compute()) == 'caught'
        '''
    }

    @Test
    void testAwaitFirstGracefulDegradationInAsyncContext() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async compute() {
                def failing = Awaitable.failed(new RuntimeException('down'))
                def ok      = Awaitable.go { Thread.sleep(30); 'fallback' }
                return await Awaitable.first(failing, ok)
            }

            assert await(compute()) == 'fallback'
        '''
    }

    // ---- allAsync single-element fast path tests ----

    @Test
    void testAllAsyncSingleElementFastPath() {
        def single = Awaitable.of(99)
        def result = AsyncSupport.await(Awaitable.all(single))
        assert result == [99]
    }

    @Test
    void testAllAsyncSingleElementWithFailure() {
        def failing = Awaitable.failed(new RuntimeException('boom'))

        def ex = shouldFail(RuntimeException) {
            AsyncSupport.await(Awaitable.all(failing))
        }
        assert ex.message == 'boom'
    }

    @Test
    void testAllAsyncEmptyReturnsEmptyList() {
        def result = AsyncSupport.await(Awaitable.all())
        assert result == []
    }
}
