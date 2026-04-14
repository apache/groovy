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
    void testConcurrentSendsAreSerialised() {
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
}
