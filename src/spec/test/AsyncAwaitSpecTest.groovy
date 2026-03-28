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

/**
 * Spec tests for async/await documentation.
 * <p>
 * Tagged code snippets in this file are referenced by {@code core-async-await.adoc}
 * via AsciiDoc {@code include::} directives.
 */
class AsyncAwaitSpecTest {

    // =========================================================================
    // 1. Basic async methods
    // =========================================================================

    @Test
    void testAsyncMethodBasic() {
        assertScript '''
// tag::async_method_basic[]
import groovy.concurrent.Awaitable

class GreetingService {
    async greet(String name) {
        return "Hello, ${name}!"
    }
}

def service = new GreetingService()
def awaitable = service.greet("World")
assert awaitable instanceof Awaitable
assert awaitable.get() == "Hello, World!"
// end::async_method_basic[]
        '''
    }

    @Test
    void testAsyncMethodTyped() {
        assertScript '''
// tag::async_method_typed[]
class MathService {
    async int square(int n) { n * n }
    async String upper(String s) { s.toUpperCase() }
}

def svc = new MathService()
assert svc.square(7).get() == 49
assert svc.upper("groovy").get() == "GROOVY"
// end::async_method_typed[]
        '''
    }

    @Test
    void testAsyncMethodVoid() {
        assertScript '''
// tag::async_method_void[]
import groovy.concurrent.Awaitable
import java.util.concurrent.atomic.AtomicReference

class Logger {
    AtomicReference<String> lastMessage = new AtomicReference<>()

    async void log(String msg) {
        lastMessage.set(msg)
    }
}

def logger = new Logger()
def a = logger.log("event occurred")
assert a instanceof Awaitable
a.get()
assert logger.lastMessage.get() == "event occurred"
// end::async_method_void[]
        '''
    }

    @Test
    void testAsyncMethodStatic() {
        assertScript '''
// tag::async_method_static[]
class Util {
    async static int add(int a, int b) { a + b }
}

assert Util.add(3, 4).get() == 7
// end::async_method_static[]
        '''
    }

    @Test
    void testAsyncScript() {
        assertScript '''
// tag::async_script[]
async multiply(int a, int b) { a * b }

def result = multiply(6, 7)
assert result.get() == 42
// end::async_script[]
        '''
    }

    // =========================================================================
    // 2. await expression
    // =========================================================================

    @Test
    void testAwaitBasic() {
        assertScript '''
// tag::await_basic[]
class DataService {
    async fetchData() { "database result" }

    async process() {
        def data = await fetchData()
        return "Processed: ${data}"
    }
}

def result = await new DataService().process()
assert result == "Processed: database result"
// end::await_basic[]
        '''
    }

    @Test
    void testAwaitCompletableFuture() {
        assertScript '''
// tag::await_completable_future[]
import java.util.concurrent.CompletableFuture

class Service {
    async compute() {
        def value = await CompletableFuture.supplyAsync { 42 }
        return value * 2
    }
}

assert await(new Service().compute()) == 84
// end::await_completable_future[]
        '''
    }

    @Test
    void testAwaitArithmetic() {
        assertScript '''
// tag::await_arithmetic[]
import java.util.concurrent.CompletableFuture

class Calculator {
    async sum() {
        def a = await CompletableFuture.supplyAsync { 10 }
        def b = await CompletableFuture.supplyAsync { 20 }
        return a + b
    }
}

assert await(new Calculator().sum()) == 30
// end::await_arithmetic[]
        '''
    }

    @Test
    void testAwaitParenthesized() {
        assertScript '''
// tag::await_parenthesized[]
import java.util.concurrent.CompletableFuture

async compute() {
    // Both forms are equivalent:
    def a = await CompletableFuture.supplyAsync { 10 }
    def b = await CompletableFuture.supplyAsync { 20 }
    return a + b
}

assert await(compute()) == 30
// end::await_parenthesized[]
        '''
    }

    // =========================================================================
    // 3. Async closures and lambdas
    // =========================================================================

    @Test
    void testAsyncClosureBasic() {
        assertScript '''
// tag::async_closure_basic[]
import groovy.concurrent.Awaitable

// async { ... } creates a Closure that returns an Awaitable
def asyncTask = async { 1 + 2 }
assert asyncTask instanceof Closure

// Explicit invocation is required to start execution
def awaitable = asyncTask()
assert awaitable instanceof Awaitable
assert await(awaitable) == 3
// end::async_closure_basic[]
        '''
    }

    @Test
    void testAsyncClosureWithParams() {
        assertScript '''
// tag::async_closure_params[]
def asyncSquare = async { int n -> n * n }

// Call with argument, then await the result
assert await(asyncSquare(5)) == 25
assert await(asyncSquare(7)) == 49
// end::async_closure_params[]
        '''
    }

    @Test
    void testAsyncClosureMultipleParams() {
        assertScript '''
// tag::async_closure_multi_params[]
def asyncConcat = async { a, b, sep -> "${a}${sep}${b}" }
assert await(asyncConcat("hello", "world", " ")) == "hello world"
// end::async_closure_multi_params[]
        '''
    }

    @Test
    void testAsyncClosureInCollection() {
        assertScript '''
// tag::async_closure_collection[]
def ops = [
    add: async { a, b -> a + b },
    mul: async { a, b -> a * b },
    sub: async { a, b -> a - b }
]

assert await(ops.add(3, 4)) == 7
assert await(ops.mul(3, 4)) == 12
assert await(ops.sub(10, 4)) == 6
// end::async_closure_collection[]
        '''
    }

    @Test
    void testAsyncLambda() {
        assertScript '''
// tag::async_lambda[]
def asyncDouble = async (n) -> { n * 2 }
assert await(asyncDouble(21)) == 42

def asyncGreet = async (name) -> { "Hello, ${name}!" }
assert await(asyncGreet("Groovy")) == "Hello, Groovy!"
// end::async_lambda[]
        '''
    }

