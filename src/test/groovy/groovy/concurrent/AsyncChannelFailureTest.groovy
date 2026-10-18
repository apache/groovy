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
package groovy.concurrent

import org.apache.groovy.runtime.async.AsyncSupport
import org.apache.groovy.runtime.async.GroovyPromise
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Predicate

import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * A producer closes the channel with the failure it caught.
 * {@code for await} throws that failure. The reader writes the catch.
 * {@code await receive()} still fails with {@link ChannelClosedException}.
 */
final class AsyncChannelFailureTest {

    @Test
    void normalCloseEndsTheLoop() {
        def channel = AsyncChannel.create(2)
        await channel.send(1)
        await channel.send(2)
        assert channel.close()
        assert !channel.close()
        assert channel.closeCause == null

        def seen = []
        for await (value in channel) {
            seen << value
        }
        assert seen == [1, 2]
    }

    @Test
    void closeNullIsANormalClose() {
        def channel = AsyncChannel.create(1)
        assert channel.close(null)
        assert !channel.close(null)
        assert channel.closed
        assert channel.closeCause == null
        def received = assertThrows(ChannelClosedException) {
            await channel.receive()
        }
        assert received.cause == null
        def sent = assertThrows(ChannelClosedException) {
            await channel.send('late')
        }
        assert sent.cause == null
    }

    @Test
    void callerCatchesTheCheckedFailureFromForAwait() {
        def channel = AsyncChannel.create(1)
        def failure = new IOException('disk')
        async {
            try {
                throw failure
            } catch (Exception caught) {
                channel.close(caught)
            }
        }
        def error = assertThrows(IOException) {
            for await (ignored in channel) {
            }
        }
        assert error.is(failure)
    }

    @Test
    void forAwaitThrowsTheFailureAfterBufferedValues() {
        def channel = AsyncChannel.create(2)
        def failure = new IllegalStateException('boom')
        await channel.send(1)
        await channel.send(2)
        channel.close(failure)

        assert await(channel.receive()) == 1
        assert await(channel.receive()) == 2
        def closed = assertThrows(ChannelClosedException) {
            await channel.receive()
        }
        assert closed.cause.is(failure)

        def again = AsyncChannel.create(2)
        await again.send(1)
        await again.send(2)
        again.close(failure)
        def seen = []
        def error = assertThrows(IllegalStateException) {
            for await (value in again) {
                seen << value
            }
        }
        assert seen == [1, 2]
        assert error.is(failure)
        assert again.closeCause.is(failure)
    }

    @Test
    void forAwaitThrowsAChannelClosedExceptionUsedAsTheFailure() {
        def channel = AsyncChannel.create(1)
        def failure = new ChannelClosedException('not a clean end')
        channel.close(failure)

        def iterator = channel.iterator()
        def error = assertThrows(ChannelClosedException) {
            iterator.hasNext()
        }
        assert error.is(failure)
        assert !iterator.hasNext()
    }

    @Test
    void forAwaitThrowsCheckedExceptionsAndErrors() {
        def checked = AsyncChannel.create(1)
        def io = new IOException('disk')
        checked.close(io)
        def received = assertThrows(ChannelClosedException) {
            await checked.receive()
        }
        assert received.cause.is(io)

        def iterated = AsyncChannel.create(1)
        iterated.close(io)
        def iterator = iterated.iterator()
        def thrown = assertThrows(IOException) {
            iterator.hasNext()
        }
        assert thrown.is(io)
        assert !iterator.hasNext()

        def serious = AsyncChannel.create(1)
        def failure = new AssertionError('ae')
        serious.close(failure)
        def seriousIterator = serious.iterator()
        def error = assertThrows(AssertionError) {
            seriousIterator.hasNext()
        }
        assert error.is(failure)
        assert !seriousIterator.hasNext()
    }

