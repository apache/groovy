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

def result = await(new DataService().process())
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
        def a = await(CompletableFuture.supplyAsync { 10 })
        def b = await(CompletableFuture.supplyAsync { 20 })
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
    def b = await(CompletableFuture.supplyAsync { 20 })
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

def result = await(new DeferExample().runWithDefer())
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
    await(new ResourceHandler().processWithCleanup())
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
            results << await(riskyTask(flag))
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

def results = await(Awaitable.all(fetchUser(), fetchOrder(), fetchBalance()))
assert results == ["Alice", "Order#42", 100.0]
// end::await_all[]
        '''
    }

    @Test
    void testAwaitAny() {
        assertScript '''
// tag::await_any[]
import groovy.concurrent.Awaitable

def fast = async { "fast result" }
def slow = async {
    await(Awaitable.delay(2000))
    return "slow result"
}

def winner = await(Awaitable.any(fast(), slow()))
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
    return await(Awaitable.allSettled(a, b, c))
}

def results = await(caller())
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
    await(Awaitable.delay(100))  // pause for 100 milliseconds
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
await(Awaitable.of("groovy").thenAccept { observed.set(it.toUpperCase()) })
assert observed.get() == "GROOVY"

def recovered = await(
    Awaitable.failed(new IOException("boom"))
        .handle { value, error -> "recovered from ${error.message}" }
)
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
    await(Awaitable.delay(5_000))
    return "done"
}

try {
    await(Awaitable.timeout(slowCall(), 50))
    assert false : "should have timed out"
} catch (TimeoutException e) {
    assert e.message.contains("Timed out after 50")
}

assert await(Awaitable.timeoutOr(slowCall(), "cached", 50)) == "cached"
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
Thread.start {
    Thread.sleep(200)
    publisher.submit("hello from publisher")
    publisher.close()
}

def result = await(publisher)
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
Thread.start {
    Thread.sleep(50)
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

    def threadName = await(new WorkService().work())
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
    await(Awaitable.delay(10))  // simulate network I/O
    return "Content of ${url}"
}

// Launch three tasks in parallel
def pages = await(Awaitable.all(
    fetchPage("https://example.com/page1"),
    fetchPage("https://example.com/page2"),
    fetchPage("https://example.com/page3")
))

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
                await(Awaitable.delay(10 * attempt))  // exponential backoff
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
    await(Awaitable.delay(5000))
    return "completed"
}

try {
    await Awaitable.timeout(longRunningTask(), 50)
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
    await(Awaitable.delay(10_000))
    return "done"
}

def task = longRunningTask()
task.cancel()

try {
    await(task)
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
        await(Awaitable.delay(10))
        return "Person-${id}"
    }

    def getOrFetch(int id) {
        cache.computeIfAbsent(id, { key -> fetchPersonAsync(key) })
    }
}

def demo = new CacheDemo()

// First call triggers actual fetch
def p1 = await(demo.getOrFetch(42))
// Second call returns cached Awaitable (no new fetch)
def p2 = await(demo.getOrFetch(42))

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
        await(Awaitable.delay(10))  // simulate I/O
        return "conn-" + System.nanoTime()
    }
}

def service = await(RemoteService.create())
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
        await(Awaitable.delay(10))
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
    async step1() { await(Awaitable.delay(5)); return 10 }
    async step2(int n) { await(Awaitable.delay(5)); return n * 2 }
    async step3(int n) { await(Awaitable.delay(5)); return n + 3 }

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
    await(Awaitable.delay(10))
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
    // 16. Custom adapter registration
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
}
