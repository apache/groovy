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
import groovy.transform.Internal;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default lock-based implementation of {@link AsyncChannel}.
 * <p>
 * Uses a {@link ReentrantLock} to coordinate access to the internal buffer
 * and the waiting-sender/waiting-receiver queues. All operations return
 * {@link Awaitable} immediately; the underlying {@link CompletableFuture}
 * is completed asynchronously when matching counterparts arrive.
 * <p>
 * Every operation is arbitrated by a {@link SelectClaim}: the branches of a
 * {@link groovy.concurrent.ChannelSelect} share their select's claim, and a
 * plain operation carries a private one. The claim is the sole owner of a
 * parked operation's fate — a delivery commits it before it completes the
 * future, and cancellation must commit it before it may touch the future —
 * so a select over sends and receives on several channels commits exactly
 * one transfer, and a losing branch never disturbs its channel.
 * <p>
 * Both waiting queues are concurrent deques so that a cancelled operation
 * can withdraw itself without taking the channel lock: a
 * {@link groovy.concurrent.ChannelSelect} withdraws its losing branches from
 * inside the winning channel's delivery, and taking a second channel's lock
 * there could deadlock against a select completing on that channel.
 *
 * @param <T> the payload type
 * @see AsyncChannel
 * @since 6.0.0
 */
public final class DefaultAsyncChannel<T> implements AsyncChannel<T> {

    private final ReentrantLock lock = new ReentrantLock();
    private final Deque<T> buffer = new ArrayDeque<>();
    private final Deque<PendingSend<T>> waitingSenders = new ConcurrentLinkedDeque<>();
    private final Deque<PendingOp<T>> waitingReceivers = new ConcurrentLinkedDeque<>();
    private final int capacity;
    private volatile boolean closed;
    /** run once, outside the lock, by the call that closes the channel; {@code null} for none */
    private volatile Runnable onClose;

    public DefaultAsyncChannel() {
        this(0);
    }

