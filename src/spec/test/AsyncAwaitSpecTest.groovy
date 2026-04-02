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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

class AsyncAwaitSpecTest {

    @Test
    void testBasicAsyncAwait() {
        assertScript '''
        // tag::basic_async_await[]
        def task = async { 21 * 2 }
        assert await(task()) == 42
        // end::basic_async_await[]
        '''
    }

    @Test
    void testSequentialWorkflow() {
        assertScript '''
        // tag::sequential_workflow[]
        def fetchUserId = { String token -> 'user-42' }
        def fetchUserName = { String id -> 'Alice' }
        def loadProfile = { String name -> [name: name, level: 10] }

        def task = async {
            var userId  = fetchUserId('token-abc')
            var name    = fetchUserName(userId)
            var profile = loadProfile(name)
            profile
        }
        def profile = await task()
        assert profile.name == 'Alice'
        assert profile.level == 10
        // end::sequential_workflow[]
        '''
    }

    @Test
    void testParallelTasks() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::parallel_tasks[]
        def stats     = async { [hp: 100, mp: 50] }
        def inventory = async { ['sword', 'shield'] }
        def villain   = async { [name: 'Dragon', level: 20] }

        def (s, inv, v) = await Awaitable.all(stats(), inventory(), villain())
        assert s.hp == 100
        assert inv.size() == 2
        assert v.name == 'Dragon'
        // end::parallel_tasks[]
        '''
    }

    @Test
    void testMultiArgAwait() {
        assertScript '''
        // tag::multi_arg_await[]
        def a = async { 1 }
        def b = async { 2 }
        def c = async { 3 }

