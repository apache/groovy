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
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    /** How to choose when several channels are ready at the same time. */
    private enum Policy { PRIORITY, FAIR, RANDOM }

    private final List<AsyncChannel<?>> channels;
    private final Policy policy;
    /** index of the last winner; only maintained under {@link Policy#FAIR} */
    private final AtomicInteger lastWinner;

    private ChannelSelect(List<AsyncChannel<?>> channels, Policy policy) {
        this.channels = channels;
        this.policy = policy;
        this.lastWinner = policy == Policy.FAIR ? new AtomicInteger(-1) : null;
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
        return new ChannelSelect(List.of(channels), Policy.PRIORITY);
    }

    /**
     * Returns a select over the same channels that chooses fairly among
     * channels that are ready at the same time.
     * <p>
     * By default {@link #select()} prefers the channel listed first, so a
     * channel that always has a value waiting starves the ones after it. A
     * fair select instead starts each call at the channel after the one
     * that last won, so every channel that is ready is taken within
     * {@code n} calls, where {@code n} is the number of channels. This is
     * the rotating policy of JCSP's {@code fairSelect}. When no channel is
     * ready, the first value to arrive wins under either policy.
     * <p>
     * The rotation state lives in the returned instance, so keep and reuse
     * it across calls (typically in a loop); a shared instance may be used
     * from several threads, in which case the rotation is best effort.
     *
     * @return a fair ChannelSelect over the same channels
     * @see #random()
     * @since 6.0.0
     */
    public ChannelSelect fair() {
        return new ChannelSelect(channels, Policy.FAIR);
    }

    /**
     * Returns a select over the same channels that chooses uniformly at
     * random among channels that are ready at the same time.
     * <p>
     * This is the policy of Go's {@code select} and GPars' {@code Select}.
     * Unlike {@link #fair()} it keeps no state between calls, so it is
     * exactly as fair from any number of threads and cannot fall into
     * lock-step with the producers, but it offers no bound on how long a
     * ready channel may be passed over. When no channel is ready, the first
     * value to arrive wins under either policy.
     *
     * @return a randomly choosing ChannelSelect over the same channels
     * @see #fair()
     * @since 6.0.0
     */
    public ChannelSelect random() {
        return new ChannelSelect(channels, Policy.RANDOM);
    }

    /**
     * Waits for the first value available from any of the channels.
     * <p>
     * Returns an {@link Awaitable} that completes with a {@link Result}
     * containing the channel index and the received value.
     * <p>
     * Exactly one value is taken, from exactly one channel. The other
     * channels are left untouched: their contents and order are preserved,
     * and nothing remains registered on them once the result completes.
     * When several channels already hold a value, the one listed first is
     * taken (see {@link #fair()} for a rotating choice and {@link #random()}
     * for a random one). Cancelling the
     * result (for example through
     * {@link Awaitable#orTimeout(long, java.util.concurrent.TimeUnit)})
     * withdraws the pending receives, so a timed-out select consumes
     * nothing.
     * <p>
     * If every channel is closed and drained, the result fails with
     * {@link ChannelClosedException}.
     * <p>
     * Only channels created by {@link AsyncChannel#create} take part in the
     * claim protocol that makes this possible. Any other {@code AsyncChannel}
     * implementation consumes a value before the select can decide; if that
     * value loses, it is re-sent to its channel, which preserves it but may
     * reorder that channel.
     *
     * @return an awaitable result indicating which channel produced the value
     */
    public Awaitable<Result> select() {
        int count = channels.size();
        Winner winner = new Winner();
        AtomicInteger closedCount = new AtomicInteger();
        Awaitable<?>[] branches = new Awaitable<?>[count];

        // a ready channel completes synchronously during registration, so the
        // registration order is the priority order: rotate it under fair(),
        // shuffle it under random() (a random start alone would favour a
        // ready channel by the run of not-ready channels before it)
        int[] order = registrationOrder(count);
        for (int k = 0; k < count && !winner.isDone(); k++) {
            final int index = order[k];
            AsyncChannel<?> ch = channels.get(index);
            final boolean claimable = ch instanceof DefaultAsyncChannel;
            Awaitable<?> branch = claimable
                    ? ((DefaultAsyncChannel<?>) ch).receiveIfUnclaimed(winner.claim)
                    : ch.receive();
            branches[index] = branch;
            branch.toCompletableFuture().whenComplete((value, error) -> {
                if (error == null) {
                    // a claimable channel took the claim as it delivered; any other
                    // channel has consumed a value and must compete for it now
                    if (claimable || winner.claim.compareAndSet(false, true)) {
                        withdraw(branches); // losers registered so far, before the caller sees the result
                        winner.complete(new Result(index, value)); // holding the claim, this cannot fail
                    } else {
                        resend(ch, value);
                    }
                } else if (error instanceof ChannelClosedException && closedCount.incrementAndGet() == count) {
                    winner.completeExceptionally(new ChannelClosedException("all channels are closed"));
                }
            });
        }
        // branches registered after the win, and cancellation of the result itself
        winner.whenComplete((result, error) -> {
            withdraw(branches);
            if (result != null && lastWinner != null) lastWinner.set(result.index);
        });
        return GroovyPromise.of(winner);
    }

    private int[] registrationOrder(int count) {
        int[] order = new int[count];
        switch (policy) {
            case PRIORITY -> {
                for (int i = 0; i < count; i++) order[i] = i;
            }
            case FAIR -> {
                int start = Math.floorMod(lastWinner.get() + 1, count);
                for (int i = 0; i < count; i++) order[i] = (start + i) % count;
            }
            case RANDOM -> { // Fisher-Yates: every ready channel is equally likely to be met first
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (int i = 0; i < count; i++) {
                    int j = random.nextInt(i + 1);
                    order[i] = order[j];
                    order[j] = i;
                }
            }
        }
        return order;
    }

    private static void withdraw(Awaitable<?>[] branches) {
        for (Awaitable<?> branch : branches) {
            if (branch != null) branch.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private static void resend(AsyncChannel<?> ch, Object value) {
        // best effort: if the channel has since been closed the value cannot be preserved
        ((AsyncChannel<Object>) ch).send(value);
    }

    /**
     * The result of one select. Its {@link #claim} is the single winner
     * state: a channel takes it as it hands a value over, and cancelling
     * the result competes for the same claim, so a cancel arriving after a
     * delivery has claimed is refused and the value reaches the caller,
     * while a cancel that wins the claim guarantees no channel delivers.
     */
    private static final class Winner extends CompletableFuture<Result> {
        final AtomicBoolean claim = new AtomicBoolean();

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return claim.compareAndSet(false, true) && super.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * The result of a {@link #select()} operation, indicating which
     * channel produced the value.
     *
     * @since 6.0.0
     */
    public static final class Result {
        private final int index;
        private final Object value;

        /**
         * Creates a selection result.
         *
         * @param index the zero-based index of the selected channel
         * @param value the received value
         */
        Result(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        /** The zero-based index of the channel that produced the value. */
        public int getIndex() { return index; }

        /** The received value. */
        @SuppressWarnings("unchecked")
        public <T> T getValue() { return (T) value; }

        /**
         * Returns a diagnostic representation of this selection result.
         *
         * @return the selection result description
         */
        @Override
        public String toString() {
            return "SelectResult[channel=" + index + ", value=" + value + "]";
        }
    }
}
