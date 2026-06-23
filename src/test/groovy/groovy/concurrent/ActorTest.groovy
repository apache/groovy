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
import static groovy.test.GroovyAssert.shouldFail

final class ActorTest {

    // === Reactor ===

    @Test
    void testReactorProcessesMessage() {
        assertScript '''
            import groovy.concurrent.Actor

            def doubler = Actor.reactor { it * 2 }
            assert await(doubler.sendAndGet(5)) == 10
            assert await(doubler.sendAndGet(21)) == 42
            doubler.stop()
        '''
    }

    @Test
    void testReactorFireAndForget() {
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()
            def logger = Actor.reactor { msg -> log << msg; null }

            logger.send('hello')
            logger.send('world')
            // Give time to process
            Thread.sleep(100)
            assert 'hello' in log
            assert 'world' in log
            logger.stop()
        '''
    }

    @Test
    void testReactorHandlerException() {
        assertScript '''
            import groovy.concurrent.Actor

            def failing = Actor.reactor { throw new RuntimeException('boom') }
            try {
                await(failing.sendAndGet('anything'))
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'boom'
            }
            failing.stop()
        '''
    }

    // === Stateful ===

    @Test
    void testStatefulCounter() {
        assertScript '''
            import groovy.concurrent.Actor

            def counter = Actor.stateful(0) { state, msg ->
                switch (msg) {
                    case 'increment': return state + 1
                    case 'decrement': return state - 1
                    default: return state
                }
            }

            counter.send('increment')
            counter.send('increment')
            counter.send('increment')
            counter.send('decrement')
            assert await(counter.sendAndGet('increment')) == 3
            counter.stop()
        '''
    }

    @Test
    void testStatefulListAccumulator() {
        assertScript '''
            import groovy.concurrent.Actor

            def collector = Actor.stateful([]) { state, msg -> state + [msg] }

            collector.send('a')
            collector.send('b')
            def result = await(collector.sendAndGet('c'))
            assert result == ['a', 'b', 'c']
            collector.stop()
        '''
    }

    @Test
    void testStatefulBankAccount() {
        assertScript '''
            import groovy.concurrent.Actor

            def account = Actor.stateful(100.0) { balance, msg ->
                switch (msg) {
                    case { it instanceof Map && it.deposit }:
                        return balance + msg.deposit
                    case { it instanceof Map && it.withdraw }:
                        if (msg.withdraw > balance) throw new RuntimeException('Insufficient funds')
                        return balance - msg.withdraw
                    default: return balance
                }
            }

            account.send([deposit: 50.0])
            account.send([withdraw: 30.0])
            def balance = await(account.sendAndGet([deposit: 0.0]))
            assert balance == 120.0
            account.stop()
        '''
    }

    // === Lifecycle ===

    @Test
    void testActorIsActive() {
        assertScript '''
            import groovy.concurrent.Actor

            def actor = Actor.reactor { it }
            assert actor.isActive()
            actor.stop()
            Thread.sleep(50)
            assert !actor.isActive()
        '''
    }

    @Test
    void testStoppedActorRejectsSend() {
        shouldFail(IllegalStateException, '''
            import groovy.concurrent.Actor

            def actor = Actor.reactor { it }
            actor.stop()
            Thread.sleep(50)
            actor.send('too late')
        ''')
    }

    @Test
    void testCloseStopsActor() {
        assertScript '''
            import groovy.concurrent.Actor

            def actor = Actor.reactor { it }
            actor.close()  // AutoCloseable
            Thread.sleep(50)
            assert !actor.isActive()
        '''
    }

    // === Concurrent stress ===

    @Test
    void testConcurrentSendsAreSerialized() {
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.CountDownLatch

            def counter = Actor.stateful(0) { state, msg -> state + 1 }
            def n = 1000
            def latch = new CountDownLatch(n)

            n.times {
                Thread.start {
                    counter.send('tick')
                    latch.countDown()
                }
            }
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

            def result = await(counter.sendAndGet('final'))
            assert result == n + 1  // n ticks + 1 final
            counter.stop()
        '''
    }

    // === Reactor with typed messages ===

    @Test
    void testReactorWithTypedDispatch() {
        assertScript '''
            import groovy.concurrent.Actor

            def calc = Actor.reactor { msg ->
                switch (msg) {
                    case { it instanceof List && it.size() == 3 && it[0] == 'add' }:
                        return msg[1] + msg[2]
                    case { it instanceof List && it.size() == 3 && it[0] == 'mul' }:
                        return msg[1] * msg[2]
                    default:
                        return 'unknown'
                }
            }

            assert await(calc.sendAndGet(['add', 3, 4])) == 7
            assert await(calc.sendAndGet(['mul', 3, 4])) == 12
            assert await(calc.sendAndGet('wat')) == 'unknown'
            calc.stop()
        '''
    }

    // === Self-stop from handler (GROOVY-12033) ===

    @Test
    void testStatefulSelfStopViaContext() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext

            def max = 3
            def bot = Actor.stateful(0) { ActorContext ctx, int count, msg ->
                def next = count + 1
                if (next >= max) ctx.self().stop()
                next
            }

