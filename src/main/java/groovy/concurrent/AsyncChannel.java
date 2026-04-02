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

import org.apache.groovy.runtime.async.DefaultAsyncChannel;

/**
 * An asynchronous channel for inter-task communication with optional buffering.
 * <p>
 * A channel coordinates producers and consumers without exposing explicit
 * locks or shared mutable state, following the CSP (Communicating Sequential
 * Processes) paradigm popularized by Go's channels.
 * <p>
 * Channels support both unbuffered (rendezvous) and buffered modes:
 * <ul>
 *   <li><b>Unbuffered</b> — {@code create()} or {@code create(0)}. Each
 *       {@code send} suspends until a matching {@code receive} arrives.</li>
 *   <li><b>Buffered</b> — {@code create(n)}. Values are enqueued until the
 *       buffer fills, then senders suspend.</li>
 * </ul>
 * <p>
 * Channels implement {@link Iterable}, so they work with {@code for await}
 * and regular {@code for} loops — iteration yields received values until the
 * channel is closed and drained:
 * <pre>{@code
 * def ch = AsyncChannel.create(2)
 * async { ch.send('a'); ch.send('b'); ch.close() }
 * for await (item in ch) {
 *     println item   // prints 'a', then 'b'
 * }
 * }</pre>
 *
 * @param <T> the payload type
 * @see Awaitable
 * @since 6.0.0
 */
public interface AsyncChannel<T> extends Iterable<T> {

    /**
     * Creates an unbuffered (rendezvous) channel.
     */
    static <T> AsyncChannel<T> create() {
        return new DefaultAsyncChannel<>();
    }

    /**
     * Creates a channel with the specified buffer capacity.
     *
     * @param capacity the maximum buffer size; 0 for unbuffered
     */
    static <T> AsyncChannel<T> create(int capacity) {
        return new DefaultAsyncChannel<>(capacity);
    }

    /** Returns this channel's buffer capacity. */
    int getCapacity();

    /** Returns the number of values currently buffered. */
    int getBufferedSize();

    /** Returns {@code true} if this channel has been closed. */
    boolean isClosed();

    /**
     * Sends a value through this channel.
     * <p>
     * The returned {@link Awaitable} completes when the value has been
     * delivered to a receiver or buffered. Sending to a closed channel
     * fails immediately with {@link ChannelClosedException}.
     *
     * @param value the value to send; must not be {@code null}
     * @return an Awaitable that completes when the send succeeds
     * @throws NullPointerException if value is null
     */
    Awaitable<Void> send(T value);

    /**
     * Receives the next value from this channel.
     * <p>
     * The returned {@link Awaitable} completes when a value is available.
     * Receiving from a closed, empty channel fails with
     * {@link ChannelClosedException}.
     *
     * @return an Awaitable that yields the next value
     */
    Awaitable<T> receive();

    /**
     * Closes this channel. Idempotent.
     * <p>
     * Buffered values remain receivable. Pending senders fail with
     * {@link ChannelClosedException}. After all buffered values are
     * drained, subsequent receives also fail.
     *
     * @return {@code true} if this call actually closed the channel
     */
    boolean close();
}
