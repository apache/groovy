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

    // GROOVY-12323: mixed offers — a select may offer to send as well as to receive

    @Test
    void testSendOfferCommitsWhenAReceiverIsWaiting() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def out = AsyncChannel.create()        // rendezvous
            def other = AsyncChannel.create(4)

            def taken = out.receive()              // a receiver is already waiting
            def result = await offers(send(out, 'opener'), receive(other)).select()
            assert result.index == 0 && result.send && result.value == 'opener'
            assert (await taken) == 'opener'
            assert other.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testSendOfferCommitsIntoFreeBufferSpace() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def out = AsyncChannel.create(4)
            def other = AsyncChannel.create(4)

            // documented caveat: with buffer space the send commits unilaterally,
            // so cross-select mixed-choice coherence needs capacity-0 channels
            def result = await offers(send(out, 'v'), receive(other)).select()
            assert result.index == 0 && result.send && result.value == 'v'
            assert out.bufferedSize == 1
            assert (await out.receive()) == 'v'
            assert other.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testRetiredSendOfferLeavesNoResidue() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def quiet = AsyncChannel.create()      // rendezvous: the send cannot complete alone
            def ready = AsyncChannel.create(4)
            ready.send('r1'); ready.send('r2')

            def result = await offers(send(quiet, 'never'), receive(ready)).select()
            assert result.index == 1 && !result.send && result.value == 'r1'
            assert ready.bufferedSize == 1

            // the losing offer left nothing behind: no waiting sender, no value
            assert quiet.toString().contains('waitingSenders=0')
            def probe = quiet.receive()
            assert !probe.toCompletableFuture().isDone()
            probe.cancel()
        '''
    }

    @Test
    void testTimedOutMixedSelectSendsNothing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.TimeoutException
            import static groovy.concurrent.ChannelSelect.*
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create()          // rendezvous
            def b = AsyncChannel.create()

            shouldFail(TimeoutException) {
                await offers(send(a, 'x'), receive(b)).select().orTimeoutMillis(50)
            }
            assert a.toString().contains('waitingSenders=0')
            assert b.toString().contains('waitingReceivers=0')

            // the send never happened
            def probe = a.receive()
            assert !probe.toCompletableFuture().isDone()
            probe.cancel()
        '''
    }

    @Test
    void testMixedSelectFailsWhenAllChannelsAreClosed() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import static groovy.concurrent.ChannelSelect.*
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create(1)
            def b = AsyncChannel.create(1)
            a.close(); b.close()
            shouldFail(ChannelClosedException) {
                await offers(send(a, 'x'), receive(b)).select().orTimeoutMillis(1000)
            }

            // closed later, while the offers are parked
            def c = AsyncChannel.create()          // rendezvous so the send offer parks
            def d = AsyncChannel.create()
            async { Thread.sleep(50); c.close(); d.close() }
            shouldFail(ChannelClosedException) {
                await offers(send(c, 'x'), receive(d)).select().orTimeoutMillis(1000)
            }
        '''
    }

    @Test
    void testMixedSelectStillWaitsWhileOneChannelIsOpen() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def closed = AsyncChannel.create()
            closed.close()
            def open = AsyncChannel.create(4)

            async { Thread.sleep(50); open.send('still here') }
            def result = await offers(send(closed, 'x'), receive(open)).select()
            assert result.index == 1 && !result.send && result.value == 'still here'
        '''
    }

    @Test
    void testSendOfferRequiresBuiltInChannel() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect
            import static groovy.test.GroovyAssert.shouldFail

            class Wrapped<T> implements AsyncChannel<T> {
                @Delegate AsyncChannel<T> inner
            }
            shouldFail(IllegalArgumentException) {
                ChannelSelect.send(new Wrapped(inner: AsyncChannel.create(4)), 'x')
            }
        '''
    }

    @Test
    void testMixedChoicePeersCommitExactlyOneCoherentBranch() {
        // the arbitrated mixed choice: either peer may open; over rendezvous
        // channels exactly one opener commits and both peers agree on it
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            int trials = 200
            int leftOpens = 0, rightOpens = 0
            for (t in 0..<trials) {
                def ping = AsyncChannel.create()   // left -> right, rendezvous
                def pong = AsyncChannel.create()   // right -> left, rendezvous
                def results = new Object[2]
                def left = Thread.start {
                    results[0] = awaitFor(offers(send(ping, 1), receive(pong)).select())
                }
                def right = Thread.start {
                    results[1] = awaitFor(offers(send(pong, 2), receive(ping)).select())
                }
                left.join(10_000); right.join(10_000)
                assert !left.alive && !right.alive

                def l = results[0], r = results[1]
                if (l.index == 0) {                // left opened: ping transferred
                    assert l.send && !r.send && r.index == 1 && r.value == 1
                    leftOpens++
                } else {                           // right opened: pong transferred
                    assert !l.send && l.value == 2 && r.send && r.index == 0
                    rightOpens++
                }
                for (ch in [ping, pong]) {
                    def state = ch.toString()
                    assert state.contains('waitingSenders=0') && state.contains('waitingReceivers=0')
                    assert ch.bufferedSize == 0
                }
            }
            assert leftOpens + rightOpens == trials
        '''
    }

    @Test
    void testTimeoutRacingADeliveryNeverLosesATransfer() {
        // orTimeout withdraws the select through Winner.cancel, which competes
        // for the claim: a delivery that already committed refuses the cancel
        // and the result reaches the caller, while a timeout that wins the
        // claim guarantees nothing was sent — never both, never neither
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.TimeoutException
            import java.util.concurrent.ThreadLocalRandom
            import static groovy.concurrent.ChannelSelect.*
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            int trials = 50
            int delivered = 0, timedOut = 0
            for (t in 0..<trials) {
                def rz = AsyncChannel.create()     // rendezvous: the send offer parks
                def quiet = AsyncChannel.create()
                def peerProbe = null
                def peer = Thread.start {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10))
                    peerProbe = rz.receive()       // may arrive before or after the timeout
                }
                def outcome
                try {
                    outcome = awaitFor(offers(send(rz, 42), receive(quiet)).select().orTimeoutMillis(5))
                } catch (TimeoutException e) {
                    outcome = e
                }
                peer.join(10_000)
                assert !peer.alive

                if (outcome instanceof TimeoutException) {
                    timedOut++
                    // the timeout won the claim: the send must never happen,
                    // so the peer's receive must still be waiting
                    Thread.sleep(20)
                    assert !peerProbe.toCompletableFuture().isDone()
                    peerProbe.cancel()
                } else {
                    delivered++
                    // the delivery won the claim: the caller sees the committed
                    // send and the peer holds the transferred value
                    assert outcome.index == 0 && outcome.send && outcome.value == 42
                    assert awaitFor(peerProbe) == 42
                }
                for (ch in [rz, quiet]) {
                    def state = ch.toString()
                    assert state.contains('waitingSenders=0')
                }
            }
            assert delivered + timedOut == trials
        '''
    }

    @Test
    void testContendedMixedOffersOnOneChannelPairExactly() {
        // 2N selects all offering both to send into and to receive from ONE
        // rendezvous channel: every commit pairs one select's send with
        // another's receive (never its own), so exactly N transfers happen
        // and every select commits exactly one branch
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.ConcurrentLinkedQueue
            import static groovy.concurrent.ChannelSelect.*
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            int pairs = 50
            def ch = AsyncChannel.create()          // one rendezvous channel for everyone
            def sent = new ConcurrentLinkedQueue<Integer>()
            def received = new ConcurrentLinkedQueue<Integer>()
            def threads = (0..<2 * pairs).collect { n ->
                Thread.start {
                    def result = awaitFor(offers(send(ch, n), receive(ch)).select())
                    if (result.send) sent << n else received << (int) result.value
                }
            }
            threads.each { it.join(20_000) }
            assert threads.every { !it.alive }

            assert sent.size() == pairs && received.size() == pairs
            assert sent.toList().sort() == received.toList().sort()
            def state = ch.toString()
            assert state.contains('waitingSenders=0') && state.contains('waitingReceivers=0')
            assert ch.bufferedSize == 0
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

    @Test
    void testDisabledOfferIsNotRegisteredAndIndicesAreUnchanged() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def first = AsyncChannel.create(4)
            def second = AsyncChannel.create(4)
            first.send('f1'); second.send('s1')

            // both ready, but the first branch is masked off: the second wins
            // and still calls itself index 1
            def sel = offers(receive(first), receive(second))
            def result = await sel.select(false, true)
            assert result.index == 1 && result.value == 's1'

            // the disabled branch was never registered: nothing taken, no receiver left
            assert first.bufferedSize == 1
            assert first.toString().contains('waitingReceivers=0')

            // unmasking it restores the priority order
            assert (await sel.select(true, true)).index == 0
        '''
    }

    @Test
    void testDisabledSendOfferTransfersNothing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def out = AsyncChannel.create(4)      // room to spare: the send would commit at once
            def input = AsyncChannel.create(4)
            input.send('i1')

            def result = await offers(send(out, 'v'), receive(input)).select(false, true)
            assert result.index == 1 && !result.send && result.value == 'i1'
            assert out.bufferedSize == 0
            assert out.toString().contains('waitingSenders=0')
        '''
    }

    @Test
    void testSelectWithEveryOfferDisabledFails() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)
            a.send('ready')

            def sel = ChannelSelect.from(a, b)
            shouldFail(IllegalStateException) {
                await sel.select(false, false)
            }
            // the ready channel was left alone
            assert a.bufferedSize == 1
            assert a.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testSelectRejectsAPreconditionCountThatIsNotTheOfferCount() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect
            import static groovy.test.GroovyAssert.shouldFail

            def sel = ChannelSelect.from(AsyncChannel.create(4), AsyncChannel.create(4))
            shouldFail(IllegalArgumentException) { sel.select(true) }
            shouldFail(IllegalArgumentException) { sel.select(true, true, true) }
        '''
    }

    @Test
    void testFairSelectRotatesAmongTheEnabledOffersOnly() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            def c = AsyncChannel.create(20)
            for (i in 0..<10) { a.send('a'); b.send('b'); c.send('c') }

            // b stays masked off, so the rotation alternates between a and c
            def sel = ChannelSelect.from(a, b, c).fair()
            def indices = (0..<6).collect { (await sel.select(true, false, true)).index }
            assert indices.toSet() == [0, 2] as Set
            assert indices.count { it == 0 } == 3 && indices.count { it == 2 } == 3
            assert b.bufferedSize == 10
        '''
    }

    @Test
    void testSelectWaitsOnTheEnabledOffersWhileADisabledOneIsReady() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def masked = AsyncChannel.create(4)
            def live = AsyncChannel.create(4)
            masked.send('ignored')

            async { Thread.sleep(50); live.send('late') }
            def result = await ChannelSelect.from(masked, live).select(false, true).orTimeoutMillis(5000)
            assert result.index == 1 && result.value == 'late'
            assert masked.bufferedSize == 1
        '''
    }

    @Test
    void testSelectFailsOnlyWhenEveryEnabledChannelIsClosed() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import groovy.concurrent.ChannelSelect
            import static groovy.test.GroovyAssert.shouldFail

            def open = AsyncChannel.create(4)
            def closed = AsyncChannel.create(4)
            closed.close()

            // the open channel is masked off, so the closed one decides the result
            shouldFail(ChannelClosedException) {
                await ChannelSelect.from(open, closed).select(false, true).orTimeoutMillis(1000)
            }
        '''
    }

    @Test
    void testResultNamesTheWinningChannel() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def input = AsyncChannel.create(4)
            def output = AsyncChannel.create(4)
            input.send('i1')

            def received = await offers(receive(input), send(output, 'o1')).select()
            assert received.channel.is(input) && received.value == 'i1'

            def sent = await offers(receive(input), send(output, 'o1')).select(false, true)
            assert sent.channel.is(output) && sent.send
            assert (await output.receive()) == 'o1'
        '''
    }

    @Test
    void testGuardedBoundedBuffer() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import static groovy.concurrent.ChannelSelect.*
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            // Kerridge's bounded buffer: accept input only while there is room,
            // answer a request only while the buffer holds something
            int capacity = 3
            def input = AsyncChannel.create()       // rendezvous, so a producer past capacity really blocks
            def request = AsyncChannel.create()
            def reply = AsyncChannel.create()

            def buffer = Thread.start {
                def held = new LinkedList()
                def sel = offers(receive(input), receive(request))
                while (true) {
                    try {
                        def result = awaitFor(sel.select(held.size() < capacity, !held.isEmpty()))
                        if (result.index == 0) held.addLast(result.value)
                        else awaitFor(reply.send(held.removeFirst()))
                    } catch (ChannelClosedException e) {
                        break
                    }
                }
            }

            // a fourth value would block while the buffer is full, so interleave
            for (i in 1..3) awaitFor(input.send(i))
            awaitFor(request.send('next'))
            assert awaitFor(reply.receive()) == 1
            awaitFor(input.send(4))

            for (expected in 2..4) {
                awaitFor(request.send('next'))
                assert awaitFor(reply.receive()) == expected
            }

            input.close(); request.close()
            buffer.join(10_000)
            assert !buffer.alive
        '''
    }

    @Test
    void testGuardedOfferIsNotRegisteredAndIndicesAreUnchanged() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def first = AsyncChannel.create(4)
            def second = AsyncChannel.create(4)
            first.send('f1'); second.send('s1')

            // both ready, but the first branch is guarded off: the second wins
            // and still calls itself index 1
            def result = await offers(receive(first).when { false }, receive(second)).select()
            assert result.index == 1 && result.value == 's1'

            // the guarded branch was never registered: nothing taken, no receiver left
            assert first.bufferedSize == 1
            assert first.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testGuardIsConsultedOnEverySelect() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            for (i in 0..<4) { a.send('a' + i); b.send('b' + i) }

            // one held select whose guard tracks changing state
            boolean takeA = false
            def sel = offers(receive(a).when { takeA }, receive(b))

            assert (await sel.select()).value == 'b0'
            takeA = true
            assert (await sel.select()).value == 'a0'
            takeA = false
            assert (await sel.select()).value == 'b1'
            takeA = true
            assert (await sel.select()).value == 'a1'
        '''
    }

    @Test
    void testGuardedSendOfferTransfersNothing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def out = AsyncChannel.create(4)      // room to spare: the send would commit at once
            def input = AsyncChannel.create(4)
            input.send('i1')

            def result = await offers(send(out, 'v').when { false }, receive(input)).select()
            assert result.index == 1 && !result.send && result.value == 'i1'
            assert out.bufferedSize == 0
            assert out.toString().contains('waitingSenders=0')
        '''
    }

    @Test
    void testGuardsConjoinWithEachOtherAndWithThePreconditionMask() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            for (i in 0..<6) { a.send('a' + i); b.send('b' + i) }

            // chained guards must both hold
            boolean one = true, two = true
            def sel = offers(receive(a).when { one }.when { two }, receive(b))
            assert (await sel.select()).index == 0
            two = false
            assert (await sel.select()).index == 1
            two = true; one = false
            assert (await sel.select()).index == 1

            // an enabled guard is still overridden by a false mask flag
            one = true
            assert (await sel.select(false, true)).index == 1
            assert (await sel.select(true, true)).index == 0
        '''
    }

    @Test
    void testSelectWithEveryOfferGuardedOffFails() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*
            import static groovy.test.GroovyAssert.shouldFail

            def a = AsyncChannel.create(4)
            def b = AsyncChannel.create(4)
            a.send('ready')

            def sel = offers(receive(a).when { false }, receive(b).when { false })
            shouldFail(IllegalStateException) { await sel.select() }
            shouldFail(IllegalStateException) { await sel.select(true, true) }

            // the ready channel was left alone
            assert a.bufferedSize == 1
            assert a.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testGuardedOfferIsReusableAndLeavesTheOriginalUnguarded() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            for (i in 0..<2) { a.send('a' + i); b.send('b' + i) }

            def plain = receive(a)
            def guarded = plain.when { false }        // when() copies, it does not mutate

            assert (await offers(guarded, receive(b)).select()).index == 1
            assert (await offers(plain, receive(b)).select()).index == 0
        '''
    }

    @Test
    void testFairSelectRotatesAmongTheUnguardedOffersOnly() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def a = AsyncChannel.create(20)
            def b = AsyncChannel.create(20)
            def c = AsyncChannel.create(20)
            for (i in 0..<10) { a.send('a'); b.send('b'); c.send('c') }

            // b stays guarded off, so the rotation alternates between a and c
            def sel = offers(receive(a), receive(b).when { false }, receive(c)).fair()
            def indices = (0..<6).collect { (await sel.select()).index }
            assert indices.toSet() == [0, 2] as Set
            assert indices.count { it == 0 } == 3 && indices.count { it == 2 } == 3
            assert b.bufferedSize == 10
        '''
    }

    @Test
    void testGuardedBoundedBufferWithWhen() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import static groovy.concurrent.ChannelSelect.*
            import static org.apache.groovy.runtime.async.AsyncSupport.await as awaitFor

            // the same bounded buffer, with the guards written onto the branches
            int capacity = 3
            def input = AsyncChannel.create()       // rendezvous
            def request = AsyncChannel.create()
            def reply = AsyncChannel.create()
            def held = new LinkedList()

            def buffer = Thread.start {
                def sel = offers(receive(input).when { held.size() < capacity },
                                 receive(request).when { !held.isEmpty() })
                while (true) {
                    try {
                        def result = awaitFor(sel.select())
                        if (result.index == 0) held.addLast(result.value)
                        else awaitFor(reply.send(held.removeFirst()))
                    } catch (ChannelClosedException e) {
                        break
                    }
                }
            }

            for (i in 1..3) awaitFor(input.send(i))
            awaitFor(request.send('next'))
            assert awaitFor(reply.receive()) == 1
            awaitFor(input.send(4))

            for (expected in 2..4) {
                awaitFor(request.send('next'))
                assert awaitFor(reply.receive()) == expected
            }

            input.close(); request.close()
            buffer.join(10_000)
            assert !buffer.alive
        '''
    }

    // GROOVY-12343: a deadline as a branch of the select

    @Test
    void testTimerBranchWinsAQuietSelect() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect
            import java.time.Instant

            def work = AsyncChannel.create(4)
            def before = Instant.now()
            def result = await ChannelSelect.from(work, AsyncChannel.after(50)).select()

            assert result.index == 1
            assert result.value instanceof Instant
            assert !result.value.isBefore(before)
            assert work.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testDataBeatsTheTimerAndTheTimerStillFires() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException
            import groovy.concurrent.ChannelSelect
            import java.time.Instant

            def work = AsyncChannel.create(4)
            def timer = AsyncChannel.after(100)
            async { Thread.sleep(20); work.send('done') }

            def result = await ChannelSelect.from(work, timer).select()
            assert result.index == 0 && result.value == 'done'

            // the losing timer was withdrawn, not consumed: it fires all the
            // same, holds its instant until received, and then closes
            assert !timer.closed
            def fired = await timer.receive()
            assert fired instanceof Instant
            assert timer.closed
            try {
                await timer.receive()
                assert false
            } catch (ChannelClosedException expected) {
            }
        '''
    }

    @Test
    void testPerBranchDeadlines() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def fast = AsyncChannel.create()
            def slow = AsyncChannel.create()
            def sel = offers(receive(fast), receive(AsyncChannel.after(30)),
                             receive(slow), receive(AsyncChannel.after(2000)))

            def result = await sel.select()
            assert result.index == 1
        '''
    }

    @Test
    void testTimerCreatedOnceIsAFixedDeadlineAcrossSelects() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def work = AsyncChannel.create(16)
            def deadline = AsyncChannel.after(200)
            def sel = ChannelSelect.from(work, deadline)

            async {
                for (i in 0..<3) { work.send(i); Thread.sleep(10) }
            }

            def received = []
            while (true) {
                def result = await sel.select()
                if (result.index == 1) break
                received << result.value
            }
            assert received == [0, 1, 2]
            assert deadline.closed
        '''
    }

    @Test
    void testGuardedTimerIsNotRegisteredWhileItsGuardIsOff() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(4)
            // already elapsed: holds its instant with no scheduler hop, so the
            // assertion that the guarded-off branch left it alone does not race
            // the single scheduler thread
            def timer = AsyncChannel.after(0)
            boolean armed = false
            def sel = offers(receive(work), receive(timer).when { armed })
            assert timer.bufferedSize == 1 && timer.closed

            // work is not ready yet: a registered timer would commit at once
            def firstPending = sel.select()
            assert !firstPending.toCompletableFuture().isDone()
            assert timer.bufferedSize == 1

            work.send('x')
            def first = await firstPending
            assert first.index == 0 && first.value == 'x'
            assert timer.bufferedSize == 1

            armed = true
            def second = await sel.select()
            assert second.index == 1
            assert timer.bufferedSize == 0
        '''
    }

    @Test
    void testFairSelectServesAFiredTimerLikeAnyReadyBranch() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def busy = AsyncChannel.create(8)
            for (i in 0..<8) busy.send(i)
            def timer = AsyncChannel.after(0)
            Thread.sleep(30)

            // busy never runs dry, yet the fired timer is served within n calls
            def sel = ChannelSelect.from(busy, timer).fair()
            def indices = (0..<4).collect { (await sel.select()).index }
            assert indices.count { it == 1 } == 1
        '''
    }

    @Test
    void testClosingATimerBeforeItFiresCancelsIt() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelClosedException

            def timer = AsyncChannel.after(50)
            assert timer.close()
            assert !timer.close()
            Thread.sleep(100)
            assert timer.bufferedSize == 0
            try {
                await timer.receive()
                assert false
            } catch (ChannelClosedException expected) {
            }
        '''
    }

    @Test
    void testDurationAndNegativeDelays() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.time.Duration
            import java.time.Instant

            def before = Instant.now()
            def byDuration = await AsyncChannel.after(Duration.ofMillis(20)).receive()
            assert byDuration instanceof Instant && !byDuration.isBefore(before)

            // a deadline that has already passed is created already fired
            def overdue = AsyncChannel.after(-1000)
            assert overdue.bufferedSize == 1 && overdue.closed
            assert (await overdue.receive()) instanceof Instant
            def zero = AsyncChannel.after(Duration.ZERO)
            assert zero.bufferedSize == 1 && zero.closed
            assert (await zero.receive()) instanceof Instant

            try {
                AsyncChannel.after((Duration) null)
                assert false
            } catch (NullPointerException expected) {
            }
        '''
    }

    // GROOVY-12343: the timer offer re-arms on every select call

    @Test
    void testTimerOfferWinsAQuietSelect() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.time.Instant
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(4)
            def before = Instant.now()
            def result = await offers(receive(work), after(50)).select()

            assert result.index == 1
            assert result.timeout && !result.send
            assert result.channel == null
            assert result.value instanceof Instant
            assert !result.value.isBefore(before)
            assert result.toString().startsWith('SelectResult[timeout=1, at=')
            assert work.toString().contains('waitingReceivers=0')
        '''
    }

    @Test
    void testTimerOfferReArmsOnEveryRound() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.SynchronousQueue
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create()
            def sel = offers(receive(work), after(300))
            def rounds = new SynchronousQueue()

            // Signal the sender only after select has armed its branches. Each
            // value is well within its own deadline but all four exceed a
            // single fixed deadline started before the first round.
            async {
                for (i in 0..<4) {
                    rounds.take()
                    Thread.sleep(100)
                    work.send(i)
                }
            }
            def received = (0..<4).collect {
                def pending = sel.select()
                rounds.put(it)
                def result = await pending
                assert !result.timeout && result.channel.is(work)
                result.value
            }
            assert received == [0, 1, 2, 3]

            // and a round in which nothing arrives times out
            def result = await sel.select()
            assert result.timeout && result.index == 1
        '''
    }

    @Test
    void testElapsedTimerOfferIsReadyAtRegistrationAndThePolicyDecides() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(4)
            work.send('ready')

            // listed last, after(0) is Go's default clause: a ready channel wins...
            def result = await offers(receive(work), after(0)).select()
            assert result.index == 0 && result.value == 'ready'

            // ...and with nothing ready the select completes without waiting
            def polled = offers(receive(work), after(0)).select()
            assert polled.toCompletableFuture().isDone()
            assert (await polled).timeout
            assert work.toString().contains('waitingReceivers=0')

            // listed first, the elapsed timer is the ready offer with priority
            work.send('ready')
            result = await offers(after(0), receive(work)).select()
            assert result.timeout && result.index == 0
            assert work.bufferedSize == 1
        '''
    }

    @Test
    void testFairSelectRotatesOverAnElapsedTimerOffer() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(8)
            for (i in 0..<8) work.send(i)

            def sel = offers(receive(work), after(0)).fair()
            def indices = (0..<4).collect { (await sel.select()).index }
            assert indices == [0, 1, 0, 1]
            assert work.bufferedSize == 6
        '''
    }

    @Test
    void testFairRotationSurvivesATimedOutRound() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def a = AsyncChannel.create(4)
            def c = AsyncChannel.create(4)
            def sel = offers(receive(a), after(20), receive(c)).fair()

            a.send('a1'); c.send('c1')
            assert (await sel.select()).index == 0      // starts at a
            assert (await sel.select()).index == 2      // rotation: c, the timer is not ready
            assert (await sel.select()).timeout          // nothing ready: the timer fires

            // the rotation continues from the timer's branch, so c is served
            // before a even though a is listed first
            a.send('a2'); c.send('c2')
            assert (await sel.select()).index == 2
            assert (await sel.select()).index == 0
        '''
    }

    @Test
    void testGuardedTimerOfferArmsNothing() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.TimeoutException
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(4)
            boolean armed = false
            def sel = offers(receive(work), after(10).when { armed })

            // with the timer guarded off the select waits on work alone
            try {
                await sel.select().orTimeoutMillis(100)
                assert false
            } catch (TimeoutException expected) {
            }
            assert work.toString().contains('waitingReceivers=0')

            armed = true
            def result = await sel.select()
            assert result.timeout && result.index == 1
        '''
    }

    @Test
    void testPerBranchTimerOffersAndDurations() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import java.time.Duration
            import static groovy.concurrent.ChannelSelect.*

            def fast = AsyncChannel.create()
            def slow = AsyncChannel.create()
            def sel = offers(receive(fast), after(Duration.ofMillis(30)),
                             receive(slow), after(Duration.ofSeconds(2)))

            def result = await sel.select()
            assert result.timeout && result.index == 1

            try {
                after((Duration) null)
                assert false
            } catch (NullPointerException expected) {
            }
        '''
    }

    @Test
    void testTimerOfferWithinPositionalPreconditions() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import static groovy.concurrent.ChannelSelect.*

            def work = AsyncChannel.create(4)
            def sel = offers(receive(work), after(10))
            work.send('x')

            // the timer masked off: the value is taken
            assert (await sel.select(true, false)).value == 'x'
            // work masked off: only the timer can commit
            assert (await sel.select(false, true)).timeout
        '''
    }
}
