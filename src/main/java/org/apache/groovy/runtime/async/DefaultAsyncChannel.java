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
import groovy.concurrent.Awaitable;
import groovy.concurrent.ChannelClosedException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default lock-based implementation of {@link AsyncChannel}.
 * <p>
 * Uses a {@link ReentrantLock} to coordinate access to the internal buffer
 * and the waiting-sender/waiting-receiver queues. All operations return
 * {@link Awaitable} immediately; the underlying {@link CompletableFuture}
 * is completed asynchronously when matching counterparts arrive.
 *
 * @param <T> the payload type
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

    public DefaultAsyncChannel() {
        this(0);
    }

    public DefaultAsyncChannel(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("channel capacity must not be negative: " + capacity);
        }
        this.capacity = capacity;
    }

    // ---- Query ----------------------------------------------------------

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
            if (closed) return false;
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

    // ---- Iterable (for await / for loop) --------------------------------

    /**
     * Returns a blocking iterator that receives values until the channel
     * is closed and drained. Each {@code next()} call blocks until a value
     * is available. {@link ChannelClosedException} signals end-of-iteration.
     */
    @Override
    public Iterator<T> iterator() {
        return new ChannelIterator();
    }

    private final class ChannelIterator implements Iterator<T> {
        private T next;
        private boolean done;

        @Override
        public boolean hasNext() {
            if (done) return false;
            if (next != null) return true;
            try {
                next = AsyncSupport.await(receive());
                return true;
            } catch (ChannelClosedException e) {
                done = true;
                return false;
            }
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T value = next;
            next = null;
            return value;
        }
    }

    // ---- toString -------------------------------------------------------

    @Override
    public String toString() {
        lock.lock();
        try {
            return "AsyncChannel{capacity=" + capacity
                    + ", buffered=" + buffer.size()
                    + ", waitingSenders=" + waitingSenders.size()
                    + ", waitingReceivers=" + waitingReceivers.size()
                    + ", closed=" + closed + '}';
        } finally {
            lock.unlock();
        }
    }

    // ---- Internal -------------------------------------------------------

    private boolean deliverToWaitingReceiver(T value) {
        while (!waitingReceivers.isEmpty()) {
            CompletableFuture<T> receiver = waitingReceivers.removeFirst();
            if (receiver.complete(value)) return true;
        }
        return false;
    }

    private void drainBufferToReceivers() {
        while (!waitingReceivers.isEmpty() && !buffer.isEmpty()) {
            CompletableFuture<T> receiver = waitingReceivers.removeFirst();
            if (receiver.complete(buffer.peekFirst())) {
                buffer.removeFirst();
            }
        }
    }

    private T pollBuffer() {
        if (buffer.isEmpty()) return null;
        T value = buffer.removeFirst();
        refillBufferFromWaitingSenders();
        return value;
    }

    private void refillBufferFromWaitingSenders() {
        while (buffer.size() < capacity) {
            PendingSend<T> sender = pollPendingSender();
            if (sender == null) return;
            buffer.addLast(sender.value);
            sender.completion.complete(null);
        }
    }

    private PendingSend<T> pollPendingSender() {
        while (!waitingSenders.isEmpty()) {
            PendingSend<T> sender = waitingSenders.removeFirst();
            if (!sender.completion.isDone()) return sender;
        }
        return null;
    }

    private void removePendingSender(PendingSend<T> sender) {
        lock.lock();
        try { waitingSenders.remove(sender); } finally { lock.unlock(); }
    }

    private void removePendingReceiver(CompletableFuture<T> receiver) {
        lock.lock();
        try { waitingReceivers.remove(receiver); } finally { lock.unlock(); }
    }

    private static ChannelClosedException closedForSend() {
        return new ChannelClosedException("channel is closed for send");
    }

    private static ChannelClosedException closedForReceive() {
        return new ChannelClosedException("channel is closed");
    }

    private record PendingSend<T>(T value, CompletableFuture<Void> completion) {}
}
