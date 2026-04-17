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
package groovy.concurrent;

import org.apache.groovy.runtime.async.AsyncSupport;
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A one-to-many broadcast channel where each value sent is delivered
 * to all subscribers.
 * <p>
 * Unlike {@link AsyncChannel} (point-to-point, each value consumed by
 * one receiver), a {@code BroadcastChannel} delivers every value to
 * every subscriber that has called {@link #subscribe()}.
 *
 * <pre>{@code
 * def broadcast = BroadcastChannel.create()
 * def sub1 = broadcast.subscribe()
 * def sub2 = broadcast.subscribe()
 *
 * async {
 *     broadcast.send('hello')
 *     broadcast.send('world')
 *     broadcast.close()
 * }
 *
 * // Both subscribers receive both values
 * for await (msg in sub1) { println "Sub1: $msg" }
 * for await (msg in sub2) { println "Sub2: $msg" }
 * }</pre>
 * <p>
 * Inspired by GPars' {@code DataflowBroadcast}.
 *
 * @param <T> the value type
 * @see AsyncChannel
 * @since 6.0.0
 */
public final class BroadcastChannel<T> {

    private final CopyOnWriteArrayList<AsyncChannel<T>> subscribers = new CopyOnWriteArrayList<>();
    private volatile boolean closed;

    private BroadcastChannel() { }

    /**
     * Creates a new broadcast channel.
     *
     * @param <T> the value type
     * @return a new BroadcastChannel
     */
    public static <T> BroadcastChannel<T> create() {
        return new BroadcastChannel<>();
    }

    /**
     * Creates a new subscriber channel. The returned {@link AsyncChannel}
     * will receive all values sent to this broadcast from this point forward.
     * Each subscriber is independent — values are buffered per subscriber.
     *
     * @return a new subscriber channel
     */
    public AsyncChannel<T> subscribe() {
        return subscribe(16);
    }

    /**
     * Creates a new subscriber channel with the specified buffer capacity.
     *
     * @param bufferSize the buffer capacity for this subscriber
     * @return a new subscriber channel
     */
    public AsyncChannel<T> subscribe(int bufferSize) {
        if (closed) throw new ChannelClosedException("BroadcastChannel is closed");
        AsyncChannel<T> sub = AsyncChannel.create(bufferSize);
        subscribers.add(sub);
        return sub;
    }

    /**
     * Sends a value to all current subscribers.
     *
     * @param value the value to broadcast
     * @return an Awaitable that completes when all subscribers have accepted the value
     * @throws ChannelClosedException if the broadcast channel is closed
     */
    public Awaitable<Void> send(T value) {
        Objects.requireNonNull(value, "value must not be null");
        if (closed) throw new ChannelClosedException("BroadcastChannel is closed");
        CompletableFuture<?>[] futures = subscribers.stream()
                .map(sub -> {
                    try {
                        return sub.send(value).toCompletableFuture();
                    } catch (ChannelClosedException e) {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .toArray(CompletableFuture[]::new);
        return GroovyPromise.of(CompletableFuture.allOf(futures));
    }

    /**
     * Closes this broadcast channel and all subscriber channels.
     */
    public void close() {
        closed = true;
        for (AsyncChannel<T> sub : subscribers) {
            sub.close();
        }
    }

    /**
     * Returns {@code true} if this broadcast channel has been closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the number of current subscribers.
     */
    public int getSubscriberCount() {
        return subscribers.size();
    }

    /**
     * Returns a {@link Flow.Publisher} view of this broadcast channel. Each
     * call to {@link Flow.Publisher#subscribe(Flow.Subscriber)} on the returned
     * publisher creates a new {@link AsyncChannel} subscriber under the hood,
     * draining values to the downstream subscriber according to its requested
     * demand.
     * <p>
     * Semantics:
     * <ul>
     *   <li>Cold per-subscribe binding: each subscription starts seeing values
     *       from the moment it subscribes (consistent with
     *       {@link #subscribe()}).</li>
     *   <li>Backpressure: respects {@code request(n)}; the worker blocks the
     *       broadcast send when no demand exists (sender-side backpressure).</li>
     *   <li>Cancellation: closes this subscriber's channel and removes it
     *       from the broadcast's subscriber set.</li>
     *   <li>Completion: signals {@code onComplete} when the broadcast channel
     *       is closed and the per-subscriber buffer drained.</li>
     * </ul>
     * <p>
     * <b>Backpressure policy (important).</b> This bridge uses lossless,
     * sender-gated backpressure: {@link #send(Object)} awaits delivery to
     * every live subscriber, and each per-subscriber channel has a bounded
     * buffer (default 16). A subscriber that never calls {@code request(n)},
     * or that requests slowly, will fill its buffer; once full, the
     * subscriber's channel suspends its backing {@code send}, which in turn
     * stalls {@code BroadcastChannel.send(...)} for <em>all</em>
     * subscribers. In other words, the slowest subscriber controls producer
     * throughput.
     * <p>
     * This is intentional and matches the point-to-point semantics of
     * {@link #subscribe()}: values are neither dropped nor reordered. If
     * you need decoupled per-subscriber policies (drop-newest, drop-oldest,
     * latest-only, or unbounded buffering), wrap the publisher with a
     * Reactive Streams operator of your choice, or use a subscriber that
     * drains promptly with {@code request(Long.MAX_VALUE)}.
     *
     * @return a {@code Flow.Publisher} backed by per-subscriber channels
     * @since 6.0.0
     */
    public Flow.Publisher<T> asPublisher() {
        return new BroadcastFlowPublisher();
    }

    private final class BroadcastFlowPublisher implements Flow.Publisher<T> {
        @Override
        public void subscribe(Flow.Subscriber<? super T> downstream) {
            Objects.requireNonNull(downstream, "subscriber must not be null");
            AsyncChannel<T> channel;
            try {
                channel = BroadcastChannel.this.subscribe();
            } catch (ChannelClosedException e) {
                downstream.onSubscribe(NOOP_SUBSCRIPTION);
                downstream.onError(e);
                return;
            }
            new BroadcastFlowSubscription<>(BroadcastChannel.this, channel, downstream).start();
        }
    }

    private static final Flow.Subscription NOOP_SUBSCRIPTION = new Flow.Subscription() {
        @Override public void request(long n) { }
        @Override public void cancel() { }
    };

    /**
     * Per-subscriber bridge that converts a backing {@link AsyncChannel} into
     * a Reactive Streams subscription with demand tracking.
     */
    private static final class BroadcastFlowSubscription<T> implements Flow.Subscription {
        private final BroadcastChannel<T> owner;
        private final AsyncChannel<T> channel;
        private final Flow.Subscriber<? super T> subscriber;
        private final AtomicLong demand = new AtomicLong();
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicReference<Throwable> terminalError = new AtomicReference<>();
        private final Object lock = new Object();

        BroadcastFlowSubscription(BroadcastChannel<T> owner, AsyncChannel<T> channel, Flow.Subscriber<? super T> subscriber) {
            this.owner = owner;
            this.channel = channel;
            this.subscriber = subscriber;
        }

        void start() {
            subscriber.onSubscribe(this);
            AsyncSupport.getExecutor().execute(this::drain);
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                // Reactive Streams §3.9 violation. Route the terminal error
                // through the drain thread (§1.3 requires onNext / onError /
                // onComplete signals to be serialised); calling
                // subscriber.onError here would race a concurrent onNext.
                terminalError.compareAndSet(null, new IllegalArgumentException(
                        "Reactive Streams §3.9: request must be positive, got " + n));
                synchronized (lock) { lock.notifyAll(); }
                return;
            }
            long prev, next;
            do {
                prev = demand.get();
                next = prev + n;
                if (next < 0) next = Long.MAX_VALUE; // saturate on overflow
            } while (!demand.compareAndSet(prev, next));
            synchronized (lock) { lock.notifyAll(); }
        }

        @Override
        public void cancel() {
            if (!cancelled.compareAndSet(false, true)) return;
            owner.subscribers.remove(channel);
            channel.close();
            synchronized (lock) { lock.notifyAll(); }
        }

        private void drain() {
            try {
                while (!cancelled.get()) {
                    // Caller injected a terminal error via request(n<=0) — emit and stop.
                    Throwable terminal = terminalError.get();
                    if (terminal != null) {
                        subscriber.onError(terminal);
                        cancel();
                        return;
                    }
                    // RS §1.7: terminal completion must not depend on demand.
                    // If the upstream channel has closed and drained, emit
                    // onComplete even when demand == 0.
                    if (channel.isClosed() && channel.getBufferedSize() == 0) {
                        subscriber.onComplete();
                        cancel();
                        return;
                    }
                    if (demand.get() == 0) {
                        synchronized (lock) {
                            if (demand.get() == 0 && !cancelled.get()
                                    && terminalError.get() == null
                                    && !(channel.isClosed() && channel.getBufferedSize() == 0)) {
                                // Bounded wait so we periodically re-check upstream
                                // close even if no request(n) ever arrives.
                                lock.wait(100L);
                            }
                        }
                        continue;
                    }
                    T item;
                    try {
                        item = AsyncSupport.await(channel.receive());
                    } catch (ChannelClosedException e) {
                        if (!cancelled.get()) {
                            subscriber.onComplete();
                            cancel();
                        }
                        return;
                    }
                    if (cancelled.get()) return;
                    subscriber.onNext(item);
                    long current = demand.get();
                    if (current != Long.MAX_VALUE) demand.decrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!cancelled.get()) {
                    subscriber.onError(e);
                    cancel();
                }
            } catch (Throwable t) {
                if (!cancelled.get()) {
                    subscriber.onError(t);
                    cancel();
                }
            }
        }
    }
}
