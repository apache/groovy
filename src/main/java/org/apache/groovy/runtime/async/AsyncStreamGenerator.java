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

import java.util.concurrent.CancellationException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A producer/consumer implementation of {@link groovy.concurrent.AsyncStream AsyncStream}
 * backed by a {@link SynchronousQueue}, used by {@code async} methods that
 * contain {@code yield return} statements.
 * <p>
 * Extends {@link AbstractAsyncStream} which provides the common
 * {@link #moveNext()}/{@link #getCurrent()}/{@link #close()} template.
 * This class adds the producer-side API ({@link #yield}, {@link #complete},
 * {@link #error}) and overrides the following hooks:
 *
 * <ul>
 *   <li>{@link #beforeTake()} — enforces single-consumer semantics via
 *       {@link #consumerThread} CAS + double-check of the {@link #closed} flag</li>
 *   <li>{@link #afterMoveNext()} — unregisters the consumer thread</li>
 *   <li>{@link #onMoveNextInterrupted(InterruptedException)} — returns
 *       {@code MOVE_NEXT_FALSE} if the stream was already closed (cooperative
 *       cancellation), otherwise throws {@link CancellationException}</li>
 *   <li>{@link #onClose()} — interrupts both producer and consumer threads</li>
 * </ul>
 *
 * <h2>Back-pressure</h2>
 * Uses a {@link SynchronousQueue} to provide natural back-pressure:
 * the producer thread blocks at each {@code yield return} until the
 * consumer has consumed the previous element, providing natural one-at-a-time
 * delivery semantics.
 *
 * <h2>Cooperative cancellation via thread tracking</h2>
 * The {@link #producerThread} and {@link #consumerThread} fields track the
 * threads currently blocked inside {@link SynchronousQueue#put} (producer)
 * and {@link SynchronousQueue#take} (consumer).  This tracking is essential
 * because {@code SynchronousQueue} operations block <em>indefinitely</em>
 * when the counterpart is absent:
 * <ul>
 *   <li>If the consumer exits the {@code for await} loop early (via
 *       {@code break}, {@code return}, or an exception), the producer may
 *       still be blocked in {@code queue.put()}.  Without a reference to the
 *       producer thread, there is no way to unblock it, resulting in a
 *       <b>permanent thread leak</b>.</li>
 *   <li>Conversely, if the producer finishes but the consumer is still
 *       blocked in {@code queue.take()} (e.g. an external cancellation
 *       closed the stream), the consumer thread would also leak.</li>
 * </ul>
 * When {@link #close()} is called (typically from the compiler-generated
 * {@code finally} block of a {@code for await} loop), it atomically sets the
 * {@link #closed} flag and then interrupts both the producer and consumer
 * threads (if any), causing their blocking {@code SynchronousQueue} operations
 * to throw {@link InterruptedException}.  The interrupted methods detect the
 * closed state and exit gracefully — the producer's {@link #yield} throws
 * {@link java.util.concurrent.CancellationException}, while the consumer's
 * {@link #moveNext()} returns {@code Awaitable.of(false)}.
 * <p>
 * A <em>double-check</em> pattern in {@link #moveNext()} closes a TOCTOU race
 * window: the {@link #closed} flag is re-checked <em>after</em> registering the
 * consumer thread, ensuring that an externally-triggered {@code close()} cannot
 * slip between the initial check and the registration, which would otherwise
 * leave the consumer blocked forever in {@code queue.take()}.
 * <p>
 * The {@link #attachProducer}/{@link #detachProducer} lifecycle methods are
 * called by the async runtime (in {@link AsyncSupport}) to register and
 * unregister the producer thread.  The consumer thread is tracked
 * automatically inside {@link #moveNext()}.
 * <p>
 * This class is an internal implementation detail and should not be referenced
 * directly by user code.
 *
 * @param <T> the element type
 * @since 6.0.0
 */
public class AsyncStreamGenerator<T> extends AbstractAsyncStream<T> {

    private final AtomicReference<Thread> producerThread = new AtomicReference<>();
    private final AtomicReference<Thread> consumerThread = new AtomicReference<>();

    public AsyncStreamGenerator() {
        super(new SynchronousQueue<>());
    }

    /**
     * Registers the given thread as the producer for this stream.
     * Called by the async runtime immediately after the producer
     * thread starts, <em>before</em> the generator body executes.
     * <p>
     * If the stream has already been {@linkplain #close() closed} by the
     * time this method runs, the thread is immediately interrupted so
     * that the generator body can exit promptly.
     *
     * @param thread the producer thread to register
     */
    void attachProducer(Thread thread) {
        producerThread.set(thread);
        if (closed.get()) {
            thread.interrupt();
        }
    }

    /**
     * Unregisters the given thread as the producer for this stream.
     * Called from a {@code finally} block after the generator body
     * completes (normally or exceptionally).
     *
     * @param thread the producer thread to unregister
     */
    void detachProducer(Thread thread) {
        producerThread.compareAndSet(thread, null);
    }

    /**
     * Produces the next element. Called from the generator body when
     * a {@code yield return expr} statement is executed. Blocks until
     * the consumer is ready.
     */
    public void yield(Object value) {
        if (closed.get()) {
            throw streamClosed(null);
        }
        try {
            queue.put(new ValueSignal(value));
        } catch (InterruptedException e) {
            if (closed.get()) {
                throw streamClosed(e);
            }
            Thread.currentThread().interrupt();
            throw newCancellationException("Interrupted during yield", e);
        }
    }

    /**
     * Signals that the generator has completed (no more elements).
     * <p>
     * If the blocking {@code queue.put()} is interrupted, a best-effort
     * non-blocking {@code queue.offer()} is attempted.  If that also fails
     * (no consumer is currently blocked in {@code take()}), the stream is
     * force-closed to prevent the consumer from blocking indefinitely on a
     * subsequent {@link #moveNext()} call.  This defensive close ensures no
     * thread leak occurs even under unexpected interrupt timing.
     */
    public void complete() {
        putTerminalSignal(COMPLETE);
    }

    /**
     * Signals that the generator failed with an exception.
     * <p>
     * If the blocking {@code queue.put()} is interrupted, a best-effort
     * non-blocking {@code queue.offer()} is attempted.  If that also fails,
     * the stream is force-closed (same rationale as {@link #complete()}).
     * The original error is not propagated to the consumer in this edge case;
     * instead the consumer sees a clean stream closure — this is acceptable
     * because the interrupt itself indicates an external cancellation.
     */
    public void error(Throwable t) {
        putTerminalSignal(new ErrorSignal(t != null ? t : new NullPointerException("null error in generator")));
    }

    /**
     * Shared logic for {@link #complete()} and {@link #error(Throwable)}:
     * attempts a blocking {@code put()}, falling back to non-blocking
     * {@code offer()} on interrupt, and force-closing if both fail.
     */
    private void putTerminalSignal(Object signal) {
        if (closed.get()) {
            return;
        }
        try {
            queue.put(signal);
        } catch (InterruptedException e) {
            if (!closed.get()) {
                Thread.currentThread().interrupt();
                if (!queue.offer(signal)) {
                    close();
                }
            }
        }
    }

    // ---- Template method overrides ----

    /**
     * Enforces single-consumer semantics via CAS on {@link #consumerThread},
     * with a double-check of the {@link #closed} flag to close the TOCTOU
     * race window (see class-level javadoc).
     */
    @Override
    protected Awaitable<Boolean> beforeTake() {
        if (closed.get()) {
            return MOVE_NEXT_FALSE;
        }
        Thread ct = Thread.currentThread();
        if (!consumerThread.compareAndSet(null, ct)) {
            Thread existing = consumerThread.get();
            if (existing != ct) {
                throw new IllegalStateException(
                        "AsyncStream does not support concurrent consumers. "
                        + "Current consumer: " + existing);
            }
        }
        // Double-check: if close() raced between the first closed check and
        // consumerThread CAS, the consumer reference was not yet visible to
        // close(), so no interrupt was delivered.
        if (closed.get()) {
            consumerThread.compareAndSet(ct, null);
            return MOVE_NEXT_FALSE;
        }
        return null;
    }

    /**
     * Unregisters the consumer thread after every {@link #moveNext()} attempt.
     */
    @Override
    protected void afterMoveNext() {
        consumerThread.compareAndSet(Thread.currentThread(), null);
    }

    /**
     * If the stream was already closed (cooperative cancellation), returns
     * {@code MOVE_NEXT_FALSE}; otherwise restores the interrupt flag and
     * throws {@link CancellationException}.
     */
    @Override
    protected Awaitable<Boolean> onMoveNextInterrupted(InterruptedException e) {
        if (closed.get()) {
            return MOVE_NEXT_FALSE;
        }
        Thread.currentThread().interrupt();
        throw newCancellationException("Interrupted during moveNext", e);
    }

    /**
     * Interrupts both producer and consumer threads (if any) to unblock
     * pending {@link SynchronousQueue} operations.  A thread is never
     * self-interrupted.
     */
    @Override
    protected void onClose() {
        Thread producer = producerThread.getAndSet(null);
        if (producer != null && producer != Thread.currentThread()) {
            producer.interrupt();
        }
        Thread consumer = consumerThread.getAndSet(null);
        if (consumer != null && consumer != Thread.currentThread()) {
            consumer.interrupt();
        }
    }

    private static CancellationException streamClosed(InterruptedException cause) {
        CancellationException ce = new CancellationException("Async stream was closed");
        if (cause != null) {
            ce.initCause(cause);
        }
        return ce;
    }
}
