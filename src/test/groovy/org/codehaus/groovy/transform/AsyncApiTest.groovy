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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Flow
import java.util.concurrent.FutureTask
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static groovy.test.GroovyAssert.shouldFail

/**
 * Pure API verification tests for Groovy's async/await runtime classes.
 * <p>
 * Every test in this class exercises the public API of {@code AsyncSupport},
 * {@code AsyncStreamGenerator}, {@code GroovyPromise}, {@code Awaitable},
 * {@code AwaitResult}, and {@code AwaitableAdapterRegistry} directly —
 * without {@code assertScript} or compiler-driven async transformation.
 * <p>
 * Pure API surface verification that runs independently of the AST
 * transformation tests.
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

    @Test
    void testAwaitCompletionStage() {
        // Create a pure CompletionStage wrapper so Groovy dispatches to await(CompletionStage)
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
            AsyncSupport.await((java.util.concurrent.Future<String>) ft)
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
    // Combinators: awaitAll / awaitAny / awaitAllSettled
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

    @Test
    void testAwaitAllWithFailedCompletableFutureUnwrapsCause() {
        def failed = new CompletableFuture<String>()
        failed.completeExceptionally(new IOException('all-fail'))
        def ex = shouldFail(IOException) {
            AsyncSupport.awaitAll(Awaitable.of('ok'), failed)
        }
        assert ex.message == 'all-fail'
    }

    @Test
    void testAwaitAnyWithFailedCompletableFutureUnwrapsCause() {
        def failed = new CompletableFuture<String>()
        failed.completeExceptionally(new IOException('any-fail'))
        def ex = shouldFail(IOException) {
            AsyncSupport.awaitAny(failed)
        }
        assert ex.message == 'any-fail'
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
        // LIFO order
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
        // Test indirectly via await(Future) — errors are rethrown directly
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
            gen.error(null) // should be treated as NullPointerException
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

        def ex = shouldFail(java.lang.reflect.InvocationTargetException) {
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

        def ex = shouldFail(java.lang.reflect.InvocationTargetException) {
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
            assert e instanceof IOException || e instanceof java.util.concurrent.ExecutionException
        }
    }

    @Test
    void testFutureAdapterInterruptedFutureMapsToCancellation() {
        def interruptedFuture = new java.util.concurrent.Future<String>() {
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
        def cancelledFuture = new java.util.concurrent.Future<String>() {
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
        def brokenFuture = new java.util.concurrent.Future<String>() {
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
        def custom = { Runnable r -> r.run() } as java.util.concurrent.Executor
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
        assert ex.cause instanceof java.util.concurrent.TimeoutException
    }

    @Test
    void testAwaitableOrTimeoutWithUnitInstanceMethod() {
        def cf = new CompletableFuture<>()
        def awaitable = GroovyPromise.of(cf)
        def withTimeout = awaitable.orTimeout(50, TimeUnit.MILLISECONDS)
        def ex = shouldFail(ExecutionException) {
            withTimeout.get()
        }
        assert ex.cause instanceof java.util.concurrent.TimeoutException
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
        shouldFail(java.util.concurrent.TimeoutException) {
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
        // Should not throw or block
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
        // Should not throw or block
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
        // All should be no-ops; moveNext should still return false
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
        assert ex.cause instanceof java.util.concurrent.TimeoutException
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
            def custom = java.util.concurrent.Executors.newSingleThreadExecutor()
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
}
