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
package org.apache.groovy.runtime.async;

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridges JDK {@link Flow.Publisher} instances into Groovy's
 * {@link Awaitable}/{@link AsyncStream} world.
 *
 * <h2>Registration</h2>
 * This adapter is auto-discovered by the
 * {@link groovy.concurrent.AwaitableAdapterRegistry} via the
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter} file.
 * It handles any object that implements {@link Flow.Publisher}.
 *
 * <h2>Adaptation modes</h2>
 * <ul>
 *   <li><b>Single-value</b> ({@code await publisher}):
 *       subscribes, takes the first {@code onNext} item, cancels the
 *       upstream subscription, and completes the returned {@link Awaitable}.</li>
 *   <li><b>Multi-value</b> ({@code for await (item in publisher)}):
 *       wraps the publisher into an {@link AsyncStream} backed by a
 *       bounded {@link LinkedBlockingQueue}, providing natural
 *       back-pressure by requesting one item at a time.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>All subscriber callbacks ({@code onSubscribe}, {@code onNext},
 * {@code onError}, {@code onComplete}) are safe for invocation from
 * any thread.  Subscription references use {@link AtomicReference}
 * for safe publication and race-free cancellation.  The close path
 * uses a CAS on an {@link AtomicBoolean} to guarantee exactly-once
 * cleanup semantics.</p>
 *
 * <h2>Reactive Streams compliance</h2>
 * <p>This adapter follows the Reactive Streams specification
 * (JDK {@link Flow} variant) rules:</p>
 * <ul>
 *   <li>§2.5 — duplicate {@code onSubscribe} cancels the second subscription</li>
 *   <li>§2.13 — {@code null} items in {@code onNext} are rejected immediately</li>
 *   <li>All signals ({@code onNext}, {@code onError}, {@code onComplete}) use
 *       blocking {@code put()} with a non-blocking {@code offer()} fallback
 *       when the publisher thread is interrupted — this prevents both silent
 *       item loss and consumer deadlock even under unexpected interrupts</li>
 *   <li>Terminal callbacks enqueue an explicit terminal signal
 *       ({@code onError}/{@code onComplete}) before the consumer observes
 *       end-of-stream.  A dedicated {@link AtomicBoolean} suppresses
 *       post-terminal {@code onNext} calls from non-compliant publishers
 *       without racing the consumer into a premature normal completion.</li>
 *   <li>Back-pressure is enforced by requesting exactly one item after
 *       each consumed element; demand is signalled <em>before</em>
 *       {@code moveNext()} returns, preventing livelock when producer and
 *       consumer share the same thread pool</li>
 * </ul>
 *
 * @see groovy.concurrent.AwaitableAdapterRegistry
 * @see AsyncStream
 * @since 6.0.0
 */
public class FlowPublisherAdapter implements AwaitableAdapter {

