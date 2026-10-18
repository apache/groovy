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
import static org.junit.jupiter.api.Assertions.assertThrows

final class BroadcastChannelTest {

    @Test
    void testBroadcastToMultipleSubscribers() {
        assertScript '''
            import groovy.concurrent.BroadcastChannel

            def broadcast = BroadcastChannel.create()
            def sub1 = broadcast.subscribe()
            def sub2 = broadcast.subscribe()

            async {
                broadcast.send('hello')
                broadcast.send('world')
                broadcast.close()
            }

            def results1 = []
            def results2 = []
            for (msg in sub1) { results1 << msg }
            for (msg in sub2) { results2 << msg }

            assert results1 == ['hello', 'world']
            assert results2 == ['hello', 'world']
        '''
    }

    @Test
    void testSubscriberCount() {
        assertScript '''
            import groovy.concurrent.BroadcastChannel

            def broadcast = BroadcastChannel.create()
            assert broadcast.subscriberCount == 0
            broadcast.subscribe()
            assert broadcast.subscriberCount == 1
            broadcast.subscribe()
            assert broadcast.subscriberCount == 2
            broadcast.close()
        '''
    }

    @Test
    void testLateSubscriberMissesEarlierMessages() {
        assertScript '''
            import groovy.concurrent.BroadcastChannel

            def broadcast = BroadcastChannel.create()
            def early = broadcast.subscribe()

            async { broadcast.send('first') }
            Thread.sleep(100)

            def late = broadcast.subscribe()

            async {
                broadcast.send('second')
                broadcast.close()
            }

            def earlyResults = []
            for (msg in early) { earlyResults << msg }

            def lateResults = []
            for (msg in late) { lateResults << msg }

            assert earlyResults == ['first', 'second']
            assert lateResults == ['second']
        '''
    }

    @Test
    void closeWithAFailureReachesEverySubscriber() {
        def broadcast = BroadcastChannel.create()
        def first = broadcast.subscribe()
        def second = broadcast.subscribe()
        def failure = new IllegalStateException('broadcast-boom')
        await broadcast.send('a')
        assert broadcast.close(failure)

        for (sub in [first, second]) {
            assert await(sub.receive()) == 'a'
            def error = assertThrows(IllegalStateException) {
                for await (ignored in sub) {
                }
            }
            assert error.is(failure)
        }
        assert broadcast.closeCause.is(failure)

        def late = assertThrows(ChannelClosedException) {
            broadcast.subscribe()
        }
        assert late.cause.is(failure)
        def sent = assertThrows(ChannelClosedException) {
            await broadcast.send('late')
        }
        assert sent.cause.is(failure)
        assert sent.message == 'BroadcastChannel is closed'

        broadcast.close()
        assert !broadcast.close(new IllegalStateException('later'))
        assert broadcast.closeCause.is(failure)
    }

    @Test
    void closeNullEndsSubscribersNormally() {
        def broadcast = BroadcastChannel.create()
        def sub = broadcast.subscribe()
        await broadcast.send('a')
        assert broadcast.close(null)
        def seen = []
        for await (value in sub) {
            seen << value
        }
        assert seen == ['a']
        assert broadcast.closeCause == null

        def closed = BroadcastChannel.create()
        def child = closed.subscribe()
        closed.close()
        assert !closed.close(new IllegalStateException('late'))
        def drained = []
        for await (value in child) {
            drained << value
        }
        assert drained.isEmpty()
        assert closed.closeCause == null
        def rejected = assertThrows(ChannelClosedException) {
            closed.subscribe()
        }
        assert rejected.cause == null

        def empty = BroadcastChannel.create()
        def only = new IllegalStateException('only')
        assert empty.close(only)
        assert !empty.close(null)
        assert empty.closeCause.is(only)
        def late = assertThrows(ChannelClosedException) {
            empty.subscribe()
        }
        assert late.cause.is(only)
    }
}
