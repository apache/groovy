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

import java.time.Duration;

/**
 * Context handle passed to context-aware actor handlers. Provides access
 * to the executing actor itself and to the per-dispatch capabilities
 * needed to express FSM-style actors: behavior swaps via
 * {@link #become(ReactorHandler) become(...)}, message deferral via
 * {@link #stash()} / {@link #unstashAll()}, and timed self-sends via
 * {@link #scheduleOnce(Object, Duration) scheduleOnce(...)} /
 * {@link #scheduleAtFixedRate(Object, Duration, Duration) scheduleAtFixedRate(...)}.
 * <p>
 * A context is scoped to a single actor and is only valid for use during
 * a handler invocation (including a context-aware {@code onError}
 * callback). Calls to its mutating methods from outside that window —
 * for example from a captured reference invoked on another thread —
 * throw {@link IllegalStateException}.
 *
 * @param <T> the actor's message type
 * @see Actor
 * @since 6.0.0
 */
public interface ActorContext<T> {

    /**
     * Returns the actor whose handler is currently executing.
     */
    Actor<T> self();

    /**
     * Replaces the handler used to process subsequent messages on a
     * reactor actor. The swap takes effect on the next message — the
     * current handler invocation completes normally, including binding
     * any {@code sendAndGet} reply and firing {@code onError} on failure.
     * <p>
     * The new handler may declare a different reply type than the original;
     * {@code sendAndGet} callers receive the new handler's return value.
     * <p>
     * Messages already queued at the moment of the swap, and messages sent
     * by other threads that have not yet observed the swap, are dispatched
     * to the new handler. The new handler is responsible for tolerating any
     * message its predecessor could have received — typically by including
     * a default branch that ignores or rejects unexpected messages, or by
     * deferring them with {@link #stash()} for later replay.
     *
     * @param newHandler the replacement reactor handler
     * @param <R>        the new reactor's reply type
     * @throws UnsupportedOperationException if this actor is stateful, or
     *         if this {@code ActorContext} implementation does not support
     *         behavior swaps
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws NullPointerException  if {@code newHandler} is null
     * @since 6.0.0
     */
    default <R> void become(ReactorHandler<T, R> newHandler) {
        throw new UnsupportedOperationException(
                "become(ReactorHandler) requires a reactor actor");
    }

    /**
     * Replaces the handler used to process subsequent messages on a
     * stateful actor. The current state value is preserved verbatim and
     * passed unchanged to the new handler. The swap takes effect on the
     * next message.
     * <p>
     * The state type {@code S} is unchecked at the swap site: if the new
     * handler's expected state type is incompatible with the actor's
     * current state, a {@link ClassCastException} is thrown when the next
     * message is dispatched.
     *
     * @param newHandler the replacement stateful handler
     * @param <S>        the state type expected by the new handler
     * @throws UnsupportedOperationException if this actor is a reactor, or
     *         if this {@code ActorContext} implementation does not support
     *         behavior swaps
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws NullPointerException  if {@code newHandler} is null
     * @since 6.0.0
     */
    default <S> void become(StatefulHandler<S, T> newHandler) {
        throw new UnsupportedOperationException(
                "become(StatefulHandler) requires a stateful actor");
    }

    /**
     * Defers the message currently being processed. The message is moved
     * out of the dispatch path: any {@code sendAndGet} reply remains
     * unbound, any state change computed by the current handler is
     * discarded, and the message is re-delivered when {@link #unstashAll()}
     * is later called.
     * <p>
     * Calling {@code stash()} more than once during a single handler
     * invocation is idempotent — the message is stashed once. If the
     * handler subsequently throws, the stash is rolled back and the failure
     * is reported normally (reply bound to error, {@code onError} fires).
     * A context-aware {@code onError} callback may itself call
     * {@code stash()} to defer the failed message for later retry.
     * <p>
     * Stashed messages do <em>not</em> count against the configured
     * mailbox bound — the bound applies to the queue of pending sends,
     * not to messages held in the stash.
     * <p>
     * <b>Warning — the stash buffer is unbounded by default.</b> An
     * actor that stashes messages from a source whose volume you do not
     * control (network input, external clients, untrusted callers) can
     * grow the stash without limit and exhaust the JVM heap if the
     * phase transition that would call {@link #unstashAll()} never
     * arrives. For any such actor, configure a bound and overflow
     * policy at construction time via
     * {@link ActorOptions#withStashBound(int, ActorOptions.StashOverflow)}.
     * The three policies are {@code FAIL} (this method throws),
     * {@code DROP_OLDEST} (evicts the oldest stashed message, binding
     * its reply to {@link IllegalStateException}), and {@code REJECT}
     * (binds the current message's reply to {@link IllegalStateException}
     * and does not stash it).
     * <p>
     * If {@link Actor#stop()} is invoked while messages are stashed, the
     * stashed messages are rejected: any {@code sendAndGet} reply is bound
     * to an {@link IllegalStateException} and fire-and-forget stashed
     * messages are discarded.
     *
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws UnsupportedOperationException if this {@code ActorContext}
     *         implementation does not support stash
     * @since 6.0.0
     */
    default void stash() {
        throw new UnsupportedOperationException(
                "This ActorContext implementation does not support stash");
    }

