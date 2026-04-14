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

import org.apache.groovy.runtime.async.DefaultActor;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A lightweight message-passing actor for concurrent state management.
 * <p>
 * Each actor has a dedicated thread that processes messages sequentially
 * from a queue. This guarantees that the actor's state is never accessed
 * concurrently — no locks needed.
 * <p>
 * Two factory methods provide the common patterns:
 * <ul>
 *   <li>{@link #reactor(Function)} — stateless; each message produces a reply</li>
 *   <li>{@link #stateful(Object, BiFunction)} — maintains state; handler
 *       receives (state, message) and returns new state</li>
 * </ul>
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
 * }</pre>
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
     *
     * @param message the message to send
     * @throws IllegalStateException if the actor has been stopped
     */
    void send(T message);

    /**
     * Sends a message and returns an {@link Awaitable} that completes
     * with the reply. For reactors, the reply is the handler's return
     * value. For stateful actors, the reply is the new state.
     *
     * @param message the message to send
     * @param <R>     the reply type
     * @return an awaitable reply
     * @throws IllegalStateException if the actor has been stopped
     */
    <R> Awaitable<R> sendAndGet(T message);

    /**
     * Returns {@code true} if this actor is running and accepting messages.
     */
    boolean isActive();

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
        return DefaultActor.reactor(handler);
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
     * @param initialState the initial state
     * @param handler      receives (state, message), returns new state
     * @param <T>          the message type
     * @param <S>          the state type
     * @return a started actor
     */
    static <T, S> Actor<T> stateful(S initialState, BiFunction<S, T, S> handler) {
        return DefaultActor.stateful(initialState, handler);
    }
}
