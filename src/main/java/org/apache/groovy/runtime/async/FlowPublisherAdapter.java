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

import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapter for {@link java.util.concurrent.Flow.Publisher}, the JDK's built-in
 * Reactive Streams type. Enables:
 * <ul>
 *   <li>{@code await publisher} — completes with the first {@code onNext}
 *       value, then cancels the subscription. Completes with {@code null} if
 *       the publisher signals {@code onComplete} without emitting.</li>
 *   <li>{@code for await (item in publisher)} — iterates over emitted values
 *       with bounded backpressure (see {@link #DEFAULT_BATCH_SIZE}).</li>
 * </ul>
 * <p>
 * Conformance:
 * <ul>
 *   <li>Reactive Streams §2.13: {@code onNext(null)} is treated as a protocol
 *       violation and surfaced as a {@link NullPointerException}.</li>
 *   <li>A second {@code onSubscribe} after the first is cancelled.</li>
 *   <li>Signals after a terminal {@code onError}/{@code onComplete} are ignored.</li>
 * </ul>
 * <p>
 * This adapter is registered as the lowest-priority built-in (after SPI-loaded
 * adapters) so framework-specific adapters (Reactor, RxJava) take precedence
 * for their concrete types.
 *
 * @since 6.0.0
 */
public final class FlowPublisherAdapter implements AwaitableAdapter {

    /**
     * Default request batch size for {@code for await} iteration. Chosen as a
     * compromise between throughput (larger = fewer {@code request()} calls)
     * and memory (larger = bigger in-flight buffer). Override per-call by
     * wrapping with a custom adapter if needed.
     */
    public static final int DEFAULT_BATCH_SIZE = 32;

    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Flow.Publisher.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsIterable(Class<?> type) {
        return Flow.Publisher.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        if (!(source instanceof Flow.Publisher<?>)) {
            throw new IllegalArgumentException("Cannot convert to Awaitable: " + source.getClass());
        }
        return (Awaitable<T>) publisherToAwaitable((Flow.Publisher<Object>) source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Iterable<T> toIterable(Object source) {
        if (!(source instanceof Flow.Publisher<?>)) {
            throw new IllegalArgumentException("Cannot convert to Iterable: " + source.getClass());
        }
        return (Iterable<T>) publisherToIterable((Flow.Publisher<Object>) source, DEFAULT_BATCH_SIZE);
    }

    // ---- single-value: first onNext wins ---------------------------------

    private static <T> Awaitable<T> publisherToAwaitable(Flow.Publisher<T> publisher) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();
        publisher.subscribe(new Flow.Subscriber<T>() {
            @Override
            public void onSubscribe(Flow.Subscription s) {
                if (!subRef.compareAndSet(null, s)) {
                    s.cancel(); // duplicate onSubscribe — cancel the second
                    return;
                }
                s.request(1);
            }

            @Override
            public void onNext(T item) {
                if (cf.isDone()) return;
                if (item == null) {
                    cf.completeExceptionally(new NullPointerException(
                            "Flow.Publisher onNext received null (Reactive Streams §2.13)"));
                } else {
                    cf.complete(item);
                }
                Flow.Subscription s = subRef.getAndSet(null);
                if (s != null) s.cancel();
            }

            @Override
            public void onError(Throwable t) {
                if (cf.isDone()) return;
                cf.completeExceptionally(t);
                subRef.set(null);
            }

            @Override
            public void onComplete() {
                if (cf.isDone()) return;
                cf.complete(null); // empty publisher → null result
                subRef.set(null);
            }
        });
        return GroovyPromise.of(cf);
    }

    // ---- multi-value: blocking iterable with bounded backpressure --------

    private static <T> Iterable<T> publisherToIterable(Flow.Publisher<T> publisher, int batchSize) {
        return new PublisherBlockingIterable<>(publisher, batchSize);
    }

    /**
     * Blocking {@link Iterable} backed by a {@link Flow.Publisher} with bounded
     * backpressure. Implements {@link AutoCloseable} so {@code for await}
     * cleanup (via {@code AsyncSupport.closeIterable}) cancels the subscription
     * on early break.
     */
    private static final class PublisherBlockingIterable<T> implements Iterable<T>, AutoCloseable {
        private final Flow.Publisher<T> publisher;
        private final int batchSize;
        private final AtomicReference<QueueSubscriber<T>> active = new AtomicReference<>();

        PublisherBlockingIterable(Flow.Publisher<T> publisher, int batchSize) {
            this.publisher = publisher;
            this.batchSize = Math.max(1, batchSize);
        }

        @Override
        public Iterator<T> iterator() {
            QueueSubscriber<T> sub = new QueueSubscriber<>(batchSize);
            if (!active.compareAndSet(null, sub)) {
                throw new IllegalStateException(
                        "PublisherBlockingIterable is single-use; iterator() was already called");
            }
            publisher.subscribe(sub);
            return sub;
        }

        @Override
        public void close() {
            QueueSubscriber<T> sub = active.get();
            if (sub != null) sub.cancel();
        }
    }

    /**
     * A {@link Flow.Subscriber} that buffers signals into a blocking queue and
     * exposes them as an {@link Iterator}. Demand is replenished in halves of
     * the batch size to overlap consumption with production.
     */
    private static final class QueueSubscriber<T> implements Flow.Subscriber<T>, Iterator<T> {
        private static final Object COMPLETE = new Object();
        private static final Object ERROR = new Object();

        private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        private final int batchSize;
        private final int replenishThreshold;
        private final AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();
        private final AtomicBoolean cancelled = new AtomicBoolean();

        private Object next;
        private boolean hasNext;
        private boolean terminated;
        private Throwable error;
        private int consumedSinceRequest;

        QueueSubscriber(int batchSize) {
            this.batchSize = batchSize;
            this.replenishThreshold = Math.max(1, batchSize / 2);
        }

        @Override
        public void onSubscribe(Flow.Subscription s) {
            if (!subRef.compareAndSet(null, s)) {
                s.cancel();
                return;
            }
            // Handle cancel-before-subscribe race: cancel() may have already
            // run and enqueued the sentinel; dispose of this late subscription.
            if (cancelled.get() && subRef.compareAndSet(s, null)) {
                s.cancel();
                return;
            }
            s.request(batchSize);
        }

        @Override
        public void onNext(T item) {
            if (cancelled.get()) return;
            if (item == null) {
                onError(new NullPointerException(
                        "Flow.Publisher onNext received null (Reactive Streams §2.13)"));
                return;
            }
            queue.offer(item);
        }

        @Override
        public void onError(Throwable t) {
            if (cancelled.get()) return;
            this.error = t;
            queue.offer(ERROR);
            subRef.set(null);
        }

        @Override
        public void onComplete() {
            if (cancelled.get()) return;
            queue.offer(COMPLETE);
            subRef.set(null);
        }

        void cancel() {
            if (!cancelled.compareAndSet(false, true)) return;
            try {
                Flow.Subscription s = subRef.getAndSet(null);
                if (s != null) s.cancel();
            } finally {
                // Wake any thread blocked in queue.take() inside hasNext().
                // The queue's happens-before also publishes the cancelled flag.
                queue.offer(COMPLETE);
            }
        }

        @Override
        public boolean hasNext() {
            if (hasNext) return true;
            if (terminated) return false;
            try {
                Object item = queue.take();
                if (item == COMPLETE) {
                    terminated = true;
                    return false;
                }
                if (item == ERROR) {
                    terminated = true;
                    if (error instanceof RuntimeException re) throw re;
                    if (error instanceof Error err) throw err;
                    throw new RuntimeException(error);
                }
                next = item;
                hasNext = true;
                replenishIfNeeded();
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancel();
                return false;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T value = (T) next;
            next = null;
            hasNext = false;
            return value;
        }

        private void replenishIfNeeded() {
            consumedSinceRequest++;
            if (consumedSinceRequest >= replenishThreshold) {
                Flow.Subscription s = subRef.get();
                if (s != null) {
                    int toRequest = consumedSinceRequest;
                    consumedSinceRequest = 0;
                    s.request(toRequest);
                }
            }
        }
    }
}
