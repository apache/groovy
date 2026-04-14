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

import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Selects the first available value from multiple {@link AsyncChannel}s.
 * <p>
 * This is the channel equivalent of {@link Awaitable#any(Object...)} —
 * while {@code Awaitable.any} races futures, {@code ChannelSelect} races
 * channel receives. Each call to {@link #select()} returns an
 * {@link Awaitable} that completes with a {@link Result} indicating
 * which channel produced the value and what it was.
 *
 * <pre>{@code
 * def prices = AsyncChannel.create(10)
 * def alerts = AsyncChannel.create(10)
 *
 * def sel = ChannelSelect.from(prices, alerts)
 * def result = await sel.select()
 * println "Channel ${result.index}: ${result.value}"
 * }</pre>
 * <p>
 * Inspired by GPars' {@code Select} and Go's {@code select} statement.
 *
 * @since 6.0.0
 */
public final class ChannelSelect {

    private final List<AsyncChannel<?>> channels;

    private ChannelSelect(List<AsyncChannel<?>> channels) {
        this.channels = channels;
    }

    /**
     * Creates a select over the given channels.
     *
     * @param channels the channels to select from
     * @return a new ChannelSelect
     */
    @SafeVarargs
    public static ChannelSelect from(AsyncChannel<?>... channels) {
        Objects.requireNonNull(channels, "channels must not be null");
        if (channels.length == 0) {
            throw new IllegalArgumentException("At least one channel is required");
        }
        return new ChannelSelect(List.of(channels));
    }

    /**
     * Waits for the first value available from any of the channels.
     * <p>
     * Returns an {@link Awaitable} that completes with a {@link Result}
     * containing the channel index and the received value.
     * <p>
     * Values consumed by non-winning channels are re-sent back to those
     * channels to prevent message loss. This may reorder values within
     * a channel but guarantees no values are silently dropped.
     *
     * @return an awaitable result indicating which channel produced the value
     */
    @SuppressWarnings("unchecked")
    public Awaitable<Result> select() {
        CompletableFuture<Result> winner = new CompletableFuture<>();
        AtomicBoolean won = new AtomicBoolean();
        for (int i = 0; i < channels.size(); i++) {
            final int index = i;
            AsyncChannel<?> ch = channels.get(i);
            ch.receive().toCompletableFuture().whenComplete((value, error) -> {
                if (error != null) return;
                if (won.compareAndSet(false, true)) {
                    winner.complete(new Result(index, value));
                } else {
                    // Re-send the consumed value back to avoid message loss
                    try {
                        ((AsyncChannel<Object>) ch).send(value);
                    } catch (ChannelClosedException ignored) {
                        // Channel was closed; value cannot be preserved
                    }
                }
            });
        }
        return GroovyPromise.of(winner);
    }

    /**
     * The result of a {@link #select()} operation, indicating which
     * channel produced the value.
     */
    public static final class Result {
        private final int index;
        private final Object value;

        Result(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        /** The zero-based index of the channel that produced the value. */
        public int getIndex() { return index; }

        /** The received value. */
        @SuppressWarnings("unchecked")
        public <T> T getValue() { return (T) value; }

        @Override
        public String toString() {
            return "SelectResult[channel=" + index + ", value=" + value + "]";
        }
    }
}
