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

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Template base class for queue-based {@link AsyncStream} implementations.
 *
 * <p>This class implements the
 * <a href="https://en.wikipedia.org/wiki/Template_method_pattern">Template Method</a>
 * pattern, centralising the signal dispatch logic, lifecycle management, and
 * interrupt handling that is common to all queue-based async streams.
 * Concrete subclasses only need to supply a {@link BlockingQueue} and override
 * a small number of hook methods to customise behaviour.</p>
 *
 * <h2>Signal protocol</h2>
 * <p>Elements flowing through the queue are wrapped in one of three signal types:</p>
 * <ul>
 *   <li>{@link ValueSignal} — carries a data element (may wrap {@code null})</li>
 *   <li>{@link ErrorSignal} — carries a {@link Throwable} to propagate</li>
 *   <li>{@link #COMPLETE} — singleton sentinel indicating normal end-of-stream</li>
 * </ul>
 * <p>The template's {@link #moveNext()} dispatches on these signals with a fixed
 * sequence: value → set current + {@link #afterValueConsumed()} + return {@code true};
 * error → set closed + sneaky-throw; complete → set closed + return {@code false}.</p>
 *
 * <h2>Hook methods (override points)</h2>
 * <table>
 *   <caption>Hook methods and their defaults</caption>
 *   <tr><th>Hook</th><th>Default</th><th>Typical override</th></tr>
 *   <tr><td>{@link #beforeTake()}</td><td>return {@code MOVE_NEXT_FALSE} if closed</td>
 *       <td>thread registration, double-check, or drain check</td></tr>
 *   <tr><td>{@link #afterValueConsumed()}</td><td>no-op</td>
 *       <td>request more items from upstream (back-pressure)</td></tr>
 *   <tr><td>{@link #afterMoveNext()}</td><td>no-op</td>
 *       <td>unregister consumer thread</td></tr>
 *   <tr><td>{@link #onMoveNextInterrupted(InterruptedException)}</td>
 *       <td>set closed, restore interrupt, throw {@link CancellationException}</td>
 *       <td>return {@code MOVE_NEXT_FALSE} if already closed</td></tr>
 *   <tr><td>{@link #onClose()}</td><td><em>abstract</em></td>
 *       <td>interrupt threads, cancel subscriptions, drain queue</td></tr>
 * </table>
 *
 * <h2>Thread safety</h2>
 * <p>The {@link #closed} flag is an {@link AtomicBoolean} shared between the
 * producer (subclass-managed) and consumer ({@code moveNext()}) sides.
 * The {@link #close()} method uses CAS to guarantee exactly-once semantics.
 * The {@link #current} field is {@code volatile} for safe cross-thread visibility.</p>
 *
 * <p>This class is an internal implementation detail and should not be referenced
 * directly by user code.</p>
 *
 * @param <T> the element type
 * @see AsyncStreamGenerator
 * @see FlowPublisherAdapter
 * @since 6.0.0
 */
public abstract class AbstractAsyncStream<T> implements AsyncStream<T> {

    // ---- Unified signal types ----

    /**
     * Wraps a data element for transport through the signal queue.
     * The wrapper is necessary because the queue element type is {@code Object},
     * and the actual value may be {@code null}.
     */
    protected record ValueSignal(Object value) { }

    /**
     * Wraps an error for transport through the signal queue.
     * When dispatched by {@link #moveNext()}, the wrapped throwable is
     * re-thrown via {@link AsyncSupport#sneakyThrow(Throwable)}.
     */
    protected record ErrorSignal(Throwable error) { }

    /**
     * Singleton sentinel indicating normal stream completion.
     * Identity comparison ({@code ==}) is used in the dispatch logic.
     */
    protected static final Object COMPLETE = new Object();

    // ---- Shared state ----

    /** The signal queue bridging producer and consumer. */
    protected final BlockingQueue<Object> queue;

    /** Lifecycle flag: set exactly once when the stream is closed. */
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    /** Most recently consumed value, set by {@link #moveNext()} on value signals. */
    private volatile T current;

    /**
     * @param queue the blocking queue used for producer→consumer signal delivery;
     *              must not be {@code null}
     */
    protected AbstractAsyncStream(BlockingQueue<Object> queue) {
        this.queue = Objects.requireNonNull(queue, "queue");
    }

    // ---- Template method: moveNext ----

    /**
     * Template method implementing the {@link AsyncStream} iteration protocol.
     *
     * <p>Execution sequence:</p>
     * <ol>
     *   <li>{@link #beforeTake()} — may short-circuit with an early return</li>
     *   <li>{@code queue.take()} — blocks until a signal is available</li>
     *   <li>Signal dispatch: value / error / complete</li>
     *   <li>{@link #afterMoveNext()} — always runs (finally block)</li>
     * </ol>
     *
     * <p>If {@code queue.take()} throws {@link InterruptedException},
     * {@link #onMoveNextInterrupted(InterruptedException)} handles it.</p>
     *
     * @return an {@code Awaitable<Boolean>} — {@code true} if a new element is
     *         available via {@link #getCurrent()}, {@code false} if the stream
     *         is exhausted or closed
     */
    @Override
    @SuppressWarnings("unchecked")
    public final Awaitable<Boolean> moveNext() {
        Awaitable<Boolean> earlyReturn = beforeTake();
        if (earlyReturn != null) {
            return earlyReturn;
        }
        try {
            Object signal = queue.take();

            if (signal instanceof ValueSignal vs) {
                current = (T) vs.value;
                afterValueConsumed();
                return MOVE_NEXT_TRUE;
            }
            if (signal instanceof ErrorSignal es) {
                closed.set(true);
                Throwable cause = es.error;
                if (cause instanceof Error err) throw err;
                throw AsyncSupport.sneakyThrow(cause);
            }
            // COMPLETE sentinel — end-of-stream
            closed.set(true);
            return MOVE_NEXT_FALSE;
        } catch (InterruptedException e) {
            return onMoveNextInterrupted(e);
        } finally {
            afterMoveNext();
        }
    }

    // ---- Final implementations ----

    /**
     * {@inheritDoc}
     * <p>
     * Returns the most recently consumed element, set by the last successful
     * {@link #moveNext()} call.
     *
     * @return the current element, or {@code null} before the first successful
     *         {@code moveNext()} call
     */
    @Override
    public final T getCurrent() {
        return current;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Atomically marks this stream as closed via CAS on the {@link #closed}
     * flag, then delegates to {@link #onClose()} for subclass-specific cleanup.
     * Idempotent: only the first invocation triggers {@code onClose()}.
     */
    @Override
    public final void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        onClose();
    }

    // ---- Abstract methods ----

    /**
     * Subclass-specific cleanup, called exactly once from {@link #close()}.
     * Typical actions include interrupting blocked threads, cancelling
     * upstream subscriptions, and draining the queue.
     */
    protected abstract void onClose();

    // ---- Hook methods with defaults ----

    /**
     * Pre-take hook invoked at the start of {@link #moveNext()}.
     * <p>
     * Return a non-{@code null} {@link Awaitable} to short-circuit
     * {@code moveNext()} without blocking on the queue.  Return {@code null}
     * to proceed with the normal take-and-dispatch sequence.
     * <p>
     * The default implementation returns {@link #MOVE_NEXT_FALSE} when
     * the stream is closed, and {@code null} otherwise.
     *
     * @return an early-return value, or {@code null} to continue
     */
    protected Awaitable<Boolean> beforeTake() {
        return closed.get() ? MOVE_NEXT_FALSE : null;
    }

    /**
     * Post-value hook invoked after a {@link ValueSignal} has been consumed
     * and the {@link #current} field updated.
     * <p>
     * Subclasses may use this to signal demand to an upstream source
     * (e.g. {@code Subscription.request(1)} for reactive streams).
     * <p>
     * The default implementation is a no-op.
     */
    protected void afterValueConsumed() { }

    /**
     * Finally hook invoked after every {@link #moveNext()} attempt,
     * regardless of outcome (value, error, completion, or interrupt).
     * <p>
     * Subclasses may use this to unregister the consumer thread.
     * <p>
     * The default implementation is a no-op.
     */
    protected void afterMoveNext() { }

    /**
     * Interrupt handler invoked when {@code queue.take()} throws
     * {@link InterruptedException} inside {@link #moveNext()}.
     * <p>
     * The default implementation sets {@link #closed} to {@code true},
     * restores the interrupt flag, and throws a {@link CancellationException}.
     * Subclasses may override to return {@link #MOVE_NEXT_FALSE} instead
     * of throwing (e.g. when an external {@code close()} caused the interrupt).
     *
     * @param e the interrupt exception
     * @return an {@link Awaitable} to return from {@code moveNext()}, or
     *         the method may throw instead
     */
    protected Awaitable<Boolean> onMoveNextInterrupted(InterruptedException e) {
        closed.set(true);
        Thread.currentThread().interrupt();
        throw newCancellationException("Interrupted during moveNext", e);
    }

    // ---- Utilities for subclasses ----

    /**
     * Creates a {@link CancellationException} with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the interrupt that triggered the cancellation
     * @return a new {@code CancellationException}
     */
    protected static CancellationException newCancellationException(String message, InterruptedException cause) {
        CancellationException ce = new CancellationException(message);
        ce.initCause(cause);
        return ce;
    }
}