    // =========================================================================
    // 4. for await — async iteration
    // =========================================================================

    @Test
    void testForAwaitBasic() {
        assertScript '''
// tag::for_await_basic[]
class Collector {
    async collectItems() {
        def results = []
        for await (item in [10, 20, 30]) {
            results << item
        }
        return results
    }
}

assert await(new Collector().collectItems()) == [10, 20, 30]
// end::for_await_basic[]
        '''
    }

    @Test
    void testForAwaitWithTransform() {
        assertScript '''
// tag::for_await_transform[]
class Transformer {
    async doubleAll() {
        def results = []
        for await (item in [1, 2, 3, 4]) {
            results << item * 2
        }
        return results
    }
}

assert await(new Transformer().doubleAll()) == [2, 4, 6, 8]
// end::for_await_transform[]
        '''
    }

    @Test
    void testForAwaitWithBreak() {
        assertScript '''
// tag::for_await_break[]
class Finder {
    async findFirstOver(int threshold) {
        for await (item in [5, 10, 15, 20, 25]) {
            if (item > threshold) return item
        }
        return -1
    }
}

assert await(new Finder().findFirstOver(12)) == 15
// end::for_await_break[]
        '''
    }

    // =========================================================================
    // 5. yield return — async generators
    // =========================================================================

    @Test
    void testYieldReturnBasic() {
        assertScript '''
// tag::yield_return_basic[]
import groovy.concurrent.AsyncStream

class NumberGenerator {
    async numbers() {
        yield return 1
        yield return 2
        yield return 3
    }
}

def stream = new NumberGenerator().numbers()
assert stream instanceof AsyncStream

def results = []
for await (n in stream) {
    results << n
}
assert results == [1, 2, 3]
// end::yield_return_basic[]
        '''
    }

    @Test
    void testYieldReturnLoop() {
        assertScript '''
// tag::yield_return_loop[]
class RangeGenerator {
    async range(int start, int end) {
        for (int i = start; i <= end; i++) {
            yield return i
        }
    }
}

def results = []
for await (n in new RangeGenerator().range(1, 5)) {
    results << n
}
assert results == [1, 2, 3, 4, 5]
// end::yield_return_loop[]
        '''
    }

    @Test
    void testYieldReturnFilter() {
        assertScript '''
// tag::yield_return_filter[]
class FilteredGenerator {
    async evenNumbers(int max) {
        for (int i = 1; i <= max; i++) {
            if (i % 2 == 0) {
                yield return i
            }
        }
    }
}

def results = []
for await (n in new FilteredGenerator().evenNumbers(10)) {
    results << n
}
assert results == [2, 4, 6, 8, 10]
// end::yield_return_filter[]
        '''
    }

    @Test
    void testYieldReturnWithAwait() {
        assertScript '''
// tag::yield_return_with_await[]
import java.util.concurrent.CompletableFuture

class AsyncDataGenerator {
    async fetchSequence() {
        for (int i = 1; i <= 3; i++) {
            def value = await CompletableFuture.supplyAsync { i * 10 }
            yield return value
        }
    }
}

def results = []
for await (v in new AsyncDataGenerator().fetchSequence()) {
    results << v
}
assert results == [10, 20, 30]
// end::yield_return_with_await[]
        '''
    }

    // =========================================================================
    // 6. defer — Go-style cleanup
    // =========================================================================

    @Test
    void testDeferBasic() {
        assertScript '''
// tag::defer_basic[]
class DeferExample {
    async runWithDefer() {
        def log = []
        defer { log << "first deferred" }
        defer { log << "second deferred" }
        defer { log << "third deferred" }
        log << "body executed"
        return log
    }
}

def result = await new DeferExample().runWithDefer()
// Body runs first; deferred blocks execute in LIFO order
assert result == ["body executed", "third deferred", "second deferred", "first deferred"]
// end::defer_basic[]
        '''
    }

    @Test
    void testDeferOnException() {
        assertScript '''
// tag::defer_exception[]
class ResourceHandler {
    static log = []

    async processWithCleanup() {
        defer { log << "cleanup done" }
        throw new RuntimeException("something went wrong")
    }
}

try {
    await new ResourceHandler().processWithCleanup()
} catch (RuntimeException e) {
    assert e.message == "something went wrong"
}
// Deferred blocks always execute, even on exception
assert ResourceHandler.log == ["cleanup done"]
// end::defer_exception[]
        '''
    }

    @Test
    void testDeferResourceCleanup() {
        assertScript '''
// tag::defer_resource_cleanup[]
class ResourceManager {
    static resources = []
    static cleanupLog = []

    async processResources() {
        def r1 = "database-conn"
        resources << r1
        defer { resources.remove(r1); cleanupLog << "closed ${r1}" }

        def r2 = "file-handle"
        resources << r2
        defer { resources.remove(r2); cleanupLog << "closed ${r2}" }

        assert resources.size() == 2
        return "done"
    }
}

assert await(new ResourceManager().processResources()) == "done"
// LIFO order: r2 closed first, then r1
assert ResourceManager.cleanupLog == ["closed file-handle", "closed database-conn"]
assert ResourceManager.resources.isEmpty()
// end::defer_resource_cleanup[]
        '''
    }

    // =========================================================================
    // 7. Exception handling
    // =========================================================================

    @Test
    void testExceptionTransparency() {
        assertScript '''
// tag::exception_transparency[]
async fetchData() {
    throw new java.io.IOException("disk failure")
}

async caller() {
    try {
        await fetchData()
        assert false : "should not reach here"
    } catch (java.io.IOException e) {
        return "Recovered: ${e.message}"
    }
}

assert await(caller()) == "Recovered: disk failure"
// end::exception_transparency[]
        '''
    }

