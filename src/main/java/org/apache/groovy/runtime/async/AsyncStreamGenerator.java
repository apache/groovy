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
     * If interrupted, the completion signal is delivered on a best-effort basis.
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
                // Best-effort delivery: use non-blocking offer as fallback
                queue.offer(DONE);
            }
        }
    }

    /**
     * Signals that the generator failed with an exception.
     * If interrupted, the error signal is delivered on a best-effort basis.
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
                // Best-effort delivery: use non-blocking offer as fallback
                queue.offer(item);
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
     *
     * @return an {@code Awaitable<Boolean>} that resolves to {@code true} if a
     *         new element is available via {@link #getCurrent()}, or {@code false}
     *         if the stream is exhausted or closed
     */
    @Override
    @SuppressWarnings("unchecked")
    public Awaitable<Boolean> moveNext() {
        if (closed.get()) {
            return Awaitable.of(false);
        }
        Thread currentThread = Thread.currentThread();
        consumerThread.set(currentThread);
        try {
            Object next = queue.take();
            if (next == DONE) {
                closed.set(true);
                return Awaitable.of(false);
            }
            if (next instanceof ErrorItem ei) {
                closed.set(true);
                Throwable cause = ei.error;
                if (cause instanceof Error err) throw err;
                throw AsyncSupport.sneakyThrow(cause);
            }
            current = (T) ((Item) next).value;
            return Awaitable.of(true);
        } catch (InterruptedException e) {
            if (closed.get()) {
                return Awaitable.of(false);
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
