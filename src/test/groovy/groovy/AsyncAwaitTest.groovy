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

            def slow = async { Thread.sleep(5000); 'done' }
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

            def slow = async { Thread.sleep(5000); 'done' }
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

            def slowStarted = new java.util.concurrent.CountDownLatch(1)
            def result = null
            try {
                AsyncScope.withScope { scope ->
                    def slow = scope.async {
                        slowStarted.countDown()
                        Thread.sleep(5000)
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
}
