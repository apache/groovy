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

import java.util.concurrent.CancellationException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A producer/consumer implementation of {@link AsyncStream} used by
 * {@code async} methods that contain {@code yield return} statements.
 * <p>
 * The producer (method body) runs on a separate thread and calls
 * {@link #yield(Object)} for each emitted element. The consumer
 * calls {@link #moveNext()}/{@link #getCurrent()} — typically via
 * a {@code for await} loop.
 *
 * <h2>Back-pressure</h2>
 * Uses a {@link SynchronousQueue} to provide natural back-pressure:
 * the producer thread blocks at each {@code yield return} until the
 * consumer has consumed the previous element (mirroring C#'s async
 * iterator suspension semantics).
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
public class AsyncStreamGenerator<T> implements AsyncStream<T> {

    private static final Object DONE = new Object();

    /**
     * Cached awaitables for the two common {@code moveNext()} outcomes.
     * Eliminates per-call object allocation on the hot path.
     */
    private static final Awaitable<Boolean> MOVE_NEXT_TRUE = Awaitable.of(Boolean.TRUE);
    private static final Awaitable<Boolean> MOVE_NEXT_FALSE = Awaitable.of(Boolean.FALSE);

    private final SynchronousQueue<Object> queue = new SynchronousQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicReference<Thread> producerThread = new AtomicReference<>();
    private final AtomicReference<Thread> consumerThread = new AtomicReference<>();
    private volatile T current;

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
            queue.put(new Item(value));
        } catch (InterruptedException e) {
            if (closed.get()) {
                throw streamClosed(e);
            }
            Thread.currentThread().interrupt();
            throw interrupted("Interrupted during yield", e);
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
        if (closed.get()) {
            return;
        }
        try {
            queue.put(DONE);
        } catch (InterruptedException e) {
            if (!closed.get()) {
                Thread.currentThread().interrupt();
                // Best-effort: non-blocking handoff to a waiting consumer.
                // If no consumer is waiting, offer() returns false and the DONE
                // signal is lost — force-close to unblock future moveNext() calls.
                if (!queue.offer(DONE)) {
                    close();
                }
            }
        }
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
        if (closed.get()) {
            return;
        }
        ErrorItem item = new ErrorItem(t != null ? t : new NullPointerException("null error in generator"));
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            if (!closed.get()) {
                Thread.currentThread().interrupt();
                if (!queue.offer(item)) {
                    close();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Blocks the calling (consumer) thread on the {@link SynchronousQueue} until
     * the producer offers the next element, a completion sentinel, or an error.
     * If the stream has been {@linkplain #close() closed}, returns
     * {@code Awaitable.of(false)} immediately without blocking.
     * <p>
     * The consumer thread is registered via {@code consumerThread} during the
     * blocking call so that {@link #close()} can interrupt it if needed.
     * <p>
     * <b>Single-consumer invariant</b>: only one thread may call
     * {@code moveNext()} at a time.  A {@code compareAndSet(null, current)}
     * guard enforces this — a concurrent second caller receives an
     * {@link IllegalStateException} immediately instead of silently corrupting
     * the producer/consumer handshake.
     * <p>
     * A <em>double-check</em> of the {@link #closed} flag is performed after
     * registration to close the TOCTOU race window: if {@code close()} executes
     * between the initial {@code closed.get()} check and the
     * {@code consumerThread} CAS, the consumer reference would not yet be
     * visible to {@code close()}, so no interrupt would be delivered, and
     * {@code queue.take()} would block indefinitely.  The re-check after
     * registration detects this case and returns immediately.
     *
     * @return an {@code Awaitable<Boolean>} that resolves to {@code true} if a
     *         new element is available via {@link #getCurrent()}, or {@code false}
     *         if the stream is exhausted or closed
     */
    @Override
    @SuppressWarnings("unchecked")
    public Awaitable<Boolean> moveNext() {
        if (closed.get()) {
            return MOVE_NEXT_FALSE;
        }
        // Enforce single-consumer semantics: only one thread may call moveNext()
        // at a time.  A concurrent second caller would overwrite consumerThread,
        // breaking close()'s interrupt targeting and causing data races on
        // queue.take().  CAS guards this invariant at the cost of one atomic op.
        Thread currentThread = Thread.currentThread();
        if (!consumerThread.compareAndSet(null, currentThread)) {
            Thread existing = consumerThread.get();
            if (existing != currentThread) {
                throw new IllegalStateException(
                        "AsyncStream does not support concurrent consumers. "
                        + "Current consumer: " + existing);
            }
        }
        // Double-check after registration: if close() raced between the first
        // closed check and consumerThread CAS, the consumer reference was not
        // yet visible to close(), so no interrupt was delivered.  Without this
        // re-check the consumer would block in queue.take() indefinitely.
        if (closed.get()) {
            consumerThread.compareAndSet(currentThread, null);
            return MOVE_NEXT_FALSE;
        }
        try {
            Object next = queue.take();
            if (next == DONE) {
                closed.set(true);
                return MOVE_NEXT_FALSE;
            }
            if (next instanceof ErrorItem ei) {
                closed.set(true);
                Throwable cause = ei.error;
                if (cause instanceof Error err) throw err;
                throw AsyncSupport.sneakyThrow(cause);
            }
            current = (T) ((Item) next).value;
            return MOVE_NEXT_TRUE;
        } catch (InterruptedException e) {
            if (closed.get()) {
                return MOVE_NEXT_FALSE;
            }
            Thread.currentThread().interrupt();
            throw interrupted("Interrupted during moveNext", e);
        } finally {
            consumerThread.compareAndSet(currentThread, null);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the most recently consumed element. The value is updated each time
     * {@link #moveNext()} returns {@code true}.
     *
     * @return the current element, or {@code null} before the first successful
     *         {@code moveNext()} call
     */
    @Override
    public T getCurrent() {
        return current;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Atomically marks this stream as closed and interrupts any producer or
     * consumer thread that is currently blocked on the {@link SynchronousQueue}.
     * The interrupted threads detect the {@link #closed} flag and exit
     * gracefully (see the class-level javadoc for details).
     * <p>
     * This method is idempotent: only the first invocation performs the
     * interrupt; subsequent calls are no-ops.  A thread calling {@code close()}
     * on itself (e.g. the consumer calling close inside a {@code for await}
     * body) is never self-interrupted.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        Thread producer = producerThread.getAndSet(null);
        if (producer != null && producer != Thread.currentThread()) {
            producer.interrupt();
        }
        Thread consumer = consumerThread.getAndSet(null);
        if (consumer != null && consumer != Thread.currentThread()) {
            consumer.interrupt();
        }
    }

    private static CancellationException interrupted(String message, InterruptedException cause) {
        CancellationException ce = new CancellationException(message);
        ce.initCause(cause);
        return ce;
    }

    private static CancellationException streamClosed(InterruptedException cause) {
        CancellationException ce = new CancellationException("Async stream was closed");
        if (cause != null) {
            ce.initCause(cause);
        }
        return ce;
    }

    // Wrapper to handle null values in the queue
    private record Item(Object value) { }
    private record ErrorItem(Throwable error) { }
}
