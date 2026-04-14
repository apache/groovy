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
}
