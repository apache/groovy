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

import groovy.concurrent.AsyncUtils
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
 *   <li>At script top-level: {@code AsyncUtils.await(expr)} or static import</li>
 * </ul>
 */
class AsyncPatternsTest {

    @AfterEach
    void resetExecutor() {
        AsyncUtils.setExecutor(null)
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
            import groovy.concurrent.AsyncUtils

            def fetchDataAsync(String url) {
                def fetch = async {
                    await(AsyncUtils.delay(50))
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
            import groovy.concurrent.AsyncUtils

            def fetchDataAsync() {
                def fetch = async {
                    await(AsyncUtils.delay(50))
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
            import groovy.concurrent.AsyncUtils

            def fetchDataAsync() {
                def fetch = async {
                    await(AsyncUtils.delay(50))
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
            import groovy.concurrent.AsyncUtils

            def fetchDataAsync() {
                def fetch = async {
                    await(AsyncUtils.delay(30))
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
            import groovy.concurrent.AsyncUtils

            def fetchPage(String url) {
                def page = async {
                    await(AsyncUtils.delay(30))
                    return "Content of ${url} (${url.length()} chars)"
                }
                return page()
            }

            def results = AsyncUtils.awaitAll(
                fetchPage("https://www.google.com/"),
                fetchPage("https://www.microsoft.com/"),
                fetchPage("https://www.example.com/")
            )

            assert results.size() == 3
            assert results[0].contains("google")
            assert results[1].contains("microsoft")
            assert results[2].contains("example")
        '''
    }

    @Test
    void testAwaitAnyRace() {
        assertScript '''
            import groovy.concurrent.AsyncUtils

            def fastTask = async {
                await(AsyncUtils.delay(10))
                return "fast"
            }
            def fast = fastTask()
            def slowTask = async {
                await(AsyncUtils.delay(2000))
                return "slow"
            }
            def slow = slowTask()

            assert AsyncUtils.awaitAny(fast, slow) == "fast"
        '''
    }

    // =========================================================================
    // Concurrent interleaving workflow
    // =========================================================================

    @Test
    void testConcurrentTasksInterleaving() {
        assertScript '''
            import groovy.concurrent.AsyncUtils

            def log = Collections.synchronizedList([])

            def methodAAsync(List log) {
                def method = async {
                    for (int i = 0; i < 5; i++) {
                        log << ("A" + i)
                        await(AsyncUtils.delay(50))
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
            import groovy.concurrent.AsyncUtils

            def loop = async {
                def sum = 0
                for (int i = 1; i <= 5; i++) {
                    await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def task = async {
                await(AsyncUtils.delay(0))
                return "done"
            }
            def result = await(task())
            assert result == "done"
        '''
    }

    @Test
    void testDelayNegativeThrows() {
        assertScript '''
            import groovy.concurrent.AsyncUtils

            try {
                AsyncUtils.delay(-1)
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
            import groovy.concurrent.AsyncUtils

            def task = async {
                await(AsyncUtils.delay(50, TimeUnit.MILLISECONDS))
                return "delayed"
            }
            def result = await(task())
            assert result == "delayed"
        '''
    }

    @Test
    void testDelayIsNonBlocking() {
        assertScript '''
            import groovy.concurrent.AsyncUtils

            long startTime = System.currentTimeMillis()
            def delayed = async {
                await(AsyncUtils.delay(200))
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
            import groovy.concurrent.AsyncUtils

            def failingMethodAsync() {
                def failing = async {
                    for (int i = 0; i < 5; i++) {
                        await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            try {
                def task = async {
                    await(AsyncUtils.delay(10))
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
            import static groovy.concurrent.AsyncUtils.*

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
            import static groovy.concurrent.AsyncUtils.*
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
            import groovy.concurrent.AsyncUtils

            def computeAsync = async { int a, int b ->
                await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def doubler = async (x) -> {
                await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def computeAsync(int x, int y) {
                def compute = async { x + y }
                return compute()
            }

            def results = AsyncUtils.awaitAll(
                computeAsync(1, 2),
                computeAsync(3, 4),
                computeAsync(5, 6)
            )
            assert results == [3, 7, 11]
        '''
    }

    @Test
    void testCancellationPattern() {
        assertScript '''
            import java.util.concurrent.CancellationException
            import groovy.concurrent.AsyncUtils

            def longRunning = async {
                await(AsyncUtils.delay(5000))
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
            import groovy.concurrent.AsyncUtils

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
                            await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def processedItems = Collections.synchronizedList([])
            def items = (1..12).toList()
            def batchSize = 4

            for (int batchStart = 0; batchStart < items.size(); batchStart += batchSize) {
                def batch = items.subList(batchStart, Math.min(batchStart + batchSize, items.size()))
                def tasks = batch.collect { item ->
                    def task = async {
                        await(AsyncUtils.delay(10))
                        processedItems << item
                        return item * 2
                    }
                    task()
                }
                def batchResults = AsyncUtils.awaitAll(*tasks)
                assert batchResults.size() == batch.size()
            }

            assert processedItems.sort() == (1..12).toList()
        '''
    }

    @Test
    void testCleanAsyncComposition() {
        assertScript '''
            import groovy.concurrent.AsyncUtils

            def fetchUserAsync(int id) {
                def fetch = async {
                    await(AsyncUtils.delay(20))
                    return [id: id, name: "User${id}"]
                }
                return fetch()
            }

            def enrichUserAsync(Map user) {
                def enrich = async {
                    await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def innerCompleted = new AtomicBoolean(false)
            def outerCompleted = new AtomicBoolean(false)

            def methodAsync(AtomicBoolean inner, AtomicBoolean outer) {
                def method = async {
                    await(AsyncUtils.delay(30))
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
            import groovy.concurrent.AsyncUtils

            def threadNames = Collections.synchronizedList([])

            def loop = async {
                def sum = 0
                for (int i = 0; i < 5; i++) {
                    await(AsyncUtils.delay(10))
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
            import groovy.concurrent.AsyncUtils

            def success = async {
                await(AsyncUtils.delay(10))
                return "ok"
            }
            def successTask = success()
            def fail = async {
                await(AsyncUtils.delay(10))
                throw new RuntimeException("failed")
            }
            def failTask = fail()

            def results = AsyncUtils.awaitAllSettled(successTask, failTask)

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
            import groovy.concurrent.AsyncUtils

            def fetchAsync(String key) {
                def fetch = async {
                    await(AsyncUtils.delay(20))
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
                return AsyncUtils.awaitAll(*tasks)
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
            import static groovy.concurrent.AsyncUtils.*

            class PipelineService {
                @Async
                def calculateAsync(int x, int y) {
                    await delay(10)
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
            import static groovy.concurrent.AsyncUtils.*

            class Workflow {
                @Async
                def process(List<Integer> items) {
                    def results = []
                    for (item in items) {
                        await delay(5)
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
}
