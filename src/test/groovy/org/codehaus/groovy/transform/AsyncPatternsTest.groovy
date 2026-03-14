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

import groovy.concurrent.Awaitable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests covering async/await patterns including CPU-bound work, I/O-bound
 * simulation, caller perspectives, continuations, parallel execution,
 * delay, exception handling, async closures/lambdas, and async method
 * control flow.
 *
 * <p>Await usage patterns:
 * <ul>
 *   <li>Inside {@code @Async} methods: {@code await expr} (keyword syntax)</li>
 *   <li>Inside {@code async &#123; &#125;} closures: {@code await(expr)} with static import</li>
 *   <li>At script top-level: {@code await(expr)} or static import</li>
 * </ul>
 */
class AsyncPatternsTest {

    @AfterEach
    void resetExecutor() {
        Awaitable.setExecutor(null)
    }

    // =========================================================================
    // CPU-Bound async work
    // =========================================================================

    @Test
    void testCpuBoundWithAsyncClosure() {
        assertScript '''
            def calculateComplexOutput(String input) {
                new StringBuilder(input).reverse().toString()
            }

            def calculateResultAsync(String input) {
                def task = async {
                    def compute = async { calculateComplexOutput(input) }
                    def result = await(compute())
                    return result
                }
                return task()
            }

            assert await(calculateResultAsync("Hello, World!")) == "!dlroW ,olleH"
        '''
    }

    @Test
    void testCpuBoundWithAsyncAnnotation() {
        assertScript '''
            class ComputeService {
                private String calculateComplexOutput(String input) {
                    new StringBuilder(input).reverse().toString()
                }

                async calculateResult(String input) {
                    return calculateComplexOutput(input)
                }
            }

            def service = new ComputeService()
            assert await(service.calculateResult("Groovy")) == "yvoorG"
        '''
    }

    // =========================================================================
    // I/O-Bound async simulation
    // =========================================================================

    @Test
    void testIoBoundSimulated() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchDataAsync(String url) {
                def fetch = async {
                    await(Awaitable.delay(50))
                    return "Response from ${url}"
                }
                return fetch()
            }

            def downloadDataAsync() {
                def download = async {
                    def result = await(fetchDataAsync("https://example.com/data"))
                    return result
                }
                return download()
            }

            assert await(downloadDataAsync()) == "Response from https://example.com/data"
        '''
    }

    // =========================================================================
    // Caller perspectives
    // =========================================================================

    @Test
    void testCallerAwaitImmediately() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchDataAsync() {
                def fetch = async {
                    await(Awaitable.delay(50))
                    return "fetched data"
                }
                return fetch()
            }

            assert await(fetchDataAsync()) == "fetched data"
        '''
    }

    @Test
    void testCallerStartThenAwaitLater() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchDataAsync() {
                def fetch = async {
                    await(Awaitable.delay(50))
                    return "fetched data"
                }
                return fetch()
            }

            def caller = async {
                def task = fetchDataAsync()
                def localResult = 1 + 2 + 3
                def downloadResult = await(task)
                return "${downloadResult} (local: ${localResult})"
            }
            def result = await(caller())
            assert result == "fetched data (local: 6)"
        '''
    }

    @Test
    void testCallerReturnsTaskWithoutAwaiting() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchDataAsync() {
                def fetch = async {
                    await(Awaitable.delay(30))
                    return "data"
                }
                return fetch()
            }

            def caller() {
                def task = fetchDataAsync()
                return task
            }

            assert await(caller()) == "data"
        '''
    }

    // =========================================================================
    // Task continuations
    // =========================================================================

    @Test
    void testTaskContinuationWithThen() {
        assertScript '''
            def fetchDataAsync() {
                def fetch = async { "Hello, World!" }
                return fetch()
            }

            def calculateComplexOutput(String input) {
                new StringBuilder(input).reverse().toString()
            }

            def result = await(
                fetchDataAsync().then { data -> calculateComplexOutput(data) }
            )
            assert result == "!dlroW ,olleH"
        '''
    }

    @Test
    void testTaskContinuationChaining() {
        assertScript '''
            def chain = async { 10 }
            def result = await(
                chain()
                    .then { it * 2 }
                    .then { it + 5 }
                    .then { "Result: $it" }
            )
            assert result == "Result: 25"
        '''
    }

    @Test
    void testTaskContinuationWithThenCompose() {
        assertScript '''
            def fetchUser(int id) {
                def user = async { [id: id, name: "User${id}"] }
                return user()
            }

            def fetchOrders(int userId) {
                def orders = async { ["order1", "order2"] }
                return orders()
            }

            def result = await(
                fetchUser(42).thenCompose { user ->
                    fetchOrders(user.id).then { orders ->
                        [user: user, orders: orders]
                    }
                }
            )

            assert result.user.name == "User42"
            assert result.orders == ["order1", "order2"]
        '''
    }

    // =========================================================================
    // Parallel execution: awaitAll / awaitAny
    // =========================================================================

    @Test
    void testAwaitAllParallelTasks() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchPage(String url) {
                def page = async {
                    await(Awaitable.delay(30))
                    return "Content of ${url} (${url.length()} chars)"
                }
                return page()
            }

            def results = await(Awaitable.all(
                fetchPage("https://www.google.com/"),
                fetchPage("https://www.microsoft.com/"),
                fetchPage("https://www.example.com/")
            ))

            assert results.size() == 3
            assert results[0].contains("google")
            assert results[1].contains("microsoft")
            assert results[2].contains("example")
        '''
    }

    @Test
    void testAwaitAnyRace() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fastTask = async {
                await(Awaitable.delay(10))
                return "fast"
            }
            def fast = fastTask()
            def slowTask = async {
                await(Awaitable.delay(2000))
                return "slow"
            }
            def slow = slowTask()

            assert await(Awaitable.any(fast, slow)) == "fast"
        '''
    }

    // =========================================================================
    // Concurrent interleaving workflow
    // =========================================================================

    @Test
    void testConcurrentTasksInterleaving() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def log = Collections.synchronizedList([])

            def methodAAsync(List log) {
                def method = async {
                    for (int i = 0; i < 5; i++) {
                        log << ("A" + i)
                        await(Awaitable.delay(50))
                    }
                    log << "A returns result 123"
                    return 123
                }
                return method()
            }

            def taskA = methodAAsync(log)

            for (int i = 0; i < 5; i++) {
                log << ("B" + i)
                Thread.sleep(25)
            }

            log << "Wait for taskA termination"
            def result = await(taskA)
            log << ("The result of taskA is " + result)

            assert result == 123
            assert log.contains("A0")
            assert log.contains("B0")
            assert log.contains("A returns result 123")
            assert log.last() == "The result of taskA is 123"
        '''
    }

    // =========================================================================
    // Delay patterns
    // =========================================================================

    @Test
    void testAsyncDelayInLoop() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def loop = async {
                def sum = 0
                for (int i = 1; i <= 5; i++) {
                    await(Awaitable.delay(10))
                    sum += i
                }
                return sum
            }
            def result = await(loop())
            assert result == 15
        '''
    }

    @Test
    void testDelayZeroCompletesImmediately() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def task = async {
                await(Awaitable.delay(0))
                return "done"
            }
            def result = await(task())
            assert result == "done"
        '''
    }

    @Test
    void testDelayNegativeThrows() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                Awaitable.delay(-1)
                assert false : "Should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("negative")
            }
        '''
    }

    @Test
    void testDelayWithTimeUnit() {
        assertScript '''
            import java.util.concurrent.TimeUnit
            import groovy.concurrent.Awaitable

            def task = async {
                await(Awaitable.delay(50, TimeUnit.MILLISECONDS))
                return "delayed"
            }
            def result = await(task())
            assert result == "delayed"
        '''
    }

    @Test
    void testDelayIsNonBlocking() {
        assertScript '''
            import groovy.concurrent.Awaitable

            long startTime = System.currentTimeMillis()
            def delayed = async {
                await(Awaitable.delay(200))
                return "delayed"
            }
            def task = delayed()

            long afterStart = System.currentTimeMillis()
            assert (afterStart - startTime) < 100 : "async should return immediately"

            def result = await(task)
            long afterAwait = System.currentTimeMillis()

            assert result == "delayed"
            assert (afterAwait - startTime) >= 180 : "should have waited for delay"
        '''
    }

    // =========================================================================
    // Exception handling
    // =========================================================================

    @Test
    void testExceptionHandlingWithAwait() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def failingMethodAsync() {
                def failing = async {
                    for (int i = 0; i < 5; i++) {
                        await(Awaitable.delay(10))
                        throw new RuntimeException("Boum")
                    }
                    return 123
                }
                return failing()
            }

            def task = failingMethodAsync()

            try {
                await(task)
                assert false : "Should have thrown"
            } catch (RuntimeException ex) {
                assert ex.message == "Boum"
            }
        '''
    }

    @Test
    void testAwaitVsGetExceptionBehavior() {
        assertScript '''
            import java.util.concurrent.ExecutionException

            def failing1 = async {
                throw new IllegalStateException("async failure")
            }
            def failingTask = failing1()
            Thread.sleep(50)

            // await: original exception rethrown transparently
            def awaitCaughtType = null
            try {
                await(failingTask)
            } catch (IllegalStateException e) {
                awaitCaughtType = e.class.name
            }
            assert awaitCaughtType == 'java.lang.IllegalStateException'

            // .get(): wraps in ExecutionException
            def failing2 = async {
                throw new IllegalStateException("async failure 2")
            }
            def failingTask2 = failing2()
            Thread.sleep(50)

            def getCaughtType = null
            try {
                failingTask2.get()
            } catch (ExecutionException e) {
                getCaughtType = e.class.name
            }
            assert getCaughtType == 'java.util.concurrent.ExecutionException'
        '''
    }

    @Test
    void testExceptionInAsyncClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            try {
                def task = async {
                    await(Awaitable.delay(10))
                    throw new IOException("Network error")
                }
                await(task())
                assert false : "Should have thrown"
            } catch (IOException e) {
                assert e.message == "Network error"
            }
        '''
    }

    @Test
    void testExceptionRecoveryWithExceptionally() {
        assertScript '''
            def failing = async {
                throw new RuntimeException("fail")
            }
            def result = await(
                failing().exceptionally { ex ->
                    "Recovered: ${ex.message}"
                }
            )

            assert result == "Recovered: fail"
        '''
    }

    // =========================================================================
    // @Async annotation patterns
    // =========================================================================

    @Test
    void testAsyncAnnotationBasicPattern() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class DataService {
                @Async
                def fetchUser(long id) {
                    def profile = await fetchProfile(id)
                    def orders = await fetchOrders(id)
                    return [profile: profile, orders: orders]
                }

                @Async
                def fetchProfile(long id) {
                    return [name: "User${id}"]
                }

                @Async
                def fetchOrders(long id) {
                    return ["order1", "order2"]
                }
            }

            def service = new DataService()
            def result = await(service.fetchUser(1))
            assert result.profile.name == 'User1'
            assert result.orders == ['order1', 'order2']
        '''
    }

    @Test
    void testAsyncWithCustomExecutor() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors

            class Service {
                final def myExecutor = Executors.newSingleThreadExecutor { r ->
                    def t = new Thread(r, 'custom-exec')
                    t.daemon = true
                    t
                }

                @Async(executor = "myExecutor")
                def fetchData() {
                    return Thread.currentThread().name
                }
            }

            def service = new Service()
            assert await(service.fetchData()) == 'custom-exec'
        '''
    }

    // =========================================================================
    // Async closures and lambdas
    // =========================================================================

    @Test
    void testAsyncClosureWithParameters() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def computeAsync = async { int a, int b ->
                await(Awaitable.delay(10))
                return a + b
            }

            // Must call the closure first, then await the result
            def r1 = await(computeAsync(3, 4))
            def r2 = await(computeAsync(10, 20))

            assert r1 == 7
            assert r2 == 30
        '''
    }

    @Test
    void testAsyncLambdaExpression() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def doubler = async (x) -> {
                await(Awaitable.delay(10))
                return x * 2
            }

            // Must call the lambda first, then await the result
            assert await(doubler(21)) == 42
        '''
    }

    @Test
    void testAwaitClosureDirectlyThrows() {
        assertScript '''
            // Zero-param async closure
            def task = async { 42 }
            try {
                await(task)
                assert false : "Should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("Cannot await a Closure directly")
            }

            // Correct usage: call first, then await
            assert await(task()) == 42

            // Parameterized async closure
            def asyncDouble = async { n -> n * 2 }
            try {
                await(asyncDouble)
                assert false : "Should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("Cannot await a Closure directly")
            }

            // Correct usage: call with args, then await
            assert await(asyncDouble(21)) == 42
        '''
    }

    @Test
    void testAwaitLambdaDirectlyThrows() {
        assertScript '''
            def asyncInc = async (n) -> { n + 1 }

            try {
                await(asyncInc)
                assert false : "Should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message.contains("Cannot await a Closure directly")
            }

            // Correct usage: call with args, then await
            assert await(asyncInc(99)) == 100
        '''
    }

    // =========================================================================
    // Guidelines: purity, cancellation, retry, parallelism control
    // =========================================================================

    @Test
    void testPureAsyncFunction() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def computeAsync(int x, int y) {
                def compute = async { x + y }
                return compute()
            }

            def results = await(Awaitable.all(
                computeAsync(1, 2),
                computeAsync(3, 4),
                computeAsync(5, 6)
            ))
            assert results == [3, 7, 11]
        '''
    }

    @Test
    void testCancellationPatternWithClosureExpression() {
        assertScript '''
            import java.util.concurrent.CancellationException
            import groovy.concurrent.Awaitable

            def longRunning = async {
                await(Awaitable.delay(2000))
                return "completed"
            }
            def longTask = longRunning()

            Thread.sleep(50)
            assert longTask.cancel()
            assert longTask.isCancelled()

            try {
                await(longTask)
                assert false : "Should have thrown"
            } catch (CancellationException e) {
                assert true
            }
        '''
    }

    @Test
    void testRetryWithErrorHandling() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchWithRetry(int maxRetries) {
                def retryTask = async {
                    for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        try {
                            def attemptTask = async {
                                if (attempt < 3) throw new IOException("Attempt ${attempt} failed")
                                return "Success on attempt ${attempt}"
                            }
                            def result = await(attemptTask())
                            return result
                        } catch (IOException e) {
                            if (attempt == maxRetries) throw e
                            await(Awaitable.delay(10))
                        }
                    }
                }
                return retryTask()
            }

            assert await(fetchWithRetry(3)) == "Success on attempt 3"
        '''
    }

    @Test
    void testControlledParallelism() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def processedItems = Collections.synchronizedList([])
            def items = (1..12).toList()
            def batchSize = 4

            for (int batchStart = 0; batchStart < items.size(); batchStart += batchSize) {
                def batch = items.subList(batchStart, Math.min(batchStart + batchSize, items.size()))
                def tasks = batch.collect { item ->
                    def task = async {
                        await(Awaitable.delay(10))
                        processedItems << item
                        return item * 2
                    }
                    task()
                }
                def batchResults = await(Awaitable.all(*tasks))
                assert batchResults.size() == batch.size()
            }

            assert processedItems.sort() == (1..12).toList()
        '''
    }

    @Test
    void testCleanAsyncComposition() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchUserAsync(int id) {
                def fetch = async {
                    await(Awaitable.delay(20))
                    return [id: id, name: "User${id}"]
                }
                return fetch()
            }

            def enrichUserAsync(Map user) {
                def enrich = async {
                    await(Awaitable.delay(10))
                    return user + [enriched: true]
                }
                return enrich()
            }

            def formatUserAsync(Map user) {
                def format = async {
                    return "${user.name} (id=${user.id}, enriched=${user.enriched})"
                }
                return format()
            }

            def pipeline = async {
                def user = await(fetchUserAsync(42))
                def enriched = await(enrichUserAsync(user))
                def formatted = await(formatUserAsync(enriched))
                return formatted
            }
            def result = await(pipeline())

            assert result == "User42 (id=42, enriched=true)"
        '''
    }

    // =========================================================================
    // Advanced patterns
    // =========================================================================

    @Test
    void testInnerAndOuterTaskCompletion() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicBoolean
            import groovy.concurrent.Awaitable

            def innerCompleted = new AtomicBoolean(false)
            def outerCompleted = new AtomicBoolean(false)

            def methodAsync(AtomicBoolean inner, AtomicBoolean outer) {
                def method = async {
                    await(Awaitable.delay(30))
                    inner.set(true)
                    def result = 42
                    outer.set(true)
                    return result
                }
                return method()
            }

            def task = methodAsync(innerCompleted, outerCompleted)
            def result = await(task)

            assert result == 42
            assert innerCompleted.get()
            assert outerCompleted.get()
        '''
    }

    @Test
    void testSequentialAsyncStepsInLoop() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def threadNames = Collections.synchronizedList([])

            def loop = async {
                def sum = 0
                for (int i = 0; i < 5; i++) {
                    await(Awaitable.delay(10))
                    threadNames << Thread.currentThread().name
                    sum += (i + 1)
                }
                return sum
            }
            def result = await(loop())

            assert result == 15
            assert threadNames.size() == 5
        '''
    }

    @Test
    void testAllSettledMixedResults() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def success = async {
                await(Awaitable.delay(10))
                return "ok"
            }
            def successTask = success()
            def fail = async {
                await(Awaitable.delay(10))
                throw new RuntimeException("failed")
            }
            def failTask = fail()

            def results = await(Awaitable.allSettled(successTask, failTask))

            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].value == "ok"
            assert results[1].isFailure()
            assert results[1].error.message == "failed"
        '''
    }

    @Test
    void testComprehensiveAsyncPipeline() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fetchAsync(String key) {
                def fetch = async {
                    await(Awaitable.delay(20))
                    if (key == "bad") throw new IOException("Not found: ${key}")
                    return "data:${key}"
                }
                return fetch()
            }

            def pipeline = async {
                def tasks = ["alpha", "beta", "bad"].collect { key ->
                    fetchAsync(key).exceptionally { ex ->
                        "ERROR:${ex.message}"
                    }
                }
                return await(Awaitable.all(*tasks))
            }
            def result = await(pipeline())

            assert result[0] == "data:alpha"
            assert result[1] == "data:beta"
            assert result[2] == "ERROR:Not found: bad"
        '''
    }

    // =========================================================================
    // @Async method with await keyword syntax
    // =========================================================================

    @Test
    void testAsyncMethodWithAwaitKeyword() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class PipelineService {
                @Async
                def calculateAsync(int x, int y) {
                    await Awaitable.delay(10)
                    return x + y
                }

                @Async
                def pipelineAsync(int input) {
                    def step1 = await calculateAsync(input, 10)
                    def step2 = await calculateAsync(step1, 20)
                    return step2
                }
            }

            def svc = new PipelineService()
            assert await(svc.pipelineAsync(5)) == 35
        '''
    }

    @Test
    void testAsyncMethodControlFlow() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Workflow {
                @Async
                def process(List<Integer> items) {
                    def results = []
                    for (item in items) {
                        await Awaitable.delay(5)
                        if (item % 2 == 0) {
                            def multiply = async { item * 10 }
                            results << await(multiply())
                        } else {
                            results << item
                        }
                    }
                    return results
                }
            }

            def wf = new Workflow()
            assert await(wf.process([1, 2, 3, 4])) == [1, 20, 3, 40]
        '''
    }

    // ===== Awaitable combinator and integration tests =====

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
                await(Awaitable.orTimeout(new CompletableFuture<String>(), 50, TimeUnit.MILLISECONDS))
                assert false : "Should have timed out"
            } catch (TimeoutException e) {
                assert e.message.contains("Timed out after 50")
            }

            def fallback = await(Awaitable.completeOnTimeout(
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
    void testAwaitableOfNull() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def a = Awaitable.of(null)
            assert a.isDone()
            assert await(a) == null
        '''
    }

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
    void testParallelAsyncGeneratorsUnderConcurrency() {
        assertScript '''
import groovy.concurrent.Awaitable

async List<Integer> produce(int genId, int count) {
    def items = []
    for (int i = 0; i < count; i++) {
        items << await(Awaitable.of(genId * 1000 + i))
    }
    return items
}

int numTasks = 20
int itemsPerTask = 50

// Launch all tasks concurrently and await all results
def tasks = (0..<numTasks).collect { produce(it, itemsPerTask) }
def allResults = await(Awaitable.all(*tasks))

assert allResults.size() == numTasks
for (int g = 0; g < numTasks; g++) {
    def expected = (0..<itemsPerTask).collect { g * 1000 + it }
    assert allResults[g] == expected
}
        '''
    }

    /**
     * Verifies back-pressure: a fast producer is naturally throttled
     * by a slow consumer through the SynchronousQueue handoff.
     */

    @Test
    void testBackPressureFastProducerSlowConsumer() {
        assertScript '''
import groovy.concurrent.Awaitable

async generateFast() {
    for (int i = 0; i < 100; i++) {
        yield return i
    }
}

def results = []
for await (item in generateFast()) {
    results << item
    // Slow consumer: the producer cannot get far ahead because
    // SynchronousQueue blocks until consumer takes each item
    if (item < 5) {
        Thread.sleep(10)
    }
}
assert results == (0..<100).toList()
        '''
    }

    /**
     * Consumer breaks out of for-await mid-stream under high load.
     * Verifies the producer is interrupted and cleaned up promptly.
     */

    @Test
    void testMidStreamCancelUnderLoad() {
        assertScript '''
import groovy.concurrent.Awaitable

async generateMany() {
    for (int i = 0; i < 1_000_000; i++) {
        yield return i
    }
}

def consumed = []
for await (item in generateMany()) {
    consumed << item
    if (item == 9) break  // Cancel after 10 items
}

assert consumed == (0..9).toList()
        '''
    }

    /**
     * Nested async chains: async method A calls async method B which calls C.
     * All running concurrently with multiple callers via Awaitable.all().
     */

    @Test
    void testNestedAwaitChainsUnderConcurrency() {
        assertScript '''
import groovy.concurrent.Awaitable

async int fetchValue(int id) {
    await(Awaitable.delay(1))
    return id * 10
}

async int transform(int id) {
    def raw = await(fetchValue(id))
    return raw + 1
}

async int pipeline(int id) {
    def transformed = await(transform(id))
    return transformed + 100
}

int numCallers = 50
def tasks = (0..<numCallers).collect { pipeline(it) }
def results = await(Awaitable.all(*tasks))

assert results.size() == numCallers
for (int i = 0; i < numCallers; i++) {
    assert results[i] == i * 10 + 1 + 100
}
        '''
    }

    /**
     * Retry pattern under concurrent failures:
     * multiple tasks experience transient errors and are retried.
     */

    @Test
    void testRetryPatternUnderConcurrentFailures() {
        assertScript '''
import groovy.concurrent.Awaitable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import groovy.transform.Field

@Field def attemptCounts = new ConcurrentHashMap<Integer, AtomicInteger>()

async int unstableService(int taskId) {
    def counter = attemptCounts.computeIfAbsent(taskId, { new AtomicInteger(0) })
    int attempt = counter.incrementAndGet()
    if (attempt <= 2) {
        throw new IOException("Transient failure #${attempt} for task ${taskId}")
    }
    return taskId * 100
}

async int retryWithBackoff(int taskId, int maxRetries) {
    for (int i = 0; i <= maxRetries; i++) {
        try {
            return await(unstableService(taskId))
        } catch (IOException e) {
            if (i == maxRetries) throw e
            await(Awaitable.delay(1))
        }
    }
    throw new AssertionError("unreachable")
}

int numTasks = 20
def tasks = (0..<numTasks).collect { retryWithBackoff(it, 3) }
def results = await(Awaitable.all(*tasks))

assert results.size() == numTasks
for (int i = 0; i < numTasks; i++) {
    assert results[i] == i * 100
    assert attemptCounts[i].get() == 3  // 2 failures + 1 success
}
        '''
    }

    /**
     * Concurrent allSettled with mixed success and failure tasks.
     * Verifies thread-safe result collection under contention.
     */

    @Test
    void testConcurrentAllSettledMixedResults() {
        assertScript '''
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitResult
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

async int succeedOrFail(int id) {
    await(Awaitable.delay(1))
    if (id % 3 == 0) throw new RuntimeException("fail-${id}")
    return id
}

async List<AwaitResult> runBatch(int batchSize) {
    def tasks = (0..<batchSize).collect { succeedOrFail(it) }
    return await(Awaitable.allSettled(*tasks))
}

int batchSize = 30
def results = await(runBatch(batchSize))
assert results.size() == batchSize

int successes = 0, failures = 0
for (int i = 0; i < batchSize; i++) {
    if (i % 3 == 0) {
        assert results[i].isFailure()
        assert results[i].error.message == "fail-${i}"
        failures++
    } else {
        assert results[i].isSuccess()
        assert results[i].value == i
        successes++
    }
}
assert successes == 20
assert failures == 10
        '''
    }

    /**
     * Multiple for-await loops consuming generators concurrently via Awaitable.all().
     * Each task runs its own generator and collects results independently.
     */

    @Test
    void testConcurrentForAwaitLoops() {
        assertScript '''
import groovy.concurrent.Awaitable

async generateSequence(int start, int count) {
    for (int i = start; i < start + count; i++) {
        yield return i
    }
}

async List<Integer> consumeStream(int loopId, int itemsPerLoop) {
    def items = []
    for await (n in generateSequence(loopId * 1000, itemsPerLoop)) {
        items << n
    }
    return items
}

int numLoops = 20
int itemsPerLoop = 25
def tasks = (0..<numLoops).collect { consumeStream(it, itemsPerLoop) }
def allResults = await(Awaitable.all(*tasks))

assert allResults.size() == numLoops
for (int loop = 0; loop < numLoops; loop++) {
    def expected = (loop * 1000 ..< loop * 1000 + itemsPerLoop).toList()
    assert allResults[loop] == expected
}
        '''
    }

    /**
     * Simulates a real-world fan-out/fan-in pattern:
     * one coordinator dispatches work to multiple async workers,
     * collects results, and aggregates them.
     */

    @Test
    void testFanOutFanInPattern() {
        assertScript '''
import groovy.concurrent.Awaitable

async int processChunk(List<Integer> chunk) {
    await(Awaitable.delay(1))
    return chunk.sum()
}

async int distributedSum(List<Integer> data, int chunkSize) {
    def chunks = data.collate(chunkSize)
    def tasks = chunks.collect { processChunk(it) }
    def results = await(Awaitable.all(*tasks))
    return results.sum()
}

def data = (1..1000).toList()
def total = await(distributedSum(data, 50))
assert total == (1..1000).sum()  // 500500
        '''
    }

    /**
     * Verifies that exceptions thrown in async generators propagate
     * correctly to the consumer even under concurrent usage.
     */

    @Test
    void testGeneratorExceptionPropagationUnderConcurrency() {
        assertScript '''
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitResult

async generateWithError(int failAt) {
    for (int i = 0; i < 100; i++) {
        if (i == failAt) throw new IllegalStateException("boom at ${i}")
        yield return i
    }
}

async Map consumeWithErrorHandling(int consumerId, int failPoint) {
    int count = 0
    String error = null
    try {
        for await (n in generateWithError(failPoint)) {
            count++
        }
    } catch (IllegalStateException e) {
        error = e.message
    }
    return [count: count, error: error]
}

int numConsumers = 15
def tasks = (0..<numConsumers).collect { consumeWithErrorHandling(it, 5 + it) }
def allResults = await(Awaitable.all(*tasks))

assert allResults.size() == numConsumers
for (int c = 0; c < numConsumers; c++) {
    assert allResults[c].error == "boom at ${5 + c}"
    assert allResults[c].count == 5 + c  // Items consumed before the error
}
        '''
    }

    /**
     * Stress test: many short-lived async tasks created and awaited rapidly.
     * Verifies executor handles rapid task churn without thread leaks.
     */

    @Test
    void testRapidAsyncTaskChurn() {
        assertScript '''
import groovy.concurrent.Awaitable
import java.util.concurrent.atomic.AtomicInteger

def completed = new AtomicInteger(0)

async int quickTask(int n) {
    return n + 1
}

// Create and await many small tasks in rapid succession
for (int batch = 0; batch < 10; batch++) {
    def tasks = (0..<100).collect { quickTask(it) }
    def results = await(Awaitable.all(*tasks))
    assert results.size() == 100
    for (int i = 0; i < 100; i++) {
        assert results[i] == i + 1
    }
    completed.addAndGet(100)
}

assert completed.get() == 1000
        '''
    }

}
