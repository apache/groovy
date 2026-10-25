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
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
 * {@link #produce(int, Consumer) produce} runs a producer and closes the
 * channel when it finishes, normally or with the failure it throws.
 * A producer that does not use {@code produce} catches that failure and
 * passes it to {@link #close(Throwable)}. {@code for await} throws that
 * failure after
 * the values already sent, and the code that reads the channel writes the
 * {@code try}/{@code catch}. A plain {@code for} loop, {@code each},
 * {@code collect} and {@code toList} use the same iterator and throw it
 * too. A Java enhanced {@code for} calls {@code Iterator.hasNext()}, which
 * may throw a checked exception that {@code Iterator} does not declare.
 * {@code receive} and {@code send} still fail with
 * {@link ChannelClosedException}; the failure is that exception's
 * {@linkplain Throwable#getCause() cause}, so a hand-written loop has to
 * read the cause or the failure is dropped.
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
     * Starts a producer for a new unbuffered channel.
     * <p>
     * The body runs asynchronously. {@link Producer#send(Object)} waits until
     * the channel accepts each value. When the body returns, the channel is
     * closed normally. When the body throws, the channel is closed with that
     * failure, after the values already accepted. A {@link ChannelClosedException}
     * from {@code send} means the channel was closed by its consumer; that is
     * not recorded as a producer failure.
     *
     * @param body the producer; must not be {@code null}
     * @param <T> the payload type
     * @return the channel the body sends to
     * @throws NullPointerException if {@code body} is null
     * @see #produce(int, Consumer)
     * @since 6.0.1
     */
    static <T> AsyncChannel<T> produce(Consumer<? super Producer<T>> body) {
        return produce(0, body);
    }

    /**
     * Starts a producer for a new channel of the given capacity.
     * See {@link #produce(Consumer)}.
     *
     * @param capacity the maximum buffer size; 0 for unbuffered
     * @param body the producer; must not be {@code null}
     * @param <T> the payload type
     * @return the channel the body sends to
     * @throws NullPointerException if {@code body} is null
     * @throws IllegalArgumentException if {@code capacity} is negative
     * @since 6.0.1
     */
    static <T> AsyncChannel<T> produce(int capacity, Consumer<? super Producer<T>> body) {
        Objects.requireNonNull(body, "body must not be null");
        AsyncChannel<T> channel = create(capacity);
        Producer<T> producer = new Producer<>(channel);
        AsyncSupport.getExecutor().execute(() -> runProducer(channel, producer, body));
        return channel;
    }

    /**
     * Runs {@code body} and closes {@code channel}. The body can throw an
     * {@link Error} or a checked exception; that failure has to close the
     * channel on this thread rather than escape the executor.
     */
    private static <T> void runProducer(AsyncChannel<T> channel, Producer<T> producer,
                                        Consumer<? super Producer<T>> body) {
        Throwable failure = null;
        try {
            body.accept(producer);
        } catch (Throwable thrown) {
            Throwable unwrapped = AsyncSupport.unwrap(thrown);
            // send throws ChannelClosedException only after the consumer has
            // already closed the channel. A ChannelClosedException thrown while
            // the channel is still open is the producer's own failure.
            if (!(unwrapped instanceof ChannelClosedException) || !channel.isClosed()) {
                failure = unwrapped;
            }
        } finally {
            channel.close(failure);
        }
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
     * fails immediately with {@link ChannelClosedException}. When the channel
     * was closed with a failure, that failure is the exception's cause.
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
     * {@link ChannelClosedException}. When the channel was closed with a
     * failure, that failure is the exception's cause. A loop that catches
     * only {@code ChannelClosedException} drops the failure unless it reads
     * {@link Throwable#getCause()}. {@code for await} throws the failure
     * itself; the code that reads the channel writes the {@code try}/{@code catch}.
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
     * @see #close(Throwable)
     */
    boolean close();

    /**
     * Closes this channel because {@code cause} failed the producer.
     * <p>
     * Values already buffered are still delivered. {@code receive} and
     * {@code send} then fail with {@link ChannelClosedException}, and
     * {@code cause} is that exception's cause. {@code for await} throws
     * {@code cause} after those values. The reading code writes the
     * {@code try}/{@code catch}. A normal {@link #close()} still ends the
     * loop without throwing.
     * <p>
     * {@code null} means a normal close, the same as {@link #close()}.
     * Returns {@code true} when this call closes the channel. The first
     * close wins, so a later call returns {@code false} and leaves any
     * retained failure unchanged.
     * <p>
     * The default implementation calls {@link #close()} and returns its
     * result. It does not retain {@code cause}. An implementation that does
     * not override this method therefore ends iteration without throwing
     * {@code cause}. The channel returned by {@link #create(int)} retains
     * {@code cause}. An implementation of {@link #close()} must not call
     * this method unless it also overrides it: the default calls
     * {@link #close()} again.
     *
     * @param cause the failure iteration should throw, or {@code null} for a normal close
     * @return {@code true} if this call closed the channel
     * @since 6.0.1
     */
    default boolean close(Throwable cause) {
        return close();
    }

    /**
     * Returns the failure passed to {@link #close(Throwable)}, or {@code null}
     * when this channel is open, was closed normally, or does not retain a cause.
     * <p>
     * The default is {@code null}. {@code receive} and {@code send} do not
     * throw this failure; it is the {@linkplain Throwable#getCause() cause}
     * of the {@link ChannelClosedException} they throw.
     *
     * @return the retained failure, or {@code null}
     * @since 6.0.1
     */
    default Throwable getCloseCause() {
        return null;
    }

    /**
     * Reads {@code source} until it closes, {@code step} returns {@code false},
     * or {@code step} throws, then runs {@code close}. A close of {@code source}
     * keeps that close's cause, and {@code null} means a normal close. A
     * throwable from {@code step} is the failure to retain. A closed output is
     * not one of those: {@code step} reports it by returning {@code false}
     * (see {@link #offer}), and that output's own close is left as it is.
     */
    private static <T> void pump(AsyncChannel<? extends T> source, Predicate<T> step,
                                 Consumer<Throwable> close) {
        Throwable failure = null;
        try {
            while (true) {
                T item;
                try {
                    item = AsyncSupport.await(source.receive());
                } catch (ChannelClosedException closed) {
                    failure = closed.getCause();
                    break;
                }
                if (!step.test(item)) break;
            }
        } catch (Throwable thrown) {
            failure = AsyncSupport.unwrap(thrown);
        } finally {
            close.accept(failure);
        }
    }

    /**
     * Sends {@code item} to {@code out}.
     *
     * @return {@code false} when {@code out} is already closed, so the caller
     *         stops without adopting {@code out}'s close cause
     */
    private static <T> boolean offer(AsyncChannel<? super T> out, T item) {
        try {
            AsyncSupport.await(out.send(item));
            return true;
        } catch (ChannelClosedException ignored) {
            return false;
        }
    }

    // ---- Composition (each returns a new channel) -----------------------
    //
    // Each method spawns a background task that reads from this channel
    // using explicit receive()/await, writes to the output channel(s),
    // and closes them when the source is exhausted. We use receive()
    // directly rather than the Iterable iterator to avoid blocking
    // iterator issues on JDK 17 (no virtual threads). A failure from the
    // source, or thrown by the callback, is retained on the output. A normal
    // close is not. merge forwards a value a relay has already taken and the
    // values buffered on the other input, then retains a failure without
    // waiting for the other input to end. An output the consumer has
    // already closed stops that send and is not treated as a failure of
    // the source.

    /**
     * Returns a new channel that passes only elements matching the predicate.
     * The result closes when this channel does. A failure from this channel
     * or from {@code predicate} is retained, so {@code for await} throws it.
     *
     * @param predicate the filter function
     * @return a new filtered channel
     * @since 6.0.0
     */
    default AsyncChannel<T> filter(Predicate<T> predicate) {
        AsyncChannel<T> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> pump(this, item -> {
            if (!predicate.test(item)) return true;
            return offer(out, item);
        }, out::close));
        return out;
    }

    /**
     * Returns a new channel that transforms each element using the function.
     * The result closes when this channel does. A failure from this channel
     * or from {@code transform} is retained, so {@code for await} throws it.
     *
     * @param transform the mapping function
     * @param <R>       the output element type
     * @return a new transformed channel
     * @since 6.0.0
     */
    default <R> AsyncChannel<R> map(Function<T, R> transform) {
        AsyncChannel<R> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> pump(this,
                item -> offer(out, transform.apply(item)), out::close));
        return out;
    }

    /**
     * Returns a new channel that receives values from both this channel and
     * {@code other}. Values are interleaved as they arrive.
     * <p>
     * The output closes when both inputs have closed normally. A failure
     * from either input closes the output with that failure once every value
     * a relay has already taken, and every value buffered on the other input
     * when the failure is seen, has been forwarded. Values sent to the other
     * input after that are not forwarded, so a busy producer cannot hold the
     * failure back. The other input is not closed. A producer still waiting
     * to hand a value over is not released. When both inputs fail, the first
     * failure retained on the output wins. A later normal close does not
     * replace it.
     *
     * @param other the channel to merge with
     * @return a new merged channel
     * @throws NullPointerException if {@code other} is null
     * @since 6.0.0
     */
    default AsyncChannel<T> merge(AsyncChannel<? extends T> other) {
        Objects.requireNonNull(other, "other must not be null");
        AsyncChannel<T> out = create(getCapacity());
        // A failure must not take a count. If it did, the sibling's normal
        // end could be the decrement that reaches zero and close the output
        // with no cause before the failure is retained.
        var openInputs = new AtomicInteger(2);
        var left = new MergeRelay();
        var right = new MergeRelay();
        relay(this, out, openInputs, left, right);
        relay(other, out, openInputs, right, left);
        return out;
    }

    /**
     * Forwards {@code in} to {@code out}. A failure first lets {@code sibling}
     * finish values it has already taken or that are already buffered, then
     * closes {@code out}. A normal end closes {@code out} only after every
     * input has ended the same way.
     */
    private static <T> void relay(AsyncChannel<? extends T> in, AsyncChannel<T> out,
                                  AtomicInteger openInputs, MergeRelay self, MergeRelay sibling) {
        AsyncSupport.getExecutor().execute(() -> {
            Throwable failure = null;
            int remaining = -1; // values still to forward once the sibling has failed
            try {
                while (true) {
                    // Once the sibling has failed, only the values buffered at
                    // that point are forwarded: they are already the sender's.
                    // A later value would let a busy producer hold the failure
                    // back, and receiving from an empty input would remove a
                    // value only to find the output closed.
                    if (self.isStopping()) {
                        if (remaining < 0) remaining = in.getBufferedSize();
                        if (remaining-- == 0) break;
                    }
                    Awaitable<? extends T> pending = in.receive();
                    self.publish(pending);
                    // A sender can commit between isDone() and cancel().
                    // cancel() then returns false and the value is already
                    // out of the input; awaiting it forwards that value.
                    if (self.isStopping() && !pending.isDone() && pending.cancel()) {
                        break;
                    }
                    T item;
                    try {
                        item = AsyncSupport.await(pending);
                    } catch (ChannelClosedException closed) {
                        failure = closed.getCause();
                        break;
                    } catch (CancellationException cancelled) {
                        break;
                    } finally {
                        self.clearPending(pending);
                    }
                    if (!offer(out, item)) break;
                }
            } catch (Throwable thrown) {
                Throwable unwrapped = AsyncSupport.unwrap(thrown);
                if (!(unwrapped instanceof CancellationException)) {
                    failure = unwrapped;
                }
            } finally {
                self.markIdle();
                if (failure != null) {
                    sibling.stopAndWait();
                    out.close(failure);
                } else if (openInputs.decrementAndGet() == 0) {
                    out.close();
                }
            }
        });
    }

    /**
     * Returns two new channels: elements matching the predicate go to the
     * first, non-matching to the second. Both close when this channel does,
     * and both retain a failure from this channel or from {@code predicate}.
     * Closing one result stops the split and closes the other normally; a
     * failure close of one result is not copied onto the other.
     *
     * @param predicate the split condition
     * @return a list of two channels: [matching, non-matching]
     * @since 6.0.0
     */
    default List<AsyncChannel<T>> split(Predicate<T> predicate) {
        AsyncChannel<T> trueOut = create(getCapacity());
        AsyncChannel<T> falseOut = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> pump(this, item ->
                offer(predicate.test(item) ? trueOut : falseOut, item), failure -> {
            trueOut.close(failure);
            falseOut.close(failure);
        }));
        return List.of(trueOut, falseOut);
    }

    /**
     * Returns a new channel that receives all values from this channel while
     * also sending a copy of each value to the tap channel. Useful for
     * logging, monitoring, or forking a side pipeline. A closed tap is
     * ignored and the pass-through continues. A failure from this channel
     * is retained on the result.
     *
     * @param tap the channel to send copies to
     * @return a new pass-through channel
     * @since 6.0.0
     */
    default AsyncChannel<T> tap(AsyncChannel<T> tap) {
        AsyncChannel<T> out = create(getCapacity());
        AsyncSupport.getExecutor().execute(() -> pump(this, item -> {
            offer(tap, item); // a closed tap does not stop the pass-through
            return offer(out, item);
        }, out::close));
        return out;
    }

    /**
     * Blocking sender for {@link AsyncChannel#produce(int, Consumer)}.
     * <p>
     * {@link #send(Object)} waits until the channel accepts the value, so a
     * rendezvous producer does not run ahead of its consumer.
     *
     * @param <T> the payload type
     * @since 6.0.1
     */
    final class Producer<T> {
        private final AsyncChannel<T> channel;

        private Producer(AsyncChannel<T> channel) {
            this.channel = channel;
        }

        /**
         * Sends {@code value} and waits until it is buffered or delivered.
         *
         * @param value the value to send; must not be {@code null}
         * @throws ChannelClosedException if the channel is closed
         * @throws NullPointerException if {@code value} is null
         */
        public void send(T value) {
            AsyncSupport.await(channel.send(value));
        }

        /**
         * Returns the channel this producer sends to.
         *
         * @return the underlying channel
         */
        public AsyncChannel<T> getChannel() {
            return channel;
        }
    }
}

/**
 * Handshake for the two relays of {@link AsyncChannel#merge(AsyncChannel)}.
 * A failure waits until the sibling is idle, after cancelling a receive that
 * has not taken a value, so a value already removed from that input is
 * forwarded before the output closes.
 */
final class MergeRelay {
    private final Object lock = new Object();
    /** False until the relay task exits. Starts false so a failure waits for it. */
    private boolean idle;
    private boolean stopping;
    private Awaitable<?> pending;

    boolean isStopping() {
        synchronized (lock) {
            return stopping;
        }
    }

    void publish(Awaitable<?> receive) {
        synchronized (lock) {
            pending = receive;
        }
    }

    void clearPending(Awaitable<?> receive) {
        synchronized (lock) {
            if (pending == receive) pending = null;
        }
    }

    void markIdle() {
        synchronized (lock) {
            pending = null;
            idle = true;
            lock.notifyAll();
        }
    }

    /** Cancels a receive that has not completed, then waits until the relay exits. */
    void stopAndWait() {
        Awaitable<?> toCancel;
        synchronized (lock) {
            stopping = true;
            toCancel = pending;
        }
        if (toCancel != null) toCancel.cancel();
        synchronized (lock) {
            while (!idle) {
                try {
                    lock.wait();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
