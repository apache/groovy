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

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Construction-time configuration for an {@link Actor}.
 * <p>
 * Carries the mailbox capacity / overflow policy, the optional stash
 * capacity / overflow policy, the executor, and the opt-in flag for
 * {@link Actor#currentSelf()} thread-local support. Pass to the
 * {@link Actor#reactor(java.util.function.Function, ActorOptions)} or
 * {@link Actor#stateful(Object, java.util.function.BiFunction, ActorOptions)}
 * factory variants. Settings that can be changed after construction
 * (currently: the error handler) are not carried here — see
 * {@link Actor#onError(java.util.function.BiConsumer)}.
 *
 * <pre>{@code
 * var actor = Actor.reactor(handler,
 *     ActorOptions.DEFAULTS
 *         .withBoundedMailbox(1000, ActorOptions.Overflow.BLOCK)
 *         .withStashBound(64, ActorOptions.StashOverflow.REJECT)
 *         .withExecutor(Pool.cpu()));
 * }</pre>
 *
 * @param mailboxCapacity    the maximum mailbox (queue) size; {@code 0}
 *                           for unbounded
 * @param overflow           the policy when a bounded mailbox is full;
 *                           ignored when {@code mailboxCapacity} is {@code 0}
 * @param stashCapacity      the maximum stash buffer size; {@code 0}
 *                           for unbounded (the back-compat default)
 * @param stashOverflow      the policy when a bounded stash is full;
 *                           ignored when {@code stashCapacity} is {@code 0}
 * @param executor           the executor used to run the actor's processing
 *                           loop; {@code null} selects the default async executor
 * @param currentSelfEnabled when {@code true}, the actor publishes itself
 *                           via a thread-local during handler dispatch so
 *                           that {@link Actor#currentSelf()} can be used
 *                           from inside handlers. Off by default to avoid
 *                           paying for a mechanism the actor doesn't use.
 * @since 6.0.0
 */
public record ActorOptions(int mailboxCapacity, Overflow overflow,
                           int stashCapacity, StashOverflow stashOverflow,
                           Executor executor, boolean currentSelfEnabled) {

    /**
     * Policy applied when {@link Actor#send(Object)} is called on an actor
     * whose bounded mailbox is full.
     *
     * @since 6.0.0
     */
    public enum Overflow {
        /** The sending thread blocks until space is available. */
        BLOCK,
        /** The new message is silently dropped. */
        DROP_NEWEST,
        /** {@link Actor#send(Object)} throws {@link IllegalStateException}. */
        FAIL
    }

    /**
     * Policy applied when {@link ActorContext#stash()} is called and the
     * actor's bounded stash is already at capacity.
     * <p>
     * There is no {@code BLOCK} variant: the handler running {@code stash()}
     * is on the actor's only worker thread, so blocking it on stash capacity
     * would deadlock.
     *
     * @since 6.0.0
     */
    public enum StashOverflow {
        /**
         * {@link ActorContext#stash()} throws {@link IllegalStateException}.
         * The exception propagates out of the handler unless caught; if
         * uncaught, the dispatch treats it as a normal handler failure
         * (reply bound to the exception, {@code onError} fires).
         */
        FAIL,
        /**
         * The oldest stashed message is evicted to make room for the
         * current one. If the evicted message originated from
         * {@link Actor#sendAndGet(Object)}, its reply is bound to
         * {@link IllegalStateException} so the caller does not wait forever.
         */
        DROP_OLDEST,
        /**
         * The current message is rejected: any {@code sendAndGet} reply
         * is bound to {@link IllegalStateException}; any pending state
         * change computed by the current handler is discarded; the
         * message is <em>not</em> added to the stash and will not be
         * replayed.
         */
        REJECT
    }

    /**
     * Default options: unbounded mailbox, unbounded stash, default async
     * executor, no thread-local current-self.
     * <p>
     * Note: the {@code overflow} and {@code stashOverflow} values carried
     * by {@code DEFAULTS} are placeholders, used only when the
     * corresponding capacity is later raised above zero via
     * {@link #withBoundedMailbox(int, Overflow)} or
     * {@link #withStashBound(int, StashOverflow)}. Read
     * {@link #isBounded()} / {@link #isStashBounded()} before treating
     * {@link #overflow()} / {@link #stashOverflow()} as meaningful.
     */
    public static final ActorOptions DEFAULTS = new ActorOptions(
            0, Overflow.BLOCK, 0, StashOverflow.FAIL, null, false);

    /**
     * Canonical constructor — validates that capacities are non-negative
     * and that the overflow policies are non-null. The executor may be
     * {@code null} (meaning "use the default").
     */
    public ActorOptions {
        if (mailboxCapacity < 0) {
            throw new IllegalArgumentException(
                    "mailboxCapacity must be >= 0, got " + mailboxCapacity);
        }
        if (stashCapacity < 0) {
            throw new IllegalArgumentException(
                    "stashCapacity must be >= 0, got " + stashCapacity);
        }
        Objects.requireNonNull(overflow, "overflow must not be null");
        Objects.requireNonNull(stashOverflow, "stashOverflow must not be null");
    }

    /**
     * Returns a copy of these options configured with a bounded mailbox
     * of the given capacity and overflow strategy.
     * <p>
     * Note: {@code capacity == 0} is rejected here even though the
     * canonical constructor accepts it. The constructor's {@code 0}
     * means "explicitly unbounded"; passing {@code 0} to this builder
     * almost always indicates a missing real capacity, so it fails fast.
     * To revert a bounded actor to unbounded, build a new options
     * instance from {@link #DEFAULTS}.
     *
     * @param capacity the mailbox capacity (must be positive)
     * @param strategy the overflow policy
     * @return a new {@code ActorOptions} with the bounded mailbox applied
     * @throws IllegalArgumentException if {@code capacity} is not positive
     */
    public ActorOptions withBoundedMailbox(int capacity, Overflow strategy) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "capacity must be positive, got " + capacity);
        }
        Objects.requireNonNull(strategy, "strategy must not be null");
        return new ActorOptions(capacity, strategy, stashCapacity, stashOverflow,
                executor, currentSelfEnabled);
    }

    /**
     * Returns a copy of these options configured with a bounded stash
     * buffer of the given capacity and overflow strategy. Unbounded by
     * default — set a bound when the actor accepts messages from a
     * source whose volume you do not control and the actor may stay in
     * a stashing phase indefinitely.
     *
     * @param capacity the stash capacity (must be positive)
     * @param strategy the overflow policy
     * @return a new {@code ActorOptions} with the bounded stash applied
     * @throws IllegalArgumentException if {@code capacity} is not positive
     * @since 6.0.0
     */
    public ActorOptions withStashBound(int capacity, StashOverflow strategy) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "capacity must be positive, got " + capacity);
        }
        Objects.requireNonNull(strategy, "strategy must not be null");
        return new ActorOptions(mailboxCapacity, overflow, capacity, strategy,
                executor, currentSelfEnabled);
    }

    /**
     * Returns a copy of these options configured to use the given executor
     * for the actor's processing loop. Pass {@code null} to revert to the
     * default async executor.
     *
     * @param executor the executor to use, or {@code null} for the default
     * @return a new {@code ActorOptions} with the executor applied
     */
    public ActorOptions withExecutor(Executor executor) {
        return new ActorOptions(mailboxCapacity, overflow, stashCapacity, stashOverflow,
                executor, currentSelfEnabled);
    }

    /**
     * Returns a copy of these options with thread-local {@code currentSelf}
     * support toggled. When enabled, the actor publishes itself into a
     * thread-local for the duration of each handler dispatch so that
     * {@link Actor#currentSelf()} returns the executing actor.
     * <p>
     * Off by default. Prefer the context-aware {@link StatefulHandler} /
     * {@link ReactorHandler} factories where possible; this knob exists
     * for callers who want self-stop without restructuring to the
     * context-aware handler shape.
     *
     * @param enabled {@code true} to enable {@code Actor.currentSelf()}
     *                support; {@code false} to disable
     * @return a new {@code ActorOptions} with the flag applied
     * @since 6.0.0
     */
    public ActorOptions withCurrentSelf(boolean enabled) {
        return new ActorOptions(mailboxCapacity, overflow, stashCapacity, stashOverflow,
                executor, enabled);
    }

    /** Returns {@code true} if the mailbox is bounded. */
    public boolean isBounded() {
        return mailboxCapacity > 0;
    }

    /** Returns {@code true} if the stash buffer is bounded. */
    public boolean isStashBounded() {
        return stashCapacity > 0;
    }
}
