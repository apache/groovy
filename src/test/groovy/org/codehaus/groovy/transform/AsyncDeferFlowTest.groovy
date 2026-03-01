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
 * Tests for the Go-style {@code defer} statement and JDK {@code Flow.Publisher}
 * integration with async/await.
 *
 * <p>The {@code defer} statement registers cleanup actions that execute in LIFO
 * order when the enclosing async method or closure returns, even if an exception
 * occurs — similar to Go's {@code defer} keyword.
 *
 * <p>{@code Flow.Publisher} instances are automatically adapted by the built-in
 * adapter, enabling seamless use with {@code await} and {@code for await}.
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

            def result = await(new DeferTest().runWithDefer())
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
                await(new DeferTest().runWithDeferAndException())
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
                await(new DeferTest().runMultipleDefers())
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
                await(new DeferTest().runDeferWithErrors())
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

            def result = await(new DeferTest().doWork())
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

            def result = await(new DeferTest().runWithDeferStatement())
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
            def result = await(task())
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

            def result = await(new ResourceManager().processResources())
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
            def result = await(dt.asyncDefer())
            assert result == 42
            assert DeferTest.log == ['got 42', 'final-cleanup']
        '''
    }

    // ---- Flow.Publisher tests ----

    @Test
    void testAwaitFlowPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            def publisher = new SubmissionPublisher<String>()
            Thread.start {
                Thread.sleep(50)
                publisher.submit('hello')
                publisher.close()
            }
            def result = await(publisher)
            assert result == 'hello'
        '''
    }

    @Test
    void testForAwaitFlowPublisher() {
        assertScript '''
            class FlowTest {
                async consumePublisher(java.util.concurrent.SubmissionPublisher<Integer> pub) {
                    def results = []
                    for await (item in pub) {
                        results << item
                    }
                    return results
                }
            }

            def publisher = new java.util.concurrent.SubmissionPublisher<Integer>()
            def future = new FlowTest().consumePublisher(publisher)
            Thread.start {
                Thread.sleep(50)
                (1..5).each { publisher.submit(it) }
                publisher.close()
            }
            def result = await(future)
            assert result == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitFlowPublisherWithError() {
        assertScript '''
            class FlowTest {
                async consumeWithError(java.util.concurrent.SubmissionPublisher<Integer> pub) {
                    def results = []
                    for await (item in pub) {
                        results << item
                    }
                    return results
                }
            }

            def publisher = new java.util.concurrent.SubmissionPublisher<Integer>()
            def future = new FlowTest().consumeWithError(publisher)
            Thread.start {
                Thread.sleep(50)
                publisher.submit(1)
                publisher.submit(2)
                publisher.closeExceptionally(new RuntimeException('stream-error'))
            }
            try {
                await(future)
                assert false : 'Should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'stream-error'
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
            Thread.start {
                Thread.sleep(50)
                ['a', 'b', 'c'].each { publisher.submit(it) }
                publisher.close()
            }
            def result = await(future)
            assert result == ['a', 'b', 'c']
        '''
    }

    @Test
    void testDeferCombinedWithForAwaitFlow() {
        assertScript '''
            class CombinedTest {
                static log = []

                async processStream(java.util.concurrent.SubmissionPublisher<Integer> pub) {
                    defer { log << 'stream-cleanup' }
                    def sum = 0
                    for await (item in pub) {
                        sum += item
                    }
                    log << "sum=$sum"
                    return sum
                }
            }

            def publisher = new java.util.concurrent.SubmissionPublisher<Integer>()
            def future = new CombinedTest().processStream(publisher)
            Thread.start {
                Thread.sleep(50)
                (1..3).each { publisher.submit(it) }
                publisher.close()
            }
            def result = await(future)
            assert result == 6
            assert CombinedTest.log == ['sum=6', 'stream-cleanup']
        '''
    }

    @Test
    void testAwaitSingleValueFlowPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            def publisher = new SubmissionPublisher<Integer>()
            Thread.start {
                Thread.sleep(50)
                publisher.submit(42)
                publisher.submit(99)  // second value ignored by await
                publisher.close()
            }
            def result = await(publisher)
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
            await(new DeferTest().conditionalDefer(true))
            assert DeferTest.log == ['acquired', 'done', 'released']

            // Without acquisition
            DeferTest.log.clear()
            await(new DeferTest().conditionalDefer(false))
            assert DeferTest.log == ['done']
        '''
    }
}