    @Test
    void sendAndPendingOperationsCarryTheFailure() {
        def closed = AsyncChannel.create(1)
        def failure = new IllegalStateException('boom')
        closed.close(failure)
        def sent = assertThrows(ChannelClosedException) {
            await closed.send('x')
        }
        assert sent.cause.is(failure)

        def senders = AsyncChannel.create(0)
        def pendingSend = senders.send('x')
        def sendFailure = new IllegalStateException('send-boom')
        senders.close(sendFailure)
        def sendError = assertThrows(ChannelClosedException) {
            await pendingSend
        }
        assert sendError.cause.is(sendFailure)

        def receivers = AsyncChannel.create(0)
        def pendingReceive = receivers.receive()
        def receiveFailure = new IllegalStateException('receive-boom')
        receivers.close(receiveFailure)
        def receiveError = assertThrows(ChannelClosedException) {
            await pendingReceive
        }
        assert receiveError.cause.is(receiveFailure)
    }

    @Test
    void firstCloseWins() {
        def channel = AsyncChannel.create(0)
        def first = new IllegalStateException('first')
        def second = new IllegalStateException('second')
        assert channel.close(first)
        assert !channel.close(second)
        assert !channel.close()
        assert channel.closeCause.is(first)

        def later = AsyncChannel.create(0)
        assert later.close()
        assert !later.close(new IllegalStateException('late'))
        assert later.closeCause == null
        def error = assertThrows(ChannelClosedException) {
            await later.receive()
        }
        assert error.cause == null
    }

    @Test
    void closingATimerStillCancelsIt() {
        def timer = AsyncChannel.after(60_000)
        assert timer.close()
        assert timer.closed
        assert timer.closeCause == null
        assert !timer.close(new IllegalStateException('late'))
        assert timer.closeCause == null
    }

    @Test
    void defaultCloseDoesNotRetainAFailure() {
        def channel = new CloseOnlyChannel()
        assert channel.close(new IllegalStateException('not retained'))
        assert channel.closed
        assert channel.closeCause == null
        assert !channel.close(null)
        assert channel.closed
    }

    @Test
    void stagesDeliverTheFailureToForAwait() {
        def fed = AsyncChannel.create(4)
        def filtered = fed.filter {
            if (it == 2) throw new IllegalStateException('bad-item')
            it % 2 == 0
        }
        async {
            await fed.send(1)
            await fed.send(2)
            fed.close()
        }
        def seen = []
        def error = assertThrows(IllegalStateException) {
            for await (value in filtered) {
                seen << value
            }
        }
        assert seen.isEmpty()
        assert error.message == 'bad-item'

        def upstream = AsyncChannel.create(4)
        def failure = new IllegalStateException('map-boom')
        def mapped = upstream.map { it * 10 }
        async {
            await upstream.send(2)
            upstream.close(failure)
        }
        def mappedSeen = []
        def mappedError = assertThrows(IllegalStateException) {
            for await (value in mapped) {
                mappedSeen << value
            }
        }
        assert mappedSeen == [20]
        assert mappedError.is(failure)
    }

    @Test
    void mergeSplitAndTapCarryTheFailure() {
        def left = AsyncChannel.create(2)
        def right = AsyncChannel.create(0)
        def merged = left.merge(right)
        def mergeFailure = new IllegalStateException('merge-boom')
        async {
            await left.send(1)
            left.close(mergeFailure)
        }
        def mergedSeen = []
        def mergedConsumption = async {
            for await (value in merged) {
                mergedSeen << value
            }
        }
        try {
            def mergedError = assertThrows(IllegalStateException) {
                await mergedConsumption.orTimeoutMillis(2000)
            }
            assert mergedSeen == [1]
            assert mergedError.is(mergeFailure)
            assert !right.closed
        } finally {
            right.close()
        }

        def splitSource = AsyncChannel.create(4)
        def parts = splitSource.split({ it >= 5 } as Predicate)
        async {
            await splitSource.send(1)
            await splitSource.send(6)
            splitSource.close(new IllegalStateException('split-boom'))
        }
        def high = []
        def highError = assertThrows(IllegalStateException) {
            for await (value in parts[0]) {
                high << value
            }
        }
        def low = []
        def lowError = assertThrows(IllegalStateException) {
            for await (value in parts[1]) {
                low << value
            }
        }
        assert high == [6]
        assert low == [1]
        assert highError.message == 'split-boom'
        assert lowError.message == 'split-boom'

        def tapSource = AsyncChannel.create(2)
        def monitor = AsyncChannel.create(2)
        def passed = tapSource.tap(monitor)
        async {
            await tapSource.send('a')
            tapSource.close(new IllegalStateException('tap-boom'))
        }
        def passedSeen = []
        def tapError = assertThrows(IllegalStateException) {
            for await (value in passed) {
                passedSeen << value
            }
        }
        assert passedSeen == ['a']
        assert tapError.message == 'tap-boom'
        monitor.close()
        def copied = []
        for (value in monitor) {
            copied << value
        }
        assert copied == ['a']
    }

