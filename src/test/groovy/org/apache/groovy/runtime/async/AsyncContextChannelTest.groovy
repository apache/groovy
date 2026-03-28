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
 * Integration tests for async context propagation, channels, and structured
 * concurrency using Groovy's async/await syntax.  All tests use
 * {@link groovy.test.GroovyAssert#assertScript assertScript} so the code
 * under test exercises the full Groovy compiler pipeline including the
 * {@code async}/{@code await} AST transformation.
 */
final class AsyncContextChannelTest {

    // ---- AsyncContext propagation ----

    @Test
    void testAsyncContextPropagatesAndIsIsolatedAcrossTasks() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['traceId'] = 'parent-trace'

            def child = Awaitable.go {
                assert AsyncContext.current()['traceId'] == 'parent-trace'
                AsyncContext.current()['traceId'] = 'child-trace'
                AsyncContext.current()['traceId']
            }

            assert await(child) == 'child-trace'
            assert AsyncContext.current()['traceId'] == 'parent-trace'
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testPromiseContinuationsCaptureAsyncContext() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['requestId'] = 'request-1'

            def source = Awaitable.go {
                await Awaitable.delay(25)
                40
            }

            def continuation = source.then { value ->
                assert AsyncContext.current()['requestId'] == 'request-1'
                value + 2
            }

            AsyncContext.current()['requestId'] = 'request-2'

            assert await(continuation) == 42
            assert AsyncContext.current()['requestId'] == 'request-2'
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextPreservedAcrossContinuationChains() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['traceId'] = 'trace-chain'

            def result = Awaitable.go { 10 }
                .then { it * 2 }
                .then { value ->
                    assert AsyncContext.current()['traceId'] == 'trace-chain'
                    value + 1
                }
                .thenCompose { value ->
                    assert AsyncContext.current()['traceId'] == 'trace-chain'
                    Awaitable.of(value * 3)
                }
                .handle { value, error ->
                    assert AsyncContext.current()['traceId'] == 'trace-chain'
                    value
                }

            assert await(result) == 63
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextInExceptionallyHandler() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['reqId'] = 'err-test'

            def result = Awaitable.go { throw new RuntimeException('fail') }
                .exceptionally { error ->
                    assert AsyncContext.current()['reqId'] == 'err-test'
                    "recovered: ${error.message}"
                }

            assert await(result) == 'recovered: fail'
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextInWhenCompleteHandler() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit

            AsyncContext.current()['traceId'] = 'wc-test'
            def observed = new CompletableFuture<String>()

            def task = Awaitable.go { 42 }
                .whenComplete { value, error ->
                    observed.complete(AsyncContext.current()['traceId'] as String)
                }

            await task
            assert observed.get(2, TimeUnit.SECONDS) == 'wc-test'
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextIsolationUnderConcurrentMutations() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            int taskCount = 100
            AsyncContext.current()['shared'] = 'root'

            def tasks = (0..<taskCount).collect { n ->
                Awaitable.go {
                    assert AsyncContext.current()['shared'] == 'root'
                    AsyncContext.current()['taskId'] = "task-${n}".toString()
                    await Awaitable.delay(5)
                    assert AsyncContext.current()['taskId'] == "task-${n}".toString()
                    assert AsyncContext.current()['shared'] == 'root'
                    AsyncContext.current()['taskId']
                }
            }

            def results = await Awaitable.all(*tasks)
            assert results.toSet().size() == taskCount
            assert AsyncContext.current()['shared'] == 'root'
            assert !AsyncContext.current().containsKey('taskId')
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextSnapshotIsImmutableAfterCapture() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['key'] = 'before'
            def snapshot = AsyncContext.capture()

            AsyncContext.current()['key'] = 'after'
            AsyncContext.current()['extra'] = 'new'

            assert snapshot.get('key') == 'before'
            assert !snapshot.containsKey('extra')

            def result = Awaitable.go { AsyncContext.current()['key'] }
            assert await(result) == 'after'
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextWithOverlayAndRestore() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current()['env'] = 'production'
            AsyncContext.current()['user'] = 'admin'

            def result = AsyncContext.with([env: 'staging', feature: 'beta']) {
                assert AsyncContext.current()['env'] == 'staging'
                assert AsyncContext.current()['user'] == 'admin'
                assert AsyncContext.current()['feature'] == 'beta'
                'inside'
            }

            assert result == 'inside'
            assert AsyncContext.current()['env'] == 'production'
            assert AsyncContext.current()['user'] == 'admin'
            assert !AsyncContext.current().containsKey('feature')
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextWithNullValueRemovesKey() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current()['key'] = 'value'
            assert AsyncContext.current().containsKey('key')
            AsyncContext.current()['key'] = null
            assert !AsyncContext.current().containsKey('key')
        '''
    }

    @Test
    void testAsyncContextSnapshotWith() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current()['a'] = '1'
            AsyncContext.current()['b'] = '2'
            def snapshot = AsyncContext.capture()

            def merged = snapshot.with([b: '3', c: '4'])
            assert merged.get('a') == '1'
            assert merged.get('b') == '3'
            assert merged.get('c') == '4'

            assert snapshot.get('b') == '2'
            assert !snapshot.containsKey('c')
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextSnapshotWithNullRemovesKey() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current()['a'] = '1'
            AsyncContext.current()['b'] = '2'
            def snapshot = AsyncContext.capture()

            def merged = snapshot.with([b: null])
            assert merged.get('a') == '1'
            assert !merged.containsKey('b')
            assert merged.size() == 1
            AsyncContext.current().clear()
        '''
    }

    @Test
    void testAsyncContextScopeRestoresOnException() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current()['key'] = 'original'

            try {
                AsyncContext.with([key: 'overlay']) {
                    assert AsyncContext.current()['key'] == 'overlay'
                    throw new RuntimeException('boom')
                }
            } catch (RuntimeException ignored) {}

            assert AsyncContext.current()['key'] == 'original'
            AsyncContext.current().clear()
        '''
    }

    // ---- Structured concurrency (AsyncScope) ----

    @Test
    void testGoJoinsCurrentScope() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            def result = AsyncScope.withScope { scope ->
                def task = Awaitable.go { 42 }
                [value: await task, childCount: scope.childCount]
            }

            assert result.value == 42
            assert result.childCount == 1
        '''
    }

    @Test
    void testGoOutsideScopeBehavesAsDetachedTask() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            assert AsyncScope.current() == null
            def result = await Awaitable.go { 'detached' }
            assert result == 'detached'
        '''
    }

    @Test
    void testGoInsideScopeRegistersAsChild() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            def result = AsyncScope.withScope { scope ->
                Awaitable.go { 'child-1' }
                Awaitable.go { 'child-2' }
                scope.childCount
            }
            assert result == 2
        '''
    }

    @Test
    void testAsyncContextPropagatedIntoScopeChildren() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.AsyncScope

            AsyncContext.current()['scopeCtx'] = 'scoped-value'

            def result = AsyncScope.withScope { scope ->
                def child = scope.async { AsyncContext.current()['scopeCtx'] }
                await child
            }

            assert result == 'scoped-value'
            AsyncContext.current().clear()
        '''
    }

    // ---- Channel tests ----

    @Test
    void testUnbufferedChannelBackPressuresSenderUntilReceiverArrives() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def channel = AsyncChannel.create()
            def senderStarted = new CountDownLatch(1)

            def sendTask = Awaitable.go {
                senderStarted.countDown()
                await channel.send('payload')
                'sent'
            }

            assert senderStarted.await(2, TimeUnit.SECONDS)
            Thread.sleep(50)
            assert !sendTask.done

            assert await(channel.receive()) == 'payload'
            assert await(sendTask) == 'sent'
        '''
    }

    @Test
    void testBufferedChannelSendCompletesImmediately() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def channel = AsyncChannel.create(2)

            def s1 = channel.send('a')
            def s2 = channel.send('b')

            assert s1.done
            assert s2.done
            assert channel.bufferedSize == 2

            assert await(channel.receive()) == 'a'
            assert await(channel.receive()) == 'b'
        '''
    }

    @Test
    void testUnbufferedChannelRendezvous() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def channel = AsyncChannel.create()
            def senderReady = new CountDownLatch(1)
            def receiverReady = new CountDownLatch(1)

            def sender = Awaitable.go {
                senderReady.countDown()
                receiverReady.await(2, TimeUnit.SECONDS)
                await channel.send('rendezvous')
                'sent'
            }

            def receiver = Awaitable.go {
                senderReady.await(2, TimeUnit.SECONDS)
                receiverReady.countDown()
                await channel.receive()
            }

            assert await(receiver) == 'rendezvous'
            assert await(sender) == 'sent'
        '''
    }

    @Test
    void testChannelCloseFailsReceiversAfterDrain() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def channel = AsyncChannel.create(1)
            await channel.send('first')
            assert channel.close()
            assert await(channel.receive()) == 'first'
            await channel.receive()
        '''
    }

    @Test
    void testChannelRejectsNullPayload() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            AsyncChannel.create(1).send(null)
        '''
    }

    @Test
    void testChannelRejectsNegativeCapacity() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            AsyncChannel.create(-1)
        '''
    }

    @Test
    void testChannelCloseIsIdempotent() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def channel = AsyncChannel.create(1)
            assert channel.close()
            assert !channel.close()
        '''
    }

    @Test
    void testChannelSendOnClosedChannel() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def channel = AsyncChannel.create(1)
            channel.close()
            await channel.send('late')
        '''
    }

    @Test
    void testChannelClosePreservesBufferedValues() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import static groovy.test.GroovyAssert.shouldFail

            def channel = AsyncChannel.create(5)

            await channel.send(1)
            await channel.send(2)
            await channel.send(3)
            channel.close()

            assert await(channel.receive()) == 1
            assert await(channel.receive()) == 2
            assert await(channel.receive()) == 3

            shouldFail(groovy.concurrent.ChannelClosedException) {
                await channel.receive()
            }
        '''
    }

    @Test
    void testChannelCloseDrainsBufferToWaitingReceivers() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def channel = AsyncChannel.create(2)
            await channel.send('a')
            await channel.send('b')

            def r1 = channel.receive()
            def r2 = channel.receive()

            channel.close()

            assert await(r1) == 'a'
            assert await(r2) == 'b'
        '''
    }

    @Test
    void testChannelToString() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def channel = AsyncChannel.create(5)
            await channel.send('a')
            def str = channel.toString()
            assert str.contains('capacity=5')
            assert str.contains('buffered=1')
            assert str.contains('closed=false')
        '''
    }

    @Test
    void testChannelPendingSendersCancelledOnClose() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def channel = AsyncChannel.create()  // unbuffered
            def senderStarted = new CountDownLatch(1)

            def sendTask = Awaitable.go {
                senderStarted.countDown()
                await channel.send('will-fail')
            }

            assert senderStarted.await(2, TimeUnit.SECONDS)
            Thread.sleep(50)
            channel.close()

            await sendTask
        '''
    }

    @Test
    void testChannelPendingReceiversFailOnClose() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def channel = AsyncChannel.create()
            def receiverStarted = new CountDownLatch(1)

            def receiveTask = Awaitable.go {
                receiverStarted.countDown()
                await channel.receive()
            }

            assert receiverStarted.await(2, TimeUnit.SECONDS)
            Thread.sleep(50)
            channel.close()

            await receiveTask
        '''
    }

    @Test
    void testAnyRacesChannelReceiveAgainstTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def channel = AsyncChannel.create(1)

            // Timeout should win since channel has no data
            def result = await Awaitable.any(
                channel.receive(),
                Awaitable.delay(50).then { 'timeout' }
            )

            assert result == 'timeout'
        '''
    }

    @Test
    void testAnyWithMultipleChannels() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch1 = AsyncChannel.create(1)
            def ch2 = AsyncChannel.create(1)

            await ch2.send('from-ch2')

            def result = await Awaitable.any(
                ch1.receive(),
                ch2.receive()
            )

            assert result == 'from-ch2'
        '''
    }

    // ---- High-concurrency tests ----

    @Test
    void testBufferedChannelHighConcurrencyFanIn() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            int producerCount = 200
            def channel = AsyncChannel.create(32)

            def sum = AsyncScope.withScope { scope ->
                def senders = (0..<producerCount).collect { n ->
                    Awaitable.go {
                        await channel.send(n)
                        null
                    }
                }
                def consumer = scope.async {
                    int total = 0
                    for (int i = 0; i < producerCount; i++) {
                        total += await channel.receive()
                    }
                    total
                }

                await Awaitable.all(*senders)
                await consumer
            }

            assert sum == (producerCount * (producerCount - 1)) / 2
        '''
    }

    @Test
    void testHighConcurrencyMultiProducerMultiConsumer() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CopyOnWriteArrayList

            int producerCount = 50
            int messagesPerProducer = 20
            int totalMessages = producerCount * messagesPerProducer
            def channel = AsyncChannel.create(64)
            def received = new CopyOnWriteArrayList<Integer>()

            AsyncScope.withScope { scope ->
                (0..<producerCount).each { p ->
                    scope.async {
                        for (int i = 0; i < messagesPerProducer; i++) {
                            await channel.send(p * messagesPerProducer + i)
                        }
                        null
                    }
                }

                int consumerCount = 10
                int messagesPerConsumer = totalMessages / consumerCount
                (0..<consumerCount).each { c ->
                    scope.async {
                        for (int i = 0; i < messagesPerConsumer; i++) {
                            received.add(await channel.receive())
                        }
                        null
                    }
                }
            }

            assert received.size() == totalMessages
            assert received.toSet().size() == totalMessages
        '''
    }

    @Test
    void testHighConcurrencyUnbufferedChannelStressTest() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.atomic.AtomicInteger

            int pairCount = 100
            def channel = AsyncChannel.create()
            def sum = new AtomicInteger(0)

            AsyncScope.withScope { scope ->
                (0..<pairCount).each { n ->
                    scope.async {
                        await channel.send(n)
                        null
                    }
                    scope.async {
                        sum.addAndGet(await(channel.receive()) as int)
                        null
                    }
                }
            }

            assert sum.get() == (pairCount * (pairCount - 1)) / 2
        '''
    }

    @Test
    void testScopeFailFastCancellsSiblings() {
        shouldFail '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            AsyncScope.withScope { scope ->
                scope.async {
                    await Awaitable.delay(10)
                    throw new RuntimeException('boom')
                }
                scope.async {
                    await Awaitable.delay(5000)
                    'should be cancelled'
                }
            }
        '''
    }

    @Test
    void testScopeClosedRejectsNewTasks() {
        shouldFail '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.close()
            scope.async { 'too late' }
        '''
    }

    @Test
    void testAsyncContextCapturedAtGoCallTime() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            AsyncContext.current()['phase'] = 'A'
            def task1 = Awaitable.go {
                await Awaitable.delay(30)
                AsyncContext.current()['phase']
            }

            AsyncContext.current()['phase'] = 'B'
            def task2 = Awaitable.go {
                await Awaitable.delay(10)
                AsyncContext.current()['phase']
            }

            assert await(task1) == 'A'
            assert await(task2) == 'B'
            AsyncContext.current().clear()
        '''
    }

    // ---- for-await over channels ----

    @Test
    void testForAwaitOverBufferedChannel() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def items = []
            def ch = AsyncChannel.create(4)

            AsyncScope.withScope { scope ->
                scope.async {
                    await ch.send(1)
                    await ch.send(2)
                    await ch.send(3)
                    ch.close()
                    null
                }

                for await (item in ch) {
                    items << item
                }
            }

            assert items == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitOverUnbufferedChannel() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def items = []
            def ch = AsyncChannel.create()

            AsyncScope.withScope { scope ->
                scope.async {
                    for (int i = 0; i < 5; i++) {
                        await ch.send(i)
                    }
                    ch.close()
                    null
                }

                for await (item in ch) {
                    items << item
                }
            }

            assert items == [0, 1, 2, 3, 4]
        '''
    }

    @Test
    void testForAwaitExitsOnChannelClose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(2)
            await ch.send('a')
            await ch.send('b')
            ch.close()

            def items = []
            for await (item in ch) {
                items << item
            }
            assert items == ['a', 'b']
        '''
    }

    @Test
    void testForAwaitBreakDoesNotCloseChannel() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(10)
            for (int i = 0; i < 5; i++) { await ch.send(i) }

            def firstTwo = []
            for await (item in ch) {
                firstTwo << item
                if (firstTwo.size() == 2) break
            }

            // Channel should still be open — asStream().close() is a no-op
            assert !ch.closed
            assert firstTwo == [0, 1]

            // Remaining items are still receivable
            assert await(ch.receive()) == 2
        '''
    }

    @Test
    void testForAwaitOverEmptyClosedChannel() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(1)
            ch.close()

            def items = []
            for await (item in ch) {
                items << item
            }
            assert items.isEmpty()
        '''
    }

    @Test
    void testForAwaitConcurrentFanInPipeline() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            int producerCount = 5
            int itemsPerProducer = 10
            def ch = AsyncChannel.create(16)

            def received = []
            AsyncScope.withScope { scope ->
                def producerTasks = (0..<producerCount).collect { p ->
                    scope.async {
                        for (int i = 0; i < itemsPerProducer; i++) {
                            await ch.send("p${p}-${i}".toString())
                        }
                        null
                    }
                }

                // Close channel after all producers complete (deterministic, no timing dependency)
                scope.async {
                    await Awaitable.all(producerTasks as Object[])
                    ch.close()
                    null
                }

                for await (item in ch) {
                    received << item
                }
            }

            assert received.size() == producerCount * itemsPerProducer
            assert received.toSet().size() == producerCount * itemsPerProducer
        '''
    }

    @Test
    void testForAwaitPreservesAsyncContext() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            AsyncContext.current()['traceId'] = 'for-await-trace'
            def ch = AsyncChannel.create(3)

            AsyncScope.withScope { scope ->
                scope.async {
                    await ch.send('a')
                    await ch.send('b')
                    ch.close()
                    null
                }

                for await (item in ch) {
                    assert AsyncContext.current()['traceId'] == 'for-await-trace'
                }
            }

            AsyncContext.current().clear()
        '''
    }

    @Test
    void testChannelAsStreamExplicit() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(3)
            await ch.send(10)
            await ch.send(20)
            await ch.send(30)
            ch.close()

            def stream = ch.asStream()
            def items = []
            while (await stream.moveNext()) {
                items << stream.current
            }
            assert items == [10, 20, 30]
        '''
    }

    // =========================================================================
    // Edge-case and error-path coverage
    // =========================================================================

    // ----- AsyncScope edge cases -----

    @Test
    void testScopeWithCurrentNull() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.function.Supplier

            def result = AsyncScope.withCurrent(null, { -> 'hello' } as Supplier)
            assert result == 'hello'
        '''
    }

    @Test
    void testScopeWithCurrentRunnable() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def executed = false
            AsyncScope.withCurrent(null, { -> executed = true } as Runnable)
            assert executed
        '''
    }

    @Test
    void testScopeWithCurrentNested() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope1 = AsyncScope.create()
            def scope2 = AsyncScope.create()

            AsyncScope.withCurrent(scope1, {
                assert AsyncScope.current() == scope1
                AsyncScope.withCurrent(scope2, {
                    assert AsyncScope.current() == scope2
                } as Runnable)
                assert AsyncScope.current() == scope1
            } as Runnable)

            scope1.close()
            scope2.close()
        '''
    }

    @Test
    void testScopeChildCompletionException() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CompletionException

            def ex = null
            try {
                AsyncScope.withScope { scope ->
                    scope.async {
                        throw new CompletionException(new IllegalStateException("test"))
                    }
                }
            } catch (IllegalStateException e) {
                ex = e
            }
            assert ex?.message == 'test'
        '''
    }

    @Test
    void testScopeCloseWithCancelledChild() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CancellationException

            def scope = AsyncScope.create()
            def task = scope.async { await Awaitable.delay(10000) }
            assert task.cancel()
            scope.close()
            assert task.isCancelled()
        '''
    }

    @Test
    void testScopeCloseWithMultipleFailures() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.Executors
            import java.util.concurrent.TimeUnit

            // Use non-fail-fast scope to ensure all tasks fail independently
            def pool = Executors.newCachedThreadPool()
            def scope = AsyncScope.create(pool, false)
            def latch = new CountDownLatch(3)

            try {
                scope.async {
                    latch.countDown()
                    throw new RuntimeException("first")
                }
                scope.async {
                    latch.countDown()
                    throw new RuntimeException("second")
                }
                scope.async {
                    latch.countDown()
                    throw new RuntimeException("third")
                }

                assert latch.await(5, TimeUnit.SECONDS)
                Thread.sleep(200) // ensure all tasks complete exceptionally

                def ex = null
                try {
                    scope.close()
                } catch (RuntimeException e) {
                    ex = e
                }
                assert ex != null
                assert ex.suppressed.length >= 1 : "Expected at least 1 suppressed but got: ${ex.suppressed.length}"
            } finally {
                pool.shutdown()
                assert pool.awaitTermination(5, TimeUnit.SECONDS)
            }
        '''
    }

    @Test
    void testScopeCloseWithNonCompletionException() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CompletableFuture

            def scope = AsyncScope.create()
            def cf = new CompletableFuture()
            cf.cancel(false)
            scope.@children.add(cf)

            // CancellationException is silently ignored during close
            scope.close()
            assert cf.isCancelled()
        '''
    }

    @Test
    void testScopeIdempotentClose() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.close()
            scope.close()

            // Verify the scope truly rejected new work after close
            def ex = null
            try {
                scope.async { 'should fail' }
            } catch (IllegalStateException e) {
                ex = e
            }
            assert ex instanceof IllegalStateException
        '''
    }

    @Test
    void testScopeAsyncAfterClose() {
        shouldFail '''
            import groovy.concurrent.AsyncScope

            def scope = AsyncScope.create()
            scope.close()
            scope.async { 'should fail' }
        '''
    }

    @Test
    void testScopeFailFastCancellation() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            try {
                AsyncScope.withScope { scope ->
                    scope.async {
                        throw new RuntimeException("fail fast")
                    }
                    scope.async {
                        await Awaitable.delay(10000)
                        'should be cancelled'
                    }
                }
                assert false : 'should throw'
            } catch (RuntimeException e) {
                assert e.message == 'fail fast'
            }
        '''
    }

    @Test
    void testScopeChildCount() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable

            def count = AsyncScope.withScope { scope ->
                scope.async { 1 }
                scope.async { 2 }
                scope.async { 3 }
                Thread.sleep(100)
                scope.childCount
            }
            assert count == 3
        '''
    }

    @Test
    void testScopeConcurrentAsyncCallsViaAssertScript() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.CopyOnWriteArrayList

            def scope = AsyncScope.create()
            def latch = new CountDownLatch(1)
            def results = new CopyOnWriteArrayList()

            def threads = (1..50).collect { i ->
                Thread.start {
                    latch.await()
                    results << scope.async { i * 2 }
                }
            }
            latch.countDown()
            threads*.join()

            scope.close()
            assert results.size() == 50
            def values = results.collect { it.get() } as Set
            assert values.size() == 50
        '''
    }

    @Test
    void testScopeAsyncAfterCloseRace() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.atomic.AtomicInteger

            20.times {
                def scope = AsyncScope.create()
                def submitted = new AtomicInteger(0)
                def rejected = new AtomicInteger(0)

                def producer = Thread.start {
                    50.times {
                        try {
                            scope.async { 'ok' }
                            submitted.incrementAndGet()
                        } catch (IllegalStateException e) {
                            rejected.incrementAndGet()
                        }
                    }
                }

                Thread.sleep(1)
                scope.close()
                producer.join()

                assert submitted.get() + rejected.get() == 50
            }
        '''
    }

    // ----- AsyncContext edge cases -----

    @Test
    void testAsyncContextConstructorWithMap() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create([key1: 'val1', key2: 'val2'])
            assert ctx.get('key1') == 'val1'
            assert ctx.get('key2') == 'val2'
        '''
    }

    @Test
    void testAsyncContextRemove() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create()
            ctx.put('myKey', 'myVal')
            def removed = ctx.remove('myKey')
            assert removed == 'myVal'
            assert ctx.get('myKey') == null
        '''
    }

    @Test
    void testAsyncContextPutAllEdgeCases() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create()
            ctx.putAll(null)       // should be no-op
            ctx.putAll([:])        // should be no-op
            assert ctx.isEmpty()

            ctx.putAll([a: 1, b: 2, c: 3])
            assert ctx.size() == 3
            assert ctx.get('a') == 1
        '''
    }

    @Test
    void testAsyncContextSizeAndIsEmpty() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create()
            assert ctx.size() == 0
            assert ctx.isEmpty()

            ctx.put('k', 'v')
            assert ctx.size() == 1
            assert !ctx.isEmpty()
        '''
    }

    @Test
    void testAsyncContextSnapshot() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create()
            ctx.put('key', 'value')
            def snap = ctx.snapshot()
            assert snap instanceof Map
            assert snap['key'] == 'value'

            try {
                snap['newKey'] = 'newVal'
                assert false : 'snapshot should be unmodifiable'
            } catch (UnsupportedOperationException e) {
                assert snap.size() == 1
            }
        '''
    }

    @Test
    void testAsyncContextToString() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def ctx = AsyncContext.create()
            ctx.put('x', 1)
            assert ctx.toString().startsWith('AsyncContext')
        '''
    }

    @Test
    void testAsyncContextSnapshotIsEmpty() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            // Snapshot with data should not be empty
            AsyncContext.current().put('k', 'v')
            def snap = AsyncContext.capture()
            assert !snap.isEmpty()
            assert snap.asMap()['k'] == 'v'
            AsyncContext.current().remove('k')
        '''
    }

    @Test
    void testAsyncContextSnapshotAsMap() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current().put('mapKey', 'mapVal')
            def snap = AsyncContext.capture()
            def map = snap.asMap()
            assert map['mapKey'] == 'mapVal'
            AsyncContext.current().remove('mapKey')
        '''
    }

    @Test
    void testAsyncContextSnapshotWithEmpty() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            AsyncContext.current().put('existing', 'val')
            def snap = AsyncContext.capture()
            def same = snap.with(null)
            assert same.is(snap)
            def same2 = snap.with([:])
            assert same2.is(snap)
            AsyncContext.current().remove('existing')
        '''
    }

    @Test
    void testAsyncContextSnapshotToString() {
        assertScript '''
            import groovy.concurrent.AsyncContext

            def snap = AsyncContext.capture()
            assert snap.toString().startsWith('AsyncContext.Snapshot')
        '''
    }

    // ----- Channel edge cases -----

    @Test
    void testChannelGetCapacity() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch0 = AsyncChannel.create()
            assert ch0.capacity == 0

            def ch5 = AsyncChannel.create(5)
            assert ch5.capacity == 5
        '''
    }

    @Test
    void testChannelDrainBufferToReceivers() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async drainTest() {
                def ch = AsyncChannel.create(3)

                // Fill buffer
                await ch.send('a')
                await ch.send('b')
                await ch.send('c')

                // Receive all — triggers drainBufferToReceivers internally
                assert await(ch.receive()) == 'a'
                assert await(ch.receive()) == 'b'
                assert await(ch.receive()) == 'c'
            }

            drainTest().get()
        '''
    }

    @Test
    void testChannelPollPendingSenderReturnsNull() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async pollTest() {
                def ch = AsyncChannel.create(2)
                await ch.send('x')
                // No pending senders — pollPendingSender returns null during receive
                assert await(ch.receive()) == 'x'
            }

            pollTest().get()
        '''
    }

    @Test
    void testChannelStreamViewCheckedExceptionWrap() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async checkedExceptionTest() {
                def ch = AsyncChannel.create(1)

                // Close the channel and try for-await iteration
                ch.close()
                def items = []
                for await (item in ch) {
                    items << item
                }
                assert items.isEmpty()
            }

            checkedExceptionTest().get()
        '''
    }

    @Test
    void testChannelBufferedMultipleReceiversBeforeSend() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.AsyncScope

            async multiReceiverTest() {
                def ch = AsyncChannel.create(0)

                AsyncScope.withScope { scope ->
                    // Start receiver task
                    def t1 = scope.async { await ch.receive() }

                    // Brief delay then send
                    await Awaitable.delay(50)
                    await ch.send('hello')

                    assert await(t1) == 'hello'
                }
            }

            multiReceiverTest().get()
        '''
    }

    @Test
    void testChannelToStringReflectsCapacityAndState() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            assert ch.toString().contains('AsyncChannel')
            assert ch.toString().contains('capacity=5')

            ch.close()
            assert ch.toString().contains('closed')
        '''
    }

    @Test
    void testChannelGetBufferedSize() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async bufferedSizeTest() {
                def ch = AsyncChannel.create(5)
                assert ch.bufferedSize == 0
                await ch.send('a')
                assert ch.bufferedSize == 1
                await ch.send('b')
                assert ch.bufferedSize == 2
                await ch.receive()
                assert ch.bufferedSize == 1
                ch.close()
            }

            bufferedSizeTest().get()
        '''
    }

    @Test
    void testChannelSendAfterClose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            async sendAfterClose() {
                def ch = AsyncChannel.create(1)
                ch.close()
                try {
                    await ch.send('x')
                    assert false : 'should throw'
                } catch (ChannelClosedException e) {
                    assert e instanceof ChannelClosedException
                }
            }

            sendAfterClose().get()
        '''
    }

    @Test
    void testChannelReceiveAfterCloseAndDrain() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            async receiveAfterDrain() {
                def ch = AsyncChannel.create(2)
                await ch.send('last')
                ch.close()

                assert await(ch.receive()) == 'last'

                try {
                    await ch.receive()
                    assert false : 'should throw'
                } catch (ChannelClosedException e) {
                    assert e instanceof ChannelClosedException
                }
            }

            receiveAfterDrain().get()
        '''
    }

    @Test
    void testChannelIdempotentClose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(1)
            assert ch.close()   // first close returns true
            assert !ch.close()  // second close returns false
            assert ch.isClosed()
        '''
    }

    // ---- DefaultAsyncChannel Interface Contract Tests -------------------

    @Test
    void testChannelImplementsAsyncChannelInterface() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(1)
            assert ch instanceof AsyncChannel
        '''
    }

    @Test
    void testUnbufferedChannelCapacityIsZero() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create()
            assert ch.capacity == 0
            assert ch.bufferedSize == 0
        '''
    }

    @Test
    void testBufferedChannelReportsCorrectCapacity() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            [1, 10, 100, 1000].each { cap ->
                def ch = AsyncChannel.create(cap)
                assert ch.capacity == cap
            }
        '''
    }

    @Test
    void testBufferedSizeDuringLifecycle() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async lifecycle() {
                def ch = AsyncChannel.create(3)

                assert ch.bufferedSize == 0

                await ch.send('a')
                assert ch.bufferedSize == 1

                await ch.send('b')
                assert ch.bufferedSize == 2

                await ch.receive()
                assert ch.bufferedSize == 1

                await ch.receive()
                assert ch.bufferedSize == 0
            }

            lifecycle().get()
        '''
    }

    @Test
    void testSendReceiveFIFOOrder() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async fifoTest() {
                def ch = AsyncChannel.create(5)

                await ch.send('first')
                await ch.send('second')
                await ch.send('third')

                assert await(ch.receive()) == 'first'
                assert await(ch.receive()) == 'second'
                assert await(ch.receive()) == 'third'
            }

            fifoTest().get()
        '''
    }

    @Test
    void testUnbufferedChannelRendezvousPairing() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.atomic.AtomicReference

            async rendezvousTest() {
                def ch = AsyncChannel.create()
                def received = new AtomicReference<String>()

                AsyncScope.withScope { scope ->
                    scope.async {
                        received.set(await(ch.receive()))
                    }

                    await Awaitable.delay(50)
                    await ch.send('rendezvous')
                }

                assert received.get() == 'rendezvous'
            }

            rendezvousTest().get()
        '''
    }

    @Test
    void testClosePreservesBufferedValuesForReceiver() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async closePreservesTest() {
                def ch = AsyncChannel.create(3)
                await ch.send('x')
                await ch.send('y')
                ch.close()

                // Buffered values should still be receivable
                assert await(ch.receive()) == 'x'
                assert await(ch.receive()) == 'y'

                // Now it should throw
                try {
                    await ch.receive()
                    assert false : 'should have thrown'
                } catch (groovy.concurrent.ChannelClosedException e) {
                    assert e.message.contains('closed')
                }
            }

            closePreservesTest().get()
        '''
    }

    @Test
    void testSendOnClosedChannelFailsImmediately() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            async sendClosedTest() {
                def ch = AsyncChannel.create(1)
                ch.close()

                try {
                    await ch.send('too-late')
                    assert false : 'should have thrown'
                } catch (ChannelClosedException e) {
                    assert e.message.contains('closed')
                    assert e.message.contains('send')
                }
            }

            sendClosedTest().get()
        '''
    }

    @Test
    void testAsStreamYieldsAllBufferedThenEnds() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async streamTest() {
                def ch = AsyncChannel.create(5)
                (1..5).each { await ch.send(it) }
                ch.close()

                def results = []
                for await (item in ch) {
                    results << item
                }
                assert results == [1, 2, 3, 4, 5]
            }

            streamTest().get()
        '''
    }

    @Test
    void testAsStreamIsNoOpClose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async noOpCloseTest() {
                def ch = AsyncChannel.create(1)
                def stream = ch.asStream()

                await ch.send('value')

                // Closing stream should NOT close the channel
                stream.close()
                assert !ch.isClosed()

                // Channel should still be usable
                assert await(ch.receive()) == 'value'
            }

            noOpCloseTest().get()
        '''
    }

    @Test
    void testConcurrentProducersAndConsumersNoDataLoss() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.ConcurrentSkipListSet

            async fanInTest() {
                int numProducers = 8
                int itemsPerProducer = 50
                def ch = AsyncChannel.create(16)
                def received = new ConcurrentSkipListSet<Integer>()

                AsyncScope.withScope { scope ->
                    // Spawn producers
                    numProducers.times { pid ->
                        scope.async {
                            itemsPerProducer.times { i ->
                                await ch.send(pid * itemsPerProducer + i)
                            }
                        }
                    }

                    // Spawn consumer
                    scope.async {
                        int total = numProducers * itemsPerProducer
                        total.times {
                            received.add(await(ch.receive()))
                        }
                        ch.close()
                    }
                }

                assert received.size() == numProducers * itemsPerProducer
                // Verify all values present
                (0..<(numProducers * itemsPerProducer)).each { assert it in received }
            }

            fanInTest().get()
        '''
    }

    @Test
    void testChannelWithZeroCapacityToString() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create()
            def str = ch.toString()
            assert str.contains('AsyncChannel')
            assert str.contains('capacity=0')
            assert str.contains('closed=false')
        '''
    }

    @Test
    void testNewChannelIsNotClosed() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(5)
            assert !ch.isClosed()
            assert ch.bufferedSize == 0
        '''
    }

    @Test
    void testReceiveFromClosedEmptyChannelFails() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            async receiveClosedEmpty() {
                def ch = AsyncChannel.create()
                ch.close()

                try {
                    await ch.receive()
                    assert false : 'should have thrown'
                } catch (ChannelClosedException e) {
                    assert e.message.contains('closed')
                }
            }

            receiveClosedEmpty().get()
        '''
    }

    @Test
    void testPendingSenderTimesOutGracefully() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.TimeoutException

            async timeoutTest() {
                def ch = AsyncChannel.create()  // unbuffered

                // Send with timeout — no receiver, so it should time out
                try {
                    await Awaitable.orTimeoutMillis(ch.send('data'), 100)
                    assert false : 'should have timed out'
                } catch (TimeoutException e) {
                    // expected
                }

                // Channel should still be usable
                assert !ch.isClosed()
            }

            timeoutTest().get()
        '''
    }

    @Test
    void testPendingReceiverTimesOutGracefully() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.TimeoutException

            async receiveTimeoutTest() {
                def ch = AsyncChannel.create()  // unbuffered

                // Receive with timeout — no sender, so it should time out
                try {
                    await Awaitable.orTimeoutMillis(ch.receive(), 100)
                    assert false : 'should have timed out'
                } catch (TimeoutException e) {
                    // expected
                }

                // Channel should still be usable
                assert !ch.isClosed()
            }

            receiveTimeoutTest().get()
        '''
    }

    @Test
    void testBufferedChannelSenderDoesNotBlock() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            async bufferNonBlocking() {
                def ch = AsyncChannel.create(3)

                // All three sends should complete immediately
                await ch.send('a')
                await ch.send('b')
                await ch.send('c')

                assert ch.bufferedSize == 3
                assert ch.capacity == 3
            }

            bufferNonBlocking().get()
        '''
    }

    @Test
    void testRefillBufferFromWaitingSenders() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.AsyncScope

            async refillTest() {
                def ch = AsyncChannel.create(1)  // capacity 1

                await ch.send('first')  // fills buffer

                // This sender will wait (buffer is full)
                def sendResult
                AsyncScope.withScope { scope ->
                    scope.async {
                        await ch.send('second')  // queued as waiting sender
                        sendResult = 'sent'
                    }

                    await Awaitable.delay(50)

                    // Receive 'first' — should refill buffer from waiting sender
                    assert await(ch.receive()) == 'first'
                    // 'second' should now be in the buffer
                    assert await(ch.receive()) == 'second'
                }

                assert sendResult == 'sent'
            }

            refillTest().get()
        '''
    }

    // ---- AsyncChannel.create() factory tests ----------------------------

    @Test
    void testAsyncChannelCreateReturnsUnbufferedChannel() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create()
            assert ch != null
            assert ch.getCapacity() == 0
            assert !ch.isClosed()
            assert ch.getBufferedSize() == 0
        '''
    }

    @Test
    void testAsyncChannelCreateWithCapacityReturnsBufferedChannel() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.<String>create(5)
            assert ch != null
            assert ch.getCapacity() == 5
            assert !ch.isClosed()
        '''
    }

    @Test
    void testAsyncChannelCreateWithZeroCapacityIsRendezvous() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch = AsyncChannel.create(0)
            assert ch.getCapacity() == 0
        '''
    }

    @Test
    void testAsyncChannelCreateWithNegativeCapacityThrows() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            try {
                AsyncChannel.create(-1)
                assert false : "Should have thrown"
            } catch (IllegalArgumentException e) {
                assert e.message.contains('negative')
            }
        '''
    }

    @Test
    void testAsyncChannelCreateProducesFunctionalChannel() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable

            def ch = AsyncChannel.<Integer>create(3)
            ch.send(10)
            ch.send(20)
            ch.send(30)
            ch.close()

            def results = []
            def stream = ch.asStream()
            while (stream.moveNext().get()) {
                results << stream.current
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testAsyncChannelCreateRendezvousSendReceivePairing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def ch = AsyncChannel.<String>create()
            def latch = new CountDownLatch(1)
            def received = null

            Awaitable.go {
                received = await ch.receive()
                latch.countDown()
            }

            // Small delay to let receiver register
            Thread.sleep(50)
            await ch.send('hello')
            assert latch.await(5, TimeUnit.SECONDS)
            assert received == 'hello'
        '''
    }

    // ---- Cancellation cleanup robustness tests --------------------------

    @Test
    void testCancelledSendCleansUpFromQueue() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def ch = AsyncChannel.<String>create()  // unbuffered

            // Send without a receiver — will be queued
            def sendAwaitable = ch.send('value')

            // Cancel the send
            sendAwaitable.cancel()

            // The channel should still function properly
            def latch = new CountDownLatch(1)
            def received = null
            Awaitable.go {
                await ch.send('after-cancel')
            }

            Thread.sleep(50)
            received = ch.receive().get()
            assert received == 'after-cancel'
            ch.close()
        '''
    }

    @Test
    void testCancelledReceiveCleansUpFromQueue() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def ch = AsyncChannel.<String>create()  // unbuffered

            // Receive without a sender — will be queued
            def receiveAwaitable = ch.receive()

            // Cancel the receive
            receiveAwaitable.cancel()

            // The channel should still function properly
            Awaitable.go { await ch.send('after-cancel') }
            Thread.sleep(50)
            def v = ch.receive().get()
            assert v == 'after-cancel'
            ch.close()
        '''
    }

    @Test
    void testMultipleCancelledSendsDoNotLeakMemory() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable

            def ch = AsyncChannel.<Integer>create()  // unbuffered

            // Queue many sends then cancel them all
            def sends = (1..100).collect { ch.send(it) }
            sends.each { it.cancel() }

            // Channel should still work
            Awaitable.go { await ch.send(999) }
            Thread.sleep(50)
            def val = ch.receive().get()
            assert val == 999
            ch.close()
        '''
    }

    // ================================================================
    // AsyncScope: structured concurrency via async/await syntax
    // ================================================================

    @Test
    void testAsyncScopeChildrenCompleteBeforeScopeExits() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicInteger

            def counter = new AtomicInteger(0)

            AsyncScope.withScope { scope ->
                scope.async {
                    await Awaitable.delay(50)
                    counter.incrementAndGet()
                    return null
                }
                scope.async {
                    await Awaitable.delay(50)
                    counter.incrementAndGet()
                    return null
                }
            }
            // After withScope returns, both children must have completed
            assert counter.get() == 2
        '''
    }

    @Test
    void testAsyncScopeFailFastPropagatesError() {
        shouldFail '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                scope.async { throw new RuntimeException('scope-fail') }
                scope.async {
                    Thread.sleep(5000)
                    return null
                }
            }
        '''
    }

    @Test
    void testAsyncScopeContextPropagation() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.AsyncScope

            AsyncContext.current().put('scopeKey', 'scopeValue')

            def result = AsyncScope.withScope { scope ->
                def task = scope.async {
                    return AsyncContext.current().get('scopeKey')
                }
                return await task
            }
            assert result == 'scopeValue'
        '''
    }

    // ================================================================
    // AwaitResult: functional composition via async/await syntax
    // ================================================================

    @Test
    void testAwaitResultMapInAsyncContext() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitResult

            async compute() {
                def results = await Awaitable.allSettled(
                    Awaitable.of(10),
                    Awaitable.of(20),
                    Awaitable.failed(new RuntimeException('err'))
                )

                // Use map to transform successful results
                def mapped = results.collect { it.map { v -> v * 2 } }

                assert mapped[0].isSuccess()
                assert mapped[0].value == 20
                assert mapped[1].isSuccess()
                assert mapped[1].value == 40
                assert mapped[2].isFailure()
                assert mapped[2].error.message == 'err'
            }

            await compute()
        '''
    }

    @Test
    void testAwaitResultEqualsInAsyncContext() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitResult

            async compare() {
                def a = await Awaitable.allSettled(Awaitable.of(42))
                def b = await Awaitable.allSettled(Awaitable.of(42))

                assert a[0] == b[0] : "Equal success results must be equal"
                assert a[0].hashCode() == b[0].hashCode()
            }

            await compare()
        '''
    }

    // ================================================================
    // ChannelClosedException: exception transparency via async/await
    // ================================================================

    @Test
    void testChannelClosedExceptionTransparentInForAwait() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable

            async consumeChannel() {
                def ch = AsyncChannel.<String>create(3)
                await ch.send('alpha')
                await ch.send('beta')
                ch.close()

                def items = []
                for await (item in ch) {
                    items << item
                }
                // for-await translates ChannelClosedException to clean exit
                assert items == ['alpha', 'beta']
            }

            await consumeChannel()
        '''
    }
}
