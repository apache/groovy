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
 *   <li>Terminal callbacks atomically close the upstream side
 *       ({@code closedRef}) and clear/cancel the stored subscription.
 *       This makes post-terminal {@code onNext} calls from non-compliant
 *       publishers harmless and releases resources promptly.</li>
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
     * Queue capacity for the push→pull bridge in
     * {@link #publisherToAsyncStream}.  256 provides a generous buffer
     * for bursty publishers while bounding memory.
     */
    private static final int QUEUE_CAPACITY = 256;

    /**
     * Cached awaitables for the two common {@code moveNext()} outcomes.
     * Eliminates per-call {@link CompletableFuture} + {@link GroovyPromise}
     * allocation on the hot path (every element and stream-end).
     */
    private static final Awaitable<Boolean> MOVE_NEXT_TRUE = Awaitable.of(Boolean.TRUE);
    private static final Awaitable<Boolean> MOVE_NEXT_FALSE = Awaitable.of(Boolean.FALSE);

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
                    cf.completeExceptionally(t);
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

    // Signal wrapper types allow us to distinguish values, errors, and
    // completion in a single queue without type confusion.

    private record ValueSignal<T>(T value) {
    }

    private record ErrorSignal(Throwable error) {
    }

    /** Singleton sentinel for stream completion. */
    private static final Object COMPLETE_SENTINEL = new Object();

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
     * <p>The internal bounded queue (capacity {@value QUEUE_CAPACITY})
     * absorbs minor timing jitter between producer and consumer.  Signals
     * use blocking {@code put()} for normal delivery with a non-blocking
     * {@code offer()} fallback when the publisher thread is interrupted —
     * ensuring no items or terminal events are silently dropped.</p>
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
    @SuppressWarnings("unchecked")
    private <T> AsyncStream<T> publisherToAsyncStream(Flow.Publisher<T> publisher) {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();
        // Tracks whether the stream has been closed (by consumer or terminal signal).
        // CAS ensures exactly-once semantics for the close/cleanup path.
        AtomicBoolean closedRef = new AtomicBoolean(false);
        AtomicBoolean streamClosed = new AtomicBoolean(false);

        publisher.subscribe(new Flow.Subscriber<T>() {
            @Override
            public void onSubscribe(Flow.Subscription s) {
                // §2.5: reject duplicate subscriptions
                if (!subRef.compareAndSet(null, s)) {
                    s.cancel();
                    return;
                }
                // Double-check pattern: if close() raced between the CAS and this point,
                // the subscription must be cancelled immediately to avoid a dangling stream.
                if (closedRef.get()) {
                    Flow.Subscription sub = subRef.getAndSet(null);
                    if (sub != null) sub.cancel();
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
                if (!closedRef.get()) {
                    try {
                        // Blocking put() guarantees the item reaches the consumer.
                        // Since demand is capped at 1 (one request(1) per moveNext),
                        // a well-behaved publisher will never overflow the queue; put()
                        // still protects against misbehaving publishers by blocking
                        // rather than silently dropping the value.
                        queue.put(new ValueSignal<>(item));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        // Blocking put() was interrupted. Fall back to non-blocking
                        // offer() so the item still reaches the consumer. With
                        // one-at-a-time demand the queue is almost never full, so
                        // offer() effectively always succeeds. If it doesn't
                        // (misbehaving publisher overfilling the queue), cancel
                        // upstream and inject an error signal to terminate the
                        // consumer cleanly instead of silently dropping the item.
                        if (!queue.offer(new ValueSignal<>(item))) {
                            Flow.Subscription sub = subRef.getAndSet(null);
                            if (sub != null) sub.cancel();
                            closedRef.set(true);
                            queue.offer(new ErrorSignal(
                                    new CancellationException(
                                            "Item delivery interrupted and queue full")));
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                // First terminal signal wins. Ignore duplicate terminal callbacks.
                if (!closedRef.compareAndSet(false, true)) {
                    return;
                }
                // Cancel subscription eagerly to release upstream resources
                Flow.Subscription sub = subRef.getAndSet(null);
                if (sub != null) sub.cancel();
                try {
                    // Terminal signals use blocking put() to guarantee delivery —
                    // the consumer MUST see the error to propagate it correctly.
                    queue.put(new ErrorSignal(t));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    // Blocking put() was interrupted. Fall back to non-blocking
                    // offer(). If that also fails (queue full), set streamClosed
                    // so the consumer's next moveNext() returns false instead of
                    // blocking indefinitely.
                    if (!queue.offer(new ErrorSignal(t))) {
                        streamClosed.set(true);
                    }
                }
            }

            @Override
            public void onComplete() {
                // First terminal signal wins. Ignore duplicate terminal callbacks.
                if (!closedRef.compareAndSet(false, true)) {
                    return;
                }
                // Clear subscription consistently with other terminal paths.
                Flow.Subscription sub = subRef.getAndSet(null);
                if (sub != null) sub.cancel();
                try {
                    // Blocking put() guarantees the consumer will see the sentinel,
                    // even if the queue was temporarily full from buffered values.
                    queue.put(COMPLETE_SENTINEL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    // Same fallback as onError: try non-blocking offer,
                    // set streamClosed if that also fails.
                    if (!queue.offer(COMPLETE_SENTINEL)) {
                        streamClosed.set(true);
                    }
                }
            }
        });

        return new AsyncStream<T>() {
            private T current;

            @Override
            public Awaitable<Boolean> moveNext() {
                if (streamClosed.get()) {
                    return MOVE_NEXT_FALSE;
                }

                try {
                    Object signal = queue.take();

                    if (signal instanceof ValueSignal) {
                        current = ((ValueSignal<T>) signal).value;
                        // Signal demand BEFORE returning so the publisher can
                        // begin producing the next value while the consumer
                        // processes this one — prevents livelock when both
                        // share a thread pool.
                        Flow.Subscription sub = subRef.get();
                        if (sub != null) sub.request(1);
                        return MOVE_NEXT_TRUE;
                    } else if (signal instanceof ErrorSignal es) {
                        streamClosed.set(true);
                        // Throw directly (matching AsyncStreamGenerator) to
                        // avoid unnecessary CF allocation on the error path
                        // and JDK 23+ CompletableFuture.get() wrapping issues.
                        Throwable cause = es.error;
                        if (cause instanceof Error err) throw err;
                        throw AsyncSupport.sneakyThrow(cause);
                    } else {
                        // COMPLETE_SENTINEL — end-of-stream
                        streamClosed.set(true);
                        return MOVE_NEXT_FALSE;
                    }
                } catch (InterruptedException ie) {
                    streamClosed.set(true);
                    Thread.currentThread().interrupt();
                    CancellationException ce = new CancellationException("Interrupted during moveNext");
                    ce.initCause(ie);
                    throw ce;
                }
            }

            @Override
            public T getCurrent() {
                return current;
            }

            @Override
            public void close() {
                if (streamClosed.compareAndSet(false, true)) {
                    closedRef.set(true);
                    Flow.Subscription sub = subRef.getAndSet(null);
                    if (sub != null) sub.cancel();
                    // Drain the queue and inject a sentinel to unblock a
                    // concurrent moveNext() that may be blocked in take().
                    // offer() is non-blocking and cannot throw InterruptedException;
                    // after clear(), the queue is empty (capacity 256) so offer()
                    // effectively always succeeds.
                    queue.clear();
                    queue.offer(COMPLETE_SENTINEL);
                }
            }
        };
    }
}