    @Test
    void selectThrowsTheFailureAndIgnoresAWithdrawnBranch() {
        def failed = AsyncChannel.create(0)
        def open = AsyncChannel.create(1)
        def pending = ChannelSelect.from(failed, open).select()
        def failure = new IllegalStateException('boom')
        failed.close(failure)
        def error = assertThrows(IllegalStateException) {
            await pending
        }
        assert error.is(failure)
        await open.send('kept')
        assert await(open.receive().orTimeoutMillis(1000)) == 'kept'

        def marked = AsyncChannel.create(0)
        def marker = new ChannelClosedException('not a clean end')
        def markedSelect = ChannelSelect.from(marked).select()
        marked.close(marker)
        def markedError = assertThrows(ChannelClosedException) {
            await markedSelect
        }
        assert markedError.is(marker)

        def first = AsyncChannel.create(0)
        def second = AsyncChannel.create(0)
        first.close()
        second.close()
        def closed = assertThrows(ChannelClosedException) {
            await ChannelSelect.from(first, second).select()
        }
        assert closed.message == 'all channels are closed'

        def ready = AsyncChannel.create(1)
        def other = AsyncChannel.create(1)
        def race = ChannelSelect.from(ready, other).select()
        await ready.send('win')
        def result = await race
        assert result.value == 'win'
        assert result.index == 0
        await other.send('still-open')
        assert await(other.receive()) == 'still-open'
    }

    @Test
    void selectKeepsACommittedValueWhenAnotherChannelFailsLater() {
        def left = AsyncChannel.create(0)
        def right = AsyncChannel.create(0)
        def pending = ChannelSelect.from(left, right).select()
        await left.send('win')
        def failure = new IllegalStateException('late')
        right.close(failure)

        def result = await pending
        assert result.value == 'win'
        assert result.index == 0
        assert right.closeCause.is(failure)

        def failedFirst = AsyncChannel.create(0)
        def buffered = AsyncChannel.create(1)
        def earlier = new IllegalStateException('earlier')
        failedFirst.close(earlier)
        await buffered.send('kept')
        def error = assertThrows(IllegalStateException) {
            await ChannelSelect.from(failedFirst, buffered).select()
        }
        assert error.is(earlier)
        assert await(buffered.receive()) == 'kept'

        def sendFailed = AsyncChannel.create(0)
        sendFailed.close(failure)
        def sendError = assertThrows(IllegalStateException) {
            await ChannelSelect.offers(ChannelSelect.send(sendFailed, 'x')).select()
        }
        assert sendError.is(failure)

        def ready = AsyncChannel.create(1)
        def failedLater = AsyncChannel.create(0)
        await ready.send('win')
        assert failedLater.close(failure)
        def preferred = await ChannelSelect.from(ready, failedLater).select()
        assert preferred.value == 'win'
        assert preferred.index == 0
        assert failedLater.closeCause.is(failure)

        def bufferedFailure = AsyncChannel.create(1)
        def untouched = AsyncChannel.create(1)
        await bufferedFailure.send('kept')
        assert bufferedFailure.close(failure)
        await untouched.send('later')
        def bufferedResult = await ChannelSelect.from(bufferedFailure, untouched).select()
        assert bufferedResult.index == 0
        assert bufferedResult.value == 'kept'
        assert await(untouched.receive()) == 'later'
    }

    @Test
    void selectKeepsACommittedTransferWhenACloseLosesTheClaim() {
        // Not a DefaultAsyncChannel, so select completes the branch future
        // before it can withdraw the sibling. Cancel is a no-op, which lets
        // the sibling still report its close after the transfer has committed.
        def value = new ManualChannel()
        def failed = new ManualChannel()
        def pending = ChannelSelect.from(value, failed).select()
        value.future.complete('win')
        failed.future.completeExceptionally(
                new ChannelClosedException('closed', new IllegalStateException('late')))

        def result = await pending
        assert result.value == 'win'
        assert result.index == 0
    }

