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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A one-to-many broadcast channel where each value sent is delivered
 * to all subscribers.
 * <p>
 * Unlike {@link AsyncChannel} (point-to-point, each value consumed by
 * one receiver), a {@code BroadcastChannel} delivers every value to
 * every subscriber that has called {@link #subscribe()}.
 *
 * <pre>{@code
 * def broadcast = BroadcastChannel.create()
 * def sub1 = broadcast.subscribe()
 * def sub2 = broadcast.subscribe()
 *
 * async {
 *     broadcast.send('hello')
 *     broadcast.send('world')
 *     broadcast.close()
 * }
 *
 * // Both subscribers receive both values
 * for await (msg in sub1) { println "Sub1: $msg" }
 * for await (msg in sub2) { println "Sub2: $msg" }
 * }</pre>
 * <p>
 * Inspired by GPars' {@code DataflowBroadcast}.
 *
 * @param <T> the value type
 * @see AsyncChannel
 * @since 6.0.0
 */
public final class BroadcastChannel<T> {

    private final CopyOnWriteArrayList<AsyncChannel<T>> subscribers = new CopyOnWriteArrayList<>();
    private volatile boolean closed;

    private BroadcastChannel() { }

    /**
     * Creates a new broadcast channel.
     *
     * @param <T> the value type
     * @return a new BroadcastChannel
     */
    public static <T> BroadcastChannel<T> create() {
        return new BroadcastChannel<>();
    }

    /**
     * Creates a new subscriber channel. The returned {@link AsyncChannel}
     * will receive all values sent to this broadcast from this point forward.
     * Each subscriber is independent — values are buffered per subscriber.
     *
     * @return a new subscriber channel
     */
    public AsyncChannel<T> subscribe() {
        return subscribe(16);
    }

    /**
     * Creates a new subscriber channel with the specified buffer capacity.
     *
     * @param bufferSize the buffer capacity for this subscriber
     * @return a new subscriber channel
     */
    public AsyncChannel<T> subscribe(int bufferSize) {
        if (closed) throw new ChannelClosedException("BroadcastChannel is closed");
        AsyncChannel<T> sub = AsyncChannel.create(bufferSize);
        subscribers.add(sub);
        return sub;
    }

    /**
     * Sends a value to all current subscribers.
     *
     * @param value the value to broadcast
     * @return an Awaitable that completes when all subscribers have accepted the value
     * @throws ChannelClosedException if the broadcast channel is closed
     */
    public Awaitable<Void> send(T value) {
        Objects.requireNonNull(value, "value must not be null");
        if (closed) throw new ChannelClosedException("BroadcastChannel is closed");
        CompletableFuture<?>[] futures = subscribers.stream()
                .map(sub -> {
                    try {
                        return sub.send(value).toCompletableFuture();
                    } catch (ChannelClosedException e) {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .toArray(CompletableFuture[]::new);
        return GroovyPromise.of(CompletableFuture.allOf(futures));
    }

    /**
     * Closes this broadcast channel and all subscriber channels.
     */
    public void close() {
        closed = true;
        for (AsyncChannel<T> sub : subscribers) {
            sub.close();
        }
    }

    /**
     * Returns {@code true} if this broadcast channel has been closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the number of current subscribers.
     */
    public int getSubscriberCount() {
        return subscribers.size();
    }
}
