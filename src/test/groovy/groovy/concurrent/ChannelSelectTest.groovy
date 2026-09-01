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
}