    @Test
    void closingOneSplitOutputDoesNotFailTheOther() {
        def source = AsyncChannel.create(4)
        def parts = source.split({ it >= 5 } as Predicate)
        def side = new IllegalStateException('only-high')
        parts[0].close(side)
        async {
            await source.send(1)
            await source.send(6)
            source.close()
        }
        def low = []
        for await (value in parts[1]) {
            low << value
        }
        assert low == [1]
        assert parts[1].closeCause == null
        def highError = assertThrows(IllegalStateException) {
            for await (ignored in parts[0]) {
            }
        }
        assert highError.is(side)
    }

    @Test
    void tapIgnoresAClosedSideChannel() {
        def source = AsyncChannel.create(2)
        def monitor = AsyncChannel.create(1)
        def side = new IllegalStateException('side')
        monitor.close(side)
        def passed = source.tap(monitor)
        def sourceFailure = new IllegalStateException('source')
        async {
            await source.send('a')
            await source.send('b')
            source.close(sourceFailure)
        }
        def seen = []
        def error = assertThrows(IllegalStateException) {
            for await (value in passed) {
                seen << value
            }
        }
        assert seen == ['a', 'b']
        assert error.is(sourceFailure)
        assert monitor.closeCause.is(side)
    }

    @Test
    void produceClosesNormallyAndOnFailure() {
        def squares = AsyncChannel.produce {
            it.send(1)
            it.send(2)
            assert it.channel.capacity == 0
        }
        def seen = []
        for await (value in squares) {
            seen << value
        }
        assert seen == [1, 2]
        assert squares.closeCause == null
        assert !squares.close()

        def channel = AsyncChannel.produce(2) {
            it.send(1)
            throw new IOException('disk')
        }
        def buffered = []
        def error = assertThrows(IOException) {
            for await (value in channel) {
                buffered << value
            }
        }
        assert buffered == [1]
        assert error.message == 'disk'
        assert channel.closeCause.is(error)

        def serious = AsyncChannel.produce(1) {
            throw new AssertionError('ae')
        }
        def seriousError = assertThrows(AssertionError) {
            for await (ignored in serious) {
            }
        }
        assert seriousError.message == 'ae'
        assert serious.closeCause.is(seriousError)

        def marked = AsyncChannel.produce(1) {
            throw new ChannelClosedException('not a clean end')
        }
        def markedError = assertThrows(ChannelClosedException) {
            for await (ignored in marked) {
            }
        }
        assert markedError.message == 'not a clean end'
        assert marked.closeCause.is(markedError)
    }

    @Test
    void produceSendUnblocksWhenTheConsumerCloses() {
        def channel = AsyncChannel.produce(0) {
            it.send('a')
            it.send('b')
        }
        assert await(channel.receive()) == 'a'
        assert channel.close()
        assert channel.closeCause == null
        def closed = assertThrows(ChannelClosedException) {
            await channel.receive().orTimeoutMillis(2000)
        }
        assert closed.cause == null
    }

    @Test
    void produceRejectsANullBodyAndANegativeCapacity() {
        assertThrows(NullPointerException) {
            AsyncChannel.produce(null)
        }
        assertThrows(NullPointerException) {
            AsyncChannel.produce(1, null)
        }
        assertThrows(IllegalArgumentException) {
            AsyncChannel.produce(-1) { }
        }
    }

    @Test
    void produceSendRejectsNull() {
        def channel = AsyncChannel.produce(1) {
            it.send(null)
        }
        def error = assertThrows(NullPointerException) {
            for await (ignored in channel) {
            }
        }
        assert channel.closeCause.is(error)
    }

    @Test
    void mergeKeepsOneFailureWhenBothInputsFail() {
        def left = AsyncChannel.create(1)
        def right = AsyncChannel.create(1)
        def leftFailure = new IllegalStateException('left')
        def rightFailure = new IllegalStateException('right')
        left.close(leftFailure)
        right.close(rightFailure)
        def merged = left.merge(right)
        def error = assertThrows(IllegalStateException) {
            for await (ignored in merged) {
            }
        }
        assert error.is(leftFailure) || error.is(rightFailure)
    }