    @Test
    void testExceptionMultipleTasks() {
        assertScript '''
// tag::exception_multiple_tasks[]
async riskyTask(boolean shouldFail) {
    if (shouldFail) throw new IllegalStateException("task failed")
    return "success"
}

async coordinator() {
    def results = []
    for (flag in [false, true, false]) {
        try {
            results << await riskyTask(flag)
        } catch (IllegalStateException e) {
            results << "error: ${e.message}"
        }
    }
    return results
}

assert await(coordinator()) == ["success", "error: task failed", "success"]
// end::exception_multiple_tasks[]
        '''
    }

    // =========================================================================
    // 8. Utility methods — awaitAll, awaitAny, awaitAllSettled, delay
    // =========================================================================

    @Test
    void testAwaitAll() {
        assertScript '''
// tag::await_all[]
import groovy.concurrent.Awaitable

async fetchUser() { "Alice" }
async fetchOrder() { "Order#42" }
async fetchBalance() { 100.0 }

def results = await Awaitable.all(fetchUser(), fetchOrder(), fetchBalance())
assert results == ["Alice", "Order#42", 100.0]
// end::await_all[]
        '''
    }

    @Test
    void testAwaitAllFailFast() {
        assertScript '''
// tag::await_all_fail_fast[]
import groovy.concurrent.Awaitable

import java.io.IOException
import java.util.concurrent.TimeUnit

async slowCall() {
    await Awaitable.delay(5_000)
    return "slow result"
}

async caller() {
    def started = System.nanoTime()
    try {
        await Awaitable.all(slowCall(), Awaitable.failed(new IOException("network error")))
        assert false : "should fail fast"
    } catch (IOException e) {
        def elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
        assert elapsedMillis < 2_000
        return e.message
    }
}

assert await(caller()) == "network error"
// end::await_all_fail_fast[]
        '''
    }

    @Test
    void testAwaitAny() {
        assertScript '''
// tag::await_any[]
import groovy.concurrent.Awaitable

def fast = async { "fast result" }
def slow = async {
    await Awaitable.delay(2000)
    return "slow result"
}

def winner = await Awaitable.any(fast(), slow())
assert winner == "fast result"
// end::await_any[]
        '''
    }

    @Test
    void testAwaitAllSettled() {
        assertScript '''
// tag::await_all_settled[]
import groovy.concurrent.Awaitable

async caller() {
    def a = Awaitable.of(1)
    def b = Awaitable.failed(new IOException("network error"))
    def c = Awaitable.of(3)
    return await Awaitable.allSettled(a, b, c)
}

def results = await caller()
assert results.size() == 3

assert results[0].isSuccess() && results[0].value == 1
assert results[1].isFailure() && results[1].error.message == "network error"
assert results[2].isSuccess() && results[2].value == 3
// end::await_all_settled[]
        '''
    }

    @Test
    void testDelay() {
        assertScript '''
// tag::delay_example[]
import groovy.concurrent.Awaitable

async delayedGreeting() {
    await Awaitable.delay(100)  // pause for 100 milliseconds
    return "Hello after delay"
}

assert await(delayedGreeting()) == "Hello after delay"
// end::delay_example[]
        '''
    }

    @Test
    void testContinuationHelpers() {
        assertScript '''
// tag::continuation_helpers[]
import groovy.concurrent.Awaitable
import java.util.concurrent.atomic.AtomicReference

def observed = new AtomicReference()
await Awaitable.of("groovy").thenAccept { observed.set(it.toUpperCase()) }
assert observed.get() == "GROOVY"

def recovered = await 
    Awaitable.failed(new IOException("boom"))
        .handle { value, error -> "recovered from ${error.message}" }

assert recovered == "recovered from boom"

def completion = new AtomicReference()
assert await(Awaitable.of(7).whenComplete { value, error ->
    completion.set("${value}:${error}")
}) == 7
assert completion.get() == "7:null"
// end::continuation_helpers[]
        '''
    }

    @Test
    void testTimeoutCombinators() {
        assertScript '''
// tag::timeout_combinators[]
import groovy.concurrent.Awaitable
import java.util.concurrent.TimeoutException

async slowCall() {
    await Awaitable.delay(5_000)
    return "done"
}

try {
    await Awaitable.orTimeoutMillis(slowCall(), 50)
    assert false : "should have timed out"
} catch (TimeoutException e) {
    assert e.message.contains("Timed out after 50")
}

assert await(Awaitable.completeOnTimeoutMillis(slowCall(), "cached", 50)) == "cached"
// end::timeout_combinators[]
        '''
    }

    // =========================================================================
    // 9. Flow.Publisher integration
    // =========================================================================

    @Test
    void testFlowPublisherAwait() {
        assertScript '''
// tag::flow_publisher_await[]
import java.util.concurrent.SubmissionPublisher

def publisher = new SubmissionPublisher<String>()
def waitForSubscriber = { SubmissionPublisher pub, long timeoutMillis = 5_000L ->
    long deadline = System.nanoTime() + timeoutMillis * 1_000_000L
    while (pub.numberOfSubscribers == 0 && System.nanoTime() < deadline) {
        Thread.sleep(1)
    }
    assert pub.numberOfSubscribers > 0 : "Timed out waiting for publisher subscription"
}
Thread.start {
    waitForSubscriber(publisher)
    publisher.submit("hello from publisher")
    publisher.close()
}

def result = await publisher
assert result == "hello from publisher"
// end::flow_publisher_await[]
        '''
    }

