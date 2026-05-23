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

/**
 * Context-aware handler for stateful actors. Receives the actor context,
 * the current state, and the incoming message; returns the new state,
 * which is also bound to the reply for {@link Actor#sendAndGet} callers.
 * <p>
 * To stop the actor from inside the handler, call
 * {@code ctx.self().stop()}.
 * <p>
 * <b>Known limitation around behaviour swaps.</b> If a handler calls
 * {@link ActorContext#become(StatefulHandler) ctx.become(...)} with a
 * {@code StatefulHandler} whose {@code S} type is incompatible with the
 * actor's current state, the {@link ClassCastException} surfaces on the
 * <em>next</em> dispatch — not at the {@code become} call site. The
 * actor's {@code S} is erased at construction and cannot be re-checked
 * at the swap site; see {@link ActorContext#become(StatefulHandler)}
 * for the full discussion.
 *
 * @param <S> the state type
 * @param <T> the message type
 * @see Actor#stateful(Object, StatefulHandler)
 * @since 6.0.0
 */
@FunctionalInterface
public interface StatefulHandler<S, T> {

    /**
     * Processes a single message and returns the new state.
     *
     * @param ctx     the actor context
     * @param state   the current state
     * @param message the incoming message
     * @return the new state
     */
    S apply(ActorContext<T> ctx, S state, T message);
}
