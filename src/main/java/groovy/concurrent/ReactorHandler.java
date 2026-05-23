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
 * Context-aware handler for reactor actors. Receives the actor context
 * and the incoming message; returns the reply value bound to
 * {@link Actor#sendAndGet} callers.
 * <p>
 * To stop the actor from inside the handler, call
 * {@code ctx.self().stop()}.
 *
 * @param <T> the message type
 * @param <R> the reply type
 * @see Actor#reactor(ReactorHandler)
 * @since 6.0.0
 */
@FunctionalInterface
public interface ReactorHandler<T, R> {

    /**
     * Processes a single message and returns the reply.
     *
     * @param ctx     the actor context
     * @param message the incoming message
     * @return the reply value
     */
    R apply(ActorContext<T> ctx, T message);
}
