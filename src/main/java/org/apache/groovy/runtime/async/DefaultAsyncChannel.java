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

import groovy.concurrent.AsyncChannel;
import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.concurrent.ChannelClosedException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default lock-based implementation of the {@link AsyncChannel} interface.
 *
 * <p>This implementation uses a {@link ReentrantLock} to coordinate access
 * to the internal buffer and the waiting-sender / waiting-receiver queues.
 * All operations are non-blocking from the caller's perspective: they
 * return an {@link Awaitable} immediately, and the underlying
 * {@link CompletableFuture} is completed asynchronously when matching
 * counterparts arrive.
 *
 * <h2>Internal Data Structures</h2>
 * <ul>
 *   <li><b>buffer</b> — an {@link ArrayDeque} of capacity {@code capacity}
 *       that holds values sent but not yet received.  For unbuffered
 *       channels (capacity {@code 0}) this deque is always empty.</li>
 *   <li><b>waitingSenders</b> — FIFO queue of senders whose values could
 *       not be delivered or buffered at send time.</li>
 *   <li><b>waitingReceivers</b> — FIFO queue of receivers for which no
 *       value was available at receive time.</li>
 * </ul>
 *
 * <h2>Cancellation Cleanup</h2>
 * <p>When a pending send or receive is cancelled or completed exceptionally
 * (e.g., via {@link Awaitable#orTimeout}), a cleanup callback removes the
 * stale entry from the appropriate queue.  This prevents memory leaks in
 * long-lived channels.
 *
 * <h2>Thread Safety</h2>
 * <p>All public methods are thread-safe.  The lock is held only for the
 * duration of queue/buffer manipulation — never across asynchronous
 * boundaries — ensuring that lock hold times remain bounded and short.
 *
 * @param <T> the payload type carried through the channel
 * @see AsyncChannel
 * @since 6.0.0
 */
public final class DefaultAsyncChannel<T> implements AsyncChannel<T> {

    private final ReentrantLock lock = new ReentrantLock();
    private final Deque<T> buffer = new ArrayDeque<>();
    private final Deque<PendingSend<T>> waitingSenders = new ArrayDeque<>();
    private final Deque<CompletableFuture<T>> waitingReceivers = new ArrayDeque<>();
    private final int capacity;

    private volatile boolean closed;

    /**
     * Creates an unbuffered (rendezvous) channel with a capacity of {@code 0}.
     *
     * <p>In this mode every {@link #send(Object)} suspends until a matching
     * {@link #receive()} arrives, providing the strongest producer–consumer
     * synchronization guarantee.
     */
    public DefaultAsyncChannel() {
        this(0);
    }

    /**
     * Creates a channel with the specified buffer capacity.
     *
     * @param capacity the maximum number of values that can be buffered;
     *                 {@code 0} for an unbuffered (rendezvous) channel
     * @throws IllegalArgumentException if {@code capacity} is negative
     */
    public DefaultAsyncChannel(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("channel capacity must not be negative: " + capacity);
        }
        this.capacity = capacity;
    }

    // ---- Query Methods --------------------------------------------------

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getBufferedSize() {
        lock.lock();
        try {
            return buffer.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    // ---- Core Operations ------------------------------------------------

    @Override
    public Awaitable<Void> send(T value) {
        Objects.requireNonNull(value, "channel does not support null values");

        CompletableFuture<Void> completion = new CompletableFuture<>();
        PendingSend<T> pending = new PendingSend<>(value, completion);
        boolean queued;

        lock.lock();
        try {
            if (closed) {
                completion.completeExceptionally(closedForSend());
                queued = false;
            } else if (deliverToWaitingReceiver(value)) {
                completion.complete(null);
                queued = false;
            } else if (buffer.size() < capacity) {
                buffer.addLast(value);
                completion.complete(null);
                queued = false;
            } else {
                waitingSenders.addLast(pending);
                queued = true;
            }
        } finally {
            lock.unlock();
        }

        // Register cleanup for cancellation/timeout of a queued send.
        // The callback is registered unconditionally for queued sends to
        // avoid a race where an external cancel between lock release and
        // callback registration leaves a stale entry in the queue.
        if (queued) {
            completion.whenComplete((ignored, error) -> {
                if (error != null || completion.isCancelled()) {
                    removePendingSender(pending);
                }
            });
        }

        return GroovyPromise.of(completion);
    }

    @Override
    public Awaitable<T> receive() {
        CompletableFuture<T> completion = new CompletableFuture<>();
        boolean queued;

        lock.lock();
        try {
            T buffered = pollBuffer();
            if (buffered != null) {
                completion.complete(buffered);
                queued = false;
            } else {
                PendingSend<T> sender = pollPendingSender();
                if (sender != null) {
                    sender.completion.complete(null);
                    completion.complete(sender.value);
                    queued = false;
                } else if (closed) {
                    completion.completeExceptionally(closedForReceive());
                    queued = false;
                } else {
                    waitingReceivers.addLast(completion);
                    queued = true;
                }
            }
        } finally {
            lock.unlock();
        }

        // Register cleanup for cancellation/timeout of a queued receive.
        // Same rationale as in send(): unconditional registration avoids
        // a race between lock release and external cancellation.
        if (queued) {
            completion.whenComplete((ignored, error) -> {
                if (error != null || completion.isCancelled()) {
                    removePendingReceiver(completion);
                }
            });
        }

        return GroovyPromise.of(completion);
    }

    @Override
    public boolean close() {
        lock.lock();
        try {
            if (closed) {
                return false;
            }
            closed = true;

            drainBufferToReceivers();

            while (!waitingReceivers.isEmpty()) {
                waitingReceivers.removeFirst().completeExceptionally(closedForReceive());
            }

            while (!waitingSenders.isEmpty()) {
                waitingSenders.removeFirst().completion.completeExceptionally(closedForSend());
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AsyncStream<T> asStream() {
        return new ChannelAsyncStream<>(this);
    }

    /**
     * Returns a diagnostic string showing the channel's current state.
     *
     * <p>The output includes capacity, buffered element count, queue sizes,
     * and closed status.  Useful for logging and debugging.
     *
     * @return a human-readable representation of this channel
     */
    @Override
    public String toString() {
        lock.lock();
        try {
            return "AsyncChannel{capacity=" + capacity
                    + ", buffered=" + buffer.size()
                    + ", waitingSenders=" + waitingSenders.size()
                    + ", waitingReceivers=" + waitingReceivers.size()
                    + ", closed=" + closed
                    + '}';
        } finally {
            lock.unlock();
        }
    }

    // ---- Internal Helpers -----------------------------------------------

    /**
     * Attempts to deliver a value directly to the first viable waiting
     * receiver.  Skips receivers that have already been completed (e.g.,
     * due to cancellation or timeout).
     */
    private boolean deliverToWaitingReceiver(T value) {
        while (!waitingReceivers.isEmpty()) {
            CompletableFuture<T> receiver = waitingReceivers.removeFirst();
            if (receiver.complete(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delivers buffered values to as many waiting receivers as possible.
     * Called during {@link #close()} to ensure buffered data is not lost.
     */
    private void drainBufferToReceivers() {
        while (!waitingReceivers.isEmpty() && !buffer.isEmpty()) {
            CompletableFuture<T> receiver = waitingReceivers.removeFirst();
            if (receiver.complete(buffer.peekFirst())) {
                buffer.removeFirst();
            }
        }
    }

    /**
     * Removes and returns the first buffered value, refilling the freed
     * slot from waiting senders if possible.
     */
    private T pollBuffer() {
        if (buffer.isEmpty()) {
            return null;
        }
        T value = buffer.removeFirst();
        refillBufferFromWaitingSenders();
        return value;
    }

    /**
     * Admits waiting senders into freed buffer slots up to the capacity.
     */
    private void refillBufferFromWaitingSenders() {
        while (buffer.size() < capacity) {
            PendingSend<T> sender = pollPendingSender();
            if (sender == null) {
                return;
            }
            buffer.addLast(sender.value);
            sender.completion.complete(null);
        }
    }

    /**
     * Returns the first pending sender whose future has not been completed
     * (i.e., not yet cancelled or timed out), or {@code null} if none.
     */
    private PendingSend<T> pollPendingSender() {
        while (!waitingSenders.isEmpty()) {
            PendingSend<T> sender = waitingSenders.removeFirst();
            if (!sender.completion.isDone()) {
                return sender;
            }
        }
        return null;
    }

    private void removePendingSender(PendingSend<T> sender) {
        lock.lock();
        try {
            waitingSenders.remove(sender);
        } finally {
            lock.unlock();
        }
    }

    private void removePendingReceiver(CompletableFuture<T> receiver) {
        lock.lock();
        try {
            waitingReceivers.remove(receiver);
        } finally {
            lock.unlock();
        }
    }

    private static ChannelClosedException closedForSend() {
        return new ChannelClosedException("channel is closed for send");
    }

    private static ChannelClosedException closedForReceive() {
        return new ChannelClosedException("channel is closed");
    }

    // ---- Inner Classes --------------------------------------------------

    /**
     * Holds a value and its associated completion future for a sender
     * that is waiting for buffer space or a matching receiver.
     */
    private record PendingSend<T>(T value, CompletableFuture<Void> completion) {}

    /**
     * Read-only {@link AsyncStream} view over an {@link AsyncChannel}.
     *
     * <p>Each {@link #moveNext()} call delegates to
     * {@link AsyncChannel#receive()}; a {@link ChannelClosedException} is
     * translated to end-of-stream ({@code false}).  The stream does not
     * own the channel, so its {@link #close()} is a no-op.
     *
     * @param <T> the element type
     */
    private static final class ChannelAsyncStream<T> implements AsyncStream<T> {
        private final AsyncChannel<T> channel;
        private volatile T current;

        ChannelAsyncStream(AsyncChannel<T> channel) {
            this.channel = channel;
        }

        @Override
        public Awaitable<Boolean> moveNext() {
            return channel.receive().handle((value, error) -> {
                if (error != null) {
                    if (error instanceof ChannelClosedException) {
                        return false;
                    }
                    if (error instanceof RuntimeException re) throw re;
                    if (error instanceof Error e) throw e;
                    throw new RuntimeException(error);
                }
                current = value;
                return true;
            });
        }

        @Override
        public T getCurrent() {
            return current;
        }
    }
}
