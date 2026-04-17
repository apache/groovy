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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

import static groovy.test.GroovyAssert.assertScript

final class AgentChangesTest {

    @Test
    void emitsValueAfterEachUpdate() {
        Agent<Integer> agent = Agent.create(0)
        try {
            List<Integer> received = Collections.synchronizedList([])
            CountDownLatch sawThree = new CountDownLatch(3)

            // SubmissionPublisher.subscribe() registers the subscriber
            // synchronously, so subsequent offers reach it without further
            // synchronisation needed here.
            agent.changes().subscribe(new Flow.Subscriber<Integer>() {
                @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
                @Override void onNext(Integer item) { received << item; sawThree.countDown() }
                @Override void onError(Throwable t) {}
                @Override void onComplete() {}
            })

            agent.send { it + 1 }
            agent.send { it + 1 }
            agent.send { it + 1 }

            assert sawThree.await(2, TimeUnit.SECONDS)
            assert received == [1, 2, 3]
        } finally {
            agent.shutdown()
        }
    }

    @Test
    void doesNotReplayPriorChangesToLateSubscriber() {
        Agent<Integer> agent = Agent.create(0)
        try {
            agent.send { it + 100 }
            // Wait for the update to land before subscribing
            assert org.apache.groovy.runtime.async.AsyncSupport.await(agent.getAsync()) == 100

            List<Integer> received = Collections.synchronizedList([])
            CountDownLatch sawOne = new CountDownLatch(1)
            agent.changes().subscribe(new Flow.Subscriber<Integer>() {
                @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
                @Override void onNext(Integer item) { received << item; sawOne.countDown() }
                @Override void onError(Throwable t) {}
                @Override void onComplete() {}
            })

            agent.send { it + 1 }
            assert sawOne.await(2, TimeUnit.SECONDS)
            assert received == [101]
        } finally {
            agent.shutdown()
        }
    }

    @Test
    void shutdownClosesPublisher() {
        Agent<Integer> agent = Agent.create(0)
        CountDownLatch completed = new CountDownLatch(1)
        agent.changes().subscribe(new Flow.Subscriber<Integer>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(Integer item) {}
            @Override void onError(Throwable t) {}
            @Override void onComplete() { completed.countDown() }
        })
        agent.shutdown()
        assert completed.await(2, TimeUnit.SECONDS)
    }

    @Test
    void pendingUpdatesAreDeliveredBeforeShutdownCloses() {
        // Regression for race where shutdown() closed the publisher before
        // queued updates had run, causing applyUpdate's offer() to throw
        // IllegalStateException and drop emissions silently.
        Agent<Integer> agent = Agent.create(0)
        int count = 50
        List<Integer> received = Collections.synchronizedList([])
        CountDownLatch completed = new CountDownLatch(1)

        agent.changes().subscribe(new Flow.Subscriber<Integer>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(Integer item) { received << item }
            @Override void onError(Throwable t) {}
            @Override void onComplete() { completed.countDown() }
        })

        count.times { agent.send { it + 1 } }
        agent.shutdown()

        assert completed.await(5, TimeUnit.SECONDS)
        assert received.size() == count
        assert received == (1..count).toList()
    }

    @Test
    void changesAfterShutdownCompletesImmediately() {
        // Regression for lazy changes() creating a publisher after shutdown
        // that was never closed, leaving subscribers hanging.
        Agent<Integer> agent = Agent.create(0)
        agent.shutdown()

        CountDownLatch completed = new CountDownLatch(1)
        agent.changes().subscribe(new Flow.Subscriber<Integer>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(Integer item) {}
            @Override void onError(Throwable t) {}
            @Override void onComplete() { completed.countDown() }
        })

        assert completed.await(2, TimeUnit.SECONDS)
    }

    @Test
    void doubleShutdownIsSafe() {
        Agent<Integer> agent = Agent.create(0)
        agent.shutdown()
        agent.shutdown()  // must not throw
    }

    @Test
    void multipleSubscribersEachReceiveAll() {
        Agent<String> agent = Agent.create('init')
        try {
            List<String> a = Collections.synchronizedList([])
            List<String> b = Collections.synchronizedList([])
            CountDownLatch latchA = new CountDownLatch(2)
            CountDownLatch latchB = new CountDownLatch(2)

            agent.changes().subscribe(sink(a, latchA))
            agent.changes().subscribe(sink(b, latchB))

            agent.send { 'hello' }
            agent.send { 'world' }

            assert latchA.await(2, TimeUnit.SECONDS)
            assert latchB.await(2, TimeUnit.SECONDS)
            assert a == ['hello', 'world']
            assert b == ['hello', 'world']
        } finally {
            agent.shutdown()
        }
    }

    @Test
    void forAwaitOverChanges() {
        // End-to-end via the FlowPublisherAdapter
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create(0)
            try {
                def collected = []
                async {
                    Thread.sleep(30)  // let consumer subscribe
                    3.times { agent.send { it + 1 } }
                    Thread.sleep(50)
                    agent.shutdown()
                }

                for await (val in agent.changes()) {
                    collected << val
                }
                assert collected == [1, 2, 3]
            } finally {
                agent.shutdown()
            }
        '''
    }

    private Flow.Subscriber<String> sink(List<String> sink, CountDownLatch latch) {
        return new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String item) { sink << item; latch.countDown() }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        }
    }
}