    @Test
    void testFlowPublisherForAwait() {
        assertScript '''
// tag::flow_publisher_for_await[]
import java.util.concurrent.SubmissionPublisher

class StreamConsumer {
    async consumeAll(SubmissionPublisher<Integer> pub) {
        def results = []
        for await (item in pub) {
            results << item
        }
        return results
    }
}

def publisher = new SubmissionPublisher<Integer>()
def future = new StreamConsumer().consumeAll(publisher)
def waitForSubscriber = { SubmissionPublisher pub, long timeoutMillis = 5_000L ->
    long deadline = System.nanoTime() + timeoutMillis * 1_000_000L
    while (pub.numberOfSubscribers == 0 && System.nanoTime() < deadline) {
        Thread.sleep(1)
    }
    assert pub.numberOfSubscribers > 0 : "Timed out waiting for publisher subscription"
}
Thread.start {
    waitForSubscriber(publisher)
    (1..5).each { publisher.submit(it) }
    publisher.close()
}

assert await(future) == [1, 2, 3, 4, 5]
// end::flow_publisher_for_await[]
        '''
    }

    // =========================================================================
    // 10. @Async annotation
    // =========================================================================

    @Test
    void testAsyncAnnotation() {
        assertScript '''
// tag::async_annotation[]
import groovy.transform.Async
import groovy.concurrent.Awaitable

class Service {
    @Async
    def fetchData() {
        return "data loaded"
    }
}

def svc = new Service()
def awaitable = svc.fetchData()
assert awaitable instanceof Awaitable
assert awaitable.get() == "data loaded"
// end::async_annotation[]
        '''
    }

    @Test
    void testAsyncAnnotationWithExecutor() {
        assertScript '''
// tag::async_annotation_executor[]
import groovy.transform.Async
import groovy.concurrent.Awaitable
import java.util.concurrent.Executors

class DataService {
    private final myPool = Executors.newFixedThreadPool(4)

    @Async(executor = "myPool")
    def fetchData() {
        return Thread.currentThread().name
    }
}

def svc = new DataService()
def name = svc.fetchData().get()
assert name.startsWith('pool-')
// end::async_annotation_executor[]
        '''
    }

    @Test
    void testChannelRacingPattern() {
        assertScript '''
// tag::channel_racing[]
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncChannel

async def racingExample() {
    def ch = AsyncChannel.create(1)

    Awaitable.go {
        await Awaitable.delay(50)
        await ch.send('data arrived')
    }

    def winner = await Awaitable.any(
        ch.receive(),                               // channel receive
        Awaitable.delay(1000).then { 'timeout' }    // timeout fallback
    )

    assert winner == 'data arrived'
    winner
}

assert racingExample().get() == 'data arrived'
// end::channel_racing[]
        '''
    }

    // =========================================================================
    // 11. Custom executor
    // =========================================================================

    @Test
    void testCustomExecutor() {
        assertScript '''
// tag::custom_executor[]
import groovy.concurrent.Awaitable
import java.util.concurrent.Executors

class WorkService {
    async work() { Thread.currentThread().name }
}

def customPool = Executors.newFixedThreadPool(2)
try {
    Awaitable.setExecutor(customPool)

    def threadName = await new WorkService().work()
    assert threadName.contains("pool")
} finally {
    Awaitable.setExecutor(null)  // restore default
    customPool.shutdown()
}
// end::custom_executor[]
        '''
    }

    // =========================================================================
    // 12. Comprehensive pattern — parallel web scraping
    // =========================================================================

    @Test
    void testParallelPattern() {
        assertScript '''
// tag::parallel_pattern[]
import groovy.concurrent.Awaitable

async fetchPage(String url) {
    await Awaitable.delay(10)  // simulate network I/O
    return "Content of ${url}"
}

// Launch three tasks in parallel
def pages = await Awaitable.all(
    fetchPage("https://example.com/page1"),
    fetchPage("https://example.com/page2"),
    fetchPage("https://example.com/page3")
)

assert pages.size() == 3
assert pages.every { it.startsWith("Content of") }
// end::parallel_pattern[]
        '''
    }

    // =========================================================================
    // 13. Common patterns — retry, timeout, cancellation
    // =========================================================================

    @Test
    void testRetryPattern() {
        assertScript '''
// tag::retry_pattern[]
import groovy.concurrent.Awaitable
import java.util.concurrent.atomic.AtomicInteger

class RetryDemo {
    final AtomicInteger attempts = new AtomicInteger(0)

    async fetchData() {
        if (attempts.incrementAndGet() < 3) throw new IOException("transient error")
        return "data"
    }

    async fetchWithRetry(int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return await fetchData()
            } catch (Exception e) {
                if (attempt == maxRetries) throw e
                await Awaitable.delay(10 * attempt)  // exponential backoff
            }
        }
    }
}

def demo = new RetryDemo()
assert await(demo.fetchWithRetry(5)) == "data"
assert demo.attempts.get() == 3
// end::retry_pattern[]
        '''
    }

    @Test
    void testTimeoutPattern() {
        assertScript '''
// tag::timeout_pattern[]
import groovy.concurrent.Awaitable
import java.util.concurrent.TimeoutException

async longRunningTask() {
    await Awaitable.delay(5000)
    return "completed"
}

try {
    await Awaitable.orTimeoutMillis(longRunningTask(), 50)
    assert false : "should have timed out"
} catch (TimeoutException e) {
    assert e.message.contains("Timed out after 50")
}
// end::timeout_pattern[]
        '''
    }

    @Test
    void testCancellation() {
        assertScript '''
// tag::cancellation[]
import groovy.concurrent.Awaitable
import java.util.concurrent.CancellationException

async longRunningTask() {
    await Awaitable.delay(10_000)
    return "done"
}

def task = longRunningTask()
task.cancel()

try {
    await task
    assert false : "should have thrown"
} catch (CancellationException e) {
    assert task.isCancelled()
}
// end::cancellation[]
        '''
    }

    // =========================================================================
    // 14. Best practices — Awaitable.of, async caching, static factory
    // =========================================================================

