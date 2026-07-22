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

import groovy.concurrent.Awaitable
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.shouldFail

/**
 * Focused unit coverage for the async runtime refactor
 * ({@link AsyncSupport}, {@link AsyncExecutors}, {@link AwaitCombinators}).
 */
final class AsyncSupportInternalsTest {

    @Test
    void testGoIsAliasOfAsync() {
        def viaGo = AsyncSupport.go { 11 }
        def viaAsync = AsyncSupport.async { 22 }
        assert AsyncSupport.await(viaGo) == 11
        assert AsyncSupport.await(viaAsync) == 22
    }

    @Test
    void testAwaitObjectOnCompletableFutureImplementsMultipleInterfaces() {
        // CompletableFuture is CompletionStage and Future; await(Object) is unambiguous
        CompletableFuture<String> cf = CompletableFuture.completedFuture('cf')
        assert AsyncSupport.await((Object) cf) == 'cf'
        assert AsyncSupport.await((CompletionStage<String>) cf) == 'cf'
        assert AsyncSupport.await(cf) == 'cf'
    }

    @Test
    void testAwaitUnwrapsNestedWrappers() {
        def cause = new IllegalStateException('root')
        def cf = new CompletableFuture()
        cf.completeExceptionally(new CompletionException(new ExecutionException(cause)))
        def thrown = shouldFail(IllegalStateException) {
            AsyncSupport.await(cf)
        }
        assert thrown.is(cause)
    }

    @Test
    void testAllAsyncEmptyAndSingle() {
        assert AsyncSupport.await(AsyncSupport.allAsync()) == []
        assert AsyncSupport.await(AsyncSupport.allAsync(Awaitable.of(1))) == [1]
        assert AsyncSupport.all() == []
        assert AsyncSupport.all(Awaitable.of('a'), Awaitable.of('b')) == ['a', 'b']
    }

    @Test
    void testAllPropagatesOriginalFailureNotWrapper() {
        def fail = Awaitable.failed(new RuntimeException('all-x'))
        def ok = Awaitable.of(1)
        def thrown = shouldFail(RuntimeException) {
            AsyncSupport.all(ok, fail)
        }
        assert thrown.message == 'all-x'
    }

    @Test
    void testFirstAllFailThrowsUnwrappedCause() {
        def f1 = Awaitable.failed(new RuntimeException('e1'))
        def f2 = Awaitable.failed(new RuntimeException('e2'))
        // Aggregate is a CompletionException; await transparency peels to the cause
        def thrown = shouldFail(RuntimeException) {
            AsyncSupport.await(AsyncSupport.firstAsync(f1, f2))
        }
        assert thrown.message == 'e1'
    }

    @Test
    void testFirstRejectsNullArrayAndNullElementAndEmpty() {
        shouldFail(IllegalArgumentException) {
            AsyncSupport.firstAsync((Object[]) null)
        }
        shouldFail(IllegalArgumentException) {
            AsyncSupport.firstAsync()
        }
        shouldFail(IllegalArgumentException) {
            AsyncSupport.firstAsync(Awaitable.of(1), null)
        }
    }

    @Test
    void testAnyAsyncPicksCompletedValue() {
        def result = AsyncSupport.await(AsyncSupport.anyAsync(
                Awaitable.of('winner'),
                asyncSleep(500, 'loser')))
        assert result == 'winner'
    }

    @Test
    void testAnyRejectsNullArrayAndNullElementAndEmpty() {
        // an empty anyOf() never completes, so validate rather than hang await()
        shouldFail(IllegalArgumentException) {
            AsyncSupport.anyAsync((Object[]) null)
        }
        shouldFail(IllegalArgumentException) {
            AsyncSupport.anyAsync()
        }
        shouldFail(IllegalArgumentException) {
            AsyncSupport.anyAsync(Awaitable.of(1), null)
        }
    }

    @Test
    void testAllSettledMixed() {
        def results = AsyncSupport.allSettled(
                Awaitable.of('ok'),
                Awaitable.failed(new RuntimeException('bad')))
        assert results.size() == 2
        assert results[0].success && results[0].value == 'ok'
        assert results[1].failure && results[1].error.message == 'bad'
    }

