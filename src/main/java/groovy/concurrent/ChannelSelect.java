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
import org.apache.groovy.runtime.async.SelectClaim;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Selects the first offer that can complete among multiple
 * {@link AsyncChannel} operations.
 * <p>
 * This is the channel equivalent of {@link Awaitable#any(Object...)} —
 * while {@code Awaitable.any} races futures, {@code ChannelSelect} races
 * channel operations. Each call to {@link #select()} returns an
 * {@link Awaitable} that completes with a {@link Result} indicating
 * which offer committed and, for a receive, what value arrived.
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
 * A select may also mix input and output guards — the <em>mixed choice</em>
 * of the CSP literature: "I will send my opener, or take my peer's if it
 * sends first". Offers to send are created with
 * {@link #send(AsyncChannel, Object)}, offers to receive with
 * {@link #receive(AsyncChannel)}, and combined with {@link #offers(Offer...)}:
 *
 * <pre>{@code
 * import static groovy.concurrent.ChannelSelect.*
 *
 * def result = await offers(send(ping, 1), receive(pong)).select()
 * if (result.send) { ... my opener committed ... }
 * else            { ... my peer opened first: result.value ... }
 * }</pre>
 * <p>
 * A send offer commits as soon as its channel can accept the value: when a
 * receiver is waiting, or when buffer space holds it. Two peers racing a
 * mixed choice are therefore coherent — exactly one of the two openers
 * commits — only over rendezvous (capacity-0) channels, where a send cannot
 * complete unilaterally; over buffered channels with space, each peer's send
 * offer commits immediately under its own select, and the two proceed down
 * different branches of the same session.
 * <p>
 * Any branch may also carry a precondition — the <em>guarded choice</em> of
 * CSP — either written onto the offer itself with
 * {@link Offer#when(BooleanSupplier)} or passed positionally to
 * {@link #select(boolean...)}. A disabled offer is left unregistered while
 * the enabled ones keep their positions, so a guard can be masked off
 * without renumbering the branches around it.
 *
 * <pre>{@code
 * def result = await offers(receive(input).when { size < capacity },
 *                           receive(request).when { size > 0 }).select()
 * }</pre>
 * <p>
 * A deadline is just another branch. The timer offer {@link #after(long)}
 * arms a fresh timer on every {@link #select()} call, so a select held
 * across rounds waits for work, but not forever, and keeps its choice
 * policy state while it does:
 *
 * <pre>{@code
 * def sel = offers(receive(work), after(100)).fair()
 * while (true) {
 *     def result = await sel.select()
 *     if (result.timeout) { ... nothing arrived this round ... }
 * }
 * }</pre>
 *
 * For a fixed deadline across rounds, receive from a timer channel
 * instead: {@link AsyncChannel#after(long)} starts its clock when it is
 * created and delivers the instant it fired.
 * <p>
 * Inspired by GPars' {@code Select} and Go's {@code select} statement
 * (whose send cases these offers mirror).
 *
 * @since 6.0.0
 */
public final class ChannelSelect {

    /** How to choose when several offers are ready at the same time. */
    private enum Policy { PRIORITY, FAIR, RANDOM }

    /** What an offer does when it commits. */
    private enum Kind { SEND, RECEIVE, TIMER }

    private final List<Offer> offers;
    private final Policy policy;
    /** index of the last winner; only maintained under {@link Policy#FAIR} */
    private final AtomicInteger lastWinner;
    /** how many offers are timers, so a select without any allocates nothing for them */
    private final int timerCount;

    private ChannelSelect(List<Offer> offers, Policy policy) {
        this.offers = offers;
        this.policy = policy;
        this.lastWinner = policy == Policy.FAIR ? new AtomicInteger(-1) : null;
        this.timerCount = (int) offers.stream().filter(o -> o.kind == Kind.TIMER).count();
    }

    /**
     * Creates a select over receives from the given channels.
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
        List<Offer> offers = new ArrayList<>(channels.length);
        for (AsyncChannel<?> channel : channels) {
            offers.add(receive(channel));
        }
        return new ChannelSelect(List.copyOf(offers), Policy.PRIORITY);
    }

    /**
     * Creates a select over the given offers, which may mix sends and
     * receives.
     *
     * @param offers the offers to select among
     * @return a new ChannelSelect
     * @see #send(AsyncChannel, Object)
     * @see #receive(AsyncChannel)
     * @since 6.0.0
     */
    public static ChannelSelect offers(Offer... offers) {
        Objects.requireNonNull(offers, "offers must not be null");
        if (offers.length == 0) {
            throw new IllegalArgumentException("At least one offer is required");
        }
        return new ChannelSelect(List.of(offers), Policy.PRIORITY);
    }

    /**
     * An offer to transfer {@code value} into {@code channel}: an output
     * guard. It commits when the channel can accept the value — a waiting
     * receiver takes it, or buffer space holds it — and a committed offer
     * behaves exactly like {@code channel.send(value)}. A retired offer has
     * no effect on the channel.
     * <p>
     * Only channels created by {@link AsyncChannel#create} can take part in
     * the claim protocol that makes an effect-free retired send possible, so
     * only they may carry send offers.
     *
     * @param channel the channel to send into
     * @param value   the value to transfer
     * @param <V>     the payload type
     * @return the send offer
     * @throws IllegalArgumentException if the channel is not a built-in one
     * @since 6.0.0
     */
    public static <V> Offer send(AsyncChannel<V> channel, V value) {
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(value, "channel does not support null values");
        if (!(channel instanceof DefaultAsyncChannel)) {
            throw new IllegalArgumentException("send offers require a channel created by AsyncChannel.create");
        }
        return new Offer(channel, value, Kind.SEND, 0, null);
    }

    /**
     * An offer to receive the next value from {@code channel}: an input
     * guard, the branch a plain {@link #from(AsyncChannel...)} select is
     * made of.
     *
     * @param channel the channel to receive from
     * @return the receive offer
     * @since 6.0.0
     */
    public static Offer receive(AsyncChannel<?> channel) {
        Objects.requireNonNull(channel, "channel must not be null");
        return new Offer(channel, null, Kind.RECEIVE, 0, null);
    }

    /**
     * An offer that commits once {@code millis} has elapsed from the start
     * of the {@link #select()} call it takes part in: a timeout as a branch
     * of the choice, rather than an exception thrown around it by
     * {@link Awaitable#orTimeout(long, TimeUnit)}. It commits with the
     * {@link Instant} at which it fired.
     * <p>
     * Semantically it is a receive from a timer channel that the select
     * creates and, if the offer loses, cancels on every call — the
     * difference from {@code receive(AsyncChannel.after(millis))}, whose
     * clock starts once, when the channel is created. The timer offer is
     * therefore the "wait for work, but not forever" branch of a select
     * that is held and reused, where it re-arms each round while the
     * instance keeps its {@link #fair()} rotation. The channel form is
     * the fixed deadline shared by every round. Like any other offer it
     * may be guarded with {@link Offer#when}; a guarded-off timer offer
     * arms nothing.
     * <p>
     * A timer offer with a positive delay is never ready at the moment of
     * registration, so among offers that are ready at once the choice
     * policy considers only the channel offers; the timer commits only when
     * no channel offer could before it fired. A delay that is not positive
     * has already elapsed, so that offer is ready at registration and takes
     * part in the choice among ready offers like any other: under the
     * default priority it wins if listed before every ready channel offer
     * and loses to one listed before it, and {@link #fair()} rotates over it.
     * Listed last, {@code after(0)} is therefore the {@code default} clause
     * of Go's {@code select}: take a transfer if one can complete at once,
     * otherwise proceed without waiting.
     *
     * <pre>{@code
     * def result = await offers(receive(work), after(0)).select()
     * if (result.timeout) { ... nothing was ready ... }
     * }</pre>
     *
     * @param millis how long to wait, in milliseconds, from the start of
     *               each select call
     * @return the timer offer
     * @see #after(Duration)
     * @see Result#isTimeout()
     * @since 6.0.0
     */
    public static Offer after(long millis) {
        return new Offer(null, null, Kind.TIMER, TimeUnit.MILLISECONDS.toNanos(millis), null);
    }

    /**
     * An offer that commits once {@code duration} has elapsed from the
     * start of the select call it takes part in; see {@link #after(long)}.
     *
     * @param duration how long to wait from the start of each select call
     * @return the timer offer
     * @throws NullPointerException if duration is null
     * @since 6.0.0
     */
    public static Offer after(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return new Offer(null, null, Kind.TIMER, duration.toNanos(), null);
    }

    /**
     * Returns a select over the same offers that chooses fairly among
     * offers that are ready at the same time.
     * <p>
     * By default {@link #select()} prefers the offer listed first, so a
     * channel that always has a value waiting starves the ones after it. A
     * fair select instead starts each call at the offer after the one
     * that last won, so every offer that is ready is taken within
     * {@code n} calls, where {@code n} is the number of offers. This is
     * the rotating policy of JCSP's {@code fairSelect}. When no offer is
     * ready, the first to become completable wins under either policy.
     * <p>
     * The rotation state lives in the returned instance, so keep and reuse
     * it across calls (typically in a loop); a shared instance may be used
     * from several threads, in which case the rotation is best effort.
     *
     * @return a fair ChannelSelect over the same offers
     * @see #random()
     * @since 6.0.0
     */
    public ChannelSelect fair() {
        return new ChannelSelect(offers, Policy.FAIR);
    }

    /**
     * Returns a select over the same offers that chooses uniformly at
     * random among offers that are ready at the same time.
     * <p>
     * This is the policy of Go's {@code select} and GPars' {@code Select}.
     * Unlike {@link #fair()} it keeps no state between calls, so it is
     * exactly as fair from any number of threads and cannot fall into
     * lock-step with the producers, but it offers no bound on how long a
     * ready offer may be passed over. When no offer is ready, the first
     * to become completable wins under either policy.
     *
     * @return a randomly choosing ChannelSelect over the same offers
     * @see #fair()
     * @since 6.0.0
     */
    public ChannelSelect random() {
        return new ChannelSelect(offers, Policy.RANDOM);
    }

    /**
     * Waits for the first offer that can complete.
     * <p>
     * Returns an {@link Awaitable} that completes with a {@link Result}
     * naming the committed offer: the received value for an input guard,
     * the sent value for an output guard.
     * <p>
     * Exactly one offer commits. The other channels are left untouched:
     * their contents and order are preserved, and nothing remains registered
     * on them once the result completes — a retired send offer in particular
     * leaves no buffered residue and no waiting sender. When several offers
     * are ready, the one listed first commits (see {@link #fair()} for a
     * rotating choice and {@link #random()} for a random one). Cancelling the
     * result (for example through
     * {@link Awaitable#orTimeout(long, java.util.concurrent.TimeUnit)})
     * withdraws the pending offers, so a timed-out select consumes nothing
     * and sends nothing. To take a timeout as a branch instead of an
     * exception, add a timer offer from {@link #after(long)} or receive
     * from a timer channel from {@link AsyncChannel#after(long)}.
     * <p>
     * If every offer's channel is closed, the result fails with
     * {@link ChannelClosedException}. If every offer is guarded off (see
     * {@link Offer#when(BooleanSupplier)}) there is nothing to wait for and
     * the result fails with {@link IllegalStateException}.
     * <p>
     * Only channels created by {@link AsyncChannel#create} take part in the
     * claim protocol that makes this possible. Any other {@code AsyncChannel}
     * implementation consumes a value before the select can decide; if that
     * value loses, it is re-sent to its channel, which preserves it but may
     * reorder that channel. Send offers are limited to built-in channels for
     * the same reason.
     *
     * @return an awaitable result indicating which offer committed
     */
    public Awaitable<Result> select() {
        return start(registrationOrder(null));
    }

    /**
     * Waits for the first offer that can complete among those its
     * precondition enables: the guarded choice of the CSP literature, where
     * a branch is masked off for this call without disturbing the others.
     * <p>
     * Flag {@code i} enables offer {@code i}, and is conjoined with any guard
     * the offer carries of its own: an offer is registered only if both hold.
     * A disabled offer is not registered on its channel — nothing is consumed
     * from it and nothing is sent to it — but it keeps its position, so
     * {@link Result#getIndex()} still denotes the same branch whatever the
     * mask. That positional stability is the point: dropping an offer from
     * the argument list instead would silently renumber the branches after it.
     *
     * <pre>{@code
     * // the classic bounded buffer: take input only while there is room,
     * // answer requests only while there is something to hand over
     * def sel = offers(receive(input), receive(request))
     * while (true) {
     *     def result = await sel.select(size < capacity, size > 0)
     *     if (result.index == 0) { ... buffer result.value ... }
     *     else                   { ... reply with the oldest value ... }
     * }
     * }</pre>
     * <p>
     * In every other respect this behaves as {@link #select()}: the enabled
     * offers are scanned in the order the choice policy dictates, and exactly
     * one commits.
     *
     * @param enabled one flag per offer, in offer order
     * @return an awaitable result indicating which offer committed, or one
     *         that fails with {@link IllegalStateException} if every offer is
     *         disabled
     * @throws IllegalArgumentException if the number of flags is not the
     *         number of offers
     * @see Offer#when(BooleanSupplier)
     * @since 6.0.0
     */
    public Awaitable<Result> select(boolean... enabled) {
        Objects.requireNonNull(enabled, "enabled must not be null");
        if (enabled.length != offers.size()) {
            throw new IllegalArgumentException("Expected " + offers.size()
                    + " precondition(s), one per offer, but got " + enabled.length);
        }
        return start(registrationOrder(enabled));
    }

    private Awaitable<Result> start(int[] order) {
        int count = order.length;
        if (count == 0) {
            CompletableFuture<Result> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("every offer of the select is disabled"));
            return GroovyPromise.of(failed);
        }
        Winner winner = new Winner();
        AtomicInteger closedCount = new AtomicInteger();
        Awaitable<?>[] branches = new Awaitable<?>[offers.size()];
        // the timers armed for this call, closed (and so cancelled) with the losers
        AsyncChannel<?>[] timers = timerCount == 0 ? null : new AsyncChannel<?>[offers.size()];

        // a ready offer completes synchronously during registration, so the
        // registration order is the priority order: rotate it under fair(),
        // shuffle it under random() (a random start alone would favour a
        // ready offer by the run of not-ready offers before it)
        for (int k = 0; k < count && !winner.isDone(); k++) {
            final int index = order[k];
            final Offer offer = offers.get(index);
            final boolean timer = offer.kind == Kind.TIMER;
            final AsyncChannel<?> ch = timer
                    ? DefaultAsyncChannel.after(offer.delayNanos, TimeUnit.NANOSECONDS)
                    : offer.channel;
            final boolean claimable = ch instanceof DefaultAsyncChannel;
            @SuppressWarnings("unchecked")
            Awaitable<?> branch = offer.kind == Kind.SEND
                    ? ((DefaultAsyncChannel<Object>) ch).sendIfUnclaimed(offer.value, winner.claim)
                    : claimable
                        ? ((DefaultAsyncChannel<?>) ch).receiveIfUnclaimed(winner.claim)
                        : ch.receive();
            branches[index] = branch;
            if (timer) timers[index] = ch;
            CompletableFuture<?> future = branch.toCompletableFuture();
            future.whenComplete((value, error) -> {
                if (error == null) {
                    // a claimable channel committed the claim as it completed; any
                    // other channel has consumed a value and must compete for it now
                    if (claimable || winner.claim.tryCommit(future)) {
                        withdraw(branches, timers); // losers registered so far, before the caller sees the result
                        winner.complete(new Result(index, offer.kind == Kind.SEND ? offer.value : value,
                                offer.kind, timer ? null : ch)); // holding the claim, this cannot fail
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
            withdraw(branches, timers);
            if (result != null && lastWinner != null) lastWinner.set(result.index);
        });
        return GroovyPromise.of(winner);
    }

    /**
     * The offer indices to register, in registration order, omitting those
     * disabled by the positional preconditions ({@code null} enables every
     * position) or by a guard the offer carries.
     */
    private int[] registrationOrder(boolean[] enabled) {
        int count = offers.size();
        int[] order = new int[count];
        int size = 0;
        switch (policy) {
            case PRIORITY -> {
                for (int i = 0; i < count; i++) {
                    if (isEnabled(i, enabled)) order[size++] = i;
                }
            }
            case FAIR -> {
                int start = Math.floorMod(lastWinner.get() + 1, count);
                for (int i = 0; i < count; i++) {
                    int index = (start + i) % count;
                    if (isEnabled(index, enabled)) order[size++] = index;
                }
            }
            case RANDOM -> { // Fisher-Yates: every ready offer is equally likely to be met first
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (int i = 0; i < count; i++) {
                    if (!isEnabled(i, enabled)) continue;
                    int j = random.nextInt(size + 1);
                    order[size++] = order[j];
                    order[j] = i;
                }
            }
        }
        return size == count ? order : Arrays.copyOf(order, size);
    }

    /**
     * Whether offer {@code index} takes part in this select: its positional
     * precondition and its own guard must both hold. Each guard is consulted
     * once per {@link #select()} call, in offer order under
     * {@link Policy#PRIORITY} and {@link Policy#RANDOM} and in rotation order
     * under {@link Policy#FAIR}.
     */
    private boolean isEnabled(int index, boolean[] enabled) {
        return (enabled == null || enabled[index]) && offers.get(index).isEnabled();
    }

    /**
     * Withdraws every registered branch, and closes every timer armed for
     * the call so its scheduled firing is cancelled. Branches go first: a
     * timer's receive is then already cancelled when the close would fail
     * it, so the close counts no spurious closed branch. Closing takes the
     * timer's lock, which is safe from inside another channel's delivery
     * because a firing timer does only lock-free work while it holds it and
     * never takes another channel's lock.
     */
    private static void withdraw(Awaitable<?>[] branches, AsyncChannel<?>[] timers) {
        for (Awaitable<?> branch : branches) {
            if (branch != null) branch.cancel();
        }
        if (timers != null) {
            for (AsyncChannel<?> timer : timers) {
                if (timer != null) timer.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void resend(AsyncChannel<?> ch, Object value) {
        // best effort: if the channel has since been closed the value cannot be preserved
        ((AsyncChannel<Object>) ch).send(value);
    }

    /**
     * One branch of a select: an input guard created by
     * {@link #receive(AsyncChannel)}, an output guard created by
     * {@link #send(AsyncChannel, Object)}, or a timeout created by
     * {@link #after(long)}. Immutable and freely reusable across selects.
     *
     * @since 6.0.0
     */
    public static final class Offer {
        /** the channel operated on, or {@code null} for a timer */
        private final AsyncChannel<?> channel;
        private final Object value;
        private final Kind kind;
        /** the timer delay; meaningful only when {@link #kind} is {@link Kind#TIMER} */
        private final long delayNanos;
        /** the guard, or {@code null} when the offer is unconditional */
        private final BooleanSupplier guard;

        private Offer(AsyncChannel<?> channel, Object value, Kind kind, long delayNanos, BooleanSupplier guard) {
            this.channel = channel;
            this.value = value;
            this.kind = kind;
            this.delayNanos = delayNanos;
            this.guard = guard;
        }

        /**
         * Returns a copy of this offer guarded by {@code condition}: the
         * precondition written onto the branch itself, as occam, Ada, Erlang
         * and Kotlin write it, rather than passed positionally to
         * {@link ChannelSelect#select(boolean...)}.
         *
         * <pre>{@code
         * def sel = offers(receive(input).when { size < capacity },
         *                  receive(request).when { size > 0 })
         * def result = await sel.select()
         * }</pre>
         * <p>
         * The condition is evaluated afresh on every {@link
         * ChannelSelect#select()} call — that is why it is a supplier and not
         * a boolean — so one guarded select may be held and reused as the
         * state it guards on changes. When it does not hold, the offer is not
         * registered on its channel: nothing is taken from it and nothing is
         * sent to it, and the remaining offers keep their positions, so
         * {@link Result#getIndex()} denotes the same branch either way.
         * <p>
         * Guards conjoin: over a second {@code when} both conditions must
         * hold, as must the corresponding flag of a
         * {@link ChannelSelect#select(boolean...)} call. If no offer of a
         * select is enabled the result fails with
         * {@link IllegalStateException}, since there is nothing left to wait
         * for.
         *
         * @param condition the guard, consulted once per select
         * @return a guarded copy of this offer
         * @since 6.0.0
         */
        public Offer when(BooleanSupplier condition) {
            Objects.requireNonNull(condition, "condition must not be null");
            BooleanSupplier outer = guard;
            return new Offer(channel, value, kind, delayNanos,
                    outer == null ? condition : () -> outer.getAsBoolean() && condition.getAsBoolean());
        }

        boolean isEnabled() {
            return guard == null || guard.getAsBoolean();
        }
    }

    /**
     * The result of one select. Its {@link #claim} is the single winner
     * state: an offer takes it as its channel commits the transfer, and
     * cancelling the result competes for the same claim, so a cancel arriving
     * after a commit has claimed is refused and the result reaches the
     * caller, while a cancel that wins the claim guarantees no offer commits.
     */
    private static final class Winner extends CompletableFuture<Result> {
        final SelectClaim claim = new SelectClaim();

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return claim.tryCommitCancel() && super.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * The result of a {@link #select()} operation, indicating which
     * offer committed.
     *
     * @since 6.0.0
     */
    public static final class Result {
        private final int index;
        private final Object value;
        private final Kind kind;
        private final AsyncChannel<?> channel;

        /**
         * Creates a selection result.
         *
         * @param index   the zero-based index of the committed offer
         * @param value   the received value, the sent value for a send offer,
         *                or the firing instant for a timer offer
         * @param kind    what the committed offer did
         * @param channel the channel the committed offer transferred over, or
         *                {@code null} for a timer offer
         */
        Result(int index, Object value, Kind kind, AsyncChannel<?> channel) {
            this.index = index;
            this.value = value;
            this.kind = kind;
            this.channel = channel;
        }

        /** The zero-based index of the offer that committed. */
        public int getIndex() { return index; }

        /**
         * The channel the committed offer transferred over: the one received
         * from, or the one sent to. Lets a branch be recognised by identity
         * rather than by position, which suits a select whose offers are
         * assembled dynamically. A timer offer transfers over no channel of
         * the caller's, so for a timeout this is {@code null}: a select that
         * carries a timer offer and dispatches on the channel must test
         * {@link #isTimeout()} first, and dereference the channel only when
         * that is {@code false}.
         *
         * @return the winning channel, or {@code null} for a timeout
         * @see #isTimeout()
         * @since 6.0.0
         */
        public /*@Nullable*/ AsyncChannel<?> getChannel() { return channel; }

        /**
         * The transferred value: what arrived, for a receive offer; what was
         * sent, for a send offer; the {@link Instant} it fired, for a timer
         * offer.
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue() { return (T) value; }

        /**
         * Whether the committed offer was a send (an output guard).
         *
         * @since 6.0.0
         */
        public boolean isSend() { return kind == Kind.SEND; }

        /**
         * Whether the committed offer was a timer created by
         * {@link ChannelSelect#after(long)}: the select timed out, and
         * {@link #getValue()} is the instant at which it did.
         *
         * @since 6.0.0
         */
        public boolean isTimeout() { return kind == Kind.TIMER; }

        /**
         * Returns a diagnostic representation of this selection result.
         *
         * @return the selection result description
         */
        @Override
        public String toString() {
            return switch (kind) {
                case SEND -> "SelectResult[sent to channel=" + index + ", value=" + value + "]";
                case RECEIVE -> "SelectResult[channel=" + index + ", value=" + value + "]";
                case TIMER -> "SelectResult[timeout=" + index + ", at=" + value + "]";
            };
        }
    }
}
