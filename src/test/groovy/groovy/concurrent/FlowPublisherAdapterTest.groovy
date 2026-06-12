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
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class FlowPublisherAdapterTest {

    @Test
    void testAwaitOnSubmissionPublisherTakesFirstValue() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher
            import groovy.concurrent.Awaitable

            def publisher = new SubmissionPublisher<String>()
            // Subscribe before the producer runs — SubmissionPublisher.submit
            // only delivers to subscribers present at submit-time.
            def awaitable = Awaitable.from(publisher)
            async {
                publisher.submit('first')
                publisher.submit('second')
                publisher.close()
            }
            def result = await awaitable
            assert result == 'first'
        '''
    }

    @Test
    void testAwaitOnEmptyPublisherCompletesWithNull() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher
            import groovy.concurrent.Awaitable

            def publisher = new SubmissionPublisher<String>()
            def awaitable = Awaitable.from(publisher)
            async { publisher.close() }
            def result = await awaitable
            assert result == null
        '''
    }

    @Test
    void testAwaitOnFailingPublisherRethrows() {
        def boom = new RuntimeException('boom')
        Throwable thrown = shouldFail RuntimeException, {
            def publisher = new SubmissionPublisher<String>()
            new Thread({
                publisher.closeExceptionally(boom)
            }).start()
            org.apache.groovy.runtime.async.AsyncSupport.await(groovy.concurrent.Awaitable.from(publisher))
        }
        assert thrown.message == 'boom'
    }

    @Test
    void testForAwaitIteratesAllValues() {
        // SubmissionPublisher.submit delivers only to subscribers present at
        // submit-time, so we must wait for `for await` to subscribe before
        // the producer starts — otherwise on fast CI the producer races ahead
        // and all 5 values are dropped before the consumer ever sees them.
        assertScript '''
            import java.util.concurrent.SubmissionPublisher
            import groovy.concurrent.Awaitable

            def publisher = new SubmissionPublisher<Integer>()
            async {
                // SubmissionPublisher only delivers submitted items to existing subscribers.
                for (int i = 0; i < 100 && !publisher.hasSubscribers(); i++) {
                    await Awaitable.delay(50)
                }

                (1..5).each { publisher.submit(it) }
                publisher.close()
            }
            def results = []
            for await (item in publisher) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitOnEmptyPublisher() {
        assertScript '''
            import java.util.concurrent.SubmissionPublisher

            def publisher = new SubmissionPublisher<Integer>()
            async { publisher.close() }
            def results = []
            for await (item in publisher) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitPropagatesError() {
        def thrown = shouldFail RuntimeException, '''
            import java.util.concurrent.SubmissionPublisher
            import groovy.concurrent.Awaitable

            def publisher = new SubmissionPublisher<Integer>()
            async {
                // SubmissionPublisher only delivers submitted items to existing subscribers.
                for (int i = 0; i < 100 && !publisher.hasSubscribers(); i++) {
                    await Awaitable.delay(50)
                }

                publisher.submit(1)
                publisher.submit(2)
                publisher.closeExceptionally(new RuntimeException('mid-stream'))
            }
            def results = []
            for await (item in publisher) {
                results << item
            }
        '''
        assert thrown.message == 'mid-stream'
    }

    @Test
    void testNullOnNextBecomesNpe() {
        // Custom publisher that violates §2.13
        def publisher = { Flow.Subscriber<Object> sub ->
            sub.onSubscribe(new Flow.Subscription() {
                @Override void request(long n) { sub.onNext(null) }
                @Override void cancel() {}
            })
        } as Flow.Publisher<Object>

        def thrown = shouldFail NullPointerException, {
            org.apache.groovy.runtime.async.AsyncSupport.await(groovy.concurrent.Awaitable.from(publisher))
        }
        assert thrown.message.contains('§2.13')
    }

    @Test
    void testEarlyBreakCancelsSubscription() {
        // Verify the subscription is cancelled when the consumer breaks early.
        AtomicBoolean cancelled = new AtomicBoolean()
        AtomicInteger requested = new AtomicInteger()
        CountDownLatch cancelSignalled = new CountDownLatch(1)

        def publisher = { Flow.Subscriber<Integer> sub ->
            sub.onSubscribe(new Flow.Subscription() {
                @Override
                void request(long n) {
                    requested.addAndGet((int) n)
                    // Emit a few values; do not auto-complete
                    Thread.startDaemon {
                        for (int i = 0; i < (int) n; i++) {
                            sub.onNext(i)
                        }
                    }
                }
                @Override void cancel() { cancelled.set(true); cancelSignalled.countDown() }
            })
        } as Flow.Publisher<Integer>

        def shell = new GroovyShell(new Binding(publisher: publisher))
        shell.evaluate '''
            int seen = 0
            for await (item in publisher) {
                seen++
                if (seen >= 3) break
            }
        '''

        assert cancelSignalled.await(5, TimeUnit.SECONDS), 'Subscription.cancel was never invoked after early break'
        assert cancelled.get()
    }

    @Test
    void testCrossThreadCancelUnblocksIterator() {
        // A publisher that never emits or completes — hasNext() will park in
        // queue.take(). Closing the iterable from another thread must unblock it.
        AtomicBoolean upstreamCancelled = new AtomicBoolean()
        CountDownLatch requested = new CountDownLatch(1)
        def publisher = { Flow.Subscriber<Integer> sub ->
            sub.onSubscribe(new Flow.Subscription() {
                @Override void request(long n) { requested.countDown() }
                @Override void cancel() { upstreamCancelled.set(true) }
            })
        } as Flow.Publisher<Integer>

        def iterable = AwaitableAdapterRegistry.toIterable(publisher)
        assert iterable instanceof AutoCloseable
        def iter = iterable.iterator()
        assert requested.await(5, TimeUnit.SECONDS), 'subscriber never asked to request'

        AtomicReference<Boolean> hasNextResult = new AtomicReference<>()
        CountDownLatch done = new CountDownLatch(1)
        Thread consumer = new Thread({
            hasNextResult.set(iter.hasNext())
            done.countDown()
        })
        consumer.start()

        // Close from the main thread — must unblock the parked consumer.
        ((AutoCloseable) iterable).close()

        assert done.await(5, TimeUnit.SECONDS), 'hasNext() did not unblock after cancel'
        assert hasNextResult.get() == Boolean.FALSE
        assert upstreamCancelled.get(), 'upstream subscription should have been cancelled'
    }

    @Test
    void testCancelBeforeOnSubscribeCancelsLateSubscription() {
        // Race: cancel() runs before onSubscribe delivers its Subscription.
        // The late subscription must still be cancelled.
        AtomicBoolean upstreamCancelled = new AtomicBoolean()
        AtomicReference<Flow.Subscriber<Integer>> subscriber = new AtomicReference<>()
        def publisher = { Flow.Subscriber<Integer> sub ->
            subscriber.set(sub) // defer onSubscribe
        } as Flow.Publisher<Integer>

        def iterable = AwaitableAdapterRegistry.toIterable(publisher)
        iterable.iterator()
        assert subscriber.get() != null

        // Cancel *before* delivering onSubscribe.
        ((AutoCloseable) iterable).close()

        // Now deliver the subscription — adapter must cancel it.
        subscriber.get().onSubscribe(new Flow.Subscription() {
            @Override void request(long n) {}
            @Override void cancel() { upstreamCancelled.set(true) }
        })

        assert upstreamCancelled.get(), 'late subscription should have been cancelled'
    }

    @Test
    void testBlockingIterableIsSingleUseAndCleansUpFirstSubscription() {
        // Two requirements: (1) calling iterator() twice must fail fast, rather
        // than silently leak the first subscription; (2) close() must still
        // cancel the first subscription even after the rejected second call.
        AtomicBoolean firstCancelled = new AtomicBoolean()
        def publisher = { Flow.Subscriber<Integer> sub ->
            sub.onSubscribe(new Flow.Subscription() {
                @Override void request(long n) {}
                @Override void cancel() { firstCancelled.set(true) }
            })
        } as Flow.Publisher<Integer>

        def iterable = AwaitableAdapterRegistry.toIterable(publisher)
        iterable.iterator()

        shouldFail(IllegalStateException) {
            iterable.iterator()
        }

        ((AutoCloseable) iterable).close()
        assert firstCancelled.get(), 'first subscription must be cancelled even after a rejected second iterator()'
    }

    @Test
    void testAdapterRegistered() {
        // The registry resolves Flow.Publisher via the built-in adapter.
        def publisher = new SubmissionPublisher<String>()
        try {
            def awaitable = Awaitable.from(publisher)
            assert awaitable != null
            def iter = AwaitableAdapterRegistry.toIterable(publisher)
            assert iter != null
        } finally {
            publisher.close()
        }
    }
}
