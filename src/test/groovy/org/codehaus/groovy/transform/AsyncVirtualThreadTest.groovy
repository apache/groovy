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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for virtual thread integration, executor configuration, exception
 * handling consistency across async methods/closures/lambdas, and coverage
 * improvements for the async/await feature.
 *
 * @since 6.0.0
 */
final class AsyncVirtualThreadTest {

    // ---- Virtual thread detection and usage ----

    @Test
    void testVirtualThreadsAvailable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            // JDK 21+ should have virtual threads
            def jdkVersion = Runtime.version().feature()
            if (jdkVersion >= 21) {
                assert Awaitable.isVirtualThreadsAvailable()
            }
        '''
    }

    @Test
    void testAsyncMethodRunsOnVirtualThread() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class VTService {
                async checkThread() {
                    return Thread.currentThread().isVirtual()
                }
            }

            if (Awaitable.isVirtualThreadsAvailable()) {
                def service = new VTService()
                def result = service.checkThread().get()
                assert result == true
            }
        '''
    }

    @Test
    void testAsyncClosureRunsOnVirtualThread() {
        assertScript '''
            import groovy.concurrent.Awaitable

            if (Awaitable.isVirtualThreadsAvailable()) {
                def asyncVirtual = async { Thread.currentThread().isVirtual() }
                def awaitable = asyncVirtual()
                assert await(awaitable) == true
            }
        '''
    }

    @Test
    void testAsyncLambdaRunsOnVirtualThread() {
        assertScript '''
            import groovy.concurrent.Awaitable

            if (Awaitable.isVirtualThreadsAvailable()) {
                def asyncFn = async { x -> Thread.currentThread().isVirtual() }
                def result = await(asyncFn(42))
                assert result == true
            }
        '''
    }

    @Test
    void testForAwaitRunsOnVirtualThread() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncStream

            class VTGenerator {
                async generate() {
                    yield return Thread.currentThread().isVirtual()
                }
            }

            if (Awaitable.isVirtualThreadsAvailable()) {
                def gen = new VTGenerator()
                def stream = gen.generate()
                def results = []
                for await (item in stream) {
                    results << item
                }
                assert results == [true]
            }
        '''
    }

    @Test
    void testHighConcurrencyWithVirtualThreads() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class HighConcurrency {
                async compute(int n) {
                    Thread.sleep(10)
                    return n * 2
                }
            }

            if (Awaitable.isVirtualThreadsAvailable()) {
                def svc = new HighConcurrency()
                // Launch 1000 concurrent tasks — trivial with virtual threads
                def awaitables = (1..1000).collect { svc.compute(it) }
                def results = await(Awaitable.all(awaitables as Object[]))
                assert results.size() == 1000
                assert results[0] == 2
                assert results[999] == 2000
            }
        '''
    }

    // ---- Executor configuration ----

    @Test
    void testCustomExecutorOverride() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors
            import java.util.concurrent.atomic.AtomicReference

            def savedExecutor = Awaitable.getExecutor()
            try {
                def customPool = Executors.newFixedThreadPool(2, { r ->
                    def t = new Thread(r)
                    t.setName("custom-async-" + t.getId())
                    t
                })
                Awaitable.setExecutor(customPool)

                def asyncName = async {
                    Thread.currentThread().getName()
                }
                def awaitable = asyncName()
                def threadName = await(awaitable)
                assert threadName.startsWith("custom-async-")
            } finally {
                Awaitable.setExecutor(savedExecutor)
            }
        '''
    }

    @Test
    void testSetExecutorNullResetsToDefault() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors

            def originalExecutor = Awaitable.getExecutor()
            // Set a custom executor
            Awaitable.setExecutor(Executors.newSingleThreadExecutor())
            assert Awaitable.getExecutor() != originalExecutor
            // Reset to null — should restore default
            Awaitable.setExecutor(null)
            def restored = Awaitable.getExecutor()
            assert restored != null
            // Verify it works
            def task = async { 42 }; def awaitable = task()
            assert await(awaitable) == 42
            // Restore original
            Awaitable.setExecutor(originalExecutor)
        '''
    }

    @Test
    void testAsyncMethodWithCustomExecutorField() {
        assertScript '''
            import groovy.transform.Async
            import java.util.concurrent.Executor
            import java.util.concurrent.Executors

            class CustomExecutorService {
                static Executor myPool = Executors.newFixedThreadPool(1, { r ->
                    def t = new Thread(r)
                    t.setName("my-pool-thread")
                    t
                })

                @Async(executor = "myPool")
                def doWork() {
                    return Thread.currentThread().getName()
                }
            }

            def svc = new CustomExecutorService()
            def result = svc.doWork().get()
            assert result.startsWith("my-pool-thread")
        '''
    }

    // ---- Exception handling consistency: async method vs closure vs lambda ----

    @Test
    void testCheckedExceptionConsistencyAcrossAsyncMethods() {
        assertScript '''
            class ExcService {
                async failWithChecked() {
                    throw new java.io.IOException("async method error")
                }
            }

            def svc = new ExcService()
            try {
                await(svc.failWithChecked())
                assert false : "Should have thrown"
            } catch (java.io.IOException e) {
                assert e.message == "async method error"
            }
        '''
    }

    @Test
    void testCheckedExceptionConsistencyAcrossClosures() {
        assertScript '''
            def asyncIOError = async { throw new java.io.IOException("closure error") }
            def awaitable = asyncIOError()
            try {
                await(awaitable)
                assert false : "Should have thrown"
            } catch (java.io.IOException e) {
                assert e.message == "closure error"
            }
        '''
    }

    @Test
    void testCheckedExceptionConsistencyAcrossLambdas() {
        assertScript '''
            def asyncFn = async { x -> throw new java.io.IOException("lambda error: ${x}") }
            try {
                await(asyncFn("test"))
                assert false : "Should have thrown"
            } catch (java.io.IOException e) {
                assert e.message == "lambda error: test"
            }
        '''
    }

    @Test
    void testRuntimeExceptionConsistencyAllForms() {
        assertScript '''
            // Form 1: async method
            class Svc {
                async failMethod() { throw new IllegalStateException("from method") }
            }

            // Form 2: async closure
            def asyncClosure = async { throw new IllegalArgumentException("from closure") }
            def closure = asyncClosure()

            // Form 3: async lambda with params
            def lambda = async { x -> throw new UnsupportedOperationException("from lambda") }

            // All should throw the exact exception type (no wrapping)
            try { await(new Svc().failMethod()); assert false }
            catch (IllegalStateException e) { assert e.message == "from method" }

            try { await(closure); assert false }
            catch (IllegalArgumentException e) { assert e.message == "from closure" }

            try { await(lambda("x")); assert false }
            catch (UnsupportedOperationException e) { assert e.message == "from lambda" }
        '''
    }

    @Test
    void testErrorPropagationConsistencyAllForms() {
        assertScript '''
            class ErrorSvc {
                async fail() { throw new StackOverflowError("method") }
            }

            // async method
            try { await(new ErrorSvc().fail()); assert false }
            catch (StackOverflowError e) { assert e.message == "method" }

            // async closure
            def asyncSOE = async { throw new StackOverflowError("closure") }
            try { await(asyncSOE()); assert false }
            catch (StackOverflowError e) { assert e.message == "closure" }

            // async lambda
            def fn = async { x -> throw new StackOverflowError("lambda") }
            try { await(fn(1)); assert false }
            catch (StackOverflowError e) { assert e.message == "lambda" }
        '''
    }

    // ---- Async stream (yield return) consistency ----

    @Test
    void testYieldReturnConsistencyMethodVsClosure() {
        assertScript '''
            // Form 1: async method with yield return
            class GenSvc {
                async range(int n) {
                    for (int i = 1; i <= n; i++) {
                        yield return i
                    }
                }
            }

            // Form 2: async closure with yield return
            def asyncClosureGen = async { for (int i = 1; i <= 3; i++) { yield return i * 10 } }
            def closureGen = asyncClosureGen()

            // Form 3: async lambda with yield return + params
            def lambdaGen = async { n -> for (int i = 1; i <= n; i++) { yield return i * 100 } }

            // Verify all produce correct streams
            def methodResults = []
            for await (item in new GenSvc().range(3)) { methodResults << item }
            assert methodResults == [1, 2, 3]

            def closureResults = []
            for await (item in closureGen) { closureResults << item }
            assert closureResults == [10, 20, 30]

            def lambdaResults = []
            for await (item in lambdaGen(3)) { lambdaResults << item }
            assert lambdaResults == [100, 200, 300]
        '''
    }

    @Test
    void testYieldReturnExceptionConsistencyAllForms() {
        assertScript '''
            // async method generator with error
            class FailGen {
                async failing() {
                    yield return 1
                    throw new java.io.IOException("gen method error")
                }
            }

            // async closure generator with error
            def asyncFailGen = async {
                yield return 10
                throw new java.io.IOException("gen closure error")
            }
            def closureFailGen = asyncFailGen()

            // async lambda generator with error
            def lambdaFailGen = async { x ->
                yield return x
                throw new java.io.IOException("gen lambda error")
            }

            // All should propagate IOException through for-await
            for (gen in [new FailGen().failing(), closureFailGen, lambdaFailGen(100)]) {
                def items = []
                try {
                    for await (item in gen) { items << item }
                    assert false : "Should have thrown"
                } catch (java.io.IOException e) {
                    assert e.message.contains("gen")
                    assert items.size() == 1
                }
            }
        '''
    }

    // ---- executeAsync / executeAsyncVoid (unified path) ----

    @Test
    void testExecuteAsyncWithCustomExecutor() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors

            def pool = Executors.newSingleThreadExecutor({ r ->
                def t = new Thread(r)
                t.setName("exec-async-test")
                t
            })

            def awaitable = org.apache.groovy.runtime.async.AsyncSupport.executeAsync({ ->
                Thread.currentThread().getName()
            }, pool)
            def result = await(awaitable)
            assert result.startsWith("exec-async-test")
            pool.shutdown()
        '''
    }

    @Test
    void testExecuteAsyncVoidWithCustomExecutor() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Executors
            import java.util.concurrent.atomic.AtomicBoolean

            def pool = Executors.newSingleThreadExecutor()
            def executed = new AtomicBoolean(false)

            def awaitable = org.apache.groovy.runtime.async.AsyncSupport.executeAsyncVoid({ ->
                executed.set(true)
            }, pool)
            def _v1 = await(awaitable)
            assert executed.get()
            pool.shutdown()
        '''
    }

    @Test
    void testAsyncVoidMethodReturnsAwaitable() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            class VoidService {
                static AtomicInteger counter = new AtomicInteger(0)

                @Async
                void increment() {
                    counter.incrementAndGet()
                }
            }

            def svc = new VoidService()
            def awaitable = svc.increment()
            assert awaitable instanceof Awaitable
            def _v2 = await(awaitable)
            assert VoidService.counter.get() == 1
        '''
    }

    // ---- Edge cases and coverage improvements ----

    @Test
    void testAwaitNull() {
        assertScript '''
            def result = await((Object) null)
            assert result == null
        '''
    }

    @Test
    void testAwaitAlreadyCompletedAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def awaitable = Awaitable.of(42)
            assert awaitable.isDone()
            def result = await(awaitable)
            assert result == 42
        '''
    }

    @Test
    void testAwaitFailedAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def awaitable = Awaitable.failed(new RuntimeException("pre-failed"))
            assert awaitable.isCompletedExceptionally()
            try {
                await(awaitable)
                assert false
            } catch (RuntimeException e) {
                assert e.message == "pre-failed"
            }
        '''
    }

    @Test
    void testAwaitCancelledAwaitable() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CancellationException

            def cf = new CompletableFuture()
            def awaitable = GroovyPromise.of(cf)
            awaitable.cancel()
            assert awaitable.isCancelled()
            try {
                await(awaitable)
                assert false
            } catch (CancellationException e) {
                // expected
            }
        '''
    }

    @Test
    void testAsyncWithReturnValue() {
        assertScript '''
            def asyncHello = async { return "hello" }
            def awaitable = asyncHello()
            assert await(awaitable) == "hello"
        '''
    }

    @Test
    void testAsyncWithNullReturnValue() {
        assertScript '''
            def asyncNull = async { return null }
            def awaitable = asyncNull()
            assert await(awaitable) == null
        '''
    }

    @Test
    void testMultipleConcurrentAsyncGenerators() {
        assertScript '''
            def asyncGen1 = async { for (int i = 0; i < 5; i++) { yield return "A${i}" } }
            def gen1 = asyncGen1()
            def asyncGen2 = async { for (int i = 0; i < 5; i++) { yield return "B${i}" } }
            def gen2 = asyncGen2()

            def results1 = []
            def results2 = []
            for await (item in gen1) { results1 << item }
            for await (item in gen2) { results2 << item }
            assert results1 == ["A0", "A1", "A2", "A3", "A4"]
            assert results2 == ["B0", "B1", "B2", "B3", "B4"]
        '''
    }

    @Test
    void testEmptyAsyncStream() {
        assertScript '''
            class EmptyGen {
                async empty() {
                    // A generator must have yield return to be detected as one.
                    // This has one but it's unreachable at runtime.
                    if (false) yield return "unreachable"
                }
            }

            def results = []
            for await (item in new EmptyGen().empty()) {
                results << item
            }
            assert results.isEmpty()
        '''
    }

    @Test
    void testAwaitableCompositionWithThen() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def asyncOriginal = async { 10 }
            def original = asyncOriginal()
            def doubled = original.then { it * 2 }
            def result = await(doubled)
            assert result == 20
        '''
    }

    @Test
    void testAwaitableCompositionWithThenCompose() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def asyncFirst = async { 5 }
            def first = asyncFirst()
            def chained = first.thenCompose { v -> def asyncCompose = async { v + 10 }; asyncCompose() }
            def result = await(chained)
            assert result == 15
        '''
    }

    @Test
    void testAwaitableExceptionally() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def asyncFailing = async { throw new RuntimeException("oops") }
            def failing = asyncFailing()
            def recovered = failing.exceptionally { e -> "recovered: ${e.message}" }
            def result = await(recovered)
            assert result == "recovered: oops"
        '''
    }

    @Test
    void testAwaitWithCompletionStage() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.CompletionStage

            CompletionStage stage = CompletableFuture.supplyAsync { "from stage" }
            def result = await(stage)
            assert result == "from stage"
        '''
    }

    @Test
    void testAwaitWithFuture() {
        assertScript '''
            import java.util.concurrent.Executors
            import java.util.concurrent.Future

            def pool = Executors.newSingleThreadExecutor()
            Future future = pool.submit({ "from future" } as java.util.concurrent.Callable)
            def result = await(future)
            assert result == "from future"
            pool.shutdown()
        '''
    }

    @Test
    void testAwaitAllWithMixedTypes() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def asyncA1 = async { 1 }
            def a1 = asyncA1()
            def a2 = CompletableFuture.supplyAsync { 2 }
            def a3 = Awaitable.of(3)

            def results = await(Awaitable.all(a1, a2, a3))
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitAnyReturnsFirst() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def fast = Awaitable.of("fast")
            def asyncSlow = async { Thread.sleep(500); "slow" }
            def slow = asyncSlow()

            def result = await(Awaitable.any(fast, slow))
            assert result == "fast"
        '''
    }

    @Test
    void testAwaitAllSettledMixed() {
        assertScript '''
            import groovy.concurrent.Awaitable

            def asyncSuccess = async { "ok" }
            def success = asyncSuccess()
            def asyncFailure = async { throw new RuntimeException("fail") }
            def failure = asyncFailure()

            def results = await(Awaitable.allSettled(success, failure))
            assert results.size() == 2
            assert results[0].isSuccess()
            assert results[0].getValue() == "ok"
            assert results[1].isFailure()
            assert results[1].getError().message == "fail"
        '''
    }

    @Test
    void testAsyncMethodWithPrimitiveReturnType() {
        assertScript '''
            import groovy.transform.Async

            class PrimService {
                @Async
                int computeInt() { return 42 }

                @Async
                boolean checkBool() { return true }

                @Async
                double computeDouble() { return 3.14 }
            }

            def svc = new PrimService()
            assert await(svc.computeInt()) == 42
            assert await(svc.checkBool()) == true
            assert Math.abs(await(svc.computeDouble()) - 3.14) < 0.001
        '''
    }

    @Test
    void testDeepUnwrapNestedExceptions() {
        assertScript '''
            import java.util.concurrent.CompletionException
            import java.util.concurrent.ExecutionException

            // Create deeply nested exception chain
            def original = new java.io.IOException("deep")
            def wrapped = new CompletionException(new ExecutionException(
                new java.lang.reflect.UndeclaredThrowableException(
                    new java.lang.reflect.InvocationTargetException(original))))

            def cf = new java.util.concurrent.CompletableFuture()
            cf.completeExceptionally(wrapped)

            try {
                await(cf)
                assert false
            } catch (java.io.IOException e) {
                assert e.message == "deep"
            }
        '''
    }

    @Test
    void testAsyncWithNestedAwait() {
        assertScript '''
            class NestedService {
                async inner(int v) { return v * 2 }

                async outer(int v) {
                    def intermediate = await inner(v)
                    return await inner(intermediate)
                }
            }

            def svc = new NestedService()
            def result = await(svc.outer(5))
            assert result == 20  // 5 * 2 * 2
        '''
    }

    @Test
    void testAsyncClosureWithNestedAwait() {
        assertScript '''
            def inner = async { x -> x + 1 }
            def asyncOuter = async {
                def v1 = await(inner(10))
                def v2 = await(inner(v1))
                return v2
            }
            def outer = asyncOuter()
            assert await(outer) == 12  // 10 + 1 + 1
        '''
    }

    @Test
    void testParallelAwaitInAsyncMethod() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class ParallelService {
                CompletableFuture<Integer> compute(int v) {
                    return CompletableFuture.supplyAsync {
                        Thread.sleep(50)
                        return v * 2
                    }
                }

                async parallel() {
                    def f1 = compute(1)
                    def f2 = compute(2)
                    def f3 = compute(3)
                    return await(f1) + await(f2) + await(f3)
                }
            }

            def svc = new ParallelService()
            long start = System.currentTimeMillis()
            def result = await(svc.parallel())
            long elapsed = System.currentTimeMillis() - start
            assert result == 12  // 2 + 4 + 6
            assert elapsed < 500  // parallel, not sequential
        '''
    }

    @Test
    void testToAsyncStreamConversion() {
        assertScript '''
            import groovy.concurrent.AsyncStream
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry

            // Register an adapter for List that provides an AsyncStream
            AwaitableAdapterRegistry.register(new AwaitableAdapter() {
                boolean supportsAwaitable(Class type) { return false }
                Awaitable toAwaitable(Object source) { return null }
                boolean supportsAsyncStream(Class type) { return List.isAssignableFrom(type) }
                AsyncStream toAsyncStream(Object source) {
                    def list = (List) source
                    def iter = list.iterator()
                    return new AsyncStream() {
                        def current
                        Awaitable<Boolean> moveNext() {
                            if (iter.hasNext()) {
                                current = iter.next()
                                return Awaitable.of(true)
                            }
                            return Awaitable.of(false)
                        }
                        Object getCurrent() { return current }
                    }
                }
            })

            def results = []
            def stream = org.apache.groovy.runtime.async.AsyncSupport.toAsyncStream([10, 20, 30])
            for await (item in stream) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testAwaitObjectDispatchesToCorrectOverload() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            // Test Object overload dispatch for each type
            Object a = Awaitable.of("awaitable")
            Object b = CompletableFuture.completedFuture("cf")
            Object c = CompletableFuture.completedFuture("stage") as java.util.concurrent.CompletionStage

            assert await(a) == "awaitable"
            assert await(b) == "cf"
            assert await(c) == "stage"
        '''
    }

    @Test
    void testAsyncMethodWithParameters() {
        assertScript '''
            class ParamService {
                async add(int a, int b) {
                    return a + b
                }

                async greet(String name) {
                    return "Hello, ${name}!"
                }
            }

            def svc = new ParamService()
            assert await(svc.add(3, 4)) == 7
            assert await(svc.greet("Groovy")) == "Hello, Groovy!"
        '''
    }

    @Test
    void testAsyncChainedMethods() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Pipeline {
                async step1() { return 1 }

                async step2(Awaitable<Integer> input) {
                    return await(input) + 10
                }

                async step3(Awaitable<Integer> input) {
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
    void testAwaitSyntaxWithBinaryOperations() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class MathService {
                async compute(int v) { return v }
            }

            def svc = new MathService()
            // Test that await properly integrates with binary operators
            def a = svc.compute(10)
            def b = svc.compute(20)
            def r1 = await(a)
            def r2 = await(b)
            assert r1 + r2 == 30
        '''
    }

    @Test
    void testAsyncClosureWithoutYieldReturnIsNotGenerator() {
        assertScript '''
            // Closure with no yield return produces an Awaitable (not AsyncStream)
            def asyncResult = async { 42 }
            def result = asyncResult()
            assert await(result) == 42

            def asyncResultNull = async { /* empty body */ }
            def resultNull = asyncResultNull()
            assert await(resultNull) == null
        '''
    }

    @Test
    void testGroovyPromiseToString() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def pending = new CompletableFuture()
            def promise = GroovyPromise.of(pending)
            assert promise.toString() == "GroovyPromise{pending}"

            pending.complete(42)
            assert promise.toString() == "GroovyPromise{completed}"

            def failed = GroovyPromise.of(CompletableFuture.failedFuture(new RuntimeException()))
            assert failed.toString() == "GroovyPromise{failed}"
        '''
    }

    @Test
    void testGroovyPromiseGetWithTimeout() {
        assertScript '''
            import org.apache.groovy.runtime.async.GroovyPromise
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            def asyncTimeout = async { Thread.sleep(5000); "never" }
            def awaitable = asyncTimeout()
            try {
                awaitable.get(50, TimeUnit.MILLISECONDS)
                assert false : "Should have timed out"
            } catch (TimeoutException e) {
                // expected — cancel to avoid leaving the task running
                awaitable.cancel()
            }
        '''
    }

    // ---- for-await null safety ----

    @Test
    void testForAwaitNullSkipsLoop() {
        assertScript '''
            class Gen {
                async static test() {
                    def results = []
                    for await (item in null) {
                        results << item
                    }
                    return results
                }
            }
            assert Gen.test().get() == []
        '''
    }

    @Test
    void testForAwaitNullWithExistingItems() {
        assertScript '''
            class Gen {
                async static test() {
                    def results = []
                    // Normal stream works
                    def asyncStream = async { yield return 42 }
                    def stream = asyncStream()
                    for await (item in stream) {
                        results << item
                    }
                    // Null stream skips
                    for await (item in null) {
                        results << "should not appear"
                    }
                    return results
                }
            }
            assert Gen.test().get() == [42]
        '''
    }

    @Test
    void testForAwaitEmptyStream() {
        assertScript '''
            import groovy.concurrent.AsyncStream

            class Gen {
                async static test() {
                    def results = []
                    for await (item in AsyncStream.empty()) {
                        results << item
                    }
                    return results
                }
            }
            assert Gen.test().get() == []
        '''
    }

    // ---- ThreadLocal elimination verification ----

    @Test
    void testYieldReturnNoThreadLocal() {
        assertScript '''
            // Verify that yield return works without ThreadLocal
            // by running many concurrent generators simultaneously
            class Gen {
                async static generate(int id, int count) {
                    for (i in 1..count) {
                        yield return "${id}:${i}"
                    }
                }
            }

            def streams = (1..10).collect { Gen.generate(it, 5) }
            def allResults = []
            for (stream in streams) {
                def items = []
                for await (item in stream) {
                    items << item
                }
                allResults << items
            }

            // Each generator produces exactly its items
            for (i in 0..9) {
                def expected = (1..5).collect { "${i+1}:${it}" }
                assert allResults[i] == expected
            }
        '''
    }

    @Test
    void testYieldReturnOutsideAsyncContextFails() {
        assertScript '''
            // yield return outside async context should fail at runtime
            try {
                org.apache.groovy.runtime.async.AsyncSupport.yieldReturn("oops")
                assert false : "expected exception"
            } catch (IllegalStateException e) {
                assert e.message.contains("yield return")
            }
        '''
    }
}
