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
import groovy.test.GroovyAssert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript


class AsyncBestPracticesTest {

    @AfterEach
    void resetExecutor() {
        Awaitable.setExecutor(null)
    }

    @Test
    void testAsyncAllTheWay() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Async should be viral: callers should await, not block
            async fetchData() { "data" }
            async processData() {
                def data = await fetchData()
                "processed: $data"
            }
            async handleRequest() {
                def result = await processData()
                "response: $result"
            }

            assert await(handleRequest()) == "response: processed: data"
        '''
    }

    @Test
    void testAvoidAsyncVoidPattern() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicBoolean

            // Correct: async method returns Awaitable, so caller can await and catch errors
            class OrderService {
                AtomicBoolean saved = new AtomicBoolean(false)
                async processOrder(String orderId) {
                    saved.set(true)
                    "order-$orderId"
                }
            }

            def svc = new OrderService()
            def result = await svc.processOrder("123")
            assert result == "order-123"
            assert svc.saved.get()
        '''
    }

    @Test
    void testNeverBlockOnAsyncCode() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // CORRECT: use await to suspend without blocking
            async fetchUser(int id) { "user-$id" }

            async correctWay() {
                def user = await fetchUser(1)
                assert user == "user-1"
                user
            }

            assert await(correctWay()) == "user-1"
        '''
    }

    @Test
    void testAlwaysAwaitTasks() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            // Always await — never fire-and-forget
            class BatchProcessor {
                AtomicInteger count = new AtomicInteger(0)
                async processSingle(String item) {
                    count.incrementAndGet()
                    "done-$item"
                }

                async processAll(List<String> items) {
                    // Correct: await each or use Awaitable.all
                    def tasks = items.collect { processSingle(it) }
                    await Awaitable.all(*tasks)
                }
            }

            def proc = new BatchProcessor()
            await proc.processAll(["a", "b", "c"])
            assert proc.count.get() == 3
        '''
    }

    @Test
    void testSequentialExecution() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CopyOnWriteArrayList

            class SequentialTest {
                def steps = new CopyOnWriteArrayList<String>()

                async step1() { steps.add("validate"); "valid" }
                async step2(input) { steps.add("process"); "processed-$input" }
                async step3(input) { steps.add("confirm"); "confirmed-$input" }

                async processOrder() {
                    def v = await step1()
                    def p = await step2(v)
                    await step3(p)
                }
            }

            def t = new SequentialTest()
            def result = await t.processOrder()
            assert result == "confirmed-processed-valid"
            assert t.steps == ["validate", "process", "confirm"]
        '''
    }

    @Test
    void testParallelExecution() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Parallel: independent operations started together
            async fetchUser(int id)     { "user-$id" }
            async fetchOrders(int id)   { ["order1", "order2"] }
            async fetchStats(int id)    { [views: 100] }

            async getDashboard(int userId) {
                def userTask    = fetchUser(userId)
                def ordersTask  = fetchOrders(userId)
                def statsTask   = fetchStats(userId)

                def results = await Awaitable.all(userTask, ordersTask, statsTask)
                [user: results[0], orders: results[1], stats: results[2]]
            }

            def dashboard = await getDashboard(42)
            assert dashboard.user == "user-42"
            assert dashboard.orders == ["order1", "order2"]
            assert dashboard.stats == [views: 100]
        '''
    }

    @Test
    void testCancellationPattern() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException

            // Long-running task that can be cancelled
            async longRunning() {
                await Awaitable.delay(10_000)
                "done"
            }

            def task = longRunning()
            // Cancel before it completes
            task.cancel()

            assert task.isCancelled()
            assert task.isCompletedExceptionally()

            try {
                await(task)
                assert false
            } catch (CancellationException e) {
                // expected — properly propagated
            }
        '''
    }

    @Test
    void testExceptionHandlingSingleTask() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async riskyOperation() {
                throw new IOException("db error")
            }

            async handleSafely() {
                try {
                    await riskyOperation()
                } catch (IOException e) {
                    "recovered: ${e.message}"
                }
            }

            assert await(handleSafely()) == "recovered: db error"
        '''
    }

    @Test
    void testExceptionHandlingWithAllSettled() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // allSettled captures all exceptions without short-circuiting
            async ok()   { "success" }
            async fail1() { throw new IOException("io") }
            async fail2() { throw new ArithmeticException("math") }

            def results = await Awaitable.allSettled(ok(), fail1(), fail2())

            def errors = results.findAll { it.isFailure() }
            assert errors.size() == 2
            assert errors[0].error instanceof IOException
            assert errors[1].error instanceof ArithmeticException

            def successes = results.findAll { it.isSuccess() }
            assert successes.size() == 1
            assert successes[0].value == "success"
        '''
    }

    @Test
    void testCachedAsyncResult() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.ConcurrentHashMap

            // ValueTask-like pattern: return cached results synchronously
            class UserCache {
                def cache = new ConcurrentHashMap<Integer, String>()
                int fetchCount = 0

                def getUser(int id) {
                    def cached = cache.get(id)
                    if (cached != null) {
                        return Awaitable.of(cached)
                    }
                    return fetchAndCache(id)
                }

                async fetchAndCache(int id) {
                    fetchCount++
                    def user = "user-$id"
                    cache.put(id, user)
                    user
                }
            }

            def svc = new UserCache()
            assert await(svc.getUser(1)) == "user-1"
            assert await(svc.getUser(1)) == "user-1"  // from cache
            assert svc.fetchCount == 1
        '''
    }

    @Test
    void testAsyncStreamWithPagination() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncStream

            // Async stream pattern: process large datasets without loading all into memory
            async getPage(int pageNum) {
                if (pageNum >= 3) return []
                (1..5).collect { "item-${pageNum * 5 + it}" }
            }

            async processAllItems() {
                def allItems = []
                int page = 0
                while (true) {
                    def items = await getPage(page)
                    if (items.isEmpty()) break
                    allItems.addAll(items)
                    page++
                }
                allItems
            }

            def items = await processAllItems()
            assert items.size() == 15
            assert items[0] == "item-1"
            assert items[-1] == "item-15"
        '''
    }

    @Test
    void testAsyncInitializationPattern() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.ConcurrentHashMap

            class AsyncService {
                private final ConcurrentHashMap<String, String> state = new ConcurrentHashMap<>()

                async getConnection() {
                    def c = state.get("conn")
                    if (c != null) return c
                    def newConn = await initConnection()
                    state.putIfAbsent("conn", newConn)
                    state.get("conn")
                }

                async initConnection() {
                    await Awaitable.delay(10)
                    "db-conn-open"
                }
            }

            def svc = new AsyncService()
            def results = await Awaitable.all(
                svc.getConnection(),
                svc.getConnection()
            )
            assert results.every { it == "db-conn-open" }
        '''
    }

    @Test
    void testTimeoutWithAny() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // Race operation against timeout
            async slowOp() {
                await Awaitable.delay(10_000)
                "result"
            }

            def task = slowOp()
            def timeout = Awaitable.delay(50)

            def winner = await Awaitable.any(task, timeout)
            // Timeout wins (null from delay)
            assert winner == null
            task.cancel()
        '''
    }

    @Test
    void testRetryPattern() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            class RetryService {
                def attempts = new AtomicInteger(0)

                async unreliable() {
                    if (attempts.incrementAndGet() < 3) {
                        throw new IOException("transient error")
                    }
                    "success"
                }

                async withRetry(int maxRetries) {
                    for (int i = 0; i <= maxRetries; i++) {
                        try {
                            return await unreliable()
                        } catch (IOException e) {
                            if (i == maxRetries) throw e
                            await Awaitable.delay(10)
                        }
                    }
                }
            }

            def svc = new RetryService()
            assert await(svc.withRetry(3)) == "success"
            assert svc.attempts.get() == 3
        '''
    }

    // =========================================================================
    // 1. Asynchrony is viral — propagate await through the call chain
    // =========================================================================

    /**
     * Demonstrates that once a method is async, callers should also use
     * {@code await} rather than blocking with {@code .get()}.
     */
    @Test
    void testAsynchronyIsViral_AwaitPropagation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class DependencyService {
                async callDependency() { return 42 }
            }

            class ConsumerService {
                private final DependencyService dep = new DependencyService()

                // GOOD: async all the way through
                async doSomething() {
                    def result = await dep.callDependency()
                    return result + 1
                }
            }

            def service = new ConsumerService()
            assert await(service.doSomething()) == 43
        '''
    }

    @Test
    void testAsynchronyIsViral_MultipleLayersOfAwait() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class DataService {
                async fetchFromDb() {
                    await Awaitable.delay(10)
                    return "db-data"
                }
            }

            class BusinessLogic {
                private final DataService dataService = new DataService()
                async process() {
                    def raw = await dataService.fetchFromDb()
                    return raw.toUpperCase()
                }
            }

            class Controller {
                private final BusinessLogic logic = new BusinessLogic()
                async handleRequest() {
                    def result = await logic.process()
                    return "Response: ${result}"
                }
            }

            def controller = new Controller()
            assert await(controller.handleRequest()) == "Response: DB-DATA"
        '''
    }

    // =========================================================================
    // 2. Prefer Awaitable.of() for pre-computed data
    // =========================================================================

    /**
     * When the result is already known, use {@code Awaitable.of()} to avoid
     * spawning a thread. This is the equivalent of C#'s {@code Task.FromResult}.
     */
    @Test
    void testPreComputedResult_AwaitableOf() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class MyLibrary {
                static Awaitable<Integer> addAsync(int a, int b) {
                    return Awaitable.of(a + b)
                }
            }

            // No thread spawned — result is immediately available
            assert await(MyLibrary.addAsync(3, 4)) == 7
        '''
    }

    @Test
    void testPreComputedResult_AwaitableOfVsAsyncClosure() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // GOOD: Awaitable.of() — no thread allocation
            def cachedValue = Awaitable.of("cached")
            assert await(cachedValue) == "cached"
            assert cachedValue.isDone()

            // Also fine for truly async work: async closure
            def computed = async { Thread.sleep(10); "computed" }
            assert await(computed()) == "computed"
        '''
    }

    @Test
    void testPreComputedResult_FailedAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def failed = Awaitable.failed(new IllegalArgumentException("bad input"))
            assert failed.isDone()
            assert failed.isCompletedExceptionally()

            try {
                await(failed)
                assert false : "should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message == "bad input"
            }
        '''
    }

    // =========================================================================
    // 3. Avoid sync-over-async — never block with .get()
    // =========================================================================

    /**
     * Demonstrates the correct pattern: use {@code await} instead of the
     * blocking {@code .get()} method on {@code Awaitable}.
     */
    @Test
    void testAvoidSyncOverAsync_UseAwait() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class AsyncService {
                async fetchData() {
                    return "async-data"
                }
            }

            def service = new AsyncService()

            // GOOD: use await to get the result
            def result = await(service.fetchData())
            assert result == "async-data"
        '''
    }

    @Test
    void testAvoidSyncOverAsync_NestedAwait() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async fetchLevel1() {
                return await(fetchLevel2())
            }

            async fetchLevel2() {
                return await(fetchLevel3())
            }

            async fetchLevel3() {
                return 42
            }

            // GOOD: await propagates cleanly through the chain
            assert await(fetchLevel1()) == 42
        '''
    }

    // =========================================================================
    // 4. Prefer await over then/thenCompose
    // =========================================================================

    /**
     * Use {@code await} instead of chaining with {@code .then()} or
     * {@code .thenCompose()} for cleaner, more readable code.
     */
    @Test
    void testPreferAwaitOverThen() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async callDependency() { return 10 }

            // GOOD: await with sequential logic is clearer than chaining
            async doSomething() {
                def result = await callDependency()
                return result + 1
            }

            assert await(doSomething()) == 11
        '''
    }

    @Test
    void testPreferAwaitOverThenCompose_MultiStep() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async step1() { return 1 }
            async step2(int n) { return n * 2 }
            async step3(int n) { return n + 10 }

            // GOOD: sequential awaits read like synchronous code
            async pipeline() {
                def a = await step1()
                def b = await step2(a)
                def c = await step3(b)
                return c
            }

            assert await(pipeline()) == 12
        '''
    }

    // =========================================================================
    // 5. Prefer async/await over directly returning Awaitable
    // =========================================================================

    /**
     * Wrapping in {@code async} provides better exception handling and
     * debugging compared to directly returning an {@code Awaitable}.
     */
    @Test
    void testPreferAsyncOverDirectReturn() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async callDependency() { return "data" }

            // GOOD: async/await wraps exceptions and provides better stack traces
            async doSomething() {
                return await callDependency()
            }

            assert await(doSomething()) == "data"
        '''
    }

    @Test
    void testPreferAsyncOverDirectReturn_ExceptionWrapping() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async riskyOperation() {
                throw new IOException("disk failure")
            }

            // GOOD: exceptions in async methods are captured in the Awaitable
            async safeWrapper() {
                try {
                    return await riskyOperation()
                } catch (IOException e) {
                    return "fallback: ${e.message}"
                }
            }

            assert await(safeWrapper()) == "fallback: disk failure"
        '''
    }

    // =========================================================================
    // 6. Cancelling uncancellable operations with a timeout
    // =========================================================================

    /**
     * Use {@code awaitAny} with {@code delay} to implement timeout-based
     * cancellation for operations that don't support cancellation natively.
     * This is the Groovy equivalent of C#'s {@code Task.WhenAny + Task.Delay}.
     */
    @Test
    void testTimeoutWithAwaitAny() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            async slowOperation() {
                await Awaitable.delay(5000)
                return "completed"
            }

            async withTimeout(Awaitable task, long timeoutMs) {
                def timeout = Awaitable.delay(timeoutMs)
                def winner = await(Awaitable.any(task, timeout))
                if (winner == null) {
                    throw new java.util.concurrent.TimeoutException("operation timed out")
                }
                return winner
            }

            def task = slowOperation()
            try {
                await(withTimeout(task, 50))
                assert false : "should have timed out"
            } catch (java.util.concurrent.TimeoutException e) {
                assert e.message == "operation timed out"
            } finally {
                task.cancel()
            }
        '''
    }

    @Test
    void testTimeoutWithAwaitAny_SuccessBeforeTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            async fastOperation() {
                await Awaitable.delay(10)
                return "fast-result"
            }

            def task = fastOperation()
            def timeout = Awaitable.delay(2000)
            def winner = await(Awaitable.any(task, timeout))
            assert winner == "fast-result"
        '''
    }

    // =========================================================================
    // 7. Timer callback patterns
    // =========================================================================

    /**
     * Demonstrates safe async work in scheduled/timer contexts.
     * Instead of fire-and-forget, capture the Awaitable for error tracking.
     */
    @Test
    void testTimerCallback_SafeAsyncExecution() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger
            import java.util.concurrent.CountDownLatch

            def pingCount = new AtomicInteger(0)
            def latch = new CountDownLatch(3)

            // Use async closure that captures local variables
            def doAsyncPing = { AtomicInteger pc, CountDownLatch lt ->
                def task = async {
                    await Awaitable.delay(5)
                    pc.incrementAndGet()
                    lt.countDown()
                    return "pong"
                }
                task()
            }

            // Schedule 3 pings using a timer
            def timer = new Timer(true)
            timer.schedule(new TimerTask() {
                void run() {
                    doAsyncPing(pingCount, latch)
                }
            }, 0, 20)

            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            timer.cancel()
            assert pingCount.get() >= 3
        '''
    }

    @Test
    void testTimerCallback_PeriodicAsyncWork() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch

            def results = Collections.synchronizedList([])
            def latch = new CountDownLatch(3)

            // Use async closures that capture the local variables
            3.times { i ->
                def myResults = results
                def myLatch = latch
                def task = async {
                    await Awaitable.delay(5)
                    myResults.add("item-${i}")
                    myLatch.countDown()
                    return "done"
                }
                task()
            }

            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            assert results.size() == 3
            assert results.sort() == ["item-0", "item-1", "item-2"]
        '''
    }

    // =========================================================================
    // 8. Async lazy caching with ConcurrentHashMap
    // =========================================================================

    /**
     * Demonstrates async lazy caching pattern using {@code ConcurrentHashMap}
     * with {@code Awaitable} values. This avoids thread-pool starvation that
     * would occur with synchronous {@code .get()} in the cache factory.
     */
    @Test
    void testAsyncLazyCaching() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.atomic.AtomicInteger

            class PersonCache {
                final AtomicInteger fetchCount = new AtomicInteger(0)
                final ConcurrentHashMap<Integer, Awaitable> cache = new ConcurrentHashMap<>()

                Awaitable fetchPerson(int id) {
                    def counter = fetchCount
                    def fetch = async {
                        counter.incrementAndGet()
                        await Awaitable.delay(10)
                        return "Person-${id}"
                    }
                    return fetch()
                }
            }

            def pc = new PersonCache()

            // First access: triggers fetch
            def result1 = await(pc.cache.computeIfAbsent(1, { id -> pc.fetchPerson(id) }))
            assert result1 == "Person-1"
            assert pc.fetchCount.get() == 1

            // Second access: cache hit, no new fetch
            def result2 = await(pc.cache.computeIfAbsent(1, { id -> pc.fetchPerson(id) }))
            assert result2 == "Person-1"
            assert pc.fetchCount.get() == 1

            // Different key: new fetch
            def result3 = await(pc.cache.computeIfAbsent(2, { id -> pc.fetchPerson(id) }))
            assert result3 == "Person-2"
            assert pc.fetchCount.get() == 2
        '''
    }

    @Test
    void testAsyncLazyCaching_ConcurrentAccess() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.atomic.AtomicInteger
            import java.util.concurrent.CountDownLatch

            class DataCache {
                final AtomicInteger fetchCount = new AtomicInteger(0)
                final ConcurrentHashMap<Integer, Awaitable> cache = new ConcurrentHashMap<>()

                Awaitable fetchData(int id) {
                    def counter = fetchCount
                    def fetch = async {
                        counter.incrementAndGet()
                        await Awaitable.delay(50)
                        return "Data-${id}"
                    }
                    return fetch()
                }
            }

            def dc = new DataCache()
            def latch = new CountDownLatch(10)
            def results = Collections.synchronizedList([])

            // Simulate 10 concurrent requests for the same key
            10.times {
                def myCache = dc
                def myResults = results
                def myLatch = latch
                def task = async {
                    def value = await(myCache.cache.computeIfAbsent(1, { id -> myCache.fetchData(id) }))
                    myResults.add(value)
                    myLatch.countDown()
                }
                task()
            }

            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            assert results.size() == 10
            assert results.every { it == "Data-1" }
            // computeIfAbsent ensures at most 1 fetch for the same key
            assert dc.fetchCount.get() == 1
        '''
    }

    // =========================================================================
    // 9. Static factory pattern for async construction
    // =========================================================================

    /**
     * When a class requires asynchronous initialization, use a static factory
     * method instead of blocking in the constructor.
     */
    @Test
    void testStaticFactoryForAsyncConstruction() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class RemoteService {
                final String connectionId

                private RemoteService(String connectionId) {
                    this.connectionId = connectionId
                }

                // GOOD: static async factory
                static async create() {
                    await Awaitable.delay(10)
                    def connId = "conn-${System.nanoTime()}"
                    return new RemoteService(connId)
                }

                async publish(String message) {
                    return "Published '${message}' on ${connectionId}"
                }
            }

            def service = await(RemoteService.create())
            assert service.connectionId.startsWith("conn-")
            def publishResult = await(service.publish("hello"))
            assert publishResult.contains("Published 'hello'")
        '''
    }

    @Test
    void testStaticFactoryForAsyncConstruction_WithDependency() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            class Database {
                final String url
                private Database(String url) { this.url = url }

                static async connect(String url) {
                    await Awaitable.delay(10)
                    return new Database(url)
                }

                async query(String sql) {
                    await Awaitable.delay(5)
                    return ["row1", "row2"]
                }
            }

            class Repository {
                final Database db
                private Repository(Database db) { this.db = db }

                static async create(String dbUrl) {
                    def db = await Database.connect(dbUrl)
                    return new Repository(db)
                }

                async findAll() {
                    return await db.query("SELECT * FROM items")
                }
            }

            def repo = await(Repository.create("jdbc:h2:mem:test"))
            assert await(repo.findAll()) == ["row1", "row2"]
        '''
    }

    // =========================================================================
    // 10. Long-running work with dedicated threads
    // =========================================================================

    /**
     * For long-running blocking work, use a dedicated thread instead of
     * occupying a thread-pool thread indefinitely.
     */
    @Test
    void testLongRunningWork_DedicatedThread() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.LinkedBlockingQueue
            import java.util.concurrent.CountDownLatch

            class QueueProcessor {
                private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>()
                private final List<String> processed = Collections.synchronizedList([])
                private volatile boolean running = true

                void startProcessing() {
                    // GOOD: dedicated daemon thread for long-running work
                    def t = new Thread({
                        while (running) {
                            def item = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                            if (item != null) processed.add(item.toUpperCase())
                        }
                    })
                    t.daemon = true
                    t.name = "queue-processor"
                    t.start()
                }

                void enqueue(String message) { queue.add(message) }

                void stop() { running = false }

                List<String> getProcessed() { return processed }
            }

            def processor = new QueueProcessor()
            processor.startProcessing()

            // Enqueue items
            3.times { i -> processor.enqueue("msg-${i}") }
            Thread.sleep(300)

            processor.stop()
            assert processor.processed.sort() == ["MSG-0", "MSG-1", "MSG-2"]
        '''
    }

    // =========================================================================
    // 11. Fire-and-forget safely
    // =========================================================================

    /**
     * Always capture the {@code Awaitable} returned by async methods.
     * Uncaptured awaitables may lose exceptions silently.
     */
    @Test
    void testFireAndForget_CaptureAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.atomic.AtomicBoolean

            def completed = new AtomicBoolean(false)
            def latch = new CountDownLatch(1)
            def myCompleted = completed
            def myLatch = latch

            // Use async closure to capture local variables
            def task = async {
                await Awaitable.delay(10)
                myCompleted.set(true)
                myLatch.countDown()
                return "done"
            }

            // GOOD: capture the Awaitable for tracking
            def awaitable = task()

            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            assert completed.get()
            assert await(awaitable) == "done"
        '''
    }

    @Test
    void testFireAndForget_ExceptionTracking() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            class BackgroundService {
                async failingBackground() {
                    await Awaitable.delay(10)
                    throw new RuntimeException("background failure")
                }
            }

            def service = new BackgroundService()

            // GOOD: capture so we can detect the failure
            def task = service.failingBackground()

            // Wait for completion, then check the error
            Thread.sleep(200)
            assert task.isCompletedExceptionally()

            try {
                await(task)
                assert false : "should have thrown"
            } catch (RuntimeException e) {
                assert e.message == "background failure"
            }
        '''
    }

    // =========================================================================
    // 12. Parallel execution with awaitAll
    // =========================================================================

    /**
     * Use {@code awaitAll} to run independent operations concurrently, then
     * collect all results. This is the equivalent of C#'s {@code Task.WhenAll}.
     */
    @Test
    void testParallelExecution_AwaitAll() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async fetchUser() {
                await Awaitable.delay(10)
                return "User-1"
            }

            async fetchOrders() {
                await Awaitable.delay(10)
                return "Orders-5"
            }

            async fetchSettings() {
                await Awaitable.delay(10)
                return "Theme-Dark"
            }

            // GOOD: run all three concurrently
            def userTask = fetchUser()
            def ordersTask = fetchOrders()
            def settingsTask = fetchSettings()

            def results = await(Awaitable.all(userTask, ordersTask, settingsTask))
            assert results == ["User-1", "Orders-5", "Theme-Dark"]
        '''
    }

    @Test
    void testParallelExecution_AwaitAllSettled() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitResult

            async successOp() {
                await Awaitable.delay(10)
                return "ok"
            }

            async failOp() {
                await Awaitable.delay(10)
                throw new RuntimeException("fail")
            }

            def results = await(Awaitable.allSettled(successOp(), failOp()))
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].value == "ok"
            assert results[1].isFailure()
            assert results[1].error.message == "fail"
        '''
    }

    // =========================================================================
    // 13. Cancellation propagation
    // =========================================================================

    /**
     * Always propagate cancellation through the async call chain.
     */
    @Test
    void testCancellationPropagation() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async longRunning() {
                await Awaitable.delay(10000)
                return "done"
            }

            def task = longRunning()
            Thread.sleep(50)
            task.cancel()

            assert task.isCancelled()
            assert task.isCompletedExceptionally()
        '''
    }

    @Test
    void testCancellationPropagation_WithCleanup() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicBoolean

            class CancelCleanupDemo {
                final AtomicBoolean cleanedUp = new AtomicBoolean(false)

                async longRunningWithCleanup() {
                    defer { cleanedUp.set(true) }
                    await Awaitable.delay(10000)
                    return "done"
                }
            }

            def demo = new CancelCleanupDemo()
            def task = demo.longRunningWithCleanup()
            Thread.sleep(100)
            task.cancel()
            Thread.sleep(100)

            assert task.isCancelled()
        '''
    }

    // =========================================================================
    // 14. Exception handling in async chains
    // =========================================================================

    /**
     * Use try/catch in async methods for clean exception handling, similar
     * to synchronous code.
     */
    @Test
    void testExceptionHandling_TryCatchInAsync() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async riskyFetch(boolean shouldFail) {
                await Awaitable.delay(10)
                if (shouldFail) throw new IOException("network error")
                return "data"
            }

            async resilientFetch() {
                try {
                    return await riskyFetch(true)
                } catch (IOException e) {
                    return "fallback"
                }
            }

            assert await(resilientFetch()) == "fallback"
            assert await(riskyFetch(false)) == "data"
        '''
    }

    @Test
    void testExceptionHandling_ExceptionallyRecovery() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async failingOp() {
                throw new RuntimeException("oops")
            }

            // Use Awaitable.exceptionally() for inline recovery
            def recovered = failingOp().exceptionally { ex -> "recovered from: ${ex.message}" }
            assert await(recovered) == "recovered from: oops"
        '''
    }

    // =========================================================================
    // 15. Mixing async and synchronous code
    // =========================================================================

    /**
     * Demonstrates the recommended pattern of keeping synchronous computation
     * synchronous and only using async for truly asynchronous operations.
     */
    @Test
    void testMixingSyncAndAsync() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            // Synchronous computation — no need for async
            int add(int a, int b) { a + b }

            // Truly asynchronous I/O simulation
            async fetchRemoteValue() {
                await Awaitable.delay(10)
                return 100
            }

            async compute() {
                def remote = await fetchRemoteValue()
                // GOOD: synchronous work stays synchronous
                def local = add(remote, 50)
                return local
            }

            assert await(compute()) == 150
        '''
    }

    @Test
    void testMixingSyncAndAsync_ConditionalAsync() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.Awaitable

            async getValueAsync(boolean useCache) {
                if (useCache) {
                    // No async operation needed — return pre-computed value
                    return 42
                }
                // Truly async path
                await Awaitable.delay(10)
                return 42
            }

            // Both paths return the same result
            assert await(getValueAsync(true)) == 42
            assert await(getValueAsync(false)) == 42
        '''
    }

    // =========================================================================
    // 16. Structured concurrency patterns
    // =========================================================================

    /**
     * Demonstrates structured concurrency: start multiple tasks, await them
     * all, and aggregate results.
     */
    @Test
    void testStructuredConcurrency_FanOutFanIn() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async processChunk(int chunkId, List<Integer> data) {
                await Awaitable.delay(10)
                return data.sum() * chunkId
            }

            async processAll(List<Integer> data) {
                def chunks = data.collate(3)
                def tasks = chunks.withIndex().collect { chunk, idx ->
                    processChunk(idx + 1, chunk)
                }
                def results = await(Awaitable.all(*tasks))
                return results.sum()
            }

            def data = [1, 2, 3, 4, 5, 6, 7, 8, 9]
            def total = await(processAll(data))
            // chunks: [1,2,3]=6, [4,5,6]=15, [7,8,9]=24
            // results: 6*1=6, 15*2=30, 24*3=72
            assert total == 108
        '''
    }

    @Test
    void testStructuredConcurrency_RetryPattern() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            class RetryDemo {
                final AtomicInteger attempts = new AtomicInteger(0)

                async unreliableOp() {
                    await Awaitable.delay(5)
                    if (attempts.incrementAndGet() < 3) {
                        throw new IOException("transient failure")
                    }
                    return "success"
                }
            }

            async withRetry(int maxRetries, Closure<groovy.concurrent.Awaitable> op) {
                for (int i = 0; i <= maxRetries; i++) {
                    try {
                        return await op()
                    } catch (IOException e) {
                        if (i == maxRetries) throw e
                        await Awaitable.delay(10)
                    }
                }
            }

            def demo = new RetryDemo()
            def result = await(withRetry(3, { demo.unreliableOp() }))
            assert result == "success"
            assert demo.attempts.get() == 3
        '''
    }

    // =========================================================================
    // 17. Async generators with for-await
    // =========================================================================

    /**
     * Use {@code for await} to consume asynchronous data streams, similar to
     * C#'s {@code await foreach} or JavaScript's {@code for await...of}.
     */
    @Test
    void testAsyncGenerator_ForAwaitConsumption() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async generatePages() {
                def results = []
                for await (page in asyncStream()) {
                    results << page
                }
                return results
            }

            async Iterable asyncStream() {
                yield return "page-1"
                await Awaitable.delay(10)
                yield return "page-2"
                await Awaitable.delay(10)
                yield return "page-3"
            }

            assert await(generatePages()) == ["page-1", "page-2", "page-3"]
        '''
    }

    // =========================================================================
    // 18. Defer for cleanup (Go-style)
    // =========================================================================

    /**
     * Use {@code defer} for guaranteed cleanup, analogous to Go's defer.
     * Defer actions execute in LIFO order when the method returns.
     */
    @Test
    void testDeferCleanup() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class DeferDemo {
                final List log = Collections.synchronizedList([])

                async processWithCleanup() {
                    log << "start"
                    defer { log << "cleanup-2" }
                    defer { log << "cleanup-1" }
                    await Awaitable.delay(10)
                    log << "work-done"
                    return "result"
                }
            }

            def demo = new DeferDemo()
            assert await(demo.processWithCleanup()) == "result"
            assert demo.log == ["start", "work-done", "cleanup-1", "cleanup-2"]
        '''
    }

    @Test
    void testDeferCleanup_EvenOnException() {
        assertScript '''
            class DeferExceptionDemo {
                final List log = Collections.synchronizedList([])

                async riskyWork() {
                    defer { log << "cleaned-up" }
                    throw new RuntimeException("fail")
                }
            }

            def demo = new DeferExceptionDemo()
            try {
                await(demo.riskyWork())
            } catch (RuntimeException e) {
                assert e.message == "fail"
            }

            Thread.sleep(100)
            assert demo.log.contains("cleaned-up")
        '''
    }
}
