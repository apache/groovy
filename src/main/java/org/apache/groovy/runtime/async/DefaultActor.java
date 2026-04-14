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

import groovy.concurrent.Actor;
import groovy.concurrent.Awaitable;
import groovy.concurrent.DataflowVariable;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link Actor} using a dedicated thread
 * and a {@link LinkedBlockingQueue} for message processing.
 * <p>
 * Each actor runs on its own thread (virtual on JDK 21+). Messages
 * are processed one at a time, guaranteeing thread-safe state access
 * without locks.
 *
 * @param <T> the message type
 * @see Actor
 * @since 6.0.0
 */
public final class DefaultActor<T> implements Actor<T> {

    private static final Object POISON = new Object();

    private final LinkedBlockingQueue<Envelope<T>> queue = new LinkedBlockingQueue<>();
    private final MessageProcessor<T> processor;
    private volatile boolean active = true;

    private DefaultActor(MessageProcessor<T> processor) {
        this.processor = processor;
        AsyncSupport.getExecutor().execute(this::processLoop);
    }

    // ---- Factory methods ------------------------------------------------

    public static <T, R> Actor<T> reactor(Function<T, R> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        return new DefaultActor<>(new ReactorProcessor<>(handler));
    }

    public static <T, S> Actor<T> stateful(S initialState, BiFunction<S, T, S> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        return new DefaultActor<>(new StatefulProcessor<>(initialState, handler));
    }

    // ---- Actor interface ------------------------------------------------

    @Override
    public void send(T message) {
        Objects.requireNonNull(message, "message must not be null");
        if (!active) throw new IllegalStateException("Actor has been stopped");
        queue.add(new Envelope<>(message, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Awaitable<R> sendAndGet(T message) {
        Objects.requireNonNull(message, "message must not be null");
        if (!active) throw new IllegalStateException("Actor has been stopped");
        DataflowVariable<R> reply = new DataflowVariable<>();
        queue.add(new Envelope<>(message, (DataflowVariable<Object>) (DataflowVariable<?>) reply));
        return reply;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void stop() {
        if (!active) return;
        active = false;
        // Poison pill signals the processing loop to exit after draining
        queue.add((Envelope<T>) (Envelope<?>) new Envelope<>(POISON, null));
    }

    // ---- Internal -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void processLoop() {
        while (true) {
            try {
                Envelope<T> envelope = queue.take();
                if (envelope.message == POISON) return;

                try {
                    Object result = processor.process((T) envelope.message);
                    if (envelope.reply != null) {
                        envelope.reply.bind(result);
                    }
                } catch (Throwable t) {
                    if (envelope.reply != null) {
                        envelope.reply.bindError(t);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "Actor[active=" + active + ", queued=" + queue.size() + "]";
    }

    // ---- Internal types -------------------------------------------------

    private record Envelope<T>(Object message, DataflowVariable<Object> reply) {}

    private interface MessageProcessor<T> {
        Object process(T message);
    }

    private static final class ReactorProcessor<T, R> implements MessageProcessor<T> {
        private final Function<T, R> handler;

        ReactorProcessor(Function<T, R> handler) {
            this.handler = handler;
        }

        @Override
        public Object process(T message) {
            return handler.apply(message);
        }
    }

    private static final class StatefulProcessor<T, S> implements MessageProcessor<T> {
        private final BiFunction<S, T, S> handler;
        private S state;

        StatefulProcessor(S initialState, BiFunction<S, T, S> handler) {
            this.state = initialState;
            this.handler = handler;
        }

        @Override
        public Object process(T message) {
            state = handler.apply(state, message);
            return state;
        }
    }
}