    @Test
    void testAwaitableOf() {
        assertScript '''
// tag::awaitable_of[]
import groovy.concurrent.Awaitable

// No thread spawned — result is immediately available
Awaitable<Integer> addAsync(int a, int b) {
    return Awaitable.of(a + b)
}

assert await(addAsync(2, 3)) == 5

// Pre-computed error result
Awaitable<String> failFast() {
    return Awaitable.failed(new IllegalStateException("not ready"))
}

try {
    await failFast()
    assert false
} catch (IllegalStateException e) {
    assert e.message == "not ready"
}
// end::awaitable_of[]
        '''
    }

    @Test
    void testAsyncCaching() {
        assertScript '''
// tag::async_caching[]
import groovy.concurrent.Awaitable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CacheDemo {
    final AtomicInteger fetchCount = new AtomicInteger(0)
    final cache = new ConcurrentHashMap<Integer, Awaitable>()

    async fetchPersonAsync(int id) {
        fetchCount.incrementAndGet()
        await Awaitable.delay(10)
        return "Person-${id}"
    }

    def getOrFetch(int id) {
        cache.computeIfAbsent(id, { key -> fetchPersonAsync(key) })
    }
}

def demo = new CacheDemo()

// First call triggers actual fetch
def p1 = await demo.getOrFetch(42)
// Second call returns cached Awaitable (no new fetch)
def p2 = await demo.getOrFetch(42)

assert p1 == "Person-42"
assert p2 == "Person-42"
assert demo.fetchCount.get() == 1  // fetched only once
// end::async_caching[]
        '''
    }

    @Test
    void testStaticFactoryPattern() {
        assertScript '''
// tag::static_factory[]
import groovy.concurrent.Awaitable

class RemoteService {
    final String connectionId
    private RemoteService(String connectionId) { this.connectionId = connectionId }

    static async create() {
        def connId = await connectToRemote()
        return new RemoteService(connId)
    }

    private static async connectToRemote() {
        await Awaitable.delay(10)  // simulate I/O
        return "conn-" + System.nanoTime()
    }
}

def service = await RemoteService.create()
assert service.connectionId.startsWith("conn-")
// end::static_factory[]
        '''
    }

    // =========================================================================
    // 15. Best practices — sync-over-async, prefer await, fire-and-forget
    // =========================================================================

    @Test
    void testSyncOverAsync() {
        assertScript '''
// tag::sync_over_async[]
import groovy.concurrent.Awaitable

class ConsumerService {
    // BAD: blocking sync-over-async
    // def result = computeAsync(5).get()

    // GOOD: propagate await through the call chain
    async computeAsync(int n) {
        await Awaitable.delay(10)
        return n * 2
    }

    async doSomething() {
        def result = await computeAsync(5)
        return result + 1
    }
}

def svc = new ConsumerService()
assert await(svc.doSomething()) == 11
// end::sync_over_async[]
        '''
    }

    @Test
    void testPreferAwaitOverThen() {
        assertScript '''
// tag::prefer_await_over_then[]
import groovy.concurrent.Awaitable

class PipelineDemo {
    async step1() { await Awaitable.delay(5); return 10 }
    async step2(int n) { await Awaitable.delay(5); return n * 2 }
    async step3(int n) { await Awaitable.delay(5); return n + 3 }

    // More readable: sequential awaits
    async pipeline() {
        def a = await step1()
        def b = await step2(a)
        return await step3(b)
    }
}

def demo = new PipelineDemo()
assert await(demo.pipeline()) == 23
// end::prefer_await_over_then[]
        '''
    }

    @Test
    void testFireAndForget() {
        assertScript '''
// tag::fire_and_forget[]
import groovy.concurrent.Awaitable

def errors = Collections.synchronizedList([])

async backgroundOperation() {
    await Awaitable.delay(10)
    throw new RuntimeException("background error")
}

// GOOD: capture for tracking
def task = backgroundOperation()
Thread.sleep(100)  // wait for completion
assert task.isCompletedExceptionally()
// end::fire_and_forget[]
        '''
    }

    @Test
    void testPitfallBlockingGetBad() {
        assertScript '''
// tag::pitfall_blocking_get_bad[]
import java.util.concurrent.CompletableFuture

class BlockingClient {
    async fetch() {
        def future = CompletableFuture.completedFuture("payload")
        return future.get()  // works, but blocks the async thread
    }
}

assert await(new BlockingClient().fetch()) == "payload"
// end::pitfall_blocking_get_bad[]
        '''
    }

    @Test
    void testPitfallBlockingGetGood() {
        assertScript '''
// tag::pitfall_blocking_get_good[]
import java.util.concurrent.CompletableFuture

class NonBlockingClient {
    async fetch() {
        def future = CompletableFuture.completedFuture("payload")
        return await future
    }
}

assert await(new NonBlockingClient().fetch()) == "payload"
// end::pitfall_blocking_get_good[]
        '''
    }

    @Test
    void testPitfallMissingAwaitBad() {
        assertScript '''
// tag::pitfall_missing_await_bad[]
import groovy.concurrent.Awaitable
import java.util.Collections

def events = Collections.synchronizedList([])

def save = async {
    await Awaitable.delay(10)
    events << "saved"
}

save()  // returned Awaitable is discarded
Thread.sleep(100)
assert events == ["saved"]
// end::pitfall_missing_await_bad[]
        '''
    }

    @Test
    void testPitfallMissingAwaitGood() {
        assertScript '''
// tag::pitfall_missing_await_good[]
import groovy.concurrent.Awaitable

async save() {
    await Awaitable.delay(10)
    return "saved"
}

def task = save()
assert await(task) == "saved"
// end::pitfall_missing_await_good[]
        '''
    }

    @Test
    void testPitfallNullChannelBad() {
        assertScript '''
// tag::pitfall_null_channel_bad[]
import groovy.concurrent.AsyncChannel

def ch = AsyncChannel.create(1)
try {
    await ch.send(null)
    assert false : "should have thrown"
} catch (NullPointerException ignored) {
    assert true
} finally {
    ch.close()
}
// end::pitfall_null_channel_bad[]
        '''
    }

