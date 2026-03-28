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
 *
 * <p>A channel coordinates producers and consumers without exposing explicit
 * locks or shared mutable state.  It models the classic
 * <a href="https://en.wikipedia.org/wiki/Communicating_sequential_processes">CSP</a>
 * (Communicating Sequential Processes) paradigm popularized by Go's channels
 * and Kotlin's {@code Channel} type.
 *
 * <h2>Obtaining Instances</h2>
 * <p>Channels are created via the static factory methods on this interface,
 * following the same pattern as {@link java.util.List#of()}:
 * <pre>
 * def unbuffered = AsyncChannel.create()       // capacity 0 (rendezvous)
 * def buffered   = AsyncChannel.create(10)     // capacity 10
 * </pre>
 *
 * <h2>Buffering Modes</h2>
 * <ul>
 *   <li><b>Unbuffered (rendezvous)</b> — created with a capacity of {@code 0}.
 *       Each {@link #send(Object)} suspends until a matching {@link #receive()}
 *       arrives, and vice versa.  This provides the strongest synchronization
 *       guarantee between producer and consumer.</li>
 *   <li><b>Buffered</b> — created with a positive capacity.  Values are
 *       enqueued in an internal buffer until it fills, after which senders
 *       suspend until a receiver drains at least one slot.</li>
 * </ul>
 *
 * <h2>Composition with {@code await}</h2>
 * <p>All channel operations return {@link Awaitable} values, composing
 * naturally with Groovy's {@code await} keyword and combinators such as
 * {@link Awaitable#any(Object...)}.
 *
 * <h2>Iteration with {@code for await}</h2>
 * <p>Channels integrate with Groovy's {@code for await} syntax via the
 * {@link #asStream()} method, which provides a read-only {@link AsyncStream}
 * view that yields received values until the channel is closed and drained:
 * <pre>
 * def ch = AsyncChannel.create(2)
 * ch.send('a')
 * ch.send('b')
 * ch.close()
 * for await (item in ch) {
 *     println item   // prints 'a', then 'b'
 * }
 * </pre>
 *
 * <h2>Null Rejection</h2>
 * <p>{@code null} payloads are rejected to maintain a clear distinction
 * between "no value yet" and an actual payload.  Sending {@code null}
 * throws {@link NullPointerException} immediately.
 *
 * <h2>Close Semantics</h2>
 * <p>Closing a channel is an idempotent, one-way operation:
 * <ul>
 *   <li>Values already buffered remain receivable.</li>
 *   <li>Pending senders fail immediately with {@link ChannelClosedException}.</li>
 *   <li>After all buffered values are drained, subsequent receivers fail
 *       with {@link ChannelClosedException}.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods on {@code AsyncChannel} are safe for concurrent use by
 * multiple threads.  Values are delivered in FIFO (first-in, first-out)
 * order with respect to the order in which sends and receives are enqueued.
 *
 * @param <T> the payload type carried through the channel
 * @see #create()
 * @see #create(int)
 * @see ChannelClosedException
 * @see AsyncStream
 * @since 6.0.0
 */
public interface AsyncChannel<T> {

    // ---- Static Factory Methods -----------------------------------------

    /**
     * Creates an unbuffered (rendezvous) channel.
     *
     * <p>An unbuffered channel has a capacity of {@code 0}: each
     * {@link #send(Object)} suspends until a matching {@link #receive()}
     * arrives, providing the strongest producer–consumer synchronization
     * guarantee.
     *
     * <p>This is the channel equivalent of Go's {@code make(chan T)} and
     * Kotlin's {@code Channel(RENDEZVOUS)}.
     *
     * <p><b>Example:</b>
     * <pre>
     * def ch = AsyncChannel.&lt;String&gt;create()
     * Awaitable.go { ch.send('hello') }
     * def msg = await ch.receive()
     * </pre>
     *
     * @param <T> the payload type
     * @return a new unbuffered {@link AsyncChannel} instance
     * @since 6.0.0
     */
    static <T> AsyncChannel<T> create() {
        return new DefaultAsyncChannel<>();
    }

    /**
     * Creates a channel with the specified buffer capacity.
     *
     * <p>Values sent to a buffered channel are enqueued in an internal
     * buffer.  Senders complete immediately as long as buffer space is
     * available; they suspend only when the buffer is full.
     *
     * <p>A capacity of {@code 0} is equivalent to calling {@link #create()},
     * producing an unbuffered (rendezvous) channel.
     *
     * <p>This is the channel equivalent of C#'s
     * {@code Channel.CreateBounded<T>(capacity)} and Go's
     * {@code make(chan T, capacity)}.
     *
     * <p><b>Example:</b>
     * <pre>
     * def ch = AsyncChannel.&lt;Integer&gt;create(10)
     * for (i in 1..10) { ch.send(i) }  // all complete immediately
     * ch.close()
     * </pre>
     *
     * @param capacity the maximum number of values the buffer can hold;
     *                 must be {@code >= 0}.  A capacity of {@code 0}
     *                 creates an unbuffered (rendezvous) channel.
     * @param <T>      the payload type
     * @return a new {@link AsyncChannel} instance with the given capacity
     * @throws IllegalArgumentException if {@code capacity} is negative
     * @since 6.0.0
     */
    static <T> AsyncChannel<T> create(int capacity) {
        return new DefaultAsyncChannel<>(capacity);
    }

    /**
     * Returns this channel's configured buffer capacity.
     *
     * <p>A capacity of {@code 0} indicates an unbuffered (rendezvous)
     * channel.  A positive value indicates the maximum number of elements
     * that can be buffered before senders must wait.
     *
     * @return the buffer capacity (always {@code >= 0})
     */
    int getCapacity();

    /**
     * Returns the number of elements currently held in the buffer.
     *
     * <p>This is a snapshot value and may be stale by the time the caller
     * acts on it.  It is primarily useful for monitoring and diagnostics.
     *
     * @return the current number of buffered elements
     */
    int getBufferedSize();

    /**
     * Returns {@code true} if the channel has been closed.
     *
     * <p>A closed channel may still contain buffered values that can be
     * received.  This method only indicates that no further sends will
     * be accepted.
     *
     * @return {@code true} if the channel has been closed
     */
    boolean isClosed();

    /**
     * Sends a value through the channel asynchronously.
     *
     * <p>The returned {@link Awaitable} completes when the value has been
     * either handed off to a waiting receiver or enqueued in the buffer.
     * If the buffer is full (or the channel is unbuffered and no receiver
     * is waiting), the awaitable suspends until space becomes available.
     *
     * <p>If the channel is already closed, the returned awaitable completes
     * exceptionally with {@link ChannelClosedException}.
     *
     * @param value the value to send; must not be {@code null}
     * @return an {@link Awaitable} that completes when the send succeeds
     * @throws NullPointerException if {@code value} is {@code null}
     */
    Awaitable<Void> send(T value);

    /**
     * Receives the next value from the channel asynchronously.
     *
     * <p>Values are consumed in FIFO order.  If the channel is buffered,
     * buffered values are consumed first; a waiting sender is then
     * admitted to refill the freed buffer slot.  If the channel is
     * unbuffered, the receiver pairs directly with a waiting sender.
     *
     * <p>If the channel is closed and all buffered values have been
     * drained, the returned awaitable completes exceptionally with
     * {@link ChannelClosedException}.
     *
     * @return an {@link Awaitable} that yields the next value
     */
    Awaitable<T> receive();

    /**
     * Closes the channel.
     *
     * <p>Closing is idempotent — calling {@code close()} on an already-closed
     * channel is a safe no-op that returns {@code false}.  On the first
     * invocation:
     * <ol>
     *   <li>Buffered values that match a waiting receiver are delivered.</li>
     *   <li>Any remaining waiting receivers (after the buffer is exhausted)
     *       are failed with {@link ChannelClosedException}.</li>
     *   <li>All pending senders are failed with
     *       {@link ChannelClosedException}.</li>
     * </ol>
     *
     * @return {@code true} if this invocation transitioned the channel
     *         from open to closed; {@code false} if it was already closed
     */
    boolean close();

    /**
     * Returns a read-only {@link AsyncStream} view of this channel.
     *
     * <p>The returned stream yields values received from the channel.
     * When the channel is closed and all buffered values are drained, the
     * stream signals completion ({@link AsyncStream#moveNext() moveNext()}
     * returns {@code false}).
     *
     * <p>This enables natural {@code for await} iteration:
     * <pre>
     * for await (item in channel) {
     *     process(item)
     * }
     * </pre>
     *
     * <p><b>Ownership:</b> the stream view does <em>not</em> own the
     * channel — calling {@link AsyncStream#close() close()} on the stream
     * is a no-op.  The producer is responsible for closing the channel when
     * done sending.  This follows the Go convention where the sender, not
     * the receiver, closes the channel.
     *
     * @return an {@link AsyncStream} view backed by this channel
     */
    AsyncStream<T> asStream();
}