        // These three forms are equivalent:
        def r1 = await(a(), b(), c())    // parenthesized multi-arg
        assert r1 == [1, 2, 3]
        // end::multi_arg_await[]
        '''
    }

    @Test
    void testRacingTasks() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::racing_tasks[]
        def fast = async { 'fast' }
        def slow = async { Thread.sleep(500); 'slow' }

        def winner = await Awaitable.any(fast(), slow())
        assert winner == 'fast'
        // end::racing_tasks[]
        '''
    }

    @Test
    void testFirstSuccess() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::first_success[]
        def failing  = async { throw new RuntimeException('fail') }
        def succeeding = async { 'ok' }

        // Awaitable.first returns the first *successful* result
        def result = await Awaitable.first(failing(), succeeding())
        assert result == 'ok'
        // end::first_success[]
        '''
    }

    @Test
    void testAllSettled() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::all_settled[]
        def ok   = async { 42 }
        def fail = async { throw new RuntimeException('boom') }

        def results = await Awaitable.allSettled(ok(), fail())
        assert results[0].success && results[0].value == 42
        assert !results[1].success && results[1].error.message == 'boom'
        // end::all_settled[]
        '''
    }

    @Test
    void testExceptionHandling() {
        assertScript '''
        // tag::exception_handling[]
        def task = async {
            throw new IOException('network error')
        }
        try {
            await task()
        } catch (IOException e) {
            // Original exception is rethrown — no wrapper layers
            assert e.message == 'network error'
        }
        // end::exception_handling[]
        '''
    }

    @Test
    void testTimeout() {
        assertScript '''
        import groovy.concurrent.Awaitable
        import java.util.concurrent.TimeoutException

        // tag::timeout[]
        def slow = async { Thread.sleep(5000); 'done' }
        try {
            await Awaitable.orTimeoutMillis(slow(), 100)
        } catch (TimeoutException e) {
            assert true  // timed out as expected
        }
        // end::timeout[]
        '''
    }

    @Test
    void testTimeoutWithFallback() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::timeout_fallback[]
        def slow = async { Thread.sleep(5000); 'done' }
        def result = await Awaitable.completeOnTimeoutMillis(slow(), 'fallback', 100)
        assert result == 'fallback'
        // end::timeout_fallback[]
        '''
    }

    @Test
    void testDelay() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::delay[]
        long start = System.currentTimeMillis()
        await Awaitable.delay(100)
        long elapsed = System.currentTimeMillis() - start
        assert elapsed >= 90
        // end::delay[]
        '''
    }

    @Test
    void testDeferBasic() {
        assertScript '''
        // tag::defer_basic[]
        def log = []
        def task = async {
            defer { log << 'cleanup 1' }
            defer { log << 'cleanup 2' }
            log << 'work'
            'done'
        }
        assert await(task()) == 'done'
        // Deferred actions run in LIFO order
        assert log == ['work', 'cleanup 2', 'cleanup 1']
        // end::defer_basic[]
        '''
    }

    @Test
    void testDeferOnException() {
        assertScript '''
        // tag::defer_exception[]
        def cleaned = false
        def task = async {
            defer { cleaned = true }
            throw new RuntimeException('oops')
        }
        try {
            await task()
        } catch (RuntimeException e) {
            assert e.message == 'oops'
        }
        // Deferred actions still run even when an exception occurs
        assert cleaned
        // end::defer_exception[]
        '''
    }

    @Test
    void testGoSpawn() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::go_spawn[]
        def task = Awaitable.go { 'spawned' }
        assert await(task) == 'spawned'
        // end::go_spawn[]
        '''
    }

    @Test
    void testCompletableFutureInterop() {
        assertScript '''
        import java.util.concurrent.CompletableFuture

        // tag::cf_interop[]
        // await works with CompletableFuture from Java libraries
        def future = CompletableFuture.supplyAsync { 'from Java' }
        assert await(future) == 'from Java'
        // end::cf_interop[]
        '''
    }

    @Test
    void testStructuredConcurrency() {
        assertScript '''
        import groovy.concurrent.AsyncScope

        // tag::structured_concurrency[]
        def result = AsyncScope.withScope { scope ->
            def user   = scope.async { [name: 'Alice'] }
            def orders = scope.async { ['order-1', 'order-2'] }
            [user: await(user), orders: await(orders)]
        }
        // Both tasks guaranteed complete when withScope returns
        assert result.user.name == 'Alice'
        assert result.orders.size() == 2
        // end::structured_concurrency[]
        '''
    }

    @Test
    void testScopeFailFast() {
        assertScript '''
        import groovy.concurrent.AsyncScope

        // tag::scope_fail_fast[]
        try {
            AsyncScope.withScope { scope ->
                scope.async { Thread.sleep(5000); 'slow' }
                scope.async { throw new RuntimeException('fail!') }
            }
        } catch (RuntimeException e) {
            // The first failure cancels siblings and propagates
            assert e.message == 'fail!'
        }
        // end::scope_fail_fast[]
        '''
    }

    @Test
    void testScopeWaitsForAll() {
        assertScript '''
        import groovy.concurrent.AsyncScope
        import java.util.concurrent.atomic.AtomicInteger

        // tag::scope_waits[]
        def counter = new AtomicInteger(0)
        AsyncScope.withScope { scope ->
            3.times { scope.async { Thread.sleep(50); counter.incrementAndGet() } }
        }
        // All children have completed
        assert counter.get() == 3
        // end::scope_waits[]
        '''
    }

    @Test
    void testYieldReturn() {
        assertScript '''
        // tag::yield_return[]
        def fibonacci = async {
            long a = 0, b = 1
            for (i in 1..10) {
                yield return a
                (a, b) = [b, a + b]
            }
        }
        assert fibonacci().collect() == [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
        // end::yield_return[]
        '''
    }

    @Test
    void testGeneratorWithRegularFor() {
        assertScript '''
        // tag::generator_regular_for[]
        def countdown = async {
            for (i in 5..1) {
                yield return i
            }
        }
        // Generators return Iterable — regular for loop works
        def results = []
        for (n in countdown()) {
            results << n
        }
        assert results == [5, 4, 3, 2, 1]

        // Collection methods work too
        assert countdown().collect { it * 10 } == [50, 40, 30, 20, 10]
        // end::generator_regular_for[]
        '''
    }

    @Test
    void testForAwaitWithoutGenerator() {
        assertScript '''
        import java.util.concurrent.CompletableFuture

        // tag::for_await_no_generator[]
        // for await works with any iterable — no generator needed
        def results = []
        for await (item in ['alpha', 'beta', 'gamma']) {
            results << item.toUpperCase()
        }
        assert results == ['ALPHA', 'BETA', 'GAMMA']
        // end::for_await_no_generator[]
        '''
    }

    @Test
    void testForAwait() {
        assertScript '''
        // tag::for_await[]
        def squares = async {
            for (i in 1..5) {
                yield return i * i
            }
        }
        def results = []
        for await (n in squares()) {
            results << n
        }
        assert results == [1, 4, 9, 16, 25]
        // end::for_await[]
        '''
    }
}