    @Test
    void testPitfallNullChannelGood() {
        assertScript '''
// tag::pitfall_null_channel_good[]
import groovy.concurrent.AsyncChannel
import java.util.Optional

def ch = AsyncChannel.create(1)

def produceEmptyValue = async {
    await ch.send(Optional.empty())
    ch.close()
}

await produceEmptyValue()
assert await(ch.receive()) == Optional.empty()
// end::pitfall_null_channel_good[]
        '''
    }

    @Test
    void testForAwaitColonNotation() {
        assertScript '''
// tag::for_await_colon[]
class ColonNotationDemo {
    async generateNumbers() {
        for (int i = 1; i <= 3; i++) {
            yield return i
        }
    }
}

def demo = new ColonNotationDemo()
def results = []
// for await supports both `in` and `:` notation
for await (n : demo.generateNumbers()) {
    results << n
}
assert results == [1, 2, 3]
// end::for_await_colon[]
        '''
    }

    // =========================================================================
    // 16. AwaitResult API
    // =========================================================================

    @Test
    void testAwaitResultApi() {
        assertScript '''
// tag::await_result_api[]
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitResult

async caller() {
    def a = Awaitable.of(42)
    def b = Awaitable.failed(new IOException("disk full"))
    return await Awaitable.allSettled(a, b)
}

def results = await caller()

// Inspect each outcome
AwaitResult success = results[0]
assert success.isSuccess()
assert success.value == 42
assert !success.isFailure()

AwaitResult failure = results[1]
assert failure.isFailure()
assert failure.error.message == "disk full"
assert !failure.isSuccess()

// getOrElse: recover from failures with a fallback function
assert success.getOrElse { -1 } == 42
assert failure.getOrElse { err -> "recovered: ${err.message}" } == "recovered: disk full"

// toString for debugging
assert success.toString() == "AwaitResult.Success[42]"
assert failure.toString().startsWith("AwaitResult.Failure[")
// end::await_result_api[]
        '''
    }

    // =========================================================================
    // 17. Instance timeout methods
    // =========================================================================

    @Test
    void testInstanceTimeoutMethods() {
        assertScript '''
// tag::instance_timeout_methods[]
import groovy.concurrent.Awaitable
import java.util.concurrent.TimeoutException

async slowTask() {
    await Awaitable.delay(5_000)
    return "done"
}

// orTimeout: fails with TimeoutException if not completed in time
try {
    await slowTask().orTimeoutMillis(50)
    assert false : "should have timed out"
} catch (TimeoutException e) {
    assert e.message.contains("50")
}

// completeOnTimeout: completes with a fallback value instead of failing
assert await(slowTask().completeOnTimeoutMillis("fallback", 50)) == "fallback"
// end::instance_timeout_methods[]
        '''
    }

    // =========================================================================
    // 18. Inspection methods
    // =========================================================================

    @Test
    void testInspectionMethods() {
        assertScript '''
// tag::inspection_methods[]
import groovy.concurrent.Awaitable

// Check state without blocking
def completed = Awaitable.of(42)
assert completed.isDone()
assert !completed.isCancelled()

def failed = Awaitable.failed(new RuntimeException("oops"))
assert failed.isDone()
assert failed.isCompletedExceptionally()

// Cancel an awaitable
def task = Awaitable.delay(10_000)
task.cancel()
assert task.isCancelled()
assert task.isDone()
// end::inspection_methods[]
        '''
    }

    // =========================================================================
    // 19. Type conversion with from()
    // =========================================================================

    @Test
    void testFromConversion() {
        assertScript '''
// tag::from_conversion[]
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncStream
import java.util.concurrent.CompletableFuture

// Convert a CompletableFuture to Awaitable
def cf = CompletableFuture.completedFuture("hello")
Awaitable<String> awaitable = Awaitable.from(cf)
assert awaitable.get() == "hello"

// If the source is already an Awaitable, it is returned as-is
def original = Awaitable.of(42)
assert Awaitable.from(original).is(original)

// Convert an Iterable to AsyncStream
AsyncStream<String> stream = AsyncStream.from(["a", "b", "c"])
def items = []
assert stream.moveNext().get() == true
items << stream.current
assert stream.moveNext().get() == true
items << stream.current
assert stream.moveNext().get() == true
items << stream.current
assert stream.moveNext().get() == false
assert items == ["a", "b", "c"]
// end::from_conversion[]
        '''
    }

    // =========================================================================
    // 20. Custom adapter registration
    // =========================================================================

    @Test
    void testAdapterRegistration() {
        assertScript '''
// tag::adapter_registration[]
import groovy.concurrent.AwaitableAdapterRegistry
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.Awaitable

// A simple custom async type (simulating a third-party framework)
class CustomFuture<T> {
    private final T value
    CustomFuture(T value) { this.value = value }
    T blockingGet() { return value }
}

// Register adapter for the custom type
def handle = AwaitableAdapterRegistry.register(new AwaitableAdapter() {
    boolean supportsAwaitable(Class<?> type) { CustomFuture.isAssignableFrom(type) }
    Awaitable toAwaitable(Object source) { Awaitable.of(((CustomFuture) source).blockingGet()) }
})

try {
    def future = new CustomFuture<String>("hello from adapter")
    assert await(future) == "hello from adapter"
} finally {
    handle.close()  // deregister when done
}
// end::adapter_registration[]
        '''
    }

    // =========================================================================
    // Motivation: Why Groovy Needs Async/Await
    // =========================================================================

    @Test
    void testMotivationCallbackHell() {
        assertScript '''
// tag::motivation_callback_hell[]
import java.util.concurrent.CompletableFuture

// ------ Before: chained CompletableFuture (callback style) ------
def result = CompletableFuture.supplyAsync { 42 }
        .thenCompose { userId ->
            CompletableFuture.supplyAsync { "User-${userId}" }
        }
        .thenCompose { userName ->
            CompletableFuture.supplyAsync { "${userName}: premium" }
        }
        .thenApply { profile ->
            profile.toUpperCase()
        }
        .exceptionally { ex ->
            "ERROR: ${ex.message}"
        }
        .get()

assert result == "USER-42: PREMIUM"
// end::motivation_callback_hell[]
        '''
    }