    public DefaultAsyncChannel(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("channel capacity must not be negative: " + capacity);
        }
        this.capacity = capacity;
    }

    /**
     * Creates a timer channel: a capacity-1 channel that delivers one value,
     * the {@link Instant} at which it fired, once {@code delay} has elapsed
     * from this call, and then closes. Closing the channel before it fires
     * cancels the timer, so a timer that is no longer wanted holds no
     * scheduler slot. A delay that is not positive has already elapsed, so
     * the channel is returned already holding its instant and closed: it is
     * ready to a receiver or a select at once, with no scheduler hop.
     * <p>
     * Implementation of {@link AsyncChannel#after(long)}; not part of the
     * channel contract.
     *
     * @param delay how long to wait before firing
     * @param unit  the unit of {@code delay}
     * @return the timer channel
     */
    @Internal
    public static DefaultAsyncChannel<Instant> after(long delay, TimeUnit unit) {
        Objects.requireNonNull(unit, "unit must not be null");
        DefaultAsyncChannel<Instant> timer = new DefaultAsyncChannel<>(1);
        if (delay <= 0) {
            timer.send(Instant.now());
            timer.close();
            return timer;
        }
        ScheduledFuture<?> firing = AsyncExecutors.getScheduler().schedule(() -> {
            // the only sender into a capacity-1 buffer: the send buffers or
            // delivers at once, never parks, and only fails if already closed
            timer.send(Instant.now());
            timer.close();
        }, delay, unit);
        timer.onClose = () -> firing.cancel(false);
        return timer;
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
        return send(value, null);
    }

    /**
     * Offers {@code value}, but only if {@code claim} commits to this branch
     * at the moment the channel could accept it: when a waiting receiver takes
     * it, or when buffer space holds it. A group of offers sharing one claim
     * (the branches of a {@link groovy.concurrent.ChannelSelect}) commits
     * exactly one between them; an offer whose claim was committed elsewhere
     * is retired without any effect on the channel — no buffered residue, no
     * lingering waiting sender.
     * <p>
     * Internal support for {@code ChannelSelect}; not part of the channel
     * contract.
     *
     * @param value the value offered for transfer
     * @param claim the claim shared by the competing offers
     * @return an Awaitable that completes when the send committed, or is
     *         cancelled if the claim was taken elsewhere first
     */
    @Internal
    public Awaitable<Void> sendIfUnclaimed(T value, SelectClaim claim) {
        Objects.requireNonNull(claim, "claim must not be null");
        return send(value, claim);
    }

    private Awaitable<Void> send(T value, SelectClaim claim) {
        Objects.requireNonNull(value, "channel does not support null values");

        PendingOp<Void> completion = new PendingOp<>(claim);
        boolean queued = false;

        lock.lock();
        try {
            if (closed) {
                completion.completeExceptionally(closedForSend());
            } else if (!resolveAgainstReceivers(completion, value)) {
                if (buffer.size() < capacity) {
                    if (completion.claim.tryCommit(completion)) {
                        buffer.addLast(value);
                        completion.complete(null);
                    } else {
                        completion.internalCancel();
                    }
                } else if (completion.claim.isCommitted()) {
                    completion.internalCancel();
                } else {
                    waitingSenders.addLast(new PendingSend<>(value, completion));
                    queued = true;
                }
            }
        } finally {
            lock.unlock();
        }

        if (queued) {
            completion.whenComplete((ignored, error) -> {
                if (error != null || completion.isCancelled()) {
                    // lock-free on purpose: may run while another channel's lock is held
                    waitingSenders.removeIf(pending -> pending.completion == completion);
                }
            });
        }

        return GroovyPromise.of(completion);
    }

    @Override
    public Awaitable<T> receive() {
        return receive(null);
    }

    /**
     * Receives the next value, but only if {@code claim} commits to this
     * branch at the moment this channel would hand the value over. The claim
     * is resolved under the channel lock immediately before the value is
     * dequeued, so a group of offers sharing one claim (the branches of a
     * {@link groovy.concurrent.ChannelSelect}) takes exactly one value between
     * them: a branch that loses the claim never touches its channel's contents
     * and is completed as cancelled.
     * <p>
     * Internal support for {@code ChannelSelect}; not part of the channel
     * contract.
     *
     * @param claim the claim shared by the competing offers
     * @return an Awaitable that yields the next value, or is cancelled if the
     *         claim was taken elsewhere first
     */
    @Internal
    public Awaitable<T> receiveIfUnclaimed(SelectClaim claim) {
        Objects.requireNonNull(claim, "claim must not be null");
        return receive(claim);
    }

    private Awaitable<T> receive(SelectClaim claim) {
        PendingOp<T> completion = new PendingOp<>(claim);
        boolean queued = false;

        lock.lock();
        try {
            if (!buffer.isEmpty()) {
                if (completion.claim.tryCommit(completion)) {
                    completion.complete(pollBuffer());
                } else {
                    completion.internalCancel();
                }
            } else if (!resolveAgainstSenders(completion)) {
                if (closed) {
                    completion.completeExceptionally(closedForReceive());
                } else if (completion.claim.isCommitted()) {
                    completion.internalCancel();
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
                    // lock-free on purpose: may run while another channel's lock is held
                    waitingReceivers.removeIf(pending -> pending == completion);
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

            for (PendingOp<T> receiver; (receiver = waitingReceivers.pollFirst()) != null; ) {
                receiver.completeExceptionally(closedForReceive());
            }
            for (PendingSend<T> sender; (sender = waitingSenders.pollFirst()) != null; ) {
                sender.completion.completeExceptionally(closedForSend());
            }
        } finally {
            lock.unlock();
        }

        Runnable hook = onClose;
        if (hook != null) hook.run();
        return true;
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

    // Hand-over discipline: resolve the claims first, then act. A pairing of
    // two parties reserves both claims (in id order, via SelectClaim.pendBoth),
    // commits both, and only then completes the futures; a single-party commit
    // happens only when the outcome is guaranteed under the channel lock. Once
    // an operation's claim is committed to it, its future cannot be cancelled
    // (the claim gates cancel), so a post-commit completion cannot fail. No
    // future is ever completed, and no lock ever taken, while a reservation is
    // held.

    /**
     * Tries to hand {@code value} to a waiting receiver, pairing the sender's
     * claim with each candidate's in turn.
     *
     * @return whether the send was resolved: delivered, or retired because its
     *         own claim was committed elsewhere; {@code false} when no viable
     *         receiver remains and the send must buffer or park
     */
    private boolean resolveAgainstReceivers(PendingOp<Void> completion, T value) {
        Object[] tokens = new Object[2];
        for (Iterator<PendingOp<T>> it = waitingReceivers.iterator(); it.hasNext(); ) {
            PendingOp<T> receiver = it.next();
            if (receiver.isDone()) {
                it.remove();
            } else if (receiver.claim == completion.claim) {
                // a sibling branch of the same select never pairs with it
            } else {
                switch (SelectClaim.pendBoth(completion.claim, receiver.claim, tokens)) {
                    case 0 -> {
                        completion.claim.commit(tokens[0], completion);
                        receiver.claim.commit(tokens[1], receiver);
                        it.remove();
                        receiver.complete(value);
                        completion.complete(null);
                        return true;
                    }
                    case 1 -> {
                        completion.internalCancel();
                        return true;
                    }
                    case 2 -> it.remove();
                }
            }
        }
        return false;
    }

    /**
     * Tries to take a value from a waiting sender, pairing the receiver's
     * claim with each candidate's in turn.
     *
     * @return whether the receive was resolved: a value taken, or the branch
     *         retired because its own claim was committed elsewhere;
     *         {@code false} when no viable sender remains
     */
    private boolean resolveAgainstSenders(PendingOp<T> completion) {
        Object[] tokens = new Object[2];
        for (Iterator<PendingSend<T>> it = waitingSenders.iterator(); it.hasNext(); ) {
            PendingSend<T> sender = it.next();
            if (sender.completion.isDone()) {
                it.remove();
            } else if (sender.completion.claim == completion.claim) {
                // a sibling branch of the same select never pairs with it
            } else {
                switch (SelectClaim.pendBoth(completion.claim, sender.completion.claim, tokens)) {
                    case 0 -> {
                        completion.claim.commit(tokens[0], completion);
                        sender.completion.claim.commit(tokens[1], sender.completion);
                        it.remove();
                        sender.completion.complete(null);
                        completion.complete(sender.value);
                        return true;
                    }
                    case 1 -> {
                        completion.internalCancel();
                        return true;
                    }
                    case 2 -> it.remove();
                }
            }
        }
        return false;
    }

    private void drainBufferToReceivers() {
        while (!buffer.isEmpty()) {
            PendingOp<T> receiver = pollCommittableReceiver();
            if (receiver == null) return;
            receiver.complete(buffer.removeFirst());
        }
    }

    private PendingOp<T> pollCommittableReceiver() {
        for (PendingOp<T> receiver; (receiver = waitingReceivers.pollFirst()) != null; ) {
            if (!receiver.isDone() && receiver.claim.tryCommit(receiver)) return receiver;
        }
        return null;
    }

    private T pollBuffer() {
        T value = buffer.removeFirst();
        refillBufferFromWaitingSenders();
        return value;
    }

    private void refillBufferFromWaitingSenders() {
        for (Iterator<PendingSend<T>> it = waitingSenders.iterator(); buffer.size() < capacity && it.hasNext(); ) {
            PendingSend<T> sender = it.next();
            it.remove();
            if (!sender.completion.isDone() && sender.completion.claim.tryCommit(sender.completion)) {
                buffer.addLast(sender.value);
                sender.completion.complete(null);
            }
        }
    }

    private static ChannelClosedException closedForSend() {
        return new ChannelClosedException("channel is closed for send");
    }

    private static ChannelClosedException closedForReceive() {
        return new ChannelClosedException("channel is closed");
    }

    /**
     * A channel operation's completion, arbitrated by its {@link SelectClaim}:
     * the claim owns the operation's fate, so cancellation must commit the
     * claim before it may touch the future, and a pairing that committed the
     * claim to this operation owns the completion outright. A plain operation
     * carries a private claim; the branches of one select share the select's.
     */
    private static final class PendingOp<V> extends CompletableFuture<V> {
        final SelectClaim claim;

        PendingOp(SelectClaim claim) {
            this.claim = claim != null ? claim : new SelectClaim();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return claim.allowCancel(this) && super.cancel(mayInterruptIfRunning);
        }

        /** retires the branch of a claim that was committed elsewhere */
        boolean internalCancel() {
            return super.cancel(false);
        }
    }

    /** A waiting sender: the offered value and its arbitrated completion. */
    private record PendingSend<T>(T value, PendingOp<Void> completion) {}
}
