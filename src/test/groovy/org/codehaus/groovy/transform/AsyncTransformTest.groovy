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


import org.codehaus.groovy.control.CompilationFailedException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static groovy.concurrent.AsyncUtils.async
import static groovy.concurrent.AsyncUtils.await
import static groovy.concurrent.AsyncUtils.awaitAll
import static groovy.concurrent.AsyncUtils.awaitAny
import static groovy.concurrent.AsyncUtils.setExecutor

/**
 * Tests for async/await: @Async annotation, async/await keywords,
 * Awaitable abstraction, for-await, and AsyncUtils.
 */
class AsyncTransformTest {

    @AfterEach
    void resetExecutor() {
        setExecutor(null)
    }

    // ==== @Async annotation tests ====

    @Test
    void testBasicAsyncReturnsAwaitable() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Service {
                @Async
                def fetchData() {
                    return "hello"
                }
            }

            def service = new Service()
            def future = service.fetchData()
            assert future instanceof Awaitable
            assert future.get() == "hello"
        '''
    }

    @Test
    void testAsyncWithTypedReturnValue() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Service {
                @Async
                String compute() {
                    return "typed-result"
                }
            }

            def service = new Service()
            Awaitable<String> future = service.compute()
            assert future.get() == "typed-result"
        '''
    }

    @Test
    void testAsyncWithIntReturnValue() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Calculator {
                @Async
                int add(int a, int b) {
                    return a + b
                }
            }

            def calc = new Calculator()
            def future = calc.add(3, 4)
            assert future instanceof Awaitable
            assert future.get() == 7
        '''
    }

    @Test
    void testAsyncVoidMethod() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicBoolean

            class Worker {
                AtomicBoolean executed = new AtomicBoolean(false)

                @Async
                void doWork() {
                    executed.set(true)
                }
            }

            def worker = new Worker()
            def future = worker.doWork()
            assert future instanceof Awaitable
            future.get()
            assert worker.executed.get()
        '''
    }

    @Test
    void testAsyncStaticMethod() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Util {
                @Async
                static String process(String input) {
                    return input.toUpperCase()
                }
            }

            def future = Util.process("hello")
            assert future instanceof Awaitable
            assert future.get() == "HELLO"
        '''
    }

    // ==== await() call-style tests (backward compat) ====

    @Test
    void testAwaitCallInAsyncMethod() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def fetchAndProcess() {
                    def value = await(CompletableFuture.supplyAsync { 42 })
                    return value * 2
                }
            }

            def service = new Service()
            assert service.fetchAndProcess().get() == 84
        '''
    }

    @Test
    void testMultipleSequentialAwaits() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def combineResults() {
                    def a = await(CompletableFuture.supplyAsync { 10 })
                    def b = await(CompletableFuture.supplyAsync { 20 })
                    def c = await(CompletableFuture.supplyAsync { 30 })
                    return a + b + c
                }
            }

            def service = new Service()
            assert service.combineResults().get() == 60
        '''
    }

    @Test
    void testAwaitWithDependentFutures() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def pipeline() {
                    def raw = await(CompletableFuture.supplyAsync { "hello world" })
                    def upper = await(CompletableFuture.supplyAsync { raw.toUpperCase() })
                    return upper
                }
            }

            def service = new Service()
            assert service.pipeline().get() == "HELLO WORLD"
        '''
    }

    @Test
    void testNestedAwaitInExpression() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def nestedExpr() {
                    return await(CompletableFuture.supplyAsync { 5 }) + await(CompletableFuture.supplyAsync { 3 })
                }
            }

            def service = new Service()
            assert service.nestedExpr().get() == 8
        '''
    }

    @Test
    void testAwaitInsideConditional() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def conditional(boolean flag) {
                    if (flag) {
                        return await(CompletableFuture.supplyAsync { "yes" })
                    } else {
                        return await(CompletableFuture.supplyAsync { "no" })
                    }
                }
            }

            def service = new Service()
            assert service.conditional(true).get() == "yes"
            assert service.conditional(false).get() == "no"
        '''
    }

    @Test
    void testAwaitInsideLoop() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def sumAsync(int count) {
                    int sum = 0
                    for (int i = 1; i <= count; i++) {
                        sum += await(CompletableFuture.supplyAsync { i })
                    }
                    return sum
                }
            }

            def service = new Service()
            assert service.sumAsync(5).get() == 15
        '''
    }

    @Test
    void testAwaitInsideTryCatch() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def safeOperation() {
                    try {
                        def result = await(CompletableFuture.supplyAsync { 42 })
                        return result
                    } catch (Exception e) {
                        return -1
                    }
                }
            }

            def service = new Service()
            assert service.safeOperation().get() == 42
        '''
    }

    @Test
    void testAwaitInsideClosureInAsyncMethod() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def collectResults() {
                    def futures = [
                        CompletableFuture.supplyAsync { 1 },
                        CompletableFuture.supplyAsync { 2 },
                        CompletableFuture.supplyAsync { 3 }
                    ]
                    return futures.collect { f -> await(f) }
                }
            }

            def service = new Service()
            assert service.collectResults().get() == [1, 2, 3]
        '''
    }

    // ==== Exception handling ====

    @Test
    void testAsyncExceptionPropagation() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.ExecutionException

            class Service {
                @Async
                def failingMethod() {
                    throw new IllegalStateException("async failure")
                }
            }

            def service = new Service()
            def future = service.failingMethod()
            try {
                future.get()
                assert false : "should have thrown"
            } catch (ExecutionException e) {
                assert e.cause instanceof IllegalStateException
                assert e.cause.message == "async failure"
            }
        '''
    }

    @Test
    void testAwaitExceptionFromFuture() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.ExecutionException

            class Service {
                @Async
                def awaitFailing() {
                    return await(CompletableFuture.supplyAsync {
                        throw new IllegalArgumentException("inner failure")
                    })
                }
            }

            def service = new Service()
            try {
                service.awaitFailing().get()
                assert false : "should have thrown"
            } catch (ExecutionException e) {
                assert e.cause instanceof IllegalArgumentException
                assert e.cause.message == "inner failure"
            }
        '''
    }

    @Test
    void testAwaitExceptionCaughtInTryCatch() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def recoverFromError() {
                    try {
                        return await(CompletableFuture.supplyAsync {
                            throw new IllegalArgumentException("oops")
                        })
                    } catch (IllegalArgumentException e) {
                        return "recovered: " + e.message
                    }
                }
            }

            def service = new Service()
            assert service.recoverFromError().get() == "recovered: oops"
        '''
    }

    // ==== Custom executor ====

    @Test
    void testAsyncWithCustomExecutor() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.Executors

            class Service {
                def myPool = Executors.newSingleThreadExecutor()

                @Async(executor = "myPool")
                def runOnCustomPool() {
                    return Thread.currentThread().name
                }
            }

            def service = new Service()
            def threadName = service.runOnCustomPool().get()
            assert threadName.contains("pool")
            service.myPool.shutdown()
        '''
    }

    @Test
    void testAsyncStaticMethodWithStaticExecutor() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.Executors

            class Service {
                static def pool = Executors.newSingleThreadExecutor()

                @Async(executor = "pool")
                static def runStatic() {
                    return "static-result"
                }
            }

            assert Service.runStatic().get() == "static-result"
            Service.pool.shutdown()
        '''
    }

    // ==== Concurrency ====

    @Test
    void testAsyncMethodRunsOnDifferentThread() {
        assertScript '''
            import groovy.transform.Async

            class Service {
                @Async
                def getThreadName() {
                    return Thread.currentThread().name
                }
            }

            def service = new Service()
            def callerThread = Thread.currentThread().name
            def asyncThread = service.getThreadName().get()
            assert asyncThread != callerThread || asyncThread.contains("ForkJoinPool")
        '''
    }

    @Test
    void testConcurrentAsyncExecution() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.atomic.AtomicInteger

            class Service {
                AtomicInteger concurrentCount = new AtomicInteger(0)
                AtomicInteger maxConcurrent = new AtomicInteger(0)

                @Async
                def concurrentTask(CountDownLatch startLatch) {
                    int current = concurrentCount.incrementAndGet()
                    maxConcurrent.updateAndGet { max -> Math.max(max, current) }
                    startLatch.await(5, java.util.concurrent.TimeUnit.SECONDS)
                    concurrentCount.decrementAndGet()
                    return current
                }
            }

            def service = new Service()
            def latch = new CountDownLatch(1)
            def futures = (1..10).collect { service.concurrentTask(latch) }
            Thread.sleep(500)
            latch.countDown()
            futures.each { it.get(5, java.util.concurrent.TimeUnit.SECONDS) }
            assert service.maxConcurrent.get() > 1
        '''
    }

    @Test
    void testHighConcurrencyStressTest() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.atomic.AtomicInteger

            class Service {
                AtomicInteger counter = new AtomicInteger(0)

                @Async
                def increment() {
                    counter.incrementAndGet()
                    return counter.get()
                }
            }

            def service = new Service()
            int taskCount = 1000
            def futures = (1..taskCount).collect { service.increment() }
            futures.each { it.get(10, java.util.concurrent.TimeUnit.SECONDS) }
            assert service.counter.get() == taskCount
        '''
    }

    @Test
    void testParallelAwaitPattern() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                CompletableFuture<Integer> asyncCompute(int value) {
                    return CompletableFuture.supplyAsync {
                        Thread.sleep(50)
                        return value * 2
                    }
                }

                @Async
                def parallelFetch() {
                    def f1 = asyncCompute(1)
                    def f2 = asyncCompute(2)
                    def f3 = asyncCompute(3)
                    return await(f1) + await(f2) + await(f3)
                }
            }

            def service = new Service()
            long start = System.currentTimeMillis()
            def result = service.parallelFetch().get()
            long elapsed = System.currentTimeMillis() - start
            assert result == 12
            assert elapsed < 500
        '''
    }

    // ==== AsyncUtils standalone ====

    @Test
    void testAsyncUtilsAsyncClosure() {
        def future = async { 42 }
        assert future instanceof groovy.concurrent.Awaitable
        assert future.get() == 42
    }

    @Test
    void testAsyncUtilsAwait() {
        def future = CompletableFuture.supplyAsync { "hello" }
        def result = await(future)
        assert result == "hello"
    }

    @Test
    void testAsyncUtilsAwaitAll() {
        def f1 = CompletableFuture.supplyAsync { 1 }
        def f2 = CompletableFuture.supplyAsync { 2 }
        def f3 = CompletableFuture.supplyAsync { 3 }
        def results = awaitAll(f1, f2, f3)
        assert results == [1, 2, 3]
    }

    @Test
    void testAsyncUtilsAwaitAny() {
        def f1 = CompletableFuture.supplyAsync { "first" }
        def f2 = new CompletableFuture()
        def f3 = new CompletableFuture()
        def result = awaitAny(f1, f2, f3)
        assert result == "first"
    }

    @Test
    void testAsyncUtilsSetCustomExecutor() {
        def pool = Executors.newSingleThreadExecutor()
        try {
            setExecutor(pool)
            def future = async { Thread.currentThread().name }
            assert future.get().contains("pool")
        } finally {
            setExecutor(null)
            pool.shutdown()
        }
    }

    @Test
    void testAsyncUtilsAwaitFutureInterface() {
        def pool = Executors.newSingleThreadExecutor()
        try {
            java.util.concurrent.Future<String> future = pool.submit({ "from-executor" } as java.util.concurrent.Callable)
            def result = await(future)
            assert result == "from-executor"
        } finally {
            pool.shutdown()
        }
    }

    @Test
    void testAsyncUtilsAwaitExceptionUnwrapping() {
        def future = CompletableFuture.supplyAsync {
            throw new IllegalStateException("test error")
        }
        try {
            await(future)
            assert false : "should have thrown"
        } catch (IllegalStateException e) {
            assert e.message == "test error"
        }
    }

    // ==== Compilation errors ====

    @Test
    void testAsyncOnAbstractMethodFails() {
        def err = shouldFail CompilationFailedException, '''
            import groovy.transform.Async

            abstract class Service {
                @Async
                abstract def fetchData()
            }
        '''
        assert err.message.contains("cannot be applied to abstract method")
    }

    @Test
    void testAsyncOnAwaitableReturnTypeFails() {
        def err = shouldFail CompilationFailedException, '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Service {
                @Async
                Awaitable<String> fetchData() {
                    return groovy.concurrent.Awaitable.of("hello")
                }
            }
        '''
        assert err.message.contains("already returns an async type")
    }

    @Test
    void testAsyncWithMissingExecutorFieldFails() {
        def err = shouldFail CompilationFailedException, '''
            import groovy.transform.Async

            class Service {
                @Async(executor = "nonExistentField")
                def fetchData() {
                    return "hello"
                }
            }
        '''
        assert err.message.contains("executor field 'nonExistentField' not found")
    }

    @Test
    void testAsyncStaticMethodWithInstanceExecutorFails() {
        def err = shouldFail CompilationFailedException, '''
            import groovy.transform.Async
            import java.util.concurrent.Executors

            class Service {
                def myPool = Executors.newSingleThreadExecutor()

                @Async(executor = "myPool")
                static def fetchData() {
                    return "hello"
                }
            }
        '''
        assert err.message.contains("must be static for static method")
    }

    // ==== Static import async/await ====

    @Test
    void testAsyncClosureWithStaticImport() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletableFuture

            def future = async {
                def a = await(CompletableFuture.supplyAsync { 10 })
                def b = await(CompletableFuture.supplyAsync { 20 })
                return a + b
            }
            assert future.get() == 30
        '''
    }

    @Test
    void testAsyncClosureExceptionHandling() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.ExecutionException

            def future = async {
                throw new IllegalStateException("closure error")
            }
            try {
                future.get()
                assert false : "should have thrown"
            } catch (ExecutionException e) {
                assert e.cause instanceof IllegalStateException
                assert e.cause.message == "closure error"
            }
        '''
    }

    // ==== Integration scenarios ====

    @Test
    void testAsyncProducerConsumerPattern() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Producer {
                @Async
                def produce(int id) {
                    Thread.sleep(10)
                    return "item-${id}"
                }
            }

            class Consumer {
                @Async
                def consume(Awaitable<String> itemFuture) {
                    def item = await(itemFuture)
                    return "consumed: ${item}"
                }
            }

            def producer = new Producer()
            def consumer = new Consumer()
            def item = producer.produce(1)
            def result = consumer.consume(item)
            assert result.get() == "consumed: item-1"
        '''
    }

    @Test
    void testAsyncChainedCalls() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Pipeline {
                @Async
                def step1() { return 1 }

                @Async
                def step2(Awaitable<Integer> input) {
                    return await(input) + 10
                }

                @Async
                def step3(Awaitable<Integer> input) {
                    return await(input) * 2
                }
            }

            def p = new Pipeline()
            def r1 = p.step1()
            def r2 = p.step2(r1)
            def r3 = p.step3(r2)
            assert r3.get() == 22
        '''
    }

    @Test
    void testAsyncWithMethodParameters() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def greet(String name, int times) {
                    def parts = []
                    for (int i = 0; i < times; i++) {
                        parts << await(CompletableFuture.supplyAsync { "Hello ${name}" })
                    }
                    return parts.join(", ")
                }
            }

            def service = new Service()
            assert service.greet("World", 3).get() == "Hello World, Hello World, Hello World"
        '''
    }

    @Test
    void testAsyncWithCollectionProcessing() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.CompletableFuture

            class BatchProcessor {
                @Async
                def processBatch(List<Integer> items) {
                    def futures = items.collect { item ->
                        CompletableFuture.supplyAsync { item * item }
                    }
                    return futures.collect { f -> await(f) }
                }
            }

            def processor = new BatchProcessor()
            assert processor.processBatch([1, 2, 3, 4, 5]).get() == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testAsyncAwaitAllPattern() {
        assertScript '''
            import groovy.transform.Async
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.CompletableFuture

            class Service {
                @Async
                def fetchAll() {
                    def f1 = CompletableFuture.supplyAsync { "a" }
                    def f2 = CompletableFuture.supplyAsync { "b" }
                    def f3 = CompletableFuture.supplyAsync { "c" }
                    return awaitAll(f1, f2, f3)
                }
            }

            def service = new Service()
            assert service.fetchAll().get() == ["a", "b", "c"]
        '''
    }

    @Test
    void testAsyncInScript() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            @Async
            def asyncAdd(int a, int b) {
                return a + b
            }

            def future = asyncAdd(3, 4)
            assert future instanceof Awaitable
            assert future.get() == 7
        '''
    }

    // ==== 'async' keyword modifier ====

    @Test
    void testAsyncKeywordModifier() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Service {
                async def fetchData() {
                    return "from-async-keyword"
                }
            }

            def service = new Service()
            def result = service.fetchData()
            assert result instanceof Awaitable
            assert result.get() == "from-async-keyword"
        '''
    }

    @Test
    void testAsyncKeywordWithAwaitKeyword() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class Service {
                async def compute() {
                    def a = await CompletableFuture.supplyAsync { 10 }
                    def b = await CompletableFuture.supplyAsync { 20 }
                    return a + b
                }
            }

            def service = new Service()
            assert service.compute().get() == 30
        '''
    }

    @Test
    void testAsyncKeywordStaticMethod() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Util {
                async static process(String input) {
                    return input.toUpperCase()
                }
            }

            def result = Util.process("hello")
            assert result instanceof Awaitable
            assert result.get() == "HELLO"
        '''
    }

    @Test
    void testAsyncKeywordVoidMethod() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicBoolean

            class Worker {
                AtomicBoolean done = new AtomicBoolean(false)

                async void doWork() {
                    done.set(true)
                }
            }

            def worker = new Worker()
            def result = worker.doWork()
            assert result instanceof Awaitable
            result.get()
            assert worker.done.get()
        '''
    }

    @Test
    void testAsyncKeywordInScript() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async def add(int a, int b) {
                return a + b
            }

            def result = add(5, 7)
            assert result instanceof Awaitable
            assert result.get() == 12
        '''
    }

    // ==== 'await' keyword expression ====

    @Test
    void testAwaitKeywordExpression() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class Service {
                async def fetchAndDouble() {
                    def value = await CompletableFuture.supplyAsync { 21 }
                    return value * 2
                }
            }

            def service = new Service()
            assert service.fetchAndDouble().get() == 42
        '''
    }

    @Test
    void testAwaitKeywordPrecedence() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class Service {
                async def addAwaits() {
                    // 'await' should bind to the immediate expression, not to the sum
                    // await a + await b  =>  (await a) + (await b)
                    return (await CompletableFuture.supplyAsync { 10 }) + (await CompletableFuture.supplyAsync { 5 })
                }
            }

            def service = new Service()
            assert service.addAwaits().get() == 15
        '''
    }

    @Test
    void testAwaitKeywordWithParentheses() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Service {
                @groovy.transform.Async
                def compute() {
                    // await(expr) still works as await keyword + parenthesized expression
                    def a = await(CompletableFuture.supplyAsync { 100 })
                    return a
                }
            }

            def service = new Service()
            assert service.compute().get() == 100
        '''
    }

    @Test
    void testAwaitKeywordWithMethodCall() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class Service {
                CompletableFuture<String> fetchRemote() {
                    CompletableFuture.supplyAsync { "remote-data" }
                }

                async def process() {
                    def data = await fetchRemote()
                    return "processed: ${data}"
                }
            }

            def service = new Service()
            assert service.process().get() == "processed: remote-data"
        '''
    }

    @Test
    void testAwaitKeywordInConditional() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class Service {
                async def check(boolean flag) {
                    if (flag) {
                        return await CompletableFuture.supplyAsync { "yes" }
                    } else {
                        return await CompletableFuture.supplyAsync { "no" }
                    }
                }
            }

            def service = new Service()
            assert service.check(true).get() == "yes"
            assert service.check(false).get() == "no"
        '''
    }

    // ==== Awaitable abstraction tests ====

    @Test
    void testAwaitableInterface() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            // Test Awaitable.of()
            def a = Awaitable.of("hello")
            assert a.get() == "hello"
            assert a.isDone()

            // Test Awaitable.failed()
            def f = Awaitable.failed(new RuntimeException("oops"))
            try {
                f.get()
                assert false
            } catch (java.util.concurrent.ExecutionException e) {
                assert e.cause.message == "oops"
            }
        '''
    }

    @Test
    void testGroovyPromiseThen() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def p = GroovyPromise.of(CompletableFuture.supplyAsync { 10 })
            def p2 = p.then { it * 3 }
            assert p2.get() == 30
        '''
    }

    @Test
    void testGroovyPromiseThenCompose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def p = GroovyPromise.of(CompletableFuture.supplyAsync { 5 })
            def p2 = p.thenCompose { val ->
                GroovyPromise.of(CompletableFuture.supplyAsync { val * 4 })
            }
            assert p2.get() == 20
        '''
    }

    @Test
    void testGroovyPromiseExceptionally() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def p = GroovyPromise.of(CompletableFuture.supplyAsync { throw new RuntimeException("fail") })
            def recovered = p.exceptionally { t -> "recovered" }
            assert recovered.get() == "recovered"
        '''
    }

    @Test
    void testAwaitableToCompletableFuture() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            def p = GroovyPromise.of(CompletableFuture.completedFuture("interop"))
            CompletableFuture<String> cf = p.toCompletableFuture()
            assert cf.get() == "interop"
        '''
    }

    // ==== AwaitableAdapterRegistry tests ====

    @Test
    void testAdapterRegistryWithCompletableFuture() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapterRegistry
            import java.util.concurrent.CompletableFuture

            def cf = CompletableFuture.completedFuture("adapted")
            Awaitable<String> a = AwaitableAdapterRegistry.toAwaitable(cf)
            assert a.get() == "adapted"
        '''
    }

    @Test
    void testAdapterRegistryPassthroughAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapterRegistry

            def original = Awaitable.of(42)
            def adapted = AwaitableAdapterRegistry.toAwaitable(original)
            assert adapted.is(original)
        '''
    }

    @Test
    void testAdapterRegistryCustomAdapter() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture

            // Simulate a custom async type
            class CustomPromise {
                final String value
                CustomPromise(String v) { this.value = v }
            }

            // Register custom adapter
            AwaitableAdapterRegistry.register(new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { CustomPromise.isAssignableFrom(type) }
                def <T> Awaitable<T> toAwaitable(Object source) {
                    return Awaitable.of(((CustomPromise) source).value)
                }
            })

            def custom = new CustomPromise("custom-value")
            Awaitable<String> a = AwaitableAdapterRegistry.toAwaitable(custom)
            assert a.get() == "custom-value"
        '''
    }

    @Test
    void testAwaitWithCustomAdaptedType() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry
            import static groovy.concurrent.AsyncUtils.*

            class Deferred {
                final Object result
                Deferred(Object r) { this.result = r }
            }

            AwaitableAdapterRegistry.register(new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { Deferred.isAssignableFrom(type) }
                def <T> Awaitable<T> toAwaitable(Object source) {
                    return Awaitable.of(((Deferred) source).result)
                }
            })

            // await works with custom types via adapter
            def d = new Deferred("deferred-value")
            def result = await(d)
            assert result == "deferred-value"
        '''
    }

    // ==== for-await tests ====

    @Test
    void testForAwaitWithList() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Processor {
                @groovy.transform.Async
                def processAll() {
                    def results = []
                    for await (item in [1, 2, 3, 4, 5]) {
                        results << item * 2
                    }
                    return results
                }
            }

            def p = new Processor()
            assert p.processAll().get() == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testForAwaitWithBreak() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Processor {
                @groovy.transform.Async
                def findFirst() {
                    for await (item in [10, 20, 30, 40]) {
                        if (item > 20) {
                            return item
                        }
                    }
                    return -1
                }
            }

            def p = new Processor()
            assert p.findFirst().get() == 30
        '''
    }

    @Test
    void testForAwaitWithAsyncKeyword() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Worker {
                async def sumItems() {
                    int sum = 0
                    for await (item in [1, 2, 3]) {
                        sum += item
                    }
                    return sum
                }
            }

            def worker = new Worker()
            assert worker.sumItems().get() == 6
        '''
    }

    // ==== 'async' and 'await' as identifiers (backward compat) ====

    @Test
    void testAsyncAsVariableName() {
        assertScript '''
            def async = 42
            assert async == 42
        '''
    }

    @Test
    void testAwaitAsVariableName() {
        assertScript '''
            def await = "hello"
            assert await == "hello"
        '''
    }

    @Test
    void testAsyncAsMethodName() {
        assertScript '''
            class Compat {
                def async() { return "compatible" }
            }
            assert new Compat().async() == "compatible"
        '''
    }

    // ==== Combined async/await keyword with Awaitable ====

    @Test
    void testFullAsyncAwaitPipeline() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class DataService {
                async def fetchUser(long id) {
                    return [name: "User${id}"]
                }

                async def fetchOrders(long id) {
                    return ["order1", "order2"]
                }

                async def getUserSummary(long id) {
                    def user = await fetchUser(id)
                    def orders = await fetchOrders(id)
                    return [user: user, orders: orders]
                }
            }

            def service = new DataService()
            def summary = service.getUserSummary(1).get()
            assert summary.user.name == "User1"
            assert summary.orders.size() == 2
        '''
    }

    @Test
    void testAsyncAwaitWithExceptionRecovery() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class ResilientService {
                async def fetchWithFallback() {
                    try {
                        return await CompletableFuture.supplyAsync { throw new RuntimeException("primary failed") }
                    } catch (RuntimeException e) {
                        return "fallback-value"
                    }
                }
            }

            def service = new ResilientService()
            assert service.fetchWithFallback().get() == "fallback-value"
        '''
    }

    @Test
    void testAsyncAwaitMixedWithAnnotation() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            class MixedService {
                // Annotation style
                @Async
                def annotationStyle() {
                    return await CompletableFuture.supplyAsync { "annotation" }
                }

                // Keyword style
                async def keywordStyle() {
                    return await CompletableFuture.supplyAsync { "keyword" }
                }

                // They interoperate
                async def combined() {
                    def a = await annotationStyle()
                    def b = await keywordStyle()
                    return "${a}+${b}"
                }
            }

            def svc = new MixedService()
            assert svc.combined().get() == "annotation+keyword"
        '''
    }

    // ==== Coverage improvement tests ====

    @Test
    void testAwaitableAdapterRegistryUnregister() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            class FakePromise { String val }

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { FakePromise.isAssignableFrom(type) }
                def <T> Awaitable<T> toAwaitable(Object source) { Awaitable.of(((FakePromise) source).val) }
            }

            AwaitableAdapterRegistry.register(adapter)
            assert await(new FakePromise(val: "hello")) == "hello"

            assert AwaitableAdapterRegistry.unregister(adapter)
            assert !AwaitableAdapterRegistry.unregister(adapter) // already removed

            try {
                AwaitableAdapterRegistry.toAwaitable(new FakePromise(val: "x"))
                assert false : "should have thrown"
            } catch (IllegalArgumentException expected) {
                assert expected.message.contains("No AwaitableAdapter")
            }
        '''
    }

    @Test
    void testAwaitableAdapterRegistryRegisterReturnsAutoCloseable() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            class Token { int id }

            def handle = AwaitableAdapterRegistry.register(new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { Token.isAssignableFrom(type) }
                def <T> Awaitable<T> toAwaitable(Object source) { Awaitable.of(((Token) source).id) }
            })

            assert await(new Token(id: 42)) == 42
            handle.close() // unregister via AutoCloseable

            try {
                AwaitableAdapterRegistry.toAwaitable(new Token(id: 1))
                assert false
            } catch (IllegalArgumentException expected) { }
        '''
    }

    @Test
    void testAwaitableAdapterRegistrySetBlockingExecutor() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            def customExecutor = Executors.newSingleThreadExecutor()
            try {
                AwaitableAdapterRegistry.setBlockingExecutor(customExecutor)
                // Create a plain Future (not CompletableFuture) to exercise blocking path
                def pool = Executors.newSingleThreadExecutor()
                Future<String> future = pool.submit({ "from-blocking-future" } as Callable)
                Thread.sleep(50) // let it complete
                def result = await(future)
                assert result == "from-blocking-future"
                pool.shutdown()
            } finally {
                AwaitableAdapterRegistry.setBlockingExecutor(null)
                customExecutor.shutdown()
            }
        '''
    }

    @Test
    void testAwaitableAdapterRegistryToAsyncStreamWithIterator() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            // Test with Iterator directly
            def iter = [10, 20, 30].iterator()
            AsyncStream stream = AwaitableAdapterRegistry.toAsyncStream(iter)
            def results = []
            while (await(stream.moveNext())) {
                results << stream.getCurrent()
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testAwaitableAdapterRegistryToAsyncStreamUnsupportedType() {
        assertScript '''
            import groovy.concurrent.*

            try {
                AwaitableAdapterRegistry.toAsyncStream("a string")
                assert false : "should throw"
            } catch (IllegalArgumentException expected) {
                assert expected.message.contains("No AsyncStream adapter")
            }
        '''
    }

    @Test
    void testAwaitableAdapterRegistryToAwaitableUnsupportedType() {
        assertScript '''
            import groovy.concurrent.*

            try {
                AwaitableAdapterRegistry.toAwaitable("plain string")
                assert false : "should throw"
            } catch (IllegalArgumentException expected) {
                assert expected.message.contains("No AwaitableAdapter")
            }
        '''
    }

    @Test
    void testAwaitableAdapterRegistryPlainFutureNotDone() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            // Create a Future that completes with a delay (not done at adaptation time)
            def pool = Executors.newSingleThreadExecutor()
            Future<Integer> future = pool.submit({
                Thread.sleep(50)
                return 99
            } as Callable)
            // Future is NOT done yet at this point
            def result = await(future)
            assert result == 99
            pool.shutdown()
        '''
    }

    @Test
    void testAwaitableAdapterRegistryPlainFutureDone() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            // Create a Future that is already done
            def pool = Executors.newSingleThreadExecutor()
            Future<Integer> future = pool.submit({ 77 } as Callable)
            Thread.sleep(100)
            assert future.isDone()
            def result = await(future)
            assert result == 77
            pool.shutdown()
        '''
    }

    @Test
    void testAsyncStreamGeneratorErrorPropagation() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            @groovy.transform.Async
            def failingGenerator() {
                yield return "ok"
                throw new IOException("generator failed")
            }

            def stream = failingGenerator()
            assert await(stream.moveNext()) == true
            assert stream.getCurrent() == "ok"

            try {
                await(stream.moveNext())
                assert false : "should throw"
            } catch (IOException expected) {
                assert expected.message == "generator failed"
            }
        '''
    }

    @Test
    void testAsyncStreamGeneratorMultipleYields() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            @groovy.transform.Async
            def counter() {
                for (i in 1..5) {
                    yield return i
                }
            }

            def stream = counter()
            def items = []
            while (await(stream.moveNext())) {
                items << stream.getCurrent()
            }
            assert items == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testAsyncStreamEmpty() {
        assertScript '''
            import groovy.concurrent.*
            import static groovy.concurrent.AsyncUtils.*

            def stream = AsyncStream.empty()
            assert await(stream.moveNext()) == false
        '''
    }

    @Test
    void testAwaitNullReturnsNull() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*

            def result = await((Object) null)
            assert result == null
        '''
    }

    @Test
    void testAwaitCompletionStage() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            CompletionStage<String> stage = CompletableFuture.completedFuture("stage-value")
            def result = await(stage)
            assert result == "stage-value"
        '''
    }

    @Test
    void testAwaitPlainFutureViaObjectOverload() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            // Use Object overload which dispatches to Future overload
            def pool = Executors.newSingleThreadExecutor()
            Object future = pool.submit({ "future-obj" } as Callable)
            def result = await(future)
            assert result == "future-obj"
            pool.shutdown()
        '''
    }

    @Test
    void testToAsyncStreamWithNullReturnsEmpty() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import groovy.concurrent.AsyncStream

            AsyncStream stream = toAsyncStream(null)
            assert await(stream.moveNext()) == false
        '''
    }

    @Test
    void testDeepUnwrapWithNestedExceptions() {
        assertScript '''
            import static groovy.concurrent.AsyncUtils.*
            import java.util.concurrent.*

            def root = new IOException("root cause")
            def l1 = new ExecutionException(root)
            def l2 = new CompletionException(l1)
            def l3 = new CompletionException(l2)

            assert deepUnwrap(l3).is(root)
            assert deepUnwrap(root).is(root)

            // Non-wrapper exception is returned as-is
            def re = new RuntimeException("direct")
            assert deepUnwrap(re).is(re)
        '''
    }

    @Test
    void testBackwardCompatFacade() {
        // Verify the deprecated groovy.transform.AsyncUtils still works
        assertScript '''
            import static groovy.transform.AsyncUtils.*

            def result = await(async { 42 })
            assert result == 42

            def list = awaitAll(async { 1 }, async { 2 })
            assert list == [1, 2]

            def first = awaitAny(async { "fast" })
            assert first == "fast"

            setExecutor(null) // reset
        '''
    }

    // ==== Fallback thread pool configuration tests ====

    @Test
    void testFallbackPoolUsesNamedDaemonThreads() {
        assertScript '''
            import org.apache.groovy.runtime.async.AsyncSupport
            import groovy.concurrent.AsyncUtils

            // When using the default executor, async tasks should run on
            // named daemon threads (groovy-async-*) on JDK < 21, or
            // virtual threads on JDK 21+.
            def result = async {
                return Thread.currentThread().name
            }
            def threadName = AsyncUtils.await(result)
            assert threadName != null

            if (AsyncSupport.isVirtualThreadsAvailable()) {
                // Virtual threads have default empty names; verify task ran
                assert true
            } else {
                // Fallback pool uses "groovy-async-<id>" naming
                assert threadName.startsWith('groovy-async-') : \
                    "Expected 'groovy-async-*' but got: ${threadName}"
            }
        '''
    }

    @Test
    void testCustomExecutorOverrideAndReset() {
        assertScript '''
            import groovy.concurrent.AsyncUtils
            import java.util.concurrent.Executors
            import java.util.concurrent.Executor

            // Save original executor
            def original = AsyncUtils.getExecutor()
            assert original != null

            // Override with custom executor
            def customPool = Executors.newSingleThreadExecutor { r ->
                def t = new Thread(r, 'my-custom-async')
                t.daemon = true
                t
            }
            try {
                AsyncUtils.setExecutor(customPool)
                assert AsyncUtils.getExecutor().is(customPool)

                def threadName = AsyncUtils.await(async { Thread.currentThread().name })
                assert threadName == 'my-custom-async'
            } finally {
                // Reset to default
                AsyncUtils.setExecutor(null)
                assert AsyncUtils.getExecutor() != null
                assert !AsyncUtils.getExecutor().is(customPool)
            }
        '''
    }
}