    @Test
    void testMotivationAsyncAwaitClean() {
        assertScript '''
// tag::motivation_async_await_clean[]
import groovy.concurrent.Awaitable

// ------ After: sequential async/await (same logic, flat and readable) ------
async def fetchProfile(int userId) {
    def name = await Awaitable.of("User-${userId}")
    def profile = await Awaitable.of("${name}: premium")
    return profile.toUpperCase()
}

assert await(fetchProfile(42)) == "USER-42: PREMIUM"
// end::motivation_async_await_clean[]
        '''
    }

    @Test
    void testMotivationExceptionHandling() {
        assertScript '''
// tag::motivation_exception_handling[]
import groovy.concurrent.Awaitable

async def riskyOperation() {
    throw new IOException("network timeout")
}

// Exception handling works exactly like synchronous code
try {
    await riskyOperation()
    assert false : "should not reach here"
} catch (IOException e) {
    assert e.message == "network timeout"       // <1>
}
// end::motivation_exception_handling[]
        '''
    }

    @Test
    void testMotivationStreamProcessing() {
        assertScript '''
// tag::motivation_stream_processing[]
import groovy.concurrent.Awaitable

// Async generator produces values on-demand with back-pressure
async def fibonacci(int count) {
    long a = 0, b = 1
    for (int i = 0; i < count; i++) {
        yield return a
        (a, b) = [b, a + b]
    }
}

def results = []
for await (num in fibonacci(8)) {
    results << num
}
assert results == [0, 1, 1, 2, 3, 5, 8, 13]
// end::motivation_stream_processing[]
        '''
    }

    @Test
    void testMotivationInteropCF() {
        assertScript '''
// tag::motivation_interop[]
import java.util.concurrent.CompletableFuture
import groovy.concurrent.Awaitable

// Groovy's await understands CompletableFuture, CompletionStage,
// Future, and Flow.Publisher out of the box — no conversion needed
async def mixedSources() {
    def fromCF = await CompletableFuture.supplyAsync { "cf" }
    def fromAwaitable = await Awaitable.of("awaitable")
    return "${fromCF}+${fromAwaitable}"
}

assert await(mixedSources()) == "cf+awaitable"
// end::motivation_interop[]
        '''
    }

    // =========================================================================
    // Structured concurrency — AsyncScope
    // =========================================================================

    @Test
    void testAsyncScopeBasic() {
        assertScript '''
// tag::async_scope_basic[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable

class UserService {
    async fetchUser(int id) {
        await Awaitable.delay(10)
        return [id: id, name: "User-${id}"]
    }

    async fetchOrders(int userId) {
        await Awaitable.delay(10)
        return [[item: "Book", userId: userId], [item: "Pen", userId: userId]]
    }

    async loadDashboard(int userId) {
        AsyncScope.withScope { scope ->
            def userTask   = scope.async { await fetchUser(userId) }
            def ordersTask = scope.async { await fetchOrders(userId) }
            [user: await userTask, orders: await ordersTask]
        }
    }
}

def svc = new UserService()
def dashboard = await svc.loadDashboard(42)
assert dashboard.user.name == "User-42"
assert dashboard.orders.size() == 2
// Both tasks are guaranteed complete when loadDashboard returns
// end::async_scope_basic[]
        '''
    }

    @Test
    void testAsyncScopeFailFast() {
        assertScript '''
// tag::async_scope_fail_fast[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable

async slowTask() {
    await Awaitable.delay(5000)
    return "done"
}

async failingTask() {
    await Awaitable.delay(10)
    throw new IllegalStateException("service unavailable")
}

def error = null
try {
    AsyncScope.withScope { scope ->
        scope.async { await slowTask() }
        scope.async { await failingTask() }
    }
} catch (IllegalStateException e) {
    error = e
}

assert error != null
assert error.message == "service unavailable"
// The slow task was automatically cancelled when failingTask threw
// end::async_scope_fail_fast[]
        '''
    }

    @Test
    void testAsyncScopeFanOut() {
        assertScript '''
// tag::async_scope_fanout[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable

async fetchPage(String url) {
    await Awaitable.delay(10)
    return "Content of ${url}"
}

async crawlAll(List<String> urls) {
    AsyncScope.withScope { scope ->
        def tasks = urls.collect { url ->
            scope.async { await fetchPage(url) }
        }
        tasks.collect { task -> await task }
    }
}

def urls = ["https://example.com/1", "https://example.com/2", "https://example.com/3"]
def pages = await crawlAll(urls)
assert pages.size() == 3
assert pages.every { it.startsWith("Content of") }
// All fetches ran concurrently; all guaranteed complete when crawlAll returns
// end::async_scope_fanout[]
        '''
    }

    @Test
    void testAsyncScopeManual() {
        assertScript '''
// tag::async_scope_manual[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable

async computePrice(String item) {
    await Awaitable.delay(10)
    return item == "Book" ? 29.99 : 9.99
}

def scope = AsyncScope.create()
def price1 = scope.async { await computePrice("Book") }
def price2 = scope.async { await computePrice("Pen") }

assert await(price1) == 29.99
assert await(price2) == 9.99
scope.close() // waits for all children, idempotent
// end::async_scope_manual[]
        '''
    }

