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

import groovy.util.function.TriConsumer;
import org.apache.groovy.runtime.async.DefaultActor;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A lightweight message-passing actor for concurrent state management.
 * <p>
 * Each actor has a dedicated thread that processes messages sequentially
 * from a queue. This guarantees that the actor's state is never accessed
 * concurrently — no locks needed.
 * <p>
 * Two factory patterns provide the common shapes:
 * <ul>
 *   <li>{@link #reactor(Function)} — stateless; each message produces a reply</li>
 *   <li>{@link #stateful(Object, BiFunction)} — maintains state; handler
 *       receives (state, message) and returns new state</li>
 * </ul>
 * Each pattern has a context-aware overload ({@link #reactor(ReactorHandler)}
 * and {@link #stateful(Object, StatefulHandler)}) whose handler receives
 * an {@link ActorContext} as a first argument. The context exposes
 * {@code self()} so the handler can stop the actor without a captured
 * self-reference.
 *
 * <pre>{@code
 * // Reactor: stateless message processing
 * def doubler = Actor.reactor { msg -> msg * 2 }
 * assert await(doubler.sendAndGet(5)) == 10
 *
 * // Stateful: accumulates state across messages
 * def counter = Actor.stateful(0) { state, msg ->
 *     switch (msg) {
 *         case 'increment': return state + 1
 *         case 'decrement': return state - 1
 *         default: return state
 *     }
 * }
 * counter.send('increment')
 * counter.send('increment')
 * assert await(counter.sendAndGet('increment')) == 3
 *
 * // Self-stop from a handler via the context
 * def bot = Actor.stateful(0) { ctx, count, msg ->
 *     def next = count + 1
 *     if (next >= 3) ctx.self().stop()
 *     next
 * }
 * }</pre>
 * <p>
 * For FSM-style actors, the context-aware handler shapes
 * ({@link ReactorHandler} / {@link StatefulHandler}) receive an
 * {@link ActorContext} that supports {@link ActorContext#become(ReactorHandler)
 * ctx.become(...)} to swap the active handler, and {@link ActorContext#stash()
 * ctx.stash()} / {@link ActorContext#unstashAll() ctx.unstashAll()} to defer
 * messages received in the wrong phase and replay them later. See
 * {@link ActorContext} for the full semantics.
 * <p>
 * Actors use virtual threads on JDK 21+ for efficient scheduling.
 * Millions of actors can coexist without pool tuning.
 * <p>
 * Inspired by GPars actors, Erlang processes, and Clojure agents.
 *
 * @param <T> the message type
 * @see Agent
 * @see AsyncScope
 * @since 6.0.0
 */
public interface Actor<T> extends AutoCloseable {

    /**
     * Sends a message to this actor. The message is queued and processed
     * asynchronously. Fire-and-forget — no reply is expected.
     * <p>
     * Bounded-mailbox interaction (see {@link ActorOptions.Overflow}):
     * <ul>
     *   <li>{@link ActorOptions.Overflow#BLOCK BLOCK}: the calling thread
     *       blocks until queue capacity is available, then enqueues.</li>
     *   <li>{@link ActorOptions.Overflow#FAIL FAIL}: throws
     *       {@link IllegalStateException} when the mailbox is full.</li>
     *   <li>{@link ActorOptions.Overflow#DROP_NEWEST DROP_NEWEST}: the
     *       message is <em>silently dropped</em> — there is no reply
     *       to carry the failure, so a fire-and-forget overflow is
     *       invisible to the sender. If you need drop visibility,
     *       prefer {@link #sendAndGet} (which binds the dropped
     *       reply to {@link IllegalStateException}) or a different
     *       overflow policy.</li>
     * </ul>
     *
     * @param message the message to send
     * @throws IllegalStateException if the actor has been stopped, or if
     *         the mailbox is bounded with {@link ActorOptions.Overflow#FAIL}
     *         and is full
     */
    void send(T message);

    /**
     * Sends a message and returns an {@link Awaitable} that completes
     * with the reply. For reactors, the reply is the handler's return
     * value. For stateful actors, the reply is the new state.
     * <p>
     * Bounded-mailbox interaction (see {@link ActorOptions.Overflow}):
     * <ul>
     *   <li>{@link ActorOptions.Overflow#BLOCK BLOCK}: the calling thread
     *       blocks until queue capacity is available, then enqueues.</li>
     *   <li>{@link ActorOptions.Overflow#FAIL FAIL}: throws
     *       {@link IllegalStateException} when the mailbox is full.</li>
     *   <li>{@link ActorOptions.Overflow#DROP_NEWEST DROP_NEWEST}: returns
     *       an {@code Awaitable} that completes exceptionally with
     *       {@link IllegalStateException} indicating the message was
     *       dropped; the handler is never invoked.</li>
     * </ul>
     *
     * @param message the message to send
     * @param <R>     the reply type
     * @return an awaitable reply
     * @throws IllegalStateException if the actor has been stopped, or if
     *         the mailbox is bounded with {@link ActorOptions.Overflow#FAIL}
     *         and is full
     */
    <R> Awaitable<R> sendAndGet(T message);

    /**
     * Returns {@code true} while the actor is accepting new sends.
     * <p>
     * The actor lifecycle has three states, expressed via this method
     * and {@link #isTerminated()}:
     * <ul>
     *   <li><b>accepting</b> — {@code isActive() == true},
     *       {@code isTerminated() == false}: the actor accepts new
     *       sends and is processing them.</li>
     *   <li><b>draining</b> — {@code isActive() == false},
     *       {@code isTerminated() == false}: entered immediately when
     *       {@link #stop()} is called. Further sends throw
     *       {@link IllegalStateException}, but messages already queued
     *       (or sent in a race with {@code stop}) continue to run.</li>
     *   <li><b>terminated</b> — {@code isActive() == false},
     *       {@code isTerminated() == true}: the worker has exited;
     *       the queue and any stash have been processed (or, for
     *       stashed {@code sendAndGet} replies, rejected).</li>
     * </ul>
     */
    boolean isActive();

    /**
     * Returns {@code true} once the worker has fully exited — i.e. the
     * queue and any stashed messages have been processed (or rejected,
     * in the case of stashed {@code sendAndGet} replies). Always
     * {@code false} while {@link #isActive()} is true; becomes true
     * some time after {@link #stop()} is called, once draining
     * completes.
     *
     * @since 6.0.0
     */
    default boolean isTerminated() {
        // Conservative default for implementations that don't track
        // the draining phase: equate "not active" with "terminated".
        // DefaultActor overrides this with the precise value.
        return !isActive();
    }

    /**
     * Stops this actor gracefully. Messages already in the queue are
     * processed before the actor shuts down. New sends after stop
     * throw {@link IllegalStateException}.
     */
    void stop();

    /**
     * Stops this actor. Equivalent to {@link #stop()}.
     */
    @Override
    default void close() {
        stop();
    }

    /**
     * Registers a handler invoked when the message processor throws.
     * <p>
     * Fire-and-forget {@link #send} otherwise has no way to surface a
     * handler exception; this hook is the supported way to log, record
     * metrics for, or react to those failures. For {@link #sendAndGet}
     * the failure is still reported through the returned {@code Awaitable};
     * the {@code onError} handler runs in addition.
     * <p>
     * To stop the actor from inside an error handler, prefer the
     * context-aware overload {@link #onError(TriConsumer)} —
     * {@code ctx.self().stop()} works on any actor. Alternatively, if
     * the actor was built with
     * {@link ActorOptions#withCurrentSelf(boolean) withCurrentSelf(true)},
     * call {@link Actor#currentSelf() Actor.<T>currentSelf().stop()};
     * without that opt-in {@code currentSelf()} throws.
     * <p>
     * Exceptions thrown from the handler itself are caught and discarded
     * so the actor's processing loop is not destabilised. Replacing a
     * previously registered handler replaces it wholesale — there is no
     * chaining.
     * <p>
     * <b>Register before the first send.</b> An {@code onError} call
     * that happens after a message is already in flight may not see
     * that message's failure: only handlers visible to the worker by
     * the time it dispatches a given message are invoked for it. To
     * guarantee coverage, chain {@code onError} into actor construction
     * — as in the example below — and avoid registering it later.
     *
     * <pre>{@code
     * def actor = Actor.reactor(handler).onError { Throwable t, msg ->
     *     log.warn("actor failed processing {}", msg, t)
     * }
     * }</pre>
     *
     * @param handler invoked as {@code (throwable, message)}
     * @return this actor, for chaining
     * @throws UnsupportedOperationException if the implementation does
     *         not support custom error handling
     * @since 6.0.0
     */
    default Actor<T> onError(BiConsumer<Throwable, ? super T> handler) {
        throw new UnsupportedOperationException(
                "This Actor implementation does not support onError");
    }

    /**
     * Context-aware variant of {@link #onError(BiConsumer)}. The handler
     * receives an {@link ActorContext} that can be used to stop the
     * actor via {@code ctx.self().stop()}.
     *
     * @param handler invoked as {@code (context, throwable, message)}
     * @return this actor, for chaining
     * @throws UnsupportedOperationException if the implementation does
     *         not support custom error handling
     * @since 6.0.0
     */
    default Actor<T> onError(TriConsumer<ActorContext<T>, Throwable, ? super T> handler) {
        throw new UnsupportedOperationException(
                "This Actor implementation does not support onError");
    }

    /**
     * Returns the actor whose handler is currently executing on this
     * thread.
     * <p>
     * This convenience lets callers using the simple
     * {@link Function} / {@link BiFunction} factories self-stop without
     * restructuring to the context-aware overloads. Prefer the
     * context-aware overloads where possible.
     * <p>
     * Support is opt-in per actor: the actor must be configured with
     * {@link ActorOptions#withCurrentSelf(boolean) ActorOptions.withCurrentSelf(true)}.
     * The default options do not publish the thread-local, so this
     * method throws {@link IllegalStateException} unless the actor
     * was constructed with the flag enabled.
     *
     * @param <T> the actor's message type, inferred at the call site
     * @return the actor currently executing a handler on this thread
     * @throws IllegalStateException if called outside an actor handler,
     *         or from an actor not configured with
     *         {@link ActorOptions#withCurrentSelf(boolean)}
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    static <T> Actor<T> currentSelf() {
        Actor<?> a = DefaultActor.currentlyExecuting();
        if (a == null) {
            throw new IllegalStateException(
                    "Actor.currentSelf() unavailable: called outside a handler, "
                            + "or the actor was not configured with "
                            + "ActorOptions.withCurrentSelf(true)");
        }
        return (Actor<T>) a;
    }

    // ---- Factory methods ------------------------------------------------

    /**
     * Creates a stateless reactor actor. Each message is passed to the
     * handler function, and the return value becomes the reply for
     * {@link #sendAndGet} callers.
     *
     * <pre>{@code
     * var doubler = Actor.reactor(n -> (int) n * 2);
     * System.out.println(AsyncSupport.await(doubler.sendAndGet(5))); // 10
     * }</pre>
     *
     * @param handler the message processing function
     * @param <T>     the message type
     * @param <R>     the reply type
     * @return a started actor
     */
    static <T, R> Actor<T> reactor(Function<T, R> handler) {
        return DefaultActor.reactor(handler, ActorOptions.DEFAULTS);
    }

    /**
     * Creates a stateless reactor actor with explicit {@link ActorOptions}
     * controlling the mailbox and executor.
     *
     * @since 6.0.0
     */
    static <T, R> Actor<T> reactor(Function<T, R> handler, ActorOptions options) {
        return DefaultActor.reactor(handler, options);
    }

    /**
     * Context-aware variant of {@link #reactor(Function)}. The handler
     * receives an {@link ActorContext} alongside the message and can
     * stop the actor via {@code ctx.self().stop()}.
     *
     * @since 6.0.0
     */
    static <T, R> Actor<T> reactor(ReactorHandler<T, R> handler) {
        return DefaultActor.reactor(handler, ActorOptions.DEFAULTS);
    }

    /**
     * Context-aware variant of {@link #reactor(Function, ActorOptions)}.
     *
     * @since 6.0.0
     */
    static <T, R> Actor<T> reactor(ReactorHandler<T, R> handler, ActorOptions options) {
        return DefaultActor.reactor(handler, options);
    }

    /**
     * Creates a stateful actor. The handler receives the current state
     * and the message, and returns the new state. For {@link #sendAndGet}
     * callers, the new state is the reply.
     *
     * <pre>{@code
     * var counter = Actor.stateful(0, (state, msg) -> {
     *     if ("increment".equals(msg)) return (int) state + 1;
     *     return state;
     * });
     * counter.send("increment");
     * System.out.println(AsyncSupport.await(counter.sendAndGet("increment"))); // 2
     * }</pre>
     *
     * <p>
     * Note: the state type {@code S} is fixed at construction; if a
     * later {@link ActorContext#become(StatefulHandler) ctx.become(...)}
     * call swaps in a {@link StatefulHandler} expecting an incompatible
     * {@code S}, the resulting {@link ClassCastException} surfaces on
     * the next dispatch rather than at the swap site.
     *
     * @param initialState the initial state
     * @param handler      receives (state, message), returns new state
     * @param <T>          the message type
     * @param <S>          the state type
     * @return a started actor
     */
    static <T, S> Actor<T> stateful(S initialState, BiFunction<S, T, S> handler) {
        return DefaultActor.stateful(initialState, handler, ActorOptions.DEFAULTS);
    }

    /**
     * Creates a stateful actor with explicit {@link ActorOptions}
     * controlling the mailbox and executor.
     *
     * @since 6.0.0
     */
    static <T, S> Actor<T> stateful(S initialState, BiFunction<S, T, S> handler, ActorOptions options) {
        return DefaultActor.stateful(initialState, handler, options);
    }

    /**
     * Context-aware variant of {@link #stateful(Object, BiFunction)}.
     * The handler receives an {@link ActorContext} alongside the state
     * and message and can stop the actor via {@code ctx.self().stop()}.
     *
     * @since 6.0.0
     */
    static <T, S> Actor<T> stateful(S initialState, StatefulHandler<S, T> handler) {
        return DefaultActor.stateful(initialState, handler, ActorOptions.DEFAULTS);
    }

    /**
     * Context-aware variant of {@link #stateful(Object, BiFunction, ActorOptions)}.
     *
     * @since 6.0.0
     */
    static <T, S> Actor<T> stateful(S initialState, StatefulHandler<S, T> handler, ActorOptions options) {
        return DefaultActor.stateful(initialState, handler, options);
    }
}
