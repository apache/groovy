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
package org.apache.groovy.runtime.async

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the Go-style {@code defer} statement and JDK {@code Flow.Publisher}
 * integration with async/await.
 *
 * <p>The {@code defer} statement registers cleanup actions that execute in LIFO
 * order when the enclosing async method or closure returns, even if an exception
 * occurs — similar to Go's {@code defer} keyword.
 *
 * <p>{@code Flow.Publisher} instances are automatically adapted by the built-in
 * adapter, enabling seamless use with {@code await} and {@code for await}.
 *
 * <p><b>Test synchronisation:</b> Flow.Publisher tests use bounded waits on
 * {@code SubmissionPublisher.getNumberOfSubscribers()} to wait until the
 * subscription handshake is complete before submitting items. This keeps
 * tests deterministic while preventing unbounded hangs on regressions.
 */
class AsyncDeferFlowTest {

    // ---- defer statement tests ----

    @Test
    void testDeferBasicLIFOOrder() {
        assertScript '''
            class DeferTest {
                async runWithDefer() {
                    def log = []
                    defer { log << 'first' }
                    defer { log << 'second' }
                    defer { log << 'third' }
                    log << 'body'
                    return log
                }
            }

            def result = await new DeferTest().runWithDefer()
            // body runs first, then deferred in LIFO: third, second, first
            assert result == ['body', 'third', 'second', 'first']
        '''
    }

    @Test
    void testDeferRunsOnException() {
        assertScript '''
            class DeferTest {
                static log = []

                async runWithDeferAndException() {
                    defer { log << 'cleanup' }
                    throw new RuntimeException('boom')
                }
            }

            try {
                await new DeferTest().runWithDeferAndException()
                assert false : 'Should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'boom'
            }
            assert DeferTest.log == ['cleanup']
        '''
    }

    @Test
    void testDeferMultipleWithException() {
        assertScript '''
            class DeferTest {
                static log = []

                async runMultipleDefers() {
                    defer { log << 'A' }
                    defer { log << 'B' }
                    log << 'work'
                    throw new RuntimeException('fail')
                }
            }

            try {
                await new DeferTest().runMultipleDefers()
            } catch (RuntimeException e) {
                assert e.message == 'fail'
            }
            // work runs, then LIFO: B, A
            assert DeferTest.log == ['work', 'B', 'A']
        '''
    }

    @Test
    void testDeferExceptionSuppression() {
        assertScript '''
            class DeferTest {
                async runDeferWithErrors() {
                    defer { throw new RuntimeException('defer-error-1') }
                    defer { throw new RuntimeException('defer-error-2') }
                    return 'ok'
                }
            }

            try {
                await new DeferTest().runDeferWithErrors()
                assert false : 'Should have thrown'
            } catch (RuntimeException e) {
                // LIFO: second defer runs first (defer-error-2), first defer second (defer-error-1)
                assert e.message == 'defer-error-2'
                assert e.suppressed.length == 1
                assert e.suppressed[0].message == 'defer-error-1'
            }
        '''
    }

    @Test
    void testDeferWithAsyncModifierMethod() {
        assertScript '''
            class DeferTest {
                static log = []

                async doWork() {
                    defer { log << 'deferred' }
                    log << 'working'
                    return 42
                }
            }

            def result = await new DeferTest().doWork()
            assert result == 42
            assert DeferTest.log == ['working', 'deferred']
        '''
    }

    @Test
    void testDeferWithStatementExpression() {
        assertScript '''
            class DeferTest {
                static log = []

                async runWithDeferStatement() {
                    defer log.add('stmt-defer')
                    log << 'body'
                    return log
                }
            }

            def result = await new DeferTest().runWithDeferStatement()
            assert result == ['body', 'stmt-defer']
        '''
    }

    @Test
    void testDeferInAsyncClosure() {
        assertScript '''
            def log = []
            def task = async {
                defer { log << 'cleanup' }
                log << 'working'
                'done'
            }
            def result = await task()
            assert result == 'done'
            assert log == ['working', 'cleanup']
        '''
    }

    @Test
    void testDeferResourceCleanupPattern() {
        assertScript '''
            class ResourceManager {
                static resources = []
                static cleanupLog = []

                async processResources() {
                    def r1 = 'resource1'
                    resources << r1
                    defer { resources.remove(r1); cleanupLog << "closed $r1" }

                    def r2 = 'resource2'
                    resources << r2
                    defer { resources.remove(r2); cleanupLog << "closed $r2" }

                    // Both resources open here
                    assert resources.size() == 2
                    return 'processed'
                }
            }

            def result = await new ResourceManager().processResources()
            assert result == 'processed'
            // LIFO cleanup: r2 first, then r1
            assert ResourceManager.cleanupLog == ['closed resource2', 'closed resource1']
            assert ResourceManager.resources.isEmpty()
        '''
    }