    @Test
    void testAsyncContextPropagation() {
        assertScript '''
// tag::async_context_basic[]
import groovy.concurrent.AsyncContext
import groovy.concurrent.Awaitable

AsyncContext.with([traceId: 'req-7', tenant: 'acme']) {
    def task = Awaitable.go {
        assert AsyncContext.current()['traceId'] == 'req-7'
        AsyncContext.current()['traceId'] = 'child-trace'
        [trace: AsyncContext.current()['traceId'], tenant: AsyncContext.current()['tenant']]
    }

    def child = await task
    assert child == [trace: 'child-trace', tenant: 'acme']
    assert AsyncContext.current()['traceId'] == 'req-7'
}
// end::async_context_basic[]
        '''
    }

    @Test
    void testGoChannelSelect() {
        assertScript '''
// tag::go_channel_select[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncChannel

def result = AsyncScope.withScope { scope ->
    def ch = AsyncChannel.create(1)

    def producer = Awaitable.go {
        await ch.send('payload')
        'sent'
    }

    def consumer = scope.async {
        await ch.receive()
    }

    def value = await Awaitable.any(
        consumer,
        Awaitable.delay(250).then { 'timeout' }
    )

    [value: value, producer: await producer, childCount: scope.childCount]
}

assert result == [value: 'payload', producer: 'sent', childCount: 2]
// end::go_channel_select[]
        '''
    }

    @Test
    void testDeferAsyncCleanup() {
        assertScript '''
// tag::defer_async_cleanup[]
import groovy.concurrent.Awaitable

class ResourceHolder {
    final List<String> log = []

    Awaitable<Void> closeAsync(String name) {
        Awaitable.go {
            await Awaitable.delay(10)
            log << "close:${name}"
            null
        }
    }

    async useResource() {
        defer { closeAsync('outer') }
        defer { closeAsync('inner') }
        log << 'body'
        return log
    }
}

def holder = new ResourceHolder()
assert await(holder.useResource()) == ['body', 'close:inner', 'close:outer']
// end::defer_async_cleanup[]
        '''
    }

    @Test
    void testChannelFanInPipeline() {
        assertScript '''
// tag::channel_fan_in[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncChannel

def results = AsyncScope.withScope { scope ->
    def ch = AsyncChannel.create(8)

    // Fan-out: multiple producers write to a shared channel
    3.times { producerId ->
        scope.async {
            for (int i = 0; i < 4; i++) {
                await ch.send("p${producerId}-item${i}")
            }
            null
        }
    }

    // Fan-in: single consumer drains the channel
    def consumer = scope.async {
        def items = []
        12.times { items << await ch.receive() }
        items.sort()
    }

    await consumer
}

assert results.size() == 12
assert results.count { it.startsWith('p0-') } == 4
assert results.count { it.startsWith('p1-') } == 4
assert results.count { it.startsWith('p2-') } == 4
// end::channel_fan_in[]
        '''
    }

    @Test
    void testAsyncContextSnapshot() {
        assertScript '''
// tag::async_context_snapshot[]
import groovy.concurrent.AsyncContext
import groovy.concurrent.Awaitable

// Capture a snapshot to hand off to a different async scope
AsyncContext.current()['requestId'] = 'req-99'
def snapshot = AsyncContext.capture()

// Mutate the parent context — the snapshot is unaffected
AsyncContext.current()['requestId'] = 'req-100'

def childResult = await Awaitable.go {
    // Child sees the parent's captured state, not the earlier snapshot
    AsyncContext.current()['requestId']
}

assert childResult == 'req-100'  // go captures at call time

// Manually restore snapshot in a different scope
def restored = AsyncContext.withSnapshot(snapshot, { ->
    AsyncContext.current()['requestId']
} as java.util.function.Supplier)

assert restored == 'req-99'
// end::async_context_snapshot[]
        '''
    }

    @Test
    void testForAwaitOverChannel() {
        assertScript '''
// tag::for_await_channel[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncChannel

def items = []
def ch = AsyncChannel.create(4)

AsyncScope.withScope { scope ->
    // Producer sends values and closes the channel when done
    scope.async {
        for (word in ['hello', 'async', 'world']) {
            await ch.send(word)
        }
        ch.close()
        null
    }

    // Consumer iterates the channel with for-await
    for await (item in ch) {
        items << item
    }
}

assert items == ['hello', 'async', 'world']
// end::for_await_channel[]
        '''
    }

    @Test
    void testChannelVsFlowPublisherForAwait() {
        assertScript '''
// tag::channel_vs_flow_for_await[]
import groovy.concurrent.AsyncScope
import groovy.concurrent.Awaitable
import groovy.concurrent.AsyncChannel
import java.util.concurrent.Flow
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// --- AsyncChannel: Go-style coordination primitive ---
// Producers and consumers communicate through explicit send/receive.
// Best for task-to-task coordination and fan-in/fan-out pipelines.
def channelItems = []
def ch = AsyncChannel.create(3)

AsyncScope.withScope { scope ->
    scope.async {
        for (n in [10, 20, 30]) { await ch.send(n) }
        ch.close()
        null
    }
    for await (item in ch) {
        channelItems << item
    }
}
assert channelItems == [10, 20, 30]

// --- Flow.Publisher: reactive data stream ---
// A push-based source adapted automatically for for-await consumption.
// Best for consuming reactive streams, HTTP responses, and database cursors.
def publisherItems = []

def publisher = new Flow.Publisher<Integer>() {
    void subscribe(Flow.Subscriber<? super Integer> subscriber) {
        subscriber.onSubscribe(new Flow.Subscription() {
            int count = 0
            void request(long n) {
                for (int i = 0; i < n && count < 3; i++) {
                    count++
                    subscriber.onNext(count * 100)
                }
                if (count >= 3) subscriber.onComplete()
            }
            void cancel() {}
        })
    }
}
for await (item in publisher) {
    publisherItems << item
}
assert publisherItems == [100, 200, 300]

// Both AsyncChannel and Flow.Publisher work with 'for await' through
// the unified AsyncStream abstraction — same syntax, different use cases.
// end::channel_vs_flow_for_await[]
        '''
    }
}