    /**
     * Returns {@code true} if the given type is assignable to
     * {@link Flow.Publisher}, enabling single-value {@code await}.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can handle the type
     */
    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Flow.Publisher.class.isAssignableFrom(type);
    }

    /**
     * Returns {@code true} if the given type is assignable to
     * {@link Flow.Publisher}, enabling multi-value {@code for await}.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can produce an async stream
     */
    @Override
    public boolean supportsAsyncStream(Class<?> type) {
        return Flow.Publisher.class.isAssignableFrom(type);
    }

    /**
     * Converts a {@link Flow.Publisher} to a single-value {@link Awaitable}
     * by subscribing and taking the first emitted item.
     *
     * @param source the publisher instance
     * @param <T>    the element type
     * @return an awaitable that resolves to the first emitted value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        return publisherToAwaitable((Flow.Publisher<T>) source);
    }

    /**
     * Converts a {@link Flow.Publisher} to a multi-value {@link AsyncStream}
     * for use with {@code for await} loops.
     *
     * @param source the publisher instance
     * @param <T>    the element type
     * @return an async stream that yields publisher items
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> AsyncStream<T> toAsyncStream(Object source) {
        return publisherToAsyncStream((Flow.Publisher<T>) source);
    }

    // ---- Single-value adaptation (await publisher) ----

    /**
     * Subscribes to the publisher, takes the <em>first</em> emitted item,
     * cancels the upstream subscription, and returns a completed
     * {@link Awaitable}.
     *
     * <p>If the publisher completes without emitting any item,
     * the returned awaitable resolves to {@code null}.</p>
     *
     * @param publisher the upstream publisher
     * @param <T>       the element type
     * @return an awaitable that completes with the first emitted value
     */
    private <T> Awaitable<T> publisherToAwaitable(Flow.Publisher<T> publisher) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        // AtomicReference ensures safe publication of the subscription
        // across the onSubscribe thread and callback threads (§1.3).
        AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();
        // Guard against non-compliant publishers that send multiple signals
        AtomicBoolean done = new AtomicBoolean(false);

        publisher.subscribe(new Flow.Subscriber<T>() {
            @Override
            public void onSubscribe(Flow.Subscription s) {
                // §2.5: reject duplicate subscriptions
                if (!subRef.compareAndSet(null, s)) {
                    s.cancel();
                    return;
                }
                s.request(1);
            }

            @Override
            public void onNext(T item) {
                // §2.13: null items are spec violations
                if (item == null) {
                    onError(new NullPointerException(
                            "Flow.Publisher onNext received null (Reactive Streams §2.13)"));
                    return;
                }
                if (done.compareAndSet(false, true)) {
                    cf.complete(item);
                    Flow.Subscription sub = subRef.getAndSet(null);
                    if (sub != null) sub.cancel();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (done.compareAndSet(false, true)) {
                    // §2.13 requires non-null, but defend against non-compliant publishers
                    cf.completeExceptionally(t != null ? t
                            : new NullPointerException("onError called with null (Reactive Streams §2.13 violation)"));
                    // Cancel subscription to release resources (idempotent per §3.7)
                    Flow.Subscription sub = subRef.getAndSet(null);
                    if (sub != null) sub.cancel();
                }
            }

            @Override
            public void onComplete() {
                // Publisher completed before emitting — resolve to null
                if (done.compareAndSet(false, true)) {
                    cf.complete(null);
                    // Mirror onNext/onError cleanup for prompt resource release.
                    Flow.Subscription sub = subRef.getAndSet(null);
                    if (sub != null) sub.cancel();
                }
            }
        });

        return new GroovyPromise<>(cf);
    }

    // ---- Multi-value adaptation (for await publisher) ----

    /**
     * Wraps a {@link Flow.Publisher} into an {@link AsyncStream},
     * providing a pull-based iteration interface over a push-based source.
     *
     * <p>Back-pressure is enforced by requesting exactly one item after
     * each consumed element.  Demand is signalled <em>before</em> the
     * consumer's {@code moveNext()} awaitable completes, so the publisher
     * can begin producing the next value while the consumer processes the
     * current one — this prevents livelock when producer and consumer
     * share the same thread pool.</p>
     *
     * <p><b>Resource management:</b> When the consumer calls
     * {@link AsyncStream#close()} (e.g. via {@code break} in a
     * {@code for await} loop), the upstream subscription is cancelled
     * and a completion sentinel is injected to unblock any pending
     * {@code moveNext()} call.</p>
     *
     * @param publisher the upstream publisher
     * @param <T>       the element type
     * @return an async stream that yields publisher items
     */
    private <T> AsyncStream<T> publisherToAsyncStream(Flow.Publisher<T> publisher) {
        FlowAsyncStream<T> stream = new FlowAsyncStream<>();
        publisher.subscribe(stream.newSubscriber());
        return stream;
    }

    /**
     * Named implementation of {@link AsyncStream} that extends
     * {@link AbstractAsyncStream} to bridge a push-based
     * {@link Flow.Publisher} into a pull-based async stream.
     *
     * <p>Inherits the common {@link AbstractAsyncStream#moveNext() moveNext()}/
     * {@link AbstractAsyncStream#getCurrent() getCurrent()}/
     * {@link AbstractAsyncStream#close() close()} template and overrides:</p>
     * <ul>
     *   <li>{@link #beforeTake()} — drains remaining signals before returning
     *       false (checks {@code closed && queue.isEmpty()})</li>
     *   <li>{@link #afterValueConsumed()} — signals back-pressure demand via
     *       {@code Subscription.request(1)}</li>
     *   <li>{@link #onClose()} — cancels the upstream subscription, drains
     *       the queue, and injects a {@link #COMPLETE} sentinel to unblock
     *       any pending {@code moveNext()}</li>
     * </ul>
     *
     * <p>The internal bounded queue (capacity {@value QUEUE_CAPACITY})
     * absorbs minor timing jitter.  Signals use blocking {@code put()}
     * for normal delivery with a non-blocking {@code offer()} fallback
     * when the publisher thread is interrupted — ensuring no items or
     * terminal events are silently dropped.</p>
     *
     * @param <T> the element type
     */
    private static final class FlowAsyncStream<T> extends AbstractAsyncStream<T> {

        /**
         * Queue capacity for the push→pull bridge.  With one-at-a-time
         * demand ({@code request(1)} per consumed element), a well-behaved
         * publisher will never enqueue more than one value at a time.
         * A capacity of 2 accommodates the value + a racing terminal
         * signal without blocking, while keeping memory minimal.
         */
        private static final int QUEUE_CAPACITY = 2;

        private final AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();

        /**
         * Guards idempotency of terminal signal delivery.  Set exactly once
         * by {@code putTerminalSignal()} (or the {@code onNext()} interrupt
         * fallback) <em>before</em> the signal is enqueued.  This flag is
         * intentionally separate from {@link #closed}: it prevents duplicate
         * or post-terminal signals from entering the queue, while {@code closed}
         * is set only when the consumer ({@link #moveNext()}) actually consumes
         * the terminal signal.  Keeping them separate eliminates a TOCTOU race
         * where the consumer's {@link #beforeTake()} could observe
         * {@code closed == true} and an empty queue simultaneously — returning
         * a premature normal completion before the terminal signal is visible.
         */
        private final AtomicBoolean terminalSignalQueued = new AtomicBoolean(false);

        FlowAsyncStream() {
            super(new LinkedBlockingQueue<>(QUEUE_CAPACITY));
        }

        /**
         * Creates a new {@link Flow.Subscriber} wired to this stream's
         * internal queue and lifecycle state.
         */
        Flow.Subscriber<T> newSubscriber() {
            return new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription s) {
                    // §2.5: reject duplicate subscriptions
                    if (!subRef.compareAndSet(null, s)) {
                        s.cancel();
                        return;
                    }
                    // Double-check: if close() raced before subscription was set,
                    // cancel immediately to avoid a dangling upstream.
                    if (closed.get() || terminalSignalQueued.get()) {
                        cancelSubscription();
                        return;
                    }
                    s.request(1);
                }

                @Override
                public void onNext(T item) {
                    // §2.13: null items are spec violations
                    if (item == null) {
                        onError(new NullPointerException(
                                "Flow.Publisher onNext received null (Reactive Streams §2.13)"));
                        return;
                    }
                    if (closed.get() || terminalSignalQueued.get()) return;
                    try {
                        queue.put(new ValueSignal(item));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        if (!queue.offer(new ValueSignal(item))) {
                            cancelSubscription();
                            terminalSignalQueued.set(true);
                            // Clear the queue to guarantee room for the terminal signal;
                            // without this, a full queue could leave the consumer blocked
                            // in take() with no signal to unblock it.
                            queue.clear();
                            queue.offer(COMPLETE);
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    // §2.13 requires non-null, but defend against non-compliant publishers
                    Throwable cause = t != null ? t
                            : new NullPointerException("onError called with null (Reactive Streams §2.13 violation)");
                    putTerminalSignal(new ErrorSignal(cause));
                }

                @Override
                public void onComplete() {
                    putTerminalSignal(COMPLETE);
                }

                /**
                 * Shared logic for {@code onError()} and {@code onComplete()}:
                 * atomically marks a terminal signal as queued, cancels the
                 * upstream subscription, and delivers the signal to the
                 * consumer queue.
                 *
                 * <p>This method CASes on {@link #terminalSignalQueued} — not
                 * on {@link #closed} — to guarantee that the terminal signal
                 * is visible in the queue before the consumer can observe
                 * end-of-stream.  {@code closed} is later set by
                 * {@link AbstractAsyncStream#moveNext()} when it actually
                 * consumes the terminal signal.
                 */
                private void putTerminalSignal(Object signal) {
                    if (!terminalSignalQueued.compareAndSet(false, true)) return;
                    cancelSubscription();
                    try {
                        queue.put(signal);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        queue.offer(signal);
                    }
                }
            };
        }

        @Override
        protected Awaitable<Boolean> beforeTake() {
            return (closed.get() && queue.isEmpty()) ? MOVE_NEXT_FALSE : null;
        }

        @Override
        protected void afterValueConsumed() {
            // Signal demand BEFORE returning so the publisher can begin
            // producing the next value while the consumer processes this
            // one — prevents livelock when both share a thread pool.
            Flow.Subscription sub = subRef.get();
            if (sub != null) sub.request(1);
        }

        @Override
        protected void onClose() {
            cancelSubscription();
            // Drain the queue and inject a sentinel to unblock a
            // concurrent moveNext() that may be blocked in take().
            queue.clear();
            queue.offer(COMPLETE);
        }

        private void cancelSubscription() {
            Flow.Subscription sub = subRef.getAndSet(null);
            if (sub != null) sub.cancel();
        }
    }
}