    @Test
    void mergeForwardsAValueWhenCancelLosesTheCommit() {
        def left = AsyncChannel.create(0)
        def failure = new IllegalStateException('left')
        assert left.close(failure)
        def right = new CancelLosesChannel()
        def queue = new LinkedBlockingQueue<Runnable>()
        AsyncSupport.setExecutor({ Runnable task -> queue.add(task) } as Executor)
        AsyncChannel<String> merged
        try {
            merged = left.merge(right)
            assert queue.size() == 2
            def leftTask = queue.take()
            def rightTask = queue.take()
            def leftThread = Thread.startDaemon { leftTask.run() }
            waitUntil { leftThread.state == Thread.State.WAITING }
            def rightThread = Thread.startDaemon { rightTask.run() }
            // The relays are already running. Restore the executor so the
            // consumer below is not queued behind them.
            AsyncSupport.resetExecutor()
            def seen = []
            def consumption = async {
                for await (value in merged) {
                    seen << value
                }
            }
            def error = assertThrows(IllegalStateException) {
                await consumption.orTimeoutMillis(2000)
            }
            assert seen == ['kept']
            assert error.is(failure)
            leftThread.join(2000)
            rightThread.join(2000)
            assert !leftThread.alive
            assert !rightThread.alive
        } finally {
            AsyncSupport.resetExecutor()
            right.close()
            merged?.close()
        }
    }