    /**
     * Re-injects all stashed messages at the head of the mailbox in the
     * order they were originally stashed (FIFO). Subsequent dispatches
     * will see the unstashed messages before any messages that other
     * senders have queued in the meantime.
     * <p>
     * No-op if no messages are stashed.
     *
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws UnsupportedOperationException if this {@code ActorContext}
     *         implementation does not support stash
     * @since 6.0.0
     */
    default void unstashAll() {
        throw new UnsupportedOperationException(
                "This ActorContext implementation does not support unstashAll");
    }

    /**
     * Schedules a one-shot self-send: the given message is delivered to
     * this actor after the given delay, via the same dispatch path as
     * {@link Actor#send(Object)} (mailbox bound respected,
     * {@code onError} fires on handler failure, etc.).
     * <p>
     * The returned {@link Cancellable} can be used to call off the send
     * before it fires. On {@link Actor#stop()}, all outstanding scheduled
     * timers (one-shot and recurring) created via this context are
     * cancelled automatically.
     * <p>
     * Timer firings race with the actor's lifecycle: a send that
     * arrives after the actor has stopped is silently dropped (the
     * implementation catches the {@link IllegalStateException} that
     * {@code send} would throw). Combining a bounded mailbox using
     * {@link ActorOptions.Overflow#BLOCK} with timers is discouraged: each
     * timer firing offloads its {@code send} to the shared async executor,
     * so the scheduler thread itself never blocks, but a full mailbox will
     * then tie up an executor thread until space frees, and enough such
     * timers can exhaust the shared pool. Prefer
     * {@link ActorOptions.Overflow#FAIL FAIL} or
     * {@link ActorOptions.Overflow#DROP_NEWEST DROP_NEWEST} for
     * actors that schedule their own messages.
     *
     * @param message the message to deliver
     * @param delay   how long to wait before delivering
     * @return a handle for cancelling the scheduled send
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws NullPointerException  if {@code message} or {@code delay} is null
     * @throws UnsupportedOperationException if this {@code ActorContext}
     *         implementation does not support scheduling
     * @since 6.0.0
     */
    default Cancellable scheduleOnce(T message, Duration delay) {
        throw new UnsupportedOperationException(
                "This ActorContext implementation does not support scheduleOnce");
    }

    /**
     * Schedules a recurring self-send: the given message is delivered
     * to this actor starting after {@code initialDelay}, then again
     * every {@code interval} thereafter (fixed-rate). Cancellation
     * stops further firings; an already-dispatched message in the
     * mailbox is unaffected.
     * <p>
     * Same lifecycle and bounded-mailbox caveats as
     * {@link #scheduleOnce(Object, Duration)} apply.
     *
     * @param message      the message to deliver
     * @param initialDelay the wait before the first delivery
     * @param interval     the wait between successive deliveries
     * @return a handle for cancelling further firings
     * @throws IllegalStateException if called outside a handler dispatch
     *         or from a thread other than the actor's worker thread
     * @throws NullPointerException  if any argument is null
     * @throws UnsupportedOperationException if this {@code ActorContext}
     *         implementation does not support scheduling
     * @since 6.0.0
     */
    default Cancellable scheduleAtFixedRate(T message, Duration initialDelay, Duration interval) {
        throw new UnsupportedOperationException(
                "This ActorContext implementation does not support scheduleAtFixedRate");
    }
}