            bot.send('one')
            bot.send('two')
            bot.send('three')
            for (int i = 0; i < 20 && bot.isActive(); i++) Thread.sleep(25)
            assert !bot.isActive()
        '''
    }

    @Test
    void testReactorSelfStopViaCurrentSelf() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.atomic.AtomicInteger

            def n = new AtomicInteger()
            def options = ActorOptions.DEFAULTS.withCurrentSelf(true)
            def actor = Actor.reactor({ msg ->
                if (n.incrementAndGet() == 2) Actor.currentSelf().stop()
                msg
            }, options)

            assert await(actor.sendAndGet('first')) == 'first'
            assert await(actor.sendAndGet('second')) == 'second'
            for (int i = 0; i < 20 && actor.isActive(); i++) Thread.sleep(25)
            assert !actor.isActive()
        '''
    }

    @Test
    void testCurrentSelfThrowsWhenNotEnabled() {
        // currentSelf() is opt-in: an actor built with the default options
        // (no withCurrentSelf(true)) does not publish the thread-local,
        // so the call throws even though we are inside a handler.
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.atomic.AtomicReference

            def captured = new AtomicReference<Throwable>()
            def actor = Actor.reactor { msg ->
                try { Actor.currentSelf().stop() }
                catch (Throwable t) { captured.set(t) }
                msg
            }
            assert await(actor.sendAndGet('x')) == 'x'
            assert captured.get() instanceof IllegalStateException
            assert captured.get().message.contains('withCurrentSelf')
            actor.stop()
        '''
    }

    @Test
    void testSelfStopDrainsQueuedMessages() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import java.util.concurrent.CopyOnWriteArrayList
            import java.util.concurrent.CountDownLatch

            def log = new CopyOnWriteArrayList<Integer>()
            // Gate the handler until all three sends are queued, otherwise
            // the self-stop from msg 2 can flip active=false before
            // sendAndGet(3) runs, causing an IllegalStateException.
            def gate = new CountDownLatch(1)
            def actor = Actor.stateful(0) { ActorContext ctx, int seen, Integer msg ->
                assert gate.await(5, java.util.concurrent.TimeUnit.SECONDS), 'gate never released'
                log << msg
                if (msg == 2) ctx.self().stop()
                seen + 1
            }
            def r1 = actor.sendAndGet(1)
            def r2 = actor.sendAndGet(2)
            def r3 = actor.sendAndGet(3)
            gate.countDown()
            def v1 = await(r1)
            def v2 = await(r2)
            def v3 = await(r3)
            for (int i = 0; i < 20 && actor.isActive(); i++) Thread.sleep(25)
            assert log == [1, 2, 3]
            assert !actor.isActive()
        '''
    }

    @Test
    void testStatePreservedAcrossSelfStop() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import java.util.concurrent.CountDownLatch

            def gate = new CountDownLatch(1)
            def actor = Actor.stateful(100) { ActorContext ctx, int state, msg ->
                assert gate.await(5, java.util.concurrent.TimeUnit.SECONDS), 'gate never released'
                if (msg == 'stop') { ctx.self().stop(); return state }
                state + 1
            }
            def r1 = actor.sendAndGet('inc')
            def r2 = actor.sendAndGet('stop')
            def r3 = actor.sendAndGet('inc')      // queued before stop drains
            gate.countDown()
            def v1 = await(r1)
            def v2 = await(r2)
            def v3 = await(r3)
            assert v1 == 101
            // Reply for the stop-trigger message is the handler's return value.
            assert v2 == 101
            // Third message saw preserved state, then incremented.
            assert v3 == 102
        '''
    }

    @Test
    void testCurrentSelfOutsideHandlerThrows() {
        shouldFail(IllegalStateException, '''
            import groovy.concurrent.Actor
            Actor.currentSelf()
        ''')
    }

    // === onError callback (GROOVY-12033) ===

    @Test
    void testOnErrorFiresForFireAndForgetException() {
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.atomic.AtomicReference

            def latch = new CountDownLatch(1)
            def captured = new AtomicReference<List>()
            def actor = Actor.reactor { msg -> throw new RuntimeException("bang: $msg") }
            actor.onError { Throwable t, msg ->
                captured.set([t.message, msg])
                latch.countDown()
            }
            actor.send('payload')
            assert latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
            assert captured.get() == ['bang: payload', 'payload']
            actor.stop()
        '''
    }

    @Test
    void testOnErrorAlsoFiresForSendAndGet() {
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.atomic.AtomicInteger

            def fired = new AtomicInteger()
            def actor = Actor.reactor { throw new RuntimeException('boom') }
            actor.onError { Throwable t, msg -> fired.incrementAndGet() }

            try { await(actor.sendAndGet('x')); assert false } catch (RuntimeException expected) { }
            for (int i = 0; i < 20 && fired.get() == 0; i++) Thread.sleep(25)
            assert fired.get() == 1
            actor.stop()
        '''
    }

    @Test
    void testOnErrorContextCanStopActor() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext

            def actor = Actor.reactor { throw new RuntimeException('die') }
            actor.onError { ActorContext ctx, Throwable t, msg -> ctx.self().stop() }
            actor.send('trigger')
            for (int i = 0; i < 20 && actor.isActive(); i++) Thread.sleep(25)
            assert !actor.isActive()
        '''
    }

    @Test
    void testOnErrorHandlerExceptionIsSwallowed() {
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.atomic.AtomicInteger

            def processed = new AtomicInteger()
            def actor = Actor.reactor { msg ->
                processed.incrementAndGet()
                if (msg == 'fail') throw new RuntimeException('first')
                msg
            }
            actor.onError { Throwable t, msg -> throw new RuntimeException('handler also failed') }

            actor.send('fail')
            // Subsequent messages should still be processed even though the
            // error handler itself threw.
            assert await(actor.sendAndGet('ok')) == 'ok'
            assert processed.get() == 2
            actor.stop()
        '''
    }

    // === Bounded mailbox (GROOVY-12033) ===

    @Test
    void testBoundedMailboxFailOverflowThrows() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.CountDownLatch

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(2, ActorOptions.Overflow.FAIL)
            def actor = Actor.reactor({ msg -> started.countDown(); hold.await(); msg }, options)

            actor.send('first')     // taken by the handler, which blocks on hold
            // 'first' is off the queue once started.countDown() fires.
            assert started.await(5, java.util.concurrent.TimeUnit.SECONDS), 'handler did not start'
            actor.send('a')         // queued (1/2)
            actor.send('b')         // queued (2/2)
            try {
                actor.send('c')     // overflow
                assert false : 'expected IllegalStateException'
            } catch (IllegalStateException e) {
                assert e.message.contains('mailbox full')
            }
            hold.countDown()
            actor.stop()
        '''
    }

    @Test
    void testBoundedMailboxDropNewest() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.CopyOnWriteArrayList

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def seen = new CopyOnWriteArrayList()
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(2, ActorOptions.Overflow.DROP_NEWEST)
            def actor = Actor.reactor({ msg -> started.countDown(); hold.await(); seen << msg; msg }, options)

            actor.send('first')     // currently being processed
            assert started.await(5, java.util.concurrent.TimeUnit.SECONDS), 'handler did not start'
            actor.send('a')         // queued (1/2)
            actor.send('b')         // queued (2/2)
            actor.send('c')         // dropped silently
            actor.send('d')         // dropped silently
            hold.countDown()
            // Wait for the three accepted messages
            for (int i = 0; i < 40 && seen.size() < 3; i++) Thread.sleep(25)
            assert seen.toList() == ['first', 'a', 'b']
            actor.stop()
        '''
    }

    @Test
    void testBoundedMailboxDropNewestReplyBindsError() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.CountDownLatch

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(1, ActorOptions.Overflow.DROP_NEWEST)
            def actor = Actor.reactor({ msg -> started.countDown(); hold.await(); msg }, options)

            actor.send('busy')         // occupies the handler
            assert started.await(5, java.util.concurrent.TimeUnit.SECONDS), 'handler did not start'
            actor.send('queued')        // fills the 1-slot queue
            def dropped = actor.sendAndGet('dropped')  // overflows
            try {
                await(dropped)
                assert false : 'awaiting a dropped sendAndGet should fail'
            } catch (IllegalStateException e) {
                assert e.message.contains('dropped')
            }
            hold.countDown()
            actor.stop()
        '''
    }

    @Test
    void testBoundedMailboxBlockBackpressures() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.atomic.AtomicLong

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(1, ActorOptions.Overflow.BLOCK)
            def actor = Actor.reactor({ msg -> started.countDown(); hold.await(); msg }, options)

            actor.send('first')      // being processed
            assert started.await(5, java.util.concurrent.TimeUnit.SECONDS), 'handler did not start'
            actor.send('queued')     // fills the slot
            // The next send must block until the handler frees a slot.
            def sendStarted = new AtomicLong()
            def sendReturned = new AtomicLong()
            def t = Thread.start {
                sendStarted.set(System.nanoTime())
                actor.send('blocked')
                sendReturned.set(System.nanoTime())
            }
            // Allow time for misbehaviour to surface: if send did not block,
            // sendReturned would be set well within this window.
            Thread.sleep(150)
            assert sendReturned.get() == 0L : 'send should still be blocked'
            hold.countDown()
            t.join(2000)
            assert sendReturned.get() > 0L
            actor.stop()
        '''
    }

    @Test
    void testSendDropNewestDoesNotThrowOnOverflowContract() {
        // Anchors the Actor.send Javadoc contract for DROP_NEWEST:
        // fire-and-forget sends to a full mailbox are silently dropped,
        // not thrown. (The behavior is implicitly exercised by the
        // testBoundedMailboxDropNewest test above; this one asserts the
        // contract directly so any future "make DROP_NEWEST throw"
        // change fails loudly here as well as breaking that test.)
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(1, ActorOptions.Overflow.DROP_NEWEST)
            def actor = Actor.reactor({ msg -> started.countDown(); hold.await(); msg }, options)

            actor.send('first')                              // taken; handler blocks on hold
            assert started.await(5, TimeUnit.SECONDS)
            actor.send('queued')                             // fills the 1-slot queue

            // These must NOT throw per the send() contract. If a future
            // refactor inverts the silent-drop semantic, this test fails
            // immediately with whatever exception send started throwing.
            actor.send('overflow-1')
            actor.send('overflow-2')
            actor.send('overflow-3')

            hold.countDown()
            actor.stop()
        '''
    }

    @Test
    void testIsTerminatedTracksDrainingPhase() {
        // Verifies the accepting → draining → terminated lifecycle:
        // - active=true, terminated=false: accepting new sends
        // - active=false, terminated=false: drain in progress
        // - active=false, terminated=true: worker exited
        assertScript '''
            import groovy.concurrent.Actor
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def started = new CountDownLatch(1)
            def hold = new CountDownLatch(1)
            def actor = Actor.reactor { msg ->
                started.countDown()
                hold.await()
                msg
            }

            assert actor.isActive()
            assert !actor.isTerminated()

            actor.send('busy')
            assert started.await(5, TimeUnit.SECONDS)
            actor.stop()                                     // begin draining

            // Worker is still inside the handler holding the latch;
            // active flips immediately, terminated must NOT yet be true.
            assert !actor.isActive()
            assert !actor.isTerminated() : 'should still be draining'

            hold.countDown()
            for (int i = 0; i < 80 && !actor.isTerminated(); i++) Thread.sleep(25)
            assert actor.isTerminated() : 'worker should have fully exited'
        '''
    }

    @Test
    void testBoundedMailboxBlockRejectsSelfSendDeadlock() {
        // A handler self-sending on a full BLOCK mailbox would deadlock
        // (it is the only consumer). The actor must fail fast instead.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.atomic.AtomicReference

            def captured = new AtomicReference<Throwable>()
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(1, ActorOptions.Overflow.BLOCK)
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'trigger') {
                    // First self-send refills the 1-slot mailbox.
                    ctx.self().send('after')
                    // Second self-send would block waiting for capacity
                    // that the (only) consumer cannot free — must throw.
                    try { ctx.self().send('deadlock') }
                    catch (Throwable t) { captured.set(t) }
                }
                msg
            }, options)
            actor.send('trigger')
            for (int i = 0; i < 40 && captured.get() == null; i++) Thread.sleep(25)
            assert captured.get() instanceof IllegalStateException
            assert captured.get().message.contains('would deadlock')
            actor.stop()
        '''
    }

    // === Per-actor executor (GROOVY-12033) ===

    @Test
    void testPerActorExecutorIsUsed() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorOptions
            import java.util.concurrent.Executors
            import java.util.concurrent.atomic.AtomicReference

            def pool = Executors.newSingleThreadExecutor { r ->
                def t = new Thread(r, 'my-custom-actor-thread')
                t.daemon = true
                t
            }
            try {
                def options = ActorOptions.DEFAULTS.withExecutor(pool)
                def captured = new AtomicReference<String>()
                def actor = Actor.reactor({ msg -> captured.set(Thread.currentThread().name); msg },
                                          options)
                await(actor.sendAndGet('ping'))
                assert captured.get() == 'my-custom-actor-thread'
                actor.stop()
            } finally {
                pool.shutdown()
            }
        '''
    }

    // === ActorOptions validation ===

    @Test
    void testActorOptionsRejectsNegativeCapacity() {
        shouldFail(IllegalArgumentException, '''
            import groovy.concurrent.ActorOptions
            new ActorOptions(-1, ActorOptions.Overflow.BLOCK,
                             0, ActorOptions.StashOverflow.FAIL,
                             null, false)
        ''')
    }

    @Test
    void testActorOptionsWithBoundedMailboxRejectsZero() {
        shouldFail(IllegalArgumentException, '''
            import groovy.concurrent.ActorOptions
            ActorOptions.DEFAULTS.withBoundedMailbox(0, ActorOptions.Overflow.BLOCK)
        ''')
    }

    @Test
    void testActorOptionsRejectsNegativeStashCapacity() {
        shouldFail(IllegalArgumentException, '''
            import groovy.concurrent.ActorOptions
            new ActorOptions(0, ActorOptions.Overflow.BLOCK,
                             -1, ActorOptions.StashOverflow.FAIL,
                             null, false)
        ''')
    }

    @Test
    void testActorOptionsWithStashBoundRejectsZero() {
        shouldFail(IllegalArgumentException, '''
            import groovy.concurrent.ActorOptions
            ActorOptions.DEFAULTS.withStashBound(0, ActorOptions.StashOverflow.FAIL)
        ''')
    }

    // === become(...) — behavior swap ===

    @Test
    void testReactorBecomeSwapsHandler() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler

            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'switch') {
                    ctx.become({ c, m -> "new:$m" } as ReactorHandler)
                    return 'switched'
                }
                "orig:$msg"
            } as ReactorHandler)

            assert await(actor.sendAndGet('a')) == 'orig:a'
            assert await(actor.sendAndGet('switch')) == 'switched'
            assert await(actor.sendAndGet('b')) == 'new:b'
            assert await(actor.sendAndGet('c')) == 'new:c'
            actor.stop()
        '''
    }

    @Test
    void testStatefulBecomePreservesState() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.StatefulHandler

            // Phase A increments by 1, phase B increments by 10. State
            // (the running total) is preserved across the swap.
            StatefulHandler phaseB = { ctx, s, m -> s + 10 } as StatefulHandler
            def actor = Actor.stateful(0, { ActorContext ctx, int s, m ->
                if (m == 'swap') { ctx.become(phaseB); return s }
                s + 1
            } as StatefulHandler)

            assert await(actor.sendAndGet('inc')) == 1
            assert await(actor.sendAndGet('inc')) == 2
            assert await(actor.sendAndGet('swap')) == 2     // state preserved
            assert await(actor.sendAndGet('inc')) == 12     // now phase B
            assert await(actor.sendAndGet('inc')) == 22
            actor.stop()
        '''
    }

    @Test
    void testBecomeFromOnError() {
        // After a handler throws, an onError that calls become(...) makes
        // subsequent messages flow through the recovery handler.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler

            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'boom') throw new RuntimeException('initial-fail')
                "orig:$msg"
            } as ReactorHandler)
            actor.onError { ActorContext ctx, Throwable t, msg ->
                ctx.become({ c, m -> "safe:$m" } as ReactorHandler)
            }

            assert await(actor.sendAndGet('a')) == 'orig:a'
            try { await(actor.sendAndGet('boom')); assert false }
            catch (RuntimeException expected) { assert expected.message == 'initial-fail' }
            assert await(actor.sendAndGet('b')) == 'safe:b'
            assert await(actor.sendAndGet('c')) == 'safe:c'
            actor.stop()
        '''
    }

    @Test
    void testBecomeWithWrongShapeThrows() {
        // A reactor's context refuses become(StatefulHandler) and vice versa.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.atomic.AtomicReference

            // become(StatefulHandler) on a reactor actor hits the
            // default-throw in ActorContext, whose message says it
            // "requires a stateful actor".
            def reactorErr = new AtomicReference<Throwable>()
            def r = Actor.reactor({ ActorContext ctx, msg ->
                try {
                    ctx.become({ c, s, m -> s } as StatefulHandler)
                } catch (Throwable t) { reactorErr.set(t) }
                msg
            } as ReactorHandler)
            await(r.sendAndGet('go'))
            assert reactorErr.get() instanceof UnsupportedOperationException
            assert reactorErr.get().message.contains('stateful actor')
            r.stop()

            // Symmetric case: become(ReactorHandler) on a stateful actor
            // hits the default-throw whose message says "requires a
            // reactor actor".
            def statefulErr = new AtomicReference<Throwable>()
            def s = Actor.stateful(0, { ActorContext ctx, int st, m ->
                try {
                    ctx.become({ c, msg -> msg } as ReactorHandler)
                } catch (Throwable t) { statefulErr.set(t) }
                st
            } as StatefulHandler)
            await(s.sendAndGet('go'))
            assert statefulErr.get() instanceof UnsupportedOperationException
            assert statefulErr.get().message.contains('reactor actor')
            s.stop()
        '''
    }

    @Test
    void testBecomeFromNonWorkerThreadThrows() {
        // Capture the context inside a handler, then call become() from
        // the main thread — must fail because the context is only valid
        // on the worker thread.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.atomic.AtomicReference

            def captured = new AtomicReference<ActorContext>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                captured.set(ctx); msg
            } as ReactorHandler)
            await(actor.sendAndGet('capture'))

            try {
                captured.get().become({ c, m -> m } as ReactorHandler)
                assert false : 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message.contains('worker thread')
            }
            actor.stop()
        '''
    }

    @Test
    void testBecomeWithNullThrows() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.atomic.AtomicReference

            def caught = new AtomicReference<Throwable>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                try { ctx.become((ReactorHandler) null) }
                catch (Throwable t) { caught.set(t) }
                msg
            } as ReactorHandler)
            await(actor.sendAndGet('x'))
            assert caught.get() instanceof NullPointerException
            actor.stop()
        '''
    }

    // === stash() / unstashAll() ===

    @Test
    void testStashAndUnstashAllPreservesFifo() {
        // Messages stashed during a 'collect' phase replay in original
        // order when unstashAll runs in the 'release' phase.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.CopyOnWriteArrayList
            import java.util.concurrent.CountDownLatch

            def log = new CopyOnWriteArrayList()
            def gate = new CountDownLatch(1)
            StatefulHandler releasing = { ctx, s, m -> log << m; s } as StatefulHandler
            def actor = Actor.stateful('init', { ActorContext ctx, s, m ->
                if (m == 'wait') { gate.await(); return s }
                if (m == 'go') {
                    ctx.become(releasing)
                    ctx.unstashAll()
                    return s
                }
                ctx.stash()
                s
            } as StatefulHandler)

            actor.send('wait')              // worker parks on gate
            actor.send('m1')                // queued behind wait → will be stashed
            actor.send('m2')                // queued, will be stashed
            actor.send('go')                // queued, triggers unstash
            actor.send('m3')                // queued AFTER go — but in FIFO order
                                            // relative to m1/m2 after unstashAll
            gate.countDown()

            for (int i = 0; i < 80 && log.size() < 3; i++) Thread.sleep(25)
            assert log.toList() == ['m1', 'm2', 'm3']
            actor.stop()
        '''
    }

    @Test
    void testStashedSendAndGetReplyBindsAfterUnstash() {
        // A sendAndGet whose message gets stashed must not complete until
        // the message is actually replayed and processed.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.atomic.AtomicBoolean

            StatefulHandler releasing = { ctx, s, m -> "processed:$m" } as StatefulHandler
            def actor = Actor.stateful('init', { ActorContext ctx, s, m ->
                if (m == 'release') { ctx.become(releasing); ctx.unstashAll(); return s }
                ctx.stash()
                s
            } as StatefulHandler)

            def reply = actor.sendAndGet('payload')
            // Give the worker time to stash the message
            Thread.sleep(100)
            def landed = new AtomicBoolean(reply.isBound())
            assert !landed.get() : 'stashed reply should not be bound yet'

            actor.send('release')
            assert await(reply) == 'processed:payload'
            actor.stop()
        '''
    }

    @Test
    void testStashRollsBackOnHandlerThrow() {
        // If the main handler stashes and then throws, the stash is
        // rolled back: the reply binds to the exception, onError fires,
        // and the message is NOT in the stash for replay.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.atomic.AtomicInteger
            import java.util.concurrent.atomic.AtomicReference

            def fired = new AtomicInteger()
            def captured = new AtomicReference()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'misfire') {
                    ctx.stash()
                    throw new RuntimeException('after-stash')
                }
                "ok:$msg"
            } as ReactorHandler)
            actor.onError { ActorContext ctx, Throwable t, msg ->
                fired.incrementAndGet()
                captured.set(t.message)
            }

            try { await(actor.sendAndGet('misfire')); assert false }
            catch (RuntimeException e) { assert e.message == 'after-stash' }
            assert fired.get() == 1
            assert captured.get() == 'after-stash'

            // The actor must still be live — no stashed ghost messages
            // sitting in the buffer.
            assert await(actor.sendAndGet('next')) == 'ok:next'
            actor.stop()
        '''
    }

    @Test
    void testStashFromNonWorkerThreadThrows() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.atomic.AtomicReference

            def captured = new AtomicReference<ActorContext>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                captured.set(ctx); msg
            } as ReactorHandler)
            await(actor.sendAndGet('capture'))

            try {
                captured.get().stash()
                assert false : 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message.contains('worker thread')
            }
            actor.stop()
        '''
    }

    @Test
    void testStopRejectsStashedSendAndGetReply() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.CountDownLatch

            def stashed = new CountDownLatch(1)
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                ctx.stash()
                stashed.countDown()
                null
            } as ReactorHandler)

            def reply = actor.sendAndGet('forever')
            assert stashed.await(2, java.util.concurrent.TimeUnit.SECONDS)
            actor.stop()

            try {
                await(reply)
                assert false : 'stashed reply should have been rejected on stop'
            } catch (IllegalStateException e) {
                assert e.message.contains('stash')
            }
        '''
    }

    @Test
    void testStashedMessagesDoNotCountAgainstBoundedMailbox() {
        // The bound applies to the queue of pending sends; stashed
        // messages are held off-queue and do not occupy a slot. With
        // capacity=1, a fresh send succeeds even while one earlier
        // message is sitting in the stash.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ActorOptions
            import groovy.concurrent.ReactorHandler
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.CopyOnWriteArrayList

            def stashedOnce = new CountDownLatch(1)
            def seen = new CopyOnWriteArrayList()
            def options = ActorOptions.DEFAULTS.withBoundedMailbox(1, ActorOptions.Overflow.FAIL)
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'park') {
                    ctx.stash()
                    stashedOnce.countDown()
                    return null
                }
                seen << msg
                msg
            } as ReactorHandler, options)

            actor.send('park')
            assert stashedOnce.await(2, java.util.concurrent.TimeUnit.SECONDS)
            // 'park' is in the stash, not the queue — the slot is free.
            actor.send('after-1')
            for (int i = 0; i < 40 && seen.size() < 1; i++) Thread.sleep(25)
            assert seen.toList() == ['after-1']
            actor.stop()
        '''
    }

    @Test
    void testUnstashAllNoOpOnEmpty() {
        // Calling unstashAll without anything stashed must be a clean no-op.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler

            def actor = Actor.reactor({ ActorContext ctx, msg ->
                ctx.unstashAll()
                "ok:$msg"
            } as ReactorHandler)

            assert await(actor.sendAndGet('a')) == 'ok:a'
            assert await(actor.sendAndGet('b')) == 'ok:b'
            actor.stop()
        '''
    }

    // === Combined FSM integration test ===

    @Test
    void testFsmConnectionHandshake() {
        // End-to-end exercise of the design: a connection actor cycles
        // through disconnected → authenticating → connected. Commands
        // sent during authenticating are stashed and replayed once the
        // AuthResult lands. The transition uses become; the deferral
        // uses stash/unstashAll; state (the session info) is preserved
        // across every transition.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()

            StatefulHandler connected, authenticating
            connected = { ActorContext ctx, Map s, msg ->
                if (msg instanceof Map && msg.cmd) {
                    log << "exec(${msg.cmd}, token=${s.token})"
                    return [host: s.host, user: s.user, token: s.token, ops: s.ops + 1]
                }
                s
            } as StatefulHandler
            authenticating = { ActorContext ctx, Map s, msg ->
                if (msg instanceof Map && msg.auth) {
                    return [host: s.host, user: msg.auth, token: null, ops: 0]
                }
                if (msg instanceof Map && msg.result != null) {
                    ctx.become(connected)
                    ctx.unstashAll()
                    return [host: s.host, user: s.user, token: msg.result, ops: 0]
                }
                ctx.stash()
                s
            } as StatefulHandler
            StatefulHandler disconnected = { ActorContext ctx, Map s, msg ->
                if (msg instanceof Map && msg.connect) {
                    ctx.become(authenticating)
                    return [host: msg.connect, user: null, token: null, ops: 0]
                }
                s
            } as StatefulHandler

            def actor = Actor.stateful(
                [host: null, user: null, token: null, ops: 0],
                disconnected)

            actor.send([connect: 'db.example'])
            def r1 = actor.sendAndGet([cmd: 'read'])     // stashed in authenticating
            actor.send([auth: 'alice'])
            def r2 = actor.sendAndGet([cmd: 'write'])    // stashed in authenticating
            actor.send([result: 'tok-abc'])              // → connected, replays
            def r3 = actor.sendAndGet([cmd: 'query'])    // direct in connected

            def s1 = await(r1)
            def s2 = await(r2)
            def s3 = await(r3)

            assert log.toList() == [
                'exec(read, token=tok-abc)',
                'exec(write, token=tok-abc)',
                'exec(query, token=tok-abc)',
            ]
            assert s1.ops == 1
            assert s2.ops == 2
            assert s3.ops == 3
            assert s3.user == 'alice'
            assert s3.token == 'tok-abc'

            actor.stop()
        '''
    }

    // === Bounded stash (stash overflow policies) ===

    @Test
    void testStashBoundFailThrowsAndBindsCurrentReply() {
        // With FAIL, the (N+1)th stash throws IllegalStateException. The
        // handler does not catch, so dispatch reports the message as
        // failed: reply bound to the ISE, message is gone. The earlier
        // stashed messages are untouched and replay on unstashAll.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ActorOptions
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()
            StatefulHandler releasing = { ctx, s, m -> log << m; s } as StatefulHandler

            def options = ActorOptions.DEFAULTS
                .withStashBound(2, ActorOptions.StashOverflow.FAIL)
            def actor = Actor.stateful('init', { ActorContext ctx, s, m ->
                if (m == 'go') {
                    ctx.become(releasing)
                    ctx.unstashAll()
                    return s
                }
                ctx.stash()
                s
            } as StatefulHandler, options)

            def r1 = actor.sendAndGet('m1')
            def r2 = actor.sendAndGet('m2')
            def r3 = actor.sendAndGet('m3')          // hits the bound — FAIL

            try {
                await(r3)
                assert false : 'overflowed stash() should have failed the reply'
            } catch (IllegalStateException e) {
                assert e.message.contains('stash full')
            }

            actor.send('go')
            assert await(r1) == 'init'               // replayed under releasing
            assert await(r2) == 'init'
            for (int i = 0; i < 40 && log.size() < 2; i++) Thread.sleep(25)
            assert log.toList() == ['m1', 'm2']      // m3 is gone, not deferred
            actor.stop()
        '''
    }

    @Test
    void testStashBoundDropOldestEvictsAndBindsEvictedReply() {
        // With DROP_OLDEST, the oldest stashed message is evicted to make
        // room for the new one. Its sendAndGet reply binds to ISE so the
        // caller does not hang. The newest is in the stash for replay.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ActorOptions
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()
            StatefulHandler releasing = { ctx, s, m -> log << m; s } as StatefulHandler

            def options = ActorOptions.DEFAULTS
                .withStashBound(2, ActorOptions.StashOverflow.DROP_OLDEST)
            def actor = Actor.stateful('init', { ActorContext ctx, s, m ->
                if (m == 'go') {
                    ctx.become(releasing)
                    ctx.unstashAll()
                    return s
                }
                ctx.stash()
                s
            } as StatefulHandler, options)

            def r1 = actor.sendAndGet('m1')          // stashed
            def r2 = actor.sendAndGet('m2')          // stashed
            def r3 = actor.sendAndGet('m3')          // evicts m1, stashes m3

            try {
                await(r1)
                assert false : 'evicted reply should have been rejected'
            } catch (IllegalStateException e) {
                assert e.message.contains('evicted from stash')
            }

            actor.send('go')
            assert await(r2) == 'init'
            assert await(r3) == 'init'
            for (int i = 0; i < 40 && log.size() < 2; i++) Thread.sleep(25)
            assert log.toList() == ['m2', 'm3']
            actor.stop()
        '''
    }

    // === Scheduling (scheduleOnce / scheduleAtFixedRate) ===

    @Test
    void testScheduleOnceFiresAfterDelay() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            def fired = new CountDownLatch(1)
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    ctx.scheduleOnce('boom', Duration.ofMillis(50))
                    return msg
                }
                if (msg == 'boom') fired.countDown()
                msg
            } as ReactorHandler)

            actor.send('arm')
            assert fired.await(2, TimeUnit.SECONDS), 'scheduled message never arrived'
            actor.stop()
        '''
    }

    @Test
    void testScheduleOnceCancelSuppressesFire() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.Cancellable
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicBoolean
            import java.util.concurrent.atomic.AtomicReference

            def fired = new AtomicBoolean()
            def handle = new AtomicReference<Cancellable>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    handle.set(ctx.scheduleOnce('boom', Duration.ofMillis(100)))
                    return msg
                }
                if (msg == 'boom') fired.set(true)
                msg
            } as ReactorHandler)

            await(actor.sendAndGet('arm'))
            assert handle.get().cancel(), 'cancel should report success before fire'
            assert handle.get().isCancelled()
            Thread.sleep(250)  // well past the scheduled time
            assert !fired.get(), 'cancelled timer must not fire'
            actor.stop()
        '''
    }

    @Test
    void testScheduleAtFixedRateFiresRepeatedly() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicInteger

            def ticks = new AtomicInteger()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    ctx.scheduleAtFixedRate('tick', Duration.ofMillis(40), Duration.ofMillis(40))
                    return msg
                }
                if (msg == 'tick') ticks.incrementAndGet()
                msg
            } as ReactorHandler)

            actor.send('arm')
            Thread.sleep(300)
            def n = ticks.get()
            actor.stop()
            assert n >= 3, "expected at least 3 ticks in 300ms (got $n)"
        '''
    }

    @Test
    void testScheduleAtFixedRateCancelStopsFurtherFires() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.Cancellable
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicInteger
            import java.util.concurrent.atomic.AtomicReference

            def ticks = new AtomicInteger()
            def handle = new AtomicReference<Cancellable>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    handle.set(ctx.scheduleAtFixedRate('tick', Duration.ofMillis(40), Duration.ofMillis(40)))
                    return msg
                }
                if (msg == 'tick') {
                    if (ticks.incrementAndGet() == 2) handle.get().cancel()
                }
                msg
            } as ReactorHandler)

            actor.send('arm')
            Thread.sleep(400)              // would be ~9 ticks if uncancelled
            // A fire travels scheduler -> executor -> mailbox -> worker before it
            // counts, and cancel() only runs on the worker at tick 2. Any fires
            // already past the scheduler stay in flight and still land, so the
            // exact count is jitter-bound. The deterministic invariant is that
            // once cancel takes effect the count stops growing.
            def settled = ticks.get()
            Thread.sleep(200)              // several more periods
            def n = ticks.get()
            actor.stop()
            assert n == settled, "cancel must stop further fires (grew from $settled to $n)"
            assert n >= 2, "expected cancel to fire on at least the 2nd tick (got $n)"
            assert n < 9, "cancel must bound fires well below the uncancelled rate (got $n)"
        '''
    }

    @Test
    void testStopCancelsOutstandingTimers() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicInteger

            def ticks = new AtomicInteger()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    ctx.scheduleAtFixedRate('tick', Duration.ofMillis(30), Duration.ofMillis(30))
                    return msg
                }
                if (msg == 'tick') ticks.incrementAndGet()
                msg
            } as ReactorHandler)

            actor.send('arm')
            Thread.sleep(150)
            actor.stop()
            // Let the worker finish draining any tick that was already in-flight
            // at stop() time (a periodic tick crosses scheduler -> executor ->
            // worker threads, so one can be queued just before active flips).
            // After termination no further message can be dispatched, so a
            // cancelled timer means ticks can never move again — making the
            // snapshot below stable instead of racing the drain.
            def deadline = System.currentTimeMillis() + 2000
            while (!actor.terminated && System.currentTimeMillis() < deadline) Thread.sleep(5)
            assert actor.terminated, 'actor did not drain after stop()'
            def before = ticks.get()
            Thread.sleep(300)              // would be many more ticks if not cancelled
            assert ticks.get() == before, "ticks fired after stop (${ticks.get() - before} extra)"
        '''
    }

    @Test
    void testScheduledMessageGoesThroughOnError() {
        // A scheduled self-send dispatches like any other message —
        // including triggering onError when the handler throws.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.CopyOnWriteArrayList

            def errors = new CopyOnWriteArrayList()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                if (msg == 'arm') {
                    ctx.scheduleOnce('boom', Duration.ofMillis(40))
                    return msg
                }
                if (msg == 'boom') throw new RuntimeException('scheduled-boom')
                msg
            } as ReactorHandler)
            actor.onError { Throwable t, msg -> errors << t.message }

            actor.send('arm')
            for (int i = 0; i < 40 && errors.isEmpty(); i++) Thread.sleep(25)
            assert errors == ['scheduled-boom']
            actor.stop()
        '''
    }

    @Test
    void testScheduleOnceFromNonWorkerThreadThrows() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicReference

            def captured = new AtomicReference<ActorContext>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                captured.set(ctx); msg
            } as ReactorHandler)
            await(actor.sendAndGet('capture'))

            try {
                captured.get().scheduleOnce('x', Duration.ofMillis(50))
                assert false : 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message.contains('worker thread')
            }
            actor.stop()
        '''
    }

    @Test
    void testScheduleOnceRejectsNullArgs() {
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ReactorHandler
            import java.time.Duration
            import java.util.concurrent.atomic.AtomicReference

            def caughtNullMsg = new AtomicReference<Throwable>()
            def caughtNullDelay = new AtomicReference<Throwable>()
            def actor = Actor.reactor({ ActorContext ctx, msg ->
                try { ctx.scheduleOnce(null, Duration.ofMillis(10)) }
                catch (Throwable t) { caughtNullMsg.set(t) }
                try { ctx.scheduleOnce('x', null) }
                catch (Throwable t) { caughtNullDelay.set(t) }
                msg
            } as ReactorHandler)

            await(actor.sendAndGet('go'))
            assert caughtNullMsg.get() instanceof NullPointerException
            assert caughtNullDelay.get() instanceof NullPointerException
            actor.stop()
        '''
    }

    @Test
    void testStashBoundRejectBindsCurrentReplyAndKeepsEarlierStash() {
        // With REJECT, the current message is rejected (reply ISE'd, not
        // stashed). The already-stashed messages remain and replay on
        // unstashAll.
        assertScript '''
            import groovy.concurrent.Actor
            import groovy.concurrent.ActorContext
            import groovy.concurrent.ActorOptions
            import groovy.concurrent.StatefulHandler
            import java.util.concurrent.CopyOnWriteArrayList

            def log = new CopyOnWriteArrayList()
            StatefulHandler releasing = { ctx, s, m -> log << m; s } as StatefulHandler

            def options = ActorOptions.DEFAULTS
                .withStashBound(2, ActorOptions.StashOverflow.REJECT)
            def actor = Actor.stateful('init', { ActorContext ctx, s, m ->
                if (m == 'go') {
                    ctx.become(releasing)
                    ctx.unstashAll()
                    return s
                }
                ctx.stash()
                s
            } as StatefulHandler, options)

            def r1 = actor.sendAndGet('m1')          // stashed
            def r2 = actor.sendAndGet('m2')          // stashed
            def r3 = actor.sendAndGet('m3')          // REJECT — bound to ISE

            try {
                await(r3)
                assert false : 'rejected message should have failed reply'
            } catch (IllegalStateException e) {
                assert e.message.contains('rejected')
            }

            actor.send('go')
            assert await(r1) == 'init'
            assert await(r2) == 'init'
            for (int i = 0; i < 40 && log.size() < 2; i++) Thread.sleep(25)
            assert log.toList() == ['m1', 'm2']      // m3 is gone, m1+m2 remain
            actor.stop()
        '''
    }
}
