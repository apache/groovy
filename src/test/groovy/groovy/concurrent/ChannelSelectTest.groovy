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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class ChannelSelectTest {

    @Test
    void testSelectFirstAvailable() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def ch1 = AsyncChannel.create(10)
            def ch2 = AsyncChannel.create(10)

            def sel = ChannelSelect.from(ch1, ch2)

            async {
                Thread.sleep(50)
                ch1.send('from-ch1')
            }

            def result = await sel.select()
            assert result.index == 0
            assert result.value == 'from-ch1'
        '''
    }

    @Test
    void testSelectFromMultiple() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def fast = AsyncChannel.create(10)
            def slow = AsyncChannel.create(10)

            def sel = ChannelSelect.from(slow, fast)

            async { fast.send('fast-wins') }
            async { Thread.sleep(200); slow.send('slow') }

            def result = await sel.select()
            assert result.value == 'fast-wins'
            assert result.index == 1
        '''
    }

    // GROOVY-12320: select() must take exactly one value from exactly one channel

    @Test
    void testLosingChannelKeepsItsContentsAndOrder() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)
            a.send('a1')
            b.send('b1'); b.send('b2')

            def result = await ChannelSelect.from(a, b).select()
            assert result.index == 0 && result.value == 'a1'

            assert b.bufferedSize == 2
            def first = await b.receive()
            def second = await b.receive()
            assert [first, second] == ['b1', 'b2']
        '''
    }

    @Test
    void testFirstListedChannelWinsWhenSeveralAreReady() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            for (i in 0..<20) { a.send('a' + i); b.send('b' + i) }

            def sel = ChannelSelect.from(b, a)
            for (i in 0..<20) {
                def result = await sel.select()
                assert result.index == 0 && result.value == 'b' + i
            }
            assert a.bufferedSize == 20 && b.bufferedSize == 0
        '''
    }

    @Test
    void testLosingChannelsRetainNoReceiverAfterSelect() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def busy = AsyncChannel.create(4)
            def quiet = AsyncChannel.create(4)

            // quiet is empty on every iteration: it must not accumulate receivers
            for (i in 0..<5000) {
                busy.send(i)
                def result = await ChannelSelect.from(busy, quiet).select()
                assert result.index == 0 && result.value == i
            }
            assert quiet.toString().contains('waitingReceivers=0')

            // the next message on quiet is buffered and received intact
            quiet.send(42)
            assert quiet.bufferedSize == 1
            def value = await quiet.receive()
            assert value == 42
        '''
    }

    @Test
    void testLosingReceiversAreWithdrawnWhenTheWinnerArrivesLater() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)

            async { Thread.sleep(50); a.send('late') }

            def result = await ChannelSelect.from(a, b).select()
            assert result.index == 0 && result.value == 'late'
            assert b.toString().contains('waitingReceivers=0')
            assert a.toString().contains('waitingReceivers=0')

            b.send('b1')
            assert b.bufferedSize == 1
        '''
    }

    @Test
    void testLosingRendezvousChannelStillRendezvous() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def busy = AsyncChannel.create(4)
            def quiet = AsyncChannel.create()   // unbuffered

            async { Thread.sleep(50); busy.send(1) }
            def result = await ChannelSelect.from(busy, quiet).select()
            assert result.index == 0

            // no receiver is waiting on quiet, so a send must not complete
            def pending = quiet.send(42)
            assert !pending.toCompletableFuture().isDone()

            def value = await quiet.receive()
            assert value == 42
            assert pending.toCompletableFuture().isDone()
        '''
    }

    @Test
    void testTimedOutSelectConsumesNothing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect
            import java.util.concurrent.TimeoutException
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)

            shouldFail(TimeoutException) {
                await ChannelSelect.from(a, b).select().orTimeoutMillis(50)
            }
            assert a.toString().contains('waitingReceivers=0')
            assert b.toString().contains('waitingReceivers=0')

            a.send('x'); a.send('y')
            assert a.bufferedSize == 2
            def first = await a.receive()
            assert first == 'x'
        '''
    }

    @Test
    void testSelectFailsWhenAllChannelsAreClosed() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import groovy.concurrent.ChannelSelect
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create(1)
            def b = AsyncChannel.create(1)
            a.close(); b.close()

            shouldFail(ChannelClosedException) {
                await ChannelSelect.from(a, b).select().orTimeoutMillis(1000)
            }

            // closed later, while the select is pending
            def c = AsyncChannel.create(1)
            def d = AsyncChannel.create(1)
            c.close()
            async { Thread.sleep(50); d.close() }
            shouldFail(ChannelClosedException) {
                await ChannelSelect.from(c, d).select().orTimeoutMillis(1000)
            }
        '''
    }

    @Test
    void testSelectStillWaitsWhileOneChannelIsOpen() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def closed = AsyncChannel.create(1)
            def open = AsyncChannel.create(1)
            closed.close()

            async { Thread.sleep(50); open.send('still here') }
            def result = await ChannelSelect.from(closed, open).select()
            assert result.index == 1 && result.value == 'still here'
        '''
    }

    @Test
    void testCancellingSelectWithdrawsOrIsRefusedOnceDelivered() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)

            // nothing ready: cancel wins the claim and withdraws every branch
            def pending = ChannelSelect.from(a, b).select()
            assert pending.cancel()
            assert a.toString().contains('waitingReceivers=0')
            assert b.toString().contains('waitingReceivers=0')
            a.send('x')
            assert a.bufferedSize == 1

            // a select that has already taken a value refuses to be cancelled
            def done = ChannelSelect.from(b, a).select()
            assert !done.cancel()
            def result = await done
            assert result.index == 1 && result.value == 'x'
        '''
    }

    @Test
    void testSelectOverCustomChannelImplementation() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            // not a DefaultAsyncChannel, so it cannot take part in the claim protocol
            class Wrapped<T> implements AsyncChannel<T> {
                @Delegate AsyncChannel<T> inner
            }
            def custom = new Wrapped(inner: AsyncChannel.create(4))
            def builtin = AsyncChannel.create(4)

            // the custom channel wins: the built-in sibling is left untouched
            async { Thread.sleep(50); custom.send('custom') }
            def result = await ChannelSelect.from(custom, builtin).select()
            assert result.index == 0 && result.value == 'custom'
            assert builtin.toString().contains('waitingReceivers=0')

            // the built-in channel wins: the custom branch is withdrawn
            builtin.send('b1')
            result = await ChannelSelect.from(custom, builtin).select()
            assert result.index == 1 && result.value == 'b1'
            custom.send('c1')
            assert custom.bufferedSize == 1
            def next = await custom.receive()
            assert next == 'c1'
        '''
    }

    @Test
    void testFairSelectRotatesAmongReadyChannels() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            def c = AsyncChannel.create(20)
            for (i in 0..<20) { a.send('a' + i); b.send('b' + i); c.send('c' + i) }

            def sel = ChannelSelect.from(a, b, c).fair()
            def order = []
            for (i in 0..<9) {
                order << (await sel.select()).index
            }
            assert order == [0, 1, 2, 0, 1, 2, 0, 1, 2]

            // each channel gave up its values in order
            assert [await(a.receive()), await(b.receive()), await(c.receive())] == ['a3', 'b3', 'c3']

            // a continuously ready channel cannot starve a ready peer
            def busy = AsyncChannel.create(1000)
            def peer = AsyncChannel.create(1)
            for (i in 0..<1000) busy.send(i)
            peer.send('me too')
            def fairSel = ChannelSelect.from(busy, peer).fair()
            def taken = (0..<2).collect { (await fairSel.select()).index }
            assert taken.contains(1)

            // the default policy is priority by list order
            def prio = ChannelSelect.from(a, b, c)
            assert (0..<5).collect { (await prio.select()).index } == [0, 0, 0, 0, 0]
        '''
    }

    @Test
    void testRandomSelectSpreadsAmongReadyChannels() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            int rounds = 300
            def a = AsyncChannel.create(rounds)
            def b = AsyncChannel.create(rounds)
            def c = AsyncChannel.create(rounds)
            for (i in 0..<rounds) { a.send('a' + i); b.send('b' + i); c.send('c' + i) }

            def sel = ChannelSelect.from(a, b, c).random()
            def wins = [0, 0, 0]
            def taken = [[], [], []]
            for (i in 0..<rounds) {
                def result = await sel.select()
                wins[result.index]++
                taken[result.index] << result.value
            }
            // every channel is chosen (the chance of one being skipped in 300 draws is (2/3)^300)
            assert wins.every { it > 0 }
            // and each channel gave up its values in order
            taken.eachWithIndex { values, index ->
                assert values == (0..<values.size()).collect { 'abc'[index] + it }
            }
        '''

        // uniform among the ready channels, not weighted by the not-ready gaps between them
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            int rounds = 600
            def a = AsyncChannel.create(rounds)
            def b = AsyncChannel.create(rounds)
            def empty = AsyncChannel.create(1)
            for (i in 0..<rounds) { a.send(i); b.send(i) }

            // a random start with a cyclic scan would give a two thirds, b one third
            def sel = ChannelSelect.from(a, b, empty).random()
            def wins = [0, 0, 0]
            for (i in 0..<rounds) {
                wins[(await sel.select()).index]++
            }
            // expected 300 each with a standard deviation of about 12; 230 is 6 sigma below
            assert wins[2] == 0 && wins[0] >= 230 && wins[1] >= 230 : wins
        '''
    }

    @Test
    void testFairSelectStillTakesTheFirstArrival() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)
            def sel = ChannelSelect.from(a, b).fair()

            async { Thread.sleep(50); b.send('b-first') }
            def result = await sel.select()
            assert result.index == 1 && result.value == 'b-first'
            assert a.toString().contains('waitingReceivers=0')

            async { Thread.sleep(50); a.send('a-next') }
            result = await sel.select()
            assert result.index == 0 && result.value == 'a-next'
        '''
    }

    @Test
    void testConcurrentSelectsDeliverEveryValueExactlyOnce() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import groovy.concurrent.ChannelSelect
            import java.util.concurrent.ConcurrentHashMap
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            def a = AsyncChannel.create(8)
            def b = AsyncChannel.create(8)
            int perChannel = 2000
            def seen = new ConcurrentHashMap<Integer, Boolean>()
            def duplicates = new java.util.concurrent.atomic.AtomicInteger()

            def producers = [
                Thread.start { for (i in 0..<perChannel) awaitFor(a.send(i)); a.close() },
                Thread.start { for (i in 0..<perChannel) awaitFor(b.send(perChannel + i)); b.close() }
            ]
            // consumers list the channels in opposite orders to exercise cross-channel withdrawal
            def consumers = (0..<4).collect { n ->
                Thread.start {
                    def sel = n % 2 == 0 ? ChannelSelect.from(a, b) : ChannelSelect.from(b, a)
                    while (true) {
                        try {
                            def result = awaitFor(sel.select())
                            if (seen.putIfAbsent((int) result.value, true) != null) duplicates.incrementAndGet()
                        } catch (ChannelClosedException e) {
                            break
                        }
                    }
                }
            }
            (producers + consumers).each { it.join(20_000) }
            assert (producers + consumers).every { !it.alive }

            assert duplicates.get() == 0
            assert seen.size() == 2 * perChannel
            assert a.toString().contains('waitingReceivers=0')
            assert b.toString().contains('waitingReceivers=0')
        '''
    }
}
