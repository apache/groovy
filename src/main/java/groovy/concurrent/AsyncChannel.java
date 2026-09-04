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

import org.apache.groovy.runtime.async.AsyncSupport;
import org.apache.groovy.runtime.async.DefaultAsyncChannel;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

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

    /**
     * Creates a timer channel: a capacity-1 channel that delivers a single
     * value, the {@link Instant} at which it fired, once {@code millis} has
     * elapsed from this call, and then closes.
     * <p>
     * Its purpose is to make a deadline a branch of a {@link ChannelSelect},
     * the way Go's {@code time.After} does, rather than an exception thrown
     * around the whole select by {@link Awaitable#orTimeout(long, TimeUnit)}:
     * <pre>{@code
     * def result = await ChannelSelect.from(work, AsyncChannel.after(100)).select()
     * if (result.index == 1) {
     *     // timed out: result.value is the Instant the timer fired
     * }
     * }</pre>
     * As a channel, a timer composes with everything a select offers: several
     * timers give per-branch deadlines, a timer may be guarded with
     * {@link ChannelSelect.Offer#when}, and {@link ChannelSelect#fair()} and
     * {@link ChannelSelect#random()} treat it like any other ready branch.
     * <p>
     * The clock starts now, not at the first receive, so a timer created
     * once and reused across selects is a fixed deadline shared by every
     * round. For a per-round timeout that re-arms on each call of a select
     * that is held and reused, use the timer offer
     * {@link ChannelSelect#after(long)} instead. A timer that loses a select keeps
     * its value: it fires all the same and holds the instant until received.
     * Firing is one event — the instant is offered and the channel is closed
     * together — so a receiver that takes the value observes
     * {@link #isClosed()} as true, and further receives fail with
     * {@link ChannelClosedException} rather than waiting forever. Closing a
     * timer before it fires cancels it. A delay that is not positive has already
     * elapsed, so the channel is returned already holding its instant: it
     * is ready at once, which suits a deadline computed from an absolute
     * time that has already passed.
     *
     * @param millis how long to wait before firing, in milliseconds
     * @return a channel that delivers the firing instant and then closes
     * @see #after(Duration)
     * @see ChannelSelect
     * @since 6.0.0
     */
    static AsyncChannel<Instant> after(long millis) {
        return DefaultAsyncChannel.after(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a timer channel that fires once {@code duration} has elapsed;
     * see {@link #after(long)}.
     *
     * @param duration how long to wait before firing
     * @return a channel that delivers the firing instant and then closes
     * @throws NullPointerException if duration is null
     * @since 6.0.0
     */
    static AsyncChannel<Instant> after(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return DefaultAsyncChannel.after(duration.toNanos(), TimeUnit.NANOSECONDS);
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

    // ---- Composition (each returns a new channel) -----------------------
    //
    // Each method spawns a background task that reads from this channel
    // using explicit receive()/await, writes to the output channel(s),
    // and closes them when the source is exhausted. We use receive()
    // directly rather than the Iterable iterator to avoid blocking
    // iterator issues on JDK 17 (no virtual threads).

    /**
     * Returns a new channel that passes only elements matching the predicate.
     *
     * @param predicate the filter function
     * @return a new filtered channel
     * @since 6.0.0
     */
    default AsyncChannel<T> filter(Predicate<T> predicate) {
        AsyncChannel<T> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) {
                    T item = AsyncSupport.await(receive());
                    if (predicate.test(item)) {
                        AsyncSupport.await(out.send(item));
                    }
                }
            } catch (ChannelClosedException ignored) {
            } finally {
                out.close();
            }
        });
        return out;
    }

    /**
     * Returns a new channel that transforms each element using the function.
     *
     * @param transform the mapping function
     * @param <R>       the output element type
     * @return a new transformed channel
     * @since 6.0.0
     */
    default <R> AsyncChannel<R> map(Function<T, R> transform) {
        AsyncChannel<R> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) {
                    T item = AsyncSupport.await(receive());
                    AsyncSupport.await(out.send(transform.apply(item)));
                }
            } catch (ChannelClosedException ignored) {
            } finally {
                out.close();
            }
        });
        return out;
    }

    /**
     * Returns a new channel that receives values from both this channel and
     * the other channel. Values are interleaved as they arrive. The output
     * closes when both inputs are exhausted.
     *
     * @param other the channel to merge with
     * @return a new merged channel
     * @since 6.0.0
     */
    default AsyncChannel<T> merge(AsyncChannel<? extends T> other) {
        AsyncChannel<T> out = create(getCapacity());
        var remaining = new AtomicInteger(2);
        Runnable closer = () -> {
            if (remaining.decrementAndGet() == 0) out.close();
        };
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) AsyncSupport.await(out.send(AsyncSupport.await(this.receive())));
            } catch (ChannelClosedException ignored) {
            } finally {
                closer.run();
            }
        });
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) AsyncSupport.await(out.send(AsyncSupport.await(other.receive())));
            } catch (ChannelClosedException ignored) {
            } finally {
                closer.run();
            }
        });
        return out;
    }

    /**
     * Returns two new channels: elements matching the predicate go to the
     * first, non-matching to the second. Both are closed when this channel
     * is exhausted.
     *
     * @param predicate the split condition
     * @return a list of two channels: [matching, non-matching]
     * @since 6.0.0
     */
    default List<AsyncChannel<T>> split(Predicate<T> predicate) {
        AsyncChannel<T> trueOut = create(getCapacity());
        AsyncChannel<T> falseOut = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) {
                    T item = AsyncSupport.await(receive());
                    if (predicate.test(item)) {
                        AsyncSupport.await(trueOut.send(item));
                    } else {
                        AsyncSupport.await(falseOut.send(item));
                    }
                }
            } catch (ChannelClosedException ignored) {
            } finally {
                trueOut.close();
                falseOut.close();
            }
        });
        return List.of(trueOut, falseOut);
    }

    /**
     * Returns a new channel that receives all values from this channel while
     * also sending a copy of each value to the tap channel. Useful for
     * logging, monitoring, or forking a side pipeline.
     *
     * @param tap the channel to send copies to
     * @return a new pass-through channel
     * @since 6.0.0
     */
    default AsyncChannel<T> tap(AsyncChannel<T> tap) {
        AsyncChannel<T> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> {
            try {
                while (true) {
                    T item = AsyncSupport.await(receive());
                    try { AsyncSupport.await(tap.send(item)); }
                    catch (ChannelClosedException ignored) { }
                    AsyncSupport.await(out.send(item));
                }
            } catch (ChannelClosedException ignored) {
            } finally {
                out.close();
            }
        });
        return out;
    }
}