    @Test
    void testDeferWithAwait() {
        assertScript '''
            class DeferTest {
                static log = []

                async computeAsync() {
                    return 42
                }

                async asyncDefer() {
                    defer { log << 'final-cleanup' }
                    def result = await computeAsync()
                    log << "got $result"
                    return result
                }
            }

            def dt = new DeferTest()
            def result = await dt.asyncDefer()
            assert result == 42
            assert DeferTest.log == ['got 42', 'final-cleanup']
        '''
    }

    // ---- Flow.Publisher tests ----

    @Test
    void testAwaitFlowPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher
            import java.util.concurrent.CountDownLatch

            def publisher = new SubmissionPublisher<String>()
            // Ensure items are submitted after subscription is fully established
            def subscribed = new CountDownLatch(1)
            Thread.start {
                subscribed.await()
                publisher.submit('hello')
                publisher.close()
            }
            // SubmissionPublisher.subscribe() is synchronous — once await() internally
            // calls subscribe(), we can safely signal the publisher thread.
            // We poll getNumberOfSubscribers() to wait for the subscription handshake.
            def task = async {
                // The subscribe call happens inside await() via FlowPublisherAdapter
                def result = await publisher
                result
            }
            def future = task()
            // Wait until the subscriber is registered with the publisher
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            subscribed.countDown()
            def result = await future
            assert result == 'hello'
        '''
    }

    @Test
    void testForAwaitFlowPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            class FlowTest {
                async consumePublisher(SubmissionPublisher<Integer> pub) {
                    def results = []
                    for await (item in pub) {
                        results << item
                    }
                    return results
                }
            }

            def publisher = new SubmissionPublisher<Integer>()
            def future = new FlowTest().consumePublisher(publisher)
            // Wait until the for-await loop has subscribed to the publisher
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            (1..5).each { publisher.submit(it) }
            publisher.close()
            def result = await future
            assert result == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitFlowPublisherWithError() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            class FlowTest {
                async consumeWithError(SubmissionPublisher<Integer> pub) {
                    def seen = []
                    for await (item in pub) {
                        seen << item
                    }
                    return seen
                }
            }

            def publisher = new SubmissionPublisher<Integer>()
            def future = new FlowTest().consumeWithError(publisher)
            // Wait until the for-await loop has subscribed
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            publisher.submit(1)
            publisher.submit(2)
            publisher.closeExceptionally(new RuntimeException('stream-error'))
            try {
                await future
                assert false : 'Should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'stream-error'
            }
        '''
    }

    /**
     * Stress-tests the terminal-error path with an asynchronous publisher to
     * verify there is no TOCTOU race between the terminal signal delivery and
     * the consumer's end-of-stream check.  Each iteration publishes values
     * then delivers an error asynchronously — the consumer must always observe
     * the error rather than silently completing.
     */
    @Test
    void testForAwaitFlowPublisherTerminalErrorRaceRegression() {
        assertScript '''
            import java.util.concurrent.Flow
            import java.util.concurrent.ForkJoinPool

            class ErrorPublisher implements Flow.Publisher<Integer> {
                final int iteration
                ErrorPublisher(int iteration) { this.iteration = iteration }
                void subscribe(Flow.Subscriber<? super Integer> sub) {
                    sub.onSubscribe(new Flow.Subscription() {
                        private final java.util.concurrent.atomic.AtomicBoolean started = new java.util.concurrent.atomic.AtomicBoolean()
                        void request(long n) {
                            if (!started.compareAndSet(false, true)) return
                            ForkJoinPool.commonPool().execute {
                                sub.onNext(1)
                                sub.onNext(2)
                                sub.onError(new RuntimeException("async-error-${iteration}"))
                            }
                        }
                        void cancel() {}
                    })
                }
            }

            class FlowConsumer {
                async consume(Flow.Publisher pub) {
                    def seen = []
                    for await (item in pub) {
                        seen << item
                    }
                    return seen
                }
            }

            def consumer = new FlowConsumer()
            for (int iteration = 0; iteration < 25; iteration++) {
                try {
                    await consumer.consume(new ErrorPublisher(iteration))
                    assert false : "Iteration $iteration: should have thrown"
                } catch (RuntimeException e) {
                    assert e.message == "async-error-$iteration"
                }
            }
        '''
    }

    @Test
    void testForAwaitFlowPublisherInAsyncClosure() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            def publisher = new SubmissionPublisher<String>()
            def task = async {
                def results = []
                for await (item in publisher) {
                    results << item
                }
                results
            }
            def future = task()
            // Wait until the for-await loop has subscribed
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            ['a', 'b', 'c'].each { publisher.submit(it) }
            publisher.close()
            def result = await future
            assert result == ['a', 'b', 'c']
        '''
    }

    @Test
    void testDeferCombinedWithForAwaitFlow() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            class CombinedTest {
                static log = []

                async processStream(SubmissionPublisher<Integer> pub) {
                    defer { log << 'stream-cleanup' }
                    def sum = 0
                    for await (item in pub) {
                        sum += item
                    }
                    log << "sum=$sum"
                    return sum
                }
            }

            def publisher = new SubmissionPublisher<Integer>()
            def future = new CombinedTest().processStream(publisher)
            // Wait until the for-await loop has subscribed
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            (1..3).each { publisher.submit(it) }
            publisher.close()
            def result = await future
            assert result == 6
            assert CombinedTest.log == ['sum=6', 'stream-cleanup']
        '''
    }

    @Test
    void testAwaitSingleValueFlowPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            def publisher = new SubmissionPublisher<Integer>()
            def task = async {
                await publisher
            }
            def future = task()
            // Wait until the subscriber is registered
            def deadline = System.nanoTime() + 5_000_000_000L
            while (publisher.getNumberOfSubscribers() == 0 && System.nanoTime() < deadline) { Thread.sleep(1) }
            assert publisher.getNumberOfSubscribers() > 0 : 'Timed out waiting for publisher subscription'
            publisher.submit(42)
            publisher.submit(99)  // second value ignored by await
            publisher.close()
            def result = await future
            assert result == 42
        '''
    }

    @Test
    void testDeferConditional() {
        assertScript '''
            class DeferTest {
                static log = []

                async conditionalDefer(boolean acquire) {
                    if (acquire) {
                        defer { log << 'released' }
                        log << 'acquired'
                    }
                    log << 'done'
                    return 'ok'
                }
            }

            // With acquisition
            DeferTest.log.clear()
            await new DeferTest().conditionalDefer(true)
            assert DeferTest.log == ['acquired', 'done', 'released']

            // Without acquisition
            DeferTest.log.clear()
            await new DeferTest().conditionalDefer(false)
            assert DeferTest.log == ['done']
        '''
    }

    // ===== Defer scope tests =====

    @Test
    void testDeferExecutesOnReturn() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CopyOnWriteArrayList

            class DeferTest {
                def log = new CopyOnWriteArrayList<String>()

                async withCleanup() {
                    defer { log.add("cleanup") }
                    log.add("work")
                    "result"
                }
            }

            def t = new DeferTest()
            assert await(t.withCleanup()) == "result"
            assert t.log == ["work", "cleanup"]
        '''
    }

    @Test
    void testDeferExecutesOnException() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CopyOnWriteArrayList

            class DeferFailTest {
                def log = new CopyOnWriteArrayList<String>()

                async failing() {
                    defer { log.add("cleanup") }
                    throw new IOException("oops")
                }
            }

            def t = new DeferFailTest()
            try {
                await t.failing()
            } catch (IOException e) {
                assert e instanceof IOException
            }
            assert t.log == ["cleanup"]
        '''
    }

    // ================================================================
    // for await coverage
    // ================================================================

    @Test
    void testDeferScopeMultipleExceptionsViaAsyncDef() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class DeferMultiError {
                async run() {
                    defer { throw new IOException("first") }
                    defer { throw new ArithmeticException("second") }
                    "ok"
                }
            }

            try {
                await new DeferMultiError().run()
                assert false
            } catch (ArithmeticException e) {
                assert e.message == "second"
                assert e.suppressed.length == 1
                assert e.suppressed[0] instanceof IOException
            }
        '''
    }

    // ---- Defensive handling: non-compliant Flow.Publisher ----

    @Test
    void testAwaitPublisherOnErrorNull() {
        assertScript '''
            import java.util.concurrent.Flow

            // Non-compliant publisher that calls onError(null) — violates §2.13
            class NullErrorPublisher implements Flow.Publisher<String> {
                void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        void request(long n) {
                            subscriber.onError(null)
                        }
                        void cancel() {}
                    })
                }
            }

            class OnErrorNullTest {
                Flow.Publisher pub
                OnErrorNullTest(Flow.Publisher p) { this.pub = p }
                async run() {
                    try {
                        await pub
                        assert false : 'Should have thrown'
                    } catch (NullPointerException e) {
                        assert e.message.contains('2.13')
                        return 'caught'
                    }
                }
            }

            assert await(new OnErrorNullTest(new NullErrorPublisher()).run()) == 'caught'
        '''
    }

    @Test
    void testForAwaitPublisherOnErrorNull() {
        assertScript '''
            import java.util.concurrent.Flow

            // Non-compliant publisher that emits one item then calls onError(null)
            class ItemThenNullErrorPublisher implements Flow.Publisher<String> {
                void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        boolean sent = false
                        void request(long n) {
                            if (!sent) {
                                sent = true
                                subscriber.onNext("item1")
                            } else {
                                subscriber.onError(null)
                            }
                        }
                        void cancel() {}
                    })
                }
            }

            class ForAwaitOnErrorNullTest {
                Flow.Publisher pub
                ForAwaitOnErrorNullTest(Flow.Publisher p) { this.pub = p }
                async run() {
                    def items = []
                    try {
                        for await (item in pub) {
                            items << item
                        }
                        assert false : 'Should have thrown'
                    } catch (NullPointerException e) {
                        assert e.message.contains('2.13')
                    }
                    return items
                }
            }

            assert await(new ForAwaitOnErrorNullTest(new ItemThenNullErrorPublisher()).run()) == ['item1']
        '''
    }

    // =========================================================================
    // Edge-case and error-path coverage
    // =========================================================================

    // ----- defer edge cases -----

    @Test
    void testExecuteDeferScopeWithError() {
        assertScript '''
            try {
                def runWithDeferredFailure = async {
                    defer { throw new RuntimeException("defer error") }
                    return "body"
                }

                await runWithDeferredFailure()
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message == 'defer error'
            }
        '''
    }

    @Test
    void testDeferMultipleWithSuppressed() {
        assertScript '''
            try {
                def runWithMultipleDeferredFailures = async {
                    defer { throw new RuntimeException("d1") }
                    defer { throw new RuntimeException("d2") }
                    return "body"
                }

                await runWithMultipleDeferredFailures()
                assert false : 'should throw'
            } catch (RuntimeException e) {
                // LIFO: d2 runs first, d1 runs second
                assert e.message == 'd2'
                assert e.suppressed.length == 1
                assert e.suppressed[0].message == 'd1'
            }
        '''
    }

    @Test
    void testDeferOutsideAsync() {
        // AstBuilder error for defer outside async
        shouldFail '''
            defer { println 'cleanup' }
        '''
    }

    @Test
    void testAsyncClosureWithDeferAndYield() {
        // defer + yield-return combo in async closure
        assertScript '''
            import groovy.concurrent.Awaitable

            async closureDeferYield() {
                def log = []
                def gen = async {
                    defer { log << 'cleaned' }
                    yield return 'a'
                    yield return 'b'
                }

                def items = []
                for await (item in gen()) {
                    items << item
                }
                Thread.sleep(100)
                assert items == ['a', 'b']
            }

            closureDeferYield().get()
        '''
    }

    // ----- Flow.Publisher edge cases -----

    @Test
    void testFlowPublisherAdapterEmptyPublisher() {
        // onComplete without emission
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Flow

            def publisher = new Flow.Publisher<String>() {
                void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        void request(long n) {
                            subscriber.onComplete()
                        }
                        void cancel() {}
                    })
                }
            }

            Awaitable<String> aw = Awaitable.from(publisher)
            assert aw.get() == null
        '''
    }

    @Test
    void testFlowPublisherAdapterErrorPublisher() {
        // onError handling
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Flow
            import java.util.concurrent.ExecutionException

            def publisher = new Flow.Publisher<String>() {
                void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        void request(long n) {
                            subscriber.onError(new RuntimeException("pub error"))
                        }
                        void cancel() {}
                    })
                }
            }

            Awaitable<String> aw = Awaitable.from(publisher)
            try {
                aw.get()
                assert false : 'should throw'
            } catch (ExecutionException e) {
                assert e.cause.message == 'pub error'
            }
        '''
    }

    @Test
    void testFlowPublisherStreamClosedEarly() {
        // stream close path via early break
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Flow
            import java.util.concurrent.SubmissionPublisher

            async earlyCloseTest() {
                def pub = new SubmissionPublisher<Integer>()

                def items = Collections.synchronizedList([])
                def task = Awaitable.go {
                    for await (item in pub) {
                        items.add(item)
                        if (items.size() >= 3) break
                    }
                }

                // Submit items
                for (int i = 1; i <= 10; i++) {
                    pub.submit(i)
                    Thread.sleep(20)
                    if (items.size() >= 3) break
                }

                await task
                assert items.size() >= 3
                pub.close()
            }

            earlyCloseTest().get()
        '''
    }

    @Test
    void testFlowPublisherNullOnNextRejection() {
        // null item rejection
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.Flow
            import java.util.concurrent.ExecutionException

            def publisher = new Flow.Publisher<String>() {
                void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        void request(long n) {
                            try {
                                subscriber.onNext(null) // violates spec
                            } catch (NullPointerException e) {
                                subscriber.onError(e)
                            }
                        }
                        void cancel() {}
                    })
                }
            }

            Awaitable<String> aw = Awaitable.from(publisher)
            try {
                aw.get()
                assert false : 'should throw'
            } catch (ExecutionException e) {
                assert e.cause instanceof NullPointerException
            }
        '''
    }

}
