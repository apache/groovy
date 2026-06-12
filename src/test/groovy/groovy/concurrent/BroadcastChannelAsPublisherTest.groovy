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

final class BroadcastChannelAsPublisherTest {

    /** Spin until {@code cond} is true or the timeout elapses. */
    private static void waitUntil(long timeoutMs = 2000, Closure<Boolean> cond) {
        long deadline = System.currentTimeMillis() + timeoutMs
        while (!cond.call()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("waitUntil timed out after ${timeoutMs}ms")
            }
            Thread.sleep(2)
        }
    }

    @Test
    void deliversValuesToSingleSubscriber() {
        BroadcastChannel<String> broadcast = BroadcastChannel.create()
        Flow.Publisher<String> publisher = broadcast.asPublisher()

        List<String> received = []
        CountDownLatch done = new CountDownLatch(1)

        publisher.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String item) { received << item }
            @Override void onError(Throwable t) { done.countDown() }
            @Override void onComplete() { done.countDown() }
        })

        // BroadcastChannel.subscribe() registers the per-subscriber channel
        // synchronously, so subsequent sends are buffered for the new
        // subscriber even before its drain task starts.
        broadcast.send('a')
        broadcast.send('b')
        broadcast.send('c')
        broadcast.close()

        assert done.await(2, TimeUnit.SECONDS)
        assert received == ['a', 'b', 'c']
    }

    @Test
    void multipleSubscribersEachReceiveAll() {
        BroadcastChannel<Integer> broadcast = BroadcastChannel.create()
        Flow.Publisher<Integer> publisher = broadcast.asPublisher()

        List<Integer> a = []
        List<Integer> b = []
        CountDownLatch done = new CountDownLatch(2)

        def subscriber = { List<Integer> sink ->
            return new Flow.Subscriber<Integer>() {
                @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
                @Override void onNext(Integer item) { sink << item }
                @Override void onError(Throwable t) { done.countDown() }
                @Override void onComplete() { done.countDown() }
            }
        }

        publisher.subscribe(subscriber(a))
        publisher.subscribe(subscriber(b))

        (1..3).each { broadcast.send(it) }
        broadcast.close()

        assert done.await(2, TimeUnit.SECONDS)
        assert a == [1, 2, 3]
        assert b == [1, 2, 3]
    }

    @Test
    void honoursBackpressure() {
        BroadcastChannel<Integer> broadcast = BroadcastChannel.create()
        Flow.Publisher<Integer> publisher = broadcast.asPublisher()

        List<Integer> received = []
        CountDownLatch firstBatch = new CountDownLatch(2)
        CountDownLatch done = new CountDownLatch(1)
        def subscriptionRef = new java.util.concurrent.atomic.AtomicReference<Flow.Subscription>()

        publisher.subscribe(new Flow.Subscriber<Integer>() {
            @Override void onSubscribe(Flow.Subscription s) { subscriptionRef.set(s); s.request(2) }
            @Override void onNext(Integer item) { received << item; firstBatch.countDown() }
            @Override void onError(Throwable t) { done.countDown() }
            @Override void onComplete() { done.countDown() }
        })

        // send four values; subscriber only requested 2 — extras must wait
        Thread.startDaemon {
            (1..4).each { broadcast.send(it) }
        }

        assert firstBatch.await(2, TimeUnit.SECONDS)
        // Negative check: ensure no further onNext fires while demand is zero.
        // This is genuinely a "nothing happens within X ms" assertion, so a
        // small bounded wait is appropriate.
        Thread.sleep(50)
        assert received == [1, 2]

        // Request the rest
        subscriptionRef.get().request(Long.MAX_VALUE)
        broadcast.close()

        assert done.await(2, TimeUnit.SECONDS)
        assert received == [1, 2, 3, 4]
    }

    @Test
    void cancelledSubscribersAreRemovedFromList() {
        // Regression: asPublisher() subscriptions used to leak their backing
        // AsyncChannel in the parent's subscribers list, even after cancel.
        BroadcastChannel<Integer> broadcast = BroadcastChannel.create()
        Flow.Publisher<Integer> publisher = broadcast.asPublisher()

        int n = 10
        def subs = []
        n.times {
            def ref = new java.util.concurrent.atomic.AtomicReference<Flow.Subscription>()
            publisher.subscribe(new Flow.Subscriber<Integer>() {
                @Override void onSubscribe(Flow.Subscription s) { ref.set(s); s.request(Long.MAX_VALUE) }
                @Override void onNext(Integer item) {}
                @Override void onError(Throwable t) {}
                @Override void onComplete() {}
            })
            subs << ref
        }
        // Subscribe registration is synchronous, so the count should be n
        // immediately — but wait deterministically rather than guessing.
        waitUntil { broadcast.subscriberCount == n }

        subs.each { it.get().cancel() }
        waitUntil { broadcast.subscriberCount == 0 }
    }

    @Test
    void cancellationStopsDelivery() {
        BroadcastChannel<Integer> broadcast = BroadcastChannel.create()
        Flow.Publisher<Integer> publisher = broadcast.asPublisher()

        List<Integer> received = []
        def subRef = new java.util.concurrent.atomic.AtomicReference<Flow.Subscription>()
        CountDownLatch cancelled = new CountDownLatch(1)

        publisher.subscribe(new Flow.Subscriber<Integer>() {
            @Override void onSubscribe(Flow.Subscription s) { subRef.set(s); s.request(Long.MAX_VALUE) }
            @Override void onNext(Integer item) {
                received << item
                if (received.size() == 2) {
                    subRef.get().cancel()
                    cancelled.countDown()
                }
            }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        })

        Thread.startDaemon {
            try { (1..10).each { broadcast.send(it) } } catch (ChannelClosedException ignored) {}
        }
        // Wait until cancel has fired, plus a small grace period to confirm
        // no further onNext signals arrive (the negative part of this test).
        assert cancelled.await(2, TimeUnit.SECONDS)
        Thread.sleep(50)
        broadcast.close()

        // After cancellation, the subscriber should stop receiving — exact
        // count depends on timing but must be ≥2 and not include all 10.
        assert received.size() >= 2
        assert received.size() < 10
    }

    @Test
    void forAwaitOverPublisher() {
        // End-to-end via the FlowPublisherAdapter
        assertScript '''
            import groovy.concurrent.BroadcastChannel

            def broadcast = BroadcastChannel.create()
            def publisher = broadcast.asPublisher()

            async {
                Thread.sleep(30)  // let consumer subscribe
                ['x', 'y', 'z'].each { broadcast.send(it) }
                broadcast.close()
            }

            def collected = []
            for await (item in publisher) {
                collected << item
            }
            assert collected == ['x', 'y', 'z']
        '''
    }
}
