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
package groovy

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class AsyncAwaitTest {

    // === Layer 1: basic async/await ===

    @Test
    void testAsyncAndAwait() {
        assertScript '''
            def task = async { 21 * 2 }
            def result = await task
            assert result == 42
        '''
    }

    @Test
    void testAwaitCompletableFuture() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.supplyAsync { 'hello' }
            def result = await cf
            assert result == 'hello'
        '''
    }

    @Test
    void testAsyncExceptionHandling() {
        assertScript '''
            def task = async { throw new IOException('test error') }
            try {
                await task
            } catch (IOException e) {
                assert e.message == 'test error'
                return
            }
            assert false : 'Should have caught IOException'
        '''
    }

    @Test
    void testAsyncParallelExecution() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicInteger

            def counter = new AtomicInteger(0)
            def task1 = async { Thread.sleep(50); counter.incrementAndGet(); 'a' }
            def task2 = async { Thread.sleep(50); counter.incrementAndGet(); 'b' }
            def r1 = await task1
            def r2 = await task2
            assert counter.get() == 2
            assert r1 == 'a'
            assert r2 == 'b'
        '''
    }

    @Test
    void testAwaitNull() {
        assertScript '''
            def result = await null
            assert result == null
        '''
    }

    @Test
    void testAsyncKeywordAsVariable() {
        assertScript '''
            def async = 'hello'
            assert async.toUpperCase() == 'HELLO'
        '''
    }

    @Test
    void testAwaitKeywordAsVariable() {
        assertScript '''
            def await = 42
            assert await == 42
        '''
    }

    // === Layer 2: combinators ===

    @Test
    void testMultiArgAwaitAll() {
        assertScript '''
            def a = async { 1 }
            def b = async { 2 }
            def c = async { 3 }
            def results = await(a, b, c)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitableAll() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = async { 'x' }
            def b = async { 'y' }
            def results = await Awaitable.all(a, b)
            assert results == ['x', 'y']
        '''
    }

    @Test
    void testAwaitableAny() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fast = async { 'fast' }
            def slow = async { Thread.sleep(500); 'slow' }
            def result = await Awaitable.any(fast, slow)
            assert result == 'fast'
        '''
    }

    @Test
    void testAwaitableFirst() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fail1 = async { throw new RuntimeException('fail') }
            def success = async { 'ok' }
            def result = await Awaitable.first(fail1, success)
            assert result == 'ok'
        '''
    }

    @Test
    void testAwaitableAllSettled() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = async { 42 }
            def fail = async { throw new RuntimeException('boom') }
            def results = await Awaitable.allSettled(ok, fail)
            assert results.size() == 2
            assert results[0].success
            assert results[0].value == 42
            assert !results[1].success
            assert results[1].error.message == 'boom'
        '''
    }

    @Test
    void testAwaitableDelay() {
        assertScript '''
            import groovy.concurrent.Awaitable

            long start = System.currentTimeMillis()
            await Awaitable.delay(100)
            long elapsed = System.currentTimeMillis() - start
            assert elapsed >= 90
        '''
    }

    @Test
    void testAwaitableOrTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeoutException

            def slow = async { Thread.sleep(1000); 'done' }
            try {
                await Awaitable.orTimeoutMillis(slow, 100)
                assert false : 'Should have timed out'
            } catch (TimeoutException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitableCompleteOnTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def slow = async { Thread.sleep(1000); 'done' }
            def result = await Awaitable.completeOnTimeoutMillis(slow, 'fallback', 100)
            assert result == 'fallback'
        '''
    }

    @Test
    void testAwaitableGo() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = Awaitable.go { 42 }
            def result = await task
            assert result == 42
        '''
    }

    // === Layer 4: structured concurrency ===

    @Test
    void testWithScopeBasic() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def result = AsyncScope.withScope { scope ->
                def a = scope.async { 1 }
                def b = scope.async { 2 }
                [await(a), await(b)]
            }
            assert result == [1, 2]
        '''
    }

    @Test
    void testWithScopeViaAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = Awaitable.withScope { scope ->
                def task = scope.async { 'hello' }
                await task
            }
            assert result == 'hello'
        '''
    }

    @Test
    void testScopeFailFastCancelsSiblings() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CountDownLatch

            def slowStarted = new CountDownLatch(1)
            def result = null
            try {
                AsyncScope.withScope { scope ->
                    def slow = scope.async {
                        slowStarted.countDown()
                        Thread.sleep(1000)
                        'should not complete'
                    }
                    def failing = scope.async {
                        slowStarted.await()
                        Thread.sleep(50)
                        throw new RuntimeException('fail-fast')
                    }
                }
            } catch (RuntimeException e) {
                result = e.message
            }
            assert result == 'fail-fast'
        '''
    }

    @Test
    void testScopeWaitsForAllChildren() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.atomic.AtomicInteger

            def counter = new AtomicInteger(0)
            AsyncScope.withScope { scope ->
                scope.async { Thread.sleep(50); counter.incrementAndGet() }
                scope.async { Thread.sleep(50); counter.incrementAndGet() }
                scope.async { Thread.sleep(50); counter.incrementAndGet() }
            }
            assert counter.get() == 3
        '''
    }

    @Test
    void testScopeRejectsAfterClose() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.close()
            try {
                scope.async { 'too late' }
                assert false : 'Should have thrown'
            } catch (IllegalStateException e) {
                assert e.message.contains('closed')
            }
        '''
    }

    @Test
    void testScopeCurrent() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            assert AsyncScope.current() == null
            AsyncScope.withScope { scope ->
                def childSaw = scope.async {
                    AsyncScope.current() != null
                }
                assert await(childSaw)
            }
            assert AsyncScope.current() == null
        '''
    }

    @Test
    void testNestedScopes() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def result = AsyncScope.withScope { outer ->
                def a = outer.async { 'outer-task' }
                def inner = AsyncScope.withScope { innerScope ->
                    def b = innerScope.async { 'inner-task' }
                    await b
                }
                [await(a), inner]
            }
            assert result == ['outer-task', 'inner-task']
        '''
    }

    @Test
    void testScopeWithSupplier() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.function.Supplier

            def result = AsyncScope.withScope { scope ->
                Supplier<String> sup = () -> 'from supplier'
                def task = scope.async(sup)
                await task
            }
            assert result == 'from supplier'
        '''
    }

    // === yield return (generators) ===

    @Test
    void testYieldReturnBasic() {
        assertScript '''
            def items = async {
                yield return 1
                yield return 2
                yield return 3
            }
            def results = []
            for (item in items) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testYieldReturnWithLoop() {
        assertScript '''
            def items = async {
                for (i in 1..5) {
                    yield return i * 10
                }
            }
            assert items.collect() == [10, 20, 30, 40, 50]
        '''
    }

    // === channels ===

    @Test
    void testChannelBasic() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(2)
            async {
                await ch.send('a')
                await ch.send('b')
                ch.close()
            }
            def results = []
            for await (item in ch) {
                results << item
            }
            assert results == ['a', 'b']
        '''
    }

    @Test
    void testChannelUnbuffered() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create()  // rendezvous
            async {
                await ch.send(1)
                await ch.send(2)
                await ch.send(3)
                ch.close()
            }
            def results = []
            for (item in ch) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testChannelProducerConsumer() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(3)
            // Producer
            async {
                for (i in 1..5) {
                    await ch.send(i * 10)
                }
                ch.close()
            }
            // Consumer
            def sum = 0
            for await (value in ch) {
                sum += value
            }
            assert sum == 150
        '''
    }

    @Test
    void testChannelClosedForSend() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def ch = AsyncChannel.create(1)
            ch.close()
            try {
                await ch.send('too late')
                assert false : 'Should have thrown'
            } catch (ChannelClosedException e) {
                assert e.message.contains('closed')
            }
        '''
    }

    @Test
    void testChannelQueryMethods() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            assert ch.capacity == 5
            assert ch.bufferedSize == 0
            assert !ch.closed
            await ch.send('x')
            assert ch.bufferedSize == 1
            ch.close()
            assert ch.closed
        '''
    }

    @Test
    void testYieldReturnExceptionPropagation() {
        assertScript '''
            def items = async {
                yield return 1
                throw new RuntimeException('generator error')
            }
            def results = []
            try {
                for (item in items) {
                    results << item
                }
                assert false : 'Should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'generator error'
            }
            assert results == [1]
        '''
    }

    // === for await ===

    @Test
    void testForAwaitWithList() {
        assertScript '''
            def results = []
            for await (item in [1, 2, 3]) {
                results << item * 2
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testForAwaitWithGenerator() {
        assertScript '''
            def items = async {
                yield return 'a'
                yield return 'b'
                yield return 'c'
            }
            def results = []
            for await (item in items) {
                results << item.toUpperCase()
            }
            assert results == ['A', 'B', 'C']
        '''
    }

    @Test
    void testForAwaitWithArray() {
        assertScript '''
            def results = []
            String[] arr = ['x', 'y', 'z']
            for await (item in arr) {
                results << item
            }
            assert results == ['x', 'y', 'z']
        '''
    }

    // === regular for loop with generators ===

    @Test
    void testRegularForLoopWithGenerator() {
        assertScript '''
            def items = async {
                yield return 'a'
                yield return 'b'
                yield return 'c'
            }
            def results = []
            for (item in items) {
                results << item
            }
            assert results == ['a', 'b', 'c']
        '''
    }

    @Test
    void testCollectWithGenerator() {
        assertScript '''
            def squares = async {
                for (i in 1..5) {
                    yield return i * i
                }
            }
            assert squares.collect() == [1, 4, 9, 16, 25]
        '''
    }

    // === defer ===

    @Test
    void testDeferBasic() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'cleanup' }
                log << 'work'
                'result'
            }
            def result = await task
            assert result == 'result'
            assert log == ['work', 'cleanup']
        '''
    }

    @Test
    void testDeferLIFOOrder() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'first registered, last to run' }
                defer { log << 'second registered, first to run' }
                log << 'body'
                'done'
            }
            await task
            assert log == ['body', 'second registered, first to run', 'first registered, last to run']
        '''
    }

    @Test
    void testDeferRunsOnException() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'cleanup' }
                throw new RuntimeException('oops')
            }
            try {
                await task
            } catch (RuntimeException e) {
                assert e.message == 'oops'
            }
            assert log == ['cleanup']
        '''
    }

    @Test
    void testDeferWithClosure() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'deferred closure' }
                log << 'body'
                42
            }
            assert await(task) == 42
            assert log == ['body', 'deferred closure']
        '''
    }

    // === executor configuration ===

    @Test
    void testCustomExecutor() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import java.util.concurrent.Executors

            def exec = Executors.newFixedThreadPool(2) { r ->
                def t = new Thread(r, 'custom-pool')
                t.daemon = true
                t
            }
            try {
                AsyncSupport.setExecutor(exec)
                def task = async { Thread.currentThread().name }
                def name = await task
                assert name == 'custom-pool'
            } finally {
                AsyncSupport.resetExecutor()
                exec.shutdown()
            }
        '''
    }

    @Test
    void testVirtualThreadsDetection() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def available = AsyncSupport.isVirtualThreadsAvailable()
            assert available instanceof Boolean
        '''
    }

    @Test
    void testAwaitableOf() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.of('ready')
            assert a.isDone()
            assert !a.isCompletedExceptionally()
            assert a.get() == 'ready'
        '''
    }

    @Test
    void testAwaitableOfNull() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable a = Awaitable.of(null)
            assert a.isDone()
            assert a.get() == null
        '''
    }

    @Test
    void testAwaitableFailed() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.ExecutionException

            Awaitable<String> a = Awaitable.failed(new IOException('disk full'))
            assert a.isDone()
            assert a.isCompletedExceptionally()
            try {
                a.get()
                assert false : 'should have thrown'
            } catch (ExecutionException e) {
                assert e.cause instanceof IOException
                assert e.cause.message == 'disk full'
            }
        '''
    }

    @Test
    void testAwaitableFromCompletableFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture(99)
            Awaitable<Integer> a = Awaitable.from(cf)
            assert a.get() == 99
            assert a.isDone()
        '''
    }

    @Test
    void testAwaitableFromCompletionStage() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionStage

            CompletionStage<String> cs = CompletableFuture.supplyAsync { 'from-stage' }
            Awaitable<String> a = Awaitable.from(cs)
            assert a.get() == 'from-stage'
        '''
    }

    @Test
    void testAwaitCompletionStage() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionStage

            CompletionStage<Integer> cs = CompletableFuture.supplyAsync { 77 }
            def result = await cs
            assert result == 77
        '''
    }

    @Test
    void testAwaitPlainFuture() {
        assertScript '''
            import java.util.concurrent.Callable
            import java.util.concurrent.Executors
            import java.util.concurrent.Future

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future<String> f = exec.submit({ 'from-future' } as Callable)
                def result = await f
                assert result == 'from-future'
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testAwaitClosureThrowsIAE() {
        shouldFail '''
            def closure = { 42 }
            await closure
        '''
    }

    @Test
    void testAwaitAlreadyResolvedValue() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture('already-done')
            def result = await cf
            assert result == 'already-done'
        '''
    }

    @Test
    void testAwaitableThenTransform() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<Integer> a = Awaitable.of(5)
            Awaitable<Integer> doubled = a.then { it * 2 }
            assert await(doubled) == 10
        '''
    }

    @Test
    void testAwaitableThenCompose() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<Integer> a = Awaitable.of(5)
            Awaitable<Integer> composed = a.thenCompose { val ->
                Awaitable.of(val * 10)
            }
            assert await(composed) == 50
        '''
    }

    @Test
    void testAwaitableThenAccept() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            def sideEffect = new AtomicReference()
            Awaitable<Integer> a = Awaitable.of(42)
            Awaitable<Void> result = a.thenAccept { sideEffect.set(it) }
            await result
            assert sideEffect.get() == 42
        '''
    }

    @Test
    void testAwaitableExceptionallyRecovery() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.failed(new RuntimeException('oops'))
            Awaitable<String> recovered = a.exceptionally { 'recovered' }
            assert await(recovered) == 'recovered'
        '''
    }

    @Test
    void testAwaitableThenDoesNotRunOnFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ran = false
            Awaitable<String> a = Awaitable.failed(new RuntimeException('fail'))
            Awaitable<String> chained = a.then { ran = true; it }
            Thread.sleep(100)
            assert !ran
        '''
    }

    @Test
    void testAwaitableWhenComplete() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            def observed = new AtomicReference()
            Awaitable<String> a = Awaitable.of('hello')
            Awaitable<String> result = a.whenComplete { val, err ->
                observed.set(val)
            }
            assert result.get() == 'hello'
            Thread.sleep(50)
            assert observed.get() == 'hello'
        '''
    }

    @Test
    void testAwaitableWhenCompleteOnFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            def observed = new AtomicReference()
            Awaitable<String> a = Awaitable.failed(new RuntimeException('err'))
            Awaitable<String> result = a.whenComplete { val, err ->
                observed.set(err?.message)
            }
            Thread.sleep(100)
            assert observed.get() == 'err'
        '''
    }

    @Test
    void testAwaitableHandleSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.of('ok')
            Awaitable<String> handled = a.handle { val, err ->
                err == null ? "handled: $val" : "error"
            }
            assert await(handled) == 'handled: ok'
        '''
    }

    @Test
    void testAwaitableHandleFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.failed(new RuntimeException('boom'))
            Awaitable<String> handled = a.handle { val, err ->
                err != null ? "caught: $err.message" : val
            }
            assert await(handled) == 'caught: boom'
        '''
    }

    @Test
    void testAwaitableToCompletableFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            Awaitable<Integer> a = Awaitable.of(42)
            CompletableFuture<Integer> cf = a.toCompletableFuture()
            assert cf.get() == 42
            assert cf.isDone()
        '''
    }

    @Test
    void testAwaitableGetWithTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit

            Awaitable<String> a = Awaitable.of('fast')
            assert a.get(1, TimeUnit.SECONDS) == 'fast'
        '''
    }

    @Test
    void testAwaitableGetWithTimeoutExpires() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            def task = async { Thread.sleep(1000); 'slow' }
            Awaitable a = Awaitable.from(task)
            try {
                a.get(50, TimeUnit.MILLISECONDS)
                assert false : 'should have timed out'
            } catch (TimeoutException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitableCancelAndIsCancelled() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async { Thread.sleep(1000); 'done' }
            Awaitable a = Awaitable.from(task)
            assert !a.isCancelled()
            a.cancel()
            assert a.isDone()
            assert a.isCancelled()
        '''
    }

    @Test
    void testAwaitableIsCompletedExceptionally() {
        assertScript '''
            import groovy.concurrent.Awaitable

            Awaitable a = Awaitable.failed(new RuntimeException('err'))
            assert a.isCompletedExceptionally()
            assert a.isDone()
        '''
    }

    @Test
    void testAwaitableOrTimeoutInstance() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeoutException

            def slow = async { Thread.sleep(1000); 'done' }
            Awaitable a = Awaitable.from(slow)
            try {
                await a.orTimeoutMillis(50)
                assert false : 'should have timed out'
            } catch (TimeoutException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitableCompleteOnTimeoutInstance() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def slow = async { Thread.sleep(1000); 'done' }
            Awaitable a = Awaitable.from(slow)
            def result = await a.completeOnTimeoutMillis('fallback', 50)
            assert result == 'fallback'
        '''
    }

    @Test
    void testAwaitableOrTimeoutWithTimeUnit() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            def slow = async { Thread.sleep(1000); 'done' }
            try {
                await Awaitable.orTimeout(slow, 50, TimeUnit.MILLISECONDS)
                assert false : 'should have timed out'
            } catch (TimeoutException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitableCompleteOnTimeoutWithTimeUnit() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit

            def slow = async { Thread.sleep(1000); 'done' }
            def result = await Awaitable.completeOnTimeout(slow, 'fallback', 50, TimeUnit.MILLISECONDS)
            assert result == 'fallback'
        '''
    }

    @Test
    void testCheckedExceptionTransparency() {
        assertScript '''
            import java.sql.SQLException

            def task = async { throw new SQLException('db error') }
            try {
                await task
                assert false : 'should have thrown'
            } catch (SQLException e) {
                assert e.message == 'db error'
            }
        '''
    }

    @Test
    void testErrorPropagation() {
        assertScript '''
            def task = async { throw new StackOverflowError('deep stack') }
            try {
                await task
                assert false : 'should have thrown'
            } catch (StackOverflowError e) {
                assert e.message == 'deep stack'
            }
        '''
    }

    @Test
    void testDeepUnwrapCompletionException() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionException
            import java.util.concurrent.ExecutionException

            def cf = new CompletableFuture()
            cf.completeExceptionally(
                new CompletionException(
                    new ExecutionException(
                        new IllegalStateException('deep'))))
            try {
                await cf
                assert false
            } catch (IllegalStateException e) {
                assert e.message == 'deep'
            }
        '''
    }

    @Test
    void testAllWithFailureThrowsOriginal() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = async { 1 }
            def fail = async { throw new RuntimeException('all-fail') }
            try {
                await Awaitable.all(ok, fail)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'all-fail'
            }
        '''
    }

    @Test
    void testFirstAllFailThrows() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def f1 = async { throw new RuntimeException('err1') }
            def f2 = async { throw new RuntimeException('err2') }
            try {
                await Awaitable.first(f1, f2)
                assert false : 'should have thrown'
            } catch (Exception e) {
                assert e != null
            }
        '''
    }

    @Test
    void testAllSettledWithCancelledTask() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async { Thread.sleep(1000); 'done' }
            Awaitable a = Awaitable.from(task)
            a.cancel()
            def results = await Awaitable.allSettled(Awaitable.of(42), a)
            assert results[0].success
            assert results[0].value == 42
            assert !results[1].success
        '''
    }

    @Test
    void testTryCatchFinallyInAsync() {
        assertScript '''
            def log = []
            def task = async {
                try {
                    log << 'try'
                    throw new RuntimeException('oops')
                } catch (RuntimeException e) {
                    log << "catch:$e.message"
                } finally {
                    log << 'finally'
                }
                'done'
            }
            assert await(task) == 'done'
            assert log == ['try', 'catch:oops', 'finally']
        '''
    }

    @Test
    void testMultipleCatchBlocksInAsync() {
        assertScript '''
            def task = async {
                try {
                    throw new IOException('io error')
                } catch (IllegalArgumentException e) {
                    return 'iae'
                } catch (IOException e) {
                    return "io:$e.message"
                } catch (Exception e) {
                    return 'generic'
                }
            }
            assert await(task) == 'io:io error'
        '''
    }

    @Test
    void testNestedTryCatchInAsync() {
        assertScript '''
            def task = async {
                try {
                    try {
                        throw new RuntimeException('inner')
                    } catch (RuntimeException e) {
                        throw new IllegalStateException("wrapped:$e.message")
                    }
                } catch (IllegalStateException e) {
                    return e.message
                }
            }
            assert await(task) == 'wrapped:inner'
        '''
    }

    @Test
    void testExceptionWithNullMessage() {
        assertScript '''
            def task = async { throw new RuntimeException((String) null) }
            try {
                await task
                assert false
            } catch (RuntimeException e) {
                assert e.message == null
            }
        '''
    }

    @Test
    void testAwaitInArithmeticExpression() {
        assertScript '''
            def a = async { 10 }
            def b = async { 20 }
            def result = (await a) + (await b)
            assert result == 30
        '''
    }

    @Test
    void testAwaitInStringInterpolation() {
        assertScript '''
            def task = async { 'world' }
            def result = "hello ${ await task }"
            assert result == 'hello world'
        '''
    }

    @Test
    void testAwaitInCollectionLiteral() {
        assertScript '''
            def a = async { 1 }
            def b = async { 2 }
            def result = [await(a), await(b), 3]
            assert result == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitInMapLiteral() {
        assertScript '''
            def k = async { 'key' }
            def v = async { 'value' }
            def result = [(await k): await(v)]
            assert result == [key: 'value']
        '''
    }

    @Test
    void testAwaitAsMethodArgument() {
        assertScript '''
            def square(int n) { n * n }
            def task = async { 7 }
            def result = square(await task)
            assert result == 49
        '''
    }

    @Test
    void testNestedAwaitExpressions() {
        assertScript '''
            def inner = async { 5 }
            def outer = async { (await inner) * 2 }
            assert await(outer) == 10
        '''
    }

    @Test
    void testAwaitChainedMethodCallOnResult() {
        assertScript '''
            def task = async { 'hello world' }
            def result = (await task).toUpperCase().split(' ')
            assert result == ['HELLO', 'WORLD'] as String[]
        '''
    }

    @Test
    void testAwaitBooleanResult() {
        assertScript '''
            def task = async { 10 > 5 }
            assert await(task) == true
        '''
    }

    @Test
    void testAwaitInTernaryExpression() {
        assertScript '''
            def cond = async { true }
            def result = (await cond) ? 'yes' : 'no'
            assert result == 'yes'
        '''
    }

    @Test
    void testAwaitInIfCondition() {
        assertScript '''
            def cond = async { true }
            def result
            if (await cond) {
                result = 'taken'
            } else {
                result = 'not taken'
            }
            assert result == 'taken'
        '''
    }

    @Test
    void testAwaitInWhileLoop() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicInteger

            def counter = new AtomicInteger(0)
            def shouldContinue = { -> counter.get() < 3 }
            while (shouldContinue()) {
                def task = async { counter.incrementAndGet() }
                await task
            }
            assert counter.get() == 3
        '''
    }

    @Test
    void testMultipleSequentialAwaits() {
        assertScript '''
            def a = async { 'a' }
            def b = async { 'b' }
            def c = async { 'c' }
            def r1 = await a
            def r2 = await b
            def r3 = await c
            assert r1 + r2 + r3 == 'abc'
        '''
    }

    @Test
    void testDelayWithTimeUnit() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit

            long start = System.currentTimeMillis()
            await Awaitable.delay(100, TimeUnit.MILLISECONDS)
            assert System.currentTimeMillis() - start >= 90
        '''
    }

    @Test
    void testDelayZeroCompletesImmediately() {
        assertScript '''
            import groovy.concurrent.Awaitable

            long start = System.currentTimeMillis()
            await Awaitable.delay(0)
            assert System.currentTimeMillis() - start < 1000
        '''
    }

    @Test
    void testDeferMultipleErrors() {
        assertScript '''
            def task = async {
                defer { throw new RuntimeException('defer-err-1') }
                defer { throw new RuntimeException('defer-err-2') }
                'body-done'
            }
            try {
                await task
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'defer-err-2'
                assert e.suppressed.length == 1
                assert e.suppressed[0].message == 'defer-err-1'
            }
        '''
    }

    @Test
    void testDeferMultipleInSameAsync() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'defer-1' }
                defer { log << 'defer-2' }
                defer { log << 'defer-3' }
                log << 'body'
                'result'
            }
            assert await(task) == 'result'
            assert log[0] == 'body'
            assert log.contains('defer-1')
            assert log.contains('defer-2')
            assert log.contains('defer-3')
        '''
    }

    @Test
    void testChannelNullPayloadThrows() {
        shouldFail '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(1)
            await ch.send(null)
        '''
    }

    @Test
    void testChannelCloseIdempotent() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(1)
            assert ch.close() == true
            assert ch.close() == false
            assert ch.closed
        '''
    }

    @Test
    void testChannelToString() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            def str = ch.toString()
            assert str.contains('capacity=5')
            assert str.contains('buffered=0')
            assert str.contains('closed=false')
        '''
    }

    @Test
    void testChannelReceiveFromClosedEmpty() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def ch = AsyncChannel.create(1)
            ch.close()
            try {
                await ch.receive()
                assert false : 'should have thrown'
            } catch (ChannelClosedException e) {
                assert true
            }
        '''
    }

    @Test
    void testChannelClosePreservesBuffered() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            await ch.send('a')
            await ch.send('b')
            ch.close()
            assert await(ch.receive()) == 'a'
            assert await(ch.receive()) == 'b'
        '''
    }

    @Test
    void testChannelMultipleProducers() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(10)
            def p1 = async { (1..5).each { await ch.send("p1-$it") } }
            def p2 = async { (1..5).each { await ch.send("p2-$it") } }
            await p1
            await p2
            ch.close()
            def results = []
            for (item in ch) { results << item }
            assert results.size() == 10
            assert results.count { it.startsWith('p1-') } == 5
            assert results.count { it.startsWith('p2-') } == 5
        '''
    }

    @Test
    void testChannelReceiveExplicit() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(2)
            await ch.send(10)
            await ch.send(20)
            assert await(ch.receive()) == 10
            assert await(ch.receive()) == 20
        '''
    }

    @Test
    void testScopeGetChildCount() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                assert scope.childCount == 0
                def a = scope.async { Thread.sleep(100); 'a' }
                assert scope.childCount >= 1
            }
        '''
    }

    @Test
    void testScopeCancelAll() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CancellationException

            def scope = AsyncScope.create()
            def t1 = scope.async { Thread.sleep(1000); 'slow1' }
            def t2 = scope.async { Thread.sleep(1000); 'slow2' }
            scope.cancelAll()
            assert t1.isCancelled() || t1.isDone()
            assert t2.isCancelled() || t2.isDone()
            scope.close()
        '''
    }

    @Test
    void testScopeCreateWithExecutor() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.Executors

            def exec = Executors.newSingleThreadExecutor()
            try {
                def result = AsyncScope.withScope(exec) { scope ->
                    def t = scope.async { Thread.currentThread().name }
                    await t
                }
                assert result instanceof String
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testScopeCloseWithCancelledChild() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                def t = scope.async { Thread.sleep(1000); 'slow' }
                t.cancel()
            }
        '''
    }

    @Test
    void testScopeNonFailFast() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            def completed = new AtomicInteger(0)
            try {
                def scope = AsyncScope.create(
                    Awaitable.getExecutor(), false)
                scope.async { completed.incrementAndGet() }
                scope.async { throw new RuntimeException('fail') }
                scope.async { Thread.sleep(50); completed.incrementAndGet() }
                scope.close()
            } catch (RuntimeException e) {
                assert e.message == 'fail'
            }
            assert completed.get() == 2
        '''
    }

    @Test
    void testGeneratorBreakClosesProducer() {
        assertScript '''
            def items = async {
                for (i in 1..1000) {
                    yield return i
                }
            }
            def results = []
            for (item in items) {
                if (item > 3) break
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testGeneratorSingleElement() {
        assertScript '''
            def items = async {
                yield return 'only'
            }
            assert items.collect() == ['only']
        '''
    }

    @Test
    void testGeneratorWithConditionalYield() {
        assertScript '''
            def items = async {
                for (i in 1..10) {
                    if (i % 2 == 0) {
                        yield return i
                    }
                }
            }
            assert items.collect() == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testGeneratorYieldsNull() {
        assertScript '''
            def items = async {
                yield return null
                yield return 'after-null'
            }
            def results = items.collect()
            assert results.size() == 2
            assert results[0] == null
            assert results[1] == 'after-null'
        '''
    }

    @Test
    void testAwaitResultMapSuccess() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.success('hello')
            def mapped = r.map { it.toUpperCase() }
            assert mapped.success
            assert mapped.value == 'HELLO'
        '''
    }

    @Test
    void testAwaitResultMapFailure() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.failure(new RuntimeException('err'))
            def mapped = r.map { it.toString() }
            assert mapped.failure
            assert mapped.error.message == 'err'
        '''
    }

    @Test
    void testAwaitResultGetOrElse() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def success = AwaitResult.success(42)
            assert success.getOrElse { -1 } == 42

            def failure = AwaitResult.failure(new RuntimeException('err'))
            assert failure.getOrElse { err -> -1 } == -1
        '''
    }

    @Test
    void testAwaitResultGetValueOnFailureThrows() {
        shouldFail '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.failure(new RuntimeException('err'))
            r.getValue()
        '''
    }

    @Test
    void testAwaitResultGetErrorOnSuccessThrows() {
        shouldFail '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.success(42)
            r.getError()
        '''
    }

    @Test
    void testAwaitResultEquality() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def a = AwaitResult.success(42)
            def b = AwaitResult.success(42)
            def c = AwaitResult.success(99)
            assert a == b
            assert a != c
            assert a.hashCode() == b.hashCode()

            def err = new RuntimeException('err')
            def d = AwaitResult.failure(err)
            def e = AwaitResult.failure(err)
            assert d == e
            assert d != a
        '''
    }

    @Test
    void testAwaitResultToString() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success(42)
            assert s.toString() == 'AwaitResult.Success[42]'

            def f = AwaitResult.failure(new RuntimeException('oops'))
            assert f.toString().contains('AwaitResult.Failure[')
            assert f.toString().contains('oops')
        '''
    }

    @Test
    void testAwaitResultIsSuccessIsFailure() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def s = AwaitResult.success('ok')
            assert s.isSuccess()
            assert !s.isFailure()

            def f = AwaitResult.failure(new RuntimeException('err'))
            assert f.isFailure()
            assert !f.isSuccess()
        '''
    }

    @Test
    void testForAwaitWithBreakClosesGenerator() {
        assertScript '''
            def items = async {
                yield return 1
                yield return 2
                yield return 3
                yield return 4
                yield return 5
            }
            def results = []
            for await (item in items) {
                if (item > 2) break
                results << item
            }
            assert results == [1, 2]
        '''
    }

    @Test
    void testForAwaitWithContinue() {
        assertScript '''
            def results = []
            for await (item in [1, 2, 3, 4, 5]) {
                if (item % 2 == 0) continue
                results << item
            }
            assert results == [1, 3, 5]
        '''
    }

    @Test
    void testForAwaitNested() {
        assertScript '''
            def outer = async {
                yield return [1, 2]
                yield return [3, 4]
            }
            def results = []
            for await (list in outer) {
                for await (item in list) {
                    results << item
                }
            }
            assert results == [1, 2, 3, 4]
        '''
    }

    @Test
    void testForAwaitWithAwaitInsideBody() {
        assertScript '''
            def items = [1, 2, 3]
            def results = []
            for await (item in items) {
                def doubled = await async { item * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testForAwaitWithColonSyntax() {
        assertScript '''
            def results = []
            for await (item : [10, 20, 30]) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testGoWithComputation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = Awaitable.go { (1..10).sum() }
            assert await(task) == 55
        '''
    }

    @Test
    void testAsyncPipelinePattern() {
        assertScript '''
            def fetch = async { [1, 2, 3, 4, 5] }
            def transform = async { (await fetch).collect { it * 10 } }
            def filter = async { (await transform).findAll { it > 20 } }
            assert await(filter) == [30, 40, 50]
        '''
    }

    @Test
    void testConcurrentTasksInterleaving() {
        assertScript '''
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()
            def t1 = async { log << 'start-1'; Thread.sleep(50); log << 'end-1'; 'r1' }
            def t2 = async { log << 'start-2'; Thread.sleep(50); log << 'end-2'; 'r2' }
            assert await(t1) == 'r1'
            assert await(t2) == 'r2'
            assert log.containsAll(['start-1', 'start-2', 'end-1', 'end-2'])
        '''
    }

    @Test
    void testAllSettledPreservesOrder() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def t1 = async { Thread.sleep(50); 'first' }
            def t2 = async { 'second' }
            def t3 = async { throw new RuntimeException('third-err') }
            def results = await Awaitable.allSettled(t1, t2, t3)
            assert results.size() == 3
            assert results[0].success && results[0].value == 'first'
            assert results[1].success && results[1].value == 'second'
            assert !results[2].success && results[2].error.message == 'third-err'
        '''
    }

    @Test
    void testAllWithEmptySources() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await Awaitable.all()
            assert results == []
        '''
    }

    @Test
    void testAllSettledWithEmptySources() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await Awaitable.allSettled()
            assert results == []
        '''
    }

    @Test
    void testWithScopeNestedDeep() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def result = AsyncScope.withScope { outerScope ->
                def outer = outerScope.async { 'outer' }
                def innerResult = AsyncScope.withScope { innerScope ->
                    def inner = innerScope.async { 'inner' }
                    AsyncScope.withScope { deepScope ->
                        def deep = deepScope.async { 'deep' }
                        [await(inner), await(deep)]
                    }
                }
                [await(outer)] + innerResult
            }
            assert result == ['outer', 'inner', 'deep']
        '''
    }

    @Test
    void testScopeFailFastPropagatesOriginalException() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            try {
                AsyncScope.withScope { scope ->
                    scope.async { Thread.sleep(1000); 'slow' }
                    scope.async { throw new IllegalStateException('specific-error') }
                }
                assert false : 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message == 'specific-error'
            }
        '''
    }

    @Test
    void testAllSettledSuccessPropertyAccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def ok = async { 42 }
            def fail = async { throw new RuntimeException('boom') }
            def results = await Awaitable.allSettled(ok, fail)

            assert results[0].success == true
            assert results[0].value == 42
            assert results[1].success == false
            assert results[1].error.message == 'boom'

            assert results[0].isSuccess()
            assert !results[0].isFailure()
            assert results[1].isFailure()
            assert !results[1].isSuccess()
        '''
    }

    @Test
    void testAwaitableFromUnsupportedTypeThrows() {
        shouldFail '''
            import groovy.concurrent.Awaitable

            Awaitable.from("just a string")
        '''
    }

    @Test
    void testChannelWithRegularForLoop() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(3)
            async {
                await ch.send(10)
                await ch.send(20)
                await ch.send(30)
                ch.close()
            }
            Thread.sleep(100)
            def results = []
            for (item in ch) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testGeneratorToList() {
        assertScript '''
            def fib = async {
                int a = 0, b = 1
                for (i in 0..<8) {
                    yield return a
                    int temp = a + b
                    a = b
                    b = temp
                }
            }
            assert fib.collect() == [0, 1, 1, 2, 3, 5, 8, 13]
        '''
    }

    @Test
    void testAwaitOnVariousTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable

            assert await(Awaitable.of(42)) == 42
            assert await(Awaitable.of('str')) == 'str'
            assert await(Awaitable.of([1,2,3])) == [1,2,3]
            assert await(Awaitable.of(null)) == null
            assert await(Awaitable.of(true)) == true
        '''
    }

    @Test
    void testAsyncClosureCapturesOuterVariables() {
        assertScript '''
            def x = 10
            def y = 20
            def task = async { x + y }
            assert await(task) == 30
        '''
    }

    @Test
    void testAsyncClosureCapturesMutableVariable() {
        assertScript '''
            def list = [1, 2, 3]
            def task = async { list << 4; list }
            def result = await task
            assert result == [1, 2, 3, 4]
        '''
    }

    @Test
    void testAsyncInsideClassMethod() {
        assertScript '''
            class Calculator {
                def asyncAdd(a, b) {
                    return async { a + b }
                }
            }
            def calc = new Calculator()
            def task = calc.asyncAdd(3, 4)
            assert await(task) == 7
        '''
    }

    @Test
    void testAsyncInsideClassWithState() {
        assertScript '''
            class Counter {
                private int count = 0
                def asyncIncrement() {
                    return async {
                        synchronized(this) { count++ }
                        count
                    }
                }
            }
            def c = new Counter()
            def tasks = (1..5).collect { c.asyncIncrement() }
            tasks.each { await it }
            assert c.@count == 5
        '''
    }

    @Test
    void testChannelClosedExceptionWithCause() {
        assertScript '''
            import groovy.concurrent.ChannelClosedException

            def cause = new IOException('underlying')
            def e = new ChannelClosedException('channel closed', cause)
            assert e.message == 'channel closed'
            assert e.cause instanceof IOException
            assert e.cause.message == 'underlying'
            assert e instanceof IllegalStateException
        '''
    }

    @Test
    void testPromiseToStringPending() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = new CompletableFuture()
            def p = new GroovyPromise(cf)
            def str = p.toString()
            assert str.contains('pending')
            cf.complete('done')
        '''
    }

    @Test
    void testPromiseToStringCompleted() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture(42)
            def p = new GroovyPromise(cf)
            def str = p.toString()
            assert str.contains('completed') || str.contains('42')
        '''
    }

    @Test
    void testPromiseToStringFailed() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = new CompletableFuture()
            cf.completeExceptionally(new RuntimeException('fail'))
            def p = new GroovyPromise(cf)
            def str = p.toString()
            assert str.contains('failed') || str.contains('fail')
        '''
    }

    @Test
    void testAsyncReturnsList() {
        assertScript '''
            def task = async { [1, 2, 3] }
            assert await(task) == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncReturnsMap() {
        assertScript '''
            def task = async { [a: 1, b: 2] }
            assert await(task) == [a: 1, b: 2]
        '''
    }

    @Test
    void testMultiArgAwaitAllWithThreeArgs() {
        assertScript '''
            def a = async { 1 }
            def b = async { 2 }
            def c = async { 3 }
            def results = await(a, b, c)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAnyReturnsFirstComplete() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fast = Awaitable.of('fast')
            def slow = async { Thread.sleep(1000); 'slow' }
            def result = await Awaitable.any(fast, slow)
            assert result == 'fast'
        '''
    }

    @Test
    void testFirstReturnsFirstComplete() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fast = Awaitable.of('fast')
            def slow = async { Thread.sleep(1000); 'slow' }
            def result = await Awaitable.first(fast, slow)
            assert result == 'fast'
        '''
    }

    @Test
    void testAwaitableFromAwaitableReturnsSame() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def original = Awaitable.of(42)
            def same = Awaitable.from(original)
            assert same.is(original)
        '''
    }

    @Test
    void testAsyncThrowsMultipleExceptionTypes() {
        assertScript '''
            def task1 = async { throw new IllegalArgumentException('bad arg') }
            try {
                await task1
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message == 'bad arg'
            }

            def task2 = async { throw new UnsupportedOperationException('nope') }
            try {
                await task2
                assert false
            } catch (UnsupportedOperationException e) {
                assert e.message == 'nope'
            }
        '''
    }

    @Test
    void testChannelSendAndReceiveOrdering() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            (1..5).each { await ch.send(it) }
            def results = (1..5).collect { await ch.receive() }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testChannelForAwait() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(3)
            async {
                await ch.send('x')
                await ch.send('y')
                await ch.send('z')
                ch.close()
            }
            Thread.sleep(100)
            def results = []
            for await (item in ch) {
                results << item
            }
            assert results == ['x', 'y', 'z']
        '''
    }

    @Test
    void testDeferBodyExceptionPlusCleanup() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'cleanup' }
                log << 'body-start'
                throw new RuntimeException('body-fail')
            }
            try {
                await task
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'body-fail'
            }
            assert log.contains('body-start')
            assert log.contains('cleanup')
        '''
    }

    @Test
    void testScopeWithSupplierReturnsResult() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def result = AsyncScope.withScope { scope ->
                def t = scope.async { 42 }
                await t
            }
            assert result == 42
        '''
    }

    @Test
    void testYieldReturnInNestedLoops() {
        assertScript '''
            def items = async {
                for (i in 1..3) {
                    for (j in 1..2) {
                        yield return i * 10 + j
                    }
                }
            }
            assert items.collect() == [11, 12, 21, 22, 31, 32]
        '''
    }

    @Test
    void testAwaitableDelayMillis() {
        assertScript '''
            import groovy.concurrent.Awaitable

            long start = System.currentTimeMillis()
            await Awaitable.delay(50)
            long elapsed = System.currentTimeMillis() - start
            assert elapsed >= 40
        '''
    }

    @Test
    void testChannelIteratorAfterClose() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(3)
            await ch.send('a')
            await ch.send('b')
            ch.close()
            def iter = ch.iterator()
            assert iter.hasNext()
            assert iter.next() == 'a'
            assert iter.hasNext()
            assert iter.next() == 'b'
            assert !iter.hasNext()
        '''
    }

    @Test
    void testAsyncWithClosureReturningVoid() {
        assertScript '''
            def sideEffect = false
            def task = async {
                sideEffect = true
            }
            await task
            assert sideEffect == true
        '''
    }

    @Test
    void testAwaitableAllSingleSource() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await Awaitable.all(async { 42 })
            assert results == [42]
        '''
    }

    @Test
    void testAwaitableAnySingleSource() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = await Awaitable.any(async { 'only' })
            assert result == 'only'
        '''
    }

    @Test
    void testAwaitableFirstSingleSource() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def result = await Awaitable.first(async { 'only' })
            assert result == 'only'
        '''
    }

    @Test
    void testForAwaitWithEmptyList() {
        assertScript '''
            def results = []
            for await (item in []) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testScopeLaunchMultipleChildren() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def result = AsyncScope.withScope { scope ->
                def tasks = (1..10).collect { n ->
                    scope.async { n * n }
                }
                tasks.collect { await it }
            }
            assert result == [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
        '''
    }

    @Test
    void testAllSettledAllSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await Awaitable.allSettled(
                async { 1 }, async { 2 }, async { 3 })
            assert results.every { it.success }
            assert results.collect { it.value } == [1, 2, 3]
        '''
    }

    @Test
    void testAllSettledAllFailure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def results = await Awaitable.allSettled(
                async { throw new RuntimeException('e1') },
                async { throw new RuntimeException('e2') })
            assert results.every { it.failure }
            assert results.collect { it.error.message } == ['e1', 'e2']
        '''
    }

    @Test
    void testCloseIterableWithCloseable() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import java.io.Closeable

            def closed = false
            def closeable = new Closeable() {
                void close() { closed = true }
            }
            AsyncSupport.closeIterable(closeable)
            assert closed
        '''
    }

    @Test
    void testCloseIterableWithAutoCloseable() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def closed = false
            def autoCloseable = new AutoCloseable() {
                void close() { closed = true }
            }
            AsyncSupport.closeIterable(autoCloseable)
            assert closed
        '''
    }

    @Test
    void testCloseIterableWithNonCloseable() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            AsyncSupport.closeIterable("not-closeable")
            assert true
        '''
    }

    @Test
    void testCloseIterableWithNull() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            AsyncSupport.closeIterable(null)
            assert true
        '''
    }

    @Test
    void testToBlockingIterableNull() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def result = AsyncSupport.toBlockingIterable(null)
            assert result.collect() == []
        '''
    }

    @Test
    void testToBlockingIterableIterator() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def iter = [1, 2, 3].iterator()
            def result = AsyncSupport.toBlockingIterable(iter)
            assert result.collect() == [1, 2, 3]
        '''
    }

    @Test
    void testToBlockingIterableArray() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def arr = [10, 20, 30] as Object[]
            def result = AsyncSupport.toBlockingIterable(arr)
            assert result.collect() == [10, 20, 30]
        '''
    }

    @Test
    void testGeneratorBridgeCloseViaEarlyBreak() {
        assertScript '''
            def gen = async {
                for (i in 1..1000000) {
                    yield return i
                }
            }
            def results = []
            for (item in gen) {
                results << item
                if (item >= 2) break
            }
            assert results == [1, 2]
        '''
    }

    @Test
    void testGeneratorExceptionPropagation() {
        assertScript '''
            def gen = async {
                yield return 1
                throw new IllegalStateException('gen-error')
            }
            def results = []
            try {
                for (item in gen) {
                    results << item
                }
                assert false : 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message == 'gen-error'
            }
            assert results == [1]
        '''
    }

    @Test
    void testPromiseGetWithTimeoutSuccess() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit

            def cf = CompletableFuture.completedFuture('fast')
            def p = new GroovyPromise(cf)
            assert p.get(1, TimeUnit.SECONDS) == 'fast'
        '''
    }

    @Test
    void testPromiseGetWithTimeoutTimesOut() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            def cf = new CompletableFuture()
            def p = new GroovyPromise(cf)
            try {
                p.get(50, TimeUnit.MILLISECONDS)
                assert false : 'should have timed out'
            } catch (TimeoutException e) {
                assert true
            }
            cf.complete('done')
        '''
    }

    @Test
    void testPromiseGetWithTimeoutFailure() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.ExecutionException

            def cf = new CompletableFuture()
            cf.completeExceptionally(new IOException('io-fail'))
            def p = new GroovyPromise(cf)
            try {
                p.get(1, TimeUnit.SECONDS)
                assert false
            } catch (ExecutionException e) {
                assert e.cause instanceof IOException
                assert e.cause.message == 'io-fail'
            }
        '''
    }

    @Test
    void testPromiseCancelInterrupts() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CancellationException

            def cf = new CompletableFuture()
            def p = new GroovyPromise(cf)
            assert !p.isCancelled()
            p.cancel()
            assert p.isCancelled()
            assert p.isDone()
            try {
                p.get()
                assert false
            } catch (CancellationException e) {
                assert true
            }
        '''
    }

    @Test
    void testPromiseGetCompleted() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture('val')
            def p = new GroovyPromise(cf)
            assert p.getCompleted() == 'val'
        '''
    }

    @Test
    void testPromiseGetCompletedFastPath() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture('val')
            def p = new GroovyPromise(cf)
            assert p.get() == 'val'
        '''
    }

    @Test
    void testPromiseGetCompletedExceptionally() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.ExecutionException

            def cf = new CompletableFuture()
            cf.completeExceptionally(new RuntimeException('err'))
            def p = new GroovyPromise(cf)
            try {
                p.getCompleted()
                assert false
            } catch (ExecutionException e) {
                assert e.cause.message == 'err'
            }
        '''
    }

    @Test
    void testScopeToString() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            def str = scope.toString()
            assert str.contains('AsyncScope') || str.contains('DefaultAsyncScope')
            scope.close()
        '''
    }

    @Test
    void testScopeCurrentReturnsScopeInsideWithScope() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                def current = AsyncScope.current()
                assert current != null
            }
        '''
    }

    @Test
    void testScopeCurrentReturnsNullOutsideScope() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def current = AsyncScope.current()
            assert current == null
        '''
    }

    @Test
    void testAdapterRegistryRegisterAndUnregister() {
        assertScript '''
            import groovy.concurrent.AwaitableAdapterRegistry
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.Awaitable

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class type) { type == StringBuilder }
                Awaitable toAwaitable(Object source) {
                    Awaitable.of(source.toString())
                }
            }
            AwaitableAdapterRegistry.register(adapter)
            def result = Awaitable.from(new StringBuilder('test'))
            assert result.get() == 'test'
            AwaitableAdapterRegistry.unregister(adapter)
            try {
                Awaitable.from(new StringBuilder('test'))
                assert false : 'should fail after unregister'
            } catch (IllegalArgumentException e) {
                assert true
            }
        '''
    }

    @Test
    void testScopeWithCurrentNested() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def results = []
            AsyncScope.withScope { outer ->
                results << (AsyncScope.current() != null)
                AsyncScope.withScope { inner ->
                    results << (AsyncScope.current() != null)
                }
                results << (AsyncScope.current() != null)
            }
            assert results == [true, true, true]
        '''
    }

    @Test
    void testDeferExceptionSupressesByBodyException() {
        assertScript '''
            def task = async {
                defer { throw new RuntimeException('defer-err') }
                throw new RuntimeException('body-err')
            }
            try {
                await task
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'body-err' || e.message == 'defer-err'
            }
        '''
    }

    @Test
    void testAwaitCancelledFutureThrowsCancellationException() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CancellationException

            def cf = new CompletableFuture()
            cf.cancel(true)
            try {
                await cf
                assert false
            } catch (CancellationException e) {
                assert true
            }
        '''
    }

    @Test
    void testScopePruneCompleted() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                def t1 = scope.async { 'fast' }
                await t1
                Thread.sleep(50)
                def t2 = scope.async { 'fast2' }
                await t2
            }
        '''
    }

    @Test
    void testChannelDrainBufferToReceivers() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(0)
            def receiver = async { await ch.receive() }
            Thread.sleep(50)
            await ch.send('message')
            assert await(receiver) == 'message'
        '''
    }

    @Test
    void testChannelSendOnClosedThrows() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def ch = AsyncChannel.create(1)
            ch.close()
            try {
                await ch.send('data')
                assert false
            } catch (ChannelClosedException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitResultHashCodeConsistency() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def a = AwaitResult.success(null)
            def b = AwaitResult.success(null)
            assert a == b
            assert a.hashCode() == b.hashCode()

            def err = new RuntimeException('x')
            def c = AwaitResult.failure(err)
            def d = AwaitResult.failure(err)
            assert c == d
            assert c.hashCode() == d.hashCode()
        '''
    }

    @Test
    void testAwaitResultMapFunctionThrows() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.success(42)
            try {
                r.map { throw new RuntimeException('map-err') }
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'map-err'
            }
        '''
    }

    @Test
    void testAwaitResultGetOrElseOnSuccess() {
        assertScript '''
            import groovy.concurrent.AwaitResult

            def r = AwaitResult.success('val')
            assert r.getOrElse { 'default' } == 'val'
        '''
    }

    @Test
    void testAwaitableFromBuiltInAdapterWithFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture('cf-val')
            def a = Awaitable.from((Object) cf)
            assert a.get() == 'cf-val'
        '''
    }

    @Test
    void testAwaitableOrTimeoutMillisStatic() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeoutException

            def slow = async { Thread.sleep(1000); 'done' }
            try {
                await Awaitable.orTimeoutMillis(slow, 50)
                assert false
            } catch (TimeoutException e) {
                assert true
            }
        '''
    }

    @Test
    void testAwaitableCompleteOnTimeoutMillisStatic() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def slow = async { Thread.sleep(1000); 'done' }
            def result = await Awaitable.completeOnTimeoutMillis(slow, 'fb', 50)
            assert result == 'fb'
        '''
    }

    @Test
    void testAwaitableGetExecutorStatic() {
        assertScript '''
            import groovy.concurrent.Awaitable

            assert Awaitable.getExecutor() != null
        '''
    }

    @Test
    void testAwaitableSetExecutorStatic() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors

            def original = Awaitable.getExecutor()
            def custom = Executors.newSingleThreadExecutor()
            try {
                Awaitable.setExecutor(custom)
                assert Awaitable.getExecutor().is(custom)
            } finally {
                Awaitable.setExecutor(original)
                custom.shutdown()
            }
        '''
    }

    @Test
    void testAwaitableIsVirtualThreadsAvailableStatic() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def available = Awaitable.isVirtualThreadsAvailable()
            assert available instanceof Boolean
        '''
    }

    @Test
    void testPromiseUnwrapCancellation() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CancellationException

            def cf = new CompletableFuture()
            cf.cancel(true)
            def p = new GroovyPromise(cf)
            assert p.isCancelled()
            try {
                p.get()
                assert false
            } catch (CancellationException e) {
                assert true
            }
        '''
    }

    @Test
    void testScopeWithCustomExecutorAndFailFast() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.Executors

            def exec = Executors.newFixedThreadPool(2)
            try {
                def scope = AsyncScope.create(exec, true)
                def t = scope.async { 'result' }
                assert await(t) == 'result'
                scope.close()
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testForAwaitWithIterableSource() {
        assertScript '''
            def results = []
            for await (item in [10, 20, 30]) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testAllAsyncWithMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def a = async { 'a' }
            def b = CompletableFuture.completedFuture('b')
            def c = Awaitable.of('c')
            def results = await Awaitable.all(a, b, c)
            assert results == ['a', 'b', 'c']
        '''
    }

    @Test
    void testFirstAsyncReturnsFirstSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fail = async { throw new RuntimeException('err') }
            def success = Awaitable.of('ok')
            def result = await Awaitable.first(fail, success)
            assert result == 'ok'
        '''
    }

    @Test
    void testChannelUnbufferedSendReceive() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(0)
            def sender = async { await ch.send('msg'); 'sent' }
            Thread.sleep(50)
            def received = await ch.receive()
            assert received == 'msg'
            assert await(sender) == 'sent'
        '''
    }

    @Test
    void testDefaultAsyncChannelRemovePendingSender() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def ch = AsyncChannel.create(0)
            def sender = async {
                try { await ch.send('data') }
                catch (ChannelClosedException e) { 'cancelled' }
            }
            Thread.sleep(50)
            ch.close()
            def result = await sender
            assert result == 'cancelled'
        '''
    }

    @Test
    void testAnyWithOneFailureReturnsSuccess() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def t1 = CompletableFuture.completedFuture('fast')
            def t2 = CompletableFuture.failedFuture(new RuntimeException('any-fail'))
            def result = await Awaitable.any(t1, t2)
            assert result != null
        '''
    }

    @Test
    void testGeneratorBreakClosesIterator() {
        assertScript '''
            def collected = []
            def gen = async {
                yield return 1
                yield return 2
                yield return 3
                yield return 4
                yield return 5
            }
            for await (x in gen) {
                collected << x
                if (x == 2) break
            }
            assert collected == [1, 2]
        '''
    }

    @Test
    void testGeneratorBreakFromInfinite() {
        assertScript '''
            def n = 0
            def gen = async {
                def i = 0
                while (true) {
                    yield return i++
                }
            }
            for await (x in gen) {
                n = x
                if (x >= 5) break
            }
            assert n == 5
        '''
    }

    @Test
    void testAwaitPlainFutureWithExceptionUnwrap() {
        assertScript '''
            import java.util.concurrent.*

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future f = exec.submit({
                    throw new RuntimeException('future-err')
                } as Callable)
                try {
                    await(f)
                    assert false
                } catch (RuntimeException e) {
                    assert e.message == 'future-err'
                }
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testBuiltInAdapterPlainFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.*

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future f = exec.submit({ 'adapted' } as Callable)
                def awaitable = Awaitable.from(f)
                assert await(awaitable) == 'adapted'
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testBuiltInAdapterPlainFutureWithError() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.*

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future f = exec.submit({
                    throw new RuntimeException('adapt-err')
                } as Callable)
                def awaitable = Awaitable.from(f)
                try {
                    await(awaitable)
                    assert false
                } catch (RuntimeException e) {
                    assert e.message == 'adapt-err'
                }
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testScopeCurrentWithThreadLocal() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            def captured = AsyncScope.withCurrent(scope, { AsyncScope.current() })
            assert captured.is(scope)
            scope.close()
        '''
    }

    @Test
    void testScopeWithCurrentNestedRestore() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope1 = AsyncScope.create()
            def scope2 = AsyncScope.create()
            def innerScope = null
            def outerScope = null

            AsyncScope.withCurrent(scope1, {
                outerScope = AsyncScope.current()
                AsyncScope.withCurrent(scope2, {
                    innerScope = AsyncScope.current()
                })
            })

            assert outerScope.is(scope1)
            assert innerScope.is(scope2)
            scope1.close()
            scope2.close()
        '''
    }

    @Test
    void testScopeCloseJoinsChildren() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CopyOnWriteArrayList

            def results = new CopyOnWriteArrayList()
            def scope = AsyncScope.create()
            scope.async { Thread.sleep(50); results << 'child1' }
            scope.async { Thread.sleep(50); results << 'child2' }
            scope.close()
            assert results.size() == 2
            assert 'child1' in results
            assert 'child2' in results
        '''
    }

    @Test
    void testScopeAsyncAfterCloseThrows() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.close()
            try {
                scope.async { 42 }
                assert false
            } catch (IllegalStateException e) {
                assert e.message.contains('closed')
            }
        '''
    }

    @Test
    void testDeferNullActionThrows() {
        shouldFail(IllegalArgumentException, '''
            import org.apache.groovy.runtime.async.AsyncSupport
            def scope = AsyncSupport.createDeferScope()
            AsyncSupport.defer(scope, null)
        ''')
    }

    @Test
    void testDeferNullScopeThrows() {
        shouldFail(IllegalStateException, '''
            import org.apache.groovy.runtime.async.AsyncSupport
            AsyncSupport.defer(null, { println 'x' })
        ''')
    }

    @Test
    void testDeferWithAwaitableResult() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import java.util.concurrent.CompletableFuture

            def actions = []
            def scope = AsyncSupport.createDeferScope()
            AsyncSupport.defer(scope, {
                actions << 'deferred'
                CompletableFuture.completedFuture('done')
            })
            AsyncSupport.executeDeferScope(scope)
            assert actions == ['deferred']
        '''
    }

    @Test
    void testDeferMultipleActionsThrow() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def scope = AsyncSupport.createDeferScope()
            AsyncSupport.defer(scope, { throw new RuntimeException('d1') })
            AsyncSupport.defer(scope, { throw new RuntimeException('d2') })
            try {
                AsyncSupport.executeDeferScope(scope)
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'd2'
                assert e.suppressed.length == 1
                assert e.suppressed[0].message == 'd1'
            }
        '''
    }

    @Test
    void testExecuteDeferScopeNullIsNoop() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            AsyncSupport.executeDeferScope(null)
        '''
    }

    @Test
    void testExecuteDeferScopeEmptyIsNoop() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def scope = AsyncSupport.createDeferScope()
            AsyncSupport.executeDeferScope(scope)
        '''
    }

    @Test
    void testWrapAsyncDirect() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def wrapped = AsyncSupport.wrapAsync({ 'hello-wrap' })
            def awaitable = wrapped()
            assert await(awaitable) == 'hello-wrap'
        '''
    }

    @Test
    void testWrapAsyncWithArgs() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def wrapped = AsyncSupport.wrapAsync({ Object[] args -> args[0] + args[1] })
            def awaitable = wrapped(10, 20)
            assert await(awaitable) == 30
        '''
    }

    @Test
    void testWrapAsyncWithException() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport

            def wrapped = AsyncSupport.wrapAsync({ throw new RuntimeException('wrap-err') })
            def awaitable = wrapped()
            try {
                await(awaitable)
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'wrap-err'
            }
        '''
    }

    @Test
    void testWrapAsyncGeneratorDirect() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import org.apache.groovy.runtime.async.GeneratorBridge

            def wrapped = AsyncSupport.wrapAsyncGenerator({ GeneratorBridge bridge ->
                bridge.yield(10)
                bridge.yield(20)
                bridge.yield(30)
            })
            def iterable = wrapped()
            def items = []
            for (item in iterable) {
                items << item
            }
            assert items == [10, 20, 30]
        '''
    }

    @Test
    void testWrapAsyncGeneratorWithBreak() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import org.apache.groovy.runtime.async.GeneratorBridge

            def wrapped = AsyncSupport.wrapAsyncGenerator({ GeneratorBridge bridge ->
                bridge.yield('a')
                bridge.yield('b')
                bridge.yield('c')
            })
            def iterable = wrapped()
            def items = []
            for (item in iterable) {
                items << item
                if (item == 'b') break
            }
            assert items == ['a', 'b']
        '''
    }

    @Test
    void testWrapAsyncGeneratorWithException() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import org.apache.groovy.runtime.async.GeneratorBridge

            def wrapped = AsyncSupport.wrapAsyncGenerator({ GeneratorBridge bridge ->
                bridge.yield(1)
                throw new RuntimeException('gen-err')
            })
            def iterable = wrapped()
            def iter = iterable.iterator()
            assert iter.hasNext()
            assert iter.next() == 1
            try {
                iter.hasNext()
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'gen-err'
            }
        '''
    }

    @Test
    void testWrapAsyncNullThrows() {
        shouldFail(NullPointerException, '''
            import org.apache.groovy.runtime.async.AsyncSupport
            AsyncSupport.wrapAsync(null)
        ''')
    }

    @Test
    void testWrapAsyncGeneratorNullThrows() {
        shouldFail(NullPointerException, '''
            import org.apache.groovy.runtime.async.AsyncSupport
            AsyncSupport.wrapAsyncGenerator(null)
        ''')
    }

    @Test
    void testAdapterRegistryToBlockingIterableWithAdapter() {
        assertScript '''
            import groovy.concurrent.AwaitableAdapterRegistry
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.Awaitable

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class type) { false }
                Awaitable toAwaitable(Object source) { null }
                boolean supportsIterable(Class type) { List.isAssignableFrom(type) }
                Iterable toBlockingIterable(Object source) { (Iterable) source }
            }
            AwaitableAdapterRegistry.register(adapter)
            try {
                def iterable = AwaitableAdapterRegistry.toBlockingIterable([1, 2, 3])
                def items = []
                for (item in iterable) {
                    items << item
                }
                assert items == [1, 2, 3]
            } finally {
                AwaitableAdapterRegistry.unregister(adapter)
            }
        '''
    }

    @Test
    void testRethrowUnwrappedDeepNesting() {
        assertScript '''
            import java.util.concurrent.*

            def cf = new CompletableFuture()
            def deep = new CompletionException(
                new ExecutionException(
                    new RuntimeException('deep-cause')
                )
            )
            cf.completeExceptionally(deep)
            try {
                await(cf)
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'deep-cause'
            }
        '''
    }

    @Test
    void testWrapForFuturePreservesCompletionException() {
        assertScript '''
            import java.util.concurrent.CompletionException

            def result = async {
                throw new CompletionException(
                    new RuntimeException('already-wrapped')
                )
            }
            try {
                await(result)
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'already-wrapped'
            }
        '''
    }

    @Test
    void testScopeFailFastCancelsOnFirstFailure() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            def scope = AsyncScope.create(Awaitable.getExecutor(), true)
            scope.async { throw new RuntimeException('early-fail') }
            scope.async { Thread.sleep(2000); 'late' }
            try {
                scope.close()
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'early-fail'
            }
        '''
    }

    @Test
    void testGeneratorNextWithoutHasNext() {
        assertScript '''
            def gen = async {
                yield return 'only'
            }
            def iter = gen.iterator()
            assert iter.next() == 'only'
            assert !iter.hasNext()
        '''
    }

    @Test
    void testGeneratorNoSuchElementAfterExhausted() {
        assertScript '''
            def gen = async {
                yield return 'one'
            }
            def iter = gen.iterator()
            iter.next()
            try {
                iter.next()
                assert false
            } catch (NoSuchElementException e) {
                assert true
            }
        '''
    }

    @Test
    void testScopePruneCompletedTriggered() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            100.times { i ->
                scope.async { i }
            }
            scope.close()
        '''
    }

    @Test
    void testAwaitAwaitableExecutionException() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitResult

            def a = Awaitable.failed(new RuntimeException('exec-err'))
            try {
                await(a)
                assert false
            } catch (RuntimeException e) {
                assert e.message == 'exec-err'
            }
        '''
    }

    @Test
    void testGeneratorBridgeDirectClose() {
        assertScript '''
            import org.apache.groovy.runtime.async.GeneratorBridge

            def bridge = new GeneratorBridge()
            Thread.start {
                try {
                    bridge.yield(1)
                    bridge.yield(2)
                    bridge.yield(3)
                    bridge.complete()
                } catch (e) {}
            }
            assert bridge.hasNext()
            assert bridge.next() == 1
            bridge.close()
            assert !bridge.hasNext()
        '''
    }

    @Test
    void testGeneratorBridgeCloseWithProducerBlocked() {
        assertScript '''
            import org.apache.groovy.runtime.async.GeneratorBridge
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def bridge = new GeneratorBridge()
            def producerDone = new CountDownLatch(1)
            Thread.start {
                try {
                    bridge.yield('a')
                    bridge.yield('b')
                    bridge.complete()
                } catch (e) {
                } finally {
                    producerDone.countDown()
                }
            }
            assert bridge.hasNext()
            assert bridge.next() == 'a'
            bridge.close()
            producerDone.await(5, TimeUnit.SECONDS)
        '''
    }

    @Test
    void testGeneratorBridgeYieldAfterClose() {
        assertScript '''
            import org.apache.groovy.runtime.async.GeneratorBridge
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.atomic.AtomicReference

            def bridge = new GeneratorBridge()
            def thrown = new AtomicReference()
            def latch = new CountDownLatch(1)
            Thread.start {
                try {
                    bridge.yield(1)
                    bridge.yield(2)
                } catch (e) {
                    thrown.set(e)
                } finally {
                    latch.countDown()
                }
            }
            assert bridge.hasNext()
            assert bridge.next() == 1
            bridge.close()
            latch.await(5, TimeUnit.SECONDS)
        '''
    }

    @Test
    void testGeneratorHasNextInterrupted() {
        assertScript '''
            import org.apache.groovy.runtime.async.GeneratorBridge
            import java.util.concurrent.atomic.AtomicBoolean

            def bridge = new GeneratorBridge()
            def result = new AtomicBoolean(true)
            def t = Thread.start {
                Thread.currentThread().interrupt()
                result.set(bridge.hasNext())
            }
            t.join(1000)
            assert !result.get()
        '''
    }

    @Test
    void testBuiltInAdapterCompletionStageNotCF() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.*

            CompletionStage stage = CompletableFuture.completedFuture('via-stage')
                .thenApplyAsync { it + '-processed' }
            def awaitable = Awaitable.from(stage)
            assert await(awaitable) == 'via-stage-processed'
        '''
    }

    @Test
    void testBuiltInAdapterPlainFutureNotCF() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.*

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future f = exec.submit({ 'plain-future-val' } as Callable)
                def awaitable = Awaitable.from(f)
                def result = await(awaitable)
                assert result == 'plain-future-val'
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testBuiltInAdapterPlainFutureExecutionException() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.*

            def exec = Executors.newSingleThreadExecutor()
            try {
                Future f = exec.submit({
                    throw new IllegalStateException('plain-future-ex')
                } as Callable)
                def awaitable = Awaitable.from(f)
                try {
                    await(awaitable)
                    assert false
                } catch (IllegalStateException e) {
                    assert e.message == 'plain-future-ex'
                }
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testScopeCloseMultipleErrors() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.async { throw new RuntimeException('scope-err-1') }
            scope.async { throw new RuntimeException('scope-err-2') }
            try {
                scope.close()
                assert false
            } catch (RuntimeException e) {
                assert e.suppressed.length >= 0
            }
        '''
    }

    @Test
    void testScopeCloseIdempotent() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.async { 42 }
            scope.close()
            scope.close()
        '''
    }

    @Test
    void testScopeCloseWithCheckedExceptionWrapped() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.async { throw new Exception('checked-err') }
            try {
                scope.close()
                assert false
            } catch (RuntimeException e) {
                assert e.cause.message == 'checked-err'
            }
        '''
    }

    @Test
    void testScopeCloseWithError() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.async { throw new StackOverflowError('scope-error') }
            try {
                scope.close()
                assert false
            } catch (StackOverflowError e) {
                assert e.message == 'scope-error'
            }
        '''
    }

    @Test
    void testAdapterRegistryToBlockingIterableWithRegisteredAdapter() {
        assertScript """
            import groovy.concurrent.AwaitableAdapterRegistry
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.Awaitable

            class StringIterable implements Iterable<Character> {
                String s
                StringIterable(String s) { this.s = s }
                Iterator<Character> iterator() { s.chars().mapToObj(c -> (Character)(char)c).iterator() }
            }

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class type) { false }
                Awaitable toAwaitable(Object source) { null }
                boolean supportsIterable(Class type) { type.name.contains('StringIterable') }
                Iterable toBlockingIterable(Object source) { ((StringIterable)source) }
            }
            AwaitableAdapterRegistry.register(adapter)
            try {
                def items = []
                def iterable = AwaitableAdapterRegistry.toBlockingIterable(new StringIterable('abc'))
                for (ch in iterable) items << ch
                assert items.size() == 3
            } finally {
                AwaitableAdapterRegistry.unregister(adapter)
            }
        """
    }

    @Test
    void testDeferWithCompletionStageResult() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import java.util.concurrent.CompletableFuture

            def executed = []
            def scope = AsyncSupport.createDeferScope()
            AsyncSupport.defer(scope, {
                executed << 'defer1'
                CompletableFuture.supplyAsync { executed << 'async-done'; 'ok' }
            })
            AsyncSupport.executeDeferScope(scope)
            Thread.sleep(100)
            assert 'defer1' in executed
        '''
    }

    @Test
    void testDeferWithFutureResult() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import java.util.concurrent.*

            def executed = []
            def exec = Executors.newSingleThreadExecutor()
            try {
                def scope = AsyncSupport.createDeferScope()
                AsyncSupport.defer(scope, {
                    executed << 'defer-future'
                    exec.submit({ executed << 'future-done' } as Callable)
                })
                AsyncSupport.executeDeferScope(scope)
                assert 'defer-future' in executed
            } finally {
                exec.shutdown()
            }
        '''
    }

    @Test
    void testFirstWithMixedResults() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def slow = CompletableFuture.supplyAsync { Thread.sleep(200); 'slow' }
            def fast = CompletableFuture.completedFuture('fast')
            def fail = CompletableFuture.failedFuture(new RuntimeException('err'))
            def result = await Awaitable.first(slow, fast, fail)
            assert result == 'fast'
        '''
    }

    @Test
    void testGeneratorBridgeCloseWithNullProducerThread() {
        assertScript '''
            import org.apache.groovy.runtime.async.GeneratorBridge

            def bridge = new GeneratorBridge()
            bridge.close()
            assert !bridge.hasNext()
        '''
    }

}