    @Test
    void testSetExecutorNullAndReset() {
        def original = AsyncSupport.getExecutor()
        def custom = Executors.newSingleThreadExecutor()
        try {
            AsyncSupport.setExecutor(custom)
            assert AsyncSupport.getExecutor().is(custom)
            AsyncSupport.setExecutor(null)
            assert AsyncSupport.getExecutor() != null
            assert !AsyncSupport.getExecutor().is(custom)
            AsyncSupport.setExecutor(custom)
            AsyncSupport.resetExecutor()
            assert !AsyncSupport.getExecutor().is(custom)
        } finally {
            AsyncSupport.setExecutor(original)
            custom.shutdownNow()
        }
    }

    @Test
    void testDelayAndTimeouts() {
        assert AsyncSupport.await(AsyncSupport.delay(1)) == null
        assert AsyncSupport.await(AsyncSupport.delay(1, TimeUnit.MILLISECONDS)) == null

        def slow = asyncSleep(5_000, 'late')
        def timedOut = shouldFail(TimeoutException) {
            AsyncSupport.await(AsyncSupport.orTimeoutMillis(slow, 20))
        }
        assert timedOut.message.contains('Timed out')

        def fallback = AsyncSupport.await(
                AsyncSupport.completeOnTimeoutMillis(asyncSleep(5_000, 'late'), 'fb', 20))
        assert fallback == 'fb'
    }

    @Test
    void testToIterableAndCloseIterable() {
        assert AsyncSupport.toIterable(null).iterator().toList() == []
        assert AsyncSupport.toIterable([1, 2]).iterator().toList() == [1, 2]
        assert AsyncSupport.toIterable((Object[]) ['a', 'b']).iterator().toList() == ['a', 'b']
        def it = [9].iterator()
        assert AsyncSupport.toIterable(it).iterator().is(it)

        def closed = new boolean[1]
        def closeable = new Closeable() {
            @Override void close() { closed[0] = true }
        }
        AsyncSupport.closeIterable(closeable)
        assert closed[0]

        def autoClosed = new boolean[1]
        def auto = new AutoCloseable() {
            @Override void close() { autoClosed[0] = true }
        }
        AsyncSupport.closeIterable(auto)
        assert autoClosed[0]

        // non-closeable is a no-op
        AsyncSupport.closeIterable('plain')
    }

    @Test
    void testCloseIterableSwallowsCloseException() {
        def bad = new Closeable() {
            @Override void close() { throw new IOException('ignore-me') }
        }
        AsyncSupport.closeIterable(bad) // must not throw
    }

    @Test
    void testDeferScopeLIFOAndNullGuards() {
        def log = []
        def scope = AsyncSupport.createDeferScope()
        AsyncSupport.defer(scope, { log << 'a'; null })
        AsyncSupport.defer(scope, { log << 'b'; null })
        AsyncSupport.executeDeferScope(scope)
        assert log == ['b', 'a']

        shouldFail(IllegalStateException) {
            AsyncSupport.defer(null, { null })
        }
        shouldFail(IllegalArgumentException) {
            AsyncSupport.defer(AsyncSupport.createDeferScope(), null)
        }
        AsyncSupport.executeDeferScope(null) // no-op
        AsyncSupport.executeDeferScope(AsyncSupport.createDeferScope()) // empty no-op
    }

    @Test
    void testYieldReturnRequiresGeneratorBridge() {
        shouldFail(IllegalStateException) {
            AsyncSupport.yieldReturn('not-a-bridge', 1)
        }
    }

    @Test
    void testAsyncGeneratorProducesValues() {
        def values = AsyncSupport.<Integer>asyncGenerator { bridge ->
            AsyncSupport.yieldReturn(bridge, 1)
            AsyncSupport.yieldReturn(bridge, 2)
        }.iterator().toList()
        assert values == [1, 2]
    }

    @Test
    void testWrapForFutureAndUnwrap() {
        def ce = new CompletionException(new RuntimeException('x'))
        assert AsyncSupport.wrapForFuture(ce).is(ce)
        def wrapped = AsyncSupport.wrapForFuture(new RuntimeException('y'))
        assert wrapped instanceof CompletionException
        assert AsyncSupport.unwrap(wrapped).message == 'y'
    }

    @Test
    void testIsVirtualThreadsAvailableIsConsistent() {
        boolean available = AsyncSupport.isVirtualThreadsAvailable()
        assert available == Awaitable.isVirtualThreadsAvailable()
    }

    @Test
    void testSchedulerIsDaemon() {
        def scheduler = AsyncSupport.getScheduler()
        assert scheduler != null
    }

    private static Awaitable asyncSleep(long millis, Object value) {
        AsyncSupport.async {
            Thread.sleep(millis)
            value
        }
    }
}