    private static void waitUntil(Closure<Boolean> condition) {
        long deadline = System.currentTimeMillis() + 2000
        while (!condition.call()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError('waitUntil timed out after 2000ms')
            }
            Thread.sleep(2)
        }
    }

    @Test
    void mergeClosesAsSoonAsOneInputFails() {
        def left = AsyncChannel.create(1)
        def right = AsyncChannel.create(0)
        def failure = new IOException('sensor failed')
        await left.send(7)
        assert left.close(failure)
        def merged = left.merge(right)
        def seen = []
        def consumption = async {
            for await (value in merged) {
                seen << value
            }
        }
        try {
            def error = assertThrows(IOException) {
                await consumption.orTimeoutMillis(2000)
            }
            assert seen == [7]
            assert error.is(failure)
            assert merged.closeCause.is(failure)
            assert !right.closed
        } finally {
            right.close()
            merged.close()
        }
    }

    @Test
    void mergeForwardsABufferedSiblingValueBeforeTheFailure() {
        def left = AsyncChannel.create(1)
        def right = AsyncChannel.create(1)
        def failure = new IllegalStateException('left')
        await left.send(1)
        await right.send(2)
        assert left.close(failure)
        def merged = left.merge(right)
        def seen = []
        def consumption = async {
            for await (value in merged) {
                seen << value
            }
        }
        try {
            def error = assertThrows(IllegalStateException) {
                await consumption.orTimeoutMillis(2000)
            }
            assert seen.sort() == [1, 2]
            assert error.is(failure)
            assert merged.closeCause.is(failure)
            assert !right.closed
            assert right.bufferedSize == 0
            right.close()
            assert merged.closeCause.is(failure)
        } finally {
            right.close()
            merged.close()
        }
    }

    @Test
    void mergeRejectsANullInput() {
        def channel = AsyncChannel.create(0)
        assertThrows(NullPointerException) {
            channel.merge(null)
        }
        assert !channel.closed
    }

    @Test
    void iterationHelpersThrowTheFailure() {
        def failure = new IOException('disk')

        def forChannel = AsyncChannel.create(2)
        await forChannel.send(1)
        await forChannel.send(2)
        assert forChannel.close(failure)
        def forSeen = []
        def forError = assertThrows(IOException) {
            for (value in forChannel) {
                forSeen << value
            }
        }
        assert forSeen == [1, 2]
        assert forError.is(failure)

        def eachChannel = AsyncChannel.create(2)
        await eachChannel.send(1)
        await eachChannel.send(2)
        assert eachChannel.close(failure)
        def eachSeen = []
        def eachError = assertThrows(IOException) {
            eachChannel.each { eachSeen << it }
        }
        assert eachSeen == [1, 2]
        assert eachError.is(failure)

        def collectChannel = AsyncChannel.create(2)
        await collectChannel.send(1)
        await collectChannel.send(2)
        assert collectChannel.close(failure)
        def collectSeen = []
        def collectError = assertThrows(IOException) {
            collectChannel.collect { collectSeen << it; it }
        }
        assert collectSeen == [1, 2]
        assert collectError.is(failure)

        def listChannel = AsyncChannel.create(1)
        await listChannel.send(1)
        assert listChannel.close(failure)
        def listError = assertThrows(IOException) {
            listChannel.toList()
        }
        assert listError.is(failure)
    }

    @Test
    void receiveLoopReadsTheFailureFromTheCause() {
        def channel = AsyncChannel.create(2)
        await channel.send(1)
        await channel.send(2)
        def failure = new IOException('disk')
        assert channel.close(failure)
        def seen = []
        while (true) {
            try {
                seen << await(channel.receive())
            } catch (ChannelClosedException closed) {
                assert closed.cause.is(failure)
                break
            }
        }
        assert seen == [1, 2]
        def sent = assertThrows(ChannelClosedException) {
            await channel.send('late')
        }
        assert sent.cause.is(failure)
    }

    /**
     * Reports a buffered value, then a receive whose {@code cancel()} loses
     * to a sender that has already committed. {@code isDone()} stays false
     * so {@code relay} takes the cancel branch.
     */
    private static final class CancelLosesChannel implements AsyncChannel<String> {
        private final LateCommitPromise promise = new LateCommitPromise()
        private int buffered = 1

        @Override
        int getCapacity() { 1 }

        @Override
        int getBufferedSize() { buffered }

        @Override
        boolean isClosed() { false }

        @Override
        Awaitable<Void> send(String value) {
            throw new UnsupportedOperationException('send')
        }

        @Override
        Awaitable<String> receive() {
            buffered = 0
            promise
        }

        @Override
        boolean close() { false }

        @Override
        Iterator<String> iterator() {
            throw new UnsupportedOperationException('iterator')
        }
    }

    private static final class LateCommitPromise extends GroovyPromise<String> {
        private final CompletableFuture<String> committed = new CompletableFuture<>()

        LateCommitPromise() {
            super(new CompletableFuture<>())
        }

        @Override
        boolean isDone() { false }

        @Override
        boolean cancel() {
            committed.complete('kept')
            return false
        }

        @Override
        String get() {
            committed.get()
        }
    }

    /**
     * A channel whose one receive completes only when the test completes
     * {@link #future}. {@link #cancel()} does nothing, so a select cannot
     * retire the branch by withdrawing it.
     */
    private static final class ManualChannel implements AsyncChannel<String> {
        final CompletableFuture<String> future = new CompletableFuture<>()

        @Override
        int getCapacity() { 0 }

        @Override
        int getBufferedSize() { 0 }

        @Override
        boolean isClosed() { false }

        @Override
        Awaitable<Void> send(String value) {
            throw new UnsupportedOperationException('send')
        }

        @Override
        Awaitable<String> receive() {
            new StickyPromise(future)
        }

        @Override
        boolean close() { false }

        @Override
        Iterator<String> iterator() {
            throw new UnsupportedOperationException('iterator')
        }
    }

    private static final class StickyPromise extends GroovyPromise<String> {
        StickyPromise(CompletableFuture<String> future) {
            super(future)
        }

        @Override
        boolean cancel() { false }
    }

    /** Does not override {@code close(Throwable)}, so the interface default applies. */
    private static final class CloseOnlyChannel implements AsyncChannel<String> {
        private boolean closed

        @Override
        int getCapacity() { 0 }

        @Override
        int getBufferedSize() { 0 }

        @Override
        boolean isClosed() { closed }

        @Override
        Awaitable<Void> send(String value) {
            throw new UnsupportedOperationException('send')
        }

        @Override
        Awaitable<String> receive() {
            throw new UnsupportedOperationException('receive')
        }

        @Override
        boolean close() {
            if (closed) {
                return false
            }
            closed = true
            return true
        }

        @Override
        Iterator<String> iterator() {
            throw new UnsupportedOperationException('iterator')
        }
    }
}
